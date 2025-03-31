#!/usr/bin/perl -I/home/phil/perl/cpan/DataTableText/lib/
#-------------------------------------------------------------------------------
# Extract leaf and branch definition from the general definition of a stuck
# Philip R Brenan at gmail dot com, Appa Apps Ltd Inc., 2025
#-------------------------------------------------------------------------------
use v5.38;
use warnings FATAL => qw(all);
use strict;
use Carp;
use Data::Table::Text qw(:all);

sub translate($name, $maxSize, $key, $data)                                     # Translate a generalized stuck into a leaf or a branch stuck
 {my $s = readFile q(stuck.c);                                                  # Read the source code
  my $Name = ucfirst $name;
  $s =~ s(stuck_) (${name}_)g;
  $s =~ s(Stuck_) (${Name}_)g;
  $s =~ s(Stuck)  (${Name})g;

  $s =~ s((#define leaf_maxSize\s+)\d+)  ($1$maxSize)g;                         # The maximum number of entries in the stuck.
  $s =~ s((#define leaf_keyType\s+)\w+)  ($1$key)g;                             # The type of a key
  $s =~ s((#define leaf_dataType\s+)\w+) ($1$data)g;                            # The type of a data item in a stuck

  $s =~ s((Leaf.maxSize:)\d+)            ($1$maxSize)g;                         # Tests

  my $c = owf(fpe($name, q(c)), $s);

  my $m = owf(fpe(qw(zzz c)), <<END);                                           # Test the code
#include "$c"
int main() {return ${name}_tests();}
END

  system("gcc -fmax-errors=7 -Wall -Wextra -O0 -g3 -I. -o $name $m && timeout 10s ./$name");
  unlink $name, $m;
 }

translate(qw(leaf),   8, q(int), q(char));
translate(qw(branch), 8, q(int), q(int));
