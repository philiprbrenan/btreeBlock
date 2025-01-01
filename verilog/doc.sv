//-----------------------------------------------------------------------------
// Database on a chip
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2025
//------------------------------------------------------------------------------
`timescale 10ps/1ps
module doc(reset, stop, clock, pfd, Key, Data, data, found);                    // Database on a chip
  input      reset;                                                             // Restart the program run sequence when this goes high
  input      stop;                                                              // Program has stopped when this goes high
  input      clock;                                                             // Program counter clock
  input [2:0]pfd;                                                               // Put, find delete
  input [7:0]Key;                                                               // Input key
  input [7:0]Data;                                                              // Input data
  output[7:0]data;                                                              // Output data
  output     found;                                                             // Whether the key was found on put, find delete

  integer pc;                                                                   // Program counter

  always @ (posedge reset, posedge clock) begin                                 // Execute next step in program

    if (reset) begin;                                                           // Reset
      pc <= 0;
      $display("reset");
    end

    else begin;                                                                 // Run
      pc <= pc + 1;
      $display("%4d  %4d  %4d", pc, Key, Data);
    end
  end
endmodule
