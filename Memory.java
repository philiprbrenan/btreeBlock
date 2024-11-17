//------------------------------------------------------------------------------
// Memory
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Simulate a silicon chip.

import java.util.*;

class Memory extends Test                                                       // Memory provided in bits
 {final boolean[]bits;                                                          // The name of the bit machine

//D1 Construct                                                                  // Memory provided in bits

  Memory(int Size)                                                              // Create memory
   {bits = new boolean[Size];
   }

  static Memory memory(int Size) {return new Memory(Size);}                      // Create memory

  public String toString()
   {final StringBuilder s = new StringBuilder();
    final StringBuilder t = new StringBuilder("0123-5678+".repeat(1+bits.length/10));
    t.setLength(bits.length);
    t.reverse();
    for(int i = 0; i < bits.length; ++i) s.append(bits[i] ? "1" : "0");
    return t+"\n"+s.reverse().toString()+"\n";
   }

//D1 Operations                                                                 // Operations on memory

  void zero(int start, int width)                                               // Zero some memory
   {for(int i = 0; i < width; ++i) bits[start+i] = false;
   }

  void ones(int start, int width)                                               // Ones some memory
   {for(int i = 0; i < width; ++i) bits[start+i] = true;
   }

  boolean isAllZero(int start, int width)                                       // Check that the specified memory is all zeros
   {for(int i = 0; i < width; ++i) if ( bits[start+i]) return false;
    return true;
   }

  boolean isAllOnes(int start, int width)                                       // Check that  the specified memory is all ones
   {for(int i = 0; i < width; ++i) if (!bits[start+i]) return false;
    return true;
   }

  void set(int start, int width, int value)                                     // Set some memory from an integer
   {final int w = min(Integer.SIZE-1, width);
    for(int i = 0; i < w; ++i)
     {final int n =  value & (1 << i);
      bits[start+i] = n != 0;
     }
   }

  int getInt(int start, int width)                                              // Get an int from memory
   {final int w = min(Integer.SIZE-1, width);
    int n = 0;
    for(int i = 0; i < w; ++i) if (bits[start+i]) n |= (1 << i);
    return n;
   }

  void copy(int target, int source, int width)                                  // Copy the specified number of bits from source to start
   {for(int i = 0; i < width; ++i) bits[target+i] = bits[source+i];
   }

  void invert(int start, int width)                                             // Invert the specified bits
   {for(int i = 0; i < width; ++i) bits[start+i] = !bits[start+i];
   }

//D0                                                                            // Tests

  static void test_set_get()
   {Memory m = memory(8);
    m.set(2, 2, 3);
    m.set(5, 1, 1);

    ok(m.getInt(5, 2), 1);
    ok(m.getInt(2, 2), 3);
    ok(m.getInt(2, 4), 11);

    ok(m.isAllOnes(2, 2)); ok(!m.isAllOnes(2, 4));
    ok(m.isAllZero(0, 2)); ok(!m.isAllZero(0, 4));
   }

  static void test_zero_ones()
   {Memory m = memory(8);
    m.ones(2, 4);
    m.zero(4, 1);

    ok(m.getInt(2, 4), 11);
   }

  static void test_copy()
   {Memory m = memory(8);

    m.ones(1, 2);
    ok(""+m, """
765-3210
00000110
""");

    m.copy(5, 1, 2);
    ok(""+m, """
765-3210
01100110
""");
   }

  static void test_invert()
   {Memory m = memory(8);

    m.ones(1, 2);
    ok(""+m, """
765-3210
00000110
""");

    m.invert(2, 2);
    ok(""+m, """
765-3210
00001010
""");
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_set_get();
    test_zero_ones();
    test_copy();
    test_invert();
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
