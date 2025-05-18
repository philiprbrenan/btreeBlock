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
    m.program(Program, false);                                                  // Set program for this variable assuming it is not a unique name
   }

  Variable(MemoryLayoutDM.At at)                                                // Create a variable with the same anme and wiith but in its own memeory
   {this(at.ml().P, at.field.name, at.field.width);
   }

  Variable fork() {final Variable r = new Variable(a); r.move(this); return r;} // Duplicate the definition of a variable during compilation and copy its value during execution
  void move(Variable source)        {a.move(source.a);}                         // Copy the value of the source variable into the target during program execution

  void seti(int v) {       a.setInt(v);}                                        // Set the value of the variable immediately
  int  geti()      {return a.locateDirectAddress().getInt();}                   // Get the value of the variable immediately. A variable cannot be indirectly addressed.

  void inc () {a.inc();}                                                        // Increment the variable
  void dec () {a.dec();}                                                        // Decrement the variable

  void srz () {a.srz();}                                                        // Shift right filling with zero
  void slz () {a.slz();}                                                        // Shift left  filling with zero

  Variable copy()           {final Variable r = new Variable(program, "result", a.field.width); r.a.move(a);     return r;} // Create a copy of a variable
  Variable add (int      A) {final Variable r = new Variable(program, "result", a.field.width); r.a.add(a, A);   return r;} // Add a constant to a variable
  Variable add (Variable A) {final Variable r = new Variable(program, "result", a.field.width); r.a.add(a, A.a); return r;} // Add two variables and place the result in this variable

  Variable equal             (Variable A) {final Variable r = new Variable(program, "result", 1); a.equal             (A.a, r.a); return r;} // Set this variable to one if the specified variables are equal else zero
  Variable notEquals         (Variable A) {final Variable r = new Variable(program, "result", 1); a.notEqual          (A.a, r.a); return r;} // Set this variable to one if the specified variables are not equal else zero
  Variable lessThan          (Variable A) {final Variable r = new Variable(program, "result", 1); a.lessThan          (A.a, r.a); return r;} // Set this variable to one if the first variable is less than the second one else zero
  Variable lessThanOrEqual   (Variable A) {final Variable r = new Variable(program, "result", 1); a.lessThanOrEqual   (A.a, r.a); return r;} // Set this variable to one if the first variable is less than or equal to the second one else zero
  Variable greaterThan       (Variable A) {final Variable r = new Variable(program, "result", 1); a.greaterThan       (A.a, r.a); return r;} // Set this variable to one if the first variable is greater than the second one else zero
  Variable greaterThanOrEqual(Variable A) {final Variable r = new Variable(program, "result", 1); a.greaterThanOrEqual(A.a, r.a); return r;} // Set this variable to one if the first variable is greater than or equal to the second one else zero

  void zero() {a.zero();}                                                       // Zero the storage associated with this variable
  void one () {a.one ();}                                                       // Set the memory associated with this variable to one
  void ones() {a.ones();}                                                       // Set the memory associated wiith this variable to all ones

  String verilogLoad() {return a.verilogLoad();}                                // Verilog value   of this variable
  String verilogAddr() {return a.verilogAddr();}                                // Verilog address of this variable

  void sameSize(Variable...A)                                                   // Stop unless all the variables have the same size
   {for (int i = 0; i < A.length; i++) a.sameSize(A[i].a);
   }

  public String toString() {return a.setOff().toString();}                      // String to show variable name and value

//D0 Tests                                                                      // Testing

  static void test_var()
   {ProgramDM p = new ProgramDM();
    Variable  i = new Variable(p, "i", 4);
    Variable  j = new Variable(p, "j", 4);
    Variable  k = new Variable(p, "k", 4);

    i.sameSize(j, k);
    i.seti(1);
    j.move(i);
    p.run(); p.clear();
    ok(j.geti(), 1);
    j.inc();
    p.run();  p.clear();
    ok(j.geti(), 2);

    k = i.add(j);
    p.run();  p.clear();
    ok(k.geti(), 3);

    k = j.add(2);
    p.run();  p.clear();
    ok(k.geti(), 4);
   }

  static void test_boolean()
   {final ProgramDM p   = new ProgramDM();
    final Variable  i   = new Variable(p, "i",  4);
    final Variable  j   = new Variable(p, "j",  4);

    i.seti(1); j.seti(2);

    final Variable eq1 = i.equal             (i);
    final Variable ne1 = i.notEquals         (j);
    final Variable lt1 = i.lessThan          (j);
    final Variable le1 = i.lessThanOrEqual   (j);
    final Variable gt1 = j.greaterThan       (i);
    final Variable ge1 = j.greaterThanOrEqual(i);
    final Variable eq0 = i.equal             (j);
    final Variable ne0 = i.notEquals         (i);
    final Variable lt0 = i.lessThan          (i);
    final Variable le0 = j.lessThanOrEqual   (i);
    final Variable ge0 = i.greaterThan       (i);
    final Variable gt0 = i.greaterThanOrEqual(j);

    p.run(); p.clear();

    ok(eq1.geti(), 1);
    ok(ne1.geti(), 1);
    ok(le1.geti(), 1);
    ok(gt1.geti(), 1);
    ok(ge1.geti(), 1);

    ok(eq0.geti(), 0);
    ok(ne0.geti(), 0);
    ok(lt0.geti(), 0);
    ok(le0.geti(), 0);
    ok(gt0.geti(), 0);
    ok(ge0.geti(), 0);
   }

  static void test_shift()                                                      // Shift left and right
   {final ProgramDM p = new ProgramDM();
    final Variable  i = new Variable(p, "i",  4);
    i.seti(1);

    i.slz();i.slz();
    p.run(); p.clear();
    //stop(i);
    ok(i.geti(), 4);

    i.srz();
    p.run(); p.clear();
    //stop(i);
    ok(i.geti(), 2);
   }

  static void test_verilog()
   {final ProgramDM p = new ProgramDM();
    MemoryLayoutDM.numbers = 0;
    final Variable  i = new Variable(p, "i",  4);
    i.seti(1);

    ok(i.verilogLoad(), "i_1[       0/*i       */ +: 4]");
   }

  static void test_location()
   {z();
    final int M = 4;
    Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", M);
    Layout.Variable  b = l.variable ("b", M);
    Layout.Variable  c = l.variable ("c", M);
    Layout.Variable  d = l.variable ("d", M);
    Layout.Variable  o = l.variable ("o", M);
    Layout.Variable  t = l.variable ("t", M);
    Layout.Structure S = l.structure("A", a, b, c, d, o, t);
    MemoryLayoutDM   m = new MemoryLayoutDM(l.compile(), "fields");
    m.program(m.P);

    final Variable A   = new Variable(m.at(a));
    final Variable D   = new Variable(m.at(d));
    final Variable One = new Variable(m.at(o));
    final Variable Two = new Variable(m.at(t));

    A.seti(1); One.seti(1); Two.seti(2);
    Variable x = A.copy();
             x.inc();
    Variable y = A.add(x);
    m.P.run(); m.P.clear();
    //stop(B);
    ok(x.geti(), 2);
    //stop(C);
    ok(y.geti(), 3);

    m.P.clear();
    m.P.new If (One.greaterThanOrEqual(y))
     {void Then()
       {D.move(One);
       }
      void Else()
       {D.move(Two);
       }
     };
    m.P.run();
    //stop(D);
    ok(D.geti(), 2);
   }

  static void test_zero()
   {z();
    Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 4);
    MemoryLayoutDM   m = new MemoryLayoutDM(l.compile(), "fields");
    m.program(m.P);

    final Variable A   = new Variable(m.at(a));

    A.seti(1);
    ok(A.geti(), 1);
    A.zero();
    ok(A.geti(), 1);
    m.P.run(); m.P.clear();
    ok(A.geti(), 0);
   }

  static void test_dup()
   {z();
    Layout           l = Layout.layout();
    Layout.Variable  a = l.variable ("a", 4);
    Layout.Variable  b = l.variable ("b", 4);
    Layout.Structure S = l.structure("S", a, b);
    MemoryLayoutDM   m = new MemoryLayoutDM(l.compile(), "fields");
    m.program(m.P);
    m.at(a).setInt(12);
    m.at(b).setInt(13);

    //stop(m);
    ok(""+m, """
MemoryLayout: fields
Memory      : fields
Line T       At      Wide       Size    Indices        Value   Name
   1 S        0         8                                      S
   2 V        0         4                                 12     a
   3 V        4         4                                 13     b
""");

    final Variable A = new Variable(m.at(a));                                   // Same definition but different memory
    final Variable B = new Variable(m.at(b));

    ok(A.geti(), 0);
    ok(B.geti(), 0);

    A.seti(1);
    B.seti(2);
    A.move(B);
    ok(A.geti(), 1);
    ok(B.geti(), 2);
    m.P.run(); m.P.clear();
    ok(A.geti(), 2);
    ok(B.geti(), 2);

    B.zero();
    ok(B.geti(), 2);
    m.P.run(); m.P.clear();
    ok(B.geti(), 0);

    B.one();
    ok(B.geti(), 0);
    m.P.run(); m.P.clear();
    ok(B.geti(), 1);

    B.ones();
    ok(B.geti(), 1);
    m.P.run(); m.P.clear();
    ok(B.geti(), 15);
   }

  static void test_fork()
   {z();
    final ProgramDM p = new ProgramDM();
    final Variable  a = new Variable(p, "a", 4);
    final Variable  A = a.fork();
    a.seti(1);
    ok(a.geti(), 1);
    ok(A.geti(), 0);
    p.run();
    ok(A.geti(), 1);
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_var();
    test_boolean();
    test_shift();
    test_verilog();
    test_location();
    test_zero();
    test_dup();
    test_fork();
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
