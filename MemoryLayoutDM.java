//------------------------------------------------------------------------------
// Memory layout in Pseudo Assembler
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Memory layout

import java.util.*;

class MemoryLayoutDM extends Test implements Comparable<MemoryLayoutDM>         // Memory layout
 {final String       name;                                                      // Name of the memory layout
  final Layout     layout;                                                      // Layout of part of memory
  private Memory   memory;                                                      // Memory containing layout
  boolean           debug;                                                      // Debug if true
  ProgramDM             P = new ProgramDM();                                    // Program containing generated code
  static int      numbers = 0;                                                  // Unique number for each memory layout recreated
  final  int       number = ++numbers;                                          // Number each memory layout
  String       uniqueName;                                                      // A unique name for this layout within its executing program

  final BlockArray block;                                                       // The memory layout represents an array the elements of which have a width equal to a power of two

//D1 Construction                                                               // Construct a memory layout

  MemoryLayoutDM(Layout Layout, String Name)                                    // Memory with an associated layout and a name so we can generate verilog from it
   {zz();
    name       = Name; layout = Layout;
    memory     = new Memory(Name, layout.size());                               // Create the associated memory
    block      = new BlockArray();
   }

  public int compareTo(MemoryLayoutDM other)                                    // A progam might access several memory layouts
   {return Integer.compare(number, other.number);
   }

  At top() {zz(); return at(layout.top());}                                     // A reference to the top element of a memory layout.  Useful for moving the entire memory area.

  class BlockArray                                                              // Memory that represents an array of elements whose widths are equal to the power of two and so the memory can be efficiently processed in blocks
   {final boolean      array;                                                   // The memory layout represents an array
    final boolean powerOfTwo;                                                   // The elements of the array are a power of two in size
    final int          width;                                                   // The width of the elements of the array
    final int           size;                                                   // Number of elements in the array
    final int           log;                                                    // The log to base two of the width of the array elements
    boolean         blocked() {return powerOfTwo;}                              // A blocked array
    BlockArray()
     {zz();
      array      = layout.top() instanceof Layout.Array;
      width      = array ? layout.top().toArray().element.width : 0;
      size       = array ? layout.top().toArray().size : 1;
      powerOfTwo = nextPowerOfTwo(width) == width;
      log        = logTwo(width);
     }
   }

//D1 Control                                                                    // Testing, control and integrity

  Memory memory() {return memory;}                                              // Real memory used by this layout - executed very frequently

  void program(ProgramDM program, boolean uniqueName)                           // Program in which to generate instructions. If the name is unique it will be used directly in verilog, if not unique, then a unique making number will be added to the end
   {zz(); P = program; program.addMemoryLayout(this, uniqueName);
    P.setUniqueNames();
   }

  void program(ProgramDM program) {zz(); program(program, true);}               // Add this memory layout to a program with the intention of using its uqnique name to identify it in verilog

  void setUniqueName()                                                          // Set a unique name for this memory layout for use when tracingmemory during program execution in Java and verilog to confirm that memory is being modified identically in the two representations.
   {zz();
    if (uniqueName == null)                                                     // Name has not already been set
     {uniqueName = P != null && P.uniqueNames.contains(name) ? name             // Name is already unique
                                                             : name+"_"+number; // Not sure if the name is unique so have to add a number to make it unique
     }
   }

  String name()                                                                 // Retrieve the unique name for this memory layout within its containing program
   {if (uniqueName == null)
     {stop("No unique name for memory layout with name:", name,
           "and number:", number);
      return name+"_"+number;
     }
    return uniqueName;
   }

  int size  () {zz(); return memory.size();}                                    // Size of associated memory
  void clear() {zz(); memory.zero();}                                           // Clear underlying memory

//D1 Get and Set                                                                // Get and set values in memory but only during testing

  boolean getBit(int index)                                                     // Get a bit from the memory layout
   {return memory.getBit(index);
   }

  void setBit(int index, boolean value)                                         // Set a value in memory occupied by the layout
   {memory.set(index, value);
   }

  int  getInt(Layout.Field field, int...indices)                                // Get a value from memory occupied by the layout
   {zz(); final int i = new At(field, indices).setOff().result;
    return i;
   }

  void setInt(Layout.Field field, int value, int...indices)                     // Set a value in memory occupied by the layout
   {zz(); final At a = new At(field, indices).setOff();
    memory.set(a.at, a.width, value);
   }

  void setIntInstruction(Layout.Field field, int value)                         // Set a value in memory occupied by the layout
   {zz();
    P.new I()
     {void a()
       {final At a = new At(field).setOff();
        memory.set(a.at, a.width, value);
       }
      String v()
       {return new At(field).verilogLoad() +
         " <= " + value + ";";
       }
     };
   }

  void zero() {zz(); memory.zero(0, size());}                                   // Clear the memory associated with the layout to zeros
  void ones() {zz(); memory.ones(0, size());}                                   // Set the memory associated with the layout to zeros

  void copy(MemoryLayoutDM source)                                              // Copy as many of the bits from the source into the target as possible
   {zz();
    final int N = size();
    P.new I()
     {void a()
       {for(int i = 0; i < N; ++i) setBit(i, source.getBit(i));
       }
      String v()
       {return name()+"[0 +: "+N+"] <= "+source.name() +
                      "[0 +: "+N+"]; /* copy1 */";
       }
     };
   }

  void copy(final MemoryLayoutDM.At source)                                     // Fill the target from the source starting at the referenced address in the source
   {zz();
    final MemoryLayoutDM m = this;
    final int N = min(size(), source.width);
    P.new I()
     {void a()
       {source.setOff();
        for(int i = 0; i < N; ++i) setBit(i, source.getBit(i));
       }
      String v()
       {return m.name()+"[0 +: "+N+"] <= "+source.ml().name()+
                        "["+source.verilogAddr()+" +: "+N+"]; /* copy2 */";
       }
      String n()
       {return "copy "+N+" bits from "+source+" to "+m.name;
       }
     };
   }

//D1 Components                                                                 // Locate a variable in memory via its indices

  class At
   {final Layout.Field field;                                                   // Field description in layout
    final int  width;                                                           // Width of element in memory
    final int[]indices;                                                         // Known indices to be applied directly to locate the field in memory
    final At[] directs;                                                         // Fields whose location is known at the start so they can be used for indices into memory rather like registers on a chip
    int  at;                                                                    // Location in memory
    int  result;                                                                // The contents of memory at this location

    String verilogLoadAddr(boolean la, Integer delta)                           // A verilog representation of an addressed location in memory
     {zz();
      final Stack<String>      v = new Stack<>();                               // Verilog expression, index variable names
      Layout.Locator           L = field.locator;
      final Stack<Layout.Array>A = L.arrays;                                    // The containing arrays

      for (int i = 0; i < A.size(); ++i)                                        // Each array to index to reach this field
       {final Layout.Array a = A.elementAt(i).toArray();                        // Each array to index to reach this field
        final int          w = a.element.width;                                 // Width of containing array element
        final String       o = !hasIndirection() ? ""+indices[i] :              // Numeric index
                               directs[i].verilogLoad();                        // Indirect index loaded from memory
        v.push(" + " + o + " * " + w);                                          // Access indexing field
       }

      final int    W =  field.width;                                            // The width of the field
      final String w = w(W);                                                    // The width of the field as a padded string
      final String d = delta == null ? "" :                                     // Constant delta to modify address if needed in steps of field size as in the C programming language
                       delta > 0 ? "+"+delta*W : ""+delta*W;

      return (la ? i(field.at)+c()+joinStrings(v, "")+d    :                    // IBM S/360 Principles of Operation: LA
        name()+"["+i(field.at)+c()+joinStrings(v, "")+d+" +: "+w+"]");          // IBM S/360 Principles of Operation: L
     }

    String verilogLoad() {zz(); return verilogLoadAddr(false, null);}           // Content of a memory location as a verilog expression
    String verilogAddr() {zz(); return verilogLoadAddr(true,  null);}           // Address of a memory location
    String i(int i)      {zz(); return String.format("%8d", i);}                // Format an index
    String w(int w)      {zz(); return String.format("%1d", w);}                // Format a width

    String c()                                                                  // Format a field name as a comment
     {final StringBuilder s = new StringBuilder(field.name);
      while(s.length() % 8 > 0) s.append(" ");
      return "/*" + s +"*/";
     }
    String p(String s)   {while(s.length() % 8 > 0) s = s+" "; return s;}       // Pad a string


    At locateDirectAddress()                                                    // Locate a direct address and its content
     {zz();
      final int N = indices.length;
      at          = field.locator.at(indices);
      result      = memory.getInt(at, width);
      return this;
     }

    At locateInDirectAddress()                                                  // Locate an indirect address and its content
     {zz();
      final int N = directs.length;
      for (int i = 0; i < N; i++)
       {indices[i] = directs[i].setOff().getInt();                              // It is assumed by getInt() that setOff() will already have been called.
       }
      return locateDirectAddress();                                             // Locate the address directly now that its indices are known
     }

    At(Layout.Field Field)                                                      // No indices
     {zz();
      field = Field; indices = new int[0];
      width = field.width;
      if (width < 1) stop("Field", field.name, "does not have any bits");
      directs = null;
      locateDirectAddress();                                                    // The indices are constant so the address will not change over time
     }

    At(Layout.Field Field, int...Indices)                                       // Constant indices used for setting initial values
     {zz();
      field = Field; indices = Indices; width = field.width;
      directs = null;
      locateDirectAddress();                                                    // The indices are constant so the address will not change over time
     }

    At(Layout.Field Field, At...Directs)                                        // Variable indices used for obtaining run time values
     {zz();
      final int N = Directs.length, E = layout.new Locator(Field).arrays.size();// Number of indices supplied, number expected
      if (N != E) stop("Got", N, "indices but expected", E);
      for (int i = 0; i < N; i++)
       {if (Directs[i].directs != null)
         {stop("Index:", i, "must not have indices");
         }
       }

      field = Field; width = field.width;
      directs = Directs;
      indices = new int[N];                                                     // The values obtained from each indirect reference to an index will be placed here for the computation of the actual address of the indexed field
     }

    boolean sameSize(At b)                                                      // Check two fields are the same size
     {zz(); if (field == null) return true;                                     // Constants match any size
      z(); field.sameSize(b.field);
      z(); return true;
     }

    boolean hasIndirection() {z(); return directs != null;}                     // Is indirection used in this at reference ?
    int width()              {z(); return field.width();}                       // Width of the field in memory
    At setOff()              {z(); return setOff(true);}                        // Set the base address of the field from its indices confirming that we are inside an executing instruction

    At setOff(boolean checkSetOff)                                              // Set the base address of the field
     {zz();
      if (checkSetOff && !P.running)                                            // Check this method is being used inside an instruction
       {P.halt("Set off must be inside an instruction");
       }
      if (hasIndirection()) {z(); locateInDirectAddress();}                     // Evaluate indirect indices
      else                  {zz(); locateDirectAddress();}                      // Evaluate direct indices
      return this;
     }

    boolean getBit(int i) {zz(); return memory.getBit(at+i);}                   // Get a bit from memory assuming that setOff() has been called to fix the location of the field containing the bit
    void    setBit(int i, boolean b) {zz(); memory.set(at+i, b);}               // Set a bit in memory  assuming that setOff() has been called to fix the location of the field containing the bit

    int  getInt()          {zz(); return result;}                               // The value in memory, at the indicated location, treated as an integer or the value of the constant, assumming setOff has been called to update the variable description
    void setInt(int value) {zz(); memory.set(at, width, value);}                // Set the value in memory at the indicated location, treated as an integer

    public String toString()                                                    // Print field name(indices)=value or name=value if there are no indices
     {final StringBuilder s = new StringBuilder();
      s.append(ml().name+"."+field.name);
      if (indices.length > 0)
       {//setOff(false);
        s.append("[");
        for (int i = 0, N = indices.length; i < N; i++)
         {s.append(indices[i]);
          if (i < N-1) s.append(",");
         }
        s.append("]"+at+"="+result);
       }
      else s.append("@"+at+"="+result);
      return s.toString();
     }

    MemoryLayoutDM ml() {zz(); return MemoryLayoutDM.this;}                     // Containing memory layout

//D2 Move                                                                       // Copy data between memory locations

    void moveBits(At source)                                                    // The interior of a move on Java
     {zz();
      final At target = this;
      source.setOff();
      target.setOff();
      for(int i = 0; i < width; ++i)
       {final boolean b = source.getBit(i);
        target.setBit(i, b);
       }
     }

    void move(At source)                                                        // Copy the specified number of bits from source to target assuming no overlap. The source and target can be in the same or a different memory.
     {zz(); sameSize(source);
      final At target = this;
      if (target.ml()  == source.ml()  &&
          target.field == source.field &&
          target.indices.length == 0 && source.indices.length == 0)             // No need to copy an unindexed field into itself
       {//err("No need to move", source.field.name, target.field.name);
        return;
       }
      P.new I()
       {void a() {moveBits(source);}
        String v()
         {return target.verilogLoad()+" <= "+source.verilogLoad() + "/* move */;";
         }
        String n() {return field.name+"="+source.field.name;}
        void   i()  {}
       };
     }

    void moveTo(At...Targets)                                                   // Move the data to the named fields
     {z();
      final int N = Targets.length;
      final At source = this;
      for(int i = 0; i < N; ++i) source.sameSize(Targets[i]);
      P.new I()
       {void a()
         {source.setOff();
          for(int i = 0; i < N; ++i)
           {final At target = Targets[i];
            target.setOff();
            for(int j = 0; j < width; ++j)
             {z();
              final boolean b = source.getBit(j);
              target.setBit(j, b);
             }
           }
         }
        String v()
         {final StringBuilder s = new StringBuilder();
          for(int i = 0; i < N; ++i)
           {return Targets[i].verilogLoad()+" <= "+source.verilogLoad() + "/* moveTo */;";
           }
          return s.toString();
         }
        String n() {return field.name+"="+source.field.name;}
       };
     }

    private At createField(String Name, int Width)                              // Create a field of the specified width
     {zz();
      final Layout           l = Layout.layout();                               // Layout some memory
      final Layout.Variable  v = l.variable(Name, Width);                       // Create variable definition
      MemoryLayoutDM         m = new MemoryLayoutDM(l.compile(), Name);         // Create memory for variable
      m.program(ml().P, false);                                                 // Add memory to program
      return m.at(v);                                                           // Return variable
     }

    private At createMoveBuffer()                                               // Create a buffer for moving elements in an array
     {zz(); if (!(field instanceof Layout.Array)) stop("Array required");       // Need an array to amke a buffer for
      final Layout.Array     A  = field.toArray();                              // Array of elements to be moved
      final Layout.Field     a  = A.element;                                    // Array element
      final Layout           b = Layout.layout();                               // A field of bits the same size as the array
      final Layout.Variable  B = b.variable ("b", a.width * A.size);            // A buffer the same size as the array
      MemoryLayoutDM         m = new MemoryLayoutDM(b.compile(), "buffer");     // Create a matching array buffer
      m.program(P, false);                                                      // Add memory to program
      return m.at(B);                                                           // Return buffer
     }

    private void copyMoveBuffer(At Source)                                      // Copy a move buffer directly to the target bit by bit without regard for the structure of the source of target.  This allows such moves to be performed in verilog as a single statement.
     {zz(); sameSize(Source);                                                   // Check source anmd target gave the same size
      final int N = width;                                                      // Width of target in bits
      final Memory s = Source.ml().memory(), t = ml().memory();                 // Memory for source and target
      P.new I()
       {void a()                                                                // Emulation
        {final int  S = Source.setOff().at, T = setOff().at;                    // Start of source and target in memory
         for   (int i = 0; i < N; i++)                                          // Each bit to be moved
           {final boolean b = s.getBit(S+i);                                    // A bit to be moved
            t.set(T+i, b);                                                      // Copy the bit into the target
           }
         }
        String v()                                                              // Verilog
         {return        name()+"["+       verilogAddr()+"+:"+N+"] <= "+         // Target
            Source.ml().name()+"["+Source.verilogAddr()+"+:"+N+"];"+            // Source
            "/* copyMoveBuffer */";
         }
       };
     }


    private void   upMoveBuffer(int Width) {zz();   upMoveBuffer(0, Width);}    // Move all the bits in a buffer up   by the specified width
    private void downMoveBuffer(int Width) {zz(); downMoveBuffer(0, Width);}    // Move all the bits in a buffer down by the specified width

    private void upMoveBuffer(int Start, int Width)                             // Move the bits in a buffer up by the specified width starting at the indexed location specified in multiples of the width.
     {zz();
      final int S = Start * Width;                                              // Start position
      final int N = width - S - Width;                                          // Number of bits to move
      final MemoryLayoutDM l = ml();                                            // Memory layout containing buffer
      final Memory         m = l.memory();                                      // Memory for buffer
      P.new I()
       {void a()                                                                // Emulation
         {for  (int i = N; i > 0; i--)                                          // Each bit to be moved
           {final boolean b = getBit(S+i-1);                                    // A bit to be moved
            setBit(S+Width+i-1, b);                                             // Copy the bit into the target
           }
         }
        String v()                                                              // Verilog
         {final String n = l.name();                                            // Memory name in which the move will be performed
          return n+"["+S+"+"+Width+"+:"+N+"] <= "+n+"[0+:"+N+"];"+              // Parallel move
            "/* upMoveBufferLinear */";
         }
       };
     }

    private void downMoveBuffer(int Start, int Width)                           // Move the bits in a buffer down by the specified width starting at the indexed location specified in multiples of the width.
     {zz();
      final int S = Start * Width;                                              // Start position
      final int N = width - S - Width;                                          // Number of bits to move
      final MemoryLayoutDM l = ml();                                            // Memory layout containing buffer
      final Memory         m = l.memory();                                      // Memory for buffer
      P.new I()
       {void a()                                                                // Emulation
         {for  (int i = 0; i < N; i++)                                          // Each bit to be moved
           {final boolean b = getBit(S+Width+i);                                // A bit to be moved
            setBit(S+i, b);                                                     // Copy the bit into the target
           }
         }
        String v()                                                              // Verilog
         {final String n = l.name();                                            // Memory name in which the move will be performed
          return n+"["+S+"+:"+N+"] <= "+n+"["+Width+"+:"+N+"];"+                 // Parallel move
            "/* downMoveBufferLinear */";
         }
       };
     }

    private void upMoveBuffer(At Start, int Width, int Length)                  // Move the bits in a buffer up by the specified width starting at the indexed location specified in multiples of the width.  The length of the move is specified in multiples of the width.
     {z();
      final MemoryLayoutDM l = ml();                                            // Memory layout containing buffer
      final Memory         m = l.memory();                                      // Memory for buffer
      P.new I()
       {void a()                                                                // Emulation
         {final int S = Start.setOff().result * Width;                          // Start position
          final int N = Length * Width;                                         // Number of bits to move
          for  (int i = N; i > 0; i--)                                          // Each bit to be moved
           {final boolean b = getBit(S+i-1);                                    // A bit to be moved
            setBit(S+Width+i-1, b);                                             // Copy the bit into the target
           }
         }
        String v()                                                              // Verilog
         {final String S = Start.verilogLoad();                                 // Start position
          final String N = ""+(Length * Width);                                 // Number of bits to move
          final String n = l.name();                                            // Memory name in which the move will be performed
          return "begin " +
          n+"["+S+"+"+Width+"+:"+N+"] <= "+n+"["+S+"+:"+N+"]; "+                // Parallel move
          "end /* upMoveBufferBlock */";
         }
       };
     }

    private void downMoveBuffer(At Start, int Width, int Length)                // Move the bits in a buffer down by the specified width starting at the indexed location specified in multiples of the width.  The length of the move is specified in multiples of the width.
     {z();
      final MemoryLayoutDM l = ml();                                            // Memory layout containing buffer
      final Memory         m = l.memory();                                      // Memory for buffer
      P.new I()
       {void a()                                                                // Emulation
         {final int S = Start.setOff().result * Width;                          // Start position
          final int N = Length * Width;                                         // Number of bits to move
          for  (int i = 0; i < N; i++)                                          // Each bit to be moved
           {final boolean b = getBit(S+Width+i-1);                              // A bit to be moved
            setBit(S+i-1, b);                                                   // Copy the bit into the target
           }
         }
        String v()                                                              // Verilog
         {final String S = Start.verilogLoad();                                 // Start position
          final String N = ""+(Length * Width);                                 // Number of bits to move
          final String n = l.name();                                            // Memory name in which the move will be performed
          return "begin " +
          n+"["+S+"+:"+N+"] <= "+n+"["+S+"+"+Width+"+:"+N+"]; "+                // Parallel move
          "end /* downMoveBufferBlock */";
         }
       };
     }

    void moveUp()                                                               // Move the elements of an array up one position deleting the last element.
     {zz();
      if (!(field instanceof Layout.Array))  stop("Array required for moveUp");
      final Layout.Array A = field.toArray();                                   // Array of elements to be moved
      final At           B = createMoveBuffer();                                // Buffer containg a copy of the array to be moved

      B.copyMoveBuffer(this);
      B.upMoveBuffer(A.element.width);
        copyMoveBuffer(B);
     }

    void moveDown()                                                             // Move the elements of an array down one position deleting the first element.
     {zz();
      if (!(field instanceof Layout.Array)) stop("Array required for moveDown");
      final Layout.Array A = field.toArray();                                   // Array of elements to be moved
      final At           B = createMoveBuffer();                                // Buffer containg a copy of the array to be moved

      B.copyMoveBuffer(this);
      B.downMoveBuffer(A.element.width);
        copyMoveBuffer(B);
     }

    void moveUp(At Index)                                                       // Move the elements of an array up one position deleting the last element.  A buffer of the same size is used to permit copy in parallel.  Whether each element is copied is dependent on a binary "less than" which is expensive
     {zz();
      if (!(field instanceof Layout.Array)) stop("Array required for moveUp");
      final Layout.Array A = field.toArray();                                   // Array of elements to be moved
      final int          S = logTwo(A.size)-1;                                  // Next log 2 of size of array. Minus one because that is the size of the maximum move
      final int      width = A.element.width;                                   // Width of each array element
      final At           B = createMoveBuffer();                                // Buffer containing a copy of the array to be moved
      final At           p = createField("position", Index.width);              // Current position in buffer

      B.copyMoveBuffer(this);                                                   // Make a copy of the array to work on as this will hopefully reduce congestion
      p.ml().setIntInstruction(p.field, A.size);                                // Position in the buffer in units of array element size

      for (int i = 0; i <= S; i++)                                              // Move each logarithmically sized block
       {final int q = 1<<(S-i);                                                 // Size of block
        P.new I()
         {void a()                                                              // Emulation
           {final int o = p.ml().getInt(p.field) - q;                           // Start of block to move to
            if (o > Index.setOff().result)                                      // Move block is in range
             {final int S = o * width;                                          // Start position
              final int N = q * width;                                          // Number of bits to move
              for  (int i = N; i > 0; --i)                                      // Each bit to be moved
               {final boolean b = B.getBit(S-width+i-1);                        // A bit to be moved
                B.setBit(S+i-1, b);                                             // Copy the bit into the target
               }
              p.ml().setInt(p.field, o);                                        // Address remainder of area to be moved
             }
           }
          String v()                                                            // Verilog
           {final String S = p.verilogLoad();                                   // Start position
            final String N = i8(q * width);                                     // Number of bits to move
            final String n = B.ml().name();                                     // Memory name in which the move will be performed
            final String Q = i4(q);

            return "if ("+S+" > "+Index.verilogLoad()+"+"+Q+") begin " +        // Block is in range
              n+"[("+S+" - "+Q+")"+"*"+width+          " +: "+N+"] <= "+
              n+"[("+S+" - "+Q+")"+"*"+width+"-"+width+" +: "+N+"]; "+          // Parallel move
              p.verilogLoad()+" <= "+p.verilogLoad()+"-"+Q+";"+                 // Update position
             "end /* moveUp */";
           }
         };
       }
      copyMoveBuffer(B);                                                        // Rewrite the array after performing the move in the buffer
     }

    void moveDown(At Index)                                                     // Move the elements of an array down one position deleting the last element.  A buffer of the same size is used to permit copy in parallel.  Whether each element is copied is dependent on a binary "less than" which is expensive
     {zz();
      if (!(field instanceof Layout.Array)) stop("Array required for moveUp");
      final Layout.Array A = field.toArray();                                   // Array of elements to be moved
      final int          S = logTwo(A.size)-1;                                  // Next log 2 of size of array. Minus one because that is the size of the maximum move
      final int      width = A.element.width;                                   // Width of each array element
      final At           B = createMoveBuffer();                                // Buffer containing a copy of the array to be moved
      final At           p = createField("position", Index.width);              // Current position in buffer

      B.copyMoveBuffer(this);                                                   // Make a copy of the array to work on as this will hopefully reduce congestion
      p.move(Index);                                                            // Position in the buffer in units of array element size

      for (int i = 0; i <= S; i++)                                              // Move each logarithmically sized block
       {final int q = 1<<(S-i);                                                 // Size of block
        P.new I()
         {void a()                                                              // Emulation
           {final int o = p.ml().getInt(p.field);                               // Start of block to move to
            if (o + q < A.size)                                                 // Move block is in range
             {final int S = o * width;                                          // Start position
              final int N = q * width;                                          // Number of bits to move
              for  (int i = 0; i < N; i++)                                      // Each bit to be moved
               {final boolean b = B.getBit(S+width+i);                          // A bit to be moved
                B.setBit(S+i, b);                                               // Copy the bit into the target
               }
              p.ml().setInt(p.field, o + q);                                    // Address remainder of area to be moved
             }
           }
          String v()                                                            // Verilog
           {final String S = p.verilogLoad();                                   // Start position
            final String N = i8(q * width);                                     // Number of bits to move
            final String n = B.ml().name();                                     // Memory name in which the move will be performed
            final String Q = i8(q);                                             // Move width
            return "if ("+S+"+"+Q+" < "+A.size+") begin " +
              n+"["+S+"*"+width          +" +: "+N+"] <= "+
              n+"["+S+"*"+width+"+"+width+" +: "+N+"]; "+                               // Parallel move
              p.verilogLoad()+" <= "+p.verilogLoad()+"+"+Q+";"+
              "end /* moveDown */";
           }
         };
       }
      copyMoveBuffer(B);                                                        // Rewrite the array after performing the move in the buffer
     }

    void copy(At Source, At Length, At TargetA, At SourceA, At LengthL)         // Copy the specified number of bits from the location addressed by the source to the location addressed by the target using the specified memory locations to hold the source and target locations and the remaining length
     {zz();
      final At  Target = this;
      final int size   = Source.ml().size();
      P.new I()                                                                 // Initialize the indexes and length to describe the copy
       {void a()
         {Target.setOff();                                                      // Target of move
          Source.setOff();                                                      // Source of move
          Length.setOff();                                                      // Length of move
          SourceA.setInt(Source.at);                                            // A variable that indexes the current location in the source
          TargetA.setInt(Target.at);                                            // A variable that indexes the current location in the target
          LengthL.setInt(Length.getInt());                                      // The remaining amount of data to be moved
         }
        String v()                                                              // Logarithmic move
         {final StringBuilder v = new StringBuilder("/* copy start */\n");
          final String l = Length.verilogLoad(), L = LengthL.verilogLoad(),
                       s = Source.verilogAddr(), S = SourceA.verilogLoad(),
                       t = Target.verilogAddr(), T = TargetA.verilogLoad();

          v.append(L + " <= " + l + "; /* copy11 */\n");                        // Initialize at the start of the move
          v.append(S + " <= " + s + "; /* copy12 */\n");
          v.append(T + " <= " + t + "; /* copy13 */\n");
          return v.toString();
         }
       };

      for(int i = prevPowerOfTwo(size); i > 0; i = i >> 1)                      // Copy in logarithmically descending blocks
       {final int I = i;                                                        // Length of move
        P.new I()
         {void a()
           {final int
              l = LengthL.setOff().getInt(),                                    // Remaining length
              s = SourceA.setOff().getInt(),                                    // Current position in source memory
              t = TargetA.setOff().getInt();                                    // Current position in target memory
            final Memory sm = Source.ml().memory();                             // Memory containing source
            final Memory tm = Target.ml().memory();                             // Memory containing target
            if (l >= I)
             {for (int j = 0; j < I; ++j)                                       // Move a block of power of two size
               {final boolean b = sm.getBit(s + j);
                tm.set(t+j, b);
               }
              LengthL.setInt(l - I);                                            // Update remaining length
              SourceA.setInt(s + I);                                            // Latest position in source
              TargetA.setInt(t + I);                                            // Latest position in target
             }
           }
          String v()                                                            // Logarithmic move
           {final StringBuilder v = new StringBuilder("/* MemoryLayoutDM.copy "+I+" */\n");
            final String l = LengthL.verilogLoad(),                             // Size of this move
                         s = SourceA.verilogLoad(),                             // Position in source memory
                         t = TargetA.verilogLoad(),                             // position in target memory
                         S = Source.ml().name(),                                // Memory containing source
                         T = Target.ml().name();                                // Memory containing target

            v.append("if (" +l+" >= "+I+") begin\n");
            v.append("   "  +T+"["+t+" +: "  +I+"] <= "+S+"["+s+" +: "+I+"]; /* copy21 */\n");
            v.append("   "  +l+" <= "+l+" - "+I+"; /* copy22 */\n");            // These assigns have to be made immediately else each block has to be executed one after another to drive the length and pointers sequentially.
            v.append("   "  +s+" <= "+s+" + "+I+"; /* copy23 */\n");
            v.append("   "  +t+" <= "+t+" + "+I+"; /* copy24 */\n");
            v.append("end\n");
            return v.toString();
           }
         };
       }
     }

    void copy(MemoryLayoutDM Source)                                            // Copy the source to the location addressed by the target
     {zz();
      final int N = Source.size();
      P.new I()
       {void a()
         {setOff();
          for(int i = 0; i < N; ++i)
           {final boolean b = Source.getBit(i);
            try
             {setBit(i, b);
             }
            catch(Exception e)
             {stop(e, traceBack);
             }
           }
         }
        String v()
         {final int    n = min(size(), N);
          return ml().name()+"["+verilogAddr()+" +: "+n+"] <= "+
               Source.name()+"[0 +: "+n+"]; /* copy3 */";
         }
       };
     }

    void copy(final MemoryLayoutDM.At source)                                   // Fill the target at the referenced address from the source starting at the referenced address
     {zz(); sameSize(source);
      final int N = width;
      P.new I()
       {void a()
         {setOff(); source.setOff();
          for(int i = 0; i < N; ++i) setBit(i, source.getBit(i));
         }
        String v()
         {final int n = min(width, source.width);
           return ml().name() + "["+verilogAddr()+" +: "+n +"] <= "+
           source.ml().name() + "["+source.verilogAddr()+" +: "+n+
             "]; /* copy4 */";
         }
       };
     }

//D2 Bits                                                                       // Bit operations in a memory.

    void zero()                                                                 // Zero some memory
     {zz();
      P.new I()
       {void a()
         {setOff(); memory.zero(at, width);
         }
        String v()
         {return verilogLoad()+" <= 0; /* zero */";
         }
        String n()
         {return field.name+" <= 0";
         }
       };
     }

    void one()                                                                  // Set memory to one
     {zz();
      P.new I()
       {void a()
         {setOff(); setInt(1);
         }
        String v()
         {return verilogLoad()+" <= 1; /* ones */";
         }
        String n()
         {return field.name+" <= 1;";
         }
       };
     }

    void ones()                                                                 // Ones some memory
     {zz();
      final String one = field.verilogOnes();
      P.new I()
       {void a()
         {setOff(); memory.ones(at, width);
         }
        String v()
         {return verilogLoad()+" <= "+one+ "; /* ones */";
         }
        String n()
         {return field.name+" <= "+one;
         }
       };
     }

    void invert(At a)                                                           // Invert the specified bits
     {z();
      P.new I()
       {void a()
         {setOff(); memory.invert(a.result, a.width());
         }
        String v()
         {return verilogLoad()+" <= ~"+verilogLoad()+ ";  /* invert */";
         }
        String n()
         {return field.name+" <= ~"+field.name;
         }
       };
     }

    boolean isAllZero() {z(); setOff(); return memory.isAllZero(at, width);}    // Check that the specified memory is all zeros
    boolean isAllOnes() {z(); setOff(); return memory.isAllOnes(at, width);}    // Check that  the specified memory is all ones

//D1 Boolean                                                                    // Boolean operations on fields held in memories.

    boolean isZero()                                                            // Whether the field is all zero
     {zz();
      for(int i = 0; i < width; ++i)
       {zz(); if (getBit(i)) {z(); return false;}
       }
      z(); return true;
     }

    void    isZero(At result)                                                   // Whether a field is all zeros
     {zz();
      final At target = this;
      P.new I()
       {void a()
         {result.setOff().setInt(setOff().isZero() ? 1 : 0);
         }
        String v()
         {return result.verilogLoad()+" <= "+
                 target.verilogLoad()+" == 0;  /* isZero */";
         }
        String n() {return result.field.name+" <= isZero "+field.name;}
       };
     }

    boolean isOnes()                                                            // Whether the field is all ones
     {z();
      for(int i = 0; i < width; ++i)
       {z(); if (!getBit(i)) {z(); return false;}
       }
      z(); return true;
     }

    void isOnes(At result)                                                      // Whether a field is all ones
     {z();
      final At target = this;
      P.new I()
       {void a()
         {result.setOff().setInt(setOff().isOnes() ? 1 : 0);
         }
        String v()
         {return result.verilogLoad()+" <= "+target.verilogLoad()+
            " == " + target.field.verilogOnes()+ "; /* isOnes */";
         }
        String n() {return result.field.name+" <= isOnes "+field.name;}
       };
     }

    private boolean equal(At b)                                                 // Whether  a == b
     {zz(); sameSize(b);

      for(int i = 0; i < width; ++i)
       {zz(); if (getBit(i) != b.getBit(i)) {z(); return false;}
       }
      z(); return true;
     }

    void equal(At b, At result)                                                 // Whether  a == b
     {zz();
      final At a = this;
      P.new I()
       {void a()
         {result.setOff().setInt(equal(b.setOff()) ? 1 : 0);
         }
        String v()
         {return result.verilogLoad()+" <= "+a.verilogLoad()+
                                    " == " + b.verilogLoad()+ ";  /* equal */";
         }
        String n() {return result.field.name+"="+field.name+" == "+b.field.name;}
       };
     }

    void notEqual(At b, At result)                                              // Whether  a != b
     {z();
      final At a = this;
      P.new I()
       {void a()
         {result.setOff().setInt(!equal(b.setOff()) ? 1 : 0);
         }
        String v()
         {return result.verilogLoad()+
             " <= "+a.verilogLoad()+
             " != "+b.verilogLoad()+ ";  /* notEqual */";
         }
        String n() {return result.field.name+"="+field.name+"!="+b.field.name;}
       };
     }

    private boolean lessThan(At b)                                              // Whether a < b
     {zz(); sameSize(b);
      for(int i = width; i > 0; --i)
       {zz();
        if (!getBit(i-1) &&  b.getBit(i-1)) {z(); return true;}
        if ( getBit(i-1) && !b.getBit(i-1)) {z(); return false;}
       }
       z(); return false;
     }

    void lessThan(At b, At result)                                              // Whether  a < b
     {zz(); sameSize(b);
      final At a = this;
      P.new I()
       {void a()
         {result.setOff().setInt(lessThan(b.setOff()) ? 1 : 0);
         }
        String v()
         {return result.verilogLoad()+
            " <= "+a.verilogLoad()+
            " < "+b.verilogLoad()+ ";  /* lessThan */";
         }
        String n() {return result.field.name+"="+field.name+"<"+b.field.name;}
       };
     }

    void lessThanOrEqual(At b, At result)                                       // Whether  a <= b
     {z(); sameSize(b);
      final At a = this;
      P.new I()
       {void a()
        {result.setOff().setInt(lessThan(b.setOff()) || equal(b) ? 1 : 0);
        }
        String v()
         {return result.verilogLoad()+
            " <= "+a.verilogLoad()+
            " <= "+b.verilogLoad()+ "; /* lessThanOrEqual */";
         }
        String n() {return result.field.name+"="+field.name+"<="+b.field.name;}
      };
     }

    void greaterThan(At b, At result)                                           // Whether  a > b
     {zz(); sameSize(b);
      final At a = this;
      P.new I()
       {void a()
         {result.setOff().setInt(!lessThan(b.setOff()) && !equal(b) ? 1 : 0);
         }
        String v()
         {return result.verilogLoad()+
             " <= "+a.verilogLoad()+
             " > " +b.verilogLoad()+ "; /* greaterThan */";
         }
        String n() {return result.field.name+"="+field.name+">"+b.field.name;}
       };
     }

    void greaterThanOrEqual(At b, At result)                                    // Whether  a >= b
     {zz(); sameSize(b);
      final At a = this;
      P.new I()
       {void a()
         {result.setOff().setInt(!lessThan(b.setOff()) ? 1 : 0);
         }
        String v()
         {return result.verilogLoad()+
            " <= "+a.verilogLoad()+
            " >= "+b.verilogLoad()+ ";/* greaterThanOrEqual */";
         }
        String n() {return result.field.name+"="+field.name+">="+b.field.name;}
       };
     }

//D1 Arithmetic                                                                 // Arithmetic on integers

//D2 Binary                                                                     // Arithmetic on binary integers

    void inc()                                                                  // Increment a variable treated as an unsigned binary integer with wrap around on overflow.  Return the result after  the increment.
     {zz();
      final At a = this;
      P.new I()
       {void a()
         {setOff();
          final int i = getInt()+1;
          setInt(i);
         }
        String v()
         {final String o = ""+width+"'d1";
          return a.verilogLoad()+" <= "+a.verilogLoad()+"+"+o+"; /* inc */";
         }
        String n()
         {return "++"+field.name;
         }
       };
     }
    void dec()                                                                  // Decrement a variable treated as an unsigned binary integer with wrap around on overflow.  Return the result after  the increment.
     {zz();
      final At a = this;
      P.new I()
       {void a()
         {setOff();
          final int i = getInt()-1;
          setInt(i);
         }
        String v()
         {final String o = ""+width+"'d1";
          return a.verilogLoad()+" <= "+a.verilogLoad()+"-"+o+"; /* dec */";
         }
        String n()
         {return "--"+field.name;
         }
       };
     }

    void add(At source, int constant)                                           // Add a constant to the source and store in the target
     {zz(); sameSize(source);
      if (constant == 0) stop("Use nop() instead");                             // There is no point in adding zero
      if (logTwo(constant) > width)
       {stop("Constant too big for field. Constant:", constant,
             "too big for a field of size:", width);
       }
      final At target = this;
      P.new I()
       {void a()
         {final int v = source.setOff().getInt();
          target.setOff().setInt(v + constant);
         }
        String v()
         {final String c = (constant > 0 ? "+" : "-")+width+"'d"+abs(constant); // Create a signed constant of the same width
          final String t = target.verilogLoad(), s = source.verilogLoad();
          return t+" <= " + s + c + "; /* add1 */";
         }
        String n() {return field.name+"="+source.field.name + "+ "+ constant;}
        void   i() {}
       };
     }

    void add(At a, At b)                                                        // Add two variables and store in the target
     {z(); sameSize(a); sameSize(b);
      final At target = this;
      P.new I()
       {void a()
         {final int A = a.setOff().getInt();
          final int B = b.setOff().getInt();
          target.setOff().setInt(A + B);
         }
        String v()
         {return target.verilogLoad()+" <= "+ a.verilogLoad() + "+"
                                            + b.verilogLoad() +
            "; /* MemoryLayoutDM.add2 */";
         }
        String n() {return field.name + "=" + a.field.name + "+ " + b.field.name;}
        void   i() {}
       };
     }

    void srz()                                                                  // Shift one place to the right filling with a zero
     {z(); final At target = this;                                              // This changes inside the instruction so record it here for posterity
      final int N = width;
      P.new I()
       {void a()
         {setOff();
          for  (int i = 0; i < N - 1; ++i)                                      // Each bit to be moved
           {final boolean b = getBit(i+1);                                      // A bit to be moved
            setBit(i, b);                                                       // Copy the bit into the target
           }
          setBit(N-1, false);                                                   // Set high order bit to zero
         }
        String v()
         {return target.verilogLoad()+" <= "+ target.verilogLoad() +
                                      " >> 1; /* srz */";
         }
        String n() {return field.name + "=" + field.name + " >> 1";}
       };
     }

    void slz()                                                                  // Shift one place to the light filling with a zero
     {z(); final At target = this;                                              // This changes inside the instruction so record it here for posterity
      final int N = width;
      P.new I()
       {void a()
         {setOff();
          for  (int i = N-1; i > 0; --i)                                        // Each bit to be moved
           {final boolean b = getBit(i-1);                                      // A bit to be moved
            setBit(i, b);                                                       // Copy the bit into the target
           }
          setBit(0, false);                                                     // Set low order bit to zero
         }
        String v()
         {return target.verilogLoad()+" <= "+ target.verilogLoad() +
                                      " << 1; /* slz */";
         }
        String n() {return field.name + "=" + field.name + " << 1";}
       };
     }

//D1 Variables                                                                  // Create a variable from an at reference

    Variable alias() {zz(); return new Variable(this);}                         // Create a variable aliased to this reference
    Variable dup  () {zz(); return new Variable(P, field.name, field.width);}   // Create a variable with the same characteristics
    Variable fork () {zz(); final Variable d = dup(); d.a.move(this); return d;}// Create a variable from the source and generate an instruction to load it from the source at run time
   } // At

  At at(Layout.Field Field)                                                     // A field without indices or base addressing
   {zz(); return new At(Field);
   }

  At at(Layout.Field Field, int...Indices)                                      // A field with constant indices
   {zz(); return new At(Field,    Indices);
   }

  At at(Layout.Field Field, At...Indices)                                       // A field with  variable indices. Each index being a field with no indices
   {zz(); return new At(Field,   Indices);
   }

//D1 Verilog                                                                    // Declare and initialize a matchiung array in Verilog

  String declareVerilog()                                                       // Declare the array
   {zz();
    final StringBuilder s = new StringBuilder();                                // Verilog declaration
    final MemoryLayoutDM.BlockArray a = block;
    if (a.array)                                                                // Block memory
     {s.append("reg["+a.width+"-1 : 0] "+name()+
                  "["+a.size +"-1 : 0]; /* declareMemories_1 */");
     }
    else                                                                        // Bit memory
     {s.append("reg["+size() +"-1 : 0] "+name()+"; /* declareMemories_2 */");   // Declare the bit memory
     }
    return ""+s;
   }

  private void removeAllButLastTrailingZero(StringBuilder S)                    // Remove trailing zeros from a string
   {zz();
    final String s = S.toString().replaceAll("0+$", "");
    S.setLength(0);
    S.append(s.length() > 0 ? s : "0");
   }

  String initializeVerilog()                                                    // Initialize the array
   {zz();
    final StringBuilder s = new StringBuilder();                                // Verilog declaration
    final MemoryLayoutDM.BlockArray a = block;
    if (a.array)                                                                // Block memory
     {final int L = a.size, W = a.width;
      for   (int i = 0, p = 0; i < L; i++)
       {final StringBuilder S = new StringBuilder();
        for (int j = 0; j < W; j++, p++)
          {S.append(getBit(p) ? 1 : 0);
          }
        removeAllButLastTrailingZero(S);
        S.reverse();
        s.append(name()+"["+i+"] <= "+W+"'b"+S+";\n");                          // Initialize memory
       }
     }
    else                                                                        // Bit memory
     {final StringBuilder S = new StringBuilder();
      final int N = size();
      for (int i = 0; i < N; i++) S.append(getBit(i) ? 1 : 0);
      removeAllButLastTrailingZero(S);
      S.reverse();
      s.append(name()+" <= "+N+"'b"+S+";");                                     // Initialize memory
     }
    return ""+s;
   }

//D1 Print                                                                      // Print a memory layout

  class PrintPosition                                                           // Position in print
   {final StringBuilder s = new StringBuilder();
    int line = 1;
    int bits = 0;
    final Stack<Integer> indices = new Stack<>();
   }

  public String toString()                                                      // Print the values of the layout variable in memory
   {final PrintPosition pp = new PrintPosition();
    if (name        != null) pp.s.append("MemoryLayout: "+name+"\n");
    if (memory.name != null) pp.s.append("Memory      : "+memory.name+"\n");
    pp.s.append(String.format
       ("%4s %1s %8s  %8s   %8s   %8s     %8s   %s\n",
        "Line", "T", "At", "Wide", "Size", "Indices", "Value", "Name"));
    print(layout.top(), pp, 0);
    return pp.s.toString();
   }

  void print(Layout.Field field, PrintPosition pp, int indent)                  // Print the values of each variable in memory
   {final int   I = pp.indices.size();
    final int[]in = new int[I];
    for (int i = 0; i < I; i++) in[i] = pp.indices.elementAt(i);

    pp.s.append(String.format("%4d %s %8d  %8d",
      pp.line, field.fieldType(), pp.bits, field.width));

    final int start = pp.bits;                                                  // Start bit for this field

    pp.bits += switch(field)                                                    // Bit position
     {case Layout.Variable  v -> field.width;
      default -> 0;
     };

    pp.s.append(switch(field)                                                   // Size
     {case Layout.Variable  v -> String.format("%11s", "");
      case Layout.Array     a -> String.format("%11d", a.size);
      case Layout.Structure s -> String.format("%11s", "");
      default -> {stop("Unknown field type:", field); yield "";}
     });

    pp.s.append("   ");
    for (int i = 0; i < 4; i++)                                                 // Indices
     {if (i < pp.indices.size())
       {pp.s.append(String.format("%2d", pp.indices.elementAt(i)));
       }
      else
       {pp.s.append("  ");
       }
     }

    pp.s.append(switch(field)                                                   // Value
     {case Layout.Variable  v ->
       {final int a = field.locator.at(in);
        final int n = new At(field, in).setOff(false).result;
        yield String.format("%13d", n);
       }
      case Layout.Array     a -> String.format("%13s", "");
      case Layout.Structure s -> String.format("%13s", "");
      default -> {stop("Unknown field type:", field); yield "";}
      });

    pp.s.append("   "+("  ".repeat(indent))+field.name+"\n");                   // Name

    switch(field)                                                               // Sub fields
     {case Layout.Variable  v -> {}
      case Layout.Array     a ->                                                // Array
       {for (int i = 0; i < a.size; i++)
         {pp.indices.push(i);
          pp.line++;
          print(a.element, pp, indent+1);
          pp.indices.pop();
         }
       }
      case Layout.Union u ->                                                    // Union
       {final int end = pp.bits;                                                // Save end point of union
        for (int i = 0; i < u.fields.length; i++)
         {pp.bits = start;                                                      // Restore the start point for each field of the union
          pp.line++;
          print(u.fields[i], pp, indent+1);
          pp.bits = max(pp.bits, end);                                          // Restore end point of union
         }
       }
      case Layout.Structure s ->                                                // Structure and Union
       {for (int i = 0; i < s.fields.length; i++)
         {pp.line++;
          print(s.fields[i], pp, indent+1);
         }
       }
      default -> stop("Unknown field type:", field);
     };
   }

//D0 Tests                                                                      // Testing

  static class TestMemoryLayout
   {Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 4);
    Layout.Variable  b = l.variable ("b", 4);
    Layout.Variable  c = l.variable ("c", 4);
    Layout.Structure s = l.structure("s", a, b, c);
    Layout.Array     A = l.array    ("A", s, 3);
    Layout.Variable  d = l.variable ("d", 4);
    Layout.Variable  e = l.variable ("e", 4);
    Layout.Structure S = l.structure("S", d, A, e);
    Layout.Array     B = l.array    ("B", S, 3);
    Layout.Array     C = l.array    ("C", B, 3);
    MemoryLayoutDM   M;
    TestMemoryLayout()
     {z();
      M = new MemoryLayoutDM(l.compile(), "Test");
      M.memory.alternating(4);
     }
   }

  static void test_get_set()
   {z();
    TestMemoryLayout   t = new TestMemoryLayout();
        MemoryLayoutDM m = t.M;
        ProgramDM      p = m.P;
              Layout l = m.layout;
                      ok(m.at    (t.c,      0, 0, 0), "Test.c[0,0,0]12=15");
    p.new I() {void a() {m.setInt(t.c,  11, 0, 0, 0); }}; p.run(); p.clear();
                      ok(m.at    (t.c,      0, 0, 0), "Test.c[0,0,0]12=11");

                      ok(m.at( t.c,      0, 0, 1), "Test.c[0,0,1]24=0");
    p.new I() {void a() {m.setInt(t.c,  11, 0, 0, 1); }}; p.run(); p.clear();
                      ok(m.at( t.c,      0, 0, 1), "Test.c[0,0,1]24=11");

                      ok(m.at    (t.a,     0, 2, 2), "Test.a[0,2,2]116=15");
    p.new I() {void a() {m.setInt(t.a,  5, 0, 2, 2); }}; p.run(); p.clear();
                      ok(m.at    (t.a,     0, 2, 2), "Test.a[0,2,2]116=5");

    ok(m.at( t.b,      1, 2, 2), "Test.b[1,2,2]252=15");
    p.new I() {void a() {m.setInt(t.b,   7, 1, 2, 2); }}; p.run(); p.clear();
    ok(m.at( t.b,      1, 2, 2), "Test.b[1,2,2]252=7");

                      ok(m.at    (t.e,     1, 2), "Test.e[1,2]260=15");
    p.new I() {void a() {m.setInt(t.e, 11, 1, 2);  }}; p.run(); p.clear();
                      ok(m.at    (t.e,     1, 2), "Test.e[1,2]260=11");
   }

  static void test_boolean()
   {z();
    TestMemoryLayout t = new TestMemoryLayout();
        MemoryLayoutDM m = t.M;
        ProgramDM      p = m.P;
    p.new I()
     {void a()
       {m.setInt(t.a, 1, 0, 1, 1);
        m.setInt(t.a, 2, 0, 1, 2);
        m.setInt(t.a, 1, 0, 2, 1);
        m.setInt(t.a, 2, 0, 2, 2);
       }
     };
    m.P.run(); m.P.clear();

    m.at(t.a, 0, 1, 1).equal             (m.at(t.a, 0, 2, 1), m.at(t.e, 0, 0)); m.P.run(); m.P.clear(); ok(m.at(t.e, 0, 0).getInt(), 1);
    m.at(t.a, 0, 1, 1).equal             (m.at(t.a, 0, 1, 2), m.at(t.e, 0, 0)); m.P.run(); m.P.clear(); ok(m.at(t.e, 0, 0).getInt(), 0);
    m.at(t.a, 0, 1, 1).notEqual          (m.at(t.a, 0, 2, 1), m.at(t.e, 0, 0)); m.P.run(); m.P.clear(); ok(m.at(t.e, 0, 0).getInt(), 0);
    m.at(t.a, 0, 1, 1).notEqual          (m.at(t.a, 0, 1, 2), m.at(t.e, 0, 0)); m.P.run(); m.P.clear(); ok(m.at(t.e, 0, 0).getInt(), 1);

    m.at(t.a, 0, 1, 1).lessThan          (m.at(t.a, 0, 1, 2), m.at(t.e, 0, 0)); m.P.run(); m.P.clear(); ok(m.at(t.e, 0, 0).getInt(), 1);
    m.at(t.a, 0, 1, 1).lessThan          (m.at(t.a, 0, 2, 1), m.at(t.e, 0, 0)); m.P.run(); m.P.clear(); ok(m.at(t.e, 0, 0).getInt(), 0);
    m.at(t.a, 0, 1, 1).lessThanOrEqual   (m.at(t.a, 0, 1, 2), m.at(t.e, 0, 0)); m.P.run(); m.P.clear(); ok(m.at(t.e, 0, 0).getInt(), 1);
    m.at(t.a, 0, 1, 1).lessThanOrEqual   (m.at(t.a, 0, 2, 1), m.at(t.e, 0, 0)); m.P.run(); m.P.clear(); ok(m.at(t.e, 0, 0).getInt(), 1);
    m.at(t.a, 0, 1, 2).lessThanOrEqual   (m.at(t.a, 0, 2, 1), m.at(t.e, 0, 0)); m.P.run(); m.P.clear(); ok(m.at(t.e, 0, 0).getInt(), 0);

    m.at(t.a, 0, 1, 1).greaterThan       (m.at(t.a, 0, 1, 2), m.at(t.e, 0, 0)); m.P.run(); m.P.clear(); ok(m.at(t.e, 0, 0).getInt(), 0);
    m.at(t.a, 0, 1, 2).greaterThan       (m.at(t.a, 0, 1, 1), m.at(t.e, 0, 0)); m.P.run(); m.P.clear(); ok(m.at(t.e, 0, 0).getInt(), 1);
    m.at(t.a, 0, 2, 1).greaterThanOrEqual(m.at(t.a, 0, 1, 1), m.at(t.e, 0, 0)); m.P.run(); m.P.clear(); ok(m.at(t.e, 0, 0).getInt(), 1);
    m.at(t.a, 0, 1, 1).greaterThanOrEqual(m.at(t.a, 0, 2, 1), m.at(t.e, 0, 0)); m.P.run(); m.P.clear(); ok(m.at(t.e, 0, 0).getInt(), 1);
    m.at(t.a, 0, 2, 1).greaterThanOrEqual(m.at(t.a, 0, 1, 2), m.at(t.e, 0, 0)); m.P.run(); m.P.clear(); ok(m.at(t.e, 0, 0).getInt(), 0);
   }

  static void test_copy()
   {z();
    TestMemoryLayout t = new TestMemoryLayout();
        MemoryLayoutDM m = t.M;
        ProgramDM      p = m.P;
    p.new I()
     {void a()
       {m.setInt(t.a, 1, 0, 0, 0);
        m.setInt(t.a, 2, 0, 0, 1);
        m.setInt(t.a, 3, 0, 0, 2);
       }
     };
    m.P.run(); m.P.clear();

    ok(m.at(t.a, 0, 0, 0), "Test.a[0,0,0]4=1");
    ok(m.at(t.a, 0, 0, 1), "Test.a[0,0,1]16=2");
    ok(m.at(t.a, 0, 0, 2), "Test.a[0,0,2]28=3");

    m.at(t.a, 0, 0, 1).move(m.at(t.a, 0, 0, 0));
    m.at(t.a, 0, 0, 1).move(m.at(t.a, 0, 0, 1));
    t.M.P.run();

    ok(m.at(t.a, 0, 0, 0), "Test.a[0,0,0]4=1");
    ok(m.at(t.a, 0, 0, 1), "Test.a[0,0,1]16=1");
    ok(m.at(t.a, 0, 0, 2), "Test.a[0,0,2]28=3");
   }

  static void test_move()
   {z();
    Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 4);
    Layout.Variable  b = l.variable ("b", 4);
    Layout.Variable  c = l.variable ("c", 4);
    Layout.Variable  d = l.variable ("d", 4);
    Layout.Variable  e = l.variable ("e", 4);
    Layout.Structure s = l.structure("s", a, b, c, d, e);

    MemoryLayoutDM   m = new MemoryLayoutDM(l.compile(), "test");
    m.setIntInstruction(a, 13);
    m.at(a).moveTo(m.at(b), m.at(d));
    m.P.run();

    //stop(m);
    ok(m, """
MemoryLayout: test
Memory      : test
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        20                                      s
   2 V        0         4                                 13     a
   3 V        4         4                                 13     b
   4 V        8         4                                  0     c
   5 V       12         4                                 13     d
   6 V       16         4                                  0     e
""");
   }

  static void test_set_inc_dec_get()
   {z();
    Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 4);
    Layout.Variable  b = l.variable ("b", 4);
    Layout.Structure s = l.structure("s", a, b);
    MemoryLayoutDM   m = new MemoryLayoutDM(l.compile(), "test");

    m.at(a).setInt(1);
    m.at(b).setInt(3);
    //stop(m);
    ok(m, """
MemoryLayout: test
Memory      : test
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0         8                                      s
   2 V        0         4                                  1     a
   3 V        4         4                                  3     b
""");

    m.at(a).inc();
    m.at(b).dec();
    m.P.run(); m.P.clear();

    ok(m.at(a).getInt(), 2);
    ok(m.at(b).getInt(), 2);

    //stop(m);
    ok(m, """
MemoryLayout: test
Memory      : test
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0         8                                      s
   2 V        0         4                                  2     a
   3 V        4         4                                  2     b
""");

    ok(m.at(a).getInt(), 2);
    ok(m.at(b).getInt(), 2);

    m.at(a).dec();
    m.at(b).inc();
    m.P.run();
    //stop(m);

    ok(m.at(a).getInt(), 1);
    ok(m.at(b).getInt(), 3);
   }

  static void test_addressing()
   {z();
    final int N = 8;
    Layout           l = Layout.layout();
    Layout.Variable  z = l.variable ("z", N);
    Layout.Variable  i = l.variable ("i", N);
    Layout.Variable  j = l.variable ("j", N);
    Layout.Variable  a = l.variable ("a", N);
    Layout.Array     A = l.array    ("A", a, N);
    Layout.Structure S = l.structure("S", z, i, j, A);
    MemoryLayoutDM   m = new MemoryLayoutDM(l.compile(), "test");
    ProgramDM        p = m.P;

    MemoryLayoutDM.At  Z = m.at(z), I = m.at(i), J = m.at(j);

    p.new I()
     {void a()
       {z();
        Z.setOff().setInt(0);
        I.setOff().setInt(1);
        J.setOff().setInt(2);
       }
     };
    m.P.run(); m.P.clear();

    MemoryLayoutDM.At az = m.at(a, Z);
    MemoryLayoutDM.At ai = m.at(a, I);
    MemoryLayoutDM.At aj = m.at(a, J);

    p.new I()
     {void a()
       {z();
        az.setOff().setInt(10);
        ai.setOff().setInt(11);
        aj.setOff().setInt(12);
       }
     };
    m.P.run(); m.P.clear();

    //stop(m.memory);
    ok(""+m.memory, """
Memory: test
      4... 4... 4... 4... 3... 3... 3... 3... 2... 2... 2... 2... 1... 1... 1... 1...
Line  FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210
   0  0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0c0b 0a02 0100
""");

    //stop(m);
    ok(""+m, """
MemoryLayout: test
Memory      : test
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        88                                      S
   2 V        0         8                                  0     z
   3 V        8         8                                  1     i
   4 V       16         8                                  2     j
   5 A       24        64          8                             A
   6 V       24         8               0                 10       a
   7 V       32         8               1                 11       a
   8 V       40         8               2                 12       a
   9 V       48         8               3                  0       a
  10 V       56         8               4                  0       a
  11 V       64         8               5                  0       a
  12 V       72         8               6                  0       a
  13 V       80         8               7                  0       a
""");

    I.setInt(3);
    J.setInt(4);
    p.new I()
     {void a()
       {ai.setOff().setInt(13);
        aj.setOff().setInt(14);
       }
     };
    m.P.run(); m.P.clear();

    //stop(m);
    ok(""+m, """
MemoryLayout: test
Memory      : test
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        88                                      S
   2 V        0         8                                  0     z
   3 V        8         8                                  3     i
   4 V       16         8                                  4     j
   5 A       24        64          8                             A
   6 V       24         8               0                 10       a
   7 V       32         8               1                 11       a
   8 V       40         8               2                 12       a
   9 V       48         8               3                 13       a
  10 V       56         8               4                 14       a
  11 V       64         8               5                  0       a
  12 V       72         8               6                  0       a
  13 V       80         8               7                  0       a
""");
   }

  static void test_boolean_result()
   {z();
    Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 4);
    Layout.Variable  b = l.variable ("b", 4);
    Layout.Bit       r = l.bit      ("r");
    Layout.Bit       R = l.bit      ("R");
    Layout.Structure s = l.structure("s", a, b, r, R);
    MemoryLayoutDM   m = new MemoryLayoutDM(l.compile(), "test");

    m.at(a).setInt(1);
    m.at(b).setInt(2);

    m.at(a).lessThan(m.at(b), m.at(r));
    m.at(b).lessThan(m.at(a), m.at(R));
    m.P.run();

    ok(m.at(r).getInt(), 1);
    ok(m.at(R).getInt(), 0);
   }

  static void test_is_ones_or_zeros()
   {z();
    Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 4);
    Layout.Variable  b = l.variable ("b", 4);
    Layout.Variable  c = l.variable ("c", 4);
    Layout.Bit       A = l.bit      ("A");
    Layout.Bit       B = l.bit      ("B");
    Layout.Bit       C = l.bit      ("C");
    Layout.Structure s = l.structure("s", a, b, c, A, B, C);
    MemoryLayoutDM   m = new MemoryLayoutDM(l.compile(), "test");

    m.at(a).zero();
    m.P.new I() {void a() {m.at(b).setInt(2);}};
    m.at(c).ones();
    m.P.run(); m.P.clear();
    //stop(m);
    ok(m, """
MemoryLayout: test
Memory      : test
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        15                                      s
   2 V        0         4                                  0     a
   3 V        4         4                                  2     b
   4 V        8         4                                 15     c
   5 B       12         1                                  0     A
   6 B       13         1                                  0     B
   7 B       14         1                                  0     C
""");

    m.at(a).isZero(m.at(A));
    m.at(b).isZero(m.at(B));
    m.at(c).isZero(m.at(C));
    m.P.run(); m.P.clear();
    //stop(m);
    ok(m.at(A).isOnes());
    ok(m.at(B).isZero());
    ok(m.at(C).isZero());

    m.at(a).isOnes(m.at(A));
    m.at(b).isOnes(m.at(B));
    m.at(c).isOnes(m.at(C));
    m.P.run(); m.P.clear();
    //stop(m);
    ok(m.at(A).isZero());
    ok(m.at(B).isZero());
    ok(m.at(C).isOnes());
   }

  static void test_union()
   {z();
    Layout               l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 2);
    Layout.Variable  b = l.variable ("b", 2);
    Layout.Variable  c = l.variable ("c", 2);
    Layout.Structure s = l.structure("s", a, b, c);
    Layout.Variable  A = l.variable ("A", 4);
    Layout.Variable  B = l.variable ("B", 4);
    Layout.Variable  C = l.variable ("C", 4);
    Layout.Structure S = l.structure("S", A, B, C);
    Layout.Union     u = l.union    ("u", s, S);
    Layout.Array     r = l.array    ("r", u, 4);
    MemoryLayoutDM   m = new MemoryLayoutDM(l.compile(), "test");

    m.at(a, 0).ones();  m.at(a, 1).ones();    m.at(a, 2).ones();    m.at(a, 3).ones();
    m.at(b, 0).zero();  m.at(b, 1).zero();    m.at(b, 2).zero();    m.at(b, 3).zero();
    m.at(c, 0).ones();  m.at(c, 1).ones();    m.at(c, 2).ones();    m.at(c, 3).ones();
    m.P.run();
    //stop(m);
    ok(m, """
MemoryLayout: test
Memory      : test
Line T       At      Wide       Size    Indices        Value   Name
   1 A        0        48          4                           r
   2 U        0        12               0                        u
   3 S        0         6               0                          s
   4 V        0         2               0                  3         a
   5 V        2         2               0                  0         b
   6 V        4         2               0                  3         c
   7 S        0        12               0                          S
   8 V        0         4               0                  3         A
   9 V        4         4               0                  3         B
  10 V        8         4               0                  0         C
  11 U       12        12               1                        u
  12 S       12         6               1                          s
  13 V       12         2               1                  3         a
  14 V       14         2               1                  0         b
  15 V       16         2               1                  3         c
  16 S       12        12               1                          S
  17 V       12         4               1                  3         A
  18 V       16         4               1                  3         B
  19 V       20         4               1                  0         C
  20 U       24        12               2                        u
  21 S       24         6               2                          s
  22 V       24         2               2                  3         a
  23 V       26         2               2                  0         b
  24 V       28         2               2                  3         c
  25 S       24        12               2                          S
  26 V       24         4               2                  3         A
  27 V       28         4               2                  3         B
  28 V       32         4               2                  0         C
  29 U       36        12               3                        u
  30 S       36         6               3                          s
  31 V       36         2               3                  3         a
  32 V       38         2               3                  0         b
  33 V       40         2               3                  3         c
  34 S       36        12               3                          S
  35 V       36         4               3                  3         A
  36 V       40         4               3                  3         B
  37 V       44         4               3                  0         C
""");
   }

  static void test_verilog_address()
   {Layout               l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 2);
    Layout.Variable  b = l.variable ("b", 2);
    Layout.Variable  c = l.variable ("c", 2);
    Layout.Structure s = l.structure("s", a, b, c);
    Layout.Array     A = l.array    ("A", s, 4);
    Layout.Variable  I = l.variable ("I", 4);
    Layout.Variable  J = l.variable ("J", 4);
    Layout.Structure S = l.structure("S", A, I, J);
    l.compile();

    MemoryLayoutDM   m = new MemoryLayoutDM(l.compile(), "M");
    m.program(m.P);                                                             // Specify that the name of the memory is unique
    //stop(m);
    ok(m, """
MemoryLayout: M
Memory      : M
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        32                                      S
   2 A        0        24          4                             A
   3 S        0         6               0                          s
   4 V        0         2               0                  0         a
   5 V        2         2               0                  0         b
   6 V        4         2               0                  0         c
   7 S        6         6               1                          s
   8 V        6         2               1                  0         a
   9 V        8         2               1                  0         b
  10 V       10         2               1                  0         c
  11 S       12         6               2                          s
  12 V       12         2               2                  0         a
  13 V       14         2               2                  0         b
  14 V       16         2               2                  0         c
  15 S       18         6               3                          s
  16 V       18         2               3                  0         a
  17 V       20         2               3                  0         b
  18 V       22         2               3                  0         c
  19 V       24         4                                  0     I
  20 V       28         4                                  0     J
""");

    MemoryLayoutDM.At at = m.at(a, m.at(I));
    final String n = m.name();
    ok(at.verilogLoad(), n+"[       0/*a       */ + "+n+"[      24/*I       */ +: 4] * 6 +: 2]");
    ok(at.verilogAddr(),   "       0/*a       */ + "+n+"[      24/*I       */ +: 4] * 6");
    ok(m.at(a, 0).verilogLoad(), n+"[       0/*a       */ + 0 * 6 +: 2]");
    ok(m.at(a, 1).verilogLoad(), n+"[       0/*a       */ + 1 * 6 +: 2]");
    ok(m.at(a, 2).verilogLoad(), n+"[       0/*a       */ + 2 * 6 +: 2]");
    ok(m.at(b, 0).verilogLoad(), n+"[       2/*b       */ + 0 * 6 +: 2]");
    ok(m.at(b, 1).verilogLoad(), n+"[       2/*b       */ + 1 * 6 +: 2]");
    ok(m.at(b, 2).verilogLoad(), n+"[       2/*b       */ + 2 * 6 +: 2]");
   }

  static void test_copy_memory()
   {z();
    final int N = 8;
    Layout               l = Layout.layout();
    Layout.Variable  a = l.variable ("a", N);
    Layout.Variable  b = l.variable ("b", N);
    Layout.Variable  c = l.variable ("c", N);
    Layout.Structure s = l.structure("s", a, b, c);
    l.compile();

    Layout               L = Layout.layout();
    Layout.Variable  A = L.variable ("A", N);
    L.compile();

    MemoryLayoutDM.numbers = 0;
    MemoryLayoutDM m = new MemoryLayoutDM(l, "m");
    MemoryLayoutDM M = new MemoryLayoutDM(L, "M");
    m.program(m.P);                                                             // Specify that the name of the memory is unique
    M.program(m.P);

    m.at(a).setInt(1);
    m.at(b).setInt(2);

    //stop(m);
    ok(""+m, """
MemoryLayout: m
Memory      : m
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        24                                      s
   2 V        0         8                                  1     a
   3 V        8         8                                  2     b
   4 V       16         8                                  0     c
""");

    M.copy(m.at(a));
    //stop(m.P.printVerilog());
    ok(m.P.printVerilog(), """
   1  M[0 +: 8] <= m[       0/*a       */ +: 8]; /* copy2 */
""");

    m.P.run(); m.P.clear();
    //stop(M);
    ok(M, """
MemoryLayout: M
Memory      : M
Line T       At      Wide       Size    Indices        Value   Name
   1 V        0         8                                  1   A
""");

    m.at(c).copy(M);
    ok(m.P.printVerilog(), """
   1  m[      16/*c       */ +: 8] <= M[0 +: 8]; /* copy3 */
""");
    m.P.run(); m.P.clear();
    //stop(m);
    ok(m, """
MemoryLayout: m
Memory      : m
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        24                                      s
   2 V        0         8                                  1     a
   3 V        8         8                                  2     b
   4 V       16         8                                  1     c
""");

    M.copy(m.at(b));
    ok(m.P.printVerilog(), """
   1  M[0 +: 8] <= m[       8/*b       */ +: 8]; /* copy2 */
""");

    m.P.run(); m.P.clear();
    //stop(M);
    ok(M, """
MemoryLayout: M
Memory      : M
Line T       At      Wide       Size    Indices        Value   Name
   1 V        0         8                                  2   A
""");

    m.at(c).copy(M);
    ok(m.P.printVerilog(), """
   1  m[      16/*c       */ +: 8] <= M[0 +: 8]; /* copy3 */
""");
    m.P.run(); m.P.clear();
    //stop(m);
    ok(m, """
MemoryLayout: m
Memory      : m
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        24                                      s
   2 V        0         8                                  1     a
   3 V        8         8                                  2     b
   4 V       16         8                                  2     c
""");

    m.at(c).copy(m.at(a));
    ok(m.P.printVerilog(), """
   1  m[      16/*c       */ +: 8] <= m[       0/*a       */ +: 8]; /* copy4 */
""");
    m.P.run(); m.P.clear();
//  stop(m);
    ok(m, """
MemoryLayout: m
Memory      : m
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        24                                      s
   2 V        0         8                                  1     a
   3 V        8         8                                  2     b
   4 V       16         8                                  1     c
""");
   }

  static void test_array_addressing()
   {z();
    Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 4);
    Layout.Variable  b = l.variable ("b", 4);
    Layout.Structure s = l.structure("s", a, b);
    Layout.Array     A = l.array    ("A", s, 6);
    MemoryLayoutDM   m = new MemoryLayoutDM(l.compile(), "array");

    Layout           L = Layout.layout();
    Layout.Variable  i = L.variable ("i", 4);
    MemoryLayoutDM   M = new MemoryLayoutDM(L.compile(), "index");

    Layout           p = Layout.layout();
    Layout.Variable  q = p.variable ("q", 8);
    MemoryLayoutDM   Q = new MemoryLayoutDM(p.compile(), "block1");

    Layout           x = Layout.layout();
    Layout.Variable  y = x.variable ("y", 8);
    MemoryLayoutDM   Y = new MemoryLayoutDM(x.compile(), "block2");
    m.program(m.P);                                                             // Show that the memory layout name is unique
    M.program(m.P);
    Q.program(m.P);
    Y.program(m.P);

    ok(m.block.array);
    ok(m.block.size,  6);
    ok(m.block.width, 8);
    ok(m.block.blocked());
    ok(m.block.log, 3);

//    M.at(i).setInt(1);
//    ok(m.verilogArrayElement(M.at(i)), "array[index[       0/*i       */ +: 4]] /* MemoryLayoutDM power 2 array access */");
//
//    m.memory.alternating(4);
//    Q.loadFirstBlock(m);
//    m.P.run(); m.P.clear();
//    //stop(Q);
//    ok(Q, """
//MemoryLayout: block1
//Memory      : block1
//Line T       At      Wide       Size    Indices        Value   Name
//   1 V        0         8                                240   q
//""");
//
//    Q.memory.alternating(2);
//    Q.saveFirstBlock(m);
//    m.P.run(); m.P.clear();
//    //stop(m);
//    ok(m, """
//MemoryLayout: array
//Memory      : array
//Line T       At      Wide       Size    Indices        Value   Name
//   1 A        0        48          6                           A
//   2 S        0         8               0                        s
//   3 V        0         4               0                 12       a
//   4 V        4         4               0                 12       b
//   5 S        8         8               1                        s
//   6 V        8         4               1                  0       a
//   7 V       12         4               1                 15       b
//   8 S       16         8               2                        s
//   9 V       16         4               2                  0       a
//  10 V       20         4               2                 15       b
//  11 S       24         8               3                        s
//  12 V       24         4               3                  0       a
//  13 V       28         4               3                 15       b
//  14 S       32         8               4                        s
//  15 V       32         4               4                  0       a
//  16 V       36         4               4                 15       b
//  17 S       40         8               5                        s
//  18 V       40         4               5                  0       a
//  19 V       44         4               5                 15       b
//""");
//
//    M.setIntInstruction(i, 3);
//    Q.saveBlock(m, M.at(i));
//    m.P.run(); m.P.clear();
//    //stop(m);
//    ok(m, """
//MemoryLayout: array
//Memory      : array
//Line T       At      Wide       Size    Indices        Value   Name
//   1 A        0        48          6                           A
//   2 S        0         8               0                        s
//   3 V        0         4               0                 12       a
//   4 V        4         4               0                 12       b
//   5 S        8         8               1                        s
//   6 V        8         4               1                  0       a
//   7 V       12         4               1                 15       b
//   8 S       16         8               2                        s
//   9 V       16         4               2                  0       a
//  10 V       20         4               2                 15       b
//  11 S       24         8               3                        s
//  12 V       24         4               3                 12       a
//  13 V       28         4               3                 12       b
//  14 S       32         8               4                        s
//  15 V       32         4               4                  0       a
//  16 V       36         4               4                 15       b
//  17 S       40         8               5                        s
//  18 V       40         4               5                  0       a
//  19 V       44         4               5                 15       b
//""");
//
//    Y.loadBlock(m, M.at(i));
//    m.P.run(); m.P.clear();
//    //stop(Y);
//    ok(Y, """
//MemoryLayout: block2
//Memory      : block2
//Line T       At      Wide       Size    Indices        Value   Name
//   1 V        0         8                                204   y
//""");
//
//    M.setIntInstruction(i, 2);
//    Q.ones();
//    Q.saveBlock(m, M.at(i));
//    m.P.run(); m.P.clear();
//    //stop(m);
//    ok(m, """
//MemoryLayout: array
//Memory      : array
//Line T       At      Wide       Size    Indices        Value   Name
//   1 A        0        48          6                           A
//   2 S        0         8               0                        s
//   3 V        0         4               0                 12       a
//   4 V        4         4               0                 12       b
//   5 S        8         8               1                        s
//   6 V        8         4               1                  0       a
//   7 V       12         4               1                 15       b
//   8 S       16         8               2                        s
//   9 V       16         4               2                 15       a
//  10 V       20         4               2                 15       b
//  11 S       24         8               3                        s
//  12 V       24         4               3                 12       a
//  13 V       28         4               3                 12       b
//  14 S       32         8               4                        s
//  15 V       32         4               4                  0       a
//  16 V       36         4               4                 15       b
//  17 S       40         8               5                        s
//  18 V       40         4               5                  0       a
//  19 V       44         4               5                 15       b
//""");
   }

  static void test_upDownMoveBuffer()
   {z();
    final int M = 4, N = 6;
    Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", M);
    Layout.Array     A = l.array    ("A", a, N);
    Layout.Variable  i = l.variable ("i", M);
    Layout.Variable  z = l.variable ("z", M);
    Layout.Structure S = l.structure("s", z, A, i);
    MemoryLayoutDM   m = new MemoryLayoutDM(l.compile(), "arrays");
    m.program(m.P);

    for (int j = 0; j < N; j++) m.at(a, j).setInt(j);
    m.at(z).setInt(13);
    m.at(i).setInt(1);

    //stop(m);
    ok(m, """
MemoryLayout: arrays
Memory      : arrays
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        32                                      s
   2 V        0         4                                 13     z
   3 A        4        24          6                             A
   4 V        4         4               0                  0       a
   5 V        8         4               1                  1       a
   6 V       12         4               2                  2       a
   7 V       16         4               3                  3       a
   8 V       20         4               4                  4       a
   9 V       24         4               5                  5       a
  10 V       28         4                                  1     i
""");

    m.at(A).upMoveBuffer(M);
    m.P.run();
    //stop(m.P.printVerilog());
    ok(""+m.P.printVerilog(), """
   1  arrays[0+4+:20] <= arrays[0+:20];/* upMoveBufferLinear */
""");
    //stop(m);
    ok(""+m, """
MemoryLayout: arrays
Memory      : arrays
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        32                                      s
   2 V        0         4                                 13     z
   3 A        4        24          6                             A
   4 V        4         4               0                  0       a
   5 V        8         4               1                  0       a
   6 V       12         4               2                  1       a
   7 V       16         4               3                  2       a
   8 V       20         4               4                  3       a
   9 V       24         4               5                  4       a
  10 V       28         4                                  1     i
""");

    m.P.clear();
    m.at(A).downMoveBuffer(M);
    m.P.run();
    //stop(m.P.printVerilog());
    ok(""+m.P.printVerilog(), """
   1  arrays[0+:20] <= arrays[4+:20];/* downMoveBufferLinear */
""");
    //stop(m);
    ok(""+m, """
MemoryLayout: arrays
Memory      : arrays
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        32                                      s
   2 V        0         4                                 13     z
   3 A        4        24          6                             A
   4 V        4         4               0                  0       a
   5 V        8         4               1                  1       a
   6 V       12         4               2                  2       a
   7 V       16         4               3                  3       a
   8 V       20         4               4                  4       a
   9 V       24         4               5                  4       a
  10 V       28         4                                  1     i
""");
   }

  static void test_upDownMoveBuffer2()
   {z();
    final int M = 4, N = 6;
    Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", M);
    Layout.Array     A = l.array    ("A", a, N);
    Layout.Variable  i = l.variable ("i", M);
    Layout.Variable  z = l.variable ("z", M);
    Layout.Structure S = l.structure("s", z, A, i);
    MemoryLayoutDM   m = new MemoryLayoutDM(l.compile(), "arrays");
    m.program(m.P);

    for (int j = 0; j < N; j++) m.at(a, j).setInt(j);
    m.at(z).setInt(13);
    m.at(i).setInt(1);

    //stop(m);
    ok(m, """
MemoryLayout: arrays
Memory      : arrays
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        32                                      s
   2 V        0         4                                 13     z
   3 A        4        24          6                             A
   4 V        4         4               0                  0       a
   5 V        8         4               1                  1       a
   6 V       12         4               2                  2       a
   7 V       16         4               3                  3       a
   8 V       20         4               4                  4       a
   9 V       24         4               5                  5       a
  10 V       28         4                                  1     i
""");

    m.at(A).upMoveBuffer(2, M);
    m.P.run();
    //stop(m.P.printVerilog());
    ok(""+m.P.printVerilog(), """
   1  arrays[8+4+:12] <= arrays[0+:12];/* upMoveBufferLinear */
""");
    //stop(m);
    ok(""+m, """
MemoryLayout: arrays
Memory      : arrays
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        32                                      s
   2 V        0         4                                 13     z
   3 A        4        24          6                             A
   4 V        4         4               0                  0       a
   5 V        8         4               1                  1       a
   6 V       12         4               2                  2       a
   7 V       16         4               3                  2       a
   8 V       20         4               4                  3       a
   9 V       24         4               5                  4       a
  10 V       28         4                                  1     i
""");

    m.P.clear();
    m.at(A).downMoveBuffer(2, M);
    m.P.run();
    //stop(m.P.printVerilog());
    ok(""+m.P.printVerilog(), """
   1  arrays[8+:12] <= arrays[4+:12];/* downMoveBufferLinear */
""");
    //stop(m);
    ok(""+m, """
MemoryLayout: arrays
Memory      : arrays
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        32                                      s
   2 V        0         4                                 13     z
   3 A        4        24          6                             A
   4 V        4         4               0                  0       a
   5 V        8         4               1                  1       a
   6 V       12         4               2                  2       a
   7 V       16         4               3                  3       a
   8 V       20         4               4                  4       a
   9 V       24         4               5                  4       a
  10 V       28         4                                  1     i
""");
   }

  static void test_upDownMoveBuffer3()
   {z();
    final int M = 4, N = 6;
    Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", M);
    Layout.Array     A = l.array    ("A", a, N);
    Layout.Variable  i = l.variable ("i", M);
    Layout.Variable  z = l.variable ("z", M);
    Layout.Structure S = l.structure("s", z, A, i);
    MemoryLayoutDM   m = new MemoryLayoutDM(l.compile(), "arrays");
    m.program(m.P);

    for (int j = 0; j < N; j++) m.at(a, j).setInt(j);
    m.at(z).setInt(13);
    m.at(i).setInt(1);

    //stop(m);
    ok(m, """
MemoryLayout: arrays
Memory      : arrays
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        32                                      s
   2 V        0         4                                 13     z
   3 A        4        24          6                             A
   4 V        4         4               0                  0       a
   5 V        8         4               1                  1       a
   6 V       12         4               2                  2       a
   7 V       16         4               3                  3       a
   8 V       20         4               4                  4       a
   9 V       24         4               5                  5       a
  10 V       28         4                                  1     i
""");

    m.at(A).upMoveBuffer(m.at(i), M, 2);
    m.P.run();
    //stop(m.P.printVerilog());
    ok(""+m.P.printVerilog(), """
   1  begin arrays[arrays[      28/*i       */ +: 4]+4+:8] <= arrays[arrays[      28/*i       */ +: 4]+:8]; end /* upMoveBufferBlock */
""");
    //stop(m);
    ok(""+m, """
MemoryLayout: arrays
Memory      : arrays
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        32                                      s
   2 V        0         4                                 13     z
   3 A        4        24          6                             A
   4 V        4         4               0                  0       a
   5 V        8         4               1                  1       a
   6 V       12         4               2                  1       a
   7 V       16         4               3                  2       a
   8 V       20         4               4                  4       a
   9 V       24         4               5                  5       a
  10 V       28         4                                  1     i
""");

    m.P.clear();
    m.at(A).downMoveBuffer(m.at(i), M, 2);
    m.P.run();
    //stop(m.P.printVerilog());
    ok(""+m.P.printVerilog(), """
   1  begin arrays[arrays[      28/*i       */ +: 4]+:8] <= arrays[arrays[      28/*i       */ +: 4]+4+:8]; end /* downMoveBufferBlock */
""");
    //stop(m);
    ok(""+m, """
MemoryLayout: arrays
Memory      : arrays
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        32                                      s
   2 V        0         4                                 13     z
   3 A        4        24          6                             A
   4 V        4         4               0                  0       a
   5 V        8         4               1                  1       a
   6 V       12         4               2                  2       a
   7 V       16         4               3                  2       a
   8 V       20         4               4                  4       a
   9 V       24         4               5                  5       a
  10 V       28         4                                  1     i
""");
   }

  static void test_copyMoveBuffer()
   {z();
    numbers = 0;                                                                // Reset mnemory numbering to make tests reliable
    final int M = 4, N = 6;
    Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", M);
    Layout.Array     A = l.array    ("A", a, N);
    Layout.Variable  i = l.variable ("i", M);
    Layout.Variable  z = l.variable ("z", M);
    Layout.Structure S = l.structure("s", z, A, i);
    MemoryLayoutDM   m = new MemoryLayoutDM(l.compile(), "arrays");
    m.program(m.P);

    MemoryLayoutDM.At B = m.at(A).createMoveBuffer();

    for (int j = 0; j < N; j++) m.at(a, j).setInt(j);
    m.at(z).setInt(13);
    m.at(i).setInt(1);

    //stop(m);
    ok(m, """
MemoryLayout: arrays
Memory      : arrays
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        32                                      s
   2 V        0         4                                 13     z
   3 A        4        24          6                             A
   4 V        4         4               0                  0       a
   5 V        8         4               1                  1       a
   6 V       12         4               2                  2       a
   7 V       16         4               3                  3       a
   8 V       20         4               4                  4       a
   9 V       24         4               5                  5       a
  10 V       28         4                                  1     i
""");

    B.copyMoveBuffer(m.at(A));
    m.P.run();

    //stop(B.ml().P.printVerilog());
    ok(B.ml().P.printVerilog(), """
   1  buffer_2[       0/*b       */+:24] <= arrays[       4/*A       */+:24];/* copyMoveBuffer */
""");

    //stop(B.ml().memory());
    ok  (B.ml().memory(), """
Memory: buffer
      4... 4... 4... 4... 3... 3... 3... 3... 2... 2... 2... 2... 1... 1... 1... 1...
Line  FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210
   0  0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0054 3210
""");

    m.P.clear();
    B.upMoveBuffer(m.at(i), M, 2);
    m.P.run();

    //stop(B.ml().P.printVerilog());
    ok(B.ml().P.printVerilog(), """
   1  begin buffer_2[arrays[      28/*i       */ +: 4]+4+:8] <= buffer_2[arrays[      28/*i       */ +: 4]+:8]; end /* upMoveBufferBlock */
""");

    //stop(B.ml().memory());
    ok  (B.ml().memory(), """
Memory: buffer
      4... 4... 4... 4... 3... 3... 3... 3... 2... 2... 2... 2... 1... 1... 1... 1...
Line  FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210
   0  0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0054 2110
""");

    m.P.clear();
    m.at(A).copyMoveBuffer(B);
    m.P.run();
    //stop(m.P.printVerilog());
    ok(""+m.P.printVerilog(), """
   1  arrays[       4/*A       */+:24] <= buffer_2[       0/*b       */+:24];/* copyMoveBuffer */
""");
    //stop(m);
    ok(""+m, """
MemoryLayout: arrays
Memory      : arrays
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        32                                      s
   2 V        0         4                                 13     z
   3 A        4        24          6                             A
   4 V        4         4               0                  0       a
   5 V        8         4               1                  1       a
   6 V       12         4               2                  1       a
   7 V       16         4               3                  2       a
   8 V       20         4               4                  4       a
   9 V       24         4               5                  5       a
  10 V       28         4                                  1     i
""");
   }

  static void test_moveUpAll()
   {z();
    final int M = 4, N = 6;
    Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", M);
    Layout.Array     A = l.array    ("A", a, N);
    Layout.Variable  z = l.variable ("z", M);
    Layout.Structure S = l.structure("s", z, A);
    MemoryLayoutDM   m = new MemoryLayoutDM(l.compile(), "arrays");

    for (int i = 0; i < N; i++) m.at(a, i).setInt(i);
    m.at(z).setInt(11);
    //stop(m);
    ok(""+m, """
MemoryLayout: arrays
Memory      : arrays
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        28                                      s
   2 V        0         4                                 11     z
   3 A        4        24          6                             A
   4 V        4         4               0                  0       a
   5 V        8         4               1                  1       a
   6 V       12         4               2                  2       a
   7 V       16         4               3                  3       a
   8 V       20         4               4                  4       a
   9 V       24         4               5                  5       a
""");

    m.at(A).moveUp();
    m.P.run(); m.P.clear();
    //stop(m);
    ok(m, """
MemoryLayout: arrays
Memory      : arrays
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        28                                      s
   2 V        0         4                                 11     z
   3 A        4        24          6                             A
   4 V        4         4               0                  0       a
   5 V        8         4               1                  0       a
   6 V       12         4               2                  1       a
   7 V       16         4               3                  2       a
   8 V       20         4               4                  3       a
   9 V       24         4               5                  4       a
""");
   }

  static void test_moveUpLog()
   {z();
    numbers = 0;                                                                // Reset mnemory numbering to make tests reliable
    final int M = 4, N = 12;
    Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", M);
    Layout.Array     A = l.array    ("A", a, N);
    Layout.Variable  i = l.variable ("i", M);
    Layout.Variable  z = l.variable ("z", M);
    Layout.Structure S = l.structure("s", z, A, i);
    MemoryLayoutDM   m = new MemoryLayoutDM(l.compile(), "arrays");
    m.program(m.P);                                                             // Array name is unique

    for (int j = 0; j < N; j++) m.at(a, j).setInt(j);
    m.at(z).setInt(11);
    m.at(i).setInt(1);

    //stop(m);
    ok(m, """
MemoryLayout: arrays
Memory      : arrays
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        56                                      s
   2 V        0         4                                 11     z
   3 A        4        48         12                             A
   4 V        4         4               0                  0       a
   5 V        8         4               1                  1       a
   6 V       12         4               2                  2       a
   7 V       16         4               3                  3       a
   8 V       20         4               4                  4       a
   9 V       24         4               5                  5       a
  10 V       28         4               6                  6       a
  11 V       32         4               7                  7       a
  12 V       36         4               8                  8       a
  13 V       40         4               9                  9       a
  14 V       44         4              10                 10       a
  15 V       48         4              11                 11       a
  16 V       52         4                                  1     i
""");

    m.at(A).moveUp(m.at(i));
    m.P.run();
    //stop(m.P.printVerilog());
    ok(""+m.P.printVerilog(), """
   1  buffer_2[       0/*b       */+:48] <= arrays[       4/*A       */+:48];/* copyMoveBuffer */
   2  position_3[       0/*position*/ +: 4] <= 12;
   3  if (position_3[       0/*position*/ +: 4] > arrays[      52/*i       */ +: 4]+   8) begin buffer_2[(position_3[       0/*position*/ +: 4] -    8)*4 +:       32] <= buffer_2[(position_3[       0/*position*/ +: 4] -    8)*4-4 +:       32]; position_3[       0/*position*/ +: 4] <= position_3[       0/*position*/ +: 4]-   8;end /* moveUp */
   4  if (position_3[       0/*position*/ +: 4] > arrays[      52/*i       */ +: 4]+   4) begin buffer_2[(position_3[       0/*position*/ +: 4] -    4)*4 +:       16] <= buffer_2[(position_3[       0/*position*/ +: 4] -    4)*4-4 +:       16]; position_3[       0/*position*/ +: 4] <= position_3[       0/*position*/ +: 4]-   4;end /* moveUp */
   5  if (position_3[       0/*position*/ +: 4] > arrays[      52/*i       */ +: 4]+   2) begin buffer_2[(position_3[       0/*position*/ +: 4] -    2)*4 +:        8] <= buffer_2[(position_3[       0/*position*/ +: 4] -    2)*4-4 +:        8]; position_3[       0/*position*/ +: 4] <= position_3[       0/*position*/ +: 4]-   2;end /* moveUp */
   6  if (position_3[       0/*position*/ +: 4] > arrays[      52/*i       */ +: 4]+   1) begin buffer_2[(position_3[       0/*position*/ +: 4] -    1)*4 +:        4] <= buffer_2[(position_3[       0/*position*/ +: 4] -    1)*4-4 +:        4]; position_3[       0/*position*/ +: 4] <= position_3[       0/*position*/ +: 4]-   1;end /* moveUp */
   7  arrays[       4/*A       */+:48] <= buffer_2[       0/*b       */+:48];/* copyMoveBuffer */
""");
    //stop(m);
    ok(""+m, """
MemoryLayout: arrays
Memory      : arrays
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        56                                      s
   2 V        0         4                                 11     z
   3 A        4        48         12                             A
   4 V        4         4               0                  0       a
   5 V        8         4               1                  1       a
   6 V       12         4               2                  1       a
   7 V       16         4               3                  2       a
   8 V       20         4               4                  3       a
   9 V       24         4               5                  4       a
  10 V       28         4               6                  5       a
  11 V       32         4               7                  6       a
  12 V       36         4               8                  7       a
  13 V       40         4               9                  8       a
  14 V       44         4              10                  9       a
  15 V       48         4              11                 10       a
  16 V       52         4                                  1     i
""");
   }

  static void test_moveDownAll()
   {z();
    final int M = 4, N = 6;
    Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", M);
    Layout.Array     A = l.array    ("A", a, N);
    Layout.Variable  z = l.variable ("z", M);
    Layout.Structure S = l.structure("s", z, A);
    MemoryLayoutDM   m = new MemoryLayoutDM(l.compile(), "arrays");

    for (int i = 0; i < N; i++) m.at(a, i).setInt(i);
    m.at(z).setInt(11);
    //stop(m);
    ok(""+m, """
MemoryLayout: arrays
Memory      : arrays
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        28                                      s
   2 V        0         4                                 11     z
   3 A        4        24          6                             A
   4 V        4         4               0                  0       a
   5 V        8         4               1                  1       a
   6 V       12         4               2                  2       a
   7 V       16         4               3                  3       a
   8 V       20         4               4                  4       a
   9 V       24         4               5                  5       a
""");

    m.at(A).moveDown();
    m.P.run(); m.P.clear();
    //stop(m);
    ok(m, """
MemoryLayout: arrays
Memory      : arrays
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        28                                      s
   2 V        0         4                                 11     z
   3 A        4        24          6                             A
   4 V        4         4               0                  1       a
   5 V        8         4               1                  2       a
   6 V       12         4               2                  3       a
   7 V       16         4               3                  4       a
   8 V       20         4               4                  5       a
   9 V       24         4               5                  5       a
""");
   }

  static void test_moveDownLog()
   {z();
    numbers = 0;                                                                // Reset mnemory numbering to make tests reliable
    final int M = 4, N = 12;
    Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", M);
    Layout.Array     A = l.array    ("A", a, N);
    Layout.Variable  i = l.variable ("i", M);
    Layout.Variable  z = l.variable ("z", M);
    Layout.Structure S = l.structure("s", z, A, i);
    MemoryLayoutDM   m = new MemoryLayoutDM(l.compile(), "arrays");
    m.program(m.P);

    for (int j = 0; j < N; j++) m.at(a, j).setInt(j);
    m.at(z).setInt(11);
    m.at(i).setInt(1);

    //stop(m);
    ok(m, """
MemoryLayout: arrays
Memory      : arrays
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        56                                      s
   2 V        0         4                                 11     z
   3 A        4        48         12                             A
   4 V        4         4               0                  0       a
   5 V        8         4               1                  1       a
   6 V       12         4               2                  2       a
   7 V       16         4               3                  3       a
   8 V       20         4               4                  4       a
   9 V       24         4               5                  5       a
  10 V       28         4               6                  6       a
  11 V       32         4               7                  7       a
  12 V       36         4               8                  8       a
  13 V       40         4               9                  9       a
  14 V       44         4              10                 10       a
  15 V       48         4              11                 11       a
  16 V       52         4                                  1     i
""");

    m.at(A).moveDown(m.at(i));
    m.P.run();
    //stop(m.P.printVerilog());
    ok(m.P.printVerilog(), """
   1  buffer_2[       0/*b       */+:48] <= arrays[       4/*A       */+:48];/* copyMoveBuffer */
   2  position_3[       0/*position*/ +: 4] <= arrays[      52/*i       */ +: 4]/* move */;
   3  if (position_3[       0/*position*/ +: 4]+       8 < 12) begin buffer_2[position_3[       0/*position*/ +: 4]*4 +:       32] <= buffer_2[position_3[       0/*position*/ +: 4]*4+4 +:       32]; position_3[       0/*position*/ +: 4] <= position_3[       0/*position*/ +: 4]+       8;end /* moveDown */
   4  if (position_3[       0/*position*/ +: 4]+       4 < 12) begin buffer_2[position_3[       0/*position*/ +: 4]*4 +:       16] <= buffer_2[position_3[       0/*position*/ +: 4]*4+4 +:       16]; position_3[       0/*position*/ +: 4] <= position_3[       0/*position*/ +: 4]+       4;end /* moveDown */
   5  if (position_3[       0/*position*/ +: 4]+       2 < 12) begin buffer_2[position_3[       0/*position*/ +: 4]*4 +:        8] <= buffer_2[position_3[       0/*position*/ +: 4]*4+4 +:        8]; position_3[       0/*position*/ +: 4] <= position_3[       0/*position*/ +: 4]+       2;end /* moveDown */
   6  if (position_3[       0/*position*/ +: 4]+       1 < 12) begin buffer_2[position_3[       0/*position*/ +: 4]*4 +:        4] <= buffer_2[position_3[       0/*position*/ +: 4]*4+4 +:        4]; position_3[       0/*position*/ +: 4] <= position_3[       0/*position*/ +: 4]+       1;end /* moveDown */
   7  arrays[       4/*A       */+:48] <= buffer_2[       0/*b       */+:48];/* copyMoveBuffer */
""");
    //stop(m);
    ok(""+m, """
MemoryLayout: arrays
Memory      : arrays
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        56                                      s
   2 V        0         4                                 11     z
   3 A        4        48         12                             A
   4 V        4         4               0                  0       a
   5 V        8         4               1                  2       a
   6 V       12         4               2                  3       a
   7 V       16         4               3                  4       a
   8 V       20         4               4                  5       a
   9 V       24         4               5                  6       a
  10 V       28         4               6                  7       a
  11 V       32         4               7                  8       a
  12 V       36         4               8                  9       a
  13 V       40         4               9                 10       a
  14 V       44         4              10                 11       a
  15 V       48         4              11                 11       a
  16 V       52         4                                  1     i
""");
   }

  static void test_top()
   {z();
    Layout           l = Layout.layout();
    Layout.Bit       a = l.bit("a");
    MemoryLayoutDM   m = new MemoryLayoutDM(l.compile(), "A");
    ok(""+m.top(), "A.a@0=0");
    ok(m.top().at, 0);
    ok(m.top().width, m.size());
   }

  static void test_top_move()
   {z();
    final int N = 4;
    Layout           l = Layout.layout();
    Layout.Variable  a = l.variable("a", N);
    MemoryLayoutDM   m = new MemoryLayoutDM(l.compile(), "a");
    m.top().setInt(12);

    Layout           L = Layout.layout();
    Layout.Variable  A = L.variable("A", N);
    MemoryLayoutDM   M = new MemoryLayoutDM(L.compile(), "A");
    M.top().move(m.top());
    ok(""+M, """
MemoryLayout: A
Memory      : A
Line T       At      Wide       Size    Indices        Value   Name
   1 V        0         4                                  0   A
""");
    M.P.run();
    ok(""+M, """
MemoryLayout: A
Memory      : A
Line T       At      Wide       Size    Indices        Value   Name
   1 V        0         4                                 12   A
""");
   }

  static void test_bit_verilog()
   {z();
    final int N = 8;
    Layout           l = Layout.layout();
    Layout.Variable  a = l.variable("a", N);
    MemoryLayoutDM   m = new MemoryLayoutDM(l.compile(), "a");
    m.program(m.P);
    m.top().setInt(12);

    ok(""+m.   declareVerilog(), "reg[8-1 : 0] a; /* declareMemories_2 */");
    ok(""+m.initializeVerilog(), "a <= 8'b1100;");
   }

  static void test_block_verilog()
   {z();
    final int N = 8;
    Layout           l = Layout.layout();
    Layout.Variable  a = l.variable("a",    N);
    Layout.Array     A = l.array   ("A", a, N);
    MemoryLayoutDM   m = new MemoryLayoutDM(l.compile(), "A");
    m.program(m.P);
    for (int i = 0; i < N/2; i++) m.at(a, i).setInt(i);

    ok(""+m.   declareVerilog(), "reg[8-1 : 0] A[8-1 : 0]; /* declareMemories_1 */");
    //stop(m.initializeVerilog());
    ok(""+m.initializeVerilog(), """
A[0] <= 8'b0;
A[1] <= 8'b1;
A[2] <= 8'b10;
A[3] <= 8'b11;
A[4] <= 8'b0;
A[5] <= 8'b0;
A[6] <= 8'b0;
A[7] <= 8'b0;
""");
   }

  static void test_zero_one_ones()
   {z();
    final int N = 8;
    Layout           l = Layout.layout();
    Layout.Variable  a = l.variable("a",    N);
    MemoryLayoutDM   m = new MemoryLayoutDM(l.compile(), "A");
    m.program(m.P);

    m.at(a).ones();
    ok(m.at(a).getInt(), 0);
    m.P.run(); m.P.clear();
    ok(m.at(a).getInt(), powerTwo(N)-1);

    m.at(a).one();
    ok(m.at(a).getInt(), powerTwo(N)-1);
    m.P.run(); m.P.clear();
    ok(m.at(a).getInt(), 1);

    m.at(a).zero();
    ok(m.at(a).getInt(), 1);
    m.P.run(); m.P.clear();
    ok(m.at(a).getInt(), 0);
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_get_set();
    test_boolean();
    test_copy();
    test_move();
    test_set_inc_dec_get();
    test_addressing();
    test_boolean_result();
    test_is_ones_or_zeros();
    test_union();
    test_verilog_address();
    test_copy_memory();
    test_array_addressing();
    test_upDownMoveBuffer();
    test_upDownMoveBuffer2();
    test_upDownMoveBuffer3();
    test_copyMoveBuffer();
    test_moveUpAll();
    test_moveUpLog();
    test_moveDownAll();
    test_moveDownLog();
    test_top();
    test_top_move();
    test_bit_verilog();
    test_block_verilog();
    test_zero_one_ones();
   }

  static void newTests()                                                        // Tests being worked on
   {//oldTests();
    test_zero_one_ones();
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
