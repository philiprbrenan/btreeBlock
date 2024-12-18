//------------------------------------------------------------------------------
// Modular If statement which can be translated to assembler code
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout a btree in a block on the surface of a silicon chip.

import java.util.*;

abstract class If extends Test                                                  // Modular If statement which can be translated to assembler code
 {

//D1 Construction                                                               // Create a modular if statement

  If (boolean condition)
   {if (condition) Then(); else Else();
   }
  void Then() {}
  void Else() {}

//D0 Tests                                                                      // Testing

  int a = 0;                                                                    // A value that can be reached from inside a method

  static void test_if_then()                                                    // Tests thought to be in good shape
   {final If i = new If(true)
     {void Then() {a = 1;}
      void Else() {a = 2;}
     };
    ok(i.a, 1);
   }

  static void test_if_else()                                                    // Tests thought to be in good shape
   {If i = new If(false)
     {void Then() {a = 1;}
      void Else() {a = 2;}
     };
    ok(i.a, 2);
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_if_then();
    test_if_else();
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
