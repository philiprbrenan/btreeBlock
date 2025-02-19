#!/usr/bin/perl
use v5.30;
use warnings FATAL => qw(all);
use strict;
use Carp;
use Data::Table::Text qw(:all);
# https://docs.amd.com/v/u/en-US/zynq-7000-product-selection-guide
# Clock was k11 now C7
my $project       = q(btreeBlock);                                              # The name of the project
my $part          = q(XC7Z007S);
   $part          = q(XC7V585T);

my $localHome     = "/home/phil/";                                              # Home on local machine
my $local         = -e $localHome;                                              # On local machine
my $home          = fpd(($local ? $localHome : "/home/azureuser/"));            # Working folder
my $projectDir    = fpd $home, $project;                                        # Folder containing generated verilog files
my $verilogDir    = fpd $projectDir, q(verilog);                                # Folder containing generated verilog files
my $vivadoDir     = fpd $projectDir, q(vivado);                                 # Folder containing vivado specific files

my $vivado        = fpd $home,   qw(Vivado 2024.2);                             # Location of vivaldo installation
my $vivadoX       = fpf $vivado, qw(bin vivado);                                # Location of vivaldo executable

die "No such path: $vivado"    unless -d $vivado  or -e "/home/phil";           # Check vivado files exist
die "No such file: $vivadoX"   unless -f $vivadoX or -e "/home/phil";

sub gen                                                                         # Generate tcl to synthesize design
 {my ($project, $key) = @_;                                                     # Project, key

  my $projectDir  = fpd $verilogDir, $project, $key;                            # Location of project input files
  my $projectOut  = fpd $verilogDir, $project, qw(vivado);                      # Location of project output files
  my $includesDir = fpd $projectDir, qw(includes);                              # Set the path to the includes directory
  my $reportsDir  = fpd $projectOut, qw(reports);                               # Reports
  my $dcpDir      = fpd $projectOut, qw(dcp);                                   # Checkpoints
  my $synthesis   = fpe $vivadoDir, $project, qw(tcl);                          # Generated vivado commands
  my $constraints = fpe $vivadoDir, qw(constraints xdc);                        # Constraints file

  makePath fpd $reportsDir;                                                     # Ensure folder structure is present
  makePath fpd $dcpDir;                                                         # Ensure folder structure is present

  die "No such file: $constraints" unless -f $constraints;
  die "No such path: $reportsDir"  unless -d $reportsDir;
  die "No such path: $dcpDir"      unless -d $dcpDir;

  my @s = <<"END";                                                              # Write tcl to run the synthesis
set_param general.maxThreads 1

read_verilog $projectDir/$project.v
read_xdc     $constraints

synth_design -name $project -top $project -part $part -include_dirs $includesDir -flatten_hierarchy none
write_checkpoint -force $dcpDir/synth.dcp

opt_design
write_checkpoint -force $dcpDir/opt.dcp

report_utilization       -file $reportsDir/utilization.rpt
report_methodology       -file $reportsDir/methodology.rpt
report_timing_summary    -file $reportsDir/timing.rpt
report_power             -file $reportsDir/power.rpt
report_drc               -file $reportsDir/drc.rpt
report_clock_interaction -file $reportsDir/clock_interaction.rpt
report_cdc               -file $reportsDir/cdc.rpt
report_control_sets      -file $reportsDir/control.rpt
report_bus_skew          -file $reportsDir/bus_skew.rpt
report_high_fanout_nets  -file $reportsDir/fanout.rpt

place_design
write_checkpoint -force $dcpDir/place.dcp

route_design
write_checkpoint -force $dcpDir/route.dcp

END

  if ($project =~ m(find)is)                                                    # Write bit stream
   {push @s, <<END;
write_bitstream  -force $projectDir/final.bit
END
   }

  owf($synthesis, join "\n", @s);                                               # Write tcl to run the synthesis

  say STDERR dateTimeStamp, " $project";                                        # Run tcl
  say STDERR qx($vivadoX -mode batch -source $synthesis 1>$reportsDir/1.txt);

  #unlink $synthesis;
 }

say STDERR dateTimeStamp, " Generate   btreeBlock";                             # Create the verilog files
say STDERR qx(cd $projectDir; bash j.sh BtreePA);

say STDERR dateTimeStamp, " Synthesize btreeBlock";                             # Synthesize the verilog description
gen(qw(find   2));
#gen(qw(delete 2));
#gen(qw(put    2));
