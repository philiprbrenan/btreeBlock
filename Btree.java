//------------------------------------------------------------------------------
// BTree in a block
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout a binary tree in a block on a silicon chip.

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
    for (int i = 1; i < TreeSize; i++) freeList.push(new Next(i));              // Initially all of the leaves or branches are on the free list, except for the root at index 0
   }

// Branch or Leaf                                                               // A branch or leaf in the tree

  class BranchOrLeaf                                                            // A branch or leaf in a btree
   {enum State {leaf, branch, free};                                            // The current state of the branch or leaf: as a leaf, as a branch or free waiting for use
    State            state;                                                     // Whether this branch or leaf is a branch, a leaf or free
    LKDIndex      leafSize;                                                     // Number of key, data pairs currently contained in leafif a leaf
    BKNIndex    branchSize;                                                     // Number of key, next pairs currently contained in branch if a branch
    final KeyData[]keyData = new KeyData[maxKeysPerLeaf];                       // Key, data pairs for when a leaf
    final KeyNext[]keyNext = new KeyNext[maxKeysPerBranch];                     // Key, next pairs for when a branch
    final Next         top = new Next();                                        // Top next reference for when a branch
   }

  class Leaf                                                                    // Describe a leaf
   {final Next leaf;                                                            // Index of the leaf
    Leaf()
     {assert freeList.size() > 1;                                               // The root is always zero
      leaf = freeList.pop();
      nodes[leaf.asInt()].state = BranchOrLeaf.State.leaf;
     }
    BranchOrLeaf.State state() {return nodes[leaf.asInt()].state;     }
    LKDIndex        leafSize() {return nodes[leaf.asInt()].leafSize;  }
    final KeyData[]  keyData() {return nodes[leaf.asInt()].keyData;   }

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
   }

  class Branch                                                                  // Describe a branch
   {final Next branch;                                                          // Index of the branch
    Branch()
     {assert freeList.size() > 1;                                               // The root is always zero
      branch = freeList.pop();
      nodes[branch.asInt()].state = BranchOrLeaf.State.branch;
     }
    BranchOrLeaf.State state()   {return nodes[branch.asInt()].state;     }
    BKNIndex       branchSize()  {return nodes[branch.asInt()].branchSize;}
    final KeyNext[]   keyNext()  {return nodes[branch.asInt()].keyNext;   }
    final Next            top()  {return nodes[branch.asInt()].top;       }

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

   }

  class Key                                                                     // A key in a leaf or a branch
   {boolean[] key = new boolean[bitsPerKey];                                    // A key is composed of bits
    Key() {}
    Key(int n)  {intToBits(n, key);}
    int asInt() {return bitsToInt(key);}
    boolean equals            (Key Key) {return asInt() == Key.asInt();}
    boolean greaterThanOrEqual(Key Key) {return asInt()  > Key.asInt();}
    public String toString() {return ""+bitsToInt(key);}
   }

  class Data                                                                    // A data item associated with a key in a leaf
   {boolean[] data = new boolean[bitsPerData];                                  // Data is composed of bits
    Data() {}
    Data(int n) {intToBits(n, data);}
    public String toString() {return ""+bitsToInt(data);}
   }

  class Next                                                                    // A next reference from a branch to a leaf or a branch
   {boolean[] next = new boolean[bitsPerNext];                                  // Enough bits to reference a leaf or brahch in the block
    Next() {}
    Next(int n) {intToBits(n, next);}
    int asInt() {return bitsToInt(next);}
    public String toString() {return ""+bitsToInt(next);}
   }

  class KeyData                                                                 // A key, data pair in a leaf
   {Key  key;                                                                   // A key in a leaf
    Data data;                                                                  // Data associated with the key
    KeyData() {}
    KeyData(int Key, int Data) {intToBits(Key, key.key); intToBits(Data, data.data);}
    public String toString() {return "KeyData("+key+","+data+")";}
   }

  class KeyNext                                                                 // A key, next pair in a leaf
   {Key  key;                                                                   // A key in a branch
    Next next;                                                                  // Next branch or leaf
    KeyNext() {}
    KeyNext(int Key, int Next) {intToBits(Key, key.key); intToBits(Next, next.next);}
    public String toString() {return "KeyNext("+key+","+next+")";}
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

  static void oldTests()                                                        // Tests thought to be in good shape
   {
   }

  static void newTests()                                                        // Tests being worked on
   {//oldTests();
    test_bits();
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
