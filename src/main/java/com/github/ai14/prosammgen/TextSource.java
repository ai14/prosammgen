package com.github.ai14.prosammgen;

import com.google.common.collect.ImmutableSet;
import opennlp.tools.sentdetect.SentenceDetectorME;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class TextSource {
  private static final Pattern paragraphPattern = Pattern.compile("^([A-Z]\\w* ([\\w(),:'‘’\\-%/]* ){2,}([\\w(),:'‘’-]*[.,!?]) ){2,}", Pattern.MULTILINE);
  protected final File cache;
  protected final Pattern contentPattern;
  private final SentenceDetectorME sentenceDetector;

  public TextSource(String outputDirectory, String cacheDirectory, Pattern contentPattern, SentenceDetectorME sentenceDetector) throws IOException {
    this.contentPattern = contentPattern;
    this.sentenceDetector = sentenceDetector;
    this.cache = new File(outputDirectory, cacheDirectory);
    if (!cache.exists()) cache.mkdirs();
  }

  /**
   * Return a set of paths containing running texts relevant to the search terms.
   */
  public abstract ImmutableSet<File> getTexts(ImmutableSet<String> searchTerms, int resultsLimit) throws IOException;

  /**
   * Clean a text into only containing grammar-correct running text.
   */
  protected String extractRunningText(String content) {
    StringBuilder sb = new StringBuilder();

    // Find running text (get rid of meta data and so on).
    Matcher m = paragraphPattern.matcher(content);
    while (m.find()) {
      String paragraph = m.group();

      // Only keep grammar correct sentences (using OpenNLP).
      for (String sentence : sentenceDetector.sentDetect(paragraph)) {
        sb.append(sentence);
        sb.append(" ");
      }
      sb.append("\n");
    }

    return sb.toString();
  }
}
