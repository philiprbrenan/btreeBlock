create_project find /home/phil/btreeBlock/verilog/find/2/vivado -part XC7A200T -force

add_files /home/phil/btreeBlock/verilog/find/2/find.v
add_files -norecurse /home/phil/btreeBlock/verilog/find/2/includes/M.vh
add_files -norecurse /home/phil/btreeBlock/verilog/find/2/includes/T.vh

set_property include_dirs [list /home/phil/btreeBlock/verilog/find/2/includes] [current_fileset]

launch_runs synth_1
wait_on_runs synth_1

write_checkpoint -force /home/phil/btreeBlock/verilog/find/2/netlist.v

close_project
