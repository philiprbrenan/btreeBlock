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

//D1 Print                                                                      // Print a memory layout

  void print(Layout.Field field, StringBuilder t,                               // Print the values of each variable in memory
             int[]lineNumber, int indent, Stack<Integer> indices)
   {final int[]in = new int[indices.size()];
    for (int i = 0, I = indices.size(); i < I; i++) in[i] = indices.elementAt(i);

    t.append(String.format("%4d %s %8d  %8d",
      lineNumber[0], field.fieldType(), field.at, field.width));

    t.append(switch(field)                                                      // Size
     {case Layout.Bit       b -> String.format("%11s", "");
      case Layout.Variable  v -> String.format("%11s", "");
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
     {case Layout.Bit       b -> String.format("%13d", memory.getInt(field.locator.at(in), 1));
      case Layout.Variable  v ->
       {final int a = field.locator.at(in);
        final int n = memory.getInt(a, field.width);
        yield String.format("%13d", n);
       }
      case Layout.Array     a -> String.format("%13s", "");
      case Layout.Structure s -> String.format("%13s", "");
      default -> {stop("Unknown field type:", field); yield "";}
      });

    t.append("   "+("  ".repeat(indent))+field.name+"\n");                      // Name

    switch(field)                                                               // Sub fields
     {case Layout.Bit       b -> {}
      case Layout.Variable  v -> {}
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


//D1 Components                                                                 // A branch or leaf in the tree

  class At
   {final Layout.Field field;
    final int[]indices;
    final int  result;
    At(Layout.Field Field, int...Indices)
     {z(); field = Field; indices = Indices;
      result = memory.getInt(field.locator.at(indices), width());
     }
    int width()              {z(); return field.width();}
    int sameSize(At b)       {z(); return field.sameSize(b.field);}

    public String toString()                                                    // Print field name(indices)=value or name=value if there are no indices
     {final StringBuilder s = new StringBuilder();
      s.append(field.name);
      if (indices.length > 0)
       {s.append("[");
        for (int i = 0, N = indices.length; i < N; i++)
         {s.append(indices[i]);
          if (i < N-1) s.append(",");
         }
        s.append("]");
       }
      s.append("="+result);
      return s.toString();
     }
   }
  At at(Layout.Field Field, int...Indices)
   {return new At(Field, Indices);
   }

//D1 Boolean                                                                    // Boolean operations on fields held in memeory

  boolean equals(At a, At b)                                                    // Whether  a == b
   {z(); a.sameSize(b);
    return memory.equals(a.result, b.result, a.width());
   }

  boolean notEquals(At a, At b)                                                 // Whether a != b
   {z(); a.sameSize(b);
    return memory.notEquals(a.result, b.result, a.width());
   }

  boolean lessThan(At a, At b)                                                  // Whether a < b
   {z(); a.sameSize(b);
    return memory.lessThan(a.result, b.result, a.width());
   }

  boolean lessThanOrEqual(At a, At b)                                           // Whether a <= b
   {z(); a.sameSize(b);
    return memory.lessThanOrEqual(a.result, b.result, a.width());
   }

  boolean greaterThan(At a, At b)                                               // Whether a > b
   {z(); a.sameSize(b);
    return memory.greaterThan(a.result, b.result, a.width());
   }

  boolean greaterThanOrEqual(At a, At b)                                        // Whether a >= b
   {z(); a.sameSize(b);
    return memory.greaterThanOrEqual(a.result, b.result, a.width());
   }

//D0 Tests                                                                      // Testing

  static class TestMemoryLayout
   {Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 2);
    Layout.Variable  b = l.variable ("b", 2);
    Layout.Variable  c = l.variable ("c", 4);
    Layout.Structure s = l.structure("s", a, b, c);
    Layout.Array     A = l.array    ("A", s, 3);
    Layout.Variable  d = l.variable ("d", 4);
    Layout.Variable  e = l.variable ("e", 4);
    Layout.Structure S = l.structure("S", d, A, e);
    Layout.Array     B = l.array    ("B", S, 4);
    Layout.Array     C = l.array    ("C", B, 5);
    MemoryLayout     M;
    TestMemoryLayout()
     {l.compile();
      M = new MemoryLayout(l);
      M.memory.increasing();
     }
   }

  static void test_get()                                                        // Tests thought to be in good shape
   {TestMemoryLayout t = new TestMemoryLayout();
        MemoryLayout m = t.M;
    ok(m.at(t.a, 0, 2, 2), "a[0,2,2]=3");
    m.ok("""
Line T       At      Wide       Size    Indices        Value   Name
   1 A        0       640          5                           C
   2 A        0       128          4    0                        B
   3 S        0        32               0 0                        S
   4 V        0         4               0 0                2         d
   5 A        4        24          3    0 0                          A
   6 S        4         8               0 0 0                          s
   7 V        4         2               0 0 0              3             a
   8 V        6         2               0 0 0              0             b
   9 V        8         4               0 0 0             14             c
  10 S        4         8               0 0 1                          s
  11 V        4         2               0 0 1              0             a
  12 V        6         2               0 0 1              0             b
  13 V        8         4               0 0 1             15             c
  14 S        4         8               0 0 2                          s
  15 V        4         2               0 0 2              0             a
  16 V        6         2               0 0 2              0             b
  17 V        8         4               0 0 2             14             c
  18 V       28         4               0 0                3         e
 311 S        0        32               4 3                        S
 312 V        0         4               4 3                0         d
 313 A        4        24          3    4 3                          A
 314 S        4         8               4 3 0                          s
 315 V        4         2               4 3 0              0             a
 316 V        6         2               4 3 0              0             b
 317 V        8         4               4 3 0              0             c
 318 S        4         8               4 3 1                          s
 319 V        4         2               4 3 1              0             a
 320 V        6         2               4 3 1              0             b
 321 V        8         4               4 3 1             14             c
 322 S        4         8               4 3 2                          s
 323 V        4         2               4 3 2              3             a
 324 V        6         2               4 3 2              3             b
 325 V        8         4               4 3 2             15             c
 326 V       28         4               4 3               15         e
""");
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {
   }

  static void newTests()                                                        // Tests being worked on
   {oldTests();
    test_get();
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
