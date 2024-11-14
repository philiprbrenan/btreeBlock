//------------------------------------------------------------------------------
// A fixed size stack of ordered key, data pairs with null deemed highest
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout  a binary tree on a silicon chip.

import java.util.*;

class Stuck extends Test                                                        // A fixed size stack of ordered key, data pairs with null deemed highest
 {final int maxSize;                                                            // The maximum number of entries in the stuck.
  final int[]Keys;                                                              // The keys on the stuck
  final int[]Data;                                                              // The data on the stuck
  int currentSize;                                                              // The current size of the stuck

//D1 Construction                                                               // Create a stuck

  Stuck(int MaxSize)                                                            // Create the stuck with a maximum number of the specified elements
   {maxSize     = MaxSize;
    currentSize = 0;
    Keys        = new int[maxSize];
    Data        = new int[maxSize];
   }

  static Stuck stuck(int MaxSize)                                               // Create the stuck
   {return new Stuck(MaxSize);
   }

  public void ok(String expected) {ok(toString(), expected);}                   // Check the stuck

//D1 Characteristics                                                            // Characteristics of the stuck

  int      size          ()       {return currentSize;}                         // The current number of elements in the stuck as a binary integer
  boolean isFull         ()       {return size() > maxSize;}                    // Check the stuck is full
  boolean isEmpty        ()       {return size() == 0;}                         // Check the stuck is empty
  void     assertNotFull ()       {if (isFull ()) stop("Full");}                // Assert the stack is not full
  void     assertNotEmpty()       {if (isEmpty()) stop("Empty");}               // Assert the stack is not empty
  void     assertIn      (int i)  {if (i < 0 || i >= size()) stop("Out of normal range",   i, "for size", size());}  // Check that the index would yield a valid element
  void     assertIn1     (int i)  {if (i < 0 || i >  size()) stop("Out of extended range", i, "for size", size());}  // Check that the index would allow an element to be inserted

//D1 Actions                                                                    // Place and remove data to/from stuck

  void inc  () {assertNotFull (); currentSize++;}                               // Increment the current size
  void dec  () {assertNotEmpty(); currentSize--;}                               // Decrement the current size
  void clear() {                  currentSize = 0;}                             // Clear the stuck

  void push(int key, int data)                                                  // Push an element onto the stuck
   {assertNotFull();
    Keys[currentSize] = key;
    Data[currentSize] = data;
    inc();
   }

  void unshift(int key, int data)                                               // Unshift an element onto the stuck
   {assertNotFull();
    for (int i = size(); i > 0; --i)                                            // Shift the stuck up one place
     {Keys[i] = Keys[i-1];
      Data[i] = Data[i-1];
     }
    Keys[0] = key;
    Data[0] = data;
    inc();
   }

  class Pop                                                                     // Pop a key, data pair from the stuck
   {final int key;                                                              // The popped key
    final int data;                                                             // The popped data
    Pop()
     {assertNotEmpty();
      dec();
      final int s = size();
      key  = Keys[s];
      data = Data[s];
     }
   }
  Pop pop() {return new Pop();}

  class Shift                                                                   // Shift a key, data pair from the stuck
   {final int key;                                                              // The shifted key
    final int data;                                                             // The shifted data
    Shift()
     {assertNotEmpty();
      key  = Keys[0];
      data = Data[0];

      for (int i = 0, j = size()-1; i < j; i++)                                 // Shift the stuck down one place
       {Keys[i] = Keys[i+1];
        Data[i] = Data[i+1];
       }
      dec();
     }
   }
  Shift shift() {return new Shift();}

  class ElementAt                                                               // Gte the indexed element
   {final int index;                                                            // The index from which the key, data pair were retrieved
    final int key;                                                              // The shifted key
    final int data;                                                             // The shifted data
    ElementAt(int Index)
     {index = Index;
      assertIn(index);
      key  = Keys[index];
      data = Data[index];
     }
   }
  ElementAt elementAt(int Index) {return new ElementAt(Index);}

  void setElementAt(int key, int data, int Index)                                    // Set an element either in range or one above the current range
   {if (Index == size())                                                        // Extended range
     {Keys[Index] = key;
      Data[Index] = data;
      inc();
     }
    else                                                                        // In range
     {assertIn(Index);
      Keys[Index] = key;
      Data[Index] = data;
      }
   }

  void insertElementAt(int key, int data, int Index)                                 // Insert an element at the indicated location shifting all the remaining elements up one
   {if (Index == size())                                                        // Extended range
     {Keys[Index] = key;
      Data[Index] = data;
      inc();
     }
    else                                                                        // In range
     {assertIn(Index);
      for (int i = size(); i > Index; --i)                                      // Shift the stuck up one place
       {Keys[i] = Keys[i-1];
        Data[i] = Data[i-1];
       }
      Keys[Index] = key;
      Data[Index] = data;
     }
   }

  class RemoveElementAt                                                         // Remove the indicated element
   {final int index;                                                            // The index from which the key, data pair were retrieved
    final int key;                                                              // The shifted key
    final int data;                                                             // The shifted data
    RemoveElementAt(int Index)
     {index = Index;
      assertIn(index);
      key  = Keys[index];
      data = Data[index];
      for (int i = index, j = size()-1; i < j; i++)                             // Shift the stuck down one place
       {Keys[i] = Keys[i+1];
        Data[i] = Data[i+1];
       }
      dec();
     }
   }
  RemoveElementAt removeElementAt(int Index) {return new RemoveElementAt(Index);} // Remove the indicated element

  class FirstElement                                                            // First element
   {final int key;                                                              // The shifted key
    final int data;                                                             // The shifted data
    FirstElement()
     {assertNotEmpty();
      key  = Keys[0];
      data = Data[0];
     }
   }
  FirstElement firstElement() {return new FirstElement();}                      // First element

  class LastElement                                                             // Last element
   {final int key;                                                              // The shifted key
    final int data;                                                             // The shifted data
    LastElement()
     {assertNotEmpty();
      final int index = size()-1;
      key  = Keys[index];
      data = Data[index];
     }
   }
  LastElement lastElement() {return new LastElement();}                         // Last element

//D1 Search                                                                     // Search a stuck.

  class Search                                                                  // Search for an element
   {final boolean found;                                                        // Whether the key was found
    final int index ;                                                           // Index of the located key if any
    final int key;                                                              // Search key
    final int data;                                                             // Located data if key was found
    Search(int Search)
     {boolean looking = true;
      key = Search;
      int i = 0, j = size() - 1;
      for (; i < j && looking; i++)                                             // Search
       {if (Keys[i] == key) break;
       }
      if (looking)
       {found = false; index = 0; data = 0;
       }
      else
       {found = true; index = i; data = Data[i];
       }
     }
   }
  Search search(int Search) {return new Search(Search);}                        // Search for a key

  class SearchFirstGreaterThanOrEqual                                           // Search for the first key that is greater than or equal
   {final boolean found;                                                        // Whether the key was found
    final int index ;                                                           // Index of the located key if any
    final int key;                                                              // Search key
    final int data;                                                             // Located data if key was found
    SearchFirstGreaterThanOrEqual(int Search)
     {boolean looking = true;
      key = Search;
      int i = 0, j = size() - 1;
      for (; i < j && looking; i++)                                             // Search
       {if (Keys[i] <= key) break;                                              // Search key is equal or greaer to the current key
       }
      if (looking)
       {found = false; index = 0; data = 0;
       }
      else
       {found = true; index = i; data = Data[i];
       }
     }
   }
  SearchFirstGreaterThanOrEqual                                                 // Search for a key
  searchFirstGreaterThanOrEqual(int Search)
   {return new SearchFirstGreaterThanOrEqual(Search);
   }

//D1 Print                                                                      // Print a stuck



//D0 Tests                                                                      // Test stuck

  static void test_push()
   {
   }
  static void test_pop()
   {
   }

  static void test_shift()
   {
   }

  static void test_unshift()
   {
   }

  static void test_elementAt()
   {
   }

  static void test_insert_element_at()
   {
   }

  static void test_remove_element_at()
   {
   }

  static void test_first_last()
   {
   }

  static void test_index_of()
   {
   }

  static void test_set_element_at()
   {
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_push();
    test_pop();
    test_shift();
    test_unshift();
    test_elementAt();
    test_insert_element_at();
    test_remove_element_at();
    test_first_last();
    test_index_of();
    test_set_element_at();
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
