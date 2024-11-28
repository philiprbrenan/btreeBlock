//------------------------------------------------------------------------------
// Program
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Simulate a silicon chip.

import java.util.*;

class Program extends Test                                                      // A progam that manipulates a memory layout via si instructions
 {final Stack<I> code = new Stack<>();                                          // Code of the program
  final int maxTime = 1000;                                                     // Code of the program
  //final MemoryLayout memoryLayout;                                            // Memory layout
  int   step = 0;                                                               // Execution step
  int   time = 0;                                                               // Execution time
  boolean go = true;                                                            // Execute instructions while true

  class I                                                                       // Instruction definition
   {int in;                                                                     // Instruction number
    I() {code.push(this);}
    void action() {};
   }

//D1 Constructor                                                                // Memory provided in bits

//D1 Execute                                                                    // Execute the program

  void execute()
   {go = true;
    final int N = code.size();
    for (step = 0, time = 0; step < N && time < maxTime && go; step++, time++)
     {final I c = code.elementAt(step);
      c.action();
     }
   }

//D1 Tests                                                                      // Tests

  static void test_fibonacci()                                                  // The fibonacci numbers
   {final int           time = 40;
    final Layout           l = Layout.layout();
    final Layout.Variable  a = l.variable ("a", 8);
    final Layout.Variable  b = l.variable ("b", 8);
    final Layout.Variable  c = l.variable ("c", 8);
    final Layout.Structure s = l.structure("s", a, b, c);

    final MemoryLayout     m = new MemoryLayout(l.compile());
    final Program          p = new Program();
    final Stack<Integer>   f = new Stack<>();

    p.new I() {void action() {m.at(a).setInt(0);}};
    p.new I() {void action() {m.at(b).setInt(1);}};
    p.new I() {void action() {m.at(c).setInt(m.at(a).getInt() + m.at(b).getInt());}};
    p.new I() {void action() {m.at(a).setInt(m.at(b).getInt());}};
    p.new I() {void action() {m.at(b).setInt(m.at(c).getInt());}};
    p.new I() {void action() {f.push(m.at(c).getInt());}};
    p.new I() {void action() {if (p.time > time) p.go = false; else p.step = 1;}};
    p.execute();
    ok(""+f, "[1, 2, 3, 5, 8, 13, 21, 34]");
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_fibonacci();
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
