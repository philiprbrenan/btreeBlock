//------------------------------------------------------------------------------
// Memory layout
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Memory layout

import java.util.*;

class MemoryLayout extends Test                                                 // Memory layout
 {final Layout layout;                                                          // Layout of part of memory
  final Memory memory;                                                          // Memory containing layout
  final int      base;                                                          // Base of layout in memory

//D1 Construction                                                               // Address memory via a layout

  MemoryLayout(Layout Layout)
   {this(new Memory(Layout.size()), Layout, 0);
   }

  MemoryLayout(Memory Memory, Layout Layout) {this(Memory, Layout, 0);}

  MemoryLayout(Memory Memory, Layout Layout, int Base)
   {memory = Memory;
    layout = Layout;
    base   = Base;
   }

//D1 Control                                                                    // Testing, control and integrity

  void stop()              {Test.stop(toString());}                             // Stop after printing

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

//D1 Get and Set                                                                // Get and set values in memory

  int  getInt(Layout.Field field, int Base,            int...indices)           // Get a value from memory
   {z(); return new At(field, Base, indices).result;
   }

  void setInt(Layout.Field field, int value, int Base, int...indices)           // Set a value in memory
   {z();
    final At a = new At(field, Base, indices);
    memory.set(a.at, a.width, value);
   }

//D1 Components                                                                 // Locate a variable in memory via its indices

  class At
   {final Layout.Field field;                                                   // Field description in layout
    enum Type {constant, direct, indirect};                                     // Type of memory reference. Constant a known constant value. Direct: a field with indioces known at compile time. Indirect: a field whose indices are other fields whose indices are known at compile time.
    final Type type;                                                            // Type of memory reference
    final int[]indices;                                                         // Known indices to be applied directly to locate the field in memeory
    final Layout.Field[]indirects;                                              // Fields that do not need to be indexed whose values containg the indices to be applied to locate the field in memeory
    final int  width;                                                           // Width of element in memory
    int  base;                                                                  // Base address of memory
    Layout.Field baseField;                                                     // Field used to locate base of data being addressed
    int  delta;                                                                 // Delta due to indices
    int  at;                                                                    // Location in memory
    int  result;                                                                // The contents of memory at this location
    At(int constant)                                                            // Constant
     {z(); field = null; indices = null; width = base = delta = at = 0;
      indirects = null;
      type = Type.constant; result = constant;
     }

    void locateDirectAddress()                                                  // Locate a direct address and its content
     {delta  = field.locator.at(indices);
      at     = base + delta;
      result = memory.getInt(at, width);
     }

    void locateInDirectAddress()                                                // Locate an indirect address and its content
     {for (int i = 0; i < indirects.length; i++)
       {indices[i] = MemoryLayout.this.getInt(indirects[i], 0);
       }
      base = MemoryLayout.this.getInt(baseField, 0);
      locateDirectAddress();                                                    // Loacate the address directly now that its indices are known
     }

    At(Layout.Field Field)                                                      // No indices or base
     {z(); field = Field; indices = new int[0]; width = field.width; base = 0;
      type = Type.direct; indirects = null;
      locateDirectAddress();                                                    // The indices are constant so the address will not change over time
     }

    At(Layout.Field Field, int Base, int...Indices)                             // Constant indices
     {z(); field = Field; indices = Indices; width = field.width; base = Base;
      type = Type.direct; indirects = null;
      locateDirectAddress();                                                    // The indices are constant so the address will not change over time
     }
    At(Layout.Field Field, Layout.Field BaseField, Layout.Field...Indirects)    // Variable indices
     {z(); field = Field; width = field.width; baseField = BaseField;
      type = Type.indirect; indirects = Indirects;
      indices = new int[Indirects.length];
      }

    boolean sameSize(At b)                                                      // Check two fields are the same size
     {if (field == null) return true;                                           // Constants match any size
      z(); field.sameSize(b.field);
      z(); if (MemoryLayout.this != b.ml()) stop("Different memory layout");
      z(); return true;
     }

    int width()                                                                 // Width of the field in memory
     {z(); if (type == Type.constant) stop("A constant does not have any specific width");
      z(); return field.width();
     }

    int  getOff()                                                               // The address of the field
     {z();
      switch(type)
       {case constant -> {return 0;}                                            // Constants can have any address
        case direct   -> {return at;}                                           // Precomputed address
        case indirect ->
         {locateInDirectAddress();                                              // Evaluate indices - possible because the indices themselves have no indices, else we would be asking "Qui indexe ipso indexes"?
          return at;
         }
        default -> {stop("Unknown addressing mode:", type); return 0;}
       }
     }

    int  getInt() {z(); getOff(); return result;}                               // The value in memory, at the indicated location, treated as an integer or the value of the constant

    void setInt(int value)  {z(); getOff(); memory.set(at, width, value);}      // Set the value in memory at the indicated location, treated as an integer

    public String toString()                                                    // Print field name(indices)=value or name=value if there are no indices
     {final StringBuilder s = new StringBuilder();
      s.append(field.name);
      if (indices.length > 0)
       {s.append("[");
        for (int i = 0, N = indices.length; i < N; i++)
         {s.append(indices[i]);
          if (i < N-1) s.append(",");
         }
        s.append("]("+base+"+"+delta+")"+at);
       }
      s.append("="+result);
      return s.toString();
     }
    MemoryLayout ml() {return MemoryLayout.this;}
   }

  At at(Layout.Field Field)                                                     // A field without indices or base addressing
   {return new At(Field);
   }

  At at(Layout.Field Field, int Base, int...Indices)                            // A field with base and constant indices
   {return new At(Field, Base, Indices);
   }

  At at(Layout.Field Field, Layout.Field Base, Layout.Field ...Indices)         // A field with base and variable indices, each variable index being a field with no base and no indices
   {return new At(Field, Base, Indices);
   }

  class Constant extends At                                                     // A constant integer
   {Constant(int constant)
     {super(constant);
     }
   }

  Constant constant(int constant)
   {return new Constant(constant);
   }

//D2 Composite                                                                  // Composite memory access

  void zero(At a)                                                               // Zero some memory
   {z(); memory.zero(a.at, a.width);
   }

  void ones(At a)                                                               // Ones some memory
   {z(); memory.ones(a.at, a.width);
   }

  void copy(At target, At source)                                               // Copy the specified number of bits from source to target low bits first
   {z(); target.sameSize(source);
    memory.copy(target.at, source.at, source.width);
   }

  void copyHigh(At target, At source)                                           // Copy the specified number of bits from source to target high bits first
   {z(); target.sameSize(source);
    memory.copyHigh(target.at, source.at, source.width);
   }

  void move(At target, At source, At buffer)                                    // Copy the specified number of bits from source to target via a buffer to allow the operation to proceed in bit parallel
   {z(); target.sameSize(source); target.sameSize(buffer);
    memory.copy(buffer.at, source.at, source.width);
    memory.copy(target.at, buffer.at, source.width);
   }

  void invert(At a)                                                             // Invert the specified bits
   {z(); memory.invert(a.result, a.width());
   }

//D1 Boolean                                                                    // Boolean operations on fields held in memeory

  boolean isAllZero(At a)                                                       // Check that the specified memory is all zeros
   {z(); return memory.isAllZero(a.at, a.width);
   }

  boolean isAllOnes(At a)                                                       // Check that  the specified memory is all ones
   {z(); return memory.isAllOnes(a.at, a.width);
   }

  boolean equal(At a, At b)                                                     // Whether  a == b
   {z(); if (a.field == null || b.field == null) {z(); return a.result == b.result;} // Constant comparison
    z(); a.sameSize(b);
    z(); return memory.equals(a.at, b.at, a.width);
   }

  boolean notEqual(At a, At b)                                                  // Whether a != b
   {z(); if (a.field == null || b.field == null) {z(); return a.result != b.result;} // Constant comparison
    z(); a.sameSize(b);
    return memory.notEquals(a.at, b.at, a.width);
   }

  boolean lessThan(At a, At b)                                                  // Whether a < b
   {z(); if (a.field == null || b.field == null) {z(); return a.result <  b.result;} // Constant comparison
    z(); a.sameSize(b);
    return memory.lessThan(a.at, b.at, a.width);
   }

  boolean lessThanOrEqual(At a, At b)                                           // Whether a <= b
   {z(); if (a.field == null || b.field == null) {z(); return a.result <= b.result;} // Constant comparison
    z(); a.sameSize(b);
    return memory.lessThanOrEqual(a.at, b.at, a.width);
   }

  boolean greaterThan(At a, At b)                                               // Whether a > b
   {z(); if (a.field == null || b.field == null) {z(); return a.result > b.result;} // Constant comparison
    z(); a.sameSize(b);
    return memory.greaterThan(a.at, b.at, a.width);
   }

  boolean greaterThanOrEqual(At a, At b)                                        // Whether a >= b
   {z(); if (a.field == null || b.field == null) {z(); return a.result >= b.result;} // Constant comparison
    z(); a.sameSize(b);
    return memory.greaterThanOrEqual(a.at, b.at, a.width);
   }

//D1 Arithmetic                                                                 // Arithmetic on integers

//D2 Binary                                                                     // Arithmetic on binary integers

  void inc(At a) {z(); a.setInt(a.getInt()+1);}                                 // Increment a variable treated as an signed binary integer with wrap around on overflow
  void dec(At a) {z(); a.setInt(a.getInt()-1);}                                 // Decrement a variable treated as an signed binary integer with wrap around on underflow

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
        final int n = getInt(field, base, in);
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
    MemoryLayout     M;
    TestMemoryLayout()
     {l.compile();
      M = new MemoryLayout(l);
      M.memory.alternating(4);
     }
   }

  static void test_get_set()
   {TestMemoryLayout t = new TestMemoryLayout();
        MemoryLayout m = t.M;
              Layout l = m.layout;
   ok(m.at(t.c,     0, 0, 0, 0), "c[0,0,0](0+12)12=15");
   m.setInt(t.c, 11, 0, 0, 0, 0);
   ok(m.at(t.c,     0, 0, 0, 0), "c[0,0,0](0+12)12=11");

   ok(m.at(t.c,     0, 0, 0, 1), "c[0,0,1](0+24)24=0");
   m.setInt(t.c, 11, 0, 0, 0, 1);
   ok(m.at(t.c,     0, 0, 0, 1), "c[0,0,1](0+24)24=11");

   ok(m.at(t.a,     0, 0, 2, 2), "a[0,2,2](0+116)116=15");
   m.setInt(t.a,  5, 0, 0, 2, 2);
   ok(m.at(t.a,     0, 0, 2, 2), "a[0,2,2](0+116)116=5");

   ok(m.at(t.b,     0, 1, 2, 2), "b[1,2,2](0+252)252=15");
   m.setInt(t.b,  7, 0, 1, 2, 2);
   ok(m.at(t.b,     0, 1, 2, 2), "b[1,2,2](0+252)252=7");

    ok(m.at(t.e,     0, 1, 2), "e[1,2](0+260)260=15");
    m.setInt(t.e, 11, 0, 1, 2);
    ok(m.at(t.e,     0, 1, 2), "e[1,2](0+260)260=11");
   }

  static void test_boolean()
   {TestMemoryLayout t = new TestMemoryLayout();
        MemoryLayout m = t.M;
    m.setInt(t.a, 1, 0, 0, 1, 1);
    m.setInt(t.a, 2, 0, 0, 1, 2);
    m.setInt(t.a, 1, 0, 0, 2, 1);
    m.setInt(t.a, 2, 0, 0, 2, 2);

    ok( m.equal   (m.at(t.a, 0, 0, 1, 1), m.at(t.a, 0, 0, 2, 1)));
    ok(!m.equal   (m.at(t.a, 0, 0, 1, 1), m.at(t.a, 0, 0, 1, 2)));
    ok(!m.notEqual(m.at(t.a, 0, 0, 1, 1), m.at(t.a, 0, 0, 2, 1)));
    ok( m.notEqual(m.at(t.a, 0, 0, 1, 1), m.at(t.a, 0, 0, 1, 2)));

    ok( m.lessThan          (m.at(t.a, 0, 0, 1, 1), m.at(t.a, 0, 0, 1, 2)));
    ok(!m.lessThan          (m.at(t.a, 0, 0, 1, 1), m.at(t.a, 0, 0, 2, 1)));
    ok( m.lessThanOrEqual   (m.at(t.a, 0, 0, 1, 1), m.at(t.a, 0, 0, 1, 2)));
    ok( m.lessThanOrEqual   (m.at(t.a, 0, 0, 1, 1), m.at(t.a, 0, 0, 2, 1)));
    ok(!m.lessThanOrEqual   (m.at(t.a, 0, 0, 1, 2), m.at(t.a, 0, 0, 2, 1)));

    ok(!m.greaterThan       (m.at(t.a, 0, 0, 1, 1), m.at(t.a, 0, 0, 1, 2)));
    ok( m.greaterThan       (m.at(t.a, 0, 0, 1, 2), m.at(t.a, 0, 0, 1, 1)));
    ok( m.greaterThanOrEqual(m.at(t.a, 0, 0, 2, 1), m.at(t.a, 0, 0, 1, 1)));
    ok( m.greaterThanOrEqual(m.at(t.a, 0, 0, 1, 1), m.at(t.a, 0, 0, 2, 1)));
    ok(!m.greaterThanOrEqual(m.at(t.a, 0, 0, 2, 1), m.at(t.a, 0, 0, 1, 2)));
   }

  static void test_copy()
   {TestMemoryLayout t = new TestMemoryLayout();
        MemoryLayout m = t.M;
    m.setInt(t.a, 1, 0, 0, 0, 0);
    m.setInt(t.a, 2, 0, 0, 0, 1);
    m.setInt(t.a, 3, 0, 0, 0, 2);

    ok(m.at(t.a, 0, 0, 0, 0), "a[0,0,0](0+4)4=1");
    ok(m.at(t.a, 0, 0, 0, 1), "a[0,0,1](0+16)16=2");
    ok(m.at(t.a, 0, 0, 0, 2), "a[0,0,2](0+28)28=3");

    m.copy(m.at(t.a, 0, 0, 0, 1), m.at(t.a, 0, 0, 0, 0));
    m.copy(m.at(t.a, 4, 0, 0, 1), m.at(t.a, 0, 0, 0, 1));

    ok(m.at(t.a, 0, 0, 0, 0), "a[0,0,0](0+4)4=1");
    ok(m.at(t.a, 4, 0, 0, 0), "a[0,0,0](4+4)8=0");
    ok(m.at(t.a, 0, 0, 0, 2), "a[0,0,2](0+28)28=3");
   }

  static void test_base()
   {Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 4);
    Layout.Variable  b = l.variable ("b", 4);
    Layout.Variable  c = l.variable ("c", 4);
    Layout.Variable  d = l.variable ("d", 4);
    Layout.Structure s = l.structure("s", a, b, c, d);
    l.compile();

    MemoryLayout     m = new MemoryLayout(l);
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
    ok(m.getInt(a, 0),  0);
    ok(m.getInt(a, 4), 15);
    m.setInt(a,  9,  0);
    m.setInt(a, 10,  4);
    m.setInt(b, 11,  4);
    m.setInt(a, 12, 12);

    //stop(m);
    m.ok("""
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        16                                      s
   2 V        0         4                                  9     a
   3 V        4         4                                 10     b
   4 V        8         4                                 11     c
   5 V       12         4                                 12     d
""");
   }

  static void test_based_array()
   {Layout           l = Layout.layout();
    Layout.Variable  z = l.variable ("z", 4);
    Layout.Variable  a = l.variable ("a", 4);
    Layout.Array     A = l.array    ("A", a, 4);
    Layout.Structure S = l.structure("S", z, A);
    l.compile();

    MemoryLayout     m = new MemoryLayout(l);
    m.memory.alternating(4);
    //stop(m);
    m.ok("""
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        20                                      S
   2 V        0         4                                  0     z
   3 A        4        16          4                             A
   4 V        4         4               0                 15       a
   5 V        8         4               1                  0       a
   6 V       12         4               2                 15       a
   7 V       16         4               3                  0       a
""");
    ok(m.getInt(a, 0, 0), 15);
    ok(m.getInt(a, 4, 0),  0);
    m.setInt(a,  9,  0, 0);
    m.setInt(a, 10,  4, 0);
    m.setInt(a, 11,  8, 0);
    m.setInt(a, 12, 12, 0);
    ok(m.getInt(a, 0, 0),  9);
    ok(m.getInt(a, 4, 0), 10);
    ok(m.getInt(a, 4, 1), 11);
    ok(m.getInt(a, 4, 2), 12);

    //stop(m);
    m.ok("""
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        20                                      S
   2 V        0         4                                  0     z
   3 A        4        16          4                             A
   4 V        4         4               0                  9       a
   5 V        8         4               1                 10       a
   6 V       12         4               2                 11       a
   7 V       16         4               3                 12       a
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

    MemoryLayout     m = new MemoryLayout(l);
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
    m.move(m.at(d), m.at(a),  m.at(b));

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

    MemoryLayout     m = new MemoryLayout(l);
    m.at(a).setInt(1);
    m.at(b).setInt(3);
    //stop(m);
    m.ok("""
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0         8                                      s
   2 V        0         4                                  1     a
   3 V        4         4                                  3     b
""");
    m.inc(m.new At(a));
    m.dec(m.new At(b));

    //stop(m);
    m.ok("""
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0         8                                      s
   2 V        0         4                                  2     a
   3 V        4         4                                  2     b
""");

    ok(m.at(a).getInt(), 2);
    ok(m.at(b).getInt(), 2);
   }

  static void test_boolean_constant()
   {Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 4);
    l.compile();

    MemoryLayout     m = new MemoryLayout(l);
    m.at(a).setInt(1);

    MemoryLayout.At A = m.at(a),
     c0 = m.constant(0), c1 = m.constant(1), c2 = m.constant(2);
    final boolean T = true, F = false;

    ok(m.equal   (A, c0), F); ok(m.equal   (A, c1), T); ok(m.equal   (A, c2), F);
    ok(m.notEqual(A, c0), T); ok(m.notEqual(A, c1), F); ok(m.notEqual(A, c2), T);

    ok(m.lessThan       (A, c0), F); ok(m.lessThan       (A, c1), F); ok(m.lessThan       (A, c2), T);
    ok(m.lessThanOrEqual(A, c0), F); ok(m.lessThanOrEqual(A, c1), T); ok(m.lessThanOrEqual(A, c2), T);

    ok(m.greaterThan       (A, c0), T); ok(m.greaterThan       (A, c1), F); ok(m.greaterThan       (A, c2), F);
    ok(m.greaterThanOrEqual(A, c0), T); ok(m.greaterThanOrEqual(A, c1), T); ok(m.greaterThanOrEqual(A, c2), F);
   }

  static void test_addressing()
   {Layout           l = Layout.layout();
    Layout.Variable  z = l.variable ("z", 4);
    Layout.Variable  i = l.variable ("i", 4);
    Layout.Variable  j = l.variable ("j", 4);
    Layout.Variable  a = l.variable ("a", 4);
    Layout.Array     A = l.array    ("A", a, 4);
    Layout.Structure S = l.structure("S", z, i, j, A);
    l.compile();

    MemoryLayout     m = new MemoryLayout(l);
    m.at(i).setInt(1);
    m.at(j).setInt(2);
    m.at(a, z, j).setInt(m.at(i).getInt());
    ok(""+m, """
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        28                                      S
   2 V        0         4                                  0     z
   3 V        4         4                                  1     i
   4 V        8         4                                  2     j
   5 A       12        16          4                             A
   6 V       12         4               0                  0       a
   7 V       16         4               1                  0       a
   8 V       20         4               2                  1       a
   9 V       24         4               3                  0       a
""");
    ok(m.at(a, z, j).getOff(), 20);
    m.at(z).setInt(4);                                                          // Rebase
    ok(m.at(a, z, i).getInt(), 1);
    ok(m.at(a, z, j).getInt(), 0);
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_get_set();
    test_boolean();
    test_copy();
    test_base();
    test_based_array();
    test_move();
    test_set_inc_dec_get();
    test_boolean_constant();
   }

  static void newTests()                                                        // Tests being worked on
   {oldTests();
    test_addressing();
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
