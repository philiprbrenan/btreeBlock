#!/usr/bin/perl -I/home/phil/perl/cpan/DataTableText/lib/
#-------------------------------------------------------------------------------
# Use Vivado to synthesize a verilog project
# Philip R Brenan at gmail dot com, Appa Apps Ltd Inc., 2024
#-------------------------------------------------------------------------------
use v5.38;
use warnings FATAL => qw(all);
use strict;
use Carp;
use Data::Dump qw(dump);
use Data::Table::Text qw(:all);

my $home    = "/home/phil/btreeBlock/";
my $project = "put";
my $folder  = fpd $home, qw(vivado), $project;
my $vivado  = q(/home/phil/xilinx/Vivado/2024.2/bin/vivado);

my @sources = grep {!/machine|vivado/} searchDirectoryTreesForMatchingFiles $home, qw(.v .vh);

for my $s(@sources)
 {say STDERR "Copy $s";
  my $t = fpf $folder, fne($s);
  copyFile($s, $t);
 }

my $synthesizeTcl = owf(fpf($folder, "synthesize.tcl"), <<END);
read_verilog $project.v
synth_design -name $project  -part xc7z020clg484-1 -include_dirs . -top $project
report_utilization  -file utilization.txt
END

say STDERR qq(cd $folder; ulimit -v 2000000; cpulimit -m -l 50 $vivado -mode tcl -script $synthesizeTcl);
