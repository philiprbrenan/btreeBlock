//------------------------------------------------------------------------------
// StuckSP parameterized by a single structure in bit memory
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout  a binary tree on a silicon chip.
// Update to latest StuckSA
abstract class StuckSA extends Test                                             // A fixed size stack of ordered key, data pairs
 {abstract Memory memory();                                                     // Memory containing the stuck
  abstract int maxSize();                                                       // The maximum number of entries in the stuck.
  abstract int bitsPerKey();                                                    // The number of bits per key
  abstract int bitsPerData();                                                   // The number of bits per data
  abstract int bitsPerSize();                                                   // The number of bits in size field

  int baseAt() {return 0;}                                                      // Offset the memory for the stuck by this amount

  final Layout layout = layout();                                               // Layout of memory containing the stuck
  Layout.Variable   sKey;                                                       // Key in a stuck
  Layout.Array      Keys;                                                       // Array of keys
  Layout.Variable  sData;                                                       // Data associated with a key
  Layout.Array      Data;                                                       // Array of data associated with the array of keys
  Layout.Variable  currentSize;                                                 // Current size of stuck
  Layout.Structure stuck;                                                       // The stuck itself
  Layout.Variable search;                                                       // Search key
  Layout.Variable  limit;                                                       // Limit of search
  Layout.Bit      isFull;                                                       // Whether the stuck is currently full
  Layout.Bit     isEmpty;                                                       // Whether the stuck is currently empty
  Layout.Bit       found;                                                       // Whether a matching element was found
  Layout.Variable  index;                                                       // The index from which the key, data pair were retrieved
  Layout.Variable   tKey;                                                       // The retrieved key
  Layout.Variable  tData;                                                       // The retrieved data
  Layout.Variable   size;                                                       // The current size of the stuck
  Layout.Bit       equal;                                                       // The result of an equal operation
  Layout.Structure  temp;                                                       // Transaction intermediate fields
  final MemoryLayout trn = new MemoryLayout(transactionLayout());               // Memory for transaction intemediates
  final MemoryLayout cpy = new MemoryLayout(layout);                            // Buffer for stuck being manipulated

  static boolean debug;                                                         // Debug when true

//D1 Construction                                                               // Create a stuck

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

  Layout transactionLayout()                                                    // Layout of temporary memeory used by a transaction
   {z();
    final Layout l = Layout.layout();
     search = l.variable ( "search", bitsPerKey());
      limit = l.variable (  "limit", bitsPerSize());
     isFull = l.bit      ( "isFull");
    isEmpty = l.bit      ("isEmpty");
      found = l.bit      (  "found");
      index = l.variable (  "index", bitsPerSize());
       tKey = l.variable (    "key", bitsPerKey());
      tData = l.variable (   "data", bitsPerData());
       size = l.variable (   "size", bitsPerSize());
      equal = l.bit      (  "equal");
       temp = l.structure("temp", search, limit, isFull, isEmpty, found, index, tKey, tData, size, equal);
    return l.compile();
   }

  MemoryLayout memoryLayout()                                                   // The memory layout of this stuck
   {return new MemoryLayout(memory(), layout, baseAt());
   };

  StuckSA at(int Base)                                                          // Reference a stuck at an offset in memory by copying all the details of a stuck and applying a new base address to make this description reentrant.
   {z();
    final StuckSA  parent = this;
    final int        base = Base;
    final StuckSA   child = new StuckSA()
     {int baseAt     () {return base;}
      int maxSize    () {return parent.maxSize    ();}
      int bitsPerKey () {return parent.bitsPerKey ();}
      int bitsPerData() {return parent.bitsPerData();}
      int bitsPerSize() {return parent.bitsPerSize();}
      Memory memory  () {return parent.memory();}
     };
    child.sKey        = parent.sKey;
    child.Keys        = parent.Keys;
    child.sData       = parent.sData;
    child.Data        = parent.Data;
    child.currentSize = parent.currentSize;
    child.stuck       = parent.stuck;
    return child;
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
     };
   }

  public void ok(String expected) {ok(toString(), expected);}                   // Check the stuck

//D1 Transactions                                                               // Transactions on the stuck

  static class Transaction                                                      // Transaction on a stuck. Kept seperate from teh stuck itself because instances of this class are only needed when performing a transaction, they do not need to be stored for the long term like the stuck.
   {String action;                                                              // Action performed
    StuckSA     s;                                                              // The stuck we are transacting on, allowing this class to be static enabling preallocation followed by configuration rather than allocating memory each time which would require garbage colelction.

    void size()                                                                 // The current number of key elements in the stuck
     {z(); final MemoryLayout t = s.trn;
      t.at(s.size).move(s.memoryLayout().at(s.currentSize));
     }

    void isFull()                                                               // Check the stuck is full
     {z(); final MemoryLayout t = s.trn;
      t.at(s.size).greaterThanOrEqual(t.constant(s.maxSize()), t.at(s.isFull));
     }

    void isEmpty()                                                              // Check the stuck is empty
     {z(); final MemoryLayout t = s.trn;
      t.at(s.size).equal(t.constant(0), t.at(s.isEmpty));
     }

    void assertNotFull()                                                        // Assert the stuck is not full
     {z(); final MemoryLayout t = s.trn;
      if (t.at(s.isFull).getInt() > 0) stop("Full");
     }

    void assertNotEmpty()                                                       // Assert the stuck is not empty
     {z(); final MemoryLayout t = s.trn;
      if (t.at(s.isEmpty).getInt() > 0) stop("Empty");
     }

    void assertInNormal()                                                       // Check that the index would yield a valid element
     {z(); final MemoryLayout t = s.trn;
      final int i = t.at(s.index).getInt();
      final int n = t.at(s.size) .getInt();
      if (i < 0 || i >= n) stop("Out of normal range",   i, "for size", n);
     }

    void assertInExtended()                                                     // Check that the index would yield a valid element
     {z(); final MemoryLayout t = s.trn;
      final int i = t.at(s.index).getInt();
      final int n = t.at(s.size) .getInt();
      if (i < 0 || i > n) stop("Out of extended range", i, "for size", n);
     }

    void inc()                                                                  // Increment the current size
     {z(); final MemoryLayout m = s.memoryLayout(), t = s.trn;
      z(); assertNotFull();
      z(); final int n = m.at(s.currentSize).setOff().getInt();
                         m.at(s.currentSize).setOff().setInt(n+1);
     }

    void dec()                                                                  // Decrement the current size
     {z(); final MemoryLayout m = s.memoryLayout(), t = s.trn;
      z(); assertNotEmpty();
      z(); final int n = m.at(s.currentSize).setOff().getInt();
                         m.at(s.currentSize).setOff().setInt(n-1);
     }

    void clear()                                                                // Zero the current size to clear the stuck
     {z(); final MemoryLayout m = s.memoryLayout(), t = s.trn;
      z(); assertNotEmpty();
      z(); m.at(s.currentSize).setOff().setInt(0);
      sizeFullEmpty();
     }

    MemoryLayout.At key()                                                       // Refer to key
     {z(); final MemoryLayout m = s.memoryLayout(), t = s.trn;
      return m.at(s.sKey, t.at(s.index));
     }

    MemoryLayout.At data()                                                      // Refer to data
     {z(); final MemoryLayout m = s.memoryLayout(), t = s.trn;
      return m.at(s.sData, t.at(s.index));
     }

    void moveKey()                                                              // Move a key from the stuck to this transaction
     {z(); final MemoryLayout t = s.trn; t.at(s.tKey ).move(key ().setOff());
     }

    void moveData()                                                             // Move a key from the stuck to this transaction
     {z(); final MemoryLayout t = s.trn; t.at(s.tData).move(data().setOff());
     }

    void setKey  ()                                                             // Set the indexed key
     {z(); final MemoryLayout t = s.trn;  key().setOff().move(t.at(s.tKey));
     }

    void setData ()                                                             // Set the indexed data
     {z(); final MemoryLayout t = s.trn; data().setOff().move(t.at(s.tData));
     }

    void setKeyData()    {z(); setKey(); setData();}                            // Set a key, data element in the stuck
    void sizeFullEmpty() {z(); size(); isFull(); isEmpty();}                    // Status
    void setFound()      {z(); s.trn.at(s.found).setInt(1);}                    // Set found to true

    void push()                                                                 // Push an element onto the stuck
     {z(); action = "push";
      final MemoryLayout t = s.trn;
      size(); isFull(); assertNotFull();
      t.at(s.index).move(t.at(s.size));
      setKeyData();
      inc();
      sizeFullEmpty();
     }

    void unshift()                                                              // Unshift an element onto the stuck
     {z(); action = "unshift";
      size(); isFull(); assertNotFull();
      final MemoryLayout m = s.memoryLayout(), t = s.trn, c = s.cpy;
      setFound();
      m.at(s.Keys).moveUp(t.at(s.currentSize), c.at(s.Keys));
      m.at(s.Data).moveUp(t.at(s.currentSize), c.at(s.Data));
      inc();
      t.at(s.index).setInt(0);
      setKeyData();
      sizeFullEmpty();
     }

    void pop()                                                                  // Pop an element from the stuck
     {z(); action = "pop";
      size(); isEmpty(); assertNotEmpty();
      dec();
      final MemoryLayout t = s.trn;
      setFound();
      t.at(s.size).dec();
      t.at(s.index).move(t.at(s.size));
      moveKey(); moveData();
      sizeFullEmpty();
     }

    void shift()                                                                // Shift an element from the stuck
     {z(); action = "shift";
      size(); isEmpty(); assertNotEmpty();
      final MemoryLayout m = s.memoryLayout(), t = s.trn, c = s.cpy;
      setFound();
      t.at(s.index).setInt(0);
      moveKey(); moveData();

      t.zero();
      m.at(s.Keys).setOff().moveDown(t.constant(0), c.at(s.Keys));
      m.at(s.Data).setOff().moveDown(t.constant(0), c.at(s.Data));
      dec();

      sizeFullEmpty();
     }

    void elementAt()                                                            // Look up key and data associated with the index in the stuck at the specified base offset in memory
     {z(); action = "elementAt";
      size(); assertInNormal();
      final MemoryLayout t = s.trn;
      setFound();
      moveKey(); moveData();
     }

    void setElementAt()                                                         // Set an element either in range or one above the current range
     {z(); action = "setElementAt";
      size();
      final MemoryLayout t = s.trn;
      t.at(s.index).equal(t.at(s.size), t.at(s.equal));                         // Extended range
      new If(t.at(s.equal).getInt() > 0)
       {void Then()
         {z(); setKeyData(); inc();
          t.at(s.size).inc();
         }
        void Else()                                                             // In range
         {z(); assertInNormal(); setKeyData();
         }
       };
      setFound();
     }

    void insertElementAt()                                                      // Insert an element at the indicated location shifting all the remaining elements up one
     {z(); action = "insertElementAt";
      size(); isFull();
      assertInExtended();

      final MemoryLayout m = s.memoryLayout(), t = s.trn, c = s.cpy;
      t.zero();
      m.at(s.Keys).moveUp(t.at(s.index), c.at(s.Keys));
      m.at(s.Data).moveUp(t.at(s.index), c.at(s.Data));
      m.at(s.currentSize).inc();

      setKeyData();
      sizeFullEmpty();
     }

    void removeElementAt()                                                      // Remove an element at the indicated location from the stuck
     {z(); action = "removeElementAt";
      size(); assertInNormal();
      final MemoryLayout m = s.memoryLayout(), t = s.trn, c = s.cpy;
      setFound();
      moveKey(); moveData();

      t.zero();
      m.at(s.Keys).moveDown(t.at(s.index), c.at(s.Keys));
      m.at(s.Data).moveDown(t.at(s.index), c.at(s.Data));
      m.at(s.currentSize).setOff().dec();

      sizeFullEmpty();
     }

    void firstElement()                                                         // First element
     {z(); action = "firstElement";
      size(); isEmpty(); assertNotEmpty();
      final MemoryLayout m = s.memoryLayout(), t = s.trn;
      setFound();
      t.at(s.index).setInt(0);
      moveKey();
      moveData();
     }

    void lastElement()                                                          // Last element
     {z(); action = "lastElement";
      final MemoryLayout m = s.memoryLayout(), t = s.trn;
      size(); isEmpty(); assertNotEmpty();
      setFound();
      t.at(s.index).move(m.at(s.currentSize).setOff());
      t.at(s.index).dec();
      moveKey(); moveData();
     }

    void search()                                                               // Search for an element within all elements of the stuck
     {z(); action = "search";
      size();
      final MemoryLayout t = s.trn;

      final int n = t.at(s.size).getInt(), l = t.at(s.limit).getInt(), L = n-l; // Limit search if requested

      for (int i = 0; i < L; i++)                                               // Search
       {z(); t.at(s.index).setInt(i); moveKey();
        t.at(s.tKey).equal(t.at(s.search), t.at(s.equal));
        if (t.at(s.equal).getInt() > 0)
         {z(); setFound(); moveData();
          return;
         }
       }
      t.at(s.found).setInt(0);
     }

    void searchFirstGreaterThanOrEqual()                                        // Search for first greater than or equal
     {z(); action = "searchFirstGreaterThanOrEqual";
      size();
      final MemoryLayout t = s.trn;

      final int n = t.at(s.size).getInt(), l = t.at(s.limit).getInt(), L = n-l; // Limit search if requested

      for (int i = 0; i < L; i++)                                               // Search
       {z(); t.at(s.index).setInt(i); moveKey();
        t.at(s.tKey).greaterThanOrEqual(t.at(s.search), t.at(s.equal));
        if (t.at(s.equal).getInt() > 0)
         {z(); setFound(); moveKey(); moveData();
          return;
         }
       }
      t.at(s.found).setInt(0);
     }

    public String toString()
     {final StringBuilder S = new StringBuilder();
      final MemoryLayout  m = s.memoryLayout(), t = s.trn, c = s.cpy;
      S.append("Transaction(");
      S.append(  "action:"+action);
      S.append( " search:"+t.at(s.search) .getInt());
      S.append(  " limit:"+t.at(s.limit)  .getInt());
      S.append(  " found:"+t.at(s.found)  .getInt());
      S.append(  " index:"+t.at(s.index)  .getInt());
      S.append(    " key:"+t.at(s.tKey)   .getInt());
      S.append(   " data:"+t.at(s.tData)  .getInt());
      S.append(   " size:"+t.at(s.size)   .getInt());
      S.append( " isFull:"+t.at(s.isFull) .getInt());
      S.append(" isEmpty:"+t.at(s.isEmpty).getInt());
      S.append(")\n");
      return S.toString();
     }
   }  // Transaction

//D1 Print                                                                      // Print a stuck

  public String toString()                                                      // Print a stuck
   {final StringBuilder S = new StringBuilder();
    z();
    final Transaction  r = new Transaction(); r.s = this;
    final MemoryLayout m = memoryLayout(), t = trn;

    r.size();
    final int N = t.at(size).getInt();
    S.append("StuckSA(maxSize:"+maxSize());
    S.append(" size:"+N+")\n");
    for (int i = 0; i < N; i++)                                                 // Each element of stuck
     {z();
      t.at(index).setInt(i);
      final int k = r.key().setOff().getInt(), d = r.data().setOff().getInt();
      S.append("  "+i+" key:"+k+" data:"+d+"\n");
     }
    return S.toString();
   }

//D0 Tests                                                                      // Test stuck

  static StuckSA test_load()
   {StuckSA s = stuckStatic();
    final Transaction  t = new Transaction(); t.s = s;
    final MemoryLayout m = s.trn;
    for (int i = 0; i < 4; i++)
     {m.at(s.tKey ).setInt(2 + 2 * i);
      m.at(s.tData).setInt(1 + 1 * i);
      t.push();
     }
    //stop(s);
    ok(s, """
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
    final Transaction  t = new Transaction(); t.s = s;
    final MemoryLayout m = s.trn;
    t.size();
    ok(m.at(s.size).getInt(), 4);
    t.clear();
    ok(s.memoryLayout().at(s.currentSize).setOff().getInt(), 0);
    ok(m.at(s.size).getInt(), 0);
   }

  static void test_push()
   {StuckSA s = stuckStatic();
    final Transaction  t = new Transaction(); t.s = s;
    final MemoryLayout m = s.trn;
    m.at(s.tKey).setInt(15); m.at(s.tData).setInt( 9); t.push();
    m.at(s.tKey).setInt(14); m.at(s.tData).setInt(10); t.push();
    m.at(s.tKey).setInt(13); m.at(s.tData).setInt(11); t.push();
    m.at(s.tKey).setInt(12); m.at(s.tData).setInt(12); t.push();
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

    //stop(s);
    ok(s, """
StuckSA(maxSize:8 size:4)
  0 key:15 data:9
  1 key:14 data:10
  2 key:13 data:11
  3 key:12 data:12
""");

    m.at(s.tKey).setInt(11); m.at(s.tData).setInt(11); t.push(); ok(m.at(s.isFull).getInt() == 0); ok(m.at(s.isEmpty).getInt() == 0);
    m.at(s.tKey).setInt(10); m.at(s.tData).setInt(10); t.push();
    m.at(s.tKey).setInt( 9); m.at(s.tData).setInt( 9); t.push();
    m.at(s.tKey).setInt( 8); m.at(s.tData).setInt( 8); t.push(); ok(m.at(s.isFull).getInt() == 1);
    sayThisOrStop("Full");
    try {m.at(s.tKey).setInt(7); m.at(s.tData).setInt(7); t.push();} catch(RuntimeException e) {}
   }

  static void test_pop()
   {StuckSA s = test_load();
    final Transaction  t = new Transaction(); t.s = s;
    final MemoryLayout m = s.trn;

    t.pop();
    //stop(t);
    ok(t, """
Transaction(action:pop search:0 limit:0 found:1 index:3 key:8 data:4 size:3 isFull:0 isEmpty:0)
""");
    //stop(s);
    ok(s, """
StuckSA(maxSize:8 size:3)
  0 key:2 data:1
  1 key:4 data:2
  2 key:6 data:3
""");

    ok(m.at(s.isEmpty).getInt() == 0);
    t.clear();
    ok(m.at(s.isEmpty).getInt() == 1);
    sayThisOrStop("Empty");
    try {t.pop();} catch(RuntimeException e) {}
   }

  static void test_shift()
   {StuckSA s = test_load();
    final Transaction  t = new Transaction(); t.s = s;
    final MemoryLayout m = s.trn;

    t.shift();
    ok(t, """
Transaction(action:shift search:0 limit:0 found:1 index:0 key:2 data:1 size:3 isFull:0 isEmpty:0)
""");
    //stop(s);
    ok(s, """
StuckSA(maxSize:8 size:3)
  0 key:4 data:2
  1 key:6 data:3
  2 key:8 data:4
""");

    ok(m.at(s.isEmpty).getInt() == 0);
    t.clear();
    ok(m.at(s.isEmpty).getInt() == 1);
    sayThisOrStop("Empty");
    try {t.pop();} catch(RuntimeException e) {}
   }

  static void test_unshift()
   {StuckSA            s = test_load();
    final Transaction  t = new Transaction(); t.s = s;
    final MemoryLayout m = s.trn;

    m.at(s.tKey).setInt(9); m.at(s.tData).setInt(9);
    t.unshift();
    //stop(t);
    ok(t, """
Transaction(action:unshift search:0 limit:0 found:1 index:0 key:9 data:9 size:5 isFull:0 isEmpty:0)
""");
    //stop(s);
    ok(s, """
StuckSA(maxSize:8 size:5)
  0 key:9 data:9
  1 key:2 data:1
  2 key:4 data:2
  3 key:6 data:3
  4 key:8 data:4
""");

    ok(m.at(s.isFull).getInt() == 0);
    t.unshift(); t.unshift(); t.unshift();
    ok(m.at(s.isFull).getInt() == 1);
    sayThisOrStop("Full");
    try {t.unshift();} catch(RuntimeException e) {}
   }

  static void test_elementAt()
   {StuckSA            s = test_load();
    final Transaction  t = new Transaction(); t.s = s;
    final MemoryLayout m = s.trn;

    m.at(s.index).setInt(2);
    t.elementAt();
    //stop(t);
    ok(t, """
Transaction(action:elementAt search:0 limit:0 found:1 index:2 key:6 data:3 size:4 isFull:0 isEmpty:0)
""");

    m.at(s.index).setInt(-2);
    sayThisOrStop("Out of normal range 65534 for size 4");
    try {t.elementAt();} catch(RuntimeException e) {}

    m.at(s.index).setInt(4);
    sayThisOrStop("Out of normal range 4 for size 4");
    try {t.elementAt();} catch(RuntimeException e) {}
   }

  static void test_set_element_at()
   {StuckSA            s = test_load();
    final Transaction  t = new Transaction(); t.s = s;
    final MemoryLayout m = s.trn;

    m.at(s.tKey).setInt(22); m.at(s.tData).setInt(33); m.at(s.index).setInt(2);
    t.setElementAt();
    //stop(s);
    ok(s, """
StuckSA(maxSize:8 size:4)
  0 key:2 data:1
  1 key:4 data:2
  2 key:22 data:33
  3 key:8 data:4
""");

    m.at(s.tKey).setInt(88); m.at(s.tData).setInt(99); m.at(s.index).setInt(4); t.setElementAt();
    //stop(s);
    ok(s, """
StuckSA(maxSize:8 size:5)
  0 key:2 data:1
  1 key:4 data:2
  2 key:22 data:33
  3 key:8 data:4
  4 key:88 data:99
""");

    m.at(s.index).setInt(-2);
    sayThisOrStop("Out of normal range 65534 for size 5");
    try {t.setElementAt();} catch(RuntimeException e) {}

    m.at(s.index).setInt(6);
    sayThisOrStop("Out of normal range 6 for size 5");
    try {t.setElementAt();} catch(RuntimeException e) {}
   }

  static void test_insert_element_at()
   {StuckSA            s = test_load();
    final Transaction  t = new Transaction(); t.s = s;
    final MemoryLayout m = s.trn;

    m.at(s.tKey).setInt(9); m.at(s.tData).setInt(9); m.at(s.index).setInt(2); t.insertElementAt();
    //stop(s);
    ok(s, """
StuckSA(maxSize:8 size:5)
  0 key:2 data:1
  1 key:4 data:2
  2 key:9 data:9
  3 key:6 data:3
  4 key:8 data:4
""");

    m.at(s.tKey).setInt(7); m.at(s.tData).setInt(7); m.at(s.index).setInt(5); t.insertElementAt();
    //stop(s);
    ok(s, """
StuckSA(maxSize:8 size:6)
  0 key:2 data:1
  1 key:4 data:2
  2 key:9 data:9
  3 key:6 data:3
  4 key:8 data:4
  5 key:7 data:7
""");

    m.at(s.index).setInt(7);
    sayThisOrStop("Out of extended range 7 for size 6");
    try {t.insertElementAt();} catch(RuntimeException e) {}
   }

  static void test_remove_element_at()
   {StuckSA            s = test_load();
    final Transaction  t = new Transaction(); t.s = s;
    final MemoryLayout m = s.trn;

    m.at(s.index).setInt(2); t.removeElementAt();
    //stop(t);
    ok(t, """
Transaction(action:removeElementAt search:0 limit:0 found:1 index:2 key:6 data:3 size:3 isFull:0 isEmpty:0)
""");

    //stop(s);
    ok(s, """
StuckSA(maxSize:8 size:3)
  0 key:2 data:1
  1 key:4 data:2
  2 key:8 data:4
""");

    m.at(s.index).setInt(3);
    sayThisOrStop("Out of normal range 3 for size 3");
    try {t.removeElementAt();} catch(RuntimeException e) {}
   }

  static void test_first_last()
   {StuckSA            s = test_load();
    final Transaction  t = new Transaction(); t.s = s;
    final MemoryLayout m = s.trn;
    t.firstElement();
    //stop(t);
    ok(t, """
Transaction(action:firstElement search:0 limit:0 found:1 index:0 key:2 data:1 size:4 isFull:0 isEmpty:0)
""");

    t.lastElement();
    //stop(t);
    ok(t, """
Transaction(action:lastElement search:0 limit:0 found:1 index:3 key:8 data:4 size:4 isFull:0 isEmpty:0)
""");

    t.clear();
    sayThisOrStop("Empty");
    try {t.firstElement();} catch(RuntimeException e) {}
   }

  static void test_search()
   {StuckSA            s = test_load();
    final Transaction  t = new Transaction(); t.s = s;
    final MemoryLayout m = s.trn;

    m.at(s.search).setInt(2); t.search();
    //stop(t);
    ok(t, """
Transaction(action:search search:2 limit:0 found:1 index:0 key:2 data:1 size:4 isFull:0 isEmpty:0)
""");

    m.at(s.search).setInt(3);  t.search();
    //stop(t);
    ok(t, """
Transaction(action:search search:3 limit:0 found:0 index:3 key:8 data:1 size:4 isFull:0 isEmpty:0)
""");
    m.at(s.search).setInt(8);  t.search();
    //stop(t);
    ok(t, """
Transaction(action:search search:8 limit:0 found:1 index:3 key:8 data:4 size:4 isFull:0 isEmpty:0)
""");
   }

  static void test_search_except_last()
   {StuckSA            s = test_load();
    final Transaction  t = new Transaction(); t.s = s;
    final MemoryLayout m = s.trn;

    m.at(s.limit).setInt(1);

    m.at(s.search).setInt(4);  t.search();
    //stop(t);
    ok(t, """
Transaction(action:search search:4 limit:1 found:1 index:1 key:4 data:2 size:4 isFull:0 isEmpty:0)
""");

    m.at(s.search).setInt(8); t.search();
    //stop(t);
    ok(t, """
Transaction(action:search search:8 limit:1 found:0 index:2 key:6 data:2 size:4 isFull:0 isEmpty:0)
""");
   }

  static void test_search_first_greater_than_or_equal()
   {StuckSA            s = test_load();
    final Transaction  t = new Transaction(); t.s = s;
    final MemoryLayout m = s.trn;

    m.at(s.search).setInt(5); t.searchFirstGreaterThanOrEqual();
    //stop(t);
    ok(t, """
Transaction(action:searchFirstGreaterThanOrEqual search:5 limit:0 found:1 index:2 key:6 data:3 size:4 isFull:0 isEmpty:0)
""");

    m.at(s.search).setInt(7); t.searchFirstGreaterThanOrEqual();
    //stop(t);
    ok(t, """
Transaction(action:searchFirstGreaterThanOrEqual search:7 limit:0 found:1 index:3 key:8 data:4 size:4 isFull:0 isEmpty:0)
""");
   }

  static void test_search_first_greater_than_or_equal_except_last()
   {StuckSA            s = test_load();
    final Transaction  t = new Transaction(); t.s = s;
    final MemoryLayout m = s.trn;

    m.at(s.limit).setInt(1);
    m.at(s.search).setInt(5); t.searchFirstGreaterThanOrEqual();
    //stop(t);
    ok(t, """
Transaction(action:searchFirstGreaterThanOrEqual search:5 limit:1 found:1 index:2 key:6 data:3 size:4 isFull:0 isEmpty:0)
""");

    m.at(s.search).setInt(7); t.searchFirstGreaterThanOrEqual();
    //stop(t);
    ok(t, """
Transaction(action:searchFirstGreaterThanOrEqual search:7 limit:1 found:0 index:2 key:6 data:3 size:4 isFull:0 isEmpty:0)
""");
   }

  static void test_at()
   {final int      N = 16;
    final StuckSA S = new StuckSA()
     {final Memory memory = new Memory(layout.size()*2);
      int maxSize     () {return 4;}
      int bitsPerKey  () {return 8;}
      int bitsPerData () {return 8;}
      int bitsPerSize () {return 8;}
      int baseAt      () {return 0;}
      Memory memory   () {return memory;}
     };

    final StuckSA       s = S.at(0);
    final Transaction  st = new Transaction(); st.s = s;
    final MemoryLayout sm = s.trn;

    sm.at(s.tKey).setInt(2); sm.at(s.tData).setInt(1); st.push();
    sm.at(s.tKey).setInt(4); sm.at(s.tData).setInt(2); st.push();
    sm.at(s.tKey).setInt(6); sm.at(s.tData).setInt(3); st.push();
    sm.at(s.tKey).setInt(8); sm.at(s.tData).setInt(4); st.push();

    final StuckSA       t = S.at(S.layout.size());
    final Transaction  tt = new Transaction(); tt.s = t;
    final MemoryLayout tm = t.trn;
    tm.at(s.tKey).setInt(1); tm.at(s.tData).setInt(2); tt.push();
    tm.at(s.tKey).setInt(2); tm.at(s.tData).setInt(4); tt.push();
    tm.at(s.tKey).setInt(3); tm.at(s.tData).setInt(6); tt.push();
    tm.at(s.tKey).setInt(4); tm.at(s.tData).setInt(8); tt.push();

    //stop(s.memoryLayout());
    ok(s.memoryLayout(), """
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
    ok(t.memoryLayout(), """
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
    ok(S.memory(), """
      4... 4... 4... 4... 3... 3... 3... 3... 2... 2... 2... 2... 1... 1... 1... 1...
Line  FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210
   0  0000 0000 0000 0000 0000 0000 0000 0806 0402 0403 0201 0404 0302 0108 0604 0204
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
    test_at();
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
