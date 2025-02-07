//-----------------------------------------------------------------------------
// Database on a chip
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2025
//------------------------------------------------------------------------------
`timescale 10ps/1ps
module find(reset, stop, clock, pfd, Key, Data, data, found);               // Database on a chip
  input                 reset;                                                  // Restart the program run sequence when this goes high
  input                 clock;                                                  // Program counter clock
  input            [2:0]pfd;                                                    // Put, find delete
  input [8 :0]Key;                                                    // Input key
  input [8:0]Data;                                                   // Input data
  output                 stop;                                                  // Program has stopped when this goes high
  output[8:0]data;                                                   // Output data
  output                found;                                                  // Whether the key was found on put, find delete

  `include "M.vh"                                                               // Memory holding a pre built tree from test_dump()
  `include "T.vh"                                                               // Transaction memory which is initialized to some values to reduce the complexity of Memory at by treating constants as variables

  integer  step;                                                                // Program counter
  integer steps;                                                                // Number of steps executed
  integer traceFile;                                                            // File to write trace to
  reg   stopped;                                                                // Set when we stop
  assign stop  = stopped > 0 ? 1 : 0;                                           // Stopped execution
  assign found = T_44[27];                                                 // Found the key
  assign data  = T_44[36+:8];                                     // Data associated with key found

reg [11:0] branch_0_StuckSA_Memory_Based_45_base_offset;
reg [55:0] branch_0_StuckSA_Copy_46;  /*   MemoryLayoutPA.java:0943:declareVerilog   BtreePA.java:2296:stuckMemory   BtreePA.java:2280:stuckMemories   BtreePA.java:2487:editVariables   BtreePA.java:2482:editVariables   BtreePA.java:2460:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */
reg [44:0] branch_0_StuckSA_Transaction_47;  /*   MemoryLayoutPA.java:0943:declareVerilog   BtreePA.java:2297:stuckMemory   BtreePA.java:2280:stuckMemories   BtreePA.java:2487:editVariables   BtreePA.java:2482:editVariables   BtreePA.java:2460:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */
reg[11: 0] index_branch_0_StuckSA_Memory_Based_45_base_offset;
reg[11: 0] copyLength_branch_0_StuckSA_Memory_Based_45_base_offset;
reg [11:0] branch_1_StuckSA_Memory_Based_48_base_offset;
reg [55:0] branch_1_StuckSA_Copy_49;  /*   MemoryLayoutPA.java:0943:declareVerilog   BtreePA.java:2296:stuckMemory   BtreePA.java:2280:stuckMemories   BtreePA.java:2487:editVariables   BtreePA.java:2482:editVariables   BtreePA.java:2460:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */
reg [44:0] branch_1_StuckSA_Transaction_50;  /*   MemoryLayoutPA.java:0943:declareVerilog   BtreePA.java:2297:stuckMemory   BtreePA.java:2280:stuckMemories   BtreePA.java:2487:editVariables   BtreePA.java:2482:editVariables   BtreePA.java:2460:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */
reg[11: 0] index_branch_1_StuckSA_Memory_Based_48_base_offset;
reg[11: 0] copyLength_branch_1_StuckSA_Memory_Based_48_base_offset;
reg [11:0] branch_2_StuckSA_Memory_Based_51_base_offset;
reg [55:0] branch_2_StuckSA_Copy_52;  /*   MemoryLayoutPA.java:0943:declareVerilog   BtreePA.java:2296:stuckMemory   BtreePA.java:2280:stuckMemories   BtreePA.java:2487:editVariables   BtreePA.java:2482:editVariables   BtreePA.java:2460:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */
reg [44:0] branch_2_StuckSA_Transaction_53;  /*   MemoryLayoutPA.java:0943:declareVerilog   BtreePA.java:2297:stuckMemory   BtreePA.java:2280:stuckMemories   BtreePA.java:2487:editVariables   BtreePA.java:2482:editVariables   BtreePA.java:2460:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */
reg[11: 0] index_branch_2_StuckSA_Memory_Based_51_base_offset;
reg[11: 0] copyLength_branch_2_StuckSA_Memory_Based_51_base_offset;
reg [11:0] branch_3_StuckSA_Memory_Based_54_base_offset;
reg [55:0] branch_3_StuckSA_Copy_55;  /*   MemoryLayoutPA.java:0943:declareVerilog   BtreePA.java:2296:stuckMemory   BtreePA.java:2280:stuckMemories   BtreePA.java:2487:editVariables   BtreePA.java:2482:editVariables   BtreePA.java:2460:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */
reg [44:0] branch_3_StuckSA_Transaction_56;  /*   MemoryLayoutPA.java:0943:declareVerilog   BtreePA.java:2297:stuckMemory   BtreePA.java:2280:stuckMemories   BtreePA.java:2487:editVariables   BtreePA.java:2482:editVariables   BtreePA.java:2460:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */
reg[11: 0] index_branch_3_StuckSA_Memory_Based_54_base_offset;
reg[11: 0] copyLength_branch_3_StuckSA_Memory_Based_54_base_offset;
reg [11:0] leaf_0_StuckSA_Memory_Based_57_base_offset;
reg [35:0] leaf_0_StuckSA_Copy_58;  /*   MemoryLayoutPA.java:0943:declareVerilog   BtreePA.java:2296:stuckMemory   BtreePA.java:2281:stuckMemories   BtreePA.java:2487:editVariables   BtreePA.java:2482:editVariables   BtreePA.java:2460:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */
reg [47:0] leaf_0_StuckSA_Transaction_59;  /*   MemoryLayoutPA.java:0943:declareVerilog   BtreePA.java:2297:stuckMemory   BtreePA.java:2281:stuckMemories   BtreePA.java:2487:editVariables   BtreePA.java:2482:editVariables   BtreePA.java:2460:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */
reg[11: 0] index_leaf_0_StuckSA_Memory_Based_57_base_offset;
reg[11: 0] copyLength_leaf_0_StuckSA_Memory_Based_57_base_offset;
reg [11:0] leaf_1_StuckSA_Memory_Based_60_base_offset;
reg [35:0] leaf_1_StuckSA_Copy_61;  /*   MemoryLayoutPA.java:0943:declareVerilog   BtreePA.java:2296:stuckMemory   BtreePA.java:2281:stuckMemories   BtreePA.java:2487:editVariables   BtreePA.java:2482:editVariables   BtreePA.java:2460:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */
reg [47:0] leaf_1_StuckSA_Transaction_62;  /*   MemoryLayoutPA.java:0943:declareVerilog   BtreePA.java:2297:stuckMemory   BtreePA.java:2281:stuckMemories   BtreePA.java:2487:editVariables   BtreePA.java:2482:editVariables   BtreePA.java:2460:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */
reg[11: 0] index_leaf_1_StuckSA_Memory_Based_60_base_offset;
reg[11: 0] copyLength_leaf_1_StuckSA_Memory_Based_60_base_offset;
reg [11:0] leaf_2_StuckSA_Memory_Based_63_base_offset;
reg [35:0] leaf_2_StuckSA_Copy_64;  /*   MemoryLayoutPA.java:0943:declareVerilog   BtreePA.java:2296:stuckMemory   BtreePA.java:2281:stuckMemories   BtreePA.java:2487:editVariables   BtreePA.java:2482:editVariables   BtreePA.java:2460:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */
reg [47:0] leaf_2_StuckSA_Transaction_65;  /*   MemoryLayoutPA.java:0943:declareVerilog   BtreePA.java:2297:stuckMemory   BtreePA.java:2281:stuckMemories   BtreePA.java:2487:editVariables   BtreePA.java:2482:editVariables   BtreePA.java:2460:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */
reg[11: 0] index_leaf_2_StuckSA_Memory_Based_63_base_offset;
reg[11: 0] copyLength_leaf_2_StuckSA_Memory_Based_63_base_offset;
reg [11:0] leaf_3_StuckSA_Memory_Based_66_base_offset;
reg [35:0] leaf_3_StuckSA_Copy_67;  /*   MemoryLayoutPA.java:0943:declareVerilog   BtreePA.java:2296:stuckMemory   BtreePA.java:2281:stuckMemories   BtreePA.java:2487:editVariables   BtreePA.java:2482:editVariables   BtreePA.java:2460:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */
reg [47:0] leaf_3_StuckSA_Transaction_68;  /*   MemoryLayoutPA.java:0943:declareVerilog   BtreePA.java:2297:stuckMemory   BtreePA.java:2281:stuckMemories   BtreePA.java:2487:editVariables   BtreePA.java:2482:editVariables   BtreePA.java:2460:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */
reg[11: 0] index_leaf_3_StuckSA_Memory_Based_66_base_offset;
reg[11: 0] copyLength_leaf_3_StuckSA_Memory_Based_66_base_offset;


  always @ (posedge reset, posedge clock) begin                                 // Execute next step in program

    if (reset) begin                                                            // Reset
      step      = 0;
      steps    <= 0;
      stopped  <= 0;
      initialize_memory_M_43();                                                   // Initialize btree memory
      initialize_memory_T_44();                                                   // Initialize btree transaction
      traceFile = $fopen("trace.txt", "w");                                    // Open trace file
      if (!traceFile) $fatal(1, "Cannot open trace file trace.txt");
      branch_0_StuckSA_Memory_Based_45_base_offset <= 0;branch_0_StuckSA_Copy_46 <= 0;branch_0_StuckSA_Transaction_47 <= 0; /*   BtreePA.java:2304:stuckMemoryInitialization   BtreePA.java:2289:stuckMemoryInitialization   BtreePA.java:2488:editVariables   BtreePA.java:2482:editVariables   BtreePA.java:2460:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */branch_1_StuckSA_Memory_Based_48_base_offset <= 0;branch_1_StuckSA_Copy_49 <= 0;branch_1_StuckSA_Transaction_50 <= 0; /*   BtreePA.java:2304:stuckMemoryInitialization   BtreePA.java:2289:stuckMemoryInitialization   BtreePA.java:2488:editVariables   BtreePA.java:2482:editVariables   BtreePA.java:2460:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */branch_2_StuckSA_Memory_Based_51_base_offset <= 0;branch_2_StuckSA_Copy_52 <= 0;branch_2_StuckSA_Transaction_53 <= 0; /*   BtreePA.java:2304:stuckMemoryInitialization   BtreePA.java:2289:stuckMemoryInitialization   BtreePA.java:2488:editVariables   BtreePA.java:2482:editVariables   BtreePA.java:2460:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */branch_3_StuckSA_Memory_Based_54_base_offset <= 0;branch_3_StuckSA_Copy_55 <= 0;branch_3_StuckSA_Transaction_56 <= 0; /*   BtreePA.java:2304:stuckMemoryInitialization   BtreePA.java:2289:stuckMemoryInitialization   BtreePA.java:2488:editVariables   BtreePA.java:2482:editVariables   BtreePA.java:2460:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */leaf_0_StuckSA_Memory_Based_57_base_offset <= 0;leaf_0_StuckSA_Copy_58 <= 0;leaf_0_StuckSA_Transaction_59 <= 0; /*   BtreePA.java:2304:stuckMemoryInitialization   BtreePA.java:2290:stuckMemoryInitialization   BtreePA.java:2488:editVariables   BtreePA.java:2482:editVariables   BtreePA.java:2460:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */leaf_1_StuckSA_Memory_Based_60_base_offset <= 0;leaf_1_StuckSA_Copy_61 <= 0;leaf_1_StuckSA_Transaction_62 <= 0; /*   BtreePA.java:2304:stuckMemoryInitialization   BtreePA.java:2290:stuckMemoryInitialization   BtreePA.java:2488:editVariables   BtreePA.java:2482:editVariables   BtreePA.java:2460:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */leaf_2_StuckSA_Memory_Based_63_base_offset <= 0;leaf_2_StuckSA_Copy_64 <= 0;leaf_2_StuckSA_Transaction_65 <= 0; /*   BtreePA.java:2304:stuckMemoryInitialization   BtreePA.java:2290:stuckMemoryInitialization   BtreePA.java:2488:editVariables   BtreePA.java:2482:editVariables   BtreePA.java:2460:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */leaf_3_StuckSA_Memory_Based_66_base_offset <= 0;leaf_3_StuckSA_Copy_67 <= 0;leaf_3_StuckSA_Transaction_68 <= 0; /*   BtreePA.java:2304:stuckMemoryInitialization   BtreePA.java:2290:stuckMemoryInitialization   BtreePA.java:2488:editVariables   BtreePA.java:2482:editVariables   BtreePA.java:2460:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */
    end
    else begin                                                                  // Run
      $display            ("%4d  %4d  %b", steps, step, M_43);                    // Trace execution
      $fdisplay(traceFile, "%4d  %4d  %b", steps, step, M_43);                    // Trace execution in a file
      case(step)                                                                // Case statements to select the code for the current instruction
              0 : begin T_44[ 176/*node_setBranch  */ +: 5] <= 0; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
              1 : begin T_44[ 111/*mergeable   */ +: 1] <= M_43[   5/*isLeaf  */ + T_44[ 176/*node_setBranch  */ +: 5] * 62 +: 1]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
              2 : begin if (T_44[ 111/*mergeable   */ +: 1] == 0) step = 30; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
              3 : begin T_44[  19/*search  */ +: 8] <= T_44[ 129/*Key */ +: 8]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
              4 : begin T_44[ 196/*node_balance*/ +: 5] <= 0; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
              5 : begin leaf_0_StuckSA_Memory_Based_57_base_offset <=   11/*leaf*/ + T_44[ 196/*node_balance*/ +: 5] * 62; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
              6 : begin leaf_0_StuckSA_Transaction_59[   0/*search  */ +: 8] <= T_44[  19/*search  */ +: 8]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
              7 : begin leaf_0_StuckSA_Transaction_59[   8/*limit   */ +: 4] <= 0; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
              8 : begin leaf_0_StuckSA_Transaction_59[  35/*size*/ +: 4] <= M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+    0/*currentSize */ +: 4]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
              9 : begin if (leaf_0_StuckSA_Transaction_59[   8/*limit   */ +: 4] == 0) step = 10; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             10 : begin leaf_0_StuckSA_Transaction_59[  35/*size*/ +: 4] <= leaf_0_StuckSA_Transaction_59[  35/*size*/ +: 4]- 1; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             11 : begin leaf_0_StuckSA_Transaction_59[  14/*found   */ +: 1] <= 0;leaf_0_StuckSA_Transaction_59[  15/*index   */ +: 4] <= 0; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             12 : begin leaf_0_StuckSA_Transaction_59[  43/*equal   */ +: 1] <= leaf_0_StuckSA_Transaction_59[  15/*index   */ +: 4] == leaf_0_StuckSA_Transaction_59[  35/*size*/ +: 4]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             13 : begin if (leaf_0_StuckSA_Transaction_59[  43/*equal   */ +: 1] > 0) step = 27; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             14 : begin leaf_0_StuckSA_Transaction_59[  19/*key */ +: 8] <= M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+    4/*key */ + leaf_0_StuckSA_Transaction_59[  15/*index   */ +: 4] * 8 +: 8]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             15 : begin leaf_0_StuckSA_Transaction_59[  43/*equal   */ +: 1] <= leaf_0_StuckSA_Transaction_59[  19/*key */ +: 8] == leaf_0_StuckSA_Transaction_59[   0/*search  */ +: 8]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             16 : begin if (leaf_0_StuckSA_Transaction_59[  43/*equal   */ +: 1] == 0) step = 19; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             17 : begin leaf_0_StuckSA_Transaction_59[  14/*found   */ +: 1] <= 1; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             18 : begin leaf_0_StuckSA_Transaction_59[  27/*data*/ +: 8] <= M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+   20/*data*/ + leaf_0_StuckSA_Transaction_59[  15/*index   */ +: 4] * 8 +: 8]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             19 : begin step = 27; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             20 : begin leaf_0_StuckSA_Transaction_59[  15/*index   */ +: 4] <= 1; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             21 : begin leaf_0_StuckSA_Transaction_59[  43/*equal   */ +: 1] <= leaf_0_StuckSA_Transaction_59[  15/*index   */ +: 4] == leaf_0_StuckSA_Transaction_59[  35/*size*/ +: 4]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             22 : begin if (leaf_0_StuckSA_Transaction_59[  43/*equal   */ +: 1] > 0) step = 27; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             23 : begin leaf_0_StuckSA_Transaction_59[  19/*key */ +: 8] <= M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+    4/*key */ + leaf_0_StuckSA_Transaction_59[  15/*index   */ +: 4] * 8 +: 8]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             24 : begin leaf_0_StuckSA_Transaction_59[  43/*equal   */ +: 1] <= leaf_0_StuckSA_Transaction_59[  19/*key */ +: 8] == leaf_0_StuckSA_Transaction_59[   0/*search  */ +: 8]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             25 : begin if (leaf_0_StuckSA_Transaction_59[  43/*equal   */ +: 1] == 0) step = 27; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             26 : begin leaf_0_StuckSA_Transaction_59[  14/*found   */ +: 1] <= 1; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             27 : begin leaf_0_StuckSA_Transaction_59[  27/*data*/ +: 8] <= M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+   20/*data*/ + leaf_0_StuckSA_Transaction_59[  15/*index   */ +: 4] * 8 +: 8]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             28 : begin T_44[  27/*found   */ +: 1] <= leaf_0_StuckSA_Transaction_59[  14/*found   */ +: 1];T_44[  84/*index   */ +: 4] <= leaf_0_StuckSA_Transaction_59[  15/*index   */ +: 4];T_44[  84/*index   */ +: 4] <= leaf_0_StuckSA_Transaction_59[  15/*index   */ +: 4];T_44[  84/*index   */ +: 4] <= leaf_0_StuckSA_Transaction_59[  15/*index   */ +: 4];T_44[  84/*index   */ +: 4] <= leaf_0_StuckSA_Transaction_59[  15/*index   */ +: 4];T_44[  36/*data*/ +: 8] <= leaf_0_StuckSA_Transaction_59[  27/*data*/ +: 8];T_44[  36/*data*/ +: 8] <= leaf_0_StuckSA_Transaction_59[  27/*data*/ +: 8];T_44[  36/*data*/ +: 8] <= leaf_0_StuckSA_Transaction_59[  27/*data*/ +: 8];T_44[  36/*data*/ +: 8] <= leaf_0_StuckSA_Transaction_59[  27/*data*/ +: 8];T_44[  36/*data*/ +: 8] <= leaf_0_StuckSA_Transaction_59[  27/*data*/ +: 8];T_44[  36/*data*/ +: 8] <= leaf_0_StuckSA_Transaction_59[  27/*data*/ +: 8];T_44[  36/*data*/ +: 8] <= leaf_0_StuckSA_Transaction_59[  27/*data*/ +: 8];T_44[  36/*data*/ +: 8] <= leaf_0_StuckSA_Transaction_59[  27/*data*/ +: 8]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             29 : begin T_44[ 145/*parent  */ +: 5] <= 0; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             30 : begin step = 126; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             31 : begin
                                  T_44[ 145/*parent  */ +: 5] <= 0; /*   MemoryLayoutPA.java:0552:<init>
  MemoryLayoutPA.java:0551:zero
  BtreePA.java:1963:code
  ProgramPA.java:0201:<init>
  BtreePA.java:1949:<init>
  BtreePA.java:1948:find
  BtreePA.java:3409:test_verilog_find
  BtreePA.java:3780:newTests
  BtreePA.java:3786:main
 */
                T_44[ 167/*mergeDepth  */ +: 5] <= 0; /*   MemoryLayoutPA.java:0552:<init>
  MemoryLayoutPA.java:0551:zero
  BtreePA.java:1965:code
  ProgramPA.java:0201:<init>
  BtreePA.java:1949:<init>
  BtreePA.java:1948:find
  BtreePA.java:3409:test_verilog_find
  BtreePA.java:3780:newTests
  BtreePA.java:3786:main
 */
                end
             32 : begin T_44[ 167/*mergeDepth  */ +: 5] <= T_44[ 167/*mergeDepth  */ +: 5]+ 1; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             33 : begin T_44[ 111/*mergeable   */ +: 1] <= T_44[ 167/*mergeDepth  */ +: 5] > T_44[ 167/*mergeDepth  */ +: 5]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             34 : begin if (T_44[ 111/*mergeable   */ +: 1] > 0) step = 126; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             35 : begin T_44[  19/*search  */ +: 8] <= T_44[ 129/*Key */ +: 8]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             36 : begin T_44[ 196/*node_balance*/ +: 5] <= T_44[ 145/*parent  */ +: 5]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             37 : begin branch_0_StuckSA_Memory_Based_45_base_offset <=   11/*branch  */ + T_44[ 196/*node_balance*/ +: 5] * 62; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             38 : begin branch_0_StuckSA_Transaction_47[   0/*search  */ +: 8] <= T_44[  19/*search  */ +: 8]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             39 : begin branch_0_StuckSA_Transaction_47[   8/*limit   */ +: 4] <= 1; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             40 : begin branch_0_StuckSA_Transaction_47[  32/*size*/ +: 4] <= M_43[branch_0_StuckSA_Memory_Based_45_base_offset+      0/*currentSize */ +: 4]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             41 : begin if (branch_0_StuckSA_Transaction_47[   8/*limit   */ +: 4] == 0) step = 42; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             42 : begin branch_0_StuckSA_Transaction_47[  32/*size*/ +: 4] <= branch_0_StuckSA_Transaction_47[  32/*size*/ +: 4]- 1; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             43 : begin branch_0_StuckSA_Transaction_47[  14/*found   */ +: 1] <= 0;branch_0_StuckSA_Transaction_47[  15/*index   */ +: 4] <= 0; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             44 : begin branch_0_StuckSA_Transaction_47[  40/*equal   */ +: 1] <= branch_0_StuckSA_Transaction_47[  15/*index   */ +: 4] == branch_0_StuckSA_Transaction_47[  32/*size*/ +: 4]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             45 : begin if (branch_0_StuckSA_Transaction_47[  40/*equal   */ +: 1] > 0) step = 81; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             46 : begin branch_0_StuckSA_Transaction_47[  19/*key */ +: 8] <= M_43[branch_0_StuckSA_Memory_Based_45_base_offset+      4/*key */ + branch_0_StuckSA_Transaction_47[  15/*index   */ +: 4] * 8 +: 8]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             47 : begin branch_0_StuckSA_Transaction_47[  40/*equal   */ +: 1] <= branch_0_StuckSA_Transaction_47[  19/*key */ +: 8] >= branch_0_StuckSA_Transaction_47[   0/*search  */ +: 8]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             48 : begin if (branch_0_StuckSA_Transaction_47[  40/*equal   */ +: 1] == 0) step = 52; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             49 : begin branch_0_StuckSA_Transaction_47[  14/*found   */ +: 1] <= 1; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             50 : begin branch_0_StuckSA_Transaction_47[  19/*key */ +: 8] <= M_43[branch_0_StuckSA_Memory_Based_45_base_offset+      4/*key */ + branch_0_StuckSA_Transaction_47[  15/*index   */ +: 4] * 8 +: 8]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             51 : begin branch_0_StuckSA_Transaction_47[  27/*data*/ +: 5] <= M_43[branch_0_StuckSA_Memory_Based_45_base_offset+     36/*data*/ + branch_0_StuckSA_Transaction_47[  15/*index   */ +: 4] * 5 +: 5]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             52 : begin step = 81; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             53 : begin branch_0_StuckSA_Transaction_47[  15/*index   */ +: 4] <= 1; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             54 : begin branch_0_StuckSA_Transaction_47[  40/*equal   */ +: 1] <= branch_0_StuckSA_Transaction_47[  15/*index   */ +: 4] == branch_0_StuckSA_Transaction_47[  32/*size*/ +: 4]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             55 : begin if (branch_0_StuckSA_Transaction_47[  40/*equal   */ +: 1] > 0) step = 81; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             56 : begin branch_0_StuckSA_Transaction_47[  19/*key */ +: 8] <= M_43[branch_0_StuckSA_Memory_Based_45_base_offset+      4/*key */ + branch_0_StuckSA_Transaction_47[  15/*index   */ +: 4] * 8 +: 8]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             57 : begin branch_0_StuckSA_Transaction_47[  40/*equal   */ +: 1] <= branch_0_StuckSA_Transaction_47[  19/*key */ +: 8] >= branch_0_StuckSA_Transaction_47[   0/*search  */ +: 8]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             58 : begin if (branch_0_StuckSA_Transaction_47[  40/*equal   */ +: 1] == 0) step = 62; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             59 : begin branch_0_StuckSA_Transaction_47[  14/*found   */ +: 1] <= 1; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             60 : begin branch_0_StuckSA_Transaction_47[  19/*key */ +: 8] <= M_43[branch_0_StuckSA_Memory_Based_45_base_offset+      4/*key */ + branch_0_StuckSA_Transaction_47[  15/*index   */ +: 4] * 8 +: 8]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             61 : begin branch_0_StuckSA_Transaction_47[  27/*data*/ +: 5] <= M_43[branch_0_StuckSA_Memory_Based_45_base_offset+     36/*data*/ + branch_0_StuckSA_Transaction_47[  15/*index   */ +: 4] * 5 +: 5]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             62 : begin step = 81; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             63 : begin branch_0_StuckSA_Transaction_47[  15/*index   */ +: 4] <= 2; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             64 : begin branch_0_StuckSA_Transaction_47[  40/*equal   */ +: 1] <= branch_0_StuckSA_Transaction_47[  15/*index   */ +: 4] == branch_0_StuckSA_Transaction_47[  32/*size*/ +: 4]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             65 : begin if (branch_0_StuckSA_Transaction_47[  40/*equal   */ +: 1] > 0) step = 81; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             66 : begin branch_0_StuckSA_Transaction_47[  19/*key */ +: 8] <= M_43[branch_0_StuckSA_Memory_Based_45_base_offset+      4/*key */ + branch_0_StuckSA_Transaction_47[  15/*index   */ +: 4] * 8 +: 8]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             67 : begin branch_0_StuckSA_Transaction_47[  40/*equal   */ +: 1] <= branch_0_StuckSA_Transaction_47[  19/*key */ +: 8] >= branch_0_StuckSA_Transaction_47[   0/*search  */ +: 8]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             68 : begin if (branch_0_StuckSA_Transaction_47[  40/*equal   */ +: 1] == 0) step = 72; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             69 : begin branch_0_StuckSA_Transaction_47[  14/*found   */ +: 1] <= 1; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             70 : begin branch_0_StuckSA_Transaction_47[  19/*key */ +: 8] <= M_43[branch_0_StuckSA_Memory_Based_45_base_offset+      4/*key */ + branch_0_StuckSA_Transaction_47[  15/*index   */ +: 4] * 8 +: 8]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             71 : begin branch_0_StuckSA_Transaction_47[  27/*data*/ +: 5] <= M_43[branch_0_StuckSA_Memory_Based_45_base_offset+     36/*data*/ + branch_0_StuckSA_Transaction_47[  15/*index   */ +: 4] * 5 +: 5]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             72 : begin step = 81; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             73 : begin branch_0_StuckSA_Transaction_47[  15/*index   */ +: 4] <= 3; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             74 : begin branch_0_StuckSA_Transaction_47[  40/*equal   */ +: 1] <= branch_0_StuckSA_Transaction_47[  15/*index   */ +: 4] == branch_0_StuckSA_Transaction_47[  32/*size*/ +: 4]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             75 : begin if (branch_0_StuckSA_Transaction_47[  40/*equal   */ +: 1] > 0) step = 81; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             76 : begin branch_0_StuckSA_Transaction_47[  19/*key */ +: 8] <= M_43[branch_0_StuckSA_Memory_Based_45_base_offset+      4/*key */ + branch_0_StuckSA_Transaction_47[  15/*index   */ +: 4] * 8 +: 8]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             77 : begin branch_0_StuckSA_Transaction_47[  40/*equal   */ +: 1] <= branch_0_StuckSA_Transaction_47[  19/*key */ +: 8] >= branch_0_StuckSA_Transaction_47[   0/*search  */ +: 8]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             78 : begin if (branch_0_StuckSA_Transaction_47[  40/*equal   */ +: 1] == 0) step = 81; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             79 : begin branch_0_StuckSA_Transaction_47[  14/*found   */ +: 1] <= 1; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             80 : begin branch_0_StuckSA_Transaction_47[  19/*key */ +: 8] <= M_43[branch_0_StuckSA_Memory_Based_45_base_offset+      4/*key */ + branch_0_StuckSA_Transaction_47[  15/*index   */ +: 4] * 8 +: 8]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             81 : begin branch_0_StuckSA_Transaction_47[  27/*data*/ +: 5] <= M_43[branch_0_StuckSA_Memory_Based_45_base_offset+     36/*data*/ + branch_0_StuckSA_Transaction_47[  15/*index   */ +: 4] * 5 +: 5]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             82 : begin T_44[  27/*found   */ +: 1] <= branch_0_StuckSA_Transaction_47[  14/*found   */ +: 1];T_44[  10/*first   */ +: 4] <= branch_0_StuckSA_Transaction_47[  15/*index   */ +: 4];T_44[  10/*first   */ +: 4] <= branch_0_StuckSA_Transaction_47[  15/*index   */ +: 4];T_44[  10/*first   */ +: 4] <= branch_0_StuckSA_Transaction_47[  15/*index   */ +: 4];T_44[  10/*first   */ +: 4] <= branch_0_StuckSA_Transaction_47[  15/*index   */ +: 4]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             83 : begin if (T_44[  27/*found   */ +: 1] == 0) step = 85; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             84 : begin T_44[  14/*next*/ +: 5] <= branch_0_StuckSA_Transaction_47[  27/*data*/ +: 5]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             85 : begin step = 94; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             86 : begin branch_0_StuckSA_Transaction_47[  32/*size*/ +: 4] <= M_43[branch_0_StuckSA_Memory_Based_45_base_offset+      0/*currentSize */ +: 4]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             87 : begin branch_0_StuckSA_Transaction_47[  36/*full*/ +: 4] <= 0; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             88 : begin branch_0_StuckSA_Transaction_47[  13/*isEmpty */ +: 1] <= branch_0_StuckSA_Transaction_47[  32/*size*/ +: 4] == branch_0_StuckSA_Transaction_47[  36/*full*/ +: 4]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             89 : begin branch_0_StuckSA_Transaction_47[  14/*found   */ +: 1] <= 1; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             90 : begin branch_0_StuckSA_Transaction_47[  15/*index   */ +: 4] <= M_43[branch_0_StuckSA_Memory_Based_45_base_offset+      0/*currentSize */ +: 4]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             91 : begin branch_0_StuckSA_Transaction_47[  15/*index   */ +: 4] <= branch_0_StuckSA_Transaction_47[  15/*index   */ +: 4]- 1; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             92 : begin branch_0_StuckSA_Transaction_47[  19/*key */ +: 8] <= M_43[branch_0_StuckSA_Memory_Based_45_base_offset+      4/*key */ + branch_0_StuckSA_Transaction_47[  15/*index   */ +: 4] * 8 +: 8]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             93 : begin branch_0_StuckSA_Transaction_47[  27/*data*/ +: 5] <= M_43[branch_0_StuckSA_Memory_Based_45_base_offset+     36/*data*/ + branch_0_StuckSA_Transaction_47[  15/*index   */ +: 4] * 5 +: 5]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             94 : begin T_44[  14/*next*/ +: 5] <= branch_0_StuckSA_Transaction_47[  27/*data*/ +: 5]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             95 : begin
                                  T_44[ 150/*top */ +: 5] <= T_44[  14/*next*/ +: 5]; /*   MemoryLayoutPA.java:0372:<init>
  MemoryLayoutPA.java:0371:move
  BtreePA.java:0279:tt
  BtreePA.java:1979:code
  ProgramPA.java:0201:<init>
  BtreePA.java:1969:<init>
  BtreePA.java:1968:code
  ProgramPA.java:0201:<init>
  BtreePA.java:1949:<init>
  BtreePA.java:1948:find
  BtreePA.java:3409:test_verilog_find
  BtreePA.java:3780:newTests
  BtreePA.java:3786:main
 */
                T_44[ 176/*node_setBranch  */ +: 5] <= T_44[ 150/*top */ +: 5]; /*   MemoryLayoutPA.java:0372:<init>
  MemoryLayoutPA.java:0371:move
  BtreePA.java:0279:tt
  BtreePA.java:1981:code
  ProgramPA.java:0201:<init>
  BtreePA.java:1969:<init>
  BtreePA.java:1968:code
  ProgramPA.java:0201:<init>
  BtreePA.java:1949:<init>
  BtreePA.java:1948:find
  BtreePA.java:3409:test_verilog_find
  BtreePA.java:3780:newTests
  BtreePA.java:3786:main
 */
                end
             96 : begin T_44[ 111/*mergeable   */ +: 1] <= M_43[   5/*isLeaf  */ + T_44[  14/*next*/ +: 5] * 62 +: 1]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             97 : begin if (T_44[ 111/*mergeable   */ +: 1] == 0) step = 124; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
             98 : begin
                                  T_44[  19/*search  */ +: 8] <= T_44[ 129/*Key */ +: 8]; /*   MemoryLayoutPA.java:0372:<init>
  MemoryLayoutPA.java:0371:move
  BtreePA.java:0279:tt
  BtreePA.java:1988:Then
  ProgramPA.java:0171:<init>
  BtreePA.java:1986:<init>
  BtreePA.java:1985:code
  ProgramPA.java:0201:<init>
  BtreePA.java:1969:<init>
  BtreePA.java:1968:code
  ProgramPA.java:0201:<init>
  BtreePA.java:1949:<init>
  BtreePA.java:1948:find
  BtreePA.java:3409:test_verilog_find
  BtreePA.java:3780:newTests
  BtreePA.java:3786:main
 */
                T_44[ 196/*node_balance*/ +: 5] <= T_44[ 150/*top */ +: 5]; /*   MemoryLayoutPA.java:0372:<init>
  MemoryLayoutPA.java:0371:move
  BtreePA.java:0279:tt
  BtreePA.java:1990:Then
  ProgramPA.java:0171:<init>
  BtreePA.java:1986:<init>
  BtreePA.java:1985:code
  ProgramPA.java:0201:<init>
  BtreePA.java:1969:<init>
  BtreePA.java:1968:code
  ProgramPA.java:0201:<init>
  BtreePA.java:1949:<init>
  BtreePA.java:1948:find
  BtreePA.java:3409:test_verilog_find
  BtreePA.java:3780:newTests
  BtreePA.java:3786:main
 */
                end
             99 : begin leaf_0_StuckSA_Memory_Based_57_base_offset <=   11/*leaf*/ + T_44[ 196/*node_balance*/ +: 5] * 62; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
            100 : begin leaf_0_StuckSA_Transaction_59[   0/*search  */ +: 8] <= T_44[  19/*search  */ +: 8]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
            101 : begin leaf_0_StuckSA_Transaction_59[   8/*limit   */ +: 4] <= 0; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
            102 : begin leaf_0_StuckSA_Transaction_59[  35/*size*/ +: 4] <= M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+    0/*currentSize */ +: 4]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
            103 : begin if (leaf_0_StuckSA_Transaction_59[   8/*limit   */ +: 4] == 0) step = 104; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
            104 : begin leaf_0_StuckSA_Transaction_59[  35/*size*/ +: 4] <= leaf_0_StuckSA_Transaction_59[  35/*size*/ +: 4]- 1; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
            105 : begin leaf_0_StuckSA_Transaction_59[  14/*found   */ +: 1] <= 0;leaf_0_StuckSA_Transaction_59[  15/*index   */ +: 4] <= 0; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
            106 : begin leaf_0_StuckSA_Transaction_59[  43/*equal   */ +: 1] <= leaf_0_StuckSA_Transaction_59[  15/*index   */ +: 4] == leaf_0_StuckSA_Transaction_59[  35/*size*/ +: 4]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
            107 : begin if (leaf_0_StuckSA_Transaction_59[  43/*equal   */ +: 1] > 0) step = 121; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
            108 : begin leaf_0_StuckSA_Transaction_59[  19/*key */ +: 8] <= M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+    4/*key */ + leaf_0_StuckSA_Transaction_59[  15/*index   */ +: 4] * 8 +: 8]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
            109 : begin leaf_0_StuckSA_Transaction_59[  43/*equal   */ +: 1] <= leaf_0_StuckSA_Transaction_59[  19/*key */ +: 8] == leaf_0_StuckSA_Transaction_59[   0/*search  */ +: 8]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
            110 : begin if (leaf_0_StuckSA_Transaction_59[  43/*equal   */ +: 1] == 0) step = 113; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
            111 : begin leaf_0_StuckSA_Transaction_59[  14/*found   */ +: 1] <= 1; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
            112 : begin leaf_0_StuckSA_Transaction_59[  27/*data*/ +: 8] <= M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+   20/*data*/ + leaf_0_StuckSA_Transaction_59[  15/*index   */ +: 4] * 8 +: 8]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
            113 : begin step = 121; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
            114 : begin leaf_0_StuckSA_Transaction_59[  15/*index   */ +: 4] <= 1; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
            115 : begin leaf_0_StuckSA_Transaction_59[  43/*equal   */ +: 1] <= leaf_0_StuckSA_Transaction_59[  15/*index   */ +: 4] == leaf_0_StuckSA_Transaction_59[  35/*size*/ +: 4]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
            116 : begin if (leaf_0_StuckSA_Transaction_59[  43/*equal   */ +: 1] > 0) step = 121; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
            117 : begin leaf_0_StuckSA_Transaction_59[  19/*key */ +: 8] <= M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+    4/*key */ + leaf_0_StuckSA_Transaction_59[  15/*index   */ +: 4] * 8 +: 8]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
            118 : begin leaf_0_StuckSA_Transaction_59[  43/*equal   */ +: 1] <= leaf_0_StuckSA_Transaction_59[  19/*key */ +: 8] == leaf_0_StuckSA_Transaction_59[   0/*search  */ +: 8]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
            119 : begin if (leaf_0_StuckSA_Transaction_59[  43/*equal   */ +: 1] == 0) step = 121; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
            120 : begin leaf_0_StuckSA_Transaction_59[  14/*found   */ +: 1] <= 1; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
            121 : begin leaf_0_StuckSA_Transaction_59[  27/*data*/ +: 8] <= M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+   20/*data*/ + leaf_0_StuckSA_Transaction_59[  15/*index   */ +: 4] * 8 +: 8]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
            122 : begin T_44[  27/*found   */ +: 1] <= leaf_0_StuckSA_Transaction_59[  14/*found   */ +: 1];T_44[  84/*index   */ +: 4] <= leaf_0_StuckSA_Transaction_59[  15/*index   */ +: 4];T_44[  84/*index   */ +: 4] <= leaf_0_StuckSA_Transaction_59[  15/*index   */ +: 4];T_44[  84/*index   */ +: 4] <= leaf_0_StuckSA_Transaction_59[  15/*index   */ +: 4];T_44[  84/*index   */ +: 4] <= leaf_0_StuckSA_Transaction_59[  15/*index   */ +: 4];T_44[  36/*data*/ +: 8] <= leaf_0_StuckSA_Transaction_59[  27/*data*/ +: 8];T_44[  36/*data*/ +: 8] <= leaf_0_StuckSA_Transaction_59[  27/*data*/ +: 8];T_44[  36/*data*/ +: 8] <= leaf_0_StuckSA_Transaction_59[  27/*data*/ +: 8];T_44[  36/*data*/ +: 8] <= leaf_0_StuckSA_Transaction_59[  27/*data*/ +: 8];T_44[  36/*data*/ +: 8] <= leaf_0_StuckSA_Transaction_59[  27/*data*/ +: 8];T_44[  36/*data*/ +: 8] <= leaf_0_StuckSA_Transaction_59[  27/*data*/ +: 8];T_44[  36/*data*/ +: 8] <= leaf_0_StuckSA_Transaction_59[  27/*data*/ +: 8];T_44[  36/*data*/ +: 8] <= leaf_0_StuckSA_Transaction_59[  27/*data*/ +: 8]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
            123 : begin T_44[ 145/*parent  */ +: 5] <= T_44[ 150/*top */ +: 5]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
            124 : begin step = 126; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
            125 : begin T_44[ 145/*parent  */ +: 5] <= T_44[ 150/*top */ +: 5]; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
            126 : begin step = 31; /*   BtreePA.java:2403:<init>   BtreePA.java:3411:<init>   BtreePA.java:3410:test_verilog_find   BtreePA.java:3780:newTests   BtreePA.java:3786:main  */ end
        default : begin stopped <= 1; /* end of execution */ end
      endcase
      step   = step  + 1;
      steps <= steps + 1;
    end // Execute
  end // Always
endmodule
