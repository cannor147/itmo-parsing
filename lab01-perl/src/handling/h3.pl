#!/usr/bin/perl
use strict;
use warnings FATAL => 'all';
use List::MoreUtils qw(uniq);

my @sites;
while (<>) {
    if ($_ =~ /<\s*(a)(.*)href\s*=\s*"(?<scheme>(.+?:)\/\/)?(?<host>\w+.*?)[\"\/\:].*>/) {
        push(@sites, $+{host});
    }
}

my @filtered = uniq(sort @sites);
for (@filtered) {
    print "$_\n";
}