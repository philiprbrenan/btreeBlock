//------------------------------------------------------------------------------
// Strings to numbers
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2025
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Btree in a block on the surface of a silicon chip.

import java.util.*;

class StringToNumbers extends Test                                              // Collect numbers attached to identical strings
 {final TreeMap<String, TreeSet<Integer>> input = new TreeMap<>();              // Input string to integers
  final TreeSet<Order>                   output = new TreeSet<>();              // Output integers to string
  final Stack  <Order>              outputOrder = new Stack<>();                // Order as a stack
  Integer min, max;                                                             // minimum and maximum vakues encountered

//D1 Construction                                                               // Construct the ordering of strings and numbers

  class Order implements Comparable<Order>                                      // Order the strings by the lowest number they are associated with
   {final TreeSet<Integer> keys;                                                // Numbers asscoaited with this string
    final String string;                                                        // String asscoiated with numbers
    int   ordinal;                                                              // Position in the output order of this element

    Order(String String, TreeSet<Integer> Keys)                                 // Create an element of the order
     {string = String; keys = Keys;
      output.add(this);
     }                                                                          // Compare two ordered elements by the lowest integer associated with each

    String joinKeys()                                                           // Join the keys with commas
     {zz();
      final String j = ", ";
      final StringBuilder s = new StringBuilder();
      for(int k : keys) s.append(""+k+j);
      if (s.length() > j.length()) s.setLength(s.length() - j.length());        // Remove join following last element
      return s.toString();
     }

    public int compareTo(Order B)
     {zz();
      final Integer a = keys.first(), b = B.keys.first();
      return Integer.compare(a, b);
     }
   }

  void put(String string, int integer)                                          // An integer associated with a string
   {zz();
    TreeSet<Integer> s = input.get(string);
    min = minInt(min, integer); max = maxInt(max, integer);                     // Track the maximum and minimum encountered

    if (s == null) {z(); s = new TreeSet<>(); input.put(string, s);}
    s.add(integer);
   }

  void order()                                                                  // An integer associated with a string
   {zz(); for (String s : input.keySet()) new Order(s, input.get(s));
    for (Order o : output)
     {o.ordinal = outputOrder.size(); outputOrder.push(o);
     }
   }

//D1 Verilog                                                                    // Generate verilog

  void genVerilog(String file, String name)                                     // Verilog to map sets of numbers to the ordinal of the element in the order and then writethe verilog to a file
   {final StringBuilder s = new StringBuilder();
    final int N = outputOrder.size(), L = logTwo(N);
    s.append("reg ["+L+" : 0] "+name+"["+max+" : 0];\n");
    s.append("task initialize_"+name+";\n");
    s.append("  begin\n");
    for (int i = 0; i < N; i++)
     {final Order o = outputOrder.elementAt(i);
      for(Integer j : o.keys) s.append("        "+name+"["+j+"] <= "+i+";\n");
     }
    s.append("  end\n");
    s.append("endtask\n");

    writeFile(file, s.toString());
   }

//D0 Tests                                                                      // Testing

  private static void test_strings_to_numbers()
   {z();
    final StringToNumbers s = new StringToNumbers();
    s.put("a", 1);
    s.put("a", 2);
    s.put("b", 3);
    s.put("c", 4);
    s.put("c", 5);
    s.put("d", 6);
    s.order();

    if (true)
     {final StringBuilder t = new StringBuilder();
      for (Order o : s.outputOrder)
       {say(t, o.string, o.keys);
      }
    ok (t, """
a [1, 2]
b [3]
c [4, 5]
d [6]
""");
      ok(s.output.first().joinKeys(), "1, 2");
     }

    if (true)
     {final String t = tempFile();
      s.genVerilog(t, "caseMap");
      ok(joinLines(readFile(t))+"\n", """
reg [2 : 0] caseMap[6 : 0];
task initialize_caseMap;
  begin
        caseMap[1] <= 0;
        caseMap[2] <= 0;
        caseMap[3] <= 1;
        caseMap[4] <= 2;
        caseMap[5] <= 2;
        caseMap[6] <= 3;
  end
endtask
""");
      deleteFile(t);
     }
   }

  protected static void oldTests()                                              // Tests thought to be in good shape
   {test_strings_to_numbers();
   }

  protected static void newTests()                                              // Tests being worked on
   {//oldTests();
    test_strings_to_numbers();
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
