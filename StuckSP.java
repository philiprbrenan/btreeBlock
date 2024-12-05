//------------------------------------------------------------------------------
// A fixed size stack in bit memory parameterized by a structure
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout  a binary tree on a silicon chip.

abstract class StuckSP extends Test                                             // A fixed size stack of ordered key, data pairs with null deemed highest
 {abstract Memory memory();                                                     // Memory containing the stuck
  MemoryLayout memoryLayout() {return new MemoryLayout(memory(), layout);};     // The memory layout of this stuck
  abstract int maxSize();                                                       // The maximum number of entries in the stuck.
  abstract int bitsPerKey();                                                    // The number of bits per key
  abstract int bitsPerData();                                                   // The number of bits per data
  abstract int bitsPerSize();                                                   // The number of bits in size field
  final MemoryLayout tmp = new MemoryLayout(layout());                          // Temporary storage to hold intermediate results

  int baseAt() {return 0;}                                                      // Offset the meory for the stuck by this amount

  Layout layout;                                                                // Layout of memory containing the stuck
  Layout.Variable  key;                                                         // Key in a stuck
  Layout.Array     Keys;                                                        // Array of keys
  Layout.Variable  data;                                                        // Data associated with a key
  Layout.Array     Data;                                                        // Array of data associated with the array of keys
  Layout.Variable  currentSize;                                                 // Current size of stuck
  Layout.Structure stuck;                                                       // The stuck itself

  static boolean debug;                                                         // Debug when true

//D1 Construction                                                               // Create a stuck

  StuckSP()                                                                     // Create the stuck with a maximum number of the specified elements
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

  static StuckSP stuckStatic()                                                  // Create a sample stuck
   {z();
    return new StuckSP()
     {final Memory memory = new Memory(layout.size()+baseAt());
      int maxSize     () {return  8;}
      int bitsPerKey  () {return 16;}
      int bitsPerData () {return 16;}
      int bitsPerSize () {return 16;}
      int baseAt ()      {return 16;}                                           // Test an offset stuck
      Memory       memory      () {return memory;}
      MemoryLayout memoryLayout() {return new MemoryLayout(memory, layout, baseAt());}
     };
   }

  public void ok(String expected) {ok(toString(), expected);}                   // Check the stuck

//D1 Transactions                                                               // Transactions on the stuck

  class Transaction                                                             // Transaction on a stuck. Kept seprate from teh stuck itself because instances of this class are only needed when performing a transaction, they do not need to be stored for the long term like the stuck.
   {String action;                                                              // Action performed
    int    search;                                                              // Search key
    int     limit;                                                              // Limit of search
    boolean isFull;                                                             // Whether the stuck is currently full
    boolean isEmpty;                                                            // Whether the stuck is currently empty
    boolean found;                                                              // Whether a matcbing element was found
    int     index;                                                              // The index from which the key, data pair were retrieved
    int       key;                                                              // The retrieved key
    int      data;                                                              // The retrieved data
    int      base;                                                              // The base of the stuck
    int      size;                                                              // The current size of the stuck

    void size() {z(); size = memoryLayout().getInt(currentSize, base);}         // The current number of key elements in the stuck

    void isFull () {z(); isFull  = size >= maxSize();}                          // Check the stuck is full
    void isEmpty() {z(); isEmpty = size == 0;}                                  // Check the stuck is empty

    void assertNotFull   () {z(); if (isFull ) stop("Full") ;}                  // Assert the stuck is not full
    void assertNotEmpty  () {z(); if (isEmpty) stop("Empty");}                  // Assert the stuck is not empty
    void assertInNormal  () {z(); if (index < 0 || index >= size) stop("Out of normal range",   index, "for size", size);} // Check that the index would yield a valid element
    void assertInExtended() {z(); if (index < 0 || index >  size) stop("Out of extended range", index, "for size", size);} // Check that the index would yield a valid element

    void inc() {z(); assertNotFull (); final int v = size+1; memoryLayout().setInt(currentSize, v, base);}  // Increment the current size
    void dec() {z(); assertNotEmpty(); final int v = size-1; memoryLayout().setInt(currentSize, v, base);}  // Decrement the current size

    void key () {z(); key  = memoryLayout().getInt(StuckSP.this.key,  base, index);}           // Get key
    void data() {z(); data = memoryLayout().getInt(StuckSP.this.data, base, index);}           // Get data

    void setKey  ()  {z(); memoryLayout().setInt (StuckSP.this.key,  key,  base, index);}
    void setData ()  {z(); memoryLayout().setInt (StuckSP.this.data, data, base, index);}

    void  setKeyData()                                                          // Set a key, data element in the stuck
     {z();
      setKey ();
      setData();
     }

    void clear()                                                                // Clear the stuck
     {z();
      memoryLayout().setInt(currentSize, 0, base);
      size(); isFull(); isEmpty();
     }

    void push()                                                                 // Push an element onto the stuck
     {z(); action = "push";
      size(); isFull(); assertNotFull();
      index = size;
      setKeyData();
      inc();
      size(); isFull(); isEmpty();
     }

    void unshift()                                                              // Unshift an element onto the stuck
     {z(); action = "unshift";
      size(); isFull(); assertNotFull();
      found = true;
      final MemoryLayout M = memoryLayout(), T = tmp;
      T.zero();
      M.at(Keys, base).moveUp(T.at(currentSize), T.at(Keys));
      M.at(Data, base).moveUp(T.at(currentSize), T.at(Data));
      M.at(currentSize, base).inc();
      index = 0;
      setKeyData();
      size(); isFull(); isEmpty();
     }

    void pop()                                                                  // Pop an element from the stuck
     {z(); action = "pop";
      size(); isEmpty(); assertNotEmpty();
      dec();
      found = true;
      index = --size;
      key ();
      data();
      size(); isFull(); isEmpty();
     }

    void shift()                                                                // Shift an element from the stuck
     {z(); action = "shift";
      size(); isEmpty(); assertNotEmpty();
      found = true;
      index = 0;
      key ();
      data();

      final MemoryLayout M = memoryLayout(), T = tmp;
      T.zero();
      M.at(Keys, base).moveDown(T.at(currentSize), T.at(Keys));
      M.at(Data, base).moveDown(T.at(currentSize), T.at(Data));
      M.at(currentSize, base).dec();

      size(); isEmpty(); isFull();
     }

    void elementAt()                                                            // Look up key and data associated with the index in the stuck at the specified base offset in memory
     {z(); action = "elementAt";
      size(); assertInNormal();
      found = true;
      key ();
      data();
     }

    void setElementAt()                                                         // Set an element either in range or one above the current range
     {z(); action = "setElementAt";
      size();
      if (index == size)                                                        // Extended range
       {z(); setKeyData(); inc(); ++size;
       }
      else                                                                      // In range
       {z(); assertInNormal(); setKeyData();
       }
      found = true;
     }

    void insertElementAt()                                                      // Insert an element at the indicated location shifting all the remaining elements up one
     {z(); action = "insertElementAt";
      size(); isFull();
      assertInExtended();

      final MemoryLayout M = memoryLayout(), T = tmp;
      T.zero();
      T.at(currentSize).setInt(index);
      M.at(Keys, base).moveUp(T.at(currentSize), T.at(Keys));
      M.at(Data, base).moveUp(T.at(currentSize), T.at(Data));
      M.at(currentSize, base).inc();

      setKeyData();
      inc();
      size(); isEmpty(); isFull();
     }

    void removeElementAt()                                                      // Remove an element at the indicated location from the stuck
     {z(); action = "removeElementAt";
      size(); assertInNormal();
      found = true;
      key ();
      data();
      final MemoryLayout M = memoryLayout(), T = tmp;
      T.zero();
      T.at(currentSize).setInt(index);
      M.at(Keys, base).moveDown(T.at(currentSize), T.at(Keys));
      M.at(Data, base).moveDown(T.at(currentSize), T.at(Data));
      M.at(currentSize, base).dec();

      size(); isEmpty(); isFull();
     }

    void firstElement()                                                         // First element
     {z(); action = "firstElement";
      size(); isEmpty(); assertNotEmpty();
      found = true;
      index = 0;
      key ();
      data();
     }

    void lastElement()                                                          // Last element
     {z(); action = "lastElement";
      size(); isEmpty(); assertNotEmpty();
      found = true;
      index = size-1;
      key ();
      data();
     }

    void search()                                                               // Search for an element within all elements of the stuck
     {z(); action = "search";
      size();
      boolean looking = true;
      final int j = size-limit;                                                 // Limit search if requested
      for (index = 0; index < j && looking; index++)                            // Search
       {z(); key(); if (key == search) {z(); looking = false; break;}
       }
      found = !looking;
      if (found) {z(); data();}                                                 // Get data associated with equal key
     }

    void searchFirstGreaterThanOrEqual()                                        // Search for first greater than or equal
     {z();  action = "searchFirstGreaterThanOrEqual";
      size();
      boolean looking = true;
      final int j = size-limit;                                                 // Limit search if requested
      for (index = 0; index < j && looking; index++)                            // Search
       {z(); key(); if (key >= search) {z(); looking = false; break;}           // Search key is equal or greater to the current key
       }
      found = !looking;
      if (found) {z(); key(); data();}                                          // Key greater than or equal to and its associated data
     }

    public String toString()
     {final StringBuilder s = new StringBuilder();
      s.append("Transaction(");
      s.append(  "action:"+action);
      s.append( " search:"+search);
      s.append(  " limit:"+limit);
      s.append(  " found:"+found);
      s.append(  " index:"+index);
      s.append(    " key:"+key);
      s.append(   " data:"+data);
      s.append(   " base:"+base);
      s.append(   " size:"+size);
      s.append( " isFull:"+isFull);
      s.append(" isEmpty:"+isEmpty);
      s.append(")\n");
      return s.toString();
     }
   }

//D1 Print                                                                      // Print a stuck

  public String toString(int Base)                                              // Print a stuck
   {final StringBuilder s = new StringBuilder();
    z();
    final Transaction r = new Transaction(); r.base = Base; r.size();
    final int N = r.size;
    s.append("StuckSP(maxSize:"+maxSize());
    s.append(" size:"+N+")\n");
    for (r.index = 0; r.index < N; r.index++)                                   // Each element of stuck
     {z(); r.key(); r.data(); s.append("  "+r.index+" key:"+r.key+" data:"+r.data+"\n");
     }
    return s.toString();
   }

//D0 Tests                                                                      // Test stuck

  static StuckSP test_load()
   {StuckSP t = stuckStatic();
    final Transaction r = t.new Transaction();
    r.base = t.baseAt();
    for (int i = 0; i < 4; i++)
     {r.key = 2 + 2 * i; r.data = 1 + i;
      r.push();
     }
    //stop(t.toString(r.base));
    ok(t.toString(r.base), """
StuckSP(maxSize:8 size:4)
  0 key:2 data:1
  1 key:4 data:2
  2 key:6 data:3
  3 key:8 data:4
""");
    return t;
   }

  static void test_clear()
   {StuckSP t = test_load();
    final Transaction r = t.new Transaction();
    r.base = t.baseAt();
    r.clear();
    ok(r.size,   0);
   }

  static void test_push()
   {StuckSP t = stuckStatic();
    final Transaction r = t.new Transaction();
    r.base = t.baseAt();
    r.key = 15; r.data =  9; r.push();
    r.key = 14; r.data = 10; r.push();
    r.key = 13; r.data = 11; r.push();
    r.key = 12; r.data = 12; r.push();
    //stop(t.memoryLayout());
    ok(t.memoryLayout(), """
Line T       At      Wide       Size    Indices        Value   Name
   1 S       16       272                                      stuck
   2 V       16        16                                  4     currentSize
   3 A       32       128          8                             Keys
   4 V       32        16               0                 15       key
   5 V       48        16               1                 14       key
   6 V       64        16               2                 13       key
   7 V       80        16               3                 12       key
   8 V       96        16               4                  0       key
   9 V      112        16               5                  0       key
  10 V      128        16               6                  0       key
  11 V      144        16               7                  0       key
  12 A      160       128          8                             Data
  13 V      160        16               0                  9       data
  14 V      176        16               1                 10       data
  15 V      192        16               2                 11       data
  16 V      208        16               3                 12       data
  17 V      224        16               4                  0       data
  18 V      240        16               5                  0       data
  19 V      256        16               6                  0       data
  20 V      272        16               7                  0       data
""");
    //stop(t.memory());
    ok(t.memory(), """
      4... 4... 4... 4... 3... 3... 3... 3... 2... 2... 2... 2... 1... 1... 1... 1...
Line  FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210
   0  0000 0000 000c 000b 000a 0009 0000 0000 0000 0000 000c 000d 000e 000f 0004 0000
   1  0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000
""");

    //stop(t.toString(r.base));
    ok(t.toString(r.base), """
StuckSP(maxSize:8 size:4)
  0 key:15 data:9
  1 key:14 data:10
  2 key:13 data:11
  3 key:12 data:12
""");

    r.key = 11; r.data = 11; r.push(); ok(!r.isFull); ok(!r.isEmpty);
    r.key = 10; r.data = 10; r.push();
    r.key =  9; r.data =  9; r.push();
    r.key =  8; r.data =  8; r.push(); ok(r.isFull);
    sayThisOrStop("Full");
    try {r.key = 7; r.data = 7; r.push();} catch(RuntimeException e) {}
   }

  static void test_pop()
   {StuckSP      t = test_load();
    final Transaction r = t.new Transaction();
    r.base = t.baseAt();

    r.pop();
    //stop(r);
    ok(r, """
Transaction(action:pop search:0 limit:0 found:true index:3 key:8 data:4 base:16 size:3 isFull:false isEmpty:false)
""");
    //stop(t.toString(r.base));
    ok(t.toString(r.base), """
StuckSP(maxSize:8 size:3)
  0 key:2 data:1
  1 key:4 data:2
  2 key:6 data:3
""");

    ok(!r.isEmpty);
    r.clear();
    ok( r.isEmpty);
    sayThisOrStop("Empty");
    try {r.pop();} catch(RuntimeException e) {}
   }


  static void test_shift()
   {StuckSP t = test_load();
    final Transaction r = t.new Transaction();
    r.base = t.baseAt();

    r.shift();
    ok(r, """
Transaction(action:shift search:0 limit:0 found:true index:0 key:2 data:1 base:16 size:3 isFull:false isEmpty:false)
""");
    //stop(t.toString(r.base));
    ok(t.toString(r.base), """
StuckSP(maxSize:8 size:3)
  0 key:4 data:2
  1 key:6 data:3
  2 key:8 data:4
""");

    ok(!r.isEmpty);
    r.clear();
    ok( r.isEmpty);
    sayThisOrStop("Empty");
    try {r.pop();} catch(RuntimeException e) {}
   }

  static void test_unshift()
   {StuckSP t = test_load();
    final Transaction r = t.new Transaction();
    r.base = t.baseAt();

    r.key = 9; r.data = 9; r.unshift();
    //stop(r);
    ok(r, """
Transaction(action:unshift search:0 limit:0 found:true index:0 key:9 data:9 base:16 size:5 isFull:false isEmpty:false)
""");
    //stop(t.toString(r.base));
    ok(t.toString(r.base), """
StuckSP(maxSize:8 size:5)
  0 key:9 data:9
  1 key:2 data:1
  2 key:4 data:2
  3 key:6 data:3
  4 key:8 data:4
""");

    ok(!r.isFull);
    r.unshift(); r.unshift(); r.unshift();
    ok( r.isFull);
    sayThisOrStop("Full");
    try {r.unshift();} catch(RuntimeException e) {}
   }

  static void test_elementAt()
   {StuckSP     t = test_load();
    final Transaction r = t.new Transaction();
    r.base = t.baseAt();
    r.index = 2; r.elementAt();
    //stop(r);
    ok(r, """
Transaction(action:elementAt search:0 limit:0 found:true index:2 key:6 data:3 base:16 size:4 isFull:false isEmpty:false)
""");

    r.index = -2;
    sayThisOrStop("Out of normal range -2 for size 4");
    try {r.elementAt();} catch(RuntimeException e) {}

    r.index = 4;
    sayThisOrStop("Out of normal range 4 for size 4");
    try {r.elementAt();} catch(RuntimeException e) {}
   }

  static void test_set_element_at()
   {StuckSP  t = test_load();
    final Transaction r = t.new Transaction();
    r.base = t.baseAt();

    r.key = 22; r.data = 33; r.index = 2; r.setElementAt();
    //stop(t.toString(r.base));
    ok(t.toString(r.base), """
StuckSP(maxSize:8 size:4)
  0 key:2 data:1
  1 key:4 data:2
  2 key:22 data:33
  3 key:8 data:4
""");

    r.key = 88; r.data = 99; r.index = 4; r.setElementAt();
    //stop(t.toString(r.base));
    ok(t.toString(r.base), """
StuckSP(maxSize:8 size:5)
  0 key:2 data:1
  1 key:4 data:2
  2 key:22 data:33
  3 key:8 data:4
  4 key:88 data:99
""");

    r.index = -2;
    sayThisOrStop("Out of normal range -2 for size 5");
    try {r.setElementAt();} catch(RuntimeException e) {}

    r.index = 6;
    sayThisOrStop("Out of normal range 6 for size 5");
    try {r.setElementAt();} catch(RuntimeException e) {}
   }

  static void test_insert_element_at()
   {StuckSP t = test_load();
    final Transaction r = t.new Transaction();
    r.base = t.baseAt();

    r.key = 9; r.data = 9; r.index = 2; r.insertElementAt();
    //stop(t.toString(r.base));
    ok(t.toString(r.base), """
StuckSP(maxSize:8 size:5)
  0 key:2 data:1
  1 key:4 data:2
  2 key:9 data:9
  3 key:6 data:3
  4 key:8 data:4
""");

    r.key = 7; r.data = 7; r.index = 5; r.insertElementAt();
    //stop(t.toString(r.base));
    ok(t.toString(r.base), """
StuckSP(maxSize:8 size:6)
  0 key:2 data:1
  1 key:4 data:2
  2 key:9 data:9
  3 key:6 data:3
  4 key:8 data:4
  5 key:7 data:7
""");

    r.index = 7;
    sayThisOrStop("Out of extended range 7 for size 6");
    try {r.insertElementAt();} catch(RuntimeException e) {}
   }

  static void test_remove_element_at()
   {StuckSP t = test_load();
    final Transaction r = t.new Transaction();
    r.base = t.baseAt();

    r.index = 2; r.removeElementAt();
    //stop(r);
    ok(r, """
Transaction(action:removeElementAt search:0 limit:0 found:true index:2 key:6 data:3 base:16 size:3 isFull:false isEmpty:false)
""");

    //stop(t.toString(r.base));
    ok(t.toString(r.base), """
StuckSP(maxSize:8 size:3)
  0 key:2 data:1
  1 key:4 data:2
  2 key:8 data:4
""");

    r.index = 3;
    sayThisOrStop("Out of normal range 3 for size 3");
    try {r.removeElementAt();} catch(RuntimeException e) {}
   }

  static void test_first_last()
   {StuckSP t = test_load();
    final Transaction r = t.new Transaction();
    r.base = t.baseAt();

    r.firstElement();
    //stop(r);
    ok(r, """
Transaction(action:firstElement search:0 limit:0 found:true index:0 key:2 data:1 base:16 size:4 isFull:false isEmpty:false)
""");

    r.lastElement();
    //stop(r);
    ok(r, """
Transaction(action:lastElement search:0 limit:0 found:true index:3 key:8 data:4 base:16 size:4 isFull:false isEmpty:false)
""");

    r.clear();
    sayThisOrStop("Empty");
    try {r.firstElement();} catch(RuntimeException e) {}
   }

  static void test_search()
   {StuckSP  t = test_load();
    final Transaction r = t.new Transaction();
    r.base = t.baseAt();

    r.search = 2; r.search();
    //stop(r);
    ok(r, """
Transaction(action:search search:2 limit:0 found:true index:0 key:2 data:1 base:16 size:4 isFull:false isEmpty:false)
""");

    r.search = 3;  r.search();
    //stop(r);
    ok(r, """
Transaction(action:search search:3 limit:0 found:false index:4 key:8 data:1 base:16 size:4 isFull:false isEmpty:false)
""");
    r.search = 8;  r.search();
    //stop(r);
    ok(r, """
Transaction(action:search search:8 limit:0 found:true index:3 key:8 data:4 base:16 size:4 isFull:false isEmpty:false)
""");
   }

  static void test_search_except_last()
   {StuckSP  t = test_load();
    final Transaction r = t.new Transaction();
    r.base  = t.baseAt();
    r.limit = 1;

    r.search = 4;  r.search();
    //stop(r);
    ok(r, """
Transaction(action:search search:4 limit:1 found:true index:1 key:4 data:2 base:16 size:4 isFull:false isEmpty:false)
""");

    r.search = 8; r.search();
    //stop(r);
    ok(r, """
Transaction(action:search search:8 limit:1 found:false index:3 key:6 data:2 base:16 size:4 isFull:false isEmpty:false)
""");
   }

  static void test_search_first_greater_than_or_equal()
   {StuckSP  t = test_load();
    final Transaction r = t.new Transaction();
    r.base = t.baseAt();

    r.search = 5; r.searchFirstGreaterThanOrEqual();
    //stop(r);
    ok(r, """
Transaction(action:searchFirstGreaterThanOrEqual search:5 limit:0 found:true index:2 key:6 data:3 base:16 size:4 isFull:false isEmpty:false)
""");

    r.search = 7; r.searchFirstGreaterThanOrEqual();
    //stop(r);
    ok(r, """
Transaction(action:searchFirstGreaterThanOrEqual search:7 limit:0 found:true index:3 key:8 data:4 base:16 size:4 isFull:false isEmpty:false)
""");
   }

  static void test_search_first_greater_than_or_equal_except_last()
   {StuckSP      t = test_load();
    final Transaction r = t.new Transaction();
    r.base   = t.baseAt();
    r.limit  = 1;
    r.search = 5; r.searchFirstGreaterThanOrEqual();
    //stop(r);
    ok(r, """
Transaction(action:searchFirstGreaterThanOrEqual search:5 limit:1 found:true index:2 key:6 data:3 base:16 size:4 isFull:false isEmpty:false)
""");

    r.search   = 7; r.searchFirstGreaterThanOrEqual();
    //stop(r);
    ok(r, """
Transaction(action:searchFirstGreaterThanOrEqual search:7 limit:1 found:false index:3 key:6 data:3 base:16 size:4 isFull:false isEmpty:false)
""");
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_clear();
    test_push();
    test_pop();
    test_shift();
    test_unshift();
    test_elementAt();
    test_set_element_at();
    test_insert_element_at();
    test_remove_element_at();
    test_first_last();
    test_search();
    test_search_except_last();
    test_search_first_greater_than_or_equal();
    test_search_first_greater_than_or_equal_except_last();
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
