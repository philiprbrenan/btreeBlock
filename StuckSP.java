//------------------------------------------------------------------------------
// StuckSML parameterized by a transaction
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout  a binary tree on a silicon chip.

abstract class StuckSP extends Test                                             // A fixed size stack of ordered key, data pairs
 {abstract int maxSize();                                                       // The maximum number of entries in the stuck.
  abstract int bitsPerKey();                                                    // The number of bits per key
  abstract int bitsPerData();                                                   // The number of bits per data
  abstract int bitsPerSize();                                                   // The number of bits in size field

  final MemoryLayout M = new MemoryLayout();                                    // Memory for stuck
  final MemoryLayout C = new MemoryLayout();                                    // Temporary storage containing a copy of parts of the stuck to allow shifts to occur in parallel

  Layout.Variable  sKey;                                                        // Key in a stuck
  Layout.Array     Keys;                                                        // Array of keys
  Layout.Variable sData;                                                        // Data associated with a key
  Layout.Array     Data;                                                        // Array of data associated with the array of keys
  Layout.Variable  currentSize;                                                 // Current size of stuck
  Layout.Structure stuck;                                                       // The stuck itself
                                                                                // Transactions against the stuck
  String   action;                                                              // The last action performed on the stuck
  int      search;                                                              // Search key to be used
  int       limit;                                                              // Limit of search through stuck from end
  boolean  isFull;                                                              // Whether the stuck is currently full
  boolean isEmpty;                                                              // Whether the stuck is currently empty
  boolean   found;                                                              // Whether a matching element was found
  int       index;                                                              // The index from which the key, data pair were retrieved
  int        tKey;                                                              // The retrieved key
  int       tData;                                                              // The retrieved data
  int        size;                                                              // The current size of the stuck

  static boolean debug;                                                         // Debug when true

//D1 Construction                                                               // Create a stuck

  StuckSP()                                                                     // Create the stuck with a maximum number of the specified elements
   {z();
    M.layout(layout());
    M.memory(new Memory("StuckSP", M.layout.size()));                                      // Can be set after construction to address a different memory. You will also need to call base if the stuck is located some where other than at location zero in memory.
    C.layout(M.layout);
    C.memory(new Memory("StuckSP", C.layout.size()));
   }

  void base(int Base)                                                           // Set the base address of the stuck in the memory layout containing the stuck
   {z();  M.base(Base);
   }

  StuckSP copy()                                                                // Copy a stuck definition
   {z();
    final StuckSP parent = this;
    final StuckSP  child = new StuckSP()
     {int maxSize    () {return parent.maxSize    ();}
      int bitsPerKey () {return parent.bitsPerKey ();}
      int bitsPerData() {return parent.bitsPerData();}
      int bitsPerSize() {return parent.bitsPerSize();}
     };
    child.sKey        = parent.sKey;
    child.Keys        = parent.Keys;
    child.sData       = parent.sData;
    child.Data        = parent.Data;
    child.currentSize = parent.currentSize;
    child.stuck       = parent.stuck;
    child.M.memory(parent.M.memory);
    child.M.layout(parent.M.layout);
    child.M.base  (parent.M.base);
    return child;
   }

  Layout layout()                                                               // Layout describing stuck
   {z();
    final Layout  l = Layout.layout();
    sKey        = l.variable ("key"        ,         bitsPerKey());
    Keys        = l.array    ("Keys"       , sKey,   maxSize());
    sData       = l.variable ("data"       ,         bitsPerData());
    Data        = l.array    ("Data"       , sData,  maxSize());
    currentSize = l.variable ("currentSize", bitsPerSize());
    stuck       = l.structure("stuck"      , currentSize, Keys, Data);
    return l.compile();
   }

//D1 Transactions                                                               // Transactions on the stuck

  void size() {z(); size = M.getInt(currentSize);}                              // The current number of key elements in the stuck

  void isFull () {z(); isFull  = size >= maxSize();}                            // Check the stuck is full
  void isEmpty() {z(); isEmpty = size == 0;}                                    // Check the stuck is empty

  void assertNotFull   () {z(); if (isFull ) stop("Full") ;}                    // Assert the stuck is not full
  void assertNotEmpty  () {z(); if (isEmpty) stop("Empty");}                    // Assert the stuck is not empty
  void assertInNormal  () {z(); if (index < 0 || index >= size) stop("Out of normal range",   index, "for size", size);} // Check that the index would yield a valid element
  void assertInExtended() {z(); if (index < 0 || index >  size) stop("Out of extended range", index, "for size", size);} // Check that the index would yield a valid element

  void inc() {z(); assertNotFull (); final int v = size+1; M.setInt(currentSize, v);}  // Increment the current size
  void dec() {z(); assertNotEmpty(); final int v = size-1; M.setInt(currentSize, v);}  // Decrement the current size

  void key () {z(); tKey  = M.getInt(sKey,  index);}                            // Get key
  void data() {z(); tData = M.getInt(sData, index);}                            // Get data

  void setKey  ()  {z(); M.setInt (sKey,  tKey,  index);}
  void setData ()  {z(); M.setInt (sData, tData, index);}

  void  setKeyData()                                                            // Set a key, data element in the stuck
   {z();
    setKey ();
    setData();
   }

  void clear()                                                                  // Clear the stuck
   {z();
    M.setInt(currentSize, 0);
    size(); isFull(); isEmpty();
   }

  void push()                                                                   // Push an element onto the stuck
   {z(); action = "push";
    size(); isFull(); assertNotFull();
    index = size;
    setKeyData();
    inc();
    size(); isFull(); isEmpty();
   }

  void unshift()                                                                // Unshift an element onto the stuck
   {z(); action = "unshift";
    size(); isFull(); assertNotFull();
    found = true;
    C.zero();
    M.at(Keys).moveUp(C.at(currentSize), C.at(Keys));
    M.at(Data).moveUp(C.at(currentSize), C.at(Data));
    M.at(currentSize).inc();
    index = 0;
    setKeyData();
    size(); isFull(); isEmpty();
   }

  void pop()                                                                    // Pop an element from the stuck
   {z(); action = "pop";
    size(); isEmpty(); assertNotEmpty();
    dec();
    found = true;
    index = --size;
    key ();
    data();
    size(); isFull(); isEmpty();
   }

  void shift()                                                                  // Shift an element from the stuck
   {z(); action = "shift";
    size(); isEmpty(); assertNotEmpty();
    found = true;
    index = 0;
    key ();
    data();

    C.zero();
    M.at(Keys).moveDown(C.at(currentSize), C.at(Keys));
    M.at(Data).moveDown(C.at(currentSize), C.at(Data));
    M.at(currentSize).dec();

    size(); isEmpty(); isFull();
   }

  void elementAt()                                                              // Look up key and data associated with the index in the stuck at the specified base offset in memory
   {z(); action = "elementAt";
    size(); assertInNormal();
    found = true;
    key ();
    data();
   }

  void setElementAt()                                                           // Set an element either in range or one above the current range
   {z(); action = "setElementAt";
    size();
    if (index == size)                                                          // Extended range
     {z(); setKeyData(); inc(); ++size;
     }
    else                                                                        // In range
     {z(); assertInNormal(); setKeyData();
     }
    found = true;
   }

  void insertElementAt()                                                        // Insert an element at the indicated location shifting all the remaining elements up one
   {z(); action = "insertElementAt";
    size(); isFull();
    assertInExtended();

    C.zero();
    C.at(currentSize).setInt(index);
    M.at(Keys).moveUp(C.at(currentSize), C.at(Keys));
    M.at(Data).moveUp(C.at(currentSize), C.at(Data));
    M.at(currentSize).inc();

    setKeyData();
    inc();
    size(); isEmpty(); isFull();
   }

  void removeElementAt()                                                        // Remove an element at the indicated location from the stuck
   {z(); action = "removeElementAt";
    size(); assertInNormal();
    found = true;
    key ();
    data();
    C.zero();
    C.at(currentSize).setInt(index);
    M.at(Keys).moveDown(C.at(currentSize), C.at(Keys));
    M.at(Data).moveDown(C.at(currentSize), C.at(Data));
    M.at(currentSize).dec();

    size(); isEmpty(); isFull();
   }

  void firstElement()                                                           // First element
   {z(); action = "firstElement";
    size(); isEmpty(); assertNotEmpty();
    found = true;
    index = 0;
    key ();
    data();
   }

  void lastElement()                                                            // Last element
   {z(); action = "lastElement";
    size(); isEmpty(); assertNotEmpty();
    found = true;
    index = size-1;
    key ();
    data();
   }

  void search()                                                                 // Search for an element within all elements of the stuck
   {z(); action = "search";
    size();
    boolean looking = true;
    final int j = size-limit;                                                   // Limit search if requested
    for (index = 0; index < j && looking; index++)                              // Search
     {z(); key(); if (tKey == search) {z(); looking = false; break;}
     }
    found = !looking;
    if (found) {z(); data();}                                                   // Get data associated with equal key
   }

  void searchFirstGreaterThanOrEqual()                                          // Search for first greater than or equal
   {z();  action = "searchFirstGreaterThanOrEqual";
    size();
    boolean looking = true;
    final int j = size-limit;                                                   // Limit search if requested
    for (index = 0; index < j && looking; index++)                              // Search
     {z(); key(); if (tKey >= search) {z(); looking = false; break;}            // Search key is equal or greater to the current key
     }
    found = !looking;
    if (found) {z(); key(); data();}                                            // Key greater than or equal to and its associated data
   }

  public String transaction()
   {final StringBuilder s = new StringBuilder();
    s.append("Transaction(");
    s.append(  "action:"+action);
    s.append( " search:"+search);
    s.append(  " limit:"+limit);
    s.append(  " found:"+found);
    s.append(  " index:"+index);
    s.append(    " key:"+tKey);
    s.append(   " data:"+tData);
    s.append(   " size:"+size);
    s.append( " isFull:"+isFull);
    s.append(" isEmpty:"+isEmpty);
    s.append(")\n");
    return s.toString();
   }

//D1 Print                                                                      // Print a stuck

  public String toString()                                                      // Print a stuck
   {final StringBuilder s = new StringBuilder();
    z();
    size();
    final int N = size;
    s.append("StuckSP(maxSize:"+maxSize());
    s.append(" size:"+N+")\n");
    for (index = 0; index < N; index++)                                   // Each element of stuck
     {z(); key(); data(); s.append("  "+index+" key:"+tKey+" data:"+tData+"\n");
     }
    return s.toString();
   }

//D1 Testing                                                                    // Test the stuck

  static StuckSP stuckStatic()                                                  // Create a sample stuck
   {z();
    final int offset = 16;                                                      // To make testing more relevant
    final StuckSP s =  new StuckSP()
     {int maxSize     () {return  8;}
      int bitsPerKey  () {return 16;}
      int bitsPerData () {return 16;}
      int bitsPerSize () {return 16;}
     };
    s.M.memory(new Memory("StuckSP", s.M.layout.size()+offset));
    s.base(offset);
    return s;
   }

  public void ok(String expected) {ok(toString(), expected);}                   // Check the stuck

//D0 Tests                                                                      // Test stuck

  static StuckSP test_load()
   {StuckSP t = stuckStatic();
    for (int i = 0; i < 4; i++)
     {t.tKey = 2 + 2 * i; t.tData = 1 + i;
      t.push();
     }
    //stop(t);
    ok(t, """
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
    t.clear();
    ok(t.size,   0);
   }

  static void test_push()
   {StuckSP t = stuckStatic();
    t.tKey = 15; t.tData =  9; t.push();
    t.tKey = 14; t.tData = 10; t.push();
    t.tKey = 13; t.tData = 11; t.push();
    t.tKey = 12; t.tData = 12; t.push();
    //stop(t.memoryLayout());
    ok(t.M, """
Memory: StuckSP
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
    ok(t.M.memory, """
Memory: StuckSP
      4... 4... 4... 4... 3... 3... 3... 3... 2... 2... 2... 2... 1... 1... 1... 1...
Line  FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210
   0  0000 0000 000c 000b 000a 0009 0000 0000 0000 0000 000c 000d 000e 000f 0004 0000
   1  0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000
""");

    //stop(t));
    ok(t, """
StuckSP(maxSize:8 size:4)
  0 key:15 data:9
  1 key:14 data:10
  2 key:13 data:11
  3 key:12 data:12
""");

    t.tKey = 11; t.tData = 11; t.push(); ok(!t.isFull); ok(!t.isEmpty);
    t.tKey = 10; t.tData = 10; t.push();
    t.tKey =  9; t.tData =  9; t.push();
    t.tKey =  8; t.tData =  8; t.push(); ok(t.isFull);
    sayThisOrStop("Full");
    try {t.tKey = 7; t.tData = 7; t.push();} catch(RuntimeException e) {}
   }

  static void test_pop()
   {StuckSP      t = test_load();
    t.pop();
    //stop(t);
    ok(t.transaction(), """
Transaction(action:pop search:0 limit:0 found:true index:3 key:8 data:4 size:3 isFull:false isEmpty:false)
""");
    //stop(t);
    ok(t, """
StuckSP(maxSize:8 size:3)
  0 key:2 data:1
  1 key:4 data:2
  2 key:6 data:3
""");

    ok(!t.isEmpty);
    t.clear();
    ok( t.isEmpty);
    sayThisOrStop("Empty");
    try {t.pop();} catch(RuntimeException e) {}
   }


  static void test_shift()
   {StuckSP t = test_load();

    t.shift();
    ok(t.transaction(), """
Transaction(action:shift search:0 limit:0 found:true index:0 key:2 data:1 size:3 isFull:false isEmpty:false)
""");
    //stop(t);
    ok(t, """
StuckSP(maxSize:8 size:3)
  0 key:4 data:2
  1 key:6 data:3
  2 key:8 data:4
""");

    ok(!t.isEmpty);
    t.clear();
    ok( t.isEmpty);
    sayThisOrStop("Empty");
    try {t.pop();} catch(RuntimeException e) {}
   }

  static void test_unshift()
   {StuckSP t = test_load();

    t.tKey = 9; t.tData = 9; t.unshift();
    //stop(r);
    ok(t.transaction(), """
Transaction(action:unshift search:0 limit:0 found:true index:0 key:9 data:9 size:5 isFull:false isEmpty:false)
""");
    //stop(t);
    ok(t, """
StuckSP(maxSize:8 size:5)
  0 key:9 data:9
  1 key:2 data:1
  2 key:4 data:2
  3 key:6 data:3
  4 key:8 data:4
""");

    ok(!t.isFull);
    t.unshift(); t.unshift(); t.unshift();
    ok( t.isFull);
    sayThisOrStop("Full");
    try {t.unshift();} catch(RuntimeException e) {}
   }

  static void test_elementAt()
   {StuckSP     t = test_load();
    t.index = 2; t.elementAt();
    //stop(t.transaction());
    ok(t.transaction(), """
Transaction(action:elementAt search:0 limit:0 found:true index:2 key:6 data:3 size:4 isFull:false isEmpty:false)
""");

    t.index = -2;
    sayThisOrStop("Out of normal range -2 for size 4");
    try {t.elementAt();} catch(RuntimeException e) {}

    t.index = 4;
    sayThisOrStop("Out of normal range 4 for size 4");
    try {t.elementAt();} catch(RuntimeException e) {}
   }

  static void test_set_element_at()
   {StuckSP  t = test_load();

    t.tKey = 22; t.tData = 33; t.index = 2; t.setElementAt();
    //stop(t);
    ok(t, """
StuckSP(maxSize:8 size:4)
  0 key:2 data:1
  1 key:4 data:2
  2 key:22 data:33
  3 key:8 data:4
""");

    t.tKey = 88; t.tData = 99; t.index = 4; t.setElementAt();
    //stop(t);
    ok(t, """
StuckSP(maxSize:8 size:5)
  0 key:2 data:1
  1 key:4 data:2
  2 key:22 data:33
  3 key:8 data:4
  4 key:88 data:99
""");

    t.index = -2;
    sayThisOrStop("Out of normal range -2 for size 5");
    try {t.setElementAt();} catch(RuntimeException e) {}

    t.index = 6;
    sayThisOrStop("Out of normal range 6 for size 5");
    try {t.setElementAt();} catch(RuntimeException e) {}
   }

  static void test_insert_element_at()
   {StuckSP t = test_load();

    t.tKey = 9; t.tData = 9; t.index = 2; t.insertElementAt();
    //stop(t);
    ok(t, """
StuckSP(maxSize:8 size:5)
  0 key:2 data:1
  1 key:4 data:2
  2 key:9 data:9
  3 key:6 data:3
  4 key:8 data:4
""");

    t.tKey = 7; t.tData = 7; t.index = 5; t.insertElementAt();
    //stop(t);
    ok(t, """
StuckSP(maxSize:8 size:6)
  0 key:2 data:1
  1 key:4 data:2
  2 key:9 data:9
  3 key:6 data:3
  4 key:8 data:4
  5 key:7 data:7
""");

    t.index = 7;
    sayThisOrStop("Out of extended range 7 for size 6");
    try {t.insertElementAt();} catch(RuntimeException e) {}
   }

  static void test_remove_element_at()
   {StuckSP t = test_load();

    t.index = 2; t.removeElementAt();
    //stop(t.transaction());
    ok(t.transaction(), """
Transaction(action:removeElementAt search:0 limit:0 found:true index:2 key:6 data:3 size:3 isFull:false isEmpty:false)
""");

    //stop(t);
    ok(t, """
StuckSP(maxSize:8 size:3)
  0 key:2 data:1
  1 key:4 data:2
  2 key:8 data:4
""");

    t.index = 3;
    sayThisOrStop("Out of normal range 3 for size 3");
    try {t.removeElementAt();} catch(RuntimeException e) {}
   }

  static void test_first_last()
   {StuckSP t = test_load();

    t.firstElement();
    //stop(t.transaction());
    ok(t.transaction(), """
Transaction(action:firstElement search:0 limit:0 found:true index:0 key:2 data:1 size:4 isFull:false isEmpty:false)
""");

    t.lastElement();
    //stop(t.transaction());
    ok(t.transaction(), """
Transaction(action:lastElement search:0 limit:0 found:true index:3 key:8 data:4 size:4 isFull:false isEmpty:false)
""");

    t.clear();
    sayThisOrStop("Empty");
    try {t.firstElement();} catch(RuntimeException e) {}
   }

  static void test_search()
   {StuckSP  t = test_load();

    t.search = 2; t.search();
    //stop(t.transaction());
    ok(t.transaction(), """
Transaction(action:search search:2 limit:0 found:true index:0 key:2 data:1 size:4 isFull:false isEmpty:false)
""");

    t.search = 3;  t.search();
    //stop(t.transaction());
    ok(t.transaction(), """
Transaction(action:search search:3 limit:0 found:false index:4 key:8 data:1 size:4 isFull:false isEmpty:false)
""");
    t.search = 8;  t.search();
    //stop(t.transaction());
    ok(t.transaction(), """
Transaction(action:search search:8 limit:0 found:true index:3 key:8 data:4 size:4 isFull:false isEmpty:false)
""");
   }

  static void test_search_except_last()
   {StuckSP  t = test_load();
    t.limit = 1;

    t.search = 4;  t.search();
    //stop(t.transaction());
    ok(t.transaction(), """
Transaction(action:search search:4 limit:1 found:true index:1 key:4 data:2 size:4 isFull:false isEmpty:false)
""");

    t.search = 8; t.search();
    //stop(t.transaction());
    ok(t.transaction(), """
Transaction(action:search search:8 limit:1 found:false index:3 key:6 data:2 size:4 isFull:false isEmpty:false)
""");
   }

  static void test_search_first_greater_than_or_equal()
   {StuckSP  t = test_load();

    t.search = 5; t.searchFirstGreaterThanOrEqual();
    //stop(t.transaction());
    ok(t.transaction(), """
Transaction(action:searchFirstGreaterThanOrEqual search:5 limit:0 found:true index:2 key:6 data:3 size:4 isFull:false isEmpty:false)
""");

    t.search = 7; t.searchFirstGreaterThanOrEqual();
    //stop(t.transaction());
    ok(t.transaction(), """
Transaction(action:searchFirstGreaterThanOrEqual search:7 limit:0 found:true index:3 key:8 data:4 size:4 isFull:false isEmpty:false)
""");
   }

  static void test_search_first_greater_than_or_equal_except_last()
   {StuckSP      t = test_load();

    t.limit  = 1;
    t.search = 5; t.searchFirstGreaterThanOrEqual();
    //stop(t.transaction());
    ok(t.transaction(), """
Transaction(action:searchFirstGreaterThanOrEqual search:5 limit:1 found:true index:2 key:6 data:3 size:4 isFull:false isEmpty:false)
""");

    t.search   = 7; t.searchFirstGreaterThanOrEqual();
    //stop(t.transaction());
    ok(t.transaction(), """
Transaction(action:searchFirstGreaterThanOrEqual search:7 limit:1 found:false index:3 key:6 data:3 size:4 isFull:false isEmpty:false)
""");
   }

  static void test_at()
   {final int     N = 16;
    final StuckSP S = new StuckSP()
     {int maxSize     () {return 4;}
      int bitsPerKey  () {return 8;}
      int bitsPerData () {return 8;}
      int bitsPerSize () {return 8;}
      int baseAt      () {return 0;}
     };

    S.M.memory(new Memory("StuckSP", S.M.layout.size()*2));

    final StuckSP      s = S.copy(); s.base(0);

    s.tKey = 2; s.tData = 1; s.push();
    s.tKey = 4; s.tData = 2; s.push();
    s.tKey = 6; s.tData = 3; s.push();
    s.tKey = 8; s.tData = 4; s.push();

    final StuckSP t = S.copy(); t.base(S.M.layout.size());

    t.tKey = 1; t.tData = 2; t.push();
    t.tKey = 2; t.tData = 4; t.push();
    t.tKey = 3; t.tData = 6; t.push();
    t.tKey = 4; t.tData = 8; t.push();
    //stop(s.M);
    ok(s.M, """
Memory: StuckSP
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

    //stop(t.M);
    ok(t.M, """
Memory: StuckSP
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

    //stop(S.M.memory);
    ok(S.M.memory, """
Memory: StuckSP
      4... 4... 4... 4... 3... 3... 3... 3... 2... 2... 2... 2... 1... 1... 1... 1...
Line  FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210
   0  0000 0000 0000 0000 0000 0000 0000 0806 0402 0403 0201 0404 0302 0108 0604 0204
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
    test_at();
   }

  static void newTests()                                                        // Tests being worked on
   {oldTests();
    test_at();
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
