//------------------------------------------------------------------------------
// Layout of a Basic Array Machine
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2025
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Btree in a block on the surface of a silicon chip.

import java.util.*;

abstract class LayoutBam extends Test                                           // Layout the array used by the basic array machine
 {int size = 0;                                                                 // The size of the memory used by the basic array machine
  final Stack         <Array> order  = new Stack<>();                           // The order in which the arrays were defined
  final TreeMap<String,Array> arrays = new TreeMap<>();                         // A number of different sized arrays concatenated to make one array
  final int[]memory;                                                            // The concatenation of all the arrays

  class Array
   {final String name;
    final int[]dimensions;
    int base;                                                                   // Base position of this array
    final int length;                                                           // Total number of elements in this array

    Array(String Name, int...Dimensions)                                        // Array definition within the basic array machine
     {name = Name;
      final int D = Dimensions.length;
      if (D > 2) stop("Too many dimensions:", D);                               // Too many dimensions
      dimensions = new int[D];
      for (int i = 0; i < D; i++) dimensions[D-1-i] = Dimensions[i];            // Save the dimensions. A zero dimension array is a field.
      base = size;
      size += length = switch(D)                                                // Number of elements in this array
       {case  0 -> 1;
        case  1 -> dimensions[0];
        default -> dimensions[1] * dimensions[0];
       };
      order.push(this);                                                         // The definition order
      arrays.put(name, this);                                                   // Make the array accessible by name
     }
   }

  Array array(String Name, int...Dimensions)                                    // Array definition within the basic array machine
   {return new Array(Name, Dimensions);
   }

  Array overlay(String Name, int...Dimensions)                                  // Overlay this definition over the previous one
   {if (order.size() == 0) stop("No previous array to overlay");                // Nothing to overlay
    final Array p = order.lastElement();                                        // Previous array which we will overlay
    final Array q = array(Name, Dimensions);                                    // New array
    q.base = p.base;                                                            // Overlay on previous array
    size   = size - p.length + max(p.length, q.length);                         // Account for overall size
    return q;                                                                   // Return overlay
   }

  void load() {}                                                                // Override this method to define the layout

  LayoutBam()                                                                   // Create a layout
   {load();                                                                     // Load array definitions
    memory = new int[size];                                                     // Create the memeory for the basic array machine
   }

  private Array get(String Name)                                                // Get an array by name or complain if the name is invalid
   {final Array A = arrays.get(Name);
    if (A == null) stop("No such array as", Name);
    return A;
   }

  int lookUpIndex(Array Array, int Dimension, String Index)                     // Look up an index which will be used in the specified dimension of the specified array
   {int v;                                                                      // The value of the index
    try
     {v = Integer.parseInt(Index);                                              // The integer has been supplied directly as a string that we can parse
     }
    catch (NumberFormatException e)                                             // The index is a reference to an array
     {final Array I = get(Index);                                               // Hopefully the index is a field - a zero dimensional array
      if (I.dimensions.length > 0)                                              // Complain if the index is not a field
       {stop("Index:", Index, "cannot itself be be an indexed array element");
       }
      v = memory[I.base];                                                       // The value of the index
     }
    if (v < 0) stop("Index:", Index, "is negative");
    if (v >= Array.dimensions[Dimension]) stop("Index:", Index, "value", v, "is greater than dimension["+Dimension+"] with extent:", Array.dimensions[Dimension], "in array:", Array.name);
    return v;
   }

  void set(int Value, String target, String...Indices)                          // Set the value of an array element
   {final Array t   = get(target);
    final String[]i = Indices;

    final int I = i.length;
    final int T = t.dimensions.length;
    if (T != I) stop("Wrong number of dimensions:", T, "!=", I, "for array:", target);

    final int v = switch(T)
     {case  0 -> memory[t.base]                                                                 = Value;
      case  1 -> memory[t.base+lookUpIndex(t, 0, i[0])]                                         = Value;
      default -> memory[t.base+lookUpIndex(t, 1, i[0])*t.dimensions[0]+lookUpIndex(t, 0, i[1])] = Value;
     };
   }

  void set(String target, int...values)                                         // Load an array
   {final Array t = get(target);
    final int   N = values.length;
    if (N > t.length) stop("Too many initializers for array:", t.name, "with length", t.length);

    for (int i = 0; i < N; i++) memory[t.base+i] = values[i];
   }

  int get(String source, String...Indices)                                      // Get the value of an array element
   {final Array s   = get(source);
    final String[]i = Indices;

    final int I = i.length;
    final int S = s.dimensions.length;
    if (S != I) stop("Wrong number of dimensions:", S, "!=", I, "for array:", source);

    return switch(S)
     {case  0 -> memory[s.base];
      case  1 -> memory[s.base+lookUpIndex(s, 0, i[0])];
      default -> memory[s.base+lookUpIndex(s, 1, i[0])*s.dimensions[0]+lookUpIndex(s, 0, i[1])];
     };
   }

  void clear(int Value, String target, String...Indices)                        // Clear the indicated part of the specified array to the the specified value
   {final Array t   = get(target);
    final String[]i = Indices;

    final int I = i.length;
    final int T = t.dimensions.length;
    if (I > T) stop("Too many dimensions:", I, ">", T, "for array:", target);

    if (I == 0)                                                                 // Clear entire array
     {for (int j = 0; j < t.length; j++) memory[t.base + j] = Value;
      return;
     }

    if ((I == 1 && T == 1) || (I == 2 && T == 2))                               // Clear single element of one or two dimensional array
     {set(0, target, Indices);
      return;
     }

    if (I == 1 && T == 2)                                                       // Clear indicated sub array
     {final int l = t.dimensions[0];                                            // Size of sub array
      final int p = l * lookUpIndex(t, 1, i[0]);                                // Start of sub array
      for (int j = 0; j < l; j++) memory[t.base+ p + j] = 0;                    // Clear sub array in two dimensional array
      return;
     }
   }

  void zero(String source, String...Indices) {clear( 0, source, Indices);}      // Zero the indicated part of the specified array
  void ones(String source, String...Indices) {clear(-1, source, Indices);}      // Set the indicated part of the specified array to all ones

  void move(String target, String source, String...Indices)                     // Copy the indexed source field to the indexed target fields
   {final Array t = get(target), s = get(source);

    final int I = Indices.length;
    final int T = t.dimensions.length, S = s.dimensions.length;
    if (T + S != I) stop("Wrong number of dimensions:", T, "+", S, "!=", I, "for arrays:", target+",", source);

    final String[]ti = new String[T];                                           // Assign indices
    final String[]si = new String[S];
    for (int i = 0; i < T; i++) ti[i]   = Indices[i];
    for (int i = T; i < I; i++) si[i-T] = Indices[i];

    set(get(source, si), target, ti);
   }

  int compareImmediate(int immediate, String source, String...Indices)          // Compare the indexed source field to the immediate value returning -1 if source is less that target, 0 if equal otherwise +1
   {final Array s = get(source);

    final int I = Indices.length;
    final int S = s.dimensions.length;
    if (S != I) stop("Wrong number of dimensions:", S, "!=", I, "for array:", source);

    final int a = immediate;
    final int b = get(source, Indices);
    return Integer.valueOf(b).compareTo(Integer.valueOf(a));
   }

  int compare(String target, String source, String...Indices)                   // Compare the indexed source field to the indexed target field returning -1 if source is less that target, 0 if equal otherwise +1
   {final Array t = get(target), s = get(source);

    final int I = Indices.length;
    final int T = t.dimensions.length, S = s.dimensions.length;
    if (T + S != I) stop("Wrong number of dimensions:", T, "+", S, "!=", I);     // Check we have the right number of indices

    final String[]ti = new String[T];                                           // Assign indices
    final String[]si = new String[S];
    for (int i = 0; i < T; i++) ti[i]   = Indices[i];
    for (int i = T; i < I; i++) si[i-T] = Indices[i];

    final int a = get(source, si);
    final int b = get(target, ti);
    return Integer.valueOf(b).compareTo(Integer.valueOf(a));
   }

  void addImmediate(int immediate, String source, String...Indices)             // Add a constant value to the source field
   {final Array s = get(source);

    final int I = Indices.length;
    final int S = s.dimensions.length;
    if (S != I) stop("Wrong number of dimensions:", S, "!=", I, "for array:", source);

    final int a =  get(source, Indices);
    set(immediate + a, source, Indices);
   }

  void add(String target, String source, String...Indices)                      // Add the source to the target replacing the target
   {final Array t = get(target), s = get(source);

    final int I = Indices.length;
    final int T = t.dimensions.length, S = s.dimensions.length;
    if (T + S != I) stop("Wrong number of dimensions:", T, "+", S, "!=", I);    // Check we have the right number of indices

    final String[]ti = new String[T];                                           // Assign indices
    final String[]si = new String[S];
    for (int i = 0; i < T; i++) ti[i]   = Indices[i];
    for (int i = T; i < I; i++) si[i-T] = Indices[i];

    final int a = get(source, si);
    final int b = get(target, ti);
    set(a+b, target, ti);
   }

  public String toString()                                                      // Print layout
   {final StringBuilder s = new StringBuilder();
    int l = 0;
    for  (Array a: order)
     {if      (a.dimensions.length == 0)
       {    s.append(String.format("%4d  %4d               %4d  %s %s\n", l++, a.base,     memory[a.base], a.name, "-"));
       }
      else if (a.dimensions.length == 1)
       {for   (int i = 0; i < a.dimensions[0]; i++)
         {  s.append(String.format("%4d  %4d        %4d   %4d  %s %s\n", l++, a.base, i,   memory[a.base+i], a.name, i == 0 ? "-" : "|"));
         }
       }
      else if (a.dimensions.length == 2)
       {for   (int j = 0; j < a.dimensions[1]; j++)
         {for (int i = 0; i < a.dimensions[0]; i++)
           {s.append(String.format("%4d  %4d  %4d  %4d   %4d  %s %s\n", l++, a.base, j, i, memory[a.base+j*a.dimensions[0]+i], a.name,  i == 0 && j == 0 ? "-" : i == 0 ?  "+" : "|"));
           }
         }
       }
     }

    return ""+s;
   }

//D0 Tests                                                                      // Testing

  static LayoutBam test_layout()                                                // A test layout
   {return new LayoutBam()
     {void load()
       {array("i");
        array("j");
        array("k");
        array("a", 4);
        array("b", 6);
        array("A", 2, 4);
        array("B", 4, 3);
       }
     };
   }

  static LayoutBam test_set()
   {LayoutBam l = test_layout();
    l.set(1, "i");
    l.set(2, "j");
    l.set(3, "k");
    l.set(4, "a", "i");
    l.set(5, "a", "j");
    l.set(6, "A", "i", "k"); // 1,3
    l.set(7, "A", "i", "j"); // 1,2
    l.set(8, "B", "k", "i"); // 3,1
    l.set(9, "B", "j", "j"); // 2,2
    //stop(l);
    ok(""+l, """
   0     0                  1  i -
   1     1                  2  j -
   2     2                  3  k -
   3     3           0      0  a -
   4     3           1      4  a |
   5     3           2      5  a |
   6     3           3      0  a |
   7     7           0      0  b -
   8     7           1      0  b |
   9     7           2      0  b |
  10     7           3      0  b |
  11     7           4      0  b |
  12     7           5      0  b |
  13    13     0     0      0  A -
  14    13     0     1      0  A |
  15    13     0     2      0  A |
  16    13     0     3      0  A |
  17    13     1     0      0  A +
  18    13     1     1      0  A |
  19    13     1     2      7  A |
  20    13     1     3      6  A |
  21    21     0     0      0  B -
  22    21     0     1      0  B |
  23    21     0     2      0  B |
  24    21     1     0      0  B +
  25    21     1     1      0  B |
  26    21     1     2      0  B |
  27    21     2     0      0  B +
  28    21     2     1      0  B |
  29    21     2     2      9  B |
  30    21     3     0      0  B +
  31    21     3     1      8  B |
  32    21     3     2      0  B |
""");
    return l;
   }

  static void test_get()
   {LayoutBam l = test_set();
    ok(l.get("B", "k", "i"), 8);
    ok(l.get("B", "j", "j"), 9);
   }

  static LayoutBam test_move()
   {LayoutBam l = test_set();
    l.move("B", "a", "k", "i", "i");
    l.move("B", "a", "k", "j", "j");
    //stop(l);
    ok(""+l, """
   0     0                  1  i -
   1     1                  2  j -
   2     2                  3  k -
   3     3           0      0  a -
   4     3           1      4  a |
   5     3           2      5  a |
   6     3           3      0  a |
   7     7           0      0  b -
   8     7           1      0  b |
   9     7           2      0  b |
  10     7           3      0  b |
  11     7           4      0  b |
  12     7           5      0  b |
  13    13     0     0      0  A -
  14    13     0     1      0  A |
  15    13     0     2      0  A |
  16    13     0     3      0  A |
  17    13     1     0      0  A +
  18    13     1     1      0  A |
  19    13     1     2      7  A |
  20    13     1     3      6  A |
  21    21     0     0      0  B -
  22    21     0     1      0  B |
  23    21     0     2      0  B |
  24    21     1     0      0  B +
  25    21     1     1      0  B |
  26    21     1     2      0  B |
  27    21     2     0      0  B +
  28    21     2     1      0  B |
  29    21     2     2      9  B |
  30    21     3     0      0  B +
  31    21     3     1      4  B |
  32    21     3     2      5  B |
""");
    return l;
   }

  static void test_compare()
   {LayoutBam l = test_move();
    ok(l.compareImmediate(4, "B", "k", "j"), +1);
    ok(l.compareImmediate(5, "B", "k", "j"),  0);
    ok(l.compareImmediate(6, "B", "k", "j"), -1);
    ok(l.compare("B", "B", "k", "j", "k", "i"), +1);
    ok(l.compare("B", "B", "k", "j", "j", "j"), -1);
    ok(l.compare("B", "a", "k", "j", "j"),       0);
   }

  static void test_compare_int()                                                // Encode some indices directly instead of indirectly
   {LayoutBam l = test_move();
    ok(l.compareImmediate(4, "B",    "3",  "j"), +1);
    ok(l.compareImmediate(5, "B",    "3",  "j"),  0);
    ok(l.compareImmediate(6, "B",    "3",  "j"), -1);
    ok(l.compare("B", "B", "3", "j", "3",  "i"), +1);
    ok(l.compare("B", "B", "3", "j", "j", "j"), -1);
    ok(l.compare("B", "a", "3", "j", "j"),       0);
   }

  static void test_add()
   {LayoutBam l = test_move();

    l.addImmediate(1, "B", "k", "i");
             ok(l.get("B", "k", "i"),  5);
    l.add       ("B", "B", "k", "j", "k", "i");
             ok(l.get("B", "k", "j"), 10);
   }

  static LayoutBam test_overlay()                                               // A test layout  with overlays
   {final int M = 4, N = 3;
     final LayoutBam l = new LayoutBam()
     {void load()
       {array("i");
        array("j");
        array("k");
        array("a", M*N);
        overlay("b", M, N);
       }
     };

    for (int i = 0; i < M*N; i++)
     {l.set(i, "i");
      l.set(i, "a", "i");
     }

    for   (int i = 0; i < M; i++)
     {for (int j = 0; j < N; j++)
       {l.set(i, "i");
        l.set(j, "j");
        ok(l.get("b", "i", "j"), i*N+j);
       }
     }
    return l;
   }

  static void test_clear()
   {final int M = 4, N = 3;
     final LayoutBam l = new LayoutBam()
     {void load()
       {array("i");
        array("j");
        array("k");
        array("a", M);
        array("b", M, N);
       }
     };

    for (int i = 0; i < M; i++)
     {l.set(i, "i");
      l.set(i, "a", "i");
     }

    for   (int i = 0; i < M; i++)
     {for (int j = 0; j < N; j++)
       {l.set(i, "i");
        l.set(j, "j");
        l.set(i*N+j, "b", "i", "j");
       }
     }
    //stop(l);
    l.clear(3, "j");
    l.clear(4, "a", "j");
    l.clear(2, "i");
    l.clear(5, "b", "i");
    //stop(l);
    ok(""+l, """
   0     0                  2  i -
   1     1                  3  j -
   2     2                  0  k -
   3     3           0      0  a -
   4     3           1      1  a |
   5     3           2      2  a |
   6     3           3      0  a |
   7     7     0     0      0  b -
   8     7     0     1      1  b |
   9     7     0     2      2  b |
  10     7     1     0      3  b +
  11     7     1     1      4  b |
  12     7     1     2      5  b |
  13     7     2     0      0  b +
  14     7     2     1      0  b |
  15     7     2     2      0  b |
  16     7     3     0      9  b +
  17     7     3     1     10  b |
  18     7     3     2     11  b |
""");
   }

  static void test_initialize()
   {final LayoutBam l = new LayoutBam()
     {void load()
       {array("a");
        array("b", 2);
        array("c", 2, 3);
       }
     };

    l.set("a", 1);
    l.set("b", 1, 2);
    l.set("c", 11, 12, 13, 21, 22, 23);
    //stop(l);
    ok(""+l, """
   0     0                  1  a -
   1     1           0      1  b -
   2     1           1      2  b |
   3     3     0     0     11  c -
   4     3     0     1     12  c |
   5     3     0     2     13  c |
   6     3     1     0     21  c +
   7     3     1     1     22  c |
   8     3     1     2     23  c |
""");
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_set();
    test_get();
    test_move();
    test_compare();
    test_compare_int();
    test_add();
    test_overlay();
    test_clear();
   }

  static void newTests()                                                        // Tests being worked on
   {oldTests();
    test_initialize();
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
