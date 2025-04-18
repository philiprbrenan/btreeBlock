//-----------------------------------------------------------------------------
// Generic cpu
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2025-01-07
//------------------------------------------------------------------------------
`timescale 10ps/1ps
(* keep_hierarchy = "yes" *)
module find(reset, stop, clock, Key, Data, data, found);                    // Database on a chip
  input                      reset;                                             // Restart the program run sequence when this goes high
  input                      clock;                                             // Program counter clock
  input [16-1:0]  Key;                                             // Input key
  input [16-1:0] Data;                                             // Input data
  output                      stop;                                             // Program has stopped when this goes high
  output[16-1:0] data;                                             // Output data
  output                     found;                                             // Whether the key was found on put, find delete

  integer step;
  integer steps;
  integer stopped;
  integer intermediateValue;
  reg [16-1:0] memory [241-1: 0]; /* declareVerilogMemory */
  reg [6-1: 0] opCodes[126 : 0];

  assign stop  = stopped > 0 ? 1 : 0;
  assign found = memory[58];  // found
  assign data  = memory[57];   // data

  always @ (posedge clock) begin                                                // Execute next step in program
    if (reset) begin                                                            // Reset
      step     <= 0;
      steps    <= 0;
      stopped  <= 0;
      begin /* initilizeVerilogMemory */
        integer i;
        for (i = 0; i < 241; i = i + 1) begin
          memory[i] <= 0;
        end
      end
      memory[   3] <=    2; /* branchSize */
      memory[   4] <=    2; /* child */
      memory[   5] <=    2; /* current_size[0] */
      memory[   6] <=    2; /* current_size[1] */
      memory[   7] <=    2; /* current_size[2] */
      memory[   8] <=    2; /* current_size[3] */
      memory[   9] <=    2; /* current_size[4] */
      memory[  10] <=    2; /* current_size[5] */
      memory[  11] <=    3; /* current_size[6] */
      memory[  12] <=    1; /* current_size[7] */
      memory[  15] <=    5; /* data[0,0] */
      memory[  16] <=    6; /* data[0,1] */
      memory[  17] <=    2; /* data[0,2] */
      memory[  18] <=    2; /* data[0,3] */
      memory[  19] <=    8; /* data[1,0] */
      memory[  20] <=    7; /* data[1,1] */
      memory[  23] <=    1; /* data[2,0] */
      memory[  27] <=    6; /* data[3,0] */
      memory[  28] <=    5; /* data[3,1] */
      memory[  31] <=    4; /* data[4,0] */
      memory[  32] <=    3; /* data[4,1] */
      memory[  35] <=    1; /* data[5,0] */
      memory[  36] <=    3; /* data[5,1] */
      memory[  39] <=    4; /* data[6,0] */
      memory[  40] <=    7; /* data[6,1] */
      memory[  41] <=    2; /* data[6,2] */
      memory[  43] <=    2; /* data[7,0] */
      memory[  57] <=    2; /* f_data */
      memory[  61] <=    9; /* findAndInsert_Key */
      memory[  62] <=    1; /* f_index */
      memory[  63] <=    9; /* find_Key */
      memory[  64] <=    1; /* find_loop */
      memory[  65] <=    2; /* find_result_leaf */
      memory[  67] <=    9; /* f_key */
      memory[  68] <=    2; /* f_leaf */
      memory[  69] <=    8; /* freeChainHead */
      memory[  78] <=    9; /* free[8] */
      memory[  80] <=    1; /* f_success */
      memory[  81] <=    1; /* hasLeavesForChildren */
      memory[  82] <=    1; /* isALeaf */
      memory[  86] <=    1; /* isLeaf[1] */
      memory[  87] <=    1; /* isLeaf[2] */
      memory[  88] <=    1; /* isLeaf[3] */
      memory[  89] <=    1; /* isLeaf[4] */
      memory[  92] <=    1; /* isLeaf[7] */
      memory[  96] <=    4; /* keys[0,0] */
      memory[ 100] <=    1; /* keys[1,0] */
      memory[ 101] <=    2; /* keys[1,1] */
      memory[ 104] <=    8; /* keys[2,0] */
      memory[ 105] <=    9; /* keys[2,1] */
      memory[ 108] <=    3; /* keys[3,0] */
      memory[ 109] <=    4; /* keys[3,1] */
      memory[ 112] <=    5; /* keys[4,0] */
      memory[ 113] <=    6; /* keys[4,1] */
      memory[ 116] <=    2; /* keys[5,0] */
      memory[ 120] <=    6; /* keys[6,0] */
      memory[ 121] <=    7; /* keys[6,1] */
      memory[ 124] <=    7; /* keys[7,0] */
      memory[ 136] <=    2; /* leafSize */
      memory[ 137] <=    3; /* merge_indexLimit */
      memory[ 138] <=    3; /* merge_indices */
      memory[ 139] <=    9; /* merge_Key */
      memory[ 140] <=    2; /* mergeLeftSibling_bs */
      memory[ 141] <=    2; /* mergeLeftSibling_index */
      memory[ 142] <=    4; /* mergeLeftSibling_l */
      memory[ 143] <=    2; /* mergeLeftSibling_nl */
      memory[ 144] <=    3; /* mergeLeftSibling_nlr */
      memory[ 145] <=    1; /* mergeLeftSibling_nr */
      memory[ 146] <=    6; /* mergeLeftSibling_parent */
      memory[ 147] <=    7; /* mergeLeftSibling_r */
      memory[ 151] <=    2; /* merge_loop */
      memory[ 152] <=    2; /* mergeRightSibling_bs */
      memory[ 154] <=    2; /* mergeRightSibling_index */
      memory[ 156] <=    7; /* mergeRightSibling_l */
      memory[ 157] <=    1; /* mergeRightSibling_nl */
      memory[ 158] <=    3; /* mergeRightSibling_nlr */
      memory[ 159] <=    2; /* mergeRightSibling_nr */
      memory[ 160] <=    6; /* mergeRightSibling_parent */
      memory[ 161] <=    6; /* mergeRightSibling_pk */
      memory[ 162] <=    2; /* mergeRightSibling_r */
      memory[ 163] <=    1; /* mergeRightSibling_size */
      memory[ 166] <=    5; /* mergeRoot_l */
      memory[ 167] <=    1; /* mergeRoot_nl */
      memory[ 168] <=    4; /* mergeRoot_nlr */
      memory[ 169] <=    1; /* mergeRoot_nP */
      memory[ 170] <=    2; /* mergeRoot_nr */
      memory[ 172] <=    6; /* mergeRoot_r */
      memory[ 173] <=    2; /* parent */
      memory[ 175] <=    9; /* put_Key */
      memory[ 176] <=    1; /* put_loop */
      memory[ 179] <=    2; /* s_data */
      memory[ 180] <=    6; /* setBranch */
      memory[ 181] <=    7; /* setLeaf */
      memory[ 183] <=    2; /* s_index */
      memory[ 190] <=    5; /* splitBranchRoot_l */
      memory[ 191] <=    4; /* splitBranchRoot_plk */
      memory[ 192] <=    6; /* splitBranchRoot_r */
      memory[ 193] <=    8; /* splitLeaf_F */
      memory[ 194] <=    7; /* splitLeaf_fl */
      memory[ 195] <=    1; /* splitLeaf_index */
      memory[ 196] <=    7; /* splitLeaf_L */
      memory[ 197] <=    7; /* splitLeaf_l */
      memory[ 198] <=    2; /* splitLeaf_node */
      memory[ 199] <=    6; /* splitLeaf_parent */
      memory[ 200] <=    2; /* splitLeafRoot_first */
      memory[ 202] <=    1; /* splitLeafRoot_kv */
      memory[ 203] <=    1; /* splitLeafRoot_last */
      memory[ 204] <=    1; /* splitLeafRoot_l */
      memory[ 205] <=    2; /* splitLeafRoot_r */
      memory[ 229] <=    1; /* stuckInsertElementAt_i */
      memory[ 230] <=    1; /* stuckInsertElementAt_L */
      memory[ 231] <=    3; /* stuckRemoveElementAt_I */
      memory[ 232] <=    2; /* stuckRemoveElementAt_i */
      memory[ 233] <=    3; /* stuckRemoveElementAt_N */
      memory[ 234] <=    2; /* stuckSearch_N */
      memory[ 235] <=    1; /* stuckShift_i */
      memory[ 236] <=    2; /* stuckShift_I */
      memory[ 237] <=    2; /* stuckShift_N */
      memory[ 238] <=    6; /* stuck */

      opCodes[0] <= 0;
      opCodes[1] <= 1;
      opCodes[2] <= 2;
      opCodes[3] <= 3;
      opCodes[4] <= 4;
      opCodes[5] <= 5;
      opCodes[43] <= 5;
      opCodes[82] <= 5;
      opCodes[6] <= 6;
      opCodes[17] <= 6;
      opCodes[44] <= 6;
      opCodes[58] <= 6;
      opCodes[69] <= 6;
      opCodes[83] <= 6;
      opCodes[94] <= 6;
      opCodes[7] <= 7;
      opCodes[45] <= 7;
      opCodes[84] <= 7;
      opCodes[8] <= 8;
      opCodes[46] <= 8;
      opCodes[85] <= 8;
      opCodes[9] <= 9;
      opCodes[47] <= 9;
      opCodes[86] <= 9;
      opCodes[10] <= 10;
      opCodes[48] <= 10;
      opCodes[51] <= 10;
      opCodes[87] <= 10;
      opCodes[11] <= 11;
      opCodes[24] <= 11;
      opCodes[52] <= 11;
      opCodes[65] <= 11;
      opCodes[88] <= 11;
      opCodes[101] <= 11;
      opCodes[12] <= 12;
      opCodes[25] <= 12;
      opCodes[13] <= 13;
      opCodes[54] <= 13;
      opCodes[90] <= 13;
      opCodes[14] <= 14;
      opCodes[15] <= 15;
      opCodes[56] <= 15;
      opCodes[92] <= 15;
      opCodes[16] <= 16;
      opCodes[57] <= 16;
      opCodes[68] <= 16;
      opCodes[93] <= 16;
      opCodes[18] <= 17;
      opCodes[59] <= 17;
      opCodes[70] <= 17;
      opCodes[95] <= 17;
      opCodes[19] <= 18;
      opCodes[60] <= 18;
      opCodes[71] <= 18;
      opCodes[96] <= 18;
      opCodes[20] <= 19;
      opCodes[21] <= 20;
      opCodes[31] <= 20;
      opCodes[62] <= 20;
      opCodes[98] <= 20;
      opCodes[110] <= 20;
      opCodes[22] <= 21;
      opCodes[63] <= 21;
      opCodes[99] <= 21;
      opCodes[120] <= 21;
      opCodes[23] <= 22;
      opCodes[64] <= 22;
      opCodes[100] <= 22;
      opCodes[26] <= 23;
      opCodes[27] <= 24;
      opCodes[38] <= 24;
      opCodes[28] <= 25;
      opCodes[107] <= 25;
      opCodes[29] <= 26;
      opCodes[108] <= 26;
      opCodes[30] <= 27;
      opCodes[109] <= 27;
      opCodes[32] <= 28;
      opCodes[111] <= 28;
      opCodes[33] <= 29;
      opCodes[112] <= 29;
      opCodes[34] <= 30;
      opCodes[113] <= 30;
      opCodes[35] <= 31;
      opCodes[72] <= 31;
      opCodes[114] <= 31;
      opCodes[36] <= 32;
      opCodes[115] <= 32;
      opCodes[37] <= 33;
      opCodes[116] <= 33;
      opCodes[39] <= 34;
      opCodes[118] <= 34;
      opCodes[40] <= 35;
      opCodes[41] <= 36;
      opCodes[42] <= 37;
      opCodes[81] <= 37;
      opCodes[49] <= 38;
      opCodes[50] <= 39;
      opCodes[53] <= 40;
      opCodes[66] <= 40;
      opCodes[55] <= 41;
      opCodes[61] <= 42;
      opCodes[67] <= 43;
      opCodes[73] <= 44;
      opCodes[74] <= 45;
      opCodes[80] <= 45;
      opCodes[104] <= 45;
      opCodes[117] <= 45;
      opCodes[75] <= 46;
      opCodes[77] <= 46;
      opCodes[76] <= 47;
      opCodes[78] <= 48;
      opCodes[79] <= 49;
      opCodes[89] <= 50;
      opCodes[102] <= 50;
      opCodes[91] <= 51;
      opCodes[97] <= 52;
      opCodes[103] <= 53;
      opCodes[105] <= 54;
      opCodes[106] <= 55;
      opCodes[119] <= 56;
      opCodes[122] <= 56;
      opCodes[121] <= 57;
      opCodes[123] <= 58;
      opCodes[124] <= 59;
      opCodes[125] <= 60;
      opCodes[126] <= 61;

      memory[63] <= 4; /* find key */
      
    end
    else begin                                                                  // Run
      //$display("%4d %4d %4d s=%4d f=%4d d=%4d", steps, step, intermediateValue, stop, found, data);
      case(opCodes[step])
           0 : begin intermediateValue <= memory[85+memory[177]/*root*/]/*isLeaf[root]*/; /* get 2 */ step <= step + 1; end
           1 : begin memory[178]/*rootIsLeaf*/ <= intermediateValue; /* set 1 */ step <= step + 1; end
           2 : begin intermediateValue <= memory[178]/*rootIsLeaf*/; /* get 1 */ step <= step + 1; end
           3 : begin if (intermediateValue == 0) step <=   38; else step = step + 1;/* endIfEq*/    end
           4 : begin memory[238]/*stuck*/ <= 0; /* set 1 */ step <= step + 1; end
           5 : begin intermediateValue <= memory[63]/*find_Key*/; /* get 1 */ step <= step + 1; end
           6 : begin memory[184]/*s_key*/ <= intermediateValue; /* set 1 */ step <= step + 1; end
           7 : begin memory[182]/*s_found*/ <= 0; /* set 1 */ step <= step + 1; end
           8 : begin memory[183]/*s_index*/ <= 0; /* set 1 */ step <= step + 1; end
           9 : begin intermediateValue <= memory[5+memory[238]/*stuck*/]/*current_size[stuck]*/; /* get 2 */ step <= step + 1; end
          10 : begin memory[234]/*stuckSearch_N*/ <= intermediateValue; /* set 1 */ step <= step + 1; end
          11 : begin intermediateValue <= memory[183]/*s_index*/ < memory[234]/*stuckSearch_N*/ ? -1 : memory[183]/*s_index*/ == memory[234]/*stuckSearch_N*/ ?  0 : +1; /* compare 2 */ step <= step + 1; end
          12 : begin if (intermediateValue >= 0) step <=   27; else step = step + 1;/* endIfGe*/    end
          13 : begin intermediateValue <= memory[184]/*s_key*/ < memory[96+memory[238]/*stuck*/*4+memory[183]/*s_index*/]/*keys[s_index,stuck]*/ ? -1 : memory[184]/*s_key*/ == memory[96+memory[238]/*stuck*/*4+memory[183]/*s_index*/]/*keys[s_index,stuck]*/ ?  0 : +1; /* compare 2 */ step <= step + 1; end
          14 : begin if (intermediateValue != 0) step <=   21; else step = step + 1;/* endIfNe*/    end
          15 : begin memory[182]/*s_found*/ <= 1; /* set 1 */ step <= step + 1; end
          16 : begin intermediateValue <= memory[96+memory[238]/*stuck*/*4+memory[183]/*s_index*/]/*keys[s_index,stuck]*/; /* get 3 */ step <= step + 1; end
          17 : begin intermediateValue <= memory[15+memory[238]/*stuck*/*4+memory[183]/*s_index*/]/*data[s_index,stuck]*/; /* get 3 */ step <= step + 1; end
          18 : begin memory[179]/*s_data*/ <= intermediateValue; /* set 1 */ step <= step + 1; end
          19 : begin step <=   27; /* end */ end
          20 : begin intermediateValue <= memory[183]/*s_index*/; /* get 1 */ step <= step + 1; end
          21 : begin intermediateValue <= 1 + intermediateValue;  /* add 1 */ step <= step + 1; end
          22 : begin memory[183]/*s_index*/ <= intermediateValue; /* set 1 */ step <= step + 1; end
          23 : begin step <= 11; /* start */ end
          24 : begin intermediateValue <= memory[177]/*root*/; /* get 1 */ step <= step + 1; end
          25 : begin memory[68]/*f_leaf*/ <= intermediateValue; /* set 1 */ step <= step + 1; end
          26 : begin intermediateValue <= memory[182]/*s_found*/; /* get 1 */ step <= step + 1; end
          27 : begin memory[58]/*f_found*/ <= intermediateValue; /* set 1 */ step <= step + 1; end
          28 : begin memory[62]/*f_index*/ <= intermediateValue; /* set 1 */ step <= step + 1; end
          29 : begin intermediateValue <= memory[184]/*s_key*/; /* get 1 */ step <= step + 1; end
          30 : begin memory[67]/*f_key*/ <= intermediateValue; /* set 1 */ step <= step + 1; end
          31 : begin intermediateValue <= memory[179]/*s_data*/; /* get 1 */ step <= step + 1; end
          32 : begin memory[57]/*f_data*/ <= intermediateValue; /* set 1 */ step <= step + 1; end
          33 : begin step <=   127; /* end */ end
          34 : begin memory[173]/*parent*/ <= intermediateValue; /* set 1 */ step <= step + 1; end
          35 : begin memory[64] <= 0; /* clear 1 */ step <= step + 1; end
          36 : begin intermediateValue <= memory[173]/*parent*/; /* get 1 */ step <= step + 1; end
          37 : begin memory[238]/*stuck*/ <= intermediateValue; /* set 1 */ step <= step + 1; end
          38 : begin intermediateValue <= memory[234]/*stuckSearch_N*/; /* get 1 */ step <= step + 1; end
          39 : begin intermediateValue <= -1 + intermediateValue;  /* add 1 */ step <= step + 1; end
          40 : begin if (intermediateValue >= 0) step <=   68; else step = step + 1;/* endIfGe*/    end
          41 : begin if (intermediateValue >  0) step <=   62; else step = step + 1;/* endIfGt*/    end
          42 : begin step <=   68; /* end */ end
          43 : begin step <= 52; /* start */ end
          44 : begin memory[4]/*child*/ <= intermediateValue; /* set 1 */ step <= step + 1; end
          45 : begin intermediateValue <= memory[4]/*child*/; /* get 1 */ step <= step + 1; end
          46 : begin memory[82]/*isALeaf*/ <= intermediateValue; /* set 1 */ step <= step + 1; end
          47 : begin intermediateValue <= memory[85+memory[82]/*isALeaf*/]/*isLeaf[isALeaf]*/; /* get 2 */ step <= step + 1; end
          48 : begin intermediateValue <= memory[82]/*isALeaf*/; /* get 1 */ step <= step + 1; end
          49 : begin if (intermediateValue == 0) step <=   117; else step = step + 1;/* endIfEq*/    end
          50 : begin if (intermediateValue >= 0) step <=   104; else step = step + 1;/* endIfGe*/    end
          51 : begin if (intermediateValue != 0) step <=   98; else step = step + 1;/* endIfNe*/    end
          52 : begin step <=   104; /* end */ end
          53 : begin step <= 88; /* start */ end
          54 : begin memory[65]/*find_result_leaf*/ <= intermediateValue; /* set 1 */ step <= step + 1; end
          55 : begin intermediateValue <= memory[65]/*find_result_leaf*/; /* get 1 */ step <= step + 1; end
          56 : begin intermediateValue <= memory[64]/*find_loop*/; /* get 1 */ step <= step + 1; end
          57 : begin memory[64]/*find_loop*/ <= intermediateValue; /* set 1 */ step <= step + 1; end
          58 : begin intermediateValue <= 9 <  intermediateValue ? -1 : 9 == intermediateValue ?  0 : +1; /* compare 1 */ step <= step + 1; end
          59 : begin if (intermediateValue >= 0) step <=   126; else step = step + 1;/* endIfGe*/    end
          60 : begin step <= 41; /* start */ end
          61 : begin stopped <= 1; end

        default: stopped <= 1;
      endcase
      steps    <= steps + 1;
    end // Execute
  end // Always
endmodule
