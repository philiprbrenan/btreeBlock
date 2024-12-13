//------------------------------------------------------------------------------
// Test a java program.
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout digital a binary tree on a silicon chip.
// Extend coverage to multiple files
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import java.text.*;

//D1 Construct                                                                  // Develop and test a java program

public class Test                                                               // Describe a chip and emulate its operation.
 {final static boolean github_actions =                                         // Whether we are on a github
    "true".equals(System.getenv("GITHUB_ACTIONS"));
  final static long start = System.nanoTime();                                  // Start time
  final static Stack<String> sayThisOrStop = new Stack<>();                     // The next says should say this or else we should stop

//D1 Utility routines                                                           // Utility routines

//D2 String routines                                                            // String routines

  static String binaryString(int n, int width)                                  // Convert a integer to a binary string of specified width
   {final String b = "0".repeat(width)+Long.toBinaryString(n);
    return b.substring(b.length() - width);
   }

  static String   ones(int n) {return "1".repeat(n);}                           // A string of ones
  static String zeroes(int n) {return "0".repeat(n);}                           // A string of zeroes

  static int longestLine(String s)                                              // Longest line  in a string
   {int l = 0, i = 0, j = 0;
    for (; i < s.length(); i++, j++)
     {if (s.charAt(i) == '\n')
       {l = max(l, j);
        j = 0;
       }
     }
    return max(l, j);
   }

//D2 Numeric routines                                                           // Numeric routines

  static int max(int n, int...rest)                                             // Maximum of some numbers
   {int m = n;
    for (int i = 0; i < rest.length; i++) m = m < rest[i] ? rest[i] : m;
    return m;
   }

  static int min(int n, int...rest)                                             // Minimum of some numbers
   {int m = n;
    for (int i = 0; i < rest.length; i++) m = m > rest[i] ? rest[i] : m;
    return m;
   }

  static double max(double n, double...rest)                                    // Maximum number from a list of one or more numbers
   {double m = n;
    for (int i = 0; i < rest.length; ++i) m = m < rest[i] ? rest[i] : m;
    return m;
   }

  static double min(double n, double...rest)                                    // Minimum number from a list of one or more numbers
   {double m = n;
    for (int i = 0; i < rest.length; ++i) m = m > rest[i] ? rest[i] : m;
    return m;
   }

  static int nextPowerOfTwo(int n)                                              // If this is a power of two return it, else return the next power of two greater than this number
   {int p = 1;
    for (int i = 0; i < 32; ++i, p *= 2) if (p >= n) return p;
    stop("Cannot find next power of two for", n);
    return -1;
   }

  static int logTwo(int n)                                                      // Log 2 of containing power of 2
   {int p = 1;
    for (int i = 0; i < 32; ++i, p *= 2) if (p >= n) return i;
    stop("Cannot find log two for", n);
    return -1;
   }

  static int powerTwo(int n) {return 1 << n;}                                   // Power of 2
  static int powerOf (int a, int b)                                             // Raise a to the power b
   {int v = 1; for (int i = 0; i < b; ++i) v *= a; return v;
   }

//D1 Array routines                                                             // Routines operating on arrays

   static void reverseArray(Object[] array)                                     // Reverse an array in situ
    {final int N = array.length;
     for (int i = 0; i < N / 2; i++)
      {final Object temp = array[i];
       array[i] = array[N - 1 - i];
                  array[N - 1 - i] = temp;
      }
    }

   static void randomizeArray(Object[] array)                                   // Randomize an array
    {final Random random = new Random();
     for (int i = array.length - 1; i > 0; i--)
      {int j = random.nextInt(i + 1);
       Object temp = array[i];
                     array[i] = array[j];
                                array[j] = temp;
      }
    }

//D1 Logging                                                                    // Logging and tracing

//D2 Traceback                                                                  // Trace back so we know where we are

  static String fullTraceBack(Exception e)                                      // Get a full stack trace that we can use in Geany
   {final StackTraceElement[]  t = e.getStackTrace();
    final StringBuilder        b = new StringBuilder();
    if (e.getMessage() != null)b.append(e.getMessage()+'\n');

    for(StackTraceElement s : t)
     {final String f = s.getFileName();
      final String c = s.getClassName();
      final String m = s.getMethodName();
      final String l = String.format("%04d", s.getLineNumber());
      b.append("  "+f+":"+l+":"+m+'\n');
     }
    return b.toString();
   }

  static String traceBack(Exception e)                                          // Get a stack trace that we can use in Geany
   {final int Skip = 2;
    final StackTraceElement[]  t = e.getStackTrace();
    final StringBuilder        b = new StringBuilder();
    if (e.getMessage() != null)b.append(e.getMessage()+'\n');

    int skipped = 0;
    for(StackTraceElement s : t)
     {final String f = s.getFileName();
      final String c = s.getClassName();
      final String m = s.getMethodName();
      final String l = String.format("%04d", s.getLineNumber());
      if (f.equals("Main.java") || f.equals("Method.java") || f.equals("DirectMethodHandleAccessor.java")) {}
      else if (skipped < Skip) ++skipped;
      else b.append("  "+f+":"+l+":"+m+'\n');
     }
    return b.toString();
   }

  static String traceBack() {return traceBack(new Exception());}                // Get a stack trace that we can use in Geany

  static String traceDdd()                                                      // Locate line associated with a say statement
   {final StackTraceElement[]  t = new Exception().getStackTrace();
    for(int i = 0; i < t.length; ++i)
     {if (t[i].getMethodName().equals("ddd"))
       {final StackTraceElement s = t[i+1];
        final String f = s.getFileName();
        final String m = s.getMethodName();
        final String l = String.format("%04d", s.getLineNumber());
        return f+":"+l+":";
       }
     }
    return "";
   }

  static String currentTestName()                                               // Name of the current test
   {final StackTraceElement[] T = Thread.currentThread().getStackTrace();       // Current stack trace
    for (StackTraceElement t : T)                                               // Locate deepest method that starts with test
     {final String c = t.getMethodName();
      if (c.matches("\\Atest_.*\\Z")) return c;
     }
    return null;                                                                // Not called in a test
   }

  static String currentTestNameSuffix()                                         // Name of the current test
   {final String t = currentTestName();
    if (t == null) return null;
    final String[]s = t.split("_", 2);
    if (s.length < 2) return null;
    return s[1];
   }

  static String currentCallerName()                                             // Looks for the first method written in camel case
   {final StackTraceElement[] T = Thread.currentThread().getStackTrace();       // Current stack trace
    for (StackTraceElement t : T)                                               // Locate deepest method with a name written in camel case
     {final String c = t.getMethodName();
      if (c.matches("\\A.*_.*\\Z")) return c;
     }
    return null;                                                                // No method written in camel case
   }

  static String sourceFileName()                                                // Name of source file calling this method
   {final StackTraceElement e = Thread.currentThread().getStackTrace()[2];      // 0 is getStackTrace, 1 is this routine, 2 is calling method
    return e.getFileName();
   }

  static String callerFileAndLine2()                                            // Locate file and line number of caller of caller
   {final StackTraceElement[] t = new Exception().getStackTrace();
    if (t.length < 3) return null;
    final StackTraceElement s = t[2];
    final String f = s.getFileName();
    final String m = s.getMethodName();
    final String l = String.format("%04d", s.getLineNumber());
    return f+" "+m+" "+l;
   }

//D2 Coverage                                                                   // Analyze code coverage

  static final TreeMap<String, Integer> coverage = new TreeMap<>();             // Count of how many times each line has been executed

  static void z()                                                               // A line that is being executed
   {final String s = callerFileAndLine2();                                      // File method line
    Integer c = coverage.get(s);
    coverage.put(s, c == null ? 1 : c+1);
   }

  static class LineCount                                                        // Line count
   {final String line;
    final int count;
    LineCount(String Line, int Count) {line = Line; count = Count;}
   }

  static void printMostExecuted(Stack<LineCount> stack, String line, int n)     // Print a most frequently executed subroutine as in: 9843 Btree.java leafSize 0124
   {final String[]s = line.split("\\s");
    stack.push(new LineCount(String.format("%s:%s:%s", s[0], s[2], s[1]), n));
   }

  static void coverageAnalysis(String source, int top)                          // Coverage analysis: unexecuted lines and lines most frequently executed in a Geany clickable format
   {final Stack<String> sourceLines = readFile(source);                         // Lines of source from indicated file
    final Stack<String> notExecuted = new Stack<>();                            // Lines not executed
    final TreeSet<Integer> executed = new TreeSet<>();                          // Lines executed

    for (String s : coverage.keySet())                                          // Find lines executed in this file
     {final String[]fml = s.split("\\s+");
      if (fml[0].equals(source)) executed.add(Integer.parseInt(fml[2]));
     }

    for (int i = 1; i <= sourceLines.size(); i++)                               // Lines not executed in this file
     {final String line = sourceLines.elementAt(i-1);
      if (line.contains("z();"))                                                // Line has been marked as executable
       {if (!executed.contains(i))                                              // Line has not been executed
         {notExecuted.push(""+source+":"+i+":");
         }
       }
     }
    if (notExecuted.size() > 0)                                                 // Lines not executed
     {say("Not executed");
      for (int i = 1; i <= notExecuted.size(); i++)                             // Not executed lines as a table
       {say(notExecuted.elementAt(i-1));
       }
     }
    else say("All lines executed");                                             // All lines were executed

    if (top > 0)                                                                // Most frequently executed
     {final Stack<LineCount> lc = new Stack<>();                                // Lines executed most frequqntly
      coverage.entrySet().stream()                                              // Find most frequently executed lines
       .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))              // Sort by value in descending order
       .limit(top)                                                              // Take the most frequent elements
       .forEach(e -> printMostExecuted(lc, e.getKey(), e.getValue()));          // Print each entry

      int w = 1; for (LineCount l: lc) w = max(w, l.line.length());             // Maximum width of line executed specification
      final String f = "%-" + w + "s";
      say(String.format(f+"  %6s  %8s", "Most Executed", "Count", "#"));

      final int N = min(w, lc.size());
      for (int i = 1; i <= N; i++)                                              // Print lines executed most frequently
       {final LineCount l = lc.elementAt(i-1);
        final String    c = NumberFormat.getInstance().format(l.count);
        say(String.format(f+"  %6d  %8s", l.line, c, i));
       }
     }
   }

  static Stack<String> readFile(String filePath)                                // Read a file into stack of strings
   {try
     {final Stack<String> S = new Stack<>();
      for(String s:  Files.readAllLines(Paths.get(filePath))) S.push(s);
      return S;
     }
    catch (IOException e)
     {stop("Cannot read file", filePath);
     }
    return null;
   }

//D2 Timing                                                                     // Print log messages

  static class Timer                                                            // Time a section of code
   {final long start = System.nanoTime();
    public String toString()
     {return String.format("%6.2f %s", seconds(), currentCallerName());
     }
    double seconds()
     {final long duration = System.nanoTime() - start;
      return duration / 1e9;
     }
   }

  static Timer timer() {return new Timer();}                                    // Create a new timer

//D2 Printing                                                                   // Print log messages

  static void sayf(String format, Object...O)                                   // Say something under the control of a format string
   {System.err.println(String.format(format, O));
    return;
   }

  static void say(Object...O)                                                   // Say something
   {final StringBuilder b = new StringBuilder();                                // Print as a series of whitespace separated items
    for (int i = 0; i <  O.length; ++i)
     {final Object o = O[i];
      if (o == null) {b.append("(null)"); continue;}
      final String s = o.toString();

      if (b.length() > 0 && s.length() > 0 && s.charAt(s.length()-1) == '\n')   // Print a string that has a new line at the end indocating it is vertially aligned
       {b.append("\n"+s);
       }

      else if (b.length() > 0 && s.length() > 0           &&                    // Offset the next item from the previous item with a space unless a space has been provided
        !Character.isWhitespace(b.charAt(b.length() - 1)) &&
        !Character.isWhitespace(s.charAt(0))) b.append(" "+s);
      else b.append(s);
     }

    if (sayThisOrStop.size() > 0)                                               // Convert the say into a stop if the expected message does not eventuate
     {final String act = b.toString() .replace("\n", "\\n").trim();             // Message we actually got
      final String exp = sayThisOrStop.removeFirst().replace("\n", "\\n").trim();// Message we expected
      if (!exp.equals(act))                                                     // Expected message does not match what we have got
       {stop("Actual message does not equal expected message:\n"+
          "Actual  :"+act+" length("+act.length()+")\n"+
          "Expected:"+exp+" length("+exp.length()+")\n");
       }
     }

    else if (b.length() > 0) System.err.println(b.toString());
   }

  static StringBuilder say(StringBuilder b, Object...O)                         // Say something in a string builder
   {for (int i = 0; i < O.length; i++)
     {if (i > 0) b.append(" ");
      b.append(O[i]);
     }
    b.append('\n');
    return b;
   }

  static void ddd(Object...O)                                                   // Debug something
   {final int W = 10;                                                           // Width of traceback
    if (O.length == 0)
     {System.err.println(traceDdd());                                           // Nothing to say
      return;
     }

    final StringBuilder b = new StringBuilder();                                // Concatenate objects as strings
    for (int i = 0; i < O.length; i++)
     {final Object o = O[i];
      if (i > 0) b.append(' ');
      b.append(o);
     }
    while (b.length() > 0)                                                      // Remove trailing white space
     {final int  l = b.length();
      final char c = b.charAt(l-1);
      if (!Character.isWhitespace(c)) break;
      b.setLength(l - 1);
     }

    StringBuilder p = new StringBuilder(traceDdd());                            // Create traceback line prefix
    p.append(" ".repeat(W - p.length() % W));
    final int     w = p.length();
    final String[]s = b.toString().split("\n");                                 // Print first line with line position and message in a format understood by Geany with a re of: ([a-zA-Z0-9./]):(\d+)
    System.err.println(p.toString()+" "+s[0]);

    for (int i=1; i < s.length; i++) System.err.println(" ".repeat(w)+" "+s[i]);// Any following lines are indented to match the first line
   }

  static void err(Object...O)                                                   // Say something and provide an error trace.
   {final boolean testing = sayThisOrStop.size() > 0;                           // We are testing something that would normally stop the system
    say(O);
    if (!testing) System.err.println(traceBack());
   }

  static void stop(Object...O)                                                  // Say something, provide an error trace and stop
   {final boolean sos = sayThisOrStop.size() > 0;                               // Say or stop checking in effect
    say(O);
    if (sos)
     {throw new RuntimeException("Stopping after say an error message");
     }
    else
     {System.err.println(traceBack());
      System.exit(1);
     }
   }

  static void sayThisOrStop(Object...O)                                         // The next things to be said
   {sayThisOrStop.clear();
    for (Object o : O) sayThisOrStop.push(o.toString());
   }

//D1 Testing                                                                    // Test expected output against got output

  static int testsPassed = 0, testsFailed = 0;                                  // Number of tests passed and failed

  static void ok(boolean b)                                                     // Check test results match expected results.
   {if (b) {++testsPassed; return;}
    testsFailed++;
    err(currentTestName(), "failed\n");
   }

  static void ok(Object a, Object b)                                            // Check test results match expected results.
   {if (a.toString().equals(b.toString())) {++testsPassed; return;}
    final boolean n = b.toString().contains("\n");
    testsFailed++;
    if (n) err(currentTestName(), "Failed, got:\n"+a+"\n");
    else   err(a, "\ndoes not equal\n", b, "\nin", currentTestName());
   }

  static void ok(String got, String expected)                                   // Confirm two strings match
   {final String G = got, E = expected;
    final int lg = G.length(), le = E.length();
    final StringBuilder b = new StringBuilder();

    boolean matchesLen = true, matches = true;
    if (le != lg)                                                               // Failed on length
     {matchesLen = false;
      err(b, currentTestName(), "Failed: mismatched length, expected",
        le, "got", lg, "for text:\n"+G);

//    for (int i = 0; i < G.length(); i++)                                      // Check each character side by side
//     {final int  g = G.charAt(i);
//      final int  e = i < E.length() ? E.charAt(i) : ' ';
//      final char c =                  G.charAt(i);
//      say(i, g, e, c);
//     }
     }

    int l = 1, c = 0;
    final int N = le < lg ? le : lg;
    for (int i = 0; i < N && matches; i++)                                      // Check each character
     {final int  e = E.charAt(i), g = G.charAt(i);
      if (e != g)                                                               // Character mismatch
       {final String ee = e == '\n' ? "new-line" : ""+(char)e;
        final String gg = g == '\n' ? "new-line" : ""+(char)g;

        say(b, "Character "+c+", expected="+ee+"= got="+gg+"=");
        say(b, "0----+----1----+----2----+----3----+----4----+----5----+----6");
        final String[]t = G.split("\\n");
        for(int k = 0; k < t.length; k++)                                       // Details of  mismatch
         {say(b, t[k]);
          if (l == k+1) say(b, " ".repeat(c)+'^');
         }
        matches = false;
       }
      if (e == '\n') {++l; c = 0;} else c++;
     }

    if (matchesLen && matches) ++testsPassed; else {++testsFailed; err(b);}     // Show error location with a trace back so we know where the failure occurred
   }

  static void ok(Integer G, Integer E)                                          // Check that two integers are equal
   {if (false)                        {}
    else if ( G == null && E == null) ++testsPassed;
    else if ( G != null && E == null) {err(String.format("Expected null, got:", G)); ++testsFailed;}
    else if ( G == null && E != null) {err(String.format("Got null, expected:", E)); ++testsFailed;}
    else if (!G.equals(E))            {err(currentTestName(), G, "!=", E);           ++testsFailed;}
    else ++testsPassed;
   }

  static void ok(Long    G, Long    E)                                          // Check that two longs are equal
   {if (false)                        {}
    else if ( G == null && E == null) ++testsPassed;
    else if ( G != null && E == null) {err(String.format("Expected null, got:", G)); ++testsFailed;}
    else if ( G == null && E != null) {err(String.format("Got null, expected:", E)); ++testsFailed;}
    else if (!G.equals(E))            {err(currentTestName(), G, "!=", E);           ++testsFailed;}
    else ++testsPassed;
   }

  static void ok(Integer[]G, String E)                                          // Check that two integer arrays are are equal
   {ok(""+G, E);
   }

  static void ok(Integer[]G, Integer[]E)                                        // Check that two integer arrays are are equal
   {final StringBuilder b = new StringBuilder();
    final int lg = G.length, le = E.length;

    if (le != lg)
     {err(currentTestName(), "Failed:",
       "mismatched length, got", lg, "expected", le, "got:\n"+G);
      ++testsFailed;
      return;
     }

    int fails = 0, passes = 0;
    for (int i = 1; i <= lg; i++)
     {final Integer e = E[i-1], g = G[i-1];
      if (false)                       {}
      else if (e == null && g == null) {}
      else if (e != null && g == null) {b.append(String.format("Index %d expected %d, but got null\n", i, e   )); ++fails;}
      else if (e == null && g != null) {b.append(String.format("Index %d expected null, but got %d\n", i, g   )); ++fails;}
      else if (!e.equals(g))           {b.append(String.format("Index %d expected %d, but got %d\n",   i, e, g)); ++fails;}
      else ++passes;
     }
    if (fails > 0) err(b);
    testsPassed += passes; testsFailed += fails;                                // Passes and fails
   }

  static void testSummary()                                                     // Print a summary of the testing
   {final double delta = (System.nanoTime() - start) / (double)(1<<30);         // Run time in seconds
    final String d = String.format("tests in %5.2f seconds.", delta);           // Format run time
    if (false) {}                                                               // Analyze results of tests
    else if (testsPassed == 0 && testsFailed == 0) say("No",    d);             // No tests
    else if (testsFailed == 0)   say("PASSed ALL", testsPassed, d);             // Passed all tests
    else say("Passed "+testsPassed+",    FAILed:", testsFailed, d);             // Failed some tests
    System.exit(testsFailed > 0 ? 1 : 0);                                       // Set the return code
   }

//D0                                                                            // Tests

  static void test_max_min()
   {ok(min(3,  2,  1),  1);
    ok(max(1,  2,  3),  3);
    ok(min(3d, 2d, 1d), 1d);
    ok(max(1d, 2d, 3d), 3d);
   }

  static void test_string()                                                     // Confirm an error message
   {String e = """
AAAAA
BBBBB
CCCCC
""";
    String g = """
AAAAA
BBDBB
CCCCC
""";


    sayThisOrStop("""
Character 2, expected=D= got=B=
0----+----1----+----2----+----3----+----4----+----5----+----6
AAAAA
BBBBB
  ^
CCCCC
""");

    ok(e, g); --testsFailed;
   }

  static void test_longest_line()
   {ok(longestLine(""),      0);
    ok(longestLine("A"),     1);
    ok(longestLine("\n"),    1);
    ok(longestLine("\n\n"),  1);
    ok(longestLine("\nA\n"), 2);
    ok(longestLine("A\nBBB\nCC\n"), 4);
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_max_min();
    test_string();
    test_longest_line();
   }

  static void newTests()                                                        // Tests being worked on
   {oldTests();
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
      System.exit(testsFailed);
     }
   }
 }
