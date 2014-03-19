package cmu.arktweetnlp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import cmu.arktweetnlp.impl.Model;
import cmu.arktweetnlp.impl.ModelSentence;
import cmu.arktweetnlp.impl.Sentence;
import cmu.arktweetnlp.impl.features.FeatureExtractor;
import cmu.arktweetnlp.util.ComparablePair;

class RawTagger {
    
    public Model model;
    public FeatureExtractor featureExtractor;
    
    public void loadModel(String modelFileName) throws IOException {
        this.model = Model.loadModelFromText(modelFileName);
        this.featureExtractor = new FeatureExtractor(model, false);
    }
    
    public class TaggedToken {
        public String token;
        public String tag;
        public ComparablePair<Integer, Integer> span;
        public double confidence;
    }
    
    public List<RawTagger.TaggedToken> tokenizeAndTag(String text) {
        Sentence sentence;
        ModelSentence ms;
        String punctText;
        
        // Retrieve tokenised offsets
        List<ComparablePair<Integer, Integer>> tokens;
        tokens = RawTwokenize.tokenizeRawTweetText(text);
        punctText = RawTwokenize.normalizeTextForTagger(text);
        
        // Construct model sentences and tag everything
        List<String> sentenceTokens = new ArrayList<String>();
        for (ComparablePair<Integer, Integer> t : tokens) {
            String sub = punctText.substring(t.first, t.second); 
            sentenceTokens.add(sub);
        }
        
        sentence = new Sentence();
        sentence.tokens = sentenceTokens;
        ms = new ModelSentence(sentence.T());
        featureExtractor.computeFeatures(sentence, ms);
        model.greedyDecode(ms, false);
        
        // Construct the output
        List<RawTagger.TaggedToken> taggedTokens = new ArrayList<RawTagger.TaggedToken>();
        for (int t=0; t < sentence.T(); t++) {
            RawTagger.TaggedToken tt = new RawTagger.TaggedToken();
            tt.span = tokens.get(t);
            tt.token = punctText.substring(tt.span.first, tt.span.second);
            tt.tag = model.labelVocab.name(ms.labels[t]);
            tt.confidence = ms.confidences[t];
            taggedTokens.add(tt);
        }
        
        return taggedTokens;
    }
}