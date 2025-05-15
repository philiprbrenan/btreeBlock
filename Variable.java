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

  Variable(MemoryLayoutDM.At at)                                                // Create a variable from a memory location
   {this(at.ml().P, at.field.name, at.field.width);
   }

  void set(int v) {       a.setInt(v);}                                         // Set the value of the variable
  int  get()      {return a.locateDirectAddress().getInt();}                    // Get the value of the variable. A variable cannot be in directly addressed.
  void inc ()     {a.inc();}                                                    // Increment the variable
  void dec ()     {a.dec();}                                                    // Decrement the variable
  void move(Variable source)        {a.move(source.a);}                         // Copy the value of the source variable ino the target

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
    i.set(1);
    j.move(i);
    p.run(); p.clear();
    ok(j.get(), 1);
    j.inc();
    p.run();  p.clear();
    ok(j.get(), 2);

    k = i.add(j);
    p.run();  p.clear();
    ok(k.get(), 3);

    k = j.add(2);
    p.run();  p.clear();
    ok(k.get(), 4);
   }

  static void test_boolean()
   {final ProgramDM p   = new ProgramDM();
    final Variable  i   = new Variable(p, "i",  4);
    final Variable  j   = new Variable(p, "j",  4);

    i.set(1); j.set(2);

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

    ok(eq1.get(), 1);
    ok(ne1.get(), 1);
    ok(le1.get(), 1);
    ok(gt1.get(), 1);
    ok(ge1.get(), 1);

    ok(eq0.get(), 0);
    ok(ne0.get(), 0);
    ok(lt0.get(), 0);
    ok(le0.get(), 0);
    ok(gt0.get(), 0);
    ok(ge0.get(), 0);
   }

  static void test_shift()                                                      // Shift left and right
   {final ProgramDM p = new ProgramDM();
    final Variable  i = new Variable(p, "i",  4);
    i.set(1);

    i.slz();i.slz();
    p.run(); p.clear();
    //stop(i);
    ok(i.get(), 4);

    i.srz();
    p.run(); p.clear();
    //stop(i);
    ok(i.get(), 2);
   }

  static void test_verilog()
   {final ProgramDM p = new ProgramDM();
    MemoryLayoutDM.numbers = 0;
    final Variable  i = new Variable(p, "i",  4);
    i.set(1);

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

    A.set(1); One.set(1); Two.set(2);
    Variable x = A.copy();
             x.inc();
    Variable y = A.add(x);
    m.P.run(); m.P.clear();
    //stop(B);
    ok(x.get(), 2);
    //stop(C);
    ok(y.get(), 3);

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
    ok(D.get(), 2);
 }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_var();
    test_boolean();
    test_shift();
    test_verilog();
    test_location();
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
