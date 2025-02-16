//-----------------------------------------------------------------------------
// Database on a chip
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2025-01-07
//------------------------------------------------------------------------------
`timescale 10ps/1ps
(* keep_hierarchy = "yes" *)
module find(reset, stop, clock, Key, Data, data, found);                    // Database on a chip
  input                 reset;                                                  // Restart the program run sequence when this goes high
  input                 clock;                                                  // Program counter clock
  input [5 :0]  Key;                                                  // Input key
  input [4:0] Data;                                                  // Input data
  output                 stop;                                                  // Program has stopped when this goes high
  output[4:0] data;                                                  // Output data
  output                found;                                                  // Whether the key was found on put, find delete

  `include "M.vh"                                                               // Memory holding a pre built tree from test_dump()
  `include "T.vh"                                                               // Transaction memory which is initialized to some values to reduce the complexity of Memory at by treating constants as variables
  `include "opCodeMap.vh"                                                       // Op code map gives step to instruction

  integer  step;                                                                // Program counter
  `ifndef SYNTHESIS
    integer steps;                                                              // Number of steps executed
    integer traceFile;                                                          // File to write trace to
  `endif
  reg   stopped;                                                                // Set when we stop
  assign stop  = stopped > 0 ? 1 : 0;                                           // Stopped execution
  assign found = T_10[22];                                                 // Found the key
  assign data  = T_10[28+:4];                                     // Data associated with key found

reg [10:0] branch_0_StuckSA_Memory_Based_11_base_offset;
reg [38:0] branch_0_StuckSA_Copy_12;  /*   MemoryLayoutPA.java:0978:declareVerilog   BtreePA.java:2299:stuckMemory   BtreePA.java:2282:stuckMemories   BtreePA.java:2555:editVariables   BtreePA.java:2550:editVariables   BtreePA.java:2520:<init>   BtreePA.java:3497:<init>   BtreePA.java:3496:test_verilog_find   BtreePA.java:3870:newTests   BtreePA.java:3877:main  */
reg [47:0] branch_0_StuckSA_Transaction_13;  /*   MemoryLayoutPA.java:0978:declareVerilog   BtreePA.java:2300:stuckMemory   BtreePA.java:2282:stuckMemories   BtreePA.java:2555:editVariables   BtreePA.java:2550:editVariables   BtreePA.java:2520:<init>   BtreePA.java:3497:<init>   BtreePA.java:3496:test_verilog_find   BtreePA.java:3870:newTests   BtreePA.java:3877:main  */
reg[10: 0] index_branch_0_StuckSA_Memory_Based_11_base_offset;
reg[10: 0] copyLength_branch_0_StuckSA_Memory_Based_11_base_offset;
reg [10:0] leaf_0_StuckSA_Memory_Based_23_base_offset;
reg [20:0] leaf_0_StuckSA_Copy_24;  /*   MemoryLayoutPA.java:0978:declareVerilog   BtreePA.java:2299:stuckMemory   BtreePA.java:2283:stuckMemories   BtreePA.java:2555:editVariables   BtreePA.java:2550:editVariables   BtreePA.java:2520:<init>   BtreePA.java:3497:<init>   BtreePA.java:3496:test_verilog_find   BtreePA.java:3870:newTests   BtreePA.java:3877:main  */
reg [47:0] leaf_0_StuckSA_Transaction_25;  /*   MemoryLayoutPA.java:0978:declareVerilog   BtreePA.java:2300:stuckMemory   BtreePA.java:2283:stuckMemories   BtreePA.java:2555:editVariables   BtreePA.java:2550:editVariables   BtreePA.java:2520:<init>   BtreePA.java:3497:<init>   BtreePA.java:3496:test_verilog_find   BtreePA.java:3870:newTests   BtreePA.java:3877:main  */
reg[10: 0] index_leaf_0_StuckSA_Memory_Based_23_base_offset;
reg[10: 0] copyLength_leaf_0_StuckSA_Memory_Based_23_base_offset;


  always @ (posedge reset, posedge clock) begin                                 // Execute next step in program

    if (reset) begin                                                            // Reset
      step      = 0;
     `ifndef SYNTHESIS
        steps  <= 0;
     `endif
      stopped  <= 0;
      initialize_memory_M_9();                                                   // Initialize btree memory
      initialize_memory_T_10();                                                   // Initialize btree transaction
      initialize_opCodeMap();                                                  // Initialize op code map
     `ifndef SYNTHESIS
        traceFile = $fopen("trace.txt", "w");                                  // Open trace file
        if (!traceFile) $fatal(1, "Cannot open trace file trace.txt");
     `endif
      branch_0_StuckSA_Memory_Based_11_base_offset <= 0;branch_0_StuckSA_Copy_12 <= 0;branch_0_StuckSA_Transaction_13 <= 0; /*   BtreePA.java:2307:stuckMemoryInitialization   BtreePA.java:2292:stuckMemoryInitialization   BtreePA.java:2556:editVariables   BtreePA.java:2550:editVariables   BtreePA.java:2520:<init>   BtreePA.java:3497:<init>   BtreePA.java:3496:test_verilog_find   BtreePA.java:3870:newTests   BtreePA.java:3877:main  */leaf_0_StuckSA_Memory_Based_23_base_offset <= 0;leaf_0_StuckSA_Copy_24 <= 0;leaf_0_StuckSA_Transaction_25 <= 0; /*   BtreePA.java:2307:stuckMemoryInitialization   BtreePA.java:2293:stuckMemoryInitialization   BtreePA.java:2556:editVariables   BtreePA.java:2550:editVariables   BtreePA.java:2520:<init>   BtreePA.java:3497:<init>   BtreePA.java:3496:test_verilog_find   BtreePA.java:3870:newTests   BtreePA.java:3877:main  */
     `ifdef SYNTHESIS
        T_10[113 +:5 ] <= Key;                                       // Load key
        T_10[118+:4] <= Data;                                      // Connect data
     `endif
    end
    else begin                                                                  // Run
     `ifndef SYNTHESIS
        $display            ("%4d  %4d  %b", steps, step, M_9);                  // Trace execution
        $fdisplay(traceFile, "%4d  %4d  %b", steps, step, M_9);                  // Trace execution in a file
     `endif
      case(opCodeMap[step])
          0 : begin if (M_9[       4/*isLeaf  */ + 0 * 44 +: 1] == 0) step = 3; end
          1 : begin leaf_0_StuckSA_Memory_Based_23_base_offset <=        9/*leaf    */ + 0 * 44; end
          2 : begin
T_10[      22/*found   */ +: 1]= ( 0
 || (M_9[leaf_0_StuckSA_Memory_Based_23_base_offset+            3/*key     */ + 0 * 5 +: 5] == T_10[     113/*Key     */ +: 5] &&  0 < M_9[leaf_0_StuckSA_Memory_Based_23_base_offset+            0/*currentSize     */ +: 3])
 || (M_9[leaf_0_StuckSA_Memory_Based_23_base_offset+            3/*key     */ + 1 * 5 +: 5] == T_10[     113/*Key     */ +: 5] &&  1 < M_9[leaf_0_StuckSA_Memory_Based_23_base_offset+            0/*currentSize     */ +: 3])
) ? 1 : 0;
T_10[      70/*index   */ +: 3] =
(M_9[leaf_0_StuckSA_Memory_Based_23_base_offset+            3/*key     */ + 0 * 5 +: 5] == T_10[     113/*Key     */ +: 5] && 0 < M_9[leaf_0_StuckSA_Memory_Based_23_base_offset+            0/*currentSize     */ +: 3]) ? 0 :
(M_9[leaf_0_StuckSA_Memory_Based_23_base_offset+            3/*key     */ + 1 * 5 +: 5] == T_10[     113/*Key     */ +: 5] && 1 < M_9[leaf_0_StuckSA_Memory_Based_23_base_offset+            0/*currentSize     */ +: 3]) ? 1 :
0;
T_10[      28/*data    */ +: 4] =
(M_9[leaf_0_StuckSA_Memory_Based_23_base_offset+            3/*key     */ + 0 * 5 +: 5] == T_10[     113/*Key     */ +: 5] && 0 < M_9[leaf_0_StuckSA_Memory_Based_23_base_offset+            0/*currentSize     */ +: 3]) ? M_9[leaf_0_StuckSA_Memory_Based_23_base_offset+           13/*data    */ + 0 * 4 +: 4] :
(M_9[leaf_0_StuckSA_Memory_Based_23_base_offset+            3/*key     */ + 1 * 5 +: 5] == T_10[     113/*Key     */ +: 5] && 1 < M_9[leaf_0_StuckSA_Memory_Based_23_base_offset+            0/*currentSize     */ +: 3]) ? M_9[leaf_0_StuckSA_Memory_Based_23_base_offset+           13/*data    */ + 1 * 4 +: 4] :
0;

                end
          3 : begin
                    T_10[     122/*find    */ +: 4] <= 0;
                    step = 11;

                end
          4 : begin T_10[     130/*parent  */ +: 4] <= 0; end
          5 : begin branch_0_StuckSA_Memory_Based_11_base_offset <=        9/*branch  */ + T_10[     130/*parent  */ +: 4] * 44; end
          6 : begin
T_10[     134/*child   */ +: 4] =
(M_9[branch_0_StuckSA_Memory_Based_11_base_offset+          3/*key     */ + 0 * 5 +: 5] >= T_10[     113/*Key     */ +: 5] && 0 < M_9[branch_0_StuckSA_Memory_Based_11_base_offset+          0/*currentSize     */ +: 3]-1) ? M_9[branch_0_StuckSA_Memory_Based_11_base_offset+         23/*data    */ + 0 * 4 +: 4] :
(M_9[branch_0_StuckSA_Memory_Based_11_base_offset+          3/*key     */ + 1 * 5 +: 5] >= T_10[     113/*Key     */ +: 5] && 1 < M_9[branch_0_StuckSA_Memory_Based_11_base_offset+          0/*currentSize     */ +: 3]-1) ? M_9[branch_0_StuckSA_Memory_Based_11_base_offset+         23/*data    */ + 1 * 4 +: 4] :
(M_9[branch_0_StuckSA_Memory_Based_11_base_offset+          3/*key     */ + 2 * 5 +: 5] >= T_10[     113/*Key     */ +: 5] && 2 < M_9[branch_0_StuckSA_Memory_Based_11_base_offset+          0/*currentSize     */ +: 3]-1) ? M_9[branch_0_StuckSA_Memory_Based_11_base_offset+         23/*data    */ + 2 * 4 +: 4] :
(M_9[branch_0_StuckSA_Memory_Based_11_base_offset+          3/*key     */ + 3 * 5 +: 5] >= T_10[     113/*Key     */ +: 5] && 3 < M_9[branch_0_StuckSA_Memory_Based_11_base_offset+          0/*currentSize     */ +: 3]-1) ? M_9[branch_0_StuckSA_Memory_Based_11_base_offset+         23/*data    */ + 3 * 4 +: 4] :
M_9[branch_0_StuckSA_Memory_Based_11_base_offset+         23/*data    */ + M_9[branch_0_StuckSA_Memory_Based_11_base_offset+          0/*currentSize     */ +: 3] * 4-4 +: 4];

                end
          7 : begin if (M_9[       4/*isLeaf  */ + T_10[     134/*child   */ +: 4] * 44 +: 1] == 0) step = 10; end
          8 : begin leaf_0_StuckSA_Memory_Based_23_base_offset <=        9/*leaf    */ + T_10[     134/*child   */ +: 4] * 44; end
          9 : begin
                    T_10[     122/*find    */ +: 4] <= T_10[     134/*child   */ +: 4];
                    step = 11;

                end
          10 : begin
                    T_10[     130/*parent  */ +: 4] <= T_10[     134/*child   */ +: 4];
                    step = 4;

                end
        default : begin stopped <= 1; /* end of execution */ end
      endcase
      step = step + 1;
     `ifndef SYNTHESIS
        steps <= steps + 1;
     `endif
    end // Execute
  end // Always
endmodule
