#!/usr/bin/perl
use v5.30;
use warnings FATAL => qw(all);
use strict;
use Carp;

my $home      =  $ENV{HOME};                                                    # Home folder
my $project   = "$home/btreeBlock/vivado/";                                     # Project we wish to compile
my $synthesis = "$project/synthesis.tcl";                                       # Location of vivaldo
my $vivado    = "$home/Vivado/2024.2/";                                         # Location of vivaldo
my $vivadoX   = "$home/Vivado/2024.2/bin/vivado";                               # Location of vivaldo executable

die "No such file: $synthesis" unless -f $synthesis;
die "No such path: $vivado"    unless -d $vivado;
die "No such file: $vivadoX"   unless -f $vivadoX;

say STDERR qx($vivadoX -mode batch -source $synthesis);
