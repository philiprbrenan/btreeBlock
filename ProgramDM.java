//------------------------------------------------------------------------------
// ProgramDM with distributed memory
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Btree in a block on the surface of a silicon chip.
// Create a case statement so that we can perform a linear memory move in Java and Verilog with identical effects on memory
import java.util.*;

class ProgramDM extends Test                                                    // A program that manipulates a memory layout via instructions
 {final Stack<Stack<I>> code = new Stack<>();                                   // Code of the program
  int            currentCode =  0;                                              // Point at which we are currently adding instructions to the program. Instructions can run in parallel so the current point is not always at the end as it is with non parallel instruction execution
  final Stack<Parallel> currentParallel = new Stack<>();                        // Start and end of current parallel range
  I       currentInstruction;                                                   // The currently executing instruction
  int               maxSteps = 100_000;                                         // Maximum number of steps permitted while running the program
  int                   step = 0;                                               // Execution step
  int                  steps = 0;                                               // Execution steps
  int                  start = 0;                                               // Step to start at
  boolean              debug = false;                                           // Debug code if true
  boolean            running = false;                                           // Executing if true
  Stack<Label>        labels = new Stack<>();                                   // Labels for some instructions
  final Stack<String>  Trace = new Stack<>();                                   // Trace execution steps
  static int         numbers = 0;                                               // Program numbers
  final  int          number = ++numbers;                                       // Program number
  final TreeSet<MemoryLayoutDM> memories = new TreeSet<>();                     // Memory layouts associated with this program
  final TreeSet<String>      uniqueNames = new TreeSet<>();                     // Confirm that memory layouts that claim they have unique names really do have unique names

  ProgramDM() {z();}                                                            // Create a program that instructions can be added to and then executed

  ProgramDM(ProgramDM Program)                                                  // Copy a program
   {z();
    for(Stack<I> i : Program.code)   code  .push(i);
    for(Label    l : Program.labels) labels.push(l);
   }

  ProgramDM programDM() {return this;}                                          // Address containing class

  void addMemoryLayout(MemoryLayoutDM ml, boolean uniqueName)                   // This program uses this memory layout amongst others
   {final String name = ml.name;
    if (uniqueName)                                                             // The intention is to use this name directly in verilog programs
     {if (uniqueNames.contains(name))                                           // Confirm it is unique
       {stop("Memory layout with name:", name, "has already been defined");
       }
      uniqueNames.add(name);                                                    // Record new unique name
     }
    if (memories.contains(ml))                                                  // Record memories assocaited w uth this program
     {stop("Already added to memories for this program:", name);
     }
    memories.add(ml);                                                           // Add this memory layout to the set of memory layouts associated with this progam
   }

  class Label                                                                   // Label definition
   {int instruction;                                                            // The instruction location to which this labels applies
    final String name;                                                          // An optional name for the label to assist in debugging
    Label()         {zz(); set(); labels.push(this); name = null;}              // A label assigned to an instruction location
    void set()      {zz(); instruction = code.size();}                          // Reassign the label to an instruction
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
      if (currentCode >= code.size())                                           // Normally every new instruction starts a new block of parallel instructions
       {final Stack<I> I = new Stack<>();
        I.push(this);
        code.push(I);
        currentCode = code.size();                                              // Ensure we match code size as we are extending the program
       }
      else                                                                      // Append to an existing block of instructionsin parallel
       {final Stack<I> I = code.elementAt(currentCode);
        I.push(this);
        ++currentCode;
       }
      i();                                                                      // Additional initialization
     }
    void   a() {stop("No instruction definition\n"+traceBack);}                 // Action performed by instruction
    String n() {return "instruction";}                                          // Instruction name
    String v() {stop("No instruction definition:\n"+traceBack); return null;}   // Corresponding verilog
    void   i() {}                                                               // initialization for each instruction
    String i4(int i) {return String.format("%4d", i);}                          // Format an index
    String i8(int i) {return String.format("%8d", i);}                          // Format an index

    String traceComment() {return " /* " + traceBack.replaceAll("\n", " ") + " */";} // Traceback as a comment

    public int compareTo(I that)
     {z(); return Integer.compare(instructionNumber, that.instructionNumber);
     }
   }

//D1 Parallel                                                                   // Execute instructions in parallel

  class Parallel                                                                // The start and end of the current parallel range
   {int start;                                                                  // Start of the parallel range
    int   end;                                                                  // End of the parallel range
    final String traceBack = traceComment();
    Parallel()                                                                  // Start at current code position
     {if (currentParallel.size() == 0)
       {start = end = code.size();
       }
      else
       {start = end = currentCode;
       }
      currentParallel.push(this);
     }
   }

  void  parallelStart()                                                         // Start a parallel block.  Only one parallel block is allowed at a time.  Parallel blocks are much easoer to deal with than automated optimization as we can make local incremental changes.
   {zz();
    new Parallel();
   }

  void  parallelSection()                                                       // Start a parallel block.  Only one parallel block is allowed at a time
   {zz();
    if (currentParallel.size() == 0) stop("No active parallel section");        // Check we are in a parallel section
    final Parallel p = currentParallel.lastElement();                           // Current parallel block
    p.end            = max(currentCode, p.end);                                 // Furthest extent of parallel block
    currentCode      = p.start;// + (currentParallel.size() > 1 ? 1 : 0);          // Add the instructions in this section from the start of the block
   }

  void  parallelEnd()                                                           // End a parallel block
   {zz();
    if (currentParallel.size() == 0) stop("No active parallel section");        // Check we are in apralle section
    final Parallel p = currentParallel.pop();                                   // Current parallel block
    currentCode      = max(currentCode, p.end);                                 // Furthest extent of parallel block
   }

  void  parallelDump()                                                          // Dump the openeing locations for the current set of paalle blocks
   {final int N = currentParallel.size();                                       // Open sections
    say(N, "open parallel sections started at:");
    for (int i = 1; i <= N; ++i)
     {final Parallel p = currentParallel.elementAt(i-1);
      say(i, p.traceBack);
     }
   }

//D1 Execute                                                                    // Execute the program

  void traceMemory()                                                            // Trace memory
   {zz();
    final StringBuilder S = new StringBuilder();
    for(MemoryLayoutDM m : memories)
     {final MemoryLayoutDM.BlockArray a = m.block;
      if (a.blocked())                                                          // Blocked array
       {for (int i = 0; i < a.size; i++)
         {S.append(m.name()+"["+i+"]="+m.memory().print(i*a.width, a.width)+" ");
         }
       }
      else                                                                      // Bit array
       {S.append(m.name()+"="+m.memory().print()+" ");
       }
     }
    if (S.length() > 0) S.setLength(S.length() - 1);                            // Remove final space if there is one

    Trace.push(String.format("%4d  %4d        %s",  steps, step, S));           // Changes to memory with room for verilog to write the op code
   }

  void traceInstruction(I i) {}                                                 // Trace instruction

  void setUniqueNames()                                                         // Assign a unique name to each memory layout to speed up tracing
   {for(MemoryLayoutDM m: memories)
     {m.setUniqueName();
     }
   }

  void run(String traceFile)                                                    // Run the program tracing to the named file
   {zz();
    if (currentParallel.size() > 0)                                             // Check all parallel sections have been closed
     {parallelDump(); stop("Parallel sections still open");
     }

    setUniqueNames();                                                           // Assign a unique name to each memory layout to speed up tracing

    Trace.clear();
    running = true;
    final int N = code.size();
    for (step = start, steps = 0;                                               // The execution loop
      step >= 0 && step < N && steps < maxSteps && running;
      step++, steps++)
     {traceMemory();
      for (I i : code.elementAt(step))                                          // Execute each instruction in the parallel block
       {currentInstruction = i;
        traceInstruction(i);
        try
         {i.a();
         }
        catch(Exception E)
         {say("Exception at step:", step, "after:", steps, "steps.");
          say(i.traceBack);
          say(E);
          step = N;
         }
       }
     }
    traceMemory();
    if (steps >= maxSteps) stop("Out of steps: ", steps);
    running = false;
    if (traceFile != null) writeFile(traceFile, joinLines(Trace));              // Write the trace
   }

  void run()                                                                    // Run the program tracing to a default file
   {z();
    //run("trace/"+currentTestName()+".txt");
    run(null);
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

  void nop()                                                                    // Do nothing, but do it well
   {new I()
     {void   a() {z();}
      String v() {return "/* no operation */";}
      String n() {return "No operation";}
     };
   }

//D1 Blocks                                                                     // Blocks of code used to implement if statements and for loops

  void Goto(Label label)                                                        // Goto a label
   {zz();
    new I()
     {void   a() {z(); step = label.instruction-1;}                             // The program execution for loop will increment
//    String v() {return "step = "+(label.instruction-1)+";";}                  // The program execution for loop will increment
      String v() {return "step <= "+(label.instruction)+"; /*GoTo*/";}          // The program execution for loop will increment
      String n() {return "Go to "+(label.instruction+1);}                       // One based with no auto increment from run
      void   i() {mightJump = true;}                                            // Will certainly jump
     };
   }
  void GoOn(Label label, MemoryLayoutDM.At condition)                           // Go to a specified label if a memory location is on, i.e. not zero
   {zz();
    new I()
     {void a()
       {z(); if (condition.setOff().getInt() > 0) step = label.instruction-1;
       }
//    String v() {return "if ("+condition.verilogLoad()+" > 0) step = "+(label.instruction-1)+";";} // The program execution for loop will increment
      String v() {return "if ("+condition.verilogLoad()+" > 0) step <= "+(label.instruction)+"; /*GoNext*/";} // The program execution for loop will increment
      String n() {return "GoOn "+condition.field.name+" to "+(label.instruction+1);}
      void   i() {mightJump = true;}                                            // Might jump
     };
   }
  void GoOff(Label label, MemoryLayoutDM.At condition)                          // Go to a specified label if a memory location is off, i.e. zero
   {zz();
    new I()
     {void a()
       {z(); if (condition.setOff().getInt() == 0) step = label.instruction-1;
       }
//    String v() {return "if ("+condition.verilogLoad()+" == 0) step = "+(label.instruction-1)+";";} // The program execution for loop will increment
      String v() {return "if ("+condition.verilogLoad()+" == 0) step <= "+(label.instruction)+"; /*GoNext*/";} // The program execution for loop will increment
      String n() {return "GoOff "+condition.field.name+" to "+(label.instruction+1);}
      void   i() {mightJump = true;}                                            // Might jump
     };
   }

  abstract class If                                                             // An if statement
   {final MemoryLayoutDM.At condition;                                          // Then if this field is non zero at run time, else Else
    final Label Else = new Label(), End = new Label();                          // Components of an if statement

    If (MemoryLayoutDM.At Condition)                                            // If a condition
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

    If (Variable Condition) {this(Condition.a);}                                // If a condition held in a variable

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
    final MemoryLayoutDM           M;
    final Layout              layout;
    final Layout.Variable      index;
    final Layout.Bit         compare;
    final Layout.Variable      limit;
    final Layout.Structure structure;
    final ProgramDM P = ProgramDM.this;

    Loop(int Limit, int Width)
     {z();
      if (Limit < 1) stop("Loop limit must be 1 or more, not:", Limit);
      layout    = Layout.layout();
      index     = layout.variable ("index",   Width);
      limit     = layout.variable ("limit",   Width);
      compare   = layout.bit      ("compare");
      structure = layout.structure("structure", index, limit, compare);
      M = new MemoryLayoutDM(layout.compile(), "M");
      M.program(ProgramDM.this);
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
    final MemoryLayoutDM           M;
    final Layout              layout;
    final Layout.Variable      index;
    final Layout.Bit         compare;
    final Layout.Structure structure;
    final ProgramDM P = ProgramDM.this;

    Pool(int Limit, int Width)
     {z();
      if (Limit < 1) stop("Pool start index must be 1 or more, not:", Limit);
      layout    = Layout.layout();
      index     = layout.variable ("index",   Width);
      compare   = layout.bit      ("compare");
      structure = layout.structure("structure", index, compare);
      M = new MemoryLayoutDM(layout.compile(), "M");
      M.program(ProgramDM.this);
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

  void debugVerilog(String title, MemoryLayoutDM.At at)                         // Add a debug statement to both the program and the verilog version
   {final int N = code.size();
    new I()
     {void   a() {say(N, title, at, at.setOff().getInt());}
      String v() {return "$display(\""+N+" \", \""+title+" \", \""+at+" \", "+at.verilogLoad()+");";}
     };
   }

  String printVerilog()                                                         // Print verilog code in program
   {final StringBuilder s = new StringBuilder();
    for(int i = 0; i < code.size(); ++i)
     {final Stack<I> I = code.elementAt(i);
      final int N = I.size();
      if (N == 1)                                                               // Only one instruction in parallel  block
       {s.append(String.format("%4d  %s\n", i+1, I.firstElement().v()));
       }
      else if (N > 1)                                                           // Several instructions in parallel  block
       {s.append(String.format("%4d\n", i+1));
        for(I j: I) s.append(String.format("      %s\n", j.v()));
       }
     }
    return s.toString();
   }

//D1 Tests                                                                      // Tests

  static void test_inc()
   {z();
    Layout           l = Layout.layout();
    Layout.Variable  n = l.variable ("n", 8);
    ProgramDM        p = new ProgramDM();
    MemoryLayoutDM   m = new MemoryLayoutDM(l.compile(), "M");
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
    ProgramDM        p = new ProgramDM();
    MemoryLayoutDM   m = new MemoryLayoutDM(l.compile(), "M");
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

    ProgramDM q = new ProgramDM();
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
    ProgramDM        p = new ProgramDM();
    MemoryLayoutDM   m = new MemoryLayoutDM(l.compile(), "M");
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
    MemoryLayoutDM   m = new MemoryLayoutDM(l.compile(), "M");
    ProgramDM        p = m.P;
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
    MemoryLayoutDM     m = new MemoryLayoutDM(l.compile(), "M");

    ProgramDM        p = m.P;
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
    MemoryLayoutDM   m = new MemoryLayoutDM(l.compile(), "M");
    ProgramDM        p = m.P;
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
    ProgramDM p = new ProgramDM();
    sayThisOrStop("stopping");
    p.new I() {void a() {Test.stop("stopping");} String n() {return "stop";}};
    try {p.run();} catch(RuntimeException e) {};
   }

  static void test_loop()
   {z();
    ProgramDM p = new ProgramDM();
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
    ProgramDM p = new ProgramDM();
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
    MemoryLayoutDM   m = new MemoryLayoutDM(l.compile(), "M");
    ProgramDM        p = m.P;

    m.at(a).setInt(11);
    m.at(b).setInt(22);

    p.debugVerilog("AAAA", m.at(a));
    p.debugVerilog("BBBB", m.at(b));
    p.run();
   }

  static void test_parallel_start_end()
   {z();
    Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 4);
    Layout.Variable  b = l.variable ("b", 4);
    Layout.Variable  c = l.variable ("c", 4);
    Layout.Variable  d = l.variable ("d", 4);
    Layout.Variable  e = l.variable ("e", 4);
    Layout.Variable  f = l.variable ("f", 4);
    Layout.Structure s = l.structure("s", a, b, c, d, e, f);

    MemoryLayoutDM   m = new MemoryLayoutDM(l.compile(), "M");
    ProgramDM        p = m.P;

                          p.new I() {void a() {m.at(a).setInt( 1);} String n() {return "a =  1";}};
                          p.new I() {void a() {m.at(b).setInt( 2);} String n() {return "b =  2";}};

    p.parallelStart();    p.new I() {void a() {m.at(c).setInt( 3);} String n() {return "c =  3";}};
                          p.new I() {void a() {m.at(d).setInt( 4);} String n() {return "d =  4";}};
    p.parallelEnd();
                          p.new I() {void a() {m.at(e).setInt( 5);} String n() {return "e =  5";}};
                          p.new I() {void a() {m.at(f).setInt( 6);} String n() {return "f =  6";}};

    p.run();
    //stop(p);
    ok(p, """
   1  a =  1
   2  b =  2
   3  c =  3
   4  d =  4
   5  e =  5
   6  f =  6
""");
    //stop(m);
    ok(m, """
MemoryLayout: M
Memory      : M
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        24                                      s
   2 V        0         4                                  1     a
   3 V        4         4                                  2     b
   4 V        8         4                                  3     c
   5 V       12         4                                  4     d
   6 V       16         4                                  5     e
   7 V       20         4                                  6     f
""");
   }

  static void test_parallel_start_section_end()
   {z();
    Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 4);
    Layout.Variable  b = l.variable ("b", 4);
    Layout.Variable  c = l.variable ("c", 4);
    Layout.Variable  d = l.variable ("d", 4);
    Layout.Variable  e = l.variable ("e", 4);
    Layout.Variable  f = l.variable ("f", 4);
    Layout.Structure s = l.structure("s", a, b, c, d, e, f);

    MemoryLayoutDM   m = new MemoryLayoutDM(l.compile(), "M");
    ProgramDM        p = m.P;

                          p.new I() {void a() {m.at(a).setInt( 1);} String n() {return "a =  1";}};
                          p.new I() {void a() {m.at(b).setInt( 2);} String n() {return "b =  2";}};

    p.parallelStart();    p.new I() {void a() {m.at(c).setInt( 3);} String n() {return "c =  3";}};
    p.parallelSection();  p.new I() {void a() {m.at(d).setInt( 4);} String n() {return "d =  4";}};
    p.parallelEnd();
                          p.new I() {void a() {m.at(e).setInt( 5);} String n() {return "e =  5";}};
                          p.new I() {void a() {m.at(f).setInt( 6);} String n() {return "f =  6";}};

    p.run();
    //stop(p);
    ok(p, """
   1  a =  1
   2  b =  2
   3
      c =  3
      d =  4
   4  e =  5
   5  f =  6
""");
    //stop(m);
    ok(m, """
MemoryLayout: M
Memory      : M
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        24                                      s
   2 V        0         4                                  1     a
   3 V        4         4                                  2     b
   4 V        8         4                                  3     c
   5 V       12         4                                  4     d
   6 V       16         4                                  5     e
   7 V       20         4                                  6     f
""");
   }

  static void test_parallel_start_section_end_twice()
   {z();
    Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 4);
    Layout.Variable  b = l.variable ("b", 4);
    Layout.Variable  c = l.variable ("c", 4);
    Layout.Variable  d = l.variable ("d", 4);
    Layout.Variable  e = l.variable ("e", 4);
    Layout.Variable  f = l.variable ("f", 4);
    Layout.Variable  g = l.variable ("g", 4);
    Layout.Variable  h = l.variable ("h", 4);
    Layout.Variable  i = l.variable ("i", 4);
    Layout.Variable  j = l.variable ("j", 4);
    Layout.Variable  k = l.variable ("k", 4);
    Layout.Structure s = l.structure("s", a, b, c, d, e, f, g, h, i, j, k);

    MemoryLayoutDM   m = new MemoryLayoutDM(l.compile(), "M");
    ProgramDM        p = m.P;

                          p.new I() {void a() {m.at(a).setInt( 1);} String n() {return "a =  1";}};
                          p.new I() {void a() {m.at(b).setInt( 2);} String n() {return "b =  2";}};

    p.parallelStart();    p.new I() {void a() {m.at(c).setInt( 3);} String n() {return "c =  3";}};
    p.parallelSection();  p.new I() {void a() {m.at(d).setInt( 4);} String n() {return "d =  4";}};
    p.parallelEnd();
                          p.new I() {void a() {m.at(e).setInt( 5);} String n() {return "e =  5";}};
                          p.new I() {void a() {m.at(f).setInt( 6);} String n() {return "f =  6";}};

    p.parallelStart();    p.new I() {void a() {m.at(g).setInt( 7);} String n() {return "g =  7";}};
    p.parallelSection();  p.new I() {void a() {m.at(h).setInt( 8);} String n() {return "h =  8";}};
    p.parallelEnd();
                          p.new I() {void a() {m.at(i).setInt( 9);} String n() {return "i =  9";}};
                          p.new I() {void a() {m.at(j).setInt(10);} String n() {return "j = 10";}};

    p.run();
    //stop(p);
    ok(p, """
   1  a =  1
   2  b =  2
   3
      c =  3
      d =  4
   4  e =  5
   5  f =  6
   6
      g =  7
      h =  8
   7  i =  9
   8  j = 10
""");
    //stop(m);
    ok(m, """
MemoryLayout: M
Memory      : M
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0        44                                      s
   2 V        0         4                                  1     a
   3 V        4         4                                  2     b
   4 V        8         4                                  3     c
   5 V       12         4                                  4     d
   6 V       16         4                                  5     e
   7 V       20         4                                  6     f
   8 V       24         4                                  7     g
   9 V       28         4                                  8     h
  10 V       32         4                                  9     i
  11 V       36         4                                 10     j
  12 V       40         4                                  0     k
""");
   }

  static void test_parallel_three()
   {z();
    Layout           L = Layout.layout();
    Layout.Variable  a = L.variable ("a", 8);
    Layout.Variable  b = L.variable ("b", 8);
    Layout.Variable  c = L.variable ("c", 8);
    Layout.Variable  d = L.variable ("d", 8);
    Layout.Variable  e = L.variable ("e", 8);
    Layout.Variable  f = L.variable ("f", 8);
    Layout.Variable  g = L.variable ("g", 8);
    Layout.Variable  h = L.variable ("h", 8);
    Layout.Variable  i = L.variable ("i", 8);
    Layout.Variable  j = L.variable ("j", 8);
    Layout.Variable  k = L.variable ("k", 8);
    Layout.Variable  l = L.variable ("l", 8);
    Layout.Variable  m = L.variable ("m", 8);
    Layout.Variable  n = L.variable ("n", 8);
    Layout.Variable  o = L.variable ("o", 8);
    Layout.Variable  p = L.variable ("p", 8);
    Layout.Variable  q = L.variable ("q", 8);
    Layout.Variable  r = L.variable ("r", 8);
    Layout.Variable  s = L.variable ("s", 8);
    Layout.Variable  t = L.variable ("t", 8);
    Layout.Variable  u = L.variable ("u", 8);
    Layout.Variable  v = L.variable ("v", 8);
    Layout.Variable  w = L.variable ("w", 8);
    Layout.Variable  x = L.variable ("x", 8);
    Layout.Variable  y = L.variable ("y", 8);
    Layout.Variable  z = L.variable ("z", 8);
    Layout.Structure S = L.structure("S", a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z);

    MemoryLayoutDM   M = new MemoryLayoutDM(L.compile(), "M");
    ProgramDM        P = M.P;

                              P.new I() {void a() {M.at(a).setInt( 1);} String n() {return "a =  1";}};

    P.parallelStart();        P.new I() {void a() {M.at(b).setInt( 2);} String n() {return "b =  2";}};
    P.parallelSection();      P.new I() {void a() {M.at(c).setInt( 3);} String n() {return "c =  3";}};
      P.parallelStart();      P.new I() {void a() {M.at(d).setInt( 4);} String n() {return "d =  4";}};
        P.parallelStart();    P.new I() {void a() {M.at(e).setInt( 5);} String n() {return "e =  5";}};
        P.parallelSection();  P.new I() {void a() {M.at(f).setInt( 6);} String n() {return "f =  6";}};
        P.parallelSection();  P.new I() {void a() {M.at(g).setInt( 7);} String n() {return "g =  7";}};
        P.parallelEnd();
      P.parallelSection();    P.new I() {void a() {M.at(h).setInt( 8);} String n() {return "h =  8";}};
        P.parallelStart();    P.new I() {void a() {M.at(i).setInt( 9);} String n() {return "i =  9";}};
        P.parallelSection();  P.new I() {void a() {M.at(j).setInt(10);} String n() {return "j = 10";}};
        P.parallelSection();  P.new I() {void a() {M.at(k).setInt(11);} String n() {return "k = 11";}};
        P.parallelEnd();
      P.parallelEnd();
    P.parallelSection();      P.new I() {void a() {M.at(l).setInt(12);} String n() {return "l = 12";}};
      P.parallelStart();      P.new I() {void a() {M.at(m).setInt(13);} String n() {return "m = 13";}};
        P.parallelStart();    P.new I() {void a() {M.at(n).setInt(14);} String n() {return "n = 14";}};
        P.parallelSection();  P.new I() {void a() {M.at(o).setInt(15);} String n() {return "o = 15";}};
        P.parallelSection();  P.new I() {void a() {M.at(p).setInt(16);} String n() {return "p = 16";}};
        P.parallelEnd();
      P.parallelSection();    P.new I() {void a() {M.at(q).setInt(17);} String n() {return "q = 17";}};
        P.parallelStart();    P.new I() {void a() {M.at(r).setInt(18);} String n() {return "r = 18";}};
        P.parallelSection();  P.new I() {void a() {M.at(s).setInt(19);} String n() {return "s = 19";}};
        P.parallelSection();  P.new I() {void a() {M.at(t).setInt(20);} String n() {return "t = 20";}};
        P.parallelEnd();
      P.parallelEnd();
    P.parallelEnd();
                              P.new I() {void a() {M.at(u).setInt(21);} String n() {return "u = 21";}};

    P.run();
    //stop(P);
    ok(P, """
   1  a =  1
   2
      b =  2
      c =  3
      l = 12
   3
      d =  4
      h =  8
      m = 13
      q = 17
   4
      e =  5
      f =  6
      g =  7
      i =  9
      j = 10
      k = 11
      n = 14
      o = 15
      p = 16
      r = 18
      s = 19
      t = 20
   5  u = 21
""");
    //stop(M);
    ok(M, """
MemoryLayout: M
Memory      : M
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0       208                                      S
   2 V        0         8                                  1     a
   3 V        8         8                                  2     b
   4 V       16         8                                  3     c
   5 V       24         8                                  4     d
   6 V       32         8                                  5     e
   7 V       40         8                                  6     f
   8 V       48         8                                  7     g
   9 V       56         8                                  8     h
  10 V       64         8                                  9     i
  11 V       72         8                                 10     j
  12 V       80         8                                 11     k
  13 V       88         8                                 12     l
  14 V       96         8                                 13     m
  15 V      104         8                                 14     n
  16 V      112         8                                 15     o
  17 V      120         8                                 16     p
  18 V      128         8                                 17     q
  19 V      136         8                                 18     r
  20 V      144         8                                 19     s
  21 V      152         8                                 20     t
  22 V      160         8                                 21     u
  23 V      168         8                                  0     v
  24 V      176         8                                  0     w
  25 V      184         8                                  0     x
  26 V      192         8                                  0     y
  27 V      200         8                                  0     z
""");
   }

  static void test_parallel()
   {z();
    Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 4);
    Layout.Variable  b = l.variable ("b", 4);
    Layout.Variable  c = l.variable ("c", 4);
    Layout.Variable  d = l.variable ("d", 4);
    Layout.Variable  e = l.variable ("e", 4);
    Layout.Variable  f = l.variable ("f", 4);
    Layout.Structure s = l.structure("s", a, b, c, d, e, f);
    Layout.Array     A = l.array    ("A", s, 2);

    MemoryLayoutDM   m = new MemoryLayoutDM(l.compile(), "M");
    ProgramDM        p = m.P;

                          p.new I() {void a() {m.at(a, 0).setInt( 1);} String n() {return "a[0] =  1";}};
                          p.new I() {void a() {m.at(a, 1).setInt( 2);} String n() {return "a[1] =  2";}};

    p.parallelStart();    p.new I() {void a() {m.at(b, 0).setInt( 3);} String n() {return "b[0] =  3";}};
                          p.new I() {void a() {m.at(b, 1).setInt( 4);} String n() {return "b[1] =  4";}};
      p.parallelSection();
        p.parallelStart();
                          p.new I() {void a() {m.at(c, 0).setInt( 5);} String n() {return "c[0] =  5";}};
                          p.new I() {void a() {m.at(c, 1).setInt( 6);} String n() {return "c[1] =  6";}};
        p.parallelSection();
                          p.new I() {void a() {m.at(d, 0).setInt( 7);} String n() {return "d[0] =  7";}};
                          p.new I() {void a() {m.at(d, 1).setInt( 8);} String n() {return "d[1] =  8";}};
      p.parallelEnd();
      p.parallelSection();
                          p.new I() {void a() {m.at(e, 0).setInt( 9);} String n() {return "e[0] =  9";}};
                          p.new I() {void a() {m.at(e, 1).setInt(10);} String n() {return "e[1] = 10";}};
    p.parallelEnd();
                          p.new I() {void a() {m.at(f, 0).setInt(11);} String n() {return "f[0] = 11";}};
                          p.new I() {void a() {m.at(f, 1).setInt(12);} String n() {return "f[1] = 12";}};

    p.run();
    //stop(p);
    ok(p, """
   1  a[0] =  1
   2  a[1] =  2
   3
      b[0] =  3
      c[0] =  5
      d[0] =  7
      e[0] =  9
   4
      b[1] =  4
      c[1] =  6
      d[1] =  8
      e[1] = 10
   5  f[0] = 11
   6  f[1] = 12
""");
    //stop(m);
    ok(m, """
MemoryLayout: M
Memory      : M
Line T       At      Wide       Size    Indices        Value   Name
   1 A        0        48          2                           A
   2 S        0        24               0                        s
   3 V        0         4               0                  1       a
   4 V        4         4               0                  3       b
   5 V        8         4               0                  5       c
   6 V       12         4               0                  7       d
   7 V       16         4               0                  9       e
   8 V       20         4               0                 11       f
   9 S       24        24               1                        s
  10 V       24         4               1                  2       a
  11 V       28         4               1                  4       b
  12 V       32         4               1                  6       c
  13 V       36         4               1                  8       d
  14 V       40         4               1                 10       e
  15 V       44         4               1                 12       f
""");
   }

  static void test_nop()
   {z();
    ProgramDM  p = new ProgramDM();
    p.nop();
    p.run();
    ok(p.printVerilog(), """
   1  /* no operation */
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
    test_parallel_start_end();
    test_parallel_start_section_end();
    test_parallel_start_section_end_twice();
    test_parallel_three();
    test_parallel();
    test_nop();
    //test_debug();
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
