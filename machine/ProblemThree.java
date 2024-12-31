public class ProblemThree                                                       // Bit pattern search by Briana Ashley Nevarez 01/17/2022 fom CSE 141L - Winter 2022 P#3
 {static int COUNT = 0;
  int [] D = new int[256];

  public void initialize()                                                      // Initialize the machine
   {for(int i = 0; i < D.length; i++) D[i] = 0;                                 // Zero memory
   }

  public void problemThree()                                                    // bitsToLookFor = d
   {int a = 0, b = 0, c = 0, d = 0, e = 0, f = 0, g = 0b11111,                  // g is the mask
        h = 0, i = 0, j = 0, k = 0, l = 0, m = 0, n = 0, o = 0, p = 0;

    d = D[160];
    d >>= 3;                                                                    // Put needle into positions 4:0

    for(i = 159; i >= 0; i--)                                                   // Finding # of matches
     {n = 0;
      f = 0;
      h = i;
      h = D[h];
      f = h;
      f &= g;
      k = f == d ? 1 : 0;

      if (k != 0)
       {n++;
       }

      for(int z = 0; z < 3; z++)                                                // Java
       {h >>= 1;
        f = h;
        f &= g;
        k = f == d ? 1 : 0;

        if (k != 0)
         {n++;
         }
       }

      a += n;                                                                   // Total number of matches from mem[128:159]
      if (n != 0)
       {b++;
       }                                                                        // Number of ints with at least a match

      j = i;
      j = i < 159 ? 1 : 0;

      if (j != 0)
       {m = 0;
        e = i;
        e ++;
        e = D[e];                                                               // Combining two ints at a time to make a word
        e <<= 8;

        f = i;
        f = D[f];
        e |= f;
        f = e;
        f &= g;

        k = f == d ? 1 : 0;
        if (k != 0)
         {m++;
         }

        for(int q = 0; q < 11; q++)                                             // Java
         {e >>= 1;
          f  = e;
          f &= g;
          k = f == d ? 1 : 0;

          if (k != 0) m++;
         }

        c += m;
       }

      k = i < 159 ? 1 : 0;

      if   (k != 0)
       {k = i > 128 ? 1 : 0;

        if (k != 0)
         {c -= n;
         }
       }

//    say(String.format("%2d: %3d  %3d  %3d  %3d  %3d  %3d  %3d  %3d  %3d  %3d  %3d  %3d  %3d  %3d  %3d  %3d", i, a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p));

      k = i > 128 ? 1 : 0;
      if (k != 0)
       {continue;
       }

      break;
     }

    D[192] = a;                                                                 // Storing Part A answer
    D[193] = b;                                                                 // Storing Part B answer
    D[194] = c;                                                                 // Storing Part C answer
   }

  public void test(int needle, int A, int B, int C){
      initialize();                                                             // Initialize the machine

      D[128] =  35;                                                             // See Diagram 2
      D[129] = 102;
      D[130] =   0;
      D[131] = 192;                                                             // Part A - # of occurrences in every int
      D[160] = needle << 3;                                                     // Needle is in 7:3

      problemThree();
      assert(D[192] == A);
      assert(D[193] == B);
      assert(D[194] == C);
      assert(D[194] >= D[192]);
      COUNT++;
  }

  public static void say(Object...O)                                            // Say something
   {final StringBuilder b = new StringBuilder();
    for(Object o: O) b.append(o);
    System.err.print(b.toString()+"\n");
   }

  public static void main(String args[]){
      final ProblemThree i = new ProblemThree();
      i.test(0b00110, 1, 1, 3);
      i.test(0b00000, 118, 30, 231);
      System.out.println("Passes: "+ COUNT + " Tests");
  }
}
