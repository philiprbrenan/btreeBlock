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
    treeSize;                                                                   // Number of leaves or branches in tree

  final int              root = 0;                                              // The root is always leaf or branch zero
  final Stack<Next>  freeList = new Stack<>();                                  // Free leaf or branches
  final BranchOrLeaf [] nodes;                                                  // The leaves or branches comprising the tree

  final static int
    linesToPrintABranch =  4,                                                   // The number of lines required to print a branch
         maxPrintLevels = 10,                                                   // Maximum number of levels to print in a tree
               maxDepth =  6;                                                   // Maximum depth of any realistic tree

//D1 Construction                                                               // Create a BTree from nodes which can be branches or leaves.  The data associated with the BTree is stored only in the leaves opposite the keys

  Btree                                                                         // Define a BTree with the specified dimensions
   (int BitsPerKey, int BitsPerData, int BitsPerNext, int MaxKeysPerLeaf,
    int MaxKeysPerBranch, int TreeSize)
   {z();
    bitsPerKey        = BitsPerKey;
    bitsPerData       = BitsPerData;
    bitsPerNext       = BitsPerNext;
    maxKeysPerLeaf    = MaxKeysPerLeaf;
    maxKeysPerBranch  = MaxKeysPerBranch;
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
     {z(); index = new Next(n);
     }
    Leaf(Next n)                                                                // Access a leaf by index
     {z(); index = n;
     }
    Leaf(boolean root)                                                          // Access the root assuming it is a leaf
     {z();
      if (nodes[0].state != BranchOrLeaf.State.leaf) stop("Root is not a leaf");
      index = new Next(0);
     }

    void freeLeaf() {freeList.push(index);}                                     // Free this leaf

    BranchOrLeaf.State state() {z(); return nodes[index.asInt()].state;     }
    LKDIndex        leafSize() {z(); return nodes[index.asInt()].leafSize;  }
    KeyData[]        keyData() {z(); return nodes[index.asInt()].keyData;   }

    void  incLeafSize() {z(); leafSize().inc();}
    void  decLeafSize() {z(); leafSize().dec();}
    void zeroLeafSize() {z(); leafSize().zero();}

    boolean isFull() {z(); return leafSize().i == maxKeysPerLeaf;}

    void update(LKDIndex at, Data Data)
     {z();
      final KeyData[]kd = keyData();
      kd[at.i].set(Data);
     }

    Branch makeIntoBranch()                                                     // Transform this leaf into a branch
     {z();
      nodes[index.asInt()].state = BranchOrLeaf.State.branch;
      return new Branch(index.asInt());
     }
                                                                                // Push a new key, data pair on into the leaf
    void push(int Key, int Data)
     {z();
      final KeyData kd = nodes[index.asInt()].keyData[nodes[index.asInt()].leafSize.i++];
      kd.set(new Key (Key));
      kd.set(new Data(Data));
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
        int i; for (i = 0; i < leafSize().i && i < keyData().length && looking; i++)
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
        if (found) s.append(" data:"+data+" index:"+index.i);
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
        int i; for (i = 0; i < leafSize().i && i < keyData().length && looking; i++)
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

    void print(Stack<StringBuilder>S, int level)                                // Print leaf horizontally
     {padStrings(S, level);
      final StringBuilder s = new StringBuilder();                              // String builder
      final int K = leafSize().i;
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
     }
    Branch(Next n)                                                              // Access the branch at this index
     {z();
      index = n;
     }
    Branch(boolean root)                                                        // The root is always zero
     {z();
      if (nodes[0].state != BranchOrLeaf.State.branch) stop("Root is not a branch");
      index = new Next(0);
     }

    void freeBranch() {freeList.push(index);}                                   // Free this branch

    BranchOrLeaf.State state()   {z(); return nodes[index.asInt()].state;     }
    BKNIndex       branchSize()  {z(); return nodes[index.asInt()].branchSize;}
    final KeyNext[]   keyNext()  {z(); return nodes[index.asInt()].keyNext;   }
    final Next            top()  {z(); return nodes[index.asInt()].top;       }

    void incBranchSize()  {z(); branchSize().inc();}
    void decBranchSize()  {z(); branchSize().dec();}
    void zeroBranchSize() {z(); branchSize().zero();}
    boolean isFull()      {z(); return branchSize().i == maxKeysPerBranch;}

    Branch makeIntoLeaf()
     {z();
      nodes[index.asInt()].state = BranchOrLeaf.State.leaf;
      return new Branch(index.asInt());
     }

    void push(Key Key, Leaf Leaf)
     {z();
      final int n = index.asInt(), i = branchSize().i;
      if (i >= maxKeysPerBranch) stop("Branch", n, " is already full");
      nodes[n].keyNext[i].key  = Key;
      nodes[n].keyNext[i].next = Leaf.index;
      incBranchSize();
     }

    void push(Key Key, Branch Branch)
     {z();
      final int n = index.asInt(), i = branchSize().i;
      if (n == 0) stop("Cannot push root into a branch");
      if (n == index.asInt()) stop("Cannot push branch into self");
      if (i >= maxKeysPerBranch) stop("Branch", n, " is already full");
      nodes[n].keyNext[i].key  = Key;
      nodes[n].keyNext[i].next = Branch.index;
      incBranchSize();
     }

    void top(Leaf   Leaf)   {z(); nodes[index.asInt()].top = Leaf.index;}       // Set top to refer to a leaf
    void top(Branch Branch) {z(); nodes[index.asInt()].top = Branch.index;}     // Set top to refer to a leaf

    int countChildKeys()                                                        // Find the number of keys in the immediate children of this branch
     {z();
      final KeyNext[]kn = keyNext();
      final int N = branchSize().i;
      int       C = 0;
      if (isLeaf(top()))
       {z();
        for (int i = 0; i < N; i++)
         {z();
          C += new Leaf(kn[i].next).leafSize().i;
         }
        C += new Leaf(top()).leafSize().i;
       }
      else
       {z();
        for (int i = 0; i < N; i++)
         {z();
          C += new Branch(kn[i].next).branchSize().i;
         }
        C += new Branch(top()).branchSize().i;
       }
      return C;
     }

    Leaf pushNewLeaf()                                                          // Create a new leaf and push it onto this branch
     {final Leaf leaf = new Leaf();

      final int p = branchSize().i;                                             // Current size of branch
      if (p < maxKeysPerBranch)                                                 // Room in the branch
       {z();
        keyNext()[p].next = leaf.index;
        incBranchSize();                                                        // New size of branch
        return leaf;                                                            // Return leaf so created
       }
      else                                                                      // Reuse top because there is no more room in the branch
       {z();
        say("Use top");
        return new Leaf(top());
       }
     }

    void unpackLeaf(Leaf leaf, Stack<KeyData> up)                               // Unpack a leaf
     {z();
      final KeyData[]kd = leaf.keyData();
      final int L = leaf.leafSize().i;
      for (int l = 0; l < L; l++)
       {z();
        up.push(kd[l]);
       }
     }

    void repackLeaves(Key Key, Data Data)                                       // Repack the keys in the leaves under this branch
     {z();
      final KeyNext[]kn = keyNext();                                            // Key, next pairs associated with this branch
      final Stack<KeyData> up = new Stack<>();                                  // Save area for key, next pairs during repack
      final  int B = branchSize().i;                                            // Current size of branch
      for   (int b = 0; b < B; b++)                                             // Unpack each leaf referenced
       {z();
        final Leaf leaf = new Leaf(kn[b].next);
        unpackLeaf(leaf, up);
       }
      unpackLeaf(new Leaf(top()), up);                                          // Include top

      for   (int b = 0; b < B; b++)                                             // Free all the existing leaves except top which will always be there
       {z();
        final Leaf leaf = new Leaf(kn[b].next);
        leaf.freeLeaf();
       }
      new Leaf(top()).zeroLeafSize();                                           // Clear leaf referenced by top
      zeroBranchSize();                                                         // Zero the size of this branch ready for repack of key, next pairs

      for (int i = 0; i < up.size(); i++)                                       // Insert the new key, data pair
       {z();
        final KeyData kd = up.elementAt(i);
        if (kd.key.greaterThanOrEqual(Key))                                     // Insert new key, data pair
         {z();
          up.insertElementAt(new KeyData(Key, Data), i);
          break;
         }
       }

      final int N = up.size();                                                  // Recreate the leaves with the key, data pairs packed in
      Leaf leaf = pushNewLeaf();                                                // First new leaf into branch

      for (int k = 0; k < N; k++)                                               // Repack the leaves
       {z();
        final KeyData source = up.elementAt(k);                                 // Source of repack
        if (leaf.leafSize().equals(maxKeysPerLeaf))                             // Start a new leaf when the current one is full
         {z();
         leaf = pushNewLeaf();
         }

        leaf.keyData()[leaf.leafSize().i] = new KeyData(source);                // Current location
        leaf.incLeafSize();
       }

      if (new Leaf(top()).leafSize().i == 0)
       {z();
        decBranchSize();
        final int n = branchSize().i;
        final KeyNext[]bkn = keyNext();
        top(new Leaf(bkn[n].next));
       }
     }

    void top(Next top)  {z(); nodes[index.asInt()].top = top;}

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
        int i; for (i = 0; i < branchSize().i && i < keyNext().length && looking; i++)
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
        int i; for (i = 0; i < branchSize().i && i < keyNext().length && looking; i++)
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
      final int K = branchSize().i;
      final int L = level * linesToPrintABranch;

      if (K > 0)                                                                // Branch has key, next pairs
       {for  (int i = 0; i < K; i++)
         {final int next = keyNext()[i].next.asInt();                              // Each key, next pair
          if (nodes[next].isLeaf())
           {final Leaf l = new Leaf(next);
            l.print(S, level+1);
           }
          else
           {if (next == 0) stop("Cannot descend through root from index", i, "in branch", index);
            final Branch b = new Branch(next);
            b.print(S, level+1);
           }

          S.elementAt(L+0).append(""+keyNext()[i].key.asInt());                 // Key
          S.elementAt(L+1).append(""+index.asInt()+(i > 0 ?  "."+i : ""));     // Branch,key, next pair
          S.elementAt(L+2).append(""+keyNext()[i].next.asInt());
         }
       }
      else                                                                      // Branch is empty so print just the index of the branch
       {S.elementAt(L+0).append(""+index.asInt()+"-");
       }
      final int top = top().asInt();                                            // Top next will always be present
      S.elementAt(L+3).append(top);                                             // Append top next
      if (nodes[top].isLeaf())
       {final Leaf l = new Leaf(top);
        l.print(S, level+1);
       }
      else
       {if (top == 0) stop("Cannot descend through root from top in branch", index);
        final Branch b = new Branch(top);
        b.print(S, level+1);
       }

      padStrings(S, level);                                                     // Pad the strings at each level of the tree so we have a vertical face to continue with - a bit like Marc Brunel's tunnelling shield
     }
   }

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
   {final boolean[] data = new boolean[bitsPerData];                                  // Data is composed of bits
    Data() {z(); }
    Data(int n) {z(); intToBits(n, data);}
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
    KeyNext(int Key, int Next) {z(); intToBits(Key, key.key); intToBits(Next, next.next);}
    void set(KeyNext source) {z(); key = source.key; next = source.next;}
    public String toString() {return "KeyNext("+key+","+next+")";}
    void key (Key  Key)  {z(); key  = Key;}
    void next(Next Next) {z(); next = Next;}
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
   }

  class BKNIndex                                                                // An index to key, next pair in a branch
   {int i;                                                                      // Index to a key, next pair in a leaf
    BKNIndex() {z(); }
    BKNIndex(int I) {z(); i = I;}
    public String toString() {return "BKNIndex("+i+")";}
    void inc()  {z(); if (i < maxKeysPerBranch) ++i; else stop("cannot increment branch index");}
    void dec()  {z(); if (i > 0)                --i; else stop("cannot decrement branch index");}
    void zero() {z(); i = 0;}
    boolean equals(int N) {z(); return i == N;}
   }

//D1 Find                                                                       // Find the data associated with a key

  class Find                                                                    // Find the data associated with a key in a tree
   {Leaf.FindEqual leaf;                                                        // Details of the leaf we found using this search
    boolean rootIsLeaf;                                                         // The root is a leaf
    boolean allFull;                                                            // All the branches were full if true
    Branch parent;                                                              // If the root is a branch (not a leaf) then this branch is the aprent of the leaf
    Branch lastNotFull;                                                         // Last branch not full if not all the branches were full

    Find(Key Key)
     {z();
      if (rootIsLeaf())                                                         // The root is a leaf
       {z();
        final Leaf l = new Leaf(0);
        leaf         = l.new FindEqual(Key);
        allFull      = true;
        rootIsLeaf   = true;
        return;
       }

      parent  = new Branch(0);                                                  // Parent starts at root which is known to be a branch
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
        s.append(" index:"+index().i);
       }
      s.append(" allFull:"+printBit(allFull));
      if (!allFull) s.append( " lastNotFull:"+lastNotFull.index.asInt());
      return s.toString().trim();
     }
   }

  Find find(Key Key) {z(); return new Find(Key);}

/*
          8        12            |
          0        0.1           |
          3        2             |
                   1             |
2,4,6,8=3  10,12=2    14,16,18=1 |
*/

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
          final int F = fge.first.i;
          for(int i = maxKeysPerLeaf - 1; i > F; --i)
           {z();
            kd[i].set(kd[i-1]);
           }
          kd[F].set(Key); kd[F].set(Data);
         }
        else                                                                    // No matching key so put at end
         {z();
          final int F = maxKeysPerLeaf;
          kd[F-1].set(Key); kd[F-1].set(Data);
         }
        l.incLeafSize();
        return;
       }

      if (parent.countChildKeys() < maxKeysInLeaves)                            // Repackaging the leaves is possible
       {z();
        parent.repackLeaves(Key, Data);
        return;
       }
     }

    Leaf leaf() {return new Leaf(leaf.next());}

    public String toString()
     {final StringBuilder s = new StringBuilder();                                 //
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

    final StringBuilder t = new StringBuilder();
    for  (StringBuilder s : S)
     {final String l = s.toString();
      if (l.isBlank()) continue;
      t.append(l+"|\n");
     }
    return t.toString();
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
    final Branch R = t.new Leaf(0).makeIntoBranch();
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
   {final Btree  t = test_small_tree();
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
    ok(t.new Branch(true).countChildKeys(), 10);

    FindAndInsert fi19 = t.findAndInsert(t.new Key(19), t.new Data(19));        // Insert directly into a leaf
    //t.stop();
    t.ok("""
          8           12               |
          0           0.1              |
          7           6                |
                      5                |
2,4,6,8=7  10,11,12=6    14,16,18,19=5 |
""");
    ok(t.new Branch(true).countChildKeys(), 11);

    FindAndInsert fi5 = t.findAndInsert(t.new Key(5), t.new Data(5));           // Insert by repacking
    //t.stop();
    t.ok("""
          8             12               |
          0             0.1              |
          6             7                |
                        4                |
2,4,5,6=6  8,10,11,12=7    14,16,18,19=4 |
""");
    ok(t.new Branch(true).countChildKeys(), 12);
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_bits();
    test_find();
    test_find_and_insert();
   }

  static void newTests()                                                        // Tests being worked on
   {oldTests();
    writeCoverage();
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
