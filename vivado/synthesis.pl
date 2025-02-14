#!/usr/bin/perl
use v5.30;
use warnings FATAL => qw(all);
use strict;
use Carp;
use Data::Table::Text qw(:all);

my $project      = q(find);                                                     # Folder within verilog delete/find/put
my $key          =      2;                                                      # Key for operation
my $part         = q(XC7Z007S);                                                 # Part  $131 https://www.xilinx.com/products/boards-and-kits/1-1bkpiul.html

my $home         =  $ENV{HOME};                                                 # Home folder
my $project_dir  = "${home}/btreeBlock/verilog/${project}/${key}";              # Location of project files
my $includes_dir = "${project_dir}/includes";                                   # Set the path to the includes directory
my $reports_dir  = "${project_dir}/reports";                                    # Reports

my $synthesis = "$home/btreeBlock/vivado/synthesis.tcl";                        # Location of vivaldo
my $vivado    = "$home/Vivado/2024.2/";                                         # Location of vivaldo
my $vivadoX   = "$home/Vivado/2024.2/bin/vivado";                               # Location of vivaldo executable

makePath $reports_dir;                                                          # Ensure folder structure is present

die "No such path: $vivado"    unless -d $vivado;
die "No such file: $vivadoX"   unless -f $vivadoX;

owf($synthesis, <<"END");                                                       # Write tcl to run the synthesis
create_project ${project} ${project_dir}/vivado -part $part -force

add_files ${project_dir}/${project}.v
add_files -norecurse ${includes_dir}/M.vh
add_files -norecurse ${includes_dir}/T.vh

set_property include_dirs [list ${includes_dir}] [current_fileset]
set_param general.maxThreads 1

set_param synth.elaborate.keepHierarchy true
set_param synth.keep_equivalent_registers true

launch_runs  synth_1
wait_on_runs synth_1

write_checkpoint -force post_synth.dcp

opt_design
write_checkpoint -force post_opt.dcp

place_design
write_checkpoint -force post_place.dcp

route_design
write_checkpoint -force post_route.dcp

report_utilization       -file $reports_dir/utilization.rpt
report_timing_summary    -file $reports_dir/timing.rpt
report_power             -file $reports_dir/power.rpt
report_drc               -file $reports_dir/drc.rpt
report_clock_interaction -file $reports_dir/clock_interaction.rpt
report_cdc               -file $reports_dir/cdc.rpt
report_control_sets      -file $reports_dir/control.rpt
report_bus_skew          -file $reports_dir/bus_skew.rpt
report_high_fanout_nets  -file $reports_dir/fanout.rpt

write_checkpoint -force final_routed.dcp
write_bitstream  -force final.bit

close_project
END

say STDERR qx($vivadoX -mode batch -source $synthesis);

unlink $synthesis;
