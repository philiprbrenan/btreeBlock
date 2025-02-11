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
  assign found = T_44[29];                                                 // Found the key
  assign data  = T_44[38+:8];                                     // Data associated with key found

reg [11:0] branch_0_StuckSA_Memory_Based_45_base_offset;
reg [55:0] branch_0_StuckSA_Copy_46;  /*   MemoryLayoutPA.java:0971:declareVerilog   BtreePA.java:2339:stuckMemory   BtreePA.java:2323:stuckMemories   BtreePA.java:2540:editVariables   BtreePA.java:2535:editVariables   BtreePA.java:2505:<init>   BtreePA.java:3477:<init>   BtreePA.java:3476:test_verilog_find   BtreePA.java:3850:newTests   BtreePA.java:3857:main  */
reg [65:0] branch_0_StuckSA_Transaction_47;  /*   MemoryLayoutPA.java:0971:declareVerilog   BtreePA.java:2340:stuckMemory   BtreePA.java:2323:stuckMemories   BtreePA.java:2540:editVariables   BtreePA.java:2535:editVariables   BtreePA.java:2505:<init>   BtreePA.java:3477:<init>   BtreePA.java:3476:test_verilog_find   BtreePA.java:3850:newTests   BtreePA.java:3857:main  */
reg[11: 0] index_branch_0_StuckSA_Memory_Based_45_base_offset;
reg[11: 0] copyLength_branch_0_StuckSA_Memory_Based_45_base_offset;
reg [11:0] branch_1_StuckSA_Memory_Based_48_base_offset;
reg [55:0] branch_1_StuckSA_Copy_49;  /*   MemoryLayoutPA.java:0971:declareVerilog   BtreePA.java:2339:stuckMemory   BtreePA.java:2323:stuckMemories   BtreePA.java:2540:editVariables   BtreePA.java:2535:editVariables   BtreePA.java:2505:<init>   BtreePA.java:3477:<init>   BtreePA.java:3476:test_verilog_find   BtreePA.java:3850:newTests   BtreePA.java:3857:main  */
reg [65:0] branch_1_StuckSA_Transaction_50;  /*   MemoryLayoutPA.java:0971:declareVerilog   BtreePA.java:2340:stuckMemory   BtreePA.java:2323:stuckMemories   BtreePA.java:2540:editVariables   BtreePA.java:2535:editVariables   BtreePA.java:2505:<init>   BtreePA.java:3477:<init>   BtreePA.java:3476:test_verilog_find   BtreePA.java:3850:newTests   BtreePA.java:3857:main  */
reg[11: 0] index_branch_1_StuckSA_Memory_Based_48_base_offset;
reg[11: 0] copyLength_branch_1_StuckSA_Memory_Based_48_base_offset;
reg [11:0] branch_2_StuckSA_Memory_Based_51_base_offset;
reg [55:0] branch_2_StuckSA_Copy_52;  /*   MemoryLayoutPA.java:0971:declareVerilog   BtreePA.java:2339:stuckMemory   BtreePA.java:2323:stuckMemories   BtreePA.java:2540:editVariables   BtreePA.java:2535:editVariables   BtreePA.java:2505:<init>   BtreePA.java:3477:<init>   BtreePA.java:3476:test_verilog_find   BtreePA.java:3850:newTests   BtreePA.java:3857:main  */
reg [65:0] branch_2_StuckSA_Transaction_53;  /*   MemoryLayoutPA.java:0971:declareVerilog   BtreePA.java:2340:stuckMemory   BtreePA.java:2323:stuckMemories   BtreePA.java:2540:editVariables   BtreePA.java:2535:editVariables   BtreePA.java:2505:<init>   BtreePA.java:3477:<init>   BtreePA.java:3476:test_verilog_find   BtreePA.java:3850:newTests   BtreePA.java:3857:main  */
reg[11: 0] index_branch_2_StuckSA_Memory_Based_51_base_offset;
reg[11: 0] copyLength_branch_2_StuckSA_Memory_Based_51_base_offset;
reg [11:0] branch_3_StuckSA_Memory_Based_54_base_offset;
reg [55:0] branch_3_StuckSA_Copy_55;  /*   MemoryLayoutPA.java:0971:declareVerilog   BtreePA.java:2339:stuckMemory   BtreePA.java:2323:stuckMemories   BtreePA.java:2540:editVariables   BtreePA.java:2535:editVariables   BtreePA.java:2505:<init>   BtreePA.java:3477:<init>   BtreePA.java:3476:test_verilog_find   BtreePA.java:3850:newTests   BtreePA.java:3857:main  */
reg [65:0] branch_3_StuckSA_Transaction_56;  /*   MemoryLayoutPA.java:0971:declareVerilog   BtreePA.java:2340:stuckMemory   BtreePA.java:2323:stuckMemories   BtreePA.java:2540:editVariables   BtreePA.java:2535:editVariables   BtreePA.java:2505:<init>   BtreePA.java:3477:<init>   BtreePA.java:3476:test_verilog_find   BtreePA.java:3850:newTests   BtreePA.java:3857:main  */
reg[11: 0] index_branch_3_StuckSA_Memory_Based_54_base_offset;
reg[11: 0] copyLength_branch_3_StuckSA_Memory_Based_54_base_offset;
reg [11:0] leaf_0_StuckSA_Memory_Based_57_base_offset;
reg [35:0] leaf_0_StuckSA_Copy_58;  /*   MemoryLayoutPA.java:0971:declareVerilog   BtreePA.java:2339:stuckMemory   BtreePA.java:2324:stuckMemories   BtreePA.java:2540:editVariables   BtreePA.java:2535:editVariables   BtreePA.java:2505:<init>   BtreePA.java:3477:<init>   BtreePA.java:3476:test_verilog_find   BtreePA.java:3850:newTests   BtreePA.java:3857:main  */
reg [71:0] leaf_0_StuckSA_Transaction_59;  /*   MemoryLayoutPA.java:0971:declareVerilog   BtreePA.java:2340:stuckMemory   BtreePA.java:2324:stuckMemories   BtreePA.java:2540:editVariables   BtreePA.java:2535:editVariables   BtreePA.java:2505:<init>   BtreePA.java:3477:<init>   BtreePA.java:3476:test_verilog_find   BtreePA.java:3850:newTests   BtreePA.java:3857:main  */
reg[11: 0] index_leaf_0_StuckSA_Memory_Based_57_base_offset;
reg[11: 0] copyLength_leaf_0_StuckSA_Memory_Based_57_base_offset;
reg [11:0] leaf_1_StuckSA_Memory_Based_60_base_offset;
reg [35:0] leaf_1_StuckSA_Copy_61;  /*   MemoryLayoutPA.java:0971:declareVerilog   BtreePA.java:2339:stuckMemory   BtreePA.java:2324:stuckMemories   BtreePA.java:2540:editVariables   BtreePA.java:2535:editVariables   BtreePA.java:2505:<init>   BtreePA.java:3477:<init>   BtreePA.java:3476:test_verilog_find   BtreePA.java:3850:newTests   BtreePA.java:3857:main  */
reg [71:0] leaf_1_StuckSA_Transaction_62;  /*   MemoryLayoutPA.java:0971:declareVerilog   BtreePA.java:2340:stuckMemory   BtreePA.java:2324:stuckMemories   BtreePA.java:2540:editVariables   BtreePA.java:2535:editVariables   BtreePA.java:2505:<init>   BtreePA.java:3477:<init>   BtreePA.java:3476:test_verilog_find   BtreePA.java:3850:newTests   BtreePA.java:3857:main  */
reg[11: 0] index_leaf_1_StuckSA_Memory_Based_60_base_offset;
reg[11: 0] copyLength_leaf_1_StuckSA_Memory_Based_60_base_offset;
reg [11:0] leaf_2_StuckSA_Memory_Based_63_base_offset;
reg [35:0] leaf_2_StuckSA_Copy_64;  /*   MemoryLayoutPA.java:0971:declareVerilog   BtreePA.java:2339:stuckMemory   BtreePA.java:2324:stuckMemories   BtreePA.java:2540:editVariables   BtreePA.java:2535:editVariables   BtreePA.java:2505:<init>   BtreePA.java:3477:<init>   BtreePA.java:3476:test_verilog_find   BtreePA.java:3850:newTests   BtreePA.java:3857:main  */
reg [71:0] leaf_2_StuckSA_Transaction_65;  /*   MemoryLayoutPA.java:0971:declareVerilog   BtreePA.java:2340:stuckMemory   BtreePA.java:2324:stuckMemories   BtreePA.java:2540:editVariables   BtreePA.java:2535:editVariables   BtreePA.java:2505:<init>   BtreePA.java:3477:<init>   BtreePA.java:3476:test_verilog_find   BtreePA.java:3850:newTests   BtreePA.java:3857:main  */
reg[11: 0] index_leaf_2_StuckSA_Memory_Based_63_base_offset;
reg[11: 0] copyLength_leaf_2_StuckSA_Memory_Based_63_base_offset;
reg [11:0] leaf_3_StuckSA_Memory_Based_66_base_offset;
reg [35:0] leaf_3_StuckSA_Copy_67;  /*   MemoryLayoutPA.java:0971:declareVerilog   BtreePA.java:2339:stuckMemory   BtreePA.java:2324:stuckMemories   BtreePA.java:2540:editVariables   BtreePA.java:2535:editVariables   BtreePA.java:2505:<init>   BtreePA.java:3477:<init>   BtreePA.java:3476:test_verilog_find   BtreePA.java:3850:newTests   BtreePA.java:3857:main  */
reg [71:0] leaf_3_StuckSA_Transaction_68;  /*   MemoryLayoutPA.java:0971:declareVerilog   BtreePA.java:2340:stuckMemory   BtreePA.java:2324:stuckMemories   BtreePA.java:2540:editVariables   BtreePA.java:2535:editVariables   BtreePA.java:2505:<init>   BtreePA.java:3477:<init>   BtreePA.java:3476:test_verilog_find   BtreePA.java:3850:newTests   BtreePA.java:3857:main  */
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
      branch_0_StuckSA_Memory_Based_45_base_offset <= 0;branch_0_StuckSA_Copy_46 <= 0;branch_0_StuckSA_Transaction_47 <= 0; /*   BtreePA.java:2347:stuckMemoryInitialization   BtreePA.java:2332:stuckMemoryInitialization   BtreePA.java:2541:editVariables   BtreePA.java:2535:editVariables   BtreePA.java:2505:<init>   BtreePA.java:3477:<init>   BtreePA.java:3476:test_verilog_find   BtreePA.java:3850:newTests   BtreePA.java:3857:main  */branch_1_StuckSA_Memory_Based_48_base_offset <= 0;branch_1_StuckSA_Copy_49 <= 0;branch_1_StuckSA_Transaction_50 <= 0; /*   BtreePA.java:2347:stuckMemoryInitialization   BtreePA.java:2332:stuckMemoryInitialization   BtreePA.java:2541:editVariables   BtreePA.java:2535:editVariables   BtreePA.java:2505:<init>   BtreePA.java:3477:<init>   BtreePA.java:3476:test_verilog_find   BtreePA.java:3850:newTests   BtreePA.java:3857:main  */branch_2_StuckSA_Memory_Based_51_base_offset <= 0;branch_2_StuckSA_Copy_52 <= 0;branch_2_StuckSA_Transaction_53 <= 0; /*   BtreePA.java:2347:stuckMemoryInitialization   BtreePA.java:2332:stuckMemoryInitialization   BtreePA.java:2541:editVariables   BtreePA.java:2535:editVariables   BtreePA.java:2505:<init>   BtreePA.java:3477:<init>   BtreePA.java:3476:test_verilog_find   BtreePA.java:3850:newTests   BtreePA.java:3857:main  */branch_3_StuckSA_Memory_Based_54_base_offset <= 0;branch_3_StuckSA_Copy_55 <= 0;branch_3_StuckSA_Transaction_56 <= 0; /*   BtreePA.java:2347:stuckMemoryInitialization   BtreePA.java:2332:stuckMemoryInitialization   BtreePA.java:2541:editVariables   BtreePA.java:2535:editVariables   BtreePA.java:2505:<init>   BtreePA.java:3477:<init>   BtreePA.java:3476:test_verilog_find   BtreePA.java:3850:newTests   BtreePA.java:3857:main  */leaf_0_StuckSA_Memory_Based_57_base_offset <= 0;leaf_0_StuckSA_Copy_58 <= 0;leaf_0_StuckSA_Transaction_59 <= 0; /*   BtreePA.java:2347:stuckMemoryInitialization   BtreePA.java:2333:stuckMemoryInitialization   BtreePA.java:2541:editVariables   BtreePA.java:2535:editVariables   BtreePA.java:2505:<init>   BtreePA.java:3477:<init>   BtreePA.java:3476:test_verilog_find   BtreePA.java:3850:newTests   BtreePA.java:3857:main  */leaf_1_StuckSA_Memory_Based_60_base_offset <= 0;leaf_1_StuckSA_Copy_61 <= 0;leaf_1_StuckSA_Transaction_62 <= 0; /*   BtreePA.java:2347:stuckMemoryInitialization   BtreePA.java:2333:stuckMemoryInitialization   BtreePA.java:2541:editVariables   BtreePA.java:2535:editVariables   BtreePA.java:2505:<init>   BtreePA.java:3477:<init>   BtreePA.java:3476:test_verilog_find   BtreePA.java:3850:newTests   BtreePA.java:3857:main  */leaf_2_StuckSA_Memory_Based_63_base_offset <= 0;leaf_2_StuckSA_Copy_64 <= 0;leaf_2_StuckSA_Transaction_65 <= 0; /*   BtreePA.java:2347:stuckMemoryInitialization   BtreePA.java:2333:stuckMemoryInitialization   BtreePA.java:2541:editVariables   BtreePA.java:2535:editVariables   BtreePA.java:2505:<init>   BtreePA.java:3477:<init>   BtreePA.java:3476:test_verilog_find   BtreePA.java:3850:newTests   BtreePA.java:3857:main  */leaf_3_StuckSA_Memory_Based_66_base_offset <= 0;leaf_3_StuckSA_Copy_67 <= 0;leaf_3_StuckSA_Transaction_68 <= 0; /*   BtreePA.java:2347:stuckMemoryInitialization   BtreePA.java:2333:stuckMemoryInitialization   BtreePA.java:2541:editVariables   BtreePA.java:2535:editVariables   BtreePA.java:2505:<init>   BtreePA.java:3477:<init>   BtreePA.java:3476:test_verilog_find   BtreePA.java:3850:newTests   BtreePA.java:3857:main  */
    end
    else begin                                                                  // Run
      $display            ("%4d  %4d  %b", steps, step, M_43);                    // Trace execution
      $fdisplay(traceFile, "%4d  %4d  %b", steps, step, M_43);                    // Trace execution in a file
      case(step)                                                                // Case statements to select the code for the current instruction
              0 : begin T_44[     137/*mergeable       */ +: 1] <= M_43[       5/*isLeaf  */ + 0 * 62 +: 1]; /*   MemoryLayoutPA.java:0382:<init>   MemoryLayoutPA.java:0381:move   BtreePA.java:0601:isRootLeaf   BtreePA.java:1952:code   ProgramPA.java:0270:<init>   BtreePA.java:1949:<init>   BtreePA.java:1948:find   BtreePA.java:3474:test_verilog_find   BtreePA.java:3850:newTests   BtreePA.java:3857:main  */ end
              1 : begin if (T_44[     137/*mergeable       */ +: 1] == 0) step = 5; /*   ProgramPA.java:0221:<init>   ProgramPA.java:0220:GoOff   ProgramPA.java:0238:<init>   BtreePA.java:1954:<init>   BtreePA.java:1953:code   ProgramPA.java:0270:<init>   BtreePA.java:1949:<init>   BtreePA.java:1948:find   BtreePA.java:3474:test_verilog_find   BtreePA.java:3850:newTests   BtreePA.java:3857:main  */ end
              2 : begin
                    T_44[      21/*search  */ +: 8] <= T_44[     163/*Key     */ +: 8]; /*   MemoryLayoutPA.java:0382:<init>   MemoryLayoutPA.java:0381:move   BtreePA.java:0260:tt   BtreePA.java:1955:Then   ProgramPA.java:0239:<init>   BtreePA.java:1954:<init>   BtreePA.java:1953:code   ProgramPA.java:0270:<init>   BtreePA.java:1949:<init>   BtreePA.java:1948:find   BtreePA.java:3474:test_verilog_find   BtreePA.java:3850:newTests   BtreePA.java:3857:main  */
                    T_44[     250/*node_balance    */ +: 5] <= 0; /*   MemoryLayoutPA.java:0562:<init>   MemoryLayoutPA.java:0561:zero   BtreePA.java:1956:Then   ProgramPA.java:0239:<init>   BtreePA.java:1954:<init>   BtreePA.java:1953:code   ProgramPA.java:0270:<init>   BtreePA.java:1949:<init>   BtreePA.java:1948:find   BtreePA.java:3474:test_verilog_find   BtreePA.java:3850:newTests   BtreePA.java:3857:main  */
                  end
              3 : begin leaf_0_StuckSA_Memory_Based_57_base_offset <=       11/*leaf    */ + T_44[     250/*node_balance    */ +: 5] * 62; /*   BtreePA.java:0668:<init>   BtreePA.java:0667:leafBase   BtreePA.java:0819:findEqualInLeaf   BtreePA.java:1959:Then   ProgramPA.java:0239:<init>   BtreePA.java:1954:<init>   BtreePA.java:1953:code   ProgramPA.java:0270:<init>   BtreePA.java:1949:<init>   BtreePA.java:1948:find   BtreePA.java:3474:test_verilog_find   BtreePA.java:3850:newTests   BtreePA.java:3857:main  */ end
              4 : begin T_44[      29/*found   */ +: 1]= ( 0
 || (M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            4/*key     */ + 0 * 8 +: 8] == T_44[     163/*Key     */ +: 8] &&  0 < M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            0/*currentSize     */ +: 4])
 || (M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            4/*key     */ + 1 * 8 +: 8] == T_44[     163/*Key     */ +: 8] &&  1 < M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            0/*currentSize     */ +: 4])
) ? 1 : 0;
T_44[     110/*index   */ +: 4] =
(M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            4/*key     */ + 0 * 8 +: 8] == T_44[     163/*Key     */ +: 8] && 0 < M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            0/*currentSize     */ +: 4]) ? 0 :
(M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            4/*key     */ + 1 * 8 +: 8] == T_44[     163/*Key     */ +: 8] && 1 < M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            0/*currentSize     */ +: 4]) ? 1 :
0;
T_44[      38/*data    */ +: 8] =
(M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            4/*key     */ + 0 * 8 +: 8] == T_44[     163/*Key     */ +: 8] && 0 < M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            0/*currentSize     */ +: 4]) ? M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+           20/*data    */ + 0 * 8 +: 8] :
(M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            4/*key     */ + 1 * 8 +: 8] == T_44[     163/*Key     */ +: 8] && 1 < M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            0/*currentSize     */ +: 4]) ? M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+           20/*data    */ + 1 * 8 +: 8] :
0;
 /*   StuckPA.java:0526:<init>   StuckPA.java:0525:search   BtreePA.java:0823:findEqualInLeaf   BtreePA.java:1959:Then   ProgramPA.java:0239:<init>   BtreePA.java:1954:<init>   BtreePA.java:1953:code   ProgramPA.java:0270:<init>   BtreePA.java:1949:<init>   BtreePA.java:1948:find   BtreePA.java:3474:test_verilog_find   BtreePA.java:3850:newTests   BtreePA.java:3857:main  */ end
              5 : begin
                    T_44[     179/*find    */ +: 5] <= 0; /*   MemoryLayoutPA.java:0562:<init>   MemoryLayoutPA.java:0561:zero   BtreePA.java:1961:Then   ProgramPA.java:0239:<init>   BtreePA.java:1954:<init>   BtreePA.java:1953:code   ProgramPA.java:0270:<init>   BtreePA.java:1949:<init>   BtreePA.java:1948:find   BtreePA.java:3474:test_verilog_find   BtreePA.java:3850:newTests   BtreePA.java:3857:main  */
                    step = 15; /*   ProgramPA.java:0201:<init>   ProgramPA.java:0200:Goto   BtreePA.java:1962:Then   ProgramPA.java:0239:<init>   BtreePA.java:1954:<init>   BtreePA.java:1953:code   ProgramPA.java:0270:<init>   BtreePA.java:1949:<init>   BtreePA.java:1948:find   BtreePA.java:3474:test_verilog_find   BtreePA.java:3850:newTests   BtreePA.java:3857:main  */
                  end
              6 : begin T_44[     189/*parent  */ +: 5] <= 0; /*   MemoryLayoutPA.java:0562:<init>   MemoryLayoutPA.java:0561:zero   BtreePA.java:1966:code   ProgramPA.java:0270:<init>   BtreePA.java:1949:<init>   BtreePA.java:1948:find   BtreePA.java:3474:test_verilog_find   BtreePA.java:3850:newTests   BtreePA.java:3857:main  */ end
              7 : begin branch_0_StuckSA_Memory_Based_45_base_offset <=       11/*branch  */ + T_44[     189/*parent  */ +: 5] * 62; /*   BtreePA.java:0693:<init>   BtreePA.java:0692:branchBase   BtreePA.java:0877:findFirstGreaterThanOrEqualInBranch   BtreePA.java:1980:code   ProgramPA.java:0270:<init>   BtreePA.java:1971:<init>   BtreePA.java:1970:code   ProgramPA.java:0270:<init>   BtreePA.java:1949:<init>   BtreePA.java:1948:find   BtreePA.java:3474:test_verilog_find   BtreePA.java:3850:newTests   BtreePA.java:3857:main  */ end
              8 : begin T_44[     194/*child   */ +: 5] =
(M_43[branch_0_StuckSA_Memory_Based_45_base_offset+          4/*key     */ + 0 * 8 +: 8] >= T_44[     163/*Key     */ +: 8] && 0 < M_43[branch_0_StuckSA_Memory_Based_45_base_offset+          0/*currentSize     */ +: 4]-1) ? M_43[branch_0_StuckSA_Memory_Based_45_base_offset+         36/*data    */ + 0 * 5 +: 5] :
(M_43[branch_0_StuckSA_Memory_Based_45_base_offset+          4/*key     */ + 1 * 8 +: 8] >= T_44[     163/*Key     */ +: 8] && 1 < M_43[branch_0_StuckSA_Memory_Based_45_base_offset+          0/*currentSize     */ +: 4]-1) ? M_43[branch_0_StuckSA_Memory_Based_45_base_offset+         36/*data    */ + 1 * 5 +: 5] :
(M_43[branch_0_StuckSA_Memory_Based_45_base_offset+          4/*key     */ + 2 * 8 +: 8] >= T_44[     163/*Key     */ +: 8] && 2 < M_43[branch_0_StuckSA_Memory_Based_45_base_offset+          0/*currentSize     */ +: 4]-1) ? M_43[branch_0_StuckSA_Memory_Based_45_base_offset+         36/*data    */ + 2 * 5 +: 5] :
(M_43[branch_0_StuckSA_Memory_Based_45_base_offset+          4/*key     */ + 3 * 8 +: 8] >= T_44[     163/*Key     */ +: 8] && 3 < M_43[branch_0_StuckSA_Memory_Based_45_base_offset+          0/*currentSize     */ +: 4]-1) ? M_43[branch_0_StuckSA_Memory_Based_45_base_offset+         36/*data    */ + 3 * 5 +: 5] :
M_43[branch_0_StuckSA_Memory_Based_45_base_offset+         36/*data    */ + M_43[branch_0_StuckSA_Memory_Based_45_base_offset+          0/*currentSize     */ +: 4] * 5-5 +: 5];
 /*   StuckPA.java:0622:<init>   StuckPA.java:0621:searchFirstGreaterThanOrEqual   BtreePA.java:0878:findFirstGreaterThanOrEqualInBranch   BtreePA.java:1980:code   ProgramPA.java:0270:<init>   BtreePA.java:1971:<init>   BtreePA.java:1970:code   ProgramPA.java:0270:<init>   BtreePA.java:1949:<init>   BtreePA.java:1948:find   BtreePA.java:3474:test_verilog_find   BtreePA.java:3850:newTests   BtreePA.java:3857:main  */ end
              9 : begin T_44[     137/*mergeable       */ +: 1] <= M_43[       5/*isLeaf  */ + T_44[     194/*child   */ +: 5] * 62 +: 1]; /*   MemoryLayoutPA.java:0382:<init>   MemoryLayoutPA.java:0381:move   BtreePA.java:0607:isLeaf   BtreePA.java:1987:code   ProgramPA.java:0270:<init>   BtreePA.java:1971:<init>   BtreePA.java:1970:code   ProgramPA.java:0270:<init>   BtreePA.java:1949:<init>   BtreePA.java:1948:find   BtreePA.java:3474:test_verilog_find   BtreePA.java:3850:newTests   BtreePA.java:3857:main  */ end
             10 : begin if (T_44[     137/*mergeable       */ +: 1] == 0) step = 14; /*   ProgramPA.java:0221:<init>   ProgramPA.java:0220:GoOff   ProgramPA.java:0238:<init>   BtreePA.java:1989:<init>   BtreePA.java:1988:code   ProgramPA.java:0270:<init>   BtreePA.java:1971:<init>   BtreePA.java:1970:code   ProgramPA.java:0270:<init>   BtreePA.java:1949:<init>   BtreePA.java:1948:find   BtreePA.java:3474:test_verilog_find   BtreePA.java:3850:newTests   BtreePA.java:3857:main  */ end
             11 : begin
                    T_44[      21/*search  */ +: 8] <= T_44[     163/*Key     */ +: 8]; /*   MemoryLayoutPA.java:0382:<init>   MemoryLayoutPA.java:0381:move   BtreePA.java:0260:tt   BtreePA.java:1990:Then   ProgramPA.java:0239:<init>   BtreePA.java:1989:<init>   BtreePA.java:1988:code   ProgramPA.java:0270:<init>   BtreePA.java:1971:<init>   BtreePA.java:1970:code   ProgramPA.java:0270:<init>   BtreePA.java:1949:<init>   BtreePA.java:1948:find   BtreePA.java:3474:test_verilog_find   BtreePA.java:3850:newTests   BtreePA.java:3857:main  */
                    T_44[     250/*node_balance    */ +: 5] <= T_44[     194/*child   */ +: 5]; /*   MemoryLayoutPA.java:0382:<init>   MemoryLayoutPA.java:0381:move   BtreePA.java:0260:tt   BtreePA.java:1991:Then   ProgramPA.java:0239:<init>   BtreePA.java:1989:<init>   BtreePA.java:1988:code   ProgramPA.java:0270:<init>   BtreePA.java:1971:<init>   BtreePA.java:1970:code   ProgramPA.java:0270:<init>   BtreePA.java:1949:<init>   BtreePA.java:1948:find   BtreePA.java:3474:test_verilog_find   BtreePA.java:3850:newTests   BtreePA.java:3857:main  */
                  end
             12 : begin leaf_0_StuckSA_Memory_Based_57_base_offset <=       11/*leaf    */ + T_44[     250/*node_balance    */ +: 5] * 62; /*   BtreePA.java:0668:<init>   BtreePA.java:0667:leafBase   BtreePA.java:0819:findEqualInLeaf   BtreePA.java:1994:Then   ProgramPA.java:0239:<init>   BtreePA.java:1989:<init>   BtreePA.java:1988:code   ProgramPA.java:0270:<init>   BtreePA.java:1971:<init>   BtreePA.java:1970:code   ProgramPA.java:0270:<init>   BtreePA.java:1949:<init>   BtreePA.java:1948:find   BtreePA.java:3474:test_verilog_find   BtreePA.java:3850:newTests   BtreePA.java:3857:main  */ end
             13 : begin T_44[      29/*found   */ +: 1]= ( 0
 || (M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            4/*key     */ + 0 * 8 +: 8] == T_44[     163/*Key     */ +: 8] &&  0 < M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            0/*currentSize     */ +: 4])
 || (M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            4/*key     */ + 1 * 8 +: 8] == T_44[     163/*Key     */ +: 8] &&  1 < M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            0/*currentSize     */ +: 4])
) ? 1 : 0;
T_44[     110/*index   */ +: 4] =
(M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            4/*key     */ + 0 * 8 +: 8] == T_44[     163/*Key     */ +: 8] && 0 < M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            0/*currentSize     */ +: 4]) ? 0 :
(M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            4/*key     */ + 1 * 8 +: 8] == T_44[     163/*Key     */ +: 8] && 1 < M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            0/*currentSize     */ +: 4]) ? 1 :
0;
T_44[      38/*data    */ +: 8] =
(M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            4/*key     */ + 0 * 8 +: 8] == T_44[     163/*Key     */ +: 8] && 0 < M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            0/*currentSize     */ +: 4]) ? M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+           20/*data    */ + 0 * 8 +: 8] :
(M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            4/*key     */ + 1 * 8 +: 8] == T_44[     163/*Key     */ +: 8] && 1 < M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+            0/*currentSize     */ +: 4]) ? M_43[leaf_0_StuckSA_Memory_Based_57_base_offset+           20/*data    */ + 1 * 8 +: 8] :
0;
 /*   StuckPA.java:0526:<init>   StuckPA.java:0525:search   BtreePA.java:0823:findEqualInLeaf   BtreePA.java:1994:Then   ProgramPA.java:0239:<init>   BtreePA.java:1989:<init>   BtreePA.java:1988:code   ProgramPA.java:0270:<init>   BtreePA.java:1971:<init>   BtreePA.java:1970:code   ProgramPA.java:0270:<init>   BtreePA.java:1949:<init>   BtreePA.java:1948:find   BtreePA.java:3474:test_verilog_find   BtreePA.java:3850:newTests   BtreePA.java:3857:main  */ end
             14 : begin
                    T_44[     179/*find    */ +: 5] <= T_44[     194/*child   */ +: 5]; /*   MemoryLayoutPA.java:0382:<init>   MemoryLayoutPA.java:0381:move   BtreePA.java:0260:tt   BtreePA.java:1995:Then   ProgramPA.java:0239:<init>   BtreePA.java:1989:<init>   BtreePA.java:1988:code   ProgramPA.java:0270:<init>   BtreePA.java:1971:<init>   BtreePA.java:1970:code   ProgramPA.java:0270:<init>   BtreePA.java:1949:<init>   BtreePA.java:1948:find   BtreePA.java:3474:test_verilog_find   BtreePA.java:3850:newTests   BtreePA.java:3857:main  */
                    step = 15; /*   ProgramPA.java:0201:<init>   ProgramPA.java:0200:Goto   BtreePA.java:1996:Then   ProgramPA.java:0239:<init>   BtreePA.java:1989:<init>   BtreePA.java:1988:code   ProgramPA.java:0270:<init>   BtreePA.java:1971:<init>   BtreePA.java:1970:code   ProgramPA.java:0270:<init>   BtreePA.java:1949:<init>   BtreePA.java:1948:find   BtreePA.java:3474:test_verilog_find   BtreePA.java:3850:newTests   BtreePA.java:3857:main  */
                  end
             15 : begin
                    T_44[     189/*parent  */ +: 5] <= T_44[     194/*child   */ +: 5]; /*   MemoryLayoutPA.java:0382:<init>   MemoryLayoutPA.java:0381:move   BtreePA.java:0260:tt   BtreePA.java:2002:code   ProgramPA.java:0270:<init>   BtreePA.java:1971:<init>   BtreePA.java:1970:code   ProgramPA.java:0270:<init>   BtreePA.java:1949:<init>   BtreePA.java:1948:find   BtreePA.java:3474:test_verilog_find   BtreePA.java:3850:newTests   BtreePA.java:3857:main  */
                    step = 6; /*   ProgramPA.java:0201:<init>   ProgramPA.java:0200:Goto   BtreePA.java:2004:code   ProgramPA.java:0270:<init>   BtreePA.java:1971:<init>   BtreePA.java:1970:code   ProgramPA.java:0270:<init>   BtreePA.java:1949:<init>   BtreePA.java:1948:find   BtreePA.java:3474:test_verilog_find   BtreePA.java:3850:newTests   BtreePA.java:3857:main  */
                  end
        default : begin stopped <= 1; /* end of execution */ end
      endcase
      step   = step  + 1;
      steps <= steps + 1;
    end // Execute
  end // Always
endmodule
