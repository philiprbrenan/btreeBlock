//------------------------------------------------------------------------------
// Btree in the pure java paradigm
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Btree in a block on the surface of a silicon chip.

import java.util.*;

class Btree extends Test                                                        // Manipulate a btree
 {final int
    maxKeysPerLeaf,                                                             // Maximum number of leafs in a key
    maxKeysPerBranch,                                                           // Maximum number of keys in a branch
    splitLeafSize,                                                              // The number of key, data pairs to split out of a leaf
    splitBranchSize;                                                            // The number of key, next pairs to split out of a branch

  final Node         root;                                                      // The root of the tree
  final Stack<Node>  nodes = new Stack<>();                                     // The leaves and branches comprising the tree

  final static int
   linesToPrintABranch =  4,                                                    // The number of lines required to print a branch
        maxPrintLevels = 10,                                                    // Maximum number of levels to print in a tree
              maxDepth = 99;                                                    // Maximum depth of any realistic tree

  int        nodeCount = 0;                                                     // Unique number for each node
  int      maxNodeUsed = 0;                                                     // Maximum branch or leaf index used
  static boolean debug = false;                                                 // Debugging enabled

  final static int[]random_small = {27, 442, 545, 317, 511, 578, 391, 993, 858, 586, 472, 906, 658, 704, 882, 246, 261, 501, 354, 903, 854, 279, 526, 686, 987, 403, 401, 989, 650, 576, 436, 560, 806, 554, 422, 298, 425, 912, 503, 611, 135, 447, 344, 338, 39, 804, 976, 186, 234, 106, 667, 494, 690, 480, 288, 151, 773, 769, 260, 809, 438, 237, 516, 29, 376, 72, 946, 103, 961, 55, 358, 232, 229, 90, 155, 657, 681, 43, 907, 564, 377, 615, 612, 157, 922, 272, 490, 679, 830, 839, 437, 826, 577, 937, 884, 13, 96, 273, 1, 188};
  final static int[]random_large = {5918,5624,2514,4291,1791,5109,7993,60,1345,2705,5849,1034,2085,4208,4590,7740,9367,6582,4178,5578,1120,378,7120,8646,5112,4903,1482,8005,3801,5439,4534,9524,6111,204,5459,248,4284,8037,5369,7334,3384,5193,2847,1660,5605,7371,3430,1786,1216,4282,2146,1969,7236,2187,136,2726,9480,5,4515,6082,969,5017,7809,9321,3826,9179,5781,3351,4819,4545,8607,4146,6682,1043,2890,2964,7472,9405,4348,8333,2915,9674,7225,4743,995,1321,3885,6061,9958,3901,4710,4185,4776,5070,8892,8506,6988,2317,9342,3764,9859,4724,5195,673,359,9740,2089,9942,3749,9208,1,7446,7023,5496,4206,3272,3527,8593,809,3149,4173,9605,9021,5120,5265,7121,8667,6911,4717,2535,2743,1289,1494,3788,6380,9366,2732,1501,8543,8013,5612,2393,7041,3350,3204,288,7213,1741,1238,9830,6722,4687,6758,8067,4443,5013,5374,6986,282,6762,192,340,5075,6970,7723,5913,1060,1641,1495,5738,1618,157,6891,173,7535,4952,9166,8950,8680,1974,5466,2383,3387,3392,2188,3140,6806,3131,6237,6249,7952,1114,9017,4285,7193,3191,3763,9087,7284,9170,6116,3717,6695,6538,6165,6449,8960,2897,6814,3283,6600,6151,4624,3992,5860,9557,1884,5585,2966,1061,6414,2431,9543,6654,7417,2617,878,8848,8241,3790,3370,8768,1694,9875,9882,8802,7072,3772,2689,5301,7921,7774,1614,494,2338,8638,4161,4523,5709,4305,17,9626,843,9284,3492,7755,5525,4423,9718,2237,7401,2686,8751,1585,5919,9444,3271,1490,7004,5980,3904,370,5930,6304,7737,93,5941,9079,4968,9266,262,2766,4999,2450,9518,5137,8405,483,8840,2231,700,8049,8823,9811,9378,3811,8074,153,1940,1998,4354,7830,7086,6132,9967,5680,448,1976,4101,7839,3122,4379,9296,4881,1246,4334,9457,5401,1945,9548,8290,1184,3464,132,2458,7704,1056,7554,6203,2270,6070,4889,7369,1676,485,3648,357,1912,9661,4246,1576,1836,4521,7667,6907,2098,8825,7404,4019,8284,3710,7202,7050,9870,3348,3624,9224,6601,7897,6288,3713,932,5596,353,2615,3273,833,1446,8624,2489,3872,486,1091,2493,4157,3611,6570,7107,9153,4543,9504,4746,1342,9737,3247,8984,3640,5698,7814,307,8775,1150,4330,3059,5784,2370,5248,4806,6107,9700,231,3566,5627,3957,5317,5415,8119,2588,9440,2961,9786,4769,466,5411,3080,7623,5031,2378,9286,4801,797,1527,2325,847,6341,5310,1926,9481,2115,2165,5255,5465,5561,3606,7673,7443,7243,8447,2348,7925,6447,8311,6729,4441,7763,8107,267,8135,9194,6775,3883,9639,612,5024,1351,7557,9241,5181,2239,8002,5446,747,166,325,9925,3820,9531,5163,3545,558,7103,7658,5670,8323,4821,6263,7982,59,3700,1082,4474,4353,8637,9558,5191,842,5925,6455,4092,9929,9961,290,3523,6290,7787,8266,7986,7269,6408,3620,406,5964,7289,1620,6726,1257,1993,7006,5545,2913,5093,5066,3019,7081,6760,6779,7061,9051,8852,8118,2340,6596,4594,9708,8430,8659,8920,9268,5431,9203,2823,1427,2203,6422,6193,5214,9566,8791,4964,7575,4350,56,2227,8545,5646,3089,2204,4081,487,8496,2258,4336,6955,3452,556,8602,8251,8569,8636,9430,1025,9459,7137,8392,3553,5945,9414,3078,1688,5480,327,8117,2289,2195,8564,9423,103,7724,3091,8548,7298,5279,6042,2855,3286,3542,9361,420,7020,4112,5320,5366,6379,114,9174,9744,592,5346,3985,3174,5157,9890,1605,3082,8099,4346,7256,8670,5687,6613,6620,1458,1045,7917,2980,2399,1433,3315,4084,178,7056,2132,2728,4421,9195,4181,6017,6229,2945,4627,2809,8816,6737,18,8981,3813,8890,5304,3789,6959,7476,1856,4197,6944,9578,5915,3060,9932,3463,67,7393,9857,5822,3187,501,653,8453,3691,9736,6845,1365,9645,4120,2157,8471,4436,6435,2758,7591,9805,7142,7612,4891,7342,5764,8683,8365,2967,6947,441,2116,6612,1399,7585,972,6548,5481,7733,7209,222,5903,6161,9172,9628,7348,1588,5992,6094,7176,4214,8702,2987,74,8486,9788,7164,5788,8535,8422,6826,1800,8965,4965,565,5609,4686,2556,9324,5000,9809,1994,4737,63,8992,4783,2536,4462,8868,6346,5553,3980,2670,1601,4272,8725,4698,7333,7826,9233,4198,1997,1687,4851,62,7893,8149,8015,341,2230,1280,5559,9756,3761,7834,6805,9287,4622,5748,2320,1958,9129,9649,1644,4323,5096,9490,7529,6444,7478,7044,9525,7713,234,7553,9099,9885,7135,6493,9793,6268,8363,2267,9157,9451,1438,9292,1637,3739,695,1090,4731,4549,5171,5975,7347,5192,5243,1084,2216,9860,3318,5594,5790,1107,220,9397,3378,1353,4498,6497,5442,7929,7377,9541,9871,9895,6742,9146,9409,292,6278,50,5288,2217,4923,6790,4730,9240,3006,3547,9347,7863,4275,3287,2673,7485,1915,9837,2931,3918,635,9131,1197,6250,3853,4303,790,5548,9993,3702,2446,3862,9652,4432,973,41,3507,8585,2444,1633,956,5789,1523,8657,4869,8580,8474,7093,7812,2549,7363,9315,6731,1130,7645,7018,7852,362,1636,2905,8006,4040,6643,8052,7021,3665,8383,715,1876,2783,3065,604,4566,8761,7911,1983,3836,5547,8495,8144,1950,2537,8575,640,8730,8303,1454,8165,6647,4762,909,9449,8640,9253,7293,8767,3004,4623,6862,8994,2520,1215,6299,8414,2576,6148,1510,313,3693,9843,8757,5774,8871,8061,8832,5573,5275,9452,1248,228,9749,2730};

//D1 Construction                                                               // Create a BTree from nodes which can be branches or leaves.  The data associated with the BTree is stored only in the leaves opposite the keys

  Btree(int MaxKeysPerLeaf, int MaxKeysPerBranch)                               // Define a BTree with the specified dimensions
   {zz();
    maxKeysPerLeaf   = MaxKeysPerLeaf;
    maxKeysPerBranch = MaxKeysPerBranch;
    splitLeafSize    = maxKeysPerLeaf   >> 1;
    splitBranchSize  = maxKeysPerBranch >> 1;
    nodes.push(root  = new Node());                                             // The root
    root.isLeaf      = true;                                                    // The root starts as a leaf
   }

//D1 Control                                                                    // Testing, control and integrity

  void ok(String expected) {Test.ok(toString(), expected);}                     // Confirm tree is as expected
  void stop()              {Test.stop(toString());}                             // Stop after printing the tree
  public String toString() {return print();}                                    // Print the tree

//D1 Components                                                                 // A branch or leaf in the tree

  class Node                                                                    // A branch or leaf in the tree
   {boolean isLeaf;                                                             // A leaf if true
    final  int node = nodeCount++;

    final Stack<KeyData> keyData = new Stack<>();                               // Key, data pairs when a leaf
    final Stack<KeyNext> keyNext = new Stack<>();                               // Key, next pairs when a branch. The last entry is top and so this stack must always have at least one element in it when the node is acting as a branch

    void assertLeaf()   {if (!isLeaf) stop("Leaf required");}
    void assertBranch() {if ( isLeaf) stop("Branch required");}

    Node allocLeaf()    {z();
    final Node n = new Node(); nodes.push(n); n.isLeaf = true;  return n;}
    Node allocBranch()  {z();
    final Node n = new Node(); nodes.push(n); n.isLeaf = false; return n;}

    int leafSize()   {z(); return keyData.size();}                              // Number of children in body of leaf
    int branchSize() {z(); return keyNext.size() - 1;}                          // Number of children in body of branch

    boolean isFull()                                                            // The node is full
     {z(); return isLeaf ? leafSize() == maxKeysPerLeaf : branchSize() == maxKeysPerBranch;
     }

    boolean isLow()                                                             // The node is low on children making it impossible to merge two sibling children
     {z(); return (isLeaf ? leafSize() : branchSize()) < 2;
     }

    boolean hasLeavesForChildren()                                              // The node has leaves for children
     {z(); assertBranch();
      final KeyNext kn = keyNext.lastElement();
      return nodes.elementAt(kn.next).isLeaf;
     }

    int top()                                                                   // The top next element of a branch
     {z(); assertBranch();
      return keyNext.elementAt(branchSize()).next;
     }

    public String toString()                                                    // Print a node
     {final StringBuilder s = new StringBuilder();
      if (isLeaf)                                                               // Print a leaf
       {s.append("Leaf(node:"+node+" size:"+leafSize()+")\n");

        for (int i = 0; i < keyData.size(); i++)
         {final KeyData kd = keyData.elementAt(i);
          s.append("  "+(i+1)+" key:"+kd.key+" data:"+kd.data+"\n");
         }
       }
      else                                                                      // Print a branch
       {s.append("Branch(node:"+node+
                 " size:"+branchSize()+
                 " top:"+top()+"\n");

        final int N = keyNext.size() - 1;
        for (int i = 0; i < N; i++)
         {final KeyNext kn = keyNext.elementAt(i);
          s.append(String.format("  %2d key:%2d next:%2d\n", i+1, kn.key, kn.next));
         }
        final KeyNext kn = keyNext.elementAt(N);
        s.append("             Top:"+kn.next+")\n");
       }
      return s.toString();
     }

//D2 Search                                                                     // Search within a node

    class FindEqualInLeaf                                                       // Find the first key in the leaf that is equal to the search key
     {final Node     leaf;                                                      // The leaf being searched
      final int    search;                                                      // Search key
      final boolean found;                                                      // Whether the key was found
      final int      data;                                                      // Data associated with the  key
      final int     index;                                                      // Index of first such key if found

      FindEqualInLeaf(int Search)                                               // Find the first key in the leaf that is equal to the search key
       {z(); assertLeaf();
        leaf = Node.this;
        search = Search;
        boolean looking = true;
        final int N = leafSize();
        int i;
        for (i = 0; i < N && looking; i++)                                      // Compare with each valid key
         {z();
         if (keyData.elementAt(i).key == search)
           {z(); looking = false; break;
           }
         }
        if (looking)                                                            // Not found
         {z(); index = N; found = false; data = 0;                              // Unnecessary magic number only to overcome complaints from Java
         }
        else                                                                    // Found
         {z(); index = i; found = true; data = keyData.elementAt(i).data;
         }
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

    class FindFirstGreaterThanOrEqualInLeaf                                     // Find the first key in the leaf that is equal to or greater than the search key
     {final Node    leaf;                                                       // The leaf being searched
      final int    search;                                                      // Search key
      final boolean found;                                                      // Whether the key was found
      final int     first;                                                      // Index of first such key if found

      FindFirstGreaterThanOrEqualInLeaf(int Search)                             // Find the first key in the  leaf that is equal to or greater than the search key
       {z(); assertLeaf();
        leaf   = Node.this;
        search = Search;
        boolean looking = true;
        final int N = leafSize();
        int i;
        for (i = 0; i < N && looking; i++)                                      // Search the key, data pairs int the leaf
         {z();
         if (keyData.elementAt(i).key >= search)
           {z(); looking = false; break;
           }
         }
        if (looking)                                                            // Key not found
         {z(); found = false; first = N;
         }
        else                                                                    // Key found
         {z(); found = true; first = i;
         }
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

    class FindFirstGreaterThanOrEqualInBranch                                   // Find the first key in the branch that is equal to or greater than the search key
     {final Node     branch;                                                    // The branch being searched
      final int     search;                                                     // Search key
      final boolean  found;                                                     // Whether the key was found
      final int      first;                                                     // Index of first such key if found
      final int       next;                                                     // The corresponding next field or top if no such key was found

      FindFirstGreaterThanOrEqualInBranch(int Search)                           // Find the first key in the branch that is equal to or greater than the search key
       {z(); assertBranch();
        branch = Node.this;
        search = Search;
        boolean looking = true;
        final int N = branchSize();
        int i;
        for (i = 0; i < N && looking; i++)                                      // Check each key
         {z();
          if (keyNext.elementAt(i).key >= search)
           {z(); looking = false; break;
           }
         }
        if (looking)                                                            // Key not found
         {z(); found = false; first = N; next = keyNext.elementAt(N).next;
         }
        else                                                                    // Key has been found
         {z(); found = true;  first = i; next = keyNext.elementAt(i).next;
         }
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

//D2 Array                                                                      // Represent the contents of the tree as an array

    void leafToArray(Stack<KeyData>s)                                           // Leaf as an array
     {z(); assertLeaf();
      final int K = leafSize();
      for  (int i = 0; i < K; i++) {z(); s.push(keyData.elementAt(i));}
     }

    void branchToArray(Stack<KeyData>s)                                         // Branch to array
     {z(); assertBranch();
      final int K = branchSize() + 1;                                           // To allow for top next

      if (K > 0)                                                                // Branch has key, next pairs
       {z();
        for  (int i = 0; i < K; i++)
         {z();
          final int next = keyNext.elementAt(i).next;                           // Each key, next pair
          if (nodes.elementAt(next).isLeaf)
           {z(); nodes.elementAt(next).leafToArray(s);
           }
          else
           {z();
            if (next == 0)
             {say("Cannot descend through root from index", i,
                  "in branch", node);
              break;
             }
            z(); nodes.elementAt(next).branchToArray(s);
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
      for  (int i = 0; i < K; i++) s.append(""+keyData.elementAt(i).key+",");
      if (s.length() > 0) s.setLength(s.length()-1);                            // Remove trailing comma if present
      s.append("="+node+" ");
      S.elementAt(level*linesToPrintABranch).append(s.toString());
      padStrings(S, level);
     }

    void printBranch(Stack<StringBuilder>S, int level)                          // Print branch horizontally
     { assertBranch();
      if (level > maxPrintLevels) return;
      padStrings(S, level);
      final int K = branchSize();
      final int L = level * linesToPrintABranch;

      if (K > 0)                                                                // Branch has key, next pairs
       {for  (int i = 0; i < K; i++)
         {final int next = keyNext.elementAt(i).next;                           // Each key, next pair
          if (nodes.elementAt(next).isLeaf)
           {nodes.elementAt(next).printLeaf(S, level+1);
           }
          else
           {if (next == 0)
             {say("Cannot descend through root from index", i,
                  "in branch", node);
              break;
             }
            nodes.elementAt(next).printBranch(S, level+1);
           }

          S.elementAt(L+0).append(""+keyNext.elementAt(i).key);                 // Key
          S.elementAt(L+1).append(""+node+(i > 0 ?  "."+i : ""));               // Branch,key, next pair
          S.elementAt(L+2).append(""+keyNext.elementAt(i).next);
         }
       }
      else                                                                      // Branch is empty so print just the index of the branch
       {S.elementAt(L+0).append(""+node+"Empty");
       }
      final int top = top();                                                    // Top next will always be present
      S.elementAt(L+3).append(top);                                             // Append top next
      if (nodes.elementAt(top).isLeaf)                                          // Print leaf
       { nodes.elementAt(top).printLeaf(S, level+1);
       }
      else                                                                      // Print branch
       {if (top == 0)
         {say("Cannot descend through root from top in branch", node);
          return;
         }
        nodes.elementAt(top).printBranch(S, level+1);
       }

      padStrings(S, level);
     }

//D2 Split                                                                      // Split nodes in half to increase the number of nodes in the tree

    void splitLeafRoot()                                                        // Split a leaf which happens to be a full root into two half full leaves while transforming the root leaf into a branch
     {z(); assertLeaf();
      if (node != 0) stop("Wanted root, but got node:", node);
      if (!isFull()) stop("Root is not full, but has size:", leafSize());
      keyNext.clear();
      isLeaf = false;

      final Node l  = allocLeaf();
      final Node r  = allocLeaf();
      KeyData first = null;
      for (int i = 0; i < splitLeafSize; i++)                                   // Build left leaf
       {z();
        first        = keyData.firstElement();
        l.keyData.push(keyData.firstElement());
                       keyData.removeElementAt(0);
       }

      KeyData last = keyData.firstElement();
      final int kv = (last.key + first.key) / 2;                                // Insert left leaf into root
      keyNext.push(new KeyNext(kv, l.node));                                    // Insert left leaf into root

      for (int i = 0; i < splitLeafSize; i++)                                   // Build right leaf
       {z();
        r.keyData.push(keyData.firstElement());
        keyData.removeElementAt(0);
       }
      keyNext.push(new KeyNext(r.node));                                        // Insert right into root
      keyData.clear();                                                          // A leaf no more
     }

    void splitBranchRoot()                                                      // Split a branch which happens to be a full root into two half full branches while retaining the current branch as the root
     {z(); assertBranch();
      if (node != 0) stop("Not root, but node:", node);
      if (!isFull()) stop("Root is not full, but has size:", branchSize());

      final Node l = allocBranch();
      final Node r = allocBranch();

      for (int i = 0; i < splitBranchSize; i++)                                 // Left
       {z();
        l.keyNext.push(keyNext. firstElement  ());
                       keyNext.removeElementAt(0);
       }
      l.keyNext.push(new KeyNext(keyNext.firstElement().next));

      for(int i = 0; i < splitBranchSize; i++)                                  // Right
       {z();
        r.keyNext.push(keyNext.      elementAt(1));
                       keyNext.removeElementAt(1);
       }
      r.keyNext.push(new KeyNext(top()));

      final KeyNext pl = keyNext.firstElement();
      keyData.clear(); keyNext.clear();

      keyNext.push(new KeyNext(pl.key, l.node));
      keyNext.push(new KeyNext(        r.node));
     }

    void splitLeaf(Node parent, int index)                                      // Split a leaf which is not the root
     {z(); assertLeaf();
      if (node == 0)          stop("Cannot split root with this method");
      if (!isFull())          stop("Leaf:", node, "is not full, but has:", leafSize(), this);
      if (parent.isFull())    stop("Parent:", parent, "must not be full");
      if (index < 0)          stop("Index", index, "too small");
      if (index > leafSize()) stop("Index", index, "too big for leaf with:", leafSize());

      final Node p = parent;
      final Node l = allocLeaf();
      final Node r = this;

      for (int i = 0; i < splitLeafSize; i++)                                   // Build left leaf
       {z();
        l.keyData.push(r.keyData.firstElement());
                       r.keyData.removeElementAt(0);
       }
      final int F = r.keyData.firstElement().key;
      final int L = l.keyData.lastElement().key;
      final int splitKey = (F + L) / 2;

      parent.keyNext.insertElementAt(new KeyNext(splitKey, l.node), index);
     }

    void splitBranch(Node parent, int index)                                    // Split a branch which is not the root by splitting right to left
     {z(); assertBranch();
      if (node == 0)            stop("Cannot split root with this method");
      if (!isFull())            stop("Branch:", node, "is not full, but", branchSize());
      if (parent.isFull())      stop("Parent:", parent.node, "must not be full");
      if (index < 0)            stop("Index", index, "too small in node:", node);
      if (index > branchSize()) stop("Index", index, "too big for branch with:", branchSize(), "in node:", node);

      final Node p = parent;
      final Node l = allocBranch();
      final Node r = this;

      for (int i = 0; i < splitBranchSize; i++)                                 // Build left branch
       {z();
        l.keyNext.push(r.keyNext.firstElement());
                       r.keyNext.removeElementAt(0);
       }

      final KeyNext split = r.keyNext.firstElement();                           // Build right branch
      r.keyNext.removeElementAt(0);
      l     .keyNext.push(new KeyNext(split.next));
      parent.keyNext.insertElementAt(new KeyNext(split.key, l.node), index);
     }

//D2 Steal                                                                      // Steal children from left or sibling to balance tree

    boolean stealFromLeft(int index)                                            // Steal from the left sibling of the indicated child if possible to give to the right - Dennis Moore, Dennis Moore, Dennis Moore.
     {z(); assertBranch();
      if (index == 0) return false;
      if (index < 0)            stop("Index", index, "too small");
      if (index > branchSize()) stop("Index", index, "too big");

      final KeyNext          L = keyNext.elementAt(index-1);
      final KeyNext          R = keyNext.elementAt(index+0);
      final Stack<KeyNext>   p = keyNext;

      if (hasLeavesForChildren())                                               // Children are leaves
       {z();
        final Stack<KeyData> l = nodes.elementAt(L.next).keyData;
        final Stack<KeyData> r = nodes.elementAt(R.next).keyData;
        final int           nl = nodes.elementAt(L.next).leafSize();
        final int           nr = nodes.elementAt(R.next).leafSize();

        if (nr >= maxKeysPerLeaf) return false;                                 // Steal not possible because there is no where to put the steal
        if (nl <= 1) return false;                                              // Steal not allowed because it would leave the leaf sibling empty

        final KeyData       ll = l.lastElement();
        r.insertElementAt(new KeyData(ll.key, ll.data), 0);                     // Increase right
        l.pop();                                                                // Reduce left
        p.setElementAt   (new KeyNext(l.elementAt(nl-2).key, L.next), index-1); // Swap key of parent
       }
      else                                                                      // Children are branches
       {z();
        final Stack<KeyNext> l = nodes.elementAt(L.next).keyNext;
        final Stack<KeyNext> r = nodes.elementAt(R.next).keyNext;
        final int           nl = nodes.elementAt(L.next).branchSize();
        final int           nr = nodes.elementAt(R.next).branchSize();

        if (nr >= maxKeysPerBranch) return false;                               // Steal not possible because there is no where to put the steal
        if (nl <= 1) return false;                                              // Steal not allowed because it would leave the left sibling empty

        final KeyNext       t = l.lastElement();                                // Increase right with left top
        r.insertElementAt(new KeyNext(p.elementAt(index).key, t.next), 0);      // Increase right with left top
        l.pop();                                                                // Remove left top
        final KeyNext       b = r.firstElement();                               // Increase right with left top
        r.setElementAt(new KeyNext(p.elementAt(index-1).key, b.next), 0);       // Reduce key of parent of left
        p.setElementAt(new KeyNext(l.lastElement().key, L.next), index-1);      // Reduce key of parent of left
       }
      return true;
     }

    boolean stealFromRight(int index)                                           // Steal from the right sibling of the indicated child if possible
     {z(); assertBranch();
      if (index == branchSize()) return false;
      if (index < 0)             stop("Index", index, "too small");
      if (index >= branchSize()) stop("Index", index, "too big");

      final KeyNext          L = keyNext.elementAt(index+0);
      final KeyNext          R = keyNext.elementAt(index+1);
      final Stack<KeyNext>   p = keyNext;
      if (hasLeavesForChildren())                                               // Children are leaves
       {z();
        final Stack<KeyData> l = nodes.elementAt(L.next).keyData;
        final Stack<KeyData> r = nodes.elementAt(R.next).keyData;
        final int           nl = nodes.elementAt(L.next).leafSize();
        final int           nr = nodes.elementAt(R.next).leafSize();

        if (nl >= maxKeysPerLeaf) return false;                                 // Steal not possible because there is no where to put the steal
        if (nr <= 1) return false;                                              // Steal not allowed because it would leave the right sibling empty

        final int k = r.firstElement().key;
        l.push        (new KeyData(k, r.firstElement().data));                  // Increase left
        p.setElementAt(new KeyNext(k, L.next), index);                          // Swap key of parent
        r.removeElementAt(0);                                                   // Reduce right
       }
      else                                                                      // Children are branches
       {z();
        final Stack<KeyNext> l = nodes.elementAt(L.next).keyNext;
        final Stack<KeyNext> r = nodes.elementAt(R.next).keyNext;
        final int           nl = nodes.elementAt(L.next).branchSize();
        final int           nr = nodes.elementAt(R.next).branchSize();

        if (nl >= maxKeysPerBranch) return false;                               // Steal not possible because there is no where to put the steal
        if (nr <= 1) return false;                                              // Steal not allowed because it would leave the right sibling empty

        l.setElementAt(new KeyNext(L.key, l.lastElement().next), nl);           // Left top becomes real
        l.push        (new KeyNext(r.firstElement().next));                     // Increase left with right first
        p.setElementAt(new KeyNext(r.firstElement().key, L.next), index);       // Swap key of parent
        r.removeElementAt(0);                                                   // Reduce right
       }
      return true;
     }

//D2 Merge                                                                      // Merge  two nodes together and free the resulting free node

    boolean mergeRoot()                                                         // Merge into the root
     {z();
      if (root.isLeaf || branchSize() > 1) return false;
      if (node != 0) stop("Expected root, got:", node);
      final Node p = this;
      final Node l = nodes.elementAt(keyNext.firstElement().next);
      final Node r = nodes.elementAt(keyNext. lastElement().next);

      if (p.hasLeavesForChildren())                                             // Leaves
       {z();
        if (l.leafSize() + r.leafSize() <= maxKeysPerLeaf)
         {z(); p.keyData.clear();
          final int nl = l.leafSize();
          for (int i = 0; i < nl; ++i)
           {z();
            p.keyData.push(l.keyData.elementAt(0));
                           l.keyData.removeElementAt(0);
           }
          final int nr = r.leafSize();
          for (int i = 0; i < nr; ++i)
           {z();
            p.keyData.push(r.keyData.elementAt(0));
                           r.keyData.removeElementAt(0);
           }
          p.keyNext.clear();
          isLeaf = true;
          return true;
         }
       }
      else if (l.branchSize() + 1 + r.branchSize() <= maxKeysPerBranch)         // Branches
       {z();
        final KeyNext pkn = p.keyNext.firstElement();
        p.keyNext.clear();
        final int nl = l.branchSize();
        for (int i = 0; i < nl; ++i)
         {z();
          p.keyNext.push(l.keyNext.elementAt(0));
                         l.keyNext.removeElementAt(0);

         }
        p.keyNext.push(new KeyNext(pkn.key, l.keyNext.lastElement().next));
        final int nr = r.branchSize();
        for (int i = 0; i < nr; ++i)
         {z();
          p.keyNext.push(r.keyNext.elementAt(0));
                         r.keyNext.removeElementAt(0);
         }
        p.keyNext.push(new KeyNext(r.keyNext.lastElement().next));
        return true;
       }
      return false;
     }

    boolean mergeLeftSibling(int index)                                         // Merge the left sibling
     {z(); assertBranch();
      if (index == 0) return false;
      if (index < 0)            stop("Index", index, "too small");
      if (index > branchSize()) stop("Index", index, "too big");
      if (branchSize() < 2)     stop("Node:", this,  "must have two or more children");

      final KeyNext          L = keyNext.elementAt(index-1);
      final KeyNext          R = keyNext.elementAt(index-0);
      final Stack<KeyNext>   p = keyNext;

      if (hasLeavesForChildren())                                               // Children are leaves
       {z();
        final Stack<KeyData> l = nodes.elementAt(L.next).keyData;
        final Stack<KeyData> r = nodes.elementAt(R.next).keyData;
        final int           nl = nodes.elementAt(L.next).leafSize();
        final int           nr = nodes.elementAt(R.next).leafSize();

        if (nl + nr >= maxKeysPerLeaf) return false;                            // Combined body would be too big

        for (int i = 0; i < maxKeysPerLeaf && l.size() > 0; i++)                // Transfer left to right
         {z(); r.insertElementAt(l.pop(), 0);
         }
       }
      else                                                                      // Children are branches
       {z();
        final Stack<KeyNext> l = nodes.elementAt(L.next).keyNext;
        final Stack<KeyNext> r = nodes.elementAt(R.next).keyNext;
        final int           nl = nodes.elementAt(L.next).branchSize();
        final int           nr = nodes.elementAt(R.next).branchSize();

        if (nl + 1 + nr > maxKeysPerBranch) return false;                       // Merge not possible because there is not enough room for the combined result
        final int t = p.elementAt(index-1).key;                                 // Top key
        r.insertElementAt(new KeyNext(t, l.lastElement().next), 0);             // Left top to right

        l.pop();                                                                // Remove left top
        for (int i = 0; i < maxKeysPerBranch && l.size() > 0; i++)              // Transfer left to right
         {z(); r.insertElementAt(l.pop(), 0);
         }
       }
        p.removeElementAt(index-1);                                             // Reduce parent on left
      return true;
     }

    boolean mergeRightSibling(int index)                                        // Merge the right sibling
     {z(); assertBranch();
      if (index > branchSize()) return false;
      if (index < 0)            stop("Index", index, "too small");
      if (index > branchSize()) stop("Index", index, "too big");
      //if (branchSize() < 2)     stop("Node:", this,  "must have two or more children");

      final KeyNext          L = keyNext.elementAt(index+0);
      final KeyNext          R = keyNext.elementAt(index+1);
      final Stack<KeyNext>   p = keyNext;

      if (hasLeavesForChildren())                                               // Children are leaves
       {z();
        final Stack<KeyData> l = nodes.elementAt(L.next).keyData;
        final Stack<KeyData> r = nodes.elementAt(R.next).keyData;
        final int           nl = nodes.elementAt(L.next).leafSize();
        final int           nr = nodes.elementAt(R.next).leafSize();

        if (nl + nr > maxKeysPerLeaf) return false;                             // Combined body would be too big
        for (int i = 0; i < maxKeysPerLeaf && r.size() > 0; i++)                // Transfer right to left
         {z(); l.push(r.pop());
         }
       }
      else                                                                      // Children are branches
       {z();
        final Stack<KeyNext> l = nodes.elementAt(L.next).keyNext;
        final Stack<KeyNext> r = nodes.elementAt(R.next).keyNext;
        final int           nl = nodes.elementAt(L.next).branchSize();
        final int           nr = nodes.elementAt(R.next).branchSize();

        if (nl + 1 + nr > maxKeysPerBranch) return false;                       // Merge not possible because there is no where to put the steal
        final KeyNext ll = l.lastElement();
        l.setElementAt(new KeyNext(p.elementAt(index).key, ll.next), nl);       // Re key left top

        for (int i = 0; i < maxKeysPerBranch && r.size() > 0; i++)              // Transfer right to left
         {z();
          l.push(r.firstElement());
          r.removeElementAt(0);
         }
       }

      final KeyNext pkn = p.elementAt(index+1);                                 // Key of right sibling
      p.setElementAt(new KeyNext(pkn.key, p.elementAt(index).next), index);     // Install key of right sibling in this child
      p.removeElementAt(index+1);                                               // Reduce parent on right
      return true;
     }

//D2 Balance                                                                    // Balance the tree by merging and stealing

    void balance(int index)                                                     // Augment the indexed child so it has at least two children in its body
     {z(); assertBranch();
      if (index < 0)            stop("Index", index, "too small");
      if (index > branchSize()) stop("Index", index, "too big");
      if (isLow() && node != root.node) stop("Parent:", node, "must not be low on children");

      final KeyNext p = nodes.elementAt(node).keyNext.elementAt(index);
      final Node    c = nodes.elementAt(p.next);
      if (!c.isLow())               return;
      if (stealFromLeft    (index)) return;
      if (stealFromRight   (index)) return;
      if (mergeLeftSibling (index)) return;
      if (mergeRightSibling(index)) return;
      stop("Unable to balance child:", c.node);
     }
   }  // Node

  class KeyData                                                                 // A key, data pair
   {final int key, data;
    KeyData(int Key, int Data) {z(); key = Key; data = Data;}
    public String toString()
     {return"KeyData(Key:"+key+" data:"+data+")\n";
     }
   }

  class KeyNext                                                                 // A key, next pair
   {final int key, next;
    KeyNext(int Key, int Next) {z(); key = Key; next = Next;}
    KeyNext(         int Next) {z(); key = -1;  next = Next;}
    public String toString()
     {return"KeyNext(Key:"+key+" next:"+next+")\n";
     }
   }

//D1 Array                                                                      // Key, data pairs in the tree as an array

  Stack<KeyData> toArray()                                                      // Key, data pairs in the tree as an array
   {z();
    final Stack<KeyData> s = new Stack<>();

    if (root.isLeaf) {z(); root.  leafToArray(s);}
    else             {z(); root.branchToArray(s);}
    return s;
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

    if (root.isLeaf)
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

    Find(int Key)                                                               // Find the data associated with a key in the tree
     {z();
      if (root.isLeaf)                                                          // The root is a leaf
       {z();
        search = root.new FindEqualInLeaf(Key);
        return;
       }

      Node parent = root;                                                       // Parent starts at root which is known to be a branch

      for (int i = 0; i < maxDepth; i++)                                        // Step down through tree
       {z();
        final Node.FindFirstGreaterThanOrEqualInBranch down =                   // Find next child in search path of key
          parent.new FindFirstGreaterThanOrEqualInBranch(Key);
        final int n = down.next;
        if (nodes.elementAt(n).isLeaf)                                          // Found the containing search
         {z();
          final Node l = nodes.elementAt(n);
          search  = l.new FindEqualInLeaf(Key);
          return;
         }
        parent = nodes.elementAt(n);                                            // Step down to lower branch
       }
      stop("Search did not terminate in a leaf");
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

  Find find(int Key) {z(); return new Find(Key);}                               // Find a key in the tree

  class FindAndInsert extends Find                                              // Insert the specified key and data into the tree if there is room in the target leaf,or update the key with the data if the key already exists
   {int          key;                                                           // Key to insert
    int         data;                                                           // Data being inserted or updated
    boolean  success;                                                           // Inserted or updated if true
    boolean inserted;                                                           // Inserted if true

    FindAndInsert(int Key, int Data)                                            // Find the leaf that should contain this key and insert or update it is possible
     {super(Key);                                                               // Find the leaf that should contain this key
      z();
      key = Key; data = Data;

      if (found())                                                              // Found the key in the leaf so update it with the new data
       {z();
        leaf().keyData.setElementAt(new KeyData(Key, Data), index());
        success = true; inserted = false;
        return;
       }

      if (!leaf().isFull())                                                     // Leaf is not full so we can insert immediately
       {z();
        final Node.FindFirstGreaterThanOrEqualInLeaf f =
          leaf().new FindFirstGreaterThanOrEqualInLeaf(Key);
//        if (f.found)
         {z();
          leaf().keyData.insertElementAt(new KeyData(Key, Data), f.first);
         }
//        else                                                                    // No matching key so put at end
//         {z();
//          leaf().keyData.push(new KeyData(Key, Data));
//         }
        success = true;
        return;
       }
      success = false;
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

  FindAndInsert findAndInsert(int Key, int Data)                                // Find a key and insert it if possible with its associated data
   {z();
    return new FindAndInsert(Key, Data);
   }

//D1 Insertion                                                                  // Insert a key, data pair into the tree or update and existing key with a new datum

  void put(int Key, int Data)                                                   // Insert a key, data pair into the tree or update and existing key with a new datum
   {z();
    final FindAndInsert f = new FindAndInsert(Key, Data);                       // Try direct insertion with no modifications to the shape of the tree
    if (f.success) return;                                                      // Inserted or updated successfully

    if (root.isFull())                                                          // Start the insertion at the root, after splitting it if necessary
     {z();
     if (root.isLeaf)
       {z();
        root.splitLeafRoot();
       }
      else
       {z();
        root.splitBranchRoot();
       }
      final FindAndInsert F = new FindAndInsert(Key, Data);                     // Splitting the root might have been enough
      if (F.success) return;                                                    // Inserted or updated successfully
     }

    Node p = root;

    for (int i = 0; i < maxDepth; i++)                                          // Step down from branch to branch through the tree until reaching a leaf repacking as we go
     {z();
      final Node.FindFirstGreaterThanOrEqualInBranch                            // Step down
      down = p.new FindFirstGreaterThanOrEqualInBranch(Key);
      final Node q = nodes.elementAt(down.next);
      if (q.isLeaf)                                                             // Reached a leaf
       {z();
        q.splitLeaf(p, down.first);
        findAndInsert(Key, Data);
        return;
       }

      if (q.isFull())
       {z();
        q.splitBranch(p, down.first);                                           // Split the child branch in the search path for the key from the parent so the the search path does not contain a full branch above the containing leaf
        final Node.FindFirstGreaterThanOrEqualInBranch                          // Step down again as the split will have altered the local layout
        Down = p.new FindFirstGreaterThanOrEqualInBranch(Key);
        p = nodes.elementAt(Down.next);
       }
      else
       {z();
        p = q;
       }
     }
    stop("Fallen off the end of the tree");                                     // The tree must be missing a leaf
   }

  void put(int Key)                                                             // Put some test data into the tree
   {z();
    put(Key, Key);
   }

//D1 Deletion                                                                   // Delete a key, data pair from the tree

  Integer findAndDelete(int Key)                                                // Delete a key from the tree and returns its data if present without modifying the shape of tree
   {z();
    final Find f = new Find(Key);                                               // Try direct insertion with no modifications to the shape of the tree
    if (!f.found()) return null;                                                // Inserted or updated successfully
    final Node     l = f.leaf();                                                // The leaf that contains the key
    final int      i = f.index();                                               // Position in the leaf of the key
    final KeyData kd = l.keyData.elementAt(i);                                  // Key, data pairs in the leaf
    l.keyData.removeElementAt(i);                                               // Remove the key, data pair from the leaf
    //merge(Key);
    return kd.data;
   }

  Integer delete(int Key)                                                       // Insert a key, data pair into the tree or update and existing key with a new datum
   {z(); root.mergeRoot();

    if (root.isLeaf)                                                            // Find and delete directly in root as a leaf
     {z(); return findAndDelete(Key);
     }

    Node p = root;                                                              // Start at root

    for (int i = 0; i < maxDepth; i++)                                          // Step down from branch to branch through the tree until reaching a leaf repacking as we go
     {z();
      final Node.FindFirstGreaterThanOrEqualInBranch                            // Step down
      down = p.new FindFirstGreaterThanOrEqualInBranch(Key);

      p.balance(down.first);
      final Node q = nodes.elementAt(down.next);

      if (q.isLeaf)                                                             // Reached a leaf
       {z();
        return findAndDelete(Key);
       }
      p = q;
     }
    stop("Fallen off the end of the tree");                                     // The tree must be missing a leaf
    return null;
   }

//D0 Tests                                                                      // Testing

  static void test_put_ascending()
   {final Btree t = new Btree(2, 3);
    final int N = 64;
    for (int i = 1; i <= N; i++) t.put(i);
    //stop(t);
    t.ok("""
                                                                                                              16                                                                                                                              32                                                                                                                                                                                                                                                                                       |
                                                                                                              0                                                                                                                               0.1                                                                                                                                                                                                                                                                                      |
                                                                                                              65                                                                                                                              97                                                                                                                                                                                                                                                                                       |
                                                                                                                                                                                                                                              66                                                                                                                                                                                                                                                                                       |
                                               8                                                                                                                              24                                                                                                                               40                                                                48                                                                                                                                                    |
                                               65                                                                                                                             97                                                                                                                               66                                                                66.1                                                                                                                                                  |
                                               31                                                                                                                             62                                                                                                                               94                                                                110                                                                                                                                                   |
                                               47                                                                                                                             79                                                                                                                                                                                                 32                                                                                                                                                    |
                    4                                                         12                                                              20                                                              28                                                               36                                                              44                                                                     52                                      56                                                                       |
                    31                                                        47                                                              62                                                              79                                                               94                                                              110                                                                    32                                      32.1                                                                     |
                    14                                                        29                                                              45                                                              60                                                               77                                                              92                                                                     108                                     116                                                                      |
                    22                                                        38                                                              53                                                              70                                                               85                                                              101                                                                                                            15                                                                       |
         2                       6                            10                              14                              18                              22                              26                              30                               34                              38                              42                               46                                 50                                     54                                       58                  60                              |
         14                      22                           29                              38                              45                              53                              60                              70                               77                              85                              92                               101                                108                                    116                                      15                  15.1                            |
         5                       12                           20                              27                              36                              43                              51                              58                               68                              75                              83                               90                                 99                                     106                                      114                 118                             |
         9                       17                           24                              33                              40                              48                              55                              63                               72                              80                              87                               95                                 103                                    111                                                          6                               |
    1          3          5             7             9               11              13              15              17              19              21              23              25              27              29              31               33              35              37              39              41              43               45               47                49               51                  53                  55                   57                  59                   61        62         |
    5          9          12            17            20              24              27              33              36              40              43              48              51              55              58              63               68              72              75              80              83              87               90               95                99               103                 106                 111                  114                 118                  6         6.1        |
    1          4          8             11            16              19              23              26              30              35              39              42              46              50              54              57               61              67              71              74              78              82               86               89                93               98                  102                 105                  109                 113                  117       119        |
    3          7          10            13            18              21              25              28              34              37              41              44              49              52              56              59               64              69              73              76              81              84               88               91                96               100                 104                 107                  112                 115                            2          |
1=1  2=3   3=4  4=7   5=8   6=10   7=11   8=13   9=16   10=18   11=19   12=21   13=23   14=25   15=26   16=28   17=30   18=34   19=35   20=37   21=39   22=41   23=42   24=44   25=46   26=49   27=50   28=52   29=54   30=56   31=57   32=59    33=61   34=64   35=67   36=69   37=71   38=73   39=74   40=76   41=78   42=81   43=82   44=84    45=86   46=88    47=89   48=91     49=93   50=96    51=98    52=100    53=102    54=104    55=105    56=107     57=109    58=112    59=113    60=115     61=117    62=119    63,64=2 |
""");
   }

  static void test_put_ascending_wide()
   {final Btree t = new Btree(8, 7);
    final int N = 64;
    for (int i = 0; i < N; ++i) t.put(i);
    //stop(t);
    t.ok("""
                                                     15                                                                   31                                                                                                                                           |
                                                     0                                                                    0.1                                                                                                                                          |
                                                     9                                                                    15                                                                                                                                           |
                                                                                                                          10                                                                                                                                           |
          3          7              11                               19              23                27                                   35               39                 43                 47                 51                 55                            |
          9          9.1            9.2                              15              15.1              15.2                                 10               10.1               10.2               10.3               10.4               10.5                          |
          1          3              4                                6               7                 8                                    12               13                 14                 16                 17                 18                            |
                                    5                                                                  11                                                                                                                                2                             |
0,1,2,3=1  4,5,6,7=3    8,9,10,11=4    12,13,14,15=5   16,17,18,19=6   20,21,22,23=7     24,25,26,27=8     28,29,30,31=11    32,33,34,35=12   36,37,38,39=13     40,41,42,43=14     44,45,46,47=16     48,49,50,51=17     52,53,54,55=18     56,57,58,59,60,61,62,63=2 |
""");
   }

  static void test_put_descending()
   {final Btree t = new Btree(2, 3);
    final int N = 64;
    for (int i = N; i > 0; --i) t.put(i);
    //stop(t);
    t.ok("""
                                                                                                                                                                                                                                                                                32                                                                                                                              48                                                                                                                         |
                                                                                                                                                                                                                                                                                0                                                                                                                               0.1                                                                                                                        |
                                                                                                                                                                                                                                                                                97                                                                                                                              65                                                                                                                         |
                                                                                                                                                                                                                                                                                                                                                                                                                66                                                                                                                         |
                                                                                                                                             16                                                               24                                                                                                                                40                                                                                                                               56                                                        |
                                                                                                                                             97                                                               97.1                                                                                                                              65                                                                                                                               66                                                        |
                                                                                                                                             110                                                              94                                                                                                                                62                                                                                                                               31                                                        |
                                                                                                                                                                                                              79                                                                                                                                47                                                                                                                               32                                                        |
                                                                  8                                      12                                                                   20                                                                28                                                              36                                                              44                                                               52                                                            60                          |
                                                                  110                                    110.1                                                                94                                                                79                                                              62                                                              47                                                               31                                                            32                          |
                                                                  116                                    108                                                                  92                                                                77                                                              60                                                              45                                                               29                                                            14                          |
                                                                                                         101                                                                  85                                                                70                                                              53                                                              38                                                               22                                                            15                          |
                            4                 6                                      10                                     14                                18                              22                                26                              30                              34                              38                              42                              46                               50                              54                              58                          62            |
                            116               116.1                                  108                                    101                               92                              85                                77                              70                              60                              53                              45                              38                               29                              22                              14                          15            |
                            118               114                                    106                                    99                                90                              83                                75                              68                              58                              51                              43                              36                               27                              20                              12                          5             |
                                              111                                    103                                    95                                87                              80                                72                              63                              55                              48                              40                              33                               24                              17                              9                           6             |
        2        3                   5                   7                 9                   11                   13               15               17              19              21              23                25              27              29              31              33              35              37              39              41              43              45              47               49              51              53              55              57             59            61            63     |
        118      118.1               114                 111               106                 103                  99               95               90              87              83              80                75              72              68              63              58              55              51              48              43              40              36              33               27              24              20              17              12             9             5             6      |
        119      117                 113                 109               105                 102                  98               93               89              86              82              78                74              71              67              61              57              54              50              46              42              39              35              30               26              23              19              16              11             8             4             1      |
                 115                 112                 107               104                 100                  96               91               88              84              81              76                73              69              64              59              56              52              49              44              41              37              34              28               25              21              18              13              10             7             3             2      |
1,2=119    3=117      4=115    5=113    6=112      7=109    8=107    9=105    10=104    11=102    12=100      13=98   14=96    15=93   16=91    17=89   18=88   19=86   20=84   21=82   22=81   23=78   24=76     25=74   26=73   27=71   28=69   29=67   30=64   31=61   32=59   33=57   34=56   35=54   36=52   37=50   38=49   39=46   40=44   41=42   42=41   43=39   44=37   45=35   46=34   47=30   48=28    49=26   50=25   51=23   52=21   53=19   54=18   55=16   56=13   57=11   58=10   59=8   60=7   61=4   62=3   63=1   64=2 |
""");
   }

  static void test_put_small_random()
   {final Btree t = new Btree(2, 3);
    for (int i = 0; i < random_small.length; ++i) t.put(random_small[i]);
    //stop(t);
    t.ok("""
                                                                                                                                                                                                                                                   335                                                                                                                                                                                                                    561                                                                                                                                                                                                                                                                                                                                                           |
                                                                                                                                                                                                                                                   0                                                                                                                                                                                                                      0.1                                                                                                                                                                                                                                                                                                                                                           |
                                                                                                                                                                                                                                                   73                                                                                                                                                                                                                     47                                                                                                                                                                                                                                                                                                                                                            |
                                                                                                                                                                                                                                                                                                                                                                                                                                                                          48                                                                                                                                                                                                                                                                                                                                                            |
                                                         81                                                                160                                                                                                                                                                                       416                                                                         493                                                                                                                                                                                               681                                                                                                                    882                                                                                                           |
                                                         73                                                                73.1                                                                                                                                                                                      47                                                                          47.1                                                                                                                                                                                              48                                                                                                                     48.1                                                                                                          |
                                                         98                                                                71                                                                                                                                                                                        33                                                                          90                                                                                                                                                                                                60                                                                                                                     44                                                                                                            |
                                                                                                                           54                                                                                                                                                                                                                                                                    16                                                                                                                                                                                                                                                                                                                       17                                                                                                            |
                                         47                                             120                                                                                      253                                      281                                                                379                                                            439                                                                                    528                                                       582                                622                                                                                      785                 807                                     856                                                              925                                           988                 |
                                         98                                             71                                                                                       54                                       54.1                                                               33                                                             90                                                                                     16                                                        60                                 60.1                                                                                     44                  44.1                                    44.2                                                             17                                            17.1                |
                                         96                                             84                                                                                       34                                       100                                                                19                                                             86                                                                                     49                                                        82                                 45                                                                                       14                  92                                      61                                                               41                                            66                  |
                                         68                                             55                                                                                                                                52                                                                 31                                                             5                                                                                      11                                                                                           57                                                                                                                                                   26                                                                                                             6                   |
         20       28        33                   55                 96       104                  143       153                           210           233       234                          270            275                        307                     349           365                     397                         429           437                      457       476                         502       506            518                 552                    570           577                  598           613                   654           662        674                          695       738                     805                  819           830                       870                      904           909                          953       961        981                   990      |
         96       96.1      96.2                 68                 84       84.1                 55        55.1                          34            34.1      34.2                         100            100.1                      52                      19            19.1                    31                          86            86.1                     5         5.1                         49        49.1           49.2                11                     82            82.1                 45            45.1                  57            57.1       57.2                         14        14.1                    92                   61            61.1                      26                       41            41.1                         66        66.1       66.2                  6        |
         102      97        64                   69                 99       75                   76        85                            72            74        1                            59             101                        53                      38            70                      25                          36            91                       28        87                          50        23             63                  30                     94            80                   81            83                    46            77         88                           51        58                      62                   93            40                        21                       37            79                           95        67         65                    27       |
                            78                   39                          56                             43                                                    22                                          20                         35                                    7                       10                                        32                                 3                                                    4                   8                                    13                                 15                                             24                                     9                       89                                 29                        18                                     12                                                42                    2        |
1,13=102    27=97     29=64     39,43=78   55=69   72=39   90,96=99   103=75     106=56    135=76    151=85     155,157=43     186,188=72    229,232=74     234=1     237,246=22    260,261=59    272,273=101      279=20     288,298=53    317=35    338,344=38    354,358=70     376,377=7    391=25    401,403=10    422,425=36    436,437=91     438=32    442,447=28    472=87    480,490=3     494,501=50    503=23     511,516=63     526=4    545=30    554,560=8    564=94    576,577=80     578=13    586=81    611,612=83     615=15     650=46    657,658=77     667=88     679,681=24    686,690=51    704=58     769,773=9    804=62    806=89     809=93    826,830=40     839,854=29     858=21    882=18     884,903=37    906,907=79     912,922=12    937,946=95    961=67     976=65     987=42     989=27    993=2 |
""");
   }

  static void test_put_large_random()
   {if (!github_actions) return;
    final Btree t = new Btree(2, 3);
    final TreeMap<Integer,Integer> s = new TreeMap<>();
    for (int i = 0; i < random_large.length; ++i)
     {final int r = random_large[i];
      s.put(r, i);
      t.put(r, i);
     }
    final int a = s.firstKey(), b = s.lastKey();
    for (int i = a-1; i < b + 1; ++i)
     {if (s.containsKey(i))
       {Find f = t.find(i);
        ok(f.found());
        ok(f.data(), s.get(i));
       }
      else
       {Find f = t.find(i);
        ok(!f.found());
       }
     }
   }

  static void test_find()
   {final Btree t = new Btree(2, 3);
    final int N = 32;
    for (int i = 1; i <= N; i++) t.put(2*i);                                    // Insert
    //stop(t);
    t.ok("""
                                                   17                                                              33                                                                                                                              |
                                                   0                                                               0.1                                                                                                                             |
                                                   31                                                              47                                                                                                                              |
                                                                                                                   32                                                                                                                              |
                    9                                                              25                                                               41                              49                                                             |
                    31                                                             47                                                               32                              32.1                                                           |
                    14                                                             29                                                               45                              53                                                             |
                    22                                                             38                                                                                               15                                                             |
         5                         13                              21                              29                               37                              45                                53              57                           |
         14                        22                              29                              38                               45                              53                                15              15.1                         |
         5                         12                              20                              27                               36                              43                                51              55                           |
         9                         17                              24                              33                               40                              48                                                6                            |
    3          7           11              15              19              23              27              31               35              39              43              47                51              55                59      61         |
    5          9           12              17              20              24              27              33               36              40              43              48                51              55                6       6.1        |
    1          4           8               11              16              19              23              26               30              35              39              42                46              50                54      56         |
    3          7           10              13              18              21              25              28               34              37              41              44                49              52                        2          |
2=1  4=3   6=4  8=7   10=8   12=10   14=11   16=13   18=16   20=18   22=19   24=21   26=23   28=25   30=26   32=28    34=30   36=34   38=35   40=37   42=39   44=41   46=42   48=44     50=46   52=49   54=50   56=52     58=54   60=56    62,64=2 |
""");
    for (int i = 0; i <= 2*N+1; i++)                                            // Update
     {Find f = t.find(i);
      if (i > 0 && i % 2 == 0)
       {ok(f.found(), true);
        ok(f.data(),  i);
        t.put(i, i-1);
       }
      else ok(f.found(), false);
     }
    for (int i = 0; i <= 2*N+1; i++)
     {Find f = t.find(i);
      if (i > 0 && i % 2 == 0)
       {ok(f.found(), true);
        ok(f.data(),  i-1);
       }
      else ok(f.found(), false);
     }
   }

  static void test_steal()
   {final Btree t = new Btree(4, 3);
    final int N = 12;
    for (int i = 1; i <= N; i++) t.put(i);
    //t.stop();
    t.ok("""
             4                             |
             0                             |
             5                             |
             6                             |
      2             6      8               |
      5             6      6.1             |
      1             4      7               |
      3                    2               |
1,2=1  3,4=3  5,6=4  7,8=7    9,10,11,12=2 |
""");
    t.nodes.elementAt(5).stealFromRight(0);
    //t.stop();
    t.ok("""
             4                             |
             0                             |
             5                             |
             6                             |
        3           6      8               |
        5           6      6.1             |
        1           4      7               |
        3                  2               |
1,2,3=1  4=3  5,6=4  7,8=7    9,10,11,12=2 |
""");
    t.nodes.elementAt(5).stealFromLeft(1);
    //t.stop();
t.ok("""
             4                             |
             0                             |
             5                             |
             6                             |
      2             6      8               |
      5             6      6.1             |
      1             4      7               |
      3                    2               |
1,2=1  3,4=3  5,6=4  7,8=7    9,10,11,12=2 |
""");
    t.nodes.elementAt(0).stealFromRight(0);
    //t.stop();
t.ok("""
                      6                    |
                      0                    |
                      5                    |
                      6                    |
      2      4               8             |
      5      5.1             6             |
      1      3               7             |
             4               2             |
1,2=1  3,4=3    5,6=4  7,8=7  9,10,11,12=2 |
""");
    t.nodes.elementAt(0).stealFromLeft(1);
    //t.stop();
t.ok("""
             4                             |
             0                             |
             5                             |
             6                             |
      2             6      8               |
      5             6      6.1             |
      1             4      7               |
      3                    2               |
1,2=1  3,4=3  5,6=4  7,8=7    9,10,11,12=2 |
""");
   }

  static void test_merge_left()
   {final Btree t = new Btree(4, 3);
    final int N = 32;
    for (int i = 1; i <= N; i++) t.put(i);
    t.findAndDelete(25);
    t.findAndDelete(28);
    //t.stop();
    t.ok("""
                            8                                         16                                                                              |
                            0                                         0.1                                                                             |
                            14                                        22                                                                              |
                                                                      15                                                                              |
             4                                  12                                           20                    24                                 |
             14                                 22                                           15                    15.1                               |
             5                                  12                                           20                    24                                 |
             9                                  17                                                                 6                                  |
      2              6               10                    14                     18                    22                   26      28               |
      5              9               12                    17                     20                    24                   6       6.1              |
      1              4               8                     11                     16                    19                   23      25               |
      3              7               10                    13                     18                    21                           2                |
1,2=1  3,4=3   5,6=4  7,8=7   9,10=8   11,12=10   13,14=11   15,16=13    17,18=16   19,20=18   21,22=19   23,24=21     26=23   27=25    29,30,31,32=2 |
""");
    t.nodes.elementAt(6).mergeLeftSibling(1);
    //t.stop();
    t.ok("""
                            8                                         16                                                                        |
                            0                                         0.1                                                                       |
                            14                                        22                                                                        |
                                                                      15                                                                        |
             4                                  12                                           20                    24                           |
             14                                 22                                           15                    15.1                         |
             5                                  12                                           20                    24                           |
             9                                  17                                                                 6                            |
      2              6               10                    14                     18                    22                      28              |
      5              9               12                    17                     20                    24                      6               |
      1              4               8                     11                     16                    19                      25              |
      3              7               10                    13                     18                    21                      2               |
1,2=1  3,4=3   5,6=4  7,8=7   9,10=8   11,12=10   13,14=11   15,16=13    17,18=16   19,20=18   21,22=19   23,24=21     26,27=25   29,30,31,32=2 |
""");
    t.nodes.elementAt(15).mergeLeftSibling(1);
    //t.stop();
t.ok("""
                            8                                         16                                                                          |
                            0                                         0.1                                                                         |
                            14                                        22                                                                          |
                                                                      15                                                                          |
             4                                  12                                                                     24                         |
             14                                 22                                                                     15                         |
             5                                  12                                                                     24                         |
             9                                  17                                                                     6                          |
      2              6               10                    14                     18         20           22                      28              |
      5              9               12                    17                     24         24.1         24.2                    6               |
      1              4               8                     11                     16         18           19                      25              |
      3              7               10                    13                                             21                      2               |
1,2=1  3,4=3   5,6=4  7,8=7   9,10=8   11,12=10   13,14=11   15,16=13    17,18=16   19,20=18     21,22=19     23,24=21   26,27=25   29,30,31,32=2 |
""");
   }

  static void test_merge_right()
   {final Btree t = new Btree(4, 3);
    final int N = 32;
    for (int i = 1; i <= N; i++) t.put(i);
    t.findAndDelete(25);
    t.findAndDelete(28);
    //t.stop();
    t.ok("""
                            8                                         16                                                                              |
                            0                                         0.1                                                                             |
                            14                                        22                                                                              |
                                                                      15                                                                              |
             4                                  12                                           20                    24                                 |
             14                                 22                                           15                    15.1                               |
             5                                  12                                           20                    24                                 |
             9                                  17                                                                 6                                  |
      2              6               10                    14                     18                    22                   26      28               |
      5              9               12                    17                     20                    24                   6       6.1              |
      1              4               8                     11                     16                    19                   23      25               |
      3              7               10                    13                     18                    21                           2                |
1,2=1  3,4=3   5,6=4  7,8=7   9,10=8   11,12=10   13,14=11   15,16=13    17,18=16   19,20=18   21,22=19   23,24=21     26=23   27=25    29,30,31,32=2 |
""");
    t.nodes.elementAt(6).mergeRightSibling(0);
    //t.stop();
    t.ok("""
                            8                                         16                                                                        |
                            0                                         0.1                                                                       |
                            14                                        22                                                                        |
                                                                      15                                                                        |
             4                                  12                                           20                    24                           |
             14                                 22                                           15                    15.1                         |
             5                                  12                                           20                    24                           |
             9                                  17                                                                 6                            |
      2              6               10                    14                     18                    22                      28              |
      5              9               12                    17                     20                    24                      6               |
      1              4               8                     11                     16                    19                      23              |
      3              7               10                    13                     18                    21                      2               |
1,2=1  3,4=3   5,6=4  7,8=7   9,10=8   11,12=10   13,14=11   15,16=13    17,18=16   19,20=18   21,22=19   23,24=21     26,27=23   29,30,31,32=2 |
""");
    t.nodes.elementAt(15).mergeRightSibling(0);
    //t.stop();
    t.ok("""
                            8                                         16                                                                          |
                            0                                         0.1                                                                         |
                            14                                        22                                                                          |
                                                                      15                                                                          |
             4                                  12                                                                     24                         |
             14                                 22                                                                     15                         |
             5                                  12                                                                     20                         |
             9                                  17                                                                     6                          |
      2              6               10                    14                     18         20           22                      28              |
      5              9               12                    17                     20         20.1         20.2                    6               |
      1              4               8                     11                     16         18           19                      23              |
      3              7               10                    13                                             21                      2               |
1,2=1  3,4=3   5,6=4  7,8=7   9,10=8   11,12=10   13,14=11   15,16=13    17,18=16   19,20=18     21,22=19     23,24=21   26,27=23   29,30,31,32=2 |
""");
   }

  static void test_delete()
   {final Btree t = new Btree(4, 3);
    final int N = 32;
    for (int i = 1; i <= N; i++) t.put(i);
    //t.stop();
    t.ok("""
                            8                                         16                                                                                    |
                            0                                         0.1                                                                                   |
                            14                                        22                                                                                    |
                                                                      15                                                                                    |
             4                                  12                                           20                    24                                       |
             14                                 22                                           15                    15.1                                     |
             5                                  12                                           20                    24                                       |
             9                                  17                                                                 6                                        |
      2              6               10                    14                     18                    22                      26         28               |
      5              9               12                    17                     20                    24                      6          6.1              |
      1              4               8                     11                     16                    19                      23         25               |
      3              7               10                    13                     18                    21                                 2                |
1,2=1  3,4=3   5,6=4  7,8=7   9,10=8   11,12=10   13,14=11   15,16=13    17,18=16   19,20=18   21,22=19   23,24=21     25,26=23   27,28=25    29,30,31,32=2 |
""");

    for (int i = 1; i <= N; i++)
     {t.delete(i);
      //say("        case", i, "-> t.ok(\"\"\"", t, "\"\"\");");
      switch(i)
       {case 1 -> t.ok("""
                                                                         16                                                                                   |
                                                                         0                                                                                    |
                                                                         14                                                                                   |
                                                                         15                                                                                   |
                             8                   12                                            20                    24                                       |
                             14                  14.1                                          15                    15.1                                     |
                             5                   12                                            20                    24                                       |
                                                 17                                                                  6                                        |
    2      4        6                 10                      14                    18                    22                      26         28               |
    5      5.1      5.2               12                      17                    20                    24                      6          6.1              |
    1      3        4                 8                       11                    16                    19                      23         25               |
                    7                 10                      13                    18                    21                                 2                |
2=1  3,4=3    5,6=4    7,8=7   9,10=8   11,12=10     13,14=11   15,16=13   17,18=16   19,20=18   21,22=19   23,24=21     25,26=23   27,28=25    29,30,31,32=2 |
""");
        case 2 -> t.ok("""
                                                                       16                                                                                   |
                                                                       0                                                                                    |
                                                                       14                                                                                   |
                                                                       15                                                                                   |
                           8                   12                                            20                    24                                       |
                           14                  14.1                                          15                    15.1                                     |
                           5                   12                                            20                    24                                       |
                                               17                                                                  6                                        |
    3    4        6                 10                      14                    18                    22                      26         28               |
    5    5.1      5.2               12                      17                    20                    24                      6          6.1              |
    1    3        4                 8                       11                    16                    19                      23         25               |
                  7                 10                      13                    18                    21                                 2                |
3=1  4=3    5,6=4    7,8=7   9,10=8   11,12=10     13,14=11   15,16=13   17,18=16   19,20=18   21,22=19   23,24=21     25,26=23   27,28=25    29,30,31,32=2 |
""");
        case 3 -> t.ok("""
                                                                16                                                                                   |
                                                                0                                                                                    |
                                                                14                                                                                   |
                                                                15                                                                                   |
                    8                   12                                            20                    24                                       |
                    14                  14.1                                          15                    15.1                                     |
                    5                   12                                            20                    24                                       |
                                        17                                                                  6                                        |
    4      6                 10                      14                    18                    22                      26         28               |
    5      5.1               12                      17                    20                    24                      6          6.1              |
    1      4                 8                       11                    16                    19                      23         25               |
           7                 10                      13                    18                    21                                 2                |
4=1  5,6=4    7,8=7   9,10=8   11,12=10     13,14=11   15,16=13   17,18=16   19,20=18   21,22=19   23,24=21     25,26=23   27,28=25    29,30,31,32=2 |
""");
        case 4 -> t.ok("""
                                                              16                                                                                   |
                                                              0                                                                                    |
                                                              14                                                                                   |
                                                              15                                                                                   |
                  8                   12                                            20                    24                                       |
                  14                  14.1                                          15                    15.1                                     |
                  5                   12                                            20                    24                                       |
                                      17                                                                  6                                        |
    5    6                 10                      14                    18                    22                      26         28               |
    5    5.1               12                      17                    20                    24                      6          6.1              |
    1    4                 8                       11                    16                    19                      23         25               |
         7                 10                      13                    18                    21                                 2                |
5=1  6=4    7,8=7   9,10=8   11,12=10     13,14=11   15,16=13   17,18=16   19,20=18   21,22=19   23,24=21     25,26=23   27,28=25    29,30,31,32=2 |
""");
        case 5 -> t.ok("""
                                                       16                                                                                   |
                                                       0                                                                                    |
                                                       14                                                                                   |
                                                       15                                                                                   |
           8                   12                                            20                    24                                       |
           14                  14.1                                          15                    15.1                                     |
           5                   12                                            20                    24                                       |
                               17                                                                  6                                        |
    6               10                      14                    18                    22                      26         28               |
    5               12                      17                    20                    24                      6          6.1              |
    1               8                       11                    16                    19                      23         25               |
    7               10                      13                    18                    21                                 2                |
6=1  7,8=7   9,10=8   11,12=10     13,14=11   15,16=13   17,18=16   19,20=18   21,22=19   23,24=21     25,26=23   27,28=25    29,30,31,32=2 |
""");
        case 6 -> t.ok("""
                                                     16                                                                                   |
                                                     0                                                                                    |
                                                     14                                                                                   |
                                                     15                                                                                   |
                               12                                          20                    24                                       |
                               14                                          15                    15.1                                     |
                               5                                           20                    24                                       |
                               17                                                                6                                        |
    7    8         10                     14                    18                    22                      26         28               |
    5    5.1       5.2                    17                    20                    24                      6          6.1              |
    1    7         8                      11                    16                    19                      23         25               |
                   10                     13                    18                    21                                 2                |
7=1  8=7    9,10=8    11,12=10   13,14=11   15,16=13   17,18=16   19,20=18   21,22=19   23,24=21     25,26=23   27,28=25    29,30,31,32=2 |
""");
        case 7 -> t.ok("""
                                                                      20                                                           |
                                                                      0                                                            |
                                                                      14                                                           |
                                                                      15                                                           |
                        12                    16                                            24                                     |
                        14                    14.1                                          15                                     |
                        5                     17                                            24                                     |
                                              20                                            6                                      |
    8       10                     14                      18                    22                    26         28               |
    5       5.1                    17                      20                    24                    6          6.1              |
    1       8                      11                      16                    19                    23         25               |
            10                     13                      18                    21                               2                |
8=1  9,10=8    11,12=10   13,14=11   15,16=13     17,18=16   19,20=18   21,22=19   23,24=21   25,26=23   27,28=25    29,30,31,32=2 |
""");
        case 8 -> t.ok("""
                                                                    20                                                           |
                                                                    0                                                            |
                                                                    14                                                           |
                                                                    15                                                           |
                      12                    16                                            24                                     |
                      14                    14.1                                          15                                     |
                      5                     17                                            24                                     |
                                            20                                            6                                      |
    9     10                     14                      18                    22                    26         28               |
    5     5.1                    17                      20                    24                    6          6.1              |
    1     8                      11                      16                    19                    23         25               |
          10                     13                      18                    21                               2                |
9=1  10=8    11,12=10   13,14=11   15,16=13     17,18=16   19,20=18   21,22=19   23,24=21   25,26=23   27,28=25    29,30,31,32=2 |
""");
        case 9 -> t.ok("""
                                                              20                                                           |
                                                              0                                                            |
                                                              14                                                           |
                                                              15                                                           |
                12                    16                                            24                                     |
                14                    14.1                                          15                                     |
                5                     17                                            24                                     |
                                      20                                            6                                      |
     10                    14                      18                    22                    26         28               |
     5                     17                      20                    24                    6          6.1              |
     1                     11                      16                    19                    23         25               |
     10                    13                      18                    21                               2                |
10=1   11,12=10   13,14=11   15,16=13     17,18=16   19,20=18   21,22=19   23,24=21   25,26=23   27,28=25    29,30,31,32=2 |
""");
        case 10 -> t.ok("""
                                                           20                                                           |
                                                           0                                                            |
                                                           14                                                           |
                                                           15                                                           |
                                     16                                          24                                     |
                                     14                                          15                                     |
                                     5                                           24                                     |
                                     20                                          6                                      |
     11      12          14                     18                    22                    26         28               |
     5       5.1         5.2                    20                    24                    6          6.1              |
     1       10          11                     16                    19                    23         25               |
                         13                     18                    21                               2                |
11=1   12=10    13,14=11    15,16=13   17,18=16   19,20=18   21,22=19   23,24=21   25,26=23   27,28=25    29,30,31,32=2 |
""");
        case 11 -> t.ok("""
                            16                    20                     24                                      |
                            0                     0.1                    0.2                                     |
                            5                     20                     24                                      |
                                                                         6                                       |
     12         14                     18                     22                     26         28               |
     5          5.1                    20                     24                     6          6.1              |
     1          11                     16                     19                     23         25               |
                13                     18                     21                                2                |
12=1   13,14=11    15,16=13   17,18=16   19,20=18    21,22=19   23,24=21    25,26=23   27,28=25    29,30,31,32=2 |
""");
        case 12 -> t.ok("""
                         16                    20                     24                                      |
                         0                     0.1                    0.2                                     |
                         5                     20                     24                                      |
                                                                      6                                       |
     13      14                     18                     22                     26         28               |
     5       5.1                    20                     24                     6          6.1              |
     1       11                     16                     19                     23         25               |
             13                     18                     21                                2                |
13=1   14=11    15,16=13   17,18=16   19,20=18    21,22=19   23,24=21    25,26=23   27,28=25    29,30,31,32=2 |
""");
        case 13 -> t.ok("""
                16                    20                     24                                      |
                0                     0.1                    0.2                                     |
                5                     20                     24                                      |
                                                             6                                       |
     14                    18                     22                     26         28               |
     5                     20                     24                     6          6.1              |
     1                     16                     19                     23         25               |
     13                    18                     21                                2                |
14=1   15,16=13   17,18=16   19,20=18    21,22=19   23,24=21    25,26=23   27,28=25    29,30,31,32=2 |
""");
        case 14 -> t.ok("""
                                     20                    24                                      |
                                     0                     0.1                                     |
                                     5                     24                                      |
                                                           6                                       |
     15      16          18                     22                     26         28               |
     5       5.1         5.2                    24                     6          6.1              |
     1       13          16                     19                     23         25               |
                         18                     21                                2                |
15=1   16=13    17,18=16    19,20=18   21,22=19   23,24=21    25,26=23   27,28=25    29,30,31,32=2 |
""");
        case 15 -> t.ok("""
                            20                    24                                      |
                            0                     0.1                                     |
                            5                     24                                      |
                                                  6                                       |
     16         18                     22                     26         28               |
     5          5.1                    24                     6          6.1              |
     1          16                     19                     23         25               |
                18                     21                                2                |
16=1   17,18=16    19,20=18   21,22=19   23,24=21    25,26=23   27,28=25    29,30,31,32=2 |
""");
        case 16 -> t.ok("""
                         20                    24                                      |
                         0                     0.1                                     |
                         5                     24                                      |
                                               6                                       |
     17      18                     22                     26         28               |
     5       5.1                    24                     6          6.1              |
     1       16                     19                     23         25               |
             18                     21                                2                |
17=1   18=16    19,20=18   21,22=19   23,24=21    25,26=23   27,28=25    29,30,31,32=2 |
""");
        case 17 -> t.ok("""
                20                    24                                      |
                0                     0.1                                     |
                5                     24                                      |
                                      6                                       |
     18                    22                     26         28               |
     5                     24                     6          6.1              |
     1                     19                     23         25               |
     18                    21                                2                |
18=1   19,20=18   21,22=19   23,24=21    25,26=23   27,28=25    29,30,31,32=2 |
""");
        case 18 -> t.ok("""
                                     24                                     |
                                     0                                      |
                                     5                                      |
                                     6                                      |
     19      20          22                     26         28               |
     5       5.1         5.2                    6          6.1              |
     1       18          19                     23         25               |
                         21                                2                |
19=1   20=18    21,22=19    23,24=21   25,26=23   27,28=25    29,30,31,32=2 |
""");
        case 19 -> t.ok("""
                            24                                     |
                            0                                      |
                            5                                      |
                            6                                      |
     20         22                     26         28               |
     5          5.1                    6          6.1              |
     1          19                     23         25               |
                21                                2                |
20=1   21,22=19    23,24=21   25,26=23   27,28=25    29,30,31,32=2 |
""");
        case 20 -> t.ok("""
                         24                                     |
                         0                                      |
                         5                                      |
                         6                                      |
     21      22                     26         28               |
     5       5.1                    6          6.1              |
     1       19                     23         25               |
             21                                2                |
21=1   22=19    23,24=21   25,26=23   27,28=25    29,30,31,32=2 |
""");
        case 21 -> t.ok("""
                24                                     |
                0                                      |
                5                                      |
                6                                      |
     22                    26         28               |
     5                     6          6.1              |
     1                     23         25               |
     21                               2                |
22=1   23,24=21   25,26=23   27,28=25    29,30,31,32=2 |
""");
        case 22 -> t.ok("""
                         26                         |
                         0                          |
                         5                          |
                         6                          |
     23      24                     28              |
     5       5.1                    6               |
     1       21                     25              |
             23                     2               |
23=1   24=21    25,26=23   27,28=25   29,30,31,32=2 |
""");
        case 23 -> t.ok("""
                26                         |
                0                          |
                5                          |
                6                          |
     24                    28              |
     5                     6               |
     1                     25              |
     23                    2               |
24=1   25,26=23   27,28=25   29,30,31,32=2 |
""");
        case 24 -> t.ok("""
     25      26          28               |
     0       0.1         0.2              |
     1       23          25               |
                         2                |
25=1   26=23    27,28=25    29,30,31,32=2 |
""");
        case 25 -> t.ok("""
     26         28               |
     0          0.1              |
     1          25               |
                2                |
26=1   27,28=25    29,30,31,32=2 |
""");
        case 26 -> t.ok("""
     27      28               |
     0       0.1              |
     1       25               |
             2                |
27=1   28=25    29,30,31,32=2 |
""");
        case 27 -> t.ok("""
     28              |
     0               |
     1               |
     2               |
28=1   29,30,31,32=2 |
""");
        case 28 -> t.ok("""
     29           |
     0            |
     1            |
     2            |
29=1   30,31,32=2 |
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

  static void test_to_array()
   {final Btree t = new Btree(2, 3);

    final int M = 2;
    for (int i = 1; i <= M; i++) t.put(i);
    ok(""+t.toArray(), """
[KeyData(Key:1 data:1)
, KeyData(Key:2 data:2)
]""");

    final int N = 16;
    for (int i = M; i <= N; i++) t.put(i);
    ok(""+t.toArray(), """
[KeyData(Key:1 data:1)
, KeyData(Key:2 data:2)
, KeyData(Key:3 data:3)
, KeyData(Key:4 data:4)
, KeyData(Key:5 data:5)
, KeyData(Key:6 data:6)
, KeyData(Key:7 data:7)
, KeyData(Key:8 data:8)
, KeyData(Key:9 data:9)
, KeyData(Key:10 data:10)
, KeyData(Key:11 data:11)
, KeyData(Key:12 data:12)
, KeyData(Key:13 data:13)
, KeyData(Key:14 data:14)
, KeyData(Key:15 data:15)
, KeyData(Key:16 data:16)
]""");
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_put_ascending();
    test_put_ascending_wide();
    test_put_descending();
    test_put_small_random();
    test_put_large_random();
    test_find();
    test_steal();
    test_merge_left();
    test_merge_right();
    test_delete();
    test_to_array();
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
