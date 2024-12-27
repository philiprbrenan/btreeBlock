//------------------------------------------------------------------------------
// Memory layout with base being global written in Pseudo Assembler
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Memory layout

import java.util.*;

class MemoryLayoutPA extends Test                                               // Memory layout
 {Layout layout;                                                                // Layout of part of memory
  Memory memory;                                                                // Memory containing layout
  int      base;                                                                // Base of layout in memory - like located in Pl1
  boolean debug;                                                                // Debug if true
  ProgramPA P = new ProgramPA();                                                // Program containing generated code

//D1 Control                                                                    // Testing, control and integrity

  void memory (Memory    Memory)  {memory = Memory;}                            // Set the base of the layout in memory allowing such layouts to be relocated
  void layout (Layout    Layout)  {layout = Layout;}                            // Set the base of the layout in memory allowing such layouts to be relocated
  void program(ProgramPA program) {P      = program;}                           // Program in which to generate instructions
  void base   (int Base)          {base   = Base;}                              // Set the base of the layout in memory allowing such layouts to be relocated

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

    void locateDirectAddress()                                                  // Locate a direct address and its content
     {delta  = field.locator.at(indices);
      at     = base + delta;
      result = memory.getInt(at, width);
     }

    void locateInDirectAddress()                                                // Locate an indirect address and its content
     {final int N = directs.length;
      for (int i = 0; i < N; i++)
       {indices[i] = directs[i].setOff().getInt();
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
      field = Field; indices = new int[0]; width = field.width;
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
      for (int i = 0; i < Directs.length; i++)
       {if (Directs[i].directs != null) stop("Index:", i, "must not have a base or indices");
       }

      field = Field; width = field.width;
      directs = Directs;
      indices = new int[Directs.length];
     }

    boolean sameSize(At b)                                                      // Check two fields are the same size
     {if (field == null) return true;                                           // Constants match any size
      z(); field.sameSize(b.field);
      z(); return true;
     }

    int width() {z(); return field.width();}                                    // Width of the field in memory

    At setOff()                                                                 // Set the base address of the field
     {z();
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
       {setOff();
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

    MemoryLayoutPA ml() {return MemoryLayoutPA.this;}                               // Containing memory layout

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
      P.new I() {void a() {memory.zero(at, width);}};
     }

    void ones()                                                                 // Ones some memory
     {z();
      P.new I() {void a() {memory.ones(at, width);}};
     }

    void invert(At a)                                                           // Invert the specified bits
     {z();
      P.new I() {void a() {memory.invert(a.result, a.width());}};
     }

    boolean isAllZero()                                                         // Check that the specified memory is all zeros
     {z(); return memory.isAllZero(at, width);
     }

    boolean isAllOnes()                                                         // Check that  the specified memory is all ones
     {z(); return memory.isAllOnes(at, width);
     }

//D1 Boolean                                                                    // Boolean operations on fields held in memories.

    private boolean isZero()                                                    // Whether the field is all zero
     {z();
      for(int i = 0; i < width; ++i)
       {z(); if (getBit(i)) {z(); return false;}
       }
      z(); return true;
     }

    void    isZero(At result)                                                   // Whether a field is all zeros
     {z();
      P.new I()
       {void a()
         {result.setInt(isZero() ? 1 : 0);
         }
        String n() {return result.field.name+" = isZero "+field.name;}
       };
     }

    private boolean isOnes()                                                    // Whether the field is all ones
     {z();
      for(int i = 0; i < width; ++i)
       {z(); if (!getBit(i)) {z(); return false;}
       }
      z(); return true;
     }

    void isOnes(At result)                                                      // Whether a field is all ones
     {z();
      P.new I()
       {void a()
         {result.setInt(isOnes() ? 1 : 0);
         }
        String n() {return result.field.name+" = isOnes "+field.name;}
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
      P.new I()
       {void a()
         {result.setInt(equal(b) ? 1 : 0);
         }
        String n() {return result.field.name+"="+field.name+"=="+b.field.name;}
       };
     }

    void    notEqual(At b, At result)                                           // Whether  a != b
     {z();
      P.new I()
       {void a()
         {result.setInt(!equal(b) ? 1 : 0);
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
      P.new I()
       {void a()
         {result.setInt(lessThan(b) ? 1 : 0);
         }
        String n() {return result.field.name+"="+field.name+"<"+b.field.name;}
       };
     }

    void lessThanOrEqual(At b, At result)                                       // Whether  a <= b
     {z(); sameSize(b);
      P.new I()
       {void a()
        {result.setInt(lessThan(b) || equal(b) ? 1 : 0);
        }
        String n() {return result.field.name+"="+field.name+"<="+b.field.name;}
      };
     }

    void    greaterThan(At b, At result)                                        // Whether  a > b
     {z(); sameSize(b);
      P.new I()
       {void a()
         {result.setInt(!lessThan(b) && !equal(b) ? 1 : 0);
         }
        String n() {return result.field.name+"="+field.name+">"+b.field.name;}
       };
     }

    void    greaterThanOrEqual(At b, At result)                                 // Whether  a >= b
     {z(); sameSize(b);
      P.new I()
       {void a()
         {result.setInt(!lessThan(b) ? 1 : 0);
         }
        String n() {return result.field.name+"="+field.name+">="+b.field.name;}
       };
     }

//D1 Arithmetic                                                                 // Arithmetic on integers

//D2 Binary                                                                     // Arithmetic on binary integers

    void inc() {z(); P.new I() {void a() {setOff(); final int i = getInt()+1; setInt(i);} String n() {return "++"+field.name;}};} // Increment a variable treated as an signed binary integer with wrap around on overflow.  Return the result after  the increment.
    void dec() {z(); P.new I() {void a() {setOff(); final int i = getInt()-1; setInt(i);} String n() {return "--"+field.name;}};} // Decrement a variable treated as an signed binary integer with wrap around on underflow. Return the result after  the decrement.
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
        final int n = getInt(field, in);
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
    MemoryLayoutPA   M;
    TestMemoryLayout()
     {l.compile();
      M = new MemoryLayoutPA();
      M.memory(new Memory(l.size()));
      M.layout(l);
      M.base(0);
      M.memory.alternating(4);
     }
   }

  static void test_get_set()
   {TestMemoryLayout t = new TestMemoryLayout();
        MemoryLayoutPA m = t.M;
              Layout l = m.layout;
   ok(m.at( t.c,      0, 0, 0), "c[0,0,0](0+12)12=15");
   m.setInt(t.c,  11, 0, 0, 0);
   ok(m.at( t.c,      0, 0, 0), "c[0,0,0](0+12)12=11");

   ok(m.at( t.c,      0, 0, 1), "c[0,0,1](0+24)24=0");
   m.setInt(t.c,  11, 0, 0, 1);
   ok(m.at( t.c,      0, 0, 1), "c[0,0,1](0+24)24=11");

   ok(m.at( t.a,      0, 2, 2), "a[0,2,2](0+116)116=15");
   m.setInt(t.a,   5, 0, 2, 2);
   ok(m.at( t.a,      0, 2, 2), "a[0,2,2](0+116)116=5");

   ok(m.at( t.b,      1, 2, 2), "b[1,2,2](0+252)252=15");
   m.setInt(t.b,   7, 1, 2, 2);
   ok(m.at( t.b,      1, 2, 2), "b[1,2,2](0+252)252=7");

    ok(m.at( t.e,     1, 2), "e[1,2](0+260)260=15");
    m.setInt(t.e, 11, 1, 2);
    ok(m.at( t.e,     1, 2), "e[1,2](0+260)260=11");
   }

  static void test_boolean()
   {TestMemoryLayout t = new TestMemoryLayout();
        MemoryLayoutPA m = t.M;
    m.setInt(t.a, 1, 0, 1, 1);
    m.setInt(t.a, 2, 0, 1, 2);
    m.setInt(t.a, 1, 0, 2, 1);
    m.setInt(t.a, 2, 0, 2, 2);

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
    m.setInt(t.a, 1, 0, 0, 0);
    m.setInt(t.a, 2, 0, 0, 1);
    m.setInt(t.a, 3, 0, 0, 2);

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
    l.compile();
    final int        N = l.size();

    MemoryLayoutPA     m = new MemoryLayoutPA();
    m.layout(l);
    m.memory(new Memory(2*N));
    m.base(N);
    m.memory.alternating(4);
    //stop(m);
    m.ok("""
Line T       At      Wide       Size    Indices        Value   Name
   1 S       16        16                                      s
   2 V       16         4                                  0     a
   3 V       20         4                                 15     b
   4 V       24         4                                  0     c
   5 V       28         4                                 15     d
""");
    ok(m.getInt(a),  0);
    ok(m.getInt(b), 15);
       m.setInt(a,  9);
       m.setInt(b, 10);
       m.setInt(c, 11);
       m.setInt(d, 12);

    //stop(m);
    m.ok("""
Line T       At      Wide       Size    Indices        Value   Name
   1 S       16        16                                      s
   2 V       16         4                                  9     a
   3 V       20         4                                 10     b
   4 V       24         4                                 11     c
   5 V       28         4                                 12     d
""");
   }

  static void test_based_array()
   {final int        B = 8;
    Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 4);
    Layout.Array     A = l.array    ("A", a, 4);
    l.compile();
    MemoryLayoutPA     m = new MemoryLayoutPA();
    m.layout(l);
    m.memory(new Memory(l.size()+B));
    m.base(B);
    m.memory.alternating(4);
    //stop(m);
    m.ok("""
Line T       At      Wide       Size    Indices        Value   Name
   1 A        8        16          4                           A
   2 V        8         4               0                  0     a
   3 V       12         4               1                 15     a
   4 V       16         4               2                  0     a
   5 V       20         4               3                 15     a
""");
    ok(m.getInt(a,  0),  0);
    ok(m.getInt(a,  1), 15);
    ok(m.getInt(a,  2),  0);
    ok(m.getInt(a,  3), 15);
       m.setInt(a,  9,  0);
       m.setInt(a, 10,  1);
       m.setInt(a, 11,  2);
       m.setInt(a, 12,  3);
    ok(m.getInt(a, 0),  9);
    ok(m.getInt(a, 1), 10);
    ok(m.getInt(a, 2), 11);
    ok(m.getInt(a, 3), 12);

    //stop(m);
    m.ok("""
Line T       At      Wide       Size    Indices        Value   Name
   1 A        8        16          4                           A
   2 V        8         4               0                  9     a
   3 V       12         4               1                 10     a
   4 V       16         4               2                 11     a
   5 V       20         4               3                 12     a
""");
   }

  static void test_move()
   {Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 4);
    Layout.Variable  b = l.variable ("b", 4);
    Layout.Variable  c = l.variable ("c", 4);
    Layout.Variable  d = l.variable ("d", 4);
    Layout.Structure s = l.structure("s", a, b, c, d);
    l.compile();

    MemoryLayoutPA     m = new MemoryLayoutPA();
    m.layout(l);
    m.memory(new Memory(l.size()));
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
    l.compile();

    MemoryLayoutPA   m = new MemoryLayoutPA();
    m.layout(l);
    m.memory(new Memory(l.size()));

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
    l.compile();

    MemoryLayoutPA     m = new MemoryLayoutPA();
    m.layout(l);
    m.memory(new Memory(l.size()));

    MemoryLayoutPA.At  Z = m.at(z), I = m.at(i), J = m.at(j);
    Z.setOff().setInt(0);
    I.setOff().setInt(1);
    J.setOff().setInt(2);

    MemoryLayoutPA.At az = m.at(a, Z);
    MemoryLayoutPA.At ai = m.at(a, I);
    MemoryLayoutPA.At aj = m.at(a, J);

    az.setOff().setInt(10);
    ai.setOff().setInt(11);
    aj.setOff().setInt(12);

    //stop(m.memory);
    ok(""+m.memory, """
      4... 4... 4... 4... 3... 3... 3... 3... 2... 2... 2... 2... 1... 1... 1... 1...
Line  FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210
   0  0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0c0b 0a02 0100
""");

    //stop(m);
    ok(""+m, """
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
    ai.setOff().setInt(13);
    aj.setOff().setInt(14);
    //stop(m);
    ok(""+m, """
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

    MemoryLayoutPA     L = new MemoryLayoutPA();
    L.layout(l);
    L.memory(new Memory(l.size()+16));
    L.base(16);

    L.memory.alternating(4);
    //stop(L.memory);
    ok(L.memory, """
      4... 4... 4... 4... 3... 3... 3... 3... 2... 2... 2... 2... 1... 1... 1... 1...
Line  FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210
   0  0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 00f0 f0f0 f0f0
""");

    //stop(L);
    ok(L, """
Line T       At      Wide       Size    Indices        Value   Name
   1 A       16        24          6                           A
   2 V       16         4               0                  0     a
   3 V       20         4               1                 15     a
   4 V       24         4               2                  0     a
   5 V       28         4               3                 15     a
   6 V       32         4               4                  0     a
   7 V       36         4               5                 15     a
""");

    L.zero();

    //stop(L.memory);
    ok(L.memory, """
      4... 4... 4... 4... 3... 3... 3... 3... 2... 2... 2... 2... 1... 1... 1... 1...
Line  FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210
   0  0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 f0f0
""");

    //stop(L);
    ok(L, """
Line T       At      Wide       Size    Indices        Value   Name
   1 A       16        24          6                           A
   2 V       16         4               0                  0     a
   3 V       20         4               1                  0     a
   4 V       24         4               2                  0     a
   5 V       28         4               3                  0     a
   6 V       32         4               4                  0     a
   7 V       36         4               5                  0     a
""");
   }

  static void test_boolean_result()
   {Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 4);
    Layout.Variable  b = l.variable ("b", 4);
    Layout.Bit       r = l.bit      ("r");
    Layout.Bit       R = l.bit      ("R");
    Layout.Structure s = l.structure("s", a, b, r, R);
    l.compile();

    MemoryLayoutPA     m = new MemoryLayoutPA();
    m.layout(l);
    m.memory(new Memory(l.size()));

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
    l.compile();

    MemoryLayoutPA   m = new MemoryLayoutPA();
    m.layout(l);
    m.memory(new Memory(l.size()));

    m.at(a).zero();
    m.P.new I() {void a() {m.at(b).setInt(2);}};
    m.at(c).ones();
    m.P.run(); m.P.clear();
    //stop(m);
    ok(m, """
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

  static void test_not_compiled()
   {Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 4);
    Layout.Variable  b = l.variable ("b", 4);
    Layout.Variable  c = l.variable ("c", 4);
    Layout.Structure s = l.structure("s", a, b, c);
    MemoryLayoutPA     m = new MemoryLayoutPA();

    sayThisOrStop("Field: a has not been compiled yet");

    try {m.at(a).zero();} catch(Exception e) {}
    m.layout(l.compile());
    m.memory(new Memory(l.size()));
    m.at(a).zero();
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_get_set();
    test_boolean();
    test_copy();
    test_base();
    test_based_array();
    test_move();
    test_set_inc_dec_get();
    test_addressing();
    test_zero();
    test_boolean_result();
    test_is_ones_or_zeros();
    test_not_compiled();
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
