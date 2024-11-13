//------------------------------------------------------------------------------
// BTree in a block
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout a btree in a block on the surface of a silicon chip.

import java.util.*;

class Btree extends Test                                                        // Manipulate a btree
 {final int
    maxKeysPerLeaf,                                                             // Maximum number of leafs in a key
    maxKeysPerBranch,                                                           // Maximum number of keys in a branch
    splitLeafSize,                                                              // The number of key, data pairs to split out of a leaf
    splitBranchSize;                                                            // The number of key, next pairs to split out of a branch

  final Node         root;                                                      // The root of the tree
  final Stack<Node>  nodes = new Stack<>();                                     // The leaves and branches comprising the tree

  final static int
    linesToPrintABranch =  4,                                                   // The number of lines required to print a branch
         maxPrintLevels = 10,                                                   // Maximum number of levels to print in a tree
               maxDepth =  6;                                                   // Maximum depth of any realistic tree

  int       maxNodeUsed = 0;                                                    // Maximum branch or leaf index used
  static boolean debug  = false;                                                // Debugging enabled

//D1 Construction                                                               // Create a BTree from nodes which can be branches or leaves.  The data associated with the BTree is stored only in the leaves opposite the keys

  Btree(int MaxKeysPerLeaf, int MaxKeysPerBranch)                               // Define a BTree with the specified dimensions
   {maxKeysPerLeaf    = MaxKeysPerLeaf;
    maxKeysPerBranch  = MaxKeysPerBranch;
    splitLeafSize     = maxKeysPerLeaf   >> 1;
    splitBranchSize   = maxKeysPerBranch >> 1;
    nodes.push(root = new Node());                                              // The root
    root.isLeaf = true;                                                         // The root starts as a leaf
   }

//D1 Control                                                                    // Testing, control and integrity

  void ok(String expected) {Test.ok(toString(), expected);}                     // Confirm tree is as expected
  void stop()              {Test.stop(toString());}                             // Stop after printing the tree

//D1 Components                                                                 // A branch or leaf in the tree

  class Node                                                                    // A branch or leaf in the tree
   {boolean isLeaf;                                                             // A leaf if true
    static int nodeCount = 0;                                                   // Unique number for each node
    final  int node = nodeCount++;

    final Stack<KeyData> keyData = new Stack<>();                               // Key, data pairs when a leaf
    final Stack<KeyNext> keyNext = new Stack<>();                               // Key, next pairs when a leaf. The last entry is top and so this stack must always have at least one element in it when the node is acting as a branch

    void assertLeaf()   {if (!isLeaf) stop("Leaf required");}
    void assertBranch() {if ( isLeaf) stop("Branch required");}

    int leafSize()   {return keyData.size();}
    int branchSize() {return keyNext.size() - 1;}                               // The  top most entry contains only the top

    int top()
     {assertBranch();
      return keyNext.elementAt(branchSize()).next;
     }

    class FindEqualInLeaf                                                       // Find the first key in the leaf that is equal to the search key
     {final int    search;                                                      // Search key
      final boolean found;                                                      // Whether the key was found
      final int      data;                                                      // Data associated with the  key
      final int     index;                                                      // Index of first such key if found

      FindEqualInLeaf(int Search)                                               // Find the first key in the leaf that is equal to the search key
       {assertLeaf();
        search = Search;
        boolean looking = true;
        final int N = leafSize();
        int i;
        for (i = 0; i < N && looking; i++)                                      // Compare with each valid key
         {z();
          if (keyData.elementAt(i).key == search)
           {looking = false; break;
           }
         }
        if (looking)                                                            // Not found
         {data = -1; index = -1; found = false;
         }
        else                                                                    // Found
         {data = keyData.elementAt(i).data; index = i; found = true;
         }
       }

      public String toString()
       {final StringBuilder s = new StringBuilder();
        s.append("FindEqualInLeaf(Key:"+search+" found:"+found);
        if (found) s.append(" data:"+data+" index:"+index);
        s.append(")\n");
        return s.toString();
       }
     }

    class FindFirstGreaterThanOrEqualInLeaf                                     // Find the first key in the leaf that is equal to or greater than the search key
     {final int     search;                                                     // Search key
      final boolean  found;                                                     // Whether the key was found
      final int      first;                                                     // Index of first such key if found
      FindFirstGreaterThanOrEqualInLeaf(int Search)                             // Find the first key in the  leaf that is equal to or greater than the search key
       {assertLeaf();
        search = Search;
        boolean looking = true;
        final int N = leafSize();
        int i;
        for (i = 0; i < N && looking; i++)                                      // Search the key, data pairs int the leaf
         {if (keyData.elementAt(i).key >= search)
           {looking = false; break;
           }
         }
        if (looking)                                                            // Key not found
         {found = false; first = -1;
         }
        else                                                                    // Key found
         {found = true;  first = i;
         }
       }

      public String toString()                                                  // Print results of search
       {final StringBuilder s = new StringBuilder();
        s.append("FindFirstGreaterThanOrEqualInLeaf(Key:"+search+" found:"+found);
        if (found) s.append(" first:"+first);
        s.append(")\n");
        return s.toString();
       }
     }

    class FindEqualInBranch                                                     // Find the first key in the leaf that is equal to the search key
     {final int    search;                                                      // Search key
      final boolean found;                                                      // Whether the key was found
      final int      next;                                                      // Next associated with the key
      final int     index;                                                      // Index of first such key if found

      FindEqualInBranch(int Search)                                             // Find the first key in the branch that is equal to the search key
       {assertBranch();
        search = Search;
        boolean looking = true;
        final int N = branchSize();
        int i;
        for (i = 0; i < N && looking; i++)                                      // Compare with each valid key
         {z();
          if (keyNext.elementAt(i).key == search)
           {looking = false; break;
           }
         }
        if (looking)                                                            // Not found so use top for next
         {next = keyNext.elementAt(N).next; index = -1; found = false;
         }
        else                                                                    // Found
         {next = keyNext.elementAt(i).next; index = i; found = true;
         }
       }

      public String toString()
       {final StringBuilder s = new StringBuilder();
        s.append("FindEqualInBranch(Key:"+search+" found:"+found+" next:"+next);
        if (found) s.append(" index:"+index);
        s.append(")\n");
        return s.toString();
       }
     }

    class FindFirstGreaterThanOrEqualInBranch                                   // Find the first key in the branch that is equal to or greater than the search key
     {final int     search;                                                     // Search key
      final boolean  found;                                                     // Whether the key was found
      final int      first;                                                     // Index of first such key if found
      final int       next;                                                     // The corresponding next field or top if no such key was found

      FindFirstGreaterThanOrEqualInBranch(int Search)                                   // Find the first key in the branch that is equal to or greater than the search key
       {assertBranch();
        search = Search;
        boolean looking = true;
        final int N = branchSize();
        int i;
        for (i = 0; i < N && looking; i++)                                      // Check each key
         {if (keyNext.elementAt(i).key >= search)
           {looking = false; break;
           }
         }
        if (looking)                                                            // Key not found
         {found = false; first = -1; next = keyNext.elementAt(N).next;
         }
        else                                                                    // Key has been found
         {found = true; first = i; next  =  keyNext.elementAt(i).next;
         }
       }

      public String toString()                                                  // Print search results
       {final StringBuilder s = new StringBuilder();
        s.append("FindFirstGreaterThanOrEqualInBranch(Key:"+search+" found:"+found+" next:"+next);
        if (found) s.append(" first:"+first);
        s.append(")\n");
        return s.toString();
       }
     }


    void printLeaf(Stack<StringBuilder>S, int level)                            // Print leaf horizontally
     {assertLeaf();
      padStrings(S, level);
      final StringBuilder s = new StringBuilder();                              // String builder
      final int K = leafSize();
      for  (int i = 0; i < K; i++) s.append(""+keyData.elementAt(i).key+",");
      if (s.length() > 0) s.setLength(s.length()-1);                            // Remove trailing comma if present
      s.append("="+node+" ");
      S.elementAt(level*linesToPrintABranch).append(s.toString());
      padStrings(S, level);
     }

    void printBranch(Stack<StringBuilder>S, int level)                          // Print branch horizontally
     {assertBranch();
      if (level > maxPrintLevels) return;
      padStrings(S, level);
      final int K = branchSize();
      final int L = level * linesToPrintABranch;

      if (K > 0)                                                                // Branch has key, next pairs
       {for  (int i = 0; i < K; i++)
         {final int next = keyNext.elementAt(i).next;                           // Each key, next pair
          if (nodes.elementAt(next).isLeaf)
           {nodes.elementAt(next).printLeaf(S, level+1);
           }
          else
           {if (next == 0)
              {say("Cannot descend through root from index", i,
                   "in branch", node);
               break;
              }
            nodes.elementAt(next).printBranch(S, level+1);
           }

          S.elementAt(L+0).append(""+keyNext.elementAt(i).key);                 // Key
          S.elementAt(L+1).append(""+node+(i > 0 ?  "."+i : ""));               // Branch,key, next pair
          S.elementAt(L+2).append(""+keyNext.elementAt(i).next);
         }
       }
      else                                                                      // Branch is empty so print just the index of the branch
       {S.elementAt(L+0).append(""+node+"Empty");
       }
      final int top = top();                                                    // Top next will always be present
      S.elementAt(L+3).append(top);                                             // Append top next
      if (nodes.elementAt(top).isLeaf)
       {nodes.elementAt(top).printLeaf(S, level+1);
       }
      else
       {if (top == 0)
         {say("Cannot descend through root from top in branch", node);
          return;
         }
        nodes.elementAt(top).printBranch(S, level+1);
       }

      padStrings(S, level);
     }
   }

  class KeyData                                                                 // A key, data pair
   {final int key, data;
    KeyData(int Key, int Data) {key = Key; data = Data;}
    public String toString()
     {return"KeyData(Key:"+key+" data:"+data+")";
     }
   }

  class KeyNext                                                                 // A key, next pair
   {final int key, next;
    KeyNext(int Key, int Next) {key = Key; next = Next;}
    public String toString()
     {return"KeyNext(Key:"+key+" next:"+next+")";
     }
   }

//D1 Print                                                                      // Print a BTree horizontally

  void padStrings(Stack<StringBuilder> S, int level)                            // Pad the strings at each level of the tree so we have a vertical face to continue with - a bit like Marc Brunel's tunneling shield
   {final int N = level * linesToPrintABranch + maxKeysPerLeaf;                 // Number of lines we might want
    for (int i = S.size(); i <= N; ++i) S.push(new StringBuilder());            // Make sure we have a full deck of strings
    int m = 0;                                                                  // Maximum length
    for (StringBuilder s : S) m = m < s.length() ? s.length() : m;              // Find maximum length
    for (StringBuilder s : S)                                                   // Pad each string to maximum length
     {if (s.length() < m) s.append(" ".repeat(m - s.length()));                 // Pad string to maximum length
     }
   }

  String printCollapsed(Stack<StringBuilder> S)                                 // Collapse horizontal representation into a string
   {final StringBuilder t = new StringBuilder();                                // Print the lines of the tree that are not blank
    for  (StringBuilder s : S)
     {final String l = s.toString();
      if (l.isBlank()) continue;
      t.append(l+"|\n");
     }
    return t.toString();
   }

  String print()                                                                // Print a tree horizontally
   {final Stack<StringBuilder> S = new Stack<>();

    if (root.isLeaf)
     {root.printLeaf(S, 0);
     }
    else
     {root.printBranch(S, 0);
     }
    return printCollapsed(S);
   }

//D0 Tests                                                                      // Testing

  static void oldTests()                                                        // Tests thought to be in good shape
   {
   }

  static void newTests()                                                        // Tests being worked on
   {oldTests();
   }

  public static void main(String[] args)                                        // Test if called as a program
   {try                                                                         // Get a traceback in a format clickable in Geany if something goes wrong to speed up debugging.
     {if (github_actions) oldTests(); else newTests();                          // Tests to run
      if (github_actions)                                                       // Coverage analysis
       {coverageAnalysis(sourceFileName(), 12);
       }
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
