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
reg [253:0] T;

reg [9:0]branch_0_StuckSA_Memory_base_offset;
reg [26:0] branch_0_StuckSA_Transaction;
reg [9:0]branch_1_StuckSA_Memory_base_offset;
reg [26:0] branch_1_StuckSA_Transaction;
reg [9:0]branch_2_StuckSA_Memory_base_offset;
reg [26:0] branch_2_StuckSA_Transaction;
reg [9:0]branch_3_StuckSA_Memory_base_offset;
reg [26:0] branch_3_StuckSA_Transaction;
reg [9:0]leaf_0_StuckSA_Memory_base_offset;
reg [27:0] leaf_0_StuckSA_Transaction;
reg [9:0]leaf_1_StuckSA_Memory_base_offset;
reg [27:0] leaf_1_StuckSA_Transaction;
reg [9:0]leaf_2_StuckSA_Memory_base_offset;
reg [27:0] leaf_2_StuckSA_Transaction;
reg [9:0]leaf_3_StuckSA_Memory_base_offset;
reg [27:0] leaf_3_StuckSA_Transaction;


  always @ (posedge reset, posedge clock) begin                                 // Execute next step in program

  if (reset) begin;                                                           // Reset
    step <= 0;
    $display("reset");
  end

  else begin;                                                                 // Run
    case(step)
           0 : begin T[164+3-1 +: 3] /* node_isLeaf */ <= 0; end
           1 : begin T[77+1-1 +: 1] /* IsLeaf */ <= M[3+1-1 +T[167-1+:3]*35 +: 1] /* isLeaf(node_isLeaf) */; end
           2 : begin if (T[77+1-1 +: 1] /* IsLeaf */ == 0) step = 46; end
           3 : begin T[14+4-1 +: 4] /* search */ <= T[114+4-1 +: 4] /* Key */; end
           4 : begin /* setIntInstruction */ end
           5 : begin T[173+3-1 +: 3] /* node_assertLeaf */ <= T[221+3-1 +: 3] /* node_findEqualInLeaf */; end
           6 : begin T[164+3-1 +: 3] /* node_isLeaf */ <= T[173+3-1 +: 3] /* node_assertLeaf */; end
           7 : begin T[77+1-1 +: 1] /* IsLeaf */ <= M[3+1-1 +T[167-1+:3]*35 +: 1] /* isLeaf(node_isLeaf) */; end
           8 : begin if (T[77+1-1 +: 1] /* IsLeaf */ == 0) step = 9; end
           9 : begin step = 10; end
          10 : begin /* Halt */ end
          11 : begin T[194+3-1 +: 3] /* node_leafBase */ <= T[221+3-1 +: 3] /* node_findEqualInLeaf */; end
          12 : begin /* leafBase */ end
          13 : begin  end
          14 : begin leaf_0_StuckSA_Transaction[0+4-1 +: 4] /* search */ <= T[14+4-1 +: 4] /* search */; end
          15 : begin /* setIntInstruction */ end
          16 : begin leaf_0_StuckSA_Transaction[21+3-1 +: 3] /* size */ <= M[leaf_0_StuckSA_Memory_base_offset+0+3-1 +: 3] /* currentSize */; end
          17 : begin if (leaf_0_StuckSA_Transaction[4+3-1 +: 3] /* limit */ == 0) step = 19; end
          18 : begin leaf_0_StuckSA_Transaction[21+3-1 +: 3] /* size */ <= leaf_0_StuckSA_Transaction[21+3-1 +: 3] /* size */- 1; end
          19 : begin step = 19; end
          20 : begin /* setIntInstruction */ end
          21 : begin /* setIntInstruction */ end
          22 : begin leaf_0_StuckSA_Transaction[27+1-1 +: 1] /* equal */ <= leaf_0_StuckSA_Transaction[10+3-1 +: 3] /* index */ == leaf_0_StuckSA_Transaction[21+3-1 +: 3] /* size */; end
          23 : begin if (leaf_0_StuckSA_Transaction[27+1-1 +: 1] /* equal */ > 0) step = 40; end
          24 : begin leaf_0_StuckSA_Transaction[13+4-1 +: 4] /* key */ <= M[leaf_0_StuckSA_Memory_base_offset+3+4-1 +leaf_0_StuckSA_Transaction[13-1+:3]*4 +: 4] /* key(index) */; end
          25 : begin leaf_0_StuckSA_Transaction[27+1-1 +: 1] /* equal */ <= leaf_0_StuckSA_Transaction[13+4-1 +: 4] /* key */ == leaf_0_StuckSA_Transaction[0+4-1 +: 4] /* search */; end
          26 : begin if (leaf_0_StuckSA_Transaction[27+1-1 +: 1] /* equal */ == 0) step = 30; end
          27 : begin /* setIntInstruction */ end
          28 : begin leaf_0_StuckSA_Transaction[17+4-1 +: 4] /* data */ <= M[leaf_0_StuckSA_Memory_base_offset+11+4-1 +leaf_0_StuckSA_Transaction[13-1+:3]*4 +: 4] /* data(index) */; end
          29 : begin step = 40; end
          30 : begin step = 30; end
          31 : begin /* setIntInstruction */ end
          32 : begin leaf_0_StuckSA_Transaction[27+1-1 +: 1] /* equal */ <= leaf_0_StuckSA_Transaction[10+3-1 +: 3] /* index */ == leaf_0_StuckSA_Transaction[21+3-1 +: 3] /* size */; end
          33 : begin if (leaf_0_StuckSA_Transaction[27+1-1 +: 1] /* equal */ > 0) step = 40; end
          34 : begin leaf_0_StuckSA_Transaction[13+4-1 +: 4] /* key */ <= M[leaf_0_StuckSA_Memory_base_offset+3+4-1 +leaf_0_StuckSA_Transaction[13-1+:3]*4 +: 4] /* key(index) */; end
          35 : begin leaf_0_StuckSA_Transaction[27+1-1 +: 1] /* equal */ <= leaf_0_StuckSA_Transaction[13+4-1 +: 4] /* key */ == leaf_0_StuckSA_Transaction[0+4-1 +: 4] /* search */; end
          36 : begin if (leaf_0_StuckSA_Transaction[27+1-1 +: 1] /* equal */ == 0) step = 40; end
          37 : begin /* setIntInstruction */ end
          38 : begin leaf_0_StuckSA_Transaction[17+4-1 +: 4] /* data */ <= M[leaf_0_StuckSA_Memory_base_offset+11+4-1 +leaf_0_StuckSA_Transaction[13-1+:3]*4 +: 4] /* data(index) */; end
          39 : begin step = 40; end
          40 : begin step = 40; end
          41 : begin T[18+1-1 +: 1] /* found */ <= leaf_0_StuckSA_Transaction[9+1-1 +: 1] /* found */; end
          42 : begin T[59+3-1 +: 3] /* index */ <= leaf_0_StuckSA_Transaction[10+3-1 +: 3] /* index */; end
          43 : begin T[23+4-1 +: 4] /* data */ <= leaf_0_StuckSA_Transaction[17+4-1 +: 4] /* data */; end
          44 : begin T[122+3-1 +: 3] /* find */ <= 0; end
          45 : begin step = 182; end
          46 : begin step = 46; end
          47 : begin T[128+3-1 +: 3] /* parent */ <= 0; end
          48 : begin T[149+3-1 +: 3] /* findDepth */ <= 0; end
          49 : begin T[149+3-1 +: 3] /* findDepth */ <= T[149+3-1 +: 3] /* findDepth */+ 1; end
          50 : begin T[83+1-1 +: 1] /* pastMaxDepth */ <= T[149+3-1 +: 3] /* findDepth */ > T[146+3-1 +: 3] /* maxDepth */; end
          51 : begin if (T[83+1-1 +: 1] /* pastMaxDepth */ > 0) step = 181; end
          52 : begin T[14+4-1 +: 4] /* search */ <= T[114+4-1 +: 4] /* Key */; end
          53 : begin T[227+3-1 +: 3] /* node_findFirstGreaterThanOrEqualInBranch */ <= T[128+3-1 +: 3] /* parent */; end
          54 : begin T[176+3-1 +: 3] /* node_assertBranch */ <= T[227+3-1 +: 3] /* node_findFirstGreaterThanOrEqualInBranch */; end
          55 : begin T[164+3-1 +: 3] /* node_isLeaf */ <= T[176+3-1 +: 3] /* node_assertBranch */; end
          56 : begin T[77+1-1 +: 1] /* IsLeaf */ <= M[3+1-1 +T[167-1+:3]*35 +: 1] /* isLeaf(node_isLeaf) */; end
          57 : begin if (T[77+1-1 +: 1] /* IsLeaf */ == 0) step = 59; end
          58 : begin /* Halt */ end
          59 : begin step = 59; end
          60 : begin T[197+3-1 +: 3] /* node_branchBase */ <= T[227+3-1 +: 3] /* node_findFirstGreaterThanOrEqualInBranch */; end
          61 : begin /* branchBase */ end
          62 : begin  end
          63 : begin branch_0_StuckSA_Transaction[0+4-1 +: 4] /* search */ <= T[14+4-1 +: 4] /* search */; end
          64 : begin /* setIntInstruction */ end
          65 : begin branch_0_StuckSA_Transaction[20+3-1 +: 3] /* size */ <= M[branch_0_StuckSA_Memory_base_offset+0+3-1 +: 3] /* currentSize */; end
          66 : begin if (branch_0_StuckSA_Transaction[4+3-1 +: 3] /* limit */ == 0) step = 68; end
          67 : begin branch_0_StuckSA_Transaction[20+3-1 +: 3] /* size */ <= branch_0_StuckSA_Transaction[20+3-1 +: 3] /* size */- 1; end
          68 : begin step = 68; end
          69 : begin /* setIntInstruction */ end
          70 : begin branch_0_StuckSA_Transaction[10+3-1 +: 3] /* index */ <= branch_0_StuckSA_Transaction[20+3-1 +: 3] /* size */; end
          71 : begin /* setIntInstruction */ end
          72 : begin branch_0_StuckSA_Transaction[26+1-1 +: 1] /* equal */ <= branch_0_StuckSA_Transaction[10+3-1 +: 3] /* index */ == branch_0_StuckSA_Transaction[20+3-1 +: 3] /* size */; end
          73 : begin if (branch_0_StuckSA_Transaction[26+1-1 +: 1] /* equal */ > 0) step = 114; end
          74 : begin branch_0_StuckSA_Transaction[13+4-1 +: 4] /* key */ <= M[branch_0_StuckSA_Memory_base_offset+3+4-1 +branch_0_StuckSA_Transaction[13-1+:3]*4 +: 4] /* key(index) */; end
          75 : begin branch_0_StuckSA_Transaction[26+1-1 +: 1] /* equal */ <= branch_0_StuckSA_Transaction[13+4-1 +: 4] /* key */ >= branch_0_StuckSA_Transaction[0+4-1 +: 4] /* search */; end
          76 : begin if (branch_0_StuckSA_Transaction[26+1-1 +: 1] /* equal */ == 0) step = 81; end
          77 : begin /* setIntInstruction */ end
          78 : begin branch_0_StuckSA_Transaction[13+4-1 +: 4] /* key */ <= M[branch_0_StuckSA_Memory_base_offset+3+4-1 +branch_0_StuckSA_Transaction[13-1+:3]*4 +: 4] /* key(index) */; end
          79 : begin branch_0_StuckSA_Transaction[17+3-1 +: 3] /* data */ <= M[branch_0_StuckSA_Memory_base_offset+19+3-1 +branch_0_StuckSA_Transaction[13-1+:3]*3 +: 3] /* data(index) */; end
          80 : begin step = 114; end
          81 : begin step = 81; end
          82 : begin /* setIntInstruction */ end
          83 : begin branch_0_StuckSA_Transaction[26+1-1 +: 1] /* equal */ <= branch_0_StuckSA_Transaction[10+3-1 +: 3] /* index */ == branch_0_StuckSA_Transaction[20+3-1 +: 3] /* size */; end
          84 : begin if (branch_0_StuckSA_Transaction[26+1-1 +: 1] /* equal */ > 0) step = 114; end
          85 : begin branch_0_StuckSA_Transaction[13+4-1 +: 4] /* key */ <= M[branch_0_StuckSA_Memory_base_offset+3+4-1 +branch_0_StuckSA_Transaction[13-1+:3]*4 +: 4] /* key(index) */; end
          86 : begin branch_0_StuckSA_Transaction[26+1-1 +: 1] /* equal */ <= branch_0_StuckSA_Transaction[13+4-1 +: 4] /* key */ >= branch_0_StuckSA_Transaction[0+4-1 +: 4] /* search */; end
          87 : begin if (branch_0_StuckSA_Transaction[26+1-1 +: 1] /* equal */ == 0) step = 92; end
          88 : begin /* setIntInstruction */ end
          89 : begin branch_0_StuckSA_Transaction[13+4-1 +: 4] /* key */ <= M[branch_0_StuckSA_Memory_base_offset+3+4-1 +branch_0_StuckSA_Transaction[13-1+:3]*4 +: 4] /* key(index) */; end
          90 : begin branch_0_StuckSA_Transaction[17+3-1 +: 3] /* data */ <= M[branch_0_StuckSA_Memory_base_offset+19+3-1 +branch_0_StuckSA_Transaction[13-1+:3]*3 +: 3] /* data(index) */; end
          91 : begin step = 114; end
          92 : begin step = 92; end
          93 : begin /* setIntInstruction */ end
          94 : begin branch_0_StuckSA_Transaction[26+1-1 +: 1] /* equal */ <= branch_0_StuckSA_Transaction[10+3-1 +: 3] /* index */ == branch_0_StuckSA_Transaction[20+3-1 +: 3] /* size */; end
          95 : begin if (branch_0_StuckSA_Transaction[26+1-1 +: 1] /* equal */ > 0) step = 114; end
          96 : begin branch_0_StuckSA_Transaction[13+4-1 +: 4] /* key */ <= M[branch_0_StuckSA_Memory_base_offset+3+4-1 +branch_0_StuckSA_Transaction[13-1+:3]*4 +: 4] /* key(index) */; end
          97 : begin branch_0_StuckSA_Transaction[26+1-1 +: 1] /* equal */ <= branch_0_StuckSA_Transaction[13+4-1 +: 4] /* key */ >= branch_0_StuckSA_Transaction[0+4-1 +: 4] /* search */; end
          98 : begin if (branch_0_StuckSA_Transaction[26+1-1 +: 1] /* equal */ == 0) step = 103; end
          99 : begin /* setIntInstruction */ end
         100 : begin branch_0_StuckSA_Transaction[13+4-1 +: 4] /* key */ <= M[branch_0_StuckSA_Memory_base_offset+3+4-1 +branch_0_StuckSA_Transaction[13-1+:3]*4 +: 4] /* key(index) */; end
         101 : begin branch_0_StuckSA_Transaction[17+3-1 +: 3] /* data */ <= M[branch_0_StuckSA_Memory_base_offset+19+3-1 +branch_0_StuckSA_Transaction[13-1+:3]*3 +: 3] /* data(index) */; end
         102 : begin step = 114; end
         103 : begin step = 103; end
         104 : begin /* setIntInstruction */ end
         105 : begin branch_0_StuckSA_Transaction[26+1-1 +: 1] /* equal */ <= branch_0_StuckSA_Transaction[10+3-1 +: 3] /* index */ == branch_0_StuckSA_Transaction[20+3-1 +: 3] /* size */; end
         106 : begin if (branch_0_StuckSA_Transaction[26+1-1 +: 1] /* equal */ > 0) step = 114; end
         107 : begin branch_0_StuckSA_Transaction[13+4-1 +: 4] /* key */ <= M[branch_0_StuckSA_Memory_base_offset+3+4-1 +branch_0_StuckSA_Transaction[13-1+:3]*4 +: 4] /* key(index) */; end
         108 : begin branch_0_StuckSA_Transaction[26+1-1 +: 1] /* equal */ <= branch_0_StuckSA_Transaction[13+4-1 +: 4] /* key */ >= branch_0_StuckSA_Transaction[0+4-1 +: 4] /* search */; end
         109 : begin if (branch_0_StuckSA_Transaction[26+1-1 +: 1] /* equal */ == 0) step = 114; end
         110 : begin /* setIntInstruction */ end
         111 : begin branch_0_StuckSA_Transaction[13+4-1 +: 4] /* key */ <= M[branch_0_StuckSA_Memory_base_offset+3+4-1 +branch_0_StuckSA_Transaction[13-1+:3]*4 +: 4] /* key(index) */; end
         112 : begin branch_0_StuckSA_Transaction[17+3-1 +: 3] /* data */ <= M[branch_0_StuckSA_Memory_base_offset+19+3-1 +branch_0_StuckSA_Transaction[13-1+:3]*3 +: 3] /* data(index) */; end
         113 : begin step = 114; end
         114 : begin step = 114; end
         115 : begin T[18+1-1 +: 1] /* found */ <= branch_0_StuckSA_Transaction[9+1-1 +: 1] /* found */; end
         116 : begin T[8+3-1 +: 3] /* first */ <= branch_0_StuckSA_Transaction[10+3-1 +: 3] /* index */; end
         117 : begin if (T[18+1-1 +: 1] /* found */ == 0) step = 119; end
         118 : begin T[11+3-1 +: 3] /* next */ <= branch_0_StuckSA_Transaction[17+3-1 +: 3] /* data */; end
         119 : begin step = 131; end
         120 : begin branch_0_StuckSA_Transaction[20+3-1 +: 3] /* size */ <= M[branch_0_StuckSA_Memory_base_offset+0+3-1 +: 3] /* currentSize */; end
         121 : begin /* setIntInstruction */ end
         122 : begin branch_0_StuckSA_Transaction[8+1-1 +: 1] /* isEmpty */ <= branch_0_StuckSA_Transaction[20+3-1 +: 3] /* size */ == branch_0_StuckSA_Transaction[23+3-1 +: 3] /* full */; end
         123 : begin if (branch_0_StuckSA_Transaction[8+1-1 +: 1] /* isEmpty */ == 0) step = 125; end
         124 : begin /* Halt */ end
         125 : begin step = 125; end
         126 : begin /* setIntInstruction */ end
         127 : begin branch_0_StuckSA_Transaction[10+3-1 +: 3] /* index */ <= M[branch_0_StuckSA_Memory_base_offset+0+3-1 +: 3] /* currentSize */; end
         128 : begin branch_0_StuckSA_Transaction[10+3-1 +: 3] /* index */ <= branch_0_StuckSA_Transaction[10+3-1 +: 3] /* index */- 1; end
         129 : begin branch_0_StuckSA_Transaction[13+4-1 +: 4] /* key */ <= M[branch_0_StuckSA_Memory_base_offset+3+4-1 +branch_0_StuckSA_Transaction[13-1+:3]*4 +: 4] /* key(index) */; end
         130 : begin branch_0_StuckSA_Transaction[17+3-1 +: 3] /* data */ <= M[branch_0_StuckSA_Memory_base_offset+19+3-1 +branch_0_StuckSA_Transaction[13-1+:3]*3 +: 3] /* data(index) */; end
         131 : begin T[11+3-1 +: 3] /* next */ <= branch_0_StuckSA_Transaction[17+3-1 +: 3] /* data */; end
         132 : begin T[131+3-1 +: 3] /* child */ <= T[11+3-1 +: 3] /* next */; end
         133 : begin T[164+3-1 +: 3] /* node_isLeaf */ <= T[131+3-1 +: 3] /* child */; end
         134 : begin T[77+1-1 +: 1] /* IsLeaf */ <= M[3+1-1 +T[167-1+:3]*35 +: 1] /* isLeaf(node_isLeaf) */; end
         135 : begin if (T[77+1-1 +: 1] /* IsLeaf */ == 0) step = 179; end
         136 : begin T[14+4-1 +: 4] /* search */ <= T[114+4-1 +: 4] /* Key */; end
         137 : begin T[221+3-1 +: 3] /* node_findEqualInLeaf */ <= T[131+3-1 +: 3] /* child */; end
         138 : begin T[173+3-1 +: 3] /* node_assertLeaf */ <= T[221+3-1 +: 3] /* node_findEqualInLeaf */; end
         139 : begin T[164+3-1 +: 3] /* node_isLeaf */ <= T[173+3-1 +: 3] /* node_assertLeaf */; end
         140 : begin T[77+1-1 +: 1] /* IsLeaf */ <= M[3+1-1 +T[167-1+:3]*35 +: 1] /* isLeaf(node_isLeaf) */; end
         141 : begin if (T[77+1-1 +: 1] /* IsLeaf */ == 0) step = 142; end
         142 : begin step = 143; end
         143 : begin /* Halt */ end
         144 : begin T[194+3-1 +: 3] /* node_leafBase */ <= T[221+3-1 +: 3] /* node_findEqualInLeaf */; end
         145 : begin /* leafBase */ end
         146 : begin  end
         147 : begin leaf_0_StuckSA_Transaction[0+4-1 +: 4] /* search */ <= T[14+4-1 +: 4] /* search */; end
         148 : begin /* setIntInstruction */ end
         149 : begin leaf_0_StuckSA_Transaction[21+3-1 +: 3] /* size */ <= M[leaf_0_StuckSA_Memory_base_offset+0+3-1 +: 3] /* currentSize */; end
         150 : begin if (leaf_0_StuckSA_Transaction[4+3-1 +: 3] /* limit */ == 0) step = 152; end
         151 : begin leaf_0_StuckSA_Transaction[21+3-1 +: 3] /* size */ <= leaf_0_StuckSA_Transaction[21+3-1 +: 3] /* size */- 1; end
         152 : begin step = 152; end
         153 : begin /* setIntInstruction */ end
         154 : begin /* setIntInstruction */ end
         155 : begin leaf_0_StuckSA_Transaction[27+1-1 +: 1] /* equal */ <= leaf_0_StuckSA_Transaction[10+3-1 +: 3] /* index */ == leaf_0_StuckSA_Transaction[21+3-1 +: 3] /* size */; end
         156 : begin if (leaf_0_StuckSA_Transaction[27+1-1 +: 1] /* equal */ > 0) step = 173; end
         157 : begin leaf_0_StuckSA_Transaction[13+4-1 +: 4] /* key */ <= M[leaf_0_StuckSA_Memory_base_offset+3+4-1 +leaf_0_StuckSA_Transaction[13-1+:3]*4 +: 4] /* key(index) */; end
         158 : begin leaf_0_StuckSA_Transaction[27+1-1 +: 1] /* equal */ <= leaf_0_StuckSA_Transaction[13+4-1 +: 4] /* key */ == leaf_0_StuckSA_Transaction[0+4-1 +: 4] /* search */; end
         159 : begin if (leaf_0_StuckSA_Transaction[27+1-1 +: 1] /* equal */ == 0) step = 163; end
         160 : begin /* setIntInstruction */ end
         161 : begin leaf_0_StuckSA_Transaction[17+4-1 +: 4] /* data */ <= M[leaf_0_StuckSA_Memory_base_offset+11+4-1 +leaf_0_StuckSA_Transaction[13-1+:3]*4 +: 4] /* data(index) */; end
         162 : begin step = 173; end
         163 : begin step = 163; end
         164 : begin /* setIntInstruction */ end
         165 : begin leaf_0_StuckSA_Transaction[27+1-1 +: 1] /* equal */ <= leaf_0_StuckSA_Transaction[10+3-1 +: 3] /* index */ == leaf_0_StuckSA_Transaction[21+3-1 +: 3] /* size */; end
         166 : begin if (leaf_0_StuckSA_Transaction[27+1-1 +: 1] /* equal */ > 0) step = 173; end
         167 : begin leaf_0_StuckSA_Transaction[13+4-1 +: 4] /* key */ <= M[leaf_0_StuckSA_Memory_base_offset+3+4-1 +leaf_0_StuckSA_Transaction[13-1+:3]*4 +: 4] /* key(index) */; end
         168 : begin leaf_0_StuckSA_Transaction[27+1-1 +: 1] /* equal */ <= leaf_0_StuckSA_Transaction[13+4-1 +: 4] /* key */ == leaf_0_StuckSA_Transaction[0+4-1 +: 4] /* search */; end
         169 : begin if (leaf_0_StuckSA_Transaction[27+1-1 +: 1] /* equal */ == 0) step = 173; end
         170 : begin /* setIntInstruction */ end
         171 : begin leaf_0_StuckSA_Transaction[17+4-1 +: 4] /* data */ <= M[leaf_0_StuckSA_Memory_base_offset+11+4-1 +leaf_0_StuckSA_Transaction[13-1+:3]*4 +: 4] /* data(index) */; end
         172 : begin step = 173; end
         173 : begin step = 173; end
         174 : begin T[18+1-1 +: 1] /* found */ <= leaf_0_StuckSA_Transaction[9+1-1 +: 1] /* found */; end
         175 : begin T[59+3-1 +: 3] /* index */ <= leaf_0_StuckSA_Transaction[10+3-1 +: 3] /* index */; end
         176 : begin T[23+4-1 +: 4] /* data */ <= leaf_0_StuckSA_Transaction[17+4-1 +: 4] /* data */; end
         177 : begin T[122+3-1 +: 3] /* find */ <= T[131+3-1 +: 3] /* child */; end
         178 : begin step = 182; end
         179 : begin step = 179; end
         180 : begin T[128+3-1 +: 3] /* parent */ <= T[131+3-1 +: 3] /* child */; end
         181 : begin step = 48; end
         182 : begin /* Halt */ end
    endcase
    step <= step + 1;
    $display("%4d", step);
  end
end

endmodule
