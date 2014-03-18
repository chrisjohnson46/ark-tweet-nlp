#!/usr/bin/env python

"""
    Quick script to check that the tagger is working
"""

import subprocess
import sys

if __name__ == "__main__":

    tag_proc = subprocess.Popen(
        ['sh','runTagger.sh','examples/example_tweets.txt'],
        stdout = subprocess.PIPE
    )

    output, errors = tag_proc.communicate()
    zipped_buf = []
    
    for line in output.split('\n'):
        line = line.strip()
        if len(line) == 0:
            continue
        tokens,_,pos = line.partition('\t')
        pos,_,_      =  pos.partition('\t')

        tokens = tokens.split(' ')
        pos    =    pos.split(' ')

        for t, p in zip(tokens, pos):
            tmp = "%s\t%s" % (t, p)
            print tmp
            zipped_buf.append(tmp)

     
    orig_buf = []
    with open('examples/tagged_tweets_expected.txt', 'r') as fp:
        for line in fp:
            line = line.strip()
            if len(line) == 0:
                continue
            orig_buf.append(line)

    print orig_buf
    print zipped_buf

    for i, j in zip(orig_buf, zipped_buf):
        print i, j,
        if i.strip() == j.strip():
            print "OK"
        else:
            print "ERROR"


