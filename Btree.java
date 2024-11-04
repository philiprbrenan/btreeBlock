//------------------------------------------------------------------------------
// BTree in a block
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout a btree in a block on a silicon chip.

import java.util.*;

class Btree extends Test                                                        // Manipulate a btree
 {final int
   bitsPerKey,                                                                  // Size of a key in bits
   bitsPerData,                                                                 // Size of data associated with key in bits
   bitsPerNext,                                                                 // Bits in a next reference
   maxKeysPerLeaf,                                                              // Maximum number of leafs in a key
   maxKeysPerBranch,                                                            // Maximum number of keys in a branch
   treeSize;                                                                    // Number of leaves or branches in tree

  final int              root = 0;                                              // The root is always leaf or branch zero
  final Stack<Next>  freeList = new Stack<>();                                  // Free leaf or branches
  final BranchOrLeaf [] nodes;                                                  // The leaves or branches comprising the tree

  final static int linesToPrintABranch = 4;                                     // The number of lines required to print a branch
  final static int maxPrintLevels = 10;                                         // Maximum number of levels to print in a tree

//D1 Construction                                                               // Create a BTree from nodes which can be branches or leaves.  The data associated with the BTree is stored only in the leaves opposite the keys

  Btree                                                                         // Define a BTree with the specified dimensions
   (int BitsPerKey, int BitsPerData, int BitsPerNext, int MaxKeysPerLeaf,
    int MaxKeysPerBranch, int TreeSize)
   {bitsPerKey                = BitsPerKey;
    bitsPerData               = BitsPerData;
    bitsPerNext               = BitsPerNext;
    maxKeysPerLeaf            = MaxKeysPerLeaf;
    maxKeysPerBranch          = MaxKeysPerBranch;
    treeSize                  = TreeSize;
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
    final Next            top = new Next();                                     // Top next reference for when a branch
    void setLeaf()   {state = State.leaf;}
    void setBranch() {state = State.branch;}
    boolean isLeaf()   {return state == State.leaf;}
    boolean isBranch() {return state == State.branch;}

    BranchOrLeaf()
     {for (int i = 0; i < maxKeysPerLeaf;   i++) keyData[i] = new KeyData();
      for (int i = 0; i < maxKeysPerBranch; i++) keyNext[i] = new KeyNext();
     }
   }

  class Leaf                                                                    // Describe a leaf
   {final Next leaf;                                                            // Index of the leaf
    Leaf()
     {assert freeList.size() > 1;
      leaf = freeList.pop();
      nodes[leaf.asInt()].state = BranchOrLeaf.State.leaf;
     }
    Leaf(int n)
     {leaf = new Next(n);
     }
    Leaf(boolean root)
     {assert nodes[0].state == BranchOrLeaf.State.leaf;
      leaf = new Next(0);
     }
    BranchOrLeaf.State state() {return nodes[leaf.asInt()].state;     }
    LKDIndex        leafSize() {return nodes[leaf.asInt()].leafSize;  }
    final KeyData[]  keyData() {return nodes[leaf.asInt()].keyData;   }

    Branch makeBranch()
     {nodes[leaf.asInt()].state = BranchOrLeaf.State.branch;
      return new Branch(leaf.asInt());
     }

    void push(int Key, int Data)
     {final KeyData kd = nodes[leaf.asInt()].keyData[nodes[leaf.asInt()].leafSize.i++];
      kd.key (new Key (Key));
      kd.data(new Data(Data));
     }

    class FindFirstGreaterThanOrEqual                                           // Find the first key in the leaf that is equal to or greater than the search key
     {final Key     search;                                                     // Search key
      final LKDIndex first;                                                     // Index of first such key if found
      final boolean  found;                                                     // Whether the key was found
      FindFirstGreaterThanOrEqual(Key Search)                                   // Find the first key in the  leaf that is equal to or greater than the search key
       {assert state() == BranchOrLeaf.State.leaf;
        search = Search;
        boolean looking = true;
        int i; for (i = 0; i < leafSize().i && i < keyData().length && looking; i++)
         {if (keyData()[i].key.greaterThanOrEqual(search))
           {looking = false;
            break;
           }
         }
        if (looking)
         {found = false;
          first = null;
         }
        else
         {found = true;
          first = new LKDIndex(i);
         }
       }
     }

    void print(Stack<StringBuilder>S, int level)                                // Print leaf horizontally
     {padStrings(S, level);
      final StringBuilder s = new StringBuilder();                              // String builder
      final int K = leafSize().i;
      for  (int i = 0; i < K; i++) s.append(""+keyData()[i].key+",");
      if (s.length() > 0) s.setLength(s.length()-1);                            // Remove trailing comma if present
      s.append("="+leaf+" ");
      S.elementAt(level*linesToPrintABranch).append(s.toString());
      padStrings(S, level);
     }
   }

  class Branch                                                                  // Describe a branch
   {final Next branch;                                                          // Index of the branch
    Branch()
     {assert freeList.size() > 1;
      branch = freeList.pop();
      nodes[branch.asInt()].state = BranchOrLeaf.State.branch;
     }
    Branch(int n)
     {branch = new Next(n);
     }
    Branch(boolean root)                                                        // The root is always zero
     {assert nodes[0].state == BranchOrLeaf.State.branch;
      branch = new Next(0);
     }
    BranchOrLeaf.State state()   {return nodes[branch.asInt()].state;     }
    BKNIndex       branchSize()  {return nodes[branch.asInt()].branchSize;}
    final KeyNext[]   keyNext()  {return nodes[branch.asInt()].keyNext;   }
    final Next            top()  {return nodes[branch.asInt()].top;       }

    Branch makeLeaf()
     {nodes[branch.asInt()].state = BranchOrLeaf.State.leaf;
      return new Branch(branch.asInt());
     }

    void push(int Key, int Next)
     {nodes[branch.asInt()].keyNext[nodes[branch.asInt()].branchSize.i++] =
        new KeyNext(Key, Next);
     }

    void top(int top)  {nodes[branch.asInt()].top.set(top);}

    class FindFirstGreaterThanOrEqual                                           // Find the first key in the branch that is equal to or greater than the search key
     {final Key     search;                                                     // Search key
      final BKNIndex first;                                                     // Index of first such key if found
      final boolean  found;                                                     // Whether the key was found
      final Next      next;                                                     // The corresponding next field or top if no such key was found

      FindFirstGreaterThanOrEqual(Key Search)                                   // Find the first key in the branch that is equal to or greater than the search key
       {assert state() == BranchOrLeaf.State.branch;
        search = Search;
        boolean looking = true;
        int i; for (i = 0; i < branchSize().i && i < keyNext().length && looking; i++)
         {if (keyNext()[i].key.greaterThanOrEqual(search))
           {looking = false;
            break;
           }
         }
        if (looking)
         {found = false;
          first = null;
          next = top();
         }
        else
         {found = true;
          first = new BKNIndex(i);
          next  = keyNext()[i].next;
         }
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
           {final Branch b = new Branch(next);
            b.print(S, level+1);
           }

          S.elementAt(L+0).append(""+keyNext()[i].key.asInt());                 // Key
          S.elementAt(L+1).append(""+branch.asInt()+(i > 0 ?  "."+i : ""));     // Branch,key, next pair
          S.elementAt(L+2).append(""+keyNext()[i].next.asInt());
         }
       }
      else                                                                      // Branch is empty so print just the index of the branch
       {S.elementAt(L+0).append(""+branch.asInt()+"-");
       }
      final int top = top().asInt();                                            // Top next will always be present
      S.elementAt(L+3).append(top);                                             // Append top next
      if (nodes[top].isLeaf())
       {final Leaf l = new Leaf(top);
        l.print(S, level+1);
       }
      else
       {final Branch b = new Branch(top);
        b.print(S, level+1);
       }

      padStrings(S, level);                                                     // Pad the strings at each level of the tree so we have a vertical face to continue with - a bit like Marc Brunel's tunnelling shield
     }
   }

  class RootBranch extends Branch {RootBranch() {super(true);}}                 // The Root is a branch
  class RootLeaf   extends Leaf   {RootLeaf()   {super(true);}}                 // The Root is a leaf

  boolean rootIsLeaf() {return nodes[0].state == BranchOrLeaf.State.leaf;}      // Root is a leaf

  class Key                                                                     // A key in a leaf or a branch
   {final boolean[] key = new boolean[bitsPerKey];                                    // A key is composed of bits
    Key() {}
    Key(int n)  {intToBits(n, key);}
    int asInt() {return bitsToInt(key);}
    boolean equals            (Key Key) {return asInt() == Key.asInt();}
    boolean greaterThanOrEqual(Key Key) {return asInt()  > Key.asInt();}
    public String toString() {return ""+bitsToInt(key);}
    void set(Key Key) {System.arraycopy(Key.key, 0, key, 0, key.length);}
   }

  class Data                                                                    // A data item associated with a key in a leaf
   {final boolean[] data = new boolean[bitsPerData];                                  // Data is composed of bits
    Data() {}
    Data(int n) {intToBits(n, data);}
    public String toString() {return ""+bitsToInt(data);}
    void set(Data Data) {System.arraycopy(Data.data, 0, data, 0, data.length);}
   }

  class Next                                                                    // A next reference from a branch to a leaf or a branch
   {boolean[] next = new boolean[bitsPerNext];                                  // Enough bits to reference a leaf or brahch in the block
    Next() {this(0);}                                                           // The root
    Next(int n) {set(n);}
    void set(int n)
     {assert n < treeSize;
      intToBits(n, next);
     }
    int asInt() {return bitsToInt(next);}
    public String toString() {return ""+bitsToInt(next);}
    void set(Next Next) {System.arraycopy(Next.next, 0, next, 0, next.length);}
   }

  class KeyData                                                                 // A key, data pair in a leaf
   {final Key   key = new Key();                                                // A key in a leaf
    final Data data = new Data();                                               // Data associated with the key
    KeyData() {}
    KeyData(int Key, int Data) {intToBits(Key, key.key); intToBits(Data, data.data);}
    public String toString() {return "KeyData("+key+","+data+")";}
    void key (Key  Key)  {key.set(Key);}
    void data(Data Data) {data.set(Data);}
   }

  class KeyNext                                                                 // A key, next pair in a leaf
   {final Key   key = new Key();                                                // A key in a branch
    final Next next = new Next();                                               // Next branch or leaf
    KeyNext() {}
    KeyNext(int Key, int Next) {intToBits(Key, key.key); intToBits(Next, next.next);}
    public String toString() {return "KeyNext("+key+","+next+")";}
    void key (Key  Key)  {key .set(Key);}
    void next(Next Next) {next.set(Next);}
   }

  class LKDIndex                                                                // An index to key, data pair in a leaf
   {int i;                                                                      // Index to a key, data, pair in a leaf
    LKDIndex() {}
    LKDIndex(int I) {i = I;}
    public String toString() {return "LKDIndex("+i+")";}
   }

  class BKNIndex                                                                // An index to key, next pair in a branch
   {int i;                                                                      // Index to a key, next pair in a leaf
    BKNIndex() {}
    BKNIndex(int I) {i = I;}
    public String toString() {return "BKNIndex("+i+")";}
   }

//D1 Find                                                                       // Find the data associated with a key

  class Find                                                                    // Find the data associated with a key in a tree
   {final Key        key;                                                       // Key to find
    final boolean  found;                                                       // Whether the key was in fact found
    final Data      data;                                                       // Data associated with key if found
    final Next        bl;                                                       // Index of leaf located by the key
    final LKDIndex index;                                                       // Index within leaf of key if found

    Find(Key Key)
     {  key = Key;
      found = false;
       data = new Data();
         bl = new Next();
      index = new LKDIndex();

      Next parent = new Next();

     }


//    new Repeat()
//     {void code()
//       {returnIfOne(isLeaf(nodeIndex));                                         // Exit when we reach a leaf
//        final StepDown gte =                                                    // Find child we will step to
//              stepDown(nodeIndex, Key);
//        copy(nodeIndex.v, gte.child.v);
//       }
//     };
//
//    leafFindIndexOf(nodeIndex, Key, Found, leafIndex);                          // Find index of the specified key, data pair in the specified leaf
//    new If(Found)
//     {void Then()
//       {final KeyData kd = new KeyData();                                       // Key, data pair from leaf
//        leafGet(nodeIndex, leafIndex, kd);                                      // Get key, data from stuck
//        copy(Data.v, kd.v.asLayout().get("leafData"));
//       }
//     };
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
   {final StringBuilder s = new StringBuilder();
    for(int i = 0; i < B.length; ++i)
     {final boolean b = B[B.length-1-i];
      s.append(b ? "1" : "o");
     }
    return s.toString();
   }

//D0 Tests                                                                      // Testing

  static void test_bits()
   {final boolean[]b = new boolean[4];
    intToBits(13, b);
    ok(printBits(b), "11o1");
    ok(bitsToInt(b), 13);
   }

  static void test_print_leaf()
   {final Btree t = new Btree(4, 4, 4, 4, 3, 4);
    final Leaf lr = t.new Leaf(0);
    lr.push(1, 11);
    lr.push(2, 22);
    t.ok("""
1,2=0 |
""");
   }

  static void test_print_branch()
   {final Btree  t = new Btree(4, 4, 4, 4, 3, 4);
    final Branch R = t.new Leaf(0).makeBranch();
    final Leaf   l = t.new Leaf();
    final Leaf   r = t.new Leaf();
    R.push(5, l.leaf.asInt());
    R.top(    r.leaf.asInt());
    l.push(1, 11);
    l.push(2, 12);
    l.push(3, 13);
    l.push(4, 14);
    r.push(6, 16);
    r.push(7, 17);
    r.push(8, 18);
    r.push(9, 19);
    //t.stop();
    t.ok("""
          5          |
          0          |
          3          |
          2          |
1,2,3,4=3  6,7,8,9=2 |
""");
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {
   }

  static void newTests()                                                        // Tests being worked on
   {//oldTests();
    test_bits();
    test_print_leaf();
    test_print_branch();
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
