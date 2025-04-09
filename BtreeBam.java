//------------------------------------------------------------------------------
// Btree on the Basic Array Machine for performance comparision with BtreeSF.
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2025
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Btree in a block on the surface of a silicon chip.
//Collapse steal from left and indeed any other method where leaf/branch nolonger needed to be consodered differently
import java.util.*;

class BtreeBam extends Test                                                     // Manipulate a btree
 {final LayoutBam L;                                                            // The btree laid out in arrays

  final int
    maxKeysPerLeaf,                                                             // Maximum number of leafs in a key
    maxKeysPerBranch,                                                           // Maximum number of keys in a branch
    splitLeafSize,                                                              // The number of key, data pairs to split out of a leaf
    splitBranchSize,                                                            // The number of key, next pairs to split out of a branch
    numberOfNodes;                                                              // The number of noes in the btree

  final static int
   linesToPrintABranch =  4,                                                    // The number of lines required to print a branch
        maxPrintLevels = 10,                                                    // Maximum number of levels to print in a tree
              maxDepth = 99,                                                    // Maximum depth of any realistic tree
                gutter =  2,                                                    // Gutter between printed items
          linesPerNode =  4,                                                    // Number of lines needed to print a node
          linesPerTree = linesPerNode * maxDepth;                               // Number of lines needed to print a tree

  static boolean debug = false;                                                 // Debugging enabled

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

  int allocate()                                                                // Allocate a node
   {int f = L.get("freeChainHead");                                             // Last freed node
    if (f == 0) stop("No more memory available");                               // No more free nodes available
    L.move("freeChainHead", "free", ""+f);                                      // Second to last freed node becomes head of the free chain
    L.zero("free", ""+f); L.zero("isLeaf", ""+f); L.zero("current_size", ""+f); // Clear all control information
    L.zero("keys", ""+f); L.zero("data",   ""+f);
    return f;                                                                   // Return the node to be reused
   }

  void  free(int n)                                                             // Free a new node to make it available for reuse
   {if (n == 0) stop("Cannot free root");                                       // The root is never freed
    L.zero("free", ""+n); L.zero("isLeaf", ""+n); L.zero("current_size", ""+n); // Invalidate all control information
    L.zero("keys", ""+n); L.zero("data",   ""+n);
    int f = L.get("freeChainHead");                                             // Last freed node
    L.set(n, "freeChainHead");                                                  // Chain this node in front of the last freed node
    L.set(f, "free", ""+n);                                                     // Second to last freed node becomes head of the free chain
   }

  int allocLeaf()   {int n = allocate(); setLeaf  (n); return n;}               // Allocate leaf
  int allocBranch() {int n = allocate(); setBranch(n); return n;}               // Allocate branch

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
  boolean rootIsLeaf()   {return L.get("isLeaf", "root") > 0;}                  // The root is a leaf if true
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

  void splitLeafRoot()                                                         // Split a leaf which happens to be a full root into two half full leaves while transforming the root leaf into a branch
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
    final int plk = getKey();
    setStuck(p); stuck_shift();
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
      setStuck(P); setData(l); setIndex(index-1); stuck_setElementAt();         // Swap key of parent
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

      setStuck(P); setData(l); setIndex(index-1); stuck_setElementAt();         // Reduce key of parent of left
     }
    return true;
   }

  boolean stealFromRight(int node, int index)                                   // Steal from the right sibling of the indicated child if possible
   {final int P = node;
    setStuck(P);
    setIndex(index+0); stuck_elementAt(); final int l = getData(), lk = getKey();
    setIndex(index+1); stuck_elementAt(); final int r = getData();

    if (hasLeavesForChildren(P))                                                // Children are leaves
     {final int nl = leafSize(l);
      final int nr = leafSize(r);

      if (nl >= maxKeysPerLeaf) return false;                                       // Steal not possible because there is no where to put the steal
      if (nr <= 1) return false;                                                    // Steal not allowed because it would leave the right sibling empty

      setStuck(r); stuck_firstElement(); final int fk = getKey();               // First element of right child
      setStuck(l); stuck_push();                                                // Increase left
      setStuck(P); setKey(fk); setData(l); setIndex(index);stuck_setElementAt();// Swap key of parent
      setStuck(r); stuck_shift();                                               // Reduce right
     }
    else                                                                        // Children are branches
     {final int nl = branchSize(l);
      final int nr = branchSize(r);

      if (nl >= maxKeysPerBranch) return false;                                 // Steal not possible because there is no where to put the steal
      if (nr <= 1) return false;                                                // Steal not allowed because it would leave the right sibling empty

      setStuck(l); stuck_lastElement();                                         // Last element of left child
      setStuck(l); setKey(lk); setIndex(nl); stuck_setElementAt();              // Left top becomes real

      setStuck(r); stuck_firstElement(); final int fk = getKey();               // First element of  right child
      setStuck(l); setKey(0); stuck_push();                                     // New top for left is ignored by search ,.. except last

      setStuck(P); setKey(fk); setData(l); setIndex(index);stuck_setElementAt();// Swap key of parent
      setStuck(r); stuck_shift();                                               // Reduce right
     }
    return true;
   }

//D2 Merge                                                                      // Merge two nodes together and free the resulting free node

  boolean mergeRoot()                                                           // Merge into the root
   {if (rootIsLeaf() || branchSize(0) > 1) return false;

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
        free(l);
        free(r);
        return true;
       }
     }
    else if (branchSize(l) + 1 + branchSize(r) <= maxKeysPerBranch - 1)         // Branches
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
      free(l);
      free(r);
      return true;
     }
    return false;
   }

  boolean mergeLeftSibling(int parent, int index)                               // Merge the left sibling
   {final int P = parent;
    if (index == 0) return false;
    int bs = branchSize(P);
    if (bs < 2) return false;

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
      free(l);                                                                  // Free the empty left node
     }
    else                                                                        // Children are branches
     {final int nl = branchSize(l);
      final int nr = branchSize(r);

      if (nl + 1 + nr >= maxKeysPerBranch-1) return false;                      // Merge not possible because there is not enough room for the combined result

      setStuck(P); setIndex(index-1); stuck_elementAt(); final int t = getKey();// Top key
      setStuck(l); stuck_lastElement();                                         // Last element of left child
      setStuck(r); setKey(t); stuck_unshift();                                  // Left top to right

      setStuck(l); stuck_pop();                                                 // Remove left top
      int N = branchSize(l);                                                    // Number of entries to remove
      for (int i = 0; i < N; i++)                                               // Transfer left to right
       {setStuck(l); stuck_pop();
        setStuck(r); stuck_unshift();
       }
      free(l);                                                                  // Free the empty left node
     }
    setStuck(P); setIndex(index-1); stuck_removeElementAt();                    // Reduce P on left
    return true;
   }

  boolean mergeRightSibling(int parent, int index)                              // Merge the right sibling
   {final int P = parent;
    setStuck(P);
    final int bs = stuck_size();
    if (bs < 2) return false;

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
      free(r);                                                                  // Free the empty right node
     }
    else                                                                        // Children are branches
     {final int nl = branchSize(l);
      final int nr = branchSize(r);

      if (nl + 1 + nr >= maxKeysPerBranch-1) return false;                      // Merge not possible because there is not enough room in a single branch
      setStuck(l); stuck_lastElement(); final int ld = getData();               // Last element of left child
      setStuck(parent); setIndex(index); stuck_elementAt();                     // Parent dividing element
      setStuck(l); setData(ld); setIndex(nl); stuck_setElementAt();             // Re-key left top

      final int N = branchSize(r);                                              // Number of entries to remove
      for (int i = 0; i < N; i++)                                               // Transfer right to left
       {setStuck(r); stuck_shift();
        setStuck(l); stuck_push();
       }
      free(r);                                                                  // Free the empty right node
     }

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
    s.setLength(s.length()-1);
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

  String dump()                                                                 // Dump a tree horizontally
   {L.save();
    final int N = linesPerTree*linesPerNode;                                    // A big buffer with room for several lines per node
    final StringBuilder [] S = new StringBuilder[N];                            // A big buffer with room for several lines per node
    for (int i = 0; i < N; i++) S[i] = new StringBuilder();
    if (rootIsLeaf()) printLeaf(0, S, 0); else printBranch(0, S, 0);            // Print tree
    L.restore();
    return printCollapsed(S);                                                   // Collapse lines into text
   }

  void print(String title)                                                      // Print the tree
   {final String s = dump();
    say("Tree: %s", title);
    say(s);
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
   {if (rootIsLeaf())                                                           // The root is a leaf
     {setStuck(0); setKey(Key); stuck_search();
      find_result(0, L.get("s_found"), getIndex(), Key, getData());
      return;
     }

    int parent = 0;                                                             // Parent starts at root which is known to be a branch

    for (int i = 0; i < maxDepth; i++)                                          // Step down through tree
     {setStuck(parent); setKey(Key);
      stuck_searchFirstGreaterThanOrEqualExceptLast();
      final int n = getData();

      if (isLeaf(n))                                                            // Found the containing search
       {setStuck(n);  setKey(Key); stuck_search();
        find_result(n, L.get("s_found"), getIndex(), Key, getData());
        return;
       }
      parent = n;                                                               // Step down to lower branch
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
     {if (rootIsLeaf())
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

    if (rootIsLeaf())                                                           // Find and delete directly in root as a leaf
     {findAndDelete(Key);
      return;
     }

    int p = 0;                                                                  // Start at root

    for (int i = 0; i < maxDepth; i++)                                          // Step down from branch to branch through the tree until reaching a leaf repacking as we go
     {setStuck(p); setKey(Key);
      stuck_searchFirstGreaterThanOrEqualExceptLast();

      balance(p, getIndex());                                                   // Make sure there are enough entries in the parent to permit a deletion
      final int q = getIndex();

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

      for (int j = 0; j < branchSize(p); j++)                                   // Try merging each sibling pair which might change the size of the parent
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
    //stop(b.dump());
    ok(b.dump(), """
                                          2                                                         16
                                          13                                                        21                                                                                                                     14
                                          0                                                         0                                                                                                                      0
                                          0                                                         1                                                                                                                      2
                   2                                                  12                                                          20                            24                            28
                   5                      9                           12                            17                            20                            24                            27                           6
                   13                     13                          21                            21                            14                            14                            14                           14
                   0                      1                           0                             1                             0                             1                             2                            3
        2                      6                       10                            14                            18                            22                            26                            30
        1          3           4          7            8              10             11             15             16             18             19             22             23             25             26            2
        5          5           9          9            12             12             17             17             20             20             24             24             27             27             6             6
        0          1           0          1            0              1              0              1              0              1              0              1              0              1              0             1
1 2=1      3 4=3       5 6=4      7 8=7       9 10=8       11 12=10       13 14=11       15 16=15       17 18=16       19 20=18       21 22=19       23 24=22       25 26=23       27 28=25       29 30=26       31 32=2
""");
 }

  static void test_put_descending()
   {final BtreeBam b = new BtreeBam(2, 3, 29);
    int N = 32;
    for (int i = N; i > 0; i--) b.put(i, i);
    //stop(b.dump());
    ok(b.dump(), """
                                                                                                           16                                                        20
                                                                                                           19                                                        11                                                      14
                                                                                                           0                                                         0                                                       0
                                                                                                           0                                                         1                                                       2
                      4                         8                            12                                                          20                                                      27
                      27                        24                           20                            17                            12                          9                           5                           6
                      19                        19                           19                            19                            11                          11                          14                          14
                      0                         1                            2                             3                             0                           1                           0                           1
         2                         6                          10                            14                            18                           22                          26                          30
         28           25           23           21            22             18             16             13             15             10            8             7             4             3             2             1
         27           27           24           24            20             20             17             17             12             12            9             9             5             5             6             6
         0            1            0            1             0              1              0              1              0              1             0             1             0             1             0             1
1 2=28       3 4=25       5 6=23       7 8=21       9 10=22       11 12=18       13 14=16       15 16=13       17 18=15       19 20=10       21 22=8       23 24=7       25 26=4       27 28=3       29 30=2       31 32=1
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
   }

  static void newTests()                                                        // Tests being worked on
   {oldTests();
    test_put_ascending();
    test_put_descending();
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
