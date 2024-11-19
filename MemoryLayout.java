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

//D1 Control                                                                    // Testing, control and integrity

  void ok(String expected) {Test.ok(toString(), expected);}                     // Confirm memory layout is as expected
  void stop()              {Test.stop(toString());}                             // Stop after printing
  public String toString() {return "";}                                         // Print

//D1 Components                                                                 // A branch or leaf in the tree

  boolean equals(Layout.Field.Location a, Layout.Field.Location b)
    {a.sameSize(b);
     return memory.equals(a.at, b.at, a.width);
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
