#!/usr/bin/perl
use v5.30;
use warnings FATAL => qw(all);
use strict;
use Carp;

my $home    =  $ENV{HOME};                                                      # Home folder
my $project = "$home/btreeBlock/vivado/";                                           # Project we wish to compile
my $vivado  = "$home/Vivado/2024.2/";                                               # Location of vivaldo

die "No such path: $vivado" if ! -d $vivado;

say STDERR qx($vivado/bin/vivado -mode batch -source $project/synthesize.tcl);
