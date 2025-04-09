//------------------------------------------------------------------------------
// BtreeStuckStatic in bit memory
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout a btree in a block on the surface of a silicon chip.

import java.util.*;

abstract class BtreeSML extends Test                                            // Manipulate a btree using static methods and memory
 {MemoryLayout memoryLayout = new MemoryLayout();                               // The memory layout of the btree
  abstract int maxSize();                                                       // The maxuimum bunber of leaves plus branches in the bree
  abstract int bitsPerKey();                                                    // The number of bits per key
  abstract int bitsPerData();                                                   // The number of bits per data
  abstract int bitsPerNext();                                                   // The number of bits in a next field
  abstract int bitsPerSize();                                                   // The number of bits in size field
  abstract int maxKeysPerLeaf();                                                // Maximum number of leafs in a key
  abstract int maxKeysPerBranch();                                              // Maximum number of keys in a branch
  int splitLeafSize  () {return maxKeysPerLeaf()   >> 1;}                       // The number of key, data pairs to split out of a leaf
  int splitBranchSize() {return maxKeysPerBranch() >> 1;}                       // The number of key, next pairs to split out of a branch

  StuckSML Leaf;                                                                // Leaf definition
  StuckSML Branch;                                                              // Branch defintion

  Layout.Field     leaf;                                                        // Layout of a leaf in the memory used by btree
  Layout.Field     branch;                                                      // Layout of a branch in the memory used by btree
  Layout.Union     branchOrLeaf;                                                // Layout of either a leaf or a branch in the memory used by btree
  Layout.Bit       isLeaf;                                                      // Whether the current node is a leaf or a branch
  Layout.Variable  free;                                                        // Free list chain
  Layout.Structure node;                                                        // Layout of a node in the memory used by btree
  Layout.Array     nodes;                                                       // Layout of an array of nodes in the memory used by btree
  Layout.Variable  freedChain;                                                  // Single linked list of freed nodes
  Layout.Structure bTree;                                                       // Btree

  final static int
   linesToPrintABranch =  4,                                                    // The number of lines required to print a branch
        maxPrintLevels = 10,                                                    // Maximum number of levels to print in a tree
              maxDepth = 99,                                                    // Maximum depth of any realistic tree
           testMaxSize = github_actions ? 1000 : 50;                            // Maximum number of leaves plus branchs during testing

  int         nodeUsed = 0;                                                     // Number of nodes currently in use
  int      maxNodeUsed = 0;                                                     // Maximum number of branches plus leaves used

  final Node       root;                                                        // The description ofthe root node
  final Node parentNode;                                                        // Node used for initializing the tree and for the parent node
  final Node   leftNode;                                                        // Node used for a left hand child
  final Node  rightNode;                                                        // Node used for a righthand child
  final Node  childNode;                                                        // Node used as a generic child
  final Node   tempNode;                                                        // Temporary node
  final Node   findNode;                                                        // Find node
  final Node    putNode;                                                        // Put node
  final Node deleteNode;                                                        // Delete node

  static boolean debug = false;                                                 // Debugging enabled

//D1 Construction                                                               // Create a BTree from nodes which can be branches or leaves.  The data associated with the BTree is stored only in the leaves opposite the keys

  BtreeSML()                                                                    // Define a BTree with user specified dimensions
   {zz();
    memoryLayout.layout = layout();
    memoryLayout.memory(new Memory("BtreeSML", memoryLayout.layout.size()));

          root = new Node();                                                    // The description ofthe root node
    parentNode = new Node();                                                    // Node used for initializing the tree and for the parent node
      leftNode = new Node();                                                    // Node used for a left hand child
     rightNode = new Node();                                                    // Node used for a righthand child
     childNode = new Node();                                                    // Node used as a generic child
      tempNode = new Node();                                                    // Temporary node
      findNode = new Node();                                                    // Find node
       putNode = new Node();                                                    // Put node
    deleteNode = new Node();                                                    // Delete node

    for (int i = maxSize(); i > 0; --i)                                         // Put all the nodes on the free chain at the start with low nodes first
     {final Node n = parentNode;
      n.node = i - 1;
      n.clear();
      final int  f = getInt(freedChain);
                     setInt(free, f,    n.node);
                     setInt(freedChain, n.node);
     }
    allocate(root, false);                                                      // The root is always at zero, which frees zero to act as the end of list marker on the free chain
    root.setLeaf();                                                             // The root starts as a leaf
    root.setStucks();                                                           // Describe stucks addressable fronm the root
   }

  static BtreeSML btreeSML(final int leafKeys, int branchKeys)                  // Define a test BTree with the specified dimensions
   {return  new BtreeSML()
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
    final BtreeSML btree = this;

    Leaf = new StuckSML()                                                       // Leaf
     {int               maxSize() {return btree.maxKeysPerLeaf();}
      int            bitsPerKey() {return btree.bitsPerKey();}
      int           bitsPerData() {return btree.bitsPerData();}
      int           bitsPerSize() {return btree.bitsPerSize();}
     };

    Branch = new StuckSML()                                                     // Branch
     {int               maxSize() {return btree.maxKeysPerBranch()+1;}          // Not forgetting top next
      int            bitsPerKey() {return btree.bitsPerKey();}
      int           bitsPerData() {return btree.bitsPerNext();}
      int           bitsPerSize() {return btree.bitsPerSize();}
     };

    final Layout l = Layout.layout();
    leaf         = l.duplicate("leaf",         Leaf  .layout());
    branch       = l.duplicate("branch",       Branch.layout());
    branchOrLeaf = l.union    ("branchOrLeaf", leaf,   branch);
    isLeaf       = l.bit      ("isLeaf");
    free         = l.variable ("free",         btree.bitsPerNext());
    node         = l.structure("node",         isLeaf, free, branchOrLeaf);
    nodes        = l.array    ("nodes",        node,         maxSize());
    freedChain   = l.variable ("freedChain",   btree.bitsPerNext());
    bTree        = l.structure("bTree",        freedChain  , nodes);
    return l.compile();
   }

  void fixMemory(Memory memory)                                                 // Fix memory so that we can use the methods in this implementation of btree against memory created by other implementations of btree as a long as they use the same memory layout
   {memoryLayout .memory(memory);                                               // Refer to supplied memory
    root.Leaf  .M.memory(memory);                                               // Fix leaf stuck of root
    root.Branch.M.memory(memory);                                               // Fix branch stuck of root
    root.setStucks();                                                           // Set base addresses for the leaf and branch stucks used by the root
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

  void allocate(Node node,  boolean check)                                      // Allocate a node with or without checking for sufficient free space
   {z(); final int  f = getInt(freedChain);                                     // Last freed node
    z(); if (check && f == 0) stop("No more memory available");                 // No more free nodes available
    z(); final int  F = getInt(free,       f);                                  // Second to last freed node
                        setInt(freedChain, F);                                  // Make second to last freed node the forst freed nod to liberate the existeing first free node
    node.node = f; node.clear();                                                   // Construct and clear the node
    maxNodeUsed  = max(maxNodeUsed, ++nodeUsed);                                // Number of nodes in use
//    return n;
   }

  void allocate(Node node) {z(); allocate(node, true);}                         // Allocate a node checking for free space

//D1 Components                                                                 // A branch or leaf in the tree

  class Node                                                                    // A branch or leaf in the tree
   {int node;                                                                   // The number of the node
    StuckSML Leaf, Branch;                                                      // Stucks used in this node with their base addresses set corrctly to allow addressing of the fields in the stuck

    Node()
     {Leaf   = BtreeSML.this.Leaf.copy();                                       // Address the leaf stuck
      Leaf.M.memory(BtreeSML.this.memoryLayout.memory);
      Branch = BtreeSML.this.Branch.copy();                                     // Address the branch stuck
      Branch.M.memory(BtreeSML.this.memoryLayout.memory);
     }

    boolean isLeaf() {z(); return getInt(isLeaf,    node) > 0;}                 // A leaf if true
    void   setLeaf() {z();        setInt(isLeaf, 1, node);}                     // Set as leaf
    void setBranch() {z();        setInt(isLeaf, 0, node);}                     // Set as branch

    void assertLeaf()   {if (!isLeaf()) stop("Leaf required");}
    void assertBranch() {if ( isLeaf()) stop("Branch required");}

    Node allocLeaf(Node node)                                                   // Allocate leaf
     {z(); allocate(node); node.setLeaf(); node.setStucks();
      return node;
     }

    Node allocBranch(Node node)                                                 // Allocate branch
     {z(); allocate(node); node.setBranch(); node.setStucks();
      return node;
     }

    void setStucks()                                                            // Descriptions of the stucks addressed by this node setting their base offsets
     {Leaf.base(leafBase());
      Branch.base(branchBase());
     }

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
      final Layout.Field n = BtreeSML.this.node;
      final int at = n.at(node), w = n.width;
      BtreeSML.this.memoryLayout.memory.zero(at, w);
     }

    void erase()                                                                // Clear a new node to ones as this is likely to create invalid values that will be easily detected in the case of erroneous frees
     {z();
      final Layout.Field n = BtreeSML.this.node;
      final int at = n.at(node), w = n.width;
      BtreeSML.this.memoryLayout.memory.ones(at, w);
     }

    int leafBase()   {z(); return leaf  .at(node);}                             // Base of leaf stuck in memory
    int branchBase() {z(); return branch.at(node);}                             // Base of branch stuck in memory

    int leafSize()   {z(); return Leaf.size();}                                 // Number of children in body of leaf
    int branchSize() {z(); return Branch.size()-1;}                             // Number of children in body of branch taking top for granted as it is always there

    boolean isFull()                                                            // The node is full
     {z(); return isLeaf() ? leafSize() == maxKeysPerLeaf  ():
                           branchSize() == maxKeysPerBranch();
     }

    boolean isLow() {z(); return (isLeaf() ? leafSize() : branchSize()) < 2;}   // The node is low on children making it impossible to merge two sibling children

    boolean hasLeavesForChildren()                                              // The node has leaves for children
     {z(); assertBranch();
      return node(tempNode, Branch.lastElement1().data).isLeaf();
     }

    int top()                                                                   // The top next element of a branch
     {z(); assertBranch();
      final int s = branchSize();
      return Branch.elementAt1(s).data;
     }

    public String toString()                                                    // Print a node
     {final StringBuilder s = new StringBuilder();
      if (isLeaf())                                                             // Print a leaf
       {s.append("Leaf(node:"+node+" size:"+leafSize()+")\n");
        final int N  = leafSize();                                              // Number of elements in leaf
        for (int i = 0; i < N; i++)                                             // Each element in the leaf
         {final StuckSML.ElementAt kd = Leaf.elementAt1(i);
          s.append("  "+(i+1)+" key:"+kd.key+" data:"+kd.data+"\n");
         }
       }
      else                                                                      // Print a branch
       {s.append("Branch(node:"+node+
                       " size:"+branchSize()+
                        " top:"+top()+"\n");

        final int N = Branch.size()-1;                                          // Number of elements in branch not including top
        for (int i = 0; i < N; i++)
         {final StuckSML.ElementAt kn = Branch.elementAt1(i);
          s.append(String.format("  %2d key:%2d next:%2d\n", i+1, kn.key, kn.data));
         }
        final StuckSML.ElementAt kn = Branch.elementAt1(N);
        s.append("             Top:"+kn.data+")\n");
       }
      return s.toString();
     }

//D2 Search                                                                     // Search within a node

    class FindEqualInLeaf                                                       // Find the first key in the leaf that is equal to the search key
     {Node     leaf;                                                            // The leaf being searched
      int    search;                                                            // Search key
      boolean found;                                                            // Whether the key was found
      int      data;                                                            // Data associated with the  key
      int     index;                                                            // Index of first such key if found
      int      base;                                                            // Base of the leaf

      FindEqualInLeaf() {}                                                      // Create finder

      FindEqualInLeaf findEqualInLeaf(int Search)                               // Find the first key in the leaf that is equal to the search key
       {z(); assertLeaf();
        leaf   = Node.this;
        search = Search;
        base   = leafBase();
        final StuckSML.Search s = Leaf.search1(Search);
        found  = s.found;
        index  = s.index;
        data   = s.data;
        return this;
       }

      public String toString()                                                  // Print details of find equal in leaf
       {final StringBuilder s = new StringBuilder();
        s.append("FindEqualInLeaf(Leaf:"+leaf.node);
        s.append(" Key:"+search+" found:"+found);
        if (found) s.append(" data:"+data+" index:"+index);
        s.append(")\n");
        return s.toString();
       }
     }

    final FindEqualInLeaf FindEqualInLeaf1 =                    new FindEqualInLeaf();
          FindEqualInLeaf findEqualInLeaf1(int Search) {z(); return FindEqualInLeaf1.findEqualInLeaf(Search);}

    class FindFirstGreaterThanOrEqualInLeaf                                     // Find the first key in the leaf that is equal to or greater than the search key
     {Node     leaf;                                                            // The leaf being searched
      int    search;                                                            // Search key
      boolean found;                                                            // Whether the key was found
      int     first;                                                            // Index of first such key if found
      int      base;                                                            // Base of the leaf

      FindFirstGreaterThanOrEqualInLeaf                                         // Find the first key in the  leaf that is equal to or greater than the search key
      findFirstGreaterThanOrEqualInLeaf(int Search)                             // Find the first key in the  leaf that is equal to or greater than the search key
       {z(); assertLeaf();
        leaf   = Node.this;
        search = Search;
        base   = leafBase();
        final StuckSML.SearchFirstGreaterThanOrEqual s =
        Leaf.searchFirstGreaterThanOrEqual1(Search);
        found = s.found;
        first = s.index;
        return this;
       }

      public String toString()                                                  // Print results of search
       {final StringBuilder s = new StringBuilder();
        s.append("FindFirstGreaterThanOrEqualInLeaf(Leaf:"+leaf.node);
        s.append(" Key:"+search+" found:"+found);
        if (found) s.append(" first:"+first);
        s.append(")\n");
        return s.toString();
       }
     }
    final FindFirstGreaterThanOrEqualInLeaf FindFirstGreaterThanOrEqualInLeaf1 =                    new FindFirstGreaterThanOrEqualInLeaf();
          FindFirstGreaterThanOrEqualInLeaf findFirstGreaterThanOrEqualInLeaf1(int Search) {z(); return FindFirstGreaterThanOrEqualInLeaf1.findFirstGreaterThanOrEqualInLeaf(Search);}

    class FindFirstGreaterThanOrEqualInBranch                                   // Find the first key in the branch that is equal to or greater than the search key
     {Node     branch;                                                          // The branch being searched
      int     search;                                                           // Search key
      boolean  found;                                                           // Whether the key was found
      int      first;                                                           // Index of first such key if found
      int       next;                                                           // The corresponding next field or top if no such key was found
      int       base;                                                           // Base of the branch

      FindFirstGreaterThanOrEqualInBranch                                       // Find the first key in the branch that is equal to or greater than the search key
      findFirstGreaterThanOrEqualInBranch(int Search)                           // Find the first key in the branch that is equal to or greater than the search key
       {z(); assertBranch();
        branch = Node.this;
        search = Search;
        base   = branchBase();
        final StuckSML.SearchFirstGreaterThanOrEqualExceptLast s =
            Branch.searchFirstGreaterThanOrEqualExceptLast1(Search);
        found = s.found;
        first = s.index;
        next  = s.found ? s.data : Branch.lastElement1().data;                  // Next if no key matches
        return this;
       }

      public String toString()                                                  // Print search results
       {final StringBuilder s = new StringBuilder();
        s.append("FindFirstGreaterThanOrEqualInBranch(branch:"+branch.node);
        s.append(" Key:"+search+" found:"+found+" next:"+next);
        if (found) s.append(" first:"+first);
        s.append(")\n");
        return s.toString();
       }
     }
    final FindFirstGreaterThanOrEqualInBranch FindFirstGreaterThanOrEqualInBranch1 =                    new FindFirstGreaterThanOrEqualInBranch();
          FindFirstGreaterThanOrEqualInBranch findFirstGreaterThanOrEqualInBranch1(int Search) {z(); return FindFirstGreaterThanOrEqualInBranch1.findFirstGreaterThanOrEqualInBranch(Search);}

//D2 Array                                                                      // Represent the contents of the tree as an array

    void leafToArray(Stack<StuckSML.ElementAt>s)                                // Leaf as an array
     {z(); assertLeaf();
      final int K = leafSize();
      for  (int i = 0; i < K; i++)
       {z();
        final StuckSML.ElementAt e = Leaf.elementAt1(i), E = e.copy();
        s.push(E);
       }
     }

    void branchToArray(Stack<StuckSML.ElementAt>s)                              // Branch to array
     {z(); assertBranch();
      final int K = Branch.size();                                              // Include top next

      if (K > 0)                                                                // Branch has key, next pairs
       {z();
        for  (int i = 0; i < K; i++)
         {z();
          final int next = Branch.elementAt1(i).data;                           // Each key, next pair
          final Node n = node(new Node(), next);                                // Using recursion here is unsatisfactory.
          if (n.isLeaf()) {z(); n.leafToArray(s);}
          else
           {z();
            if (next == 0)
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
      final int K = leafSize();
      for  (int i = 0; i < K; i++)
       {s.append(""+Leaf.elementAt1(i).key+",");
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
       {for  (int i = 0; i < K; i++)
         {final int next = Branch.elementAt1(i).data;                           // Each key, next pair
          final Node n = node(new Node(), next);                                // printing will not be part of the chip so we can use recursionand create new nodes as needed
          if (n.isLeaf())
           {n.printLeaf(S, level+1);
           }
          else
           {if (next == 0)
             {say("Cannot descend through root from index", i,
                  "in branch", node);
              break;
             }
            n.printBranch(S, level+1);
           }

          S.elementAt(L+0).append(""+Branch.elementAt1(i).key);                 // Key
          S.elementAt(L+1).append(""+node+(i > 0 ?  "."+i : ""));               // Branch,key, next pair
          S.elementAt(L+2).append(""+Branch.elementAt1(i).data);
         }
       }
      else                                                                      // Branch is empty so print just the index of the branch
       {S.elementAt(L+0).append(""+node+"Empty");
       }
      final int top = top();                                                    // Top next will always be present
      S.elementAt(L+3).append(top);                                             // Append top next

      final Node n = node(new Node(), top);
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

//D2 Split                                                                      // Split nodes in half to increase the number of nodes in the tree

    void splitLeafRoot()                                                        // Split a leaf which happens to be a full root into two half full leaves while transforming the root leaf into a branch
     {z(); assertLeaf();
      z(); if (node != 0) stop("Wanted root, but got node:", node);
      z(); if (!isFull()) stop("Root is not full, but has size:", leafSize());

      final Node l = allocLeaf(leftNode);                                       // New left leaf
      final Node r = allocLeaf(rightNode);                                      // New right leaf
      final Node p = this;                                                      // Root is the parent
      final int sl = splitLeafSize();

      for (int i = 0; i < sl; i++)                                              // Build left leaf from parent
       {z();
        final StuckSML.Shift f = p.Leaf.shift1();
        l.Leaf.push(f.key, f.data);
       }
      for (int i = 0; i < sl; i++)                                              // Build right leaf from parent
       {z();
        final StuckSML.Shift f = p.Leaf.shift1();
        r.Leaf.push(f.key, f.data);
       }

      final int first = r.Leaf.firstElement1().key;                             // First of right leaf
      final int last  = l.Leaf. lastElement1().key;                             // Last of left leaf
      final int kv    = (last + first) / 2;                                     // Mid key
      setBranch();
      p.Branch.clear();                                                         // Clear the branch
      p.Branch.push (kv, l.node);                                               // Insert left leaf into root
      p.Branch.push (0,  r.node);                                               // Insert right into root. This will be the top node and so ignored by search ... except last.
     }

    void splitBranchRoot()                                                      // Split a branch which happens to be a full root into two half full branches while retaining the current branch as the root
     {z(); assertBranch();
      z(); if (node != 0) stop("Not root, but node:", node);
      z(); if (!isFull()) stop("Root is not full, but has size:", branchSize());
      z();
      final Node l = allocBranch(leftNode);                                     // New left branch
      final Node r = allocBranch(rightNode);                                    // New right branch
      final Node p = this;                                                      // Root is the parent
      final int sb = splitBranchSize();                                         // Branch split size

      for (int i = 0; i < sb; i++)                                              // Build left child from parent
       {z();
        final StuckSML.Shift f = p.Branch.shift1();
        l.Branch.push(f.key, f.data);
       }
      final StuckSML.Shift  pl = Branch.shift2();                               // This key, next pair will be part of the root
      l.Branch.push(0, pl.data);                                                // Becomes top and so ignored by search ... except last

      for(int i = 0; i < sb; i++)                                               // Build right child from parent
       {z();
        final StuckSML.Shift f = p.Branch.shift1();
        r.Branch.push(f.key, f.data);
       }

      final StuckSML.Shift  pr = Branch.shift3();                               // Top of root
      r.Branch.push(0, pr.data);                                                // Becomes top and so ignored by search ... except last

      p.Branch.clear();                                                         // Refer to new branches from root
      p.Branch.push (pl.key, l.node);
      p.Branch.push (0,      r.node);                                           // Becomes top and so ignored by search ... except last
     }

    void splitLeaf(Node parent, int index)                                      // Split a leaf which is not the root
     {z(); assertLeaf();
      z(); if (node == 0) stop("Cannot split root with this method");
      z(); final int S = leafSize(), I = index;
      z(); final boolean nif = !isFull(), pif = parent.isFull();
      z(); if (nif)   stop("Leaf:", node, "is not full, but has:", S, this);
      z(); if (pif)   stop("Parent:", parent, "must not be full");
      z(); if (I < 0) stop("Index", I, "too small");
      z(); if (I > S) stop("Index", I, "too big for leaf with:", S);
      z();
      final Node p = parent;                                                    // Parent
      final Node l = allocLeaf(leftNode);                                       // New  split out leaf
      final Node r = this;                                                      // Existing  leaf
      final int sl = splitLeafSize();

      for (int i = 0; i < sl; i++)                                              // Build left leaf
       {z();
        final StuckSML.Shift f = r.Leaf.shift1();
        l.Leaf.push(f.key, f.data);
       }
      final int F = r.Leaf.firstElement1().key;
      final int L = l.Leaf. lastElement1().key;
      final int splitKey = (F + L) / 2;
      p.Branch.insertElementAt(splitKey, l.node, index);                        // Insert new key, next pair in parent
     }

    void splitBranch(Node parent, int index)                                    // Split a branch which is not the root by splitting right to left
     {z(); assertBranch();
      z(); final int bs = branchSize(), I = index, pn = parent.node, nd = node;
      z(); final boolean nif = !isFull(), pif = parent.isFull();
      z(); if (nd == 0) stop("Cannot split root with this method");
      z(); if (nif)     stop("Branch:", nd, "is not full, but", bs);
      z(); if (pif)     stop("Parent:", pn, "must not be full");
      z(); if (I < 0)   stop("Index", I, "too small in node:", nd);
      z(); if (I > bs)  stop("Index", I, "too big for branch with:",
                              bs, "in node:", nd);
      z();
      final Node p = parent;
      final Node l = allocBranch(leftNode);
      final Node r = this;
      final int pb = parent.branchBase(),
                lb = l.branchBase(), rb = r.branchBase();
      final int sb = splitBranchSize();

      for (int i = 0; i < sb; i++)                                              // Build left branch from right
       {z(); final StuckSML.Shift f = r.Branch.shift1();
        l.Branch.push(f.key, f.data);
       }

      final StuckSML.Shift split = r.Branch.shift1();                           // Build right branch
      l.Branch.push           (0, split.data);                                  // Becomes top and so is ignored by search ... except last
      p.Branch.insertElementAt(split.key, l.node, index);
     }

    boolean stealFromLeft(int index)                                            // Steal from the left sibling of the indicated child if possible to give to the right - Dennis Moore, Dennis Moore, Dennis Moore.
     {z(); assertBranch();
      z(); if (index == 0) return false;
      z(); if (index < 0)            stop("Index", index, "too small");
      z(); if (index > branchSize()) stop("Index", index, "too big");
      z();
      final Node                P = this;
      final StuckSML.ElementAt  L = P.Branch.elementAt1(index-1);
      final StuckSML.ElementAt  R = P.Branch.elementAt2(index+0);

      if (hasLeavesForChildren())                                               // Children are leaves
       {z();
        final Node  l = node( leftNode,L.data);
        final Node  r = node(rightNode,R.data);
        final int  nl = l.leafSize();
        final int  nr = r.leafSize();

        z(); if (nr >= maxKeysPerLeaf()) return false;                          // Steal not possible because there is no where to put the steal
        z(); if (nl <= 1) return false;                                         // Steal not allowed because it would leave the leaf sibling empty
        z();
        final StuckSML.LastElement le = l.Leaf.lastElement1();
        r.Leaf.insertElementAt(le.key, le.data, 0);                             // Increase right
        l.Leaf.pop();                                                           // Reduce left
        final int lk = l.Leaf.elementAt1(nl-2).key;                             // Last key on left
        P.Branch.setElementAt(lk, L.data, index-1);                             // Swap key of parent
       }
      else                                                                      // Children are branches
       {z();
        final Node l  = node( leftNode, L.data);
        final Node r  = node(rightNode, R.data);
        final int  nl = l.branchSize();
        final int  nr = r.branchSize();

        z(); if (nr >= maxKeysPerBranch()) return false;                        // Steal not possible because there is no where to put the steal
        z(); if (nl <= 1) return false;                                         // Steal not allowed because it would leave the left sibling empty
        z();
        final StuckSML.LastElement  t = l.Branch.lastElement1();                // Increase right with left top
        final int key = P.Branch.elementAt1(index).key;                         // Top key
        r.Branch.insertElementAt(key, t.data, 0);                               // Increase right with left top
        l.Branch.pop();                                                         // Remove left top
        final StuckSML.FirstElement b = r.Branch.firstElement1();               // Increase right with left top
        final int pk = P.Branch.elementAt1(index-1).key;                        // Parent key
        r.Branch.setElementAt             (pk, b.data, 0);                      // Reduce key of parent of right
        final int lk = l.Branch.lastElement1().key;                             // Last left key
        P.Branch.setElementAt(lk, L.data, index-1);                             // Reduce key of parent of left
       }
      return true;
     }

    boolean stealFromRight(int index)                                           // Steal from the right sibling of the indicated child if possible
     {z(); assertBranch();
      z(); if (index == branchSize()) return false;
      z(); if (index < 0)             stop("Index", index, "too small");
      z(); if (index >= branchSize()) stop("Index", index, "too big");
      z();
      final Node               P = this;
      final StuckSML.ElementAt L = P.Branch.elementAt1(index+0);
      final StuckSML.ElementAt R = P.Branch.elementAt2(index+1);

      if (hasLeavesForChildren())                                               // Children are leaves
       {z();
        final Node  l = node( leftNode, L.data);
        final Node  r = node(rightNode, R.data);
        final int  nl = l.leafSize();
        final int  nr = r.leafSize();

        z(); if (nl >= maxKeysPerLeaf()) return false;                          // Steal not possible because there is no where to put the steal
        z(); if (nr <= 1) return false;                                         // Steal not allowed because it would leave the right sibling empty
        z();
        final StuckSML.FirstElement f = r.Leaf.firstElement1();                 // First element of right child
        l.Leaf.push            (f.key, f.data);                                 // Increase left
        P.Branch.setElementAt  (f.key, L.data, index);                          // Swap key of parent
        r.Leaf.removeElementAt1(0);                                             // Reduce right
       }
      else                                                                      // Children are branches
       {z();
        final Node  l = node( leftNode, L.data);
        final Node  r = node(rightNode, R.data);
        final int  nl = l.branchSize();
        final int  nr = r.branchSize();

        z(); if (nl >= maxKeysPerBranch()) return false;                        // Steal not possible because there is no where to put the steal
        z(); if (nr <= 1) return false;                                         // Steal not allowed because it would leave the right sibling empty
        z();
        final StuckSML.LastElement le = l.Branch.lastElement1();                // Last element of left child
        l.Branch.setElementAt(L.key, le.data, nl);                              // Left top becomes real
        final StuckSML.FirstElement fe = r.Branch.firstElement1();              // First element of  right child
        l.Branch.push(0,      fe.data);                                         // New top for left is ignored by search ,.. except last
        P.Branch.setElementAt(fe.key, L.data, index);                           // Swap key of parent
        r.Branch.removeElementAt1(0);                                           // Reduce right
       }
      return true;
     }

//D2 Merge                                                                      // Merge two nodes together and free the resulting free node

    boolean mergeRoot()                                                         // Merge into the root
     {z();
      z(); if (root.isLeaf() || branchSize() > 1) return false;
      z(); if (node != 0) stop("Expected root, got:", node);
      z();
      final Node p = this;
      final Node l = node( leftNode, p.Branch.firstElement1().data);
      final Node r = node(rightNode, p.Branch. lastElement1().data);

      if (hasLeavesForChildren())                                               // Leaves
       {z();
        if (l.leafSize() + r.leafSize() <= maxKeysPerLeaf())
         {z(); p.Leaf.clear();
          final int nl = l.leafSize();
          for (int i = 0; i < nl; ++i)
           {z();
            final StuckSML.Shift f = l.Leaf.shift1();
            p.Leaf.push(f.key, f.data);
           }
          final int nr = r.leafSize();
          for (int i = 0; i < nr; ++i)
           {z();
            final StuckSML.Shift f = r.Leaf.shift1();
            p.Leaf.push(f.key, f.data);
           }
          setLeaf();
          l.free();
          r.free();
          return true;
         }
       }
      else if (l.branchSize() + 1 + r.branchSize() <= maxKeysPerBranch())       // Branches
       {z();
        final StuckSML.FirstElement pkn = p.Branch.firstElement1();
        p.Branch.clear();
        final int nl = l.branchSize();
        for (int i = 0; i < nl; ++i)
         {z();
          final StuckSML.Shift f = l.Branch.shift1();
          p.Branch.push(f.key, f.data);

         }
        final int data = l.Branch.lastElement1().data;
        p.Branch.push(pkn.key, data);
        final int nr = r.branchSize();
        for (int i = 0; i < nr; ++i)
         {z();
          final StuckSML.Shift f = r.Branch.shift1();
          p.Branch.push(f.key, f.data);
         }
        final int Data = r.Branch.lastElement2().data;                          // Top next
        p.Branch.push(0, Data);                                                 // Top so ignored by search ... except last
        l.free();
        r.free();
        return true;
       }
      z();
      return false;
     }

    boolean mergeLeftSibling(int index)                                         // Merge the left sibling
     {z(); assertBranch();
      z(); if (index == 0) return false;
      final int bs = branchSize();
      final String bss = "for branch of size:";
      z(); if (index < 0 ) stop("Index", index, "too small", bss, bs);
      z(); if (index > bs) stop("Index", index, "too big",   bss, bs);
      //if (branchSize() < 2)     stop("Node:", this,  "must have two or more children");
      z(); if (bs    < 2 ) return false;
      z();
      final Node               p = this;
      final StuckSML.ElementAt L = p.Branch.elementAt1(index-1);
      final StuckSML.ElementAt R = p.Branch.elementAt2(index-0);

      if (hasLeavesForChildren())                                               // Children are leaves
       {z();
        final Node  l = node( leftNode, L.data);
        final Node  r = node(rightNode, R.data);
        final  int nl = l.leafSize();
        final  int nr = r.leafSize();

        if (nl + nr >= maxKeysPerLeaf()) return false;                          // Combined body would be too big
        z();
        final int N = l.Leaf.size();                                            // Number of entries to remove
        for (int i = 0; i < N; i++)                                             // Transfer left to right
         {z(); final StuckSML.Pop q = l.Leaf.pop();
          r.Leaf.insertElementAt(q.key, q.data, 0);
         }
        l.free();                                                               // Free the empty left node
       }
      else                                                                      // Children are branches
       {z();
        final Node  l = node( leftNode, L.data);
        final Node  r = node(rightNode, R.data);
        final int  nl = l.branchSize();
        final int  nr = r.branchSize();

        if (nl + 1 + nr > maxKeysPerBranch()) return false;                     // Merge not possible because there is not enough room for the combined result
        z();
        final int t = p.Branch.elementAt1(index-1).key;                         // Top key
        final StuckSML.LastElement le = l.Branch.lastElement1();                // Last element of left child
        r.Branch.insertElementAt(t, le.data, 0);                                // Left top to right

        l.Branch.pop();                                                         // Remove left top
        final int N = l.Branch.size();                                          // Number of entries to remove
        for (int i = 0; i < N; i++)                                             // Transfer left to right
         {z();
          final StuckSML.Pop q = l.Branch.pop();
          r.Branch.insertElementAt(q.key, q.data, 0);
         }
        l.free();                                                               // Free the empty left node
       }
      p.Branch.removeElementAt1(index-1);                                       // Reduce parent on left
      return true;
     }

    boolean mergeRightSibling(int index)                                        // Merge the right sibling
     {z(); assertBranch();
      final int bs = branchSize();
      final String bss = "for branch of size:";
      z(); if (index >= bs) return false;
      z(); if (index <  0 ) stop("Index", index, "too small", bss, bs);
      z(); if (index >  bs) stop("Index", index, "too big",   bss, bs);
      //if (branchSize() < 2)     stop("Node:", this,  "must have two or more children");
      z(); if (bs < 2) return false;
      z();
      final Node               p = this;
      final StuckSML.ElementAt L = p.Branch.elementAt1(index+0);
      final StuckSML.ElementAt R = p.Branch.elementAt2(index+1);

      if (hasLeavesForChildren())                                               // Children are leaves
       {z();
        final Node  l = node( leftNode, L.data);
        final Node  r = node(rightNode, R.data);
        final int  nl = l.leafSize();
        final int  nr = r.leafSize();

        if (nl + nr > maxKeysPerLeaf()) return false;                           // Combined body would be too big for one leaf
        z();
        final int N = r.Leaf.size();                                            // Number of entries to remove
        for (int i = 0; i < N; i++)                                             // Transfer right to left
         {z();
          final StuckSML.Shift q = r.Leaf.shift1();
          l.Leaf.push(q.key, q.data);
         }
        r.free();                                                               // Free the empty right node
       }
      else                                                                      // Children are branches
       {z();
        final Node  l = node( leftNode, L.data);
        final Node  r = node(rightNode, R.data);
        final int  nl = l.branchSize();
        final int  nr = r.branchSize();

        if (nl + 1 + nr > maxKeysPerBranch()) return false;                     // Merge not possible because there is not enough room in a single branch
        z(); final StuckSML.LastElement le = l.Branch.lastElement1();           // Last element of left child
        z(); final StuckSML.ElementAt   ea = p.Branch.elementAt1(index);        // Parent dividing element
        l.Branch.setElementAt(ea.key, le.data, nl);                             // Re-key left top

        final int N = r.Branch.size();                                          // Number of entries to remove
        for (int i = 0; i < N; i++)                                             // Transfer right to left
         {z(); final StuckSML.Shift f = r.Branch.shift1();
          l.Branch.push(f.key, f.data);
         }
        r.free();                                                               // Free the empty right node
       }

      final StuckSML.ElementAt pkn = p.Branch.elementAt1(index+1);              // One up from dividing point in parent
      final StuckSML.ElementAt dkn = p.Branch.elementAt2(index);                // Dividing point in parent
      p.Branch.setElementAt(pkn.key, dkn.data, index);                          // Install key of right sibling in this child
      p.Branch.removeElementAt1(index+1);                                       // Reduce parent on right
      return true;
     }

//D2 Balance                                                                    // Balance the tree by merging and stealing

    void balance(int index)                                                     // Augment the indexed child so it has at least two children in its body
     {z(); assertBranch();
      z(); if (index < 0)            stop("Index", index, "too small");
      z(); if (index > branchSize()) stop("Index", index, "too big");
      z(); if (isLow() && node != root.node)
            {stop("Parent:", node, "must not be low on children");
            }
      z();

      final StuckSML.ElementAt p = Branch.elementAt1(index);

      z(); if (!node(tempNode, p.data).isLow()) return;
      z(); if (stealFromLeft    (index))        return;
      z(); if (stealFromRight   (index))        return;
      z(); if (mergeLeftSibling (index))        return;
      z(); if (mergeRightSibling(index))        return;
      stop("Unable to balance child:", p.data);
     }
   } // Node

  Node node(Node Node, int node)                                                // Refer to a node by number
   {Node.node = node;
    Node.setStucks();
    return Node;
   }

//D1 Array                                                                      // Key, data pairs in the tree as an array

  Stack<StuckSML.ElementAt> toArray()                                           // Key, data pairs in the tree as an array
   {z();
    final Stack<StuckSML.ElementAt> s = new Stack<>();

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
     {z();
      root.printLeaf(S, 0);
     }
    else
     {z();
      root.printBranch(S, 0);
     }
    return printCollapsed(S);
   }

//D1 Find                                                                       // Find the data associated with a key

  class Find                                                                    // Find the data associated with a key in the tree
   {Node.FindEqualInLeaf search;                                                // Details of the search of the containing leaf

    Find find(int Key)                                                          // Find the data associated with a key in the tree
     {z();
      if (root.isLeaf())                                                        // The root is a leaf
       {z();
        search = root.findEqualInLeaf1(Key);
        return this;
       }

      Node parent = root;                                                       // Parent starts at root which is known to be a branch

      for (int i = 0; i < maxDepth; i++)                                        // Step down through tree
       {z();
        final Node.FindFirstGreaterThanOrEqualInBranch down =                   // Find next child in search path of key
          parent.findFirstGreaterThanOrEqualInBranch1(Key);
        final Node n = node(findNode, down.next);

        if (n.isLeaf())                                                         // Found the containing search
         {z();
          search  = n.findEqualInLeaf1(Key);
          return this;
         }
        parent = node(parentNode, n.node);                                      // Step down to lower branch
       }
      stop("Search did not terminate in a leaf");
      return this;
     }

    Node     leaf()  {z(); return search.leaf;}
    boolean  found() {z(); return search.found;}
    int      index() {z(); return search.index;}
    int       data() {z(); return search.data;}

    public String toString()                                                    // Print find result
     {final StringBuilder s = new StringBuilder();
      s.append("Find(search:"+search);
      s.append( " search:"+index());
      if (found())
       {s.append( " data:"+data());
        s.append(" index:"+index());
       }
      s.append(")\n");
      return s.toString();
     }
   }
  final Find Find1 =                    new Find();
  final Find Find2 =                    new Find();
        Find find1(int Search) {z(); return Find1.find(Search);}
        Find find2(int Search) {z(); return Find2.find(Search);}

  class FindAndInsert extends Find                                              // Insert the specified key and data into the tree if there is room in the target leaf,or update the key with the data if the key already exists
   {int          key;                                                           // Key to insert
    int         data;                                                           // Data being inserted or updated
    boolean  success;                                                           // Inserted or updated if true
    boolean inserted;                                                           // Inserted if true

    FindAndInsert() {}
    FindAndInsert findAndInsert(int Key, int Data)                              // Find the leaf that should contain this key and insert or update it is possible
     {z(); find(Key);                                                           // Find the leaf that should contain this key
      key = Key; data = Data;

      if (found())                                                              // Found the key in the leaf so update it with the new data
       {z();
        leaf().Leaf.setElementAt(Key, Data, index());
        success = true; inserted = false;
        return this;
       }

      if (!leaf().isFull())                                                     // Leaf is not full so we can insert immediately
       {z();
        final Node.FindFirstGreaterThanOrEqualInLeaf f =
          leaf().findFirstGreaterThanOrEqualInLeaf1(Key);
//        if (f.found)                                                            // Overwrite existing key
         {z(); leaf().Leaf.insertElementAt(Key, Data, f.first);
         }
//        else                                                                    // Insert into position
//         {z();
//           leaf().Leaf.push(Key, Data);
//         }
        success = true;
        return this;
       }
      z(); success = false;
      return this;
     }

    public String toString()                                                    // Print find and insert
     {final StringBuilder s = new StringBuilder();
      s.append("FindAndInsert(key:"+key);
      s.append(" data:"+data);
      s.append(" success:"+success);
      if (success) s.append(" inserted:"+inserted);
      s.append(")\n" );
      return s.toString();
     }
   }

  final FindAndInsert FindAndInsert1 =                           new FindAndInsert();
  final FindAndInsert FindAndInsert2 =                           new FindAndInsert();
  final FindAndInsert FindAndInsert3 =                           new FindAndInsert();
        FindAndInsert findAndInsert1(int Key, int Data) {z(); return FindAndInsert1.findAndInsert(Key, Data);}
        FindAndInsert findAndInsert2(int Key, int Data) {z(); return FindAndInsert2.findAndInsert(Key, Data);}
        FindAndInsert findAndInsert3(int Key, int Data) {z(); return FindAndInsert3.findAndInsert(Key, Data);}

//D1 Insertion                                                                  // Insert a key, data pair into the tree or update and existing key with a new datum

  void put(int Key, int Data)                                                   // Insert a key, data pair into the tree or update and existing key with a new datum
   {z();
    final FindAndInsert f = findAndInsert1(Key, Data);                          // Try direct insertion with no modifications to the shape of the tree
    if (f.success) return;                                                      // Inserted or updated successfully
    z();
    if (root.isFull())                                                          // Start the insertion at the root, after splitting it if necessary
     {z();
      if (root.isLeaf()) {z(); root.splitLeafRoot();}
      else               {z(); root.splitBranchRoot();}
      z();
      final FindAndInsert F = findAndInsert2(Key, Data);                        // Splitting the root might have been enough
      if (F.success) return;                                                    // Inserted or updated successfully
     }
    z();
    Node p = root;

    for (int i = 0; i < maxDepth; i++)                                          // Step down from branch to branch through the tree until reaching a leaf repacking as we go
     {z();
      final Node.FindFirstGreaterThanOrEqualInBranch                            // Step down
      down = p.findFirstGreaterThanOrEqualInBranch1(Key);
      final Node q = node(putNode, down.next);
      if (q.isLeaf())                                                           // Reached a leaf
       {z();
        q.splitLeaf(p, down.first);
        findAndInsert3(Key, Data);
        merge(Key);
        return;
       }
      z();
      if (q.isFull())
       {z();
        q.splitBranch(p, down.first);                                           // Split the child branch in the search path for the key from the parent so the the search path does not contain a full branch above the containing leaf
        final Node.FindFirstGreaterThanOrEqualInBranch                          // Step down again as the split will have altered the local layout
        Down = p.findFirstGreaterThanOrEqualInBranch1(Key);
        p = node(parentNode, Down.next);
       }
      else
       {z();
        p = node(parentNode, q.node);
       }
     }
    stop("Fallen off the end of the tree");                                     // The tree must be missing a leaf
   }

  void put(int Key)                                                             // Put some test data into the tree
   {z(); put(Key, Key);
   }

//D1 Deletion                                                                   // Delete a key, data pair from the tree

  Integer findAndDelete(int Key)                                                // Delete a key from the tree and returns its data if present without modifying the shape of tree
   {z();
    z(); final Find     f = find1(Key);                                         // Try direct insertion with no modifications to the shape of the tree
    z(); if (!f.found()) return null;                                           // Inserted or updated successfully
    z(); final Node     l = f.leaf();                                           // The leaf that contains the key
    z(); final int      i = f.index();                                          // Position in the leaf of the key
    z(); final StuckSML.ElementAt kd = l.Leaf.elementAt1(i);                    // Key, data pairs in the leaf
    z(); l.Leaf.removeElementAt1(i);                                            // Remove the key, data pair from the leaf
    z(); return kd.data;
   }

  Integer delete(int Key)                                                       // Insert a key, data pair into the tree or update and existing key with a new datum
   {z(); root.mergeRoot();

    if (root.isLeaf())                                                          // Find and delete directly in root as a leaf
     {z(); return findAndDelete(Key);
     }
    z();

    Node p = root;                                                              // Start at root

    for (int i = 0; i < maxDepth; i++)                                          // Step down from branch to branch through the tree until reaching a leaf repacking as we go
     {z();
      final Node.FindFirstGreaterThanOrEqualInBranch                            // Step down
      down = p.findFirstGreaterThanOrEqualInBranch1(Key);

      p.balance(down.first);                                                    // Make sure there are enough entries in the parent to permit a deletion
      final Node q = node(deleteNode, down.next);

      if (q.isLeaf())                                                           // Reached a leaf
       {z();
        final int data = findAndDelete(Key);
        merge(Key);
        return data;
       }
      z(); p = node(parentNode, q.node);
     }
    stop("Fallen off the end of the tree");                                     // The tree must be missing a leaf
    return null;
   }

//D1 Merge                                                                      // Merge along the specified search path

  void merge(int Key)                                                           // Merge along the specified search path
   {z();
    root.mergeRoot();
    Node p = root;                                                              // Start at root

    for (int i = 0; i < maxDepth; i++)                                          // Step down from branch to branch through the tree until reaching a leaf repacking as we go
     {z(); if (p.isLeaf()) return;
      z();
      for (int j = 0; j < p.branchSize(); j++)                                  // Try merging each sibling pair which might change the size of the parent
       {z();
        if (p.mergeLeftSibling(j)) --j;                                         // A successful merge of the left  sibling reduces the current index and the upper limit
        p.mergeRightSibling(j);                                                 // A successful merge of the right sibling maintains the current position but reduces the upper limit
       }

      final Node.FindFirstGreaterThanOrEqualInBranch                            // Step down
      down = p.findFirstGreaterThanOrEqualInBranch1(Key);
      p = node(parentNode, down.next);
     }
    stop("Fallen off the end of the tree");                                     // The tree must be missing a leaf
   }

//D0 Tests                                                                      // Testing

  final static int[]random_small = {27, 442, 545, 317, 511, 578, 391, 993, 858, 586, 472, 906, 658, 704, 882, 246, 261, 501, 354, 903, 854, 279, 526, 686, 987, 403, 401, 989, 650, 576, 436, 560, 806, 554, 422, 298, 425, 912, 503, 611, 135, 447, 344, 338, 39, 804, 976, 186, 234, 106, 667, 494, 690, 480, 288, 151, 773, 769, 260, 809, 438, 237, 516, 29, 376, 72, 946, 103, 961, 55, 358, 232, 229, 90, 155, 657, 681, 43, 907, 564, 377, 615, 612, 157, 922, 272, 490, 679, 830, 839, 437, 826, 577, 937, 884, 13, 96, 273, 1, 188};
  final static int[]random_large = {5918,5624,2514,4291,1791,5109,7993,60,1345,2705,5849,1034,2085,4208,4590,7740,9367,6582,4178,5578,1120,378,7120,8646,5112,4903,1482,8005,3801,5439,4534,9524,6111,204,5459,248,4284,8037,5369,7334,3384,5193,2847,1660,5605,7371,3430,1786,1216,4282,2146,1969,7236,2187,136,2726,9480,5,4515,6082,969,5017,7809,9321,3826,9179,5781,3351,4819,4545,8607,4146,6682,1043,2890,2964,7472,9405,4348,8333,2915,9674,7225,4743,995,1321,3885,6061,9958,3901,4710,4185,4776,5070,8892,8506,6988,2317,9342,3764,9859,4724,5195,673,359,9740,2089,9942,3749,9208,1,7446,7023,5496,4206,3272,3527,8593,809,3149,4173,9605,9021,5120,5265,7121,8667,6911,4717,2535,2743,1289,1494,3788,6380,9366,2732,1501,8543,8013,5612,2393,7041,3350,3204,288,7213,1741,1238,9830,6722,4687,6758,8067,4443,5013,5374,6986,282,6762,192,340,5075,6970,7723,5913,1060,1641,1495,5738,1618,157,6891,173,7535,4952,9166,8950,8680,1974,5466,2383,3387,3392,2188,3140,6806,3131,6237,6249,7952,1114,9017,4285,7193,3191,3763,9087,7284,9170,6116,3717,6695,6538,6165,6449,8960,2897,6814,3283,6600,6151,4624,3992,5860,9557,1884,5585,2966,1061,6414,2431,9543,6654,7417,2617,878,8848,8241,3790,3370,8768,1694,9875,9882,8802,7072,3772,2689,5301,7921,7774,1614,494,2338,8638,4161,4523,5709,4305,17,9626,843,9284,3492,7755,5525,4423,9718,2237,7401,2686,8751,1585,5919,9444,3271,1490,7004,5980,3904,370,5930,6304,7737,93,5941,9079,4968,9266,262,2766,4999,2450,9518,5137,8405,483,8840,2231,700,8049,8823,9811,9378,3811,8074,153,1940,1998,4354,7830,7086,6132,9967,5680,448,1976,4101,7839,3122,4379,9296,4881,1246,4334,9457,5401,1945,9548,8290,1184,3464,132,2458,7704,1056,7554,6203,2270,6070,4889,7369,1676,485,3648,357,1912,9661,4246,1576,1836,4521,7667,6907,2098,8825,7404,4019,8284,3710,7202,7050,9870,3348,3624,9224,6601,7897,6288,3713,932,5596,353,2615,3273,833,1446,8624,2489,3872,486,1091,2493,4157,3611,6570,7107,9153,4543,9504,4746,1342,9737,3247,8984,3640,5698,7814,307,8775,1150,4330,3059,5784,2370,5248,4806,6107,9700,231,3566,5627,3957,5317,5415,8119,2588,9440,2961,9786,4769,466,5411,3080,7623,5031,2378,9286,4801,797,1527,2325,847,6341,5310,1926,9481,2115,2165,5255,5465,5561,3606,7673,7443,7243,8447,2348,7925,6447,8311,6729,4441,7763,8107,267,8135,9194,6775,3883,9639,612,5024,1351,7557,9241,5181,2239,8002,5446,747,166,325,9925,3820,9531,5163,3545,558,7103,7658,5670,8323,4821,6263,7982,59,3700,1082,4474,4353,8637,9558,5191,842,5925,6455,4092,9929,9961,290,3523,6290,7787,8266,7986,7269,6408,3620,406,5964,7289,1620,6726,1257,1993,7006,5545,2913,5093,5066,3019,7081,6760,6779,7061,9051,8852,8118,2340,6596,4594,9708,8430,8659,8920,9268,5431,9203,2823,1427,2203,6422,6193,5214,9566,8791,4964,7575,4350,56,2227,8545,5646,3089,2204,4081,487,8496,2258,4336,6955,3452,556,8602,8251,8569,8636,9430,1025,9459,7137,8392,3553,5945,9414,3078,1688,5480,327,8117,2289,2195,8564,9423,103,7724,3091,8548,7298,5279,6042,2855,3286,3542,9361,420,7020,4112,5320,5366,6379,114,9174,9744,592,5346,3985,3174,5157,9890,1605,3082,8099,4346,7256,8670,5687,6613,6620,1458,1045,7917,2980,2399,1433,3315,4084,178,7056,2132,2728,4421,9195,4181,6017,6229,2945,4627,2809,8816,6737,18,8981,3813,8890,5304,3789,6959,7476,1856,4197,6944,9578,5915,3060,9932,3463,67,7393,9857,5822,3187,501,653,8453,3691,9736,6845,1365,9645,4120,2157,8471,4436,6435,2758,7591,9805,7142,7612,4891,7342,5764,8683,8365,2967,6947,441,2116,6612,1399,7585,972,6548,5481,7733,7209,222,5903,6161,9172,9628,7348,1588,5992,6094,7176,4214,8702,2987,74,8486,9788,7164,5788,8535,8422,6826,1800,8965,4965,565,5609,4686,2556,9324,5000,9809,1994,4737,63,8992,4783,2536,4462,8868,6346,5553,3980,2670,1601,4272,8725,4698,7333,7826,9233,4198,1997,1687,4851,62,7893,8149,8015,341,2230,1280,5559,9756,3761,7834,6805,9287,4622,5748,2320,1958,9129,9649,1644,4323,5096,9490,7529,6444,7478,7044,9525,7713,234,7553,9099,9885,7135,6493,9793,6268,8363,2267,9157,9451,1438,9292,1637,3739,695,1090,4731,4549,5171,5975,7347,5192,5243,1084,2216,9860,3318,5594,5790,1107,220,9397,3378,1353,4498,6497,5442,7929,7377,9541,9871,9895,6742,9146,9409,292,6278,50,5288,2217,4923,6790,4730,9240,3006,3547,9347,7863,4275,3287,2673,7485,1915,9837,2931,3918,635,9131,1197,6250,3853,4303,790,5548,9993,3702,2446,3862,9652,4432,973,41,3507,8585,2444,1633,956,5789,1523,8657,4869,8580,8474,7093,7812,2549,7363,9315,6731,1130,7645,7018,7852,362,1636,2905,8006,4040,6643,8052,7021,3665,8383,715,1876,2783,3065,604,4566,8761,7911,1983,3836,5547,8495,8144,1950,2537,8575,640,8730,8303,1454,8165,6647,4762,909,9449,8640,9253,7293,8767,3004,4623,6862,8994,2520,1215,6299,8414,2576,6148,1510,313,3693,9843,8757,5774,8871,8061,8832,5573,5275,9452,1248,228,9749,2730};

  static void test_put_ascending()
   {final BtreeSML t = btreeSML(4, 3);
    final int N = 64;
    for (int i = 1; i <= N; i++) t.put(i);
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
   {final BtreeSML t = btreeSML(8, 7);
    final int N = 64;
    for (int i = 1; i <= N; ++i) t.put(i);
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
   {final BtreeSML t = btreeSML(2, 3);
    final int N = 64;
    for (int i = N; i > 0; --i) t.put(i);
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
   {final BtreeSML t = btreeSML(6, 3);
    for (int i = 0; i < random_small.length; ++i) t.put(random_small[i]);
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
    final BtreeSML t = btreeSML(2, 3);
    final TreeMap<Integer,Integer> s = new TreeMap<>();
    for (int i = 0; i < random_large.length; ++i)
     {final int r = random_large[i];
      s.put(r, i);
      t.put(r, i);
     }
    final int a = s.firstKey(), b = s.lastKey();
    for (int i = a-1; i < b + 1; ++i)
     {if (s.containsKey(i))
       {Find f = t.find1(i);
        ok(f.found());
        ok(f.data(), s.get(i));
       }
      else
       {Find f = t.find1(i);
        ok(!f.found());
       }
     }
   }

  static void test_find()
   {final BtreeSML t = btreeSML(8, 3);
    final int N = 32;
    for (int i = 1; i <= N; i++) t.put(2*i);                                    // Insert
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
     {Find f = t.find1(i);
      if (i > 0 && i % 2 == 0)
       {ok(f.found(), true);
        ok(f.data(),  i);
        t.put(i, i-1);
       }
      else ok(f.found(), false);
     }

    for (int i = 0; i <= 2*N+1; i++)
     {Find f = t.find2(i);
      if (i > 0 && i % 2 == 0)
       {ok(f.found(), true);
        ok(f.data(),  i-1);
       }
      else ok(f.found(), false);
     }
   }

  static void test_delete_ascending()
   {final BtreeSML t = btreeSML(4, 3);
    final int N = 32;
    final boolean box = false;                                                  // Print read me
    for (int i = 1; i <= N; i++) t.put(i);
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
     {t.delete(i);
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
   {final BtreeSML t = btreeSML(4, 3);
    final int N = 32;
    final boolean box = false;                                                  // Print read me
    for (int i = 1; i <= N; i++) t.put(i);
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
     {t.delete(i);
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
   {final BtreeSML t = btreeSML(2, 3);

    final int M = 2;
    for (int i = 1; i <= M; i++) t.put(i);
    //stop(""+t.toArray());
    ok(""+t.toArray(), """
[ElementAt(index:0 key:1 data:1)
, ElementAt(index:1 key:2 data:2)
]""");

    final int N = 16;
    for (int i = M; i <= N; i++) t.put(i);
    //stop(""+t.toArray());
    ok(""+t.toArray(), """
[ElementAt(index:0 key:1 data:1)
, ElementAt(index:1 key:2 data:2)
, ElementAt(index:0 key:3 data:3)
, ElementAt(index:1 key:4 data:4)
, ElementAt(index:0 key:5 data:5)
, ElementAt(index:1 key:6 data:6)
, ElementAt(index:0 key:7 data:7)
, ElementAt(index:1 key:8 data:8)
, ElementAt(index:0 key:9 data:9)
, ElementAt(index:1 key:10 data:10)
, ElementAt(index:0 key:11 data:11)
, ElementAt(index:1 key:12 data:12)
, ElementAt(index:0 key:13 data:13)
, ElementAt(index:1 key:14 data:14)
, ElementAt(index:0 key:15 data:15)
, ElementAt(index:1 key:16 data:16)
]""");
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {//test_put_ascending();                                                       //  7.99
    //test_put_ascending_wide();                                                  //  5.33
    //test_put_descending();                                                      // 12.98
    test_put_small_random();                                                    //  8.72
    //test_put_large_random();                                                    //  0
    //test_find();                                                                //  4.62
    //test_delete_ascending();                                                    //  7.27
    //test_delete_descending();                                                   //  7.66
    //test_to_array();                                                            //  2.52
   }

  static void newTests()                                                        // Tests being worked on
   {oldTests();
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
