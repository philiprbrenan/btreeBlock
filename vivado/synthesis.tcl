# Set the project directory
set home $env(HOME)
set project_dir "${home}/btreeBlock/verilog/find/2"

# Set the path to the includes directory
set includes_dir "${project_dir}/includes"

# Create a new Vivado project
create_project find ${project_dir}/vivado -part xc7z020clg484-1

# Add the main Verilog file and the include files
add_files ${project_dir}/find.v
add_files -norecurse ${includes_dir}/file1.v
add_files -norecurse ${includes_dir}/file2.v

# Set the include directory for Verilog files
set_property include_dirs [list ${includes_dir}] [current_fileset]

# Run synthesis
launch_runs synth_1

# Wait for the synthesis to finish
wait_on_run synth_1

# Check the synthesis results
if { [get_runs -completed] == 0 } {
    puts "Synthesis failed."
} else {
    puts "Synthesis completed successfully."
}

# Close the project
close_project
