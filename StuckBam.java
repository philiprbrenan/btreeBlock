//------------------------------------------------------------------------------
// Layout of a Basic Array Machine
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2025
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Btree in a block on the surface of a silicon chip.

import java.util.*;

abstract class StuckBam extends Test                                            // Stuck implemented in the basic array machine
 {final BtreeBam       btree = new BtreeBam(2, 3, 10);                          // The Btree definition
  final LayoutBam     layout = btree.layout;                                    // Layout of btree
  final LayoutBam     result = layoutResult();                                  // Layout of memory recording the results of a stuck operation
  final int maxKeysPerLeaf   = btree.maxKeysPerLeaf;                            // Maximum numbers of keys per leaf
  final int maxKeysPerBranch = btree.maxKeysPerBranch;                          // Maximum number of keys per branch

  int stuck_size       (String S) {return layout.get("currentSize", S);}        // The current number of key elements in the stuck
  int stuck_size1      (String S) {return stuck_size(S) - 1;}                   // The current number of key elements in the stuck minus one whichmakes it suitable for describing a branch
  boolean stuck_isEmpty(String S) {return stuck_size(S) == 0;}                  // Check the stuck is empty
  boolean leaf_isFull  (String S) {return stuck_size(S) > maxKeysPerLeaf;}      // Check the leaf stuck is full
  boolean branch_isFull(String S) {return stuck_size(S) > maxKeysPerBranch;}    // Check the branch stuck is full

  int stuck_key (String S, String I) {return layout.get("keys", I);}
  int stuck_data(String S, String I) {return layout.get("data", I);}

  void stuck_setKey   (String S, String I, String V) {layout.put(V, "keys",   S, I);}
  void stuck_setData  (String S, String I, String V) {layout.put(V, "data",   S, I);}

  void stuck_copyKey  (String S, String Target, String Source) {layout.move("keys", "keys", S, Target, Source);}
  void stuck_copyData (String S, String Target, String Source) {layout.move("data", "data", S, Target, Source);}

  void  stuck_setKeyData(String S, String Index, String Key, String Data)       // Set key, data pair in a stuck
   {stuck_setKey  (S, Index, Key);
    stuck_setData (S, Index, Data);
   }
                                                                                // Copy key, data pair in a stuck
  void stuck_copyKeyData(String S, int Target, int Source)
   {stuck_copyKey (S, Target, Source);
    stuck_copyData(S, Target, Source);
   }

  void stuck_inc  (String S) {layout.add(+1, "current_size");}                  // Increment the current size
  void stuck_dec  (String S) {layout.add(-1, "current_size");}                  // Decrement the current size
  void stuck_clear(String S) {layout.set( 0, "current_size");}                  // Clear the stuck

  void  stuck_push (String S, String Key, String Data)                           // Push an element onto a stuck
   {int n = stuck_size(S);
    stuck_setKeyData(S, ""+n, Key, Data);
    stuck_inc(s);
   }

  void  stuck_unshift(String S, String Key, String Data)                        // Unshift an element onto a stuck
   {for (int i = stuck_size(s); i > 0; --i)                                     // Shift the stuck up one place
     {stuck_copyKeyData(s, ""+i, ""+(i-1));
     }
    stuck_setKeyData(s, "0", Key, Data);
    stuck_inc(s);
   }

  LayoutBam layoutResult()                                                      // Layout the search results area
   {final LayoutBam l = new LayoutBam()
     {return new LayoutBam()
       {void load()
         {l.array("search");                                                    // Search key
          l.array("found");                                                     // Whether a matching element was found
          l.array("index");                                                     // The index from which the key, data pair were retrieved
          l.array("key");                                                       // The retrieved key
          l.array("data");                                                      // The retrieved data
         }
       };
     };
   }

  void stuck_pop(String S)                                                      // Pop from a leaf stuck
   {stuck_dec(S);
    final int s = stuck_size(S);
    result.set(s,                        "index");
    result.set(stuck_key ("keys", S, s), "key");
    result.set(stuck_data("data", S, s), "data");
    return r;
   }

  void stuck_shift(String S)
   {result.set(stuck_key (S, "0"), "key");
    result.set(stuck_data(S, "0"), "data");
    for (int i = 0, j = stuck_size(s)-1; i < j; i++)
     {stuck_copyKeyData(s, i, i+1);
     }
    stuck_dec(S);
   }

  void stuck_elementAt(String S, String Index)
   {result.move("index", Indexset(index = Index;
    result.set(key   = stuck_key (s, Index);
    result.set(data  = stuck_data(s, Index);
   }

void  stuck_setElementAt(String S, String Key, String Data, int Index)                 // Set an element either in range or one above the current range
 {if (Index == stuck_size(s))                                                   // Extended range
   {stuck_setKeyData(s, Index, key, data); stuck_inc(s);
   }
  else                                                                          // In range
   {stuck_setKeyData(s, Index, key, data);
   }
 }

void  stuck_insertElementAt(String S, String Key, String Data, int Index)              // Insert an element at the indicated location shifting all the remaining elements up one
 {for (int i = stuck_size(s); i > Index; --i)
   {stuck_copyKeyData(s, i, i-1);
   }
  stuck_setKeyData(s, Index, key, data);
  stuck_inc(s);
 }

Stuck_Result stuck_removeElementAt(String S, int Index)                         // Remove the indicated element
 {Stuck_Result r = stuck_result();
  result.index = Index;
  result.key   = stuck_key (s, Index);
  result.data  = stuck_data(s, Index);
  for (int i = Index, j = stuck_size(s)-1; i < j; i++)                          // Shift the stuck down one place
   {stuck_copyKeyData(s, i, i+1);
   }
  stuck_dec(s);
  return r;
 }

Stuck_Result stuck_firstElement(String S)
 {Stuck_Result r = stuck_result();

  result.found = !stuck_isEmpty(s);
  if (result.found)
   {result.index = 0;
    result.key   = stuck_key (s, 0);
    result.data  = stuck_data(s, 0);
   }
  return r;
 }

Stuck_Result stuck_lastElement(String S)
 {Stuck_Result r = stuck_result();

  result.found = !stuck_isEmpty(s);
  int i = stuck_size(s)-1;
  if (result.found)
   {result.index = i;
    result.key   = stuck_key (s, i);
    result.data  = stuck_data(s, i);
   }
  return r;
 }

//D1 Search                                                                     // Search a stuck.

Stuck_Result stuck_search(String S, int Search)                                 // Search for an element within all elements of the stuck
 {Stuck_Result r = stuck_result();
  result.key       = Search;

  for (int i = 0, j = stuck_size(s); i < j; i++)                                // Search
   {if (stuck_key(s, i) == Search)
     {result.found = 1;
      result.index = i;
      result.data  = stuck_data(s, i);
      return r;
     }
   }
  result.found = 0;
  return r;
 }

Stuck_Result stuck_searchFirstGreaterThanOrEqual(String S, int Search)
 {Stuck_Result r = stuck_result();
  result.search = Search;
  for (int i = 0, j = stuck_size(s); i < j; i++)
   {if (stuck_key(s, i) >= Search)
     {result.found = 1;
      result.index = i;
      result.key = stuck_key(s, i);
      result.data = stuck_data(s, i);
      return r;
     }
   }
  result.found = 0;
  return r;
 }

Stuck_Result stuck_searchFirstGreaterThanOrEqualExceptLast(String S, int Search)
 {Stuck_Result r = stuck_result();
  result.search = Search;
  int L = stuck_size(s)-1;
  for (int i = 0, j = L; i < j; i++)
   {if (stuck_key(s, i) >= Search)
     {result.found = 1;
      result.index = i;
      result.key = stuck_key(s, i);
      result.data = stuck_data(s, i);
      return r;
     }
   }
  result.found = 1;
  result.index = L;
  result.key   = 0;
  result.data  = stuck_data(s, L);
  return r;
 }

// Tests

#ifdef assert_checks
int stuck_tests_passed = 0;
int stuck_tests_failed = 0;
#endif

//D1 Print                                                                      // Print a stuck

#ifdef assert_checks
char *stuck_print(String S)                                                     // Print a stuck
 {char *C = (char *)malloc(4096), *c = C;
  int N = stuck_size(s);
  c += sprintf(c, "Stuck(maxSize:%d, size:%d)\n", stuck_maxSize, N);
  for (int i = 0; i < N; i++)                                                   // Search
   {c += sprintf(c, "  %2d key: %2d data:%2d\n", i, stuck_key(s, i), stuck_data(s, i));
   }
  *c = 0;
  return C;
 }

void  stuck_print_err(String S)                                                  // Print a stuck on stderr
 {fprintf(stderr, "%s", stuck_print(s));
 }

char *stuck_print_result(Stuck_Result r)                                        // Print the result of a stuck operation
 {char *C = (char *)malloc(4096), *c = C;
  c += sprintf(c, "search: %d\n", result.search);
  c += sprintf(c, " found: %d\n", result.found);
  c += sprintf(c, " index: %d\n", result.index);
  c += sprintf(c, "   key: %d\n", result.key);
  c += sprintf(c, "  data: %d\n", result.data);
  *c = 0;
  return C;
 }

void  stuck_print_result_err(Stuck_Result r)                                     // Print the result of a stuck operation
 {fprintf(stderr, "%s", stuck_print_result(r));
 }
#endif

//D1 Tests                                                                      // Testing

#ifdef assert_checks

void  stuck_ok(const char *name, const char *g, const char *e)                   // Test got versus expected
 {int c = strcmp(g, e);
  if (c == 0)
   {++stuck_tests_passed;
    return;
   }
  ++stuck_tests_failed;
  printf("Test: %s failed\n", name);
 }

void  stuck_check_result_field(const char *format, int got, int expected)
 {if (expected >= 0 && got != expected)
   {stuck_tests_failed++;
    printf(format, got, expected);
   }
  else
   {stuck_tests_passed++;
   }
 }

void  stuck_check_result(Stuck_Result r , int Search, int Found, int Index, String Key, String Data)  // Check a result
 {stuck_check_result_field("Search got %2d, expected %2d\n", result.search, Search);
  stuck_check_result_field("Found  got %2d, expected %2d\n", result.found,  Found);
  stuck_check_result_field("Index  got %2d, expected %2d\n", result.index,  Index);
  stuck_check_result_field("Key    got %2d, expected %2d\n", result.key,    Key);
  stuck_check_result_field("Data   got %2d, expected %2d\n", result.data,   Data);
 }

//D0 Tests                                                                      // Test stuck

String Stuck_test_load()
 {String S = (Stuck *)calloc(sizeof(Stuck), 1);

  stuck_push(s, 2, 1);
  stuck_push(s, 4, 2);
  stuck_push(s, 6, 3);
  stuck_push(s, 8, 4);

  return s;
 }

void  stuck_test_push()
 {Stuck *t = stuck_test_load();

  //stuck_print_err(t),
  stuck_ok("push", stuck_print(t),
"Stuck(maxSize:20, size:4)\n"
"   0 key:  2 data: 1\n"
"   1 key:  4 data: 2\n"
"   2 key:  6 data: 3\n"
"   3 key:  8 data: 4\n"
);
 }

void  stuck_test_pop()
 {Stuck *t = stuck_test_load();

  Stuck_Result r  = stuck_pop(t);
  stuck_check_result(r, 0,0,3,8,4);

  //stuck_print_err(t),
  stuck_ok("pop", stuck_print(t),
"Stuck(maxSize:20, size:3)\n"
"   0 key:  2 data: 1\n"
"   1 key:  4 data: 2\n"
"   2 key:  6 data: 3\n"
);
 }

void  stuck_test_shift()
 {Stuck       *t = stuck_test_load();
  Stuck_Result r = stuck_shift(t);
  //stuck_print_result_err(r);
  stuck_check_result(r, 0,0,0,2,1);

  //stuck_print_err(t),
  stuck_ok("shift", stuck_print(t),
"Stuck(maxSize:20, size:3)\n"
"   0 key:  4 data: 2\n"
"   1 key:  6 data: 3\n"
"   2 key:  8 data: 4\n"
);
 }

void  stuck_test_unshift()
 {Stuck *t = stuck_test_load();
  stuck_unshift(t, 9, 8);

  //stuck_print_err(t),
  stuck_ok("unshift", stuck_print(t),
"Stuck(maxSize:20, size:5)\n"
"   0 key:  9 data: 8\n"
"   1 key:  2 data: 1\n"
"   2 key:  4 data: 2\n"
"   3 key:  6 data: 3\n"
"   4 key:  8 data: 4\n"
);
   }

void  stuck_test_elementAt()
 {Stuck *t = stuck_test_load();
  Stuck_Result r  = stuck_elementAt(t, 2);
  //stuck_print_result_err(r);
  stuck_check_result(r, 0,0,2,6,3);
 }

void  stuck_test_insert_element_at()
 {Stuck *t = stuck_test_load();
  stuck_insertElementAt(t, 9, 8, 4);
  //stuck_print_err(t);
  stuck_ok("insertElementAt", stuck_print(t),
"Stuck(maxSize:20, size:5)\n"
"   0 key:  2 data: 1\n"
"   1 key:  4 data: 2\n"
"   2 key:  6 data: 3\n"
"   3 key:  8 data: 4\n"
"   4 key:  9 data: 8\n"
);
 }

void  stuck_test_remove_element_at()
 {Stuck *t = stuck_test_load();
  stuck_removeElementAt(t, 2);
  //stuck_print_err(t);
  stuck_ok("removeElementAt", stuck_print(t),
"Stuck(maxSize:20, size:3)\n"
"   0 key:  2 data: 1\n"
"   1 key:  4 data: 2\n"
"   2 key:  8 data: 4\n"
);
 }

void  stuck_test_first_last()
 {Stuck *t = stuck_test_load();
  Stuck_Result f = stuck_firstElement(t);
  Stuck_Result l = stuck_lastElement (t);
  //stuck_print_result_err(f);
  //stuck_print_result_err(l);
  stuck_check_result(f, 0,1,0,2,1);
  stuck_check_result(l, 0,1,3,8,4);
 }

void  stuck_test_search()
 {Stuck *t = stuck_test_load();
  Stuck_Result s = stuck_search(t, 6);
  //stuck_print_result_err(s);
  stuck_check_result(s, 0,1,2,6,3);
 }

void  stuck_test_search_first_greater_than_or_equal()
 {Stuck *t = stuck_test_load();
  Stuck_Result s = stuck_searchFirstGreaterThanOrEqual(t, 7);
  //stuck_print_result_err(s);
  stuck_check_result(s, 7,1,3,8,4);
 }

void  stuck_test_search_first_greater_than_or_equal_except_last()
 {Stuck *t = stuck_test_load();
  Stuck_Result s = stuck_searchFirstGreaterThanOrEqualExceptLast(t, 7);
  //stuck_print_result_err(s);
  stuck_check_result(s, 7,1,3,0,4);
  Stuck_Result S = stuck_searchFirstGreaterThanOrEqualExceptLast(t, 5);
  //stuck_print_result_err(S);
  stuck_check_result(S, 5,1,2,6,3);
 }

void  stuck_test_set_element_at()
 {Stuck *t = stuck_test_load();
  stuck_setElementAt(t, 22, 33, 2);
  //stuck_print_err(t);
  stuck_ok("setElementAt", stuck_print(t),
"Stuck(maxSize:20, size:4)\n"
"   0 key:  2 data: 1\n"
"   1 key:  4 data: 2\n"
"   2 key: 22 data:33\n"
"   3 key:  8 data: 4\n"
);
 }

int stuck_tests()                                                               // Tests
 {stuck_test_push();
  stuck_test_pop();
  stuck_test_shift();
  stuck_test_unshift();
  stuck_test_elementAt();
  stuck_test_insert_element_at();
  stuck_test_remove_element_at();
  stuck_test_first_last();
  stuck_test_search();
  stuck_test_search_first_greater_than_or_equal();
  stuck_test_search_first_greater_than_or_equal_except_last();
  stuck_test_set_element_at();

  if (1)
   {int p = stuck_tests_passed, f = stuck_tests_failed, n = p + f;

    if      (f == 0 && p > 0) {printf("Passed all %d tests\n", n);                        return 0;}
    else if (          f > 0) {printf("FAILed %d, passed %d tests out of %d\n", f, p, n); return f;}
    else                      {printf("No tests run\n");                                  return 1;}
   }
  return  0;
 }


  static void oldTests()                                                        // Tests thought to be in good shape
   {
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
     {System.erresult.println(e);
      System.erresult.println(fullTraceBack(e));
      System.exit(1);
     }
   }
 }
