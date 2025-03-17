#!/usr/bin/perl
use v5.30;
use warnings FATAL => qw(all);
use strict;
use Carp;
use Data::Table::Text qw(:all);
use Time::HiRes qw(time);

my $timingTcl = q(timing.tcl);
my $timingRpt = q(timing.rpt);

my @t;
my %p = (delete=>[3, 400], find=>[2, 20], put=>[1, 400]);

for my $p(sort keys %p)
 {my ($k, $s) = $p{$p}->@*;
  for my $i(0..$s)
   {my $rpt = qq(/home/azureuser/btreeBlock/verilog/$p/$k/statement/$s/vivado/reports/timing_route.rpt);
    my $dcp = qq(/home/azureuser/btreeBlock/verilog/$p/$k/statement/$s/vivado/dcp/route.dcp);
    push @t, <<END;
open_checkpoint $dcp
create_clock -period 100  [get_ports clock]

set_input_delay  -clock clock -min 0 [get_ports -filter {DIRECTION == IN}]
set_input_delay  -clock clock -max 0 [get_ports -filter {DIRECTION == IN}]
set_output_delay -clock clock -min 0 [get_ports -filter {DIRECTION == OUT}]
set_output_delay -clock clock -max 0 [get_ports -filter {DIRECTION == OUT}]
END
   }
 }

owf($timingTcl, join "\n", @t);                                                    # Write tcl to run timing

say STDERR dateTimeStamp, " Timing";
system("$vivadoX -mode batch -source $timingTcl 1>$timingRpt");

say STDERR dateTimeStamp, " Finished";                                          # Done
