//------------------------------------------------------------------------------
// Layout and address memory
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Simulate a silicon chip.

import java.util.*;

//D1 Construct                                                                  // Describe the layout of the memory used by the chip.  All memory is laid out it in little endian format.

public class Layout extends Test                                                // A memory layout for a chip. There might be several such layouts representing parts of the chip.
 {private Field                 top;                                            // The top most field in a set of nested fields describing memory.
  private final Stack<Field> fields = new Stack<>();                            // Field creation sequence to enable efficient duplication of a layout
  String                 layoutName;                                            // Name this memory layout if helpful
  static boolean              debug = false;                                    // Debugging when true

  static Layout layout() {return new Layout();}                                 // Create a new Layout that can be loaded field by field

  Layout compile()                                                              // Lay out the layout
   {zz();
    top = fields.lastElement();                                                 // The last defined field becomes the super structure
    top.layout(0, 0);                                                           // Locate field positions
    top.indexNames();                                                           // Index the names of the fields
    for (Field f: fields) f.locator  = new Locator(f);                          // Create a locator for each field
    return this;
   }

  int size() {z(); return top == null ? 0 : top.width;}                         // Size of memory
  void ok(String expected) {Test.ok(top.toString(), expected);}                 // Confirm layout is as expected
  Field top()              {return top;}                                        // Topmost field

  Field get(String path)                                                        // Address a contained field by name
   {final Field f = top.fullNames.get(path);
    if (f == null) stop("No such path:", f, toString());
    return f;
   }

  public String toString() {return top.toString();}                             // Print layout

//D1 Layouts                                                                    // Field memory of the chip as variables, arrays, structures, unions. Dividing the memory in this manner makes it easier to program the chip symbolically.

  abstract class Field                                                          // Variable/Array/Structure/Union definition.
   {String                       name;                                          // Name of field
    int                        number;                                          // Number of field
    String                   fullName2;                                          // Full name of this field
    int                            at;                                          // Offset of field from start of memory
    int                         width;                                          // Number of bits in a field
    int                         depth;                                          // Depth of field - the number of containing arrays/structures/unions above
    Field                          up;                                          // Upward chain to containing array/structure/union
    Locator                   locator;                                          // A locator for this field which includes array indices
    final Map<String,Field> fullNames = new TreeMap<>();                        // Fields by name
    final Set<String>  classification = new TreeSet<>();                        // Names that identify the type of the field to aid debugging

    Field(String Name)                                                          // Create a new named field with a unique number
     {zz(); name = Name; number = fields.size();
      fields.push(this);
     }

    Layout container() {return Layout.this;}                                    // The containing layout

    int at(int...Indices) {zz(); return locator.at(Indices);}                   // Location of field taking into account field indices
    int width()   {z(); return width;}                                          // Size of the memory in bits occupied by this field

    private void fullName(Layout.Field top, StringBuilder s)                    // The full name of a field relative to the indicated top
     {z();
      if (top == this) return;
      z();
      final Stack<String> names = new Stack<>();                                // Full name path
      for(Layout.Field f = this; f.up != null; f = f.up)                        // Go up structure
       {z();
        names.insertElementAt(f.name, 0);
        if (f.up == top) break;
       }
      if (names.size() == 0) return;                                            // Zero length path to element
      z();
      final String        t = names.pop();
      s.append("     ");
      for(String n : names) {z(); s.append(n+".");}
      s.append(t);
     }

    void indexName()                                                            // Index all the full names of a field
     {zz();
      final Stack<String> names = new Stack<>();                                // Full name path
      for(Layout.Field f = this; f.up != null; f = f.up)                        // Go up structure
       {zz();
        names.insertElementAt(f.name, 0);
        final StringBuilder s = new StringBuilder();
        final String top = names.pop();
        for(String n : names) s.append(n+".");
        s.append(top); names.push(top);
        f.up.fullNames.put(s.toString(), this);
       }
     }

    Field get(String path)                                                      // Address a contained field by name
     {z();
      final Field f = fullNames.get(path);
      if (f == null) stop("Cannot find path:"+path);
      return f;
     }

    int sameSize(Field b)                                                       // Check the specified field is the same size as this field
     {zz();
      final int A = width, B = b.width;
      if (A != B) stop("Fields must have the same width, but field", name,
        "has width", A, "while field", b.name, "has size", B);
      return A;
     }

    abstract void layout(int at, int depth);                                    // Layout this field
    abstract void indexNames();                                                 // Set the full names of all the sub fields in a field
    private String  indent() {return "  ".repeat(depth);}                       // Indentation during printing

    protected char fieldType()                                                  // First letter of inner most class name to identify type of field
     {z(); return getClass().getName().split("\\$")[1].charAt(0);
     }

    private StringBuilder header()                                              // Create a string builder with a header preloaded
     {z();
      final StringBuilder s = new StringBuilder();
      if (layoutName != null) s.append("Memory Layout: "+layoutName+"\n");
      s.append(String.format
       ("%1s %4s  %4s  %4s    %-16s       %s\n",
        "T", "At", "Wide", "Size", "Name", "Path"));
      return s;
     }

    protected String printName(Layout.Field top)                                // Print the name of a field showing whether it is a constant and its classification
     {z();
      final StringBuilder s = new StringBuilder();
      s.append(indent());
      s.append(String.format("%-16s", name));
      fullName(top, s);                                                         // Full name for this field
      return s.toString();
     }

    void print(Layout.Field top, StringBuilder s)                               // Print the field
     {z();
      final String n = printName(top);                                          // Name using indentation to show depth
      final char   c = fieldType();                                             // First letter of inner most class name to identify type of field

      s.append(String.format("%c %4d  %4d          %-16s\n",                    // Variable
                             c,  at,  width,   n));
     }

    public String toString()                                                    // Print the field and its sub structure
     {final StringBuilder s = header();
      print(this, s);
      return s.toString().replaceAll("\\s+\n", "\n");
     }

    Bit       toBit      () {z(); return (Bit)      this;}                      // Try to cast a field to a bit
    Variable  toVariable () {z(); return (Variable) this;}                      // Try to cast a field to a variable
    Array     toArray    () {z(); return (Array)    this;}                      // Try to cast a field to an array
    Structure toStructure() {z(); return (Structure)this;}                      // Try to cast a field to a structure
    Union     toUnion    () {z(); return (Union)    this;}                      // Try to cast a field to a union

    String verilogOnes()                                                        // A verilog binary value of all ones the width of the field
     {z();
      return width+"'b"+"1".repeat(width);
     }
   }

  class Variable extends Field                                                  // Layout a variable with no sub structure
   {Variable(String name, int Width)
     {super(name); width = Width;
      zz(); if (width < 1) stop("Field", name, "has no bits");
     }

    void indexNames()                                                           // Index the name of this field
     {zz(); indexName();
     }

    void layout(int At, int Depth)                                              // Layout the variable in the structure
     {zz(); at = At; depth = Depth;
     }
   }

  class Bit extends Variable                                                    // A variable of unit width is a boolean. could have called it a boolean but decied to callit abool instead becuase it was shorter and more like
   {Bit(String name) {super(name, 1); zz();}
   }

  class Array extends Field                                                     // Layout an array definition.  Arrays are of fixed size being that many repitions of their element.
   {int size;                                                                   // Dimension of array
    Field element;                                                              // The elements of this array are of this type

    Array(String Name, Field Element, int Size)                                 // Create the array definition
     {super(Name);                                                              // Name of array
      zz();

      size = Size;                                                              // Size of array
      element = Element;                                                        // Field definition associated with this layout
     }

    void layout(int At, int Depth)                                              // Position this array within the layout
     {zz();
      depth = Depth;                                                            // Depth of field in the layout
      element.layout(At, Depth+1);                                              // Field sub structure
      at = At;                                                                  // Position on index
      element.up = this;                                                        // Chain up to containing parent field
      width = size * element.width;                                             // The size of the array is the sie of its element times the number of elements in the array
     }

    void indexNames()                                                           // Index the name of this field and its sub fields
     {zz();
      indexName();
      element.indexNames();                                                     // Full names of sub fields relative to outer most layout
     }

    void print(Layout.Field top, StringBuilder s)                               // Print the array
     {final String  n = printName(top);                                         // Name using indentation to show depth
      final char    c = fieldType();                                            // First letter of inner most class name to identify type of field
      final int     w = width;                                                  // Bits occupied by the array
      final int     z = size;                                                   // Number of elements in array
      final int     p = at;                                                     // Position of the array element
      s.append(String.format("%c %4d  %4d  %4d    %16s\n",                      // Index of the array
                               c,  p,   w,  z,   n));
      element.print(top, s);                                                    // Print the array element without headers
     }
   }

  class Structure extends Field                                                 // Layout a structure
   {final Field[]fields;                                                        // Supplied field definitions
    final Map<String,Field> subMap   = new TreeMap<>();                         // Unique variables contained inside this structure
    final Stack     <Field> subStack = new Stack  <>();                         // Order of fields inside this structure

    Structure(String Name, Field...Fields)                                      // Fields in the structure
     {super(Name);
      zz();
      fields = Fields;
      for (int i = 0; i < Fields.length; ++i) addField(Fields[i]);              // Each field supplied
     }

    private void addField(Field field)                                          // Add additional fields
     {zz();
      field.up = this;                                                          // Chain up to containing structure
      if (subMap.containsKey(field.name))
       {stop("Structure:", name, "already contains field with this name:",
             field.name);
       }
      z();
      subMap.put   (field.name, field);                                         // Add as a field by name to this structure
      subStack.push(field);                                                     // Add as a field in order to this structure
     }

    void layout(int At, int Depth)                                              // Place the structure in the layout
     {zz();
      at = At;
      width = 0;
      depth = Depth;
      for(Field v : subStack)                                                   // Field sub structure
       {zz();
        v.at = at+width;
        v.layout(v.at, Depth+1);
        width += v.width;
       }
     }

    void indexNames()                                                           // Index the name of this structure and its sub fields
     {zz();
      indexName();
      for (Field f : subStack)                                                  // Each field in the structure
       {zz(); f.indexNames();                                                   // Index name in outermost layout
       }
     }

    void print(Layout.Field top, StringBuilder s)                               // Print the structure
     {z();
      super.print(top, s);
      for(Field f: subStack) f.print(top, s);                                   // Print each field of structure
     }
   }

  class Union extends Structure                                                 // Union of fields laid out in memory on top of each other - it is up to you to have a way of deciding which fields are valid
   {Union(String Name, Field...Fields)                                          // Fields in the union
     {super(Name, Fields);
      zz();
     }

    void layout(int at, int Depth)                                              // Compile this variable so that the size, width and byte fields are correct
     {zz();

      width = 0;
      depth = Depth;
      for(Field v : subMap.values())                                            // Find largest substructure
       {zz();
        v.at = at;                                                              // Substructures are laid out on top of each other
        v.layout(v.at, Depth+1);
        width = max(width, v.width);                                            // Space occupied is determined by largest field of union
       }
     }
   }

  Bit       bit      (String n)                   {z(); return new Bit      (n);}
  Variable  variable (String n, int w)            {z(); return new Variable (n, w);}
  Array     array    (String n, Field   m, int s) {z(); return new Array    (n, m, s);}
  Structure structure(String n, Field...m)        {z(); return new Structure(n, m);}
  Union     union    (String n, Field...m)        {z(); return new Union    (n, m);}

//D1 Duplication                                                                // Duplicate a layout so that ot can be integrated into other layouts

  private Field locateField(Layout l, int offset, Field f)                      // Locate the field in the specified layout that corresponds to the specified field in this layout
   {zz();
    return l.fields.elementAt(offset+f.number);
   }

  private Field[]locateFields(Layout l, int offset, Field[]f)                   // Locate the fields in the specified layout that corresponds to the specified fields in this layout
   {zz();
    final Field[]fields = new Field[f.length];
    for (int i = 0; i < f.length; i++)
     {z(); fields[i] = l.fields.elementAt(offset+f[i].number);
     }
    return fields;
   }

  private Layout duplicate()                                                    // Duplicate this layout
   {z();
    Layout l = layout();                                                        // Start the layout

    for(Field f: fields)
     {z();
      final Field F = switch (f)
       {case Bit        b -> l.bit      (b.name);
        case Variable   v -> l.variable (v.name, v.width);
        case Array      a -> l.array    (a.name, locateField (l, 0, a.element), a.size);
        case Union      u -> l.union    (u.name, locateFields(l, 0, u.fields));
        case Structure  s -> l.structure(s.name, locateFields(l, 0, s.fields));
        default -> null;
       };
     }
    return l.compile();
   }

  Field duplicate(Layout Layout)                                                // Duplicate the specified layout inside this layout
   {zz();
    if (Layout == this) stop("Cannot duplicate self into self");
    Field F = null;
    final int offset = fields.size();
    for(Field f: Layout.fields)
     {z(); F = switch (f)
       {case Bit        b -> bit      (b.name);
        case Variable   v -> variable (v.name, v.width);
        case Array      a -> array    (a.name, locateField (this, offset, a.element), a.size);
        case Union      u -> union    (u.name, locateFields(this, offset, u.fields));
        case Structure  s -> structure(s.name, locateFields(this, offset, s.fields));
        default -> null;
       };
     }
    if (F == null) stop("Cannot duplicate an empty layout");
    return F;                                                                   // Resulting field
   }

  Field duplicate(String name, Layout layout)                                   // Duplicate this layout for use int the current layout giving the top most field a new name
   {zz();
    final Field f = duplicate(layout);
    f.name = name;
    return f;
   }

//D1 Location                                                                   // The location in memory of a field after array indices have been applied

  class Locator                                                                 // Locate the address in memory of a layout element
   {final Field       field;                                                    // Field definition
    final Stack<Array>arrays = new Stack<>();                                   // Array elements requiring including in the path to the element

    Locator(String Name)                                                        // Locate a field by name
     {this(top.fullNames.get(Name));                                            // Locate field name
      z();
      if (field == null)                                                        // Undefined field
       {stop("No such name:", Name, "in:", Layout.this);
       }
      z();
     }

    Locator(Field Field)                                                        // Locate a field
     {zz();
      field = Field;
      for(Field f = field.up; f != null; f = f.up)                              // Convolute path to field with indices of the arrays encountered in the path down to the element but not including the element (which might be an array definition which should be indexed by its parent arrays but not by itself.)
       {zz();
        if (f instanceof Array)
         {zz();
          arrays.insertElementAt((Array)f, 0);
         }
       }
     }

    int at(int...Indices)                                                       // The address of an element in memory including any array indices
     {zz();
      if (Indices.length != arrays.size())                                      // Check number of indices
       {stop("Wrong number of indices for:", field.name,
         ", expected:", arrays.size(),
         "but got:", Indices.length);
       }

      z(); int d = field.at; final int N = arrays.size();

      for(int i = 0; i < N; ++i)                                                // Convolute path to field with indices
       {zz();
        final Array A = arrays.elementAt(i);
        final int   w = A.element.width, s = A.size, n = Indices[i];
        if (n < 0 || n >= s) stop("Array:", A.name, "has size:", s,
         "but is being indexed with:", n);
        d += w * n;
       }
      return d;
     }

    public String toString()                                                    // Print locator
     {final StringBuilder s = new StringBuilder();
      s.append("Locator:\n");
      for(int i = 0; i < arrays.size(); ++i)                                    // Convolute path to field with indices
       {s.append(arrays.elementAt(i));
       }
      return s.toString();
     }
   }

  Locator locator(String Name)                                                  // Locate the address in memory of a layout element
   {z(); return new Locator(Name);
   }

//D0                                                                            // Tests.

  static void test_layout()
   {z();
    Layout    l = new Layout();
    Variable  a = l.variable ("a", 2);
    Variable  b = l.variable ("b", 2);
    Variable  c = l.variable ("c", 4);
    Structure s = l.structure("s", a, b, c);
    Array     A = l.array    ("A", s, 3);
    Variable  d = l.variable ("d", 4);
    Variable  e = l.variable ("e", 4);
    Structure S = l.structure("S", d, A, e);
    l.compile();
    //stop(l);
    l.ok("""
T   At  Wide  Size    Name                   Path
S    0    32          S
V    0     4            d                    d
A    4    24     3      A                    A
S    4     8              s                    A.s
V    4     2                a                    A.s.a
V    6     2                b                    A.s.b
V    8     4                c                    A.s.c
V   28     4            e                    e
""");

    Locator lc = l.locator("A.s.c");
    ok(lc.at(0),   8);
    ok(lc.at(1),  16);
    ok(lc.at(2),  24);
    ok(e.at(), 28);
   }

  static void test_array()
   {z();
    Layout    l = new Layout();
    Variable  a = l.variable ("a", 2);
    Array     A = l.array    ("A", a, 4);
    l.compile();
    //stop(l);
    l.ok("""
T   At  Wide  Size    Name                   Path
A    0     8     4    A
V    0     2            a                    a
""");

    Locator lc = l.locator("a");
    ok(lc.at(0), 0);
    ok(lc.at(1), 2);
    ok(lc.at(2), 4);
    ok(A.width, 8);

   }

  static void test_arrays()
   {z();
    Layout    l = layout();
    Variable  a = l.variable ("a", 2);
    Variable  b = l.variable ("b", 2);
    Variable  c = l.variable ("c", 4);
    Structure s = l.structure("s", a, b, c);
    Array     A = l.array    ("A", s, 3);
    Variable  d = l.variable ("d", 4);
    Variable  e = l.variable ("e", 4);
    Structure S = l.structure("S", d, A, e);
    Array     B = l.array    ("B", S, 4);
    Array     C = l.array    ("C", B, 5);
    l.compile();
    //stop(l);
    l.ok("""
T   At  Wide  Size    Name                   Path
A    0   640     5    C
A    0   128     4      B                    B
S    0    32              S                    B.S
V    0     4                d                    B.S.d
A    4    24     3          A                    B.S.A
S    4     8                  s                    B.S.A.s
V    4     2                    a                    B.S.A.s.a
V    6     2                    b                    B.S.A.s.b
V    8     4                    c                    B.S.A.s.c
V   28     4                e                    B.S.e
""");


    Locator k = l.locator("B.S.A.s.a");
    ok(k.at(0, 0, 0),   4);
    ok(k.at(0, 0, 1),  12);
    ok(k.at(1, 2, 1), 204);
    ok(k.at(1, 2, 2), 212);

    Locator x = l.locator("B.S.A.s.c");
    ok(x.at(0, 0, 0),   8);
    ok(x.at(0, 0, 1),  16);
    ok(x.at(1, 2, 1), 208);
    ok(x.at(1, 2, 2), 216);

    ok(l.size(), 640);
    ok(a.sameSize(b), 2);
    s.toStructure();
    A.toArray();
    ok(b.at(0,0,0),    6);
    ok(b.width(), 2);

    Layout L = l.duplicate();
    ok(l, L);
   }

  static void test_union()
   {z();
    Layout    l = layout();
    Variable  a = l.variable ("a", 2);
    Variable  b = l.variable ("b", 2);
    Variable  c = l.variable ("c", 2);
    Structure s = l.structure("s", a, b, c);
    Variable  A = l.variable ("A", 4);
    Variable  B = l.variable ("B", 4);
    Variable  C = l.variable ("C", 4);
    Structure S = l.structure("S", A, B, C);
    Union     u = l.union    ("u", s, S);
    Array     r = l.array    ("r", u, 4);
    l.compile();
    //stop(l);
    l.ok("""
T   At  Wide  Size    Name                   Path
A    0    48     4    r
U    0    12            u                    u
S    0     6              s                    u.s
V    0     2                a                    u.s.a
V    2     2                b                    u.s.b
V    4     2                c                    u.s.c
S    0    12              S                    u.S
V    0     4                A                    u.S.A
V    4     4                B                    u.S.B
V    8     4                C                    u.S.C
""");

    ok(l.size(), 48);
   }

  static void test_duplicate_whole()
   {z();
    Layout    l = new Layout();
    Variable  a = l.variable ("a", 2);
    Variable  b = l.variable ("b", 2);
    Variable  c = l.variable ("c", 4);
    Structure s = l.structure("s", a, b, c);
    Variable  d = l.variable ("d", 2);
    Array     A = l.array    ("A", s, 2);
    Variable  e = l.variable ("e", 2);
    Structure S = l.structure("S", d, A, e);
    l.compile();
    //stop(l);
    ok(l, """
T   At  Wide  Size    Name                   Path
S    0    20          S
V    0     2            d                    d
A    2    16     2      A                    A
S    2     8              s                    A.s
V    2     2                a                    A.s.a
V    4     2                b                    A.s.b
V    6     4                c                    A.s.c
V   18     2            e                    e
""");

    Layout    m = l.duplicate();
    //stop(m);
    ok(l, m);

    Layout    n = m.duplicate();
    //stop(n);
    ok(l, n);
   }

  static void test_duplicate_part()
   {z();
    Layout    l = new Layout();
    Variable  a = l.variable ("a", 2);
    Variable  b = l.variable ("b", 2);
    Variable  c = l.variable ("c", 4);
    Structure s = l.structure("s", a, b, c);
    l.compile();
    //stop(l);

    Layout    L = new Layout();
    Variable  A = L.variable ("A", 2);
    Field     B = L.duplicate("B", l);
    Variable  C = L.variable ("C", 4);
    Structure S = L.structure("S", A, B, C);
    L.compile();

    //stop(L);
    L.ok("""
T   At  Wide  Size    Name                   Path
S    0    14          S
V    0     2            A                    A
S    2     8            B                    B
V    2     2              a                    B.a
V    4     2              b                    B.b
V    6     4              c                    B.c
V   10     4            C                    C
""");

    Layout  M = L.duplicate();

    //say(L); say(M);
    ok(L, M);

    Layout  N = M.duplicate();
    //say(M); stop(N);
    ok(L, N);

    //stop(M.get("B.a"));
    ok(""+M.get("B.a"), """
T   At  Wide  Size    Name                   Path
V    2     2              a
""");
   }

  static void test_duplicate_array()
   {z();
    Layout    l = new Layout();
    Variable  a = l.variable ("a", 2);
    Array     A = l.array    ("A", a, 8);
    l.compile();
    //stop(l);
    l.ok("""
T   At  Wide  Size    Name                   Path
A    0    16     8    A
V    0     2            a                    a
""");

    Layout    L = new Layout();
    Variable  X = L.variable ("X", 2);
    Field     Y = L.duplicate(l);
    Variable  Z = L.variable ("Y", 4);
    Structure S = L.structure("S", X, Y, Z);
    L.compile();

    //stop(L);
    L.ok("""
T   At  Wide  Size    Name                   Path
S    0    22          S
V    0     2            X                    X
A    2    16     8      A                    A
V    2     2              a                    A.a
V   18     4            Y                    Y
""");
   }

  static void test_array_indexing()
   {z();
    Layout    l = new Layout();
    Variable  a = l.variable ("a", 2);
    Array     A = l.array    ("A", a, 4);
    Array     B = l.array    ("B", A, 4);
    l.compile();
    //stop(l);
    l.ok("""
T   At  Wide  Size    Name                   Path
A    0    32     4    B
A    0     8     4      A                    A
V    0     2              a                    A.a
""");

    ok(a.at(2, 2),            20);
    ok(l.get("A.a").at(2, 3), 22);
   }

  static void test_container()
   {z();
    Layout    l = new Layout();
    Variable  a = l.variable ("a", 2);
    Variable  b = l.variable ("b", 2);
    Variable  c = l.variable ("c", 4);
    Structure s = l.structure("s", a, b, c);
    l.compile();  l.layoutName = "aaa";

    Layout    L = new Layout();
    Variable  A = L.variable ("A", 2);
    Field     B = L.duplicate("B", l);
    Variable  C = L.variable ("C", 4);
    Structure S = L.structure("S", A, B, C);
    L.compile();  L.layoutName = "bbb";

    ok(L, """
Memory Layout: bbb
T   At  Wide  Size    Name                   Path
S    0    14          S
V    0     2            A                    A
S    2     8            B                    B
V    2     2              a                    B.a
V    4     2              b                    B.b
V    6     4              c                    B.c
V   10     4            C                    C
""");
    ok(a.container().layoutName, "aaa");
    ok(A.container().layoutName, "bbb");
   }

  static void test_verilog_ones()
   {Layout    l = new Layout();
    Variable  a = l.variable ("a", 2);
    l.compile();
    //stop(a.verilogOnes());
    ok(a.verilogOnes(), "2'b11");
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_layout();
    test_array();
    test_arrays();
    test_union();
    test_duplicate_whole();
    test_duplicate_part();
    test_duplicate_array();
    test_array_indexing();
    test_container();
    test_verilog_ones();
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
