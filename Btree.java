//------------------------------------------------------------------------------
// BTree in a block
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout a btree in a block on a silicon chip.

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
    splitBranchSize,                                                            // The number of key, nexta pairs to split out of a branch
    treeSize;                                                                   // Number of leaves or branches in tree

  final int              root = 0;                                              // The root is always leaf or branch zero
  final Stack<Next>  freeList = new Stack<>();                                  // Free leaf or branches
  final BranchOrLeaf [] nodes;                                                  // The leaves or branches comprising the tree

  final static int
    linesToPrintABranch =  4,                                                   // The number of lines required to print a branch
         maxPrintLevels = 10,                                                   // Maximum number of levels to print in a tree
               maxDepth =  6;                                                   // Maximum depth of any realistic tree

  static boolean debug = false;                                                 // Debugging enabled

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

    nodes = new BranchOrLeaf[TreeSize];                                         // Allocate leaves and branches
    for (int i = 0; i < TreeSize; i++)                                          // Initially all of the leaves or branches are on the free list, except for the root at index 0
     {if (i > 0) freeList.push(new Next(i));
      nodes[i] = new BranchOrLeaf();
     }
    nodes[0].setLeaf();                                                         // Start with the root as a leaf
   }

//D1 Control                                                                    // Testing and control

  void ok(String expected) {Test.ok(print(), expected);}
  void stop() {Test.stop(toString());}
  public String toString() {return print();}

//D1 Components                                                                 // A branch or leaf in the tree

  class BranchOrLeaf                                                            // A branch or leaf in a btree
   {enum State {leaf, branch, free};                                            // The current state of the branch or leaf: as a leaf, as a branch or free waiting for use
    State               state;                                                  // Whether this branch or leaf is a branch, a leaf or free
    final LKDIndex   leafSize = new LKDIndex();                                 // Number of key, data pairs currently contained in leafif a leaf
    final BKNIndex branchSize = new BKNIndex();                                 // Number of key, next pairs currently contained in branch if a branch
    final KeyData[]   keyData = new KeyData[maxKeysPerLeaf];                    // Key, data pairs for when a leaf
    final KeyNext[]   keyNext = new KeyNext[maxKeysPerBranch];                  // Key, next pairs for when a branch
    Next                  top = new Next();                                     // Top next reference for when a branch
    void setLeaf()   {state = State.leaf;}
    void setBranch() {state = State.branch;}
    boolean isLeaf()   {return state == State.leaf;}
    boolean isBranch() {return state == State.branch;}

    BranchOrLeaf()
     {z();
      for (int i = 0; i < maxKeysPerLeaf;   i++) keyData[i] = new KeyData();
      for (int i = 0; i < maxKeysPerBranch; i++) keyNext[i] = new KeyNext();
     }
   }

  void splitRoot()
   {if (rootIsLeaf()) new Leaf  (true).splitLeafRoot();                         // Split the root branch or leaf
    else              root().splitBranchRoot();
   }

  class Leaf                                                                    // Describe a leaf
   {final Next index;                                                           // Index of the leaf
    Leaf()                                                                      // Get a leaf off the free list
     {z();
      if (freeList.size() < 1) stop("No more leaves available");
      index = freeList.pop();
      nodes[index.asInt()].state = BranchOrLeaf.State.leaf;
      zeroLeafSize();
     }
    Leaf(int n)                                                                 // Access a leaf by number
     {z();
      index = new Next(n);
      assertLeaf();
     }
    Leaf(Next n)                                                                // Access a leaf by index
     {z(); index = n;
      assertLeaf();
     }
    Leaf(boolean root)                                                          // Access the root assuming it is a leaf
     {z();
      if (nodes[0].state != BranchOrLeaf.State.leaf) stop("Root is not a leaf");
      index = new Next(0);
     }

    void assertLeaf()
     {if (state() != BranchOrLeaf.State.leaf) stop("Not a leaf at node:", index);
     }

    void freeLeaf()                                                             // Free this leaf
     {if (index == null || index.asInt() == 0) stop("Cannot free root currently a leaf");
      freeList.push(index);
     }

    BranchOrLeaf.State state() {z(); return nodes[index.asInt()].state;     }
    LKDIndex        leafSize() {z(); return nodes[index.asInt()].leafSize;  }
    KeyData[]        keyData() {z(); return nodes[index.asInt()].keyData;   }

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

    void  incLeafSize() {z(); leafSize().inc();}
    void  decLeafSize() {z(); leafSize().dec();}
    void zeroLeafSize() {z(); leafSize().zero();}

    boolean isFull() {z(); return leafSize().asInt() == maxKeysPerLeaf;}

    void update(LKDIndex at, Data Data)
     {z();
      final KeyData[]kd = keyData();
      kd[at.asInt()].set(Data);
     }

    Branch makeIntoBranch()                                                     // Transform this leaf into a branch
     {z();
      nodes[index.asInt()].state = BranchOrLeaf.State.branch;
      return new Branch(index.asInt());
     }

    Key smallestKey()                                                           // Smallest key in a leaf
     {z();
      final KeyData[]kd = keyData();                                            // Key, data pairs in leaf
      final int L = leafSize().asInt();                                         // Size of leaf
      if (L > 0) return kd[0].key;
      stop("No keys in leaf", index);
      return null;
     }
                                                                                // Push a new key, data pair on into the leaf
    void push(int Key, int Data)
     {z();
      final int i = index.asInt();
      final Leaf l = new Leaf(i);
      final KeyData kd = l.keyData()[l.leafSize().asInt()];
      l.incLeafSize();
      kd.set(new Key (Key));
      kd.set(new Data(Data));
     }
                                                                                // Push a new key, data pair on into the leaf
    void push(Key Key, Data Data)
     {z();
      push(Key.asInt(), Data.asInt());
     }

    class FindEqual                                                             // Find the first key in the leaf that is equal to the search key
     {final Key     search;                                                     // Search key
      final boolean  found;                                                     // Whether the key was found
      final Data      data;                                                     // Data associated with the  key
      final LKDIndex index;                                                     // Index of first such key if found

      FindEqual(Key Search)                                                     // Find the first key in the leaf that is equal to the search key
       {z();
        if (state() != BranchOrLeaf.State.leaf) stop("Leaf required, not", state());
        search = Search;
        boolean looking = true;
        final KeyData[]kd = keyData();
        int i; for (i = 0; i < leafSize().asInt() && i < keyData().length && looking; i++)
         {if (kd[i].key.equals(search))
           {z();
            looking = false;
            break;
           }
         }
        if (looking)
         {z();
          data  = null;
          index = null;
          found = false;
         }
        else
         {z();
          data = keyData()[i].data;
          index = new LKDIndex(i);
          found = true;
         }
       }
      Next next() {z(); return Leaf.this.index;}
      public String toString()
       {final StringBuilder s = new StringBuilder();
        s.append("Key:"+search+" found:"+printBit(found));
        if (found) s.append(" data:"+data+" index:"+index.asInt());
        return s.toString();
       }
     }

    class FindFirstGreaterThanOrEqual                                           // Find the first key in the leaf that is equal to or greater than the search key
     {final Key     search;                                                     // Search key
      final boolean  found;                                                     // Whether the key was found
      final LKDIndex first;                                                     // Index of first such key if found
      FindFirstGreaterThanOrEqual(Key Search)                                   // Find the first key in the  leaf that is equal to or greater than the search key
       {z();
        if (state() != BranchOrLeaf.State.leaf) stop("Leaf required, not", state());
        search = Search;
        boolean looking = true;
        final KeyData[]kd = keyData();
        int i; for (i = 0; i < leafSize().asInt() && i < keyData().length && looking; i++)
         {if (kd[i].key.greaterThanOrEqual(search))
           {z();
            looking = false;
            break;
           }
         }
        if (looking)
         {z();
          found = false;
          first = null;
         }
        else
         {z();
          found = true;
          first = new LKDIndex(i);
         }
       }
      public String toString()
       {final StringBuilder s = new StringBuilder();
        s.append("Key:"+search+" found:"+printBit(found));
        if (found) s.append(" first:"+first);
        return s.toString();
       }
     }

    Branch splitLeafRoot()                                                      // Split a leaf which happens to be a full root into two half full leaves while transforming the root leaf into a branch
     {z();
      if (state() != BranchOrLeaf.State.leaf) stop("Leaf required, not", state());
      if (index.asInt() != 0) stop("Not root, but", index);
      final KeyData[]kd = keyData();
      if (!isFull()) stop("Root is not full, but", leafSize());
      final Branch R = makeIntoBranch();
      R.zeroBranchSize();

      final Leaf l = new Leaf(); final KeyData[]lkd = l.keyData();
      final Leaf r = new Leaf(); final KeyData[]rkd = r.keyData();
      for (int i = 0; i < splitLeafSize; i++)
       {l.push(kd[i].key, kd[i].data);
       }
      R.push(kd[splitLeafSize-1].key, l);
      for (int i = splitLeafSize; i < maxKeysPerLeaf;  i++)
       {r.push(kd[i].key, kd[i].data);
       }
      R.top(r);
      return R;
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
   {final Next index;                                                           // Index of the branch
    Branch()                                                                    // get a new branch off the free list
     {z();
      if (freeList.size() < 1) stop("No more branches");
      index = freeList.pop();
      nodes[index.asInt()].state = BranchOrLeaf.State.branch;
      zeroBranchSize();
     }
    Branch(int n)                                                               // Access the specified branch
     {z();
      index = new Next(n);
      assertBranch();
     }
    Branch(Next n)                                                              // Access the branch at this index
     {z();
      index = n;
      assertBranch();
     }
    Branch(boolean root)                                                        // The root is always zero
     {z();
      if (nodes[0].state != BranchOrLeaf.State.branch) stop("Root is not a branch");
      index = new Next(0);
     }

    void assertBranch()
     {if (state() != BranchOrLeaf.State.branch) stop("Not a branch at node:", index);
     }

    void freeBranch()                                                           // Free this branch
     {if (index.asInt() == 0) stop("Cannot free root as branch");
      freeList.push(index);
     }

    BranchOrLeaf.State state() {z(); return nodes[index.asInt()].state;     }
    BKNIndex      branchSize() {z(); return nodes[index.asInt()].branchSize;}
    KeyNext[]        keyNext() {z(); return nodes[index.asInt()].keyNext;   }
    Next                 top() {z(); return nodes[index.asInt()].top;       }
    boolean        hasLeaves() {z(); return isLeaf(top());                  }
    boolean isFull          () {z(); return branchSize().asInt() == maxKeysPerBranch;}

    void incBranchSize      () {z(); branchSize().inc(); }
    void decBranchSize      () {z(); branchSize().dec(); }
    void zeroBranchSize     () {z(); branchSize().zero();}

    void setBranchSize(int size) {z(); branchSize().set(size);}

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

    Key lastKey()
     {z();
      final Next child = top();
      if (isLeaf(child))
       {final Leaf l = new Leaf(child);
        final KeyData[]kd = l.keyData();
        final int       n = l.leafSize().asInt() - 1;
        if (n < 0) stop("No last key in leaf");
        return kd[n].key;
       }
      else
       {final Branch    b = new Branch(child);
        final KeyNext[]kn = keyNext();
        final int       n = branchSize().asInt() - 1;
        if (n < 0) stop("No last key in branch");
        return kn[n].key;
       }
     }

    Branch makeIntoLeaf()
     {z();
      nodes[index.asInt()].state = BranchOrLeaf.State.leaf;
      return new Branch(index.asInt());
     }

    void push(Key Key, Leaf Leaf)
     {z();
      final int n = index.asInt(), i = branchSize().asInt();
      if (i >= maxKeysPerBranch) stop("Branch", n, " is already full");
      nodes[n].keyNext[i].key  = Key;
      nodes[n].keyNext[i].next = Leaf.index;
      incBranchSize();
     }

    void push(Key Key, Branch Branch)
     {z();
      final int n = index.asInt(), i = branchSize().asInt();
      if (0 == Branch.index.asInt()) stop("Cannot push root into a branch");
      if (n == Branch.index.asInt()) stop("Cannot push branch into self");
      if (i >= maxKeysPerBranch) stop("Branch", n, " is already full");
      nodes[n].keyNext[i].key  = Key;
      nodes[n].keyNext[i].next = Branch.index;
      incBranchSize();
     }

    void top(Leaf   Leaf)   {z(); nodes[index.asInt()].top = Leaf.index;}       // Set top to refer to a leaf
    void top(Branch Branch) {z(); nodes[index.asInt()].top = Branch.index;}     // Set top to refer to a leaf

    Key smallestKey()                                                           // Find the smallest key under a branch
     {z();
      Next p = index;
      for (int i = 0; i < maxDepth; i++)
       {if (isLeaf(p)) break;
        final Branch b = new Branch(p);
        final KeyNext[]kn = b.keyNext();
        final int       N = b.branchSize().asInt();
        p = N == 0 ? b.top() : kn[0].next;
       }
      return new Leaf(p).smallestKey();
     }

    int countChildKeys()                                                        // Find the number of keys in the immediate children of this branch
     {z();
      final KeyNext[]kn = keyNext();
      final int N = branchSize().asInt();
      int       C = 0;
      if (isLeaf(top()))
       {z();
        for (int i = 0; i < N; i++)
         {z();
          C += new Leaf(kn[i].next).leafSize().asInt();
         }
        C += new Leaf(top()).leafSize().asInt();
       }
      else
       {z();
        for (int i = 0; i < N; i++)
         {z();
          C += new Branch(kn[i].next).branchSize().asInt();
         }
        C += new Branch(top()).branchSize().asInt();
       }
      return C;
     }

    Leaf pushNewLeaf()                                                          // Create a new child leaf and push it onto the parent branch
     {final int       p = branchSize().asInt();                                 // Current size of branch
      final KeyNext[]kn = keyNext();                                            // Key ], next pairs for this branch
      if (p < maxKeysPerBranch)                                                 // Room in the parent branch to push this new child leaf
       {z();
        final Leaf l = new Leaf();                                              // New leaf
        kn[p] = new KeyNext(new Key(0), l.index);                               // Push child leaf into parent branch
        incBranchSize();                                                        // New size of parent branch
        return l;                                                               // Return leaf so created
       }
      else                                                                      // Reuse branch top because there is no more room in the parent branch
       {z();
        final Leaf l = new Leaf(top());                                         // Return top as a leaf
        l.zeroLeafSize();
        return l;                                                                 // Return top as a leaf
       }
     }

    void unpackLeaf(Leaf leaf, Stack<KeyData> up)                               // Unpack a leaf
     {z();
      final KeyData[]kd = leaf.keyData();                                       // Key, data pairs in leaf
      final int L = leaf.leafSize().asInt();                                    // Size of leaf
      for (int l = 0; l < L; l++)                                               // Unpack each key, data pair in the leaf
       {z();
        up.push(kd[l]);                                                         // Unpack the leaf into a stack of key, data pairs
       }
     }

    void repackLeaves(Key Key, Data Data)                                       // Repack the keys in the leaves under the parent branch
     {z();
      final KeyNext[]kn = keyNext();                                            // Key, next pairs associated with the parent branch
      final Stack<KeyData> up = new Stack<>();                                  // Save area for key, next pairs during repack
      final  int B = branchSize().asInt();                                      // Current size of parent branch
      for   (int b = 0; b < B; b++)                                             // Unpack each leaf referenced
       {z();
        unpackLeaf(new Leaf(kn[b].next), up);                                   // Unpack leaf
       }
      unpackLeaf(new Leaf(top()), up);                                          // Unpack top

      for   (int b = 0; b < B; b++)                                             // Free all the existing leaves except top which will always be there
       {z();
        new Leaf(kn[b].next).freeLeaf();                                        // Free leaf
       }
      new Leaf(top()).zeroLeafSize();                                           // Clear leaf referenced by top
      zeroBranchSize();                                                         // Zero the size of this branch ready for repack of key, next pairs

      boolean inserted = false;                                                 // Whether thenew key, data pair was inserted or not into the stack of key, data pairs ready to be repacked
      for (int i = 0; i < up.size(); i++)                                       // Insert the new key, data pair in the stack of key, data pairs awaiting repacking
       {z();
        if (up.elementAt(i).key.greaterThanOrEqual(Key))                        // Found insertion point
         {z();
          up.insertElementAt(new KeyData(Key, Data), i);                        // Insert new key, data pair
          inserted = true;
          break;
         }
       }
      if (!inserted)                                                            // Key is bigger than all existing keys
       {up.push(new KeyData(Key, Data));
       }

      final int N = up.size();                                                  // Recreate the leaves with the key, data pairs packed in
      Leaf leaf = pushNewLeaf();                                                // First new leaf into branch

      for (int k = 0; k < N; k++)                                               // Repack the leaves
       {z();
        final KeyData source = up.elementAt(k);                                 // Source of repack
        if (leaf.leafSize().equals(maxKeysPerLeaf))                             // Start a new leaf when the current one is full
         {z();
          leaf = pushNewLeaf();                                                 // New leaf
         }

        leaf.keyData()[leaf.leafSize().asInt()] = new KeyData(source);          // Push current key, data pair into the current leaf
        leaf.incLeafSize();                                                     // Move up in leaf
       }

      if (new Leaf(top()).leafSize().asInt() == 0)                              // The top leaf is empty so we replace it with the next top leaf
       {z();
        decBranchSize();                                                        // Pop the last key, next pair off the body of the parent branch
        top(new Leaf(keyNext()[branchSize().asInt()].next));                    // Place last next on top of parent branch
       }

      final int children = branchSize().asInt();                                // Update the keys in each key, next pair in the parent branch to one less than the lowest key in the next child leaf.
      final KeyNext[]Pkn = keyNext();
      for(int b = 0; b < children-1; b++)                                       // Fix keys
       {z();
        Pkn[b].key = new Key(new Leaf(kn[b+1].next).smallestKey().asInt()-1);  // A key smaller than any key in the next sibling leaf
       }
      Pkn[children-1].key = new Key(new Leaf(top()).smallestKey().asInt()-1);   // A key smaller than any key in the leaf refernced by top in the parent
     }

    void unpackBranch(KeyNext pkn, Stack<KeyNext> up)                           // Unpack a branch referenced by a key, next pair from the parent branch
     {z();
      final Branch child = new Branch(pkn.next);                                // Branch to unpack
      final KeyNext[]ckn = child.keyNext();                                     // Key, next pairs in branch
      final int L = child.branchSize().asInt();                                 // Size of child
      for (int l = 0; l < L; l++)                                               // Unpack each key, next pair in the child
       {z();
        up.push(ckn[l]);                                                        // Unpack the child into a stack of key, data pairs
       }
      up.push(new KeyNext(pkn.key, child.top()));                               // Unpack the top of the child using the key from the parent
     }

    void top(Next top)  {z(); nodes[index.asInt()].top = top;}                  // Set the top next reference for this branch

    class FindEqual                                                             // Find the first key in the branch that is equal to the search key
     {final Key     search;                                                     // Search key
      final boolean  found;                                                     // Whether the key was found
      final BKNIndex first;                                                     // Index of first such key if found
      final Next     next;                                                      // Next branch or leaf associated with this key if found

      FindEqual(Key Search)                                                     // Find the first key in the branch that is equal to the search key
       {z();
        search = Search;
        boolean looking = true;
        final KeyNext[]kn = keyNext();
        int i; for (i = 0; i < branchSize().asInt() && i < keyNext().length && looking; i++)
         {z();
          if (kn[i].key.equals(search))
           {z();
           looking = false;
            break;
           }
         }
        if (looking)
         {z();
          found = false;
          first = null;
          next  = null;
         }
        else
         {z();
          found = true;
          first = new BKNIndex(i);
          next  = keyNext()[i].next;
         }
       }
      public String toString()
       {z();
        final StringBuilder s = new StringBuilder();
        s.append("Key:"+search+" found:"+printBit(found));
        if (found) s.append(" first:"+first+" next:"+next);
        return s.toString();
       }
     }

    class FindFirstGreaterThanOrEqual                                           // Find the first key in the branch that is equal to or greater than the search key
     {final Key     search;                                                     // Search key
      final boolean  found;                                                     // Whether the key was found
      final BKNIndex first;                                                     // Index of first such key if found
      final Next      next;                                                     // The corresponding next field or top if no such key was found

      FindFirstGreaterThanOrEqual(Key Search)                                   // Find the first key in the branch that is equal to or greater than the search key
       {z();
        if (state() != BranchOrLeaf.State.branch) stop("Branch required, not", state());
        search = Search;
        boolean looking = true;
        final KeyNext[]kn = keyNext();
        int i; for (i = 0; i < branchSize().asInt() && i < keyNext().length && looking; i++)
         {z();
          if (kn[i].key.greaterThanOrEqual(search))
           {z();
            looking = false;
            break;
           }
         }
        if (looking)
         {z();
          found = false;
          first = null;
          next = top();
         }
        else
         {z();
          found = true;
          first = new BKNIndex(i);
          next  = keyNext()[i].next;
         }
       }
      public String toString()
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
          if (nodes[next].isLeaf())
           {final Leaf l = new Leaf(next);
            l.print(S, level+1);
           }
          else
           {if (next == 0)
              {say("Cannot descend through root from index", i, "in branch", index);
               break;
              }
            final Branch b = new Branch(next);
            b.print(S, level+1);
           }

          S.elementAt(L+0).append(""+keyNext()[i].key.asInt());                 // Key
          S.elementAt(L+1).append(""+index.asInt()+(i > 0 ?  "."+i : ""));      // Branch,key, next pair
          S.elementAt(L+2).append(""+keyNext()[i].next.asInt());
         }
       }
      else                                                                      // Branch is empty so print just the index of the branch
       {S.elementAt(L+0).append(""+index.asInt()+"Empty");
        padStrings(S, level);                                                   // Pad the strings at each level of the tree so we have a vertical face to continue with - a bit like Marc Brunel's tunnelling shield
       }
      final int top = top().asInt();                                            // Top next will always be present
      S.elementAt(L+3).append(top);                                             // Append top next
      if (nodes[top].isLeaf())
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

      padStrings(S, level);                                                     // Pad the strings at each level of the tree so we have a vertical face to continue with - a bit like Marc Brunel's tunnelling shield
     }

    void splitBranchRoot()                                                      // Split a branch which happens to be a full root into two half full branches while retaining the current branch as the root
     {z();
      assertBranch();
      if (index.asInt() != 0) stop("Not root, but", index);
      if (!isFull()) stop("Root is not full, but", branchSize());
      zeroBranchSize();
      final KeyNext[]kn = keyNext();                                            // Key, next pairs for root to be split

      final Branch l = new Branch(); final KeyNext[]lkn = l.keyNext();
      final Branch r = new Branch(); final KeyNext[]rkn = r.keyNext();

      if (hasLeaves())                                                          // The immediate cildren are leaves are children
       {for (int i = 0; i < splitBranchSize; i++)
         {l.push(kn[i].key, new Leaf(kn[i].next));
         }
        push(kn[splitBranchSize].key, l);
        for(int i = maxKeysPerBranch - splitBranchSize; i < maxKeysPerBranch; i++)
         {r.push(kn[i].key, new Leaf(kn[i].next));
         }
       }
      else                                                                      // The immediate children are branches
       {for (int i = 0; i < splitBranchSize; i++)
         {l.push(kn[i].key, new Branch(kn[i].next));
         }
        push(kn[splitBranchSize].key, l);
        for(int i = maxKeysPerBranch - splitBranchSize; i < maxKeysPerBranch; i++)
         {r.push(kn[i].key, new Branch(kn[i].next));
         }
       }
      r.top(top()); top(r);
      l.top(kn[splitBranchSize].next);
     }

    void splitChildOfBranch(FindFirstGreaterThanOrEqual gte)                    // Split the indicated child branch under this parent because the child branch contains the search path for the specified key under this parent
     {z();
      assertBranch();
      if (isFull()) stop("Branch should not be full, but it is, branch:", this);
      final Branch   right = new Branch(gte.next);                              // The child being split
      if (!right.isFull()) stop("Branch:", right, "should be full but is only:", right.branchSize());
      final Branch    left = new Branch();                                      // The split out left hand side off the child being split

      final KeyNext [] pkn = keyNext();                                         // Parent key next
      final KeyNext [] lkn = left.keyNext();                                    // Left child key, next
      final KeyNext [] rkn = right.keyNext();                                   // Child being split
      final KeyNext  split = rkn[splitBranchSize];                              // Split out

      left.top(split.next);                                                     // The top of the left child is  the content of the splitting key

      if (gte.found)                                                            // Insert in body of parent
       {final int I = gte.first.asInt();
        for (int i = branchSize().asInt(); i > I; i--)
         {pkn[i] = pkn[i-1];
         }
        pkn[I] = new KeyNext(split.key, left.index);                            // Insert a key, next pair into the parent
       }
      else                                                                      // Push at end of parent
       {top(right);
        pkn[branchSize().asInt()] = new KeyNext(split.key, left.index);         // Push onto end of parent
       }
      incBranchSize();                                                          // The parent now has one more key, next pair

      for (int i = 0; i < splitBranchSize; i++)                                 // Move out of right chiulks into new left child
       {lkn[i] = rkn[i];
        rkn[i] = rkn[splitBranchSize+1+i];
       }
      left.setBranchSize(splitBranchSize);
      right.setBranchSize(splitBranchSize);
     }
   }

  Branch root() {return new Branch(true);}                                      // Return the root as a branch
  class RootBranch extends Branch {RootBranch() {super(true); z();}}            // The Root is a branch
  class RootLeaf   extends Leaf   {RootLeaf()   {super(true); z();}}            // The Root is a leaf

  boolean rootIsLeaf()    {z(); return nodes[0] .state == BranchOrLeaf.State.leaf;}  // Root is a leaf
  boolean isLeaf(int  bl) {z(); return nodes[bl].state == BranchOrLeaf.State.leaf;}  // Indexed branch or leaf is a leaf
  boolean isLeaf(Next bl) {z(); return isLeaf(bl.asInt());}                          // Indexed branch or leaf is a leaf
  boolean isFull(int bl)                                                        // Whether branch of leaf is full
   {z(); return isLeaf(bl) ? new Leaf(bl).isFull() : new Branch(bl).isFull();
   }

  class Key                                                                     // A key in a leaf or a branch
   {final boolean[] key = new boolean[bitsPerKey];                              // A key is composed of bits
    Key() {z(); }
    Key(int n)  {z(); intToBits(n, key);}
    int asInt() {return bitsToInt(key);}
    boolean equals            (Key Key) {z(); return asInt() == Key.asInt();}
    boolean greaterThanOrEqual(Key Key) {z(); return asInt() >= Key.asInt();}
    public String toString() {return ""+bitsToInt(key);}
    void set(Key Key)
     {z();
      for (int i = 0; i < key.length; i++) key[i] = Key.key[i];
     }
   }

  class Data                                                                    // A data item associated with a key in a leaf
   {final boolean[] data = new boolean[bitsPerData];                            // Data is composed of bits
    Data() {z(); }
    Data(int n) {z(); intToBits(n, data);}
    int asInt() {return bitsToInt(data);}
    public String toString() {z(); return ""+bitsToInt(data);}
    void set(Data Data)
     {z();
      for (int i = 0; i < data.length; i++) data[i] = Data.data[i];
     }
   }

  class Next                                                                    // A next reference from a branch to a leaf or a branch
   {boolean[] next = new boolean[bitsPerNext];                                  // Enough bits to reference a leaf or brahch in the block
    Next() {this(0);}                                                           // The root
    Next(int n)
     {if (n >= treeSize) stop("n must be less than", treeSize, "but is", n) ;
      intToBits(n, next);
     }
    boolean equals(Next that) {z(); return asInt() == that.asInt();}
    int asInt() {return bitsToInt(next);}
    public String toString() {z(); return ""+bitsToInt(next);}
   }

  class KeyData                                                                 // A key, data pair in a leaf
   {Key   key = new Key();                                                      // A key in a leaf
    Data data = new Data();                                                     // Data associated with the key
    KeyData () {z(); }
    KeyData (int Key, int  Data) {z(); intToBits(Key, key.key); intToBits(Data, data.data);}
    KeyData (Key Key, Data Data) {z(); set(Key); set(Data);}
    KeyData (KeyData  KeyData)   {this(KeyData.key, KeyData.data); z();}
    void set(KeyData source)     {z(); key.set(source.key); data.set(source.data);}
    void set(Key  Key)           {z(); key.set(Key);}
    void set(Data Data)          {z(); data.set(Data);}
    public String toString()     {z(); return "KeyData("+key+","+data+")";}
    void notExecuted() {z();}
   }

  class KeyNext                                                                 // A key, next pair in a leaf
   {Key   key = new Key();                                                      // A key in a branch
    Next next = new Next();                                                     // Next branch or leaf
    KeyNext() {z(); }
    KeyNext(int Key, int Next)  {z(); intToBits(Key, key.key); intToBits(Next, next.next);}
    KeyNext(Key Key, Next Next) {z(); set(Key); set(Next);}
    KeyNext(KeyNext  KeyNext)   {this(KeyNext.key, KeyNext.next); z();}
    void set(KeyNext source) {z(); key = source.key; next = source.next;}
    void set(Key  Key)  {z(); key  = Key;}
    void set(Next Next) {z(); next = Next;}
    public String toString() {return "KeyNext("+key+","+next+")";}
   }

  class LKDIndex                                                                // An index to key, data pair in a leaf
   {int i;                                                                      // Index to a key, data, pair in a leaf
    LKDIndex() {z(); }
    LKDIndex(int I) {z(); i = I;}
    public String toString() {return "LKDIndex("+i+")";}
    void inc()  {z(); if (i < maxKeysPerLeaf) ++i; else stop("cannot increment leaf index");}
    void dec()  {z(); if (i > 0)              --i; else stop("cannot decrement leaf index");}
    void zero() {z(); i = 0;}
    boolean equals(int N) {z(); return i == N;}
    int asInt() {return i;}
   }

  class BKNIndex                                                                // An index to key, next pair in a branch
   {int i;                                                                      // Index to a key, next pair in a leaf
    BKNIndex() {z(); }
    BKNIndex(int I) {z(); i = I;}
    public String toString() {return "BKNIndex("+i+")";}
    void inc()  {z(); if (i < maxKeysPerBranch) ++i; else stop("cannot increment branch index");}
    void dec()  {z(); if (i > 0)                --i; else stop("cannot decrement branch index");}
    void zero() {z(); i = 0;}
    void set(int I)       {z(); i = I;}
    boolean equals(int N) {z(); return i == N;}
    int asInt() {return i;}
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
      allFull = true;

      for (int i = 0; i < maxDepth; i++)                                        // Step down through tree
       {z();
        if (!parent.isFull())                                                   // Track last not full branch
         {allFull = false;
          lastNotFull = parent;
         }
        Branch.FindFirstGreaterThanOrEqual down =
          parent.new FindFirstGreaterThanOrEqual(Key);
        final Next n = down.next;
        if (isLeaf(n))                                                          // Found the containing leaf
         {z();
          final Leaf l = new Leaf(n);
          leaf         = l.new FindEqual(Key);
          //say("ok(\""+toString()+"\");");
          return;
         }
        parent = new Branch(n);                                                 // Step down to lower branch
       }
      stop("Search did not terminate in a leaf");
     }

    Leaf      leaf() {z(); return new Leaf(leaf.next());}
    boolean  found() {z(); return leaf.found;}
    LKDIndex index() {z(); return leaf.index;}
    Data      data() {z(); return leaf.data;}

    public String toString()
     {z();
      final StringBuilder s = new StringBuilder();
      s.append("search:"+leaf.search);
      s.append( " leaf:"+leaf().index.asInt());
      s.append(" rootIsLeaf:"+printBit(rootIsLeaf));
      if (!rootIsLeaf) s.append(" parent:"+parent.index);
      s.append(" found:"+printBit(found()));
      if (leaf.found)
       {s.append( " data:"+data());
        s.append(" index:"+index().asInt());
       }
      s.append(" allFull:"+printBit(allFull));
      if (!allFull) s.append( " lastNotFull:"+lastNotFull.index.asInt());
      return s.toString().trim();
     }
   }

  Find find(Key Key) {z(); return new Find(Key);}

  class FindAndInsert extends Find                                              // Insert the specified key and data into the tree if there is room in the target leaf,or update the key with the data if the key already exists
   {Key         key;                                                            // Key to insert
    Data        data;                                                           // Data being inserted or updated
    boolean  success;                                                           // Inserted or updated if true
    boolean inserted;                                                           // Inserted if true

    FindAndInsert(Key Key, Data Data)                                           // Findtheleaf that should contain this key and insert or update it is possible
     {super(Key);                                                               // Find the leaf that should contain this key
      z();
      key  = Key; data = Data;
      final Leaf l = leaf();                                                    // Leaf in which the key should go

      if (leaf.found)                                                           // Found the key in the leaf so update it with the new data
       {z();
        l.update(leaf.index, Data);
        success = true; inserted = false;
        return;
       }

      if (!l.isFull())                                                          // Leaf is not full so we can insert
       {z();
        final Leaf.FindFirstGreaterThanOrEqual fge =
             l.new FindFirstGreaterThanOrEqual(Key);
        final KeyData[]kd = l.keyData();
        if (fge.found)                                                          // Found a matching key so insert into body of leaf
         {z();
          final int F = fge.first.asInt();
          for(int i = maxKeysPerLeaf - 1; i > F; --i)
           {z();
            kd[i].set(kd[i-1]);
           }
          kd[F].set(Key); kd[F].set(Data);
         }
        else                                                                    // No matching key so put at end
         {z();
          final int F = l.leafSize().asInt();
          kd[F].set(Key); kd[F].set(Data);
         }
        l.incLeafSize();
        success = true;
        return;
       }

      if (parent == null)                                                       // No parent so we must have a full leaf as root
       {l.splitLeafRoot();                                                      // Split the full leaf root
        parent = root();                                              // The root is no longer full
       }

      if (parent.countChildKeys() < maxKeysInLeaves)                            // Repackaging the leaves is possible
       {z();
        parent.repackLeaves(Key, Data);
        success = true;
        return;
       }
     }

    Leaf leaf() {return new Leaf(leaf.next());}

    public String toString()
     {final StringBuilder s = new StringBuilder();
      s.append("key:"+key);
      s.append(" data:"+data);
      s.append(" success:"+success);
      if (success) s.append(" inserted:"+inserted);
      s.append(" "+super.toString());
      return s.toString().trim();
     }
   }

  FindAndInsert findAndInsert(Key Key, Data Data)
   {return new FindAndInsert(Key, Data);
   }

//D1 Insertion                                                                  // Insert a key, data pair into the tree or update and existing key with a new datum

  void put(Key Key, Data Data)                                                  // Insert a key, data pair into the tree or update and existing key with a new datum
   {final FindAndInsert f = new FindAndInsert(Key, Data);                       // Try direct insertion with no modifications to the shape of the tree
    if (f.success) return;                                                      // Inserted or updated successfully
    Branch p = f.lastNotFull;                                                   // Start the  insertion at the last not full branch in the path to the containing leaf

    if (f.allFull)                                                              // Start the insertion at the root, after splitting it, because all branches in the path to the leaf for this search were full
     {splitRoot();                                                              // Split the root
      final Branch.FindFirstGreaterThanOrEqual                                  // Step down - from the root
        q = root().new FindFirstGreaterThanOrEqual(Key);
      p = new Branch(q.next);                                                   // Not full because we just split the root and this is one of the fragments but everything else is full so this must be the last not full on the search path of the key
     }

    for (int i = 0; i < maxDepth; i++)                                          // Step down from branch to branch through the tree until reaching a leaf repacking as we go
     {final Branch.FindFirstGreaterThanOrEqual                                  // Step down
        q = p.new FindFirstGreaterThanOrEqual(Key);
      if (isLeaf(q.next))                                                       // Reached a leaf
       {p.repackLeaves(Key, Data);                                              // Add the key, data pair: if it were already there findAndInsert would have already inserted it
        return;
       }
      p.splitChildOfBranch(q);                                                  // Split the child branch in the search path for the key from the parent so the the serach path does not contain a full branch above the containing leaf
      final Branch.FindFirstGreaterThanOrEqual                                  // Step down again as the repack will have altered the local layout
        r = p.new FindFirstGreaterThanOrEqual(Key);
      p = new Branch(r.next);                                                   // We are not on a leaf so continue down through the tree
     }
    stop("Fallen of the end of the tree");                                      // The tree must be missing a leaf
   }

  void put(int Key, int Data)                                                   // Put some test data into the tree
   {put(new Key(Key), new Data(Data));
   }

  void put(int Key)                                                             // Put some test data into the tree
   {put(new Key(Key), new Data(Key));
   }

//D1 Print                                                                      // Print a BTree horizontally

  void padStrings(Stack<StringBuilder> S, int level)                            // Pad a stack of strings so they all have the same length
   {final int N = level * linesToPrintABranch + maxKeysPerLeaf;                 // Number of lines we might want
    for (int i = S.size(); i <= N; ++i) S.push(new StringBuilder());            // Make sure we have a full deck of strings
    int m = 0;                                                                  // Maximum length
    for (StringBuilder s : S) m = m < s.length() ? s.length() : m;              // Find maximum length
    for (StringBuilder s : S)                                                   // Pad each string to maximum length
     {if (s.length() < m) s.append(" ".repeat(m - s.length()));                 // Pad string to maximum length
     }
   }

  String printCollapsed(Stack<StringBuilder> S)                                 // Collapse horizontal representation intot a string
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

//D1 Bits as integers                                                           // Convert between ararys of bits and integers

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
    R.top(     r.index);
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
          7        6             |
                   5             |
2,4,6,8=7  10,12=6    14,16,18=5 |
""");
    Find f1 = t.find(t.new Key( 1));
    Find f2 = t.find(t.new Key( 2));
    Find f3 = t.find(t.new Key( 3));
    Find f4 = t.find(t.new Key( 4));
    Find f5 = t.find(t.new Key( 5));
    Find f6 = t.find(t.new Key( 6));
    Find f7 = t.find(t.new Key( 7));
    Find f8 = t.find(t.new Key( 8));
    Find f9 = t.find(t.new Key( 9));
    Find fa = t.find(t.new Key(10));
    Find fb = t.find(t.new Key(11));
    Find fc = t.find(t.new Key(12));
    Find fd = t.find(t.new Key(13));
    Find fe = t.find(t.new Key(14));
    Find ff = t.find(t.new Key(15));
    Find fg = t.find(t.new Key(16));
    Find fh = t.find(t.new Key(17));
    Find fj = t.find(t.new Key(18));
    Find fk = t.find(t.new Key(19));
    ok(f1, "search:1 leaf:7 rootIsLeaf:n parent:0 found:n allFull:n lastNotFull:0");
    ok(f2, "search:2 leaf:7 rootIsLeaf:n parent:0 found:1 data:12 index:0 allFull:n lastNotFull:0");
    ok(f3, "search:3 leaf:7 rootIsLeaf:n parent:0 found:n allFull:n lastNotFull:0");
    ok(f4, "search:4 leaf:7 rootIsLeaf:n parent:0 found:1 data:14 index:1 allFull:n lastNotFull:0");
    ok(f5, "search:5 leaf:7 rootIsLeaf:n parent:0 found:n allFull:n lastNotFull:0");
    ok(f6, "search:6 leaf:7 rootIsLeaf:n parent:0 found:1 data:16 index:2 allFull:n lastNotFull:0");
    ok(f7, "search:7 leaf:7 rootIsLeaf:n parent:0 found:n allFull:n lastNotFull:0");
    ok(f8, "search:8 leaf:7 rootIsLeaf:n parent:0 found:1 data:18 index:3 allFull:n lastNotFull:0");
    ok(f9, "search:9 leaf:6 rootIsLeaf:n parent:0 found:n allFull:n lastNotFull:0");
    ok(fa, "search:10 leaf:6 rootIsLeaf:n parent:0 found:1 data:20 index:0 allFull:n lastNotFull:0");
    ok(fb, "search:11 leaf:6 rootIsLeaf:n parent:0 found:n allFull:n lastNotFull:0");
    ok(fc, "search:12 leaf:6 rootIsLeaf:n parent:0 found:1 data:22 index:1 allFull:n lastNotFull:0");
    ok(fd, "search:13 leaf:5 rootIsLeaf:n parent:0 found:n allFull:n lastNotFull:0");
    ok(fe, "search:14 leaf:5 rootIsLeaf:n parent:0 found:1 data:24 index:0 allFull:n lastNotFull:0");
    ok(ff, "search:15 leaf:5 rootIsLeaf:n parent:0 found:n allFull:n lastNotFull:0");
    ok(fg, "search:16 leaf:5 rootIsLeaf:n parent:0 found:1 data:26 index:1 allFull:n lastNotFull:0");
    ok(fh, "search:17 leaf:5 rootIsLeaf:n parent:0 found:n allFull:n lastNotFull:0");
    ok(fj, "search:18 leaf:5 rootIsLeaf:n parent:0 found:1 data:28 index:2 allFull:n lastNotFull:0");
    ok(fk, "search:19 leaf:5 rootIsLeaf:n parent:0 found:n allFull:n lastNotFull:0");
   }

  static void test_find_and_insert()
   {final Btree t = test_small_tree();
    //t.stop();
    t.ok("""
          8        12            |
          0        0.1           |
          7        6             |
                   5             |
2,4,6,8=7  10,12=6    14,16,18=5 |
""");
    FindAndInsert fi6 = t.findAndInsert(t.new Key(6), t.new Data(7));           // Update leaf
    //stop(t);
    t.ok("""
          8        12            |
          0        0.1           |
          7        6             |
                   5             |
2,4,6,8=7  10,12=6    14,16,18=5 |
""");


    ok(fi6, "key:6 data:7 success:true inserted:false search:6 leaf:7 rootIsLeaf:n parent:0 found:1 data:7 index:2 allFull:n lastNotFull:0");
    ok(t.find(t.new Key(6)).data(),  7);
    ok(t.find(t.new Key(8)).data(), 18);

    FindAndInsert fi11 = t.findAndInsert(t.new Key(11), t.new Data(11));        // Insert directly into a leaf
    //t.stop();
    t.ok("""
          8           12            |
          0           0.1           |
          7           6             |
                      5             |
2,4,6,8=7  10,11,12=6    14,16,18=5 |
""");
    ok(t.root().countChildKeys(), 10);

    FindAndInsert fi19 = t.findAndInsert(t.new Key(19), t.new Data(19));        // Insert directly into a leaf
    //t.stop();
    t.ok("""
          8           12               |
          0           0.1              |
          7           6                |
                      5                |
2,4,6,8=7  10,11,12=6    14,16,18,19=5 |
""");
    ok(t.root().countChildKeys(), 11);

    FindAndInsert fi5 = t.findAndInsert(t.new Key(5), t.new Data(5));           // Insert by repacking
    //t.stop();
    t.ok("""
          7             13               |
          0             0.1              |
          6             7                |
                        4                |
2,4,5,6=6  8,10,11,12=7    14,16,18,19=4 |
""");
    ok(t.root().countChildKeys(), 12);
   }

  static void test_split_leaf_root()
   {final Btree t = new Btree(8, 8, 4, 4, 3, 8);
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
      7      |
      6      |
2,4=7  6,8=6 |
""");
   ok(b.hasLeaves(), true);
   }

  static void test_split_branch_root()
   {final Btree t = new Btree(8, 8, 4, 4, 3,16);
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
         40         80            120           |
         0          0.1           0.2           |
         15         14            13            |
                                  12            |
20,40=15   60,80=14    100,120=13    140,160=12 |
""");
    R.splitBranchRoot();
    //stop(t);
    t.ok("""
                    80                         |
                    0                          |
                    11                         |
                    10                         |
         40                      120           |
         11                      10            |
         15                      13            |
         14                      12            |
20,40=15   60,80=14   100,120=13    140,160=12 |
""");

    final Leaf il = t.new Leaf();
    il.push( 90, 91);
    il.push( 92, 93);

    final Leaf ir = t.new Leaf();
    ir.push( 96, 97);
    ir.push( 98, 99);
   }

  static void test_put_ascending()
   {final Btree t = new Btree(8, 8, 8, 4, 3, 40);
    final int N = 64;
    for (int i = 1; i <= N; i++) t.put(i);
    //stop(t);
    t.ok("""
                                                         16                                                                  32                                                                                                                                          |
                                                         0                                                                   0.1                                                                                                                                         |
                                                         20                                                                  10                                                                                                                                          |
                                                                                                                             19                                                                                                                                          |
                        8                                                                  24                                                                   40                                48                                                                     |
                        20                                                                 10                                                                   19                                19.1                                                                   |
                        33                                                                 24                                                                   14                                9                                                                      |
                        28                                                                 18                                                                                                     32                                                                     |
           4                            12                                20                                28                                 36                                44                                 52               56                60                |
           33                           28                                24                                18                                 14                                9                                  32               32.1              32.2              |
           36                           31                                27                                23                                 17                                13                                 8                11                6                 |
           39                           34                                29                                25                                 21                                15                                                                    7                 |
1,2,3,4=36   5,6,7,8=39   9,10,11,12=31   13,14,15,16=34   17,18,19,20=27   21,22,23,24=29   25,26,27,28=23   29,30,31,32=25    33,34,35,36=17   37,38,39,40=21   41,42,43,44=13   45,46,47,48=15     49,50,51,52=8   53,54,55,56=11     57,58,59,60=6     61,62,63,64=7 |
""");
   }

  static void test_put_descending()
   {final Btree t = new Btree(8, 8, 8, 4, 3, 60);
    final int N = 64;
    for (int i = N; i > 0; --i) t.put(i);
    //stop(t);
    t.ok("""
                                                                                                                            32                                                                  48                                                                   |
                                                                                                                            0                                                                   0.1                                                                  |
                                                                                                                            9                                                                   25                                                                   |
                                                                                                                                                                                                24                                                                   |
                                                       16                                24                                                                   40                                                                   56                                |
                                                       9                                 9.1                                                                  25                                                                   24                                |
                                                       8                                 16                                                                   32                                                                   47                                |
                                                                                         23                                                                   39                                                                   46                                |
          4          8                12                                20                                 28                                36                                44                                 52                                60               |
          8          8.1              8.2                               16                                 23                                32                                39                                 47                                46               |
          2          7                10                                17                                 26                                33                                40                                 48                                59               |
                                      3                                 11                                 18                                27                                34                                 41                                49               |
1,2,3,4=2  5,6,7,8=7    9,10,11,12=10    13,14,15,16=3   17,18,19,20=17   21,22,23,24=11    25,26,27,28=26   29,30,31,32=18   33,34,35,36=33   37,38,39,40=27   41,42,43,44=40   45,46,47,48=34    49,50,51,52=48   53,54,55,56=41   57,58,59,60=59   61,62,63,64=49 |
""");
   }


  static int[]random_array()                                                    // Random array
   {final int[]r = {27, 442, 545, 317, 511, 578, 391, 993, 858, 586, 472, 906, 658, 704, 882, 246, 261, 501, 354, 903, 854, 279, 526, 686, 987, 403, 401, 989, 650, 576, 436, 560, 806, 554, 422, 298, 425, 912, 503, 611, 135, 447, 344, 338, 39, 804, 976, 186, 234, 106, 667, 494, 690, 480, 288, 151, 773, 769, 260, 809, 438, 237, 516, 29, 376, 72, 946, 103, 961, 55, 358, 232, 229, 90, 155, 657, 681, 43, 907, 564, 377, 615, 612, 157, 922, 272, 490, 679, 830, 839, 437, 826, 577, 937, 884, 13, 96, 273, 1, 188};
    return r;
   }

  static void test_put_random()                                                 // Load a BTree from random data
   {final int[]r = random_array();
    final int N = r.length;
    final Btree t = new Btree(16, 16, 16, 4, 3, N);

    for (int i = 0; i < N; i++) t.put(r[i]);
    //stop(t);
    t.ok("""
                                                                                                                                                                                                                    402                                                                                                                               577                                                                                                                                                                                                                      |
                                                                                                                                                                                                                    0                                                                                                                                 0.1                                                                                                                                                                                                                      |
                                                                                                                                                                                                                    34                                                                                                                                56                                                                                                                                                                                                                       |
                                                                                                                                                                                                                                                                                                                                                      55                                                                                                                                                                                                                       |
                                                              150                                                                               278                                                                                                                                    500                                                                                                                               689                                                            857                                                                                    |
                                                              34                                                                                34.1                                                                                                                                   56                                                                                                                                55                                                             55.1                                                                                   |
                                                              33                                                                                51                                                                                                                                     54                                                                                                                                27                                                             68                                                                                     |
                                                                                                                                                77                                                                                                                                     90                                                                                                                                                                                               89                                                                                     |
              38               89                  134                              187                   236                    271                                   337                   375                                          436                   471                                          525                   563                                      614                   666                                          803                   829                                       905                   936                    986                |
              33               33.1                33.2                             51                    51.1                   51.2                                  77                    77.1                                         54                    54.1                                         90                    90.1                                     27                    27.1                                         68                    68.1                                      89                    89.1                   89.2               |
              38               30                  9                                10                    62                     32                                    91                    50                                           69                    53                                           42                    80                                       78                    26                                           46                    17                                        84                    60                     18                 |
                                                   11                                                                            19                                                          35                                                                 14                                                                 22                                                             20                                                                 13                                                                                     23                 |
1,13,27,29=38   39,43,55,72=30     90,96,103,106=9     135=11    151,155,157,186=10    188,229,232,234=62     237,246,260,261=32     272,273=19     279,288,298,317=91    338,344,354,358=50     376,377,391,401=35    403,422,425,436=69    437,438,442,447=53     472,480,490,494=14    501,503,511,516=42    526,545,554,560=80     564,576,577=22    578,586,611,612=78    615,650,657,658=26     667,679,681,686=20    690,704,769,773=46    804,806,809,826=17     830,839,854=13     858,882,884,903=84    906,907,912,922=60     937,946,961,976=18     987,989,993=23 |
""");
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
   }

  static void newTests()                                                        // Tests being worked on
   {oldTests();
    //writeCoverage();
    test_put_random();
   }

  public static void main(String[] args)                                        // Test if called as a program
   {try                                                                         // Get a traceback in a format clickable in Geany if something goes wrong to speed up debugging.
     {if (github_actions) oldTests(); else newTests();                          // Tests to run
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
