CMU ARK Twitter Part-of-Speech Tagger 

    ./runTagger.sh examples/example_tweets.txt
    ./runTagger.sh --help

We also include a script that invokes just the tokenizer:

    ./twokenize.sh examples/example_tweets.txt

You may have to adjust the parameters to "java" depending on your system.

If instead you are using a source checkout, see docs/hacking.txt for info.


Version 0.3 of the tagger is much faster and more accurate.  Please see the
tech report on the website for details.

For the Java API, see src/cmu/arktweetnlp; especially Tagger.java.
See also documentation in docs/ and src/cmu/arktweetnlp/package.html.

This tagger is described in the following two papers, available at the website.
Please cite these if you write a research paper using this software.

Part-of-Speech Tagging for Twitter: Annotation, Features, and Experiments

Part-of-Speech Tagging for Twitter: Word Clusters and Other Advances

Technical Report, Machine Learning Department. CMU-ML-12-107. September 2015.

Contact
=======

Please contact Chris Johnson 
