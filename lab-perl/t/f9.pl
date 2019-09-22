#!/usr/bin/perl
use strict;
use warnings FATAL => 'all';

while (<>) {
    print if /^((?!\s).)*(((?!\s).)+((\s)+((?!\s).)+)*|((?!\s).)*)((?!\s).)*$/;
}
