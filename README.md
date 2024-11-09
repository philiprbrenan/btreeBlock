<div>
    <p><a href="https://github.com/philiprbrenan/btreeBlock"><img src="https://github.com/philiprbrenan/btreeBlock/workflows/Test/badge.svg"></a>
</div>

# Btree in a block

A [B-Tree](https://en.wikipedia.org/wiki/B-tree) in a block

```
                                                                                                                               32                                                                                                                                              |
                                                                                                                               0                                                                                                                                               |
                                                                                                                               11                                                                                                                                              |
                                                                                                                               10                                                                                                                                              |
                                                       16                                                                                                                                              48                                                                      |
                                                       11                                                                                                                                              10                                                                      |
                                                       9                                                                                                                                               39                                                                      |
                                                       25                                                                                                                                              46                                                                      |
          4          8                12                                20               24                 28                                  36               40                 44                                  52               56                 60                 |
          9          9.1              9.2                               25               25.1               25.2                                39               39.1               39.2                                46               46.1               46.2               |
          3          8                12                                19               13                 26                                  33               27                 40                                  48               41                 59                 |
                                      4                                                                     20                                                                      34                                                                      49                 |
1,2,3,4=3  5,6,7,8=8    9,10,11,12=12    13,14,15,16=4   17,18,19,20=19   21,22,23,24=13     25,26,27,28=26     29,30,31,32=20   33,34,35,36=33   37,38,39,40=27     41,42,43,44=40     45,46,47,48=34   49,50,51,52=48   53,54,55,56=41     57,58,59,60=59     61,62,63,64=49 |
```
