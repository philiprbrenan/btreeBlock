#!/usr/bin/perl -I/home/phil/perl/cpan/DataTableText/lib/
#-------------------------------------------------------------------------------
# XXX
# Philip R Brenan at gmail dot com, Appa Apps Ltd Inc., 2025
#-------------------------------------------------------------------------------
use v5.38;
use warnings FATAL => qw(all);
use strict;
use Carp;
use Data::Dump qw(dump);
use Data::Table::Text qw(:all);

my $out = q(performance.htm);

my $cs  =    23;                                                                # Custom statements
my $gs  =   122;                                                                # Generic statements
my $ca  =  2467;                                                                # Custom silicon area
my $cf  =   902;                                                                # Custom speed
my $ga  = 12927;                                                                # Generic silicon area
my $gf  =   399;                                                                # Generic speed

my $gsCs   = sprintf "%5.2f", $gs / $cs;
my $gaCa   = sprintf "%5.2f", $ga / $ca;
my $gfCf   = sprintf "%5.2f", $cf / $gf;
my $better = sprintf "%5.2f", $gaCa * $gfCf * $gsCs;

owf($out, <<END)
<table cellpadding=10 border=1>
<tr><th colspan=2>Area um2<th colspan=2>Fmax MHz<th colspan=2>Statements
<tr><th>Custom<th>Generic <th>Custom<th>Generic<th>Custom<th>Generic
<tr><td>$ca<td>$ga<td>$cf<td>$gf<td>$cs<td>$gs
<tr><td colspan=2>$gaCa x Smaller<td colspan=2>$gfCf x Faster<td colspan=2>$gsCs * Compact code
<tr><td colspan=4>$better x better
</table>
END
