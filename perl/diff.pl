#!/usr/bin/perl -I/home/phil/perl/cpan/DataTableText/lib/
#-------------------------------------------------------------------------------
# Diff the trace files
# Philip R Brenan at gmail dot com, Appa Apps Ltd Inc., 2024
#-------------------------------------------------------------------------------
use v5.38;
use warnings FATAL => qw(all);
use strict;
use Carp;
use Data::Dump qw(dump);
use Data::Table::Text qw(:all);

my $project;
   $project = q(delete);
   $project = q(put);
   $project = q(find);

say STDERR qq(meld /home/phil/btreeBlock/trace/test_verilog_$project.txt /home/phil/btreeBlock/verilog/trace/$project/trace.txt);
