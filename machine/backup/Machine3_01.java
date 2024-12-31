//------------------------------------------------------------------------------
// The BAN Mark 2 computer ISA for: CSE141L - Term Project
// Assembler, emulator and sample programs in less than 700 lines of Java code.
// Copyright: Brianna Ashley Nevarez, 2022-01-21, All Rights Reserved.
//------------------------------------------------------------------------------
/*
Executes Program 1,2 in high level assembler
Executes three other test progams written in high level assembler.
Prints low level assembler and machine code for each program.
Program 3 under development
*/
import java.util.*;

public class Machine3                                                           // Define a machine along with its associated Instruction set, assembler and emulator
 {static boolean Trace  = false, Dump = false, LowLevelCode = false;            // Trace program execution, dump memory after execution, print low level instructions
  static boolean PrintProgram       = false;                                    // Print program listing
  static boolean PrintNumberOfLines = false;                                    // Print number of lines
  static int     Count           = 0;                                           // Tests passed
  static int     LabelCounter    = 0;                                           // Generate a new label
  final  int     MaxInstructionsToExecute = 50_000,                             // Maximum number of instructions to execute
                 TargetBoxBase   = 0b110000000,                                 // Start of target register specification in low level instruction set
                 SourceBoxBase   = 0b101000000,                                 // Start of source register specification in low level instruction set
                 ConstantBoxBase = 0b000000000,                                 // Start of constant register specification in low level instruction set
                 OpCodeBase      = 0b111000000;                                 // Start of operation code specification in low level instruction set

  String         Title = null;                                                  // Title of the current program
  Map<Integer,I> manual = new TreeMap<>();                                      // Manual describing each instruction used

  Stack<I>       C = new Stack<>();                                             // High level instructions comprising the program
  Stack<Integer> L = new Stack<>();                                             // Low  level instructions as translated from the high level instructions comprising the program

  int [] R = new int[16];                                                       // Register file
  int [] D = new int[256];                                                      // Data memory

  class I                                                                       // Definition of an instruction in the High Level Instruction Architecture
   {void i() {};                                                                // Code for the instruction
    final String  name;                                                         // Name of instruction
          String  description = null;                                           // Description of the instruction
          String  format      = null;                                           // Format of the instruction
    final Integer labelOfThisInstruction;                                       // Optional numeric soft label for this instruction
    final Integer labelOfJumpTarget;                                            // Optional numeric soft label for the target of a jump instruction
    final boolean stop,                                                         // Whether we should stop on this instruction
                  labelSet;                                                     // Whether this instruction crates a label

    boolean jump = false;                                                       // False - continue to the next address true - goto the jump address
    int length;                                                                 // Instruction length
    int highLevelAddress,     lowLevelAddress;                                  // Hard address of this instruction in high / low level program
    int nextHighLevelAddress, nextLowLevelAddress;                              // Hard address of next instruction in high / low level program
    int jumpHighLevelAddress, jumpLowLevelAddress;                              // Hard address of jump instruction in high / low level program

    I(String Name)                                                              // Not a label or a jump instruction
     {name = Name;
      labelOfThisInstruction = labelOfJumpTarget = null;
      stop = name.equals("stop");
      labelSet               = false;
     }

    I(String Name, Integer Null, int Label)                                     // Specified label
     {name = Name;
      labelOfThisInstruction = Label;
      labelOfJumpTarget      = null;
      stop = name.equals("stop");
      labelSet               = true;
     }

    I(String Name, int constant)                                                // Constant specified in jump instruction as soft label of target instruction
     {name = Name;
      labelOfThisInstruction = labelOfJumpTarget = null;
      stop = name.equals("stop");
      labelSet               = false;
     }

    Integer opCode, targetBox, sourceBox, constantBox;                          // Boxes needed to translate instruction into low level instruction set architecture

    void boxes(Integer o, Integer t, Integer s, Integer c, String f, String d)  // Low level ISA code - target, source, constant boxes, format, description
     {opCode      = o;
      targetBox   = t;
      sourceBox   = s;
      constantBox = c;
      format      = f;
      description = d;
     }

    void lid() {}                                                               // Load instruction details

    void J(boolean j) {jump = j;}                                               // Whether we should jump or not
   }

  final int a = 0, b = 1, c =  2, d =  3, e =  4, f =  5, g =  6, h =  7,       // Register  names
            i = 8, j = 9, k = 10, l = 11, m = 12, n = 13, o = 14, p = 15;

  public void initialize(String title)                                          // Initialize machine by clearing registers and memory
   {for(int i = 0; i < D.length; i++) D[i] = 0;                                 // Clear data memory
    for(int i = 0; i < R.length; i++) R[i] = 0;                                 // Clear registers
    C.clear(); L.clear();                                                       // Clear code == program memory in the high and low instruction set representations
    LabelCounter = 0;
    this.Title = title;                                                         // Set title of current program
   }

  public void assemble()                                                        // Resolve addresses - we need to do this before running the program to convert soft jump labels into hard jump addresses
   {Integer pc = 0;                                                             // Program counter - program starts at first instruction
    if (Trace) say("<p>Assemble program: ", Title);

    int highLevelAddress = 0, lowLevelAddress = 0;                              // Current address in program
    for(I i : C)                                                                // Locate the address of each instruction in the program and the length of each instruction
     {i.lid();                                                                  // Load instruction details into instruction description
      i.highLevelAddress = highLevelAddress;                                    // Address of instruction in high level program
      i.lowLevelAddress  =  lowLevelAddress;                                    // Address of instruction in low level program
      int lll = 1;                                                              // Length of low level instruction
      if (i.targetBox   != null) lll++;                                         // Instruction also has a word for a target register
      if (i.sourceBox   != null) lll++;                                         // Instruction also has a word for a source register
      if (i.constantBox != null) lll++;                                         // Instruction also has a word for a constant
      highLevelAddress ++;                                                      // Location of next instruction in program memory using high level address
      if (i.labelSet) lll = 4;                                                  // The label instruction carries the soft target in its constant box, the low bits of the target in the source box and the high bits of the target in its target box
      lowLevelAddress += lll;                                                   // Location of next instruction in program memory using low level address
      i.length = lll;                                                           // Length of the low level instruction
      manual.put(i.opCode, i);                                                  // Update the manual
     }

    HashMap<Integer,I> jumps = new HashMap<>();                                 // Map between soft labels and target instructions
    for(I i : C)                                                                // Each instruction in the program
     {if (i.labelOfThisInstruction != null)                                     // The instruction has a soft label so map that label to the address of the instruction
       {jumps.put(i.labelOfThisInstruction, i);
       }
     }

    int N = 0;                                                                  // Number of jumps fixed
    for(I i : C)                                                                // Fix jump instructions so that they use a hard address rather than a soft label
     {if (i.name.length() > 4 && i.name.substring(0, 4).equals("jump"))         // Each jump instruction
       {final I target = jumps.get(i.constantBox);                              // Constant field tells us the label of the target of the jump

        if (target != null)                                                     // We have found the target instruction, so set address of target instruction as next instruction
         {i.jumpHighLevelAddress = target.highLevelAddress;
          ++N;
         }
        else                                                                    // Cannot find requested soft label
         {exit("  Cannot find label: ", i.constantBox, "\n  At instruction: ",  // Complain about missing label
               i.name,  " at address: ", i.highLevelAddress);
         }
       }
       i.nextHighLevelAddress = i.highLevelAddress + 1;                         // Next instruction in normal non jumping execution
       i.nextLowLevelAddress  = i. lowLevelAddress + i.length;                  // Next instruction in normal non jumping execution
     }

    if (Trace && N > 0) say("<p>Fixed: ", N, " jump instructions");             // Print number of jump instructions fixed

    for(I i : C)                                                                // Translate each high level instruction into a low level instruction now that we know the targets of branches
     {if (i.constantBox != null) L.push(i.constantBox + ConstantBoxBase);       // Constant if present
      if (i.sourceBox   != null) L.push(i.sourceBox   + SourceBoxBase);         // Source   if present
      if (i.targetBox   != null) L.push(i.targetBox   + TargetBoxBase);         // Target   if present

      if (i.labelSet)                                                           // Labels need special processing to load their targets into the target and source boxes
       {final I target = jumps.get(i.constantBox);                              // Constant field tells us the label of the target of the jump
        final int la   = target.lowLevelAddress;                                // A program address can be between 0 and 2^16 - 1

        L.push(SourceBoxBase + (la & 0xFF));                                    // Low bits of target in source box
        L.push(TargetBoxBase + (la >> 8));                                      // High bits of target in target box
       }
      L.push(i.opCode      + OpCodeBase);                                       // Operation code in the high level instruction set
     }

    if (PrintNumberOfLines) say(C.size(), " instructions");                     // Print number of instructions
   }

  public void printProgram()                                                    // Print a program
   {if (!PrintProgram) return;
    say("<h1>Listing of program : ", Title, "</h1>");
    say("<p>Number of instructions: ", C.size());                               // Print number of instructions
    say("<table cellpadding=10 border=0>");

    say("<tr><td>AddrH<td>AddL<td>NextH<td>NextL<td>JumpH<td>JumpL<td>LengthLow<td>Instruction<td>Target<td>Source<td>Constant");

    for(I i : C)                                                                // Locate the address of each instruction in the program and the length of each instruction
     {say(String.format
       ("<tr><td>%4d<td>%4d<td>%4d<td>%4d<td>%4d<td>%4d<td>%4d<td>%-12s<td>%4d<td>%4d<td>%4d",
        i.highLevelAddress, i.lowLevelAddress,
        i.nextHighLevelAddress, i.nextLowLevelAddress,
        i.jumpHighLevelAddress, i.jumpLowLevelAddress,
        i.length,               i.name,
        i.targetBox, i.sourceBox, i.constantBox));
     }
    say("</table>");
   }

  public void executeProgram()                                                  // Execute a program until it stops or we have executed a lot of instructions
   {int pc = 0;                                                                 // Program counter - program always starts at first instruction
    int J  = 0;                                                                 // Number of jumps
    if (Trace) say("<p>Start run of program: ", Title);                         // Title of trace output
    if (Trace) say("<table cellpadding=10 border=0>");

    for(int z = 0; z < MaxInstructionsToExecute; ++z)                           // Execute instructions until stop requested or we run out of time
     {final I i = C.elementAt(pc);                                              // Next instruction
      if (Trace && z % 40 == 0) say("<tr><td>#<td>Addr<td>Opcode<td>Operands"); // Column headers for trace output as it can go on for some time

      String c = "";
      if (Trace) c += String.format("<tr><td>%4d<td>%4d<td>%-12s", z, pc, i.name);        // Trace execution

      if (i.stop)                                                               // Stop execution
       {if (Trace)                                                              // Print number of instructions and number of jumps executed
         {say("</table>",
              "<p>Normal completion after executing: ",
               z, " instructions and ",
               J, " jumps");
         }
        if (Dump)         print();                                              // Print results if tracing
        if (LowLevelCode) printInLowLevelCode();                                // Print low level code
        return;
       }

      int [] RR = cloneInts(R);                                                 // Save all registers
      int [] DD = cloneInts(D);                                                 // Save all memory

      i.i();                                                                    // Execute the instruction

      Integer r = compareInts(RR, R);                                           // Check for changes in registers
      Integer d = compareInts(DD, D);                                           // Check for changes in memory

      if (r != null) c += String.format("    %c %d -> %d", 'a'+r, RR[r], R[r]);
      if (d != null) c += String.format("    D[%d] from %x to %x",  d, DD[d], D[d]);

      pc = i.jump ? i.jumpHighLevelAddress : i.nextHighLevelAddress;            // Next instruction address
      if (i.jump)
       {++J;                                                                    // Count number of jumps
        c += String.format("    Jump to %d", pc);                               // Track jump
       }

      if (Trace) say("<td>", c);                                                // Print changes if tracing
     }

    exit("Execution stopped after: ", MaxInstructionsToExecute);                // Complain about not enough cycles
   }

  public void run()                                                             // Fix the addresses in a program and then run it until it stops or we run out of cycles
   {assemble();                                                                 // Resolve addresses
    printProgram();                                                             // Print program
    executeProgram();                                                           // Execute program
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

  public void printInLowLevelCode()                                             // Print a program in low level ISA format
   {say("<h1>Low level code for program: ", Title, "</h1>");
    say("<pre>");

    for(I i : C)                                                                // Each instruction
     {final Integer c = i.constantBox, t = i.targetBox, s = i.sourceBox;

      if (t != null) say(codeWord(0b110000000 + t), " target   ", String.format("%12d  # %s",  t, "Load the target box with "  +t));
      if (s != null) say(codeWord(0b101000000 + s), " source   ", String.format("%12d  # %s",  s, "Load the source box with "  +s));
      if (c != null) say(codeWord(0b000000000 + c), " constant ", String.format("%12d  # %s",  c, "Load the constant box with "+c));
                     say(codeWord(0b111000000 + i.opCode),      " exec     ", String.format("%-12s  # %s", i.name, i.description));
     }
    say("</pre>");
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
     {final int d = D[i];
      s += d > 0 ? String.format(d >= 16 ? "  %2x" :  "   %x" , D[i]) : "  __";
      if      (i % 10 == 9) s += String.format("\n%4d: ", i+1);
      else if (i % 5  == 4) s += "  ";
     }
    s += "\n</pre>\n";
    return s;
   }

  public void print()                                                           // Print state of machine
   {say("<h1>Memory after running program: ", Title, "</h1>\n", toString());
   }

  public Integer[]d(Integer constant, Integer target, Integer source)           // Describe an instruction
   {return new Integer[] {constant, target, source};
   }
                                                                                // High level instruction set - 22 instructions actually needed
  void add         (int r1, int r2) {C.push(new I("add"               ) {void i() { R[r1]+=R[r2];               }  void lid() {boxes( 1, r2,   r1,   null, "RR", "Add the first and second registers together and replace the first register with the result");}});}
  void and         (int r1, int r2) {C.push(new I("and"               ) {void i() { R[r1]&=R[r2];               }  void lid() {boxes( 2, r2,   r1,   null, "RR", "And the first and second registers together and replace the first register with the result");}});}
  void cmpGtrc     (int r,  int c)  {C.push(new I("cmpgtrc"           ) {void i() { cmpGtRC(r, c);              }  void lid() {boxes( 3, null, r,    c   , "RC", "Compare the first register with the specified constant and place a one in the register if it is greater than the constant else zero");}});}
  void cmpLtrc     (int r,  int c)  {C.push(new I("cmpltrc"           ) {void i() { cmpLtRC(r, c);              }  void lid() {boxes( 4, null, r,    c   , "RC", "Compare the first register with the specified constant and place a one in the register if it is less than the constant else zero");}});}
//void cmpEqrc     (int r,  int c)  {C.push(new I("cmpeqrc"           ) {void i() { cmpEqRC(r, c);              }  void lid() {boxes( 5, null, r,    c   , "RC", "Compare the first register with the specified constant and place a one in the register if it is equal to the constant else zero");}});}
//void cmpGtrr     (int r1, int r2) {C.push(new I("cmpgtrr"           ) {void i() { cmpGtRR(r1, r2);            }  void lid() {boxes( 6, r2,   r1,   null, "RR", "Compare two registers and set the first register to one if it is greater than the second register else zero");}});}
//void cmpLtrr     (int r1, int r2) {C.push(new I("cmpltrr"           ) {void i() { cmpLtRR(r1, r2);            }  void lid() {boxes( 7, r2,   r1,   null, "RR", "Compare two registers and set the first register to one if it is less than the second register else zero");}});}
  void cmpEqrr     (int r1, int r2) {C.push(new I("cmpeqrr"           ) {void i() { cmpEqRR(r1, r2);            }  void lid() {boxes( 8, r2,   r1,   null, "RR", "Compare two registers and set the first register to one if it is equal to the second register else zero");}});}
  void dec         (int r)          {C.push(new I("dec"               ) {void i() { R[r]--;                     }  void lid() {boxes( 9, null, r,    null, "R",  "Decrement a register by one");}});}
  void inc         (int r)          {C.push(new I("inc"               ) {void i() { R[r]++;                     }  void lid() {boxes(10, null, r,    null, "R",  "Increment a register by one");}});}
//void jump        (int r)          {C.push(new I("jump",         r   ) {void i() { J(true);                    }  void lid() {boxes(11, null, null, c   , "R",  "");}});}  // Used ?
//void jumpBck     (int c)          {C.push(new I("jumpBck",      c   ) {void i() { J(true);                    }  void lid() {boxes(12, null, null, c   , "C",  "");}});}  // Used ?
//void jumpFwd     (int c)          {C.push(new I("jumpFwd",      c   ) {void i() { J(true);                    }  void lid() {boxes(13, null, null, c   , "C",  "");}});}  // Used ?
//void jumpBckIfZ  (int r,  int c)  {C.push(new I("jumpBckIfZ",   c   ) {void i() { J(R[r] == 0);               }  void lid() {boxes(14, null, r,    c   , "RC", "");}});}  // Used ?
  void jumpBckIfNz (int r,  int c)  {C.push(new I("jumpBckIfNz",  c   ) {void i() { J(R[r] >  0);               }  void lid() {boxes(15, null, r,    c   , "RC", "Jump backwards to the specified location in the program if the register is not zero - useful for constructing for loops");}});}
  void jumpFwdIfZ  (int r,  int c)  {C.push(new I("jumpFwdIfZ",   c   ) {void i() { J(R[r] == 0);               }  void lid() {boxes(16, null, r,    c   , "RC", "Jump forwards to the specified location in the program if the register is zero - useful for constructing if statements");}});}
//void jumpFwdIfNz (int r,  int c)  {C.push(new I("jumpFwdIfNz",  c   ) {void i() { J(R[r] >  0);               }  void lid() {boxes(17, null, r,    c   , "RC", "Jump forwards to the specified location in the program if the register is not zero - useful for constructing if statements");}});}
  void ldrd        (int r,  int c)  {C.push(new I("ldrd"              ) {void i() { R[r]     = D[c];            }  void lid() {boxes(18, null, r,    c   , "RD", "Load the first register from a memory location");}});}  // Used ?
  void ldrc        (int r,  int c)  {C.push(new I("ldrc"              ) {void i() { R[r]     = c;               }  void lid() {boxes(19, null, r,    c   , "RC", "Load the first register from a constant");}});}
  void ldri        (int r1, int r2) {C.push(new I("ldri"              ) {void i() { R[r1]    = D[R[r2]];        }  void lid() {boxes(20, r2,   r1,   null, "RI", "Load the first register from the memory location specified by the second register");}});}
  void ldrr        (int r1, int r2) {C.push(new I("ldrr"              ) {void i() { R[r1]    = R[r2];           }  void lid() {boxes(21, r2,   r1,   null, "RR", "Load the first register from the second register");}});}
//void not         (int r)          {C.push(new I("not"               ) {void i() { R[r]     = ~R[r];           }  void lid() {boxes(22, null, r,    null, "R",  "");}});}  // Used ?
  void or          (int r1, int r2) {C.push(new I("or"                ) {void i() { R[r1]   |= R[r2];           }  void lid() {boxes(23, r2,   r1,   null, "RR", "Or the first and second registers together and replace the first register with the result");}});}
  void putb        (int r)          {C.push(new I("putb"              ) {void i() { putB(r);                    }  void lid() {boxes(24, null, r,    null, "R",  "Write the contents of the specified register to the terminal in binary");}});}
  void putd        (int r)          {C.push(new I("putd"              ) {void i() { putD(r);                    }  void lid() {boxes(25, null, r,    null, "R",  "Write the contents of the specified register to the terminal in decimal");}});}
  void putx        (int r)          {C.push(new I("putx"              ) {void i() { putX(r);                    }  void lid() {boxes(26, null, r,    null, "R",  "Write the contents of the specified register to the terminal in hexadecimal");}});}
  void putRegs     ()               {C.push(new I("putRegs"           ) {void i() { sayInts(R);                 }  void lid() {boxes(27, null, null, null, "",   "Write all the registers"                                                                   );}});}
  void sl          (int r,  int c)  {C.push(new I("sll"               ) {void i() { shiftLeft(r, c);            }  void lid() {boxes(28, null, r,    c   , "RC", "Shift the first register left by the number of bits specified by the constant filling the vacated bits with zeroes.");}});}
  void strd        (int r,  int c)  {C.push(new I("strd"              ) {void i() { D[c]     = R[r];            }  void lid() {boxes(29, null, r,    c   , "RD", "Store the contents of the first register in the location specified by the constant");}});}
  void stri        (int r1, int r2) {C.push(new I("stri"              ) {void i() { D[R[r2]] = R[r1];           }  void lid() {boxes(30, r2,   r1,   null, "RI", "Store the contents of the first register in the location specified by the second register");}});}
  void sr          (int r,  int c)  {C.push(new I("srl"               ) {void i() { shiftRight(r, c);           }  void lid() {boxes(31, null, r,    c   , "RC", "Shift the first register right by the number of bits specified by the constant filling the vacated bits with zeroes.");}});}
  void stop        ()               {C.push(new I("stop"              ) {void i() {                             }  void lid() {boxes(32, null, null, null, "",   "Stop program execution");}});}
  void sub         (int r1, int r2) {C.push(new I("sub"               ) {void i() { R[r1]   -= R[r2];           }  void lid() {boxes(33, r2,   r1,   null, "RR", "Subtract the second register from the first register replace the first register with the result");}});}  // Used ?

  int  label       ()               {C.push(new I("label", null, newLabel())      {                                void lid() {boxes(34, null, null, LabelCounter, "", "Create and set a label");}}); return LabelCounter;}
  int  label       (int label)      {C.push(new I("label", null, label)           {                                void lid() {boxes(35, null, null, label,        "", "Set a label"           );}}); return label       ;}
  int  newLabel    ()               {return ++LabelCounter;}                    // Create a new label that can be set later using the label(int label) method

  void cmpGtRC(int r, int c)        {if (R[r] > c ) R[r] = 1;       else R[r] = 0; }
  void cmpLtRC(int r, int c)        {if (R[r] < c ) R[r] = 1;       else R[r] = 0; }
//void cmpEqRC(int r, int c)        {if (R[r] == c) R[r] = 1;       else R[r] = 0; }

//void cmpGtRR(int r2, int r1)      {if (R[r2] > R[r1] ) R[r2] = 1; else R[r2] = 0;}
//void cmpLtRR(int r2, int r1)      {if (R[r2] < R[r1] ) R[r2] = 1; else R[r2] = 0;}
  void cmpEqRR(int r2, int r1)      {if (R[r2] == R[r1]) R[r2] = 1; else R[r2] = 0;}

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
    a &= 0xFFFF;
    if (c < 0)  exit("Attempt to shift left by negative: " + c);
    if (c > 16) c = 16;
    R[r] = a << c;
   }

  void shiftRight(int r, int c)                                                 // Shift logical right with no sign extension
   {int a = R[r];
    a &= 0xFFFF;
    if (c < 0)  exit("Attempt to shift right by negative: " + c);
    if (c > 16) c = 16;
    R[r] = a >> c;
   }

// Test programs

  public void program1to3()                                                    // Count up to 3
   {ldrc(a,0);
    strd(a,0);
    inc(a);
    strd(a,1);
    inc(a);
    strd(a,2);
    stop();
   }

  public void programFor()
   {ldrc(a, 0xff);                                                              // Loop variable in a
    int loop = label();                                                         // For a..0
      stri(a, a);                                                               //   D[a] = a
      dec(a);                                                                   //   Decrement loop variable
      jumpBckIfNz(a, loop);                                                     // end_for
    stop();
   }

  public void programIf ()
   {ldrc        (a, 255);                                                        // Loop this many times
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

  void parityCheckStart(int r)                                                  // Start parity computation for register r
   {ldrc(p, 0);                                                                 // p = parity so far
    ldrr(o, r);                                                                 // o = copy of r to be parity checked
   }

  void parityCheckContinue(int r)                                               // Continue parity computation with new register
   {ldrr(o, r);                                                                 // o = copy of r to be parity checked
   }

  void parityCheckBit(int b)                                                    // Bit 7:0 of byte being tested
   {ldrc(m, 1<<b);                                                              // m = mask
    and (m, o);                                                                 // And source byte with mask and overwrite mask
    int endIf = newLabel();
    jumpFwdIfZ (m, endIf);                                                      // if (bit is one)
      inc (p);                                                                  //   Increase parity
    label(endIf);                                                               // end_if
   }

  void parityCheckBits(Integer...B)                                             // Parity check several bits
   {for(Integer b: B) parityCheckBit(b);
   }

  void parityCheckEnd  (int r)                                                  // Place the resulting parity in bi1 0 of the specified register
   {ldrc(r, 1);                                                                 // r = mask to select lowest bit in parity count
    and (r, p);                                                                 // r = parity
   }

  public void Program1()                                                        // Program 1 as described in: CSE141L - Term Project.pdf
   {ldrc(a,30);                                                                 // Address source byte
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

      ldrr(c, b);                                                               //   c = 7    6    5    4    3    2     1    0
      sl (c, 5);                                                                //   c = b11  b10  b_9  b_8  b_7  b_6   b_5  0
      sr (c, 9);                                                                //   .
      sl (c, 1);                                                                //   .

      ldrr(d, b);                                                               //   d = 7    6    5    4    3    2     1    0
      sl (d,  8);                                                               //   d = b_4  b_3  b_2  0    0    0     0    0
      sr (d, 13);                                                               //   .
      sl (d,  5);                                                               //   .

      ldrr(e, b);                                                               //   e = 7    6    5    4    3    2     1    0
      sl (e, 15);                                                               //   e = 0    0    0    0    b_1  0     0    0
      sr (e, 12);                                                               //   .

// f = p8  =  ^(b11:b5) = 0;
// g = p4  =  ^(b11:b8,b4,b3,b2) = 1;
// h = p2  =  ^(b11,b10,b7,b6,b4,b3,b1) = 0;
// i = p1  =  ^(b11,b9,b7,b5,b4,b2,b1) = 1;

      parityCheckStart(c);                                                      // f = p8 = 0
      parityCheckBits(1, 2, 3, 4, 5, 6, 7);
      parityCheckEnd(f);

      parityCheckStart(c);                                                      // g = p4 = 1
      parityCheckBits(4, 5, 6, 7);
      parityCheckContinue(d);
      parityCheckBits(5, 6, 7);
      parityCheckEnd(g);

      parityCheckStart(c);                                                      // h = p2 = 0
      parityCheckBits(2, 3, 6, 7);
      parityCheckContinue(d);
      parityCheckBits(6, 7);
      parityCheckContinue(e);
      parityCheckBits(3);
      parityCheckEnd(h);

      parityCheckStart(c);                                                      // i = p1 = 1
      parityCheckBits(1, 3, 5, 7);
      parityCheckContinue(d);
      parityCheckBits(5, 7);
      parityCheckContinue(e);
      parityCheckBits(3);
      parityCheckEnd(i);
//
// output
//   c = MSW = b11 b10 b9 b8 b7 b6 b5 p8
//   d = SW = b4 b3 b2 p4 b1 p2 p1 p16, where px denotes a parity bit
//   f = p16 = ^(b11:1,p8,p4,p2,p1) = 0;

      or   (c, f);                                                              // Format c and d with the expanded messages

      sl   (g, 4);
      or   (d, g);

      or   (d, e);

      sl   (h, 2);
      or   (d, h);

      sl   (i, 1);
      or   (d, i);

// mem[31] = 10101010 -- b11:b5, p8 = 1010101_0                    -- 0xAA
// mem[30] = 01011010 -- b4:b2, p4, b1, p2:p1, p16 = 010_1_1_01_0  -- 0x5A


      parityCheckStart(c);                                                      // f = p16 = 0
      for(int i = 0; i < 8; ++i) parityCheckBit(i);
      parityCheckContinue(d);
      for(int i = 1; i < 8; ++i) parityCheckBit(i);
      parityCheckEnd(f);

      or   (d, f);                                                              // c, d are now the high/low bytes with parity

      ldrc (b, 30);                                                             // b = 30
      add  (b, a);                                                              // b = a + 30
      stri (d, b);                                                              // Store c
      inc  (b);                                                                 // b = a + 31
      stri (c, b);                                                              // Store d at b = a + 31

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

  public void Program2()                                                        // Program 1 as described in: CSE141L - Term Project.pdf
   {ldrc(a, 94);                                                                 // Address source byte
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
      jumpBckIfNz(f, loop);                                                    // end_for
    stop();
   }

  public void Program3() {                                                      // Program 3 as described in: CSE141L - Term Project.pdf
//  public void problemThree()                                                  // bitsToLookFor = d
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

  public  void testIf ()                                                        // Test If statement
   {initialize("If");
    programIf ();
    run();
    for(int i = 1; i < 15; i +=2 ) assert(D[i] == i);
    for(int i = 0; i < 14; i +=2 ) assert(D[i] == 0);
    Count++;
  }

  public  void testProgram1()                                                   // Test program 1
   {initialize("Program 1");
    Program1();
    D[28] = 0x55;
    D[29] = 0x05;
    run();
    assert(D[28] == 0x55);
    assert(D[29] == 0x05);
    assert(D[58] == 0x5a);
    assert(D[59] == 0xaa);
    Count++;
  }

  public  void testProgram2()                                                   // Test program 2
   {initialize("Program 2");
    Program2();
    D[92] = 0x5A;
    D[93] = 0xAA;
    run();
    assert(D[ 92] == 0x5A);
    assert(D[ 93] == 0xAA);
    assert(D[122] == 0x55);
    assert(D[123] == 0x05);
    Count++;
  }

  public  void testProgram3a()                                                   // Test program 3
   {final int needle = 0b110;
    initialize("Program 3");
    D[128] =  35;                                                             // See Diagram 2
    D[129] = 102;
    D[130] =   0;
    D[131] = 192;                                                             // Part A - # of occurrences in every int
    D[160] = needle << 3;                                                     // Needle is in 7:3

    Program3();
    run();

    assert(D[192] == 1);
    assert(D[193] == 1);
    assert(D[194] == 3);
    assert(D[194] >= D[192]);
    Count++;
  }

  public  void testProgram3b()                                                   // Test program 3
   {final int needle = 0;
    initialize("Program 3");
    D[128] =  35;                                                             // See Diagram 2
    D[129] = 102;
    D[130] =   0;
    D[131] = 192;                                                             // Part A - # of occurrences in every int
    D[160] = needle << 3;                                                     // Needle is in 7:3

    Program3();
    run();

    assert(D[192] == 118);
    assert(D[193] == 30);
    assert(D[194] == 231);
    assert(D[194] >= D[192]);
    Count++;
  }

// Documentation

  public void printManual()                                                     // Print the High Level Instruction Architecture
   {say("<h1>High Level Instruction Set Architecture: Reference Manual</h1>");

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

// Utility functions

  public static void say(Object...O)                                            // Say something
   {final StringBuilder b = new StringBuilder();
    for(Object o: O) b.append(o);
    System.err.print(b.toString()+"\n");
   }

  public static void exit(Object...O)                                           // Say something and exit
   {say(O);
    System.exit(1);
   }

  public int [] cloneInts(int[]in)                                              // Clone an array of ints
   {int[]out = new int [in.length];
    for(int i = 0; i < in.length; ++i) out[i] = in[i];
    return out;
   }

  public Integer compareInts(int[]a, int[]b)                                    // Compare two arrays of integers and return the index of the first element that differs
   {for(int i = 0; i < a.length; ++i) if (a[i] != b[i]) return i;
    return null;
   }

  public void sayInts(int[]a)                                                   // Say the integers in an array
   {String s = "";
    for(int i = 0; i < a.length; ++i) s += " " + String.format("%3d", a[i]);
    if (s.length() > 0) say(s.substring(1, s.length()));
   }

  public static void main(String args[])                                        // Tests
   {final Machine3 m = new Machine3();
    Trace = false; Dump = false; LowLevelCode = false; PrintProgram = false; PrintNumberOfLines = true;        // Options
    //Trace = true; Dump = true; LowLevelCode = true; PrintProgram = true;        // Options

    //m.test1to3();                                                               // Structured programming tests
    //m.testFor();
    //m.testIf ();
    //
    //Trace = false;
    m.testProgram1();                                                           // Program 1 from the term paper
    //m.testProgram2();                                                           // Program 2 from the term paper
    //m.testProgram3a();                                                          // Program 3 from the term paper
    //m.testProgram3b();                                                          // Program 3 from the term paper
    //
    //m.printManual();

    say("Passes: ", Count, " Tests");                                           // Passing test
   }
 }
