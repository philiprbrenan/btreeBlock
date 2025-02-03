# Set the project directory
set project find
set key     2
set home $env(HOME)
set project_dir "${home}/btreeBlock/verilog/${project}/${key}"

# Set the path to the includes directory
set includes_dir "${project_dir}/includes"

# Create a new Vivado project
create_project ${project} ${project_dir}/vivado -part xc7z020clg484-1 -force

# Add the main Verilog file and the include files
add_files ${project_dir}/${project}.v
add_files -norecurse ${includes_dir}/M.vh
add_files -norecurse ${includes_dir}/T.vh

# Set the include directory for Verilog files
set_property include_dirs [list ${includes_dir}] [current_fileset]

# Run synthesis
launch_runs synth_1

# Wait for the synthesis to finish
wait_on_run synth_1

# Check the synthesis results
#if { [get_runs -completed] == 0 } {
#    puts "Synthesis failed."
#} else {
#    puts "Synthesis completed successfully."
#}

# Close the project
close_project
