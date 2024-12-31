//------------------------------------------------------------------------------
// The BAN Mark 2 computer ISA for: CSE141L - Term Project
// Programs, assembler, emulators for high and low level instruction formats.
// Copyright: Brianna Ashley Nevarez, 2022-01-27, All Rights Reserved.
//------------------------------------------------------------------------------
// All programs run in both the high and low level architectures!
import java.util.*;
import java.io.*;

class Machine3                                                                  // Define a machine along with its associated Instruction set, assembler and emulator
 {static boolean Trace  = false, Dump = false;                                  // Trace program execution, dump memory after execution
  static boolean PrintProgram         = false;                                  // Print program listing
  static boolean PrintISA             = false;                                  // Print ISA Reference manual
  static boolean PrintNumberOfLines   = false;                                  // Print number of lines
  static int     Tests           = 0;                                           // Number of tests passed
  static int     LabelCounter    = 0;                                           // Generate a new label
  final  int     MaxInstructionsToExecute = 5_100,                              // Maximum number of instructions to execute
                 ConstantBoxBase = 0b000000000,                                 // Start of constant register specification in low level instruction set
                 SourceBoxBase   = 0b101000000,                                 // Start of source register specification in low level instruction set
                 TargetBoxBase   = 0b110000000,                                 // Start of target register specification in low level instruction set
                 OpCodeBase      = 0b111000000;                                 // Start of operation code specification in low level instruction set

  enum OpCodes {add, and, cmpGtrc, cmpLtrc, cmpEqrc, cmpGtrr, cmpLtrr, cmpEqrr, // Operation codes
    dec, inc, jump, jumpBck, jumpFwd, jumpBckIfZ, jumpBckIfNz, jumpFwdIfZ,
    jumpFwdIfNz, ldrd, ldrc, ldri, ldrr, not, or, putb, putd, putx, putRegs,
    sl, sr, stop, strd, stri, sub, label;};

  enum ExecMode{high, low};                                                     // Execute in high level or low level emulator
  ExecMode execMode = ExecMode.high;                                            // Default execution mode

  String         Title = null;                                                  // Title of the current program
  static Stack<String> log       = new Stack<>();                               // Log the output of the program
  final Map<String,Integer>sizes = new TreeMap<>();                             // Size of each program so we can print a table show the program sizes
  final Map<String,Integer>execs = new TreeMap<>();                             // Number of instuction executions
  final Map<OpCodes,I> manual    = new TreeMap<>();                             // Manual describing each instruction used

  final Stack<I>       C         = new Stack<>();                               // High level instructions comprising the program
  final Stack<Integer> L         = new Stack<>();                               // Low  level instructions as translated from the high level instructions comprising the program

  final int a = 0, b = 1, c =  2, d =  3, e =  4, f =  5, g =  6, h =  7,       // Register  names
            i = 8, j = 9, k = 10, l = 11, m = 12, n = 13, o = 14, p = 15;

  final int [] R                 = new int[16];                                 // Register file
  final int [] D                 = new int[256];                                // Data memory

  class I                                                                       // Definition of an instruction in the High Level Instruction Architecture
   {void i() {};                                                                // Code for the instruction
    final Integer label;                                                        // Optional numeric soft label for this instruction
    boolean stop;                                                               // Whether we should stop on this instruction
    String  name                 = null;                                        // Name of instruction
    String  description          = null;                                        // Description of the instruction
    String  format               = null;                                        // Format of the instruction

    boolean jump = false;                                                       // False - continue to the next address true - goto the jump address
    int length,                                                                 // Instruction length
         address,                                                               // Address of this instruction in the high level program - the low level address is four times the high level address
         next;                                                                  // Address of next instruction in high level program

    int targetBox, sourceBox, constantBox;                                      // Boxes needed to translate instruction into low level instruction set architecture
    OpCodes opCode;                                                             // Opcode

    I target = null;                                                            // Address of jump target instruction in high level program
    I() {label = null;}                                                         // Not a label or a jump instruction
    I(int Label) {label = Label;}                                               // Specified label

    void boxes(OpCodes o, int t, int s, int c, String f, String d)              // Low level ISA code - target, source, constant boxes, format, description
     {if (t >=  64) exit("T must be less than 64 in ", o, " instruction");
      if (s >=  64) exit("S must be less than 64 in ", o, " instruction");
      if (c >= 256) exit("C must be less than 256 in ", o, " instruction");
      opCode      = o;
      targetBox   = t;
      sourceBox   = s;
      constantBox = c;
      format      = f;
      description = d;
     }

    void lid() {}                                                               // Load instruction details

    void J(boolean j) {jump = j;}                                               // Whether we should jump or not after the execution of this instruction
   }

  void initialize(String title, ExecMode em)                                    // Initialize machine by clearing registers and memory
   {for(int i = 0; i < D.length; i++) D[i] = 0;                                 // Clear data memory
    for(int i = 0; i < R.length; i++) R[i] = 0;                                 // Clear registers
    C.clear(); L.clear();                                                       // Clear code == program memory in the high and low instruction set representations
    LabelCounter = 0;
    this.Title = title+ " "+ em;                                                // Set title of current program
    execMode = em;                                                              // Record execution mode
   }

  void assemble()                                                               // Resolve addresses - we need to do this before running the program to convert soft jump labels into hard jump addresses
   {Integer pc = 0;                                                             // Program counter - program starts at first instruction
    if (Trace) say("<p>Assemble program: ", Title);

    int address = 0;                                                            // Current address in program
    for(I i : C)                                                                // Locate the address of each instruction in the program and the length of each instruction
     {i.lid();                                                                  // Load instruction details into instruction description
      i.address = address++;                                                    // Address of instruction in high level program
      manual.put(i.opCode, i);                                                  // Update the manual
      i.name = i.opCode.name();
      i.stop  = i.name.equals("stop");
     }

    final HashMap<Integer,I> jumps = new HashMap<>();                           // Map between soft labels and target instructions
    for(I i : C) if (i.label != null) jumps.put(i.label, i);                    // The instruction has a soft label so map that label to the address of the instruction

    int N = 0;                                                                  // Number of jumps fixed
    for(I i : C)                                                                // Fix jump instructions so that they use a hard address rather than a soft label
     {if (i.name.length() > 4 && i.name.substring(0, 4).equals("jump"))         // Each jump instruction
       {final I target = jumps.get(i.constantBox);                              // Constant field tells us the label of the target of the jump

        if (target != null)                                                     // We have found the target instruction, so set address of target instruction as next instruction
         {i.target = target;                                                    // Target instruction
          i.constantBox = target.address;                                       // The address of the target instruction is loaded into the target box so that it gets generated into the low level code
          ++N;
         }
        else                                                                    // Cannot find requested soft label
         {exit("  Cannot find label: ", i.constantBox, "\n  At instruction: ",  // Complain about missing label
               i.name,  " at address: ", i.address);
         }
       }
       i.next = i.address + 1;                                                  // Next instruction in normal non jumping execution
     }

    if (Trace && N > 0) say("<p>Fixed: ", N, " jump instructions");             // Print number of jump instructions fixed

    for(I i : C)                                                                // Translate each high level instruction into a low level instruction now that we know the targets of branches
     {L.push(i.constantBox + ConstantBoxBase);                                  // Constant
      L.push(i.sourceBox   + SourceBoxBase);                                    // Source
      L.push(i.targetBox   + TargetBoxBase);                                    // Target
      L.push(i.opCode.ordinal() + OpCodeBase);                                  // Operation code in the high level instruction set
     }

     sizes.put(Title, C.size());                                                // Record program size
   }

  void printProgramSizes()                                                      // Print a table of program sizes
   {if (!PrintNumberOfLines) return;
    say("<h1>Program sizes</h1>");
    say("<p>The following table shows the number of instructions in each program run, the number of instructions executed if the program terminated else the program is flagged as having failed:");
    say("<table cellpadding=10 border=0>");
    say("<tr><td><b>Count</b><td><b>Execs</b><td><b>Program</b>");

    for(String t : sizes.keySet())                                              // Each program
     {say("<tr><td>"+sizes.get(t)+"<td>");
      final Integer e = execs.get(t);
      if (e != null) say(e); else say("<b>FAILED</b>");                         // Number of instructions executed or failed to stop
      say("<td>"+t);
     }

    say("</table>");
   }

  void printProgram()                                                           // Print a program
   {if (!PrintProgram || execMode != ExecMode.high) return;
    say("<h1>Listing of program : ", Title, "</h1>");
    say("<p>Number of instructions: ", C.size());                               // Print number of instructions
    say("<table cellpadding=10 border=0>");

    say("<tr><td>Address<td>Next<td>Jump<td>Instruction<td>Target<td>Source<td>Constant");

    for(I i : C)                                                                // Locate the address of each instruction in the program and the length of each instruction
     {final I t = i.target;
      say(String.format
       ("<tr><td>%4d<td>%4d<td>%4s<td>%-12s<td>%4d<td>%4d<td>%4d",
        i.address,   i.next,
        t != null ?  String.format("%4d", t.address) : "",
        i.name,  i.targetBox, i.sourceBox, i.constantBox));
     }
    say("</table>");
   }

  void printProgramLowLevel()                                                   // Print a program in low level ISA format
   {if (!PrintProgram || execMode != ExecMode.low) return;
    say("<h1>Low level code for program: ", Title, "</h1>");
    say("<pre>");

    int n = 0;
    for(I i : C)                                                                // Each instruction
     {final Integer c = i.constantBox, t = i.targetBox, s = i.sourceBox;

      final int td = 0b110000000 + t;
      final int sd = 0b101000000 + s;
      final int cd = 0b000000000 + c;
      final int id = 0b111000000 + i.opCode.ordinal();

      final String tb = codeWord(td);
      final String sb = codeWord(sd);
      final String cb = codeWord(cd);
      final String ib = codeWord(id);

      if (t != null) say(String.format("%6d:  %s  %4x target   %-12d  # %s",  n++, tb, td, t,      "Load the target box with "  +t));
      if (s != null) say(String.format("%6d:  %s  %4x source   %-12d  # %s",  n++, sb, sd, s,      "Load the source box with "  +s));
      if (c != null) say(String.format("%6d:  %s  %4x constant %-12d  # %s",  n++, cb, cd, c,      "Load the constant box with "+c));
                     say(String.format("%6d:  %s  %4x exec     %-12s  # %s",  n++, ib, id, i.name, i.description));
     }
    say("</pre>");
   }

  void printISA()                                                               // Print the High Level Instruction Architecture but only for instructions used by a running program
   {if (!PrintISA) return;
    say("<h1>High Level Instruction Set Architecture: Reference Manual</h1>");

    Integer opCode, targetBox, sourceBox, constantBox;                          // Boxes needed to translate instruction into low level instruction set architecture

    say("<table border=0 cellpadding=10px>");
    for(I i : manual.values())
     {say("<tr><td>");
      say("<tr><td colspan=3><h2>", i.name, "</h2>");
      say("<tr><td>Operation code<td><b><big>", i.opCode, "</big></b>");
      say("<tr><td>Format        <td>");

      if (i.format.equals(""))   say("<td>No operand");
      if (i.format.equals("C"))  say("C<td>Single Constant operand");
      if (i.format.equals("R"))  say("R<td>Single Register operand");
      if (i.format.equals("RC")) say("RC<td>Double Register from Constant");
      if (i.format.equals("RD")) say("RD<td>Double Register from/to Direct memory");
      if (i.format.equals("RI")) say("RI<td>Double Register from/to indirect memory");
      if (i.format.equals("RR")) say("RR<td>Double Register to register");

      say("<tr><td>Description<td colspan=2>", i.description);
     }
    say("</table>");
   }

  void printLog()                                                               // Print the log to a file
   {try
     {final FileWriter w = new FileWriter("ISA.html");
      for(String s: log) w.write(s);
      w.close();
     }
    catch (Exception e)
     {System.err.println("Error: "+e);
      e.printStackTrace();
    }
  }

  public String toString()                                                      // Stringify state of machine
   {String s = "<pre>\nRegisters:\n";
    for(int i = 0; i < R.length; i++)
     {s += String.format("%4d %c ", R[i], 'a'+i);
      if      (i % 8 == 7) s += "\n";
      else if (i % 4 == 3) s += "  ";
     }
    s += "\nData:    0   1   2   3   4     5   6   7   8   9\n   0: ";
    for(int i = 0; i < D.length; i++)
     {final int d = D[i] & 0xFF;
      s += d > 0 ? String.format(d >= 16 ? "  %2x" :  "   %x" , d) : "  __";
      if      (i % 10 == 9) s += String.format("\n%4d: ", i+1);
      else if (i % 5  == 4) s += "  ";
     }
    s += "\n</pre>\n";
    return s;
   }

  void print()                                                                  // Print state of machine
   {say("<h1>Memory after running program: ", Title, "</h1>\n", toString());
   }

  void executeProgram()                                                         // Execute a program until it stops or we have executed a lot of instructions
   {int pc = 0;                                                                 // Program counter - program always starts at first instruction
    int J  = 0;                                                                 // Number of jumps
    if (Trace) say("<p>Start run of program: ", Title);                         // Title of trace output
    if (Trace) say("<pre>");

    for(int z = 0; z < MaxInstructionsToExecute; ++z)                           // Execute instructions until stop requested or we run out of time
     {final I i = C.elementAt(pc);                                              // Next instruction
      //if (Trace && z % 40 == 0) say("<tr><td>#<td>Addr<td>Opcode<td>Target<td>Source<td>Constant<td>Changes"); // Column headers for trace output as it can go on for some time

      String c = "";
      if (Trace) c += String.format("%4d  %4d  %-12s  %4d  %4d  %4d",           // Trace execution
                      z, pc, i.name, i.targetBox, i.sourceBox, i.constantBox);

      if (i.stop)                                                               // Stop execution
       {if (Trace)                                                              // Print number of instructions and number of jumps executed
         {say("</pre>",
              "<p>Normal completion after executing: ",
               z, " instructions and ",
               J, " jumps");
         }
        if (Dump) print();                                                      // Print results if tracing
        execs.put(Title, z);                                                    // Print number of instructions
        return;
       }

      int [] RR = cloneInts(R);                                                 // Save all registers
      int [] DD = cloneInts(D);                                                 // Save all memory

      i.i();                                                                    // Execute the instruction

      final Integer r = compareInts(RR, R);                                     // Check for changes in registers
      final Integer d = compareInts(DD, D);                                     // Check for changes in memory

      if (r != null) c += String.format("    %c %d -> %d", 'a'+r,      RR[r], R[r]);
      if (d != null) c += String.format("    D[%d] from %x to %x",  d, DD[d], D[d]);

      pc = i.jump ? i.target.address : i.next;                                  // Next instruction address

      if (i.jump)
       {++J;                                                                    // Count number of jumps
        c += String.format("    Jump to %d", pc);                               // Track jump
       }

      if (Trace) say(c);                                                        // Print changes if tracing
     }

    say("Execution stopped after: ", MaxInstructionsToExecute);                 // Complain about not enough cycles
    return;
   }

  void executeLowLevelProgram()                                                 // Execute a program in the low level ISA until it stops or we have executed a lot of instructions. This is the routine (minus the tracing statements) that we need to translate into Verilog.
   {int pc = 0,                                                                 // Program counter - program always starts at first instruction
        J  = 0,                                                                 // Number of jumps
        C  = 0, S = 0, T = 0;
    if (Trace) say("<p>Start run of low level program: ", Title);               // Title of trace output
    if (Trace) say("<pre>");

    int N = 0;                                                                  // Count number of high level instructions executed
    for(int z = 0; z < (MaxInstructionsToExecute<<4); ++z)                      // Execute instructions until stop requested or we run out of time
     {final int i = L.elementAt(pc);                                            // Next instruction
      if (Trace && N % 40 == 0)
       {//say("<tr><td>#<td>Addr<td>Opcode<td>Target<td>Source<td>Constant<td>Changes");     // Column headers for trace output as it can go on for some time
       }

      if      (i < SourceBoxBase) C = i;                                        // Constant
      else if (i < TargetBoxBase) S = i & ~SourceBoxBase;                       // Source
      else if (i < OpCodeBase)    T = i & ~TargetBoxBase;                       // Target

      else                                                                      // Execute instruction
       {final OpCodes O = OpCodes.values()[i & ~OpCodeBase & 0xFF];             // Instruction to execute
        String c = "";
        if (Trace)
         {c += String.format                                                    // Trace execution
           ("%4d  %4d  %-12s  %4d  %4d  %4d",
            N, pc/4, O, T, S, C);
         }

        N++;

        int [] RR = cloneInts(R);                                               // Save all registers
        int [] DD = cloneInts(D);                                               // Save all memory
        boolean jump = false;                                                   // Set true by jumps so we can count them

        switch(O)                                                               // Decode operation code
         {case add        : R[T]     += R[S];                            break;
          case and        : R[T]     &= R[S];                            break;
          case cmpEqrr    : if (R[S] == R[T]) R[T] = 1;  else R[T] = 0;  break;
          case cmpGtrc    : if (R[S]  > C )    R[S] = 1; else R[S] = 0;  break;
          case cmpLtrc    : if (R[S]  < C )    R[S] = 1; else R[S] = 0;  break;
          case dec        : R[S]--;                                      break;
          case inc        : R[S]++;                                      break;
          case jumpBckIfNz: if (R[S]  > 0)    {pc = C * 4;  jump = true;}break; // 4 low level clocks per high level instruction, but we could have made it some other number if we wished without altering the high level instruction set architecture.
          case jumpFwdIfZ : if (R[S] == 0)    {pc = C * 4;  jump = true;}break;
          case label      :                                              break;
          case ldrc       : R[S]      = C;                               break;
          case ldrd       : R[S]      = D[C];                            break;
          case ldri       : R[T]      = D[R[S]];                         break;
          case ldrr       : R[T]      = R[S];                            break;
          case not        : R[S]     = ~R[S];                            break; // No ~= operator in Java!
          case or         : R[T]     |= R[S];                            break;
          case sl         : R[S] <<= C; R[S] &= 0xFFFF;                  break;
          case sr         : R[S] &= 0xFFFF; R[S] >>= C;                  break;
          case strd       : D[C]    = R[S];                              break;
          case stri       : D[R[S]] = R[T];                              break;
          case sub        : R[T]   -= R[S];                              break;
          case stop       :
            if (Trace)                                                          // Print number of instructions and number of jumps executed
             {say("</pre>",
                  "<p>Normal completion after executing: ",
                  z, " instructions and ",
                  J, " jumps");
             }
            if (Dump) print();                                                  // Print results if tracing
            execs.put(Title, z);                                                // Print number of instructions
          return;
          default:
            exit("Unknown instruction: ", O);
        }

        final Integer r = compareInts(RR, R);                                   // Check for changes in registers
        final Integer d = compareInts(DD, D);                                   // Check for changes in memory

        if (r != null) c += String.format("    %c %d -> %d", 'a'+r, RR[r], R[r]);
        if (d != null) c += String.format("    D[%d] from %x to %x",  d, DD[d], D[d]);

        if (jump)
         {++J;                                                                  // Count number of jumps
          c += String.format("    Jump to %d", pc / 4);                         // Track jump
         }

        if (Trace) say(c);                                                      // Print changes if tracing
       }

      pc++;                                                                     // Increment instruction counter
     }

    say("Execution stopped after: ", MaxInstructionsToExecute);                 // Complain about not enough cycles
    return;
   }

  void run(ExecMode ll)                                                         // Assemble a program and then run it until it stops or we run out of cycles using either the high level or low level emulator
   {assemble();                                                                 // Resolve addresses
    printProgram();                                                             // Print program
    printProgramLowLevel();                                                     // Print low level code
    if (ll == ExecMode.high) executeProgram(); else executeLowLevelProgram();   // Execute program
    printLog();                                                                 // So we can crash in an assert and still have a copy of the log to view in a browser
   }

  String codeWord(int r)                                                        // Print a number as 9 bits to form a ow level code word
   {r &= 0x1FF;
    String b = "";
    for(int i = 8; i >= 0; --i)
     {final int m = 1 << i;
      b += (r & m) > 0 ? "1" : "0";
     }
    return b;
   }
                                                                                // High level instruction set - 22 instructions actually needed                                                                                // High level instruction set - 22 instructions actually needed
  void add         (int t, int s) {C.push(new I() {void i() {R[t]+=R[s];       } void lid() {boxes(OpCodes.add        , t, s, 0, "RR", "Add the first and second registers together and replace the first register with the result");}});}
  void and         (int t, int s) {C.push(new I() {void i() {R[t]&=R[s];       } void lid() {boxes(OpCodes.and        , t, s, 0, "RR", "And the first and second registers together and replace the first register with the result");}});}
  void cmpGtrc     (int r, int c) {C.push(new I() {void i() {cmpGtRC(r, c);    } void lid() {boxes(OpCodes.cmpGtrc    , 0, r, c, "RC", "Compare the first register with the specified constant and place a one in the register if it is greater than the constant else zero");}});}
  void cmpLtrc     (int r, int c) {C.push(new I() {void i() {cmpLtRC(r, c);    } void lid() {boxes(OpCodes.cmpLtrc    , 0, r, c, "RC", "Compare the first register with the specified constant and place a one in the register if it is less than the constant else zero");}});}
//void cmpEqrc     (int r, int c) {C.push(new I() {void i() {cmpEqRC(r, c);    } void lid() {boxes(OpCodes.cmpEqrc    , 0, r, c, "RC", "Compare the first register with the specified constant and place a one in the register if it is equal to the constant else zero");}});}
//void cmpGtrr     (int t, int s) {C.push(new I() {void i() {cmpGtRR(t, s);    } void lid() {boxes(OpCodes.cmpGtrr    , t, s, 0, "RR", "Compare two registers and set the first register to one if it is greater than the second register else zero");}});}
//void cmpLtrr     (int t, int s) {C.push(new I() {void i() {cmpLtRR(t, s);    } void lid() {boxes(OpCodes.cmpLtrr    , t, s, 0, "RR", "Compare two registers and set the first register to one if it is less than the second register else zero");}});}
  void cmpEqrr     (int t, int s) {C.push(new I() {void i() {cmpEqRR(t, s);    } void lid() {boxes(OpCodes.cmpEqrr    , t, s, 0, "RR", "Compare two registers and set the first register to one if it is equal to the second register else zero");}});}
  void dec         (int r)        {C.push(new I() {void i() {R[r]--;           } void lid() {boxes(OpCodes.dec        , 0, r, 0, "R",  "Decrement a register by one");}});}
  void inc         (int r)        {C.push(new I() {void i() {R[r]++;           } void lid() {boxes(OpCodes.inc        , 0, r, 0, "R",  "Increment a register by one");}});}
//void jump        (int r)        {C.push(new I() {void i() {J(true);          } void lid() {boxes(OpCodes.jump       , 0, 0, c, "R",  "");}});}  // Used ?
//void jumpBck     (int c)        {C.push(new I() {void i() {J(true);          } void lid() {boxes(OpCodes.jumpBck    , 0, 0, c, "C",  "");}});}  // Used ?
//void jumpFwd     (int c)        {C.push(new I() {void i() {J(true);          } void lid() {boxes(OpCodes.jumpFwd    , 0, 0, c, "C",  "");}});}  // Used ?
//void jumpBckIfZ  (int r, int c) {C.push(new I() {void i() {J(R[r] == 0);     } void lid() {boxes(OpCodes.jumpBckIfZ , 0, r, c, "RC", "");}});}  // Used ?
  void jumpBckIfNz (int r, int c) {C.push(new I() {void i() {J(R[r] >  0);     } void lid() {boxes(OpCodes.jumpBckIfNz, 0, r, c, "RC", "Jump backwards to the specified location in the program if the register is not zero - useful for constructing for loops");}});}
  void jumpFwdIfZ  (int r, int c) {C.push(new I() {void i() {J(R[r] == 0);     } void lid() {boxes(OpCodes.jumpFwdIfZ , 0, r, c, "RC", "Jump forwards to the specified location in the program if the register is zero - useful for constructing if statements");}});}
//void jumpFwdIfNz (int r, int c) {C.push(new I() {void i() {J(R[r] >  0);     } void lid() {boxes(OpCodes.jumpFwdIfNz, 0, r, c, "RC", "Jump forwards to the specified location in the program if the register is not zero - useful for constructing if statements");}});}
  void ldrd        (int r, int c) {C.push(new I() {void i() {R[r]    = D[c];   } void lid() {boxes(OpCodes.ldrd       , 0, r, c, "RD", "Load the first register from a memory location");}});}  // Used ?
  void ldrc        (int r, int c) {C.push(new I() {void i() {R[r]    = c;      } void lid() {boxes(OpCodes.ldrc       , 0, r, c, "RC", "Load the first register from a constant");}});}
  void ldri        (int t, int s) {C.push(new I() {void i() {R[t]    = D[R[s]];} void lid() {boxes(OpCodes.ldri       , t, s, 0, "RI", "Load the first register from the memory location specified by the second register");}});}
  void ldrr        (int t, int s) {C.push(new I() {void i() {R[t]    = R[s];   } void lid() {boxes(OpCodes.ldrr       , t, s, 0, "RR", "Load the first register from the second register");}});}
  void not         (int r)        {C.push(new I() {void i() {R[r]    = ~R[r];  } void lid() {boxes(OpCodes.not        , 0, r, 0, "R",  "Invert the bits in a register");}});}  // Used ?
  void or          (int t, int s) {C.push(new I() {void i() {R[t]   |= R[s];   } void lid() {boxes(OpCodes.or         , t, s, 0, "RR", "Or the first and second registers together and replace the first register with the result");}});}
  void putb        (int r)        {C.push(new I() {void i() {putB(r);          } void lid() {boxes(OpCodes.putb       , 0, r, 0, "R",  "Write the contents of the specified register to the terminal in binary");}});}
  void putd        (int r)        {C.push(new I() {void i() {putD(r);          } void lid() {boxes(OpCodes.putd       , 0, r, 0, "R",  "Write the contents of the specified register to the terminal in decimal");}});}
  void putx        (int r)        {C.push(new I() {void i() {putX(r);          } void lid() {boxes(OpCodes.putx       , 0, r, 0, "R",  "Write the contents of the specified register to the terminal in hexadecimal");}});}
  void putRegs     ()             {C.push(new I() {void i() {sayIntsX(R);      } void lid() {boxes(OpCodes.putRegs    , 0, 0, 0, "",   "Write all the registers"                                                                   );}});}
  void sl          (int r, int c) {C.push(new I() {void i() {shiftLeft(r, c);  } void lid() {boxes(OpCodes.sl         , 0, r, c, "RC", "Shift the first register left by the number of bits specified by the constant filling the vacated bits with zeroes.");}});}
  void sr          (int r, int c) {C.push(new I() {void i() {shiftRight(r, c); } void lid() {boxes(OpCodes.sr         , 0, r, c, "RC", "Shift the first register right by the number of bits specified by the constant filling the vacated bits with zeroes.");}});}
  void stop        ()             {C.push(new I() {void i() {                  } void lid() {boxes(OpCodes.stop       , 0, 0, 0, "",   "Stop program execution");}});}
  void strd        (int r, int c) {C.push(new I() {void i() {D[c]    = R[r];   } void lid() {boxes(OpCodes.strd       , 0, r, c, "RD", "Store the contents of the first register in the location specified by the constant");}});}
  void stri        (int t, int s) {C.push(new I() {void i() {D[R[s]] = R[t];   } void lid() {boxes(OpCodes.stri       , t, s, 0, "RI", "Store the contents of the first register in the location specified by the second register");}});}
  void sub         (int t, int s) {C.push(new I() {void i() {R[t]   -= R[s];   } void lid() {boxes(OpCodes.sub        , t, s, 0, "RR", "Subtract the second register from the first register replace the first register with the result");}});}  // Used ?

  int  label       ()               {C.push(new I(newLabel()) {                  void lid() {boxes(OpCodes.label      , 0,  0,   LabelCounter, "", "Create and set a label");}}); return LabelCounter;}
  int  label       (int label)      {C.push(new I(label)      {                  void lid() {boxes(OpCodes.label      , 0,  0,   label,        "", "Set a label"           );}}); return label       ;}
  int  newLabel    ()               {return ++LabelCounter;}                    // Create a new label that can be set later using the label(int label) method

  void cmpGtRC(int r, int c)   {if (R[r] > c ) R[r] = 1;       else R[r]  = 0;}
  void cmpLtRC(int r, int c)   {if (R[r] < c ) R[r] = 1;       else R[r]  = 0;}
//void cmpEqRC(int r, int c)   {if (R[r] == c) R[r] = 1;       else R[r]  = 0;}

//void cmpGtRR(int s, int t) {if (R[s] > R[t] ) R[s] = 1; else R[s] = 0;}
//void cmpLtRR(int s, int t) {if (R[s] < R[t] ) R[s] = 1; else R[s] = 0;}
  void cmpEqRR(int s, int t) {if (R[s] == R[t]) R[s] = 1; else R[s] = 0;}

  void putB(int r)                                                              // Print a register in binary
   {int a = R[r];
    a &= 0xFFFF;
    String b = "";
    for(int i = 15; i >= 0; --i)
     {final int m = 1 << i;
      b += (a & m) > 0 ? "1" : "0";
      if (i > 0 && i % 4 == 0) b += " ";
     }
    say(String.format("%c = %sb", ('a'+r), b));
   }

  void putD(int r)                                                              // Print a register in decimal
   {int a = R[r];
    a &= 0xFFFF;
    say(String.format("%c = %d ", ('a'+r), R[r]));
   }

  void putX(int r)                                                              // Print a register in hexadecimal
   {int a = R[r];
    a &= 0xFFFF;
    say(String.format("%c = 0x%x ", ('a'+r), R[r]));
   }

  void shiftLeft(int r, int c)                                                  // Shift logical left with no sign extension
   {int a = R[r];
    if (c < 0)  exit("Attempt to shift left by negative: " + c);
    if (c > 16) c = 16;
    R[r] = (a << c) & 0xFFFF;
   }

  void shiftRight(int r, int c)                                                 // Shift logical right with no sign extension
   {int a = R[r];
    a &= 0xFFFF;
    if (c < 0)  exit("Attempt to shift right by negative: " + c);
    if (c > 16) c = 16;
    R[r] = a >> c;
   }

// Test programs

  void programTestInstructions()                                                // Test individual instructions
   {ldrc(o, 1);          ldrc(p, 2);      add(o, p); strd(o, 0);
    ldrc(p, 0b00010101); not(p);                     strd(p, 1);
    ldrc(o, 0b0101);     ldrc(p, 0b1001); and(o, p); strd(o, 2);
    ldrc(o, 0b0101);     ldrc(p, 0b1001); or (o, p); strd(o, 3);
    ldrc(c, 21);         inc(c);                     strd(c, 4);
    ldrr(d, c);          dec(d);                     strd(d, 5);
    ldrc(c, 3);          sl (c, 1);                  strd(c, 6);
    ldrc(c, 3);          sl (c, 2);                  strd(c, 7);
    ldrc(e, 20);         ldrc(f, 9);      sub(e, f); strd(e, 8);
    ldrc(b, 9);          ldrc(c, 99);                stri(c, b);
    stop();
   }

  void testInstructions(ExecMode ll)                                            // Test individual instructions
   {initialize("Instructions", ll);
    programTestInstructions();
    run(ll);
    assert( D[0]         == 3);
    assert((D[1] & 0xFF) == 0b11101010);
    assert( D[2] == 1);
    assert( D[3] == 13);
    assert( D[4] == 22);
    assert( D[5] == 21);
    assert( D[6] ==  6);
    assert( D[7] == 12);
    assert( D[8] == 11);
    assert( D[9] == 99);
    Tests++;
  }

  void program1to3()                                                            // Count up to 3
   {ldrc(a,0);
    strd(a,0);
    inc(a);
    strd(a,1);
    inc(a);
    strd(a,2);
    stop();
   }

  void programFor()
   {ldrc(a, 0xff);                                                              // Loop variable in a
    int loop = label();                                                         // For a..0
      stri(a, a);                                                               //   D[a] = a
      dec(a);                                                                   //   Decrement loop variable
      jumpBckIfNz(a, loop);                                                     // end_for
    stop();
   }

  void programIf ()
   {ldrc        (a, 255);                                                       // Loop this many times
    int loop = label();                                                         // For a..0
      ldrr      (b,  a);                                                        //   Test a for even/odd
      sl        (b, 15);
      sr        (b, 15);
      int endIf = newLabel();
      jumpFwdIfZ(b, endIf);                                                     //   if even
        stri    (a, a);                                                         //     D[a] = a
      label     (endIf);                                                        //   end_if
      dec(a);                                                                   //   Decrement loop variable
      jumpBckIfNz(a, loop);                                                     // end_for
    stop();
   }

// Program 1

  void Program1()                                                               // Program 1 as described in: CSE141L - Term Project.pdf
   {final int saveA = 255;                                                      // Location in memory at which to save a
    ldrc(a,30);                                                                 // Address source byte
    int loop = label();                                                         // For a..0
      dec (a);                                                                  //   Decrement loop variable
// input
//   MSW = 0 0 0 0 0 b11 b10 b09
//   LSW = b8 b7 b6 b5 b4 b3 b2 b1, where bx denotes a data bit
// output
//   MSW = b11 b10 b9 b8 b7 b6 b5 p8
//   LSW = b4 b3 b2 p4 b1 p2 p1 p16, where px denotes a parity bit

// 28   29
// 0x55 0x05
      ldri(b, a);                                                               //   b = high byte in low position
      sl (b, 8);                                                                //   b = high byte in high position
      dec (a);
      ldri(c, a);                                                               //   c = value of low byte
      or  (b, c);                                                               //   b = [28, 29]
      strd(a, saveA);                                                           //   Save a to free it

      ldrr(c, b); sl(c,  5); sr(c, 15);                                         //   c=b11  1
      ldrr(d, b); sl(d,  6); sr(d, 15);                                         //   d=b10  0
      ldrr(e, b); sl(e,  7); sr(e, 15);                                         //   e=b_9  1
      ldrr(f, b); sl(f,  8); sr(f, 15);                                         //   f=b_8  0
      ldrr(g, b); sl(g,  9); sr(g, 15);                                         //   g=b_7  1
      ldrr(h, b); sl(h, 10); sr(h, 15);                                         //   h=b_6  0
      ldrr(i, b); sl(i, 11); sr(i, 15);                                         //   i=b_5  1
      ldrr(j, b); sl(j, 12); sr(j, 15);                                         //   j=b_4  0
      ldrr(k, b); sl(k, 13); sr(k, 15);                                         //   k=b_3  1
      ldrr(l, b); sl(l, 14); sr(l, 15);                                         //   l=b_2  0
      ldrr(m, b); sl(m, 15); sr(m, 15);                                         //   m=b_1  1
      ldrr(n, b);                                                               //       7    6    5    4    3    2     1    0
      sl (n, 5);                                                                //   n = b11  b10  b_9  b_8  b_7  b_6   b_5  0
      sr (n, 9);                                                                //       1    0    1    0    1    0     1    0 = 0xAA
      sl (n, 1);                                                                //

      ldrr(o, b);                                                               //       7    6    5    4    3    2     1    0
      sl (o,  8);                                                               //   o = b_4  b_3  b_2  0    0    0     0    0
      sr (o, 13);                                                               //       0    1    0    0    0    0     0    0 = 0x40
      sl (o,  5);                                                               //

      ldrr(p, b);                                                               //       7    6    5    4    3    2     1    0
      sl (p, 15);                                                               //   p = 0    0    0    0    b_1  0     0    0
      sr (p, 12);                                                               //                                              0x08

      or (o, p);                                                                //       7    6    5    4    3    2     1    0
                                                                                //   o = b_4  b_3  b_2  0    b1   0     0    0  0x48
                                                                                //   abp free
// output
//   c = MSW = b11 b10 b9 b8 b7 b6 b5 p8
//   d = SW = b4 b3 b2 p4 b1 p2 p1 p16, where px denotes a parity bit

// p = p8  =  ^(b11:b5) = 0;
      ldrr(p,c); add(p,d); add(p,e); add(p,f); add(p,g); add(p,h); add(p,i);
      ldrc(a, 1);
      and (a, p);
      ldrr(b, a);                                                               // use B to sum the parities
      or  (n, a);                                                               //   n = b11  b10  b_9  b_8  b_7  b_6   b_5  p8  0xAA

// p = p4  =  ^(b11:b8,b4,b3,b2) = 1;
      ldrr(p,c); add(p,d); add(p,e); add(p,f); add(p,j); add(p,k); add(p,l);
      ldrc(a, 1);
      and (a, p);
      add (b, a);                                                               // use B to sum the parities
      sl  (a, 4);
      or  (o, a);                                                               //   o = b_4  b_3  b_2  p4    b1   0     0    0   0x58

// p = p2  =  ^(b11,b10,b7,b6,b4,b3,b1) = 0;
      ldrr(p,c); add(p,d); add(p,g); add(p,h); add(p,j); add(p,k); add(p,m);
      ldrc(a, 1);
      and (a, p);
      add (b, a);                                                               // use B to sum the parities
      sl  (a, 2);
      or  (o, a);                                                               //   o = b_4  b_3  b_2  p4    b1   p2    0    0  0x58

// p = p1  =  ^(b11,b9,b7,b5,b4,b2,b1) = 1;
      ldrr(p,c); add(p,e); add(p,g); add(p,i); add(p,j); add(p,l); add(p,m);
      ldrc(a, 1);
      and (a, p);
      add (b, a);                                                               // use B to sum the parities
      sl  (a, 1);
      or  (o, a);                                                               //   o = b_4  b_3  b_2  p4    b1   p2    p1   0 0x5A

// p = p16 = ^(b11:1,p8,p4,p2,p1) = 0;
      add(b,c); add(b,d); add(b,e); add(b,f); add(b,g); add(b,h); add(b,i); add(b,j); add(b,k); add(b,l); add(b,m);
      ldrc(a, 1);
      and (a, b);
      or  (o, a);                                                               //   o = b_4  b_3  b_2  p4    b1   p2    p1  p16 0x5A

      ldrd(a, saveA);                                                           // Restore a

      ldrc (b, 30);                                                             // b = 30
      add  (b, a);                                                              // b = a + 30
      stri (o, b);                                                              // Store o
      inc  (b);                                                                 // b = a + 31
      stri (n, b);                                                              // Store n at b = a + 31

      jumpBckIfNz(a, loop);                                                     // end_for
    stop();
   }

// Program 2
// input
//   MSW = b11  b10  b_9  b_8  b_7  b6   b5   0   at 92  .. 64    c   1010 1010
//   LSW = b_4  b_3  b_2  0    b_1  0    0    0   at 93     65    b   0101 1010

// output
//   MSW = 0    0    0    0    0    b11  b10  b09   at 122..94    d  0000 0101
//   LSW = b_8  b_7  b_6  b_5  b_4  b_3  b_2  b_1   at 123..95    e  0101 0101

  void Program2()                                                               // Program 1 as described in: CSE141L - Term Project.pdf
   {ldrc(a, 94);                                                                // Address source byte
    int loop = label();                                                         // For a..0
      dec (a);                                                                  //   Decrement loop variable
      ldri(c, a);                                                               //   c = 93 MSW
      dec (a);                                                                  //   Decrement loop variable
      ldri(b, a);                                                               //   b = 92 LSW

      ldrr(d, c);
      sr  (d, 5);                                                               //   d = 0000 0101

      ldrr(e,  c);
      sr  (e,  1);                                                              //   e = 0101 0000
      sl  (e, 12);                                                              //
      sr  (e,  8);                                                              //

      ldrr(f, b);
      sr  (f, 5);                                                               //   e = 0101 0100
      sl  (f, 1);                                                               //
      or  (e, f);                                                               //

      ldrr(f, b);
      sl  (f, 12);                                                              //   e = 0101 0101
      sr  (f, 15);                                                              //
      or  (e, f);                                                               //

      ldrc(f, 30);                                                              //   f = a + 30
      add (f, a);
      stri(e, f);
      inc (f);
      stri(d, f);

      ldrr(f, a);
      cmpGtrc(f, 64);
      jumpBckIfNz(f, loop);                                                     // end_for
    stop();
   }

  void Program3() {                                                             // Program 3 as described in: CSE141L - Term Project.pdf
//  void problemThree()                                                         // bitsToLookFor = d
//   {int a = 0, b = 0, c = 0, d = 0, e = 0, f = 0, g = 0b11111,                // g is the mask
    ldrc(g, 0b11111);
//        h = 0, i = 0, j = 0, k = 0, l = 0, m = 0, n = 0, o = 0, p = 0;
//
//  d = D[160];
    ldrd(d, 160);
//  d >>= 3;                                                                    // Put needle into positions 4:0
    sr  (d, 3);

//  for(i = 159; i >= 0; i--)                                                   // Finding # of matches
    ldrc(i, 159);
    int loop = label();
//   {n = 0;
      ldrc(n, 0);
//    f = 0;
      ldrc(f, 0);
//    h = i;
      ldrr(h, i);
//    h = D[h];
      ldri(h, h);
//    f = h;
      ldrr(f, h);
//    f &= g;
      and (f, g);
//    k = f == d ? 1 : 0;
      ldrr   (k, f);
      cmpEqrr(k, d);
//
      int endIf1 = newLabel();
//    if (k != 0) {
      jumpFwdIfZ(k, endIf1);
//      n++;
        inc(n);
//     }
      label(endIf1);
//
      for(int z = 0; z < 3; z++) {                                              // Java
//      h >>= 1;
        sr  (h, 1);
//      f = h;
        ldrr(f, h);
//      f &= g;
        and (f, g);
//      k = f == d ? 1 : 0;
        ldrr   (k, f);
        cmpEqrr(k, d);
//
        int endIf2 = newLabel();
//      if (k != 0) :
        jumpFwdIfZ(k, endIf2);
//        n++;
          inc(n);
//       }
        label(endIf2);
       }
//
//    a += n;                                                                   // Total number of matches from mem[128:159]
      add(a, n);

      int endIf3 = newLabel();
//    if (n != 0)
      jumpFwdIfZ(n, endIf3);
//     {b++;
        inc(b);
//     }                                                                        // Number of ints with at least a match
      label(endIf3);
//
//    j = i;
      ldrr(j, i);
//    j = i < 159 ? 1 : 0;
      cmpLtrc(j, 159);
//
      int endIf4 = newLabel();
//    if (j != 0) {
      jumpFwdIfZ(j, endIf4);
//      m = 0;
        ldrc(m, 0);
//      e = i;
        ldrr(e, i);
//      e ++;
        inc(e);
//      e = D[e];                                                               // Combining two ints at a time to make a word
        ldri(e, e);
//      e <<= 8;
        sl  (e, 8);
//
//      f = i;
        ldrr(f, i);
//      f = D[f];
        ldri(f, f);
//      e |= f;
        or (e, f);
//      f = e;
        ldrr(f, e);
//      f &= g;
        and (f, g);
//
//      k = f == d ? 1 : 0;
        ldrr(k, f);
        cmpEqrr(k, d);

        int endIf5 = newLabel();
//      if (k != 0)
        jumpFwdIfZ(k, endIf5);
//       {m++;
          inc(m);
//       }
        label(endIf5);
//
        for(int q = 0; q < 11; q++) {                                           // Java
//        e >>= 1;
          sr  (e, 1);
//        f  = e;
          ldrr(f, e);
//        f &= g;
          and (f, g);
//        k = f == d ? 1 : 0;
          ldrr(k, f);
          cmpEqrr(k, d);
//
          int endIf6 = newLabel();
//        if (k != 0) {
          jumpFwdIfZ(k, endIf6);
//          m++;
            inc(m);
//        }
          label(endIf6);
        }
//
//      c += m;
        add(c, m);
      label(endIf4);

//    //System.out.println(String.format("%2d: %3d  %3d  %3d  %3d  %3d  %3d  %3d  %3d  %3d  %3d  %3d  %3d  %3d  %3d  %3d  %3d", i, a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p));
//    k = i < 159 ? 1 : 0;
      ldrr(k, i);
      cmpLtrc(k, 159);

//
      int endIf7 = newLabel();
//    if   (k != 0) {
        jumpFwdIfZ(k, endIf7);

//      k = i > 128 ? 1 : 0;
        ldrr(k, i);
        cmpGtrc(k, 128);
//
        int endIf8 = newLabel();
//      if (k != 0) {
          jumpFwdIfZ(k, endIf8);
//        c -= n;
          sub(c, n);
//       }
        label(endIf8);
//     }
      label(endIf7);

//    --i;
      dec(i);
//    k = i > 127 ? 1 : 0;
      ldrr(k, i);
      cmpGtrc(k, 127);
//    if (k != 0) {
//      continue;
        jumpBckIfNz(k, loop);
//     }
//    break;
//   }
//
//  D[192] = a;                                                                 // Storing Part A answer
    strd(a,192);                                                                // PPP -  a = memory[192]
//  D[193] = b;                                                                 // Storing Part B answer
    strd(b,193);                                                                // PPP -  b = memory[193]
//  D[194] = c;                                                                 // Storing Part C answer
    strd(c,194);                                                                // PPP -  c = memory[194]

    stop();
  }

  void test1to3(ExecMode ll)                                                    // Test sequential execution
   {initialize("1 to 3", ll);
    program1to3();
    run(ll);
    assert(D[0] == 0);
    assert(D[1] == 1);
    assert(D[2] == 2);
    assert(R[a] == 2);
    Tests++;
  }

  void testFor(ExecMode ll)                                                     // Test for loop
   {initialize("For Loop", ll);
    programFor();
    run(ll);
    for(int i  = 0; i < 16; ++i )  assert(D[i] == i);
    Tests++;
  }

  void testIf(ExecMode ll)                                                      // Test If statement
   {initialize("If", ll);
    programIf ();
    run(ll);
    for(int i = 1; i < 15; i +=2 ) assert(D[i] == i);
    for(int i = 0; i < 14; i +=2 ) assert(D[i] == 0);
    Tests++;
  }

  void testProgram1(ExecMode ll)                                                // Test program 1
   {initialize("Program 1", ll);
    Program1();
    D[28] = 0x55;
    D[29] = 0x05;
    run(ll);
    assert(D[28] == 0x55);
    assert(D[29] == 0x05);
    assert(D[58] == 0x5a);
    assert(D[59] == 0xaa);
    Tests++;
   }

  void testProgram2(ExecMode ll)                                                // Test program 2
   {initialize("Program 2", ll);
    Program2();
    D[92] = 0x5A;
    D[93] = 0xAA;
    run(ll);
    assert(D[ 92] == 0x5A);
    assert(D[ 93] == 0xAA);
    assert(D[122] == 0x55);
    assert(D[123] == 0x05);
    Tests++;
  }

  void testProgram3a(ExecMode ll)                                               // Test program 3
   {final int needle = 0b110;
    initialize("Program 3a", ll);
    D[128] =  35;                                                               // See Diagram 2
    D[129] = 102;
    D[130] =   0;
    D[131] = 192;                                                               // Part A - # of occurrences in every int
    D[160] = needle << 3;                                                       // Needle is in 7:3

    Program3();
    run(ll);

    assert(D[192] == 1);
    assert(D[193] == 1);
    assert(D[194] == 3);
    assert(D[194] >= D[192]);
    Tests++;
  }

  void testProgram3b(ExecMode ll)                                               // Test program 3
   {final int needle = 0;
    initialize("Program 3b", ll);
    D[128] =  35;                                                               // See Diagram 2
    D[129] = 102;
    D[130] =   0;
    D[131] = 192;                                                               // Part A - # of occurrences in every int
    D[160] = needle << 3;                                                       // Needle is in 7:3

    Program3();
    run(ll);

    assert(D[192] == 118);
    assert(D[193] ==  30);
    assert(D[194] == 231);
    assert(D[194] >= D[192]);
    Tests++;
   }

// Utility functions

  static void say(Object...O)                                                   // Say something
   {final StringBuilder b = new StringBuilder();
    for(Object o: O) b.append(o);
    final String s = b.toString()+"\n";
    System.err.print(s);
    log.push(s);
   }

  static void exit(Object...O)                                                  // Say something and exit
   {say(O);
    System.exit(1);
   }

  int [] cloneInts(int[]in)                                                     // Clone an array of ints
   {int[]out = new int [in.length];
    for(int i = 0; i < in.length; ++i) out[i] = in[i];
    return out;
   }

  Integer compareInts(int[]a, int[]b)                                           // Compare two arrays of integers and return the index of the first element that differs
   {for(int i = 0; i < a.length; ++i) if (a[i] != b[i]) return i;
    return null;
   }

  void sayInts(int[]a)                                                          // Say the integers in an array
   {String s = "";
    for(int i = 0; i < a.length; ++i) s += " " + String.format("%3d", a[i]);
    if (s.length() > 0) say(s.substring(1, s.length()));
   }

  void sayIntsX(int[]a)                                                         // Say the integers in an array in hexadecimal
   {String s = "";
    for(int i = 0; i < a.length; ++i) s += " " + String.format("%4x", a[i]);
    if (s.length() > 0) say(s.substring(1, s.length()));
   }

  void sayTests()                                                               // Say the results of the tests
   {printISA();                                                                 // Include the manual if requested
    printProgramSizes();                                                        // Include the program sizes if requested
    say("Passes: "+ Tests + " Tests");
    printLog();
   }

  static public void main1(String args[])                                        // Test one program
   {final Machine3 m = new Machine3();
    Trace = true; Dump = true; PrintProgram = true; PrintNumberOfLines = false; // Options
    m.testInstructions(ExecMode.low);
    m.sayTests();
   }

  static public void main(String args[])                                       // Test all programs
   {final Machine3 m = new Machine3();
    Trace = true; Dump = true; PrintProgram = true;  PrintNumberOfLines = true; // Options

    final ExecMode[]EM = new ExecMode[]{ExecMode.high, ExecMode.low};
    for (ExecMode em : EM)
     {Trace = true;
      m.testInstructions(em);                                                   // Instruction tests
      m.test1to3(em);                                                           // Structured programming tests
      m.testFor(em);
      m.testIf (em);

      Trace = false;
      m.testProgram1(em);                                                       // Program 1 from the term paper
      m.testProgram2(em);                                                       // Program 2 from the term paper
      m.testProgram3a(em);                                                      // Program 3 from the term paper
      m.testProgram3b(em);                                                      // Program 3 from the term paper
     }
    m.sayTests();
   }
 }
