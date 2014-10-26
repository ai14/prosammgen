package com.github.ai14.prosammgen.textgen;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;

import com.github.ai14.prosammgen.NLPModel;

import opennlp.tools.postag.POSTagger;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;

import java.io.IOException;

public class KeywordGenerator implements TextGenerator {

  private final ImmutableSet<String> words;

  private KeywordGenerator(ImmutableSet<String> words) {
    this.words = words;
  }

  public static KeywordGenerator withPOSParsing(NLPModel nlpModel,
                                                final ImmutableSet<String> stopWords,
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
        if ("NN".equals(tags[i]) || "NNS".equals(tags[i]) || "NNP".equals(tags[i]) ||
            "NNPS".equals(tags[i])) {
          if (wordBuilder == null) {
            wordBuilder = new StringBuilder();
          } else {
            wordBuilder.append(' ');
          }
          wordBuilder.append(tokens[i]);

        } else {
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
    System.err.println("Keywords are: " + keywords); //TODO Remove.

    ImmutableSet<String> filteredKeywords =
        ImmutableSet.copyOf(Iterables.filter(keywords, new AnyStringContains(stopWords)));

    return new KeywordGenerator(filteredKeywords);
  }

  @Override
  public void generateText(Context context) {
    context.getBuilder().append(Ordering.natural().onResultOf(LengthGetter.INSTANCE).max(words));
  }

  public ImmutableSet<String> getWords() {
    return words;
  }

  private static class StringContains implements Predicate<String> {

    private final String string;

    public StringContains(String string) {
      this.string = string;
    }

    @Override
    public boolean apply(String input) {
      return string.contains(input);
    }
  }

  private static class AnyStringContains implements Predicate<String> {

    private final ImmutableSet<String> strings;

    public AnyStringContains(ImmutableSet<String> strings) {
      this.strings = strings;
    }

    @Override
    public boolean apply(final String input) {
      return Iterables.any(strings, new StringContains(input));
    }
  }

  private enum LengthGetter implements Function<String, Integer> {
    INSTANCE;

    @Override
    public Integer apply(String input) {
      return input.length();
    }
  }
}
