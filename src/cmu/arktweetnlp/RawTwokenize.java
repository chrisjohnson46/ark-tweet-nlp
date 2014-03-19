package cmu.arktweetnlp;

import cmu.arktweetnlp.util.ComparablePair;
import java.util.regex.*;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.Collections;
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
 * 
 * RawTwokenize: output token indices
 */
public class RawTwokenize {
    static Pattern Contractions = Pattern.compile("(?i)(\\w+)(n['’′]t|['’′]ve|['’′]ll|['’′]d|['’′]re|['’′]s|['’′]m)$");
    static Pattern Whitespace = Pattern.compile("[\\s\\p{Zs}]+");

    static String punctChars = "['\"“”‘’.?!…,:;]"; 
    //static String punctSeq   = punctChars+"+";	//'anthem'. => ' anthem '.
    static String punctSeq   = "['\"“”‘’]+|[.?!,…]+|[:;]+";	//'anthem'. => ' anthem ' .
    static String entity     = "&(?:amp|lt|gt|quot);";
    //  URLs

    // BTO 2012-06: everyone thinks the daringfireball regex should be better, but they're wrong.
    // If you actually empirically test it the results are bad.
    // Please see https://github.com/brendano/ark-tweet-nlp/pull/9

    static String urlStart1  = "(?:https?://|\\bwww\\.)";
    static String commonTLDs = "(?:com|org|edu|gov|net|mil|aero|asia|biz|cat|coop|info|int|jobs|mobi|museum|name|pro|tel|travel|xxx)";
    static String ccTLDs	 = "(?:ac|ad|ae|af|ag|ai|al|am|an|ao|aq|ar|as|at|au|aw|ax|az|ba|bb|bd|be|bf|bg|bh|bi|bj|bm|bn|bo|br|bs|bt|" +
    "bv|bw|by|bz|ca|cc|cd|cf|cg|ch|ci|ck|cl|cm|cn|co|cr|cs|cu|cv|cx|cy|cz|dd|de|dj|dk|dm|do|dz|ec|ee|eg|eh|" +
    "er|es|et|eu|fi|fj|fk|fm|fo|fr|ga|gb|gd|ge|gf|gg|gh|gi|gl|gm|gn|gp|gq|gr|gs|gt|gu|gw|gy|hk|hm|hn|hr|ht|" +
    "hu|id|ie|il|im|in|io|iq|ir|is|it|je|jm|jo|jp|ke|kg|kh|ki|km|kn|kp|kr|kw|ky|kz|la|lb|lc|li|lk|lr|ls|lt|" +
    "lu|lv|ly|ma|mc|md|me|mg|mh|mk|ml|mm|mn|mo|mp|mq|mr|ms|mt|mu|mv|mw|mx|my|mz|na|nc|ne|nf|ng|ni|nl|no|np|" +
    "nr|nu|nz|om|pa|pe|pf|pg|ph|pk|pl|pm|pn|pr|ps|pt|pw|py|qa|re|ro|rs|ru|rw|sa|sb|sc|sd|se|sg|sh|si|sj|sk|" +
    "sl|sm|sn|so|sr|ss|st|su|sv|sy|sz|tc|td|tf|tg|th|tj|tk|tl|tm|tn|to|tp|tr|tt|tv|tw|tz|ua|ug|uk|us|uy|uz|" +
    "va|vc|ve|vg|vi|vn|vu|wf|ws|ye|yt|za|zm|zw)";	//TODO: remove obscure country domains?
    static String urlStart2  = "\\b(?:[A-Za-z\\d-])+(?:\\.[A-Za-z0-9]+){0,3}\\." + "(?:"+commonTLDs+"|"+ccTLDs+")"+"(?:\\."+ccTLDs+")?(?=\\W|$)";
    static String urlBody    = "(?:[^\\.\\s<>][^\\s<>]*?)?";
    static String urlExtraCrapBeforeEnd = "(?:"+punctChars+"|"+entity+")+?";
    static String urlEnd     = "(?:\\.\\.+|[<>]|\\s|$)";
    public static String url        = "(?:"+urlStart1+"|"+urlStart2+")"+urlBody+"(?=(?:"+urlExtraCrapBeforeEnd+")?"+urlEnd+")";


    // Numeric
    static String timeLike   = "\\d+(?::\\d+){1,2}";
    //static String numNum     = "\\d+\\.\\d+";
    static String numberWithCommas = "(?:(?<!\\d)\\d{1,3},)+?\\d{3}" + "(?=(?:[^,\\d]|$))";
    static String numComb	 = "\\p{Sc}?\\d+(?:\\.\\d+)+%?";

    // Abbreviations
    static String boundaryNotDot = "(?:$|\\s|[“\\u0022?!,:;]|" + entity + ")";
    static String aa1  = "(?:[A-Za-z]\\.){2,}(?=" + boundaryNotDot + ")";
    static String aa2  = "[^A-Za-z](?:[A-Za-z]\\.){1,}[A-Za-z](?=" + boundaryNotDot + ")";
    static String standardAbbreviations = "\\b(?:[Mm]r|[Mm]rs|[Mm]s|[Dd]r|[Ss]r|[Jj]r|[Rr]ep|[Ss]en|[Ss]t)\\.";
    static String arbitraryAbbrev = "(?:" + aa1 +"|"+ aa2 + "|" + standardAbbreviations + ")";
    static String separators  = "(?:--+|―|—|~|–|=)";
    static String decorations = "(?:[♫♪]+|[★☆]+|[♥❤♡]+|[\\u2639-\\u263b]+|[\\ue001-\\uebbb]+)";
    static String thingsThatSplitWords = "[^\\s\\.,?\"]";
    static String embeddedApostrophe = thingsThatSplitWords+"+['’′]" + thingsThatSplitWords + "*";
    
    public static String OR(String... parts) {
        String prefix="(?:";
        StringBuilder sb = new StringBuilder();
        for (String s:parts){
            sb.append(prefix);
            prefix="|";
            sb.append(s);
        }
        sb.append(")");
        return sb.toString();
    }
    
    //  Emoticons
    static String normalEyes = "(?iu)[:=]"; // 8 and x are eyes but cause problems
    static String wink = "[;]";
    static String noseArea = "(?:|-|[^a-zA-Z0-9 ])"; // doesn't get :'-(
    static String happyMouths = "[D\\)\\]\\}]+";
    static String sadMouths = "[\\(\\[\\{]+";
    static String tongue = "[pPd3]+";
    static String otherMouths = "(?:[oO]+|[/\\\\]+|[vV]+|[Ss]+|[|]+)"; // remove forward slash if http://'s aren't cleaned

    // mouth repetition examples:
    // @aliciakeys Put it in a love song :-))
    // @hellocalyclops =))=))=)) Oh well

    static String bfLeft = "(♥|0|o|°|v|\\$|t|x|;|\\u0CA0|@|ʘ|•|・|◕|\\^|¬|\\*)";
    static String bfCenter = "(?:[\\.]|[_-]+)";
    static String bfRight = "\\2";
    static String s3 = "(?:--['\"])";
    static String s4 = "(?:<|&lt;|>|&gt;)[\\._-]+(?:<|&lt;|>|&gt;)";
    static String s5 = "(?:[.][_]+[.])";
    static String basicface = "(?:(?i)" +bfLeft+bfCenter+bfRight+ ")|" +s3+ "|" +s4+ "|" + s5;

    static String eeLeft = "[＼\\\\ƪԄ\\(（<>;ヽ\\-=~\\*]+";
    static String eeRight= "[\\-=\\);'\\u0022<>ʃ）/／ノﾉ丿╯σっµ~\\*]+";
    static String eeSymbol = "[^A-Za-z0-9\\s\\(\\)\\*:=-]";
    static String eastEmote = eeLeft + "(?:"+basicface+"|" +eeSymbol+")+" + eeRight;

    
    public static String emoticon = OR(
            // Standard version  :) :( :] :D :P
    		"(?:>|&gt;)?" + OR(normalEyes, wink) + OR(noseArea,"[Oo]") + 
            	OR(tongue+"(?=\\W|$|RT|rt|Rt)", otherMouths+"(?=\\W|$|RT|rt|Rt)", sadMouths, happyMouths),

            // reversed version (: D:  use positive lookbehind to remove "(word):"
            // because eyes on the right side is more ambiguous with the standard usage of : ;
            "(?<=(?: |^))" + OR(sadMouths,happyMouths,otherMouths) + noseArea + OR(normalEyes, wink) + "(?:<|&lt;)?",

            //inspired by http://en.wikipedia.org/wiki/User:Scapler/emoticons#East_Asian_style
            eastEmote.replaceFirst("2", "1"), basicface
            // iOS 'emoji' characters (some smileys, some symbols) [\ue001-\uebbb]  
            // TODO should try a big precompiled lexicon from Wikipedia, Dan Ramage told me (BTO) he does this
    );

    static String Hearts = "(?:<+/?3+)+"; //the other hearts are in decorations

    static String Arrows = "(?:<*[-―—=]*>+|<+[-―—=]*>*)|\\p{InArrows}+";

    // BTO 2011-06: restored Hashtag, AtMention protection (dropped in original scala port) because it fixes
    // "hello (#hashtag)" ==> "hello (#hashtag )"  WRONG
    // "hello (#hashtag)" ==> "hello ( #hashtag )"  RIGHT
    // "hello (@person)" ==> "hello (@person )"  WRONG
    // "hello (@person)" ==> "hello ( @person )"  RIGHT
    // ... Some sort of weird interaction with edgepunct I guess, because edgepunct 
    // has poor content-symbol detection.

    // This also gets #1 #40 which probably aren't hashtags .. but good as tokens.
    // If you want good hashtag identification, use a different regex.
    static String Hashtag = "#[a-zA-Z0-9_]+";  //optional: lookbehind for \b
    //optional: lookbehind for \b, max length 15
    static String AtMention = "[@＠][a-zA-Z0-9_]+"; 

    // I was worried this would conflict with at-mentions
    // but seems ok in sample of 5800: 7 changes all email fixes
    // http://www.regular-expressions.info/email.html
    static String Bound = "(?:\\W|^|$)";
    public static String Email = "(?<=" +Bound+ ")[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}(?=" +Bound+")";

    // We will be tokenizing using these regexps as delimiters
    // Additionally, these things are "protected", meaning they shouldn't be further split themselves.
    static Pattern Protected  = Pattern.compile(
            OR(
                    Hearts,
                    url,
                    Email,
                    timeLike,
                    //numNum,
                    numberWithCommas,
                    numComb,
                    emoticon,
                    Arrows,
                    entity,
                    punctSeq,
                    arbitraryAbbrev,
                    separators,
                    decorations,
                    embeddedApostrophe,
                    Hashtag,  
                    AtMention
            ));

    // Edge punctuation
    // Want: 'foo' => ' foo '
    // While also:   don't => don't
    // the first is considered "edge punctuation".
    // the second is word-internal punctuation -- don't want to mess with it.
    // BTO (2011-06): the edgepunct system seems to be the #1 source of problems these days.  
    // I remember it causing lots of trouble in the past as well.  Would be good to revisit or eliminate.

    // Note the 'smart quotes' (http://en.wikipedia.org/wiki/Smart_quotes)
    static String edgePunctChars    = "'\"“”‘’«»{}\\(\\)\\[\\]\\*&"; //add \\p{So}? (symbols)
    static String edgePunct    = "[" + edgePunctChars + "]";
    static String notEdgePunct = "[a-zA-Z0-9]"; // content characters
    static String offEdge = "(^|$|:|;|\\s|\\.|,)";  // colon here gets "(hello):" ==> "( hello ):"
    static Pattern EdgePunctLeft  = Pattern.compile(offEdge + "("+edgePunct+"+)("+notEdgePunct+")");
    static Pattern EdgePunctRight = Pattern.compile("("+notEdgePunct+")("+edgePunct+"+)" + offEdge);

    public static String splitEdgePunct (String input) {
        Matcher m1 = EdgePunctLeft.matcher(input);
        input = m1.replaceAll("$1$2 $3");
        m1 = EdgePunctRight.matcher(input);
        input = m1.replaceAll("$1 $2$3");
        return input;
    }
    
    // The main work of tokenizing a tweet.
    public static List<ComparablePair<Integer, Integer>> simpleTokenize (String text) {

        // Do the no-brainers first
        String splitPunctText = splitEdgePunct(text);

        int textLength = splitPunctText.length();
        
        // BTO: the logic here got quite convoluted via the Scala porting detour
        // It would be good to switch back to a nice simple procedural style like in the Python version
        // ... Scala is such a pain.  Never again.

        // Find the matches for subsequences that should be protected,
        // e.g. URLs, 1.0, U.N.K.L.E., 12:53
        Matcher matches = Protected.matcher(splitPunctText);
        List<ComparablePair<Integer,Integer>> badSpans = new ArrayList<ComparablePair<Integer,Integer>>();
        while(matches.find()){
            int start = matches.start();
            int end = matches.end();
            // The spans of the "bads" should not be split.
            if (start != end) {
                badSpans.add(new ComparablePair<Integer, Integer>(start, end));
            }
        }

        //
        // Find the portions of the tweet which can be split further ("good" spans)
        List<ComparablePair<Integer, Integer>> candidateGoodSpans = new ArrayList<ComparablePair<Integer,Integer>>();
        if (badSpans.size() == 0) {
            candidateGoodSpans.add(new ComparablePair<Integer, Integer>(0, splitPunctText.length()));
        }
        else {
            // First special case: add the initial part of the string
            ComparablePair<Integer, Integer> first = badSpans.get(0);
            if (first.first != 0) {
                candidateGoodSpans.add(new ComparablePair<Integer, Integer>(0, first.first));
            }
            // Second special case: add the final part of the string
            ComparablePair<Integer, Integer> last = badSpans.get(badSpans.size()-1);
            if (last.second != splitPunctText.length()) {
                candidateGoodSpans.add(new ComparablePair<Integer, Integer>(last.second, splitPunctText.length()));
            }
            // Add the spaces inbetween each of the bad spans
            for (int i = 0; i < badSpans.size()-1; i++) {
                ComparablePair<Integer, Integer> span1 = badSpans.get(i);
                ComparablePair<Integer, Integer> span2 = badSpans.get(i+1);
                candidateGoodSpans.add(new ComparablePair<Integer, Integer>(span1.second, span2.first));
            }
        }
        // 
        // Further split the good poitions and add to the output
        // Final list of good spans
        List<ComparablePair<Integer, Integer>> goodSpans = new ArrayList<ComparablePair<Integer, Integer>>();
        for (ComparablePair<Integer, Integer> span : candidateGoodSpans) {
            int start = span.first;
            int end = span.second;
            // Fetch the relevant part of the string
            String goodString = splitPunctText.substring(start, end);
            // Match any non-space portions of the string and add them
            // to the output
            Matcher splitMatcher = Pattern.compile("[^ ]+").matcher(goodString);
            while(splitMatcher.find()) {
                int startSpace = splitMatcher.start() + start;
                int endSpace   = splitMatcher.end()   + start;
                if (startSpace == endSpace) continue;  // No point in adding empty substrings
                goodSpans.add(new ComparablePair<Integer, Integer>(startSpace, endSpace));
            }
        }
        // Combine the bad and good spans
        List<ComparablePair<Integer, Integer>> ret = new ArrayList<ComparablePair<Integer, Integer>>();
        ret.addAll(goodSpans);
        ret.addAll(badSpans);
        // Remove duplicates 
        ret = new ArrayList<ComparablePair<Integer, Integer>>(new TreeSet<ComparablePair<Integer, Integer>>(ret));
        // Sort
        Collections.sort(ret);
        return ret;
    }  

    private static List<String> addAllnonempty(List<String> master, List<String> smaller){
        for (String s : smaller){
            String strim = s.trim();
            if (strim.length() > 0)
                master.add(strim);
        }
        return master;
    }

    // Final pass tokenization based on special patterns
    private static List<String> splitToken (String token) {

        Matcher m = Contractions.matcher(token);
        if (m.find()){
        	String[] contract = {m.group(1), m.group(2)};
        	return Arrays.asList(contract);
        }
        String[] contract = {token};
        return Arrays.asList(contract);
    }

    /** Assume 'text' has no HTML escaping. **/
    public static List<ComparablePair<Integer, Integer>> tokenize(String text){
        return simpleTokenize(squeezeWhitespace(text));
    }

    /** "foo   bar " => "foo bar" */
    public static String squeezeWhitespace (String input){
        return Whitespace.matcher(input).replaceAll(" ").trim();
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
    public static List<ComparablePair<Integer, Integer>> tokenizeRawTweetText(String text) {
        return tokenize(normalizeTextForTagger(text));
    }
}
