//------------------------------------------------------------------------------
// Program with pseudo assembler in memory layouts
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Btree in a block on the surface of a silicon chip.

import java.util.*;

class ProgramPA extends Test                                                    // A progam that manipulates a memory layout via si instructions
 {final Stack<Stack<I>> code = new Stack<>();                                   // Code of the program
  int            currentCode =  0;                                              // Point at which we are currently adding instsructions to the program. Instructions can run in parallel so the current point is not always at the end as it is with non parallel instruction execution
  int        currentParallel = -1;                                              // Point at which the current parallel block started
  I       currentInstruction;                                                   // The currently executing instruction
  final int          maxTime = 100_000;                                         // Maximum number of steps permitted while running the program
  int                   step = 0;                                               // Execution step
  int                   time = 0;                                               // Execution time
  boolean            running = false;                                           // Executing if true
  Stack<Label>        labels = new Stack<>();                                   // Labels for some instructions
  Memory         traceMemory;                                                   // Labels for some instructions
  final Stack<String>  Trace = new Stack<>();                                   // Trace execution steps
  static int         numbers = 0;                                               // Program numbers
  final  int          number = ++numbers;                                       // Program number

  ProgramPA() {z();}                                                            // Create a program that instructions can be added to and then executed

  ProgramPA(ProgramPA Program)                                                  // Copy a program
   {z();
    for(Stack<I> i : Program.code)   code  .push(i);
    for(Label    l : Program.labels) labels.push(l);
   }

  ProgramPA programPA() {return this;}                                          // Address containing class

  class Label                                                                   // Label definition
   {int instruction;                                                            // The instruction location to which this labels applies
    Label()    {zz(); set(); labels.push(this);}                                // A label assigned to an instruction location
    void set() {zz(); instruction = code.size();}                               // Reassign the label to an instruction
   }

  class I implements Comparable<I>                                              // Instruction definition
   {int instructionNumber;                                                      // Instruction number
    final String traceBack;                                                     // Location of code that defined this instruction
    boolean mightJump;                                                          // This instruction might change the flow of control

    I()                                                                         // Define an instruction
     {zz();
      traceBack = traceBack();                                                  // Location of code that defined this instruction
      if (running) stop("Cannot define instructions during program execution",
        traceBack);
      ++currentCode;
      if (currentCode >= code.size())                                           // Start a new  block of parallel instructions
       {final Stack<I> I = new Stack<>();
        I.push(this);
        code.push(I);
       }
      else                                                                      // Append to an existing block of instructions
       {final Stack<I> I = code.elementAt(currentCode);
        I.push(this);
       }
     }
    void   a() {}                                                               // Action performed by instruction
    String n() {return "instruction";}                                          // Instruction name
    String v() {return " /* NOT SET */";}                                       // Corresponding verilog
    void   i() {}                                                               // initialization for each instruction

    String traceComment() {return " /* " + traceBack + " */";}                  // Traceback as a comment

    public int compareTo(I that)
     {z(); return Integer.compare(instructionNumber, that.instructionNumber);
     }
   }

  void  parallelStart()                                                         // Start a parallel block.  Only one parallel block is allowed at a time.  Parallel blocks are much easoer to deal with than automated optimization as we can make local incremental changes.
   {zz();
    if (currentParallel >= 0) stop("Only one parallel block can be active at a time");
    currentParallel = code.size()-1;
   }

  void  parallelSection()                                                       // Start a parallel block.  Only one parallel block is allowed at a time
   {zz();
    if (currentParallel < 0) stop("No active parallel section");
    currentCode = currentParallel;                                              // Add the instructions in this section from the start of the block
   }

  void  parallelEnd()                                                           // End a parallel block
   {zz();
    currentParallel = -1;
    currentCode = code.size();                                                  // Resume appending instructions at the end
   }

//D1 Execute                                                                    // Execute the program

  void traceMemory()                                                            // Trace memory
   {zz();
    if (traceMemory != null)
     {final StringBuilder s = new StringBuilder();
      final boolean[]b = traceMemory.bits;
      for(int i = 0; i < b.length; i++) s.append(b[i] ? "1" : "0");             // Match iverilog
      s.reverse();
      Trace.push(String.format("%4d  %4d  %s", time, step, s));
     }
   }

  void run(String traceFile)                                                    // Run the program tracing to the named file
   {zz();
    Trace.clear();
    running = true;
    final int N = code.size();
    for (step = 0, time = 0; step < N && time < maxTime && running; step++, time++)
     {traceMemory();
      for (I i : code.elementAt(step)) {currentInstruction =i; i.a();}          // Execute each instruction in the parallel block
     }
    traceMemory();
    if (time >= maxTime) stop("Out of time: ", time);
    running = false;
    if (traceMemory != null) writeFile(traceFile, joinLines(Trace));            // Write the trace
   }

  void run()                                                                    // Run the program tracing to a default file
   {z(); run("trace/"+currentTestName()+".txt");
   }

  void halt(final Object...O)                                                   // Halt execution with an explanatory message and traceback of current instruction
   {z();
    final String m = "/* "+saySb(O).toString()+" */";
    new I()
     {void   a() {say(O); say(currentInstruction.traceBack); running = false;}
      String v() {return "stopped <= 1; " + m;}
      String n() {return "halt";}
     };
   }

  void clear() {z(); code.clear(); currentCode = 0; running = false;}           // Clear the program code

//D1 Blocks                                                                     // Blocks of code used to implement if statements and for loops

  void Goto(Label label)                                                        // Goto a label
   {zz();
    new I()
     {void   a() {z(); step = label.instruction-1;}                             // The program execution for loop will increment
      String v() {return "step = "+(label.instruction-1)+";";}                  // The program execution for loop will increment
      String n() {return "Go to "+(label.instruction+1);}                       // One based with no auto increment from run
      void   i() {mightJump = true;}                                            // Will certainly jump
     };
   }
  void GoOn(Label label, MemoryLayoutPA.At condition)                           // Go to a specified label if a memory location is on, i.e. not zero
   {zz();
    new I()
     {void a()
       {z(); if (condition.setOff().getInt() > 0) step = label.instruction-1;
       }
      String v() {return "if ("+condition.verilogLoad()+" > 0) step = "+(label.instruction-1)+";";} // The program execution for loop will increment
      String n() {return "GoOn "+condition.field.name+" to "+(label.instruction+1);}
      void   i() {mightJump = true;}                                            // Might jump
     };
   }
  void GoOff(Label label, MemoryLayoutPA.At condition)                          // Go to a specified label if a memory location is off, i.e. zero
   {zz();
    new I()
     {void a()
       {z(); if (condition.setOff().getInt() == 0) step = label.instruction-1;
       }
      String v() {return "if ("+condition.verilogLoad()+" == 0) step = "+(label.instruction-1)+";";} // The program execution for loop will increment
      String n() {return "GoOff "+condition.field.name+" to "+(label.instruction+1);}
      void   i() {mightJump = true;}                                            // Might jump
     };
   }

  abstract class If                                                             // An if statement
   {final MemoryLayoutPA.At condition;                                          // Then if this field is non zero at run time, else Else
    final Label Else = new Label(), End = new Label();                          // Components of an if statement

    If (MemoryLayoutPA.At Condition)                                            // If a condition
     {zz();
      condition = Condition;
      final int t = code.size();
      GoOff(Else, condition);                                                   // Branch on the current value of if condition
      Then();
      if (code.size() <= t+1)                                                   // No then block
       {code.pop();
        GoOn(End, condition);                                                   // Jump over else block to avoid executing it if the condition is true
        final int e = code.size();
        Else();
        if (code.size() <= e)                                                   // Pointless if statement
         {code.pop();
          stop("Then or Else block required for If statement");
         }
       }
      else                                                                      // There was a then block
       {final int e = code.size();
        Goto(End);
        Else.set();
        Else();
        if (code.size() <= e+1)
         {code.pop();                                                           // No else block so no need to jump over it
          Else.set();                                                           // Else is now end
         }
       }
      End.set();                                                                // End of if
     }
    void Then() {}
    void Else() {}
   }

  abstract class Block                                                          // A block that can be continued or exited
   {final Label start = new Label(), end = new Label();                         // Labels at start and end of block to facilitate continuing or exiting
    Block()
     {zz();
      code();
      end.set();
     }
    abstract void code();
   }

  abstract class Loop                                                           // A loop that is executed a specified number of times
   {final Label start = new Label(), next = new Label(), end = new Label();     // Labels to restart currrent iteration, start the next iteration, or exit the loop
    final MemoryLayoutPA           M;
    final Layout              layout;
    final Layout.Variable      index;
    final Layout.Bit         compare;
    final Layout.Variable      limit;
    final Layout.Structure structure;
    final ProgramPA P = ProgramPA.this;

    Loop(int Limit, int Width)
     {z();
      if (Limit < 1) stop("Loop limit must be 1 or more, not:", Limit);
      layout    = Layout.layout();
      index     = layout.variable ("index",   Width);
      limit     = layout.variable ("limit",   Width);
      compare   = layout.bit      ("compare");
      structure = layout.structure("structure", index, limit, compare);
      M = new MemoryLayoutPA(layout.compile(), "M");
      M.program(ProgramPA.this);
      M.at(limit).setInt(Limit);
      M.setIntInstruction(index, 1);
      start.set();
      code();
      next.set();
      M.at(index).inc();
      M.at(index).lessThanOrEqual(M.at(limit), M.at(compare));
      P.GoOn(start, M.at(compare));
      end.set();
     }
    abstract void code();
   }

  abstract class Pool                                                           // A loop that is executed a specified number of times in reverse
   {final Label start = new Label(), next = new Label(), end = new Label();     // Labels to restart currrent iteration, start the next iteration, or exit the loop
    final MemoryLayoutPA           M;
    final Layout              layout;
    final Layout.Variable      index;
    final Layout.Bit         compare;
    final Layout.Structure structure;
    final ProgramPA P = ProgramPA.this;

    Pool(int Limit, int Width)
     {z();
      if (Limit < 1) stop("Pool start index must be 1 or more, not:", Limit);
      layout    = Layout.layout();
      index     = layout.variable ("index",   Width);
      compare   = layout.bit      ("compare");
      structure = layout.structure("structure", index, compare);
      M = new MemoryLayoutPA(layout.compile(), "M");
      M.program(ProgramPA.this);
      M.setIntInstruction(index, Limit);
      start.set();
      code();
      next.set();
      M.at(index).dec();
      M.at(index).isZero(M.at(compare));
      P.GoOff(end, M.at(index));
      P.Goto(start);
      end.set();
     }
    abstract void code();
   }

//D1 Print                                                                      // Print a program

  public String toString()
   {final StringBuilder s = new StringBuilder();
    for(int i = 0; i < code.size(); ++i)
     {final Stack<I> I = code.elementAt(i);
      final int N = I.size();
      if (N == 1)                                                               // Only one instruction in parallel  block
       {s.append(String.format("%4d  %s\n", i+1, I.firstElement().n()));
       }
      else if (N > 1)                                                           // Severale instructions in parallel  block
       {s.append(String.format("%4d\n", i+1));
        for(I j: I) s.append(String.format("      %s\n", j.n()));
       }
     }
    return s.toString();
   }

//D1 Verilog                                                                    // Dump verilog equivalents of the instructions in this program


  void debugVerilog(String title, MemoryLayoutPA.At at)                         // Add a debug statement to both the program and the verilog version
   {final int N = code.size();
    new I()
     {void   a() {say(N, title, at, at.setOff().getInt());}
      String v() {return "$display(\""+N+" \", \""+title+" \", \""+at+" \", "+at.verilogLoad()+");";}
     };
   }

//D1 Tests                                                                      // Tests

  static void test_inc()
   {z();
    Layout           l = Layout.layout();
    Layout.Variable  n = l.variable ("n", 8);
    ProgramPA        p = new ProgramPA();
    MemoryLayoutPA   m = new MemoryLayoutPA(l.compile(), "M");
    m.program(p);

    m.at(n).inc();
    m.at(n).inc();
    m.at(n).inc();
    p.run(); p.clear();

    //stop(m);
    ok(m, """
MemoryLayout: M
Memory      : M
Line T       At      Wide       Size    Indices        Value   Name
   1 V        0         8                                  3   n
""");
   }

  static void test_fibonacci()                                                  // The fibonacci numbers
   {z();
    final int N = 8, time = 40;
    Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", N);
    Layout.Variable  b = l.variable ("b", N);
    Layout.Variable  c = l.variable ("c", N);
    Layout.Variable  n = l.variable ("n", N);
    Layout.Variable  u = l.variable ("u", N);
    Layout.Bit       f = l.bit      ("f");
    Layout.Structure s = l.structure("s", a, b, c, n, u, f);
    ProgramPA        p = new ProgramPA();
    MemoryLayoutPA   m = new MemoryLayoutPA(l.compile(), "M");
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
   1  n = 0;
   2  u = 8;
   3  a = 0;
   4  b = 1;
   5  c = a + b;
   6  a = b
   7  b = c;
   8  F.push(c);
   9  ++n
  10  f=n<u
  11  GoOn f to 5
""");

    p.run();
    ok(F, "[1, 2, 3, 5, 8, 13, 21, 34]");
    p.run();
    ok(F, "[1, 2, 3, 5, 8, 13, 21, 34, 1, 2, 3, 5, 8, 13, 21, 34]");

    ProgramPA q = new ProgramPA();
    m.program(q);
    m.at(s).ones();
    q.run();
    //stop(m);
    ok(m, """
MemoryLayout: M
Memory      : M
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

  static void test_fizz_buzz(int z)
   {z();
    final int N = 8;
    Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", N);
    Layout.Variable  b = l.variable ("b", N);
    Layout.Variable  c = l.variable ("c", N);
    Layout.Bit       g = l.bit      ("g");
    Layout.Variable  u = l.variable ("u", N);
    Layout.Structure s = l.structure("s", a, b, c, g, u);
    ProgramPA        p = new ProgramPA();
    MemoryLayoutPA   m = new MemoryLayoutPA(l.compile(), "M");
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
   1  a =  0
   2  u = 15
   3  g = 0
   4  fizzbuzz
   5  GoOn g to 8
   6  fizz
   7  buzz
   8  ++a
   9  g=a>u
  10  GoOn g to 12
  11  Go to 3
""");
    p.run();
    ok(f, "[0 fizzbuzz, 2 fizz, 3 buzz, 4 fizz, 6 fizzbuzz, 8 fizz, 9 buzz, 10 fizz, 12 fizzbuzz, 14 fizz, 15 buzz]");
   }

  static void test_if()
   {z();
    Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 4);
    Layout.Variable  b = l.variable ("b", 4);
    Layout.Structure s = l.structure("s", a, b);
    MemoryLayoutPA   m = new MemoryLayoutPA(l.compile(), "M");
    ProgramPA        p = m.P;
    Stack<Integer>   f = new Stack<>();

    m.at(a).setInt(1);
    m.at(b).setInt(0);

    sayThisOrStop("Then or Else block required for If statement");
    try {p.new If (m.at(a)){};} catch(Exception e) {}

    p.new If (m.at(a))
     {void Then() {p.new I() {void a() {f.push(0);} String n() {return "f.push(0);";}};}
     };

    p.new If (m.at(b))
     {void Then() {p.new I() {void a() {f.push(1);} String n() {return "f.push(1);";}};}
     };

    p.new If (m.at(a))
     {void Else() {p.new I() {void a() {f.push(2);} String n() {return "f.push(2);";}};}
     };

    p.new If (m.at(b))
     {void Else() {p.new I() {void a() {f.push(3);} String n() {return "f.push(3);";}};}
     };

    p.new If (m.at(a))
     {void Then() {p.new I() {void a() {f.push(4);} String n() {return "f.push(4);";}};}
      void Else() {p.new I() {void a() {f.push(5);} String n() {return "f.push(5);";}};}
     };

    p.new If (m.at(b))
     {void Then() {p.new I() {void a() {f.push(6);} String n() {return "f.push(6);";}};}
      void Else() {p.new I() {void a() {f.push(7);} String n() {return "f.push(7);";}};}
     };
    p.run();
    ok(f, "[0, 3, 4, 7]"); f.clear();

    m.at(a).setInt(0);
    m.at(b).setInt(1);
    p.run();
    ok(f, "[1, 2, 5, 6]");
   }

  static void test_goOn()
   {z();
    Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 4);
    Layout.Variable  b = l.variable ("b", 4);
    Layout.Structure s = l.structure("s", a, b);
    MemoryLayoutPA     m = new MemoryLayoutPA(l.compile(), "M");

    ProgramPA        p = m.P;
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
   {z();
    Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 4);
    Layout.Variable  b = l.variable ("b", 4);
    Layout.Structure s = l.structure("s", a, b);
    MemoryLayoutPA   m = new MemoryLayoutPA(l.compile(), "M");
    ProgramPA        p = m.P;
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
   {z();
    ProgramPA p = new ProgramPA();
    sayThisOrStop("stopping");
    p.new I() {void a() {Test.stop("stopping");} String n() {return "stop";}};
    try {p.run();} catch(RuntimeException e) {};
   }

  static void test_loop()
   {z();
    ProgramPA p = new ProgramPA();
    final Stack<Integer> f = new Stack<>();
    p.new Loop(8, 4)
     {void code()
       {P.new I() {void a() {f.push(M.at(index).getInt());}};
       }
     };
    p.run();
    //stop(f);
    ok(f, "[1, 2, 3, 4, 5, 6, 7, 8]");
   }

  static void test_pool()
   {z();
    ProgramPA p = new ProgramPA();
    final Stack<Integer> f = new Stack<>();
    p.new Pool(8, 4)
     {void code()
       {P.new I() {void a() {f.push(M.at(index).getInt());}};
       }
     };
    p.run();
    //stop(f);
    ok(f, "[8, 7, 6, 5, 4, 3, 2, 1]");
   }

  static void test_debug()
   {z();
    Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 8);
    Layout.Variable  b = l.variable ("b", 8);
    Layout.Structure s = l.structure("s", a, b);
    MemoryLayoutPA   m = new MemoryLayoutPA(l.compile(), "M");
    ProgramPA        p = m.P;

    m.at(a).setInt(11);
    m.at(b).setInt(22);

    p.debugVerilog("AAAA", m.at(a));
    p.debugVerilog("BBBB", m.at(b));
    p.run();
   }

  static void test_parallel()
   {z();
    Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 4);
    Layout.Variable  b = l.variable ("b", 4);
    Layout.Variable  c = l.variable ("c", 4);
    Layout.Variable  d = l.variable ("d", 4);
    Layout.Structure s = l.structure("s", a, b, c, d);
    Layout.Array     A = l.array    ("A", s, 2);

    MemoryLayoutPA   m = new MemoryLayoutPA(l.compile(), "M");
    ProgramPA        p = m.P;

    p.new I() {void a() {m.at(a, 0).setInt(1);} String n() {return "a[0] = 1";}};
    p.new I() {void a() {m.at(a, 1).setInt(2);} String n() {return "a[1] = 2";}};
    p.parallelStart();
    p.new I() {void a() {m.at(b, 0).setInt(3);} String n() {return "b[0] = 3";}};
    p.new I() {void a() {m.at(b, 1).setInt(4);} String n() {return "b[1] = 4";}};
    p.parallelSection();
    p.new I() {void a() {m.at(c, 0).setInt(5);} String n() {return "c[0] = 5";}};
    p.new I() {void a() {m.at(c, 1).setInt(6);} String n() {return "c[1] = 6";}};
    p.parallelEnd();
    p.new I() {void a() {m.at(d, 0).setInt(7);} String n() {return "d[0] = 7";}};
    p.new I() {void a() {m.at(d, 1).setInt(8);} String n() {return "d[1] = 8";}};

    p.run();
    //stop(p);
    ok(p, """
   1  a[0] = 1
   2  a[1] = 2
   3
      b[0] = 3
      c[0] = 5
   4
      b[1] = 4
      c[1] = 6
   5  d[0] = 7
   6  d[1] = 8
""");
    //stop(m);
    ok(m, """
MemoryLayout: M
Memory      : M
Line T       At      Wide       Size    Indices        Value   Name
   1 A        0        32          2                           A
   2 S        0        16               0                        s
   3 V        0         4               0                  1       a
   4 V        4         4               0                  3       b
   5 V        8         4               0                  5       c
   6 V       12         4               0                  7       d
   7 S       16        16               1                        s
   8 V       16         4               1                  2       a
   9 V       20         4               1                  4       b
  10 V       24         4               1                  6       c
  11 V       28         4               1                  8       d
""");
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_inc();
    test_fibonacci();
    test_fizz_buzz(1);
    test_if();
    test_goOn();
    test_goOff();
    test_stop();
    test_loop();
    test_pool();
    test_parallel();
    //test_debug();
   }

  static void newTests()                                                        // Tests being worked on
   {oldTests();
    test_parallel();
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
