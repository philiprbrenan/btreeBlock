#!/usr/bin/perl -I/home/phil/perl/cpan/DataTableText/lib/
#-------------------------------------------------------------------------------
# Push Vivado results to GitHub
# Philip R Brenan at gmail dot com, Appa Apps Ltd Inc., 2024
#-------------------------------------------------------------------------------
use warnings FATAL => qw(all);
use strict;
use Carp;
use Data::Dump qw(dump);
use Data::Table::Text qw(:all);
use GitHub::Crud qw(:all);
use feature qw(say current_sub);

my $source = q(/home/phil/aaa/btreeBlock/verilog/);                             # Source folder
my $target = q(/home/phil/btreeBlock/verilog/);                                 # Target folder
my @ext    = qw(.txt .rpt);                                                     # Extensions of files to upload to github

if (0)
 {say STDERR "Get file names";
  push my @files, searchDirectoryTreesForMatchingFiles($source, @ext);          # Possible source files
         @files = grep {!m(vivado/pins/)} @files;
         @files = grep {!m(tests.txt|trace.txt|traceJava.txt)} @files;
  say STDERR "AAAA\n", dump(\@files);
  exit;
 }

say STDERR "Recover reports";

for my $s(&files()->@*)                                                         # Target each source file
 {say STDERR $s;
  my $t = swapFilePrefix $s, $source,  $target;
  copyFile($s, $t) if -e $s;
 }

sub files() {return [
  (map {qq(/home/phil/aaa/btreeBlock/verilog/delete/3/statement/$_/vivado/reports/timing_route.rpt)} 0..400),
  (map {qq(/home/phil/aaa/btreeBlock/verilog/find/2/statement/$_/vivado/reports/timing_route.rpt)  } 0.. 20),
  (map {qq(/home/phil/aaa/btreeBlock/verilog/put/1/statement/$_/vivado/reports/timing_route.rpt)   } 0..400),
  q(/home/phil/aaa/btreeBlock/verilog/delete/3/vivado/reports/1.txt),
  q(/home/phil/aaa/btreeBlock/verilog/delete/3/vivado/reports/bus_skew.rpt),
  q(/home/phil/aaa/btreeBlock/verilog/delete/3/vivado/reports/cdc.rpt),
  q(/home/phil/aaa/btreeBlock/verilog/delete/3/vivado/reports/clock_interaction.rpt),
  q(/home/phil/aaa/btreeBlock/verilog/delete/3/vivado/reports/control.rpt),
  q(/home/phil/aaa/btreeBlock/verilog/delete/3/vivado/reports/control_sets.rpt),
  q(/home/phil/aaa/btreeBlock/verilog/delete/3/vivado/reports/design_analysis.rpt),
  q(/home/phil/aaa/btreeBlock/verilog/delete/3/vivado/reports/drc.rpt),
  q(/home/phil/aaa/btreeBlock/verilog/delete/3/vivado/reports/fanout.rpt),
  q(/home/phil/aaa/btreeBlock/verilog/delete/3/vivado/reports/high_fanout_nets.rpt),
  q(/home/phil/aaa/btreeBlock/verilog/delete/3/vivado/reports/methodology.rpt),
  q(/home/phil/aaa/btreeBlock/verilog/delete/3/vivado/reports/power.rpt),
  q(/home/phil/aaa/btreeBlock/verilog/delete/3/vivado/reports/timing.rpt),
  q(/home/phil/aaa/btreeBlock/verilog/delete/3/vivado/reports/timing_route.rpt),
  q(/home/phil/aaa/btreeBlock/verilog/delete/3/vivado/reports/timing_summary.rpt),
  q(/home/phil/aaa/btreeBlock/verilog/delete/3/vivado/reports/utilization.rpt),
  q(/home/phil/aaa/btreeBlock/verilog/find/2/vivado/reports/1.txt),
  q(/home/phil/aaa/btreeBlock/verilog/find/2/vivado/reports/bus_skew.rpt),
  q(/home/phil/aaa/btreeBlock/verilog/find/2/vivado/reports/cdc.rpt),
  q(/home/phil/aaa/btreeBlock/verilog/find/2/vivado/reports/clock_interaction.rpt),
  q(/home/phil/aaa/btreeBlock/verilog/find/2/vivado/reports/control_sets.rpt),
  q(/home/phil/aaa/btreeBlock/verilog/find/2/vivado/reports/design_analysis.rpt),
  q(/home/phil/aaa/btreeBlock/verilog/find/2/vivado/reports/drc.rpt),
  q(/home/phil/aaa/btreeBlock/verilog/find/2/vivado/reports/high_fanout_nets.rpt),
  q(/home/phil/aaa/btreeBlock/verilog/find/2/vivado/reports/methodology.rpt),
  q(/home/phil/aaa/btreeBlock/verilog/find/2/vivado/reports/power.rpt),
  q(/home/phil/aaa/btreeBlock/verilog/find/2/vivado/reports/timing_route.rpt),
  q(/home/phil/aaa/btreeBlock/verilog/find/2/vivado/reports/timing_summary.rpt),
  q(/home/phil/aaa/btreeBlock/verilog/find/2/vivado/reports/utilization.rpt),
  q(/home/phil/aaa/btreeBlock/verilog/put/1/vivado/reports/bus_skew.rpt),
  q(/home/phil/aaa/btreeBlock/verilog/put/1/vivado/reports/cdc.rpt),
  q(/home/phil/aaa/btreeBlock/verilog/put/1/vivado/reports/clock_interaction.rpt),
  q(/home/phil/aaa/btreeBlock/verilog/put/1/vivado/reports/control.rpt),
  q(/home/phil/aaa/btreeBlock/verilog/put/1/vivado/reports/control_sets.rpt),
  q(/home/phil/aaa/btreeBlock/verilog/put/1/vivado/reports/design_analysis.rpt),
  q(/home/phil/aaa/btreeBlock/verilog/put/1/vivado/reports/drc.rpt),
  q(/home/phil/aaa/btreeBlock/verilog/put/1/vivado/reports/fanout.rpt),
  q(/home/phil/aaa/btreeBlock/verilog/put/1/vivado/reports/high_fanout_nets.rpt),
  q(/home/phil/aaa/btreeBlock/verilog/put/1/vivado/reports/methodology.rpt),
  q(/home/phil/aaa/btreeBlock/verilog/put/1/vivado/reports/power.rpt),
  q(/home/phil/aaa/btreeBlock/verilog/put/1/vivado/reports/timing.rpt),
  q(/home/phil/aaa/btreeBlock/verilog/put/1/vivado/reports/timing_route.rpt),
  q(/home/phil/aaa/btreeBlock/verilog/put/1/vivado/reports/timing_summary.rpt),
  q(/home/phil/aaa/btreeBlock/verilog/put/1/vivado/reports/utilization.rpt),

]}
