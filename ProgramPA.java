//------------------------------------------------------------------------------
// Program with pseudo assembler in memory layouts
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Simulate a silicon chip.

import java.util.*;

class ProgramPA extends Test                                                    // A progam that manipulates a memory layout via si instructions
 {final Stack<I> code = new Stack<>();                                          // Code of the program
  final int   maxTime = 1000;                                                   // Maximum numner of steps permitted while running the program
  int            step = 0;                                                      // Execution step
  int            time = 0;                                                      // Execution time
  boolean     running = false;                                                  // Executing if true
  Stack<Label> labels = new Stack<>();                                          // Labels for some instructions

  class Label                                                                   // Label definition
   {int instruction;                                                            // The instruction to which this labels applies
    Label() {set(); labels.push(this);}                                         // A label assigned to an instruction
    void set() {instruction = code.size();}                                     // Reassign the label to an instruction
   }

  class I                                                                       // Instruction definition
   {int instructionNumber;                                                      // Instruction number
    final String definition = traceBack();                                      // Location of code that defined this instruction
    I()                                                                         // Define an instruction
     {if (running) stop("Cannot define instructions during program execution",
       definition);
      instructionNumber = code.size(); code.push(this);
     }
    void   a() {}                                                               // Action performed by instruction
    void   i() {}                                                               // Initialization for instruction
    String n() {return "instruction";}                                          // Instruction name
   }

//D1 Execute                                                                    // Execute the program

  void initialize()                                                             // Initialize each instruction
   {z(); for (I i : code) {z(); i.i();}
   }

  void run()                                                                    // Run the program
   {z(); initialize();
    running = true;
    final int N = code.size();
    for (step = 0, time = 0; step < N && time < maxTime; step++, time++)
     {z(); code.elementAt(step).a();
     }
    running = false;
   }

  void stop(String em)                                                          // Stop everything with an explanatory message
   {z();
    new I() {void a() {Test.stop(em);} String n() {return "stop";}};
   }

  void clear() {z(); code.clear(); running = false;}                            // Clear the program code

//D1 Blocks                                                                     // Blocks of code used to implement if statements and for loops

  void Goto(Label label)                                                        // Goto a label
   {z();
    new I()
     {void a() {z(); step = label.instruction-1;}                               // The program execution for loop will increment
      String n() {return "Go to "+(label.instruction+1);}
     };
   }
  void GoOn(Label label, MemoryLayoutPA.At condition)                           // Go to a specified label if a memory location is on, i.e. not zero
   {z();
    new I()
     {void a()
       {z(); if (condition.setOff().getInt() > 0) step = label.instruction-1;
       }
      String n() {return "GoOn "+condition.field.name+" to "+(label.instruction+1);}
     };
   }
  void GoOff(Label label, MemoryLayoutPA.At condition)                          // Go to a specified label if a memory location is off, i.e. zero
   {z();
    new I()
     {void a()
       {z(); if (condition.setOff().getInt() == 0) step = label.instruction-1;
       }
      String n() {return "GoOff "+condition.field.name+" to "+(label.instruction+1);}
     };
   }

  abstract class If                                                             // An if statement
   {final MemoryLayoutPA.At condition;                                          // Then if this field is non zero at run time, else Else
    final Label Else = new Label(), End = new Label();                          // Components of an if statement

    If (MemoryLayoutPA.At Condition)                                            // If a condition
     {condition = Condition;
      GoOff(Else, condition);                                                   // Branch on the current value of if condition
      Then();
      Goto(End);
      Else.set();
      Else();
      End.set();
     }
    void Then() {}
    void Else() {}
   }

  abstract class Block                                                          // A block that can be continued or exited
   {final Label start = new Label(), end = new Label();                         // Labels at start and end of block to facilitate continuing or exiting
    Block()
     {code();
      end.set();
     }
    abstract void code();
   }

//D1 Print                                                                      // Print a program

  public String toString()
   {final StringBuilder s = new StringBuilder();
    for(int i = 0; i < code.size(); ++i)
     {s.append(String.format("%4d %s\n", i+1, code.elementAt(i).n()));
     }
    return s.toString();
   }

//D1 Tests                                                                      // Tests

  static void test_inc()
   {Layout           l = Layout.layout();
    Layout.Variable  n = l.variable ("n", 8);
    l.compile();

    ProgramPA        p = new ProgramPA();
    MemoryLayoutPA   m = new MemoryLayoutPA();
    m.layout(l);
    m.memory(new Memory(l.size()));
    m.program(p);

    m.at(n).inc();
    m.at(n).inc();
    m.at(n).inc();
    p.run();

    //stop(m);
    ok(m, """
Line T       At      Wide       Size    Indices        Value   Name
   1 V        0         8                                  3   n
""");
   }

  static void test_fibonacci()                                                  // The fibonacci numbers
   {final int N = 8, time = 40;
    Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", N);
    Layout.Variable  b = l.variable ("b", N);
    Layout.Variable  c = l.variable ("c", N);
    Layout.Variable  n = l.variable ("n", N);
    Layout.Variable  u = l.variable ("u", N);
    Layout.Bit       f = l.bit      ("f");
    Layout.Structure s = l.structure("s", a, b, c, n, u, f);
    l.compile();

    ProgramPA        p = new ProgramPA();
    MemoryLayoutPA   m = new MemoryLayoutPA();
    m.layout(l);
    m.memory(new Memory(l.size()));
    m.program(p);

    Stack<Integer>   F = new Stack<>();

    p.new I() {void a() {m.at(n).setInt(0);} String n() {return "n = 0;";}};
    p.new I() {void a() {m.at(u).setInt(8);} String n() {return "u = 8;";}};
    p.new I() {void a() {m.at(a).setInt(0);} String n() {return "a = 0;";}};
    p.new I() {void a() {m.at(b).setInt(1);} String n() {return "b = 1;";}};
    final Label start = p.new Label();
    p.new I() {void a() {m.at(c).setInt(m.at(a).getInt() + m.at(b).getInt());} String n() {return "c = a + b;";}};
    p.new I() {void a() {m.at(a).setInt(m.at(b).getInt());                   } String n() {return "a = b"     ;}};
    p.new I() {void a() {m.at(b).setInt(m.at(c).getInt());                   } String n() {return "b = c;"    ;}};
    p.new I() {void a() {F.push(m.at(c).getInt());                           } String n() {return "F.push(c);";}};
    m.at(n).inc();
    m.at(n).lessThan(m.at(u), m.at(f));
    p.GoOn(start, m.at(f));

    ok(p, """
   1 n = 0;
   2 u = 8;
   3 a = 0;
   4 b = 1;
   5 c = a + b;
   6 a = b
   7 b = c;
   8 F.push(c);
   9 ++n
  10 f=n<u
  11 GoOn f to 5
""");


    p.run();
    ok(F, "[1, 2, 3, 5, 8, 13, 21, 34]");
    p.run();
    ok(F, "[1, 2, 3, 5, 8, 13, 21, 34, 1, 2, 3, 5, 8, 13, 21, 34]");

    ProgramPA        q = new ProgramPA();
    m.program(q);
    m.at(s).ones();
    q.run();
    //stop(m);
    ok(m, """
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        41                                      s
   2 V        0         8                                255     a
   3 V        8         8                                255     b
   4 V       16         8                                255     c
   5 V       24         8                                255     n
   6 V       32         8                                255     u
   7 B       40         1                                  1     f
""");
   }

  static void test_fizz_buzz()
   {final int N = 8;
    Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", N);
    Layout.Variable  b = l.variable ("b", N);
    Layout.Variable  c = l.variable ("c", N);
    Layout.Bit       g = l.bit      ("g");
    Layout.Variable  u = l.variable ("u", N);
    Layout.Structure s = l.structure("s", a, b, c, g, u);
    l.compile();

    ProgramPA          p = new ProgramPA();
    MemoryLayoutPA     m = new MemoryLayoutPA();
    m.layout(l);
    m.memory(new Memory(l.size()));
    m.program(p);
    Stack<String>      f = new Stack<>();

    p.new I() {void a() {m.at(a).setInt(0);} String n() {return "a =  0";}};
    p.new I() {void a() {m.at(u).setInt(15);}String n() {return "u = 15";}};
    p.new Block()
     {void code()
       {p.new Block()
         {void code()
           {p.new I() {void a() {m.at(g).setInt(0);} String n() {return "g = 0";}};
            p.new I() {void a() {final int i = m.at(a).getInt(); if (i % 6 == 0) {f.push(""+i+" fizzbuzz"); m.at(g).setInt(1);}}String n() {return "fizzbuzz";}};
            p.GoOn(end, m.at(g));
            p.new I() {void a() {final int i = m.at(a).getInt(); if (i % 2 == 0) f.push(""+i+" fizz");} String n() {return "fizz";}};
            p.new I() {void a() {final int i = m.at(a).getInt(); if (i % 3 == 0) f.push(""+i+" buzz");} String n() {return "buzz";}};
           }
         };
        m.at(a).inc();
        m.at(a).greaterThan(m.at(u), m.at(g));
        p.GoOn(end, m.at(g));
        p.Goto(start);
       }
     };
    ok(p, """
   1 a =  0
   2 u = 15
   3 g = 0
   4 fizzbuzz
   5 GoOn g to 8
   6 fizz
   7 buzz
   8 ++a
   9 g=a>u
  10 GoOn g to 12
  11 Go to 3
""");
    p.run();
    ok(f, "[0 fizzbuzz, 2 fizz, 3 buzz, 4 fizz, 6 fizzbuzz, 8 fizz, 9 buzz, 10 fizz, 12 fizzbuzz, 14 fizz, 15 buzz]");
   }

  static void test_if()
   {Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 4);
    Layout.Variable  b = l.variable ("b", 4);
    Layout.Structure s = l.structure("s", a, b);
    l.compile();

    MemoryLayoutPA     m = new MemoryLayoutPA();
    m.layout(l);
    m.memory(new Memory(l.size()));
    ProgramPA        p = new ProgramPA();
    Stack<Integer>   f = new Stack<>();

    m.at(a).setInt(1);
    m.at(b).setInt(0);

    p.new If (m.at(a))
     {void Then() {p.new I() {void a() {f.push(1);} String n() {return "f.push(1);";}};}
      void Else() {p.new I() {void a() {f.push(2);} String n() {return "f.push(2);";}};}
     };

    p.new If (m.at(b))
     {void Then() {p.new I() {void a() {f.push(3);} String n() {return "f.push(3);";}};}
      void Else() {p.new I() {void a() {f.push(4);} String n() {return "f.push(4);";}};}
     };
    p.run();
    ok(f, "[1, 4]");

    m.at(a).setInt(0);
    m.at(b).setInt(1);
    p.run();
    ok(f, "[1, 4, 2, 3]");
   }

  static void test_goOn()
   {Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 4);
    Layout.Variable  b = l.variable ("b", 4);
    Layout.Structure s = l.structure("s", a, b);
    l.compile();
    MemoryLayoutPA     m = new MemoryLayoutPA();
    m.layout(l);
    m.memory(new Memory(l.size()));

    ProgramPA        p = new ProgramPA();
    Stack<Integer>   f = new Stack<>();

    m.at(a).setInt(1);
    m.at(b).setInt(0);

    p.new Block()
     {void code()
       {p.new I() {void a() {f.push(1);} String n() {return "f.push(1)";}};
        p.GoOn(end, m.at(a));
        p.new I() {void a() {f.push(2);} String n() {return "f.push(2)";}};
       }
     };

    p.run();
    ok(f, "[1]");
    m.at(a).setInt(0);

    p.run();
    ok(f, "[1, 1, 2]");
   }

  static void test_goOff()
   {Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 4);
    Layout.Variable  b = l.variable ("b", 4);
    Layout.Structure s = l.structure("s", a, b);
    l.compile();
    MemoryLayoutPA     m = new MemoryLayoutPA();
    m.layout(l);
    m.memory(new Memory(l.size()));
    ProgramPA        p = new ProgramPA();
    Stack<Integer>   f = new Stack<>();

    m.at(a).setInt(1);
    m.at(b).setInt(0);

    p.new Block()
     {void code()
       {p.new I() {void a() {f.push(1);} String n() {return "f.push(1);";}};
        p.GoOff(end, m.at(a));
        p.new I() {void a() {f.push(2);} String n() {return "f.push(2);";}};
       }
     };

    p.run();
    ok(f, "[1, 2]");
    m.at(a).setInt(0);

    p.run();
    ok(f, "[1, 2, 1]");
   }

  static void test_stop()
   {ProgramPA p = new ProgramPA();
    sayThisOrStop("stopping");
    p.new I() {void a() {Test.stop("stopping");} String n() {return "stop";}};
    try {p.run();} catch(RuntimeException e) {};
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_inc();
    test_fibonacci();
    test_fizz_buzz();
    test_if();
    test_goOn();
    test_goOff();
    test_stop();
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