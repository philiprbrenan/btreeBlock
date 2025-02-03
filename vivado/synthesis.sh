#!/bin/bash

# Set Vivado installation path
VIVADO_PATH="~/Vivado/2024.2/"

# Check if Vivado is installed
if [ ! -d "$VIVADO_PATH" ]; then
    echo "Vivado not found at $VIVADO_PATH"
    exit 1
fi

# Run Vivado in batch mode with the Tcl script
$VIVADO_PATH/bin/vivado -mode batch -source synthesize.tcl
