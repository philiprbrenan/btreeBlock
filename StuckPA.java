//------------------------------------------------------------------------------
// StuckSA on a bit machine - completed on Christmas Eve!
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
// Version: before placing based storage with buffered storage of the stuck
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Btree in a block on the surface of a silicon chip.

abstract class StuckPA extends Test                                             // A fixed size stack of ordered key, data pairs
 {abstract int maxSize();                                                       // The maximum number of entries in the stuck.
  abstract int bitsPerKey();                                                    // The number of bits needed to define a key
  abstract int bitsPerData();                                                   // The number of bits needed to define a data field
  abstract int bitsPerSize();                                                   // The number of bits needed to define the size field

  final String      name;                                                       // Name of the stuck
  final int bitsPerAddress;                                                     // Number of bits needed to address a bit in the memory containign the stuck
  final MemoryLayoutPA M;                                                       // Memory for stuck
  final MemoryLayoutPA B;                                                       // Copy the stuck out of main memory, manipulate it, then return it to main memory to reduce contention on main memory induced by many small accesses
  final MemoryLayoutPA C;                                                       // Temporary storage containing a copy of parts of the stuck to allow shifts to occur in parallel
  final MemoryLayoutPA T;                                                       // Memory for transaction intermediates
  ProgramPA            P = new ProgramPA();                                     // The program to be written to to describe the actions on the stuck.  The caller can provide a different one as this field is not final
  final static boolean Assert = false;                                          // Whether asserts should be executed or not
  static boolean   debug;                                                       // Debug when true
  String          action;                                                       // Last action performed

  Layout.Variable               sKey;                                           // Key in a stuck
  Layout.Array                  Keys;                                           // Array of keys
  Layout.Variable              sData;                                           // Data associated with a key
  Layout.Array                  Data;                                           // Array of data associated with the array of keys
  Layout.Variable        currentSize;                                           // Current size of stuck
  Layout.Structure             stuck;                                           // The stuck itself
  Layout.Variable             search;                                           // Search key
  Layout.Variable              limit;                                           // Limit of search
  Layout.Bit                  isFull;                                           // Whether the stuck is currently full
  Layout.Bit                 isEmpty;                                           // Whether the stuck is currently empty
  Layout.Bit                   found;                                           // Whether a matching element was found
  Layout.Variable              index;                                           // The index from which the key, data pair were retrieved
  Layout.Variable               tKey;                                           // The retrieved key
  Layout.Variable              tData;                                           // The retrieved data
  Layout.Variable               size;                                           // The current size of the stuck
  Layout.Variable               full;                                           // Used by isFull
  Layout.Bit                   equal;                                           // The result of an equal operation
  Layout.Variable          copyCount;                                           // Number of elements to copy in a copy operation
  Layout.Variable       copyBitsKeys;                                           // Number of keys bits to copy in a copy operation
  Layout.Variable       copyBitsData;                                           // Number of data bits to copy in a copy operation
  Layout.Variable       maxSizeValue;                                           // A field with the maximum size of the stuck in it
  Layout.Variable               zero;                                           // A field with zero in it
  Layout.Bit            equalLeafKey;                                           // Whether the indexed key is equal to the search key
  Layout.Array     arrayEqualLeafKey;                                           // Whether each indexed key is equal to the search key
  Layout.Bit        lessThanLeafSize;                                           // Whether the index is less than the leaf size
  Layout.Array arrayLessThanLeafSize;                                           // Whether the index is less than the leaf size
  Layout.Structure              temp;                                           // Transaction intermediate fields

  Layout.Variable  copy_source_keys, copy_source_data;                          // Index/pointer, length variables to copy a variable number of bits from one stuck to another
  Layout.Variable  copy_target_keys, copy_target_data;
  Layout.Variable  copy_length_keys, copy_length_data;

//D1 Construction                                                               // Create a stuck

  StuckPA(String Name)                                                          // Create the stuck with a maximum number of the specified elements
   {this(Name, null); z();
   }

  StuckPA(String Name, MemoryLayoutPA Based)                                    // Create the stuck with a maximum number of the specified elements
   {zz(); name = Name;
    final Layout layout = layout();                                             // Layout out the stuck
    if (Based != null && Based.based()) M = Based;                              // Some usable memory has been supplied
    else M = new MemoryLayoutPA(layout, Name+"_StuckSA_Memory", Based);         // No memory has been supplied, so create some memory and then base off it to assist in testing.
    B = new MemoryLayoutPA(layout, name+"_StuckSA_Base");                       // Base copy of stuck to reduce congestion on main memory during routing
    C = new MemoryLayoutPA(layout, name+"_StuckSA_Copy");                       // Temporary storage containing a copy of parts of the stuck to allow shifts to occur in parallel

    bitsPerAddress = logTwo(M.baseSize());                                      // The stuck might be located any where in this memory
    final Layout tl = transactionLayout();                                      // Layout out of transations performed on stuck
    T = new MemoryLayoutPA(tl,     name+"_StuckSA_Transaction");                // Memory for transaction intermediates
    program(M.P);
   }

  void base(int Base)                                                           // Set the base address of the stuck in the memory layout containing the stuck
   {z(); M.base(Base);
   }

  void base(MemoryLayoutPA.At Base)                                             // Set the base address of the stuck in the memory layout containing the stuck
   {zz();
    P.new I()
     {void   a() {M.base(Base.setOff().result);}
      String v() {return M.baseName() + " <= " + Base.verilogLoad()+";";}       // Set the base for this based data structure
     };
   }

  void program(ProgramPA program)                                               // Set the program in which the various components should generate code
   {zz();  P = program;
    M.program(P);
    C.program(P);
    T.program(P);
   }

  StuckPA copyDef()                                                             // Copy a stuck definition
   {z();
    final StuckPA parent = this;
    final StuckPA  child = new StuckPA(parent.name, parent.M.based)             // Address the underlying memory layout which is not based so wil force a new based memory to be created for this copy
     {int maxSize    () {return parent.maxSize    ();}
      int bitsPerKey () {return parent.bitsPerKey ();}
      int bitsPerData() {return parent.bitsPerData();}
      int bitsPerSize() {return parent.bitsPerSize();}
     };
    child.program(parent.P);
    return child;
   }

  Layout layout()                                                               // Layout describing stuck
   {zz();
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
   {zz();
    final Layout        l = Layout.layout();
                  isFull  = //l.bit      (       "isFull");
                 isEmpty  = //l.bit      (      "isEmpty");
                   found  = //l.bit      (        "found");
                   equal  = l.bit      (        "equal");
                  search  = //l.variable (       "search", bitsPerKey());
                    tKey  = l.variable (          "key", bitsPerKey());
                   tData  = l.variable (         "data", bitsPerData());
                   limit  = //l.variable (        "limit", bitsPerSize());
               copyCount  = l.variable (    "copyCount", bitsPerSize());
                   index  = l.variable (        "index", bitsPerSize());
                    size  = //l.variable (         "size", bitsPerSize());
                    full  = l.variable (         "full", bitsPerSize());
            copyBitsKeys  = l.variable ( "copyBitsKeys", bitsPerSize() + bitsPerKey());
            copyBitsData  = l.variable ( "copyBitsData", bitsPerSize() + bitsPerData());

         copy_source_keys = l.variable ("copy_source_keys", bitsPerAddress);
         copy_target_keys = l.variable ("copy_target_keys", bitsPerAddress);
         copy_length_keys = l.variable ("copy_length_keys", bitsPerAddress);

         copy_source_data = l.variable ("copy_source_data", bitsPerAddress);
         copy_target_data = l.variable ("copy_target_data", bitsPerAddress);
         copy_length_data = l.variable ("copy_length_data", bitsPerAddress);

             equalLeafKey = l.bit  (     "equalLeafKey");
        arrayEqualLeafKey = l.array("arrayEqualLeafKey",     equalLeafKey,     maxSize());
         lessThanLeafSize = l.bit  (     "lessThanLeafSize");
    arrayLessThanLeafSize = l.array("arrayLessThanLeafSize", lessThanLeafSize, maxSize());

    temp = l.structure("temp",
//         isFull,
//         isEmpty,
//         found,
           equal,
//         search,
           tKey,
           tData,
//         limit,
           index,
//         size,
           full,
           copyCount,
           copyBitsKeys,
           copyBitsData,
           copy_source_keys,
           copy_target_keys,
           copy_length_keys,
           copy_source_data,
           copy_target_data,
           copy_length_data,
           arrayEqualLeafKey,
           arrayLessThanLeafSize);
    return l.compile();
   }

//D1 Transactions                                                               // Transactions on the stuck

  void size()                                                                   // The current number of elements in the stuck
   {zz();
    T.at(size).move(M.at(currentSize));
   }

  void setSize()                                                                // Set the current number of elements in the stuck
   {zz();
    M.at(currentSize).move(T.at(size));
   }

  void isFull()                                                                 // Check the stuck is full
   {zz();
    T.setIntInstruction(full, maxSize());
    T.at(size).greaterThanOrEqual(T.at(full), T.at(isFull));
   }

  void isEmpty()                                                                // Check the stuck is empty
   {zz();
    T.setIntInstruction(full, 0);
    T.at(size).equal(T.at(full), T.at(isEmpty));
   }

  void assertNotFull()                                                          // Assert the stuck is not full
   {zz();
    if (Assert)
     {P.new If(T.at(isFull))
       {void Then() {P.halt("Stuck full");}
       };
     }
   }

  void assertNotEmpty()                                                         // Assert the stuck is not empty
   {zz();
    if (Assert)
     {P.new If(T.at(isEmpty))
       {void Then() {P.halt("Empty");}
       };
     }
   }

  void assertInNormal()                                                         // Check that the index would yield a valid element
   {zz();
    if (Assert)
     {P.new I()
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
   }

  void copyKeys(StuckPA source)                                                 // Copy the specified number of elements from the source array of keys at the specified index into the target array of keys at the specified target index
   {zz();
    P.new I()
     {void   a() {T.at(copyBitsKeys).setInt(T.at(copyCount).getInt()*bitsPerKey());}
      String v() {return T.at(copyBitsKeys).verilogLoad()+ " <= " + T.at(copyCount).verilogLoad() + "*" + bitsPerKey()+";" + traceComment();}
     };
    final MemoryLayoutPA.At ti = T.at(index);
    final MemoryLayoutPA.At si = source.T.at(source.index);
//  M.at(sKey, ti).copy(source.M.at(source.sKey, si), T.at(copyBitsKeys));
    M.at(sKey, ti).copy(source.M.at(source.sKey, si), T.at(copyBitsKeys),
    T.at(copy_target_keys), T.at(copy_source_keys), T.at(copy_length_keys));
   }

  void copyData(StuckPA source)                                                 // Copy the specified number of elements from the source array of data at the specified index into the target array of data at the specified target index
   {zz();
    P.new I()
     {void   a() {T.at(copyBitsData).setInt(T.at(copyCount).getInt()*bitsPerData());}
      String v() {return T.at(copyBitsData).verilogLoad()+ " <= " + T.at(copyCount).verilogLoad() + "*" + bitsPerData()+";";}
     };
    final MemoryLayoutPA.At ti = T.at(index);
    final MemoryLayoutPA.At si = source.T.at(source.index);
//  M.at(sData, ti).copy(source.M.at(source.sData, si), T.at(copyBitsData));
    M.at(sData, ti).copy(source.M.at(source.sData, si), T.at(copyBitsData),
      T.at(copy_target_data), T.at(copy_source_data), T.at(copy_length_data));
   }

  void copyKeysData(StuckPA source)                                             // Copy the specified number of key, data pairs from the source array at the specified index into the target array at the specified target index
   {zz();
    P.parallelStart();   copyKeys(source);
    P.parallelSection(); copyData(source);
    P.parallelEnd();
   }

  void assertInExtended()                                                       // Check that the index would yield a valid element
   {zz();
    if (Assert)                                                                 // Delete fails
     {P.new I()
       {void a()
         {final int i = T.at(index).getInt();
          final int n = T.at(size) .getInt();
          if (i < 0 || i > n) stop("Out of extended range", i, "for size", n, traceBack);
         }
        String v()
         {return "/* assertInExtended */";
         }
       };
     }
   }

  void inc()                                                                    // Increment the current size
   {zz();
    //assertNotFull();
    M.at(currentSize).inc();
   }

  void dec()                                                                    // Decrement the current size
   {zz();
    //assertNotEmpty();
    M.at(currentSize).dec();
   }

  void clear()                                                                  // Zero the current size to clear the stuck
   {zz();
    M.setIntInstruction(currentSize, 0);
   }

  MemoryLayoutPA.At key()                                                       // Refer to key
   {zz();
    return M.at(sKey, T.at(index));
   }

  MemoryLayoutPA.At data()                                                      // Refer to data
   {zz(); return M.at(sData, T.at(index));
   }

  void moveKey()                                                                // Move a key from the stuck to this transaction
   {zz(); T.at(tKey ).move(key ());
   }

  void moveData()                                                               // Move a key from the stuck to this transaction
   {zz(); T.at(tData).move(data());
   }

  void setKey  ()                                                               // Set the indexed key
   {zz(); key().move(T.at(tKey));
   }

  void setData ()                                                               // Set the indexed data
   {zz(); data().move(T.at(tData));
   }

  void setKeyData()                                                             // Set a key, data element in the stuck
   {zz();
    P.parallelStart();
      setKey();
    P.parallelSection();
      setData();
    P.parallelEnd();
   }

  void moveKeyData()                                                            // Move key, data
   {zz();
    P.parallelStart();
      moveKey();
    P.parallelSection();
      moveData();
    P.parallelEnd();
   }

  void sizeFullEmpty() {z(); size(); isFull(); isEmpty();}                      // Status
  void setFound()      {zz(); T.setIntInstruction(found, 1);}                    // Set found to true

  void push()                                                                   // Push an element onto the stuck
   {zz(); action = "push";
    //size();
    //isFull();
    //assertNotFull();
    size();
    T.at(index).move(T.at(size));
    P.parallelStart();   setKeyData();
    P.parallelSection(); inc();
    P.parallelEnd();
    //sizeFullEmpty();
   }

  void unshift()                                                                // Unshift an element onto the stuck
   {zz(); action = "unshift";
    //size();
    //isFull();
    //assertNotFull();
    //setFound();
    //T.setIntInstruction(index, 0);
    //M.at(Keys).moveUp(T.at(index), C.at(Keys));
    P.parallelStart();
      M.at(Keys).moveUp(null, C.at(Keys));
    //T.setIntInstruction(index, 0);
    //M.at(Data).moveUp(T.at(index), C.at(Data));
    P.parallelSection();
      M.at(Data).moveUp(null, C.at(Data));
    P.parallelSection();
      inc();
    P.parallelEnd();
    //T.setIntInstruction(index, 0);
    //setKeyData();

    P.parallelStart();   M.at(sKey,  0).move(T.at(tKey  ));
    P.parallelSection(); M.at(sData, 0).move(T.at(tData ));
    P.parallelEnd();

    //sizeFullEmpty();
   }

  void pop()                                                                    // Pop an element from the stuck
   {zz(); action = "pop";
    //size();
    //isEmpty();
    //assertNotEmpty();
    dec();
    //setFound();
    //T.at(size).dec();
    T.at(index).move(M.at(currentSize));
    moveKeyData();
    //sizeFullEmpty();
   }

//  void shift()                                                                  // Shift an element from the stuck
//   {zz(); action = "shift";
//    size();
//    //isEmpty();
//    //assertNotEmpty();
//    //setFound();
//    T.setIntInstruction(index, 0);
//    moveKeyData();
//    T.zero();
//    M.at(Keys).moveDown(T.at(index), C.at(Keys));
//    M.at(Data).moveDown(T.at(index), C.at(Data));
//    dec();
//    //sizeFullEmpty();
//   }

  void shift()                                                                  // Shift an element from the stuck
   {zz(); action = "shift";
    firstElement();
    P.parallelStart();    M.at(Keys).moveDown(null, C.at(Keys));
    P.parallelSection();  M.at(Data).moveDown(null, C.at(Data));
    P.parallelSection();  dec();
    P.parallelEnd();
    //sizeFullEmpty();
   }

  void elementAt()                                                              // Look up key and data associated with the index in the stuck at the specified base offset in memory
   {zz(); action = "elementAt";
    //size();
    //assertInNormal();
    //setFound();
    moveKeyData();
   }

  void setElementAt()                                                           // Set an element either in range or one above the current range
   {zz(); action = "setElementAt";
    //size();
    //T.at(index).equal(T.at(size), T.at(equal));                                 // Extended range
    //P.new If(T.at(equal))
    // {void Then()
    //   {setKeyData();
    //    inc();
    //    T.at(size).inc();
    //   }
    //  void Else()                                                               // In range
    // {assertInNormal();
        setKeyData();
    //   }
    // };
    //setFound();
   }

  void insertElementAt()                                                        // Insert an element at the indicated location shifting all the remaining elements up one
   {zz(); action = "insertElementAt";

    //size();
    //isFull();
    //assertInExtended();
    //T.zero();
    P.parallelStart();   M.at(Keys).moveUp(T.at(index), C.at(Keys));
    P.parallelSection(); M.at(Data).moveUp(T.at(index), C.at(Data));
    P.parallelSection(); M.at(currentSize).inc();
    P.parallelEnd();
    setKeyData();
    //sizeFullEmpty();
   }

  void removeElementAt()                                                        // Remove an element at the indicated location from the stuck
   {zz(); action = "removeElementAt";
    //size();
    //assertInNormal();
    //setFound();
    moveKeyData();
    //T.zero();
    P.parallelStart();    M.at(Keys).moveDown(T.at(index), C.at(Keys));
    P.parallelSection();  M.at(Data).moveDown(T.at(index), C.at(Data));
    P.parallelSection();  M.at(currentSize).dec();
    P.parallelEnd();
    //sizeFullEmpty();
   }

  void firstElement()                                                           // First element
   {zz(); action = "firstElement";
    //size();
    //isEmpty();
    //assertNotEmpty();
    //setFound();
    P.parallelStart();   T.at(tKey  ).move(M.at(sKey,  0));
    P.parallelSection(); T.at(tData ).move(M.at(sData, 0));
    P.parallelEnd();
   }

  void lastElement()                                                            // Last element
   {zz(); action = "lastElement";
    //size();
    //isEmpty();
    //assertNotEmpty();
    //setFound();
    T.at(index).add(M.at(currentSize), -1);
    //T.at(index).dec();
    moveKeyData();
   }

  void zeroLastKey()                                                            // Save last key in the transaction buffer and zero it in the stuck
   {zz(); action = "zeroLastKey";
    //size();
    //isEmpty();
    //assertNotEmpty();
    //setFound();
    T.at(index).add(M.at(currentSize), -1);
    //T.at(index).dec();
    moveKey();
    key().zero();
   }

  void setLastKey()                                                             // Set the last active key in the stuck from the one in the transaction buffer
   {zz(); action = "setLastKey";
    //size();
    //isEmpty();
    //assertNotEmpty();
    //setFound();
    T.at(index).add(M.at(currentSize), -1);
    //T.at(index).dec();
    setKey();
   }

//void search()                                                                 // Search for an element within all elements of the stuck
// {zz(); action = "search";
//  size();
//
//  P.new If(T.at(limit))                                                       // Better if we had a separate routine for search a branch versus a leaf
//   {void Then()
//     {T.at(size).dec();
//     }
//   };
//
//  T.setIntInstruction(found, 0, index, 0);                                    // Assume we will not find a match
//
//  P.new Block()
//   {void code()
//     {for (int I = 0; I < maxSize(); I++)                                     // Search
//       {final int i = I;
//        //T.setIntInstruction(index, i);
//        T.at(index).equal(T.at(size), T.at(equal));
//        P.GoOn(end, T.at(equal));                                             // Reached the upper limit of the stuck
//        moveKey();
//        T.at(tKey).equal(T.at(search), T.at(equal));
//        P.new If (T.at(equal))                                                // Found an equal key
//         {void Then()
//           {setFound();
//            moveData();
//            if (i != maxSize()-1) P.Goto(end);                                // Goto superfluous on last iteration
//           }
//         };
//        if (i != maxSize()-1) T.setIntInstruction(index, i+1);                // Not needed on last iteration
//       }
//     }
//   };
// }

  void search2                                                                  // Search for an element within all elements of the stuck
   (MemoryLayoutPA.At Search, MemoryLayoutPA.At Found,
    MemoryLayoutPA.At Index,  MemoryLayoutPA.At Data)
   {zz(); action = "search";

    P.new I()
     {void a()
       {Found.setOff().setInt(0);                                               // Assume we will not find the key
        final int s = Search           .setOff().getInt();                      // Key to search for
        final int N = M.at(currentSize).setOff().getInt();                      // Number of elements to search

        for (int i = 0; i < N; i++)                                             // Search
         {z();
          final int k = M.at(sKey, i)  .setOff().getInt();                      // Current key
          if (k == s)                                                           // Current key equals search key with limits of a java integer
           {Found.setInt(1);                                                    // Show found
            Index.setInt(i);                                                    // Index of key found
            Data .setInt(M.at(sData, i).setOff().getInt());                     // Data associated with key found
            break;
           }
         }
       }
      String v()
       {final StringBuilder v = new StringBuilder();                            // Verilog
        final String        s = Search.verilogLoad();
        final String        c = M.at(currentSize).verilogLoad();
        final int           N = maxSize();

        v.append("/* StuckPA.search */\n");
        v.append(Found.verilogLoad()+" <= ( 0\n");                              // Found
        for (int i = 0; i < N; i++)
         {v.append(" || ("+M.at(sKey, i).verilogLoad()+" == "+s+" &&  "+i+ " < "+c+")\n");
         }
        v.append(") ? 1 : 0;\n");

        v.append(Index.verilogLoad()+" <=\n");                                  // Index of found key if any
        for (int i = 0; i < N; i++)
         {v.append(    "("+M.at(sKey, i).verilogLoad()+" == "+s+" && "+i+ " < "+c+") ? "+i+" :\n");
         }
        v.append("0;\n");

        v.append(Data.verilogLoad()+" <=\n");                                   // Data of found key if any
        for (int i = 0; i < N; i++)
         {v.append(    "("+M.at(sKey, i).verilogLoad()+" == "+s+" && "+i+ " < "+c+") ? "+M.at(sData, i).verilogLoad()+" :\n");
         }
        v.append("0;\n");
        return v.toString();
       }
     };
   }

  void search                                                                   // Search for an element within all elements of the stuck using multiple instructions with shorter paths than the original one instruction solution.
   (MemoryLayoutPA.At Search, MemoryLayoutPA.At Found,
    MemoryLayoutPA.At Index,  MemoryLayoutPA.At Data)
   {zz(); action = "search";

    P.new I()                                                                   // Equals search key, in valid part of  stuck
     {void a()
       {Found.setOff().setInt(0);                                               // Assume we will not find the key
        final int s = Search           .setOff().getInt();                      // Key to search for
        final int X = M.at(currentSize).setOff().getInt();                      // Number of elements to search
        final int N = maxSize();                                                // Maximum number of elements to search
        for (int i = 0; i < N; i++)                                             // Search
         {z();
          final int k = M.at(sKey, i)  .setOff().getInt();                      // Current key
          T.at(equalLeafKey,     i).setInt(k == s ? 1 : 0);
          T.at(lessThanLeafSize, i).setInt(i <  X ? 1 : 0);
         }
       }
      String v()
       {final StringBuilder v = new StringBuilder();                            // Verilog
        final String        s = Search.verilogLoad();                           // Search key
        final String        X = M.at(currentSize).verilogLoad();                // Number of elements to search
        final int           N = maxSize();

        v.append("/* StuckPA.search2.1 */\n");
        for (int i = 0; i < N; i++)
         {v.append(T.at(equalLeafKey,     i).verilogLoad()+" <= "+M.at(sKey, i).verilogLoad()+" == "+s+";\n");
          v.append(T.at(lessThanLeafSize, i).verilogLoad()+" <= "+i+                          " < " +X+";\n");
         }
        return ""+v;
       }
     };

    P.new I()                                                                   // Equals search key and in valid part of stuck
     {void a()
       {Found.setOff().setInt(0);                                               // Assume we will not find the key
        final int N = maxSize();                                                // Maximum number of elements to search
        for (int i = 0; i < N; i++)                                             // Search
         {z();
          final boolean e = T.at(equalLeafKey,     i).setOff().getInt() > 0;
          final boolean v = T.at(lessThanLeafSize, i).setOff().getInt() > 0;
          T.at(equalLeafKey,     i).setInt(v && e ? 1 : 0);
         }
       }
      String v()
       {final StringBuilder v = new StringBuilder();                            // Verilog
        final int           N = maxSize();

        v.append("/* StuckPA.search2.2 */\n");
        for (int i = 0; i < N; i++)
         {v.append(T.at(equalLeafKey,     i).verilogLoad()+" <= ");
          v.append(T.at(equalLeafKey,     i).verilogLoad()+" && ");
          v.append(T.at(lessThanLeafSize, i).verilogLoad()+";\n");
         }
        return ""+v;
       }
     };

    P.new I()                                                                   // Found or not
     {void a()
       {Found.setOff().setInt(0);                                               // Assume we will not find the key
        final int N = maxSize();                                                // Maximum number of elements to search
        boolean found = false;
        Found.setOff();
        Index.setOff();
        Data .setOff();
        for (int i = 0; i < N; i++)                                             // Search
         {z();
          final boolean f = T.at(equalLeafKey, i).setOff().getInt() > 0;        // Whether this key is in range and is equal
          Found.setInt((f || Found.setOff().getInt() > 0) ? 1 : 0);             // Any key matched ?
          if (f) Index.setInt(i);                                               // Save index if this key matched
          if (f) Data .setInt(M.at(sData, i).setOff().getInt());                // Save data if this key matched
         }
       }
      String v()
       {final StringBuilder v = new StringBuilder();                            // Verilog
        final int           N = maxSize();

        v.append("/* StuckPA.search2.3 */\n");
        v.append(Found.verilogLoad()+" <= 0");                                  // Found
        for (int i = 0; i < N; i++) v.append(" || "+T.at(equalLeafKey, i).verilogLoad() + " > 0");
        v.append(";\n");

        v.append(Index.verilogLoad()+" <= ");                                   // Index
        for (int i = 0; i < N; i++) v.append(T.at(equalLeafKey, i).verilogLoad() + " > 0 ? "+i+" : ");
        v.append("0;\n");                                                       // Not found so it can be anything

        v.append(Data.verilogLoad()+" <= ");                                    // Data
        for (int i = 0; i < N; i++) v.append(T.at(equalLeafKey, i).verilogLoad() + " > 0 ? "+M.at(sData, i).verilogLoad()+" : ");
        v.append("0;\n");                                                       // Not found so it can be anything
        return ""+v;
       }
     };
   }

//void searchFirstGreaterThanOrEqual(boolean all)                                 // Search for first greater than or equal
// {zz(); action = "searchFirstGreaterThanOrEqual";
//  size();
//
//  //if (!all)
//   {P.new If(T.at(limit))                                                       // Better if we had a separate routine for search a branch versus a leaf
//     {void Then()
//       {T.at(size).dec();
//       }
//     };
//   }
//
//  //T.at(index).move(T.at(size));                                               // Index top if no match found
//  T.setIntInstruction(found, 0, index, 0);                                    // Assume we will not find a match
//
//  P.new Block()
//   {void code()
//     {for (int I = 0; I < maxSize(); I++)                                     // Search
//       {final int i = I;
//        //T.setIntInstruction(index, i);
//        T.at(index).equal(T.at(size), T.at(equal));
//        P.GoOn(end, T.at(equal));                                             // Reached the upper limit of the stuck
//        moveKey();
//        T.at(tKey).greaterThanOrEqual(T.at(search), T.at(equal));
//        P.new If (T.at(equal))                                                // Found an equal key or greater
//         {void Then()
//           {setFound();
//            moveKey();
//            moveData();
//            if (i != maxSize()-1) P.Goto(end);                                // Goto superfluous on last iteration
//           }
//         };
//        if (i != maxSize()-1) T.setIntInstruction(index, i+1);                // Not needed on last iteration
//       }
//     }
//   };
// }

  void searchFirstGreaterThanOrEqual2(boolean all,                              // Search for an element within all elements of the stuck or all but one
    MemoryLayoutPA.At Search, MemoryLayoutPA.At Found,
    MemoryLayoutPA.At Index,
    MemoryLayoutPA.At Key,    MemoryLayoutPA.At Data)
   {zz(); action = "search";
    if (Search == null) stop("Search must not be null");
    //if (Found  == null) err("Found");
    //if (Index  == null) err("Index");
    //if (Key    == null) err("Key");
    //if (Data   == null) err("Data");

    P.new I()
     {void a()
       {if (Found != null) Found.setInt(0);                                     // Assume we will not find the key
        final int s = Search.setOff().getInt();                                 // Key to search for

        final int c = M.at(currentSize).setOff().getInt();                      // Number of elements in stuck
        final int N = c - (all ? 0 : 1);                                        // Number of elements to search
        if (Index != null) Index.setInt(N);                                     // Default index if no key matches
        if (Data  != null) Data.setInt(M.at(sData, N).setOff().getInt());       // Default data  if no key matches

        for  (int i = 0; i < N; i++)                                            // Search
         {z();
          final int k = M.at(sKey, i).setOff().getInt();
          if (Key != null) Key.setInt(k);                                       // Key being considered
          if (k >= s)                                                           // Current key greater than or equal to search key with limits of a java integer
           {if (Found != null) Found.setInt(1);                                 // Show found
            if (Index != null) Index.setInt(i);                                 // Index of key found
            if (Data  != null) Data .setInt(M.at(sData, i).setOff().getInt());  // Data associated with key found
            break;
           }
         }
       }

      String v()
       {final StringBuilder v = new StringBuilder();                            // Verilog
        final String        s = Search == null ? T.at(search).verilogLoad() : Search.verilogLoad();
        final String        c = M.at(currentSize).verilogLoad() + (all ? "-0" : "-1");
        final int           N = maxSize();

        v.append("/* StuckPA.searchFirstGreaterThanOrEqual */\n");
        if (Found != null)                                                      // Found
         {v.append(Found.verilogLoad()+" <= ( 0\n");
          for (int i = 0; i < N; i++)
           {v.append(" || ("+M.at(sKey, i).verilogLoad()+" >= "+s+" &&  "+i+ " < "+c+")\n");
           }
          v.append(") ? 1 : 0;\n");
         }

        if (Index != null)                                                      // Index of found
         {v.append(Index.verilogLoad()+" <=\n");
          for (int i = 0; i < N; i++)
           {v.append(    "("+M.at(sKey, i).verilogLoad()+" >= "+s+" && "+i+ " < "+c+") ? "+i+" :\n");
           }
          v.append(c+";\n");
         }

        if (Key != null)                                                        // Greater than or equal key
         {v.append(Key.verilogLoad()+" <=\n");
          for (int i = 0; i < N; i++)
           {v.append(    "("+M.at(sKey, i).verilogLoad()+" >= "+s+" && "+i+ " < "+c+") ? "+M.at(sKey, i).verilogLoad()+" :\n");
           }
          v.append("0;\n");
         }

        if (Data != null)
         {v.append(Data.verilogLoad()+" <=\n");                                 // Data of found
          for (int i = 0; i < N; i++)
           {v.append(    "("+M.at(sKey, i).verilogLoad()+" >= "+s+" && "+i+ " < "+c+") ? "+M.at(sData, i).verilogLoad()+" :\n");
           }
          if (all) v.append(M.at(sData, M.at(currentSize)).verilogLoad() + ";\n");              // Normal address
          else     v.append(M.at(sData, M.at(currentSize)).verilogLoadAddr(false, -1) + ";\n"); // Address one back
         }
        return v.toString();
       }
     };
   }

  void searchFirstGreaterThanOrEqual(boolean all,                               // Search for the first matching element in the stuck greater than or equal to the search element using multiple instructions with shorter paths than the original one instruction solution.
    MemoryLayoutPA.At Search, MemoryLayoutPA.At Found,
    MemoryLayoutPA.At Index,
    MemoryLayoutPA.At Key,    MemoryLayoutPA.At Data)
   {zz(); action = "search";

    P.new I()                                                                   // Matching key, in valid part of  stuck
     {void a()
       {final int s = Search           .setOff().getInt();                      // Key to search for
        final int X = M.at(currentSize).setOff().getInt() - (all ? 0 : 1);      // Number of elements to search
        final int N = maxSize();                                                // Maximum number of elements to search
        for (int i = 0; i < N; i++)                                             // Search
         {z();
          final int k = M.at(sKey, i)  .setOff().getInt();                      // Current key
          T.at(equalLeafKey,     i).setInt(k >= s ? 1 : 0);
          T.at(lessThanLeafSize, i).setInt(i <  X ? 1 : 0);
         }
       }
      String v()
       {final StringBuilder v = new StringBuilder();                            // Verilog
        final String s = Search.verilogLoad();                                  // Search key
        final String X = M.at(currentSize).verilogLoad()+ (all ? "" : "-1");    // Number of elements to search
        final int    N = maxSize();

        v.append("/* StuckPA.search2.1 */\n");
        for (int i = 0; i < N; i++)
         {v.append(T.at(equalLeafKey,     i).verilogLoad()+" <= "+M.at(sKey, i).verilogLoad()+" >= "+s+";\n");
          v.append(T.at(lessThanLeafSize, i).verilogLoad()+" <= "+i+                          " < " +X+";\n");
         }
        return ""+v;
       }
     };

    P.new I()                                                                   // Matching key and in valid part of stuck
     {void a()
       {final int N = maxSize();                                                // Maximum number of elements to search
        for (int i = 0; i < N; i++)                                             // Search
         {z();
          final boolean e = i > 0 ? T.at(equalLeafKey,     i-1).setOff().getInt() > 0 : false; // Next    value
          final boolean E =         T.at(equalLeafKey,     i  ).setOff().getInt() > 0;         // Current value
          final boolean v =         T.at(lessThanLeafSize, i  ).setOff().getInt() > 0;         // In range
          final int     r = !e && E && v ? 1 : 0;
          T.at(lessThanLeafSize, i).setInt(r);                                  // Need to preserve equalLeafKey unchanged to allow previous element to be referenced
         }
       }
      String v()
       {final StringBuilder v = new StringBuilder();                            // Verilog
        final int           N = maxSize();

        v.append("/* StuckPA.search2.2 */\n");
        for (int i = 0; i < N; i++)
         {v.append(T.at(lessThanLeafSize, i).verilogLoad()+" <= ");
          v.append(T.at(equalLeafKey,     i).verilogLoad()+" >  0 && ");
          if (i > 0)                                                            // The previous element must be zero unless it is the first one
           {v.append(T.at(equalLeafKey,-1+i).verilogLoad()+" == 0 && ");
           }
          v.append(T.at(lessThanLeafSize, i).verilogLoad()+";\n");
         }
        return ""+v;
       }
     };

    P.new I()                                                                   // Found or not
     {void a()
       {if (Found != null) Found.setOff().setInt(0);                            // Assume we will not find the key
        final int N = maxSize();                                                // Maximum number of elements to search
        final int c = M.at(currentSize).setOff().getInt();                      // Number of elements in stuck
        final int C = c - (all ? 0 : 1);                                        // Number of elements to search
        if (Index != null) Index.setInt(C);                                     // Default index if no key matches
        if (Data  != null) Data.setInt(M.at(sData, C).setOff().getInt());       // Default data  if no key matches

        for (int i = 0; i < N; i++)                                             // Search
         {z();
          final boolean f = T.at(lessThanLeafSize, i).setOff().getInt() > 0;    // Whether this key is in range and is greater than or equal to the search key
          if (Found != null) Found.setInt((f || Found.setOff().getInt() > 0) ? 1 : 0); // Any key matched ?
          if (f && Index != null) Index.setInt(i);                              // Save index if this key matched
          if (f && Key   != null) Key .setInt(M.at(sKey,  i).setOff().getInt());// Save matching key
          if (f && Data  != null) Data.setInt(M.at(sData, i).setOff().getInt());// Save data if this key matched
         }
       }
      String v()
       {final StringBuilder v = new StringBuilder();                            // Verilog
        final int           N = maxSize();

        v.append("/* StuckPA.search2.3 */\n");
        if (Found != null)                                                      // Found
         {v.append(Found.verilogLoad()+" = 0");
          for (int i = 0; i < N; i++) v.append(" || "+T.at(equalLeafKey, i).verilogLoad() + " > 0");
          v.append(";\n");
         }

        if (Index != null)                                                      // Index
         {v.append(Index.verilogLoad()+" = ");
          for (int i = 0; i < N; i++) v.append(T.at(equalLeafKey, i).verilogLoad() + " > 0 ? "+i+" : ");
          if (all) v.append(M.at(currentSize).verilogLoad() + ";\n");           // Normal index
          else     v.append(M.at(currentSize).verilogLoad() + " -1;\n");        // Index one back
         }

        if (Key != null)                                                        // Key
         {v.append(Key.verilogLoad()+" = ");
          for (int i = 0; i < N; i++) v.append(T.at(equalLeafKey, i).verilogLoad() + " > 0 ? "+M.at(sKey, i).verilogLoad()+" : ");
          v.append("0;\n");                                                     // Not found so it can be anything
         }

        if (Data != null)
         {v.append(Data.verilogLoad()+" = ");                                  // Data
          for (int i = 0; i < N; i++) v.append(T.at(equalLeafKey, i).verilogLoad() + " > 0 ? "+M.at(sData, i).verilogLoad()+" : ");
          if (all) v.append(M.at(sData, M.at(currentSize)).verilogLoad() + ";\n");              // Normal address
          else     v.append(M.at(sData, M.at(currentSize)).verilogLoadAddr(false, -1) + ";\n"); // Address one back
         }
        return ""+v;
       }
     };
   }

//D1 Merge and Split                                                            // Merge and split stucks

  void checkSameProgram(StuckPA source)                                         // Confirm that we are writing into the same program
   {final int p = P.number;
    if (p != source.M.P.number) stop("Mismatched programs");
    if (p != source.T.P.number) stop("Mismatched programs");
    if (p != source.P.number)   stop("Mismatched programs");
    if (p != M.P.number)        stop("Mismatched programs");
    if (p != T.P.number)        stop("Mismatched programs");
   }

  void copy(StuckPA Source)                                                     // Copy the source stuck into the target replacing the target completely
   {zz(); action = "copy";
    checkSameProgram(Source);                                                   // Confirm that we are writing into the same program
    final StuckPA Target = this;
    Target.M.copy(Source.M);                                                    // Copy the source into the target
   }

  void concatenate(StuckPA Source)                                              // Concatenate two stucks placing the source at the end of the target while not modifying the source.
   {zz(); action = "concatenate";
    checkSameProgram(Source);                                                   // Confirm that we are writing into the same program
    final StuckPA Target = this;

    P.parallelStart();   size();                                                // Get the size of the stucks
    P.parallelSection(); Source.size();
    P.parallelEnd();

    P.parallelStart();   Source.T.at(Source.index).zero();                      // Start at start of source
    P.parallelSection(); Target.T.at(Target.index    ).move(Target.T.at(Target.size)); // Extend target
    P.parallelSection(); Target.T.at(Target.copyCount).move(Source.T.at(Source.size)); // Number of elements to copy into the target
    P.parallelEnd();

    Target.copyKeysData(Source);                                                // Copy keys
    P.new I()                                                                   // Update size of target
     {void a()
       {Target.T.at(Target.size).setInt
         (Target.T.at(Target.size).getInt() +
          Source.T.at(Source.size).getInt());
       }
      String v()
       {return Target.T.at(Target.size).verilogLoad() + " <= " +
               Target.T.at(Target.size).verilogLoad() + " +  " +
               Source.T.at(Source.size).verilogLoad() + ";";
       }
     };
    Target.setSize();
   }

  void prepend(StuckPA Source)                                                  // Concatenate two stucks placing the source at the start of the target while (apparently) not modifying the target
   {zz(); action = "prepend";
    checkSameProgram(Source);                                                   // Confirm that we are writing into the same program
    final StuckPA Target = this;

    P.parallelStart();   size();                                                // Get the size of the stucks
    P.parallelSection(); Source.size();
    P.parallelEnd();

    P.parallelStart();   Target.T.setIntInstruction(Target.index, 0);                  // Concatenate the target to the source
    P.parallelSection(); Source.T.at(Source.index    ).move(Source.T.at(Source.size)); // Extend source
    P.parallelSection(); Source.T.at(Source.copyCount).move(Target.T.at(Target.size)); // Number of elements to copy into the target
    P.parallelEnd();

    Source.copyKeysData(Target);                                                // Copy keys
    Target.copy(Source);                                                        // Copy the source to the target leaving the length of the source unchanged so it looks as if it has not been changed

    P.new I()                                                                   // Update size of target
     {void a()
       {Target.T.at(Target.size).setInt
         (Target.T.at(Target.size).getInt() +
          Source.T.at(Source.size).getInt());
       }
     };
    Target.setSize();
   }

  void split(StuckPA Low, StuckPA High)                                         // Split a full stuck into two halves
   {zz(); action = "split";
    final int H = Low.maxSize()>>1;                                             // Should check theat Low, High, Source all have the same shape
    final StuckPA Source = this;
    checkSameProgram(Low); checkSameProgram(High);                              // Confirm that we are writing into the same program

    Low.copy(Source);                                                           // Copy source into low stuck

    P.new I()                                                                   // Set size of low
     {void   a() {Low.T.at(Low.size).setInt(H);}                                // Size of half in elements
      String v() {return Low.T.at(Low.size).verilogLoad() + " <= "+H+";";}      // Size of half in elements
     };

    P.parallelStart();   Low.setSize();                                         // Set size of lower half
    P.parallelSection(); High  .T.setIntInstruction(High.index    , 0);         // High takes the upper half
    P.parallelSection(); High  .T.setIntInstruction(High.copyCount, H);         // Number of elements to copy into the target
    P.parallelSection(); Source.T.setIntInstruction(Source.index, Source.maxSize() - H); // Upper half
    P.parallelEnd();

    P.parallelStart();   High  .copyKeysData(Source);                           // Copy keys
    P.parallelSection(); High.T.setIntInstruction(High.size, H);
    P.parallelEnd();
    High.setSize();
   }

  void splitLow(StuckPA Low)                                                    // Split out the lower half of a full stuck
   {zz(); action = "splitLow";
    final int H = Low.maxSize()>>1;                                             // Should check theat Low, High, Source all have the same shape
    final StuckPA Source = this;
    checkSameProgram(Low);                                                      // Confirm that we are writing into the same program

    Low.copy(Source);

    Low.T.setIntInstruction(Low.size, Source.maxSize() - H);                    // Size of half in elements
    Low.setSize();                                                              // Set size of lower half

    P.parallelStart();   Source.T.setIntInstruction(Source.index,     0);       // Source - high - takes the upper half moved down
    P.parallelSection(); Source.T.setIntInstruction(Source.copyCount, H);       // Number of elements to copy into source
    P.parallelSection(); Low   .T.setIntInstruction(Low   .index,     Source.maxSize() - H); // Upper half
    P.parallelEnd();
    Source.copyKeysData(Low);                                                   // Copy keys back into source at the low position

    Source.T.setIntInstruction(Source.size, H);
    Source.setSize();
   }

  void splitHigh(StuckPA High)                                                  // Split out the upper half of a full stuck
   {z(); action = "splitHigh";
    final int H = High.maxSize()>>1;                                            // Should check theat Low, High, Source all have the same shape
    final StuckPA Source = this;
    checkSameProgram(High);                                                     // Confirm that we are writing into the same program

    Source.T.setIntInstruction(Source.size, Source.maxSize() - H);              // Size of half in elements
    Source.setSize();                                                           // Set size of lower half in source

    P.parallelStart();Source.T.setIntInstruction(Source.index      , Source.maxSize() - H); // High takes the upper half
    P.parallelSection();High  .T.setIntInstruction(Source.copyCount, H);        // Number of elements to copy into the target
    P.parallelSection();High  .T.setIntInstruction(High  .index    , 0);        // Upper half
    P.parallelEnd();

    High.copyKeysData(Source);                                                  // Copy keys

    High.T.setIntInstruction(High.size, H);                                     // New size of high
    High.setSize();
   }

//D1 Print                                                                      // Print a stuck

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

//D0 Testing                                                                    // Test the stuck

  static StuckPA stuckPA()                                                      // Create a sample stuck
   {z();
    return  new StuckPA("original")
     {int maxSize     () {return  8;}
      int bitsPerKey  () {return 16;}
      int bitsPerData () {return 16;}
      int bitsPerSize () {return 16;}
     };
   }

  public void ok(String expected) {ok(toString(), expected);}                   // Check the stuck

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
    s.P.run(); s.P.clear();
    ok(s.T.at(s.size).getInt(), 4);
    s.clear();
    s.P.run(); s.P.clear();
    //stop(s);
    ok(s, """
StuckSML(maxSize:8 size:0)
""");
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
MemoryLayout: original_StuckSA_Memory_Based
Memory      : original_StuckSA_Memory_Backing
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0       272                                      stuck
   2 V        0        16                                  4     currentSize
   3 A       16       128          8                             Keys
   4 V       16        16               0                 15       key
   5 V       32        16               1                 14       key
   6 V       48        16               2                 13       key
   7 V       64        16               3                 12       key
   8 V       80        16               4                  0       key
   9 V       96        16               5                  0       key
  10 V      112        16               6                  0       key
  11 V      128        16               7                  0       key
  12 A      144       128          8                             Data
  13 V      144        16               0                  9       data
  14 V      160        16               1                 10       data
  15 V      176        16               2                 11       data
  16 V      192        16               3                 12       data
  17 V      208        16               4                  0       data
  18 V      224        16               5                  0       data
  19 V      240        16               6                  0       data
  20 V      256        16               7                  0       data
""");
    //stop(s.M.memory());
    ok(s.M.memory(), """
Memory: original_StuckSA_Memory_Backing
      4... 4... 4... 4... 3... 3... 3... 3... 2... 2... 2... 2... 1... 1... 1... 1...
Line  FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210
   0  0000 0000 0000 000c 000b 000a 0009 0000 0000 0000 0000 000c 000d 000e 000f 0004
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

    if (Assert)
     {s.P.new I() {void a() {sayThisOrStop("Stuck full");}};
      s.T.at(s.tKey).setInt(7); s.T.at(s.tData).setInt(7); s.push();            // Will produce a traceback clickable in Geany because the stuck is now full
      try {s.P.run();} catch(RuntimeException e) {}
     }
   }

  static void test_pop()
   {StuckPA s = test_load();

    s.pop();
    s.P.run();
    //stop(s.print());
//    ok(s.print(), """
//Transaction(action:pop search:0 limit:0 found:1 index:3 key:8 data:4 size:3 isFull:0 isEmpty:0)
//""");
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

    if (Assert)
     {s.P.new I() {void a() {sayThisOrStop("Empty");}};
      s.pop();
      try {s.P.run();} catch(RuntimeException e) {}
     }
   }

  static void test_shift()
   {StuckPA s = test_load();

    s.shift();
    s.P.run(); s.P.clear();
//    ok(s.print(), """
//Transaction(action:shift search:0 limit:0 found:1 index:0 key:2 data:1 size:3 isFull:0 isEmpty:0)
//""");
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
    //stop(s);
    ok(s, """
StuckSML(maxSize:8 size:0)
""");
    if (Assert)
     {sayThisOrStop("Empty");
      s.pop();
      try {s.P.run();} catch(RuntimeException e) {}
     }
   }

  static void test_unshift()
   {StuckPA s = test_load();
    final MemoryLayoutPA m = s.T;

    s.T.at(s.tKey).setInt(9); s.T.at(s.tData).setInt(9);
    s.unshift();
    s.P.run(); s.P.clear();
    //stop(s);
//    ok(s.print(), """
//Transaction(action:unshift search:0 limit:0 found:1 index:0 key:9 data:9 size:5 isFull:0 isEmpty:0)
//""");
    //stop(s);
    ok(s, """
StuckSML(maxSize:8 size:5)
  0 key:9 data:9
  1 key:2 data:1
  2 key:4 data:2
  3 key:6 data:3
  4 key:8 data:4
""");

//  ok(s.T.at(s.isFull).getInt() == 0);
    s.unshift(); s.unshift(); s.unshift();
    s.P.run(); s.P.clear();
//  ok(s.T.at(s.isFull).getInt() == 1);
    //stop(s);
    ok(s, """
StuckSML(maxSize:8 size:8)
  0 key:9 data:9
  1 key:9 data:9
  2 key:9 data:9
  3 key:9 data:9
  4 key:2 data:1
  5 key:4 data:2
  6 key:6 data:3
  7 key:8 data:4
""");

    if (Assert)
     {sayThisOrStop("Stuck full");
      s.unshift();
      try {s.P.run();} catch(RuntimeException e) {}
     }
   }

  static void test_elementAt()
   {StuckPA s = test_load();

    s.T.at(s.index).setInt(2);
    s.elementAt();
    s.P.run(); s.P.clear();
    //stop(s);
//    ok(s.print(), """
//Transaction(action:elementAt search:0 limit:0 found:1 index:2 key:6 data:3 size:4 isFull:0 isEmpty:0)
//""");

    s.T.at(s.index).setInt(-2);

    if (Assert)
     {sayThisOrStop("Out of normal range 65534 for size 4");
      s.elementAt();
      try {s.P.run();} catch(RuntimeException e) {} finally {s.P.clear();}
     }

    if (Assert)
     {s.T.at(s.index).setInt(4);
      sayThisOrStop("Out of normal range 4 for size 4");
      try {s.P.run();} catch(RuntimeException e) {} finally {s.P.clear();}
     }
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

//    s.P.new I() {void a() {s.T.at(s.tKey ).setInt(88);}};
//    s.P.new I() {void a() {s.T.at(s.tData).setInt(99);}};
//    s.P.new I() {void a() {s.T.at(s.index).setInt( 4);}};
//    s.setElementAt();
//    s.P.run(); s.P.clear();
//    //stop(s);
//    ok(s, """
//StuckSML(maxSize:8 size:5)
//  0 key:2 data:1
//  1 key:4 data:2
//  2 key:22 data:33
//  3 key:8 data:4
//  4 key:88 data:99
//""");

    s.P.new I() {void a() {s.T.at(s.index).setInt(-2);}};

    if (Assert)
     {sayThisOrStop("Out of normal range 65534 for size 5");
      s.setElementAt();
      try {s.P.run();} catch(RuntimeException e) {} finally {s.P.clear();}
     }

    if (Assert)
     {s.P.new I() {void a() {s.T.at(s.index).setInt(6);}};
      sayThisOrStop("Out of normal range 6 for size 5");
      s.setElementAt();
      try {s.P.run();} catch(RuntimeException e) {} finally {s.P.clear();}
     }
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

    if (Assert)
     {sayThisOrStop("Out of extended range 7 for size 6");
      s.insertElementAt();
      try {s.P.run();} catch(RuntimeException e) {} finally {s.P.clear();}
     }
   }

  static void test_remove_element_at()
   {StuckPA s = test_load();

    s.P.new I() {void a() {s.T.at(s.index).setInt(2);}};
    s.removeElementAt();
    s.P.run(); s.P.clear();
    //stop(t);
//    ok(s.print(), """
//Transaction(action:removeElementAt search:0 limit:0 found:1 index:2 key:6 data:3 size:3 isFull:0 isEmpty:0)
//""");

    //stop(s);
    ok(s, """
StuckSML(maxSize:8 size:3)
  0 key:2 data:1
  1 key:4 data:2
  2 key:8 data:4
""");

    s.P.new I() {void a() {s.T.at(s.index).setInt(3);}};

    if (Assert)
     {sayThisOrStop("Out of normal range 3 for size 3");
      s.removeElementAt();
      try {s.P.run();} catch(RuntimeException e) {} finally {s.P.clear();}
     }
   }

  static void test_first_last()
   {StuckPA s = test_load();

    s.firstElement();
    s.P.run(); s.P.clear();
    //stop(t);
//    ok(s.print(), """
//Transaction(action:firstElement search:0 limit:0 found:1 index:0 key:2 data:1 size:4 isFull:0 isEmpty:0)
//""");

    s.lastElement();
    s.P.run(); s.P.clear();
    //stop(s);
//    ok(s.print(), """
//Transaction(action:lastElement search:0 limit:0 found:1 index:3 key:8 data:4 size:4 isFull:0 isEmpty:0)
//""");

    s.clear();
    if (Assert)
     {sayThisOrStop("Empty");
      s.firstElement();
      try {s.P.run();} catch(RuntimeException e) {} finally {s.P.clear();}
     }
   }

  static void test_search()
   {StuckPA s = test_load();

    Layout               l = Layout.layout();
    Layout.Variable  k = l.variable ("k", s.bitsPerKey());
    Layout.Variable  f = l.bit      ("f");
    Layout.Variable  i = l.variable ("i", s.bitsPerSize());
    Layout.Variable  d = l.variable ("d", s.bitsPerData());
    Layout.Structure S = l.structure("S", k, f, i, d);
    MemoryLayoutPA   m = new MemoryLayoutPA(l.compile(), "m");
                     m.program(s.P);

    m.setIntInstruction(k, 2);
    s.search(m.at(k), m.at(f), m.at(i), m.at(d));
    s.P.run(); s.P.clear();
    //stop(s);
//  ok(s.print(), """
//Transaction(action:search search:2 limit:0 found:1 index:0 key:2 data:1 size:4 isFull:0 isEmpty:0)
//""");
    //stop(m);
    ok(m, """
MemoryLayout: m
Memory      : m
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        49                                      S
   2 V        0        16                                  2     k
   3 B       16         1                                  1     f
   4 V       17        16                                  0     i
   5 V       33        16                                  1     d
""");

    m.setIntInstruction(k, 3);
    s.search(m.at(k), m.at(f), m.at(i), m.at(d));
    s.P.run(); s.P.clear();
//  //stop(s);
//    ok(s.print(), """
//Transaction(action:search search:3 limit:0 found:0 index:4 key:8 data:1 size:4 isFull:0 isEmpty:0)
//""");
    ok(m.at(f).getInt(), 0);

    m.setIntInstruction(k, 8);
    s.search(m.at(k), m.at(f), m.at(i), m.at(d));
    s.P.run(); s.P.clear();
//  //stop(s);
//    ok(s.print(), """
//Transaction(action:search search:8 limit:0 found:1 index:3 key:8 data:4 size:4 isFull:0 isEmpty:0)
//""");
    //stop(m);
    ok(m, """
MemoryLayout: m
Memory      : m
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        49                                      S
   2 V        0        16                                  8     k
   3 B       16         1                                  1     f
   4 V       17        16                                  3     i
   5 V       33        16                                  4     d
""");
   }

  static void test_search2()
   {StuckPA s = test_load();

    Layout               l = Layout.layout();
    Layout.Variable  k = l.variable ("k", s.bitsPerKey());
    Layout.Variable  f = l.bit      ("f");
    Layout.Variable  i = l.variable ("i", s.bitsPerSize());
    Layout.Variable  d = l.variable ("d", s.bitsPerData());
    Layout.Structure S = l.structure("S", k, f, i, d);
    MemoryLayoutPA   m = new MemoryLayoutPA(l.compile(), "m");
                     m.program(s.P);

    m.setIntInstruction(k, 6);
    s.search2(m.at(k), m.at(f), m.at(i), m.at(d));
    s.P.run(); s.P.clear();

    //stop(s/print());
//  ok(s.print(), """
//Transaction(action:search search:2 limit:0 found:1 index:0 key:2 data:1 size:4 isFull:0 isEmpty:0)
//""");
    //stop(m);
    ok(m, """
MemoryLayout: m
Memory      : m
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        49                                      S
   2 V        0        16                                  6     k
   3 B       16         1                                  1     f
   4 V       17        16                                  2     i
   5 V       33        16                                  3     d
""");

    m.setIntInstruction(k, 3);
    s.search(m.at(k), m.at(f), m.at(i), m.at(d));
    s.P.run(); s.P.clear();
    ok(m.at(f).getInt(), 0);

    m.setIntInstruction(k, 8);
    s.search(m.at(k), m.at(f), m.at(i), m.at(d));
    s.P.run(); s.P.clear();
    //stop(m);
    ok(m, """
MemoryLayout: m
Memory      : m
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        49                                      S
   2 V        0        16                                  8     k
   3 B       16         1                                  1     f
   4 V       17        16                                  3     i
   5 V       33        16                                  4     d
""");
   }

//  static void test_search_except_last()
//   {StuckPA s = test_load();
//
//    s.P.new I() {void a() {s.T.at(s.limit ).setInt(1);}};
//    s.P.new I() {void a() {s.T.at(s.search).setInt(4);}};
//    s.search();
//    s.P.run(); s.P.clear();
//    //stop(s);
//    ok(s.print(), """
//Transaction(action:search search:4 limit:1 found:1 index:1 key:4 data:2 size:3 isFull:0 isEmpty:0)
//""");
//
//    s.P.new I() {void a() {s.T.at(s.search).setInt(8);}};
//    s.search();
//    s.P.run(); s.P.clear();
//    //stop(t);
//    ok(s.print(), """
//Transaction(action:search search:8 limit:1 found:0 index:3 key:6 data:2 size:3 isFull:0 isEmpty:0)
//""");
//   }
//
  static void test_search_first_greater_than_or_equal()
   {StuckPA s = test_load();
    Layout               l = Layout.layout();
    Layout.Variable  K = l.variable ("K", s.bitsPerKey());
    Layout.Variable  f = l.bit      ("f");
    Layout.Variable  i = l.variable ("i", s.bitsPerSize());
    Layout.Variable  k = l.variable ("k", s.bitsPerKey());
    Layout.Variable  d = l.variable ("d", s.bitsPerData());
    Layout.Structure S = l.structure("S", k, f, i, K, d);
    MemoryLayoutPA   m = new MemoryLayoutPA(l.compile(), "m");
                     m.program(s.P);

    m.setIntInstruction(k, 5);
    s.searchFirstGreaterThanOrEqual(true, m.at(k), m.at(f), m.at(i), m.at(K), m.at(d));
    s.P.run(); s.P.clear();
    //stop(s.T);
    //stop(m);
    ok(m, """
MemoryLayout: m
Memory      : m
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        65                                      S
   2 V        0        16                                  5     k
   3 B       16         1                                  1     f
   4 V       17        16                                  2     i
   5 V       33        16                                  6     K
   6 V       49        16                                  3     d
""");

    s.P.new I() {void a() {s.T.at(s.search).setInt(7);}};
    s.searchFirstGreaterThanOrEqual(true, s.T.at(s.search), s.T.at(s.found),
      s.T.at(s.index), s.T.at(s.tKey), s.T.at(s.tData));
    s.P.run(); s.P.clear();
    //stop(t);
//    ok(s.print(), """
//Transaction(action:searchFirstGreaterThanOrEqual search:7 limit:0 found:1 index:3 key:8 data:4 size:4 isFull:0 isEmpty:0)
//""");
    ok(s.T.at(s.found).getInt(), 1);
    ok(s.T.at(s.index).getInt(), 3);
    ok(s.T.at(s.tKey) .getInt(), 8);
    ok(s.T.at(s.tData).getInt(), 4);
   }

  static void test_search_first_greater_than_or_equal_except_last()
   {StuckPA s = test_load();
    Layout               l = Layout.layout();
    Layout.Variable  K = l.variable ("K", s.bitsPerKey());
    Layout.Variable  f = l.bit      ("f");
    Layout.Variable  i = l.variable ("i", s.bitsPerSize());
    Layout.Variable  k = l.variable ("k", s.bitsPerKey());
    Layout.Variable  d = l.variable ("d", s.bitsPerData());
    Layout.Structure S = l.structure("S", k, f, i, K, d);
    MemoryLayoutPA   m = new MemoryLayoutPA(l.compile(), "m");
                     m.program(s.P);

    //s.P.new I() {void a() {s.T.at(s.limit).setInt(1);}};
    m.setIntInstruction(k, 5);
    s.searchFirstGreaterThanOrEqual(false, m.at(k), m.at(f), m.at(i), m.at(K), m.at(d));
    s.P.run(); s.P.clear();
//  //stop(t);
//    ok(s.print(), """
//Transaction(action:searchFirstGreaterThanOrEqual search:5 limit:1 found:1 index:2 key:6 data:3 size:3 isFull:0 isEmpty:0)
//""");
    //stop(m);
    ok(m, """
MemoryLayout: m
Memory      : m
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        65                                      S
   2 V        0        16                                  5     k
   3 B       16         1                                  1     f
   4 V       17        16                                  2     i
   5 V       33        16                                  6     K
   6 V       49        16                                  3     d
""");

    s.P.new I() {void a() {s.T.at(s.search).setInt(7);}};
    s.searchFirstGreaterThanOrEqual(false, s.T.at(s.search),
                                           s.T.at(s.found), null, null, null);
    s.P.run(); s.P.clear();
//  //stop(t);
//    ok(s.print(), """
//Transaction(action:searchFirstGreaterThanOrEqual search:7 limit:1 found:0 index:3 key:6 data:3 size:3 isFull:0 isEmpty:0)
//""");
    ok(s.T.at(s.found).getInt(), 0);
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

    final StuckPA s = S.copyDef();
    s.P.new I() {void a() {s.base(M.at(c, M.at(a)).setOff().at);}};

    s.P.new I() {void a() {s.T.at(s.tKey).setInt(2); s.T.at(s.tData).setInt(1);}}; s.push();
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(4); s.T.at(s.tData).setInt(2);}}; s.push();
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(6); s.T.at(s.tData).setInt(3);}}; s.push();
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(8); s.T.at(s.tData).setInt(4);}}; s.push();
    s.P.run(); s.P.clear();

    //stop(s.M);
    ok(s.M, """
MemoryLayout: test_StuckSA_Memory_Based
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
MemoryLayout: test_StuckSA_Memory_Based
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
MemoryLayout: test_StuckSA_Memory_Based
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

  static void test_copy()                                                       // Copy one stuck into another
   {z();

    final MemoryLayoutPA m = new MemoryLayoutPA(1000);

    final StuckPA    s = new StuckPA("source", m)
     {int maxSize     () {return  4;}
      int bitsPerKey  () {return 16;}
      int bitsPerData () {return 16;}
      int bitsPerSize () {return 16;}
     };

    s.P.new I() {void a() {s.T.at(s.tKey).setInt(1); s.T.at(s.tData).setInt(2);}}; s.push();
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(2); s.T.at(s.tData).setInt(4);}}; s.push();
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(3); s.T.at(s.tData).setInt(6);}}; s.push();
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(4); s.T.at(s.tData).setInt(8);}}; s.push();
    s.P.run(); s.P.clear();

    final StuckPA    t = new StuckPA("target", m)
     {int maxSize     () {return  4;}
      int bitsPerKey  () {return 16;}
      int bitsPerData () {return 16;}
      int bitsPerSize () {return 16;}
     };

    t.copy(s);
    t.P.run(); t.P.clear();

    //stop(t);
    ok(t, """
StuckSML(maxSize:4 size:4)
  0 key:1 data:2
  1 key:2 data:4
  2 key:3 data:6
  3 key:4 data:8
""");
   }

  static void test_copy_keys_and_data()                                         // Copy part of one stuck into another
   {z();
    final StuckPA    s = new StuckPA("source")
     {int maxSize     () {return  8;}
      int bitsPerKey  () {return 16;}
      int bitsPerData () {return 16;}
      int bitsPerSize () {return 16;}
     };

    s.P.new I() {void a() {s.T.at(s.tKey).setInt(2); s.T.at(s.tData).setInt(1);}}; s.push();
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(4); s.T.at(s.tData).setInt(2);}}; s.push();
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(6); s.T.at(s.tData).setInt(3);}}; s.push();
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(8); s.T.at(s.tData).setInt(4);}}; s.push();
    s.P.run(); s.P.clear();

    final StuckPA    t = new StuckPA("target")
     {int maxSize     () {return  8;}
      int bitsPerKey  () {return 16;}
      int bitsPerData () {return 16;}
      int bitsPerSize () {return 16;}
     };

    t.P.new I() {void a() {s.T.at(s.index    ).setInt(1);}};
    t.P.new I() {void a() {t.T.at(t.index    ).setInt(2);}};
    t.P.new I() {void a() {t.T.at(t.copyCount).setInt(2);}};
    t.copyKeys(s);
    t.P.run(); t.P.clear();

    ok(s.M.memory(), """
Memory: source_StuckSA_Memory_Backing
      4... 4... 4... 4... 3... 3... 3... 3... 2... 2... 2... 2... 1... 1... 1... 1...
Line  FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210
   0  0000 0000 0000 0004 0003 0002 0001 0000 0000 0000 0000 0008 0006 0004 0002 0004
   1  0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000
""");

    ok(t.M.memory(), """
Memory: target_StuckSA_Memory_Backing
      4... 4... 4... 4... 3... 3... 3... 3... 2... 2... 2... 2... 1... 1... 1... 1...
Line  FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210
   0  0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0006 0004 0000 0000 0000
   1  0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000
""");

    t.P.new I() {void a() {t.T.at(t.copyCount).setInt(1);}};
    t.copyData(s);
    t.P.run(); t.P.clear();
    ok(t.M.memory(), """
Memory: target_StuckSA_Memory_Backing
      4... 4... 4... 4... 3... 3... 3... 3... 2... 2... 2... 2... 1... 1... 1... 1...
Line  FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210
   0  0000 0000 0000 0000 0002 0000 0000 0000 0000 0000 0000 0006 0004 0000 0000 0000
   1  0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000
""");

    t.T.at(t.size).setInt(5);
    t.setSize();
    t.P.run(); t.P.clear();
    //stop(t);
    ok(t, """
StuckSML(maxSize:8 size:5)
  0 key:0 data:0
  1 key:0 data:0
  2 key:4 data:2
  3 key:6 data:0
  4 key:0 data:0
""");
   }

  static void test_concatenate()
   {z();
    final MemoryLayoutPA m = new MemoryLayoutPA(1000);
    final StuckPA s = new StuckPA("source", m)
     {int maxSize     () {return  8;}
      int bitsPerKey  () {return 16;}
      int bitsPerData () {return 16;}
      int bitsPerSize () {return 16;}
     };

    s.P.new I() {void a() {s.T.at(s.tKey).setInt(5); s.T.at(s.tData).setInt(5);}}; s.push();
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(6); s.T.at(s.tData).setInt(6);}}; s.push();
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(7); s.T.at(s.tData).setInt(7);}}; s.push();
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(8); s.T.at(s.tData).setInt(8);}}; s.push();
    s.P.run(); s.P.clear();

    //stop(s);
    ok(s, """
StuckSML(maxSize:8 size:4)
  0 key:5 data:5
  1 key:6 data:6
  2 key:7 data:7
  3 key:8 data:8
""");

    final StuckPA t = new StuckPA("target", m)
     {int maxSize     () {return s.maxSize     ();}
      int bitsPerKey  () {return s.bitsPerKey  ();}
      int bitsPerData () {return s.bitsPerData ();}
      int bitsPerSize () {return s.bitsPerSize ();}
     };

    t.base(s.M.layout.size());

    t.P.new I() {void a() {t.T.at(t.tKey).setInt(1); t.T.at(t.tData).setInt(1);}}; t.push();
    t.P.new I() {void a() {t.T.at(t.tKey).setInt(2); t.T.at(t.tData).setInt(2);}}; t.push();
    t.P.new I() {void a() {t.T.at(t.tKey).setInt(3); t.T.at(t.tData).setInt(3);}}; t.push();
    t.P.new I() {void a() {t.T.at(t.tKey).setInt(4); t.T.at(t.tData).setInt(4);}}; t.push();
    t.P.run(); t.P.clear();

    //stop(t);
    ok(t, """
StuckSML(maxSize:8 size:4)
  0 key:1 data:1
  1 key:2 data:2
  2 key:3 data:3
  3 key:4 data:4
""");

    t.concatenate(s);
    t.P.run(); t.P.clear();

    //stop(t);
    ok(t, """
StuckSML(maxSize:8 size:8)
  0 key:1 data:1
  1 key:2 data:2
  2 key:3 data:3
  3 key:4 data:4
  4 key:5 data:5
  5 key:6 data:6
  6 key:7 data:7
  7 key:8 data:8
""");
   }

  static void test_prepend()
   {z();
    final MemoryLayoutPA m = new MemoryLayoutPA(1000);

    final StuckPA s = new StuckPA("source", m)
     {int maxSize     () {return  8;}
      int bitsPerKey  () {return 16;}
      int bitsPerData () {return 16;}
      int bitsPerSize () {return 16;}
     };

    s.base(0);
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(5); s.T.at(s.tData).setInt(5);}}; s.push();
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(6); s.T.at(s.tData).setInt(6);}}; s.push();
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(7); s.T.at(s.tData).setInt(7);}}; s.push();
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(8); s.T.at(s.tData).setInt(8);}}; s.push();
    s.P.run(); s.P.clear();

    //stop(s);
    ok(s, """
StuckSML(maxSize:8 size:4)
  0 key:5 data:5
  1 key:6 data:6
  2 key:7 data:7
  3 key:8 data:8
""");

    final StuckPA t = new StuckPA("target", m)
     {int maxSize     () {return s.maxSize     ();}
      int bitsPerKey  () {return s.bitsPerKey  ();}
      int bitsPerData () {return s.bitsPerData ();}
      int bitsPerSize () {return s.bitsPerSize ();}
     };

    t.base(s.M.layout.size());
    t.P.new I() {void a() {t.T.at(t.tKey).setInt(1); t.T.at(t.tData).setInt(1);}}; t.push();
    t.P.new I() {void a() {t.T.at(t.tKey).setInt(2); t.T.at(t.tData).setInt(2);}}; t.push();
    t.P.new I() {void a() {t.T.at(t.tKey).setInt(3); t.T.at(t.tData).setInt(3);}}; t.push();
    t.P.new I() {void a() {t.T.at(t.tKey).setInt(4); t.T.at(t.tData).setInt(4);}}; t.push();
    t.P.run(); t.P.clear();

    //stop(t);
    ok(t, """
StuckSML(maxSize:8 size:4)
  0 key:1 data:1
  1 key:2 data:2
  2 key:3 data:3
  3 key:4 data:4
""");

    t.prepend(s);
    t.P.run(); t.P.clear();

    //stop(s.M);
    //stop(t);
    ok(t, """
StuckSML(maxSize:8 size:8)
  0 key:5 data:5
  1 key:6 data:6
  2 key:7 data:7
  3 key:8 data:8
  4 key:1 data:1
  5 key:2 data:2
  6 key:3 data:3
  7 key:4 data:4
""");
   }

  static void test_split()
   {z();
    final MemoryLayoutPA m = new MemoryLayoutPA(1000);

    final StuckPA s = new StuckPA("source", m)
     {int maxSize     () {return  5;}
      int bitsPerKey  () {return 16;}
      int bitsPerData () {return 16;}
      int bitsPerSize () {return 16;}
     };

    s.base(0);
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(1); s.T.at(s.tData).setInt( 2);}}; s.push();
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(2); s.T.at(s.tData).setInt( 4);}}; s.push();
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(3); s.T.at(s.tData).setInt( 6);}}; s.push();
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(4); s.T.at(s.tData).setInt( 8);}}; s.push();
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(5); s.T.at(s.tData).setInt(10);}}; s.push();
    s.P.run(); s.P.clear();

    //stop(s);
    ok(s, """
StuckSML(maxSize:5 size:5)
  0 key:1 data:2
  1 key:2 data:4
  2 key:3 data:6
  3 key:4 data:8
  4 key:5 data:10
""");

    final StuckPA l = s.copyDef(), h = s.copyDef();
    l.base(s.M.size()*1);
    h.base(s.M.size()*2);
    s.split(l, h);
    s.P.run(); s.P.clear();

    //stop(s);
    ok(s, """
StuckSML(maxSize:5 size:5)
  0 key:1 data:2
  1 key:2 data:4
  2 key:3 data:6
  3 key:4 data:8
  4 key:5 data:10
""");

    //stop(l);
    ok(l, """
StuckSML(maxSize:5 size:2)
  0 key:1 data:2
  1 key:2 data:4
""");

    //stop(h);
    ok(h, """
StuckSML(maxSize:5 size:2)
  0 key:4 data:8
  1 key:5 data:10
""");
   }

  static void test_split_low_2()
   {z();
    final MemoryLayoutPA m = new MemoryLayoutPA(1000);

    final StuckPA s = new StuckPA("source", m)
     {int maxSize     () {return  2;}
      int bitsPerKey  () {return 16;}
      int bitsPerData () {return 16;}
      int bitsPerSize () {return 16;}
     };

    s.base(0);
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(1); s.T.at(s.tData).setInt( 2);}}; s.push();
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(2); s.T.at(s.tData).setInt( 4);}}; s.push();
    s.P.run(); s.P.clear();

    //stop(s);
    ok(s, """
StuckSML(maxSize:2 size:2)
  0 key:1 data:2
  1 key:2 data:4
""");

    final StuckPA l = s.copyDef();
    l.base(s.M.size()*1);
    s.splitLow(l);
    s.P.run(); s.P.clear();

    //stop(l);
    ok(l, """
StuckSML(maxSize:2 size:1)
  0 key:1 data:2
""");

    //stop(s);
    ok(s, """
StuckSML(maxSize:2 size:1)
  0 key:2 data:4
""");
   }

  static void test_split_low_even()
   {z();
    final MemoryLayoutPA m = new MemoryLayoutPA(1000);

    final StuckPA s = new StuckPA("source", m)
     {int maxSize     () {return  4;}
      int bitsPerKey  () {return 16;}
      int bitsPerData () {return 16;}
      int bitsPerSize () {return 16;}
     };

    s.base(0);
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(1); s.T.at(s.tData).setInt( 2);}}; s.push();
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(2); s.T.at(s.tData).setInt( 4);}}; s.push();
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(3); s.T.at(s.tData).setInt( 6);}}; s.push();
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(4); s.T.at(s.tData).setInt( 8);}}; s.push();
    s.P.run(); s.P.clear();

    //stop(s);
    ok(s, """
StuckSML(maxSize:4 size:4)
  0 key:1 data:2
  1 key:2 data:4
  2 key:3 data:6
  3 key:4 data:8
""");

    final StuckPA l = s.copyDef();
    l.base(s.M.size()*1);
    s.splitLow(l);
    s.P.run(); s.P.clear();

    //stop(s);
    ok(s, """
StuckSML(maxSize:4 size:2)
  0 key:3 data:6
  1 key:4 data:8
""");

    //stop(l);
    ok(l, """
StuckSML(maxSize:4 size:2)
  0 key:1 data:2
  1 key:2 data:4
""");
   }

  static void test_split_low_odd()
   {z();
    final MemoryLayoutPA m = new MemoryLayoutPA(1000);

    final StuckPA s = new StuckPA("source", m)
     {int maxSize     () {return  5;}
      int bitsPerKey  () {return 16;}
      int bitsPerData () {return 16;}
      int bitsPerSize () {return 16;}
     };

    s.base(0);
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(1); s.T.at(s.tData).setInt( 2);}}; s.push();
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(2); s.T.at(s.tData).setInt( 4);}}; s.push();
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(3); s.T.at(s.tData).setInt( 6);}}; s.push();
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(4); s.T.at(s.tData).setInt( 8);}}; s.push();
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(5); s.T.at(s.tData).setInt(10);}}; s.push();
    s.P.run(); s.P.clear();

    //stop(s);
    ok(s, """
StuckSML(maxSize:5 size:5)
  0 key:1 data:2
  1 key:2 data:4
  2 key:3 data:6
  3 key:4 data:8
  4 key:5 data:10
""");

    final StuckPA l = s.copyDef();
    l.base(s.M.size()*1);
    s.splitLow(l);
    s.P.run(); s.P.clear();

    //stop(s);
    ok(s, """
StuckSML(maxSize:5 size:2)
  0 key:4 data:8
  1 key:5 data:10
""");

    //stop(l);
    ok(l, """
StuckSML(maxSize:5 size:3)
  0 key:1 data:2
  1 key:2 data:4
  2 key:3 data:6
""");
   }

  static void test_split_high()
   {z();
    final MemoryLayoutPA m = new MemoryLayoutPA(1000);

    final StuckPA s = new StuckPA("source", m)
     {int maxSize     () {return  5;}
      int bitsPerKey  () {return 16;}
      int bitsPerData () {return 16;}
      int bitsPerSize () {return 16;}
     };

    s.base(0);
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(1); s.T.at(s.tData).setInt( 2);}}; s.push();
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(2); s.T.at(s.tData).setInt( 4);}}; s.push();
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(3); s.T.at(s.tData).setInt( 6);}}; s.push();
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(4); s.T.at(s.tData).setInt( 8);}}; s.push();
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(5); s.T.at(s.tData).setInt(10);}}; s.push();
    s.P.run(); s.P.clear();

    //stop(s);
    ok(s, """
StuckSML(maxSize:5 size:5)
  0 key:1 data:2
  1 key:2 data:4
  2 key:3 data:6
  3 key:4 data:8
  4 key:5 data:10
""");

    final StuckPA h = s.copyDef();
    h.base(s.M.size()*1);
    s.splitHigh(h);
    s.P.run(); s.P.clear();

    //stop(s);
    ok(s, """
StuckSML(maxSize:5 size:3)
  0 key:1 data:2
  1 key:2 data:4
  2 key:3 data:6
""");

    //stop(h);
    ok(h, """
StuckSML(maxSize:5 size:2)
  0 key:4 data:8
  1 key:5 data:10
""");
   }

  static void test_zero_last_key()
   {z();
    final MemoryLayoutPA m = new MemoryLayoutPA(1000);

    final StuckPA s = new StuckPA("source", m)
     {int maxSize     () {return  4;}
      int bitsPerKey  () {return 16;}
      int bitsPerData () {return 16;}
      int bitsPerSize () {return 16;}
     };

    s.base(0);
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(1); s.T.at(s.tData).setInt( 2);}}; s.push();
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(2); s.T.at(s.tData).setInt( 4);}}; s.push();
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(3); s.T.at(s.tData).setInt( 6);}}; s.push();
    s.zeroLastKey();
    s.P.run(); s.P.clear();

    //stop(s);
    ok(s, """
StuckSML(maxSize:4 size:3)
  0 key:1 data:2
  1 key:2 data:4
  2 key:0 data:6
""");

    ok(s.T.at(s.tKey).getInt(), 3);                                             // The value of the key before it was zeroed

    s.P.new I() {void a() {s.T.at(s.tKey).setInt(4); s.T.at(s.tData).setInt( 8);}}; s.push();
    s.zeroLastKey();
    s.P.run(); s.P.clear();

    //stop(s);
    ok(s, """
StuckSML(maxSize:4 size:4)
  0 key:1 data:2
  1 key:2 data:4
  2 key:0 data:6
  3 key:0 data:8
""");
    ok(s.T.at(s.tKey).getInt(), 4);
   }

  static void test_set_last_key()
   {z();
    final MemoryLayoutPA m = new MemoryLayoutPA(1000);

    final StuckPA s = new StuckPA("source", m)
     {int maxSize     () {return  4;}
      int bitsPerKey  () {return 16;}
      int bitsPerData () {return 16;}
      int bitsPerSize () {return 16;}
     };

    s.base(0);
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(1); s.T.at(s.tData).setInt( 2);}}; s.push();
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(2); s.T.at(s.tData).setInt( 4);}}; s.push();
    s.P.new I() {void a() {s.T.at(s.tKey).setInt(3); s.T.at(s.tData).setInt( 6);}}; s.push();
    s.T.setIntInstruction(s.tKey, 4);
    s.setLastKey();
    s.P.run(); s.P.clear();

    //stop(s);
    ok(s, """
StuckSML(maxSize:4 size:3)
  0 key:1 data:2
  1 key:2 data:4
  2 key:4 data:6
""");

    s.P.new I() {void a() {s.T.at(s.tKey).setInt(5); s.T.at(s.tData).setInt( 8);}}; s.push();
    s.T.setIntInstruction(s.tKey, 6);
    s.setLastKey();
    s.P.run(); s.P.clear();

    //stop(s);
    ok(s, """
StuckSML(maxSize:4 size:4)
  0 key:1 data:2
  1 key:2 data:4
  2 key:4 data:6
  3 key:6 data:8
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
    test_search2();
//  test_search_except_last();
    test_search_first_greater_than_or_equal();
    test_search_first_greater_than_or_equal_except_last();
    test_at();
    test_copy();
    test_copy_keys_and_data();
    test_concatenate();
    test_prepend();
    test_split();
    test_split_low_2();
    test_split_low_even();
    test_split_low_odd();
    test_split_high();
    test_zero_last_key();
    test_set_last_key();
   }

  static void newTests()                                                        // Tests being worked on
   {oldTests();
    test_search_first_greater_than_or_equal_except_last();
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
