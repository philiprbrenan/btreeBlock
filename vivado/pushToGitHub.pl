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

say STDERR timeStamp,  " Recover reports";

if (0)
 {push my @files, searchDirectoryTreesForMatchingFiles($source, @ext);          # Possible source files
         @files = grep {!m(vivado/pins/)} @files;
  say STDERR "AAAA ", dump(\@files);
  exit;
 }

for my $s(&files()->@*)                                                         # Target each source file
 {say STDERR $s;
  my $t = swapFilePrefix $s, $source,  $target;
  copyFile($s, $t) if -e $s;
 }

sub files() {return [
  "/home/phil/aaa/btreeBlock/verilog/delete/1/tests.txt",
  "/home/phil/aaa/btreeBlock/verilog/delete/1/trace.txt",
  "/home/phil/aaa/btreeBlock/verilog/delete/1/traceJava.txt",
  "/home/phil/aaa/btreeBlock/verilog/delete/2/tests.txt",
  "/home/phil/aaa/btreeBlock/verilog/delete/2/trace.txt",
  "/home/phil/aaa/btreeBlock/verilog/delete/2/traceJava.txt",
  "/home/phil/aaa/btreeBlock/verilog/delete/3/tests.txt",
  "/home/phil/aaa/btreeBlock/verilog/delete/3/trace.txt",
  "/home/phil/aaa/btreeBlock/verilog/delete/3/traceJava.txt",
  "/home/phil/aaa/btreeBlock/verilog/delete/4/tests.txt",
  "/home/phil/aaa/btreeBlock/verilog/delete/4/trace.txt",
  "/home/phil/aaa/btreeBlock/verilog/delete/4/traceJava.txt",
  "/home/phil/aaa/btreeBlock/verilog/delete/5/tests.txt",
  "/home/phil/aaa/btreeBlock/verilog/delete/5/trace.txt",
  "/home/phil/aaa/btreeBlock/verilog/delete/5/traceJava.txt",
  "/home/phil/aaa/btreeBlock/verilog/delete/6/tests.txt",
  "/home/phil/aaa/btreeBlock/verilog/delete/6/trace.txt",
  "/home/phil/aaa/btreeBlock/verilog/delete/6/traceJava.txt",
  "/home/phil/aaa/btreeBlock/verilog/delete/7/tests.txt",
  "/home/phil/aaa/btreeBlock/verilog/delete/7/trace.txt",
  "/home/phil/aaa/btreeBlock/verilog/delete/7/traceJava.txt",
  "/home/phil/aaa/btreeBlock/verilog/delete/8/tests.txt",
  "/home/phil/aaa/btreeBlock/verilog/delete/8/trace.txt",
  "/home/phil/aaa/btreeBlock/verilog/delete/8/traceJava.txt",
  "/home/phil/aaa/btreeBlock/verilog/delete/9/tests.txt",
  "/home/phil/aaa/btreeBlock/verilog/delete/9/trace.txt",
  "/home/phil/aaa/btreeBlock/verilog/delete/9/traceJava.txt",
  "/home/phil/aaa/btreeBlock/verilog/delete/vivado/reports/bus_skew.rpt",
  "/home/phil/aaa/btreeBlock/verilog/delete/vivado/reports/cdc.rpt",
  "/home/phil/aaa/btreeBlock/verilog/delete/vivado/reports/clock_interaction.rpt",
  "/home/phil/aaa/btreeBlock/verilog/delete/vivado/reports/drc.rpt",
  "/home/phil/aaa/btreeBlock/verilog/delete/vivado/reports/methodology.rpt",
  "/home/phil/aaa/btreeBlock/verilog/delete/vivado/reports/power.rpt",
  "/home/phil/aaa/btreeBlock/verilog/delete/vivado/reports/utilization.rpt",
  "/home/phil/aaa/btreeBlock/verilog/find/2/tests.txt",
  "/home/phil/aaa/btreeBlock/verilog/find/2/trace.txt",
  "/home/phil/aaa/btreeBlock/verilog/find/2/traceJava.txt",
  "/home/phil/aaa/btreeBlock/verilog/find/vivado/reports/1.txt",
  "/home/phil/aaa/btreeBlock/verilog/find/vivado/reports/bus_skew.rpt",
  "/home/phil/aaa/btreeBlock/verilog/find/vivado/reports/cdc.rpt",
  "/home/phil/aaa/btreeBlock/verilog/find/vivado/reports/clock_interaction.rpt",
  "/home/phil/aaa/btreeBlock/verilog/find/vivado/reports/control_sets.rpt",
  "/home/phil/aaa/btreeBlock/verilog/find/vivado/reports/design_analysis.rpt",
  "/home/phil/aaa/btreeBlock/verilog/find/vivado/reports/drc.rpt",
  "/home/phil/aaa/btreeBlock/verilog/find/vivado/reports/high_fanout_nets.rpt",
  "/home/phil/aaa/btreeBlock/verilog/find/vivado/reports/methodology.rpt",
  "/home/phil/aaa/btreeBlock/verilog/find/vivado/reports/power.rpt",
  "/home/phil/aaa/btreeBlock/verilog/find/vivado/reports/statement/1/timing_summary.rpt",
  "/home/phil/aaa/btreeBlock/verilog/find/vivado/reports/statement/10/timing_summary.rpt",
  "/home/phil/aaa/btreeBlock/verilog/find/vivado/reports/statement/11/timing_summary.rpt",
  "/home/phil/aaa/btreeBlock/verilog/find/vivado/reports/statement/12/timing_summary.rpt",
  "/home/phil/aaa/btreeBlock/verilog/find/vivado/reports/statement/13/timing_summary.rpt",
  "/home/phil/aaa/btreeBlock/verilog/find/vivado/reports/statement/14/timing_summary.rpt",
  "/home/phil/aaa/btreeBlock/verilog/find/vivado/reports/statement/2/timing_summary.rpt",
  "/home/phil/aaa/btreeBlock/verilog/find/vivado/reports/statement/3/timing_summary.rpt",
  "/home/phil/aaa/btreeBlock/verilog/find/vivado/reports/statement/4/timing_summary.rpt",
  "/home/phil/aaa/btreeBlock/verilog/find/vivado/reports/statement/5/timing_summary.rpt",
  "/home/phil/aaa/btreeBlock/verilog/find/vivado/reports/statement/6/timing_summary.rpt",
  "/home/phil/aaa/btreeBlock/verilog/find/vivado/reports/statement/7/timing_summary.rpt",
  "/home/phil/aaa/btreeBlock/verilog/find/vivado/reports/statement/8/timing_summary.rpt",
  "/home/phil/aaa/btreeBlock/verilog/find/vivado/reports/statement/9/timing_summary.rpt",
  "/home/phil/aaa/btreeBlock/verilog/find/vivado/reports/timing_route.rpt",
  "/home/phil/aaa/btreeBlock/verilog/find/vivado/reports/timing_summary.rpt",
  "/home/phil/aaa/btreeBlock/verilog/find/vivado/reports/utilization.rpt",
  "/home/phil/aaa/btreeBlock/verilog/put/1/tests.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/1/trace.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/1/traceJava.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/10/tests.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/10/trace.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/10/traceJava.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/11/tests.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/11/trace.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/11/traceJava.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/12/tests.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/12/trace.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/12/traceJava.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/13/tests.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/13/trace.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/13/traceJava.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/14/tests.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/14/trace.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/14/traceJava.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/15/tests.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/15/trace.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/15/traceJava.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/16/tests.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/16/trace.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/16/traceJava.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/17/tests.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/17/trace.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/17/traceJava.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/18/tests.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/18/trace.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/18/traceJava.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/19/tests.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/19/trace.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/19/traceJava.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/2/tests.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/2/trace.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/2/traceJava.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/20/tests.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/20/trace.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/20/traceJava.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/3/tests.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/3/trace.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/3/traceJava.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/4/tests.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/4/trace.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/4/traceJava.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/5/tests.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/5/trace.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/5/traceJava.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/6/tests.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/6/trace.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/6/traceJava.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/7/tests.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/7/trace.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/7/traceJava.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/8/tests.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/8/trace.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/8/traceJava.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/9/tests.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/9/trace.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/9/traceJava.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/vivado/reports/1.txt",
  "/home/phil/aaa/btreeBlock/verilog/put/vivado/reports/bus_skew.rpt",
  "/home/phil/aaa/btreeBlock/verilog/put/vivado/reports/cdc.rpt",
  "/home/phil/aaa/btreeBlock/verilog/put/vivado/reports/clock_interaction.rpt",
  "/home/phil/aaa/btreeBlock/verilog/put/vivado/reports/drc.rpt",
  "/home/phil/aaa/btreeBlock/verilog/put/vivado/reports/methodology.rpt",
  "/home/phil/aaa/btreeBlock/verilog/put/vivado/reports/power.rpt",
  "/home/phil/aaa/btreeBlock/verilog/put/vivado/reports/utilization.rpt",
]}
