//------------------------------------------------------------------------------
// Strings to numbers
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2025
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Btree in a block on the surface of a silicon chip.

import java.util.*;

class StringToNumbers extends Test                                              // Collect numbers attached to identical strings
 {final TreeMap<String, TreeSet<Integer>> i = new TreeMap<>();                  // Input string to integers
  final TreeSet<Order>                    o = new TreeSet<>();                  // Output integers to string

  class Order implements Comparable<Order>                                      // Order the strings by the lowest number they are associated with
   {final TreeSet<Integer> keys;
    final String string;
    Order(String String, TreeSet<Integer> Keys)                                 // Create an element of the order
     {string = String; keys = Keys;
      o.add(this);
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
    TreeSet<Integer> s = i.get(string);
    if (s == null) {z(); s = new TreeSet<>(); i.put(string, s);}
    s.add(integer);
   }

  void order()                                                                  // An integer associated with a string
   {zz(); for (String s : i.keySet()) new Order(s, i.get(s));
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

    final StringBuilder t = new StringBuilder();
    for (Order o : s.o)
     {say(t, o.string, o.keys);
    }
    ok (t, """
a [1, 2]
b [3]
c [4, 5]
d [6]
""");
    ok(s.o.first().joinKeys(), "1, 2");
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
