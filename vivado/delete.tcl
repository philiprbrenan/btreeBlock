set_param general.maxThreads 1

read_verilog /home/phil/btreeBlock/verilog/delete/2//delete.v
read_xdc     /home/phil/btreeBlock/vivado/constraints.xdc

synth_design -name delete -top delete -part XC7Z100 -include_dirs /home/phil/btreeBlock/verilog/delete/2/includes/ -flatten_hierarchy none
write_checkpoint -force /home/phil/btreeBlock/verilog/delete/vivado/dcp//synth.dcp

opt_design
write_checkpoint -force /home/phil/btreeBlock/verilog/delete/vivado/dcp//opt.dcp

report_utilization       -file /home/phil/btreeBlock/verilog/delete/vivado/reports//utilization.rpt
report_methodology       -file /home/phil/btreeBlock/verilog/delete/vivado/reports//methodology.rpt
report_timing_summary    -file /home/phil/btreeBlock/verilog/delete/vivado/reports//timing.rpt
report_power             -file /home/phil/btreeBlock/verilog/delete/vivado/reports//power.rpt
report_drc               -file /home/phil/btreeBlock/verilog/delete/vivado/reports//drc.rpt
report_clock_interaction -file /home/phil/btreeBlock/verilog/delete/vivado/reports//clock_interaction.rpt
report_cdc               -file /home/phil/btreeBlock/verilog/delete/vivado/reports//cdc.rpt
report_control_sets      -file /home/phil/btreeBlock/verilog/delete/vivado/reports//control.rpt
report_bus_skew          -file /home/phil/btreeBlock/verilog/delete/vivado/reports//bus_skew.rpt
report_high_fanout_nets  -file /home/phil/btreeBlock/verilog/delete/vivado/reports//fanout.rpt

place_design
write_checkpoint -force /home/phil/btreeBlock/verilog/delete/vivado/dcp//place.dcp

route_design
write_checkpoint -force /home/phil/btreeBlock/verilog/delete/vivado/dcp//route.dcp

write_bitstream  -force /home/phil/btreeBlock/verilog/delete/2//final.bit
