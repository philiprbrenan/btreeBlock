//------------------------------------------------------------------------------
// BtreeSA in pseudo assembler
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Btree in a block on the surface of a silicon chip.
// Isoptimize() moving code inside then clauses?
import java.util.*;
import java.nio.file.*;

abstract class BtreePA extends Test                                             // Manipulate a btree using static methods and memory
 {final MemoryLayoutPA M;                                                       // The memory layout of the btree
  final MemoryLayoutPA T;                                                       // The memory used to hold temporary variable used during a transaction on the btree
  final ProgramPA      P = new ProgramPA();                                     // Program in which to generate instructions
  final boolean   Assert = false;                                               // Execute asserts if true
  final boolean     Halt = false;                                               // Execute tests that result in a halt
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

  Layout.Field     leaf;                                                        // Layout of a leaf in the memory used by btree
  Layout.Field     branch;                                                      // Layout of a branch in the memory used by btree
  Layout.Union     branchOrLeaf;                                                // Layout of either a leaf or a branch in the memory used by btree
  Layout.Bit       isLeaf;                                                      // Whether the current node is a leaf or a branch
  Layout.Variable  free;                                                        // Free list chain
  Layout.Structure Node;                                                        // Layout of a node in the memory used by btree
  Layout.Array     nodes;                                                       // Layout of an array of nodes in the memory used by btree
  Layout.Variable  freeList;                                                    // Single linked list of nodes that have been freed and so can be reused without fragmenting memory
  Layout.Structure bTree;                                                       // Btree

  final static int
    linesToPrintABranch =  4,                                                   // The number of lines required to print a branch
         maxPrintLevels = 10,                                                   // Maximum number of levels to print in a tree
               maxDepth = 99,                                                   // Maximum depth of any realistic tree
            testMaxSize = github_actions ? 1000 : 50;                           // Maximum number of leaves plus branches during testing

  int          nodeUsed = 0;                                                    // Number of nodes currently in use
  int       maxNodeUsed = 0;                                                    // Maximum number of branches plus leaves used

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

  final StuckPA[]branchTransactions;                                            // Transactions to use on branch stucks
  final StuckPA[]  leafTransactions;                                            // Transactions to use on leaf stucks

  final StuckPA bSize;                                                          // Branch size
  final StuckPA bLeaf;                                                          // Check whether a node has leaves for children
  final StuckPA bTop;                                                           // Get the size of a stuck
  final StuckPA bFirstBranch;                                                   // Locate the first greater or equal key in a branch
  final StuckPA bT;                                                             // Process a parent node
  final StuckPA bL;                                                             // Process a left node
  final StuckPA bR;                                                             // Process a right node

  final StuckPA lSize;                                                          // Branch size
  final StuckPA lLeaf;                                                          // Check whether a node has leaves for children
  final StuckPA lEqual;                                                         // Locate an equal key
  final StuckPA lFirstLeaf;                                                     // Locate the first greater or equal key in a leaf
  final StuckPA lT;                                                             // Process a parent node as a leaf
  final StuckPA lL;                                                             // Process a left node
  final StuckPA lR;                                                             // Process a right node

  boolean debug = false;                                                        // Debugging enabled

//D1 Construction                                                               // Create a Btree from nodes which can be branches or leaves.  The data associated with the BTree is stored only in the leaves opposite the keys

  BtreePA()                                                                     // Define a Btree with user specified dimensions
   {z();
    splitLeafSize   = maxKeysPerLeaf()   >> 1;                                  // The number of key, data pairs to split out of a leaf
    splitBranchSize = maxKeysPerBranch() >> 1;                                  // The number of key, next pairs to split out of a branch
    bitsPerNext     = logTwo(maxSize());                                        // The number of bits in a next field sufficient to index any node
    bitsPerSize     = logTwo(max(bitsPerKey(), bitsPerData())+1);               // The number of bits in stuck size field sufficient to index an key or data element including top

    M = new MemoryLayoutPA(layout(),            "M");                           // The memory layout of the btree
    bitsPerAddress = logTwo(M.memory().size());                                 // Number of bits to address any bit in memory
    T = new MemoryLayoutPA(transactionLayout(), "T");                           // The memory used to hold temporary variable used during a transaction on the btree

    M.program(P);

    T.at(maxKeysPerLeaf)  .setInt(maxKeysPerLeaf());
    T.at(maxKeysPerBranch).setInt(maxKeysPerBranch());
    T.at(two)             .setInt(2);
    T.at(MaxDepth)        .setInt(maxDepth);                                    // Prevent runaway searches of the btree by limiting the number of levels to be searched
    T.program(P);

     {final int N = Branch_length;                                              // Preallocate transactions used on branch stucks
      branchTransactions = new StuckPA[N];

      for (int i = 0; i < N; i++)
       {final StuckPA b = branchTransactions[i] = new StuckPA("branch_"+i, M)   // Based stucks
         {int     maxSize() {return BtreePA.this.maxKeysPerBranch()+1;}         // Not forgetting top next
          int  bitsPerKey() {return BtreePA.this.bitsPerKey();}
          int bitsPerData() {return BtreePA.this.bitsPerNext;}
          int bitsPerSize() {return BtreePA.this.bitsPerSize;}
         };
         b.M.layout.layoutName = "branchMain"+i;
         b.T.layout.layoutName = "branch"+i;
         b.program(P);
        }
      }

     {final int N = Leaf_length;                                                // Preallocate transactions used on leaf stucks
      leafTransactions = new StuckPA[N];

      for (int i = 0; i < N; i++)
       {final StuckPA l = leafTransactions[i] = new StuckPA("leaf_"+i, M)       // Based stucks
         {int     maxSize() {return BtreePA.this.maxKeysPerLeaf();}
          int  bitsPerKey() {return BtreePA.this.bitsPerKey();}
          int bitsPerData() {return BtreePA.this.bitsPerData();}
          int bitsPerSize() {return BtreePA.this.bitsPerSize;}
         };
         l.M.layout.layoutName = "leafMain"+i;
         l.T.layout.layoutName = "leaf"+i;
         l.program(P);
       }
     }

    bSize        = branchTransactions[Branch_Size       ];                      // Branch size
    bLeaf        = branchTransactions[Branch_Leaf       ];                      // Check whether a node has leaves for children
    bTop         = branchTransactions[Branch_Top        ];                      // Get the size of a stuck
    bFirstBranch = branchTransactions[Branch_FirstBranch];                      // Locate the first greater or equal key in a branch
    bT           = branchTransactions[Branch_T          ];                      // Process a parent node
    bL           = branchTransactions[Branch_tl         ];                      // Process a left node
    bR           = branchTransactions[Branch_tr         ];                      // Process a right node

    lSize        =   leafTransactions[Leaf_Size         ];                      // Leaf size
    lLeaf        =   leafTransactions[Leaf_Leaf         ];                      // Print a leaf
    lEqual       =   leafTransactions[Leaf_Equal        ];                      // Locate an equal key
    lFirstLeaf   =   leafTransactions[Leaf_FirstLeaf    ];                      // Locate the first greater or equal key in a leaf
    lT           =   leafTransactions[Leaf_T            ];                      // Process a parent node
    lL           =   leafTransactions[Leaf_tl           ];                      // Process a left node
    lR           =   leafTransactions[Leaf_tr           ];                      // Process a right node

    P.new I()
     {void a()
       {final int N = maxSize();                                                // Put all the nodes on the free chain at the start with low nodes first
        for (int i = N; i > 0; --i) setInt(free, (i == N ? 0 : i), i - 1);      // Link this node to the previous node
        setInt(freeList, root);                                                 // Root is first on free chain
       }
      String v() {return "/* Construct Free list */";}
     };
    allocate(false);                                                            // The root is always at zero, which frees zero to act as the end of list marker on the free chain
    T.setIntInstruction(node_setLeaf, root);
    setLeaf();                                                                  // The root starts as a leaf
  }

  static BtreePA btreePA(final int leafKeys, int branchKeys)                    // Define a test btree with the specified dimensions
   {return new BtreePA()
     {int maxSize         () {return testMaxSize;}
      int maxKeysPerLeaf  () {return    leafKeys;}
      int maxKeysPerBranch() {return  branchKeys;}
      int bitsPerKey      () {return          32;}
      int bitsPerData     () {return          32;}
     };
   }

  static BtreePA btreePA_small()                                                // Define a small test btree
   {return new BtreePA()
     {int maxSize         () {return  8;}
      int maxKeysPerLeaf  () {return  2;}
      int maxKeysPerBranch() {return  3;}
      int bitsPerKey      () {return  4;}
      int bitsPerData     () {return  4;}
     };
   }

  Layout layout()                                                               // Layout describing memory used by btree
   {z();
    final BtreePA btree = this;

    final StuckPA leafStuck = new StuckPA("leaf", M)                            // Leaf
     {int               maxSize() {return btree.maxKeysPerLeaf();}
      int            bitsPerKey() {return btree.bitsPerKey();}
      int           bitsPerData() {return btree.bitsPerData();}
      int           bitsPerSize() {return btree.bitsPerSize;}
     };
    leafStuck.T.layout.layoutName = "leaf";

    final StuckPA branchStuck = new StuckPA("branch", M)                        // Branch
     {int               maxSize() {return btree.maxKeysPerBranch()+1;}          // Not forgetting top next
      int            bitsPerKey() {return btree.bitsPerKey();}
      int           bitsPerData() {return btree.bitsPerNext;}
      int           bitsPerSize() {return btree.bitsPerSize;}
     };
    branchStuck.T.layout.layoutName = "branch";

    final Layout l = Layout.layout();
    leaf         = l.duplicate("leaf",         leafStuck.layout());
    branch       = l.duplicate("branch",       branchStuck.layout());
    branchOrLeaf = l.union    ("branchOrLeaf", leaf,   branch);
    isLeaf       = l.bit      ("isLeaf");
    free         = l.variable ("free",         btree.bitsPerNext);
    Node         = l.structure("node",         isLeaf, free, branchOrLeaf);
    nodes        = l.array    ("nodes",        Node,         maxSize());
    freeList     = l.variable ("freeList",     btree.bitsPerNext);
    bTree        = l.structure("bTree",        freeList  , nodes);
    return l.compile();
   }

//D1 Control                                                                    // Testing, control and integrity

  private void ok(String expected) {Test.ok(toString(), expected);}             // Confirm tree is as expected
  private void stop()              {Test.stop(toString());}                     // Stop after printing the tree
  public String toString()         {return print();}                            // Print the tree

//D1 Memory access                                                              // Access to memory

  private void checkMainField(Layout.Field field)                               // Check that a variable is in main memory
   {z();
    if (field.container() != M.layout)
     {final String name = field.container().layoutName;
      stop("Field:", field.name, "is part of memory layout:", name, "not main");
     }
   }

  private void checkTransactionField(Layout.Field field)                        // Check that a variable is in transaction memory
   {z();
    if (field.container() != T.layout)
     {final String name = field.container().layoutName;
      stop("Field:", field.name, "is part of memory layout:",
                           name, "not transaction");
     }
   }

  private int getInt(Layout.Field field)                                        // Get an integer from main memory
   {z(); checkMainField(field); return M.getInt(field);
   }
  private int  getInt(Layout.Field field, int index)
   {z(); checkMainField(field); return M.getInt(field, index);
   }

  private void setInt(Layout.Field field, int value)                            // Set an integer in main memory
   {z(); checkMainField(field); M.setInt(field, value);
   }
  private void setInt(Layout.Field field, int value, int index)
   {z(); checkMainField(field); M.setInt(field, value, index);
   }

  private int tGetInt(Layout.Field field)                                       // Get an integer from transaction memory
   {z(); checkTransactionField(field); return T.getInt(field);
   }
  private int tGetInt(Layout.Field field, int index)
   {z(); checkTransactionField(field); return T.getInt(field, index);
   }

  private void tSetInt(Layout.Field field, int value)                           // Set an integer in transaction memory
   {z(); checkTransactionField(field); T.setInt(field, value);
   }
  private void tSetInt(Layout.Field field, int value, int index)
   {z(); checkTransactionField(field); T.setInt(field, value, index);
   }

  void tt(Layout.Variable target, Layout.Variable source)                       // Copy the value of one transaction variable into another
   {z(); checkTransactionField(target); checkTransactionField(source);
    T.at(target).move(T.at(source));
   }

  void tm(Layout.Variable target, Layout.Variable source)                       // Copy the value of a main memory variable into transaction memory
   {z(); checkTransactionField(target); checkMainField(source);
    T.at(target).move(M.at(source));
   }

  void mt(Layout.Variable target, Layout.Variable source)                       // Copy the value of a transaction memory variable into main memory
   {z(); checkMainField(target); checkTransactionField(source);
    M.at(target).move(T.at(source));
   }

//D1 Memory allocation                                                          // Allocate and free memory

  private void allocate(boolean check)                                          // Allocate a node with or without checking for sufficient free space
   {tm(allocate, freeList);                                                     // Node at head of free nodes list
    if (check)
     {P.new If (T.at(allocate))
       {void Else()
         {P.halt("No more memory available");                                   // No more free nodes available
         }
       };
     }
    M.at(freeList).move(M.at(free, T.at(allocate)));                            // Second node on free list

    tt(node_clear, allocate);
    clear();                                                                    // Construct and clear the node
//  tt(node_clear, allocate);
//  clear(T.at(allocate));                                                      // Construct and clear the node
//    maxNodeUsed  = max(maxNodeUsed, ++nodeUsed);                              // Number of nodes in use
   }

  private void allocate() {z(); allocate(true);}                                // Allocate a node checking for free space

//D1 Components                                                                 // A branch or leaf in the tree

  Layout.Variable               Key;                                            // Key being found, inserted or deleted
  Layout.Variable              Data;                                            // Data associated with the key being inserted
  Layout.Bit                  found;                                            // Whether the key was found
  Layout.Variable               key;                                            // Key to insert
  Layout.Variable              data;                                            // Data associated with the key found

  Layout.Variable          allocate;                                            // The latest allocation result
  Layout.Variable          nextFree;                                            // Next element of the free chain

  Layout.Bit                success;                                            // Inserted or updated if true
  Layout.Bit               inserted;                                            // Inserted if true

  Layout.Variable             first;                                            // Index of first key greater than or equal to the search key
  Layout.Variable              next;                                            // The corresponding next field or top if no such key was found
                                                                                // Find equal in leaf
  Layout.Variable            search;                                            // Search key

  Layout.Variable          firstKey;                                            // First of right leaf
  Layout.Variable           lastKey;                                            // Last of left leaf
  Layout.Variable             flKey;                                            // Key mid way between last of left and first of right
  Layout.Variable         parentKey;                                            // Parent key

  Layout.Variable                lk;                                            // Left  child key
  Layout.Variable                ld;                                            // Left  child data
  Layout.Variable                rk;                                            // Right child key
  Layout.Variable                rd;                                            // Right child data
  Layout.Variable             index;                                            // Index of a slot in a node

  Layout.Variable                nl;                                            // Number in the left child
  Layout.Variable                nr;                                            // Number in the right child

  Layout.Variable                 l;                                            // Left node
  Layout.Variable                 r;                                            // Right node

  Layout.Variable       splitParent;                                            // The parent during a splitting operation
  Layout.Bit                 IsLeaf;                                            // On a leaf
  Layout.Bit                 isFull;                                            // The node is full
  Layout.Bit             leafIsFull;                                            // The leaf node is full
  Layout.Bit           branchIsFull;                                            // The branch node is full
  Layout.Bit           parentIsFull;                                            // The parent branch node is full
  Layout.Bit                isEmpty;                                            // The node is empty
  Layout.Bit                  isLow;                                            // The node has too few children for a delete
  Layout.Bit   hasLeavesForChildren;                                            // The node has leaves for children
  Layout.Bit         stolenOrMerged;                                            // A merge or steal operation succeeded
  Layout.Bit           pastMaxDepth;                                            // A merge or steal operation succeeded
  Layout.Bit             nodeMerged;                                            // All sequential pairs of siblings have been offered a chance to merge
  Layout.Bit              mergeable;                                            // The left and right children are mergable
  Layout.Bit                deleted;                                            // Whether the delete request actually deleted the specified key

  Layout.Variable          leafBase,   leafBase1,   leafBase2,   leafBase3;     // The offset of a leaf in memory
  Layout.Variable        branchBase, branchBase1, branchBase2, branchBase3;     // The offset of a branch in memory
  Layout.Variable          leafSize;                                            // Number of children in body of leaf
  Layout.Variable        branchSize;                                            // Number of children in body of branch taking top for granted as it is always there
  Layout.Variable               top;                                            // The top next element of a branch - only used in printing
                                                                                // Find, insert, delete - the public entry points to this module
  Layout.Variable              find;                                            // Results of a find operation
  Layout.Variable     findAndInsert;                                            // Results of a find and insert operation
  Layout.Variable            parent;                                            // Parent node in a descent through the tree
  Layout.Variable             child;                                            // Child node in a descent through the tree
  Layout.Variable         leafFound;                                            // Leaf found by find
  Layout.Variable    maxKeysPerLeaf;                                            // Maximum keys per leaf
  Layout.Variable  maxKeysPerBranch;                                            // Maximum keys per branch
  Layout.Variable          MaxDepth;                                            // Maximum depth of a search
  Layout.Variable               two;                                            // The value two
  Layout.Variable         findDepth;                                            // Current level being searched by find
  Layout.Variable          putDepth;                                            // Current level being traversed by put
  Layout.Variable       deleteDepth;                                            // Current level being traversed by delete
  Layout.Variable        mergeDepth;                                            // Current level being traversed by merge
  Layout.Variable        mergeIndex;                                            // Current index of node being merged across

  Layout.Variable  node_isLeaf;                                                 // The node to be used to implicitly parameterize each method call
  Layout.Variable  node_setLeaf;
  Layout.Variable  node_setBranch;
  Layout.Variable  node_assertLeaf;
  Layout.Variable  node_assertBranch;
  Layout.Variable  allocLeaf;
  Layout.Variable  allocBranch;
  Layout.Variable  node_free;
  Layout.Variable  node_clear;
  Layout.Variable  node_erase;
  Layout.Variable  node_leafBase,     node_leafBase1,   node_leafBase2,   node_leafBase3;
  Layout.Variable  node_branchBase, node_branchBase1, node_branchBase2, node_branchBase3;
  Layout.Variable  node_leafSize;
  Layout.Variable  node_branchSize;
  Layout.Variable  node_isFull;
  Layout.Variable  node_leafIsFull;
  Layout.Variable  node_branchIsFull;
  Layout.Variable  node_parentIsFull;
  Layout.Variable  node_isEmpty;
  Layout.Variable  node_isLow;
  Layout.Variable  node_hasLeavesForChildren;
  Layout.Variable  node_top;
  Layout.Variable  node_findEqualInLeaf;
  Layout.Variable  node_findFirstGreaterThanOrEqualInLeaf;
  Layout.Variable  node_findFirstGreaterThanOrEqualInBranch;
  Layout.Variable  node_splitLeaf;
  Layout.Variable  node_splitBranch;
  Layout.Variable  node_stealFromLeft;
  Layout.Variable  node_stealFromRight;
  Layout.Variable  node_mergeRoot;
  Layout.Variable  node_mergeLeftSibling;
  Layout.Variable  node_mergeRightSibling;
  Layout.Variable  node_balance;

  Layout transactionLayout()                                                    // Layout of temporary storage used during a transaction against the btree
   {final Layout L = new Layout();
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

                                    leafBase = L.variable ("leafBase"                                      , bitsPerAddress);
                                   leafBase1 = L.variable ("leafBase1"                                     , bitsPerAddress);
                                   leafBase2 = L.variable ("leafBase2"                                     , bitsPerAddress);
                                   leafBase3 = L.variable ("leafBase3"                                     , bitsPerAddress);
                                  branchBase = L.variable ("branchBase"                                    , bitsPerAddress);
                                 branchBase1 = L.variable ("branchBase1"                                   , bitsPerAddress);
                                 branchBase2 = L.variable ("branchBase2"                                   , bitsPerAddress);
                                 branchBase3 = L.variable ("branchBase3"                                   , bitsPerAddress);
                                    leafSize = L.variable ("leafSize"                                      , bitsPerSize);
                                  branchSize = L.variable ("branchSize"                                    , bitsPerSize);
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
                                    MaxDepth = //L.variable ("maxDepth"                                      , bitsPerNext);
                                   findDepth = //L.variable ("findDepth"                                     , bitsPerNext);
                                    putDepth = //L.variable ("putDepth"                                      , bitsPerNext);
                                 deleteDepth = //L.variable ("deleteDepth"                                   , bitsPerNext);
                                  mergeDepth = L.variable ("mergeDepth"                                    , bitsPerNext);
                                  mergeIndex = L.variable ("mergeIndex"                                    , bitsPerSize);

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
                               node_leafBase = L.variable ("node_leafBase"                                 , bitsPerNext);
                              node_leafBase1 = L.variable ("node_leafBase1"                                , bitsPerNext);
                              node_leafBase2 = L.variable ("node_leafBase2"                                , bitsPerNext);
                              node_leafBase3 = L.variable ("node_leafBase3"                                , bitsPerNext);
                             node_branchBase = L.variable ("node_branchBase"                               , bitsPerNext);
                            node_branchBase1 = L.variable ("node_branchBase1"                              , bitsPerNext);
                            node_branchBase2 = L.variable ("node_branchBase2"                              , bitsPerNext);
                            node_branchBase3 = L.variable ("node_branchBase3"                              , bitsPerNext);
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

      leafBase,     leafBase1,   leafBase2,   leafBase3,
      branchBase, branchBase1, branchBase2, branchBase3,
      leafSize,
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
      mergeDepth,
      mergeIndex,
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
      node_leafBase,
      node_leafBase1,
      node_leafBase2,
      node_leafBase3,
      node_branchBase,
      node_branchBase1,
      node_branchBase2,
      node_branchBase3,
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

  private void    isLeaf() {z(); T.at(IsLeaf).move(M.at(isLeaf, T.at(node_isLeaf)));}                // A leaf if true
  private void   setLeaf() {z(); M.at(isLeaf, T.at(node_setLeaf))  .ones();}    // Set as leaf
  private void setBranch() {z(); M.at(isLeaf, T.at(node_setBranch)).zero();}    // Set as branch

  private void isLeaf(MemoryLayoutPA.At node)                                   // A leaf if true
   {z();
    T.at(IsLeaf).move(M.at(isLeaf, node));
   }

  private void assertLeaf()
   {z();
    tt(node_isLeaf, node_assertLeaf); isLeaf();
    P.new If (T.at(IsLeaf))
     {void Else()
       {P.halt("Leaf required");
       }
     };
   }
  private void assertBranch()
   {z();
    tt(node_isLeaf, node_assertBranch);
    isLeaf();
    P.new If (T.at(IsLeaf))
     {void Then()
      {P.halt("Branch required");
      }
    };
   }

  private void allocLeaf()                                                      // Allocate leaf
   {z();
    allocate();
    tt(allocLeaf,     allocate);
    tt(node_setLeaf,  allocate);
    setLeaf();
   }
  private void allocBranch()                                                    // Allocate branch
   {z();
    allocate();
    tt(allocBranch   , allocate);
    tt(node_setBranch, allocate);
    setBranch();
   }

  private void free()                                                           // Free a new node to make it available for reuse
   {z();
    P.new If (T.at(node_free)) {void Else() {P.halt("Cannot free root");}};     // The root is never freed
    z(); tt(node_erase, node_free); erase();                                    // Clear the node to encourage erroneous frees to do damage that shows up quickly.
    M.at(free, T.at(node_free)).move(M.at(freeList));                           // Chain this node in front of the last freed node
    M.at(freeList).move(T.at(node_free));                                       // Make this node the head of the free chain
    maxNodeUsed = max(maxNodeUsed, --nodeUsed);                                 // Number of nodes in use
   }

  private void clear() {z();  clear(T.at(node_clear));}                         // Clear a new node to zeros ready for use

  private void clear(MemoryLayoutPA.At node)                                    // Clear a new node to zeros ready for use
   {z(); M.at(Node, node).zero();
   }

  private void erase()                                                          // Clear a new node to ones as this is likely to create invalid values that will be easily detected in the case of erroneous frees
   {z();
    M.at(Node, T.at(node_erase)).ones();
   }

  private void leafBase(Layout.Variable      leafBase,                          // Base of leaf stuck in memory
                        Layout.Variable node_leafBase)
   {z();
    P.new I()
     {void a()
       {final MemoryLayoutPA.At a = M.at(leaf, T.at(node_leafBase)).setOff();
        T.at(leafBase).setInt(a.at);
       }
      String v() {return T.at(leafBase).verilogLoad() + " <= " + M.at(leaf, T.at(node_leafBase)).verilogAddr() + ";";}
     };
   }

  private void leafBase () {leafBase(leafBase , node_leafBase );}
  private void leafBase1() {leafBase(leafBase1, node_leafBase1);}
  private void leafBase2() {leafBase(leafBase2, node_leafBase2);}
  private void leafBase3() {leafBase(leafBase3, node_leafBase3);}

  private void branchBase(Layout.Variable      branchBase,                      // Base of branch stuck in memory
                          Layout.Variable node_branchBase)
   {z();
    P.new I()
     {void a()
       {final MemoryLayoutPA.At a = M.at(branch, T.at(node_branchBase)).setOff();
        T.at(branchBase).setInt(a.at);
       }
      String v() {return T.at(branchBase).verilogLoad() + " <= " + M.at(branch, T.at(node_branchBase)).verilogAddr() + ";";}
     };
   }

  private void branchBase () {branchBase(branchBase,  node_branchBase );}
  private void branchBase1() {branchBase(branchBase1, node_branchBase1);}
  private void branchBase2() {branchBase(branchBase2, node_branchBase2);}
  private void branchBase3() {branchBase(branchBase3, node_branchBase3);}

  private void leafSize()                                                       // Number of children in body of leaf
   {z();
    tt(node_leafBase, node_leafSize); leafBase(); lSize.base(T.at(leafBase));
    lSize.size(); T.at(leafSize).move(lSize.T.at(lSize.size));
   }

  private void branchSize()                                                     // Number of children in body of branch taking top for granted as it is always there
   {z();
    tt(node_branchBase, node_branchSize); branchBase(); bSize.base(T.at(branchBase));
    bSize.size(); T.at(branchSize).move(bSize.T.at(bSize.size));                // Changed order here to match leafSize more closely
    T.at(branchSize).dec();                                                     // Account for top which will always be present
   }

  private void isEmpty()                                                        // The node is empty
   {z();
    tt(node_isLeaf, node_isEmpty);
    isLeaf();
    P.new If (T.at(IsLeaf))
     {void Then()
       {tt(node_leafSize, node_isEmpty); leafSize();
        T.at(leafSize).isZero(T.at(isEmpty));
       }
      void Else()
       {z();
        tt(node_branchSize, node_isEmpty); branchSize();
        T.at(branchSize).isZero(T.at(isEmpty));                                 // Allow for top which must always be present
       }
     };
   }

  private void isFull()                                                         // The node is full
   {z();
    tt(node_isLeaf, node_isFull);
    isLeaf();
    P.new If (T.at(IsLeaf))
     {void Then()
       {tt(node_leafSize, node_isFull);
        leafSize();
        T.at(leafSize)  .equal(T.at(maxKeysPerLeaf),   T.at(isFull));
       }
      void Else()
       {tt(node_branchSize, node_isFull);
        branchSize();
        T.at(branchSize).equal(T.at(maxKeysPerBranch), T.at(isFull));
       }
     };
   }

  private void leafIsFull()                                                     // Whether a node known to be a leaf is full
   {tt(node_leafSize, node_leafIsFull);
    leafSize();
    T.at(leafSize)  .equal(T.at(maxKeysPerLeaf),   T.at(leafIsFull));
   }

  private void branchIsFull()                                                   // Whether a node known to be a branch is full
   {tt(node_branchSize, node_branchIsFull);
    branchSize();
    T.at(branchSize).equal(T.at(maxKeysPerBranch), T.at(branchIsFull));
   }

  private void isLow()                                                          // The node is low on children making it impossible to merge two sibling children
   {z(); tt(node_isLeaf, node_isLow); isLeaf();
    P.new If (T.at(IsLeaf))
     {void Then()
       {tt(node_leafSize, node_isLow);
        leafSize();
        T.at(leafSize).lessThan(T.at(two), T.at(isLow));
       }
      void Else()
       {tt(node_branchSize, node_isLow);
        branchSize();
        T.at(branchSize).lessThan(T.at(two), T.at(isLow));
       }
     };
   }

  private void hasLeavesForChildren()                                           // The node has leaves for children
   {if (Assert) {tt(node_assertBranch, node_hasLeavesForChildren); assertBranch();}
    tt(node_branchBase, node_hasLeavesForChildren); branchBase(); bLeaf.base(T.at(branchBase));
    bLeaf.lastElement();
    T.at(node_isLeaf).move(bLeaf.T.at(bLeaf.tData)); isLeaf(bLeaf.T.at(bLeaf.tData));
    tt(hasLeavesForChildren, IsLeaf);
   }

  private void top()                                                            // The top next element of a branch - only used in printing
   {if (Assert) {tt(node_assertBranch, node_top); assertBranch();}
    tt(node_branchBase, node_top); branchBase(); bTop.base(T.at(branchBase));
    tt(node_branchSize, node_top); branchSize(); bTop.T.at(bTop.index).move(T.at(branchSize));
    bTop.elementAt();
    T.at(top).move(bTop.T.at(bTop.tData));
   }

//D2 Search                                                                     // Search within a node and update the node description with the results

  private void findEqualInLeaf()                                                // Find the first key in the leaf that is equal to the search key
   {if (Assert) {tt(node_assertLeaf, node_findEqualInLeaf); assertLeaf();}
    tt(node_leafBase, node_findEqualInLeaf); leafBase();
    lEqual.base(T.at(leafBase));

    lEqual.T.at(lEqual.search).move(T.at(search));
    lEqual.T.setIntInstruction(lEqual.limit, 0);
    lEqual.search();
    M.moveParallel
     (T.at(found), lEqual.T.at(lEqual.found),                                   /// Parallel possible
      T.at(index), lEqual.T.at(lEqual.index),
      T.at(data ), lEqual.T.at(lEqual.tData));
   }

  public String findEqualInLeaf_toString()                                      // Print details of find equal in leaf node
   {final StringBuilder s = new StringBuilder();
    s.append("FindEqualInLeaf(");
    s.append(  "Leaf:"+T.at(node_findEqualInLeaf).getInt());
    s.append(  " Key:"+T.at(search)              .getInt());
    s.append(" found:"+T.at(found)               .getInt());
    if (T.at(found).isOnes())
     {s.append(" data:"+T.at(data).getInt()+" index:"+T.at(index).getInt());
     }
    s.append(")\n");
    return s.toString();
   }

  private void findFirstGreaterThanOrEqualInLeaf()                              // Find the first key in the  leaf that is equal to or greater than the search key
   {if (Assert) {tt(node_assertLeaf, node_findFirstGreaterThanOrEqualInLeaf); assertLeaf();}
    tt(node_leafBase, node_findFirstGreaterThanOrEqualInLeaf); leafBase();
    lFirstLeaf.base(T.at(leafBase));
    lFirstLeaf.T.at(lFirstLeaf.search).move(T.at(search));
    lFirstLeaf.T.setIntInstruction(lFirstLeaf.limit, 0);
    lFirstLeaf.searchFirstGreaterThanOrEqual();
    M.moveParallel
     (T.at(found), lFirstLeaf.T.at(lFirstLeaf.found),                           /// Parallel possible
      T.at(first), lFirstLeaf.T.at(lFirstLeaf.index));
   }

  private void findFirstGreaterThanOrEqualInBranch()                            // Find the first key in the branch that is equal to or greater than the search key
   {if (Assert) {tt(node_assertBranch, node_findFirstGreaterThanOrEqualInBranch); assertBranch();}
    tt(node_branchBase,   node_findFirstGreaterThanOrEqualInBranch); branchBase();
    bFirstBranch.base(T.at(branchBase));
    bFirstBranch.T.at(bFirstBranch.search).move(T.at(search));
    bFirstBranch.T.setIntInstruction(bFirstBranch.limit, 1);

    bFirstBranch.searchFirstGreaterThanOrEqual();
    M.moveParallel
     (T.at(found), bFirstBranch.T.at(bFirstBranch.found),                       /// Parallel possible
      T.at(first), bFirstBranch.T.at(bFirstBranch.index));

    P.new If (T.at(found))                                                      // Next if key matches else top
     {void Then()
       {T.at(next).move(bFirstBranch.T.at(bFirstBranch.tData));
       }
      void Else()                                                               // Top as no key matched
       {z();
        bFirstBranch.lastElement();
        T.at(next).move(bFirstBranch.T.at(bFirstBranch.tData));
       }
     };
   }

//D2 Array                                                                      // Represent the contents of the tree as an array

  private void leafToArray(int node, Stack<ArrayElement> s)                     // Leaf as an array
   {if (Assert) {T.at(node_assertLeaf).setInt(node); assertLeaf();}
    T.at(node_leafSize).setInt(node);
    leafSize();
    final int     K = T.at(leafSize).getInt();
    final StuckPA t = lLeaf.copyDef();
    T.at(node_leafBase).setInt(node); leafBase(); t.base(T.at(leafBase));
    for  (int i = 0; i < K; i++)
     {z();
      t.T.at(t.index).setInt(i); t.elementAt();
      s.push(new ArrayElement(i, t.T.at(t.tKey).getInt(), t.T.at(t.tData).getInt()));
     }
   }

  private void branchToArray(int node, Stack<ArrayElement> s)                   // Branch to array
   {if (Assert) {T.at(node_assertBranch).setInt(node); assertBranch();}
    T.at(node_branchSize  ).setInt(node); branchSize();
    final int K = T.at(branchSize).getInt()+1;                                  // Include top next

    if (K > 0)                                                                  // Branch has key, next pairs
     {z();
      final StuckPA t = bLeaf.copyDef();
      T.at(node_branchBase).setInt(node); branchBase(); t.base(T.at(branchBase));
      for  (int i = 0; i < K; i++)
       {z();
        t.T.at(t.index).setInt(i); t.elementAt();                               // Each node in the branch

        T.at(node_isLeaf).move(t.T.at(t.tData)); isLeaf(t.T.at(t.tData));
        if (T.at(IsLeaf).isOnes())
         {z();
          leafToArray(t.T.at(t.tData).getInt(), s);
         }
        else
         {z();
          if (t.T.at(t.tData).isZero())
           {say("Cannot descend through root from index", i,
                "in branch", node);
            break;
           }
          z(); branchToArray(t.T.at(t.tData).getInt(), s);
         }
       }
     }
   }

//D2 Print                                                                      // Print the contents of the tree

  public String find_toString()                                                 // Print find result
   {final StringBuilder s = new StringBuilder();
    s.append("Find(");
    s.append(    " search:"+T.at(search).getInt());
    s.append(     " found:"+T.at(found) .getInt());
    s.append(      " data:"+T.at(data)  .getInt());
    s.append(     " index:"+T.at(index) .getInt());
    s.append(")\n");
    return s.toString();
   }

  public String findAndInsert_toString()                                        // Print find and insert result
   {final StringBuilder s = new StringBuilder();
    s.append("FindAndInsert(");
    s.append(    " key:"+T.at(key)    .getInt());
    s.append(   " data:"+T.at(data)   .getInt());
    s.append(" success:"+T.at(success).getInt());
    if (T.at(success).isOnes()) s.append(" inserted:"+T.at(inserted));
    s.append(")\n" );
    return s.toString();
   }

  public String findFirstGreaterThanOrEqualInLeaf_toString()                    // Print results of search
   {final StringBuilder s = new StringBuilder();
    s.append("FindFirstGreaterThanOrEqualInLeaf(");
    s.append(  "Leaf:"+T.at(node_findFirstGreaterThanOrEqualInLeaf).getInt());
    s.append(  " Key:"+T.at(search).getInt());
    s.append(" found:"+T.at(found).getInt());
    if (T.at(found).isOnes()) s.append(" first:"+T.at(first).getInt());
    s.append(")\n");
    return s.toString();
   }

  public String findFirstGreaterThanOrEqualInBranch_toString()                  // Print search results
   {final StringBuilder s = new StringBuilder();
    s.append("FindFirstGreaterThanOrEqualInBranch(");
    s.append("branch:"+T.at(node_findFirstGreaterThanOrEqualInBranch).getInt());
    s.append(  " Key:"+T.at(search).getInt());
    s.append(" found:"+T.at(found).getInt());
    s.append( " next:"+T.at(next).getInt());
    if (T.at(found).isOnes()) s.append(" first:"+T.at(first).getInt());
    s.append(")\n");
    return s.toString();
   }

//D2 Split                                                                      // Split nodes in half to increase the number of nodes in the tree

  private void splitLeafRoot()                                                  // Split a leaf which happens to be a full root into two half full leaves while transforming the root leaf into a branch
   {if (Assert)                                                                 // Assert that we are indeed  on a leaf
     {T.setIntInstruction(node_assertLeaf, root);
      assertLeaf();
     }
    if (Halt)                                                                   // Confirm that the leaf is full and tso can be split
     {T.setIntInstruction(node_leafIsFull, root);
      leafIsFull();
      P.new If (T.at(leafIsFull))
       {void Else()
         {P.halt("Root is not full");
         }
       };
     }

    allocLeaf(); tt(l, allocLeaf);                                              // New left leaf
    allocLeaf(); tt(r, allocLeaf);                                              // New right leaf

    P.parallelStart();
      T.at(node_leafBase1).zero(); leafBase1(); lT.base(T.at(leafBase1));       // Set address of the referenced root stuck
    P.parallelSection();
      tt  (node_leafBase2, l);     leafBase2(); lL.base(T.at(leafBase2));       // Set address of the referenced left leaf stuck
    P.parallelSection();
      tt  (node_leafBase3, r);     leafBase3(); lR.base(T.at(leafBase3));       // Set address of the referenced right leaf stuck
    P.parallelEnd();

// for (int i = 0; i < splitLeafSize; i++)                                     // Build left leaf from parent
//  {z(); lT.shift();
//   M.moveParallel
//    (lL.T.at(lL.tKey ), lT.T.at(lT.tKey ),                                   /// Parallel possible
//     lL.T.at(lL.tData), lT.T.at(lT.tData));
//   lL.push();
//  }
// for (int i = 0; i < splitLeafSize; i++)                                     // Build right leaf from parent
//  {z(); lT.shift();
//   M.moveParallel
//    (lR.T.at(lR.tKey ), lT.T.at(lT.tKey),                                    /// Parallel possible
//     lR.T.at(lR.tData), lT.T.at(lT.tData));
//   lR.push();
//  }

    lT.split(lL, lR);                                                           // Split root leaf into child leaves

    P.parallelStart();
      lR.firstElement();
    P.parallelSection();
      lL. lastElement();
    P.parallelSection();
      T.setIntInstruction(node_setBranch,  root); setBranch();                  // The root is now a branch
    P.parallelSection();
      T.setIntInstruction(node_branchBase, root); branchBase();                 // Set address of the referenced leaf stuck
      bT.base(T.at(branchBase));                                                // Set address of the referenced leaf stuck
      bT.clear();                                                               // Clear the branch
    P.parallelEnd();

    P.parallelStart();
      T.at(firstKey).move(lR.T.at(lR.tKey));                                    // First of right leaf
    P.parallelSection();
      T.at(lastKey ).move(lL.T.at(lL.tKey));                                    // Last of left leaf
    P.parallelEnd();

    P.new I()                                                                   // Mid key - keys are likely to be bigger than 31 bits
     {void a()
       {T.at(flKey).setInt((T.at(firstKey).getInt()+T.at(lastKey).getInt())/2);
       }
      String v()
       {return T.at(flKey)   .verilogLoad() + "<= " +
           "("+T.at(firstKey).verilogLoad() + " + " +
               T.at(lastKey) .verilogLoad() + ") / 2;";
       }
     };

    M.moveParallel
     (bT.T.at(bT.tKey ), T.at(flKey),                                           /// Parallel possible
      bT.T.at(bT.tData), T.at(l));
    bT.push();                                                                  // Insert left leaf into root

    P.parallelStart();
      bT.T.at(bT.tKey).zero();
    P.parallelSection();
      bT.T.at(bT.tData).move(T.at(r));
    P.parallelEnd();
    bT.push();                                                                  // Insert right into root. This will be the top node and so ignored by search ... except last.
   }

  private void splitBranchRoot()                                                // Split a branch which happens to be a full root into two half full branches while retaining the current branch as the root
   {if (Assert) {T.setIntInstruction(node_assertBranch, root); assertBranch();}
    if (Halt)                                                                   // Confirm the root is fiull
     {T.setIntInstruction(node_branchIsFull, root); branchIsFull();
      P.new If (T.at(branchIsFull))
       {void Else()
         {P.halt("Root is not full");
         }
       };
     }
    z();

    allocBranch(); tt(l, allocBranch);                                          // New left branch
    allocBranch(); tt(r, allocBranch);                                          // New right branch

    T.setIntInstruction(node_branchBase1, root);
                             branchBase1(); bT.base(T.at(branchBase1));         // Set address of the referenced branch stuck
    tt(node_branchBase2, l); branchBase2(); bL.base(T.at(branchBase2));         // Set address of the referenced branch stuck
    tt(node_branchBase3, r); branchBase3(); bR.base(T.at(branchBase3));         // Set address of the referenced branch stuck

    bT.split(bL, bR);                                                           // Split the root as a branch

//    for (int i = 0; i < splitBranchSize; i++)                                 // Build left child from parent
//     {z(); bT.shift();
//      bL.T.at(bL.tKey ).move(bT.T.at(bT.tKey ));
//      bL.T.at(bL.tData).move(bT.T.at(bT.tData));
//      bL.push();
//     }
//    bT.shift();                                                               // This key, next pair will be part of the root
//    T.at(parentKey).move(bT.T.at(bT.tKey));
    bL.T.setIntInstruction(bL.tKey, 0);
    bT.T.setIntInstruction(bT.index, splitBranchSize);
    bT.elementAt();
    T.at(parentKey).move(bT.T.at(bT.tKey));
    bL.T.at(bL.tData).move(bT.T.at(bT.tData));
    bL.T.setIntInstruction(bL.index, splitBranchSize);
    bL.setElementAt();
//  bL.push();                                                                  // Becomes top and so ignored by search ... except last

//    for(int i = 0; i < splitBranchSize; i++)                                  // Build right child from parent
//     {z(); bT.shift();
//      bR.T.at(bR.tKey ).move(bT.T.at(bT.tKey ));
//      bR.T.at(bR.tData).move(bT.T.at(bT.tData));
//      bR.push();
//     }
//
//    bT.shift();
    bR.T.setIntInstruction(bR.tKey, 0);
    bT.lastElement();
    bR.T.at(bR.tData).move(bT.T.at(bT.tData));
    bR.T.setIntInstruction(bR.index, splitBranchSize);
    bR.setElementAt();
//  bR.push();                                                                  // Becomes top and so ignored by search ... except last

    bT.clear();                                                                 // Refer to new branches from root
    M.moveParallel
     (bT.T.at(bT.tKey) , T.at(parentKey),                                       /// Parallel possible
      bT.T.at(bT.tData), T.at(l));
    bT.push();
    bT.T.at(bT.tKey ).zero();
    bT.T.at(bT.tData).move(T.at(r));
    bT.push();                                                                  // Becomes top and so ignored by search ... except last
   }

  private void splitLeaf()                                                      // Split a leaf which is not the root
   {if (Assert) {tt(node_assertLeaf, node_splitLeaf); assertLeaf();}
    if (Halt) P.new If (T.at(node_splitLeaf))
     {void Else()
       {P.halt("Cannot split root with this method");
       }
     };

    tt(node_leafSize,   node_splitLeaf); leafSize();
    tt(node_leafIsFull, node_splitLeaf); leafIsFull();

    if (Halt) P.new If (T.at(leafIsFull))
     {void Else()
       {P.halt("Leaf is not full");
       }
     };

    tt(node_branchIsFull, splitParent); branchIsFull();

    if (Halt) P.new If (T.at(branchIsFull))
     {void Then()
       {P.halt("Leaf split parent must not be full");
       }
     };
    allocLeaf(); tt(l, allocLeaf);                                              // New  split out leaf

    tt(node_leafBase1, l);              leafBase1(); lL.base(T.at(leafBase1));  // The leaf being split into
    tt(node_leafBase2, node_splitLeaf); leafBase2(); lR.base(T.at(leafBase2));  // The leaf being split on the right

//  for (int i = 0; i < splitLeafSize; i++)                                     // Build left leaf
//   {z(); lR.shift();
//
//    M.moveParallel
//     (lL.T.at(lL.tKey ), lR.T.at(lR.tKey ),                                   /// Parallel possible
//      lL.T.at(lL.tData), lR.T.at(lR.tData));
//    lL.push();
//   }

    lR.splitLow(lL);                                                            // Split out the lower half

    lR.firstElement();
    lL. lastElement();
    tt(node_branchBase, splitParent); branchBase(); bT.base(T.at(branchBase));  // The parent branch
    P.new I()                                                                   // Splitting key
     {void a()
       {bT.T.at(bT.tKey).setInt((lR.T.at(lR.tKey).getInt() +
                                 lL.T.at(lL.tKey).getInt()) / 2);
       }
      String v()
       {return bT.T.at(bT.tKey).verilogLoad() + " <= "+
           "("+lR.T.at(lR.tKey).verilogLoad() + " + " +
               lL.T.at(lL.tKey).verilogLoad() + ") / 2;";
       }
     };
    M.moveParallel
     (bT.T.at(bT.tData), T.at(l),                                               /// Parallel possible
      bT.T.at(bT.index), T.at(index));
    bT.insertElementAt();                                                       // Insert new key, next pair in parent
   }

  private void splitBranch()                                                    // Split a branch which is not the root by splitting right to left
   {if (Assert) {tt(node_assertBranch, node_splitBranch); assertBranch();}
    tt(node_branchSize, node_splitBranch); branchSize();
    if (Halt) P.new If (T.at(node_splitBranch))
     {void Else()
       {P.halt("Cannot split root with this method");
       }
     };

    tt(node_branchIsFull, node_splitBranch); branchIsFull();
    if (Halt) P.new If (T.at(branchIsFull))
     {void Else()
       {P.halt("Branch is not full");
       }
     };

    tt(node_branchIsFull, splitParent); branchIsFull();
    if (Halt) P.new If (T.at(branchIsFull))
     {void Then()
       {P.halt("Branch split parent must not be full");
       }
     };

    z();
    allocBranch(); tt(l, allocBranch);
    tt(node_branchBase1, splitParent);      branchBase1(); bT.base(T.at(branchBase1)); // The parent branch
    tt(node_branchBase2, l);                branchBase2(); bL.base(T.at(branchBase2)); // The branch being split into
    tt(node_branchBase3, node_splitBranch); branchBase3(); bR.base(T.at(branchBase3)); // The branch being split

//  for (int i = 0; i < splitBranchSize; i++)                                   // Build left branch from right
//   {z(); bR.shift();
//    M.moveParallel
//     (bL.T.at(bL.tKey ), bR.T.at(bR.tKey ),                                   /// Parallel possible
//      bL.T.at(bL.tData), bR.T.at(bR.tData));
//    bL.push();
//   }

    bR.splitLow(bL);
    bL.zeroLastKey();

//  bR.shift();
//  bL.T.at(bL.tKey ).zero();
//  bL.T.at(bL.tData).move(bR.T.at(bR.tData));
//  bL.push();                                                                  // Build right branch - becomes top and so is ignored by search ... except last

    M.moveParallel                                                              /// Parallel possible
//   (bT.T.at(bT.tKey ), bR.T.at(bR.tKey),
     (bT.T.at(bT.tKey ), bL.T.at(bL.tKey),
      bT.T.at(bT.tData), T.at(l),
      bT.T.at(bT.index), T.at(index));
    bT.insertElementAt();
   }

  private void stealNotPossible(ProgramPA.Label end)                            // Cannot perform the requested steal
   {P.new If (T.at(stolenOrMerged))
     {void Then()
       {T.at(stolenOrMerged).zero();
        P.Goto(end);
       }
     };
   }

  private void stealFromLeft()                                                  // Steal from the left sibling of the indicated child if possible to give to the right - Dennis Moore, Dennis Moore, Dennis Moore.
   {z();
    P.new Block()
     {void code()
       {if (Assert) {tt(node_assertBranch, node_stealFromLeft); assertBranch();}
        P.new If (T.at(index))
         {void Else()
           {T.at(stolenOrMerged).zero();
            P.Goto(end);
           }
         };

        tt(node_branchBase, node_stealFromLeft); branchBase(); bT.base(T.at(branchBase));

        bT.T.at(bT.index).move(T.at(index));
        bT.T.at(bT.index).dec();
        bT.elementAt();

        T.at(l).move(bT.T.at(bT.tData));
        bT.T.at(bT.index).move(T.at(index));
        bT.elementAt();
        T.at(r).move(bT.T.at(bT.tData));

        tt(node_hasLeavesForChildren, node_stealFromLeft);
        hasLeavesForChildren();

        P.new If (T.at(hasLeavesForChildren))                                   // Children are leaves
         {void Then()
           {tt(node_leafBase1, l); leafBase1(); lL.base(T.at(leafBase1));
            tt(node_leafBase2, r); leafBase2(); lR.base(T.at(leafBase2));

            tt(node_leafSize, l); leafSize(); tt(nl, leafSize);
            tt(node_leafSize, r); leafSize(); tt(nr, leafSize);

            T.at(nr).greaterThanOrEqual(T.at(maxKeysPerLeaf), T.at(stolenOrMerged));
            stealNotPossible(end);

            T.at(nl).lessThan(T.at(two), T.at(stolenOrMerged));
            stealNotPossible(end);

            lL.lastElement();

            M.moveParallel
             (lR.T.at(lR.tKey ), lL.T.at(lL.tKey ),                             /// Parallel possible
              lR.T.at(lR.tData), lL.T.at(lL.tData));
            lR.unshift();                                                       // Increase right

            lL.pop();                                                           // Reduce left

            lL.T.at(lL.index).move(T.at(nl));
            lL.T.at(lL.index).dec(2);
            lL.elementAt();                                                     // Last key on left

            M.moveParallel
             (bT.T.at(bT.tKey) , lL.T.at(lL.tKey),                              /// Parallel possible
              bT.T.at(bT.tData), T.at(l),
              bT.T.at(bT.index), T.at(index));
            bT.T.at(bT.index).dec();
            bT.setElementAt();                                                  // Reduce key of parent of left
           }
          void Else()                                                           // Children are branches
           {z();
            tt(node_branchBase1, l); branchBase1(); bL.base(T.at(branchBase1));
            tt(node_branchBase2, r); branchBase2(); bR.base(T.at(branchBase2));
            tt(node_branchSize, l); branchSize(); tt(nl, branchSize);
            tt(node_branchSize, r); branchSize(); tt(nr, branchSize);

            T.at(nr).greaterThanOrEqual(T.at(maxKeysPerBranch), T.at(stolenOrMerged));
            stealNotPossible(end);
            T.at(nl).lessThan(T.at(two), T.at(stolenOrMerged));
            stealNotPossible(end);

            bL.lastElement();                                                   // Increase right with left top
            bT.T.at(bT.index).move(T.at(index));
            bT.elementAt();                                                     // Top key

            M.moveParallel
             (bR.T.at(bR.tKey) , bT.T.at(bT.tKey),                              /// Parallel possible
              bR.T.at(bR.tData), bL.T.at(bL.tData));
            bR.unshift();                                                       // Increase right with left top
            bL.pop();                                                           // Remove left top

            bR.firstElement();                                                  // Increase right with left top

            bT.T.at(bT.index).move(T.at(index));
            bT.T.at(bT.index).dec();

            bT.elementAt();                                                     // Parent key
            bR.T.at(bR.tKey).move(bT.T.at(bT.tKey));
            bR.T.at(bR.index).zero();
            bR.setElementAt();                                                  // Reduce key of parent of right

            bL.lastElement();                                                   // Last left key

            M.moveParallel
             (bT.T.at(bT.tKey) , bL.T.at(bL.tKey),                              /// Parallel possible
              bT.T.at(bT.tData), T.at(l),
              bT.T.at(bT.index), T.at(index));
            bT.T.at(bT.index).dec();
            bT.setElementAt();                                                  // Reduce key of parent of left
           }
         };
        z(); T.at(stolenOrMerged).ones();
       }
     };
   }

  private void stealFromRight()                                                 // Steal from the right sibling of the indicated child if possible
   {z();
    P.new Block()
     {void code()
       {if (Assert) {tt(node_assertBranch, node_stealFromRight); assertBranch();}
        tt(node_branchSize, node_stealFromRight); branchSize();
        T.at(index).equal(T.at(branchSize), T.at(stolenOrMerged));
        stealNotPossible(end);
        z();

        tt(node_branchBase, node_stealFromRight); branchBase();

        bT.base(T.at(branchBase));
        bT.T.at(bT.index).move(T.at(index));
        bT.elementAt();

        M.moveParallel
         (T.at(lk), bT.T.at(bT.tKey),                                           /// Parallel possible
          T.at(l) , bT.T.at(bT.tData));
        bT.T.at(bT.index).move(T.at(index));
        bT.T.at(bT.index).inc();
        bT.elementAt();

        M.moveParallel
         (T.at(rk), bT.T.at(bT.tKey),                                           /// Parallel possible
          T.at(r) , bT.T.at(bT.tData));

        tt(node_hasLeavesForChildren, node_stealFromRight);
        hasLeavesForChildren();
        P.new If(T.at(hasLeavesForChildren))                                    // Children are leaves
         {void Then()
           {z();
            tt(node_leafBase1, l); leafBase1(); lL.base(T.at(leafBase1));
            tt(node_leafBase2, r); leafBase2(); lR.base(T.at(leafBase2));
            tt(node_leafSize, l); leafSize(); tt(nl, leafSize);
            tt(node_leafSize, r); leafSize(); tt(nr, leafSize);

            T.at(nl).greaterThanOrEqual(T.at(maxKeysPerLeaf), T.at(stolenOrMerged));
            stealNotPossible(end);
            T.at(nr).lessThan(T.at(two), T.at(stolenOrMerged));                 // Steal not allowed because it would leave the right sibling empty
            stealNotPossible(end);

            z();
            lR.firstElement();                                                  // First element of right child
            M.moveParallel
             (lL.T.at(lL.tKey) , lR.T.at(lR.tKey),                              /// Parallel possible
              lL.T.at(lL.tData), lR.T.at(lR.tData));
            lL.push();                                                          // Increase left

            M.moveParallel
             (bT.T.at(bT.tKey) , lR.T.at(lR.tKey),                              /// Parallel possible
              bT.T.at(bT.tData), T.at(l),
              bT.T.at(bT.index), T.at(index));
            bT.setElementAt();                                                  // Swap key of parent
            lR.shift();                                                         // Reduce right
           }
          void Else()                                                           // Children are branches
           {z();
            tt(node_branchBase1, l); branchBase1(); bL.base(T.at(branchBase1));
            tt(node_branchBase2, r); branchBase2(); bR.base(T.at(branchBase2));
            tt(node_branchSize, l); branchSize(); tt(nl, branchSize);
            tt(node_branchSize, r); branchSize(); tt(nr, branchSize);

            T.at(nl).greaterThanOrEqual(T.at(maxKeysPerBranch), T.at(stolenOrMerged));
            stealNotPossible(end);
            T.at(nr).lessThan(T.at(two), T.at(stolenOrMerged));
            stealNotPossible(end);

            bL.lastElement();                                                   // Last element of left child
            M.moveParallel
             (bL.T.at(bL.tKey ), T.at(lk),                                      /// Parallel possible
              bL.T.at(bL.index), T.at(nl));
            bL.setElementAt();                                                  // Left top becomes real

            bR.firstElement();                                                  // First element of  right child

            bL.T.at(bL.tKey).zero();
            bL.T.at(bL.tData).move(bR.T.at(bR.tData));
            bL.push();                                                          // New top for left is ignored by search ,.. except last

            M.moveParallel
             (bT.T.at(bT.tKey ), bR.T.at(bR.tKey),                              /// Parallel possible
              bT.T.at(bT.tData), T.at(l));
            bT.T.at(bT.index).move(T.at(index));
            bT.setElementAt();                                                  // Swap key of parent
            bR.shift();                                                         // Reduce right
           }
         };
        z(); T.at(stolenOrMerged).ones();
       }
     };
   }

//D2 Merge                                                                      // Merge two nodes together and free the resulting free node

  private void mergeRoot()                                                      // Merge into the root
   {z();
    P.new Block()
     {void code()
       {final ProgramPA.Label Return = end;
        T.at(node_isLeaf).zero();                                               // Address the root
        isLeaf();
        P.new If (T.at(IsLeaf))                                                 // Confirm we are on a branch
         {void Then()
           {T.at(stolenOrMerged).zero();
            P.Goto(Return);
           }
         };
        T.at(node_branchSize).zero();
        branchSize();
        T.at(branchSize).greaterThanOrEqual(T.at(two), T.at(stolenOrMerged));   // Confirm we are on an almost empty root
        stealNotPossible(end);

        z();
        T.at(node_branchBase).zero(); branchBase();  bT.base(T.at(branchBase));
        bT.firstElement(); T.at(l).move(bT.T.at(bT.tData));
        bT. lastElement(); T.at(r).move(bT.T.at(bT.tData));

        T.setIntInstruction(node_hasLeavesForChildren, root);
        hasLeavesForChildren();
        P.new If (T.at(hasLeavesForChildren))                                   // Leaves
         {void Then()
           {tt(node_leafSize, l); leafSize(); tt(nl, leafSize);
            tt(node_leafSize, r); leafSize(); tt(nr, leafSize);

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
                   maxKeysPerLeaf()+") ? 'b1 : 'b0;";
               }
             };

            P.new If (T.at(mergeable))
             {void Then()
               {z(); bT.clear();
                T.at(node_leafBase1).zero(); leafBase1(); lT.base(T.at(leafBase1));
                tt(node_leafBase2, l);       leafBase2(); lL.base(T.at(leafBase2));
                tt(node_leafBase3, r);       leafBase3(); lR.base(T.at(leafBase3));
//concatenate
//                P.new Block()
//                 {void code()
//                   {for (int i = 0; i < maxKeysPerLeaf(); ++i)                  // Merge in left child leaf
//                     {tt(node_isEmpty, l); isEmpty(); P.GoOn(end,T.at(isEmpty));// Stop when left leaf  child is empty
//                      lL.shift();
//                      M.moveParallel
//                       (lT.T.at(lT.tKey ), lL.T.at(lL.tKey),                    /// Parallel possible
//                        lT.T.at(lT.tData), lL.T.at(lL.tData));
//                      lT.push();
//                     }
//                   }
//                 };
//
//                P.new Block()
//                 {void code()
//                   {for (int i = 0; i < maxKeysPerLeaf(); ++i)                  // Merge in right child leaf
//                     {tt(node_isEmpty, r); isEmpty(); P.GoOn(end,T.at(isEmpty));// Stop when right leaf child is empty
//                      lR.shift();
//                      M.moveParallel
//                       (lT.T.at(lT.tKey ), lR.T.at(lR.tKey),                    /// Parallel possible
//                        lT.T.at(lT.tData), lR.T.at(lR.tData));
//                      lT.push();
//                     }
//                   }
//                 };

                lT.concatenate(lL);                                             // Merge in left  child leaf
                lT.concatenate(lR);                                             // Merge in right child leaf

                T.setIntInstruction(node_setLeaf, root);  setLeaf();            // The root is now a leaf
                tt(node_free, l); free();                                       // Free the children
                tt(node_free, r); free();
                z(); T.at(stolenOrMerged).ones(); P.Goto(Return);
               }
             };
            z(); T.at(stolenOrMerged).zero(); //Goto P.Goto(Return);;
           }
          void Else()                                                           // Branches
           {tt(node_branchSize, l); branchSize(); tt(nl, branchSize);
            tt(node_branchSize, r); branchSize(); tt(nr, branchSize);

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
                   maxKeysPerBranch()+") ? 'b1 : 'b0;";
               }
             };

            P.new If (T.at(mergeable))
             {void Then()
               {tt(node_branchBase1, l); branchBase1(); bL.base(T.at(branchBase1));
                tt(node_branchBase2, r); branchBase2(); bR.base(T.at(branchBase2));
                bT.firstElement();
                T.at(parentKey).move(bT.T.at(bT.tKey));
                bT.clear();

//              P.new Block()
//               {void code()
//                 {for (int i = 0; i < maxKeysPerBranch(); ++i)                // Merge left child branch
//                   {tt(node_isEmpty, l); isEmpty(); P.GoOn(end,T.at(isEmpty));// Stop when left branch child is empty except for top
//                    bL.shift();
//                    M.moveParallel
//                     (bT.T.at(bT.tKey) , bL.T.at(bL.tKey),                    /// Parallel possible
//                      bT.T.at(bT.tData), bL.T.at(bL.tData));
//                    bT.push();
//                   }
//                 }
//               };
//
//              bL.lastElement();
//              M.moveParallel
//               (bT.T.at(bT.tKey ), T.at(parentKey),                           /// Parallel possible
//                bT.T.at(bT.tData), bL.T.at(bL.tData));
//              bT.push();

                bT.concatenate(bL);
                bT.T.at(bT.tKey).move(T.at(parentKey));
                bT.setLastKey();

//              P.new Block()
//               {void code()
//                 {for (int i = 0; i < maxKeysPerBranch(); ++i)                // Merge right child branch
//                   {tt(node_isEmpty, r); isEmpty(); P.GoOn(end,T.at(isEmpty));// Stop when right branch child is empty except for top
//                    bR.shift();
//                    M.moveParallel
//                     (bT.T.at(bT.tKey ), bR.T.at(bR.tKey),                    /// Parallel possible
//                      bT.T.at(bT.tData), bR.T.at(bR.tData));
//                    bT.push();
//                   }
//                 }
//               };
//
//              bR.lastElement();                                               // Top next
//
//              bT.T.at(bT.tKey ).zero();
//              bT.T.at(bT.tData).move(bR.T.at(bR.tData));
//              bT.push();                                                      // Top so ignored by search ... except last

                bT.concatenate(bR);
                bT.zeroLastKey();

                tt(node_free, l); free();                                       // Free the children
                tt(node_free, r); free();
                z(); T.at(stolenOrMerged).ones(); P.Goto(Return);
               }
             };
            z(); T.at(stolenOrMerged).zero(); //Goto P.Goto(Return);
           }
         };
       };
     };
   }

  private void mergeLeftSibling()                                               // Merge the left sibling
   {z();
    P.new Block()
     {void code()
       {if (Assert) {tt(node_assertBranch, node_mergeLeftSibling); assertBranch();}
        T.at(index).isZero(T.at(stolenOrMerged));
        stealNotPossible(end);
        tt(node_branchSize, node_mergeLeftSibling);
        branchSize();

        T.at(index).greaterThan(T.at(branchSize), T.at(stolenOrMerged));
        if (Halt) P.new If (T.at(stolenOrMerged))
         {void Then()
           {P.halt("Index too big");
           }
         };
        T.at(branchSize).lessThan(T.at(two), T.at(stolenOrMerged));
        stealNotPossible(end);

        z();
        tt(node_branchBase, node_mergeLeftSibling); branchBase(); bT.base(T.at(branchBase));
        bT.T.at(bT.index).move(T.at(index));
        bT.T.at(bT.index).dec();
        bT.elementAt();
        T.at(l).move(bT.T.at(bT.tData));

        bT.T.at(bT.index).move(T.at(index));
        bT.elementAt();
        T.at(r).move(bT.T.at(bT.tData));

        tt(node_hasLeavesForChildren, node_mergeLeftSibling);
        hasLeavesForChildren();
        P.new If (T.at(hasLeavesForChildren))                                   // Children are leaves
         {void Then()
           {z();
            tt(node_leafBase1, l); leafBase1(); lL.base(T.at(leafBase1));
            tt(node_leafBase2, r); leafBase2(); lR.base(T.at(leafBase2));
            tt(node_leafSize, l); leafSize(); tt(nl, leafSize);
            tt(node_leafSize, r); leafSize(); tt(nr, leafSize);

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
                   maxKeysPerLeaf()+") ? 'b1 : 'b0;";
               }
             };
            stealNotPossible(end);

//          P.new Block()                                                       // Transfer left to right
//           {void code()
//             {for (int i = 0; i < maxKeysPerLeaf(); i++)
//               {z();
//                tt(node_leafSize, l);
//                leafSize();
//                P.GoOff(end, T.at(leafSize));
//                lL.pop();
//                lR.T.at(lR.tKey ).move(lL.T.at(lL.tKey));
//                lR.T.at(lR.tData).move(lL.T.at(lL.tData));
//                lR.unshift();
//               }
//             }
//           };
            lR.prepend(lL);
           }
          void Else()                                                           // Children are branches
           {z();
            tt(node_branchBase1, l); branchBase1(); bL.base(T.at(branchBase1));
            tt(node_branchBase2, r); branchBase2(); bR.base(T.at(branchBase2));
            tt(node_branchSize, l); branchSize(); tt(nl, branchSize);
            tt(node_branchSize, r); branchSize(); tt(nr, branchSize);

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
                   maxKeysPerBranch()+") ? 'b1 : 'b0;";
               }
             };
            stealNotPossible(end);

            z();
            bT.T.at(bT.index).move(T.at(index));                                // Top key
            bT.T.at(bT.index).dec();                                            // Top key
            bT.elementAt();                                                     // Top key

            bL.lastElement();                                                   // Last element of left child
            bR.T.at(bR.tKey ).move(bT.T.at(bT.tKey));
            bR.T.at(bR.tData).move(bL.T.at(bL.tData));
            bR.unshift();                                                       // Left top to right
            bL.pop();                                                           // Remove left top

//          P.new Block()                                                       // Transfer left to right
//           {void code()
//             {for (int i = 0; i < maxKeysPerBranch(); i++)                    // Transfer left to right
//               {z();
//                tt(node_branchSize, l);                                       // Top is always present
//                branchSize();
//                bL.pop();
//                M.moveParallel
//                 (bR.T.at(bR.tKey ), bL.T.at(bL.tKey),                        /// Parallel possible
//                  bR.T.at(bR.tData), bL.T.at(bL.tData));
//                bR.unshift();
//                P.GoOff(end, T.at(branchSize));                               // If zero then we just moved top
//               }
//             }
//           };
            bR.prepend(bL);
           }
         };
        tt(node_free, l); free();                                               // Free the empty left node
        bT.T.at(bT.index).move(T.at(index));
        bT.T.at(bT.index).dec();

        bT.removeElementAt();                                                   // Reduce parent on left
        z(); T.at(stolenOrMerged).ones();
       }
     };
   }

  private void mergeRightSibling()                                              // Merge the right sibling
   {z();
    P.new Block()
     {void code()
       {if (Assert) {tt(node_assertBranch, node_mergeRightSibling);}
        assertBranch();
        tt(node_branchSize, node_mergeRightSibling);
        branchSize();
        T.at(index).greaterThanOrEqual(T.at(branchSize), T.at(stolenOrMerged));
        stealNotPossible(end);
        T.at(branchSize).lessThan(T.at(two), T.at(stolenOrMerged));
        stealNotPossible(end);

        z();
        tt(node_branchBase, node_mergeRightSibling); branchBase();
        bT.base(T.at(branchBase));
        bT.T.at(bT.index).move(T.at(index));
        bT.elementAt();
        T.at(l).move(bT.T.at(bT.tData));
        bT.T.at(bT.index).move(T.at(index));
        bT.T.at(bT.index).inc();
        bT.elementAt();
        T.at(r).move(bT.T.at(bT.tData));

        tt(node_hasLeavesForChildren, node_mergeRightSibling);
        hasLeavesForChildren();
        P.new If (T.at(hasLeavesForChildren))                                   // Children are leaves
         {void Then()
           {tt(node_leafBase1, l); leafBase1(); lL.base(T.at(leafBase1));
            tt(node_leafBase2, r); leafBase2(); lR.base(T.at(leafBase2));

            tt(node_leafSize, l); leafSize(); tt(nl, leafSize);
            tt(node_leafSize, r); leafSize(); tt(nr, leafSize);

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
                   maxKeysPerLeaf()+") ? 'b1 : 'b0;";
               }
             };
            stealNotPossible(end);

//          P.new Block()
//           {void code()
//             {for (int i = 0; i < maxKeysPerLeaf(); i++)                      // Transfer right to left
//               {z();
//                tt(node_leafBase, r); leafSize(); P.GoOff(end, T.at(leafSize));
//                lR.shift();
//                M.moveParallel
//                 (lL.T.at(lL.tKey ), lR.T.at(lR.tKey),                        /// Parallel possible
//                  lL.T.at(lL.tData), lR.T.at(lR.tData));
//                lL.push();
//               }
//             }
//           };
            lL.concatenate(lR);
           }
          void Else()                                                           // Children are branches
           {tt(node_branchBase1, l); branchBase1(); bL.base(T.at(branchBase1));
            tt(node_branchBase2, r); branchBase2(); bR.base(T.at(branchBase2));
            tt(node_branchSize, l); branchSize(); tt(nl, branchSize);
            tt(node_branchSize, r); branchSize(); tt(nr, branchSize);

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
                   maxKeysPerBranch()+") ? 'b1 : 'b0;";
               }
             };
            stealNotPossible(end);

            z(); bL.lastElement();                                              // Last element of left child
            z(); bT.T.at(bT.index).move(T.at(index));
            z(); bT.elementAt();                                                // Parent dividing element

            bL.T.at(bL.tKey).move(bT.T.at(bT.tKey));
            bL.T.at(bL.index).move(T.at(nl));
            bL.setElementAt();                                                  // Re-key left top

//          P.new Block()
//           {void code()
//             {for (int i = 0; i < maxKeysPerBranch()+1; i++)                  // Transfer right to left including top
//               {z();
//                tt(node_branchSize, r);                                       // Top is always present
//                branchSize();
//                bR.shift();
//                M.moveParallel
//                 (bL.T.at(bL.tKey) , bR.T.at(bR.tKey),                        /// Parallel possible
//                  bL.T.at(bL.tData), bR.T.at(bR.tData));
//                bL.push();
//                P.GoOff(end, T.at(branchSize));                               // If zero then we just moved top
//               }
//             }
//           };
//          P.new I() {void a() {say("AAAA");}};
            bL.concatenate(bR);
           } // Else
         };

        tt(node_free, r);
        free();                                                                 // Free the empty right node

        bT.T.at(bT.index).move(T.at(index));
        bT.T.at(bT.index).inc();
        bT.elementAt();
                                                                                /// Parallel possible
        M.moveParallel
         (T.at(parentKey  ), bT.T.at(bT.tKey),                                  // One up from dividing point in parent
          bT.T.at(bT.index), T.at(index));
        bT.elementAt();                                                         // Dividing point in parent

        bT.T.at(bT.tKey).move(T.at(parentKey));
        bT.setElementAt();                                                      // Install key of right sibling in this child

        bT.T.at(bT.index).move(T.at(index));                                    // Reduce parent on right
        bT.T.at(bT.index).inc();
        bT.removeElementAt();                                                   // Reduce parent on right
        z(); T.at(stolenOrMerged).ones();
       }
     };
   }

//D2 Balance                                                                    // Balance the tree by merging and stealing

  private void balance()                                                        // Augment the indexed child so it has at least two children in its body
   {z();
    P.new Block()
     {void code()
       {if (Assert) {tt(node_assertBranch, node_balance); assertBranch();}
        tt(node_branchSize,   node_balance); branchSize();
        T.at(index).greaterThan(T.at(branchSize), T.at(stolenOrMerged));
        if (Halt) P.new If (T.at(stolenOrMerged))
         {void Then()
           {P.halt("Index too big");
           }
         };

        tt(node_branchBase, node_balance);
        branchBase();
        bT.base(T.at(branchBase));                                              // Address parent

        bT.T.at(bT.index).move(T.at(index));                                    // Index child to be augmented
        bT.elementAt();

        T.at(node_isLow).move(bT.T.at(bT.tData));                               // Child must have only one entry to be balanced
        isLow();
        P.GoOff(end, T.at(isLow));
        tt(node_stealFromLeft,     node_balance); stealFromLeft    (); P.GoOn(end, T.at(stolenOrMerged));
        tt(node_stealFromRight,    node_balance); stealFromRight   (); P.GoOn(end, T.at(stolenOrMerged));
        tt(node_mergeLeftSibling,  node_balance); mergeLeftSibling (); P.GoOn(end, T.at(stolenOrMerged));
        tt(node_mergeRightSibling, node_balance); mergeRightSibling(); P.GoOn(end, T.at(stolenOrMerged));
        if (Halt) P.halt("Unable to balance child");
       }
     };
   }

//D1 Array                                                                      // Key, data pairs in the tree as an array

  private class ArrayElement                                                    // A key, data pair in the btree as an array element
   {final int i, key, data;
    ArrayElement(int I, int Key, int Data)
     {i = I; key = Key; data = Data;
     }
    public String toString()
     {return "("+i+", key:"+key+" data:"+data+")\n";
     }
   }

  private Stack<ArrayElement> toArray()                                         // Key, data pairs in the tree as an array
   {z();
    final Stack<ArrayElement> s = new Stack<>();
    T.at(node_isLeaf).setInt(root); isLeaf();
    if (T.at(IsLeaf).isOnes()) {z();   leafToArray(root, s);}
    else        {z(); branchToArray(root, s);}
    return s;
   }

//D1 Print                                                                      // Print a BTree horizontally

   private String printBoxed()                                                  // Print a tree in a box
    {final String  s = toString();
     final int     n = longestLine(s)-1;
     final String[]L = s.split("\n");
     final StringBuilder t = new StringBuilder();
     t.append("+"); t.append("-".repeat(n)); t.append("+\n");
     for(String l : L) t.append("| "+l+"\n");
     t.append("+"); t.append("-".repeat(n)); t.append("+\n");
     return t.toString();
    }

  private void padStrings(Stack<StringBuilder> S, int level)                    // Pad the strings at each level of the tree so we have a vertical face to continue with - a bit like Marc Brunel's tunneling shield
   {final int N = level * linesToPrintABranch + maxKeysPerLeaf();               // Number of lines we might want
    for (int i = S.size(); i <= N; ++i) S.push(new StringBuilder());            // Make sure we have a full deck of strings
    int m = 0;                                                                  // Maximum length
    for (StringBuilder s : S) m = m < s.length() ? s.length() : m;              // Find maximum length
    for (StringBuilder s : S)                                                   // Pad each string to maximum length
     {if (s.length() < m) s.append(" ".repeat(m - s.length()));                 // Pad string to maximum length
     }
   }

  String printCollapsed(Stack<StringBuilder> S)                                 // Collapse horizontal representation into a string
   {z();
    final StringBuilder t = new StringBuilder();                                // Print the lines of the tree that are not blank
    for  (StringBuilder s : S)
     {z();
      final String l = s.toString();
      if (l.isBlank()) continue;
      t.append(l+"|\n");
     }
    return t.toString();
   }

  private String print()                                                        // Print a tree horizontally
   {final BtreePA  p = this;
    final BtreeSML s = new BtreeSML()                                           // Create a btree of the same dimensions in aclass that can already print the btree so that we do not have to deal with the complexity of printing from assembler
     {int maxSize         () {return p.maxSize         ();}
      int maxKeysPerLeaf  () {return p.maxKeysPerLeaf  ();}
      int maxKeysPerBranch() {return p.maxKeysPerBranch();}
      int bitsPerKey      () {return p.bitsPerKey      ();}
      int bitsPerData     () {return p.bitsPerData     ();}
      int bitsPerNext     () {return p.bitsPerNext       ;}
      int bitsPerSize     () {return p.bitsPerSize       ;}
      Memory   memory     () {return p.M.memory();}                             // The memory to be used by the btree
     };
    s.fixMemory(p.M.memory());
    return s.toString();
   }

//D1 Find                                                                       // Find the data associated with a key.

  public void find()                                                            // Find the leaf associated with a key in the tree
   {z();
    P.new Block()
     {void code()
       {final ProgramPA.Label Return = end;
        T.at(node_isLeaf).zero();
        isLeaf();
        P.new If (T.at(IsLeaf))                                                 // The root is a leaf
         {void Then()
           {tt(search, Key);
            T.setIntInstruction(node_findEqualInLeaf, root);
            findEqualInLeaf();
            T.at(find).zero();                                                  // Found in root
            P.Goto(Return);
           }
         };
        T.at(parent).zero();                                                    // Parent starts at root which is now known to be a branch
        T.at(findDepth).zero();                                                 // Limit number of levels searched

        P.new Block()
         {void code()
           {T.at(findDepth).inc();
            T.at(findDepth).greaterThan(T.at(MaxDepth), T.at(pastMaxDepth));
            P.GoOn(end, T.at(pastMaxDepth));                                    // Prevent runaway searches

            tt(search, Key);
            tt(node_findFirstGreaterThanOrEqualInBranch, parent);
            findFirstGreaterThanOrEqualInBranch();                              // Find next child in search path of key
            tt(child, next);
            tt(node_isLeaf, child);
            isLeaf(T.at(next));
            P.new If (T.at(IsLeaf))                                             // Found the containing leaf
             {void Then()
               {tt(search, Key);
                tt(node_findEqualInLeaf, child);
                findEqualInLeaf();
                tt(find, child);
                P.Goto(Return);
               }
             };
            tt(parent, child);                                                  // Step down to lower branch
            P.Goto(start);
           }
         };
        if (Halt) P.halt("Search did not terminate in a leaf");
       }
     };
   }

  private void findAndInsert()                                                  // Find the leaf that should contain this key and insert or update it is possible
   {P.new Block()
     {void code()
       {final ProgramPA.Label Return = end;
        find();
        tt(leafFound, find);                                                    // Find the leaf that should contain this key
        tt(node_leafBase, leafFound); leafBase(); lT.base(T.at(leafBase));

        P.new If (T.at(found))                                                  // Found the key in the leaf so update it with the new data
         {void Then()
           {z();
            M.moveParallel
             (lT.T.at(lT.tKey ), T.at(Key),                                     /// Parallel possible
              lT.T.at(lT.tData), T.at(Data),
              lT.T.at(lT.index), T.at(index));
            lT.setElementAt();
            T.at(success).ones();
            T.at(inserted).zero();
            tt(findAndInsert, leafFound);
            P.Goto(Return);
           }
         };

        tt(node_leafIsFull, leafFound); leafIsFull();
        P.new If(T.at(leafIsFull))                                              // Leaf is not full so we can insert immediately
         {void Else()
           {z();
            tt(search, Key);
            tt(node_findFirstGreaterThanOrEqualInLeaf, leafFound);
                    findFirstGreaterThanOrEqualInLeaf();
            P.new If(T.at(found))                                               // Overwrite existing key
             {void Then()
               {z();
                M.moveParallel
                 (lT.T.at(lT.tKey ), T.at(Key),                                 /// Parallel possible
                  lT.T.at(lT.tData), T.at(Data),
                  lT.T.at(lT.index), T.at(first));
                lT.insertElementAt();
               }
              void Else()                                                       // Insert into position
               {z();
                M.moveParallel
                 (lT.T.at(lT.tKey ), T.at(Key),                                 /// Parallel possible
                  lT.T.at(lT.tData), T.at(Data));
                lT.push();
               }
             };
            T.at(success).ones();
            tt(findAndInsert, leafFound);
            P.Goto(Return);
           }
         };
        T.at(success).zero();
       }
     };
   }

//D1 Insertion                                                                  // Insert a key, data pair into the tree or update and existing key with a new datum

  public void put()                                                             // Insert a key, data pair into the tree or update and existing key with a new datum
   {P.new Block()
     {void code()
       {final ProgramPA.Label Return = end;
        findAndInsert();                                                        // Try direct insertion with no modifications to the shape of the tree
        P.GoOn(Return, T.at(success));                                          // Inserted or updated successfully
        T.at(node_isFull).zero(); isFull();                                     // Start the insertion at the root(), after splitting it if necessary
        P.new If (T.at(isFull))                                                 // Start the insertion at the root(), after splitting it if necessary
         {void Then()
           {T.at(node_isLeaf).zero();
            isLeaf();
            P.new If (T.at(IsLeaf))
             {void Then() {splitLeafRoot();}
              void Else() {splitBranchRoot();}
             };
            z();
            findAndInsert();                                                    // Splitting the root() might have been enough
            P.GoOn(Return, T.at(success));                                      // Inserted or updated successfully
           }
         };

        z(); T.at(parent  ).zero();
        z(); T.at(putDepth).zero();

        P.new Block()                                                           // Step down through the tree, splitting as we go
         {void code()
           {T.at(putDepth).inc();
            T.at(putDepth).greaterThan(T.at(MaxDepth), T.at(pastMaxDepth));
            P.GoOn(end, T.at(pastMaxDepth));                                    // Prevent runaway searches

            tt(search, Key);
            tt(node_findFirstGreaterThanOrEqualInBranch, parent);
                    findFirstGreaterThanOrEqualInBranch();
            tt(child, next);
            tt(node_isLeaf, child); isLeaf(T.at(next));
            P.new If (T.at(IsLeaf))                                             // Reached a leaf
             {void Then()
               {z();
                tt(splitParent, parent);
                tt(index, first);
                tt(node_splitLeaf, child);

                splitLeaf();                                                    // Split the child leaf
                findAndInsert();
                merge();
                P.Goto(Return);
               }
             };
            z();
            tt(node_isFull, child); isFull();
            P.new If (T.at(isFull))                                             // Step down, splitting full branches as we go
             {void Then()
               {tt(splitParent, parent);
                tt(index, first);
                tt(node_splitBranch, child);
                splitBranch();                                                  // Split the child branch in the search path for the key from the parent so the the search path does not contain a full branch above the containing leaf

                tt(search, Key);
                tt(node_findFirstGreaterThanOrEqualInBranch, parent);
                findFirstGreaterThanOrEqualInBranch();                          // Perform the step down again as the split will have altered the local layout
                tt(parent, next);
               }
              void Else() {z(); tt(parent, child);}                             // Step down directly as no split was required
             };
            P.Goto(start);
           }
         };
        if (Halt) P.halt("Fallen off the end of the tree");                     // The tree must be missing a leaf
       }
     };
   }

//D1 Deletion                                                                   // Delete a key, data pair from the tree

  private void findAndDelete()                                                  // Delete a key from the tree and returns its data if present without modifying the shape of tree
   {P.new Block()
     {void code()
       {find();                                                                 // Try direct insertion with no modifications to the shape of the tree
//      P.new If (T.at(found)) {void Else() {P.Goto(end);}};                    // Key not found so nothing to delete
        P.GoOff(end, T.at(found));                                              // Key not found so nothing to delete
        z(); tt(node_leafBase, find); leafBase(); lT.base(T.at(leafBase));      // The leaf that contains the key
        lT.T.at(lT.index).move(T.at(index)); lT.elementAt();                    // Position in the leaf of the key

        T.at(Data).move(lT.T.at(lT.tData));                                     // Key, data pairs in the leaf
        lT.removeElementAt();                                                   // Remove the key, data pair from the leaf
       }
     };
   }

  public void delete()                                                          // Delete a key from the tree and return its associated Data if the key was found.
   {P.new Block()                                                               // Step down through the tree, splitting as we go
     {void code()
       {final ProgramPA.Label Return = end;
        T.at(node_mergeRoot).zero(); mergeRoot();

        T.at(node_isLeaf).zero(); isLeaf();
        P.new If (T.at(IsLeaf))                                                 // Find and delete directly in root as a leaf
         {void Then()
           {z();
            findAndDelete();
            tt(deleted, found);
            P.Goto(Return);
           }
         };

        T.at(parent).zero();                                                    // Start at root
        T.at(deleteDepth).zero();

        P.new Block()                                                           // Step down through the tree, splitting as we go
         {void code()
           {T.at(deleteDepth).inc();
            T.at(deleteDepth).greaterThan(T.at(MaxDepth), T.at(pastMaxDepth));
            P.GoOn(end, T.at(pastMaxDepth));                                    // Prevent runaway searches

            tt(search, Key);
            tt(node_findFirstGreaterThanOrEqualInBranch, parent);
            findFirstGreaterThanOrEqualInBranch();

            tt(index, first); tt(node_balance, parent); balance();              // Make sure there are enough entries in the parent to permit a deletion
            tt(child, next);
            tt(node_isLeaf, child); isLeaf(T.at(next));
            P.new If (T.at(IsLeaf))                                             // Reached a leaf
             {void Then()
               {z();
                findAndDelete();
                tt(deleted, found);
                merge();
                P.Goto(Return);
               }
             };
            tt(parent, child);
            P.Goto(start);
           }
         };
        if (Halt) P.halt("Fallen off the end of the tree");                     // The tree must be missing a leaf
       };
     };
   }

//D1 Merge                                                                      // Merge along the specified search path

  private void merge()                                                          // Merge along the specified search path
   {P.new Block()                                                               // Step down through the tree, splitting as we go
     {void code()
       {final ProgramPA.Label Return = end;
        mergeRoot();
        T.at(parent).zero();                                                    // Start at root

        T.at(mergeDepth).zero();
        P.new Block()                                                           // Step down through the tree, splitting as we go
         {void code()
           {T.at(mergeDepth).inc();
            T.at(mergeDepth).greaterThan(T.at(MaxDepth), T.at(pastMaxDepth));
            P.GoOn(end, T.at(pastMaxDepth));                                    // Prevent runaway searches

            tt(node_isLeaf, parent);
            isLeaf(T.at(parent));
            P.GoOn(Return, T.at(IsLeaf));                                       // Reached a leaf
            tt(node_branchSize, parent);
            branchSize();

            T.at(mergeIndex).zero();                                            // Index of child being merged
            P.new Block()                                                       // Try merging each sibling pair which might change the size of the parent
             {void code()
               {tt(node_branchSize, parent);
                branchSize();
                T.at(mergeIndex).greaterThanOrEqual(T.at(branchSize), T.at(nodeMerged));
                P.GoOn(end, T.at(nodeMerged));                                  // All sequential pairs of siblings have been offered a chance to merge

                T.at(index).move(T.at(mergeIndex));
                tt(node_mergeLeftSibling, parent);
                mergeLeftSibling();
                P.new If (T.at(stolenOrMerged))                                 // A successful merge of the left  sibling reduces the current index and the upper limit
                 {void Then()
                   {T.at(mergeIndex).dec();
                   }
                 };
                T.at(index).move(T.at(mergeIndex));
                tt(node_mergeRightSibling, parent);
                mergeRightSibling();                                            // A successful merge of the right sibling maintains the current position but reduces the upper limit
                tt(node_branchSize,        parent);
                branchSize();

                T.at(mergeIndex).inc();
                P.Goto(start);
               }
             };
            tt(search, Key);
            tt(node_findFirstGreaterThanOrEqualInBranch, parent);
            findFirstGreaterThanOrEqualInBranch();                              // Step down
            tt(parent, next);
            P.Goto(start);
           }
         };
        if (Halt) P.halt("Fallen off the end of the tree");                     // The tree must be missing a leaf
       }
     };
   }

//D1 Verilog                                                                    // Generate verilog code that implements the instructions used to manipulate a btree

  String stuckMemories()                                                        // Declare variables holding the base addresses of all based memory elements
   {final StringBuilder s = new StringBuilder();
    final int B = branchTransactions.length;
    final int L =   leafTransactions.length;
    for  (int b = 0; b < B; b++) s.append(stuckMemory(branchTransactions[b]));
    for  (int l = 0; l < L; l++) s.append(stuckMemory(  leafTransactions[l]));
    return s.toString();
   }

  String stuckMemoryInitialization()                                            // Initialize based memory
   {final StringBuilder s = new StringBuilder();
    final int B = branchTransactions.length;
    final int L =   leafTransactions.length;
    for  (int b = 0; b < B; b++) s.append(stuckMemoryInitialization(branchTransactions[b]));
    for  (int l = 0; l < L; l++) s.append(stuckMemoryInitialization(  leafTransactions[l]));
    return s.toString();
   }

  String stuckMemory(StuckPA s)                                                 // Base address variable for one stuck
   {return"reg ["+bitsPerAddress+":0] "+s.M.baseName() + ";\n"+
      s.C.declareVerilog()+
      s.T.declareVerilog()+
      s.M.copyVerilogDec();
   }

  String stuckMemoryInitialization(StuckPA s)                                   // Initialization for one stuck
   {return s.M.baseName()+" <= 0;"+
           s.C.name()    +" <= 0;"+
           s.T.name()    +" <= 0;"+traceComment();
   }

  abstract class GenVerilog                                                     // Generate verilog
   {final String       project;                                                 // Project name - used to generate file names
    final String        folder;                                                 // Folder in which to place project
    final String projectFolder;                                                 // Folder in which to place verilog
    final String sourceVerilog;                                                 // Source verilog file
    final String   testVerilog;                                                 // Verilog test bench file
    final String         mFile;                                                 // Folder in which to place include for btree memory
    final String         tFile;                                                 // Folder in which to place include for btree transaction memory
    final String     testsFile;                                                 // File in which to place verilog test results sumamry
    final String     traceFile;                                                 // Folder in which to place verilog execution trace file
    final String javaTraceFile;                                                 // Folder in which to place java    execution trace file
    final ProgramPA    program;                                                 // Program associated with this tree

    abstract int Key     ();                                                    // Input key value
    abstract int Data    ();                                                    // Input data value
    abstract int data    ();                                                    // Expected output data value
    abstract int maxSteps();                                                    // Maximum number if execution steps
    abstract int expSteps();                                                    // Expected number of steps

    GenVerilog(String Project, String Folder)                                   // Generate verilog
     {project = Project; folder = Folder; program = P;
      //program.optimize();                                                     // Optimize not reliable enough yet and does not make a big enough differnce versus algorithmic improvements

      projectFolder = ""+Paths.get(folder, project, ""+Key());
      sourceVerilog = ""+Paths.get(projectFolder, project+Verilog.ext);
        testVerilog = ""+Paths.get(projectFolder, project+Verilog.testExt);
              mFile = ""+Paths.get(projectFolder, "includes", "M"+Verilog.header);
              tFile = ""+Paths.get(projectFolder, "includes", "T"+Verilog.header);
          testsFile = ""+Paths.get(projectFolder, "tests.txt");
          traceFile = ""+Paths.get(projectFolder, "trace.txt");
      javaTraceFile = ""+Paths.get(projectFolder, "traceJava.txt");
      makePath(projectFolder);

      final StringBuilder s = new StringBuilder();
      s.append("""
//-----------------------------------------------------------------------------
// Database on a chip
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2025
//------------------------------------------------------------------------------
`timescale 10ps/1ps
module $project(reset, stop, clock, pfd, Key, Data, data, found);               // Database on a chip
  input                 reset;                                                  // Restart the program run sequence when this goes high
  input                 clock;                                                  // Program counter clock
  input            [2:0]pfd;                                                    // Put, find delete
  input [$bitsPerKey :0]Key;                                                    // Input key
  input [$bitsPerData:0]Data;                                                   // Input data
  output                 stop;                                                  // Program has stopped when this goes high
  output[$bitsPerData:0]data;                                                   // Output data
  output                found;                                                  // Whether the key was found on put, find delete

  `include "M.vh"                                                               // Memory holding a pre built tree from test_dump()
  `include "T.vh"                                                               // Transaction memory which is initialized to some values to reduce the complexity of Memory at by treating constants as variables

  integer  step;                                                                // Program counter
  integer steps;                                                                // Number of steps executed
  integer traceFile;                                                            // File to write trace to
  reg   stopped;                                                                // Set when we stop
  assign stop  = stopped > 0 ? 1 : 0;                                           // Stopped execution
  assign found = $T[$found_at];                                                 // Found the key
  assign data  = $T[$data_at+:$data_width];                                     // Data associated with key found

$stuckBases

  always @ (posedge reset, posedge clock) begin                                 // Execute next step in program

    if (reset) begin                                                            // Reset
      step      = 0;
      steps    <= 0;
      stopped  <= 0;
      $initialize_memory_M();                                                   // Initialize btree memory
      $initialize_memory_T();                                                   // Initialize btree transaction
      traceFile = $fopen("$traceFile", "w");                                    // Open trace file
      if (!traceFile) $fatal(1, "Cannot open trace file $traceFile");
      $stuckInitialization
    end
    else begin                                                                  // Run
      $display            ("%4d  %4d  %b", steps, step, $M);                    // Trace execution
      $fdisplay(traceFile, "%4d  %4d  %b", steps, step, $M);                    // Trace execution in a file
      case(step)                                                                // Case statements to select the code for the current instruction
""");

      final String p = " ".repeat(10);                                          // Indentation for Verilog
      final String q = " ".repeat(16);

      for(int i = 0; i < program.code.size(); ++i)                              // Write each instruction
       {final Stack<ProgramPA.I> I = program.code.elementAt(i);                 // The block of parallel instructions to write
        final int N = I.size();
        if (N > 1)
         {final StringBuilder t = new StringBuilder();

          for(ProgramPA.I j : I) t.append(q+j.v()+j.traceComment() + "\n");
          s.append(String.format("%s%5d : begin\n", p, i));
          s.append(q+"  "+t);
          s.append(q+"end\n");
         }
        else if (N == 1)
         {final String t = I.firstElement().v()+traceComment();
          s.append(String.format("%s%5d : begin %s end\n", p, i, t));           // Bracket instructions in this block with op code
         }
       }
      s.append("        default : begin stopped <= 1; /* end of execution */ end\n"); // Any invalid instruction address causes the program to halt
      s.append("""
      endcase
      step   = step  + 1;
      steps <= steps + 1;
    end // Execute
  end // Always
endmodule
""");

      final StringBuilder t = new StringBuilder();                              // Test bench
      t.append("""
//-----------------------------------------------------------------------------
// Database on a chip test bench
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2025
//------------------------------------------------------------------------------
`timescale 10ps/1ps
module $project_tb;                                                             // Test bench for database on a chip
  reg                  reset;                                                   // Restart the program run sequence when this goes high
  reg                  stop;                                                    // Program has stopped when this goes high
  reg                  clock;                                                   // Program counter clock
  reg             [2:0]pfd;                                                     // Put, find delete
  reg  [$bitsPerKey :0]Key  = $Key;                                             // Input key
  reg  [$bitsPerData:0]Data = $Data;                                            // Input data
  reg  [$bitsPerData:0]data;                                                    // Output data
  reg                  found;                                                   // Whether the key was found on put, find delete
  integer testResults;                                                          // Test results file

  $project a1(.reset(reset), .stop(stop), .clock(clock),                        // Connect to the module
    .Key(Key), .Data(Data), .data(data), .found(found));

  initial begin                                                                 // Test the module
    reset = 0; #1; reset = 1; #1; reset = 0; #1;                                // Reset the module
    execute();
  end

  task execute;                                                                 // Clock the module until it says it has stopped
    integer step;
    begin
      for(step = 0; step < $maxSteps && !stop ; step = step + 1) begin
        clock = 0; #1; clock = 1; #1;
      end
      if (stop) begin                                                           // Stopped
        testResults = $fopen("$testsFile", "w");
        $fdisplay(testResults, "Steps=%1d\\nKey=%1d\\ndata=%1d\\n",
          step, Key, data);
        $fclose(testResults);
      end
    end
  endtask
endmodule
""");

      writeFile(sourceVerilog, editVariables(s));                               // Write verilog module
      writeFile(testVerilog,   editVariables(t));                               // Write tverilog test bench
      M.dumpVerilog(mFile);                                                     // Write include file to initialize main memory
      T.dumpVerilog(tFile);                                                     // Write include file to initialize transaction memory
      P.traceMemory = M.memory();                                               // Request memory tracing
      P.run(javaTraceFile);                                                     // Run the java version and trace it
      say(Project, Folder, Key());                                              // Identify the test
      execTest();                                                               // Exeute the verilog test
     }

    void execTest()                                                             // Execute the verilog test and compare it with the results from execution under Java
     {final StringBuilder s = new StringBuilder(editVariables("cd $projectFolder && iverilog $project.tb $project.v -Iincludes -g2012 -o $project && ./$project"));
      final ExecCommand   x = new ExecCommand(s);
      final String        e = joinLines(readFile(javaTraceFile));
      final String        g = joinLines(readFile(traceFile));
      ok(x.exitCode, 0);                                                        // Confirm exit code
      ok(12, g, e);                                                             // Width of margin in verilog traces
      final TreeMap<String,String> p = readProperties(testsFile);               // Load test results
      ok(ifs(p.get("Steps")), expSteps());
      ok(ifs(p.get("data")),  data());
     }

    private String editVariables(StringBuilder S) {return editVariables(""+S);} // Edit the variables in a string builder

    private String editVariables(String s)                                      // Edit the variables in a string builder
     {s = s.replace("$bitsPerKey",    ""  + bitsPerKey());
      s = s.replace("$bitsPerData",   ""  + bitsPerData());
      s = s.replace("$stuckBases",          stuckMemories());
      s = s.replace("$stuckInitialization", stuckMemoryInitialization());
      s = s.replace("$mFile",               mFile);
      s = s.replace("$tFile",               tFile);
      s = s.replace("$testsFile",           fileName(testsFile));
      s = s.replace("$traceFile",           fileName(traceFile));
      s = s.replace("$projectFolder",       projectFolder);
      s = s.replace("$project",             project);
      s = s.replace("$Key",                 ""+Key());
      s = s.replace("$Data",                ""+Data());
      s = s.replace("$data_at",             ""+data.at);
      s = s.replace("$data_width",          ""+data.width);
      s = s.replace("$data",                ""+data());
      s = s.replace("$maxSteps",            ""+maxSteps());
      s = s.replace("$expSteps",            ""+expSteps());
      s = s.replace("$found_at",            ""+found.at);
      s = s.replace("$M",                   ""+M.name());
      s = s.replace("$T",                   ""+T.name());
      s = s.replace("$initialize_memory_M", ""+M.initializeMemory());           // Initialize btree memory
      s = s.replace("$initialize_memory_T", ""+T.initializeMemory());           // Initialize btree transaction

      return s;
     }
   }

//D0 Tests                                                                      // Testing

  final static int[]random_small = {27, 442, 545, 317, 511, 578, 391, 993, 858, 586, 472, 906, 658, 704, 882, 246, 261, 501, 354, 903, 854, 279, 526, 686, 987, 403, 401, 989, 650, 576, 436, 560, 806, 554, 422, 298, 425, 912, 503, 611, 135, 447, 344, 338, 39, 804, 976, 186, 234, 106, 667, 494, 690, 480, 288, 151, 773, 769, 260, 809, 438, 237, 516, 29, 376, 72, 946, 103, 961, 55, 358, 232, 229, 90, 155, 657, 681, 43, 907, 564, 377, 615, 612, 157, 922, 272, 490, 679, 830, 839, 437, 826, 577, 937, 884, 13, 96, 273, 1, 188};
  final static int[]random_large = {5918,5624,2514,4291,1791,5109,7993,60,1345,2705,5849,1034,2085,4208,4590,7740,9367,6582,4178,5578,1120,378,7120,8646,5112,4903,1482,8005,3801,5439,4534,9524,6111,204,5459,248,4284,8037,5369,7334,3384,5193,2847,1660,5605,7371,3430,1786,1216,4282,2146,1969,7236,2187,136,2726,9480,5,4515,6082,969,5017,7809,9321,3826,9179,5781,3351,4819,4545,8607,4146,6682,1043,2890,2964,7472,9405,4348,8333,2915,9674,7225,4743,995,1321,3885,6061,9958,3901,4710,4185,4776,5070,8892,8506,6988,2317,9342,3764,9859,4724,5195,673,359,9740,2089,9942,3749,9208,1,7446,7023,5496,4206,3272,3527,8593,809,3149,4173,9605,9021,5120,5265,7121,8667,6911,4717,2535,2743,1289,1494,3788,6380,9366,2732,1501,8543,8013,5612,2393,7041,3350,3204,288,7213,1741,1238,9830,6722,4687,6758,8067,4443,5013,5374,6986,282,6762,192,340,5075,6970,7723,5913,1060,1641,1495,5738,1618,157,6891,173,7535,4952,9166,8950,8680,1974,5466,2383,3387,3392,2188,3140,6806,3131,6237,6249,7952,1114,9017,4285,7193,3191,3763,9087,7284,9170,6116,3717,6695,6538,6165,6449,8960,2897,6814,3283,6600,6151,4624,3992,5860,9557,1884,5585,2966,1061,6414,2431,9543,6654,7417,2617,878,8848,8241,3790,3370,8768,1694,9875,9882,8802,7072,3772,2689,5301,7921,7774,1614,494,2338,8638,4161,4523,5709,4305,17,9626,843,9284,3492,7755,5525,4423,9718,2237,7401,2686,8751,1585,5919,9444,3271,1490,7004,5980,3904,370,5930,6304,7737,93,5941,9079,4968,9266,262,2766,4999,2450,9518,5137,8405,483,8840,2231,700,8049,8823,9811,9378,3811,8074,153,1940,1998,4354,7830,7086,6132,9967,5680,448,1976,4101,7839,3122,4379,9296,4881,1246,4334,9457,5401,1945,9548,8290,1184,3464,132,2458,7704,1056,7554,6203,2270,6070,4889,7369,1676,485,3648,357,1912,9661,4246,1576,1836,4521,7667,6907,2098,8825,7404,4019,8284,3710,7202,7050,9870,3348,3624,9224,6601,7897,6288,3713,932,5596,353,2615,3273,833,1446,8624,2489,3872,486,1091,2493,4157,3611,6570,7107,9153,4543,9504,4746,1342,9737,3247,8984,3640,5698,7814,307,8775,1150,4330,3059,5784,2370,5248,4806,6107,9700,231,3566,5627,3957,5317,5415,8119,2588,9440,2961,9786,4769,466,5411,3080,7623,5031,2378,9286,4801,797,1527,2325,847,6341,5310,1926,9481,2115,2165,5255,5465,5561,3606,7673,7443,7243,8447,2348,7925,6447,8311,6729,4441,7763,8107,267,8135,9194,6775,3883,9639,612,5024,1351,7557,9241,5181,2239,8002,5446,747,166,325,9925,3820,9531,5163,3545,558,7103,7658,5670,8323,4821,6263,7982,59,3700,1082,4474,4353,8637,9558,5191,842,5925,6455,4092,9929,9961,290,3523,6290,7787,8266,7986,7269,6408,3620,406,5964,7289,1620,6726,1257,1993,7006,5545,2913,5093,5066,3019,7081,6760,6779,7061,9051,8852,8118,2340,6596,4594,9708,8430,8659,8920,9268,5431,9203,2823,1427,2203,6422,6193,5214,9566,8791,4964,7575,4350,56,2227,8545,5646,3089,2204,4081,487,8496,2258,4336,6955,3452,556,8602,8251,8569,8636,9430,1025,9459,7137,8392,3553,5945,9414,3078,1688,5480,327,8117,2289,2195,8564,9423,103,7724,3091,8548,7298,5279,6042,2855,3286,3542,9361,420,7020,4112,5320,5366,6379,114,9174,9744,592,5346,3985,3174,5157,9890,1605,3082,8099,4346,7256,8670,5687,6613,6620,1458,1045,7917,2980,2399,1433,3315,4084,178,7056,2132,2728,4421,9195,4181,6017,6229,2945,4627,2809,8816,6737,18,8981,3813,8890,5304,3789,6959,7476,1856,4197,6944,9578,5915,3060,9932,3463,67,7393,9857,5822,3187,501,653,8453,3691,9736,6845,1365,9645,4120,2157,8471,4436,6435,2758,7591,9805,7142,7612,4891,7342,5764,8683,8365,2967,6947,441,2116,6612,1399,7585,972,6548,5481,7733,7209,222,5903,6161,9172,9628,7348,1588,5992,6094,7176,4214,8702,2987,74,8486,9788,7164,5788,8535,8422,6826,1800,8965,4965,565,5609,4686,2556,9324,5000,9809,1994,4737,63,8992,4783,2536,4462,8868,6346,5553,3980,2670,1601,4272,8725,4698,7333,7826,9233,4198,1997,1687,4851,62,7893,8149,8015,341,2230,1280,5559,9756,3761,7834,6805,9287,4622,5748,2320,1958,9129,9649,1644,4323,5096,9490,7529,6444,7478,7044,9525,7713,234,7553,9099,9885,7135,6493,9793,6268,8363,2267,9157,9451,1438,9292,1637,3739,695,1090,4731,4549,5171,5975,7347,5192,5243,1084,2216,9860,3318,5594,5790,1107,220,9397,3378,1353,4498,6497,5442,7929,7377,9541,9871,9895,6742,9146,9409,292,6278,50,5288,2217,4923,6790,4730,9240,3006,3547,9347,7863,4275,3287,2673,7485,1915,9837,2931,3918,635,9131,1197,6250,3853,4303,790,5548,9993,3702,2446,3862,9652,4432,973,41,3507,8585,2444,1633,956,5789,1523,8657,4869,8580,8474,7093,7812,2549,7363,9315,6731,1130,7645,7018,7852,362,1636,2905,8006,4040,6643,8052,7021,3665,8383,715,1876,2783,3065,604,4566,8761,7911,1983,3836,5547,8495,8144,1950,2537,8575,640,8730,8303,1454,8165,6647,4762,909,9449,8640,9253,7293,8767,3004,4623,6862,8994,2520,1215,6299,8414,2576,6148,1510,313,3693,9843,8757,5774,8871,8061,8832,5573,5275,9452,1248,228,9749,2730};

  static void test_put_ascending()
   {final BtreePA t = btreePA(4, 3);
    t.P.run(); t.P.clear();
    t.put();
    for(int i = 1; i <= 64; ++i)
     {//say(currentTestName(), i);
      t.T.at(t.Key).setInt(  i);
      t.T.at(t.Data).setInt( i);
      t.P.run();
     }
    t.ok("""
                                                                                                                            32                                                                                                                                           |
                                                                                                                            0                                                                                                                                            |
                                                                                                                            17                                                                                                                                           |
                                                                                                                            21                                                                                                                                           |
                                                      16                                                                                                                                            48                                56                                 |
                                                      17                                                                                                                                            21                                21.1                               |
                                                      5                                                                                                                                             16                                23                                 |
                                                      11                                                                                                                                                                              6                                  |
          4          8               12                               20               24                28                                  36               40                 44                                  52                                  60              |
          5          5.1             5.2                              11               11.1              11.2                                16               16.1               16.2                                23                                  6               |
          1          3               4                                8                10                9                                   13               15                 14                                  18                                  20              |
                                     7                                                                   12                                                                      19                                  22                                  2               |
1,2,3,4=1  5,6,7,8=3    9,10,11,12=4    13,14,15,16=7   17,18,19,20=8   21,22,23,24=10     25,26,27,28=9     29,30,31,32=12   33,34,35,36=13   37,38,39,40=15     41,42,43,44=14     45,46,47,48=19   49,50,51,52=18   53,54,55,56=22     57,58,59,60=20   61,62,63,64=2 |
""");
    // stop("maximumNodes used", t.maxNodeUsed); // 25
   }

  static void test_put_ascending_wide()
   {final BtreePA    t = btreePA(8, 7);
    t.P.run(); t.P.clear();
    t.put();
    for(int i = 1; i <= 64; ++i)
     {//say(currentTestName(), i);
      t.T.at(t.Key ).setInt(i);
      t.T.at(t.Data).setInt(i);
      t.P.run();
     }
    //stop(t);
    t.ok("""
                                                                                                      32                                                                                                                  |
                                                                                                      0                                                                                                                   |
                                                                                                      9                                                                                                                   |
                                                                                                      10                                                                                                                  |
                  8                         16                           24                                                       40                          48                            56                            |
                  9                         9.1                          9.2                                                      10                          10.1                          10.2                          |
                  1                         3                            4                                                        6                           7                             8                             |
                                                                         5                                                                                                                  2                             |
1,2,3,4,5,6,7,8=1  9,10,11,12,13,14,15,16=3    17,18,19,20,21,22,23,24=4    25,26,27,28,29,30,31,32=5   33,34,35,36,37,38,39,40=6   41,42,43,44,45,46,47,48=7     49,50,51,52,53,54,55,56=8     57,58,59,60,61,62,63,64=2 |
""");
    // stop("maximumNodes used", t.maxNodeUsed); // 12
   }

  static void test_put_descending()
   {final BtreePA     t = btreePA(2, 3);
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

  static void test_put_small_random()
   {final BtreePA    t = btreePA(6, 3);
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

  static void test_put_large_random()
   {if (!github_actions) return;
    final BtreePA t = btreePA(2, 3);
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

  static void test_find()
   {final int N = 64;
     final BtreePA t = btreePA(8, 3);
    t.P.run(); t.P.clear();
    t.put();
    for(int i = 2; i <= N; i += 2)
     {//say(currentTestName(),  "a", i);
      t.T.at(t.Key).setInt(  i);
      t.T.at(t.Data).setInt( i);
      t.P.run();                                                                // Insert
      t.P.run();                                                                // Update
     }

    //stop(t);
    t.ok("""
                                                  33                                                      |
                                                  0                                                       |
                                                  5                                                       |
                                                  6                                                       |
                      17                                                      49                          |
                      5                                                       6                           |
                      1                                                       4                           |
                      3                                                       2                           |
2,4,6,8,10,12,14,16=1   18,20,22,24,26,28,30,32=3   34,36,38,40,42,44,46,48=4   50,52,54,56,58,60,62,64=2 |
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

  static void test_delete_ascending()
   {final BtreePA t = btreePA(4, 3);
    t.P.run(); t.P.clear();

    final int N = 32;
    final boolean box = false;                                                  // Print read me

    t.put();
    for(int i = 1; i <= N; ++i)
     {//say(currentTestName(), "a", i);
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
          1          3               4                                8                                 9               |
                                     7                                10                                2               |
1,2,3,4=1  5,6,7,8=3    9,10,11,12=4    13,14,15,16=7   17,18,19,20=8   21,22,23,24=10    25,26,27,28=9   29,30,31,32=2 |
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
        1          3               4                                8                10                9                 |
                                   7                                                                   2                 |
2,3,4=1  5,6,7,8=3    9,10,11,12=4    13,14,15,16=7   17,18,19,20=8   21,22,23,24=10     25,26,27,28=9     29,30,31,32=2 |
""");
        case 2 -> t.ok("""
                                                  16                                                                   |
                                                  0                                                                    |
                                                  5                                                                    |
                                                  11                                                                   |
      4          8               12                               20               24                28                |
      5          5.1             5.2                              11               11.1              11.2              |
      1          3               4                                8                10                9                 |
                                 7                                                                   2                 |
3,4=1  5,6,7,8=3    9,10,11,12=4    13,14,15,16=7   17,18,19,20=8   21,22,23,24=10     25,26,27,28=9     29,30,31,32=2 |
""");
        case 3 -> t.ok("""
                                                16                                                                   |
                                                0                                                                    |
                                                5                                                                    |
                                                11                                                                   |
    4          8               12                               20               24                28                |
    5          5.1             5.2                              11               11.1              11.2              |
    1          3               4                                8                10                9                 |
                               7                                                                   2                 |
4=1  5,6,7,8=3    9,10,11,12=4    13,14,15,16=7   17,18,19,20=8   21,22,23,24=10     25,26,27,28=9     29,30,31,32=2 |
""");
        case 4 -> t.ok("""
                                         16                                                                   |
                                         0                                                                    |
                                         5                                                                    |
                                         11                                                                   |
          8             12                               20               24                28                |
          5             5.1                              11               11.1              11.2              |
          1             4                                8                10                9                 |
                        7                                                                   2                 |
5,6,7,8=1  9,10,11,12=4    13,14,15,16=7   17,18,19,20=8   21,22,23,24=10     25,26,27,28=9     29,30,31,32=2 |
""");
        case 5 -> t.ok("""
                                       16                                                                   |
                                       0                                                                    |
                                       5                                                                    |
                                       11                                                                   |
        8             12                               20               24                28                |
        5             5.1                              11               11.1              11.2              |
        1             4                                8                10                9                 |
                      7                                                                   2                 |
6,7,8=1  9,10,11,12=4    13,14,15,16=7   17,18,19,20=8   21,22,23,24=10     25,26,27,28=9     29,30,31,32=2 |
""");
        case 6 -> t.ok("""
                                     16                                                                   |
                                     0                                                                    |
                                     5                                                                    |
                                     11                                                                   |
      8             12                               20               24                28                |
      5             5.1                              11               11.1              11.2              |
      1             4                                8                10                9                 |
                    7                                                                   2                 |
7,8=1  9,10,11,12=4    13,14,15,16=7   17,18,19,20=8   21,22,23,24=10     25,26,27,28=9     29,30,31,32=2 |
""");
        case 7 -> t.ok("""
                                   16                                                                   |
                                   0                                                                    |
                                   5                                                                    |
                                   11                                                                   |
    8             12                               20               24                28                |
    5             5.1                              11               11.1              11.2              |
    1             4                                8                10                9                 |
                  7                                                                   2                 |
8=1  9,10,11,12=4    13,14,15,16=7   17,18,19,20=8   21,22,23,24=10     25,26,27,28=9     29,30,31,32=2 |
""");
        case 8 -> t.ok("""
                             16                                                                   |
                             0                                                                    |
                             5                                                                    |
                             11                                                                   |
             12                              20               24                28                |
             5                               11               11.1              11.2              |
             1                               8                10                9                 |
             7                                                                  2                 |
9,10,11,12=1   13,14,15,16=7   17,18,19,20=8   21,22,23,24=10     25,26,27,28=9     29,30,31,32=2 |
""");
        case 9 -> t.ok("""
                                            20                                                 |
                                            0                                                  |
                                            5                                                  |
                                            11                                                 |
           12              16                                24              28                |
           5               5.1                               11              11.1              |
           1               7                                 10              9                 |
                           8                                                 2                 |
10,11,12=1   13,14,15,16=7    17,18,19,20=8   21,22,23,24=10   25,26,27,28=9     29,30,31,32=2 |
""");
        case 10 -> t.ok("""
                                         20                                                 |
                                         0                                                  |
                                         5                                                  |
                                         11                                                 |
        12              16                                24              28                |
        5               5.1                               11              11.1              |
        1               7                                 10              9                 |
                        8                                                 2                 |
11,12=1   13,14,15,16=7    17,18,19,20=8   21,22,23,24=10   25,26,27,28=9     29,30,31,32=2 |
""");
        case 11 -> t.ok("""
                                      20                                                 |
                                      0                                                  |
                                      5                                                  |
                                      11                                                 |
     12              16                                24              28                |
     5               5.1                               11              11.1              |
     1               7                                 10              9                 |
                     8                                                 2                 |
12=1   13,14,15,16=7    17,18,19,20=8   21,22,23,24=10   25,26,27,28=9     29,30,31,32=2 |
""");
        case 12 -> t.ok("""
                              20                                                 |
                              0                                                  |
                              5                                                  |
                              11                                                 |
              16                               24              28                |
              5                                11              11.1              |
              1                                10              9                 |
              8                                                2                 |
13,14,15,16=1   17,18,19,20=8   21,22,23,24=10   25,26,27,28=9     29,30,31,32=2 |
""");
        case 13 -> t.ok("""
                                             24                              |
                                             0                               |
                                             5                               |
                                             11                              |
           16              20                                28              |
           5               5.1                               11              |
           1               8                                 9               |
                           10                                2               |
14,15,16=1   17,18,19,20=8    21,22,23,24=10   25,26,27,28=9   29,30,31,32=2 |
""");
        case 14 -> t.ok("""
                                          24                              |
                                          0                               |
                                          5                               |
                                          11                              |
        16              20                                28              |
        5               5.1                               11              |
        1               8                                 9               |
                        10                                2               |
15,16=1   17,18,19,20=8    21,22,23,24=10   25,26,27,28=9   29,30,31,32=2 |
""");
        case 15 -> t.ok("""
                                       24                              |
                                       0                               |
                                       5                               |
                                       11                              |
     16              20                                28              |
     5               5.1                               11              |
     1               8                                 9               |
                     10                                2               |
16=1   17,18,19,20=8    21,22,23,24=10   25,26,27,28=9   29,30,31,32=2 |
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
     20               24               28               |
     0                0.1              0.2              |
     1                10               9                |
                                       2                |
20=1   21,22,23,24=10    25,26,27,28=9    29,30,31,32=2 |
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
     24              28               |
     0               0.1              |
     1               9                |
                     2                |
24=1   25,26,27,28=9    29,30,31,32=2 |
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
     28              |
     0               |
     1               |
     2               |
28=1   29,30,31,32=2 |
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

  static void test_delete_descending()
   {final BtreePA     t = btreePA(4, 3);
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
          1          3               4                                8                                 9               |
                                     7                                10                                2               |
1,2,3,4=1  5,6,7,8=3    9,10,11,12=4    13,14,15,16=7   17,18,19,20=8   21,22,23,24=10    25,26,27,28=9   29,30,31,32=2 |
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
          1          3               4                                8                10               9             |
                                     7                                                                  2             |
1,2,3,4=1  5,6,7,8=3    9,10,11,12=4    13,14,15,16=7   17,18,19,20=8   21,22,23,24=10    25,26,27,28=9    29,30,31=2 |
""");
        case 31 -> t.ok("""
                                                      16                                                           |
                                                      0                                                            |
                                                      5                                                            |
                                                      6                                                            |
          4          8               12                               20               24               28         |
          5          5.1             5.2                              6                6.1              6.2        |
          1          3               4                                8                10               9          |
                                     7                                                                  2          |
1,2,3,4=1  5,6,7,8=3    9,10,11,12=4    13,14,15,16=7   17,18,19,20=8   21,22,23,24=10    25,26,27,28=9    29,30=2 |
""");
        case 30 -> t.ok("""
                                                      16                                                        |
                                                      0                                                         |
                                                      5                                                         |
                                                      6                                                         |
          4          8               12                               20               24               28      |
          5          5.1             5.2                              6                6.1              6.2     |
          1          3               4                                8                10               9       |
                                     7                                                                  2       |
1,2,3,4=1  5,6,7,8=3    9,10,11,12=4    13,14,15,16=7   17,18,19,20=8   21,22,23,24=10    25,26,27,28=9    29=2 |
""");
        case 29 -> t.ok("""
                                                      16                                                |
                                                      0                                                 |
                                                      5                                                 |
                                                      6                                                 |
          4          8               12                               20               24               |
          5          5.1             5.2                              6                6.1              |
          1          3               4                                8                10               |
                                     7                                                 9                |
1,2,3,4=1  5,6,7,8=3    9,10,11,12=4    13,14,15,16=7   17,18,19,20=8   21,22,23,24=10    25,26,27,28=9 |
""");
        case 28 -> t.ok("""
                                                      16                                             |
                                                      0                                              |
                                                      5                                              |
                                                      6                                              |
          4          8               12                               20               24            |
          5          5.1             5.2                              6                6.1           |
          1          3               4                                8                10            |
                                     7                                                 9             |
1,2,3,4=1  5,6,7,8=3    9,10,11,12=4    13,14,15,16=7   17,18,19,20=8   21,22,23,24=10    25,26,27=9 |
""");
        case 27 -> t.ok("""
                                                      16                                          |
                                                      0                                           |
                                                      5                                           |
                                                      6                                           |
          4          8               12                               20               24         |
          5          5.1             5.2                              6                6.1        |
          1          3               4                                8                10         |
                                     7                                                 9          |
1,2,3,4=1  5,6,7,8=3    9,10,11,12=4    13,14,15,16=7   17,18,19,20=8   21,22,23,24=10    25,26=9 |
""");
        case 26 -> t.ok("""
                                                      16                                       |
                                                      0                                        |
                                                      5                                        |
                                                      6                                        |
          4          8               12                               20               24      |
          5          5.1             5.2                              6                6.1     |
          1          3               4                                8                10      |
                                     7                                                 9       |
1,2,3,4=1  5,6,7,8=3    9,10,11,12=4    13,14,15,16=7   17,18,19,20=8   21,22,23,24=10    25=9 |
""");
        case 25 -> t.ok("""
                                                      16                               |
                                                      0                                |
                                                      5                                |
                                                      6                                |
          4          8               12                               20               |
          5          5.1             5.2                              6                |
          1          3               4                                8                |
                                     7                                10               |
1,2,3,4=1  5,6,7,8=3    9,10,11,12=4    13,14,15,16=7   17,18,19,20=8   21,22,23,24=10 |
""");
        case 24 -> t.ok("""
                                     12                                             |
                                     0                                              |
                                     5                                              |
                                     6                                              |
          4          8                               16              20             |
          5          5.1                             6               6.1            |
          1          3                               7               8              |
                     4                                               10             |
1,2,3,4=1  5,6,7,8=3    9,10,11,12=4   13,14,15,16=7   17,18,19,20=8    21,22,23=10 |
""");
        case 23 -> t.ok("""
                                     12                                          |
                                     0                                           |
                                     5                                           |
                                     6                                           |
          4          8                               16              20          |
          5          5.1                             6               6.1         |
          1          3                               7               8           |
                     4                                               10          |
1,2,3,4=1  5,6,7,8=3    9,10,11,12=4   13,14,15,16=7   17,18,19,20=8    21,22=10 |
""");
        case 22 -> t.ok("""
                                     12                                       |
                                     0                                        |
                                     5                                        |
                                     6                                        |
          4          8                               16              20       |
          5          5.1                             6               6.1      |
          1          3                               7               8        |
                     4                                               10       |
1,2,3,4=1  5,6,7,8=3    9,10,11,12=4   13,14,15,16=7   17,18,19,20=8    21=10 |
""");
        case 21 -> t.ok("""
                                     12                              |
                                     0                               |
                                     5                               |
                                     6                               |
          4          8                               16              |
          5          5.1                             6               |
          1          3                               7               |
                     4                               8               |
1,2,3,4=1  5,6,7,8=3    9,10,11,12=4   13,14,15,16=7   17,18,19,20=8 |
""");
        case 20 -> t.ok("""
                     8                                           |
                     0                                           |
                     5                                           |
                     6                                           |
          4                        12              16            |
          5                        6               6.1           |
          1                        4               7             |
          3                                        8             |
1,2,3,4=1  5,6,7,8=3  9,10,11,12=4   13,14,15,16=7    17,18,19=8 |
""");
        case 19 -> t.ok("""
                     8                                        |
                     0                                        |
                     5                                        |
                     6                                        |
          4                        12              16         |
          5                        6               6.1        |
          1                        4               7          |
          3                                        8          |
1,2,3,4=1  5,6,7,8=3  9,10,11,12=4   13,14,15,16=7    17,18=8 |
""");
        case 18 -> t.ok("""
                     8                                     |
                     0                                     |
                     5                                     |
                     6                                     |
          4                        12              16      |
          5                        6               6.1     |
          1                        4               7       |
          3                                        8       |
1,2,3,4=1  5,6,7,8=3  9,10,11,12=4   13,14,15,16=7    17=8 |
""");
        case 17 -> t.ok("""
                     8                             |
                     0                             |
                     5                             |
                     6                             |
          4                        12              |
          5                        6               |
          1                        4               |
          3                        7               |
1,2,3,4=1  5,6,7,8=3  9,10,11,12=4   13,14,15,16=7 |
""");
        case 16 -> t.ok("""
          4          8               12            |
          0          0.1             0.2           |
          1          3               4             |
                                     7             |
1,2,3,4=1  5,6,7,8=3    9,10,11,12=4    13,14,15=7 |
""");
        case 15 -> t.ok("""
          4          8               12         |
          0          0.1             0.2        |
          1          3               4          |
                                     7          |
1,2,3,4=1  5,6,7,8=3    9,10,11,12=4    13,14=7 |
""");
        case 14 -> t.ok("""
          4          8               12      |
          0          0.1             0.2     |
          1          3               4       |
                                     7       |
1,2,3,4=1  5,6,7,8=3    9,10,11,12=4    13=7 |
""");
        case 13 -> t.ok("""
          4          8               |
          0          0.1             |
          1          3               |
                     4               |
1,2,3,4=1  5,6,7,8=3    9,10,11,12=4 |
""");
        case 12 -> t.ok("""
          4          8            |
          0          0.1          |
          1          3            |
                     4            |
1,2,3,4=1  5,6,7,8=3    9,10,11=4 |
""");
        case 11 -> t.ok("""
          4          8         |
          0          0.1       |
          1          3         |
                     4         |
1,2,3,4=1  5,6,7,8=3    9,10=4 |
""");
        case 10 -> t.ok("""
          4          8      |
          0          0.1    |
          1          3      |
                     4      |
1,2,3,4=1  5,6,7,8=3    9=4 |
""");
        case 9 -> t.ok("""
          4          |
          0          |
          1          |
          3          |
1,2,3,4=1  5,6,7,8=3 |
""");
        case 8 -> t.ok("""
          4        |
          0        |
          1        |
          3        |
1,2,3,4=1  5,6,7=3 |
""");
        case 7 -> t.ok("""
          4      |
          0      |
          1      |
          3      |
1,2,3,4=1  5,6=3 |
""");
        case 6 -> t.ok("""
          4    |
          0    |
          1    |
          3    |
1,2,3,4=1  5=3 |
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

  static void test_delete_random(int[]random_array)
   {final BtreePA t = btreePA(4, 3);
    t.P.run(); t.P.clear();
    t.put();
    final int N = random_array.length;

    for (int i = 0; i < N; ++i)                                                 // Build tree
     {//say(currentTestName(),  "a", i);
      t.T.at(t.Key ).setInt(random_array[i]);
      t.T.at(t.Data).setInt(i);
      t.P.run();
     }

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
   }

  static void test_delete_small_random()
   {test_delete_random(random_small);
   }

  static void test_delete_large_random()
   {if (!github_actions) return;
    test_delete_random(random_large);
   }

  static void test_verilog_find()                                               // Find using generated verilog code
   {final BtreePA t = btreePA_small();
    t.P.run(); t.P.clear();
    t.put();
    final int N = 9;
    for (int i = 1; i <= N; ++i)
     {say(currentTestName(),  "a", i);
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
      1             4    7        |
      3                  2        |
1,2=1  3,4=3  5,6=4  7=7    8,9=2 |
""");
    t.P.clear();
    t.T.at(t.Key).setInt(2);                                                    // Sets memory directly not via an instruction
    t.find();
    GenVerilog v = t.new GenVerilog("find", "verilog")                          // Generate verilog now that memories have been initialized and the program written
     {int Key     () {return    2;}                                             // Input key value
      int Data    () {return    2;}                                             // Input data value
      int data    () {return    7;}                                             // Expected output data value
      int maxSteps() {return 2000;}                                             // Maximum number if execution steps
      int expSteps() {return   96;}                                             // Expected number of steps
     };
    //say("AAAA11", t);
    //say("AAAA22", t.P);
    //say("AAAA22", t.T);
    //say("AAAA22", t.M);
    ok(t.T.at(t.data).getInt(), 7);                                             // Data associated with key
   }

  void runVerilogDeleteTest(int Key, int data, int steps, String expected)      // Run the java and verilog versions and compare the resulting memory traces
   {T.at(this.Key).setInt(Key);                                                 // Sets memory directly not via an instruction
    GenVerilog v = new GenVerilog("delete", "verilog")                          // Generate verilog now that memories have beeninitialzied and the program written
     {int Key     () {return   Key;}                                            // Input key value
      int Data    () {return     3;}                                            // Input key value
      int data    () {return  data;}                                            // Expected output data value
      int maxSteps() {return  2000;}                                            // Maximum number if execution steps
      int expSteps() {return steps;}                                            // Expected number of steps
     };

    ok(T.at(this.data).getInt(), data);                                         // Data associated with key
    if (debug) stop(this);                                                      // Print tree if debugging
    ok(this, expected);                                                         // Check resultin tree
   }

  static void test_verilog_delete()                                             // Delete using generated verilog code
   {final BtreePA t = btreePA_small();
    t.P.run(); t.P.clear();
    t.put();
    final int N = 9;
    for (int i = 1; i <= N; ++i)
     {say(currentTestName(),  "a", i);
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
      1             4    7        |
      3                  2        |
1,2=1  3,4=3  5,6=4  7=7    8,9=2 |
""");

    t.P.clear();                                                                // Replace program with delete
    t.delete();                                                                 // Delete code

    t.runVerilogDeleteTest(3, 6, 931, """
                    6           |
                    0           |
                    5           |
                    6           |
      2    4             7      |
      5    5.1           6      |
      1    3             7      |
           4             2      |
1,2=1  4=3    5,6=4  7=7  8,9=2 |
""");

    t.runVerilogDeleteTest(4, 5, 802, """
             6           |
             0           |
             5           |
             6           |
      4           7      |
      5           6      |
      1           7      |
      4           2      |
1,2=1  5,6=4  7=7  8,9=2 |
""");

    t.runVerilogDeleteTest(2, 7, 771, """
    4      6      7        |
    0      0.1    0.2      |
    1      4      7        |
                  2        |
1=1  5,6=4    7=7    8,9=2 |
""");

    t.runVerilogDeleteTest(1, 8, 653, """
      6    7        |
      0    0.1      |
      1    7        |
           2        |
5,6=1  7=7    8,9=2 |
""");

    t.runVerilogDeleteTest(5, 4, 381, """
      7      |
      0      |
      1      |
      2      |
6,7=1  8,9=2 |
""");

    t.runVerilogDeleteTest(6, 3, 381, """
    7      |
    0      |
    1      |
    2      |
7=1  8,9=2 |
""");

    t.runVerilogDeleteTest(7, 2, 453, """
8,9=0 |
""");

    t.runVerilogDeleteTest(8, 1, 60, """
9=0 |
""");

    t.runVerilogDeleteTest(9, 0, 60, """
=0 |
""");
   }

  void runVerilogPutTest(int value, int steps, String expected)                 // Run the java and verilog versions and compare the resulting memory traces
   {T.at(Key ).setInt(value);                                                   // Sets memory directly not via an instruction
    T.at(Data).setInt(value);                                                   // Sets memory directly not via an instruction
    GenVerilog v = new GenVerilog("put", "verilog")                             // Generate verilog now that memories have been initialized and the program written
     {int Key     () {return value;}                                            // Input key value
      int Data    () {return     3;}                                            // Input data value
      int data    () {return     0;}                                            // Expected output data value
      int maxSteps() {return  2000;}                                            // Maximum number if execution steps
      int expSteps() {return steps;}                                            // Expected number of steps
     };
    if (debug) stop(this);
    ok(this, expected);
   }

  static void test_verilog_put()                                                // Delete using generated verilog code
   {final BtreePA t = btreePA_small();
    t.P.run(); t.P.clear();
    t.put();

    t.runVerilogPutTest(1, 64, """
1=0 |
""");

    t.runVerilogPutTest(2, 76, """
1,2=0 |
""");
                                                                                // Split instruction
    t.runVerilogPutTest(3, 245, """
    1      |
    0      |
    1      |
    2      |
1=1  2,3=2 |
""");

    t.runVerilogPutTest(4, 561, """
      2      |
      0      |
      1      |
      2      |
1,2=1  3,4=2 |
""");

    t.runVerilogPutTest(5, 674, """
      2    3        |
      0    0.1      |
      1    3        |
           2        |
1,2=1  3=3    4,5=2 |
""");

    t.runVerilogPutTest(6, 742, """
      2      4        |
      0      0.1      |
      1      3        |
             2        |
1,2=1  3,4=3    5,6=2 |
""");

    t.runVerilogPutTest(7, 855, """
      2      4      5        |
      0      0.1    0.2      |
      1      3      4        |
                    2        |
1,2=1  3,4=3    5=4    6,7=2 |
""");

    t.runVerilogPutTest(8, 1042, """
             4             |
             0             |
             5             |
             6             |
      2             6      |
      5             6      |
      1             4      |
      3             2      |
1,2=1  3,4=3  5,6=4  7,8=2 |
""");

    t.runVerilogPutTest(9, 953, """
             4                    |
             0                    |
             5                    |
             6                    |
      2             6    7        |
      5             6    6.1      |
      1             4    7        |
      3                  2        |
1,2=1  3,4=3  5,6=4  7=7    8,9=2 |
""");
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_put_ascending();
    test_put_ascending_wide();
    test_put_descending();
    test_put_small_random();
    //test_put_large_random();
    test_find();
    test_delete_ascending();
    test_delete_descending();
    //test_to_array();
    test_delete_small_random();
    //test_delete_large_random();
    test_verilog_delete();
    test_verilog_find();
    test_verilog_put();
   }

  static void newTests()                                                        // Tests being worked on
   {//oldTests();
    //test_delete_small_random();
    test_verilog_delete();
    test_verilog_find();
    test_verilog_put();
    //test_put_ascending();
    //test_delete_descending();
   }

  public static void main(String[] args)                                        // Test if called as a program
   {try                                                                         // Get a traceback in a format clickable in Geany if something goes wrong to speed up debugging.
     {if (github_actions) oldTests(); else newTests();                          // Tests to run
      if (github_actions)                                                       // Coverage analysis
       {coverageAnalysis(sourceFileName(), 12);
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
