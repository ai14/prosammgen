package com.github.ai14.prosammgen.textgen;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class KeywordGenerator implements TextGenerator {

  private static final CharMatcher LETTER_MATCHER = CharMatcher.JAVA_LETTER;

  private final ImmutableSet<String> words;

  public KeywordGenerator(ImmutableSet<String> words) {
    this.words = words;
  }

  @Override
  public String generateText(Context context) {
    return Iterables.get(words, context.getRandom().nextInt(words.size()));
  }

  public static KeywordGenerator fromText(ImmutableSet<String> stopWords, String text) {
    return new KeywordGenerator(extractKeywords(stopWords, text));
  }

  static ImmutableSet<String> extractKeywords(ImmutableSet<String> stopWords, String text) {
    for (String stopWord : stopWords) {
      text = text.replaceAll("(?i)\\b" + stopWord + "\\b", "#STOPWORD#");
    }

    return ImmutableSet
        .copyOf(Splitter.on("#STOPWORD#").omitEmptyStrings().trimResults(
            LETTER_MATCHER.negate()).split(text));
  }
}
