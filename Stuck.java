//------------------------------------------------------------------------------
// A fixed size stack of ordered key, data pairs
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout  a binary tree on a silicon chip.
// Reference the keys and data through a small mapping table and free stack so that we can manipulate keys and data by a small index rather than copying them directly.
abstract class Stuck extends Test                                               // A fixed size stack of ordered key, data pairs with null deemed highest
 {abstract int maxSize();                                                       // The maximum number of entries in the stuck.
  final int[]Keys;                                                              // The keys on the stuck
  final int[]Data;                                                              // The data on the stuck
  int currentSize;                                                              // The current size of the stuck

//D1 Construction                                                               // Create a stuck

  Stuck()                                                                       // Create the stuck with a maximum number of the specified elements
   {zz();
    currentSize = 0;
    Keys        = new int[maxSize()];
    Data        = new int[maxSize()];
   }

  static Stuck stuck(final int MaxSize)                                         // Create the stuck
   {z(); return new Stuck() {int maxSize() {return MaxSize;}};
   }

  public void ok(String expected) {ok(toString(), expected);}                   // Check the stuck

//D1 Characteristics                                                            // Characteristics of the stuck

  int      size            ()       {z(); return currentSize;}                  // The current number of key elements in the stuck as a binary integer
  boolean isFull           ()       {z(); return size() > maxSize();}           // Check the stuck is full
  boolean isEmpty          ()       {z(); return size() == 0;}                  // Check the stuck is empty
  void     assertNotFull   ()       {z(); if (isFull ()) stop("Full");}         // Assert the stack is not full
  void     assertNotEmpty  ()       {z(); if (isEmpty()) stop("Empty");}        // Assert the stack is not empty
  void     assertInNormal  (int i)  {z(); if (i < 0 || i >= size()) stop("Out of normal range",   i, "for size", size());}  // Check that the index would yield a valid element
  void     assertInExtended(int i)  {z(); if (i < 0 || i >  size()) stop("Out of extended range", i, "for size", size());}  // Check that the index would yield a valid element

  class CheckOrder extends Limit                                                // Check that all the keys are in order,
   {final boolean inOrder;                                                      // Keys are in order
    final int outOfOrder;                                                       // Index of first key out of order
    String name() {return "CheckOrder";}                                        // Name of limit

    CheckOrder()                                                                // Check that all the keys are in order,
     {boolean looking = true;
      int i = 1, j = limit();
      for (;i < j && looking; i++)                                              // Check each key
       {z();
        if (key(i-1) > key(i))
         {looking = false;
           break;
         }
       }
      if (looking)
       {z(); inOrder = true;  outOfOrder = 0;
       }
      else
       {z(); inOrder = false; outOfOrder = i;
       }
     }
    public String toString()                                                    // Print
     {z();
      final StringBuilder s = new StringBuilder();
      s.append(name()+"(inOrder:"+inOrder);
      s.append(" outOfOrder:"+outOfOrder);
      s.append(")\n");
      return s.toString();
     }
   }
  CheckOrder checkOrder() {z(); return new CheckOrder();}                       // Check the order of elements

  class CheckOrderExceptLast extends CheckOrder                                 // Check that the keys are in order except for the last one
   {String name() {return "CheckOrderExceptLast";}                              // Name of class
    int limit  () {return size()-1;}                                            // How far to check
   }
  CheckOrderExceptLast checkOrderExceptLast() {z(); return new CheckOrderExceptLast();}

//D1 Memory                                                                     // Actions on memory of stuck

  int  key     (int Index)              {return Keys[Index];}
  int  data    (int Index)              {return Data[Index];}
  void setKey  (int Index,  int Value)  {       Keys[Index] = Value;}
  void setData (int Index,  int Value)  {       Data[Index] = Value;}
  void copyKey (int Target, int Source) {setKey (Target, key (Source));}
  void copyData(int Target, int Source) {setData(Target, data(Source));}

  void  setKeyData(int Index, int Key, int Data)
   {setKey (Index, Key);
    setData(Index, Data);
   }

  void copyKeyData(int Target, int Source)
   {copyKey (Target, Source);
    copyData(Target, Source);
   }

//D1 Actions                                                                    // Place and remove data to/from stuck

  void inc  () {z(); assertNotFull (); currentSize++;}                          // Increment the current size
  void dec  () {z(); assertNotEmpty(); currentSize--;}                          // Decrement the current size
  void clear() {z();                   currentSize = 0;}                        // Clear the stuck

  void push(int key, int data)                                                  // Push an element onto the stuck
   {z();
    assertNotFull();
    setKeyData(currentSize, key, data);
    inc();
   }

  void unshift(int key, int data)                                               // Unshift an element onto the stuck
   {z();
    assertNotFull();
    for (int i = size(); i > 0; --i)                                            // Shift the stuck up one place
     {z();
      copyKeyData(i, i-1);
     }
    setKeyData(0, key, data);
    inc();
   }

  class Pop                                                                     // Pop a key, data pair from the stuck
   {final int index;                                                            // The index of the popped element
    final int key;                                                              // The popped key
    final int data;                                                             // The popped data
    Pop()
     {z();
      assertNotEmpty();
      dec();
      index = size();
      key   = key (index);
      data  = data(index);
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
     {z();
      assertNotEmpty();
      key   = key (0);
      data  = data(0);

      for (int i = 0, j = size()-1; i < j; i++)                                 // Shift the stuck down one place
       {z();
        copyKeyData(i, i+1);
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
  Shift shift() {z(); return new Shift();}

  class ElementAt                                                               // Get the indexed element
   {final int index;                                                            // The index from which the key, data pair were retrieved
    final int key;                                                              // The shifted key
    final int data;                                                             // The shifted data
    ElementAt(int Index)
     {z();
      index = Index;
      assertInNormal(index);
      key   = key (index);
      data  = data(index);
     }
    public String toString()                                                    // Print
     {z();
      final StringBuilder s = new StringBuilder();
      s.append("ElementAt(index:"+index);
      s.append(" key:"+key);
      s.append(" data:"+data);
      s.append(")\n");
      return s.toString();
     }
   }
  ElementAt elementAt(int Index) {z(); return new ElementAt(Index);}

  void setElementAt(int key, int data, int Index)                               // Set an element either in range or one above the current range
   {if (Index == size())                                                        // Extended range
     {z();
      setKeyData(Index, key, data);
      inc();
     }
    else                                                                        // In range
     {z();
      assertInNormal(Index);
      setKeyData(Index, key, data);
      }
   }

  void insertElementAt(int key, int data, int Index)                            // Insert an element at the indicated location shifting all the remaining elements up one
   {z();
    assertInExtended(Index);
    for (int i = size(); i > Index; --i)                                        // Shift the stuck up one place
     {z();
      copyKeyData(i, i-1);
     }
    setKeyData(Index, key, data);
    inc();
   }

  class RemoveElementAt                                                         // Remove the indicated element
   {final int index;                                                            // The index from which the key, data pair were retrieved
    final int key;                                                              // The shifted key
    final int data;                                                             // The shifted data
    RemoveElementAt(int Index)
     {z();
      index = Index;
      assertInNormal(index);
      key  = key (index);
      data = data(index);
      for (int i = index, j = size()-1; i < j; i++)                             // Shift the stuck down one place
       {z();
        copyKeyData(i, i+1);
       }
      dec();
     }
    public String toString()                                                    // Print
     {z();
      final StringBuilder s = new StringBuilder();
      s.append("RemoveElementAt(index:"+index);
      s.append(" key:"+key);
      s.append(" data:"+data);
      s.append(")\n");
      return s.toString();
     }
   }
  RemoveElementAt removeElementAt(int Index) {z(); return new RemoveElementAt(Index);} // Remove the indicated element

  class FirstElement                                                            // First element
   {final boolean found;                                                        // Whether there was a first element
    final int key;                                                              // The shifted key
    final int data;                                                             // The shifted data
    FirstElement()
     {z();
      found = !isEmpty();
      if (found)
       {z();
        key  = key (0);
        data = data(0);
       }
      else {z(); key = data = 0;}
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
     {z();
      found = !isEmpty();
      final int s = size()-1;
      if (found)
       {z();
        key  = key (s);
        data = data(s);
       }
      else {z(); key = data = 0;}
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
  LastElement lastElement() {z(); return new LastElement();}                    // Last element

//D1 Search                                                                     // Search a stuck.

  class Limit                                                                   // Search all of the stuck
   {String name() {return "Limit";}                                             // Name of limit
    int   limit() {return size();}                                              // How far to check
   }

  class Search extends Limit                                                    // Search for an element within all elements of the stuck
   {final int key;                                                              // Search key
    final boolean found;                                                        // Whether the key was found
    final int index ;                                                           // Index of the located key if any
    final int data;                                                             // Located data if key was found
    String name() {return "Search";}                                            // Name of the search

    Search(int Search)                                                          // Search for an element within all elements of the stuck
     {z();
      boolean looking = true;
      key = Search;
      int i = 0, j = limit();
      for (; i < j && looking; i++)                                             // Search
       {z();
        if (key(i) == key)
         {z();
          looking = false; break;
         }
       }
      if (looking)                                                              // Search key is bigger than all keys in the stuck
       {z(); found = false; index = 0; data = 0;
       }
      else                                                                      // Found a greater than or equal key in the stuck
       {z(); found = true;  index = i; data = data(i);
       }
     }
    public String toString()                                                    // Print
     {z();
      final StringBuilder s = new StringBuilder();
      s.append(name()+"(Search:"+key);
      s.append(" found:"+found);
      if (found)
       {z();
        s.append(" index:"+index);
        s.append(" data:"+data);
       }
      s.append(")\n");
      return s.toString();
     }
   }
  Search search(int Search) {return new Search(Search);}                        // Search for a key ignoring the last element on the stuck

  class SearchExceptLast extends Search                                         // Search for an element ignoring the last element on the stuck
   {SearchExceptLast(int Search) {super(Search);}
    int   limit() {return size()-1;}                                            // How much of the stuck to search
    String name() {return "SearchExceptLast";}                                  // Name of the search
   }
  SearchExceptLast searchExceptLast(int Search) {return new SearchExceptLast(Search);}  // Search for a key ignoring the last element on the stuck

  class SearchFirstGreaterThanOrEqual extends Limit                             // Search for the first key that is greater than or equal
   {final int    search;                                                        // Search key
    final boolean found;                                                        // Whether the search key was found
    final int     index;                                                        // Index of the located key if any
    final int       key;                                                        // Located key if the search key was found
    final int      data;                                                        // Located data if search key was found
    String name() {return "SearchFirstGreaterThanOrEqual";}                     // Name of the search

    SearchFirstGreaterThanOrEqual(int Search)
     {z();
      boolean looking = true;
      search = Search;
      int i = 0, j = limit();
      for (; i < j && looking; i++)                                             // Search
       {z();
        if (key(i) >= search)                                                  // Search key is equal or greater to the current key
         {z(); looking = false; break;
         }
       }
      if (looking)                                                              // Search key is bigger than all keys in the stuck except possibly the last one
       {z(); found = false; index = limit(); key = 0;       data = 0;
       }
      else                                                                      // Found a greater than or equal key in the stuck excluding the last key
       {z(); found = true;  index = i; key = key(i); data = data(i);
       }
     }
    public String toString()                                                    // Print
     {final StringBuilder s = new StringBuilder();
      z();
      s.append(name()+"(Search:"+search);
      s.append(" index:"+index);
      s.append(" found:"+found);
      if (found)
       {z();
        s.append(  " key:"+key);
        s.append( " data:"+data);
       }
      s.append(")\n");
      return s.toString();
     }
   }
  SearchFirstGreaterThanOrEqual                                                 // Search for a key
  searchFirstGreaterThanOrEqual(int Search)
   {return new SearchFirstGreaterThanOrEqual(Search);
   }

  class     SearchFirstGreaterThanOrEqualExceptLast                             // Search for an element ignoring the last element on the stuck
    extends SearchFirstGreaterThanOrEqual
   {SearchFirstGreaterThanOrEqualExceptLast(int Search) {super(Search);}
    int   limit() {return size()-1;}                                            // How much of the stuck to search
    String name() {return "SearchFirstGreaterThanOrEqualExceptLast";}           // Name of the search
   }
  SearchFirstGreaterThanOrEqualExceptLast                                       // Search for a key ignoring the last element on the stuck
  searchFirstGreaterThanOrEqualExceptLast(int Search)
   {return new SearchFirstGreaterThanOrEqualExceptLast(Search);
   }

//D1 Print                                                                      // Print a stuck

  public String toString()
   {final StringBuilder s = new StringBuilder();                                //
    z();
    s.append("Stuck(maxSize:"+maxSize());
    s.append(" size:"+size()+")\n");
    for (int i = 0, j = size(); i < j; i++)                                     // Search
     {z(); s.append("  "+i+" key:"+key(i)+" data:"+data(i)+"\n");
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
    t.ok("""
Stuck(maxSize:8 size:5)
  0 key:2 data:1
  1 key:4 data:2
  2 key:9 data:9
  3 key:6 data:3
  4 key:8 data:4
""");
    t.insertElementAt(7, 7, 5);
    //stop(t);
    t.ok("""
Stuck(maxSize:8 size:6)
  0 key:2 data:1
  1 key:4 data:2
  2 key:9 data:9
  3 key:6 data:3
  4 key:8 data:4
  5 key:7 data:7
""");
    t.insertElementAt(6, 6, 0);
    //stop(t);
    t.ok("""
Stuck(maxSize:8 size:7)
  0 key:6 data:6
  1 key:2 data:1
  2 key:4 data:2
  3 key:9 data:9
  4 key:6 data:3
  5 key:8 data:4
  6 key:7 data:7
""");
    t.insertElementAt(5, 5, 0);
    //stop(t);
    t.ok("""
Stuck(maxSize:8 size:8)
  0 key:5 data:5
  1 key:6 data:6
  2 key:2 data:1
  3 key:4 data:2
  4 key:9 data:9
  5 key:6 data:3
  6 key:8 data:4
  7 key:7 data:7
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

    t.clear();
    FirstElement F = t.firstElement();
    //stop(F);
    ok(F, """
FirstElement(found:false key:0 data:0)
""");
    LastElement L = t.lastElement();
    //stop(L);
    ok(L, """
LastElement(found:false key:0 data:0)
""");
   }

  static void test_search()
   {Stuck  t = test_load();
    t.ok("""
Stuck(maxSize:8 size:4)
  0 key:2 data:1
  1 key:4 data:2
  2 key:6 data:3
  3 key:8 data:4
""");

    Search s = t.search(2);
    //stop(s);
    ok(s, """
Search(Search:2 found:true index:0 data:1)
""");

    Search S = t.search(3);
    //stop(S);
    ok(S, """
Search(Search:3 found:false)
""");
   }

  static void test_search_except_last()
   {Stuck  t = test_load();
    t.ok("""
Stuck(maxSize:8 size:4)
  0 key:2 data:1
  1 key:4 data:2
  2 key:6 data:3
  3 key:8 data:4
""");
    SearchExceptLast s = t.searchExceptLast(4);
    //stop(s);
    ok(s, """
SearchExceptLast(Search:4 found:true index:1 data:2)
""");
    SearchExceptLast S = t.searchExceptLast(8);
    //stop(S);
    ok(S, """
SearchExceptLast(Search:8 found:false)
""");
   }

  static void test_search_first_greater_than_or_equal()
   {Stuck  t = test_load();
    t.ok("""
Stuck(maxSize:8 size:4)
  0 key:2 data:1
  1 key:4 data:2
  2 key:6 data:3
  3 key:8 data:4
""");

    SearchFirstGreaterThanOrEqual b = t.searchFirstGreaterThanOrEqual(1);
    //stop(b);
    ok(b, """
SearchFirstGreaterThanOrEqual(Search:1 index:0 found:true key:2 data:1)
""");

    SearchFirstGreaterThanOrEqual s = t.searchFirstGreaterThanOrEqual(4);
    //stop(s);
    ok(s, """
SearchFirstGreaterThanOrEqual(Search:4 index:1 found:true key:4 data:2)
""");

    SearchFirstGreaterThanOrEqual S = t.searchFirstGreaterThanOrEqual(5);
    //stop(S);
    ok(S, """
SearchFirstGreaterThanOrEqual(Search:5 index:2 found:true key:6 data:3)
""");

    SearchFirstGreaterThanOrEqual T = t.searchFirstGreaterThanOrEqual(7);
    //stop(T);
    ok(T, """
SearchFirstGreaterThanOrEqual(Search:7 index:3 found:true key:8 data:4)
""");

    SearchFirstGreaterThanOrEqual E = t.searchFirstGreaterThanOrEqual(9);
    //stop(E);
    ok(E, """
SearchFirstGreaterThanOrEqual(Search:9 index:4 found:false)
""");
   }

  static void test_search_first_greater_than_or_equal_except_last()
   {Stuck  t = test_load();
    t.ok("""
Stuck(maxSize:8 size:4)
  0 key:2 data:1
  1 key:4 data:2
  2 key:6 data:3
  3 key:8 data:4
""");

    SearchFirstGreaterThanOrEqualExceptLast b = t.searchFirstGreaterThanOrEqualExceptLast(1);
    //stop(b);
    ok(b, """
SearchFirstGreaterThanOrEqualExceptLast(Search:1 index:0 found:true key:2 data:1)
""");

    SearchFirstGreaterThanOrEqualExceptLast s = t.searchFirstGreaterThanOrEqualExceptLast(4);
    //stop(s);
    ok(s, """
SearchFirstGreaterThanOrEqualExceptLast(Search:4 index:1 found:true key:4 data:2)
""");

    SearchFirstGreaterThanOrEqualExceptLast S = t.searchFirstGreaterThanOrEqualExceptLast(5);
    //stop(S);
    ok(S, """
SearchFirstGreaterThanOrEqualExceptLast(Search:5 index:2 found:true key:6 data:3)
""");

    SearchFirstGreaterThanOrEqualExceptLast T = t.searchFirstGreaterThanOrEqualExceptLast(7);
    //stop(T);
    ok(T, """
SearchFirstGreaterThanOrEqualExceptLast(Search:7 index:3 found:false)
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

  static void test_check_order()
   {Stuck      t = test_load();
    CheckOrder c = t.checkOrder();
    //stop(c);
    ok(c, """
CheckOrder(inOrder:true outOfOrder:0)
""");
    t.insertElementAt(99, 97, 1);
    CheckOrder C = t.checkOrder();
    //stop(C);
    ok(C, """
CheckOrder(inOrder:false outOfOrder:2)
""");
   }

  static void test_check_order_except_last()
   {Stuck      t = test_load();
    t.push(2, 2);
    CheckOrderExceptLast c = t.checkOrderExceptLast();
    //stop(c);
    ok(c, """
CheckOrderExceptLast(inOrder:true outOfOrder:0)
""");
    t.push(1, 1);
    CheckOrderExceptLast C = t.checkOrderExceptLast();
    //stop(C);
    ok(C, """
CheckOrderExceptLast(inOrder:false outOfOrder:4)
""");
   }

  static void test_find_first()
   {Stuck      t = stuck(4);
    t.push(2, 2);
    say(t);
    SearchFirstGreaterThanOrEqual s = t.searchFirstGreaterThanOrEqual(1);
    stop(s);
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
    test_search_except_last();
    test_search_first_greater_than_or_equal();
    test_search_first_greater_than_or_equal_except_last();
    test_set_element_at();
    test_check_order();
    test_check_order_except_last();
   }

  static void newTests()                                                        // Tests being worked on
   {oldTests();
    //test_find_first();
   }

  public static void main(String[] args)                                        // Test if called as a program
   {try                                                                         // Get a traceback in a format clickable in Geany if something goes wrong to speed up debugging.
     {if (github_actions) oldTests(); else newTests();                          // Tests to run
      if (github_actions)                                                     // Coverage analysis
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
