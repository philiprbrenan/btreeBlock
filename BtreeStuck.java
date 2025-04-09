//------------------------------------------------------------------------------
// Btree using Stuck instead of Stack
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout a btree in a block on the surface of a silicon chip.

import java.util.*;

class BtreeStuck extends Test                                                   // Manipulate a btree
 {final int
    maxKeysPerLeaf,                                                             // Maximum number of leafs in a key
    maxKeysPerBranch,                                                           // Maximum number of keys in a branch
    splitLeafSize,                                                              // The number of key, data pairs to split out of a leaf
    splitBranchSize;                                                            // The number of key, next pairs to split out of a branch

  final Node         root;                                                      // The root of the tree
  final Stack<Node>  nodes = new Stack<>();                                     // The leaves and branches comprising the tree

  final static int
   linesToPrintABranch =  4,                                                    // The number of lines required to print a branch
        maxPrintLevels = 10,                                                    // Maximum number of levels to print in a tree
              maxDepth = 99;                                                    // Maximum depth of any realistic tree

  int        nodeCount = 0;                                                     // Unique number for each node
  int      maxNodeUsed = 0;                                                     // Maximum branch or leaf index used
  static boolean debug = false;                                                 // Debugging enabled

//D1 Construction                                                               // Create a BTree from nodes which can be branches or leaves.  The data associated with the BTree is stored only in the leaves opposite the keys

  BtreeStuck(int MaxKeysPerLeaf, int MaxKeysPerBranch)                          // Define a BTree with the specified dimensions
   {zz();
    maxKeysPerLeaf   = MaxKeysPerLeaf;
    maxKeysPerBranch = MaxKeysPerBranch;
    splitLeafSize    = maxKeysPerLeaf   >> 1;
    splitBranchSize  = maxKeysPerBranch >> 1;
    nodes.push(root  = new Node());                                             // The root
    root.isLeaf      = true;                                                    // The root starts as a leaf
   }

//D1 Control                                                                    // Testing, control and integrity

  void ok(String expected) {Test.ok(toString(), expected);}                     // Confirm tree is as expected
  void stop()              {Test.stop(toString());}                             // Stop after printing the tree
  public String toString() {return print();}                                    // Print the tree

//D1 Components                                                                 // A branch or leaf in the tree

  class Node                                                                    // A branch or leaf in the tree
   {boolean isLeaf;                                                             // A leaf if true
    final  int node = nodeCount++;

    class KeyStuck  extends Stuck {int maxSize() {return maxKeysPerLeaf;}};     // Key, data pairs when a leaf
    class NextStuck extends Stuck {int maxSize() {return maxKeysPerBranch+1;}}; // Key, next pairs when a branch. The last entry is top and so this stack must always have at least one element in it when the node is acting as a branch

    final Stuck keyData = new KeyStuck();                                       // Key, data pairs when a leaf
    final Stuck keyNext = new NextStuck();                                      // Key, next pairs when a branch. The last entry is top and so this stack must always have at least one element in it when the node is acting as a branch

    void assertLeaf()   {if (!isLeaf) stop("Leaf required");}
    void assertBranch() {if ( isLeaf) stop("Branch required");}

    Node allocLeaf()    {z();
    final Node n = new Node(); nodes.push(n); n.isLeaf = true;  return n;}
    Node allocBranch()  {z();
    final Node n = new Node(); nodes.push(n); n.isLeaf = false; return n;}

    int leafSize()   {z(); return keyData.size();}                              // Number of children in body of leaf
    int branchSize() {z(); return keyNext.size() - 1;}                          // Number of children in body of branch

    boolean isFull()                                                            // The node is full
     {z(); return isLeaf ? leafSize() == maxKeysPerLeaf : branchSize() == maxKeysPerBranch;
     }

    boolean isLow()                                                             // The node is low on children making it impossible to merge two sibling children
     {z(); return (isLeaf ? leafSize() : branchSize()) < 2;
     }

    boolean hasLeavesForChildren()                                              // The node has leaves for children
     {z(); assertBranch();
      final int last = keyNext.lastElement().data;
      return nodes.elementAt(last).isLeaf;
     }

    int top()                                                                   // The top next element of a branch
     {z(); assertBranch();
      return keyNext.elementAt(branchSize()).data;
     }

    public String toString()                                                    // Print a node
     {final StringBuilder s = new StringBuilder();
      if (isLeaf)                                                               // Print a leaf
       {s.append("Leaf(node:"+node+" size:"+leafSize()+")\n");

        for (int i = 0; i < keyData.size(); i++)
         {final Stuck.ElementAt kd = keyData.elementAt(i);
          s.append("  "+(i+1)+" key:"+kd.key+" data:"+kd.data+"\n");
         }
       }
      else                                                                      // Print a branch
       {s.append("Branch(node:"+node+
                 " size:"+branchSize()+
                 " top:"+top()+"\n");

        for (int i = 0; i < keyNext.size(); i++)
         {final Stuck.ElementAt kn = keyNext.elementAt(i);
          s.append("  "+(i+1)+" key:"+kn.key+" next:"+kn.data+"\n");
         }
       }
      return s.toString();
     }

//D2 Search                                                                     // Search within a node

    class FindEqualInLeaf                                                       // Find the first key in the leaf that is equal to the search key
     {final Node     leaf;                                                      // The leaf being searched
      final int    search;                                                      // Search key
      final boolean found;                                                      // Whether the key was found
      final int      data;                                                      // Data associated with the  key
      final int     index;                                                      // Index of first such key if found

      FindEqualInLeaf(int Search)                                               // Find the first key in the leaf that is equal to the search key
       {z(); assertLeaf();
        leaf   = Node.this;
        search = Search;

        final Stuck.Search s = keyData.new Search(Search);
        found  = s.found;
        index  = s.index;
        data   = s.data;
       }

      public String toString()                                                  // Print details of find equal in leaf
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
      final int    search;                                                      // Search key
      final boolean found;                                                      // Whether the key was found
      final int     first;                                                      // Index of first such key if found

      FindFirstGreaterThanOrEqualInLeaf(int Search)                             // Find the first key in the  leaf that is equal to or greater than the search key
       {z(); assertLeaf();
        leaf   = Node.this;
        search = Search;

        final Stuck.SearchFirstGreaterThanOrEqual s =
        keyData.new SearchFirstGreaterThanOrEqual(Search);
        found = s.found;
        first = s.index;
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

    class FindFirstGreaterThanOrEqualInBranch                                   // Find the first key in the branch that is equal to or greater than the search key
     {final Node     branch;                                                    // The branch being searched
      final int     search;                                                     // Search key
      final boolean  found;                                                     // Whether the key was found
      final int      first;                                                     // Index of first such key if found
      final int       next;                                                     // The corresponding next field or top if no such key was found

      FindFirstGreaterThanOrEqualInBranch(int Search)                           // Find the first key in the branch that is equal to or greater than the search key
       {z(); assertBranch();
        branch = Node.this;
        search = Search;
        final Stuck.SearchFirstGreaterThanOrEqualExceptLast s =
            keyNext.searchFirstGreaterThanOrEqualExceptLast(Search);
        found = s.found;
        first = s.index;
        next  = s.found ? s.data : keyNext.lastElement().data;                  // Next if no key matches
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

//D2 Array                                                                      // Represent the contents of the tree as an array

    void leafToArray(Stack<KeyData>s)                                           // Leaf as an array
     {z(); assertLeaf();
      final int K = leafSize();
      for  (int i = 0; i < K; i++)
       {z();
        final Stuck.ElementAt e = keyData.elementAt(i);
        s.push(new KeyData(e.key, e.data));
       }
     }

    void branchToArray(Stack<KeyData>s)                                         // Branch to array
     {z(); assertBranch();
      final int K = branchSize() + 1;                                           // To allow for top next

      if (K > 0)                                                                // Branch has key, next pairs
       {z();
        for  (int i = 0; i < K; i++)
         {z();
          final int next = keyNext.elementAt(i).data;                           // Each key, next pair
          if (nodes.elementAt(next).isLeaf)
           {z(); nodes.elementAt(next).leafToArray(s);
           }
          else
           {z();
            if (next == 0)
             {say("Cannot descend through root from index", i,
                  "in branch", node);
              break;
             }
            z(); nodes.elementAt(next).branchToArray(s);
           }
         }
       }
     }

//D2 Print                                                                      // Print the contents of the tree

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
     { assertBranch();
      if (level > maxPrintLevels) return;
      padStrings(S, level);
      final int K = branchSize();
      final int L = level * linesToPrintABranch;

      if (K > 0)                                                                // Branch has key, next pairs
       {for  (int i = 0; i < K; i++)
         {final int next = keyNext.elementAt(i).data;                           // Each key, next pair
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
          S.elementAt(L+2).append(""+keyNext.elementAt(i).data);
         }
       }
      else                                                                      // Branch is empty so print just the index of the branch
       {S.elementAt(L+0).append(""+node+"Empty");
       }
      final int top = top();                                                    // Top next will always be present
      S.elementAt(L+3).append(top);                                             // Append top next
      if (nodes.elementAt(top).isLeaf)                                          // Print leaf
       { nodes.elementAt(top).printLeaf(S, level+1);
       }
      else                                                                      // Print branch
       {if (top == 0)
         {say("Cannot descend through root from top in branch", node);
          return;
         }
        nodes.elementAt(top).printBranch(S, level+1);
       }

      padStrings(S, level);
     }

//D2 Split                                                                      // Split nodes in half to increase the number of nodes in the tree

    void splitLeafRoot()                                                        // Split a leaf which happens to be a full root into two half full leaves while transforming the root leaf into a branch
     {z(); assertLeaf();
      if (node != 0) stop("Wanted root, but got node:", node);
      if (!isFull()) stop("Root is not full, but has size:", leafSize());
      keyNext.clear();
      isLeaf = false;

      final Node l  = allocLeaf();                                              // New left leaf
      final Node r  = allocLeaf();                                              // New right leaf

      for (int i = 0; i < splitLeafSize; i++)                                   // Build left leaf
       {z();
        final Stuck.Shift f = keyData.shift();
        l.keyData.push(f.key, f.data);
       }
      for (int i = 0; i < splitLeafSize; i++)                                   // Build right leaf
       {z();
        final Stuck.Shift f = keyData.shift();
        r.keyData.push(f.key, f.data);
       }

      final int first = r.keyData.firstElement().key;                           // First of right leaf
      final int last  = l.keyData. lastElement().key;                           // Last of left leaf
      final int kv = (last + first) / 2;                                        // Insert left leaf into root
      keyNext.push(kv, l.node);                                                 // Insert left leaf into root
      keyNext.push(0,  r.node);                                                 // Insert right into root. This will be the top node and so ignored by search ... except last.
      keyData.clear();                                                          // A leaf no more
     }

    void splitBranchRoot()                                                      // Split a branch which happens to be a full root into two half full branches while retaining the current branch as the root
     {z(); assertBranch();
      if (node != 0) stop("Not root, but node:", node);
      if (!isFull()) stop("Root is not full, but has size:", branchSize());

      final Node l = allocBranch();                                             // New left branch
      final Node r = allocBranch();                                             // New right branch

      for (int i = 0; i < splitBranchSize; i++)                                 // Left
       {z();
        final Stuck.Shift f = keyNext.shift();
        l.keyNext.push(f.key, f.data);
       }
      final Stuck.Shift pl = keyNext.shift();                                   // This key, next pair will be part of the root
      l.keyNext.push(0, pl.data);                                               // Becomes top and so ignored by search ... except last

      for(int i = 0; i < splitBranchSize; i++)                                  // Right
       {z();
        final Stuck.Shift f = keyNext.shift();
        r.keyNext.push(f.key, f.data);
       }

      final Stuck.Shift pr = keyNext.shift();                                   // Top of root
      r.keyNext.push(0, pr.data);                                               // Becomes top and so ignored by search ... except last
      keyData.clear(); keyNext.clear();

      keyNext.push(pl.key, l.node);
      keyNext.push(0,      r.node);                                             // Becomes top and so ignored by search ... except last
     }

    void splitLeaf(Node parent, int index)                                      // Split a leaf which is not the root
     {z(); assertLeaf();
      if (node == 0)          stop("Cannot split root with this method");
      if (!isFull())          stop("Leaf:", node, "is not full, but has:", leafSize(), this);
      if (parent.isFull())    stop("Parent:", parent, "must not be full");
      if (index < 0)          stop("Index", index, "too small");
      if (index > leafSize()) stop("Index", index, "too big for leaf with:", leafSize());

      final Node p = parent;                                                    // Parent
      final Node l = allocLeaf();                                               // New  split out leaf
      final Node r = this;                                                      // Existing  leaf

      for (int i = 0; i < splitLeafSize; i++)                                   // Build left leaf
       {z();
        final Stuck.Shift f = r.keyData.shift();
        l.keyData.push(f.key, f.data);
       }
      final int F = r.keyData.firstElement().key;
      final int L = l.keyData.lastElement().key;
      final int splitKey = (F + L) / 2;

      p.keyNext.insertElementAt(splitKey, l.node, index);                       // Insert new key, next pair in parent
     }

    void splitBranch(Node parent, int index)                                    // Split a branch which is not the root by splitting right to left
     {z(); assertBranch();
      if (node == 0)            stop("Cannot split root with this method");
      if (!isFull())            stop("Branch:", node, "is not full, but", branchSize());
      if (parent.isFull())      stop("Parent:", parent.node, "must not be full");
      if (index < 0)            stop("Index", index, "too small in node:", node);
      if (index > branchSize()) stop("Index", index, "too big for branch with:", branchSize(), "in node:", node);

      final Node p = parent;
      final Node l = allocBranch();
      final Node r = this;

      for (int i = 0; i < splitBranchSize; i++)                                 // Build left branch
       {z();
        final Stuck.Shift f = r.keyNext.shift();
        l.keyNext.push(f.key, f.data);
       }

      final Stuck.Shift split = r.keyNext.shift();                              // Build right branch
      l     .keyNext.push(0, split.data);                                       // Becomes top and so is iognored by search ... except last
      parent.keyNext.insertElementAt(split.key, l.node, index);
     }

//D2 Steal                                                                      // Steal children from left or sibling to balance tree

    boolean stealFromLeft(int index)                                            // Steal from the left sibling of the indicated child if possible to give to the right - Dennis Moore, Dennis Moore, Dennis Moore.
     {z(); assertBranch();
      if (index == 0) return false;
      if (index < 0)            stop("Index", index, "too small");
      if (index > branchSize()) stop("Index", index, "too big");

      final Stuck.ElementAt  L = keyNext.elementAt(index-1);
      final Stuck.ElementAt  R = keyNext.elementAt(index+0);
      final Stuck            p = keyNext;

      if (hasLeavesForChildren())                                               // Children are leaves
       {z();
        final Stuck l = nodes.elementAt(L.data).keyData;
        final Stuck r = nodes.elementAt(R.data).keyData;
        final int  nl = nodes.elementAt(L.data).leafSize();
        final int  nr = nodes.elementAt(R.data).leafSize();

        if (nr >= maxKeysPerLeaf) return false;                                 // Steal not possible because there is no where to put the steal
        if (nl <= 1) return false;                                              // Steal not allowed because it would leave the leaf sibling empty

        final Stuck.LastElement ll = l.lastElement();
        r.insertElementAt(ll.key, ll.data, 0);                                  // Increase right
        l.pop();                                                                // Reduce left
        p.setElementAt   (l.elementAt(nl-2).key, L.data, index-1);              // Swap key of parent
       }
      else                                                                      // Children are branches
       {z();
        final Stuck l = nodes.elementAt(L.data).keyNext;
        final Stuck r = nodes.elementAt(R.data).keyNext;
        final int  nl = nodes.elementAt(L.data).branchSize();
        final int  nr = nodes.elementAt(R.data).branchSize();

        if (nr >= maxKeysPerBranch) return false;                               // Steal not possible because there is no where to put the steal
        if (nl <= 1) return false;                                              // Steal not allowed because it would leave the left sibling empty

        final Stuck.LastElement  t = l.lastElement();                           // Increase right with left top
        r.insertElementAt(p.elementAt(index).key, t.data, 0);                   // Increase right with left top
        l.pop();                                                                // Remove left top
        final Stuck.FirstElement b = r.firstElement();                          // Increase right with left top
        r.setElementAt(p.elementAt(index-1).key, b.data, 0);                    // Reduce key of parent of left
        p.setElementAt(l.lastElement().key,      L.data, index-1);              // Reduce key of parent of left
       }
      return true;
     }

    boolean stealFromRight(int index)                                           // Steal from the right sibling of the indicated child if possible
     {z(); assertBranch();
      if (index == branchSize()) return false;
      if (index < 0)             stop("Index", index, "too small");
      if (index >= branchSize()) stop("Index", index, "too big");

      final Stuck.ElementAt L = keyNext.elementAt(index+0);
      final Stuck.ElementAt R = keyNext.elementAt(index+1);
      final Stuck           p = keyNext;
      if (hasLeavesForChildren())                                               // Children are leaves
       {z();
        final Stuck          l = nodes.elementAt(L.data).keyData;
        final Stuck          r = nodes.elementAt(R.data).keyData;
        final int           nl = nodes.elementAt(L.data).leafSize();
        final int           nr = nodes.elementAt(R.data).leafSize();

        if (nl >= maxKeysPerLeaf) return false;                                 // Steal not possible because there is no where to put the steal
        if (nr <= 1) return false;                                              // Steal not allowed because it would leave the right sibling empty

        final int k = r.firstElement().key;
        l.push        (k, r.firstElement().data);                               // Increase left
        p.setElementAt(k, L.data, index);                                       // Swap key of parent
        r.removeElementAt(0);                                                   // Reduce right
       }
      else                                                                      // Children are branches
       {z();
        final Stuck l = nodes.elementAt(L.data).keyNext;
        final Stuck r = nodes.elementAt(R.data).keyNext;
        final int  nl = nodes.elementAt(L.data).branchSize();
        final int  nr = nodes.elementAt(R.data).branchSize();

        if (nl >= maxKeysPerBranch) return false;                               // Steal not possible because there is no where to put the steal
        if (nr <= 1) return false;                                              // Steal not allowed because it would leave the right sibling empty

        l.setElementAt(L.key, l.lastElement().data, nl);                        // Left top becomes real
        l.push        (0, r.firstElement().data);                               // New top for left is ignored by search ,.. except last
        p.setElementAt(r.firstElement().key, L.data, index);                    // Swap key of parent
        r.removeElementAt(0);                                                   // Reduce right
       }
      return true;
     }

//D2 Merge                                                                      // Merge  two nodes together and free the resulting free node

    boolean mergeRoot()                                                         // Merge into the root
     {z();
      if (root.isLeaf || branchSize() > 1) return false;
      if (node != 0) stop("Expected root, got:", node);
      final Node p = this;
      final Node l = nodes.elementAt(keyNext.firstElement().data);
      final Node r = nodes.elementAt(keyNext. lastElement().data);

      if (p.hasLeavesForChildren())                                             // Leaves
       {z();
        if (l.leafSize() + r.leafSize() <= maxKeysPerLeaf)
         {z(); p.keyData.clear();
          final int nl = l.leafSize();
          for (int i = 0; i < nl; ++i)
           {z();
            final Stuck.Shift f = l.keyData.shift();
            p.keyData.push(f.key, f.data);
           }
          final int nr = r.leafSize();
          for (int i = 0; i < nr; ++i)
           {z();
            final Stuck.Shift f = r.keyData.shift();
            p.keyData.push(f.key, f.data);
           }
          p.keyNext.clear();
          isLeaf = true;
          return true;
         }
       }
      else if (l.branchSize() + 1 + r.branchSize() <= maxKeysPerBranch)         // Branches
       {z();
        final Stuck.FirstElement pkn = p.keyNext.firstElement();
        p.keyNext.clear();
        final int nl = l.branchSize();
        for (int i = 0; i < nl; ++i)
         {z();
          final Stuck.Shift f = l.keyNext.shift();
          p.keyNext.push(f.key, f.data);

         }
        p.keyNext.push(pkn.key, l.keyNext.lastElement().data);
        final int nr = r.branchSize();
        for (int i = 0; i < nr; ++i)
         {z();
          final Stuck.Shift f = r.keyNext.shift();
          p.keyNext.push(f.key, f.data);
         }
        p.keyNext.push(0, r.keyNext.lastElement().data);                        // Top so ignored by search ... except last
        return true;
       }
      return false;
     }

    boolean mergeLeftSibling(int index)                                         // Merge the left sibling
     {z(); assertBranch();
      if (index == 0) return false;
      if (index < 0)            stop("Index", index, "too small for branch of size:", branchSize());
      if (index > branchSize()) stop("Index", index, "too big for branch of size:", branchSize());
      //if (branchSize() < 2)     stop("Node:", this,  "must have two or more children");
      if (branchSize() < 2) return false;

      final Stuck.ElementAt L = keyNext.elementAt(index-1);
      final Stuck.ElementAt R = keyNext.elementAt(index-0);
      final Stuck           p = keyNext;

      if (hasLeavesForChildren())                                               // Children are leaves
       {z();
        final Stuck l = nodes.elementAt(L.data).keyData;
        final Stuck r = nodes.elementAt(R.data).keyData;
        final int  nl = nodes.elementAt(L.data).leafSize();
        final int  nr = nodes.elementAt(R.data).leafSize();

        if (nl + nr >= maxKeysPerLeaf) return false;                            // Combined body would be too big

        for (int i = 0; i < maxKeysPerLeaf && l.size() > 0; i++)                // Transfer left to right
         {z();
          final Stuck.Pop q = l.pop();
          r.insertElementAt(q.key, q.data, 0);
         }
       }
      else                                                                      // Children are branches
       {z();
        final Stuck l = nodes.elementAt(L.data).keyNext;
        final Stuck r = nodes.elementAt(R.data).keyNext;
        final int  nl = nodes.elementAt(L.data).branchSize();
        final int  nr = nodes.elementAt(R.data).branchSize();

        if (nl + 1 + nr > maxKeysPerBranch) return false;                       // Merge not possible because there is not enough room for the combined result
        final int t = p.elementAt(index-1).key;                                 // Top key
        r.insertElementAt(t, l.lastElement().data, 0);                          // Left top to right

        l.pop();                                                                // Remove left top
        for (int i = 0; i < maxKeysPerBranch && l.size() > 0; i++)              // Transfer left to right
         {z();
          final Stuck.Pop q = l.pop();
          r.insertElementAt(q.key, q.data, 0);
         }
       }
        p.removeElementAt(index-1);                                             // Reduce parent on left
      return true;
     }

    boolean mergeRightSibling(int index)                                        // Merge the right sibling
     {z(); assertBranch();
      if (index >= branchSize()) return false;
      if (index < 0)            stop("Index", index, "too small");
      if (index > branchSize()) stop("Index", index, "too big");
      //if (branchSize() < 2)     stop("Node:", this,  "must have two or more children");
      if (branchSize() < 2) return false;

      final Stuck.ElementAt L = keyNext.elementAt(index+0);
      final Stuck.ElementAt R = keyNext.elementAt(index+1);
      final Stuck           p = keyNext;

      if (hasLeavesForChildren())                                               // Children are leaves
       {z();
        final Stuck l = nodes.elementAt(L.data).keyData;
        final Stuck r = nodes.elementAt(R.data).keyData;
        final int  nl = nodes.elementAt(L.data).leafSize();
        final int  nr = nodes.elementAt(R.data).leafSize();

        if (nl + nr > maxKeysPerLeaf) return false;                             // Combined body would be too big
        for (int i = 0; i < maxKeysPerLeaf && r.size() > 0; i++)                // Transfer right to left
         {z();
          final Stuck.Shift q = r.shift();
          l.push(q.key, q.data);
         }
       }
      else                                                                      // Children are branches
       {z();
        final Stuck l = nodes.elementAt(L.data).keyNext;
        final Stuck r = nodes.elementAt(R.data).keyNext;
        final int  nl = nodes.elementAt(L.data).branchSize();
        final int  nr = nodes.elementAt(R.data).branchSize();

        if (nl + 1 + nr > maxKeysPerBranch) return false;                       // Merge not possible because there is no where to put the steal
        final Stuck.LastElement ll = l.lastElement();
        l.setElementAt(p.elementAt(index).key, ll.data, nl);                    // Re-key left top

        for (int i = 0; i < maxKeysPerBranch && r.size() > 0; i++)              // Transfer right to left
         {z();
          final Stuck.Shift f = r.shift();
          l.push(f.key, f.data);
         }
       }

      final Stuck.ElementAt pkn = p.elementAt(index+1);                         // Key of right sibling
      p.setElementAt(pkn.key, p.elementAt(index).data, index);                  // Install key of right sibling in this child
      p.removeElementAt(index+1);                                               // Reduce parent on right
      return true;
     }

//D2 Balance                                                                    // Balance the tree by merging and stealing

    void balance(int index)                                                     // Augment the indexed child so it has at least two children in its body
     {z(); assertBranch();
      if (index < 0)            stop("Index", index, "too small");
      if (index > branchSize()) stop("Index", index, "too big");
      if (isLow() && node != root.node) stop("Parent:", node, "must not be low on children");

      final Stuck.ElementAt p = nodes.elementAt(node).keyNext.elementAt(index);
      final Node    c = nodes.elementAt(p.data);
      if (!c.isLow())               return;
      if (stealFromLeft    (index)) return;
      if (stealFromRight   (index)) return;
      if (mergeLeftSibling (index)) return;
      if (mergeRightSibling(index)) return;
      stop("Unable to balance child:", c.node);
     }
   }  // Node

  class KeyData                                                                 // A key, data pair
   {final int key, data;
    KeyData(int Key, int Data) {z(); key = Key; data = Data;}
    public String toString()
     {return"KeyData(Key:"+key+" data:"+data+")\n";
     }
   }

  class KeyNext                                                                 // A key, next pair
   {final int key, next;
    KeyNext(int Key, int Next) {z(); key = Key; next = Next;}
    KeyNext(         int Next) {z(); key =   0; next = Next;}                   // The zero can in fact be any number so it is not magic.
    public String toString()
     {return"KeyNext(Key:"+key+" next:"+next+")\n";
     }
   }

//D1 Array                                                                      // Key, data pairs in the tree as an array

  Stack<KeyData> toArray()                                                      // Key, data pairs in the tree as an array
   {z();
    final Stack<KeyData> s = new Stack<>();

    if (root.isLeaf) {z(); root.  leafToArray(s);}
    else             {z(); root.branchToArray(s);}
    return s;
   }

//D1 Print                                                                      // Print a BTree horizontally

   String printBoxed()                                                          // Print a tree in a box
    {final String  s = toString();
     final int     n = longestLine(s)-1;
     final String[]L = s.split("\n");
     final StringBuilder t = new StringBuilder();
     t.append("+"); t.append("-".repeat(n)); t.append("+\n");
     for(String l : L) t.append("| "+l+"\n");
     t.append("+"); t.append("-".repeat(n)); t.append("+\n");
     return t.toString();
    }

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
   {z();
    final StringBuilder t = new StringBuilder();                                // Print the lines of the tree that are not blank
    for  (StringBuilder s : S)
     {z();
      final String l = s.toString();
      if (l.isBlank()) continue;
      t.append(l+"|\n");
     }
    return t.toString();
   }

  String print()                                                                // Print a tree horizontally
   {z();
    final Stack<StringBuilder> S = new Stack<>();

    if (root.isLeaf)
     {z();
      root.printLeaf(S, 0);
     }
    else
     {z();
      root.printBranch(S, 0);
     }
    return printCollapsed(S);
   }

//D1 Find                                                                       // Find the data associated with a key

  class Find                                                                    // Find the data associated with a key in the tree
   {Node.FindEqualInLeaf search;                                                // Details of the search of the containing leaf

    Find(int Key)                                                               // Find the data associated with a key in the tree
     {z();
      if (root.isLeaf)                                                          // The root is a leaf
       {z();
        search = root.new FindEqualInLeaf(Key);
        return;
       }

      Node parent = root;                                                       // Parent starts at root which is known to be a branch

      for (int i = 0; i < maxDepth; i++)                                        // Step down through tree
       {z();
        final Node.FindFirstGreaterThanOrEqualInBranch down =                   // Find next child in search path of key
          parent.new FindFirstGreaterThanOrEqualInBranch(Key);
        final int n = down.next;
        if (nodes.elementAt(n).isLeaf)                                          // Found the containing search
         {z();
          final Node l = nodes.elementAt(n);
          search  = l.new FindEqualInLeaf(Key);
          return;
         }
        parent = nodes.elementAt(n);                                            // Step down to lower branch
       }
      stop("Search did not terminate in a leaf");
     }

    Node     leaf()  {z(); return search.leaf;}
    boolean  found() {z(); return search.found;}
    int      index() {z(); return search.index;}
    int       data() {z(); return search.data;}

    public String toString()                                                    // Print find result
     {final StringBuilder s = new StringBuilder();
      s.append("Find(search:"+search);
      s.append( " search:"+index());
      if (found())
       {s.append( " data:"+data());
        s.append(" index:"+index());
       }
      s.append(")\n");
      return s.toString();
     }
   }

  Find find(int Key) {z(); return new Find(Key);}                               // Find a key in the tree

  class FindAndInsert extends Find                                              // Insert the specified key and data into the tree if there is room in the target leaf,or update the key with the data if the key already exists
   {int          key;                                                           // Key to insert
    int         data;                                                           // Data being inserted or updated
    boolean  success;                                                           // Inserted or updated if true
    boolean inserted;                                                           // Inserted if true

    FindAndInsert(int Key, int Data)                                            // Find the leaf that should contain this key and insert or update it is possible
     {super(Key);                                                               // Find the leaf that should contain this key
      z();
      key = Key; data = Data;

      if (found())                                                              // Found the key in the leaf so update it with the new data
       {z();
        leaf().keyData.setElementAt(Key, Data, index());
        success = true; inserted = false;
        return;
       }

      if (!leaf().isFull())                                                     // Leaf is not full so we can insert immediately
       {z();
        final Node.FindFirstGreaterThanOrEqualInLeaf f =
          leaf().new FindFirstGreaterThanOrEqualInLeaf(Key);
//        if (f.found)                                                            // Overwrite existing key
//         {z();
          leaf().keyData.insertElementAt(Key, Data, f.first);
//         }
//        else                                                                    // Insert into position
//         {z();
//          leaf().keyData.push(Key, Data);
//         }
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
   {z();
    return new FindAndInsert(Key, Data);
   }

//D1 Insertion                                                                  // Insert a key, data pair into the tree or update and existing key with a new datum

  void put(int Key, int Data)                                                   // Insert a key, data pair into the tree or update and existing key with a new datum
   {z();
    final FindAndInsert f = new FindAndInsert(Key, Data);                       // Try direct insertion with no modifications to the shape of the tree
    if (f.success) return;                                                      // Inserted or updated successfully

    if (root.isFull())                                                          // Start the insertion at the root, after splitting it if necessary
     {z();
      if (root.isLeaf)
       {z();
        root.splitLeafRoot();
       }
      else
       {z();
        root.splitBranchRoot();
       }
      final FindAndInsert F = new FindAndInsert(Key, Data);                     // Splitting the root might have been enough
      if (F.success) return;                                                    // Inserted or updated successfully
     }

    Node p = root;

    for (int i = 0; i < maxDepth; i++)                                          // Step down from branch to branch through the tree until reaching a leaf repacking as we go
     {z();
      final Node.FindFirstGreaterThanOrEqualInBranch                            // Step down
      down = p.new FindFirstGreaterThanOrEqualInBranch(Key);
      final Node q = nodes.elementAt(down.next);
      if (q.isLeaf)                                                             // Reached a leaf
       {z();
        q.splitLeaf(p, down.first);
        findAndInsert(Key, Data);
        merge(Key);
        return;
       }

      if (q.isFull())
       {z();
        q.splitBranch(p, down.first);                                           // Split the child branch in the search path for the key from the parent so the the search path does not contain a full branch above the containing leaf
        final Node.FindFirstGreaterThanOrEqualInBranch                          // Step down again as the split will have altered the local layout
        Down = p.new FindFirstGreaterThanOrEqualInBranch(Key);
        p = nodes.elementAt(Down.next);
       }
      else
       {z();
        p = q;
       }
     }
    stop("Fallen off the end of the tree");                                     // The tree must be missing a leaf
   }

  void put(int Key)                                                             // Put some test data into the tree
   {z();
    put(Key, Key);
   }

//D1 Deletion                                                                   // Delete a key, data pair from the tree

  Integer findAndDelete(int Key)                                                // Delete a key from the tree and returns its data if present without modifying the shape of tree
   {z();
    final Find f = new Find(Key);                                               // Try direct insertion with no modifications to the shape of the tree
    if (!f.found()) return null;                                                // Inserted or updated successfully
    final Node     l = f.leaf();                                                // The leaf that contains the key
    final int      i = f.index();                                               // Position in the leaf of the key
    final Stuck.ElementAt kd = l.keyData.elementAt(i);                          // Key, data pairs in the leaf
    l.keyData.removeElementAt(i);                                               // Remove the key, data pair from the leaf
    return kd.data;
   }

  Integer delete(int Key)                                                       // Insert a key, data pair into the tree or update and existing key with a new datum
   {z(); root.mergeRoot();

    if (root.isLeaf)                                                            // Find and delete directly in root as a leaf
     {z(); return findAndDelete(Key);
     }

    Node p = root;                                                              // Start at root

    for (int i = 0; i < maxDepth; i++)                                          // Step down from branch to branch through the tree until reaching a leaf repacking as we go
     {z();
      final Node.FindFirstGreaterThanOrEqualInBranch                            // Step down
      down = p.new FindFirstGreaterThanOrEqualInBranch(Key);

      p.balance(down.first);
      final Node q = nodes.elementAt(down.next);

      if (q.isLeaf)                                                             // Reached a leaf
       {z();
        final int data = findAndDelete(Key);
        merge(Key);
        return data;
       }
      p = q;
     }
    stop("Fallen off the end of the tree");                                     // The tree must be missing a leaf
    return null;
   }

//D1 Merge                                                                      // Merge along the specified search path

  void merge(int Key)                                                           // Merge along the specified search path
   {z();
    root.mergeRoot();
    Node p = root;                                                              // Start at root
    for (int i = 0; i < maxDepth; i++)                                          // Step down from branch to branch through the tree until reaching a leaf repacking as we go
     {z();
      if (p.isLeaf) return;
      for (int j = 0; j < p.branchSize(); j++)                                  // Try merging each sibling pair
       {z();
        p.mergeLeftSibling (j);
        p.mergeRightSibling(j);
       }

      final Node.FindFirstGreaterThanOrEqualInBranch                            // Step down
      down = p.new FindFirstGreaterThanOrEqualInBranch(Key);
      p = nodes.elementAt(down.next);
     }
    stop("Fallen off the end of the tree");                                     // The tree must be missing a leaf
   }

//D0 Tests                                                                      // Testing

  final static int[]random_small = {27, 442, 545, 317, 511, 578, 391, 993, 858, 586, 472, 906, 658, 704, 882, 246, 261, 501, 354, 903, 854, 279, 526, 686, 987, 403, 401, 989, 650, 576, 436, 560, 806, 554, 422, 298, 425, 912, 503, 611, 135, 447, 344, 338, 39, 804, 976, 186, 234, 106, 667, 494, 690, 480, 288, 151, 773, 769, 260, 809, 438, 237, 516, 29, 376, 72, 946, 103, 961, 55, 358, 232, 229, 90, 155, 657, 681, 43, 907, 564, 377, 615, 612, 157, 922, 272, 490, 679, 830, 839, 437, 826, 577, 937, 884, 13, 96, 273, 1, 188};
  final static int[]random_large = {5918,5624,2514,4291,1791,5109,7993,60,1345,2705,5849,1034,2085,4208,4590,7740,9367,6582,4178,5578,1120,378,7120,8646,5112,4903,1482,8005,3801,5439,4534,9524,6111,204,5459,248,4284,8037,5369,7334,3384,5193,2847,1660,5605,7371,3430,1786,1216,4282,2146,1969,7236,2187,136,2726,9480,5,4515,6082,969,5017,7809,9321,3826,9179,5781,3351,4819,4545,8607,4146,6682,1043,2890,2964,7472,9405,4348,8333,2915,9674,7225,4743,995,1321,3885,6061,9958,3901,4710,4185,4776,5070,8892,8506,6988,2317,9342,3764,9859,4724,5195,673,359,9740,2089,9942,3749,9208,1,7446,7023,5496,4206,3272,3527,8593,809,3149,4173,9605,9021,5120,5265,7121,8667,6911,4717,2535,2743,1289,1494,3788,6380,9366,2732,1501,8543,8013,5612,2393,7041,3350,3204,288,7213,1741,1238,9830,6722,4687,6758,8067,4443,5013,5374,6986,282,6762,192,340,5075,6970,7723,5913,1060,1641,1495,5738,1618,157,6891,173,7535,4952,9166,8950,8680,1974,5466,2383,3387,3392,2188,3140,6806,3131,6237,6249,7952,1114,9017,4285,7193,3191,3763,9087,7284,9170,6116,3717,6695,6538,6165,6449,8960,2897,6814,3283,6600,6151,4624,3992,5860,9557,1884,5585,2966,1061,6414,2431,9543,6654,7417,2617,878,8848,8241,3790,3370,8768,1694,9875,9882,8802,7072,3772,2689,5301,7921,7774,1614,494,2338,8638,4161,4523,5709,4305,17,9626,843,9284,3492,7755,5525,4423,9718,2237,7401,2686,8751,1585,5919,9444,3271,1490,7004,5980,3904,370,5930,6304,7737,93,5941,9079,4968,9266,262,2766,4999,2450,9518,5137,8405,483,8840,2231,700,8049,8823,9811,9378,3811,8074,153,1940,1998,4354,7830,7086,6132,9967,5680,448,1976,4101,7839,3122,4379,9296,4881,1246,4334,9457,5401,1945,9548,8290,1184,3464,132,2458,7704,1056,7554,6203,2270,6070,4889,7369,1676,485,3648,357,1912,9661,4246,1576,1836,4521,7667,6907,2098,8825,7404,4019,8284,3710,7202,7050,9870,3348,3624,9224,6601,7897,6288,3713,932,5596,353,2615,3273,833,1446,8624,2489,3872,486,1091,2493,4157,3611,6570,7107,9153,4543,9504,4746,1342,9737,3247,8984,3640,5698,7814,307,8775,1150,4330,3059,5784,2370,5248,4806,6107,9700,231,3566,5627,3957,5317,5415,8119,2588,9440,2961,9786,4769,466,5411,3080,7623,5031,2378,9286,4801,797,1527,2325,847,6341,5310,1926,9481,2115,2165,5255,5465,5561,3606,7673,7443,7243,8447,2348,7925,6447,8311,6729,4441,7763,8107,267,8135,9194,6775,3883,9639,612,5024,1351,7557,9241,5181,2239,8002,5446,747,166,325,9925,3820,9531,5163,3545,558,7103,7658,5670,8323,4821,6263,7982,59,3700,1082,4474,4353,8637,9558,5191,842,5925,6455,4092,9929,9961,290,3523,6290,7787,8266,7986,7269,6408,3620,406,5964,7289,1620,6726,1257,1993,7006,5545,2913,5093,5066,3019,7081,6760,6779,7061,9051,8852,8118,2340,6596,4594,9708,8430,8659,8920,9268,5431,9203,2823,1427,2203,6422,6193,5214,9566,8791,4964,7575,4350,56,2227,8545,5646,3089,2204,4081,487,8496,2258,4336,6955,3452,556,8602,8251,8569,8636,9430,1025,9459,7137,8392,3553,5945,9414,3078,1688,5480,327,8117,2289,2195,8564,9423,103,7724,3091,8548,7298,5279,6042,2855,3286,3542,9361,420,7020,4112,5320,5366,6379,114,9174,9744,592,5346,3985,3174,5157,9890,1605,3082,8099,4346,7256,8670,5687,6613,6620,1458,1045,7917,2980,2399,1433,3315,4084,178,7056,2132,2728,4421,9195,4181,6017,6229,2945,4627,2809,8816,6737,18,8981,3813,8890,5304,3789,6959,7476,1856,4197,6944,9578,5915,3060,9932,3463,67,7393,9857,5822,3187,501,653,8453,3691,9736,6845,1365,9645,4120,2157,8471,4436,6435,2758,7591,9805,7142,7612,4891,7342,5764,8683,8365,2967,6947,441,2116,6612,1399,7585,972,6548,5481,7733,7209,222,5903,6161,9172,9628,7348,1588,5992,6094,7176,4214,8702,2987,74,8486,9788,7164,5788,8535,8422,6826,1800,8965,4965,565,5609,4686,2556,9324,5000,9809,1994,4737,63,8992,4783,2536,4462,8868,6346,5553,3980,2670,1601,4272,8725,4698,7333,7826,9233,4198,1997,1687,4851,62,7893,8149,8015,341,2230,1280,5559,9756,3761,7834,6805,9287,4622,5748,2320,1958,9129,9649,1644,4323,5096,9490,7529,6444,7478,7044,9525,7713,234,7553,9099,9885,7135,6493,9793,6268,8363,2267,9157,9451,1438,9292,1637,3739,695,1090,4731,4549,5171,5975,7347,5192,5243,1084,2216,9860,3318,5594,5790,1107,220,9397,3378,1353,4498,6497,5442,7929,7377,9541,9871,9895,6742,9146,9409,292,6278,50,5288,2217,4923,6790,4730,9240,3006,3547,9347,7863,4275,3287,2673,7485,1915,9837,2931,3918,635,9131,1197,6250,3853,4303,790,5548,9993,3702,2446,3862,9652,4432,973,41,3507,8585,2444,1633,956,5789,1523,8657,4869,8580,8474,7093,7812,2549,7363,9315,6731,1130,7645,7018,7852,362,1636,2905,8006,4040,6643,8052,7021,3665,8383,715,1876,2783,3065,604,4566,8761,7911,1983,3836,5547,8495,8144,1950,2537,8575,640,8730,8303,1454,8165,6647,4762,909,9449,8640,9253,7293,8767,3004,4623,6862,8994,2520,1215,6299,8414,2576,6148,1510,313,3693,9843,8757,5774,8871,8061,8832,5573,5275,9452,1248,228,9749,2730};

  static void test_put_ascending()
   {final BtreeStuck t = new BtreeStuck(4, 3);
    final int N = 64;
    for (int i = 1; i <= N; i++) t.put(i);
    //t.stop();
    t.ok("""
                                                                                                                               32                                                                                                                                           |
                                                                                                                               0                                                                                                                                            |
                                                                                                                               50                                                                                                                                           |
                                                                                                                               51                                                                                                                                           |
                                                       16                                                                                                                                              48                                56                                 |
                                                       50                                                                                                                                              51                                51.1                               |
                                                       7                                                                                                                                               28                                52                                 |
                                                       18                                                                                                                                                                                8                                  |
          4          8               12                                 20               24                 28                                  36               40                 44                                  52                                  60              |
          7          7.1             7.2                                18               18.1               18.2                                28               28.1               28.2                                52                                  8               |
          1          4               6                                  12               15                 17                                  22               25                 27                                  38                                  49              |
                                     10                                                                     20                                                                      32                                  43                                  2               |
1,2,3,4=1  5,6,7,8=4    9,10,11,12=6    13,14,15,16=10   17,18,19,20=12   21,22,23,24=15     25,26,27,28=17     29,30,31,32=20   33,34,35,36=22   37,38,39,40=25     41,42,43,44=27     45,46,47,48=32   49,50,51,52=38   53,54,55,56=43     57,58,59,60=49   61,62,63,64=2 |
""");
   }

  static void test_put_ascending_wide()
   {final BtreeStuck t = new BtreeStuck(8, 7);
    final int N = 64;
    for (int i = 1; i <= N; ++i) t.put(i);
    //stop(t);
    t.ok("""
                                                                                                         32                                                                                                                     |
                                                                                                         0                                                                                                                      |
                                                                                                         15                                                                                                                     |
                                                                                                         16                                                                                                                     |
                  8                          16                            24                                                         40                           48                             56                            |
                  15                         15.1                          15.2                                                       16                           16.1                           16.2                          |
                  1                          4                             6                                                          10                           12                             14                            |
                                                                           8                                                                                                                      2                             |
1,2,3,4,5,6,7,8=1   9,10,11,12,13,14,15,16=4     17,18,19,20,21,22,23,24=6     25,26,27,28,29,30,31,32=8   33,34,35,36,37,38,39,40=10   41,42,43,44,45,46,47,48=12     49,50,51,52,53,54,55,56=14     57,58,59,60,61,62,63,64=2 |
""");
   }

  static void test_put_descending()
   {final BtreeStuck t = new BtreeStuck(2, 3);
    final int N = 64;
    for (int i = N; i > 0; --i) t.put(i);
    //t.stop();
    t.ok("""
                                                                                        16                                                                                                32                                                                                                                                                                                              |
                                                                                        0                                                                                                 0.1                                                                                                                                                                                             |
                                                                                        102                                                                                               100                                                                                                                                                                                             |
                                                                                                                                                                                          68                                                                                                                                                                                              |
                   4                  8                                                                                                  24                                                                                                40                                              48                                                56                                           |
                   102                102.1                                                                                              100                                                                                               68                                              68.1                                              68.2                                         |
                   103                95                                                                                                 76                                                                                                52                                              28                                                18                                           |
                                      86                                                                                                 62                                                                                                                                                                                                  7                                            |
        2                    6                     10         12           14                       18         20           22                       26         28           30                       34         36           38                      42         44           46                        50         52           54                        58        60         62         |
        103                  95                    86         86.1         86.2                     76         76.1         76.2                     62         62.1         62.2                     52         52.1         52.2                    28         28.1         28.2                      18         18.1         18.2                      7         7.1        7.2        |
        104                  97                    89         85           83                       78         75           73                       65         61           59                       54         49           43                      32         27           25                        20         17           15                        10        6          4          |
        101                  93                                            80                                               69                                               56                                               38                                              22                                                12                                             1          |
1,2=104    3,4=101    5,6=97   7,8=93      9,10=89   11,12=85     13,14=83     15,16=80    17,18=78   19,20=75     21,22=73     23,24=69    25,26=65   27,28=61     29,30=59     31,32=56    33,34=54   35,36=49     37,38=43     39,40=38   41,42=32   43,44=27     45,46=25     47,48=22     49,50=20   51,52=17     53,54=15     55,56=12     57,58=10   59,60=6    61,62=4    63,64=1 |
""");
   }

  static void test_put_small_random()
   {final BtreeStuck t = new BtreeStuck(6, 3);
    for (int i = 0; i < random_small.length; ++i) t.put(random_small[i]);
    //stop(t);
    t.ok("""
                                                                                                                                                                                                                                                      476                                                                                                                                                                                                                                                                                      |
                                                                                                                                                                                                                                                      0                                                                                                                                                                                                                                                                                        |
                                                                                                                                                                                                                                                      23                                                                                                                                                                                                                                                                                       |
                                                                                                                                                                                                                                                      24                                                                                                                                                                                                                                                                                       |
                                                                           160                                                                                              354                                                                                                                                                            582                                                                           781                                                 892                                                               |
                                                                           23                                                                                               23.1                                                                                                                                                           24                                                                            24.1                                                24.2                                                              |
                                                                           33                                                                                               29                                                                                                                                                             25                                                                            10                                                  37                                                                |
                                                                                                                                                                            6                                                                                                                                                                                                                                                                                                7                                                                 |
                 41            81                   120                                                  241               270                            327                                              419                       439                                    502                   535                562                                             654                           688                                              831                                         909                   949                      |
                 33            33.1                 33.2                                                 29                29.1                           29.2                                             6                         6.1                                    25                    25.1               25.2                                            10                            10.1                                             37                                          7                     7.1                      |
                 39            40                   30                                                   31                13                             22                                               12                        27                                     28                    11                 36                                              32                            19                                               35                                          38                    14                       |
                                                    16                                                                                                    5                                                                          1                                                                               8                                                                             3                                                9                                                                 2                        |
1,13,27,29,39=39   43,55,72=40     90,96,103,106=30     135,151,155,157=16    186,188,229,232,234,237=31    246,260,261=13     272,273,279,288,298,317=22     338,344,354=5     358,376,377,391,401,403=12    422,425,436,437,438=27    442,447,472=1    480,490,494,501=28    503,511,516,526=11     545,554,560=36     564,576,577,578=8    586,611,612,615,650=32    657,658,667,679,681,686=19     690,704,769,773=3     804,806,809,826,830=35    839,854,858,882,884=9     903,906,907=38    912,922,937,946=14    961,976,987,989,993=2 |
""");
   }

  static void test_put_large_random()
   {if (!github_actions) return;
    final BtreeStuck t = new BtreeStuck(2, 3);
    final TreeMap<Integer,Integer> s = new TreeMap<>();
    for (int i = 0; i < random_large.length; ++i)
     {final int r = random_large[i];
      s.put(r, i);
      t.put(r, i);
     }
    final int a = s.firstKey(), b = s.lastKey();
    for (int i = a-1; i < b + 1; ++i)
     {if (s.containsKey(i))
       {Find f = t.find(i);
        ok(f.found());
        ok(f.data(), s.get(i));
       }
      else
       {Find f = t.find(i);
        ok(!f.found());
       }
     }
   }

  static void test_find()
   {final BtreeStuck t = new BtreeStuck(8, 3);
    final int N = 32;
    for (int i = 1; i <= N; i++) t.put(2*i);                                    // Insert
    //stop(t);
    t.ok("""
                                                  33                                                      |
                                                  0                                                       |
                                                  7                                                       |
                                                  8                                                       |
                      17                                                      49                          |
                      7                                                       8                           |
                      1                                                       6                           |
                      4                                                       2                           |
2,4,6,8,10,12,14,16=1   18,20,22,24,26,28,30,32=4   34,36,38,40,42,44,46,48=6   50,52,54,56,58,60,62,64=2 |
""");
    for (int i = 0; i <= 2*N+1; i++)                                            // Update
     {Find f = t.find(i);
      if (i > 0 && i % 2 == 0)
       {ok(f.found(), true);
        ok(f.data(),  i);
        t.put(i, i-1);
       }
      else ok(f.found(), false);
     }
    for (int i = 0; i <= 2*N+1; i++)
     {Find f = t.find(i);
      if (i > 0 && i % 2 == 0)
       {ok(f.found(), true);
        ok(f.data(),  i-1);
       }
      else ok(f.found(), false);
     }
   }

  static void test_delete()
   {final BtreeStuck t = new BtreeStuck(4, 3);
    final int N = 32;
    final boolean box = false;                                                  // Print read me
    for (int i = 1; i <= N; i++) t.put(i);
    //t.stop();
    t.ok("""
                                                       16                                24                                |
                                                       0                                 0.1                               |
                                                       7                                 18                                |
                                                                                         8                                 |
          4          8               12                                 20                                 28              |
          7          7.1             7.2                                18                                 8               |
          1          4               6                                  12                                 17              |
                                     10                                 15                                 2               |
1,2,3,4=1  5,6,7,8=4    9,10,11,12=6    13,14,15,16=10   17,18,19,20=12   21,22,23,24=15    25,26,27,28=17   29,30,31,32=2 |
""");

    if (box) say("At start with", N, "elements", t.printBoxed());

    for (int i = 1; i <= N; i++)
     {t.delete(i);
      //say("        case", i, "-> t.ok(\"\"\"", t, "\"\"\");");
      if (box) say("After deleting:", i, t.printBoxed());
      switch(i)
       {case 1 -> t.ok("""
                                                     16                                                                     |
                                                     0                                                                      |
                                                     7                                                                      |
                                                     18                                                                     |
        4          8               12                                 20               24                 28                |
        7          7.1             7.2                                18               18.1               18.2              |
        1          4               6                                  12               15                 17                |
                                   10                                                                     2                 |
2,3,4=1  5,6,7,8=4    9,10,11,12=6    13,14,15,16=10   17,18,19,20=12   21,22,23,24=15     25,26,27,28=17     29,30,31,32=2 |
""");
        case 2 -> t.ok("""
                                                   16                                                                     |
                                                   0                                                                      |
                                                   7                                                                      |
                                                   18                                                                     |
      4          8               12                                 20               24                 28                |
      7          7.1             7.2                                18               18.1               18.2              |
      1          4               6                                  12               15                 17                |
                                 10                                                                     2                 |
3,4=1  5,6,7,8=4    9,10,11,12=6    13,14,15,16=10   17,18,19,20=12   21,22,23,24=15     25,26,27,28=17     29,30,31,32=2 |
""");
        case 3 -> t.ok("""
                                                 16                                                                     |
                                                 0                                                                      |
                                                 7                                                                      |
                                                 18                                                                     |
    4          8               12                                 20               24                 28                |
    7          7.1             7.2                                18               18.1               18.2              |
    1          4               6                                  12               15                 17                |
                               10                                                                     2                 |
4=1  5,6,7,8=4    9,10,11,12=6    13,14,15,16=10   17,18,19,20=12   21,22,23,24=15     25,26,27,28=17     29,30,31,32=2 |
""");
        case 4 -> t.ok("""
                                          16                                                                     |
                                          0                                                                      |
                                          7                                                                      |
                                          18                                                                     |
          8             12                                 20               24                 28                |
          7             7.1                                18               18.1               18.2              |
          1             6                                  12               15                 17                |
                        10                                                                     2                 |
5,6,7,8=1  9,10,11,12=6    13,14,15,16=10   17,18,19,20=12   21,22,23,24=15     25,26,27,28=17     29,30,31,32=2 |
""");
        case 5 -> t.ok("""
                                        16                                                                     |
                                        0                                                                      |
                                        7                                                                      |
                                        18                                                                     |
        8             12                                 20               24                 28                |
        7             7.1                                18               18.1               18.2              |
        1             6                                  12               15                 17                |
                      10                                                                     2                 |
6,7,8=1  9,10,11,12=6    13,14,15,16=10   17,18,19,20=12   21,22,23,24=15     25,26,27,28=17     29,30,31,32=2 |
""");
        case 6 -> t.ok("""
                                      16                                                                     |
                                      0                                                                      |
                                      7                                                                      |
                                      18                                                                     |
      8             12                                 20               24                 28                |
      7             7.1                                18               18.1               18.2              |
      1             6                                  12               15                 17                |
                    10                                                                     2                 |
7,8=1  9,10,11,12=6    13,14,15,16=10   17,18,19,20=12   21,22,23,24=15     25,26,27,28=17     29,30,31,32=2 |
""");
        case 7 -> t.ok("""
                                    16                                                                     |
                                    0                                                                      |
                                    7                                                                      |
                                    18                                                                     |
    8             12                                 20               24                 28                |
    7             7.1                                18               18.1               18.2              |
    1             6                                  12               15                 17                |
                  10                                                                     2                 |
8=1  9,10,11,12=6    13,14,15,16=10   17,18,19,20=12   21,22,23,24=15     25,26,27,28=17     29,30,31,32=2 |
""");
        case 8 -> t.ok("""
                              16                                                                     |
                              0                                                                      |
                              7                                                                      |
                              18                                                                     |
             12                                20               24                 28                |
             7                                 18               18.1               18.2              |
             1                                 12               15                 17                |
             10                                                                    2                 |
9,10,11,12=1   13,14,15,16=10   17,18,19,20=12   21,22,23,24=15     25,26,27,28=17     29,30,31,32=2 |
""");
        case 9 -> t.ok("""
                                              20                                                  |
                                              0                                                   |
                                              7                                                   |
                                              18                                                  |
           12               16                                 24               28                |
           7                7.1                                18               18.1              |
           1                10                                 15               17                |
                            12                                                  2                 |
10,11,12=1   13,14,15,16=10    17,18,19,20=12   21,22,23,24=15   25,26,27,28=17     29,30,31,32=2 |
""");
        case 10 -> t.ok("""
                                           20                                                  |
                                           0                                                   |
                                           7                                                   |
                                           18                                                  |
        12               16                                 24               28                |
        7                7.1                                18               18.1              |
        1                10                                 15               17                |
                         12                                                  2                 |
11,12=1   13,14,15,16=10    17,18,19,20=12   21,22,23,24=15   25,26,27,28=17     29,30,31,32=2 |
""");
        case 11 -> t.ok("""
                                        20                                                  |
                                        0                                                   |
                                        7                                                   |
                                        18                                                  |
     12               16                                 24               28                |
     7                7.1                                18               18.1              |
     1                10                                 15               17                |
                      12                                                  2                 |
12=1   13,14,15,16=10    17,18,19,20=12   21,22,23,24=15   25,26,27,28=17     29,30,31,32=2 |
""");
        case 12 -> t.ok("""
                               20                                                  |
                               0                                                   |
                               7                                                   |
                               18                                                  |
              16                                24               28                |
              7                                 18               18.1              |
              1                                 15               17                |
              12                                                 2                 |
13,14,15,16=1   17,18,19,20=12   21,22,23,24=15   25,26,27,28=17     29,30,31,32=2 |
""");
        case 13 -> t.ok("""
                                              24                               |
                                              0                                |
                                              7                                |
                                              18                               |
           16               20                                 28              |
           7                7.1                                18              |
           1                12                                 17              |
                            15                                 2               |
14,15,16=1   17,18,19,20=12    21,22,23,24=15   25,26,27,28=17   29,30,31,32=2 |
""");
        case 14 -> t.ok("""
                                           24                               |
                                           0                                |
                                           7                                |
                                           18                               |
        16               20                                 28              |
        7                7.1                                18              |
        1                12                                 17              |
                         15                                 2               |
15,16=1   17,18,19,20=12    21,22,23,24=15   25,26,27,28=17   29,30,31,32=2 |
""");
        case 15 -> t.ok("""
                                        24                               |
                                        0                                |
                                        7                                |
                                        18                               |
     16               20                                 28              |
     7                7.1                                18              |
     1                12                                 17              |
                      15                                 2               |
16=1   17,18,19,20=12    21,22,23,24=15   25,26,27,28=17   29,30,31,32=2 |
""");
        case 16 -> t.ok("""
                               24                               |
                               0                                |
                               7                                |
                               18                               |
              20                                28              |
              7                                 18              |
              1                                 17              |
              15                                2               |
17,18,19,20=1   21,22,23,24=15   25,26,27,28=17   29,30,31,32=2 |
""");
        case 17 -> t.ok("""
           20               24                28               |
           0                0.1               0.2              |
           1                15                17               |
                                              2                |
18,19,20=1   21,22,23,24=15    25,26,27,28=17    29,30,31,32=2 |
""");
        case 18 -> t.ok("""
        20               24                28               |
        0                0.1               0.2              |
        1                15                17               |
                                           2                |
19,20=1   21,22,23,24=15    25,26,27,28=17    29,30,31,32=2 |
""");
        case 19 -> t.ok("""
     20               24                28               |
     0                0.1               0.2              |
     1                15                17               |
                                        2                |
20=1   21,22,23,24=15    25,26,27,28=17    29,30,31,32=2 |
""");
        case 20 -> t.ok("""
              24               28               |
              0                0.1              |
              1                17               |
                               2                |
21,22,23,24=1   25,26,27,28=17    29,30,31,32=2 |
""");
        case 21 -> t.ok("""
           24               28               |
           0                0.1              |
           1                17               |
                            2                |
22,23,24=1   25,26,27,28=17    29,30,31,32=2 |
""");
        case 22 -> t.ok("""
        24               28               |
        0                0.1              |
        1                17               |
                         2                |
23,24=1   25,26,27,28=17    29,30,31,32=2 |
""");
        case 23 -> t.ok("""
     24               28               |
     0                0.1              |
     1                17               |
                      2                |
24=1   25,26,27,28=17    29,30,31,32=2 |
""");
        case 24 -> t.ok("""
              28              |
              0               |
              1               |
              2               |
25,26,27,28=1   29,30,31,32=2 |
""");
        case 25 -> t.ok("""
           28              |
           0               |
           1               |
           2               |
26,27,28=1   29,30,31,32=2 |
""");
        case 26 -> t.ok("""
        28              |
        0               |
        1               |
        2               |
27,28=1   29,30,31,32=2 |
""");
        case 27 -> t.ok("""
     28              |
     0               |
     1               |
     2               |
28=1   29,30,31,32=2 |
""");
        case 28 -> t.ok("""
29,30,31,32=0 |
""");
        case 29 -> t.ok("""
30,31,32=0 |
""");
        case 30 -> t.ok("""
31,32=0 |
""");
        case 31 -> t.ok("""
32=0 |
""");
        case 32 -> t.ok("""
=0 |
""");
       }
     }
   }

  static void test_to_array()
   {final BtreeStuck t = new BtreeStuck(2, 3);

    final int M = 2;
    for (int i = 1; i <= M; i++) t.put(i);
    ok(""+t.toArray(), """
[KeyData(Key:1 data:1)
, KeyData(Key:2 data:2)
]""");

    final int N = 16;
    for (int i = M; i <= N; i++) t.put(i);
    ok(""+t.toArray(), """
[KeyData(Key:1 data:1)
, KeyData(Key:2 data:2)
, KeyData(Key:3 data:3)
, KeyData(Key:4 data:4)
, KeyData(Key:5 data:5)
, KeyData(Key:6 data:6)
, KeyData(Key:7 data:7)
, KeyData(Key:8 data:8)
, KeyData(Key:9 data:9)
, KeyData(Key:10 data:10)
, KeyData(Key:11 data:11)
, KeyData(Key:12 data:12)
, KeyData(Key:13 data:13)
, KeyData(Key:14 data:14)
, KeyData(Key:15 data:15)
, KeyData(Key:16 data:16)
]""");
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_put_ascending();
    test_put_ascending_wide();
    test_put_descending();
    test_put_small_random();
    test_put_large_random();
    test_find();
    test_delete();
    test_to_array();
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
