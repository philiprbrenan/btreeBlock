//------------------------------------------------------------------------------
// BTree in bit machine assembler.
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout  a binary tree on a silicon chip.

import java.util.*;

class MjafPut extends Mjaf                                                      // Put data into a BTree
 {

//D1 Construction                                                               // Create a BTree from nodes which can be branches or leaves.  The data associated with the BTree is stored only in the leaves opposite the keys

  MjafPut(int BitsPerKey, int BitsPerData, int MaxKeysPerLeaf, int size)        // Define a BTree with the specified dimensions
   {super(BitsPerKey, BitsPerData, MaxKeysPerLeaf, size);
   }

//D0 Tests                                                                      // Testing

  static void test_split_reduction()                                            // Only split the branches that really have to be split and recombine the iother side of the split if possible to create a denser tree
   {int BitsPerKey = 4, BitsPerData = 4, MaxKeysPerLeaf = 4, size = 10, N = 9;

    final Mjaf m = mjaf(BitsPerKey, BitsPerData, MaxKeysPerLeaf, size);
    for (int i = 1; i <= N; i++) m.put(i, i);
    m.execute();

    //stop(m.layout);
    //stop(m.print());
    ok(m.print(), """
          8(2-0)      7(6-0.1)9        |
1,2,3,4=8       5,6=7          7,8,9=9 |
""");

    Layout b = m.branchKeyNext.duplicate();
    Layout l = m.leafKeyData.duplicate();
    Stuck  s = new Stuck("unpack", 20, l);

    m.reset();
    m.new Say() {void action() {say("Start-----------------------------");}};
    m.setIndex(m.nodes, m.root);
    m.branchStuck.new Up("outer")
     {void up(Repeat r)
       {m.copy(b.top, value);
        m.new Say() {void action() {say("BBBB", name, index, b, b.get("branchNext").toVariable());}};
        m.setIndex(m.nodes, b.get("branchNext").toVariable());
        m.new Say() {void action() {say("CCCC", m.leaf);}};
        //m.leaf.new Up()
        // {void up(Repeat R)
        //    {m.copy(l.top, value);
        //     m.new Say() {void action() {say("LLLL", index, l);}};
        //    }
        // };
       }
     };
    //BitMachine.debug = true;
    m.execute();
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {
   }

  static void newTests()                                                        // Tests being worked on
   {//oldTests();
    test_split_reduction();
   }

  public static void main(String[] args)                                        // Test if called as a program
   {try                                                                         // Get a traceback in a format clickable in Geany if something goes wrong to speed up debugging.
     {if (github_actions) oldTests(); else newTests();                          // Tests to run
      testSummary();                                                            // Summarize test results
      System.exit(testsFailed);
     }
    catch(Exception e)                                                          // Get a traceback in a format clickable in Geany
     {System.err.println(e);
      System.err.println(fullTraceBack(e));
      System.exit(1);
     }
   }
 }
