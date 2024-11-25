//------------------------------------------------------------------------------
// A fixed size stack of ordered key, data pairs in a static bit memory
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout  a binary tree on a silicon chip.
// Reference the keys and data through a small mapping table and free stack so that we can manipulate keys and data by a small index rather than copying them directly.
abstract class StuckSML extends Test                                            // A fixed size stack of ordered key, data pairs with null deemed highest
 {abstract Memory memory();                                                     // Memory containing the stuck
  MemoryLayout memoryLayout() {return new MemoryLayout(memory(), layout);};     // The memory layout of this stuck
  abstract int maxSize();                                                       // The maximum number of entries in the stuck.
  abstract int bitsPerKey();                                                    // The number of bits per key
  abstract int bitsPerData();                                                   // The number of bits per data
  abstract int bitsPerSize();                                                   // The number of bits in size field
  int baseAt() {return 0;}                                                      // Offset the meory for the stuck by this amount

  Layout layout;                                                                // Layout of memory containing the stuck
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
  final SearchExceptLast SearchExceptLast1 = new SearchExceptLast();            // Search for an element in the stuck ignoriung the last element
  final SearchExceptLast SearchExceptLast2 = new SearchExceptLast();            // Search for an element in the stuck ignoriung the last element
  final SearchFirstGreaterThanOrEqual SearchFirstGreaterThanOrEqual1 = new SearchFirstGreaterThanOrEqual();  // Search element
  final SearchFirstGreaterThanOrEqual SearchFirstGreaterThanOrEqual2 = new SearchFirstGreaterThanOrEqual();  // Search element
  final SearchFirstGreaterThanOrEqualExceptLast SearchFirstGreaterThanOrEqualExceptLast1 = new SearchFirstGreaterThanOrEqualExceptLast();  // Search element
  final SearchFirstGreaterThanOrEqualExceptLast SearchFirstGreaterThanOrEqualExceptLast2 = new SearchFirstGreaterThanOrEqualExceptLast();  // Search element

  static boolean debug;                                                         // Debug when true
//D1 Construction                                                               // Create a stuck

  StuckSML()                                                                    // Create the stuck with a maximum number of the specified elements
   {z();
    layout = layout();
   }

  Layout layout()                                                               // Layout describing stuck
   {z();
    layout      = Layout.layout();
    key         = layout.variable ("key"        ,         bitsPerKey());
    Keys        = layout.array    ("Keys"       , key,    maxSize());
    data        = layout.variable ("data"       ,         bitsPerData());
    Data        = layout.array    ("Data"       , data,   maxSize());
    currentSize = layout.variable ("currentSize", bitsPerSize());
    stuck       = layout.structure("stuck"      , currentSize, Keys, Data);
    return layout.compile();
   }

  static StuckSML stuckStatic()                                                 // Create a sample stuck
   {z();
    return new StuckSML()
     {final Memory memory = new Memory(layout.size()+baseAt());
      int maxSize     () {return  4;}
      int bitsPerKey  () {return 16;}
      int bitsPerData () {return 16;}
      int bitsPerSize () {return 16;}
      int baseAt ()      {return 16;}                                           // Test an offset stuck
      Memory       memory      () {return memory;}
      MemoryLayout memoryLayout() {return new MemoryLayout(memory, layout, baseAt());}
     };
   }

  public void ok(String expected) {ok(toString(), expected);}                   // Check the stuck

//D1 Characteristics                                                            // Characteristics of the stuck

  int size(int base)                                                            // The current number of key elements in the stuck
   {z(); final int s = memoryLayout().getInt(currentSize, base);
    return s;
   }
  boolean isFull       (int base) {z(); return size(base) > maxSize();}         // Check the stuck is full
  boolean isEmpty      (int base) {z(); return size(base) == 0;}                // Check the stuck is empty
  void assertNotFull   (int base) {z(); if (isFull (base)) stop("Full") ;}      // Assert the stack is not full
  void assertNotEmpty  (int base) {z(); if (isEmpty(base)) stop("Empty");}      // Assert the stack is not empty
  void assertInNormal  (int base, int i) {z(); final int s = size(base); if (i < 0 || i >= s) stop("Out of normal range",   i, "for size", s);} // Check that the index would yield a valid element
  void assertInExtended(int base, int i) {z(); final int s = size(base); if (i < 0 || i >  s) stop("Out of extended range", i, "for size", s);} // Check that the index would yield a valid element

  class CheckOrder extends Limit                                                // Check that all the keys are in order,
   {boolean inOrder;                                                            // Keys are in order
    int  outOfOrder;                                                            // Index of first key out of order
    int        base;                                                            // Position of stuck in memory
    String name() {return "CheckOrder";}                                        // Name of limit

    CheckOrder() {}                                                             // Check that all the keys are in order,

    CheckOrder(int Base)                                                        // Check that all the keys are in order,
     {super(); z(); base = Base;
      boolean looking = true;
      int i = 1, j = limit(base);
      for (;i < j && looking; i++)                                              // Check each key
       {z(); if (key(base, i-1) > key(base, i)) {looking = false; break;}
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
  CheckOrder checkOrder(int base) {z(); return new CheckOrder(base);}           // Check the order of elements

  class CheckOrderExceptLast extends CheckOrder                                 // Check that the keys are in order except for the last one
   {CheckOrderExceptLast(int base)
     {super(base);
     }
    String name() {return "CheckOrderExceptLast";}                              // Name of class
    int limit  (int base) {return size(base)-1;}                                // How far to check
   }
  CheckOrderExceptLast checkOrderExceptLast(int base) {z(); return new CheckOrderExceptLast(base);}

//D1 Memory                                                                     // Actions on memory of stuck

  int  getInt(Layout.Field field,            int base)            {z(); return memoryLayout().getInt(field,        base);}
  int  getInt(Layout.Field field,            int base, int index) {z(); return memoryLayout().getInt(field,        base, index);}

  void setInt(Layout.Field field, int value, int base)            {z();        memoryLayout().setInt(field, value, base);}
  void setInt(Layout.Field field, int value, int base, int index) {z();        memoryLayout().setInt(field, value, base, index);}

  int  key     (int base, int Index)              {z(); return getInt(key,    base, Index);}
  int  data    (int base, int Index)              {z(); return getInt(data,   base, Index);}
  void setKey  (int base, int Index,  int Value)  {z(); setInt (key,  Value,  base, Index);}
  void setData (int base, int Index,  int Value)  {z(); setInt (data, Value,  base, Index);}
  void copyKey (int base, int Target, int Source) {z(); setKey (base, Target, key (base, Source));}
  void copyData(int base, int Target, int Source) {z(); setData(base, Target, data(base, Source));}

  void  setKeyData(int base, int Index, int Key, int Data)
   {z();
    setKey (base, Index, Key);
    setData(base, Index, Data);
   }

  void copyKeyData(int base, int Target, int Source)
   {copyKey (base, Target, Source);
    copyData(base, Target, Source);
   }

//D1 Actions                                                                    // Place and remove data to/from stuck

  void inc  (int base) {z(); assertNotFull (base); final int v = getInt(currentSize, base)+1; setInt(currentSize, v, base);}  // Increment the current size
  void dec  (int base) {z(); assertNotEmpty(base); final int v = getInt(currentSize, base)-1; setInt(currentSize, v, base);}  // Decrement the current size
  void clear(int base) {z();                       final int v = 0                          ; setInt(currentSize, v, base);}  // Clear the stuck

  void push(int base, int key, int data)                                        // Push an element onto the stuck
   {z();
    assertNotFull(base);
    final int cs = getInt(currentSize, base);
    setKeyData(base, cs, key, data);
    inc(base);
   }

  void unshift(int base, int key, int data)                                     // Unshift an element onto the stuck
   {z();
    assertNotFull(base);
    for (int i = size(base); i > 0; --i)                                        // Shift the stuck up one place
     {z();
      copyKeyData(base, i, i-1);
     }
    setKeyData(base, 0, key, data);
    inc(base);
   }

  class Pop                                                                     // Pop a key, data pair from the stuck
   {int index;                                                                  // The index of the popped element
    int key;                                                                    // The popped key
    int data;                                                                   // The popped data
    int base;                                                                   // The base of the stuck
    Pop pop(int Base)
     {z(); base = Base;
      assertNotEmpty(base);
      dec(base);
      index = size(base);
      key   = key (base, index);
      data  = data(base, index);
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
  Pop pop(int base) {return Pop1.pop(base);}

  class Shift                                                                   // Shift a key, data pair from the stuck
   {int key;                                                                    // The shifted key
    int data;                                                                   // The shifted data
    int base;                                                                   // The base of the stuck
    Shift shift(int Base)
     {z(); base = Base;
      assertNotEmpty(base);
      key   = key (base, 0);
      data  = data(base, 0);

      for (int i = 0, j = size(base)-1; i < j; i++)                             // Shift the stuck down one place
       {z(); copyKeyData(base, i, i+1);
       }
      dec(base);
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
  Shift shift1(int base) {z(); return Shift1.shift(base);}
  Shift shift2(int base) {z(); return Shift2.shift(base);}
  Shift shift3(int base) {z(); return Shift3.shift(base);}

  class ElementAt                                                               // Get the indexed element
   {int index;                                                                  // The index from which the key, data pair were retrieved
    int key;                                                                    // The shifted key
    int data;                                                                   // The shifted data
    int base;                                                                   // The base of the stuck
    ElementAt() {}
    ElementAt elementAt(int Base, int Index)                                    // Look up key and data associated with the index in the stuck at the specified base offset in memory
     {z(); index = Index; base = Base;
      assertInNormal(base, index);
      key   = key   (base, index);
      data  = data  (base, index);
      return this;
     }
    ElementAt copy()
     {z(); final ElementAt c = new ElementAt();
      c.index = index; c.key = key; c.data = data; c.base = base;
      return c;
     }
    boolean equals(ElementAt e)
     {z();
      return index == e.index && key == e.key && data == e.data && base == e.base;
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
  ElementAt elementAt1(int Base, int Index) {z(); return ElementAt1.elementAt(Base, Index);}
  ElementAt elementAt2(int Base, int Index) {z(); return ElementAt2.elementAt(Base, Index);}

  void setElementAt(int Base, int key, int data, int Index)                     // Set an element either in range or one above the current range
   {if (Index == size(Base))                                                    // Extended range
     {z(); setKeyData(Base, Index, key, data); inc(Base);
     }
    else                                                                        // In range
     {z(); assertInNormal(Base, Index); setKeyData(Base, Index, key, data);
     }
   }

  void insertElementAt(int base, int key, int data, int Index)                  // Insert an element at the indicated location shifting all the remaining elements up one
   {z(); assertInExtended(base, Index);
    for (int i = size(base); i > Index; --i)                                    // Shift the stuck up one place
     {z(); copyKeyData(base, i, i-1);
     }
    setKeyData(base, Index, key, data);
    inc(base);
   }

  class RemoveElementAt                                                         // Remove the indicated element
   {int index;                                                                  // The index from which the key, data pair were retrieved
    int key;                                                                    // The shifted key
    int data;                                                                   // The shifted data
    int base;                                                                   // The base of the stuck
    RemoveElementAt removeElementAt(int Base, int Index)
     {z(); index = Index; base = Base;
      assertInNormal(base, index);
      key     = key (base, index);
      data    = data(base, index);
      for (int i = index, j = size(base)-1; i < j; i++)                         // Shift the stuck down one place
       {z(); copyKeyData(base, i, i+1);
       }
      dec(base);
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
  RemoveElementAt removeElementAt1(int Base, int Index) {z(); return RemoveElementAt1.removeElementAt(Base, Index);} // Remove the indicated element

  class FirstElement                                                            // First element
   {boolean found;                                                              // Whether there was a first element
    int key;                                                                    // The shifted key
    int data;                                                                   // The shifted data
    int base;                                                                   // The base of the stuck
    FirstElement firstElement(int Base)
     {z(); base = Base;
      found = !isEmpty(base);
      if (found)
       {z();
        key  = key (base, 0);
        data = data(base, 0);
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
  FirstElement firstElement1(int base) {z(); return FirstElement1.firstElement(base);} // First element
  FirstElement firstElement2(int base) {z(); return FirstElement2.firstElement(base);} // First element

  class LastElement                                                             // Last element
   {boolean found;                                                              // Whether there was a last element
    int key;                                                                    // The shifted key
    int data;                                                                   // The shifted data
    int base;                                                                   // The base of the stuck
    LastElement lastElement(int Base)
     {z(); base = Base;
      found = !isEmpty(base);
      final int s = size(base)-1;
      if (found)
       {z();
        key  = key (base, s);
        data = data(base, s);
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
  LastElement lastElement1(int base) {z(); return LastElement1.lastElement(base);} // Last element
  LastElement lastElement2(int base) {z(); return LastElement2.lastElement(base);} // Last element

//D1 Search                                                                     // Search a stuck.

  class Limit                                                                   // Search all of the stuck
   {Limit() {z();}
    String name() {return "Limit";}                                             // Name of limit
    int   limit(int base) {z(); return size(base);}                             // How far to check
   }

  class Search extends Limit                                                    // Search for an element within all elements of the stuck
   {int key;                                                                    // Search key
    boolean found;                                                              // Whether the key was found
    int index ;                                                                 // Index of the located key if any
    int data;                                                                   // Located data if key was found
    String name() {return "Search";}                                            // Name of the search

    Search()                     {z();}                                         // Search for an element within all elements of the stuck

    Search search(int base, int Search)                                         // Search for an element within all elements of the stuck
     {z();
      boolean looking = true;
      key = Search;
      int i = 0, j = limit(base);
      for (; i < j && looking; i++)                                             // Search
       {z(); if (key(base, i) == key) {z();looking = false; break;}
       }
      if (looking) {z(); found = false; index = 0; data = 0;}                   // Search key is bigger than all keys in the stuck
      else {z(); found = true;  index = i; data = data(base, i);}               // Found a greater than or equal key in the stuck
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
  Search search1(int base, int Search) {z(); return Search1.search(base, Search);} // Search for a key ignoring the last element on the stuck
  Search search2(int base, int Search) {z(); return Search2.search(base, Search);} // Search for a key ignoring the last element on the stuck

  class SearchExceptLast extends Search                                         // Search for an element ignoring the last element on the stuck
   {SearchExceptLast                 ()                     {super(); z();}
    SearchExceptLast searchExceptLast(int base, int search) {z(); search(base, search); return this;}
    int   limit(int base) {z(); return size(base)-1;}                           // How much of the stuck to search
    String name()         {z(); return "SearchExceptLast";}                     // Name of the search
   }
  SearchExceptLast searchExceptLast1(int base, int Search) {z(); return SearchExceptLast1.searchExceptLast(base, Search);}  // Search for a key ignoring the last element on the stuck
  SearchExceptLast searchExceptLast2(int base, int Search) {z(); return SearchExceptLast2.searchExceptLast(base, Search);}  // Search for a key ignoring the last element on the stuck

  class SearchFirstGreaterThanOrEqual extends Limit                             // Search for the first key that is greater than or equal
   {int    search;                                                              // Search key
    boolean found;                                                              // Whether the search key was found
    int     index;                                                              // Index of the located key if any
    int       key;                                                              // Located key if the search key was found
    int      data;                                                              // Located data if search key was found
    String name() {return "SearchFirstGreaterThanOrEqual";}                     // Name of the search

    SearchFirstGreaterThanOrEqual searchFirstGreaterThanOrEqual                 // Search for first greater than or equal
     (int base, int Search)
     {z();
      boolean looking = true;
      search = Search;
      int i = 0, j = limit(base);
      for (; i < j && looking; i++)                                             // Search
       {z();
        if (key(base, i) >= search)                                             // Search key is equal or greater to the current key
         {z(); looking = false; break;
         }
       }
      if (looking)                                                              // Search key is bigger than all keys in the stuck except possibly the last one
       {z(); found = false; index = limit(base); key = 0;  data = 0;
       }
      else                                                                      // Found a greater than or equal key in the stuck excluding the last key
       {z(); found = true;  index = i; key = key(base, i); data = data(base, i);
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
  searchFirstGreaterThanOrEqual1(int Base, int Search)
   {z(); return SearchFirstGreaterThanOrEqual1.searchFirstGreaterThanOrEqual(Base, Search);
   }
  SearchFirstGreaterThanOrEqual                                                 // Search for a key
  searchFirstGreaterThanOrEqual2(int Base, int Search)
   {z(); return SearchFirstGreaterThanOrEqual2.searchFirstGreaterThanOrEqual(Base, Search);
   }

  class     SearchFirstGreaterThanOrEqualExceptLast                             // Search for an element ignoring the last element on the stuck
    extends SearchFirstGreaterThanOrEqual
   {SearchFirstGreaterThanOrEqualExceptLast()           {}
    SearchFirstGreaterThanOrEqualExceptLast
    searchFirstGreaterThanOrEqualExceptLast(int Base, int search) {z(); searchFirstGreaterThanOrEqual(Base, search); return this;}   // Search for a key ignoring the last element on the stuck
    int   limit(int base) {z(); return size(base)-1;}                           // How much of the stuck to search
    String name()         {z(); return "SearchFirstGreaterThanOrEqualExceptLast";}      // Name of the search
   }
  SearchFirstGreaterThanOrEqualExceptLast                                       // Search for a key ignoring the last element on the stuck
  searchFirstGreaterThanOrEqualExceptLast1(int Base, int Search)
   {z(); return SearchFirstGreaterThanOrEqualExceptLast1.searchFirstGreaterThanOrEqualExceptLast(Base, Search);
   }
  SearchFirstGreaterThanOrEqualExceptLast                                       // Search for a key ignoring the last element on the stuck
  searchFirstGreaterThanOrEqualExceptLast2(int Base, int Search)
   {z(); return SearchFirstGreaterThanOrEqualExceptLast2.searchFirstGreaterThanOrEqualExceptLast(Base, Search);
   }

//D1 Print                                                                      // Print a stuck

  public String toString(int Base)
   {final StringBuilder s = new StringBuilder();                                //
    z();
    s.append("StuckSML(maxSize:"+maxSize());
    s.append(" size:"+size(Base)+")\n");
    for (int i = 0, j = size(Base); i < j; i++)                                 // Search
     {z(); s.append("  "+i+" key:"+key(Base, i)+" data:"+data(Base, i)+"\n");
     }
    return s.toString();
   }

//D0 Tests                                                                      // Test stuck

  static StuckSML test_load()
   {StuckSML t = stuckStatic();
    final int base = t.baseAt();
    t.push(base, 2, 1); t.push(base, 4, 2); t.push(base, 6, 3); t.push(base, 8, 4);
    ok(t.toString(base), """
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
    final int base = t.baseAt();
    t.push(base, 15,  9);
    t.push(base, 14, 10);
    t.push(base, 13, 11);
    t.push(base, 12, 12);
    //stop(t.memoryLayout());
    ok(t.memoryLayout(), """
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
    ok(t.memory(), """
      4... 4... 4... 4... 3... 3... 3... 3... 2... 2... 2... 2... 1... 1... 1... 1...
Line  FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210
   0  0000 0000 0000 0000 0000 0000 000c 000b 000a 0009 000c 000d 000e 000f 0004 0000
""");

    //stop(t.toString(base));

    ok(t.toString(base), """
StuckSML(maxSize:4 size:4)
  0 key:15 data:9
  1 key:14 data:10
  2 key:13 data:11
  3 key:12 data:12
""");
   }

  static void test_pop()
   {StuckSML     t = test_load();
    final int base = t.baseAt();
    Pop p = t.pop(base);
    //stop(p);
    ok(p, """
Pop(index:3 key:8 data:4)
""");
    //stop(t);
    ok(t.toString(base), """
StuckSML(maxSize:4 size:3)
  0 key:2 data:1
  1 key:4 data:2
  2 key:6 data:3
""");
   }

  static void test_shift()
   {StuckSML t = test_load();
    final int base = t.baseAt();
    Shift s = t.shift1(base);
    //stop(s);
    ok(s, """
Shift(key:2 data:1)
""");
    //stop(t.toString(base));
    ok(t.toString(base), """
StuckSML(maxSize:4 size:3)
  0 key:4 data:2
  1 key:6 data:3
  2 key:8 data:4
""");
    Shift S = t.shift2(base);
    //stop(s);
    ok(S, """
Shift(key:4 data:2)
""");
    //stop(t.toString(base));
    ok(t.toString(base), """
StuckSML(maxSize:4 size:2)
  0 key:6 data:3
  1 key:8 data:4
""");
    Shift T = t.shift3(base);
    //stop(s);
    ok(T, """
Shift(key:6 data:3)
""");
    //stop(t.toString(base));
    ok(t.toString(base), """
StuckSML(maxSize:4 size:1)
  0 key:8 data:4
""");
   }

  static void test_unshift()
   {StuckSML t = test_load();
    final int base = t.baseAt();
    Pop p = t.pop(base);
    t.unshift(base, 9, 9);
    //stop(t.toString(base));
    ok(t.toString(base), """
StuckSML(maxSize:4 size:4)
  0 key:9 data:9
  1 key:2 data:1
  2 key:4 data:2
  3 key:6 data:3
""");
   }

  static void test_elementAt()
   {StuckSML     t = test_load();
    final int base = t.baseAt();
    ElementAt e = t.elementAt1(base, 2);
    //stop(e);
    ok(e, """
ElementAt(index:2 key:6 data:3)
""");
    ElementAt E = t.elementAt2(base, 3);
    //stop(E);
    ok(E, """
ElementAt(index:3 key:8 data:4)
""");
    ElementAt f = e.copy();
    ok(e.equals(f));
   }

  static void test_insert_element_at()
   {StuckSML t = test_load();
    final int base = t.baseAt();
    Pop p = t.pop(base);
    t.insertElementAt(base, 9, 9, 2);
    //stop(t.toString(base));
    ok(t.toString(base), """
StuckSML(maxSize:4 size:4)
  0 key:2 data:1
  1 key:4 data:2
  2 key:9 data:9
  3 key:6 data:3
""");
    Pop P = t.pop(base);
    t.insertElementAt(base, 7, 7, 3);
    //stop(t.toString(base));
    ok(t.toString(base), """
StuckSML(maxSize:4 size:4)
  0 key:2 data:1
  1 key:4 data:2
  2 key:9 data:9
  3 key:7 data:7
""");
   }

  static void test_remove_element_at()
   {StuckSML t = test_load();
    final int base = t.baseAt();
    RemoveElementAt a = t.removeElementAt1(base, 2);
    //stop(a);
    ok(a, """
RemoveElementAt(index:2 key:6 data:3)
""");
    //stop(t.toString(base));
    ok(t.toString(base), """
StuckSML(maxSize:4 size:3)
  0 key:2 data:1
  1 key:4 data:2
  2 key:8 data:4
""");
   }

  static void test_first_last()
   {StuckSML t = test_load();
    final int base = t.baseAt();
    FirstElement f = t.firstElement1(base);
    //stop(f);
    ok(f, """
FirstElement(found:true key:2 data:1)
""");
    LastElement l = t.lastElement1(base);
    //stop(l);
    ok(l, """
LastElement(found:true key:8 data:4)
""");

    t.clear(base);
    FirstElement F = t.firstElement2(base);
    //stop(F);
    ok(F, """
FirstElement(found:false key:0 data:0)
""");
    LastElement L = t.lastElement2(base);
    //stop(L);
    ok(L, """
LastElement(found:false key:0 data:0)
""");
   }

  static void test_search()
   {StuckSML  t = test_load();
    final int base = t.baseAt();
    //stop(t.toString(base));
    ok(t.toString(base), """
StuckSML(maxSize:4 size:4)
  0 key:2 data:1
  1 key:4 data:2
  2 key:6 data:3
  3 key:8 data:4
""");

    Search s = t.search1(base, 2);
    //stop(s);
    ok(s, """
Search(Search:2 found:true index:0 data:1)
""");

    Search S = t.search2(base, 3);
    //stop(S);
    ok(S, """
Search(Search:3 found:false)
""");
   }

  static void test_search_except_last()
   {StuckSML  t = test_load();
    final int base = t.baseAt();
    //stop(t.toString(base));
    ok(t.toString(base), """
StuckSML(maxSize:4 size:4)
  0 key:2 data:1
  1 key:4 data:2
  2 key:6 data:3
  3 key:8 data:4
""");
    SearchExceptLast s = t.searchExceptLast1(base, 4);
    SearchExceptLast S = t.searchExceptLast2(base, 8);
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
    final int base = t.baseAt();
    //stop(t.toString(base));
    ok(t.toString(base), """
StuckSML(maxSize:4 size:4)
  0 key:2 data:1
  1 key:4 data:2
  2 key:6 data:3
  3 key:8 data:4
""");

    SearchFirstGreaterThanOrEqual b = t.searchFirstGreaterThanOrEqual1(base, 1);
    SearchFirstGreaterThanOrEqual s = t.searchFirstGreaterThanOrEqual2(base, 4);
    //stop(b);
    ok(b, """
SearchFirstGreaterThanOrEqual(Search:1 index:0 found:true key:2 data:1)
""");

    //stop(s);
    ok(s, """
SearchFirstGreaterThanOrEqual(Search:4 index:1 found:true key:4 data:2)
""");

    SearchFirstGreaterThanOrEqual S = t.searchFirstGreaterThanOrEqual1(base, 5);
    SearchFirstGreaterThanOrEqual T = t.searchFirstGreaterThanOrEqual2(base, 7);
    //stop(S);
    ok(S, """
SearchFirstGreaterThanOrEqual(Search:5 index:2 found:true key:6 data:3)
""");

    //stop(T);
    ok(T, """
SearchFirstGreaterThanOrEqual(Search:7 index:3 found:true key:8 data:4)
""");

    SearchFirstGreaterThanOrEqual E = t.searchFirstGreaterThanOrEqual1(base, 9);
    //stop(E);
    ok(E, """
SearchFirstGreaterThanOrEqual(Search:9 index:4 found:false)
""");
   }

  static void test_search_first_greater_than_or_equal_except_last()
   {StuckSML  t = test_load();
    final int base = t.baseAt();
    //stop(t.toString(base));
    ok(t.toString(base), """
StuckSML(maxSize:4 size:4)
  0 key:2 data:1
  1 key:4 data:2
  2 key:6 data:3
  3 key:8 data:4
""");

    SearchFirstGreaterThanOrEqualExceptLast b = t.searchFirstGreaterThanOrEqualExceptLast1(base, 1);
    SearchFirstGreaterThanOrEqualExceptLast s = t.searchFirstGreaterThanOrEqualExceptLast2(base, 4);
    //stop(b);
    ok(b, """
SearchFirstGreaterThanOrEqualExceptLast(Search:1 index:0 found:true key:2 data:1)
""");

    //stop(s);
    ok(s, """
SearchFirstGreaterThanOrEqualExceptLast(Search:4 index:1 found:true key:4 data:2)
""");

    SearchFirstGreaterThanOrEqualExceptLast S = t.searchFirstGreaterThanOrEqualExceptLast1(base, 5);
    SearchFirstGreaterThanOrEqualExceptLast T = t.searchFirstGreaterThanOrEqualExceptLast2(base, 7);
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
    final int base = t.baseAt();
    t.setElementAt(base, 22, 33, 2);
    //stop(t.toString(base));
    ok(t.toString(base), """
StuckSML(maxSize:4 size:4)
  0 key:2 data:1
  1 key:4 data:2
  2 key:22 data:33
  3 key:8 data:4
""");
    t.setElementAt(base, 99, 97, 3);
    //stop(t.toString(base));
    ok(t.toString(base), """
StuckSML(maxSize:4 size:4)
  0 key:2 data:1
  1 key:4 data:2
  2 key:22 data:33
  3 key:99 data:97
""");
    t.pop(base);
    t.setElementAt(base, 88, 87, 3);
    //stop(t.toString(base));
    ok(t.toString(base), """
StuckSML(maxSize:4 size:4)
  0 key:2 data:1
  1 key:4 data:2
  2 key:22 data:33
  3 key:88 data:87
""");
   }

  static void test_check_order()
   {StuckSML     t = test_load();
    final int base = t.baseAt();
    CheckOrder   c = t.checkOrder(base);
    //stop(c);
    ok(c, """
CheckOrder(inOrder:true outOfOrder:0)
""");
    Pop p = t.pop(base);
    t.insertElementAt(base, 99, 97, 1);
    CheckOrder C = t.checkOrder(base);
    //stop(C);
    ok(C, """
CheckOrder(inOrder:false outOfOrder:2)
""");
   }

  static void test_check_order_except_last()
   {StuckSML     t = test_load();
    final int base = t.baseAt();
    CheckOrderExceptLast c = t.checkOrderExceptLast(base);
    ok(c, """
CheckOrderExceptLast(inOrder:true outOfOrder:0)
""");
    t.pop (base);
    t.pop (base);
    t.push(base, 1, 1);
    t.push(base, 2, 2);
    CheckOrderExceptLast C = t.checkOrderExceptLast(base);
    //stop(C);
    ok(C, """
CheckOrderExceptLast(inOrder:false outOfOrder:2)
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
