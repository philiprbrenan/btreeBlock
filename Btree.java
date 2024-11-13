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
  public String toString() {return print();}                                    // Print the tree

//D1 Components                                                                 // A branch or leaf in the tree

  class Node                                                                    // A branch or leaf in the tree
   {boolean isLeaf;                                                             // A leaf if true
    static int nodeCount = 0;                                                   // Unique number for each node
    final  int node = nodeCount++;

    final Stack<KeyData> keyData = new Stack<>();                               // Key, data pairs when a leaf
    final Stack<KeyNext> keyNext = new Stack<>();                               // Key, next pairs when a leaf. The last entry is top and so this stack must always have at least one element in it when the node is acting as a branch

    void assertLeaf()   {if (!isLeaf) stop("Leaf required");}
    void assertBranch() {if ( isLeaf) stop("Branch required");}

    Node allocLeaf()    {final Node n = new Node(); nodes.push(n); n.isLeaf = true;  return n;}
    Node allocBranch()  {final Node n = new Node(); nodes.push(n); n.isLeaf = false; return n;}

    int leafSize()   {return keyData.size();}
    int branchSize() {return keyNext.size() - 1;}                               // The  top most entry contains only the top

    boolean isFull()
     {return isLeaf ? leafSize() == maxKeysPerLeaf : branchSize() == maxKeysPerBranch;
     }

    boolean hasLeavesForChildren()
     {assertBranch();
      final KeyNext kn = keyNext.lastElement();
      return nodes.elementAt(kn.next).isLeaf;
     }

    int top()
     {assertBranch();
      return keyNext.elementAt(branchSize()).next;
     }

    public String toString()
     {final StringBuilder s = new StringBuilder();
      if (isLeaf)
       {s.append("Leaf(node:"+node+" size:"+leafSize()+")\n");

        for (int i = 0; i < keyData.size(); i++)
         {final KeyData kd = keyData.elementAt(i);
          s.append("  "+(i+1)+" key:"+kd.key+" data:"+kd.data+"\n");
         }
       }
      else
       {s.append("Branch(node:"+node+" size:"+branchSize()+")\n");

        for (int i = 0; i < keyNext.size(); i++)
         {final KeyNext kn = keyNext.elementAt(i);
          s.append("  "+(i+1)+" key:"+kn.key+" next:"+kn.next+"\n");
         }
       }
      return s.toString();
     }

    class FindEqualInLeaf                                                       // Find the first key in the leaf that is equal to the search key
     {final Node     leaf;                                                      // The leafbeing searched
      final int    search;                                                      // Search key
      final boolean found;                                                      // Whether the key was found
      final int      data;                                                      // Data associated with the  key
      final int     index;                                                      // Index of first such key if found

      FindEqualInLeaf(int Search)                                               // Find the first key in the leaf that is equal to the search key
       {assertLeaf();
        leaf = Node.this;
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
        s.append("FindEqualInLeaf(Leaf:"+leaf.node);
        s.append(" Key:"+search+" found:"+found);
        if (found) s.append(" data:"+data+" index:"+index);
        s.append(")\n");
        return s.toString();
       }
     }

    class FindFirstGreaterThanOrEqualInLeaf                                     // Find the first key in the leaf that is equal to or greater than the search key
     {final Node     leaf;                                                      // The leaf being searched
      final int     search;                                                     // Search key
      final boolean  found;                                                     // Whether the key was found
      final int      first;                                                     // Index of first such key if found
      FindFirstGreaterThanOrEqualInLeaf(int Search)                             // Find the first key in the  leaf that is equal to or greater than the search key
       {assertLeaf();
        leaf = Node.this;
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
        s.append("FindFirstGreaterThanOrEqualInLeaf(Leaf:"+leaf.node);
        s.append(" Key:"+search+" found:"+found);
        if (found) s.append(" first:"+first);
        s.append(")\n");
        return s.toString();
       }
     }

    class FindEqualInBranch                                                     // Find the first key in the leaf that is equal to the search key
     {final Node     branch;                                                    // The branch being searched
      final int    search;                                                      // Search key
      final boolean found;                                                      // Whether the key was found
      final int      next;                                                      // Next associated with the key
      final int     index;                                                      // Index of first such key if found

      FindEqualInBranch(int Search)                                             // Find the first key in the branch that is equal to the search key
       {assertBranch();
        branch = Node.this;
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
        s.append("FindEqualInBranch(branch:"+branch.node);
        s.append(" Key:"+search+" found:"+found+" next:"+next);
        if (found) s.append(" index:"+index);
        s.append(")\n");
        return s.toString();
       }
     }

    class FindFirstGreaterThanOrEqualInBranch                                   // Find the first key in the branch that is equal to or greater than the search key
     {final Node     branch;                                                    // The branch being searched
      final int     search;                                                     // Search key
      final boolean  found;                                                     // Whether the key was found
      final int      first;                                                     // Index of first such key if found
      final int       next;                                                     // The corresponding next field or top if no such key was found

      FindFirstGreaterThanOrEqualInBranch(int Search)                           // Find the first key in the branch that is equal to or greater than the search key
       {assertBranch();
        branch = Node.this;
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
        s.append("FindFirstGreaterThanOrEqualInBranch(branch:"+branch.node);
        s.append(" Key:"+search+" found:"+found+" next:"+next);
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

    void splitLeafRoot()                                                        // Split a leaf which happens to be a full root into two half full leaves while transforming the root leaf into a branch
     {assertLeaf();
      if (node != 0) stop("Not root, but", node);
      if (!isFull()) stop("Root is not full, but", leafSize());
      keyNext.clear();
      isLeaf = false;

      final Node l = allocLeaf();
      final Node r = allocLeaf();
      KeyData first = null;
      for (int i = 0; i < splitLeafSize; i++)                                   // Build left leaf
       {first        = keyData.firstElement();
        l.keyData.push(keyData.firstElement());
                       keyData.removeElementAt(0);
       }

      KeyData last = keyData.firstElement();
      keyNext.push(new KeyNext((last.key - first.key) / 2, l.node));            // Insert left leaf into root

      for (int i = 0; i < splitLeafSize; i++)                                   // Build right leaf
       {r.keyData.push(keyData.firstElement());
        keyData.removeElementAt(0);
       }
      keyNext.push(new KeyNext(r.node));                                        // Insert right into root
      keyData.clear();                                                          // A leaf no more
     }

    void splitBranchRoot()                                                      // Split a branch which happens to be a full root into two half full branches while retaining the current branch as the root
     {assertBranch();
      if (node != 0) stop("Not root, but", node);
      if (!isFull()) stop("Root is not full, but", branchSize());

      final Node l = allocBranch();
      final Node r = allocBranch();

      for (int i = 0; i < splitBranchSize; i++)
       {l.keyNext.push(keyNext.firstElement());
                       keyNext.removeElementAt(0);
       }

      final KeyNext split = keyNext.firstElement();
      keyNext.removeElementAt(0);

      for(int i = 0; i < splitBranchSize; i++)
       {r.keyNext.push(keyNext.firstElement());
                       keyNext.removeElementAt(0);
       }
        keyNext.push(new KeyNext(split.key, l.node));
      r.keyNext.push(new KeyNext(top()));
        keyNext.push(new KeyNext(r.node));
      l.keyNext.push(new KeyNext(split.next));
     }

    void splitLeaf(Node parent, int index)                                      // Split a leaf which is not the root
     {assertLeaf();
      if (node == 0) stop("Cannot split root with this method");
      if (!isFull()) stop("Leaf:", node, "is not full, but has:", leafSize(), this);
      if (parent.isFull()) stop("Parent:", parent, "must not be full");
      if (index < 0)           stop("Index", index, "too small");
      if (index > leafSize()) stop("Index", index, "too big for leaf with:", leafSize());

      final Node p = parent;
      final Node l = allocLeaf();
      final Node r = this;

      for (int i = 0; i < splitLeafSize; i++)                                   // Build left leaf
       {l.keyData.push(r.keyData.firstElement());
                       r.keyData.removeElementAt(0);
       }

      final int splitKey = (r.keyNext.firstElement().key - l.keyNext.lastElement().key) / 2;

      parent.keyNext.insertElementAt(new KeyNext(splitKey, l.node), index);
     }

    void splitBranch(Node parent, int index)                                    // Split a branch which is not the root
     {assertBranch();
      if (node == 0) stop("Cannot split root with this method");
      if (!isFull()) stop("Branch is not full, but", branchSize());
      if (parent.isFull()) stop("Parent must not be full");
      if (index < 0)            stop("Index", index, "too small");
      if (index > branchSize()) stop("Index", index, "too big for branch with:", branchSize());

      final Node p = parent;
      final Node l = allocBranch();
      final Node r = this;

      for (int i = 0; i < splitBranchSize; i++)                                 // Build left branch
       {l.keyNext.push(r.keyNext.firstElement());
                       r.keyNext.removeElementAt(0);
       }

      final int splitKey = r.keyNext.firstElement().key;
      r.keyNext.removeElementAt(0);

      parent.keyNext.insertElementAt(new KeyNext(splitKey, l.node), index);
     }

    boolean mergeRoot()                                                         // Merge into the root
     {assertBranch();
      if (branchSize() != 2) stop("Root must have just two children, not:", branchSize());

      final Node p = this;
      final Node l = nodes.elementAt(keyNext.firstElement().next);
      final Node r = nodes.elementAt(keyNext. lastElement().next);

      if (p.hasLeavesForChildren())
       {if (l.leafSize() + r.leafSize() <= maxKeysPerLeaf)
         {p.keyData.clear();
          for (; l.leafSize() > 0;)
           {p.keyData.push(l.keyData.pop());
           }
          for (; r.leafSize() > 0;)
           {p.keyData.push(r.keyData.pop());
           }
           p.keyNext.clear();
          return true;
         }
       }
      else if (l.branchSize() + 1 + r.branchSize() <= maxKeysPerBranch)
       {final KeyNext pkn = p.keyNext.firstElement();
        p.keyNext.clear();
        for (; l.branchSize() > 0;)
         {p.keyNext.push(l.keyNext.pop());
         }
        p.keyNext.push(new KeyNext(pkn.key, l.keyNext.lastElement().next));
        for (; r.branchSize() > 0;)
         {p.keyNext.push(r.keyNext.pop());
         }
        p.keyNext.push(new KeyNext(r.keyNext.lastElement().next));
        return true;
       }
      return false;
     }

    boolean merge(int index)                                                    // Merge the indexed child with its left sibling
     {assertBranch();
      if (index == 0) return false;
      if (index < 0)            stop("Index", index, "too small");
      if (index > branchSize()) stop("Index", index, "too big");
      if (isFull()) stop("Parent must not be full");

      final Node p = this;
      final Node l = nodes.elementAt(p.keyNext.elementAt(index-1).next);
      final Node r = nodes.elementAt(p.keyNext.elementAt(index-0).next);

      if (p.hasLeavesForChildren())
       {if (l.leafSize() + r.leafSize() <= maxKeysPerLeaf)
         {final int N = l.leafSize();
          for (; l.leafSize() > 0;)
           {r.keyData.insertElementAt(l.keyData.pop(), 0);
           }
          p.keyNext.removeElementAt(index);
          return true;
         }
       }
      else if (l.branchSize() + 1 + r.branchSize() <= maxKeysPerBranch)
       {final KeyNext pkn = p.keyNext.elementAt(index-1);
        r.keyNext.insertElementAt(new KeyNext(pkn.key), l.top());
        final int N = l.leafSize();
        for (; l.leafSize() > 0;)
         {r.keyData.insertElementAt(l.keyData.pop(), 0);
         }
        p.keyNext.removeElementAt(index);
        return true;
       }
      return false;
     }
   }  // Node

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
    KeyNext(         int Next) {key = -1;  next = Next;}
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

//D1 Find                                                                       // Find the data associated with a key

  class Find                                                                    // Find the data associated with a key in the tree
   {Node.FindEqualInLeaf search;                                                // Details of the search of the containing leaf

    Find(int Key)
     {if (root.isLeaf)                                                          // The root is a leaf
       {search = root.new FindEqualInLeaf(Key);
        return;
       }

      Node parent = root;                                                       // Parent starts at root which is known to be a branch

      for (int i = 0; i < maxDepth; i++)                                        // Step down through tree
       {z();
        final Node.FindFirstGreaterThanOrEqualInBranch down =                   // Find next child in search path of key
          parent.new FindFirstGreaterThanOrEqualInBranch(Key);
        final int n = down.next;
        if (nodes.elementAt(n).isLeaf)                                          // Found the containing search
         {final Node l = nodes.elementAt(n);
          search  = l.new FindEqualInLeaf(Key);
          return;
         }
        parent = nodes.elementAt(n);                                            // Step down to lower branch
       }
      stop("Search did not terminate in a search");
     }

    boolean  found() {return search.found;}
    int      index() {return search.index;}
    int       data() {return search.data;}

    public String toString()                                                    // Print find result
     {final StringBuilder s = new StringBuilder();
      s.append("Find(search:"+search.search);
      s.append( " search:"+search.index);
      if (search.found)
       {s.append( " data:"+data());
        s.append(" index:"+index());
       }
      s.append(")\n");
      return s.toString();
     }
   }

  Find find(int Key) {return find(Key);}                                        // Find a key in the tree

  class FindAndInsert extends Find                                              // Insert the specified key and data into the tree if there is room in the target leaf,or update the key with the data if the key already exists
   {int          key;                                                           // Key to insert
    int         data;                                                           // Data being inserted or updated
    boolean  success;                                                           // Inserted or updated if true
    boolean inserted;                                                           // Inserted if true

    FindAndInsert(int Key, int Data)                                            // Find the leaf that should contain this key and insert or update it is possible
     {super(Key);                                                               // Find the leaf that should contain this key
      key = Key; data = Data;

      if (search.found)                                                         // Found the key in the leaf so update it with the new data
       {search.leaf.keyData.setElementAt(new KeyData(Key, Data), search.index);
        success = true; inserted = false;
        return;
       }

      if (!search.leaf.isFull())                                                       // Leaf is not full so we can insert immediately
       {z();
        final Node.FindFirstGreaterThanOrEqualInLeaf fge =
          search.leaf.new FindFirstGreaterThanOrEqualInLeaf(Key);
        if (fge.found)                                                          // Found a matching key so insert into body of leaf
         {search.leaf.keyData.setElementAt(new KeyData(Key, Data), fge.first);
         }
        else                                                                    // No matching key so put at end
         {search.leaf.keyData.push(new KeyData(Key, Data));
         }
        success = true;
        return;
       }
      success = false;
     }

    public String toString()                                                    // Print find and insert
     {final StringBuilder s = new StringBuilder();
      s.append("FindAndInsert(key:"+key);
      s.append(" data:"+data);
      s.append(" success:"+success);
      if (success) s.append(" inserted:"+inserted);
      s.append(")\n" );
      return s.toString();
     }
   }

  FindAndInsert findAndInsert(int Key, int Data)                                // Find a key and insert it if possible with its associated data
   {return new FindAndInsert(Key, Data);
   }

//D1 Insertion                                                                  // Insert a key, data pair into the tree or update and existing key with a new datum

  void put(int Key, int Data)                                                   // Insert a key, data pair into the tree or update and existing key with a new datum
   {z();

    final FindAndInsert f = new FindAndInsert(Key, Data);                       // Try direct insertion with no modifications to the shape of the tree
    if (f.success) return;                                                      // Inserted or updated successfully

    if (root.isFull())                                                          // Start the insertion at the root, after splitting it if necessary
     {if (root.isLeaf) root.splitLeafRoot();
      else root.splitBranchRoot();
      final FindAndInsert F = new FindAndInsert(Key, Data);                       // Spliting the root might have been enough
      if (F.success) return;                                                      // Inserted or updated successfully
     }

    Node p = root;

    for (int i = 0; i < maxDepth; i++)                                          // Step down from branch to branch through the tree until reaching a leaf repacking as we go
     {final Node.FindFirstGreaterThanOrEqualInBranch                            // Step down
      down = p.new FindFirstGreaterThanOrEqualInBranch(Key);
      Node q = nodes.elementAt(down.next);
      if (q.isLeaf)                                                             // Reached a leaf
       {q.splitLeaf(p, down.first);
        findAndInsert(Key, Data);
        return;
       }

      q.splitBranch(p, down.next);                                              // Split the child branch in the search path for the key from the parent so the the search path does not contain a full branch above the containing leaf
      final Node.FindFirstGreaterThanOrEqualInBranch                            // Step down again as the repack will have altered the local layout
      Down = p.new FindFirstGreaterThanOrEqualInBranch(Key);
      p = nodes.elementAt(Down.next);
     }
    stop("Fallen off the end of the tree");                                     // The tree must be missing a leaf
   }

  void put(int Key)                                                             // Put some test data into the tree
   {put(Key, Key);
   }

//D0 Tests                                                                      // Testing

  static void test_put_ascending()
   {final Btree t = new Btree(4, 3);
    final int N = 16;
    for (int i = 1; i <= N; i++)
     {say(t);
      t.put(i);
      say(t);
     }
    //stop(t);
    t.ok("""
                                                                                                                            32                                                                                                                                           |
                                                                                                                            0                                                                                                                                            |
                                                                                                                            19                                                                                                                                           |
                                                                                                                            20                                                                                                                                           |
                                                      16                                                                                                                                            48                                                                   |
                                                      19                                                                                                                                            20                                                                   |
                                                      9                                                                                                                                             21                                                                   |
                                                      14                                                                                                                                            6                                                                    |
          4          8               12                               20               24                28                                  36               40                 44                                  52               56                60               |
          9          9.1             9.2                              14               14.1              14.2                                21               21.1               21.2                                6                6.1               6.2              |
          3          1               4                                8                10                5                                   13               15                 11                                  18               22                16               |
                                     7                                                                   12                                                                      17                                                                     2                |
1,2,3,4=3  5,6,7,8=1    9,10,11,12=4    13,14,15,16=7   17,18,19,20=8   21,22,23,24=10     25,26,27,28=5     29,30,31,32=12   33,34,35,36=13   37,38,39,40=15     41,42,43,44=11     45,46,47,48=17   49,50,51,52=18   53,54,55,56=22    57,58,59,60=16    61,62,63,64=2 |
""");
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_put_ascending();
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
