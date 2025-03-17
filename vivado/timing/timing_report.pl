#!/usr/bin/perl -I/home/phil/perl/cpan/DataTableText/lib/
#-------------------------------------------------------------------------------
# Generate timing reports from latest checkpoint
# Philip R Brenan at gmail dot com, Appa Apps Ltd Inc., 2025
#-------------------------------------------------------------------------------
use v5.30;
use warnings FATAL => qw(all);
use strict;
use Carp;
use Data::Table::Text qw(:all);
use Time::HiRes qw(time);

my $vivado    = fpd $ENV{HOME}, qw(Vivado 2024.2);                              # Location of vivado installation
my $vivadoX   = fpf $vivado,    qw(bin vivado);                                 # Location of vivado executable

my $tcl = q(timing.tcl);
my $sum = q(timing.rpt);
my %pks = (delete=>[3, 400], find=>[2, 20], put=>[1, 400]);

say STDERR dateTimeStamp, " Timing";
unlink $sum;

my @t;
my $count;

sub executeVivado($)                                                            # Execute Vivado now and then to avoid start up overhead
 {my ($force) = @_;
  if (@t == 20 || $force)
   {++$count;
    say STDERR  dateTimeStamp, " Block: $count";
    owf($tcl, join "\n", @t);
    system("$vivadoX -mode batch -source $tcl 1>>$sum");
    @t = ();
   }
 }

for my $p(sort keys %pks)                                                       # Generate timing reports from latest checkpoint
 {my ($k, $s) = $pks{$p}->@*;
  for my $i(0..$s)
   {my $rpt = qq(/home/azureuser/btreeBlock/verilog/$p/$k/statement/$i/vivado/reports/timing_route.rpt);
    my $dcp = qq(/home/azureuser/btreeBlock/verilog/$p/$k/statement/$i/vivado/dcp/route.dcp);
    push @t, <<END;
catch {
  open_checkpoint $dcp
  create_clock -period 100  [get_ports clock]

  set_input_delay  -clock clock -min 0 [get_ports -filter {DIRECTION == IN}]
  set_input_delay  -clock clock -max 0 [get_ports -filter {DIRECTION == IN}]
  set_output_delay -clock clock -min 0 [get_ports -filter {DIRECTION == OUT}]
  set_output_delay -clock clock -max 0 [get_ports -filter {DIRECTION == OUT}]

  report_timing_summary -file $rpt
  close_project
}
END
    executeVivado(0);
   }
 }
executeVivado(1);

say STDERR dateTimeStamp, " Finished";                                          # Done
