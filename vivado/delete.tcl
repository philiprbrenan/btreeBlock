read_verilog /home/azureuser/btreeBlock/verilog/delete/3/statement/10/delete.v
read_xdc     /home/azureuser/btreeBlock/vivado/constraints/xc7a50tcpg236.xdc

synth_design -name delete -top delete -part xc7a50tcpg236 -include_dirs /home/azureuser/btreeBlock/verilog/delete/3/statement/10/includes/ -flatten_hierarchy none -no_timing_driven -directive AlternateRoutability

write_checkpoint -force /home/azureuser/btreeBlock/verilog/delete/3/statement/10/vivado/dcp/synth.dcp

opt_design -directive RuntimeOptimized
write_checkpoint -force /home/azureuser/btreeBlock/verilog/delete/3/statement/10/vivado/dcp/opt.dcp

report_bus_skew -file /home/azureuser/btreeBlock/verilog/delete/3/statement/10/vivado/reports/bus_skew.rpt
report_clock_interaction -file /home/azureuser/btreeBlock/verilog/delete/3/statement/10/vivado/reports/clock_interaction.rpt
report_control_sets -file /home/azureuser/btreeBlock/verilog/delete/3/statement/10/vivado/reports/control_sets.rpt
report_cdc -file /home/azureuser/btreeBlock/verilog/delete/3/statement/10/vivado/reports/cdc.rpt
report_design_analysis -file /home/azureuser/btreeBlock/verilog/delete/3/statement/10/vivado/reports/design_analysis.rpt
report_drc -file /home/azureuser/btreeBlock/verilog/delete/3/statement/10/vivado/reports/drc.rpt
report_high_fanout_nets -file /home/azureuser/btreeBlock/verilog/delete/3/statement/10/vivado/reports/high_fanout_nets.rpt
report_methodology -file /home/azureuser/btreeBlock/verilog/delete/3/statement/10/vivado/reports/methodology.rpt
report_power -file /home/azureuser/btreeBlock/verilog/delete/3/statement/10/vivado/reports/power.rpt
report_timing_summary -file /home/azureuser/btreeBlock/verilog/delete/3/statement/10/vivado/reports/statement/10/timing_summary.rpt
report_utilization -file /home/azureuser/btreeBlock/verilog/delete/3/statement/10/vivado/reports/utilization.rpt

place_design -directive AltSpreadLogic_high
write_checkpoint -force /home/azureuser/btreeBlock/verilog/delete/3/statement/10/vivado/dcp/place.dcp

route_design -directive Quick
write_checkpoint -force /home/azureuser/btreeBlock/verilog/delete/3/statement/10/vivado/dcp/route.dcp
report_timing_summary    -file /home/azureuser/btreeBlock/verilog/delete/3/statement/10/vivado/reports/timing_route.rpt

write_bitstream  -force /home/azureuser/btreeBlock/verilog/delete/3/statement/10/vivado/delete.bit
