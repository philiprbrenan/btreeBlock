//-----------------------------------------------------------------------------
// Database on a chip
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2025
//------------------------------------------------------------------------------
`timescale 10ps/1ps
module find(reset, stop, clock, pfd, Key, Data, data, found);               // Database on a chip
  input                 reset;                                                  // Restart the program run sequence when this goes high
  input                 clock;                                                  // Program counter clock
  input            [2:0]pfd;                                                    // Put, find delete
  input [5 :0]Key;                                                    // Input key
  input [4:0]Data;                                                   // Input data
  output                 stop;                                                  // Program has stopped when this goes high
  output[4:0]data;                                                   // Output data
  output                found;                                                  // Whether the key was found on put, find delete

  `include "M.vh"                                                               // Memory holding a pre built tree from test_dump()
  `include "T.vh"                                                               // Transaction memory which is initialized to some values to reduce the complexity of Memory at by treating constants as variables

  integer  step;                                                                // Program counter
  integer steps;                                                                // Number of steps executed
  integer traceFile;                                                            // File to write trace to
  reg   stopped;                                                                // Set when we stop
  assign stop  = stopped > 0 ? 1 : 0;                                           // Stopped execution
  assign found = T_44[22];                                                 // Found the key
  assign data  = T_44[28+:4];                                     // Data associated with key found

reg [10:0] branch_0_StuckSA_Memory_Based_45_base_offset;
reg [38:0] branch_0_StuckSA_Copy_46;  /*   MemoryLayoutPA.java:0971:declareVerilog   BtreePA.java:2296:stuckMemory   BtreePA.java:2280:stuckMemories   BtreePA.java:2497:editVariables   BtreePA.java:2492:editVariables   BtreePA.java:2462:<init>   BtreePA.java:3434:<init>   BtreePA.java:3433:test_verilog_find   BtreePA.java:3807:newTests   BtreePA.java:3814:main  */
reg [47:0] branch_0_StuckSA_Transaction_47;  /*   MemoryLayoutPA.java:0971:declareVerilog   BtreePA.java:2297:stuckMemory   BtreePA.java:2280:stuckMemories   BtreePA.java:2497:editVariables   BtreePA.java:2492:editVariables   BtreePA.java:2462:<init>   BtreePA.java:3434:<init>   BtreePA.java:3433:test_verilog_find   BtreePA.java:3807:newTests   BtreePA.java:3814:main  */
reg[10: 0] index_branch_0_StuckSA_Memory_Based_45_base_offset;
reg[10: 0] copyLength_branch_0_StuckSA_Memory_Based_45_base_offset;
reg [10:0] branch_1_StuckSA_Memory_Based_48_base_offset;
reg [38:0] branch_1_StuckSA_Copy_49;  /*   MemoryLayoutPA.java:0971:declareVerilog   BtreePA.java:2296:stuckMemory   BtreePA.java:2280:stuckMemories   BtreePA.java:2497:editVariables   BtreePA.java:2492:editVariables   BtreePA.java:2462:<init>   BtreePA.java:3434:<init>   BtreePA.java:3433:test_verilog_find   BtreePA.java:3807:newTests   BtreePA.java:3814:main  */
reg [47:0] branch_1_StuckSA_Transaction_50;  /*   MemoryLayoutPA.java:0971:declareVerilog   BtreePA.java:2297:stuckMemory   BtreePA.java:2280:stuckMemories   BtreePA.java:2497:editVariables   BtreePA.java:2492:editVariables   BtreePA.java:2462:<init>   BtreePA.java:3434:<init>   BtreePA.java:3433:test_verilog_find   BtreePA.java:3807:newTests   BtreePA.java:3814:main  */
reg[10: 0] index_branch_1_StuckSA_Memory_Based_48_base_offset;
reg[10: 0] copyLength_branch_1_StuckSA_Memory_Based_48_base_offset;
reg [10:0] branch_2_StuckSA_Memory_Based_51_base_offset;
reg [38:0] branch_2_StuckSA_Copy_52;  /*   MemoryLayoutPA.java:0971:declareVerilog   BtreePA.java:2296:stuckMemory   BtreePA.java:2280:stuckMemories   BtreePA.java:2497:editVariables   BtreePA.java:2492:editVariables   BtreePA.java:2462:<init>   BtreePA.java:3434:<init>   BtreePA.java:3433:test_verilog_find   BtreePA.java:3807:newTests   BtreePA.java:3814:main  */
reg [47:0] branch_2_StuckSA_Transaction_53;  /*   MemoryLayoutPA.java:0971:declareVerilog   BtreePA.java:2297:stuckMemory   BtreePA.java:2280:stuckMemories   BtreePA.java:2497:editVariables   BtreePA.java:2492:editVariables   BtreePA.java:2462:<init>   BtreePA.java:3434:<init>   BtreePA.java:3433:test_verilog_find   BtreePA.java:3807:newTests   BtreePA.java:3814:main  */
reg[10: 0] index_branch_2_StuckSA_Memory_Based_51_base_offset;
reg[10: 0] copyLength_branch_2_StuckSA_Memory_Based_51_base_offset;
reg [10:0] branch_3_StuckSA_Memory_Based_54_base_offset;
reg [38:0] branch_3_StuckSA_Copy_55;  /*   MemoryLayoutPA.java:0971:declareVerilog   BtreePA.java:2296:stuckMemory   BtreePA.java:2280:stuckMemories   BtreePA.java:2497:editVariables   BtreePA.java:2492:editVariables   BtreePA.java:2462:<init>   BtreePA.java:3434:<init>   BtreePA.java:3433:test_verilog_find   BtreePA.java:3807:newTests   BtreePA.java:3814:main  */
reg [47:0] branch_3_StuckSA_Transaction_56;  /*   MemoryLayoutPA.java:0971:declareVerilog   BtreePA.java:2297:stuckMemory   BtreePA.java:2280:stuckMemories   BtreePA.java:2497:editVariables   BtreePA.java:2492:editVariables   BtreePA.java:2462:<init>   BtreePA.java:3434:<init>   BtreePA.java:3433:test_verilog_find   BtreePA.java:3807:newTests   BtreePA.java:3814:main  */
reg[10: 0] index_branch_3_StuckSA_Memory_Based_54_base_offset;
reg[10: 0] copyLength_branch_3_StuckSA_Memory_Based_54_base_offset;
reg [10:0] leaf_0_StuckSA_Memory_Based_57_base_offset;
reg [20:0] leaf_0_StuckSA_Copy_58;  /*   MemoryLayoutPA.java:0971:declareVerilog   BtreePA.java:2296:stuckMemory   BtreePA.java:2281:stuckMemories   BtreePA.java:2497:editVariables   BtreePA.java:2492:editVariables   BtreePA.java:2462:<init>   BtreePA.java:3434:<init>   BtreePA.java:3433:test_verilog_find   BtreePA.java:3807:newTests   BtreePA.java:3814:main  */
reg [47:0] leaf_0_StuckSA_Transaction_59;  /*   MemoryLayoutPA.java:0971:declareVerilog   BtreePA.java:2297:stuckMemory   BtreePA.java:2281:stuckMemories   BtreePA.java:2497:editVariables   BtreePA.java:2492:editVariables   BtreePA.java:2462:<init>   BtreePA.java:3434:<init>   BtreePA.java:3433:test_verilog_find   BtreePA.java:3807:newTests   BtreePA.java:3814:main  */
reg[10: 0] index_leaf_0_StuckSA_Memory_Based_57_base_offset;
reg[10: 0] copyLength_leaf_0_StuckSA_Memory_Based_57_base_offset;
reg [10:0] leaf_1_StuckSA_Memory_Based_60_base_offset;
reg [20:0] leaf_1_StuckSA_Copy_61;  /*   MemoryLayoutPA.java:0971:declareVerilog   BtreePA.java:2296:stuckMemory   BtreePA.java:2281:stuckMemories   BtreePA.java:2497:editVariables   BtreePA.java:2492:editVariables   BtreePA.java:2462:<init>   BtreePA.java:3434:<init>   BtreePA.java:3433:test_verilog_find   BtreePA.java:3807:newTests   BtreePA.java:3814:main  */
reg [47:0] leaf_1_StuckSA_Transaction_62;  /*   MemoryLayoutPA.java:0971:declareVerilog   BtreePA.java:2297:stuckMemory   BtreePA.java:2281:stuckMemories   BtreePA.java:2497:editVariables   BtreePA.java:2492:editVariables   BtreePA.java:2462:<init>   BtreePA.java:3434:<init>   BtreePA.java:3433:test_verilog_find   BtreePA.java:3807:newTests   BtreePA.java:3814:main  */
reg[10: 0] index_leaf_1_StuckSA_Memory_Based_60_base_offset;
reg[10: 0] copyLength_leaf_1_StuckSA_Memory_Based_60_base_offset;
reg [10:0] leaf_2_StuckSA_Memory_Based_63_base_offset;
reg [20:0] leaf_2_StuckSA_Copy_64;  /*   MemoryLayoutPA.java:0971:declareVerilog   BtreePA.java:2296:stuckMemory   BtreePA.java:2281:stuckMemories   BtreePA.java:2497:editVariables   BtreePA.java:2492:editVariables   BtreePA.java:2462:<init>   BtreePA.java:3434:<init>   BtreePA.java:3433:test_verilog_find   BtreePA.java:3807:newTests   BtreePA.java:3814:main  */
reg [47:0] leaf_2_StuckSA_Transaction_65;  /*   MemoryLayoutPA.java:0971:declareVerilog   BtreePA.java:2297:stuckMemory   BtreePA.java:2281:stuckMemories   BtreePA.java:2497:editVariables   BtreePA.java:2492:editVariables   BtreePA.java:2462:<init>   BtreePA.java:3434:<init>   BtreePA.java:3433:test_verilog_find   BtreePA.java:3807:newTests   BtreePA.java:3814:main  */
reg[10: 0] index_leaf_2_StuckSA_Memory_Based_63_base_offset;
reg[10: 0] copyLength_leaf_2_StuckSA_Memory_Based_63_base_offset;
reg [10:0] leaf_3_StuckSA_Memory_Based_66_base_offset;
reg [20:0] leaf_3_StuckSA_Copy_67;  /*   MemoryLayoutPA.java:0971:declareVerilog   BtreePA.java:2296:stuckMemory   BtreePA.java:2281:stuckMemories   BtreePA.java:2497:editVariables   BtreePA.java:2492:editVariables   BtreePA.java:2462:<init>   BtreePA.java:3434:<init>   BtreePA.java:3433:test_verilog_find   BtreePA.java:3807:newTests   BtreePA.java:3814:main  */
reg [47:0] leaf_3_StuckSA_Transaction_68;  /*   MemoryLayoutPA.java:0971:declareVerilog   BtreePA.java:2297:stuckMemory   BtreePA.java:2281:stuckMemories   BtreePA.java:2497:editVariables   BtreePA.java:2492:editVariables   BtreePA.java:2462:<init>   BtreePA.java:3434:<init>   BtreePA.java:3433:test_verilog_find   BtreePA.java:3807:newTests   BtreePA.java:3814:main  */
reg[10: 0] index_leaf_3_StuckSA_Memory_Based_66_base_offset;
reg[10: 0] copyLength_leaf_3_StuckSA_Memory_Based_66_base_offset;


  always @ (posedge reset, posedge clock) begin                                 // Execute next step in program

    if (reset) begin                                                            // Reset
      step      = 0;
      steps    <= 0;
      stopped  <= 0;
      initialize_memory_M_43();                                                   // Initialize btree memory
      initialize_memory_T_44();                                                   // Initialize btree transaction
      traceFile = $fopen("trace.txt", "w");                                    // Open trace file
      if (!traceFile) $fatal(1, "Cannot open trace file trace.txt");
      branch_0_StuckSA_Memory_Based_45_base_offset <= 0;branch_0_StuckSA_Copy_46 <= 0;branch_0_StuckSA_Transaction_47 <= 0; /*   BtreePA.java:2304:stuckMemoryInitialization   BtreePA.java:2289:stuckMemoryInitialization   BtreePA.java:2498:editVariables   BtreePA.java:2492:editVariables   BtreePA.java:2462:<init>   BtreePA.java:3434:<init>   BtreePA.java:3433:test_verilog_find   BtreePA.java:3807:newTests   BtreePA.java:3814:main  */branch_1_StuckSA_Memory_Based_48_base_offset <= 0;branch_1_StuckSA_Copy_49 <= 0;branch_1_StuckSA_Transaction_50 <= 0; /*   BtreePA.java:2304:stuckMemoryInitialization   BtreePA.java:2289:stuckMemoryInitialization   BtreePA.java:2498:editVariables   BtreePA.java:2492:editVariables   BtreePA.java:2462:<init>   BtreePA.java:3434:<init>   BtreePA.java:3433:test_verilog_find   BtreePA.java:3807:newTests   BtreePA.java:3814:main  */branch_2_StuckSA_Memory_Based_51_base_offset <= 0;branch_2_StuckSA_Copy_52 <= 0;branch_2_StuckSA_Transaction_53 <= 0; /*   BtreePA.java:2304:stuckMemoryInitialization   BtreePA.java:2289:stuckMemoryInitialization   BtreePA.java:2498:editVariables   BtreePA.java:2492:editVariables   BtreePA.java:2462:<init>   BtreePA.java:3434:<init>   BtreePA.java:3433:test_verilog_find   BtreePA.java:3807:newTests   BtreePA.java:3814:main  */branch_3_StuckSA_Memory_Based_54_base_offset <= 0;branch_3_StuckSA_Copy_55 <= 0;branch_3_StuckSA_Transaction_56 <= 0; /*   BtreePA.java:2304:stuckMemoryInitialization   BtreePA.java:2289:stuckMemoryInitialization   BtreePA.java:2498:editVariables   BtreePA.java:2492:editVariables   BtreePA.java:2462:<init>   BtreePA.java:3434:<init>   BtreePA.java:3433:test_verilog_find   BtreePA.java:3807:newTests   BtreePA.java:3814:main  */leaf_0_StuckSA_Memory_Based_57_base_offset <= 0;leaf_0_StuckSA_Copy_58 <= 0;leaf_0_StuckSA_Transaction_59 <= 0; /*   BtreePA.java:2304:stuckMemoryInitialization   BtreePA.java:2290:stuckMemoryInitialization   BtreePA.java:2498:editVariables   BtreePA.java:2492:editVariables   BtreePA.java:2462:<init>   BtreePA.java:3434:<init>   BtreePA.java:3433:test_verilog_find   BtreePA.java:3807:newTests   BtreePA.java:3814:main  */leaf_1_StuckSA_Memory_Based_60_base_offset <= 0;leaf_1_StuckSA_Copy_61 <= 0;leaf_1_StuckSA_Transaction_62 <= 0; /*   BtreePA.java:2304:stuckMemoryInitialization   BtreePA.java:2290:stuckMemoryInitialization   BtreePA.java:2498:editVariables   BtreePA.java:2492:editVariables   BtreePA.java:2462:<init>   BtreePA.java:3434:<init>   BtreePA.java:3433:test_verilog_find   BtreePA.java:3807:newTests   BtreePA.java:3814:main  */leaf_2_StuckSA_Memory_Based_63_base_offset <= 0;leaf_2_StuckSA_Copy_64 <= 0;leaf_2_StuckSA_Transaction_65 <= 0; /*   BtreePA.java:2304:stuckMemoryInitialization   BtreePA.java:2290:stuckMemoryInitialization   BtreePA.java:2498:editVariables   BtreePA.java:2492:editVariables   BtreePA.java:2462:<init>   BtreePA.java:3434:<init>   BtreePA.java:3433:test_verilog_find   BtreePA.java:3807:newTests   BtreePA.java:3814:main  */leaf_3_StuckSA_Memory_Based_66_base_offset <= 0;leaf_3_StuckSA_Copy_67 <= 0;leaf_3_StuckSA_Transaction_68 <= 0; /*   BtreePA.java:2304:stuckMemoryInitialization   BtreePA.java:2290:stuckMemoryInitialization   BtreePA.java:2498:editVariables   BtreePA.java:2492:editVariables   BtreePA.java:2462:<init>   BtreePA.java:3434:<init>   BtreePA.java:3433:test_verilog_find   BtreePA.java:3807:newTests   BtreePA.java:3814:main  */
    end
    else begin                                                                  // Run
      $display            ("%4d  %4d  %b", steps, step, M_43);                    // Trace execution
      $fdisplay(traceFile, "%4d  %4d  %b", steps, step, M_43);                    // Trace execution in a file
      case(step)                                                                // Case statements to select the code for the current instruction
              0 : begin if (M_43[       4/*isLeaf  */ + 0 * 44 +: 1] == 0) step = 3; /*   ProgramPA.java:0221:<init>   ProgramPA.java:0220:GoOff   BtreePA.java:1913:code   ProgramPA.java:0270:<init>   BtreePA.java:1912:<init>   BtreePA.java:1911:code   ProgramPA.java:0270:<init>   BtreePA.java:1907:<init>   BtreePA.java:1906:find   BtreePA.java:3431:test_verilog_find   BtreePA.java:3807:newTests   BtreePA.java:3814:main  */ end
              1 : begin leaf_0_StuckSA_Memory_Based_57_base_offset <=        9/*leaf    */ + 0 * 44; /*   BtreePA.java:0672:<init>   BtreePA.java:0671:leafBase   BtreePA.java:0832:findEqualInLeaf   BtreePA.java:1918:code   ProgramPA.java:0270:<init>   BtreePA.java:1912:<init>   BtreePA.java:1911:code   ProgramPA.java:0270:<init>   BtreePA.java:1907:<init>   BtreePA.java:1906:find   BtreePA.java:3431:test_verilog_find   BtreePA.java:3807:newTests   BtreePA.java:3814:main  */ end
              2 : begin T_44[      22/*found   */ +: 1]= ( 0
 || (M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            3/*key     */ + 0 * 5 +: 5] == T_44[     113/*Key     */ +: 5] &&  0 < M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            0/*currentSize     */ +: 3])
 || (M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            3/*key     */ + 1 * 5 +: 5] == T_44[     113/*Key     */ +: 5] &&  1 < M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            0/*currentSize     */ +: 3])
) ? 1 : 0;
T_44[      70/*index   */ +: 3] =
(M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            3/*key     */ + 0 * 5 +: 5] == T_44[     113/*Key     */ +: 5] && 0 < M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            0/*currentSize     */ +: 3]) ? 0 :
(M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            3/*key     */ + 1 * 5 +: 5] == T_44[     113/*Key     */ +: 5] && 1 < M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            0/*currentSize     */ +: 3]) ? 1 :
0;
T_44[      28/*data    */ +: 4] =
(M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            3/*key     */ + 0 * 5 +: 5] == T_44[     113/*Key     */ +: 5] && 0 < M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            0/*currentSize     */ +: 3]) ? M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+           13/*data    */ + 0 * 4 +: 4] :
(M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            3/*key     */ + 1 * 5 +: 5] == T_44[     113/*Key     */ +: 5] && 1 < M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            0/*currentSize     */ +: 3]) ? M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+           13/*data    */ + 1 * 4 +: 4] :
0;
 /*   StuckPA.java:0526:<init>   StuckPA.java:0525:search   BtreePA.java:0836:findEqualInLeaf   BtreePA.java:1918:code   ProgramPA.java:0270:<init>   BtreePA.java:1912:<init>   BtreePA.java:1911:code   ProgramPA.java:0270:<init>   BtreePA.java:1907:<init>   BtreePA.java:1906:find   BtreePA.java:3431:test_verilog_find   BtreePA.java:3807:newTests   BtreePA.java:3814:main  */ end
              3 : begin
                    T_44[     122/*find    */ +: 4] <= 0; /*   MemoryLayoutPA.java:0562:<init>   MemoryLayoutPA.java:0561:zero   BtreePA.java:1920:code   ProgramPA.java:0270:<init>   BtreePA.java:1912:<init>   BtreePA.java:1911:code   ProgramPA.java:0270:<init>   BtreePA.java:1907:<init>   BtreePA.java:1906:find   BtreePA.java:3431:test_verilog_find   BtreePA.java:3807:newTests   BtreePA.java:3814:main  */
                    step = 11; /*   ProgramPA.java:0201:<init>   ProgramPA.java:0200:Goto   BtreePA.java:1921:code   ProgramPA.java:0270:<init>   BtreePA.java:1912:<init>   BtreePA.java:1911:code   ProgramPA.java:0270:<init>   BtreePA.java:1907:<init>   BtreePA.java:1906:find   BtreePA.java:3431:test_verilog_find   BtreePA.java:3807:newTests   BtreePA.java:3814:main  */
                  end
              4 : begin T_44[     130/*parent  */ +: 4] <= 0; /*   MemoryLayoutPA.java:0562:<init>   MemoryLayoutPA.java:0561:zero   BtreePA.java:1925:code   ProgramPA.java:0270:<init>   BtreePA.java:1907:<init>   BtreePA.java:1906:find   BtreePA.java:3431:test_verilog_find   BtreePA.java:3807:newTests   BtreePA.java:3814:main  */ end
              5 : begin branch_0_StuckSA_Memory_Based_45_base_offset <=        9/*branch  */ + T_44[     130/*parent  */ +: 4] * 44; /*   BtreePA.java:0706:<init>   BtreePA.java:0705:branchBase   BtreePA.java:0890:findFirstGreaterThanOrEqualInBranch   BtreePA.java:1939:code   ProgramPA.java:0270:<init>   BtreePA.java:1930:<init>   BtreePA.java:1929:code   ProgramPA.java:0270:<init>   BtreePA.java:1907:<init>   BtreePA.java:1906:find   BtreePA.java:3431:test_verilog_find   BtreePA.java:3807:newTests   BtreePA.java:3814:main  */ end
              6 : begin T_44[     134/*child   */ +: 4] =
(M_43[branch_0_StuckSA_Memory_Based_45_base_offset+          3/*key     */ + 0 * 5 +: 5] >= T_44[     113/*Key     */ +: 5] && 0 < M_43[branch_0_StuckSA_Memory_Based_45_base_offset+          0/*currentSize     */ +: 3]-1) ? M_43[branch_0_StuckSA_Memory_Based_45_base_offset+         23/*data    */ + 0 * 4 +: 4] :
(M_43[branch_0_StuckSA_Memory_Based_45_base_offset+          3/*key     */ + 1 * 5 +: 5] >= T_44[     113/*Key     */ +: 5] && 1 < M_43[branch_0_StuckSA_Memory_Based_45_base_offset+          0/*currentSize     */ +: 3]-1) ? M_43[branch_0_StuckSA_Memory_Based_45_base_offset+         23/*data    */ + 1 * 4 +: 4] :
(M_43[branch_0_StuckSA_Memory_Based_45_base_offset+          3/*key     */ + 2 * 5 +: 5] >= T_44[     113/*Key     */ +: 5] && 2 < M_43[branch_0_StuckSA_Memory_Based_45_base_offset+          0/*currentSize     */ +: 3]-1) ? M_43[branch_0_StuckSA_Memory_Based_45_base_offset+         23/*data    */ + 2 * 4 +: 4] :
(M_43[branch_0_StuckSA_Memory_Based_45_base_offset+          3/*key     */ + 3 * 5 +: 5] >= T_44[     113/*Key     */ +: 5] && 3 < M_43[branch_0_StuckSA_Memory_Based_45_base_offset+          0/*currentSize     */ +: 3]-1) ? M_43[branch_0_StuckSA_Memory_Based_45_base_offset+         23/*data    */ + 3 * 4 +: 4] :
M_43[branch_0_StuckSA_Memory_Based_45_base_offset+         23/*data    */ + M_43[branch_0_StuckSA_Memory_Based_45_base_offset+          0/*currentSize     */ +: 3] * 4-4 +: 4];
 /*   StuckPA.java:0622:<init>   StuckPA.java:0621:searchFirstGreaterThanOrEqual   BtreePA.java:0891:findFirstGreaterThanOrEqualInBranch   BtreePA.java:1939:code   ProgramPA.java:0270:<init>   BtreePA.java:1930:<init>   BtreePA.java:1929:code   ProgramPA.java:0270:<init>   BtreePA.java:1907:<init>   BtreePA.java:1906:find   BtreePA.java:3431:test_verilog_find   BtreePA.java:3807:newTests   BtreePA.java:3814:main  */ end
              7 : begin if (M_43[       4/*isLeaf  */ + T_44[     134/*child   */ +: 4] * 44 +: 1] == 0) step = 10; /*   ProgramPA.java:0221:<init>   ProgramPA.java:0220:GoOff   BtreePA.java:1947:code   ProgramPA.java:0270:<init>   BtreePA.java:1946:<init>   BtreePA.java:1945:code   ProgramPA.java:0270:<init>   BtreePA.java:1930:<init>   BtreePA.java:1929:code   ProgramPA.java:0270:<init>   BtreePA.java:1907:<init>   BtreePA.java:1906:find   BtreePA.java:3431:test_verilog_find   BtreePA.java:3807:newTests   BtreePA.java:3814:main  */ end
              8 : begin leaf_0_StuckSA_Memory_Based_57_base_offset <=        9/*leaf    */ + T_44[     134/*child   */ +: 4] * 44; /*   BtreePA.java:0672:<init>   BtreePA.java:0671:leafBase   BtreePA.java:0832:findEqualInLeaf   BtreePA.java:1949:code   ProgramPA.java:0270:<init>   BtreePA.java:1946:<init>   BtreePA.java:1945:code   ProgramPA.java:0270:<init>   BtreePA.java:1930:<init>   BtreePA.java:1929:code   ProgramPA.java:0270:<init>   BtreePA.java:1907:<init>   BtreePA.java:1906:find   BtreePA.java:3431:test_verilog_find   BtreePA.java:3807:newTests   BtreePA.java:3814:main  */ end
              9 : begin T_44[      22/*found   */ +: 1]= ( 0
 || (M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            3/*key     */ + 0 * 5 +: 5] == T_44[     113/*Key     */ +: 5] &&  0 < M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            0/*currentSize     */ +: 3])
 || (M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            3/*key     */ + 1 * 5 +: 5] == T_44[     113/*Key     */ +: 5] &&  1 < M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            0/*currentSize     */ +: 3])
) ? 1 : 0;
T_44[      70/*index   */ +: 3] =
(M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            3/*key     */ + 0 * 5 +: 5] == T_44[     113/*Key     */ +: 5] && 0 < M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            0/*currentSize     */ +: 3]) ? 0 :
(M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            3/*key     */ + 1 * 5 +: 5] == T_44[     113/*Key     */ +: 5] && 1 < M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            0/*currentSize     */ +: 3]) ? 1 :
0;
T_44[      28/*data    */ +: 4] =
(M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            3/*key     */ + 0 * 5 +: 5] == T_44[     113/*Key     */ +: 5] && 0 < M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            0/*currentSize     */ +: 3]) ? M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+           13/*data    */ + 0 * 4 +: 4] :
(M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            3/*key     */ + 1 * 5 +: 5] == T_44[     113/*Key     */ +: 5] && 1 < M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            0/*currentSize     */ +: 3]) ? M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+           13/*data    */ + 1 * 4 +: 4] :
0;
 /*   StuckPA.java:0526:<init>   StuckPA.java:0525:search   BtreePA.java:0836:findEqualInLeaf   BtreePA.java:1949:code   ProgramPA.java:0270:<init>   BtreePA.java:1946:<init>   BtreePA.java:1945:code   ProgramPA.java:0270:<init>   BtreePA.java:1930:<init>   BtreePA.java:1929:code   ProgramPA.java:0270:<init>   BtreePA.java:1907:<init>   BtreePA.java:1906:find   BtreePA.java:3431:test_verilog_find   BtreePA.java:3807:newTests   BtreePA.java:3814:main  */ end
             10 : begin
                    T_44[     122/*find    */ +: 4] <= T_44[     134/*child   */ +: 4]; /*   MemoryLayoutPA.java:0382:<init>   MemoryLayoutPA.java:0381:move   BtreePA.java:0258:tt   BtreePA.java:1951:code   ProgramPA.java:0270:<init>   BtreePA.java:1946:<init>   BtreePA.java:1945:code   ProgramPA.java:0270:<init>   BtreePA.java:1930:<init>   BtreePA.java:1929:code   ProgramPA.java:0270:<init>   BtreePA.java:1907:<init>   BtreePA.java:1906:find   BtreePA.java:3431:test_verilog_find   BtreePA.java:3807:newTests   BtreePA.java:3814:main  */
                    step = 11; /*   ProgramPA.java:0201:<init>   ProgramPA.java:0200:Goto   BtreePA.java:1952:code   ProgramPA.java:0270:<init>   BtreePA.java:1946:<init>   BtreePA.java:1945:code   ProgramPA.java:0270:<init>   BtreePA.java:1930:<init>   BtreePA.java:1929:code   ProgramPA.java:0270:<init>   BtreePA.java:1907:<init>   BtreePA.java:1906:find   BtreePA.java:3431:test_verilog_find   BtreePA.java:3807:newTests   BtreePA.java:3814:main  */
                  end
             11 : begin
                    T_44[     130/*parent  */ +: 4] <= T_44[     134/*child   */ +: 4]; /*   MemoryLayoutPA.java:0382:<init>   MemoryLayoutPA.java:0381:move   BtreePA.java:0258:tt   BtreePA.java:1958:code   ProgramPA.java:0270:<init>   BtreePA.java:1930:<init>   BtreePA.java:1929:code   ProgramPA.java:0270:<init>   BtreePA.java:1907:<init>   BtreePA.java:1906:find   BtreePA.java:3431:test_verilog_find   BtreePA.java:3807:newTests   BtreePA.java:3814:main  */
                    step = 4; /*   ProgramPA.java:0201:<init>   ProgramPA.java:0200:Goto   BtreePA.java:1960:code   ProgramPA.java:0270:<init>   BtreePA.java:1930:<init>   BtreePA.java:1929:code   ProgramPA.java:0270:<init>   BtreePA.java:1907:<init>   BtreePA.java:1906:find   BtreePA.java:3431:test_verilog_find   BtreePA.java:3807:newTests   BtreePA.java:3814:main  */
                  end
        default : begin stopped <= 1; /* end of execution */ end
      endcase
      step   = step  + 1;
      steps <= steps + 1;
    end // Execute
  end // Always
endmodule
