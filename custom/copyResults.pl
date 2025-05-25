#!/usr/bin/perl -I/home/phil/perl/cpan/DataTableText/lib/
#-------------------------------------------------------------------------------
# Copy Btree Silicon compiler output to GitHub for a given day
# Philip R Brenan at gmail dot com, Appa Apps Ltd Inc., 2025
#-------------------------------------------------------------------------------
#die "Turned off"
use v5.38;
use warnings FATAL => qw(all);
use strict;
use Carp;
use Data::Dump qw(dump);
use Data::Table::Text qw(:all);

my $home   = q(/home/phil/btreeBlock/custom/);                                 # Home folder
#my $date   = dateTimeStamp;
my $date   = dateStamp;
my $target = fpe qw(/home/phil/btreeBlock/custom/), $date, qw(zip);
#my $ds     = qq(/home/phil/aaa/btreeBlock/verilog/delete_verilog/1/siliconCompiler/build/delete_verilog/job0/delete_verilog.pkg.json);
#my $fs     = qq(/home/phil/aaa/btreeBlock/verilog/find_verilog/1/siliconCompiler/build/find_verilog/job0/find_verilog.pkg.json);
#my $ps     = qq(/home/phil/aaa/btreeBlock/verilog/put_verilog/1/siliconCompiler/build/put_verilog/job0/put_verilog.pkg.json);
#
#my $cmd    = qq(zip -j "$target" $ds $fs $ps);
#say STDERR qq($cmd);
#say STDERR qx($cmd);

my $cmd    = qq(cp /home/phil/aaa/sc.zip $target);
say STDERR qq($cmd);
say STDERR qx($cmd);
