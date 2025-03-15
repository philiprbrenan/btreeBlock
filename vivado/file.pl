#!/usr/bin/perl
use v5.30;
use warnings FATAL => qw(all);
use strict;
use Carp;
use Data::Table::Text qw(:all);
use Data::Dump qw(dump);

my @files = searchDirectoryTreesForMatchingFiles(qq($ENV{HOME}/btreeBlock/verilog/), qw(.tb));

for my $f(@files)
 {if ($f =~ m(/(\w+)/(\d+)/statement/(\d+)/)igs)
   {my ($project, $key, $statement) = ($1, $2, $3);
    say STDERR "gen($project, $key, $statement);\n";
   }
 }
