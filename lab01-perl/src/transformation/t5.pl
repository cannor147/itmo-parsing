#!/usr/bin/perl
use strict;
use warnings FATAL => 'all';

while (<>) {
    s/(\W*)(\w)(\w)(\w*)(\W*)/$1$3$2$4$5/g;
    print;
}
