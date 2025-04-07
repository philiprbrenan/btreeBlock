//------------------------------------------------------------------------------
// Layout of a Basic Array Machine
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2025
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Btree in a block on the surface of a silicon chip.

import java.util.*;

abstract class LayoutBam extends Test                                              // Layout the array used by the basic array machine
 {int size = 0;
  final Stack         <Array> order  = new Stack<>();                           // The order in which the arrays were defined
  final TreeMap<String,Array> arrays = new TreeMap<>();                         // A number of different sized arrays concatenated to make one array
  final int[]memory;                                                            // The concatenation of all the arrays

  class Array
   {final String name;
    final int[]dimensions;
    final int base;                                                             // Base position of this array
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

  String[]reverse(String[]a)                                                    // Reverse an array
   {final int N = a.length;
    final String[]A = new String[N];
    for (int i = 0; i < N; i++) A[N-1-i] = a[i];
    return A;
   }

  void load() {}                                                                // Override this method to define the layout

  LayoutBam()                                                                   // Create a layout
   {load();                                                                     // Load array definitions
    memory = new int[size];                                                     // Create the memeory for the basic array machine
   }

  Array get(String Name)                                                        // Get an array by name or complain if the name is invalid
   {final Array A = arrays.get(Name);
    if (A == null) stop("No such array as", Name);
    return A;
   }

  int lookUpIndex(Array Array, int Dimension, String Index)                     // Look up an index which will be used in the specified dimension of the specified array
   {final Array I = get(Index);                                                 // Hopefully the index is a field
    if (I.dimensions.length > 0)                                                // Complain if the index is not a field
     {stop("Index:", Index, "cannot itself be be an indexed array element");
     }
    final int v = memory[I.base];                                               // The value of the index
    if (v < 0) stop("Index:", Index, "is negative");
    if (v >= Array.dimensions[Dimension]) stop("Index:", Index, "value", v, "is greater than dimension["+Dimension+"] with extent:", Array.dimensions[Dimension]);
    return v;
   }

  void set(int Value, String target, String...Indices)                          // Set the value of an array element
   {final Array t   = get(target);
    final String[]i = Indices;

    final int I = i.length;
    final int T = t.dimensions.length;
    if (T != I) stop("Wrong number of dimensions", T, "!=", I);                 // Check we have the right number of indices

    final int v = switch(T)
     {case  0 -> memory[t.base]                                                                 = Value;
      case  1 -> memory[t.base+lookUpIndex(t, 0, i[0])]                                         = Value;
      default -> memory[t.base+lookUpIndex(t, 1, i[0])*t.dimensions[0]+lookUpIndex(t, 0, i[1])] = Value;
     };
   }

  int get(String target, String...Indices)                                      // Get the value of an array element
   {final Array t   = get(target);
    final String[]i = Indices;

    final int I = i.length;
    final int T = t.dimensions.length;
    if (T != I) stop("Wrong number of dimensions", T, "!=", I);                 // Check we have the right number of indices

    return switch(T)
     {case  0 -> memory[t.base];
      case  1 -> memory[t.base+lookUpIndex(t, 0, i[0])];
      default -> memory[t.base+lookUpIndex(t, 1, i[1])*t.dimensions[0]+lookUpIndex(t, 0, i[0])];
     };
   }

  void move(String target, String source, String...Indices)                     // Copy the indexed source field to the indexed target fields
   {final Array t   = get(target), s = get(source);

    final int I = Indices.length;
    final int T = t.dimensions.length, S = s.dimensions.length;
    if (T + S != I) stop("Wrong number of dimensions", T, "+", S, "!=", I);     // Check we have the right number of indices

    final String[]ti = new String[T];
    final String[]si = new String[S];
    for (int i = 0; i < T; i++) ti[i]   = Indices[T-i-i];
    for (int i = T; i < I; i++) si[i-T] = Indices[I-1-i];

    set(get(source, si), target, ti);
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

  static LayoutBam test_layout()                                                   // A test layout
   {return new LayoutBam()
     {void load()
       {new Array("i");
        new Array("j");
        new Array("k");
        new Array("a", 4);
        new Array("b", 6);
        new Array("A", 2, 4);
        new Array("B", 4, 3);
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

  static void oldTests()                                                        // Tests thought to be in good shape
   {
   }

  static void newTests()                                                        // Tests being worked on
   {oldTests();
    test_set();
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
