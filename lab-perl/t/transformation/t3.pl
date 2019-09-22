#!/usr/bin/perl
use strict;
use warnings FATAL => 'all';

while (<>) {
    s/(?<!\w)(a|A)+(?!\w)/argh/;
    print;
}
