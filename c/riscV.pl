#!/usr/bin/perl -I/home/phil/perl/cpan/DataTableText/lib/
#-------------------------------------------------------------------------------
# Btree in RiscV assembler
# Philip R Brenan at gmail dot com, Appa Apps Ltd Inc., 2025
#-------------------------------------------------------------------------------
use v5.38;
use warnings FATAL => qw(all);
use strict;
use Carp;
use Data::Table::Text qw(:all);

my $jpl = q(/home/phil/btreeBlock/c/riscV.java);                                # Input java skeleton
my $Jpl = q(/home/phil/btreeBlock/c/RiscV.java);                                # Output java
my $asm = q(/home/phil/btreeBlock/c/btree.asm);                                 # Code to classify
my $jsm = q(/home/phil/btreeBlock/c/btree.jasm);                                # Java assembler
my $cpl = q(/home/phil/btreeBlock/c/btree.c);                                   # Btree  algorithm in C so we can convert ot to RiscV assember using gcc

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

#define assert_checks
#include "$c"
int main() {return ${name}_tests();}
END

  system("gcc -fmax-errors=7 -Wall -Wextra -O0 -g3 -I. -o $name $m && timeout 10s ./$name");
  unlink $name, $m;
 }

sub translateRiscVToJava()                                                      # Translate RiscV assembler code verion of btree algorithm to java
 {my %i;                                                                        # Instruction text
  my %t;                                                                        # Instruction op code
  my @L;                                                                        # Labels
  my @j;                                                                        # Corresponding Java

  my sub ll($label) {$label =~ s([.]) (_)igs; "\$$label"}                       # Make a label into a valid java name

  system("riscv64-unknown-elf-gcc -S -I. -I/usr/include/newlib/ -o $asm $cpl"); # Compile source code for nbtree to produce RiscV code

  my @l = readFile $asm;                                                        # Each line of assembler

  for my $l(@l)                                                                 # Each line of assembler
   {last if $l =~ m(\Atests_passed:);                                           # Tests after this point
    $l =~ s(\A\s+|\s+\Z) ()g;
    $l =~ s([\t,])       ( )g;
    $l =~ s(\bsext\.w\b) (sextw)g;

    if ($l =~ m(\A\.?((\w|\.)+):\Z)i)                                           # Label
     {push @L, $1;
      push @j, ll($1).qq{.set();};
      next;
     }

    next  if $l =~ m(\A\.);                                                     # Directive

    my @l = split /\s+/, $l;                                                    # Instruction
    my $op = shift @l;
    $i{$l}++;                                                                   # Statistics
    $t{$op}++;

    my ($t, $s1, $s2) = @l;                                                     # Parse instruction

    if ($op =~ m(lui)i)                                                         # Lui
     {if    ($s1 =~ m(\A%hi\(_impure_ptr\)\Z)is)                                # Lui hi with no operand assume zero
       {push @j, qq{$op($t, true, 0);            /* AAAA */};
       }
      elsif ($s1 =~ m(\A%hi\(\.?(\w+)\)\Z)is)                                   # Lui hi label
       {my $L = ll($1);
        push @j, qq{$op($t, true, $L)            /* BBBB */;};
       }
      elsif ($s1 =~ m(\A%hi\((.*?)\+(\d+)\)\Z)is)                               # Lui hi function + offset
       {my $L = ll($1);
        push @j, qq{$op($t, true, $L, $2);       /* CCCC */};
       }
      elsif ($s1 =~ m(\A%hi\((.*?)\)\Z)is)                                      # Lui hi function
       {my $L = ll($1);
        push @j, qq{$op($t, true, $L);           /* DDDD */};
       }
      else
       {push @j, qq{$op($t, $s1);                /* EEEE */};
       }
     }
    elsif ($op =~ m(addi)i)                                                     # addi
     {if    ($s2 =~ m(\A%lo\(\.?(\w+)\)\Z)is)
       {my $L = ll($1);
        push @j, qq{$op($t, $s1, false, $L);     /* IIII */};
       }
      elsif ($s2 =~ m(\A%lo\((.*?)\+(\d+)\)\Z)is)
       {my $L = ll($1);
        push @j, qq{$op($t, $s1, false, $L, $2); /* JJJJ */};
       }
      elsif ($s2 =~ m(\A%lo\((.*?)\)\Z)is)
       {my $L = ll($1);
        push @j, qq{$op($t, $s1, false, $L);     /* KKKK */};
       }
      else
       {push @j, qq{$op($t, $s1, $s2);           /* LLLL */};
       }
     }
    elsif ($op =~ m(ld)i)                                                       # Ld
     {if    ($s1 =~ m(\A%lo\(_impure_ptr\)\((\w+)\)\Z)is)
       {push @j, qq{$op($t, false,  $1);         /* FFFF */};
       }
      elsif ($s1 =~ m(\A-?(\d+)\((\w+)\)\Z)is)
       {push @j, qq{$op($t, $2, $1);             /* GGGG */};
       }
      else
       {push @j, qq{$op($t, $s1);                /* HHHH */};
       }
     }
    elsif ($op =~ m(sb|sbu|sw|sd)i)                                             # store
     {if    ($s1 =~ m(\A%lo\(\.?(\w+)\+(\d+)\)\((\w+)\)\Z)is)
       {my $L = ll($1);
        push @j, qq{$op($t, false, $L, $2, $3);  /* MMMM */};
       }
      elsif ($s1 =~ m(\A%lo\(\.?(\w+)\)\((\w+)\)\Z)is)
       {my $L = ll($1);
        push @j, qq{$op($t, false, $L, $2);      /* NNNN */};
       }
      elsif  ($s1 =~ m/\A(-?\d+)\((\w+)\)\Z/is)
       {push @j, qq{$op($t, $1, $2);             /* OOOO */};
       }
      else
       {push @j, qq/$op(/.join(",", @l).");      /* PPPP */";
       }
     }
    elsif ($op =~ m(lb|lbu|lw|ld)i)                                             # load
     {if     ($s1 =~ m/\A%lo\((\w+)\+(\d+)\)\((\w+)\)\Z/is)
       {my $L = ll($1);
        push @j, qq{$op($t, false, $L, $2, $3);  /* QQQQ */};
       }
      elsif ($s1 =~ m/\A%lo\((\w+)\)\((\w+)\)\Z/is)
       {my $L = ll($1);
        push @j, qq{$op($t, false, $L, $2);      /* RRRR */};
       }
      elsif     ($s1 =~ m/\A(-?\d+)\((\w+)\)\Z/is)
       {push @j, qq{$op($t, $1, $2);             /* SSSS */};
       }
      else
       {push @j, qq/$op(/.join(",", @l).");      /* TTTT */";
       }
     }
    elsif ($op =~ m(jr)i)                                                       # jr
     {push @j, qq{$op($t);                       /* UUUU */};
     }
    elsif ($op =~ m(j)i)                                                        # j
     {my $L = ll($t =~ s(\A.) ()ir);
      push @j, qq{$op($L);                       /* WWWW */};
     }
    elsif ($op =~ m(call|tail)i)                                                # call
     {my $L = ll($t);
      push @j, qq{$op($L);                      /* XXXX */};
     }
    elsif ($op =~ m(b(eq|ge|gt|le|lt|ne))i)                                     # branch
     {my $L = ll($s2 =~ s(\A.) ()ir);
      push @j, qq{$op($t, $s1, $L);             /* YYYY */};
     }
    elsif ($op =~ m(seqz)i)                                                     # seqz
     {push @j, qq{$op($t, $s1);                 /* YY11 */};
     }
    else
     {push @j, qq/$op(/.join(",", @l).");        /* ZZZZ */";
     }
   }

  say STDERR "Instr ", scalar keys %i;
  say STDERR "Ops   ", scalar keys %t;

  my @J;
  push @J,  map {q(  ProgramDM.Label ).ll($_).qq( = P.new Label();)} @L;        # Put all the labels first
  push @J, "  void test_btree1() {";
  push @J, qq(    $j[$_]) for 1..@j/2;
  push @J, "   }";
  push @J, "  void test_btree2() {";
  push @J, qq(    $j[$_]) for @j/2+1..$#j;
  push @J, "   }";

  my $J = join "\n", @J;                                                        # The java code

  my $s = readFile $jpl;                                                        # Java skeleton
  $s =~ s(\nRiscV\n) ($J)is;
  owf($Jpl, join "\n", $s);
 }

translate(qw(leaf),   8, q(int), q(char));                                      # Leaf and branch stucks which do not need to be the same
translate(qw(branch), 8, q(int), q(int));
translateRiscVToJava();                                                         # Embed the RiscV assembler in Java

unlink $asm, $jsm, qw(branch.c leaf.c);                                         # Remove generated files
