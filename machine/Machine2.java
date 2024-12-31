//------------------------------------------------------------------------------
// The BAN Mark 2 computer ISA for: CSE141L - Term Project
// Copyright: Brianna Ashley Nevarez, 2022-01-21
//------------------------------------------------------------------------------
/*
Executes Program 1 in high level assembler
Executes three other test progams written in high level assembler.
Prints low level assembler and machine code for each program.
Need Program 2 and Program 3
*/
import java.util.*;

public class Machine2 {
  static boolean Trace = false, Dump = false, LowLevelCode = false;             // Trace program execution, dump memory, print low level instructions
  static int     Count = 0;                                                     // Tests passed
  final  int     MaxInstructionsToExecute = 10_000;                             // Maximum number of instructions to execute
  static int     LabelCounter = 0;                                              // Generate a new label
  String         Title = null;                                                  // Title of the current program

  int [] R = new int[16];                                                       // Register file
  int [] D = new int[256];                                                      // Data memory

  class I                                                                       // Definition of an instruction in the High Level Instruction Architecture
   {void i() {};                                                                // Code for the instruction
    final String  name;                                                         // Name of instruction
    final Integer labelOfThisInstruction;                                       // Optional numeric soft label for this instruction
    final Integer labelOfJumpTarget;                                            // Optional numeric soft label for the target of a jump instruction
    final boolean stop;                                                         // Whether we should stop

    boolean jump = false;                                                       // False - continue to the next address true - goto the jump address
    int length;                                                                 // Instruction length
    int highLevelAddress,     lowLevelAddress;                                  // Hard address of this instruction in high / low level program
    int nextHighLevelAddress, nextLowLevelAddress;                              // Hard address of next instruction in high / low level program
    int jumpHighLevelAddress, jumpLowLevelAddress;                              // Hard address of jump instruction in high / low level program

    I(String Name)                                                              // Not a label or a jump instruction
     {name = Name;
      labelOfThisInstruction = null;
      labelOfJumpTarget      = null;
      stop = name.equals("stop");
     }

    I(String Name, Integer Null, int Label)                                     // Specified label
     {name = Name;
      labelOfThisInstruction = Label;
      labelOfJumpTarget      = null;
      stop = name.equals("stop");
     }

    I(String Name, int constant)                                                // Constant specified in jump instruction as soft label of target instruction
     {name = Name;
      labelOfThisInstruction = null;
      labelOfJumpTarget      = null;
      stop = name.equals("stop");
     }

    Integer opCode, targetBox, sourceBox, constantBox;                          // Boxes needed to translate instruction into low level instruction set architecture

    void boxes(Integer o, Integer t, Integer s, Integer c)                      // Low level ISA code - target, source, constant boxes
     {opCode      = o;
      targetBox   = t;
      sourceBox   = s;
      constantBox = c;
     }

    void lid() {}                                                               // Load instruction details

    void J(boolean j) {jump = j;}                                               // Whether we should jump or not
   }

  Stack<I> C = new Stack<>();                                                   // Code
  final int a = 0, b = 1, c =  2, d =  3, e =  4, f =  5, g =  6, h =  7,       // Register  names
            i = 8, j = 9, k = 10, l = 11, m = 12, n = 13, o = 14, p = 15;

  public void initialize(String title)                                          // Initialize machine by clearing registers and memory
   {for(int i = 0; i < D.length; i++) D[i] = 0;                                 // Clear data memory
    for(int i = 0; i < R.length; i++) R[i] = 0;                                 // Clear registers
    C.clear();                                                                  // Clear code == program memory
    LabelCounter = 0;
    this.Title = title;                                                         // Set title of current program
   }

  public void assemble()                                                        // Resolve addresses - we need to do this before running the program to convert soft jump labels into hard jump addresses
   {Integer pc = 0;                                                             // Program counter - program starts at first instruction
    if (Trace) say("Assemble program: ", Title);

    int highLevelAddress = 0, lowLevelAddress = 0;                              // Current address in program
    for(I i : C)                                                                // Locate the address of each instruction in the program and the length of each instruction
     {i.lid();                                                                  // Load instruction details into instruction description
      i.highLevelAddress = highLevelAddress;                                    // Address of instruction in high level program
      i.lowLevelAddress  =  lowLevelAddress;                                    // Address of instruction in low level program
      int l = 1;                                                                // Length of instruction is at least one word
      if (i.targetBox   != null) l++;                                           // Instruction also has a word for a target register
      if (i.sourceBox   != null) l++;                                           // Instruction also has a word for a source register
      if (i.constantBox != null) l++;                                           // Instruction also has a word for a constant
      highLevelAddress ++;                                                      // Location of next instruction in program memory using high level address
       lowLevelAddress += l;                                                    // Location of next instruction in program memory using low level address
      i.length = l;                                                             // Length of the low level instruction
     }

    HashMap<Integer,Integer> jumps = new HashMap<>();                           // Map between soft labels and hard addresses
    for(I i : C)                                                                // Each instruction in the program
     {if (i.labelOfThisInstruction != null)                                     // The instruction has a soft label so map that label to the address of the instruction
       {jumps.put(i.labelOfThisInstruction, i.highLevelAddress);
       }
     }

    int N = 0;                                                                  // Number of jumps fixed
    for(I i : C)                                                                // Fix jump instructions so that they use a hard address rather than a soft label
     {if (i.name.length() > 4 && i.name.substring(0, 4).equals("jump"))         // Each jump instruction
       {Integer target = jumps.get(i.constantBox);                                // Constant field tells us the label of the target of the jump

        if (target != null) {i.jumpHighLevelAddress = target; ++N;}             // We have found the target instruction, so set address of target instruction as next instruction
        else                                                                    // Cannot find requested soft label
         {say("  Cannot find label: ", i.constantBox, "\n  At instruction: ",   // Complain about missing label
               i.name,  " at address: ", i.highLevelAddress);
          exit();                                                               // Exit after complaining about a missing label
         }
       }
     i.nextHighLevelAddress = i.highLevelAddress + 1;                           // Next instruction in normal non jumping execution
     i.nextLowLevelAddress  = i. lowLevelAddress + i.length;                    // Next instruction in normal non jumping execution
     }
    if (Trace) say("  Fixed ", N, " jump instructions");                        // Print number of jump instructions fixed
   }

  public void printProgram()                                                    // Print a program
   {say("Listing of program : ", Title);

    say("AddrH  AddL  NextH  NextL  JumpH  JumpL LengthLow  Instruction  Target  Source Constant");

    for(I i : C)                                                                // Locate the address of each instruction in the program and the length of each instruction
     {say(String.format
       (" %4d  %4d   %4d   %4d   %4d   %4d      %4d  %-12s   %4d    %4d     %4d",
        i.highLevelAddress, i.lowLevelAddress,
        i.nextHighLevelAddress, i.nextLowLevelAddress,
        i.jumpHighLevelAddress, i.jumpLowLevelAddress,
        i.length,               i.name,
        i.targetBox, i.sourceBox, i.constantBox));
     }
   }

  public void executeProgram()                                                  // Execute a program until it stops or we have executed a lot of instructions
   {int pc = 0;                                                                 // Program counter - program always starts at first instruction
    int J  = 0;                                                                 // Number of jumps
    if (Trace) say("Start run of program: ", Title);                            // Title of trace output

    for(int z = 0; z < MaxInstructionsToExecute; ++z)                           // Execute instructions until stop requested or we run out of time
     {final I i = C.elementAt(pc);                                              // Next instruction
      if (Trace && z % 40 == 0) say("#     Addr    Opcode          Operands");  // Column headers for trace output as it can go on for some time

      String c = "";
      if (Trace) c += String.format("%4d  %4d    %-12s", z, pc, i.name);        // Trace execution

      if (i.stop)                                                               // Stop execution
       {if (Trace)                                                              // Print number of instructions and number of jumps executed
         {say("Normal completion after executing: ", z, " instructions and ",
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

      if(Trace) say(c);                                                         // Print changes if tracing
     }

    say("Execution stopped after: ", MaxInstructionsToExecute);                 // Complain about not enough cycles
    exit();                                                                     // Exit after complaining about not enough cycles
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
   {say("Low level code for program: ", Title);

    for(I i : C)                                                                // Each instruction
     {if (i.targetBox   != null) say(codeWord(0b110000000 + i.targetBox),   " target   ", i.targetBox  );
      if (i.sourceBox   != null) say(codeWord(0b101000000 + i.sourceBox),   " source   ", i.sourceBox  );
      if (i.constantBox != null) say(codeWord(0b000000000 + i.constantBox), " constant ", i.constantBox);
                                 say(codeWord(0b111000000 + i.opCode),      " exec     ", i.name);
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
      for(int i = 0; i < D.length; i++)
       {final int d = D[i];
        s += d > 0 ? String.format(d > 16 ? "  %2x" :  "   %x" , D[i]) : "  __";
        if      (i % 10 == 9) s += String.format("\n%4d: ", i+1);
        else if (i % 5  == 4) s += "  ";
       }
      s += "\n";
      return s;
  }

  public void print()                                                           // Print state of machine
   {say("Memory after running program: ", Title, "\n", toString());
   }

  public Integer[]d(Integer constant, Integer target, Integer source)           // Describe an instruction
   {return new Integer[] {constant, target, source};
   }
                                                                                // High level instruction set
  void add         (int r1, int r2) {C.push(new I("add"               ) {void i() { R[r1]+=R[r2];               }  void lid() {boxes( 1, r2,   r1,   null);}});}
  void and         (int r1, int r2) {C.push(new I("and"               ) {void i() { R[r1]&=R[r2];               }  void lid() {boxes( 2, r2,   r1,   null);}});}
  void dec         (int r)          {C.push(new I("dec"               ) {void i() { R[r]--;                     }  void lid() {boxes( 3, null, r,    null);}});}
  void inc         (int r)          {C.push(new I("inc"               ) {void i() { R[r]++;                     }  void lid() {boxes( 4, null, r,    null);}});}
  void jump        (int r)          {C.push(new I("jump",         r   ) {void i() { J(true);                    }  void lid() {boxes( 5, null, null, c   );}});}  // Used ?
  void jumpBck     (int c)          {C.push(new I("jumpBck",      c   ) {void i() { J(true);                    }  void lid() {boxes( 6, null, null, c   );}});}  // Used ?
  void jumpFwd     (int c)          {C.push(new I("jumpFwd",      c   ) {void i() { J(true);                    }  void lid() {boxes( 7, null, null, c   );}});}  // Used ?
  void jumpBckif0  (int r,  int c)  {C.push(new I("jumpBckif0",   c   ) {void i() { J(R[r] == 0);               }  void lid() {boxes( 8, null, r,    c   );}});}  // Used ?
  void jumpBckIfGt0(int r,  int c)  {C.push(new I("jumpBckIfGt0", c   ) {void i() { J(R[r] >  0);               }  void lid() {boxes( 9, null, r,    c   );}});}
  void jumpFwdif0  (int r,  int c)  {C.push(new I("jumpFwdif0",   c   ) {void i() { J(R[r] == 0);               }  void lid() {boxes(10, null, r,    c   );}});}
  void jumpFwdIfGt0(int r,  int c)  {C.push(new I("jumpFwdIfGt0", c   ) {void i() { J(R[r] >  0);               }  void lid() {boxes(11, null, r,    c   );}});}  // Used ?
  void ldrd        (int r,  int c)  {C.push(new I("ldrd"              ) {void i() { R[r]     = D[c];            }  void lid() {boxes(12, null, r,    c   );}});}  // Used ?
  void ldrc        (int r,  int c)  {C.push(new I("ldrc"              ) {void i() { R[r]     = c;               }  void lid() {boxes(13, null, r,    c   );}});}
  void ldri        (int r1, int r2) {C.push(new I("ldri"              ) {void i() { R[r1]    = D[R[r2]];        }  void lid() {boxes(14, r2,   r1,   null);}});}
  void ldrr        (int r1, int r2) {C.push(new I("ldrr"              ) {void i() { R[r1]    = R[r2];           }  void lid() {boxes(15, r2,   r1,   null);}});}
  void not         (int r)          {C.push(new I("not"               ) {void i() { R[r]     = ~R[r];           }  void lid() {boxes(16, null, r,    null);}});}  // Used ?
  void or          (int r1, int r2) {C.push(new I("or"                ) {void i() { R[r1]   |= R[r2];           }  void lid() {boxes(17, r2,   r1,   null);}});}
  void putb        (int r)          {C.push(new I("putb"              ) {void i() { putB(r);                    }  void lid() {boxes(18, null, r,    null);}});}
  void putd        (int r)          {C.push(new I("putd"              ) {void i() { putD(r);                    }  void lid() {boxes(19, null, r,    null);}});}
  void putx        (int r)          {C.push(new I("putx"              ) {void i() { putX(r);                    }  void lid() {boxes(20, null, r,    null);}});}
  void sll         (int r,  int c)  {C.push(new I("sll"               ) {void i() { shiftLeft(r, c);            }  void lid() {boxes(21, null, r,    c   );}});}
  void strd        (int r,  int c)  {C.push(new I("strd"              ) {void i() { D[c]     = R[r];            }  void lid() {boxes(22, null, r,    c   );}});}
  void stri        (int r1, int r2) {C.push(new I("stri"              ) {void i() { D[R[r2]] = R[r1];           }  void lid() {boxes(23, r2,   r1,   null);}});}
  void srl         (int r,  int c)  {C.push(new I("srl"               ) {void i() { shiftRight(r, c);           }  void lid() {boxes(24, null, r,    c   );}});}
  void stop        ()               {C.push(new I("stop"              ) {void i() {                             }  void lid() {boxes(25, null, null, null);}});}
  void sub         (int r1, int r2) {C.push(new I("sub"               ) {void i() { R[r1]   -= R[r2];           }  void lid() {boxes(26, r2,   r1,   null);}});}  // Used ?

  int  label       ()               {C.push(new I("label", null, ++LabelCounter)  {                                void lid() {boxes(27, null, null, LabelCounter);}}); return LabelCounter;}  // Create and set a label
  int  label       (int label)      {C.push(new I("label", null, label)           {                                void lid() {boxes(27, null, null, label)       ;}}); return label       ;}  // Set a label
  int  newLabel    ()               {return ++LabelCounter;}                    // Create a new label that can be set later using the label(int label) method

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
   {ldrc(a, 0xff);                                                              // Loop variable in a
    int loop = label();                                                         // For a..0
      stri(a, a);                                                               //   D[a] = a
      dec(a);                                                                   //   Decrement loop variable
      jumpBckIfGt0(a, loop);                                                    // end_for
    stop();
  }

  public void programIf()
   {ldrc        (a, 15);                                                        // Loop this many times
    int loop = label();                                                         // For a..0
      ldrr      (b,  a);                                                        //   Test a for even/odd
      sll       (b, 15);
      srl       (b, 15);
      int endIf = newLabel();
      jumpFwdif0(b, endIf);                                                     //   if even
        stri    (a, a);                                                         //     D[a] = a
        putd    (a);                                                            //     put a
      label     (endIf);                                                        //   end_if
      dec(a);                                                                   //   Decrement loop variable
      jumpBckIfGt0(a, loop);                                                    // end_for
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

  void parityCheckBit  (int b)                                                  // Bit 7:0 of byte being tested
   {ldrc(m, 1);                                                                 // m = mask
    sll (m, b);                                                                 // Mask bit into position
    and (m, o);                                                                 // And source byte with mask and overwrite mask
    int endIf = newLabel();
    jumpFwdif0 (m, endIf);                                                      // if (bit is one)
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
      sll (b, 8);                                                               //   b = high byte in high position
      dec (a);
      ldri(c, a);                                                               //   c = value of low byte
      or  (b, c);                                                               //   b = [28, 29]

      ldrr(c, b);                                                               //   c = 7    6    5    4    3    2     1    0
      sll (c, 5);                                                               //   c = b11  b10  b_9  b_8  b_7  b_6   b_5  0
      srl (c, 9);                                                               //   .
      sll (c, 1);                                                               //   .

      ldrr(d, b);                                                               //   d = 7    6    5    4    3    2     1    0
      sll (d,  8);                                                              //   d = b_4  b_3  b_2  0    0    0     0    0
      srl (d, 13);                                                              //   .
      sll (d,  5);                                                              //   .

      ldrr(e, b);                                                               //   e = 7    6    5    4    3    2     1    0
      sll (e, 15);                                                              //   e = 0    0    0    0    b_1  0     0    0
      srl (e, 12);                                                              //   .

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
      parityCheckBits(7, 6, 3, 2);
      parityCheckContinue(d);
      parityCheckBits(7, 6);
      parityCheckContinue(e);
      parityCheckBits(3);
      parityCheckEnd(h);

      parityCheckStart(c);                                                      // i = p1 = 1
      parityCheckBits(7, 5, 3, 1);
      parityCheckContinue(d);
      parityCheckBits(7, 5);
      parityCheckContinue(e);
      parityCheckBits(3);
      parityCheckEnd(i);
//
// output
//   c = MSW = b11 b10 b9 b8 b7 b6 b5 p8
//   d = SW = b4 b3 b2 p4 b1 p2 p1 p16, where px denotes a parity bit
//   f = p16 = ^(b11:1,p8,p4,p2,p1) = 0;

      or   (c, f);                                                              // Format c and d with the expanded messages

      sll  (g, 4);
      or   (d, g);

      or   (d, e);

      sll  (h, 2);
      or   (d, h);

      sll  (i, 1);
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

      jumpBckIfGt0(a, loop);                                                    // end_for
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

  public  void testProgram1()                                                   // Test program 1
   {initialize("Program 1");
    Program1();
    D[28] = 0x55;                                                               // mem[1] = 00000101
    D[29] = 0x05;                                                               // mem[0] = 01010101
    run();
    assert(D[28] == 0x55);
    assert(D[29] == 0x05);
    assert(D[58] == 0x5a);
    assert(D[59] == 0xaa);
    Count++;
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

  public static void exit()                                                     // Exit
   {System.exit(0);
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
    for(int i = 0; i < a.length; ++i) s += " "+a[i];
    if (s.length() > 0) say(s.substring(1, s.length()));
   }

  public static void main(String args[])                                        // Tests
   {final Machine2 m = new Machine2();
    Trace = true; Dump = true; LowLevelCode = true;                             // Options
    m.test1to3();                                                               // Structured programming tests
    m.testFor();
    m.testIf();
    m.testProgram1();                                                           // Program 1 from the term paper
    say("Passes: ", Count, " Tests");                                           // Passing test
   }
}
