#!/usr/bin/perl
#-------------------------------------------------------------------------------
# Run vhdl
# Philip R Brenan at gmail dot com, Appa Apps Ltd Inc., 2021
#-------------------------------------------------------------------------------
use warnings FATAL => qw(all);
use strict;
use Carp;
use Data::Dump qw(dump);
use Data::Table::Text qw(:all);
use feature qw(say current_sub);

my $home = currentDirectory;                                                    # Local files
my $vhdl = fpe $home, qw(cpu    vhdl);                                          # Source file

lll qx(ghdl -a cpu.vhdl);
lll qx(ghdl -e cpu);
my @r = qx(ghdl -r cpu);                                                        # Execute vhdl

say STDERR join '', @r;

my $title = sub                                                                 # Title of recently executed program
 {my @t;
  my $p;
  for my $l(readFile $vhdl)
   {if ($l =~ m(\A.*Load program:\s+(.*)\s+\Z)i)
     {push @t, $1;
     }
    if ($l =~ m(\A\s+constant\s+run\s+:\s+integer\s+:=\s+(\d);\Z)i)
     {$p = $1;
     }
   }
  confess "No program number" unless $p;
  $t[$p-1]
 }->();

if (0)                                                                          # Create memory map in hex
 {my @m;

  for my $r(@r)                                                                 # Parse memory output
   {if ($r =~ m(\A.*Memory: (\d+)=(\d+)))
     {$m[$1] = $2;
     }
   }

  my @M = '   0  ';                                                             # Print a memory table
  for my $m(keys @m)
   {my $i16 = $m % 16 == 0 && $m > 0;
    my $i8  = $m % 8  == 0 && $m > 0 && !$i16;
    my $i4  = $m % 4  == 0 && $m > 0 && !$i8;
    push @M, sprintf("%4x", int $m / 16) if  $i16;
    $M[-1] .= '  '                       if !$i8 and $i4;
    $M[-1] .= "    "                     if  $i8;
    $M[-1] .= $m[$m] ? sprintf("  %4x", $m[$m]) : "     _";
   }

  my $H = <<END;                                                                # Header line
           0     1     2     3       4     5     6     7         8     9     a     b       c     d     e     f
END

  my $o = join "\n", $H, @M;
  say STDERR $o;
  my $f = owf(fpe($home, $title, q(txt)), $o);                                  # Output file showing memory trace
  say STDERR "Output in file:\n$f";
 }

if (1)                                                                          # Create memory map in decimal
 {my @m;

  for my $r(@r)                                                                 # Parse memory output
   {if ($r =~ m(\A.*Memory: (\d+)=(\d+)))
     {$m[$1] = $2;
     }
   }

  my @M = '    0';                                                             # Print a memory table
  for my $m(keys @m)
   {my $i10 = $m % 10 == 0 && $m > 0;
    my $i5  = $m % 5  == 0 && $m > 0 && !$i10;
    push @M, sprintf("%4d0", int $m / 10) if $i10;
    $M[-1] .= '  '                       if $i5;
    $M[-1] .= $m[$m] ? sprintf("  %4x", $m[$m]) : "     _";
   }

  my $H = <<END;                                                                # Header line
          0     1     2     3     4       5     6     7     8     9
END

  my $o = join "\n", $H, @M;
  say STDERR $o;
  my $f = owf(fpe($home, $title, q(txt)), $o);                                  # Output file showing memory trace
  say STDERR "Output in file:\n$f";
 }
