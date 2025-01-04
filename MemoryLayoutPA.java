//------------------------------------------------------------------------------
// Memory layout with base being global written in Pseudo Assembler
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

//D1 Construction                                                               // Construct a memory layout

  MemoryLayoutPA(Layout Layout, String Name)                                    // Every memory layout needs a layout and a name so we can generate verilog from it
   {this(Layout, Name, null);                                                   // Not based
   }

  MemoryLayoutPA(Layout Layout, String Name, MemoryLayoutPA Based)              // Like based storage in PL1.
   {name   = Name; based = Based; layout = Layout;
    memory = based != null ? Based.memory() : new Memory(Name, layout.size());  // If it is based it uses some one else's memory, if not based we must supply memory
    if (based != null) P = based.P;                                             // Reusetheunderlying program by default as well
   }

//D1 Control                                                                    // Testing, control and integrity

  Memory memory() {return based == null ? memory : based.memory();}             // Find the real memory used by this layout


  //void layout (Layout    Layout)  {layout = Layout;}                          // Set the base of the layout in memory allowing such layouts to be relocated
  void program(ProgramPA program) {P = program;}                                // Program in which to generate instructions

  void base(int Base)                                                           // Set the base of the layout in memory allowing such layouts to be relocated
   {if (based == null) stop("Memory layout is not based so cannot set a base");
    base = Base;
   }

  String name    () {return based == null ? name : based.name();}               // Name of this memory layout
  Layout layout  () {return layout;}                                            // Get the layout in use
  String baseName() {return name+"_base_offset";}                               // Name of the verilog field used to hold the base being used for this memory layout
  int    base    () {return base;}                                              // Get the base offset into memory being used

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

//D1 Get and Set                                                                // Get and set values in memory but only during testing

  int  getInt(Layout.Field field, int...indices)                                // Get a value from memory occupied by the layout
   {z();
    final int i = new At(field, indices).setOff().result;
    return i;
   }

  void setInt(Layout.Field field, int value, int...indices)                     // Set a value in memory occupoied by the layout
   {z();
    final At a = new At(field, indices).setOff();
    memory.set(a.at, a.width, value);
   }

  void setIntInstruction(Layout.Field field, int value, int...indices)          // Set a value in memory occupied by the layout
   {z();
    P.new I()
     {void a()
       {final At a = new At(field, indices).setOff();
        memory.set(a.at, a.width, value);
       }
      String v() {return new At(field, indices).verilogLoad() + " <= " + value + ";" + traceComment();}
     };
   }

  void zero()                                                                   // Clear the memory associated with the layout to zeros
   {z();
    memory.set(base, layout.size(), 0);
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

    String verilogLoadAddr(boolean la)                                          // A verilog representation of an addressed location in memory
     {final String base = based != null ? baseName()+"+" : "";                  // Base field name if a based memory layot
      if (directs == null || directs.length == 0)                               // An unindexed field
       {return (la ? base+field.at :                                            // IBM S/360 Principles of Operation: LA
          name()+"["+base+field.at +" +: "+field.width+"]")                     // IBM S/360 Principles of Operation: L
          +" /* "+field.name+" */";
       }
      final int N = directs.length;

      final Stack<String>      v = new Stack<>(), n = new Stack<>();            // Verilog expression, index variable names
      Layout.Locator           L = field.locator;
      final Stack<Layout.Array>A = L.arrays;                                    // The containing arrays

      for (int i = 0; i < N; i++)                                               // Convolute locator
       {final int w = A.elementAt(i).element.width;                             // Width of ontaining array element
        final At  a = directs[i];
        final int b = a.field.at + a.field.width;                               // The indexing field is assumed to have zero indices
        v.push(a.ml().name+"["+a.field.at+" +: "+a.field.width+"]*"+w);         // Access indexing field
        n.push(a.field.name);

       }
      final String name = field.name+(n.size() > 0 ?                            // Name of field plus any indexing fields
              "("+joinStrings(n, ",")+")" : "");

      return (la ? base+field.at+"+"+joinStrings(v, "+") :                      // IBM S/360 Principles of Operation: LA
        name()+"["+base+field.at+"+"+joinStrings(v, "+")+" +: "+field.width+"]")// IBM S/360 Principles of Operation: L
        +" /* "+name+" */";
     }

    String verilogLoad() {return verilogLoadAddr(false);}                       // Content of a memory location as a verilog expression
    String verilogAddr() {return verilogLoadAddr(true);}                        // Address of a memory location

    void locateDirectAddress()                                                  // Locate a direct address and its content
     {delta  = field.locator.at(indices);
      at     = base + delta;
      result = memory.getInt(at, width);
     }

    void locateInDirectAddress()                                                // Locate an indirect address and its content
     {final int N = directs.length;
      for (int i = 0; i < N; i++)
       {indices[i] = directs[i].setOff().getInt();  //////// Set off should not be needed here as the component variables of a locator expression should not have indices per the constructor.
       }
      locateDirectAddress();                                                    // Locate the address directly now that its indices are known
     }

    void checkCompiled(Layout.Field Field)                                      // Check the field has been compiled
     {if (!Field.compiled)
       {stop("Field:", Field.name, "has not been compiled yet");
       }
     if (layout == null)
       {stop("No layout has been supplied for this memory layout");
       }
     if (memory == null)
       {stop("No memory has been supplied for this memory layout");
       }
     if (layout != Field.container())
       {final String n = Field.name, m = layout.layoutName, f = Field.container().layoutName;
        if (f == null || m == null || !f.equals(m))
         {stop("Field:", n,      (f == null ? "" : "in layout: "+ f),
               "is not part of", (m == null ? "this layout" : "layout: "+ f));
         }
       }
     }

    At(Layout.Field Field)                                                      // No indices or base
     {z(); checkCompiled(Field);
      field = Field; indices = new int[0];
      width = field.width;
      if (width < 1) stop("Field", field.name, "does not have any bits");
      directs = null;
      locateDirectAddress();                                                    // The indices are constant so the address will not change over time
     }

    At(Layout.Field Field, int...Indices)                                       // Constant indices used for setting initial values
     {z(); checkCompiled(Field);
      field = Field; indices = Indices; width = field.width;
      directs = null;
      locateDirectAddress();                                                    // The indices are constant so the address will not change over time
     }

    At(Layout.Field Field, At...Directs)                                        // Variable indices used for obtaining run time values
     {z(); checkCompiled(Field);
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

    int width() {z(); return field.width();}                                    // Width of the field in memory

    At setOff() {z(); return setOff(true);}                                     // Set the base address of the field from its indices confirming that we are inside an executing instruction

    At setOff(boolean checkSetOff)                                              // Set the base address of the field
     {z();
      if (checkSetOff && !P.running) stop("Set off must be inside an instruction");
      if (directs != null) {z(); locateInDirectAddress();}                      // Evaluate indirect indices
      else                 {z(); locateDirectAddress();}                        // Evaluate direct indices
      return this;
     }

    boolean getBit(int i)            {return memory.getBit(at+i);}              // Get a bit from memory assuming that setOff() has been called to fix the location of the field containing the bit
    void    setBit(int i, boolean b) {memory.set(at+i, b);}                     // Set a bit in memory  assuming that setOff() has been called to fix the location of the field containing the bit

    int  getInt()          {z(); return result;}                                // The value in memory, at the indicated location, treated as an integer or the value of the constant, assumming setOff has been called to update the variable description
    void setInt(int value) {z(); memory.set(at, width, value);}                 // Set the value in memory at the indicated location, treated as an integer

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
     {z(); sameSize(source);
      final At target = this;
      P.new I()
       {void a()
         {source.setOff();
          target.setOff();
          for(int i = 0; i < width; ++i)
           {z();
            final boolean b = source.getBit(i);
            target.setBit(i, b);
           }
         }
        String v()
         {return target.verilogLoad()+" <= "+source.verilogLoad() + ";" + traceComment();
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
     {z(); sameSize(buffer);
      if (!(field instanceof Layout.Array))  stop("Array required for moveUp");
      final At target = this;
      buffer.move(target);                                                      // Make a copy of the thing to be moved so we can move in parallel
      P.new I()
       {void a()
         {final Layout.Array A = field.toArray();                               // Address field to be moved as an array
          final  int w =     A.element.width;                                   // Width of array element
          Index.setOff();
          for   (int i = Index.result+1; i < A.size; i++)                       // Each element
           {for (int j = 0;              j < w;      j++)                       // Each bit in each element
             {final boolean b = buffer.getBit((i-1)*w + j);
              target.setBit(i*w+j, b);
             }
            }
         }
        String n() {return field.name+" moveUp @ "+Index.field.name+" using "+buffer.field.name;}
       };
     }

    void moveDown(At Index, At buffer)                                          // Move the elements of an array down one position deleting the indexed element.  A buffer of the same size is used to permit copy in parallel.
     {z(); sameSize(buffer);
      if (!(field instanceof Layout.Array)) stop("Array required for moveDown");
      final At target = this;
      buffer.move(target);                                                      // Make a copy of the thing to be moved so we can move in parallel
      P.new I()
       {void a()
         {final Layout.Array A = field.toArray();                               // Address field to be moved as an array
          final  int w =     A.element.width;                                   // Width of array element
          Index.setOff();
          for   (int i = Index.result; i < A.size-1; i++)                       // Each element
           {for (int j = 0;            j < w;        j++)                       // Each bit in each element
             {final boolean b = buffer.getBit((i+1)*w + j);
              target.setBit(i*w+j, b);
             }
           }
         }
        String n() {return field.name+" moveDown @ "+Index.field.name+" using "+buffer.field.name;}
       };
     }

//D2 Bits                                                                       // Bit operations in a memory.

    void zero()                                                                 // Zero some memory
     {z();
      P.new I()
       {void a() {setOff(); memory.zero(at, width);}
        String v()
         {return verilogLoad()+" <= 0;" + traceComment();
         }
        String n()
         {return field.name+" <= 0";
         }
       };
     }

    void ones()                                                                 // Ones some memory
     {z();
      final String one = field.verilogOnes();
      P.new I()
       {void a()
         {setOff(); memory.ones(at, width);
         }
        String v()
         {return verilogLoad()+" <= "+one+ ";" + traceComment();
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
         {return verilogLoad()+" <= ~"+verilogLoad()+ ";" + traceComment();
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
     {z();
      for(int i = 0; i < width; ++i)
       {z(); if (getBit(i)) {z(); return false;}
       }
      z(); return true;
     }

    void    isZero(At result)                                                   // Whether a field is all zeros
     {z();
      final At target = this;
      P.new I()
       {void a()
         {result.setOff().setInt(setOff().isZero() ? 1 : 0);
         }
        String v()
         {return result.verilogLoad()+" <= "+target.verilogLoad()+" == 0;" + traceComment();
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
            " == " + target.field.verilogOnes()+ ";" + traceComment();
         }
        String n() {return result.field.name+" <= isOnes "+field.name;}
       };
     }

    private boolean equal(At b)                                                 // Whether  a == b
     {z(); sameSize(b);

      for(int i = 0; i < width; ++i)
       {z(); if (getBit(i) != b.getBit(i)) {z(); return false;}
       }
      z(); return true;
     }

    void equal(At b, At result)                                                // Whether  a == b
     {z();
      final At a = this;
      P.new I()
       {void a()
         {result.setOff().setInt(equal(b.setOff()) ? 1 : 0);
         }
        String v()
         {return result.verilogLoad()+" <= "+a.verilogLoad()+
                                       " == " + b.verilogLoad()+ ";" + traceComment();
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
                                         " != "+b.verilogLoad()+ ";" + traceComment();
         }
        String n() {return result.field.name+"="+field.name+"!="+b.field.name;}
       };
     }

    private boolean lessThan(At b)                                              // Whether a < b
     {z(); sameSize(b);
      for(int i = width; i > 0; --i)
       {z();
        if (!getBit(i-1) &&  b.getBit(i-1)) {z(); return true;}
        if ( getBit(i-1) && !b.getBit(i-1)) {z(); return false;}
       }
       z(); return false;
     }

    void lessThan(At b, At result)                                              // Whether  a < b
     {z(); sameSize(b);
      final At a = this;
      P.new I()
       {void a()
         {result.setOff().setInt(lessThan(b.setOff()) ? 1 : 0);
         }
        String v()
         {return result.verilogLoad()+" <= "+a.verilogLoad()+
                                          " < "+b.verilogLoad()+ ";" + traceComment();
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
                                         " <= "+b.verilogLoad()+ ";" + traceComment();
         }
        String n() {return result.field.name+"="+field.name+"<="+b.field.name;}
      };
     }

    void    greaterThan(At b, At result)                                        // Whether  a > b
     {z(); sameSize(b);
      final At a = this;
      P.new I()
       {void a()
         {result.setOff().setInt(!lessThan(b.setOff()) && !equal(b) ? 1 : 0);
         }
        String v()
         {return result.verilogLoad()+" <= "+a.verilogLoad()+
                                          " > "+b.verilogLoad()+ ";" + traceComment();
         }
        String n() {return result.field.name+"="+field.name+">"+b.field.name;}
       };
     }

    void    greaterThanOrEqual(At b, At result)                                 // Whether  a >= b
     {z(); sameSize(b);
      final At a = this;
      P.new I()
       {void a()
         {result.setOff().setInt(!lessThan(b.setOff()) ? 1 : 0);
         }
        String v()
         {return result.verilogLoad()+" <= "+a.verilogLoad()+
                                         " >= "+b.verilogLoad()+ ";" + traceComment();
         }
        String n() {return result.field.name+"="+field.name+">="+b.field.name;}
       };
     }

//D1 Arithmetic                                                                 // Arithmetic on integers

//D2 Binary                                                                     // Arithmetic on binary integers

    void inc()                                                                  // Increment a variable treated as an signed binary integer with wrap around on overflow.  Return the result after  the increment.
     {z();
      final At a = this;
      P.new I()
       {void a()
         {setOff();
          final int i = getInt()+1;
          setInt(i);
         }
        String v()
         {return a.verilogLoad()+" <= "+a.verilogLoad()+"+ 1;" + traceComment();
         }
        String n()
         {return "++"+field.name;
         }
       };
     }
    void dec()                                                                  // Decrement a variable treated as an signed binary integer with wrap around on overflow.  Return the result after  the increment.
     {z();
      final At a = this;
      P.new I()
       {void a()
         {setOff();
          final int i = getInt()-1;
          setInt(i);
         }
        String v()
         {return a.verilogLoad()+" <= "+a.verilogLoad()+"- 1;" + traceComment();
         }
        String n()
         {return "--"+field.name;
         }
       };
     }
   } // At

  At at(Layout.Field Field)                                                     // A field without indices or base addressing
   {return new    At(Field);
   }

  At at(Layout.Field Field, int...Indices)                                      // A field with constant indices
   {return new    At(Field,       Indices);
   }

  At at(Layout.Field Field, At...Indices)                                       // A field with  variable indices. Each index being a field with no indices
   {return new    At(Field,      Indices);
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
    print(layout.top, pp, 0);
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

//D1 Verilog                                                                    // Transfer memory to and from Verilog

  void dumpVerilog(String folder)                                               // Initialize memory in verilog with the contents of this memory
   {final StringBuilder s = new StringBuilder();
    final int N = memory.bits.length-1, B = logTwo(N)-1;
    final String name = name();
    s.append(declareVerilog());
    s.append("task initialize_memory_"+name+traceComment());
    s.append("    begin\n");
    for(int i = 0; i<= N; ++i)
     {final int b = memory.bits[i] ? 1 : 0;
      s.append("        "+name+"["+i+"] = "+b+traceComment());
     }
    s.append("    end\n");
    s.append("endtask\n");
    writeFile(folder+"includes/"+name+".sv", s);
   }

  String declareVerilog()                                                       // Declare matching memory  but do not initialize it
   {final int N = memory.bits.length-1, B = logTwo(N)-1;
    final StringBuilder s = new StringBuilder();
    if (based == null) s.append("reg ["+N+":0] "+name()    +traceComment());    // Actual memory if it is not based
    else               s.append("reg ["+B+":0] "+baseName()+traceComment());    // Base offset for this memory
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
     {M = new MemoryLayoutPA(l.compile(), "test");
      M.memory.alternating(4);
     }
   }

  static void test_get_set()
   {TestMemoryLayout   t = new TestMemoryLayout();
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
   {TestMemoryLayout t = new TestMemoryLayout();
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
   {TestMemoryLayout t = new TestMemoryLayout();
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
   {Layout           l = Layout.layout();
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
   {Layout           l = Layout.layout();
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

  static void test_set_inc_dec_get()
   {Layout           l = Layout.layout();
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
   }

  static void test_addressing()
   {final int N = 8;
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
       {Z.setOff().setInt(0);
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
       {az.setOff().setInt(10);
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
   {Layout           l = Layout.layout();
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
   {Layout           l = Layout.layout();
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
   {Layout           l = Layout.layout();
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
   {Layout               l = Layout.layout();
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
    //stop(at.verilogLoad());
    ok(at.verilogLoad(), "M[0+M[24 +: 4]*6 +: 2] /* a(I) */");
    //stop(at.verilogAddr());
    ok(at.verilogAddr(),   "0+M[24 +: 4]*6 /* a(I) */");
   }

  static void test_dump_verilog()
   {Layout               l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 2);
    Layout.Array     A = l.array    ("A", a, 4);
    MemoryLayoutPA   m = new MemoryLayoutPA(l.compile(), "M");
    m.memory().alternating(4);
    m.dumpVerilog("verilog/");                                                  // Dump main memory
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_get_set();
    test_boolean();
    test_copy();
    test_base();
    test_move();
    test_set_inc_dec_get();
    test_addressing();
    test_zero();
    test_boolean_result();
    test_is_ones_or_zeros();
    test_union();
    test_verilog_address();
    test_dump_verilog();
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
