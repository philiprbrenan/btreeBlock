#!/usr/bin/perl -I/home/phil/perl/cpan/DataTableText/lib/
#-------------------------------------------------------------------------------
# Yosys + magoc on some verilog
# Philip R Brenan at gmail dot com, Appa Apps Ltd Inc., 2024
#-------------------------------------------------------------------------------
use v5.38;
use warnings FATAL => qw(all);
use strict;
use Carp;
use Data::Dump qw(dump);
use Data::Table::Text qw(:all);

my $project = q(delete);                                                        # Project
my $key     = q(2);                                                             # Key being processed by the project
my $home    = q(/home/phil/btreeBlock/);                                        # Home folder
my $source  = fpd $home, qw(verilog), $project, $key;                           # Source folder to be compiled
my $inc     = fpd $source, qw(includes);                                        # Include files
my $sv      = fpe $source, $project, q(v);                                      # Source verilog to be compiled
my $ym      = fpd $home, qw(ym);                                                # This folder
my $out     = fpe $ym, qw(edif), $project, qw(edif);                            # Output file

makePath $out;

system(qq(yosys -D SYNTHESIS=1 -p "read_verilog -sv -I $inc $sv; synth" -o $out)); # Run synthesis
