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
  boolean debug;                                                                // Debug if true

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

  void zero()                                                                   // Clear the memory associated with the layout to zeros
   {z();
    memory.set(0, memory.size(), 0);
   }

//D1 Components                                                                 // Locate a variable in memory via its indices

  class At
   {final Layout.Field field;                                                   // Field description in layout
    final int  width;                                                           // Width of element in memory
    enum Type {constant, direct, indirect};                                     // Type of memory reference. Constant a known constant value. Direct: a field with indioces known at compile time. Indirect: a field whose indices are other fields whose indices are known at compile time.
    final Type type;                                                            // Type of memory reference
          int     base;                                                         // Base address of memory
    final int[]indices;                                                         // Known indices to be applied directly to locate the field in memory
          At baseField;                                                         // A field who gives is the base address in memory of the data structure described by the layout
    final At[] directs;                                                         // Fields whose location is known at the start so they can be used for indices into memory rather like registers on a chip
    int  delta;                                                                 // Delta due to indices
    int  at;                                                                    // Location in memory
    int  result;                                                                // The contents of memory at this location

    At(int constant)                                                            // Constant value made to look like a memory reference
     {z(); field = null; indices = null; width = base = delta = at = 0;
      directs = null;
      type = Type.constant; result = constant;
     }

    void locateDirectAddress()                                                  // Locate a direct address and its content
     {delta  = field.locator.at(indices);
      at     = base + delta;
      result = memory.getInt(at, width);
     }

    void locateInDirectAddress()                                                // Locate an indirect address and its content
     {for (int i = 0; i < directs.length; i++)
       {indices[i] = directs[i].getInt();
       }
      base = baseField != null ? baseField.getInt() : 0;                        // An empty base address is resolved as address zero
      locateDirectAddress();                                                    // Locate the address directly now that its indices are known
     }

    At(Layout.Field Field)                                                      // No indices or base
     {z(); field = Field; indices = new int[0]; width = field.width; base = 0;
      type = Type.direct; directs = null;
      locateDirectAddress();                                                    // The indices are constant so the address will not change over time
     }

    At(Layout.Field Field, int Base, int...Indices)                             // Constant indices used for setting initial values
     {z(); field = Field; indices = Indices; width = field.width; base = Base;
      type = Type.direct; directs = null;
      locateDirectAddress();                                                    // The indices are constant so the address will not change over time
     }
    At(Layout.Field Field, At BaseField, At...Directs)                          // Variable indices used for obtaining run time values
     {z();
      if (BaseField != null && BaseField.type != Type.direct)                   // Check that the base field, if present, does have recursive dependencies
       {stop("Base field must not have a base or indices");
       }
      for (int i = 0; i < Directs.length; i++)
       {if (Directs[i].type != Type.direct) stop("Index:", i, "must not have a base or indices");
       }

      field = Field; width = field.width; baseField = BaseField;
      type = Type.indirect; directs = Directs;
      indices = new int[Directs.length];
     }

    boolean sameSize(At b)                                                      // Check two fields are the same size
     {if (field == null) return true;                                           // Constants match any size
      z(); field.sameSize(b.field);
      //z(); if (MemoryLayout.this != b.ml()) stop("Different memory layout");
      z(); return true;
     }

    int width()                                                                 // Width of the field in memory
     {z(); if (type == Type.constant) stop("A constant does not have any specific width");
      z(); return field.width();
     }

    At setOff()                                                                 // Set the base address of the field
     {z();
      if (type == Type.indirect) {z(); locateInDirectAddress();}                // Evaluate indirect indices
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

//D2 Search                                                                     // Search an array

//    void findEqual(At key, At limit, At found)                                  // Search an array up to a specified limit for a element that is equal to the supplied search key. Found is set to 1 if the key is found else 0.  The index of the located key is set in limit.
//     {z(); sameSize(buffer);
//      if (!(field instanceof Layout.Array)) stop("Array required for findEqual");
//      Layout.Array A = field.toArray();                                         // The array to be searched
//      final int w = A.element.width;                                            // Width of array element
//      z(); setOff();                                                            // Set the offset of the array to be searched
//      z(); key.setOff();                                                        // Set the offset of the key to search for
//      z(); limit.setOff();                                                      // Set the offset of the index limititing the smount of array to search
//
//      boolean looking = true;
//      final int j = min(w, limit.result);                                       // Upper limit of search
//      int i;
//      for (i = 0; i < j && looking; i++)                                        // Search
//       {z(); key(); if ( == search) {z(); looking = false; break;}
//       }
//      found.setInt(looking ? 0 : 1);                                            // Show result
//      if (found) {z(); limit.setInt(i);}                                        // Save index of result
//     }

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
   }

  At at(Layout.Field Field)                                                     // A field without indices or base addressing
   {return new At(Field);
   }

  At at(Layout.Field Field, int Base, int...Indices)                            // A field with constant base and constant indices
   {return new At(Field, Base, Indices);
   }

  At at(Layout.Field Field, At Base, At...Indices)                              // A field with a variable base and variable indices, the abse and each variable index being a field with no base and no indices
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
   ok(m.at(t.c,      0, 0, 0, 0), "c[0,0,0](0+12)12=15");
   m.setInt(t.c, 11, 0, 0, 0, 0);
   ok(m.at(t.c,      0, 0, 0, 0), "c[0,0,0](0+12)12=11");

   ok(m.at(t.c,      0, 0, 0, 1), "c[0,0,1](0+24)24=0");
   m.setInt(t.c, 11, 0, 0, 0, 1);
   ok(m.at(t.c,      0, 0, 0, 1), "c[0,0,1](0+24)24=11");

   ok(m.at(t.a,      0, 0, 2, 2), "a[0,2,2](0+116)116=15");
   m.setInt(t.a,  5, 0, 0, 2, 2);
   ok(m.at(t.a,      0, 0, 2, 2), "a[0,2,2](0+116)116=5");

   ok(m.at(t.b,      0, 1, 2, 2), "b[1,2,2](0+252)252=15");
   m.setInt(t.b,  7, 0, 1, 2, 2);
   ok(m.at(t.b,      0, 1, 2, 2), "b[1,2,2](0+252)252=7");

    ok(m.at(t.e,      0, 1, 2), "e[1,2](0+260)260=15");
    m.setInt(t.e, 11, 0, 1, 2);
    ok(m.at(t.e,      0, 1, 2), "e[1,2](0+260)260=11");
   }

  static void test_boolean()
   {TestMemoryLayout t = new TestMemoryLayout();
        MemoryLayout m = t.M;
    m.setInt(t.a, 1, 0, 0, 1, 1);
    m.setInt(t.a, 2, 0, 0, 1, 2);
    m.setInt(t.a, 1, 0, 0, 2, 1);
    m.setInt(t.a, 2, 0, 0, 2, 2);

    ok( m.at(t.a, 0, 0, 1, 1).equal   (m.at(t.a, 0, 0, 2, 1)));
    ok(!m.at(t.a, 0, 0, 1, 1).equal   (m.at(t.a, 0, 0, 1, 2)));
    ok(!m.at(t.a, 0, 0, 1, 1).notEqual(m.at(t.a, 0, 0, 2, 1)));
    ok( m.at(t.a, 0, 0, 1, 1).notEqual(m.at(t.a, 0, 0, 1, 2)));

    ok( m.at(t.a, 0, 0, 1, 1).lessThan       (m.at(t.a, 0, 0, 1, 2)));
    ok(!m.at(t.a, 0, 0, 1, 1).lessThan       (m.at(t.a, 0, 0, 2, 1)));
    ok( m.at(t.a, 0, 0, 1, 1).lessThanOrEqual(m.at(t.a, 0, 0, 1, 2)));
    ok( m.at(t.a, 0, 0, 1, 1).lessThanOrEqual(m.at(t.a, 0, 0, 2, 1)));
    ok(!m.at(t.a, 0, 0, 1, 2).lessThanOrEqual(m.at(t.a, 0, 0, 2, 1)));

    ok(!m.at(t.a, 0, 0, 1, 1).greaterThan       (m.at(t.a, 0, 0, 1, 2)));
    ok( m.at(t.a, 0, 0, 1, 2).greaterThan       (m.at(t.a, 0, 0, 1, 1)));
    ok( m.at(t.a, 0, 0, 2, 1).greaterThanOrEqual(m.at(t.a, 0, 0, 1, 1)));
    ok( m.at(t.a, 0, 0, 1, 1).greaterThanOrEqual(m.at(t.a, 0, 0, 2, 1)));
    ok(!m.at(t.a, 0, 0, 2, 1).greaterThanOrEqual(m.at(t.a, 0, 0, 1, 2)));
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

    m.at(t.a, 0, 0, 0, 1).move(m.at(t.a, 0, 0, 0, 0));
    m.at(t.a, 4, 0, 0, 1).move(m.at(t.a, 0, 0, 0, 1));

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

    MemoryLayout     m1 = new MemoryLayout(l1);
    MemoryLayout     m2 = new MemoryLayout(l2);
    m1.at(a).setInt(1);
    m2.at(b).move(m1.at(a));
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

    MemoryLayout     m = new MemoryLayout(l);
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
   {final int N = 4;
    Layout           l = Layout.layout();
    Layout.Variable  z = l.variable ("z", N);
    Layout.Variable  i = l.variable ("i", N);
    Layout.Variable  j = l.variable ("j", N);
    Layout.Variable  a = l.variable ("a", N);
    Layout.Array     A = l.array    ("A", a, N);
    Layout.Structure S = l.structure("S", z, i, j, A);
    l.compile();

    MemoryLayout     m = new MemoryLayout(l);
    m.at(i).setInt(1);
    m.at(j).setInt(2);
    MemoryLayout.At I = m.at(i), J = m.at(j);
    m.at(a, null, J).setOff().setInt(I.getInt());
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
    ok(m.at(a, null, m.at(j)).setOff().getInt(), 1);
    m.at(z).setInt(N);                                                          // Rebase by the number of bits in one element of the array
    ok(m.at(a, m.at(z), m.at(i)).setOff().getInt(), 1);                         // Set element now appears one element lower
    ok(m.at(a, m.at(z), m.at(j)).setOff().getInt(), 0);
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

    MemoryLayout     L = new MemoryLayout(l);
    MemoryLayout     W = new MemoryLayout(w);
    for (int j = 0; j < A.size; j++) L.at(a, 0, j).setInt(10+j);

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

    MemoryLayout     L = new MemoryLayout(l);
    MemoryLayout     W = new MemoryLayout(w);
    for (int j = 0; j < A.size; j++) L.at(a, 0, j).setInt(10+j);

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

    MemoryLayout     L = new MemoryLayout(l);
    L.memory.alternating(4);
    //stop(L);
    ok(L, """
Line T       At      Wide       Size    Indices        Value   Name
   1 A        0        24          6                           A
   2 V        0         4               0                  0     a
   3 V        4         4               1                 15     a
   4 V        8         4               2                  0     a
   5 V       12         4               3                 15     a
   6 V       16         4               4                  0     a
   7 V       20         4               5                 15     a
""");

    L.zero();
    //stop(L);
    ok(L, """
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
    Layout.Variable  r = l.bit      ("c");
    Layout.Structure s = l.structure("s", a, b, r);
    l.compile();

    MemoryLayout     m = new MemoryLayout(l);

    m.at(a).setInt(1);
    m.at(b).setInt(2);

    m.at(a).lessThan(m.at(b), m.at(r)); ok( m.at(r).getInt(), 1);
    m.at(b).lessThan(m.at(a), m.at(r)); ok( m.at(r).getInt(), 0);
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_get_set();
    test_boolean();
    test_copy();
    test_base();
    test_based_array();
    test_move();
    test_move_across();
    test_set_inc_dec_get();
    test_boolean_constant();
    test_addressing();
    test_move_up();
    test_move_down();
    test_zero();
    test_boolean_result();
   }

  static void newTests()                                                        // Tests being worked on
   {oldTests();
    test_boolean_result();
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
