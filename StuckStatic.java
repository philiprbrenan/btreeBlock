//------------------------------------------------------------------------------
// Stuck with reduced use of new
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout  a binary tree on a silicon chip.
// Reference the keys and data through a small mapping table and free stack so that we can manipulate keys and data by a small index rather than copying them directly.
abstract class StuckStatic extends Test                                         // A fixed size stack of ordered key, data pairs with null deemed highest
 {abstract int maxSize();                                                       // The maximum number of entries in the stuck.
  final int[]Keys;                                                              // The keys on the stuck
  final int[]Data;                                                              // The data on the stuck
  int currentSize;                                                              // The current size of the stuck
  final Pop       Pop1       = new Pop();                                       // A popped element
  final Pop       Pop2       = new Pop();                                       // A popped element
  final Shift     Shift1     = new Shift();                                     // A shifted element
  final Shift     Shift2     = new Shift();                                     // A shifted element
  final Shift     Shift3     = new Shift();                                     // A shifted element
  final ElementAt ElementAt1 = new ElementAt();                                 // An element at
  final ElementAt ElementAt2 = new ElementAt();                                 // An element at
  final ElementAt ElementAt3 = new ElementAt();                                 // An element at
  final RemoveElementAt   RemoveElementAt1 = new RemoveElementAt();             // Remove an element
  final RemoveElementAt   RemoveElementAt2 = new RemoveElementAt();             // Remove an element
  final FirstElement         FirstElement1 = new FirstElement();                // First element
  final FirstElement         FirstElement2 = new FirstElement();                // First element
  final LastElement           LastElement1 = new LastElement();                 // Last element
  final LastElement           LastElement2 = new LastElement();                 // Last element
  final Search                     Search1 = new Search();                      // Search element
  final Search                     Search2 = new Search();                      // Search element
  final SearchExceptLast SearchExceptLast1 = new SearchExceptLast();            // Search element
  final SearchExceptLast SearchExceptLast2 = new SearchExceptLast();            // Search element
  final SearchFirstGreaterThanOrEqual SearchFirstGreaterThanOrEqual1 = new SearchFirstGreaterThanOrEqual();  // Search element
  final SearchFirstGreaterThanOrEqual SearchFirstGreaterThanOrEqual2 = new SearchFirstGreaterThanOrEqual();  // Search element
  final SearchFirstGreaterThanOrEqualExceptLast SearchFirstGreaterThanOrEqualExceptLast1 = new SearchFirstGreaterThanOrEqualExceptLast();  // Search element
  final SearchFirstGreaterThanOrEqualExceptLast SearchFirstGreaterThanOrEqualExceptLast2 = new SearchFirstGreaterThanOrEqualExceptLast();  // Search element

//D1 Construction                                                               // Create a stuck

  StuckStatic()                                                                 // Create the stuck with a maximum number of the specified elements
   {zz();
    currentSize = 0;
    Keys        = new int[maxSize()];
    Data        = new int[maxSize()];
   }

  static StuckStatic stuckStatic(final int MaxSize)                             // Create the stuck
   {z(); return new StuckStatic() {int maxSize() {return MaxSize;}};
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

  class CheckOrder extends Limit                                                // Check that tall he keys are in order,
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
   {int index;                                                                  // The index of the popped element
    int key;                                                                    // The popped key
    int data;                                                                   // The popped data
    Pop pop()
     {z();
      assertNotEmpty();
      dec();
      index = size();
      key   = key (index);
      data  = data(index);
      return this;
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
  Pop pop() {return Pop1.pop();}

  class Shift                                                                   // Shift a key, data pair from the stuck
   {int key;                                                                    // The shifted key
    int data;                                                                   // The shifted data
    Shift shift()
     {z();
      assertNotEmpty();
      key   = key (0);
      data  = data(0);

      for (int i = 0, j = size()-1; i < j; i++)                                 // Shift the stuck down one place
       {z();
        copyKeyData(i, i+1);
       }
      dec();
      return this;
     }
    public String toString()                                                    // Print
     {final StringBuilder s = new StringBuilder();
      s.append("Shift(key:"+key);
      s.append(" data:"+data);
      s.append(")\n");
      return s.toString();
     }
   }
  Shift shift1() {z(); return Shift1.shift();}
  Shift shift2() {z(); return Shift2.shift();}
  Shift shift3() {z(); return Shift3.shift();}

  class ElementAt                                                               // Get the indexed element
   {int index;                                                                  // The index from which the key, data pair were retrieved
    int key;                                                                    // The shifted key
    int data;                                                                   // The shifted data
    ElementAt elementAt(int Index)
     {z();
      index = Index;
      assertInNormal(index);
      key   = key (index);
      data  = data(index);
      return this;
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
  ElementAt elementAt1(int Index) {z(); return ElementAt1.elementAt(Index);}
  ElementAt elementAt2(int Index) {z(); return ElementAt2.elementAt(Index);}
  ElementAt elementAt3(int Index) {z(); return ElementAt3.elementAt(Index);}

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
   {int index;                                                                  // The index from which the key, data pair were retrieved
    int key;                                                                    // The shifted key
    int data;                                                                   // The shifted data
    RemoveElementAt removeElementAt(int Index)
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
      return this;
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
  RemoveElementAt removeElementAt1(int Index) {z(); return RemoveElementAt1.removeElementAt(Index);} // Remove the indicated element
  RemoveElementAt removeElementAt2(int Index) {z(); return RemoveElementAt2.removeElementAt(Index);} // Remove the indicated element

  class FirstElement                                                            // First element
   {boolean found;                                                              // Whether there was a first element
    int key;                                                                    // The shifted key
    int data;                                                                   // The shifted data
    FirstElement firstElement()
     {z();
      found = !isEmpty();
      if (found)
       {z();
        key  = key (0);
        data = data(0);
       }
      else {z(); key = data = 0;}
      return this;
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
  FirstElement firstElement1() {z(); return FirstElement1.firstElement();}      // First element
  FirstElement firstElement2() {z(); return FirstElement2.firstElement();}      // First element

  class LastElement                                                             // Last element
   {boolean found;                                                              // Whether there was a last element
    int key;                                                                    // The shifted key
    int data;                                                                   // The shifted data
    LastElement lastElement()
     {z();
      found = !isEmpty();
      final int s = size()-1;
      if (found)
       {z();
        key  = key (s);
        data = data(s);
       }
      else {z(); key = data = 0;}
      return this;
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
  LastElement lastElement1() {z(); return LastElement1.lastElement();}          // Last element
  LastElement lastElement2() {z(); return LastElement2.lastElement();}          // Last element

//D1 Search                                                                     // Search a stuck.

  class Limit                                                                   // Search all of the stuck
   {String name() {return "Limit";}                                             // Name of limit
    int   limit() {return size();}                                              // How far to check
   }

  class Search extends Limit                                                    // Search for an element within all elements of the stuck
   {int key;                                                                    // Search key
    boolean found;                                                              // Whether the key was found
    int index ;                                                                 // Index of the located key if any
    int data;                                                                   // Located data if key was found
    String name() {return "Search";}                                            // Name of the search

    Search()           {z(); }                                                  // Search for an element within all elements of the stuck
    Search(int Search) {z(); search(Search);}                                   // Search for an element within all elements of the stuck

    Search search(int Search)                                                   // Search for an element within all elements of the stuck
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
      return this;
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
  Search search1(int Search) {z(); return Search1.search(Search);}              // Search for a key ignoring the last element on the stuck
  Search search2(int Search) {z(); return Search2.search(Search);}              // Search for a key ignoring the last element on the stuck

  class SearchExceptLast extends Search                                         // Search for an element ignoring the last element on the stuck
   {SearchExceptLast()                            {super(); z();}
    SearchExceptLast                 (int search) {super(search); z();}
    SearchExceptLast searchExceptLast(int search) {z(); search(search); return this;}
    int   limit() {z(); return size()-1;}                                            // How much of the stuck to search
    String name() {z(); return "SearchExceptLast";}                                  // Name of the search
   }
  SearchExceptLast searchExceptLast1(int Search) {z(); return SearchExceptLast1.searchExceptLast(Search);}  // Search for a key ignoring the last element on the stuck
  SearchExceptLast searchExceptLast2(int Search) {z(); return SearchExceptLast2.searchExceptLast(Search);}  // Search for a key ignoring the last element on the stuck

  class SearchFirstGreaterThanOrEqual extends Limit                             // Search for the first key that is greater than or equal
   {int    search;                                                              // Search key
    boolean found;                                                              // Whether the search key was found
    int     index;                                                              // Index of the located key if any
    int       key;                                                              // Located key if the search key was found
    int      data;                                                              // Located data if search key was found
    String name() {return "SearchFirstGreaterThanOrEqual";}                     // Name of the search

    SearchFirstGreaterThanOrEqual searchFirstGreaterThanOrEqual(int Search)
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
      return this;
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
  searchFirstGreaterThanOrEqual1(int Search)
   {z(); return SearchFirstGreaterThanOrEqual1.searchFirstGreaterThanOrEqual(Search);
   }
  SearchFirstGreaterThanOrEqual                                                 // Search for a key
  searchFirstGreaterThanOrEqual2(int Search)
   {z(); return SearchFirstGreaterThanOrEqual2.searchFirstGreaterThanOrEqual(Search);
   }

  class     SearchFirstGreaterThanOrEqualExceptLast                             // Search for an element ignoring the last element on the stuck
    extends SearchFirstGreaterThanOrEqual
   {SearchFirstGreaterThanOrEqualExceptLast()           {}
    SearchFirstGreaterThanOrEqualExceptLast
    searchFirstGreaterThanOrEqualExceptLast(int search) {z(); searchFirstGreaterThanOrEqual(search); return this;}   // Search for a key ignoring the last element on the stuck
    int   limit() {z(); return size()-1;}                                       // How much of the stuck to search
    String name() {z(); return "SearchFirstGreaterThanOrEqualExceptLast";}      // Name of the search
   }
  SearchFirstGreaterThanOrEqualExceptLast                                       // Search for a key ignoring the last element on the stuck
  searchFirstGreaterThanOrEqualExceptLast1(int Search)
   {z(); return SearchFirstGreaterThanOrEqualExceptLast1.searchFirstGreaterThanOrEqualExceptLast(Search);
   }
  SearchFirstGreaterThanOrEqualExceptLast                                       // Search for a key ignoring the last element on the stuck
  searchFirstGreaterThanOrEqualExceptLast2(int Search)
   {z(); return SearchFirstGreaterThanOrEqualExceptLast2.searchFirstGreaterThanOrEqualExceptLast(Search);
   }

//D1 Print                                                                      // Print a stuck

  public String toString()
   {final StringBuilder s = new StringBuilder();                                //
    z();
    s.append("StuckStatic(maxSize:"+maxSize());
    s.append(" size:"+size()+")\n");
    for (int i = 0, j = size(); i < j; i++)                                     // Search
     {z(); s.append("  "+i+" key:"+key(i)+" data:"+data(i)+"\n");
     }
    return s.toString();
   }

//D0 Tests                                                                      // Test stuck

  static StuckStatic test_load()
   {StuckStatic t = stuckStatic(8);
    t.push(2, 1); t.push(4, 2); t.push(6, 3); t.push(8, 4);
    //stop(t);
    t.ok("""
StuckStatic(maxSize:8 size:4)
  0 key:2 data:1
  1 key:4 data:2
  2 key:6 data:3
  3 key:8 data:4
""");
    return t;
   }

  static void test_push()
   {StuckStatic t = stuckStatic(4);
    t.push(1, 2); t.push(2, 4);
    //stop(t);
    t.ok("""
StuckStatic(maxSize:4 size:2)
  0 key:1 data:2
  1 key:2 data:4
""");
   }

  static void test_pop()
   {StuckStatic t = test_load();
    Pop p = t.pop();
    //stop(p);
    ok(p, """
Pop(index:3 key:8 data:4)
""");
    //stop(t);
    t.ok("""
StuckStatic(maxSize:8 size:3)
  0 key:2 data:1
  1 key:4 data:2
  2 key:6 data:3
""");
   }

  static void test_shift()
   {StuckStatic t = test_load();
    Shift s = t.shift1();
    //stop(s);
    ok(s, """
Shift(key:2 data:1)
""");
    //stop(t);
    t.ok("""
StuckStatic(maxSize:8 size:3)
  0 key:4 data:2
  1 key:6 data:3
  2 key:8 data:4
""");
   }

  static void test_unshift()
   {StuckStatic t = test_load();
    t.unshift(9, 9);
    //stop(t);
    t.ok("""
StuckStatic(maxSize:8 size:5)
  0 key:9 data:9
  1 key:2 data:1
  2 key:4 data:2
  3 key:6 data:3
  4 key:8 data:4
""");
   }

  static void test_elementAt()
   {StuckStatic     t = test_load();
    ElementAt e = t.elementAt1(2);
    //stop(e);
    ok(e, """
ElementAt(index:2 key:6 data:3)
""");
   }

  static void test_insert_element_at()
   {StuckStatic t = test_load();
    t.insertElementAt(9, 9, 2);
    t.ok("""
StuckStatic(maxSize:8 size:5)
  0 key:2 data:1
  1 key:4 data:2
  2 key:9 data:9
  3 key:6 data:3
  4 key:8 data:4
""");
    t.insertElementAt(7, 7, 5);
    //stop(t);
    t.ok("""
StuckStatic(maxSize:8 size:6)
  0 key:2 data:1
  1 key:4 data:2
  2 key:9 data:9
  3 key:6 data:3
  4 key:8 data:4
  5 key:7 data:7
""");
   }

  static void test_remove_element_at()
   {StuckStatic t = test_load();
    RemoveElementAt a = t.removeElementAt1(2);
    //stop(a);
    ok(a, """
RemoveElementAt(index:2 key:6 data:3)
""");
    //stop(t);
    t.ok("""
StuckStatic(maxSize:8 size:3)
  0 key:2 data:1
  1 key:4 data:2
  2 key:8 data:4
""");
   }

  static void test_first_last()
   {StuckStatic t = test_load();
    FirstElement f = t.firstElement1();
    //stop(f);
    ok(f, """
FirstElement(found:true key:2 data:1)
""");
    LastElement l = t.lastElement1();
    //stop(l);
    ok(l, """
LastElement(found:true key:8 data:4)
""");

    t.clear();
    FirstElement F = t.firstElement2();
    //stop(F);
    ok(F, """
FirstElement(found:false key:0 data:0)
""");
    LastElement L = t.lastElement2();
    //stop(L);
    ok(L, """
LastElement(found:false key:0 data:0)
""");
   }

  static void test_search()
   {StuckStatic  t = test_load();
    t.ok("""
StuckStatic(maxSize:8 size:4)
  0 key:2 data:1
  1 key:4 data:2
  2 key:6 data:3
  3 key:8 data:4
""");

    Search s = t.search1(2);
    //stop(s);
    ok(s, """
Search(Search:2 found:true index:0 data:1)
""");

    Search S = t.search2(3);
    //stop(S);
    ok(S, """
Search(Search:3 found:false)
""");
   }

  static void test_search_except_last()
   {StuckStatic  t = test_load();
    t.ok("""
StuckStatic(maxSize:8 size:4)
  0 key:2 data:1
  1 key:4 data:2
  2 key:6 data:3
  3 key:8 data:4
""");
    SearchExceptLast s = t.searchExceptLast1(4);
    SearchExceptLast S = t.searchExceptLast2(8);
    //stop(s);
    ok(s, """
SearchExceptLast(Search:4 found:true index:1 data:2)
""");
    //stop(S);
    ok(S, """
SearchExceptLast(Search:8 found:false)
""");
   }

  static void test_search_first_greater_than_or_equal()
   {StuckStatic  t = test_load();
    t.ok("""
StuckStatic(maxSize:8 size:4)
  0 key:2 data:1
  1 key:4 data:2
  2 key:6 data:3
  3 key:8 data:4
""");

    SearchFirstGreaterThanOrEqual b = t.searchFirstGreaterThanOrEqual1(1);
    SearchFirstGreaterThanOrEqual s = t.searchFirstGreaterThanOrEqual2(4);
    //stop(b);
    ok(b, """
SearchFirstGreaterThanOrEqual(Search:1 index:0 found:true key:2 data:1)
""");

    //stop(s);
    ok(s, """
SearchFirstGreaterThanOrEqual(Search:4 index:1 found:true key:4 data:2)
""");

    SearchFirstGreaterThanOrEqual S = t.searchFirstGreaterThanOrEqual1(5);
    SearchFirstGreaterThanOrEqual T = t.searchFirstGreaterThanOrEqual2(7);
    //stop(S);
    ok(S, """
SearchFirstGreaterThanOrEqual(Search:5 index:2 found:true key:6 data:3)
""");

    //stop(T);
    ok(T, """
SearchFirstGreaterThanOrEqual(Search:7 index:3 found:true key:8 data:4)
""");

    SearchFirstGreaterThanOrEqual E = t.searchFirstGreaterThanOrEqual1(9);
    //stop(E);
    ok(E, """
SearchFirstGreaterThanOrEqual(Search:9 index:4 found:false)
""");
   }

  static void test_search_first_greater_than_or_equal_except_last()
   {StuckStatic  t = test_load();
    t.ok("""
StuckStatic(maxSize:8 size:4)
  0 key:2 data:1
  1 key:4 data:2
  2 key:6 data:3
  3 key:8 data:4
""");

    SearchFirstGreaterThanOrEqualExceptLast b = t.searchFirstGreaterThanOrEqualExceptLast1(1);
    SearchFirstGreaterThanOrEqualExceptLast s = t.searchFirstGreaterThanOrEqualExceptLast2(4);
    //stop(b);
    ok(b, """
SearchFirstGreaterThanOrEqualExceptLast(Search:1 index:0 found:true key:2 data:1)
""");

    //stop(s);
    ok(s, """
SearchFirstGreaterThanOrEqualExceptLast(Search:4 index:1 found:true key:4 data:2)
""");

    SearchFirstGreaterThanOrEqualExceptLast S = t.searchFirstGreaterThanOrEqualExceptLast1(5);
    SearchFirstGreaterThanOrEqualExceptLast T = t.searchFirstGreaterThanOrEqualExceptLast2(7);
    //stop(S);
    ok(S, """
SearchFirstGreaterThanOrEqualExceptLast(Search:5 index:2 found:true key:6 data:3)
""");

    //stop(T);
    ok(T, """
SearchFirstGreaterThanOrEqualExceptLast(Search:7 index:3 found:false)
""");
   }

  static void test_set_element_at()
   {StuckStatic  t = test_load();
    t.setElementAt(22, 33, 2);
    //stop(t);
    t.ok("""
StuckStatic(maxSize:8 size:4)
  0 key:2 data:1
  1 key:4 data:2
  2 key:22 data:33
  3 key:8 data:4
""");
    t.setElementAt(99, 97, 4);
    //stop(t);
    t.ok("""
StuckStatic(maxSize:8 size:5)
  0 key:2 data:1
  1 key:4 data:2
  2 key:22 data:33
  3 key:8 data:4
  4 key:99 data:97
""");
   }

  static void test_check_order()
   {StuckStatic      t = test_load();
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
   {StuckStatic      t = test_load();
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
