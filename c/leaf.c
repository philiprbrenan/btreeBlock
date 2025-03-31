//------------------------------------------------------------------------------
// Leaf
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2025
//------------------------------------------------------------------------------
#define leaf_maxSize  8                                                       // The maximum number of entries in the stuck.
#define leaf_keyType  int                                                      // The type of a key
#define leaf_dataType char                                                      // The type of a data item in a stuck

typedef struct                                                                  // Definition of a stuck
 {int currentSize;                                                              // Current size of the stuck
  leaf_keyType  keys[leaf_maxSize];                                           // Keys
  leaf_dataType data[leaf_maxSize];                                           // Data
 } Leaf;

int leaf_size   (Leaf *s) {return s->currentSize;}                            // The current number of key elements in the stuck
int leaf_isFull (Leaf *s) {return leaf_size(s) > leaf_maxSize;}             // Check the stuck is full
int leaf_isEmpty(Leaf *s) {return leaf_size(s) == 0;}                        // Check the stuck is empty

//D1 Memory                                                                     // Actions on memory of stuck

leaf_keyType  leaf_key (Leaf *s, int Index)              {return s->keys[Index];}
leaf_dataType leaf_data(Leaf *s, int Index)              {return s->data[Index];}

void       leaf_setKey  (Leaf *s, int Index,  int Value)  {s->keys[Index] = Value;}
void       leaf_setData (Leaf *s, int Index,  int Value)  {s->data[Index] = Value;}
void       leaf_copyKey (Leaf *s, int Target, int Source) {s->keys[Target] = s->keys[Source];}
void       leaf_copyData(Leaf *s, int Target, int Source) {s->data[Target] = s->data[Source];}

void  leaf_setKeyData(Leaf *s, int Index, int Key, int Data)
 {leaf_setKey (s, Index, Key);
  leaf_setData(s, Index, Data);
 }

void leaf_copyKeyData(Leaf *s, int Target, int Source)
 {leaf_copyKey (s, Target, Source);
  leaf_copyData(s, Target, Source);
 }

//D1 Actions                                                                    // Place and remove data to/from stuck

void leaf_inc  (Leaf *s) {s->currentSize++;}                                  // Increment the current size
void leaf_dec  (Leaf *s) {s->currentSize--;}                                  // Decrement the current size
void leaf_clear(Leaf *s) {s->currentSize = 0;}                                // Clear the stuck

void leaf_push (Leaf *s, int key, int data)                                   // Push an element onto the stuck
 {int n = leaf_size(s);
  leaf_setKeyData(s, n, key, data);
  leaf_inc(s);
 }

void leaf_unshift(Leaf *s, int key, int data)                                 // Unshift an element onto the stuck
 {for (int i = leaf_size(s); i > 0; --i)                                       // Shift the stuck up one place
   {leaf_copyKeyData(s, i, i-1);
   }
  leaf_setKeyData(s, 0, key, data);
  leaf_inc(s);
 }

typedef struct
 {int          search;                                                          // Search key
  int           found;                                                          // Whether a matcbing element was found
  int           index;                                                          // The index from which the key, data pair were retrieved
  leaf_keyType   key;                                                          // The retrieved key
  leaf_dataType data;                                                          // The retrieved data
 } Leaf_Result;

Leaf_Result leaf_result()
 {Leaf_Result r;
  r.search = r.found = r.index = r.key = r.data = 0;
  return r;
 }

Leaf_Result leaf_pop(Leaf *s)
 {Leaf_Result r = leaf_result();
  leaf_dec(s);
  r.index = leaf_size(s);
  r.key   = leaf_key (s, r.index);
  r.data  = leaf_data(s, r.index);
  return r;
 }

Leaf_Result leaf_shift(Leaf *s)
 {Leaf_Result r = leaf_result();
  r.key   = leaf_key (s, 0);
  r.data  = leaf_data(s, 0);
  for (int i = 0, j = leaf_size(s)-1; i < j; i++)
   {leaf_copyKeyData(s, i, i+1);
   }
  leaf_dec(s);
  return r;
 }

Leaf_Result leaf_elementAt(Leaf *s, int Index)
 {Leaf_Result r = leaf_result();
  r.index = Index;
  r.key   = leaf_key (s, Index);
  r.data  = leaf_data(s, Index);
  return r;
 }

void leaf_setElementAt(Leaf *s, int key, int data, int Index)                 // Set an element either in range or one above the current range
 {if (Index == leaf_size(s))                                                   // Extended range
   {leaf_setKeyData(s, Index, key, data); leaf_inc(s);
   }
  else                                                                          // In range
   {leaf_setKeyData(s, Index, key, data);
   }
 }

void leaf_insertElementAt(Leaf *s, int key, int data, int Index)              // Insert an element at the indicated location shifting all the remaining elements up one
 {for (int i = leaf_size(s); i > Index; --i)
   {leaf_copyKeyData(s, i, i-1);
   }
  leaf_setKeyData(s, Index, key, data);
  leaf_inc(s);
 }

Leaf_Result leaf_removeElementAt(Leaf *s, int Index)                         // Remove the indicated element
 {Leaf_Result r = leaf_result();
  r.index = Index;
  r.key   = leaf_key (s, Index);
  r.data  = leaf_data(s, Index);
  for (int i = Index, j = leaf_size(s)-1; i < j; i++)                          // Shift the stuck down one place
   {leaf_copyKeyData(s, i, i+1);
   }
  leaf_dec(s);
  return r;
 }

Leaf_Result leaf_firstElement(Leaf *s)
 {Leaf_Result r = leaf_result();

  r.found = !leaf_isEmpty(s);
  if (r.found)
   {r.index = 0;
    r.key   = leaf_key (s, 0);
    r.data  = leaf_data(s, 0);
   }
  return r;
 }

Leaf_Result leaf_lastElement(Leaf *s)
 {Leaf_Result r = leaf_result();

  r.found = !leaf_isEmpty(s);
  int i = leaf_size(s)-1;
  if (r.found)
   {r.index = i;
    r.key   = leaf_key (s, i);
    r.data  = leaf_data(s, i);
   }
  return r;
 }

//D1 Search                                                                     // Search a stuck.

Leaf_Result leaf_search(Leaf *s, int Search)                                 // Search for an element within all elements of the stuck
 {Leaf_Result r = leaf_result();
  r.key       = Search;

  for (int i = 0, j = leaf_size(s); i < j; i++)                                // Search
   {if (leaf_key(s, i) == Search)
     {r.found = 1;
      r.index = i;
      r.data  = leaf_data(s, i);
      return r;
     }
   }
  r.found = 0;
  return r;
 }

Leaf_Result leaf_searchFirstGreaterThanOrEqual(Leaf *s, int Search)
 {Leaf_Result r = leaf_result();
  r.search = Search;
  for (int i = 0, j = leaf_size(s); i < j; i++)
   {if (leaf_key(s, i) >= Search)
     {r.found = 1;
      r.index = i;
      r.key = leaf_key(s, i);
      r.data = leaf_data(s, i);
      return r;
     }
   }
  r.found = 0;
  return r;
 }

Leaf_Result leaf_searchFirstGreaterThanOrEqualExceptLast(Leaf *s, int Search)
 {Leaf_Result r = leaf_result();
  r.search = Search;
  for (int i = 0, j = leaf_size(s)-1; i < j; i++)
   {if (leaf_key(s, i) >= Search)
     {r.found = 1;
      r.index = i;
      r.key = leaf_key(s, i);
      r.data = leaf_data(s, i);
      return r;
     }
   }
  r.found = 0;
  return r;
 }

// Tests
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int leaf_tests_passed = 0;
int leaf_tests_failed = 0;

//D1 Print                                                                      // Print a stuck

char *leaf_print(Leaf *s)                                                     // Print a stuck
 {char *C = malloc(4096), *c = C;
  int N = leaf_size(s);
  c += sprintf(c, "Leaf(maxSize:%d, size:%d)\n", leaf_maxSize, N);
  for (int i = 0; i < N; i++)                                                   // Search
   {c += sprintf(c, "  %2d key: %2d data:%2d\n", i, leaf_key(s, i), leaf_data(s, i));
   }
  *c = 0;
  return C;
 }

void leaf_print_err(Leaf *s)                                                  // Print a stuck on stderr
 {fprintf(stderr, "%s", leaf_print(s));
 }

char *leaf_print_result(Leaf_Result r)                                        // Print the result of a stuck operation
 {char *C = malloc(4096), *c = C;
  c += sprintf(c, "search: %d\n", r.search);
  c += sprintf(c, " found: %d\n", r.found);
  c += sprintf(c, " index: %d\n", r.index);
  c += sprintf(c, "   key: %d\n", r.key);
  c += sprintf(c, "  data: %d\n", r.data);
  *c = 0;
  return C;
 }

void leaf_print_result_err(Leaf_Result r)                                     // Print the result of a stuck operation
 {fprintf(stderr, "%s", leaf_print_result(r));
 }

//D1 Tests                                                                      // Testing

void leaf_ok(char *name, char *g, char *e)                                     // Test got versus expected
 {int c = strcmp(g, e);
  if (c == 0)
   {++leaf_tests_passed;
    return;
   }
  ++leaf_tests_failed;
  printf("Test: %s failed\n", name);
 }

void leaf_check_result_field(char *format, int got, int expected)
 {if (expected >= 0 && got != expected)
   {leaf_tests_failed++;
    printf(format, got, expected);
   }
  else
   {leaf_tests_passed++;
   }
 }

void leaf_check_result(Leaf_Result r , int Search, int Found, int Index, int Key, int Data)  // Check a result
 {leaf_check_result_field("Search got %2d, expected %2d\n", r.search, Search);
  leaf_check_result_field("Found  got %2d, expected %2d\n", r.found,  Found);
  leaf_check_result_field("Index  got %2d, expected %2d\n", r.index,  Index);
  leaf_check_result_field("Key    got %2d, expected %2d\n", r.key,    Key);
  leaf_check_result_field("Data   got %2d, expected %2d\n", r.data,   Data);
 }

//D0 Tests                                                                      // Test stuck

Leaf *leaf_test_load()
 {Leaf *s = calloc(sizeof(Leaf), 1);

  leaf_push(s, 2, 1);
  leaf_push(s, 4, 2);
  leaf_push(s, 6, 3);
  leaf_push(s, 8, 4);

  return s;
 }

void leaf_test_push()
 {Leaf *t = leaf_test_load();

  //leaf_print_err(t),
  leaf_ok("push", leaf_print(t),
"Leaf(maxSize:8, size:4)\n"
"   0 key:  2 data: 1\n"
"   1 key:  4 data: 2\n"
"   2 key:  6 data: 3\n"
"   3 key:  8 data: 4\n"
);
 }

void leaf_test_pop()
 {Leaf *t = leaf_test_load();

  Leaf_Result r  = leaf_pop(t);
  leaf_check_result(r, 0,0,3,8,4);

  //leaf_print_err(t),
  leaf_ok("pop", leaf_print(t),
"Leaf(maxSize:8, size:3)\n"
"   0 key:  2 data: 1\n"
"   1 key:  4 data: 2\n"
"   2 key:  6 data: 3\n"
);
 }

void leaf_test_shift()
 {Leaf       *t = leaf_test_load();
  Leaf_Result r = leaf_shift(t);
  //leaf_print_result_err(r);
  leaf_check_result(r, 0,0,0,2,1);

  //leaf_print_err(t),
  leaf_ok("shift", leaf_print(t),
"Leaf(maxSize:8, size:3)\n"
"   0 key:  4 data: 2\n"
"   1 key:  6 data: 3\n"
"   2 key:  8 data: 4\n"
);
 }

void leaf_test_unshift()
 {Leaf *t = leaf_test_load();
  leaf_unshift(t, 9, 8);

  //leaf_print_err(t),
  leaf_ok("unshift", leaf_print(t),
"Leaf(maxSize:8, size:5)\n"
"   0 key:  9 data: 8\n"
"   1 key:  2 data: 1\n"
"   2 key:  4 data: 2\n"
"   3 key:  6 data: 3\n"
"   4 key:  8 data: 4\n"
);
   }

void leaf_test_elementAt()
 {Leaf *t = leaf_test_load();
  Leaf_Result r  = leaf_elementAt(t, 2);
  //leaf_print_result_err(r);
  leaf_check_result(r, 0,0,2,6,3);
 }

void leaf_test_insert_element_at()
 {Leaf *t = leaf_test_load();
  leaf_insertElementAt(t, 9, 8, 4);
  //leaf_print_err(t);
  leaf_ok("insertElementAt", leaf_print(t),
"Leaf(maxSize:8, size:5)\n"
"   0 key:  2 data: 1\n"
"   1 key:  4 data: 2\n"
"   2 key:  6 data: 3\n"
"   3 key:  8 data: 4\n"
"   4 key:  9 data: 8\n"
);
 }

void leaf_test_remove_element_at()
 {Leaf *t = leaf_test_load();
  leaf_removeElementAt(t, 2);
  //leaf_print_err(t);
  leaf_ok("removeElementAt", leaf_print(t),
"Leaf(maxSize:8, size:3)\n"
"   0 key:  2 data: 1\n"
"   1 key:  4 data: 2\n"
"   2 key:  8 data: 4\n"
);
 }

void leaf_test_first_last()
 {Leaf *t = leaf_test_load();
  Leaf_Result f = leaf_firstElement(t);
  Leaf_Result l = leaf_lastElement (t);
  //leaf_print_result_err(f);
  //leaf_print_result_err(l);
  leaf_check_result(f, 0,1,0,2,1);
  leaf_check_result(l, 0,1,3,8,4);
 }

void leaf_test_search()
 {Leaf *t = leaf_test_load();
  Leaf_Result s = leaf_search(t, 6);
  //leaf_print_result_err(s);
  leaf_check_result(s, 0,1,2,6,3);
 }

void leaf_test_search_first_greater_than_or_equal()
 {Leaf *t = leaf_test_load();
  Leaf_Result s = leaf_searchFirstGreaterThanOrEqual(t, 7);
  //leaf_print_result_err(s);
  leaf_check_result(s, 7,1,3,8,4);
 }

void leaf_test_search_first_greater_than_or_equal_except_last()
 {Leaf *t = leaf_test_load();
  Leaf_Result s = leaf_searchFirstGreaterThanOrEqualExceptLast(t, 7);
  //leaf_print_result_err(s);
  leaf_check_result(s, 7,0,0,0,0);
  Leaf_Result S = leaf_searchFirstGreaterThanOrEqualExceptLast(t, 5);
  //leaf_print_result_err(S);
  leaf_check_result(S, 5,1,2,6,3);
 }

void leaf_test_set_element_at()
 {Leaf *t = leaf_test_load();
  leaf_setElementAt(t, 22, 33, 2);
  //leaf_print_err(t);
  leaf_ok("setElementAt", leaf_print(t),
"Leaf(maxSize:8, size:4)\n"
"   0 key:  2 data: 1\n"
"   1 key:  4 data: 2\n"
"   2 key: 22 data:33\n"
"   3 key:  8 data: 4\n"
);
 }
#include <unistd.h>

int main()                                                                      // Tests
 {leaf_test_push();
  leaf_test_pop();
  leaf_test_shift();
  leaf_test_unshift();
  leaf_test_elementAt();
  leaf_test_insert_element_at();
  leaf_test_remove_element_at();
  leaf_test_first_last();
  leaf_test_search();
  leaf_test_search_first_greater_than_or_equal();
  leaf_test_search_first_greater_than_or_equal_except_last();
  leaf_test_set_element_at();

  if (1)
   {int p = leaf_tests_passed, f = leaf_tests_failed, n = p + f;

    if      (f == 0 && p > 0) {printf("Passed all %d tests\n", n);                        return 0;}
    else if (          f > 0) {printf("FAILed %d, passed %d tests out of %d\n", f, p, n); return f;}
    else                      {printf("No tests run\n");                                  return 1;}
   }
  _exit(0);
  return  0;
 }
