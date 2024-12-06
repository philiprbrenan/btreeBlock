//------------------------------------------------------------------------------
// StuckPA in Psuedo Assembler
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout  a binary tree on a silicon chip.

abstract class StuckPA extends Test                                             // A fixed size stack of ordered key, data pairs
 {abstract Memory memory();                                                     // Memory containing the stuck
  MemoryLayout memoryLayout() {return new MemoryLayout(memory(), layout);};     // The memory layout of this stuck
  abstract int maxSize();                                                       // The maximum number of entries in the stuck.
  abstract int bitsPerKey();                                                    // The number of bits per key
  abstract int bitsPerData();                                                   // The number of bits per data
  abstract int bitsPerSize();                                                   // The number of bits in size field

  int baseAt() {return 0;}                                                      // Offset the memory for the stuck by this amount

  Layout layout;                                                                // Layout of memory containing the stuck
  Layout.Variable  key;                                                         // Key in a stuck
  Layout.Array     Keys;                                                        // Array of keys
  Layout.Variable  data;                                                        // Data associated with a key
  Layout.Array     Data;                                                        // Array of data associated with the array of keys
  Layout.Variable  currentSize;                                                 // Current size of stuck
  Layout.Structure stuck;                                                       // The stuck itself

  static boolean debug;                                                         // Debug when true

//D1 Construction                                                               // Create a stuck

  StuckPA()                                                                     // Create the stuck with a maximum number of the specified elements
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

  static StuckPA stuckStatic()                                                  // Create a sample stuck
   {z();
    return new StuckPA()
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
   {Program program;                                                            // Program whose execution performs the transaction
    String action;                                                              // Action performed
    Layout         tLayout;                                                     // Layout of a transaction against a stuck
    Layout.Variable search;                                                     // Search key
    Layout.Variable  limit;                                                     // Limit of search
    Layout.Bit      isFull;                                                     // Whether the stuck is currently full
    Layout.Bit     isEmpty;                                                     // Whether the stuck is currently empty
    Layout.Bit       found;                                                     // Whether a matching element was found
    Layout.Variable  index;                                                     // The index from which the key, data pair were retrieved
    Layout.Variable    key;                                                     // The retrieved key
    Layout.Variable   data;                                                     // The retrieved data
    Layout.Variable   base;                                                     // The base of the stuck
    Layout.Variable   size;                                                     // The current size of the stuck
    Layout.Bit       equal;                                                     // The result of an equal operation
    Layout.Variable   slit;                                                     // The current search limit
    Layout.Structure  temp;                                                     // Transaction intermediate fields
    final MemoryLayout trn = new MemoryLayout(layout());                        // Memory for transaction intemediates
    final MemoryLayout cpy = new MemoryLayout(StuckPA.this.layout);             // Buffer for stuck being manipulated

    Layout layout()
     {z();
      tLayout =  Layout.layout();
       search = tLayout.variable ( "search", bitsPerKey());
        limit = tLayout.variable (  "limit", bitsPerSize());
       isFull = tLayout.bit      ( "isFull");
      isEmpty = tLayout.bit      ("isEmpty");
        found = tLayout.bit      (  "found");
        index = tLayout.variable (  "index", bitsPerSize());
          key = tLayout.variable (    "key", bitsPerKey());
         data = tLayout.variable (   "data", bitsPerData());
         base = tLayout.variable (   "base", bitsPerSize());
         size = tLayout.variable (   "size", bitsPerSize());
        equal = tLayout.bit      (  "equal");
         slit = tLayout.variable (   "slit", bitsPerSize());
         temp = tLayout.structure("temp", search, limit, isFull, isEmpty, found, index, key, data, base, size, equal, slit);
      return tLayout.compile();
     }

    void size()                                                                 // The current number of key elements in the stuck
     {z(); final MemoryLayout t = trn;
      t.at(size).move(memoryLayout().at(currentSize, t.at(base)).setOff());
     }

    void isFull()                                                               // Check the stuck is full
     {z(); final MemoryLayout t = trn;
      t.at(size).greaterThanOrEqual(t.constant(maxSize()), t.at(isFull));
     }

    void isEmpty()                                                              // Check the stuck is empty
     {z(); final MemoryLayout t = trn;
      t.at(size).equal(t.constant(0), t.at(isEmpty));
     }

    void assertNotFull()                                                        // Assert the stuck is not full
     {z(); final MemoryLayout t = trn;
      if (t.at(isFull).getInt() > 0) stop("Full");
     }

    void assertNotEmpty()                                                       // Assert the stuck is not empty
     {z(); final MemoryLayout t = trn;
      if (t.at(isEmpty).getInt() > 0) stop("Empty");
     }

    void assertInNormal()                                                       // Check that the index would yield a valid element
     {z(); final MemoryLayout t = trn;
      final int i = t.at(index).getInt();
      final int s = t.at(size) .getInt();
      if (i < 0 || i >= s) stop("Out of normal range",   i, "for size", s);
     }

    void assertInExtended()                                                     // Check that the index would yield a valid element
     {z(); final MemoryLayout t = trn;
      final int i = t.at(index).getInt();
      final int s = t.at(size) .getInt();
      if (i < 0 || i > s) stop("Out of extended range", i, "for size", s);
     }

    void inc()                                                                  // Increment the current size
     {z(); final MemoryLayout m = memoryLayout(), t = trn;
      z(); assertNotFull();
      z(); final int s = m.at(currentSize, t.at(base)).setOff().getInt();
                         m.at(currentSize, t.at(base)).setOff().setInt(s+1);
     }

    void dec()                                                                  // Decrement the current size
     {z(); final MemoryLayout m = memoryLayout(), t = trn;
      z(); assertNotEmpty();
      z(); final int s = m.at(currentSize, t.at(base)).setOff().getInt();
                         m.at(currentSize, t.at(base)).setOff().setInt(s-1);
     }

    void clear()                                                                // Zero the current size to clear the stuck
     {z(); final MemoryLayout m = memoryLayout(), t = trn;
      z(); assertNotEmpty();
      z(); m.at(currentSize, t.at(base)).setOff().setInt(0);
      sizeFullEmpty();
     }

    MemoryLayout.At key()                                                       // Refer to key
     {z(); final MemoryLayout m = memoryLayout(), t = trn;
      return m.at(StuckPA.this.key,  t.at(base), t.at(index));
     }

    MemoryLayout.At data()                                                      // Refer to data
     {z(); final MemoryLayout m = memoryLayout(), t = trn;
      return m.at(StuckPA.this.data, t.at(base), t.at(index));
     }

    void moveKey()                                                              // Move a key from the stuck to this transaction
     {z(); final MemoryLayout t = trn; t.at(key ).move(key ().setOff());
     }

    void moveData()                                                             // Move a key from the stuck to this transaction
     {z(); final MemoryLayout t = trn; t.at(data).move(data().setOff());
     }

    void setKey  ()                                                             // Set the indexed key
     {z(); final MemoryLayout t = trn;  key().setOff().move(t.at(key));
     }

    void setData ()                                                             // Set the indexed data
     {z(); final MemoryLayout t = trn; data().setOff().move(t.at(data));
     }

    void setKeyData()    {z(); setKey(); setData();}                            // Set a key, data element in the stuck
    void sizeFullEmpty() {z(); size(); isFull(); isEmpty();}                    // Status
    void setFound()      {z(); trn.at(found).setInt(1);}                        // Set found to true

    void push()                                                                 // Push an element onto the stuck
     {z(); action = "push";
      final MemoryLayout t = trn;
      final Program      p = new Program();
      p.new I() {void a() {size();}};
      p.new I() {void a() {isFull();}};
      p.new I() {void a() {assertNotFull();}};
      p.new I() {void a() {t.at(index).move(t.at(size));}};
      p.new I() {void a() {setKeyData();}};
      p.new I() {void a() {inc();}};
      p.new I() {void a() {sizeFullEmpty();}};
      p.execute();
     }

    void unshift()                                                              // Unshift an element onto the stuck
     {z(); action = "unshift";
      final MemoryLayout m = memoryLayout(), t = trn, c = cpy;
      final Program      p = new Program();
      p.new I() {void a() {size();}};
      p.new I() {void a() {isFull();}};
      p.new I() {void a() {assertNotFull();}};
      p.new I() {void a() {setFound();}};
      p.new I() {void a() {t.zero();}};
      p.new I() {void a() {m.at(Keys,  t.at(base)).moveUp(t.at(currentSize), c.at(Keys));}};
      p.new I() {void a() {m.at(Data,  t.at(base)).moveUp(t.at(currentSize), c.at(Data));}};
      p.new I() {void a() {inc();}};
      p.new I() {void a() {m.at(index, t.at(base)).setInt(0);}};
      p.new I() {void a() {setKeyData();}};
      p.new I() {void a() {sizeFullEmpty();}};
      p.execute();
     }

    void pop()                                                                  // Pop an element from the stuck
     {z(); action = "pop";
      final MemoryLayout t = trn;
      final Program      p = new Program();
      p.new I() {void a() {size();}};
      p.new I() {void a() {isEmpty();}};
      p.new I() {void a() {assertNotEmpty();}};
      p.new I() {void a() {dec();}};
      p.new I() {void a() {setFound();}};
      p.new I() {void a() {t.at(size).dec();}};
      p.new I() {void a() {t.at(index).move(t.at(size));}};
      p.new I() {void a() {moveKey(); moveData();}};
      p.new I() {void a() {sizeFullEmpty();}};
      p.execute();
     }

    void shift()                                                                // Shift an element from the stuck
     {z(); action = "shift";
      final MemoryLayout m = memoryLayout(), t = trn, c = cpy;
      final Program      p = new Program();
      p.new I() {void a() {size();}};
      p.new I() {void a() {isEmpty();}};
      p.new I() {void a() {assertNotEmpty();}};
      p.new I() {void a() {setFound();}};
      p.new I() {void a() {t.at(index).setInt(0);}};
      p.new I() {void a() {moveKey();}};
      p.new I() {void a() {moveData();}};
      p.new I() {void a() {t.zero();}};
      p.new I() {void a() {m.at(Keys, t.at(base)).setOff().moveDown(t.constant(0), c.at(Keys));}};
      p.new I() {void a() {m.at(Data, t.at(base)).setOff().moveDown(t.constant(0), c.at(Data));}};
      p.new I() {void a() {dec();}};
      p.new I() {void a() {sizeFullEmpty();}};
      p.execute();
     }

    void elementAt()                                                            // Look up key and data associated with the index in the stuck at the specified base offset in memory
     {z(); action = "elementAt";
      final MemoryLayout t = trn;
      final Program      p = new Program();
      p.new I() {void a() {size();}};
      p.new I() {void a() {assertInNormal();}};
      p.new I() {void a() {setFound();}};
      p.new I() {void a() {moveKey();}};
      p.new I() {void a() {moveData();}};
      p.execute();
     }

    void setElementAt()                                                         // Set an element either in range or one above the current range
     {z(); action = "setElementAt";
      final MemoryLayout t = trn;
      final Program      p = new Program();
      p.new I() {void a() {size();}};
      p.new I() {void a() {t.at(index).equal(t.at(size), t.at(equal));}};       // Extended range
      p.new If(t.at(equal))
       {void Then()
         {z();
          p.new I() {void a() {setKeyData();}};
          p.new I() {void a() {inc();}};
          p.new I() {void a() {t.at(size).inc();}};
         }
        void Else()                                                             // In range
         {z();
          p.new I() {void a() {assertInNormal();}};
          p.new I() {void a() {setKeyData();}};
         }
       };
      p.new I() {void a() {setFound();}};
      p.execute();
     }

    void insertElementAt()                                                      // Insert an element at the indicated location shifting all the remaining elements up one
     {z(); action = "insertElementAt";
      final MemoryLayout m = memoryLayout(), t = trn, c = cpy;
      final Program      p = new Program();
      p.new I() {void a() {size();}};
      p.new I() {void a() {isFull();}};
      p.new I() {void a() {assertInExtended();}};

      p.new I() {void a() {t.zero();}};
      p.new I() {void a() {m.at(Keys, t.at(base)).moveUp(t.at(index), c.at(Keys));}};
      p.new I() {void a() {m.at(Data, t.at(base)).moveUp(t.at(index), c.at(Data));}};
      p.new I() {void a() {m.at(currentSize, t.at(base)).inc();}};
      p.new I() {void a() {setKeyData();}};
      p.new I() {void a() {inc();}};
      p.new I() {void a() {sizeFullEmpty();}};
      p.execute();
     }

    void removeElementAt()                                                      // Remove an element at the indicated location from the stuck
     {z(); action = "removeElementAt";
      final MemoryLayout m = memoryLayout(), t = trn, c = cpy;
      final Program      p = new Program();
      p.new I() {void a() {size();}};
      p.new I() {void a() {assertInNormal();}};
      p.new I() {void a() {setFound();}};
      p.new I() {void a() {moveKey();}};
      p.new I() {void a() {moveData();}};

      p.new I() {void a() {t.zero();}};
      p.new I() {void a() {m.at(Keys, t.at(base)).moveDown(t.at(index), c.at(Keys));}};
      p.new I() {void a() {m.at(Data, t.at(base)).moveDown(t.at(index), c.at(Data));}};
      p.new I() {void a() {m.at(currentSize, t.at(base)).setOff().dec();}};

      p.new I() {void a() {sizeFullEmpty();}};
      p.execute();
     }

    void firstElement()                                                         // First element
     {z(); action = "firstElement";
      final MemoryLayout m = memoryLayout(), t = trn;
      final Program      p = new Program();
      p.new I() {void a() {size();}};
      p.new I() {void a() {isEmpty();}};
      p.new I() {void a() {assertNotEmpty();}};
      p.new I() {void a() {setFound();}};
      p.new I() {void a() {m.at(index, t.at(base)).setInt(0);}};
      p.new I() {void a() {moveKey();}};
      p.new I() {void a() {moveData();}};
      p.execute();
     }

    void lastElement()                                                          // Last element
     {z(); action = "lastElement";
      final MemoryLayout m = memoryLayout(), t = trn;
      final Program      p = new Program();
      p.new I() {void a() {size();}};
      p.new I() {void a() {isEmpty();}};
      p.new I() {void a() {assertNotEmpty();}};
      p.new I() {void a() {setFound();}};
      p.new I() {void a() {t.at(index).move(m.at(currentSize, t.at(base)).setOff());}};
      p.new I() {void a() {t.at(index).dec();}};
      p.new I() {void a() {moveKey(); moveData();}};
      p.execute();
     }

    void search()                                                               // Search for an element within all elements of the stuck
     {z(); action = "search";
      size();
      final MemoryLayout t = trn;
      final Program      p = new Program();

      p.new I()                                                                 // Search limit
       {void a()
         {final int s = t.at(size).getInt(), l = t.at(limit).getInt(), L = s-l;
          t.at(slit).setInt(L);
         }
       };

      p.new Block()                                                             // Search each active element. Jump out when we find a match or we process all the active elements.
       {void code()
         {final Program.Label endSearch = this.end;
          final int N = maxSize();                                              // Upper limit on search
          p.new I() {void a() {t.at(found).setInt(0);}};                        // Assume that the search will fail

          for (int I = 0; I < N; I++)                                           // Search loop unrolled
           {z();
            final int i = I;                                                    // Fix the  current loop iteration
            p.new I() {void a() {t.constant(i).greaterThanOrEqual(t.at(slit), t.at(equal));}};
            p.new I() {void a() {p.GoOn(endSearch, t.at(equal));}};             // Jump out if all the active elements have been checked

            p.new I() {void a() {t.at(index).setInt(i);}};                      // Set index
            p.new I() {void a() {moveKey();}};                                  // Key at this index
            p.new I() {void a() {t.at(key).equal(t.at(search), t.at(equal));}}; // Check for a match between indexed key and search key

            p.new If (t.at(equal))                                              // Found a match
             {void Then()
               {z();
                p.new I() {void a() {setFound();}};                             // Show found
                p.new I() {void a() {moveData();}};                             // Matching data
                p.new I() {void a() {p.Goto(endSearch);}};                      // End the search
               }
             };
           }
         }
       };
      p.execute();
     }

    void searchFirstGreaterThanOrEqual()                                        // Search for first greater than or equal
     {z(); action = "searchFirstGreaterThanOrEqual";
      size();
      final MemoryLayout t = trn;
      final Program      p = new Program();

      p.new I()                                                                 // Search limit
       {void a()
         {final int s = t.at(size).getInt(), l = t.at(limit).getInt(), L = s-l;
          t.at(slit).setInt(L);
         }
       };

      p.new Block()                                                             // Search each active element. Jump out when we find a match or we process all the active elements.
       {void code()
         {final Program.Label endSearch = this.end;
          final int N = maxSize();                                              // Upper limit on search
          p.new I() {void a() {t.at(found).setInt(0);}};                        // Assume that the search will fail

          for (int I = 0; I < N; I++)                                           // Search loop unrolled
           {z();
            final int i = I;                                                    // Fix the  current loop iteration
            p.new I() {void a() {t.constant(i).greaterThanOrEqual(t.at(slit), t.at(equal));}};
            p.new I() {void a() {p.GoOn(endSearch, t.at(equal));}};             // Jump out if all the active elements have been checked

            p.new I() {void a() {t.at(index).setInt(i);}};                      // Set index
            p.new I() {void a() {moveKey();}};                                  // Key at this index
            p.new I() {void a() {t.at(key).greaterThanOrEqual(t.at(search), t.at(equal));}}; // Check for a match between indexed key and search key

            p.new If (t.at(equal))                                              // Found a match
             {void Then()
               {z();
                p.new I() {void a() {setFound();}};                             // Show found
                p.new I() {void a() {moveData();}};                             // Matching data
                p.new I() {void a() {p.Goto(endSearch);}};                      // End the search
               }
             };
           }
         }
       };
      p.execute();
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
      s.append(    " key:"+t.at(key)   .getInt());
      s.append(   " data:"+t.at(data)  .getInt());
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
    s.append("StuckPA(maxSize:"+maxSize());
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

  static StuckPA test_load()
   {StuckPA s = stuckStatic();
    final Transaction  t = s.new Transaction();
    final MemoryLayout m = t.trn;
    m.at(t.base).setInt(s.baseAt());
    for (int i = 0; i < 4; i++)
     {m.at(t.key ).setInt(2 + 2 * i);
      m.at(t.data).setInt(1 + 1 * i);
      t.push();
     }
    //stop(s.toString(s.baseAt()));
    ok(s.toString(s.baseAt()), """
StuckPA(maxSize:8 size:4)
  0 key:2 data:1
  1 key:4 data:2
  2 key:6 data:3
  3 key:8 data:4
""");
    return s;
   }

  static void test_clear()
   {StuckPA s = test_load();
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
   {StuckPA s = stuckStatic();
    final Transaction  t = s.new Transaction();
    final MemoryLayout m = t.trn;
    m.at(t.base).setInt(s.baseAt());
    m.at(t.key).setInt(15); m.at(t.data).setInt( 9); t.push();
    m.at(t.key).setInt(14); m.at(t.data).setInt(10); t.push();
    m.at(t.key).setInt(13); m.at(t.data).setInt(11); t.push();
    m.at(t.key).setInt(12); m.at(t.data).setInt(12); t.push();
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
StuckPA(maxSize:8 size:4)
  0 key:15 data:9
  1 key:14 data:10
  2 key:13 data:11
  3 key:12 data:12
""");

    m.at(t.key).setInt(11); m.at(t.data).setInt(11); t.push(); ok(m.at(t.isFull).getInt() == 0); ok(m.at(t.isEmpty).getInt() == 0);
    m.at(t.key).setInt(10); m.at(t.data).setInt(10); t.push();
    m.at(t.key).setInt( 9); m.at(t.data).setInt( 9); t.push();
    m.at(t.key).setInt( 8); m.at(t.data).setInt( 8); t.push(); ok(m.at(t.isFull).getInt() == 1);
    sayThisOrStop("Full");
    try {m.at(t.key).setInt(7); m.at(t.data).setInt(7); t.push();} catch(RuntimeException e) {}
   }

  static void test_pop()
   {StuckPA s = test_load();
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
StuckPA(maxSize:8 size:3)
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
   {StuckPA s = test_load();
    final Transaction t = s.new Transaction();
    final MemoryLayout m = t.trn;
    m.at(t.base).setInt(s.baseAt());

    t.shift();
    ok(t, """
Transaction(action:shift search:0 limit:0 found:1 index:0 key:2 data:1 base:16 size:3 isFull:0 isEmpty:0)
""");
    //stop(s.toString(s.baseAt()));
    ok(s.toString(s.baseAt()), """
StuckPA(maxSize:8 size:3)
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
   {StuckPA            s = test_load();
    final Transaction  t = s.new Transaction();
    final MemoryLayout m = t.trn;
    m.at(t.base).setInt(s.baseAt());

    m.at(t.key).setInt(9); m.at(t.data).setInt(9);
    t.unshift();
    //stop(t);
    ok(t, """
Transaction(action:unshift search:0 limit:0 found:1 index:0 key:9 data:9 base:16 size:5 isFull:0 isEmpty:0)
""");
    //stop(s.toString(s.baseAt()));
    ok(s.toString(s.baseAt()), """
StuckPA(maxSize:8 size:5)
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
   {StuckPA            s = test_load();
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
   {StuckPA            s = test_load();
    final Transaction  t = s.new Transaction();
    final MemoryLayout m = t.trn;
    m.at(t.base).setInt(s.baseAt());

    m.at(t.key).setInt(22); m.at(t.data).setInt(33); m.at(t.index).setInt(2);
    t.setElementAt();
    //stop(s.toString(s.baseAt()));
    ok(s.toString(s.baseAt()), """
StuckPA(maxSize:8 size:4)
  0 key:2 data:1
  1 key:4 data:2
  2 key:22 data:33
  3 key:8 data:4
""");

    m.at(t.key).setInt(88); m.at(t.data).setInt(99); m.at(t.index).setInt(4); t.setElementAt();
    //stop(s.toString(s.baseAt()));
    ok(s.toString(s.baseAt()), """
StuckPA(maxSize:8 size:5)
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
   {StuckPA            s = test_load();
    final Transaction  t = s.new Transaction();
    final MemoryLayout m = t.trn;
    m.at(t.base).setInt(s.baseAt());

    m.at(t.key).setInt(9); m.at(t.data).setInt(9); m.at(t.index).setInt(2); t.insertElementAt();
    //stop(s.toString(s.baseAt()));
    ok(s.toString(s.baseAt()), """
StuckPA(maxSize:8 size:5)
  0 key:2 data:1
  1 key:4 data:2
  2 key:9 data:9
  3 key:6 data:3
  4 key:8 data:4
""");

    m.at(t.key).setInt(7); m.at(t.data).setInt(7); m.at(t.index).setInt(5); t.insertElementAt();
    //stop(s.toString(s.baseAt()));
    ok(s.toString(s.baseAt()), """
StuckPA(maxSize:8 size:6)
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
   {StuckPA            s = test_load();
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
StuckPA(maxSize:8 size:3)
  0 key:2 data:1
  1 key:4 data:2
  2 key:8 data:4
""");

    m.at(t.index).setInt(3);
    sayThisOrStop("Out of normal range 3 for size 3");
    try {t.removeElementAt();} catch(RuntimeException e) {}
   }

  static void test_first_last()
   {StuckPA            s = test_load();
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
   {StuckPA            s = test_load();
    final Transaction  t = s.new Transaction();
    final MemoryLayout m = t.trn;

    m.at(t.base).setInt(s.baseAt());

    m.at(t.search).setInt(2); t.search();
    //stop(t);
    ok(t, """
Transaction(action:search search:2 limit:0 found:1 index:0 key:2 data:1 base:16 size:4 isFull:0 isEmpty:0)
""");

    m.at(t.search).setInt(3);
    t.search();
    //stop(t);
    ok(t, """
Transaction(action:search search:3 limit:0 found:0 index:3 key:8 data:1 base:16 size:4 isFull:0 isEmpty:0)
""");

    m.at(t.search).setInt(7);
    t.search();
    //stop(t);
    ok(t, """
Transaction(action:search search:7 limit:0 found:0 index:3 key:8 data:1 base:16 size:4 isFull:0 isEmpty:0)
""");

    m.at(t.search).setInt(8);  t.search();
    //stop(t);
    ok(t, """
Transaction(action:search search:8 limit:0 found:1 index:3 key:8 data:4 base:16 size:4 isFull:0 isEmpty:0)
""");
   }

  static void test_search_except_last()
   {StuckPA            s = test_load();
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
   {StuckPA            s = test_load();
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
   {StuckPA            s = test_load();
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
