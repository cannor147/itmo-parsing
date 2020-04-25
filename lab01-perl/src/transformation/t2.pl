#!/usr/bin/perl
use strict;
use warnings FATAL => 'all';

while (<>) {
    s/(?<!\w)human(?!\w)/computer/g;
    print;
}
