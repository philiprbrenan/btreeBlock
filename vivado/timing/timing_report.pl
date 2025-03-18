#!/usr/bin/perl -I/home/phil/perl/cpan/DataTableText/lib/
#-------------------------------------------------------------------------------
# Generate timing reports from latest checkpoint
# Philip R Brenan at gmail dot com, Appa Apps Ltd Inc., 2025
#-------------------------------------------------------------------------------
use v5.30;
use warnings FATAL => qw(all);
use strict;
use Carp;
use Data::Table::Text qw(:all);
use Time::HiRes qw(time);
use Data::Dump qw(dump);

my @files = searchDirectoryTreesForMatchingFiles("../../verilog/", qw(.rpt));   # All reports

my @a;                                                                          # Arrivale times

for my $f(@files)                                                               # Find statements still to be tested
 {if ($f =~ m(/(\w+)/vivado/timing_route/0*(\d+).rpt))                          # Project, key, statement
   {my ($p, $s) = ($1, $2);
    my  $t = readFile($f);
    if ($t =~ m(arrival time\s+-?(\S+)))
     {my $a = $1;
      #say STDERR "$p $s $a";
      push @a, $a;
     }
   }
 }

say STDERR dump max(@a);
