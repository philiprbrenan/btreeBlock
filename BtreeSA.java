//------------------------------------------------------------------------------
// BtreeSA with transaction in bit memory
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, layout and simulate a btree in a block on the surface of a silicon chip.

import java.util.*;

abstract class BtreeSA extends Test                                             // Manipulate a btree using static methods and memory
 {MemoryLayout memoryLayout = new MemoryLayout();                               // The memory layout of the btree
  abstract int maxSize();                                                       // The maximum number of leaves plus branches in the bree
  abstract int bitsPerKey();                                                    // The number of bits per key
  abstract int bitsPerData();                                                   // The number of bits per data
  abstract int bitsPerNext();                                                   // The number of bits in a next field
  abstract int bitsPerSize();                                                   // The number of bits in size field
  abstract int maxKeysPerLeaf();                                                // Maximum number of leafs in a key
  abstract int maxKeysPerBranch();                                              // Maximum number of keys in a branch
  int splitLeafSize  () {return maxKeysPerLeaf()   >> 1;}                       // The number of key, data pairs to split out of a leaf
  int splitBranchSize() {return maxKeysPerBranch() >> 1;}                       // The number of key, next pairs to split out of a branch

  StuckSP leafStuck, branchStuck;                                               // Leaf, branch definition

  Layout.Field     leaf;                                                        // Layout of a leaf in the memory used by btree
  Layout.Field     branch;                                                      // Layout of a branch in the memory used by btree
  Layout.Union     branchOrLeaf;                                                // Layout of either a leaf or a branch in the memory used by btree
  Layout.Bit       isLeaf;                                                      // Whether the current node is a leaf or a branch
  Layout.Variable  free;                                                        // Free list chain
  Layout.Structure Node;                                                        // Layout of a node in the memory used by btree
  Layout.Array     nodes;                                                       // Layout of an array of nodes in the memory used by btree
  Layout.Variable  freedChain;                                                  // Single linked list of freed nodes
  Layout.Structure bTree;                                                       // Btree

  final static int
   linesToPrintABranch  =  4,                                                   // The number of lines required to print a branch
        maxPrintLevels  = 10,                                                   // Maximum number of levels to print in a tree
              maxDepth  = 99,                                                   // Maximum depth of any realistic tree
           testMaxSize  = github_actions ? 1000 : 50;                           // Maximum number of leaves plus branches during testing

  int         nodeUsed  = 0;                                                    // Number of nodes currently in use
  int      maxNodeUsed  = 0;                                                    // Maximum number of branches plus leaves used

  final int Node_root   = 0;                                                    // The root of the tree is always node zero
  final int Node_parent = 1;                                                    // Node used for initializing the tree and for the parent node
  final int Node_left   = 2;                                                    // Node used for a left  hand child
  final int Node_right  = 3;                                                    // Node used for a right hand child
  final int Node_temp   = 4;                                                    // Temporary node
  final int Node_find   = 5;                                                    // Find node
  final int Node_put    = 6;                                                    // Put node
  final int Node_delete = 7;                                                    // Delete node
  final int Node_length = 8;                                                    // Number of node types

  final NodeTransaction[]nodeTypes;                                             // One node description for each type of transacton

  final int Branch_Size        = 0;                                             // Get the size of a stuck
  final int Branch_Leaf        = 1;                                             // Check whether a node has leaves for children
  final int Branch_Top         = 2;                                             // Get the top element of a branch
  final int Branch_FirstBranch = 3;                                             // Locate the first greater or equal key in a branch
  final int Branch_T           = 4;                                             // Process a parent node
  final int Branch_tl          = 5;                                             // Process a left node
  final int Branch_tr          = 6;                                             // Process a right node
  final int Branch_length      = 7;                                             // Number of transaction types

  final int Leaf_Size          = 0;                                             // Get the size of a stuck
  final int Leaf_Leaf          = 1;                                             // Check whether a node has leaves for children
  final int Leaf_Equal         = 2;                                             // Locate an equal key
  final int Leaf_FirstLeaf     = 3;                                             // Locate the first greater or equal key in a leaf
  final int Leaf_T             = 4;                                             // Process a parent node
  final int Leaf_tl            = 5;                                             // Process a left node
  final int Leaf_tr            = 6;                                             // Process a right node
  final int Leaf_length        = 7;                                             // Number of transaction types

  final StuckSP[]branchTransactions;                                            // Transactions to use on branch stucks
  final StuckSP[]  leafTransactions;                                            // Transactions to use on leaf stucks

  NodeTransaction root;                                                         // The root of the tree
  StuckSP bSize;                                                                // Branch size
  StuckSP bLeaf;                                                                // Check whether a node has leaves for childrn
  StuckSP bTop;                                                                 // Get the size of a stuck
  StuckSP bFirstBranch;                                                         // Locate the first greater or equal key in a branch
  StuckSP bT;                                                                   // Process a parent node
  StuckSP bL;                                                                   // Process a left node
  StuckSP bR;                                                                   // Process a right node

  StuckSP lSize;                                                                // Branch size
  StuckSP lLeaf;                                                                // Check whether a node has leaves for childrn
  StuckSP lEqual;                                                               // Locate an equal key
  StuckSP lFirstLeaf;                                                           // Locate the first greater or equal key in a leaf
  StuckSP lT;                                                                   // Process a parent node
  StuckSP lL;                                                                   // Process a left node
  StuckSP lR;                                                                   // Process a right node

  Layout.Variable node;                                                         // The number of the node
  Layout.Variable parent;                                                       // Parent node
  Layout.Variable anode;                                                        // Node being allocated
  Layout.Variable P;                                                            // Parent node during a split, merge or steal operation
  Layout.Variable l;                                                            // Left node
  Layout.Variable r;                                                            // Right node
  Layout.Variable splitLeafSize;                                                // Where to split a full leaf
  Layout.Variable splitBranchSize;                                              // Branch split size
  Layout.Variable firstKey;                                                     // First of right leaf
  Layout.Variable lastKey;                                                      // Last of left leaf
  Layout.Variable flKey;                                                        // Key mid way between last of left and first of right
  Layout.Variable parentKey;                                                    // Parent key
  Layout.Variable L;                                                            // Reference to left child
  Layout.Variable R;                                                            // Reference to right child
  Layout.Variable nl;                                                           // Number in the left child, number in the right child
  Layout.Variable nr;                                                           // Number in the left child, number in the right child
  Layout.Variable lk;                                                           // Left  child key
  Layout.Variable ld;                                                           // Left  child data
  Layout.Variable rk;                                                           // Right child key
  Layout.Variable rd;                                                           // Right child data
  Layout.Variable index;                                                        // Index of a slot in a node
                                                                                // Find equal in leaf
  Layout.Variable search;                                                       // Search key
  Layout.Bit       found;                                                       // Whether the key was found
  Layout.Variable    key;                                                       // Key to insert
  Layout.Variable   data;                                                       // Data associated with the  key

  Layout.Variable  first;                                                       // Index of first key greater than or equal to the search key
  Layout.Variable   next;                                                       // The corresponding next field or top if no such key was found

  Layout.Bit  success;                                                          // Inserted or updated if true
  Layout.Bit inserted;                                                          // Inserted if true
  Layout.Structure nodeTransaction;                                             // Collection of fields used to perform a transaction against the btree
  Layout.Array     nodeTransactions;                                            // Array of node transactions

  final MemoryLayout transactionMemory = new MemoryLayout();                    // Memory layout used to describe a trnasaction against the btree

  static boolean debug = false;                                                 // Debugging enabled

//D1 Construction                                                               // Create a Btree from nodes which can be branches or leaves.  The data associated with the BTree is stored only in the leaves opposite the keys

  BtreeSA()                                                                     // Define a Btree with user specified dimensions
   {z();
    memoryLayout.layout(layout());
    memoryLayout.memory(new Memory(memoryLayout.layout.size()));

     {final int N = Branch_length;                                              // Preallocate transactions used on branch stucks
      branchTransactions = new StuckSP[N];

      for (int i = 0; i < N; i++)
       {final StuckSP b = branchTransactions[i] = new StuckSP()
         {int     maxSize() {return BtreeSA.this.maxKeysPerBranch()+1;}         // Not forgetting top next
          int  bitsPerKey() {return BtreeSA.this.bitsPerKey();}
          int bitsPerData() {return BtreeSA.this.bitsPerNext();}
          int bitsPerSize() {return BtreeSA.this.bitsPerSize();}
         };
         b.M.memory(memoryLayout.memory);
        }
      }

     {final int N = Leaf_length;                                                // Preallocate transactions used on leaf stucks
      leafTransactions = new StuckSP[N];

      for (int i = 0; i < N; i++)
       {final StuckSP l = leafTransactions[i] = new StuckSP()
         {int     maxSize() {return BtreeSA.this.maxKeysPerLeaf();}
          int  bitsPerKey() {return BtreeSA.this.bitsPerKey();}
          int bitsPerData() {return BtreeSA.this.bitsPerData();}
          int bitsPerSize() {return BtreeSA.this.bitsPerSize();}
         };
         l.M.memory(memoryLayout.memory);
       }
     }

     {final int N = Node_length;                                                // Preallocate nodes
      nodeTypes = new NodeTransaction[N];
      for (int i = 0; i < N; i++) nodeTypes[i] = new NodeTransaction();
     }

    root         = nodeTypes[Node_root];                                        // The root of the tree
    bSize        = branchTransactions[Branch_Size       ];                      // Branch size
    bLeaf        = branchTransactions[Branch_Leaf       ];                      // Check whether a node has leaves for childrn
    bTop         = branchTransactions[Branch_Top        ];                      // Get the size of a stuck
    bFirstBranch = branchTransactions[Branch_FirstBranch];                      // Locate the first greater or equal key in a branch
    bT           = branchTransactions[Branch_T          ];                      // Process a parent node
    bL           = branchTransactions[Branch_tl         ];                      // Process a left node
    bR           = branchTransactions[Branch_tr         ];                      // Process a right node

    lSize        =   leafTransactions[Leaf_Size         ];                      // Branch size
    lLeaf        =   leafTransactions[Leaf_Leaf         ];                      // Check whether a node has leaves for childrn
    lEqual       =   leafTransactions[Leaf_Equal        ];                      // Locate an equal key
    lFirstLeaf   =   leafTransactions[Leaf_FirstLeaf    ];                      // Locate the first greater or equal key in a leaf
    lT           =   leafTransactions[Leaf_T            ];                      // Process a parent node
    lL           =   leafTransactions[Leaf_tl           ];                      // Process a left node
    lR           =   leafTransactions[Leaf_tr           ];                      // Process a right node

    for (int i = maxSize(); i > 0; --i)                                         // Put all the nodes on the free chain at the start with low nodes first
     {final NodeTransaction n = nodeTypes[0];                                   // Any node type will do at this point
      n.node = i - 1;
      n.clear();
      final int  f = getInt(freedChain);
                     setInt(free, f,    n.node);
                     setInt(freedChain, n.node);
     }

     {allocate(root, false);                                                    // The root is always at zero, which frees zero to act as the end of list marker on the free chain
      root.setLeaf();                                                           // The root starts as a leaf
     }
    transactionMemory.layout(transactionLayout());                              // Memory layout for a transaction against the btree
    transactionMemory.memory(new Memory(transactionMemory.layout.size()));      // Memory for a transaction against the btree
   }

  static BtreeSA btreeSML(final int leafKeys, int branchKeys)                   // Define a test btree with the specified dimensions
   {return  new BtreeSA()
     {int maxSize         () {return testMaxSize;}
      int maxKeysPerLeaf  () {return    leafKeys;}
      int maxKeysPerBranch() {return  branchKeys;}
      int bitsPerKey      () {return          16;}
      int bitsPerData     () {return          16;}
      int bitsPerNext     () {return          16;}
      int bitsPerSize     () {return          16;}
     };
   }

  Layout layout()                                                               // Layout describing memory used by btree
   {z();
    final BtreeSA btree = this;

    leafStuck = new StuckSP()                                                   // Leaf
     {int               maxSize() {return btree.maxKeysPerLeaf();}
      int            bitsPerKey() {return btree.bitsPerKey();}
      int           bitsPerData() {return btree.bitsPerData();}
      int           bitsPerSize() {return btree.bitsPerSize();}
     };

    branchStuck = new StuckSP()                                                 // Branch
     {int               maxSize() {return btree.maxKeysPerBranch()+1;}          // Not forgetting top next
      int            bitsPerKey() {return btree.bitsPerKey();}
      int           bitsPerData() {return btree.bitsPerNext();}
      int           bitsPerSize() {return btree.bitsPerSize();}
     };

    final Layout l = Layout.layout();
    leaf         = l.duplicate("leaf",         leafStuck.layout());
    branch       = l.duplicate("branch",       branchStuck.layout());
    branchOrLeaf = l.union    ("branchOrLeaf", leaf,   branch);
    isLeaf       = l.bit      ("isLeaf");
    free         = l.variable ("free",         btree.bitsPerNext());
    Node         = l.structure("node",         isLeaf, free, branchOrLeaf);
    nodes        = l.array    ("nodes",        Node,         maxSize());
    freedChain   = l.variable ("freedChain",   btree.bitsPerNext());
    bTree        = l.structure("bTree",        freedChain  , nodes);
    return l.compile();
   }

  Layout transactionLayout()                                                    // Layout describing memory used by btree
   {z();
    final Layout  t = new Layout();                                             // The layout of the memory used by a transaction against the btree
    node            = t.variable ("node"           , bitsPerNext());            // The number of the node
    parent          = t.variable ("parent"         , bitsPerNext());            // Parent node
    anode           = t.variable ("anode"          , bitsPerNext());            // Node being allocated
    P               = t.variable ("P"              , bitsPerNext());            // Parent node during a split, merge or steal operation
    l               = t.variable ("l"              , bitsPerNext());            // Left node
    r               = t.variable ("r"              , bitsPerNext());            // Right node
    splitLeafSize   = t.variable ("splitLeafSize"  , bitsPerSize());            // Where to split a full leaf
    splitBranchSize = t.variable ("splitBranchSize", bitsPerSize());            // Branch split size
    firstKey        = t.variable ("firstKey"       , bitsPerKey());             // First of right leaf
    lastKey         = t.variable ("lastKey"        , bitsPerKey());             // Last of left leaf
    flKey           = t.variable ("flKey"          , bitsPerKey());             // Key mid way between last of left and first of right
    parentKey       = t.variable ("parentKey"      , bitsPerKey());             // Parent key
    L               = t.variable ("L"              , bitsPerNext());            // Reference to left child
    R               = t.variable ("R"              , bitsPerNext());            // Reference to right child
    nl              = t.variable ("nl"             , bitsPerSize());            // Number in the left child, number in the right child
    nr              = t.variable ("nr"             , bitsPerSize());            // Number in the left child, number in the right child
    lk              = t.variable ("lk"             , bitsPerKey());             // Left  child key
    ld              = t.variable ("ld"             , bitsPerData());            // Left  child data
    rk              = t.variable ("rk"             , bitsPerKey());             // Right child key
    rd              = t.variable ("rd"             , bitsPerData());            // Right child data
    index           = t.variable ("index"          , bitsPerSize());            // Index of a slot in a node
                                                                                // Find in leaf
    search          = t.variable ("search"         , bitsPerKey());             // Search key
    found           = t.bit      ("found");                                     // Whether the key was found
    key             = t.variable ("key"            , bitsPerKey());             // Key to insert
    data            = t.variable ("data"           , bitsPerData());            // Data associated with the  key

    first           = t.variable ("first"          , bitsPerSize());            // Index of first key greater than or equal to the search key
    next            = t.variable ("next"           , bitsPerNext());            // The corresponding next field or top if no such key was found

    success         = t.bit      ("success"        );                           // Inserted or updated if true
    inserted        = t.bit      ("inserted"       );                           // Inserted if true
    nodeTransaction = t.structure("nodeTransaction",                            // Collection of fields used to perform a transaction against the btree
      node, parent, anode, P, l, r, splitLeafSize, splitBranchSize,
      firstKey, lastKey, flKey, parentKey,
      L, R, nl, nr, lk, ld, rk, rd,
      index, search, found, key, data, first, next, success, inserted);
    nodeTransactions = t.array   ("nodeTransactions",                           // Collection of fields used to perform a transaction against the btree
                                   nodeTransaction, Node_length);
    return  t.compile();
   }

//D1 Control                                                                    // Testing, control and integrity

  void ok(String expected) {Test.ok(toString(), expected);}                     // Confirm tree is as expected
  void stop()              {Test.stop(toString());}                             // Stop after printing the tree
  public String toString() {return print();}                                    // Print the tree

//D1 Memory access                                                              // Access to memory

  int  getInt(Layout.Field field)            {z(); return memoryLayout.getInt(field);}
  int  getInt(Layout.Field field, int index) {z(); return memoryLayout.getInt(field, index);}

  void setInt(Layout.Field field, int value)            {z(); memoryLayout.setInt(field, value);}
  void setInt(Layout.Field field, int value, int index) {z(); memoryLayout.setInt(field, value, index);}

//D1 Memory allocation                                                          // Allocate and free memory

  void allocate(NodeTransaction node,  boolean check)                           // Allocate a node with or without checking for sufficient free space
   {z(); final int  f = getInt(freedChain);                                     // Last freed node
    z(); if (check && f == 0) stop("No more memory available");                 // No more free nodes available
    z(); final int  F = getInt(free,       f);                                  // Second to last freed node
                        setInt(freedChain, F);                                  // Make second to last freed node the forst freed nod to liberate the existeing first free node
    node.node = f; node.clear();                                                // Construct and clear the node
    maxNodeUsed  = max(maxNodeUsed, ++nodeUsed);                                // Number of nodes in use
//    return n;
   }

  void allocate(NodeTransaction node) {z(); allocate(node, true);}              // Allocate a node checking for free space

//D1 Components                                                                 // A branch or leaf in the tree

  class NodeTransaction                                                         // A transient description of a branch or leaf in the tree - the actual data is contained in the bit memory
   {int node;                                                                   // The index number of the node
    int parent;                                                                 // Parent node
    int anode;                                                                  // Node being allocated
    int P;                                                                      // Parent node during a split, merge or steal operation
    int l;                                                                      // Left node
    int r;                                                                      // Right node
    int splitLeafSize;                                                          // Where to split a full leaf
    int splitBranchSize;                                                        // Branch split size
    int firstKey;                                                               // First of right leaf
    int lastKey;                                                                // Last of left leaf
    int flKey;                                                                  // Key mid way between last of left and first of right
    int parentKey;                                                              // Parent key
    int L;                                                                      // Reference to left child
    int R;                                                                      // Reference to right child
    int nl;                                                                     // Number in the left child, number in the right child
    int nr;                                                                     // Number in the left child, number in the right child
    int lk;                                                                     // Left  child key
    int ld;                                                                     // Left  child data
    int rk;                                                                     // Right child key
    int rd;                                                                     // Right child data
    int index;                                                                  // Index of a slot in a node
                                                                                // Find equal in leaf
    int    search;                                                              // Search key
    boolean found;                                                              // Whether the key was found
    int       key;                                                              // Key to insert
    int      data;                                                              // Data associated with the  key

    int     first;                                                              // Index of first key greater than or equal to the search key
    int      next;                                                              // The corresponding next field or top if no such key was found

    boolean  success;                                                           // Inserted or updated if true
    boolean inserted;                                                           // Inserted if true

    boolean isLeaf() {z(); return getInt(isLeaf,    node) > 0;}                 // A leaf if true
    void   setLeaf() {z();        setInt(isLeaf, 1, node);}                     // Set as leaf
    void setBranch() {z();        setInt(isLeaf, 0, node);}                     // Set as branch

    void assertLeaf()   {if (!isLeaf()) stop("Leaf required");}
    void assertBranch() {if ( isLeaf()) stop("Branch required");}

    void allocLeaf()  {z(); allocate(nodeTypes[anode]); nodeTypes[anode].setLeaf();  } // Allocate leaf
    void allocBranch(){z(); allocate(nodeTypes[anode]); nodeTypes[anode].setBranch();} // Allocate branch

    void free()                                                                 // Free a new node to make it available for reuse
     {z(); if (node == 0) stop("Cannot free root");                             // The root is never freed
      z(); erase();                                                             // Clear the node to encourage erroneous frees to do damage that shows up quickly.
      final int  f = getInt(freedChain);                                        // Last freed node from head of free chain
                     setInt(free,    f, node);                                  // Chain this node in front of the last freed node
                     setInt(freedChain, node);                                  // Make this node the head of the free chain
      maxNodeUsed  = max(maxNodeUsed, --nodeUsed);                              // Number of nodes in use
     }

    void clear()                                                                // Clear a new node to zeros ready for use
     {z();
      final Layout.Field n = BtreeSA.this.Node;
      final int at = n.at(node), w = n.width;
      memoryLayout.memory.zero(at, w);
     }

    void erase()                                                                // Clear a new node to ones as this is likely to create invalid values that will be easily detected in the case of erroneous frees
     {z();
      final Layout.Field n = BtreeSA.this.Node;
      final int at = n.at(node), w = n.width;
      memoryLayout.memory.ones(at, w);
     }

    int leafBase()   {z(); return leaf  .at(node);}                             // Base of leaf stuck in memory
    int branchBase() {z(); return branch.at(node);}                             // Base of branch stuck in memory

    int leafSize()                                                              // Number of children in body of leaf
     {z();
      lSize.base(leafBase());
      lSize.size();
      return lSize.size;
     }

    int branchSize()                                                            // Number of children in body of branch taking top for granted as it is always there
     {z();
      bSize.base(branchBase());
      bSize.size();
      return bSize.size-1;
     }

    boolean isFull()                                                            // The node is full
     {z(); return isLeaf() ? leafSize() == maxKeysPerLeaf  ():
                           branchSize() == maxKeysPerBranch();
     }

    boolean isLow() {z(); return (isLeaf() ? leafSize() : branchSize()) < 2;}   // The node is low on children making it impossible to merge two sibling children

    boolean hasLeavesForChildren()                                              // The node has leaves for children
     {z(); assertBranch();
      bLeaf.base(branchBase());
      bLeaf.lastElement();
      return nodeTypes[node(Node_temp, bLeaf.tData)].isLeaf();
     }

    int top()                                                                   // The top next element of a branch - only used in printing
     {z(); assertBranch();
      bTop.base(branchBase());
      bTop.index = branchSize();
      bTop.elementAt();
      return bTop.tData;
     }

    public String toString()                                                    // Print a node
     {final StringBuilder s = new StringBuilder();
      if (isLeaf())                                                             // Print a leaf
       {final int N  = leafSize();                                              // Number of elements in leaf
        s.append("Leaf(node:"+node+" size:"+N+")\n");
        lLeaf.base(leafBase());
        for (int i = 0; i < N; i++)                                             // Each element in the leaf
         {lLeaf.index = i;
          lLeaf.elementAt();
          s.append("  "+(i+1)+" key:"+lLeaf.tKey+" data:"+lLeaf.tData+"\n");
         }
       }
      else                                                                      // Print a branch
       {s.append("Branch(node:"+node+" size:"+branchSize()+" top:"+top()+"\n");

        final int N = branchSize();                                             // Number of elements in branch not including top
        bLeaf.base(branchBase());
        for (int i = 0; i < N; i++)
         {bLeaf.index = i; bLeaf.elementAt();
          s.append(String.format("  %2d key:%2d next:%2d\n", i+1, bLeaf.tKey, bLeaf.tData));
         }
        bLeaf.index = N; bLeaf.elementAt();
        s.append("             Top:"+bLeaf.tData+")\n");
       }

      return s.toString();
     }

//D2 Search                                                                     // Search within a node and update the node description with the results

    void findEqualInLeaf()                                                      // Find the first key in the leaf that is equal to the search key
     {z(); assertLeaf();
      lEqual.base(leafBase());

      lEqual.search = search; lEqual.search();
      found         = lEqual.found;
      index         = lEqual.index;
      data          = lEqual.tData;
     }

    public String findEqualInLeaf_toString()                                    // Print details of find equal in leaf node
     {final StringBuilder s = new StringBuilder();
      s.append("FindEqualInLeaf(Leaf:"+node);
      s.append(" Key:"+search+" found:"+found);
      if (found) s.append(" data:"+data+" index:"+index);
      s.append(")\n");
      return s.toString();
     }

    void findFirstGreaterThanOrEqualInLeaf()                                    // Find the first key in the  leaf that is equal to or greater than the search key
     {z(); assertLeaf();
      lFirstLeaf.base(leafBase());
      lFirstLeaf.search = search; lFirstLeaf.searchFirstGreaterThanOrEqual();
      found = lFirstLeaf.found;
      first = lFirstLeaf.index;
     }

    void findFirstGreaterThanOrEqualInBranch()                                  // Find the first key in the branch that is equal to or greater than the search key
     {z(); assertBranch();
      bFirstBranch.base(branchBase());
      bFirstBranch.search = search; bFirstBranch.limit = 1;
      bFirstBranch.searchFirstGreaterThanOrEqual();
      found = bFirstBranch.found;
      first = bFirstBranch.index;
      if (bFirstBranch.found) next = bFirstBranch.tData;                        // Next if key matches else top
      else {bFirstBranch.lastElement(); next = bFirstBranch.tData;}
     }

//D2 Array                                                                      // Represent the contents of the tree as an array

    void leafToArray(Stack<ArrayElement> s)                                     // Leaf as an array
     {z(); assertLeaf();
      final int     K = leafSize();
      final StuckSP t = lLeaf.copy();
      t.base(leafBase());
      for  (int i = 0; i < K; i++)
       {z();
        t.index = i; t.elementAt();
        s.push(new ArrayElement(i, t.tKey, t.tData));
       }
     }

    void branchToArray(Stack<ArrayElement> s)                                   // Branch to array
     {z(); assertBranch();
      final int K = branchSize()+1;                                             // Include top next

      if (K > 0)                                                                // Branch has key, next pairs
       {z();
        final StuckSP t = bLeaf.copy();
        t.base(branchBase());
        for  (int i = 0; i < K; i++)
         {z();
          t.index = i; t.elementAt();                                           // Each node in the branch
          final NodeTransaction n = node(new NodeTransaction(), t.tData);       // Using recursion here is unsatisfactory.

          if (n.isLeaf()) {z(); n.leafToArray(s);}
          else
           {z();
            if (t.tData == 0)
             {say("Cannot descend through root from index", i,
                  "in branch", node);
              break;
             }
            z(); n.branchToArray(s);
           }
         }
       }
     }

//D2 Print                                                                      // Print the contents of the tree

    void printLeaf(Stack<StringBuilder>S, int level)                            // Print leaf horizontally
     {assertLeaf();
      padStrings(S, level);
      final StringBuilder s = new StringBuilder();                              // String builder
      final int     K = leafSize();
      final StuckSP t = lLeaf.copy();
      t.base(leafBase());

      for  (int i = 0; i < K; i++)
       {t.index = i; t.elementAt();                                             // Each node in the branch
        s.append(""+t.tKey+",");
       }
      if (s.length() > 0) s.setLength(s.length()-1);                            // Remove trailing comma if present
      s.append("="+node+" ");
      S.elementAt(level*linesToPrintABranch).append(s.toString());
      padStrings(S, level);
     }

    void printBranch(Stack<StringBuilder>S, int level)                          // Print branch horizontally
     {assertBranch();
      if (level > maxPrintLevels) return;
      padStrings(S, level);
      final int L = level * linesToPrintABranch;
      final int K = branchSize();

      if (K > 0)                                                                // Branch has key, next pairs
       {final StuckSP t = bLeaf.copy();
        t.base(branchBase());
        for  (int i = 0; i < K; i++)
         {t.index = i; t.elementAt();                                           // Each node in the branch
          final NodeTransaction n = node(new NodeTransaction(), t.tData);       // Printing will not be part of the chip so we can use recursion and create new nodes as needed
          if (n.isLeaf())
           {n.printLeaf(S, level+1);
           }
          else
           {if (t.tData == 0)
             {say("Cannot descend through root from index", i,
                  "in branch", node);
              break;
             }
            n.printBranch(S, level+1);
           }

          S.elementAt(L+0).append(""+t.tKey);                                    // Key
          S.elementAt(L+1).append(""+node+(i > 0 ?  "."+i : ""));               // Index in node
          S.elementAt(L+2).append(""+t.tData);                                   // Next
         }
       }
      else                                                                      // Branch is empty so print just the index of the branch
       {S.elementAt(L+0).append(""+node+"Empty");
       }
      final int top = top();                                                    // Top next will always be present
      S.elementAt(L+3).append(top);                                             // Append top next

      final NodeTransaction n = node(new NodeTransaction(), top);
      if (n.isLeaf())                                                           // Print leaf
       {n.printLeaf(S, level+1);
       }
      else                                                                      // Print branch
       {if (top == 0)
         {say("Cannot descend through root from top in branch:", node);
          return;
         }
        n.printBranch(S, level+1);
       }

      padStrings(S, level);
     }

    public String find_toString()                                               // Print find result
     {final StringBuilder s = new StringBuilder();
      s.append("Find(search:"+node);
      s.append(    " search:"+search);
      s.append(     " found:"+found);
      s.append(      " data:"+data);
      s.append(     " index:"+index);
      s.append(")\n");
      return s.toString();
     }

    public String findAndInsert_toString()                                      // Print find and insert result
     {final StringBuilder s = new StringBuilder();
      s.append("FindAndInsert(");
      s.append(    " key:"+key);
      s.append(   " data:"+data);
      s.append(" success:"+success);
      if (success) s.append(" inserted:"+inserted);
      s.append(")\n" );
      return s.toString();
     }

    public String findFirstGreaterThanOrEqualInLeaf_toString()                  // Print results of search
     {final StringBuilder s = new StringBuilder();
      s.append("FindFirstGreaterThanOrEqualInLeaf(");
      s.append(  "Leaf:"+node);
      s.append(  " Key:"+search);
      s.append(" found:"+found);
      if (found) s.append(" first:"+first);
      s.append(")\n");
      return s.toString();
     }

    public String findFirstGreaterThanOrEqualInBranch_toString()                // Print search results
     {final StringBuilder s = new StringBuilder();
      s.append("FindFirstGreaterThanOrEqualInBranch(");
      s.append("branch:"+node);
      s.append(  " Key:"+search);
      s.append(" found:"+found);
      s.append( " next:"+next);
      if (found) s.append(" first:"+first);
      s.append(")\n");
      return s.toString();
     }

//D2 Split                                                                      // Split nodes in half to increase the number of nodes in the tree

    void splitLeafRoot()                                                        // Split a leaf which happens to be a full root into two half full leaves while transforming the root leaf into a branch
     {z(); assertLeaf();
      z(); if (node != 0) stop("Wanted root, but got node:", node);
      z(); if (!isFull()) stop("Root is not full, but has size:", leafSize());

      P = Node_root;                                                            // Root is the parent
      l = anode = Node_left;  allocLeaf();                                      // New left leaf
      r = anode = Node_right; allocLeaf();                                      // New right leaf

      lT.base(nodeTypes[P].leafBase());                                         // Set address of the referenced leaf stuck
      lL.base(nodeTypes[l].leafBase());                                         // Set address of the referenced leaf stuck
      lR.base(nodeTypes[r].leafBase());                                         // Set address of the referenced leaf stuck

      splitLeafSize = splitLeafSize();

      for (int i = 0; i < splitLeafSize; i++)                                   // Build left leaf from parent
       {z(); lT.shift(); lL.tKey = lT.tKey; lL.tData = lT.tData; lL.push();
       }
      for (int i = 0; i < splitLeafSize; i++)                                   // Build right leaf from parent
       {z(); lT.shift(); lR.tKey = lT.tKey; lR.tData = lT.tData; lR.push();
       }

      lR.firstElement();
      lL. lastElement();
      setBranch();
      bT.base(nodeTypes[P].branchBase());                                       // Set address of the referenced leaf stuck
      bT.clear();                                                               // Clear the branch
      firstKey = lR.tKey;                                                       // First of right leaf
      lastKey  = lL.tKey;                                                       // Last of left leaf
      flKey    = (lastKey + firstKey) / 2;                                      // Mid key
      bT.tKey  = flKey; bT.tData = nodeTypes[l].node; bT.push();                // Insert left leaf into root
      bT.tKey  =     0; bT.tData = nodeTypes[r].node; bT.push();                // Insert right into root. This will be the top node and so ignored by search ... except last.
     }

    void splitBranchRoot()                                                      // Split a branch which happens to be a full root into two half full branches while retaining the current branch as the root
     {z(); assertBranch();
      z(); if (node != 0) stop("Not root, but node:", node);
      z(); if (!isFull()) stop("Root is not full, but has size:", branchSize());
      z();

      P = Node_root;                                                            // Root is the parent
      l = anode = Node_left;  allocBranch();                                    // New left branch
      r = anode = Node_right; allocBranch();                                    // New right branch

      bT.base(nodeTypes[P].branchBase());                                       // Set address of the referenced branch stuck
      bL.base(nodeTypes[l].branchBase());                                       // Set address of the referenced branch stuck
      bR.base(nodeTypes[r].branchBase());                                       // Set address of the referenced branch stuck

      splitBranchSize = splitBranchSize();                                      // Branch split size
      for (int i = 0; i < splitBranchSize; i++)                                 // Build left child from parent
       {z(); bT.shift(); bL.tKey = bT.tKey; bL.tData = bT.tData; bL.push();
       }
      bT.shift();                                                               // This key, next pair will be part of the root
      parentKey = bT.tKey;
      bL.tKey = 0; bL.tData = bT.tData; bL.push();                              // Becomes top and so ignored by search ... except last

      for(int i = 0; i < splitBranchSize; i++)                                  // Build right child from parent
       {z(); bT.shift(); bR.tKey = bT.tKey; bR.tData = bT.tData; bR.push();
       }

      bT.shift(); bR.tKey = 0; bR.tData = bT.tData; bR.push();                  // Becomes top and so ignored by search ... except last

      bT.clear();                                                               // Refer to new branches from root
      bT.tKey = parentKey; bT.tData = nodeTypes[l].node; bT.push();
      bT.tKey =         0; bT.tData = nodeTypes[r].node; bT.push();             // Becomes top and so ignored by search ... except last
     }

    void splitLeaf()                                                            // Split a leaf which is not the root
     {z(); assertLeaf();
      z(); if (node == 0) stop("Cannot split root with this method");
      z(); final int S = leafSize(), I = index;
      z(); final boolean nif = !isFull(), pif = nodeTypes[parent].isFull();
      z(); if (nif)   stop("Leaf:", node, "is not full, but has:", S, this);
      z(); if (pif)   stop("Parent:", parent, "must not be full");
      z(); if (I < 0) stop("Index", I, "too small");
      z(); if (I > S) stop("Index", I, "too big for leaf with:", S);
      z();
      l = anode = Node_left; allocLeaf();                                       // New  split out leaf

      lL.base(nodeTypes[l].leafBase());                                         // The leaf being split into
      lR.base(             leafBase());                                         // The leaf being split on the right

      splitLeafSize = splitLeafSize();
      for (int i = 0; i < splitLeafSize; i++)                                   // Build left leaf
       {z(); lR.shift(); lL.tKey = lR.tKey; lL.tData = lR.tData; lL.push();
       }
      lR.firstElement();
      lL. lastElement();
      P = parent;                                                               // Parent
      bT.base(nodeTypes[P].branchBase());                                       // The parent branch
      bT.tKey  = (lR.tKey + lL.tKey) / 2;                                       // Splitting key
      bT.tData = nodeTypes[l].node;
      bT.index = index;
      bT.insertElementAt();                                                     // Insert new key, next pair in parent
     }

    void splitBranch()                                                          // Split a branch which is not the root by splitting right to left
     {z(); assertBranch();
      z(); final int bs = branchSize(), I = index, pn = nodeTypes[parent].node, nd = node;
      z(); final boolean nif = !isFull(), pif = nodeTypes[parent].isFull();
      z(); if (nd == 0) stop("Cannot split root with this method");
      z(); if (nif)     stop("Branch:", nd, "is not full, but", bs);
      z(); if (pif)     stop("Parent:", pn, "must not be full");
      z(); if (I < 0)   stop("Index", I, "too small in node:", nd);
      z(); if (I > bs)  stop("Index", I, "too big for branch with:",
                              bs, "in node:", nd);
      z();
      P = parent;
      l = anode = Node_left; allocBranch();

      bT.base(nodeTypes[P].branchBase());                                       // The parent branch
      bL.base(nodeTypes[l].branchBase());                                       // The branch being split into
      bR.base(             branchBase());                                       // The branch being split

      splitBranchSize = splitBranchSize();
      for (int i = 0; i < splitBranchSize; i++)                                 // Build left branch from right
       {z(); bR.shift(); bL.tKey = bR.tKey; bL.tData = bR.tData; bL.push();
       }
      bR.shift(); bL.tKey = 0; bL.tData = bR.tData; bL.push();                  // Build right branch - becomes top and so is ignored by search ... except last
      bT.tKey = bR.tKey; bT.tData = nodeTypes[l].node; bT.index = index; bT.insertElementAt();
     }

    boolean stealFromLeft()                                                     // Steal from the left sibling of the indicated child if possible to give to the right - Dennis Moore, Dennis Moore, Dennis Moore.
     {z(); assertBranch();
      z(); if (index == 0) {z(); return false;}
      z(); if (index < 0)            stop("Index", index, "too small");
      z(); if (index > branchSize()) stop("Index", index, "too big");
      z();

      bT.base(branchBase());
      bT.index = index - 1; bT.elementAt(); L = bT.tData;
      bT.index = index - 0; bT.elementAt(); R = bT.tData;

      l = node(Node_left,  L);
      r = node(Node_right, R);

      if (hasLeavesForChildren())                                               // Children are leaves
       {z();
        lL.base(nodeTypes[l].leafBase());
        lR.base(nodeTypes[r].leafBase());

        nl = nodeTypes[l].leafSize();
        nr = nodeTypes[r].leafSize();

        z(); if (nr >= maxKeysPerLeaf()) {z(); return false;}                   // Steal not possible because there is no where to put the steal
        z(); if (nl <= 1)                {z(); return false;}                   // Steal not allowed because it would leave the leaf sibling empty
        z();

        lL.lastElement(); lR.tKey = lL.tKey; lR.tData = lL.tData; lR.unshift(); // Increase right

        lL.pop();                                                               // Reduce left
        lL.index = nl-2; lL.elementAt();                                        // Last key on left
        bT.tKey  = lL.tKey; bT.tData = L; bT.index = index-1; bT.setElementAt();// Reduce key of parent of left
       }
      else                                                                      // Children are branches
       {z();
        bL.base(nodeTypes[l].branchBase());
        bR.base(nodeTypes[r].branchBase());
        nl =    nodeTypes[l].branchSize();
        nr =    nodeTypes[r].branchSize();

        z(); if (nr >= maxKeysPerBranch()) {z(); return false;}                 // Steal not possible because there is no where to put the steal
        z(); if (nl <= 1)                  {z(); return false;}                 // Steal not allowed because it would leave the left sibling empty
        z();

        bL.lastElement();                                                       // Increase right with left top
        bT.index = index; bT.elementAt();                                       // Top key
        bR.tKey  = bT.tKey; bR.tData = bL.tData; bR.unshift();                  // Increase right with left top
        bL.pop();                                                               // Remove left top

        bR.firstElement();                                                      // Increase right with left top

        bT.index = index-1; bT.elementAt();                                     // Parent key
        bR.tKey  = bT.tKey; bR.index = 0; bR.setElementAt();                    // Reduce key of parent of right
        bL.lastElement();                                                       // Last left key
        bT.tKey = bL.tKey; bT.tData = L; bT.index = index-1; bT.setElementAt(); // Reduce key of parent of left
       }
      z(); return true;
     }

    boolean stealFromRight()                                                    // Steal from the right sibling of the indicated child if possible
     {z(); assertBranch();
      z(); if (index == branchSize()) {z(); return false;}
      z(); if (index < 0)             stop("Index", index, "too small");
      z(); if (index >= branchSize()) stop("Index", index, "too big");
      z();

      bT.base(branchBase());
      bT.index = index+0; bT.elementAt(); lk = bT.tKey; ld = bT.tData;
      bT.index = index+1; bT.elementAt(); rk = bT.tKey; rd = bT.tData;

      l = node(Node_left,  ld);
      r = node(Node_right, rd);

      if (hasLeavesForChildren())                                               // Children are leaves
       {z();
        lL.base(nodeTypes[l].leafBase());
        lR.base(nodeTypes[r].leafBase());
        nl =    nodeTypes[l].leafSize();
        nr =    nodeTypes[r].leafSize();

        z(); if (nl >= maxKeysPerLeaf()) {z(); return false;}                   // Steal not possible because there is no where to put the steal
        z(); if (nr <= 1)                {z(); return false;}                   // Steal not allowed because it would leave the right sibling empty
        z();
        lR.firstElement();                                                      // First element of right child
        lL.tKey = lR.tKey; lL.tData = lR.tData; lL.push();                      // Increase left
        bT.tKey = lR.tKey; bT.tData = ld; bT.index = index; bT.setElementAt();  // Swap key of parent
        lR.shift();                                                             // Reduce right
       }
      else                                                                      // Children are branches
       {z();
        bL.base(nodeTypes[l].branchBase());
        bR.base(nodeTypes[r].branchBase());
        nl =    nodeTypes[l].branchSize();
        nr =    nodeTypes[r].branchSize();

        z(); if (nl >= maxKeysPerBranch()) {z(); return false;}                 // Steal not possible because there is no where to put the steal
        z(); if (nr <= 1)                  {z(); return false;}                 // Steal not allowed because it would leave the right sibling empty
        z();

        bL.lastElement();                                                       // Last element of left child
        bL.tKey = lk; bL.index = nl; bL.setElementAt();                         // Left top becomes real

        bR.firstElement();                                                      // First element of  right child

        bL.tKey = 0; bL.tData = bR.tData; bL.push();                            // New top for left is ignored by search ,.. except last
        bT.tKey = bR.tKey; bT.tData = ld; bT.index = index; bT.setElementAt();  // Swap key of parent
        bR.shift();                                                             // Reduce right
       }
      z(); return true;
     }

//D2 Merge                                                                      // Merge two nodes together and free the resulting free node

    boolean mergeRoot()                                                         // Merge into the root
     {z();
      if (nodeTypes[Node_root].isLeaf() || branchSize() > 1)
       {z(); return false;
       }
      z(); if (node != 0) stop("Expected root, got:", node);
      z();
      bT.base(branchBase());
      bT.firstElement(); l = node(Node_left,  bT.tData);
      bT. lastElement(); r = node(Node_right, bT.tData);

      if (hasLeavesForChildren())                                               // Leaves
       {z();
        nl = nodeTypes[l].leafSize();
        nr = nodeTypes[r].leafSize();
        if (nl + nr <= maxKeysPerLeaf())
         {z(); bT.clear();
          lL.base(nodeTypes[l].leafBase());
          lR.base(nodeTypes[r].leafBase());
          for (int i = 0; i < nl; ++i)                                          // Merge in left child leaf
           {z(); lL.shift(); bT.tKey = lL.tKey; bT.tData = lL.tData; bT.push();
           }
          for (int i = 0; i < nr; ++i)                                          // Merge in right child leaf
           {z(); lR.shift(); bT.tKey  = lR.tKey; bT.tData = lR.tData; bT.push();
           }
          setLeaf();                                                            // The root is now a leaf
          nodeTypes[l].free();                                                  // Free the children
          nodeTypes[r].free();
          z(); return true;
         }
        z(); return false;
       }
      else                                                                      // Branches
       {nl = nodeTypes[l].branchSize();
        nr = nodeTypes[r].branchSize();

        if (nl + 1 + nr <= maxKeysPerBranch())
         {z();
          bL.base(nodeTypes[l].branchBase());
          bR.base(nodeTypes[r].branchBase());
          bT.firstElement();
          parentKey = bT.tKey;
          bT.clear();
          for (int i = 0; i < nl; ++i)                                          // Merge left child branch
           {z(); bL.shift(); bT.tKey  = bL.tKey; bT.tData = bL.tData; bT.push();
           }

          bL.lastElement();
          bT.tKey = parentKey; bT.tData = bL.tData; bT.push();

          for (int i = 0; i < nr; ++i)                                          // Merge right child branch
           {z(); bR.shift(); bT.tKey  = bR.tKey; bT.tData = bR.tData; bT.push();
           }

          bR.lastElement();                                                     // Top next

          bT.tKey = 0; bT.tData = bR.tData; bT.push();                          // Top so ignored by search ... except last
          nodeTypes[l].free();                                                  // Free the children
          nodeTypes[r].free();
          z(); return true;
         }
        z(); return false;
       }
     }

    boolean mergeLeftSibling()                                                  // Merge the left sibling
     {z(); assertBranch();
      z(); if (index == 0) {z(); return false;}
      final int bs = branchSize();
      final String s = "for branch of size: "+bs;
      z(); if (index < 0 ) stop("Index", index, "too small", s);
      z(); if (index > bs) stop("Index", index, "too big",   s);
      //if (branchSize() < 2)     stop("Node:", this,  "must have two or more children");
      z(); if (bs    < 2 ) {z(); return false;}
      z();
      bT.base(branchBase());
      bT.index = index-1; bT.elementAt(); L = bT.tData;
      bT.index = index-0; bT.elementAt(); R = bT.tData;

      l = node(Node_left,  L);
      r = node(Node_right, R);

      if (hasLeavesForChildren())                                               // Children are leaves
       {z();
        lL.base(nodeTypes[l].leafBase());
        lR.base(nodeTypes[r].leafBase());
        nl = nodeTypes[l].leafSize();
        nr = nodeTypes[r].leafSize();

        if (nl + nr >= maxKeysPerLeaf()) {z(); return false;}                   // Combined body would be too big
        for (int i = 0; i < nl; i++)                                            // Transfer left to right
         {z(); lL.pop(); lR.tKey = lL.tKey; lR.tData = lL.tData; lR.unshift();
         }
       }
      else                                                                      // Children are branches
       {z();
        bL.base(nodeTypes[l].branchBase());
        bR.base(nodeTypes[r].branchBase());
        nl =    nodeTypes[l].branchSize();
        nr =    nodeTypes[r].branchSize();

        if (nl + 1 + nr > maxKeysPerBranch()) {z(); return false;}              // Merge not possible because there is not enough room for the combined result
        z();
        bT.index = index-1;                                                     // Top key
        bT.elementAt();                                                         // Top key
        bL.lastElement();                                                       // Last element of left child
        bR.tKey = bT.tKey; bR.tData = bL.tData; bR.unshift();                   // Left top to right

        bL.pop();                                                               // Remove left top
        for (int i = 0; i < nl; i++)                                            // Transfer left to right
         {z(); bL.pop(); bR.tKey = bL.tKey; bR.tData = bL.tData; bR.unshift();
         }
       }
      nodeTypes[l].free();                                                      // Free the empty left node
      bT.index = index - 1;
      bT.removeElementAt();                                                     // Reduce parent on left
      {z(); return true;}
     }

    boolean mergeRightSibling()                                                 // Merge the right sibling
     {z(); assertBranch();
      final int bs = branchSize();
      final String s = "for branch of size: "+bs;
      z(); if (index >= bs) {z(); return false;}
      z(); if (index <  0 ) stop("Index", index, "too small", s);
      z(); if (index >  bs) stop("Index", index, "too big",   s);
      //if (branchSize() < 2)     stop("Node:", this,  "must have two or more children");
      z(); if (bs < 2) {z(); return false;}
      z();
      bT.base(branchBase());
      bT.index = index+0; bT.elementAt(); L = bT.tData;
      bT.index = index+1; bT.elementAt(); R = bT.tData;

      l = node(Node_left,  L);
      r = node(Node_right, R);

      if (hasLeavesForChildren())                                               // Children are leaves
       {z();
        lL.base(nodeTypes[l].leafBase());
        lR.base(nodeTypes[r].leafBase());
        nl =    nodeTypes[l].leafSize();
        nr =    nodeTypes[r].leafSize();

        if (nl + nr > maxKeysPerLeaf()) {z(); return false;}                    // Combined body would be too big
        for (int i = 0; i < nr; i++)                                            // Transfer right to left
         {z(); lR.shift(); lL.tKey  = lR.tKey; lL.tData = lR.tData; lL.push();
         }
       }
      else                                                                      // Children are branches
       {z();
        bL.base(nodeTypes[l].branchBase());
        bR.base(nodeTypes[r].branchBase());
        nl =    nodeTypes[l].branchSize();
        nr =    nodeTypes[r].branchSize();

        if (nl + 1 + nr > maxKeysPerBranch()) {z(); return false;}              // Merge not possible because there is no where to put the steal

        z(); bL.lastElement();                                                  // Last element of left child
        z(); bT.index = index;
        z(); bT.elementAt();                                                    // Parent dividing element

        bL.tKey = bT.tKey; bL.index = nl;                                       // Re-key left top
        bL.setElementAt();                                                      // Re-key left top

        for (int i = 0; i < nr+1; i++)                                          // Transfer right to left
         {z(); bR.shift(); bL.tKey  = bR.tKey; bL.tData = bR.tData; bL.push();
         }
       }
      nodeTypes[r].free();                                                      // Free the empty right node

      bT.index = index+1; bT.elementAt(); parentKey = bT.tKey;                  // One up from dividing point in parent
      bT.index = index;   bT.elementAt();                                       // Dividing point in parent
      bT.tKey  = parentKey;
      bT.setElementAt();                                                        // Install key of right sibling in this child
      bT.index = index+1;                                                       // Reduce parent on right
      bT.removeElementAt();                                                     // Reduce parent on right
      z(); return true;
     }

//D2 Balance                                                                    // Balance the tree by merging and stealing

    void balance()                                                              // Augment the indexed child so it has at least two children in its body
     {z(); assertBranch();
      z(); if (index < 0)            stop("Index", index, "too small");
      z(); if (index > branchSize()) stop("Index", index, "too big");
      z(); if (isLow() && node != root.node)
            {stop("Parent:", node, "must not be low on children");
            }
      z();

      bT.base(branchBase());
      bT.index = index;
      bT.elementAt();

      z();
      z(); if (!node(nodeTypes[Node_temp], bT.tData).isLow()) return;
      z(); if (stealFromLeft    ())                   return;
      z(); if (stealFromRight   ())                   return;
      z(); if (mergeLeftSibling ())                   return;
      z(); if (mergeRightSibling())                   return;
      stop("Unable to balance child:", bT.tData);
     }
   }  // Node

  int node(int nodeType, int node)                                              // Refer to a node by number
   {nodeTypes[nodeType].node = node;
    return nodeType;
   }

  NodeTransaction node(NodeTransaction Node, int node)                          // Refer to a node by number
   {Node.node = node;
    return Node;
   }

//D1 Array                                                                      // Key, data pairs in the tree as an array

  class ArrayElement                                                            // A key, data pair in the btree as an array element
   {final int i, key, data;
    ArrayElement(int I, int Key, int Data)
     {i = I; key = Key; data = Data;
     }
    public String toString()
     {return "("+i+", key:"+key+" data:"+data+")\n";
     }
   }

  Stack<ArrayElement> toArray()                                                 // Key, data pairs in the tree as an array
   {z();
    final Stack<ArrayElement> s = new Stack<>();
    if (root.isLeaf()) {z(); root.  leafToArray(s);}
    else               {z(); root.branchToArray(s);}
    return s;
   }

//D1 Print                                                                      // Print a BTree horizontally

   String printBoxed()                                                          // Print a tree in a box
    {final String  s = toString();
     final int     n = longestLine(s)-1;
     final String[]L = s.split("\n");
     final StringBuilder t = new StringBuilder();
     t.append("+"); t.append("-".repeat(n)); t.append("+\n");
     for(String l : L) t.append("| "+l+"\n");
     t.append("+"); t.append("-".repeat(n)); t.append("+\n");
     return t.toString();
    }

  void padStrings(Stack<StringBuilder> S, int level)                            // Pad the strings at each level of the tree so we have a vertical face to continue with - a bit like Marc Brunel's tunneling shield
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

  String print()                                                                // Print a tree horizontally
   {z();
    final Stack<StringBuilder> S = new Stack<>();

    if (root.isLeaf())
     {z(); root.printLeaf(S, 0);
     }
    else
     {z(); root.printBranch(S, 0);
     }
    return printCollapsed(S);
   }

//D1 Find                                                                       // Find the data associeted with a key.

  class Transaction                                                             // A transaction on the btree
   {BtreeSA btree;                                                              // Btree being processed
    int           Key;                                                          // Key being found, inserted or deleted
    int          Data;                                                          // Data found, inserted or deleted
    int          find;                                                          // Results of a find operation
    int findAndInsert;                                                          // Results of a find and insert operation
    int        parent;                                                          // Parent node in a descent through the tree
    int         child;                                                          // Child node in a descent through the tree
    int          leaf;                                                          // Leaf found by find
    Integer    delete;                                                          // Results of a deletion

    int find()                                                                  // Find the data associated with a key in the tree
     {z();
      if (root.isLeaf())                                                        // The root is a leaf
       {z(); root.search = Key; root.findEqualInLeaf();
        return Node_root;
       }

      parent = Node_root;                                                       // Parent starts at root which is known to be a branch

      for (int i = 0; i < maxDepth; i++)                                        // Step down through tree
       {z();
        nodeTypes[parent].search = Key; nodeTypes[parent].findFirstGreaterThanOrEqualInBranch(); // Find next child in search path of key
        child = node(Node_find, nodeTypes[parent].next);

        if (nodeTypes[child].isLeaf())                                          // Found the containing search
         {z(); nodeTypes[child].search = Key;
          nodeTypes[child].findEqualInLeaf();
          return child;
         }
        parent = node(Node_parent, nodeTypes[child].node);                      // Step down to lower branch
       }
      stop("Search did not terminate in a leaf");
      return -1;
     }

    int findAndInsert()                                                         // Find the leaf that should contain this key and insert or update it is possible
     {z();
      leaf = find();                                                            // Find the leaf that should contain this key

      final NodeTransaction l = nodeTypes[leaf];
      lT.base(l.leafBase());

      if (l.found)                                                              // Found the key in the leaf so update it with the new data
       {z(); lT.tKey = Key; lT.tData = Data; lT.index = l.index; lT.setElementAt();
        l.success = true; l.inserted = false;
        return leaf;
       }

      if (!l.isFull())                                                          // Leaf is not full so we can insert immediately
       {z();
        l.search = Key; l.findFirstGreaterThanOrEqualInLeaf();
        if (l.found)                                                            // Overwrite existing key
         {z();
          lT.tKey = Key; lT.tData = Data; lT.index = l.first; lT.insertElementAt();
         }
        else                                                                    // Insert into position
         {z(); lT.tKey = Key; lT.tData = Data; lT.push();
         }
        l.success = true;
        return leaf;
       }
      z(); l.success = false;
      return leaf;
     }

//D1 Insertion                                                                  // Insert a key, data pair into the tree or update and existing key with a new datum

    void put()                                                                  // Insert a key, data pair into the tree or update and existing key with a new datum
     {z(); findAndInsert = findAndInsert();                                     // Try direct insertion with no modifications to the shape of the tree
      if (nodeTypes[findAndInsert].success) return;                             // Inserted or updated successfully
      z();

      if (root.isFull())                                                        // Start the insertion at the root(), after splitting it if necessary
       {z();
        if (root.isLeaf()) {z(); root.splitLeafRoot();}
        else               {z(); root.splitBranchRoot();}
        z();
        findAndInsert = findAndInsert();                                        // Splitting the root() might have been enough
        if (nodeTypes[findAndInsert].success) return;                           // Inserted or updated successfully
       }
      z(); parent = Node_root;

      for (int i = 0; i < maxDepth; i++)                                        // Step down from branch to branch through the tree until reaching a leaf repacking as we go
       {z();
        nodeTypes[parent].search = Key;
        nodeTypes[parent].findFirstGreaterThanOrEqualInBranch();
        child = node(Node_put, nodeTypes[parent].next);
        if (nodeTypes[child].isLeaf())                                          // Reached a leaf
         {z();
          nodeTypes[child].parent = parent;
          nodeTypes[child].index = nodeTypes[parent].first;
          nodeTypes[child].splitLeaf();                                         // Split the child leaf
          findAndInsert();
          merge();
          return;
         }
        z();
        if (nodeTypes[child].isFull())
         {z();
          nodeTypes[child].parent = parent;
          nodeTypes[child].index = nodeTypes[parent].first;
          nodeTypes[child].splitBranch();                                       // Split the child branch in the search path for the key from the parent so the the search path does not contain a full branch above the containing leaf

          nodeTypes[parent].search = Key;
          nodeTypes[parent].findFirstGreaterThanOrEqualInBranch();              // Perform the step down again as the split will have altered the local layout
          parent = node(Node_parent, nodeTypes[parent].next);
         }
        else                                                                    // Step down directly as no split was required
         {z(); parent = node(Node_parent, nodeTypes[child].node);
         }
       }
      stop("Fallen off the end of the tree");                                   // The tree must be missing a leaf
     }

//D1 Deletion                                                                   // Delete a key, data pair from the tree

    Integer findAndDelete()                                                     // Delete a key from the tree and returns its data if present without modifying the shape of tree
     {z(); find = find();                                                       // Try direct insertion with no modifications to the shape of the tree
      if (!nodeTypes[find].found) return null;                                  // Inserted or updated successfully
      z();
      lT.base(nodeTypes[find].leafBase());                                      // The leaf that contains the key
      lT.index = nodeTypes[find].index; lT.elementAt();                         // Position in the leaf of the key

      Data = lT.tData;                                                          // Key, data pairs in the leaf
      lT.removeElementAt();                                                     // Remove the key, data pair from the leaf
      return Data;
     }

    Integer delete()                                                            // Insert a key, data pair into the tree or update and existing key with a new datum
     {z(); root.mergeRoot();

      if (root.isLeaf())                                                        // Find and delete directly in root as a leaf
       {z(); return findAndDelete();
       }
      z();

      parent = Node_root;                                                       // Start at root

      for (int i = 0; i < maxDepth; i++)                                        // Step down from branch to branch through the tree until reaching a leaf repacking as we go
       {z();                                                                    // Step down
        nodeTypes[parent].search = Key;
        nodeTypes[parent].findFirstGreaterThanOrEqualInBranch();

        nodeTypes[parent].index = nodeTypes[parent].first;
        nodeTypes[parent].balance();
        child = node(Node_delete, nodeTypes[parent].next);

        if (nodeTypes[child].isLeaf())                                          // Reached a leaf
         {z();
          final Integer data = findAndDelete();
          merge();
          return data;
         }
        z(); parent = node(Node_parent, nodeTypes[child].node);
       }
      stop("Fallen off the end of the tree");                                   // The tree must be missing a leaf
      return null;
     }

//D1 Merge                                                                      // Merge along the specified search path

    void merge()                                                                // Merge along the specified search path
     {z();
      root.mergeRoot();
      parent = Node_root;                                                       // Start at root

      for (int i = 0; i < maxDepth; i++)                                        // Step down from branch to branch through the tree until reaching a leaf repacking as we go
       {z(); if (nodeTypes[parent].isLeaf()) return;
        z();
        for (int j = 0; j < nodeTypes[parent].branchSize(); j++)                // Try merging each sibling pair which might change the size of the parent
         {z();
          nodeTypes[parent].index = j; if (nodeTypes[parent].mergeLeftSibling ()) --j; // A successful merge of the left  sibling reduces the current index and the upper limit
          nodeTypes[parent].index = j;     nodeTypes[parent].mergeRightSibling();      // A successful merge of the right sibling maintains the current position but reduces the upper limit
         }

        nodeTypes[parent].search = Key;
        nodeTypes[parent].findFirstGreaterThanOrEqualInBranch();                // Step down
        parent = node(Node_parent, nodeTypes[parent].next);
       }
      stop("Fallen off the end of the tree");                                   // The tree must be missing a leaf
     }
   }

//D0 Tests                                                                      // Testing

  final static int[]random_small = {27, 442, 545, 317, 511, 578, 391, 993, 858, 586, 472, 906, 658, 704, 882, 246, 261, 501, 354, 903, 854, 279, 526, 686, 987, 403, 401, 989, 650, 576, 436, 560, 806, 554, 422, 298, 425, 912, 503, 611, 135, 447, 344, 338, 39, 804, 976, 186, 234, 106, 667, 494, 690, 480, 288, 151, 773, 769, 260, 809, 438, 237, 516, 29, 376, 72, 946, 103, 961, 55, 358, 232, 229, 90, 155, 657, 681, 43, 907, 564, 377, 615, 612, 157, 922, 272, 490, 679, 830, 839, 437, 826, 577, 937, 884, 13, 96, 273, 1, 188};
  final static int[]random_large = {5918,5624,2514,4291,1791,5109,7993,60,1345,2705,5849,1034,2085,4208,4590,7740,9367,6582,4178,5578,1120,378,7120,8646,5112,4903,1482,8005,3801,5439,4534,9524,6111,204,5459,248,4284,8037,5369,7334,3384,5193,2847,1660,5605,7371,3430,1786,1216,4282,2146,1969,7236,2187,136,2726,9480,5,4515,6082,969,5017,7809,9321,3826,9179,5781,3351,4819,4545,8607,4146,6682,1043,2890,2964,7472,9405,4348,8333,2915,9674,7225,4743,995,1321,3885,6061,9958,3901,4710,4185,4776,5070,8892,8506,6988,2317,9342,3764,9859,4724,5195,673,359,9740,2089,9942,3749,9208,1,7446,7023,5496,4206,3272,3527,8593,809,3149,4173,9605,9021,5120,5265,7121,8667,6911,4717,2535,2743,1289,1494,3788,6380,9366,2732,1501,8543,8013,5612,2393,7041,3350,3204,288,7213,1741,1238,9830,6722,4687,6758,8067,4443,5013,5374,6986,282,6762,192,340,5075,6970,7723,5913,1060,1641,1495,5738,1618,157,6891,173,7535,4952,9166,8950,8680,1974,5466,2383,3387,3392,2188,3140,6806,3131,6237,6249,7952,1114,9017,4285,7193,3191,3763,9087,7284,9170,6116,3717,6695,6538,6165,6449,8960,2897,6814,3283,6600,6151,4624,3992,5860,9557,1884,5585,2966,1061,6414,2431,9543,6654,7417,2617,878,8848,8241,3790,3370,8768,1694,9875,9882,8802,7072,3772,2689,5301,7921,7774,1614,494,2338,8638,4161,4523,5709,4305,17,9626,843,9284,3492,7755,5525,4423,9718,2237,7401,2686,8751,1585,5919,9444,3271,1490,7004,5980,3904,370,5930,6304,7737,93,5941,9079,4968,9266,262,2766,4999,2450,9518,5137,8405,483,8840,2231,700,8049,8823,9811,9378,3811,8074,153,1940,1998,4354,7830,7086,6132,9967,5680,448,1976,4101,7839,3122,4379,9296,4881,1246,4334,9457,5401,1945,9548,8290,1184,3464,132,2458,7704,1056,7554,6203,2270,6070,4889,7369,1676,485,3648,357,1912,9661,4246,1576,1836,4521,7667,6907,2098,8825,7404,4019,8284,3710,7202,7050,9870,3348,3624,9224,6601,7897,6288,3713,932,5596,353,2615,3273,833,1446,8624,2489,3872,486,1091,2493,4157,3611,6570,7107,9153,4543,9504,4746,1342,9737,3247,8984,3640,5698,7814,307,8775,1150,4330,3059,5784,2370,5248,4806,6107,9700,231,3566,5627,3957,5317,5415,8119,2588,9440,2961,9786,4769,466,5411,3080,7623,5031,2378,9286,4801,797,1527,2325,847,6341,5310,1926,9481,2115,2165,5255,5465,5561,3606,7673,7443,7243,8447,2348,7925,6447,8311,6729,4441,7763,8107,267,8135,9194,6775,3883,9639,612,5024,1351,7557,9241,5181,2239,8002,5446,747,166,325,9925,3820,9531,5163,3545,558,7103,7658,5670,8323,4821,6263,7982,59,3700,1082,4474,4353,8637,9558,5191,842,5925,6455,4092,9929,9961,290,3523,6290,7787,8266,7986,7269,6408,3620,406,5964,7289,1620,6726,1257,1993,7006,5545,2913,5093,5066,3019,7081,6760,6779,7061,9051,8852,8118,2340,6596,4594,9708,8430,8659,8920,9268,5431,9203,2823,1427,2203,6422,6193,5214,9566,8791,4964,7575,4350,56,2227,8545,5646,3089,2204,4081,487,8496,2258,4336,6955,3452,556,8602,8251,8569,8636,9430,1025,9459,7137,8392,3553,5945,9414,3078,1688,5480,327,8117,2289,2195,8564,9423,103,7724,3091,8548,7298,5279,6042,2855,3286,3542,9361,420,7020,4112,5320,5366,6379,114,9174,9744,592,5346,3985,3174,5157,9890,1605,3082,8099,4346,7256,8670,5687,6613,6620,1458,1045,7917,2980,2399,1433,3315,4084,178,7056,2132,2728,4421,9195,4181,6017,6229,2945,4627,2809,8816,6737,18,8981,3813,8890,5304,3789,6959,7476,1856,4197,6944,9578,5915,3060,9932,3463,67,7393,9857,5822,3187,501,653,8453,3691,9736,6845,1365,9645,4120,2157,8471,4436,6435,2758,7591,9805,7142,7612,4891,7342,5764,8683,8365,2967,6947,441,2116,6612,1399,7585,972,6548,5481,7733,7209,222,5903,6161,9172,9628,7348,1588,5992,6094,7176,4214,8702,2987,74,8486,9788,7164,5788,8535,8422,6826,1800,8965,4965,565,5609,4686,2556,9324,5000,9809,1994,4737,63,8992,4783,2536,4462,8868,6346,5553,3980,2670,1601,4272,8725,4698,7333,7826,9233,4198,1997,1687,4851,62,7893,8149,8015,341,2230,1280,5559,9756,3761,7834,6805,9287,4622,5748,2320,1958,9129,9649,1644,4323,5096,9490,7529,6444,7478,7044,9525,7713,234,7553,9099,9885,7135,6493,9793,6268,8363,2267,9157,9451,1438,9292,1637,3739,695,1090,4731,4549,5171,5975,7347,5192,5243,1084,2216,9860,3318,5594,5790,1107,220,9397,3378,1353,4498,6497,5442,7929,7377,9541,9871,9895,6742,9146,9409,292,6278,50,5288,2217,4923,6790,4730,9240,3006,3547,9347,7863,4275,3287,2673,7485,1915,9837,2931,3918,635,9131,1197,6250,3853,4303,790,5548,9993,3702,2446,3862,9652,4432,973,41,3507,8585,2444,1633,956,5789,1523,8657,4869,8580,8474,7093,7812,2549,7363,9315,6731,1130,7645,7018,7852,362,1636,2905,8006,4040,6643,8052,7021,3665,8383,715,1876,2783,3065,604,4566,8761,7911,1983,3836,5547,8495,8144,1950,2537,8575,640,8730,8303,1454,8165,6647,4762,909,9449,8640,9253,7293,8767,3004,4623,6862,8994,2520,1215,6299,8414,2576,6148,1510,313,3693,9843,8757,5774,8871,8061,8832,5573,5275,9452,1248,228,9749,2730};

  static void test_put_ascending()
   {final BtreeSA     t = btreeSML(4, 3);
    final Transaction T = t.new Transaction();
    final int N = 64;
    for (int i = 1; i <= N; i++) {T.Key = T.Data = i; T.put();}
    //t.stop();
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
   {final BtreeSA     t = btreeSML(8, 7);
    final Transaction T = t.new Transaction();
    final int N = 64;
    for (int i = 1; i <= N; ++i) {T.Key = T.Data = i; T.put();}
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
   {final BtreeSA     t = btreeSML(2, 3);
    final Transaction T = t.new Transaction();
    final int N = 64;
    for (int i = N; i > 0; --i) {T.Key = T.Data = i; T.put();}
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
   {final BtreeSA     t = btreeSML(6, 3);
    final Transaction T = t.new Transaction();

    for (int i = 0; i < random_small.length; ++i)
     {T.Key = T.Data = random_small[i];
      T.put();
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
    final BtreeSA t = btreeSML(2, 3);
    final Transaction T = t.new Transaction();
    final TreeMap<Integer,Integer> s = new TreeMap<>();

    for (int i = 0; i < random_large.length; ++i)
     {final int r = random_large[i];
      s.put(r, i);
      T.Key = r; T.Data = i; T.put();
     }
    final int a = s.firstKey(), b = s.lastKey();
    for (int i = a-1; i < b + 1; ++i)
     {T.Key = i;
      if (s.containsKey(i))
       {NodeTransaction f = t.nodeTypes[T.find()];
        assert f == t.nodeTypes[t.Node_find];
        ok(f.found);
        ok(f.data, s.get(i));
       }
      else
       {NodeTransaction f = t.nodeTypes[T.find()];
        ok(!f.found);
       }
     }
   }

  static void test_find()
   {final BtreeSA     t = btreeSML(8, 3);
    final Transaction T = t.new Transaction();
    final int N = 32;
    for (int i = 1; i <= N; i++) {T.Key = T.Data = 2*i; T.put();}               // Insert
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

    for (int i = 0; i <= 2*N+1; i++)                                            // Update
     {T.Key = i;
      NodeTransaction f = t.nodeTypes[T.find()];
      assert f == t.nodeTypes[t.Node_find];
      if (i > 0 && i % 2 == 0)
       {ok(f.found, true);
        ok(f.data,  i);
        T.Data = i-1;
        T.put();
       }
      else ok(f.found, false);
     }

    for (int i = 0; i <= 2*N+1; i++)
     {T.Key = i;
      NodeTransaction f = t.nodeTypes[T.find()];
      if (i > 0 && i % 2 == 0)
       {ok(f.found, true);
        ok(f.data,  i-1);
       }
      else ok(f.found, false);
     }
   }

  static void test_delete_ascending()
   {final BtreeSA     t = btreeSML(4, 3);
    final Transaction T = t.new Transaction();

    final int N = 32;
    final boolean box = false;                                                  // Print read me
    for (int i = 1; i <= N; i++) {T.Key = T.Data = i; T.put();}
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

    for (int i = 1; i <= N; i++)
     {T.Key = i; T.delete();
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
   {final BtreeSA     t = btreeSML(4, 3);
    final Transaction T = t.new Transaction();
    final int N = 32;
    final boolean box = false;                                                  // Print read me
    for (int i = 1; i <= N; i++) {T.Key = T.Data = i; T.put();}
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

    for (int i = N; i > 0; --i)
     {T.Key = i;
      T.delete();
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
  static void test_to_array()
   {final BtreeSA     t = btreeSML(2, 3);
    final Transaction T = t.new Transaction();

    final int M = 2;
    for (int i = 1; i <= M; i++)  {T.Key = T.Data = i; T.put();}
    //stop(""+t.toArray());
    ok(""+t.toArray(), """
[(0, key:1 data:1)
, (1, key:2 data:2)
]""");

    final int N = 16;
    for (int i = M; i <= N; i++)  {T.Key = T.Data = i; T.put();}
    //stop(t);
    //stop(""+t.toArray());
    ok(""+t.toArray(), """
[(0, key:1 data:1)
, (1, key:2 data:2)
, (0, key:3 data:3)
, (1, key:4 data:4)
, (0, key:5 data:5)
, (1, key:6 data:6)
, (0, key:7 data:7)
, (1, key:8 data:8)
, (0, key:9 data:9)
, (1, key:10 data:10)
, (0, key:11 data:11)
, (1, key:12 data:12)
, (0, key:13 data:13)
, (1, key:14 data:14)
, (0, key:15 data:15)
, (1, key:16 data:16)
]""");
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_put_ascending();                                                       //  7.99
    test_put_ascending_wide();                                                  //  5.33
    test_put_descending();                                                      // 12.98
    test_put_small_random();                                                    //  8.72
    //test_put_large_random();                                                    //  0
    test_find();                                                                //  4.62
    test_delete_ascending();                                                    //  7.27
    test_delete_descending();                                                   //  7.66
    test_to_array();                                                            //  2.52
   }

  static void newTests()                                                        // Tests being worked on
   {oldTests();
   }

  public static void main(String[] args)                                        // Test if called as a program
   {try                                                                         // Get a traceback in a format clickable in Geany if something goes wrong to speed up debugging.
     {if (github_actions) oldTests(); else newTests();                          // Tests to run
      //if (github_actions)                                                       // Coverage analysis
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
