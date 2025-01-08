//------------------------------------------------------------------------------
// Program with pseudo assembler in memory layouts
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Btree in a block on the surface of a silicon chip.

import java.util.*;

class ProgramPA extends Test                                                    // A progam that manipulates a memory layout via si instructions
 {final Stack<I>  code = new Stack<>();                                         // Code of the program
  final int    maxTime = 100_000;                                               // Maximum number of steps permitted while running the program
  int             step = 0;                                                     // Execution step
  int             time = 0;                                                     // Execution time
  boolean      running = false;                                                 // Executing if true
  boolean        trace = false;                                                 // Trace execution if true
  Stack<Label>  labels = new Stack<>();                                         // Labels for some instructions

  ProgramPA() {}                                                                // Create a program that instructions can be added to and then executed

  ProgramPA(ProgramPA Program)                                                  // Copy a program
   {for(I     i : Program.code  ) code  .push(i);
    for(Label l : Program.labels) labels.push(l);
   }

  ProgramPA programPA() {return this;}                                          // Address containing class

  class Label                                                                   // Label definition
   {int instruction;                                                            // The instruction to which this labels applies
    Label() {set(); labels.push(this);}                                         // A label assigned to an instruction
    void set() {instruction = code.size();}                                     // Reassign the label to an instruction
   }

  class I implements Comparable<I>                                              // Instruction definition
   {int instructionNumber;                                                      // Instruction number
    final String traceBack = traceBack();                                       // Location of code that defined this instruction
    final TreeSet<String>outputs = new TreeSet<>();                             // The set of outputs written by this instruction
    final TreeSet<String> inputs = new TreeSet<>();                             // The set of inputs read by this instruction
    final TreeSet<I>      merged = new TreeSet<>();                             // The refenced instructions can eb executed at the same time as this one
    I merge ;                                                                   // These instructions can be excuted at the same time as this instruction as they affect differendt fields
    boolean mergeableInstruction;                                               // This instruction can be merged with earlier instructions as long as there are no collisions
    boolean mightJump;                                                          // This instruction might change the flow of control

    I()                                                                         // Define an instruction
     {if (running) stop("Cannot define instructions during program execution",
        traceBack);
      instructionNumber = code.size(); code.push(this);
     }
    void   a() {}                                                               // Action performed by instruction
    String n() {return "instruction";}                                          // Instruction name
    String v() {return "" + traceComment();}                                    // Corresponding verilog
    void   i() {}                                                               // initialization for each instruction
    void out(MemoryLayoutPA.At at) {outputs.add(at.verilogLoad());}             // Record an output of this instruction
    void  in(MemoryLayoutPA.At at) { inputs.add(at.verilogLoad());}             // Record an input of this instruction

    String traceComment() {return " /*"+traceBack.replaceAll("\\n", " ")+" */";}// Trace back comment

    boolean canAccept(I source)                                                 // Whether the specified instruction could be merged into this instruction
     {for (String o : source.outputs)                                           // Each output of the instruction being moved
       {if  (outputs.contains(o)) return false;                                 // Collision - two outputs write to the same field
       }
      for (String o : source.outputs)                                           // Each output of the instruction being moved
       {if  (inputs.contains(o)) return false;                                  // Collision - output of the source instruction would overwrite input of target instruction
       }
      for (String i : source.inputs)                                            // Each output of the instruction being moved
       {if  (outputs.contains(i)) return false;                                 // Collision - an input to the source instruction is an output of the target instructrion
       }
      return true;
     }

    void merge(I source)                                                        // Merge the specified  instruction into this one
     {source.merge = this;                                                      // Instruction can be merged with this one
      outputs.addAll(source.outputs);                                           // Include outputs of merged instruction in current instruction making it harder to move subsequent instructions back through this instruction
      inputs .addAll(source.inputs);                                            // Include outputs of merged instruction in current instruction making it harder to move subsequent instructions back through this instruction
      merged.add(source);                                                       // Merge source to target
     }

    public int compareTo(I that)
     {return Integer.compare(instructionNumber, that.instructionNumber);
     }
   }

//D1 Execute                                                                    // Execute the program

  void compile()                                                                // Compile the program
   {final int N = code.size();
    for (step = 1; step < N; step++)                                            // Initialize each instruction
     {code.elementAt(step).i();
     }
    final TreeSet<Integer>  ls = new TreeSet<Integer>();                        // Values of labels
    for (Label l : labels) ls.add(l.instruction);                               // Values of labels as sets

    for (step = 1; step < N; step++)                                            // Move each instruction back as far as it will go without colliding with a prior instruction
     {if (ls.contains(step)) continue;                                          // This instruction might be a target of a jump
      final I s = code.elementAt(step);                                         // Source instruction that we want to merge into  a target instruction
      if (!s.mergeableInstruction) continue;                                    // Cannot merge this instruction
      if (s.mightJump)    continue;                                             // Cannot merge a jump
      for (int i = step; i > 0; --i)                                            // Possible target instructions
       {final I t = code.elementAt(i-1);                                        // Can we move to this instruction?
        if (!t.mergeableInstruction || t.mightJump ||                           // Nergeable instructions are done by block to enable this feature to be rolled out incrementally
            ls.contains(t.instructionNumber) || !t.canAccept(s))                // Target cannot accept source or the target might jump or the target might be a target of a jump
         {if (i != step)                                                        // Cannot merge an instruction into itself
           {code.elementAt(i).merge(s);                                         // Merge into the preceding instruction if it exists because it must have been viable to permit progress to this instruction
           }
          break;
         }
        else if (i == 1) t.merge(s);                                            // Every preceding instruction can accept the current instruction so merge it with the first instructon
       }
     }
   }

  void run()                                                                    // Run the program
   {z();
    final Stack<String> Trace = new Stack<>();                                  // Trace execution steps
    running = true;
    final int N = code.size();
    for (step = 0, time = 0; step < N && time < maxTime && running; step++, time++)
     {if (trace) Trace.push(String.format("%4d  %4d", time, step));
      z(); code.elementAt(step).a();
     }
    if (time >= maxTime) stop("Out of time: ", time);
    running = false;
    if (trace) writeFile("trace/"+currentTestName()+".txt", joinLines(Trace));  // Write the trace
   }

  void halt(Object...O)                                                         // Halt execution with an explanatory message
   {z();
    final String m = "/* "+saySb(O).toString()+" */";
    new I()
     {void   a() {say(O); /*say(traceBack);*/ running = false;}
      String v() {return "stopped <= 1; " + m + traceComment();}
      String n() {return "halt";}
     };
   }

  void clear() {z(); code.clear(); running = false;}                            // Clear the program code

//D1 Blocks                                                                     // Blocks of code used to implement if statements and for loops

  void Goto(Label label)                                                        // Goto a label
   {z();
    new I()
     {void   a() {z(); step = label.instruction-1;}                             // The program execution for loop will increment
      String v() {return "step = "+(label.instruction-1)+";" + traceComment();} // The program execution for loop will increment
      String n() {return "Go to "+(label.instruction+1);}                       // One based with no auto increment from run
      void   i() {mightJump = true;}                                            // Will certainly jump
     };
   }
  void GoOn(Label label, MemoryLayoutPA.At condition)                           // Go to a specified label if a memory location is on, i.e. not zero
   {z();
    new I()
     {void a()
       {z(); if (condition.setOff().getInt() > 0) step = label.instruction-1;
       }
      String v() {return "if ("+condition.verilogLoad()+" > 0) step = "+(label.instruction-1)+";" + traceComment();} // The program execution for loop will increment
      String n() {return "GoOn "+condition.field.name+" to "+(label.instruction+1);}
      void   i() {mightJump = true;}                                            // Might jump
     };
   }
  void GoOff(Label label, MemoryLayoutPA.At condition)                          // Go to a specified label if a memory location is off, i.e. zero
   {z();
    new I()
     {void a()
       {z(); if (condition.setOff().getInt() == 0) step = label.instruction-1;
       }
      String v() {return "if ("+condition.verilogLoad()+" == 0) step = "+(label.instruction-1)+";" + traceComment();} // The program execution for loop will increment
      String n() {return "GoOff "+condition.field.name+" to "+(label.instruction+1);}
      void   i() {mightJump = true;}                                            // Might jump
     };
   }

  abstract class If                                                             // An if statement
   {final MemoryLayoutPA.At condition;                                          // Then if this field is non zero at run time, else Else
    final Label Else = new Label(), End = new Label();                          // Components of an if statement

    If (MemoryLayoutPA.At Condition)                                            // If a condition
     {condition = Condition;
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
     {code();
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
     {if (Limit < 1) stop("Loop limit must be 1 or more, not:", Limit);
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
     {if (Limit < 1) stop("Pool start index must be 1 or more, not:", Limit);
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
     {final I c = code.elementAt(i);
      final String m = c.merge != null ? String.format("%4d", c.merge.instructionNumber) : "";
      s.append(String.format("%4d  %4s  %s\n", i+1, m, code.elementAt(i).n()));
      if (c.outputs.size() > 0) s.append("    Outputs: "+joinStrings(c.outputs, " ")+"\n");
      if (c. inputs.size() > 0) s.append("    Inputs : "+joinStrings(c. inputs, " ")+"\n");
      if (c.merged.size() > 0)                                                  // List of merged instructions
       {s.append("    Merged:");
        for (I j : c.merged) s.append(" "+(j.instructionNumber+1));
        s.append("\n");
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
   {Layout           l = Layout.layout();
    Layout.Variable  n = l.variable ("n", 8);
    ProgramPA        p = new ProgramPA();
    MemoryLayoutPA   m = new MemoryLayoutPA(l.compile(), "M");
    m.program(p);

    m.at(n).inc();
    m.at(n).inc();
    m.at(n).inc();
    p.run();

    //stop(m);
    ok(m, """
MemoryLayout: M
Memory      : M
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
   1        n = 0;
   2        u = 8;
   3        a = 0;
   4        b = 1;
   5        c = a + b;
   6        a = b
   7        b = c;
   8        F.push(c);
   9        ++n
  10        f=n<u
  11        GoOn f to 5
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

  static void test_fizz_buzz()
   {final int N = 8;
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
   1        a =  0
   2        u = 15
   3        g = 0
   4        fizzbuzz
   5        GoOn g to 8
   6        fizz
   7        buzz
   8        ++a
   9        g=a>u
  10        GoOn g to 12
  11        Go to 3
""");
    p.run();
    ok(f, "[0 fizzbuzz, 2 fizz, 3 buzz, 4 fizz, 6 fizzbuzz, 8 fizz, 9 buzz, 10 fizz, 12 fizzbuzz, 14 fizz, 15 buzz]");
   }

  static void test_if()
   {Layout           l = Layout.layout();
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
   {Layout           l = Layout.layout();
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
   {Layout           l = Layout.layout();
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
   {ProgramPA p = new ProgramPA();
    sayThisOrStop("stopping");
    p.new I() {void a() {Test.stop("stopping");} String n() {return "stop";}};
    try {p.run();} catch(RuntimeException e) {};
   }

  static void test_loop()
   {ProgramPA p = new ProgramPA();
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
   {ProgramPA p = new ProgramPA();
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
   {Layout           l = Layout.layout();
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

  static void test_merge()
   {Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 8);
    Layout.Variable  b = l.variable ("b", 8);
    Layout.Variable  c = l.variable ("c", 8);
    Layout.Variable  d = l.variable ("d", 8);
    Layout.Variable  e = l.variable ("e", 8);
    Layout.Structure s = l.structure("s", a, b, c, d, e);
    MemoryLayoutPA   m = new MemoryLayoutPA(l.compile(), "M");
    ProgramPA        p = m.P;

    m.at(b).move(m.at(a));
    m.at(c).move(m.at(a));
    m.at(d).move(m.at(b));
    m.at(e).move(m.at(b));
    p.compile();
    ok(p, """
   1        b=a
   2        c=a
    Outputs: M[  16/*c   */ +: 8] M[  24/*d   */ +: 8] M[  32/*e   */ +: 8]
    Inputs : M[   0/*a   */ +: 8] M[   8/*b   */ +: 8]
    Merged: 3 4
   3     1  d=b
    Outputs: M[  24/*d   */ +: 8]
    Inputs : M[   8/*b   */ +: 8]
   4     1  e=b
    Outputs: M[  32/*e   */ +: 8]
    Inputs : M[   8/*b   */ +: 8]
""");
   }

  static void test_merge_if()
   {Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 8);
    Layout.Variable  b = l.variable ("b", 8);
    Layout.Variable  c = l.variable ("c", 8);
    Layout.Variable  d = l.variable ("d", 8);
    Layout.Variable  e = l.variable ("e", 8);
    Layout.Structure s = l.structure("s", a, b, c, d, e);
    MemoryLayoutPA   m = new MemoryLayoutPA(l.compile(), "M");
    ProgramPA        p = m.P;

    m.at(b).move(m.at(a));
    m.at(c).move(m.at(a));
    m.at(d).move(m.at(b));
    m.at(e).move(m.at(b));
    p.new If (m.at(a))
     {void Then()
       {m.at(b).move(m.at(a));
        m.at(c).move(m.at(a));
        m.at(d).move(m.at(b));
        m.at(e).move(m.at(b));
       }
     };
    m.at(b).move(m.at(a));
    m.at(c).move(m.at(a));
    m.at(d).move(m.at(b));
    m.at(e).move(m.at(b));
    p.compile();
    //stop(p);
    ok(p, """
   1        b=a
   2        c=a
    Outputs: M[  16/*c   */ +: 8] M[  24/*d   */ +: 8] M[  32/*e   */ +: 8]
    Inputs : M[   0/*a   */ +: 8] M[   8/*b   */ +: 8]
    Merged: 3 4
   3     1  d=b
    Outputs: M[  24/*d   */ +: 8]
    Inputs : M[   8/*b   */ +: 8]
   4     1  e=b
    Outputs: M[  32/*e   */ +: 8]
    Inputs : M[   8/*b   */ +: 8]
   5        GoOff a to 10
   6        b=a
    Outputs: M[   8/*b   */ +: 8] M[  16/*c   */ +: 8]
    Inputs : M[   0/*a   */ +: 8]
    Merged: 7
   7     5  c=a
    Outputs: M[  16/*c   */ +: 8] M[  24/*d   */ +: 8] M[  32/*e   */ +: 8]
    Inputs : M[   0/*a   */ +: 8] M[   8/*b   */ +: 8]
    Merged: 8 9
   8     6  d=b
    Outputs: M[  24/*d   */ +: 8]
    Inputs : M[   8/*b   */ +: 8]
   9     6  e=b
    Outputs: M[  32/*e   */ +: 8]
    Inputs : M[   8/*b   */ +: 8]
  10        b=a
    Outputs: M[   8/*b   */ +: 8]
    Inputs : M[   0/*a   */ +: 8]
  11        c=a
    Outputs: M[  16/*c   */ +: 8] M[  24/*d   */ +: 8] M[  32/*e   */ +: 8]
    Inputs : M[   0/*a   */ +: 8] M[   8/*b   */ +: 8]
    Merged: 12 13
  12    10  d=b
    Outputs: M[  24/*d   */ +: 8]
    Inputs : M[   8/*b   */ +: 8]
  13    10  e=b
    Outputs: M[  32/*e   */ +: 8]
    Inputs : M[   8/*b   */ +: 8]
""");
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_inc();
    test_fibonacci();
    test_fizz_buzz();
    test_if();
    test_goOn();
    test_goOff();
    test_stop();
    test_loop();
    test_pool();
    test_merge();
    test_merge_if();
    //test_debug();
   }

  static void newTests()                                                        // Tests being worked on
   {oldTests();
    test_merge_if();
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

/*

Fields written that always have the same value
Fields that are never read
Field propogation across move
Instruction eager evaluation - in progress

*/
