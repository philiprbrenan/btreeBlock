#!/usr/bin/perl -I/home/phil/perl/cpan/DataTableText/lib/
#-------------------------------------------------------------------------------
# Analyze coverage
# Philip R Brenan at gmail dot com, Appa Apps Ltd Inc., 2024
#-------------------------------------------------------------------------------
use v5.38;
use warnings FATAL => qw(all);
use strict;
use Carp;
use Data::Dump qw(dump);
use Data::Table::Text qw(:all);

my $source   = "/home/phil/btreeBlock/Btree.java";                              # File to analyze
my $coverage = "/home/phil/btreeBlock/coverage/coverage.txt";                   # Coverage of file
my $top      = 12;                                                              # How many most frequently executed to print

my @c = readFile $coverage;
my %c;
for my $c(@c)
 {my ($file, $method, $line, $count) = split /\s+/, $c;
  $c{$line+0} = [$count, $method];
 }

my @top = (sort { $c{$b}[0] <=> $c{$a}[0] } keys %c)[0..$top-1];                # Top most executed lines

say STDERR "Top $top most frequently executed lines:";
for my $l(@top)
 {say STDERR "$l: $c{$l}[1] $c{$l}[0]\n";
 }

my @s = readFile $source;                                                       # Lines not executed
my %s;
for my $i(1..@s)
 {my $s = $s[$i-1];
  next unless $s =~ m(z\(\););
  $s{$i}++;
 }

my $s = setDifference(\%s, \%c);
say STDERR "Lines not executed";
for my $l(sort {$a <=> $b} keys %$s)
 {say STDERR $l
}
