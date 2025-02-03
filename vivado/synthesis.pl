#!/usr/bin/perl
use v5.30;
use warnings FATAL => qw(all);
use strict;
use Carp;

my $home        = "~/btreeBlock/vivado/";
my $vivado_path = "~/Vivado/2024.2/";

die "No such path: $vivado_path" if ! -d $vivado_path;

# Run Vivado in batch mode with the Tcl script
say STDERR qx($vivado_path/bin/vivado -mode batch -source $home/synthesize.tcl);
