#!/usr/bin/perl
use strict;
use warnings FATAL => 'all';

our $result = "";
while (<>) {
    s/(^[ ]+)|([ ]+$)//g;
    $result = "$result$_";
}

$result =~ s/<.*?>//g;

$result =~ s/[ ][ ]+/ /g;
$result =~ s/\n(\n)+/\n\n/g;
$result =~ s/(^\n+)|(\n+$)//g;

print $result;