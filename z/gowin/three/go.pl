#!/usr/bin/perl -I/home/phil/perl/cpan/DataTableText/lib/
#-------------------------------------------------------------------------------
# Show the number three on the leds
# Philip R Brenan at gmail dot com, Appa Apps Ltd Inc., 2025
#-------------------------------------------------------------------------------
use v5.38;
use warnings FATAL => qw(all);
use strict;
use Carp;
use Data::Dump qw(dump);
use Data::Table::Text qw(:all);

my $project = q(three);
my $verilog = fpe $project, q(v);
my $bits    = fpe $project, q(fs);
my $cable   = q(ft2232);
my $device  = q(GW1NR-LV9QN88PC6/I5);
my $pack    = q(GW1N-9C);

my $reports = q(reports);
my $build   = q(build);
my $y1      = fpe $reports, qw(yosys-1 txt);
my $y2      = fpe $reports, qw(yosys-2 txt);
my $n1      = fpe $reports, qw(nextpnr-gowin-1 txt);
my $n2      = fpe $reports, qw(nextpnr-gowin-2 txt);
my $p1      = fpe $reports, qw(gowin_pack-1 txt);
my $p2      = fpe $reports, qw(gowin_pack-2 txt);

my $yj      = fpe $build,   qw(yosys   json);
my $nj      = fpe $build,   qw(nextpnr json);
my $pj      = fpe $build,   qw(pack    json);

makePath($_) for $y1, $yj;

# Create project files
owf($verilog, <<"EOF");
module $project (
    input  wire clk,
    output wire led1,
    output wire led2,
    output wire led3,
    output wire led4,
    output wire led5,
    output wire led6
);
    assign led1 = 1;                                                            // Leds must be pulled low to show. We are looking from the button end.
    assign led2 = 1;
    assign led3 = 1;
    assign led4 = 1;
    assign led5 = 1;
    assign led6 = 0;
endmodule
EOF

owf("tang9k.cst", <<'EOF');
IO_LOC "led1" 10;
IO_LOC "led2" 11;
IO_LOC "led3" 13;
IO_LOC "led4" 14;
IO_LOC "led5" 15;
IO_LOC "led6" 16;
IO_PORT "led1" PULL_MODE=NONE DRIVE=8;
IO_PORT "led2" PULL_MODE=NONE DRIVE=8;
IO_PORT "led3" PULL_MODE=NONE DRIVE=8;
IO_PORT "led4" PULL_MODE=NONE DRIVE=8;
IO_PORT "led5" PULL_MODE=NONE DRIVE=8;
IO_PORT "led6" PULL_MODE=NONE DRIVE=8;
IO_LOC "clk" 52;
EOF

system(qq(yosys -qp "read_verilog $verilog; synth_gowin -top $project -json $yj"        2>$y2));
if ($?)
 {say STDERR readFile($y2);
  confess;
 }

system(qq(nextpnr-gowin --json $yj  --write $nj --device "$device" --cst tang9k.cst 2>$n2));
if ($?)
 {say STDERR readFile($n2);
  confess;
 }
system(qq(gowin_pack -d $pack -o $bits $nj                                          2>$p2));
if ($?)
 {say STDERR readFile($p2);
  confess;
 }
system(qq(openFPGALoader -c $cable $bits));

## Troubleshooting (run separately if needed)
#echo 'ATTRS{idVendor}=="0403", ATTRS{idProduct}=="6010", MODE="0660"' | sudo tee /etc/udev/rules.d/99-tang9k.rules && \
##sudo udevadm control --reload-rules && \
#openFPGALoader --detect
