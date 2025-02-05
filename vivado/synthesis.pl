#!/usr/bin/perl
use v5.30;
use warnings FATAL => qw(all);
use strict;
use Carp;
use Data::Table::Text qw(:all);

my $project      = q(find);                                                     # Folder within verilog delete/find/put
my $key          =      2;                                                      # Key for operation
my $part         = q(XC7A200T);                                                 # Part

my $home         =  $ENV{HOME};                                                 # Home folder
my $project_dir  = "${home}/btreeBlock/verilog/${project}/${key}";              # Location of project files
my $includes_dir = "${project_dir}/includes";                                   # Set the path to the includes directory
my $netList      = "${project_dir}/netlist.v";                                  # Resulting net list

my $synthesis = "$home/btreeBlock/vivado/synthesis.tcl";                        # Location of vivaldo
my $vivado    = "$home/Vivado/2024.2/";                                         # Location of vivaldo
my $vivadoX   = "$home/Vivado/2024.2/bin/vivado";                               # Location of vivaldo executable

die "No such path: $vivado"    unless -d $vivado;
die "No such file: $vivadoX"   unless -f $vivadoX;

owf($synthesis, <<"END");                                                       # Write tcl to run the synthesis and write the resukting netlist
create_project ${project} ${project_dir}/vivado -part $part -force

add_files ${project_dir}/${project}.v
add_files -norecurse ${includes_dir}/M.vh
add_files -norecurse ${includes_dir}/T.vh

set_property include_dirs [list ${includes_dir}] [current_fileset]

launch_runs synth_1
wait_on_runs synth_1

write_checkpoint -force $netList

close_project
END

say STDERR qx($vivadoX -mode batch -source $synthesis);

unlink $synthesis;
