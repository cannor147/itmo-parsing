#!/usr/bin/perl
use strict;
use warnings FATAL => 'all';

while (<>) {
    print if /^(.*\W+)?((\w+)\3)(\W+.*)?$/;
}
