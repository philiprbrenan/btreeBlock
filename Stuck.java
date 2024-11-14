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

  int      size          ()       {return currentSize;}                         // The current number of key elements in the stuck as a binary integer
  int   maxSize          ()       {return maxSize;}                             // The maximum number of key elements the stuck can contain
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
   {final int index;
    final int key;                                                              // The popped key
    final int data;                                                             // The popped data
    Pop()
     {assertNotEmpty();
      dec();
      index = size();
      key  = Keys[index];
      data = Data[index];
     }
    public String toString()                                                    // Print
     {final StringBuilder s = new StringBuilder();
      s.append("Pop(index:"+index);
      s.append(" key:"+key);
      s.append(" data:"+data);
      s.append(")\n");
      return s.toString();
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
    public String toString()                                                    // Print
     {final StringBuilder s = new StringBuilder();
      s.append("Shift(key:"+key);
      s.append(" data:"+data);
      s.append(")\n");
      return s.toString();
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
    public String toString()                                                    // Print
     {final StringBuilder s = new StringBuilder();
      s.append("ElementAt(index:"+index);
      s.append(" key:"+key);
      s.append(" data:"+data);
      s.append(")\n");
      return s.toString();
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
    public String toString()                                                    // Print
     {final StringBuilder s = new StringBuilder();
      s.append("RemoveElementAt(index:"+index);
      s.append(" key:"+key);
      s.append(" data:"+data);
      s.append(")\n");
      return s.toString();
     }
   }
  RemoveElementAt removeElementAt(int Index) {return new RemoveElementAt(Index);} // Remove the indicated element

  class FirstElement                                                            // First element
   {final boolean found;                                                        // Whether there was a first element
    final int key;                                                              // The shifted key
    final int data;                                                             // The shifted data
    FirstElement()
     {found = !isEmpty();
      if (found)
       {key  = Keys[0];
        data = Data[0];
       }
      else key = data = 0;
     }
    public String toString()                                                    // Print
     {final StringBuilder s = new StringBuilder();
      s.append("FirstElement(found:"+found);
      s.append(" key:"+key);
      s.append(" data:"+data);
      s.append(")\n");
      return s.toString();
     }
   }
  FirstElement firstElement() {return new FirstElement();}                      // First element

  class LastElement                                                             // Last element
   {final boolean found;                                                        // Whether there was a last element
    final int key;                                                              // The shifted key
    final int data;                                                             // The shifted data
    LastElement()
     {found = !isEmpty();
      final int s = size()-1;
      if (found)
       {key  = Keys[s];
        data = Data[s];
       }
      else key = data = 0;
     }
    public String toString()                                                    // Print
     {final StringBuilder s = new StringBuilder();
      s.append("LastElement(found:"+found);
      s.append(" key:"+key);
      s.append(" data:"+data);
      s.append(")\n");
      return s.toString();
     }
   }
  LastElement lastElement() {return new LastElement();}                         // Last element

//D1 Search                                                                     // Search a stuck.

  class Search                                                                  // Search for an element
   {final int key;                                                              // Search key
    final boolean found;                                                        // Whether the key was found
    final int index ;                                                           // Index of the located key if any
    final int data;                                                             // Located data if key was found
    Search(int Search)
     {boolean looking = true;
      key = Search;
      int i = 0, j = size() - 1;
      for (; i < j && looking; i++)                                             // Search
       {if (Keys[i] == key)
         {looking = false; break;
         }
       }
      if (looking)
       {found = false; index = 0; data = 0;
       }
      else
       {found = true; index = i; data = Data[i];
       }
     }
    public String toString()                                                    // Print
     {final StringBuilder s = new StringBuilder();
      s.append("Search(Search:"+key);
      s.append(" found:"+found);
      if (found)
       {s.append(" index:"+index);
        s.append(" data:"+data);
       }
      s.append(")\n");
      return s.toString();
     }
   }
  Search search(int Search) {return new Search(Search);}                        // Search for a key

  class SearchFirstGreaterThanOrEqual                                           // Search for the first key that is greater than or equal
   {final int key;                                                              // Search key
    final boolean found;                                                        // Whether the key was found
    final int index ;                                                           // Index of the located key if any
    final int data;                                                             // Located data if key was found
    SearchFirstGreaterThanOrEqual(int Search)
     {boolean looking = true;
      key = Search;
      int i = 0, j = size() - 1;
      for (; i < j && looking; i++)                                             // Search
       {if (Keys[i] <= key)                                                     // Search key is equal or greater to the current key
         {looking = false; break;
         }
       }
      if (looking)
       {found = false; index = 0; data = 0;
       }
      else
       {found = true; index = i; data = Data[i];
       }
     }
    public String toString()                                                    // Print
     {final StringBuilder s = new StringBuilder();
      s.append("SearchFirstGreaterThanOrEqual(Search:"+key);
      s.append(" found:"+found);
      if (found)
       {s.append(" index:"+index);
        s.append(" data:"+data);
       }
      s.append(")\n");
      return s.toString();
     }
   }
  SearchFirstGreaterThanOrEqual                                                 // Search for a key
  searchFirstGreaterThanOrEqual(int Search)
   {return new SearchFirstGreaterThanOrEqual(Search);
   }

//D1 Print                                                                      // Print a stuck

  public String toString()
   {final StringBuilder s = new StringBuilder();                                //
    s.append("Stuck(maxSize:"+maxSize());
    s.append(" size:"+size()+")\n");
    for (int i = 0, j = size(); i < j; i++)                                     // Search
     {s.append("  "+i+" key:"+Keys[i]+" data:"+Data[i]+"\n");
     }
    return s.toString();
   }

//D0 Tests                                                                      // Test stuck

  static Stuck test_load()
   {Stuck t = stuck(8);
    t.push(2, 1); t.push(4, 2); t.push(6, 3); t.push(8, 4);
    //stop(t);
    t.ok("""
Stuck(maxSize:8 size:4)
  0 key:2 data:1
  1 key:4 data:2
  2 key:6 data:3
  3 key:8 data:4
""");
    return t;
   }

  static void test_push()
   {Stuck t = stuck(4);
    t.push(1, 2); t.push(2, 4);
    //stop(t);
    t.ok("""
Stuck(maxSize:4 size:2)
  0 key:1 data:2
  1 key:2 data:4
""");
   }

  static void test_pop()
   {Stuck t = test_load();
    Pop p = t.pop();
    //stop(p);
    ok(p, """
Pop(index:3 key:8 data:4)
""");
    //stop(t);
    t.ok("""
Stuck(maxSize:8 size:3)
  0 key:2 data:1
  1 key:4 data:2
  2 key:6 data:3
""");
   }

  static void test_shift()
   {Stuck t = test_load();
    Shift s = t.shift();
    //stop(s);
    ok(s, """
Shift(key:2 data:1)
""");
    //stop(t);
    t.ok("""
Stuck(maxSize:8 size:3)
  0 key:4 data:2
  1 key:6 data:3
  2 key:8 data:4
""");
   }

  static void test_unshift()
   {Stuck t = test_load();
    t.unshift(9, 9);
    //stop(t);
    t.ok("""
Stuck(maxSize:8 size:5)
  0 key:9 data:9
  1 key:2 data:1
  2 key:4 data:2
  3 key:6 data:3
  4 key:8 data:4
""");
   }

  static void test_elementAt()
   {Stuck     t = test_load();
    ElementAt e = t.elementAt(2);
    //stop(e);
    ok(e, """
ElementAt(index:2 key:6 data:3)
""");
   }

  static void test_insert_element_at()
   {Stuck t = test_load();
    t.insertElementAt(9, 9, 2);
    //stop(t);
    t.ok("""
Stuck(maxSize:8 size:4)
  0 key:2 data:1
  1 key:4 data:2
  2 key:9 data:9
  3 key:6 data:3
""");
    t.insertElementAt(9, 9, 4);
    //stop(t);
    t.ok("""
Stuck(maxSize:8 size:5)
  0 key:2 data:1
  1 key:4 data:2
  2 key:9 data:9
  3 key:6 data:3
  4 key:9 data:9
""");
   }

  static void test_remove_element_at()
   {Stuck t = test_load();
    RemoveElementAt a = t.removeElementAt(2);
    //stop(a);
    ok(a, """
RemoveElementAt(index:2 key:6 data:3)
""");
    //stop(t);
    t.ok("""
Stuck(maxSize:8 size:3)
  0 key:2 data:1
  1 key:4 data:2
  2 key:8 data:4
""");
   }

  static void test_first_last()
   {Stuck t = test_load();
    FirstElement f = t.firstElement();
    //stop(f);
    ok(f, """
FirstElement(found:true key:2 data:1)
""");
    LastElement l = t.lastElement();
    //stop(l);
    ok(l, """
LastElement(found:true key:8 data:4)
""");
   }

  static void test_search()
   {Stuck  t = test_load();
    Search s = t.search(2);
    //stop(s);
    ok(s, """
Search(Search:2 found:true index:0 data:1)
""");
   }

  static void test_set_element_at()
   {Stuck  t = test_load();
    t.setElementAt(22, 33, 2);
    //stop(t);
    t.ok("""
Stuck(maxSize:8 size:4)
  0 key:2 data:1
  1 key:4 data:2
  2 key:22 data:33
  3 key:8 data:4
""");
    t.setElementAt(99, 97, 4);
    //stop(t);
    t.ok("""
Stuck(maxSize:8 size:5)
  0 key:2 data:1
  1 key:4 data:2
  2 key:22 data:33
  3 key:8 data:4
  4 key:99 data:97
""");
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
    test_search();
    test_set_element_at();
   }

  static void newTests()                                                        // Tests being worked on
   {oldTests();
   }

  public static void main(String[] args)                                        // Test if called as a program
   {try                                                                         // Get a traceback in a format clickable in Geany if something goes wrong to speed up debugging.
     {if (github_actions) oldTests(); else newTests();                          // Tests to run
      //if (github_actions)                                                       // Coverage analysis
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
