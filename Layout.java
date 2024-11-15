//------------------------------------------------------------------------------
// Layout memory
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Simulate a silicon chip.

import java.util.*;

//D1 Construct                                                                  // Layout a description of the memory used by a chip

public class Layout extends Test                                                // A Memory layout for a chip. There might be several such layouts representing parts of the chip.
 {Field top;                                                                    // The top most field in a set of nested fields describing memory.

  void layout(String name, Field...fields)                                      // Create a new Layout loaded from an implied structure of fields
   {top = new Structure(name, fields);                                          // Create a structure to hold the named  outermost fields
    top.layout(0, 0);                                                           // Locate field positions
    top.indexNames();                                                           // Index the names of the fields
   }

  int size() {return top == null ? 0 : top.width;}                              // Size of memory

  Field asFields() {return top;}                                                // Get field definitions associated with memory

  void ok(String expected) {Test.ok(top.toString(), expected);}                 // Confirm layout is as expected

  public String toString()
   {return top.toString();
   }

//D1 Layouts                                                                    // Field memory of the chip as variables, arrays, structures, unions. Dividing the memory in this manner makes it easier to program the chip symbolically.

  abstract class Field                                                          // Variable/Array/Structure/Union definition.
   {final String  name;                                                         // Name of field
    final  int  number;                                                         // Number of field
    static int numbers = 0;                                                     // Numbers of fields
    String    fullName;                                                         // Full name of this field
    int at;                                                                     // Offset of field from start of memory
    int width;                                                                  // Number of bits in a field
    int depth;                                                                  // Depth of field - the number of containing arrays/structures/unions above
    Field up;                                                                   // Upward chain to containing array/structure/union
    final Map<String,Field> fullNames = new TreeMap<>();                        // Fields by name
    final Set<String>  classification = new TreeSet<>();                        // Names that identify the type of the field to aid debugging

    Field(String Name) {name = Name; number = ++numbers;}                       // Create a new named field with a unique number

    int at   () {return at;}                                                    // Position of field in memory
    int width() {return width;}                                                 // Size of the memory in bits occupied by this field

    void fullName(Layout.Field top, StringBuilder s)                            // The full name of a field relative to the indicated top
     {if (top == this) return;
      final Stack<String> names = new Stack<>();                                // Full name path
      for(Layout.Field f = this; f.up != null; f = f.up)                        // Go up structure
       {names.insertElementAt(f.name, 0);
        if (f.up == top) break;
       }
      if (names.size() == 0) return;                                            // Zero length path to element
      final String        t = names.pop();
      s.append("     ");
      for(String n : names) s.append(n+".");
      s.append(t);
     }

    void indexName()                                                            // Index all the full names of a field
     {final Stack<String> names = new Stack<>();                                // Full name path
      for(Layout.Field f = this; f.up != null; f = f.up)                        // Go up structure
       {names.insertElementAt(f.name, 0);
        final StringBuilder s = new StringBuilder();
        final String top = names.pop();
        for(String n : names) s.append(n+".");
        s.append(top); names.push(top);
        f.up.fullNames.put(s.toString(), this);
       }
     }

    Field get(String path) {return fullNames.get(path);}                        // Address a contained field by name
    abstract void indexNames();                                                 // Set the full names of all the sub fields in a field

    public Layout.Field asField () {return this;}                               // Layout associated with this field
    public Layout       asLayout() {return Layout.this;}                        // Layout associated with this field

    int sameSize(Field b)                                                       // Check the specified field is the same size as this field
     {final int A = width, B = b.width;
      if (A != B) stop("Fields must have the same width, but field", name,
        "has width", A, "while field", b.name, "has size", B);
      return A;
     }

    void isBit()                                                                // Check the specified field is a bit field
     {if (width != 1) stop("Field must be one bit wide, but it is",
        width, "bits wide");
     }

    abstract void layout(int at, int depth);                                    // Layout this field

    void position(int At) {at = At;}                                            // Reposition a field after an index of a containing array has been changed

    String  indent() {return "  ".repeat(depth);}                               // Indentation during printing
    char fieldType() {return getClass().getName().split("\\$")[1].charAt(0);}   // First letter of inner most class name to identify type of field

    StringBuilder header()                                                      // Create a string builder with a header preloaded
     {final StringBuilder s = new StringBuilder();
      //s.append("Memory: "+memory.memoryNumber+"\n");
      s.append(String.format
       ("%1s %4s  %4s      %-16s       %s\n",
        "T", "At", "Wide", "Name", "Path"));
      return s;
     }

    String printName(Layout.Field top)                                          // Print the name of a field showing whether it is a constant and its classification
     {final StringBuilder s = new StringBuilder();
      s.append(indent());
      s.append(String.format("%-16s", name));
      fullName(top, s);                                                         // Full name for this field
      return s.toString();
     }

    void print(Layout.Field top, StringBuilder s)                               // Print the field
     {final String  n = printName(top);                                         // Name using indentation to show depth
      final char    c = fieldType();                                            // First letter of inner most class name to identify type of field

      s.append(String.format("%c %4d  %4d      %-16s\n",                        // Variable
                             c,  at,  width,   n));
     }

    public String toString()                                                    // Print the field and its sub structure
     {final StringBuilder s = header();
      print(this, s);
      return s.toString().replaceAll("\\s+\n", "\n");
     }

    Bit       toBit      () {return (Bit)      this;}                           // Try to convert to a bit
    Variable  toVariable () {return (Variable) this;}                           // Try to convert to a variable
    Array     toArray    () {return (Array)    this;}                           // Try to convert to an array
    Structure toStructure() {return (Structure)this;}                           // Try to convert to a structure
    Union     toUnion    () {return (Union)    this;}                           // Try to convert to a union
   }

  class Variable extends Field                                                  // Layout a variable with no sub structure
   {Variable(String name, int Width)
     {super(name); width = Width;
     }

    void indexNames()                                                           // Index the name of this field
     {indexName();
     }

    void layout(int At, int Depth)                                              // Layout the variable in the structure
     {at = At; depth = Depth;
     }
   }

  class Bit extends Variable                                                    // A variable of unit width is a boolean. could have called it a boolean but decied to callit abool instead becuase it was shorter and more like
   {Bit(String name) {super(name, 1);}
   }

  class Array extends Field                                                     // Layout an array definition.
   {int size;                                                                   // Dimension of array
    Field element;                                                              // The elements of this array are of this type

    Array(String Name, Field Element, int Size)                                 // Create the array definition
     {super(Name);                                                              // Name of array
      size = Size;                                                              // Size of array
      element = Element;                                                        // Field definition associated with this layout
     }

    int at(int i) {return at+i*element.width;}                                  // Offset of this array field in the structure

    void layout(int At, int Depth)                                              // Position this array within the layout
     {depth = Depth;                                                            // Depth of field in the layout
      element.layout(At, Depth+1);                                              // Field sub structure
      at = At;                                                                  // Position on index
      element.up = this;                                                        // Chain up to containing parent field
      width = size * element.width;                                             // The size of the array is the sie of its element times the number of elements in the array
     }

    void indexNames()                                                           // Index the name of this field and its sub fields
     {indexName();
      element.indexNames();                                                     // Full names of sub fields relative to outer most layout
     }

    void print(Layout.Field top, StringBuilder s)                               // Print the array
     {final String  n = printName(top);                                         // Name using indentation to show depth
      final char    c = fieldType();                                            // First letter of inner most class name to identify type of field
      final int     w = width;                                                  // Bits occupied bythe array
      final int     p = at;                                                     // Position of the array element
      s.append(String.format("%c %4d  %4d      %16s\n",                         // Index of the array
                               c,  p,   w,     n));
      element.print(top, s);                                                    // Print the array element without headers
     }
   }

  class Structure extends Field                                                 // Layout a structure
   {final Map<String,Field> subMap   = new TreeMap<>();                         // Unique variables contained inside this structure
    final Stack     <Field> subStack = new Stack  <>();                         // Order of fields inside this structure

    Structure(String Name, Field...Fields)                                      // Fields in the structure
     {super(Name);
      for (int i = 0; i < Fields.length; ++i) addField(Fields[i]);              // Each field supplied
     }

    void addField(Field layout)                                                 // Add additional fields
     {final Field field = layout.asField();                                     // Field associated with this layout
      field.up = this;                                                          // Chain up to containing structure
      if (subMap.containsKey(field.name))
       {stop("Structure:", name, "already contains field with this name:",
             field.name);
       }
      subMap.put   (field.name, field);                                         // Add as a field by name to this structure
      subStack.push(field);                                                     // Add as a field in order to this structure
     }

    void layout(int At, int Depth)                                              // Place the structure in the layout
     {at = At;
      width = 0;
      depth = Depth;
      for(Field v : subStack)                                                   // Field sub structure
       {v.at = at+width;
        v.layout(v.at, Depth+1);
        width += v.width;
       }
     }

    void indexNames()                                                           // Index the name of this structure and its sub fields
     {indexName();
      for (Field f : subStack)                                                  // Each field in the structure
       {f.indexNames();                                                         // Index name in outermost layout
       }
     }

    void position(int At)                                                       // Reposition this structure after an index of a containing array has been changed
     {at = At;
      int w = 0;
      for(Field v : subStack)                                                   // Field sub structure
       {v.position(v.at = at+w);                                                // Substructures are laid out sequentially
        w += v.width;
       }
     }

    void print(Layout.Field top, StringBuilder s)                               // Print the structure
     {super.print(top, s);
      for(Field f: subStack) f.print(top, s);                                   // Print each field of structure
     }
   }

  class Union extends Structure                                                 // Union of fields laid out in memory on top of each other - it is up to you to have a way of deciding which fields are valid
   {Union(String Name, Field...Fields)                                          // Fields in the union
     {super(Name, Fields);
     }

    void layout(int at, int Depth)                                              // Compile this variable so that the size, width and byte fields are correct
     {width = 0;
      depth = Depth;
      for(Field v : subMap.values())                                            // Find largest substructure
       {v.at = at;                                                              // Substructures are laid out on top of each other
        v.layout(v.at, Depth+1);
        width = max(width, v.width);                                            // Space occupied is determined by largest field of union
       }
     }

    void position(int At)                                                       // Reposition this union after an index of a containing array has been changed
     {at = At;
      for(Field v : subMap.values()) v.position(at);
     }
   }

  Bit       bit      (String name)                       {return new Bit      (name);}
  Variable  variable (String name, int width)            {return new Variable (name, width);}
  Array     array    (String name, Field   ml, int size) {return new Array    (name, ml, size);}
  Structure structure(String name, Field...ml)           {return new Structure(name, ml);}
  Union     union    (String name, Field...ml)           {return new Union    (name, ml);}

//D0                                                                            // Tests.

  static void test_layout()
   {Layout    l = new Layout();
    Variable  a = l.variable ("a", 2);
    Variable  b = l.variable ("b", 2);
    Variable  c = l.variable ("c", 4);
    Structure s = l.structure("s", a, b, c);
    Array     A = l.array    ("A", s, 3);
    Variable  d = l.variable ("d", 4);
    Variable  e = l.variable ("e", 4);
    l.layout("S", d, A, e);
    //stop(l);
    l.ok("""
T   At  Wide      Name                   Path
S    0    32      S
V    0     4        d                    d
A    4    24        A                    A
S    4     8          s                    A.s
V    4     2            a                    A.s.a
V    6     2            b                    A.s.b
V    8     4            c                    A.s.c
V   28     4        e                    e
""");
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_layout();
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
