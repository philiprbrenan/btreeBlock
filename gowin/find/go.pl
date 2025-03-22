#!/usr/bin/perl -I/home/phil/perl/cpan/DataTableText/lib/
#-------------------------------------------------------------------------------
# Find from Database On A Chip
# Philip R Brenan at gmail dot com, Appa Apps Ltd Inc., 2025
#-------------------------------------------------------------------------------
use v5.38;
use warnings FATAL => qw(all);
use strict;
use Carp;
use Data::Dump qw(dump);
use Data::Table::Text qw(:all);

my $project = q(find);
my $verilog = fpe $project, q(v);
my $cst     = fpe $project, q(xdc);
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

owf($cst, <<'EOF');
IO_LOC "led[0]" 10;
IO_LOC "led[1]" 11;
IO_LOC "led[2]" 13;
IO_LOC "led[3]" 14;
IO_LOC "led[4]" 15;
IO_LOC "led[5]" 16;
IO_PORT "led[0]" PULL_MODE=NONE DRIVE=8;
IO_PORT "led[1]" PULL_MODE=NONE DRIVE=8;
IO_PORT "led[2]" PULL_MODE=NONE DRIVE=8;
IO_PORT "led[3]" PULL_MODE=NONE DRIVE=8;
IO_PORT "led[4]" PULL_MODE=NONE DRIVE=8;
IO_PORT "led[5]" PULL_MODE=NONE DRIVE=8;
IO_LOC  "clock" 52;
IO_LOC  "button1" 4;
IO_PORT "button1" PULL_MODE=UP;
IO_LOC  "button2" 3;
IO_PORT "button2" PULL_MODE=UP;
EOF

system(qq(yosys -p "read_verilog $verilog; synth_gowin -top $project -json $yj" 1>$y1 2>$y2));
if ($?)
 {say STDERR readFile($y2);
  confess;
 }

system(qq(nextpnr-gowin --json $yj  --write $nj --device "$device" --cst $cst   1>$n1 2>$n2));
if ($?)
 {say STDERR readFile($n2);
  confess;
 }
system(qq(gowin_pack -d $pack -o $bits $nj                                      1>$p1 2>$p2));
if ($?)
 {say STDERR readFile($p2);
  confess;
 }
say STDERR qq(openFPGALoader -c $cable $bits);
system(qq(openFPGALoader -c $cable $bits));

## Troubleshooting (run separately if needed)
#echo 'ATTRS{idVendor}=="0403", ATTRS{idProduct}=="6010", MODE="0660"' | sudo tee /etc/udev/rules.d/99-tang9k.rules && \
##sudo udevadm control --reload-rules && \
#openFPGALoader --detect
