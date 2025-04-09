//------------------------------------------------------------------------------
// BtreeSP with transaction in bit memory
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Btree in a block on the surface of a silicon chip.
// Need binary add so we compute the mid key
import java.util.*;

abstract class BtreeSA extends Test                                             // Manipulate a btree using static methods and memory
 {final MemoryLayout M = new MemoryLayout();                                    // The memory layout of the btree
  final MemoryLayout T = new MemoryLayout();                                    // The memory used to hold temporary variable used during a transaction on the btree
  abstract int maxSize();                                                       // The maximum number of leaves plus branches in the bree
  abstract int bitsPerKey();                                                    // The number of bits per key
  abstract int bitsPerData();                                                   // The number of bits per data
  abstract int bitsPerNext();                                                   // The number of bits in a next field
  abstract int bitsPerSize();                                                   // The number of bits in stuck size field
  abstract int maxKeysPerLeaf();                                                // Maximum number of leafs in a key
  abstract int maxKeysPerBranch();                                              // Maximum number of keys in a branch
  final   int splitLeafSize;                                                    // The number of key, data pairs to split out of a leaf
  final   int splitBranchSize;                                                  // The number of key, next pairs to split out of a branch

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

  final int Leaf_T             = 0;                                             // Process a parent node
  final int Leaf_tl            = 1;                                             // Process a left node
  final int Leaf_tr            = 2;                                             // Process a right node
  final int Leaf_length        = 3;                                             // Number of transaction types

  final StuckSA[]branchTransactions;                                            // Transactions to use on branch stucks
  final StuckSA[]  leafTransactions;                                            // Transactions to use on leaf stucks

  final StuckSA bSize;                                                          // Branch size
  final StuckSA bLeaf;                                                          // Check whether a node has leaves for children
  final StuckSA bTop;                                                           // Get the size of a stuck
  final StuckSA bFirstBranch;                                                   // Locate the first greater or equal key in a branch
  final StuckSA bT;                                                             // Process a parent node
  final StuckSA bL;                                                             // Process a left node
  final StuckSA bR;                                                             // Process a right node

  final StuckSA lSize;                                                          // Branch size
  final StuckSA lLeaf;                                                          // Check whether a node has leaves for children
  final StuckSA lEqual;                                                         // Locate an equal key
  final StuckSA lFirstLeaf;                                                     // Locate the first greater or equal key in a leaf
  final StuckSA lT;                                                             // Process a parent node
  final StuckSA lL;                                                             // Process a left node
  final StuckSA lR;                                                             // Process a right node

  static boolean debug = false;                                                 // Debugging enabled

//D1 Construction                                                               // Create a Btree from nodes which can be branches or leaves.  The data associated with the BTree is stored only in the leaves opposite the keys

  BtreeSA()                                                                     // Define a Btree with user specified dimensions
   {zz();
    splitLeafSize   = maxKeysPerLeaf()   >> 1;                                  // The number of key, data pairs to split out of a leaf
    splitBranchSize = maxKeysPerBranch() >> 1;                                  // The number of key, next pairs to split out of a branch

    M.layout(layout());
    M.layout.layoutName = "Main";
    M.memory(new Memory("BtreeSA", M.layout.size()));

    T.layout(transactionLayout());                                              // Memory and layout of memory used by a transaction against the btree
    T.layout.layoutName = "transaction";
    T.memory(new Memory("BtreeSA", T.layout.size()));

     {final int N = Branch_length;                                              // Preallocate transactions used on branch stucks
      branchTransactions = new StuckSA[N];

      for (int i = 0; i < N; i++)
       {final StuckSA b = branchTransactions[i] = new StuckSA()
         {int     maxSize() {return BtreeSA.this.maxKeysPerBranch()+1;}         // Not forgetting top next
          int  bitsPerKey() {return BtreeSA.this.bitsPerKey();}
          int bitsPerData() {return BtreeSA.this.bitsPerNext();}
          int bitsPerSize() {return BtreeSA.this.bitsPerSize();}
         };
         b.M.memory(M.memory);
         b.M.layout.layoutName = "branchMain";
         b.T.layout.layoutName = "branch";
        }
      }

     {final int N = Leaf_length;                                                // Preallocate transactions used on leaf stucks
      leafTransactions = new StuckSA[N];

      for (int i = 0; i < N; i++)
       {final StuckSA l = leafTransactions[i] = new StuckSA()
         {int     maxSize() {return BtreeSA.this.maxKeysPerLeaf();}
          int  bitsPerKey() {return BtreeSA.this.bitsPerKey();}
          int bitsPerData() {return BtreeSA.this.bitsPerData();}
          int bitsPerSize() {return BtreeSA.this.bitsPerSize();}
         };
         l.M.memory(M.memory);
         l.M.layout.layoutName = "leafMain";
         l.T.layout.layoutName = "leaf";
       }
     }

    bSize        = branchTransactions[Branch_Size       ];                      // Branch size
    bLeaf        = branchTransactions[Branch_Leaf       ];                      // Check whether a node has leaves for children
    bTop         = branchTransactions[Branch_Top        ];                      // Get the size of a stuck
    bFirstBranch = branchTransactions[Branch_FirstBranch];                      // Locate the first greater or equal key in a branch
    bT           = branchTransactions[Branch_T          ];                      // Process a parent node
    bL           = branchTransactions[Branch_tl         ];                      // Process a left node
    bR           = branchTransactions[Branch_tr         ];                      // Process a right node

    lSize        =   leafTransactions[Leaf_T            ];                      // Leaf size
    lLeaf        =   leafTransactions[Leaf_T            ];                      // Print a leaf
    lEqual       =   leafTransactions[Leaf_T            ];                      // Locate an equal key
    lFirstLeaf   =   leafTransactions[Leaf_T            ];                      // Locate the first greater or equal key in a leaf
    lT           =   leafTransactions[Leaf_T            ];                      // Process a parent node
    lL           =   leafTransactions[Leaf_tl           ];                      // Process a left node
    lR           =   leafTransactions[Leaf_tr           ];                      // Process a right node

     {final int N = maxSize();                                                  // Put all the nodes on the free chain at the start with low nodes first
      for (int i = N; i > 0; --i)                                               // Put all the nodes on the free chain at the start with low nodes first
       {final int n = i - 1;                                                    // Number of node
        T.at(node_clear).setInt(n);
        clear();
        final int  f = i == N ? 0 : i;                                          // Curent first element on free chain
        setInt(free, (i == N ? 0 : i), n);                                      // Link this node to the previous node
       }
      setInt(freeList, root);                                                   // Root is first on free chain
     }
    allocate(false);                                                            // The root is always at zero, which frees zero to act as the end of list marker on the free chain
    T.at(node_setLeaf).setInt(root);
    setLeaf();                                                                  // The root starts as a leaf
   }

  static BtreeSA btreeSA(final int leafKeys, int branchKeys)                    // Define a test btree with the specified dimensions
   {return  new BtreeSA()
     {int maxSize         () {return testMaxSize;}
      int maxKeysPerLeaf  () {return    leafKeys;}
      int maxKeysPerBranch() {return  branchKeys;}
      int bitsPerKey      () {return          32;}
      int bitsPerData     () {return          32;}
      int bitsPerNext     () {return          32;}
      int bitsPerSize     () {return          32;}
     };
   }

  Layout layout()                                                               // Layout describing memory used by btree
   {z();
    final BtreeSA btree = this;

    final StuckSA leafStuck = new StuckSA()                                     // Leaf
     {int               maxSize() {return btree.maxKeysPerLeaf();}
      int            bitsPerKey() {return btree.bitsPerKey();}
      int           bitsPerData() {return btree.bitsPerData();}
      int           bitsPerSize() {return btree.bitsPerSize();}
     };
    leafStuck.T.layout.layoutName = "leaf";

    final StuckSA branchStuck = new StuckSA()                                   // Branch
     {int               maxSize() {return btree.maxKeysPerBranch()+1;}          // Not forgetting top next
      int            bitsPerKey() {return btree.bitsPerKey();}
      int           bitsPerData() {return btree.bitsPerNext();}
      int           bitsPerSize() {return btree.bitsPerSize();}
     };
    branchStuck.T.layout.layoutName = "branch";

    final Layout l = Layout.layout();
    leaf         = l.duplicate("leaf",         leafStuck.layout());
    branch       = l.duplicate("branch",       branchStuck.layout());
    branchOrLeaf = l.union    ("branchOrLeaf", leaf,   branch);
    isLeaf       = l.bit      ("isLeaf");
    free         = l.variable ("free",         btree.bitsPerNext());
    Node         = l.structure("node",         isLeaf, free, branchOrLeaf);
    nodes        = l.array    ("nodes",        Node,         maxSize());
    freeList   = l.variable ("freeList",   btree.bitsPerNext());
    bTree        = l.structure("bTree",        freeList  , nodes);
    return l.compile();
   }

//D1 Control                                                                    // Testing, control and integrity

  private void ok(String expected) {Test.ok(toString(), expected);}             // Confirm tree is as expected
  private void stop()              {Test.stop(toString());}                     // Stop after printing the tree
  public String toString() {return print();}                                    // Print the tree

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
   {z();
    tm(allocate, freeList);                                                     // Node at head of free nodes list
    z(); if (check && tGetInt(allocate) == 0) stop("No more memory available"); // No more free nodes available
    z(); T.at(nextFree).move(M.at(free, T.at(allocate)).setOff());              // Second node on free list
    mt(freeList, nextFree);                                                     // Make second to last freed node the first freed node to liberate the existing first free node
    tt(node_clear, allocate);
    clear();                                                                    // Construct and clear the node
    maxNodeUsed  = max(maxNodeUsed, ++nodeUsed);                                // Number of nodes in use
   }

  private void allocate() {z(); allocate(true);}                                // Allocate a node checking for free space

//D1 Components                                                                 // A branch or leaf in the tree

  Layout.Variable         allocate;                                             // The latest allocation result
  Layout.Variable         nextFree;                                             // Next element of the free chain

  Layout.Bit               success;                                             // Inserted or updated if true
  Layout.Bit              inserted;                                             // Inserted if true

  Layout.Variable            first;                                             // Index of first key greater than or equal to the search key
  Layout.Variable             next;                                             // The corresponding next field or top if no such key was found
                                                                                // Find equal in leaf
  Layout.Variable           search;                                             // Search key
  Layout.Bit                 found;                                             // Whether the key was found
  Layout.Variable              key;                                             // Key to insert
  Layout.Variable             data;                                             // Data associated with the key

  Layout.Variable         firstKey;                                             // First of right leaf
  Layout.Variable          lastKey;                                             // Last of left leaf
  Layout.Variable            flKey;                                             // Key mid way between last of left and first of right
  Layout.Variable        parentKey;                                             // Parent key

  Layout.Variable               lk;                                             // Left  child key
  Layout.Variable               ld;                                             // Left  child data
  Layout.Variable               rk;                                             // Right child key
  Layout.Variable               rd;                                             // Right child data
  Layout.Variable            index;                                             // Index of a slot in a node

  Layout.Variable               nl;                                             // Number in the left child
  Layout.Variable               nr;                                             // Number in the right child

  Layout.Variable                l;                                             // Left node
  Layout.Variable                r;                                             // Right node

  Layout.Variable      splitParent;                                             // The parent during a splitting operation
  Layout.Bit             IsLeaf;                                                // On a leaf
  Layout.Bit             isFull;                                                // The node is full
  Layout.Bit              isLow;                                                // The node has too few children for a delete
  Layout.Bit        hasLeavesForChildren;                                       // The node has leaves for children
  Layout.Bit        stolenOrMerged;                                             // A merge or steal operation succeeded

  Layout.Variable        leafBase;                                              // The offset of a leaf in memory
  Layout.Variable      branchBase;                                              // The offset of a branch in memory
  Layout.Variable        leafSize;                                              // Number of children in body of leaf
  Layout.Variable      branchSize;                                              // Number of children in body of branch taking top for granted as it is always there
  Layout.Variable             top;                                              // The top next element of a branch - only used in printing
                                                                                // Find, insert, delete - the public entry points to this module
  Layout.Variable             Key;                                              // Key being found, inserted or deleted
  Layout.Variable            Data;                                              // Data found, inserted or deleted
  Layout.Variable            find;                                              // Results of a find operation
  Layout.Variable   findAndInsert;                                              // Results of a find and insert operation
  Layout.Variable          parent;                                              // Parent node in a descent through the tree
  Layout.Variable           child;                                              // Child node in a descent through the tree
  Layout.Variable       leafFound;                                              // Leaf found by find

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
  Layout.Variable  node_leafBase;
  Layout.Variable  node_branchBase;
  Layout.Variable  node_leafSize;
  Layout.Variable  node_branchSize;
  Layout.Variable  node_isFull;
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
                                    allocate = L.variable ("allocate"                                      , bitsPerNext());
                                    nextFree = L.variable ("nextFree"                                      , bitsPerNext());
                                     success = L.bit      ("success"                                       );
                                    inserted = L.bit      ("inserted"                                      );

                                       first = L.variable ("first"                                         , bitsPerSize());
                                        next = L.variable ("next"                                          , bitsPerKey());

                                      search = L.variable ("search"                                        , bitsPerKey());
                                       found = L.bit      ("found"                                         );
                                         key = L.variable (" key"                                          , bitsPerKey());
                                        data = L.variable ("data"                                          , bitsPerData());

                                    firstKey = L.variable ("firstKey"                                      , bitsPerKey());
                                     lastKey = L.variable ("lastKey"                                       , bitsPerKey());
                                       flKey = L.variable ("flKey"                                         , bitsPerKey());
                                   parentKey = L.variable ("parentKey"                                     , bitsPerKey());

                                          lk = L.variable ("lk"                                            , bitsPerKey());
                                          ld = L.variable ("ld"                                            , bitsPerData());
                                          rk = L.variable ("rk"                                            , bitsPerKey());
                                          rd = L.variable ("rd"                                            , bitsPerData());
                                       index = L.variable ("index"                                         , bitsPerSize());

                                          nl = L.variable ("nl"                                            , bitsPerSize());
                                          nr = L.variable ("nr"                                            , bitsPerSize());

                                           l = L.variable ("l"                                             , bitsPerNext());
                                           r = L.variable ("r"                                             , bitsPerNext());

                                 splitParent = L.variable ("splitParent"                                   , bitsPerNext());
                                      IsLeaf = L.bit      ("IsLeaf"                                        );
                                      isFull = L.bit      ("isFull"                                        );
                                       isLow = L.bit      ("isLow"                                         );
                        hasLeavesForChildren = L.bit      ("hasLeavesForChildren"                          );
                              stolenOrMerged = L.bit      ("stolenOrMerged"                                );

                                    leafBase = L.variable ("leafBase"                                      , bitsPerNext());
                                  branchBase = L.variable ("branchBase"                                    , bitsPerNext());
                                    leafSize = L.variable ("leafSize"                                      , bitsPerSize());
                                  branchSize = L.variable ("branchSize"                                    , bitsPerSize());
                                         top = L.variable ("top"                                           , bitsPerNext());

                                         Key = L.variable ("Key"                                           , bitsPerKey());
                                        Data = L.variable ("Data"                                          , bitsPerData());
                                        find = L.variable ("find"                                          , bitsPerNext());
                               findAndInsert = L.variable ("findAndInsert"                                 , bitsPerNext());
                                      parent = L.variable ("parent"                                        , bitsPerNext());
                                       child = L.variable ("child"                                         , bitsPerNext());
                                   leafFound = L.variable ("leafFound"                                     , bitsPerNext());

                                 node_isLeaf = L.variable ("node_isLeaf"                                   , bitsPerNext());
                                node_setLeaf = L.variable ("node_setLeaf"                                  , bitsPerNext());
                              node_setBranch = L.variable ("node_setBranch"                                , bitsPerNext());
                             node_assertLeaf = L.variable ("node_assertLeaf"                               , bitsPerNext());
                           node_assertBranch = L.variable ("node_assertBranch"                             , bitsPerNext());
                                   allocLeaf = L.variable ("allocLeaf"                                     , bitsPerNext());
                                 allocBranch = L.variable ("allocBranch"                                   , bitsPerNext());
                                   node_free = L.variable ("node_free"                                     , bitsPerNext());
                                  node_clear = L.variable ("node_clear"                                    , bitsPerNext());
                                  node_erase = L.variable ("node_erase"                                    , bitsPerNext());
                               node_leafBase = L.variable ("node_leafBase"                                 , bitsPerNext());
                             node_branchBase = L.variable ("node_branchBase"                               , bitsPerNext());
                               node_leafSize = L.variable ("node_leafSize"                                 , bitsPerNext());
                             node_branchSize = L.variable ("node_branchSize"                               , bitsPerNext());
                                 node_isFull = L.variable ("node_isFull"                                   , bitsPerNext());
                                  node_isLow = L.variable ("node_isLow"                                    , bitsPerNext());
                   node_hasLeavesForChildren = L.variable ("node_hasLeavesForChildren"                     , bitsPerNext());
                                    node_top = L.variable ("node_top"                                      , bitsPerNext());
                        node_findEqualInLeaf = L.variable ("node_findEqualInLeaf"                          , bitsPerNext());
      node_findFirstGreaterThanOrEqualInLeaf = L.variable ("node_findFirstGreaterThanOrEqualInLeaf"        , bitsPerNext());
    node_findFirstGreaterThanOrEqualInBranch = L.variable ("node_findFirstGreaterThanOrEqualInBranch"      , bitsPerNext());
                              node_splitLeaf = L.variable ("node_splitLeaf"                                , bitsPerNext());
                            node_splitBranch = L.variable ("node_splitBranch"                              , bitsPerNext());
                          node_stealFromLeft = L.variable ("node_stealFromLeft"                            , bitsPerNext());
                         node_stealFromRight = L.variable ("node_stealFromRight"                           , bitsPerNext());
                              node_mergeRoot = L.variable ("node_mergeRoot"                                , bitsPerNext());
                       node_mergeLeftSibling = L.variable ("node_mergeLeftSibling"                         , bitsPerNext());
                      node_mergeRightSibling = L.variable ("node_mergeRightSibling"                        , bitsPerNext());
                                node_balance = L.variable ("node_balance"                                  , bitsPerNext());

    final Layout.Structure transaction = L.structure("transaction", allocate,
      nextFree, success, inserted, first, next, search, found, key, data,
      firstKey, lastKey, flKey, parentKey, lk, ld, rk, rd, index, nl, nr,
      l, r, splitParent, IsLeaf, isFull, isLow, hasLeavesForChildren,
      stolenOrMerged, leafBase, branchBase, leafSize, branchSize,
      top, Key, Data, find, findAndInsert, parent, child, leafFound,
      node_isLeaf, node_setLeaf, node_setBranch, node_assertLeaf,
      node_assertBranch, allocLeaf, allocBranch, node_free,
      node_clear, node_erase, node_leafBase, node_branchBase,
      node_leafSize, node_branchSize, node_isFull, node_isLow,
      node_hasLeavesForChildren, node_top, node_findEqualInLeaf,
      node_findFirstGreaterThanOrEqualInLeaf,
      node_findFirstGreaterThanOrEqualInBranch, node_splitLeaf,
      node_splitBranch, node_stealFromLeft, node_stealFromRight,
      node_mergeRoot, node_mergeLeftSibling, node_mergeRightSibling,
      node_balance);

    return L.compile();
   }

  private void    isLeaf() {z(); T.at(IsLeaf).move(M.at(isLeaf, T.at(node_isLeaf)).setOff());}      // A leaf if true
  private void   setLeaf() {z(); M.at(isLeaf, T.at(node_setLeaf))  .setOff().setInt(1);} // Set as leaf
  private void setBranch() {z(); M.at(isLeaf, T.at(node_setBranch)).setOff().setInt(0);} // Set as branch

  private void assertLeaf()
   {z();
    tt(node_isLeaf, node_assertLeaf);
    isLeaf();
    if (tGetInt(IsLeaf) == 0) stop("Leaf required");
   }
  private void assertBranch()
   {z();
    tt(node_isLeaf, node_assertBranch);
    isLeaf();
    if (tGetInt(IsLeaf) != 0) stop("Branch required");
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
   {z(); if (tGetInt(node_free) == 0) stop("Cannot free root");                 // The root is never freed
    z(); tt(node_erase, node_free); erase();                                    // Clear the node to encourage erroneous frees to do damage that shows up quickly.
    M.at(free, T.at(node_free)).setOff().move(M.at(freeList));                  // Chain this node in front of the last freed node
    M.at(freeList).move(T.at(node_free));                                       // Make this node the head of the free chain
    maxNodeUsed = max(maxNodeUsed, --nodeUsed);                                 // Number of nodes in use
   }

  private void clear()                                                          // Clear a new node to zeros ready for use
   {z();
    M.at(Node, T.at(node_clear)).setOff().zero();
   }

  private void erase()                                                          // Clear a new node to ones as this is likely to create invalid values that will be easily detected in the case of erroneous frees
   {z();
    M.at(Node, T.at(node_erase)).setOff().ones();
   }

  private void leafBase()   {z(); T.at(leafBase  ).setInt(M.at(leaf,   T.at(node_leafBase  )).setOff().at);} // Base of leaf stuck in memory
  private void branchBase() {z(); T.at(branchBase).setInt(M.at(branch, T.at(node_branchBase)).setOff().at);} // Base of branch stuck in memory

  private void leafSize()                                                       // Number of children in body of leaf
   {z();
    tt(node_leafBase, node_leafSize); leafBase();
    lSize.base(T.at(leafBase));
    lSize.size();
    T.at(leafSize).move(lSize.T.at(lSize.size));
   }

  private void branchSize()                                                     // Number of children in body of branch taking top for granted as it is always there
   {z();
    tt(node_branchBase, node_branchSize); branchBase();
    bSize.base(T.at(branchBase));
    bSize.size();
    bSize.T.at(bSize.size).dec();                                               // Account for top
    T.at(branchSize).move(bSize.T.at(bSize.size));
   }

  private void isFull()                                                         // The node is full
   {z();
    tt(node_isLeaf, node_isFull);
    isLeaf();
    if (T.at(IsLeaf).isOnes())
     {z();
      tt(node_leafSize, node_isFull);
      leafSize();
      T.at(leafSize).equal(T.constant(maxKeysPerLeaf()), T.at(isFull));
     }
    else
     {z();
      tt(node_branchSize, node_isFull);
      branchSize();
      T.at(branchSize).equal(T.constant(maxKeysPerBranch()), T.at(isFull));
     }
   }

  private void isLow()                                                          // The node is low on children making it impossible to merge two sibling children
   {z(); tt(node_isLeaf, node_isLow); isLeaf();
    if (T.at(IsLeaf).isOnes())
     {tt(node_leafSize, node_isLow);
      leafSize();
      T.at(leafSize).lessThan(T.constant(2), T.at(isLow));
     }
    else
     {tt(node_branchSize, node_isLow);
      branchSize();
      T.at(branchSize).lessThan(T.constant(2), T.at(isLow));
     }
   }

  private void hasLeavesForChildren()                                           // The node has leaves for children
   {z(); tt(node_assertBranch , node_hasLeavesForChildren);
    assertBranch();
    tt(node_branchBase, node_hasLeavesForChildren); branchBase();
    bLeaf.base(T.at(branchBase));
    bLeaf.lastElement();
    T.at(node_isLeaf).move(bLeaf.T.at(bLeaf.tData)); isLeaf();
    tt(hasLeavesForChildren, IsLeaf);
   }

  private void top()                                                            // The top next element of a branch - only used in printing
   {z(); tt(node_assertBranch, node_top); assertBranch();
    tt(node_branchBase, node_top); branchBase();
    bTop.base(T.at(branchBase));
    tt(node_branchSize, node_top); branchSize();
    bTop.T.at(bTop.index).move(T.at(branchSize));
    bTop.elementAt();
    T.at(top).move(bTop.T.at(bTop.tData));
   }

  public String toString(int node)                                              // Print a node
   {final StringBuilder s = new StringBuilder();
    T.at(node_isLeaf).setInt(node); isLeaf();
    if (T.at(IsLeaf).isOnes())                                                  // Print a leaf
     {T.at(node_leafSize).setInt(node);
      leafSize();
      final int N = T.at(leafSize).getInt();                                    // Number of elements in leaf
      s.append("Leaf(node:"+node+" size:"+N+")\n");
      T.at(node_leafBase).setInt(node); leafBase();
      lLeaf.base(T.at(leafBase));
      for (int i = 0; i < N; i++)                                               // Each element in the leaf
       {lLeaf.T.at(lLeaf.index).setInt(i);
        lLeaf.elementAt();
        s.append("  "+(i+1)+" key:"+lLeaf.tKey+" data:"+lLeaf.tData+"\n");
       }
     }
    else                                                                        // Print a branch
     {T.at(node_branchSize).setInt(node); branchSize();
      final int N = T.at(branchSize).getInt();                                  // Number of elements in branch not including top
      T.at(node_top).setInt(node); top();
      s.append("Branch(node:"+node+" size:"+N+" top:"+top+"\n");

      T.at(node_branchBase).setInt(node); branchBase();
      bLeaf.base(T.at(branchBase));
      for (int i = 0; i < N; i++)
       {bLeaf.T.at(bLeaf.index).setInt(i); bLeaf.elementAt();
        s.append(String.format("  %2d key:%2d next:%2d\n", i+1,
        bLeaf.T.at(bLeaf.tKey) .getInt(),
        bLeaf.T.at(bLeaf.tData).getInt()));
       }
      bLeaf.T.at(bLeaf.index).setInt(N); bLeaf.elementAt();
      s.append("             Top:"+bLeaf.T.at(bLeaf.tData).getInt()+")\n");
     }

    return s.toString();
   }

//D2 Search                                                                     // Search within a node and update the node description with the results

  private void findEqualInLeaf()                                                // Find the first key in the leaf that is equal to the search key
   {z(); tt(node_assertLeaf, node_findEqualInLeaf); assertLeaf();
    tt(node_leafBase, node_findEqualInLeaf); leafBase();
    lEqual.base(T.at(leafBase));

    lEqual.T.at(lEqual.search).move(T.at(search));
    lEqual.search();
    T.at(found).move(lEqual.T.at(lEqual.found));
    T.at(index).move(lEqual.T.at(lEqual.index));
    T.at(data ).move(lEqual.T.at(lEqual.tData));
   }

  public String findEqualInLeaf_toString()                                      // Print details of find equal in leaf node
   {final StringBuilder s = new StringBuilder();
    s.append("FindEqualInLeaf(");
    s.append(  "Leaf:"+T.at(node_findEqualInLeaf).getInt());
    s.append(  " Key:"+T.at(search)              .getInt());
    s.append(" found:"+T.at(found)               .getInt());
    if (T.at(found).isOnes()) s.append(" data:"+T.at(data).getInt()+" index:"+T.at(index).getInt());
    s.append(")\n");
    return s.toString();
   }

  private void findFirstGreaterThanOrEqualInLeaf()                              // Find the first key in the  leaf that is equal to or greater than the search key
   {z(); tt(node_assertLeaf, node_findFirstGreaterThanOrEqualInLeaf); assertLeaf();
    tt(node_leafBase, node_findFirstGreaterThanOrEqualInLeaf); leafBase();
    lFirstLeaf.base(T.at(leafBase));
    lFirstLeaf.T.at(lFirstLeaf.search).move(T.at(search));
    lFirstLeaf.searchFirstGreaterThanOrEqual();
    T.at(found).move(lFirstLeaf.T.at(lFirstLeaf.found));
    T.at(first).move(lFirstLeaf.T.at(lFirstLeaf.index));
   }

  private void findFirstGreaterThanOrEqualInBranch()                            // Find the first key in the branch that is equal to or greater than the search key
   {z();
    tt(node_assertBranch, node_findFirstGreaterThanOrEqualInBranch); assertBranch();
    tt(node_branchBase,   node_findFirstGreaterThanOrEqualInBranch); branchBase();
    bFirstBranch.base(T.at(branchBase));
    bFirstBranch.T.at(bFirstBranch.search).move(T.at(search));
    bFirstBranch.T.at(bFirstBranch.limit).setInt(1);
    bFirstBranch.searchFirstGreaterThanOrEqual();
    T.at(found).move(bFirstBranch.T.at(bFirstBranch.found));
    T.at(first).move(bFirstBranch.T.at(bFirstBranch.index));
    if (T.at(found).isOnes())                                                   // Next if key matches else top
     {z();
      T.at(next).move(bFirstBranch.T.at(bFirstBranch.tData));
     }
    else
     {z();
      bFirstBranch.lastElement();
      T.at(next).move(bFirstBranch.T.at(bFirstBranch.tData));
     }
   }

//D2 Array                                                                      // Represent the contents of the tree as an array

  private void leafToArray(int node, Stack<ArrayElement> s)                     // Leaf as an array
   {z(); T.at(node_assertLeaf).setInt(node); assertLeaf();
    T.at(node_leafSize).setInt(node);
    leafSize();
    final int     K = T.at(leafSize).getInt();
    final StuckSA t = lLeaf.copy();
    T.at(node_leafBase).setInt(node); leafBase();
    t.base(T.at(leafBase));
    for  (int i = 0; i < K; i++)
     {z();
      t.T.at(t.index).setInt(i); t.elementAt();
      s.push(new ArrayElement(i, t.T.at(t.tKey).getInt(), t.T.at(t.tData).getInt()));
     }
   }

  private void branchToArray(int node, Stack<ArrayElement> s)                   // Branch to array
   {z();
    T.at(node_assertBranch).setInt(node); assertBranch();
    T.at(node_branchSize  ).setInt(node); branchSize();
    final int K = T.at(branchSize).getInt()+1;                                  // Include top next

    if (K > 0)                                                                  // Branch has key, next pairs
     {z();
      final StuckSA t = bLeaf.copy();
      T.at(node_branchBase).setInt(node); branchBase();
      t.base(T.at(branchBase));
      for  (int i = 0; i < K; i++)
       {z();
        t.T.at(t.index).setInt(i); t.elementAt();                               // Each node in the branch

        T.at(node_isLeaf).move(t.T.at(t.tData)); isLeaf();
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

  private void printLeaf(int node, Stack<StringBuilder>S, int level)            // Print leaf horizontally
   {T.at(node_assertLeaf).setInt(node);
    assertLeaf();
    padStrings(S, level);
    final StringBuilder s = new StringBuilder();                                // String builder
    T.at(node_leafSize).setInt(node);
    leafSize();
    final int     K = T.at(leafSize).getInt();

    final StuckSA t = lLeaf.copy();
    T.at(node_leafBase).setInt(node); leafBase();
    t.base(T.at(leafBase));

    for  (int i = 0; i < K; i++)
     {t.T.at(t.index).setInt(i); t.elementAt();                                 // Each node in the leaf
      s.append(""+t.T.at(t.tKey).getInt()+",");
     }
    if (s.length() > 0) s.setLength(s.length()-1);                              // Remove trailing comma if present
    s.append("="+node+" ");
    S.elementAt(level*linesToPrintABranch).append(s.toString());
    padStrings(S, level);
   }

  private void printBranch(int node, Stack<StringBuilder>S, int level)          // Print branch horizontally
   {T.at(node_assertBranch).setInt(node); assertBranch();
    if (level > maxPrintLevels) return;
    padStrings(S, level);
    final int L = level * linesToPrintABranch;
    T.at(node_branchSize).setInt(node); branchSize();
    final int K = T.at(branchSize).getInt();

    if (K > 0)                                                                  // Branch has key, next pairs
     {final StuckSA t = bLeaf.copy();
      T.at(node_branchBase).setInt(node); branchBase();
      t.base(T.at(branchBase));
      for  (int i = 0; i < K; i++)
       {t.T.at(t.index).setInt(i); t.elementAt();                               // Each node in the branch
        T.at(node_isLeaf).move(t.T.at(t.tData)); isLeaf();
        if (T.at(IsLeaf).isOnes())
         {printLeaf(t.T.at(t.tData).getInt(), S, level+1);
         }
        else
         {if (t.T.at(t.tData).isZero())
           {say("Cannot descend through root from index", i,
                "in branch", node);
            break;
           }
          printBranch(t.T.at(t.tData).getInt(), S, level+1);
         }

        S.elementAt(L+0).append(""+t.T.at(t.tKey ).getInt());                   // Key
        S.elementAt(L+1).append(""+node+(i > 0 ?  "."+i : ""));                 // Index in node
        S.elementAt(L+2).append(""+t.T.at(t.tData).getInt());                   // Next
       }
     }
    else                                                                        // Branch is empty so print just the index of the branch
     {S.elementAt(L+0).append(""+node+"Empty");
     }

    T.at(node_top).setInt(node); top();                                         // Top next will always be present
    S.elementAt(L+3).append(T.at(top).getInt());                                // Append top next

    T.at(node_isLeaf).move(T.at(top)); isLeaf();                                // Print leaf
    if (T.at(IsLeaf).isOnes())                                                  // Print leaf
     {printLeaf(T.at(top).getInt(), S, level+1);
     }
    else                                                                        // Print branch
     {if (T.at(top).isZero())
       {say("Cannot descend through root from top in branch:", node);
        return;
       }
      printBranch(T.at(top).getInt(), S, level+1);
     }

    padStrings(S, level);
   }

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
   {z(); T.at(node_assertLeaf).setInt(root); assertLeaf();
    z(); T.at(node_isFull).setInt(root); isFull();
    if (T.at(isFull).isZero())
     {T.at(node_leafSize).setInt(root);
      leafSize();
      stop("Root is not full, but has size:", T.at(leafSize).getInt());
     }

    allocLeaf(); tt(l, allocLeaf);                                              // New left leaf
    allocLeaf(); tt(r, allocLeaf);                                              // New right leaf

    T.at(node_leafBase).zero(); leafBase(); lT.base(T.at(leafBase));            // Set address of the referenced leaf stuck
    tt  (node_leafBase, l);     leafBase(); lL.base(T.at(leafBase));            // Set address of the referenced leaf stuck
    tt  (node_leafBase, r);     leafBase(); lR.base(T.at(leafBase));            // Set address of the referenced leaf stuck

    for (int i = 0; i < splitLeafSize; i++)                                     // Build left leaf from parent
     {z(); lT.shift();
      lL.T.at(lL.tKey ).move(lT.T.at(lT.tKey ));
      lL.T.at(lL.tData).move(lT.T.at(lT.tData));
      lL.push();
     }
    for (int i = 0; i < splitLeafSize; i++)                                     // Build right leaf from parent
     {z(); lT.shift();
      lR.T.at(lR.tKey ).move(lT.T.at(lT.tKey));
      lR.T.at(lR.tData).move(lT.T.at(lT.tData));
      lR.push();
     }

    lR.firstElement();
    lL. lastElement();
    T.at(node_setBranch).setInt(root); setBranch();                             // The root is now a branch
    T.at(node_branchBase).setInt(root); branchBase();                           // Set address of the referenced leaf stuck
    bT.base(T.at(branchBase));                                                  // Set address of the referenced leaf stuck
    bT.clear();                                                                 // Clear the branch
    T.at(firstKey).move(lR.T.at(lR.tKey));                                      // First of right leaf
    T.at(lastKey ).move(lL.T.at(lL.tKey));                                      // Last of left leaf
    T.at(flKey   ).setInt((T.at(firstKey).getInt() +
                           T.at(lastKey).getInt()) / 2);                        // Mid key - keys are likely to be bigger than 31 bits
    bT.T.at(bT.tKey).move(T.at(flKey));
    bT.T.at(bT.tData).move(T.at(l));
    bT.push();                                                                  // Insert left leaf into root
    bT.T.at(bT.tKey).zero();
    bT.T.at(bT.tData).move(T.at(r));
    bT.push();                                                                  // Insert right into root. This will be the top node and so ignored by search ... except last.
   }

  private void splitBranchRoot()                                                // Split a branch which happens to be a full root into two half full branches while retaining the current branch as the root
   {z(); T.at(node_assertBranch).setInt(root); assertBranch();
    z(); T.at(node_isFull).setInt(root); isFull();
    if (T.at(isFull).isZero())
     {T.at(node_branchSize).setInt(root); branchSize();
      stop("Root is not full, but has size:", branchSize);
     }
    z();

    allocBranch(); tt(l, allocBranch);                                          // New left branch
    allocBranch(); tt(r, allocBranch);                                          // New right branch

    T.at(node_branchBase).setInt(root); branchBase(); bT.base(T.at(branchBase));// Set address of the referenced branch stuck
    tt(node_branchBase, l);             branchBase(); bL.base(T.at(branchBase));// Set address of the referenced branch stuck
    tt(node_branchBase, r);             branchBase(); bR.base(T.at(branchBase));// Set address of the referenced branch stuck

    for (int i = 0; i < splitBranchSize; i++)                                   // Build left child from parent
     {z(); bT.shift();
      bL.T.at(bL.tKey ).move(bT.T.at(bT.tKey ));
      bL.T.at(bL.tData).move(bT.T.at(bT.tData));
      bL.push();
     }
    bT.shift();                                                                 // This key, next pair will be part of the root
    T.at(parentKey).move(bT.T.at(bT.tKey));
    bL.T.at(bL.tKey).zero();
    bL.T.at(bL.tData).move(bT.T.at(bT.tData));
    bL.push();                                                                  // Becomes top and so ignored by search ... except last

    for(int i = 0; i < splitBranchSize; i++)                                    // Build right child from parent
     {z(); bT.shift();
      bR.T.at(bR.tKey ).move(bT.T.at(bT.tKey ));
      bR.T.at(bR.tData).move(bT.T.at(bT.tData));
      bR.push();
     }

    bT.shift();
    bR.T.at(bR.tKey).zero();
    bR.T.at(bR.tData).move(bT.T.at(bT.tData));
    bR.push();                                                                  // Becomes top and so ignored by search ... except last

    bT.clear();                                                                 // Refer to new branches from root
    bT.T.at(bT.tKey) .move(T.at(parentKey));
    bT.T.at(bT.tData).move(T.at(l));
    bT.push();
    bT.T.at(bT.tKey ).zero();
    bT.T.at(bT.tData).move(T.at(r));
    bT.push();                                                                  // Becomes top and so ignored by search ... except last
   }

  private void splitLeaf()                                                      // Split a leaf which is not the root
   {z(); tt(node_assertLeaf, node_splitLeaf); assertLeaf();
    if (T.at(node_splitLeaf).isZero())
     {stop("Cannot split root with this method");
     }
    tt(node_leafSize, node_splitLeaf);
    leafSize();
    final int S = T.at(leafSize).getInt(), I = T.at(index).getInt();
    tt(node_isFull, node_splitLeaf); isFull();
    if (T.at(isFull).isZero())
     {stop("Leaf:", node_splitLeaf, "is not full, but has:", S, this);
     }
    tt(node_isFull, splitParent); isFull();
    if (T.at(isFull).isOnes())
     {stop("Leaf split parent:", splitParent, "must not be full");
     }
    if (I < 0) stop("Index", I, "too small");
    if (I > S) stop("Index", I, "too big for leaf with:", S);
    z();
    allocLeaf(); tt(l, allocLeaf);                                              // New  split out leaf

    tt(node_leafBase, l);               leafBase(); lL.base(T.at(leafBase));    // The leaf being split into
    tt(node_leafBase, node_splitLeaf);  leafBase(); lR.base(T.at(leafBase));    // The leaf being split on the right

    for (int i = 0; i < splitLeafSize; i++)                                     // Build left leaf
     {z(); lR.shift();
      lL.T.at(lL.tKey ).move(lR.T.at(lR.tKey ));
      lL.T.at(lL.tData).move(lR.T.at(lR.tData));
      lL.push();
     }
    lR.firstElement();
    lL. lastElement();
    tt(node_branchBase, splitParent); branchBase();                             // The parent branch
    bT.base(T.at(branchBase));                                                  // The parent branch
    bT.T.at(bT.tKey).setInt((lR.T.at(lR.tKey).getInt() +
                             lL.T.at(lL.tKey).getInt()) / 2);                   // Splitting key
    bT.T.at(bT.tData).move(T.at(l));
    bT.T.at(bT.index).move(T.at(index));
    bT.insertElementAt();                                                       // Insert new key, next pair in parent
   }

  private void splitBranch()                                                    // Split a branch which is not the root by splitting right to left
   {z(); tt(node_assertBranch, node_splitBranch); assertBranch();
    tt(node_branchSize, node_splitBranch); branchSize();
    final int bs = T.at(branchSize      ).getInt(),
               I = T.at(index           ).getInt(),
              pn = T.at(splitParent     ).getInt(),
              nd = T.at(node_splitBranch).getInt();
    if (nd == 0) stop("Cannot split root with this method");

    tt(node_isFull, node_splitBranch); isFull();
    if (T.at(isFull).isZero())     stop("Branch:", nd, "is not full, but", bs);

    tt(node_isFull, splitParent); isFull();
    if (T.at(isFull).isOnes())
     {stop("Branch split parent:", pn, "must not be full");
     }
    if (I < 0)   stop("Index", I, "too small in node:", nd);
    if (I > bs)  stop("Index", I, "too big for branch with:",
                            bs, "in node:", nd);
    z();
    allocBranch(); tt(l, allocBranch);
    tt(node_branchBase, splitParent);      branchBase(); bT.base(T.at(branchBase)); // The parent branch
    tt(node_branchBase, l);                branchBase(); bL.base(T.at(branchBase)); // The branch being split into
    tt(node_branchBase, node_splitBranch); branchBase(); bR.base(T.at(branchBase)); // The branch being split

    for (int i = 0; i < splitBranchSize; i++)                                   // Build left branch from right
     {z(); bR.shift();
      bL.T.at(bL.tKey ).move(bR.T.at(bR.tKey ));
      bL.T.at(bL.tData).move(bR.T.at(bR.tData));
      bL.push();
     }
    bR.shift();
    bL.T.at(bL.tKey ).zero();
    bL.T.at(bL.tData).move(bR.T.at(bR.tData));
    bL.push();                                                                  // Build right branch - becomes top and so is ignored by search ... except last
    bT.T.at(bT.tKey ).move(bR.T.at(bR.tKey));
    bT.T.at(bT.tData).move(T.at(l));
    bT.T.at(bT.index).move(T.at(index));
    bT.insertElementAt();
   }

  private void stealFromLeft()                                                  // Steal from the left sibling of the indicated child if possible to give to the right - Dennis Moore, Dennis Moore, Dennis Moore.
   {z(); tt(node_assertBranch, node_stealFromLeft); assertBranch();
    if (T.at(index).isZero())
     {z(); T.at(stolenOrMerged).zero();
      return;
     }
    tt(node_branchSize, node_stealFromLeft); branchSize();
    if (T.at(index).greaterThan(T.at(branchSize)))
     {stop("Index", index, "too big");
     }
    z();

    tt(node_branchBase, node_stealFromLeft);
    branchBase();
    bT.base(T.at(branchBase));

    bT.T.at(bT.index).move(T.at(index));
    bT.T.at(bT.index).dec();
    bT.elementAt();

    T.at(l).move(bT.T.at(bT.tData));
    bT.T.at(bT.index).move(T.at(index));
    bT.elementAt();
    T.at(r).move(bT.T.at(bT.tData));

    tt(node_hasLeavesForChildren, node_stealFromLeft);
    hasLeavesForChildren();

    if (T.at(hasLeavesForChildren).isOnes())                                    // Children are leaves
     {z();
      tt(node_leafBase, l); leafBase(); lL.base(T.at(leafBase));
      tt(node_leafBase, r); leafBase(); lR.base(T.at(leafBase));

      tt(node_leafSize, l); leafSize(); tt(nl, leafSize);
      tt(node_leafSize, r); leafSize(); tt(nr, leafSize);

      if (T.at(nr).greaterThanOrEqual(T.constant(maxKeysPerLeaf())))            // Steal not possible because there is no where to put the steal
       {z(); T.at(stolenOrMerged).zero();
        return;
       }
      if (T.at(nl).lessThanOrEqual   (T.constant(0)))                           // Steal not allowed because it would leave the leaf sibling empty
       {z(); T.at(stolenOrMerged).zero();
        return;
       }
      z();

      lL.lastElement();

      lR.T.at(lR.tKey ).move(lL.T.at(lL.tKey ));
      lR.T.at(lR.tData).move(lL.T.at(lL.tData));
      lR.unshift();                                                             // Increase right

      lL.pop();                                                                 // Reduce left

      lL.T.at(lL.index).move(T.at(nl));
      lL.T.at(lL.index).dec();
      lL.T.at(lL.index).dec();
      lL.elementAt();                                                           // Last key on left

      bT.T.at(bT.tKey) .move(lL.T.at(lL.tKey));
      bT.T.at(bT.tData).move(T.at(l));
      bT.T.at(bT.index).move(T.at(index));
      bT.T.at(bT.index).dec();
      bT.setElementAt();                                                        // Reduce key of parent of left
     }
    else                                                                        // Children are branches
     {z();
      tt(node_branchBase, l); branchBase(); bL.base(T.at(branchBase));
      tt(node_branchBase, r); branchBase(); bR.base(T.at(branchBase));
      tt(node_branchSize, l); branchSize(); tt(nl, branchSize);
      tt(node_branchSize, r); branchSize(); tt(nr, branchSize);

      if (T.at(nr).greaterThanOrEqual(T.constant(maxKeysPerBranch())))          // Steal not possible because there is no where to put the steal
       {z(); T.at(stolenOrMerged).zero();
        return;
       }
      if (T.at(nl).lessThanOrEqual   (T.constant(1)))                           // Steal not allowed because it would leave the left sibling empty
       {z(); T.at(stolenOrMerged).zero();
        return;
       }
      z();

      bL.lastElement();                                                         // Increase right with left top
      bT.T.at(bT.index).move(T.at(index));
      bT.elementAt();                                                           // Top key
      bR.T.at(bR.tKey) .move(bT.T.at(bT.tKey));
      bR.T.at(bR.tData).move(bL.T.at(bL.tData));
      bR.unshift();                                                             // Increase right with left top
      bL.pop();                                                                 // Remove left top

      bR.firstElement();                                                        // Increase right with left top

      bT.T.at(bT.index).move(T.at(index));
      bT.T.at(bT.index).dec();

      bT.elementAt();                                                           // Parent key
      bR.T.at(bR.tKey).move(bT.T.at(bT.tKey));
      bR.T.at(bR.index).zero();
      bR.setElementAt();                                                        // Reduce key of parent of right

      bL.lastElement();                                                         // Last left key
      bT.T.at(bT.tKey) .move(bL.T.at(bL.tKey));
      bT.T.at(bT.tData).move(T.at(l));
      bT.T.at(bT.index).move(T.at(index));
      bT.T.at(bT.index).dec();
      bT.setElementAt();                                                        // Reduce key of parent of left
     }
    z(); T.at(stolenOrMerged).ones();
    return;
   }

  private void stealFromRight()                                                 // Steal from the right sibling of the indicated child if possible
   {z(); tt(node_assertBranch, node_stealFromRight); assertBranch();
    z(); tt(node_branchSize, node_stealFromRight);   branchSize();
    if (T.at(index).equal(T.at(branchSize)))
     {z(); T.at(stolenOrMerged).zero();
      return;
     }
    if (T.at(index).greaterThanOrEqual(T.at(branchSize)))
     {stop("Index", T.at(index).getInt(), "too big");
     }
    z();

    tt(node_branchBase, node_stealFromRight);
    branchBase();

    bT.base(T.at(branchBase));
    bT.T.at(bT.index).move(T.at(index));
    bT.elementAt();

    T.at(lk).move(bT.T.at(bT.tKey));
    T.at(l) .move(bT.T.at(bT.tData));
    bT.T.at(bT.index).move(T.at(index));
    bT.T.at(bT.index).inc();
    bT.elementAt();

    T.at(rk).move(bT.T.at(bT.tKey));
    T.at(r) .move(bT.T.at(bT.tData));

    tt(node_hasLeavesForChildren, node_stealFromRight);
    hasLeavesForChildren();
    if (T.at(hasLeavesForChildren).isOnes())                                    // Children are leaves
     {z();
      tt(node_leafBase, l); leafBase(); lL.base(T.at(leafBase));
      tt(node_leafBase, r); leafBase(); lR.base(T.at(leafBase));
      tt(node_leafSize, l);
      leafSize();
      tt(nl, leafSize);
      tt(node_leafSize, r);
      leafSize();
      tt(nr, leafSize);

      if (T.at(nl).greaterThanOrEqual(T.constant(maxKeysPerLeaf())))            // Steal not possible because there is no where to put the steal
       {z(); T.at(stolenOrMerged).zero();
        return;
       }
      if (T.at(nr).lessThanOrEqual(T.constant(1)))                              // Steal not allowed because it would leave the right sibling empty
       {z(); T.at(stolenOrMerged).zero();
        return;
       }
      z();
      lR.firstElement();                                                        // First element of right child
      lL.T.at(lL.tKey) .move(lR.T.at(lR.tKey));
      lL.T.at(lL.tData).move(lR.T.at(lR.tData));
      lL.push();                                                                // Increase left

      bT.T.at(bT.tKey) .move(lR.T.at(lR.tKey));
      bT.T.at(bT.tData).move(T.at(l));
      bT.T.at(bT.index).move(T.at(index));
      bT.setElementAt();                                                        // Swap key of parent
      lR.shift();                                                               // Reduce right
     }
    else                                                                        // Children are branches
     {z();
      tt(node_branchBase, l); branchBase(); bL.base(T.at(branchBase));
      tt(node_branchBase, r); branchBase(); bR.base(T.at(branchBase));
      tt(node_branchSize, l); branchSize(); tt(nl, branchSize);
      tt(node_branchSize, r); branchSize(); tt(nr, branchSize);

      if (T.at(nl).greaterThanOrEqual(T.constant(maxKeysPerBranch())))          // Steal not possible because there is no where to put the steal
       {z(); T.at(stolenOrMerged).zero();
        return;
       }
      if (T.at(nr).lessThanOrEqual(T.constant(1)))                              // Steal not allowed because it would leave the right sibling empty
       {z(); T.at(stolenOrMerged).zero();
        return;
       }
      z();

      bL.lastElement();                                                         // Last element of left child
      bL.T.at(bL.tKey).move(T.at(lk));
      bL.T.at(bL.index).move(T.at(nl));
      bL.setElementAt();                                                        // Left top becomes real

      bR.firstElement();                                                        // First element of  right child

      bL.T.at(bL.tKey).zero();
      bL.T.at(bL.tData).move(bR.T.at(bR.tData));
      bL.push();                                                                // New top for left is ignored by search ,.. except last

      bT.T.at(bT.tKey).move(bR.T.at(bR.tKey));
      bT.T.at(bT.tData).move(T.at(l));
      bT.T.at(bT.index).move(T.at(index));
      bT.setElementAt();                                                        // Swap key of parent
      bR.shift();                                                               // Reduce right
     }
    z(); T.at(stolenOrMerged).ones();
    return;
   }

//D2 Merge                                                                      // Merge two nodes together and free the resulting free node

  private void mergeRoot()                                                      // Merge into the root
   {z();
    T.at(node_isLeaf).zero(); isLeaf();
    if (T.at(IsLeaf).isOnes())                                                  // Confirm we are on a branch
     {z(); T.at(stolenOrMerged).zero();
      return;
     }
    T.at(node_branchSize).zero(); branchSize();
    if (T.at(branchSize).greaterThan(T.constant(1)))                            // Confirm we are on an almost empty root
     {z(); T.at(stolenOrMerged).zero();
      return;
     }
    z();
    T.at(node_branchBase).zero(); branchBase();
    bT.base(T.at(branchBase));
    bT.firstElement();
    T.at(l).move(bT.T.at(bT.tData));
    bT. lastElement();
    T.at(r).move(bT.T.at(bT.tData));

    T.at(node_hasLeavesForChildren).setInt(root);
    hasLeavesForChildren();
    if (T.at(hasLeavesForChildren).isOnes())                                    // Leaves
     {z();
      tt(node_leafSize, l);
      leafSize();
      tt(nl, leafSize);
      tt(node_leafSize, r);
      leafSize();
      tt(nr, leafSize);
      if (T.at(nl).getInt() + T.at(nr).getInt() <= maxKeysPerLeaf())
       {z(); bT.clear();
        tt(node_leafBase, l); leafBase(); lL.base(T.at(leafBase));
        tt(node_leafBase, r); leafBase(); lR.base(T.at(leafBase));
        for (int i = 0; i < T.at(nl).getInt(); ++i)                             // Merge in left child leaf
         {z();
          lL.shift();
          bT.T.at(bT.tKey ).move(lL.T.at(lL.tKey));
          bT.T.at(bT.tData).move(lL.T.at(lL.tData));
          bT.push();
         }
        for (int i = 0; i < T.at(nr).getInt(); ++i)                             // Merge in right child leaf
         {z();
          lR.shift();
          bT.T.at(bT.tKey ).move(lR.T.at(lR.tKey));
          bT.T.at(bT.tData).move(lR.T.at(lR.tData));
          bT.push();
         }
        T.at(node_setLeaf).setInt(root);                                        // The root is now a leaf
        setLeaf();
        tt(node_free, l); free();                                               // Free the children
        tt(node_free, r); free();
        z(); T.at(stolenOrMerged).ones(); return;
       }
      z(); T.at(stolenOrMerged).zero(); return;
     }
    else                                                                        // Branches
     {tt(node_branchSize, l); branchSize(); tt(nl, branchSize);
      tt(node_branchSize, r); branchSize(); tt(nr, branchSize);

      if (T.at(nl).getInt() + 1 + T.at(nr).getInt() <= maxKeysPerBranch())
       {z();
        tt(node_branchBase, l); branchBase(); bL.base(T.at(branchBase));
        tt(node_branchBase, r); branchBase(); bR.base(T.at(branchBase));
        bT.firstElement();
        T.at(parentKey).move(bT.T.at(bT.tKey));
        bT.clear();
        for (int i = 0; i < T.at(nl).getInt(); ++i)                             // Merge left child branch
         {z();
          bL.shift();
          bT.T.at(bT.tKey) .move(bL.T.at(bL.tKey));
          bT.T.at(bT.tData).move(bL.T.at(bL.tData));
          bT.push();
         }

        bL.lastElement();
        bT.T.at(bT.tKey ).move(T.at(parentKey));
        bT.T.at(bT.tData).move(bL.T.at(bL.tData));
        bT.push();

        for (int i = 0; i < T.at(nr).getInt(); ++i)                             // Merge right child branch
         {z();
          bR.shift();
          bT.T.at(bT.tKey ).move(bR.T.at(bR.tKey));
          bT.T.at(bT.tData).move(bR.T.at(bR.tData));
          bT.push();
         }

        bR.lastElement();                                                       // Top next

        bT.T.at(bT.tKey ).zero();
        bT.T.at(bT.tData).move(bR.T.at(bR.tData));
        bT.push();                                                              // Top so ignored by search ... except last

        tt(node_free, l); free();                                                  // Free the children
        tt(node_free, r); free();
        z(); T.at(stolenOrMerged).ones(); return;
       }
      z(); T.at(stolenOrMerged).zero(); return;
     }
   }

  private void mergeLeftSibling()                                               // Merge the left sibling
   {z(); tt(node_assertBranch, node_mergeLeftSibling); assertBranch();
    if (T.at(index).isZero())
     {z(); T.at(stolenOrMerged).zero();
      return;
     }
    tt(node_branchSize, node_mergeLeftSibling);
    branchSize();

    if (T.at(index).greaterThan(T.at(branchSize)))
     {stop("Index", T.at(index).getInt(), "too big");
     }
    if (T.at(branchSize).lessThan(T.constant(2)))
     {z(); T.at(stolenOrMerged).zero();
      return;
     }

    z();
    tt(node_branchBase, node_mergeLeftSibling);
    branchBase();
    bT.base(T.at(branchBase));
    bT.T.at(bT.index).move(T.at(index));
    bT.T.at(bT.index).dec();
    bT.elementAt();
    T.at(l).move(bT.T.at(bT.tData));

    bT.T.at(bT.index).move(T.at(index));
    bT.elementAt();
    T.at(r).move(bT.T.at(bT.tData));

    tt(node_hasLeavesForChildren, node_mergeLeftSibling);
    hasLeavesForChildren();
    if (T.at(hasLeavesForChildren).isOnes())                                    // Children are leaves
     {z();
      tt(node_leafBase, l); leafBase(); lL.base(T.at(leafBase));
      tt(node_leafBase, r); leafBase(); lR.base(T.at(leafBase));
      tt(node_leafSize, l); leafSize(); tt(nl, leafSize);
      tt(node_leafSize, r); leafSize(); tt(nr, leafSize);

      if (T.at(nl).getInt() + T.at(nr).getInt() >= maxKeysPerLeaf())            // Combined body would be too big
       {z(); T.at(stolenOrMerged).zero();
        return;
       }
      for (int i = 0; i < T.at(nl).getInt(); i++)                               // Transfer left to right
       {z();
        lL.pop();
        lR.T.at(lR.tKey ).move(lL.T.at(lL.tKey));
        lR.T.at(lR.tData).move(lL.T.at(lL.tData));
        lR.unshift();
       }
     }
    else                                                                        // Children are branches
     {z();
      tt(node_branchBase, l); branchBase(); bL.base(T.at(branchBase));
      tt(node_branchBase, r); branchBase(); bR.base(T.at(branchBase));
      tt(node_branchSize, l); branchSize(); tt(nl, branchSize);
      tt(node_branchSize, r); branchSize(); tt(nr, branchSize);

      if (T.at(nl).getInt() + 1 + T.at(nr).getInt() > maxKeysPerBranch())       // Merge not possible because there is not enough room for the combined result
       {z(); T.at(stolenOrMerged).zero();
        return;
       }
      z();
      bT.T.at(bT.index).move(T.at(index));                                      // Top key
      bT.T.at(bT.index).dec();                                                  // Top key
      bT.elementAt();                                                           // Top key
      bL.lastElement();                                                         // Last element of left child
      bR.T.at(bR.tKey ).move(bT.T.at(bT.tKey));
      bR.T.at(bR.tData).move(bL.T.at(bL.tData));
      bR.unshift();                                                             // Left top to right

      bL.pop();                                                                 // Remove left top
      for (int i = 0; i < T.at(nl).getInt(); i++)                               // Transfer left to right
       {z();
        bL.pop();
        bR.T.at(bR.tKey ).move(bL.T.at(bL.tKey));
        bR.T.at(bR.tData).move(bL.T.at(bL.tData));
        bR.unshift();
       }
     }
    tt(node_free, l); free();                                                   // Free the empty left node
    bT.T.at(bT.index).move(T.at(index));
    bT.T.at(bT.index).dec();

    bT.removeElementAt();                                                       // Reduce parent on left
    z(); T.at(stolenOrMerged).ones(); return;
   }

  private void mergeRightSibling()                                              // Merge the right sibling
   {z(); tt(node_assertBranch, node_mergeRightSibling); assertBranch();
    tt(node_branchSize, node_mergeRightSibling); branchSize();
    if (T.at(index).greaterThanOrEqual(T.at(branchSize)))
     {z(); T.at(stolenOrMerged).zero();
      return;
     }
    if (T.at(branchSize).lessThan(T.constant(2)))
     {z(); T.at(stolenOrMerged).zero();
      return;
     }
    z();
    tt(node_branchBase, node_mergeRightSibling);
    branchBase();
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
    if (T.at(hasLeavesForChildren).isOnes())                                    // Children are leaves
     {z();
      tt(node_leafBase, l);
      leafBase();
      lL.base(T.at(leafBase));
      tt(node_leafBase, r);
      leafBase();
      lR.base(T.at(leafBase));
      tt(node_leafSize, l);
      leafSize();
      tt(nl, leafSize);
      tt(node_leafSize, r);
      leafSize();
      tt(nr, leafSize);

      if (T.at(nl).getInt() + T.at(nr).getInt() > maxKeysPerLeaf())             // Combined body would be too big
       {z(); T.at(stolenOrMerged).zero();
        return;
       }
      for (int i = 0; i < T.at(nr).getInt(); i++)                               // Transfer right to left
       {z();
        lR.shift();
        lL.T.at(lL.tKey ).move(lR.T.at(lR.tKey));
        lL.T.at(lL.tData).move(lR.T.at(lR.tData));
        lL.push();
       }
     }
    else                                                                        // Children are branches
     {z();
      tt(node_branchBase, l);
      branchBase();
      bL.base(T.at(branchBase));
      tt(node_branchBase, r);
      branchBase();
      bR.base(T.at(branchBase));
      tt(node_branchSize, l);
      branchSize();
      tt(nl, branchSize);
      tt(node_branchSize, r);
      branchSize();
      tt(nr, branchSize);

      if (T.at(nl).getInt() + 1 + T.at(nr).getInt() > maxKeysPerBranch())       // Merge not possible because there is no where to put the steal
       {z(); T.at(stolenOrMerged).zero();
        return;
       }

      z(); bL.lastElement();                                                    // Last element of left child
      z(); bT.T.at(bT.index).move(T.at(index));
      z(); bT.elementAt();                                                      // Parent dividing element

      bL.T.at(bL.tKey).move(bT.T.at(bT.tKey));
      bL.T.at(bL.index).move(T.at(nl));
      bL.setElementAt();                                                        // Re-key left top

      final int N = T.at(nr).getInt()+1;
      for (int i = 0; i < N; i++)                                               // Transfer right to left including top
       {z();
        bR.shift();
        bL.T.at(bL.tKey) .move(bR.T.at(bR.tKey));
        bL.T.at(bL.tData).move(bR.T.at(bR.tData));
        bL.push();
       }
     }
    tt(node_free, r);
    free();                                                                     // Free the empty right node

    bT.T.at(bT.index).move(T.at(index));
    bT.T.at(bT.index).inc();

    bT.elementAt();
    T.at(parentKey).move(bT.T.at(bT.tKey));                                     // One up from dividing point in parent
    bT.T.at(bT.index).move(T.at(index));
    bT.elementAt();                                                             // Dividing point in parent
    bT.T.at(bT.tKey).move(T.at(parentKey));
    bT.setElementAt();                                                          // Install key of right sibling in this child
    bT.T.at(bT.index).move(T.at(index));                                        // Reduce parent on right
    bT.T.at(bT.index).inc();
    bT.removeElementAt();                                                       // Reduce parent on right
    z(); T.at(stolenOrMerged).ones();
    return;
   }

//D2 Balance                                                                    // Balance the tree by merging and stealing

  private void balance()                                                        // Augment the indexed child so it has at least two children in its body
   {z(); tt(node_assertBranch, node_balance); assertBranch();
    tt(node_branchSize, node_balance); branchSize();
    if (T.at(index).greaterThan(T.at(branchSize)))
     {stop("Index", T.at(index).getInt(), "too big");
     }
    tt(node_isLow, node_balance); isLow();
    if (T.at(isLow).isOnes() && !T.at(node_balance).isZero())
     {stop("Parent:", node_balance, "must not be low on children");
     }
    z();

    tt(node_branchBase, node_balance);
    branchBase();
    bT.base(T.at(branchBase));                                                  // Address parent

    bT.T.at(bT.index).move(T.at(index));                                        // Address child we are seeking to balance
    bT.elementAt();

    z();
    z(); T.at(node_isLow).move(bT.T.at(bT.tData));
         isLow();
    z(); if (T.at(isLow).isZero())                                              return;
    z(); tt(node_stealFromLeft,     node_balance); stealFromLeft    (); if (T.at(stolenOrMerged).isOnes()) return;
    z(); tt(node_stealFromRight,    node_balance); stealFromRight   (); if (T.at(stolenOrMerged).isOnes()) return;
    z(); tt(node_mergeLeftSibling,  node_balance); mergeLeftSibling (); if (T.at(stolenOrMerged).isOnes()) return;
    z(); tt(node_mergeRightSibling, node_balance); mergeRightSibling(); if (T.at(stolenOrMerged).isOnes()) return;
    stop("Unable to balance child:", bT.T.at(bT.tData).getInt());
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
   {z();
    final Stack<StringBuilder> S = new Stack<>();

    T.at(node_isLeaf).setInt(root); isLeaf();
    if (T.at(IsLeaf).isOnes())
     {z(); printLeaf(root, S, 0);
     }
    else
     {z(); printBranch(root, S, 0);
     }
    return printCollapsed(S);
   }

//D1 Find                                                                       // Find the data associated with a key.

  public void find()                                                            // Find the leaf associated with a key in the tree
   {z();

    T.at(node_isLeaf).zero();
    isLeaf();
    if (T.at(IsLeaf).isOnes())                                                  // The root is a leaf
     {z();
      tt(search, Key);
      T.at(node_findEqualInLeaf).setInt(root);
      findEqualInLeaf();
      T.at(find).zero();                                                        // Found in root
      return;
     }

    T.at(parent).zero();                                                        // Parent starts at root which is now known to be a branch

    for (int i = 0; i < maxDepth; i++)                                          // Step down through tree
     {z();
      tt(search, Key);
      tt(node_findFirstGreaterThanOrEqualInBranch, parent);
      findFirstGreaterThanOrEqualInBranch();                                    // Find next child in search path of key
      tt(child, next);
      tt(node_isLeaf, child);
      isLeaf();
      if (T.at(IsLeaf).isOnes())                                                // Found the containing leaf
       {z();
        tt(search, Key);
        tt(node_findEqualInLeaf, child);
        findEqualInLeaf();
        tt(find, child); return;
       }
      tt(parent, child);                                                        // Step down to lower branch
     }
    stop("Search for", Key, "did not terminate in a leaf");
   }

  private void findAndInsert()                                                  // Find the leaf that should contain this key and insert or update it is possible
   {z();
    find();
    tt(leafFound, find);                                                        // Find the leaf that should contain this key
    tt(node_leafBase, leafFound);
    leafBase();
    lT.base(T.at(leafBase));

    if (T.at(found).isOnes())                                                   // Found the key in the leaf so update it with the new data
     {z();
      lT.T.at(lT.tKey ).move(T.at(Key));
      lT.T.at(lT.tData).move(T.at(Data));
      lT.T.at(lT.index).move(T.at(index));
      lT.setElementAt();

      T.at(success).ones();
      T.at(inserted).zero();
      tt(findAndInsert, leafFound);
      return;
     }

    tt(node_isFull, leafFound);
    isFull();
    if (T.at(isFull).isZero())                                                  // Leaf is not full so we can insert immediately
     {z();
      tt(search, Key);
      tt(node_findFirstGreaterThanOrEqualInLeaf, leafFound);
      findFirstGreaterThanOrEqualInLeaf();
//      if (T.at(found).isOnes())                                                 // Overwrite existing key
//       {z();
        lT.T.at(lT.tKey ).move(T.at(Key));
        lT.T.at(lT.tData).move(T.at(Data));
        lT.T.at(lT.index).move(T.at(first));
        lT.insertElementAt();
//       }
//      else                                                                      // Insert into position
//       {z();
//        lT.T.at(lT.tKey ).move(T.at(Key));
//        lT.T.at(lT.tData).move(T.at(Data));
//        lT.push();
//       }
      T.at(success).ones();
      tt(findAndInsert, leafFound);
      return;
     }
    z(); T.at(success).zero();
   }

//D1 Insertion                                                                  // Insert a key, data pair into the tree or update and existing key with a new datum

  public void put()                                                             // Insert a key, data pair into the tree or update and existing key with a new datum
   {z(); findAndInsert();                                                       // Try direct insertion with no modifications to the shape of the tree
    if (T.at(success).isOnes()) return;                                         // Inserted or updated successfully
    z();
    T.at(node_isFull).zero();
    isFull();                                                                   // Start the insertion at the root(), after splitting it if necessary
    if (T.at(isFull).isOnes())                                                  // Start the insertion at the root(), after splitting it if necessary
     {z();
      T.at(node_isLeaf).zero(); isLeaf();
      if (T.at(IsLeaf).isOnes()) {z(); splitLeafRoot();}
      else                       {z(); splitBranchRoot();}
      z();
      findAndInsert();                                                          // Splitting the root() might have been enough
      if (T.at(success).isOnes()) return;                                       // Inserted or updated successfully
     }
    z(); T.at(parent).zero();

    for (int i = 0; i < maxDepth; i++)                                          // Step down from branch to branch through the tree until reaching a leaf repacking as we go
     {z();
      tt(search, Key);
      tt(node_findFirstGreaterThanOrEqualInBranch, parent);
              findFirstGreaterThanOrEqualInBranch();
      tt(child, next);
      tt(node_isLeaf, child); isLeaf();
      if (T.at(IsLeaf).isOnes())                                                // Reached a leaf
       {z();
        tt(splitParent, parent);
        tt(index, first);
        tt(node_splitLeaf, child);
        splitLeaf();                                                            // Split the child leaf
        findAndInsert();
        merge();
        return;
       }
      z();
      tt(node_isFull, child); isFull();
      if (T.at(isFull).isOnes())
       {z();
        tt(splitParent, parent);
        tt(index, first);
        tt(node_splitBranch, child); splitBranch();                              // Split the child branch in the search path for the key from the parent so the the search path does not contain a full branch above the containing leaf

        tt(search, Key);
        tt(node_findFirstGreaterThanOrEqualInBranch, parent);
        findFirstGreaterThanOrEqualInBranch();                                  // Perform the step down again as the split will have altered the local layout
        tt(parent, next);
       }
      else                                                                      // Step down directly as no split was required
       {z(); tt(parent, child);
       }
     }
    stop("Fallen off the end of the tree");                                     // The tree must be missing a leaf
   }

//D1 Deletion                                                                   // Delete a key, data pair from the tree

  private void findAndDelete()                                                  // Delete a key from the tree and returns its data if present without modifying the shape of tree
   {z(); find();                                                                // Try direct insertion with no modifications to the shape of the tree
    if (T.at(found).isZero()) return;                                           // Inserted or updated successfully
    z(); tt(node_leafBase, find); leafBase();                                   // The leaf that contains the key
    lT.base(T.at(leafBase));                                                    // The leaf that contains the key
    lT.T.at(lT.index).move(T.at(index)); lT.elementAt();                        // Position in the leaf of the key

    T.at(Data).move(lT.T.at(lT.tData));                                         // Key, data pairs in the leaf
    lT.removeElementAt();                                                       // Remove the key, data pair from the leaf
   }

  public void delete()                                                          // Delete a key from the tree and return its associated Data if the key was found.
   {z(); T.at(node_mergeRoot).zero(); mergeRoot();

    T.at(node_isLeaf).zero(); isLeaf();
    if (T.at(IsLeaf).isOnes())                                                  // Find and delete directly in root as a leaf
     {z(); findAndDelete(); return;
     }
    z();

    T.at(parent).zero();                                                        // Start at root

    for (int i = 0; i < maxDepth; i++)                                          // Step down from branch to branch through the tree until reaching a leaf repacking as we go
     {z();                                                                      // Step down
      tt(search, Key);
      tt(node_findFirstGreaterThanOrEqualInBranch, parent);
      findFirstGreaterThanOrEqualInBranch();

      tt(index, first); tt(node_balance, parent); balance();                    // Make sure there are enough entries in the parent to permit a deletion
      tt(child, next);

      tt(node_isLeaf, child); isLeaf();
      if (T.at(IsLeaf).isOnes())                                                // Reached a leaf
       {z();
        findAndDelete();
        final int f = T.at(found).getInt();
        merge();
        T.at(found).setInt(f);
        return;
       }
      z(); tt(parent, child);
     }
    stop("Fallen off the end of the tree");                                     // The tree must be missing a leaf
   }

//D1 Merge                                                                      // Merge along the specified search path

  private void merge()                                                          // Merge along the specified search path
   {z();
    mergeRoot();
    T.at(parent).zero();                                                        // Start at root

    for (int i = 0; i < maxDepth; i++)                                          // Step down from branch to branch through the tree until reaching a leaf repacking as we go
     {z(); tt(node_isLeaf, parent); isLeaf();
      if (T.at(IsLeaf).isOnes()) return;
      z();
      tt(node_branchSize, parent); branchSize();
      for (int j = 0; j < T.at(branchSize).getInt(); j++)                       // Try merging each sibling pair which might change the size of the parent
       {z();
        T.at(index).setInt(j);
        tt(node_mergeLeftSibling, parent); mergeLeftSibling();
        if (T.at(stolenOrMerged).isOnes()) --j;                                 // A successful merge of the left  sibling reduces the current index and the upper limit
        T.at(index).setInt(j);
        tt(node_mergeRightSibling, parent); mergeRightSibling();                // A successful merge of the right sibling maintains the current position but reduces the upper limit
        tt(node_branchSize,        parent); branchSize();
       }

      tt(search, Key);
      tt(node_findFirstGreaterThanOrEqualInBranch, parent);
      findFirstGreaterThanOrEqualInBranch();                                    // Step down
      tt(parent, next);
     }
    stop("Fallen off the end of the tree");                                     // The tree must be missing a leaf
   }

//D0 Tests                                                                      // Testing

  final static int[]random_small = {27, 442, 545, 317, 511, 578, 391, 993, 858, 586, 472, 906, 658, 704, 882, 246, 261, 501, 354, 903, 854, 279, 526, 686, 987, 403, 401, 989, 650, 576, 436, 560, 806, 554, 422, 298, 425, 912, 503, 611, 135, 447, 344, 338, 39, 804, 976, 186, 234, 106, 667, 494, 690, 480, 288, 151, 773, 769, 260, 809, 438, 237, 516, 29, 376, 72, 946, 103, 961, 55, 358, 232, 229, 90, 155, 657, 681, 43, 907, 564, 377, 615, 612, 157, 922, 272, 490, 679, 830, 839, 437, 826, 577, 937, 884, 13, 96, 273, 1, 188};
  final static int[]random_large = {5918,5624,2514,4291,1791,5109,7993,60,1345,2705,5849,1034,2085,4208,4590,7740,9367,6582,4178,5578,1120,378,7120,8646,5112,4903,1482,8005,3801,5439,4534,9524,6111,204,5459,248,4284,8037,5369,7334,3384,5193,2847,1660,5605,7371,3430,1786,1216,4282,2146,1969,7236,2187,136,2726,9480,5,4515,6082,969,5017,7809,9321,3826,9179,5781,3351,4819,4545,8607,4146,6682,1043,2890,2964,7472,9405,4348,8333,2915,9674,7225,4743,995,1321,3885,6061,9958,3901,4710,4185,4776,5070,8892,8506,6988,2317,9342,3764,9859,4724,5195,673,359,9740,2089,9942,3749,9208,1,7446,7023,5496,4206,3272,3527,8593,809,3149,4173,9605,9021,5120,5265,7121,8667,6911,4717,2535,2743,1289,1494,3788,6380,9366,2732,1501,8543,8013,5612,2393,7041,3350,3204,288,7213,1741,1238,9830,6722,4687,6758,8067,4443,5013,5374,6986,282,6762,192,340,5075,6970,7723,5913,1060,1641,1495,5738,1618,157,6891,173,7535,4952,9166,8950,8680,1974,5466,2383,3387,3392,2188,3140,6806,3131,6237,6249,7952,1114,9017,4285,7193,3191,3763,9087,7284,9170,6116,3717,6695,6538,6165,6449,8960,2897,6814,3283,6600,6151,4624,3992,5860,9557,1884,5585,2966,1061,6414,2431,9543,6654,7417,2617,878,8848,8241,3790,3370,8768,1694,9875,9882,8802,7072,3772,2689,5301,7921,7774,1614,494,2338,8638,4161,4523,5709,4305,17,9626,843,9284,3492,7755,5525,4423,9718,2237,7401,2686,8751,1585,5919,9444,3271,1490,7004,5980,3904,370,5930,6304,7737,93,5941,9079,4968,9266,262,2766,4999,2450,9518,5137,8405,483,8840,2231,700,8049,8823,9811,9378,3811,8074,153,1940,1998,4354,7830,7086,6132,9967,5680,448,1976,4101,7839,3122,4379,9296,4881,1246,4334,9457,5401,1945,9548,8290,1184,3464,132,2458,7704,1056,7554,6203,2270,6070,4889,7369,1676,485,3648,357,1912,9661,4246,1576,1836,4521,7667,6907,2098,8825,7404,4019,8284,3710,7202,7050,9870,3348,3624,9224,6601,7897,6288,3713,932,5596,353,2615,3273,833,1446,8624,2489,3872,486,1091,2493,4157,3611,6570,7107,9153,4543,9504,4746,1342,9737,3247,8984,3640,5698,7814,307,8775,1150,4330,3059,5784,2370,5248,4806,6107,9700,231,3566,5627,3957,5317,5415,8119,2588,9440,2961,9786,4769,466,5411,3080,7623,5031,2378,9286,4801,797,1527,2325,847,6341,5310,1926,9481,2115,2165,5255,5465,5561,3606,7673,7443,7243,8447,2348,7925,6447,8311,6729,4441,7763,8107,267,8135,9194,6775,3883,9639,612,5024,1351,7557,9241,5181,2239,8002,5446,747,166,325,9925,3820,9531,5163,3545,558,7103,7658,5670,8323,4821,6263,7982,59,3700,1082,4474,4353,8637,9558,5191,842,5925,6455,4092,9929,9961,290,3523,6290,7787,8266,7986,7269,6408,3620,406,5964,7289,1620,6726,1257,1993,7006,5545,2913,5093,5066,3019,7081,6760,6779,7061,9051,8852,8118,2340,6596,4594,9708,8430,8659,8920,9268,5431,9203,2823,1427,2203,6422,6193,5214,9566,8791,4964,7575,4350,56,2227,8545,5646,3089,2204,4081,487,8496,2258,4336,6955,3452,556,8602,8251,8569,8636,9430,1025,9459,7137,8392,3553,5945,9414,3078,1688,5480,327,8117,2289,2195,8564,9423,103,7724,3091,8548,7298,5279,6042,2855,3286,3542,9361,420,7020,4112,5320,5366,6379,114,9174,9744,592,5346,3985,3174,5157,9890,1605,3082,8099,4346,7256,8670,5687,6613,6620,1458,1045,7917,2980,2399,1433,3315,4084,178,7056,2132,2728,4421,9195,4181,6017,6229,2945,4627,2809,8816,6737,18,8981,3813,8890,5304,3789,6959,7476,1856,4197,6944,9578,5915,3060,9932,3463,67,7393,9857,5822,3187,501,653,8453,3691,9736,6845,1365,9645,4120,2157,8471,4436,6435,2758,7591,9805,7142,7612,4891,7342,5764,8683,8365,2967,6947,441,2116,6612,1399,7585,972,6548,5481,7733,7209,222,5903,6161,9172,9628,7348,1588,5992,6094,7176,4214,8702,2987,74,8486,9788,7164,5788,8535,8422,6826,1800,8965,4965,565,5609,4686,2556,9324,5000,9809,1994,4737,63,8992,4783,2536,4462,8868,6346,5553,3980,2670,1601,4272,8725,4698,7333,7826,9233,4198,1997,1687,4851,62,7893,8149,8015,341,2230,1280,5559,9756,3761,7834,6805,9287,4622,5748,2320,1958,9129,9649,1644,4323,5096,9490,7529,6444,7478,7044,9525,7713,234,7553,9099,9885,7135,6493,9793,6268,8363,2267,9157,9451,1438,9292,1637,3739,695,1090,4731,4549,5171,5975,7347,5192,5243,1084,2216,9860,3318,5594,5790,1107,220,9397,3378,1353,4498,6497,5442,7929,7377,9541,9871,9895,6742,9146,9409,292,6278,50,5288,2217,4923,6790,4730,9240,3006,3547,9347,7863,4275,3287,2673,7485,1915,9837,2931,3918,635,9131,1197,6250,3853,4303,790,5548,9993,3702,2446,3862,9652,4432,973,41,3507,8585,2444,1633,956,5789,1523,8657,4869,8580,8474,7093,7812,2549,7363,9315,6731,1130,7645,7018,7852,362,1636,2905,8006,4040,6643,8052,7021,3665,8383,715,1876,2783,3065,604,4566,8761,7911,1983,3836,5547,8495,8144,1950,2537,8575,640,8730,8303,1454,8165,6647,4762,909,9449,8640,9253,7293,8767,3004,4623,6862,8994,2520,1215,6299,8414,2576,6148,1510,313,3693,9843,8757,5774,8871,8061,8832,5573,5275,9452,1248,228,9749,2730};

  static void test_put_ascending()
   {final BtreeSA     t = btreeSA(4, 3);
    final int N = 64;
    for (int i = 1; i <= N; i++)
     {t.T.at(t.Key ).setInt(i);
      t.T.at(t.Data).setInt(i);
      t.put();
      say(t);
     }
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
   {final BtreeSA     t = btreeSA(8, 7);
    final int N = 64;
    for (int i = 1; i <= N; ++i) {t.T.at(t.Key).setInt(i); t.T.at(t.Data).setInt(i); t.put();}
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
   {final BtreeSA     t = btreeSA(2, 3);
    final int N = 64;
    for (int i = N; i > 0; --i) {t.T.at(t.Key).setInt(i); t.T.at(t.Data).setInt(i); t.put();}
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
   {final BtreeSA     t = btreeSA(6, 3);

    for (int i = 0; i < random_small.length; ++i)
     {t.T.at(t.Key).setInt(random_small[i]);
      t.T.at(t.Data).setInt(random_small[i]);
      t.put();
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
    final BtreeSA t = btreeSA(2, 3);
    final TreeMap<Integer,Integer> s = new TreeMap<>();
    for (int i = 0; i < random_large.length; ++i)
     {final int r = random_large[i];
      s.put(r, i);
      t.T.at(t.Key).setInt(r); t.T.at(t.Data).setInt(i);
      t.put();
     }
    final int a = s.firstKey(), b = s.lastKey();
    for (int i = a-1; i < b + 1; ++i)
     {t.T.at(t.Key).setInt(i);
      if (s.containsKey(i))
       {t.find();
        ok(t.T.at(t.found).isOnes());
        ok(t.T.at(t.data ).getInt(), s.get(i));
       }
      else
       {t.find();
        ok(t.T.at(t.found).isZero());
       }
     }
   }

  static void test_find()
   {final BtreeSA     t = btreeSA(8, 3);
    final int N = 32;
    for (int i = 1; i <= N; i++)
     {t.T.at(t.Key ).setInt(2*i);
      t.T.at(t.Data).setInt(2*i);
      t.put();
     }               // Insert
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
     {t.T.at(t.Key ).setInt(i);
      t.find();
      if (i > 0 && i % 2 == 0)
       {ok(t.T.at(t.found).isOnes());
        ok(t.T.at(t.data ).getInt(), i);
        t.T.at(t.Data).setInt(i-1);
        t.put();
       }
      else ok(t.T.at(t.found).isZero());
     }

    for (int i = 0; i <= 2*N+1; i++)
     {t.T.at(t.Key ).setInt(i);
      t.find();
      if (i > 0 && i % 2 == 0)
       {ok(t.T.at(t.found).isOnes());
        ok(t.T.at(t.data ).getInt(), i-1);
       }
      else ok(t.T.at(t.found).isZero());
     }
   }

  static void test_delete_ascending()
   {final BtreeSA     t = btreeSA(4, 3);

    final int N = 32;
    final boolean box = false;                                                  // Print read me
    for (int i = 1; i <= N; i++) {t.T.at(t.Key).setInt(i); t.T.at(t.Data).setInt(i); t.put();}
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
     {t.T.at(t.Key ).setInt(i);
      t.delete();
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
   {final BtreeSA     t = btreeSA(4, 3);
    final int N = 32;
    final boolean box = false;                                                  // Print read me
    for (int i = 1; i <= N; i++) {t.T.at(t.Key).setInt(i); t.T.at(t.Data).setInt(i); t.put();}
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
     {t.T.at(t.Key ).setInt(i);
      t.delete();
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
   {final BtreeSA     t = btreeSA(2, 3);

    final int M = 2;
    for (int i = 1; i <= M; i++)  {t.T.at(t.Key).setInt(i); t.T.at(t.Data).setInt(i); t.put();}
    //stop(""+t.toArray());
    ok(""+t.toArray(), """
[(0, key:1 data:1)
, (1, key:2 data:2)
]""");

    final int N = 16;
    for (int i = M; i <= N; i++)  {t.T.at(t.Key).setInt(i); t.T.at(t.Data).setInt(i); t.put();}
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

  static void test_delete_small_random()
   {final BtreeSA t = btreeSA(4, 3);

    for (int i = 0; i < random_small.length; ++i)
     {t.T.at(t.Key ).setInt(random_small[i]);
      t.T.at(t.Data).setInt(i);
      t.put();
     }

    for (int i = 0; i < random_small.length; ++i)
     {t.T.at(t.Key ).setInt(-1);
      t.delete();
      ok(t.T.at(t.found).isZero());

      t.T.at(t.Key ).setInt(random_small[i]);
      t.delete();
      ok(t.T.at(t.found).isOnes());
      ok(t.T.at(t.Data).getInt(), i);
     }
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {//test_put_ascending();
    //test_put_ascending_wide();
    //test_put_descending();
    //test_put_small_random();
    ////test_put_large_random();
    //test_find();
    //test_delete_ascending();
    //test_delete_descending();
    //test_to_array();
    test_delete_small_random();
   }

  static void newTests()                                                        // Tests being worked on
   {//oldTests();
    test_delete_small_random();
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
