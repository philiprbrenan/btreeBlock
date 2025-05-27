#!/usr/bin/perl -I/home/phil/perl/cpan/DataTableText/lib/
#-------------------------------------------------------------------------------
# Copy Btree Silicon compiler output held in a zip file on the remote server
# Philip R Brenan at gmail dot com, Appa Apps Ltd Inc., 2025
#-------------------------------------------------------------------------------
use v5.38;
use warnings FATAL => qw(all);
use strict;
use Carp;
use Data::Dump qw(dump);
use Data::Table::Text qw(:all);

my $home   = q(/home/phil/btreeBlock/custom/);                                  # Home folder
my $date   = dateTimeStamp;                                                     # Separate runs
my $source = q(/home/phil/aaa/sc.zip);                                          # Source file after execution on remote server
my $target = fpe qw(/home/phil/btreeBlock/custom/), $date, qw(zip);             # Local target

my $cmd    = qq(cp $source "$target");                                          # Copy the zip file from ht e remote server
say STDERR qq($cmd);
say STDERR qx($cmd);
