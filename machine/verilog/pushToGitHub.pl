#!/usr/bin/perl
#-------------------------------------------------------------------------------
# Compile and run verilog
#-------------------------------------------------------------------------------
say STDERR qx(rm cpu; iverilog -g2012 -o cpu cpu_tb.sv cpu.sv && ./cpu | tee cpu.txt);
