#!/usr/bin/perl
#-------------------------------------------------------------------------------
# Zip machine files
# Philip R Brenan at gmail dot com, Appa Apps Ltd Inc., 2021
#-------------------------------------------------------------------------------
use warnings FATAL => qw(all);
use strict;
use Carp;
use Data::Dump qw(dump);
use Data::Table::Text qw(:all);
use feature qw(say current_sub);

my $title = "<b>BAN&#169; Mk2 nine bit micro processor</b>";

my $home   = currentDirectory;                                                  # Home folder
my $z      = q(machine.zip);                                                    # Zip file
my $Z      = fpf($home, $z);

my $toPdf  = q(/usr/local/bin/wkhtmltopdf --enable-local-file-access);          # Convert to pdf

my $input  = fpe $home, qw(ISA html);                                           # Input file
my $output = fpe $home, qw(ban html);                                           # Output file
my $doc    = fpd $home, qw(doc);                                                # Documentation folder

#my $head = &head;
my $middle1 = &middle1;                                                         # Description
my $middle2 = &middle2;                                                         # ISA
my $foot = &foot;

my $text = readFile($input);
if ($text =~ m(\A(.*)(<h1\s+id=h80>High Level.*)\Z)s)                           # Reorder ISA.html
 {my $h = "$middle1\n$2\n$middle2\n$1\n$foot";
  owf $output, $h;
 }
else
 {confess;
 }

#push my @toc, <<END;                                                            # Index input file
#<table cellpadding=2>
#END
#
#for my $l(@head, @middle, @text, @foot)
# {if ($l =~ m(\A<h(\d)\s+id=(.*?)>(.*?)<))
#   {my $h = $1; my $r = $2; my $t = $3;
#    my $b = '&nbsp' x (16 * $h);
#    push @toc, "<tr><td>$b" if $h == 1;
#    push @toc, qq(<tr><td>$b<a href="#$r">$t</a>\n);
#   }
# }
#
#push @toc, <<END;
#</table>
#END

#owf($output, join('', @head, @toc, @middle, @text, @foot) =~ s(s.png) (.png)gsr);

if (1)                                                                          # Pdf
 {my $h = owf(fpe($doc, qw(head html)),   &head);

  my $p = fpe $home, qw(ban pdf);
  say STDERR qx($toPdf cover $h toc $output $p);

  my $b = readBinaryFile $p;
     $b =~ s(phil) (ashley)gs;
     $b =~ s(people/ashley/) ()gs;
     $b =~ m(phil) and confess "Has phil";
  unlink $p;
  writeBinaryFile $p, $b;
 }

# Cut out programs 1 to 3  in high and low level versions

if (1)
 {my @h = readFile $input;
  my @p; my $p; my $hl;
  for my $l(@h)                                                                 #
   {if (!$p)
     {if ($l =~ m(\A<!-- (\w\w) (\w+) -->\Z))
       {$hl = $1; $p = $2;
       }
     }
    else
     {if ($l =~ m(\A</pre>\Z))
       {shift @p;
        owf(fpe($home, q(assemblerPrograms), ($hl eq "ll" ? q(lowLevel) : q(highLevel)), $p, q(txt)), join "", @p);
        $p = undef; @p = (); $hl = undef;
       }
      else
       {push @p, $l;
       }
     }
   }
 }

# Zip

unlink $Z;

lll qx(zip $z Machine3.java ban.pdf cpu.jpg assemblerPrograms/*/* verilog/run.sh verilog/cpu.sv verilog/cpu_tb.sv verilog/images/*s.png; ls -la $Z);

sub head {<<END}
<!DOCTYPE html>
<html>
<header>
  <meta charset="ISO-8859-1">
<style>
body{margin: 1% 10% 1% 10%}
img {width: 80vw}
pre {font-family: monospace}
</style>
</header>
<body>
<h1>The $title</h1>

<h3>Copyright&#169; 2022 Briana Ashley Nevarez. All rights reserved. Patents pending.</h3>

<p>This document describes the implementation of the

$title

 for class: CSE141L - Term Project Winter 2022.</p>

<p>The

$title

 is a world class, patent pending, full function, net generation,
high speed computational device implemented in

<b>10nm</b>

 germanium nitride

<b>GeN&#x2083;</b>

 suitable for wide application in environments where radiation
resistance and fault tolerance are highly desirable.

<p>The

$title

 was first modeled and executed in

<b>20nm</b>

 Java to confirm its correct operation.  The

<b>20nm</b>

  Java version was then used to derive a

<b>10nm</b>

System Verilog version which was synthesised and shown to produce identical
results. The relevant Java and System Verilog code can be seen below.

<p>The documentation below also contains the high level instruction
architecture, System Verilog program execution results and screen shots of processor
wave forms.</p>.
END

sub convertToHtml($)
 {my ($f) = @_;
  my $s = readFile($f);
  $s =~ s(<) (&lt;)gs;
  $s =~ s(>) (&gt;)gs;
  $s
 }

sub resizeImage($)
 {my ($s) = @_;
  my $t = $s =~ s(\.png\Z) (s.png)r;
  say STDERR qx(convert $s -resize 50%  $t);
  $t
 }

sub resizeJpg($)
 {my ($s) = @_;
  my $t = $s =~ s(\.jpg\Z) (s.jpg)r;
  say STDERR qx(convert $s -resize 75%  $t);
  $t
 }

sub resizeImage3($)
 {my ($s) = @_;
  my $t = $s =~ s(\.png\Z) (s.png)r;
  say STDERR qx(convert $s -resize 33%  $t);
  $t
 }

sub middle1                                                                     # Architecture
 {my $s       = resizeJpg    ("cpu.jpg");
  my $syn     = resizeImage  ("verilog/images/synthesis.png");
  my $tb      = convertToHtml("verilog/cpu_tb.sv");
  my $trace   = convertToHtml("verilog/cpu.txt");

  <<END,

<h1 id=h2029>Synthesis</h1>

<p>Successful synthesis on <a href="https://en.wikipedia.org/wiki/Intel_Quartus_Prime">Intel QuartusPrime</a> of the
 $title

demonstrating that the
implementation is fully parallel and capable of being programmed into an <a
href="https://en.wikipedia.org/wiki/Field-programmable_gate_array">fpga</a>.

<p><img src="$syn">

<h1 id=h2022>Simplified Schematic Design</h1>

<p>Here is the schematic high level layout of the

$title

as successfully synthesized on Quartus:

<p><img src="$s">

<h1 id=h1027>Packing List</h1>

<p>You should have received the following files:
<p><table cellspacing=10 border=0>
<tr><th>File                       <th>Contents
<tr><td><a href="https://github.com/banevare/ban/ban.pdf"                     >ban.pdf                     </a><td>This file containing complete schematics, implementation details and test execution results
<tr><td><a href="https://github.com/banevare/ban/cpu.jpg"                     >cpu.jpg                     </a><td>Schematic diagram of $title
<tr><td><a href="https://github.com/banevare/ban/Machine3.java"               >Machine3.java               </a><td>The java source code used to assemble, emulate and test the requested programs.
<tr><td><a href="https://github.com/banevare/ban/verilog/cpu.sv"              >verilog/cpu.sv              </a><td>System verilog implementation of the cpu.
<tr><td><a href="https://github.com/banevare/ban/verilog/cpu_tb.sv"           >verilog/cpu_tb.sv           </a><td>System verilog test bench to test the implementation of the cpu
<tr><td><a href="https://github.com/banevare/ban/verilog/images/synthesis.png">verilog/images/synthesis.png</a><td>Synthesis with Quartus
<tr><td><a href="https://github.com/banevare/ban/verilog/images/*"            >verilog/images/*            </a><td>Wave form images from test executions after synthesis on Quartus.
</table>

<h1 id=h1028>Change Log</h1>

<p>Updates to this document:
<p><table cellspacing=10 border=0>
<tr><th>Date                      <th>Change
<tr><td>2022-03-05<td>Separated test bench code form implementation code
<tr><td>2022-02-27<td>First full successful synthesis on Quartus
<tr><td>2022-02-22<td>Renamed instructions shrr and shlr to sr, sr.
<tr><td>2022-02-21<td>Double ported data memory
<tr><td>2022-02-19<td>Reduced instruction set from 31 to 22 instructions
<tr><td>2022-02-17<td>Renamed instructions jmpBckIfZero to jumpIfZero and jmpFrwrdIfNotZero to jumpIfZero
<tr><td>2022-02-16<td>Eliminated extra actions on negative edges, all operations now done on positive edges
<tr><td>2022-02-14<td>Eliminated all non parallel instructions
<tr><td>2022-02-12<td>Added source register and target register ports to generate wave forms of $title in operation.
<tr><td>2022-02-11<td>Code for Program 2 operational and added to this document
<tr><td>2022-01-31<td>Code for Program 1 operational and added to this document
<tr><td>2022-01-30<td>Included low level ISA in this document
<tr><td>2022-01-15<td>Code for Program 3 operational and added to this document
</table>

<h1 id=h1024>System Verilog test bench execution</h1>

<p>The $title is fully operational as shown by the <a href="h1025">sample
output</a> produced by running the specified programs <b>1</b> to <b>3</b> using the
<a href="h1026">test bench</a> supplied in file:
<a href="https://github.com/banevare/ban/verilog/cpu_tb.sv">cpu_tb.sv</a>.
The first three programs are the ones requested by the assignment.  The following five programs
verify correct operation of the $title as follows:

<p><table cellspacing=10>
<tr><th>Program     <td>Description
<tr><td><a href="#h40">Instructions</a><td>Tests most of the instructions described in the <a href="h1">High Level Instruction Architecture</a>
<tr><td><a href="#h48">1_to_3      </a><td>Writes the number <b>1</b> into memory location <b>1</b> and the number <b>2</b> into memory location <b>2</b> to prove that the data addressing scheme works correctly
<tr><td><a href="#h56">For_loop    </a><td>Tests the construction of <b>for</b> loops by writing the number <b>i</b> into data memory byte <b>i</b>
<tr><td><a href="#h64">If          </a><td>Tests the construction of <b>if</b> statements by writing the even numbers in the range <b>0-255</b> into the corresponding data locations.
<tr><td><a href="#h72">ParityBits  </a><td>Tests a subroutine used in programs <b>1</b> and <b>2</b>.
</table>

<h2 id=h1025>Output</h2>

<p>Here are the results of running the specified programs and the extra test
programs. As can be seen all <b>57</b> tests pass successfully.

<p><pre>
$trace
</pre>

<h2 id=h1026>Test bench</h2>

<p>File: <a href="https://github.com/banevare/ban/verilog/cpu_tb.sv">cpu_tb.sv</a> contains the test bench code used to test the system verilog implementation of the $title .

<p><pre>
$tb
</pre>

<h1 id=h2001>Architecture</h1>

<h2 id=h2037>Clock</h2>

<p>The $title has a single clock.

<h2 id=h2002>Registers</h2>

<p>The $title has <b>16</b> registers conveniently addressed by the letters
<b>a</b> through <b>p</b> to make them easy to remember.  All registers are
general purpose.

<h2 id=h2004>Data memory</h2>

<p>The $title has <b>256</b> <b>8</b> bit bytes of double ported data memory.
Data can be loaded into the input data buffer ready for use in the next program
run. Once a program has finished executing its output can be examined in the
data output buffer.

<h2 id=h2004>Program memory</h2>

<p>The $title has <b>2048</b> <b>9</b> bit bytes of single ported program memory.

<h2 id=h2008>Ports</h2>

<p>The $title has the following ports all of which activate on a positive edge:
<p><table cellspacing=10 border=0>
<tr><th>Pin             <th>Widths<th>Action when pin goes high
<tr><td><b>Reset           </b><td>1     <td>Reset the processor
<tr><td><b>Next            </b><td>1     <td>Execute next program
<tr><td><b>Clock           </b><td>1     <td>Program clock - execute next instruction
<tr><td><b>Data input      </b><td>8,8,1 <td>Address, data and latch to load data into the data memory input buffer ready for use by the next program.
<tr><td><b>Data output     </b><td>8,8,1 <td>Address, data and latch to examine the contents of the data memory output buffer after a program run.
<tr><td><b>Program counter </b><td>16    <td>An output port to show the address of the currently executing instruction.
<tr><td><b>Source register </b><td>8     <td>The current value of the source register.
<tr><td><b>Target register </b><td>8     <td>The current value of the target register.
</table>
END
 }

sub middle2                                                                     # isa
 {my $java    = convertToHtml("Machine3.java");
  my $verilog = convertToHtml("verilog/cpu.sv");
  my $rtl1    = resizeImage3 "/home/phil/people/Ashley/machine/Screenshots/rtl1.png";
  my $rtl2    =              "/home/phil/people/Ashley/machine/Screenshots/rtl2.png";
  my $rtl3    = resizeImage  "/home/phil/people/Ashley/machine/Screenshots/rtl3.png";
  my $rtl4    = resizeImage  "/home/phil/people/Ashley/machine/Screenshots/rtl4.png";
  my $add12   =              "/home/phil/people/Ashley/machine/verilog/images/add12s.png";
  my $and59   =              "/home/phil/people/Ashley/machine/verilog/images/and59s.png";
  my $trace1  = resizeImage  "/home/phil/people/Ashley/machine/Screenshots/trace1.png";
  my $trace4  = resizeImage  "/home/phil/people/Ashley/machine/Screenshots/trace4.png";
  my $trace5  = resizeImage  "/home/phil/people/Ashley/machine/Screenshots/trace5.png";
  my $trace6  = resizeImage  "/home/phil/people/Ashley/machine/Screenshots/trace6.png";
  my $trace7  = resizeImage  "/home/phil/people/Ashley/machine/Screenshots/trace7.png";
  my $trace8  = resizeImage  "/home/phil/people/Ashley/machine/Screenshots/trace8.png";

  <<END,

<h1 id=h1004>System Verilog execution trace signal images</h1>
<p>Here are the execution images produced while testing the cpu:

<h2 id=h1005>Adding 1 to 2 to get 3</h2>

<p>See next page for the details of loading 1 and 2 into a register to add them into a second register:

<p><img src="$add12">

<h2 id=h1006>And of 5 and 9 is oxD:</h2>
<p>See next page for the details of loading 5 and 9 into a register to and them into a second register:
<p><img src="$and59">

<h2 id=h1007>Random shots</h2>

<p>See next page for some wave form screen shots taken at random points
along the execution path.

<p><img src="$trace1">
<p><img src="$trace4">
<p><img src="$trace5">
<p><img src="$trace6">
<p><img src="$trace7">
<p><img src="$trace8">

<h1 id=h1003>Register Transfer Level</h1>
<p>Some screenshots from the register transfer layout generated by the synthesis of this design:

<p>See next page.
<p><img src="$rtl1">
<p><img src="$rtl2">
<p><img src="$rtl3">
<p><img src="$rtl4">

<h1 id=h1002>System Verilog</h1>
<p>The file <a href="https://github.com/banevare/ban/verilog/cpu.sv">cpu.sv</a> contains the System Verilog code used to implement the  $title :
<p><pre>
$verilog
</pre>

<h1 id=h1001>Assembler</h1>
<p>The file <a href="https://github.com/banevare/ban/Machine3.java">Machine3.java</a> contains the Java code used to assemble, emulate and test the design of the $title.
<p><pre>
$java
</pre>
END
 }

sub foot
 {map {"$_\n"} split /\n/, <<END,
END
 }
