//-----------------------------------------------------------------------------
// Database on a chip
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2025-01-07
//------------------------------------------------------------------------------
`timescale 10ps/1ps
(* keep_hierarchy = "yes" *)
module find(reset, clock, found);
  input                   reset;                                                // Restart the program run sequence when this goes high
  input                   clock;                                                // Program counter clock
  output                  found;                                                // Whether the key was found on put, find delete

  reg [4:0] T;
  assign found = T[0];
  integer  step;                                                                // Program counter

  always @ (posedge reset, posedge clock) begin                                 // Execute next step in program

    if (reset) begin                                                            // Reset
      T <= 0;
      step <= 0;
    end
    else if (clock) begin                                                                  // Run
      case(step)
        0 : begin if (T[1] == T[0]) T[1] <= 1; end
        1 : begin if (T[2] == T[0]) T[2] <= 1; end
        2 : begin if (T[3] == T[0]) T[3] <= 1; end
        default: step <= 0;
      endcase
      step <= step + 1;
    end
  end
endmodule
