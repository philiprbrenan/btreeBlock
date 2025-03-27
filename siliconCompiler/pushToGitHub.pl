#!/usr/bin/perl -I/home/phil/perl/cpan/DataTableText/lib/
#-------------------------------------------------------------------------------
# Push Btree Silicon compiler output to GitHub
# Philip R Brenan at gmail dot com, Appa Apps Ltd Inc., 2024
#-------------------------------------------------------------------------------
#die "Turned off"
use v5.38;
use warnings FATAL => qw(all);
use strict;
use Carp;
use Data::Dump qw(dump);
use Data::Table::Text qw(:all);
use GitHub::Crud qw(:all);
use feature qw(say current_sub);

my $home    = q(/home/phil/btreeBlock/);                                        # Home folder
my $source  = q(/home/phil/aaa/btreeBlock/verilog/);                            # Source folder
my $target  = fpd $home, qw(siliconCompiler build);                             # Target folder

my $shaFile = qq($home.shaSums);                                                # Sha file sums for each known file to detect changes
my $user    = q(philiprbrenan);                                                 # User
my $repo    = q(btreeBlock);                                                    # Repo
my @ext     = qw(.png .gds);                                                    # Extensions of files to upload to github

say STDERR timeStamp,  " push Silicon Compiler to github $repo";

say STDERR "Recover Open Road reports";

sub zipFile($s) {fpe $s, q(zip)}                                                # Zip file name

sub files()                                                                     # Files associated with each project
 {my @f;
  for my $p(qw(delete find put))
   {push @f, [qq($source$p/1/siliconCompiler/build/$p/job0/write.gds/0/outputs/$p.png),
              fpe ($target, $p, $p, q(png))];
    push @f, [qq($source$p/1/siliconCompiler/build/$p/job0/write.gds/0/outputs/$p.gds),
              fpe ($target, $p, $p, q(gds))];
   }
  push @f, [fpe(qw(/home phil aaa/ bashrc)),  fpe($target, q(bashrc))];
  @f;
 }

my @files = files();                                                            # Files to recover

if (0)
 {say STDERR "Get files from Azure";                                            # Retrieve each file of interst via sshfs
  for my $f(@files)                                                             # Recover each file by copying it to local
   {my ($s, $t) = @$f;
    if (-f $s)
     {say STDERR sprintf("%-72s  to  %-72s", $s, $t);
      makePath($t);
      copyBinaryFile($s, $t);
      say STDERR $@ if $@;
     }
    else
     {say STDERR "Fail: $s";
     }
   }
 }

@files = map {$$_[1]} @files;                                                   # Local files

say STDERR "Zip gsd files";

for my $s(@files)                                                               # Zip gds files as they are big and zip well
 {next unless $s =~ m(gds\Z)is;
  my $z = zipFile($s);
  unlink $z;
  say STDERR "Zip $s to $z";
  system(qq(zip -r $z $s)) if 1;
  $s = $z;
 }

@files = changedFiles $shaFile, @files if 0;                                    # Changed local files

if (!@files)                                                                    # No new files
 {say "Everything up to date";
  exit;
 }

say STDERR "Upload changed files";

if (1)                                                                          # Upload via github crud
 {for my $s(@files)                                                             # Upload each selected file
   {my $c = readBinaryFile $s;                                                  # Load file
    my $t = swapFilePrefix $s, $home;                                           # File on github

    my $w = writeFileUsingSavedToken($user, $repo, $t, $c);                     # Write file into github
    lll "$w  $t";
   }
 }
