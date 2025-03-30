//------------------------------------------------------------------------------
// Stuck
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2025
//------------------------------------------------------------------------------
#include "stuck.h"

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
 } stuck_Result;

stuck_Result stuck_result()
 {stuck_Result r;
  r.search = r.found = r.index = r.key = r.data = 0;
  return r;
 }

stuck_Result stuck_pop(Stuck *s)
 {stuck_Result r = stuck_result();
  stuck_dec(s);
  r.index = stuck_size(s);
  r.key   = stuck_key (s, r.index);
  r.data  = stuck_data(s, r.index);
  return r;
 }

stuck_Result stuck_shift(Stuck *s)
 {stuck_Result r = stuck_result();
  r.key   = stuck_key (s, 0);
  r.data  = stuck_data(s, 0);
  for (int i = 0, j = stuck_size(s)-1; i < j; i++)
   {stuck_copyKeyData(s, i, i+1);
   }
  stuck_dec(s);
  return r;
 }

stuck_Result stuck_elementAt(Stuck *s, int Index)
 {stuck_Result r = stuck_result();
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

stuck_Result stuck_removeElementAt(Stuck *s, int Index)                         // Remove the indicated element
 {stuck_Result r = stuck_result();
  r.index = Index;
  r.key   = stuck_key (s, Index);
  r.data  = stuck_data(s, Index);
  for (int i = Index, j = stuck_size(s)-1; i < j; i++)                          // Shift the stuck down one place
   {stuck_copyKeyData(s, i, i+1);
   }
  stuck_dec(s);
  return r;
 }

stuck_Result stuck_firstElement(Stuck *s)
 {stuck_Result r = stuck_result();

  r.found = !stuck_isEmpty(s);
  if (r.found)
   {r.index = 0;
    r.key   = stuck_key (s, 0);
    r.data  = stuck_data(s, 0);
   }
  return r;
 }

stuck_Result stuck_lastElement(Stuck *s)
 {stuck_Result r = stuck_result();

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

stuck_Result stuck_search(Stuck *s, int Search)                                 // Search for an element within all elements of the stuck
 {stuck_Result r = stuck_result();
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

stuck_Result stuck_searchFirstGreaterThanOrEqual(Stuck *s, int Search)
 {stuck_Result r = stuck_result();
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

stuck_Result stuck_searchFirstGreaterThanOrEqualExceptLast(Stuck *s, int Search)
 {stuck_Result r = stuck_result();
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
