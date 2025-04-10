//------------------------------------------------------------------------------
// Btree on the Basic Array Machine.
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2025
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Btree in a block on the surface of a silicon chip.

import java.util.*;

class BtreeBam extends Test                                                     // Manipulate a btree
 {final LayoutBam L;                                                            // The btree laid out in arrays

  final int
    maxKeysPerLeaf,                                                             // Maximum number of leafs in a key
    maxKeysPerBranch,                                                           // Maximum number of keys in a branch
    splitLeafSize,                                                              // The number of key, data pairs to split out of a leaf
    splitBranchSize,                                                            // The number of key, next pairs to split out of a branch
    numberOfNodes;                                                              // The number of nodes in the btree

  final static int
              maxDepth = 9,                                                     // Maximum depth of any realistic tree
                gutter = 2,                                                     // Gutter between printed items
          linesPerNode = 4,                                                     // Number of lines needed to print a node
          linesPerTree = linesPerNode * maxDepth;                               // Number of lines needed to print a tree

  static boolean debug = false;                                                 // Debugging when enabled

//D1 Construction                                                               // Create a BTree from nodes which can be branches or leaves.  The data associated with the BTree is stored only in the leaves opposite the keys

  BtreeBam(int MaxKeysPerLeaf, int MaxKeysPerBranch, int NumberOfNodes)         // Define a BTree with the specified dimensions
   {zz();
    maxKeysPerLeaf   = MaxKeysPerLeaf;
    maxKeysPerBranch = MaxKeysPerBranch;
    splitLeafSize    = maxKeysPerLeaf   >> 1;
    splitBranchSize  = maxKeysPerBranch >> 1;
    numberOfNodes    = NumberOfNodes;
    L                = layoutBtree();
    L.zero("free"); L.zero("isLeaf"); L.zero("current_size");                   // Clear all control information
    L.zero("keys"); L.zero("data");                                             // Clear all data
    for (int i = numberOfNodes-1; i > 1; --i) L.set(i, "free", ""+(i-1));       // Initialize free chain by putting all the nodes on the free chain except the root (which is permanently allocated at position 0) with the low nodes first to be allocated.
    L.set(1, "freeChainHead");                                                  // The fiurst freed node
    L.set(0, "root");                                                           // The root
    L.set(1, "isLeaf", "root");                                                 // The root starts as a leaf
   }

  LayoutBam layoutBtree()                                                       // Layout the btree
   {final int k = max(maxKeysPerBranch+1,   maxKeysPerLeaf);
    final int d = max(maxKeysPerBranch+1, maxKeysPerLeaf);

    return new LayoutBam()
     {void load()
       {array("freeChainHead");                                                 // The head of the free chain
        array("root");                                                          // Always zero indicating the location of the root which never changes
        array("stuck");                                                         // The index of the stuck to be operated on
        array("s_key");                                                         // The input key for searching or adding to a stuck and the output of a search greater than
        array("s_data");                                                        // The input data for a stuck or the resulting data found in a stuck
        array("s_index");                                                       // The input index of a key,data pair in a stuck or the output index of a located key, data pair
        array("s_found");                                                       // Whether a matching key was found when searching a stuck
        array("f_leaf");                                                        // Node number of leaf found
        array("f_found");                                                       // Whether the key was found
        array("f_index");                                                       // The index in the leaf or branch of the greater than or equal key
        array("f_key");                                                         // Matching found key
        array("f_data");                                                        // Data associated with key
        array("f_success");                                                     // Inserted or updated if true
        array("f_inserted");                                                    // Inserted if true
        array("allocate");                                                      // Result of calling allocate
        array("free_1");                                                        // Result of calling isLeaf
        array("isLeaf_0");                                                      // A node to be freed
        array("isLeaf_1");                                                      // First parameter to is leaf
        array("rootIsLeaf");                                                    // WHether the root is a
        array("free",         numberOfNodes);                                   // Used to place the node  on the free chain else zero if in use
        array("isLeaf",       numberOfNodes);                                   // Whether each node is a leaf or a branch
        array("current_size", numberOfNodes);                                   // Current size of stuck
        array("keys",         numberOfNodes, k);                                // Keys
        array("data",         numberOfNodes, d);                                // Data
       }
     };
   }

//D1 Nodes

//D2 Allocate and free                                                          // Allocate nodes for use in the tree and free them for reuse

  void allocate()                                                                // Allocate a node
   {final String fc = "freeChainHead", a = "allocate";
    L.move(a, fc);
    int f = L.get(a);                                                           // Last freed node
    if (f == 0) stop("No more memory available");                               // No more free nodes available
    L.move(fc, "free", a);                                                      // Second to last freed node becomes head of the free chain
    L.zero("free", a); L.zero("isLeaf", a); L.zero("current_size", a);          // Clear all control information
    L.zero("keys", a); L.zero("data",   a);
   }

  void  free()                                                                  // Free a new node to make it available for reuse
   {final String f = "free_1";                                                  // Index of node to be freed
    final int n = L.get(f);
    if (n == 0) stop("Cannot free root");                                       // The root is never freed
    L.ones("free", f); L.ones("isLeaf", f); L.ones("current_size", f);          // Invalidate all control information
    L.ones("keys", f); L.ones("data",   f);
    L.move("free", "freeChainHead", f);                                         // Chain this node in front of the last freed node
    L.move("freeChainHead", f);                                                 // Freed node becomes head of the free chain
   }

  int allocLeaf()   {allocate(); int n = L.get("allocate"); setLeaf  (n); return n;} // Allocate leaf
  int allocBranch() {allocate(); int n = L.get("allocate"); setBranch(n); return n;} // Allocate branch

//D2 Basics                                                                     // Basic operations on nodes

  int getKey  () {return L.get("s_key");}                                       // Get current key
  int getData () {return L.get("s_data");}                                      // Get current data
  int getStuck() {return L.get("stuck");}                                       // Get current stuck
  int getIndex() {return L.get("s_index");}                                     // Get current index

  void setKey  (int n) {L.set(n, "s_key");}                                     // Set current key
  void setData (int n) {L.set(n, "s_data");}                                    // Set current data
  void setStuck(int n) {L.set(n, "stuck");}                                     // Set current stuck
  void setIndex(int n) {L.set(n, "s_index");}                                   // Set current index

  int getFLeaf    () {return L.get("f_leaf");}                                  // Node number of leaf found
  int getFound    () {return L.get("f_found");}                                 // Whether the key was found
  int getFIndex   () {return L.get("f_index");}                                 // The index in the leaf or branch of the greater than or equal key
  int getFKey     () {return L.get("f_key");}                                   // Matching found key
  int getFData    () {return L.get("f_data");}                                  // Data associated with key
  int getFSuccess () {return L.get("f_success");}                               // Inserted or updated if true
  int getFInserted() {return L.get("f_inserted");}                              // Inserted if true

  void putFLeaf    (int n) {L.set(n, "f_leaf");}                                // Node number of leaf found
  void putFound    (int n) {L.set(n, "f_found");}                               // Whether the key was found
  void putFIndex   (int n) {L.set(n, "f_index");}                               // The index in the leaf or branch of the greater than or equal key
  void putFKey     (int n) {L.set(n, "f_key");}                                 // Matching found key
  void putFData    (int n) {L.set(n, "f_data");}                                // Data associated with key
  void putFSuccess (int n) {L.set(n, "f_success");}                             // Inserted or updated if true
  void putFInserted(int n) {L.set(n, "f_inserted");}                            // Inserted if true

  boolean isLeaf(int n)  {return L.get("isLeaf", ""+n)   > 0;}                  // A leaf if true

  void rootIsLeaf() {L.move("rootIsLeaf", "isLeaf", "root");}                   // The root is a leaf if the target is not zero

  void    setLeaf(int n) {L.set(1, "isLeaf",  ""+n);}                           // Set as leaf
  void  setBranch(int n) {L.set(0, "isLeaf",  ""+n);}                           // Set as branch
  void    setLeafRoot () {L.set(1, "isLeaf", "root");}                          // Set root as leaf
  void  setBranchRoot () {L.set(0, "isLeaf", "root");}                          // Set root as branch

  int   leafSize(int n)  {return L.get("current_size", ""+n);}                  // Number of children in leaf
  int branchSize(int n)  {return L.get("current_size", ""+n)-1;}                // Number of children in body of branch

  boolean isFull(int n)                                                         // The node is full
   {return isLeaf(n) ? leafSize(n) >= maxKeysPerLeaf  :
                     branchSize(n) >= maxKeysPerBranch;                         // Allow for top
   }

  boolean isFullRoot() {return isFull(0);}                                      // The node is full
  boolean isLow(int n) {return (isLeaf(n) ? leafSize(n) : branchSize(n)) < 2;}  // The node is low on children making it impossible to merge two sibling children

  boolean hasLeavesForChildren(int n)                                           // Whether the branch has leaves for children
   {final int c = L.get("data", ""+n, "0");
    return isLeaf(c);
   }

//D2 Nodes                                                                      // Operations on nodes

//D2 Split                                                                      // Split nodes in half to increase the number of nodes in the tree

  void splitLeafRoot()                                                          // Split a leaf which happens to be a full root into two half full leaves while transforming the root leaf into a branch
   {final int l = allocLeaf();                                                  // New left leaf
    final int r = allocLeaf();                                                  // New right leaf
    final int p = 0;                                                            // Root
    final int sl = splitLeafSize;                                               // Size of a split leaf

    for (int i = 0; i < sl; i++)                                                // Build left leaf from parent
     {setStuck(p); stuck_shift();
      setStuck(l); stuck_push();
     }
    for (int i = 0; i < sl; i++)                                                // Build right leaf from parent
     {setStuck(0);  stuck_shift();
      setStuck(r);  stuck_push();
     }

    stuck_firstElement();
    int first = getKey();                                                       // First of right leaf

    setStuck(l); stuck_lastElement();                                            // Last of left leaf
    int last = getKey();                                                        // Last of left leaf
    int kv   = (last + first) / 2;                                              // Mid key

    setBranchRoot();
    setStuck(0); stuck_clear();                                                 // Clear the root
    setKey(kv);  setData(l); stuck_push();                                      // Insert left leaf into root
    setKey(0);   setData(r); stuck_push();                                      // Insert right into root. This will be the top node and so ignored by search ... except last.
   }

  void splitBranchRoot()                                                        // Split a branch which happens to be a full root into two half full branches while retaining the current branch as the root
   {final int l = allocBranch();                                                // New left branch
    final int r = allocBranch();                                                // New right branch
    final int p = 0;                                                            // Root
    final int sb = splitBranchSize;                                             // Branch split size

    for (int i = 0; i < sb; i++)                                                // Build left child from parent
     {setStuck(p); stuck_shift();
      setStuck(l); stuck_push();
     }
    setStuck(p); stuck_shift();
    final int plk = getKey();
    setStuck(l); setKey(0); stuck_push();                                       // Left top

    for(int i = 0; i < sb; i++)                                                 // Build right child from parent
     {setStuck(p); stuck_shift();
      setStuck(r); stuck_push();
     }

    setStuck(p); stuck_shift();
    setStuck(r); setKey(0); stuck_push();                                       // Right top

    setStuck(p);                                                                // Clear root
    stuck_clear();
    setKey(plk);  setData(l); stuck_push();
    setKey(0);    setData(r); stuck_push();
   }

  void splitLeaf(int node, int parent, int index)                               // Split a leaf which is not the root
   {final int p = parent;                                                       // Parent
    final int l = allocLeaf();                                                  // New  split out leaf
    final int r = node;                                                         // Existing  leaf on right
    final int sl = splitLeafSize;                                               // Size of a split leaf

    for (int i = 0; i < sl; i++)                                                // Build left leaf
     {setStuck(r); stuck_shift();
      setStuck(l); stuck_push();
     }
    setStuck(r); stuck_firstElement(); final int F = getKey();
    setStuck(l); stuck_lastElement (); final int L = getKey();
    final int sk = (F + L) / 2;
    setStuck(p); setKey(sk); setData(l); setIndex(index);
    stuck_insertElementAt();                                                    // Insert new key, next pair in parent
   }

  void splitBranch(int node, int parent, int index)                             // Split a branch which is not the root by splitting right to left
   {final int p = parent;
    final int l = allocBranch();
    final int r = node;
    final int sb = splitBranchSize;

    for (int i = 0; i < sb; i++)                                                // Build left branch from right
     {setStuck(r); stuck_shift();
      setStuck(l); stuck_push();
     }

    setStuck(r); stuck_shift();                                                 // Build right branch
    final int sk = getKey();
    setStuck(l); setKey(0); stuck_push();                                       // Becomes top and so is ignored by search ... except last
    setStuck(p); setKey(sk); setData(l); setIndex(index);
    stuck_insertElementAt();
   }

  boolean stealFromLeft(int node, int index)                                    // Steal from the left sibling of the indicated child if possible to give to the right - Dennis Moore, Dennis Moore, Dennis Moore.
   {final int P = node;
    setStuck(P);
    if (index == 0) return false;
    setIndex(index-1); stuck_elementAt(); final int l = getData();
    setIndex(index-0); stuck_elementAt(); final int r = getData();

    if (hasLeavesForChildren(node))                                             // Children are leaves
     {final int nl = leafSize(l);
      final int nr = leafSize(r);

      if (nr >= maxKeysPerLeaf) return false;                                   // Steal not possible because there is no where to put the steal
      if (nl <= 1) return false;                                                // Steal not allowed because it would leave the leaf sibling empty

      setStuck(l); stuck_lastElement();
      setStuck(r); setIndex(0); stuck_insertElementAt();                        // Increase right
      setStuck(l); stuck_pop();                                                 // Reduce left
      setIndex(nl-2); stuck_elementAt();                                        // Last key on left
     }
    else                                                                        // Children are branches
     {final int nl = branchSize(l);
      final int nr = branchSize(r);

      if (nr >= maxKeysPerBranch) return false;                                 // Steal not possible because there is no where to put the steal
      if (nl <= 1) return false;                                                // Steal not allowed because it would leave the left sibling empty

      setStuck(l); stuck_lastElement(); final int td = getData();               // Increase right with left top
      setStuck(P); setIndex(index); stuck_elementAt();

      setStuck(r); setData(td); setIndex(0); stuck_insertElementAt();           // Increase right with left top
      setStuck(l); stuck_pop();                                                 // Remove left top

      setStuck(r); stuck_firstElement(); final int bd = getData();              // Increase right with left top
      setStuck(P); setIndex(index-1); stuck_elementAt();

      setStuck(r); setData(bd); setIndex(0); stuck_setElementAt();              // Reduce key of parent of right
      setStuck(l); stuck_lastElement();                                         // Last left key
     }
    setStuck(P); setData(l); setIndex(index-1); stuck_setElementAt();         // Reduce key of parent of left
    return true;
   }

  boolean stealFromRight(int node, int index)                                   // Steal from the right sibling of the indicated child if possible
   {final int P = node;
    if (index == branchSize(P)) return false;
    setStuck(P);
    setIndex(index+0); stuck_elementAt(); final int l = getData(), lk = getKey();
    setIndex(index+1); stuck_elementAt(); final int r = getData();

    if (hasLeavesForChildren(P))                                                // Children are leaves
     {final int nl = leafSize(l);
      final int nr = leafSize(r);

      if (nl >= maxKeysPerLeaf) return false;                                       // Steal not possible because there is no where to put the steal
      if (nr <= 1) return false;                                                    // Steal not allowed because it would leave the right sibling empty
     }
    else                                                                        // Children are branches
     {final int nl = branchSize(l);
      final int nr = branchSize(r);

      if (nl >= maxKeysPerBranch) return false;                                 // Steal not possible because there is no where to put the steal
      if (nr <= 1) return false;                                                // Steal not allowed because it would leave the right sibling empty

      setStuck(l); stuck_lastElement();                                         // Last element of left child
      setStuck(l); setKey(lk); setIndex(nl); stuck_setElementAt();              // Left top becomes real
     }

    setStuck(r); stuck_firstElement(); final int fk = getKey();                 // First element of right child
    setStuck(l); stuck_push();                                                  // Increase left
    setStuck(P); setKey(fk); setData(l); setIndex(index); stuck_setElementAt(); // Swap key of parent
    setStuck(r); stuck_shift();                                                 // Reduce right
    return true;
   }

//D2 Merge                                                                      // Merge two nodes together and free the resulting free node

  boolean mergeRoot()                                                           // Merge into the root
   {rootIsLeaf();
    if (L.get("rootIsLeaf") > 0 || branchSize(0) > 1) return false;

    int p = 0;
    setStuck(p);
    stuck_firstElement(); int l = getData();
    stuck_lastElement (); int r = getData();

    if (hasLeavesForChildren(p))                                                // Leaves
     {if (leafSize(l) + leafSize(r) <= maxKeysPerLeaf)
       {setStuck(p); stuck_clear();
        int nl = leafSize(l);
        for (int i = 0; i < nl; ++i)
         {setStuck(l); stuck_shift();
          setStuck(p); stuck_push();
         }
        int nr = leafSize(r);
        for (int i = 0; i < nr; ++i)
         {setStuck(r); stuck_shift();
          setStuck(p); stuck_push();
         }
        setLeaf(p);
        L.set(l, "free_1"); free();
        L.set(r, "free_1"); free();
        return true;
       }
     }
    else if (branchSize(l) + 1 + branchSize(r) <= maxKeysPerBranch)         // Branches
     {setStuck(p); stuck_firstElement(); final int pkn = getKey();
      stuck_clear();
      int nl = branchSize(l);
      for (int i = 0; i < nl; ++i)
       {setStuck(l); stuck_shift();
        setStuck(p); stuck_push();
       }
      setStuck(l); stuck_lastElement();
      setStuck(p); setKey(pkn); stuck_push();
      final int nr = branchSize(r);
      for (int i = 0; i < nr; ++i)
       {setStuck(r); stuck_shift();
        setStuck(p); stuck_push();
       }
      setStuck(r); stuck_lastElement();                                         // Top next
      setStuck(p); setKey(0); stuck_push();                                     // Top so ignored by search ... except last
      L.set(l, "free_1"); free();
      L.set(r, "free_1"); free();
      return true;
     }
    return false;
   }

  boolean mergeLeftSibling(int parent, int index)                               // Merge the left sibling
   {final int P = parent;
    if (index == 0) return false;
    int bs = branchSize(P);
    if (index >= bs) return false;

    setStuck(P);
    setIndex(index-1); stuck_elementAt(); final int l = getData();
    setIndex(index-0); stuck_elementAt(); final int r = getData();

    if (hasLeavesForChildren(P))                                                // Children are leaves
     {final int nl = leafSize(l);
      final int nr = leafSize(r);

      if (nl + nr >= maxKeysPerLeaf) return false;                              // Combined body would be too big

      setStuck(l); final int N = stuck_size();                                  // Number of entries to remove
      for (int i = 0; i < N; i++)                                               // Transfer left to right
       {setStuck(l); stuck_pop();
        setStuck(r); stuck_unshift();
       }
     }
    else                                                                        // Children are branches
     {final int nl = branchSize(l);
      final int nr = branchSize(r);

      if (nl + 1 + nr > maxKeysPerBranch) return false;                         // Merge not possible because there is not enough room for the combined result

      setStuck(P); setIndex(index-1); stuck_elementAt(); final int t = getKey();// Top key
      setStuck(l); stuck_lastElement();                                         // Last element of left child
      setStuck(r); setKey(t); stuck_unshift();                                  // Left top to right

      setStuck(l); stuck_pop();                                                 // Remove left top
      int N = branchSize(l)+1;                                                  // Number of entries to remove
      for (int i = 0; i < N; i++)                                               // Transfer left to right
       {setStuck(l); stuck_pop();
        setStuck(r); stuck_unshift();
       }
     }
    L.set(l, "free_1"); free();                                                 // Free the empty left node
    setStuck(P); setIndex(index-1); stuck_removeElementAt();                    // Reduce P on left
    return true;
   }

  boolean mergeRightSibling(int parent, int index)                              // Merge the right sibling
   {final int P = parent;
    setStuck(P);
    final int bs = stuck_size()-1;
    if (index >= bs) return false;                                              // No right sibling

    setStuck(parent);
    setIndex(index+0); stuck_elementAt(); final int l = getData();
    setIndex(index+1); stuck_elementAt(); final int r = getData();

    if (hasLeavesForChildren(P))                                                // Children are leaves
     {final int nl = leafSize(l);
      final int nr = leafSize(r);

      if (nl + nr > maxKeysPerLeaf) return false;                               // Combined body would be too big for one leaf

      setStuck(r); final int N = stuck_size();                                  // Number of entries to remove
      for (int i = 0; i < N; i++)                                               // Transfer right to left
       {setStuck(r); stuck_shift();
        setStuck(l); stuck_push();
       }
     }
    else                                                                        // Children are branches
     {final int nl = branchSize(l);
      final int nr = branchSize(r);

      if (nl + 1 + nr >  maxKeysPerBranch) return false;                        // Merge not possible because there is not enough room in a single branch
      setStuck(l); stuck_lastElement(); final int ld = getData();               // Last element of left child
      setStuck(parent); setIndex(index); stuck_elementAt();                     // Parent dividing element
      setStuck(l); setData(ld); setIndex(nl); stuck_setElementAt();             // Re-key left top

      for (int i = 0; i < nr+1; i++)                                            // Transfer right to left
       {setStuck(r); stuck_shift();
        setStuck(l); stuck_push();
       }
     }
    L.set(r, "free_1"); free();                                                // Free the empty right node

    setStuck(P);
    setIndex(index+1); stuck_elementAt(); final int pk = getKey();              // One up from dividing point in parent
    setIndex(index+0); stuck_elementAt();                                       // Dividing point in parent
    setKey(pk); setIndex(index); stuck_setElementAt();                          // Install key of right sibling in this child
    setIndex(index+1); stuck_removeElementAt();                                 // Reduce parent on right

    return true;
   }

//D2 Balance                                                                    // Balance the tree by merging and stealing

  void  balance(int parent, int index)                                          // Augment the indexed child so it has at least two children in its body
   {setStuck(parent); setIndex(index); stuck_elementAt();

    if (!isLow(getData())) return;
    if (stealFromLeft    (parent, index)) return;
    if (stealFromRight   (parent, index)) return;
    if (mergeLeftSibling (parent, index)) return;
    if (mergeRightSibling(parent, index)) return;
   }

//D1 Print                                                                      // Print a BTree horizontally

  void  padStrings(StringBuilder[]S)                                            // Pad the strings at each level of the tree so we have a vertical face to continue with - a bit like Marc Brunel's tunneling shield
   {int L = 0;
    for (int i = 0; i < linesPerTree; ++i)  L = max(L, S[i].length());          // Maximum advance so far
    L += gutter;                                                                // Gutter between printed items
    for (int i = 0; i < linesPerTree; ++i)                                      // Pad all strings to maximum advance
      {S[i].append(" ".repeat(L-S[i].length()));
     }
   }

  void  printLeaf(int node, StringBuilder[]S, int level)                        // Print a leaf
   {final StringBuilder s = S[level];
    setStuck(node); final int L = stuck_size();
    for (int i = 0; i < L; i++)                                                 // Each element in the leaf
     {setStuck(node); setIndex(i); stuck_elementAt();
      s.append(String.format("%d ", getKey()));
     }
    if (s.length() > 0) s.setLength(s.length()-1);
    s.append(String.format("=%d ", node));
    padStrings(S);
   }

  void  printBranch(int node, StringBuilder[]S, int level)                      // Print a branch
   {setStuck(node);
    final int L = stuck_size() - 1;
    for (int i = 0; i < L; i++)                                                 // Each element in the branch
     {setStuck(node); setIndex(i); stuck_elementAt();
      final int k = getKey(), d = getData();
      if (isLeaf(d)) printLeaf(d, S, level+linesPerNode);
      else         printBranch(d, S, level+linesPerNode);
      S[level+0].append(String.format("%d", k));
      S[level+1].append(String.format("%d", d));
      S[level+2].append(String.format("%d", node));
      S[level+3].append(String.format("%d", i));
      padStrings(S);
     }
    setStuck(node); setIndex(L); stuck_elementAt();
    final int k = getKey(), d = getData();
    if (isLeaf(d)) printLeaf(d, S, level+linesPerNode);
    else         printBranch(d, S, level+linesPerNode);
    S[level+0].append("+");
    S[level+1].append(String.format("%d", d));
    S[level+2].append(String.format("%d", node));
    S[level+3].append(String.format("%d", L));
   }

  String printCollapsed(StringBuilder[]S)                                       // Collapse horizontal representation into a string
   {int N = 0;
    for (int i = 0; i < linesPerTree; ++i)                                      // Remove trailing blanks
     {final StringBuilder s = S[i];
      while (s.length() > 0 && Character.isWhitespace(s.charAt(s.length() - 1))) s.setLength(s.length() - 1);
      N += s.length()+1;
     }
    final StringBuilder t = new StringBuilder();                                // Concatenate each line
    for (int i = 0; i < linesPerTree; ++i)                                      // Concatenate line representing the tree
     {final StringBuilder s = S[i];
      if (s.length() == 0) continue;
      t.append(s+"\n");
     }
    return ""+t;                                                                // Printed tree
   }

  public String toString()                                                      // Dump a tree horizontally
   {L.save();
    final int N = linesPerTree*linesPerNode;                                    // A big buffer with room for several lines per node
    final StringBuilder [] S = new StringBuilder[N];                            // A big buffer with room for several lines per node
    for (int i = 0; i < N; i++) S[i] = new StringBuilder();
    rootIsLeaf();
    if (L.get("rootIsLeaf") > 0) printLeaf(0, S, 0); else printBranch(0, S, 0); // Print tree
    L.restore();
    return printCollapsed(S);                                                   // Collapse lines into text
   }

  void print(String title)                                                      // Print the tree
   {say("Tree: %s", title);
    say(this);
   }

//D1 Find                                                                       // Find the data associated with a key

  void find_result(int Leaf, int Found, int Index, int Key, int Data)           // Find result
   {putFLeaf(Leaf);
    putFound(Found);
    putFIndex(Index);
    putFKey(Key);
    putFData(Data);
   }

  void findAndInsert_result(int Success, int Inserted)                               // Find and insert result
   {putFSuccess(Success);
    putFInserted(Inserted);
   }

  String print_find_result()                                                    // Print result
   {return String.format("Find_Result leaf=%d,found=%d,index=%d,key=%d,data=%d,success=%d,inserted=%d",
      getFLeaf(), getFound(), getFIndex(), getFKey(), getFData(), getFSuccess(), getFInserted());
   }

  void find(int Key)                                                            // Find the data associated with a key in the tree
   {rootIsLeaf();                                                               // The root is a leaf
    if (L.get("rootIsLeaf") > 0)
     {setStuck(0); setKey(Key); stuck_search();
      find_result(0, L.get("s_found"), getIndex(), Key, getData());
      return;
     }

    int p = 0;                                                             // Parent starts at root which is known to be a branch

    for (int i = 0; i < maxDepth; i++)                                          // Step down through tree
     {setStuck(p); setKey(Key);
      stuck_searchFirstGreaterThanOrEqualExceptLast();
      final int q = getData();

      if (isLeaf(q))                                                            // Found the containing search
       {setStuck(q);  setKey(Key); stuck_search();

        find_result(q, L.get("s_found"), getIndex(), Key, getData());
        return;
       }
      p = q;                                                                    // Step down to lower branch
     }
    stop("Search did not terminate in a leaf");
   }

  void findAndInsert(int Key, int Data)                                         // Find the leaf that should contain this key and insert or update it is possible
   {find(Key);                                                                  // Find the leaf that should contain this key

    if (getFound() > 0)                                                         // Found the key in the leaf so update it with the new data
     {setStuck(getFLeaf()); setKey(Key); setData(Data); setIndex(getFIndex());
      stuck_setElementAt();
      findAndInsert_result(1, 0);
      return;
     }

    if (!isFull(getFLeaf()))                                                    // Leaf is not full so we can insert immediately
     {setStuck(getFLeaf()); setKey(Key);
      stuck_searchFirstGreaterThanOrEqual();
//      if (getFound() > 0)                                                       // Overwrite existing key
       {setStuck(getFLeaf());
        setKey(Key); setData(Data); //setIndex(getFIndex());
        stuck_insertElementAt();
        findAndInsert_result(1, 0);
       }
//    else                                                                      // Insert into position
//     {setStuck(getFLeaf());
//      setKey(Key); setData(Data);
//      stuck_push();
//     }
//    findAndInsert_result(1, 1);                                               // Record result of insertion
      return;
     }
    findAndInsert_result(0, 0);
    return;
   }

  //D1 Insertion                                                                // Insert a key, data pair into the tree or update and existing key with a new datum

  void put(int Key, int Data)                                                   // Insert a key, data pair into the tree or update and existing key with a new datum
   {findAndInsert(Key, Data);                                                   // Try direct insertion with no modifications to the shape of the tree
    if (getFSuccess() > 0) return;                                              // Inserted or updated successfully

    if (isFullRoot())                                                           // Start the insertion at the root, after splitting it if necessary
     {rootIsLeaf();
      if (L.get("rootIsLeaf") > 0)
       {splitLeafRoot();
       }
      else
       {splitBranchRoot();
       }

      findAndInsert(Key, Data);                                                 // Splitting the root might have been enough
      if (getFSuccess() > 0) return;                                            // Inserted or updated successfully
     }

    int p = 0;                                                                  // Step down the tree from the root

    for (int i = 0; i < maxDepth; i++)                                          // Step down from branch to branch through the tree until reaching a leaf repacking as we go
     {setStuck(p); setKey(Key);
      stuck_searchFirstGreaterThanOrEqualExceptLast();
      final int q = getData();

      if (isLeaf(q))                                                            // Reached a leaf
       {splitLeaf(q, p, getIndex());
        findAndInsert(Key, Data);
        merge(Key);
        return;
       }

      if (isFull(q))
       {splitBranch(q, p, getIndex());                                         // Split the child branch in the search path for the key from the parent so the the search path does not contain a full branch above the containing leaf
        setStuck(p); setKey(Key);
        stuck_searchFirstGreaterThanOrEqualExceptLast();
        p = getData();
       }
      else
       {p = q;
       }
     }
    stop("Fallen off the end of the tree");                                     // The tree must be missing a leaf
   }

//D1 Deletion                                                                   // Delete a key, data pair from the tree

  void delete_result(int Found, int Data)                                       // Delete and insert result
   {putFound(Found);
    putFData(Data);
   }

  void findAndDelete(int Key)                                                   // Delete a key from the tree and returns its data if present without modifying the shape of tree
   {find(Key);                                                                  // Find the key
    if (getFound() == 0)                                                        // No such key
     {findAndInsert_result(0, 0);
      return;
     }
    final int l = getFLeaf();                                                   // The leaf that contains the key
    final int i = getFIndex();                                                  // Position in the leaf of the key
    setStuck(l); setIndex(i);                                                   // Key, data pairs in the leaf
    stuck_removeElementAt();                                                    // Remove the key, data pair from the leaf
    delete_result(1, getData());                                                // Delete result
    return;
   }

  void delete(int Key)                                                          // Insert a key, data pair into the tree or update and existing key with a new datum
   {mergeRoot();

    rootIsLeaf();                                                               // Find and delete directly in root as a leaf
    if (L.get("rootIsLeaf") > 0)
     {findAndDelete(Key);
      return;
     }

    int p = 0;                                                                  // Start at root

    for (int i = 0; i < maxDepth; i++)                                          // Step down from branch to branch through the tree until reaching a leaf repacking as we go
     {setStuck(p); setKey(Key);
      stuck_searchFirstGreaterThanOrEqualExceptLast();
      balance(p, getIndex());                                                   // Make sure there are enough entries in the parent to permit a deletion

      setStuck(p); setKey(Key);
      stuck_searchFirstGreaterThanOrEqualExceptLast();

      final int q = getData();
      if (isLeaf(q))                                                            // Reached a leaf
       {findAndDelete(Key);
        merge(Key);
        return;
       }
      p = q;
     }
    stop("Fallen off the end of the tree");                                     // The tree must be missing a leaf
   }

//D1 Merge                                                                      // Merge along the specified search path

  void merge(int Key)                                                           // Merge along the specified search path
   {mergeRoot();
    int p = 0;                                                                  // Start at root

    for (int i = 0; i < maxDepth; i++)                                          // Step down from branch to branch through the tree until reaching a leaf repacking as we go
     {if (isLeaf(p)) return;

      for (int j = 0; j < branchSize(p)+1; j++)                                   // Try merging each sibling pair which might change the size of the parent
       {if (mergeLeftSibling(p, j)) --j;                                        // A successful merge of the left  sibling reduces the current index and the upper limit
        mergeRightSibling(p, j);                                                // A successful merge of the right sibling maintains the current position but reduces the upper limit
       }

      setStuck(p); setKey(Key);
      stuck_searchFirstGreaterThanOrEqualExceptLast();
      p = getData();
     }
    stop("Fallen off the end of the tree");                                     // The tree must be missing a leaf
   }

//D1 Stucks                                                                     // Store data in each node

  int stuck_size       () {return L.get("current_size", "stuck");}              // The current number of key elements in a stuck
  int stuck_size1      () {return stuck_size() - 1;}                            // The current number of key elements in a stuck minus one whichmakes it suitable for describing a branch
  boolean stuck_isEmpty() {return stuck_size() == 0;}                           // Check the stuck is empty
  boolean leaf_isFull  () {return stuck_size() > maxKeysPerLeaf;}               // Check the leaf stuck is full
  boolean branch_isFull() {return stuck_size() > maxKeysPerBranch;}             // Check the branch stuck is full

  void stuck_key () {L.set(L.get("keys", "stuck", "s_index"), "s_key" );}       // Key from a stuck at indicated index
  void stuck_data() {L.set(L.get("data", "stuck", "s_index"), "s_data");}       // Data from a stuck at indicated index

  void stuck_setKey () {L.set(L.get("s_key" ), "keys", "stuck", "s_index");}    // Save a key  in a stuck at the specified index
  void stuck_setData() {L.set(L.get("s_data"), "data", "stuck", "s_index");}    // Save a data in a stuck at the specified index

  void stuck_copyKey  (int T, int S) {L.move("keys", "keys", "stuck", ""+T, "stuck", ""+S);}
  void stuck_copyData (int T, int S) {L.move("data", "data", "stuck", ""+T, "stuck", ""+S);}

  void  stuck_setKeyData()                                                      // Set key, data pair in a stuck
   {stuck_setKey  ();
    stuck_setData ();
   }
                                                                                // Copy key, data pair in a stuck
  void stuck_copyKeyData(int Target, int Source)
   {stuck_copyKey (Target, Source);
    stuck_copyData(Target, Source);
   }

  void stuck_inc  () {L.addImmediate(+1, "current_size", "stuck");}             // Increment the current size
  void stuck_dec  () {L.addImmediate(-1, "current_size", "stuck");}             // Decrement the current size
  void stuck_clear() {L.set( 0, "current_size", "stuck");}                      // Clear the stuck

  void stuck_push()                                                             // Push an element onto a stuck
   {L.set(stuck_size(), "s_index");
    stuck_setKeyData();
    stuck_inc();
   }

  void stuck_unshift()                                                          // Unshift an element onto a stuck
   {for (int i = stuck_size(); i > 0; --i) stuck_copyKeyData(i, i-1);           // Shift the stuck up one place
    L.set(0, "s_index");
    stuck_setKeyData();
    stuck_inc();
   }

  void stuck_pop()                                                              // Pop from a stuck
   {stuck_dec();
    final int s = stuck_size();
    L.set(s, "s_index");
    stuck_elementAt();
   }

  void stuck_shift()                                                            // Shift off the first element
   {L.set(0, "s_index");
    stuck_elementAt();
    for (int i = 0, j = stuck_size()-1; i < j; i++) stuck_copyKeyData(i, i+1);
    stuck_dec();
   }

  void stuck_elementAt()                                                        // Element at specified index
   {stuck_key ();
    stuck_data();
   }

  void stuck_setElementAt()                                                     // Set an element either in range or one above the current range
   {final int size = stuck_size();                                              // Size of stuck
    final int indx = L.get("s_index");                                          // Set index
    stuck_setKeyData();                                                         // Set key and data
    if (indx == size) stuck_inc();                                              // Extend range if necessary
   }

  void stuck_insertElementAt()                                                  // Insert an element at the indicated location shifting all the remaining elements up one
   {final int size = stuck_size();                                              // Size of stuck
    final int indx = L.get("s_index");                                          // Set index
    for (int i = size; i > indx; --i) stuck_copyKeyData(i, i-1);
    stuck_setKeyData();
    stuck_inc();
   }

  void stuck_removeElementAt()                                                  // Remove the indicated element
   {final int size = stuck_size();                                              // Size of stuck
    final int indx = L.get("s_index");                                          // Set index
    stuck_elementAt();
    for (int i = indx, j = size-1; i < j; i++) stuck_copyKeyData(i, i+1);       // Shift the stuck down one place
    stuck_dec();
   }

  void stuck_firstElement()                                                     // First element
   {L.set(0, "s_index");
    stuck_elementAt();
   }

  void stuck_lastElement()                                                      // Last element
   {final int size = stuck_size();                                              // Size of stuck
    L.set(size-1, "s_index");
    stuck_elementAt();
   }

//D1 Search                                                                     // Search a stuck.

  void stuck_search()                                                           // Search for an element within all elements of the stuck
   {for (int i = 0, j = stuck_size(); i < j; i++)                                // Search
     {L.set(i, "s_index");
      final int c = L.compare("s_key", "keys", "stuck", "s_index");
      if (c == 0)
       {L.set(1, "s_found");
        stuck_elementAt();
        return;
       }
     }
    L.set(0, "s_found");
   }

  void stuck_searchFirstGreaterThanOrEqual()                                    // Find first key equal or greater than the search key
   {final int s = stuck_size();
    for (int i = 0, j = s; i < j; i++)
     {L.set(i, "s_index");
      final int c = L.compare("s_key", "keys", "stuck", "s_index");
      if (c <= 0)
       {L.set(1, "s_found");
        stuck_elementAt();
        return;
       }
     }
    L.set(s, "s_index");
    L.set(0, "s_found");
   }

  void stuck_searchFirstGreaterThanOrEqualExceptLast()                          // Find first key equal or greater than the search key
   {final int s = stuck_size()-1;
    for (int i = 0; i < s; i++)
     {L.set(i, "s_index");
      final int c = L.compare("s_key", "keys", "stuck", "s_index");
      if (c <= 0)
       {L.set(1, "s_found");
        stuck_elementAt();
        return;
       }
     }
    L.set(s, "s_index");
    stuck_elementAt();
    L.set(0, "s_found");
   }

// Tests

//D1 Print                                                                      // Print a stuck

  String stuck_print()                                                          // Print a stuck
   {final StringBuilder s = new StringBuilder();
    int N = stuck_size();
    s.append(String.format("Stuck(size:%d)\n", N));
    for (int i = 0; i < N; i++)                                                 // Search
     {L.set(i, "s_index");
      stuck_elementAt();
      s.append(String.format("  %2d key: %2d data: %2d\n", i, L.get("s_key"),   L.get("s_data")));
     }
    return ""+s;
   }

  String stuck_print_result()                                                   // Print the result of a stuck operation
   {final StringBuilder s = new StringBuilder();
    s.append(String.format(" found: %d\n", L.get("s_found")));
    s.append(String.format(" index: %d\n", L.get("s_index")));
    s.append(String.format("   key: %d\n", L.get("s_key")));
    s.append(String.format("  data: %d\n", L.get("s_data")));;
    return ""+s;
   }

//D1 Tests                                                                      // Tests

//D2 Stuck                                                                      // Test stuck

  static BtreeBam stuck_test_load()
   {final BtreeBam b = new BtreeBam(3, 3, 1);
    b.L.set("s_index", 0);
    b.L.set("current_size", 1);
    b.L.set("keys", 1, 2, 3);
    b.L.set("data", 11, 22, 33, 44);

    ok(b.stuck_print(), """
Stuck(size:1)
   0 key:  1 data: 11
""");
    return b;
   }

  static BtreeBam stuck_test_push()
   {final BtreeBam b = stuck_test_load();

    b.L.set(2, "s_key"); b.L.set(22, "s_data");
    b.stuck_push();

    ok(b.stuck_print(), """
Stuck(size:2)
   0 key:  1 data: 11
   1 key:  2 data: 22
""");
    return b;
   }

  static void stuck_test_pop()
   {final BtreeBam b = stuck_test_push();
    b.stuck_pop();

    ok(b.stuck_print_result(), """
 found: 0
 index: 1
   key: 2
  data: 22
""");

    ok(b.stuck_print(), """
Stuck(size:1)
   0 key:  1 data: 11
""");
   }

  static void stuck_test_shift()
   {final BtreeBam b = stuck_test_push();
    b.stuck_shift();

    ok(b.stuck_print_result(), """
 found: 0
 index: 0
   key: 1
  data: 11
""");

    ok(b.stuck_print(), """
Stuck(size:1)
   0 key:  2 data: 22
""");
   }

  static BtreeBam stuck_test_unshift()
   {final BtreeBam b = stuck_test_push();

    b.L.set(3, "s_key"); b.L.set(33, "s_data");
    b.stuck_unshift();

    ok(b.stuck_print_result(), """
 found: 0
 index: 0
   key: 3
  data: 33
""");

    ok(b.stuck_print(), """
Stuck(size:3)
   0 key:  3 data: 33
   1 key:  1 data: 11
   2 key:  2 data: 22
""");
    return b;
   }

  static void stuck_test_elementAt()
   {final BtreeBam b = stuck_test_unshift();

    b.L.set(1, "s_index");
    b.stuck_elementAt();

    ok(b.stuck_print_result(), """
 found: 0
 index: 1
   key: 1
  data: 11
""");
   }

  static BtreeBam stuck_test_insertElementAt()
   {final BtreeBam b = stuck_test_push();
    b.L.set(1, "s_index"); b.L.set(3, "s_key"); b.L.set(33, "s_data");
    b.stuck_insertElementAt();

    ok(b.stuck_print_result(), """
 found: 0
 index: 1
   key: 3
  data: 33
""");

    //stop(b.stuck_print());
    ok(b.stuck_print(), """
Stuck(size:3)
   0 key:  1 data: 11
   1 key:  3 data: 33
   2 key:  2 data: 22
""");
    return b;
   }

  static BtreeBam stuck_test_remove_element_at()
   {final BtreeBam b = stuck_test_insertElementAt();
    b.L.set(1, "s_index");
    b.stuck_removeElementAt();

    ok(b.stuck_print_result(), """
 found: 0
 index: 1
   key: 3
  data: 33
""");

    //stop(b.stuck_print());
    ok(b.stuck_print(), """
Stuck(size:2)
   0 key:  1 data: 11
   1 key:  2 data: 22
""");
    return b;
   }

  static BtreeBam stuck_test_first_last()
   {final BtreeBam b = stuck_test_insertElementAt();

    b.stuck_firstElement();
    ok(b.stuck_print_result(), """
 found: 0
 index: 0
   key: 1
  data: 11
""");

    b.stuck_lastElement();
    ok(b.stuck_print_result(), """
 found: 0
 index: 2
   key: 2
  data: 22
""");
    return b;
   }

  static BtreeBam stuck_test_search()
   {final BtreeBam b = stuck_test_push();

    b.L.set(3, "s_key"); b.L.set(33, "s_data");
    b.stuck_push();

    b.L.set(2, "s_key");
    b.stuck_search();
    ok(b.stuck_print_result(), """
 found: 1
 index: 1
   key: 2
  data: 22
""");

    b.L.set(4, "s_key");
    b.stuck_search();
    ok(b.stuck_print_result(), """
 found: 0
 index: 2
   key: 4
  data: 22
""");
    return b;
   }

  static BtreeBam stuck_test_search_greater_than_or_equal()
   {final BtreeBam b = new BtreeBam(3, 3, 1);
    b.L.set("s_index", 0);
    b.L.set("current_size", 3);
    b.L.set("keys", 2, 4, 6);
    b.L.set("data", 1, 3, 5, 7);

    ok(b.stuck_print(), """
Stuck(size:3)
   0 key:  2 data:  1
   1 key:  4 data:  3
   2 key:  6 data:  5
""");

    b.L.set(3, "s_key");
    b.stuck_searchFirstGreaterThanOrEqual();
    ok(b.stuck_print_result(), """
 found: 1
 index: 1
   key: 4
  data: 3
""");

    b.L.set(4, "s_key");
    b.stuck_searchFirstGreaterThanOrEqual();
    ok(b.stuck_print_result(), """
 found: 1
 index: 1
   key: 4
  data: 3
""");

    b.L.set(5, "s_key");
debug = true;
    b.stuck_searchFirstGreaterThanOrEqual();
    ok(b.stuck_print_result(), """
 found: 1
 index: 2
   key: 6
  data: 5
""");

    b.L.set(6, "s_key");
debug = true;
    b.stuck_searchFirstGreaterThanOrEqual();
    ok(b.stuck_print_result(), """
 found: 1
 index: 2
   key: 6
  data: 5
""");

    b.L.set(7, "s_key");
debug = true;
    b.stuck_searchFirstGreaterThanOrEqual();
    ok(b.stuck_print_result(), """
 found: 0
 index: 3
   key: 7
  data: 5
""");
    return b;
   }

  static BtreeBam stuck_test_search_greater_than_or_equal_except_last()
   {final BtreeBam b = stuck_test_search_greater_than_or_equal();

    ok(b.stuck_print(), """
Stuck(size:3)
   0 key:  2 data:  1
   1 key:  4 data:  3
   2 key:  6 data:  5
""");

    b.L.set(7, "s_key");
    b.stuck_searchFirstGreaterThanOrEqualExceptLast();
    ok(b.stuck_print_result(), """
 found: 0
 index: 2
   key: 6
  data: 5
""");
    return b;
   }

  static void test_put_ascending()
   {final BtreeBam b = new BtreeBam(2, 3, 29);
    int N = 32;
    for (int i = 1; i <= N; i++) b.put(i, i);
    //stop(b.print());
    ok(b, """
                                                                                                  16                                                                                                                     +
                                                                                                  17                                                                                                                     21
                                                                                                  0                                                                                                                      0
                                                                                                  0                                                                                                                      1
                                         8                                                        +                                                           24                            28                           +
                                         5                                                        11                                                          16                            23                           6
                                         17                                                       17                                                          21                            21                           21
                                         0                                                        1                                                           0                             1                            2
        2          4          6          +            10             12            14             +              18             20             22             +              26             +              30            +
        1          3          4          7            8              10            9              12             13             15             14             19             18             22             20            2
        5          5          5          5            11             11            11             11             16             16             16             16             23             23             6             6
        0          1          2          3            0              1             2              3              0              1              2              3              0              1              0             1
1 2=1      3 4=3      5 6=4      7 8=7       9 10=8       11 12=10       13 14=9       15 16=12       17 18=13       19 20=15       21 22=14       23 24=19       25 26=18       27 28=22       29 30=20       31 32=2
""");
   }

  static void test_put_descending()
   {final BtreeBam b = new BtreeBam(2, 3, 29);
    int N = 32;
    for (int i = N; i > 0; i--) b.put(i, i);
    //stop(b.print());
    ok(b, """
                                                                                                           16                                                                                                               +
                                                                                                           9                                                                                                                22
                                                                                                           0                                                                                                                0
                                                                                                           0                                                                                                                1
                      4                         8                                                          +                                                        24                                                      +
                      23                        21                                                         16                                                       11                                                      5
                      9                         9                                                          9                                                        22                                                      22
                      0                         1                                                          2                                                        0                                                       1
         2            +            6            +             10             12             14             +             18             20            22            +             26            28            30            +
         24           17           20           18            19             15             13             12            6              10            8             7             4             3             2             1
         23           23           21           21            16             16             16             16            11             11            11            11            5             5             5             5
         0            1            0            1             0              1              2              3             0              1             2             3             0             1             2             3
1 2=24       3 4=17       5 6=20       7 8=18       9 10=19       11 12=15       13 14=13       15 16=12       17 18=6       19 20=10       21 22=8       23 24=7       25 26=4       27 28=3       29 30=2       31 32=1
""");
   }

  final static int[]random_small = {27, 442, 545, 317, 511, 578, 391, 993, 858, 586, 472, 906, 658, 704, 882, 246, 261, 501, 354, 903, 854, 279, 526, 686, 987, 403, 401, 989, 650, 576, 436, 560, 806, 554, 422, 298, 425, 912, 503, 611, 135, 447, 344, 338, 39, 804, 976, 186, 234, 106, 667, 494, 690, 480, 288, 151, 773, 769, 260, 809, 438, 237, 516, 29, 376, 72, 946, 103, 961, 55, 358, 232, 229, 90, 155, 657, 681, 43, 907, 564, 377, 615, 612, 157, 922, 272, 490, 679, 830, 839, 437, 826, 577, 937, 884, 13, 96, 273, 1, 188};
  final static int[]random_large = {5918,5624,2514,4291,1791,5109,7993,60,1345,2705,5849,1034,2085,4208,4590,7740,9367,6582,4178,5578,1120,378,7120,8646,5112,4903,1482,8005,3801,5439,4534,9524,6111,204,5459,248,4284,8037,5369,7334,3384,5193,2847,1660,5605,7371,3430,1786,1216,4282,2146,1969,7236,2187,136,2726,9480,5,4515,6082,969,5017,7809,9321,3826,9179,5781,3351,4819,4545,8607,4146,6682,1043,2890,2964,7472,9405,4348,8333,2915,9674,7225,4743,995,1321,3885,6061,9958,3901,4710,4185,4776,5070,8892,8506,6988,2317,9342,3764,9859,4724,5195,673,359,9740,2089,9942,3749,9208,1,7446,7023,5496,4206,3272,3527,8593,809,3149,4173,9605,9021,5120,5265,7121,8667,6911,4717,2535,2743,1289,1494,3788,6380,9366,2732,1501,8543,8013,5612,2393,7041,3350,3204,288,7213,1741,1238,9830,6722,4687,6758,8067,4443,5013,5374,6986,282,6762,192,340,5075,6970,7723,5913,1060,1641,1495,5738,1618,157,6891,173,7535,4952,9166,8950,8680,1974,5466,2383,3387,3392,2188,3140,6806,3131,6237,6249,7952,1114,9017,4285,7193,3191,3763,9087,7284,9170,6116,3717,6695,6538,6165,6449,8960,2897,6814,3283,6600,6151,4624,3992,5860,9557,1884,5585,2966,1061,6414,2431,9543,6654,7417,2617,878,8848,8241,3790,3370,8768,1694,9875,9882,8802,7072,3772,2689,5301,7921,7774,1614,494,2338,8638,4161,4523,5709,4305,17,9626,843,9284,3492,7755,5525,4423,9718,2237,7401,2686,8751,1585,5919,9444,3271,1490,7004,5980,3904,370,5930,6304,7737,93,5941,9079,4968,9266,262,2766,4999,2450,9518,5137,8405,483,8840,2231,700,8049,8823,9811,9378,3811,8074,153,1940,1998,4354,7830,7086,6132,9967,5680,448,1976,4101,7839,3122,4379,9296,4881,1246,4334,9457,5401,1945,9548,8290,1184,3464,132,2458,7704,1056,7554,6203,2270,6070,4889,7369,1676,485,3648,357,1912,9661,4246,1576,1836,4521,7667,6907,2098,8825,7404,4019,8284,3710,7202,7050,9870,3348,3624,9224,6601,7897,6288,3713,932,5596,353,2615,3273,833,1446,8624,2489,3872,486,1091,2493,4157,3611,6570,7107,9153,4543,9504,4746,1342,9737,3247,8984,3640,5698,7814,307,8775,1150,4330,3059,5784,2370,5248,4806,6107,9700,231,3566,5627,3957,5317,5415,8119,2588,9440,2961,9786,4769,466,5411,3080,7623,5031,2378,9286,4801,797,1527,2325,847,6341,5310,1926,9481,2115,2165,5255,5465,5561,3606,7673,7443,7243,8447,2348,7925,6447,8311,6729,4441,7763,8107,267,8135,9194,6775,3883,9639,612,5024,1351,7557,9241,5181,2239,8002,5446,747,166,325,9925,3820,9531,5163,3545,558,7103,7658,5670,8323,4821,6263,7982,59,3700,1082,4474,4353,8637,9558,5191,842,5925,6455,4092,9929,9961,290,3523,6290,7787,8266,7986,7269,6408,3620,406,5964,7289,1620,6726,1257,1993,7006,5545,2913,5093,5066,3019,7081,6760,6779,7061,9051,8852,8118,2340,6596,4594,9708,8430,8659,8920,9268,5431,9203,2823,1427,2203,6422,6193,5214,9566,8791,4964,7575,4350,56,2227,8545,5646,3089,2204,4081,487,8496,2258,4336,6955,3452,556,8602,8251,8569,8636,9430,1025,9459,7137,8392,3553,5945,9414,3078,1688,5480,327,8117,2289,2195,8564,9423,103,7724,3091,8548,7298,5279,6042,2855,3286,3542,9361,420,7020,4112,5320,5366,6379,114,9174,9744,592,5346,3985,3174,5157,9890,1605,3082,8099,4346,7256,8670,5687,6613,6620,1458,1045,7917,2980,2399,1433,3315,4084,178,7056,2132,2728,4421,9195,4181,6017,6229,2945,4627,2809,8816,6737,18,8981,3813,8890,5304,3789,6959,7476,1856,4197,6944,9578,5915,3060,9932,3463,67,7393,9857,5822,3187,501,653,8453,3691,9736,6845,1365,9645,4120,2157,8471,4436,6435,2758,7591,9805,7142,7612,4891,7342,5764,8683,8365,2967,6947,441,2116,6612,1399,7585,972,6548,5481,7733,7209,222,5903,6161,9172,9628,7348,1588,5992,6094,7176,4214,8702,2987,74,8486,9788,7164,5788,8535,8422,6826,1800,8965,4965,565,5609,4686,2556,9324,5000,9809,1994,4737,63,8992,4783,2536,4462,8868,6346,5553,3980,2670,1601,4272,8725,4698,7333,7826,9233,4198,1997,1687,4851,62,7893,8149,8015,341,2230,1280,5559,9756,3761,7834,6805,9287,4622,5748,2320,1958,9129,9649,1644,4323,5096,9490,7529,6444,7478,7044,9525,7713,234,7553,9099,9885,7135,6493,9793,6268,8363,2267,9157,9451,1438,9292,1637,3739,695,1090,4731,4549,5171,5975,7347,5192,5243,1084,2216,9860,3318,5594,5790,1107,220,9397,3378,1353,4498,6497,5442,7929,7377,9541,9871,9895,6742,9146,9409,292,6278,50,5288,2217,4923,6790,4730,9240,3006,3547,9347,7863,4275,3287,2673,7485,1915,9837,2931,3918,635,9131,1197,6250,3853,4303,790,5548,9993,3702,2446,3862,9652,4432,973,41,3507,8585,2444,1633,956,5789,1523,8657,4869,8580,8474,7093,7812,2549,7363,9315,6731,1130,7645,7018,7852,362,1636,2905,8006,4040,6643,8052,7021,3665,8383,715,1876,2783,3065,604,4566,8761,7911,1983,3836,5547,8495,8144,1950,2537,8575,640,8730,8303,1454,8165,6647,4762,909,9449,8640,9253,7293,8767,3004,4623,6862,8994,2520,1215,6299,8414,2576,6148,1510,313,3693,9843,8757,5774,8871,8061,8832,5573,5275,9452,1248,228,9749,2730};

  static void test_put_random_small()
   {final BtreeBam b = new BtreeBam(2, 3, 100);
    int N = random_small.length;
    for (int i = 0; i < N; ++i) b.put(random_small[i], i);
    //stop(b.print());
    ok(b, """
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                493                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               +
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                6                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 60
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                0                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 0
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                0                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 1
                                                                                                                                                      190                                                                                                                                                                                     379                                                                                                                               +                                                                                                                                                                                622                                                                                                                                                                                                    882                                                                                                                       +
                                                                                                                                                      44                                                                                                                                                                                      61                                                                                                                                32                                                                                                                                                                               72                                                                                                                                                                                                     69                                                                                                                        2
                                                                                                                                                      6                                                                                                                                                                                       6                                                                                                                                 6                                                                                                                                                                                60                                                                                                                                                                                                     60                                                                                                                        60
                                                                                                                                                      0                                                                                                                                                                                       1                                                                                                                                 2                                                                                                                                                                                0                                                                                                                                                                                                      1                                                                                                                         2
                         33                         55                                          120                                                   +                                                 253                                               281                            335                                                  +                              416                                               439                                              +                                                               528                            561                                               598                             +                                                               681                                              785                                               830                                 +                                                                       949                                               +
                         88                         63                                          9                                                     42                                                65                                                38                             12                                                   35                             80                                                13                                               30                                                              48                             73                                                82                              16                                                              52                                               10                                                79                                  24                                                                      37                                                39
                         44                         44                                          44                                                    44                                                61                                                61                             61                                                   61                             32                                                32                                               32                                                              72                             72                                                72                              72                                                              69                                               69                                                69                                  69                                                                      2                                                 2
                         0                          1                                           2                                                     3                                                 0                                                 1                              2                                                    3                              0                                                 1                                                2                                                               0                              1                                                 2                               3                                                               0                                                1                                                 2                                   3                                                                       0                                                 1
          20             +              47          +           72             96               +                 153               160               +                 233           235               +                 270               275           +                 307          +                 349               365              +             397              +                 429               437           +                457           476               +                 502           506               518           +             552              +             570               577               +                 613           +             654               662           674               +                 695           738              +                 807           819               +                 856               +                 904               909               929               +                 981           988               +
          89             85             56          62          59             86               67                68                43                41                34            66                47                54                87            14                50           3                 36                64               1             23               8                 15                81            31               5             78                28                49            21                57            17            29               4             84                74                27                75            11            45                70            77                22                46            53               7                 55            83                26                20                19                71                33                76                18                51            40                25
          88             88             63          63          9              9                9                 42                42                42                65            65                65                38                38            38                12           12                35                35               35            80               80                13                13            13               30            30                30                48            48                48            48            73               73            82                82                82                16            16            52                52            52                52                10            10               10                79            79                79                24                24                37                37                37                37                39            39                39
          0              1              0           1           0              1                2                 0                 1                 2                 0             1                 2                 0                 1             2                 0            1                 0                 1                2             0                1                 0                 1             2                0             1                 2                 0             1                 2             3             0                1             0                 1                 2                 0             1             0                 1             2                 3                 0             1                2                 0             1                 2                 0                 1                 0                 1                 2                 3                 0             1                 2
1 13=89       27 29=85       39 43=56       55=62       72=59       90 96=86       103 106=67        135 151=68        155 157=43        186 188=41        229 232=34        234=66        237 246=47        260 261=54        272 273=87        279=14        288 298=50        317=3        338 344=36        354 358=64        376 377=1        391=23        401 403=8        422 425=15        436 437=81        438=31        442 447=5        472=78        480 490=28        494 501=49        503=21        511 516=57        526=17        545=29        554 560=4        564=84        576 577=74        578 586=27        611 612=75        615=11        650=45        657 658=70        667=77        679 681=22        686 690=46        704=53        769 773=7        804 806=55        809=83        826 830=26        839 854=20        858 882=19        884 903=71        906 907=33        912 922=76        937 946=18        961 976=51        987=40        989 993=25
""");
   }

  static void test_delete_odd_ascending()
   {final BtreeBam b = new BtreeBam(2, 3, 30);
    int N = 32;
    for (int i = 1; i <= N; i++)    b.put(i, i);
    for (int i = 1; i <= N; i += 2) b.delete(i);
    //stop(b.print());
    ok(b, """
                                              16                            24                            +
                                              5                             16                            23
                                              0                             0                             0
                                              0                             1                             2
        4          8            12            +              20             +              28             +
        1          4            8             9              13             14             18             20
        5          5            5             5              16             16             23             23
        0          1            2             3              0              1              0              1
2 4=1      6 8=4      10 12=8       14 16=9       18 20=13       22 24=14       26 28=18       30 32=20
""");
   }

  static void test_delete_even_descending()
   {final BtreeBam b = new BtreeBam(2, 3, 100);
    int N = 32;
    for (int i = 1; i <= N; i++)   b.put(i, i);
    for (int i = N; i > 0; i -= 2) b.delete(i);
    //stop(b.print());
    ok(b, """
                   8                         16                                                          +
                   5                         11                                                          16
                   0                         0                                                           0
                   0                         1                                                           2
        4          +           12            +              20             24             28             +
        1          4           8             9              13             14             18             20
        5          5           11            11             16             16             16             16
        0          1           0             1              0              1              2              3
1 3=1      5 7=4      9 11=8       13 15=9       17 19=13       21 23=14       25 27=18       29 31=20
""");
   }

  static void test_primes()
   {final BtreeBam b = new BtreeBam(2, 3, 100);
    int N = 64;
    for (int i = 1; i <= N; i++)
     {b.put(i, i);
     }
    for (int i = 2; i <= N; i++)
     {b.find(i);
      if (b.getFound() > 0)
       {for (int j = 2*i; j <= N; j += i)
         {b.delete(j);
         }
       }
     }
    //stop(b.print());
    ok(b, """
                   6                                  17                                        40                                           +
                   5                                  11                                        23                                           33
                   0                                  0                                         0                                            0
                   0                                  1                                         2                                            3
        2          +        8             16          +           19             29             +              43             53             +
        1          3        7             8           13          14             18             26             29             35             21
        5          5        11            11          11          23             23             23             33             33             33
        0          1        0             1           2           0              1              2              0              1              2
1 2=1      3 5=3      7=7       11 13=8       17=13       19=14       23 29=18       31 37=26       41 43=29       47 53=35       59 61=21
""");
   }

//D0 Tests                                                                      // Testing

  static void oldTests()                                                        // Tests thought to be in good shape
   {stuck_test_push();
    stuck_test_pop();
    stuck_test_shift();
    stuck_test_unshift();
    stuck_test_elementAt();
    stuck_test_insertElementAt();
    stuck_test_remove_element_at();
    stuck_test_first_last();
    stuck_test_search();
    stuck_test_search_greater_than_or_equal();
    stuck_test_search_greater_than_or_equal_except_last();
    test_put_ascending();
    test_put_descending();
    test_put_random_small();
    test_delete_odd_ascending();
    test_delete_even_descending();
    test_primes();
   }

  static void newTests()                                                        // Tests being worked on
   {oldTests();
    //test_primes();
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
