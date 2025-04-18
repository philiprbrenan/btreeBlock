###############################################################################
# Created by write_sdc
# Fri Apr 18 02:31:37 2025
###############################################################################
current_design find
###############################################################################
# Timing Constraints
###############################################################################
create_clock -name clk0 -period 100.0000 [get_ports {clock}]
set_clock_uncertainty 0.0000 clk0
set_propagated_clock [get_clocks {clk0}]
###############################################################################
# Environment
###############################################################################
set_load -pin_load 9.7466 [get_ports {found}]
set_load -pin_load 9.7466 [get_ports {stop}]
set_load -pin_load 9.7466 [get_ports {data[15]}]
set_load -pin_load 9.7466 [get_ports {data[14]}]
set_load -pin_load 9.7466 [get_ports {data[13]}]
set_load -pin_load 9.7466 [get_ports {data[12]}]
set_load -pin_load 9.7466 [get_ports {data[11]}]
set_load -pin_load 9.7466 [get_ports {data[10]}]
set_load -pin_load 9.7466 [get_ports {data[9]}]
set_load -pin_load 9.7466 [get_ports {data[8]}]
set_load -pin_load 9.7466 [get_ports {data[7]}]
set_load -pin_load 9.7466 [get_ports {data[6]}]
set_load -pin_load 9.7466 [get_ports {data[5]}]
set_load -pin_load 9.7466 [get_ports {data[4]}]
set_load -pin_load 9.7466 [get_ports {data[3]}]
set_load -pin_load 9.7466 [get_ports {data[2]}]
set_load -pin_load 9.7466 [get_ports {data[1]}]
set_load -pin_load 9.7466 [get_ports {data[0]}]
set_driving_cell -lib_cell BUF_X1 -pin {Z} -input_transition_rise 0.0000 -input_transition_fall 0.0000 [get_ports {clock}]
set_driving_cell -lib_cell BUF_X1 -pin {Z} -input_transition_rise 0.0000 -input_transition_fall 0.0000 [get_ports {reset}]
set_driving_cell -lib_cell BUF_X1 -pin {Z} -input_transition_rise 0.0000 -input_transition_fall 0.0000 [get_ports {Data[15]}]
set_driving_cell -lib_cell BUF_X1 -pin {Z} -input_transition_rise 0.0000 -input_transition_fall 0.0000 [get_ports {Data[14]}]
set_driving_cell -lib_cell BUF_X1 -pin {Z} -input_transition_rise 0.0000 -input_transition_fall 0.0000 [get_ports {Data[13]}]
set_driving_cell -lib_cell BUF_X1 -pin {Z} -input_transition_rise 0.0000 -input_transition_fall 0.0000 [get_ports {Data[12]}]
set_driving_cell -lib_cell BUF_X1 -pin {Z} -input_transition_rise 0.0000 -input_transition_fall 0.0000 [get_ports {Data[11]}]
set_driving_cell -lib_cell BUF_X1 -pin {Z} -input_transition_rise 0.0000 -input_transition_fall 0.0000 [get_ports {Data[10]}]
set_driving_cell -lib_cell BUF_X1 -pin {Z} -input_transition_rise 0.0000 -input_transition_fall 0.0000 [get_ports {Data[9]}]
set_driving_cell -lib_cell BUF_X1 -pin {Z} -input_transition_rise 0.0000 -input_transition_fall 0.0000 [get_ports {Data[8]}]
set_driving_cell -lib_cell BUF_X1 -pin {Z} -input_transition_rise 0.0000 -input_transition_fall 0.0000 [get_ports {Data[7]}]
set_driving_cell -lib_cell BUF_X1 -pin {Z} -input_transition_rise 0.0000 -input_transition_fall 0.0000 [get_ports {Data[6]}]
set_driving_cell -lib_cell BUF_X1 -pin {Z} -input_transition_rise 0.0000 -input_transition_fall 0.0000 [get_ports {Data[5]}]
set_driving_cell -lib_cell BUF_X1 -pin {Z} -input_transition_rise 0.0000 -input_transition_fall 0.0000 [get_ports {Data[4]}]
set_driving_cell -lib_cell BUF_X1 -pin {Z} -input_transition_rise 0.0000 -input_transition_fall 0.0000 [get_ports {Data[3]}]
set_driving_cell -lib_cell BUF_X1 -pin {Z} -input_transition_rise 0.0000 -input_transition_fall 0.0000 [get_ports {Data[2]}]
set_driving_cell -lib_cell BUF_X1 -pin {Z} -input_transition_rise 0.0000 -input_transition_fall 0.0000 [get_ports {Data[1]}]
set_driving_cell -lib_cell BUF_X1 -pin {Z} -input_transition_rise 0.0000 -input_transition_fall 0.0000 [get_ports {Data[0]}]
set_driving_cell -lib_cell BUF_X1 -pin {Z} -input_transition_rise 0.0000 -input_transition_fall 0.0000 [get_ports {Key[15]}]
set_driving_cell -lib_cell BUF_X1 -pin {Z} -input_transition_rise 0.0000 -input_transition_fall 0.0000 [get_ports {Key[14]}]
set_driving_cell -lib_cell BUF_X1 -pin {Z} -input_transition_rise 0.0000 -input_transition_fall 0.0000 [get_ports {Key[13]}]
set_driving_cell -lib_cell BUF_X1 -pin {Z} -input_transition_rise 0.0000 -input_transition_fall 0.0000 [get_ports {Key[12]}]
set_driving_cell -lib_cell BUF_X1 -pin {Z} -input_transition_rise 0.0000 -input_transition_fall 0.0000 [get_ports {Key[11]}]
set_driving_cell -lib_cell BUF_X1 -pin {Z} -input_transition_rise 0.0000 -input_transition_fall 0.0000 [get_ports {Key[10]}]
set_driving_cell -lib_cell BUF_X1 -pin {Z} -input_transition_rise 0.0000 -input_transition_fall 0.0000 [get_ports {Key[9]}]
set_driving_cell -lib_cell BUF_X1 -pin {Z} -input_transition_rise 0.0000 -input_transition_fall 0.0000 [get_ports {Key[8]}]
set_driving_cell -lib_cell BUF_X1 -pin {Z} -input_transition_rise 0.0000 -input_transition_fall 0.0000 [get_ports {Key[7]}]
set_driving_cell -lib_cell BUF_X1 -pin {Z} -input_transition_rise 0.0000 -input_transition_fall 0.0000 [get_ports {Key[6]}]
set_driving_cell -lib_cell BUF_X1 -pin {Z} -input_transition_rise 0.0000 -input_transition_fall 0.0000 [get_ports {Key[5]}]
set_driving_cell -lib_cell BUF_X1 -pin {Z} -input_transition_rise 0.0000 -input_transition_fall 0.0000 [get_ports {Key[4]}]
set_driving_cell -lib_cell BUF_X1 -pin {Z} -input_transition_rise 0.0000 -input_transition_fall 0.0000 [get_ports {Key[3]}]
set_driving_cell -lib_cell BUF_X1 -pin {Z} -input_transition_rise 0.0000 -input_transition_fall 0.0000 [get_ports {Key[2]}]
set_driving_cell -lib_cell BUF_X1 -pin {Z} -input_transition_rise 0.0000 -input_transition_fall 0.0000 [get_ports {Key[1]}]
set_driving_cell -lib_cell BUF_X1 -pin {Z} -input_transition_rise 0.0000 -input_transition_fall 0.0000 [get_ports {Key[0]}]
###############################################################################
# Design Rules
###############################################################################
