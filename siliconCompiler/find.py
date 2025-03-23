#!/usr/bin/env python3

from siliconcompiler import Chip             # import python package
from siliconcompiler.targets import skywater130_demo

if __name__ == "__main__":
    chip = Chip('find')                  # create chip object
    chip.input('/home/azureuser/btreeBlock/verilog/find/2/nano9k/find.v')                 # define list of source files
    chip.input('/home/azureuser/aaa/find.sdc')                 # define list of source files
    chip.clock('clock', period=100)              # define clock speed of design
    chip.use(skywater130_demo)                # load predefined technology and flow target
    chip.set('option', 'remote', False)        # run remote in the cloud
    chip.run()                                # run compilation of design and target
    chip.summary()                            # print results summary
