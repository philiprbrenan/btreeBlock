//------------------------------------------------------------------------------
// Btree on the Basic Array Machine for performance comparision with BtreeSF
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
    numberOfNodes;                                                              // The number of noes in the btree

  final static int
   linesToPrintABranch =  4,                                                    // The number of lines required to print a branch
        maxPrintLevels = 10,                                                    // Maximum number of levels to print in a tree
              maxDepth = 99;                                                    // Maximum depth of any realistic tree

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
   }

  LayoutBam layoutBtree()                                                       // Layout the btree
   {final int k = max(maxKeysPerBranch,   maxKeysPerLeaf);
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
        array("free",         numberOfNodes);                                   // Used to place the node  on the free chain else zero if in use
        array("isLeaf",       numberOfNodes);                                   // Whether each node is a leaf or a branch
        array("current_size", numberOfNodes);                                   // Current size of stuck
        array("keys",         numberOfNodes, k);                                // Keys
        array("data",         numberOfNodes, d);                                // Data
       }
     };
   }

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
    stuck_lastElement();
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

//D0 Tests                                                                      // Test stuck

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
 index: 2
   key: 6
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
