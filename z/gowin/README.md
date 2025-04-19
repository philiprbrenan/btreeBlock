# How to run Database on a Chip on the Tang Nano 9K

The ``Nano 9k`` is just large enough to run ``find``, but it is not big enough to run ``delete`` or ``put``.

Change to [folder](https://en.wikipedia.org/wiki/File_folder): ```btreeBlock/verilog/find/1/nano9k/```

Plug the ``Nano 9K`` into a ``USB`` slot.  Observe that the ``LED``s flash.

To [program](https://en.wikipedia.org/wiki/Computer_program) the ``Nano 9K``, run:

```
perl find.pl
```

The [tree](https://en.wikipedia.org/wiki/Tree_(data_structure)) preloaded into the ``Nano 9K`` is:

```
             4                    |
             0                    |
             5                    |
             6                    |
      2             6    7        |
      5             6    6.1      |
      1             3    8        |
      4                  2        |
1,2=1  3,4=4  5,6=3  7=8    8,9=2 |
```

Searching for the [database key](https://en.wikipedia.org/wiki/Key%E2%80%93value_database) below will switch the leds as shown reading left to right
below away from the power light on the ``Nano 9K``.

```
 Key   LEDs
   0   000000
   1   100010
   2   011110
   3   011010
   4   010110
   5   010010
   6   001110
   7   001010
   8   000110
   9   000010
  10   000000
```
