package com.github.ai14.prosammgen.textgen;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class KeywordGenerator implements TextGenerator {

  private static final CharMatcher LETTER_MATCHER = CharMatcher.JAVA_LETTER;
  private static final Splitter STOPWORD_SPLITTER =
      Splitter.on("#STOPWORD#").omitEmptyStrings().trimResults(
          LETTER_MATCHER.negate());
  private static final Splitter CLEAN_SPLITTER =
      Splitter
          .on(CharMatcher.JAVA_LETTER_OR_DIGIT.or(CharMatcher.INVISIBLE).negate()).trimResults()
          .omitEmptyStrings();

  private final ImmutableSet<String> words;

  private KeywordGenerator(ImmutableSet<String> words) {
    this.words = words;
  }

  public static KeywordGenerator withPOSParsing(Path sentDb, Path tokenDb, Path posDb,
                                                ImmutableSet<String> stopWords,
                                                String body)
      throws IOException {
    final SentenceModel sentenceModel;
    try (InputStream is = Files.newInputStream(sentDb)) {
      sentenceModel = new SentenceModel(is);
    }

    final TokenizerModel tokenizerModel;
    try (InputStream is = Files.newInputStream(tokenDb)) {
      tokenizerModel = new TokenizerModel(is);
    }

    POSModel posModel;
    try (InputStream is = Files.newInputStream(posDb)) {
      posModel = new POSModel(is);
    }

    SentenceDetector sentenceDetector = new SentenceDetectorME(sentenceModel);
    String[] sentences = sentenceDetector.sentDetect(body);

    ImmutableSet.Builder<String> resultBuilder = ImmutableSet.builder();

    for (String sentence : sentences) {
      Tokenizer tokenizer = new TokenizerME(tokenizerModel);
      String[] tokens = tokenizer.tokenize(sentence);

      POSTagger posTagger = new POSTaggerME(posModel);
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
    return Iterables.get(words, context.getRandom().nextInt(words.size()));
  }

  public ImmutableSet<String> getWords() {
    return words;
  }
}
