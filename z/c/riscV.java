//------------------------------------------------------------------------------
// Java RiscV assembler representation of the btree algorithm in c via gcc
// Philip R Brenan at gmail dot com, Appa Apps Ltd. Inc., 2025
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, layout and simulate a btree in a block on the surface of a silicon chip.

import java.util.*;

class RiscV extends Test                                                        // Execute the RiscV assembler version of the btree algorithm
 {boolean traceCalls = false;                                                   // Trace subroutine calls if true
  final int x0  =  0, zero = 0;
  final int x1  =  1, ra  =  1;
  final int x2  =  2, sp  =  2;
//final int x3  =  3, gp  =  3;
//final int x4  =  4, tp  =  4;
//final int x5  =  5, t0  =  5;
//final int x6  =  6, t1  =  6;
//final int x7  =  7, t2  =  7;
  final int x8  =  8, s0  =  8;
  final int x9  =  9, s1  =  9;
  final int x10 = 10, a0  = 10;
  final int x11 = 11, a1  = 11;
  final int x12 = 12, a2  = 12;
  final int x13 = 13, a3  = 13;
  final int x14 = 14, a4  = 14;
  final int x15 = 15, a5  = 15;
  final int x16 = 16, a6  = 16;
  final int x17 = 17, a7  = 17;
//final int x18 = 18, s2  = 18;
//final int x19 = 19, s3  = 19;
//final int x20 = 20, s4  = 20;
//final int x21 = 21, s5  = 21;
//final int x22 = 22, s6  = 22;
//final int x23 = 23, s7  = 23;
//final int x24 = 24, s8  = 24;
//final int x25 = 25, s9  = 25;
//final int x26 = 26, s10 = 26;
//final int x27 = 27, s11 = 27;
//final int x28 = 28, t3  = 28;
//final int x29 = 29, t4  = 29;
//final int x30 = 30, t5  = 30;
//final int x31 = 31, t6  = 31;

  final int    [] R = new int [32];                                             // Registers
  final int    [] M = new int [powerTwo(20)];                                   // Memory: each location represents one byte. The locations are presensted as integers because java has no unsigned byte
  final ProgramDM P = new ProgramDM()                                           // Program
   {void traceInstruction(ProgramDM.I i)                                        // Dump registers
     {if (!debug) return;
      final StringBuilder s = new StringBuilder();
      s.append(String.format(" %6d  %6d  %-8s ", step, steps, i.v()));

      s.append(String.format(" %8d=ra", R[ra]));
      s.append(String.format(" %8d=sp", R[sp]));
    //s.append(String.format(" %8d=gp", R[gp]));
    //s.append(String.format(" %8d=tp", R[tp]));
    //s.append(String.format(" %8d=t0", R[t0]));
    //s.append(String.format(" %8d=t1", R[t1]));
    //s.append(String.format(" %8d=t2", R[t2]));
      s.append(String.format(" %8d=s0", R[s0]));
      s.append(String.format(" %8d=s1", R[s1]));
      s.append(String.format(" %8d=a0", R[a0]));
      s.append(String.format(" %8d=a1", R[a1]));
      s.append(String.format(" %8d=a2", R[a2]));
      s.append(String.format(" %8d=a3", R[a3]));
      s.append(String.format(" %8d=a4", R[a4]));
      s.append(String.format(" %8d=a5", R[a5]));
      s.append(String.format(" %8d=a6", R[a6]));
      s.append(String.format(" %8d=a7", R[a7]));
    //s.append(String.format(" %8d=s2", R[s2]));
    //s.append(String.format(" %8d=s3", R[s3]));
    //s.append(String.format(" %8d=s4", R[s4]));
    //s.append(String.format(" %8d=s5", R[s5]));
    //s.append(String.format(" %8d=s6", R[s6]));
    //s.append(String.format(" %8d=s7", R[s7]));
    //s.append(String.format(" %8d=s8", R[s8]));
    //s.append(String.format(" %8d=s9", R[s9]));
    //s.append(String.format(" %8d=s10",R[s10]));
    //s.append(String.format(" %8d=s11",R[s11]));
    //s.append(String.format(" %8d=t3", R[t3]));
    //s.append(String.format(" %8d=t4", R[t4]));
    //s.append(String.format(" %8d=t5", R[t5]));
    //s.append(String.format(" %8d=t6", R[t6]));
      say(""+s);
     }
   };
  int callDepth = 0;                                                            // Current call depth
  final int  bs = 8;                                                            // Number of bits in a byte
  final int ubs = 0xFFF;                                                        // Upper bits

  int getB(int i) {return M[i] & 0xFF;}

  int getW(int i)
   {long a   = M[i], b   = M[i+1], c   = M[i+2], d   = M[i+3];
         a &= 0xFF;  b  &= 0xFF;   c  &= 0xFF;   d  &= 0xFF;
                     b <<= (1*bs); c <<= (2*bs); d <<= (3*bs);
    final long s = a | b | c | d;
    return (int)s;
   }

  int getD(int i) {return getW(i);}

  void setB(int i, int v) {M[i] = v;}

  void setW(int i, int v)
   {int a  = v >> (0*bs), b  = v >> (1*bs), c  = v >> (2*bs), d  = v >> (3*bs);
        a &= 0xFF;        b &= 0xFF;        c &= 0xFF;        d &= 0xFF;
    setB(i+0, a); setB(i+1, b); setB(i+2, c); setB(i+3, d);
   }

  void setD(int i,int v)
   {setW(i, v);
    final int s = v < 0 ? -1 : 0;                                               // Sign
    setB(i+4, s); setB(i+5, s); setB(i+6, s); setB(i+7, s);                     // Java only has signed types and weird >> and << operators so this is the best we can do while using ints
   }

  void add  (int t, int s1,     int     s2)                             {P.new I() {String v() {return "add"  ;} void a() {R[t] = R[s1] + R[s2];                         }};}
  void addi (int t, int s1,     int     s2)                             {P.new I() {String v() {return "addi" ;} void a() {R[t] = R[s1] +   s2;                          }};}
  void addi (int t, int s1,     boolean lo, ProgramDM.Label s2)         {P.new I() {String v() {return "addi" ;} void a() {                                              }};}
  void addi (int t, int s1,     boolean lo, int             s2)         {P.new I() {String v() {return "addi" ;} void a() {                                              }};}
  void addw (int t, int s1,     int     s2)                             {P.new I() {String v() {return "addw" ;} void a() {R[t] = R[s1] + R[s2];                         }};}
  void addiw(int t, int s1,     int     s2)                             {P.new I() {String v() {return "addiw";} void a() {R[t] = R[s1] +   s2;                          }};}

  void and  (int t, int s1,     int     s2)                             {P.new I() {String v() {return "and"  ;} void a() {R[t] = R[s1] & R[s2];                         }};}
  void andi (int t, int s1,     int     s2)                             {P.new I() {String v() {return "andi" ;} void a() {R[t] = R[s1] &   s2;                          }};}

  void bleu (int t, int s1,     ProgramDM.Label s2)                     {P.new I() {String v() {return "bleu" ;} void a() {if (R[t] <= R[s1]) P.step = s2.get1();        }};}
  void bltu (int t, int s1,     ProgramDM.Label s2)                     {P.new I() {String v() {return "bltu" ;} void a() {if (R[t] <  R[s1]) P.step = s2.get1();        }};}
  void bgtu (int t, int s1,     ProgramDM.Label s2)                     {P.new I() {String v() {return "bgtu" ;} void a() {if (R[t] >  R[s1]) P.step = s2.get1();        }};}
  void beq  (int t, int s1,     ProgramDM.Label s2)                     {P.new I() {String v() {return "beq"  ;} void a() {if (R[t] == R[s1]) P.step = s2.get1();        }};}
  void bge  (int t, int s1,     ProgramDM.Label s2)                     {P.new I() {String v() {return "bge"  ;} void a() {if (R[t] >= R[s1]) P.step = s2.get1();        }};}
  void bgt  (int t, int s1,     ProgramDM.Label s2)                     {P.new I() {String v() {return "bgt"  ;} void a() {if (R[t] >  R[s1]) P.step = s2.get1();        }};}
  void ble  (int t, int s1,     ProgramDM.Label s2)                     {P.new I() {String v() {return "ble"  ;} void a() {if (R[t] <= R[s1]) P.step = s2.get1();        }};}
  void blt  (int t, int s1,     ProgramDM.Label s2)                     {P.new I() {String v() {return "blt"  ;} void a() {if (R[t] <  R[s1]) P.step = s2.get1();        }};}
  void bne  (int t, int s1,     ProgramDM.Label s2)                     {P.new I() {String v() {return "bne"  ;} void a() {if (R[t] != R[s1]) P.step = s2.get1();        }};}

  void call (ProgramDM.Label t)                                         {P.new I() {String v() {return "call" ;} void a() {R[ra] = P.step+1; P.step = t.get1(); ++callDepth; if (traceCalls) say(""  .repeat(callDepth),   callDepth, t.name);}};}
  void j    (ProgramDM.Label t)                                         {P.new I() {String v() {return "j"    ;} void a() {P.step = t.get1();}};}
  void jr   (int t)                                                     {P.new I() {String v() {return "jr"   ;} void a() {P.step = R[t]-1;                     --callDepth; if (traceCalls) say(""  .repeat(callDepth+1), callDepth," return", R[t]);}};}

  void lb   (int t, int     s1, int     s2)                             {P.new I() {String v() {return "lb"   ;} void a() {R[t] =  getB(s1 + R[s2]);       }};}
  void lbu  (int t, int     s1, int     s2)                             {P.new I() {String v() {return "lbu"  ;} void a() {R[t] =  getB(s1 + R[s2]);       }};}
  void ld   (int t, int     s1, int     s2)                             {P.new I() {String v() {return "ld"   ;} void a() {R[t] =  getD(R[s1] + s2);       }};}

  void li   (int t, int     s1)                                         {P.new I() {String v() {return "li"   ;} void a() {R[t] =  s1;                     }};}
  void lw   (int t, int     s1, int s2)                                 {P.new I() {String v() {return "lw"   ;} void a() {R[t] =  getW( s1 + R[s2]);      }};}
  void lui  (int t, boolean hi, ProgramDM.Label s2)                     {P.new I() {String v() {return "lui"  ;} void a() {R[t] =  s2.get();               }};}
  void lui  (int t, boolean hi, int             s2)                     {P.new I() {String v() {return "lui"  ;} void a() {R[t] =  s2;                     }};}
  void mul  (int t, int s1,     int     s2)                             {P.new I() {String v() {return "mul"  ;} void a() {R[t] =  R[s1] * R[s2];          }};}
  void mulw (int t, int s1,     int     s2)                             {P.new I() {String v() {return "mulw" ;} void a() {R[t] =  R[s1] * R[s2];          }};}
  void mv   (int t, int s1)                                             {P.new I() {String v() {return "mv"   ;} void a() {R[t] =  R[s1];                  }};}
  void neg  (int t, int s1)                                             {P.new I() {String v() {return "neg"  ;} void a() {R[t] = -R[s1];                  }};}
  void nop  ()                                                          {P.new I() {String v() {return "nop"  ;} void a() {}};}
  void or   (int t, int s1,     int     s2)                             {P.new I() {String v() {return "or"   ;} void a() {R[t] =  R[s1] | R[s2];          }};}
  void ori  (int t, int s1,     int     s2)                             {P.new I() {String v() {return "ori"  ;} void a() {R[t] =  R[s1] |   s2;           }};}
  void ret  ()                                                          {P.new I() {String v() {return "ret"  ;} void a() {P.step = R[ra];                 }};}

  void sb   (int t, int s1,     int     s2)                             {P.new I() {String v() {return "sb"   ;} void a() {setB(s1 + R[s2], R[t]);         }};}
  void sbu  (int t, int s1,     int     s2)                             {P.new I() {String v() {return "sbu"  ;} void a() {setB(s1 + R[s2], R[t]);         }};}
  void sd   (int t, int s1,     int     s2)                             {P.new I() {String v() {return "sd"   ;} void a() {setD(s1 + R[s2], R[t]);         }};}
  void seqz (int t, int s1)                                             {P.new I() {String v() {return "seqz" ;} void a() {R[t] =    R[s1] == 0   ? 1 : 0; }};}
  void sextw(int t, int s1) /* fix sign extend */                       {P.new I() {String v() {return "sextw";} void a() {R[t] =    R[s1];                }};}
  void sgt  (int t, int s1,     int     s2)                             {P.new I() {String v() {return "sgt"  ;} void a() {R[t] =    R[s1] >   s2 ? 1 : 0; }};}
  void slli (int t, int s1,     int     s2)                             {P.new I() {String v() {return "slli" ;} void a() {R[t] =    R[s1] <<  s2;         }};}
  void srli (int t, int s1,     int     s2)                             {P.new I() {String v() {return "srli" ;} void a() {R[t] =    R[s1] >>  s2;         }};}
  void srliw(int t, int s1,     int     s2)                             {P.new I() {String v() {return "srliw";} void a() {R[t] =    R[s1] + R[s2];        }};}
  void sraiw(int t, int s1,     int     s2)                             {P.new I() {String v() {return "sraiw";} void a() {R[t] =    R[s1] + R[s2];        }};}
  void sub  (int t, int s1,     int     s2)                             {P.new I() {String v() {return "sub"  ;} void a() {R[t] =    R[s1] + R[s2];        }};}
  void subw (int t, int s1,     int     s2)                             {P.new I() {String v() {return "subw" ;} void a() {R[t] =    R[s1] + R[s2];        }};}
  void sw   (int t, int s1,     int     s2)                             {P.new I() {String v() {return "sw"   ;} void a() {setW(s1+R[s2], R[t]);           }};}
  void tail (ProgramDM.Label t)                                         {P.new I() {String v() {return "tail" ;} void a() {}};}

  RiscV()
   {R[sp] = M.length;                                                           // Set the stack pointer
    for (int i = 0; i < R.length; i++) R[i] = 0;
    for (int i = 0; i < M.length; i++) M[i] = 0;
   }

  void initializeMemoryAndRegisters()                                           // Initialize memory and registers
   {for (int i = 0; i < R.length; i++) R[i] = 0;
    for (int i = 0; i < M.length; i++) M[i] = 0;
   }

//D0 Tests                                                                      // Testing

// Generated by Classify instructions

  void test_btree()                                                             // RiscV code for btree algorithm
   {test_btree1();
    test_btree2();
    initializeMemoryAndRegisters();
    R[ra] = -1;                                                                 // A return to this address shows that the program has finished
    R[sp] = M.length-64;                                                        // Stack pointer needs some space above it
    P.maxSteps = 99999;
    P.start = $create.get();                                                    // Create an empty tree
    //P.debug = true;
    //traceCalls = true;
    P.run();
    ok(P.steps, 53422);

    R[ra] = -1;                                                                 // A return to this address shows that the program has finished
    R[a0] =  1;                                                                 // Key
    R[a1] =  1;                                                                 // Data
    P.start = $put.get();
    P.debug = true;
    traceCalls = true;
    P.run();
   }

//RiscV

  void test_sw_lw()
   {sw(s1, -20, s0);
    mv(s1, zero);
    lw(s1, -20, s0);
    nop();

    R[ra] = -1;                                                                 // A return to this address shows that the program has finished
    R[s0] = 100;
    R[s1] = 0x44332211;
    P.maxSteps = 7;
    P.run();
    ok(R[s1], 0x44332211);
   }

  void test_lui_addi()
   {lui (s0,     true, 123456789);
    addi(s0, s0, true, 123456789);
    nop();

    R[ra] = -1;                                                                 // A return to this address shows that the program has finished
    P.maxSteps = 9;
    P.run();
    ok(R[s0], 123456789);
   }

  void test_instructions()                                                      // RiscV code instruction testing
   {addi(sp, sp, -32);
    sd(s0, 24, sp);
    mv(s0, zero);
    ld(s0, sp, 24);
    addi(sp, sp, 32);
    nop();

    R[ra] = -1;                                                                 // A return to this address shows that the program has finished
    R[s0] = -123456789;                                                         // Return address
    R[sp] = M.length;                                                           // Stack pointer
    P.maxSteps = 9;
    P.run();
    ok(R[s0], -123456789);
    ok(R[sp], M.length);
   }

  static void oldTests()                                                        // Tests thought to be in good shar.
   {//new RiscV().test_btree();
   }

  static void newTests()                                                        // Tests being worked on
   {//oldTests();
    new RiscV().test_sw_lw();
    new RiscV().test_lui_addi();
    new RiscV().test_instructions();
    new RiscV().test_btree();
   }

  public static void main(String[] args)                                        // Test if called as a r.ogram
   {try                                                                         // Get a traceback in a format clickable in Geany if something goes wrong to sr.ed ur.debugging.
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
