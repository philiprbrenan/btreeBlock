//------------------------------------------------------------------------------
// Program
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Simulate a silicon chip.

import java.util.*;

class Program extends Test                                                      // A progam that manipulates a memory layout via si instructions
 {final Stack<I> code = new Stack<>();                                          // Code of the program
  final int   maxTime = 1000;                                                   // Code of the program
  int            step = 0;                                                      // Execution step
  int            time = 0;                                                      // Execution time
  boolean ifCondition = true;                                                   // Execute the then part of the next if statement if true, else the else part if present
  Stack<Label> labels = new Stack<>();                                          // Labels for some instructions

  class Label                                                                   // Label definition
   {int instruction;                                                            // The instruction to which this labels applies
    Label() {set(); labels.push(this);}                                         // A label assigned to an instruction
    void set() {instruction = code.size();}                                     // Reassign the label to an instruction
   }

  class I                                                                       // Instruction definition
   {int instructionNumber;                                                      // Instruction number
    I() {instructionNumber = code.size(); code.push(this);}                     // Can always be removed from this position if needed and placed some where else
    void   a() {}                                                               // Action performed by instruction
    void   i() {}                                                               // Initialization for instruction
    String n() {return "instruction";}                                          // Instruction name
   }

//D1 Execute                                                                    // Execute the program

  void initialize()                                                             // Initialize each instruction
   {z(); for (I i : code) {z(); i.i();}
   }

  void execute()
   {z(); initialize();
    final int N = code.size();
    for (step = 0, time = 0; step < N && time < maxTime; step++, time++)
     {z(); code.elementAt(step).a();
     }
   }

//D1 Blocks                                                                     // Blocks of code used to implement if statements and for loops

  void Goto(Label label) {step = label.instruction-1;}                          // The program execution for loop will increment first

  abstract class If                                                             // An if statement
   {final MemoryLayout.At condition;                                            // Then if this field is non zero at run time, else Else
    final Label Else = new Label(), End = new Label();                          // Components of an if statement

    If (MemoryLayout.At Condition)                                              // If a condition
     {condition = Condition;
      new I() {void a() {if (condition.setOff().getInt() == 0) Goto(Else);}};   // Branch on the current value of if condition
      Then();
      new I() {void a() {Goto(End);}};
      Else.set();
      Else();
      End.set();
     }
    abstract void Then();
    abstract void Else();
   }

  abstract class Block                                                          // A block that can be continued or exited
   {final Label start = new Label(), end = new Label();                         // Labels at start and end of block to facilitate continuing or exiting
    Block()
     {code();
      end.set();
     }
    abstract void code();
   }

//D1 Tests                                                                      // Tests

  static void test_fibonacci()                                                  // The fibonacci numbers
   {int           time = 40;
    Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 8);
    Layout.Variable  b = l.variable ("b", 8);
    Layout.Variable  c = l.variable ("c", 8);
    Layout.Structure s = l.structure("s", a, b, c);

    MemoryLayout     m = new MemoryLayout(l.compile());
    Program          p = new Program();
    Stack<Integer>   f = new Stack<>();

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
    p.execute();
    ok(f, "[1, 2, 3, 5, 8, 13, 21, 34, 1, 2, 3, 5, 8, 13, 21, 34]");

    Program          q = new Program();
    q.new I() {void a() {m.at(s).ones();}};
    q.execute();
    //stop(m);
    ok(m, """
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        24                                      s
   2 V        0         8                                255     a
   3 V        8         8                                255     b
   4 V       16         8                                255     c
""");
   }

  static void test_fizz_buzz()
   {int           time = 40;
    Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 8);
    Layout.Variable  b = l.variable ("b", 8);
    Layout.Variable  c = l.variable ("c", 8);
    Layout.Structure s = l.structure("s", a, b, c);

    MemoryLayout     m = new MemoryLayout(l.compile());
    Program          p = new Program();
    Stack<String>    f = new Stack<>();

    p.new I() {void a() {m.at(a).setInt(0);}};
    p.new Block()
     {void code()
       {p.new Block()
         {void code()
           {p.new I() {void a() {final int i = m.at(a).getInt(); if (i % 6 == 0) {f.push(""+i+" fizzbuzz"); p.Goto(end);}}};
            p.new I() {void a() {final int i = m.at(a).getInt(); if (i % 2 == 0) {f.push(""+i+" fizz");     p.Goto(end);}}};
            p.new I() {void a() {final int i = m.at(a).getInt(); if (i % 3 == 0) {f.push(""+i+" buzz");     p.Goto(end);}}};
           }
         };
        p.new I() {void a() {if (m.at(a).inc() > 15) p.Goto(end);}};
        p.new I() {void a() {p.Goto(start);}};
       }
     };
    p.execute();
    ok(f, "[0 fizzbuzz, 2 fizz, 3 buzz, 4 fizz, 6 fizzbuzz, 8 fizz, 9 buzz, 10 fizz, 12 fizzbuzz, 14 fizz, 15 buzz]");
   }

  static void test_if()
   {int           time = 40;
    Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 4);
    Layout.Variable  b = l.variable ("b", 4);
    Layout.Structure s = l.structure("s", a, b);

    MemoryLayout     m = new MemoryLayout(l.compile());
    Program          p = new Program();
    Stack<Integer>   f = new Stack<>();

    m.at(a).setInt(1);

    p.new If (m.at(a))
     {void Then() {p.new I() {void a() {f.push(1);}};}
      void Else() {p.new I() {void a() {f.push(2);}};}
     };

    p.new If (m.at(b))
     {void Then() {p.new I() {void a() {f.push(3);}};}
      void Else() {p.new I() {void a() {f.push(4);}};}
     };
    p.execute();
    ok(f, "[1, 4]");
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_fibonacci();
    test_fizz_buzz();
    test_if();
   }

  static void newTests()                                                        // Tests being worked on
   {oldTests();
    test_if();
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
