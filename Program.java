//------------------------------------------------------------------------------
// Program
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Simulate a silicon chip.

import java.util.*;

class Program extends Test                                                      // A progam that manipulates a memory layout via si instructions
 {final Stack<I>       code = new Stack<>();                                    // Code of the program
  final int         maxTime = 1000;                                             // Code of the program
  int                  step = 0;                                                // Execution step
  int                  time = 0;                                                // Execution time
  boolean       ifCondition = true;                                             // Execute the then part of the next if statement if true, else the else part if present
  Stack<Label>       labels = new Stack<>();                                    // Labels for some instructions

  class Label                                                                   // Label definition
   {int instruction;                                                            // The instruction to which this labels applies
    Label() {set(); labels.push(this);}                                         // A label assigned to an instruction
    void set() {instruction = code.size();}                                     // Reassign the label to an instruction
   }

  class I                                                                       // Instruction definition
   {int instructionNumber;                                                      // Instruction number
    I() {instructionNumber = code.size(); code.push(this);}                     // Can always be removed from this position if needed and placed some where else
    void a() {};                                                                // Action for action
   }

//D1 Execute                                                                    // Execute the program

  void execute()
   {final int N = code.size();
    for (step = 0, time = 0; step < N && time < maxTime; step++, time++)
     {final I c = code.elementAt(step);
      c.a();
     }
   }

//D1 Blocks                                                                     // Blocks of code used to implement if statements and for loops

  void Goto(Label label) {step = label.instruction-1;}                          // The program execution for loop will increment first

  abstract class If                                                             // An if statement
   {final Label Else = new Label(), End = new Label();                          // Components of an if statement
    If()
     {new I() {void a() {if (!ifCondition) Goto(Else);}};                       // Branch on the current value of if condition
      Then();
      Goto(End);
      Else.set();
      Else();
      End.set();
     }
    abstract void Then();
    abstract void Else();
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

    p.new I() {void a() {m.at(a).setInt(0);}};
    p.new I() {void a() {m.at(b).setInt(1);}};
    final Label start = p.new Label();
    p.new I() {void a() {m.at(c).setInt(m.at(a).getInt() + m.at(b).getInt());}};
    p.new I() {void a() {m.at(a).setInt(m.at(b).getInt());}};
    p.new I() {void a() {m.at(b).setInt(m.at(c).getInt());}};
    p.new I() {void a() {f.push(m.at(c).getInt());}};
    p.new I() {void a() {if (p.time < time) p.Goto(start);}};
    p.execute();
    ok(f, "[1, 2, 3, 5, 8, 13, 21, 34]");
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
