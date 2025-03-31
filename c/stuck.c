//------------------------------------------------------------------------------
// Stuck
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2025
//------------------------------------------------------------------------------
#define stuck_maxSize  20                                                       // The maximum number of entries in the stuck.
#define stuck_keyType  int                                                      // The type of a key
#define stuck_dataType int                                                      // The type of a data item in a stuck

typedef struct                                                                  // Definition of a stuck
 {int currentSize;                                                              // Current size of the stuck
  stuck_keyType  keys[stuck_maxSize];                                           // Keys
  stuck_dataType data[stuck_maxSize];                                           // Data
 } Stuck;

int stuck_size   (Stuck *s) {return s->currentSize;}                            // The current number of key elements in the stuck
int stuck_isFull (Stuck *s) {return stuck_size(s) > stuck_maxSize;}             // Check the stuck is full
int stuck_isEmpty(Stuck *s) {return stuck_size(s) == 0;}                        // Check the stuck is empty

//D1 Memory                                                                     // Actions on memory of stuck

stuck_keyType  stuck_key (Stuck *s, int Index)              {return s->keys[Index];}
stuck_dataType stuck_data(Stuck *s, int Index)              {return s->data[Index];}

void       stuck_setKey  (Stuck *s, int Index,  int Value)  {s->keys[Index] = Value;}
void       stuck_setData (Stuck *s, int Index,  int Value)  {s->data[Index] = Value;}
void       stuck_copyKey (Stuck *s, int Target, int Source) {s->keys[Target] = s->keys[Source];}
void       stuck_copyData(Stuck *s, int Target, int Source) {s->data[Target] = s->data[Source];}

void  stuck_setKeyData(Stuck *s, int Index, int Key, int Data)
 {stuck_setKey (s, Index, Key);
  stuck_setData(s, Index, Data);
 }

void stuck_copyKeyData(Stuck *s, int Target, int Source)
 {stuck_copyKey (s, Target, Source);
  stuck_copyData(s, Target, Source);
 }

//D1 Actions                                                                    // Place and remove data to/from stuck

void stuck_inc  (Stuck *s) {s->currentSize++;}                                  // Increment the current size
void stuck_dec  (Stuck *s) {s->currentSize--;}                                  // Decrement the current size
void stuck_clear(Stuck *s) {s->currentSize = 0;}                                // Clear the stuck

void stuck_push (Stuck *s, int key, int data)                                   // Push an element onto the stuck
 {int n = stuck_size(s);
  stuck_setKeyData(s, n, key, data);
  stuck_inc(s);
 }

void stuck_unshift(Stuck *s, int key, int data)                                 // Unshift an element onto the stuck
 {for (int i = stuck_size(s); i > 0; --i)                                       // Shift the stuck up one place
   {stuck_copyKeyData(s, i, i-1);
   }
  stuck_setKeyData(s, 0, key, data);
  stuck_inc(s);
 }

typedef struct
 {int          search;                                                          // Search key
  int           found;                                                          // Whether a matcbing element was found
  int           index;                                                          // The index from which the key, data pair were retrieved
  stuck_keyType   key;                                                          // The retrieved key
  stuck_dataType data;                                                          // The retrieved data
 } Stuck_Result;

Stuck_Result stuck_result()
 {Stuck_Result r;
  r.search = r.found = r.index = r.key = r.data = 0;
  return r;
 }

Stuck_Result stuck_pop(Stuck *s)
 {Stuck_Result r = stuck_result();
  stuck_dec(s);
  r.index = stuck_size(s);
  r.key   = stuck_key (s, r.index);
  r.data  = stuck_data(s, r.index);
  return r;
 }

Stuck_Result stuck_shift(Stuck *s)
 {Stuck_Result r = stuck_result();
  r.key   = stuck_key (s, 0);
  r.data  = stuck_data(s, 0);
  for (int i = 0, j = stuck_size(s)-1; i < j; i++)
   {stuck_copyKeyData(s, i, i+1);
   }
  stuck_dec(s);
  return r;
 }

Stuck_Result stuck_elementAt(Stuck *s, int Index)
 {Stuck_Result r = stuck_result();
  r.index = Index;
  r.key   = stuck_key (s, Index);
  r.data  = stuck_data(s, Index);
  return r;
 }

void stuck_setElementAt(Stuck *s, int key, int data, int Index)                 // Set an element either in range or one above the current range
 {if (Index == stuck_size(s))                                                   // Extended range
   {stuck_setKeyData(s, Index, key, data); stuck_inc(s);
   }
  else                                                                          // In range
   {stuck_setKeyData(s, Index, key, data);
   }
 }

void stuck_insertElementAt(Stuck *s, int key, int data, int Index)              // Insert an element at the indicated location shifting all the remaining elements up one
 {for (int i = stuck_size(s); i > Index; --i)
   {stuck_copyKeyData(s, i, i-1);
   }
  stuck_setKeyData(s, Index, key, data);
  stuck_inc(s);
 }

Stuck_Result stuck_removeElementAt(Stuck *s, int Index)                         // Remove the indicated element
 {Stuck_Result r = stuck_result();
  r.index = Index;
  r.key   = stuck_key (s, Index);
  r.data  = stuck_data(s, Index);
  for (int i = Index, j = stuck_size(s)-1; i < j; i++)                          // Shift the stuck down one place
   {stuck_copyKeyData(s, i, i+1);
   }
  stuck_dec(s);
  return r;
 }

Stuck_Result stuck_firstElement(Stuck *s)
 {Stuck_Result r = stuck_result();

  r.found = !stuck_isEmpty(s);
  if (r.found)
   {r.index = 0;
    r.key   = stuck_key (s, 0);
    r.data  = stuck_data(s, 0);
   }
  return r;
 }

Stuck_Result stuck_lastElement(Stuck *s)
 {Stuck_Result r = stuck_result();

  r.found = !stuck_isEmpty(s);
  int i = stuck_size(s)-1;
  if (r.found)
   {r.index = i;
    r.key   = stuck_key (s, i);
    r.data  = stuck_data(s, i);
   }
  return r;
 }

//D1 Search                                                                     // Search a stuck.

Stuck_Result stuck_search(Stuck *s, int Search)                                 // Search for an element within all elements of the stuck
 {Stuck_Result r = stuck_result();
  r.key       = Search;

  for (int i = 0, j = stuck_size(s); i < j; i++)                                // Search
   {if (stuck_key(s, i) == Search)
     {r.found = 1;
      r.index = i;
      r.data  = stuck_data(s, i);
      return r;
     }
   }
  r.found = 0;
  return r;
 }

Stuck_Result stuck_searchFirstGreaterThanOrEqual(Stuck *s, int Search)
 {Stuck_Result r = stuck_result();
  r.search = Search;
  for (int i = 0, j = stuck_size(s); i < j; i++)
   {if (stuck_key(s, i) >= Search)
     {r.found = 1;
      r.index = i;
      r.key = stuck_key(s, i);
      r.data = stuck_data(s, i);
      return r;
     }
   }
  r.found = 0;
  return r;
 }

Stuck_Result stuck_searchFirstGreaterThanOrEqualExceptLast(Stuck *s, int Search)
 {Stuck_Result r = stuck_result();
  r.search = Search;
  for (int i = 0, j = stuck_size(s)-1; i < j; i++)
   {if (stuck_key(s, i) >= Search)
     {r.found = 1;
      r.index = i;
      r.key = stuck_key(s, i);
      r.data = stuck_data(s, i);
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

int stuck_tests_passed = 0;
int stuck_tests_failed = 0;

//D1 Print                                                                      // Print a stuck

char *stuck_print(Stuck *s)                                                     // Print a stuck
 {char *C = malloc(4096), *c = C;
  int N = stuck_size(s);
  c += sprintf(c, "Stuck(maxSize:%d, size:%d)\n", stuck_maxSize, N);
  for (int i = 0; i < N; i++)                                                   // Search
   {c += sprintf(c, "  %2d key: %2d data:%2d\n", i, stuck_key(s, i), stuck_data(s, i));
   }
  *c = 0;
  return C;
 }

void stuck_print_err(Stuck *s)                                                  // Print a stuck on stderr
 {fprintf(stderr, "%s", stuck_print(s));
 }

char *stuck_print_result(Stuck_Result r)                                        // Print the result of a stuck operation
 {char *C = malloc(4096), *c = C;
  c += sprintf(c, "search: %d\n", r.search);
  c += sprintf(c, " found: %d\n", r.found);
  c += sprintf(c, " index: %d\n", r.index);
  c += sprintf(c, "   key: %d\n", r.key);
  c += sprintf(c, "  data: %d\n", r.data);
  *c = 0;
  return C;
 }

void stuck_print_result_err(Stuck_Result r)                                     // Print the result of a stuck operation
 {fprintf(stderr, "%s", stuck_print_result(r));
 }

//D1 Tests                                                                      // Testing

void stuck_ok(char *name, char *g, char *e)                                     // Test got versus expected
 {int c = strcmp(g, e);
  if (c == 0)
   {++stuck_tests_passed;
    return;
   }
  ++stuck_tests_failed;
  printf("Test: %s failed\n", name);
 }

void stuck_check_result_field(char *format, int got, int expected)
 {if (expected >= 0 && got != expected)
   {stuck_tests_failed++;
    printf(format, got, expected);
   }
  else
   {stuck_tests_passed++;
   }
 }

void stuck_check_result(Stuck_Result r , int Search, int Found, int Index, int Key, int Data)  // Check a result
 {stuck_check_result_field("Search got %2d, expected %2d\n", r.search, Search);
  stuck_check_result_field("Found  got %2d, expected %2d\n", r.found,  Found);
  stuck_check_result_field("Index  got %2d, expected %2d\n", r.index,  Index);
  stuck_check_result_field("Key    got %2d, expected %2d\n", r.key,    Key);
  stuck_check_result_field("Data   got %2d, expected %2d\n", r.data,   Data);
 }

//D0 Tests                                                                      // Test stuck

Stuck *stuck_test_load()
 {Stuck *s = calloc(sizeof(Stuck), 1);

  stuck_push(s, 2, 1);
  stuck_push(s, 4, 2);
  stuck_push(s, 6, 3);
  stuck_push(s, 8, 4);

  return s;
 }

void stuck_test_push()
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

void stuck_test_pop()
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

void stuck_test_shift()
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

void stuck_test_unshift()
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

void stuck_test_elementAt()
 {Stuck *t = stuck_test_load();
  Stuck_Result r  = stuck_elementAt(t, 2);
  //stuck_print_result_err(r);
  stuck_check_result(r, 0,0,2,6,3);
 }

void stuck_test_insert_element_at()
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

void stuck_test_remove_element_at()
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

void stuck_test_first_last()
 {Stuck *t = stuck_test_load();
  Stuck_Result f = stuck_firstElement(t);
  Stuck_Result l = stuck_lastElement (t);
  //stuck_print_result_err(f);
  //stuck_print_result_err(l);
  stuck_check_result(f, 0,1,0,2,1);
  stuck_check_result(l, 0,1,3,8,4);
 }

void stuck_test_search()
 {Stuck *t = stuck_test_load();
  Stuck_Result s = stuck_search(t, 6);
  //stuck_print_result_err(s);
  stuck_check_result(s, 0,1,2,6,3);
 }

void stuck_test_search_first_greater_than_or_equal()
 {Stuck *t = stuck_test_load();
  Stuck_Result s = stuck_searchFirstGreaterThanOrEqual(t, 7);
  //stuck_print_result_err(s);
  stuck_check_result(s, 7,1,3,8,4);
 }

void stuck_test_search_first_greater_than_or_equal_except_last()
 {Stuck *t = stuck_test_load();
  Stuck_Result s = stuck_searchFirstGreaterThanOrEqualExceptLast(t, 7);
  //stuck_print_result_err(s);
  stuck_check_result(s, 7,0,0,0,0);
  Stuck_Result S = stuck_searchFirstGreaterThanOrEqualExceptLast(t, 5);
  //stuck_print_result_err(S);
  stuck_check_result(S, 5,1,2,6,3);
 }

void stuck_test_set_element_at()
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
#include <unistd.h>

int main()                                                                      // Tests
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
  _exit(0);
  return  0;
 }
