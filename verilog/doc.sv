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

  integer pc;                                                                   // Program counter

  `include "memory.sv"
  reg [253:0] T;


  always @ (posedge reset, posedge clock) begin                                 // Execute next step in program

    if (reset) begin;                                                           // Reset
      pc <= 0;
      $display("reset");
    end

    else begin;                                                                 // Run
      pc <= pc + 1;
      case(pc)
     1 : begin T[164+3-1 +: 3] /* node_isLeaf */ <= 0; end
     2 : begin T[77+1-1 +: 1] /* IsLeaf */ <= M[3+1-1 +T[167-1+:3]*35 +: 1] /* isLeaf(node_isLeaf) */; end
     3 : begin  end
     4 : begin T[14+4-1 +: 4] /* search */ <= T[114+4-1 +: 4] /* Key */; end
     5 : begin  end
     6 : begin T[173+3-1 +: 3] /* node_assertLeaf */ <= T[221+3-1 +: 3] /* node_findEqualInLeaf */; end
     7 : begin T[164+3-1 +: 3] /* node_isLeaf */ <= T[173+3-1 +: 3] /* node_assertLeaf */; end
     8 : begin T[77+1-1 +: 1] /* IsLeaf */ <= M[3+1-1 +T[167-1+:3]*35 +: 1] /* isLeaf(node_isLeaf) */; end
     9 : begin  end
    10 : begin  end
    11 : begin  end
    12 : begin T[194+3-1 +: 3] /* node_leafBase */ <= T[221+3-1 +: 3] /* node_findEqualInLeaf */; end
    13 : begin  end
    14 : begin  end
    15 : begin StuckSA_Transaction[0+4-1 +: 4] /* search */ <= T[14+4-1 +: 4] /* search */; end
    16 : begin  end
    17 : begin StuckSA_Transaction[21+3-1 +: 3] /* size */ <= StuckSA_Memory[0+3-1 +: 3] /* currentSize */; end
    18 : begin  end
    19 : begin  end
    20 : begin  end
    21 : begin StuckSA_Transaction[27+1-1 +: 1] /* equal */ <= StuckSA_Transaction[10+3-1 +: 3] /* index */ == StuckSA_Transaction[21+3-1 +: 3] /* size */; end
    22 : begin  end
    23 : begin StuckSA_Transaction[13+4-1 +: 4] /* key */ <= StuckSA_Memory[3+4-1 +StuckSA_Transaction[13-1+:3]*4 +: 4] /* key(index) */; end
    24 : begin StuckSA_Transaction[27+1-1 +: 1] /* equal */ <= StuckSA_Transaction[13+4-1 +: 4] /* key */ == StuckSA_Transaction[0+4-1 +: 4] /* search */; end
    25 : begin  end
    26 : begin  end
    27 : begin StuckSA_Transaction[17+4-1 +: 4] /* data */ <= StuckSA_Memory[11+4-1 +StuckSA_Transaction[13-1+:3]*4 +: 4] /* data(index) */; end
    28 : begin  end
    29 : begin  end
    30 : begin  end
    31 : begin StuckSA_Transaction[27+1-1 +: 1] /* equal */ <= StuckSA_Transaction[10+3-1 +: 3] /* index */ == StuckSA_Transaction[21+3-1 +: 3] /* size */; end
    32 : begin  end
    33 : begin StuckSA_Transaction[13+4-1 +: 4] /* key */ <= StuckSA_Memory[3+4-1 +StuckSA_Transaction[13-1+:3]*4 +: 4] /* key(index) */; end
    34 : begin StuckSA_Transaction[27+1-1 +: 1] /* equal */ <= StuckSA_Transaction[13+4-1 +: 4] /* key */ == StuckSA_Transaction[0+4-1 +: 4] /* search */; end
    35 : begin  end
    36 : begin  end
    37 : begin StuckSA_Transaction[17+4-1 +: 4] /* data */ <= StuckSA_Memory[11+4-1 +StuckSA_Transaction[13-1+:3]*4 +: 4] /* data(index) */; end
    38 : begin  end
    39 : begin  end
    40 : begin T[18+1-1 +: 1] /* found */ <= StuckSA_Transaction[9+1-1 +: 1] /* found */; end
    41 : begin T[59+3-1 +: 3] /* index */ <= StuckSA_Transaction[10+3-1 +: 3] /* index */; end
    42 : begin T[23+4-1 +: 4] /* data */ <= StuckSA_Transaction[17+4-1 +: 4] /* data */; end
    43 : begin T[122+3-1 +: 3] /* find */ <= 0; end
    44 : begin  end
    45 : begin  end
    46 : begin T[128+3-1 +: 3] /* parent */ <= 0; end
    47 : begin T[149+3-1 +: 3] /* findDepth */ <= 0; end
    48 : begin T[149+3-1 +: 3] /* findDepth */ <= T[149+3-1 +: 3] /* findDepth */+ 1; end
    49 : begin T[83+1-1 +: 1] /* pastMaxDepth */ <= T[149+3-1 +: 3] /* findDepth */ > T[146+3-1 +: 3] /* maxDepth */; end
    50 : begin  end
    51 : begin T[14+4-1 +: 4] /* search */ <= T[114+4-1 +: 4] /* Key */; end
    52 : begin T[227+3-1 +: 3] /* node_findFirstGreaterThanOrEqualInBranch */ <= T[128+3-1 +: 3] /* parent */; end
    53 : begin T[176+3-1 +: 3] /* node_assertBranch */ <= T[227+3-1 +: 3] /* node_findFirstGreaterThanOrEqualInBranch */; end
    54 : begin T[164+3-1 +: 3] /* node_isLeaf */ <= T[176+3-1 +: 3] /* node_assertBranch */; end
    55 : begin T[77+1-1 +: 1] /* IsLeaf */ <= M[3+1-1 +T[167-1+:3]*35 +: 1] /* isLeaf(node_isLeaf) */; end
    56 : begin  end
    57 : begin  end
    58 : begin  end
    59 : begin T[197+3-1 +: 3] /* node_branchBase */ <= T[227+3-1 +: 3] /* node_findFirstGreaterThanOrEqualInBranch */; end
    60 : begin  end
    61 : begin  end
    62 : begin StuckSA_Transaction[0+4-1 +: 4] /* search */ <= T[14+4-1 +: 4] /* search */; end
    63 : begin  end
    64 : begin StuckSA_Transaction[20+3-1 +: 3] /* size */ <= StuckSA_Memory[0+3-1 +: 3] /* currentSize */; end
    65 : begin  end
    66 : begin  end
    67 : begin StuckSA_Transaction[10+3-1 +: 3] /* index */ <= StuckSA_Transaction[20+3-1 +: 3] /* size */; end
    68 : begin  end
    69 : begin StuckSA_Transaction[26+1-1 +: 1] /* equal */ <= StuckSA_Transaction[10+3-1 +: 3] /* index */ == StuckSA_Transaction[20+3-1 +: 3] /* size */; end
    70 : begin  end
    71 : begin StuckSA_Transaction[13+4-1 +: 4] /* key */ <= StuckSA_Memory[3+4-1 +StuckSA_Transaction[13-1+:3]*4 +: 4] /* key(index) */; end
    72 : begin StuckSA_Transaction[26+1-1 +: 1] /* equal */ <= StuckSA_Transaction[13+4-1 +: 4] /* key */ >= StuckSA_Transaction[0+4-1 +: 4] /* search */; end
    73 : begin  end
    74 : begin  end
    75 : begin StuckSA_Transaction[13+4-1 +: 4] /* key */ <= StuckSA_Memory[3+4-1 +StuckSA_Transaction[13-1+:3]*4 +: 4] /* key(index) */; end
    76 : begin StuckSA_Transaction[17+3-1 +: 3] /* data */ <= StuckSA_Memory[19+3-1 +StuckSA_Transaction[13-1+:3]*3 +: 3] /* data(index) */; end
    77 : begin  end
    78 : begin  end
    79 : begin  end
    80 : begin StuckSA_Transaction[26+1-1 +: 1] /* equal */ <= StuckSA_Transaction[10+3-1 +: 3] /* index */ == StuckSA_Transaction[20+3-1 +: 3] /* size */; end
    81 : begin  end
    82 : begin StuckSA_Transaction[13+4-1 +: 4] /* key */ <= StuckSA_Memory[3+4-1 +StuckSA_Transaction[13-1+:3]*4 +: 4] /* key(index) */; end
    83 : begin StuckSA_Transaction[26+1-1 +: 1] /* equal */ <= StuckSA_Transaction[13+4-1 +: 4] /* key */ >= StuckSA_Transaction[0+4-1 +: 4] /* search */; end
    84 : begin  end
    85 : begin  end
    86 : begin StuckSA_Transaction[13+4-1 +: 4] /* key */ <= StuckSA_Memory[3+4-1 +StuckSA_Transaction[13-1+:3]*4 +: 4] /* key(index) */; end
    87 : begin StuckSA_Transaction[17+3-1 +: 3] /* data */ <= StuckSA_Memory[19+3-1 +StuckSA_Transaction[13-1+:3]*3 +: 3] /* data(index) */; end
    88 : begin  end
    89 : begin  end
    90 : begin  end
    91 : begin StuckSA_Transaction[26+1-1 +: 1] /* equal */ <= StuckSA_Transaction[10+3-1 +: 3] /* index */ == StuckSA_Transaction[20+3-1 +: 3] /* size */; end
    92 : begin  end
    93 : begin StuckSA_Transaction[13+4-1 +: 4] /* key */ <= StuckSA_Memory[3+4-1 +StuckSA_Transaction[13-1+:3]*4 +: 4] /* key(index) */; end
    94 : begin StuckSA_Transaction[26+1-1 +: 1] /* equal */ <= StuckSA_Transaction[13+4-1 +: 4] /* key */ >= StuckSA_Transaction[0+4-1 +: 4] /* search */; end
    95 : begin  end
    96 : begin  end
    97 : begin StuckSA_Transaction[13+4-1 +: 4] /* key */ <= StuckSA_Memory[3+4-1 +StuckSA_Transaction[13-1+:3]*4 +: 4] /* key(index) */; end
    98 : begin StuckSA_Transaction[17+3-1 +: 3] /* data */ <= StuckSA_Memory[19+3-1 +StuckSA_Transaction[13-1+:3]*3 +: 3] /* data(index) */; end
    99 : begin  end
   100 : begin  end
   101 : begin  end
   102 : begin StuckSA_Transaction[26+1-1 +: 1] /* equal */ <= StuckSA_Transaction[10+3-1 +: 3] /* index */ == StuckSA_Transaction[20+3-1 +: 3] /* size */; end
   103 : begin  end
   104 : begin StuckSA_Transaction[13+4-1 +: 4] /* key */ <= StuckSA_Memory[3+4-1 +StuckSA_Transaction[13-1+:3]*4 +: 4] /* key(index) */; end
   105 : begin StuckSA_Transaction[26+1-1 +: 1] /* equal */ <= StuckSA_Transaction[13+4-1 +: 4] /* key */ >= StuckSA_Transaction[0+4-1 +: 4] /* search */; end
   106 : begin  end
   107 : begin  end
   108 : begin StuckSA_Transaction[13+4-1 +: 4] /* key */ <= StuckSA_Memory[3+4-1 +StuckSA_Transaction[13-1+:3]*4 +: 4] /* key(index) */; end
   109 : begin StuckSA_Transaction[17+3-1 +: 3] /* data */ <= StuckSA_Memory[19+3-1 +StuckSA_Transaction[13-1+:3]*3 +: 3] /* data(index) */; end
   110 : begin  end
   111 : begin  end
   112 : begin T[18+1-1 +: 1] /* found */ <= StuckSA_Transaction[9+1-1 +: 1] /* found */; end
   113 : begin T[8+3-1 +: 3] /* first */ <= StuckSA_Transaction[10+3-1 +: 3] /* index */; end
   114 : begin  end
   115 : begin T[11+3-1 +: 3] /* next */ <= StuckSA_Transaction[17+3-1 +: 3] /* data */; end
   116 : begin  end
   117 : begin StuckSA_Transaction[20+3-1 +: 3] /* size */ <= StuckSA_Memory[0+3-1 +: 3] /* currentSize */; end
   118 : begin  end
   119 : begin StuckSA_Transaction[8+1-1 +: 1] /* isEmpty */ <= StuckSA_Transaction[20+3-1 +: 3] /* size */ == StuckSA_Transaction[23+3-1 +: 3] /* full */; end
   120 : begin  end
   121 : begin  end
   122 : begin StuckSA_Transaction[10+3-1 +: 3] /* index */ <= StuckSA_Memory[0+3-1 +: 3] /* currentSize */; end
   123 : begin StuckSA_Transaction[10+3-1 +: 3] /* index */ <= StuckSA_Transaction[10+3-1 +: 3] /* index */- 1; end
   124 : begin StuckSA_Transaction[13+4-1 +: 4] /* key */ <= StuckSA_Memory[3+4-1 +StuckSA_Transaction[13-1+:3]*4 +: 4] /* key(index) */; end
   125 : begin StuckSA_Transaction[17+3-1 +: 3] /* data */ <= StuckSA_Memory[19+3-1 +StuckSA_Transaction[13-1+:3]*3 +: 3] /* data(index) */; end
   126 : begin T[11+3-1 +: 3] /* next */ <= StuckSA_Transaction[17+3-1 +: 3] /* data */; end
   127 : begin T[131+3-1 +: 3] /* child */ <= T[11+3-1 +: 3] /* next */; end
   128 : begin T[164+3-1 +: 3] /* node_isLeaf */ <= T[131+3-1 +: 3] /* child */; end
   129 : begin T[77+1-1 +: 1] /* IsLeaf */ <= M[3+1-1 +T[167-1+:3]*35 +: 1] /* isLeaf(node_isLeaf) */; end
   130 : begin  end
   131 : begin T[14+4-1 +: 4] /* search */ <= T[114+4-1 +: 4] /* Key */; end
   132 : begin T[221+3-1 +: 3] /* node_findEqualInLeaf */ <= T[131+3-1 +: 3] /* child */; end
   133 : begin T[173+3-1 +: 3] /* node_assertLeaf */ <= T[221+3-1 +: 3] /* node_findEqualInLeaf */; end
   134 : begin T[164+3-1 +: 3] /* node_isLeaf */ <= T[173+3-1 +: 3] /* node_assertLeaf */; end
   135 : begin T[77+1-1 +: 1] /* IsLeaf */ <= M[3+1-1 +T[167-1+:3]*35 +: 1] /* isLeaf(node_isLeaf) */; end
   136 : begin  end
   137 : begin  end
   138 : begin  end
   139 : begin T[194+3-1 +: 3] /* node_leafBase */ <= T[221+3-1 +: 3] /* node_findEqualInLeaf */; end
   140 : begin  end
   141 : begin  end
   142 : begin StuckSA_Transaction[0+4-1 +: 4] /* search */ <= T[14+4-1 +: 4] /* search */; end
   143 : begin  end
   144 : begin StuckSA_Transaction[21+3-1 +: 3] /* size */ <= StuckSA_Memory[0+3-1 +: 3] /* currentSize */; end
   145 : begin  end
   146 : begin  end
   147 : begin  end
   148 : begin StuckSA_Transaction[27+1-1 +: 1] /* equal */ <= StuckSA_Transaction[10+3-1 +: 3] /* index */ == StuckSA_Transaction[21+3-1 +: 3] /* size */; end
   149 : begin  end
   150 : begin StuckSA_Transaction[13+4-1 +: 4] /* key */ <= StuckSA_Memory[3+4-1 +StuckSA_Transaction[13-1+:3]*4 +: 4] /* key(index) */; end
   151 : begin StuckSA_Transaction[27+1-1 +: 1] /* equal */ <= StuckSA_Transaction[13+4-1 +: 4] /* key */ == StuckSA_Transaction[0+4-1 +: 4] /* search */; end
   152 : begin  end
   153 : begin  end
   154 : begin StuckSA_Transaction[17+4-1 +: 4] /* data */ <= StuckSA_Memory[11+4-1 +StuckSA_Transaction[13-1+:3]*4 +: 4] /* data(index) */; end
   155 : begin  end
   156 : begin  end
   157 : begin  end
   158 : begin StuckSA_Transaction[27+1-1 +: 1] /* equal */ <= StuckSA_Transaction[10+3-1 +: 3] /* index */ == StuckSA_Transaction[21+3-1 +: 3] /* size */; end
   159 : begin  end
   160 : begin StuckSA_Transaction[13+4-1 +: 4] /* key */ <= StuckSA_Memory[3+4-1 +StuckSA_Transaction[13-1+:3]*4 +: 4] /* key(index) */; end
   161 : begin StuckSA_Transaction[27+1-1 +: 1] /* equal */ <= StuckSA_Transaction[13+4-1 +: 4] /* key */ == StuckSA_Transaction[0+4-1 +: 4] /* search */; end
   162 : begin  end
   163 : begin  end
   164 : begin StuckSA_Transaction[17+4-1 +: 4] /* data */ <= StuckSA_Memory[11+4-1 +StuckSA_Transaction[13-1+:3]*4 +: 4] /* data(index) */; end
   165 : begin  end
   166 : begin  end
   167 : begin T[18+1-1 +: 1] /* found */ <= StuckSA_Transaction[9+1-1 +: 1] /* found */; end
   168 : begin T[59+3-1 +: 3] /* index */ <= StuckSA_Transaction[10+3-1 +: 3] /* index */; end
   169 : begin T[23+4-1 +: 4] /* data */ <= StuckSA_Transaction[17+4-1 +: 4] /* data */; end
   170 : begin T[122+3-1 +: 3] /* find */ <= T[131+3-1 +: 3] /* child */; end
   171 : begin  end
   172 : begin  end
   173 : begin T[128+3-1 +: 3] /* parent */ <= T[131+3-1 +: 3] /* child */; end
   174 : begin  end
   175 : begin  end
endcase

      $display("%4d  %4d  %4d", pc, Key, Data);
    end
  end
endmodule
