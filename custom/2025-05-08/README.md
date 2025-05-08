```
  private static BtreeSF allTreeOps()
   {return new BtreeSF()
     {int maxSize         () {return 16;}
      int maxKeysPerLeaf  () {return  2;}
      int maxKeysPerBranch() {return  3;}
      int bitsPerKey      () {return  5;}
      int bitsPerData     () {return  4;}
     };
   }
```
 Route time was  one hour. Density was default.
