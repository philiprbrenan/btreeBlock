#!/usr/bin/perl
use v5.30;
use warnings FATAL => qw(all);
use strict;
use Carp;
use Data::Table::Text qw(:all);

my $part = q(XC7Z007S);                                                         # Part $131 https://www.xilinx.com/products/boards-and-kits/1-1bkpiul.html

sub gen                                                                         # Generate tcl to synthesize design
 {my ($project, $key) = @_;                                                     # Project, key

  my $home         =  $ENV{HOME};                                               # Home folder
  my $project_dir  = "$home/btreeBlock/verilog/$project/$key";                  # Location of project input files
  my $project_out  = "$home/btreeBlock/verilog/$project/vivado";                # Location of project output files
  my $includes_dir = "$project_dir/includes";                                   # Set the path to the includes directory
  my $constraints  = "$home/btreeBlock/vivado/constraints.xdc";                 # Constraints file
  my $reports_dir  = "$project_out/reports";                                    # Reports
  my $dcp_dir      = "$project_out/dcp";                                        # Checkpoints

  my $synthesis    = "$home/btreeBlock/vivado/$project.tcl";                    # Generated vivado commands
  my $vivado       = "$home/Vivado/2024.2/";                                    # Location of vivaldo
  my $vivadoX      = "$home/Vivado/2024.2/bin/vivado";                          # Location of vivaldo executable

  makePath fpd $reports_dir;                                                    # Ensure folder structure is present
  makePath fpd $dcp_dir;                                                        # Ensure folder structure is present

  die "No such file: $constraints" unless -f $constraints;
  die "No such path: $reports_dir" unless -d $reports_dir;
  die "No such path: $dcp_dir"     unless -d $dcp_dir;

  die "No such path: $vivado"    unless -d $vivado;
  die "No such file: $vivadoX"   unless -f $vivadoX;

  owf($synthesis, <<"END");                                                     # Write tcl to run the synthesis
set_param general.maxThreads 1

read_verilog $project_dir/$project.v
read_xdc     $constraints

synth_design -name $project -top $project -part $part -include_dirs $includes_dir -flatten_hierarchy none
write_checkpoint -force $dcp_dir/synth.dcp

opt_design
write_checkpoint -force $dcp_dir/opt.dcp

report_utilization       -file $reports_dir/utilization.rpt
report_methodology       -file $reports_dir/methodology.rpt
report_timing_summary    -file $reports_dir/timing.rpt
report_power             -file $reports_dir/power.rpt
report_drc               -file $reports_dir/drc.rpt
report_clock_interaction -file $reports_dir/clock_interaction.rpt
report_cdc               -file $reports_dir/cdc.rpt
report_control_sets      -file $reports_dir/control.rpt
report_bus_skew          -file $reports_dir/bus_skew.rpt
report_high_fanout_nets  -file $reports_dir/fanout.rpt

#place_design
#write_checkpoint -force $dcp_dir/place.dcp

#route_design
#write_checkpoint -force $dcp_dir/route.dcp

# Need pin locations
#write_bitstream  -force $project_dir/final.bit
END

  say STDERR dateTimeStamp, " $project";                                        # Run tcl
  say STDERR qx($vivadoX -mode batch -source $synthesis 1>$reports_dir/1.txt);

  unlink $synthesis;
 }

say STDERR dateTimeStamp, " Synthesis of btreeBlock";
gen(qw(find   2));
gen(qw(delete 2));
gen(qw(put    2));
