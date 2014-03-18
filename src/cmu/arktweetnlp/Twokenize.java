package cmu.arktweetnlp;

import java.util.regex.*;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * Twokenize -- a tokenizer designed for Twitter text in English and some other European languages.
 * This is the Java version. If you want the old Python version, see: http://github.com/brendano/tweetmotif
 * 
 * This tokenizer code has gone through a long history:
 *
 * (1) Brendan O'Connor wrote original version in Python, http://github.com/brendano/tweetmotif
 *        TweetMotif: Exploratory Search and Topic Summarization for Twitter.
 *        Brendan O'Connor, Michel Krieger, and David Ahn.
 *        ICWSM-2010 (demo track), http://brenocon.com/oconnor_krieger_ahn.icwsm2010.tweetmotif.pdf
 * (2a) Kevin Gimpel and Daniel Mills modified it for POS tagging for the CMU ARK Twitter POS Tagger
 * (2b) Jason Baldridge and David Snyder ported it to Scala
 * (3) Brendan bugfixed the Scala port and merged with POS-specific changes
 *     for the CMU ARK Twitter POS Tagger  
 * (4) Tobi Owoputi ported it back to Java and added many improvements (2012-06)
 * 
 * Current home is http://github.com/brendano/ark-tweet-nlp and http://www.ark.cs.cmu.edu/TweetNLP
 *
 * There have been at least 2 other Java ports, but they are not in the lineage for the code here.
 */
public class Twokenize {
    
    // The main work of tokenizing a tweet.
    private static List<String> simpleTokenize (String text) {
        List<String> ret = new ArrayList<String>();
        List<Integer> indices = RawTwokenize.simpleTokenize(text);

        for (int i = 0; i < indices.size(); i += 2) {
            ret.add(text.substring(indices.get(i), indices.get(i+1)));
        }
 
        return ret;
    }  

    /** Assume 'text' has no HTML escaping. **/
    public static List<String> tokenize(String text){
        return simpleTokenize(RawTwokenize.squeezeWhitespace(text));
    }

    /**
     * Twitter text comes HTML-escaped, so unescape it.
     * We also first unescape &amp;'s, in case the text has been buggily double-escaped.
     */
    public static String normalizeTextForTagger(String text) {
    	text = text.replaceAll("&amp;", "&");
    	text = StringEscapeUtils.unescapeHtml(text);
    	return text;
    }

    /**
     * This is intended for raw tweet text -- we do some HTML entity unescaping before running the tagger.
     * 
     * This function normalizes the input text BEFORE calling the tokenizer.
     * So the tokens you get back may not exactly correspond to
     * substrings of the original text.
     */
    public static List<String> tokenizeRawTweetText(String text) {
        List<String> tokens = tokenize(normalizeTextForTagger(text));
        return tokens;
    }
}
