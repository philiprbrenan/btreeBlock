#!/usr/bin/perl -I/home/phil/perl/cpan/DataTableText/lib/
#-------------------------------------------------------------------------------
# Btree in x64 assembler
# Philip R Brenan at gmail dot com, Appa Apps Ltd Inc., 2025
#-------------------------------------------------------------------------------
use v5.38;
use warnings FATAL => qw(all);
use strict;
use Carp;
use Data::Table::Text qw(:all);
use Data::Dump qw(dump);

my $jpl = q(riscV.java);                                                        # Input java skeleton
my $Jpl = q(RiscV.java);                                                        # Output java
my $asm = q(btree.asm);                                                         # Code to classify
my $jsm = q(btree.jasm);                                                        # Java assembler
my $cpl = q(btree.c);                                                           # Btree  algorithm in C so we can convert ot to RiscV assember using gcc
my $sde = q(/home/phil/zeasal/sde/sde64);                                       # Intel emulator
my $bld = q(/home/phil/btreeBlock/c/build/);                                    # Build folder
my $run = q(/home/phil/btreeBlock/c/run/);                                      # Run folder

makePath($_) for $bld, $run;

sub executeBtreeInSde()                                                         # Run SDE on current btree
 {my $o = fpf $bld, qq(btree);
  my $a = fpe $bld, qw(btree asm);
  my $t = fpe $run, qw(btree log);
  my $g = "gcc -masm=intel -fomit-frame-pointer  -mno-sse   -fmax-errors=7 ".
          "-g3 -O0 -rdynamic -Wall -Wextra -I. ";
  my $c = "$g    -o $o btree.c && ".
          "timeout 10s ".
          "$sde -odebugtrace $t  -- $o";
  say STDERR $c;
  system($c);

  my $A = "$g -fverbose-asm -S -o $a btree.c";
  say STDERR $A;
  system($A);
  #unlink $name, $m;
 }
executeBtreeInSde(); exit;

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

  my $gc = "gcc -masm=intel -fomit-frame-pointer  -mno-sse   -fmax-errors=7 ".
          " -Wall -Wextra -I. -o $name $m && timeout 10s $sde -- ./$name";
  say STDERR $gc;
  system($gc);
  #unlink $name, $m;
 }

sub translateToJava()                                                      # Translate RiscV assembler code verion of btree algorithm to java
 {my %i;                                                                        # Instruction text
  my %t;                                                                        # Instruction op code
  my @L;                                                                        # Labels
  my @j;                                                                        # Corresponding Java

  my sub ll($label) {$label =~ s([.]) (_)igs; "\$$label"}                       # Make a label into a valid java name

  my $c = "gcc -masm=intel -fomit-frame-pointer  -mno-sse  -S -I. -o $asm $cpl";            # Compile source code for nbtree to produce RiscV code
  say STDERR $c;
  system($c);

  my @la = readFile $asm;                                                       # Each line of assembler
  my $in = 0;                                                                   # The number of each instruction

  for my $la(@la)                                                               # Each line of assembler
   {last if $la =~ m(\Atests_passed:);                                          # Tests after this point
    $la =~ s(\A\s+|\s+\Z) ()g;
    $la =~ s([\t,])       ( )g;
    $la =~ s(\bsext\.w\b) (sextw)g;

    if ($la =~ m(\A\.?((\w|\.)+):\Z)i)                                          # Label
     {push @L, $1;
      push @j, ll($1).qq{.set();};
      next;
     }

    next  if $la =~ m(\A\.);                                                    # Directive

    my @l = split /\s+/, $la;                                                   # Instruction
    my $op = shift @l;
    $i{$la}++;                                                                  # Statistics
    $t{$op}++;

    my ($t, $s1, $s2) = @l;                                                     # Parse instruction
    my $j; my $x;                                                                      # Corresonding line of java

    if ($op =~ m(lui)i)                                                         # Lui
     {if    ($s1 =~ m(\A%hi\(_impure_ptr\)\Z)is)                                # Lui hi with no operand assume zero
       {$j =  qq{$op($t, true, 0);};            $x = q(A);
       }
      elsif ($s1 =~ m(\A%hi\(\.?(\w+)\)\Z)is)                                   # Lui hi label
       {my $L = ll($1);
        $j =  qq{$op($t, true, $L);};           $x = q(B);
       }
      elsif ($s1 =~ m(\A%hi\((.*?)\+(\d+)\)\Z)is)                               # Lui hi function + offset
       {my $L = ll($1);
        $j =  qq{$op($t, true, $L, $2);};       $x = q(C);
       }
      elsif ($s1 =~ m(\A%hi\((.*?)\)\Z)is)                                      # Lui hi function
       {my $L = ll($1);
        $j =  qq{$op($t, true, $L);};           $x = q(D);
       }
      else
       {$j =  qq{$op($t, $s1);};                $x = q(E);
       }
     }
    elsif ($op =~ m(addi)i)                                                     # addi
     {if    ($s2 =~ m(\A%lo\(\.?(\w+)\)\Z)is)
       {my $L = ll($1);
        $j =  qq{$op($t, $s1, false, $L);};     $x = q(F);
       }
      elsif ($s2 =~ m(\A%lo\((.*?)\+(\d+)\)\Z)is)
       {my $L = ll($1);
        $j =  qq{$op($t, $s1, false, $L, $2);}; $x = q(G);
       }
      elsif ($s2 =~ m(\A%lo\((.*?)\)\Z)is)
       {my $L = ll($1);
        $j =  qq{$op($t, $s1, false, $L);};     $x = q(H);
       }
      else
       {$j =  qq{$op($t, $s1, $s2);};           $x = q(I);
       }
     }
    elsif ($op =~ m(ld)i)                                                       # Ld
     {if    ($s1 =~ m(\A%lo\(_impure_ptr\)\((\w+)\)\Z)is)
       {$j =  qq{$op($t, false,  $1);};         $x = q(J);
       }
      elsif ($s1 =~ m(\A-?(\d+)\((\w+)\)\Z)is)
       {$j =  qq{$op($t, $2, $1);};             $x = q(K);
       }
      else
       {$j =  qq{$op($t, $s1);};                $x = q(L);
       }
     }
    elsif ($op =~ m(sb|sbu|sw|sd)i)                                             # store
     {if    ($s1 =~ m(\A%lo\(\.?(\w+)\+(\d+)\)\((\w+)\)\Z)is)
       {my $L = ll($1);
        $j =  qq{$op($t, false, $L, $2, $3);};  $x = q(M);
       }
      elsif ($s1 =~ m(\A%lo\(\.?(\w+)\)\((\w+)\)\Z)is)
       {my $L = ll($1);
        $j =  qq{$op($t, false, $L, $2);};      $x = q(N);
       }
      elsif  ($s1 =~ m/\A(-?\d+)\((\w+)\)\Z/is)
       {$j =  qq{$op($t, $1, $2);};             $x = q(O);
       }
      else
       {$j =  qq/$op(/.join(",", @l).");";      $x = q(P);
       }
     }
    elsif ($op =~ m(lb|lbu|lw|ld)i)                                             # load
     {if     ($s1 =~ m/\A%lo\((\w+)\+(\d+)\)\((\w+)\)\Z/is)
       {my $L = ll($1);
        $j =  qq{$op($t, false, $L, $2, $3);}; $x = q(R);
       }
      elsif ($s1 =~ m/\A%lo\((\w+)\)\((\w+)\)\Z/is)
       {my $L = ll($1);
        $j =  qq{$op($t, false, $L, $2);};     $x = q(S);
       }
      elsif     ($s1 =~ m/\A(-?\d+)\((\w+)\)\Z/is)
       {$j =  qq{$op($t, $1, $2);};            $x = q(T);
       }
      else
       {$j =  qq/$op(/.join(",", @l).");";     $x = q(U);
       }
     }
    elsif ($op =~ m(jr)i)                                                       # jr
     {$j =  qq{$op($t);};                      $x = q(W);
     }
    elsif ($op =~ m(j)i)                                                        # j
     {my $L = ll($t =~ s(\A.) ()ir);
      $j =  qq{$op($L);};                      $x = q(X);
     }
    elsif ($op =~ m(call|tail)i)                                                # call
     {my $L = ll($t);
      $j =  qq{$op($L);};                      $x = q(Y);
     }
    elsif ($op =~ m(b(eq|ge|gt|le|lt|ne))i)                                     # branch
     {my $L = ll($s2 =~ s(\A.) ()ir);
      $j =  qq{$op($t, $s1, $L);};             $x = q(Z);
     }
    elsif ($op =~ m(seqz)i)                                                     # seqz
     {$j =  qq{$op($t, $s1);};                 $x = q(a);
     }
    else
     {$j =  qq/$op(/.join(",", @l).");";       $x = q(b);
     }

    push @j, sprintf "/* %4d %s */ %s", $in++, $x , $j;
   }

  say STDERR "Instr ", scalar keys %i;
  say STDERR "Ops   ", scalar keys %t;
  #say STDERR "AAAA ", dump(\%t); exit;

  my @J;
  push @J,  map {q(  ProgramDM.Label ).ll($_).qq( = P.new Label("$_");)} @L;    # Put all the labels first
  push @J, "  void test_btree1() {";
  push @J, qq(    $j[$_]) for 0..@j/2;
  push @J, "   }";
  push @J, "  void test_btree2() {";
  push @J, qq(    $j[$_]) for @j/2+1..@j-1;
  push @J, "   }";

  my $J = join "\n", @J;                                                        # The java code

  my $s = readFile $jpl;                                                        # Java skeleton
  $s =~ s(\n//RiscV\n) ($J)is;                                                  # Insert generated program
  owf($Jpl, join "\n", $s);                                                     # Save generated program
 }

translate(qw(leaf),   8, q(int), q(char));                                      # Leaf and branch stucks which do not need to be the same
translate(qw(branch), 8, q(int), q(int));
translateToJava();                                                         # Embed the RiscV assembler in Java

#unlink $asm, $jsm, qw(branch.c leaf.c);                                         # Remove generated files
#unlink $asm, $jsm;

makePath(q(Classes));
system("javac -g -d Classes/ -cp Classes/ ../*.java RiscV.java && java -ea -cp Classes/ com.AppaApps.Silicon.RiscV");
