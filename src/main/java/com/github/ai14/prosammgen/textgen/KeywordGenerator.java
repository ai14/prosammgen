package com.github.ai14.prosammgen.textgen;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

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

  public KeywordGenerator(ImmutableSet<String> words) {
    this.words = words;
  }

  @Override
  public String generateText(Context context) {
    return Iterables.get(words, context.getRandom().nextInt(words.size()));
  }

  public ImmutableSet<String> getWords() {
    return words;
  }

  public static KeywordGenerator fromText(ImmutableSet<String> stopWords, String text) {
    return new KeywordGenerator(extractKeywords(stopWords, text));
  }

  static ImmutableSet<String> extractKeywords(ImmutableSet<String> stopWords, String text) {
    for (String stopWord : stopWords) {
      text = text.replaceAll("(?i)\\b" + stopWord + "\\b", "#STOPWORD#");
    }

    Iterable<String> splits = STOPWORD_SPLITTER.split(text);

    Iterable<String> realWords =
        Iterables.concat(Iterables.transform(splits, CLEAN_SPLITTER::split));
    ImmutableSet<String> keywords = ImmutableSet.copyOf(realWords);
    System.out.println("Keywords are: " + keywords);
    return keywords;
  }
}
