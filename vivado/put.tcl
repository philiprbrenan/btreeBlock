set_param general.maxThreads 1

read_verilog /home/phil/btreeBlock/verilog/put/2//put.v
read_xdc     /home/phil/btreeBlock/vivado/constraints/xc7a200tffv1156-2.xdc

synth_design -name put -top put -part xc7a200tffv1156-2 -include_dirs /home/phil/btreeBlock/verilog/put/2/includes/ -flatten_hierarchy none
write_checkpoint -force /home/phil/btreeBlock/verilog/put/vivado/dcp//synth.dcp

opt_design
write_checkpoint -force /home/phil/btreeBlock/verilog/put/vivado/dcp//opt.dcp

report_utilization       -file /home/phil/btreeBlock/verilog/put/vivado/reports//utilization.rpt
report_methodology       -file /home/phil/btreeBlock/verilog/put/vivado/reports//methodology.rpt
report_timing_summary    -file /home/phil/btreeBlock/verilog/put/vivado/reports//timing.rpt
report_power             -file /home/phil/btreeBlock/verilog/put/vivado/reports//power.rpt
report_drc               -file /home/phil/btreeBlock/verilog/put/vivado/reports//drc.rpt
report_clock_interaction -file /home/phil/btreeBlock/verilog/put/vivado/reports//clock_interaction.rpt
report_cdc               -file /home/phil/btreeBlock/verilog/put/vivado/reports//cdc.rpt
report_control_sets      -file /home/phil/btreeBlock/verilog/put/vivado/reports//control.rpt
report_bus_skew          -file /home/phil/btreeBlock/verilog/put/vivado/reports//bus_skew.rpt
report_high_fanout_nets  -file /home/phil/btreeBlock/verilog/put/vivado/reports//fanout.rpt

place_design
write_checkpoint -force /home/phil/btreeBlock/verilog/put/vivado/dcp//place.dcp

route_design
write_checkpoint -force /home/phil/btreeBlock/verilog/put/vivado/dcp//route.dcp

write_bitstream  -force /home/phil/btreeBlock/verilog/put/2//final.bit
