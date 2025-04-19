//------------------------------------------------------------------------------
// Memory
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Simulate a silicon chip.

import java.util.*;

class Memory extends Test                                                       // Memory provided in bits
 {private final boolean[]bits;                                                     // The memory in bits with one bit per byte to speed up emulations
  final String name;                                                            // The name of the memory
  private long reads, writes;                                                   // Number of reads and writes for this memory

//D1 Construct                                                                  // Memory provided in bits

  Memory(String Name, int Size)                                                 // Create memory
   {zz(); name = Name; bits = new boolean[Size];
   }

  static Memory memory(String Name, int Size)                                   // Create memory
   {z(); return new Memory(Name, Size);
   }

  int size() {z(); return bits.length;}                                         // Size of memory

//D1 Print                                                                      // Print memory

  StringBuilder print(int Start, int Length)                                    // Print a part of the memory
   {zz();
    final StringBuilder s = new StringBuilder();
    for(int i = Start; i < Start+Length; i++) s.append(bits[i] ? "1" : "0");
    s.reverse();
    return s;
   }

  StringBuilder print()                                                         // Print all of memory
   {zz();
    return print(0, bits.length);
   }

  public String toString()                                                      // Print memory in hex
   {z();
    final int N = 256;
    final String T = "4... 4... 4... 4... "+
                     "3... 3... 3... 3... "+
                     "2... 2... 2... 2... "+
                     "1... 1... 1... 1...";
    final String t = (""+new StringBuilder("0123 4567 89AB CDEF ".repeat(4)).reverse()).trim();

    final StringBuilder B = new StringBuilder();                                // One block a multiple of N in length
    for(int i = 0; i < bits.length; i++) B.append(getBit(i) ? '1' : '0');
    for(int i = 0; i < N && B.length() % N > 0; i++) B.append('0');

    final StringBuilder S = new StringBuilder();                                // Final result
    for   (int i = 0, n = B.length() / N; i < n; ++i)                           // Lines of hex
     {z();
      final StringBuilder s = new StringBuilder();                              // Line of hex
      for (int j = 0; j < N; j += 4)                                            // Blocks of 4 bits
       {final StringBuilder H = new StringBuilder(B.substring(i*N+j, i*N+j+4)); // Bits to convert
        final int h = Integer.parseInt(""+H.reverse(), 2);                      // Low endian
        s.append("0123456789abcdef".charAt(h));
        if (j % 16 == 12) s.append(" ");
       }
      S.append(String.format("%4d  ", i)+(""+s.reverse()).trim()+"\n");
     }
    return "Memory: "+name+"\n      "+T+"\nLine  "+t+"\n"+S;
   }

//D1 Operations                                                                 // Operations on memory

//D2 Basic                                                                      // Basic memory access

//D3 Statistics                                                                 // Statistics on memory usage

  long   reads() {return reads;}                                                // Number of reads
  long  writes() {return writes;}                                               // Number of writes
  boolean used() {return reads > 0 || writes > 0;}

//D3 Copy                                                                       // Copy memory from source to target

  void check(int start, int width)                                              // Check a request is in the range of the memory
   {if (start < 0) stop("Too small:", start);
    if (start + width  > size()) stop("Out of range. Addressing:", start+width,
     "in memory of size:", size());
    if  (width < 1) stop("Width must be one or more, not:", width);
   }

  void copy(Memory...sources)                                                   // Initialize this memory from a source memory by copying as many bits as possible into the start of the target memory from the source memory
   {zz();
    int t = 0;
    for (int s = 0; s < sources.length; s++)                                    // Copy each source
     {final Memory source = sources[s];
      final int N = min(size() - t, source.size());                             // Amount to copy
      for(int i = 0; i < N; ++i)
       {z();
        bits[t++] = source.bits[i];                                             // Copy bit by bit
       }
      writes += N; source.reads += N;
     }
   }

  void copy(Memory source, int offset)                                          // Initialize this memory from a source memory by copying as many bits as possible into the start of the target memory from the indexed location in the source memory
   {z();
    final int N = min(size(), source.size()-offset);

    for(int i = 0; i < N; ++i)
     {z();
      bits[i] = source.bits[offset+i];
     }
    writes += N; source.reads += N;
   }

  void copy(int targetOffset, Memory source, int sourceOffset, int Length)      // Initialize part of this memory from part of a source memory
   {z();
    final int N = min(size() - targetOffset, source.size()- sourceOffset, Length);

    for(int i = 0; i < N; ++i)
     {z();
      bits[targetOffset + i] = source.bits[sourceOffset+i];
     }
    writes += N; source.reads += N;
   }

//D3 Set                                                                        // Set a bit

  void set(int start, boolean value)                                            // Set a bit from a boolean
   {zz();
    //check(start, 1);
    bits[start] = value;
    writes += 1;
   }

  void set(int start, int width, int value)                                     // Set some memory from an integer
   {//check(start, width);
    zz();
    final int w = min(Integer.SIZE-1, width);
    for(int i = 0; i < w; ++i)
     {z();
      final int n = value & (1 << i);
      set(start+i, n != 0);
     }
   }

//D3 Get                                                                        // Get a bit

  boolean getBit(int start)                                                     // Get a boolean from memory
   {//check(start, 1);
    zz(); reads += 1; return bits[start];
   }

  int getInt(int start, int width)                                              // Get an int from memory
   {//check(start, width);
    zz();
    if (start < 0 || start + width  > size()) stop("Out of range in memory", name, "Start:", start, "width:", width, "size:", size());
    z();
    if  (width < 1) stop("width must be one or more, not:", width);
    z();
    final int w = min(Integer.SIZE-1, width);
    int n = 0;
    for(int i = 0; i < w; ++i)
     {z();
      if (getBit(start+i)) {z(); n |= (1 << i);}
     }
    return n;
   }

//D2 Composite                                                                  // Composite memory access

  void zero() {z(); for(int i = 0; i < bits.length; ++i) {z(); set(i, false);}} // Zero all memory
  void ones() {z(); for(int i = 0; i < bits.length; ++i) {z(); set(i, true);}}  // Ones all memory

  void zero(int start, int width)                                               // Zero some memory
   {z(); for(int i = 0; i < width; ++i) {z(); set(start+i, false);}
   }

  void ones(int start, int width)                                               // Ones some memory
   {z(); for(int i = 0; i < width; ++i) {z(); set(start+i, true);}
   }

  boolean isAllZero(int start, int width)                                       // Check that the specified memory is all zeros
   {z();
    for(int i = 0; i < width; ++i)
     {z();
      if (getBit(start+i))
       {z();
        return false;
       }
     }
    return true;
   }

  boolean isAllOnes(int start, int width)                                       // Check that  the specified memory is all ones
   {z();
    for(int i = 0; i < width; ++i)
     {z();
      if (!getBit(start+i)) {z(); return false;}
     }
    return true;
   }

  void copy(int target, int source, int width)                                  // Copy the specified number of bits from source to target low bits first
   {z();
    for(int i = 0; i < width; ++i)
     {z();
      set(target+i, getBit(source+i));
     }
   }

  void copyHigh(int target, int source, int width)                              // Copy the specified number of bits from source to target high bits first
   {z();
    for(int i = width; i > 0; --i)
     {z();
      set(target+i-1, getBit(source+i-1));
     }
   }

  void invert(int start, int width)                                             // Invert the specified bits
   {z();
    for(int i = 0; i < width; ++i)
     {z();
      set(start+i, !getBit(start+i));
     }
   }

//D2 Boolean operations

  boolean equals(int a, int b, int width)                                       // Whether a == b
   {z();
    for(int i = 0; i < width; ++i)
     {z();
      if (getBit(a+i) != getBit(b+i))
       {z();
        return false;
       }
     }
    z();
    return true;
   }

  boolean notEquals(int a, int b, int width)                                    // Whether a != b
   {z(); return !equals(a, b, width);
   }

  boolean lessThan(int a, int b, int width)                                     // Whether a < b
   {z();
    for(int i = width; i > 0; --i)
     {z();
      if (!getBit(a+i-1) &&  getBit(b+i-1)) {z(); return true;}
      if ( getBit(a+i-1) && !getBit(b+i-1)) {z(); return false;}
     }
    z();
    return false;
   }

  boolean lessThanOrEqual(int a, int b, int width)                              // Whether a <= b
   {z();
    return lessThan(a, b, width) || equals(a, b, width);
   }

  boolean greaterThan(int a, int b, int width)                                  // Whether a > b
   {z();
    return !lessThan(a, b, width) && !equals(a, b, width);
   }

  boolean greaterThanOrEqual(int a, int b, int width)                           // Whether a >= b
   {z();
    return !lessThan(a, b, width);
   }

//D1 Patterns                                                                   // Pattern the memory to make testing more interesting

  void alternating(int b)                                                       // Alternate between 0 and 1 in blocks of the specified size
   {z();
    final int N = size();
    boolean v = false;
    for(int i = 0, j = 0; i < N; ++i, ++j)
     {z(); if (j == b) {j = 0; v = !v;}
      set(i, v);
     }
   }

//D0                                                                            // Tests

  static void test_set_get()
   {z();
    Memory m = memory(currentTestName(), 8);
    m.set(2, 2, 3);
    m.set(5, 1, 1);

    ok(m.getInt(5, 2), 1);
    ok(m.getInt(2, 2), 3);
    ok(m.getInt(2, 4), 11);

    ok(m.isAllOnes(2, 2)); ok(!m.isAllOnes(2, 4));
    ok(m.isAllZero(0, 2)); ok(!m.isAllZero(0, 4));
   }

  static void test_zero_ones()
   {z();
    Memory m = memory(currentTestName(), 8);
    m.ones(2, 4);
    m.zero(4, 1);

    ok(m.getInt(2, 4), 11);
   }

  static void test_copy()
   {z();
    Memory m = memory(currentTestName(), 16);

    m.ones(1, 2);
    //stop(m);
    ok(""+m, """
Memory: test_copy
      4... 4... 4... 4... 3... 3... 3... 3... 2... 2... 2... 2... 1... 1... 1... 1...
Line  FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210
   0  0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0006
""");

    m.copyHigh(5, 1, 2);
    //stop(m);
    ok(""+m, """
Memory: test_copy
      4... 4... 4... 4... 3... 3... 3... 3... 2... 2... 2... 2... 1... 1... 1... 1...
Line  FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210
   0  0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0066
""");

    m.copy(8, 0, 8);
    //stop(m);
    ok(""+m, """
Memory: test_copy
      4... 4... 4... 4... 3... 3... 3... 3... 2... 2... 2... 2... 1... 1... 1... 1...
Line  FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210
   0  0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 6666
""");
   }

  static void test_invert()
   {z();
    Memory m = memory(currentTestName(), 8);

    m.ones(1, 2);
    //stop(m);
    ok(""+m, """
Memory: test_invert
      4... 4... 4... 4... 3... 3... 3... 3... 2... 2... 2... 2... 1... 1... 1... 1...
Line  FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210
   0  0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0006
""");

    m.invert(2, 2);
    //stop(m);
    ok(""+m, """
Memory: test_invert
      4... 4... 4... 4... 3... 3... 3... 3... 2... 2... 2... 2... 1... 1... 1... 1...
Line  FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210
   0  0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 000a
""");
   }

  static void test_boolean()
   {z();
    for   (int i = 0; i <= 2; i++)
     {for (int j = 0; j <= 2; j++)
       {Memory m = memory(currentTestName(), 8);
        m.set(0, 4, i);
        m.set(4, 4, j);
        ok(m.equals            (0, 4, 4) == (i == j));
        ok(m.notEquals         (0, 4, 4) == (i != j));
        ok(m.lessThan          (0, 4, 4) == (i <  j));
        ok(m.lessThanOrEqual   (0, 4, 4) == (i <= j));
        ok(m.greaterThan       (0, 4, 4) == (i >  j));
        ok(m.greaterThanOrEqual(0, 4, 4) == (i >= j));
       }
     }
   }

  static void test_alternating()
   {z();
    Memory m = memory(currentTestName(), 256);
    m.alternating(4);
    //stop(m);
    ok(""+m, """
Memory: test_alternating
      4... 4... 4... 4... 3... 3... 3... 3... 2... 2... 2... 2... 1... 1... 1... 1...
Line  FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210
   0  f0f0 f0f0 f0f0 f0f0 f0f0 f0f0 f0f0 f0f0 f0f0 f0f0 f0f0 f0f0 f0f0 f0f0 f0f0 f0f0
""");
   }

  static void test_all_zeros_and_ones()
   {z();
    Memory m = memory(currentTestName(), 256);
    m.alternating(4);
    m.ones();
    //stop(m);
    ok(""+m, """
Memory: test_all_zeros_and_ones
      4... 4... 4... 4... 3... 3... 3... 3... 2... 2... 2... 2... 1... 1... 1... 1...
Line  FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210
   0  ffff ffff ffff ffff ffff ffff ffff ffff ffff ffff ffff ffff ffff ffff ffff ffff
""");
    m.zero();
    //stop(m);
    ok(""+m, """
Memory: test_all_zeros_and_ones
      4... 4... 4... 4... 3... 3... 3... 3... 2... 2... 2... 2... 1... 1... 1... 1...
Line  FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210
   0  0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000
""");
   }

  static void test_copy_memory()
   {z();
    Memory m = memory("aaa", 8);
    Memory n = memory("bbb", 8);
    m.alternating(4);
    //stop(m);
    ok(m, """
Memory: aaa
      4... 4... 4... 4... 3... 3... 3... 3... 2... 2... 2... 2... 1... 1... 1... 1...
Line  FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210
   0  0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 00f0
""");
    //stop(n);
    ok(n, """
Memory: bbb
      4... 4... 4... 4... 3... 3... 3... 3... 2... 2... 2... 2... 1... 1... 1... 1...
Line  FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210
   0  0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000
""");
    n.copy(m);
    //stop(n);
    ok(n, """
Memory: bbb
      4... 4... 4... 4... 3... 3... 3... 3... 2... 2... 2... 2... 1... 1... 1... 1...
Line  FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210
   0  0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 00f0
""");
   }

  static void test_copy_source()
   {z();
    Memory m = memory("aaa",  256);
    Memory n = memory("bbb",  128);
    m.alternating(4);
    //stop(m);
    ok(m, """
Memory: aaa
      4... 4... 4... 4... 3... 3... 3... 3... 2... 2... 2... 2... 1... 1... 1... 1...
Line  FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210
   0  f0f0 f0f0 f0f0 f0f0 f0f0 f0f0 f0f0 f0f0 f0f0 f0f0 f0f0 f0f0 f0f0 f0f0 f0f0 f0f0
""");

    //stop(n);
    ok(n, """
Memory: bbb
      4... 4... 4... 4... 3... 3... 3... 3... 2... 2... 2... 2... 1... 1... 1... 1...
Line  FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210
   0  0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000
""");

    n.copy(m, 1);
    //stop(n);
    ok(n, """
Memory: bbb
      4... 4... 4... 4... 3... 3... 3... 3... 2... 2... 2... 2... 1... 1... 1... 1...
Line  FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210
   0  0000 0000 0000 0000 0000 0000 0000 0000 7878 7878 7878 7878 7878 7878 7878 7878
""");
   }

  static void test_copy_memories()
   {z();
    Memory a = memory("aaa",  256);
    Memory b = memory("bbb",  256);
    Memory c = memory("ccc",  256);
    b.alternating(4);
    c.alternating(2);
    a.copy(  4, b, 0,  64);
    a.copy( 68, c, 0,  64);
    a.copy(192, c, 0, 128);
    //stop(a);
    ok(a, """
Memory: aaa
      4... 4... 4... 4... 3... 3... 3... 3... 2... 2... 2... 2... 1... 1... 1... 1...
Line  FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210
   0  cccc cccc cccc cccc 0000 0000 0000 000c cccc cccc cccc cccf 0f0f 0f0f 0f0f 0f00
""");
   }

  static void test_print()
   {z();
    Memory m = memory("aaa",  256);
    m.alternating(4);
    //stop(m);
    ok(m, """
Memory: aaa
      4... 4... 4... 4... 3... 3... 3... 3... 2... 2... 2... 2... 1... 1... 1... 1...
Line  FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210 FEDC BA98 7654 3210
   0  f0f0 f0f0 f0f0 f0f0 f0f0 f0f0 f0f0 f0f0 f0f0 f0f0 f0f0 f0f0 f0f0 f0f0 f0f0 f0f0
""");

    //stop(m.print());
    ok(m.print(), "1111000011110000111100001111000011110000111100001111000011110000111100001111000011110000111100001111000011110000111100001111000011110000111100001111000011110000111100001111000011110000111100001111000011110000111100001111000011110000111100001111000011110000");
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_set_get();
    test_zero_ones();
    test_copy();
    test_invert();
    test_boolean();
    test_alternating();
    test_all_zeros_and_ones();
    test_copy_memory();
    test_copy_source();
    test_copy_memories();
    test_print();
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
