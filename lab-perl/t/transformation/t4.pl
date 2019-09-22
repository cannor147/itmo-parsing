#!/usr/bin/perl
use strict;
use warnings FATAL => 'all';

while (<>) {
    s/(\w+)(\W+)(\w+)(\W+)/$3$2$1$4/;
    print;
}
