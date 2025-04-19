# BAM Basic Array Machine

This [folder](https://en.wikipedia.org/wiki/File_folder) attempts to reduce the [B-Tree](https://en.wikipedia.org/wiki/B-tree) algorithm to just [array](https://en.wikipedia.org/wiki/Dynamic_array) operations
with each [array](https://en.wikipedia.org/wiki/Dynamic_array) element a fixed size and a very limited number of operations
that can be performed on the [array](https://en.wikipedia.org/wiki/Dynamic_array) elements.  Doing so enables us to [write](https://en.wikipedia.org/wiki/Write_(system_call)) a
very reduced instruction set generic [CPU](https://en.wikipedia.org/wiki/Central_processing_unit) on which the [B-Tree](https://en.wikipedia.org/wiki/B-tree) algorithm can be
run to compare the performance of a generic [CPU](https://en.wikipedia.org/wiki/Central_processing_unit) implementing generic operations
with a specific [CPU](https://en.wikipedia.org/wiki/Central_processing_unit) implementating operations specific to the [B-Tree](https://en.wikipedia.org/wiki/B-tree) algorithm.

We can then compare the performance of the two [CPU](https://en.wikipedia.org/wiki/Central_processing_unit) to see which is faster by
running both [CPUs](https://en.wikipedia.org/wiki/Central_processing_unit) on the same sized [trees](https://en.wikipedia.org/wiki/Tree_(data_structure)) on the same [Field Programmable Gate Array](https://en.wikipedia.org/wiki/Field-programmable_gate_array) .

If the specifically designed [CPU](https://en.wikipedia.org/wiki/Central_processing_unit) is significantly faster than the generic [CPU](https://en.wikipedia.org/wiki/Central_processing_unit) when running the [B-Tree](https://en.wikipedia.org/wiki/B-tree) algorithm, then we can, like Rabbit, say **Aha** and go
and [find](https://en.wikipedia.org/wiki/Find_(Unix)) some-one to sell it to.
