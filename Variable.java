//------------------------------------------------------------------------------
// Variable held in a memory layout
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2025
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Btree in a block on the surface of a silicon chip.

class Variable extends Test                                                     // Variable
 {final ProgramDM   program;                                                    // Program the variable appears in
  final String         name;                                                    // Name of variable
  final int            bits;                                                    // Width of variable in bits
  final Layout            l;                                                    // Layout of variable
  final Layout.Variable   v;                                                    // Variable
  final MemoryLayoutDM    m;                                                    // Memory layout associated with variable
  final MemoryLayoutDM.At a;                                                    // Memory locatioin in memory

  Variable(ProgramDM Program, String Name, int Bits)                            // The size and name of the variable
   {program = Program; name = Name; bits = Bits;
    l = new Layout();
    v = l.variable(name, bits);
    m = new MemoryLayoutDM(l.compile(), name);
    a = m.top();
    m.program(Program);                                                         // Set program for this variable
   }

  void set(int v) {       a.setInt(v);}                                         // Set the value of the variable
  int  get()      {return a.getInt();}                                          // Get the value of the variable
  void inc ()     {a.inc();}                                                    // Increment the variable
  void dec ()     {a.dec();}                                                    // Decrement the variable
  void copy(Variable source)        {a.move(source.a);}                         // Copy the value of the source variable ino the target
  void add (Variable A, int      B) {a.add(A.a, B);}                            // Add a constant to a variable
  void add (Variable A, Variable B) {a.add(A.a, B.a);}                          // Add two variables and place the result in this variable

  void srz () {a.srz();}                                                        // Shift right filling with zero
  void slz () {a.slz();}                                                        // Shift left  filling with zero

  void equal             (Variable A, Variable B) {A.a.equal             (B.a, a);} // Set this variable to one if the specified variables are equal else zero
  void notEquals         (Variable A, Variable B) {A.a.notEqual          (B.a, a);} // Set this variable to one if the specified variables are not equal else zero
  void lessThan          (Variable A, Variable B) {A.a.lessThan          (B.a, a);} // Set this variable to one if the first variable is less than the second one else zero
  void lessThanOrEqual   (Variable A, Variable B) {A.a.lessThanOrEqual   (B.a, a);} // Set this variable to one if the first variable is less than or equal to the second one else zero
  void greaterThan       (Variable A, Variable B) {A.a.greaterThan       (B.a, a);} // Set this variable to one if the first variable is greater than the second one else zero
  void greaterThanOrEqual(Variable A, Variable B) {A.a.greaterThanOrEqual(B.a, a);} // Set this variable to one if the first variable is greater than or equal to the second one else zero

  public String toString() {return m.toString();}                               // String to show variable name and value

//D0 Tests                                                                      // Testing

  static void test_var()
   {final ProgramDM p = new ProgramDM();
    final Variable  i = new Variable(p, "i", 4);
    final Variable  j = new Variable(p, "j", 4);
    final Variable  k = new Variable(p, "k", 4);
    i.set(1);
    j.copy(i);
    p.run(); p.clear();
    ok(j, """
MemoryLayout: j
Memory      : j
Line T       At      Wide       Size    Indices        Value   Name
   1 V        0         4                                  1   j
""");
    j.inc();
    p.run();  p.clear();
    ok(j, """
MemoryLayout: j
Memory      : j
Line T       At      Wide       Size    Indices        Value   Name
   1 V        0         4                                  2   j
""");

    k.add(i, j);
    p.run();  p.clear();
    ok(k, """
MemoryLayout: k
Memory      : k
Line T       At      Wide       Size    Indices        Value   Name
   1 V        0         4                                  3   k
""");

    k.add(j, 2);
    p.run();  p.clear();
    ok(k, """
MemoryLayout: k
Memory      : k
Line T       At      Wide       Size    Indices        Value   Name
   1 V        0         4                                  4   k
""");
   }

  static void test_boolean()
   {final ProgramDM p   = new ProgramDM();
    final Variable  i   = new Variable(p, "i",  4);
    final Variable  j   = new Variable(p, "j",  4);
    final Variable  eq0 = new Variable(p, "eq0", 1);
    final Variable  ne0 = new Variable(p, "ne0", 1);
    final Variable  lt0 = new Variable(p, "lt0", 1);
    final Variable  le0 = new Variable(p, "le0", 1);
    final Variable  ge0 = new Variable(p, "ge0", 1);
    final Variable  gt0 = new Variable(p, "gt0", 1);
    final Variable  eq1 = new Variable(p, "eq1", 1);
    final Variable  ne1 = new Variable(p, "ne1", 1);
    final Variable  lt1 = new Variable(p, "lt1", 1);
    final Variable  le1 = new Variable(p, "le1", 1);
    final Variable  ge1 = new Variable(p, "ge1", 1);
    final Variable  gt1 = new Variable(p, "gt1", 1);
    i.set(1); j.set(2);
    eq1.equal             (i, i); eq0.equal             (i, j);
    ne1.notEquals         (i, j); ne0.notEquals         (i, i);
    lt1.lessThan          (i, j); lt0.lessThan          (i, i);
    le1.lessThanOrEqual   (i, j); le0.lessThanOrEqual   (j, i);
    ge1.greaterThan       (j, i); ge0.greaterThan       (i, i);
    gt1.greaterThanOrEqual(j, j); gt0.greaterThanOrEqual(i, j);
    p.run(); p.clear();

    //stop(eq0);
    //stop(ne0);
    //stop(lt0);
    //stop(le0);
    //stop(ge0);
    //stop(gt0);

    //stop(eq1);
    //stop(ne1);
    //stop(lt1);
    //stop(le1);
    //stop(ge1);
    //stop(gt1);

    ok(""+eq0, """
MemoryLayout: eq0
Memory      : eq0
Line T       At      Wide       Size    Indices        Value   Name
   1 V        0         1                                  0   eq0
""");

    ok(""+ne0, """
MemoryLayout: ne0
Memory      : ne0
Line T       At      Wide       Size    Indices        Value   Name
   1 V        0         1                                  0   ne0
""");

    ok(""+lt0, """
MemoryLayout: lt0
Memory      : lt0
Line T       At      Wide       Size    Indices        Value   Name
   1 V        0         1                                  0   lt0
""");

    ok(""+le0, """
MemoryLayout: le0
Memory      : le0
Line T       At      Wide       Size    Indices        Value   Name
   1 V        0         1                                  0   le0
""");

    ok(""+gt0, """
MemoryLayout: gt0
Memory      : gt0
Line T       At      Wide       Size    Indices        Value   Name
   1 V        0         1                                  0   gt0
""");

    ok(""+ge0, """
MemoryLayout: ge0
Memory      : ge0
Line T       At      Wide       Size    Indices        Value   Name
   1 V        0         1                                  0   ge0
""");


    ok(""+eq1, """
MemoryLayout: eq1
Memory      : eq1
Line T       At      Wide       Size    Indices        Value   Name
   1 V        0         1                                  1   eq1
""");

    ok(""+ne1, """
MemoryLayout: ne1
Memory      : ne1
Line T       At      Wide       Size    Indices        Value   Name
   1 V        0         1                                  1   ne1
""");

    ok(""+lt1, """
MemoryLayout: lt1
Memory      : lt1
Line T       At      Wide       Size    Indices        Value   Name
   1 V        0         1                                  1   lt1
""");

    ok(""+le1, """
MemoryLayout: le1
Memory      : le1
Line T       At      Wide       Size    Indices        Value   Name
   1 V        0         1                                  1   le1
""");

    ok(""+gt1, """
MemoryLayout: gt1
Memory      : gt1
Line T       At      Wide       Size    Indices        Value   Name
   1 V        0         1                                  1   gt1
""");

    ok(""+ge1, """
MemoryLayout: ge1
Memory      : ge1
Line T       At      Wide       Size    Indices        Value   Name
   1 V        0         1                                  1   ge1
""");

   }

  static void test_shift()                                                      // Shift left and right
   {final ProgramDM p   = new ProgramDM();
    final Variable  i   = new Variable(p, "i",  4);
    i.set(1);

    i.slz();i.slz();
    p.run(); p.clear();
    //stop(i);
    ok(""+i, """
MemoryLayout: i
Memory      : i
Line T       At      Wide       Size    Indices        Value   Name
   1 V        0         4                                  4   i
""");

    i.srz();
    p.run(); p.clear();
    //stop(i);
    ok(""+i, """
MemoryLayout: i
Memory      : i
Line T       At      Wide       Size    Indices        Value   Name
   1 V        0         4                                  2   i
""");
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_var();
    test_boolean();
    test_shift();
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
