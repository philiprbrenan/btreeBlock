create_clock -period 11.2 [get_ports clock]

set_input_delay  -clock clock -min 0 [get_ports -filter {DIRECTION == IN && NAME != "clock"}]
set_input_delay  -clock clock -max 0 [get_ports -filter {DIRECTION == IN && NAME != "clock"}]

set_output_delay -clock clock -min 0 [get_ports -filter {DIRECTION == OUT}]
set_output_delay -clock clock -max 2 [get_ports -filter {DIRECTION == OUT}]

set_property PACKAGE_PIN K19  [get_ports clock]
set_property IOSTANDARD LVCMOS18 [get_ports clock]
set_property IOSTANDARD LVCMOS18 [get_ports {data[0]}]
set_property IOSTANDARD LVCMOS18 [get_ports {Data[0]}]
set_property IOSTANDARD LVCMOS18 [get_ports {data[1]}]
set_property IOSTANDARD LVCMOS18 [get_ports {Data[1]}]
set_property IOSTANDARD LVCMOS18 [get_ports {data[2]}]
set_property IOSTANDARD LVCMOS18 [get_ports {Data[2]}]
set_property IOSTANDARD LVCMOS18 [get_ports {data[3]}]
set_property IOSTANDARD LVCMOS18 [get_ports {Data[3]}]
set_property IOSTANDARD LVCMOS18 [get_ports found]
set_property IOSTANDARD LVCMOS18 [get_ports {Key[0]}]
set_property IOSTANDARD LVCMOS18 [get_ports {Key[1]}]
set_property IOSTANDARD LVCMOS18 [get_ports {Key[2]}]
set_property IOSTANDARD LVCMOS18 [get_ports {Key[3]}]
set_property IOSTANDARD LVCMOS18 [get_ports {Key[4]}]
set_property IOSTANDARD LVCMOS18 [get_ports reset]
set_property IOSTANDARD LVCMOS18 [get_ports stop]
set_property PACKAGE_PIN N22  [get_ports reset]
set_property PACKAGE_PIN P22  [get_ports found]
set_property PACKAGE_PIN R20  [get_ports stop]
set_property PACKAGE_PIN R21  [get_ports {Key[0]}]
set_property PACKAGE_PIN P20  [get_ports {Key[1]}]
set_property PACKAGE_PIN P21  [get_ports {Key[2]}]
set_property PACKAGE_PIN N15  [get_ports {Key[3]}]
set_property PACKAGE_PIN P15  [get_ports {Key[4]}]
set_property PACKAGE_PIN P17  [get_ports {data[0]}]
set_property PACKAGE_PIN P18  [get_ports {data[1]}]
set_property PACKAGE_PIN T16  [get_ports {data[2]}]
set_property PACKAGE_PIN T17  [get_ports {data[3]}]
set_property PACKAGE_PIN R19  [get_ports {Data[0]}]
set_property PACKAGE_PIN T19  [get_ports {Data[1]}]
set_property PACKAGE_PIN R18  [get_ports {Data[2]}]
set_property PACKAGE_PIN T18  [get_ports {Data[3]}]
#
