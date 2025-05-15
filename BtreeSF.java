//------------------------------------------------------------------------------
// BtreeDM with pipelined free chain and single access memory.
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024-2025
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Btree in a block on the surface of a silicon chip.
// Double allocation would be faster as allocations are often done in pairs
// Node confirm that a load or a save identified by the trace back is actually changing the node - eliminate this that never have an effect
// Stuck - get penultimate element. Use currentSize field directly instead of copy in it to a temporary variable
// Use free in node to hold node number while allocated so that Node knows where to write it back to without being told
// Start splitting lower down and merge only along split path
// Separate nano9k gen from silicon compiler gen
// Try memory operations in parallel in class Node without getting congestion complaints from silicon compiler
// +Remove removable memories as the use of local registers eliminates the need for them
// Investigate whether it would be worth changing from a block of variables in T to individual memories for each variable now in T.  This would localize access which would simplify routing at the cost of more silicon spent on registers.
// Can MemoryLayoutDM be merged with Layout?  I.e. each field laid out would know what memory it resides in.
// Testing the effect of split branch having its own node buffers
import java.util.*;
import java.nio.file.*;

abstract class BtreeSF extends Test                                             // Manipulate a btree using static methods and memory
 {final BtreeSF            thisBTree = this;                                    // Direct access to this btree even when this goes out of range
  final MemoryLayoutDM             F;                                           // Free chain of nodes not currently in use
  final MemoryLayoutDM             M;                                           // The memory layout of the btree
  final MemoryLayoutDM             T;                                           // The memory used to hold temporary variable used during a transaction on the btree
  final ProgramDM                  P = new ProgramDM();                         // Program in which to generate instructions
  final boolean              OpCodes = true;                                    // Refactor op codes
  final boolean           runVerilog = true;                                    // Run verilog tests alongside java tests and check they produce the same results
  final String     designDescription = "? versus |";                            // Description of latest change
  //final static TreeMap<String,String>removableMemories = new TreeMap<>();     // Record memories that can be removed from each project as they are not used
  final String     processTechnology = "freepdk45";                             // Process technology from: https://docs.siliconcompiler.com/en/stable/#supported-technologies . Ask chat for details of each.
  abstract int maxSize();                                                       // The maximum number of leaves plus branches in the bree
  abstract int bitsPerKey();                                                    // The number of bits per key
  abstract int bitsPerData();                                                   // The number of bits per data
  abstract int maxKeysPerLeaf();                                                // Maximum number of leafs in a key
  abstract int maxKeysPerBranch();                                              // Maximum number of keys in a branch
  final    int splitLeafSize;                                                   // The number of key, data pairs to split out of a leaf
  final    int splitBranchSize;                                                 // The number of key, next pairs to split out of a branch
  final    int bitsPerAddress;                                                  // The number of bits required to address a bit in memory
  final    int bitsPerNext;                                                     // The number of bits in a next field
  final    int bitsPerSize;                                                     // The number of bits in stuck size field
  final static String verilogFolder = "verilog";                                // The folder to write verilog into
  static   int widthOfMarginInExecutionTrace = 18;                              // The width of the left margin in verilog traces that is to be protected from differential writing

  Layout.Field     leaf;                                                        // Layout of a leaf in the memory used by btree
  Layout.Field     branch;                                                      // Layout of a branch in the memory used by btree
  Layout.Union     branchOrLeaf;                                                // Layout of either a leaf or a branch in the memory used by btree
  Layout.Bit       isLeaf;                                                      // Whether the current node is a leaf or a branch
  Layout.Variable  free;                                                        // Free list chain
  Layout.Field     node;                                                        // Layout of a node in the memory used by btree
  Layout.Array     bTreeNodes;                                                  // Layout of an array of nodes in the memory used by btree
  Layout.Variable  freeList;                                                    // Single linked list of nodes that have been freed and so can be reused without fragmenting memory
  Layout.Structure bTree;                                                       // Btree
  Layout.Field     bTree_free;                                                  // Free list in node observed from btree main memory and thus likely to add to memory congestion unless separated out into a spearate memory block
//Layout.Field     bTree_isLeaf;                                                // Is a leaf in node observed from btree main memory and thus likely to add to memory congestion unless separated out into a spearate memory block
  Layout.Field     node_size;                                                   // Size of a node observed from a node
  Layout.Field     freeChainHead;                                               // Head of the free list

  Layout           nodeLayout;                                                  // Layout of a node in the btree
  Layout      freeChainLayout;                                                  // Layout of the head of the free chain

  final static int
    linesToPrintABranch =  4,                                                   // The number of lines required to print a branch
         maxPrintLevels = 10,                                                   // Maximum number of levels to print in a tree
               maxDepth = 99;                                                   // Maximum depth of any realistic tree

  int          nodeUsed = 0;                                                    // Number of nodes currently in use
//int       maxNodeUsed = 0;                                                    // Maximum number of branches plus leaves used

  final int        root = 0;                                                    // The root of the tree is always node zero

  final int Branch_Size        = 0;                                             // Get the size of a stuck
  final int Branch_Leaf        = 0;                                             // Check whether a node has leaves for children
  final int Branch_Top         = 0;                                             // Get the top element of a branch
  final int Branch_FirstBranch = 0;                                             // Locate the first greater or equal key in a branch
  final int Branch_T           = 1;                                             // Process a parent node
  final int Branch_tl          = 2;                                             // Process a left node
  final int Branch_tr          = 3;                                             // Process a right node
  final int Branch_length      = 4;                                             // Number of transaction types

  final int Leaf_Size          = 0;                                             // Get the size of a stuck
  final int Leaf_Leaf          = 0;                                             // Check whether a node has leaves for children
  final int Leaf_Equal         = 0;                                             // Get the top element of a branch
  final int Leaf_FirstLeaf     = 0;                                             // Locate the first greater or equal key in a branch
  final int Leaf_T             = 1;                                             // Process a parent node
  final int Leaf_tl            = 2;                                             // Process a left node
  final int Leaf_tr            = 3;                                             // Process a right node
  final int Leaf_length        = 4;                                             // Number of transaction types

  final StuckDM bSize;                                                          // Branch size
  final StuckDM bLeaf;                                                          // Check whether a node has leaves for children
  final StuckDM bTop;                                                           // Get the size of a stuck
  final StuckDM bFirstBranch;                                                   // Locate the first greater or equal key in a branch
  final StuckDM bT;                                                             // Process a parent node
  final StuckDM bL;                                                             // Process a left node
  final StuckDM bR;                                                             // Process a right node

  final StuckDM lSize;                                                          // Branch size
  final StuckDM lLeaf;                                                          // Check whether a node has leaves for children
  final StuckDM lEqual;                                                         // Locate an equal key
  final StuckDM lFirstLeaf;                                                     // Locate the first greater or equal key in a leaf
  final StuckDM lT;                                                             // Process a parent node as a leaf
  final StuckDM lL;                                                             // Process a left node
  final StuckDM lR;                                                             // Process a right node

  final Node nT;                                                                // Memory sufficient to contain a single parent node
  final Node nL;                                                                // Memory sufficient to contain a single left node
  final Node nR;                                                                // Memory sufficient to contain a single right node
  final Node nC;                                                                // Memory sufficient to contain a single child node - equated to a left node until we dicover the  need for a separate memory area
  final Node memoryIn;                                                          // The nodes that interfaces with memory

  static int testNumber = 0;                                                    // Number of the current test
  boolean         debug = false;                                                // Debugging enabled

//D1 Construction                                                               // Create a Btree from nodes which can be branches or leaves.  The data associated with the BTree is stored only in the leaves opposite the keys

  BtreeSF()                                                                     // Define a Btree with user specified dimensions
   {zz();
    splitLeafSize   = maxKeysPerLeaf()   >> 1;                                  // The number of key, data pairs to split out of a leaf
    splitBranchSize = maxKeysPerBranch() >> 1;                                  // The number of key, next pairs to split out of a branch
    bitsPerNext     = logTwo(maxSize());                                        // The number of bits in a next field sufficient to index any node
    bitsPerSize     = logTwo(max(bitsPerKey(), bitsPerData())+1);               // The number of bits in stuck size field sufficient to index an key or data element including top

    F = new MemoryLayoutDM(freeChainLayout(),   "F");                           // The memory layout of the free chain list header
    M = new MemoryLayoutDM(layout(),            "M");                           // The memory layout of the btree
    bitsPerAddress = logTwo(M.memory().size());                                 // Number of bits to address any bit in memory
    T = new MemoryLayoutDM(transactionLayout(), "T");                           // The memory used to hold temporary variable used during a transaction on the btree

    F.program(P); M.program(P); T.program(P);

    T.at(maxKeysPerLeaf)  .setInt(maxKeysPerLeaf());
    T.at(maxKeysPerBranch).setInt(maxKeysPerBranch());
    T.at(two)             .setInt(2);
//  T.at(MaxDepth)        .setInt(maxDepth);                                    // Prevent runaway searches of the btree by limiting the number of levels to be searched

    bSize        = //branchTransactions[Branch_Size       ];                    // Branch size
    bLeaf        = //branchTransactions[Branch_Leaf       ];                    // Check whether a node has leaves for children
    bTop         = //branchTransactions[Branch_Top        ];                    // Get the size of a stuck
    bFirstBranch = //branchTransactions[Branch_FirstBranch];                    // Locate the first greater or equal key in a branch
    bT           = createBranchStuck("bT");                                     // Process a parent node
    bL           = createBranchStuck("bL");                                     // Process a left node
    bR           = createBranchStuck("bR");                                     // Process a right node

    lSize        =   //leafTransactions[Leaf_Size         ];                    // Leaf size
    lLeaf        =   //leafTransactions[Leaf_Leaf         ];                    // Print a leaf
    lEqual       =   //leafTransactions[Leaf_Equal        ];                    // Locate an equal key
    lFirstLeaf   =   //leafTransactions[Leaf_FirstLeaf    ];                    // Locate the first greater or equal key in a leaf
    lT           = createLeafStuck("lT");                                       // Process a parent node
    lL           = createLeafStuck("lL");                                       // Process a left node
    lR           = createLeafStuck("lR");                                       // Process a right node

    nT = new Node("nT");
    nL = new Node("nL"); nC = nL;
    nR = new Node("nR");
    memoryIn = new Node("memoryIn");

    P.new I()
     {void a()
       {final int N = maxSize();                                                // Put all the nodes on the free chain at the start with low nodes first
        for (int i = N; i > 0; --i) setInt(bTree_free, (i == N ? 0 : i), i - 1);// Link this node to the previous node
        F.setInt(freeChainHead, root);                                          // Root is first on free chain
       }
      String v() {return "/* Construct Free list */";}
     };
    allocate(false);                                                            // The root is always at zero, which frees zero to act as the end of list marker on the free chain

    nC.loadRoot();                                                              // Load the allocated node
    nC.setLeaf();                                                               // Set the root as a leaf
    nC.saveRoot();                                                              // Write back into memory

    //removableMemories.put("find",   " bT_StuckSA_Copy bL_StuckSA_Memory bL_StuckSA_Copy bL_StuckSA_Transaction bR_StuckSA_Memory bR_StuckSA_Copy bR_StuckSA_Transaction lT_StuckSA_Copy lL_StuckSA_Memory lL_StuckSA_Copy lL_StuckSA_Transaction lR_StuckSA_Memory  lR_StuckSA_Copy lR_StuckSA_Transaction nL nR ");
    //removableMemories.put("delete", " bL_StuckSA_Copy lL_StuckSA_Copy ");
    //removableMemories.put("put",    " bL_StuckSA_Copy lL_StuckSA_Copy lR_StuckSA_Copy ");
   }

  StuckDM createBranchStuck(String name)                                        // Create a branch Stuck
   {zz();
    final StuckDM b = new StuckDM(name)                                         // Branch stucks
     {int     maxSize() {return BtreeSF.this.maxKeysPerBranch()+1;}             // Not forgetting top next
      int  bitsPerKey() {return BtreeSF.this.bitsPerKey();}
      int bitsPerData() {return BtreeSF.this.bitsPerNext;}
      int bitsPerSize() {return BtreeSF.this.bitsPerSize;}
     };
    b.M.layout.layoutName = name+"_M";
    b.T.layout.layoutName = name+"_T";
    b.program(P);
    return b;
   }

  StuckDM createLeafStuck(String name)                                          // Create a leaf Stuck
   {zz();
    final StuckDM l = new StuckDM(name)                                         // Leaf stucks
     {int     maxSize() {return BtreeSF.this.maxKeysPerLeaf();}
      int  bitsPerKey() {return BtreeSF.this.bitsPerKey();}
      int bitsPerData() {return BtreeSF.this.bitsPerData();}
      int bitsPerSize() {return BtreeSF.this.bitsPerSize;}
     };
    l.M.layout.layoutName = name+"_M";
    l.T.layout.layoutName = name+"_T";
    l.program(P);
   return l;
  }

  private static BtreeSF BtreeSF(int leafKeys, int branchKeys, int maxSize)     // Define a test btree with the specified dimensions
   {zz();
    return new BtreeSF()
     {int maxSize         () {return     maxSize;}
      int maxKeysPerLeaf  () {return    leafKeys;}
      int maxKeysPerBranch() {return  branchKeys;}
      int bitsPerKey      () {return          32;}
      int bitsPerData     () {return          32;}
     };
   }

  private static BtreeSF allTreeOps()                                           // Define a tree capable of performing all operations
   {zz();
    return new BtreeSF()
     {int maxSize         () {return 16;}
      int maxKeysPerLeaf  () {return  2;}
      int maxKeysPerBranch() {return  3;}
      int bitsPerKey      () {return  5;}
      int bitsPerData     () {return  4;}
     };
   }

  private static BtreeSF wideTree()                                             // Define a tree with nodes wide enough to test logarithmic moves and searching
   {zz();
    return new BtreeSF()
     {int maxSize         () {return 16;}
      int maxKeysPerLeaf  () {return  8;}
      int maxKeysPerBranch() {return  9;}
      int bitsPerKey      () {return 64;}
      int bitsPerData     () {return 64;}
     };
   }

  Layout nodeLayout()                                                           // Layout describing a node in btree
   {zz();
    final BtreeSF btree = this;

    final StuckDM leafStuck = new StuckDM("leaf")                               // Leaf
     {int               maxSize() {return btree.maxKeysPerLeaf();}
      int            bitsPerKey() {return btree.bitsPerKey();}
      int           bitsPerData() {return btree.bitsPerData();}
      int           bitsPerSize() {return btree.bitsPerSize;}
     };
    leafStuck.T.layout.layoutName = "leaf";

    final StuckDM branchStuck = new StuckDM("branch")                           // Branch
     {int               maxSize() {return btree.maxKeysPerBranch()+1;}          // Not forgetting top next
      int            bitsPerKey() {return btree.bitsPerKey();}
      int           bitsPerData() {return btree.bitsPerNext;}
      int           bitsPerSize() {return btree.bitsPerSize;}
     };
    branchStuck.T.layout.layoutName = "branch";

    final Layout   l = Layout.layout();
    final int      n = max(leafStuck.M.size(), branchStuck.M.size())            // Size of the data in the node
                       + 1 + btree.bitsPerNext;
    final int      p = nextPowerOfTwo(n) - n;                                   // Amount of padding - which might be zero
    final Layout.Variable pad = p > 0 ? l.variable("pad", p) : null;            // Padding might be required
    leaf         = l.duplicate("leaf",         leafStuck.layout());
    branch       = l.duplicate("branch",       branchStuck.layout());
    branchOrLeaf = l.union    ("branchOrLeaf", leaf,   branch);
    isLeaf       = l.bit      ("isLeaf");
    free         = l.variable ("free",         btree.bitsPerNext);
    node         = pad == null ?
                   l.structure("node",         isLeaf, free, branchOrLeaf):     // No padding required
                   l.structure("node",         isLeaf, free, branchOrLeaf, pad);// Padding to the next power of two to make indexing of nodes easy
    nodeLayout   = l.compile();
    node_size    = l.get("branchOrLeaf.branch.currentSize");                    // Relies on size being in the same position and having the same size in branches and leaves
    return nodeLayout;
   }

  Layout freeChainLayout()                                                      // Layout describing free chain head
   {zz();
    final Layout      l = Layout.layout();
    freeChainHead   = l.variable ("freeList", bitsPerNext);
    freeChainLayout = l.compile();
    return l;
   }

  Layout layout()                                                               // Layout describing memory used by btree
   {zz();
    final BtreeSF btree = this;
    nodeLayout();

    final Layout   l = Layout.layout();
    node         = l.duplicate(nodeLayout);
    bTreeNodes   = l.array("nodes",  node, maxSize());
    l.compile();
//  bTree_isLeaf = l.get("node.isLeaf");
    bTree_free   = l.get("node.free");
    return l;
   }

  BtreePA btreePA()                                                             // Convert to an earlier version known to work correctly so we can print it without having to write and execute pribt code.
   {zz();
    final BtreeSF s = this;                                                     // Source
    final BtreePA t = new BtreePA()                                             // Target
     {int maxSize         () {return s.maxSize         () ;}
      int maxKeysPerLeaf  () {return s.maxKeysPerLeaf  () ;}
      int maxKeysPerBranch() {return s.maxKeysPerBranch() ;}
      int bitsPerKey      () {return s.bitsPerKey      () ;}
      int bitsPerData     () {return s.bitsPerData     () ;}
     };

    t.M.memory().copy(s.F.memory());                                            // The difference between BTreeDM and SF. I was listening to the Caves of Steel at the time.
    final int N = t.maxSize(), P = t.Node.width, Q = s.nodeLayout.size();
    for (int i = 0, p = s.F.memory().size(), q = 0; i < N; i++, p += P, q += Q) // Copy all the nodes out of the source tree and place thme in the target
     {t.M.memory().copy(p, s.M.memory(), q, P);
     }

    return t;
   }

  static BtreeSF btreeDM(BtreePA p)                                             // Convert from an earlier version known to work correctly
   {zz();
    final BtreeSF t = new BtreeSF()
     {int maxSize         () {return p.maxSize         () ;}
      int maxKeysPerLeaf  () {return p.maxKeysPerLeaf  () ;}
      int maxKeysPerBranch() {return p.maxKeysPerBranch() ;}
      int bitsPerKey      () {return p.bitsPerKey      () ;}
      int bitsPerData     () {return p.bitsPerData     () ;}
     };
    t.M.memory().copy(p.M.memory());
    return t;
   }

//D1 Control                                                                    // Testing, control and integrity

  private void ok(String expected) {zz(); Test.ok(toString(), expected);}       // Confirm tree is as expected
  private void stop()              {zz(); Test.stop(toString());}               // Stop after printing the tree
  public String toString()         {zz(); return ""+btreePA();}                 // Print the tree

//D1 Memory access                                                              // Access to memory

  private void checkMainField(Layout.Field field)                               // Check that a variable is in main memory
   {zz();
    if (field.container() != M.layout)
     {final String name = field.container().layoutName;
      stop("Field:", field.name, "is part of memory layout:", name, "not main");
     }
   }

  private void checkTransactionField(Layout.Field field)                        // Check that a variable is in transaction memory
   {zz();
    if (field.container() != T.layout)
     {final String name = field.container().layoutName;
      stop("Field:", field.name, "is part of memory layout:",
                           name, "not transaction");
     }
   }

  private void setInt(Layout.Field field, int value)                            // Set an integer in main memory
   {zz(); checkMainField(field); M.setInt(field, value);
   }
  private void setInt(Layout.Field field, int value, int index)
   {zz(); checkMainField(field); M.setInt(field, value, index);
   }

  void tt(Layout.Variable target, Layout.Variable source)                       // Copy the value of one transaction variable into another
   {zz(); checkTransactionField(target); checkTransactionField(source);
    T.at(target).move(T.at(source));
   }

  void tm(Layout.Variable target, Layout.Variable source)                       // Copy the value of a main memory variable into transaction memory
   {zz(); checkTransactionField(target); checkMainField(source);
    T.at(target).move(M.at(source));
   }

//D1 Memory allocation                                                          // Allocate and free memory

  private void allocate(boolean check)                                          // Allocate a node with or without checking for sufficient free space
   {zz();
    T.at(allocate).move(F.at(freeChainHead));                                   // Node at head of free nodes list
    if (check)
     {P.new If (T.at(allocate))
       {void Else()
         {P.new I()
           {void   a() {stop("No more memory available");}                      // No more free nodes available
            String v() {return "/* No more memory available */";}
           };
         }
       };
     }
    nC.loadNode(T.at(allocate));                                                // Load allocated node
    F.at(freeChainHead).move(nC.N.at(free));                                    // Second node on free list

    nC.zero();                                                                  // Clear the node
    nC.saveNode(T.at(allocate));                                                // Construct and clear the node
//    maxNodeUsed  = max(maxNodeUsed, ++nodeUsed);                              // Number of nodes in use
   }

  private void allocate() {z(); allocate(true);}                                // Allocate a node checking for free space

//D1 Components                                                                 // A branch or leaf in the tree

  private Layout.Variable               Key;                                    // Key being found, inserted or deleted
  private Layout.Variable              Data;                                    // Data associated with the key being inserted
  private Layout.Bit                  found;                                    // Whether the key was found
  private Layout.Variable               key;                                    // Key to insert
  private Layout.Variable              data;                                    // Data associated with the key found

  private Layout.Variable          allocate;                                    // The latest allocation result
  private Layout.Variable          nextFree;                                    // Next element of the free chain

  private Layout.Bit                success;                                    // Inserted or updated if true
  private Layout.Bit               inserted;                                    // Inserted if true

  private Layout.Variable             first;                                    // Index of first key greater than or equal to the search key
  private Layout.Variable              next;                                    // The corresponding next field or top if no such key was found

  private Layout.Variable            search;                                    // Search key

  private Layout.Variable          firstKey;                                    // First of right leaf
  private Layout.Variable           lastKey;                                    // Last of left leaf
  private Layout.Variable             flKey;                                    // Key mid way between last of left and first of right
  private Layout.Variable         parentKey;                                    // Parent key

  private Layout.Variable                lk;                                    // Left  child key
  private Layout.Variable                ld;                                    // Left  child data
  private Layout.Variable                rk;                                    // Right child key
  private Layout.Variable                rd;                                    // Right child data
  private Layout.Variable             index;                                    // Index of a slot in a node

  private Layout.Variable                nl;                                    // Number in the left child
  private Layout.Variable                nr;                                    // Number in the right child

  private Layout.Variable                 l;                                    // Left node
  private Layout.Variable                 r;                                    // Right node

  private Layout.Variable       splitParent;                                    // The parent during a splitting operation
  private Layout.Bit                 IsLeaf;                                    // On a leaf
  private Layout.Bit                 isFull;                                    // The node is full
  private Layout.Bit             leafIsFull;                                    // The leaf node is full
  private Layout.Bit           branchIsFull;                                    // The branch node is full
  private Layout.Bit           parentIsFull;                                    // The parent branch node is full
  private Layout.Bit                isEmpty;                                    // The node is empty
  private Layout.Bit                  isLow;                                    // The node has too few children for a delete
  private Layout.Bit   hasLeavesForChildren;                                    // The node has leaves for children
  private Layout.Bit         stolenOrMerged;                                    // A merge or steal operation succeeded
  private Layout.Bit           pastMaxDepth;                                    // A merge or steal operation succeeded
  private Layout.Bit             nodeMerged;                                    // All sequential pairs of siblings have been offered a chance to merge
  private Layout.Bit              mergeable;                                    // The left and right children are mergable
  private Layout.Bit                deleted;                                    // Whether the delete request actually deleted the specified key

  private Layout.Variable        branchBase;                                    // The offset of a branch in memory
  private Layout.Variable          leafSize;                                    // Number of children in body of leaf
  private Layout.Variable        branchSize;                                    // Number of children in body of branch taking top for granted as it is always there
  private Layout.Variable         childSize;                                    // Number of children in body of a child node that might be a branch or a leaf - fortuately the size field of a stuck is in the same location reagrdless of whether it represents a leaf or a branch
  private Layout.Variable               top;                                    // The top next element of a branch - only used in printing
                                                                                // Find, insert, delete - the public entry points to this module
  private Layout.Variable              find;                                    // Results of a find operation
  private Layout.Variable     findAndInsert;                                    // Results of a find and insert operation
  private Layout.Variable            parent;                                    // Parent node in a descent through the tree
  private Layout.Variable             child;                                    // Child node in a descent through the tree
  private Layout.Variable         leafFound;                                    // Leaf found by find
  private Layout.Variable    maxKeysPerLeaf;                                    // Maximum keys per leaf
  private Layout.Variable  maxKeysPerBranch;                                    // Maximum keys per branch
//private Layout.Variable          MaxDepth;                                    // Maximum depth of a search
  private Layout.Variable               two;                                    // The value two
//private Layout.Variable         findDepth;                                    // Current level being searched by find
//private Layout.Variable          putDepth;                                    // Current level being traversed by put
//private Layout.Variable       deleteDepth;                                    // Current level being traversed by delete
//private Layout.Variable        mergeDepth;                                    // Current level being traversed by merge
  private Layout.Variable        mergeIndex;                                    // Current index of node being merged across
  private Layout.Variable   memoryIOAddress;                                    // Pipelined input or outoput to memory
  private Layout.Bit      memoryIODirection;                                    // If true we are writing into memory else we are reading from memory

  private Layout.Variable  node_isLeaf;                                         // The node to be used to implicitly parameterize each method call
  private Layout.Variable  node_setLeaf;
  private Layout.Variable  node_setBranch;
  private Layout.Variable  node_assertLeaf;
  private Layout.Variable  node_assertBranch;
  private Layout.Variable  allocLeaf;
  private Layout.Variable  allocBranch;
  private Layout.Variable  node_free;
  private Layout.Variable  node_clear;
  private Layout.Variable  node_erase;
  private Layout.Variable  node_leafBase;
  private Layout.Variable  node_branchBase;
  private Layout.Variable  node_leafSize;
  private Layout.Variable  node_branchSize;
  private Layout.Variable  node_isFull;
  private Layout.Variable  node_leafIsFull;
  private Layout.Variable  node_branchIsFull;
  private Layout.Variable  node_parentIsFull;
  private Layout.Variable  node_isEmpty;
  private Layout.Variable  node_isLow;
  private Layout.Variable  node_hasLeavesForChildren;
  private Layout.Variable  node_top;
  private Layout.Variable  node_findEqualInLeaf;
  private Layout.Variable  node_findFirstGreaterThanOrEqualInLeaf;
  private Layout.Variable  node_findFirstGreaterThanOrEqualInBranch;
  private Layout.Variable  node_splitLeaf;
  private Layout.Variable  node_splitBranch;
  private Layout.Variable  node_stealFromLeft;
  private Layout.Variable  node_stealFromRight;
  private Layout.Variable  node_mergeRoot;
  private Layout.Variable  node_mergeLeftSibling;
  private Layout.Variable  node_mergeRightSibling;
  private Layout.Variable  node_balance;

  private Layout transactionLayout()                                            // Layout of temporary storage used during a transaction against the btree
   {zz();
    final Layout L = new Layout();
                                    allocate = L.variable ("allocate"                                      , bitsPerNext);
                                    nextFree = L.variable ("nextFree"                                      , bitsPerNext);
                                     success = L.bit      ("success"                                       );
                                    inserted = L.bit      ("inserted"                                      );

                                       first = L.variable ("first"                                         , bitsPerSize);
                                        next = L.variable ("next"                                          , bitsPerNext);

                                      search = L.variable ("search"                                        , bitsPerKey());
                                       found = L.bit      ("found"                                         );
                                         key = L.variable ("key"                                           , bitsPerKey());
                                        data = L.variable ("data"                                          , bitsPerData());

                                    firstKey = L.variable ("firstKey"                                      , bitsPerKey());
                                     lastKey = L.variable ("lastKey"                                       , bitsPerKey());
                                       flKey = L.variable ("flKey"                                         , bitsPerKey());
                                   parentKey = L.variable ("parentKey"                                     , bitsPerKey());

                                          lk = L.variable ("lk"                                            , bitsPerKey());
                                          ld = L.variable ("ld"                                            , bitsPerData());
                                          rk = L.variable ("rk"                                            , bitsPerKey());
                                          rd = L.variable ("rd"                                            , bitsPerData());
                                       index = L.variable ("index"                                         , bitsPerSize);

                                          nl = L.variable ("nl"                                            , bitsPerSize);
                                          nr = L.variable ("nr"                                            , bitsPerSize);

                                           l = L.variable ("l"                                             , bitsPerNext);
                                           r = L.variable ("r"                                             , bitsPerNext);

                                 splitParent = L.variable ("splitParent"                                   , bitsPerNext);

                                      IsLeaf = //L.bit      ("IsLeaf"                                        );
                                      isFull = //L.bit      ("isFull"                                        );
                                  leafIsFull = //L.bit      ("leafIsFull"                                    );
                                branchIsFull = //L.bit      ("branchIsFull"                                  );
                                parentIsFull = //L.bit      ("parentIsFull"                                  );
                                     isEmpty = //L.bit      ("isEmpty"                                       );
                                       isLow = //L.bit      ("isLow"                                         );
                        hasLeavesForChildren = //L.bit      ("hasLeavesForChildren"                          );
                              stolenOrMerged = //L.bit      ("stolenOrMerged"                                );
                                pastMaxDepth = //L.bit      ("pastMaxDepth"                                  );
                                  nodeMerged = //L.bit      ("nodeMerged"                                    );
                                   mergeable = L.bit      ("mergeable"                                     );
                                     deleted = L.bit      ("deleted"                                       );

                                  branchBase = L.variable ("branchBase"                                    , bitsPerAddress);
                                    leafSize = L.variable ("leafSize"                                      , bitsPerSize);
                      childSize = branchSize = L.variable ("branchSize"                                    , bitsPerSize);
                                         top = L.variable ("top"                                           , bitsPerNext);

                                         Key = L.variable ("Key"                                           , bitsPerKey());
                                        Data = L.variable ("Data"                                          , bitsPerData());
                                        find = L.variable ("find"                                          , bitsPerNext);
                               findAndInsert = L.variable ("findAndInsert"                                 , bitsPerNext);
                                      parent = L.variable ("parent"                                        , bitsPerNext);
                                       child = L.variable ("child"                                         , bitsPerNext);
                                   leafFound = L.variable ("leafFound"                                     , bitsPerNext);

                              maxKeysPerLeaf = L.variable ("maxKeysPerLeaf"                                , bitsPerSize);
                            maxKeysPerBranch = L.variable ("maxKeysPerBranch"                              , bitsPerSize);
                                         two = L.variable ("two"                                           , bitsPerSize);
//                                  MaxDepth = //L.variable ("maxDepth"                                      , bitsPerNext);
//                                 findDepth = //L.variable ("findDepth"                                     , bitsPerNext);
//                                  putDepth = //L.variable ("putDepth"                                      , bitsPerNext);
//                               deleteDepth = //L.variable ("deleteDepth"                                   , bitsPerNext);
//                                mergeDepth = L.variable ("mergeDepth"                                    , bitsPerNext);
                                  mergeIndex = L.variable ("mergeIndex"                                    , bitsPerSize);
                             memoryIOAddress = L.variable ("memoryIOAddress"                               , bitsPerNext);
                           memoryIODirection = L.bit      ("memoryIODirection");

                                 node_isLeaf = //L.variable ("node_isLeaf"                                   , bitsPerNext);
                                node_setLeaf = //L.variable ("node_setLeaf"                                  , bitsPerNext);
                              node_setBranch = L.variable ("node_setBranch"                                , bitsPerNext);
                             node_assertLeaf = //L.variable ("node_assertLeaf"                               , bitsPerNext);
                           node_assertBranch = L.variable ("node_assertBranch"                             , bitsPerNext);
                                   allocLeaf = //L.variable ("allocLeaf"                                     , bitsPerNext);
                                 allocBranch = L.variable ("allocBranch"                                   , bitsPerNext);
                                   node_free = //L.variable ("node_free"                                     , bitsPerNext);
                                  node_clear = //L.variable ("node_clear"                                    , bitsPerNext);
                                  node_erase = L.variable ("node_erase"                                    , bitsPerNext);
                               node_leafBase = //L.variable ("node_leafBase"                                 , bitsPerNext);
                             node_branchBase = //L.variable ("node_branchBase"                               , bitsPerNext);
                               node_leafSize = //L.variable ("node_leafSize"                                 , bitsPerNext);
                             node_branchSize = //L.variable ("node_branchSize"                               , bitsPerNext);
                                 node_isFull = //L.variable ("node_isFull"                                   , bitsPerNext);
                             node_leafIsFull = //L.variable ("node_leafIsFull"                               , bitsPerNext);
                           node_branchIsFull = //L.variable ("node_branchIsFull"                             , bitsPerNext);
                           node_parentIsFull = //L.variable ("node_parentIsFull"                             , bitsPerNext);
                                node_isEmpty = //L.variable ("node_isEmpty"                                  , bitsPerNext);
                                  node_isLow = L.variable ("node_isLow"                                    , bitsPerNext);
                   node_hasLeavesForChildren = //L.variable ("node_hasLeavesForChildren"                     , bitsPerNext);
                                    node_top = //L.variable ("node_top"                                      , bitsPerNext);
                        node_findEqualInLeaf = //L.variable ("node_findEqualInLeaf"                          , bitsPerNext);
      node_findFirstGreaterThanOrEqualInLeaf = //L.variable ("node_findFirstGreaterThanOrEqualInLeaf"        , bitsPerNext);
    node_findFirstGreaterThanOrEqualInBranch = //L.variable ("node_findFirstGreaterThanOrEqualInBranch"      , bitsPerNext);
                              node_splitLeaf = //L.variable ("node_splitLeaf"                                , bitsPerNext);
                            node_splitBranch = //L.variable ("node_splitBranch"                              , bitsPerNext);
                          node_stealFromLeft = //L.variable ("node_stealFromLeft"                            , bitsPerNext);
                         node_stealFromRight = //L.variable ("node_stealFromRight"                           , bitsPerNext);
                              node_mergeRoot = //L.variable ("node_mergeRoot"                                , bitsPerNext);
                       node_mergeLeftSibling = //L.variable ("node_mergeLeftSibling"                         , bitsPerNext);
                      node_mergeRightSibling = //L.variable ("node_mergeRightSibling"                        , bitsPerNext);
                                node_balance = L.variable ("node_balance"                                  , bitsPerNext);

    final Layout.Structure transaction = L.structure("transaction",
      allocate,
      nextFree,
      success,
      inserted,
      first,
      next,
      search,
      found,
      key,
      data,
      firstKey,
      lastKey,
      flKey,
      parentKey,
      lk,
      ld,
      rk,
      rd,
      index,
      nl,
      nr,
      l,
      r,

      splitParent,

      //IsLeaf,
      //isFull,
      //leafIsFull,
      //branchIsFull,
      //parentIsFull,
      //isEmpty,
      //isLow,
      //hasLeavesForChildren,
      //stolenOrMerged,
      //pastMaxDepth,
      //nodeMerged,
      mergeable,
      deleted,

      branchBase,
      leafSize,
      //childSize
      branchSize,
      top,
      Key,
      Data,
      find,
      findAndInsert,
      parent,
      child,
      leafFound,
      maxKeysPerLeaf,
      maxKeysPerBranch,
      two,
      //MaxDepth,
      //findDepth,
      //putDepth,
      //deleteDepth,
      //mergeDepth,
        mergeIndex,
        memoryIOAddress,
        memoryIODirection,
      //node_isLeaf,
      //node_setLeaf,
      node_setBranch,
      //node_assertLeaf,
      node_assertBranch,
      //allocLeaf,
      allocBranch,
      //node_free,
      //node_clear,
      node_erase,
      //node_leafBase,
      //node_branchBase,
      //node_leafSize,
      //node_branchSize,
      //node_isFull,
      //node_leafIsFull,
      //node_branchIsFull,
      //node_parentIsFull,
      //node_isEmpty,
      node_isLow,
      //node_hasLeavesForChildren,
      //node_top,
      //node_findEqualInLeaf,
      //node_findFirstGreaterThanOrEqualInLeaf,
      //node_findFirstGreaterThanOrEqualInBranch,
      //node_splitLeaf,
      //node_splitBranch,
      //node_stealFromLeft,
      //node_stealFromRight,
      //node_mergeRoot,
      //node_mergeLeftSibling,
      //node_mergeRightSibling,
      node_balance);
    return L.compile();
   }

  private MemoryLayoutDM.At ifRootLeaf(Node n)                                  // A variable that indicates whether the root is a leaf
   {zz(); return n.N.at(isLeaf);
   }

  private MemoryLayoutDM.At ifLeaf(Node Node)                                   // A variable that indicates whether the node is a leaf
   {zz(); return Node.N.at(isLeaf);
   }

  private void allocLeaf()                                                      // Allocate leaf
   {zz();
    allocate();
    tt(allocLeaf,     allocate);
    tt(node_setLeaf,  allocate);
    nC.loadNode(T.at(allocate));                                                // Load the allocated node
    nC.setLeaf();                                                               // Set as a leaf
    nC.saveNode(T.at(allocate));                                                // Write back into memory
   }

  private void allocBranch()                                                    // Allocate branch
   {zz();
    allocate();
    tt(allocBranch   , allocate);
    tt(node_setBranch, allocate);
    nC.loadNode(T.at(allocate));                                                // Load the allocated node
    nC.setBranch();                                                             // Set as a branch
    nC.saveNode(T.at(allocate));                                                // Write back into memory
   }

  private void free(Layout.Variable node_free)                                  // Free a node to make it available for reuse
   {zz();
    nC.ones();                                                                  // Clear the node
    nC.N.at(free).move(F.at(freeChainHead));                                    // Chain this node in front of the last freed node
    nC.saveNode(T.at(node_free));                                               // Save node

    F.at(freeChainHead).move(T.at(node_free));                                  // Make this node the head of the free chain
   }

  private void hasLeavesForChildren(StuckDM bLeaf)                              // The node has leaves for children
   {zz();
    bLeaf.firstElement();                                                       // Was lastElement but firstElement() is faster
    nC.loadNode(bLeaf.T.at(bLeaf.tData));
    nC.isLeaf(T.at(hasLeavesForChildren));
   }

//D2 Node                                                                       // Description of a node

  class Node                                                                    // Node in a btree. Assumes that both leaves and branches occupy the same amount of memory and compute their size in the same way avoiding the need to differentiate between the two.
   {final String      name;
    final MemoryLayoutDM N;

    Node(String Name)                                                           // Create a node.
     {zz(); name = Name;
      N = new MemoryLayoutDM(nodeLayout, name);
      N.program(P);
     }

    void zero()                                                                 // Clear the memory associated with this node to zeroes
     {zz();
      P.new I()
       {void   a() {N.zero();}
        String v() {return N.name + " <= 0; /* MemoryLayoutDM.zero */";}
       };
     }

    void ones()                                                                 // Set the memory associated with this node to ones
     {zz();
      P.new I()
       {void   a() {N.ones();}
        String v() {return N.name + " <= -1; /* MemoryLayoutDM.ones */";}
       };
     }

    void loadRoot() {zz(); loadNode(null);}                                     // Read the root from memory into this node

    void loadNode(MemoryLayoutDM.At at)                                         // Load this node from addressed memory
     {zz();
      P.parallelStart();
        if (at != null)    T.at(memoryIOAddress).move(at);                      // Index of node in memory
        else               T.at(memoryIOAddress).zero();
      P.parallelSection(); T.at(memoryIODirection).zero();                      // Read
      P.parallelEnd();

      P.nop();                                                                  // Let memory catch up

      P.new I()
       {void a()
         {N.top().moveBits(M.at(node, T.at(memoryIOAddress)));
         }
       String v()
         {return N.name()+" <= memoryOut; /* loadNode */\n";
         }
       };
     }

    void saveRoot() {zz(); saveNode(null);}                                     // Write the root into memory from this node

    void saveNode(MemoryLayoutDM.At at)                                         // Save the node indexed by this variable into memory
     {zz();
      P.parallelStart();
        if (at != null) T.at(memoryIOAddress).move(at);                         // Index of node in memory
        else            T.at(memoryIOAddress).zero();
      P.parallelSection(); T.at(memoryIODirection).ones();                      // Write
      P.parallelSection(); memoryIn.N.top().move(N.top());                      // Transfer from memory interface buffer into node
      P.parallelEnd();

      P.new I()
       {void a()
         {M.at(node, T.at(memoryIOAddress)).moveBits(memoryIn.N.top());
         }
       String v()
         {return "/* loadNode */";
         }
       };
      T.at(memoryIODirection).zero();                                           // Reset the write flag to prevent further writes from occurring
     }

    void loadRootStuck(StuckDM Stuck)                                           // Load a root stuck from main memory
     {zz();
      loadRoot();
      loadStuck(Stuck);
     }

    void loadStuck(StuckDM Stuck)                                               // Load a stuck from a node
     {zz(); Stuck.M.copy(N.at(branchOrLeaf));
     }

    void loadStuck(StuckDM Stuck, Layout.Variable at)                           // Load a stuck from indexed main memory via this node
     {zz();
      loadNode(T.at(at));
      loadStuck(Stuck);
     }

    void loadLeafStuckAndSize                                                   // Load a stuck from indexed main memory and store its size in a temporary variable,
     (StuckDM Stuck, Layout.Variable at, Layout.Variable field)
     {zz();
      loadStuck(Stuck, at);                                                     // Load stuck from node memory
      T.at(field).move(Stuck.M.at(Stuck.currentSize));                          // Save size
     }

    void loadBranchStuckAndSize                                                 // Load a stuck from indexed main memory and store its size in a temporary variable,
     (StuckDM Stuck, Layout.Variable at, Layout.Variable field)
     {zz();
      loadStuck(Stuck, at);
      T.at(field).add(Stuck.M.at(Stuck.currentSize), -1);                       // Account for top
     }

    void saveRootStuck(StuckDM Stuck)                                           // Save root stuck into main memory
     {zz();
      saveStuck(Stuck);
      saveRoot();
     }

    void saveStuck(StuckDM Stuck)                                               // Save a stuck from a node
     {zz(); N.at(branchOrLeaf).copy(Stuck.M);
     }

    void saveStuck(StuckDM Stuck, Layout.Variable at)                           // Save a stuck into indexed main memory
     {zz();
      saveStuck(Stuck);
      saveNode(T.at(at));
     }

    void setLeaf()                                                              // Mark a node as a leaf
     {zz(); N.at(isLeaf).ones();
     }

    void setBranch()                                                            // Mark a node as a bramch
     {zz(); N.at(isLeaf).zero();
     }

    void isLeaf(MemoryLayoutDM.At at)                                           // Check for a leaf
     {zz(); at.copy(N.at(isLeaf));
     }

    void branchSize(MemoryLayoutDM.At at)                                       // Get size of branch stuck
     {zz();
      at.copy(N.at(node_size));                                                 // Relies on size being in the same position and having the same size in both branches and leaves
      at.dec();                                                                 // Account for top
     }

    void isFull()                                                               // Set isFull to show whether the node is full or not, incidentlly setting IsLeaf as well
     {zz();
      isLeaf(T.at(IsLeaf));
      P.new If (T.at(IsLeaf))
       {void Then()
         {z();
          N.at(node_size).equal(T.at(maxKeysPerLeaf), T.at(isFull));
         }
        void Else()
         {z();
          N.at(node_size).greaterThan(T.at(maxKeysPerBranch), T.at(isFull));    // The presence of top adds one more to the size so greater than is required rather than equals
         }
       };
     }

    void isLeafFull()                                                           // Set isFull to show whether the node known to be a leaf is full or not
     {zz();
      N.at(node_size).equal(T.at(maxKeysPerLeaf), T.at(isFull));
     }

    public String toString() {return ""+N;}                                     // As string

    String print(int at)                                                        // Print the indexed node
     {N.memory().copy(M.memory(), M.at(node, at).at);
      return ""+N;
     }
   }

//D2 Search                                                                     // Search within a node and update the node description with the results

  private void findEqualInLeaf(MemoryLayoutDM.At Key, Node Node)                // Find the first key in the node that is equal to the search key
   {zz();
    Node.loadStuck(lEqual);
    lEqual.search(Key, T.at(found), T.at(index), T.at(data));
   }

  private void findFirstGreaterThanOrEqualInLeaf                                // Find the first key in the  leaf that is equal to or greater than the search key
   (StuckDM Leaf, MemoryLayoutDM.At Search,
    MemoryLayoutDM.At Found, MemoryLayoutDM.At Index)
   {zz();
    Leaf.searchFirstGreaterThanOrEqual(true, Search, Found, Index, null, null);
   }

  private void findFirstGreaterThanOrEqualInBranch                              // Find the first key in the branch that is equal to or greater than the search key
   (Node Node,  MemoryLayoutDM.At Search, MemoryLayoutDM.At Found,
    MemoryLayoutDM.At Index, MemoryLayoutDM.At Data)
   {zz();
    Node.loadStuck(bFirstBranch);
    bFirstBranch.searchFirstGreaterThanOrEqual
     (false, Search, Found, Index, null, Data);
   }

//D2 Split                                                                      // Split nodes in half to increase the number of nodes in the tree

  private void splitLeafRoot()                                                  // Split a leaf which happens to be a full root into two half full leaves while transforming the root leaf into a branch
   {zz();
    //final  Variable L = new Variable(P, "left",  bitsPerNext);                  // Index of left allocated node
    //final  Variable R = new Variable(P, "right", bitsPerNext);                  // Index of right allocated node

    allocLeaf(); tt(l, allocLeaf);                                              // New left leaf
    allocLeaf(); tt(r, allocLeaf);                                              // New right leaf

    nT.loadRootStuck(lT);                                                       // Load root
    nL.loadStuck(lL, l);                                                        // Clear left stuck
    nR.loadStuck(lR, r);                                                        // Clear right stuck

    lT.split(lL, lR);                                                           // Split root leaf into child leaves

    P.parallelStart();   lR.firstElement();                                     // First of right
    P.parallelSection(); lL. lastElement();                                     // Lat of left
    P.parallelSection(); nT.setBranch();                                        // The root is a branch
    P.parallelSection(); bT.clear();                                            // Clear the root
    P.parallelEnd();

    P.parallelStart();    T.at(firstKey).move(lR.T.at(lR.tKey));                // First of right leaf
    P.parallelSection();  T.at(lastKey ).move(lL.T.at(lL.tKey));                // Last of left leaf
    P.parallelEnd();

    P.new I()                                                                   // Mid key - keys are likely to be bigger than 31 bits
     {void a()
       {T.at(flKey).setInt((T.at(firstKey).getInt()+T.at(lastKey).getInt())/2);
       }
      String v()
       {return T.at(flKey)   .verilogLoad() + "<= " +
           "("+T.at(firstKey).verilogLoad() + " + " +
               T.at(lastKey) .verilogLoad() + ") / 2; /* MidKey1 */";
       }
     };

    P.parallelStart();    bT.T.at(bT.tKey ).move(T.at(flKey));
    P.parallelSection();  bT.T.at(bT.tData).move(T.at(l));
    P.parallelEnd();
    bT.push();                                                                  // Insert left leaf into root

    P.parallelStart();    bT.T.at(bT.tKey).zero();
    P.parallelSection();  bT.T.at(bT.tData).move(T.at(r));
    P.parallelEnd();
    bT.push();                                                                  // Insert right into root. This will be the top node and so ignored by search ... except last.

    nT.saveRootStuck(bT);
    nL.saveStuck(lL, l);
    nR.saveStuck(lR, r);
   }

  private void splitBranchRoot()                                                // Split a branch which happens to be a full root into two half full branches while retaining the current branch as the root
   {zz();
    allocBranch(); tt(l, allocBranch);                                          // New left branch
    allocBranch(); tt(r, allocBranch);                                          // New right branch

    nT.loadRootStuck(bT);                                                       // Load root
    nL.loadStuck(bL, l);                                                        // Clear left stuck
    nR.loadStuck(bR, r);                                                        // Clear right stuck

    bT.split(bL, bR);                                                           // Split the root as a branch

    P.parallelStart();   bL.T.setIntInstruction(bL.tKey, 0);                    // Left top components
    P.parallelSection(); bT.T.setIntInstruction(bT.index, splitBranchSize);
    P.parallelEnd();
    bT.elementAt();

    P.parallelStart();   T.at(parentKey)  .move(bT.T.at(bT.tKey));              // Set left top
    P.parallelSection(); bL.T.at(bL.tData).move(bT.T.at(bT.tData));
    P.parallelSection(); bL.T.setIntInstruction(bL.index, splitBranchSize);
    P.parallelEnd();
    bL.setElementAt();
                                                                                // Right top components
    bR.T.setIntInstruction(bR.tKey, 0);
    bT.lastElement();

    P.parallelStart();   bR.T.at(bR.tData).move(bT.T.at(bT.tData));             // Set right top
    P.parallelSection(); bR.T.setIntInstruction(bR.index, splitBranchSize);
    P.parallelEnd();
    bR.setElementAt();

    P.parallelStart();   bT.clear();                                            // Refer to new branches from root
    P.parallelSection(); bT.T.at(bT.tKey) .move(T.at(parentKey));
    P.parallelSection(); bT.T.at(bT.tData).move(T.at(l));
    P.parallelEnd();
    bT.push();

    P.parallelStart();   bT.T.at(bT.tKey ).zero();
    P.parallelSection(); bT.T.at(bT.tData).move(T.at(r));
    P.parallelEnd();
    bT.push();                                                                  // Becomes top and so ignored by search ... except last

    nT.saveRootStuck(bT);
    nL.saveStuck(bL, l);
    nR.saveStuck(bR, r);
   }

  private void splitLeaf()                                                      // Split a leaf which is not the root assuming the nT/bT ahave the full details of the parent and that the node to be split is indexed by node_splitLeaf
   {zz();
    final StuckDM lL = createLeafStuck("splitLeaf_lL");                         // Process a left node
    final StuckDM lR = createLeafStuck("splitLeaf_lR");                         // Process a right node

    allocLeaf(); tt(l, allocLeaf);                                              // New split out leaf

    nL.loadStuck(lL, l);                                                        // Clear the left stuck
    nR.loadStuck(lR, node_splitLeaf);                                           // Load stuck on right to be split

    lR.splitLow(lL);                                                            // Split out the lower half

    P.parallelStart();   lR.firstElement();
    P.parallelSection(); lL. lastElement();
    P.parallelEnd();

    P.parallelStart();
      P.new I()                                                                 // Splitting key
       {void a()
         {bT.T.at(bT.tKey).setInt((lR.T.at(lR.tKey).getInt() +
                                   lL.T.at(lL.tKey).getInt()) / 2);
         }
        String v()
         {return bT.T.at(bT.tKey).verilogLoad() + " <= "+
             "("+lR.T.at(lR.tKey).verilogLoad() + " + " +
                 lL.T.at(lL.tKey).verilogLoad() + ") / 2; /* Midkey2 */";
         }
       };
    P.parallelSection(); bT.T.at(bT.tData).move(T.at(l));                       // Insert splitting key into parent
    P.parallelSection(); bT.T.at(bT.index).move(T.at(index));
    P.parallelEnd();
    bT.insertElementAt();                                                       // Insert new key, next pair into parent

    nT.saveStuck(bT,  splitParent);
    nL.saveStuck(lL, l);
    nR.saveStuck(lR, node_splitLeaf);
   }

  private void splitBranch()                                                    // Split a branch which is not the root  assuming the nT/bT ahave the full details of the parent and that the node to be split is indexed by node_splitBranch
   {zz();
    final StuckDM bL = createBranchStuck("splitBranch_bL");                     // Process a left node
    final StuckDM bR = createBranchStuck("splitBranch_bR");                     // Process a right node

    allocBranch(); tt(l, allocBranch);

    nL.loadStuck(bL, l);                                                        // Clear left stuck
    nR.loadStuck(bR, node_splitBranch);                                         // Load right stuck to be split

    bR.splitLow(bL);                                                            // Split right
    bL.zeroLastKey();

    P.parallelStart();   bT.T.at(bT.tKey ).move(bL.T.at(bL.tKey));              // Insert splitting key into parent
    P.parallelSection(); bT.T.at(bT.tData).move(T.at(l));
    P.parallelSection(); bT.T.at(bT.index).move(T.at(index));
    P.parallelEnd();
    bT.insertElementAt();

    nT.saveStuck(bT, parent);
    nL.saveStuck(bL, l);
    nR.saveStuck(bR, node_splitBranch);
   }

  private void stealNotPossible(ProgramDM.Label end)                            // Cannot perform the requested steal
   {zz();
    P.new If (T.at(stolenOrMerged))
     {void Then()
       {T.at(stolenOrMerged).zero();
        P.Goto(end);
       }
     };
   }

  private void stealFromLeft()                                                  // Steal from the left sibling of the indicated child if possible to give to the right - Dennis Moore, Dennis Moore, Dennis Moore.
   {zz();                                                                       // Assume that index has the node wanting to steal from the left and that the parent branch is fully loaded in to nT/bT while being indexed by node_stealFromLeft
    P.new Block()
     {void code()
       {P.new If (T.at(index))                                                  // Nothing on the left to steal from
         {void Else()
           {T.at(stolenOrMerged).zero();
            P.Goto(end);
           }
         };

        bT.T.at(bT.index).add(T.at(index), -1);                                 // Node to the left of the indexed node
        bT.elementAt();

        P.parallelStart();    T.at(l).move(bT.T.at(bT.tData));
        P.parallelSection();  bT.T.at(bT.index).move(T.at(index));
        P.parallelEnd();
        bT.elementAt();

        T.at(r).move(bT.T.at(bT.tData));                                        // Index of right node
        hasLeavesForChildren(bT);

        P.new If (T.at(hasLeavesForChildren))                                   // Children are leaves
         {void Then()
           {nL.loadLeafStuckAndSize(lL, l, nl);                                 // Address leaves on each side and get their size
            nR.loadLeafStuckAndSize(lR, r, nr);

            T.at(nr).greaterThanOrEqual(T.at(maxKeysPerLeaf),
                                        T.at(stolenOrMerged));
            stealNotPossible(end);

            T.at(nl).lessThan(T.at(two), T.at(stolenOrMerged));
            stealNotPossible(end);

            lL.pop();                                                           // Steal from left

            P.parallelStart();    lR.T.at(lR.tKey ).move(lL.T.at(lL.tKey ));
            P.parallelSection();  lR.T.at(lR.tData).move(lL.T.at(lL.tData));
            P.parallelEnd();
            lR.unshift();                                                       // Increase right

            lL.T.at(lL.index).add(T.at(nl), -2);                                // Account for top and zero base
            lL.elementAt();                                                     // Last key on left

            P.parallelStart();    bT.T.at(bT.tKey) .move(lL.T.at(lL.tKey));
            P.parallelSection();  bT.T.at(bT.tData).move(T.at(l));
            P.parallelSection();  bT.T.at(bT.index).add(T.at(index), -1);
            P.parallelEnd();
            bT.setElementAt();                                                  // Reduce key of parent of left

            nT.saveStuck(bT, node_stealFromLeft);                               // Save parent branch and modified left and right leaves
            nL.saveStuck(lL, l);
            nR.saveStuck(lR, r);
           }
          void Else()                                                           // Children are branches
           {z();
            nL.loadBranchStuckAndSize(bL, l, nl);
            nR.loadBranchStuckAndSize(bR, r, nr);


            T.at(nr).greaterThanOrEqual(T.at(maxKeysPerBranch),
                                        T.at(stolenOrMerged));
            stealNotPossible(end);

            T.at(nl).lessThan(T.at(two), T.at(stolenOrMerged));
            stealNotPossible(end);

            bL.pop();                                                           // Increase right with left top
            bT.T.at(bT.index).move(T.at(index));
            bT.elementAt();                                                     // Top key

            P.parallelStart();   bR.T.at(bR.tKey) .move(bT.T.at(bT.tKey));
            P.parallelSection(); bR.T.at(bR.tData).move(bL.T.at(bL.tData));
            P.parallelEnd();
            bR.unshift();                                                       // Increase right with left top

            bR.firstElement();                                                  // Increase right with left top

            bT.T.at(bT.index).add(T.at(index), -1);                             // Account for top

            bT.elementAt();                                                     // Parent key

            P.parallelStart();   bR.T.at(bR.tKey).move(bT.T.at(bT.tKey));
            P.parallelSection(); bR.T.at(bR.index).zero();
            P.parallelEnd();
            bR.setElementAt();                                                  // Reduce key of parent of right

            bL.lastElement();                                                   // Last left key

            P.parallelStart();   bT.T.at(bT.tKey) .move(bL.T.at(bL.tKey));
            P.parallelSection(); bT.T.at(bT.tData).move(T.at(l));
            P.parallelSection(); bT.T.at(bT.index).add(T.at(index), -1);
            P.parallelEnd();
            bT.setElementAt();                                                  // Reduce key of parent of left

            nT.saveStuck(bT, node_stealFromLeft);                               // Save parent branch and modified left and right branches
            nL.saveStuck(bL, l);
            nR.saveStuck(bR, r);
           }
         };
        z(); T.at(stolenOrMerged).ones();
       }
     };
   }

  private void stealFromRight()                                                 // Steal from the right sibling of the indicated child if possible
   {zz();                                                                       // Assume that index has the node wanting to steal from the right and that the parent branch is fully loaded in to nT/bT whil ebeing indexed by node_stealFromRight
    P.new Block()
     {void code()
       {nT.branchSize(T.at(branchSize));
        T.at(index).equal(T.at(branchSize), T.at(stolenOrMerged));              // If we are at the last index of the parent there is nothing to the right
        stealNotPossible(end);
        z();

        bT.T.at(bT.index).move(T.at(index));                                    // Get details of indexed key, next pair in parent branch
        bT.elementAt();

        P.parallelStart();    T.at(lk).move(bT.T.at(bT.tKey));                  // Save details of indexed key, next pair and get details of next element to the right now that we know there is one
        P.parallelSection();  T.at(l) .move(bT.T.at(bT.tData));
        P.parallelSection();  bT.T.at(bT.index).add(T.at(index), +1);
        P.parallelEnd();
        bT.elementAt();

        P.parallelStart();    T.at(rk).move(bT.T.at(bT.tKey));                  // Check for leaves or branches as children
        P.parallelSection();  T.at(r) .move(bT.T.at(bT.tData));
        P.parallelEnd();

        hasLeavesForChildren(bT);

        P.new If(T.at(hasLeavesForChildren))                                    // Children are leaves
         {void Then()
           {z();
            nL.loadLeafStuckAndSize(lL, l, nl);
            nR.loadLeafStuckAndSize(lR, r, nr);

            T.at(nl).greaterThanOrEqual(T.at(maxKeysPerLeaf),
                                        T.at(stolenOrMerged));
            stealNotPossible(end);

            T.at(nr).lessThan(T.at(two), T.at(stolenOrMerged));                 // Steal not allowed because it would leave the right sibling empty
            stealNotPossible(end);

            z();
            lR.shift();                                                         // First element of right child
            P.parallelStart();   lL.T.at(lL.tKey) .move(lR.T.at(lR.tKey));
            P.parallelSection(); lL.T.at(lL.tData).move(lR.T.at(lR.tData));
            P.parallelSection(); bT.T.at(bT.tKey) .move(lR.T.at(lR.tKey));
            P.parallelSection(); bT.T.at(bT.tData).move(T.at(l));
            P.parallelSection(); bT.T.at(bT.index).move(T.at(index));
            P.parallelEnd();
            lL.push();                                                          // Increase left
            bT.setElementAt();                                                  // Swap key of parent

            nT.saveStuck(bT, node_stealFromRight);                              // Save parent branch and modified left and right leaves
            nL.saveStuck(lL, l);
            nR.saveStuck(lR, r);
           }
          void Else()                                                           // Children are branches
           {z();
            nL.loadBranchStuckAndSize(bL, l, nl);
            nR.loadBranchStuckAndSize(bR, r, nr);

            T.at(nl).greaterThanOrEqual(T.at(maxKeysPerBranch),
                                        T.at(stolenOrMerged));
            stealNotPossible(end);
            T.at(nr).lessThan(T.at(two), T.at(stolenOrMerged));
            stealNotPossible(end);

            bL.lastElement();                                                   // Last element of left child
            P.parallelStart();   bL.T.at(bL.tKey ).move(T.at(lk));
            P.parallelSection(); bL.T.at(bL.index).move(T.at(nl));
            P.parallelEnd();
            bL.setElementAt();                                                  // Left top becomes real

            bR.shift();                                                         // First element of  right child

            P.parallelStart();   bL.T.at(bL.tKey).zero();
            P.parallelSection(); bL.T.at(bL.tData).move(bR.T.at(bR.tData));
            P.parallelEnd();
            bL.push();                                                          // New top for left is ignored by search ,.. except last

            P.parallelStart();   bT.T.at(bT.tKey ).move(bR.T.at(bR.tKey));
            P.parallelSection(); bT.T.at(bT.tData).move(T.at(l));
            P.parallelSection(); bT.T.at(bT.index).move(T.at(index));
            P.parallelEnd();
            bT.setElementAt();                                                  // Swap key of parent

            nT.saveStuck(bT, node_stealFromRight);                              // Save parent branch and modified left and right branches
            nL.saveStuck(bL, l);
            nR.saveStuck(bR, r);
           }
         };
        z(); T.at(stolenOrMerged).ones();
       }
     };
   }

//D2 Merge                                                                      // Merge two nodes together and free the resulting free node

  private void mergeRoot()                                                      // Merge into the root. Assume that the root has not yet been loaded
   {zz();
    P.new Block()
     {void code()
       {final ProgramDM.Label Return = end;
        nT.loadRoot();
        nT.isLeaf(T.at(IsLeaf));
        P.new If (T.at(IsLeaf))                                                 // Confirm we are on a branch
         {void Then()
           {T.at(stolenOrMerged).zero();
            P.Goto(Return);
           }
         };
        nT.loadRootStuck(bT);                                                   // Lood root and get its size
        bT.M.at(bT.currentSize).greaterThan(T.at(two), T.at(stolenOrMerged));   // Confirm we are on an almost empty root
        stealNotPossible(end);

        z();
        bT.firstElement(); T.at(l).move(bT.T.at(bT.tData));                     // Two elements in root
        bT. lastElement(); T.at(r).move(bT.T.at(bT.tData));

        hasLeavesForChildren(bT);
        P.new If (T.at(hasLeavesForChildren))                                   // Leaves
         {void Then()
           {z();
            nL.loadLeafStuckAndSize(lL, l, nl);
            nR.loadLeafStuckAndSize(lR, r, nr);

            P.new I()                                                           // Check that combined node would not be too big
             {void a()
               {T.at(mergeable).setInt
                 ((T.at(nl).getInt() + T.at(nr).getInt() <= maxKeysPerLeaf()) ?
                  1 : 0);
               }
              String v()
               {return T.at(mergeable).verilogLoad() + " <= " +
                          "("+T.at(nl).verilogLoad() + " + "  +
                              T.at(nr).verilogLoad() + " <= " +
                   maxKeysPerLeaf()+") ? 'b1 : 'b0; /* mergeRoot1 */";
               }
             };

            P.new If (T.at(mergeable))                                          // Merge
             {void Then()
               {z(); bT.clear();
                lT.clear();
                lT.concatenate(lL);                                             // Merge in left  child leaf
                lT.concatenate(lR);                                             // Merge in right child leaf

                free(l);                                                        // Free the children
                free(r);
                nT.setLeaf();                                                   // The root is now a leaf
                nT.saveRootStuck(lT);                                           // Save the leaf root
                z(); T.at(stolenOrMerged).ones(); P.Goto(Return);
               }
             };
            z(); T.at(stolenOrMerged).zero();
           }
          void Else()                                                           // Branches
           {z();
            nL.loadBranchStuckAndSize(bL, l, nl);
            nR.loadBranchStuckAndSize(bR, r, nr);

            P.new I()                                                           // Check that combined node would not be too big
             {void a()
               {T.at(mergeable).setInt
                 ((T.at(nl).getInt() + 1 + T.at(nr).getInt() <= maxKeysPerBranch()) ?
                  1 : 0);
               }
              String v()
               {return T.at(mergeable).verilogLoad() + " <= "   +
                          "("+T.at(nl).verilogLoad() + "+ 1 +"  +
                              T.at(nr).verilogLoad() + " <= "  +
                   maxKeysPerBranch()+") ? 'b1 : 'b0;  /* mergeRoot2 */";
               }
             };

            P.new If (T.at(mergeable))
             {void Then()
               {z();
                bT.firstElement();
                T.at(parentKey).move(bT.T.at(bT.tKey));
                bT.clear();

                bT.concatenate(bL);
                bT.T.at(bT.tKey).move(T.at(parentKey));
                bT.setLastKey();

                bT.concatenate(bR);
                bT.zeroLastKey();

                free(l);                                                        // Free the children
                free(r);
                nT.saveRootStuck(bT);                                           // Save the branch root
                z(); T.at(stolenOrMerged).ones(); P.Goto(Return);
               }
             };
            z(); T.at(stolenOrMerged).zero();
           }
         };
       };
     };
   }

  private void mergeLeftSibling()                                               // Merge the left sibling
   {zz();                                                                       // Assume that index has the node wanting to merge from its left sibling and that the parent branch is fully loaded in to nT/bT while being indexed with: node_mergeLeftSibling
    P.new Block()
     {void code()
       {z();
        T.at(index).isZero(T.at(stolenOrMerged));
        stealNotPossible(end);

        bT.size();
        bT.T.at(bT.size).lessThan(T.at(two), T.at(stolenOrMerged));
        stealNotPossible(end);

        bT.T.at(bT.index).add(T.at(index), -1);                                 // Locate left sibling
        bT.elementAt();

        P.parallelStart();   T.at(l).move(bT.T.at(bT.tData));                   // Locate right sibling being merged into
        P.parallelSection(); bT.T.at(bT.index).move(T.at(index));
        P.parallelEnd();
        bT.elementAt();

        T.at(r).move(bT.T.at(bT.tData));
                                                                                // Right sibling
        hasLeavesForChildren(bT);
        P.new If (T.at(hasLeavesForChildren))                                   // Children are leaves
         {void Then()
           {z();
            nL.loadLeafStuckAndSize(lL, l, nl);
            nR.loadLeafStuckAndSize(lR, r, nr);

            P.new I()                                                           // Check that combined node would not be too big
             {void a()
               {T.at(stolenOrMerged).setInt
                 ((T.at(nl).getInt() + T.at(nr).getInt() >= maxKeysPerLeaf()) ?
                  1 : 0);
               }
              String v()
               {return T.at(stolenOrMerged).verilogLoad() + " <= " +
                               "("+T.at(nl).verilogLoad() + " + "  +
                                   T.at(nr).verilogLoad() + " >= " +
                   maxKeysPerLeaf()+") ? 'b1 : 'b0; /* mergeLeftSibling1 */";
               }
             };
            stealNotPossible(end);

            lR.prepend(lL);
            nR.saveStuck(lR, r);                                                // Save modified right branch
           }
          void Else()                                                           // Children are branches
           {z();
            nL.loadBranchStuckAndSize(bL, l, nl);
            nR.loadBranchStuckAndSize(bR, r, nr);

            P.new I()                                                           // Check that combined node would not be too big
             {void a()
               {T.at(stolenOrMerged).setInt
                 ((T.at(nl).getInt() + 1 + T.at(nr).getInt() > maxKeysPerBranch()) ?
                  1 : 0);
               }
              String v()
               {return T.at(stolenOrMerged).verilogLoad() + " <= "   +
                               "("+T.at(nl).verilogLoad() + "+ 1 +"  +
                                   T.at(nr).verilogLoad() + " > "    +
                   maxKeysPerBranch()+") ? 'b1 : 'b0; /* mergeLeftSibling2 */";
               }
             };
            stealNotPossible(end);

            bT.T.at(bT.index).add(T.at(index), -1);                             // Top key
            bT.elementAt();                                                     // Top key

            bL.pop();                                                           // Last element of left child
            P.parallelStart();   bR.T.at(bR.tKey ).move(bT.T.at(bT.tKey));
            P.parallelSection(); bR.T.at(bR.tData).move(bL.T.at(bL.tData));
            P.parallelEnd();
            bR.unshift();                                                       // Left top to right

            bR.prepend(bL);
            nR.saveStuck(bR, r);                                                // Save modified right branch
           }
         };

        bT.T.at(bT.index).add(T.at(index), -1);                                 // Account for top
        bT.removeElementAt();                                                   // Reduce parent on left

        nT.saveStuck(bT, node_mergeLeftSibling);                                // Save parent branch
        free(l);                                                                // Free the left node whose contents have been merged away

        z(); T.at(stolenOrMerged).ones();
       }
     };
   }

  private void mergeRightSibling()                                              // Merge the right sibling
   {zz();                                                                       // Assume that index has the node wanting to merge from its right sibling and that the parent branch is fully loaded in to nT/bT while being indexed with: node_mergeRightSibling
    P.new Block()
     {void code()
       {nT.branchSize(T.at(branchSize));
        T.at(index).greaterThanOrEqual(T.at(branchSize), T.at(stolenOrMerged));
        stealNotPossible(end);
        bT.T.at(bT.size).lessThan(T.at(two), T.at(stolenOrMerged));
        stealNotPossible(end);

        bT.T.at(bT.index).move(T.at(index));
        bT.elementAt();

        P.parallelStart();   T.at(l).move(bT.T.at(bT.tData));
        P.parallelSection(); bT.T.at(bT.index).add(T.at(index), +1);
        P.parallelEnd();
        bT.elementAt();

        T.at(r).move(bT.T.at(bT.tData));
        hasLeavesForChildren(bT);
        P.new If (T.at(hasLeavesForChildren))                                   // Children are leaves
         {void Then()
           {z();
            nL.loadLeafStuckAndSize(lL, l, nl);
            nR.loadLeafStuckAndSize(lR, r, nr);

            P.new I()                                                           // Check that combined node would not be too big
             {void a()
               {T.at(stolenOrMerged).setInt
                 ((T.at(nl).getInt() + T.at(nr).getInt() > maxKeysPerLeaf()) ?
                  1 : 0);
               }
              String v()
               {return T.at(stolenOrMerged).verilogLoad() + " <= " +
                               "("+T.at(nl).verilogLoad() + " + "  +
                                   T.at(nr).verilogLoad() + " > "  +
                   maxKeysPerLeaf()+") ? 'b1 : 'b0;  /* mergeRightSibling1 */";
               }
             };
            stealNotPossible(end);

            lL.concatenate(lR);
            nL.saveStuck(lL, l);                                                // Save modified left branch
           }
          void Else()                                                           // Children are branches
           {z();
            nL.loadBranchStuckAndSize(bL, l, nl);
            nR.loadBranchStuckAndSize(bR, r, nr);


            P.new I()                                                           // Check that combined node would not be too big
             {void a()
               {T.at(stolenOrMerged).setInt
                 ((T.at(nl).getInt() + 1 + T.at(nr).getInt() > maxKeysPerBranch()) ?
                  1 : 0);
               }
              String v()
               {return T.at(stolenOrMerged).verilogLoad() + " <= "   +
                               "("+T.at(nl).verilogLoad() + "+ 1 +"  +
                                   T.at(nr).verilogLoad() + " > "    +
                   maxKeysPerBranch()+") ? 'b1 : 'b0; /* mergeRightSibling2 */";
               }
             };
            stealNotPossible(end);

            z(); bL.lastElement();                                              // Last element of left child
            z(); bT.T.at(bT.index).move(T.at(index));
            z(); bT.elementAt();                                                // Parent dividing element

            P.parallelStart();   bL.T.at(bL.tKey) .move(bT.T.at(bT.tKey));
            P.parallelSection(); bL.T.at(bL.index).move(T.at(nl));
            P.parallelEnd();
            bL.setElementAt();                                                  // Re-key left top

            bL.concatenate(bR);

            nL.saveStuck(bL, l);                                                // Save modified left branch
           } // Else
         };

        bT.T.at(bT.index).add(T.at(index), +1);
        bT.elementAt();

        P.parallelStart();   T.at(parentKey  ).move(bT.T.at(bT.tKey));          // One up from dividing point in parent
        P.parallelSection(); bT.T.at(bT.index).move(T.at(index));
        P.parallelSection(); free(r);                                           // Free the empty right node
        P.parallelEnd();
        bT.elementAt();                                                         // Dividing point in parent

        bT.T.at(bT.tKey).move(T.at(parentKey));
        bT.setElementAt();                                                      // Install key of right sibling in this child

        bT.T.at(bT.index).add(T.at(index), +1);
        bT.removeElementAt();                                                   // Reduce parent on right

        nT.saveStuck(bT, node_mergeRightSibling);                               // Save parent branch

        z(); T.at(stolenOrMerged).ones();
       }
     };
   }

//D2 Augment                                                                    // Fill out a node by merging and stealing

  private void augment()                                                        // Augment the indexed child if it has at least two children in its body
   {zz();
    P.new Block()
     {void code()
       {bT.T.at(bT.index).move(T.at(index));                                    // Index child to be augmented
        bT.elementAt();

        nC.loadNode(bT.T.at(bT.tData));                                         // Child node
        nC.branchSize(T.at(childSize));                                         // Size of branch accounting for top
        T.at(childSize).lessThan(T.at(two), T.at(isLow));                       // Check that the child node has at least two elements otherwise we cannot steal from it
        P.GoOff(end, T.at(isLow));

        tt(node_stealFromLeft,     node_balance); stealFromLeft    (); P.GoOn(end, T.at(stolenOrMerged));
        tt(node_stealFromRight,    node_balance); stealFromRight   (); P.GoOn(end, T.at(stolenOrMerged));
        tt(node_mergeLeftSibling,  node_balance); mergeLeftSibling (); P.GoOn(end, T.at(stolenOrMerged));
        tt(node_mergeRightSibling, node_balance); mergeRightSibling(); P.GoOn(end, T.at(stolenOrMerged));
       }
     };
   }

//D1 Position                                                                   // Locate the first or last key in the tree

  public void firstLast(boolean first)                                          // Find the first leaf in the treeif true else the last leaf in the tree
   {zz();
    P.new Block()
     {void code()
       {final ProgramDM.Label Return = end;
        nT.loadRoot();                                                          // The first thing in the tree is the root

        P.new Block()                                                           // The root is a leaf
         {void code()
           {P.GoOff(end, ifRootLeaf(nT));                                       // Confirm that the root is a leaf

            P.parallelStart();   T.at(leafFound).zero();                        // Leaf that should contain this key is the root
            P.parallelSection(); P.Goto(Return);
            P.parallelEnd();
           }
         };

        P.new Block()
         {void code()
           {nT.loadStuck(bT);                                                   // Load the stuck
            if (first) bT.firstElement(); else bT.lastElement();                // First or last element of stuck
            nT.loadNode(bT.T.at(bT.tData));                                     // Move down to next level

            P.new Block()                                                       // Found the containing leaf
             {void code()
               {P.GoOff(end, ifLeaf(nT));                                       // Confirm that it is a leaf

                P.parallelStart();   T.at(leafFound).move(bT.T.at(bT.tData));   // Save node index of leaf
                P.parallelSection(); P.Goto(Return);
                P.parallelEnd();
               }
             };

            P.Goto(start);                                                      // Restart traverse one level down
           }
         };
       }
     };
   }

  public void first() {firstLast(true);}                                        // Find the first leaf in the tree
  public void  last() {firstLast(false);}                                       // Find the last  leaf in the tree

//D1 Find                                                                       // Find the data associated with a key.

  public void find()                                                            // Find the leaf associated with a key in the tree
   {zz();
    P.new Block()
     {void code()
       {final ProgramDM.Label Return = end;
        nT.loadRoot();                                                          // The first thing in the tree is the root

        P.new Block()                                                           // The root is a leaf
         {void code()
           {P.parallelStart();   P.GoOff(end, ifRootLeaf(nT));                  // Confirm that the root is a leaf
            P.parallelSection(); findEqualInLeaf(T.at(Key), nT);                // Assume the root is a leaf and start looking for the key
            P.parallelEnd();

            P.parallelStart();   T.at(find).zero();                             // Leaf that should contain this key is the root
            P.parallelSection(); P.Goto(Return);
            P.parallelEnd();
           }
         };

        P.new Block()
         {void code()
           {findFirstGreaterThanOrEqualInBranch                                 // Find next child in search path of key
             (nT, T.at(Key), null, null, T.at(child));
            nT.loadNode(T.at(child));

            P.new Block()                                                       // Found the containing leaf
             {void code()
               {P.parallelStart();   P.GoOff(end, ifLeaf(nT));                  // Confirm that it is a leaf
                P.parallelSection(); findEqualInLeaf(T.at(Key), nT);
                P.parallelEnd();

                P.parallelStart();   tt(find, child);
                P.parallelSection(); P.Goto(Return);
                P.parallelEnd();
               }
             };

            P.Goto(start);                                                      // Restart search one level down
           }
         };
       }
     };
   }

  public void findNext()                                                        // Find the next key relative to the supplied key
   {zz();
    P.new Block()
     {void code()
       {final ProgramDM.Label Return = end;
        nT.loadRoot();                                                          // The first thing in the tree is the root

        P.new Block()                                                           // The root is a leaf
         {void code()
           {P.parallelStart();   P.GoOff(end, ifRootLeaf(nT));                  // Confirm that the root is a leaf
            P.parallelSection(); findEqualInLeaf(T.at(Key), nT);                // Assume the root is a leaf and start looking for the key
            P.parallelEnd();

            leafSize();
            final Variable empty = new Variable(P, "empty", 1);                 // Whether the tree is empty
            T.at(size).isZero(empty);
            P.new If(empty.a)
             {void Then()                                                       // The tree is empty so there is no next key
               {T.at(found).zero();
               }
              void Else()                                                       // The tree is not empty
               {T.at(size).dec();                                               // Index of the last element - we know that there is one
                final Variable more = new Variable(P, "more", 1);               // Whether there are mopr keys
                T.at(index).lessThan(T.at(size), more.a);
                P.new If(T.at(empty))
                 {void Then()                                                       // The tree is empty so there is no next key
                   {T.at(found).zero();
                   }
                  void Else()                                                       // The tree is not empty
                   {T.at(size).dec();                                               // Index of the last element - we know that there is one

               }

                 T.at(index).inc();
T.at(found), T.at(index)
T.at(found), T.at(index)
               }
             };
            T.at(find).zero();                                                  // Leaf that should contain this key is the root
            P.Goto(Return);
           }
         };

        P.new Block()
         {void code()
           {findFirstGreaterThanOrEqualInBranch                                 // Find next child in search path of key
             (nT, T.at(Key), null, null, T.at(child));
            nT.loadNode(T.at(child));

            P.new Block()                                                       // Found the containing leaf
             {void code()
               {P.parallelStart();   P.GoOff(end, ifLeaf(nT));                  // Confirm that it is a leaf
                P.parallelSection(); findEqualInLeaf(T.at(Key), nT);
                P.parallelEnd();

                P.parallelStart();   tt(find, child);
                P.parallelSection(); P.Goto(Return);
                P.parallelEnd();
               }
             };

            P.Goto(start);                                                      // Restart search one level down
           }
         };
       }
     };
   }

  private void findAndInsert(ProgramDM.Label Success)                           // Find the leaf that should contain this key and insert or update it is possible
   {zz();
    P.new Block()
     {void code()
       {final ProgramDM.Label Return = end;
        find();
        tt(leafFound, find);                                                    // Find the leaf that should contain this key

        P.new If (T.at(found))                                                  // Found the key in the leaf so update it with the new data
         {void Then()
           {z();
            P.parallelStart();    lEqual.T.at(lEqual.tKey ).move(T.at(Key));
            P.parallelSection();  lEqual.T.at(lEqual.tData).move(T.at(Data));
            P.parallelSection();  lEqual.T.at(lEqual.index).move(T.at(index));
            P.parallelEnd();
            lEqual.setElementAt();                                              // Update stuck - we are assuming that the new data element differs from the old one to  justify this action
            nT.saveStuck(lEqual, leafFound);                                    // Save the node into memory

            P.parallelStart();    T.at(success).ones();
            P.parallelSection();  T.at(inserted).zero();
            P.parallelSection();  tt(findAndInsert, leafFound);
            P.parallelSection();  P.Goto(Success == null ? Return : Success);
            P.parallelEnd();
           }
         };

        nT.isLeafFull();
        P.new If(T.at(isFull))
         {void Else()                                                           // Leaf is not full so we can insert immediately
           {z();
            tt(search, Key);
            tt(node_findFirstGreaterThanOrEqualInLeaf, leafFound);

            findFirstGreaterThanOrEqualInLeaf(lEqual, T.at(Key),                // Leaf known not to contain the search key
              T.at(found), lEqual.T.at(lEqual.index));

//          P.new If(T.at(found))                                               // Insert
//           {void Then()
               {z();
                P.parallelStart();    lEqual.T.at(lEqual.tKey ).move(T.at(Key));
                P.parallelSection();  lEqual.T.at(lEqual.tData).move(T.at(Data));
                P.parallelSection(); T.at(inserted).ones();
                P.parallelEnd();

                lEqual.insertElementAt();
               }
//              void Else()                                                       // Extend
//               {z();
//                P.parallelStart();   lEqual.T.at(lEqual.tKey ).move(T.at(Key));
//                P.parallelSection(); lEqual.T.at(lEqual.tData).move(T.at(Data));
//                P.parallelSection(); T.at(inserted).ones();
//                P.parallelEnd();
//                lEqual.push();
//               }
//             };
            nT.saveStuck(lEqual, leafFound);                                    // Save stuck back into memory
            P.parallelStart();   T.at(success).ones();
            P.parallelSection(); tt(findAndInsert, leafFound);
            P.parallelSection(); P.Goto(Success == null ? Return : Success);
            P.parallelEnd();
           }
         };
        T.at(success).zero();
       }
     };
   }

//D1 Insertion                                                                  // Insert a key, data pair into the tree or update and existing key with a new datum

  public void put()                                                             // Insert a key, data pair into the tree or update and existing key with a new datum
   {zz();
    P.new Block()
     {void code()
       {final ProgramDM.Label Return = end;
        findAndInsert(Return);                                                  // Try direct insertion with no modifications to the shape of the tree
        nT.loadRoot();                                                          // Load root
        nT.isFull();
        P.new If (T.at(isFull))                                                 // Start the insertion at the root(), after splitting it if necessary
         {void Then()
           {nT.isLeaf(T.at(IsLeaf));
            P.new If (T.at(IsLeaf))
             {void Then() {splitLeafRoot  ();}
              void Else() {
                splitBranchRoot();
                }
             };
            z();
            findAndInsert(Return);                                              // Splitting the root() might have been enough
           }
         };
        nT.loadRootStuck(bT);                                                   // Load root as branch. If it were a leaf and had spae find and insert would have worked or the root would have been split and so must be branch.
        T.at(parent).zero();

        P.new Block()                                                           // Step down through the tree, splitting as we go
         {void code()
           {findFirstGreaterThanOrEqualInBranch                                 // Step down from parent to child
             (nT, T.at(Key), null, T.at(first), T.at(child));

            P.new Block()                                                       // Reached a leaf
             {void code()
               {nC.loadNode(T.at(child));
                nC.isLeaf(T.at(IsLeaf));
                P.GoOff(end, T.at(IsLeaf));
                P.parallelStart();   tt(index,          first);                 // Index of the matching key
                P.parallelSection(); tt(node_splitLeaf, child);
                P.parallelSection(); tt(splitParent,   parent);
                P.parallelEnd();

                splitLeaf();                                                    // Split the child leaf
                findAndInsert(null);                                            // Now guaranteed to work

                merge();                                                        // Improve the tree along the path to the key
                P.Goto(Return);
               }
             };
            z();

            nC.loadNode(T.at(child));
            nC.branchSize(T.at(childSize));
            T.at(childSize).equal(T.at(maxKeysPerBranch), T.at(branchIsFull));  // Check whether the child needs splitting because it is full

            P.new If (T.at(branchIsFull))                                       // Step down, splitting full branches as we go
             {void Then()
               {P.parallelStart();   tt(index, first);
                P.parallelSection(); tt(node_splitBranch, child);
                P.parallelSection(); tt(splitParent, parent);
                P.parallelEnd();

                splitBranch();                                                  // Split the child branch in the search path for the key from the parent so the the search path does not contain a full branch above the containing leaf

                findFirstGreaterThanOrEqualInBranch                             // Perform the step down again as the split will have altered the local layout
                 (nT, T.at(Key), null, null, T.at(child));
               }
             };
            nT.loadStuck(bT, child);                                            // Step down "From the heights"
            tt(parent, child);
            P.Goto(start);
           }
         };
       }
     };
   }

//D1 Deletion                                                                   // Delete a key, data pair from the tree

  private void findAndDelete()                                                  // Delete a key from the tree and returns its data if present without modifying the shape of tree
   {zz();
    P.new Block()
     {void code()
       {find();                                                                 // Try direct insertion with no modifications to the shape of the tree
        P.GoOff(end, T.at(found));                                              // Key not found so nothing to delete
        z(); nT.loadStuck(lT, find);                                            // The leaf that contains the key
        lT.T.at(lT.index).move(T.at(index));
//      lT.elementAt();                                                         // Position in the leaf of the key
        lT.removeElementAt();                                                   // Remove the key, data pair from the leaf
        T.at(Data).move(lT.T.at(lT.tData));                                     // Key, data pairs in the leaf
        nT.saveStuck(lT, find);
       }
     };
   }

  public void delete()                                                          // Delete a key from the tree and return its associated Data if the key was found.
   {zz();
    P.new Block()                                                               // Step down through the tree, splitting as we go
     {void code()
       {final ProgramDM.Label Return = end;
        mergeRoot();
//      nT.loadRoot();

        P.new Block()                                                           // Find and delete directly in root as a leaf
         {void code()
           {P.GoOff(end, ifRootLeaf(nT));
            findAndDelete();

            P.parallelStart();   tt(deleted, found);
            P.parallelSection(); P.Goto(Return);
            P.parallelEnd();
           }
         };

        nT.loadRootStuck(bT);
        T.at(parent).zero();                                                    // Start at root

        P.new Block()                                                           // Step down through the tree, merging as we go
         {void code()
           {findFirstGreaterThanOrEqualInBranch
             (nT, T.at(Key), null, T.at(first), T.at(child));

            P.parallelStart();   tt(index, first);
            P.parallelSection(); tt(node_balance, parent);
            P.parallelEnd();

            augment();                                                          // Make sure there are enough entries in the parent to permit a deletion

            nC.loadNode(T.at(child));
            nC.isLeaf(T.at(isLeaf));
//          T.at(isLeaf).move(M.at(bTree_isLeaf, T.at(child)));
            P.new If (T.at(isLeaf))                                             // Reached a leaf
             {void Then()
               {z();
                findAndDelete();
                tt(deleted, found);
                merge();
                P.Goto(Return);
               }
             };
            nT.loadStuck(bT, child);
            tt(parent, child);
            P.Goto(start);
           }
         };
       };
     };
   }

//D1 Merge                                                                      // Merge along the specified search path

  private void merge()                                                          // Merge along the specified search path
   {zz();
    P.new Block()                                                               // Step down through the tree, splitting as we go
     {void code()
       {final ProgramDM.Label Return = end;
        mergeRoot();
        T.at(parent).zero();                                                    // Start at root

        P.new Block()                                                           // Step down through the tree, splitting as we go
         {void code()
           {nT.loadStuck(bT, parent);
            nT.isLeaf(T.at(IsLeaf));
            P.GoOn(Return, T.at(IsLeaf));                                       // Reached a leaf

            T.at(mergeIndex).zero();                                            // Index of child being merged
            P.new Block()                                                       // Try merging each sibling pair which might change the size of the parent
             {void code()
               {nT.branchSize(T.at(childSize));
                T.at(mergeIndex).greaterThanOrEqual(T.at(childSize),
                                                    T.at(nodeMerged));
                P.GoOn(end, T.at(nodeMerged));                                  // All sequential pairs of siblings have been offered a chance to merge

                P.parallelStart();   T.at(index).move(T.at(mergeIndex));
                P.parallelSection(); tt(node_mergeLeftSibling, parent);
                P.parallelEnd();
                mergeLeftSibling();

                P.new If (T.at(stolenOrMerged))                                 // A successful merge of the left  sibling reduces the current index and the upper limit
                 {void Then()
                   {T.at(mergeIndex).dec();
                   }
                 };

                P.parallelStart();   T.at(index).move(T.at(mergeIndex));
                P.parallelSection(); tt(node_mergeRightSibling, parent);
                P.parallelEnd();
                mergeRightSibling();                                            // A successful merge of the right sibling maintains the current position but reduces the upper limit

                T.at(mergeIndex).inc();
                P.Goto(start);
               }
             };

            findFirstGreaterThanOrEqualInBranch                                 // Step down
             (nT, T.at(Key), null, null, T.at(parent));

            P.Goto(start);
           }
         };
       }
     };
   }

   private String printBoxed()                                                  // Print a tree in a box
    {return btreePA().printBoxed();                                             //
    }

//D1 Verilog                                                                    // Generate verilog code that implements the instructions used to manipulate a btree

  abstract class VerilogCode                                                    // Generate verilog code
   {final String          project;                                              // Project name - used to generate file names
    final String           folder;                                              // Folder in which to place project
    final String        opCodeMap = "opCodeMap";                                // Name of op code map
    final String           nano9k = "nano9k";                                   // Name of folder containing code for the GoWin Nano 9k
    final String  siliconCompiler = "siliconCompiler";                          // Name of folder containing code for Silicon Compiler
    final ProgramDM       program;                                              // Program associated with this tree
    final StringToNumbers     ops = new StringToNumbers();                      // Collapse identical instructions
    final String      blockIndent = " ".repeat(10);                             // Indentation for Verilog case statements
    final String  statementIndent = " ".repeat(16);                             // Indentation for Verilog instruction code

    Integer            statements = null;                                       // Set if only one statement is to be generated
    Boolean            resultJava = null;                                       // Pass or fail of Java test

    abstract int     Key     ();                                                // Input key value
    abstract Integer Data    ();                                                // Input data value if not null
    abstract Integer found   ();                                                // Whether the key was found (1) or not (0) if not null
    abstract Integer data    ();                                                // Expected output data value if not null
    abstract int     maxSteps();                                                // Maximum number if execution steps
    abstract int     expSteps();                                                // Expected number of steps
    abstract String  expected();                                                // Expected number of steps
             int     density () {return 60;}                                    // Indication of gate density required on die.

    String projectFolder() {return ""+Paths.get(folder, project, ""+Key());}    // Define the project folder

    String     sourceVerilog() {return ""+Paths.get(projectFolder(), project                       +Verilog.ext);}
    String       testVerilog() {return ""+Paths.get(projectFolder(), project                       +Verilog.testExt);}
    String     nano9kVerilog() {return ""+Paths.get(projectFolder(), nano9k,     project           +Verilog.ext);}
    String   nano9kTestBench() {return ""+Paths.get(projectFolder(), nano9k,     project           +Verilog.testExt);}
    String nano9kConstraints() {return ""+Paths.get(projectFolder(), nano9k,     project           +Verilog.constraintsExt);}
    String       nano9kBuild() {return ""+Paths.get(projectFolder(), nano9k,     project           +".pl");}
    String           scBuild() {return ""+Paths.get(projectFolder(), siliconCompiler, project      +".py");}
    String          scSource() {return ""+Paths.get(projectFolder(), siliconCompiler, project      +Verilog.ext);}
    String          scMemory() {return ""+Paths.get(projectFolder(), siliconCompiler, "memory"     +Verilog.ext);}
    String     scConstraints() {return ""+Paths.get(projectFolder(), siliconCompiler, project      +".sdc");}
    String     declareMemory() {return ""+Paths.get(projectFolder(), "includes", "declareMemory"   +Verilog.header);}
    String  initializeMemory() {return ""+Paths.get(projectFolder(), "includes", "initializeMemory"+Verilog.header);}
    String     opCodeMapFile() {return ""+Paths.get(projectFolder(), "includes", opCodeMap         +Verilog.header);}
    String         testsFile() {return ""+Paths.get(projectFolder(), "tests.txt");}
    String         traceFile() {return ""+Paths.get(projectFolder(), "trace.txt");}
    String     javaTraceFile() {return ""+Paths.get(projectFolder(), "traceJava.txt");}

    VerilogCode(String Project, String Folder)                                  // Generate verilog code
     {zz();
      project = Project; folder = Folder; program = P;

      //removeMemories();                                                       // Remove memories reported as not used from Vivado

      T.at(Key).setInt(Key());                                                  // Key value
      if (Data() != null) T.at(Data).setInt(Data());                            // Optional data value to insert into tree
      generateVerilog();                                                        // Generate verilog

      execJavaTest();                                                           // Execute the Java test to load memories
      if (resultJava)                                                           // Generate verilog if Java test executed successfully
       {execVerilogTest();                                                      // Execute the corresponding Verilog test if the java test passed
       }
     }

    void execJavaTest()                                                         // Execute the Java test
     {zz();
      deleteFile(javaTraceFile());
      P.run(javaTraceFile());                                                   // Run the Java version and trace it

      resultJava = ok(P.steps+1, expSteps());                                   // Steps in Java code
      if (found() != null) ok(T.at(found).getInt(), found());                   // Whether the data was found or not
      if (data () != null) ok(T.at(data) .getInt(), data());                    // Data associated with key from java code
      if (debug) stop(""+thisBTree);                                            // Print tree if debugging
      if (expected() != null) ok(BtreeSF.this, expected());                     // Check resulting tree
     }

    void compactCode()                                                          // Reuse comon instructions rather then regenerating them
     {zz();
      for(int i = 0; i < program.code.size(); ++i)                              // Write each instruction
       {final Stack<ProgramDM.I> I = program.code.elementAt(i);                 // The block of parallel instructions to write
        if (I.size() > 1)
         {final StringBuilder t = new StringBuilder();
          for(ProgramDM.I j : I) t.append(statementIndent+"    "+j.v()+"\n");
          ops.put(t.toString(), i);
         }
        else
         {final ProgramDM.I j = I.firstElement();
          ops.put(j.v(), i);
         }
       }
      ops.order();                                                              // Order the instructions
     }

//    boolean requiredMemory(MemoryLayoutDM m)                                    // Check memory is required for this project
//     {zz();
//      final String r = removableMemories.get(project);                          // Removable memories for this project
//      if (r == null) return true;                                               // No removable memories yet
//      return !r.contains(" "+m.name+" ");                                       // Check whether memory is removable or not
//     }
//
//    void removeMemories()                                                       // Remove memories reported as unneeded
//     {zz();
//      final Stack<MemoryLayoutDM> r = new Stack<>();                            // Memories that can be removed
//      for(MemoryLayoutDM m : P.memories) if (!requiredMemory(m)) r.push(m);     // Each memory not used by the program
//      for(MemoryLayoutDM m : r) P.memories.remove(m);
//     }

    void declareMemories()                                                      // Declare memories
     {zz();
      final StringBuilder s = new StringBuilder();
      for(MemoryLayoutDM m : P.memories)                                        // Each memory used by the program
       {if (!m.block.blocked()) s.append("  "+m.declareVerilog()+"\n");
       }
      writeFile(declareMemory(), s);
     }

    void initializeMemories()                                                   // Initialize memories
     {zz();
      final StringBuilder s = new StringBuilder();
      for(MemoryLayoutDM m : P.memories)                                        // Each memory declared by the program
       {if (!m.block.blocked()) s.append("  "+m.initializeVerilog()+"\n");
       }
      writeFile(initializeMemory(), s);
     }

    String verilogMemoryPrintFormat()                                           // Format statement for printing memory from Verilog to match the tracing used in Java
     {zz();
      final StringBuilder f = new StringBuilder("\"%4d  %4d  %4d ");            // Format code  to be used to print an execution trace
      widthOfMarginInExecutionTrace = 18;                                       // Derived from the previous line
      final StringBuilder s = new StringBuilder("\", steps, step, opCodeMap[step]");

      for(MemoryLayoutDM m : P.memories)
       {final MemoryLayoutDM.BlockArray a = m.block;
        if (a.blocked())
         {final String name = "memory_"+ m.name()+'.'+m.name();                 // Name of the memory module array elements as seen from outside the module
          for (int i = 0; i < a.size; i++)
           {f.append(" " +m.name()+"["+i+"]"+"=%b");
            s.append(", "+name    +"["+i+"]");                                  // Extract the current value from inside the instantiated module
           }
         }
        else
         {final String n = m.name();
          f.append(" "+n+"=%b");
          s.append(", "+m.name());
         }
       }
      return ""+f+s;
     }

    String genOpCodes()                                                         // Generate opcodes
     {zz();
      final StringBuilder s = new StringBuilder();                              // Verilog
      if (OpCodes)                                                              // Reduce program size by refactoring op codes at the cost of one additional look up per instruction cycle. Also appears to reduce synthesis time by about 30% on Vivado and likewise reduces the number of FPGA cells.
       {s.append("      case(opCodeMap[step])\n");                              // Case statements to select the code for the current instruction

        for(StringToNumbers.Order o : ops.outputOrder)                          // I shall say each instruction only once
         {final int    n = o.ordinal;                                           // Step
          if (statements != null && statements != n) continue;                  // Generating just one statement so we can check timing for this statement
          final String I = o.string;                                            // Instruction before next added
          final String i;                                                       // Instruction after next added
          final String p = blockIndent, q = statementIndent;

          if      (I.contains("/*GoTo*/"))   i = I;
          else if (I.contains("/*GoNext*/")) i = I.replace("/*GoNext*/", " else step <= step+1;");
          else                               i = I +                          " step <= step+1;";

          if (i.contains("\n"))                                                 // Multi line instruction
           {s.append(String.format("%s%4d : begin\n%s\n%s end\n", p, n, i, q));
           }
          else                                                                  // Single line instruction
           {s.append(String.format("%s%4d : begin %s end\n",      p, n, i));
           }
         }
       }
      else                                                                      // Write the code for each instruction at each step
       {s.append("      case(step)");                                           // Case statements to select the code for the current instruction
        final String p = blockIndent, q = statementIndent;
        for(int i = 0; i < program.code.size(); ++i)                            // Write each instruction
         {final Stack<ProgramDM.I> I = program.code.elementAt(i);               // The block of parallel instructions to write
          final int N = I.size();
          if (N > 1)
           {final StringBuilder t = new StringBuilder();
            for(ProgramDM.I j : I) t.append(q+"    "+j.v()+"\n");
            s.append(String.format("%s%5d : begin\n", p, i));
            s.append(t);
            s.append(q+"  end\n");
           }
          else if (N == 1)
           {final ProgramDM.I j = I.firstElement();
            final String t = j.v();
            s.append(String.format("%s%5d : begin %s end\n", p, i, t));         // Bracket instructions in this block with op code
           }
         }
       }
      s.append("""
        default : begin stopped <= 1; /* end of execution */ end                // Any invalid instruction address causes the program to halt
      endcase
""");
      return ""+s;
     }

    void generateVerilogCode()                                                  // Generate verilog code for standalone verification and for Vivado
     {zz();
      final StringBuilder s = new StringBuilder();                              // Generate code
      s.append("""
//-----------------------------------------------------------------------------
// Database on a chip simulation
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2025-01-07
//------------------------------------------------------------------------------
`timescale 10ps/1ps
(* keep_hierarchy = "yes" *)
module $project(reset, stop, clock, Key, Data, data, found);                    // Database on a chip
  input                   reset;                                                // Restart the program run sequence when this goes high
  input                   clock;                                                // Program counter clock
  input [$bitsPerKey-1 :0]  Key;                                                // Input key
  input [$bitsPerData-1:0] Data;                                                // Input data
  output                   stop;                                                // Program has stopped when this goes high
  output[$bitsPerData-1:0] data;                                                // Output data
  output                  found;                                                // Whether the key was found on put, find delete

  `include "includes/declareMemory.vh"                                          // Declare memory
  `include "includes/opCodeMap.vh"                                              // Op code map gives step to instruction

  reg [$nodeSize-1:0] memoryOut;                                                // Output from memory

  Memory memory_M(.clock(clock), .reset(reset),                                 // Create main memory with the expected name.  There is only one such at the moment but perhaps in the future there will be more at which point it would be worth creating the corresponding memory module automatically.
      .write  ($memoryIOWrite),
      .in     ($memoryInBuffer),
      .out    (memoryOut),                                                      // IVerilog will drive a subset of an array so pipeline instead
      .index  ($memoryIOAddress));

  integer  step;                                                                // Program counter
  integer steps;                                                                // Number of steps executed
  integer traceFile;                                                            // File to write trace to
  reg   stopped;                                                                // Set when we stop
  assign stop  = stopped > 0 ? 1 : 0;                                           // Stopped execution
  assign found = T[$found_at];                                                  // Found the key
  assign data  = T[$data_at+:$data_width];                                      // Data associated with key found

  always @ (posedge clock) begin                                                // Execute next step in program
    if (reset) begin                                                            // Reset
      step     <= 0;
      steps    <= 0;
      stopped  <= 0;

     `include "includes/initializeMemory.vh"                                    // Load memory
      $initialize_opCodeMap();                                                  // Initialize op code map

      traceFile = $fopen("$traceFile", "w");                                    // Open trace file
      if (!traceFile) $fatal(1, "Cannot open trace file $traceFile");

      T[$Key_at +:$Key_width ] <= Key;                                          // Load test key
      T[$Data_at+:$Data_width] <= Data;                                         // Load test data
    end
    else begin                                                                  // Run
      $display            ($format);                                            // Trace execution
      $fdisplay(traceFile, $format);                                            // Trace execution in a file
$opCodes
      steps <= steps + 1;
    end
  end // Always
endmodule

module Memory                                                                   // Memory used to hold the btree
 (input                         reset,                                          // Reinitialize memory when this bit goes high
  input                         clock,                                          // Clock
  input      [$bitsPerNext-1:0] index,                                          // Index of node in memory
  input      [$nodeSize-1:0]       in,                                          // Input to memory
  output reg [$nodeSize-1:0]      out,                                          // Output from memory
  input                         write);                                         // Write into memory if true
$memoryDeclare

  always @ (posedge clock) begin                                                // Execute next step in program
    if (reset) begin                                                            // Reset
$memoryInitialize
    end
    else begin
      if (write) begin
        M[index] <= in;
      end
      else begin
        out = M[index];
      end
    end
  end
endmodule
""");
      writeFile(sourceVerilog(), editVariables(s));                             // Write verilog module
     }

    void generateVerilogForSynthesis()                                          // Generate verilog code for Nano 9k
     {zz();
      final StringBuilder s = new StringBuilder();
      s.append("""
//-----------------------------------------------------------------------------
// Database on a chip for nano 9k and other real devices or OpenRoad asic flow
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2025-01-07
//------------------------------------------------------------------------------
`timescale 10ps/1ps
(* keep_hierarchy = "yes" *)
module $project(button1, stop, clock, Key, Data, data, found, led);             // Database on a chip
  input                     button1;                                            // Restart the program run sequence when this button is pushed
  input                     clock;                                              // Program counter clock
  input  [$bitsPerKey-1:0]  Key;                                                // Input key
  input  [$bitsPerData-1:0] Data;                                               // Input data
  output                    stop;                                               // Program has stopped when this goes high
  output [$bitsPerData-1:0] data;                                               // Output data
  output                    found;                                              // Whether the key was found on put, find delete
  output [6-1:0]            led;

  assign led = ~{data,found,stop};                                              // Show output on leds

  wire reset;
  assign reset = button1_state;                                                 // Button if button 1 is not beiong pushed

  `include "../includes/declareMemory.vh"                                       // Declare memory
  `include "../includes/opCodeMap.vh"                                           // Op code map gives step to instruction

  integer  step;                                                                // Program counter
  reg    stopped;                                                               // Set when we stop
  assign stop  = stopped > 0 ? 1 : 0;                                           // Stopped execution
  assign found = T[$found_at];                                                  // Found the key
  assign data  = T[$data_at+:$data_width];                                      // Data associated with key found

  reg [16-1:0] button1_count;                                                   // 16 bit integer used to debounce the button
  reg button1_state;

  reg [$nodeSize-1:0] memoryOut;                                                // Output from memory

  Memory memory(.clock(clock), .reset(reset),
      .write  ($memoryIOWrite),
      .in     ($memoryInBuffer),
      .out    (memoryOut),
      .index  ($memoryIOAddress));

  initial begin                                                                 // It does not matter what state we start in as long as we start in some state
    button1_count = 0;
  end

  always @(posedge clock) begin                                                 // Debounce button 1
    if      (!button1 && button1_count > 0) button1_count <= button1_count - 1;
    else if ( button1 && button1_count < 8) button1_count <= button1_count + 1;
    button1_state <= button1_count     < 4;                                     // Button state == 0 when pushed and debounced
  end

  always @ (posedge clock) begin                                                // Execute next step in program
    if (reset) begin                                                            // Reset as long as the button is not being pushed, run the program if it is being pushed
      step     <= 0;
      stopped  <= 0;

     `include "../includes/initializeMemory.vh"                                 // Load memory
      initialize_opCodeMap();                                                   // Initialize op code map

      T[$Key_at +:$Key_width ] <= $Key;                                         // Load test key
      T[$Data_at+:$Data_width] <= $Data;                                        // Load test data
    end

    else begin                                                                  // Run
$opCodes
    end // Execute
  end // Always
endmodule
""");
      writeFile(nano9kVerilog(), editVariables(s));                             // Write verilog for nano 9k
      writeFile(scSource(),      editVariables(s));                             // Write verilog for silicon compiler
     }

    void generateVerilogForMemory()                                             // Generate verilog code for memory black box
     {zz();
      final StringBuilder s = new StringBuilder();                              // Generate code
      s.append("""
//-----------------------------------------------------------------------------
// Database on a chip memory black box
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2025
//------------------------------------------------------------------------------
`timescale 10ps/1ps
(* keep_hierarchy = "yes" *)
(* blackbox *)
module Memory                                                                   // Memory used to hold the btree
 (input                         reset,                                          // Reinitialize memory when this bit goes high
  input                         clock,                                          // Clock
  input      [$bitsPerNext-1:0] index,                                          // Index of node in memory
  input      [$nodeSize-1:0]       in,                                          // Input to memory
  output reg [$nodeSize-1:0]      out,                                          // Output from memory
  input                         write);                                         // Write into memory if true
endmodule
""");
      writeFile(scMemory(), editVariables(s));                                  // Write verilog for silicon compiler
     }

    void generateVerilogTestBench()                                             // Generate verilog test bench for normal and Vivado versions
     {zz();
      final StringBuilder s = new StringBuilder();                              // Test bench
      s.append("""
//-----------------------------------------------------------------------------
// Database on a chip test bench
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2025-01-07
//------------------------------------------------------------------------------
`timescale 10ps/1ps
module $project_tb;                                                             // Test bench for database on a chip
  reg                   reset;                                                  // Restart the program run sequence when this goes high
  reg                   stop;                                                   // Program has stopped when this goes high
  reg                   clock;                                                  // Program counter clock
  reg  [$bitsPerKey -1:0] Key  = $Key;                                          // Input key
  reg  [$bitsPerData-1:0]Data = $Data;                                          // Input data
  reg  [$bitsPerData-1:0]data;                                                  // Output data
  reg                   found;                                                  // Whether the key was found on put, find delete
  integer testResults;                                                          // Test results file

  $project a1(.reset(reset), .stop(stop), .clock(clock),                        // Connect to the module
    .Key(Key), .Data(Data), .data(data), .found(found));

  initial begin                                                                 // Test the module
    clock = 0; reset = 0; #1;                                                   // Reset the module
    clock = 1; reset = 1; #1;
    clock = 0; reset = 0; #1;
    execute();
  end

  task execute;                                                                 // Clock the module until it says it has stopped
    integer step;
    begin
      for(step = 0; step < $maxSteps && !stop; step = step + 1) begin
        clock = 0; #1; clock = 1; #1;
      end
      if (stop) begin                                                           // Stopped
        testResults = $fopen("$testsFile", "w");
        $fdisplay(testResults, "Steps=%1d\\nKey=%1d\\ndata=%1d\\n", step, Key, data);
        $fclose(testResults);
        $display ("Steps=%1d\\nKey=%1d\\ndata=%1d\\n", step, Key, data);
      end
      else begin                                                                // Not stopped
        $display ("Out of steps at step: %d\\n", step);
      end
    end
  endtask
endmodule
""");
      writeFile(testVerilog(), editVariables(s));                               // Write verilog test bench
     }

    void generateVerilogTestBenchNano9K()                                       // Generate verilog test bench for Nano 9k using leds to veryify correct operation
     {zz();
      final StringBuilder s = new StringBuilder();
      s.append("""
//-----------------------------------------------------------------------------
// Database on a chip test bench for nano 9k
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2025-03-21
//------------------------------------------------------------------------------
`timescale 10ps/1ps
module $project_tb;                                                             // Test bench for database on a chip
  reg                 button1;                                                  // Restart the program run sequence when this button is pushed
  reg                   clock;                                                  // Program counter clock
  reg [$bitsPerKey-1:0]   Key;                                                  // Input key
  reg [$bitsPerData-1:0] Data;                                                  // Input data
  reg                    stop;                                                  // Program has stopped when this goes high
  reg                   found;                                                  // Whether the key was found on put, find delete
  reg [$bitsPerData-1:0] data;                                                  // Output data
  reg [6-1:0]             led;                                                  // leds

  find a1                                                                       // Connect to the module
   (.button1(button1),
    .clock(clock),
    .Key(Key),
    .Data(Data),
    .stop(stop),
    .found(found),
    .data(data),
    .led(led));

  integer step;

  initial begin                                                                 // Test the module
    Key = 3; button1 = 0;                                                       // Search key
//  for(step = 0; step < 40000; step = step + 1) begin                          // Let the device reset
    for(step = 0; step < 16; step = step + 1) begin                             // Let the device reset
      clock = 0; #1;                                                            // Resets if button 1 not touched for a long time
      clock = 1; #1;
    end

    button1 = 1;                                                                // Program runs while button 1 is pressed
    $display("%d  %d  %d  %d  %d  %d", 0, stop, Key, found, data, led);

    for(step = 0; step < 100 && !stop; step = step + 1) begin
      clock = 0; #1; clock = 1; #1;
      $display("%2d      %2d                    %2d  %2d  %6b", step, stop,          found, data, ~led);
    end
  end
endmodule
""");
      writeFile(nano9kTestBench  (), editVariables(s));                         // Write verilog test bench for nano 9k
     }

    void generateVerilogConstraintsNano9K()                                     // Generate constraints for Nano 9k using leds to veryify correct operation
     {final StringBuilder s = new StringBuilder();                              // Constraints for nano 9k
      s.append("""
IO_LOC "led[0]" 10;
IO_LOC "led[1]" 11;
IO_LOC "led[2]" 13;
IO_LOC "led[3]" 14;
IO_LOC "led[4]" 15;
IO_LOC "led[5]" 16;
IO_PORT "led[0]" PULL_MODE=NONE DRIVE=8;
IO_PORT "led[1]" PULL_MODE=NONE DRIVE=8;
IO_PORT "led[2]" PULL_MODE=NONE DRIVE=8;
IO_PORT "led[3]" PULL_MODE=NONE DRIVE=8;
IO_PORT "led[4]" PULL_MODE=NONE DRIVE=8;
IO_PORT "led[5]" PULL_MODE=NONE DRIVE=8;
IO_LOC  "clock" 52;
IO_LOC  "button1" 4;
IO_PORT "button1" PULL_MODE=UP;
IO_LOC  "button2" 3;
IO_PORT "button2" PULL_MODE=UP;
""");
      writeFile(nano9kConstraints(), editVariables(s));                         // Write constraints for nano 9k
     }

    void generateBuildNano9K()                                                  // Generate build commands for Nano 9k fpga
     {zz();
      final StringBuilder s = new StringBuilder();
      s.append("""
#!/usr/bin/perl -I/home/phil/perl/cpan/DataTableText/lib/
#-------------------------------------------------------------------------------
# Find from Database On A Chip running on a Nano 9K
# Philip R Brenan at gmail dot com, Appa Apps Ltd Inc., 2025
#-------------------------------------------------------------------------------
use v5.38;
use warnings FATAL => qw(all);
use strict;
use Data::Dump qw(dump);
use Data::Table::Text qw(:all);

my $project = q(find);
my $verilog = fpe $project, q(v);
my $cst     = fpe $project, q(xdc);
my $bits    = fpe $project, q(fs);
my $cable   = q(ft2232);
my $device  = q(GW1NR-LV9QN88PC6/I5);
my $pack    = q(GW1N-9C);

my $reports = q(reports);
my $build   = q(build);
my $y       = fpe $reports, qw(yosys txt);
my $n       = fpe $reports, qw(nextpnr-gowin txt);

my $yj      = fpe $build,   qw(yosys   json);
my $nj      = fpe $build,   qw(nextpnr json);
my $pj      = fpe $build,   qw(pack    json);

makePath($_) for $y, $yj;

if (1)
 {my $cmd = qq(yosys -p "read_verilog $verilog; synth_gowin -top $project -json $yj"    | tee $y);
  say STDERR $cmd;
  system($cmd);
  die if $?;
 }

if (1)
 {my $cmd = qq(nextpnr-gowin --json $yj  --write $nj --device "$device" --cst $cst 2>&1 | tee $n);
  say STDERR $cmd;
  system($cmd);
  die if $?;
 }

system(qq(gowin_pack     -d $pack -o $bits $nj));
system(qq(openFPGALoader -c $cable   $bits));
""");
      writeFile(nano9kBuild(), s);                                              // Write build file for Nano 9k
     }

    void generateSiliconCompiler()                                              // Generate build commands for Silicon compiler to get an asic mask
     {zz();
      final StringBuilder s = new StringBuilder();
      s.append("""
#!/usr/bin/env python3

from siliconcompiler import Chip                                                # import python package
from siliconcompiler.targets import $processTechnology_demo

if __name__ == "__main__":
    chip = Chip('$project')                                                     # Create chip object
    chip.input('/home/azureuser/btreeBlock/verilog/$project/$Key/siliconCompiler/$project.v') # Source code
    chip.input('/home/azureuser/btreeBlock/verilog/$project/$Key/siliconCompiler/memory.v'  ) # Memory black box
   #chip.input('/home/azureuser/btreeBlock/verilog/$project/$Key/siliconCompiler/$project.sdc')
    chip.set('design', '$project')                                              # Show the top most module
    chip.use($processTechnology_demo)                                           # Load predefined technology and flow target
    chip.set('package', 'description', '$designDescription')                    # Description of design
    chip.clock('clock', period=10)                                              # Define clock speed of design was 100
    chip.set('option', 'remote', False)                                         # Run remote in the cloud
    chip.set('option', 'nodisplay', True)                                       # Do not open displays
    chip.set('option', 'loglevel', 'warning')                                   # Warnings and above
   #chip.set('constraint', 'density', $density)                                 # Lowering the density gives more area in which to route connections at the cost of wasting surface area and making the chip run slower. For find it seems best to leave this parameter alone
    chip.set('option', 'clean', True)                                           # Clean start else it reuses previous results
    chip.run()                                                                  # Run compilation of design and target
    chip.summary()
    chip.snapshot()
# chip.show()
# chip.set('option', 'define', 'CFG_ASIC=1')
# chip.set('option', 'env', 'PDK_HOME', '/disk/mypdk')
# chip.set('option', 'idir', './mylib')
# chip.set('option', 'loglevel', 'warning')
# chip.set('option', 'nodisplay', True)
""");
      writeFile(scBuild(), editVariables(s));                                   // Write build file for silicon compiler

      final StringBuilder c = new StringBuilder();
      c.append("""
create_clock -name clock -period 100 [get_ports {clock}]
""");
      writeFile(scConstraints(), editVariables(c));                             // Write constraints file for silicon compiler
     }

    void generateVerilog()                                                      // Generate verilog
     {zz();
      makePath(projectFolder());                                                // Write files to the project folder
      P.setUniqueNames();
      compactCode();                                                            // Compact the code to make better use of the surface area of the chip

      ops.genVerilog(opCodeMapFile(), opCodeMap);                               // Write op code map

      declareMemories();
      initializeMemories();

      generateVerilogCode();                                                    // Generate code and test banches for various devices
      generateVerilogForSynthesis();                                            // Synthesizable verilog
      generateVerilogTestBench();                                               // Test bench
      generateSiliconCompiler();                                                // Silicon compiler Python driver
      generateVerilogForMemory();                                               // Black box for memory

      if (project.equalsIgnoreCase("find"))                                     // Only the find project will fit on the nano 9k
       {generateVerilogTestBenchNano9K();
        generateVerilogConstraintsNano9K();
        generateBuildNano9K();
       }
     }

    VerilogCode execVerilogTest()                                               // Execute the Verilog test and compare it with the results from execution under Java
     {zz();
      final StringBuilder s = new StringBuilder(editVariables("cd $projectFolder && iverilog $project.tb $project.v -Iincludes -g2012 -o $project -DSIMULATION && ./$project"));
      deleteFile(traceFile());
      final ExecCommand   x = new ExecCommand(s);
      final String        e = joinLines(readFile(javaTraceFile()));             // Read java output
      final String        g = joinLines(readFile(traceFile()));                 // Execute verilog
      ok(x.exitCode, 0);                                                        // Confirm exit code
      ok(widthOfMarginInExecutionTrace, g, e);                                  // Width of margin in verilog traces
      //ok(0, g, e);                                                            // Width of margin in verilog traces
      final TreeMap<String,String> p = readProperties(testsFile());             // Load test results
      ok(ifs(p.get("Steps")), expSteps());                                      // Confirm results from Verilog
      ok(ifs(p.get("data")),  data());
      return this;
     }

    private String editVariables(StringBuilder S)                               // Edit the variables in a string builder
     {zz();
      return editVariables(""+S);
     }

    private String editVariables(String s)                                      // Edit the variables in a string builder
     {zz();
      s = s.replace("$bitsPerKey",    ""  + bitsPerKey());
      s = s.replace("$bitsPerData",   ""  + bitsPerData());
      s = s.replace("$testsFile",           fileName(testsFile()));
      s = s.replace("$traceFile",           fileName(traceFile()));
      s = s.replace("$projectFolder",       projectFolder());
      s = s.replace("$project",             project);
      s = s.replace("$Key_at",           ""+Key.at);
      s = s.replace("$Key_width",        ""+Key.width);
      s = s.replace("$Data_at",          ""+Data.at);
      s = s.replace("$Data_width",       ""+Data.width);
      s = s.replace("$data_at",          ""+data.at);
      s = s.replace("$data_width",       ""+data.width);
      s = s.replace("$data",             ""+data());
      s = s.replace("$Key",              ""+Key());
      s = s.replace("$Data",             ""+(Data() != null ? Data() : 0));
      s = s.replace("$nodeSize",         ""+nodeLayout.size());
      s = s.replace("$numberOfNodes",    ""+maxSize());
      s = s.replace("$bitsPerAddress",   ""+bitsPerAddress);
      s = s.replace("$bitsPerNext",      ""+bitsPerNext);
      s = s.replace("$maxSteps",         ""+maxSteps());
      s = s.replace("$expSteps",         ""+expSteps());
      s = s.replace("$found_at",         ""+found.at);
      s = s.replace("$format",           ""+verilogMemoryPrintFormat());
      s = s.replace("$initialize_opCodeMap","initialize_"+opCodeMap);           // Initialize op code map
      s = s.replace("$processTechnology",   processTechnology);                 // Process technology used for building chips
      s = s.replace("$memoryInitialize",  M.initializeVerilog());               // Initialize memory
      s = s.replace("$memoryIOWrite", T.at(memoryIODirection).verilogLoad());   // True - we are writing to memory, 0 reading from memory
      s = s.replace("$memoryInBuffer",    memoryIn.N.top().verilogLoad());      // The buffer to hold a node in transit into memory
      s = s.replace("$memoryIOAddress",   T.at(memoryIOAddress).verilogLoad()); // Address to read or write from or to memory
      s = s.replace("$memoryDeclare",     M.declareVerilog());                  // Declaration of memory
      s = s.replace("$opCodes",           genOpCodes());                        // Generate op codes
      s = s.replace("$density",        ""+density());                           // An indication of the gate density to use on the chip
      s = s.replace("$designDescription", designDescription);                   // Description of this iteration

      return s;
     }
   }

//D0 Tests                                                                      // Testing

  final static int[]random_small = {27, 442, 545, 317, 511, 578, 391, 993, 858, 586, 472, 906, 658, 704, 882, 246, 261, 501, 354, 903, 854, 279, 526, 686, 987, 403, 401, 989, 650, 576, 436, 560, 806, 554, 422, 298, 425, 912, 503, 611, 135, 447, 344, 338, 39, 804, 976, 186, 234, 106, 667, 494, 690, 480, 288, 151, 773, 769, 260, 809, 438, 237, 516, 29, 376, 72, 946, 103, 961, 55, 358, 232, 229, 90, 155, 657, 681, 43, 907, 564, 377, 615, 612, 157, 922, 272, 490, 679, 830, 839, 437, 826, 577, 937, 884, 13, 96, 273, 1, 188};
  final static int[]random_large = {5918,5624,2514,4291,1791,5109,7993,60,1345,2705,5849,1034,2085,4208,4590,7740,9367,6582,4178,5578,1120,378,7120,8646,5112,4903,1482,8005,3801,5439,4534,9524,6111,204,5459,248,4284,8037,5369,7334,3384,5193,2847,1660,5605,7371,3430,1786,1216,4282,2146,1969,7236,2187,136,2726,9480,5,4515,6082,969,5017,7809,9321,3826,9179,5781,3351,4819,4545,8607,4146,6682,1043,2890,2964,7472,9405,4348,8333,2915,9674,7225,4743,995,1321,3885,6061,9958,3901,4710,4185,4776,5070,8892,8506,6988,2317,9342,3764,9859,4724,5195,673,359,9740,2089,9942,3749,9208,1,7446,7023,5496,4206,3272,3527,8593,809,3149,4173,9605,9021,5120,5265,7121,8667,6911,4717,2535,2743,1289,1494,3788,6380,9366,2732,1501,8543,8013,5612,2393,7041,3350,3204,288,7213,1741,1238,9830,6722,4687,6758,8067,4443,5013,5374,6986,282,6762,192,340,5075,6970,7723,5913,1060,1641,1495,5738,1618,157,6891,173,7535,4952,9166,8950,8680,1974,5466,2383,3387,3392,2188,3140,6806,3131,6237,6249,7952,1114,9017,4285,7193,3191,3763,9087,7284,9170,6116,3717,6695,6538,6165,6449,8960,2897,6814,3283,6600,6151,4624,3992,5860,9557,1884,5585,2966,1061,6414,2431,9543,6654,7417,2617,878,8848,8241,3790,3370,8768,1694,9875,9882,8802,7072,3772,2689,5301,7921,7774,1614,494,2338,8638,4161,4523,5709,4305,17,9626,843,9284,3492,7755,5525,4423,9718,2237,7401,2686,8751,1585,5919,9444,3271,1490,7004,5980,3904,370,5930,6304,7737,93,5941,9079,4968,9266,262,2766,4999,2450,9518,5137,8405,483,8840,2231,700,8049,8823,9811,9378,3811,8074,153,1940,1998,4354,7830,7086,6132,9967,5680,448,1976,4101,7839,3122,4379,9296,4881,1246,4334,9457,5401,1945,9548,8290,1184,3464,132,2458,7704,1056,7554,6203,2270,6070,4889,7369,1676,485,3648,357,1912,9661,4246,1576,1836,4521,7667,6907,2098,8825,7404,4019,8284,3710,7202,7050,9870,3348,3624,9224,6601,7897,6288,3713,932,5596,353,2615,3273,833,1446,8624,2489,3872,486,1091,2493,4157,3611,6570,7107,9153,4543,9504,4746,1342,9737,3247,8984,3640,5698,7814,307,8775,1150,4330,3059,5784,2370,5248,4806,6107,9700,231,3566,5627,3957,5317,5415,8119,2588,9440,2961,9786,4769,466,5411,3080,7623,5031,2378,9286,4801,797,1527,2325,847,6341,5310,1926,9481,2115,2165,5255,5465,5561,3606,7673,7443,7243,8447,2348,7925,6447,8311,6729,4441,7763,8107,267,8135,9194,6775,3883,9639,612,5024,1351,7557,9241,5181,2239,8002,5446,747,166,325,9925,3820,9531,5163,3545,558,7103,7658,5670,8323,4821,6263,7982,59,3700,1082,4474,4353,8637,9558,5191,842,5925,6455,4092,9929,9961,290,3523,6290,7787,8266,7986,7269,6408,3620,406,5964,7289,1620,6726,1257,1993,7006,5545,2913,5093,5066,3019,7081,6760,6779,7061,9051,8852,8118,2340,6596,4594,9708,8430,8659,8920,9268,5431,9203,2823,1427,2203,6422,6193,5214,9566,8791,4964,7575,4350,56,2227,8545,5646,3089,2204,4081,487,8496,2258,4336,6955,3452,556,8602,8251,8569,8636,9430,1025,9459,7137,8392,3553,5945,9414,3078,1688,5480,327,8117,2289,2195,8564,9423,103,7724,3091,8548,7298,5279,6042,2855,3286,3542,9361,420,7020,4112,5320,5366,6379,114,9174,9744,592,5346,3985,3174,5157,9890,1605,3082,8099,4346,7256,8670,5687,6613,6620,1458,1045,7917,2980,2399,1433,3315,4084,178,7056,2132,2728,4421,9195,4181,6017,6229,2945,4627,2809,8816,6737,18,8981,3813,8890,5304,3789,6959,7476,1856,4197,6944,9578,5915,3060,9932,3463,67,7393,9857,5822,3187,501,653,8453,3691,9736,6845,1365,9645,4120,2157,8471,4436,6435,2758,7591,9805,7142,7612,4891,7342,5764,8683,8365,2967,6947,441,2116,6612,1399,7585,972,6548,5481,7733,7209,222,5903,6161,9172,9628,7348,1588,5992,6094,7176,4214,8702,2987,74,8486,9788,7164,5788,8535,8422,6826,1800,8965,4965,565,5609,4686,2556,9324,5000,9809,1994,4737,63,8992,4783,2536,4462,8868,6346,5553,3980,2670,1601,4272,8725,4698,7333,7826,9233,4198,1997,1687,4851,62,7893,8149,8015,341,2230,1280,5559,9756,3761,7834,6805,9287,4622,5748,2320,1958,9129,9649,1644,4323,5096,9490,7529,6444,7478,7044,9525,7713,234,7553,9099,9885,7135,6493,9793,6268,8363,2267,9157,9451,1438,9292,1637,3739,695,1090,4731,4549,5171,5975,7347,5192,5243,1084,2216,9860,3318,5594,5790,1107,220,9397,3378,1353,4498,6497,5442,7929,7377,9541,9871,9895,6742,9146,9409,292,6278,50,5288,2217,4923,6790,4730,9240,3006,3547,9347,7863,4275,3287,2673,7485,1915,9837,2931,3918,635,9131,1197,6250,3853,4303,790,5548,9993,3702,2446,3862,9652,4432,973,41,3507,8585,2444,1633,956,5789,1523,8657,4869,8580,8474,7093,7812,2549,7363,9315,6731,1130,7645,7018,7852,362,1636,2905,8006,4040,6643,8052,7021,3665,8383,715,1876,2783,3065,604,4566,8761,7911,1983,3836,5547,8495,8144,1950,2537,8575,640,8730,8303,1454,8165,6647,4762,909,9449,8640,9253,7293,8767,3004,4623,6862,8994,2520,1215,6299,8414,2576,6148,1510,313,3693,9843,8757,5774,8871,8061,8832,5573,5275,9452,1248,228,9749,2730};

  static void sayCurrentTestName()
   {testNumber++;
    say(String.format("%2d. %s", testNumber, currentTestName()));
   }

  private static void test_put_ascending()
   {z(); sayCurrentTestName();
    final int N = 32;
    final BtreeSF t = BtreeSF(4, 3, N);
    t.P.run(); t.P.clear();
    t.put();
    for(int i = 1; i <= N+N; ++i)
     {//say(currentTestName(), i);
      t.T.at(t.Key).setInt(  i);
      t.T.at(t.Data).setInt( i);
      t.P.run();
     }
    //stop(t);
    t.ok("""
                                                                                                                            32                                                                                                                                           |
                                                                                                                            0                                                                                                                                            |
                                                                                                                            17                                                                                                                                           |
                                                                                                                            16                                                                                                                                           |
                                                      16                                                                                                                                            48                                56                                 |
                                                      17                                                                                                                                            16                                16.1                               |
                                                      5                                                                                                                                             21                                23                                 |
                                                      11                                                                                                                                                                              6                                  |
          4          8               12                               20               24                28                                  36               40                 44                                  52                                  60              |
          5          5.1             5.2                              11               11.1              11.2                                21               21.1               21.2                                23                                  6               |
          1          4               3                                7                10                9                                   13               15                 14                                  18                                  20              |
                                     8                                                                   12                                                                      19                                  22                                  2               |
1,2,3,4=1  5,6,7,8=4    9,10,11,12=3    13,14,15,16=8   17,18,19,20=7   21,22,23,24=10     25,26,27,28=9     29,30,31,32=12   33,34,35,36=13   37,38,39,40=15     41,42,43,44=14     45,46,47,48=19   49,50,51,52=18   53,54,55,56=22     57,58,59,60=20   61,62,63,64=2 |
""");
    // stop("maximumNodes used", t.maxNodeUsed); // 25
   }

  private static void test_put_ascending_wide()
   {z(); sayCurrentTestName();
    final BtreeSF    t = BtreeSF(16, 17, 400);
    t.P.run(); t.P.clear();
    t.put();
    int N = 256, s = 0;
    for(int i = 1; i <= N; ++i)
     {//say(currentTestName(), i);
      t.T.at(t.Key ).setInt(i);
      t.T.at(t.Data).setInt(i);
      t.P.run();
      s += t.P.steps;
     }
    ok(s, 26717);                                                               // vs 210899 = 8 times less instructions

    stop(t);
    t.ok("""
                                         16                                                  32                                                   48                                                   64                                                   80                                                   96                                                                112                                                                  128                                                                   144                                                                   160                                                                   176                                                                    192                                                                    208                                                                    224                                                                    240                                                                   |
                                         0                                                   0.1                                                  0.2                                                  0.3                                                  0.4                                                  0.5                                                               0.6                                                                  0.7                                                                   0.8                                                                   0.9                                                                   0.10                                                                   0.11                                                                   0.12                                                                   0.13                                                                   0.14                                                                  |
                                         1                                                   4                                                    3                                                    5                                                    6                                                    7                                                                 8                                                                    9                                                                     10                                                                    11                                                                    12                                                                     13                                                                     14                                                                     15                                                                     16                                                                    |
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      2                                                                     |
1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16=1   17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32=4    33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48=3    49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64=5    65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80=6    81,82,83,84,85,86,87,88,89,90,91,92,93,94,95,96=7    97,98,99,100,101,102,103,104,105,106,107,108,109,110,111,112=8    113,114,115,116,117,118,119,120,121,122,123,124,125,126,127,128=9    129,130,131,132,133,134,135,136,137,138,139,140,141,142,143,144=10    145,146,147,148,149,150,151,152,153,154,155,156,157,158,159,160=11    161,162,163,164,165,166,167,168,169,170,171,172,173,174,175,176=12     177,178,179,180,181,182,183,184,185,186,187,188,189,190,191,192=13     193,194,195,196,197,198,199,200,201,202,203,204,205,206,207,208=14     209,210,211,212,213,214,215,216,217,218,219,220,221,222,223,224=15     225,226,227,228,229,230,231,232,233,234,235,236,237,238,239,240=16     241,242,243,244,245,246,247,248,249,250,251,252,253,254,255,256=2 |
""");
    // stop("maximumNodes used", t.maxNodeUsed); // 12
   }

  private static void test_put_descending()
   {z(); sayCurrentTestName();
    final BtreeSF t = BtreeSF(2, 3, 60);
    t.P.run(); t.P.clear();
    t.put();
    for(int i = 64; i > 0; --i)
     {//say(currentTestName(), i);
      t.T.at(t.Key).setInt(   i);
      t.T.at(t.Data).setInt(  i);
      t.P.run();
     }
    //t.stop();
    t.ok("""
                                                                                  16                                                                                              32                                                                                                                                                                                          |
                                                                                  0                                                                                               0.1                                                                                                                                                                                         |
                                                                                  39                                                                                              43                                                                                                                                                                                          |
                                                                                                                                                                                  29                                                                                                                                                                                          |
               4                 8                                                                                                24                                                                                               40                                              48                                             56                                          |
               39                39.1                                                                                             43                                                                                               29                                              29.1                                           29.2                                        |
               44                42                                                                                               22                                                                                               23                                              16                                             11                                          |
                                 38                                                                                               28                                                                                                                                                                                              5                                           |
       2                6                    10         12           14                      18         20           22                      26         28           30                       34         36           38                      42         44           46                       50         52          54                      58        60         62         |
       44               42                   38         38.1         38.2                    22         22.1         22.2                    28         28.1         28.2                     23         23.1         23.2                    16         16.1         16.2                     11         11.1        11.2                    5         5.1        5.2        |
       45               41                   40         37           35                      26         33           31                      30         27           25                       14         17           20                      19         15           13                       6          10          8                       4         3          2          |
       9                32                                           34                                              21                                              24                                               18                                              12                                              7                                            1          |
1,2=45   3,4=9   5,6=41   7,8=32     9,10=40   11,12=37     13,14=35     15,16=34   17,18=26   19,20=33     21,22=31     23,24=21   25,26=30   27,28=27     29,30=25     31,32=24    33,34=14   35,36=17     37,38=20     39,40=18   41,42=19   43,44=15     45,46=13     47,48=12     49,50=6   51,52=10     53,54=8     55,56=7     57,58=4   59,60=3    61,62=2    63,64=1 |
""");
    // stop("maximumNodes used", t.maxNodeUsed); // 46
   }

  private static void test_put_small_random()
   {z(); sayCurrentTestName();
    final BtreeSF    t = BtreeSF(6, 3, 100);
    t.P.run(); t.P.clear();
    t.put();
    for(int i = 0; i < random_small.length; i++)
     {//say(currentTestName(), i);
      t.T.at(t.Key).setInt( random_small[i]);
      t.T.at(t.Data).setInt(  i);
      t.P.run();
     }
    //stop(t);
    t.ok("""
                                                                                                                                                                                                                                                      476                                                                                                                                                                                                                                                                                     |
                                                                                                                                                                                                                                                      0                                                                                                                                                                                                                                                                                       |
                                                                                                                                                                                                                                                      16                                                                                                                                                                                                                                                                                      |
                                                                                                                                                                                                                                                      17                                                                                                                                                                                                                                                                                      |
                                                                           160                                                                                              354                                                                                                                                                            582                                                                          781                                                 892                                                               |
                                                                           16                                                                                               16.1                                                                                                                                                           17                                                                           17.1                                                17.2                                                              |
                                                                           27                                                                                               23                                                                                                                                                             20                                                                           9                                                   30                                                                |
                                                                                                                                                                            5                                                                                                                                                                                                                                                                                               6                                                                 |
                 41            81                   120                                                  241               270                            327                                              419                       439                                    502                   535                562                                             654                           688                                             831                                         909                   949                      |
                 27            27.1                 27.2                                                 23                23.1                           23.2                                             5                         5.1                                    20                    20.1               20.2                                            9                             9.1                                             30                                          6                     6.1                      |
                 32            28                   24                                                   25                12                             19                                               11                        21                                     22                    10                 29                                              26                            18                                              14                                          31                    13                       |
                                                    15                                                                                                    4                                                                          1                                                                               7                                                                             3                                               8                                                                 2                        |
1,13,27,29,39=32   43,55,72=28     90,96,103,106=24     135,151,155,157=15    186,188,229,232,234,237=25    246,260,261=12     272,273,279,288,298,317=19     338,344,354=4     358,376,377,391,401,403=11    422,425,436,437,438=21    442,447,472=1    480,490,494,501=22    503,511,516,526=10     545,554,560=29     564,576,577,578=7    586,611,612,615,650=26    657,658,667,679,681,686=18    690,704,769,773=3     804,806,809,826,830=14    839,854,858,882,884=8     903,906,907=31    912,922,937,946=13    961,976,987,989,993=2 |
""");
    //stop("maximumNodes used", t.maxNodeUsed); // 33
   }

  private static void test_put_large_random()
   {z(); sayCurrentTestName();
    final BtreeSF t = BtreeSF(2, 3, 1000);
    t.P.run(); t.P.clear();
    final TreeMap<Integer,Integer> s = new TreeMap<>();

    t.put();
    for (int i = 0; i < random_large.length; ++i)
     {final int r = random_large[i];
      s.put(r, i);
      t.T.at(t.Key).setInt(r); t.T.at(t.Data).setInt(i);
      t.P.run();
     }
    //stop("AAAA", t);
    t.P.clear();
    t.find();
    for (Integer i : s.keySet())
     {t.T.at(t.Key).setInt(i);
      t.P.run();
      ok(t.T.at(t.found).getInt(), 1);
      ok(t.T.at(t.data ).getInt(), s.get(i));

      if (!s.containsKey(i+1))
       {t.T.at(t.Key).setInt(i+1);
        t.P.run();
        ok(t.T.at(t.found).isZero());
       }
      else if (!s.containsKey(i-1))
       {t.T.at(t.Key).setInt(i-1);
        t.P.run();
        ok(t.T.at(t.found).isZero());
       }
     }
   }

  private static void test_find32()
   {z(); sayCurrentTestName();
    final int     N = 32;
    final BtreeSF t = BtreeSF(2, 3, N);
    t.P.run(); t.P.clear();
    t.put();
    for(int i = 1; i <= N; ++i)
     {t.T.at(t.Key).setInt (i);
      t.T.at(t.Data).setInt(i-1);
      t.P.run();
     }

    //stop(t);
    ok(t, """
                                                                            16                                                                                           |
                                                                            0                                                                                            |
                                                                            17                                                                                           |
                                                                            16                                                                                           |
                               8                                                                                            24                    28                     |
                               17                                                                                           16                    16.1                   |
                               5                                                                                            21                    23                     |
                               11                                                                                                                 6                      |
      2      4        6                 10         12          14                      18         20           22                      26                      30        |
      5      5.1      5.2               11         11.1        11.2                    21         21.1         21.2                    23                      6         |
      1      4        3                 7          10          9                       13         15           14                      18                      20        |
                      8                                        12                                              19                      22                      2         |
1,2=1  3,4=4    5,6=3    7,8=8   9,10=7   11,12=10     13,14=9     15,16=12   17,18=13   19,20=15     21,22=14     23,24=19   25,26=18   27,28=22     29,30=20   31,32=2 |
""");

    t.P.clear();
    t.find();

    for(int i = 1; i <= N; ++i)
     {t.T.at(t.Key).setInt(i);
      t.P.run();
      ok(t.T.at(t.found).getInt(), 1);
      ok(t.T.at(t.data) .getInt(), i-1);
     }

    t.T.at(t.Key).setInt(N+1);
    t.P.run();
    ok(t.T.at(t.found).getInt(), 0);
   }

  private static void test_find_and_insert()
   {z(); sayCurrentTestName();
    final int N = 3;
    final BtreeSF t = BtreeSF(2, 3, 10);
    t.P.run(); t.P.clear();
    t.put();
    for(int i = 1; i <= N; ++i)
     {t.T.at(t.Key) .setInt(2*i);
      t.T.at(t.Data).setInt(i);
      t.P.run();
     }

    //stop(T);
    ok(t, """
    3      |
    0      |
    1      |
    2      |
2=1  4,6=2 |
""");

    t.P.clear();
    t.findAndInsert(null);

    final Node node = t.new Node("Node");

    //stop(node.print(1));
    ok(node.print(1), """
MemoryLayout: Node
Memory      : Node
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0       256                                      node
   2 B        0         1                                  1     isLeaf
   3 V        1         4                                  0     free
   4 U        5       150                                        branchOrLeaf
   5 S        5       134                                          leaf
   6 V        5         6                                  1         currentSize
   7 A       11        64          2                                 Keys
   8 V       11        32               0                  2           key
   9 V       43        32               1                  4           key
  10 A       75        64          2                                 Data
  11 V       75        32               0                  1           data
  12 V      107        32               1                  2           data
  13 S        5       150                                          branch
  14 V        5         6                                  1         currentSize
  15 A       11       128          4                                 Keys
  16 V       11        32               0                  2           key
  17 V       43        32               1                  4           key
  18 V       75        32               2                  1           key
  19 V      107        32               3                  2           key
  20 A      139        16          4                                 Data
  21 V      139         4               0                  0           data
  22 V      143         4               1                  0           data
  23 V      147         4               2                  0           data
  24 V      151         4               3                  0           data
  25 V      155       101                                  0     pad
""");

    t.T.at(t.Key) .setInt(2);
    t.T.at(t.Data).setInt(2);
    t.P.run();
    ok(t.T.at(t.success) .getInt(), 1);
    ok(t.T.at(t.found)   .getInt(), 1);
    ok(t.T.at(t.inserted).getInt(), 0);
    //stop(node.print(1));
    ok(node.print(1), """
MemoryLayout: Node
Memory      : Node
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0       256                                      node
   2 B        0         1                                  1     isLeaf
   3 V        1         4                                  0     free
   4 U        5       150                                        branchOrLeaf
   5 S        5       134                                          leaf
   6 V        5         6                                  1         currentSize
   7 A       11        64          2                                 Keys
   8 V       11        32               0                  2           key
   9 V       43        32               1                  4           key
  10 A       75        64          2                                 Data
  11 V       75        32               0                  2           data
  12 V      107        32               1                  2           data
  13 S        5       150                                          branch
  14 V        5         6                                  1         currentSize
  15 A       11       128          4                                 Keys
  16 V       11        32               0                  2           key
  17 V       43        32               1                  4           key
  18 V       75        32               2                  2           key
  19 V      107        32               3                  2           key
  20 A      139        16          4                                 Data
  21 V      139         4               0                  0           data
  22 V      143         4               1                  0           data
  23 V      147         4               2                  0           data
  24 V      151         4               3                  0           data
  25 V      155       101                                  0     pad
""");


    t.T.at(t.Key) .setInt(3);
    t.T.at(t.Data).setInt(3);
    t.P.run();
    ok(t.T.at(t.success) .getInt(), 1);
    ok(t.T.at(t.found)   .getInt(), 0);
    ok(t.T.at(t.inserted).getInt(), 1);
    //stop(node.print(1));
    ok(node.print(1), """
MemoryLayout: Node
Memory      : Node
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0       256                                      node
   2 B        0         1                                  1     isLeaf
   3 V        1         4                                  0     free
   4 U        5       150                                        branchOrLeaf
   5 S        5       134                                          leaf
   6 V        5         6                                  2         currentSize
   7 A       11        64          2                                 Keys
   8 V       11        32               0                  2           key
   9 V       43        32               1                  3           key
  10 A       75        64          2                                 Data
  11 V       75        32               0                  2           data
  12 V      107        32               1                  3           data
  13 S        5       150                                          branch
  14 V        5         6                                  2         currentSize
  15 A       11       128          4                                 Keys
  16 V       11        32               0                  2           key
  17 V       43        32               1                  3           key
  18 V       75        32               2                  2           key
  19 V      107        32               3                  3           key
  20 A      139        16          4                                 Data
  21 V      139         4               0                  0           data
  22 V      143         4               1                  0           data
  23 V      147         4               2                  0           data
  24 V      151         4               3                  0           data
  25 V      155       101                                  0     pad
""");
   }

  private static void test_find_and_update()
   {z(); sayCurrentTestName();
    final int N = 64;
    final BtreeSF t = BtreeSF(8, 3, 48);
    t.P.run(); t.P.clear();
    t.put();
    for(int i = 2; i <= N; i += 2)
     {//say(currentTestName(),  "a", i);
      t.T.at(t.Key ).setInt(i);
      t.T.at(t.Data).setInt(i);
      t.P.run();                                                                // Insert
      t.P.run();                                                                // Update
     }

    //stop(t);
    ok(t, """
                                                  33                                                           |
                                                  0                                                            |
                                                  5                                                            |
                                                  6                                                            |
                      17                                          41              49                           |
                      5                                           6               6.1                          |
                      1                                           3               7                            |
                      4                                                           2                            |
2,4,6,8,10,12,14,16=1   18,20,22,24,26,28,30,32=4   34,36,38,40=3   42,44,46,48=7    50,52,54,56,58,60,62,64=2 |
""");

    t.P.clear();
    t.find();
    for(int i = 1; i <= N+1; i += 2)                                            // Keys that cannot be found
     {//say(currentTestName(), "b", i);
      t.T.at(t.Key).setInt(i);
      t.P.run();
      ok(t.T.at(t.found).getInt(), 0);
     }

    for(int i = 2; i <= N; i += 2)                                              // Keys that can be found
     {//say(currentTestName(),  "c", i);
      t.T.at(t.Key).setInt(i);
      t.P.run();
      ok(t.T.at(t.found).getInt(), 1);
      ok(t.T.at(t.data ).getInt(), i);
     }

    t.P.clear();
    t.put();
    for(int i = 2; i <= N; i += 2)                                              // Update data associated with each key
     {//say(currentTestName(),  "d", i);
      t.T.at(t.Key) .setInt(i);
      t.T.at(t.Data).setInt(i-1);
      t.P.run();
     }

    t.P.clear();
    t.find();
    for(int i = 2; i <= N; i += 2)                                              // Keys that can be found
     {//say(currentTestName(),  "e", i);
      t.T.at(t.Key).setInt(i);
      t.P.run();
      ok(t.T.at(t.found).getInt(), 1);
      t.T.at(t.Key).dec();
      ok(t.T.at(t.data ).getInt(), i-1);
     }
   }

  private static void test_delete_ascending()
   {z(); sayCurrentTestName();
    final BtreeSF t = BtreeSF(4, 3, 48);
    t.P.run(); t.P.clear();

    final int N = 32;
    final boolean box = false;                                                  // Print read me

    t.put();
    for(int i = 1; i <= N; ++i)
     {//say(currentTestName(), "a", i);
      t.T.at(t.Key ).setInt(i);
      t.T.at(t.Data).setInt(i);
      t.P.run();
     }
    //t.stop();
    t.ok("""
                                                      16                               24                               |
                                                      0                                0.1                              |
                                                      5                                11                               |
                                                                                       6                                |
          4          8               12                               20                                28              |
          5          5.1             5.2                              11                                6               |
          1          4               3                                7                                 9               |
                                     8                                10                                2               |
1,2,3,4=1  5,6,7,8=4    9,10,11,12=3    13,14,15,16=8   17,18,19,20=7   21,22,23,24=10    25,26,27,28=9   29,30,31,32=2 |
""");

    if (box) say("At start with", N, "elements", t.printBoxed());

    t.P.clear();
    t.delete();
    for (int i = 1; i <= N; i++)
     {//say(currentTestName(), "b", i);
      t.T.at(t.Key).setInt( i);
      t.P.run();
      //say("        case", i, "-> t.ok(\"\"\"", t, "\"\"\");"); if (true) continue;
      if (box) say("After deleting:", i, t.printBoxed());
      switch(i) {
        case 1 -> t.ok("""
                                                    16                                                                   |
                                                    0                                                                    |
                                                    5                                                                    |
                                                    11                                                                   |
        4          8               12                               20               24                28                |
        5          5.1             5.2                              11               11.1              11.2              |
        1          4               3                                7                10                9                 |
                                   8                                                                   2                 |
2,3,4=1  5,6,7,8=4    9,10,11,12=3    13,14,15,16=8   17,18,19,20=7   21,22,23,24=10     25,26,27,28=9     29,30,31,32=2 |
""");
        case 2 -> t.ok("""
                                                  16                                                                   |
                                                  0                                                                    |
                                                  5                                                                    |
                                                  11                                                                   |
      4          8               12                               20               24                28                |
      5          5.1             5.2                              11               11.1              11.2              |
      1          4               3                                7                10                9                 |
                                 8                                                                   2                 |
3,4=1  5,6,7,8=4    9,10,11,12=3    13,14,15,16=8   17,18,19,20=7   21,22,23,24=10     25,26,27,28=9     29,30,31,32=2 |
""");
        case 3 -> t.ok("""
                                                16                                                                   |
                                                0                                                                    |
                                                5                                                                    |
                                                11                                                                   |
      5        8               12                               20               24                28                |
      5        5.1             5.2                              11               11.1              11.2              |
      1        4               3                                7                10                9                 |
                               8                                                                   2                 |
4,5=1  6,7,8=4    9,10,11,12=3    13,14,15,16=8   17,18,19,20=7   21,22,23,24=10     25,26,27,28=9     29,30,31,32=2 |
""");
        case 4 -> t.ok("""
                                         16                                                                   |
                                         0                                                                    |
                                         5                                                                    |
                                         11                                                                   |
          8             12                               20               24                28                |
          5             5.1                              11               11.1              11.2              |
          1             3                                7                10                9                 |
                        8                                                                   2                 |
5,6,7,8=1  9,10,11,12=3    13,14,15,16=8   17,18,19,20=7   21,22,23,24=10     25,26,27,28=9     29,30,31,32=2 |
""");
        case 5 -> t.ok("""
                                       16                                                                   |
                                       0                                                                    |
                                       5                                                                    |
                                       11                                                                   |
        8             12                               20               24                28                |
        5             5.1                              11               11.1              11.2              |
        1             3                                7                10                9                 |
                      8                                                                   2                 |
6,7,8=1  9,10,11,12=3    13,14,15,16=8   17,18,19,20=7   21,22,23,24=10     25,26,27,28=9     29,30,31,32=2 |
""");
        case 6 -> t.ok("""
                                     16                                                                   |
                                     0                                                                    |
                                     5                                                                    |
                                     11                                                                   |
      8             12                               20               24                28                |
      5             5.1                              11               11.1              11.2              |
      1             3                                7                10                9                 |
                    8                                                                   2                 |
7,8=1  9,10,11,12=3    13,14,15,16=8   17,18,19,20=7   21,22,23,24=10     25,26,27,28=9     29,30,31,32=2 |
""");
        case 7 -> t.ok("""
                                   16                                                                   |
                                   0                                                                    |
                                   5                                                                    |
                                   11                                                                   |
      9           12                               20               24                28                |
      5           5.1                              11               11.1              11.2              |
      1           3                                7                10                9                 |
                  8                                                                   2                 |
8,9=1  10,11,12=3    13,14,15,16=8   17,18,19,20=7   21,22,23,24=10     25,26,27,28=9     29,30,31,32=2 |
""");
        case 8 -> t.ok("""
                             16                                                                   |
                             0                                                                    |
                             5                                                                    |
                             11                                                                   |
             12                              20               24                28                |
             5                               11               11.1              11.2              |
             1                               7                10                9                 |
             8                                                                  2                 |
9,10,11,12=1   13,14,15,16=8   17,18,19,20=7   21,22,23,24=10     25,26,27,28=9     29,30,31,32=2 |
""");
        case 9 -> t.ok("""
                                            20                                                 |
                                            0                                                  |
                                            5                                                  |
                                            11                                                 |
           12              16                                24              28                |
           5               5.1                               11              11.1              |
           1               8                                 10              9                 |
                           7                                                 2                 |
10,11,12=1   13,14,15,16=8    17,18,19,20=7   21,22,23,24=10   25,26,27,28=9     29,30,31,32=2 |
""");
        case 10 -> t.ok("""
                                         20                                                 |
                                         0                                                  |
                                         5                                                  |
                                         11                                                 |
        12              16                                24              28                |
        5               5.1                               11              11.1              |
        1               8                                 10              9                 |
                        7                                                 2                 |
11,12=1   13,14,15,16=8    17,18,19,20=7   21,22,23,24=10   25,26,27,28=9     29,30,31,32=2 |
""");
        case 11 -> t.ok("""
                                      20                                                 |
                                      0                                                  |
                                      5                                                  |
                                      11                                                 |
        13           16                                24              28                |
        5            5.1                               11              11.1              |
        1            8                                 10              9                 |
                     7                                                 2                 |
12,13=1   14,15,16=8    17,18,19,20=7   21,22,23,24=10   25,26,27,28=9     29,30,31,32=2 |
""");
        case 12 -> t.ok("""
                              20                                                 |
                              0                                                  |
                              5                                                  |
                              11                                                 |
              16                               24              28                |
              5                                11              11.1              |
              1                                10              9                 |
              7                                                2                 |
13,14,15,16=1   17,18,19,20=7   21,22,23,24=10   25,26,27,28=9     29,30,31,32=2 |
""");
        case 13 -> t.ok("""
                                             24                              |
                                             0                               |
                                             5                               |
                                             11                              |
           16              20                                28              |
           5               5.1                               11              |
           1               7                                 9               |
                           10                                2               |
14,15,16=1   17,18,19,20=7    21,22,23,24=10   25,26,27,28=9   29,30,31,32=2 |
""");
        case 14 -> t.ok("""
                                          24                              |
                                          0                               |
                                          5                               |
                                          11                              |
        16              20                                28              |
        5               5.1                               11              |
        1               7                                 9               |
                        10                                2               |
15,16=1   17,18,19,20=7    21,22,23,24=10   25,26,27,28=9   29,30,31,32=2 |
""");
        case 15 -> t.ok("""
                                       24                              |
                                       0                               |
                                       5                               |
                                       11                              |
        17           20                                28              |
        5            5.1                               11              |
        1            7                                 9               |
                     10                                2               |
16,17=1   18,19,20=7    21,22,23,24=10   25,26,27,28=9   29,30,31,32=2 |
""");
        case 16 -> t.ok("""
                               24                              |
                               0                               |
                               5                               |
                               11                              |
              20                               28              |
              5                                11              |
              1                                9               |
              10                               2               |
17,18,19,20=1   21,22,23,24=10   25,26,27,28=9   29,30,31,32=2 |
""");
        case 17 -> t.ok("""
           20               24               28               |
           0                0.1              0.2              |
           1                10               9                |
                                             2                |
18,19,20=1   21,22,23,24=10    25,26,27,28=9    29,30,31,32=2 |
""");
        case 18 -> t.ok("""
        20               24               28               |
        0                0.1              0.2              |
        1                10               9                |
                                          2                |
19,20=1   21,22,23,24=10    25,26,27,28=9    29,30,31,32=2 |
""");
        case 19 -> t.ok("""
        21            24               28               |
        0             0.1              0.2              |
        1             10               9                |
                                       2                |
20,21=1   22,23,24=10    25,26,27,28=9    29,30,31,32=2 |
""");
        case 20 -> t.ok("""
              24              28               |
              0               0.1              |
              1               9                |
                              2                |
21,22,23,24=1   25,26,27,28=9    29,30,31,32=2 |
""");
        case 21 -> t.ok("""
           24              28               |
           0               0.1              |
           1               9                |
                           2                |
22,23,24=1   25,26,27,28=9    29,30,31,32=2 |
""");
        case 22 -> t.ok("""
        24              28               |
        0               0.1              |
        1               9                |
                        2                |
23,24=1   25,26,27,28=9    29,30,31,32=2 |
""");
        case 23 -> t.ok("""
        25           28               |
        0            0.1              |
        1            9                |
                     2                |
24,25=1   26,27,28=9    29,30,31,32=2 |
""");
        case 24 -> t.ok("""
              28              |
              0               |
              1               |
              2               |
25,26,27,28=1   29,30,31,32=2 |
""");
        case 25 -> t.ok("""
           28              |
           0               |
           1               |
           2               |
26,27,28=1   29,30,31,32=2 |
""");
        case 26 -> t.ok("""
        28              |
        0               |
        1               |
        2               |
27,28=1   29,30,31,32=2 |
""");
        case 27 -> t.ok("""
        29           |
        0            |
        1            |
        2            |
28,29=1   30,31,32=2 |
""");
        case 28 -> t.ok("""
29,30,31,32=0 |
""");
        case 29 -> t.ok("""
30,31,32=0 |
""");
        case 30 -> t.ok("""
31,32=0 |
""");
        case 31 -> t.ok("""
32=0 |
""");
        case 32 -> t.ok("""
=0 |
""");
       }
     }
   }

  private static void test_delete_descending()
   {z(); sayCurrentTestName();
    final BtreeSF t = BtreeSF(4, 3, 20);
    t.P.run(); t.P.clear();
    final int N = 32;
    final boolean box = false;                                                  // Print read me
    t.put();
    for(int i = 1; i <= N; ++i)
     {//say(currentTestName(),  "a", i);
      t.T.at(t.Key).setInt(  i);
      t.T.at(t.Data).setInt( i);
      t.P.run();

     }
    //t.stop();
    t.ok("""
                                                      16                               24                               |
                                                      0                                0.1                              |
                                                      5                                11                               |
                                                                                       6                                |
          4          8               12                               20                                28              |
          5          5.1             5.2                              11                                6               |
          1          4               3                                7                                 9               |
                                     8                                10                                2               |
1,2,3,4=1  5,6,7,8=4    9,10,11,12=3    13,14,15,16=8   17,18,19,20=7   21,22,23,24=10    25,26,27,28=9   29,30,31,32=2 |
""");

    if (box) say("At start with", N, "elements", t.printBoxed());

    t.P.clear();
    t.delete();
    for (int i = N; i > 0; --i)
     {//say(currentTestName(), "b", i);
      t.T.at(t.Key).setInt( i);
      t.P.run();
      //say("        case", i, "-> t.ok(\"\"\"", t, "\"\"\");"); if (true) continue;
      if (box) say("After deleting:", i, t.printBoxed());
      switch(i) {
        case 32 -> t.ok("""
                                                      16                                                              |
                                                      0                                                               |
                                                      5                                                               |
                                                      6                                                               |
          4          8               12                               20               24               28            |
          5          5.1             5.2                              6                6.1              6.2           |
          1          4               3                                7                10               9             |
                                     8                                                                  2             |
1,2,3,4=1  5,6,7,8=4    9,10,11,12=3    13,14,15,16=8   17,18,19,20=7   21,22,23,24=10    25,26,27,28=9    29,30,31=2 |
""");
        case 31 -> t.ok("""
                                                      16                                                           |
                                                      0                                                            |
                                                      5                                                            |
                                                      6                                                            |
          4          8               12                               20               24               28         |
          5          5.1             5.2                              6                6.1              6.2        |
          1          4               3                                7                10               9          |
                                     8                                                                  2          |
1,2,3,4=1  5,6,7,8=4    9,10,11,12=3    13,14,15,16=8   17,18,19,20=7   21,22,23,24=10    25,26,27,28=9    29,30=2 |
""");
        case 30 -> t.ok("""
                                                      16                                                        |
                                                      0                                                         |
                                                      5                                                         |
                                                      6                                                         |
          4          8               12                               20               24            27         |
          5          5.1             5.2                              6                6.1           6.2        |
          1          4               3                                7                10            9          |
                                     8                                                               2          |
1,2,3,4=1  5,6,7,8=4    9,10,11,12=3    13,14,15,16=8   17,18,19,20=7   21,22,23,24=10    25,26,27=9    28,29=2 |
""");
        case 29 -> t.ok("""
                                                      16                                                |
                                                      0                                                 |
                                                      5                                                 |
                                                      6                                                 |
          4          8               12                               20               24               |
          5          5.1             5.2                              6                6.1              |
          1          4               3                                7                10               |
                                     8                                                 9                |
1,2,3,4=1  5,6,7,8=4    9,10,11,12=3    13,14,15,16=8   17,18,19,20=7   21,22,23,24=10    25,26,27,28=9 |
""");
        case 28 -> t.ok("""
                                                      16                                             |
                                                      0                                              |
                                                      5                                              |
                                                      6                                              |
          4          8               12                               20               24            |
          5          5.1             5.2                              6                6.1           |
          1          4               3                                7                10            |
                                     8                                                 9             |
1,2,3,4=1  5,6,7,8=4    9,10,11,12=3    13,14,15,16=8   17,18,19,20=7   21,22,23,24=10    25,26,27=9 |
""");
        case 27 -> t.ok("""
                                                      16                                          |
                                                      0                                           |
                                                      5                                           |
                                                      6                                           |
          4          8               12                               20               24         |
          5          5.1             5.2                              6                6.1        |
          1          4               3                                7                10         |
                                     8                                                 9          |
1,2,3,4=1  5,6,7,8=4    9,10,11,12=3    13,14,15,16=8   17,18,19,20=7   21,22,23,24=10    25,26=9 |
""");
        case 26 -> t.ok("""
                                                      16                                       |
                                                      0                                        |
                                                      5                                        |
                                                      6                                        |
          4          8               12                               20            23         |
          5          5.1             5.2                              6             6.1        |
          1          4               3                                7             10         |
                                     8                                              9          |
1,2,3,4=1  5,6,7,8=4    9,10,11,12=3    13,14,15,16=8   17,18,19,20=7   21,22,23=10    24,25=9 |
""");
        case 25 -> t.ok("""
                                                      16                               |
                                                      0                                |
                                                      5                                |
                                                      6                                |
          4          8               12                               20               |
          5          5.1             5.2                              6                |
          1          4               3                                7                |
                                     8                                10               |
1,2,3,4=1  5,6,7,8=4    9,10,11,12=3    13,14,15,16=8   17,18,19,20=7   21,22,23,24=10 |
""");
        case 24 -> t.ok("""
                                     12                                             |
                                     0                                              |
                                     5                                              |
                                     6                                              |
          4          8                               16              20             |
          5          5.1                             6               6.1            |
          1          4                               8               7              |
                     3                                               10             |
1,2,3,4=1  5,6,7,8=4    9,10,11,12=3   13,14,15,16=8   17,18,19,20=7    21,22,23=10 |
""");
        case 23 -> t.ok("""
                                     12                                          |
                                     0                                           |
                                     5                                           |
                                     6                                           |
          4          8                               16              20          |
          5          5.1                             6               6.1         |
          1          4                               8               7           |
                     3                                               10          |
1,2,3,4=1  5,6,7,8=4    9,10,11,12=3   13,14,15,16=8   17,18,19,20=7    21,22=10 |
""");
        case 22 -> t.ok("""
                                     12                                       |
                                     0                                        |
                                     5                                        |
                                     6                                        |
          4          8                               16           19          |
          5          5.1                             6            6.1         |
          1          4                               8            7           |
                     3                                            10          |
1,2,3,4=1  5,6,7,8=4    9,10,11,12=3   13,14,15,16=8   17,18,19=7    20,21=10 |
""");
        case 21 -> t.ok("""
                                     12                              |
                                     0                               |
                                     5                               |
                                     6                               |
          4          8                               16              |
          5          5.1                             6               |
          1          4                               8               |
                     3                               7               |
1,2,3,4=1  5,6,7,8=4    9,10,11,12=3   13,14,15,16=8   17,18,19,20=7 |
""");
        case 20 -> t.ok("""
                     8                                           |
                     0                                           |
                     5                                           |
                     6                                           |
          4                        12              16            |
          5                        6               6.1           |
          1                        3               8             |
          4                                        7             |
1,2,3,4=1  5,6,7,8=4  9,10,11,12=3   13,14,15,16=8    17,18,19=7 |
""");
        case 19 -> t.ok("""
                     8                                        |
                     0                                        |
                     5                                        |
                     6                                        |
          4                        12              16         |
          5                        6               6.1        |
          1                        3               8          |
          4                                        7          |
1,2,3,4=1  5,6,7,8=4  9,10,11,12=3   13,14,15,16=8    17,18=7 |
""");
        case 18 -> t.ok("""
                     8                                     |
                     0                                     |
                     5                                     |
                     6                                     |
          4                        12           15         |
          5                        6            6.1        |
          1                        3            8          |
          4                                     7          |
1,2,3,4=1  5,6,7,8=4  9,10,11,12=3   13,14,15=8    16,17=7 |
""");
        case 17 -> t.ok("""
                     8                             |
                     0                             |
                     5                             |
                     6                             |
          4                        12              |
          5                        6               |
          1                        3               |
          4                        8               |
1,2,3,4=1  5,6,7,8=4  9,10,11,12=3   13,14,15,16=8 |
""");
        case 16 -> t.ok("""
          4          8               12            |
          0          0.1             0.2           |
          1          4               3             |
                                     8             |
1,2,3,4=1  5,6,7,8=4    9,10,11,12=3    13,14,15=8 |
""");
        case 15 -> t.ok("""
          4          8               12         |
          0          0.1             0.2        |
          1          4               3          |
                                     8          |
1,2,3,4=1  5,6,7,8=4    9,10,11,12=3    13,14=8 |
""");
        case 14 -> t.ok("""
          4          8            11         |
          0          0.1          0.2        |
          1          4            3          |
                                  8          |
1,2,3,4=1  5,6,7,8=4    9,10,11=3    12,13=8 |
""");
        case 13 -> t.ok("""
          4          8               |
          0          0.1             |
          1          4               |
                     3               |
1,2,3,4=1  5,6,7,8=4    9,10,11,12=3 |
""");
        case 12 -> t.ok("""
          4          8            |
          0          0.1          |
          1          4            |
                     3            |
1,2,3,4=1  5,6,7,8=4    9,10,11=3 |
""");
        case 11 -> t.ok("""
          4          8         |
          0          0.1       |
          1          4         |
                     3         |
1,2,3,4=1  5,6,7,8=4    9,10=3 |
""");
        case 10 -> t.ok("""
          4        7        |
          0        0.1      |
          1        4        |
                   3        |
1,2,3,4=1  5,6,7=4    8,9=3 |
""");
        case 9 -> t.ok("""
          4          |
          0          |
          1          |
          4          |
1,2,3,4=1  5,6,7,8=4 |
""");
        case 8 -> t.ok("""
          4        |
          0        |
          1        |
          4        |
1,2,3,4=1  5,6,7=4 |
""");
        case 7 -> t.ok("""
          4      |
          0      |
          1      |
          4      |
1,2,3,4=1  5,6=4 |
""");
        case 6 -> t.ok("""
        3      |
        0      |
        1      |
        4      |
1,2,3=1  4,5=4 |
""");
        case 5 -> t.ok("""
1,2,3,4=0 |
""");
        case 4 -> t.ok("""
1,2,3=0 |
""");
        case 3 -> t.ok("""
1,2=0 |
""");
        case 2 -> t.ok("""
1=0 |
""");
        case 1 -> t.ok("""
=0 |
""");
       }
     }
   }

  private static void test_delete_random(int[]random_array, int maxSize, String expected)
   {z();
    final BtreeSF t = BtreeSF(4, 3, maxSize);
    t.P.run(); t.P.clear();
    t.put();
    final int N = random_array.length;

    for (int i = 0; i < N; ++i)                                                 // Build tree
     {//say(currentTestName(),  "a", i);
      t.T.at(t.Key ).setInt(random_array[i]);
      t.T.at(t.Data).setInt(i);
      t.P.run();
     }

    if (expected != null) t.ok(expected);

    t.P.clear();
    t.delete();
    for (int i = 0; i < N; ++i)
     {//say(currentTestName(),  "b", i);
      t.T.at(t.Key ).setInt(random_array[i]);                                   // Delete
      t.P.run();
      ok(t.T.at(t.deleted).getInt(), 1);
      ok(t.T.at(t.Data)   .getInt(), i);

      t.T.at(t.Key).setInt(random_array[i]);                                    // Delete an element that should no longer be there
      t.P.run();
      ok(t.T.at(t.deleted).getInt(), 0);
     }
    t.ok("""
=0 |
""");
   }

  private static void test_delete_small_random()
   {z(); sayCurrentTestName();
    test_delete_random(random_small, 50, """
                                                                                                                                                                                                                        379                                                                                                                    528                                                                                                                                                                                                                                                                  |
                                                                                                                                                                                                                        0                                                                                                                      0.1                                                                                                                                                                                                                                                                  |
                                                                                                                                                                                                                        48                                                                                                                     43                                                                                                                                                                                                                                                                   |
                                                                                                                                                                                                                                                                                                                                               41                                                                                                                                                                                                                                                                   |
                                                               143                                                              253                                                     341                                                             429                                                   497                                                                     582                                                                          718                                                                      894                                                                     |
                                                               48                                                               48.1                                                    48.2                                                            43                                                    43.1                                                                    41                                                                           41.1                                                                     41.2                                                                    |
                                                               36                                                               25                                                      46                                                              14                                                    44                                                                      21                                                                           11                                                                       3                                                                       |
                                                                                                                                                                                        10                                                                                                                    5                                                                                                                                                                                                                             6                                                                       |
              34               87               104                          156               210                235                          266               283                                   356                            402                                 440          457                                   507                                     568                                    630                   672                688                              805           819                   856                                  909                   946               988          |
              36               36.1             36.2                         25                25.1               25.2                         46                46.1                                  10                             14                                  44           44.1                                  5                                       21                                     11                    11.1               11.2                             3             3.1                   3.2                                  6                     6.1               6.2          |
              37               24               17                           49                26                 38                           47                30                                    39                             15                                  45           8                                     33                                      18                                     27                    13                 42                               29            34                    32                                   40                    12                35           |
                                                16                                                                9                                              19                                    1                              20                                               28                                    22                                      7                                                                               4                                                                    23                                                                           2            |
1,13,27,29=37   39,43,55,72=24     90,96,103=17     106,135=16    151,155=49    157,186,188=26     229,232,234=38     237,246=9     260,261=47    272,273,279=30     288,298,317,338=19     344,354=39    358,376,377=1    391,401=15    403,422,425=20    436,437,438=45    442,447=8     472,480,490,494=28     501,503=33    511,516,526=22    545,554,560,564=18    576,577,578=7    586,611,612,615=27    650,657,658,667=13     679,681,686=42     690,704=4     769,773,804=29    806,809=34    826,830,839,854=32    858,882,884=23     903,906,907=40    912,922,937,946=12    961,976,987=35    989,993=2 |
""");
   }

  private static void test_delete_large_random()
   {z(); sayCurrentTestName();
    test_delete_random(random_large, 1000, null);
   }

  void run_verilogFind(int Key, int Found, int Data, int ExpSteps)              // Test a find operation in Verilog
   {VerilogCode v = new VerilogCode(currentTestNameSuffix(), verilogFolder)     // Generate verilog now that memories have been initialized and the program written
     {int     Key     () {return      Key;}                                     // Input key value
      Integer Data    () {return     null;}                                     // Input data value
      Integer found   () {return    Found;}                                     // Whether we should expect to find the key on a find operation
      Integer data    () {return     Data;}                                     // Expected output data value
      int     maxSteps() {return       40;}                                     // Maximum number of execution steps
      int     expSteps() {return ExpSteps;}                                     // Expected number of steps
      String  expected() {return null;}                                         // Expected tree if present
     };
   }

  private static void test_first_last()                                         // First/last node of a tree
   {z(); sayCurrentTestName();
    final BtreeSF t = allTreeOps() ;
    t.P.run(); t.P.clear();
    t.put();
    final int N = 9;
    for (int i = 1; i <= N; ++i)
     {//say(currentTestName(),  "a", i);
      t.T.at(t.Key ).setInt(i);
      t.T.at(t.Data).setInt(N-i);
      t.P.run();
     }
    //stop(t.M);
    //stop(t);
    ok(t, """
             4                    |
             0                    |
             5                    |
             6                    |
      2             6    7        |
      5             6    6.1      |
      1             3    8        |
      4                  2        |
1,2=1  3,4=4  5,6=3  7=8    8,9=2 |
""");
    t.P.clear();
    t.first();
    t.P.run();
    //stop(t.T.at(t.leafFound));
    ok(t.T.at(t.leafFound), "T.leafFound@138=1");

    t.P.clear();
    t.last();
    t.P.run();
    //stop(t.T.at(t.leafFound));
    ok(t.T.at(t.leafFound), "T.leafFound@138=2");
   }

  private static void test_find_verilog()                                               // Find using generated verilog code
   {z(); sayCurrentTestName();
    final BtreeSF t = allTreeOps() ;
    t.P.run(); t.P.clear();
    t.put();
    final int N = 9;
    for (int i = 1; i <= N; ++i)
     {//say(currentTestName(),  "a", i);
      t.T.at(t.Key ).setInt(i);
      t.T.at(t.Data).setInt(N-i);
      t.P.run();
     }
    //stop(t.M);
    //stop(t);
    ok(t, """
             4                    |
             0                    |
             5                    |
             6                    |
      2             6    7        |
      5             6    6.1      |
      1             3    8        |
      4                  2        |
1,2=1  3,4=4  5,6=3  7=8    8,9=2 |
""");
    t.P.clear(); t.T.clear();                                                   // Clear program and transaction memory
    t.T.at(t.Key).setInt(2);                                                    // Sets memory directly not via an instruction
    t.find();

    t.run_verilogFind( 0, 0, 0, 28);
    t.run_verilogFind( 1, 1, 8, 28);
    t.run_verilogFind( 2, 1, 7, 28);
    t.run_verilogFind( 3, 1, 6, 28);
    t.run_verilogFind( 4, 1, 5, 28);
    t.run_verilogFind( 5, 1, 4, 28);
    t.run_verilogFind( 6, 1, 3, 28);
    t.run_verilogFind( 7, 1, 2, 28);
    t.run_verilogFind( 8, 1, 1, 28);
    t.run_verilogFind( 9, 1, 0, 28);
    t.run_verilogFind(10, 0, 0, 28);
   }

  private void runVerilogDeleteTest                                             // Run the java and verilog versions and compare the resulting memory traces
   (int Key, int data, int steps, String expected)
   {z();

    VerilogCode v = new VerilogCode(currentTestNameSuffix(), "verilog")         // Generate verilog now that memories have beeninitialzied and the program written
     {int     Key     () {return   Key;}                                        // Input key value
      Integer Data    () {return     3;}                                        // Input key value
      Integer found   () {return     1;}                                        // Whether we should expect to find the key on a find operation
      Integer data    () {return  data;}                                        // Expected output data value
      int     maxSteps() {return  2000;}                                        // Maximum number if execution steps
      int     expSteps() {return steps;}                                        // Expected number of steps
      String  expected() {return expected;}                                     // Expected tree if present
     };
   }

  private static void test_delete_verilog()                                     // Delete using generated verilog code
   {z(); sayCurrentTestName();
    final BtreeSF t = allTreeOps();
    t.P.run(); t.P.clear();
    t.put();
    final int N = 9;
    for (int i = 1; i <= N; ++i)
     {//say(currentTestName(),  "a", i);
      t.T.at(t.Key ).setInt(i);
      t.T.at(t.Data).setInt(N-i);
      t.P.run();
     }
    //stop(t.M);
    //stop(t);
    ok(t, """
             4                    |
             0                    |
             5                    |
             6                    |
      2             6    7        |
      5             6    6.1      |
      1             3    8        |
      4                  2        |
1,2=1  3,4=4  5,6=3  7=8    8,9=2 |
""");

    t.P.clear();                                                                // Replace program with delete
    t.delete();                                                                 // Delete code

    t.runVerilogDeleteTest(3, 6, 548, """
                    6           |
                    0           |
                    5           |
                    6           |
      2    4             7      |
      5    5.1           6      |
      1    4             8      |
           3             2      |
1,2=1  4=4    5,6=3  7=8  8,9=2 |
""");

    t.runVerilogDeleteTest(4, 5, 387, """
             6           |
             0           |
             5           |
             6           |
      4           7      |
      5           6      |
      1           8      |
      3           2      |
1,2=1  5,6=3  7=8  8,9=2 |
""");

    t.runVerilogDeleteTest(2, 7, 439, """
    4      6      7        |
    0      0.1    0.2      |
    1      3      8        |
                  2        |
1=1  5,6=3    7=8    8,9=2 |
""");

    t.runVerilogDeleteTest(1, 8, 320, """
      6    7        |
      0    0.1      |
      1    8        |
           2        |
5,6=1  7=8    8,9=2 |
""");

    t.runVerilogDeleteTest(5, 4, 264, """
      7      |
      0      |
      1      |
      2      |
6,7=1  8,9=2 |
""");

    t.runVerilogDeleteTest(6, 3, 273, """
    7      |
    0      |
    1      |
    2      |
7=1  8,9=2 |
""");

    t.runVerilogDeleteTest(7, 2, 239, """
8,9=0 |
""");

    t.runVerilogDeleteTest(8, 1, 34, """
9=0 |
""");

    t.runVerilogDeleteTest(9, 0, 34, """
=0 |
""");
   }

  private void runVerilogPutTest                                                // Run the java and verilog versions and compare the resulting memory traces
   (int value, int steps, String expected)
   {z();

    VerilogCode v = new VerilogCode(currentTestNameSuffix(), "verilog")         // Generate verilog now that memories have been initialized and the program written
     {int     Key     () {return value;}                                        // Input key value
      Integer Data    () {return value;}                                        // Input data value
      Integer found   () {return     0;}                                        // Whether we should expect to find the key on a find operation
      Integer data    () {return     0;}                                        // Expected output data value
      int     maxSteps() {return  2000;}                                        // Maximum number if execution steps
      int     expSteps() {return steps;}                                        // Expected number of steps
      String  expected() {return expected;}                                     // Expected tree if present
      int      density() {return 10;}                                           // Lower than normal density to facilitate routing
     };
   }

  private static void test_put_verilog()                                        // Delete using generated verilog code
   {z(); sayCurrentTestName();
    final BtreeSF t = allTreeOps();
    t.P.run(); t.P.clear();
    t.put();
    t.runVerilogPutTest(1, 30, """
1=0 |
""");

    t.runVerilogPutTest(2, 30, """
1,2=0 |
""");
                                                                                // Split instruction
    t.runVerilogPutTest(3, 150, """
    1      |
    0      |
    1      |
    2      |
1=1  2,3=2 |
""");

    t.runVerilogPutTest(4, 275, """
    1    2        |
    0    0.1      |
    1    3        |
         2        |
1=1  2=3    3,4=2 |
""");

    t.runVerilogPutTest(5, 335, """
      2    3        |
      0    0.1      |
      1    4        |
           2        |
1,2=1  3=4    4,5=2 |
""");

    t.runVerilogPutTest(6, 335, """
      2      4        |
      0      0.1      |
      1      4        |
             2        |
1,2=1  3,4=4    5,6=2 |
""");

    t.runVerilogPutTest(7, 368, """
      2      4      5        |
      0      0.1    0.2      |
      1      4      3        |
                    2        |
1,2=1  3,4=4    5=3    6,7=2 |
""");

    t.runVerilogPutTest(8, 495, """
             4                  |
             0                  |
             5                  |
             6                  |
      2           5    6        |
      5           6    6.1      |
      1           3    7        |
      4                2        |
1,2=1  3,4=4  5=3  6=7    7,8=2 |
""");

    t.runVerilogPutTest(9, 461, """
             4                    |
             0                    |
             5                    |
             6                    |
      2             6    7        |
      5             6    6.1      |
      1             3    8        |
      4                  2        |
1,2=1  3,4=4  5,6=3  7=8    8,9=2 |
""");

    t.runVerilogPutTest(10, 461, """
             4                       |
             0                       |
             5                       |
             6                       |
      2             6      8         |
      5             6      6.1       |
      1             3      8         |
      4                    2         |
1,2=1  3,4=4  5,6=3  7,8=8    9,10=2 |
""");

    t.runVerilogPutTest(11, 494, """
             4                               |
             0                               |
             5                               |
             6                               |
      2             6      8      9          |
      5             6      6.1    6.2        |
      1             3      8      7          |
      4                           2          |
1,2=1  3,4=4  5,6=3  7,8=8    9=7    10,11=2 |
""");

    t.runVerilogPutTest(12, 482, """
                               8                 |
                               0                 |
                               5                 |
                               6                 |
      2      4        6                10        |
      5      5.1      5.2              6         |
      1      4        3                7         |
                      8                2         |
1,2=1  3,4=4    5,6=3    7,8=8  9,10=7   11,12=2 |
""");

    t.runVerilogPutTest(13, 424, """
                               8                          |
                               0                          |
                               5                          |
                               6                          |
      2      4        6                10      11         |
      5      5.1      5.2              6       6.1        |
      1      4        3                7       10         |
                      8                        2          |
1,2=1  3,4=4    5,6=3    7,8=8  9,10=7   11=10    12,13=2 |
""");

    t.runVerilogPutTest(14, 461, """
                               8                             |
                               0                             |
                               5                             |
                               6                             |
      2      4        6                10         12         |
      5      5.1      5.2              6          6.1        |
      1      4        3                7          10         |
                      8                           2          |
1,2=1  3,4=4    5,6=3    7,8=8  9,10=7   11,12=10    13,14=2 |
""");

    t.runVerilogPutTest(15, 494, """
                               8                                     |
                               0                                     |
                               5                                     |
                               6                                     |
      2      4        6                10         12      13         |
      5      5.1      5.2              6          6.1     6.2        |
      1      4        3                7          10      9          |
                      8                                   2          |
1,2=1  3,4=4    5,6=3    7,8=8  9,10=7   11,12=10    13=9    14,15=2 |
""");

    t.runVerilogPutTest(16, 509, """
                               8                  12                   |
                               0                  0.1                  |
                               5                  11                   |
                                                  6                    |
      2      4        6                10                    14        |
      5      5.1      5.2              11                    6         |
      1      4        3                7                     9         |
                      8                10                    2         |
1,2=1  3,4=4    5,6=3    7,8=8  9,10=7   11,12=10    13,14=9   15,16=2 |
""");

    t.runVerilogPutTest(17, 472, """
                               8                  12                            |
                               0                  0.1                           |
                               5                  11                            |
                                                  6                             |
      2      4        6                10                    14      15         |
      5      5.1      5.2              11                    6       6.1        |
      1      4        3                7                     9       12         |
                      8                10                            2          |
1,2=1  3,4=4    5,6=3    7,8=8  9,10=7   11,12=10    13,14=9   15=12    16,17=2 |
""");

    t.runVerilogPutTest(18, 509, """
                               8                  12                               |
                               0                  0.1                              |
                               5                  11                               |
                                                  6                                |
      2      4        6                10                    14         16         |
      5      5.1      5.2              11                    6          6.1        |
      1      4        3                7                     9          12         |
                      8                10                               2          |
1,2=1  3,4=4    5,6=3    7,8=8  9,10=7   11,12=10    13,14=9   15,16=12    17,18=2 |
""");

    t.runVerilogPutTest(19, 542, """
                               8                  12                                        |
                               0                  0.1                                       |
                               5                  11                                        |
                                                  6                                         |
      2      4        6                10                    14         16       17         |
      5      5.1      5.2              11                    6          6.1      6.2        |
      1      4        3                7                     9          12       13         |
                      8                10                                        2          |
1,2=1  3,4=4    5,6=3    7,8=8  9,10=7   11,12=10    13,14=9   15,16=12    17=13    18,19=2 |
""");

    t.runVerilogPutTest(20, 552, """
                               8                                           16                    |
                               0                                           0.1                   |
                               5                                           11                    |
                                                                           6                     |
      2      4        6                10         12          14                       18        |
      5      5.1      5.2              11         11.1        11.2                     6         |
      1      4        3                7          10          9                        13        |
                      8                                       12                       2         |
1,2=1  3,4=4    5,6=3    7,8=8  9,10=7   11,12=10     13,14=9     15,16=12    17,18=13   19,20=2 |
""");
   }

  static void test_delete_random_not_100()
   {z(); sayCurrentTestName();
    final BtreeSF t = BtreeSF(2, 3, 999);
    t.P.run(); t.P.clear();
    t.put();

    int N = random_large.length;
    for (int i = 0; i < N; ++i)
     {final int r = random_large[i];
      t.T.at(t.Key).setInt(r); t.T.at(t.Data).setInt(i);
      t.P.run();
     }
    t.P.run(); t.P.clear();
    t.delete();
    for (int i = 0; i < N; ++i)
     {final int r = random_large[i];
      if (r % 100 > 0)
       {t.T.at(t.Key).setInt(r);
        t.P.run();
       }
     }
    stop(t);
    ok(t, """
             2965              6444              |
             0                 0.1               |
             223               589               |
                               430               |
700,1800=223     3700,5000=589     6600,9700=430 |
""");
   }

  static void test_primes()
   {final BtreeSF t = BtreeSF(2, 3, 100);
    t.P.run(); t.P.clear();
    t.put();

    int N = 64;
    for (int i = 1; i <= N; i++)
     {t.T.at(t.Key ).setInt(i);
      t.T.at(t.Data).setInt(i);
      t.P.run();
     }
    for (int i = 2; i <= N; i++)
     {t.P.clear(); t.find();
      t.T.at(t.Key ).setInt(i);
      t.P.run();
      if (t.T.at(t.found).getInt() > 0)
       {t.P.clear(); t.delete();
        for (int j = 2*i; j <= N; j += i)
         {t.T.at(t.Key ).setInt(j);
          t.P.run();
         }
       }
     }
    //stop(t);
    ok(t, """
             6                        17                               40                                 |
             0                        0.1                              0.2                                |
             5                        11                               23                                 |
                                                                       6                                  |
      2           8         16                 19         29                       43         53          |
      5           11        11.1               23         23.1                     6          6.1         |
      1           8         7                  14         18                       29         35          |
      4                     13                            26                                  16          |
1,2=1  3,5=4  7=8   11,13=7     17=13    19=14   23,29=18     31,37=26    41,43=29   47,53=35    59,61=16 |
""");
   }

  private static void test_node()
   {z();
    final int N = 3;
    final BtreeSF t = new BtreeSF()
     {int maxSize         () {return  3;}
      int maxKeysPerLeaf  () {return  2;}
      int maxKeysPerBranch() {return  3;}
      int bitsPerKey      () {return  4;}
      int bitsPerData     () {return  4;}
     };
    t.P.run(); t.P.clear();
    t.put();
    for(int i = 1; i <= N; ++i)
     {t.T.at(t.Key).setInt (i);
      t.T.at(t.Data).setInt(i);
      t.P.run();
     }

    //stop(t);
    ok(t, """
    1      |
    0      |
    1      |
    2      |
1=1  2,3=2 |
""");

    t.P.clear();

    StuckDM l = new StuckDM("leaf")
     {int     maxSize() {return t.maxKeysPerLeaf();}
      int  bitsPerKey() {return t.bitsPerKey();}
      int bitsPerData() {return t.bitsPerData();}
      int bitsPerSize() {return t.bitsPerSize;}
     };
    l.program(t.P);

    StuckDM b = new StuckDM("branch")
     {int     maxSize() {return t.maxKeysPerBranch()+1;}
      int  bitsPerKey() {return t.bitsPerKey();}
      int bitsPerData() {return t.bitsPerNext;}
      int bitsPerSize() {return t.bitsPerSize;}
     };
    b.program(t.P);

    final Layout ni = new Layout();
    Layout.Variable  one = ni.variable ("one", t.bitsPerNext);
    Layout.Variable  two = ni.variable ("two", t.bitsPerNext);
    Layout.Structure str = ni.structure("str", one, two);
    final MemoryLayoutDM nim = new MemoryLayoutDM(ni.compile(), "ni");
    nim.at(one).setInt(1);
    nim.at(two).setInt(2);

    Node n = t.new Node("node");
    n.loadRoot();
    b.clear();
    n.loadStuck(b);
    t.P.run(); t.P.clear();
    //stop(b);
    ok(b, """
StuckSML(maxSize:4 size:2)
  0 key:1 data:1
  1 key:0 data:2
""");

    n.loadNode(nim.at(one));
    l.clear();
    n.loadStuck(l);
    t.P.run(); t.P.clear();
    //stop(l);
    ok(l, """
StuckSML(maxSize:2 size:1)
  0 key:1 data:1
""");

    n.loadNode(nim.at(two));
    l.clear();
    n.loadStuck(l);
    t.P.run(); t.P.clear();
    //stop(l);
    ok(l, """
StuckSML(maxSize:2 size:2)
  0 key:2 data:2
  1 key:3 data:3
""");

    t.T.at(t.l).setInt(1);
    l.clear();
    n.loadStuck(l, t.l);
    t.P.run(); t.P.clear();
    //stop(l);
    ok(l, """
StuckSML(maxSize:2 size:1)
  0 key:1 data:1
""");

    t.T.at(t.l).setInt(2);
    l.clear();
    n.loadStuck(l, t.l);
    t.P.run(); t.P.clear();
    //stop(l);
    ok(l, """
StuckSML(maxSize:2 size:2)
  0 key:2 data:2
  1 key:3 data:3
""");

    t.T.at(t.l).setInt(0);
    b.clear();
    n.loadBranchStuckAndSize(b, t.l, t.index);
    n.isFull();
    t.P.run(); t.P.clear();
    ok(t.T.at(t.index),  "T.index@56=1");
    ok(t.T.at(t.isFull), "T.mergeable@71=0");
    //stop(b);
    ok(b, """
StuckSML(maxSize:4 size:2)
  0 key:1 data:1
  1 key:0 data:2
""");

    t.T.at(t.l).setInt(1);
    l.clear();
    n.loadLeafStuckAndSize(l, t.l, t.index);
    n.isFull();
    t.P.run(); t.P.clear();
    ok(t.T.at(t.index),  "T.index@56=1");
    ok(t.T.at(t.isFull), "T.mergeable@71=0");
    //stop(l);
    ok(l, """
StuckSML(maxSize:2 size:1)
  0 key:1 data:1
""");

    t.T.at(t.isFull).ones();
    n.isLeafFull();
    t.P.run(); t.P.clear();
    ok(t.T.at(t.isFull), "T.mergeable@71=0");

    t.T.at(t.l).setInt(2);
    l.clear();
    n.loadLeafStuckAndSize(l, t.l, t.index);
    n.isFull();
    t.P.run(); t.P.clear();
    ok(t.T.at(t.index),  "T.index@56=2");
    ok(t.T.at(t.isFull), "T.mergeable@71=1");
    //stop(l);
    ok(l, """
StuckSML(maxSize:2 size:2)
  0 key:2 data:2
  1 key:3 data:3
""");

    t.T.at(t.isFull).zero();
    n.isLeafFull();
    t.P.run(); t.P.clear();
    ok(t.T.at(t.isFull), "T.mergeable@71=1");

    l.clear();
    t.new Node("root").loadRootStuck(t.bT);
    t.P.run(); t.P.clear();
    //stop(t.bT);
    ok(t.bT, """
StuckSML(maxSize:4 size:2)
  0 key:1 data:1
  1 key:0 data:2
""");

    b.pop();
    n.setLeaf();
    n.saveStuck(b);
    n.saveRoot();
    t.P.run(); t.P.clear();
    //stop(b);
    ok(b, """
StuckSML(maxSize:4 size:1)
  0 key:1 data:1
""");

    n.loadNode(nim.at(one));
    n.loadStuck(l);
    t.P.run(); t.P.clear();
    //stop(l);

    ok(l, """
StuckSML(maxSize:2 size:1)
  0 key:1 data:1
""");

    n.loadNode(nim.at(two));
    n.loadStuck(l);
    t.P.run(); t.P.clear();
    //stop(l);

    ok(l, """
StuckSML(maxSize:2 size:2)
  0 key:2 data:2
  1 key:3 data:3
""");

    final Layout                    L = new Layout();
    final Layout.Variable   index = L.variable("index", 2);
    final Layout.Bit    branchBit = L.bit("branchBit");
    final Layout.Bit      leafBit = L.bit("leafBit");
    final Layout.Structure struct = L.structure("struct", index, branchBit, leafBit);
    final MemoryLayoutDM        M = new MemoryLayoutDM(L.compile(), "MM");
    M.program(t.P);
    M.at(index).setInt(2);

    l.pop();
    n.isLeaf(M.at(leafBit));
    n.setBranch();
    n.isLeaf(M.at(branchBit));
    n.saveStuck(l);
    n.saveNode(M.at(index));
    t.P.run(); t.P.clear();
    //stop(l);

    ok(l, """
StuckSML(maxSize:2 size:1)
  0 key:2 data:2
""");

    //stop(M);
    ok(""+M, """
MemoryLayout: MM
Memory      : MM
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0         4                                      struct
   2 V        0         2                                  2     index
   3 B        2         1                                  0     branchBit
   4 B        3         1                                  1     leafBit
""");

    n.loadRootStuck(l);
    t.P.run(); t.P.clear();
    l.T.setIntInstruction(l.tKey , 2);
    l.T.setIntInstruction(l.tData, 2);
    l.push();
    l.P.run(); l.P.clear();
    //stop(l);
    ok(l, """
StuckSML(maxSize:2 size:2)
  0 key:1 data:0
  1 key:2 data:2
""");
    //stop(b);
    ok(b, """
StuckSML(maxSize:4 size:1)
  0 key:1 data:1
""");
   }

//  private static void test_memory()
//   {n.saveRootStuck(l);
//    t.T.setIntInstruction(t.memoryIn, 1);
//    n.saveStuck(l, t.memoryIn);
//    t.T.setIntInstruction(t.memoryIn, 2);
//    n.saveStuck(b, t.memoryIn);
//    t.P.run(); t.P.clear();
//
//    l.M.zero(); b.M.zero();
//    //stop(l);
//    ok(l, """
//StuckSML(maxSize:2 size:0)
//""");
//    //stop(b);
//    ok(b, """
//StuckSML(maxSize:4 size:0)
//""");
//
//    t.T.setIntInstruction(t.memoryOut, 1);
//    n.loadStuck(l, t.memoryOut);
//    t.T.setIntInstruction(t.memoryOut, 2);
//    n.loadStuck(b, t.memoryOut);
//    t.P.run(); t.P.clear();
//
//    //stop(l);
//    ok(l, """
//StuckSML(maxSize:2 size:2)
//  0 key:1 data:0
//  1 key:2 data:2
//""");
//    //stop(b);
//    ok(b, """
//StuckSML(maxSize:4 size:1)
//  0 key:1 data:1
//""");
//   }

  private static void test_memory()
   {z(); sayCurrentTestName();
    final BtreeSF t = BtreeSF(2, 3, 4);
    t.P.run(); t.P.clear();
    ok(t.F.at(t.freeChainHead), "F.freeList@0=1");
    ok(t.M.at(t.bTree_free, 0), "M.free[0]1=0");
    ok(t.M.at(t.bTree_free, 1), "M.free[1]257=2");
    ok(t.M.at(t.bTree_free, 2), "M.free[2]513=3");
    ok(t.M.at(t.bTree_free, 3), "M.free[3]769=0");

    t.allocLeaf();
    t.P.run(); t.P.clear();

    ok(t.F.at(t.freeChainHead), "F.freeList@0=2");
    ok(t.M.at(t.bTree_free, 0), "M.free[0]1=0");
    ok(t.M.at(t.bTree_free, 1), "M.free[1]257=0");
    ok(t.M.at(t.bTree_free, 2), "M.free[2]513=3");
    ok(t.M.at(t.bTree_free, 3), "M.free[3]769=0");
    ok(t.T.at(t.allocate),      "T.allocate@0=1");

    t.free(t.allocate);
    t.P.run(); t.P.clear();
    ok(t.F.at(t.freeChainHead), "F.freeList@0=1");
    ok(t.M.at(t.bTree_free, 0), "M.free[0]1=0");
    ok(t.M.at(t.bTree_free, 1), "M.free[1]257=2");
    ok(t.M.at(t.bTree_free, 2), "M.free[2]513=3");
    ok(t.M.at(t.bTree_free, 3), "M.free[3]769=0");
   }

  private static void test_find_wide()                                               // Find using generated verilog code
   {z(); sayCurrentTestName();
    final BtreeSF t = wideTree();
    t.P.run(); t.P.clear();
    t.put();
    final int N = 32;
    for (int i = 1; i <= N; ++i)
     {t.T.at(t.Key ).setInt(i);
      t.T.at(t.Data).setInt(N-i);
      t.P.run();
     }
    //stop(t.M);
    //stop(t);
    ok(t, """
                  8                         16                           24                           |
                  0                         0.1                          0.2                          |
                  1                         4                            3                            |
                                                                         2                            |
1,2,3,4,5,6,7,8=1  9,10,11,12,13,14,15,16=4    17,18,19,20,21,22,23,24=3    25,26,27,28,29,30,31,32=2 |
""");
      t.P.clear(); t.T.clear();                                                 // Clear program and transaction memory
      t.T.at(t.Key).setInt(2);                                                  // Sets memory directly not via an instruction
      t.find();

    t.run_verilogFind(  0, 0,  0, 18);
    t.run_verilogFind(  1, 1, 31, 18);
    t.run_verilogFind(  2, 1, 30, 18);
    t.run_verilogFind(  3, 1, 29, 18);
    t.run_verilogFind(  4, 1, 28, 18);
    t.run_verilogFind(  5, 1, 27, 18);
    t.run_verilogFind(  6, 1, 26, 18);
    t.run_verilogFind(  7, 1, 25, 18);
    t.run_verilogFind(  8, 1, 24, 18);
    t.run_verilogFind(  9, 1, 23, 18);
    t.run_verilogFind(N+1, 0,  0, 18);
   }

  private static void test_put_wide()
   {z(); sayCurrentTestName();
    final BtreeSF t = wideTree();
    t.P.run(); t.P.clear();
    t.put();
    t.runVerilogPutTest(1, 32, """
1=0 |
""");

    t.runVerilogPutTest(2, 32, """
1,2=0 |
""");
                                                                                // Split instruction
    t.runVerilogPutTest(3, 32, """
1,2,3=0 |
""");

    t.runVerilogPutTest(4, 32, """
1,2,3,4=0 |
""");

    t.runVerilogPutTest(5, 32, """
1,2,3,4,5=0 |
""");

    t.runVerilogPutTest(6, 32, """
1,2,3,4,5,6=0 |
""");

    t.runVerilogPutTest(7, 32, """
1,2,3,4,5,6,7=0 |
""");

    t.runVerilogPutTest(8, 32, """
1,2,3,4,5,6,7,8=0 |
""");

    t.runVerilogPutTest(9, 162, """
          4            |
          0            |
          1            |
          2            |
1,2,3,4=1  5,6,7,8,9=2 |
""");

    t.runVerilogPutTest(10, 40, """
          4               |
          0               |
          1               |
          2               |
1,2,3,4=1  5,6,7,8,9,10=2 |
""");

    t.runVerilogPutTest(11, 40, """
          4                  |
          0                  |
          1                  |
          2                  |
1,2,3,4=1  5,6,7,8,9,10,11=2 |
""");

    t.runVerilogPutTest(12, 40, """
          4                     |
          0                     |
          1                     |
          2                     |
1,2,3,4=1  5,6,7,8,9,10,11,12=2 |
""");

    t.runVerilogPutTest(13, 286, """
          4          8                  |
          0          0.1                |
          1          3                  |
                     2                  |
1,2,3,4=1  5,6,7,8=3    9,10,11,12,13=2 |
""");

    t.runVerilogPutTest(14, 40, """
          4          8                     |
          0          0.1                   |
          1          3                     |
                     2                     |
1,2,3,4=1  5,6,7,8=3    9,10,11,12,13,14=2 |
""");

    t.runVerilogPutTest(15, 40, """
          4          8                        |
          0          0.1                      |
          1          3                        |
                     2                        |
1,2,3,4=1  5,6,7,8=3    9,10,11,12,13,14,15=2 |
""");

    t.runVerilogPutTest(16, 40, """
          4          8                           |
          0          0.1                         |
          1          3                           |
                     2                           |
1,2,3,4=1  5,6,7,8=3    9,10,11,12,13,14,15,16=2 |
""");

    t.runVerilogPutTest(17, 358, """
                  8             12                  |
                  0             0.1                 |
                  1             4                   |
                                2                   |
1,2,3,4,5,6,7,8=1  9,10,11,12=4    13,14,15,16,17=2 |
""");

    t.runVerilogPutTest(18, 40, """
                  8             12                     |
                  0             0.1                    |
                  1             4                      |
                                2                      |
1,2,3,4,5,6,7,8=1  9,10,11,12=4    13,14,15,16,17,18=2 |
""");

    t.runVerilogPutTest(19, 40, """
                  8             12                        |
                  0             0.1                       |
                  1             4                         |
                                2                         |
1,2,3,4,5,6,7,8=1  9,10,11,12=4    13,14,15,16,17,18,19=2 |
""");

    t.runVerilogPutTest(20, 40, """
                  8             12                           |
                  0             0.1                          |
                  1             4                            |
                                2                            |
1,2,3,4,5,6,7,8=1  9,10,11,12=4    13,14,15,16,17,18,19,20=2 |
""");
   }

  protected static void oldTests()                                              // Tests thought to be in good shape
   {final boolean longRunning = github_actions && 1 == 0;
    test_memory();
    test_find_and_insert();
    test_put_ascending();
    if (longRunning) test_put_ascending_wide();
    test_put_descending();
    test_put_small_random();
    if (longRunning) test_put_large_random();
    test_find32();
    test_find_and_update();
    test_delete_ascending();
    test_delete_descending();
    test_delete_small_random();
    if (longRunning) test_delete_large_random();
    if (longRunning) test_delete_random_not_100();
    test_primes();
    test_node();
    test_delete_verilog();
    test_find_verilog();
    test_put_verilog();
//  test_find_wide();
//  test_put_wide();
    test_first_last();
   }

  protected static void newTests()                                              // Tests being worked on
   {//oldTests();
    //test_delete_verilog();
    //test_find_verilog();
    //test_put_verilog();
    test_first_last();
   }

  public static void main(String[] args)                                        // Test if called as a program
   {deleteAllFiles(verilogFolder, 1000);                                         // Clear the verilog folder as otherwise life gets very confusing
    try                                                                         // Get a traceback in a format clickable in Geany if something goes wrong to speed up debugging.
     {if (github_actions) oldTests(); else newTests();                          // Tests to run
      if (github_actions)                                                       // Coverage analysis
       {coverageAnalysis                                                        // Used for printing
         (12,
         "BtreePA.java",                                                        // These classes are not fully exercised by these tests
         "BtreeSF.java",
         "BtreeSML.java",
         "MemoryLayout.java",
         "MemoryLayoutPA.java",
         "ProgramPA.java",
         "StuckPA.java",
         "StuckSML.java");
       }
      testSummary();                                                            // Summarize test results
      System.exit(testsFailed);
     }
    catch(Exception e)                                                          // Get a traceback in a format clickable in Geany
     {System.err.println(e);
      System.err.println(fullTraceBack(e));
      System.exit(1);
     }
   }
 }
// the retort courteous.
// the quip modest.
// the reply churlish
// the reproof valiant
// the countercheck quarrelsome
// the lie circumstantial
// the lie direct
