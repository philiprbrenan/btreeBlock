//------------------------------------------------------------------------------
// StuckStatic in bit memory
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout  a binary tree on a silicon chip.
// Reference the keys and data through a small mapping table and free stack so that we can manipulate keys and data by a small index rather than copying them directly.
abstract class StuckSML extends Test                                            // A fixed size stack of ordered key, data pairs with null deemed highest
 {abstract int maxSize();                                                       // The maximum number of entries in the stuck.
  abstract int bitsPerKey();                                                    // The number of bits per key
  abstract int bitsPerData();                                                   // The number of bits per data
  abstract int bitsPerSize();                                                   // The number of bits in size field

  final MemoryLayout memoryLayout = new MemoryLayout();                         // Memory for stuck

  Layout.Variable  key;                                                         // Key in a stuck
  Layout.Array     Keys;                                                        // Array of keys
  Layout.Variable  data;                                                        // Data associated with a key
  Layout.Array     Data;                                                        // Array of data associated with the array of keys
  Layout.Variable  currentSize;                                                 // Current size of stuck
  Layout.Structure stuck;                                                       // The stuck itself

  final Pop                           Pop1 = new Pop();                         // A popped element
  final Shift                       Shift1 = new Shift();                       // A shifted element
  final Shift                       Shift2 = new Shift();                       // A shifted element
  final Shift                       Shift3 = new Shift();                       // A shifted element
  final ElementAt               ElementAt1 = new ElementAt();                   // An element at
  final ElementAt               ElementAt2 = new ElementAt();                   // An element at
  final RemoveElementAt   RemoveElementAt1 = new RemoveElementAt();             // Remove an element
  final FirstElement         FirstElement1 = new FirstElement();                // First element
  final FirstElement         FirstElement2 = new FirstElement();                // First element
  final LastElement           LastElement1 = new LastElement();                 // Last element
  final LastElement           LastElement2 = new LastElement();                 // Last element
  final Search                     Search1 = new Search();                      // Search element
  final Search                     Search2 = new Search();                      // Search element
  final SearchExceptLast SearchExceptLast1 = new SearchExceptLast();            // Search for an element in the stuck ignoring the last element
  final SearchExceptLast SearchExceptLast2 = new SearchExceptLast();            // Search for an element in the stuck ignoring the last element
  final SearchFirstGreaterThanOrEqual SearchFirstGreaterThanOrEqual1 = new SearchFirstGreaterThanOrEqual();  // Search element
  final SearchFirstGreaterThanOrEqual SearchFirstGreaterThanOrEqual2 = new SearchFirstGreaterThanOrEqual();  // Search element
  final SearchFirstGreaterThanOrEqualExceptLast SearchFirstGreaterThanOrEqualExceptLast1 = new SearchFirstGreaterThanOrEqualExceptLast();  // Search element
  final SearchFirstGreaterThanOrEqualExceptLast SearchFirstGreaterThanOrEqualExceptLast2 = new SearchFirstGreaterThanOrEqualExceptLast();  // Search element

  static boolean debug;                                                         // Debug when true

//D1 Construction                                                               // Create a stuck

  StuckSML()                                                                    // Create the layout for the stuck
   {z();
    memoryLayout.layout(layout());
   }

  StuckSML copy()                                                               // Copy a stuck definition
   {z();
    final StuckSML parent = this;
    final StuckSML  child = new StuckSML()
     {int maxSize    () {return parent.maxSize    ();}
      int bitsPerKey () {return parent.bitsPerKey ();}
      int bitsPerData() {return parent.bitsPerData();}
      int bitsPerSize() {return parent.bitsPerSize();}
     };
    child.key         = parent.key;
    child.Keys        = parent.Keys;
    child.data        = parent.data;
    child.Data        = parent.Data;
    child.currentSize = parent.currentSize;
    child.stuck       = parent.stuck;
    child.memoryLayout.memory(parent.memoryLayout.memory);
    child.memoryLayout.layout(parent.memoryLayout.layout);
    child.memoryLayout.base  (parent.memoryLayout.base);
    return child;
   }

  Layout layout()                                                               // Layout describing stuck
   {z();
    final Layout  l = Layout.layout();
    key         = l.variable ("key"        ,         bitsPerKey());
    Keys        = l.array    ("Keys"       , key,    maxSize());
    data        = l.variable ("data"       ,         bitsPerData());
    Data        = l.array    ("Data"       , data,   maxSize());
    currentSize = l.variable ("currentSize", bitsPerSize());
    stuck       = l.structure("stuck"      , currentSize, Keys, Data);
    l.layoutName = "mainStuckSML";
    return l.compile();
   }

//D1 Characteristics                                                            // Characteristics of the stuck

  void base(int Base)                                                           // Set the base address of the stuck in the memory layout containing the stuck
   {z();  memoryLayout.base(Base);
   }

  int size()                                                                    // The current number of key elements in the stuck
   {z(); final int s = memoryLayout.getInt(currentSize);
    return s;
   }
  boolean isFull       () {z(); return size() > maxSize();}                     // Check the stuck is full
  boolean isEmpty      () {z(); return size() == 0;}                            // Check the stuck is empty
  void assertNotFull   () {z(); if (isFull ()) stop("Full") ;}                  // Assert the stack is not full
  void assertNotEmpty  () {z(); if (isEmpty()) stop("Empty");}                  // Assert the stack is not empty
  void assertInNormal  (int i) {z(); final int s = size(); if (i < 0 || i >= s) stop("Out of normal range",   i, "for size", s);} // Check that the index would yield a valid element
  void assertInExtended(int i) {z(); final int s = size(); if (i < 0 || i >  s) stop("Out of extended range", i, "for size", s);} // Check that the index would yield a valid element

  class CheckOrder extends Limit                                                // Check that all the keys are in order,
   {boolean inOrder;                                                            // Keys are in order
    int  outOfOrder;                                                            // Index of first key out of order
    String name() {return "CheckOrder";}                                        // Name of limit

    CheckOrder()                                                                // Check that all the keys are in order,
     {super(); z();
      boolean looking = true;
      int i = 1, j = limit();
      for (;i < j && looking; i++)                                              // Check each key
       {z(); if (key(i-1) > key(i)) {looking = false; break;}
       }
      if (looking) {z(); inOrder = true;  outOfOrder = 0;}
      else         {z(); inOrder = false; outOfOrder = i;}
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
  CheckOrderExceptLast checkOrderExceptLast()
   {z();
    return new CheckOrderExceptLast();
   }

//D1 Memory                                                                     // Actions on memory of stuck

  int  getInt(Layout.Field field)                       {z(); return memoryLayout.getInt(field);}
  int  getInt(Layout.Field field,            int index) {z(); return memoryLayout.getInt(field,        index);}

  void setInt(Layout.Field field, int value)            {z();        memoryLayout.setInt(field, value);}
  void setInt(Layout.Field field, int value, int index) {z();        memoryLayout.setInt(field, value, index);}

  int  key     (int Index)              {z(); return getInt(key,    Index);}
  int  data    (int Index)              {z(); return getInt(data,   Index);}
  void setKey  (int Index,  int Value)  {z(); setInt (key,  Value,  Index);}
  void setData (int Index,  int Value)  {z(); setInt (data, Value,  Index);}
  void copyKey (int Target, int Source) {z(); setKey (Target, key (Source));}
  void copyData(int Target, int Source) {z(); setData(Target, data(Source));}

  void  setKeyData(int Index, int Key, int Data)
   {z();
    setKey (Index, Key);
    setData(Index, Data);
   }

  void copyKeyData(int Target, int Source)
   {copyKey (Target, Source);
    copyData(Target, Source);
   }

//D1 Actions                                                                    // Place and remove data to/from stuck

  void inc  () {z(); assertNotFull (); final int v = getInt(currentSize)+1; setInt(currentSize, v);}  // Increment the current size
  void dec  () {z(); assertNotEmpty(); final int v = getInt(currentSize)-1; setInt(currentSize, v);}  // Decrement the current size
  void clear() {z();                       final int v = 0                ; setInt(currentSize, v);}  // Clear the stuck

  void push(int key, int data)                                                  // Push an element onto the stuck
   {z();
    assertNotFull();
    final int cs = getInt(currentSize);
    setKeyData(cs, key, data);
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

  class Result                                                                  // Result of a stuck operation
   {int    search;                                                              // Search key
    boolean found;                                                              // Whether a matcbing element was found
    int     index;                                                              // The index from which the key, data pair were retrieved
    int       key;                                                              // The retrieved key
    int     data;                                                               // The retrieved data
   }

  class Pop extends Result                                                      // Pop a key, data pair from the stuck
   {Pop pop()
     {z();
      assertNotEmpty();
      dec();
      index = size();
      key   = key (index);
      data  = data(index);
      return this;
     }
    public String toString()
     {final StringBuilder s = new StringBuilder();
      s.append("Pop(index:"+index);
      s.append(" key:"+key);
      s.append(" data:"+data);
      s.append(")\n");
      return s.toString();
     }
   }
  Pop pop() {return Pop1.pop();}

  class Shift extends Result                                                    // Shift a key, data pair from the stuck
   {Shift shift()
     {z();
      assertNotEmpty();
      key   = key (0);
      data  = data(0);
      for (int i = 0, j = size()-1; i < j; i++)                                 // Shift the stuck down one place
       {z(); copyKeyData(i, i+1);
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

  class ElementAt extends Result                                                // Get the indexed element
   {ElementAt() {}
    ElementAt elementAt(int Index)                                              // Look up key and data associated with the index in the stuck at the specified base offset in memory
     {z(); index = Index;
      assertInNormal(index);
      key   = key   (index);
      data  = data  (index);
      return this;
     }
    ElementAt copy()
     {z(); final ElementAt c = new ElementAt();
      c.index = index; c.key = key; c.data = data;
      return c;
     }
    boolean equals(ElementAt e)
     {z();
      return index == e.index && key == e.key && data == e.data;
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

  void setElementAt(int key, int data, int Index)                               // Set an element either in range or one above the current range
   {if (Index == size())                                                        // Extended range
     {z(); setKeyData(Index, key, data); inc();
     }
    else                                                                        // In range
     {z(); assertInNormal(Index); setKeyData(Index, key, data);
     }
   }

  void insertElementAt(int key, int data, int Index)                  // Insert an element at the indicated location shifting all the remaining elements up one
   {z(); assertInExtended(Index);
    for (int i = size(); i > Index; --i)                                        // Shift the stuck up one place
     {z(); copyKeyData(i, i-1);
     }
    setKeyData(Index, key, data);
    inc();
   }

  class RemoveElementAt extends Result                                          // Remove the indicated element
   {RemoveElementAt removeElementAt(int Index)
     {z(); index = Index;
      assertInNormal(index);
      key     = key (index);
      data    = data(index);
      for (int i = index, j = size()-1; i < j; i++)                             // Shift the stuck down one place
       {z(); copyKeyData(i, i+1);
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

  class FirstElement extends Result                                             // First element
   {FirstElement firstElement()
     {z();
      found = !isEmpty();
      if (found)
       {z();
        index = 0;
        key   = key (0);
        data  = data(0);
       }
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
  FirstElement firstElement1() {z(); return FirstElement1.firstElement();} // First element
  FirstElement firstElement2() {z(); return FirstElement2.firstElement();} // First element

  class LastElement extends Result                                              // Last element
   {LastElement lastElement()
     {z();
      found = !isEmpty();
      final int s = size()-1;
      if (found)
       {z();
        index = s;
        key   = key (s);
        data  = data(s);
       }
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

  class Limit extends Result                                                    // Search all of the stuck
   {Limit() {z();}
    String name() {return "Limit";}                                             // Name of limit
    int   limit() {z(); return size();}                                         // How far to check
   }

  class Search extends Limit                                                    // Search for an element within all elements of the stuck
   {String name() {return "Search";}                                            // Name of the search

    Search() {z();}                                                             // Search for an element within all elements of the stuck

    Search search(int Search)                                                   // Search for an element within all elements of the stuck
     {z();
      boolean looking = true;
      key = Search;
      int i = 0, j = limit();

      for (; i < j && looking; i++)                                             // Search
       {z(); if (key(i) == key) {z(); looking = false; break;}
       }
      if (looking) {z(); found = false; index = 0; data = 0;}                   // Search key is bigger than all keys in the stuck
      else {z(); found = true;  index = i; data = data(i);}                     // Found a greater than or equal key in the stuck
      return this;
     }

    public String toString()                                                    // Print
     {z();
      final StringBuilder s = new StringBuilder();
      s.append(name()+"(Search:"+key);
      s.append(" found:"+found);
      if (found) {z(); s.append(" index:"+index+" data:"+data);}
      s.append(")\n");
      return s.toString();
     }
   }
  Search search1(int Search) {z(); return Search1.search(Search);}              // Search for a key ignoring the last element on the stuck
  Search search2(int Search) {z(); return Search2.search(Search);}              // Search for a key ignoring the last element on the stuck

  class SearchExceptLast extends Search                                         // Search for an element ignoring the last element on the stuck
   {SearchExceptLast() {super(); z();}
    SearchExceptLast searchExceptLast(int search) {z(); search(search); return this;}
    int   limit() {z(); return size()-1;}                                       // How much of the stuck to search
    String name()         {z(); return "SearchExceptLast";}                     // Name of the search
   }
  SearchExceptLast searchExceptLast1(int Search) {z(); return SearchExceptLast1.searchExceptLast(Search);}  // Search for a key ignoring the last element on the stuck
  SearchExceptLast searchExceptLast2(int Search) {z(); return SearchExceptLast2.searchExceptLast(Search);}  // Search for a key ignoring the last element on the stuck

  class SearchFirstGreaterThanOrEqual extends Limit                             // Search for the first key that is greater than or equal
   {String name() {return "SearchFirstGreaterThanOrEqual";}                     // Name of the search

    SearchFirstGreaterThanOrEqual searchFirstGreaterThanOrEqual                 // Search for first greater than or equal
     (int Search)
     {z();
      boolean looking = true;
      search = Search;
      int i = 0, j = limit();
      for (; i < j && looking; i++)                                             // Search
       {z();
        if (key(i) >= search)                                                   // Search key is equal or greater to the current key
         {z(); looking = false; break;
         }
       }
      if (looking)                                                              // Search key is bigger than all keys in the stuck except possibly the last one
       {z(); found = false; index = limit(); key = 0;  data = 0;
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
    int   limit() {z(); return size()-1;}                           // How much of the stuck to search
    String name()         {z(); return "SearchFirstGreaterThanOrEqualExceptLast";}      // Name of the search
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
    s.append("StuckSML(maxSize:"+maxSize());
    s.append(" size:"+size()+")\n");
    for (int i = 0, j = size(); i < j; i++)                                     // Search
     {z(); s.append("  "+i+" key:"+key(i)+" data:"+data(i)+"\n");
     }
    return s.toString();
   }

//D1 Testing                                                                    // Test the stuck

  static StuckSML stuckStatic()                                                 // Create a sample stuck
   {z();
    final int offset = 16;                                                      // To make testing more relevant
    final StuckSML s =  new StuckSML()
     {int maxSize     () {return  4;}
      int bitsPerKey  () {return 16;}
      int bitsPerData () {return 16;}
      int bitsPerSize () {return 16;}
     };
    s.memoryLayout.memory(new Memory(s.memoryLayout.layout.size()+offset));
    s.base(offset);
    return s;
   }

  public void ok(String expected) {ok(toString(), expected);}                   // Check the stuck

//D0 Tests                                                                      // Test stuck

  static StuckSML test_load()
   {StuckSML t = stuckStatic();

    t.push(2, 1); t.push(4, 2); t.push(6, 3); t.push(8, 4);
    ok(t.toString(), """
StuckSML(maxSize:4 size:4)
  0 key:2 data:1
  1 key:4 data:2
  2 key:6 data:3
  3 key:8 data:4
""");
    return t;
   }

  static void test_push()
   {StuckSML t = stuckStatic();

    t.push(15,  9);
    t.push(14, 10);
    t.push(13, 11);
    t.push(12, 12);
    //stop(t.memoryLayout());
    ok(t.memoryLayout, """
Line T       At      Wide       Size    Indices        Value   Name
   1 S       16       144                                      stuck
   2 V       16        16                                  4     currentSize
   3 A       32        64          4                             Keys
   4 V       32        16               0                 15       key
   5 V       48        16               1                 14       key
   6 V       64        16               2                 13       key
   7 V       80        16               3                 12       key
   8 A       96        64          4                             Data
   9 V       96        16               0                  9       data
  10 V      112        16               1                 10       data
  11 V      128        16               2                 11       data
  12 V      144        16               3                 12       data
""");
    //stop(t.memory());
    ok(t.memoryLayout.memory, """
      4... 4... 4... 4... 3... 3... 3... 3... 2... 2... 2... 2... 1... 1... 1... 1...
Line  FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210
   0  0000 0000 0000 0000 0000 0000 000c 000b 000a 0009 000c 000d 000e 000f 0004 0000
""");

    //stop(t.toString());

    ok(t.toString(), """
StuckSML(maxSize:4 size:4)
  0 key:15 data:9
  1 key:14 data:10
  2 key:13 data:11
  3 key:12 data:12
""");
   }

  static void test_pop()
   {StuckSML     t = test_load();

    Pop p = t.pop();
    //stop(p);
    ok(p, """
Pop(index:3 key:8 data:4)
""");
    //stop(t);
    ok(t.toString(), """
StuckSML(maxSize:4 size:3)
  0 key:2 data:1
  1 key:4 data:2
  2 key:6 data:3
""");
   }

  static void test_shift()
   {StuckSML t = test_load();

    Shift s = t.shift1();
    //stop(s);
    ok(s, """
Shift(key:2 data:1)
""");
    //stop(t.toString());
    ok(t.toString(), """
StuckSML(maxSize:4 size:3)
  0 key:4 data:2
  1 key:6 data:3
  2 key:8 data:4
""");
    Shift S = t.shift2();
    //stop(s);
    ok(S, """
Shift(key:4 data:2)
""");
    //stop(t.toString());
    ok(t.toString(), """
StuckSML(maxSize:4 size:2)
  0 key:6 data:3
  1 key:8 data:4
""");
    Shift T = t.shift3();
    //stop(s);
    ok(T, """
Shift(key:6 data:3)
""");
    //stop(t.toString());
    ok(t.toString(), """
StuckSML(maxSize:4 size:1)
  0 key:8 data:4
""");
   }

  static void test_unshift()
   {StuckSML t = test_load();

    Pop p = t.pop();
    t.unshift(9, 9);
    //stop(t.toString());
    ok(t.toString(), """
StuckSML(maxSize:4 size:4)
  0 key:9 data:9
  1 key:2 data:1
  2 key:4 data:2
  3 key:6 data:3
""");
   }

  static void test_elementAt()
   {StuckSML     t = test_load();

    ElementAt e = t.elementAt1(2);
    //stop(e);
    ok(e, """
ElementAt(index:2 key:6 data:3)
""");
    ElementAt E = t.elementAt2(3);
    //stop(E);
    ok(E, """
ElementAt(index:3 key:8 data:4)
""");
    ElementAt f = e.copy();
    ok(e.equals(f));
   }

  static void test_insert_element_at()
   {StuckSML t = test_load();

    Pop p = t.pop();
    t.insertElementAt(9, 9, 2);
    //stop(t.toString());
    ok(t.toString(), """
StuckSML(maxSize:4 size:4)
  0 key:2 data:1
  1 key:4 data:2
  2 key:9 data:9
  3 key:6 data:3
""");
    Pop P = t.pop();
    t.insertElementAt(7, 7, 3);
    //stop(t.toString());
    ok(t.toString(), """
StuckSML(maxSize:4 size:4)
  0 key:2 data:1
  1 key:4 data:2
  2 key:9 data:9
  3 key:7 data:7
""");
   }

  static void test_remove_element_at()
   {StuckSML t = test_load();

    RemoveElementAt a = t.removeElementAt1(2);
    //stop(a);
    ok(a, """
RemoveElementAt(index:2 key:6 data:3)
""");
    //stop(t.toString());
    ok(t.toString(), """
StuckSML(maxSize:4 size:3)
  0 key:2 data:1
  1 key:4 data:2
  2 key:8 data:4
""");
   }

  static void test_first_last()
   {StuckSML t = test_load();
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
   {StuckSML  t = test_load();
    //stop(t.toString());
    ok(t.toString(), """
StuckSML(maxSize:4 size:4)
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
   {StuckSML  t = test_load();
    //stop(t.toString());
    ok(t.toString(), """
StuckSML(maxSize:4 size:4)
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
   {StuckSML  t = test_load();
    //stop(t.toString());
    ok(t.toString(), """
StuckSML(maxSize:4 size:4)
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
   {StuckSML  t = test_load();
    //stop(t.toString());
    ok(t.toString(), """
StuckSML(maxSize:4 size:4)
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
   {StuckSML  t = test_load();
    t.setElementAt(22, 33, 2);
    //stop(t.toString());
    ok(t.toString(), """
StuckSML(maxSize:4 size:4)
  0 key:2 data:1
  1 key:4 data:2
  2 key:22 data:33
  3 key:8 data:4
""");
    t.setElementAt(99, 97, 3);
    //stop(t.toString());
    ok(t.toString(), """
StuckSML(maxSize:4 size:4)
  0 key:2 data:1
  1 key:4 data:2
  2 key:22 data:33
  3 key:99 data:97
""");
    t.pop();
    t.setElementAt(88, 87, 3);
    //stop(t.toString());
    ok(t.toString(), """
StuckSML(maxSize:4 size:4)
  0 key:2 data:1
  1 key:4 data:2
  2 key:22 data:33
  3 key:88 data:87
""");
   }

  static void test_check_order()
   {StuckSML     t = test_load();
    CheckOrder   c = t.checkOrder();
    //stop(c);
    ok(c, """
CheckOrder(inOrder:true outOfOrder:0)
""");
    Pop p = t.pop();
    t.insertElementAt(99, 97, 1);
    CheckOrder C = t.checkOrder();
    //stop(C);
    ok(C, """
CheckOrder(inOrder:false outOfOrder:2)
""");
   }

  static void test_check_order_except_last()
   {StuckSML             t = test_load();
    CheckOrderExceptLast c = t.checkOrderExceptLast();
    ok(c, """
CheckOrderExceptLast(inOrder:true outOfOrder:0)
""");
    t.pop ();
    t.pop ();
    t.push(1, 1);
    t.push(2, 2);
    CheckOrderExceptLast C = t.checkOrderExceptLast();
    //stop(C);
    ok(C, """
CheckOrderExceptLast(inOrder:false outOfOrder:2)
""");
   }

  static void test_at()
   {final StuckSML S = new StuckSML()
     {int maxSize     () {return 4;}
      int bitsPerKey  () {return 8;}
      int bitsPerData () {return 8;}
      int bitsPerSize () {return 8;}
      int baseAt      () {return 0;}
     };
    final int N = S.memoryLayout.layout.size();
    S.memoryLayout.memory(new Memory(2 * N));

    final StuckSML s = S.copy(); s.base(0); s.push(2, 1); s.push(4, 2); s.push(6, 3); s.push(8, 4);
    final StuckSML t = S.copy(); t.base(N); t.push(1, 2); t.push(2, 4); t.push(3, 6); t.push(4, 8);
    //stop(s.memoryLayout());
    ok(s.memoryLayout, """
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        72                                      stuck
   2 V        0         8                                  4     currentSize
   3 A        8        32          4                             Keys
   4 V        8         8               0                  2       key
   5 V       16         8               1                  4       key
   6 V       24         8               2                  6       key
   7 V       32         8               3                  8       key
   8 A       40        32          4                             Data
   9 V       40         8               0                  1       data
  10 V       48         8               1                  2       data
  11 V       56         8               2                  3       data
  12 V       64         8               3                  4       data
""");

    //stop(t.memoryLayout());
    ok(t.memoryLayout, """
Line T       At      Wide       Size    Indices        Value   Name
   1 S       72        72                                      stuck
   2 V       72         8                                  4     currentSize
   3 A       80        32          4                             Keys
   4 V       80         8               0                  1       key
   5 V       88         8               1                  2       key
   6 V       96         8               2                  3       key
   7 V      104         8               3                  4       key
   8 A      112        32          4                             Data
   9 V      112         8               0                  2       data
  10 V      120         8               1                  4       data
  11 V      128         8               2                  6       data
  12 V      136         8               3                  8       data
""");

    //stop(S.memory());
    ok(S.memoryLayout.memory, """
      4... 4... 4... 4... 3... 3... 3... 3... 2... 2... 2... 2... 1... 1... 1... 1...
Line  FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210
   0  0000 0000 0000 0000 0000 0000 0000 0806 0402 0403 0201 0404 0302 0108 0604 0204
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
    test_at();
   }

  static void newTests()                                                        // Tests being worked on
   {oldTests();
    //test_at();
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
