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
   $part          = q(XC7V2000T);
   $part          = q(xc7a200tffv1156-2);                                       # 150K
   $part          = q(xc7v2000tflg1925-1);                                      # 1 million
#  $part          = q(xcvu440-flga2892-1-c);                                    # 5 million

my $localHome     = "/home/phil/";                                              # Home on local machine
my $local         = -e $localHome;                                              # On local machine
my $home          = fpd(($local ? $localHome : "/home/azureuser/"));            # Working folder
my $projectDir    = fpd $home, $project;                                        # Folder containing generated verilog files
my $verilogDir    = fpd $projectDir, q(verilog);                                # Folder containing generated verilog files
my $vivadoDir     = fpd $projectDir, q(vivado);                                 # Folder containing vivado specific files
my $constraintsDir= fpd $vivadoDir,  q(constraints);                            # Folder containing constraints

my $vivado        = fpd $home,   qw(Vivado 2024.2);                             # Location of vivado installation
my $vivadoX       = fpf $vivado, qw(bin vivado);                                # Location of vivado executable

die "No such path: $vivado"    unless -d $vivado  or -e "/home/phil";           # Check vivado files exist
die "No such file: $vivadoX"   unless -f $vivadoX or -e "/home/phil";

sub gen                                                                         # Generate tcl to synthesize design
 {my ($design, $key, $statement) = @_;                                          # Project, key, optional statement

  my @statement = defined($statement) ? (q(statement), $statement) : ();        # Address statement files

  my $designDir   = fpd $verilogDir, $design, $key, @statement;                 # Location of project input files
  my $designOut   = fpd $verilogDir, $design, qw(vivado);                       # Location of project output files
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


  my @reports     =                                                             # Reports
   qw(bus_skew clock_interaction control_sets cdc design_analysis
      drc high_fanout_nets methodology power timing_summary utilization);

  my $reports = sub
   {my @r;
    for my $r(@reports)
     {if (defined($statement) and $r =~ m(timing)is)
       {my $f = fpe $reportsDir, q(statement), $statement, $r,  q(rpt);
        makePath $f;
        say STDERR "AAAA $f";
        push @r, qq(report_$r -file $f);
       }
      else
       {push @r, qq(report_$r -file ${reportsDir}$r.rpt);
       }
     }
    join "\n", @r;
   }->();

  makePath fpd $reportsDir;                                                     # Ensure folder structure is present
  makePath fpd $dcpDir;                                                         # Ensure folder structure is present

  die "No such file: $constraints" unless -f $constraints;
  die "No such path: $reportsDir"  unless -d $reportsDir;
  die "No such path: $dcpDir"      unless -d $dcpDir;

  my @s = <<"END";                                                              # Write tcl to run the synthesis
set_param general.maxThreads 4

read_verilog $verilog
read_xdc     $constraints

synth_design -name $design -top $design -part $part -include_dirs $includesDir -flatten_hierarchy none
write_checkpoint -force $synth

opt_design
write_checkpoint -force $opt

$reports

place_design -directive AltSpreadLogic_high
write_checkpoint -force $place

route_design -directive Quick
write_checkpoint -force $route
report_timing_summary    -file ${reportsDir}timing_route.rpt

write_bitstream  -force $final
END

  owf($synthesis, join "\n", @s);                                               # Write tcl to run the synthesis

  say   STDERR dateTimeStamp, " $part for $design ".join " ", @statement;       # Run tcl
  print STDERR qx($vivadoX -mode batch -source $synthesis 1>$reportsDir/1.txt);
  #unlink $synthesis;
 }

if    (-e q(/home/phil/)) {}                                                    # Create the verilog files if on azure
elsif (-e q(/home/azureuser/btreeblock/vivado/find/1/)) {}                      # Create the verilog files if they have not already been created
else
 {say STDERR dateTimeStamp, " Generate   btreeBlock";
  say STDERR qx(cd $projectDir; bash j.sh BtreePA);
 }

say STDERR dateTimeStamp, " Synthesize btreeBlock";                             # Synthesize the verilog description
gen(qw(find   2), $_) for 0..14;
gen(qw(delete 2));
gen(qw(put    2));
