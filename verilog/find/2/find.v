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
  assign found = T_44[22];                                                 // Found the key
  assign data  = T_44[28+:4];                                     // Data associated with key found

reg [10:0] branch_0_StuckSA_Memory_Based_45_base_offset;
reg [10:0] leaf_0_StuckSA_Memory_Based_57_base_offset;


  always @ (posedge reset, posedge clock) begin                                 // Execute next step in program

    if (reset) begin                                                            // Reset
      step      = 0;
     `ifndef SYNTHESIS
        steps  <= 0;
     `endif
      stopped  <= 0;
      initialize_memory_M_43();                                                   // Initialize btree memory
      initialize_memory_T_44();                                                   // Initialize btree transaction
      initialize_opCodeMap();                                                  // Initialize op code map
     `ifndef SYNTHESIS
        traceFile = $fopen("trace.txt", "w");                                  // Open trace file
        if (!traceFile) $fatal(1, "Cannot open trace file trace.txt");
     `endif
      
     `ifdef SYNTHESIS
        T_44[113 +:5 ] <= Key;                                       // Load key
        T_44[118+:4] <= Data;                                      // Connect data
     `endif
    end
    else begin                                                                  // Run
     `ifndef SYNTHESIS
        $display            ("%4d  %4d  %b", steps, step, M_43);                  // Trace execution
        $fdisplay(traceFile, "%4d  %4d  %b", steps, step, M_43);                  // Trace execution in a file
     `endif
      case(opCodeMap[step])
          0 : begin if (M_43[       4/*isLeaf  */ + 0 * 44 +: 1] == 0) step = 3; end
          1 : begin leaf_0_StuckSA_Memory_Based_57_base_offset <=        9/*leaf    */ + 0 * 44; end
          2 : begin
T_44[      22/*found   */ +: 1]= ( 0
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

                end
          3 : begin
                    T_44[     122/*find    */ +: 4] <= 0;
                    step = 11;

                end
          4 : begin T_44[     130/*parent  */ +: 4] <= 0; end
          5 : begin branch_0_StuckSA_Memory_Based_45_base_offset <=        9/*branch  */ + T_44[     130/*parent  */ +: 4] * 44; end
          6 : begin
T_44[     134/*child   */ +: 4] =
(M_43[branch_0_StuckSA_Memory_Based_45_base_offset+          3/*key     */ + 0 * 5 +: 5] >= T_44[     113/*Key     */ +: 5] && 0 < M_43[branch_0_StuckSA_Memory_Based_45_base_offset+          0/*currentSize     */ +: 3]-1) ? M_43[branch_0_StuckSA_Memory_Based_45_base_offset+         23/*data    */ + 0 * 4 +: 4] :
(M_43[branch_0_StuckSA_Memory_Based_45_base_offset+          3/*key     */ + 1 * 5 +: 5] >= T_44[     113/*Key     */ +: 5] && 1 < M_43[branch_0_StuckSA_Memory_Based_45_base_offset+          0/*currentSize     */ +: 3]-1) ? M_43[branch_0_StuckSA_Memory_Based_45_base_offset+         23/*data    */ + 1 * 4 +: 4] :
(M_43[branch_0_StuckSA_Memory_Based_45_base_offset+          3/*key     */ + 2 * 5 +: 5] >= T_44[     113/*Key     */ +: 5] && 2 < M_43[branch_0_StuckSA_Memory_Based_45_base_offset+          0/*currentSize     */ +: 3]-1) ? M_43[branch_0_StuckSA_Memory_Based_45_base_offset+         23/*data    */ + 2 * 4 +: 4] :
(M_43[branch_0_StuckSA_Memory_Based_45_base_offset+          3/*key     */ + 3 * 5 +: 5] >= T_44[     113/*Key     */ +: 5] && 3 < M_43[branch_0_StuckSA_Memory_Based_45_base_offset+          0/*currentSize     */ +: 3]-1) ? M_43[branch_0_StuckSA_Memory_Based_45_base_offset+         23/*data    */ + 3 * 4 +: 4] :
M_43[branch_0_StuckSA_Memory_Based_45_base_offset+         23/*data    */ + M_43[branch_0_StuckSA_Memory_Based_45_base_offset+          0/*currentSize     */ +: 3] * 4-4 +: 4];

                end
          7 : begin if (M_43[       4/*isLeaf  */ + T_44[     134/*child   */ +: 4] * 44 +: 1] == 0) step = 10; end
          8 : begin leaf_0_StuckSA_Memory_Based_57_base_offset <=        9/*leaf    */ + T_44[     134/*child   */ +: 4] * 44; end
          9 : begin
                    T_44[     122/*find    */ +: 4] <= T_44[     134/*child   */ +: 4];
                    step = 11;

                end
          10 : begin
                    T_44[     130/*parent  */ +: 4] <= T_44[     134/*child   */ +: 4];
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
