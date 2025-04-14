//------------------------------------------------------------------------------
// BtreeBap with machine code
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2025
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Btree in a block on the surface of a silicon chip.

import java.util.*;

class BtreeBap extends Test                                                     // Manipulate a btree using only a basic array machine.
 {final Ban L;                                                                  // The btree laid out in arrays and operated on by a program

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

  BtreeBap(int MaxKeysPerLeaf, int MaxKeysPerBranch, int NumberOfNodes)         // Define a BTree with the specified dimensions
   {zz();
    maxKeysPerLeaf   = MaxKeysPerLeaf;
    maxKeysPerBranch = MaxKeysPerBranch;
    splitLeafSize    = maxKeysPerLeaf   >> 1;
    splitBranchSize  = maxKeysPerBranch >> 1;
    numberOfNodes    = NumberOfNodes;
    L                = layoutBtree();
    L.zero("free"); L.zero("isLeaf"); L.zero("current_size");                   // Clear all control information
    L.zero("keys"); L.zero("data");                                             // Clear all data
    for (int i = numberOfNodes-1; i > 1; --i) L.clear(i, "free", ""+(i-1));     // Initialize free chain by putting all the nodes on the free chain except the root (which is permanently allocated at position 0) with the low nodes first to be allocated.
    L.clear(1, "freeChainHead");                                                // The first freed node
    L.clear(0, "root");                                                         // The root
    L.clear(1, "isLeaf", "root");                                               // The root starts as a leaf
   }

  Ban layoutBtree()                                                             // Layout the btree
   {final int k = max(maxKeysPerBranch+1, maxKeysPerLeaf);
    final int d = max(maxKeysPerBranch+1, maxKeysPerLeaf);

    return new Ban()
     {void load() {
        array("allocate");                                                      // Result of calling allocate
        array("balance_index");                                                 // The index of the child to be balanced
        array("balance_parent");                                                // The parent of the branch which wants to balance a child
        array("branchSize");                                                    // Get branch size
        array("child");                                                         // The child node during a descent through the tree
        array("current_size", numberOfNodes);                                   // Current size of stuck
        array("data",         numberOfNodes, d);                                // Data
        array("delete_Key");                                                    // The key to delete
        array("f_data");                                                        // Data associated with key
        array("f_found");                                                       // Whether the key was found
        array("findAndDelete_Key");                                             // The key to find and delete
        array("findAndInsert_Data");                                            // The data to insert
        array("findAndInsert_Key");                                             // The key to insert
        array("f_index");                                                       // The index in the leaf or branch of the greater than or equal key
        array("find_Key");                                                      // Key to find in a tree
        array("find_result_leaf");                                              // The node index of the leaf containing the key found by fine
        array("f_inserted");                                                    // Inserted if true
        array("f_key");                                                         // Matching found key
        array("f_leaf");                                                        // Node number of leaf found
        array("freeChainHead");                                                 // The head of the free chain
        array("free",         numberOfNodes);                                   // Used to place the node  on the free chain else zero if in use
        array("f_success");                                                     // Inserted or updated if true
        array("hasLeavesForChildren");                                          // Whether the node has leaves for children
        array("isFullRoot");                                                    // Whether the root is full
        array("isFull");                                                        // Whether the node is  full or not
        array("isALeaf");                                                       // Test whether a node is a leaf
        array("isLeaf",       numberOfNodes);                                   // Whether each node is a leaf or a branch
        array("isLow");                                                         // Test whether the node is low on children
        array("keys",         numberOfNodes, k);                                // Keys
        array("leafSize");                                                      // The result of leaf size
        array("merge_Key");                                                     // The Key along whose path we should merge
        array("mergeLeftSibling_bs");
        array("mergeLeftSibling_index");                                        // The index of the child that wants to steal from the child in its parent
        array("mergeLeftSibling_l");
        array("mergeLeftSibling_left");
        array("mergeLeftSibling_nl");                                           // Number of children on left
        array("mergeLeftSibling_nr");                                           // Number of children on right
        array("mergeLeftSibling_nlr");                                          // Number of children on left and right
        array("mergeLeftSibling_parent");                                       // The parent of the branch which wants to merge with its left sibling
        array("mergeLeftSibling_r");
        array("mergeLeftSibling_size");
        array("mergeLeftSibling_t");
        array("mergeLeftSibling");                                              // Whether the merge with the left sibling was successful
        array("mergeRightSibling_bs");
        array("mergeRightSibling_index");                                       // The index of the child that wants to steal from the child in its parent
        array("mergeRightSibling_index1");                                      // The index of the child to be stolen from
        array("mergeRightSibling_l");
        array("mergeRightSibling_nl");                                          // Number of children on left
        array("mergeRightSibling_nr");                                          // Number of children on right
        array("mergeRightSibling_nlr");                                         // Number of children on left and right
        array("mergeRightSibling_parent");                                      // The parent of the branch which wants to merge with its left sibling
        array("mergeRightSibling_ld");                                          // Last child of left node
        array("mergeRightSibling_pk");                                          // One up from split point in parent
        array("mergeRightSibling_r");
        array("mergeRightSibling_size");
        array("mergeRightSibling_t");
        array("mergeRightSibling");                                             // Whether the merge with the left sibling was successful
        array("mergeRoot_l");                                                   // Left child of root
        array("mergeRoot_nl");                                                  // Number in left child
        array("mergeRoot_nP");                                                  // Number in root
        array("mergeRoot_nr");                                                  // Number in right child
        array("mergeRoot_nlr");                                                 // Number in left plus right child
        array("mergeRoot_pkn");                                                 // First key in root
        array("mergeRoot_r");                                                   // Right child of node
        array("parent");                                                        // The parent node during a descent through the tree
        array("put_Data");                                                      // The data to put into the tree
        array("put_Key");                                                       // The key to put into the tree
        array("root");                                                          // Always zero indicating the location of the root which never changes
        array("rootIsLeaf");                                                    // Whether the root is a
        array("s_data");                                                        // The input data for a stuck or the resulting data found in a stuck
        array("setBranch");                                                     // Set the specified  node as a branch
        array("setLeaf");                                                       // Set the specified  node as a leaf
        array("s_found");                                                       // Whether a matching key was found when searching a stuck
        array("s_index");                                                       // The input index of a key,data pair in a stuck or the output index of a located key, data pair
        array("s_key");                                                         // The input key for searching or adding to a stuck and the output of a search greater than
        array("splitBranch_index");                                             // The index in the parent of the branch to be split
        array("splitBranch_l");                                                 // New left leaf
        array("splitBranch_node");                                              // The branch to be split
        array("splitBranch_parent");                                            // The parent of the leag=f to be split
        array("splitBranch_rk");                                                // Right key
        array("splitBranchRoot_l");                                             // New left branch
        array("splitBranchRoot_plk");                                           // Parent left key
        array("splitBranchRoot_r");                                             // New right branch
        array("splitLeaf_F");                                                   // First of right
        array("splitLeaf_fl");                                                  // Mid point
        array("splitLeaf_index");                                               // The index in the parent of the leaf to be split
        array("splitLeaf_L");                                                   // Last of left
        array("splitLeaf_l");                                                   // New left split out leaf
        array("splitLeaf_node");                                                // The leaf to be split
        array("splitLeaf_parent");                                              // The parent of the leag=f to be split
        array("splitLeafRoot_first");                                           // First of right leaf
        array("splitLeafRoot_i");                                               // Build left leaf from parent
        array("splitLeafRoot_kv");                                              // Mid key
        array("splitLeafRoot_last");                                            // Last of left leaf
        array("splitLeafRoot_l");                                               // New left leaf
        array("splitLeafRoot_r");                                               // New right leaf
        array("stealFromLeft_bd");                                              // Right half of mid key
        array("stealFromLeft_index");                                           // The index of the child that wants to steal from its left sibling in its parent
        array("stealFromLeft_left");                                            // The index of the left child being stolen from
        array("stealFromLeft_l");                                               // New left node
        array("stealFromLeft_nl2");                                             // Two less than the number on the left
        array("stealFromLeft_nl");                                              // Size of left node
        array("stealFromLeft_nr");                                              // Size of right node
        array("stealFromLeft_parent");                                          // The parent of the branch which wants to steal from the left to give to the right
        array("stealFromLeft_r");                                               // Existing right node
        array("stealFromLeft_td");                                              // Left half of mid key
        array("stealFromLeft");                                                 // Whether the steal from the left was successful
        array("stealFromRight_bd");                                             //
        array("stealFromRight_fk");                                             // Right child key
        array("stealFromRight_index");                                          // The index of the child that wants to steal from the right sibling in its parent
        array("stealFromRight_lk");                                             // Left child key
        array("stealFromRight_l");                                              // Left child index
        array("stealFromRight_nl");                                             // Number in left child
        array("stealFromRight_nP");                                             // Parent branch size
        array("stealFromRight_nr");                                             // Number in right child
        array("stealFromRight_parent");                                         // The parent of the branch which wants to steal from the right child
        array("stealFromRight_right");                                          // Index of sibling on right
        array("stealFromRight_r");                                              // Right child index
        array("stealFromRight_td");                                             //
        array("stealFromRight");                                                // Whether the steal from the right was successful
        array("stuck");                                                         // The index of the stuck to be operated on
        array("find_loop");                                                     // Iterator for the find loop
        array("put_loop");                                                      // Iterator for the put loop
        array("delete_loop");                                                   // Iterator for the delete loop
        array("merge_loop");                                                    // Iterator for the merge loop
        array("merge_indices");                                                 // Iterator for each index in a node being examined for merges
        array("merge_IndexLimit");                                              // The number of indoces to be checked
        array("stuckUnshift_i");                                                // Loop iterator to move elements up
        array("stuckUnshift_I");                                                // Loop iterator to move elements up minus one
        array("stuckShift_i");                                                  // Loop iterator to move elements down
        array("stuckShift_I");                                                  // Loop iterator to move elements down minus one
        array("stuckShift_N");                                                  // Loop limit to move elements down
        array("stuckInsertElementAt_i");                                        // Index of upper element to move up to
        array("stuckInsertElementAt_I");                                        // Index of lower element to be moved
        array("stuckInsertElementAt_L");                                        // Loop limit
        array("stuckRemoveElementAt_i");                                        // Index of upper element to move up to
        array("stuckRemoveElementAt_I");                                        // Index of lower element to be moved
        array("stuckRemoveElementAt_N");                                        // Loop limit
        array("stuckSearch_N");                                                 // Stuck size
       }
     };
   }

//D1 Nodes

//D2 Allocate and free                                                          // Allocate nodes for use in the tree and free them for reuse

  void allocate(String a)                                                       // Allocate a node
   {final String fc = "freeChainHead";
    L.move(a, fc);
    L.get(a);                                                                   // Last freed node
    if (L.i() == 0) stop("No more memory available");                           // No more free nodes available
    L.move(fc, "free", a);                                                      // Second to last freed node becomes head of the free chain
    L.zero("free", a); L.zero("isLeaf", a); L.zero("current_size", a);          // Clear all control information
    L.zero("keys", a); L.zero("data",   a);
   }

  void  free(String f)                                                          // Free a new node to make it available for reuse
   {L.get(f);
    if (L.i() == 0) stop("Cannot free root");                                       // The root is never freed
    L.ones("free", f); L.ones("isLeaf", f); L.ones("current_size", f);          // Invalidate all control information
    L.ones("keys", f); L.ones("data",   f);
    L.move("free", "freeChainHead", f);                                         // Chain this node in front of the last freed node
    L.move("freeChainHead", f);                                                 // Freed node becomes head of the free chain
   }

  void allocLeaf  (String node) {allocate(node); L.move("setLeaf",   node);   setLeaf();} // Allocate leaf
  void allocBranch(String node) {allocate(node); L.move("setBranch", node); setBranch();} // Allocate branch

//D2 Basics                                                                     // Basic operations on nodes

  void getKey  (String n) {L.move(n, "s_key"  );}                               // Move current key   to target
  void getData (String n) {L.move(n, "s_data" );}                               // Move current data  to target
  void getStuck(String n) {L.move(n, "stuck"  );}                               // Move current stuck to target
  void getIndex(String n) {L.move(n, "s_index");}                               // Move current index to target

  void setKey  (int n) {L.set(n, "s_key");}
  void setData (int n) {L.set(n, "s_data");}
  void setStuck(int n) {L.set(n, "stuck");}
  void setIndex(int n) {L.set(n, "s_index");}

  void setKey   (String n) {L.move("s_key" ,  n);}                              // Set current key
  void setData  (String n) {L.move("s_data",  n);}                              // Set current data
  void setStuck (String n) {L.move("stuck",   n);}                              // Set current stuck
  void setIndex (String n) {L.move("s_index", n);}                              // Set current index
  void setIndexL(String n) {L.move("s_index", n); L.dec("s_index");}            // Set current index left
  void setIndexR(String n) {L.move("s_index", n); L.inc("s_index");}            // Set current index right

  int getFound    () {L.get("f_found");  return L.i();}                         // Whether the key was found
  int getFSuccess () {L.get("f_success");return L.i();}                         // Inserted or updated if true

  void putFSuccess (int n) {L.set(n, "f_success");}                             // Inserted or updated if true
  void putFInserted(int n) {L.set(n, "f_inserted");}                            // Inserted if true

  void isLeaf       () {L.move("isALeaf", "isLeaf", "isALeaf");}                // A leaf if the target is not zero
  void rootIsLeaf   () {L.move("rootIsLeaf", "isLeaf", "root");}                // The root is a leaf if the target is not zero

  void setLeaf      () {L.clear(1, "isLeaf", "setLeaf");}                       // Set as leaf
  void setBranch    () {L.clear(0, "isLeaf", "setBranch");}                     // Set as branch
  void setLeafRoot  () {L.clear(1, "isLeaf", "root");}                          // Set root as leaf
  void setBranchRoot() {L.clear(0, "isLeaf", "root");}                          // Set root as branch

  void leafSize     () {L.move("leafSize",   "current_size", "leafSize"  );}    // Number of children in leaf
  void branchSize   ()                                                          // Number of children in body of branch
   {L.move("branchSize", "current_size", "branchSize");
    L.dec("branchSize");
   }

  void isFull()                                                                 // The node is full
   {L.move("isALeaf", "isFull");
    isLeaf();
    L.get("isALeaf");
    if (L.i() > 0) isFullLeaf(); else isFullBranch();
   }

  void isFullLeaf()                                                             // The leaf is full
   {L.move("leafSize", "isFull");
    leafSize();
    L.get("leafSize");
    L.set(L.i() >= maxKeysPerLeaf ? 1 : 0, "isFull");
   }
  void isFullBranch()                                                           // The branch is full
   {L.move("branchSize", "isFull");
    branchSize();
    L.get("branchSize");
    L.set(L.i() >= maxKeysPerBranch ? 1 : 0, "isFull");
   }

  void isFullRoot()                                                             // The root is full
   {L.zero("isFull");
    isFull();
    L.get("isFull");
    L.set(L.i(), "isFullRoot");
   }

  void isLow()                                                                  // The branch is low on children making it impossible to merge two sibling children
   {L.move("branchSize", "isLow");
    branchSize();
    L.get("branchSize");
    L.set(L.i() < 2 ? 1 : 0, "isLow");
   }

  void hasLeavesForChildren()                                                   // Whether the branch has leaves for children
   {L.move("isALeaf", "data", "hasLeavesForChildren", "0");
    isLeaf();
    L.get("isALeaf");
    L.set(L.i() > 0 ? 1 : 0, "hasLeavesForChildren");
   }

//D2 Nodes                                                                      // Operations on nodes

//D2 Split                                                                      // Split nodes in half to increase the number of nodes in the tree

  void splitLeafRoot()                                                          // Split a leaf which happens to be a full root into two half full leaves while transforming the root leaf into a branch
   {final String $l     = "splitLeafRoot_l"; allocLeaf($l);                     // Allocate new left leaf
    final String $r     = "splitLeafRoot_r"; allocLeaf($r);                     // Allocate new right leaf
    final String $p     = "root";                                               // Parent
    final String $first = "splitLeafRoot_first";
    final String $last  = "splitLeafRoot_last";
    final String $kv    = "splitLeafRoot_kv";

    for (int i = 0; i < splitLeafSize; i++)                                     // Build left leaf from parent
     {setStuck($p); stuck_shift();
      setStuck($l); stuck_push();
     }
    for (int i = 0; i < splitLeafSize; i++)                                     // Build right leaf from parent
     {setStuck($p); stuck_shift();
      setStuck($r); stuck_push();
     }

    stuck_firstElement();
    getKey($first);                                                             // First of right leaf

    L.move("stuck", $l); stuck_lastElement();                                   // Last of left leaf
    getKey($last);                                                              // Last of left leaf

    L.move($kv, $last);                                                         // Mid key
    L.add ($kv, $first);
    L.shiftRight($kv);

    setBranchRoot();
    setStuck($p);  stuck_clear();                                               // Clear the root
    setKey($kv); setData($l); stuck_push();                                     // Insert left leaf into root
    setKey($p);  setData($r); stuck_push();                                     // Insert right into root. This will be the top node and so ignored by search ... except last.
   }

  void splitBranchRoot()                                                        // Split a branch which happens to be a full root into two half full branches while retaining the current branch as the root
   {final String $l = "splitBranchRoot_l"; allocBranch($l);                     // New left branch
    final String $r = "splitBranchRoot_r"; allocBranch($r);                     // New right branch
    final String $p = "root";                                                   // Root
    final String $plk = "splitBranchRoot_plk";

    for (int i = 0; i < splitBranchSize; i++)                                   // Build left child from parent
     {setStuck($p); stuck_shift();
      setStuck($l); stuck_push();
     }
    setStuck($p); stuck_shift();
    getKey($plk);
    setStuck($l); setKey($p); stuck_push();                                     // Left top

    for(int i = 0; i < splitBranchSize; i++)                                    // Build right child from parent
     {setStuck($p); stuck_shift();
      setStuck($r); stuck_push();
     }

    setStuck($p); stuck_shift();
    setStuck($r); setKey($p); stuck_push();                                     // Right top

    setStuck($p);                                                               // Clear root
    stuck_clear();
    setKey($plk);  setData($l); stuck_push();
    setKey($p);    setData($r); stuck_push();
   }

  void splitLeaf()                                                              // Split a leaf which is not the root
   {final String $p  = "splitLeaf_parent";                                      // Parent
    final String $l  = "splitLeaf_l";      allocLeaf($l);                       // New  split out leaf
    final String $r  = "splitLeaf_node";                                        // Existing  leaf on right
    final String $in = "splitLeaf_index";                                       // Index of the child to be split in its parent
    final String $fl = "splitLeaf_fl";
    final String $F  = "splitLeaf_F";
    final String $L  = "splitLeaf_L";

    for (int i = 0; i < splitLeafSize; i++)                                     // Build left leaf
     {setStuck($r); stuck_shift();
      setStuck($l); stuck_push();
     }
    setStuck($r); stuck_firstElement(); getKey($F);
    setStuck($l); stuck_lastElement (); getKey($L);

    L.move($fl, $F);                                                            // Mid key
    L.add ($fl, $L);
    L.shiftRight($fl);

    setStuck($p); setKey($fl); setData($l); setIndex($in);
    stuck_insertElementAt();                                                    // Insert new key, next pair in parent
   }

  void splitBranch()                                                            // Split a branch which is not the root by splitting right to left
   {final String $p  = "splitBranch_parent";
    final String $l  = "splitBranch_l";       allocBranch($l);
    final String $r  = "splitBranch_node";
    final String $in = "splitBranch_index";
    final String $rk = "splitBranch_rk";

    for (int i = 0; i < splitBranchSize; i++)                                   // Build left branch from right
     {setStuck($r); stuck_shift();
      setStuck($l); stuck_push();
     }

    setStuck($r); stuck_shift();                                                // Build right branch
    getKey($rk);
    setStuck($l); setKey("root"); stuck_push();                                 // Becomes top and so is ignored by search ... except last
    setStuck($p); setKey($rk); setData($l); setIndex($in);
    stuck_insertElementAt();
   }

  void stealFromLeft()                                                          // Steal from the left sibling of the indicated child if possible to give to the right - Dennis Moore, Dennis Moore, Dennis Moore.
   {final String $sfl    = "stealFromLeft";
    final String $P      = "stealFromLeft_parent";
    final String $index  = "stealFromLeft_index";
    final String $nl2    = "stealFromLeft_nl2";
    final String $left   = "stealFromLeft_left";
    final String $l      = "stealFromLeft_l";
    final String $r      = "stealFromLeft_r";
    final String $nl     = "stealFromLeft_nl";
    final String $nr     = "stealFromLeft_nr";
    final String $td     = "stealFromLeft_td";
    final String $bd     = "stealFromLeft_bd";
    L.clear(0, $sfl);                                                           // Assume at the start that we cannot steal from the lrft

    L.new Block()                                                               // Exiting this block has the same effect as return would have
     {void code()
       {final Ban.Block finished = this;                                        // Return
        setStuck($P);
        L.get($index); finished.endIfEq();                                      // Nothing to the left to steal from and equals is easier to synthesize than less than or equal
        L.move($left, $index); L.dec ($left);                                   // Index of left child
        setIndex($left);  stuck_elementAt(); getData($l);
        setIndex($index); stuck_elementAt(); getData($r);

        L.move("hasLeavesForChildren", $P);
        hasLeavesForChildren();

        L.new Block()                                                           // Exiting this block has the same effect as return would have
         {void code()
           {final Ban.Block leaves = this;
             {L.get("hasLeavesForChildren"); leaves.endIfEq();                  // Children are leaves
              L.move("leafSize", $l); leafSize(); L.move($nl, "leafSize");
              L.move("leafSize", $r); leafSize(); L.move($nr, "leafSize");

              L.compare(maxKeysPerLeaf, $nr); finished.endIfGe();               // Steal not possible because there is no where to put the steal
              L.compare(1, $nl); finished.endIfLe();                            // Steal not allowed because it would leave the leaf sibling empty

              setStuck($l); stuck_lastElement();
              setStuck($r); stuck_unshift();                                    // Increase right
              setStuck($l); stuck_pop();                                        // Reduce left

              L.move($nl2, $nl); L.add(-2, $nl2);                               // Index of last key on left
              setIndex($nl2); stuck_elementAt();                                // Last key on left
             }
           }
         };
        L.new Block()                                                           // Exiting this block has the same effect as return would have
         {void code()
           {final Ban.Block branches = this;                                    // Children are branches
             {L.get("hasLeavesForChildren"); branches.endIfGt();
              L.move("branchSize", $l); branchSize(); L.move($nl, "branchSize");
              L.move("branchSize", $r); branchSize(); L.move($nr, "branchSize");

              L.compare(maxKeysPerBranch, $nr); finished.endIfGe();             // Steal not possible because there is no where to put the steal
              L.compare(1, $nl); finished.endIfLe();                            // Steal not allowed because it would leave the left sibling empty

              setStuck($l); stuck_lastElement(); getData($td);                  // Increase right with left top
              setStuck($P); setIndex($index);    stuck_elementAt();

              setStuck($r); setData($td); setIndex("root");
              stuck_insertElementAt();                                          // Increase right with left top
              setStuck($l); stuck_pop();                                        // Remove left top

              setStuck($r); stuck_firstElement(); getData($bd);                 // Increase right with left top
              setStuck($P); setIndex($left); stuck_elementAt();

              setStuck($r); setData($bd); setIndex("root");
              stuck_setElementAt();                                             // Reduce key of parent of right
              setStuck($l); stuck_lastElement();                                // Last left key
             }
            setStuck($P); setData($l); setIndex($left); stuck_setElementAt();   // Reduce key of parent of left
            L.clear(1, $sfl);
           }
         };
       }
     };
   }

  void stealFromRight()                                                         // Steal from the right sibling of the indicated child if possible
   {final String $sfr    = "stealFromRight";                                    // Whether the steal from the right was successful
    final String $P      = "stealFromRight_parent";                             // Parent node
    final String $index  = "stealFromRight_index";                              // Index of child stealing from the right sibling
    final String $parent = "stealFromRight_parent";                             // The parent of the branch which wants to steal from the right child
    final String $nP     = "stealFromRight_nP";                                 // Parent branch size
    final String $right  = "stealFromRight_right";                              // Index of sibling on right
    final String $l      = "stealFromRight_l";                                  // Left child index
    final String $lk     = "stealFromRight_lk";                                 // Left child key
    final String $fk     = "stealFromRight_fk";                                 // Right child key
    final String $r      = "stealFromRight_r";                                  // Right child index
    final String $nl     = "stealFromRight_nl";                                 // Number in left child
    final String $nr     = "stealFromRight_nr";                                 // Number in right child
    final String $td     = "stealFromRight_td";                                 //
    final String $bd     = "stealFromRight_bd";                                 //

    L.clear(0, $sfr);                                                           // Assume at the start that we cannot steal from the right

    L.move("branchSize", $P); branchSize(); L.move($nP, "branchSize");

    L.new Block()
     {void code()
       {final Ban.Block finished = this;
        L.compare($index, $nP); finished.endIfGe();
        setStuck($P);
        setIndex($index); stuck_elementAt(); getData($l); getKey($lk);
        L.move($right, $index); L.inc($right);
        setIndex($right); stuck_elementAt(); getData($r);

        L.move("hasLeavesForChildren", $P);
        hasLeavesForChildren();
        L.new Block()                                                           // Children are leaves
         {void code()
           {final Ban.Block leaves = this;
            leaves.endIfEq();
            L.move("leafSize", $l); leafSize(); L.move($nl, "leafSize");
            L.move("leafSize", $r); leafSize(); L.move($nr, "leafSize");

            L.compare(maxKeysPerLeaf, $nl); finished.endIfGe();                 // Steal not possible because there is no where to put the steal
            L.compare(1, $nr);              finished.endIfLe();                 // Steal not allowed because it would leave the right sibling empty
           }
         };
        L.new Block()                                                           // Children are branches
         {void code()
           {final Ban.Block branches = this;
            L.get("hasLeavesForChildren"); branches.endIfGt();
            L.move("branchSize", $l); branchSize(); L.move($nl, "branchSize");
            L.move("branchSize", $r); branchSize(); L.move($nr, "branchSize");

            L.compare(maxKeysPerBranch, $nl); finished.endIfGe();               // Steal not possible because there is no where to put the steal
            L.compare(1, $nr);                finished.endIfLe();               // Steal not allowed because it would leave the right sibling empty

            setStuck($l);
            stuck_lastElement();                                                // Last element of left child
            setKey($lk); setIndex($nl); stuck_setElementAt();                   // Left top becomes real
           }
         };

        setStuck($r); stuck_firstElement(); getKey($fk);                        // First element of right child
        setStuck($l); stuck_push();                                             // Increase left
        setStuck($P); setKey($fk); setData($l);
        setIndex($index); stuck_setElementAt();                                 // Swap key of parent
        setStuck($r); stuck_shift();                                            // Reduce right
        L.clear(1, $sfr);
       }
     };
   }

//D2 Merge                                                                      // Merge two nodes together and free the resulting free node

  void mergeRoot()                                                              // Merge into the root
   {final String $nP  = "mergeRoot_nP";
    final String $l   = "mergeRoot_l";
    final String $r   = "mergeRoot_r";
    final String $nl  = "mergeRoot_nl";
    final String $nr  = "mergeRoot_nr";
    final String $nlr = "mergeRoot_nlr";
    final String $pkn = "mergeRoot_pkn";

    L.new Block()
     {void code()
       {final Ban.Block finished = this;
        rootIsLeaf();
        L.get("rootIsLeaf");         finished.endIfGt();
        L.clear(0, "branchSize"); branchSize(); L.move($nP, "branchSize");
        L.compare(1, $nP);           finished.endIfGt();

        setStuck(0);
        stuck_firstElement(); getData($l);
        stuck_lastElement (); getData($r);

        L.move("hasLeavesForChildren", "root");
        hasLeavesForChildren();
        L.new Block()
         {void code()
           {final Ban.Block leaves = this;                                      // Children are leaves
            L.get("hasLeavesForChildren"); leaves.endIfEq();
            L.move("leafSize", $l); leafSize(); L.move($nl, "leafSize");
            L.move("leafSize", $r); leafSize(); L.move($nr, "leafSize");

            L.move($nlr, $nl); L.add($nlr, $nr);
            L.compare(maxKeysPerLeaf, $nlr); finished.endIfGt();
            setStuck("root"); stuck_clear();

            L.new Block()
             {void code()
               {final Ban.Block leftChild = this;
                for (int i = 0; i < maxKeysPerLeaf; ++i)
                 {L.compare(i, $nl); leftChild.endIfLe();
                  setStuck($l);     stuck_shift();
                  setStuck("root"); stuck_push();
                 }
               }
             };

            L.new Block()
             {void code()
               {final Ban.Block rightChild = this;
                for (int i = 0; i < maxKeysPerLeaf; ++i)
                 {L.compare(i, $nr); rightChild.endIfLe();
                  setStuck($r); stuck_shift();
                  setStuck("root"); stuck_push();
                 }
               }
             };

            L.move("setLeaf", "root"); setLeaf();
            free($l);
            free($r);
           }
         };

        L.new Block()                                                           // Children are branches
         {void code()
           {final Ban.Block branches = this;
            L.get("hasLeavesForChildren"); branches.endIfGe();
            L.move("branchSize", $l); branchSize(); L.move($nl, "branchSize");
            L.move("branchSize", $r); branchSize(); L.move($nr, "branchSize");

            L.move($nlr, $nl); L.add($nlr, $nr); L.inc($nlr);
            L.compare(maxKeysPerBranch, $nlr); finished.endIfGt();
            setStuck("root"); stuck_firstElement(); getKey($pkn);
            stuck_clear();

            L.new Block()
             {void code()
               {final Ban.Block leftChild = this;
                for (int i = 0; i < maxKeysPerBranch; ++i)
                 {L.compare(i, $nl); leftChild.endIfLe();
                  setStuck($l);     stuck_shift();
                  setStuck("root"); stuck_push();
                 }
               }
             };
            setStuck($l);                   stuck_lastElement();
            setStuck("root"); setKey($pkn); stuck_push();

            L.new Block()
             {void code()
               {final Ban.Block rightChild = this;
                for (int i = 0; i < maxKeysPerBranch; ++i)
                 {L.compare(i, $nr); rightChild.endIfLe();
                  setStuck($r); stuck_shift();
                  setStuck("root"); stuck_push();
                 }
               }
             };
            setStuck($r); stuck_lastElement();                                  // Top next
            setStuck("root"); setKey("root"); stuck_push();                     // Top so ignored by search ... except last
            free($l);
            free($r);
           }
         };
       }
     };
   }

  void mergeLeftSibling()                                                       // Merge the left sibling
   {final String $mls    = "mergeLeftSibling";
    final String $P      = "mergeLeftSibling_parent";
    final String $index  = "mergeLeftSibling_index";
    final String $bs     = "mergeLeftSibling_bs";
    final String $l      = "mergeLeftSibling_l";
    final String $r      = "mergeLeftSibling_r";
    final String $nl     = "mergeLeftSibling_nl";
    final String $nr     = "mergeLeftSibling_nr";
    final String $nlr    = "mergeLeftSibling_nlr";
    final String $size   = "mergeLeftSibling_size";
    final String $t      = "mergeLeftSibling_t";
    L.clear(0, $mls);                                                           // Assume at the start that we will not be able to merge with the left sibling

    L.new Block()
     {void code()
       {final Ban.Block finished = this;
        L.get($index); finished.endIfEq();                                      // No left sibling to merge from
        L.move("branchSize", $P); branchSize(); L.move($bs, "branchSize");
        L.compare($index, $bs); finished.endIfGe();

        setStuck($P);
        setIndexL($index); stuck_elementAt(); getData($l);
        setIndex ($index); stuck_elementAt(); getData($r);

        L.move("hasLeavesForChildren", $P);
        hasLeavesForChildren();
        L.new Block()                                                           // Children are leaves
         {void code()
           {final Ban.Block leaves = this;
            L.get("hasLeavesForChildren"); leaves.endIfEq();
            L.move("leafSize", $l); leafSize(); L.move($nl, "leafSize");
            L.move("leafSize", $r); leafSize(); L.move($nr, "leafSize");

            L.move($nlr, $nl); L.add($nlr, $nr);
            L.compare(maxKeysPerLeaf, $nlr); finished.endIfGe();                // Combined body would be too big

            stuck_size($size, $l);                                              // Number of entries to remove
            L.new Block()
             {void code()
               {final Ban.Block leftChild = this;
                for (int i = 0; i < maxKeysPerLeaf; i++)                        // Transfer left to right
                 {L.compare(i, $size); leftChild.endIfLe();
                  setStuck($l); stuck_pop();
                  setStuck($r); stuck_unshift();
                 }
               }
             };
           }
         };
        L.new Block()                                                           // Children are branches
         {void code()
           {final Ban.Block branches = this;
            L.get("hasLeavesForChildren"); branches.endIfGt();
            L.move("branchSize", $l); branchSize(); L.move($nl, "branchSize");
            L.move("branchSize", $r); branchSize(); L.move($nr, "branchSize");

            L.move($nlr, $nl); L.add(1, $nlr); L.add($nlr, $nr);
            L.compare(maxKeysPerBranch, $nlr); finished.endIfGt();              // Merge not possible because there is not enough room for the combined result

            setStuck($P);
            setIndexL($index); stuck_elementAt(); getKey($t);                   // Top key
            setStuck($l); stuck_lastElement();                                  // Last element of left child
            setStuck($r); setKey($t); stuck_unshift();                          // Left top to right

            setStuck($l); stuck_pop();                                          // Remove left top
            L.move("branchSize", $l); branchSize();
            L.move($size, "branchSize");  L.add(1, $size);

            L.new Block()
             {void code()
               {final Ban.Block rightChild = this;
                for (int i = 0; i < maxKeysPerBranch; i++)                      // Transfer left to right
                 {L.compare(i, $size); rightChild.endIfLe();
                  setStuck($l); stuck_pop();
                  setStuck($r); stuck_unshift();
                 }
               }
             };
           }
         };
        free($l);                                                               // Free the empty left node
        setStuck($P); setIndexL($index); stuck_removeElementAt();               // Reduce parent on left
        L.clear(1, $mls);                                                       // Success
       }
     };
   }

  void mergeRightSibling()                                                      // Merge the right sibling
   {final String $mrs    = "mergeRightSibling";
    final String $P      = "mergeRightSibling_parent";
    final String $index  = "mergeRightSibling_index";
    final String $index1 = "mergeRightSibling_index1";
    final String $bs     = "mergeRightSibling_bs";
    final String $l      = "mergeRightSibling_l";
    final String $r      = "mergeRightSibling_r";
    final String $nl     = "mergeRightSibling_nl";
    final String $nr     = "mergeRightSibling_nr";
    final String $nlr    = "mergeRightSibling_nlr";
    final String $size   = "mergeRightSibling_size";
    final String $t      = "mergeRightSibling_t";
    final String $pk     = "mergeRightSibling_pk";
    final String $ld     = "mergeRightSibling_ld";
    L.clear(0, $mrs);                                                           // Assume at the start that it will  mnot be possible to merge with the right sibling

    L.new Block()
     {void code()
       {final Ban.Block finished = this;
        stuck_size1($bs, $P);
        L.compare($index, $bs); finished.endIfGe();                             // No right sibling

        setStuck ($P);
        setIndex ($index); stuck_elementAt(); getData($l);
        setIndexR($index); stuck_elementAt(); getData($r);

        L.move("hasLeavesForChildren", $P);
        hasLeavesForChildren();
        L.new Block()                                                           // Leaves for children
         {void code()
           {final Ban.Block leaves = this;
            L.get("hasLeavesForChildren"); leaves.endIfEq();
            L.move("leafSize", $l); leafSize(); L.move($nl, "leafSize");
            L.move("leafSize", $r); leafSize(); L.move($nr, "leafSize");

            L.move($nlr, $nl); L.add($nlr, $nr);
            L.compare(maxKeysPerLeaf, $nlr); finished.endIfGt();                // Combined body would be too big for one leaf

            stuck_size($size, $r);                                              // Number of entries to remove
            L.new Block()
             {void code()
               {final Ban.Block merge = this;
                for (int i = 0; i < maxKeysPerLeaf; i++)                        // Transfer right to left
                 {L.compare(i, $size); merge.endIfLe();
                  setStuck($r); stuck_shift();
                  setStuck($l); stuck_push();
                 }
               }
             };
           }
         };

        L.new Block()                                                           // Branches for children
         {void code()
           {final Ban.Block branches = this;
            L.get("hasLeavesForChildren"); branches.endIfGt();
            L.move("branchSize", $l); branchSize(); L.move($nl, "branchSize");
            L.move("branchSize", $r); branchSize(); L.move($nr, "branchSize");

            L.move($nlr, $nl); L.inc($nlr); L.add($nlr, $nr);
            L.compare(maxKeysPerBranch, $nlr); finished.endIfGt();              // Merge not possible because there is not enough room in a single branch

            setStuck($l); stuck_lastElement(); L.move($ld, "s_data");           // Last element of left child
            setStuck($P); setIndex($index); stuck_elementAt();                  // Parent dividing element
            setStuck($l); setData($ld); setIndex($nl); stuck_setElementAt();    // Re-key left top
            L.move($size, $nr); L.inc ($size);

            L.new Block()
             {void code()
               {final Ban.Block merge = this;
                for (int i = 0; i < maxKeysPerBranch; i++)                      // Transfer right to left
                 {L.compare(i, $size); merge.endIfLe();
                  setStuck($r); stuck_shift();
                  setStuck($l); stuck_push();
                 }
               }
             };
           }
         };

        free($r);                                                               // Free the empty right node

        setStuck($P);
        setIndexR($index); stuck_elementAt(); getKey($pk);                      // One up from dividing point in parent
        setIndex ($index); stuck_elementAt();                                   // Dividing point in parent
        setKey($pk); setIndex($index); stuck_setElementAt();                    // Install key of right sibling in this child
        setIndexR($index);             stuck_removeElementAt();                 // Reduce parent on right

        L.clear(1, "mergeRightSibling");
       }
     };
   }

//D2 Balance                                                                    // Balance the tree by merging and stealing

  void balance()                                                                // Augment the indexed child so it has at least two children in its body
   {final String $parent = "balance_parent";
    final String $index  = "balance_index";

    L.new Block()
     {void code()
       {final Ban.Block finished = this;
        setStuck($parent); setIndex($index); stuck_elementAt();
        L.move("isLow", "s_data");
        isLow();
        L.get("isLow");                                  finished.endIfEq();
        L.move("stealFromLeft_parent", $parent);
        L.move("stealFromLeft_index",  $index);
        stealFromLeft ();    L.get("stealFromLeft");     finished.endIfGt();

        L.move("stealFromRight_parent", $parent);
        L.move("stealFromRight_index",  $index);
        stealFromRight();    L.get("stealFromRight");    finished.endIfGt();

        L.move("mergeLeftSibling_parent", $parent);
        L.move("mergeLeftSibling_index",  $index);
        mergeLeftSibling();  L.get("mergeLeftSibling");  finished.endIfGt();

        L.move("mergeRightSibling_parent", $parent);
        L.move("mergeRightSibling_index",  $index);
        mergeRightSibling(); L.get("mergeRightSibling"); finished.endIfGt();
       }
     };
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
    setStuck(node); final int N = L.getMemory("current_size", "stuck");
    for (int i = 0; i < N; i++)                                                 // Each element in the leaf
     {setStuck(node); setIndex(i); stuck_elementAt();
      s.append(String.format("%d ", L.getMemory("s_key")));
     }
    if (s.length() > 0) s.setLength(s.length()-1);
    s.append(String.format("=%d ", node));
    padStrings(S);
   }

  void  printBranch(int node, StringBuilder[]S, int level)                      // Print a branch
   {setStuck(node);
    final int N = L.getMemory("current_size", "stuck")-1;
    for (int i = 0; i < N; i++)                                                 // Each element in the branch
     {setStuck(node); setIndex(i); stuck_elementAt();
      final int k = L.getMemory("s_key"), d = L.getMemory("s_data");
      L.set(d, "isALeaf");
      isLeaf();
      if (L.getMemory("isALeaf") > 0) printLeaf(d, S, level+linesPerNode);
      else         printBranch(d, S, level+linesPerNode);
      S[level+0].append(String.format("%d", k));
      S[level+1].append(String.format("%d", d));
      S[level+2].append(String.format("%d", node));
      S[level+3].append(String.format("%d", i));
      padStrings(S);
     }
    setStuck(node); setIndex(N); stuck_elementAt();

    final int k = L.getMemory("s_key"), d = L.getMemory("s_data");
    L.set(d, "isALeaf");
    isLeaf();
    if (L.getMemory("isALeaf") > 0) printLeaf  (d, S, level+linesPerNode);
    else                            printBranch(d, S, level+linesPerNode);
    S[level+0].append("+");
    S[level+1].append(String.format("%d", d));
    S[level+2].append(String.format("%d", node));
    S[level+3].append(String.format("%d", N));
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
   {final int N = linesPerTree*linesPerNode;                                    // A big buffer with room for several lines per node
    final StringBuilder [] S = new StringBuilder[N];                            // A big buffer with room for several lines per node
    for (int i = 0; i < N; i++) S[i] = new StringBuilder();
    rootIsLeaf();
    if (L.getMemory("rootIsLeaf") > 0) printLeaf  (0, S, 0);                    // Print tree
    else                               printBranch(0, S, 0);
    return printCollapsed(S);                                                   // Collapse lines into text
   }

  void print(String title)                                                      // Print the tree
   {say("Tree: %s", title);
    say(this);
   }

//D1 Find                                                                       // Find the data associated with a key

  void find_result                                                              // Find result
   (String Leaf, String Found, String Index, String Key, String Data)
   {L.move("f_leaf",  Leaf);
    L.move("f_found", Found);
    L.move("f_index", Index);
    L.move("f_key",   Key);
    L.move("f_data",  Data);
   }

  void findAndInsert_result(int Success, int Inserted)                          // Find and insert result
   {putFSuccess(Success);
    putFInserted(Inserted);
   }

  void find()                                                                   // Find the data associated with a key in the tree
   {final String $i   = "find_loop";                                            // Step down iterator
    final String $Key = "find_Key";
    rootIsLeaf();                                                               // The root is a leaf

    L.new Block()
     {void code()
       {final Ban.Block finished = this;
        L.new Block()
         {void code()
           {final Ban.Block root = this;
            L.get("rootIsLeaf"); root.endIfEq();
            setStuck(0); setKey($Key); stuck_search();
            find_result("root", "s_found", "s_index", "s_key", "s_data");
            finished.end();
           }
         };

        L.move("parent", "root");                                               // Parent starts at root which is known to be a branch

        L.clear(0, $i);                                                         // Step down iterator
        L.new Block()
         {void code()
           {final Ban.Block loop = this;
            setStuck("parent"); setKey($Key);
            stuck_searchFirstGreaterThanOrEqualExceptLast();
            getData("child");

            L.move("isALeaf", "child"); isLeaf();
            L.new Block()                                                       // Found the containing leaf
             {void code()
               {final Ban.Block leaf = this;
                L.get("isALeaf");  leaf.endIfEq();
                setStuck("child"); setKey($Key); stuck_search();

                L.move("find_result_leaf", "child");
                find_result("find_result_leaf", "s_found", "s_index", "s_key", "s_data");
                finished.end();
               }
             };
            L.move("parent", "child");                                          // Step down to lower branch
            L.inc($i); L.compare(maxDepth, $i); loop.endIfGe(); loop.start();
           }
         };
       }
     };
    stop("Search did not terminate in a leaf");
   }

  void findAndInsert()                                                          // Find the leaf that should contain this key and insert or update it is possible
   {final String $Key  = "findAndInsert_Key";
    final String $Data = "findAndInsert_Data";
    L.move("find_Key", "findAndInsert_Key");
    find();                                                                     // Find the leaf that should contain this key

    L.new Block()
     {void code()
       {final Ban.Block finished = this;
        L.new Block()
         {void code()
           {final Ban.Block found = this;
            L.get("f_found"); found.endIfEq();                                  // Found the key in the leaf so update it with the new data
            setStuck("f_leaf");  setKey($Key); setData($Data);
            setIndex("f_index"); stuck_setElementAt();
            findAndInsert_result(1, 0);
            finished.end();
           }
         };

        L.move("isFull", "f_leaf"); isFullLeaf();
        L.new Block()
         {void code()
           {final Ban.Block notFull = this;
            L.get("isFull"); notFull.endIfGt();                                 // Leaf is not full so we can insert immediately
            setStuck("f_leaf"); setKey($Key);
            stuck_searchFirstGreaterThanOrEqual();
            setStuck("f_leaf");
            setKey($Key); setData($Data);
            stuck_insertElementAt();
            findAndInsert_result(1, 0);
            finished.end();
           }
         };
        findAndInsert_result(0, 0);
       }
     };
   }

  //D1 Insertion                                                                // Insert a key, data pair into the tree or update and existing key with a new datum

  void put()                                                                    // Insert a key, data pair into the tree or update and existing key with a new datum
   {final String $i    = "put_loop";                                            // Step down iterator
    final String $Key  = "put_Key";
    final String $Data = "put_Data";
    L.move("findAndInsert_Key",  $Key);
    L.move("findAndInsert_Data", $Data);

    findAndInsert();                                                            // Try direct insertion with no modifications to the shape of the tree
    L.new Block()
     {void code()
       {final Ban.Block finished = this;
        getFSuccess(); finished.endIfGt();                                      // Inserted or updated successfully

        isFullRoot();
        L.new Block()
         {void code()
           {final Ban.Block fullRoot = this;
            L.get("isFullRoot"); fullRoot.endIfEq();                            // Start the insertion at the root, after splitting it if necessary
            rootIsLeaf();

            L.new Block()
             {void code()
               {final Ban.Block rootIsLeaf = this;
                L.get("rootIsLeaf"); rootIsLeaf.endIfEq();
                 splitLeafRoot();
               }
             };
            L.new Block()
             {void code()
               {final Ban.Block rootIsBranch = this;
                L.get("rootIsLeaf"); rootIsBranch.endIfGt();
                splitBranchRoot();
               }
             };

            findAndInsert();                                                    // Splitting the root might have been enough
            getFSuccess(); finished.endIfGt();                                  // Inserted or updated successfully
           }
         };

        L.move("parent", "root");                                               // Parent starts at root which is known to be a branch

        L.clear(0, $i);                                                         // Step down iterator
        L.new Block()
         {void code()                                                           // Step down from branch to branch through the tree until reaching a leaf repacking as we go
           {final Ban.Block loop = this;
            setStuck("parent"); setKey($Key);
            stuck_searchFirstGreaterThanOrEqualExceptLast();
            getData("child");

            L.move("isALeaf", "child");
            isLeaf();

            L.new Block()
             {void code()                                                       // Step down from branch to branch through the tree until reaching a leaf repacking as we go
               {final Ban.Block isLeaf = this;
                L.get("isALeaf"); isLeaf.endIfEq();                             // Reached a leaf
                L.move("splitLeaf_node",   "child");
                L.move("splitLeaf_parent", "parent");
                L.move("splitLeaf_index",  "s_index");
                splitLeaf();

                findAndInsert();
                L.set($Key, "merge_Key");
                merge();
                finished.end();
               }
             };

            L.move("isFull", "child");
            isFullBranch();
            L.new Block()
             {void code()                                                       // Step down from branch to branch through the tree until reaching a leaf repacking as we go
               {final Ban.Block isFullBranch = this;
                L.get("isFull"); isFullBranch.endIfEq();
                L.move("splitBranch_node",   "child");
                L.move("splitBranch_parent", "parent");
                L.move("splitBranch_index",  "s_index");
                splitBranch();                                                  // Split the child branch in the search path for the key from the parent so the the search path does not contain a full branch above the containing leaf
                setStuck("parent"); setKey($Key);
                stuck_searchFirstGreaterThanOrEqualExceptLast();
                getData("parent");
               }
             };

            L.new Block()
             {void code()                                                       // Step down from branch to branch through the tree until reaching a leaf repacking as we go
               {final Ban.Block isNotFullBranch = this;
                L.get("isFull"); isNotFullBranch.endIfGt();
                L.move("parent", "child");
               }
             };
            L.inc($i); L.compare(maxDepth, $i); loop.endIfGt(); loop.start();
           }
         };
        stop("Fallen off the end of the tree");                                 // The tree must be missing a leaf
       }
     };
   }

//D1 Deletion                                                                   // Delete a key, data pair from the tree

  void findAndDelete()                                                          // Delete a key from the tree and returns its data if present without modifying the shape of tree
   {final String $Key = "findAndDelete_Key";                                    // The key to find and delete
    L.set($Key, "find_Key");
    find();                                                                     // Find the key
    finished:
     {notFound:
       {if (getFound() > 0) break notFound;                                     // No such key
        findAndInsert_result(0, 0);
        break finished;
       }
      setStuck("f_leaf"); setIndex("f_index");                                  // Key, data pairs in the leaf
      stuck_removeElementAt();                                                  // Remove the key, data pair from the leaf
      L.set(1, "f_found");
      L.move("f_data", "s_data");
     }
   }


  void delete()                                                                 // Insert a key, data pair into the tree or update and existing key with a new datum
   {final String $i   = "delete_loop";                                          // Step down iterator
    final String $Key = "delete_Key";
    mergeRoot();
    L.new Block()
     {void code()
       {final Ban.Block finished = this;
        rootIsLeaf();                                                           // Find and delete directly in root as a leaf
        L.new Block()
         {void code()
           {final Ban.Block rootIsLeaf = this;
            L.get("rootIsLeaf"); rootIsLeaf.endIfEq();
            L.move("findAndDelete_Key", $Key);                                  // The key to find and delete
            findAndDelete();
            finished.end();
           }
         };

        L.move("parent", "root");                                               // Parent starts at root which is known to be a branch

        L.clear(0, $i);                                                         // Step down iterator
        L.new Block()
         {void code()                                                           // Step down from branch to branch through the tree until reaching a leaf repacking as we go
           {final Ban.Block loop = this;
            setStuck("parent"); setKey($Key);
            stuck_searchFirstGreaterThanOrEqualExceptLast();
            L.move("balance_parent", "parent");
            getIndex("balance_index");
            balance();                                                            // Make sure there are enough entries in the parent to permit a deletion

            setStuck("parent"); setKey($Key);
            stuck_searchFirstGreaterThanOrEqualExceptLast();

            getData("child");
            L.move("isALeaf", "child");
            isLeaf();
            L.new Block()
             {void code()                                                       // Step down from branch to branch through the tree until reaching a leaf repacking as we go
               {final Ban.Block isLeaf = this;
                L.get("isALeaf"); isLeaf.endIfEq();                             // Reached a leaf
                L.move("findAndDelete_Key", $Key);                              // The key to find and delete
                findAndDelete();
                L.move("merge_Key", $Key);
                merge();
                finished.end();
               }
             };
            L.move("parent", "child");
            L.inc($i); L.compare(maxDepth, $i); loop.endIfGt();
           }
         };
        stop("Fallen off the end of the tree");                                 // The tree must be missing a leaf
       }
     };
   }

//D1 Merge                                                                      // Merge along the specified search path

  void merge()                                                                  // Merge along the specified search path
   {final String $i   = "merge_loop";                                           // Step down iterator
    final String $j   = "merge_indices";                                        // Step down iterator
    final String $N   = "merge_indexLimit";                                     // Index iterator
    final String $Key = "merge_Key";
    mergeRoot();

    L.move("parent", "root");                                                   // Parent starts at root
    L.clear(0, $i);                                                             // Step down iterator

    L.new Block()
     {void code()
       {final Ban.Block finished = this;
        L.set(0, $i);

        L.new Block()
         {void code()
           {final Ban.Block loop = this;
            L.move("isALeaf", "parent"); isLeaf();
            L.get ("isALeaf"); finished.endIfGt();

            L.move("branchSize", "parent"); branchSize();
            L.move($N, "branchSize"); L.inc($N);
            L.set(0, $j);

            L.new Block()
             {void code()
               {final Ban.Block indices = this;
                L.compare($j, $N); indices.endIfGe();
                L.move("mergeLeftSibling_parent", "parent");
                L.set(0, $j);

                mergeLeftSibling();                                             // A successful merge of the left  sibling reduces the current index and the upper limit

                L.new Block()
                 {void code()
                   {final Ban.Block merged = this;
                    L.get("mergeLeftSibling"); merged.endIfEq();
                    L.dec($j);
                   }
                 };
                L.move("mergeRightSibling_parent", "parent");
                L.set($j, "mergeRightSibling_index");
                mergeRightSibling();                                            // A successful merge of the right sibling maintains the current position but reduces the upper limit
               }
             };
            setStuck("parent"); setKey($Key);
            stuck_searchFirstGreaterThanOrEqualExceptLast();
            getData("parent");
            L.inc($i); L.compare(maxDepth, $i); loop.endIfGe(); loop.start();
           }
         };
        stop("Fallen off the end of the tree");                                 // The tree must be missing a leaf
       }
     };
   }

//D1 Stucks                                                                     // Store data in each node

  void stuck_size (String target, String stuck)                                 // The current number of key elements in a stuck
   {L.move(target, "current_size", stuck);
   }

  void stuck_size1(String target, String stuck)                                 // The current number of key elements in a stuck minus one which makes it suitable for describing a branch
   {stuck_size(target, stuck); L.dec(target);
   }

  void stuck_key () {L.get("keys", "stuck", "s_index"); L.set("s_key");}        // Key from a stuck at indicated index
  void stuck_data() {L.get("data", "stuck", "s_index"); L.set("s_data");}       // Data from a stuck at indicated index

  void stuck_setKey () {L.get("s_key" ); L.set("keys", "stuck", "s_index");}    // Save a key  in a stuck at the specified index
  void stuck_setData() {L.get("s_data"); L.set("data", "stuck", "s_index");}    // Save a data in a stuck at the specified index

  void stuck_copyKey  (String T, String S) {L.move("keys", "keys", "stuck", T, "stuck", S);}
  void stuck_copyData (String T, String S) {L.move("data", "data", "stuck", T, "stuck", S);}

  void stuck_setKeyData()                                                       // Set key, data pair in a stuck
   {stuck_setKey ();
    stuck_setData();
   }

  void stuck_copyKeyData(String Target, String Source)                          // Copy key, data pair in a stuck
   {stuck_copyKey (Target, Source);
    stuck_copyData(Target, Source);
   }

  void stuck_inc  () {L.inc("current_size", "stuck");}                          // Increment the current size
  void stuck_dec  () {L.dec("current_size", "stuck");}                          // Decrement the current size
  void stuck_clear() {L.set( 0, "current_size", "stuck");}                      // Clear the stuck

  void stuck_push()                                                             // Push an element onto a stuck
   {L.move("s_index", "current_size", "stuck");
    stuck_setKeyData();
    stuck_inc();
   }

  void stuck_unshift()                                                          // Unshift an element onto a stuck
   {final String $i = "stuckUnshift_i";                                         // Index of upper element to move up to
    final String $I = "stuckUnshift_I";                                         // Index of lower element to be moved
    L.move($i, "current_size", "stuck");

    L.new Block()
     {void code()
       {final Ban.Block loop = this;                                            // Loop to move each element up in the stuck
        L.get($i); loop.endIfLe();
        L.move($I, $i); L.dec($I);
        stuck_copyKeyData($i, $I);
        L.dec($i);
        loop.start();
       }
     };
    L.set(0, "s_index");                                                        // Place new key, data pair at front
    stuck_setKeyData();
    stuck_inc();                                                                // Increase size of stuck
   }

  void stuck_pop()                                                              // Pop from a stuck
   {stuck_dec();
    L.move("s_index", "current_size", "stuck");
    stuck_elementAt();
   }

  void stuck_shift()                                                            // Shift off the first element
   {final String $i = "stuckShift_i";                                           // Index of upper element to move up to
    final String $I = "stuckShift_I";                                           // Index of lower element to be moved
    final String $N = "stuckShift_N";                                           // Loop limit
    L.clear(0, "s_index");
    stuck_elementAt();
    L.move($N, "current_size", "stuck");
    L.set(0, $i); L.set(1, $I);
    L.new Block()
     {void code()
       {final Ban.Block loop = this;                                            // Loop to move each element down in the stuck
        L.compare($I, $N); loop.endIfGe();
        stuck_copyKeyData($i, $I);
        L.inc($i); L.inc($I);
        loop.start();
       }
     };
    stuck_dec();                                                                // Reduce the size of the stuck
   }

  void stuck_elementAt()                                                        // Element at specified index
   {stuck_key ();
    stuck_data();
   }

  void stuck_setElementAt()                                                     // Set an element either in range or one above the current range
   {stuck_setKeyData();                                                         // Set key and data
    L.new Block()
     {void code()
       {final Ban.Block expand = this;                                          // If statement to expand size of stuck if necessary
        L.compare("s_index", "current_size", "stuck");
        expand.endIfLt();
        stuck_inc();
       }
     };
   }

  void stuck_insertElementAt()                                                  // Insert an element at the indicated location shifting all the remaining elements up one
   {final String $i = "stuckInsertElementAt_i";                                 // Index of upper element to move up to
    final String $I = "stuckInsertElementAt_I";                                 // Index of lower element to be moved
    final String $L = "stuckInsertElementAt_L";                                 // Loop limit
    L.move($L, "s_index");                                                      // Set limit
    L.move($i, "current_size", "stuck");                                                             // Set index
    L.move($I, $i); L.dec($I);                                                  // Element below index
    L.new Block()
     {void code()
       {final Ban.Block loop = this;                                            // Loop to move each element down in the stuck
        L.compare($i, $L); loop.endIfEq();
        stuck_copyKeyData($i, $I);
        L.dec($i); L.dec($I);
       }
     };
    stuck_setKeyData();                                                         // Insert new key, data pair in liberated slot
    stuck_inc();
   }

  void stuck_removeElementAt()                                                  // Remove the indicated element
   {final String $i = "stuckRemoveElementAt_i";                                 // Index of upper element to move up to
    final String $I = "stuckRemoveElementAt_I";                                 // Index of lower element to be moved
    final String $N = "stuckRemoveElementAt_N";                                 // Loop limit
    L.move($N, "current_size", "stuck");                                        // Size of stuck
    L.move($i, "s_index");                                                      // Set index
    L.move($I, $i); L.inc($I);                                                  // Set index of next element up
    stuck_elementAt();
    L.new Block()
     {void code()
       {final Ban.Block loop = this;                                            // Loop to move each element down in the stuck
        L.compare($I, $N); loop.endIfEq();
        stuck_copyKeyData($i, $I);                                              // Shift the stuck down one place
        L.inc($i); L.inc($I);                                                   // Shift the stuck down one place
       }
     };
    stuck_dec();
   }

  void stuck_firstElement()                                                     // First element
   {L.set(0, "s_index");
    stuck_elementAt();
   }

  void stuck_lastElement()                                                      // Last element
   {L.move("s_index", "current_size", "stuck");                                 // Size of stuck
    L.dec("s_index");                                                           // Last element
    stuck_elementAt();                                                          // Get least element
   }

//D1 Search                                                                     // Search a stuck.

  void stuck_search()                                                           // Search for an element within all elements of the stuck
   {final String $i = "s_index";                                                // Index of current element
    final String $N = "stuckSearch_N";                                          // Size of stuck

    L.set(0, "s_found");                                                        // Assume we will not find a match
    L.set(0, $i);                                                               // Start index
    L.move($N, "current_size", "stuck");                                        // Size of stuck

    L.new Block()                                                               // Return block
     {void code()
       {final Ban.Block finished = this;                                        // Return if a match is foumd
        L.new Block()
         {void code()
           {final Ban.Block loop = this;                                        // Loop to check each element
            L.compare($i, $N); loop.endIfGe();
            L.new Block()                                                       // Found a match
             {void code()
               {final Ban.Block found = this;
                L.compare("s_key", "keys", "stuck", $i); found.endIfNe();
                L.set(1, "s_found");
                stuck_elementAt();
                finished.end();
               }
             };
            L.inc($i); L.compare($i, $N); loop.endIfGe(); loop.start();
           }
         };
       }
     };
   }

  void stuck_searchFirstGreaterThanOrEqual()                                    // Find first key equal or greater than the search key
   {final String $i = "s_index";                                                // Index of current element
    final String $N = "stuckSearch_N";                                          // Size of stuck

    L.set(0, "s_found");                                                        // Assume we will not find a match
    L.set(0, $i);                                                               // Start index
    L.move($N, "current_size", "stuck");                                        // Size of stuck

    L.new Block()                                                               // Return block
     {void code()
       {final Ban.Block finished = this;                                        // Return if a match is foumd
        L.new Block()
         {void code()
           {final Ban.Block loop = this;                                        // Loop to check each element
            L.compare($i, $N); loop.endIfGe();
            L.new Block()                                                       // Found a match
             {void code()
               {final Ban.Block found = this;
                L.compare("s_key", "keys", "stuck", $i); found.endIfGt();       // Found a key in the stuck that is greater than or equal to the search key
                L.set(1, "s_found");
                stuck_elementAt();
                finished.end();
               }
             };
            L.inc($i); L.compare($i, $N); loop.endIfGe(); loop.start();
           }
         };
       }
     };
   }

  void stuck_searchFirstGreaterThanOrEqualExceptLast()                          // Find first key equal or greater than the search key
   {final String $i = "s_index";                                                // Index of current element
    final String $N = "stuckSearch_N";                                          // Size of stuck

    L.set(0, "s_found");                                                        // Assume we will not find a match
    L.set(0, $i);                                                               // Start index
    L.move($N, "current_size", "stuck");                                        // Size of stuck
    L.dec ($N);                                                                 // Size of stuck excluding top

    L.new Block()                                                               // Return block
     {void code()
       {final Ban.Block finished = this;                                        // Return if a match is foumd
        L.new Block()
         {void code()
           {final Ban.Block loop = this;                                        // Loop to check each element
            L.compare($i, $N); loop.endIfGe();
            L.new Block()                                                       // Found a match
             {void code()
               {final Ban.Block found = this;
                L.compare("s_key", "keys", "stuck", $i); found.endIfGt();       // Found a key in the stuck that is greater than or equal to the search key
                L.set(1, "s_found");
                stuck_elementAt();
                finished.end();
               }
             };
            L.inc($i); L.compare($i, $N); loop.endIfGe(); loop.start();
           }
         };
       }
     };
    stuck_elementAt();
   }

// Tests

//D1 Print                                                                      // Print a stuck

  String stuck_print()                                                          // Print a stuck
   {final StringBuilder s = new StringBuilder();
    int N = L.getMemory("current_size", "stuck");
    s.append(String.format("Stuck(size:%d)\n", N));
    for (int i = 0; i < N; i++)                                                 // Search
     {L.set(i, "s_index");
      stuck_elementAt();
      s.append(String.format("  %2d key: %2d data: %2d\n", i, L.getMemory("s_key"),   L.getMemory("s_data")));
     }
    return ""+s;
   }

  String stuck_print_result()                                                   // Print the result of a stuck operation
   {final StringBuilder s = new StringBuilder();
    s.append(String.format(" found: %d\n", L.getMemory("s_found")));
    s.append(String.format(" index: %d\n", L.getMemory("s_index")));
    s.append(String.format("   key: %d\n", L.getMemory("s_key")));
    s.append(String.format("  data: %d\n", L.getMemory("s_data")));;
    return ""+s;
   }

//D1 Tests                                                                      // Tests

//D2 Stuck                                                                      // Test stuck

  static BtreeBap stuck_test_load()
   {final BtreeBap b = new BtreeBap(3, 3, 1);
    b.L.loadArray("s_index", 0);
    b.L.loadArray("current_size", 1);
    b.L.loadArray("keys", 1, 2, 3);
    b.L.loadArray("data", 11, 22, 33, 44);

    ok(b.stuck_print(), """
Stuck(size:1)
   0 key:  1 data: 11
""");
    return b;
   }

  static BtreeBap stuck_test_push()
   {final BtreeBap b = stuck_test_load();

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
   {final BtreeBap b = stuck_test_push();
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
   {final BtreeBap b = stuck_test_push();
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

  static BtreeBap stuck_test_unshift()
   {final BtreeBap b = stuck_test_push();

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
   {final BtreeBap b = stuck_test_unshift();

    b.L.clear(1, "s_index");
    b.stuck_elementAt();

    ok(b.stuck_print_result(), """
 found: 0
 index: 1
   key: 1
  data: 11
""");
   }

  static BtreeBap stuck_test_insertElementAt()
   {final BtreeBap b = stuck_test_push();
    b.L.clear(1, "s_index"); b.L.set(3, "s_key"); b.L.set(33, "s_data");
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

  static BtreeBap stuck_test_remove_element_at()
   {final BtreeBap b = stuck_test_insertElementAt();
    b.L.clear(1, "s_index");
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

  static BtreeBap stuck_test_first_last()
   {final BtreeBap b = stuck_test_insertElementAt();

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

  static BtreeBap stuck_test_search()
   {final BtreeBap b = stuck_test_push();

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

  static BtreeBap stuck_test_search_greater_than_or_equal()
   {final BtreeBap b = new BtreeBap(3, 3, 1);
    b.L.loadArray("s_index", 0);
    b.L.loadArray("current_size", 3);
    b.L.loadArray("keys", 2, 4, 6);
    b.L.loadArray("data", 1, 3, 5, 7);

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

  static BtreeBap stuck_test_search_greater_than_or_equal_except_last()
   {final BtreeBap b = stuck_test_search_greater_than_or_equal();

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
   {final BtreeBap b = new BtreeBap(2, 3, 29);
    int N = 32;
    for (int i = 1; i <= N; i++)
     {b.L.set(i, "put_Key"); b.L.set(i, "put_Data");
      b.put();
     }
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
   {final BtreeBap b = new BtreeBap(2, 3, 29);
    int N = 32;
    for (int i = N; i > 0; i--)
     {b.L.set(i, "put_Key"); b.L.set(i, "put_Data");
      b.put();
     }
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
   {final BtreeBap b = new BtreeBap(2, 3, 100);
    int N = random_small.length;
    for (int i = 0; i < N; ++i)
     {b.L.set(random_small[i], "put_Key"); b.L.set(i, "put_Data");
      b.put();
     }
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

  static void test_delete_random_not_100()                                      // Produces the same answer as BtreeSF with a slightly different layout. Why?
   {final BtreeBap b = new BtreeBap(2, 3, 999);
    int N = random_large.length;
    for (int i = 0; i < N; ++i)
     {b.L.set(random_large[i], "put_Key"); b.L.set(i, "put_Data");
      b.put();
     }
    for (int i = 0; i < N; ++i)
     {final int r = random_large[i];
      if (r % 100 > 0)
       {b.L.set(r, "delete_Key");
        b.delete();
       }
     }
    //stop(b);
    ok(b, """
               1846             4249                  6600             +
               223              163                   733              638
               0                0                     0                0
               0                1                     2                3
700 1800=223         3700=163         5000 6600=733         9700=638
""");
   }

  static void test_delete_odd_ascending()
   {final BtreeBap b = new BtreeBap(2, 3, 30);
    int N = 32;
    for (int i = 1; i <= N; i++)
     {b.L.set(i, "put_Key"); b.L.set(i, "put_Data");
      b.put();
     }
    for (int i = 1; i <= N; i += 2)
     {b.L.set(i, "delete_Key");
      b.delete();
     }
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
   {final BtreeBap b = new BtreeBap(2, 3, 100);
    int N = 32;
    for (int i = 1; i <= N; i++)
     {b.L.set(i, "put_Key"); b.L.set(i, "put_Data");
      b.put();
     }
    for (int i = N; i > 0; i -= 2)
     {b.L.set(i, "delete_Key");
      b.delete();
     }
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
   {final BtreeBap b = new BtreeBap(2, 3, 100);
    int N = 64;
    for (int i = 1; i <= N; i++)
     {b.L.set(i, "put_Key"); b.L.set(i, "put_Data");
      b.put();
     }
    for (int i = 2; i <= N; i++)
     {b.L.set(i, "find_Key");
      b.find();
      if (b.getFound() > 0)
       {for (int j = 2*i; j <= N; j += i)
         {b.L.set(j, "delete_Key");
          b.delete();
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
    test_delete_random_not_100();
    test_delete_odd_ascending();
    test_delete_even_descending();
    test_primes();
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

/*
Phased transformation of normal Java into machine code
1. Remove return values and parameters by replacing them with layout variables.
2. Remove internal variables so we are left with set(get) and move as the data manipulation operations
3. Replace if statements with label: {}
4. Replace for loop conditions with interior break
*/
