#!/usr/bin/perl -I/home/phil/perl/cpan/DataTableText/lib/
#-------------------------------------------------------------------------------
# Recover reports from Azure
# Philip R Brenan at gmail dot com, Appa Apps Ltd Inc., 2024
#-------------------------------------------------------------------------------
use warnings FATAL => qw(all);
use strict;
use Carp;
use Data::Dump qw(dump);
use Data::Table::Text qw(:all);
use GitHub::Crud qw(:all);
use feature qw(say current_sub);

my $source = q(/home/phil/aaa/btreeBlock/verilog/);                             # Source folder
my $target = q(/home/phil/btreeBlock/verilog/);                                 # Target folder

say STDERR "Recover reports";

my @files = &files();                                                           # Files to recover

for my $f(@files)                                                               # Target each source file
 {my ($s, $t) = @$f;
  if (-e $s)
   {say STDERR sprintf("%-72s  to  %-72s", $s, $t);
    copyFile($s, $t);
    say STDERR $@ if $@;
   }
  else
   {#say STDERR "Fail: $s";
   }
 }

sub files()                                                                     # Files associated with each project
 {my @f;
  my %P = eval(readFile(q(ranges.txt)))->%*;
  my %p = $P{projects}->%*;

  for my $p(sort keys %p)
   {my ($k, $s) = $p{$p}->@*;
    #push @f, map {[qq(${source}$p/$k/statement/$_/vivado/reports/timing_route.rpt),
    #       sprintf(qq(${target}$p/vivado/timing_route/%03d.rpt), $_)]} 0..$s;

    push @f, [qq(${source}$p/vivado/reports/1.txt),                  qq(${target}$p/vivado/reports/1.txt)];
    push @f, [qq(${source}$p/vivado/reports/bus_skew.rpt),           qq(${target}$p/vivado/reports/bus_skew.rpt)];
    push @f, [qq(${source}$p/vivado/reports/cdc.rpt),                qq(${target}$p/vivado/reports/cdc.rpt)];
    push @f, [qq(${source}$p/vivado/reports/clock_interaction.rpt),  qq(${target}$p/vivado/reports/clock_interaction.rpt)];
    push @f, [qq(${source}$p/vivado/reports/control.rpt),            qq(${target}$p/vivado/reports/control.rpt)];
    push @f, [qq(${source}$p/vivado/reports/control_sets.rpt),       qq(${target}$p/vivado/reports/control_sets.rpt)];
    push @f, [qq(${source}$p/vivado/reports/design_analysis.rpt),    qq(${target}$p/vivado/reports/design_analysis.rpt)];
    push @f, [qq(${source}$p/vivado/reports/drc.rpt),                qq(${target}$p/vivado/reports/drc.rpt)];
    push @f, [qq(${source}$p/vivado/reports/fanout.rpt),             qq(${target}$p/vivado/reports/fanout.rpt)];
    push @f, [qq(${source}$p/vivado/reports/high_fanout_nets.rpt),   qq(${target}$p/vivado/reports/high_fanout_nets.rpt)];
    push @f, [qq(${source}$p/vivado/reports/methodology.rpt),        qq(${target}$p/vivado/reports/methodology.rpt)];
    push @f, [qq(${source}$p/vivado/reports/power.rpt),              qq(${target}$p/vivado/reports/power.rpt)];
    push @f, [qq(${source}$p/vivado/reports/timing.rpt),             qq(${target}$p/vivado/reports/timing.rpt)];
    push @f, [qq(${source}$p/vivado/reports/timing_route.rpt),       qq(${target}$p/vivado/reports/timing_route.rpt)];
    push @f, [qq(${source}$p/vivado/reports/timing_summary.rpt),     qq(${target}$p/vivado/reports/timing_summary.rpt)];
    push @f, [qq(${source}$p/vivado/reports/utilization.rpt),        qq(${target}$p/vivado/reports/utilization.rpt)];
   }
  @f;
 }
