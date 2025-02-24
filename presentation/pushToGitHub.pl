#!/usr/bin/perl -I/home/phil/perl/cpan/DataTableText/lib/
#-------------------------------------------------------------------------------
# Push Btree presntation to GitHub
# Philip R Brenan at gmail dot com, Appa Apps Ltd Inc., 2025
#-------------------------------------------------------------------------------
use warnings FATAL => qw(all);
use strict;
use Carp;
use Data::Dump qw(dump);
use Data::Table::Text qw(:all);
use GitHub::Crud qw(:all);
use feature qw(say current_sub);

my $home    = q(/home/phil/btreeBlock/presentation/);                           # Home folder
my $user    = q(philiprbrenan);                                                 # User
my $repo    = q(philiprbrenan.github.io);                                       # Repo
my $dir     = q(zesal/presentation);                                            # Work flow on Ubuntu
my @ext     = qw(.html .jpg .pl);                                               # Extensions of files to upload to github

say STDERR timeStamp,  " Push presentation to github $repo";

push my @files, searchDirectoryTreesForMatchingFiles($home, @ext);              # Files to upload

if (1)                                                                          # Remove most of the verilog except the reports
 {my @f = @files; @files = ();
  for my $f(@f)
   {next if $f =~ m(verilog) and $f !~ m(/vivado/reports/);
    push @files, $f;
   }
 }

for my $s(@files)                                                               # Upload each selected file
 {my $c = readBinaryFile $s;                                                    # Load file

  $c = expandWellKnownWordsAsUrlsInMdFormat $c if $s =~ m(README);              # Expand README

  my $t = fpf $dir, swapFilePrefix $s, $home;                                             # File on github
  my $w = writeFileUsingSavedToken($user, $repo, $t, $c);                       # Write file into github
  lll "$w  $t";
 }
