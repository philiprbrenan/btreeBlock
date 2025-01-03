//------------------------------------------------------------------------------
// StuckSA on a bit machine - completed on Christmas Eve!
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Btree in a block on the surface of a silicon chip.

abstract class StuckPA extends Test                                             // A fixed size stack of ordered key, data pairs
 {abstract int maxSize();                                                       // The maximum number of entries in the stuck.
  abstract int bitsPerKey();                                                    // The number of bits per key
  abstract int bitsPerData();                                                   // The number of bits per data
  abstract int bitsPerSize();                                                   // The number of bits in size field

  final String      name;                                                       // Name of the stuck
  final MemoryLayoutPA M;                                                       // Memory for stuck
  final MemoryLayoutPA C;                                                       // Temporary storage containing a copy of parts of the stuck to allow shifts to occur in parallel
  final MemoryLayoutPA T;                                                       // Memory for transaction intermediates
  ProgramPA            P = new ProgramPA();                                     // The program to be written to to describe the actions on the stuck.  The caller can provide a different one as this field is not final

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
  Layout.Variable   full;                                                       // Used by isFull
  Layout.Bit       equal;                                                       // The result of an equal operation
  Layout.Structure  temp;                                                       // Transaction intermediate fields

  String action;                                                                // Last action performed
  static boolean debug;                                                         // Debug when true

//D1 Construction                                                               // Create a stuck

  StuckPA(String Name)                                                          // Create the stuck with a maximum number of the specified elements
   {this(Name, null);
   }

  StuckPA(String Name, MemoryLayoutPA based)                                    // Create the stuck with a maximum number of the specified elements
   {z();
    name = Name;
    final Layout layout = layout(), tl = transactionLayout();                   // Layout out the stuck
    M = new MemoryLayoutPA(layout, name+"_StuckSA_Memory",     based);          // Memory for stuck
    C = new MemoryLayoutPA(layout, name+"_StuckSA_Copy");                       // Temporary storage containing a copy of parts of the stuck to allow shifts to occur in parallel
    T = new MemoryLayoutPA(tl,     name+"_StuckSA_Transaction");                // Memory for transaction intermediates

    program(M.P);
   }

  void base(int Base)                                                           // Set the base address of the stuck in the memory layout containing the stuck
   {z();  M.base(Base);
   }

  void base(MemoryLayoutPA.At Base)                                             // Set the base address of the stuck in the memory layout containing the stuck
   {z();
    P.new I()
     {void   a() {M.base(Base.setOff().result);}
      String v() {return M.baseName() + " = " + Base.verilogAddress()+";" + traceComment();}     // Set the base for this based data structure
     };
   }

  void program(ProgramPA program)                                               // Set the program in which the various components should generate code
   {z();  P = program;
    M.program(P);
    C.program(P);
    T.program(P);
   }

  StuckPA copy()                                                                // Copy a stuck definition
   {z();
    final StuckPA parent = this;
    final StuckPA  child = new StuckPA(parent.name, parent.M)
     {int maxSize    () {return parent.maxSize    ();}
      int bitsPerKey () {return parent.bitsPerKey ();}
      int bitsPerData() {return parent.bitsPerData();}
      int bitsPerSize() {return parent.bitsPerSize();}
     };
    child.program(parent.P);
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

  Layout transactionLayout()                                                    // Layout of temporary memory used by a transaction
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
       full = l.variable (   "full", bitsPerSize());
      equal = l.bit      (  "equal");
       temp = l.structure("temp", search, limit, isFull, isEmpty, found, index, tKey, tData, size, full, equal);
    return l.compile();
   }

//D1 Transactions                                                               // Transactions on the stuck

  void size()                                                                   // The current number of key elements in the stuck
   {z();
    T.at(size).move(M.at(currentSize));
   }

  void isFull()                                                                 // Check the stuck is full
   {z();
    T.setIntInstruction(full, maxSize());
    T.at(size).greaterThanOrEqual(T.at(full), T.at(isFull));
   }

  void isEmpty()                                                                // Check the stuck is empty
   {z();
    T.setIntInstruction(full, 0);
    T.at(size).equal(T.at(full), T.at(isEmpty));
   }

  void assertNotFull()                                                          // Assert the stuck is not full
   {z();
   final StuckPA t = this;
    P.new If(T.at(isFull))
     {void Then()
       {P.halt("Stuck full");
       }
     };
   }

  void assertNotEmpty()                                                         // Assert the stuck is not empty
   {z();
    P.new If(T.at(isEmpty))
     {void Then() {P.halt("Empty");}
     };
   }

  void assertInNormal()                                                         // Check that the index would yield a valid element
   {z();
    P.new I()
     {void a()
       {final int i = T.at(index).getInt();
        final int n = T.at(size) .getInt();
        if (i < 0 || i >= n) stop("Out of normal range",   i, "for size", n, traceBack);
       }
      String v()
       {return "/* assertInNormal */";
       }
     };
   }

  void assertInExtended()                                                       // Check that the index would yield a valid element
   {z();
    P.new I()
     {void a()
       {final int i = T.at(index).getInt();
        final int n = T.at(size) .getInt();
        if (i < 0 || i > n) stop("Out of extended range", i, "for size", n, traceBack);
       }
      String v()
       {return "/* assertInEXtended */";
       }
     };
   }

  void inc()                                                                    // Increment the current size
   {z();
    assertNotFull();
    M.at(currentSize).inc();
   }

  void dec()                                                                    // Decrement the current size
   {z();
    assertNotEmpty();
    M.at(currentSize).dec();
   }

  void clear()                                                                  // Zero the current size to clear the stuck
   {z();
    M.setIntInstruction(currentSize, 0);
    sizeFullEmpty();
   }

  MemoryLayoutPA.At key()                                                       // Refer to key
   {z();
    return M.at(sKey, T.at(index));
   }

  MemoryLayoutPA.At data()                                                      // Refer to data
   {z(); return M.at(sData, T.at(index));
   }

  void moveKey()                                                                // Move a key from the stuck to this transaction
   {z(); T.at(tKey ).move(key ());
   }

  void moveData()                                                               // Move a key from the stuck to this transaction
   {z(); T.at(tData).move(data());
   }

  void setKey  ()                                                               // Set the indexed key
   {z(); key().move(T.at(tKey));
   }

  void setData ()                                                               // Set the indexed data
   {z(); data().move(T.at(tData));
   }

  void setKeyData()    {z(); setKey(); setData();}                              // Set a key, data element in the stuck
  void sizeFullEmpty() {z(); size(); isFull(); isEmpty();}                      // Status
  void setFound()      {z(); T.setIntInstruction(found, 1);}                    // Set found to true

  void push()                                                                   // Push an element onto the stuck
   {z(); action = "push";
    size();
    isFull();
    assertNotFull();
    size();
    T.at(index).move(T.at(size));
    setKeyData();
    inc();
    sizeFullEmpty();
   }

  void unshift()                                                                // Unshift an element onto the stuck
   {z(); action = "unshift";
    size();
    isFull();
    assertNotFull();
    setFound();
    T.setIntInstruction(index, 0);
    M.at(Keys).moveUp(T.at(index), C.at(Keys));
    //T.setIntInstruction(index, 0);
    M.at(Data).moveUp(T.at(index), C.at(Data));
    inc();
    setKeyData();
    sizeFullEmpty();
   }

  void pop()                                                                    // Pop an element from the stuck
   {z(); action = "pop";
    size();
    isEmpty();
    assertNotEmpty();
    dec();
    setFound();
    T.at(size).dec();
    T.at(index).move(T.at(size));
    moveKey(); moveData();
    sizeFullEmpty();
   }

  void shift()                                                                  // Shift an element from the stuck
   {z(); action = "shift";
    size();
    isEmpty();
    assertNotEmpty();
    setFound();
    T.setIntInstruction(index, 0);
    moveKey(); moveData();
    T.zero();
    M.at(Keys).moveDown(T.at(index), C.at(Keys));
    M.at(Data).moveDown(T.at(index), C.at(Data));
    dec();
    sizeFullEmpty();
   }

  void elementAt()                                                              // Look up key and data associated with the index in the stuck at the specified base offset in memory
   {z(); action = "elementAt";
    size();
    assertInNormal();
    setFound();
    moveKey();
    moveData();
   }

  void setElementAt()                                                           // Set an element either in range or one above the current range
   {z(); action = "setElementAt";
    size();
    T.at(index).equal(T.at(size), T.at(equal));                                 // Extended range
    P.new If(T.at(equal))
     {void Then()
       {setKeyData();
        inc();
        T.at(size).inc();
       }
      void Else()                                                               // In range
       {assertInNormal(); setKeyData();
       }
     };
    setFound();
   }

  void insertElementAt()                                                        // Insert an element at the indicated location shifting all the remaining elements up one
   {z(); action = "insertElementAt";

    size();
    isFull();
    assertInExtended();
    T.zero();
    M.at(Keys).moveUp(T.at(index), C.at(Keys));
    M.at(Data).moveUp(T.at(index), C.at(Data));
    M.at(currentSize).inc();
    setKeyData();
    sizeFullEmpty();
   }

  void removeElementAt()                                                        // Remove an element at the indicated location from the stuck
   {z(); action = "removeElementAt";
    size();
    assertInNormal();
    setFound();
    moveKey();
    moveData();
    T.zero();
    M.at(Keys).moveDown(T.at(index), C.at(Keys));
    M.at(Data).moveDown(T.at(index), C.at(Data));
    M.at(currentSize).dec();
    sizeFullEmpty();
   }

  void firstElement()                                                           // First element
   {z(); action = "firstElement";
    size();
    isEmpty();
    assertNotEmpty();
    setFound();
    T.setIntInstruction(index, 0);
    moveKey();
    moveData();
   }

  void lastElement()                                                            // Last element
   {z(); action = "lastElement";
    size();
    isEmpty();
    assertNotEmpty();
    setFound();
    T.at(index).move(M.at(currentSize));
    T.at(index).dec();
    moveKey();
    moveData();
   }

  void search()                                                                 // Search for an element within all elements of the stuck
   {z(); action = "search";
    size();

    P.new If(T.at(limit))                                                       // Better if we had a separate routine for search a branch versus a leaf
     {void Then()
       {T.at(size).dec();
       }
     };

    T.setIntInstruction(found, 0);                                              // Assume we will not find a match

    P.new Block()
     {void code()
       {for (int I = 0; I < maxSize(); I++)                                     // Search
         {final int i = I;
          T.setIntInstruction(index, i);
          T.at(index).equal(T.at(size), T.at(equal));
          P.GoOn(end, T.at(equal));                                             // Reached the upper limit of the stuck
          moveKey();
          T.at(tKey).equal(T.at(search), T.at(equal));
          P.new If (T.at(equal))                                                // Found an equal key
           {void Then()
             {setFound();
              moveData();
              P.Goto(end);
             }
           };
         }
       }
     };
   }

  void searchFirstGreaterThanOrEqual()                                          // Search for first greater than or equal
   {z(); action = "searchFirstGreaterThanOrEqual";
    size();

    P.new If(T.at(limit))                                                       // Better if we had a separate routine for search a branch versus a leaf
     {void Then()
       {T.at(size).dec();
       }
     };

    T.setIntInstruction(found, 0);                                              // Assume we will not find a match
    T.at(index).move(T.at(size));                                               // Index top if no match found

    P.new Block()
     {void code()
       {for (int I = 0; I < maxSize(); I++)                                     // Search
         {final int i = I;
          T.setIntInstruction(index, i);
          T.at(index).equal(T.at(size), T.at(equal));
          P.GoOn(end, T.at(equal));                                             // Reached the upper limit of the stuck
          moveKey();
          T.at(tKey).greaterThanOrEqual(T.at(search), T.at(equal));
          P.new If (T.at(equal))                                                // Found an equal key or greater
           {void Then()
             {setFound();
              moveKey();
              moveData();
              P.Goto(end);
             }
           };
         }
       }
     };
   }

  public String print()
   {final StringBuilder s = new StringBuilder();
    s.append("Transaction(");
    s.append(  "action:"+action);
    s.append( " search:"+T.at(search) .getInt());
    s.append(  " limit:"+T.at(limit)  .getInt());
    s.append(  " found:"+T.at(found)  .getInt());
    s.append(  " index:"+T.at(index)  .getInt());
    s.append(    " key:"+T.at(tKey)   .getInt());
    s.append(   " data:"+T.at(tData)  .getInt());
    s.append(   " size:"+T.at(size)   .getInt());
    s.append( " isFull:"+T.at(isFull) .getInt());
    s.append(" isEmpty:"+T.at(isEmpty).getInt());
    s.append(")\n");
    return s.toString();
   }

//D1 Print                                                                      // Print a stuck

  public String toString()                                                      // Print a stuck
   {final StuckPA  s = this;
    final StuckSML t = new StuckSML()
     {int maxSize    () {return s.maxSize    ();};                              // The maximum number of entries in the stuck.
      int bitsPerKey () {return s.bitsPerKey ();};                              // The number of bits per key
      int bitsPerData() {return s.bitsPerData();};                              // The number of bits per data
      int bitsPerSize() {return s.bitsPerSize();};                              // The number of bits in size field
     };
    t.M.memory(s.M.memory());
    t.base(s.M.base());
    return t.toString();
   }

//D1 Testing                                                                    // Test the stuck

  static StuckPA stuckPA()                                                      // Create a sample stuck
   {z();
    final int   offset = 16;                                                    // To make testing more relevant
    final StuckPA    S = new StuckPA("original")
     {int maxSize     () {return  8;}
      int bitsPerKey  () {return 16;}
      int bitsPerData () {return 16;}
      int bitsPerSize () {return 16;}
     };

    Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", S.M.layout.size()+offset);
    l.compile();

    MemoryLayoutPA   m = new MemoryLayoutPA(l.compile(), "M");

    final StuckPA    T = new StuckPA("copyable", m)
     {int maxSize     () {return  8;}
      int bitsPerKey  () {return 16;}
      int bitsPerData () {return 16;}
      int bitsPerSize () {return 16;}
     };

    final StuckPA    t =  T.copy();
    t.base(offset);

    return t;
   }

  public void ok(String expected) {ok(toString(), expected);}                   // Check the stuck

//D0 Tests                                                                      // Test stuck

  static StuckPA test_load()
   {StuckPA s = stuckPA();

    for (int I = 0; I < 4; I++)
     {final int i = I;
      s.P.new I() {void a()
        {s.T.at(s.tKey ).setInt(2 + 2 * i);
        }};
      s.P.new I() {void a() {s.T.at(s.tData).setInt(1 + 1 * i);}};
      s.push();
     }
    s.P.run(); s.P.clear();
    //stop(s);
    ok(s, """
StuckSML(maxSize:8 size:4)
  0 key:2 data:1
  1 key:4 data:2
  2 key:6 data:3
  3 key:8 data:4
""");
    return s;
   }

  static void test_clear()
   {StuckPA s = test_load();
    s.size();
    ok(s.T.at(s.size).getInt(), 4);
    s.clear();
    s.P.run();
    ok(s.M.at(s.currentSize).setOff(false).getInt(), 0);
    ok(s.T.at(s.size).getInt(), 0);
   }

  static void test_push()
   {StuckPA s = stuckPA();
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(15); s.T.at(s.tData).setInt( 9);}}; s.push();
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(14); s.T.at(s.tData).setInt(10);}}; s.push();
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(13); s.T.at(s.tData).setInt(11);}}; s.push();
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(12); s.T.at(s.tData).setInt(12);}}; s.push();
    s.P.run();
    //stop(s.M);
    ok(""+s.M, """
MemoryLayout: copyable_StuckSA_Memory
Memory      : M
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
    //stop(s.memoryLayout.memory);
    ok(s.M.memory(), """
Memory: M
      4... 4... 4... 4... 3... 3... 3... 3... 2... 2... 2... 2... 1... 1... 1... 1...
Line  FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210
   0  0000 0000 000c 000b 000a 0009 0000 0000 0000 0000 000c 000d 000e 000f 0004 0000
   1  0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000
""");

    //stop(s);
    ok(s, """
StuckSML(maxSize:8 size:4)
  0 key:15 data:9
  1 key:14 data:10
  2 key:13 data:11
  3 key:12 data:12
""");

    s.P.clear();
    s.T.at(s.tKey).setInt(11); s.T.at(s.tData  ).setInt(11); s.push();
    s.P.new I() {void a()  {ok(s.T.at(s.isFull ).getInt() == 0);}};
    s.P.new I() {void a()  {ok(s.T.at(s.isEmpty).getInt() == 0);}};
    s.T.at(s.tKey).setInt(10); s.T.at(s.tData  ).setInt(10); s.push();
    s.T.at(s.tKey).setInt( 9); s.T.at(s.tData  ).setInt( 9); s.push();
    s.T.at(s.tKey).setInt( 8); s.T.at(s.tData  ).setInt( 8); s.push();
    s.P.new I() {void a() {ok(s.T.at(s.isFull  ).getInt() == 1);}};
    s.P.new I() {void a() {sayThisOrStop("Stuck full");}};
    s.T.at(s.tKey).setInt(7); s.T.at(s.tData).setInt(7); s.push();
    try {s.P.run();} catch(RuntimeException e) {}
   }

  static void test_pop()
   {StuckPA s = test_load();

    s.pop();
    s.P.run();
    //stop(s.print());
    ok(s.print(), """
Transaction(action:pop search:0 limit:0 found:1 index:3 key:8 data:4 size:3 isFull:0 isEmpty:0)
""");
    //stop(s);
    ok(s, """
StuckSML(maxSize:8 size:3)
  0 key:2 data:1
  1 key:4 data:2
  2 key:6 data:3
""");

    s.P.clear();
    s.P.new I() {void a() {ok(s.T.at(s.isEmpty).getInt() == 0);}};
    s.clear();
    s.P.new I() {void a() {ok(s.T.at(s.isEmpty).getInt() == 1);}};
    s.P.new I() {void a() {sayThisOrStop("Empty");}};
    s.pop();
    try {s.P.run();} catch(RuntimeException e) {}
   }

  static void test_shift()
   {StuckPA s = test_load();

    s.shift();
    s.P.run(); s.P.clear();
    ok(s.print(), """
Transaction(action:shift search:0 limit:0 found:1 index:0 key:2 data:1 size:3 isFull:0 isEmpty:0)
""");
    //stop(s);
    ok(s, """
StuckSML(maxSize:8 size:3)
  0 key:4 data:2
  1 key:6 data:3
  2 key:8 data:4
""");

    ok(s.T.at(s.isEmpty).getInt() == 0);
    s.clear();
    s.P.run(); s.P.clear();
    ok(s.T.at(s.isEmpty).getInt() == 1);
    sayThisOrStop("Empty");
    s.pop();
    try {s.P.run();} catch(RuntimeException e) {}
   }

  static void test_unshift()
   {StuckPA s = test_load();
    final MemoryLayoutPA m = s.T;

    s.T.at(s.tKey).setInt(9); s.T.at(s.tData).setInt(9);
    s.unshift();
    s.P.run(); s.P.clear();
    //stop(s);
    ok(s.print(), """
Transaction(action:unshift search:0 limit:0 found:1 index:0 key:9 data:9 size:5 isFull:0 isEmpty:0)
""");
    //stop(s);
    ok(s, """
StuckSML(maxSize:8 size:5)
  0 key:9 data:9
  1 key:2 data:1
  2 key:4 data:2
  3 key:6 data:3
  4 key:8 data:4
""");

    ok(s.T.at(s.isFull).getInt() == 0);
    s.unshift(); s.unshift(); s.unshift();
    s.P.run(); s.P.clear();
    ok(s.T.at(s.isFull).getInt() == 1);
    sayThisOrStop("Stuck full");
    s.unshift();
    try {s.P.run();} catch(RuntimeException e) {}
   }

  static void test_elementAt()
   {StuckPA s = test_load();

    s.T.at(s.index).setInt(2);
    s.elementAt();
    s.P.run(); s.P.clear();
    //stop(s);
    ok(s.print(), """
Transaction(action:elementAt search:0 limit:0 found:1 index:2 key:6 data:3 size:4 isFull:0 isEmpty:0)
""");

    s.T.at(s.index).setInt(-2);
    sayThisOrStop("Out of normal range 65534 for size 4");
    s.elementAt();
    try {s.P.run();} catch(RuntimeException e) {} finally {s.P.clear();}

    s.T.at(s.index).setInt(4);
    sayThisOrStop("Out of normal range 4 for size 4");
    try {s.P.run();} catch(RuntimeException e) {} finally {s.P.clear();}
   }

  static void test_set_element_at()
   {StuckPA s = test_load();

    s.P.new I() {void a() {s.T.at(s.tKey ).setInt(22);}};
    s.P.new I() {void a() {s.T.at(s.tData).setInt(33);}};
    s.P.new I() {void a() {s.T.at(s.index).setInt( 2);}};
    s.setElementAt();
    s.P.run(); s.P.clear();
    //stop(s);
    ok(s, """
StuckSML(maxSize:8 size:4)
  0 key:2 data:1
  1 key:4 data:2
  2 key:22 data:33
  3 key:8 data:4
""");

    s.P.new I() {void a() {s.T.at(s.tKey ).setInt(88);}};
    s.P.new I() {void a() {s.T.at(s.tData).setInt(99);}};
    s.P.new I() {void a() {s.T.at(s.index).setInt( 4);}};
    s.setElementAt();
    s.P.run(); s.P.clear();
    //stop(s);
    ok(s, """
StuckSML(maxSize:8 size:5)
  0 key:2 data:1
  1 key:4 data:2
  2 key:22 data:33
  3 key:8 data:4
  4 key:88 data:99
""");

    s.P.new I() {void a() {s.T.at(s.index).setInt(-2);}};
    sayThisOrStop("Out of normal range 65534 for size 5");
    s.setElementAt();
    try {s.P.run();} catch(RuntimeException e) {} finally {s.P.clear();}

    s.P.new I() {void a() {s.T.at(s.index).setInt(6);}};
    sayThisOrStop("Out of normal range 6 for size 5");
    s.setElementAt();
    try {s.P.run();} catch(RuntimeException e) {} finally {s.P.clear();}
   }

  static void test_insert_element_at()
   {StuckPA s = test_load();

    s.P.new I() {void a() {s.T.at(s.tKey ).setInt(9);}};
    s.P.new I() {void a() {s.T.at(s.tData).setInt(9);}};
    s.P.new I() {void a() {s.T.at(s.index).setInt(2);}};
    s.insertElementAt();
    s.P.run(); s.P.clear();
    //stop(s);
    ok(s, """
StuckSML(maxSize:8 size:5)
  0 key:2 data:1
  1 key:4 data:2
  2 key:9 data:9
  3 key:6 data:3
  4 key:8 data:4
""");

    s.P.new I() {void a() {s.T.at(s.tKey ).setInt(7);}};
    s.P.new I() {void a() {s.T.at(s.tData).setInt(7);}};
    s.P.new I() {void a() {s.T.at(s.index).setInt(5);}};
    s.insertElementAt();
    s.P.run(); s.P.clear();
    //stop(s);
    ok(s, """
StuckSML(maxSize:8 size:6)
  0 key:2 data:1
  1 key:4 data:2
  2 key:9 data:9
  3 key:6 data:3
  4 key:8 data:4
  5 key:7 data:7
""");

    s.P.new I() {void a() {s.T.at(s.index).setInt(7);}};
    sayThisOrStop("Out of extended range 7 for size 6");
    s.insertElementAt();
    try {s.P.run();} catch(RuntimeException e) {} finally {s.P.clear();}
   }

  static void test_remove_element_at()
   {StuckPA s = test_load();

    s.P.new I() {void a() {s.T.at(s.index).setInt(2);}};
    s.removeElementAt();
    s.P.run(); s.P.clear();
    //stop(t);
    ok(s.print(), """
Transaction(action:removeElementAt search:0 limit:0 found:1 index:2 key:6 data:3 size:3 isFull:0 isEmpty:0)
""");

    //stop(s);
    ok(s, """
StuckSML(maxSize:8 size:3)
  0 key:2 data:1
  1 key:4 data:2
  2 key:8 data:4
""");

    s.P.new I() {void a() {s.T.at(s.index).setInt(3);}};
    sayThisOrStop("Out of normal range 3 for size 3");
    s.removeElementAt();
    try {s.P.run();} catch(RuntimeException e) {} finally {s.P.clear();}
   }

  static void test_first_last()
   {StuckPA s = test_load();

    s.firstElement();
    s.P.run(); s.P.clear();
    //stop(t);
    ok(s.print(), """
Transaction(action:firstElement search:0 limit:0 found:1 index:0 key:2 data:1 size:4 isFull:0 isEmpty:0)
""");

    s.lastElement();
    s.P.run(); s.P.clear();
    //stop(s);
    ok(s.print(), """
Transaction(action:lastElement search:0 limit:0 found:1 index:3 key:8 data:4 size:4 isFull:0 isEmpty:0)
""");

    s.clear();
    sayThisOrStop("Empty");
    s.firstElement();
    try {s.P.run();} catch(RuntimeException e) {} finally {s.P.clear();}
   }

  static void test_search()
   {StuckPA s = test_load();

    s.P.new I() {void a() {s.T.at(s.search).setInt(2);}};
    s.search();
    s.P.run(); s.P.clear();
    //stop(s);
    ok(s.print(), """
Transaction(action:search search:2 limit:0 found:1 index:0 key:2 data:1 size:4 isFull:0 isEmpty:0)
""");

    s.P.new I() {void a() {s.T.at(s.search).setInt(3);}};
    s.search();
    s.P.run(); s.P.clear();
    //stop(s);
    ok(s.print(), """
Transaction(action:search search:3 limit:0 found:0 index:4 key:8 data:1 size:4 isFull:0 isEmpty:0)
""");
    s.P.new I() {void a() {s.T.at(s.search).setInt(8);  }};
    s.search();
    s.P.run(); s.P.clear();
    //stop(s);
    ok(s.print(), """
Transaction(action:search search:8 limit:0 found:1 index:3 key:8 data:4 size:4 isFull:0 isEmpty:0)
""");
   }

  static void test_search_except_last()
   {StuckPA s = test_load();

    s.P.new I() {void a() {s.T.at(s.limit ).setInt(1);}};
    s.P.new I() {void a() {s.T.at(s.search).setInt(4);}};
    s.search();
    s.P.run(); s.P.clear();
    //stop(s);
    ok(s.print(), """
Transaction(action:search search:4 limit:1 found:1 index:1 key:4 data:2 size:3 isFull:0 isEmpty:0)
""");

    s.P.new I() {void a() {s.T.at(s.search).setInt(8);}};
    s.search();
    s.P.run(); s.P.clear();
    //stop(t);
    ok(s.print(), """
Transaction(action:search search:8 limit:1 found:0 index:3 key:6 data:2 size:3 isFull:0 isEmpty:0)
""");
   }

  static void test_search_first_greater_than_or_equal()
   {StuckPA s = test_load();

    s.P.new I() {void a() {s.T.at(s.search).setInt(5);}};
    s.searchFirstGreaterThanOrEqual();
    s.P.run(); s.P.clear();
    //stop(t);
    ok(s.print(), """
Transaction(action:searchFirstGreaterThanOrEqual search:5 limit:0 found:1 index:2 key:6 data:3 size:4 isFull:0 isEmpty:0)
""");

    s.P.new I() {void a() {s.T.at(s.search).setInt(7);}};
    s.searchFirstGreaterThanOrEqual();
    s.P.run(); s.P.clear();
    //stop(t);
    ok(s.print(), """
Transaction(action:searchFirstGreaterThanOrEqual search:7 limit:0 found:1 index:3 key:8 data:4 size:4 isFull:0 isEmpty:0)
""");
   }

  static void test_search_first_greater_than_or_equal_except_last()
   {StuckPA s = test_load();

    s.P.new I() {void a() {s.T.at(s.limit).setInt(1);}};
    s.P.new I() {void a() {s.T.at(s.search).setInt(5);}};
    s.searchFirstGreaterThanOrEqual();
    s.P.run(); s.P.clear();
    //stop(t);
    ok(s.print(), """
Transaction(action:searchFirstGreaterThanOrEqual search:5 limit:1 found:1 index:2 key:6 data:3 size:3 isFull:0 isEmpty:0)
""");

    s.P.new I() {void a() {s.T.at(s.search).setInt(7);}};
    s.searchFirstGreaterThanOrEqual();
    s.P.run(); s.P.clear();
    //stop(t);
    ok(s.print(), """
Transaction(action:searchFirstGreaterThanOrEqual search:7 limit:1 found:0 index:3 key:6 data:3 size:3 isFull:0 isEmpty:0)
""");
   }

  static void test_at()
   {final int     N = 16;

    final StuckPA D = new StuckPA("test")
     {int maxSize     () {return 4;}
      int bitsPerKey  () {return 8;}
      int bitsPerData () {return 8;}
      int bitsPerSize () {return 8;}
      int baseAt      () {return 0;}
     };

    Layout               l = new Layout();
    Layout.Variable  a = l.variable ("a", N);
    Layout.Variable  b = l.variable ("b", N);
    Layout.Field     c = l.duplicate("c", D.M.layout());
    Layout.Array     C = l.array    ("C", c, 2);
    Layout.Structure d = l.structure("d", a, b, C);

    MemoryLayoutPA   M = new MemoryLayoutPA(l.compile(), "M");
    M.P.new I() {void a() {M.at(a).setInt(0); }};
    M.P.new I() {void a() {M.at(b).setInt(1); }};
    M.P.run();
    //stop(M);
    ok(M, """
MemoryLayout: M
Memory      : M
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0       176                                      d
   2 V        0        16                                  0     a
   3 V       16        16                                  1     b
   4 A       32       144          2                             C
   5 S       32        72               0                          c
   6 V       32         8               0                  0         currentSize
   7 A       40        32          4    0                            Keys
   8 V       40         8               0 0                0           key
   9 V       48         8               0 1                0           key
  10 V       56         8               0 2                0           key
  11 V       64         8               0 3                0           key
  12 A       72        32          4    0                            Data
  13 V       72         8               0 0                0           data
  14 V       80         8               0 1                0           data
  15 V       88         8               0 2                0           data
  16 V       96         8               0 3                0           data
  17 S      104        72               1                          c
  18 V      104         8               1                  0         currentSize
  19 A      112        32          4    1                            Keys
  20 V      112         8               1 0                0           key
  21 V      120         8               1 1                0           key
  22 V      128         8               1 2                0           key
  23 V      136         8               1 3                0           key
  24 A      144        32          4    1                            Data
  25 V      144         8               1 0                0           data
  26 V      152         8               1 1                0           data
  27 V      160         8               1 2                0           data
  28 V      168         8               1 3                0           data
""");

    final StuckPA S = new StuckPA("test", M)
     {int maxSize     () {return 4;}
      int bitsPerKey  () {return 8;}
      int bitsPerData () {return 8;}
      int bitsPerSize () {return 8;}
      int baseAt      () {return 0;}
     };

    final StuckPA s = S.copy();
    s.P.new I() {void a() {s.base(M.at(c, M.at(a)).setOff().at);}};

    s.P.new I() {void a() {s.T.at(s.tKey).setInt(2); s.T.at(s.tData).setInt(1);}}; s.push();
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(4); s.T.at(s.tData).setInt(2);}}; s.push();
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(6); s.T.at(s.tData).setInt(3);}}; s.push();
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(8); s.T.at(s.tData).setInt(4);}}; s.push();
    s.P.run(); s.P.clear();

//stop(s); STuckSML needs to allocate memory of a specified size?
    //stop(s.M);
    ok(s.M, """
MemoryLayout: test_StuckSA_Memory
Memory      : M
Line T       At      Wide       Size    Indices        Value   Name
   1 S       32        72                                      stuck
   2 V       32         8                                  4     currentSize
   3 A       40        32          4                             Keys
   4 V       40         8               0                  2       key
   5 V       48         8               1                  4       key
   6 V       56         8               2                  6       key
   7 V       64         8               3                  8       key
   8 A       72        32          4                             Data
   9 V       72         8               0                  1       data
  10 V       80         8               1                  2       data
  11 V       88         8               2                  3       data
  12 V       96         8               3                  4       data
""");
    //stop(M);
    ok(M, """
MemoryLayout: M
Memory      : M
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0       176                                      d
   2 V        0        16                                  0     a
   3 V       16        16                                  1     b
   4 A       32       144          2                             C
   5 S       32        72               0                          c
   6 V       32         8               0                  4         currentSize
   7 A       40        32          4    0                            Keys
   8 V       40         8               0 0                2           key
   9 V       48         8               0 1                4           key
  10 V       56         8               0 2                6           key
  11 V       64         8               0 3                8           key
  12 A       72        32          4    0                            Data
  13 V       72         8               0 0                1           data
  14 V       80         8               0 1                2           data
  15 V       88         8               0 2                3           data
  16 V       96         8               0 3                4           data
  17 S      104        72               1                          c
  18 V      104         8               1                  0         currentSize
  19 A      112        32          4    1                            Keys
  20 V      112         8               1 0                0           key
  21 V      120         8               1 1                0           key
  22 V      128         8               1 2                0           key
  23 V      136         8               1 3                0           key
  24 A      144        32          4    1                            Data
  25 V      144         8               1 0                0           data
  26 V      152         8               1 1                0           data
  27 V      160         8               1 2                0           data
  28 V      168         8               1 3                0           data
""");


    s.P.new I() {void a() {s.base(M.at(c, M.at(b)).setOff().at);}};
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(1); s.T.at(s.tData).setInt(2);}}; s.push();
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(2); s.T.at(s.tData).setInt(4);}}; s.push();
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(3); s.T.at(s.tData).setInt(6);}}; s.push();
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(4); s.T.at(s.tData).setInt(8);}}; s.push();
    s.P.run(); s.P.clear();
    //stop(s.M);
    ok(s.M.toString(), """
MemoryLayout: test_StuckSA_Memory
Memory      : M
Line T       At      Wide       Size    Indices        Value   Name
   1 S      104        72                                      stuck
   2 V      104         8                                  4     currentSize
   3 A      112        32          4                             Keys
   4 V      112         8               0                  1       key
   5 V      120         8               1                  2       key
   6 V      128         8               2                  3       key
   7 V      136         8               3                  4       key
   8 A      144        32          4                             Data
   9 V      144         8               0                  2       data
  10 V      152         8               1                  4       data
  11 V      160         8               2                  6       data
  12 V      168         8               3                  8       data
""");
    //stop(M);
    ok(M, """
MemoryLayout: M
Memory      : M
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0       176                                      d
   2 V        0        16                                  0     a
   3 V       16        16                                  1     b
   4 A       32       144          2                             C
   5 S       32        72               0                          c
   6 V       32         8               0                  4         currentSize
   7 A       40        32          4    0                            Keys
   8 V       40         8               0 0                2           key
   9 V       48         8               0 1                4           key
  10 V       56         8               0 2                6           key
  11 V       64         8               0 3                8           key
  12 A       72        32          4    0                            Data
  13 V       72         8               0 0                1           data
  14 V       80         8               0 1                2           data
  15 V       88         8               0 2                3           data
  16 V       96         8               0 3                4           data
  17 S      104        72               1                          c
  18 V      104         8               1                  4         currentSize
  19 A      112        32          4    1                            Keys
  20 V      112         8               1 0                1           key
  21 V      120         8               1 1                2           key
  22 V      128         8               1 2                3           key
  23 V      136         8               1 3                4           key
  24 A      144        32          4    1                            Data
  25 V      144         8               1 0                2           data
  26 V      152         8               1 1                4           data
  27 V      160         8               1 2                6           data
  28 V      168         8               1 3                8           data
""");

    //stop(s.M);
    ok(s.M, """
MemoryLayout: test_StuckSA_Memory
Memory      : M
Line T       At      Wide       Size    Indices        Value   Name
   1 S      104        72                                      stuck
   2 V      104         8                                  4     currentSize
   3 A      112        32          4                             Keys
   4 V      112         8               0                  1       key
   5 V      120         8               1                  2       key
   6 V      128         8               2                  3       key
   7 V      136         8               3                  4       key
   8 A      144        32          4                             Data
   9 V      144         8               0                  2       data
  10 V      152         8               1                  4       data
  11 V      160         8               2                  6       data
  12 V      168         8               3                  8       data
""");

    //stop(s.M.memory());
    ok(s.M.memory(), """
Memory: M
      4... 4... 4... 4... 3... 3... 3... 3... 2... 2... 2... 2... 1... 1... 1... 1...
Line  FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210
   0  0000 0000 0000 0000 0000 0806 0402 0403 0201 0404 0302 0108 0604 0204 0001 0000
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
    //test_at();
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
