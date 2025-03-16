#!/usr/bin/perl
use v5.30;
use warnings FATAL => qw(all);
use strict;
use Carp;
use Data::Table::Text qw(:all);
# https://docs.amd.com/v/u/en-US/zynq-7000-product-selection-guide
# Clock was k11 now C7
my $statements    = 1;                                                          # Time statements individually if true else delete/find/put components
my $part          = q(xc7a50tcpg236);                                           # 50K
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

sub gen                                                                         # Generate tcl to synthesize design
 {my ($design, $key, $statement) = @_;                                          # Project, key, optional statement

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

  if (defined($statement) and -e $timingRoute)                                  # Timing file has already been created
   {say STDERR "Exists: $timingRoute";
    return;
   }

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

synth_design -name $design -top $design -part $part -include_dirs $includesDir -flatten_hierarchy none -no_timing_driven -directive AlternateRoutability

write_checkpoint -force $synth

opt_design -directive RuntimeOptimized
write_checkpoint -force $opt

$reports

place_design -directive AltSpreadLogic_high
write_checkpoint -force $place

route_design -directive Quick
write_checkpoint -force $route
report_timing_summary    -file $timingRoute

write_bitstream  -force $final
END

  owf($synthesis, join "\n", @s);                                               # Write tcl to run the synthesis

  say   STDERR dateTimeStamp, " $part for $design ".join " ", @statement;       # Run tcl
  system("$vivadoX -mode batch -source $synthesis 1>$reportsDir/1.txt");
  unlink $synthesis;
 }

if    (-e q(/home/phil/)) {}                                                    # Create the verilog files if on azure
elsif (-e q(/home/azureuser/btreeblock/vivado/find/1/)) {}                      # Create the verilog files if they have not already been created
else
 {say STDERR dateTimeStamp, " Generate   btreeBlock";
  system("cd $projectDir; bash j.sh BtreeDM");
 }


if (!$statements)                                                               # Synthesize, place and route the verilog description
 {say STDERR dateTimeStamp, " Synthesize, place and route btreeBlock";
  gen(qw(find   2));
  gen(qw(delete 3));
  gen(qw(put    1));
 }
else                                                                            # Arrival time for each statement
 {say STDERR dateTimeStamp, " Time individual statements";
  my @files = searchDirectoryTreesForMatchingFiles($verilogDir, qw(.tb));

  for my $f(@files)
   {if ($f =~ m(/(\w+)/(\d+)/statement/(\d+)/)igs)
     {my ($project, $key, $statement) = ($1, $2, $3);
      #next if $project =~ m(find)i;
      #next if $project =~ m(delete)i and $statement < 140;
      #next if $project =~ m(put)i    and $statement < 140;
      gen($project, $key, $statement);
     }
   }
 }

say STDERR dateTimeStamp, " Finished";                                          # Done
