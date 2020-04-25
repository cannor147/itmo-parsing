#!/usr/bin/perl
use strict;
use warnings FATAL => 'all';

while (<>) {
    s/(\d)0(?!\d)/$1/g;
    print;
}
