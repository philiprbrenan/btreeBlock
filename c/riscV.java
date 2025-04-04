//------------------------------------------------------------------------------
// Java RiscV assembler representation of the btree algorithm coded in c
// Philip R Brenan at gmail dot com, Appa Apps Ltd. Inc., 2025
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, layout and simulate a btree in a block on the surface of a silicon chip.

import java.util.*;

class RiscV extends Test                                                        // Execute the RiscV assembler version of the btree algorithm
 {final int x0  =  0, zero = 0;
  final int x1  =  1, ra  =  1;
  final int x2  =  2, sp  =  2;
  final int x3  =  3, gp  =  3;
  final int x4  =  4, tp  =  4;
  final int x5  =  5, t0  =  5;
  final int x6  =  6, t1  =  6;
  final int x7  =  7, t2  =  7;
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
  final int x18 = 18, s2  = 18;
  final int x19 = 19, s3  = 19;
  final int x20 = 20, s4  = 20;
  final int x21 = 21, s5  = 21;
  final int x22 = 22, s6  = 22;
  final int x23 = 23, s7  = 23;
  final int x24 = 24, s8  = 24;
  final int x25 = 25, s9  = 25;
  final int x26 = 26, s10 = 26;
  final int x27 = 27, s11 = 27;
  final int x28 = 28, t3  = 28;
  final int x29 = 29, t4  = 29;
  final int x30 = 30, t5  = 30;
  final int x31 = 31, t6  = 31;

  final int    [] R = new int [32];                                             // Registers
  final byte   [] M = new byte[powerTwo(20)];                                   // Memory
  final ProgramDM P = new ProgramDM();                                          // Program
  int     calldepth = 0;                                                        // Current call depth

  void add  (int t, int s1,     int     s2)                             {P.new I() {String v() {return "add  ";} void a() {R[t] = R[s1] + R[s2];}};}
  void addi (int t, int s1,     int     s2)                             {P.new I() {String v() {return "addi ";} void a() {R[t] = R[s1] +   s2; }};}
  void addi (int t, int s1,     boolean s2, ProgramDM.Label s3)         {P.new I() {String v() {return "addi ";} void a() {R[t] =  s2 ? s3.get()>>20 : s3.get();}};}
  void addi (int t, int s1,     boolean s2, ProgramDM.Label s3, int s4) {P.new I() {String v() {return "addi ";} void a() {R[t] = (s2 ? s3.get()>>20 : s3.get()) + R[s4];}};}
  void addw (int t, int s1,     int     s2)                             {P.new I() {String v() {return "addw ";} void a() {R[t] = R[s1] + R[s2];}};}
  void addiw(int t, int s1,     int     s2)                             {P.new I() {String v() {return "addiw";} void a() {R[t] = R[s1] +   s2;}};}

  void and  (int t, int s1,     int     s2)                             {P.new I() {String v() {return "and  ";} void a() {R[t] = R[s1] & R[s2];}};}
  void andi (int t, int s1,     int     s2)                             {P.new I() {String v() {return "andi ";} void a() {R[t] = R[s1] &   s2; }};}

  void bleu (int t, int s1,     ProgramDM.Label s2)                     {P.new I() {String v() {return "bleu ";} void a() {if (R[t] <= R[s1]) P.step = s2.get1();}};}
  void bltu (int t, int s1,     ProgramDM.Label s2)                     {P.new I() {String v() {return "bltu ";} void a() {if (R[t] <  R[s1]) P.step = s2.get1();}};}
  void bgtu (int t, int s1,     ProgramDM.Label s2)                     {P.new I() {String v() {return "bgtu ";} void a() {if (R[t] >  R[s1]) P.step = s2.get1();}};}
  void beq  (int t, int s1,     ProgramDM.Label s2)                     {P.new I() {String v() {return "beq  ";} void a() {if (R[t] == R[s1]) P.step = s2.get1();}};}
  void bge  (int t, int s1,     ProgramDM.Label s2)                     {P.new I() {String v() {return "bge  ";} void a() {if (R[t] >= R[s1]) P.step = s2.get1();}};}
  void bgt  (int t, int s1,     ProgramDM.Label s2)                     {P.new I() {String v() {return "bgt  ";} void a() {if (R[t] >  R[s1]) P.step = s2.get1();}};}
  void ble  (int t, int s1,     ProgramDM.Label s2)                     {P.new I() {String v() {return "ble  ";} void a() {if (R[t] <= R[s1]) P.step = s2.get1();}};}
  void blt  (int t, int s1,     ProgramDM.Label s2)                     {P.new I() {String v() {return "blt  ";} void a() {if (R[t] <  R[s1]) P.step = s2.get1();}};}
  void bne  (int t, int s1,     ProgramDM.Label s2)                     {P.new I() {String v() {return "bne  ";} void a() {if (R[t] <  R[s1]) P.step = s2.get1();}};}

  void call (ProgramDM.Label t)                                         {P.new I() {String v() {return "call ";} void a() {R[ra] = P.step+1; P.step = t.get1();}};}
  void j    (ProgramDM.Label t)                                         {P.new I() {String v() {return "j    ";} void a() {P.step = t.get1();}};}
  void jr   (int t)                                                     {P.new I() {String v() {return "jr   ";} void a() {P.step = R[t]-1;}};}

  void lb   (int t, int     s1, int     s2)                             {P.new I() {String v() {return "lb   ";} void a() {R[t] =  M[s1  + R[s2]];                         }};}
  void lbu  (int t, int     s1, int     s2)                             {P.new I() {String v() {return "lbu  ";} void a() {R[t] =  M[s1  + R[s2]];                         }};}
  void ld   (int t, boolean s1, int     s2)                             {P.new I() {String v() {return "ld   ";} void a() {R[t] =  s1 ? R[s2]>>20 : R[s2];                 }};}
  void ld   (int t, int     s1, int     s2)                             {P.new I() {String v() {return "ld   ";} void a() {R[t] =  R[s1] +   s2;                           }};}
  void li   (int t, int s1)                                             {P.new I() {String v() {return "li   ";} void a() {R[t] =    s1;                                   }};}
  void lw   (int t, int s1, int s2)                                     {P.new I() {String v() {return "lw   ";} void a() {R[t] =    s1  + R[s2];                          }};}
  void lw   (int t, boolean s1, ProgramDM.Label s2, int s3)             {P.new I() {String v() {return "lw   ";} void a() {R[t] =  s1 ? s2.get()>>20 : s2.get();           }};}
  void lw   (int t, boolean s1, ProgramDM.Label s2, int s3, int s4)     {P.new I() {String v() {return "lw   ";} void a() {R[t] = (s1 ? s2.get()>>20 : s2.get())+R[s3]+s4; }};}
  void lui  (int t, boolean s1, int     s2)                             {P.new I() {String v() {return "lui  ";} void a() {R[t] =  s1 ? s2>>20 : s2;                       }};}
  void lui  (int t, boolean s1, ProgramDM.Label s2)                     {P.new I() {String v() {return "lui  ";} void a() {R[t] =  s1 ? s2.get()>>20 : s2.get();           }};}
  void lui  (int t, boolean s1, ProgramDM.Label s2, int s3)             {P.new I() {String v() {return "lui  ";} void a() {R[t] = (s1 ? s2.get()>>20 : s2.get()) + R[s3];  }};}
  void mul  (int t, int s1,     int     s2)                             {P.new I() {String v() {return "mul  ";} void a() {R[t] =  R[s1] * R[s2];                          }};}
  void mulw (int t, int s1,     int     s2)                             {P.new I() {String v() {return "mulw ";} void a() {R[t] =  R[s1] * R[s2];                          }};}
  void mv   (int t, int s1)                                             {P.new I() {String v() {return "mv   ";} void a() {R[t] =  R[s1];                                  }};}
  void neg  (int t, int s1)                                             {P.new I() {String v() {return "neg  ";} void a() {R[t] = -R[s1];                                  }};}
  void nop  ()                                                          {P.new I() {String v() {return "nop  ";} void a() {}};}
  void or   (int t, int s1,     int     s2)                             {P.new I() {String v() {return "or   ";} void a() {R[t] =  R[s1] | R[s2];                          }};}
  void ori  (int t, int s1,     int     s2)                             {P.new I() {String v() {return "ori  ";} void a() {R[t] =  R[s1] |   s2;                           }};}
  void ret  ()                                                          {P.new I() {String v() {return "ret  ";} void a() {P.step = R[ra];}};}
  void sb   (int t, int s1,     int     s2)                             {P.new I() {String v() {return "sb   ";} void a() {final int i =  s1 + R[s2]; M[t] = (byte)i;      }};}
  void sbu  (int t, int s1,     int     s2)                             {P.new I() {String v() {return "sbu  ";} void a() {final int i =  s1 + R[s2]; M[t] = (byte)i;      }};}
  void sd   (int t, int s1,     int     s2)                             {P.new I() {String v() {return "sd   ";} void a() {final int i =  s1 + R[s2];                             M[t] = (byte) i; M[t+1] = (byte)(i >> Byte.SIZE); M[t+2] = (byte)(i >> (2*Byte.SIZE)); M[t+3] = (byte)(i >> (3 * Byte.SIZE)); M[t+4] = (byte) (i >> (4 * Byte.SIZE)); M[t+5] = (byte)(i >> (5 * Byte.SIZE)); M[t+6] = (byte)(i >> (6*Byte.SIZE)); M[t+7] = (byte)(i >> (7 * Byte.SIZE));}};}
  void sd   (int t, boolean s1, ProgramDM.Label s2, int s3)             {P.new I() {String v() {return "sd   ";} void a() {final int i = (s1 ? s2.get()>>20 : s2.get())+R[s3];    M[t] = (byte) i; M[t+1] = (byte)(i >> Byte.SIZE); M[t+2] = (byte)(i >> (2*Byte.SIZE)); M[t+3] = (byte)(i >> (3 * Byte.SIZE)); M[t+4] = (byte) (i >> (4 * Byte.SIZE)); M[t+5] = (byte)(i >> (5 * Byte.SIZE)); M[t+6] = (byte)(i >> (6*Byte.SIZE)); M[t+7] = (byte)(i >> (7 * Byte.SIZE));}};}
  void seqz (int t, int s1)                                             {P.new I() {String v() {return "seqz ";} void a() {R[t] =  R[s2] == 0 ? 1 : 0;                     }};}
  void sextw(int t, int s1)                                             {P.new I() {String v() {return "sextw";} void a() {R[t] =  R[s1];                                  }};}
  void sgt  (int t, int s1,     int     s2)                             {P.new I() {String v() {return "sgt  ";} void a() {R[t] =  R[s1] >   s2 ? 1 : 0;                   }};}
  void slli (int t, int s1,     int     s2)                             {P.new I() {String v() {return "slli ";} void a() {R[t] =  R[s1] <<  s2;                           }};}
  void srli (int t, int s1,     int     s2)                             {P.new I() {String v() {return "srli ";} void a() {R[t] =  R[s1] >>  s2;                           }};}
  void srliw(int t, int s1,     int     s2)                             {P.new I() {String v() {return "srliw";} void a() {R[t] =  R[s1] + R[s2];                          }};}
  void sraiw(int t, int s1,     int     s2)                             {P.new I() {String v() {return "sraiw";} void a() {R[t] =  R[s1] + R[s2];                          }};}
  void sub  (int t, int s1,     int     s2)                             {P.new I() {String v() {return "sub  ";} void a() {R[t] =  R[s1] + R[s2];                          }};}
  void subw (int t, int s1,     int     s2)                             {P.new I() {String v() {return "subw ";} void a() {R[t] =  R[s1] + R[s2];                          }};}
  void sw   (int t, int s1,     int     s2)                             {P.new I() {String v() {return "sw   ";} void a() {final int i =  s1 + R[s2];                             M[t] = (byte) i; M[t+1] = (byte)(i >> Byte.SIZE); M[t+2] = (byte)(i >> (2*Byte.SIZE)); M[t+3] = (byte)(i >> (3 * Byte.SIZE));}};}
  void sw   (int t, boolean s1, ProgramDM.Label s2, int s3)             {P.new I() {String v() {return "sw   ";} void a() {final int i = (s1 ? s2.get()>>20 : s2.get())+R[s3];    M[t] = (byte) i; M[t+1] = (byte)(i >> Byte.SIZE); M[t+2] = (byte)(i >> (2*Byte.SIZE)); M[t+3] = (byte)(i >> (3 * Byte.SIZE));}};}
  void sw   (int t, boolean s1, ProgramDM.Label s2, int s3, int s4)     {P.new I() {String v() {return "sw   ";} void a() {final int i = (s1 ? s2.get()>>20 : s2.get())+R[s3]+s4; M[t] = (byte) i; M[t+1] = (byte)(i >> Byte.SIZE); M[t+2] = (byte)(i >> (2*Byte.SIZE)); M[t+3] = (byte)(i >> (3 * Byte.SIZE));}};}
  void tail (ProgramDM.Label t)                                         {P.new I() {String v() {return "tail ";} void a() {}};}

  RiscV()
   {R[sp] = M.length;                                                           // Set the stack pointer
   }

//D0 Tests                                                                      // Testing

//  final static int[]random_small = {27, 442, 545, 317, 511, 578, 391, 993, 858, 586, 472, 906, 658, 704, 882, 246, 261, 501, 354, 903, 854, 279, 526, 686, 987, 403, 401, 989, 650, 576, 436, 560, 806, 554, 422, 298, 425, 912, 503, 611, 135, 447, 344, 338, 39, 804, 976, 186, 234, 106, 667, 494, 690, 480, 288, 151, 773, 769, 260, 809, 438, 237, 516, 29, 376, 72, 946, 103, 961, 55, 358, 232, 229, 90, 155, 657, 681, 43, 907, 564, 377, 615, 612, 157, 922, 272, 490, 679, 830, 839, 437, 826, 577, 937, 884, 13, 96, 273, 1, 188};
//  final static int[]random_large = {5918,5624,2514,4291,1791,5109,7993,60,1345,2705,5849,1034,2085,4208,4590,7740,9367,6582,4178,5578,1120,378,7120,8646,5112,4903,1482,8005,3801,5439,4534,9524,6111,204,5459,248,4284,8037,5369,7334,3384,5193,2847,1660,5605,7371,3430,1786,1216,4282,2146,1969,7236,2187,136,2726,9480,5,4515,6082,969,5017,7809,9321,3826,9179,5781,3351,4819,4545,8607,4146,6682,1043,2890,2964,7472,9405,4348,8333,2915,9674,7225,4743,995,1321,3885,6061,9958,3901,4710,4185,4776,5070,8892,8506,6988,2317,9342,3764,9859,4724,5195,673,359,9740,2089,9942,3749,9208,1,7446,7023,5496,4206,3272,3527,8593,809,3149,4173,9605,9021,5120,5265,7121,8667,6911,4717,2535,2743,1289,1494,3788,6380,9366,2732,1501,8543,8013,5612,2393,7041,3350,3204,288,7213,1741,1238,9830,6722,4687,6758,8067,4443,5013,5374,6986,282,6762,192,340,5075,6970,7723,5913,1060,1641,1495,5738,1618,157,6891,173,7535,4952,9166,8950,8680,1974,5466,2383,3387,3392,2188,3140,6806,3131,6237,6249,7952,1114,9017,4285,7193,3191,3763,9087,7284,9170,6116,3717,6695,6538,6165,6449,8960,2897,6814,3283,6600,6151,4624,3992,5860,9557,1884,5585,2966,1061,6414,2431,9543,6654,7417,2617,878,8848,8241,3790,3370,8768,1694,9875,9882,8802,7072,3772,2689,5301,7921,7774,1614,494,2338,8638,4161,4523,5709,4305,17,9626,843,9284,3492,7755,5525,4423,9718,2237,7401,2686,8751,1585,5919,9444,3271,1490,7004,5980,3904,370,5930,6304,7737,93,5941,9079,4968,9266,262,2766,4999,2450,9518,5137,8405,483,8840,2231,700,8049,8823,9811,9378,3811,8074,153,1940,1998,4354,7830,7086,6132,9967,5680,448,1976,4101,7839,3122,4379,9296,4881,1246,4334,9457,5401,1945,9548,8290,1184,3464,132,2458,7704,1056,7554,6203,2270,6070,4889,7369,1676,485,3648,357,1912,9661,4246,1576,1836,4521,7667,6907,2098,8825,7404,4019,8284,3710,7202,7050,9870,3348,3624,9224,6601,7897,6288,3713,932,5596,353,2615,3273,833,1446,8624,2489,3872,486,1091,2493,4157,3611,6570,7107,9153,4543,9504,4746,1342,9737,3247,8984,3640,5698,7814,307,8775,1150,4330,3059,5784,2370,5248,4806,6107,9700,231,3566,5627,3957,5317,5415,8119,2588,9440,2961,9786,4769,466,5411,3080,7623,5031,2378,9286,4801,797,1527,2325,847,6341,5310,1926,9481,2115,2165,5255,5465,5561,3606,7673,7443,7243,8447,2348,7925,6447,8311,6729,4441,7763,8107,267,8135,9194,6775,3883,9639,612,5024,1351,7557,9241,5181,2239,8002,5446,747,166,325,9925,3820,9531,5163,3545,558,7103,7658,5670,8323,4821,6263,7982,59,3700,1082,4474,4353,8637,9558,5191,842,5925,6455,4092,9929,9961,290,3523,6290,7787,8266,7986,7269,6408,3620,406,5964,7289,1620,6726,1257,1993,7006,5545,2913,5093,5066,3019,7081,6760,6779,7061,9051,8852,8118,2340,6596,4594,9708,8430,8659,8920,9268,5431,9203,2823,1427,2203,6422,6193,5214,9566,8791,4964,7575,4350,56,2227,8545,5646,3089,2204,4081,487,8496,2258,4336,6955,3452,556,8602,8251,8569,8636,9430,1025,9459,7137,8392,3553,5945,9414,3078,1688,5480,327,8117,2289,2195,8564,9423,103,7724,3091,8548,7298,5279,6042,2855,3286,3542,9361,420,7020,4112,5320,5366,6379,114,9174,9744,592,5346,3985,3174,5157,9890,1605,3082,8099,4346,7256,8670,5687,6613,6620,1458,1045,7917,2980,2399,1433,3315,4084,178,7056,2132,2728,4421,9195,4181,6017,6229,2945,4627,2809,8816,6737,18,8981,3813,8890,5304,3789,6959,7476,1856,4197,6944,9578,5915,3060,9932,3463,67,7393,9857,5822,3187,501,653,8453,3691,9736,6845,1365,9645,4120,2157,8471,4436,6435,2758,7591,9805,7142,7612,4891,7342,5764,8683,8365,2967,6947,441,2116,6612,1399,7585,972,6548,5481,7733,7209,222,5903,6161,9172,9628,7348,1588,5992,6094,7176,4214,8702,2987,74,8486,9788,7164,5788,8535,8422,6826,1800,8965,4965,565,5609,4686,2556,9324,5000,9809,1994,4737,63,8992,4783,2536,4462,8868,6346,5553,3980,2670,1601,4272,8725,4698,7333,7826,9233,4198,1997,1687,4851,62,7893,8149,8015,341,2230,1280,5559,9756,3761,7834,6805,9287,4622,5748,2320,1958,9129,9649,1644,4323,5096,9490,7529,6444,7478,7044,9525,7713,234,7553,9099,9885,7135,6493,9793,6268,8363,2267,9157,9451,1438,9292,1637,3739,695,1090,4731,4549,5171,5975,7347,5192,5243,1084,2216,9860,3318,5594,5790,1107,220,9397,3378,1353,4498,6497,5442,7929,7377,9541,9871,9895,6742,9146,9409,292,6278,50,5288,2217,4923,6790,4730,9240,3006,3547,9347,7863,4275,3287,2673,7485,1915,9837,2931,3918,635,9131,1197,6250,3853,4303,790,5548,9993,3702,2446,3862,9652,4432,973,41,3507,8585,2444,1633,956,5789,1523,8657,4869,8580,8474,7093,7812,2549,7363,9315,6731,1130,7645,7018,7852,362,1636,2905,8006,4040,6643,8052,7021,3665,8383,715,1876,2783,3065,604,4566,8761,7911,1983,3836,5547,8495,8144,1950,2537,8575,640,8730,8303,1454,8165,6647,4762,909,9449,8640,9253,7293,8767,3004,4623,6862,8994,2520,1215,6299,8414,2576,6148,1510,313,3693,9843,8757,5774,8871,8061,8832,5573,5275,9452,1248,228,9749,2730};

// Generated by Classify instructions

  static void test_btree()                                                      // RiscV code for btree algorithm
   {RiscV r = new RiscV();
    r.test_btree1();
    r.test_btree2();
    r.P.start = r.$put.get();
    r.R[r.ra] = -1;                                                             // Return address
    r.R[r.a0] =  1;                                                             // Key
    r.R[r.a1] =  1;                                                             // Data
    r.R[r.sp] = r.M.length;                                                     // Stack pointer
    r.P.debug = true;
    r.P.maxSteps = 160;
    r.P.run();
   }

RiscV

  static void oldTests()                                                        // Tests thought to be in good shar.
   {test_btree();
   }

  static void newTests()                                                        // Tests being worked on
   {oldTests();
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
