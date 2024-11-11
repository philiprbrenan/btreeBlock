//------------------------------------------------------------------------------
// BTree in a block
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout a btree in a block on the surface of a silicon chip.

import java.util.*;

class Btree extends Test                                                        // Manipulate a btree
 {final int
    bitsPerKey,                                                                 // Size of a key in bits
    bitsPerData,                                                                // Size of data associated with key in bits
    bitsPerNext,                                                                // Bits in a next reference
    maxKeysPerLeaf,                                                             // Maximum number of leafs in a key
    maxKeysPerBranch,                                                           // Maximum number of keys in a branch
    maxKeysInLeaves,                                                            // Maximum number of keys in the leaves under one branch
    maxKeysInBranches,                                                          // Maximum number of keys in the branches under one branch
    splitLeafSize,                                                              // The number of key, data pairs to split out of a leaf
    splitBranchSize,                                                            // The number of key, next pairs to split out of a branch
    treeSize;                                                                   // Number of leaves or branches in tree

  final int                 root = 0;                                           // The root is always at zero
  final Stack<Integer>  freeList = new Stack<>();                               // Free leaf or branches
  final BranchOrLeaf []    nodes;                                               // The leaves or branches comprising the tree

  final static int
    linesToPrintABranch =  4,                                                   // The number of lines required to print a branch
         maxPrintLevels = 10,                                                   // Maximum number of levels to print in a tree
               maxDepth =  6;                                                   // Maximum depth of any realistic tree

  int       maxNodeUsed = 0;;                                                   // Maximum branch or leaf index used
  static boolean debug  = false;                                                // Debugging enabled

//D1 Construction                                                               // Create a BTree from nodes which can be branches or leaves.  The data associated with the BTree is stored only in the leaves opposite the keys

  Btree                                                                         // Define a BTree with the specified dimensions
   (int BitsPerKey, int BitsPerData, int BitsPerNext, int MaxKeysPerLeaf,
    int MaxKeysPerBranch, int TreeSize)
   {z();
    if (powerTwo(BitsPerNext) < TreeSize)
     {stop("Bits per next:", BitsPerNext, "too small for size:", TreeSize);
     }
    bitsPerKey        = BitsPerKey;
    bitsPerData       = BitsPerData;
    bitsPerNext       = BitsPerNext;
    maxKeysPerLeaf    = MaxKeysPerLeaf;
    maxKeysPerBranch  = MaxKeysPerBranch;
    splitLeafSize     = maxKeysPerLeaf   >> 1;
    splitBranchSize   = maxKeysPerBranch >> 1;
    treeSize          = TreeSize;
    maxKeysInLeaves   = maxKeysPerLeaf   * (1 + maxKeysPerBranch);              // Including top
    maxKeysInBranches = maxKeysPerBranch * (1 + maxKeysPerBranch);              // Including top

    nodes = new BranchOrLeaf[TreeSize];                                         // Pre-allocate leaves and branches
    for (int i = treeSize; i > 0; i--)                                          // Initially all of the leaves or branches are on the free list, except for the root at index 0
     {if (i-1 > 0) freeList.push(i-1);
      nodes[i-1]       = new BranchOrLeaf(i);
      nodes[i-1].state = BranchOrLeaf.State.neverUsed;
     }
    nodes[0].state = BranchOrLeaf.State.leaf;                                   // Start with the root as a leaf
   }

//D1 Control                                                                    // Testing, control and integrity

  void ok(String expected) {Test.ok(print(), expected);}                        // Confirm tree is as expected
  void stop()              {Test.stop(toString());}                             // Stop after printing the tree
  public String toString() {return print();}                                    // Print the tree

  class CheckFreeList                                                           // Check for memory leaks
   {CheckFreeList()
     {final Set<Integer> s = active();                                          // Active branches and leaves

      for (int i = 0; i < treeSize; i++)                                        // Make sure that all allocated branches and leaves that are accessible from the root of the tree
       {BranchOrLeaf n = nodes[i];
        if      (n.state.leaf())                                                // Leaf
         {if (!s.contains(i)) err("Inaccessible leaf", i);
         }
        else if (n.state.branch())                                              // Branch
         {if (!s.contains(i)) err("Inaccessible branch", i);
         }
        else                                                                    // Free or never used
         {if (s.contains(i))  err("Freed but in use", i);
         }
       }

      for (int i : freeList)                                                    // Make sure that all items in the free list are not accessible from the root of the tree
       {BranchOrLeaf n = nodes[i];
        if      (n.state.leaf())                                                // Leaf
         {if (s.contains(i)) stop("Using free leaf", i);
         }
        else if (n.state.branch())                                              // Branch
         {if (s.contains(i)) stop("Using free branch", i);
         }
       }
     }

    Set<Integer> active()                                                       // Active branches and leaves
     {final Set<Integer> s = new TreeSet<>();                                   // The set of active branches and leaves
      s.add(0);
      active(s, 0);
      return s;
     }

    void active(Set<Integer> s, int index)                                      // Active branches and leaves
     {if (nodes[index].state.leaf())                                            // Add leaf
       {s.add(index);
       }
      else if (nodes[index].state.branch())                                     // Add branch and its children
       {s.add(index);
        final Branch    B = new Branch(index);
        final int       N = B.branchSize().asInt();
        final KeyNext[]kn = B.keyNext();
        for (int i = 0; i < N; i++)                                             // Each child of branch
         {active(s, kn[i].next.asInt());
         }
        active(s, B.top().asInt());                                             // Add top
       }
     }
   }

  void checkFreeList() {new CheckFreeList();}                                   // Check the free list is consistent with the tree

//D1 Allocate and Free                                                          // Allocate and free branches and leaves allowing memory to be recycled

  int allocate()                                                                // Allocate a branch or leaf
   {z();
    if (freeList.size() < 1) stop("No more leaves or branches available");
    z();
    final int index = freeList.pop();
    final BranchOrLeaf bl = nodes[index];
    if (bl.state.leaf() || bl.state.branch())
     {stop("Attempting to allocate an already allocated node:", index);
      }
    maxNodeUsed = max(maxNodeUsed, index);
    return index;
   }

  void free(int index)                                                          // Free the indicated branch or leaf
   {z();
    final BranchOrLeaf bl = nodes[index];
    if (!bl.state.leaf() && !bl.state.branch())
     {stop("Attempting to free an already freed node:", index);
     }
    freeList.push(index);
   }

//D1 Components                                                                 // A branch or leaf in the tree

  class BranchOrLeaf                                                            // A branch or leaf in a btree
   {final  int i;                                                               // The index of this branch or leaf int the block of memory assigned to the tree

    enum State                                                                  // The current state of the branch or leaf: as a leaf, as a branch or free waiting for use
     {leaf, branch, free, neverUsed;
      boolean leaf     () {return this == leaf;}
      boolean branch   () {return this == branch;}
      boolean free     () {return this == free;}
      boolean neverUsed() {return this == neverUsed;}
     }
    State               state;                                                  // Whether this branch or leaf is a branch, a leaf or free
    final LKDIndex   leafSize = new LKDIndex();                                 // Number of key, data pairs currently contained in leaf if a leaf
    final BKNIndex branchSize = new BKNIndex();                                 // Number of key, next pairs currently contained in branch if a branch
    final KeyData[]   keyData = new KeyData[maxKeysPerLeaf];                    // Key, data pairs for when a leaf
    final KeyNext[]   keyNext = new KeyNext[maxKeysPerBranch];                  // Key, next pairs for when a branch
    Next                  top = new Next();                                     // Top next reference for when a branch

    BranchOrLeaf(int I)                                                         // Memory for branches and leaves
     {z(); i = I;
      for (int i = 0; i < maxKeysPerLeaf;   i++) keyData[i] = new KeyData();
      for (int i = 0; i < maxKeysPerBranch; i++) keyNext[i] = new KeyNext();
     }
   }

  class Leaf                                                                    // Describe a leaf
   {final int index;                                                            // Index of the leaf
    Leaf()                                                                      // Get a leaf off the free list
     {z(); if (freeList.size() < 1) stop("No more leaves available");
      z(); index = allocate();
      nodes[index].state = BranchOrLeaf.State.leaf;
      zeroLeafSize();
     }
    Leaf(int n)                                                                 // Access a leaf by number
     {z(); index = n; assertLeaf();
     }
    Leaf(Next n)                                                                // Access a leaf by index
     {z(); index = n.asInt(); assertLeaf();
     }
    Leaf(boolean root)                                                          // Access the root as a leaf
     {z(); if (!nodes[0].state.leaf()) stop("Root is not a leaf");
      z(); index = 0;
     }

    void assertLeaf()                                                           // Confirm that we are on a leaf
     {z(); if (!state().leaf()) stop("Not a leaf at node:", index);
      z();
     }

    void freeLeaf()                                                             // Free this leaf
     {z();
      assertLeaf();
      z();
      if (index == 0) stop("Cannot free root currently a leaf");
      z();
      free(index);
      nodes[index].state = BranchOrLeaf.State.free;
     }

    BranchOrLeaf.State state() {z(); return nodes[index].state;   }             // State of this memory
    LKDIndex        leafSize() {z(); return nodes[index].leafSize;}             // Number of key, data pairs in leaf
    KeyData[]        keyData() {z(); return nodes[index].keyData; }             // Key, data pairs in leaf

    void setLeafSize(int size) {z(); leafSize().set(size);}                     //Set the size of the leaf

    void copy(Leaf source)                                                      // Copy a leaf
     {z();
      final KeyData[]skd = source.keyData();                                    // Source key, data pairs
      final KeyData[]tkd = keyData();                                           // Target key, data pairs
      final int N = source.leafSize().asInt();
      for (int i = 0; i < N; i++)
       {z();
        tkd[i].set(skd[i].key);
        tkd[i].set(skd[i].data);
       }
      setLeafSize(N);
     }

    public String toString()                                                    // Print leaf
     {final StringBuilder s = new StringBuilder();
      s.append("Leaf(index:"+index+" state:"+state());
      s.append(" size:"+leafSize()+")\n");

      final KeyData[]kd = keyData();
      final int N = leafSize().asInt();
      for (int i = 0; i < N; i++)
       {s.append("  "+i+" key:"+kd[i].key+" next:"+kd[i].data+"\n");
       }
      return s.toString();
     }

    void  incLeafSize() {z(); leafSize().inc();}                                // Increase the number of key, data pairs in the leaf
    void  decLeafSize() {z(); leafSize().dec();}                                // Decrease the number of key, data pairs in the leaf
    void zeroLeafSize() {z(); leafSize().zero();}                               // Set the size of the leaf to zero, effectively clearing it
    boolean    isFull() {z(); return leafSize().asInt() == maxKeysPerLeaf;}     // Whether the leaf is full or not

    void update(LKDIndex at, Data Data)                                         // Update the key, data pair at the specified index with the data value
     {z(); keyData()[at.asInt()].set(Data);
     }

    Branch makeIntoBranch()                                                     // Transform this leaf into a branch
     {z();
      nodes[index].state = BranchOrLeaf.State.branch;                           // Make this leaf into a branch
      return new Branch(index);                                                 // Return leaf as a branch
     }

    Key smallestKey()                                                           // Smallest key in a leaf
     {z();
      final KeyData[]kd = keyData();                                            // Key, data pairs in leaf
      final int L = leafSize().asInt();                                         // Size of leaf
      if (L > 0) return kd[0].key;                                              // Return first key if there is one
      stop("No keys in leaf", index);
      return null;
     }

    void push(Key Key, Data Data)                                               // Push a new key, data pair on into the leaf
     {z();
      final KeyData kd = keyData()[leafSize().asInt()];                         // Next key, data pair
      kd.set(Key);
      kd.set(Data);
      incLeafSize();                                                            // Increase leaf  size
     }

    void push(int Key, int Data)                                                // Push a new key, data pair of integers into the leaf
     {z();
      push(new Key(Key), new Data(Data));
     }

    class FindEqual                                                             // Find the first key in the leaf that is equal to the search key
     {final Key     search;                                                     // Search key
      final boolean  found;                                                     // Whether the key was found
      final Data      data;                                                     // Data associated with the  key
      final LKDIndex index;                                                     // Index of first such key if found

      FindEqual(Key Search)                                                     // Find the first key in the leaf that is equal to the search key
       {z();
        if (!state().leaf()) stop("Leaf required, not", state());
        z();
        search = Search;
        boolean looking = true;
        final KeyData[]kd = keyData();
        int i;
        final int N = leafSize().asInt();
        for (i = 0; i < N && looking; i++)                                      // Compare with each valid key
         {z();
          if (kd[i].key.equals(search))
           {z(); looking = false; break;
           }
         }
        if (looking)                                                            // Not found
         {z(); data  = null; index = null; found = false;
         }
        else                                                                    // Found
         {z(); data = keyData()[i].data; index = new LKDIndex(i); found = true;
         }
       }

      Next next() {z(); return new Next(Leaf.this.index);}                      // Index of the containing leaf

      public String toString()
       {final StringBuilder s = new StringBuilder();
        s.append("Key:"+search+" found:"+printBit(found));
        if (found) s.append(" data:"+data+" index:"+index);
        return s.toString();
       }
     }

    class FindFirstGreaterThanOrEqual                                           // Find the first key in the leaf that is equal to or greater than the search key
     {final Key     search;                                                     // Search key
      final boolean  found;                                                     // Whether the key was found
      final LKDIndex first;                                                     // Index of first such key if found
      FindFirstGreaterThanOrEqual(Key Search)                                   // Find the first key in the  leaf that is equal to or greater than the search key
       {z(); if (!state().leaf()) stop("Leaf required, not", state());
        z(); search = Search;
        boolean looking = true;
        final KeyData[]kd = keyData();
        int i;
        final int N = leafSize().asInt();
        for (i = 0; i < N && looking; i++)                                      // Search the key, data pairs int the leaf
         {z();
          if (kd[i].key.greaterThanOrEqual(search))
           {z(); looking = false; break;
           }
         }
        if (looking)                                                            // Key not found
         {z(); found = false; first = null;
         }
        else                                                                    // Key found
         {z(); found = true;  first = new LKDIndex(i);
         }
       }
      public String toString()                                                  // Print results of search
       {final StringBuilder s = new StringBuilder();
        s.append("Key:"+search+" found:"+printBit(found));
        if (found) s.append(" first:"+first);
        return s.toString();
       }
     }

    Branch splitLeafRoot()                                                      // Split a leaf which happens to be a full root into two half full leaves while transforming the root leaf into a branch
     {z(); if (!state().leaf()) stop("Leaf required, not", state());
      z(); if (index != 0) stop("Not root, but", index);
      final KeyData[]kd = keyData();
      z(); if (!isFull()) stop("Root is not full, but", leafSize());
      z();
      final Branch R = makeIntoBranch();                                        // Current leaf becomes the new root branch
      R.zeroBranchSize();

      final Leaf l = new Leaf(); final KeyData[]lkd = l.keyData();
      final Leaf r = new Leaf(); final KeyData[]rkd = r.keyData();
      for (int i = 0; i < splitLeafSize; i++)                                   // Build left leaf
       {z(); l.push(kd[i].key, kd[i].data);
       }
      R.push(kd[splitLeafSize-1].key, l);                                       // Insert left leaf into root
      for (int i = splitLeafSize; i < maxKeysPerLeaf;  i++)                     // Build right leaf
       {z(); r.push(kd[i].key, kd[i].data);
       }
      R.top(r);                                                                 // Insert right leaf into root
      return R;                                                                 // Return root
     }

    void print(Stack<StringBuilder>S, int level)                                // Print leaf horizontally
     {padStrings(S, level);
      final StringBuilder s = new StringBuilder();                              // String builder
      final int K = leafSize().asInt();
      for  (int i = 0; i < K; i++) s.append(""+keyData()[i].key+",");
      if (s.length() > 0) s.setLength(s.length()-1);                            // Remove trailing comma if present
      s.append("="+index+" ");
      S.elementAt(level*linesToPrintABranch).append(s.toString());
      padStrings(S, level);
     }
   }

  class Branch                                                                  // Describe a branch
   {final int index;                                                            // Index of the branch
    Branch()                                                                    // get a new branch off the free list
     {z(); if (freeList.size() < 1) stop("No more branches");
      z(); index = allocate();
      z(); nodes[index].state = BranchOrLeaf.State.branch;
      z(); zeroBranchSize();
     }
    Branch(int n)                                                               // Access the specified branch
     {z(); index = n; assertBranch();
     }
    Branch(Next n)                                                              // Access the branch at this index
     {z(); index = n.asInt(); assertBranch();
     }
    Branch(boolean root)                                                        // The root is always zero
     {z(); if (!nodes[0].state.branch()) stop("Root is not a branch");
      z(); index = 0;
     }

    void freeBranch()                                                           // Free this branch
     {z(); assertBranch();
      z(); if (index == 0) stop("Cannot free root currently as branch");
      z(); free(index);
      z(); nodes[index].state = BranchOrLeaf.State.free;
     }

    void assertBranch()
     {z(); if (!state().branch()) stop("Not a branch at node:", index);
      z();
     }

    BranchOrLeaf.State state() {z(); return nodes[index].state;     }
    BKNIndex      branchSize() {z(); return nodes[index].branchSize;}
    KeyNext[]        keyNext() {z(); return nodes[index].keyNext;   }
    Next                 top() {z(); return nodes[index].top;       }
    boolean        hasLeaves() {z(); return isLeaf(top());          }
    boolean isFull          ()
     {z(); return branchSize().asInt() == maxKeysPerBranch;
     }

    void incBranchSize      () {z(); branchSize().inc(); }
    void decBranchSize      () {z(); branchSize().dec(); }
    void zeroBranchSize     () {z(); branchSize().zero();}

    void setBranchSize(int size) {z(); branchSize().set(size);}

    void copy(Branch source)                                                    // Copy a branch
     {final KeyNext[]skn = source.keyNext();                                    // Source key, next pairs
      final KeyNext[]tkn = keyNext();                                           // Target key, next pairs
      final int N = source.branchSize().asInt();
      for (int i = 0; i < N; i++)                                               // Copy in each key, next pair
       {tkn[i] = new KeyNext(skn[i].key, skn[i].next);
       }
      top(source.top());                                                        // Copy top next from source
      setBranchSize(N);                                                         // Set size of branch
     }

    public String toString()                                                    // Print branch
     {final StringBuilder s = new StringBuilder();
      s.append("Branch(index:"+index+" state:"+state());
      s.append(" size:"+branchSize());
      s.append(" top:"+top().asInt()+")\n");
      final KeyNext[]kn = keyNext();
      final int N = branchSize().asInt();
      for (int i = 0; i < N; i++)
       {s.append("  "+i+" key:"+kn[i].key+" next:"+kn[i].next+"\n");
       }
      return s.toString();
     }

    Leaf makeIntoLeaf()                                                         // Transform this branch into leaf
     {z(); nodes[index].state = BranchOrLeaf.State.leaf;                        // Make this branch into a leaf
      return new Leaf(index);                                                   // Return leaf as a branch
     }

    void push(Key Key, Leaf Leaf)                                               // Push a leaf into a branch
     {z();
      final int n = index, i = branchSize().asInt();
      if (i >= maxKeysPerBranch) stop("Branch", n, " is already full");
      z();
      nodes[n].keyNext[i].key.set(Key);
      nodes[n].keyNext[i].next.set(Leaf.index);
      incBranchSize();
     }

    void push(Key Key, Branch Branch)                                           // Push a branch into a leaf
     {z();
      final int n = index, i = branchSize().asInt();
      if (0 == Branch.index) stop("Cannot push root into a branch");
      z();
      if (n == Branch.index) stop("Cannot push branch into self");
      z();
      if (i >= maxKeysPerBranch) stop("Branch", n, " is already full");
      z();
      nodes[n].keyNext[i].key .set(Key);
      nodes[n].keyNext[i].next.set(Branch.index);
      incBranchSize();
     }

    void top(Leaf   Leaf)   {z(); nodes[index].top = new Next(Leaf.index);}     // Set top of this branch to refer to a leaf
    void top(Branch Branch) {z(); nodes[index].top = new Next(Branch.index);}   // Set top of this branch to refer to a branch

    int countChildLeafKeys()                                                    // Find the number of keys in the immediate children of this branch
     {z();
      final KeyNext[]kn = keyNext();
      final int       N = branchSize().asInt();
      int             C = 0;
      for (int i = 0; i < N; i++)
       {z(); C += new Leaf(kn[i].next).leafSize().asInt();
       }
      C += new Leaf(top()).leafSize().asInt();
      return C;
     }

    Leaf pushNewLeaf()                                                          // Create a new child leaf and push it onto the parent branch
     {z();
      final int       p = branchSize().asInt();                                 // Current size of branch
      final KeyNext[]kn = keyNext();                                            // Key ], next pairs for this branch
      if (p < maxKeysPerBranch)                                                 // Room in the parent branch to push this new child leaf
       {z();
        final Leaf l = new Leaf();                                              // New leaf
        kn[p] = new KeyNext(new Key(0), new Next(l.index));                     // Push child leaf into parent branch
        incBranchSize();                                                        // New size of parent branch
        return l;                                                               // Return leaf so created
       }
      else                                                                      // Reuse branch top because there is no more room in the parent branch
       {z();
        final Leaf l = new Leaf(top());                                         // Return top as a leaf
        l.zeroLeafSize();
        return l;                                                               // Return top as a leaf
       }
     }

    void unpackLeaf(Leaf leaf, Stack<KeyData> up)                               // Unpack a leaf
     {z();
      final KeyData[]kd = leaf.keyData();                                       // Key, data pairs in leaf
      final int L = leaf.leafSize().asInt();                                    // Size of leaf
      for (int l = 0; l < L; l++)                                               // Unpack each key, data pair in the leaf
       {z(); up.push(new KeyData(kd[l]));                                       // Unpack the leaf into a stack of key, data pairs
       }
     }

    class UnpackLeaves                                                          // Unpack the leaves
     {final KeyNext    []  kn;                                                  // Key, next pairs associated with the parent branch
      final Stack<KeyData> up;                                                  // Save area for key, next pairs during repack
      final int B;                                                              // Current size of parent branch
      UnpackLeaves()                                                            // Unpack the keys in the leaves under the parent branch
       {z();
        kn = keyNext();                                                         // Key, next pairs associated with the parent branch
        up = new Stack<>();                                                     // Save area for key, next pairs during repack
         B = branchSize().asInt();                                              // Current size of parent branch
        for  (int b = 0; b < B; b++)                                            // Unpack each leaf referenced
         {z();
          unpackLeaf(new Leaf(kn[b].next), up);                                 // Unpack leaf
         }
        unpackLeaf(new Leaf(top()), up);                                        // Unpack top

        for  (int b = 0; b < B; b++)                                            // Free all the existing leaves except top which will always be there
         {z(); new Leaf(kn[b].next).freeLeaf();                                 // Free leaf
         }
        setBranchSize(0);                                                       // No key, next pairs in use at the moment
        new Leaf(top()).zeroLeafSize();                                         // Clear leaf referenced by top
       }
     }

    void repackLeaves(Stack<KeyData>up, KeyNext[]kn)                            // Repack the key, data pairs into leaves under the current branch
     {final int N = up.size();                                                  // Recreate the leaves with the key, data pairs packed in
      Leaf leaf = pushNewLeaf();                                                // First new leaf into branch
      for (int k = 0; k < N; k++)                                               // Repack the leaves
       {z();
        final KeyData source = up.elementAt(k);                                 // Source of repack
        if (leaf.leafSize().equals(maxKeysPerLeaf))                             // Start a new leaf when the current one is full
         {z();
          leaf = pushNewLeaf();                                                 // New leaf
         }
        leaf.keyData()[leaf.leafSize().asInt()].set(source);                    // Push current key, data pair into the current leaf
        leaf.incLeafSize();                                                     // Move up in leaf
       }

      final Leaf t = new Leaf(top());                                           // The top leaf is empty so we replace it with the next top leaf
      if (new Leaf(top()).leafSize().asInt() == 0)                              // The top leaf is empty so we replace it with the next top leaf
       {z();
        decBranchSize();                                                        // Pop the last key, next pair off the body of the parent branch
        t.copy(leaf);                                                           // Copy the last leaf in the body to the top next leaf
        leaf.freeLeaf();
       }

      final int c = branchSize().asInt();                                       // Update the keys in each key, next pair in the parent branch to one less than the lowest key in the next child leaf.
      final KeyNext[]Pkn = keyNext();

      for(int b = 0; b < c-1; b++)                                              // Fix keys in parent
       {z();
        Pkn[b].key.set(new Key(new Leaf(kn[b+1].next).smallestKey().asInt()-1));// A key smaller than any key in the next sibling leaf
       }

      if (c > 0)
       {z();
        Pkn[c-1].key.set(new Key(new Leaf(top()).smallestKey().asInt()-1));     // A key smaller than any key in the leaf referenced by top in the parent
       }
     }

    void repackLeaves(Key Key, Data Data)                                       // Repack the keys in the leaves under the parent branch
     {z();
      final UnpackLeaves   ul = new UnpackLeaves();                             // Unpack the leaves under this branch
      final Stack<KeyData> up = ul.up;                                          // Save area for key, next pairs during repack

      new Leaf(top()).zeroLeafSize();                                           // Clear leaf referenced by top
      zeroBranchSize();                                                         // Zero the size of this branch ready for repack of key, next pairs

      boolean inserted = false;                                                 // Whether the new key, data pair was inserted or not into the stack of key, data pairs ready to be repacked

      for (int i = 0; i < up.size(); i++)                                       // Insert the new key, data pair in the stack of key, data pairs awaiting repacking
       {z();
        if (up.elementAt(i).key.greaterThanOrEqual(Key))                        // Found insertion point
         {z();
          up.insertElementAt(new KeyData(Key, Data), i);                        // Insert new key, data pair
          z(); inserted = true;
          break;
         }
       }
      if (!inserted)                                                            // Key is bigger than all existing keys
       {z(); up.push(new KeyData(Key, Data));
       }

      z(); repackLeaves(up, ul.kn);                                             // Repack the key, data pairs into leaves under the current branch
     }

    void repackLeaves()                                                         // Repack the keys in the leaves under the parent branch
     {z();
      final UnpackLeaves   ul = new UnpackLeaves();                             // Unpack the leaves under this branch

      new Leaf(top()).zeroLeafSize();                                           // Clear leaf referenced by top
      zeroBranchSize();                                                         // Zero the size of this branch ready for repack of key, next pairs
      repackLeaves(ul.up, ul.kn);                                               // Repack the key, data pairs into leaves under the current branch
     }

    void repackLeavesIntoRoot()                                                 // Repack the keys in the leaves under the root into the root
     {z();

      final UnpackLeaves   ul = new UnpackLeaves();                             // Unpack the leaves under this branch
      final Stack<KeyData> up = ul.up;                                          // Save area for key, next pairs during repack

      new Leaf(top()).freeLeaf();                                               // Free top

      final Leaf leaf = makeIntoLeaf();                                         // Convert root to leaf
      leaf.setLeafSize(up.size());                                              // Set size of leaf

      final int N = up.size();                                                  // Recreate the leaves with the key, data pairs packed in
      final KeyData[]kd = leaf.keyData();                                       // Key, data pairs
      for (int k = 0; k < N; k++)                                               // Repack the leaves
       {z();
        final KeyData ukd = up.elementAt(k);                                    // Source of repack
        kd[k].set(ukd.key);
        kd[k].set(ukd.data);
       }
     }

    void top(Next top)  {z(); nodes[index].top = top;}                          // Set the top next reference for this branch

    class FindFirstGreaterThanOrEqual                                           // Find the first key in the branch that is equal to or greater than the search key
     {final Key     search;                                                     // Search key
      final boolean  found;                                                     // Whether the key was found
      final BKNIndex first;                                                     // Index of first such key if found
      final Next      next;                                                     // The corresponding next field or top if no such key was found

      FindFirstGreaterThanOrEqual(Key Search)                                   // Find the first key in the branch that is equal to or greater than the search key
       {z(); if (!state().branch()) stop("Branch required, not", state());
        z(); search = Search;
        boolean looking = true;
        final KeyNext[]kn = keyNext();
        int i;
        final int N = branchSize().asInt();
        for (i = 0; i < N && looking; i++)                                      // Check each key
         {z();
          if (kn[i].key.greaterThanOrEqual(search))
           {z(); looking = false; break;
           }
         }
        if (looking)                                                            // Key not found
         {z(); found = false; first = null; next = top();
         }
        else                                                                    // Key has been found
         {z(); found = true; first = new BKNIndex(i); next  = keyNext()[i].next;
         }
       }
      public String toString()                                                  // Print search results
       {final StringBuilder s = new StringBuilder();
        s.append("Key:"+search+" found:"+printBit(found));
        if (found) s.append(" first:"+first+" next:"+next);
        return s.toString();
       }
     }

    void print(Stack<StringBuilder>S, int level)                                // Print branch horizontally
     {if (level > maxPrintLevels) return;
      padStrings(S, level);
      final int K = branchSize().asInt();
      final int L = level * linesToPrintABranch;

      if (K > 0)                                                                // Branch has key, next pairs
       {for  (int i = 0; i < K; i++)
         {final int next = keyNext()[i].next.asInt();                           // Each key, next pair
          if (nodes[next].state.leaf())
           {final Leaf l = new Leaf(next);
            l.print(S, level+1);
           }
          else if (nodes[next].state.branch())
           {if (next == 0)
              {say("Cannot descend through root from index", i,
                   "in branch", index);
               break;
              }
            final Branch b = new Branch(next);
            b.print(S, level+1);
           }

          S.elementAt(L+0).append(""+keyNext()[i].key.asInt());                 // Key
          S.elementAt(L+1).append(""+index+(i > 0 ?  "."+i : ""));              // Branch,key, next pair
          S.elementAt(L+2).append(""+keyNext()[i].next.asInt());
         }
       }
      else                                                                      // Branch is empty so print just the index of the branch
       {S.elementAt(L+0).append(""+index+"Empty");
        padStrings(S, level);
       }
      final int top = top().asInt();                                            // Top next will always be present
      S.elementAt(L+3).append(top);                                             // Append top next
      if (nodes[top].state.leaf())
       {final Leaf l = new Leaf(top);
        l.print(S, level+1);
       }
      else
       {if (top == 0)
         {say("Cannot descend through root from top in branch", index);
          return;
         }
        final Branch b = new Branch(top);
        b.print(S, level+1);
       }

      padStrings(S, level);
     }

    void splitBranchRoot()                                                      // Split a branch which happens to be a full root into two half full branches while retaining the current branch as the root
     {z(); assertBranch();
      if (index != 0) stop("Not root, but", index);
      z();
      if (!isFull()) stop("Root is not full, but", branchSize());
      z();
      zeroBranchSize();
      final KeyNext[]kn = keyNext();                                            // Key, next pairs for root to be split

      final Branch l = new Branch(); final KeyNext[]lkn = l.keyNext();
      final Branch r = new Branch(); final KeyNext[]rkn = r.keyNext();

      if (hasLeaves())                                                          // The immediate children are leaves
       {z();
        for (int i = 0; i < splitBranchSize; i++)
         {z(); l.push(kn[i].key, new Leaf(kn[i].next));
         }
        push(kn[splitBranchSize].key, l);
        for(int i = maxKeysPerBranch-splitBranchSize; i < maxKeysPerBranch; i++)
         {z(); r.push(kn[i].key, new Leaf(kn[i].next));
         }
       }
      else                                                                      // The immediate children are branches
       {z();
        for (int i = 0; i < splitBranchSize; i++)
         {z(); l.push(kn[i].key, new Branch(kn[i].next));
         }
        push(kn[splitBranchSize].key, l);
        for(int i = maxKeysPerBranch-splitBranchSize; i < maxKeysPerBranch; i++)
         {z(); r.push(kn[i].key, new Branch(kn[i].next));
         }
       }
      r.top(top()); top(r);
      l.top(kn[splitBranchSize].next);
     }

    void splitChildOfBranch(FindFirstGreaterThanOrEqual gte)                    // Split the indicated child branch under this parent because the child branch contains the search path for the specified key under this parent
     {z(); assertBranch();
      if (isFull()) stop("Branch should not be full, but it is, branch:", this);
      z();
      final Branch   right = new Branch(gte.next);                              // The child being split
      if (!right.isFull()) stop("Branch:",
        right, "should be full but is only:", right.branchSize());
      z();

      final Branch    left = new Branch();                                      // The split out left hand side off the child being split
      final KeyNext [] pkn = keyNext();                                         // Parent key next
      final KeyNext [] lkn = left.keyNext();                                    // Left child key, next
      final KeyNext [] rkn = right.keyNext();                                   // Child being split
      final KeyNext  split = rkn[splitBranchSize];                              // Split out

      left.top(split.next);                                                     // The top of the left child is  the content of the splitting key

      if (gte.found)                                                            // Insert in body of parent
       {z();
        final int I = gte.first.asInt();
        for (int i = branchSize().asInt(); i > I; i--)
         {z(); pkn[i] = pkn[i-1];
         }
        pkn[I] = new KeyNext(split.key, new Next(left.index));                  // Insert a key, next pair into the parent
       }
      else                                                                      // Push at end of parent
       {z(); top(right);
        z(); pkn[branchSize().asInt()] =                                        // Push onto end of parent
          new KeyNext(split.key, new Next(left.index));
       }
      incBranchSize();                                                          // The parent now has one more key, next pair

      for (int i = 0; i < splitBranchSize; i++)                                 // Move key, next pairs out of right child into new left child
       {z(); lkn[i] = rkn[i];
        z(); rkn[i] = rkn[splitBranchSize+1+i];
       }
      left.setBranchSize(splitBranchSize);
      right.setBranchSize(splitBranchSize);
     }
   }

  Branch root() {return new Branch(true);}                                      // Return the root as a branch
  class RootBranch extends Branch {RootBranch() {super(true); z();}}            // The Root is a branch
  class RootLeaf   extends Leaf   {RootLeaf()   {super(true); z();}}            // The Root is a leaf

  boolean rootIsLeaf()        {z(); return nodes[0] .state.leaf();}             // Root is a leaf
  boolean isLeaf    (int  bl) {z(); return nodes[bl].state.leaf();}             // Indexed branch or leaf is a leaf
  boolean isLeaf    (Next bl) {z(); return isLeaf(bl.asInt());}                 // Indexed branch or leaf is a leaf
  boolean isBranch  (int  bl) {z(); return nodes[bl].state.branch();}           // Indexed branch or leaf is a branch
  boolean isBranch  (Next bl) {z(); return isBranch(bl.asInt());}               // Indexed branch or leaf is a branch

  class Key                                                                     // A key in a leaf or a branch
   {final boolean[] key = new boolean[bitsPerKey];                              // A key is composed of bits
    Key()       {z(); }
    Key(int n)  {z(); intToBits(n, key);}
    int asInt() {return bitsToInt(key);}
    boolean equals            (Key Key) {z(); return asInt() == Key.asInt();}
    boolean greaterThanOrEqual(Key Key) {z(); return asInt() >= Key.asInt();}
    public String toString() {return ""+bitsToInt(key);}
    void set(Key Key)                                                           // Copy the specified key into the current key
     {z(); for (int i = 0; i < key.length; i++) key[i] = Key.key[i];
     }
   }

  class Data                                                                    // A data item associated with a key in a leaf
   {final boolean[] data = new boolean[bitsPerData];                            // Data is composed of bits
    Data()      {z();}
    Data(int n) {z(); intToBits(n, data);}
    Data(Data Data) {z(); set(Data);}
    int asInt() {return bitsToInt(data);}
    public String toString() {z(); return ""+bitsToInt(data);}
    void set(Data Data)                                                         // Copy the specified datum into the current datum
     {z(); for (int i = 0; i < data.length; i++) data[i] = Data.data[i];
     }
   }

  class Next                                                                    // A next reference from a branch to a leaf or a branch
   {final boolean[] next = new boolean[bitsPerNext];                            // Enough bits to reference a leaf or branch in the block
    Next() {this(0); z();}                                                      // The root
    Next(int n)
     {z(); if (n >= treeSize) stop("n must be less than", treeSize, "but is",n);
      z(); intToBits(n, next);
     }
    int asInt()               {return bitsToInt(next);}
    public String toString()  {return ""+bitsToInt(next);}
    void set(Next Next)                                                         // Copy the specified next reference
     {z(); for (int i = 0; i < next.length; i++) next[i] = Next.next[i];
     }
    void set(int n)                                                             // Copy the specified integer as a next reference
     {z(); intToBits(n, next);
     }
   }

  class KeyData                                                                 // A key, data pair in a leaf
   {final Key   key = new Key();                                                // A key in a leaf
    final Data data = new Data();                                               // Data associated with the key
    KeyData ()                {z(); }
    KeyData (Key Key, Data Data) {z(); set(Key); set(Data);}
    KeyData (KeyData  KeyData){this(KeyData.key, KeyData.data); z();}
    void set(KeyData source)  {z(); key.set(source.key); data.set(source.data);}
    void set(Key  Key)        {z(); key.set(Key);}
    void set(Data Data)       {z(); data.set(Data);}
    public String toString()  {return "KeyData("+key+","+data+")";}
   }

  class KeyNext                                                                 // A key, next pair in a leaf
   {final Key   key = new Key();                                                // A key in a branch
    final Next next = new Next();                                               // Next branch or leaf
    KeyNext()                     {z();}
    KeyNext(KeyNext KeyNext)      {z(); set(KeyNext.key); set(KeyNext.next);}
    KeyNext(Key Key, Next Next)   {z(); set(Key); set(Next);}
    void set(Key  Key)            {z(); key.set(Key);}
    void set(Next Next)           {z(); next.set(Next);}
    public String toString() {return "KeyNext("+key+","+next+")";}
   }

  abstract class BLIndex                                                        // An index to key, data pair in a leaf or key, next pair in a branch
   {int i;                                                                      // Index
    void zero()     {z(); i = 0;}
    boolean equals(int N) {z(); return i == N;}
    void set(int I) {z(); i = I;}
    int asInt()     {return i;}
    void stop(String s) {Test.stop("Cannot "+s+"rement leaf index");}
   }

  class LKDIndex extends BLIndex                                                // An index to key, data pair in a leaf
   {LKDIndex()      {z(); }
    LKDIndex(int I) {z(); i = I;}
    void inc() {z(); if (i < maxKeysPerLeaf) ++i; else stop("inc");}
    void dec() {z(); if (i > 0)              --i; else stop("dec");}
   }

  class BKNIndex extends BLIndex                                                // An index to key, next pair in a branch
   {BKNIndex()      {z(); }
    BKNIndex(int I) {z(); i = I;}
    public String toString() {return "BKNIndex("+i+")";}
    void inc()      {z(); if (i < maxKeysPerBranch) ++i; else stop("inc");}
    void dec()      {z(); if (i > 0)                --i; else stop("dec");}
   }

//D1 Find                                                                       // Find the data associated with a key

  class Find                                                                    // Find the data associated with a key in a tree
   {Leaf.FindEqual leaf;                                                        // Details of the leaf we found using this search
    boolean rootIsLeaf;                                                         // The root is a leaf
    boolean allFull;                                                            // All the branches were full if true
    Branch parent;                                                              // If the root is a branch (not a leaf) then this branch is the parent of the leaf that should contain they key
    Branch lastNotFull;                                                         // Last branch not full if not all the branches were full

    Find(Key Key)
     {z();
      if (rootIsLeaf())                                                         // The root is a leaf
       {z();
        final Leaf l = new Leaf(true);
        leaf         = l.new FindEqual(Key);
        allFull      = true;
        rootIsLeaf   = true;
        return;
       }

      parent  = root();                                                         // Parent starts at root which is known to be a branch
      allFull = true;                                                           // Assume that all branches are full until we discover otherwise

      for (int i = 0; i < maxDepth; i++)                                        // Step down through tree
       {z();
        if (!parent.isFull())                                                   // Track last not full branch
         {z(); allFull = false; lastNotFull = parent;
         }
        final Branch.FindFirstGreaterThanOrEqual down =                         // Find next child in search path of key
          parent.new FindFirstGreaterThanOrEqual(Key);
        final Next n = down.next;
        if (isLeaf(n))                                                          // Found the containing leaf
         {z();
          final Leaf l = new Leaf(n);
          leaf         = l.new FindEqual(Key);
          return;
         }
        parent = new Branch(n);                                                 // Step down to lower branch
       }
      stop("Search did not terminate in a leaf");
     }

    Find(int Key) {this(new Key(Key)); z();}                                    // Find an integer

    Leaf      leaf() {z(); return new Leaf(leaf.next());}
    boolean  found() {z(); return leaf.found;}
    LKDIndex index() {z(); return leaf.index;}
    Data      data() {z(); return leaf.data;}

    public String toString()                                                    // Print find result
     {final StringBuilder s = new StringBuilder();
      s.append("search:"+leaf.search);
      s.append( " leaf:"+leaf().index);
      s.append(" rootIsLeaf:"+printBit(rootIsLeaf));
      if (!rootIsLeaf) s.append(" parent:"+parent.index);
      s.append(" found:"+printBit(found()));
      if (leaf.found)
       {s.append( " data:"+data());
        s.append(" index:"+index().asInt());
       }
      s.append(" allFull:"+printBit(allFull));
      if (!allFull) s.append( " lastNotFull:"+lastNotFull.index);
      return s.toString().trim();
     }
   }

  Find find(Key Key) {z(); return new Find(Key);}                               // Find a key in a branch
  Find find(int Key) {z(); return find(new Key(Key));}                          // Find an integer key in a branch

  class FindAndInsert extends Find                                              // Insert the specified key and data into the tree if there is room in the target leaf,or update the key with the data if the key already exists
   {Key         key;                                                            // Key to insert
    Data        data;                                                           // Data being inserted or updated
    boolean  success;                                                           // Inserted or updated if true
    boolean inserted;                                                           // Inserted if true

    FindAndInsert(Key Key, Data Data)                                           // Find the leaf that should contain this key and insert or update it is possible
     {super(Key);                                                               // Find the leaf that should contain this key
      z(); key  = Key; data = Data;
      final Leaf l = leaf();                                                    // Leaf in which the key should go

      if (leaf.found)                                                           // Found the key in the leaf so update it with the new data
       {z(); l.update(leaf.index, Data); success = true; inserted = false;
        return;
       }

      if (!l.isFull())                                                          // Leaf is not full so we can insert immediately
       {z();
        final Leaf.FindFirstGreaterThanOrEqual fge =
             l.new FindFirstGreaterThanOrEqual(Key);
        final KeyData[]kd = l.keyData();
        if (fge.found)                                                          // Found a matching key so insert into body of leaf
         {z();
          final int F = fge.first.asInt();
          for(int i = maxKeysPerLeaf - 1; i > F; --i)
           {z(); kd[i].set(kd[i-1]);
           }
          kd[F].set(Key); kd[F].set(Data);
         }
        else                                                                    // No matching key so put at end
         {z();
          final int F = l.leafSize().asInt();
          kd[F].set(Key);
          kd[F].set(Data);
         }
        l.incLeafSize();
        success = true;
        return;
       }

      if (parent == null)                                                       // No parent so we must have a full leaf as root
       {z(); l.splitLeafRoot();                                                 // Split the full leaf root
        z(); parent = root();                                                   // The root is no longer full
       }

      if (parent.countChildLeafKeys() < maxKeysInLeaves)                        // Repackaging the leaves is possible
       {z(); parent.repackLeaves(Key, Data); success = true;
        return;
       }
     }

    public String toString()                                                    // Print find and insert
     {final StringBuilder s = new StringBuilder();
      s.append("key:"+key);
      s.append(" data:"+data);
      s.append(" success:"+success);
      if (success) s.append(" inserted:"+inserted);
      s.append(" "+super.toString());
      return s.toString().trim();
     }
   }

  FindAndInsert findAndInsert(Key Key, Data Data)                               // Find a key and insert it if possible with its associated data
   {z();
    return new FindAndInsert(Key, Data);
   }

//D1 Insertion                                                                  // Insert a key, data pair into the tree or update and existing key with a new datum

  void put(Key Key, Data Data)                                                  // Insert a key, data pair into the tree or update and existing key with a new datum
   {z();
    final FindAndInsert f = new FindAndInsert(Key, Data);                       // Try direct insertion with no modifications to the shape of the tree
    if (f.success) return;                                                      // Inserted or updated successfully
    z();
    Branch p = f.lastNotFull;                                                   // Start the  insertion at the last not full branch in the path to the containing leaf

    if (f.allFull)                                                              // Start the insertion at the root, after splitting it, because all branches in the path to the leaf for this search were full
     {z(); root().splitBranchRoot();                                            // The root must be a branch because all full is true
      final Branch.FindFirstGreaterThanOrEqual                                  // Step down - from the root
      q = root().new FindFirstGreaterThanOrEqual(Key);
      p = new Branch(q.next);                                                   // Not full because we just split the root and this is one of the fragments but everything else is full so this must be the last not full on the search path of the key
     }

    for (int i = 0; i < maxDepth; i++)                                          // Step down from branch to branch through the tree until reaching a leaf repacking as we go
     {z();
      final Branch.FindFirstGreaterThanOrEqual                                  // Step down
        q = p.new FindFirstGreaterThanOrEqual(Key);
      if (isLeaf(q.next))                                                       // Reached a leaf
       {z(); p.repackLeaves(Key, Data);                                         // Add the key, data pair: if it were already there findAndInsert would have already inserted it
        z(); merge(Key);                                                        // Push the tree back together again along the path of the search
        return;
       }
      z();
      p.splitChildOfBranch(q);                                                  // Split the child branch in the search path for the key from the parent so the the search path does not contain a full branch above the containing leaf
      final Branch.FindFirstGreaterThanOrEqual                                  // Step down again as the repack will have altered the local layout
        r = p.new FindFirstGreaterThanOrEqual(Key);
      p = new Branch(r.next);                                                   // We are not on a leaf so continue down through the tree
     }
    stop("Fallen of the end of the tree");                                      // The tree must be missing a leaf
   }

  void put(int Key, int Data)                                                   // Put some test data into the tree
   {z(); put(new Key(Key), new Data(Data));
   }

  void put(int Key)                                                             // Put some test data into the tree
   {z(); put(Key, Key);
   }

//D1 Deletion                                                                   // Delete a key, data pair from the tree

  Data delete(Key Key)                                                          // Delete a key from the tree and returns its data if present
   {z();
    final Find f = new Find(Key);                                               // Try direct insertion with no modifications to the shape of the tree
    if (!f.found()) return null;                                                // Inserted or updated successfully
    z();
    final Leaf l = f.leaf();                                                    // The leaf that contains the key
    final int  N = l.leafSize().asInt(), p = f.index().asInt();                 // Size of leaf, position in the leaf of the key
    final KeyData[]kd = l.keyData();                                            // Key, data pairs in the leaf
    final Data   data = new Data(kd[p].data);                                   // Data associated with the leaf
    for (int i = p; i < N-1; ++i)                                               // Remove the key, data pair from the leaf
     {z();
      kd[i] = new KeyData(kd[i+1]);
     }
    l.decLeafSize();                                                            // Reduce the leaf size to match
    merge(Key);
    return data;
   }

  Data delete(int Key) {z(); return delete(new Key(Key));}                      // Delete a key from the tree and returns its data if present

// D1 Merge

  void merge(Key Key)                                                           // Merge where possible along a path to a key
   {z(); if (rootIsLeaf()) return;                                              // Only works on a branch
    z(); Branch parent = root();                                                // Parent starts at root which is known to be a branch
                                                                                // Collapse root if any of the following scenarios are detected
//          P                       L   P   R                                   // Root
//    L          R       ->       a   b   m   n                                 // Child branches
//  a   b     m    n                                                            // Grand child branches
    if (parent.branchSize().asInt() == 1)                                       // The body of the parent has one entry
     {z();
      if (isBranch(parent.top()))                                               // The next level down contains branches
       {z();
        final Branch R = new Branch(parent.top());
        final Branch L = new Branch(parent.keyNext()[0].next);
        if (L.branchSize().asInt() == 1)                                        // Left has one child in body
         {z();
          if (R.branchSize().asInt() == 1)                                      // Right has one child in body
           {z();
            final KeyNext[]kn = parent.keyNext();                               // Parent body
            final Key pk = kn[0].key;                                           // Parent key
            kn[0] = new KeyNext(L.keyNext()[0]);                                // Move body of left into parent
            kn[1] = new KeyNext(pk, L.top());                                   // Move top of left into parent
            kn[2] = new KeyNext(R.keyNext()[0]);                                // Move body of right into parent
            parent.top(R.top());                                                // Move top of right to top of parent
            parent.setBranchSize(3);
            L.freeBranch();                                                     // Free liberated left child
            R.freeBranch();                                                     // Free liberated right child
           }
//          P                       L   P
//    L          R       ->       a   b   n
//  a   b   empty   n
          else if (R.branchSize().asInt() == 0)                                 // Right has one child in body
           {z();
            final KeyNext[]kn = parent.keyNext();                               // Parent body
            final Key pk = kn[0].key;                                           // Parent key
            kn[0] = new KeyNext(L.keyNext()[0]);                                // Move body of left into parent
            kn[1] = new KeyNext(pk, L.top());                                   // Move top of left into parent
            parent.top(R.top());                                                // Move top of right to top of parent
            parent.setBranchSize(2);
            L.freeBranch();                                                     // Free liberated left child
            R.freeBranch();                                                     // Free livebrated right child
           }
         }
//            P                       P   R
//       L          R       ->      b   m   n
// empty   b     m    n
        else if (L.branchSize().asInt() == 0)                                   // Left has no child in body
         {z();
          if (R.branchSize().asInt() == 1)                                      // Right has one child in body
           {z();
            final KeyNext[]kn = parent.keyNext();                               // Parent body
            final Key pk = kn[0].key;                                           // Parent key
            kn[0] = new KeyNext(pk, L.top());                                   // Move top of left into parent
            kn[1] = new KeyNext(R.keyNext()[0]);                                // Move body of right into parent
            parent.top(R.top());                                                // Move top of right to top of parent
            parent.setBranchSize(2);
            L.freeBranch();                                                     // Free liberated left child
            R.freeBranch();                                                     // Free liberated right child
           }
//             P                       P
//       L           R       ->      b   n
// empty   b   empty  n
          else if (R.branchSize().asInt() == 0)                                 // Right has one child in body
           {z();
            final KeyNext[]kn = parent.keyNext();                               // Parent body
            final Key pk = kn[0].key;                                           // Parent key
            kn[0] = new KeyNext(pk, L.top());                                   // Move top of left into parent
            parent.top(R.top());                                                // Move top of right to top of parent
            parent.setBranchSize(1);
            L.freeBranch();                                                     // Free liberated left child
            R.freeBranch();                                                     // Free liberated right child
           }
//               P                  R                                           // Root
//       L              R   ->                                                  // Branch layer
// empty   empty                                                                // Leaf layer
          else if (isLeaf(L.top()) && new Leaf(L.top()).leafSize().asInt() == 0)// Left has empty leaf under it
           {z();
            parent.copy(R);
            new Leaf(L.top()).freeLeaf();                                       // Free empty top of left
            L.freeBranch();                                                     // Free liberated left child
            R.freeBranch();                                                     // Free liberated right child
           }
         }
       }
     }

    for (int i = 0; i < maxDepth; i++)                                          // Step down through tree
     {z();
      final Branch.FindFirstGreaterThanOrEqual down =
        parent.new FindFirstGreaterThanOrEqual(Key);

      if (isLeaf(down.next))                                                    // Found the containing leaf
       {z();
        if (0 == parent.index)                                                  // At the root, the root is a branch and the children are leaves
         {if (parent.countChildLeafKeys() <= maxKeysPerLeaf)                    // Few enough key, data pairs to hold them in the root as a leaf
           {z(); parent.repackLeavesIntoRoot();
            return;
           }
         }
        parent.repackLeaves();                                                  // Repack the leaves of this branch
        return;
       }

      z();                                                                      // Branch in the path of the search for the key
      final int N = parent.branchSize().asInt();
      final KeyNext[]Pkn = parent.keyNext();
      final Stack<KeyNext> pkn = new Stack<>();                                 // Children of parent
      for (int j = 0; j < N; j++)
       {z(); pkn.push(Pkn[j]);
       }
      pkn.push(new KeyNext(new Key(0), parent.top()));                          // Add top next so we have all the children in one place

      for (int b = pkn.size()-1; b > 0; b--)                                    // Merge children in body of parent by comparing them pairwise from top to bottom to avoid disruption
       {z();
        final Branch left  = new Branch(pkn.elementAt(b-1).next);
        final Branch right = new Branch(pkn.elementAt(b-0).next);
        final int L = left .branchSize().asInt(),
                  R = right.branchSize().asInt(), P = L + 1 + R;
        if (parent.branchSize().asInt() > 1 && P <= maxKeysPerBranch)           // Can merge
         {z();
          final KeyNext []    lkn = left .keyNext();
          final KeyNext []    rkn = right.keyNext();
          final Stack<KeyNext> kn = new Stack<>();
          for (int j = 0; j < L; j++) {z(); kn.push(lkn[j]);}                   // Left
          kn.push(new KeyNext(pkn.elementAt(b-1).key, left.top()));             // Merge in splitting key
          for (int j = 0; j < R; j++) {z(); kn.push(rkn[j]);}                   // Right
          for (int j = 0; j < P; j++) {z(); rkn[j] = kn.elementAt(j);}          // Copy back combined body into right sibling
          right.setBranchSize(P);
          pkn.removeElementAt(b-1);                                             // Remove splitting key from parent
          left.freeBranch();                                                    // Free the branch liberated on the left
         }
       }

      parent.top(pkn.pop().next);                                               // Remove top next
      final int PS = pkn.size();                                                // Size of parent
      for (int b = 0; b < PS; b++)                                              // Reload parent body
       {z(); Pkn[b] = pkn.elementAt(b);
       }
      parent.setBranchSize(PS);                                                 // Set parent size to match

      final Branch.FindFirstGreaterThanOrEqual Down =
        parent.new FindFirstGreaterThanOrEqual(Key);
      if (isBranch(Down.next)) {z(); parent = new Branch(Down.next);}           // Step down to a branch: if we step down to a leaf the code at the top of the loop will process it.
     }
    stop("Fell off the end of the tree while merging",
         "along the search path for:", Key);
   }

//D1 Print                                                                      // Print a BTree horizontally

  void padStrings(Stack<StringBuilder> S, int level)                            // Pad the strings at each level of the tree so we have a vertical face to continue with - a bit like Marc Brunel's tunneling shield
   {final int N = level * linesToPrintABranch + maxKeysPerLeaf;                 // Number of lines we might want
    for (int i = S.size(); i <= N; ++i) S.push(new StringBuilder());            // Make sure we have a full deck of strings
    int m = 0;                                                                  // Maximum length
    for (StringBuilder s : S) m = m < s.length() ? s.length() : m;              // Find maximum length
    for (StringBuilder s : S)                                                   // Pad each string to maximum length
     {if (s.length() < m) s.append(" ".repeat(m - s.length()));                 // Pad string to maximum length
     }
   }

  String printCollapsed(Stack<StringBuilder> S)                                 // Collapse horizontal representation into a string
   {final StringBuilder t = new StringBuilder();                                // Print the lines of the tree that are not blank
    for  (StringBuilder s : S)
     {final String l = s.toString();
      if (l.isBlank()) continue;
      t.append(l+"|\n");
     }
    return t.toString();
   }

  String print(Branch branch)                                                   // Print a tree horizontally starting at the specified branch
   {final Stack<StringBuilder> S = new Stack<>();
    branch.print(S, 0);
    return printCollapsed(S);
   }

  String print()                                                                // Print a tree horizontally
   {final Stack<StringBuilder> S = new Stack<>();

    if (rootIsLeaf())
     {final RootLeaf r = new RootLeaf();
      r.print(S, 0);
     }
    else
     {final RootBranch r = new RootBranch();
      r.print(S, 0);
     }
    return printCollapsed(S);
   }

//D1 Bits as integers                                                           // Convert between arrays of bits and integers

  static int bitsToInt(boolean[]B)                                              // Convert an array of bits to an integer with the lowest elements in the array representing the least significant bits of the integer
   {int N = 0;
    final int w = min(Integer.SIZE-1, B.length);
    for(int i = 0; i < w; ++i) N += B[i] ?  1 << i : 0;
    return N;
   }

  static void intToBits(int N, boolean[]B)                                      // Convert an integer to an array of bits  storing the least significant bit in the lowest element of the array
   {final int w = min(Integer.SIZE-1, B.length);
    for(int i = 0; i < w; ++i)
     {final int n =  N & (1 << i);
      B[i] = n != 0;
     }
   }

  static String printBits(boolean[]B)                                           // Print an array of bits as a string
   {z();
    final StringBuilder s = new StringBuilder();
    for(int i = 0; i < B.length; ++i)
     {final boolean b = B[B.length-1-i];
      s.append(b ? "1" : "o");
     }
    return s.toString();
   }

  static String printBit(boolean B) {z(); return B ? "1" : "n";}                // Print a bit

//D0 Tests                                                                      // Testing

  static void test_bits()
   {final boolean[]b = new boolean[4];
    intToBits(13, b);
    ok(printBits(b), "11o1");
    ok(bitsToInt(b), 13);
   }

  static Btree test_small_tree()
   {final Btree  t = new Btree(8, 8, 4, 4, 3, 8);
    final Branch R = t.new Leaf(true).makeIntoBranch();
    final Leaf   l = t.new Leaf();
    final Leaf   m = t.new Leaf();
    final Leaf   r = t.new Leaf();
    R.push(t.new Key( 8), l);
    R.push(t.new Key(12), m);
    R.top( t.new Next(r.index));
    l.push( 2, 12);
    l.push( 4, 14);
    l.push( 6, 16);
    l.push( 8, 18);
    m.push(10, 20);
    m.push(12, 22);
    r.push(14, 24);
    r.push(16, 26);
    r.push(18, 28);
    return t;
   }

  static void test_find()
   {final Btree  t = test_small_tree();
    //t.stop();
    t.ok("""
          8        12            |
          0        0.1           |
          1        2             |
                   3             |
2,4,6,8=1  10,12=2    14,16,18=3 |
""");

    //say("\""+t.find(t.new Key( 1))+"\");");
    //say("\""+t.find(t.new Key( 2))+"\");");
    //say("\""+t.find(t.new Key( 3))+"\");");
    //say("\""+t.find(t.new Key( 4))+"\");");
    //say("\""+t.find(t.new Key( 5))+"\");");
    //say("\""+t.find(t.new Key( 6))+"\");");
    //say("\""+t.find(t.new Key( 7))+"\");");
    //say("\""+t.find(t.new Key( 8))+"\");");
    //say("\""+t.find(t.new Key( 9))+"\");");
    //say("\""+t.find(t.new Key(10))+"\");");
    //say("\""+t.find(t.new Key(11))+"\");");
    //say("\""+t.find(t.new Key(12))+"\");");
    //say("\""+t.find(t.new Key(13))+"\");");
    //say("\""+t.find(t.new Key(14))+"\");");
    //say("\""+t.find(t.new Key(15))+"\");");
    //say("\""+t.find(t.new Key(16))+"\");");
    //say("\""+t.find(t.new Key(17))+"\");");
    //say("\""+t.find(t.new Key(18))+"\");");
    //say("\""+t.find(t.new Key(19))+"\");");

    ok(t.find(t.new Key( 1)), "search:1 leaf:1 rootIsLeaf:n parent:0 found:n allFull:n lastNotFull:0");
    ok(t.find(t.new Key( 2)), "search:2 leaf:1 rootIsLeaf:n parent:0 found:1 data:12 index:0 allFull:n lastNotFull:0");
    ok(t.find(t.new Key( 3)), "search:3 leaf:1 rootIsLeaf:n parent:0 found:n allFull:n lastNotFull:0");
    ok(t.find(t.new Key( 4)), "search:4 leaf:1 rootIsLeaf:n parent:0 found:1 data:14 index:1 allFull:n lastNotFull:0");
    ok(t.find(t.new Key( 5)), "search:5 leaf:1 rootIsLeaf:n parent:0 found:n allFull:n lastNotFull:0");
    ok(t.find(t.new Key( 6)), "search:6 leaf:1 rootIsLeaf:n parent:0 found:1 data:16 index:2 allFull:n lastNotFull:0");
    ok(t.find(t.new Key( 7)), "search:7 leaf:1 rootIsLeaf:n parent:0 found:n allFull:n lastNotFull:0");
    ok(t.find(t.new Key( 8)), "search:8 leaf:1 rootIsLeaf:n parent:0 found:1 data:18 index:3 allFull:n lastNotFull:0");
    ok(t.find(t.new Key( 9)), "search:9 leaf:2 rootIsLeaf:n parent:0 found:n allFull:n lastNotFull:0");
    ok(t.find(t.new Key(10)), "search:10 leaf:2 rootIsLeaf:n parent:0 found:1 data:20 index:0 allFull:n lastNotFull:0");
    ok(t.find(t.new Key(11)), "search:11 leaf:2 rootIsLeaf:n parent:0 found:n allFull:n lastNotFull:0");
    ok(t.find(t.new Key(12)), "search:12 leaf:2 rootIsLeaf:n parent:0 found:1 data:22 index:1 allFull:n lastNotFull:0");
    ok(t.find(t.new Key(13)), "search:13 leaf:3 rootIsLeaf:n parent:0 found:n allFull:n lastNotFull:0");
    ok(t.find(t.new Key(14)), "search:14 leaf:3 rootIsLeaf:n parent:0 found:1 data:24 index:0 allFull:n lastNotFull:0");
    ok(t.find(t.new Key(15)), "search:15 leaf:3 rootIsLeaf:n parent:0 found:n allFull:n lastNotFull:0");
    ok(t.find(t.new Key(16)), "search:16 leaf:3 rootIsLeaf:n parent:0 found:1 data:26 index:1 allFull:n lastNotFull:0");
    ok(t.find(t.new Key(17)), "search:17 leaf:3 rootIsLeaf:n parent:0 found:n allFull:n lastNotFull:0");
    ok(t.find(t.new Key(18)), "search:18 leaf:3 rootIsLeaf:n parent:0 found:1 data:28 index:2 allFull:n lastNotFull:0");
    ok(t.find(t.new Key(19)), "search:19 leaf:3 rootIsLeaf:n parent:0 found:n allFull:n lastNotFull:0");
   }

  static void test_find_and_insert()
   {final Btree t = test_small_tree();
    //t.stop();
    t.ok("""
          8        12            |
          0        0.1           |
          1        2             |
                   3             |
2,4,6,8=1  10,12=2    14,16,18=3 |
""");
    FindAndInsert fi6 = t.findAndInsert(t.new Key(6), t.new Data(7));           // Update leaf
    //stop(t);
    t.ok("""
          8        12            |
          0        0.1           |
          1        2             |
                   3             |
2,4,6,8=1  10,12=2    14,16,18=3 |
""");


    //stop(fi6);
    ok(fi6, "key:6 data:7 success:true inserted:false search:6 leaf:1 rootIsLeaf:n parent:0 found:1 data:7 index:2 allFull:n lastNotFull:0");
    ok(t.find(t.new Key(6)).data(),  7);
    ok(t.find(t.new Key(8)).data(), 18);

    FindAndInsert fi11 = t.findAndInsert(t.new Key(11), t.new Data(11));        // Insert directly into a leaf
    //t.stop();
    t.ok("""
          8           12            |
          0           0.1           |
          1           2             |
                      3             |
2,4,6,8=1  10,11,12=2    14,16,18=3 |
""");
    ok(t.root().countChildLeafKeys(), 10);

    FindAndInsert fi19 = t.findAndInsert(t.new Key(19), t.new Data(19));        // Insert directly into a leaf
    //t.stop();
    t.ok("""
          8           12               |
          0           0.1              |
          1           2                |
                      3                |
2,4,6,8=1  10,11,12=2    14,16,18,19=3 |
""");
    ok(t.root().countChildLeafKeys(), 11);

    FindAndInsert fi5 = t.findAndInsert(t.new Key(5), t.new Data(5));           // Insert by repacking
    //t.stop();
    t.ok("""
          7             13               |
          0             0.1              |
          2             1                |
                        3                |
2,4,5,6=2  8,10,11,12=1    14,16,18,19=3 |
""");
    ok(t.root().countChildLeafKeys(), 12);
   }

  static void test_split_leaf_root()
   {final Btree t = new Btree(8, 8, 4, 4, 3, 3);
    final Leaf  l = t.new Leaf(true);
    l.push( 2, 12);
    l.push( 4, 14);
    l.push( 6, 16);
    l.push( 8, 18);
    //stop(t);
    t.ok("""
2,4,6,8=0 |
""");
    Branch b = l.splitLeafRoot();
    //stop(t);
    t.ok("""
      4      |
      0      |
      1      |
      2      |
2,4=1  6,8=2 |
""");
   ok(b.hasLeaves(), true);
   }

  static void test_split_branch_root()
   {final Btree t = new Btree(8, 8, 4, 4, 3, 9);
    final Leaf  r = t.new Leaf(true);
    r.push( 20, 12);
    r.push( 40, 14);
    r.push( 60, 16);
    r.push( 80, 18);
    //stop(t);
    t.ok("""
20,40,60,80=0 |
""");
    final Branch R = r.splitLeafRoot();
    final Leaf l2 = t.new Leaf();
    l2.push( 100, 110);
    l2.push( 120, 130);
    final Leaf l3 = t.new Leaf();
    l3.push( 140, 150);
    l3.push( 160, 170);
    R.push(t.new Key( 80), t.new Leaf(R.top()));
    R.push(t.new Key(120), t.new Leaf(l2.index));
    R.top(l3);
    //stop(t);
    t.ok("""
        40        80           120          |
        0         0.1          0.2          |
        1         2            3            |
                               4            |
20,40=1   60,80=2    100,120=3    140,160=4 |
""");
    R.splitBranchRoot();
    //stop(t);
    t.ok("""
                  80                       |
                  0                        |
                  5                        |
                  6                        |
        40                    120          |
        5                     6            |
        1                     3            |
        2                     4            |
20,40=1   60,80=2   100,120=3    140,160=4 |
""");

    final Leaf il = t.new Leaf();
    il.push( 90, 91);
    il.push( 92, 93);

    final Leaf ir = t.new Leaf();
    ir.push( 96, 97);
    ir.push( 98, 99);
   }

  static void test_put_ascending()
   {final Btree t = new Btree(8, 8, 8, 4, 3, 24);
    final int N = 64;
    for (int i = 1; i <= N; i++) t.put(i);
    //stop(t);
    t.ok("""
                                                                                                                            32                                                                                                                                           |
                                                                                                                            0                                                                                                                                            |
                                                                                                                            19                                                                                                                                           |
                                                                                                                            20                                                                                                                                           |
                                                      16                                                                                                                                            48                                                                   |
                                                      19                                                                                                                                            20                                                                   |
                                                      9                                                                                                                                             21                                                                   |
                                                      14                                                                                                                                            6                                                                    |
          4          8               12                               20               24                28                                  36               40                 44                                  52               56                60               |
          9          9.1             9.2                              14               14.1              14.2                                21               21.1               21.2                                6                6.1               6.2              |
          3          1               4                                8                10                5                                   13               15                 11                                  18               22                16               |
                                     7                                                                   12                                                                      17                                                                     2                |
1,2,3,4=3  5,6,7,8=1    9,10,11,12=4    13,14,15,16=7   17,18,19,20=8   21,22,23,24=10     25,26,27,28=5     29,30,31,32=12   33,34,35,36=13   37,38,39,40=15     41,42,43,44=11     45,46,47,48=17   49,50,51,52=18   53,54,55,56=22    57,58,59,60=16    61,62,63,64=2 |
""");
   }

  static void test_put_descending()
   {final Btree t = new Btree(8, 8, 8, 4, 3, 24);
    final int N = 64;
    for (int i = N; i > 0; --i) t.put(i);
    //stop(t);
    t.ok("""
                                                                                                                                    32                                                                                                                                    |
                                                                                                                                    0                                                                                                                                     |
                                                                                                                                    19                                                                                                                                    |
                                                                                                                                    20                                                                                                                                    |
                                                             16                                                                                                                                         48                                                                |
                                                             19                                                                                                                                         20                                                                |
                                                             21                                                                                                                                         9                                                                 |
                                                             14                                                                                                                                         6                                                                 |
           4            8                 12                                  20               24                 28                                 36              40                44                               52              56               60               |
           21           21.1              21.2                                14               14.1               14.2                               9               9.1               9.2                              6               6.1              6.2              |
           16           18                22                                  17               13                 15                                 12              8                 10                               7               3                1                |
                                          11                                                                      5                                                                    4                                                                 2                |
1,2,3,4=16   5,6,7,8=18     9,10,11,12=22     13,14,15,16=11   17,18,19,20=17   21,22,23,24=13     25,26,27,28=15     29,30,31,32=5   33,34,35,36=12   37,38,39,40=8    41,42,43,44=10    45,46,47,48=4   49,50,51,52=7   53,54,55,56=3    57,58,59,60=1    61,62,63,64=2 |
""");
   }

  static class RandomArray
   {final static int[]r = {27, 442, 545, 317, 511, 578, 391, 993, 858, 586, 472, 906, 658, 704, 882, 246, 261, 501, 354, 903, 854, 279, 526, 686, 987, 403, 401, 989, 650, 576, 436, 560, 806, 554, 422, 298, 425, 912, 503, 611, 135, 447, 344, 338, 39, 804, 976, 186, 234, 106, 667, 494, 690, 480, 288, 151, 773, 769, 260, 809, 438, 237, 516, 29, 376, 72, 946, 103, 961, 55, 358, 232, 229, 90, 155, 657, 681, 43, 907, 564, 377, 615, 612, 157, 922, 272, 490, 679, 830, 839, 437, 826, 577, 937, 884, 13, 96, 273, 1, 188};
    final static TreeSet<Integer> present = new TreeSet<>();
    static
     {for (int i = 0; i < r.length; i++) present.add(r[i]);
     }
    boolean present(int i) {return present.contains(i);}
    int     max()          {return present.last();}
   }

  static void test_put_random()
   {final RandomArray r = new RandomArray();
    final int N = RandomArray.r.length;
    final Btree t = new Btree(16, 16, 16, 4, 3, 40);

    for (int i = 0; i < N; i++) t.put(RandomArray.r[i]);
    //stop(t);
    t.ok("""
                                                                                                                                                                                                                   402                                                                                                                            577                                                                                                                                                                                                                 |
                                                                                                                                                                                                                   0                                                                                                                              0.1                                                                                                                                                                                                                 |
                                                                                                                                                                                                                   29                                                                                                                             32                                                                                                                                                                                                                  |
                                                                                                                                                                                                                                                                                                                                                  20                                                                                                                                                                                                                  |
                                                               150                                                                               278                                                                                                                                 500                                                                                                                             689                                                           857                                                                                |
                                                               29                                                                                29.1                                                                                                                                32                                                                                                                              20                                                            20.1                                                                               |
                                                               30                                                                                23                                                                                                                                  21                                                                                                                              33                                                            15                                                                                 |
                                                                                                                                                 11                                                                                                                                  5                                                                                                                                                                                             6                                                                                  |
              38               89                   134                              187                   236                    271                                   337                  375                                         436                   471                                         525                   563                                    614                   666                                          803                   829                                     905                   936                   986              |
              30               30.1                 30.2                             23                    23.1                   23.2                                  11                   11.1                                        21                    21.1                                        5                     5.1                                    33                    33.1                                         15                    15.1                                    6                     6.1                   6.2              |
              28               19                   38                               37                    31                     17                                    24                   7                                           22                    14                                          27                    12                                     34                    16                                           26                    36                                      8                     18                    35               |
                                                    25                                                                            13                                                         4                                                                 9                                                                 1                                                            10                                                                 3                                                                                   2                |
1,13,27,29=28   39,43,55,72=19     90,96,103,106=38     135=25    151,155,157,186=37    188,229,232,234=31     237,246,260,261=17     272,273=13     279,288,298,317=24    338,344,354,358=7     376,377,391,401=4    403,422,425,436=22    437,438,442,447=14     472,480,490,494=9    501,503,511,516=27    526,545,554,560=12    564,576,577=1    578,586,611,612=34    615,650,657,658=16     667,679,681,686=10    690,704,769,773=26    804,806,809,826=36     830,839,854=3     858,882,884,903=8    906,907,912,922=18    937,946,961,976=35    987,989,993=2 |
""");

    if (true)                                                                   // Check we can find everything we should be able to find and cannot find the rest
     {final int M = r.max() + 1;
      for (int i = 0; i < M; i++)
       {final Find f = t.new Find(i);
        ok(r.present(i), f.found());
        if (f.found()) ok(i, f.data());
       }
     }
   }

  static void test_delete()
   {final Btree t = new Btree(8, 8, 8, 4, 3, 24);
    final int N = 64;
    for (int i = 1; i <= N; i++)
     {t.put(i);
     }
    t.ok("""
                                                                                                                            32                                                                                                                                           |
                                                                                                                            0                                                                                                                                            |
                                                                                                                            19                                                                                                                                           |
                                                                                                                            20                                                                                                                                           |
                                                      16                                                                                                                                            48                                                                   |
                                                      19                                                                                                                                            20                                                                   |
                                                      9                                                                                                                                             21                                                                   |
                                                      14                                                                                                                                            6                                                                    |
          4          8               12                               20               24                28                                  36               40                 44                                  52               56                60               |
          9          9.1             9.2                              14               14.1              14.2                                21               21.1               21.2                                6                6.1               6.2              |
          3          1               4                                8                10                5                                   13               15                 11                                  18               22                16               |
                                     7                                                                   12                                                                      17                                                                     2                |
1,2,3,4=3  5,6,7,8=1    9,10,11,12=4    13,14,15,16=7   17,18,19,20=8   21,22,23,24=10     25,26,27,28=5     29,30,31,32=12   33,34,35,36=13   37,38,39,40=15     41,42,43,44=11     45,46,47,48=17   49,50,51,52=18   53,54,55,56=22    57,58,59,60=16    61,62,63,64=2 |
""");
    ok(t.delete(t.new Key(21)), 21);
    ok(t.delete(t.new Key(22)), 22);
    ok(t.delete(t.new Key(23)), 23);
    ok(t.delete(t.new Key(24)), 24);
    t.checkFreeList();

    //stop(t);
    t.ok("""
                                                      16                                                  32                                                                       48                                                                    |
                                                      0                                                   0.1                                                                      0.2                                                                   |
                                                      9                                                   14                                                                       21                                                                    |
                                                                                                                                                                                   6                                                                     |
          4          8               12                               24               28                                   36               40                 44                                   52               56                60               |
          9          9.1             9.2                              14               14.1                                 21               21.1               21.2                                 6                6.1               6.2              |
          3          1               4                                8                10                                   13               15                 11                                   18               22                16               |
                                     7                                                 12                                                                       17                                                                      2                |
1,2,3,4=3  5,6,7,8=1    9,10,11,12=4    13,14,15,16=7   17,18,19,20=8   25,26,27,28=10     29,30,31,32=12    33,34,35,36=13   37,38,39,40=15     41,42,43,44=11     45,46,47,48=17    49,50,51,52=18   53,54,55,56=22    57,58,59,60=16    61,62,63,64=2 |
""");

    ok(t.delete(t.new Key(17)), 17);
    ok(t.delete(t.new Key(18)), 18);
    ok(t.delete(t.new Key(19)), 19);
    ok(t.delete(t.new Key(20)), 20);
    t.checkFreeList();

    //stop(t);
    t.ok("""
                                                      16                               32                                                                       48                                                                    |
                                                      0                                0.1                                                                      0.2                                                                   |
                                                      9                                14                                                                       21                                                                    |
                                                                                                                                                                6                                                                     |
          4          8               12                               28                                 36               40                 44                                   52               56                60               |
          9          9.1             9.2                              14                                 21               21.1               21.2                                 6                6.1               6.2              |
          3          1               4                                8                                  13               15                 11                                   18               22                16               |
                                     7                                12                                                                     17                                                                      2                |
1,2,3,4=3  5,6,7,8=1    9,10,11,12=4    13,14,15,16=7   25,26,27,28=8   29,30,31,32=12    33,34,35,36=13   37,38,39,40=15     41,42,43,44=11     45,46,47,48=17    49,50,51,52=18   53,54,55,56=22    57,58,59,60=16    61,62,63,64=2 |
""");

    ok(t.delete(t.new Key(29)), 29);
    ok(t.delete(t.new Key(30)), 30);
    ok(t.delete(t.new Key(31)), 31);
    ok(t.delete(t.new Key(32)), 32);
    t.checkFreeList();

    //stop(t);
    t.ok("""
                                                      16                        32                                                                       48                                                                    |
                                                      0                         0.1                                                                      0.2                                                                   |
                                                      9                         14                                                                       21                                                                    |
                                                                                                                                                         6                                                                     |
          4          8               12                 14Empty                                   36               40                 44                                   52               56                60               |
          9          9.1             9.2                                                          21               21.1               21.2                                 6                6.1               6.2              |
          3          1               4                                                            13               15                 11                                   18               22                16               |
                                     7                         12                                                                     17                                                                      2                |
1,2,3,4=3  5,6,7,8=1    9,10,11,12=4    13,14,15,16=7            25,26,27,28=12    33,34,35,36=13   37,38,39,40=15     41,42,43,44=11     45,46,47,48=17    49,50,51,52=18   53,54,55,56=22    57,58,59,60=16    61,62,63,64=2 |
""");

    ok(t.delete(t.new Key(13)), 13);
    ok(t.delete(t.new Key(14)), 14);
    ok(t.delete(t.new Key(15)), 15);
    ok(t.delete(t.new Key(16)), 16);
    t.checkFreeList();

    //stop(t);
    t.ok("""
                                     16                        32                                                                       48                                                                    |
                                     0                         0.1                                                                      0.2                                                                   |
                                     9                         14                                                                       21                                                                    |
                                                                                                                                        6                                                                     |
          4          8                 14Empty                                   36               40                 44                                   52               56                60               |
          9          9.1                                                         21               21.1               21.2                                 6                6.1               6.2              |
          3          1                                                           13               15                 11                                   18               22                16               |
                     7                        12                                                                     17                                                                      2                |
1,2,3,4=3  5,6,7,8=1    9,10,11,12=7            25,26,27,28=12    33,34,35,36=13   37,38,39,40=15     41,42,43,44=11     45,46,47,48=17    49,50,51,52=18   53,54,55,56=22    57,58,59,60=16    61,62,63,64=2 |
""");

    ok(t.delete(t.new Key(25)), 25);
    ok(t.delete(t.new Key(26)), 26);
    ok(t.delete(t.new Key(33)), 33);
    ok(t.delete(t.new Key(34)), 34);
    t.checkFreeList();

    //stop(t);
    t.ok("""
                                                    32                                                                48                                                                    |
                                                    0                                                                 0.1                                                                   |
                                                    14                                                                21                                                                    |
                                                                                                                      6                                                                     |
          4           8                26                            38               42                 46                             52               56                60               |
          14          14.1             14.2                          21               21.1               21.2                           6                6.1               6.2              |
          3           1                7                             13               15                 11                             18               22                16               |
                                       12                                                                17                                                                2                |
1,2,3,4=3   5,6,7,8=1     9,10,11,12=7     27,28=12   35,36,37,38=13   39,40,41,42=15     43,44,45,46=11     47,48=17    49,50,51,52=18   53,54,55,56=22    57,58,59,60=16    61,62,63,64=2 |
""");

    ok(t.delete(t.new Key(37)), 37);
    ok(t.delete(t.new Key(38)), 38);
    ok(t.delete(t.new Key(39)), 39);
    ok(t.delete(t.new Key(40)), 40);
    t.checkFreeList();

    //stop(t);
    t.ok("""
                                                    32                                             48                                                                    |
                                                    0                                              0.1                                                                   |
                                                    14                                             21                                                                    |
                                                                                                   6                                                                     |
          4           8                26                            42               46                             52               56                60               |
          14          14.1             14.2                          21               21.1                           6                6.1               6.2              |
          3           1                7                             13               15                             18               22                16               |
                                       12                                             17                                                                2                |
1,2,3,4=3   5,6,7,8=1     9,10,11,12=7     27,28=12   35,36,41,42=13   43,44,45,46=15     47,48=17    49,50,51,52=18   53,54,55,56=22    57,58,59,60=16    61,62,63,64=2 |
""");

    ok(t.delete(t.new Key(3)), 3);
    ok(t.delete(t.new Key(4)), 4);
    ok(t.delete(t.new Key(5)), 5);
    ok(t.delete(t.new Key(6)), 6);
    t.checkFreeList();

    //stop(t);
    t.ok("""
                                      32                                             48                                                                    |
                                      0                                              0.1                                                                   |
                                      14                                             21                                                                    |
                                                                                     6                                                                     |
          8              26                            42               46                             52               56                60               |
          14             14.1                          21               21.1                           6                6.1               6.2              |
          3              1                             13               15                             18               22                16               |
                         12                                             17                                                                2                |
1,2,7,8=3   9,10,11,12=1     27,28=12   35,36,41,42=13   43,44,45,46=15     47,48=17    49,50,51,52=18   53,54,55,56=22    57,58,59,60=16    61,62,63,64=2 |
""");

    ok(t.delete(t.new Key(59)), 59);
    ok(t.delete(t.new Key(60)), 60);
    ok(t.delete(t.new Key(61)), 61);
    ok(t.delete(t.new Key(62)), 62);
    t.checkFreeList();

    //stop(t);
    t.ok("""
                                      32                                             48                                                  |
                                      0                                              0.1                                                 |
                                      14                                             21                                                  |
                                                                                     6                                                   |
          8              26                            42               46                             52               56               |
          14             14.1                          21               21.1                           6                6.1              |
          3              1                             13               15                             18               22               |
                         12                                             17                                              2                |
1,2,7,8=3   9,10,11,12=1     27,28=12   35,36,41,42=13   43,44,45,46=15     47,48=17    49,50,51,52=18   53,54,55,56=22    57,58,63,64=2 |
""");

    ok(t.delete(t.new Key(51)), 51);
    ok(t.delete(t.new Key(52)), 52);
    ok(t.delete(t.new Key(53)), 53);
    ok(t.delete(t.new Key(54)), 54);
    t.checkFreeList();

    //stop(t);
    t.ok("""
                                      32                                             48                                |
                                      0                                              0.1                               |
                                      14                                             21                                |
                                                                                     6                                 |
          8              26                            42               46                             56              |
          14             14.1                          21               21.1                           6               |
          3              1                             13               15                             18              |
                         12                                             17                             2               |
1,2,7,8=3   9,10,11,12=1     27,28=12   35,36,41,42=13   43,44,45,46=15     47,48=17    49,50,55,56=18   57,58,63,64=2 |
""");

    ok(t.delete(t.new Key(35)), 35);
    ok(t.delete(t.new Key(36)), 36);
    ok(t.delete(t.new Key(45)), 45);
    ok(t.delete(t.new Key(46)), 46);
    t.checkFreeList();

    //stop(t);
    t.ok("""
                                      32                                                             |
                                      0                                                              |
                                      14                                                             |
                                      6                                                              |
          8              26                            46               54                62         |
          14             14.1                          6                6.1               6.2        |
          3              1                             13               17                18         |
                         12                                                               2          |
1,2,7,8=3   9,10,11,12=1     27,28=12   41,42,43,44=13   47,48,49,50=17    55,56,57,58=18    63,64=2 |
""");

    ok(t.delete(t.new Key( 1)),  1);
    ok(t.delete(t.new Key( 2)),  2);
    ok(t.delete(t.new Key( 7)),  7);
    ok(t.delete(t.new Key(64)), 64);
    t.checkFreeList();

    //stop(t);
    t.ok("""
                          32                                                          |
                          0                                                           |
                          14                                                          |
                          6                                                           |
            11                             46               54                62      |
            14                             6                6.1               6.2     |
            3                              18               17                13      |
            12                                                                2       |
8,9,10,11=3   12,27,28=12   41,42,43,44=18   47,48,49,50=17    55,56,57,58=13    63=2 |
""");

    ok(t.delete(t.new Key( 8)),  8);
    ok(t.delete(t.new Key( 9)),  9);
    ok(t.delete(t.new Key(27)), 27);
    ok(t.delete(t.new Key(28)), 28);
    t.checkFreeList();

    //stop(t);
    t.ok("""
                     32                                                          |
                     0                                                           |
                     14                                                          |
                     6                                                           |
14Empty                               46               54                62      |
                                      6                6.1               6.2     |
                                      18               17                13      |
       12                                                                2       |
         10,11,12=12   41,42,43,44=18   47,48,49,50=17    55,56,57,58=13    63=2 |
""");

    ok(t.delete(t.new Key(10)), 10);
    ok(t.delete(t.new Key(11)), 11);
    ok(t.delete(t.new Key(12)), 12);
    t.checkFreeList();

    //stop(t);
    t.ok("""
               46               54                62      |
               0                0.1               0.2     |
               13               17                18      |
                                                  2       |
41,42,43,44=13   47,48,49,50=17    55,56,57,58=18    63=2 |
""");

    ok(t.delete(t.new Key(49)), 49);
    ok(t.delete(t.new Key(50)), 50);
    ok(t.delete(t.new Key(55)), 55);
    ok(t.delete(t.new Key(56)), 56);
    t.checkFreeList();

    //stop(t);
    t.ok("""
               46               62      |
               0                0.1     |
               17               18      |
                                2       |
41,42,43,44=17   47,48,57,58=18    63=2 |
""");

    ok(t.delete(t.new Key(43)), 43);
    ok(t.delete(t.new Key(44)), 44);
    ok(t.delete(t.new Key(57)), 57);
    ok(t.delete(t.new Key(58)), 58);
    t.checkFreeList();

    //stop(t);
    t.ok("""
               62     |
               0      |
               18     |
               2      |
41,42,47,48=18   63=2 |
""");

    ok(t.delete(t.new Key(41)), 41);
    ok(t.delete(t.new Key(42)), 42);
    ok(t.delete(t.new Key(47)), 47);
    t.checkFreeList();

    //stop(t);
    t.ok("""
48,63=0 |
""");

    ok(t.delete(t.new Key(63)), 63);
    t.checkFreeList();

    //stop(t);
    t.ok("""
48=0 |
""");

    ok(t.delete(t.new Key(48)), 48);
    t.checkFreeList();

    //stop(t);
    t.ok("""
=0 |
""");
    ok(t.maxNodeUsed, 23);
   }

  static void test_delete_random()
   {final RandomArray      r = new RandomArray();
    final int              N = RandomArray.r.length;
    final Btree            t = new Btree(16, 16, 16, 4, 3, 40);
    final TreeSet<Integer> s = new TreeSet<>();
    for (int i = 0; i < N; i++)
     {final int a = RandomArray.r[i];
      t.put(a);
      s.add(a);
     }

    for (int i = 0; i < N; i++)
     {final int a = RandomArray.r[i];
      ok( t.find(a).found());

      t.delete(a); s.remove(a);

      ok(!t.find(a).found());

      if (s.size() > 1)
       {ok( t.find(s.first()).found());
        ok( t.find(s.last ()).found());
       }
     }
    t.ok("""
=0 |
""");
    t.checkFreeList();
   }


  static void oldTests()                                                        // Tests thought to be in good shape
   {test_bits();
    test_find();
    test_find_and_insert();
    test_split_leaf_root();
    test_split_branch_root();
    test_put_ascending();
    test_put_descending();
    test_put_random();
    test_delete();
    test_delete_random();
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
