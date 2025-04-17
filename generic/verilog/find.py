#!/usr/bin/env python3

from siliconcompiler import Chip                                                # import python package
from siliconcompiler.targets import freepdk45_demo

if __name__ == "__main__":
    chip = Chip('find')                                                          # Create chip object
    chip.input('find.v')                                                        # Define list of source files
    chip.clock('clock', period=100)                                             # Define clock speed of design
    chip.use(freepdk45_demo)                                                    # Load predefined technology and flow target
    chip.set('option', 'remote', False)                                         # Run remote in the cloud
    chip.run()                                                                  # Run compilation of design and target
    chip.summary()
