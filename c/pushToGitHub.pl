#!/usr/bin/perl -I/home/phil/perl/cpan/DataTableText/lib/
#-------------------------------------------------------------------------------
# Push Btree block C code to GitHub
# Philip R Brenan at gmail dot com, Appa Apps Ltd Inc., 2024
#-------------------------------------------------------------------------------
use warnings FATAL => qw(all);
use strict;
use Carp;
use Data::Dump qw(dump);
use Data::Table::Text qw(:all);
use GitHub::Crud qw(:all);
use feature qw(say current_sub);

my $homeC   = q(/home/phil/btreeBlock/c/);                                      # Home folder for C code
my $home    = q(/home/phil/btreeBlock/);                                        # Home folder
my $md5File = qq($homeC.shaSums);                                               # Sha file sums for each known file to detect changes
my $user    = q(philiprbrenan);                                                 # User
my $repo    = q(btreeBlock);                                                    # Repo
my $wf      = q(.github/workflows/c.yml);                                       # Work flow on Ubuntu
my @ext     = qw(.c .h .md .pl);                                                # Extensions of files to upload to github

say STDERR timeStamp,  " push C to github $repo";

push my @files, searchDirectoryTreesForMatchingFiles($homeC, @ext);             # Files to upload
        @files = grep {!m(/\.|backups/|Classes/)} @files;                       # Remove files that do not need to be saved

@files = changedFiles $md5File, @files if 1;                                    # Filter out files that have not changed

if (!@files)                                                                    # No new files
 {say "Everything up to date";
  exit;
 }

if  (1)                                                                         # Upload via github crud
 {for my $s(@files)                                                             # Upload each selected file
   {my $c = readBinaryFile $s;                                                  # Load file

    $c = expandWellKnownWordsAsUrlsInMdFormat $c if $s =~ m(README);            # Expand README

    my $t = swapFilePrefix $s, $home;                                           # File on github
    my $w = writeFileUsingSavedToken($user, $repo, $t, $c);                     # Write file into github
    lll "$w  $t";
   }
 }

if (1)                                                                          # Write workflow
 {my $d = dateTimeStamp;
  my $y = <<"END";
# Test $d

name: Test
run-name: $repo

on:
  push:
    paths:
      - '**/c.yml'
jobs:

  test:
    permissions: write-all
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout\@v4
      with:
        ref: 'main'

    - name: Install C and dtt
      run: |
         sudo apt install build-essential
         sudo cpan install -T Data::Table::Text GitHub::Crud

    - name: Risc V
      run: |
        sudo apt install gcc-riscv64-unknown-elf libnewlib-dev

    - name: Test
      run: |
        cd c; perl stuck.pl

    - name: Asm
      run: |
        cd c; riscv64-unknown-elf-gcc -S -I. -I/usr/include/newlib/  -o stuck.asm stuck.c
END

  my $f = writeFileUsingSavedToken $user, $repo, $wf, $y;                       # Upload workflow
  lll "$f  Ubuntu work flow for $repo";
 }
else
 {say STDERR "No Java files changed";
 }
