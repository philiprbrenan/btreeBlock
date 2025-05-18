//------------------------------------------------------------------------------
// Memory layout with base being global
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Memory layout

import java.util.*;

class MemoryLayout extends Test                                                 // Memory layout
 {Layout layout;                                                                // Layout of part of memory
  Memory memory;                                                                // Memory containing layout
  int      base;                                                                // Base of layout in memory - like located in Pl1
  boolean debug;                                                                // Debug if true
  String   name;                                                                // Name of the memory layout if supplied

  MemoryLayout() {zz();}                                                        // Constructor

//D1 Control                                                                    // Testing, control and integrity

  void memory(Memory Memory) {memory = Memory;}                                 // Set the base of the layout in memory allowing such layouts to be relocated
  void layout(Layout Layout) {layout = Layout;}                                 // Set the base of the layout in memory allowing such layouts to be relocated
  void base  (int Base)      {base   = Base;}                                   // Set the base of the layout in memory allowing such layouts to be relocated

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
   {z(); return new     At(field,       indices).setOff().result;
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
    enum Type {constant, direct, indirect};                                     // Type of memory reference. Constant a known constant value. Direct: a field with indioces known at compile time. Indirect: a field whose indices are other fields whose indices are known at compile time.
    final Type type;                                                            // Type of memory reference
    final int[]indices;                                                         // Known indices to be applied directly to locate the field in memory
    final At[] directs;                                                         // Fields whose location is known at the start so they can be used for indices into memory rather like registers on a chip
    int  delta;                                                                 // Delta due to indices
    int  at;                                                                    // Location in memory
    int  result;                                                                // The contents of memory at this location

    At(int constant)                                                            // Constant value made to look like a memory reference
     {zz(); field = null; indices = null; width = delta = at = 0;
      directs = null;
      type = Type.constant; result = constant;
     }

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

    At(Layout.Field Field)                                                      // No indices or base
     {z();
      field = Field; indices = new int[0]; width = field.width;
      type = Type.direct; directs = null;
      locateDirectAddress();                                                    // The indices are constant so the address will not change over time
     }

    At(Layout.Field Field, int...Indices)                                       // Constant indices used for setting initial values
     {z();
      field = Field; indices = Indices; width = field.width;
      type = Type.direct; directs = null;
      locateDirectAddress();                                                    // The indices are constant so the address will not change over time
     }

    At(Layout.Field Field, At...Directs)                                        // Variable indices used for obtaining run time values
     {z();
      for (int i = 0; i < Directs.length; i++)
       {if (Directs[i].type != Type.direct) stop("Index:", i, "must not have a base or indices");
       }

      field = Field; width = field.width;
      type = Type.indirect; directs = Directs;
      indices = new int[Directs.length];
     }

    boolean sameSize(At b)                                                      // Check two fields are the same size
     {if (field == null) return true;                                           // Constants match any size
      z(); field.sameSize(b.field);
      z(); return true;
     }

    int width()                                                                 // Width of the field in memory
     {z(); if (type == Type.constant) stop("A constant does not have any specific width");
      z(); return field.width();
     }

    At setOff()                                                                 // Set the base address of the field
     {z();
      if (type == Type.indirect) {z(); locateInDirectAddress();}                // Evaluate indirect indices
      else                       {z(); locateDirectAddress();}                  // Evaluate direct indices
      return this;
     }

    boolean getBit(int i)            {return memory.getBit(at+i);}              // Get a bit from memory assuming that setOff() has been called to fix the location of the field containing the bit
    void    setBit(int i, boolean b) {       memory.set(at+i, b);}              // Set a bit in memory  assuming that setOff() has been called to fix the location of the field containing the bit

    int  getInt()          {z(); return result;}                                // The value in memory, at the indicated location, treated as an integer or the value of the constant
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

    MemoryLayout ml() {return MemoryLayout.this;}                               // Containing memory layout

//D2 Move                                                                       // Copy data between memory locations

    void move(At source)                                                        // Copy the specified number of bits from source to target assuming no overlap. The source and target can be in the same or a different memory.
     {z(); sameSize(source);
      for(int i = 0; i < width; ++i)
       {z();
        final boolean b = source.getBit(i);
        setBit(i, b);
       }
     }

    void move(At source, At buffer)                                             // Copy the specified number of bits from source to target via a buffer to allow the operation to proceed in bit parallel assuming the buffer does not overlap either the source or target, each of which can be in different memories.
     {z(); sameSize(source); sameSize(buffer);
      buffer.move(source);
      move(buffer);
     }

    void moveUp(At Index, At buffer)                                            // Move the elements of an array up one position deleting the last element.  A buffer of the same size is used to permit copy in parallel.
     {z(); sameSize(buffer);
      if (!(field instanceof Layout.Array))  stop("Array required for moveUp");
      z(); setOff();                                                            // Set the offset of the source/target
      z(); buffer.setOff().move(this);                                          // Set the offset of the buffer and copies the source

      final Layout.Array A = field.toArray();                                   // Address field to be moved as an array
      final  int w =     A.element.width;                                       // Width of array element
      for   (int i = Index.result+1; i < A.size; i++)                           // Each element
       {for (int j = 0;              j < w;      j++)                           // Each bit in each element
         {final boolean b = buffer.getBit((i-1)*w + j);
          setBit(i*w+j, b);
         }
       }
     }

    void moveDown(At Index, At buffer)                                          // Move the elements of an array down one position deleting the indexed element.  A buffer of the same size is used to permit copy in parallel.
     {z(); sameSize(buffer);
      if (!(field instanceof Layout.Array)) stop("Array required for moveDown");
      z(); setOff();                                                            // Set the offset of the target
      z(); buffer.setOff().move(this);                                          // Set the offset of the buffer and copies the source

      final Layout.Array A = field.toArray();                                   // Address field to be moved as an array
      final  int w =     A.element.width;                                       // Width of array element
      for   (int i = Index.result; i < A.size-1; i++)                           // Each element
       {for (int j = 0;            j < w;        j++)                           // Each bit in each element
         {final boolean b = buffer.getBit((i+1)*w + j);
          setBit(i*w+j, b);
         }
       }
     }

//D2 Bits                                                                       // Bit operations in a memory.

    void zero()                                                                 // Zero some memory
     {z(); memory.zero(at, width);
     }

    void ones()                                                                 // Ones some memory
     {z(); memory.ones(at, width);
     }

    void invert(At a)                                                           // Invert the specified bits
     {z(); memory.invert(a.result, a.width());
     }

    boolean isAllZero()                                                         // Check that the specified memory is all zeros
     {z(); return memory.isAllZero(at, width);
     }

    boolean isAllOnes()                                                         // Check that  the specified memory is all ones
     {z(); return memory.isAllOnes(at, width);
     }

//D1 Boolean                                                                    // Boolean operations on fields held in memories.

    boolean isZero()                                                            // Whether the field is all zero
     {z();
      for(int i = 0; i < width; ++i)
       {z(); if (getBit(i)) {z(); return false;}
       }
      z(); return true;
     }

    boolean isOnes()                                                            // Whether the field is all ones
     {z();
      for(int i = 0; i < width; ++i)
       {z(); if (!getBit(i)) {z(); return false;}
       }
      z(); return true;
     }

    boolean equal(At b)                                                         // Whether  a == b
     {z();
      if (field == null || b.field == null)
       {z(); return result == b.result;
       }

      z(); sameSize(b);

      for(int i = 0; i < width; ++i)
       {z(); if (getBit(i) != b.getBit(i)) {z(); return false;}
       }
      z(); return true;
     }

    void    equal(At b, At result)                                              // Whether  a == b
     {z();
      result.setInt(equal(b) ? 1 : 0);
     }

    boolean notEqual(At b) {return !equal(b);}                                  // Whether a != b

    void    notEqual(At b, At result)                                           // Whether  a != b
     {z();
      result.setInt(equal(b) ? 1 : 0);
     }

    boolean lessThan(At b)                                                      // Whether a < b
     {z();
      if (field == null || b.field == null) {z(); return result <  b.result;}
      z(); sameSize(b);

      for(int i = width; i > 0; --i)
       {z();
        if (!getBit(i-1) &&  b.getBit(i-1)) {z(); return true;}
        if ( getBit(i-1) && !b.getBit(i-1)) {z(); return false;}
       }
       z(); return false;
     }

    void    lessThan(At b, At result)                                           // Whether  a < b
     {z();
      result.setInt(lessThan(b) ? 1 : 0);
     }

    boolean lessThanOrEqual(At b)                                               // Whether a <= b
     {z();
      if (field == null || b.field == null) {z(); return result <= b.result;}
      z(); sameSize(b);
      return lessThan(b) || equal(b);
     }

    void    lessThanOrEqual(At b, At result)                                    // Whether  a <= b
     {z();
      result.setInt(lessThanOrEqual(b) ? 1 : 0);
     }

    boolean greaterThan(At b)                                                   // Whether a > b
     {z();
      if (field == null || b.field == null) {z(); return result > b.result;}
      z(); sameSize(b);
      return !lessThan(b) && !equal(b);
     }

    void    greaterThan(At b, At result)                                        // Whether  a > b
     {z();
      result.setInt(greaterThan(b) ? 1 : 0);
     }

    boolean greaterThanOrEqual(At b)                                            // Whether a >= b
     {z();
      return !lessThan(b);
     }

    void    greaterThanOrEqual(At b, At result)                                 // Whether  a >= b
     {z();
      result.setInt(greaterThanOrEqual(b) ? 1 : 0);
     }

//D1 Arithmetic                                                                 // Arithmetic on integers

//D2 Binary                                                                     // Arithmetic on binary integers

    int inc()     {z(); final int i = getInt()+1; setInt(i);   return i;}       // Increment a variable treated as an signed binary integer with wrap around on overflow.  Return the result after  the increment.
    int dec()     {z(); final int i = getInt()-1; setInt(i);   return i;}       // Decrement a variable treated as an signed binary integer with wrap around on underflow. Return the result after  the decrement.
    int incPost() {z(); final int i = getInt();   setInt(i+1); return i;}       // Increment a variable treated as an signed binary integer with wrap around on overflow.  Return the result before the increment.
    int decPost() {z(); final int i = getInt();   setInt(i-1); return i;}       // Decrement a variable treated as an signed binary integer with wrap around on underflow. Return the result before the decrement.
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

  class Constant extends At                                                     // A constant integer
   {Constant(int constant)
     {super(constant);
     }
   }

  Constant constant(int constant)
   {return new Constant(constant);
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
    if (memory.name != null) pp.s.append("Memory: "+memory.name+"\n");
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

    pp.bits += switch(field)                                                    // Width
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
      case Layout.Union u ->                                                    // Union
       {final int end = pp.bits;                                                // Save end point of union
        for (int i = 0; i < u.fields.length; i++)
         {pp.bits = start;                                                      // Restore the start point for each field of the union
          pp.line++;
          print(u.fields[i], pp, indent+1);
          pp.bits = max(pp.bits, end);                                          // Restore end point of union
         }
       }
      case Layout.Structure s ->                                                // Structure
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
      M = new MemoryLayout();
      M.memory(new Memory("ML", l.size()));
      M.layout(l);
      M.base(0);
      M.memory.alternating(4);
     }
   }

  static void test_get_set()
   {TestMemoryLayout t = new TestMemoryLayout();
        MemoryLayout m = t.M;
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
        MemoryLayout m = t.M;
    m.setInt(t.a, 1, 0, 1, 1);
    m.setInt(t.a, 2, 0, 1, 2);
    m.setInt(t.a, 1, 0, 2, 1);
    m.setInt(t.a, 2, 0, 2, 2);

    ok( m.at(t.a, 0, 1, 1).equal   (m.at(t.a, 0, 2, 1)));
    ok(!m.at(t.a, 0, 1, 1).equal   (m.at(t.a, 0, 1, 2)));
    ok(!m.at(t.a, 0, 1, 1).notEqual(m.at(t.a, 0, 2, 1)));
    ok( m.at(t.a, 0, 1, 1).notEqual(m.at(t.a, 0, 1, 2)));

    ok( m.at(t.a, 0, 1, 1).lessThan       (m.at(t.a, 0, 1, 2)));
    ok(!m.at(t.a, 0, 1, 1).lessThan       (m.at(t.a, 0, 2, 1)));
    ok( m.at(t.a, 0, 1, 1).lessThanOrEqual(m.at(t.a, 0, 1, 2)));
    ok( m.at(t.a, 0, 1, 1).lessThanOrEqual(m.at(t.a, 0, 2, 1)));
    ok(!m.at(t.a, 0, 1, 2).lessThanOrEqual(m.at(t.a, 0, 2, 1)));

    ok(!m.at(t.a, 0, 1, 1).greaterThan       (m.at(t.a, 0, 1, 2)));
    ok( m.at(t.a, 0, 1, 2).greaterThan       (m.at(t.a, 0, 1, 1)));
    ok( m.at(t.a, 0, 2, 1).greaterThanOrEqual(m.at(t.a, 0, 1, 1)));
    ok( m.at(t.a, 0, 1, 1).greaterThanOrEqual(m.at(t.a, 0, 2, 1)));
    ok(!m.at(t.a, 0, 2, 1).greaterThanOrEqual(m.at(t.a, 0, 1, 2)));
   }

  static void test_copy()
   {TestMemoryLayout t = new TestMemoryLayout();
        MemoryLayout m = t.M;
    m.setInt(t.a, 1, 0, 0, 0);
    m.setInt(t.a, 2, 0, 0, 1);
    m.setInt(t.a, 3, 0, 0, 2);

    ok(m.at(t.a, 0, 0, 0), "a[0,0,0](0+4)4=1");
    ok(m.at(t.a, 0, 0, 1), "a[0,0,1](0+16)16=2");
    ok(m.at(t.a, 0, 0, 2), "a[0,0,2](0+28)28=3");

    m.at(t.a, 0, 0, 1).move(m.at(t.a, 0, 0, 0));
    m.at(t.a, 0, 0, 1).move(m.at(t.a, 0, 0, 1));

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

    MemoryLayout     m = new MemoryLayout();
    m.layout(l);
    m.memory(new Memory("ML", 2*N));
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
    MemoryLayout     m = new MemoryLayout();
    m.layout(l);
    m.memory(new Memory("ML", l.size()+B));
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

    MemoryLayout     m = new MemoryLayout();
    m.layout(l);
    m.memory(new Memory("ML", l.size()));
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

  static void test_move_across()
   {Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 4);
    Layout.Variable  b = l.variable ("b", 4);
    Layout.Structure s = l.structure("s", a, b);
    l.compile();

    Layout           l1 = Layout.layout();
    Layout.Field     s1 = l1.duplicate("s", l);
    l1.compile();

    Layout           l2 = Layout.layout();
    Layout.Field     s2 = l2.duplicate("s", l);
    l2.compile();

    MemoryLayout     m1 = new MemoryLayout();
    m1.layout(l1);
    m1.memory(new Memory("ML", l1.size()));

    MemoryLayout     m2 = new MemoryLayout();
    m2.layout(l2);
    m2.memory(new Memory("ML", l2.size()));
    m1.at(l1.get("a")).setInt(1);
    m2.at(l2.get("b")).move(m1.at(l1.get("a")));
    //stop(m1);
    m1.ok("""
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0         8                                      s
   2 V        0         4                                  1     a
   3 V        4         4                                  0     b
""");
    //stop(m2);
    m2.ok("""
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0         8                                      s
   2 V        0         4                                  0     a
   3 V        4         4                                  1     b
""");
   }

  static void test_set_inc_dec_get()
   {Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 4);
    Layout.Variable  b = l.variable ("b", 4);
    Layout.Structure s = l.structure("s", a, b);
    l.compile();

    MemoryLayout     m = new MemoryLayout();
    m.layout(l);
    m.memory(new Memory("ML", l.size()));

    m.at(a).setInt(1);
    m.at(b).setInt(3);
    //stop(m);
    m.ok("""
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0         8                                      s
   2 V        0         4                                  1     a
   3 V        4         4                                  3     b
""");
    ok(m.new At(a).inc(), 2);
    ok(m.new At(b).dec(), 2);

    //stop(m);
    m.ok("""
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0         8                                      s
   2 V        0         4                                  2     a
   3 V        4         4                                  2     b
""");

    ok(m.at(a).getInt(), 2);
    ok(m.at(b).getInt(), 2);

    ok(m.new At(a).decPost(), 2);
    ok(m.new At(b).incPost(), 2);

    ok(m.at(a).getInt(), 1);
    ok(m.at(b).getInt(), 3);
   }

  static void test_boolean_constant()
   {Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 4);
    l.compile();

    MemoryLayout     m = new MemoryLayout();
    m.layout(l);
    m.memory(new Memory("ML", l.size()));
    m.at(a).setInt(1);

    MemoryLayout.At A = m.at(a),
     c0 = m.constant(0), c1 = m.constant(1), c2 = m.constant(2);
    final boolean T = true, F = false;

    ok(A.equal   (c0), F); ok(A.equal   (c1), T); ok(A.equal   (c2), F);
    ok(A.notEqual(c0), T); ok(A.notEqual(c1), F); ok(A.notEqual(c2), T);

    ok(A.lessThan       (c0), F); ok(A.lessThan       (c1), F); ok(A.lessThan       (c2), T);
    ok(A.lessThanOrEqual(c0), F); ok(A.lessThanOrEqual(c1), T); ok(A.lessThanOrEqual(c2), T);

    ok(A.greaterThan       (c0), T); ok(A.greaterThan       (c1), F); ok(A.greaterThan       (c2), F);
    ok(A.greaterThanOrEqual(c0), T); ok(A.greaterThanOrEqual(c1), T); ok(A.greaterThanOrEqual(c2), F);
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

    MemoryLayout     m = new MemoryLayout();
    m.layout(l);
    m.memory(new Memory("ML", l.size()));

    MemoryLayout.At  Z = m.at(z), I = m.at(i), J = m.at(j);
    Z.setOff().setInt(0);
    I.setOff().setInt(1);
    J.setOff().setInt(2);

    MemoryLayout.At az = m.at(a, Z);
    MemoryLayout.At ai = m.at(a, I);
    MemoryLayout.At aj = m.at(a, J);

    az.setOff().setInt(10);
    ai.setOff().setInt(11);
    aj.setOff().setInt(12);

    //stop(m.memory);
    ok(""+m.memory, """
Memory: ML
      4... 4... 4... 4... 3... 3... 3... 3... 2... 2... 2... 2... 1... 1... 1... 1...
Line  FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210
   0  0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0c0b 0a02 0100
""");

    //stop(m);
    ok(""+m, """
Memory: ML
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
Memory: ML
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

  static void test_move_up()
   {final int        N = 8;
    Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", N);
    Layout.Array     A = l.array    ("A", a, 6);
    l.compile();

    Layout           w = Layout.layout();
    Layout.Variable  z = w.variable ("z", N);
    Layout.Variable  i = w.variable ("i", N);
    Layout.Field     B = w.duplicate("A", l);
    Layout.Structure S = w.structure("S", z, i, B);
    w.compile();

    MemoryLayout     L = new MemoryLayout();
    L.layout(l);
    L.memory(new Memory("ML", l.size()));
    MemoryLayout     W = new MemoryLayout();
    W.layout(w);
    W.memory(new Memory("ML", w.size()));

    for (int j = 0; j < A.size; j++) L.at(a, j).setInt(10+j);

    W.at(i).setInt(2);
    L.at(A).moveUp(W.at(i), W.at(B));
  //L.at(A, W.at(z)).moveUp(W.at(i), W.at(B, W.at(z)));
  //stop(L);
    ok(L, """
Line T       At      Wide       Size    Indices        Value   Name
   1 A        0        48          6                           A
   2 V        0         8               0                 10     a
   3 V        8         8               1                 11     a
   4 V       16         8               2                 12     a
   5 V       24         8               3                 12     a
   6 V       32         8               4                 13     a
   7 V       40         8               5                 14     a
""");

  //stop(W);
  ok(W, """
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        64                                      S
   2 V        0         8                                  0     z
   3 V        8         8                                  2     i
   4 A       16        48          6                             A
   5 V       16         8               0                 10       a
   6 V       24         8               1                 11       a
   7 V       32         8               2                 12       a
   8 V       40         8               3                 13       a
   9 V       48         8               4                 14       a
  10 V       56         8               5                 15       a
""");
   }

  static void test_move_down()
   {final int        N = 8;
    Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", N);
    Layout.Array     A = l.array    ("A", a, 6);
    l.compile();

    Layout           w = Layout.layout();
    Layout.Variable  z = w.variable ("z", N);
    Layout.Variable  i = w.variable ("i", N);
    Layout.Field     B = w.duplicate("A", l);
    Layout.Structure S = w.structure("S", z, i, B);
    w.compile();

    MemoryLayout     L = new MemoryLayout();
    L.layout(l);
    L.memory(new Memory("ML", l.size()));
    MemoryLayout     W = new MemoryLayout();
    W.layout(w);
    W.memory(new Memory("ML", w.size()));

    for (int j = 0; j < A.size; j++) L.at(a, j).setInt(10+j);

    W.at(i).setInt(2);
    L.at(A).moveDown(W.at(i), W.at(B));
  //L.at(A, W.at(z)).moveUp(W.at(i), W.at(B, W.at(z)));
    //stop(L);
    ok(L, """
Line T       At      Wide       Size    Indices        Value   Name
   1 A        0        48          6                           A
   2 V        0         8               0                 10     a
   3 V        8         8               1                 11     a
   4 V       16         8               2                 13     a
   5 V       24         8               3                 14     a
   6 V       32         8               4                 15     a
   7 V       40         8               5                 15     a
""");

  //stop(W);
  ok(W, """
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        64                                      S
   2 V        0         8                                  0     z
   3 V        8         8                                  2     i
   4 A       16        48          6                             A
   5 V       16         8               0                 10       a
   6 V       24         8               1                 11       a
   7 V       32         8               2                 12       a
   8 V       40         8               3                 13       a
   9 V       48         8               4                 14       a
  10 V       56         8               5                 15       a
""");
   }

  static void test_zero()
   {Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 4);
    Layout.Array     A = l.array    ("A", a, 6);
    l.compile();

    MemoryLayout     L = new MemoryLayout();
    L.layout(l);
    L.memory(new Memory("ML", l.size()+16));
    L.base(16);

    L.memory.alternating(4);
    //stop(L.memory);
    ok(L.memory, """
Memory: ML
      4... 4... 4... 4... 3... 3... 3... 3... 2... 2... 2... 2... 1... 1... 1... 1...
Line  FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210
   0  0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 00f0 f0f0 f0f0
""");

    //stop(L);
    ok(L, """
Memory: ML
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
Memory: ML
      4... 4... 4... 4... 3... 3... 3... 3... 2... 2... 2... 2... 1... 1... 1... 1...
Line  FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210
   0  0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 f0f0
""");

    //stop(L);
    ok(L, """
Memory: ML
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
    Layout.Variable  r = l.bit      ("c");
    Layout.Structure s = l.structure("s", a, b, r);
    l.compile();

    MemoryLayout     m = new MemoryLayout();
    m.layout(l);
    m.memory(new Memory("ML", l.size()));

    m.at(a).setInt(1);
    m.at(b).setInt(2);

    m.at(a).lessThan(m.at(b), m.at(r)); ok(m.at(r).getInt(), 1);
    m.at(b).lessThan(m.at(a), m.at(r)); ok(m.at(r).getInt(), 0);
   }

  static void test_is_ones_or_zeros()
   {Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 4);
    Layout.Variable  b = l.variable ("b", 4);
    Layout.Variable  c = l.variable ("c", 4);
    Layout.Structure s = l.structure("s", a, b, c);
    l.compile();

    MemoryLayout     m = new MemoryLayout();
    m.layout(l);
    m.memory(new Memory("ML", l.size()));

    m.at(a).zero();
    m.at(b).setInt(2);
    m.at(c).ones();

    ok(m, """
Memory: ML
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        12                                      s
   2 V        0         4                                  0     a
   3 V        4         4                                  2     b
   4 V        8         4                                 15     c
""");

    ok( m.at(a).isZero()); ok(!m.at(a).isOnes());
    ok(!m.at(b).isZero()); ok(!m.at(b).isOnes());
    ok(!m.at(c).isZero()); ok( m.at(c).isOnes());
   }

  static void test_not_compiled()
   {Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 4);
    Layout.Variable  b = l.variable ("b", 4);
    Layout.Variable  c = l.variable ("c", 4);
    Layout.Structure s = l.structure("s", a, b, c);
    MemoryLayout     m = new MemoryLayout();

    sayThisOrStop("Field: a has not been compiled yet");

    try {m.at(a).zero();} catch(Exception e) {}
    m.layout(l.compile());
    m.memory(new Memory("ML", l.size()));
    m.at(a).zero();
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
    l.compile();

    MemoryLayout  m = new MemoryLayout();
    m.layout(l);
    m.memory(new Memory("ML", l.size()));

    m.at(a, 0).ones();  m.at(a, 1).ones();    m.at(a, 2).ones();    m.at(a, 3).ones();
    m.at(b, 0).zero();  m.at(b, 1).zero();    m.at(b, 2).zero();    m.at(b, 3).zero();
    m.at(c, 0).ones();  m.at(c, 1).ones();    m.at(c, 2).ones();    m.at(c, 3).ones();

    //stop(m);
    ok(m, """
Memory: ML
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

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_get_set();
    test_boolean();
    test_copy();
    test_base();
    test_based_array();
    test_move();
    //test_move_across();
    test_set_inc_dec_get();
    test_boolean_constant();
    test_addressing();
    //test_move_up();
    //test_move_down();
    test_zero();
    test_boolean_result();
    test_is_ones_or_zeros();
    test_not_compiled();
    test_union();
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
