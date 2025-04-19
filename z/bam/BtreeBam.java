//------------------------------------------------------------------------------
// Btree on the Basic Array Machine for performance comparision with BtreeSF
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2025
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Btree in a block on the surface of a silicon chip.

import java.util.*;

class BtreeBam extends Test                                                     // Manipulate a btree
 {final int
    maxKeysPerLeaf,                                                             // Maximum number of leafs in a key
    maxKeysPerBranch,                                                           // Maximum number of keys in a branch
    splitLeafSize,                                                              // The number of key, data pairs to split out of a leaf
    splitBranchSize,                                                            // The number of key, next pairs to split out of a branch
    numberOfNodes;                                                              // The number of noes in the btree

  final static int
   linesToPrintABranch =  4,                                                    // The number of lines required to print a branch
        maxPrintLevels = 10,                                                    // Maximum number of levels to print in a tree
              maxDepth = 99;                                                    // Maximum depth of any realistic tree

  static boolean debug = false;                                                 // Debugging enabled

//D1 Construction                                                               // Create a BTree from nodes which can be branches or leaves.  The data associated with the BTree is stored only in the leaves opposite the keys

  BtreeBam(int MaxKeysPerLeaf, int MaxKeysPerBranch, int NumberOfNodes)         // Define a BTree with the specified dimensions
   {zz();
    maxKeysPerLeaf   = MaxKeysPerLeaf;
    maxKeysPerBranch = MaxKeysPerBranch;
    splitLeafSize    = maxKeysPerLeaf   >> 1;
    splitBranchSize  = maxKeysPerBranch >> 1;
    numberOfNodes    = NumberOfNodes;
   }

  abstract class Layout                                                         // Layout the array used by the basic array machine
   {int size = 0;
    final Stack         <Array> order  = new Stack<>();                         // The order in which the arrays were defined
    final TreeMap<String,Array> arrays = new TreeMap<>();                       // A number of different sized arrays concatenated to make one array
    final int[]memory;                                                          // The concatenation of all the arrays

    class Array
     {final String name;
      final int[]dimensions;
      final int base;
      Array(String Name, int...Dimensions)                                      // Array definition within the basic array machine
       {name = Name;
        final int D = Dimensions.length;
        if (D > 2) stop("Too many dimensions:", D);                             // Too many dimensions
        dimensions = new int[D];
        for (int i = 0; i < D; i++) dimensions[i] = Dimensions[i];              // Save the dimensions. A zero dimension array is a field.
        base = size; size = base + D;                                           // Place this array in the basic array machine
        order.push(this);                                                       // The definition order
        arrays.put(name, this);                                                 // Make the array accessible by name
       }
     }
    void load() {}                                                              // Override this method to define the layout

    Layout()                                                                    // Create a layout
     {load();                                                                   // Load array definitions
      memory = new int[size];                                                   // Create the memeory for the basic array machine
     }

    Array get(String Name)                                                      // Get an array by name or complain if the name is invalid
     {final Array A = arrays.get(Name);
      if (A == null) stop("No such array as", Name);
      return A;
     }

    int lookUpIndex(Array Array, int Dimension, String Index)                   // Look up an index which will be used in the specified dimension of the specified array
     {final Array I = get(Index);                                               // Hopefully the index is a field
      if (I.dimensions.length > 0)                                              // Complain if the index is not a field
       {stop("Index:", Index, "cannot itself be be an indexed array element");
       }
      final int v = memory[I.base];                                             // The value of the index
      if (v < 0) stop("Index:", Index, "is negative");
      if (v >= Array.dimensions[Dimension]) stop("Index:", Index, "is greater than dimension["+Dimension+"] ==", Dimension, "with extent:", Array.dimensions[Dimension]);
      return v;
     }

    void move(String target, String source, String...Indices)                   // Copy the indexed source field to the indexed target fields
     {final Array t   = get(target), s = get(source);
      final String[]i = Indices;

      final int I = Indices.length;
      final int T = t.dimensions.length, S = s.dimensions.length;
      if (T + S != I) stop("Wrong number of dimensions", T, "+", S, "!=", I);   // Check we have the right number of indices

      final int v = switch(T)
       {case    0 -> switch(S)
         {case  0 -> memory[t.base]                                                                 = memory[s.base];
          case  1 -> memory[t.base]                                                                 = memory[s.base+lookUpIndex(s, 0, i[0])];
          default -> memory[t.base]                                                                 = memory[s.base+lookUpIndex(s, 1, i[1])*s.dimensions[0]+lookUpIndex(s, 0, i[0])];
         };
        case    1 -> switch(S)
         {case  0 -> memory[t.base+lookUpIndex(t, 0, i[0])]                                         = memory[s.base];
          case  1 -> memory[t.base+lookUpIndex(t, 0, i[0])]                                         = memory[s.base+lookUpIndex(s, 0, i[0])];
          default -> memory[t.base+lookUpIndex(t, 0, i[0])]                                         = memory[s.base+lookUpIndex(s, 1, i[1])*s.dimensions[0]+lookUpIndex(s, 0, i[0])];
         };
        default   -> switch(S)
         {case  0 -> memory[t.base+lookUpIndex(t, 1, i[1])*t.dimensions[0]+lookUpIndex(t, 0, i[0])] = memory[s.base];
          case  1 -> memory[t.base+lookUpIndex(t, 1, i[1])*t.dimensions[0]+lookUpIndex(t, 0, i[0])] = memory[s.base+lookUpIndex(s, 0, i[0])];
          default -> memory[t.base+lookUpIndex(t, 1, i[1])*t.dimensions[0]+lookUpIndex(t, 0, i[0])] = memory[s.base+lookUpIndex(s, 1, i[1])*s.dimensions[0]+lookUpIndex(s, 0, i[0])];
         };
       };
     }

    public String toString()                                                    // Print layout
     {final StringBuilder s = new StringBuilder();
      int l = 0;
      for  (Array a: order)
       {if      (a.dimensions.length == 0)
         {    s.append(String.format("%4d  %4d            %4d   %s\n", l++, a.base,      memory[a.base],    a.name));
         }
        else if (a.dimensions.length == 1)
         {for   (int i = 0; i < a.dimensions[0]; i++)
           {  s.append(String.format("%4d  %4d  %4d       %4d   %s\n", l++, a.base, i,   memory[a.base+i],  a.name));
           }
         }
        else if (a.dimensions.length == 2)
         {for   (int i = 0; i < a.dimensions[0]; i++)
           {for (int j = 0; j < a.dimensions[1]; j++)
             {s.append(String.format("%4d  %4d  %4d  %4d   %4d  %s\n", l++, a.base, i, j, memory[a.base+i], a.name));
             }
           }
         }
       }

      return ""+s;
     }
   }

  Layout layout()                                                               // Layout the btree
   {return new Layout()
     {void load()
       {new Array("freeChainHead");                                             // The head of the free chain
        new Array("free",               numberOfNodes);                         // Used to place the node  on the free chain else zero if in use
        new Array("isLeaf",             numberOfNodes);                         // Whether each node is a leaf or a branch
        new Array("branch_currentSize", numberOfNodes);                         // Branch
        new Array("branch_keys",        numberOfNodes, maxKeysPerBranch);
        new Array("branch_data",        numberOfNodes, maxKeysPerBranch+1);     // Allow for top
        new Array("leaf_currentSize",   numberOfNodes);                         // Leaf
        new Array("leaf_keys",          numberOfNodes, maxKeysPerLeaf);
        new Array("leaf_data",          numberOfNodes, maxKeysPerLeaf);
       }
     };
   }

//D0 Tests                                                                      // Testing

  static void oldTests()                                                        // Tests thought to be in good shape
   {
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
