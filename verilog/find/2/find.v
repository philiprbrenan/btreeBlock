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

  reg [1:0] T;


  always @ (posedge reset, posedge clock) begin                                 // Execute next step in program

    if (reset) begin                                                            // Reset
        T <= 0;
    end
    else if (clock) begin                                                                  // Run
      T[0] <= 1;
    end // Execute
  end // Always
endmodule
