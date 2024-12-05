//------------------------------------------------------------------------------
// A fixed size stack in a bit memory parameterized by a structure in bit memory
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout  a binary tree on a silicon chip.
// rename Transaction tKey, tData to this.key, this.data where needed
abstract class StuckSA extends Test                                             // A fixed size stack of ordered key, data pairs with null deemed highest
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

  static boolean debug;                                                         // Debug when true

//D1 Construction                                                               // Create a stuck

  StuckSA()                                                                     // Create the stuck with a maximum number of the specified elements
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

  static StuckSA stuckStatic()                                                  // Create a sample stuck
   {z();
    return new StuckSA()
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
    Layout             tLayout;                                                 // Layout of a transaction against a stuck
    Layout.Variable     search;                                                 // Search key
    Layout.Variable      limit;                                                 // Limit of search
    Layout.Bit          isFull;                                                 // Whether the stuck is currently full
    Layout.Bit         isEmpty;                                                 // Whether the stuck is currently empty
    Layout.Bit           found;                                                 // Whether a matching element was found
    Layout.Variable      index;                                                 // The index from which the key, data pair were retrieved
    Layout.Variable      tKey;                                                  // The retrieved key
    Layout.Variable      tData;                                                 // The retrieved data
    Layout.Variable       base;                                                 // The base of the stuck
    Layout.Variable       size;                                                 // The current size of the stuck
    Layout.Bit           equal;                                                 // The result of an equal operation
    Layout.Structure      temp;                                                 // Transaction intermediate fields
    final MemoryLayout trn = new MemoryLayout(layout());                        // Memory for transaction intemediates
    final MemoryLayout cpy = new MemoryLayout(StuckSA.this.layout);             // Buffer for stuck being manipulated

    Layout layout()
     {z();
      tLayout =  Layout.layout();
       search = tLayout.variable ( "search", bitsPerKey());
        limit = tLayout.variable (  "limit", bitsPerSize());
       isFull = tLayout.bit      ( "isFull");
      isEmpty = tLayout.bit      ("isEmpty");
        found = tLayout.bit      (  "found");
        index = tLayout.variable (  "index", bitsPerSize());
         tKey = tLayout.variable (    "key", bitsPerKey());
        tData = tLayout.variable (   "data", bitsPerData());
         base = tLayout.variable (   "base", bitsPerSize());
         size = tLayout.variable (   "size", bitsPerSize());
        equal = tLayout.bit      (  "equal");
         temp = tLayout.structure("temp", search, limit, isFull, isEmpty, found, index, tKey, tData, base, size, equal);
      return tLayout.compile();
     }

    void size()                                                                 // The current number of key elements in the stuck
     {z(); final MemoryLayout m = memoryLayout(), t = trn, c = cpy;
      t.at(size).move(m.at(currentSize, t.at(base)).setOff());
     }

    void isFull ()                                                              // Check the stuck is full
     {z(); final MemoryLayout m = memoryLayout(), t = trn, c = cpy;
      t.at(size).greaterThanOrEqual(t.constant(maxSize()), t.at(isFull));
     }

    void isEmpty()                                                              // Check the stuck is empty
     {z(); final MemoryLayout m = memoryLayout(), t = trn, c = cpy;
      t.at(size).equal(t.constant(0), t.at(isEmpty));
     }

    void assertNotFull   ()                                                     // Assert the stuck is not full
     {z(); final MemoryLayout m = memoryLayout(), t = trn, c = cpy;
      if (t.at(isFull).getInt() > 0) stop("Full");
     }

    void assertNotEmpty   ()                                                    // Assert the stuck is not empty
     {z(); final MemoryLayout m = memoryLayout(), t = trn, c = cpy;
      if (t.at(isEmpty).getInt() > 0) stop("Empty");
     }

    void assertInNormal  ()                                                     // Check that the index would yield a valid element
     {z(); final MemoryLayout m = memoryLayout(), t = trn, c = cpy;
      final int i = t.at(index).getInt();
      final int s = t.at(size) .getInt();
      if (i < 0 || i >= s) stop("Out of normal range",   i, "for size", s);
     }

    void assertInExtended()                                                     // Check that the index would yield a valid element
     {z(); final MemoryLayout m = memoryLayout(), t = trn, c = cpy;
      final int i = t.at(index).getInt();
      final int s = t.at(size) .getInt();
      if (i < 0 || i > s) stop("Out of extended range", i, "for size", s);
     }

    void inc()                                                                  // Increment the current size
     {z(); final MemoryLayout m = memoryLayout(), t = trn, c = cpy;
      z(); assertNotFull();
      z(); final int s = m.at(currentSize, t.at(base)).setOff().getInt();
                         m.at(currentSize, t.at(base)).setOff().setInt(s+1);
     }

    void dec()                                                                  // Decrement the current size
     {z(); final MemoryLayout m = memoryLayout(), t = trn, c = cpy;
      z(); assertNotEmpty();
      z(); final int s = m.at(currentSize, t.at(base)).setOff().getInt();
                         m.at(currentSize, t.at(base)).setOff().setInt(s-1);
     }

    void clear()                                                                // Zero the current size to clear the stuck
     {z(); final MemoryLayout m = memoryLayout(), t = trn, c = cpy;
      z(); assertNotEmpty();
      z(); m.at(currentSize, t.at(base)).setOff().setInt(0);
      size(); isFull(); isEmpty();
     }

    MemoryLayout.At key ()                                                      // Refer to key
     {z(); final MemoryLayout m = memoryLayout(), t = trn, c = cpy;
      return m.at(key,  t.at(base), t.at(index));
     }

    MemoryLayout.At data()                                                      // Refer to data
     {z(); final MemoryLayout m = memoryLayout(), t = trn, c = cpy;
      return m.at(data, t.at(base), t.at(index));
     }

    void setKey  ()                                                             // Set the indexed key
     {z(); final MemoryLayout m = memoryLayout(), t = trn, c = cpy;
      z(); key().setOff().move(t.at(tKey));
     }

    void setData ()                                                             // Set the indexed data
     {z(); final MemoryLayout m = memoryLayout(), t = trn, c = cpy;
      z(); data().setOff().move(t.at(tData));
     }

    void  setKeyData()                                                          // Set a key, data element in the stuck
     {z(); setKey(); setData();
     }

    void push()                                                                 // Push an element onto the stuck
     {z(); action = "push";
      final MemoryLayout m = memoryLayout(), t = trn, c = cpy;
      size(); isFull(); assertNotFull();
      t.at(index).move(t.at(size));
      setKeyData();
      inc();
      size(); isFull(); isEmpty();
     }

    void unshift()                                                              // Unshift an element onto the stuck
     {z(); action = "unshift";
      size(); isFull(); assertNotFull();
      final MemoryLayout m = memoryLayout(), t = trn, c = cpy;
      t.at(found).setInt(1);
      t.zero();
      m.at(Keys,        t.at(base)).moveUp(t.at(currentSize), c.at(Keys));
      m.at(Data,        t.at(base)).moveUp(t.at(currentSize), c.at(Data));
      inc();
      m.at(index,       t.at(base)).setInt(0);
      setKeyData();
      size(); isFull(); isEmpty();
     }

    void pop()                                                                  // Pop an element from the stuck
     {z(); action = "pop";
      size(); isEmpty(); assertNotEmpty();
      dec();
      final MemoryLayout m = memoryLayout(), t = trn, c = cpy;
      t.at(found).setInt(1);
      t.at(size).dec();
      t.at(index).move(t.at(size));
      t.at(tKey ).move(key ().setOff());
      t.at(tData).move(data().setOff());
      size(); isFull(); isEmpty();
     }

    void shift()                                                                // Shift an element from the stuck
     {z(); action = "shift";
      size(); isEmpty(); assertNotEmpty();
      final MemoryLayout m = memoryLayout(), t = trn, c = cpy;
      t.at(found).setInt(1);
      t.at(index).setInt(0);
      t.at(tKey ).move(key ().setOff());
      t.at(tData).move(data().setOff());

      t.zero();
      m.at(Keys, t.at(base)).setOff().moveDown(t.constant(0), c.at(Keys));
      m.at(Data, t.at(base)).setOff().moveDown(t.constant(0), c.at(Data));
      dec();

      size(); isEmpty(); isFull();
     }

    void elementAt()                                                            // Look up key and data associated with the index in the stuck at the specified base offset in memory
     {z(); action = "elementAt";
      size(); assertInNormal();
      final MemoryLayout m = memoryLayout(), t = trn, c = cpy;
      t.at(found).setInt(1);
      t.at(tKey ).move(key ().setOff());
      t.at(tData).move(data().setOff());
     }

    void setElementAt()                                                         // Set an element either in range or one above the current range
     {z(); action = "setElementAt";
      size();
      final MemoryLayout m = memoryLayout(), t = trn, c = cpy;
      t.at(index).equal(t.at(size), t.at(equal));                               // Extended range
      if (t.at(equal).getInt() > 0)
       {z(); setKeyData(); inc();
        t.at(size).inc();
       }
      else                                                                      // In range
       {z(); assertInNormal(); setKeyData();
       }
      t.at(found).setInt(1);
     }

    void insertElementAt()                                                      // Insert an element at the indicated location shifting all the remaining elements up one
     {z(); action = "insertElementAt";
      size(); isFull();
      assertInExtended();

      final MemoryLayout m = memoryLayout(), t = trn, c = cpy;
      t.zero();
      m.at(Keys, t.at(base)).moveUp(t.at(index), c.at(Keys));
      m.at(Data, t.at(base)).moveUp(t.at(index), c.at(Data));
      m.at(currentSize, t.at(base)).inc();

      setKeyData();
      inc();
      size(); isEmpty(); isFull();
     }

    void removeElementAt()                                                      // Remove an element at the indicated location from the stuck
     {z(); action = "removeElementAt";
      size(); assertInNormal();
      final MemoryLayout m = memoryLayout(), t = trn, c = cpy;
      t.at(found).setInt(1);
      t.at(tKey ).move(key ().setOff());
      t.at(tData).move(data().setOff());

      t.zero();
      m.at(Keys, t.at(base)).moveDown(t.at(index), c.at(Keys));
      m.at(Data, t.at(base)).moveDown(t.at(index), c.at(Data));
      m.at(currentSize, t.at(base)).setOff().dec();

      size(); isEmpty(); isFull();
     }

    void firstElement()                                                         // First element
     {z(); action = "firstElement";
      size(); isEmpty(); assertNotEmpty();
      final MemoryLayout m = memoryLayout(), t = trn, c = cpy;
      t.at(found).setInt(1);
      m.at(index, t.at(base)).setInt(0);
      t.at(tKey ).move(key ().setOff());
      t.at(tData).move(data().setOff());
     }

    void lastElement()                                                          // Last element
     {z(); action = "lastElement";
      final MemoryLayout m = memoryLayout(), t = trn, c = cpy;
      size(); isEmpty(); assertNotEmpty();
      t.at(found).setInt(1);
      t.at(index).move(m.at(currentSize, t.at(base)).setOff());
      t.at(index).dec();
      t.at(tKey ).move(key ().setOff());
      t.at(tData).move(data().setOff());
     }

    void search()                                                               // Search for an element within all elements of the stuck
     {z(); action = "search";
      size();
      final MemoryLayout m = memoryLayout(), t = trn, c = cpy;

      final int s = t.at(size).getInt(), l = t.at(limit).getInt(), L = s-l;     // Limit search if requested

      for (int i = 0; i < L; i++)                                               // Search
       {z();
        t.at(index).setInt(i);
        t.at(tKey ).move(key ().setOff());
        t.at(tKey).equal(t.at(search), t.at(equal));
        if (t.at(equal).getInt() > 0)
         {z();
          t.at(found).setInt(1);
          t.at(tData).move(data().setOff());
          return;
         }
       }
      t.at(found).setInt(0);
     }

    void searchFirstGreaterThanOrEqual()                                        // Search for first greater than or equal
     {z(); action = "searchFirstGreaterThanOrEqual";
      size();

      final MemoryLayout m = memoryLayout(), t = trn, c = cpy;
      final int s = t.at(size).getInt(), l = t.at(limit).getInt(), L = s-l;     // Limit search if requested
      final int S = t.at(search).getInt();
      for (int i = 0; i < L; i++)                                               // Search
       {z();
        t.at(index).setInt(i);
        t.at(tKey ).move(key ().setOff());
        t.at(tKey).greaterThanOrEqual(t.at(search), t.at(equal));
        if (t.at(equal).getInt() > 0)
         {z();
          t.at(found).setInt(1);
          t.at(tKey ).move(key ().setOff());
          t.at(tData).move(data().setOff());
          return;
         }
       }
      t.at(found).setInt(0);
     }

    public String toString()
     {final StringBuilder s = new StringBuilder();
      final MemoryLayout m = memoryLayout(), t = trn, c = cpy;
      s.append("Transaction(");
      s.append(  "action:"+action);
      s.append( " search:"+t.at(search) .getInt());
      s.append(  " limit:"+t.at(limit)  .getInt());
      s.append(  " found:"+t.at(found)  .getInt());
      s.append(  " index:"+t.at(index)  .getInt());
      s.append(    " key:"+t.at(tKey)   .getInt());
      s.append(   " data:"+t.at(tData)  .getInt());
      s.append(   " base:"+t.at(base)   .getInt());
      s.append(   " size:"+t.at(size)   .getInt());
      s.append( " isFull:"+t.at(isFull) .getInt());
      s.append(" isEmpty:"+t.at(isEmpty).getInt());
      s.append(")\n");
      return s.toString();
     }
   }  // Transaction

//D1 Print                                                                      // Print a stuck

  public String toString(int Base)                                              // Print a stuck
   {final StringBuilder s = new StringBuilder();
    z();
    final Transaction  r = new Transaction();
    final MemoryLayout m = memoryLayout(), t = r.trn;
    t.at(r.base).setInt(Base);
    r.size();
    final int N = t.at(r.size).getInt();
    s.append("StuckSA(maxSize:"+maxSize());
    s.append(" size:"+N+")\n");
    for (int i = 0; i < N; i++)                                                 // Each element of stuck
     {z();
      t.at(r.index).setInt(i);
      final int k = r.key().setOff().getInt(), d = r.data().setOff().getInt();
      s.append("  "+i+" key:"+k+" data:"+d+"\n");
     }
    return s.toString();
   }

//D0 Tests                                                                      // Test stuck

  static StuckSA test_load()
   {StuckSA s = stuckStatic();
    final Transaction  t = s.new Transaction();
    final MemoryLayout m = t.trn;
    m.at(t.base).setInt(s.baseAt());
    for (int i = 0; i < 4; i++)
     {m.at(t.tKey ).setInt(2 + 2 * i);
      m.at(t.tData).setInt(1 + 1 * i);
      t.push();
     }
    //stop(s.toString(s.baseAt()));
    ok(s.toString(s.baseAt()), """
StuckSA(maxSize:8 size:4)
  0 key:2 data:1
  1 key:4 data:2
  2 key:6 data:3
  3 key:8 data:4
""");
    return s;
   }

  static void test_clear()
   {StuckSA s = test_load();
    final Transaction t = s.new Transaction();
    final MemoryLayout m = t.trn;
    m.at(t.base).setInt(s.baseAt());
    t.size();
    ok(m.at(t.size).getInt(), 4);
    t.clear();
    ok(s.memoryLayout().at(s.currentSize, m.at(t.base)).setOff().getInt(), 0);
    ok(m.at(t.size).getInt(), 0);
   }

  static void test_push()
   {StuckSA s = stuckStatic();
    final Transaction  t = s.new Transaction();
    final MemoryLayout m = t.trn;
    m.at(t.base).setInt(s.baseAt());
    m.at(t.tKey).setInt(15); m.at(t.tData).setInt( 9); t.push();
    m.at(t.tKey).setInt(14); m.at(t.tData).setInt(10); t.push();
    m.at(t.tKey).setInt(13); m.at(t.tData).setInt(11); t.push();
    m.at(t.tKey).setInt(12); m.at(t.tData).setInt(12); t.push();
    //stop(s.memoryLayout());
    ok(s.memoryLayout(), """
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
    //stop(s.memory());
    ok(s.memory(), """
      4... 4... 4... 4... 3... 3... 3... 3... 2... 2... 2... 2... 1... 1... 1... 1...
Line  FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210
   0  0000 0000 000c 000b 000a 0009 0000 0000 0000 0000 000c 000d 000e 000f 0004 0000
   1  0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000
""");

    //stop(s.toString(t.base));
    ok(s.toString(s.baseAt()), """
StuckSA(maxSize:8 size:4)
  0 key:15 data:9
  1 key:14 data:10
  2 key:13 data:11
  3 key:12 data:12
""");

    m.at(t.tKey).setInt(11); m.at(t.tData).setInt(11); t.push(); ok(m.at(t.isFull).getInt() == 0); ok(m.at(t.isEmpty).getInt() == 0);
    m.at(t.tKey).setInt(10); m.at(t.tData).setInt(10); t.push();
    m.at(t.tKey).setInt( 9); m.at(t.tData).setInt( 9); t.push();
    m.at(t.tKey).setInt( 8); m.at(t.tData).setInt( 8); t.push(); ok(m.at(t.isFull).getInt() == 1);
    sayThisOrStop("Full");
    try {m.at(t.tKey).setInt(7); m.at(t.tData).setInt(7); t.push();} catch(RuntimeException e) {}
   }

  static void test_pop()
   {StuckSA s = test_load();
    final Transaction t = s.new Transaction();
    final MemoryLayout m = t.trn;
    m.at(t.base).setInt(s.baseAt());

    t.pop();
    //stop(t);
    ok(t, """
Transaction(action:pop search:0 limit:0 found:1 index:3 key:8 data:4 base:16 size:3 isFull:0 isEmpty:0)
""");
    //stop(s.toString(s.baseAt()));
    ok(s.toString(s.baseAt()), """
StuckSA(maxSize:8 size:3)
  0 key:2 data:1
  1 key:4 data:2
  2 key:6 data:3
""");

    ok(m.at(t.isEmpty).getInt() == 0);
    t.clear();
    ok(m.at(t.isEmpty).getInt() == 1);
    sayThisOrStop("Empty");
    try {t.pop();} catch(RuntimeException e) {}
   }

  static void test_shift()
   {StuckSA s = test_load();
    final Transaction t = s.new Transaction();
    final MemoryLayout m = t.trn;
    m.at(t.base).setInt(s.baseAt());

    t.shift();
    ok(t, """
Transaction(action:shift search:0 limit:0 found:1 index:0 key:2 data:1 base:16 size:3 isFull:0 isEmpty:0)
""");
    //stop(s.toString(s.baseAt()));
    ok(s.toString(s.baseAt()), """
StuckSA(maxSize:8 size:3)
  0 key:4 data:2
  1 key:6 data:3
  2 key:8 data:4
""");

    ok(m.at(t.isEmpty).getInt() == 0);
    t.clear();
    ok(m.at(t.isEmpty).getInt() == 1);
    sayThisOrStop("Empty");
    try {t.pop();} catch(RuntimeException e) {}
   }

  static void test_unshift()
   {StuckSA            s = test_load();
    final Transaction  t = s.new Transaction();
    final MemoryLayout m = t.trn;
    m.at(t.base).setInt(s.baseAt());

    m.at(t.tKey).setInt(9); m.at(t.tData).setInt(9);
    t.unshift();
    //stop(t);
    ok(t, """
Transaction(action:unshift search:0 limit:0 found:1 index:0 key:9 data:9 base:16 size:5 isFull:0 isEmpty:0)
""");
    //stop(s.toString(s.baseAt()));
    ok(s.toString(s.baseAt()), """
StuckSA(maxSize:8 size:5)
  0 key:9 data:9
  1 key:2 data:1
  2 key:4 data:2
  3 key:6 data:3
  4 key:8 data:4
""");

    ok(m.at(t.isFull).getInt() == 0);
    t.unshift(); t.unshift(); t.unshift();
    ok(m.at(t.isFull).getInt() == 1);
    sayThisOrStop("Full");
    try {t.unshift();} catch(RuntimeException e) {}
   }

  static void test_elementAt()
   {StuckSA            s = test_load();
    final Transaction  t = s.new Transaction();
    final MemoryLayout m = t.trn;
    m.at(t.base).setInt(s.baseAt());

    m.at(t.index).setInt(2);
    t.elementAt();
    //stop(t);
    ok(t, """
Transaction(action:elementAt search:0 limit:0 found:1 index:2 key:6 data:3 base:16 size:4 isFull:0 isEmpty:0)
""");

    m.at(t.index).setInt(-2);
    sayThisOrStop("Out of normal range 65534 for size 4");
    try {t.elementAt();} catch(RuntimeException e) {}

    m.at(t.index).setInt(4);
    sayThisOrStop("Out of normal range 4 for size 4");
    try {t.elementAt();} catch(RuntimeException e) {}
   }

  static void test_set_element_at()
   {StuckSA            s = test_load();
    final Transaction  t = s.new Transaction();
    final MemoryLayout m = t.trn;
    m.at(t.base).setInt(s.baseAt());

    m.at(t.tKey).setInt(22); m.at(t.tData).setInt(33); m.at(t.index).setInt(2);
    t.setElementAt();
    //stop(s.toString(s.baseAt()));
    ok(s.toString(s.baseAt()), """
StuckSA(maxSize:8 size:4)
  0 key:2 data:1
  1 key:4 data:2
  2 key:22 data:33
  3 key:8 data:4
""");

    m.at(t.tKey).setInt(88); m.at(t.tData).setInt(99); m.at(t.index).setInt(4); t.setElementAt();
    //stop(s.toString(s.baseAt()));
    ok(s.toString(s.baseAt()), """
StuckSA(maxSize:8 size:5)
  0 key:2 data:1
  1 key:4 data:2
  2 key:22 data:33
  3 key:8 data:4
  4 key:88 data:99
""");

    m.at(t.index).setInt(-2);
    sayThisOrStop("Out of normal range 65534 for size 5");
    try {t.setElementAt();} catch(RuntimeException e) {}

    m.at(t.index).setInt(6);
    sayThisOrStop("Out of normal range 6 for size 5");
    try {t.setElementAt();} catch(RuntimeException e) {}
   }

  static void test_insert_element_at()
   {StuckSA            s = test_load();
    final Transaction  t = s.new Transaction();
    final MemoryLayout m = t.trn;
    m.at(t.base).setInt(s.baseAt());

    m.at(t.tKey).setInt(9); m.at(t.tData).setInt(9); m.at(t.index).setInt(2); t.insertElementAt();
    //stop(s.toString(s.baseAt()));
    ok(s.toString(s.baseAt()), """
StuckSA(maxSize:8 size:5)
  0 key:2 data:1
  1 key:4 data:2
  2 key:9 data:9
  3 key:6 data:3
  4 key:8 data:4
""");

    m.at(t.tKey).setInt(7); m.at(t.tData).setInt(7); m.at(t.index).setInt(5); t.insertElementAt();
    //stop(s.toString(s.baseAt()));
    ok(s.toString(s.baseAt()), """
StuckSA(maxSize:8 size:6)
  0 key:2 data:1
  1 key:4 data:2
  2 key:9 data:9
  3 key:6 data:3
  4 key:8 data:4
  5 key:7 data:7
""");

    m.at(t.index).setInt(7);
    sayThisOrStop("Out of extended range 7 for size 6");
    try {t.insertElementAt();} catch(RuntimeException e) {}
   }

  static void test_remove_element_at()
   {StuckSA            s = test_load();
    final Transaction  t = s.new Transaction();
    final MemoryLayout m = t.trn;
    m.at(t.base).setInt(s.baseAt());

    m.at(t.index).setInt(2); t.removeElementAt();
    //stop(t);
    ok(t, """
Transaction(action:removeElementAt search:0 limit:0 found:1 index:2 key:6 data:3 base:16 size:3 isFull:0 isEmpty:0)
""");

    //stop(s.toString(s.baseAt()));
    ok(s.toString(s.baseAt()), """
StuckSA(maxSize:8 size:3)
  0 key:2 data:1
  1 key:4 data:2
  2 key:8 data:4
""");

    m.at(t.index).setInt(3);
    sayThisOrStop("Out of normal range 3 for size 3");
    try {t.removeElementAt();} catch(RuntimeException e) {}
   }

  static void test_first_last()
   {StuckSA            s = test_load();
    final Transaction  t = s.new Transaction();
    final MemoryLayout m = t.trn;
    m.at(t.base).setInt(s.baseAt());

    t.firstElement();
    //stop(t);
    ok(t, """
Transaction(action:firstElement search:0 limit:0 found:1 index:0 key:2 data:1 base:16 size:4 isFull:0 isEmpty:0)
""");

    t.lastElement();
    //stop(t);
    ok(t, """
Transaction(action:lastElement search:0 limit:0 found:1 index:3 key:8 data:4 base:16 size:4 isFull:0 isEmpty:0)
""");

    t.clear();
    sayThisOrStop("Empty");
    try {t.firstElement();} catch(RuntimeException e) {}
   }

  static void test_search()
   {StuckSA            s = test_load();
    final Transaction  t = s.new Transaction();
    final MemoryLayout m = t.trn;
    m.at(t.base).setInt(s.baseAt());

    m.at(t.search).setInt(2); t.search();
    //stop(t);
    ok(t, """
Transaction(action:search search:2 limit:0 found:1 index:0 key:2 data:1 base:16 size:4 isFull:0 isEmpty:0)
""");

    m.at(t.search).setInt(3);  t.search();
    //stop(t);
    ok(t, """
Transaction(action:search search:3 limit:0 found:0 index:3 key:8 data:1 base:16 size:4 isFull:0 isEmpty:0)
""");
    m.at(t.search).setInt(8);  t.search();
    //stop(t);
    ok(t, """
Transaction(action:search search:8 limit:0 found:1 index:3 key:8 data:4 base:16 size:4 isFull:0 isEmpty:0)
""");
   }

  static void test_search_except_last()
   {StuckSA            s = test_load();
    final Transaction  t = s.new Transaction();
    final MemoryLayout m = t.trn;
    m.at(t.base).setInt(s.baseAt());

    m.at(t.limit).setInt(1);

    m.at(t.search).setInt(4);  t.search();
    //stop(t);
    ok(t, """
Transaction(action:search search:4 limit:1 found:1 index:1 key:4 data:2 base:16 size:4 isFull:0 isEmpty:0)
""");

    m.at(t.search).setInt(8); t.search();
    //stop(t);
    ok(t, """
Transaction(action:search search:8 limit:1 found:0 index:2 key:6 data:2 base:16 size:4 isFull:0 isEmpty:0)
""");
   }

  static void test_search_first_greater_than_or_equal()
   {StuckSA            s = test_load();
    final Transaction  t = s.new Transaction();
    final MemoryLayout m = t.trn;
    m.at(t.base).setInt(s.baseAt());

    m.at(t.search).setInt(5); t.searchFirstGreaterThanOrEqual();
    //stop(t);
    ok(t, """
Transaction(action:searchFirstGreaterThanOrEqual search:5 limit:0 found:1 index:2 key:6 data:3 base:16 size:4 isFull:0 isEmpty:0)
""");

    m.at(t.search).setInt(7); t.searchFirstGreaterThanOrEqual();
    //stop(t);
    ok(t, """
Transaction(action:searchFirstGreaterThanOrEqual search:7 limit:0 found:1 index:3 key:8 data:4 base:16 size:4 isFull:0 isEmpty:0)
""");
   }

  static void test_search_first_greater_than_or_equal_except_last()
   {StuckSA            s = test_load();
    final Transaction  t = s.new Transaction();
    final MemoryLayout m = t.trn;
    m.at(t.base).setInt(s.baseAt());

    m.at(t.limit).setInt(1);
    m.at(t.search).setInt(5); t.searchFirstGreaterThanOrEqual();
    //stop(t);
    ok(t, """
Transaction(action:searchFirstGreaterThanOrEqual search:5 limit:1 found:1 index:2 key:6 data:3 base:16 size:4 isFull:0 isEmpty:0)
""");

    m.at(t.search).setInt(7); t.searchFirstGreaterThanOrEqual();
    //stop(t);
    ok(t, """
Transaction(action:searchFirstGreaterThanOrEqual search:7 limit:1 found:0 index:2 key:6 data:3 base:16 size:4 isFull:0 isEmpty:0)
""");
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_load();
    test_clear();
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
