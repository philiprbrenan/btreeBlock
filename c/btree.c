//------------------------------------------------------------------------------
// Node definition
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2025
//------------------------------------------------------------------------------
#define branch_maxSize1 3                                                       // The maximum number of keys in a branch stuck - must be odd
#define branch_maxSize (branch_maxSize1+1)                                      // The size of the branch stuck includes top which is always present
#define leaf_maxSize    2                                                       // The maximum number of entries in a leafstuck - must be even.

#include "branch.c"
#include "leaf.c"
#include "basics.c"

#define number_of_nodes 100                                                     // Number of nodes available for constructing a tree
#define maxDepth         10                                                     // Maximum depth of a tree
#define stringLength   4086                                                     // Maximum length of a string
#define gutter            2                                                     // Gutter between printed items
#define linesPerNode      4                                                     // Number of lines needed to print a node
#define linesPerTree (linesPerNode * maxDepth)                                  // Number of lines needed to print a tree
// try removing_Result

int debug = 1;                                                                  // Debugging when true

typedef struct                                                                  // Definition of a stuck
 {int free;                                                                     // Next element of free chain
  int isLeaf;                                                                   // A leaf if true
  union                                                                         // Data
   {Branch branch;
    Leaf   leaf;
   } branchOrLeaf;
 } Node;

typedef struct                                                                  // Definition of a stuck
 {int free;                                                                     // Head of the free chain
  Node nodes[number_of_nodes];                                                  // Number of nodes available
 } Btree;

Btree btree;                                                                    // The btree

void merge(int Key);
void clear(int n);

void initialize_free_chain()                                                    // Initialize free chain by putting all the nodes on the free chain except the root (which is permanently allocated at position 0) with the low nodes first to be allocated.
 {for (int i = number_of_nodes; i > 1; --i)
   {btree.nodes[i-2].free = i-1;
   }
  btree.free = 1;                                                               // First node to be allocated
  btree.nodes[0].free = 0;                                                      // Not on the free chain
 }

void create()                                                                   // Create an empty btree
 {for (int i = 0; i < number_of_nodes; ++i) clear(i);
  initialize_free_chain();                                                      // Initialize free chain by putting all the nodes on the free chain except the root (which is permanently allocated at position 0) with the low nodes first to be allocated.
  btree.nodes[0].isLeaf = 1;                                                    // The root starts as a leaf
 }

int allocate()                                                                  // Allocate a node
 {int f = btree.free;                                                           // Last freed node
  if (f == 0) stop("No more memory available");                                 // No more free nodes available
  btree.free = btree.nodes[f].free;                                             // Second to last freed node becomes head of the free chain
  btree.nodes[f].free = 0;                                                      // Not on the free chain
  clear(f);                                                                     // Clear the node
  return f;                                                                     // Return the node to be reused
 }

int isLeaf    (int n) {return btree.nodes[n].isLeaf;}                           // A leaf if true
int rootIsLeaf()      {return btree.nodes[0].isLeaf;}                           // The root is a leaf if true
void   setLeaf(int n) {       btree.nodes[n].isLeaf = 1;}                       // Set as leaf
void setBranch(int n) {       btree.nodes[n].isLeaf = 0;}                       // Set as branch
void   setLeafRoot () {       btree.nodes[0].isLeaf = 1;}                       // Set root as leaf
void setBranchRoot () {       btree.nodes[0].isLeaf = 0;}                       // Set root as branch

void assertLeaf  (int n) {if (!isLeaf(n)) stop("Leaf required");}
void assertBranch(int n) {if ( isLeaf(n)) stop("Branch required");}
void assertLeafRoot   () {if (!isLeaf(0)) stop("Leaf required");}
void assertBranchRoot () {if ( isLeaf(0)) stop("Branch required");}

void clear(int n)                                                               // Clear a new node to zeros ready for use
 {char *p = (char *)&btree.nodes[n].branchOrLeaf;
  int   L = sizeof(Leaf);
  int   B = sizeof(Branch);
  int   N = L > B ? L : B;
  for(int i = 0; i < N; ++i) *(p+i) = 0;                                        // Clear
 }

void erase(int n)                                                               // Clear a new node to ones as this is likely to create invalid values that will be easily detected in the case of erroneous frees
 {char *p = (char *)&btree.nodes[n].branchOrLeaf;
  int   L = sizeof(Leaf);
  int   B = sizeof(Branch);
  int   N = L > B ? L : B;
  for(int i = 0; i < N; ++i) *(p+i) = 1;                                        // Set
 }

void freeNode(int n)                                                            // Free a new node to make it available for reuse
 {if (!n) stop("Cannot free root");                                             // The root is never freed
  erase(n);                                                                     // Clear the node to encourage erroneous frees to do damage that shows up quickly.
  int f = btree.free;                                                           // Last freed node from head of free chain
  btree.free = n;                                                               // Chain this node in front of the last freed node
  btree.nodes[n].free = f;                                                       // Second to last freed node becomes head of the free chain
 }

Leaf   *leaf  (int n) {assertLeaf  (n); return &btree.nodes[n].branchOrLeaf.leaf;}   // Leaf
Branch *branch(int n) {assertBranch(n); return &btree.nodes[n].branchOrLeaf.branch;} // Branch

int leafSize  (int n) {assertLeaf  (n); return   leaf_size (leaf  (n));}        // Number of children in body of leaf
int branchSize(int n) {assertBranch(n); return branch_size1(branch(n));}        // Number of children in body of branch taking top for granted as it is always there

int splitLeafSize  () {return   leaf_maxSize  >> 1;}                            // The number of key, data pairs to split out of a leaf
int splitBranchSize() {return branch_maxSize1 >> 1;}                            // The number of key, next pairs to split out of a branch

int isFull(int n)                                                               // The node is full
 {return isLeaf(n) ? leafSize  (n) >= leaf_maxSize  :
                     branchSize(n) >= branch_maxSize1;                          // Allow for top
 }

int isFullRoot() {return isFull(0);}                                            // The node is full

int isLow(int n) {return (isLeaf(n) ? leafSize(n) : branchSize(n)) < 2;}        // The node is low on children making it impossible to merge two sibling children

int hasLeavesForChildren(int n)                                                 // The node has leaves for children
 {assertBranch(n);
  Branch *b = branch(n);
  int     f = branch_firstElement(b).data;
  return isLeaf(f);
 }

int top(int n)                                                                  // The top next element of a branch
 {assertBranch(n);
  Branch *b = branch(n);
  return branch_lastElement(b).data;
 }

int allocLeaf()   {int n = allocate(); setLeaf  (n); return n;}                 // Allocate leaf
int allocBranch() {int n = allocate(); setBranch(n); return n;}                 // Allocate branch

//D2 Split                                                                      // Split nodes in half to increase the number of nodes in the tree

void splitLeafRoot()                                                            // Split a leaf which happens to be a full root into two half full leaves while transforming the root leaf into a branch
 {assertLeafRoot();
  if (!isFullRoot()) stop("Root is not full");

  int L = allocLeaf(); Leaf *l = leaf(L);                                       // New left leaf
  int R = allocLeaf(); Leaf *r = leaf(R);                                       // New right leaf
                       Leaf *p = leaf(0);
  int sl = splitLeafSize();

  for (int i = 0; i < sl; i++)                                                  // Build left leaf from parent
   {Leaf_Result f = leaf_shift(p);
    leaf_push(l, f.key, f.data);
   }
  for (int i = 0; i < sl; i++)                                                  // Build right leaf from parent
   {Leaf_Result f = leaf_shift(p);
    leaf_push(r, f.key, f.data);
   }

  int first = leaf_firstElement(r).key;                                         // First of right leaf
  int last  = leaf_lastElement (l).key;                                         // Last of left leaf
  int kv    = (last + first) / 2;                                               // Mid key
  setBranchRoot();
  branch_clear(branch(0));                                                      // Clear the branch
  branch_push (branch(0), kv, L);                                               // Insert left leaf into root
  branch_push (branch(0), 0,  R);                                               // Insert right into root. This will be the top node and so ignored by search ... except last.
 }

void splitBranchRoot()                                                          // Split a branch which happens to be a full root into two half full branches while retaining the current branch as the root
 {assertBranchRoot();
  if (!isFullRoot()) stop("Root is not full");

  int L = allocBranch(); Branch *l = branch(L);                                 // New left branch
  int R = allocBranch(); Branch *r = branch(R);                                 // New right branch
                         Branch *p = branch(0);
  int sb = splitBranchSize();                                                   // Branch split size

  for (int i = 0; i < sb; i++)                                                  // Build left child from parent
   {Branch_Result f = branch_shift(p);
    branch_push(l, f.key, f.data);
   }
  Branch_Result pl = branch_shift(p);                                           // This key, next pair will be part of the root
  branch_push(l, 0, pl.data);                                                   // Becomes top and so ignored by search ... except last

  for(int i = 0; i < sb; i++)                                                   // Build right child from parent
   {Branch_Result f = branch_shift(p);
    branch_push(r, f.key, f.data);
   }

  Branch_Result pr = branch_shift(p);                                           // Top of root
  branch_push(r, 0, pr.data);                                                   // Becomes top and so ignored by search ... except last

  branch_clear(branch(0));                                                      // Refer to new branches from root
  branch_push (p, pl.key, L);
  branch_push (p, 0,      R);                                                   // Becomes top and so ignored by search ... except last
 }

void splitLeaf(int node, int parent, int index)                                 // Split a leaf which is not the root
 {assertLeaf(node);
  if (node == 0) stop("Cannot split root with this method");
  int S   = leafSize(node), I   = index;
  int nif = !isFull (node), pif = isFull(parent);
  if (nif)   stop("Leaf: %d is not full, but has: %d", node, S);
  if (pif)   stop("Parent: %d must not be full", parent);
  if (I < 0) stop("Index: %d too small",              I);
  if (I > S) stop("Index: %d too big for leaf with:", I, S);

  int p = parent;                                                               // Parent
  int l = allocLeaf();                                                          // New  split out leaf
  int r = node;                                                                 // Existing  leaf on right
  int sl = splitLeafSize();

  for (int i = 0; i < sl; i++)                                                  // Build left leaf
   {Leaf_Result f = leaf_shift(leaf(r));
    leaf_push(leaf(l), f.key, f.data);
   }
   int F = leaf_firstElement(leaf(r)).key;
   int L = leaf_lastElement (leaf(l)).key;
   int splitKey = (F + L) / 2;
   branch_insertElementAt(branch(p), splitKey, l, index);                       // Insert new key, next pair in parent
 }

void splitBranch(int node, int parent, int index)                               // Split a branch which is not the root by splitting right to left
 {assertBranch(node);
  int bs = branchSize(node), I = index, nd = node;
  int nif = !isFull(node), pif = isFull(parent);
  if (nd == 0) stop("Cannot split root with this method");
  if (nif)     stop("Branch:", node, "is not full, but", bs);
  if (pif)     stop("Parent:", parent, "must not be full");
  if (I < 0)   stop("Index", I, "too small in node:", node);
  if (I > bs)  stop("Index", I, "too big for branch with:",
                          bs, "in node:", node);

  int p = parent;
  int l = allocBranch();
  int r = node;
  int sb = splitBranchSize();

  for (int i = 0; i < sb; i++)                                                  // Build left branch from right
   {Branch_Result f = branch_shift(branch(r));
    branch_push(branch(l), f.key, f.data);
   }

  Branch_Result split = branch_shift(branch(r));                                // Build right branch
  branch_push(branch(l), 0, split.data);                                        // Becomes top and so is ignored by search ... except last
  branch_insertElementAt(branch(p), split.key, l, index);
 }

int stealFromLeft(int node, int index)                                          // Steal from the left sibling of the indicated child if possible to give to the right - Dennis Moore, Dennis Moore, Dennis Moore.
 {assertBranch(node);
  if (index == 0) return 0;
  if (index < 0)                stop("Index %d too small", index);
  if (index > branchSize(node)) stop("Index %d too big",   index);

  int P = node;
  int l = branch_elementAt(branch(P), index-1).data;
  int r = branch_elementAt(branch(P), index+0).data;

  if (hasLeavesForChildren(node))                                               // Children are leaves
   {int nl = leafSize(l);
    int nr = leafSize(r);

    if (nr >= leaf_maxSize) return 0;                                           // Steal not possible because there is no where to put the steal
    if (nl <= 1) return 0;                                                      // Steal not allowed because it would leave the leaf sibling empty

    Leaf_Result le = leaf_lastElement(leaf(l));
    leaf_insertElementAt(leaf(r), le.key, le.data, 0);                          // Increase right
    leaf_pop(leaf(l));                                                          // Reduce left
    int lk = leaf_elementAt(leaf(l), nl-2).key;                                 // Last key on left
    branch_setElementAt(branch(P), lk, l, index-1);                             // Swap key of parent
   }
  else                                                                          // Children are branches
   {int nl = branchSize(l);
    int nr = branchSize(r);

    if (nr >= branch_maxSize1) return 0;                                        // Steal not possible because there is no where to put the steal
    if (nl <= 1) return 0;                                                      // Steal not allowed because it would leave the left sibling empty

    Branch_Result t = branch_lastElement(branch(l));                            // Increase right with left top
    int key = branch_elementAt(branch(P), index).key;                           // Top key
    branch_insertElementAt(branch(r), key, t.data, 0);                          // Increase right with left top
    branch_pop(branch(l));                                                      // Remove left top
    Branch_Result b = branch_firstElement(branch(r));                           // Increase right with left top
    int pk = branch_elementAt(branch(P), index-1).key;                          // Parent key
    branch_setElementAt(branch(r), pk, b.data, 0);                              // Reduce key of parent of right
    int lk = branch_lastElement(branch(l)).key;                                 // Last left key
    branch_setElementAt(branch(P), lk, l, index-1);                             // Reduce key of parent of left
   }
  return 1;
 }

int stealFromRight(int node, int index)                                         // Steal from the right sibling of the indicated child if possible
 {assertBranch(node);
  if (index == branchSize(node)) return 0;
  if (index < 0)                 stop("Index %d too small", index);
  if (index >= branchSize(node)) stop("Index %d too big",   index);

  Branch_Result L = branch_elementAt(branch(node), index+0); int l = L.data;
  Branch_Result R = branch_elementAt(branch(node), index+1); int r = R.data;

  if (hasLeavesForChildren(node))                                               // Children are leaves
   {int nl = leafSize(l);
    int nr = leafSize(r);

    if (nl >= leaf_maxSize) return 0;                                           // Steal not possible because there is no where to put the steal
    if (nr <= 1) return 0;                                                      // Steal not allowed because it would leave the right sibling empty

    Leaf_Result f = leaf_firstElement(leaf(r));                                 // First element of right child
    leaf_push            (leaf(l),   f.key, f.data);                            // Increase left
    branch_setElementAt  (branch(node), f.key, l, index);                       // Swap key of parent
    leaf_removeElementAt(leaf(r), 0);                                           // Reduce right
   }
  else                                                                          // Children are branches
   {int nl = branchSize(l);
    int nr = branchSize(r);

    if (nl >= branch_maxSize1) return 0;                                        // Steal not possible because there is no where to put the steal
    if (nr <= 1) return 0;                                                      // Steal not allowed because it would leave the right sibling empty

    Branch_Result le = branch_lastElement(branch(l));                           // Last element of left child
    branch_setElementAt(branch(l), L.key, le.data, nl);                         // Left top becomes real
    Branch_Result fe = branch_firstElement(branch(r));                          // First element of  right child
    branch_push(branch(l), 0, fe.data);                                         // New top for left is ignored by search ,.. except last
    branch_setElementAt(branch(node), fe.key, l, index);                        // Swap key of parent
    branch_removeElementAt(branch(r), 0);                                       // Reduce right
   }
  return 1;
 }

//D2 Merge                                                                      // Merge two nodes together and free the resulting free node

  int mergeRoot()                                                               // Merge into the root
   {if (rootIsLeaf() || branchSize(0) > 1) return 0;

    int p = 0;
    int l = branch_firstElement(branch(p)).data;
    int r = branch_lastElement (branch(p)).data;

    if (hasLeavesForChildren(p))                                                // Leaves
     {if (leafSize(l) + leafSize(r) <= leaf_maxSize)
       {clear(p);
        int nl = leafSize(l);
        for (int i = 0; i < nl; ++i)
         {Leaf_Result f = leaf_shift(leaf(l));
          leaf_push(leaf(p), f.key, f.data);
         }
        int nr = leafSize(r);
        for (int i = 0; i < nr; ++i)
         {Leaf_Result f = leaf_shift(leaf(r));
          leaf_push(leaf(p), f.key, f.data);
         }
        setLeaf(p);
        freeNode(l);
        freeNode(r);
        return 1;
       }
     }
    else if (branchSize(l) + 1 + branchSize(r) <= branch_maxSize1)              // Branches
     {Branch_Result pkn = branch_firstElement(branch(p));
      clear(p);
      int nl = branchSize(l);
      for (int i = 0; i < nl; ++i)
       {Branch_Result f = branch_shift(branch(l));
        branch_push(branch(p), f.key, f.data);
       }
      int data = branch_lastElement(branch(l)).data;
      branch_push(branch(p), pkn.key, data);
      int nr = branchSize(r);
      for (int i = 0; i < nr; ++i)
       {Branch_Result f = branch_shift(branch(r));
        branch_push(branch(p), f.key, f.data);
       }
      int Data = branch_lastElement(branch(r)).data;                            // Top next
      branch_push(branch(p), 0, Data);                                          // Top so ignored by search ... except last
      freeNode(l);
      freeNode(r);
      return 1;
     }
    return 0;
   }

  int mergeLeftSibling(int parent, int index)                                     // Merge the left sibling
   {assertBranch(parent);
    if (index == 0) return 0;
    int bs = branchSize(parent);
    const char * bss = "for branch of size:";
    if (index < 0 ) stop("Index: %d too small%s%d", index, bss, bs);
    if (index > bs) stop("Index: %d too big%s%d",   index, bss, bs);
    if (bs    < 2 ) return 0;

    Branch_Result L = branch_elementAt(branch(parent), index-1);
    Branch_Result R = branch_elementAt(branch(parent), index-0);

    if (hasLeavesForChildren(parent))                                             // Children are leaves
     {int  l = L.data;
      int  r = R.data;
      int nl = leafSize(l);
      int nr = leafSize(r);

      if (nl + nr >= leaf_maxSize) return 0;                                    // Combined body would be too big

      int N = leaf_size(leaf(l));                                               // Number of entries to remove
      for (int i = 0; i < N; i++)                                               // Transfer left to right
       {Leaf_Result q = leaf_pop(leaf(l));
        leaf_insertElementAt(leaf(r), q.key, q.data, 0);
       }
      freeNode(l);                                                              // Free the empty left node
     }
    else                                                                        // Children are branches
     {int  l = L.data;
      int  r = R.data;
      int  nl = branchSize(l);
      int  nr = branchSize(r);

      if (nl + 1 + nr >= branch_maxSize1) return 0;                              // Merge not possible because there is not enough room for the combined result

      int t = branch_elementAt(branch(parent), index-1).key;                    // Top key
      Branch_Result le = branch_lastElement(branch(l));                         // Last element of left child
      branch_insertElementAt(branch(r), t, le.data, 0);                         // Left top to right

      branch_pop(branch(l));                                                    // Remove left top
      int N = branchSize(l);                                                    // Number of entries to remove
      for (int i = 0; i < N; i++)                                               // Transfer left to right
       {Branch_Result q = branch_pop(branch(l));
        branch_insertElementAt(branch(r), q.key, q.data, 0);
       }
      freeNode(l);                                                              // Free the empty left node
     }
    branch_removeElementAt(branch(parent), index-1);                            // Reduce parent on left
    return 1;
   }

  int mergeRightSibling(int parent, int index)                                    // Merge the right sibling
   {assertBranch(parent);
    int bs = branchSize(parent);
    const char *bss = "for branch of size:";
    if (index >= bs) return 0;
    if (index <  0 ) stop("Index %d too small%s%d", index, bss, bs);
    if (index >  bs) stop("Index %d too big%s%d",   index, bss, bs);
    if (bs < 2) return 0;

    Branch_Result L = branch_elementAt(branch(parent), index+0);
    Branch_Result R = branch_elementAt(branch(parent), index+1);

    if (hasLeavesForChildren(parent))                                           // Children are leaves
     {int  l = L.data;
      int  r = R.data;
      int  nl = leafSize(l);
      int  nr = leafSize(r);

      if (nl + nr > leaf_maxSize) return 0;                                     // Combined body would be too big for one leaf

      int N = leaf_size(leaf(r));                                               // Number of entries to remove
      for (int i = 0; i < N; i++)                                               // Transfer right to left
       {Leaf_Result q = leaf_shift(leaf(r));
        leaf_push(leaf(l), q.key, q.data);
       }
      freeNode(r);                                                              // Free the empty right node
     }
    else                                                                        // Children are branches
     {int  l = L.data;
      int  r = R.data;
      int  nl = branchSize(l);
      int  nr = branchSize(r);

      if (nl + 1 + nr >= branch_maxSize1) return 0;                              // Merge not possible because there is not enough room in a single branch
      Branch_Result le = branch_lastElement(branch(l));                         // Last element of left child
      Branch_Result ea = branch_elementAt(branch(parent), index);               // Parent dividing element
      branch_setElementAt(branch(l), ea.key, le.data, nl);                      // Re-key left top

      int N = branchSize(r);                                                    // Number of entries to remove
      for (int i = 0; i < N; i++)                                               // Transfer right to left
       {Branch_Result f = branch_shift(branch(r));
        branch_push(branch(l), f.key, f.data);
       }
      freeNode(r);                                                              // Free the empty right node
     }

    Branch_Result pkn = branch_elementAt(branch(parent), index+1);              // One up from dividing point in parent
    Branch_Result dkn = branch_elementAt(branch(parent), index);                // Dividing point in parent
    branch_setElementAt(branch(parent), pkn.key, dkn.data, index);              // Install key of right sibling in this child
    branch_removeElementAt(branch(parent), index+1);                            // Reduce parent on right
    return 1;
   }

//D2 Balance                                                                    // Balance the tree by merging and stealing

  void balance(int parent, int index)                                           // Augment the indexed child so it has at least two children in its body
   {assertBranch(parent);
    if (index < 0)                  stop("Index %d too small", index);
    if (index > branchSize(parent)) stop("Index %d too big",   index);
    //if (isLow(parent) && parent != 0)
    // {stop("Parent: %d must not be low on children", parent);
    // }

    Branch_Result p = branch_elementAt(branch(parent), index);

    if (!isLow(p.data)) return;
    if (stealFromLeft    (parent, index)) return;
    if (stealFromRight   (parent, index)) return;
    if (mergeLeftSibling (parent, index)) return;
    if (mergeRightSibling(parent, index)) return;
    //stop("Unable to balance child:", p.data);
   }

//D1 Print                                                                      // Print a BTree horizontally

  void padStrings(char **S)                                                     // Pad the strings at each level of the tree so we have a vertical face to continue with - a bit like Marc Brunel's tunneling shield
   {int L = 0;
    for (int i = 0; i < linesPerTree; ++i)                                      // Maximum advance so far
     {int l = strlen(S[i]); if (l > L) L = l;
     }
    L += gutter;                                                                // Gutter between printed items
    for (int i = 0; i < linesPerTree; ++i)                                      // Pasdd all strigns to maximum advance
     {int l = strlen(S[i]);
      for (int j = l; j < L; ++j) {S[i][j] = ' '; S[i][j+1] = 0;}
     }
   }

  void printLeaf(int node, char **S, int level)                                 // Print a leaf
   {char *s = S[level];
    int   L = leaf_size(leaf(node));
    for (int i = 0; i < L; i++)                                                 // Each element in the leaf
     {Leaf_Result r = leaf_elementAt(leaf(node), i);
      sprintf(s + strlen(s), "%d ", r.key);
     }
    s[strlen(s)-1] = 0;
    sprintf(s + strlen(s), "=%d ", node);
    s[strlen(s)-1] = 0;
    padStrings(S);
   }

  void printBranch(int node, char **S, int level)                               // Print a branch
   {int   L = branchSize(node);
    for (int i = 0; i < L; i++)                                                 // Each element in the branch
     {Branch_Result r = branch_elementAt(branch(node), i);
      if (isLeaf(r.data)) printLeaf(r.data, S, level+linesPerNode);
      else              printBranch(r.data, S, level+linesPerNode);
      sprintf(S[level+0] + strlen(S[level+0]), "%d", r.key);
      sprintf(S[level+1] + strlen(S[level+1]), "%d", r.data);
      sprintf(S[level+2] + strlen(S[level+2]), "%d", node);
      sprintf(S[level+3] + strlen(S[level+3]), "%d", i);
      padStrings(S);
     }
    //s[strlen(s)-1] = 0;
    Branch_Result r = branch_elementAt(branch(node), L);
    if (isLeaf(r.data)) printLeaf(r.data, S, level+linesPerNode);
    else              printBranch(r.data, S, level+linesPerNode);
    sprintf(S[level+1] + strlen(S[level+1]), "%d", r.data);
    sprintf(S[level+2] + strlen(S[level+2]), "%d", node);
    sprintf(S[level+3] + strlen(S[level+3]), "%d", L);
    //padStrings(S);
   }

  char *printCollapsed(char **S)                                                // Collapse horizontal representation into a string
   {int N = 0;
    for (int i = 0; i < linesPerTree; ++i)                                      // Remove trailing blanks
     {char *s = S[i];
      for (char *p = s + strlen(s)-1; p >= s && *p == ' '; --p) *p = 0;
      N +=strlen(s)+1;
     }
    char *t = (char *)calloc(N+1, 1);
    for (int i = 0; i < linesPerTree; ++i)                                      // Concatenate line representing the tree
     {char *s = S[i];
      if (!strlen(s)) continue;
      strcat(t, s);
      strcat(t, "\n");
     }
    return t;                                                                   // Printed tree
   }

  char *dump()                                                                  // Dump a tree horizontally
   {char *S = calloc(stringLength, linesPerTree*linesPerNode);                  // A big buffer with room for several lines per node
    char *T[linesPerTree];                                                      // Array of lines

    for (int i = 0; i < linesPerTree; ++i) T[i] = S + i * stringLength;         // Array of lines in big buffer
    if (rootIsLeaf()) printLeaf(0, T, 0); else printBranch(0, T, 0);            // Print tree
    char *r = printCollapsed(T);                                                // Collapse lines into text
    free(S);                                                                    // Free lines
    return r;                                                                   // Return text
   }

  void print(const char *title)                                                 // Print the tree
   {char *s = dump();
    say("Tree: %s", title);
    say(s);
    free(s);
   }

  void dumpTree(const char *s)                                                  // Dump the tree
   {say("Tree: %s", s);
    for (int i = 0; i < number_of_nodes; ++i)
     {if (btree.nodes[i].free) continue;                                        // On free chain
      if (isLeaf(i))
       {say("Leaf  : %2d  %d %s", i, btree.nodes[i].free, leaf_print(leaf(i)));
       }
      else
       {say("Branch: %2d  %d %s", i, btree.nodes[i].free, branch_print(branch(i)));
       }
     }
   }


//D1 Find                                                                       // Find the data associated with a key

typedef struct                                                                  // Definition of a stuck
 {int leaf;
  int found;
  int index;
  int key;
  int data;
 } Find_Result;

Find_Result find_result(int Leaf, int Found, int Index, int Key, int Data)      // Find result
 {Find_Result f;
  f.leaf = Leaf;
  f.found = Found; f.index = Index; f.key = Key; f.data = Data;
  return f;
 }

void print_find_result(Find_Result r)                                           // Print result
 {say("Find_Result leaf=%d,found=%d,index=%d,key=%d,data=%d",
      r.leaf, r.found, r.index, r.key, r.data);
 }

Find_Result find(int Key) __attribute__((noinline));                            // Find the data associated with a key in the tree

Find_Result find(int Key)                                                       // Find the data associated with a key in the tree
 {if (rootIsLeaf())                                                             // The root is a leaf
   {Leaf_Result f = leaf_search(leaf(0), Key);
    return find_result(0, f.found, f.index, Key, f.data);
   }

  int parent = 0;                                                               // Parent starts at root which is known to be a branch

  for (int i = 0; i < maxDepth; i++)                                            // Step down through tree
   {Branch_Result down =                                                        // Find next child in search path of key
      branch_searchFirstGreaterThanOrEqualExceptLast(branch(parent), Key);
    int n = down.data;

    if (isLeaf(n))                                                              // Found the containing search
     {Leaf_Result f = leaf_search(leaf(n), Key);
      return find_result(n, f.found, f.index, Key, f.data);
     }
    parent = n;                                                                 // Step down to lower branch
   }
  stop("Search did not terminate in a leaf");
  Find_Result f = {0,0,0,0,0};
  return f;
 }

typedef struct                                                                  // Insert the specified key and data into the tree if there is room in the target leaf,or update the key with the data if the key already exists
 {int  success;                                                                 // Inserted or updated if true
  int inserted;                                                                 // Inserted if true
  Find_Result found;
 } FindAndInsert_Result;

void print_findAndInsert_result(FindAndInsert_Result r)                         // Print result
 {Find_Result f = r.found;
   say("FindAndInsert_Result success=%d,inserted=%d,leaf=%d,found=%d,index=%d,key=%d,data=%d",
      r.success, r.inserted, f.leaf, f.found, f.index, f.key, f.data);
 }

FindAndInsert_Result findAndInsert_result                                       // Find result
 (Find_Result f, int Success, int Inserted)
 {FindAndInsert_Result i;
  i.found = f; i.success= Success; i.inserted = Inserted;
  return i;
 }

FindAndInsert_Result findAndInsert(int Key, int Data)                           // Find the leaf that should contain this key and insert or update it is possible
 {Find_Result f = find(Key);                                                    // Find the leaf that should contain this key

  if (f.found)                                                                  // Found the key in the leaf so update it with the new data
   {leaf_setElementAt(leaf(f.leaf), Key, Data, f.index);
    return findAndInsert_result(f, 1, 0);
   }

  if (!isFull(f.leaf))                                                          // Leaf is not full so we can insert immediately
   {Leaf_Result F = leaf_searchFirstGreaterThanOrEqual(leaf(f.leaf), Key);

    if (F.found)                                                                // Overwrite existing key
     {leaf_insertElementAt(leaf(f.leaf), Key, Data, f.index);
     }
    else                                                                        // Insert into position
     {leaf_push(leaf(f.leaf), Key, Data);
     }
    return findAndInsert_result(f, 1, 1);
   }
  return findAndInsert_result(f, 0, 0);
 }

//D1 Insertion                                                                  // Insert a key, data pair into the tree or update and existing key with a new datum

void put(int Key, int Data) __attribute__((noinline));                          // Insert a key, data pair into the tree or update and existing key with a new datum

void put(int Key, int Data)                                                     // Insert a key, data pair into the tree or update and existing key with a new datum
 {FindAndInsert_Result f = findAndInsert(Key, Data);                            // Try direct insertion with no modifications to the shape of the tree
  if (f.success) return;                                                        // Inserted or updated successfully

  if (isFullRoot())                                                             // Start the insertion at the root, after splitting it if necessary
   {if (rootIsLeaf())
     {splitLeafRoot();
     }
    else
     {splitBranchRoot();
     }

    FindAndInsert_Result F = findAndInsert(Key, Data);                          // Splitting the root might have been enough
    if (F.success) return;                                                      // Inserted or updated successfully
   }

  int p = 0;                                                                    // Step down the tree from the root

  for (int i = 0; i < maxDepth; i++)                                            // Step down from branch to branch through the tree until reaching a leaf repacking as we go
   {Branch_Result down =
      branch_searchFirstGreaterThanOrEqualExceptLast(branch(p), Key);
    int q = down.data;

    if (isLeaf(q))                                                              // Reached a leaf
     {splitLeaf(q, p, down.index);
      findAndInsert(Key, Data);
      merge(Key);
      return;
     }

    if (isFull(q))
     {splitBranch(q, p, down.index);                                            // Split the child branch in the search path for the key from the parent so the the search path does not contain a full branch above the containing leaf

      Branch_Result Down =                                                      // Step down again as the split will have altered the local layout
        branch_searchFirstGreaterThanOrEqualExceptLast(branch(p), Key);
      p = Down.data;
     }
    else
     {p = q;
     }
   }
  stop("Fallen off the end of the tree");                                       // The tree must be missing a leaf
 }

//D1 Deletion                                                                   // Delete a key, data pair from the tree

typedef struct                                                                  // Insert the specified key and data into the tree if there is room in the target leaf,or update the key with the data if the key already exists
 {int success;                                                                  // Inserted or updated if true
  int data;                                                                     // Inserted if true
  Find_Result found;
 } FindAndDelete_Result;

FindAndDelete_Result findAndDelete_result                                       // Find result
 (Find_Result f, int Success, int Data)
 {FindAndDelete_Result i;
  i.found = f; i.success= Success; i.data = Data;
  return i;
 }

FindAndDelete_Result findAndDelete(int Key)                                     // Delete a key from the tree and returns its data if present without modifying the shape of tree
 {Find_Result f = find(Key);                                                    // Find the key
  if (!f.found) return findAndDelete_result(f, 0, 0);                           // No such key
  int l = f.leaf;                                                               // The leaf that contains the key
  int i = f.index;                                                              // Position in the leaf of the key
  Leaf_Result kd = leaf_elementAt(leaf(l), i);                                  // Key, data pairs in the leaf
  leaf_removeElementAt(leaf(l), i);                                             // Remove the key, data pair from the leaf
  return findAndDelete_result(f, 1, kd.data);
 }

FindAndDelete_Result delete(int Key)  __attribute__((noinline));                // Insert a key, data pair into the tree or update and existing key with a new datum

FindAndDelete_Result delete(int Key)                                            // Insert a key, data pair into the tree or update and existing key with a new datum
 {mergeRoot();

  if (rootIsLeaf())                                                             // Find and delete directly in root as a leaf
   {return findAndDelete(Key);
   }

  int p = 0;                                                                    // Start at root

  for (int i = 0; i < maxDepth; i++)                                            // Step down from branch to branch through the tree until reaching a leaf repacking as we go
   {Branch_Result down =
      branch_searchFirstGreaterThanOrEqualExceptLast(branch(p), Key);

    balance(p, down.index);                                                     // Make sure there are enough entries in the parent to permit a deletion
    int q = down.data;

    if (isLeaf(q))                                                              // Reached a leaf
     {FindAndDelete_Result r = findAndDelete(Key);
      merge(Key);
      return r;
     }
    p = q;
   }
  stop("Fallen off the end of the tree");                                       // The tree must be missing a leaf
  FindAndDelete_Result r = {0,0,{0,0,0,0,0}};
  return r;
 }

//D1 Merge                                                                      // Merge along the specified search path

void merge(int Key)                                                             // Merge along the specified search path
 {mergeRoot();
  int p = 0;                                                                    // Start at root

  for (int i = 0; i < maxDepth; i++)                                            // Step down from branch to branch through the tree until reaching a leaf repacking as we go
   {if (isLeaf(p)) return;

    for (int j = 0; j < branchSize(p); j++)                                     // Try merging each sibling pair which might change the size of the parent
     {if (mergeLeftSibling(p, j)) --j;                                          // A successful merge of the left  sibling reduces the current index and the upper limit
//if (debug) print("BBBB2222");
      mergeRightSibling(p, j);                                                  // A successful merge of the right sibling maintains the current position but reduces the upper limit
//if (debug) print("BBBB3333");
     }

    Branch_Result down =
      branch_searchFirstGreaterThanOrEqualExceptLast(branch(p), Key);
    p = down.data;
   }
  stop("Fallen off the end of the tree");                                       // The tree must be missing a leaf
 }

//D1 Tests                                                                      // Build some trees and test them

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int tests_passed = 0;
int tests_failed = 0;

void ok(const char *name, const char *g, const char *e)                         // Test got versus expected
 {int c = strcmp(g, e);

  int G = strlen(g), E = strlen(e);
  if (G != E)
   {say("%s: strings have different lengths, got: %d, expected %d", name, G, E);
    ++tests_failed;
   }

  if (c == 0)
   {++tests_passed;
    return;
   }
  ++tests_failed;
  printf("Test: %s failed\n", name);
 }

#define random_small_size   104
#define random_large_size  1000

int random_small [random_small_size] = {27, 442, 545, 317, 511, 578, 391, 993, 858, 586, 472, 906, 658, 704, 882, 246, 261, 501, 354, 903, 854, 279, 526, 686, 987, 403, 401, 989, 650, 576, 436, 560, 806, 554, 422, 298, 425, 912, 503, 611, 135, 447, 344, 338, 39, 804, 976, 186, 234, 106, 667, 494, 690, 480, 288, 151, 773, 769, 260, 809, 438, 237, 516, 29, 376, 72, 946, 103, 961, 55, 358, 232, 229, 90, 155, 657, 681, 43, 907, 564, 377, 615, 612, 157, 922, 272, 490, 679, 830, 839, 437, 826, 577, 937, 884, 13, 96, 273, 1, 188};
int random_large [random_large_size] = {5918,5624,2514,4291,1791,5109,7993,60,1345,2705,5849,1034,2085,4208,4590,7740,9367,6582,4178,5578,1120,378,7120,8646,5112,4903,1482,8005,3801,5439,4534,9524,6111,204,5459,248,4284,8037,5369,7334,3384,5193,2847,1660,5605,7371,3430,1786,1216,4282,2146,1969,7236,2187,136,2726,9480,5,4515,6082,969,5017,7809,9321,3826,9179,5781,3351,4819,4545,8607,4146,6682,1043,2890,2964,7472,9405,4348,8333,2915,9674,7225,4743,995,1321,3885,6061,9958,3901,4710,4185,4776,5070,8892,8506,6988,2317,9342,3764,9859,4724,5195,673,359,9740,2089,9942,3749,9208,1,7446,7023,5496,4206,3272,3527,8593,809,3149,4173,9605,9021,5120,5265,7121,8667,6911,4717,2535,2743,1289,1494,3788,6380,9366,2732,1501,8543,8013,5612,2393,7041,3350,3204,288,7213,1741,1238,9830,6722,4687,6758,8067,4443,5013,5374,6986,282,6762,192,340,5075,6970,7723,5913,1060,1641,1495,5738,1618,157,6891,173,7535,4952,9166,8950,8680,1974,5466,2383,3387,3392,2188,3140,6806,3131,6237,6249,7952,1114,9017,4285,7193,3191,3763,9087,7284,9170,6116,3717,6695,6538,6165,6449,8960,2897,6814,3283,6600,6151,4624,3992,5860,9557,1884,5585,2966,1061,6414,2431,9543,6654,7417,2617,878,8848,8241,3790,3370,8768,1694,9875,9882,8802,7072,3772,2689,5301,7921,7774,1614,494,2338,8638,4161,4523,5709,4305,17,9626,843,9284,3492,7755,5525,4423,9718,2237,7401,2686,8751,1585,5919,9444,3271,1490,7004,5980,3904,370,5930,6304,7737,93,5941,9079,4968,9266,262,2766,4999,2450,9518,5137,8405,483,8840,2231,700,8049,8823,9811,9378,3811,8074,153,1940,1998,4354,7830,7086,6132,9967,5680,448,1976,4101,7839,3122,4379,9296,4881,1246,4334,9457,5401,1945,9548,8290,1184,3464,132,2458,7704,1056,7554,6203,2270,6070,4889,7369,1676,485,3648,357,1912,9661,4246,1576,1836,4521,7667,6907,2098,8825,7404,4019,8284,3710,7202,7050,9870,3348,3624,9224,6601,7897,6288,3713,932,5596,353,2615,3273,833,1446,8624,2489,3872,486,1091,2493,4157,3611,6570,7107,9153,4543,9504,4746,1342,9737,3247,8984,3640,5698,7814,307,8775,1150,4330,3059,5784,2370,5248,4806,6107,9700,231,3566,5627,3957,5317,5415,8119,2588,9440,2961,9786,4769,466,5411,3080,7623,5031,2378,9286,4801,797,1527,2325,847,6341,5310,1926,9481,2115,2165,5255,5465,5561,3606,7673,7443,7243,8447,2348,7925,6447,8311,6729,4441,7763,8107,267,8135,9194,6775,3883,9639,612,5024,1351,7557,9241,5181,2239,8002,5446,747,166,325,9925,3820,9531,5163,3545,558,7103,7658,5670,8323,4821,6263,7982,59,3700,1082,4474,4353,8637,9558,5191,842,5925,6455,4092,9929,9961,290,3523,6290,7787,8266,7986,7269,6408,3620,406,5964,7289,1620,6726,1257,1993,7006,5545,2913,5093,5066,3019,7081,6760,6779,7061,9051,8852,8118,2340,6596,4594,9708,8430,8659,8920,9268,5431,9203,2823,1427,2203,6422,6193,5214,9566,8791,4964,7575,4350,56,2227,8545,5646,3089,2204,4081,487,8496,2258,4336,6955,3452,556,8602,8251,8569,8636,9430,1025,9459,7137,8392,3553,5945,9414,3078,1688,5480,327,8117,2289,2195,8564,9423,103,7724,3091,8548,7298,5279,6042,2855,3286,3542,9361,420,7020,4112,5320,5366,6379,114,9174,9744,592,5346,3985,3174,5157,9890,1605,3082,8099,4346,7256,8670,5687,6613,6620,1458,1045,7917,2980,2399,1433,3315,4084,178,7056,2132,2728,4421,9195,4181,6017,6229,2945,4627,2809,8816,6737,18,8981,3813,8890,5304,3789,6959,7476,1856,4197,6944,9578,5915,3060,9932,3463,67,7393,9857,5822,3187,501,653,8453,3691,9736,6845,1365,9645,4120,2157,8471,4436,6435,2758,7591,9805,7142,7612,4891,7342,5764,8683,8365,2967,6947,441,2116,6612,1399,7585,972,6548,5481,7733,7209,222,5903,6161,9172,9628,7348,1588,5992,6094,7176,4214,8702,2987,74,8486,9788,7164,5788,8535,8422,6826,1800,8965,4965,565,5609,4686,2556,9324,5000,9809,1994,4737,63,8992,4783,2536,4462,8868,6346,5553,3980,2670,1601,4272,8725,4698,7333,7826,9233,4198,1997,1687,4851,62,7893,8149,8015,341,2230,1280,5559,9756,3761,7834,6805,9287,4622,5748,2320,1958,9129,9649,1644,4323,5096,9490,7529,6444,7478,7044,9525,7713,234,7553,9099,9885,7135,6493,9793,6268,8363,2267,9157,9451,1438,9292,1637,3739,695,1090,4731,4549,5171,5975,7347,5192,5243,1084,2216,9860,3318,5594,5790,1107,220,9397,3378,1353,4498,6497,5442,7929,7377,9541,9871,9895,6742,9146,9409,292,6278,50,5288,2217,4923,6790,4730,9240,3006,3547,9347,7863,4275,3287,2673,7485,1915,9837,2931,3918,635,9131,1197,6250,3853,4303,790,5548,9993,3702,2446,3862,9652,4432,973,41,3507,8585,2444,1633,956,5789,1523,8657,4869,8580,8474,7093,7812,2549,7363,9315,6731,1130,7645,7018,7852,362,1636,2905,8006,4040,6643,8052,7021,3665,8383,715,1876,2783,3065,604,4566,8761,7911,1983,3836,5547,8495,8144,1950,2537,8575,640,8730,8303,1454,8165,6647,4762,909,9449,8640,9253,7293,8767,3004,4623,6862,8994,2520,1215,6299,8414,2576,6148,1510,313,3693,9843,8757,5774,8871,8061,8832,5573,5275,9452,1248,228,9749,2730};

void test_put_ascending()
 {create();
  int N = 32;
  for (int i = 1; i <= N; i++) put(i, i);
  char *g = dump();
  //say(g);exit(0);
  char *e =
"                                      8                                                     16\n"
"                                      13                                                    21                                                                                                             16\n"
"                                      0                                                     0                                                                                                              0\n"
"                                      0                                                     1                                                                                                              2\n"
"                 4                                              12                                                      20                          24                          28\n"
"                 5                    9                         12                          17                          20                          24                          27                         6\n"
"                 13                   13                        21                          21                          16                          16                          16                         16\n"
"                 0                    1                         0                           1                           0                           1                           2                          3\n"
"       2                    6                     10                          14                          18                          22                          26                          30\n"
"       1         3          4         7           8             10            11            15            14            18            19            22            23            25            26           2\n"
"       5         5          9         9           12            12            17            17            20            20            24            24            27            27            6            6\n"
"       0         1          0         1           0             1             0             1             0             1             0             1             0             1             0            1\n"
"1 2=1     3 4=3      5 6=4     7 8=7      9 10=8      11 12=10      13 14=11      15 16=15      17 18=14      19 20=18      21 22=19      23 24=22      25 26=23      27 28=25      29 30=26      31 32=2\n"
;
  ok("test_ascending", g, e);
 }

void test_put_descending()
 {create();
  int N = 32;
  for (int i = N; i > 0; --i) put(i, i);
  char *g = dump();
  //say(g);exit(0);
  char *e =
"                                                                                                   16                                                    24\n"
"                                                                                                   19                                                    11                                                  13\n"
"                                                                                                   0                                                     0                                                   0\n"
"                                                                                                   0                                                     1                                                   2\n"
"                    4                       8                          12                                                      20                                                  28\n"
"                    27                      24                         20                          17                          12                        9                         5                         6\n"
"                    19                      19                         19                          19                          11                        11                        13                        13\n"
"                    0                       1                          2                           3                           0                         1                         0                         1\n"
"        2                       6                        10                          14                          18                         22                        26                        30\n"
"        28          25          23          21           22            18            16            14            15            10           8            7            4            3            2            1\n"
"        27          27          24          24           20            20            17            17            12            12           9            9            5            5            6            6\n"
"        0           1           0           1            0             1             0             1             0             1            0            1            0            1            0            1\n"
"1 2=28      3 4=25      5 6=23      7 8=21      9 10=22      11 12=18      13 14=16      15 16=14      17 18=15      19 20=10      21 22=8      23 24=7      25 26=4      27 28=3      29 30=2      31 32=1\n"
;
  ok("test_descending", g, e);
 }

void test_put_random_small()
 {create();
  int N = random_small_size;
  for (int i = 0; i < N; ++i) put(random_small[i], i);
  char *g = dump();
  //say(g);exit(0);
  char *e =
"                                                                                                                                                                                                                                                    281                                                                                                                                                                                                       493                                                                                                                                                                                                                                                                             785\n"
"                                                                                                                                                                                                                                                    70                                                                                                                                                                                                        9                                                                                                                                                                                                                                                                               87                                                                                                                                                                                                  2\n"
"                                                                                                                                                                                                                                                    0                                                                                                                                                                                                         0                                                                                                                                                                                                                                                                               0                                                                                                                                                                                                   0\n"
"                                                                                                                                                                                                                                                    0                                                                                                                                                                                                         1                                                                                                                                                                                                                                                                               2                                                                                                                                                                                                   3\n"
"                                                          55                                                                                          190                                                                                                                                                                          379                                                                                                                                                                                                                 561                                                                                                                                                                                                                                                                     882\n"
"                                                          90                                                                                          53                                                                                            35                                                                             36                                                                                                                         20                                                                                       50                                                                                                                                                                                     26                                                                               85                                                                                                                 15\n"
"                                                          70                                                                                          70                                                                                            70                                                                             9                                                                                                                          9                                                                                        87                                                                                                                                                                                     87                                                                               2                                                                                                                  2\n"
"                                                          0                                                                                           1                                                                                             2                                                                              0                                                                                                                          1                                                                                        0                                                                                                                                                                                      1                                                                                0                                                                                                                  1\n"
"                                 33                                                                120                                                                                               253                                                                         335                                                                            416                                            439                                                                                                        528                                                                         598                           622                                                         681                                                                                          830                                                                                                   949\n"
"                                 94                       71                                       60                                                 44                                             67                                             34                           65                                                13                           30                                             80                                             5                                                           48                           16                                             88                            46                                                          54                                            10                                             83                                24                                                                  42                                             6\n"
"                                 90                       90                                       53                                                 53                                             35                                             35                           36                                                36                           20                                             20                                             20                                                          50                           50                                             26                            26                                                          26                                            26                                             85                                85                                                                  15                                             15\n"
"                                 0                        1                                        0                                                  1                                              0                                              1                            0                                                 1                            0                                              1                                              2                                                           0                            1                                              0                             1                                                           2                                             3                                              0                                 1                                                                   0                                              1\n"
"        7          20                          47                    72            96                               153              160                               233          235                               270              275                           307                          349              365                          397                              429              437                           457          476                               502          506              518                       552                          570              577                               613                       654              662          674                               695          738                              807          819                               856                               904              909              929                               981          988\n"
"        96         95            92            75         64         62            93              72               73               45               38               69           68               58               56               91           14               52          3                40               66              1            23              8                32               84           31               37           81               28               49           21               59           17           12              4            89               77               27               78           11           47               74           82               22               51           55              7                57           86               39               29               19               76               33               79               18               63           43               25\n"
"        94         94            94            71         71         60            60              60               44               44               44               67           67               67               34               34           34               65          65               13               13              13           30              30               80               80           80               5            5                5                48           48               48           48           16              16           88               88               88               46           46           54               54           54               54               10           10              10               83           83               83               24               24               42               42               42               42               6            6                6\n"
"        0          1             2             0          1          0             1               2                0                1                2                0            1                2                0                1            2                0           1                0                1               2            0               1                0                1            2                0            1                2                0            1                2            3            0               1            0                1                2                0            1            0                1            2                3                0            1               2                0            1                2                0                1                0                1                2                3                0            1                2\n"
"0 1=96      13=95      27 29=92      39 43=75      55=64      72=62      90 96=93      103 106=72       135 151=73       155 157=45       186 188=38       229 232=69       234=68       237 246=58       260 261=56       272 273=91       279=14       288 298=52       317=3       338 344=40       354 358=66       376 377=1       391=23       401 403=8       422 425=32       436 437=84       438=31       442 447=37       472=81       480 490=28       494 501=49       503=21       511 516=59       526=17       545=12       554 560=4       564=89       576 577=77       578 586=27       611 612=78       615=11       650=47       657 658=74       667=82       679 681=22       686 690=51       704=55       769 773=7       804 806=57       809=86       826 830=39       839 854=29       858 882=19       884 903=76       906 907=33       912 922=79       937 946=18       961 976=63       987=43       989 993=25\n"
;
  ok("test_put_random_small", g, e);
 }

void test_delete_odd_ascending()
 {create();
  int N = 64;
  for (int i = 1; i <= N; i++)    put(i, i);
  for (int i = 1; i <= N; i += 2) delete(i);
  char *g = dump();
  //say(g);exit(0);
  char *e =
"                                                                         16                                                                                      32\n"
"                                                                         37                                                                                      46                                                                                                                                                                             35\n"
"                                                                         0                                                                                       0                                                                                                                                                                              0\n"
"                                                                         0                                                                                       1                                                                                                                                                                              2\n"
"                              8                                                                                      24                                                                                      40                                                                52\n"
"                              13                                         21                                          28                                          34                                          44                                                                52                                                               16\n"
"                              37                                         37                                          46                                          46                                          35                                                                35                                                               35\n"
"                              0                                          1                                           0                                           1                                           0                                                                 1                                                                2\n"
"             4                                     12                                          20                                          28                                          36                                          44                    48                                          56                    60\n"
"             5                9                    12                    17                    20                    24                    27                    33                    36                    40                    43                    48                    51                    55                    58                   6\n"
"             13               13                   21                    21                    28                    28                    34                    34                    44                    44                    52                    52                    52                    16                    16                   16\n"
"             0                1                    0                     1                     0                     1                     0                     1                     0                     1                     0                     1                     2                     0                     1                    2\n"
"     2                6                 10                    14                    18                    22                    26                    30                    34                    38                    42                    46                    50                    54                    58                    62\n"
"     1       3        4       7         8          10         11         15         14         18         19         22         23         25         26         29         30         31         32         38         39         41         42         45         47         49         50         53         54         56         57        2\n"
"     5       5        9       9         12         12         17         17         20         20         24         24         27         27         33         33         36         36         40         40         43         43         48         48         51         51         55         55         58         58         6         6\n"
"     0       1        0       1         0          1          0          1          0          1          0          1          0          1          0          1          0          1          0          1          0          1          0          1          0          1          0          1          0          1          0         1\n"
"2=1     4=3      6=4     8=7      10=8      12=10      14=11      16=15      18=14      20=18      22=19      24=22      26=23      28=25      30=26      32=29      34=30      36=31      38=32      40=38      42=39      44=41      46=42      48=45      50=47      52=49      54=50      56=53      58=54      60=56      62=57      64=2\n"
;
  ok("test_ascending", g, e);
 }

void test_delete_even_descending()
 {create();
  int N = 64;
  for (int i = 1; i <= N; i++)   put(i, i);
  for (int i = N; i > 0; i -= 2) delete(i);
  char *g = dump();
  //say(g);exit(0);
  char *e =
"                                                                                                                                          28                                                                                      44\n"
"                                                                                                                                          37                                                                                      46                                                                                                           35\n"
"                                                                                                                                          0                                                                                       0                                                                                                            0\n"
"                                                                                                                                          0                                                                                       1                                                                                                            2\n"
"                                                  12                                          20                                                                                      36                                                                                      52\n"
"                                                  13                                          21                                          28                                          34                                          44                                          52                                                               16\n"
"                                                  37                                          37                                          37                                          46                                          46                                          35                                                               35\n"
"                                                  0                                           1                                           2                                           0                                           1                                           0                                                                1\n"
"             4                8                                         16                                          24                                          32                                          40                                          48                                          56                    60\n"
"             5                9                   12                    17                    20                    24                    27                    33                    36                    40                    43                    48                    51                    55                    58                   6\n"
"             13               13                  13                    21                    21                    28                    28                    34                    34                    44                    44                    52                    52                    16                    16                   16\n"
"             0                1                   2                     0                     1                     0                     1                     0                     1                     0                     1                     0                     1                     0                     1                    2\n"
"     2                6                10                    14                    18                    22                    26                    30                    34                    38                    42                    46                    50                    54                    58                    62\n"
"     1       3        4       7        8          10         11         15         14         18         19         22         23         25         26         29         30         31         32         38         39         41         42         45         47         49         50         53         54         56         57        2\n"
"     5       5        9       9        12         12         17         17         20         20         24         24         27         27         33         33         36         36         40         40         43         43         48         48         51         51         55         55         58         58         6         6\n"
"     0       1        0       1        0          1          0          1          0          1          0          1          0          1          0          1          0          1          0          1          0          1          0          1          0          1          0          1          0          1          0         1\n"
"1=1     3=3      5=4     7=7      9=8      11=10      13=11      15=15      17=14      19=18      21=19      23=22      25=23      27=25      29=26      31=29      33=30      35=31      37=32      39=38      41=39      43=41      45=42      47=45      49=47      51=49      53=50      55=53      57=54      59=56      61=57      63=2\n"
;
  ok("test_ascending", g, e);
 }

int tests()                                                                     // Tests
 {test_put_ascending();
  test_put_descending();
  test_put_random_small();
  test_delete_odd_ascending();
  test_delete_even_descending();

  if (1)
   {int p = tests_passed, f = tests_failed, n = p + f;

    if      (f == 0 && p > 0) {printf("Passed all %d tests\n", n);                        return 0;}
    else if (          f > 0) {printf("FAILed %d, passed %d tests out of %d\n", f, p, n); return f;}
    else                      {printf("No tests run\n");                                  return 1;}
   }
  return 0;
 }

int main() {return tests();}
