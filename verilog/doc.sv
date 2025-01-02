//-----------------------------------------------------------------------------
// Database on a chip
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2025
//------------------------------------------------------------------------------
`timescale 10ps/1ps
module doc(reset, stop, clock, pfd, Key, Data, data, found);                    // Database on a chip
  input                 reset;                                                  // Restart the program run sequence when this goes high
  input                 stop;                                                   // Program has stopped when this goes high
  input                 clock;                                                  // Program counter clock
  input            [2:0]pfd;                                                    // Put, find delete
  input [4 :0]Key;                                                    // Input key
  input [4:0]Data;                                                   // Input data
  output[4:0]data;                                                   // Output data
  output                found;                                                  // Whether the key was found on put, find delete

  integer step;                                                                 // Program counter

  `include "memory.sv"
  reg [7:0] T_base;
reg [253:0] T;


  always @ (posedge reset, posedge clock) begin                                 // Execute next step in program

    if (reset) begin;                                                           // Reset
      step <= 0;
      $display("reset");
    end

    else begin;                                                                 // Run
      step <= step + 1;
      case(pc)
     1 : begin T[164+3-1 +: 3] /* node_isLeaf */ <= 0; end
     2 : begin T[77+1-1 +: 1] /* IsLeaf */ <= M[3+1-1 +T[167-1+:3]*35 +: 1] /* isLeaf(node_isLeaf) */; end
     3 : begin if (T[77+1-1 +: 1] /* IsLeaf */ == 0) step = 46; end
     4 : begin T[14+4-1 +: 4] /* search */ <= T[114+4-1 +: 4] /* Key */; end
     5 : begin setIntInstruction end
     6 : begin T[173+3-1 +: 3] /* node_assertLeaf */ <= T[221+3-1 +: 3] /* node_findEqualInLeaf */; end
     7 : begin T[164+3-1 +: 3] /* node_isLeaf */ <= T[173+3-1 +: 3] /* node_assertLeaf */; end
     8 : begin T[77+1-1 +: 1] /* IsLeaf */ <= M[3+1-1 +T[167-1+:3]*35 +: 1] /* isLeaf(node_isLeaf) */; end
     9 : begin if (T[77+1-1 +: 1] /* IsLeaf */ == 0) step = 9; end
    10 : begin step = 10; end
    11 : begin Halt end
    12 : begin T[194+3-1 +: 3] /* node_leafBase */ <= T[221+3-1 +: 3] /* node_findEqualInLeaf */; end
    13 : begin leafBase end
    14 : begin  end
    15 : begin leaf_0_StuckSA_Transaction[0+4-1 +: 4] /* search */ <= T[14+4-1 +: 4] /* search */; end
    16 : begin setIntInstruction end
    17 : begin leaf_0_StuckSA_Transaction[21+3-1 +: 3] /* size */ <= leaf_0_StuckSA_Memory[0+3-1 +: 3] /* currentSize */; end
    18 : begin if (leaf_0_StuckSA_Transaction[4+3-1 +: 3] /* limit */ == 0) step = 19; end
    19 : begin leaf_0_StuckSA_Transaction[21+3-1 +: 3] /* size */ <= leaf_0_StuckSA_Transaction[21+3-1 +: 3] /* size */- 1; end
    20 : begin step = 19; end
    21 : begin setIntInstruction end
    22 : begin setIntInstruction end
    23 : begin leaf_0_StuckSA_Transaction[27+1-1 +: 1] /* equal */ <= leaf_0_StuckSA_Transaction[10+3-1 +: 3] /* index */ == leaf_0_StuckSA_Transaction[21+3-1 +: 3] /* size */; end
    24 : begin if (leaf_0_StuckSA_Transaction[27+1-1 +: 1] /* equal */ > 0) step = 40; end
    25 : begin leaf_0_StuckSA_Transaction[13+4-1 +: 4] /* key */ <= leaf_0_StuckSA_Memory[3+4-1 +leaf_0_StuckSA_Transaction[13-1+:3]*4 +: 4] /* key(index) */; end
    26 : begin leaf_0_StuckSA_Transaction[27+1-1 +: 1] /* equal */ <= leaf_0_StuckSA_Transaction[13+4-1 +: 4] /* key */ == leaf_0_StuckSA_Transaction[0+4-1 +: 4] /* search */; end
    27 : begin if (leaf_0_StuckSA_Transaction[27+1-1 +: 1] /* equal */ == 0) step = 30; end
    28 : begin setIntInstruction end
    29 : begin leaf_0_StuckSA_Transaction[17+4-1 +: 4] /* data */ <= leaf_0_StuckSA_Memory[11+4-1 +leaf_0_StuckSA_Transaction[13-1+:3]*4 +: 4] /* data(index) */; end
    30 : begin step = 40; end
    31 : begin step = 30; end
    32 : begin setIntInstruction end
    33 : begin leaf_0_StuckSA_Transaction[27+1-1 +: 1] /* equal */ <= leaf_0_StuckSA_Transaction[10+3-1 +: 3] /* index */ == leaf_0_StuckSA_Transaction[21+3-1 +: 3] /* size */; end
    34 : begin if (leaf_0_StuckSA_Transaction[27+1-1 +: 1] /* equal */ > 0) step = 40; end
    35 : begin leaf_0_StuckSA_Transaction[13+4-1 +: 4] /* key */ <= leaf_0_StuckSA_Memory[3+4-1 +leaf_0_StuckSA_Transaction[13-1+:3]*4 +: 4] /* key(index) */; end
    36 : begin leaf_0_StuckSA_Transaction[27+1-1 +: 1] /* equal */ <= leaf_0_StuckSA_Transaction[13+4-1 +: 4] /* key */ == leaf_0_StuckSA_Transaction[0+4-1 +: 4] /* search */; end
    37 : begin if (leaf_0_StuckSA_Transaction[27+1-1 +: 1] /* equal */ == 0) step = 40; end
    38 : begin setIntInstruction end
    39 : begin leaf_0_StuckSA_Transaction[17+4-1 +: 4] /* data */ <= leaf_0_StuckSA_Memory[11+4-1 +leaf_0_StuckSA_Transaction[13-1+:3]*4 +: 4] /* data(index) */; end
    40 : begin step = 40; end
    41 : begin step = 40; end
    42 : begin T[18+1-1 +: 1] /* found */ <= leaf_0_StuckSA_Transaction[9+1-1 +: 1] /* found */; end
    43 : begin T[59+3-1 +: 3] /* index */ <= leaf_0_StuckSA_Transaction[10+3-1 +: 3] /* index */; end
    44 : begin T[23+4-1 +: 4] /* data */ <= leaf_0_StuckSA_Transaction[17+4-1 +: 4] /* data */; end
    45 : begin T[122+3-1 +: 3] /* find */ <= 0; end
    46 : begin step = 182; end
    47 : begin step = 46; end
    48 : begin T[128+3-1 +: 3] /* parent */ <= 0; end
    49 : begin T[149+3-1 +: 3] /* findDepth */ <= 0; end
    50 : begin T[149+3-1 +: 3] /* findDepth */ <= T[149+3-1 +: 3] /* findDepth */+ 1; end
    51 : begin T[83+1-1 +: 1] /* pastMaxDepth */ <= T[149+3-1 +: 3] /* findDepth */ > T[146+3-1 +: 3] /* maxDepth */; end
    52 : begin if (T[83+1-1 +: 1] /* pastMaxDepth */ > 0) step = 181; end
    53 : begin T[14+4-1 +: 4] /* search */ <= T[114+4-1 +: 4] /* Key */; end
    54 : begin T[227+3-1 +: 3] /* node_findFirstGreaterThanOrEqualInBranch */ <= T[128+3-1 +: 3] /* parent */; end
    55 : begin T[176+3-1 +: 3] /* node_assertBranch */ <= T[227+3-1 +: 3] /* node_findFirstGreaterThanOrEqualInBranch */; end
    56 : begin T[164+3-1 +: 3] /* node_isLeaf */ <= T[176+3-1 +: 3] /* node_assertBranch */; end
    57 : begin T[77+1-1 +: 1] /* IsLeaf */ <= M[3+1-1 +T[167-1+:3]*35 +: 1] /* isLeaf(node_isLeaf) */; end
    58 : begin if (T[77+1-1 +: 1] /* IsLeaf */ == 0) step = 59; end
    59 : begin Halt end
    60 : begin step = 59; end
    61 : begin T[197+3-1 +: 3] /* node_branchBase */ <= T[227+3-1 +: 3] /* node_findFirstGreaterThanOrEqualInBranch */; end
    62 : begin branchBase end
    63 : begin  end
    64 : begin branch_0_StuckSA_Transaction[0+4-1 +: 4] /* search */ <= T[14+4-1 +: 4] /* search */; end
    65 : begin setIntInstruction end
    66 : begin branch_0_StuckSA_Transaction[20+3-1 +: 3] /* size */ <= branch_0_StuckSA_Memory[0+3-1 +: 3] /* currentSize */; end
    67 : begin if (branch_0_StuckSA_Transaction[4+3-1 +: 3] /* limit */ == 0) step = 68; end
    68 : begin branch_0_StuckSA_Transaction[20+3-1 +: 3] /* size */ <= branch_0_StuckSA_Transaction[20+3-1 +: 3] /* size */- 1; end
    69 : begin step = 68; end
    70 : begin setIntInstruction end
    71 : begin branch_0_StuckSA_Transaction[10+3-1 +: 3] /* index */ <= branch_0_StuckSA_Transaction[20+3-1 +: 3] /* size */; end
    72 : begin setIntInstruction end
    73 : begin branch_0_StuckSA_Transaction[26+1-1 +: 1] /* equal */ <= branch_0_StuckSA_Transaction[10+3-1 +: 3] /* index */ == branch_0_StuckSA_Transaction[20+3-1 +: 3] /* size */; end
    74 : begin if (branch_0_StuckSA_Transaction[26+1-1 +: 1] /* equal */ > 0) step = 114; end
    75 : begin branch_0_StuckSA_Transaction[13+4-1 +: 4] /* key */ <= branch_0_StuckSA_Memory[3+4-1 +branch_0_StuckSA_Transaction[13-1+:3]*4 +: 4] /* key(index) */; end
    76 : begin branch_0_StuckSA_Transaction[26+1-1 +: 1] /* equal */ <= branch_0_StuckSA_Transaction[13+4-1 +: 4] /* key */ >= branch_0_StuckSA_Transaction[0+4-1 +: 4] /* search */; end
    77 : begin if (branch_0_StuckSA_Transaction[26+1-1 +: 1] /* equal */ == 0) step = 81; end
    78 : begin setIntInstruction end
    79 : begin branch_0_StuckSA_Transaction[13+4-1 +: 4] /* key */ <= branch_0_StuckSA_Memory[3+4-1 +branch_0_StuckSA_Transaction[13-1+:3]*4 +: 4] /* key(index) */; end
    80 : begin branch_0_StuckSA_Transaction[17+3-1 +: 3] /* data */ <= branch_0_StuckSA_Memory[19+3-1 +branch_0_StuckSA_Transaction[13-1+:3]*3 +: 3] /* data(index) */; end
    81 : begin step = 114; end
    82 : begin step = 81; end
    83 : begin setIntInstruction end
    84 : begin branch_0_StuckSA_Transaction[26+1-1 +: 1] /* equal */ <= branch_0_StuckSA_Transaction[10+3-1 +: 3] /* index */ == branch_0_StuckSA_Transaction[20+3-1 +: 3] /* size */; end
    85 : begin if (branch_0_StuckSA_Transaction[26+1-1 +: 1] /* equal */ > 0) step = 114; end
    86 : begin branch_0_StuckSA_Transaction[13+4-1 +: 4] /* key */ <= branch_0_StuckSA_Memory[3+4-1 +branch_0_StuckSA_Transaction[13-1+:3]*4 +: 4] /* key(index) */; end
    87 : begin branch_0_StuckSA_Transaction[26+1-1 +: 1] /* equal */ <= branch_0_StuckSA_Transaction[13+4-1 +: 4] /* key */ >= branch_0_StuckSA_Transaction[0+4-1 +: 4] /* search */; end
    88 : begin if (branch_0_StuckSA_Transaction[26+1-1 +: 1] /* equal */ == 0) step = 92; end
    89 : begin setIntInstruction end
    90 : begin branch_0_StuckSA_Transaction[13+4-1 +: 4] /* key */ <= branch_0_StuckSA_Memory[3+4-1 +branch_0_StuckSA_Transaction[13-1+:3]*4 +: 4] /* key(index) */; end
    91 : begin branch_0_StuckSA_Transaction[17+3-1 +: 3] /* data */ <= branch_0_StuckSA_Memory[19+3-1 +branch_0_StuckSA_Transaction[13-1+:3]*3 +: 3] /* data(index) */; end
    92 : begin step = 114; end
    93 : begin step = 92; end
    94 : begin setIntInstruction end
    95 : begin branch_0_StuckSA_Transaction[26+1-1 +: 1] /* equal */ <= branch_0_StuckSA_Transaction[10+3-1 +: 3] /* index */ == branch_0_StuckSA_Transaction[20+3-1 +: 3] /* size */; end
    96 : begin if (branch_0_StuckSA_Transaction[26+1-1 +: 1] /* equal */ > 0) step = 114; end
    97 : begin branch_0_StuckSA_Transaction[13+4-1 +: 4] /* key */ <= branch_0_StuckSA_Memory[3+4-1 +branch_0_StuckSA_Transaction[13-1+:3]*4 +: 4] /* key(index) */; end
    98 : begin branch_0_StuckSA_Transaction[26+1-1 +: 1] /* equal */ <= branch_0_StuckSA_Transaction[13+4-1 +: 4] /* key */ >= branch_0_StuckSA_Transaction[0+4-1 +: 4] /* search */; end
    99 : begin if (branch_0_StuckSA_Transaction[26+1-1 +: 1] /* equal */ == 0) step = 103; end
   100 : begin setIntInstruction end
   101 : begin branch_0_StuckSA_Transaction[13+4-1 +: 4] /* key */ <= branch_0_StuckSA_Memory[3+4-1 +branch_0_StuckSA_Transaction[13-1+:3]*4 +: 4] /* key(index) */; end
   102 : begin branch_0_StuckSA_Transaction[17+3-1 +: 3] /* data */ <= branch_0_StuckSA_Memory[19+3-1 +branch_0_StuckSA_Transaction[13-1+:3]*3 +: 3] /* data(index) */; end
   103 : begin step = 114; end
   104 : begin step = 103; end
   105 : begin setIntInstruction end
   106 : begin branch_0_StuckSA_Transaction[26+1-1 +: 1] /* equal */ <= branch_0_StuckSA_Transaction[10+3-1 +: 3] /* index */ == branch_0_StuckSA_Transaction[20+3-1 +: 3] /* size */; end
   107 : begin if (branch_0_StuckSA_Transaction[26+1-1 +: 1] /* equal */ > 0) step = 114; end
   108 : begin branch_0_StuckSA_Transaction[13+4-1 +: 4] /* key */ <= branch_0_StuckSA_Memory[3+4-1 +branch_0_StuckSA_Transaction[13-1+:3]*4 +: 4] /* key(index) */; end
   109 : begin branch_0_StuckSA_Transaction[26+1-1 +: 1] /* equal */ <= branch_0_StuckSA_Transaction[13+4-1 +: 4] /* key */ >= branch_0_StuckSA_Transaction[0+4-1 +: 4] /* search */; end
   110 : begin if (branch_0_StuckSA_Transaction[26+1-1 +: 1] /* equal */ == 0) step = 114; end
   111 : begin setIntInstruction end
   112 : begin branch_0_StuckSA_Transaction[13+4-1 +: 4] /* key */ <= branch_0_StuckSA_Memory[3+4-1 +branch_0_StuckSA_Transaction[13-1+:3]*4 +: 4] /* key(index) */; end
   113 : begin branch_0_StuckSA_Transaction[17+3-1 +: 3] /* data */ <= branch_0_StuckSA_Memory[19+3-1 +branch_0_StuckSA_Transaction[13-1+:3]*3 +: 3] /* data(index) */; end
   114 : begin step = 114; end
   115 : begin step = 114; end
   116 : begin T[18+1-1 +: 1] /* found */ <= branch_0_StuckSA_Transaction[9+1-1 +: 1] /* found */; end
   117 : begin T[8+3-1 +: 3] /* first */ <= branch_0_StuckSA_Transaction[10+3-1 +: 3] /* index */; end
   118 : begin if (T[18+1-1 +: 1] /* found */ == 0) step = 119; end
   119 : begin T[11+3-1 +: 3] /* next */ <= branch_0_StuckSA_Transaction[17+3-1 +: 3] /* data */; end
   120 : begin step = 131; end
   121 : begin branch_0_StuckSA_Transaction[20+3-1 +: 3] /* size */ <= branch_0_StuckSA_Memory[0+3-1 +: 3] /* currentSize */; end
   122 : begin setIntInstruction end
   123 : begin branch_0_StuckSA_Transaction[8+1-1 +: 1] /* isEmpty */ <= branch_0_StuckSA_Transaction[20+3-1 +: 3] /* size */ == branch_0_StuckSA_Transaction[23+3-1 +: 3] /* full */; end
   124 : begin if (branch_0_StuckSA_Transaction[8+1-1 +: 1] /* isEmpty */ == 0) step = 125; end
   125 : begin Halt end
   126 : begin step = 125; end
   127 : begin setIntInstruction end
   128 : begin branch_0_StuckSA_Transaction[10+3-1 +: 3] /* index */ <= branch_0_StuckSA_Memory[0+3-1 +: 3] /* currentSize */; end
   129 : begin branch_0_StuckSA_Transaction[10+3-1 +: 3] /* index */ <= branch_0_StuckSA_Transaction[10+3-1 +: 3] /* index */- 1; end
   130 : begin branch_0_StuckSA_Transaction[13+4-1 +: 4] /* key */ <= branch_0_StuckSA_Memory[3+4-1 +branch_0_StuckSA_Transaction[13-1+:3]*4 +: 4] /* key(index) */; end
   131 : begin branch_0_StuckSA_Transaction[17+3-1 +: 3] /* data */ <= branch_0_StuckSA_Memory[19+3-1 +branch_0_StuckSA_Transaction[13-1+:3]*3 +: 3] /* data(index) */; end
   132 : begin T[11+3-1 +: 3] /* next */ <= branch_0_StuckSA_Transaction[17+3-1 +: 3] /* data */; end
   133 : begin T[131+3-1 +: 3] /* child */ <= T[11+3-1 +: 3] /* next */; end
   134 : begin T[164+3-1 +: 3] /* node_isLeaf */ <= T[131+3-1 +: 3] /* child */; end
   135 : begin T[77+1-1 +: 1] /* IsLeaf */ <= M[3+1-1 +T[167-1+:3]*35 +: 1] /* isLeaf(node_isLeaf) */; end
   136 : begin if (T[77+1-1 +: 1] /* IsLeaf */ == 0) step = 179; end
   137 : begin T[14+4-1 +: 4] /* search */ <= T[114+4-1 +: 4] /* Key */; end
   138 : begin T[221+3-1 +: 3] /* node_findEqualInLeaf */ <= T[131+3-1 +: 3] /* child */; end
   139 : begin T[173+3-1 +: 3] /* node_assertLeaf */ <= T[221+3-1 +: 3] /* node_findEqualInLeaf */; end
   140 : begin T[164+3-1 +: 3] /* node_isLeaf */ <= T[173+3-1 +: 3] /* node_assertLeaf */; end
   141 : begin T[77+1-1 +: 1] /* IsLeaf */ <= M[3+1-1 +T[167-1+:3]*35 +: 1] /* isLeaf(node_isLeaf) */; end
   142 : begin if (T[77+1-1 +: 1] /* IsLeaf */ == 0) step = 142; end
   143 : begin step = 143; end
   144 : begin Halt end
   145 : begin T[194+3-1 +: 3] /* node_leafBase */ <= T[221+3-1 +: 3] /* node_findEqualInLeaf */; end
   146 : begin leafBase end
   147 : begin  end
   148 : begin leaf_0_StuckSA_Transaction[0+4-1 +: 4] /* search */ <= T[14+4-1 +: 4] /* search */; end
   149 : begin setIntInstruction end
   150 : begin leaf_0_StuckSA_Transaction[21+3-1 +: 3] /* size */ <= leaf_0_StuckSA_Memory[0+3-1 +: 3] /* currentSize */; end
   151 : begin if (leaf_0_StuckSA_Transaction[4+3-1 +: 3] /* limit */ == 0) step = 152; end
   152 : begin leaf_0_StuckSA_Transaction[21+3-1 +: 3] /* size */ <= leaf_0_StuckSA_Transaction[21+3-1 +: 3] /* size */- 1; end
   153 : begin step = 152; end
   154 : begin setIntInstruction end
   155 : begin setIntInstruction end
   156 : begin leaf_0_StuckSA_Transaction[27+1-1 +: 1] /* equal */ <= leaf_0_StuckSA_Transaction[10+3-1 +: 3] /* index */ == leaf_0_StuckSA_Transaction[21+3-1 +: 3] /* size */; end
   157 : begin if (leaf_0_StuckSA_Transaction[27+1-1 +: 1] /* equal */ > 0) step = 173; end
   158 : begin leaf_0_StuckSA_Transaction[13+4-1 +: 4] /* key */ <= leaf_0_StuckSA_Memory[3+4-1 +leaf_0_StuckSA_Transaction[13-1+:3]*4 +: 4] /* key(index) */; end
   159 : begin leaf_0_StuckSA_Transaction[27+1-1 +: 1] /* equal */ <= leaf_0_StuckSA_Transaction[13+4-1 +: 4] /* key */ == leaf_0_StuckSA_Transaction[0+4-1 +: 4] /* search */; end
   160 : begin if (leaf_0_StuckSA_Transaction[27+1-1 +: 1] /* equal */ == 0) step = 163; end
   161 : begin setIntInstruction end
   162 : begin leaf_0_StuckSA_Transaction[17+4-1 +: 4] /* data */ <= leaf_0_StuckSA_Memory[11+4-1 +leaf_0_StuckSA_Transaction[13-1+:3]*4 +: 4] /* data(index) */; end
   163 : begin step = 173; end
   164 : begin step = 163; end
   165 : begin setIntInstruction end
   166 : begin leaf_0_StuckSA_Transaction[27+1-1 +: 1] /* equal */ <= leaf_0_StuckSA_Transaction[10+3-1 +: 3] /* index */ == leaf_0_StuckSA_Transaction[21+3-1 +: 3] /* size */; end
   167 : begin if (leaf_0_StuckSA_Transaction[27+1-1 +: 1] /* equal */ > 0) step = 173; end
   168 : begin leaf_0_StuckSA_Transaction[13+4-1 +: 4] /* key */ <= leaf_0_StuckSA_Memory[3+4-1 +leaf_0_StuckSA_Transaction[13-1+:3]*4 +: 4] /* key(index) */; end
   169 : begin leaf_0_StuckSA_Transaction[27+1-1 +: 1] /* equal */ <= leaf_0_StuckSA_Transaction[13+4-1 +: 4] /* key */ == leaf_0_StuckSA_Transaction[0+4-1 +: 4] /* search */; end
   170 : begin if (leaf_0_StuckSA_Transaction[27+1-1 +: 1] /* equal */ == 0) step = 173; end
   171 : begin setIntInstruction end
   172 : begin leaf_0_StuckSA_Transaction[17+4-1 +: 4] /* data */ <= leaf_0_StuckSA_Memory[11+4-1 +leaf_0_StuckSA_Transaction[13-1+:3]*4 +: 4] /* data(index) */; end
   173 : begin step = 173; end
   174 : begin step = 173; end
   175 : begin T[18+1-1 +: 1] /* found */ <= leaf_0_StuckSA_Transaction[9+1-1 +: 1] /* found */; end
   176 : begin T[59+3-1 +: 3] /* index */ <= leaf_0_StuckSA_Transaction[10+3-1 +: 3] /* index */; end
   177 : begin T[23+4-1 +: 4] /* data */ <= leaf_0_StuckSA_Transaction[17+4-1 +: 4] /* data */; end
   178 : begin T[122+3-1 +: 3] /* find */ <= T[131+3-1 +: 3] /* child */; end
   179 : begin step = 182; end
   180 : begin step = 179; end
   181 : begin T[128+3-1 +: 3] /* parent */ <= T[131+3-1 +: 3] /* child */; end
   182 : begin step = 48; end
   183 : begin Halt end
endcase

      $display("%4d  %4d  %4d", pc, Key, Data);
    end
  end
endmodule
