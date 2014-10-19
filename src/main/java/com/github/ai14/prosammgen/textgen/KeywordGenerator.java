package com.github.ai14.prosammgen.textgen;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import com.github.ai14.prosammgen.NLPModel;

import opennlp.tools.postag.POSTagger;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;

import java.io.IOException;
import java.util.Map;
import java.util.NavigableMap;

public class KeywordGenerator implements TextGenerator {

  private final ImmutableSet<String> words;

  private KeywordGenerator(ImmutableSet<String> words) {
    this.words = words;
  }

  public static KeywordGenerator withPOSParsing(NLPModel nlpModel,
                                                ImmutableSet<String> stopWords,
                                                String body)
      throws IOException {

    SentenceDetector sentenceDetector = new SentenceDetectorME(nlpModel.getSentenceModel());
    String[] sentences = sentenceDetector.sentDetect(body);

    ImmutableSet.Builder<String> resultBuilder = ImmutableSet.builder();

    for (String sentence : sentences) {
      Tokenizer tokenizer = new TokenizerME(nlpModel.getTokenizerModel());
      String[] tokens = tokenizer.tokenize(sentence);

      POSTagger posTagger = new POSTaggerME(nlpModel.getPosModel());
      String[] tags = posTagger.tag(tokens);

      StringBuilder wordBuilder = null;

      for (int i = 0; i < tokens.length; i++) {
        switch (tags[i]) {
          case "NN":
          case "NNS":
          case "NNP":
          case "NNPS":
            if (wordBuilder == null) {
              wordBuilder = new StringBuilder();
            } else {
              wordBuilder.append(' ');
            }
            wordBuilder.append(tokens[i]);
            break;
          default:
            if (wordBuilder != null) {
              resultBuilder.add(wordBuilder.toString());
              wordBuilder = null;
            }
        }
      }

      if (wordBuilder != null) {
        resultBuilder.add(wordBuilder.toString());
      }
    }

    ImmutableSet<String> keywords = resultBuilder.build();
    System.err.println("Keywords are: " + keywords);

    ImmutableSet<String> filteredKeywords =
        ImmutableSet.copyOf(Iterables.filter(keywords,
                                             (String word) ->
                                                 Iterables.any(stopWords, word::contains)));

    return new KeywordGenerator(filteredKeywords);
  }

  @Override
  public String generateText(Context context) {
    final NavigableMap<Double, String> probabilities = Maps.newTreeMap();
    double sumProb = 0;

    for (String word : words) {
      probabilities.put(sumProb, word);
      final double probability = Math.log(word.length());
      sumProb += probability;
    }

    Map.Entry<Double, String> keyword = probabilities.lowerEntry(Math.random() * sumProb);
    return keyword.getValue();
  }

  public ImmutableSet<String> getWords() {
    return words;
  }
}
