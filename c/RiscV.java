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
  final byte   [] S = new byte[powerTwo(20)];                                   // Stack
  final ProgramDM P = new ProgramDM();                                          // Program

  void add  (int t, int s1,     int     s2)                             {P.new I() {void a() {R[t] = R[s1] + R[s2];}};}
  void addi (int t, int s1,     int     s2)                             {P.new I() {void a() {R[t] = R[s1] +   s2; }};}
  void addi (int t, int s1,     boolean s2, ProgramDM.Label s3)         {P.new I() {void a() {R[t] =  s2 ? s3.get()>>20 : s3.get();}};}
  void addi (int t, int s1,     boolean s2, ProgramDM.Label s3, int s4) {P.new I() {void a() {R[t] = (s2 ? s3.get()>>20 : s3.get()) + R[s4];}};}
  void addiw(int t, int s1,     int     s2)                             {P.new I() {void a() {R[t] = R[s1] + R[s2];}};}
  void addw (int t, int s1,     int     s2)                             {P.new I() {void a() {R[t] = R[s1] +   s2;}};}

  void and  (int t, int s1,     int     s2)                             {P.new I() {void a() {R[t] = R[s1] & R[s2];}};}
  void andi (int t, int s1,     int     s2)                             {P.new I() {void a() {R[t] = R[s1] &   s2;}};}

  void bleu (int t, int s1,     ProgramDM.Label s2)                     {P.new I() {void a() {R[t] = R[s1] + s2.get();}};}
  void bltu (int t, int s1,     ProgramDM.Label s2)                     {P.new I() {void a() {R[t] = R[s1] + s2.get();}};}
  void bgtu (int t, int s1,     ProgramDM.Label s2)                     {P.new I() {void a() {R[t] = R[s1] + s2.get();}};}
  void beq  (int t, int s1,     ProgramDM.Label s2)                     {P.new I() {void a() {R[t] = R[s1] + s2.get();}};}
  void bge  (int t, int s1,     ProgramDM.Label s2)                     {P.new I() {void a() {R[t] = R[s1] + s2.get();}};}
  void bgt  (int t, int s1,     ProgramDM.Label s2)                     {P.new I() {void a() {R[t] = R[s1] + s2.get();}};}
  void ble  (int t, int s1,     ProgramDM.Label s2)                     {P.new I() {void a() {R[t] = R[s1] + s2.get();}};}
  void blt  (int t, int s1,     ProgramDM.Label s2)                     {P.new I() {void a() {R[t] = R[s1] + s2.get();}};}
  void bne  (int t, int s1,     ProgramDM.Label s2)                     {}

  void call (ProgramDM.Label t)                                         {}
  void j    (ProgramDM.Label t)                                         {}
  void jr   (int t)                                                     {}

  void lb   (int t, int     s1, int     s2)                             {P.new I() {void a() {R[t] =  M[s1  + R[s2]];}};}
  void lbu  (int t, int     s1, int     s2)                             {P.new I() {void a() {R[t] =  R[s1] + R[s2]; }};}
  void ld   (int t, boolean s1, int     s2)                             {P.new I() {void a() {R[t] =  s1 ? R[s2]>>20 : R[s2];}};}
  void ld   (int t, int     s1, int     s2)                             {P.new I() {void a() {R[t] =  R[s1] + R[s2]; }};}
  void li   (int t, int s1)                                             {P.new I() {void a() {R[t] =  R[s1] + R[s2]; }};}
  void lw   (int t, int s1, int s2)                                     {P.new I() {void a() {R[t] =  R[s1] + R[s2]; }};}
  void lw   (int t, boolean s1, ProgramDM.Label s2, int s3)             {P.new I() {void a() {R[t] =  s1 ? s2.get()>>20 : s2.get();     }};}
  void lw   (int t, boolean s1, ProgramDM.Label s2, int s3, int s4)     {P.new I() {void a() {R[t] = (s1 ? s2.get()>>20 : s2.get())+R[s3]+s4; }};}
  void lui  (int t, boolean s1, int     s2)                             {P.new I() {void a() {R[t] =  s1 ? s2>>20 : s2; }};}
  void lui  (int t, boolean s1, ProgramDM.Label s2)                     {P.new I() {void a() {R[t] =  s1 ? s2.get()>>20 : s2.get(); }};}
  void lui  (int t, boolean s1, ProgramDM.Label s2, int s3)             {P.new I() {void a() {R[t] = (s1 ? s2.get()>>20 : s2.get()) + R[s3]; }};}
  void mul  (int t, int s1,     int     s2)                             {P.new I() {void a() {R[t] =  R[s1] * R[s2]; }};}
  void mulw (int t, int s1,     int     s2)                             {P.new I() {void a() {R[t] =  R[s1] * R[s2]; }};}
  void mv   (int t, int s1)                                             {P.new I() {void a() {R[t] =  R[s1];}         };}
  void neg  (int t, int s1)                                             {P.new I() {void a() {R[t] = -R[s1];}        };}
  void nop  ()                                                          {P.new I() {void a() {}};}
  void or   (int t, int s1,     int     s2)                             {P.new I() {void a() {R[t] =  R[s1] | R[s2]; }};}
  void ori  (int t, int s1,     int     s2)                             {P.new I() {void a() {R[t] =  R[s1] |   s2;}  };}
  void ret  ()                                                          {P.new I() {void a() {}};}
  void sb   (int t, int s1,     int     s2)                             {P.new I() {void a() {final int i =  s1 + R[s2]; M[t] = (byte)i;}};}
  void sbu  (int t, int s1,     int     s2)                             {P.new I() {void a() {final int i =  s1 + R[s2]; M[t] = (byte)i;}};}
  void sd   (int t, int s1,     int     s2)                             {P.new I() {void a() {final int i =  s1 + R[s2];                             M[t] = (byte) i; M[t+1] = (byte)(i >> Byte.SIZE); M[t+2] = (byte)(i >> (2*Byte.SIZE)); M[t+3] = (byte)(i >> (3 * Byte.SIZE)); M[t+4] = (byte) (i >> (4 * Byte.SIZE)); M[t+5] = (byte)(i >> (5 * Byte.SIZE)); M[t+6] = (byte)(i >> (6*Byte.SIZE)); M[t+7] = (byte)(i >> (7 * Byte.SIZE));}};}
  void sd   (int t, boolean s1, ProgramDM.Label s2, int s3)             {P.new I() {void a() {final int i = (s1 ? s2.get()>>20 : s2.get())+R[s3];    M[t] = (byte) i; M[t+1] = (byte)(i >> Byte.SIZE); M[t+2] = (byte)(i >> (2*Byte.SIZE)); M[t+3] = (byte)(i >> (3 * Byte.SIZE)); M[t+4] = (byte) (i >> (4 * Byte.SIZE)); M[t+5] = (byte)(i >> (5 * Byte.SIZE)); M[t+6] = (byte)(i >> (6*Byte.SIZE)); M[t+7] = (byte)(i >> (7 * Byte.SIZE));}};}
  void seqz (int t, int s1)                                             {P.new I() {void a() {R[t] =  R[s2] == 0 ? 1 : 0;   }};}
  void sextw(int t, int s1)                                             {P.new I() {void a() {R[t] =  R[s1];                }};}
  void sgt  (int t, int s1,     int     s2)                             {P.new I() {void a() {R[t] =  R[s1] >   s2 ? 1 : 0; }};}
  void slli (int t, int s1,     int     s2)                             {P.new I() {void a() {R[t] =  R[s1] <<  s2;         }};}
  void srli (int t, int s1,     int     s2)                             {P.new I() {void a() {R[t] =  R[s1] >>  s2;         }};}
  void srliw(int t, int s1,     int     s2)                             {P.new I() {void a() {R[t] =  R[s1] + R[s2];        }};}
  void sraiw(int t, int s1,     int     s2)                             {P.new I() {void a() {R[t] =  R[s1] + R[s2];        }};}
  void sub  (int t, int s1,     int     s2)                             {P.new I() {void a() {R[t] =  R[s1] + R[s2];        }};}
  void subw (int t, int s1,     int     s2)                             {P.new I() {void a() {R[t] =  R[s1] + R[s2];        }};}
  void sw   (int t, int s1,     int     s2)                             {P.new I() {void a() {final int i =  s1 + R[s2];                             M[t] = (byte) i; M[t+1] = (byte)(i >> Byte.SIZE); M[t+2] = (byte)(i >> (2*Byte.SIZE)); M[t+3] = (byte)(i >> (3 * Byte.SIZE));}};}
  void sw   (int t, boolean s1, ProgramDM.Label s2, int s3)             {P.new I() {void a() {final int i = (s1 ? s2.get()>>20 : s2.get())+R[s3];    M[t] = (byte) i; M[t+1] = (byte)(i >> Byte.SIZE); M[t+2] = (byte)(i >> (2*Byte.SIZE)); M[t+3] = (byte)(i >> (3 * Byte.SIZE));}};}
  void sw   (int t, boolean s1, ProgramDM.Label s2, int s3, int s4)     {P.new I() {void a() {final int i = (s1 ? s2.get()>>20 : s2.get())+R[s3]+s4; M[t] = (byte) i; M[t+1] = (byte)(i >> Byte.SIZE); M[t+2] = (byte)(i >> (2*Byte.SIZE)); M[t+3] = (byte)(i >> (3 * Byte.SIZE));}};}
  void tail (ProgramDM.Label t)                                         {P.new I() {void a() {}};}

//D0 Tests                                                                      // Testing

  final static int[]random_small = {27, 442, 545, 317, 511, 578, 391, 993, 858, 586, 472, 906, 658, 704, 882, 246, 261, 501, 354, 903, 854, 279, 526, 686, 987, 403, 401, 989, 650, 576, 436, 560, 806, 554, 422, 298, 425, 912, 503, 611, 135, 447, 344, 338, 39, 804, 976, 186, 234, 106, 667, 494, 690, 480, 288, 151, 773, 769, 260, 809, 438, 237, 516, 29, 376, 72, 946, 103, 961, 55, 358, 232, 229, 90, 155, 657, 681, 43, 907, 564, 377, 615, 612, 157, 922, 272, 490, 679, 830, 839, 437, 826, 577, 937, 884, 13, 96, 273, 1, 188};
  final static int[]random_large = {5918,5624,2514,4291,1791,5109,7993,60,1345,2705,5849,1034,2085,4208,4590,7740,9367,6582,4178,5578,1120,378,7120,8646,5112,4903,1482,8005,3801,5439,4534,9524,6111,204,5459,248,4284,8037,5369,7334,3384,5193,2847,1660,5605,7371,3430,1786,1216,4282,2146,1969,7236,2187,136,2726,9480,5,4515,6082,969,5017,7809,9321,3826,9179,5781,3351,4819,4545,8607,4146,6682,1043,2890,2964,7472,9405,4348,8333,2915,9674,7225,4743,995,1321,3885,6061,9958,3901,4710,4185,4776,5070,8892,8506,6988,2317,9342,3764,9859,4724,5195,673,359,9740,2089,9942,3749,9208,1,7446,7023,5496,4206,3272,3527,8593,809,3149,4173,9605,9021,5120,5265,7121,8667,6911,4717,2535,2743,1289,1494,3788,6380,9366,2732,1501,8543,8013,5612,2393,7041,3350,3204,288,7213,1741,1238,9830,6722,4687,6758,8067,4443,5013,5374,6986,282,6762,192,340,5075,6970,7723,5913,1060,1641,1495,5738,1618,157,6891,173,7535,4952,9166,8950,8680,1974,5466,2383,3387,3392,2188,3140,6806,3131,6237,6249,7952,1114,9017,4285,7193,3191,3763,9087,7284,9170,6116,3717,6695,6538,6165,6449,8960,2897,6814,3283,6600,6151,4624,3992,5860,9557,1884,5585,2966,1061,6414,2431,9543,6654,7417,2617,878,8848,8241,3790,3370,8768,1694,9875,9882,8802,7072,3772,2689,5301,7921,7774,1614,494,2338,8638,4161,4523,5709,4305,17,9626,843,9284,3492,7755,5525,4423,9718,2237,7401,2686,8751,1585,5919,9444,3271,1490,7004,5980,3904,370,5930,6304,7737,93,5941,9079,4968,9266,262,2766,4999,2450,9518,5137,8405,483,8840,2231,700,8049,8823,9811,9378,3811,8074,153,1940,1998,4354,7830,7086,6132,9967,5680,448,1976,4101,7839,3122,4379,9296,4881,1246,4334,9457,5401,1945,9548,8290,1184,3464,132,2458,7704,1056,7554,6203,2270,6070,4889,7369,1676,485,3648,357,1912,9661,4246,1576,1836,4521,7667,6907,2098,8825,7404,4019,8284,3710,7202,7050,9870,3348,3624,9224,6601,7897,6288,3713,932,5596,353,2615,3273,833,1446,8624,2489,3872,486,1091,2493,4157,3611,6570,7107,9153,4543,9504,4746,1342,9737,3247,8984,3640,5698,7814,307,8775,1150,4330,3059,5784,2370,5248,4806,6107,9700,231,3566,5627,3957,5317,5415,8119,2588,9440,2961,9786,4769,466,5411,3080,7623,5031,2378,9286,4801,797,1527,2325,847,6341,5310,1926,9481,2115,2165,5255,5465,5561,3606,7673,7443,7243,8447,2348,7925,6447,8311,6729,4441,7763,8107,267,8135,9194,6775,3883,9639,612,5024,1351,7557,9241,5181,2239,8002,5446,747,166,325,9925,3820,9531,5163,3545,558,7103,7658,5670,8323,4821,6263,7982,59,3700,1082,4474,4353,8637,9558,5191,842,5925,6455,4092,9929,9961,290,3523,6290,7787,8266,7986,7269,6408,3620,406,5964,7289,1620,6726,1257,1993,7006,5545,2913,5093,5066,3019,7081,6760,6779,7061,9051,8852,8118,2340,6596,4594,9708,8430,8659,8920,9268,5431,9203,2823,1427,2203,6422,6193,5214,9566,8791,4964,7575,4350,56,2227,8545,5646,3089,2204,4081,487,8496,2258,4336,6955,3452,556,8602,8251,8569,8636,9430,1025,9459,7137,8392,3553,5945,9414,3078,1688,5480,327,8117,2289,2195,8564,9423,103,7724,3091,8548,7298,5279,6042,2855,3286,3542,9361,420,7020,4112,5320,5366,6379,114,9174,9744,592,5346,3985,3174,5157,9890,1605,3082,8099,4346,7256,8670,5687,6613,6620,1458,1045,7917,2980,2399,1433,3315,4084,178,7056,2132,2728,4421,9195,4181,6017,6229,2945,4627,2809,8816,6737,18,8981,3813,8890,5304,3789,6959,7476,1856,4197,6944,9578,5915,3060,9932,3463,67,7393,9857,5822,3187,501,653,8453,3691,9736,6845,1365,9645,4120,2157,8471,4436,6435,2758,7591,9805,7142,7612,4891,7342,5764,8683,8365,2967,6947,441,2116,6612,1399,7585,972,6548,5481,7733,7209,222,5903,6161,9172,9628,7348,1588,5992,6094,7176,4214,8702,2987,74,8486,9788,7164,5788,8535,8422,6826,1800,8965,4965,565,5609,4686,2556,9324,5000,9809,1994,4737,63,8992,4783,2536,4462,8868,6346,5553,3980,2670,1601,4272,8725,4698,7333,7826,9233,4198,1997,1687,4851,62,7893,8149,8015,341,2230,1280,5559,9756,3761,7834,6805,9287,4622,5748,2320,1958,9129,9649,1644,4323,5096,9490,7529,6444,7478,7044,9525,7713,234,7553,9099,9885,7135,6493,9793,6268,8363,2267,9157,9451,1438,9292,1637,3739,695,1090,4731,4549,5171,5975,7347,5192,5243,1084,2216,9860,3318,5594,5790,1107,220,9397,3378,1353,4498,6497,5442,7929,7377,9541,9871,9895,6742,9146,9409,292,6278,50,5288,2217,4923,6790,4730,9240,3006,3547,9347,7863,4275,3287,2673,7485,1915,9837,2931,3918,635,9131,1197,6250,3853,4303,790,5548,9993,3702,2446,3862,9652,4432,973,41,3507,8585,2444,1633,956,5789,1523,8657,4869,8580,8474,7093,7812,2549,7363,9315,6731,1130,7645,7018,7852,362,1636,2905,8006,4040,6643,8052,7021,3665,8383,715,1876,2783,3065,604,4566,8761,7911,1983,3836,5547,8495,8144,1950,2537,8575,640,8730,8303,1454,8165,6647,4762,909,9449,8640,9253,7293,8767,3004,4623,6862,8994,2520,1215,6299,8414,2576,6148,1510,313,3693,9843,8757,5774,8871,8061,8832,5573,5275,9452,1248,228,9749,2730};

// Generated by Classify instructions

  static void test_btree()                                                      // RiscV code for btree algorithm
   {RiscV r = new RiscV();
    r.test_btree1();
    r.test_btree2();
    say("AAAA", r.P.code.size());
   }
  ProgramDM.Label $branch_size = P.new Label();
  ProgramDM.Label $branch_size1 = P.new Label();
  ProgramDM.Label $branch_isEmpty = P.new Label();
  ProgramDM.Label $branch_key = P.new Label();
  ProgramDM.Label $branch_data = P.new Label();
  ProgramDM.Label $branch_setKey = P.new Label();
  ProgramDM.Label $branch_setData = P.new Label();
  ProgramDM.Label $branch_copyKey = P.new Label();
  ProgramDM.Label $branch_copyData = P.new Label();
  ProgramDM.Label $branch_setKeyData = P.new Label();
  ProgramDM.Label $branch_copyKeyData = P.new Label();
  ProgramDM.Label $branch_inc = P.new Label();
  ProgramDM.Label $branch_dec = P.new Label();
  ProgramDM.Label $branch_clear = P.new Label();
  ProgramDM.Label $branch_push = P.new Label();
  ProgramDM.Label $branch_result = P.new Label();
  ProgramDM.Label $branch_pop = P.new Label();
  ProgramDM.Label $branch_shift = P.new Label();
  ProgramDM.Label $L27 = P.new Label();
  ProgramDM.Label $L26 = P.new Label();
  ProgramDM.Label $branch_elementAt = P.new Label();
  ProgramDM.Label $branch_setElementAt = P.new Label();
  ProgramDM.Label $L32 = P.new Label();
  ProgramDM.Label $L34 = P.new Label();
  ProgramDM.Label $branch_insertElementAt = P.new Label();
  ProgramDM.Label $L37 = P.new Label();
  ProgramDM.Label $L36 = P.new Label();
  ProgramDM.Label $branch_removeElementAt = P.new Label();
  ProgramDM.Label $L40 = P.new Label();
  ProgramDM.Label $L39 = P.new Label();
  ProgramDM.Label $branch_firstElement = P.new Label();
  ProgramDM.Label $L43 = P.new Label();
  ProgramDM.Label $branch_lastElement = P.new Label();
  ProgramDM.Label $L46 = P.new Label();
  ProgramDM.Label $branch_searchFirstGreaterThanOrEqualExceptLast = P.new Label();
  ProgramDM.Label $L52 = P.new Label();
  ProgramDM.Label $L50 = P.new Label();
  ProgramDM.Label $L49 = P.new Label();
  ProgramDM.Label $L53 = P.new Label();
  ProgramDM.Label $leaf_size = P.new Label();
  ProgramDM.Label $leaf_isEmpty = P.new Label();
  ProgramDM.Label $leaf_key = P.new Label();
  ProgramDM.Label $leaf_data = P.new Label();
  ProgramDM.Label $leaf_setKey = P.new Label();
  ProgramDM.Label $leaf_setData = P.new Label();
  ProgramDM.Label $leaf_copyKey = P.new Label();
  ProgramDM.Label $leaf_copyData = P.new Label();
  ProgramDM.Label $leaf_setKeyData = P.new Label();
  ProgramDM.Label $leaf_copyKeyData = P.new Label();
  ProgramDM.Label $leaf_inc = P.new Label();
  ProgramDM.Label $leaf_dec = P.new Label();
  ProgramDM.Label $leaf_push = P.new Label();
  ProgramDM.Label $leaf_result = P.new Label();
  ProgramDM.Label $leaf_pop = P.new Label();
  ProgramDM.Label $leaf_shift = P.new Label();
  ProgramDM.Label $L77 = P.new Label();
  ProgramDM.Label $L76 = P.new Label();
  ProgramDM.Label $leaf_elementAt = P.new Label();
  ProgramDM.Label $leaf_setElementAt = P.new Label();
  ProgramDM.Label $L82 = P.new Label();
  ProgramDM.Label $L84 = P.new Label();
  ProgramDM.Label $leaf_insertElementAt = P.new Label();
  ProgramDM.Label $L87 = P.new Label();
  ProgramDM.Label $L86 = P.new Label();
  ProgramDM.Label $leaf_removeElementAt = P.new Label();
  ProgramDM.Label $L90 = P.new Label();
  ProgramDM.Label $L89 = P.new Label();
  ProgramDM.Label $leaf_firstElement = P.new Label();
  ProgramDM.Label $L93 = P.new Label();
  ProgramDM.Label $leaf_lastElement = P.new Label();
  ProgramDM.Label $L96 = P.new Label();
  ProgramDM.Label $leaf_search = P.new Label();
  ProgramDM.Label $L102 = P.new Label();
  ProgramDM.Label $L100 = P.new Label();
  ProgramDM.Label $L99 = P.new Label();
  ProgramDM.Label $L103 = P.new Label();
  ProgramDM.Label $leaf_searchFirstGreaterThanOrEqual = P.new Label();
  ProgramDM.Label $L108 = P.new Label();
  ProgramDM.Label $L106 = P.new Label();
  ProgramDM.Label $L105 = P.new Label();
  ProgramDM.Label $L109 = P.new Label();
  ProgramDM.Label $debug = P.new Label();
  ProgramDM.Label $btree = P.new Label();
  ProgramDM.Label $allocate = P.new Label();
  ProgramDM.Label $isLeaf = P.new Label();
  ProgramDM.Label $rootIsLeaf = P.new Label();
  ProgramDM.Label $setLeaf = P.new Label();
  ProgramDM.Label $setBranch = P.new Label();
  ProgramDM.Label $setBranchRoot = P.new Label();
  ProgramDM.Label $clear = P.new Label();
  ProgramDM.Label $L120 = P.new Label();
  ProgramDM.Label $L122 = P.new Label();
  ProgramDM.Label $L121 = P.new Label();
  ProgramDM.Label $erase = P.new Label();
  ProgramDM.Label $L124 = P.new Label();
  ProgramDM.Label $L126 = P.new Label();
  ProgramDM.Label $L125 = P.new Label();
  ProgramDM.Label $freeNode = P.new Label();
  ProgramDM.Label $leaf = P.new Label();
  ProgramDM.Label $branch = P.new Label();
  ProgramDM.Label $leafSize = P.new Label();
  ProgramDM.Label $branchSize = P.new Label();
  ProgramDM.Label $splitLeafSize = P.new Label();
  ProgramDM.Label $splitBranchSize = P.new Label();
  ProgramDM.Label $isFull = P.new Label();
  ProgramDM.Label $L141 = P.new Label();
  ProgramDM.Label $L142 = P.new Label();
  ProgramDM.Label $isFullRoot = P.new Label();
  ProgramDM.Label $isLow = P.new Label();
  ProgramDM.Label $L147 = P.new Label();
  ProgramDM.Label $L148 = P.new Label();
  ProgramDM.Label $hasLeavesForChildren = P.new Label();
  ProgramDM.Label $allocLeaf = P.new Label();
  ProgramDM.Label $allocBranch = P.new Label();
  ProgramDM.Label $splitLeafRoot = P.new Label();
  ProgramDM.Label $L158 = P.new Label();
  ProgramDM.Label $L157 = P.new Label();
  ProgramDM.Label $L160 = P.new Label();
  ProgramDM.Label $L159 = P.new Label();
  ProgramDM.Label $splitBranchRoot = P.new Label();
  ProgramDM.Label $L163 = P.new Label();
  ProgramDM.Label $L162 = P.new Label();
  ProgramDM.Label $L165 = P.new Label();
  ProgramDM.Label $L164 = P.new Label();
  ProgramDM.Label $splitLeaf = P.new Label();
  ProgramDM.Label $L168 = P.new Label();
  ProgramDM.Label $L167 = P.new Label();
  ProgramDM.Label $splitBranch = P.new Label();
  ProgramDM.Label $L171 = P.new Label();
  ProgramDM.Label $L170 = P.new Label();
  ProgramDM.Label $stealFromLeft = P.new Label();
  ProgramDM.Label $L174 = P.new Label();
  ProgramDM.Label $L176 = P.new Label();
  ProgramDM.Label $L173 = P.new Label();
  ProgramDM.Label $L179 = P.new Label();
  ProgramDM.Label $L181 = P.new Label();
  ProgramDM.Label $L177 = P.new Label();
  ProgramDM.Label $L178 = P.new Label();
  ProgramDM.Label $stealFromRight = P.new Label();
  ProgramDM.Label $L184 = P.new Label();
  ProgramDM.Label $L186 = P.new Label();
  ProgramDM.Label $L183 = P.new Label();
  ProgramDM.Label $L189 = P.new Label();
  ProgramDM.Label $L191 = P.new Label();
  ProgramDM.Label $L187 = P.new Label();
  ProgramDM.Label $L192 = P.new Label();
  ProgramDM.Label $mergeRoot = P.new Label();
  ProgramDM.Label $L194 = P.new Label();
  ProgramDM.Label $L195 = P.new Label();
  ProgramDM.Label $L200 = P.new Label();
  ProgramDM.Label $L199 = P.new Label();
  ProgramDM.Label $L202 = P.new Label();
  ProgramDM.Label $L201 = P.new Label();
  ProgramDM.Label $L197 = P.new Label();
  ProgramDM.Label $L204 = P.new Label();
  ProgramDM.Label $L203 = P.new Label();
  ProgramDM.Label $L206 = P.new Label();
  ProgramDM.Label $L205 = P.new Label();
  ProgramDM.Label $L198 = P.new Label();
  ProgramDM.Label $L196 = P.new Label();
  ProgramDM.Label $mergeLeftSibling = P.new Label();
  ProgramDM.Label $L208 = P.new Label();
  ProgramDM.Label $L210 = P.new Label();
  ProgramDM.Label $L212 = P.new Label();
  ProgramDM.Label $L214 = P.new Label();
  ProgramDM.Label $L213 = P.new Label();
  ProgramDM.Label $L211 = P.new Label();
  ProgramDM.Label $L216 = P.new Label();
  ProgramDM.Label $L219 = P.new Label();
  ProgramDM.Label $L218 = P.new Label();
  ProgramDM.Label $L215 = P.new Label();
  ProgramDM.Label $L220 = P.new Label();
  ProgramDM.Label $mergeRightSibling = P.new Label();
  ProgramDM.Label $L222 = P.new Label();
  ProgramDM.Label $L225 = P.new Label();
  ProgramDM.Label $L227 = P.new Label();
  ProgramDM.Label $L226 = P.new Label();
  ProgramDM.Label $L224 = P.new Label();
  ProgramDM.Label $L229 = P.new Label();
  ProgramDM.Label $L232 = P.new Label();
  ProgramDM.Label $L231 = P.new Label();
  ProgramDM.Label $L228 = P.new Label();
  ProgramDM.Label $L233 = P.new Label();
  ProgramDM.Label $balance = P.new Label();
  ProgramDM.Label $L242 = P.new Label();
  ProgramDM.Label $L243 = P.new Label();
  ProgramDM.Label $L244 = P.new Label();
  ProgramDM.Label $L245 = P.new Label();
  ProgramDM.Label $L234 = P.new Label();
  ProgramDM.Label $find_result = P.new Label();
  ProgramDM.Label $find = P.new Label();
  ProgramDM.Label $L249 = P.new Label();
  ProgramDM.Label $L253 = P.new Label();
  ProgramDM.Label $L252 = P.new Label();
  ProgramDM.Label $L251 = P.new Label();
  ProgramDM.Label $L254 = P.new Label();
  ProgramDM.Label $findAndInsert_result = P.new Label();
  ProgramDM.Label $findAndInsert = P.new Label();
  ProgramDM.Label $L258 = P.new Label();
  ProgramDM.Label $L261 = P.new Label();
  ProgramDM.Label $L262 = P.new Label();
  ProgramDM.Label $L260 = P.new Label();
  ProgramDM.Label $L263 = P.new Label();
  ProgramDM.Label $put = P.new Label();
  ProgramDM.Label $L268 = P.new Label();
  ProgramDM.Label $L269 = P.new Label();
  ProgramDM.Label $L267 = P.new Label();
  ProgramDM.Label $L276 = P.new Label();
  ProgramDM.Label $L273 = P.new Label();
  ProgramDM.Label $L274 = P.new Label();
  ProgramDM.Label $L278 = P.new Label();
  ProgramDM.Label $L272 = P.new Label();
  ProgramDM.Label $L279 = P.new Label();
  ProgramDM.Label $L280 = P.new Label();
  ProgramDM.Label $L264 = P.new Label();
  ProgramDM.Label $findAndDelete_result = P.new Label();
  ProgramDM.Label $findAndDelete = P.new Label();
  ProgramDM.Label $L284 = P.new Label();
  ProgramDM.Label $L286 = P.new Label();
  ProgramDM.Label $delete = P.new Label();
  ProgramDM.Label $L288 = P.new Label();
  ProgramDM.Label $L292 = P.new Label();
  ProgramDM.Label $L291 = P.new Label();
  ProgramDM.Label $L290 = P.new Label();
  ProgramDM.Label $L287 = P.new Label();
  ProgramDM.Label $merge = P.new Label();
  ProgramDM.Label $L302 = P.new Label();
  ProgramDM.Label $L300 = P.new Label();
  ProgramDM.Label $L299 = P.new Label();
  ProgramDM.Label $L298 = P.new Label();
  ProgramDM.Label $L295 = P.new Label();
  ProgramDM.Label $L303 = P.new Label();
  ProgramDM.Label $L294 = P.new Label();
  void test_btree1() {
    addi(sp, sp, -32);           /* LLLL */
    sd(s0, 24, sp);             /* OOOO */
    addi(s0, sp, 32);           /* LLLL */
    sd(a0, -24, s0);             /* OOOO */
    ld(a5, s0, 24);             /* GGGG */
    lw(a5, 0, a5);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    ld(s0, sp, 24);             /* GGGG */
    addi(sp, sp, 32);           /* LLLL */
    jr(ra);                       /* UUUU */
    $branch_size1.set();
    addi(sp, sp, -32);           /* LLLL */
    sd(s0, 24, sp);             /* OOOO */
    addi(s0, sp, 32);           /* LLLL */
    sd(a0, -24, s0);             /* OOOO */
    ld(a5, s0, 24);             /* GGGG */
    lw(a5, 0, a5);             /* SSSS */
    addiw(a5, a5, -1);           /* LLLL */
    sextw(a5,a5);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    ld(s0, sp, 24);             /* GGGG */
    addi(sp, sp, 32);           /* LLLL */
    jr(ra);                       /* UUUU */
    $branch_isEmpty.set();
    addi(sp, sp, -32);           /* LLLL */
    sd(ra, 24, sp);             /* OOOO */
    sd(s0, 16, sp);             /* OOOO */
    addi(s0, sp, 32);           /* LLLL */
    sd(a0, -24, s0);             /* OOOO */
    ld(a0, s0, 24);             /* GGGG */
    call($branch_size);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    seqz(a5, a5);                 /* YY11 */
    andi(a5,a5,0xff);        /* ZZZZ */
    sextw(a5,a5);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    ld(ra, sp, 24);             /* GGGG */
    ld(s0, sp, 16);             /* GGGG */
    addi(sp, sp, 32);           /* LLLL */
    jr(ra);                       /* UUUU */
    $branch_key.set();
    addi(sp, sp, -32);           /* LLLL */
    sd(s0, 24, sp);             /* OOOO */
    addi(s0, sp, 32);           /* LLLL */
    sd(a0, -24, s0);             /* OOOO */
    mv(a5,a1);        /* ZZZZ */
    sw(a5, -28, s0);             /* OOOO */
    ld(a4, s0, 24);             /* GGGG */
    lw(a5, -28, s0);             /* SSSS */
    slli(a5,a5,2);        /* ZZZZ */
    add(a5,a4,a5);        /* ZZZZ */
    lw(a5, 4, a5);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    ld(s0, sp, 24);             /* GGGG */
    addi(sp, sp, 32);           /* LLLL */
    jr(ra);                       /* UUUU */
    $branch_data.set();
    addi(sp, sp, -32);           /* LLLL */
    sd(s0, 24, sp);             /* OOOO */
    addi(s0, sp, 32);           /* LLLL */
    sd(a0, -24, s0);             /* OOOO */
    mv(a5,a1);        /* ZZZZ */
    sw(a5, -28, s0);             /* OOOO */
    ld(a4, s0, 24);             /* GGGG */
    lw(a5, -28, s0);             /* SSSS */
    addi(a5, a5, 4);           /* LLLL */
    slli(a5,a5,2);        /* ZZZZ */
    add(a5,a4,a5);        /* ZZZZ */
    lw(a5, 4, a5);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    ld(s0, sp, 24);             /* GGGG */
    addi(sp, sp, 32);           /* LLLL */
    jr(ra);                       /* UUUU */
    $branch_setKey.set();
    addi(sp, sp, -32);           /* LLLL */
    sd(s0, 24, sp);             /* OOOO */
    addi(s0, sp, 32);           /* LLLL */
    sd(a0, -24, s0);             /* OOOO */
    mv(a5,a1);        /* ZZZZ */
    mv(a4,a2);        /* ZZZZ */
    sw(a5, -28, s0);             /* OOOO */
    mv(a5,a4);        /* ZZZZ */
    sw(a5, -32, s0);             /* OOOO */
    ld(a4, s0, 24);             /* GGGG */
    lw(a5, -28, s0);             /* SSSS */
    slli(a5,a5,2);        /* ZZZZ */
    add(a5,a4,a5);        /* ZZZZ */
    lw(a4, -32, s0);             /* SSSS */
    sw(a4, 4, a5);             /* OOOO */
    nop();        /* ZZZZ */
    ld(s0, sp, 24);             /* GGGG */
    addi(sp, sp, 32);           /* LLLL */
    jr(ra);                       /* UUUU */
    $branch_setData.set();
    addi(sp, sp, -32);           /* LLLL */
    sd(s0, 24, sp);             /* OOOO */
    addi(s0, sp, 32);           /* LLLL */
    sd(a0, -24, s0);             /* OOOO */
    mv(a5,a1);        /* ZZZZ */
    mv(a4,a2);        /* ZZZZ */
    sw(a5, -28, s0);             /* OOOO */
    mv(a5,a4);        /* ZZZZ */
    sw(a5, -32, s0);             /* OOOO */
    ld(a4, s0, 24);             /* GGGG */
    lw(a5, -28, s0);             /* SSSS */
    addi(a5, a5, 4);           /* LLLL */
    slli(a5,a5,2);        /* ZZZZ */
    add(a5,a4,a5);        /* ZZZZ */
    lw(a4, -32, s0);             /* SSSS */
    sw(a4, 4, a5);             /* OOOO */
    nop();        /* ZZZZ */
    ld(s0, sp, 24);             /* GGGG */
    addi(sp, sp, 32);           /* LLLL */
    jr(ra);                       /* UUUU */
    $branch_copyKey.set();
    addi(sp, sp, -32);           /* LLLL */
    sd(s0, 24, sp);             /* OOOO */
    addi(s0, sp, 32);           /* LLLL */
    sd(a0, -24, s0);             /* OOOO */
    mv(a5,a1);        /* ZZZZ */
    mv(a4,a2);        /* ZZZZ */
    sw(a5, -28, s0);             /* OOOO */
    mv(a5,a4);        /* ZZZZ */
    sw(a5, -32, s0);             /* OOOO */
    ld(a4, s0, 24);             /* GGGG */
    lw(a5, -32, s0);             /* SSSS */
    slli(a5,a5,2);        /* ZZZZ */
    add(a5,a4,a5);        /* ZZZZ */
    lw(a4, 4, a5);             /* SSSS */
    ld(a3, s0, 24);             /* GGGG */
    lw(a5, -28, s0);             /* SSSS */
    slli(a5,a5,2);        /* ZZZZ */
    add(a5,a3,a5);        /* ZZZZ */
    sw(a4, 4, a5);             /* OOOO */
    nop();        /* ZZZZ */
    ld(s0, sp, 24);             /* GGGG */
    addi(sp, sp, 32);           /* LLLL */
    jr(ra);                       /* UUUU */
    $branch_copyData.set();
    addi(sp, sp, -32);           /* LLLL */
    sd(s0, 24, sp);             /* OOOO */
    addi(s0, sp, 32);           /* LLLL */
    sd(a0, -24, s0);             /* OOOO */
    mv(a5,a1);        /* ZZZZ */
    mv(a4,a2);        /* ZZZZ */
    sw(a5, -28, s0);             /* OOOO */
    mv(a5,a4);        /* ZZZZ */
    sw(a5, -32, s0);             /* OOOO */
    ld(a4, s0, 24);             /* GGGG */
    lw(a5, -32, s0);             /* SSSS */
    addi(a5, a5, 4);           /* LLLL */
    slli(a5,a5,2);        /* ZZZZ */
    add(a5,a4,a5);        /* ZZZZ */
    lw(a4, 4, a5);             /* SSSS */
    ld(a3, s0, 24);             /* GGGG */
    lw(a5, -28, s0);             /* SSSS */
    addi(a5, a5, 4);           /* LLLL */
    slli(a5,a5,2);        /* ZZZZ */
    add(a5,a3,a5);        /* ZZZZ */
    sw(a4, 4, a5);             /* OOOO */
    nop();        /* ZZZZ */
    ld(s0, sp, 24);             /* GGGG */
    addi(sp, sp, 32);           /* LLLL */
    jr(ra);                       /* UUUU */
    $branch_setKeyData.set();
    addi(sp, sp, -48);           /* LLLL */
    sd(ra, 40, sp);             /* OOOO */
    sd(s0, 32, sp);             /* OOOO */
    addi(s0, sp, 48);           /* LLLL */
    sd(a0, -24, s0);             /* OOOO */
    mv(a5,a1);        /* ZZZZ */
    mv(a4,a3);        /* ZZZZ */
    sw(a5, -28, s0);             /* OOOO */
    mv(a5,a2);        /* ZZZZ */
    sw(a5, -32, s0);             /* OOOO */
    mv(a5,a4);        /* ZZZZ */
    sw(a5, -36, s0);             /* OOOO */
    lw(a4, -32, s0);             /* SSSS */
    lw(a5, -28, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 24);             /* GGGG */
    call($branch_setKey);                      /* XXXX */
    lw(a4, -36, s0);             /* SSSS */
    lw(a5, -28, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 24);             /* GGGG */
    call($branch_setData);                      /* XXXX */
    nop();        /* ZZZZ */
    ld(ra, sp, 40);             /* GGGG */
    ld(s0, sp, 32);             /* GGGG */
    addi(sp, sp, 48);           /* LLLL */
    jr(ra);                       /* UUUU */
    $branch_copyKeyData.set();
    addi(sp, sp, -32);           /* LLLL */
    sd(ra, 24, sp);             /* OOOO */
    sd(s0, 16, sp);             /* OOOO */
    addi(s0, sp, 32);           /* LLLL */
    sd(a0, -24, s0);             /* OOOO */
    mv(a5,a1);        /* ZZZZ */
    mv(a4,a2);        /* ZZZZ */
    sw(a5, -28, s0);             /* OOOO */
    mv(a5,a4);        /* ZZZZ */
    sw(a5, -32, s0);             /* OOOO */
    lw(a4, -32, s0);             /* SSSS */
    lw(a5, -28, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 24);             /* GGGG */
    call($branch_copyKey);                      /* XXXX */
    lw(a4, -32, s0);             /* SSSS */
    lw(a5, -28, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 24);             /* GGGG */
    call($branch_copyData);                      /* XXXX */
    nop();        /* ZZZZ */
    ld(ra, sp, 24);             /* GGGG */
    ld(s0, sp, 16);             /* GGGG */
    addi(sp, sp, 32);           /* LLLL */
    jr(ra);                       /* UUUU */
    $branch_inc.set();
    addi(sp, sp, -32);           /* LLLL */
    sd(s0, 24, sp);             /* OOOO */
    addi(s0, sp, 32);           /* LLLL */
    sd(a0, -24, s0);             /* OOOO */
    ld(a5, s0, 24);             /* GGGG */
    lw(a5, 0, a5);             /* SSSS */
    addiw(a5, a5, 1);           /* LLLL */
    sextw(a4,a5);        /* ZZZZ */
    ld(a5, s0, 24);             /* GGGG */
    sw(a4, 0, a5);             /* OOOO */
    nop();        /* ZZZZ */
    ld(s0, sp, 24);             /* GGGG */
    addi(sp, sp, 32);           /* LLLL */
    jr(ra);                       /* UUUU */
    $branch_dec.set();
    addi(sp, sp, -32);           /* LLLL */
    sd(s0, 24, sp);             /* OOOO */
    addi(s0, sp, 32);           /* LLLL */
    sd(a0, -24, s0);             /* OOOO */
    ld(a5, s0, 24);             /* GGGG */
    lw(a5, 0, a5);             /* SSSS */
    addiw(a5, a5, -1);           /* LLLL */
    sextw(a4,a5);        /* ZZZZ */
    ld(a5, s0, 24);             /* GGGG */
    sw(a4, 0, a5);             /* OOOO */
    nop();        /* ZZZZ */
    ld(s0, sp, 24);             /* GGGG */
    addi(sp, sp, 32);           /* LLLL */
    jr(ra);                       /* UUUU */
    $branch_clear.set();
    addi(sp, sp, -32);           /* LLLL */
    sd(s0, 24, sp);             /* OOOO */
    addi(s0, sp, 32);           /* LLLL */
    sd(a0, -24, s0);             /* OOOO */
    ld(a5, s0, 24);             /* GGGG */
    sw(zero, 0, a5);             /* OOOO */
    nop();        /* ZZZZ */
    ld(s0, sp, 24);             /* GGGG */
    addi(sp, sp, 32);           /* LLLL */
    jr(ra);                       /* UUUU */
    $branch_push.set();
    addi(sp, sp, -48);           /* LLLL */
    sd(ra, 40, sp);             /* OOOO */
    sd(s0, 32, sp);             /* OOOO */
    addi(s0, sp, 48);           /* LLLL */
    sd(a0, -40, s0);             /* OOOO */
    mv(a5,a1);        /* ZZZZ */
    mv(a4,a2);        /* ZZZZ */
    sw(a5, -44, s0);             /* OOOO */
    mv(a5,a4);        /* ZZZZ */
    sw(a5, -48, s0);             /* OOOO */
    ld(a0, s0, 40);             /* GGGG */
    call($branch_size);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -20, s0);             /* OOOO */
    lw(a3, -48, s0);             /* SSSS */
    lw(a4, -44, s0);             /* SSSS */
    lw(a5, -20, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 40);             /* GGGG */
    call($branch_setKeyData);                      /* XXXX */
    ld(a0, s0, 40);             /* GGGG */
    call($branch_inc);                      /* XXXX */
    nop();        /* ZZZZ */
    ld(ra, sp, 40);             /* GGGG */
    ld(s0, sp, 32);             /* GGGG */
    addi(sp, sp, 48);           /* LLLL */
    jr(ra);                       /* UUUU */
    $branch_result.set();
    addi(sp, sp, -64);           /* LLLL */
    sd(s0, 56, sp);             /* OOOO */
    addi(s0, sp, 64);           /* LLLL */
    sd(a0, -56, s0);             /* OOOO */
    sw(zero, -24, s0);             /* OOOO */
    lw(a5, -24, s0);             /* SSSS */
    sw(a5, -28, s0);             /* OOOO */
    lw(a5, -28, s0);             /* SSSS */
    sw(a5, -32, s0);             /* OOOO */
    lw(a5, -32, s0);             /* SSSS */
    sw(a5, -36, s0);             /* OOOO */
    lw(a5, -36, s0);             /* SSSS */
    sw(a5, -40, s0);             /* OOOO */
    ld(a5, s0, 56);             /* GGGG */
    lw(a4, -40, s0);             /* SSSS */
    mv(a0,a4);        /* ZZZZ */
    lw(a4, -36, s0);             /* SSSS */
    mv(a1,a4);        /* ZZZZ */
    lw(a4, -32, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    lw(a4, -28, s0);             /* SSSS */
    mv(a3,a4);        /* ZZZZ */
    lw(a4, -24, s0);             /* SSSS */
    sw(a0, 0, a5);             /* OOOO */
    sw(a1, 4, a5);             /* OOOO */
    sw(a2, 8, a5);             /* OOOO */
    sw(a3, 12, a5);             /* OOOO */
    sw(a4, 16, a5);             /* OOOO */
    ld(a0, s0, 56);             /* GGGG */
    ld(s0, sp, 56);             /* GGGG */
    addi(sp, sp, 64);           /* LLLL */
    jr(ra);                       /* UUUU */
    $branch_pop.set();
    addi(sp, sp, -64);           /* LLLL */
    sd(ra, 56, sp);             /* OOOO */
    sd(s0, 48, sp);             /* OOOO */
    addi(s0, sp, 64);           /* LLLL */
    sd(a0, -56, s0);             /* OOOO */
    sd(a1, -64, s0);             /* OOOO */
    addi(a5, s0, -40);           /* LLLL */
    mv(a0,a5);        /* ZZZZ */
    call($branch_result);                      /* XXXX */
    ld(a0, s0, 64);             /* GGGG */
    call($branch_dec);                      /* XXXX */
    ld(a0, s0, 64);             /* GGGG */
    call($branch_size);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -32, s0);             /* OOOO */
    lw(a5, -32, s0);             /* SSSS */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 64);             /* GGGG */
    call($branch_key);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -28, s0);             /* OOOO */
    lw(a5, -32, s0);             /* SSSS */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 64);             /* GGGG */
    call($branch_data);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -24, s0);             /* OOOO */
    ld(a5, s0, 56);             /* GGGG */
    lw(a4, -40, s0);             /* SSSS */
    mv(a0,a4);        /* ZZZZ */
    lw(a4, -36, s0);             /* SSSS */
    mv(a1,a4);        /* ZZZZ */
    lw(a4, -32, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    lw(a4, -28, s0);             /* SSSS */
    mv(a3,a4);        /* ZZZZ */
    lw(a4, -24, s0);             /* SSSS */
    sw(a0, 0, a5);             /* OOOO */
    sw(a1, 4, a5);             /* OOOO */
    sw(a2, 8, a5);             /* OOOO */
    sw(a3, 12, a5);             /* OOOO */
    sw(a4, 16, a5);             /* OOOO */
    ld(a0, s0, 56);             /* GGGG */
    ld(ra, sp, 56);             /* GGGG */
    ld(s0, sp, 48);             /* GGGG */
    addi(sp, sp, 64);           /* LLLL */
    jr(ra);                       /* UUUU */
    $branch_shift.set();
    addi(sp, sp, -64);           /* LLLL */
    sd(ra, 56, sp);             /* OOOO */
    sd(s0, 48, sp);             /* OOOO */
    addi(s0, sp, 64);           /* LLLL */
    sd(a0, -56, s0);             /* OOOO */
    sd(a1, -64, s0);             /* OOOO */
    addi(a5, s0, -48);           /* LLLL */
    mv(a0,a5);        /* ZZZZ */
    call($branch_result);                      /* XXXX */
    li(a1,0);        /* ZZZZ */
    ld(a0, s0, 64);             /* GGGG */
    call($branch_key);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -36, s0);             /* OOOO */
    li(a1,0);        /* ZZZZ */
    ld(a0, s0, 64);             /* GGGG */
    call($branch_data);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -32, s0);             /* OOOO */
    sw(zero, -20, s0);             /* OOOO */
    ld(a0, s0, 64);             /* GGGG */
    call($branch_size);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    addiw(a5, a5, -1);           /* LLLL */
    sw(a5, -24, s0);             /* OOOO */
    j($L26);                       /* WWWW */
    $L27.set();
    lw(a5, -20, s0);             /* SSSS */
    addiw(a5, a5, 1);           /* LLLL */
    sextw(a4,a5);        /* ZZZZ */
    lw(a5, -20, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 64);             /* GGGG */
    call($branch_copyKeyData);                      /* XXXX */
    lw(a5, -20, s0);             /* SSSS */
    addiw(a5, a5, 1);           /* LLLL */
    sw(a5, -20, s0);             /* OOOO */
    $L26.set();
    lw(a5, -20, s0);             /* SSSS */
    mv(a4,a5);        /* ZZZZ */
    lw(a5, -24, s0);             /* SSSS */
    sextw(a4,a4);        /* ZZZZ */
    sextw(a5,a5);        /* ZZZZ */
    blt(a4, a5, $L27);             /* YYYY */
    ld(a0, s0, 64);             /* GGGG */
    call($branch_dec);                      /* XXXX */
    ld(a5, s0, 56);             /* GGGG */
    lw(a4, -48, s0);             /* SSSS */
    mv(a0,a4);        /* ZZZZ */
    lw(a4, -44, s0);             /* SSSS */
    mv(a1,a4);        /* ZZZZ */
    lw(a4, -40, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    lw(a4, -36, s0);             /* SSSS */
    mv(a3,a4);        /* ZZZZ */
    lw(a4, -32, s0);             /* SSSS */
    sw(a0, 0, a5);             /* OOOO */
    sw(a1, 4, a5);             /* OOOO */
    sw(a2, 8, a5);             /* OOOO */
    sw(a3, 12, a5);             /* OOOO */
    sw(a4, 16, a5);             /* OOOO */
    ld(a0, s0, 56);             /* GGGG */
    ld(ra, sp, 56);             /* GGGG */
    ld(s0, sp, 48);             /* GGGG */
    addi(sp, sp, 64);           /* LLLL */
    jr(ra);                       /* UUUU */
    $branch_elementAt.set();
    addi(sp, sp, -80);           /* LLLL */
    sd(ra, 72, sp);             /* OOOO */
    sd(s0, 64, sp);             /* OOOO */
    addi(s0, sp, 80);           /* LLLL */
    sd(a0, -56, s0);             /* OOOO */
    sd(a1, -64, s0);             /* OOOO */
    mv(a5,a2);        /* ZZZZ */
    sw(a5, -68, s0);             /* OOOO */
    addi(a5, s0, -40);           /* LLLL */
    mv(a0,a5);        /* ZZZZ */
    call($branch_result);                      /* XXXX */
    lw(a5, -68, s0);             /* SSSS */
    sw(a5, -32, s0);             /* OOOO */
    lw(a5, -68, s0);             /* SSSS */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 64);             /* GGGG */
    call($branch_key);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -28, s0);             /* OOOO */
    lw(a5, -68, s0);             /* SSSS */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 64);             /* GGGG */
    call($branch_data);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -24, s0);             /* OOOO */
    ld(a5, s0, 56);             /* GGGG */
    lw(a4, -40, s0);             /* SSSS */
    mv(a0,a4);        /* ZZZZ */
    lw(a4, -36, s0);             /* SSSS */
    mv(a1,a4);        /* ZZZZ */
    lw(a4, -32, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    lw(a4, -28, s0);             /* SSSS */
    mv(a3,a4);        /* ZZZZ */
    lw(a4, -24, s0);             /* SSSS */
    sw(a0, 0, a5);             /* OOOO */
    sw(a1, 4, a5);             /* OOOO */
    sw(a2, 8, a5);             /* OOOO */
    sw(a3, 12, a5);             /* OOOO */
    sw(a4, 16, a5);             /* OOOO */
    ld(a0, s0, 56);             /* GGGG */
    ld(ra, sp, 72);             /* GGGG */
    ld(s0, sp, 64);             /* GGGG */
    addi(sp, sp, 80);           /* LLLL */
    jr(ra);                       /* UUUU */
    $branch_setElementAt.set();
    addi(sp, sp, -48);           /* LLLL */
    sd(ra, 40, sp);             /* OOOO */
    sd(s0, 32, sp);             /* OOOO */
    addi(s0, sp, 48);           /* LLLL */
    sd(a0, -24, s0);             /* OOOO */
    mv(a5,a1);        /* ZZZZ */
    mv(a4,a3);        /* ZZZZ */
    sw(a5, -28, s0);             /* OOOO */
    mv(a5,a2);        /* ZZZZ */
    sw(a5, -32, s0);             /* OOOO */
    mv(a5,a4);        /* ZZZZ */
    sw(a5, -36, s0);             /* OOOO */
    ld(a0, s0, 24);             /* GGGG */
    call($branch_size);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    mv(a4,a5);        /* ZZZZ */
    lw(a5, -36, s0);             /* SSSS */
    sextw(a5,a5);        /* ZZZZ */
    bne(a5, a4, $L32);             /* YYYY */
    lw(a3, -32, s0);             /* SSSS */
    lw(a4, -28, s0);             /* SSSS */
    lw(a5, -36, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 24);             /* GGGG */
    call($branch_setKeyData);                      /* XXXX */
    ld(a0, s0, 24);             /* GGGG */
    call($branch_inc);                      /* XXXX */
    j($L34);                       /* WWWW */
    $L32.set();
    lw(a3, -32, s0);             /* SSSS */
    lw(a4, -28, s0);             /* SSSS */
    lw(a5, -36, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 24);             /* GGGG */
    call($branch_setKeyData);                      /* XXXX */
    $L34.set();
    nop();        /* ZZZZ */
    ld(ra, sp, 40);             /* GGGG */
    ld(s0, sp, 32);             /* GGGG */
    addi(sp, sp, 48);           /* LLLL */
    jr(ra);                       /* UUUU */
    $branch_insertElementAt.set();
    addi(sp, sp, -64);           /* LLLL */
    sd(ra, 56, sp);             /* OOOO */
    sd(s0, 48, sp);             /* OOOO */
    addi(s0, sp, 64);           /* LLLL */
    sd(a0, -40, s0);             /* OOOO */
    mv(a5,a1);        /* ZZZZ */
    mv(a4,a3);        /* ZZZZ */
    sw(a5, -44, s0);             /* OOOO */
    mv(a5,a2);        /* ZZZZ */
    sw(a5, -48, s0);             /* OOOO */
    mv(a5,a4);        /* ZZZZ */
    sw(a5, -52, s0);             /* OOOO */
    ld(a0, s0, 40);             /* GGGG */
    call($branch_size);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -20, s0);             /* OOOO */
    j($L36);                       /* WWWW */
    $L37.set();
    lw(a5, -20, s0);             /* SSSS */
    addiw(a5, a5, -1);           /* LLLL */
    sextw(a4,a5);        /* ZZZZ */
    lw(a5, -20, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 40);             /* GGGG */
    call($branch_copyKeyData);                      /* XXXX */
    lw(a5, -20, s0);             /* SSSS */
    addiw(a5, a5, -1);           /* LLLL */
    sw(a5, -20, s0);             /* OOOO */
    $L36.set();
    lw(a5, -20, s0);             /* SSSS */
    mv(a4,a5);        /* ZZZZ */
    lw(a5, -52, s0);             /* SSSS */
    sextw(a4,a4);        /* ZZZZ */
    sextw(a5,a5);        /* ZZZZ */
    bgt(a4, a5, $L37);             /* YYYY */
    lw(a3, -48, s0);             /* SSSS */
    lw(a4, -44, s0);             /* SSSS */
    lw(a5, -52, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 40);             /* GGGG */
    call($branch_setKeyData);                      /* XXXX */
    ld(a0, s0, 40);             /* GGGG */
    call($branch_inc);                      /* XXXX */
    nop();        /* ZZZZ */
    ld(ra, sp, 56);             /* GGGG */
    ld(s0, sp, 48);             /* GGGG */
    addi(sp, sp, 64);           /* LLLL */
    jr(ra);                       /* UUUU */
    $branch_removeElementAt.set();
    addi(sp, sp, -80);           /* LLLL */
    sd(ra, 72, sp);             /* OOOO */
    sd(s0, 64, sp);             /* OOOO */
    addi(s0, sp, 80);           /* LLLL */
    sd(a0, -56, s0);             /* OOOO */
    sd(a1, -64, s0);             /* OOOO */
    mv(a5,a2);        /* ZZZZ */
    sw(a5, -68, s0);             /* OOOO */
    addi(a5, s0, -48);           /* LLLL */
    mv(a0,a5);        /* ZZZZ */
    call($branch_result);                      /* XXXX */
    lw(a5, -68, s0);             /* SSSS */
    sw(a5, -40, s0);             /* OOOO */
    lw(a5, -68, s0);             /* SSSS */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 64);             /* GGGG */
    call($branch_key);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -36, s0);             /* OOOO */
    lw(a5, -68, s0);             /* SSSS */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 64);             /* GGGG */
    call($branch_data);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -32, s0);             /* OOOO */
    lw(a5, -68, s0);             /* SSSS */
    sw(a5, -20, s0);             /* OOOO */
    ld(a0, s0, 64);             /* GGGG */
    call($branch_size);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    addiw(a5, a5, -1);           /* LLLL */
    sw(a5, -24, s0);             /* OOOO */
    j($L39);                       /* WWWW */
    $L40.set();
    lw(a5, -20, s0);             /* SSSS */
    addiw(a5, a5, 1);           /* LLLL */
    sextw(a4,a5);        /* ZZZZ */
    lw(a5, -20, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 64);             /* GGGG */
    call($branch_copyKeyData);                      /* XXXX */
    lw(a5, -20, s0);             /* SSSS */
    addiw(a5, a5, 1);           /* LLLL */
    sw(a5, -20, s0);             /* OOOO */
    $L39.set();
    lw(a5, -20, s0);             /* SSSS */
    mv(a4,a5);        /* ZZZZ */
    lw(a5, -24, s0);             /* SSSS */
    sextw(a4,a4);        /* ZZZZ */
    sextw(a5,a5);        /* ZZZZ */
    blt(a4, a5, $L40);             /* YYYY */
    ld(a0, s0, 64);             /* GGGG */
    call($branch_dec);                      /* XXXX */
    ld(a5, s0, 56);             /* GGGG */
    lw(a4, -48, s0);             /* SSSS */
    mv(a0,a4);        /* ZZZZ */
    lw(a4, -44, s0);             /* SSSS */
    mv(a1,a4);        /* ZZZZ */
    lw(a4, -40, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    lw(a4, -36, s0);             /* SSSS */
    mv(a3,a4);        /* ZZZZ */
    lw(a4, -32, s0);             /* SSSS */
    sw(a0, 0, a5);             /* OOOO */
    sw(a1, 4, a5);             /* OOOO */
    sw(a2, 8, a5);             /* OOOO */
    sw(a3, 12, a5);             /* OOOO */
    sw(a4, 16, a5);             /* OOOO */
    ld(a0, s0, 56);             /* GGGG */
    ld(ra, sp, 72);             /* GGGG */
    ld(s0, sp, 64);             /* GGGG */
    addi(sp, sp, 80);           /* LLLL */
    jr(ra);                       /* UUUU */
    $branch_firstElement.set();
    addi(sp, sp, -64);           /* LLLL */
    sd(ra, 56, sp);             /* OOOO */
    sd(s0, 48, sp);             /* OOOO */
    addi(s0, sp, 64);           /* LLLL */
    sd(a0, -56, s0);             /* OOOO */
    sd(a1, -64, s0);             /* OOOO */
    addi(a5, s0, -40);           /* LLLL */
    mv(a0,a5);        /* ZZZZ */
    call($branch_result);                      /* XXXX */
    ld(a0, s0, 64);             /* GGGG */
    call($branch_isEmpty);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    seqz(a5, a5);                 /* YY11 */
    andi(a5,a5,0xff);        /* ZZZZ */
    sextw(a5,a5);        /* ZZZZ */
    sw(a5, -36, s0);             /* OOOO */
    lw(a5, -36, s0);             /* SSSS */
    beq(a5, zero, $L43);             /* YYYY */
    sw(zero, -32, s0);             /* OOOO */
    li(a1,0);        /* ZZZZ */
    ld(a0, s0, 64);             /* GGGG */
    call($branch_key);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -28, s0);             /* OOOO */
    li(a1,0);        /* ZZZZ */
    ld(a0, s0, 64);             /* GGGG */
    call($branch_data);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -24, s0);             /* OOOO */
    $L43.set();
    ld(a5, s0, 56);             /* GGGG */
    lw(a4, -40, s0);             /* SSSS */
    mv(a0,a4);        /* ZZZZ */
    lw(a4, -36, s0);             /* SSSS */
    mv(a1,a4);        /* ZZZZ */
    lw(a4, -32, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    lw(a4, -28, s0);             /* SSSS */
    mv(a3,a4);        /* ZZZZ */
    lw(a4, -24, s0);             /* SSSS */
    sw(a0, 0, a5);             /* OOOO */
    sw(a1, 4, a5);             /* OOOO */
    sw(a2, 8, a5);             /* OOOO */
    sw(a3, 12, a5);             /* OOOO */
    sw(a4, 16, a5);             /* OOOO */
    ld(a0, s0, 56);             /* GGGG */
    ld(ra, sp, 56);             /* GGGG */
    ld(s0, sp, 48);             /* GGGG */
    addi(sp, sp, 64);           /* LLLL */
    jr(ra);                       /* UUUU */
    $branch_lastElement.set();
    addi(sp, sp, -64);           /* LLLL */
    sd(ra, 56, sp);             /* OOOO */
    sd(s0, 48, sp);             /* OOOO */
    addi(s0, sp, 64);           /* LLLL */
    sd(a0, -56, s0);             /* OOOO */
    sd(a1, -64, s0);             /* OOOO */
    addi(a5, s0, -40);           /* LLLL */
    mv(a0,a5);        /* ZZZZ */
    call($branch_result);                      /* XXXX */
    ld(a0, s0, 64);             /* GGGG */
    call($branch_isEmpty);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    seqz(a5, a5);                 /* YY11 */
    andi(a5,a5,0xff);        /* ZZZZ */
    sextw(a5,a5);        /* ZZZZ */
    sw(a5, -36, s0);             /* OOOO */
    ld(a0, s0, 64);             /* GGGG */
    call($branch_size);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    addiw(a5, a5, -1);           /* LLLL */
    sw(a5, -20, s0);             /* OOOO */
    lw(a5, -36, s0);             /* SSSS */
    beq(a5, zero, $L46);             /* YYYY */
    lw(a5, -20, s0);             /* SSSS */
    sw(a5, -32, s0);             /* OOOO */
    lw(a5, -20, s0);             /* SSSS */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 64);             /* GGGG */
    call($branch_key);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -28, s0);             /* OOOO */
    lw(a5, -20, s0);             /* SSSS */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 64);             /* GGGG */
    call($branch_data);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -24, s0);             /* OOOO */
    $L46.set();
    ld(a5, s0, 56);             /* GGGG */
    lw(a4, -40, s0);             /* SSSS */
    mv(a0,a4);        /* ZZZZ */
    lw(a4, -36, s0);             /* SSSS */
    mv(a1,a4);        /* ZZZZ */
    lw(a4, -32, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    lw(a4, -28, s0);             /* SSSS */
    mv(a3,a4);        /* ZZZZ */
    lw(a4, -24, s0);             /* SSSS */
    sw(a0, 0, a5);             /* OOOO */
    sw(a1, 4, a5);             /* OOOO */
    sw(a2, 8, a5);             /* OOOO */
    sw(a3, 12, a5);             /* OOOO */
    sw(a4, 16, a5);             /* OOOO */
    ld(a0, s0, 56);             /* GGGG */
    ld(ra, sp, 56);             /* GGGG */
    ld(s0, sp, 48);             /* GGGG */
    addi(sp, sp, 64);           /* LLLL */
    jr(ra);                       /* UUUU */
    $branch_searchFirstGreaterThanOrEqualExceptLast.set();
    addi(sp, sp, -80);           /* LLLL */
    sd(ra, 72, sp);             /* OOOO */
    sd(s0, 64, sp);             /* OOOO */
    addi(s0, sp, 80);           /* LLLL */
    sd(a0, -56, s0);             /* OOOO */
    sd(a1, -64, s0);             /* OOOO */
    mv(a5,a2);        /* ZZZZ */
    sw(a5, -68, s0);             /* OOOO */
    addi(a5, s0, -48);           /* LLLL */
    mv(a0,a5);        /* ZZZZ */
    call($branch_result);                      /* XXXX */
    lw(a5, -68, s0);             /* SSSS */
    sw(a5, -48, s0);             /* OOOO */
    ld(a0, s0, 64);             /* GGGG */
    call($branch_size);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    addiw(a5, a5, -1);           /* LLLL */
    sw(a5, -24, s0);             /* OOOO */
    sw(zero, -20, s0);             /* OOOO */
    lw(a5, -24, s0);             /* SSSS */
    sw(a5, -28, s0);             /* OOOO */
    j($L49);                       /* WWWW */
    $L52.set();
    lw(a5, -20, s0);             /* SSSS */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 64);             /* GGGG */
    call($branch_key);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    mv(a4,a5);        /* ZZZZ */
    lw(a5, -68, s0);             /* SSSS */
    sextw(a5,a5);        /* ZZZZ */
    bgt(a5, a4, $L50);             /* YYYY */
    li(a5,1);        /* ZZZZ */
    sw(a5, -44, s0);             /* OOOO */
    lw(a5, -20, s0);             /* SSSS */
    sw(a5, -40, s0);             /* OOOO */
    lw(a5, -20, s0);             /* SSSS */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 64);             /* GGGG */
    call($branch_key);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -36, s0);             /* OOOO */
    lw(a5, -20, s0);             /* SSSS */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 64);             /* GGGG */
    call($branch_data);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -32, s0);             /* OOOO */
    ld(a5, s0, 56);             /* GGGG */
    lw(a4, -48, s0);             /* SSSS */
    mv(a0,a4);        /* ZZZZ */
    lw(a4, -44, s0);             /* SSSS */
    mv(a1,a4);        /* ZZZZ */
    lw(a4, -40, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    lw(a4, -36, s0);             /* SSSS */
    mv(a3,a4);        /* ZZZZ */
    lw(a4, -32, s0);             /* SSSS */
    sw(a0, 0, a5);             /* OOOO */
    sw(a1, 4, a5);             /* OOOO */
    sw(a2, 8, a5);             /* OOOO */
    sw(a3, 12, a5);             /* OOOO */
    sw(a4, 16, a5);             /* OOOO */
    j($L53);                       /* WWWW */
    $L50.set();
    lw(a5, -20, s0);             /* SSSS */
    addiw(a5, a5, 1);           /* LLLL */
    sw(a5, -20, s0);             /* OOOO */
    $L49.set();
    lw(a5, -20, s0);             /* SSSS */
    mv(a4,a5);        /* ZZZZ */
    lw(a5, -28, s0);             /* SSSS */
    sextw(a4,a4);        /* ZZZZ */
    sextw(a5,a5);        /* ZZZZ */
    blt(a4, a5, $L52);             /* YYYY */
    li(a5,1);        /* ZZZZ */
    sw(a5, -44, s0);             /* OOOO */
    lw(a5, -24, s0);             /* SSSS */
    sw(a5, -40, s0);             /* OOOO */
    sw(zero, -36, s0);             /* OOOO */
    lw(a5, -24, s0);             /* SSSS */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 64);             /* GGGG */
    call($branch_data);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -32, s0);             /* OOOO */
    ld(a5, s0, 56);             /* GGGG */
    lw(a4, -48, s0);             /* SSSS */
    mv(a0,a4);        /* ZZZZ */
    lw(a4, -44, s0);             /* SSSS */
    mv(a1,a4);        /* ZZZZ */
    lw(a4, -40, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    lw(a4, -36, s0);             /* SSSS */
    mv(a3,a4);        /* ZZZZ */
    lw(a4, -32, s0);             /* SSSS */
    sw(a0, 0, a5);             /* OOOO */
    sw(a1, 4, a5);             /* OOOO */
    sw(a2, 8, a5);             /* OOOO */
    sw(a3, 12, a5);             /* OOOO */
    sw(a4, 16, a5);             /* OOOO */
    $L53.set();
    ld(a0, s0, 56);             /* GGGG */
    ld(ra, sp, 72);             /* GGGG */
    ld(s0, sp, 64);             /* GGGG */
    addi(sp, sp, 80);           /* LLLL */
    jr(ra);                       /* UUUU */
    $leaf_size.set();
    addi(sp, sp, -32);           /* LLLL */
    sd(s0, 24, sp);             /* OOOO */
    addi(s0, sp, 32);           /* LLLL */
    sd(a0, -24, s0);             /* OOOO */
    ld(a5, s0, 24);             /* GGGG */
    lw(a5, 0, a5);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    ld(s0, sp, 24);             /* GGGG */
    addi(sp, sp, 32);           /* LLLL */
    jr(ra);                       /* UUUU */
    $leaf_isEmpty.set();
    addi(sp, sp, -32);           /* LLLL */
    sd(ra, 24, sp);             /* OOOO */
    sd(s0, 16, sp);             /* OOOO */
    addi(s0, sp, 32);           /* LLLL */
    sd(a0, -24, s0);             /* OOOO */
    ld(a0, s0, 24);             /* GGGG */
    call($leaf_size);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    seqz(a5, a5);                 /* YY11 */
    andi(a5,a5,0xff);        /* ZZZZ */
    sextw(a5,a5);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    ld(ra, sp, 24);             /* GGGG */
    ld(s0, sp, 16);             /* GGGG */
    addi(sp, sp, 32);           /* LLLL */
    jr(ra);                       /* UUUU */
    $leaf_key.set();
    addi(sp, sp, -32);           /* LLLL */
    sd(s0, 24, sp);             /* OOOO */
    addi(s0, sp, 32);           /* LLLL */
    sd(a0, -24, s0);             /* OOOO */
    mv(a5,a1);        /* ZZZZ */
    sw(a5, -28, s0);             /* OOOO */
    ld(a4, s0, 24);             /* GGGG */
    lw(a5, -28, s0);             /* SSSS */
    slli(a5,a5,2);        /* ZZZZ */
    add(a5,a4,a5);        /* ZZZZ */
    lw(a5, 4, a5);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    ld(s0, sp, 24);             /* GGGG */
    addi(sp, sp, 32);           /* LLLL */
    jr(ra);                       /* UUUU */
    $leaf_data.set();
    addi(sp, sp, -32);           /* LLLL */
    sd(s0, 24, sp);             /* OOOO */
    addi(s0, sp, 32);           /* LLLL */
    sd(a0, -24, s0);             /* OOOO */
    mv(a5,a1);        /* ZZZZ */
    sw(a5, -28, s0);             /* OOOO */
    ld(a4, s0, 24);             /* GGGG */
    lw(a5, -28, s0);             /* SSSS */
    add(a5,a4,a5);        /* ZZZZ */
    lbu(a5, 12, a5);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    ld(s0, sp, 24);             /* GGGG */
    addi(sp, sp, 32);           /* LLLL */
    jr(ra);                       /* UUUU */
    $leaf_setKey.set();
    addi(sp, sp, -32);           /* LLLL */
    sd(s0, 24, sp);             /* OOOO */
    addi(s0, sp, 32);           /* LLLL */
    sd(a0, -24, s0);             /* OOOO */
    mv(a5,a1);        /* ZZZZ */
    mv(a4,a2);        /* ZZZZ */
    sw(a5, -28, s0);             /* OOOO */
    mv(a5,a4);        /* ZZZZ */
    sw(a5, -32, s0);             /* OOOO */
    ld(a4, s0, 24);             /* GGGG */
    lw(a5, -28, s0);             /* SSSS */
    slli(a5,a5,2);        /* ZZZZ */
    add(a5,a4,a5);        /* ZZZZ */
    lw(a4, -32, s0);             /* SSSS */
    sw(a4, 4, a5);             /* OOOO */
    nop();        /* ZZZZ */
    ld(s0, sp, 24);             /* GGGG */
    addi(sp, sp, 32);           /* LLLL */
    jr(ra);                       /* UUUU */
    $leaf_setData.set();
    addi(sp, sp, -32);           /* LLLL */
    sd(s0, 24, sp);             /* OOOO */
    addi(s0, sp, 32);           /* LLLL */
    sd(a0, -24, s0);             /* OOOO */
    mv(a5,a1);        /* ZZZZ */
    mv(a4,a2);        /* ZZZZ */
    sw(a5, -28, s0);             /* OOOO */
    mv(a5,a4);        /* ZZZZ */
    sw(a5, -32, s0);             /* OOOO */
    lw(a5, -32, s0);             /* SSSS */
    andi(a4,a5,0xff);        /* ZZZZ */
    ld(a3, s0, 24);             /* GGGG */
    lw(a5, -28, s0);             /* SSSS */
    add(a5,a3,a5);        /* ZZZZ */
    sb(a4, 12, a5);             /* OOOO */
    nop();        /* ZZZZ */
    ld(s0, sp, 24);             /* GGGG */
    addi(sp, sp, 32);           /* LLLL */
    jr(ra);                       /* UUUU */
    $leaf_copyKey.set();
    addi(sp, sp, -32);           /* LLLL */
    sd(s0, 24, sp);             /* OOOO */
    addi(s0, sp, 32);           /* LLLL */
    sd(a0, -24, s0);             /* OOOO */
    mv(a5,a1);        /* ZZZZ */
    mv(a4,a2);        /* ZZZZ */
    sw(a5, -28, s0);             /* OOOO */
    mv(a5,a4);        /* ZZZZ */
    sw(a5, -32, s0);             /* OOOO */
    ld(a4, s0, 24);             /* GGGG */
    lw(a5, -32, s0);             /* SSSS */
    slli(a5,a5,2);        /* ZZZZ */
    add(a5,a4,a5);        /* ZZZZ */
    lw(a4, 4, a5);             /* SSSS */
    ld(a3, s0, 24);             /* GGGG */
    lw(a5, -28, s0);             /* SSSS */
    slli(a5,a5,2);        /* ZZZZ */
    add(a5,a3,a5);        /* ZZZZ */
    sw(a4, 4, a5);             /* OOOO */
    nop();        /* ZZZZ */
    ld(s0, sp, 24);             /* GGGG */
    addi(sp, sp, 32);           /* LLLL */
    jr(ra);                       /* UUUU */
    $leaf_copyData.set();
    addi(sp, sp, -32);           /* LLLL */
    sd(s0, 24, sp);             /* OOOO */
    addi(s0, sp, 32);           /* LLLL */
    sd(a0, -24, s0);             /* OOOO */
    mv(a5,a1);        /* ZZZZ */
    mv(a4,a2);        /* ZZZZ */
    sw(a5, -28, s0);             /* OOOO */
    mv(a5,a4);        /* ZZZZ */
    sw(a5, -32, s0);             /* OOOO */
    ld(a4, s0, 24);             /* GGGG */
    lw(a5, -32, s0);             /* SSSS */
    add(a5,a4,a5);        /* ZZZZ */
    lbu(a4, 12, a5);             /* SSSS */
    ld(a3, s0, 24);             /* GGGG */
    lw(a5, -28, s0);             /* SSSS */
    add(a5,a3,a5);        /* ZZZZ */
    sb(a4, 12, a5);             /* OOOO */
    nop();        /* ZZZZ */
    ld(s0, sp, 24);             /* GGGG */
    addi(sp, sp, 32);           /* LLLL */
    jr(ra);                       /* UUUU */
    $leaf_setKeyData.set();
    addi(sp, sp, -48);           /* LLLL */
    sd(ra, 40, sp);             /* OOOO */
    sd(s0, 32, sp);             /* OOOO */
    addi(s0, sp, 48);           /* LLLL */
    sd(a0, -24, s0);             /* OOOO */
    mv(a5,a1);        /* ZZZZ */
    mv(a4,a3);        /* ZZZZ */
    sw(a5, -28, s0);             /* OOOO */
    mv(a5,a2);        /* ZZZZ */
    sw(a5, -32, s0);             /* OOOO */
    mv(a5,a4);        /* ZZZZ */
    sw(a5, -36, s0);             /* OOOO */
    lw(a4, -32, s0);             /* SSSS */
    lw(a5, -28, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 24);             /* GGGG */
    call($leaf_setKey);                      /* XXXX */
    lw(a4, -36, s0);             /* SSSS */
    lw(a5, -28, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 24);             /* GGGG */
    call($leaf_setData);                      /* XXXX */
    nop();        /* ZZZZ */
    ld(ra, sp, 40);             /* GGGG */
    ld(s0, sp, 32);             /* GGGG */
    addi(sp, sp, 48);           /* LLLL */
    jr(ra);                       /* UUUU */
    $leaf_copyKeyData.set();
    addi(sp, sp, -32);           /* LLLL */
    sd(ra, 24, sp);             /* OOOO */
    sd(s0, 16, sp);             /* OOOO */
    addi(s0, sp, 32);           /* LLLL */
    sd(a0, -24, s0);             /* OOOO */
    mv(a5,a1);        /* ZZZZ */
    mv(a4,a2);        /* ZZZZ */
    sw(a5, -28, s0);             /* OOOO */
    mv(a5,a4);        /* ZZZZ */
    sw(a5, -32, s0);             /* OOOO */
    lw(a4, -32, s0);             /* SSSS */
    lw(a5, -28, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 24);             /* GGGG */
    call($leaf_copyKey);                      /* XXXX */
    lw(a4, -32, s0);             /* SSSS */
    lw(a5, -28, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 24);             /* GGGG */
    call($leaf_copyData);                      /* XXXX */
    nop();        /* ZZZZ */
    ld(ra, sp, 24);             /* GGGG */
    ld(s0, sp, 16);             /* GGGG */
    addi(sp, sp, 32);           /* LLLL */
    jr(ra);                       /* UUUU */
    $leaf_inc.set();
    addi(sp, sp, -32);           /* LLLL */
    sd(s0, 24, sp);             /* OOOO */
    addi(s0, sp, 32);           /* LLLL */
    sd(a0, -24, s0);             /* OOOO */
    ld(a5, s0, 24);             /* GGGG */
    lw(a5, 0, a5);             /* SSSS */
    addiw(a5, a5, 1);           /* LLLL */
    sextw(a4,a5);        /* ZZZZ */
    ld(a5, s0, 24);             /* GGGG */
    sw(a4, 0, a5);             /* OOOO */
    nop();        /* ZZZZ */
    ld(s0, sp, 24);             /* GGGG */
    addi(sp, sp, 32);           /* LLLL */
    jr(ra);                       /* UUUU */
    $leaf_dec.set();
    addi(sp, sp, -32);           /* LLLL */
    sd(s0, 24, sp);             /* OOOO */
    addi(s0, sp, 32);           /* LLLL */
    sd(a0, -24, s0);             /* OOOO */
    ld(a5, s0, 24);             /* GGGG */
    lw(a5, 0, a5);             /* SSSS */
    addiw(a5, a5, -1);           /* LLLL */
    sextw(a4,a5);        /* ZZZZ */
    ld(a5, s0, 24);             /* GGGG */
    sw(a4, 0, a5);             /* OOOO */
    nop();        /* ZZZZ */
    ld(s0, sp, 24);             /* GGGG */
    addi(sp, sp, 32);           /* LLLL */
    jr(ra);                       /* UUUU */
    $leaf_push.set();
    addi(sp, sp, -48);           /* LLLL */
    sd(ra, 40, sp);             /* OOOO */
    sd(s0, 32, sp);             /* OOOO */
    addi(s0, sp, 48);           /* LLLL */
    sd(a0, -40, s0);             /* OOOO */
    mv(a5,a1);        /* ZZZZ */
    mv(a4,a2);        /* ZZZZ */
    sw(a5, -44, s0);             /* OOOO */
    mv(a5,a4);        /* ZZZZ */
    sw(a5, -48, s0);             /* OOOO */
    ld(a0, s0, 40);             /* GGGG */
    call($leaf_size);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -20, s0);             /* OOOO */
    lw(a3, -48, s0);             /* SSSS */
    lw(a4, -44, s0);             /* SSSS */
    lw(a5, -20, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 40);             /* GGGG */
    call($leaf_setKeyData);                      /* XXXX */
    ld(a0, s0, 40);             /* GGGG */
    call($leaf_inc);                      /* XXXX */
    nop();        /* ZZZZ */
    ld(ra, sp, 40);             /* GGGG */
    ld(s0, sp, 32);             /* GGGG */
    addi(sp, sp, 48);           /* LLLL */
    jr(ra);                       /* UUUU */
    $leaf_result.set();
    addi(sp, sp, -64);           /* LLLL */
    sd(s0, 56, sp);             /* OOOO */
    addi(s0, sp, 64);           /* LLLL */
    sd(a0, -56, s0);             /* OOOO */
    sb(zero, -24, s0);             /* OOOO */
    sw(zero, -28, s0);             /* OOOO */
    lw(a5, -28, s0);             /* SSSS */
    sw(a5, -32, s0);             /* OOOO */
    lw(a5, -32, s0);             /* SSSS */
    sw(a5, -36, s0);             /* OOOO */
    lw(a5, -36, s0);             /* SSSS */
    sw(a5, -40, s0);             /* OOOO */
    ld(a5, s0, 56);             /* GGGG */
    lw(a4, -40, s0);             /* SSSS */
    mv(a0,a4);        /* ZZZZ */
    lw(a4, -36, s0);             /* SSSS */
    mv(a1,a4);        /* ZZZZ */
    lw(a4, -32, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    lw(a4, -28, s0);             /* SSSS */
    mv(a3,a4);        /* ZZZZ */
    lw(a4, -24, s0);             /* SSSS */
    sw(a0, 0, a5);             /* OOOO */
    sw(a1, 4, a5);             /* OOOO */
    sw(a2, 8, a5);             /* OOOO */
    sw(a3, 12, a5);             /* OOOO */
    sw(a4, 16, a5);             /* OOOO */
    ld(a0, s0, 56);             /* GGGG */
    ld(s0, sp, 56);             /* GGGG */
    addi(sp, sp, 64);           /* LLLL */
    jr(ra);                       /* UUUU */
    $leaf_pop.set();
    addi(sp, sp, -64);           /* LLLL */
    sd(ra, 56, sp);             /* OOOO */
    sd(s0, 48, sp);             /* OOOO */
    addi(s0, sp, 64);           /* LLLL */
    sd(a0, -56, s0);             /* OOOO */
    sd(a1, -64, s0);             /* OOOO */
    addi(a5, s0, -40);           /* LLLL */
    mv(a0,a5);        /* ZZZZ */
    call($leaf_result);                      /* XXXX */
    ld(a0, s0, 64);             /* GGGG */
    call($leaf_dec);                      /* XXXX */
    ld(a0, s0, 64);             /* GGGG */
    call($leaf_size);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -32, s0);             /* OOOO */
    lw(a5, -32, s0);             /* SSSS */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 64);             /* GGGG */
    call($leaf_key);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -28, s0);             /* OOOO */
    lw(a5, -32, s0);             /* SSSS */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 64);             /* GGGG */
    call($leaf_data);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sb(a5, -24, s0);             /* OOOO */
    ld(a5, s0, 56);             /* GGGG */
    lw(a4, -40, s0);             /* SSSS */
    mv(a0,a4);        /* ZZZZ */
    lw(a4, -36, s0);             /* SSSS */
    mv(a1,a4);        /* ZZZZ */
    lw(a4, -32, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    lw(a4, -28, s0);             /* SSSS */
    mv(a3,a4);        /* ZZZZ */
    lw(a4, -24, s0);             /* SSSS */
    sw(a0, 0, a5);             /* OOOO */
    sw(a1, 4, a5);             /* OOOO */
    sw(a2, 8, a5);             /* OOOO */
    sw(a3, 12, a5);             /* OOOO */
    sw(a4, 16, a5);             /* OOOO */
    ld(a0, s0, 56);             /* GGGG */
    ld(ra, sp, 56);             /* GGGG */
    ld(s0, sp, 48);             /* GGGG */
    addi(sp, sp, 64);           /* LLLL */
    jr(ra);                       /* UUUU */
    $leaf_shift.set();
    addi(sp, sp, -64);           /* LLLL */
    sd(ra, 56, sp);             /* OOOO */
    sd(s0, 48, sp);             /* OOOO */
    addi(s0, sp, 64);           /* LLLL */
    sd(a0, -56, s0);             /* OOOO */
    sd(a1, -64, s0);             /* OOOO */
    addi(a5, s0, -48);           /* LLLL */
    mv(a0,a5);        /* ZZZZ */
    call($leaf_result);                      /* XXXX */
    li(a1,0);        /* ZZZZ */
    ld(a0, s0, 64);             /* GGGG */
    call($leaf_key);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -36, s0);             /* OOOO */
    li(a1,0);        /* ZZZZ */
    ld(a0, s0, 64);             /* GGGG */
    call($leaf_data);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sb(a5, -32, s0);             /* OOOO */
    sw(zero, -20, s0);             /* OOOO */
    ld(a0, s0, 64);             /* GGGG */
    call($leaf_size);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    addiw(a5, a5, -1);           /* LLLL */
    sw(a5, -24, s0);             /* OOOO */
    j($L76);                       /* WWWW */
    $L77.set();
    lw(a5, -20, s0);             /* SSSS */
    addiw(a5, a5, 1);           /* LLLL */
    sextw(a4,a5);        /* ZZZZ */
    lw(a5, -20, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 64);             /* GGGG */
    call($leaf_copyKeyData);                      /* XXXX */
    lw(a5, -20, s0);             /* SSSS */
    addiw(a5, a5, 1);           /* LLLL */
    sw(a5, -20, s0);             /* OOOO */
    $L76.set();
    lw(a5, -20, s0);             /* SSSS */
    mv(a4,a5);        /* ZZZZ */
    lw(a5, -24, s0);             /* SSSS */
    sextw(a4,a4);        /* ZZZZ */
    sextw(a5,a5);        /* ZZZZ */
    blt(a4, a5, $L77);             /* YYYY */
    ld(a0, s0, 64);             /* GGGG */
    call($leaf_dec);                      /* XXXX */
    ld(a5, s0, 56);             /* GGGG */
    lw(a4, -48, s0);             /* SSSS */
    mv(a0,a4);        /* ZZZZ */
    lw(a4, -44, s0);             /* SSSS */
    mv(a1,a4);        /* ZZZZ */
    lw(a4, -40, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    lw(a4, -36, s0);             /* SSSS */
    mv(a3,a4);        /* ZZZZ */
    lw(a4, -32, s0);             /* SSSS */
    sw(a0, 0, a5);             /* OOOO */
    sw(a1, 4, a5);             /* OOOO */
    sw(a2, 8, a5);             /* OOOO */
    sw(a3, 12, a5);             /* OOOO */
    sw(a4, 16, a5);             /* OOOO */
    ld(a0, s0, 56);             /* GGGG */
    ld(ra, sp, 56);             /* GGGG */
    ld(s0, sp, 48);             /* GGGG */
    addi(sp, sp, 64);           /* LLLL */
    jr(ra);                       /* UUUU */
    $leaf_elementAt.set();
    addi(sp, sp, -80);           /* LLLL */
    sd(ra, 72, sp);             /* OOOO */
    sd(s0, 64, sp);             /* OOOO */
    addi(s0, sp, 80);           /* LLLL */
    sd(a0, -56, s0);             /* OOOO */
    sd(a1, -64, s0);             /* OOOO */
    mv(a5,a2);        /* ZZZZ */
    sw(a5, -68, s0);             /* OOOO */
    addi(a5, s0, -40);           /* LLLL */
    mv(a0,a5);        /* ZZZZ */
    call($leaf_result);                      /* XXXX */
    lw(a5, -68, s0);             /* SSSS */
    sw(a5, -32, s0);             /* OOOO */
    lw(a5, -68, s0);             /* SSSS */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 64);             /* GGGG */
    call($leaf_key);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -28, s0);             /* OOOO */
    lw(a5, -68, s0);             /* SSSS */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 64);             /* GGGG */
    call($leaf_data);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sb(a5, -24, s0);             /* OOOO */
    ld(a5, s0, 56);             /* GGGG */
    lw(a4, -40, s0);             /* SSSS */
    mv(a0,a4);        /* ZZZZ */
    lw(a4, -36, s0);             /* SSSS */
    mv(a1,a4);        /* ZZZZ */
    lw(a4, -32, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    lw(a4, -28, s0);             /* SSSS */
    mv(a3,a4);        /* ZZZZ */
    lw(a4, -24, s0);             /* SSSS */
    sw(a0, 0, a5);             /* OOOO */
    sw(a1, 4, a5);             /* OOOO */
    sw(a2, 8, a5);             /* OOOO */
    sw(a3, 12, a5);             /* OOOO */
    sw(a4, 16, a5);             /* OOOO */
    ld(a0, s0, 56);             /* GGGG */
    ld(ra, sp, 72);             /* GGGG */
    ld(s0, sp, 64);             /* GGGG */
    addi(sp, sp, 80);           /* LLLL */
    jr(ra);                       /* UUUU */
    $leaf_setElementAt.set();
    addi(sp, sp, -48);           /* LLLL */
    sd(ra, 40, sp);             /* OOOO */
    sd(s0, 32, sp);             /* OOOO */
    addi(s0, sp, 48);           /* LLLL */
    sd(a0, -24, s0);             /* OOOO */
    mv(a5,a1);        /* ZZZZ */
    mv(a4,a3);        /* ZZZZ */
    sw(a5, -28, s0);             /* OOOO */
    mv(a5,a2);        /* ZZZZ */
    sw(a5, -32, s0);             /* OOOO */
    mv(a5,a4);        /* ZZZZ */
    sw(a5, -36, s0);             /* OOOO */
    ld(a0, s0, 24);             /* GGGG */
    call($leaf_size);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    mv(a4,a5);        /* ZZZZ */
    lw(a5, -36, s0);             /* SSSS */
    sextw(a5,a5);        /* ZZZZ */
    bne(a5, a4, $L82);             /* YYYY */
    lw(a3, -32, s0);             /* SSSS */
    lw(a4, -28, s0);             /* SSSS */
    lw(a5, -36, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 24);             /* GGGG */
    call($leaf_setKeyData);                      /* XXXX */
    ld(a0, s0, 24);             /* GGGG */
    call($leaf_inc);                      /* XXXX */
    j($L84);                       /* WWWW */
    $L82.set();
    lw(a3, -32, s0);             /* SSSS */
    lw(a4, -28, s0);             /* SSSS */
    lw(a5, -36, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 24);             /* GGGG */
    call($leaf_setKeyData);                      /* XXXX */
    $L84.set();
    nop();        /* ZZZZ */
    ld(ra, sp, 40);             /* GGGG */
    ld(s0, sp, 32);             /* GGGG */
    addi(sp, sp, 48);           /* LLLL */
    jr(ra);                       /* UUUU */
    $leaf_insertElementAt.set();
    addi(sp, sp, -64);           /* LLLL */
    sd(ra, 56, sp);             /* OOOO */
    sd(s0, 48, sp);             /* OOOO */
    addi(s0, sp, 64);           /* LLLL */
    sd(a0, -40, s0);             /* OOOO */
    mv(a5,a1);        /* ZZZZ */
    mv(a4,a3);        /* ZZZZ */
    sw(a5, -44, s0);             /* OOOO */
    mv(a5,a2);        /* ZZZZ */
    sw(a5, -48, s0);             /* OOOO */
    mv(a5,a4);        /* ZZZZ */
    sw(a5, -52, s0);             /* OOOO */
    ld(a0, s0, 40);             /* GGGG */
    call($leaf_size);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -20, s0);             /* OOOO */
    j($L86);                       /* WWWW */
    $L87.set();
    lw(a5, -20, s0);             /* SSSS */
    addiw(a5, a5, -1);           /* LLLL */
    sextw(a4,a5);        /* ZZZZ */
    lw(a5, -20, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 40);             /* GGGG */
    call($leaf_copyKeyData);                      /* XXXX */
    lw(a5, -20, s0);             /* SSSS */
    addiw(a5, a5, -1);           /* LLLL */
    sw(a5, -20, s0);             /* OOOO */
    $L86.set();
    lw(a5, -20, s0);             /* SSSS */
    mv(a4,a5);        /* ZZZZ */
    lw(a5, -52, s0);             /* SSSS */
    sextw(a4,a4);        /* ZZZZ */
    sextw(a5,a5);        /* ZZZZ */
    bgt(a4, a5, $L87);             /* YYYY */
    lw(a3, -48, s0);             /* SSSS */
    lw(a4, -44, s0);             /* SSSS */
    lw(a5, -52, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 40);             /* GGGG */
    call($leaf_setKeyData);                      /* XXXX */
    ld(a0, s0, 40);             /* GGGG */
    call($leaf_inc);                      /* XXXX */
    nop();        /* ZZZZ */
    ld(ra, sp, 56);             /* GGGG */
    ld(s0, sp, 48);             /* GGGG */
    addi(sp, sp, 64);           /* LLLL */
    jr(ra);                       /* UUUU */
    $leaf_removeElementAt.set();
    addi(sp, sp, -80);           /* LLLL */
    sd(ra, 72, sp);             /* OOOO */
    sd(s0, 64, sp);             /* OOOO */
    addi(s0, sp, 80);           /* LLLL */
    sd(a0, -56, s0);             /* OOOO */
    sd(a1, -64, s0);             /* OOOO */
    mv(a5,a2);        /* ZZZZ */
    sw(a5, -68, s0);             /* OOOO */
    addi(a5, s0, -48);           /* LLLL */
    mv(a0,a5);        /* ZZZZ */
    call($leaf_result);                      /* XXXX */
    lw(a5, -68, s0);             /* SSSS */
    sw(a5, -40, s0);             /* OOOO */
    lw(a5, -68, s0);             /* SSSS */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 64);             /* GGGG */
    call($leaf_key);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -36, s0);             /* OOOO */
    lw(a5, -68, s0);             /* SSSS */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 64);             /* GGGG */
    call($leaf_data);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sb(a5, -32, s0);             /* OOOO */
    lw(a5, -68, s0);             /* SSSS */
    sw(a5, -20, s0);             /* OOOO */
    ld(a0, s0, 64);             /* GGGG */
    call($leaf_size);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    addiw(a5, a5, -1);           /* LLLL */
    sw(a5, -24, s0);             /* OOOO */
    j($L89);                       /* WWWW */
    $L90.set();
    lw(a5, -20, s0);             /* SSSS */
    addiw(a5, a5, 1);           /* LLLL */
    sextw(a4,a5);        /* ZZZZ */
    lw(a5, -20, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 64);             /* GGGG */
    call($leaf_copyKeyData);                      /* XXXX */
    lw(a5, -20, s0);             /* SSSS */
    addiw(a5, a5, 1);           /* LLLL */
    sw(a5, -20, s0);             /* OOOO */
    $L89.set();
    lw(a5, -20, s0);             /* SSSS */
    mv(a4,a5);        /* ZZZZ */
    lw(a5, -24, s0);             /* SSSS */
    sextw(a4,a4);        /* ZZZZ */
    sextw(a5,a5);        /* ZZZZ */
    blt(a4, a5, $L90);             /* YYYY */
    ld(a0, s0, 64);             /* GGGG */
    call($leaf_dec);                      /* XXXX */
    ld(a5, s0, 56);             /* GGGG */
    lw(a4, -48, s0);             /* SSSS */
    mv(a0,a4);        /* ZZZZ */
    lw(a4, -44, s0);             /* SSSS */
    mv(a1,a4);        /* ZZZZ */
    lw(a4, -40, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    lw(a4, -36, s0);             /* SSSS */
    mv(a3,a4);        /* ZZZZ */
    lw(a4, -32, s0);             /* SSSS */
    sw(a0, 0, a5);             /* OOOO */
    sw(a1, 4, a5);             /* OOOO */
    sw(a2, 8, a5);             /* OOOO */
    sw(a3, 12, a5);             /* OOOO */
    sw(a4, 16, a5);             /* OOOO */
    ld(a0, s0, 56);             /* GGGG */
    ld(ra, sp, 72);             /* GGGG */
    ld(s0, sp, 64);             /* GGGG */
    addi(sp, sp, 80);           /* LLLL */
    jr(ra);                       /* UUUU */
    $leaf_firstElement.set();
    addi(sp, sp, -64);           /* LLLL */
    sd(ra, 56, sp);             /* OOOO */
    sd(s0, 48, sp);             /* OOOO */
    addi(s0, sp, 64);           /* LLLL */
    sd(a0, -56, s0);             /* OOOO */
    sd(a1, -64, s0);             /* OOOO */
    addi(a5, s0, -40);           /* LLLL */
    mv(a0,a5);        /* ZZZZ */
    call($leaf_result);                      /* XXXX */
    ld(a0, s0, 64);             /* GGGG */
    call($leaf_isEmpty);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    seqz(a5, a5);                 /* YY11 */
    andi(a5,a5,0xff);        /* ZZZZ */
    sextw(a5,a5);        /* ZZZZ */
    sw(a5, -36, s0);             /* OOOO */
    lw(a5, -36, s0);             /* SSSS */
    beq(a5, zero, $L93);             /* YYYY */
    sw(zero, -32, s0);             /* OOOO */
    li(a1,0);        /* ZZZZ */
    ld(a0, s0, 64);             /* GGGG */
    call($leaf_key);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -28, s0);             /* OOOO */
    li(a1,0);        /* ZZZZ */
    ld(a0, s0, 64);             /* GGGG */
    call($leaf_data);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sb(a5, -24, s0);             /* OOOO */
    $L93.set();
    ld(a5, s0, 56);             /* GGGG */
    lw(a4, -40, s0);             /* SSSS */
    mv(a0,a4);        /* ZZZZ */
    lw(a4, -36, s0);             /* SSSS */
    mv(a1,a4);        /* ZZZZ */
    lw(a4, -32, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    lw(a4, -28, s0);             /* SSSS */
    mv(a3,a4);        /* ZZZZ */
    lw(a4, -24, s0);             /* SSSS */
    sw(a0, 0, a5);             /* OOOO */
    sw(a1, 4, a5);             /* OOOO */
    sw(a2, 8, a5);             /* OOOO */
    sw(a3, 12, a5);             /* OOOO */
    sw(a4, 16, a5);             /* OOOO */
    ld(a0, s0, 56);             /* GGGG */
    ld(ra, sp, 56);             /* GGGG */
    ld(s0, sp, 48);             /* GGGG */
    addi(sp, sp, 64);           /* LLLL */
    jr(ra);                       /* UUUU */
    $leaf_lastElement.set();
    addi(sp, sp, -64);           /* LLLL */
    sd(ra, 56, sp);             /* OOOO */
    sd(s0, 48, sp);             /* OOOO */
    addi(s0, sp, 64);           /* LLLL */
    sd(a0, -56, s0);             /* OOOO */
    sd(a1, -64, s0);             /* OOOO */
    addi(a5, s0, -40);           /* LLLL */
    mv(a0,a5);        /* ZZZZ */
    call($leaf_result);                      /* XXXX */
    ld(a0, s0, 64);             /* GGGG */
    call($leaf_isEmpty);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    seqz(a5, a5);                 /* YY11 */
    andi(a5,a5,0xff);        /* ZZZZ */
    sextw(a5,a5);        /* ZZZZ */
    sw(a5, -36, s0);             /* OOOO */
    ld(a0, s0, 64);             /* GGGG */
    call($leaf_size);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    addiw(a5, a5, -1);           /* LLLL */
    sw(a5, -20, s0);             /* OOOO */
    lw(a5, -36, s0);             /* SSSS */
    beq(a5, zero, $L96);             /* YYYY */
    lw(a5, -20, s0);             /* SSSS */
    sw(a5, -32, s0);             /* OOOO */
    lw(a5, -20, s0);             /* SSSS */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 64);             /* GGGG */
    call($leaf_key);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -28, s0);             /* OOOO */
    lw(a5, -20, s0);             /* SSSS */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 64);             /* GGGG */
    call($leaf_data);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sb(a5, -24, s0);             /* OOOO */
    $L96.set();
    ld(a5, s0, 56);             /* GGGG */
    lw(a4, -40, s0);             /* SSSS */
    mv(a0,a4);        /* ZZZZ */
    lw(a4, -36, s0);             /* SSSS */
    mv(a1,a4);        /* ZZZZ */
    lw(a4, -32, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    lw(a4, -28, s0);             /* SSSS */
    mv(a3,a4);        /* ZZZZ */
    lw(a4, -24, s0);             /* SSSS */
    sw(a0, 0, a5);             /* OOOO */
    sw(a1, 4, a5);             /* OOOO */
    sw(a2, 8, a5);             /* OOOO */
    sw(a3, 12, a5);             /* OOOO */
    sw(a4, 16, a5);             /* OOOO */
    ld(a0, s0, 56);             /* GGGG */
    ld(ra, sp, 56);             /* GGGG */
    ld(s0, sp, 48);             /* GGGG */
    addi(sp, sp, 64);           /* LLLL */
    jr(ra);                       /* UUUU */
    $leaf_search.set();
    addi(sp, sp, -80);           /* LLLL */
    sd(ra, 72, sp);             /* OOOO */
    sd(s0, 64, sp);             /* OOOO */
    addi(s0, sp, 80);           /* LLLL */
    sd(a0, -56, s0);             /* OOOO */
    sd(a1, -64, s0);             /* OOOO */
    mv(a5,a2);        /* ZZZZ */
    sw(a5, -68, s0);             /* OOOO */
    addi(a5, s0, -48);           /* LLLL */
    mv(a0,a5);        /* ZZZZ */
    call($leaf_result);                      /* XXXX */
    lw(a5, -68, s0);             /* SSSS */
    sw(a5, -36, s0);             /* OOOO */
    sw(zero, -20, s0);             /* OOOO */
    ld(a0, s0, 64);             /* GGGG */
    call($leaf_size);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -24, s0);             /* OOOO */
    j($L99);                       /* WWWW */
    $L102.set();
    lw(a5, -20, s0);             /* SSSS */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 64);             /* GGGG */
    call($leaf_key);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    mv(a4,a5);        /* ZZZZ */
    lw(a5, -68, s0);             /* SSSS */
    sextw(a5,a5);        /* ZZZZ */
    bne(a5, a4, $L100);             /* YYYY */
    li(a5,1);        /* ZZZZ */
    sw(a5, -44, s0);             /* OOOO */
    lw(a5, -20, s0);             /* SSSS */
    sw(a5, -40, s0);             /* OOOO */
    lw(a5, -20, s0);             /* SSSS */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 64);             /* GGGG */
    call($leaf_data);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sb(a5, -32, s0);             /* OOOO */
    ld(a5, s0, 56);             /* GGGG */
    lw(a4, -48, s0);             /* SSSS */
    mv(a0,a4);        /* ZZZZ */
    lw(a4, -44, s0);             /* SSSS */
    mv(a1,a4);        /* ZZZZ */
    lw(a4, -40, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    lw(a4, -36, s0);             /* SSSS */
    mv(a3,a4);        /* ZZZZ */
    lw(a4, -32, s0);             /* SSSS */
    sw(a0, 0, a5);             /* OOOO */
    sw(a1, 4, a5);             /* OOOO */
    sw(a2, 8, a5);             /* OOOO */
    sw(a3, 12, a5);             /* OOOO */
    sw(a4, 16, a5);             /* OOOO */
    j($L103);                       /* WWWW */
    $L100.set();
    lw(a5, -20, s0);             /* SSSS */
    addiw(a5, a5, 1);           /* LLLL */
    sw(a5, -20, s0);             /* OOOO */
    $L99.set();
    lw(a5, -20, s0);             /* SSSS */
    mv(a4,a5);        /* ZZZZ */
    lw(a5, -24, s0);             /* SSSS */
    sextw(a4,a4);        /* ZZZZ */
    sextw(a5,a5);        /* ZZZZ */
    blt(a4, a5, $L102);             /* YYYY */
    sw(zero, -44, s0);             /* OOOO */
    ld(a5, s0, 56);             /* GGGG */
    lw(a4, -48, s0);             /* SSSS */
    mv(a0,a4);        /* ZZZZ */
    lw(a4, -44, s0);             /* SSSS */
    mv(a1,a4);        /* ZZZZ */
    lw(a4, -40, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    lw(a4, -36, s0);             /* SSSS */
    mv(a3,a4);        /* ZZZZ */
    lw(a4, -32, s0);             /* SSSS */
    sw(a0, 0, a5);             /* OOOO */
    sw(a1, 4, a5);             /* OOOO */
    sw(a2, 8, a5);             /* OOOO */
    sw(a3, 12, a5);             /* OOOO */
    sw(a4, 16, a5);             /* OOOO */
    $L103.set();
    ld(a0, s0, 56);             /* GGGG */
    ld(ra, sp, 72);             /* GGGG */
    ld(s0, sp, 64);             /* GGGG */
    addi(sp, sp, 80);           /* LLLL */
    jr(ra);                       /* UUUU */
    $leaf_searchFirstGreaterThanOrEqual.set();
    addi(sp, sp, -80);           /* LLLL */
    sd(ra, 72, sp);             /* OOOO */
    sd(s0, 64, sp);             /* OOOO */
    addi(s0, sp, 80);           /* LLLL */
    sd(a0, -56, s0);             /* OOOO */
    sd(a1, -64, s0);             /* OOOO */
    mv(a5,a2);        /* ZZZZ */
    sw(a5, -68, s0);             /* OOOO */
    addi(a5, s0, -48);           /* LLLL */
    mv(a0,a5);        /* ZZZZ */
    call($leaf_result);                      /* XXXX */
    lw(a5, -68, s0);             /* SSSS */
    sw(a5, -48, s0);             /* OOOO */
    sw(zero, -20, s0);             /* OOOO */
    ld(a0, s0, 64);             /* GGGG */
    call($leaf_size);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -24, s0);             /* OOOO */
    j($L105);                       /* WWWW */
    $L108.set();
    lw(a5, -20, s0);             /* SSSS */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 64);             /* GGGG */
    call($leaf_key);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    mv(a4,a5);        /* ZZZZ */
    lw(a5, -68, s0);             /* SSSS */
    sextw(a5,a5);        /* ZZZZ */
    bgt(a5, a4, $L106);             /* YYYY */
    li(a5,1);        /* ZZZZ */
    sw(a5, -44, s0);             /* OOOO */
    lw(a5, -20, s0);             /* SSSS */
    sw(a5, -40, s0);             /* OOOO */
    lw(a5, -20, s0);             /* SSSS */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 64);             /* GGGG */
    call($leaf_key);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -36, s0);             /* OOOO */
    lw(a5, -20, s0);             /* SSSS */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 64);             /* GGGG */
    call($leaf_data);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sb(a5, -32, s0);             /* OOOO */
    ld(a5, s0, 56);             /* GGGG */
    lw(a4, -48, s0);             /* SSSS */
    mv(a0,a4);        /* ZZZZ */
    lw(a4, -44, s0);             /* SSSS */
    mv(a1,a4);        /* ZZZZ */
    lw(a4, -40, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    lw(a4, -36, s0);             /* SSSS */
    mv(a3,a4);        /* ZZZZ */
    lw(a4, -32, s0);             /* SSSS */
    sw(a0, 0, a5);             /* OOOO */
    sw(a1, 4, a5);             /* OOOO */
    sw(a2, 8, a5);             /* OOOO */
    sw(a3, 12, a5);             /* OOOO */
    sw(a4, 16, a5);             /* OOOO */
    j($L109);                       /* WWWW */
    $L106.set();
    lw(a5, -20, s0);             /* SSSS */
    addiw(a5, a5, 1);           /* LLLL */
    sw(a5, -20, s0);             /* OOOO */
    $L105.set();
    lw(a5, -20, s0);             /* SSSS */
    mv(a4,a5);        /* ZZZZ */
    lw(a5, -24, s0);             /* SSSS */
    sextw(a4,a4);        /* ZZZZ */
    sextw(a5,a5);        /* ZZZZ */
    blt(a4, a5, $L108);             /* YYYY */
    sw(zero, -44, s0);             /* OOOO */
    ld(a5, s0, 56);             /* GGGG */
    lw(a4, -48, s0);             /* SSSS */
    mv(a0,a4);        /* ZZZZ */
    lw(a4, -44, s0);             /* SSSS */
    mv(a1,a4);        /* ZZZZ */
    lw(a4, -40, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    lw(a4, -36, s0);             /* SSSS */
    mv(a3,a4);        /* ZZZZ */
    lw(a4, -32, s0);             /* SSSS */
    sw(a0, 0, a5);             /* OOOO */
    sw(a1, 4, a5);             /* OOOO */
    sw(a2, 8, a5);             /* OOOO */
    sw(a3, 12, a5);             /* OOOO */
    sw(a4, 16, a5);             /* OOOO */
    $L109.set();
    ld(a0, s0, 56);             /* GGGG */
    ld(ra, sp, 72);             /* GGGG */
    ld(s0, sp, 64);             /* GGGG */
    addi(sp, sp, 80);           /* LLLL */
    jr(ra);                       /* UUUU */
    $debug.set();
    $btree.set();
    $allocate.set();
    addi(sp, sp, -32);           /* LLLL */
    sd(ra, 24, sp);             /* OOOO */
    sd(s0, 16, sp);             /* OOOO */
    addi(s0, sp, 32);           /* LLLL */
    lui(a5, true, $btree)            /* BBBB */;
    addi(a5, a5, false, $btree);     /* IIII */
    lw(a5, 0, a5);             /* SSSS */
    sw(a5, -20, s0);             /* OOOO */
    lui(a5, true, $btree)            /* BBBB */;
    addi(a4, a5, false, $btree);     /* IIII */
    lw(a3, -20, s0);             /* SSSS */
    li(a5,44);        /* ZZZZ */
    mul(a5,a3,a5);        /* ZZZZ */
    add(a5,a4,a5);        /* ZZZZ */
    lw(a4, 4, a5);             /* SSSS */
    lui(a5, true, $btree)            /* BBBB */;
    addi(a5, a5, false, $btree);     /* IIII */
    sw(a4, 0, a5);             /* OOOO */
    lui(a5, true, $btree)            /* BBBB */;
    addi(a4, a5, false, $btree);     /* IIII */
    lw(a3, -20, s0);             /* SSSS */
    li(a5,44);        /* ZZZZ */
    mul(a5,a3,a5);        /* ZZZZ */
    add(a5,a4,a5);        /* ZZZZ */
    sw(zero, 4, a5);             /* OOOO */
    lw(a5, -20, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($clear);                      /* XXXX */
    lw(a5, -20, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    ld(ra, sp, 24);             /* GGGG */
    ld(s0, sp, 16);             /* GGGG */
    addi(sp, sp, 32);           /* LLLL */
    jr(ra);                       /* UUUU */
    $isLeaf.set();
    addi(sp, sp, -32);           /* LLLL */
    sd(s0, 24, sp);             /* OOOO */
    addi(s0, sp, 32);           /* LLLL */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -20, s0);             /* OOOO */
    lui(a5, true, $btree)            /* BBBB */;
    addi(a4, a5, false, $btree);     /* IIII */
    lw(a3, -20, s0);             /* SSSS */
    li(a5,44);        /* ZZZZ */
    mul(a5,a3,a5);        /* ZZZZ */
    add(a5,a4,a5);        /* ZZZZ */
    lw(a5, 8, a5);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    ld(s0, sp, 24);             /* GGGG */
    addi(sp, sp, 32);           /* LLLL */
    jr(ra);                       /* UUUU */
    $rootIsLeaf.set();
    addi(sp, sp, -16);           /* LLLL */
    sd(s0, 8, sp);             /* OOOO */
    addi(s0, sp, 16);           /* LLLL */
    lui(a5, true, $btree)            /* BBBB */;
    addi(a5, a5, false, $btree);     /* IIII */
    lw(a5, 8, a5);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    ld(s0, sp, 8);             /* GGGG */
    addi(sp, sp, 16);           /* LLLL */
    jr(ra);                       /* UUUU */
    $setLeaf.set();
    addi(sp, sp, -32);           /* LLLL */
    sd(s0, 24, sp);             /* OOOO */
    addi(s0, sp, 32);           /* LLLL */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -20, s0);             /* OOOO */
    lui(a5, true, $btree)            /* BBBB */;
    addi(a4, a5, false, $btree);     /* IIII */
    lw(a3, -20, s0);             /* SSSS */
    li(a5,44);        /* ZZZZ */
    mul(a5,a3,a5);        /* ZZZZ */
    add(a5,a4,a5);        /* ZZZZ */
    li(a4,1);        /* ZZZZ */
    sw(a4, 8, a5);             /* OOOO */
    nop();        /* ZZZZ */
    ld(s0, sp, 24);             /* GGGG */
    addi(sp, sp, 32);           /* LLLL */
    jr(ra);                       /* UUUU */
    $setBranch.set();
    addi(sp, sp, -32);           /* LLLL */
    sd(s0, 24, sp);             /* OOOO */
    addi(s0, sp, 32);           /* LLLL */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -20, s0);             /* OOOO */
    lui(a5, true, $btree)            /* BBBB */;
    addi(a4, a5, false, $btree);     /* IIII */
    lw(a3, -20, s0);             /* SSSS */
    li(a5,44);        /* ZZZZ */
    mul(a5,a3,a5);        /* ZZZZ */
    add(a5,a4,a5);        /* ZZZZ */
    sw(zero, 8, a5);             /* OOOO */
    nop();        /* ZZZZ */
    ld(s0, sp, 24);             /* GGGG */
    addi(sp, sp, 32);           /* LLLL */
    jr(ra);                       /* UUUU */
    $setBranchRoot.set();
    addi(sp, sp, -16);           /* LLLL */
    sd(s0, 8, sp);             /* OOOO */
    addi(s0, sp, 16);           /* LLLL */
    lui(a5, true, $btree)            /* BBBB */;
    addi(a5, a5, false, $btree);     /* IIII */
    sw(zero, 8, a5);             /* OOOO */
    nop();        /* ZZZZ */
    ld(s0, sp, 8);             /* GGGG */
    addi(sp, sp, 16);           /* LLLL */
    jr(ra);                       /* UUUU */
    $clear.set();
    addi(sp, sp, -64);           /* LLLL */
    sd(s0, 56, sp);             /* OOOO */
    addi(s0, sp, 64);           /* LLLL */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -52, s0);             /* OOOO */
    lw(a4, -52, s0);             /* SSSS */
    li(a5,44);        /* ZZZZ */
    mul(a4,a4,a5);        /* ZZZZ */
    lui(a5, true, $btree)            /* BBBB */;
    addi(a5, a5, false, $btree);     /* IIII */
    add(a5,a4,a5);        /* ZZZZ */
    addi(a5, a5, 12);           /* LLLL */
    sd(a5, -32, s0);             /* OOOO */
    li(a5,16);        /* ZZZZ */
    sw(a5, -36, s0);             /* OOOO */
    li(a5,36);        /* ZZZZ */
    sw(a5, -40, s0);             /* OOOO */
    lw(a5, -36, s0);             /* SSSS */
    mv(a2,a5);        /* ZZZZ */
    lw(a5, -40, s0);             /* SSSS */
    sextw(a3,a5);        /* ZZZZ */
    sextw(a4,a2);        /* ZZZZ */
    bge(a3, a4, $L120);             /* YYYY */
    mv(a5,a2);        /* ZZZZ */
    $L120.set();
    sw(a5, -44, s0);             /* OOOO */
    sw(zero, -20, s0);             /* OOOO */
    j($L121);                       /* WWWW */
    $L122.set();
    lw(a5, -20, s0);             /* SSSS */
    ld(a4, s0, 32);             /* GGGG */
    add(a5,a4,a5);        /* ZZZZ */
    sb(zero, 0, a5);             /* OOOO */
    lw(a5, -20, s0);             /* SSSS */
    addiw(a5, a5, 1);           /* LLLL */
    sw(a5, -20, s0);             /* OOOO */
    $L121.set();
    lw(a5, -20, s0);             /* SSSS */
    mv(a4,a5);        /* ZZZZ */
    lw(a5, -44, s0);             /* SSSS */
    sextw(a4,a4);        /* ZZZZ */
    sextw(a5,a5);        /* ZZZZ */
    blt(a4, a5, $L122);             /* YYYY */
    nop();        /* ZZZZ */
    nop();        /* ZZZZ */
    ld(s0, sp, 56);             /* GGGG */
    addi(sp, sp, 64);           /* LLLL */
    jr(ra);                       /* UUUU */
    $erase.set();
    addi(sp, sp, -64);           /* LLLL */
    sd(s0, 56, sp);             /* OOOO */
    addi(s0, sp, 64);           /* LLLL */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -52, s0);             /* OOOO */
    lw(a4, -52, s0);             /* SSSS */
    li(a5,44);        /* ZZZZ */
    mul(a4,a4,a5);        /* ZZZZ */
    lui(a5, true, $btree)            /* BBBB */;
    addi(a5, a5, false, $btree);     /* IIII */
    add(a5,a4,a5);        /* ZZZZ */
    addi(a5, a5, 12);           /* LLLL */
    sd(a5, -32, s0);             /* OOOO */
    li(a5,16);        /* ZZZZ */
    sw(a5, -36, s0);             /* OOOO */
    li(a5,36);        /* ZZZZ */
    sw(a5, -40, s0);             /* OOOO */
    lw(a5, -36, s0);             /* SSSS */
    mv(a2,a5);        /* ZZZZ */
    lw(a5, -40, s0);             /* SSSS */
    sextw(a3,a5);        /* ZZZZ */
    sextw(a4,a2);        /* ZZZZ */
    bge(a3, a4, $L124);             /* YYYY */
    mv(a5,a2);        /* ZZZZ */
    $L124.set();
    sw(a5, -44, s0);             /* OOOO */
    sw(zero, -20, s0);             /* OOOO */
    j($L125);                       /* WWWW */
    $L126.set();
    lw(a5, -20, s0);             /* SSSS */
    ld(a4, s0, 32);             /* GGGG */
    add(a5,a4,a5);        /* ZZZZ */
    li(a4,1);        /* ZZZZ */
    sb(a4, 0, a5);             /* OOOO */
    lw(a5, -20, s0);             /* SSSS */
    addiw(a5, a5, 1);           /* LLLL */
    sw(a5, -20, s0);             /* OOOO */
    $L125.set();
    lw(a5, -20, s0);             /* SSSS */
    mv(a4,a5);        /* ZZZZ */
    lw(a5, -44, s0);             /* SSSS */
    sextw(a4,a4);        /* ZZZZ */
    sextw(a5,a5);        /* ZZZZ */
    blt(a4, a5, $L126);             /* YYYY */
    nop();        /* ZZZZ */
    nop();        /* ZZZZ */
    ld(s0, sp, 56);             /* GGGG */
    addi(sp, sp, 64);           /* LLLL */
    jr(ra);                       /* UUUU */
    $freeNode.set();
    addi(sp, sp, -48);           /* LLLL */
    sd(ra, 40, sp);             /* OOOO */
    sd(s0, 32, sp);             /* OOOO */
    addi(s0, sp, 48);           /* LLLL */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -36, s0);             /* OOOO */
    lw(a5, -36, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($erase);                      /* XXXX */
    lui(a5, true, $btree)            /* BBBB */;
    addi(a5, a5, false, $btree);     /* IIII */
    lw(a5, 0, a5);             /* SSSS */
    sw(a5, -20, s0);             /* OOOO */
    lui(a5, true, $btree)            /* BBBB */;
    addi(a5, a5, false, $btree);     /* IIII */
    lw(a4, -36, s0);             /* SSSS */
    sw(a4, 0, a5);             /* OOOO */
    lui(a5, true, $btree)            /* BBBB */;
    addi(a4, a5, false, $btree);     /* IIII */
    lw(a3, -36, s0);             /* SSSS */
    li(a5,44);        /* ZZZZ */
    mul(a5,a3,a5);        /* ZZZZ */
    add(a5,a4,a5);        /* ZZZZ */
    lw(a4, -20, s0);             /* SSSS */
    sw(a4, 4, a5);             /* OOOO */
    nop();        /* ZZZZ */
    ld(ra, sp, 40);             /* GGGG */
    ld(s0, sp, 32);             /* GGGG */
    addi(sp, sp, 48);           /* LLLL */
    jr(ra);                       /* UUUU */
    $leaf.set();
    addi(sp, sp, -32);           /* LLLL */
    sd(s0, 24, sp);             /* OOOO */
    addi(s0, sp, 32);           /* LLLL */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -20, s0);             /* OOOO */
    lw(a4, -20, s0);             /* SSSS */
    li(a5,44);        /* ZZZZ */
    mul(a4,a4,a5);        /* ZZZZ */
    lui(a5, true, $btree)            /* BBBB */;
    addi(a5, a5, false, $btree);     /* IIII */
    add(a5,a4,a5);        /* ZZZZ */
    addi(a5, a5, 12);           /* LLLL */
    mv(a0,a5);        /* ZZZZ */
    ld(s0, sp, 24);             /* GGGG */
    addi(sp, sp, 32);           /* LLLL */
    jr(ra);                       /* UUUU */
    $branch.set();
    addi(sp, sp, -32);           /* LLLL */
    sd(s0, 24, sp);             /* OOOO */
    addi(s0, sp, 32);           /* LLLL */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -20, s0);             /* OOOO */
    lw(a4, -20, s0);             /* SSSS */
    li(a5,44);        /* ZZZZ */
    mul(a4,a4,a5);        /* ZZZZ */
    lui(a5, true, $btree)            /* BBBB */;
    addi(a5, a5, false, $btree);     /* IIII */
    add(a5,a4,a5);        /* ZZZZ */
    addi(a5, a5, 12);           /* LLLL */
    mv(a0,a5);        /* ZZZZ */
    ld(s0, sp, 24);             /* GGGG */
    addi(sp, sp, 32);           /* LLLL */
    jr(ra);                       /* UUUU */
    $leafSize.set();
    addi(sp, sp, -32);           /* LLLL */
    sd(ra, 24, sp);             /* OOOO */
    sd(s0, 16, sp);             /* OOOO */
    addi(s0, sp, 32);           /* LLLL */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -20, s0);             /* OOOO */
    lw(a5, -20, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($leaf);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($leaf_size);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    ld(ra, sp, 24);             /* GGGG */
    ld(s0, sp, 16);             /* GGGG */
    addi(sp, sp, 32);           /* LLLL */
    jr(ra);                       /* UUUU */
    $branchSize.set();
    addi(sp, sp, -32);           /* LLLL */
    sd(ra, 24, sp);             /* OOOO */
    sd(s0, 16, sp);             /* OOOO */
    addi(s0, sp, 32);           /* LLLL */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -20, s0);             /* OOOO */
    lw(a5, -20, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($branch_size1);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    ld(ra, sp, 24);             /* GGGG */
    ld(s0, sp, 16);             /* GGGG */
    addi(sp, sp, 32);           /* LLLL */
    jr(ra);                       /* UUUU */
    $splitLeafSize.set();
    addi(sp, sp, -16);           /* LLLL */
    sd(s0, 8, sp);             /* OOOO */
    addi(s0, sp, 16);           /* LLLL */
    li(a5,1);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    ld(s0, sp, 8);             /* GGGG */
    addi(sp, sp, 16);           /* LLLL */
    jr(ra);                       /* UUUU */
    $splitBranchSize.set();
    addi(sp, sp, -16);           /* LLLL */
    sd(s0, 8, sp);             /* OOOO */
    addi(s0, sp, 16);           /* LLLL */
    li(a5,1);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    ld(s0, sp, 8);             /* GGGG */
    addi(sp, sp, 16);           /* LLLL */
    jr(ra);                       /* UUUU */
    $isFull.set();
    addi(sp, sp, -32);           /* LLLL */
    sd(ra, 24, sp);             /* OOOO */
    sd(s0, 16, sp);             /* OOOO */
    addi(s0, sp, 32);           /* LLLL */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -20, s0);             /* OOOO */
    lw(a5, -20, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($isLeaf);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    beq(a5, zero, $L141);             /* YYYY */
    lw(a5, -20, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($leafSize);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    mv(a4,a5);        /* ZZZZ */
    li(a5,1);        /* ZZZZ */
    sgt(a5,a4,a5);        /* ZZZZ */
    andi(a5,a5,0xff);        /* ZZZZ */
    sextw(a5,a5);        /* ZZZZ */
    j($L142);                       /* WWWW */
    $L141.set();
    lw(a5, -20, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branchSize);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    mv(a4,a5);        /* ZZZZ */
    li(a5,2);        /* ZZZZ */
    sgt(a5,a4,a5);        /* ZZZZ */
    andi(a5,a5,0xff);        /* ZZZZ */
    sextw(a5,a5);        /* ZZZZ */
    $L142.set();
    mv(a0,a5);        /* ZZZZ */
    ld(ra, sp, 24);             /* GGGG */
    ld(s0, sp, 16);             /* GGGG */
    addi(sp, sp, 32);           /* LLLL */
    jr(ra);                       /* UUUU */
    $isFullRoot.set();
    addi(sp, sp, -16);           /* LLLL */
    sd(ra, 8, sp);             /* OOOO */
    sd(s0, 0, sp);             /* OOOO */
    addi(s0, sp, 16);           /* LLLL */
    li(a0,0);        /* ZZZZ */
    call($isFull);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    ld(ra, sp, 8);             /* GGGG */
    ld(s0, sp, 0);             /* GGGG */
    addi(sp, sp, 16);           /* LLLL */
    jr(ra);                       /* UUUU */
    $isLow.set();
    addi(sp, sp, -32);           /* LLLL */
    sd(ra, 24, sp);             /* OOOO */
    sd(s0, 16, sp);             /* OOOO */
    addi(s0, sp, 32);           /* LLLL */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -20, s0);             /* OOOO */
    lw(a5, -20, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($isLeaf);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    beq(a5, zero, $L147);             /* YYYY */
    lw(a5, -20, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($leafSize);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    mv(a4,a5);        /* ZZZZ */
    li(a5,1);        /* ZZZZ */
    sgt(a5,a4,a5);        /* ZZZZ */
    seqz(a5, a5);                 /* YY11 */
    andi(a5,a5,0xff);        /* ZZZZ */
    sextw(a5,a5);        /* ZZZZ */
    j($L148);                       /* WWWW */
    $L147.set();
    lw(a5, -20, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branchSize);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    mv(a4,a5);        /* ZZZZ */
    li(a5,1);        /* ZZZZ */
    sgt(a5,a4,a5);        /* ZZZZ */
    seqz(a5, a5);                 /* YY11 */
    andi(a5,a5,0xff);        /* ZZZZ */
    sextw(a5,a5);        /* ZZZZ */
    $L148.set();
    mv(a0,a5);        /* ZZZZ */
    ld(ra, sp, 24);             /* GGGG */
    ld(s0, sp, 16);             /* GGGG */
    addi(sp, sp, 32);           /* LLLL */
    jr(ra);                       /* UUUU */
    $hasLeavesForChildren.set();
    addi(sp, sp, -64);           /* LLLL */
    sd(ra, 56, sp);             /* OOOO */
    sd(s0, 48, sp);             /* OOOO */
    addi(s0, sp, 64);           /* LLLL */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -52, s0);             /* OOOO */
    lw(a5, -52, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    sd(a0, -24, s0);             /* OOOO */
    addi(a5, s0, -48);           /* LLLL */
    ld(a1, s0, 24);             /* GGGG */
    mv(a0,a5);        /* ZZZZ */
    call($branch_firstElement);                      /* XXXX */
    lw(a5, -32, s0);             /* SSSS */
    sw(a5, -28, s0);             /* OOOO */
    lw(a5, -28, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($isLeaf);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    ld(ra, sp, 56);             /* GGGG */
    ld(s0, sp, 48);             /* GGGG */
    addi(sp, sp, 64);           /* LLLL */
    jr(ra);                       /* UUUU */
    $allocLeaf.set();
    addi(sp, sp, -32);           /* LLLL */
    sd(ra, 24, sp);             /* OOOO */
    sd(s0, 16, sp);             /* OOOO */
    addi(s0, sp, 32);           /* LLLL */
    call($allocate);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -20, s0);             /* OOOO */
    lw(a5, -20, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($setLeaf);                      /* XXXX */
    lw(a5, -20, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    ld(ra, sp, 24);             /* GGGG */
    ld(s0, sp, 16);             /* GGGG */
    addi(sp, sp, 32);           /* LLLL */
    jr(ra);                       /* UUUU */
    $allocBranch.set();
    addi(sp, sp, -32);           /* LLLL */
    sd(ra, 24, sp);             /* OOOO */
    sd(s0, 16, sp);             /* OOOO */
    addi(s0, sp, 32);           /* LLLL */
    call($allocate);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -20, s0);             /* OOOO */
    lw(a5, -20, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($setBranch);                      /* XXXX */
    lw(a5, -20, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    ld(ra, sp, 24);             /* GGGG */
    ld(s0, sp, 16);             /* GGGG */
    addi(sp, sp, 32);           /* LLLL */
    jr(ra);                       /* UUUU */
    $splitLeafRoot.set();
    addi(sp, sp, -176);           /* LLLL */
    sd(ra, 168, sp);             /* OOOO */
    sd(s0, 160, sp);             /* OOOO */
    addi(s0, sp, 176);           /* LLLL */
    call($allocLeaf);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -28, s0);             /* OOOO */
    lw(a5, -28, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($leaf);                      /* XXXX */
    sd(a0, -40, s0);             /* OOOO */
    call($allocLeaf);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -44, s0);             /* OOOO */
    lw(a5, -44, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($leaf);                      /* XXXX */
    sd(a0, -56, s0);             /* OOOO */
    li(a0,0);        /* ZZZZ */
    call($leaf);                      /* XXXX */
    sd(a0, -64, s0);             /* OOOO */
    call($splitLeafSize);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -68, s0);             /* OOOO */
    sw(zero, -20, s0);             /* OOOO */
    j($L157);                       /* WWWW */
    $L158.set();
    addi(a5, s0, -152);           /* LLLL */
    ld(a1, s0, 64);             /* GGGG */
    mv(a0,a5);        /* ZZZZ */
    call($leaf_shift);                      /* XXXX */
    lw(a5, -140, s0);             /* SSSS */
    lbu(a4, -136, s0);             /* SSSS */
    sextw(a4,a4);        /* ZZZZ */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 40);             /* GGGG */
    call($leaf_push);                      /* XXXX */
    lw(a5, -20, s0);             /* SSSS */
    addiw(a5, a5, 1);           /* LLLL */
    sw(a5, -20, s0);             /* OOOO */
    $L157.set();
    lw(a5, -20, s0);             /* SSSS */
    mv(a4,a5);        /* ZZZZ */
    lw(a5, -68, s0);             /* SSSS */
    sextw(a4,a4);        /* ZZZZ */
    sextw(a5,a5);        /* ZZZZ */
    blt(a4, a5, $L158);             /* YYYY */
    sw(zero, -24, s0);             /* OOOO */
    j($L159);                       /* WWWW */
    $L160.set();
    addi(a5, s0, -176);           /* LLLL */
    ld(a1, s0, 64);             /* GGGG */
    mv(a0,a5);        /* ZZZZ */
    call($leaf_shift);                      /* XXXX */
    lw(a5, -164, s0);             /* SSSS */
    lbu(a4, -160, s0);             /* SSSS */
    sextw(a4,a4);        /* ZZZZ */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 56);             /* GGGG */
    call($leaf_push);                      /* XXXX */
    lw(a5, -24, s0);             /* SSSS */
    addiw(a5, a5, 1);           /* LLLL */
    sw(a5, -24, s0);             /* OOOO */
    $L159.set();
    lw(a5, -24, s0);             /* SSSS */
    mv(a4,a5);        /* ZZZZ */
    lw(a5, -68, s0);             /* SSSS */
    sextw(a4,a4);        /* ZZZZ */
    sextw(a5,a5);        /* ZZZZ */
    blt(a4, a5, $L160);             /* YYYY */
    addi(a5, s0, -128);           /* LLLL */
    ld(a1, s0, 56);             /* GGGG */
    mv(a0,a5);        /* ZZZZ */
    call($leaf_firstElement);                      /* XXXX */
    lw(a5, -116, s0);             /* SSSS */
    sw(a5, -72, s0);             /* OOOO */
    addi(a5, s0, -104);           /* LLLL */
    ld(a1, s0, 40);             /* GGGG */
    mv(a0,a5);        /* ZZZZ */
    call($leaf_lastElement);                      /* XXXX */
    lw(a5, -92, s0);             /* SSSS */
    sw(a5, -76, s0);             /* OOOO */
    lw(a5, -76, s0);             /* SSSS */
    mv(a4,a5);        /* ZZZZ */
    lw(a5, -72, s0);             /* SSSS */
    addw(a5,a4,a5);        /* ZZZZ */
    sextw(a5,a5);        /* ZZZZ */
    srliw(a4,a5,31);        /* ZZZZ */
    addw(a5,a4,a5);        /* ZZZZ */
    sraiw(a5,a5,1);        /* ZZZZ */
    sw(a5, -80, s0);             /* OOOO */
    call($setBranchRoot);                      /* XXXX */
    li(a0,0);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($branch_clear);                      /* XXXX */
    li(a0,0);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a3,a0);        /* ZZZZ */
    lw(a4, -28, s0);             /* SSSS */
    lw(a5, -80, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    mv(a0,a3);        /* ZZZZ */
    call($branch_push);                      /* XXXX */
    li(a0,0);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a4,a0);        /* ZZZZ */
    lw(a5, -44, s0);             /* SSSS */
    mv(a2,a5);        /* ZZZZ */
   }
  void test_btree2() {
    li(a1,0);        /* ZZZZ */
    mv(a0,a4);        /* ZZZZ */
    call($branch_push);                      /* XXXX */
    nop();        /* ZZZZ */
    ld(ra, sp, 168);             /* GGGG */
    ld(s0, sp, 160);             /* GGGG */
    addi(sp, sp, 176);           /* LLLL */
    jr(ra);                       /* UUUU */
    $splitBranchRoot.set();
    addi(sp, sp, -160);           /* LLLL */
    sd(ra, 152, sp);             /* OOOO */
    sd(s0, 144, sp);             /* OOOO */
    addi(s0, sp, 160);           /* LLLL */
    call($allocBranch);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -28, s0);             /* OOOO */
    lw(a5, -28, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    sd(a0, -40, s0);             /* OOOO */
    call($allocBranch);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -44, s0);             /* OOOO */
    lw(a5, -44, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    sd(a0, -56, s0);             /* OOOO */
    li(a0,0);        /* ZZZZ */
    call($branch);                      /* XXXX */
    sd(a0, -64, s0);             /* OOOO */
    call($splitBranchSize);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -68, s0);             /* OOOO */
    sw(zero, -20, s0);             /* OOOO */
    j($L162);                       /* WWWW */
    $L163.set();
    addi(a5, s0, -136);           /* LLLL */
    ld(a1, s0, 64);             /* GGGG */
    mv(a0,a5);        /* ZZZZ */
    call($branch_shift);                      /* XXXX */
    lw(a5, -124, s0);             /* SSSS */
    lw(a4, -120, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 40);             /* GGGG */
    call($branch_push);                      /* XXXX */
    lw(a5, -20, s0);             /* SSSS */
    addiw(a5, a5, 1);           /* LLLL */
    sw(a5, -20, s0);             /* OOOO */
    $L162.set();
    lw(a5, -20, s0);             /* SSSS */
    mv(a4,a5);        /* ZZZZ */
    lw(a5, -68, s0);             /* SSSS */
    sextw(a4,a4);        /* ZZZZ */
    sextw(a5,a5);        /* ZZZZ */
    blt(a4, a5, $L163);             /* YYYY */
    addi(a5, s0, -88);           /* LLLL */
    ld(a1, s0, 64);             /* GGGG */
    mv(a0,a5);        /* ZZZZ */
    call($branch_shift);                      /* XXXX */
    lw(a5, -72, s0);             /* SSSS */
    mv(a2,a5);        /* ZZZZ */
    li(a1,0);        /* ZZZZ */
    ld(a0, s0, 40);             /* GGGG */
    call($branch_push);                      /* XXXX */
    sw(zero, -24, s0);             /* OOOO */
    j($L164);                       /* WWWW */
    $L165.set();
    addi(a5, s0, -160);           /* LLLL */
    ld(a1, s0, 64);             /* GGGG */
    mv(a0,a5);        /* ZZZZ */
    call($branch_shift);                      /* XXXX */
    lw(a5, -148, s0);             /* SSSS */
    lw(a4, -144, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 56);             /* GGGG */
    call($branch_push);                      /* XXXX */
    lw(a5, -24, s0);             /* SSSS */
    addiw(a5, a5, 1);           /* LLLL */
    sw(a5, -24, s0);             /* OOOO */
    $L164.set();
    lw(a5, -24, s0);             /* SSSS */
    mv(a4,a5);        /* ZZZZ */
    lw(a5, -68, s0);             /* SSSS */
    sextw(a4,a4);        /* ZZZZ */
    sextw(a5,a5);        /* ZZZZ */
    blt(a4, a5, $L165);             /* YYYY */
    addi(a5, s0, -112);           /* LLLL */
    ld(a1, s0, 64);             /* GGGG */
    mv(a0,a5);        /* ZZZZ */
    call($branch_shift);                      /* XXXX */
    lw(a5, -96, s0);             /* SSSS */
    mv(a2,a5);        /* ZZZZ */
    li(a1,0);        /* ZZZZ */
    ld(a0, s0, 56);             /* GGGG */
    call($branch_push);                      /* XXXX */
    li(a0,0);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($branch_clear);                      /* XXXX */
    lw(a5, -76, s0);             /* SSSS */
    lw(a4, -28, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    ld(a0, s0, 64);             /* GGGG */
    call($branch_push);                      /* XXXX */
    lw(a5, -44, s0);             /* SSSS */
    mv(a2,a5);        /* ZZZZ */
    li(a1,0);        /* ZZZZ */
    ld(a0, s0, 64);             /* GGGG */
    call($branch_push);                      /* XXXX */
    nop();        /* ZZZZ */
    ld(ra, sp, 152);             /* GGGG */
    ld(s0, sp, 144);             /* GGGG */
    addi(sp, sp, 160);           /* LLLL */
    jr(ra);                       /* UUUU */
    $splitLeaf.set();
    addi(sp, sp, -144);           /* LLLL */
    sd(ra, 136, sp);             /* OOOO */
    sd(s0, 128, sp);             /* OOOO */
    addi(s0, sp, 144);           /* LLLL */
    mv(a5,a0);        /* ZZZZ */
    mv(a3,a1);        /* ZZZZ */
    mv(a4,a2);        /* ZZZZ */
    sw(a5, -132, s0);             /* OOOO */
    mv(a5,a3);        /* ZZZZ */
    sw(a5, -136, s0);             /* OOOO */
    mv(a5,a4);        /* ZZZZ */
    sw(a5, -140, s0);             /* OOOO */
    lw(a5, -136, s0);             /* SSSS */
    sw(a5, -24, s0);             /* OOOO */
    call($allocLeaf);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -28, s0);             /* OOOO */
    lw(a5, -132, s0);             /* SSSS */
    sw(a5, -32, s0);             /* OOOO */
    call($splitLeafSize);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -36, s0);             /* OOOO */
    sw(zero, -20, s0);             /* OOOO */
    j($L167);                       /* WWWW */
    $L168.set();
    lw(a5, -32, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($leaf);                      /* XXXX */
    mv(a4,a0);        /* ZZZZ */
    addi(a5, s0, -120);           /* LLLL */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($leaf_shift);                      /* XXXX */
    lw(a5, -28, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($leaf);                      /* XXXX */
    mv(a3,a0);        /* ZZZZ */
    lw(a5, -108, s0);             /* SSSS */
    lbu(a4, -104, s0);             /* SSSS */
    sextw(a4,a4);        /* ZZZZ */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    mv(a0,a3);        /* ZZZZ */
    call($leaf_push);                      /* XXXX */
    lw(a5, -20, s0);             /* SSSS */
    addiw(a5, a5, 1);           /* LLLL */
    sw(a5, -20, s0);             /* OOOO */
    $L167.set();
    lw(a5, -20, s0);             /* SSSS */
    mv(a4,a5);        /* ZZZZ */
    lw(a5, -36, s0);             /* SSSS */
    sextw(a4,a4);        /* ZZZZ */
    sextw(a5,a5);        /* ZZZZ */
    blt(a4, a5, $L168);             /* YYYY */
    lw(a5, -32, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($leaf);                      /* XXXX */
    mv(a4,a0);        /* ZZZZ */
    addi(a5, s0, -96);           /* LLLL */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($leaf_firstElement);                      /* XXXX */
    lw(a5, -84, s0);             /* SSSS */
    sw(a5, -40, s0);             /* OOOO */
    lw(a5, -28, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($leaf);                      /* XXXX */
    mv(a4,a0);        /* ZZZZ */
    addi(a5, s0, -72);           /* LLLL */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($leaf_lastElement);                      /* XXXX */
    lw(a5, -60, s0);             /* SSSS */
    sw(a5, -44, s0);             /* OOOO */
    lw(a5, -40, s0);             /* SSSS */
    mv(a4,a5);        /* ZZZZ */
    lw(a5, -44, s0);             /* SSSS */
    addw(a5,a4,a5);        /* ZZZZ */
    sextw(a5,a5);        /* ZZZZ */
    srliw(a4,a5,31);        /* ZZZZ */
    addw(a5,a4,a5);        /* ZZZZ */
    sraiw(a5,a5,1);        /* ZZZZ */
    sw(a5, -48, s0);             /* OOOO */
    lw(a5, -24, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    lw(a3, -140, s0);             /* SSSS */
    lw(a4, -28, s0);             /* SSSS */
    lw(a5, -48, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    call($branch_insertElementAt);                      /* XXXX */
    nop();        /* ZZZZ */
    ld(ra, sp, 136);             /* GGGG */
    ld(s0, sp, 128);             /* GGGG */
    addi(sp, sp, 144);           /* LLLL */
    jr(ra);                       /* UUUU */
    $splitBranch.set();
    addi(sp, sp, -96);           /* LLLL */
    sd(ra, 88, sp);             /* OOOO */
    sd(s0, 80, sp);             /* OOOO */
    addi(s0, sp, 96);           /* LLLL */
    mv(a5,a0);        /* ZZZZ */
    mv(a3,a1);        /* ZZZZ */
    mv(a4,a2);        /* ZZZZ */
    sw(a5, -84, s0);             /* OOOO */
    mv(a5,a3);        /* ZZZZ */
    sw(a5, -88, s0);             /* OOOO */
    mv(a5,a4);        /* ZZZZ */
    sw(a5, -92, s0);             /* OOOO */
    lw(a5, -88, s0);             /* SSSS */
    sw(a5, -24, s0);             /* OOOO */
    call($allocBranch);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -28, s0);             /* OOOO */
    lw(a5, -84, s0);             /* SSSS */
    sw(a5, -32, s0);             /* OOOO */
    call($splitBranchSize);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -36, s0);             /* OOOO */
    sw(zero, -20, s0);             /* OOOO */
    j($L170);                       /* WWWW */
    $L171.set();
    lw(a5, -32, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a4,a0);        /* ZZZZ */
    addi(a5, s0, -80);           /* LLLL */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($branch_shift);                      /* XXXX */
    lw(a5, -28, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a3,a0);        /* ZZZZ */
    lw(a5, -68, s0);             /* SSSS */
    lw(a4, -64, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    mv(a0,a3);        /* ZZZZ */
    call($branch_push);                      /* XXXX */
    lw(a5, -20, s0);             /* SSSS */
    addiw(a5, a5, 1);           /* LLLL */
    sw(a5, -20, s0);             /* OOOO */
    $L170.set();
    lw(a5, -20, s0);             /* SSSS */
    mv(a4,a5);        /* ZZZZ */
    lw(a5, -36, s0);             /* SSSS */
    sextw(a4,a4);        /* ZZZZ */
    sextw(a5,a5);        /* ZZZZ */
    blt(a4, a5, $L171);             /* YYYY */
    lw(a5, -32, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a4,a0);        /* ZZZZ */
    addi(a5, s0, -56);           /* LLLL */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($branch_shift);                      /* XXXX */
    lw(a5, -28, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a4,a0);        /* ZZZZ */
    lw(a5, -40, s0);             /* SSSS */
    mv(a2,a5);        /* ZZZZ */
    li(a1,0);        /* ZZZZ */
    mv(a0,a4);        /* ZZZZ */
    call($branch_push);                      /* XXXX */
    lw(a5, -24, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    lw(a5, -44, s0);             /* SSSS */
    lw(a3, -92, s0);             /* SSSS */
    lw(a4, -28, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    call($branch_insertElementAt);                      /* XXXX */
    nop();        /* ZZZZ */
    ld(ra, sp, 88);             /* GGGG */
    ld(s0, sp, 80);             /* GGGG */
    addi(sp, sp, 96);           /* LLLL */
    jr(ra);                       /* UUUU */
    $stealFromLeft.set();
    addi(sp, sp, -320);           /* LLLL */
    sd(ra, 312, sp);             /* OOOO */
    sd(s0, 304, sp);             /* OOOO */
    addi(s0, sp, 320);           /* LLLL */
    mv(a5,a0);        /* ZZZZ */
    mv(a4,a1);        /* ZZZZ */
    sw(a5, -276, s0);             /* OOOO */
    mv(a5,a4);        /* ZZZZ */
    sw(a5, -280, s0);             /* OOOO */
    lw(a5, -276, s0);             /* SSSS */
    sw(a5, -20, s0);             /* OOOO */
    lw(a5, -20, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a3,a0);        /* ZZZZ */
    lw(a5, -280, s0);             /* SSSS */
    addiw(a5, a5, -1);           /* LLLL */
    sextw(a4,a5);        /* ZZZZ */
    addi(a5, s0, -200);           /* LLLL */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a3);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($branch_elementAt);                      /* XXXX */
    lw(a5, -184, s0);             /* SSSS */
    sw(a5, -24, s0);             /* OOOO */
    lw(a5, -20, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a3,a0);        /* ZZZZ */
    addi(a5, s0, -176);           /* LLLL */
    lw(a4, -280, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a3);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($branch_elementAt);                      /* XXXX */
    lw(a5, -160, s0);             /* SSSS */
    sw(a5, -28, s0);             /* OOOO */
    lw(a5, -276, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($hasLeavesForChildren);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    beq(a5, zero, $L173);             /* YYYY */
    lw(a5, -24, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($leafSize);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -52, s0);             /* OOOO */
    lw(a5, -28, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($leafSize);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -56, s0);             /* OOOO */
    lw(a5, -56, s0);             /* SSSS */
    sextw(a4,a5);        /* ZZZZ */
    li(a5,1);        /* ZZZZ */
    ble(a4, a5, $L174);             /* YYYY */
    li(a5,0);        /* ZZZZ */
    j($L178);                       /* WWWW */
    $L174.set();
    lw(a5, -52, s0);             /* SSSS */
    sextw(a4,a5);        /* ZZZZ */
    li(a5,1);        /* ZZZZ */
    bgt(a4, a5, $L176);             /* YYYY */
    li(a5,0);        /* ZZZZ */
    j($L178);                       /* WWWW */
    $L176.set();
    lw(a5, -24, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($leaf);                      /* XXXX */
    mv(a4,a0);        /* ZZZZ */
    addi(a5, s0, -224);           /* LLLL */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($leaf_lastElement);                      /* XXXX */
    lw(a5, -28, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($leaf);                      /* XXXX */
    lw(a5, -212, s0);             /* SSSS */
    lbu(a4, -208, s0);             /* SSSS */
    sextw(a4,a4);        /* ZZZZ */
    li(a3,0);        /* ZZZZ */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    call($leaf_insertElementAt);                      /* XXXX */
    lw(a5, -24, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($leaf);                      /* XXXX */
    mv(a4,a0);        /* ZZZZ */
    addi(a5, s0, -320);           /* LLLL */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($leaf_pop);                      /* XXXX */
    lw(a5, -24, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($leaf);                      /* XXXX */
    mv(a3,a0);        /* ZZZZ */
    lw(a5, -52, s0);             /* SSSS */
    addiw(a5, a5, -2);           /* LLLL */
    sextw(a4,a5);        /* ZZZZ */
    addi(a5, s0, -152);           /* LLLL */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a3);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($leaf_elementAt);                      /* XXXX */
    lw(a5, -140, s0);             /* SSSS */
    sw(a5, -60, s0);             /* OOOO */
    lw(a5, -20, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    lw(a5, -280, s0);             /* SSSS */
    addiw(a5, a5, -1);           /* LLLL */
    sextw(a3,a5);        /* ZZZZ */
    lw(a4, -24, s0);             /* SSSS */
    lw(a5, -60, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    call($branch_setElementAt);                      /* XXXX */
    j($L177);                       /* WWWW */
    $L173.set();
    lw(a5, -24, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branchSize);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -32, s0);             /* OOOO */
    lw(a5, -28, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branchSize);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -36, s0);             /* OOOO */
    lw(a5, -36, s0);             /* SSSS */
    sextw(a4,a5);        /* ZZZZ */
    li(a5,2);        /* ZZZZ */
    ble(a4, a5, $L179);             /* YYYY */
    li(a5,0);        /* ZZZZ */
    j($L178);                       /* WWWW */
    $L179.set();
    lw(a5, -32, s0);             /* SSSS */
    sextw(a4,a5);        /* ZZZZ */
    li(a5,1);        /* ZZZZ */
    bgt(a4, a5, $L181);             /* YYYY */
    li(a5,0);        /* ZZZZ */
    j($L178);                       /* WWWW */
    $L181.set();
    lw(a5, -24, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a4,a0);        /* ZZZZ */
    addi(a5, s0, -248);           /* LLLL */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($branch_lastElement);                      /* XXXX */
    lw(a5, -20, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a3,a0);        /* ZZZZ */
    addi(a5, s0, -128);           /* LLLL */
    lw(a4, -280, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a3);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($branch_elementAt);                      /* XXXX */
    lw(a5, -116, s0);             /* SSSS */
    sw(a5, -40, s0);             /* OOOO */
    lw(a5, -28, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    lw(a4, -232, s0);             /* SSSS */
    lw(a5, -40, s0);             /* SSSS */
    li(a3,0);        /* ZZZZ */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    call($branch_insertElementAt);                      /* XXXX */
    lw(a5, -24, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a4,a0);        /* ZZZZ */
    addi(a5, s0, -320);           /* LLLL */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($branch_pop);                      /* XXXX */
    lw(a5, -28, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a4,a0);        /* ZZZZ */
    addi(a5, s0, -272);           /* LLLL */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($branch_firstElement);                      /* XXXX */
    lw(a5, -20, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a3,a0);        /* ZZZZ */
    lw(a5, -280, s0);             /* SSSS */
    addiw(a5, a5, -1);           /* LLLL */
    sextw(a4,a5);        /* ZZZZ */
    addi(a5, s0, -104);           /* LLLL */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a3);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($branch_elementAt);                      /* XXXX */
    lw(a5, -92, s0);             /* SSSS */
    sw(a5, -44, s0);             /* OOOO */
    lw(a5, -28, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    lw(a4, -256, s0);             /* SSSS */
    lw(a5, -44, s0);             /* SSSS */
    li(a3,0);        /* ZZZZ */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    call($branch_setElementAt);                      /* XXXX */
    lw(a5, -24, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a4,a0);        /* ZZZZ */
    addi(a5, s0, -80);           /* LLLL */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($branch_lastElement);                      /* XXXX */
    lw(a5, -68, s0);             /* SSSS */
    sw(a5, -48, s0);             /* OOOO */
    lw(a5, -20, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    lw(a5, -280, s0);             /* SSSS */
    addiw(a5, a5, -1);           /* LLLL */
    sextw(a3,a5);        /* ZZZZ */
    lw(a4, -24, s0);             /* SSSS */
    lw(a5, -48, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    call($branch_setElementAt);                      /* XXXX */
    $L177.set();
    li(a5,1);        /* ZZZZ */
    $L178.set();
    mv(a0,a5);        /* ZZZZ */
    ld(ra, sp, 312);             /* GGGG */
    ld(s0, sp, 304);             /* GGGG */
    addi(sp, sp, 320);           /* LLLL */
    jr(ra);                       /* UUUU */
    $stealFromRight.set();
    addi(sp, sp, -208);           /* LLLL */
    sd(ra, 200, sp);             /* OOOO */
    sd(s0, 192, sp);             /* OOOO */
    addi(s0, sp, 208);           /* LLLL */
    mv(a5,a0);        /* ZZZZ */
    mv(a4,a1);        /* ZZZZ */
    sw(a5, -164, s0);             /* OOOO */
    mv(a5,a4);        /* ZZZZ */
    sw(a5, -168, s0);             /* OOOO */
    lw(a5, -164, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a3,a0);        /* ZZZZ */
    addi(a5, s0, -64);           /* LLLL */
    lw(a4, -168, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a3);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($branch_elementAt);                      /* XXXX */
    lw(a5, -48, s0);             /* SSSS */
    sw(a5, -20, s0);             /* OOOO */
    lw(a5, -164, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a3,a0);        /* ZZZZ */
    lw(a5, -168, s0);             /* SSSS */
    addiw(a5, a5, 1);           /* LLLL */
    sextw(a4,a5);        /* ZZZZ */
    addi(a5, s0, -88);           /* LLLL */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a3);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($branch_elementAt);                      /* XXXX */
    lw(a5, -72, s0);             /* SSSS */
    sw(a5, -24, s0);             /* OOOO */
    lw(a5, -164, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($hasLeavesForChildren);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    beq(a5, zero, $L183);             /* YYYY */
    lw(a5, -20, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($leafSize);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -36, s0);             /* OOOO */
    lw(a5, -24, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($leafSize);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -40, s0);             /* OOOO */
    lw(a5, -36, s0);             /* SSSS */
    sextw(a4,a5);        /* ZZZZ */
    li(a5,1);        /* ZZZZ */
    ble(a4, a5, $L184);             /* YYYY */
    li(a5,0);        /* ZZZZ */
    j($L192);                       /* WWWW */
    $L184.set();
    lw(a5, -40, s0);             /* SSSS */
    sextw(a4,a5);        /* ZZZZ */
    li(a5,1);        /* ZZZZ */
    bgt(a4, a5, $L186);             /* YYYY */
    li(a5,0);        /* ZZZZ */
    j($L192);                       /* WWWW */
    $L186.set();
    lw(a5, -24, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($leaf);                      /* XXXX */
    mv(a4,a0);        /* ZZZZ */
    addi(a5, s0, -112);           /* LLLL */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($leaf_firstElement);                      /* XXXX */
    lw(a5, -20, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($leaf);                      /* XXXX */
    mv(a3,a0);        /* ZZZZ */
    lw(a5, -100, s0);             /* SSSS */
    lbu(a4, -96, s0);             /* SSSS */
    sextw(a4,a4);        /* ZZZZ */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    mv(a0,a3);        /* ZZZZ */
    call($leaf_push);                      /* XXXX */
    lw(a5, -164, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    lw(a5, -100, s0);             /* SSSS */
    lw(a3, -168, s0);             /* SSSS */
    lw(a4, -20, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    call($branch_setElementAt);                      /* XXXX */
    lw(a5, -24, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($leaf);                      /* XXXX */
    mv(a4,a0);        /* ZZZZ */
    addi(a5, s0, -208);           /* LLLL */
    li(a2,0);        /* ZZZZ */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($leaf_removeElementAt);                      /* XXXX */
    j($L187);                       /* WWWW */
    $L183.set();
    lw(a5, -20, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branchSize);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -28, s0);             /* OOOO */
    lw(a5, -24, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branchSize);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -32, s0);             /* OOOO */
    lw(a5, -28, s0);             /* SSSS */
    sextw(a4,a5);        /* ZZZZ */
    li(a5,2);        /* ZZZZ */
    ble(a4, a5, $L189);             /* YYYY */
    li(a5,0);        /* ZZZZ */
    j($L192);                       /* WWWW */
    $L189.set();
    lw(a5, -32, s0);             /* SSSS */
    sextw(a4,a5);        /* ZZZZ */
    li(a5,1);        /* ZZZZ */
    bgt(a4, a5, $L191);             /* YYYY */
    li(a5,0);        /* ZZZZ */
    j($L192);                       /* WWWW */
    $L191.set();
    lw(a5, -20, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a4,a0);        /* ZZZZ */
    addi(a5, s0, -136);           /* LLLL */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($branch_lastElement);                      /* XXXX */
    lw(a5, -20, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    lw(a5, -52, s0);             /* SSSS */
    lw(a4, -120, s0);             /* SSSS */
    lw(a3, -28, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    call($branch_setElementAt);                      /* XXXX */
    lw(a5, -24, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a4,a0);        /* ZZZZ */
    addi(a5, s0, -160);           /* LLLL */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($branch_firstElement);                      /* XXXX */
    lw(a5, -20, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a4,a0);        /* ZZZZ */
    lw(a5, -144, s0);             /* SSSS */
    mv(a2,a5);        /* ZZZZ */
    li(a1,0);        /* ZZZZ */
    mv(a0,a4);        /* ZZZZ */
    call($branch_push);                      /* XXXX */
    lw(a5, -164, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    lw(a5, -148, s0);             /* SSSS */
    lw(a3, -168, s0);             /* SSSS */
    lw(a4, -20, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    call($branch_setElementAt);                      /* XXXX */
    lw(a5, -24, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a4,a0);        /* ZZZZ */
    addi(a5, s0, -208);           /* LLLL */
    li(a2,0);        /* ZZZZ */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($branch_removeElementAt);                      /* XXXX */
    $L187.set();
    li(a5,1);        /* ZZZZ */
    $L192.set();
    mv(a0,a5);        /* ZZZZ */
    ld(ra, sp, 200);             /* GGGG */
    ld(s0, sp, 192);             /* GGGG */
    addi(sp, sp, 208);           /* LLLL */
    jr(ra);                       /* UUUU */
    $mergeRoot.set();
    addi(sp, sp, -304);           /* LLLL */
    sd(ra, 296, sp);             /* OOOO */
    sd(s0, 288, sp);             /* OOOO */
    sd(s1, 280, sp);             /* OOOO */
    addi(s0, sp, 304);           /* LLLL */
    call($rootIsLeaf);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    bne(a5, zero, $L194);             /* YYYY */
    li(a0,0);        /* ZZZZ */
    call($branchSize);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    mv(a4,a5);        /* ZZZZ */
    li(a5,1);        /* ZZZZ */
    ble(a4, a5, $L195);             /* YYYY */
    $L194.set();
    li(a5,0);        /* ZZZZ */
    j($L196);                       /* WWWW */
    $L195.set();
    sw(zero, -52, s0);             /* OOOO */
    lw(a5, -52, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a4,a0);        /* ZZZZ */
    addi(a5, s0, -176);           /* LLLL */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($branch_firstElement);                      /* XXXX */
    lw(a5, -160, s0);             /* SSSS */
    sw(a5, -56, s0);             /* OOOO */
    lw(a5, -52, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a4,a0);        /* ZZZZ */
    addi(a5, s0, -152);           /* LLLL */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($branch_lastElement);                      /* XXXX */
    lw(a5, -136, s0);             /* SSSS */
    sw(a5, -60, s0);             /* OOOO */
    lw(a5, -52, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($hasLeavesForChildren);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    beq(a5, zero, $L197);             /* YYYY */
    lw(a5, -56, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($leafSize);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    mv(s1,a5);        /* ZZZZ */
    lw(a5, -60, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($leafSize);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    addw(a5,s1,a5);        /* ZZZZ */
    sextw(a5,a5);        /* ZZZZ */
    mv(a4,a5);        /* ZZZZ */
    li(a5,2);        /* ZZZZ */
    bgt(a4, a5, $L198);             /* YYYY */
    lw(a5, -52, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($clear);                      /* XXXX */
    lw(a5, -56, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($leafSize);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -80, s0);             /* OOOO */
    sw(zero, -36, s0);             /* OOOO */
    j($L199);                       /* WWWW */
    $L200.set();
    lw(a5, -56, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($leaf);                      /* XXXX */
    mv(a4,a0);        /* ZZZZ */
    addi(a5, s0, -200);           /* LLLL */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($leaf_shift);                      /* XXXX */
    lw(a5, -52, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($leaf);                      /* XXXX */
    mv(a3,a0);        /* ZZZZ */
    lw(a5, -188, s0);             /* SSSS */
    lbu(a4, -184, s0);             /* SSSS */
    sextw(a4,a4);        /* ZZZZ */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    mv(a0,a3);        /* ZZZZ */
    call($leaf_push);                      /* XXXX */
    lw(a5, -36, s0);             /* SSSS */
    addiw(a5, a5, 1);           /* LLLL */
    sw(a5, -36, s0);             /* OOOO */
    $L199.set();
    lw(a5, -36, s0);             /* SSSS */
    mv(a4,a5);        /* ZZZZ */
    lw(a5, -80, s0);             /* SSSS */
    sextw(a4,a4);        /* ZZZZ */
    sextw(a5,a5);        /* ZZZZ */
    blt(a4, a5, $L200);             /* YYYY */
    lw(a5, -60, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($leafSize);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -84, s0);             /* OOOO */
    sw(zero, -40, s0);             /* OOOO */
    j($L201);                       /* WWWW */
    $L202.set();
    lw(a5, -60, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($leaf);                      /* XXXX */
    mv(a4,a0);        /* ZZZZ */
    addi(a5, s0, -224);           /* LLLL */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($leaf_shift);                      /* XXXX */
    lw(a5, -52, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($leaf);                      /* XXXX */
    mv(a3,a0);        /* ZZZZ */
    lw(a5, -212, s0);             /* SSSS */
    lbu(a4, -208, s0);             /* SSSS */
    sextw(a4,a4);        /* ZZZZ */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    mv(a0,a3);        /* ZZZZ */
    call($leaf_push);                      /* XXXX */
    lw(a5, -40, s0);             /* SSSS */
    addiw(a5, a5, 1);           /* LLLL */
    sw(a5, -40, s0);             /* OOOO */
    $L201.set();
    lw(a5, -40, s0);             /* SSSS */
    mv(a4,a5);        /* ZZZZ */
    lw(a5, -84, s0);             /* SSSS */
    sextw(a4,a4);        /* ZZZZ */
    sextw(a5,a5);        /* ZZZZ */
    blt(a4, a5, $L202);             /* YYYY */
    lw(a5, -52, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($setLeaf);                      /* XXXX */
    lw(a5, -56, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($freeNode);                      /* XXXX */
    lw(a5, -60, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($freeNode);                      /* XXXX */
    li(a5,1);        /* ZZZZ */
    j($L196);                       /* WWWW */
    $L197.set();
    lw(a5, -56, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branchSize);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    addiw(a5, a5, 1);           /* LLLL */
    sextw(s1,a5);        /* ZZZZ */
    lw(a5, -60, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branchSize);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    addw(a5,s1,a5);        /* ZZZZ */
    sextw(a5,a5);        /* ZZZZ */
    mv(a4,a5);        /* ZZZZ */
    li(a5,3);        /* ZZZZ */
    bgt(a4, a5, $L198);             /* YYYY */
    lw(a5, -52, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a4,a0);        /* ZZZZ */
    addi(a5, s0, -248);           /* LLLL */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($branch_firstElement);                      /* XXXX */
    lw(a5, -52, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($clear);                      /* XXXX */
    lw(a5, -56, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branchSize);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -64, s0);             /* OOOO */
    sw(zero, -44, s0);             /* OOOO */
    j($L203);                       /* WWWW */
    $L204.set();
    lw(a5, -56, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a4,a0);        /* ZZZZ */
    addi(a5, s0, -272);           /* LLLL */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($branch_shift);                      /* XXXX */
    lw(a5, -52, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a3,a0);        /* ZZZZ */
    lw(a5, -260, s0);             /* SSSS */
    lw(a4, -256, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    mv(a0,a3);        /* ZZZZ */
    call($branch_push);                      /* XXXX */
    lw(a5, -44, s0);             /* SSSS */
    addiw(a5, a5, 1);           /* LLLL */
    sw(a5, -44, s0);             /* OOOO */
    $L203.set();
    lw(a5, -44, s0);             /* SSSS */
    mv(a4,a5);        /* ZZZZ */
    lw(a5, -64, s0);             /* SSSS */
    sextw(a4,a4);        /* ZZZZ */
    sextw(a5,a5);        /* ZZZZ */
    blt(a4, a5, $L204);             /* YYYY */
    lw(a5, -56, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a4,a0);        /* ZZZZ */
    addi(a5, s0, -128);           /* LLLL */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($branch_lastElement);                      /* XXXX */
    lw(a5, -112, s0);             /* SSSS */
    sw(a5, -68, s0);             /* OOOO */
    lw(a5, -52, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a3,a0);        /* ZZZZ */
    lw(a5, -236, s0);             /* SSSS */
    lw(a4, -68, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    mv(a0,a3);        /* ZZZZ */
    call($branch_push);                      /* XXXX */
    lw(a5, -60, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branchSize);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -72, s0);             /* OOOO */
    sw(zero, -48, s0);             /* OOOO */
    j($L205);                       /* WWWW */
    $L206.set();
    lw(a5, -60, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a4,a0);        /* ZZZZ */
    addi(a5, s0, -296);           /* LLLL */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($branch_shift);                      /* XXXX */
    lw(a5, -52, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a3,a0);        /* ZZZZ */
    lw(a5, -284, s0);             /* SSSS */
    lw(a4, -280, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    mv(a0,a3);        /* ZZZZ */
    call($branch_push);                      /* XXXX */
    lw(a5, -48, s0);             /* SSSS */
    addiw(a5, a5, 1);           /* LLLL */
    sw(a5, -48, s0);             /* OOOO */
    $L205.set();
    lw(a5, -48, s0);             /* SSSS */
    mv(a4,a5);        /* ZZZZ */
    lw(a5, -72, s0);             /* SSSS */
    sextw(a4,a4);        /* ZZZZ */
    sextw(a5,a5);        /* ZZZZ */
    blt(a4, a5, $L206);             /* YYYY */
    lw(a5, -60, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a4,a0);        /* ZZZZ */
    addi(a5, s0, -104);           /* LLLL */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($branch_lastElement);                      /* XXXX */
    lw(a5, -88, s0);             /* SSSS */
    sw(a5, -76, s0);             /* OOOO */
    lw(a5, -52, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a4,a0);        /* ZZZZ */
    lw(a5, -76, s0);             /* SSSS */
    mv(a2,a5);        /* ZZZZ */
    li(a1,0);        /* ZZZZ */
    mv(a0,a4);        /* ZZZZ */
    call($branch_push);                      /* XXXX */
    lw(a5, -56, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($freeNode);                      /* XXXX */
    lw(a5, -60, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($freeNode);                      /* XXXX */
    li(a5,1);        /* ZZZZ */
    j($L196);                       /* WWWW */
    $L198.set();
    li(a5,0);        /* ZZZZ */
    $L196.set();
    mv(a0,a5);        /* ZZZZ */
    ld(ra, sp, 296);             /* GGGG */
    ld(s0, sp, 288);             /* GGGG */
    ld(s1, sp, 280);             /* GGGG */
    addi(sp, sp, 304);           /* LLLL */
    jr(ra);                       /* UUUU */
    $mergeLeftSibling.set();
    addi(sp, sp, -272);           /* LLLL */
    sd(ra, 264, sp);             /* OOOO */
    sd(s0, 256, sp);             /* OOOO */
    addi(s0, sp, 272);           /* LLLL */
    mv(a5,a0);        /* ZZZZ */
    mv(a4,a1);        /* ZZZZ */
    sw(a5, -228, s0);             /* OOOO */
    mv(a5,a4);        /* ZZZZ */
    sw(a5, -232, s0);             /* OOOO */
    lw(a5, -232, s0);             /* SSSS */
    sextw(a5,a5);        /* ZZZZ */
    bne(a5, zero, $L208);             /* YYYY */
    li(a5,0);        /* ZZZZ */
    j($L220);                       /* WWWW */
    $L208.set();
    lw(a5, -228, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branchSize);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -28, s0);             /* OOOO */
    lw(a5, -28, s0);             /* SSSS */
    sextw(a4,a5);        /* ZZZZ */
    li(a5,1);        /* ZZZZ */
    bgt(a4, a5, $L210);             /* YYYY */
    li(a5,0);        /* ZZZZ */
    j($L220);                       /* WWWW */
    $L210.set();
    lw(a5, -228, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a3,a0);        /* ZZZZ */
    lw(a5, -232, s0);             /* SSSS */
    addiw(a5, a5, -1);           /* LLLL */
    sextw(a4,a5);        /* ZZZZ */
    addi(a5, s0, -120);           /* LLLL */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a3);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($branch_elementAt);                      /* XXXX */
    lw(a5, -228, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a3,a0);        /* ZZZZ */
    addi(a5, s0, -144);           /* LLLL */
    lw(a4, -232, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a3);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($branch_elementAt);                      /* XXXX */
    lw(a5, -228, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($hasLeavesForChildren);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    beq(a5, zero, $L211);             /* YYYY */
    lw(a5, -104, s0);             /* SSSS */
    sw(a5, -56, s0);             /* OOOO */
    lw(a5, -128, s0);             /* SSSS */
    sw(a5, -60, s0);             /* OOOO */
    lw(a5, -56, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($leafSize);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -64, s0);             /* OOOO */
    lw(a5, -60, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($leafSize);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -68, s0);             /* OOOO */
    lw(a5, -64, s0);             /* SSSS */
    mv(a4,a5);        /* ZZZZ */
    lw(a5, -68, s0);             /* SSSS */
    addw(a5,a4,a5);        /* ZZZZ */
    sextw(a5,a5);        /* ZZZZ */
    mv(a4,a5);        /* ZZZZ */
    li(a5,1);        /* ZZZZ */
    ble(a4, a5, $L212);             /* YYYY */
    li(a5,0);        /* ZZZZ */
    j($L220);                       /* WWWW */
    $L212.set();
    lw(a5, -56, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($leaf);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($leaf_size);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -72, s0);             /* OOOO */
    sw(zero, -20, s0);             /* OOOO */
    j($L213);                       /* WWWW */
    $L214.set();
    lw(a5, -56, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($leaf);                      /* XXXX */
    mv(a4,a0);        /* ZZZZ */
    addi(a5, s0, -168);           /* LLLL */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($leaf_pop);                      /* XXXX */
    lw(a5, -60, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($leaf);                      /* XXXX */
    lw(a5, -156, s0);             /* SSSS */
    lbu(a4, -152, s0);             /* SSSS */
    sextw(a4,a4);        /* ZZZZ */
    li(a3,0);        /* ZZZZ */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    call($leaf_insertElementAt);                      /* XXXX */
    lw(a5, -20, s0);             /* SSSS */
    addiw(a5, a5, 1);           /* LLLL */
    sw(a5, -20, s0);             /* OOOO */
    $L213.set();
    lw(a5, -20, s0);             /* SSSS */
    mv(a4,a5);        /* ZZZZ */
    lw(a5, -72, s0);             /* SSSS */
    sextw(a4,a4);        /* ZZZZ */
    sextw(a5,a5);        /* ZZZZ */
    blt(a4, a5, $L214);             /* YYYY */
    lw(a5, -56, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($freeNode);                      /* XXXX */
    j($L215);                       /* WWWW */
    $L211.set();
    lw(a5, -104, s0);             /* SSSS */
    sw(a5, -32, s0);             /* OOOO */
    lw(a5, -128, s0);             /* SSSS */
    sw(a5, -36, s0);             /* OOOO */
    lw(a5, -32, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branchSize);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -40, s0);             /* OOOO */
    lw(a5, -36, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branchSize);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -44, s0);             /* OOOO */
    lw(a5, -40, s0);             /* SSSS */
    addiw(a5, a5, 1);           /* LLLL */
    sextw(a5,a5);        /* ZZZZ */
    lw(a4, -44, s0);             /* SSSS */
    addw(a5,a4,a5);        /* ZZZZ */
    sextw(a5,a5);        /* ZZZZ */
    mv(a4,a5);        /* ZZZZ */
    li(a5,2);        /* ZZZZ */
    ble(a4, a5, $L216);             /* YYYY */
    li(a5,0);        /* ZZZZ */
    j($L220);                       /* WWWW */
    $L216.set();
    lw(a5, -228, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a3,a0);        /* ZZZZ */
    lw(a5, -232, s0);             /* SSSS */
    addiw(a5, a5, -1);           /* LLLL */
    sextw(a4,a5);        /* ZZZZ */
    addi(a5, s0, -96);           /* LLLL */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a3);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($branch_elementAt);                      /* XXXX */
    lw(a5, -84, s0);             /* SSSS */
    sw(a5, -48, s0);             /* OOOO */
    lw(a5, -32, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a4,a0);        /* ZZZZ */
    addi(a5, s0, -192);           /* LLLL */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($branch_lastElement);                      /* XXXX */
    lw(a5, -36, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    lw(a4, -176, s0);             /* SSSS */
    lw(a5, -48, s0);             /* SSSS */
    li(a3,0);        /* ZZZZ */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    call($branch_insertElementAt);                      /* XXXX */
    lw(a5, -32, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a4,a0);        /* ZZZZ */
    addi(a5, s0, -272);           /* LLLL */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($branch_pop);                      /* XXXX */
    lw(a5, -32, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branchSize);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -52, s0);             /* OOOO */
    sw(zero, -24, s0);             /* OOOO */
    j($L218);                       /* WWWW */
    $L219.set();
    lw(a5, -32, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a4,a0);        /* ZZZZ */
    addi(a5, s0, -216);           /* LLLL */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($branch_pop);                      /* XXXX */
    lw(a5, -36, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    lw(a5, -204, s0);             /* SSSS */
    lw(a4, -200, s0);             /* SSSS */
    li(a3,0);        /* ZZZZ */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    call($branch_insertElementAt);                      /* XXXX */
    lw(a5, -24, s0);             /* SSSS */
    addiw(a5, a5, 1);           /* LLLL */
    sw(a5, -24, s0);             /* OOOO */
    $L218.set();
    lw(a5, -24, s0);             /* SSSS */
    mv(a4,a5);        /* ZZZZ */
    lw(a5, -52, s0);             /* SSSS */
    sextw(a4,a4);        /* ZZZZ */
    sextw(a5,a5);        /* ZZZZ */
    blt(a4, a5, $L219);             /* YYYY */
    lw(a5, -32, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($freeNode);                      /* XXXX */
    $L215.set();
    lw(a5, -228, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a3,a0);        /* ZZZZ */
    lw(a5, -232, s0);             /* SSSS */
    addiw(a5, a5, -1);           /* LLLL */
    sextw(a4,a5);        /* ZZZZ */
    addi(a5, s0, -272);           /* LLLL */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a3);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($branch_removeElementAt);                      /* XXXX */
    li(a5,1);        /* ZZZZ */
    $L220.set();
    mv(a0,a5);        /* ZZZZ */
    ld(ra, sp, 264);             /* GGGG */
    ld(s0, sp, 256);             /* GGGG */
    addi(sp, sp, 272);           /* LLLL */
    jr(ra);                       /* UUUU */
    $mergeRightSibling.set();
    addi(sp, sp, -304);           /* LLLL */
    sd(ra, 296, sp);             /* OOOO */
    sd(s0, 288, sp);             /* OOOO */
    addi(s0, sp, 304);           /* LLLL */
    mv(a5,a0);        /* ZZZZ */
    mv(a4,a1);        /* ZZZZ */
    sw(a5, -260, s0);             /* OOOO */
    mv(a5,a4);        /* ZZZZ */
    sw(a5, -264, s0);             /* OOOO */
    lw(a5, -260, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branchSize);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -28, s0);             /* OOOO */
    lw(a5, -28, s0);             /* SSSS */
    sextw(a4,a5);        /* ZZZZ */
    li(a5,1);        /* ZZZZ */
    bgt(a4, a5, $L222);             /* YYYY */
    li(a5,0);        /* ZZZZ */
    j($L233);                       /* WWWW */
    $L222.set();
    lw(a5, -260, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a3,a0);        /* ZZZZ */
    addi(a5, s0, -88);           /* LLLL */
    lw(a4, -264, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a3);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($branch_elementAt);                      /* XXXX */
    lw(a5, -260, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a3,a0);        /* ZZZZ */
    lw(a5, -264, s0);             /* SSSS */
    addiw(a5, a5, 1);           /* LLLL */
    sextw(a4,a5);        /* ZZZZ */
    addi(a5, s0, -112);           /* LLLL */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a3);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($branch_elementAt);                      /* XXXX */
    lw(a5, -260, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($hasLeavesForChildren);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    beq(a5, zero, $L224);             /* YYYY */
    lw(a5, -72, s0);             /* SSSS */
    sw(a5, -52, s0);             /* OOOO */
    lw(a5, -96, s0);             /* SSSS */
    sw(a5, -56, s0);             /* OOOO */
    lw(a5, -52, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($leafSize);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -60, s0);             /* OOOO */
    lw(a5, -56, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($leafSize);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -64, s0);             /* OOOO */
    lw(a5, -60, s0);             /* SSSS */
    mv(a4,a5);        /* ZZZZ */
    lw(a5, -64, s0);             /* SSSS */
    addw(a5,a4,a5);        /* ZZZZ */
    sextw(a5,a5);        /* ZZZZ */
    mv(a4,a5);        /* ZZZZ */
    li(a5,2);        /* ZZZZ */
    ble(a4, a5, $L225);             /* YYYY */
    li(a5,0);        /* ZZZZ */
    j($L233);                       /* WWWW */
    $L225.set();
    lw(a5, -56, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($leaf);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($leaf_size);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -68, s0);             /* OOOO */
    sw(zero, -20, s0);             /* OOOO */
    j($L226);                       /* WWWW */
    $L227.set();
    lw(a5, -56, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($leaf);                      /* XXXX */
    mv(a4,a0);        /* ZZZZ */
    addi(a5, s0, -184);           /* LLLL */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($leaf_shift);                      /* XXXX */
    lw(a5, -52, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($leaf);                      /* XXXX */
    mv(a3,a0);        /* ZZZZ */
    lw(a5, -172, s0);             /* SSSS */
    lbu(a4, -168, s0);             /* SSSS */
    sextw(a4,a4);        /* ZZZZ */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    mv(a0,a3);        /* ZZZZ */
    call($leaf_push);                      /* XXXX */
    lw(a5, -20, s0);             /* SSSS */
    addiw(a5, a5, 1);           /* LLLL */
    sw(a5, -20, s0);             /* OOOO */
    $L226.set();
    lw(a5, -20, s0);             /* SSSS */
    mv(a4,a5);        /* ZZZZ */
    lw(a5, -68, s0);             /* SSSS */
    sextw(a4,a4);        /* ZZZZ */
    sextw(a5,a5);        /* ZZZZ */
    blt(a4, a5, $L227);             /* YYYY */
    lw(a5, -56, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($freeNode);                      /* XXXX */
    j($L228);                       /* WWWW */
    $L224.set();
    lw(a5, -72, s0);             /* SSSS */
    sw(a5, -32, s0);             /* OOOO */
    lw(a5, -96, s0);             /* SSSS */
    sw(a5, -36, s0);             /* OOOO */
    lw(a5, -32, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branchSize);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -40, s0);             /* OOOO */
    lw(a5, -36, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branchSize);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -44, s0);             /* OOOO */
    lw(a5, -40, s0);             /* SSSS */
    addiw(a5, a5, 1);           /* LLLL */
    sextw(a5,a5);        /* ZZZZ */
    lw(a4, -44, s0);             /* SSSS */
    addw(a5,a4,a5);        /* ZZZZ */
    sextw(a5,a5);        /* ZZZZ */
    mv(a4,a5);        /* ZZZZ */
    li(a5,2);        /* ZZZZ */
    ble(a4, a5, $L229);             /* YYYY */
    li(a5,0);        /* ZZZZ */
    j($L233);                       /* WWWW */
    $L229.set();
    lw(a5, -32, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a4,a0);        /* ZZZZ */
    addi(a5, s0, -208);           /* LLLL */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($branch_lastElement);                      /* XXXX */
    lw(a5, -260, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a3,a0);        /* ZZZZ */
    addi(a5, s0, -232);           /* LLLL */
    lw(a4, -264, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a3);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($branch_elementAt);                      /* XXXX */
    lw(a5, -32, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    lw(a5, -220, s0);             /* SSSS */
    lw(a4, -192, s0);             /* SSSS */
    lw(a3, -40, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    call($branch_setElementAt);                      /* XXXX */
    lw(a5, -36, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branchSize);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -48, s0);             /* OOOO */
    sw(zero, -24, s0);             /* OOOO */
    j($L231);                       /* WWWW */
    $L232.set();
    lw(a5, -36, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a4,a0);        /* ZZZZ */
    addi(a5, s0, -256);           /* LLLL */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($branch_shift);                      /* XXXX */
    lw(a5, -32, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a3,a0);        /* ZZZZ */
    lw(a5, -244, s0);             /* SSSS */
    lw(a4, -240, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    mv(a0,a3);        /* ZZZZ */
    call($branch_push);                      /* XXXX */
    lw(a5, -24, s0);             /* SSSS */
    addiw(a5, a5, 1);           /* LLLL */
    sw(a5, -24, s0);             /* OOOO */
    $L231.set();
    lw(a5, -24, s0);             /* SSSS */
    mv(a4,a5);        /* ZZZZ */
    lw(a5, -48, s0);             /* SSSS */
    sextw(a4,a4);        /* ZZZZ */
    sextw(a5,a5);        /* ZZZZ */
    blt(a4, a5, $L232);             /* YYYY */
    lw(a5, -36, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($freeNode);                      /* XXXX */
    $L228.set();
    lw(a5, -260, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a3,a0);        /* ZZZZ */
    lw(a5, -264, s0);             /* SSSS */
    addiw(a5, a5, 1);           /* LLLL */
    sextw(a4,a5);        /* ZZZZ */
    addi(a5, s0, -136);           /* LLLL */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a3);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($branch_elementAt);                      /* XXXX */
    lw(a5, -260, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a3,a0);        /* ZZZZ */
    addi(a5, s0, -160);           /* LLLL */
    lw(a4, -264, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a3);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($branch_elementAt);                      /* XXXX */
    lw(a5, -260, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    lw(a5, -124, s0);             /* SSSS */
    lw(a4, -144, s0);             /* SSSS */
    lw(a3, -264, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    call($branch_setElementAt);                      /* XXXX */
    lw(a5, -260, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a3,a0);        /* ZZZZ */
    lw(a5, -264, s0);             /* SSSS */
    addiw(a5, a5, 1);           /* LLLL */
    sextw(a4,a5);        /* ZZZZ */
    addi(a5, s0, -304);           /* LLLL */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a3);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($branch_removeElementAt);                      /* XXXX */
    li(a5,1);        /* ZZZZ */
    $L233.set();
    mv(a0,a5);        /* ZZZZ */
    ld(ra, sp, 296);             /* GGGG */
    ld(s0, sp, 288);             /* GGGG */
    addi(sp, sp, 304);           /* LLLL */
    jr(ra);                       /* UUUU */
    $balance.set();
    addi(sp, sp, -64);           /* LLLL */
    sd(ra, 56, sp);             /* OOOO */
    sd(s0, 48, sp);             /* OOOO */
    addi(s0, sp, 64);           /* LLLL */
    mv(a5,a0);        /* ZZZZ */
    mv(a4,a1);        /* ZZZZ */
    sw(a5, -52, s0);             /* OOOO */
    mv(a5,a4);        /* ZZZZ */
    sw(a5, -56, s0);             /* OOOO */
    lw(a5, -52, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a3,a0);        /* ZZZZ */
    addi(a5, s0, -40);           /* LLLL */
    lw(a4, -56, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a3);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($branch_elementAt);                      /* XXXX */
    lw(a5, -24, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($isLow);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    beq(a5, zero, $L242);             /* YYYY */
    lw(a4, -56, s0);             /* SSSS */
    lw(a5, -52, s0);             /* SSSS */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($stealFromLeft);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    bne(a5, zero, $L243);             /* YYYY */
    lw(a4, -56, s0);             /* SSSS */
    lw(a5, -52, s0);             /* SSSS */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($stealFromRight);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    bne(a5, zero, $L244);             /* YYYY */
    lw(a4, -56, s0);             /* SSSS */
    lw(a5, -52, s0);             /* SSSS */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($mergeLeftSibling);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    bne(a5, zero, $L245);             /* YYYY */
    lw(a4, -56, s0);             /* SSSS */
    lw(a5, -52, s0);             /* SSSS */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($mergeRightSibling);                      /* XXXX */
    j($L234);                       /* WWWW */
    $L242.set();
    nop();        /* ZZZZ */
    j($L234);                       /* WWWW */
    $L243.set();
    nop();        /* ZZZZ */
    j($L234);                       /* WWWW */
    $L244.set();
    nop();        /* ZZZZ */
    j($L234);                       /* WWWW */
    $L245.set();
    nop();        /* ZZZZ */
    $L234.set();
    ld(ra, sp, 56);             /* GGGG */
    ld(s0, sp, 48);             /* GGGG */
    addi(sp, sp, 64);           /* LLLL */
    jr(ra);                       /* UUUU */
    $find_result.set();
    addi(sp, sp, -80);           /* LLLL */
    sd(s0, 72, sp);             /* OOOO */
    addi(s0, sp, 80);           /* LLLL */
    sd(a0, -56, s0);             /* OOOO */
    mv(a0,a1);        /* ZZZZ */
    mv(a1,a2);        /* ZZZZ */
    mv(a2,a3);        /* ZZZZ */
    mv(a3,a4);        /* ZZZZ */
    mv(a4,a5);        /* ZZZZ */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -60, s0);             /* OOOO */
    mv(a5,a1);        /* ZZZZ */
    sw(a5, -64, s0);             /* OOOO */
    mv(a5,a2);        /* ZZZZ */
    sw(a5, -68, s0);             /* OOOO */
    mv(a5,a3);        /* ZZZZ */
    sw(a5, -72, s0);             /* OOOO */
    mv(a5,a4);        /* ZZZZ */
    sw(a5, -76, s0);             /* OOOO */
    lw(a5, -60, s0);             /* SSSS */
    sw(a5, -40, s0);             /* OOOO */
    lw(a5, -64, s0);             /* SSSS */
    sw(a5, -36, s0);             /* OOOO */
    lw(a5, -68, s0);             /* SSSS */
    sw(a5, -32, s0);             /* OOOO */
    lw(a5, -72, s0);             /* SSSS */
    sw(a5, -28, s0);             /* OOOO */
    lw(a5, -76, s0);             /* SSSS */
    sw(a5, -24, s0);             /* OOOO */
    ld(a5, s0, 56);             /* GGGG */
    lw(a4, -40, s0);             /* SSSS */
    mv(a0,a4);        /* ZZZZ */
    lw(a4, -36, s0);             /* SSSS */
    mv(a1,a4);        /* ZZZZ */
    lw(a4, -32, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    lw(a4, -28, s0);             /* SSSS */
    mv(a3,a4);        /* ZZZZ */
    lw(a4, -24, s0);             /* SSSS */
    sw(a0, 0, a5);             /* OOOO */
    sw(a1, 4, a5);             /* OOOO */
    sw(a2, 8, a5);             /* OOOO */
    sw(a3, 12, a5);             /* OOOO */
    sw(a4, 16, a5);             /* OOOO */
    ld(a0, s0, 56);             /* GGGG */
    ld(s0, sp, 72);             /* GGGG */
    addi(sp, sp, 80);           /* LLLL */
    jr(ra);                       /* UUUU */
    $find.set();
    addi(sp, sp, -144);           /* LLLL */
    sd(ra, 136, sp);             /* OOOO */
    sd(s0, 128, sp);             /* OOOO */
    addi(s0, sp, 144);           /* LLLL */
    sd(a0, -136, s0);             /* OOOO */
    mv(a5,a1);        /* ZZZZ */
    sw(a5, -140, s0);             /* OOOO */
    call($rootIsLeaf);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    beq(a5, zero, $L249);             /* YYYY */
    li(a0,0);        /* ZZZZ */
    call($leaf);                      /* XXXX */
    mv(a3,a0);        /* ZZZZ */
    addi(a5, s0, -72);           /* LLLL */
    lw(a4, -140, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a3);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($leaf_search);                      /* XXXX */
    lw(a2, -68, s0);             /* SSSS */
    lw(a3, -64, s0);             /* SSSS */
    lbu(a5, -56, s0);             /* SSSS */
    sextw(a5,a5);        /* ZZZZ */
    ld(a0, s0, 136);             /* GGGG */
    lw(a4, -140, s0);             /* SSSS */
    li(a1,0);        /* ZZZZ */
    call($find_result);                      /* XXXX */
    j($L254);                       /* WWWW */
    $L249.set();
    sw(zero, -20, s0);             /* OOOO */
    sw(zero, -24, s0);             /* OOOO */
    j($L251);                       /* WWWW */
    $L253.set();
    lw(a5, -20, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a3,a0);        /* ZZZZ */
    addi(a5, s0, -96);           /* LLLL */
    lw(a4, -140, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a3);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($branch_searchFirstGreaterThanOrEqualExceptLast);                      /* XXXX */
    lw(a5, -80, s0);             /* SSSS */
    sw(a5, -28, s0);             /* OOOO */
    lw(a5, -28, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($isLeaf);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    beq(a5, zero, $L252);             /* YYYY */
    lw(a5, -28, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($leaf);                      /* XXXX */
    mv(a3,a0);        /* ZZZZ */
    addi(a5, s0, -120);           /* LLLL */
    lw(a4, -140, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a3);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($leaf_search);                      /* XXXX */
    lw(a2, -116, s0);             /* SSSS */
    lw(a3, -112, s0);             /* SSSS */
    lbu(a5, -104, s0);             /* SSSS */
    sextw(a5,a5);        /* ZZZZ */
    ld(a0, s0, 136);             /* GGGG */
    lw(a4, -140, s0);             /* SSSS */
    lw(a1, -28, s0);             /* SSSS */
    call($find_result);                      /* XXXX */
    j($L254);                       /* WWWW */
    $L252.set();
    lw(a5, -28, s0);             /* SSSS */
    sw(a5, -20, s0);             /* OOOO */
    lw(a5, -24, s0);             /* SSSS */
    addiw(a5, a5, 1);           /* LLLL */
    sw(a5, -24, s0);             /* OOOO */
    $L251.set();
    lw(a5, -24, s0);             /* SSSS */
    sextw(a4,a5);        /* ZZZZ */
    li(a5,9);        /* ZZZZ */
    ble(a4, a5, $L253);             /* YYYY */
    sw(zero, -48, s0);             /* OOOO */
    sw(zero, -44, s0);             /* OOOO */
    sw(zero, -40, s0);             /* OOOO */
    sw(zero, -36, s0);             /* OOOO */
    sw(zero, -32, s0);             /* OOOO */
    ld(a5, s0, 136);             /* GGGG */
    lw(a4, -48, s0);             /* SSSS */
    mv(a0,a4);        /* ZZZZ */
    lw(a4, -44, s0);             /* SSSS */
    mv(a1,a4);        /* ZZZZ */
    lw(a4, -40, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    lw(a4, -36, s0);             /* SSSS */
    mv(a3,a4);        /* ZZZZ */
    lw(a4, -32, s0);             /* SSSS */
    sw(a0, 0, a5);             /* OOOO */
    sw(a1, 4, a5);             /* OOOO */
    sw(a2, 8, a5);             /* OOOO */
    sw(a3, 12, a5);             /* OOOO */
    sw(a4, 16, a5);             /* OOOO */
    $L254.set();
    ld(a0, s0, 136);             /* GGGG */
    ld(ra, sp, 136);             /* GGGG */
    ld(s0, sp, 128);             /* GGGG */
    addi(sp, sp, 144);           /* LLLL */
    jr(ra);                       /* UUUU */
    $findAndInsert_result.set();
    addi(sp, sp, -64);           /* LLLL */
    sd(s0, 56, sp);             /* OOOO */
    sd(s1, 48, sp);             /* OOOO */
    addi(s0, sp, 64);           /* LLLL */
    sd(a0, -56, s0);             /* OOOO */
    mv(s1,a1);        /* ZZZZ */
    mv(a5,a2);        /* ZZZZ */
    mv(a4,a3);        /* ZZZZ */
    sw(a5, -60, s0);             /* OOOO */
    mv(a5,a4);        /* ZZZZ */
    sw(a5, -64, s0);             /* OOOO */
    lw(a5, 0, s1);             /* SSSS */
    mv(a1,a5);        /* ZZZZ */
    lw(a5, 4, s1);             /* SSSS */
    mv(a2,a5);        /* ZZZZ */
    lw(a5, 8, s1);             /* SSSS */
    mv(a3,a5);        /* ZZZZ */
    lw(a5, 12, s1);             /* SSSS */
    mv(a4,a5);        /* ZZZZ */
    lw(a5, 16, s1);             /* SSSS */
    sw(a1, -40, s0);             /* OOOO */
    sw(a2, -36, s0);             /* OOOO */
    sw(a3, -32, s0);             /* OOOO */
    sw(a4, -28, s0);             /* OOOO */
    sw(a5, -24, s0);             /* OOOO */
    lw(a5, -60, s0);             /* SSSS */
    sw(a5, -48, s0);             /* OOOO */
    lw(a5, -64, s0);             /* SSSS */
    sw(a5, -44, s0);             /* OOOO */
    ld(a5, s0, 56);             /* GGGG */
    lw(a4, -48, s0);             /* SSSS */
    mv(a7,a4);        /* ZZZZ */
    lw(a4, -44, s0);             /* SSSS */
    mv(a6,a4);        /* ZZZZ */
    lw(a4, -40, s0);             /* SSSS */
    mv(a0,a4);        /* ZZZZ */
    lw(a4, -36, s0);             /* SSSS */
    mv(a1,a4);        /* ZZZZ */
    lw(a4, -32, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    lw(a4, -28, s0);             /* SSSS */
    mv(a3,a4);        /* ZZZZ */
    lw(a4, -24, s0);             /* SSSS */
    sw(a7, 0, a5);             /* OOOO */
    sw(a6, 4, a5);             /* OOOO */
    sw(a0, 8, a5);             /* OOOO */
    sw(a1, 12, a5);             /* OOOO */
    sw(a2, 16, a5);             /* OOOO */
    sw(a3, 20, a5);             /* OOOO */
    sw(a4, 24, a5);             /* OOOO */
    ld(a0, s0, 56);             /* GGGG */
    ld(s0, sp, 56);             /* GGGG */
    ld(s1, sp, 48);             /* GGGG */
    addi(sp, sp, 64);           /* LLLL */
    jr(ra);                       /* UUUU */
    $findAndInsert.set();
    addi(sp, sp, -112);           /* LLLL */
    sd(ra, 104, sp);             /* OOOO */
    sd(s0, 96, sp);             /* OOOO */
    addi(s0, sp, 112);           /* LLLL */
    sd(a0, -72, s0);             /* OOOO */
    mv(a5,a1);        /* ZZZZ */
    mv(a4,a2);        /* ZZZZ */
    sw(a5, -76, s0);             /* OOOO */
    mv(a5,a4);        /* ZZZZ */
    sw(a5, -80, s0);             /* OOOO */
    addi(a5, s0, -40);           /* LLLL */
    lw(a4, -76, s0);             /* SSSS */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($find);                      /* XXXX */
    lw(a5, -36, s0);             /* SSSS */
    beq(a5, zero, $L258);             /* YYYY */
    lw(a5, -40, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($leaf);                      /* XXXX */
    lw(a3, -32, s0);             /* SSSS */
    lw(a4, -80, s0);             /* SSSS */
    lw(a5, -76, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    call($leaf_setElementAt);                      /* XXXX */
    ld(a4, s0, 72);             /* GGGG */
    ld(a5, s0, 40);             /* GGGG */
    sd(a5, -112, s0);             /* OOOO */
    ld(a5, s0, 32);             /* GGGG */
    sd(a5, -104, s0);             /* OOOO */
    lw(a5, -24, s0);             /* SSSS */
    sw(a5, -96, s0);             /* OOOO */
    addi(a5, s0, -112);           /* LLLL */
    li(a3,0);        /* ZZZZ */
    li(a2,1);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    mv(a0,a4);        /* ZZZZ */
    call($findAndInsert_result);                      /* XXXX */
    j($L263);                       /* WWWW */
    $L258.set();
    lw(a5, -40, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($isFull);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    bne(a5, zero, $L260);             /* YYYY */
    lw(a5, -40, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($leaf);                      /* XXXX */
    mv(a3,a0);        /* ZZZZ */
    addi(a5, s0, -64);           /* LLLL */
    lw(a4, -76, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a3);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($leaf_searchFirstGreaterThanOrEqual);                      /* XXXX */
    lw(a5, -60, s0);             /* SSSS */
    beq(a5, zero, $L261);             /* YYYY */
    lw(a5, -40, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($leaf);                      /* XXXX */
    lw(a3, -32, s0);             /* SSSS */
    lw(a4, -80, s0);             /* SSSS */
    lw(a5, -76, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    call($leaf_insertElementAt);                      /* XXXX */
    j($L262);                       /* WWWW */
    $L261.set();
    lw(a5, -40, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($leaf);                      /* XXXX */
    mv(a3,a0);        /* ZZZZ */
    lw(a4, -80, s0);             /* SSSS */
    lw(a5, -76, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    mv(a0,a3);        /* ZZZZ */
    call($leaf_push);                      /* XXXX */
    $L262.set();
    ld(a4, s0, 72);             /* GGGG */
    ld(a5, s0, 40);             /* GGGG */
    sd(a5, -112, s0);             /* OOOO */
    ld(a5, s0, 32);             /* GGGG */
    sd(a5, -104, s0);             /* OOOO */
    lw(a5, -24, s0);             /* SSSS */
    sw(a5, -96, s0);             /* OOOO */
    addi(a5, s0, -112);           /* LLLL */
    li(a3,1);        /* ZZZZ */
    li(a2,1);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    mv(a0,a4);        /* ZZZZ */
    call($findAndInsert_result);                      /* XXXX */
    j($L263);                       /* WWWW */
    $L260.set();
    ld(a4, s0, 72);             /* GGGG */
    ld(a5, s0, 40);             /* GGGG */
    sd(a5, -112, s0);             /* OOOO */
    ld(a5, s0, 32);             /* GGGG */
    sd(a5, -104, s0);             /* OOOO */
    lw(a5, -24, s0);             /* SSSS */
    sw(a5, -96, s0);             /* OOOO */
    addi(a5, s0, -112);           /* LLLL */
    li(a3,0);        /* ZZZZ */
    li(a2,0);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    mv(a0,a4);        /* ZZZZ */
    call($findAndInsert_result);                      /* XXXX */
    $L263.set();
    ld(a0, s0, 72);             /* GGGG */
    ld(ra, sp, 104);             /* GGGG */
    ld(s0, sp, 96);             /* GGGG */
    addi(sp, sp, 112);           /* LLLL */
    jr(ra);                       /* UUUU */
    $put.set();
    addi(sp, sp, -192);           /* LLLL */
    sd(ra, 184, sp);             /* OOOO */
    sd(s0, 176, sp);             /* OOOO */
    addi(s0, sp, 192);           /* LLLL */
    mv(a5,a0);        /* ZZZZ */
    mv(a4,a1);        /* ZZZZ */
    sw(a5, -148, s0);             /* OOOO */
    mv(a5,a4);        /* ZZZZ */
    sw(a5, -152, s0);             /* OOOO */
    addi(a5, s0, -56);           /* LLLL */
    lw(a3, -152, s0);             /* SSSS */
    lw(a4, -148, s0);             /* SSSS */
    mv(a2,a3);        /* ZZZZ */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($findAndInsert);                      /* XXXX */
    lw(a5, -56, s0);             /* SSSS */
    bne(a5, zero, $L279);             /* YYYY */
    call($isFullRoot);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    beq(a5, zero, $L267);             /* YYYY */
    call($rootIsLeaf);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    beq(a5, zero, $L268);             /* YYYY */
    call($splitLeafRoot);                      /* XXXX */
    j($L269);                       /* WWWW */
    $L268.set();
    call($splitBranchRoot);                      /* XXXX */
    $L269.set();
    addi(a5, s0, -88);           /* LLLL */
    lw(a3, -152, s0);             /* SSSS */
    lw(a4, -148, s0);             /* SSSS */
    mv(a2,a3);        /* ZZZZ */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($findAndInsert);                      /* XXXX */
    lw(a5, -88, s0);             /* SSSS */
    bne(a5, zero, $L280);             /* YYYY */
    $L267.set();
    sw(zero, -20, s0);             /* OOOO */
    sw(zero, -24, s0);             /* OOOO */
    j($L272);                       /* WWWW */
    $L276.set();
    lw(a5, -20, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a3,a0);        /* ZZZZ */
    addi(a5, s0, -112);           /* LLLL */
    lw(a4, -148, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a3);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($branch_searchFirstGreaterThanOrEqualExceptLast);                      /* XXXX */
    lw(a5, -96, s0);             /* SSSS */
    sw(a5, -28, s0);             /* OOOO */
    lw(a5, -28, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($isLeaf);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    beq(a5, zero, $L273);             /* YYYY */
    lw(a3, -104, s0);             /* SSSS */
    lw(a4, -20, s0);             /* SSSS */
    lw(a5, -28, s0);             /* SSSS */
    mv(a2,a3);        /* ZZZZ */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($splitLeaf);                      /* XXXX */
    addi(a5, s0, -192);           /* LLLL */
    lw(a3, -152, s0);             /* SSSS */
    lw(a4, -148, s0);             /* SSSS */
    mv(a2,a3);        /* ZZZZ */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($findAndInsert);                      /* XXXX */
    lw(a5, -148, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($merge);                      /* XXXX */
    j($L264);                       /* WWWW */
    $L273.set();
    lw(a5, -28, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($isFull);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    beq(a5, zero, $L274);             /* YYYY */
    lw(a3, -104, s0);             /* SSSS */
    lw(a4, -20, s0);             /* SSSS */
    lw(a5, -28, s0);             /* SSSS */
    mv(a2,a3);        /* ZZZZ */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($splitBranch);                      /* XXXX */
    lw(a5, -20, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a3,a0);        /* ZZZZ */
    addi(a5, s0, -136);           /* LLLL */
    lw(a4, -148, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a3);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($branch_searchFirstGreaterThanOrEqualExceptLast);                      /* XXXX */
    lw(a5, -120, s0);             /* SSSS */
    sw(a5, -20, s0);             /* OOOO */
    j($L278);                       /* WWWW */
    $L274.set();
    lw(a5, -28, s0);             /* SSSS */
    sw(a5, -20, s0);             /* OOOO */
    $L278.set();
    lw(a5, -24, s0);             /* SSSS */
    addiw(a5, a5, 1);           /* LLLL */
    sw(a5, -24, s0);             /* OOOO */
    $L272.set();
    lw(a5, -24, s0);             /* SSSS */
    sextw(a4,a5);        /* ZZZZ */
    li(a5,9);        /* ZZZZ */
    ble(a4, a5, $L276);             /* YYYY */
    j($L264);                       /* WWWW */
    $L279.set();
    nop();        /* ZZZZ */
    j($L264);                       /* WWWW */
    $L280.set();
    nop();        /* ZZZZ */
    $L264.set();
    ld(ra, sp, 184);             /* GGGG */
    ld(s0, sp, 176);             /* GGGG */
    addi(sp, sp, 192);           /* LLLL */
    jr(ra);                       /* UUUU */
    $findAndDelete_result.set();
    addi(sp, sp, -64);           /* LLLL */
    sd(s0, 56, sp);             /* OOOO */
    sd(s1, 48, sp);             /* OOOO */
    addi(s0, sp, 64);           /* LLLL */
    sd(a0, -56, s0);             /* OOOO */
    mv(s1,a1);        /* ZZZZ */
    mv(a5,a2);        /* ZZZZ */
    mv(a4,a3);        /* ZZZZ */
    sw(a5, -60, s0);             /* OOOO */
    mv(a5,a4);        /* ZZZZ */
    sw(a5, -64, s0);             /* OOOO */
    lw(a5, 0, s1);             /* SSSS */
    mv(a1,a5);        /* ZZZZ */
    lw(a5, 4, s1);             /* SSSS */
    mv(a2,a5);        /* ZZZZ */
    lw(a5, 8, s1);             /* SSSS */
    mv(a3,a5);        /* ZZZZ */
    lw(a5, 12, s1);             /* SSSS */
    mv(a4,a5);        /* ZZZZ */
    lw(a5, 16, s1);             /* SSSS */
    sw(a1, -40, s0);             /* OOOO */
    sw(a2, -36, s0);             /* OOOO */
    sw(a3, -32, s0);             /* OOOO */
    sw(a4, -28, s0);             /* OOOO */
    sw(a5, -24, s0);             /* OOOO */
    lw(a5, -60, s0);             /* SSSS */
    sw(a5, -48, s0);             /* OOOO */
    lw(a5, -64, s0);             /* SSSS */
    sw(a5, -44, s0);             /* OOOO */
    ld(a5, s0, 56);             /* GGGG */
    lw(a4, -48, s0);             /* SSSS */
    mv(a7,a4);        /* ZZZZ */
    lw(a4, -44, s0);             /* SSSS */
    mv(a6,a4);        /* ZZZZ */
    lw(a4, -40, s0);             /* SSSS */
    mv(a0,a4);        /* ZZZZ */
    lw(a4, -36, s0);             /* SSSS */
    mv(a1,a4);        /* ZZZZ */
    lw(a4, -32, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    lw(a4, -28, s0);             /* SSSS */
    mv(a3,a4);        /* ZZZZ */
    lw(a4, -24, s0);             /* SSSS */
    sw(a7, 0, a5);             /* OOOO */
    sw(a6, 4, a5);             /* OOOO */
    sw(a0, 8, a5);             /* OOOO */
    sw(a1, 12, a5);             /* OOOO */
    sw(a2, 16, a5);             /* OOOO */
    sw(a3, 20, a5);             /* OOOO */
    sw(a4, 24, a5);             /* OOOO */
    ld(a0, s0, 56);             /* GGGG */
    ld(s0, sp, 56);             /* GGGG */
    ld(s1, sp, 48);             /* GGGG */
    addi(sp, sp, 64);           /* LLLL */
    jr(ra);                       /* UUUU */
    $findAndDelete.set();
    addi(sp, sp, -128);           /* LLLL */
    sd(ra, 120, sp);             /* OOOO */
    sd(s0, 112, sp);             /* OOOO */
    addi(s0, sp, 128);           /* LLLL */
    sd(a0, -88, s0);             /* OOOO */
    mv(a5,a1);        /* ZZZZ */
    sw(a5, -92, s0);             /* OOOO */
    addi(a5, s0, -48);           /* LLLL */
    lw(a4, -92, s0);             /* SSSS */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($find);                      /* XXXX */
    lw(a5, -44, s0);             /* SSSS */
    bne(a5, zero, $L284);             /* YYYY */
    ld(a4, s0, 88);             /* GGGG */
    ld(a5, s0, 48);             /* GGGG */
    sd(a5, -128, s0);             /* OOOO */
    ld(a5, s0, 40);             /* GGGG */
    sd(a5, -120, s0);             /* OOOO */
    lw(a5, -32, s0);             /* SSSS */
    sw(a5, -112, s0);             /* OOOO */
    addi(a5, s0, -128);           /* LLLL */
    li(a3,0);        /* ZZZZ */
    li(a2,0);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    mv(a0,a4);        /* ZZZZ */
    call($findAndDelete_result);                      /* XXXX */
    j($L286);                       /* WWWW */
    $L284.set();
    lw(a5, -48, s0);             /* SSSS */
    sw(a5, -20, s0);             /* OOOO */
    lw(a5, -40, s0);             /* SSSS */
    sw(a5, -24, s0);             /* OOOO */
    lw(a5, -20, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($leaf);                      /* XXXX */
    mv(a3,a0);        /* ZZZZ */
    addi(a5, s0, -72);           /* LLLL */
    lw(a4, -24, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a3);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($leaf_elementAt);                      /* XXXX */
    lw(a5, -20, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($leaf);                      /* XXXX */
    mv(a3,a0);        /* ZZZZ */
    addi(a5, s0, -128);           /* LLLL */
    lw(a4, -24, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a3);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($leaf_removeElementAt);                      /* XXXX */
    lbu(a5, -56, s0);             /* SSSS */
    sextw(a3,a5);        /* ZZZZ */
    ld(a4, s0, 88);             /* GGGG */
    ld(a5, s0, 48);             /* GGGG */
    sd(a5, -128, s0);             /* OOOO */
    ld(a5, s0, 40);             /* GGGG */
    sd(a5, -120, s0);             /* OOOO */
    lw(a5, -32, s0);             /* SSSS */
    sw(a5, -112, s0);             /* OOOO */
    addi(a5, s0, -128);           /* LLLL */
    li(a2,1);        /* ZZZZ */
    mv(a1,a5);        /* ZZZZ */
    mv(a0,a4);        /* ZZZZ */
    call($findAndDelete_result);                      /* XXXX */
    $L286.set();
    ld(a0, s0, 88);             /* GGGG */
    ld(ra, sp, 120);             /* GGGG */
    ld(s0, sp, 112);             /* GGGG */
    addi(sp, sp, 128);           /* LLLL */
    jr(ra);                       /* UUUU */
    $delete.set();
    addi(sp, sp, -128);           /* LLLL */
    sd(ra, 120, sp);             /* OOOO */
    sd(s0, 112, sp);             /* OOOO */
    addi(s0, sp, 128);           /* LLLL */
    sd(a0, -120, s0);             /* OOOO */
    mv(a5,a1);        /* ZZZZ */
    sw(a5, -124, s0);             /* OOOO */
    call($mergeRoot);                      /* XXXX */
    call($rootIsLeaf);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    beq(a5, zero, $L288);             /* YYYY */
    ld(a5, s0, 120);             /* GGGG */
    lw(a4, -124, s0);             /* SSSS */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($findAndDelete);                      /* XXXX */
    j($L287);                       /* WWWW */
    $L288.set();
    sw(zero, -20, s0);             /* OOOO */
    sw(zero, -24, s0);             /* OOOO */
    j($L290);                       /* WWWW */
    $L292.set();
    lw(a5, -20, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a3,a0);        /* ZZZZ */
    addi(a5, s0, -80);           /* LLLL */
    lw(a4, -124, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a3);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($branch_searchFirstGreaterThanOrEqualExceptLast);                      /* XXXX */
    lw(a4, -72, s0);             /* SSSS */
    lw(a5, -20, s0);             /* SSSS */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($balance);                      /* XXXX */
    lw(a5, -64, s0);             /* SSSS */
    sw(a5, -28, s0);             /* OOOO */
    lw(a5, -28, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($isLeaf);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    beq(a5, zero, $L291);             /* YYYY */
    addi(a5, s0, -112);           /* LLLL */
    lw(a4, -124, s0);             /* SSSS */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($findAndDelete);                      /* XXXX */
    lw(a5, -124, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($merge);                      /* XXXX */
    ld(a5, s0, 120);             /* GGGG */
    lw(a4, -112, s0);             /* SSSS */
    mv(a7,a4);        /* ZZZZ */
    lw(a4, -108, s0);             /* SSSS */
    mv(a6,a4);        /* ZZZZ */
    lw(a4, -104, s0);             /* SSSS */
    mv(a0,a4);        /* ZZZZ */
    lw(a4, -100, s0);             /* SSSS */
    mv(a1,a4);        /* ZZZZ */
    lw(a4, -96, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    lw(a4, -92, s0);             /* SSSS */
    mv(a3,a4);        /* ZZZZ */
    lw(a4, -88, s0);             /* SSSS */
    sw(a7, 0, a5);             /* OOOO */
    sw(a6, 4, a5);             /* OOOO */
    sw(a0, 8, a5);             /* OOOO */
    sw(a1, 12, a5);             /* OOOO */
    sw(a2, 16, a5);             /* OOOO */
    sw(a3, 20, a5);             /* OOOO */
    sw(a4, 24, a5);             /* OOOO */
    j($L287);                       /* WWWW */
    $L291.set();
    lw(a5, -28, s0);             /* SSSS */
    sw(a5, -20, s0);             /* OOOO */
    lw(a5, -24, s0);             /* SSSS */
    addiw(a5, a5, 1);           /* LLLL */
    sw(a5, -24, s0);             /* OOOO */
    $L290.set();
    lw(a5, -24, s0);             /* SSSS */
    sextw(a4,a5);        /* ZZZZ */
    li(a5,9);        /* ZZZZ */
    ble(a4, a5, $L292);             /* YYYY */
    sw(zero, -56, s0);             /* OOOO */
    sw(zero, -52, s0);             /* OOOO */
    sw(zero, -48, s0);             /* OOOO */
    sw(zero, -44, s0);             /* OOOO */
    sw(zero, -40, s0);             /* OOOO */
    sw(zero, -36, s0);             /* OOOO */
    sw(zero, -32, s0);             /* OOOO */
    ld(a5, s0, 120);             /* GGGG */
    lw(a4, -56, s0);             /* SSSS */
    mv(a7,a4);        /* ZZZZ */
    lw(a4, -52, s0);             /* SSSS */
    mv(a6,a4);        /* ZZZZ */
    lw(a4, -48, s0);             /* SSSS */
    mv(a0,a4);        /* ZZZZ */
    lw(a4, -44, s0);             /* SSSS */
    mv(a1,a4);        /* ZZZZ */
    lw(a4, -40, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    lw(a4, -36, s0);             /* SSSS */
    mv(a3,a4);        /* ZZZZ */
    lw(a4, -32, s0);             /* SSSS */
    sw(a7, 0, a5);             /* OOOO */
    sw(a6, 4, a5);             /* OOOO */
    sw(a0, 8, a5);             /* OOOO */
    sw(a1, 12, a5);             /* OOOO */
    sw(a2, 16, a5);             /* OOOO */
    sw(a3, 20, a5);             /* OOOO */
    sw(a4, 24, a5);             /* OOOO */
    $L287.set();
    ld(a0, s0, 120);             /* GGGG */
    ld(ra, sp, 120);             /* GGGG */
    ld(s0, sp, 112);             /* GGGG */
    addi(sp, sp, 128);           /* LLLL */
    jr(ra);                       /* UUUU */
    $merge.set();
    addi(sp, sp, -64);           /* LLLL */
    sd(ra, 56, sp);             /* OOOO */
    sd(s0, 48, sp);             /* OOOO */
    addi(s0, sp, 64);           /* LLLL */
    mv(a5,a0);        /* ZZZZ */
    sw(a5, -52, s0);             /* OOOO */
    call($mergeRoot);                      /* XXXX */
    sw(zero, -20, s0);             /* OOOO */
    sw(zero, -24, s0);             /* OOOO */
    j($L295);                       /* WWWW */
    $L302.set();
    lw(a5, -20, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($isLeaf);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    bne(a5, zero, $L303);             /* YYYY */
    sw(zero, -28, s0);             /* OOOO */
    j($L298);                       /* WWWW */
    $L300.set();
    lw(a4, -28, s0);             /* SSSS */
    lw(a5, -20, s0);             /* SSSS */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($mergeLeftSibling);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    beq(a5, zero, $L299);             /* YYYY */
    lw(a5, -28, s0);             /* SSSS */
    addiw(a5, a5, -1);           /* LLLL */
    sw(a5, -28, s0);             /* OOOO */
    $L299.set();
    lw(a4, -28, s0);             /* SSSS */
    lw(a5, -20, s0);             /* SSSS */
    mv(a1,a4);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($mergeRightSibling);                      /* XXXX */
    lw(a5, -28, s0);             /* SSSS */
    addiw(a5, a5, 1);           /* LLLL */
    sw(a5, -28, s0);             /* OOOO */
    $L298.set();
    lw(a5, -20, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branchSize);                      /* XXXX */
    mv(a5,a0);        /* ZZZZ */
    mv(a4,a5);        /* ZZZZ */
    lw(a5, -28, s0);             /* SSSS */
    sextw(a5,a5);        /* ZZZZ */
    blt(a5, a4, $L300);             /* YYYY */
    lw(a5, -20, s0);             /* SSSS */
    mv(a0,a5);        /* ZZZZ */
    call($branch);                      /* XXXX */
    mv(a3,a0);        /* ZZZZ */
    addi(a5, s0, -48);           /* LLLL */
    lw(a4, -52, s0);             /* SSSS */
    mv(a2,a4);        /* ZZZZ */
    mv(a1,a3);        /* ZZZZ */
    mv(a0,a5);        /* ZZZZ */
    call($branch_searchFirstGreaterThanOrEqualExceptLast);                      /* XXXX */
    lw(a5, -32, s0);             /* SSSS */
    sw(a5, -20, s0);             /* OOOO */
    lw(a5, -24, s0);             /* SSSS */
    addiw(a5, a5, 1);           /* LLLL */
    sw(a5, -24, s0);             /* OOOO */
    $L295.set();
    lw(a5, -24, s0);             /* SSSS */
    sextw(a4,a5);        /* ZZZZ */
    li(a5,9);        /* ZZZZ */
    ble(a4, a5, $L302);             /* YYYY */
    j($L294);                       /* WWWW */
    $L303.set();
    nop();        /* ZZZZ */
    $L294.set();
    ld(ra, sp, 56);             /* GGGG */
    ld(s0, sp, 48);             /* GGGG */
    addi(sp, sp, 64);           /* LLLL */
    jr(ra);                       /* UUUU */
   }
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
