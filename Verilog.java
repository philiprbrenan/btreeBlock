//------------------------------------------------------------------------------
// Verilog constants and formatter
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Btree in a block on the surface of a silicon chip.

class Verilog extends Test                                                      // Verilog constants and formatter
 {static final String ext    = ".v";                                            // File extension recognized by Vivado
  static final String header = ".vh";                                           // Header file extension name recognized by Vivado

//D0 Tests                                                                      // Testing

  static void test_ext()
   {ok(Verilog.ext   .equals(".v"));
    ok(Verilog.header.equals(".vh"));
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_ext();
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
