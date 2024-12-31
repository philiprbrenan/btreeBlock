//------------------------------------------------------------------------------
// The BAN Mark 2 computer ISA
// Copyright: Brianna Ashley Nevarez, 2022-01-21
//------------------------------------------------------------------------------
/*
Executes high level ISA for three test programs
Prints low level ISA code for each program
*/
import java.util.*;

public class Machine {
  static boolean Trace = false, Dump = false, LowLevelCode = false;             // Trace program execution, dump memory, print low level instructions
  static int     Count = 0;                                                     // Tests passed
  String         Title = null;                                                  // Title of the current program

  int [] R = new int[16];                                                       // Register file
  int [] D = new int[256];                                                      // Data memory

  class I                                                                       // Instruction definition
   {void i() {};                                                                // Code for the instruction
    final String  name;                                                         // Name of instruction
    final Integer label;                                                        // Optional numeric label for the instruction
    final boolean stop;                                                         // Whether we should stop
    Integer pc = null;                                                          // The label we should go to next - else we go to the next instruction
    I(String Name)            {name = Name; label = null;  stop = false;}       // No label
    I(String Name, int Label) {name = Name; label = Label; stop = false;}       // Specified label
    I(String Name, boolean t) {name = Name; label = null;  stop = t;}           // Stop cannot have a label on it - put a label instruction just before it!
    Integer [] l() {return new Integer[] {null, null, null};}                   // Print low level ISA code
   };

  Stack<I> C = new Stack<>();                                                   // Code
  final int a = 0, b = 1, c =  2, d =  3, e =  4, f =  5, g =  6, h =  7,       // Register  names
            i = 8, j = 9, k = 10, l = 11, m = 12, n = 13, o = 14, p = 15;

  public void initialize(String title)                                          // Initialize machine by clearing registers and memory
   {for(int i = 0; i < D.length; i++) D[i] = 0;                                 // Clear data memory
    for(int i = 0; i < R.length; i++) R[i] = 0;                                 // Clear registers
    C.clear();                                                                  // Clear code == program memory
    this.Title = title;                                                         // Set title of current program
   }

  public void run()                                                             // Run a program until it stops
   {Integer pc = 0;                                                             // Program counter - program starts at first instruction
    if (Trace) say("Start run of program: ", Title);
    for(;;)                                                                     // Execute instructions until stop requested
     {final I i = C.elementAt(pc);                                              // Next instruction
      if (Trace) say(pc, " ", i.name);                                               // Trace execution

      if (i.stop)                                                               // Stop execution
       {if (Dump)         print();                                              // Print results if tracing
        if (LowLevelCode) printInLowLevelCode();                                // Print low level code
        return;
       }

      i.pc = null;                                                              // Allow the instruction to set the new next instruction address
      i.i();                                                                    // Execute the instruction

      if (i.pc != null)                                                         // Jump to a different location if the instruction requested such a change
       {Integer next = null;                                                    // Next instruction
        for(int z = 0; z < C.size(); ++z)                                       // Find label by number
         {final Integer l = C.elementAt(z).label;
          if (l != null && l == i.pc)                                           // Found the label
           {next = z;                                                           // New instruction to start at
            break;
           }
         }
        if (next == null)                                                       // Cannot find requested label
         {say("Cannot find label: ", i.pc, "\n  At instruction: "+ pc);
          exit();
         }
        pc = next + 1;                                                          // Instruction following label
       }
      else pc++;                                                                // Go to not requested - go to next instruction by default
     }
   }

  public void printInLowLevelCode()                                             // Print a program in low level ISA format
   {say("Low level code for program: ", Title);

    for(I i : C)                                                                // Each instruction
     {Integer [] cts = i.l();
      if (cts[0] != null) say("constant ", cts[0]);
      if (cts[1] != null) say("target   ", cts[1]);
      if (cts[2] != null) say("source   ", cts[2]);
                          say("exec     ", i.name);
     }
   }

  public String toString(){                                                     // Stringify state of machine
      String s = "Registers:\n";
      for(int i = 0; i < R.length; i++)
       {s += String.format("%4d %c ", R[i], 'a'+i);
        if      (i % 8 == 7) s += "\n";
        else if (i % 4 == 3) s += "  ";
       }
      s += "\nData:    0   1   2   3   4     5   6   7   8   9\n   0: ";
      for(int i = 0; i < 40; i++)
       {s += String.format("%4d", D[i]);
        if      (i % 10 == 9) s += String.format("\n%4d: ", i+1);
        else if (i % 5  == 4) s += "  ";
       }
      s += "\n";
      return s;
  }

  public void print()                                                           // Print memory
   {say("Memory after running program: ", Title, "\n", toString());
   }

  public Integer[]d(Integer constant, Integer target, Integer source)           // Describe an instruction
   {return new Integer[] {constant, target, source};
   }
                                                                                // Instruction set
  void add         (int r1, int r2) {C.push(new I("add         ")       {void i() {     R[r1]+=R[r2];        }  Integer [] l() {return d(null, r2,   r1  );}});}
  void and         (int r1, int r2) {C.push(new I("and         ")       {void i() {     R[r1]&=R[r2];        }  Integer [] l() {return d(null, r2,   r1  );}});}
  void dec         (int r)          {C.push(new I("dec         ")       {void i() {     R[r]--;              }  Integer [] l() {return d(null, null, r   );}});}
  void inc         (int r)          {C.push(new I("inc         ")       {void i() {     R[r]++;              }  Integer [] l() {return d(null, null, r   );}});}
  void jump        (int r)          {C.push(new I("jump        ")       {void i() {     pc = r;              }  Integer [] l() {return d(null, null, r   );}});}
  void jumpBck     (int c)          {C.push(new I("jumpBck     ")       {void i() {     pc = c;              }  Integer [] l() {return d(c,    null, null);}});}
  void jumpFwd     (int c)          {C.push(new I("jumpFwd     ")       {void i() {     pc = c;              }  Integer [] l() {return d(c,    null, null);}});}
  void jumpBckif0  (int r, int c)   {C.push(new I("jumpBckif0  ")       {void i() { if (R[r] == 0) pc = c;   }  Integer [] l() {return d(c,    null, r   );}});}
  void jumpBckIfGt0(int r, int c)   {C.push(new I("jumpBckIfGt0")       {void i() { if (R[r] >  0) pc = c;   }  Integer [] l() {return d(c,    null, r   );}});}
  void jumpFwdif0  (int r, int c)   {C.push(new I("jumpFwdif0  ")       {void i() { if (R[r] == 0) pc = c;   }  Integer [] l() {return d(c,    null, r   );}});}
  void jumpFwdIfGt0(int r, int c)   {C.push(new I("jumpFwdIfGt0")       {void i() { if (R[r] >  0) pc = c;   }  Integer [] l() {return d(c,    null, r   );}});}
  void ldrd        (int r, int c)   {C.push(new I("ldrd        ")       {void i() {     R[r]     = D[c];     }  Integer [] l() {return d(c,    null, r   );}});}
  void ldrc        (int r, int c)   {C.push(new I("ldrc        ")       {void i() {     R[r]     = c;        }  Integer [] l() {return d(c,    null, r   );}});}
  void ldri        (int r1, int r2) {C.push(new I("ldri        ")       {void i() {     R[r1]    = D[R[r2]]; }  Integer [] l() {return d(null, r2,   r1  );}});}
  void ldrr        (int r1, int r2) {C.push(new I("ldrr        ")       {void i() {     R[r1]    = R[r2];    }  Integer [] l() {return d(null, r2,   r1  );}});}
  void not         (int r)          {C.push(new I("not         ")       {void i() {     R[r]     = ~R[r];    }  Integer [] l() {return d(null, null, r   );}});}
  void or          (int r1, int r2) {C.push(new I("or          ")       {void i() {     R[r1]   |= R[r2];    }  Integer [] l() {return d(null, r2,   r1  );}});}
  void put         (int r1)         {C.push(new I("put         ")       {void i() { say("r", r1, "=", R[r1]);}  Integer [] l() {return d(null, null, r1  );}});}
  void sll         (int r, int c)   {C.push(new I("sll         ")       {void i() {     R[r]   <<= c;        }  Integer [] l() {return d(c,    null, r   );}});}
  void strd        (int r, int c)   {C.push(new I("strd        ")       {void i() {     D[c]     = R[r];     }  Integer [] l() {return d(c,    null, r   );}});}
  void stri        (int r1, int r2) {C.push(new I("stri        ")       {void i() {     D[R[r2]] = R[r1];    }  Integer [] l() {return d(null, r2,   r1  );}});}
  void srl         (int r, int c)   {C.push(new I("srl         ")       {void i() {     R[r]   >>= c;        }  Integer [] l() {return d(c,    null, r   );}});}
  void stop        ()               {C.push(new I("stop        ", true) {void i() {                          }  Integer [] l() {return d(null, null, null);}});}
  void sub         (int r1, int r2) {C.push(new I("sub         ")       {void i() {     R[r1]   -= R[r2];    }  Integer [] l() {return d(null, r2,   r1  );}});}

  void label       (int l)          {C.push(new I("label", l)           {                                       Integer [] l() {return d(l,    null, null);}});}  // Define a label using an arbitrary integer

  public void program1to3(){                                                    // Count up to 3
    ldrc(a,0);
    strd(a,0);
    inc(a);
    strd(a,1);
    inc(a);
    strd(a,2);
    stop();
  }

  public void programFor()
   {ldrc(a,15);                                                                 // Loop variable in a
    label(11);                                                                  // For a..0
      stri(a,a);                                                                //   D[a] = a
      dec(a);                                                                   //   Decrement loop variable
      jumpBckIfGt0(a,11);                                                       // end_for
    stop();
  }

  public void programIf()
   {ldrc(a,15);                                                                 // Loop this many times
    label(11);                                                                  // For a..0
      ldrr(b,a);                                                                //   Test a for even/odd
      sll(b, 31);
      srl(b, 31);
      jumpFwdif0(b,22);                                                         //   if even
        stri(a,a);                                                              //     D[a] = a
      label(22);                                                                //   end_if
      dec(a);                                                                   //   Decrement loop variable
      jumpBckIfGt0(a, 11);                                                      // end_for
    stop();
  }

  public  void test1to3()                                                       // Test sequential execution
   {initialize("1 to 3");
    program1to3();
    run();
    assert(D[0] == 0);
    assert(D[1] == 1);
    assert(D[2] == 2);
    assert(R[a] == 2);
    Count++;
  }

  public  void testFor()                                                        // Test for loop
   {initialize("For Loop");
    programFor();
    run();
    for(int i  = 0; i < 16; ++i )assert(D[i] == i);
    Count++;
  }

  public  void testIf()                                                         // Test If statement
   {initialize("If");
    programIf();
    run();
    for(int i = 1; i < 15; i +=2 ) assert(D[i] == i);
    for(int i = 0; i < 14; i +=2 ) assert(D[i] == 0);
    Count++;
  }

  public static void say(Object...O)                                            // Say something
   {final StringBuilder b = new StringBuilder();
    for(Object o: O) b.append(o);
    System.err.print(b.toString()+"\n");
   }

  public static void exit()                                                     // Exit
   {System.exit(0);
   }

  public static void main(String args[])                                        // Tests
   {final Machine m = new Machine();
    Trace = false; Dump = true; LowLevelCode = true;
    m.test1to3();
    m.testFor();
    m.testIf();
    say("Passes: ", Count, " Tests");                                           // Passing test
   }
}
