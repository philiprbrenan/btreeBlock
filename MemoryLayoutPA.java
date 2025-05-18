//------------------------------------------------------------------------------
// MemoryLayoutPA with distributed memory
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Memory layout

import java.util.*;

class MemoryLayoutPA extends Test                                               // Memory layout
 {final String          name;                                                   // Name of the memory layout
  final MemoryLayoutPA based;                                                   // If true, then we are using some one else's memory with a base offset into it, otherwise if false we are the owner of the memory and the base offset is always zero
  final Layout        layout;                                                   // Layout of part of memory
  private Memory      memory;                                                   // Memory containing layout
  private int           base;                                                   // Base of layout in memory - like located in Pl1
  boolean              debug;                                                   // Debug if true
  ProgramPA                P = new ProgramPA();                                 // Program containing generated code
  static int         numbers = 0;
  final  int          number = ++numbers;                                       // Number each memory layout

//D1 Construction                                                               // Construct a memory layout

  MemoryLayoutPA(int size)                                                      // Memory useful for running tests.
   {z();
    name   = null; based = null; layout = null;
    memory = new Memory("", size);
   }

  MemoryLayoutPA(Layout Layout, String Name)                                    // Memory with an associated layout and a name so we can generate verilog from it
   {zz();
    name   = Name; based = null; layout = Layout;
    memory = new Memory(Name, layout.size());                                   // Create the associated memory as this memory is not based on any other memory
   }

  MemoryLayoutPA(Layout Layout, String Name, MemoryLayoutPA Based)              // Like based storage in PL1.  If the based memory is null then we create a backing memory of the required size and base ourselves at zero in it to facilitate testing.
   {zz();
    layout = Layout;
    if (Based == null)                                                          // Create the backing memory if necessary, usually for testing purposes
     {based = new MemoryLayoutPA(layout, Name+"_Backing");                      // Create new backing memory
      name  = Name+"_Based";
     }
    else
     {based = Based;                                                            // Base off existing backing memory
      name  = Name+"_Based";
     }
    P     = based.P;
    memory = based.memory();                                                    // Backing memory
   }

//D1 Control                                                                    // Testing, control and integrity

  Memory memory() {zz(); return memory;}                                        // Real memory used by this layout

  void program(ProgramPA program) {zz(); P = program;}                          // Program in which to generate instructions

  void base(int Base)                                                           // Set the base of the layout in memory allowing such layouts to be relocated
   {if (based == null) stop("Memory layout is not based so cannot set a base");
    base = Base;
   }

  String  name    () {z(); return based() ? based.name() : name+"_"+number;}    // Name of this memory layout
  Layout  layout  () {z(); return layout;}                                      // Get the layout in use
  String  baseName() {z(); return name+"_"+number+"_base_offset";}              // Name of the verilog field used to hold the base being used for this memory layout

  String  initializeMemory() {z(); return "initialize_memory_"+name();}         // Name of the verilog field used to hold the base being used for this memory layout

  int     base () {z(); return base;}                                           // Get the base offset into memory being used
  boolean based() {z(); return based  != null;}                                 // Whether the memory used in the memory layout is based or not
  int     size () {z(); return layout != null ? layout.size() : memory.size();} // Size of memory is ideally the size of its layout otherwise if there is no layout, the size of the underlying memory
  int baseSize () {z(); return based() ? based.size() : size();}                // Size of underlying memory

  void clear()                                                                  // Clear underlying memory
   {z();
    if (based == null) {memory.zero(); return;}                                 // Not based so we just clear the memory we have
    final int N = size();
    for (int i = 0; i < N; i++) setBit(i, false);                               // Based so we clear the area occupied by the layout
   }

  void ok(String Lines)                                                         // Check that specified lines are present in the memory layout
   {final String  m = toString();                                               // Memory as string
    final String[]L = Lines.split("\\n");                                       // Lines of expected
    int p = 0;
    for(int i = 0; i < L.length; ++i)                                           // Each expected
     {final String l = L[i];
      final int    q = m.indexOf(l, p);                                         // Check specified lines are present
      if (q == -1)                                                              // Line missing
       {err("Layout does not contain line:", i+1, "\n"+l+"\n");
        ++Layout.testsFailed;
        return;
       }
      p = q;
     }
    ++Layout.testsPassed;                                                       // Lines found
   }

//D1 Copy control                                                               // Verilog variables used to implement a variable length copy

  String  copyIndex() {return "index_"     +baseName();}                        // Index to a location in this memory layout
  String copyLength() {return "copyLength_"+baseName();}                        // Length of a copy in this memory layout
  int      copySize() {return logTwo(baseSize());}                              // Size of bits for a length or index into this memory

  String copyVerilogDec()                                                       // Verilog declaration
   {zz();
    final StringBuilder s = new StringBuilder();                                // Text of declaration
    s.append("reg["+copySize()+"-1: 0] "+copyIndex ()+"; "+traceComment()+"\n");
    s.append("reg["+copySize()+"-1: 0] "+copyLength()+"; "+traceComment()+"\n");
    return ""+s;
   }

//D1 Get and Set                                                                // Get and set values in memory but only during testing

  boolean getBit(int index)                                                     // Get a bit from the memory layout
   {zz();return memory.getBit(base + index);
   }

  void setBit(int index, boolean value)                                         // Set a value in memory occupied by the layout
   {zz(); memory.set(base + index,  value);
   }

  int  getInt(Layout.Field field, int...indices)                                // Get a value from memory occupied by the layout
   {z();
    final int i = new At(field, indices).setOff().result;
    return i;
   }

  void setInt(Layout.Field field, int value, int...indices)                     // Set a value in memory occupied by the layout
   {zz();
    final At a = new At(field, indices).setOff();
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

  void setIntInstruction(Layout.Field field1, int value1,                       // Set values in memory occupied by the layout
                         Layout.Field field2, int value2)                       // Set a value in memory occupied by the layout
   {zz();
    P.new I()
     {void a()
       {final At a1 = new At(field1).setOff(), a2 = new At(field2).setOff();
        memory.set(a1.at, a1.width, value1);
        memory.set(a2.at, a2.width, value2);
       }
      String v()
       {return new At(field1).verilogLoad() + " <= " + value1 + ";"
             + new At(field2).verilogLoad() + " <= " + value2 + ";";
       }
     };
   }

  void zero()                                                                   // Clear the memory associated with the layout to zeros
   {zz();
    memory.set(base, size(), 0);
   }

//  void moveParallel(At...Fields)                                                // Move pairs of fields in parallel. Suceeded by parallel Section.
//   {zz();
//    final int N = Fields.length;
//    if (N % 2 == 1) stop("Move in parallel requires an even number of fields");
//    for(int i = 0; i < N; i += 2) Fields[i].sameSize(Fields[i+1]);
//    P.new I()
//     {void a()
//       {for(int i = 0; i < N; i += 2)
//         {final At target = Fields[i+0].setOff();
//          final At source = Fields[i+1].setOff();
//          for(int j = 0; j < target.width; ++j)
//           {zz();
//            final boolean b = source.getBit(j);
//            target.setBit(j, b);
//           }
//         }
//       }
//      String v()
//       {final StringBuilder s = new StringBuilder();
//        for(int i = 0; i < N; i += 2)
//         {final At target = Fields[i+0];
//          final At source = Fields[i+1];
//          for(int j = 0; j < target.width; ++j)
//           {s.append(target.verilogLoad()+" <= "+source.verilogLoad() + ";");
//           }
//         }
//        return s.toString();
//       }
//      String n()
//       {final StringBuilder s = new StringBuilder();
//        for(int i = 0; i < N; i += 2)
//         {final At target = Fields[i+0];
//          final At source = Fields[i+1];
//          s.append(target.field.name+" = "+source.field.name);
//         }
//        return s.toString();
//       }
//     };
//   }

  void copy(MemoryLayoutPA source)                                              // Copy all the bits from the source into the target as long as the source and target are the same size
   {zz();
    if (size() != source.size()) stop("Memory layouts have different sizes");
    final int N = size();
    P.new I()
     {void a()
       {for(int i = 0; i < N; ++i)
         {final boolean b = source.getBit(i);
          setBit(i, b);
         }
       }
      String v()
       {final String m = name();
         return m+"["+baseName()+" +: "+N+"] <= "+m+"["+source.baseName()+" +: "+N+"]; /* MemoryLayoutPA.copy */";
       }
     };
   }

//D1 Components                                                                 // Locate a variable in memory via its indices

  class At
   {final Layout.Field field;                                                   // Field description in layout
    final int  width;                                                           // Width of element in memory
    final int[]indices;                                                         // Known indices to be applied directly to locate the field in memory
    final At[] directs;                                                         // Fields whose location is known at the start so they can be used for indices into memory rather like registers on a chip
    int  delta;                                                                 // Delta due to indices
    int  at;                                                                    // Location in memory
    int  result;                                                                // The contents of memory at this location

    String verilogLoadAddr(boolean la, Integer delta)                           // A verilog representation of an addressed location in memory
     {final String base = based != null ? baseName()+"+" : "";                  // Base field name if a based memory layout

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
      return (la ? p(base)+i(field.at)+c()+joinStrings(v, "")+d    :            // IBM S/360 Principles of Operation: LA
        name()+"["+p(base)+i(field.at)+c()+joinStrings(v, "")+d+" +: "+w+"]");  // IBM S/360 Principles of Operation: L
     }

    String verilogLoad() {return verilogLoadAddr(false, null);}                 // Content of a memory location as a verilog expression
    String verilogAddr() {return verilogLoadAddr(true,  null);}                 // Address of a memory location
    String i(int i)      {return String.format("%8d", i);}                      // Format an index
    String w(int w)      {return String.format("%1d", w);}                      // Format a width

    String c()                                                                  // Format a field name as a comment
     {final StringBuilder s = new StringBuilder(field.name);
      while(s.length() % 8 > 0) s.append(" ");
      return "/*" + s +"*/";
     }
    String p(String s)   {while(s.length() % 8 > 0) s = s+" "; return s;}       // Pad a string


    void locateDirectAddress()                                                  // Locate a direct address and its content
     {final int N = indices.length;
      delta  = field.locator.at(indices);
      at     = base + delta;
      result = memory.getInt(at, width);
     }

    void locateInDirectAddress()                                                // Locate an indirect address and its content
     {final int N = directs.length;
      for (int i = 0; i < N; i++)
       {indices[i] = directs[i].setOff().getInt();                              // It is assumed by getInt() that setOff() will already have been called.
       }
      locateDirectAddress();                                                    // Locate the address directly now that its indices are known
     }

    At(Layout.Field Field)                                                      // No indices or base
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
      final int N = Directs.length;
      for (int i = 0; i < N; i++)
       {if (Directs[i].directs != null)
         {stop("Index:", i, "must not have a base or indices");
         }
       }

      field = Field; width = field.width;
      directs = Directs;
      indices = new int[N];                                                     // The values obtained from each indirect reference to an index will be placed here for the computation of the actual address of the indexed field
     }

    boolean sameSize(At b)                                                      // Check two fields are the same size
     {if (field == null) return true;                                           // Constants match any size
      z(); field.sameSize(b.field);
      z(); return true;
     }

    boolean hasIndirection() {return directs != null;}                          // Is indirection used in this at reference ?

    int width() {z(); return field.width();}                                    // Width of the field in memory

    At setOff() {z(); return setOff(true);}                                     // Set the base address of the field from its indices confirming that we are inside an executing instruction

    At setOff(boolean checkSetOff)                                              // Set the base address of the field
     {zz();
      if (checkSetOff && !P.running) P.halt("Set off must be inside an instruction");
      if (hasIndirection()) {z(); locateInDirectAddress();}                     // Evaluate indirect indices
      else                  {zz(); locateDirectAddress();}                       // Evaluate direct indices
      return this;
     }

    boolean getBit(int i)            {return memory.getBit(at+i);}              // Get a bit from memory assuming that setOff() has been called to fix the location of the field containing the bit
    void    setBit(int i, boolean b) {memory.set(at+i, b);}                     // Set a bit in memory  assuming that setOff() has been called to fix the location of the field containing the bit

    int  getInt()          {zz(); return result;}                               // The value in memory, at the indicated location, treated as an integer or the value of the constant, assumming setOff has been called to update the variable description
    void setInt(int value) {zz(); memory.set(at, width, value);}                // Set the value in memory at the indicated location, treated as an integer

    public String toString()                                                    // Print field name(indices)=value or name=value if there are no indices
     {final StringBuilder s = new StringBuilder();
      s.append(field.name);
      if (indices.length > 0)
       {setOff(false);
        s.append("[");
        for (int i = 0, N = indices.length; i < N; i++)
         {s.append(indices[i]);
          if (i < N-1) s.append(",");
         }
        s.append("]");
       }
      s.append("("+base+"+"+delta+")"+at+"="+result);
      return s.toString();
     }

    MemoryLayoutPA ml() {return MemoryLayoutPA.this;}                           // Containing memory layout

//D2 Move                                                                       // Copy data between memory locations

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
       {void a()
         {source.setOff();
          target.setOff();
          for(int i = 0; i < width; ++i)
           {zz();
            final boolean b = source.getBit(i);
            target.setBit(i, b);
           }
         }
        String v()
         {return target.verilogLoad()+" <= "+source.verilogLoad() + "/* MemoryLayoutPA.move */;";
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
           {return Targets[i].verilogLoad()+" <= "+source.verilogLoad() + "/* MemoryLayoutPA.moveTo */;";
           }
          return s.toString();
         }
        String n() {return field.name+"="+source.field.name;}
       };
     }

    void move(At source, At buffer)                                             // Copy the specified number of bits from source to target via a buffer to allow the operation to proceed in bit parallel assuming the buffer does not overlap either the source or target, each of which can be in different memories.
     {z(); sameSize(source); sameSize(buffer);
      buffer.move(source);
      move(buffer);
     }

    void moveUp(At Index, At buffer)                                            // Move the elements of an array up one position deleting the last element.  A buffer of the same size is used to permit copy in parallel.
     {zz(); sameSize(buffer);
      if (!(field instanceof Layout.Array))  stop("Array required for moveUp");
      final At target = this;
      buffer.move(target);                                                      // Make a copy of the thing to be moved so we can move in parallel
      final Layout.Array A = field.toArray();                                   // Array of elements tp be moved
      final Layout.Array B = buffer.field.toArray();                            // Buffer containg a copy of the array to be moved
      final Layout.Field a = A.element;                                         // Array element
      final Layout.Field b = B.element;                                         // Buffer array element
      final int          w = a.width;                                           // Width of array element
      P.new I()
       {void a()                                                                // Emulation
         {final int S = Index == null ? 1 : (Index.setOff().result + 1);        // A null index means start at the very beginning
          for   (int i = S; i < A.size; i++)                                    // Each element
           {for (int j = 0; j < w;      j++)                                    // Each bit in each element
             {final boolean b = buffer.getBit((i-1)*w + j);
              target.setBit(i*w+j, b);
             }
           }
         }
        String v()                                                              // Verilog
         {final StringBuilder   s = new StringBuilder("/* MemoryLayoutPA.moveUp */\n");
          final String      start = Index == null ? "0" : Index.verilogLoad();  // Load above this index
          final MemoryLayoutPA tm = ml();                                       // Target memory
          final MemoryLayoutPA sm = buffer.ml();                                // Source memory
          for   (int i = 1; i < A.size; i++)                                    // Each element
           {s.append("\nif ("+i+" > "+start +") begin\n  ");                    // Start moving when we are above the index
            s.append
             (tm.at(a, i-0).verilogLoad()+ " <= " +
              sm.at(b, i-1).verilogLoad()+ ";");
            s.append("\nend\n");
           }
          return s.toString();
         }
        String n() {return field.name+" moveUp @ "+(Index != null ? Index.field.name : "0")+" using "+buffer.field.name;}
       };
     }

    void moveDown(At Index, At buffer)                                          // Move the elements of an array down one position deleting the indexed element.  A buffer of the same size is used to permit copy in parallel.
     {zz(); sameSize(buffer);
      if (!(field instanceof Layout.Array)) stop("Array required for moveDown");
      final At target = this;
      buffer.move(target);                                                      // Make a copy of the thing to be moved so we can move in parallel
      final Layout.Array A = field.toArray();                                   // Array of elements tp be moved
      final Layout.Array B = buffer.field.toArray();                            // Buffer containg a copy of the array to be moved
      final Layout.Field a = A.element;                                         // Array element
      final Layout.Field b = B.element;                                         // Buffer array element
      final int          w = a.width;                                           // Width of array element
      P.new I()
       {void a()
         {final int S = Index == null ? 0 : Index.setOff().result;
          for   (int i = S; i < A.size-1; i++)                                  // Each element
           {//sa(target.at+i*w, buffer.at+i*w+w);
            for (int j = 0;            j < w;        j++)                       // Each bit in each element
             {final boolean b = buffer.getBit((i+1)*w + j);
              target.setBit(i*w+j, b);
             }
           }
         }
        String v()                                                              // Verilog
         {final StringBuilder   s = new StringBuilder("/* MemoryLayoutPA.moveDown */\n");
          final String      start = Index == null ? "0" : Index.verilogLoad();  // Load above this index
          final MemoryLayoutPA tm = ml();                                       // Target memory
          final MemoryLayoutPA sm = buffer.ml();                                // Source memory
          for   (int i = 0; i < A.size-1; i++)                                  // Each element
//         {s.append("\nif ("+i+" > "+start +") begin\n  ");                    // Start moving when we are above the index
           {s.append("\nif ("+i+" >= "+start +") begin\n  ");                   // Start moving when we are at the index
            s.append
             (tm.at(a, i-0).verilogLoad()+ " <= " +
              sm.at(b, i+1).verilogLoad()+ ";");               // Trace in situ
            //s.append                                                            // Debug address of move
            // ("$display(\"AAAA %d <= %d = %d\", ("+
            //      tm.at(a, i-0).verilogAddr()+"), ("+
            //      sm.at(b, i+1).verilogAddr()+"), ("+
            //      sm.at(b, i+1).verilogLoad()+"));");
            s.append("\nend\n");
           }
          return s.toString();
         }
        String n() {return field.name+" moveDown @ "+(Index != null ? Index.field.name : "0")+" using "+buffer.field.name;}
       };
     }

    void copy(At Source, At Length)                                             // Copy the specified number of bits from the location addressed by the source to the location addressed by the target.
     {zz();
      final At Target = this;
      P.new I()
       {void a()
         {Target.setOff();
          Source.setOff();
          Length.setOff();
          final int L = Length.result, S = Source.at, T = at;                   // Address and length of move
          for   (int i = 0; i < L; i++)                                         // Each element
           {final boolean b = Source.ml().memory.getBit(S+i);
            Target.ml().memory.set(T+i, b);
           }
         }
        String v()                                                              // Logarithmic move
         {final StringBuilder v = new StringBuilder("/* MemoryLayoutPA.copy */\n");

          final int    N = Length.width;                                        // Log2 of the largest possible copy
          final String l = Target.ml().copyLength(),                            // Length of move in bits
                       s = Source.ml().copyIndex(),                             // Source of move in backing memory
                       t = Target.ml().copyIndex(),                             // Target of move in backing memory
                       m = Target.ml().name();                                  // Name of backing memory

          v.append(l + " = " + Length.verilogLoad() + ";\n");
          v.append(s + " = " + Source.verilogAddr() + ";\n");
          v.append(t + " = " + Target.verilogAddr() + ";\n");

          for (int i = N+1; i > 0; --i)
           {final int u = 1<<(i-1);
            v.append("if (" +l+" >= "+u+") begin /* MemoryLayoutPA.copy */\n");
            v.append("   "+m+"["+t+" +: "+u+"] <= "+m+"["+s+" +: "+u+"];\n");
            v.append("   "  +l+" = "+l+" - "+u+";\n");                          // These assigns have to be made immediately else each block has to be executed one after another to drive the length and pointers sequentially.
            v.append("   "  +s+" = "+s+" + "+u+";\n");
            v.append("   "  +t+" = "+t+" + "+u+";\n");
            v.append("end\n");
           }
          return v.toString();
         }
       };
     }

    void copy(At Source, At Length, At TargetA, At SourceA, At LengthL)         // Copy the specified number of bits from the location addressed by the source to the location addressed by the target usign the specified memory locations to hold the source and target locations and the remaining length
     {zz();
      final At  Target = this;
      final int size   = Source.ml().size();
      P.new I()                                                                 // Initialize the indexes and length to describe the copy
       {void a()
         {Target.setOff();
          Source.setOff();
          Length.setOff();
          SourceA.setInt(Source.at);                                            // Source address
          TargetA.setInt(Target.at);                                            // Target address
          LengthL.setInt(Length.getInt());                                      // Length of move
         }
        String v()                                                              // Logarithmic move
         {final StringBuilder v = new StringBuilder("/* MemoryLayoutPA.copy start */\n");
          final String l = Length.verilogLoad(), L = LengthL.verilogLoad(),
                       s = Source.verilogAddr(), S = SourceA.verilogLoad(),
                       t = Target.verilogAddr(), T = TargetA.verilogLoad();

          v.append(L + " <= " + l + ";\n");
          v.append(S + " <= " + s + ";\n");
          v.append(T + " <= " + t + ";\n");
          return v.toString();
         }
       };

      for(int i = nextPowerOfTwo(size); i > 0; i = i >> 1)                      // Copy in logarithmically descending blocks
       {final int I = i;
        P.new I()
         {void a()
           {final int
              l = LengthL.setOff().getInt(),
              s = SourceA   .setOff().getInt(),
              t = TargetA   .setOff().getInt();
            final Memory sm = Source.ml().memory();
            final Memory tm = Target.ml().memory();
            if (l >= I)
             {for (int j = 0; j < I; ++j)                                       // Each element
               {final boolean b = sm.getBit(s + j);
                tm.set(t+j, b);
               }
              LengthL.setInt(l - I);
              SourceA.setInt(s + I);
              TargetA.setInt(t + I);
             }
           }
          String v()                                                            // Logarithmic move
           {final StringBuilder v = new StringBuilder("/* MemoryLayoutPA.copy "+I+" */\n");
            final String l = LengthL.verilogLoad(),
                         s = SourceA.verilogLoad(),
                         t = TargetA.verilogLoad(),
                         S = Source.ml().name(),
                         T = Target.ml().name();

            v.append("if (" +l+" >= "+I+") begin\n");
            v.append("   "  +T+"["+t+" +: "  +I+"] <= "+S+"["+s+" +: "+I+"];\n");
            v.append("   "  +l+" <= "+l+" - "+I+";\n");                         // These assigns have to be made immediately else each block has to be executed one after another to drive the length and pointers sequentially.
            v.append("   "  +s+" <= "+s+" + "+I+";\n");
            v.append("   "  +t+" <= "+t+" + "+I+";\n");
            v.append("end\n");
            return v.toString();
           }
         };
       }
     }

//D2 Bits                                                                       // Bit operations in a memory.

    void zero()                                                                 // Zero some memory
     {zz();
      P.new I()
       {void a() {setOff(); memory.zero(at, width);}
        String v()
         {return verilogLoad()+" <= 0;";
         }
        String n()
         {return field.name+" <= 0";
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
         {return verilogLoad()+" <= "+one+ ";";
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
         {return verilogLoad()+" <= ~"+verilogLoad()+ ";";
         }
        String n()
         {return field.name+" <= ~"+field.name;
         }
       };
     }

    boolean isAllZero()                                                         // Check that the specified memory is all zeros
     {z(); setOff(); return memory.isAllZero(at, width);
     }

    boolean isAllOnes()                                                         // Check that  the specified memory is all ones
     {z(); setOff(); return memory.isAllOnes(at, width);
     }

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
         {return result.verilogLoad()+" <= "+target.verilogLoad()+" == 0;";
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
            " == " + target.field.verilogOnes()+ ";";
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

    void equal(At b, At result)                                                // Whether  a == b
     {zz();
      final At a = this;
      P.new I()
       {void a()
         {result.setOff().setInt(equal(b.setOff()) ? 1 : 0);
         }
        String v()
         {return result.verilogLoad()+" <= "+a.verilogLoad()+
                                       " == " + b.verilogLoad()+ ";";
         }
        String n() {return result.field.name+"="+field.name+" == "+b.field.name;}
       };
     }

    void    notEqual(At b, At result)                                           // Whether  a != b
     {z();
      final At a = this;
      P.new I()
       {void a()
         {result.setOff().setInt(!equal(b.setOff()) ? 1 : 0);
         }
        String v()
         {return result.verilogLoad()+" <= "+a.verilogLoad()+
                                         " != "+b.verilogLoad()+ ";";
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
         {return result.verilogLoad()+" <= "+a.verilogLoad()+
                                          " < "+b.verilogLoad()+ ";";
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
         {return result.verilogLoad()+" <= "+a.verilogLoad()+
                                         " <= "+b.verilogLoad()+ ";";
         }
        String n() {return result.field.name+"="+field.name+"<="+b.field.name;}
      };
     }

    void    greaterThan(At b, At result)                                        // Whether  a > b
     {zz(); sameSize(b);
      final At a = this;
      P.new I()
       {void a()
         {result.setOff().setInt(!lessThan(b.setOff()) && !equal(b) ? 1 : 0);
         }
        String v()
         {return result.verilogLoad()+" <= "+a.verilogLoad()+
                                          " > "+b.verilogLoad()+ ";";
         }
        String n() {return result.field.name+"="+field.name+">"+b.field.name;}
       };
     }

    void    greaterThanOrEqual(At b, At result)                                 // Whether  a >= b
     {zz(); sameSize(b);
      final At a = this;
      P.new I()
       {void a()
         {result.setOff().setInt(!lessThan(b.setOff()) ? 1 : 0);
         }
        String v()
         {return result.verilogLoad()+" <= "+a.verilogLoad()+
                                         " >= "+b.verilogLoad()+ ";";
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
         {return a.verilogLoad()+" <= "+a.verilogLoad()+"+ 1;";
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
         {return a.verilogLoad()+" <= "+a.verilogLoad()+"- 1;";
         }
        String n()
         {return "--"+field.name;
         }
       };
     }
    void dec(int n)                                                             // Decrement a variable treated as an unsigned binary integer by a constant amount with wrap around on overflow.  Return the result after  the increment.
     {zz();
      final At a = this;
      P.new I()
       {void a()
         {setOff();
          final int i = getInt()-n;
          setInt(i);
         }
        String v()
         {return a.verilogLoad()+" <= "+a.verilogLoad()+"- "+n+";";
         }
        String n()
         {return field.name + "-= " + n;
         }
       };
     }

    void add(At source, int constant)                                           // Add to the source and store in the target
     {zz(); sameSize(source);
      final At target = this;
      P.new I()
       {void a()
         {final int v = source.setOff().getInt();
          target.setOff().setInt(v + constant);
         }
        String v()
         {return target.verilogLoad()+" <= "+source.verilogLoad() + "+" + constant + ";";
         }
        String n() {return field.name+"="+source.field.name + "+ "+ constant;}
        void   i() {}
       };
     }
   } // At

  At at(Layout.Field Field)                                                     // A field without indices or base addressing
   {zz(); return new    At(Field);
   }

  At at(Layout.Field Field, int...Indices)                                      // A field with constant indices
   {zz(); return new    At(Field,       Indices);
   }

  At at(Layout.Field Field, At...Indices)                                       // A field with  variable indices. Each index being a field with no indices
   {zz(); return new    At(Field,      Indices);
   }

//D1 Print                                                                      // Print a memory layout

  class PrintPosition                                                           // Position in print
   {final StringBuilder s = new StringBuilder();
    int line = 1;
    int bits = base;
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

//D1 Verilog                                                                    // Transfer memory to Verilog

  void dumpVerilog(String file, Layout.Variable...ignores)                      // Initialize memory in Verilog with the contents of this memory ignoring some fields that will be loaded from input ports
   {zz();
    final TreeSet<Integer> ignore = new TreeSet<>();                            // The bits to be ignored
    for (Layout.Variable v : ignores)                                           // Load the bits to be ignored
     {final int N = v.width;
      for (int i = 0; i < N; i++) ignore.add(v.at+i);                           // Bit to be ignored
     }

    final StringBuilder s = new StringBuilder();
    final int N = memory.size()-1, B = logTwo(N)-1;                             // Dimensions of memory
    final String m = name();                                                    // Name of memory
    s.append(declareVerilog());
    s.append("task "+initializeMemory()+";\n");
    s.append("    begin\n");
    for(int i = 0; i <= N; ++i)                                                 // Load each bit
     {final int b = memory.getBit(i) ? 1 : 0;                                     // Bit to dump
      final String l = "        "+m+"["+i+"] <= "+b+"; /* dumpVerilog */\n";    // Load bit into memory
      if (ignore.contains(i))                                                   // Ignore bits loaded from other sources during synthesis
       {s.append("     `ifndef SYNTHESIS\n");                                   // Load bit into memory
        s.append(l);
        s.append("     `endif\n");
       }
      else                                                                      // Load all bits as not in synthesis
       {s.append("        "+m+"["+i+"] <= "+b+";\n");                           // Load bit into memory
       }
     }
    s.append("    end\n");
    s.append("endtask\n");
    writeFile(file, s);                                                         // Write the definition into an include file
   }

  String declareVerilog()                                                       // Declare matching memory  but do not initialize it
   {zz();
    final int N = memory.size()-1, B = logTwo(N);
    final StringBuilder s = new StringBuilder();
    if (based == null) s.append("reg ["+N+":0] "+name()    +"; ");              // Actual memory if it is not based
    else               s.append("reg ["+B+":0] "+baseName()+"; ");              // Base offset for this memory
    s.append(traceComment());
    s.append("\n");
    return s.toString();
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
    MemoryLayoutPA   M;
    TestMemoryLayout()
     {z();
      M = new MemoryLayoutPA(l.compile(), "test");
      M.memory.alternating(4);
     }
   }

  static void test_get_set()
   {z();
    TestMemoryLayout   t = new TestMemoryLayout();
        MemoryLayoutPA m = t.M;
        ProgramPA      p = m.P;
              Layout l = m.layout;
                      ok(m.at    (t.c,      0, 0, 0), "c[0,0,0](0+12)12=15");
    p.new I() {void a() {m.setInt(t.c,  11, 0, 0, 0); }}; p.run(); p.clear();
                      ok(m.at    (t.c,      0, 0, 0), "c[0,0,0](0+12)12=11");

                      ok(m.at( t.c,      0, 0, 1), "c[0,0,1](0+24)24=0");
    p.new I() {void a() {m.setInt(t.c,  11, 0, 0, 1); }}; p.run(); p.clear();
                      ok(m.at( t.c,      0, 0, 1), "c[0,0,1](0+24)24=11");

                      ok(m.at    (t.a,     0, 2, 2), "a[0,2,2](0+116)116=15");
    p.new I() {void a() {m.setInt(t.a,  5, 0, 2, 2); }}; p.run(); p.clear();
                      ok(m.at    (t.a,     0, 2, 2), "a[0,2,2](0+116)116=5");

    ok(m.at( t.b,      1, 2, 2), "b[1,2,2](0+252)252=15");
    p.new I() {void a() {m.setInt(t.b,   7, 1, 2, 2); }}; p.run(); p.clear();
    ok(m.at( t.b,      1, 2, 2), "b[1,2,2](0+252)252=7");

                      ok(m.at    (t.e,     1, 2), "e[1,2](0+260)260=15");
    p.new I() {void a() {m.setInt(t.e, 11, 1, 2);  }}; p.run(); p.clear();
                      ok(m.at    (t.e,     1, 2), "e[1,2](0+260)260=11");
   }

  static void test_boolean()
   {z();
    TestMemoryLayout t = new TestMemoryLayout();
        MemoryLayoutPA m = t.M;
        ProgramPA      p = m.P;
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
        MemoryLayoutPA m = t.M;
        ProgramPA      p = m.P;
    p.new I()
     {void a()
       {m.setInt(t.a, 1, 0, 0, 0);
        m.setInt(t.a, 2, 0, 0, 1);
        m.setInt(t.a, 3, 0, 0, 2);
       }
     };
    m.P.run(); m.P.clear();

    ok(m.at(t.a, 0, 0, 0), "a[0,0,0](0+4)4=1");
    ok(m.at(t.a, 0, 0, 1), "a[0,0,1](0+16)16=2");
    ok(m.at(t.a, 0, 0, 2), "a[0,0,2](0+28)28=3");

    m.at(t.a, 0, 0, 1).move(m.at(t.a, 0, 0, 0));
    m.at(t.a, 0, 0, 1).move(m.at(t.a, 0, 0, 1));
    t.M.P.run();

    ok(m.at(t.a, 0, 0, 0), "a[0,0,0](0+4)4=1");
    ok(m.at(t.a, 0, 0, 1), "a[0,0,1](0+16)16=1");
    ok(m.at(t.a, 0, 0, 2), "a[0,0,2](0+28)28=3");
   }

  static void test_base()
   {z();
    Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 4);
    Layout.Variable  b = l.variable ("b", 4);
    Layout.Variable  c = l.variable ("c", 4);
    Layout.Variable  d = l.variable ("d", 4);
    Layout.Structure s = l.structure("s", a, b, c, d);
    Layout.Array     A = l.array    ("A", s, 2);
    l.compile();
    MemoryLayoutPA   M = new MemoryLayoutPA(l, "test");
    MemoryLayoutPA   m = new MemoryLayoutPA(l, "test", M);
    ProgramPA        p = M.P;
    p.new I() {void a() {m.base(M.at(s, 1).setOff().at);}};
    m.memory.alternating(4);
    //stop(M);
    M.ok("""
MemoryLayout: test
Memory      : test
Line T       At      Wide       Size    Indices        Value   Name
   1 A        0        32          2                           A
   2 S        0        16               0                        s
   3 V        0         4               0                  0       a
   4 V        4         4               0                 15       b
   5 V        8         4               0                  0       c
   6 V       12         4               0                 15       d
   7 S       16        16               1                        s
   8 V       16         4               1                  0       a
   9 V       20         4               1                 15       b
  10 V       24         4               1                  0       c
  11 V       28         4               1                 15       d
""");


    p.new I()
     {void a()
       {m.setInt(a,  9, 0);
        m.setInt(b, 10, 0);
        m.setInt(c, 11, 0);
        m.setInt(d, 12, 0);
       }
     };
    m.P.run(); m.P.clear();

    //stop(M);
    M.ok("""
MemoryLayout: test
Memory      : test
Line T       At      Wide       Size    Indices        Value   Name
   1 A        0        32          2                           A
   2 S        0        16               0                        s
   3 V        0         4               0                  0       a
   4 V        4         4               0                 15       b
   5 V        8         4               0                  0       c
   6 V       12         4               0                 15       d
   7 S       16        16               1                        s
   8 V       16         4               1                  9       a
   9 V       20         4               1                 10       b
  10 V       24         4               1                 11       c
  11 V       28         4               1                 12       d
""");
   }

  static void test_move()
   {z();
    Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 4);
    Layout.Variable  b = l.variable ("b", 4);
    Layout.Variable  c = l.variable ("c", 4);
    Layout.Variable  d = l.variable ("d", 4);
    Layout.Structure s = l.structure("s", a, b, c, d);

    MemoryLayoutPA   m = new MemoryLayoutPA(l.compile(), "test");
    m.memory.alternating(4);
    //stop(m);
    m.ok("""
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        16                                      s
   2 V        0         4                                  0     a
   3 V        4         4                                 15     b
   4 V        8         4                                  0     c
   5 V       12         4                                 15     d
""");
    m.at(d).move(m.at(a),  m.at(b));
    m.P.run();

    //stop(m);
    m.ok("""
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        16                                      s
   2 V        0         4                                  0     a
   3 V        4         4                                  0     b
   4 V        8         4                                  0     c
   5 V       12         4                                  0     d
""");
   }

  static void test_move_to()
   {z();
    Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 4);
    Layout.Variable  b = l.variable ("b", 4);
    Layout.Variable  c = l.variable ("c", 4);
    Layout.Variable  d = l.variable ("d", 4);
    Layout.Variable  e = l.variable ("e", 4);
    Layout.Structure s = l.structure("s", a, b, c, d, e);

    MemoryLayoutPA   m = new MemoryLayoutPA(l.compile(), "test");
    m.setIntInstruction(a, 13);
    m.at(a).moveTo(m.at(b), m.at(d));
    m.P.run();

    //stop(m);
    m.ok("""
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

    m.P.clear();
    m.setIntInstruction(a, 13, b, 12);
    m.setIntInstruction(c, 11, d, 10);
    m.P.run();

    //stop(m);
    m.ok("""
MemoryLayout: test
Memory      : test
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        20                                      s
   2 V        0         4                                 13     a
   3 V        4         4                                 12     b
   4 V        8         4                                 11     c
   5 V       12         4                                 10     d
   6 V       16         4                                  0     e
""");
   }

/* static void test_move_parallel()
   {z();
    Layout           l = Layout.layout();
    Layout.Variable  a = l.variable("a", 4);
    Layout.Variable  b = l.variable("b", 4);
    Layout.Variable  c = l.variable("c", 4);
    Layout.Variable  d = l.variable("d", 4);
    Layout.Variable  e = l.variable("e", 4);
    Layout.Variable  f = l.variable("f", 4);
    Layout.Structure s = l.structure("s", a, b, c, d, e, f);

    MemoryLayoutPA   m = new MemoryLayoutPA(l.compile(), "test");
    m.setIntInstruction(a, 14);
    m.setIntInstruction(c, 13);
    m.setIntInstruction(e, 12);
    m.moveParallel(m.at(b), m.at(a), m.at(d), m.at(c), m.at(f), m.at(e));
    m.P.run();

    //stop(m);
    m.ok("""
MemoryLayout: test
Memory      : test
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        24                                      s
   2 V        0         4                                 14     a
   3 V        4         4                                 14     b
   4 V        8         4                                 13     c
   5 V       12         4                                 13     d
   6 V       16         4                                 12     e
   7 V       20         4                                 12     f
""");
   } */

  static void test_set_inc_dec_get()
   {z();
    Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 4);
    Layout.Variable  b = l.variable ("b", 4);
    Layout.Structure s = l.structure("s", a, b);
    MemoryLayoutPA   m = new MemoryLayoutPA(l.compile(), "test");

    m.at(a).setInt(1);
    m.at(b).setInt(3);
    //stop(m);
    m.ok("""
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
    m.ok("""
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

    m.P.clear();
    m.at(a).dec(3);
    m.at(b).dec(4);
    m.P.run();
    //stop(m);

    m.ok("""
MemoryLayout: test
Memory      : test
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0         8                                      s
   2 V        0         4                                 14     a
   3 V        4         4                                 15     b
""");
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
    MemoryLayoutPA   m = new MemoryLayoutPA(l.compile(), "test");
    ProgramPA        p = m.P;

    MemoryLayoutPA.At  Z = m.at(z), I = m.at(i), J = m.at(j);

    p.new I()
     {void a()
       {z();
        Z.setOff().setInt(0);
        I.setOff().setInt(1);
        J.setOff().setInt(2);
       }
     };
    m.P.run(); m.P.clear();

    MemoryLayoutPA.At az = m.at(a, Z);
    MemoryLayoutPA.At ai = m.at(a, I);
    MemoryLayoutPA.At aj = m.at(a, J);

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

  static void test_zero()
   {z();
    Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 4);
    Layout.Array     A = l.array    ("A", a, 6);
    l.compile();

    MemoryLayoutPA   M = new MemoryLayoutPA(l.compile(), "test");
    MemoryLayoutPA   m = new MemoryLayoutPA(l.compile(), "over", M);
    m.base(12);

    m.memory.alternating(4);
    //stop(M.memory);
    ok(M.memory, """
Memory: test
      4... 4... 4... 4... 3... 3... 3... 3... 2... 2... 2... 2... 1... 1... 1... 1...
Line  FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210
   0  0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 00f0 f0f0
""");

    //stop(M);
    ok(M, """
MemoryLayout: test
Memory      : test
Line T       At      Wide       Size    Indices        Value   Name
   1 A        0        24          6                           A
   2 V        0         4               0                  0     a
   3 V        4         4               1                 15     a
   4 V        8         4               2                  0     a
   5 V       12         4               3                 15     a
   6 V       16         4               4                  0     a
   7 V       20         4               5                 15     a
""");

    M.zero();

    //stop(M.memory);
    ok(M.memory, """
Memory: test
      4... 4... 4... 4... 3... 3... 3... 3... 2... 2... 2... 2... 1... 1... 1... 1...
Line  FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210
   0  0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000
""");

    //stop(M);
    ok(M, """
MemoryLayout: test
Memory      : test
Line T       At      Wide       Size    Indices        Value   Name
   1 A        0        24          6                           A
   2 V        0         4               0                  0     a
   3 V        4         4               1                  0     a
   4 V        8         4               2                  0     a
   5 V       12         4               3                  0     a
   6 V       16         4               4                  0     a
   7 V       20         4               5                  0     a
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
    MemoryLayoutPA   m = new MemoryLayoutPA(l.compile(), "test");

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
    MemoryLayoutPA   m = new MemoryLayoutPA(l.compile(), "test");

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
    MemoryLayoutPA   m = new MemoryLayoutPA(l.compile(), "test");

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

    MemoryLayoutPA   m = new MemoryLayoutPA(l.compile(), "M");

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

    MemoryLayoutPA.At at = m.at(a, m.at(I));
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

  static void test_dump_verilog()
   {z();
    final String v = "verilog/memoryLayoutPA/dump_verilog.txt";

    Layout               l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 2);
    Layout.Array     A = l.array    ("A", a, 4);

    MemoryLayoutPA   m = new MemoryLayoutPA(l.compile(), "M");
    final String     n = m.name();
    final String     i = m.initializeMemory();
    m.memory().alternating(4);
    m.dumpVerilog(v);

    final Stack<String> r = readFile(v);
    r.removeElementAt(0);
    ok(joinLines(r)+"\n", String.format("""
task %s;
    begin
        %s[0] <= 0;
        %s[1] <= 0;
        %s[2] <= 0;
        %s[3] <= 0;
        %s[4] <= 1;
        %s[5] <= 1;
        %s[6] <= 1;
        %s[7] <= 1;
    end
endtask
""", i, n, n, n, n, n, n, n, n));
    deleteAllFiles(folderName(v), 1);
   }

  static void test_dump_verilog_ignore()
   {z();
    final String v = "verilog/memoryLayoutPA/dump_verilog.txt";

    Layout               l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 1);
    Layout.Variable  b = l.variable ("b", 2);
    Layout.Variable  c = l.variable ("c", 3);
    Layout.Variable  d = l.variable ("d", 4);
    Layout.Variable  e = l.variable ("e", 5);
    Layout.Structure S = l.structure("S", a, b, c, d, e);

    MemoryLayoutPA   m = new MemoryLayoutPA(l.compile(), "M");
    final String     n = m.name();
    final String     i = m.initializeMemory();
    m.memory().alternating(4);
    m.dumpVerilog(v, b, d);

    final Stack<String> r = readFile(v);
    r.removeElementAt(0);
    //stop(joinLines(r));
    ok(joinLines(r)+"\n", String.format("""
task %s;
    begin
        %s[0] <= 0;
     `ifndef SYNTHESIS
        %s[1] <= 0; /* dumpVerilog */
     `endif
     `ifndef SYNTHESIS
        %s[2] <= 0; /* dumpVerilog */
     `endif
        %s[3] <= 0;
        %s[4] <= 1;
        %s[5] <= 1;
     `ifndef SYNTHESIS
        %s[6] <= 1; /* dumpVerilog */
     `endif
     `ifndef SYNTHESIS
        %s[7] <= 1; /* dumpVerilog */
     `endif
     `ifndef SYNTHESIS
        %s[8] <= 0; /* dumpVerilog */
     `endif
     `ifndef SYNTHESIS
        %s[9] <= 0; /* dumpVerilog */
     `endif
        %s[10] <= 0;
        %s[11] <= 0;
        %s[12] <= 1;
        %s[13] <= 1;
        %s[14] <= 1;
    end
endtask
""", i, n, n, n, n, n, n, n, n, n, n, n, n, n, n, n));
    deleteAllFiles(folderName(v), 1);
   }

  static void test_copy_bits()
   {z();
    Layout               l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 2);
    Layout.Array     A = l.array    ("A", a, 4);
    Layout.Variable  b = l.variable ("b", 4);
    Layout.Array     B = l.array    ("B", b, 4);
    Layout.Variable  i = l.variable ("i", 4);
    Layout.Variable  j = l.variable ("j", 4);
    Layout.Variable  L = l.variable ("l", 4);
    Layout.Structure S = l.structure("S", A, B, i, j, L);
    l.compile();

    MemoryLayoutPA   m = new MemoryLayoutPA(l, "M");
    for(int I = 0; I < 4; ++I) m.at(a, I).setInt(I);
    m.at(i).setInt(2);
    m.at(j).setInt(1);
    m.at(L).setInt(4);
    m.at(b, m.at(i)).copy(m.at(a, m.at(j)), m.at(L));
    m.P.run();
    //stop(m);
    ok(m, """
MemoryLayout: M
Memory      : M
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        36                                      S
   2 A        0         8          4                             A
   3 V        0         2               0                  0       a
   4 V        2         2               1                  1       a
   5 V        4         2               2                  2       a
   6 V        6         2               3                  3       a
   7 A        8        16          4                             B
   8 V        8         4               0                  0       b
   9 V       12         4               1                  0       b
  10 V       16         4               2                  9       b
  11 V       20         4               3                  0       b
  12 V       24         4                                  2     i
  13 V       28         4                                  1     j
  14 V       32         4                                  4     l
""");
   }

  static void test_copy_memory()
   {z();
    final int N = 8;
    Layout               l = Layout.layout();
    Layout.Variable  a = l.variable ("a", N);
    Layout.Variable  b = l.variable ("b", N);
    Layout.Structure s = l.structure("s", a, b);
    l.compile();

    Layout               L = Layout.layout();
    Layout.Variable  A = L.variable ("A", N);
    L.compile();

    MemoryLayoutPA m = new MemoryLayoutPA(l, "M");                              // Original memory

    MemoryLayoutPA x = new MemoryLayoutPA(L, "first",  m);                      // Overlay at "a"
    MemoryLayoutPA o = new MemoryLayoutPA(L, "second", m);                      // Overlay at "b"

    o.base(b.at);

    m.at(a).setInt(3);
    o.copy(x);
    m.P.run(); m.P.clear();
    //stop(m);
    ok(m, """
MemoryLayout: M
Memory      : M
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        16                                      s
   2 V        0         8                                  3     a
   3 V        8         8                                  3     b
""");

    o.clear();
    m.P.run(); m.P.clear();
    //stop(m);
    ok(m, """
MemoryLayout: M
Memory      : M
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        16                                      s
   2 V        0         8                                  3     a
   3 V        8         8                                  0     b
""");
   }

  static void test_copyVerilogDec()
   {z();
    Layout               l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 8);
    Layout.Variable  b = l.variable ("b", 8);
    Layout.Structure s = l.structure("s", a, b);
    l.compile();

    MemoryLayoutPA m = new MemoryLayoutPA(l, "M");
    MemoryLayoutPA n = new MemoryLayoutPA(l, "N",  m);

    final String M = m.copyVerilogDec();
    final String N = n.copyVerilogDec();

    ok(M.contains("reg[4-1: 0] index_M"));
    ok(M.contains("reg[4-1: 0] copyLength_M"));

    ok(N.contains("reg[4-1: 0] index_N"));
    ok(N.contains("reg[4-1: 0] copyLength_N"));
   }

  static void test_copy_variable()
   {z();
    final int N = 4;
    Layout               l = Layout.layout();
    Layout.Variable  a = l.variable ("a", N);
    Layout.Array     A = l.array    ("A", a, N);
    Layout.Variable  b = l.variable ("b", N);
    Layout.Array     B = l.array    ("B", b, N);
    Layout.Variable  p = l.variable ("p", N);
    Layout.Variable  q = l.variable ("q", N);
    Layout.Variable  L = l.variable ("L", N);
    Layout.Structure s = l.structure("s", A, B, p, q, L);
    l.compile();

    MemoryLayoutPA m = new MemoryLayoutPA(l, "m");
    MemoryLayoutPA S = new MemoryLayoutPA(l, "S", m);
    MemoryLayoutPA T = new MemoryLayoutPA(l, "T", m);

    m.at(a, 0).setInt(1);
    m.at(a, 1).setInt(2);
    m.at(a, 2).setInt(3);
    m.at(a, 3).setInt(4);
    m.at(p)   .setInt(1);
    m.at(q)   .setInt(2);
    m.at(L)   .setInt(2 * a.width);
    T.at(b, T.at(q)).copy(S.at(a, S.at(p)), T.at(L));
    m.P.run();
    //stop(m);
    ok(m, """
MemoryLayout: m
Memory      : m
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        44                                      s
   2 A        0        16          4                             A
   3 V        0         4               0                  1       a
   4 V        4         4               1                  2       a
   5 V        8         4               2                  3       a
   6 V       12         4               3                  4       a
   7 A       16        16          4                             B
   8 V       16         4               0                  0       b
   9 V       20         4               1                  0       b
  10 V       24         4               2                  2       b
  11 V       28         4               3                  3       b
  12 V       32         4                                  1     p
  13 V       36         4                                  2     q
  14 V       40         4                                  8     L
""");
   }

  static void test_copy_variable2()
   {z();
    final int N = 8;
    Layout               y = Layout.layout();
    Layout.Variable  a = y.variable ("a", N);
    Layout.Array     A = y.array    ("A", a, N);
    Layout.Variable  b = y.variable ("b", N);
    Layout.Array     B = y.array    ("B", b, N);
    Layout.Variable  p = y.variable ("p", N);
    Layout.Variable  q = y.variable ("q", N);
    Layout.Variable  l = y.variable ("l", N);
    Layout.Variable  P = y.variable ("P", N);
    Layout.Variable  Q = y.variable ("Q", N);
    Layout.Variable  L = y.variable ("L", N);
    Layout.Structure s = y.structure("s", A, B, p, q, l, P, Q, L);
    y.compile();

    MemoryLayoutPA m = new MemoryLayoutPA(y, "m");

    for (int i = 0; i < N; i++) m.at(a, i).setInt(i+1);
    m.at(p)   .setInt(1);
    m.at(q)   .setInt(2);
    m.at(l)   .setInt(2 * N);
    m.P.run(); m.P.clear();
    //stop(m);
    ok(m, """
MemoryLayout: m
Memory      : m
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0       176                                      s
   2 A        0        64          8                             A
   3 V        0         8               0                  1       a
   4 V        8         8               1                  2       a
   5 V       16         8               2                  3       a
   6 V       24         8               3                  4       a
   7 V       32         8               4                  5       a
   8 V       40         8               5                  6       a
   9 V       48         8               6                  7       a
  10 V       56         8               7                  8       a
  11 A       64        64          8                             B
  12 V       64         8               0                  0       b
  13 V       72         8               1                  0       b
  14 V       80         8               2                  0       b
  15 V       88         8               3                  0       b
  16 V       96         8               4                  0       b
  17 V      104         8               5                  0       b
  18 V      112         8               6                  0       b
  19 V      120         8               7                  0       b
  20 V      128         8                                  1     p
  21 V      136         8                                  2     q
  22 V      144         8                                 16     l
  23 V      152         8                                  0     P
  24 V      160         8                                  0     Q
  25 V      168         8                                  0     L
""");
    m.at(b, m.at(q)).copy(m.at(a, m.at(p)), m.at(l), m.at(P), m.at(Q), m.at(L));
    m.P.run();
    //stop(m);
    ok(m, """
MemoryLayout: m
Memory      : m
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0       176                                      s
   2 A        0        64          8                             A
   3 V        0         8               0                  1       a
   4 V        8         8               1                  2       a
   5 V       16         8               2                  3       a
   6 V       24         8               3                  4       a
   7 V       32         8               4                  5       a
   8 V       40         8               5                  6       a
   9 V       48         8               6                  7       a
  10 V       56         8               7                  8       a
  11 A       64        64          8                             B
  12 V       64         8               0                  0       b
  13 V       72         8               1                  0       b
  14 V       80         8               2                  2       b
  15 V       88         8               3                  3       b
  16 V       96         8               4                  0       b
  17 V      104         8               5                  0       b
  18 V      112         8               6                  0       b
  19 V      120         8               7                  0       b
  20 V      128         8                                  1     p
  21 V      136         8                                  2     q
  22 V      144         8                                 16     l
  23 V      152         8                                 96     P
  24 V      160         8                                 24     Q
  25 V      168         8                                  0     L
""");
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_get_set();
    test_boolean();
    test_copy();
    test_base();
    test_move();
    test_move_to();
//  test_move_parallel();
    test_set_inc_dec_get();
    test_addressing();
    test_zero();
    test_boolean_result();
    test_is_ones_or_zeros();
    test_union();
    test_verilog_address();
    test_dump_verilog();
    test_dump_verilog_ignore();
    test_copy_bits();
    test_copy_memory();
    test_copyVerilogDec();
    test_copy_variable();
    test_copy_variable2();
   }

  static void newTests()                                                        // Tests being worked on
   {//oldTests();
    test_copy_memory();
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
