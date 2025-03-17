#!/usr/bin/perl -I/home/phil/perl/cpan/DataTableText/lib/
#-------------------------------------------------------------------------------
# Analyze timing results
# Philip R Brenan at gmail dot com, Appa Apps Ltd Inc., 2025
#-------------------------------------------------------------------------------
use v5.38;
use warnings FATAL => qw(all);
use strict;
use Carp;
use Data::Dump qw(dump);
use Data::Table::Text qw(:all);

my $home      = q(/home/phil/btreeBlock/);                                      # Home folder
my $timingDir = fpd $home, qw(verilog find vivado timing_route);                # Verilog timing folder

push my @files, searchDirectoryTreesForMatchingFiles($home, qw(.rpt));          # Files to process

for my $f(@files)
 {my $s = readFile($f);
  if ($s =~ m((\d+)\.rpt)is)
    push @files, $f;
   }
 }

if (0)
 {push my @qflow, searchDirectoryTreesForMatchingFiles($home, @qflowExt);       # Qflow Files to upload
          @qflow = grep {m(/qflow/)} @qflow;
  push @files, @qflow;
 }

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
else                                                                            # Upload files via git
 {my $g = join " ", @files;
  qx(git add $g; git commit -m aaa; git push --force);                          # Force used to overcome changes to workflow file which is used as a surrogate for any change
 }

writeFileUsingSavedToken($user, $repo, q(.config/geany/snippets.conf),          # Save the snippets file as this was the thing I missed most after a rebuild
                   readFile(q(/home/phil/.config/geany/snippets.conf)));
writeFileUsingSavedToken($user, $repo, q(.config/geany/keybindings.conf),       # Save the keybindings file for the same reason
                  readFile(q(//home/phil/.config/geany/keybindings.conf)));
writeFileUsingSavedToken($user, $repo, q(.config/bashrc),                       # Save bash commands that are useful for running synthesis on a server
                         <<'END');
alias ll='ls -alF'
alias la='ls -A'
alias l='ls -CF'
alias b='cd   ~/btreeBlock/'
alias bv='cd  ~/btreeBlock/vivado'
alias g='git status; git add *; git commit -m aaa; git push --force'
alias gg='cd; sudo rm -r ~/btreeBlock/; git clone git@github.com:philiprbrenan/btreeBlock.git; cd ~/btreeBlock'
alias m='micro'
alias dv='cd  ~/btreeBlock/verilog/delete/vivado/reports'
alias fv='cd  ~/btreeBlock/verilog/find/vivado/reports'
alias pv='cd  ~/btreeBlock/verilog/put/vivado/reports'
alias s='perl ~/btreeBlock/vivado/synthesis.pl'
alias t='top -u azureuser -E g'
alias x='bash ~/btreeBlock/j.sh
END

if (1)                                                                          # Write workflow
 {my @j = map {fn $_}  grep {fn($_) !~ m(Able\Z)}                               # Java files to test do not include interfaces
          searchDirectoryTreesForMatchingFiles($home, qw(.java));               # Java files

  my $d = dateTimeStamp;
  my $c = q(com/AppaApps/Silicon);                                              # Package to classes folder
  my $j = join ', ', @j;                                                        # Java files
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

    - name: Verilog install
      run: |
        sudo apt install iverilog

    - name: Position files in package
      run: |
        mkdir -p $c
        cp `find .  -name "*.java"` $c

    - name: Compile
      run: |
        javac -g -d Classes -cp Classes `find $c -name "*.java"`
END

  for my $j(@j)                                                                 # Java files
   {$y .= <<END;

    - name: Test $j
      if: matrix.task == '$j'
      run: |
        java -cp Classes $c/$j

END
   }

  my $f = writeFileUsingSavedToken $user, $repo, $wf, $y;                       # Upload workflow
  lll "$f  Ubuntu work flow for $repo";
 }
else
 {say STDERR "No Java files changed";
 }
