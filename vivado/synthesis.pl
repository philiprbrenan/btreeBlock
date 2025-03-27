#!/usr/bin/perl
use v5.30;
use warnings FATAL => qw(all);
use strict;
use Carp;
use Data::Table::Text qw(:all);
use Time::HiRes qw(time);
# https://docs.amd.com/v/u/en-US/zynq-7000-product-selection-guide
# Clock was k11 now C7
my $statements    = 1;                                                          # Time statements individually if true else delete/find/put components
my $part          = q(xc7z020clg484);                                           # 85K
#  $part          = q(xc7a50tcpg236);                                           # 50K
#  $part          = q(xc7a200tffv1156-2);                                       # 150K
#  $part          = q(xc7v2000tflg1925-1);                                      # 1 million
#  $part          = q(xcvu440-flga2892-1-c);                                    # 5 million

my $home          = $ENV{HOME};                                                 # Home on this machine
my $local         = "/home/phil";                                               # Home on local machine
my $project       = q(btreeBlock);                                              # The name of the project
my $projectDir    = fpd $home, $project;                                        # Folder containing generated verilog files
my $verilogDir    = fpd $projectDir, q(verilog);                                # Folder containing generated verilog files
my $vivadoDir     = fpd $projectDir, q(vivado);                                 # Folder containing vivado specific files
my $constraintsDir= fpd $vivadoDir,  q(constraints);                            # Folder containing constraints

my $vivado        = fpd $home,   qw(Vivado 2024.2);                             # Location of vivado installation
my $vivadoX       = fpf $vivado, qw(bin vivado);                                # Location of vivado executable

die "No such path: $vivado"    unless -d $vivado  or -e $local;                 # Check vivado files exist
die "No such file: $vivadoX"   unless -f $vivadoX or -e $local;

sub formatTimeDelta($)                                                          # Format a time delta preented in seconds as hours, minutes, seconds ommitting elements that are zero
 {my ($Seconds) = @_;
  my  $seconds  =   int $Seconds;
  return join ":", map {sprintf("%02d", int $_)}
      $seconds / 3600,
     ($seconds  % 3600) / 60,
      $seconds  %         60;
 }

sub gen                                                                         # Generate tcl to synthesize design
 {my ($design, $key, %options) = @_;                                            # Project, key, options
  my $statement = $options{statement};                                          # Statement to be timed
  my $exists    = $options{test};                                               # Test whether a statement file exists
  my $title     = $options{title} // '';                                        # Title to print

  my @statement = defined($statement) ? (q(statement), $statement) : ();        # Address statement files

  my $designDir   = fpd $verilogDir, $design, $key, @statement;                 # Location of project input files
  my $designOut   = fpd $designDir, qw(vivado);                                 # Location of project output files
  my $includesDir = fpd $designDir, qw(includes);                               # Set the path to the includes directory
  my $reportsDir  = fpd $designOut, qw(reports);                                # Reports
  my $dcpDir      = fpd $designOut, qw(dcp);                                    # Checkpoints
  my $synthesis   = fpe $vivadoDir,      $design, qw(tcl);                      # Generated vivado commands
  my $constraints = fpe $constraintsDir, $part,   qw(xdc);                      # Constraints file
  my $final       = fpe $designOut,      $design, qw(bit);                      # Final output bit stream
  my $verilog     = fpe $designDir,      $design, qw(v);                        # Input verilog
  my $route       = fpe $dcpDir, qw(route dcp);                                 # After route checkpoint
  my $place       = fpe $dcpDir, qw(place dcp);                                 # After place checkpoint
  my $opt         = fpe $dcpDir, qw(opt   dcp);                                 # After optimization checkpoint
  my $synth       = fpe $dcpDir, qw(synth dcp);                                 # After synthesis checkpoint
  my $timingRoute = fpe $reportsDir, qw(timing_route rpt);                      # Timing after routing

  return -e $timingRoute if defined($statement) and defined($exists);           # Test for the existance of a timing file report

  my @reports     =                                                             # Reports
   qw(bus_skew clock_interaction control_sets cdc design_analysis
      drc high_fanout_nets methodology power timing_summary utilization);

  my $reports = sub                                                             # Generate report commands
   {my @r;
    for my $r(@reports)
     {push @r, qq(report_$r -file ${reportsDir}$r.rpt);
     }
    join "\n", @r;
   }->();

  makePath fpd $reportsDir;                                                     # Ensure reports folder is present
  makePath fpd $dcpDir;                                                         # Ensure checkpoints folder is present

  die "No such file: $constraints" unless -f $constraints;
  die "No such path: $reportsDir"  unless -d $reportsDir;
  die "No such path: $dcpDir"      unless -d $dcpDir;

  my @s = <<"END";                                                              # Write tcl to run the synthesis
read_verilog $verilog
read_xdc     $constraints

set_msg_config -id {Synth 8-7080} -suppress

synth_design -name $design -top $design -part $part -include_dirs $includesDir -flatten_hierarchy none -no_timing_driven -directive AlternateRoutability
write_checkpoint -force $synth
puts "[clock format [clock seconds] -format \"%H:%M:%S\"] synth_design"

opt_design -directive RuntimeOptimized
write_checkpoint -force $opt
puts "[clock format [clock seconds] -format \"%H:%M:%S\"] opt_design"

$reports

place_design -directive AltSpreadLogic_high
write_checkpoint -force $place
puts "[clock format [clock seconds] -format \"%H:%M:%S\"] place_design"

#route_design -directive Quick
route_design
write_checkpoint -force $route
report_timing_summary    -file $timingRoute
puts "[clock format [clock seconds] -format \"%H:%M:%S\"] route_design"

write_bitstream  -force $final
puts "[clock format [clock seconds] -format \"%H:%M:%S\"] write_bitstream"
END

  owf($synthesis, join "\n", @s);                                               # Write tcl to run the synthesis

  say STDERR dateTimeStamp, " $part for $design ".join(" ", @statement), $title;# Run tcl
  system("$vivadoX -mode batch -source $synthesis 1>$reportsDir/1.txt");
# unlink $synthesis;
 }

say STDERR dateTimeStamp, " Generate btreeBlock";                               # Title

system("rm -rf /home/azureuser/btreeblock/verilog");                            # Remove previous verilog code and reports as we are abiout to regenerate them

if  (!-e q(/home/phil/))                                                        # Create the verilog files if on azure
 {system("cd $projectDir; bash j.sh BtreeDM");
 }

my $ranges = eval readFile "ranges.txt";                                        # Load statement ranges

if (!$$ranges{statements})                                                      # Synthesize, place and route the verilog description
 {say STDERR dateTimeStamp, " Synthesize, place and route btreeBlock on Vivado";
  gen(q(find)  , $$ranges{projects}{find}  [0]);
  gen(q(delete), $$ranges{projects}{delete}[0]);
  gen(q(put)   , $$ranges{projects}{put}   [0]);
 }
else                                                                            # Arrival time for each statement by synthesizing each statement in isolation
 {say STDERR dateTimeStamp, " Time individual statements";

  my @files = searchDirectoryTreesForMatchingFiles($verilogDir, qw(.tb));
  my @remainder;                                                                # Statements still to be tested

  for my $f(@files)                                                             # Find statements still to be tested
   {if ($f =~ m(/(\w+)/(\d+)/statement/(\d+)/)igs)
     {my ($p, $k, $s) = ($1, $2, $3);
      if (!gen($p, $k, statement=>$s, exists=>1))
       {push @remainder, [$p, $k, $s];
       }
     }
   }

  my $start = time; my $processed = 0;                                          # Start time of processing, number of statements processed

  for my $f(@remainder)                                                         # Process remaining statements with estimated time of arrival
   {my ($p, $k, $s) = @$f;
    if (!$processed)                                                            # No timing  information yet as we are on the first one
     {gen($p, $k, statement=>$s);
     }
    else                                                                        # Compute ETA from avergae time per statement
     {my $d = time - $start;
      my $r = @remainder - $processed;
      my $a = $d / $processed;
      my $e = $start + $a * @remainder;
      my $t = " ETA: ".strftime("%Y-%m-%d %H:%M:%S", localtime($e));
      gen($p, $k, statement=>$s, title=>$t);
     }
    ++$processed;
   }
 }

say STDERR dateTimeStamp, " Finished";                                          # Done
