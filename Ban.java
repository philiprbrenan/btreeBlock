//------------------------------------------------------------------------------
// Basic Array Machine with one register
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2025
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Btree in a block on the surface of a silicon chip.

import java.util.*;

abstract class Ban extends Test                                                 // Layout the basic array machine and execute a program in it
 {int size = 0;                                                                 // The size of the memory used by the basic array machine
  Stack         <Array> order  = new Stack<>();                                 // The order in which the arrays were defined
  TreeMap<String,Array> arrays = new TreeMap<>();                               // A number of different sized arrays concatenated to make one array
  Stack<I>       code = new Stack<>();                                          // Code of the program
  Stack<Label> labels = new Stack<>();                                          // Labels for some instructions
  int []       memory;                                                          // The concatenation of all the arrays
  int         maxTime = 1_000;                                              // Maximum number of steps permitted while running the program
  int            step = 0;                                                      // Execution step
  int            time = 0;                                                      // Execution time
  boolean     running = false;                                                  // Executing if true
  boolean       debug = false;                                                  // Debug if true
  int intermediateValue;                                                        // Written by get and used by set if no value has been supplied

  Ban()                                                                         // Create a basic array machine
   {load();                                                                     // Load array definitions
    memory = new int[size];                                                     // Create the memory for the basic array machine
   }

  Ban exec()                                                                    // Fork a basic array machine copying memory but not the code
   {final Ban source = this, target = new Ban() {void load(){};};
    target.order  = source.order;
    target.arrays = source.arrays;
    final int M = source.memory.length;
    for (int i = 0; i < M; i++) target.memory[i] = source.memory[i];
    return target;
   }

  Ban thread()                                                                  // Fork a basic array machine using the same memory but not the code
   {final Ban source = this, target = new Ban() {void load(){};};
    target.order  = source.order;
    target.arrays = source.arrays;
    target.memory = source.memory;
    return target;
   }

  void load() {}                                                                // Override this method to define the layout

  private void wantRunning  () {if (!running) stop("Too early: not running yet");} // This operation can only occur when we are running
  private void wantCompiling() {if ( running) stop("Too late: not compiling");} // This operation can only occur when we are compiling

  class Array                                                                   // Define an array in the basic array machine
   {final String name;                                                          // Name of the array
    final int[]dimensions;                                                      // Dimensions of the array
    int base;                                                                   // Base position of this array
    final int length;                                                           // Total number of elements in this array
    boolean quiet = false;                                                      // Do not print this field

    Array(String Name, int...Dimensions)                                        // Array definition within the basic array machine
     {wantCompiling();
      name = Name;
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
      if (arrays.containsKey(name))
       {stop("Name :", name, "has already been defined");
       }
      arrays.put(name, this);                                                   // Make the array accessible by name
     }
   }

  protected Array array(String Name, int...Dimensions)                          // Array definition within the basic array machine
   {return new Array(Name, Dimensions);
   }

  protected Array overlay(String Name, int...Dimensions)                        // Overlay this array definition over the previous one
   {wantCompiling();
    if (order.size() == 0) stop("No previous array to overlay");                // Nothing to overlay
    final Array p = order.lastElement();                                        // Previous array which we will overlay
    final Array q = array(Name, Dimensions);                                    // New array
    q.base = p.base;                                                            // Overlay on previous array
    size   = size - p.length + max(p.length, q.length);                         // Account for overall size
    return q;                                                                   // Return overlay
   }

  private Array getArray(String Name)                                           // Get an array by name or complain if the name is invalid
   {final Array A = arrays.get(Name);
    if (A == null) stop("No such array as", Name);
    return A;
   }

  private int lookUpIndex(Array Array, int Dimension, String Index)             // Look up an index which will be used in the specified dimension of the specified array
   {int v;                                                                      // The value of the index
    try
     {v = Integer.parseInt(Index);                                              // The integer has been supplied directly as a string that we can parse
     }
    catch (NumberFormatException e)                                             // The index is a reference to an array
     {final Array I = getArray(Index);                                          // Hopefully the index is a field - a zero dimensional array
      if (I.dimensions.length > 0)                                              // Complain if the index is not a field
       {stop("Index:", Index, "cannot itself be be an indexed array element");
       }
      v = memory[I.base];                                                       // The value of the index
      }
    if (v < 0) stop("Index:", Index, "is negative");
    if (v >= Array.dimensions[Dimension]) stop("Index:", Index, "value", v, "is greater than dimension["+Dimension+"] with extent:", Array.dimensions[Dimension], "in array:", Array.name);
    return v;
   }

  int i() {return intermediateValue;}                                           // Return intermediate value making it read only

  void setMemory(int Value, String target, String...Indices)                    // Set the value of an array element to a specified value or the current value of the intermediate value outside an instruction
   {wantCompiling();
    final Array t   = getArray(target);
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

  void set(Integer Value, String target, String...Indices)                      // Set the value of an array element to a specified value or the current value of the intermediate value
   {wantCompiling();
    final Array t   = getArray(target);
    final String[]i = Indices;

    final int I = i.length;
    final int T = t.dimensions.length;
    if (T != I) stop("Wrong number of dimensions:", T, "!=", I, "for array:", target);

    new I()                                                                     // Assign value to the indicated array element
     {void a()
       {final int v = Value != null ? Value : intermediateValue;
        final int V = switch(T)
         {case  0 -> memory[t.base]                                                                 = v;
          case  1 -> memory[t.base+lookUpIndex(t, 0, i[0])]                                         = v;
          default -> memory[t.base+lookUpIndex(t, 1, i[0])*t.dimensions[0]+lookUpIndex(t, 0, i[1])] = v;
         };
       }
      String v()
       {final int v = Value != null ? Value : intermediateValue;
        switch(T)
         {case  0: return "memory["+(t.base                                                                )+"] <= " + v + "; /*set 1 */";
          case  1: return "memory["+(t.base+lookUpIndex(t, 0, i[0])                                        )+"] <= " + v + "; /*set 2 */";
          default: return "memory["+(t.base+lookUpIndex(t, 1, i[0])*t.dimensions[0]+lookUpIndex(t, 0, i[1]))+"] <= " + v + "; /*set 3 */";
         }
       }
     };
   }

  void set(String target, String...Indices)                                     // Set the value of an array element from the intermediate value
   {set(null, target, Indices);
   }

  void loadArray(String target, int...values)                                   // Load an array
   {wantCompiling();
    final Array t = getArray(target);
    final int   N = values.length;
    if (N > t.length) stop("Too many initializers for array:", t.name, "with length", t.length);

    for (int i = 0; i < N; i++) memory[t.base+i] = values[i];
   }

  void get(String source, String...Indices)                                     // Get the value of an array element
   {wantCompiling();
    final Array s   = getArray(source);
    final String[]i = Indices;

    final int I = i.length;
    final int S = s.dimensions.length;
    if (S != I) stop("Wrong number of dimensions:", S, "!=", I, "for array:", source);

    new I()
     {void a()
       {intermediateValue = switch(S)
         {case  0 -> memory[s.base];
          case  1 -> memory[s.base+lookUpIndex(s, 0, i[0])];
          default -> memory[s.base+lookUpIndex(s, 1, i[0])*s.dimensions[0]+lookUpIndex(s, 0, i[1])];
         };
       }
      String v()
       {switch(S)
         {case  0: return "intermediateValue <= memory["+(s.base                                                                )+"]; /* get 1 */";
          case  1: return "intermediateValue <= memory["+(s.base+lookUpIndex(s, 0, i[0])                                        )+"]; /* get 2 */";
          default: return "intermediateValue <= memory["+(s.base+lookUpIndex(s, 1, i[0])*s.dimensions[0]+lookUpIndex(s, 0, i[1]))+"]; /* get 3 */";
         }
       }
     };
   }

  int getMemory(String source, String...Indices)                                // Get the value of an array element from memory
   {final Array s   = getArray(source);
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

  String getMemoryName(String source, String...Indices)                         // Get the name of an array element in memory
   {final Array s   = getArray(source);
    final String[]i = Indices;

    final int I = i.length;
    final int S = s.dimensions.length;
    if (S != I) stop("Wrong number of dimensions:", S, "!=", I, "for array:", source);

    switch(S)
     {case  0: return "memory["+(s.base                                                                )+"]";
      case  1: return "memory["+(s.base+lookUpIndex(s, 0, i[0])                                        )+"]";
      default: return "memory["+(s.base+lookUpIndex(s, 1, i[0])*s.dimensions[0]+lookUpIndex(s, 0, i[1]))+"]";
     }
   }

  void clear(int Value, String target, String...Indices)                        // Clear the indicated part of the specified array to the the specified value presumed to be a constant, typically -1 or 0
   {wantCompiling();
    final Array t   = getArray(target);
    final String[]i = Indices;

    final int I = i.length;
    final int T = t.dimensions.length;
    if (I > T) stop("Too many dimensions:", I, ">", T, "for array:", target);

    if (I == 0)                                                                 // Clear entire array
     {for (int j = 0; j < t.length; j++)
       {final int J = j;
        new I()
         {void   a() {memory[t.base + J] = Value;}
          String v() {return "memory["+(t.base + J)+"] <= "+Value+"; /* clear 1 */";}
         };
       }
      return;
     }

    if ((I == 1 && T == 1) || (I == 2 && T == 2))                               // Clear single element of one or two dimensional array
     {set(Value, target, Indices);
      return;
     }

    if (I == 1 && T == 2)                                                       // Clear indicated sub array
     {final int l = t.dimensions[0];                                            // Size of sub array
      for (int j = 0; j < l; j++)                                               // Clear sub array in two dimensional array
       {final int J = j;
        new I()
         {void   a() {        memory[   t.base + l * lookUpIndex(t, 1, i[0]) + J   ] =   Value;}
          String v() {return "memory["+(t.base + l * lookUpIndex(t, 1, i[0]) + J)+"] <= "+Value+"; /* clear 2 */";}
         };
       }
      return;
     }
   }

  void zero(String source, String...Indices) {clear( 0, source, Indices);}      // Zero the indicated part of the specified array
  void ones(String source, String...Indices) {clear(-1, source, Indices);}      // Set the indicated part of the specified array to all ones

  void move(String target, String source, String...Indices)                     // Copy the indexed source field to the indexed target fields
   {wantCompiling();
    final Array t = getArray(target), s = getArray(source);

    final int I = Indices.length;
    final int T = t.dimensions.length, S = s.dimensions.length;
    if (T + S != I) stop("Wrong number of dimensions:", T, "+", S, "!=", I, "for arrays:", target+",", source);

    final String[]ti = new String[T];                                           // Assign indices
    final String[]si = new String[S];
    for (int i = 0; i < T; i++) ti[i]   = Indices[i];
    for (int i = T; i < I; i++) si[i-T] = Indices[i];

    get(source, si);
    set(target, ti);
   }

  void compare(int immediate, String source, String...Indices)                  // Compare the indexed source field to the immediate value setting the intermediate value to -1 if is less that source, 0 if equal otherwise +1
   {wantCompiling();
    final Array s = getArray(source);

    final int I = Indices.length;
    final int S = s.dimensions.length;
    if (S != I) stop("Wrong number of dimensions:", S, "!=", I, "for array:", source);

    get(source, Indices);

    new I()
     {void a()
       {final int a = immediate;
        final int b = intermediateValue;
        intermediateValue = Integer.valueOf(b).compareTo(a);
       }
      String v()
       {final String a = ""+immediate;
        final String b = "intermediateValue";
        return "intermediateValue <= "+a+" <  intermediateValue ? -1 : "+a+" == intermediateValue ?  0 : +1; /* compare 1 */";
       }
     };
   }

  void compare(String target, String source, String...Indices)                  // Compare the indexed source fields setting the intermediate value to -1 if source is less that target, 0 if equal otherwise +1
   {wantCompiling();
    final Array t = getArray(target), s = getArray(source);

    final int I = Indices.length;
    final int T = t.dimensions.length, S = s.dimensions.length;
    if (T + S != I) stop("Wrong number of dimensions:", T, "+", S, "!=", I);    // Check we have the right number of indices

    final String[]ti = new String[T];                                           // Assign indices
    final String[]si = new String[S];
    for (int i = 0; i < T; i++) ti[i]   = Indices[i];
    for (int i = T; i < I; i++) si[i-T] = Indices[i];

    new I()
     {void a()
       {final int b = getMemory(source, si);
        final int a = getMemory(target, ti);
        intermediateValue = Integer.valueOf(a).compareTo(b);
       }
      String v()
       {final String b = getMemoryName(source, si);
        final String a = getMemoryName(target, ti);
        return "intermediateValue <= "+a+" < "+b+" ? -1 : "+a+" == "+b+" ?  0 : +1; /* compare 2 */";
       }
     };
   }

  void gt() {new I() {void a(){intermediateValue = intermediateValue >  0 ? 1 : 0;} String v() {return "intermediateValue <= intermediateValue >  0 ? 1 : 0; /* gt */";}};} // Set the intermediate value to one if it is currently greater than             zero else zero
  void ge() {new I() {void a(){intermediateValue = intermediateValue >= 0 ? 1 : 0;} String v() {return "intermediateValue <= intermediateValue >= 0 ? 1 : 0; /* ge */";}};} // Set the intermediate value to one if it is currently greater than or equal to zero else zero
  void lt() {new I() {void a(){intermediateValue = intermediateValue <  0 ? 1 : 0;} String v() {return "intermediateValue <= intermediateValue <  0 ? 1 : 0; /* lt */";}};} // Set the intermediate value to one if it is currently less    than             zero else zero
  void le() {new I() {void a(){intermediateValue = intermediateValue <= 0 ? 1 : 0;} String v() {return "intermediateValue <= intermediateValue <= 0 ? 1 : 0; /* le */";}};} // Set the intermediate value to one if it is currently less    than or equal to zero else zero
  void eq() {new I() {void a(){intermediateValue = intermediateValue == 0 ? 1 : 0;} String v() {return "intermediateValue <= intermediateValue == 0 ? 1 : 0; /* eq */";}};} // Set the intermediate value to one if it is currently equal                 to zero else zero
  void ne() {new I() {void a(){intermediateValue = intermediateValue != 0 ? 1 : 0;} String v() {return "intermediateValue <= intermediateValue != 0 ? 1 : 0; /* ne */";}};} // Set the intermediate value to one if it is currently less        not equal to zero else zero

  void add(int immediate, String source, String...Indices)                      // Add a constant value to the source field
   {wantCompiling();
    final Array s = getArray(source);

    final int I = Indices.length;
    final int S = s.dimensions.length;
    if (S != I) stop("Wrong number of dimensions:", S, "!=", I, "for array:", source);

    get(source, Indices);
    new I()
     {void   a() {        intermediateValue  =   immediate   + intermediateValue;}
      String v() {return "intermediateValue <= "+immediate+" + intermediateValue;  /* add 1 */";}
     };
    set(source, Indices);
   }

  void add(String target, String source, String...Indices)                      // Add the source to the target replacing the target
   {wantCompiling();
    final Array t = getArray(target), s = getArray(source);

    final int I = Indices.length;
    final int T = t.dimensions.length, S = s.dimensions.length;
    if (T + S != I) stop("Wrong number of dimensions:", T, "+", S, "!=", I);    // Check we have the right number of indices

    final String[]ti = new String[T];                                           // Assign indices
    final String[]si = new String[S];
    for (int i = 0; i < T; i++) ti[i]   = Indices[i];
    for (int i = T; i < I; i++) si[i-T] = Indices[i];

    new I()
     {void   a() {        intermediateValue  =   getMemory    (source, si)   +   getMemory    (target, ti)               ;}
      String v() {return "intermediateValue <= "+getMemoryName(source, si)+" + "+getMemoryName(target, ti)+"; /* add2 */";}
     };
    set(target, ti);
   }

  void inc(String source, String...Indices) {add(+1, source, Indices);}         // Increment a field
  void dec(String source, String...Indices) {add(-1, source, Indices);}         // Increment a field

  void subtract(String target, String source, String...Indices)                 // Subtract the source from the target and replace the target
   {wantCompiling();
    final Array t = getArray(target), s = getArray(source);

    final int I = Indices.length;
    final int T = t.dimensions.length, S = s.dimensions.length;
    if (T + S != I) stop("Wrong number of dimensions:", T, "+", S, "!=", I);    // Check we have the right number of indices

    final String[]ti = new String[T];                                           // Assign indices
    final String[]si = new String[S];
    for (int i = 0; i < T; i++) ti[i]   = Indices[i];
    for (int i = T; i < I; i++) si[i-T] = Indices[i];

    new I()
     {void   a() {        intermediateValue  =   getMemory    (target, ti)   -   getMemory    (source, si);                   }
      String v() {return "intermediateValue <= "+getMemoryName(target, ti)+" - "+getMemoryName(source, si)+"; /* subtract */";}
     };
    set(target, ti);
   }

  void multiply(String target, String source, String...Indices)                 // Multiply the target by the source and replace the target
   {wantCompiling();
    final Array t = getArray(target), s = getArray(source);

    final int I = Indices.length;
    final int T = t.dimensions.length, S = s.dimensions.length;
    if (T + S != I) stop("Wrong number of dimensions:", T, "+", S, "!=", I);    // Check we have the right number of indices

    final String[]ti = new String[T];                                           // Assign indices
    final String[]si = new String[S];
    for (int i = 0; i < T; i++) ti[i]   = Indices[i];
    for (int i = T; i < I; i++) si[i-T] = Indices[i];

    new I()
     {void   a() {        intermediateValue  =   getMemory    (source, si)   *   getMemory    (target, ti);                   }
      String v() {return "intermediateValue <= "+getMemoryName(target, ti)+" * "+getMemoryName(source, si)+"; /* multiply */";}
     };
    set(target, ti);
   }

  void modulus(String target, String source, String...Indices)                  // Save the target modulus the source into the source
   {wantCompiling();
    final Array t = getArray(target), s = getArray(source);

    final int I = Indices.length;
    final int T = t.dimensions.length, S = s.dimensions.length;
    if (T + S != I) stop("Wrong number of dimensions:", T, "+", S, "!=", I);    // Check we have the right number of indices

    final String[]ti = new String[T];                                           // Assign indices
    final String[]si = new String[S];
    for (int i = 0; i < T; i++) ti[i]   = Indices[i];
    for (int i = T; i < I; i++) si[i-T] = Indices[i];

    new I()
     {void   a() {        intermediateValue  =   getMemory    (source, si)   %   getMemory    (target, ti);                  }
      String v() {return "intermediateValue <= "+getMemoryName(target, ti)+" % "+getMemoryName(source, si)+"; /* modulus */";}
     };
    set(target, ti);
   }

  void shiftRight(String source, String...Indices)                              // Shift the source by one place to the right inserti g a one if the number os  neagtive else a zero
   {wantCompiling();
    final Array s = getArray(source);

    final int I = Indices.length;
    final int S = s.dimensions.length;
    if (S != I) stop("Wrong number of dimensions:", S, "!=", I);                // Check we have the right number of indices

    get(source, Indices);
    new I()
     {void   a() {        intermediateValue  = intermediateValue >> 1;                    }
      String v() {return "intermediateValue <= intermediateValue >> 1; /* shift right */";}
     };
    set(source, Indices);
   }

  public String toString()                                                      // Print layout
   {wantCompiling();
    final StringBuilder s = new StringBuilder();
    int l = 0;
    for  (Array a: order)
     {if (a.quiet) continue;
      if      (a.dimensions.length == 0)
       {    final int v = memory[a.base];
            if (v == 0) continue;
            s.append(String.format("%4d  %4d               %4d  %s %s\n", l++, a.base,     memory[a.base], a.name, "-"));
       }
      else if (a.dimensions.length == 1)
       {for   (int i = 0; i < a.dimensions[0]; i++)
         {  final int v = memory[a.base+i];
            if (v == 0) continue;
            s.append(String.format("%4d  %4d        %4d   %4d  %s %s\n", l++, a.base, i,   memory[a.base+i], a.name, i == 0 ? "-" : "|"));
         }
       }
      else if (a.dimensions.length == 2)
       {for   (int j = 0; j < a.dimensions[1]; j++)
         {for (int i = 0; i < a.dimensions[0]; i++)
           {final int v = memory[a.base+j*a.dimensions[0]+i];
            if (v == 0) continue;
            s.append(String.format("%4d  %4d  %4d  %4d   %4d  %s %s\n", l++, a.base, j, i, memory[a.base+j*a.dimensions[0]+i], a.name,  i == 0 && j == 0 ? "-" : i == 0 ?  "+" : "|"));
           }
         }
       }
     }

    return ""+s;
   }

  String print(String Source, String...Indices)                                 // Print the indicated part of the array
   {wantCompiling();
    final Array s   = getArray(Source);
    final String[]i = Indices;
    final StringBuilder t = new StringBuilder();

    final int I = i.length;
    final int S = s.dimensions.length;
    if (I > S) stop("Too many dimensions:", I, ">", S, "for array:", Source);

    if (I == 0)                                                                 // Print entire array
     {if (s.dimensions.length == 0)
       {return ""+getMemory(Source, Indices);
       }
      if (s.dimensions.length == 1)
       {for (int j = 0; j < s.length; j++) t.append(""+memory[s.base + j]+", ");
        t.setLength(t.length()-2);
        return ""+t;
       }
      for   (int k = 0; k < s.dimensions[1]; k++)
       {t.append("[");
        for (int j = 0; j < s.dimensions[0]; j++)
         {t.append(""+memory[s.base + k*s.dimensions[0]+ j]+", ");
         }
        t.setLength(t.length()-2);
        t.append("],\n");
       }
      t.setLength(t.length()-2);
      return ""+t+"\n";
     }

    if ((I == 1 && S == 1) || (I == 2 && S == 2))                               // Print single element of one or two dimensional array
     {return ""+getMemory(Source, Indices);
     }

    if (I == 1 && S == 2)                                                       // Print indicated sub array
     {final int l = s.dimensions[0];                                            // Size of sub array
      final int p = l * lookUpIndex(s, 1, i[0]);                                // Start of sub array
      for (int j = 0; j < l; j++) t.append(""+memory[s.base+ p + j]+", ");      // Clear sub array in two dimensional array
      t.setLength(t.length()-2);
      return ""+t;
     }
    return "";
   }

  class Label                                                                   // Label definition
   {int instruction;                                                            // The instruction to which this labels applies
    Label() {set(); labels.push(this);}                                         // A label assigned to an instruction
    void set() {wantCompiling(); instruction = code.size();}                    // Reassign the label to an instruction
   }

  class I                                                                       // Instruction definition
   {int instructionNumber;                                                      // Instruction number
    final String definition = traceBack();                                      // Location of code that defined this instruction
    I()                                                                         // Define an instruction
     {if (running) stop("Cannot define instructions during program execution",
       definition);
       instructionNumber = code.size(); code.push(this);
     }
    void   a() {}                                                               // Action performed by instruction
    String v() {Test.stop("No verilog supplied\n"+definition); return "";}      // Initialization for instruction
    String n() {return "instruction";}                                          // Instruction name
   }

//D1 Execute                                                                    // Execute the program

  void run()                                                                    // Run the program
   {wantCompiling();
    z();
    running = true;
    final int N = code.size();
    for (step = 0, time = 0; step < N && time < maxTime; step++, time++)
     {z(); code.elementAt(step).a();
     }
    running = false;
    if (time >= maxTime) stop("Out of time:", time);
   }

  void stop(String em)                                                          // Stop everything with an explanatory message
   {z();
    new I() {void a() {Test.stop(em);} String v() {return "$finish(1);";}};
   }

  void clearCode() {z(); code.clear(); running = false;}                        // Clear the program code

//D1 Blocks                                                                     // Blocks of code used to implement if statements and for loops

  void Goto(Label label) {new I() {void a() {                            step = label.instruction-1;} String v() {return                             "step = label.instruction-1;";}};} // The program execution for loop will increment first
  void GoEq(Label label) {new I() {void a() {if (intermediateValue == 0) step = label.instruction-1;} String v() {return "if (intermediateValue == 0) step = label.instruction-1;";}};} // Go to a specified label if the intermediate value is equal to zero
  void GoNe(Label label) {new I() {void a() {if (intermediateValue != 0) step = label.instruction-1;} String v() {return "if (intermediateValue != 0) step = label.instruction-1;";}};} // Go to a specified label if the intermediate value is not equal to zero
  void GoGt(Label label) {new I() {void a() {if (intermediateValue >  0) step = label.instruction-1;} String v() {return "if (intermediateValue >  0) step = label.instruction-1;";}};} // Go to a specified label if the intermediate value is greater than zero
  void GoGe(Label label) {new I() {void a() {if (intermediateValue >= 0) step = label.instruction-1;} String v() {return "if (intermediateValue >= 0) step = label.instruction-1;";}};} // Go to a specified label if the intermediate value is greater than or equal to zero
  void GoLt(Label label) {new I() {void a() {if (intermediateValue <  0) step = label.instruction-1;} String v() {return "if (intermediateValue <  0) step = label.instruction-1;";}};} // Go to a specified label if the intermediate value is less than zero
  void GoLe(Label label) {new I() {void a() {if (intermediateValue <= 0) step = label.instruction-1;} String v() {return "if (intermediateValue <= 0) step = label.instruction-1;";}};} // Go to a specified label if the intermediate value is less than or equal to zero

  abstract class Block                                                          // A block that can be continued or exited
   {final Label Start = new Label(), End = new Label();                         // Labels at start and end of block to facilitate continuing or exiting

    Block()
     {code();
      End.set();
     }

    abstract void code();                                                       // Override this method to supply the code of the block

    void start    () {new I() {void a() {                            step = Start.instruction-1;} String v() {return                             "step <= "+(Start.instruction-1)+";";}};}  // Restart the block
    void startIfEq() {new I() {void a() {if (intermediateValue == 0) step = Start.instruction-1;} String v() {return "if (intermediateValue == 0) step <= "+(Start.instruction-1)+";";}};}  // Restart the block if the intermediate value is equal to zero
    void startIfNe() {new I() {void a() {if (intermediateValue != 0) step = Start.instruction-1;} String v() {return "if (intermediateValue != 0) step <= "+(Start.instruction-1)+";";}};}  // Restart the block if the intermediate value is not equal to zero
    void startIfGt() {new I() {void a() {if (intermediateValue >  0) step = Start.instruction-1;} String v() {return "if (intermediateValue >  0) step <= "+(Start.instruction-1)+";";}};}  // Restart the block if the intermediate value is greater than zero
    void startIfGe() {new I() {void a() {if (intermediateValue >= 0) step = Start.instruction-1;} String v() {return "if (intermediateValue >= 0) step <= "+(Start.instruction-1)+";";}};}  // Restart the block if the intermediate value is greater than or equal to zero
    void startIfLt() {new I() {void a() {if (intermediateValue <  0) step = Start.instruction-1;} String v() {return "if (intermediateValue <  0) step <= "+(Start.instruction-1)+";";}};}  // Restart the block if the intermediate value is less than zero
    void startIfLe() {new I() {void a() {if (intermediateValue <= 0) step = Start.instruction-1;} String v() {return "if (intermediateValue <= 0) step <= "+(Start.instruction-1)+";";}};}  // Restart the block if the intermediate value is less than or equal to zero

    void end    ()   {new I() {void a() {                            step =   End.instruction-1;} String v() {return                             "step <=   "+(End.instruction-1)+";";}};}  // End the block
    void endIfEq()   {new I() {void a() {if (intermediateValue == 0) step =   End.instruction-1;} String v() {return "if (intermediateValue == 0) step <=   "+(End.instruction-1)+";";}};}  // End the block if the intermediate value is equal to zero
    void endIfNe()   {new I() {void a() {if (intermediateValue != 0) step =   End.instruction-1;} String v() {return "if (intermediateValue != 0) step <=   "+(End.instruction-1)+";";}};}  // End the block if the intermediate value is not equal to zero
    void endIfGt()   {new I() {void a() {if (intermediateValue >  0) step =   End.instruction-1;} String v() {return "if (intermediateValue >  0) step <=   "+(End.instruction-1)+";";}};}  // End the block if the intermediate value is greater than zero
    void endIfGe()   {new I() {void a() {if (intermediateValue >= 0) step =   End.instruction-1;} String v() {return "if (intermediateValue >= 0) step <=   "+(End.instruction-1)+";";}};}  // End the block if the intermediate value is greater than or equal to zero
    void endIfLt()   {new I() {void a() {if (intermediateValue <  0) step =   End.instruction-1;} String v() {return "if (intermediateValue <  0) step <=   "+(End.instruction-1)+";";}};}  // End the block if the intermediate value is less than zero
    void endIfLe()   {new I() {void a() {if (intermediateValue <= 0) step =   End.instruction-1;} String v() {return "if (intermediateValue <= 0) step <=   "+(End.instruction-1)+";";}};}  // End the block if the intermediate value is less than or equal to zero
   }

//D1 Verilog                                                                    // Generate verilog for the program

  String verilog()                                                              // Generate verilog for the program
   {final StringBuilder v = new StringBuilder();                                // Generated verilog
    final int N = code.size();
    for(int i = 0; i < N; ++i)
     {final String c = code.elementAt(i).v();
      v.append("      "+String.format("%4d", i)+": begin "+c+" end\n");
     }
    return ""+v;
   }

//D0 Tests                                                                      // Testing

  static Ban test_layout()                                                      // A test layout
   {return new Ban()
     {void load()
       {array("c0");
        array("c1");
        array("c2");
        array("c3");
        array("a", 4);
        array("b", 6);
        array("A", 2, 4);
        array("B", 4, 3);
       }
     };
   }

  static Ban test_clear()
   {Ban l = test_layout();
    l.clear( 0, "c0");
    l.clear( 1, "c1");
    l.clear( 2, "c2");
    l.clear( 3, "c3");
    l.clear( 4, "a");
    l.clear( 5, "a", "c2");
    l.clear( 6, "b", "c2");
    l.clear( 7, "b", "c2");
    l.clear( 8, "A", "c1");
    l.clear( 9, "A", "c1", "c2");
    l.clear(11, "B", "c1");
    l.clear(22, "B", "c2");
    l.clear(33, "B", "c1", "c1");
    l.clear(44, "B", "c2", "c2");
    l.run();
    //stop(l);
    ok(""+l, """
   0     1                  1  c1 -
   1     2                  2  c2 -
   2     3                  3  c3 -
   3     4           0      4  a -
   4     4           1      4  a |
   5     4           2      5  a |
   6     4           3      4  a |
   7     8           2      7  b |
   8    14     1     0      8  A +
   9    14     1     1      8  A |
  10    14     1     2      9  A |
  11    14     1     3      8  A |
  12    22     1     0     11  B +
  13    22     1     1     33  B |
  14    22     1     2     11  B |
  15    22     2     0     22  B +
  16    22     2     1     22  B |
  17    22     2     2     44  B |
""");
    return l;
   }

  static void test_get()
   {Ban l = test_clear();
    l.get("B", "c2", "c2"); l.run(); ok(l.intermediateValue, 44);
    l.get("B", "c1", "c1"); l.run(); ok(l.intermediateValue, 33);
    l.get("a", "c3");       l.run(); ok(l.intermediateValue,  4);
   }

  static Ban test_move()
   {Ban l = test_clear();
    l.move("B", "a", "c3", "c1", "c1");
    l.move("B", "a", "c3", "c2", "c2");
    l.run();
    //stop(l);
    ok(""+l, """
   0     1                  1  c1 -
   1     2                  2  c2 -
   2     3                  3  c3 -
   3     4           0      4  a -
   4     4           1      4  a |
   5     4           2      5  a |
   6     4           3      4  a |
   7     8           2      7  b |
   8    14     1     0      8  A +
   9    14     1     1      8  A |
  10    14     1     2      9  A |
  11    14     1     3      8  A |
  12    22     1     0     11  B +
  13    22     1     1     33  B |
  14    22     1     2     11  B |
  15    22     2     0     22  B +
  16    22     2     1     22  B |
  17    22     2     2     44  B |
  18    22     3     1      4  B |
  19    22     3     2      5  B |
""");
    return l;
   }

  static void test_compare()
   {Ban l = test_move();
    l.compare(4, "B", "c3", "c2");               l.run(); ok(l.intermediateValue, +1);
    l.compare(5, "B", "c3", "c2");               l.run(); ok(l.intermediateValue,  0);
    l.compare(6, "B", "c3", "c2");               l.run(); ok(l.intermediateValue, -1);
    l.compare("B", "B", "c3", "c2", "c3", "c1"); l.run(); ok(l.intermediateValue, +1);
    l.compare("B", "B", "c3", "c2", "c2", "c2"); l.run(); ok(l.intermediateValue, -1);
    l.compare("B", "a", "c3", "c2", "c2");       l.run(); ok(l.intermediateValue,  0);
   }

  static void test_compare_int()                                                // Encode some indices directly instead of indirectly
   {Ban l = test_move();
    l.compare(4, "B",    "3",  "c2");            l.run(); ok(l.intermediateValue, +1);
    l.compare(5, "B",    "3",  "c2");            l.run(); ok(l.intermediateValue,  0);
    l.compare(6, "B",    "3",  "c2");            l.run(); ok(l.intermediateValue, -1);
    l.compare("B", "B", "3", "c2", "3",  "c1");  l.run(); ok(l.intermediateValue, +1);
    l.compare("B", "B", "3", "c2", "c2",  "c2"); l.run(); ok(l.intermediateValue, -1);
    l.compare("B", "a", "3", "c2", "c2");        l.run(); ok(l.intermediateValue,  0);
   }


  static void test_add()
   {Ban l = test_move();

    l.add(1, "B", "c3", "c1");
    l.get("B", "c3", "c1");
    l.run();
    ok(l.intermediateValue,  5);

    l.add("B", "B", "c3", "c2", "c3", "c1");
    l.get("B", "c3", "c2");
    l.run();
    ok(l.intermediateValue, 10);
   }

  static void test_shiftRight()
   {Ban l = test_move();
    l.shiftRight("A", "c1", "c2"); l.run();  ok(l.intermediateValue, 4);
    l.shiftRight("A", "c1", "c2"); l.run();  ok(l.intermediateValue, 2);
   }

  static Ban test_overlay()                                                     // A test layout with overlays
   {final int M = 4, N = 3;
     final Ban l = new Ban()
     {void load()
       {array("c1");
        array("c2");
        array("c3");
        array("a", M*N);
        overlay("b", M, N);
       }
     };

    for (int i = 0; i < M*N; i++)
     {l.set(i,      "c1");
      l.set(i, "a", "c1");
     }
    l.run();

    for   (int i = 0; i < M; i++)
     {for (int j = 0; j < N; j++)
       {l.clearCode();
        l.set(i,   "c1");
        l.set(j,   "c2");
        l.get("b", "c1", "c2");
        l.run();
        ok(l.intermediateValue, i*N+j);
       }
     }
    return l;
   }

  static void test_set()
   {final int M = 4, N = 3;
     final Ban l = new Ban()
     {void load()
       {array("c0");
        array("c1");
        array("c2");
        array("c3");
        array("a", M);
        array("b", M, N);
       }
     };

    for (int i = 0; i < M; i++) l.set(i, "c"+i);
    for (int i = 0; i < M; i++) l.set(i, "a",   ""+i);

    for   (int i = 0; i < M; i++)
     {for (int j = 0; j < N; j++)
       {l.set(i*N+j, "b", "c"+i, "c"+j);
       }
     }

    l.run();
    //stop(l);
    ok(""+l, """
   0     1                  1  c1 -
   1     2                  2  c2 -
   2     3                  3  c3 -
   3     4           1      1  a |
   4     4           2      2  a |
   5     4           3      3  a |
   6     8     0     1      1  b |
   7     8     0     2      2  b |
   8     8     1     0      3  b +
   9     8     1     1      4  b |
  10     8     1     2      5  b |
  11     8     2     0      6  b +
  12     8     2     1      7  b |
  13     8     2     2      8  b |
  14     8     3     0      9  b +
  15     8     3     1     10  b |
  16     8     3     2     11  b |
""");
   }

  static Ban test_initialize()
   {final Ban l = new Ban()
     {void load()
       {array("a");
        array("b", 2);
        array("c", 2, 3);
       }
     };

    l.loadArray("a", 1);
    l.loadArray("b", 1, 2);
    l.loadArray("c", 11, 12, 13, 21, 22, 23);
    l.run();
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
    return l;
   }

  static void test_print()
   {final Ban l = test_initialize();

    ok(l.print("a"), "1");
    ok(l.print("b"), "1, 2");
    ok(l.print("c"), """
[11, 12, 13],
[21, 22, 23]
""");
   }

  static void test_sum()
   {final Ban l = new Ban()
     {void load()
       {array("i");
        array("s");
       }
     };

    l.set(0, "i");
    l.set(0, "s");
    l.new Block()
     {void code()
       {l.add ("s",   "i");
        l.add (1,     "i");
        l.compare(10, "i"); endIfGt(); start();
       }
     };
    l.run();
    ok(l.getMemory("s"), 55);
   }

  static void test_fibonacci()
   {final Ban l = new Ban()
     {void load()
       {array("a");
        array("b");
        array("c");
       }
     };

    Stack<Integer> f = new Stack<>();
    l.set(1, "b");

    l.new Block()
     {void code()
       {l.move("c", "a");
        l.add ("c", "b");
        l.new I() {void a() {f.push(l.intermediateValue);}};
        l.move("a", "b");
        l.move("b", "c");
        l.compare(100, "c"); endIfGt(); start();
       }
     };
    l.run();
    ok(f, "[1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144]");
   }

  static void test_euclid()
   {final int N = 9, p = 11, q = 13;
    final Ban l = new Ban()
     {void load()
       {array("a");
        array("b");
       }
     };

    l.set(N*p, "a");
    l.set(N*q, "b");

    l.new Block()
     {void code()
       {final Block outer = this;
        l.new Block()
         {void code()
           {l.compare("a", "b");
            outer.endIfEq();
            endIfGt();
            l.subtract("b", "a");
            outer.start();
           }
         };
        l.subtract("a", "b");
        outer.start();
       }
     };
    l.run();
    ok(l.getMemory("a"), N);
   }

  static void test_bubble_sort()
   {final int N = 8;
    final Ban l = new Ban()
     {void load()
       {array("i");
        array("j");
        array("t");
        array("a", N);
       }
     };

    for (int i = 0; i < N; i++) l.set(N - i, "a", ""+i);                        // Load in reverse order

    l.new Block()                                                               // Outer loop
     {void code()
       {l.move("j", "i");
        l.new Block()                                                           // Inner loop
         {void code()
           {l.new Block()                                                       // If
             {void code()
               {l.compare("a", "a", "i", "j");
                endIfLe();
                l.move("t", "a", "i");
                l.move("a", "a", "i", "j");
                l.move("a", "t", "j");
               }
             };
            l.inc("j");
            l.compare(N, "j"); endIfGe(); start();
           }
         };
        l.inc("i");
        l.compare(N, "i"); endIfGe(); start();
       }
     };
    l.run();
    ok(l.print("a"), "1, 2, 3, 4, 5, 6, 7, 8");
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_clear();
    test_set();
    test_get();
    test_move();
    test_compare();
    test_compare_int();
    test_add();
    test_shiftRight();
    test_overlay();
    test_clear();
    test_initialize();
    test_print();
    test_move();
    test_sum();
    test_fibonacci();
    test_euclid();
    test_bubble_sort();
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
