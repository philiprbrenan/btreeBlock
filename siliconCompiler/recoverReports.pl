#!/usr/bin/perl -I/home/phil/perl/cpan/DataTableText/lib/
#-------------------------------------------------------------------------------
# Recover OpenRoad reports from Azure
# Philip R Brenan at gmail dot com, Appa Apps Ltd Inc., 2024
#-------------------------------------------------------------------------------
use warnings FATAL => qw(all);
use strict;
use Carp;
use Data::Dump qw(dump);
use Data::Table::Text qw(:all);
use GitHub::Crud qw(:all);
use feature qw(say current_sub);

my $source = q(/home/phil/aaa/btreeBlock/verilog/);                             # Source folder
my $target = q(/home/phil/btreeBlock/siliconCompiler/);                         # Target folder

say STDERR "Recover Open Road reports";

my @files = &files();                                                           # Files to recover

for my $f(@files)                                                               # Target each source file
 {my ($s, $t) = @$f;
  if (-e $s)
   {say STDERR sprintf("%-72s  to  %-72s", $s, $t);
    copyFile($s, $t);
    say STDERR $@ if $@;
   }
  else
   {say STDERR "Fail: $s";
   }
 }

sub files()                                                                     # Files associated with each project
 {my @f;
  for my $p(qw(delete find put))
   {push @f, [qq($source$p/1/siliconCompiler/build/$p/job0/write.gds/0/outputs/$p.png),
              qq($target/$p/$p.png)];
    push @f, [qq($source$p/1/siliconCompiler/$p/job0/write.gds/0/outputs/$p.gds),
              qq($target/$p/$p.gds)];
   }
  @f;
 }
