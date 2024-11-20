//------------------------------------------------------------------------------
// Memory layout
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Memory layout

import java.util.*;

class MemoryLayout extends Test                                                 // Memory layout
 {final Layout layout;
  final Memory memory;

//D1 Construction                                                               // Address memory via a layout

  MemoryLayout(Layout Layout)
   {layout = Layout;
    memory = new Memory(layout.size());
   }

//D1 Print                                                                      // Print a memory layout

  public String toString()                                                      // Print the values of the layout variable in memory
   {final StringBuilder s = new StringBuilder();
    s.append(String.format
       ("%4s %1s %8s  %8s   %8s   %8s     %8s   %s\n",
        "Line", "T", "At", "Wide", "Size", "Indices", "Value", "Name"));
    final Stack<Integer>indices = new Stack<>();
    final int[]lineNumber       = new int[1]; lineNumber[0] = 1;
    print(layout.top, s, lineNumber, 0, indices);
    return s.toString();
   }

  void print(Layout.Field field, StringBuilder t,                               // Print the values of each variable in memory
             int[]lineNumber, int indent, Stack<Integer> indices)
   {final int[]in = new int[indices.size()];
    for (int i = 0, I = indices.size(); i < I; i++) in[i] = indices.elementAt(i);

    t.append(String.format("%4d %s %8d  %8d",
      lineNumber[0], field.fieldType(), field.at, field.width));

    t.append(switch(field)                                                      // Size
     {case Layout.Variable  v -> String.format("%11s", "");
      case Layout.Array     a -> String.format("%11d", a.size);
      case Layout.Structure s -> String.format("%11s", "");
      default -> {stop("Unknown field type:", field); yield "";}
     });

    t.append("   ");
    for (int i = 0; i < 4; i++)                                                 // Indices
     {if (i < indices.size())
       {t.append(String.format("%2d", indices.elementAt(i)));
       }
      else
       {t.append("  ");
       }
     }

    t.append(switch(field)                                                      // Value
     {case Layout.Variable  v ->
       {final int a = field.locator.at(in);
        final int n = getInt(field, in);
        yield String.format("%13d", n);
       }
      case Layout.Array     a -> String.format("%13s", "");
      case Layout.Structure s -> String.format("%13s", "");
      default -> {stop("Unknown field type:", field); yield "";}
      });

    t.append("   "+("  ".repeat(indent))+field.name+"\n");                      // Name

    switch(field)                                                               // Sub fields
     {case Layout.Variable  v -> {}
      case Layout.Array     a ->                                                // Array
       {for (int i = 0; i < a.size; i++)
         {indices.push(i);
          lineNumber[0]++;
          print(a.element, t, lineNumber, indent+1, indices);
          indices.pop();
         }
       }
      case Layout.Structure s ->                                                // Structure and Union
       {for (int i = 0; i < s.fields.length; i++)
         {lineNumber[0]++;
          print(s.fields[i], t, lineNumber, indent+1, indices);
         }
       }
      default -> stop("Unknown field type:", field);
     };
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

  int getInt(Layout.Field field,            int...indices)                      // Get a value from memory
   {z(); return new At(field, indices).result;
   }

  void set  (Layout.Field field, int value, int...indices)                      // Set a value in memory
   {z(); final At a = new At(field, indices);
    memory.set(a.at, a.width, value);
   }

//D1 Components                                                                 // A branch or leaf in the tree

  class At
   {final Layout.Field field;
    final int[]indices;
    final int  width;
    final int  at;
    final int  result;
    At(Layout.Field Field, int...Indices)
     {z(); field = Field; indices = Indices; width = field.width;
      at     = field.locator.at(indices);
      result = memory.getInt(at, width);
     }
    int     width()        {z(); return field.width();}
    boolean sameSize(At b)
     {z(); field.sameSize(b.field);
      z(); if (MemoryLayout.this != b.ml()) stop("Different memory layout");
      z(); return true;
     }

    public String toString()                                                    // Print field name(indices)=value or name=value if there are no indices
     {final StringBuilder s = new StringBuilder();
      s.append(field.name);
      if (indices.length > 0)
       {s.append("[");
        for (int i = 0, N = indices.length; i < N; i++)
         {s.append(indices[i]);
          if (i < N-1) s.append(",");
         }
        s.append("]"+at);
       }
      s.append("="+result);
      return s.toString();
     }
    MemoryLayout ml() {return MemoryLayout.this;}
   }

  At at(Layout.Field Field, int...Indices)
   {return new At(Field, Indices);
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

  void copyHigh(At target, At source)                                                      // Copy the specified number of bits from source to target high bits first
   {z(); target.sameSize(source);
    memory.copyHigh(target.at, source.at, source.width);
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
   {z(); a.sameSize(b);
    z(); return memory.equals(a.at, b.at, a.width);
   }

  boolean notEqual(At a, At b)                                                  // Whether a != b
   {z(); a.sameSize(b);
    return memory.notEquals(a.at, b.at, a.width);
   }

  boolean lessThan(At a, At b)                                                  // Whether a < b
   {z(); a.sameSize(b);
    return memory.lessThan(a.at, b.at, a.width);
   }

  boolean lessThanOrEqual(At a, At b)                                           // Whether a <= b
   {z(); a.sameSize(b);
    return memory.lessThanOrEqual(a.at, b.at, a.width);
   }

  boolean greaterThan(At a, At b)                                               // Whether a > b
   {z(); a.sameSize(b);
    return memory.greaterThan(a.at, b.at, a.width);
   }

  boolean greaterThanOrEqual(At a, At b)                                        // Whether a >= b
   {z(); a.sameSize(b);
    return memory.greaterThanOrEqual(a.at, b.at, a.width);
   }

//D0 Tests                                                                      // Testing

  static class TestMemoryLayout
   {Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 3);
    Layout.Variable  b = l.variable ("b", 4);
    Layout.Variable  c = l.variable ("c", 5);
    Layout.Structure s = l.structure("s", a, b, c);
    Layout.Array     A = l.array    ("A", s, 3);
    Layout.Variable  d = l.variable ("d", 5);
    Layout.Variable  e = l.variable ("e", 7);
    Layout.Structure S = l.structure("S", d, A, e);
    Layout.Array     B = l.array    ("B", S, 3);
    Layout.Array     C = l.array    ("C", B, 3);
    MemoryLayout     M;
    TestMemoryLayout()
     {l.compile();
      M = new MemoryLayout(l);
      M.memory.increasing();
     }
   }

  static void test_get_set()
   {TestMemoryLayout t = new TestMemoryLayout();
        MemoryLayout m = t.M;
              Layout l = m.layout;
    ok(m.at(t.c,     0, 0, 1), "c[0,0,1]24=30");
    m.set  (t.c, 11, 0, 0, 1);
    ok(m.at(t.c,     0, 0, 1), "c[0,0,1]24=11");

    ok(m.at(t.a,     0, 2, 2), "a[0,2,2]125=7");
    m.set  (t.a,  5, 0, 2, 2);
    ok(m.at(t.a,     0, 2, 2), "a[0,2,2]125=5");

    ok(m.at(t.b,     1, 2, 2), "b[1,2,2]272=0");
    m.set  (t.b,  7, 1, 2, 2);
    ok(m.at(t.b,     1, 2, 2), "b[1,2,2]272=7");

    ok(m.at(t.e,     1, 2), "e[1,2]281=0");
    m.set  (t.e, 11, 1, 2);
    ok(m.at(t.e,     1, 2), "e[1,2]281=11");
   }

  static void test_boolean()
   {TestMemoryLayout t = new TestMemoryLayout();
        MemoryLayout m = t.M;
    m.set  (t.a, 1, 0, 1, 1);
    m.set  (t.a, 2, 0, 1, 2);
    m.set  (t.a, 1, 0, 2, 1);
    m.set  (t.a, 2, 0, 2, 2);

    ok( m.equal(m.at(t.a, 0, 1, 1), m.at(t.a, 0, 2, 1)));
    ok(!m.equal(m.at(t.a, 0, 1, 1), m.at(t.a, 0, 1, 2)));

    ok(!m.notEqual(m.at(t.a, 0, 1, 1), m.at(t.a, 0, 2, 1)));
    ok( m.notEqual(m.at(t.a, 0, 1, 1), m.at(t.a, 0, 1, 2)));

    ok( m.lessThan       (m.at(t.a, 0, 1, 1), m.at(t.a, 0, 1, 2)));
    ok(!m.lessThan       (m.at(t.a, 0, 1, 1), m.at(t.a, 0, 2, 1)));
    ok( m.lessThanOrEqual(m.at(t.a, 0, 1, 1), m.at(t.a, 0, 1, 2)));
    ok( m.lessThanOrEqual(m.at(t.a, 0, 1, 1), m.at(t.a, 0, 2, 1)));
    ok(!m.lessThanOrEqual(m.at(t.a, 0, 1, 2), m.at(t.a, 0, 2, 1)));

    ok(!m.greaterThan       (m.at(t.a, 0, 1, 1), m.at(t.a, 0, 1, 2)));
    ok( m.greaterThan       (m.at(t.a, 0, 1, 2), m.at(t.a, 0, 1, 1)));
    ok( m.greaterThanOrEqual(m.at(t.a, 0, 2, 1), m.at(t.a, 0, 1, 1)));
    ok( m.greaterThanOrEqual(m.at(t.a, 0, 1, 1), m.at(t.a, 0, 2, 1)));
    ok(!m.greaterThanOrEqual(m.at(t.a, 0, 2, 1), m.at(t.a, 0, 1, 2)));
   }

  static void test_copy()
   {TestMemoryLayout t = new TestMemoryLayout();
        MemoryLayout m = t.M;
    m.set  (t.a, 1, 0, 0, 0);
    m.set  (t.a, 2, 0, 0, 1);
    m.set  (t.a, 3, 0, 0, 2);

    ok(m.at(t.a, 0, 0, 0), "a[0,0,0]5=1");
    ok(m.at(t.a, 0, 0, 1), "a[0,0,1]17=2");
    ok(m.at(t.a, 0, 0, 2), "a[0,0,2]29=3");

    m.copy(m.at(t.a, 0, 0, 1), m.at(t.a, 0, 0, 0));
    m.copy(m.at(t.a, 0, 0, 2), m.at(t.a, 0, 0, 1));

    ok(m.at(t.a, 0, 0, 0), "a[0,0,0]5=1");
    ok(m.at(t.a, 0, 0, 1), "a[0,0,1]17=1");
    ok(m.at(t.a, 0, 0, 2), "a[0,0,2]29=1");
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_get_set();
    test_boolean();
    test_copy();
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
