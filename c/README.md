# C [version](https://en.wikipedia.org/wiki/Software_versioning) 
The goal is to produce a [C programming language](https://b-ok.xyz/book/633119/db5c78) language [version](https://en.wikipedia.org/wiki/Software_versioning) of the [B-Tree](https://en.wikipedia.org/wiki/B-tree) algorithm and run it
on a generic soft [CPU](https://en.wikipedia.org/wiki/Central_processing_unit) on the same [Field Programmable Gate Array](https://en.wikipedia.org/wiki/Field-programmable_gate_array) used to run the [B-Tree](https://en.wikipedia.org/wiki/B-tree) algorithm on a [CPU](https://en.wikipedia.org/wiki/Central_processing_unit) specifically designed for the [B-Tree](https://en.wikipedia.org/wiki/B-tree) algorithm; then compare the performance of
the [B-Tree](https://en.wikipedia.org/wiki/B-tree) algorithm on the generic [CPU](https://en.wikipedia.org/wiki/Central_processing_unit) against its perfomence on the
specifically designed [CPU](https://en.wikipedia.org/wiki/Central_processing_unit). 
If the specifically designed [CPU](https://en.wikipedia.org/wiki/Central_processing_unit) is significantly faster than the generic [CPU](https://en.wikipedia.org/wiki/Central_processing_unit) when running the [B-Tree](https://en.wikipedia.org/wiki/B-tree) algorithm, then we can, like Rabbit, say **Aha** and go
and try to sell it to someone.

# Build

```
perl riscV.pl
```

This will create the ``branch`` and ``leaf`` stucks in [C programming language](https://b-ok.xyz/book/633119/db5c78) and compile them
with ``btree.c`` to generate a [RiscV](https://en.wikipedia.org/wiki/RISC-V) [assembler](https://en.wikipedia.org/wiki/Assembly_language#Assembler) [code](https://en.wikipedia.org/wiki/Computer_program) of the [B-Tree](https://en.wikipedia.org/wiki/B-tree) algoritm. This [code](https://en.wikipedia.org/wiki/Computer_program) is then represented in ``RiscV.java`` as [Java](https://en.wikipedia.org/wiki/Java_(programming_language)) which can  be run to emulate
the execution of the [assembler](https://en.wikipedia.org/wiki/Assembly_language#Assembler) [code](https://en.wikipedia.org/wiki/Computer_program) and from which corresponding [Verilog](https://en.wikipedia.org/wiki/Verilog) [code](https://en.wikipedia.org/wiki/Computer_program) can be generated to run the [B-Tree](https://en.wikipedia.org/wiki/B-tree) algorithm on a soft [RiscV](https://en.wikipedia.org/wiki/RISC-V) [CPU](https://en.wikipedia.org/wiki/Central_processing_unit). 
The question is then whether the specialized [B-Tree](https://en.wikipedia.org/wiki/B-tree) processor can execute [B-Tree](https://en.wikipedia.org/wiki/B-tree) operations faster using less power and less [Silicon](https://en.wikipedia.org/wiki/Silicon) than can a generic
processor executing equivalent [RiscV](https://en.wikipedia.org/wiki/RISC-V) [assembler](https://en.wikipedia.org/wiki/Assembly_language#Assembler) [code](https://en.wikipedia.org/wiki/Computer_program). 