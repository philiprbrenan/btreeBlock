#!/usr/bin/perl -I/home/phil/perl/cpan/DataTableText/lib/
#-------------------------------------------------------------------------------
# Push Btree block code to GitHub
# Philip R Brenan at gmail dot com, Appa Apps Ltd Inc., 2024
#-------------------------------------------------------------------------------
#die "Turned off"
use warnings FATAL => qw(all);
use strict;
use Carp;
use Data::Dump qw(dump);
use Data::Table::Text qw(:all);
use GitHub::Crud qw(:all);
use feature qw(say current_sub);

my $home    = q(/home/phil/btreeBlock/);                                        # Home folder
my $md5File = q(/home/phil/btreeBlock/.md5Sums);                                # Md5 file sums for each known file to detect changes
my $user    = q(philiprbrenan);                                                 # User
my $repo    = q(btreeBlock);                                                    # Repo
my $wf      = q(.github/workflows/main.yml);                                    # Work flow on Ubuntu
my @ext     = qw(.html .java .jpg .md .pl .pdf .png .py .rpt .sdc .txt .xdc);   # Extensions of files to upload to github
my $sc      = fpd $home, qw(siliconCompiler);                                   # Silicon compiler
my @scExt   = qw(.gds .py .xdc);                                                # Silicon compiler extensions

say STDERR timeStamp,  " push to github $repo";

push my @files, searchDirectoryTreesForMatchingFiles($home, @ext);              # Files to upload
        @files = grep {!m(/\.|backups/|Classes/)} @files;                       # Remove files that do not need to be saved
        @files = grep {!m(/vivado/runs/)}         @files;
        @files = grep {!m(/vivado/pins/)}         @files;
        @files = grep {!m(/gowin/\w+/reports/)}   @files;
        @files = grep {!m(/logs/)}                @files;

if (1)                                                                          # Remove most of the verilog except the reports
 {my @f = @files; @files = ();
  for my $f(@f)
   {next if $f =~ m(verilog) and $f !~ m(/(reports|timing_route)/);
    push @files, $f;
   }
 }

if (1)
 {push my @sc, searchDirectoryTreesForMatchingFiles($sc, @scExt);               # Silicon compiler files
  my %s;
  @files = grep {!$s{$_}++} @files, @sc;
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
  @j = qw(BtreeSF BtreeDM);                                                     # This will test all the stuff of current interest

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

    - name: 'JDK 24'
      uses: oracle-actions/setup-java\@v1
      with:
        website: jdk.java.net

    - name: Java release
      run: |
        java -version

    - name: Verilog install
      run: |
        sudo apt install iverilog

    - name: Verilog release
      run: |
        iverilog -V

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

#  $y .= <<"END";                                                               # Upload generated files
#    - name: Upload Artifact
#      if: matrix.task == 'BtreeDM' && always()
#      uses: actions/upload-artifact\@v4
#      with:
#        name: verilog
#        path: verilog/
#END

  my $f = writeFileUsingSavedToken $user, $repo, $wf, $y;                       # Upload workflow
  lll "$f  Ubuntu work flow for $repo";
 }
else
 {say STDERR "No Java files changed";
 }
