#!/usr/bin/perl -I/home/phil/perl/cpan/DataTableText/lib/                                                                  my $input =
#-------------------------------------------------------------------------------
# Classify the instructions in some assembler code
# Philip R Brenan at gmail dot com, Appa Apps Ltd Inc., 2025
#-------------------------------------------------------------------------------
use v5.38;
use warnings FATAL => qw(all);
use strict;
use Carp;
use Data::Dump qw(dump);
use Data::Table::Text qw(:all);

my $asm = q(/home/phil/btreeBlock/c/btree.asm);                                 # Code to classify
my $jsm = q(/home/phil/btreeBlock/c/btree.jasm);                                # Classified code

my %i;
my %t;
my @j;

my @l = readFile $asm;                                                          # Each line of assembler
#   @l = splice @l, 121;
#  $#l = 50;

for my $l(@l)                                                                   # Each line of assembler
 {$l =~ s(\A\s+|\s+\Z) ()g;
  $l =~ s([\t,])       ( )g;
  $l =~ s(\bsext\.w\b) (sextw)g;

  if ($l =~ m(\A\.?((\w|\.)+):\Z)i)                                             # Label
   {push @j, qq(r.new Label("$1"););
    next;
   }

  next  if $l =~ m(\A\.);                                                       # Directive

  my @l = split /\s+/, $l;                                                      # Instruction
  my $op = shift @l;
  $i{$l}++;                                                                     # Statistics
  $t{$op}++;

  my ($t, $s1, $s2) = @l;                                                       # Parse instruction

  if ($op =~ m(lui)i)                                                           # Lui
   {if    ($s1 =~ m(\A%hi\(_impure_ptr\)\Z)is)                                  # Lui hi with no operand assume zero
     {push @j, qq{r.$op($t, true, 0);};
     }
    elsif ($s1 =~ m(\A%hi\(\.?(\w+)\)\Z)is)                                      # Lui hi label
     {push @j, qq{r.$op($t, true, label("$1"));};
     }
    elsif ($s1 =~ m(\A%hi\((.*?)\)\Z)is)                                        # Lui hi function
     {push @j, qq{r.$op($t, true, label("$1"));};
     }
    else
     {push @j, qq{r.$op($t, $s1); /* AAAA */};
     }
   }
  elsif ($op =~ m(ld)i)                                                         # Ld
   {if    ($s1 =~ m(\A%lo\(_impure_ptr\)\((\w+)\)\Z)is)
     {push @j, qq{r.$op($t, false,  $1);};
     }
    elsif ($s1 =~ m(\A(\d+)\((\w+)\)\Z)is)
     {push @j, qq{r.$op($t, $2, $1);};
     }
    else
     {push @j, qq{r.$op($t, $s1); /* BBBB */};
     }
   }
  elsif ($op =~ m(addi)i)                                                       # addi
   {if    ($s2 =~ m(\A%lo\(\.?(\w+)\)\Z)is)
     {push @j, qq{r.$op($t, $s1, false, label("$1"));};
     }
    elsif ($s2 =~ m(\A%lo\((.*?)\)\Z)is)
     {push @j, qq{r.$op($t, $s1, false, label("$1"));};
     }
    else
     {push @j, qq{r.$op($t, $s1, $s2);};
     }
   }
  elsif ($op =~ m(sb|sbu|sw|sd)i)                                               # Store
   {if    ($s1 =~ m(\A%lo\(\.?(\w+)\+(\d+)\)\((\w+)\)\Z)is)
     {push @j, qq{r.$op($t, false, label("$1"), $2, $3);};
     }
    elsif ($s1 =~ m(\A%lo\(\.?(\w+)\)\((\w+)\)\Z)is)
     {push @j, qq{r.$op($t, false, label("$1"), $2);};
     }
    elsif  ($s1 =~ m/\A(-?\d+)\((\w+)\)\Z/is)
     {push @j, qq{r.$op($t, $1, $2);};
     }
    else
     {push @j, qq/r.$op(/.join(",", @l)."); /* DDDD */";
     }
   }
  elsif ($op =~ m(lb|lbu|lw|ld)i)                                        # Load
   {if     ($s1 =~ m/\A%lo\((\w+)\+(\d+)\)\((\w+)\)\Z/is)
     {push @j, qq{r.$op($t, false, label("$1"), $2, $3);};
     }
    elsif ($s1 =~ m/\A%lo\((\w+)\)\((\w+)\)\Z/is)
     {push @j, qq{r.$op($t, false, label("$1"), $2);};
     }
    elsif     ($s1 =~ m/\A(-?\d+)\((\w+)\)\Z/is)
     {push @j, qq{r.$op($t, $1, $2);};
     }
    else
     {push @j, qq/r.$op(/.join(",", @l)."); /* EEEE */";
     }
   }
  elsif ($op =~ m(j)i)                                                          # j
   {if    ($t =~ m(\A\.?(\w+)\Z)is)
     {push @j, qq{r.$op("$1");};
     }
    else
     {push @j, qq/r.$op(/.join(",", @l).");";
     }
   }
  elsif ($op =~ m(call)i)                                                       # call
   {push @j, qq{r.$op("$t");};
   }
  elsif ($op =~ m(b(eq|ge|gt|le|lt|ne))i)                                       # branch
   {my $L = $s2 =~ s(\A.) ()ir;
     push @j, qq{r.$op($t, $s1, label("$L"));};
   }
  elsif ($op =~ m(tail)i)                                                       # tail
   {push @j, qq{r.$op("$t");};
   }
  else
   {push @j, qq/r.$op(/.join(",", @l).");";
   }
 }

owf($jsm, join "\n", map {"    $_"} @j);
say STDERR "Instr ", scalar keys %i;
say STDERR "Ops   ", scalar keys %t;
