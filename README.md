<div>
   <p><a href="https://github.com/philiprbrenan/btreeBlock"><img src="https://github.com/philiprbrenan/btreeBlock/workflows/Test/badge.svg"></a>
</div>

# Btree in a block

An implementation of the [B-Tree](https://en.wikipedia.org/wiki/B-tree) algorithm in synthesized, placed and routed
Verilog targeted at a [Advanced Micro Devices XC7Z020](https://docs.amd.com/v/u/en-US/zynq-7000-product-selection-guide) [Field Programmable Gate Array](https://en.wikipedia.org/wiki/Field-programmable_gate_array)  .

For reasons why you might want to get involved in this implementation of the [B-Tree](https://en.wikipedia.org/wiki/B-tree) algorithm in [Silicon](https://en.wikipedia.org/wiki/Silicon) rather than [software](https://en.wikipedia.org/wiki/Software) see:

https://prb.appaapps.com/zesal/presentation/index.html

A more detailed presentation:

http://prb.appaapps.com/zesal/pitchdeck/pitchDeck.html

# Installation

See this [action](https://github.com/philiprbrenan/btreeBlock/blob/main/.github/workflows/main.yml).

# Roadmap: custom [CPU](https://en.wikipedia.org/wiki/Central_processing_unit) vs generic [CPU](https://en.wikipedia.org/wiki/Central_processing_unit) 
The [B-Tree](https://en.wikipedia.org/wiki/B-tree) algorithm has been implemented in normal [Java](https://en.wikipedia.org/wiki/Java_(programming_language)), then successively
reduced until it looked just like [assembler](https://en.wikipedia.org/wiki/Assembly_language#Assembler) [code](https://en.wikipedia.org/wiki/Computer_program), at which point it was easy to
generate a custom [CPU](https://en.wikipedia.org/wiki/Central_processing_unit) in [Verilog](https://en.wikipedia.org/wiki/Verilog) to execute the [assembler](https://en.wikipedia.org/wiki/Assembly_language#Assembler) [code](https://en.wikipedia.org/wiki/Computer_program).  The resulting
design was compacted by reusing identical instructions and pipelined to reduce
congestion. ![Roadmap](flowChart/DevelopmentFlowChart.png)

This produced a custom [CPU](https://en.wikipedia.org/wiki/Central_processing_unit) with an instruction set specifically designed to
facilitate the **put**, **find** and **delete** operations on a [B-Tree](https://en.wikipedia.org/wiki/B-tree) .

The same  [B-Tree](https://en.wikipedia.org/wiki/B-tree) algorithm has now been implemented on a generic [reduced instruction set computer](https://en.wikipedia.org/wiki/Reduced_instruction_set_computer) [CPU](https://en.wikipedia.org/wiki/Central_processing_unit) so that the performance of the generic [CPU](https://en.wikipedia.org/wiki/Central_processing_unit) versus the performance of the custom [CPU](https://en.wikipedia.org/wiki/Central_processing_unit) can be compared.

![ð¢ HMS Rattler vs. HMS Alecto (1845)](https://prb.appaapps.com/zesal/presentation/images/SpecializedVersusGeneric.jpg)

# The custom CPU is 62 * better than the generic CPU

Performance as measured by [Open Road](https://openroad-flow-scripts.readthedocs.io/en/latest/mainREADME.html) for the custom [CPU](https://en.wikipedia.org/wiki/Central_processing_unit) versus the generic [CPU](https://en.wikipedia.org/wiki/Central_processing_unit) on a 2/3 keys per leaf/branch [B-Tree](https://en.wikipedia.org/wiki/B-tree) .  The performance gap widens as we
increase the number of keys in each branch and leaf towards 2**20/2**20+1
leaf/branch [B-Tree](https://en.wikipedia.org/wiki/B-tree). 
<p><table cellpadding="10"  cellspacing="10" border=1>
<tr><th colspan=2>Area Î¼mÂ²<th colspan=2>Fmax MHz<th colspan=2>Statements
<tr><th>Custom<th>Generic <th>Custom<th>Generic<th>Custom<th>Generic
<tr><td>2467<td>12927<td>902<td>399<td>23<td>122
<tr><td colspan=2> 5.24 x Smaller<td colspan=2> 2.26 x Faster<td colspan=2> 5.30 * Compact Code
<tr><td colspan=4 style="text-align: center;"><b>62.76</b> x better
</table>

In the above **area** in micro meters squared equates to **power** consumption
while **frequency** in mega hertz equates to **execution speed**. And even
better a [find](https://en.wikipedia.org/wiki/Find_(Unix)) operation takes far fewer statements to execute on the custom [CPU](https://en.wikipedia.org/wiki/Central_processing_unit) than it does on the generic [CPU](https://en.wikipedia.org/wiki/Central_processing_unit) because the operation codes are so much better
adapted to manipulating a [B-Tree](https://en.wikipedia.org/wiki/B-tree) .

## Custom CPU

![custom](https://raw.githubusercontent.com/philiprbrenan/btreeBlock/refs/heads/main/custom/find.png)


## Generic CPU

![generic](https://raw.githubusercontent.com/philiprbrenan/btreeBlock/refs/heads/main/generic/find.png)


# Synthesis, Placement and Routing

## Vivado

The logs of the successful synthesis, placement and routing of the individual
components of the custom [CPU](https://en.wikipedia.org/wiki/Central_processing_unit) for [Database on a Chip](http://prb.appaapps.com/zesal/pitchdeck/pitchDeck.html) using [Vivado](https://en.wikipedia.org/wiki/Xilinx_Vivado) targeting an [Advanced Micro Devices XC7A50T](https://www.amd.com/en/products/adaptive-socs-and-fpgas/fpga/artix-7.html#product-table) [Field Programmable Gate Array](https://en.wikipedia.org/wiki/Field-programmable_gate_array) can be seen here:


 [delete](https://github.com/philiprbrenan/btreeBlock/blob/main/verilog/delete/vivado/reports),

 [find  ](https://github.com/philiprbrenan/btreeBlock/tree/main/verilog/find/vivado/reports),

 [put   ](https://github.com/philiprbrenan/btreeBlock/blob/main/verilog/put/vivado/reports).


## OpenRoad

Open Road successfully compiles the ``find``, ``delete`` and ``put`` operations
implemented by the custom [CPU](https://en.wikipedia.org/wiki/Central_processing_unit) onto [freepdk-45nm](https://github.com/mflowgen/freepdk-45nm) [Silicon](https://en.wikipedia.org/wiki/Silicon) .  The following images
show the layout on [Silicon](https://en.wikipedia.org/wiki/Silicon) for each operation:

### Put

 ![put   ](https://github.com/philiprbrenan/btreeBlock/blob/main/siliconCompiler/build/put/put.png),

### Delete

 ![delete](https://github.com/philiprbrenan/btreeBlock/blob/main/siliconCompiler/build/delete/delete.png),

### Find

 ![find  ](https://github.com/philiprbrenan/btreeBlock/blob/main/siliconCompiler/build/find/find.png).

## Gowin Tang Nano 9K

The ``find`` operation of [the](https://en.wikipedia.org/wiki/The)  custom [CPU](https://en.wikipedia.org/wiki/Central_processing_unit) is small enough to be run on a Gowin Tang Nano 9K [Field Programmable Gate Array](https://en.wikipedia.org/wiki/Field-programmable_gate_array), see:

![Nano 9k](https://github.com/philiprbrenan/btreeBlock/blob/main/images/first_light_doc_nano9K.jpg)

In the image, the power light is the LED at the bottom, then reading up the
first **4** leds from the power LED having values **8**, **4**, **2**, **1**
are signalling that the [data](https://en.wikipedia.org/wiki/Data) associated with [database key](https://en.wikipedia.org/wiki/Key%E2%80%93value_database) **5** is **4** or in binary
**P 0100F0** where **F** is powered on showing that the [database key](https://en.wikipedia.org/wiki/Key%E2%80%93value_database) was successfully
found in the [B-Tree](https://en.wikipedia.org/wiki/B-tree) .

# Example: finding the [data](https://en.wikipedia.org/wiki/Data) associated with a [database key](https://en.wikipedia.org/wiki/Key%E2%80%93value_database) 
For a small [tree](https://en.wikipedia.org/wiki/Tree_(data_structure)): 
```
   BtreeSF t = new BtreeSF()
     {int maxSize         () {return  8;}
      int maxKeysPerLeaf  () {return  2;}
      int maxKeysPerBranch() {return  3;}
      int bitsPerKey      () {return  4;}
      int bitsPerData     () {return  4;}
     };
```

The [tree](https://en.wikipedia.org/wiki/Tree_(data_structure)) being searched looks like this:

```
    ok(t, """
             4                    |
             0                    |
             5                    |
             6                    |
      2             6    7        |
      5             6    6.1      |
      1             4    7        |
      3                  2        |
1,2=1  3,4=3  5,6=4  7=7    8,9=2 |
""");
```

Running the generated [Verilog](https://en.wikipedia.org/wiki/Verilog) [code](https://en.wikipedia.org/wiki/Computer_program) to [find](https://en.wikipedia.org/wiki/Find_(Unix)) the [data](https://en.wikipedia.org/wiki/Data) associated with [database key](https://en.wikipedia.org/wiki/Key%E2%80%93value_database) **2**
produces:

```
Stopped after:  117 steps key    2  data    7
```

# Deleting in ascending order

The evolution of a [B-Tree](https://en.wikipedia.org/wiki/B-tree) as the lowest element is deleted successively until
the [tree](https://en.wikipedia.org/wiki/Tree_(data_structure)) is empty.

```
At start with 32 elements
+----------------------------------------------------------------------------------------------------------------------------+
|                                                        16                                24                                |
|                                                        0                                 0.1                               |
|                                                        7                                 18                                |
|                                                                                          8                                 |
|           4          8               12                                 20                                 28              |
|           7          7.1             7.2                                18                                 8               |
|           1          4               6                                  12                                 17              |
|                                      10                                 15                                 2               |
| 1,2,3,4=1  5,6,7,8=4    9,10,11,12=6    13,14,15,16=10   17,18,19,20=12   21,22,23,24=15    25,26,27,28=17   29,30,31,32=2 |
+----------------------------------------------------------------------------------------------------------------------------+
After deleting: 1
+-----------------------------------------------------------------------------------------------------------------------------+
|                                                      16                                                                     |
|                                                      0                                                                      |
|                                                      7                                                                      |
|                                                      18                                                                     |
|         4          8               12                                 20               24                 28                |
|         7          7.1             7.2                                18               18.1               18.2              |
|         1          4               6                                  12               15                 17                |
|                                    10                                                                     2                 |
| 2,3,4=1  5,6,7,8=4    9,10,11,12=6    13,14,15,16=10   17,18,19,20=12   21,22,23,24=15     25,26,27,28=17     29,30,31,32=2 |
+-----------------------------------------------------------------------------------------------------------------------------+
After deleting: 2
+---------------------------------------------------------------------------------------------------------------------------+
|                                                    16                                                                     |
|                                                    0                                                                      |
|                                                    7                                                                      |
|                                                    18                                                                     |
|       4          8               12                                 20               24                 28                |
|       7          7.1             7.2                                18               18.1               18.2              |
|       1          4               6                                  12               15                 17                |
|                                  10                                                                     2                 |
| 3,4=1  5,6,7,8=4    9,10,11,12=6    13,14,15,16=10   17,18,19,20=12   21,22,23,24=15     25,26,27,28=17     29,30,31,32=2 |
+---------------------------------------------------------------------------------------------------------------------------+
After deleting: 3
+-------------------------------------------------------------------------------------------------------------------------+
|                                                  16                                                                     |
|                                                  0                                                                      |
|                                                  7                                                                      |
|                                                  18                                                                     |
|     4          8               12                                 20               24                 28                |
|     7          7.1             7.2                                18               18.1               18.2              |
|     1          4               6                                  12               15                 17                |
|                                10                                                                     2                 |
| 4=1  5,6,7,8=4    9,10,11,12=6    13,14,15,16=10   17,18,19,20=12   21,22,23,24=15     25,26,27,28=17     29,30,31,32=2 |
+-------------------------------------------------------------------------------------------------------------------------+
After deleting: 4
+------------------------------------------------------------------------------------------------------------------+
|                                           16                                                                     |
|                                           0                                                                      |
|                                           7                                                                      |
|                                           18                                                                     |
|           8             12                                 20               24                 28                |
|           7             7.1                                18               18.1               18.2              |
|           1             6                                  12               15                 17                |
|                         10                                                                     2                 |
| 5,6,7,8=1  9,10,11,12=6    13,14,15,16=10   17,18,19,20=12   21,22,23,24=15     25,26,27,28=17     29,30,31,32=2 |
+------------------------------------------------------------------------------------------------------------------+
After deleting: 5
+----------------------------------------------------------------------------------------------------------------+
|                                         16                                                                     |
|                                         0                                                                      |
|                                         7                                                                      |
|                                         18                                                                     |
|         8             12                                 20               24                 28                |
|         7             7.1                                18               18.1               18.2              |
|         1             6                                  12               15                 17                |
|                       10                                                                     2                 |
| 6,7,8=1  9,10,11,12=6    13,14,15,16=10   17,18,19,20=12   21,22,23,24=15     25,26,27,28=17     29,30,31,32=2 |
+----------------------------------------------------------------------------------------------------------------+
After deleting: 6
+--------------------------------------------------------------------------------------------------------------+
|                                       16                                                                     |
|                                       0                                                                      |
|                                       7                                                                      |
|                                       18                                                                     |
|       8             12                                 20               24                 28                |
|       7             7.1                                18               18.1               18.2              |
|       1             6                                  12               15                 17                |
|                     10                                                                     2                 |
| 7,8=1  9,10,11,12=6    13,14,15,16=10   17,18,19,20=12   21,22,23,24=15     25,26,27,28=17     29,30,31,32=2 |
+--------------------------------------------------------------------------------------------------------------+
After deleting: 7
+------------------------------------------------------------------------------------------------------------+
|                                     16                                                                     |
|                                     0                                                                      |
|                                     7                                                                      |
|                                     18                                                                     |
|     8             12                                 20               24                 28                |
|     7             7.1                                18               18.1               18.2              |
|     1             6                                  12               15                 17                |
|                   10                                                                     2                 |
| 8=1  9,10,11,12=6    13,14,15,16=10   17,18,19,20=12   21,22,23,24=15     25,26,27,28=17     29,30,31,32=2 |
+------------------------------------------------------------------------------------------------------------+
After deleting: 8
+------------------------------------------------------------------------------------------------------+
|                               16                                                                     |
|                               0                                                                      |
|                               7                                                                      |
|                               18                                                                     |
|              12                                20               24                 28                |
|              7                                 18               18.1               18.2              |
|              1                                 12               15                 17                |
|              10                                                                    2                 |
| 9,10,11,12=1   13,14,15,16=10   17,18,19,20=12   21,22,23,24=15     25,26,27,28=17     29,30,31,32=2 |
+------------------------------------------------------------------------------------------------------+
After deleting: 9
+---------------------------------------------------------------------------------------------------+
|                                               20                                                  |
|                                               0                                                   |
|                                               7                                                   |
|                                               18                                                  |
|            12               16                                 24               28                |
|            7                7.1                                18               18.1              |
|            1                10                                 15               17                |
|                             12                                                  2                 |
| 10,11,12=1   13,14,15,16=10    17,18,19,20=12   21,22,23,24=15   25,26,27,28=17     29,30,31,32=2 |
+---------------------------------------------------------------------------------------------------+
After deleting: 10
+------------------------------------------------------------------------------------------------+
|                                            20                                                  |
|                                            0                                                   |
|                                            7                                                   |
|                                            18                                                  |
|         12               16                                 24               28                |
|         7                7.1                                18               18.1              |
|         1                10                                 15               17                |
|                          12                                                  2                 |
| 11,12=1   13,14,15,16=10    17,18,19,20=12   21,22,23,24=15   25,26,27,28=17     29,30,31,32=2 |
+------------------------------------------------------------------------------------------------+
After deleting: 11
+---------------------------------------------------------------------------------------------+
|                                         20                                                  |
|                                         0                                                   |
|                                         7                                                   |
|                                         18                                                  |
|      12               16                                 24               28                |
|      7                7.1                                18               18.1              |
|      1                10                                 15               17                |
|                       12                                                  2                 |
| 12=1   13,14,15,16=10    17,18,19,20=12   21,22,23,24=15   25,26,27,28=17     29,30,31,32=2 |
+---------------------------------------------------------------------------------------------+
After deleting: 12
+------------------------------------------------------------------------------------+
|                                20                                                  |
|                                0                                                   |
|                                7                                                   |
|                                18                                                  |
|               16                                24               28                |
|               7                                 18               18.1              |
|               1                                 15               17                |
|               12                                                 2                 |
| 13,14,15,16=1   17,18,19,20=12   21,22,23,24=15   25,26,27,28=17     29,30,31,32=2 |
+------------------------------------------------------------------------------------+
After deleting: 13
+--------------------------------------------------------------------------------+
|                                               24                               |
|                                               0                                |
|                                               7                                |
|                                               18                               |
|            16               20                                 28              |
|            7                7.1                                18              |
|            1                12                                 17              |
|                             15                                 2               |
| 14,15,16=1   17,18,19,20=12    21,22,23,24=15   25,26,27,28=17   29,30,31,32=2 |
+--------------------------------------------------------------------------------+
After deleting: 14
+-----------------------------------------------------------------------------+
|                                            24                               |
|                                            0                                |
|                                            7                                |
|                                            18                               |
|         16               20                                 28              |
|         7                7.1                                18              |
|         1                12                                 17              |
|                          15                                 2               |
| 15,16=1   17,18,19,20=12    21,22,23,24=15   25,26,27,28=17   29,30,31,32=2 |
+-----------------------------------------------------------------------------+
After deleting: 15
+--------------------------------------------------------------------------+
|                                         24                               |
|                                         0                                |
|                                         7                                |
|                                         18                               |
|      16               20                                 28              |
|      7                7.1                                18              |
|      1                12                                 17              |
|                       15                                 2               |
| 16=1   17,18,19,20=12    21,22,23,24=15   25,26,27,28=17   29,30,31,32=2 |
+--------------------------------------------------------------------------+
After deleting: 16
+-----------------------------------------------------------------+
|                                24                               |
|                                0                                |
|                                7                                |
|                                18                               |
|               20                                28              |
|               7                                 18              |
|               1                                 17              |
|               15                                2               |
| 17,18,19,20=1   21,22,23,24=15   25,26,27,28=17   29,30,31,32=2 |
+-----------------------------------------------------------------+
After deleting: 17
+----------------------------------------------------------------+
|            20               24                28               |
|            0                0.1               0.2              |
|            1                15                17               |
|                                               2                |
| 18,19,20=1   21,22,23,24=15    25,26,27,28=17    29,30,31,32=2 |
+----------------------------------------------------------------+
After deleting: 18
+-------------------------------------------------------------+
|         20               24                28               |
|         0                0.1               0.2              |
|         1                15                17               |
|                                            2                |
| 19,20=1   21,22,23,24=15    25,26,27,28=17    29,30,31,32=2 |
+-------------------------------------------------------------+
After deleting: 19
+----------------------------------------------------------+
|      20               24                28               |
|      0                0.1               0.2              |
|      1                15                17               |
|                                         2                |
| 20=1   21,22,23,24=15    25,26,27,28=17    29,30,31,32=2 |
+----------------------------------------------------------+
After deleting: 20
+-------------------------------------------------+
|               24               28               |
|               0                0.1              |
|               1                17               |
|                                2                |
| 21,22,23,24=1   25,26,27,28=17    29,30,31,32=2 |
+-------------------------------------------------+
After deleting: 21
+----------------------------------------------+
|            24               28               |
|            0                0.1              |
|            1                17               |
|                             2                |
| 22,23,24=1   25,26,27,28=17    29,30,31,32=2 |
+----------------------------------------------+
After deleting: 22
+-------------------------------------------+
|         24               28               |
|         0                0.1              |
|         1                17               |
|                          2                |
| 23,24=1   25,26,27,28=17    29,30,31,32=2 |
+-------------------------------------------+
After deleting: 23
+----------------------------------------+
|      24               28               |
|      0                0.1              |
|      1                17               |
|                       2                |
| 24=1   25,26,27,28=17    29,30,31,32=2 |
+----------------------------------------+
After deleting: 24
+-------------------------------+
|               28              |
|               0               |
|               1               |
|               2               |
| 25,26,27,28=1   29,30,31,32=2 |
+-------------------------------+
After deleting: 25
+----------------------------+
|            28              |
|            0               |
|            1               |
|            2               |
| 26,27,28=1   29,30,31,32=2 |
+----------------------------+
After deleting: 26
+-------------------------+
|         28              |
|         0               |
|         1               |
|         2               |
| 27,28=1   29,30,31,32=2 |
+-------------------------+
After deleting: 27
+----------------------+
|      28              |
|      0               |
|      1               |
|      2               |
| 28=1   29,30,31,32=2 |
+----------------------+
After deleting: 28
+--------------+
| 29,30,31,32=0 |
+--------------+
After deleting: 29
+-----------+
| 30,31,32=0 |
+-----------+
After deleting: 30
+--------+
| 31,32=0 |
+--------+
After deleting: 31
+-----+
| 32=0 |
+-----+
After deleting: 32
+---+
| =0 |
+---+
```


# The sieve of Eratosthenes

The Sieve of Eratosthenes is an ancient and efficient algorithm used to [find](https://en.wikipedia.org/wiki/Find_(Unix)) all prime numbers up to a given number **N**. It works by iteratively removing
the the multiples of each prime number from an initial set of integers.

In this example, with **N** set to **64**, the initial set of integers was
successfully reduced to a set of primes by deleting the multiples of each
prime. A [B-Tree](https://en.wikipedia.org/wiki/B-tree) was used to hold the initial set of integers **1..64**. Deleting
the multiples of each prime produces leaves behind the primes as the leaves
upon the [tree](https://en.wikipedia.org/wiki/Tree_(data_structure)): 
```
             6                        17                               40                                 |
             0                        0.1                              0.2                                |
             5                        11                               23                                 |
                                                                       6                                  |
      2           8         16                 19         29                       43         53          |
      5           11        11.1               23         23.1                     6          6.1         |
      1           7         8                  14         18                       29         35          |
      3                     13                            26                                  21          |
1,2=1  3,5=3  7=7   11,13=8     17=13    19=14   23,29=18     31,37=26    41,43=29   47,53=35    59,61=21 |
```
