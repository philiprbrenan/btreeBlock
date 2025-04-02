//------------------------------------------------------------------------------
// Branch
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2025
//------------------------------------------------------------------------------
#ifndef branch_maxSize
#define branch_maxSize  20                                                       // The maximum number of entries in the stuck.
#endif
#ifndef branch_keyType
#define branch_keyType  int                                                      // The type of a key
#endif
#ifndef branch_dataType
#define branch_dataType int                                                      // The type of a data item in a stuck
#endif

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef struct                                                                  // Definition of a stuck
 {int currentSize;                                                              // Current size of the stuck
  branch_keyType  keys[branch_maxSize];                                           // Keys
  branch_dataType data[branch_maxSize];                                           // Data
 } Branch;

int branch_size   (Branch *s) {return s->currentSize;}                            // The current number of key elements in the stuck
int branch_size1  (Branch *s) {return s->currentSize-1;}                          // The current number of key elements in the stuck minus one whichmakes it suitable for describing a branch
int branch_isFull (Branch *s) {return branch_size(s) > branch_maxSize;}             // Check the stuck is full
int branch_isEmpty(Branch *s) {return branch_size(s) == 0;}                        // Check the stuck is empty

//D1 Memory                                                                     // Actions on memory of stuck

branch_keyType  branch_key (Branch *s, int Index)              {return s->keys[Index];}
branch_dataType branch_data(Branch *s, int Index)              {return s->data[Index];}

void       branch_setKey  (Branch *s, int Index,  int Value)  {s->keys[Index] = Value;}
void       branch_setData (Branch *s, int Index,  int Value)  {s->data[Index] = Value;}
void       branch_copyKey (Branch *s, int Target, int Source) {s->keys[Target] = s->keys[Source];}
void       branch_copyData(Branch *s, int Target, int Source) {s->data[Target] = s->data[Source];}

void  branch_setKeyData(Branch *s, int Index, int Key, int Data)
 {branch_setKey (s, Index, Key);
  branch_setData(s, Index, Data);
 }

void branch_copyKeyData(Branch *s, int Target, int Source)
 {branch_copyKey (s, Target, Source);
  branch_copyData(s, Target, Source);
 }

//D1 Actions                                                                    // Place and remove data to/from stuck

void branch_inc  (Branch *s) {s->currentSize++;}                                  // Increment the current size
void branch_dec  (Branch *s) {s->currentSize--;}                                  // Decrement the current size
void branch_clear(Branch *s) {s->currentSize = 0;}                                // Clear the stuck

void branch_push (Branch *s, int key, int data)                                   // Push an element onto the stuck
 {int n = branch_size(s);
  branch_setKeyData(s, n, key, data);
  branch_inc(s);
 }

void branch_unshift(Branch *s, int key, int data)                                 // Unshift an element onto the stuck
 {for (int i = branch_size(s); i > 0; --i)                                       // Shift the stuck up one place
   {branch_copyKeyData(s, i, i-1);
   }
  branch_setKeyData(s, 0, key, data);
  branch_inc(s);
 }

typedef struct
 {int          search;                                                          // Search key
  int           found;                                                          // Whether a matcbing element was found
  int           index;                                                          // The index from which the key, data pair were retrieved
  branch_keyType   key;                                                          // The retrieved key
  branch_dataType data;                                                          // The retrieved data
 } Branch_Result;

Branch_Result branch_result()
 {Branch_Result r;
  r.search = r.found = r.index = r.key = r.data = 0;
  return r;
 }

Branch_Result branch_pop(Branch *s)
 {Branch_Result r = branch_result();
  branch_dec(s);
  r.index = branch_size(s);
  r.key   = branch_key (s, r.index);
  r.data  = branch_data(s, r.index);
  return r;
 }

Branch_Result branch_shift(Branch *s)
 {Branch_Result r = branch_result();
  r.key   = branch_key (s, 0);
  r.data  = branch_data(s, 0);
  for (int i = 0, j = branch_size(s)-1; i < j; i++)
   {branch_copyKeyData(s, i, i+1);
   }
  branch_dec(s);
  return r;
 }

Branch_Result branch_elementAt(Branch *s, int Index)
 {Branch_Result r = branch_result();
  r.index = Index;
  r.key   = branch_key (s, Index);
  r.data  = branch_data(s, Index);
  return r;
 }

void branch_setElementAt(Branch *s, int key, int data, int Index)                 // Set an element either in range or one above the current range
 {if (Index == branch_size(s))                                                   // Extended range
   {branch_setKeyData(s, Index, key, data); branch_inc(s);
   }
  else                                                                          // In range
   {branch_setKeyData(s, Index, key, data);
   }
 }

void branch_insertElementAt(Branch *s, int key, int data, int Index)              // Insert an element at the indicated location shifting all the remaining elements up one
 {for (int i = branch_size(s); i > Index; --i)
   {branch_copyKeyData(s, i, i-1);
   }
  branch_setKeyData(s, Index, key, data);
  branch_inc(s);
 }

Branch_Result branch_removeElementAt(Branch *s, int Index)                         // Remove the indicated element
 {Branch_Result r = branch_result();
  r.index = Index;
  r.key   = branch_key (s, Index);
  r.data  = branch_data(s, Index);
  for (int i = Index, j = branch_size(s)-1; i < j; i++)                          // Shift the stuck down one place
   {branch_copyKeyData(s, i, i+1);
   }
  branch_dec(s);
  return r;
 }

Branch_Result branch_firstElement(Branch *s)
 {Branch_Result r = branch_result();

  r.found = !branch_isEmpty(s);
  if (r.found)
   {r.index = 0;
    r.key   = branch_key (s, 0);
    r.data  = branch_data(s, 0);
   }
  return r;
 }

Branch_Result branch_lastElement(Branch *s)
 {Branch_Result r = branch_result();

  r.found = !branch_isEmpty(s);
  int i = branch_size(s)-1;
  if (r.found)
   {r.index = i;
    r.key   = branch_key (s, i);
    r.data  = branch_data(s, i);
   }
  return r;
 }

//D1 Search                                                                     // Search a stuck.

Branch_Result branch_search(Branch *s, int Search)                                 // Search for an element within all elements of the stuck
 {Branch_Result r = branch_result();
  r.key       = Search;

  for (int i = 0, j = branch_size(s); i < j; i++)                                // Search
   {if (branch_key(s, i) == Search)
     {r.found = 1;
      r.index = i;
      r.data  = branch_data(s, i);
      return r;
     }
   }
  r.found = 0;
  return r;
 }

Branch_Result branch_searchFirstGreaterThanOrEqual(Branch *s, int Search)
 {Branch_Result r = branch_result();
  r.search = Search;
  for (int i = 0, j = branch_size(s); i < j; i++)
   {if (branch_key(s, i) >= Search)
     {r.found = 1;
      r.index = i;
      r.key = branch_key(s, i);
      r.data = branch_data(s, i);
      return r;
     }
   }
  r.found = 0;
  return r;
 }

Branch_Result branch_searchFirstGreaterThanOrEqualExceptLast(Branch *s, int Search)
 {Branch_Result r = branch_result();
  r.search = Search;
  int L = branch_size(s)-1;
  for (int i = 0, j = L; i < j; i++)
   {if (branch_key(s, i) >= Search)
     {r.found = 1;
      r.index = i;
      r.key = branch_key(s, i);
      r.data = branch_data(s, i);
      return r;
     }
   }
  r.found = 1;
  r.index = L;
  r.key   = 0;
  r.data  = branch_data(s, L);
  return r;
 }

// Tests

int branch_tests_passed = 0;
int branch_tests_failed = 0;

//D1 Print                                                                      // Print a stuck

char *branch_print(Branch *s)                                                     // Print a stuck
 {char *C = (char *)malloc(4096), *c = C;
  int N = branch_size(s);
  c += sprintf(c, "Branch(maxSize:%d, size:%d)\n", branch_maxSize, N);
  for (int i = 0; i < N; i++)                                                   // Search
   {c += sprintf(c, "  %2d key: %2d data:%2d\n", i, branch_key(s, i), branch_data(s, i));
   }
  *c = 0;
  return C;
 }

void branch_print_err(Branch *s)                                                  // Print a stuck on stderr
 {fprintf(stderr, "%s", branch_print(s));
 }

char *branch_print_result(Branch_Result r)                                        // Print the result of a stuck operation
 {char *C = (char *)malloc(4096), *c = C;
  c += sprintf(c, "search: %d\n", r.search);
  c += sprintf(c, " found: %d\n", r.found);
  c += sprintf(c, " index: %d\n", r.index);
  c += sprintf(c, "   key: %d\n", r.key);
  c += sprintf(c, "  data: %d\n", r.data);
  *c = 0;
  return C;
 }

void branch_print_result_err(Branch_Result r)                                     // Print the result of a stuck operation
 {fprintf(stderr, "%s", branch_print_result(r));
 }

//D1 Tests                                                                      // Testing

#ifdef branch_runTests

void branch_ok(const char *name, const char *g, const char *e)                   // Test got versus expected
 {int c = strcmp(g, e);
  if (c == 0)
   {++branch_tests_passed;
    return;
   }
  ++branch_tests_failed;
  printf("Test: %s failed\n", name);
 }

void branch_check_result_field(const char *format, int got, int expected)
 {if (expected >= 0 && got != expected)
   {branch_tests_failed++;
    printf(format, got, expected);
   }
  else
   {branch_tests_passed++;
   }
 }

void branch_check_result(Branch_Result r , int Search, int Found, int Index, int Key, int Data)  // Check a result
 {branch_check_result_field("Search got %2d, expected %2d\n", r.search, Search);
  branch_check_result_field("Found  got %2d, expected %2d\n", r.found,  Found);
  branch_check_result_field("Index  got %2d, expected %2d\n", r.index,  Index);
  branch_check_result_field("Key    got %2d, expected %2d\n", r.key,    Key);
  branch_check_result_field("Data   got %2d, expected %2d\n", r.data,   Data);
 }

//D0 Tests                                                                      // Test stuck

Branch *branch_test_load()
 {Branch *s = (Branch *)calloc(sizeof(Branch), 1);

  branch_push(s, 2, 1);
  branch_push(s, 4, 2);
  branch_push(s, 6, 3);
  branch_push(s, 8, 4);

  return s;
 }

void branch_test_push()
 {Branch *t = branch_test_load();

  //branch_print_err(t),
  branch_ok("push", branch_print(t),
"Branch(maxSize:20, size:4)\n"
"   0 key:  2 data: 1\n"
"   1 key:  4 data: 2\n"
"   2 key:  6 data: 3\n"
"   3 key:  8 data: 4\n"
);
 }

void branch_test_pop()
 {Branch *t = branch_test_load();

  Branch_Result r  = branch_pop(t);
  branch_check_result(r, 0,0,3,8,4);

  //branch_print_err(t),
  branch_ok("pop", branch_print(t),
"Branch(maxSize:20, size:3)\n"
"   0 key:  2 data: 1\n"
"   1 key:  4 data: 2\n"
"   2 key:  6 data: 3\n"
);
 }

void branch_test_shift()
 {Branch       *t = branch_test_load();
  Branch_Result r = branch_shift(t);
  //branch_print_result_err(r);
  branch_check_result(r, 0,0,0,2,1);

  //branch_print_err(t),
  branch_ok("shift", branch_print(t),
"Branch(maxSize:20, size:3)\n"
"   0 key:  4 data: 2\n"
"   1 key:  6 data: 3\n"
"   2 key:  8 data: 4\n"
);
 }

void branch_test_unshift()
 {Branch *t = branch_test_load();
  branch_unshift(t, 9, 8);

  //branch_print_err(t),
  branch_ok("unshift", branch_print(t),
"Branch(maxSize:20, size:5)\n"
"   0 key:  9 data: 8\n"
"   1 key:  2 data: 1\n"
"   2 key:  4 data: 2\n"
"   3 key:  6 data: 3\n"
"   4 key:  8 data: 4\n"
);
   }

void branch_test_elementAt()
 {Branch *t = branch_test_load();
  Branch_Result r  = branch_elementAt(t, 2);
  //branch_print_result_err(r);
  branch_check_result(r, 0,0,2,6,3);
 }

void branch_test_insert_element_at()
 {Branch *t = branch_test_load();
  branch_insertElementAt(t, 9, 8, 4);
  //branch_print_err(t);
  branch_ok("insertElementAt", branch_print(t),
"Branch(maxSize:20, size:5)\n"
"   0 key:  2 data: 1\n"
"   1 key:  4 data: 2\n"
"   2 key:  6 data: 3\n"
"   3 key:  8 data: 4\n"
"   4 key:  9 data: 8\n"
);
 }

void branch_test_remove_element_at()
 {Branch *t = branch_test_load();
  branch_removeElementAt(t, 2);
  //branch_print_err(t);
  branch_ok("removeElementAt", branch_print(t),
"Branch(maxSize:20, size:3)\n"
"   0 key:  2 data: 1\n"
"   1 key:  4 data: 2\n"
"   2 key:  8 data: 4\n"
);
 }

void branch_test_first_last()
 {Branch *t = branch_test_load();
  Branch_Result f = branch_firstElement(t);
  Branch_Result l = branch_lastElement (t);
  //branch_print_result_err(f);
  //branch_print_result_err(l);
  branch_check_result(f, 0,1,0,2,1);
  branch_check_result(l, 0,1,3,8,4);
 }

void branch_test_search()
 {Branch *t = branch_test_load();
  Branch_Result s = branch_search(t, 6);
  //branch_print_result_err(s);
  branch_check_result(s, 0,1,2,6,3);
 }

void branch_test_search_first_greater_than_or_equal()
 {Branch *t = branch_test_load();
  Branch_Result s = branch_searchFirstGreaterThanOrEqual(t, 7);
  //branch_print_result_err(s);
  branch_check_result(s, 7,1,3,8,4);
 }

void branch_test_search_first_greater_than_or_equal_except_last()
 {Branch *t = branch_test_load();
  Branch_Result s = branch_searchFirstGreaterThanOrEqualExceptLast(t, 7);
  //branch_print_result_err(s);
  branch_check_result(s, 7,1,3,0,4);
  Branch_Result S = branch_searchFirstGreaterThanOrEqualExceptLast(t, 5);
  //branch_print_result_err(S);
  branch_check_result(S, 5,1,2,6,3);
 }

void branch_test_set_element_at()
 {Branch *t = branch_test_load();
  branch_setElementAt(t, 22, 33, 2);
  //branch_print_err(t);
  branch_ok("setElementAt", branch_print(t),
"Branch(maxSize:20, size:4)\n"
"   0 key:  2 data: 1\n"
"   1 key:  4 data: 2\n"
"   2 key: 22 data:33\n"
"   3 key:  8 data: 4\n"
);
 }

int branch_tests()                                                               // Tests
 {branch_test_push();
  branch_test_pop();
  branch_test_shift();
  branch_test_unshift();
  branch_test_elementAt();
  branch_test_insert_element_at();
  branch_test_remove_element_at();
  branch_test_first_last();
  branch_test_search();
  branch_test_search_first_greater_than_or_equal();
  branch_test_search_first_greater_than_or_equal_except_last();
  branch_test_set_element_at();

  if (1)
   {int p = branch_tests_passed, f = branch_tests_failed, n = p + f;

    if      (f == 0 && p > 0) {printf("Passed all %d tests\n", n);                        return 0;}
    else if (          f > 0) {printf("FAILed %d, passed %d tests out of %d\n", f, p, n); return f;}
    else                      {printf("No tests run\n");                                  return 1;}
   }
  return  0;
 }
#endif
