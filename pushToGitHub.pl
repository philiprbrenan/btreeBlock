#!/usr/bin/perl -I/home/phil/perl/cpan/DataTableText/lib/
#-------------------------------------------------------------------------------
# Push Btree block code to GitHub
# Philip R Brenan at gmail dot com, Appa Apps Ltd Inc., 2024
#-------------------------------------------------------------------------------
use warnings FATAL => qw(all);
use strict;
use Carp;
use Data::Dump qw(dump);
use Data::Table::Text qw(:all);
use GitHub::Crud qw(:all);
use feature qw(say current_sub);

my $home = q(/home/phil/btreeBlock/);                                           # Home folder
my $user = q(philiprbrenan);                                                    # User
my $repo = q(btreeBlock);                                                       # Repo
my $wf   = q(.github/workflows/main.yml);                                       # Work flow on Ubuntu
my @ext  = qw(.java .md .pl .txt .png .py .sv .tb);                             # All files to upload to github
#  @ext  = qw(.java .md .pl .txt);                                              # Reduced set of files to upload to github

push my @files, searchDirectoryTreesForMatchingFiles($home, @ext);              # Files to upload
        @files = grep {!m(z/|machine|perl|vivado/)} @files;                     # Remove fiels that do not need to be saved
my @java = map {fn $_}  grep {fe($_) eq q(java) && fn($_) !~ m(Able\Z)} @files; # Java files to test do not include interfaces

for my $s(@files)                                                               # Upload each selected file
 {my $c = readBinaryFile $s;                                                    # Load file

  $c = expandWellKnownWordsAsUrlsInMdFormat $c if $s =~ m(README);              # Expand README

  my $t = swapFilePrefix $s, $home;                                             # File on github
  my $w = writeFileUsingSavedToken($user, $repo, $t, $c);                       # Write file into github
  lll "$w  $t";
 }

writeFileUsingSavedToken($user, $repo, q(.config/geany/snippets.conf),          # Save the snippets file as this was the thing I missed most after a rebuild
                   readFile(q(/home/phil/.config/geany/snippets.conf)));
writeFileUsingSavedToken($user, $repo, q(.config/geany/keybindings.conf),       # Save the keybindings file for the same reason
                  readFile(q(//home/phil/.config/geany/keybindings.conf)));

if (1)                                                                          # Write workflow
 {my $d = dateTimeStamp;
  my $c = q(com/AppaApps/Silicon);                                              # Package to classes folder
  my $j = join ', ', @java;                                                     # Java files
  my $y = <<"END";
# Test $d

name: Test
run-name: $repo

on:
  push:
    paths:
      - '**/main.yml'

concurrency:
  group: \${{ github.workflow }}-\${{ github.ref }}
  cancel-in-progress: true

jobs:

  test:
    permissions: write-all
    runs-on: ubuntu-latest

    strategy:
      matrix:
        task: [$j]

    steps:
    - uses: actions/checkout\@v4
      with:
        ref: 'main'

    - name: 'JDK 22'
      uses: oracle-actions/setup-java\@v1
      with:
        website: jdk.java.net

    - name: Install Tree
      run:
        sudo apt install tree

    - name: Position files in package
      run: |
        mkdir -p $c
        cp `find .  -name "*.java"` $c

    - name: Files
      run:
        tree

    - name: Compile
      run: |
        javac -g -d Classes -cp Classes `find $c -name "*.java"`
END

  for my $j(@java)                                                              # Java files
   {$y .= <<END;

    - name: Test $j
      if: matrix.task == '$j'
      run: |
        java -cp Classes $c/$j

END
   }
  $y .= <<"END";
    - name: Verilog install
      if: matrix.task == 'BtreePA'
      run: |
        sudo apt install iverilog

    - name: Verilog Run Find
      if: matrix.task == 'BtreePA'
      run: |
        cd verilog; rm -f find;   iverilog -Iincludes/ -g2012 -o find   find.v   find.tb   && timeout 1m ./find
        diff trace/test_verilog_find.txt verilog/trace/find/trace.txt

    - name: Verilog Run Delete
      if: matrix.task == 'BtreePA'
      run: |
        cd verilog; rm -f delete; iverilog -Iincludes/ -g2012 -o delete delete.v delete.tb && timeout 1m ./delete
        diff trace/test_verilog_delete.txt verilog/trace/delete/trace.txt

    - name: Verilog Run Put
      if: matrix.task == 'BtreePA'
      run: |
        cd verilog; rm -f put;    iverilog -Iincludes/ -g2012 -o put    put.v    put.tb    && timeout 1m ./put
        diff trace/test_verilog_put.txt verilog/trace/delete/put.txt
END

  my $f = writeFileUsingSavedToken $user, $repo, $wf, $y;                       # Upload workflow
  lll "$f  Ubuntu work flow for $repo";
 }
