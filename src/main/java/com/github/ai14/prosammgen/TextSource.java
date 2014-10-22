package com.github.ai14.prosammgen;

import com.google.common.collect.ImmutableSet;
import opennlp.tools.sentdetect.SentenceDetectorME;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

public abstract class TextSource {
  public static final Path CACHE = Paths.get("cache");
  protected final SentenceDetectorME sentenceDetector; //TODO Don't use protected.
  protected final Pattern runningTextPattern; //TODO Don't use protected.

  public TextSource(NLPModel nlp, Path cache) throws IOException {
    runningTextPattern = Pattern.compile("^([A-Z]\\w* ([\\w(),:'‘’\\-%/]* ){2,}([\\w(),:'‘’-]*[.,!?]) ){2,}", Pattern.MULTILINE);
    sentenceDetector = new SentenceDetectorME(nlp.getSentenceModel());
    if (!Files.exists(CACHE)) Files.createDirectory(CACHE);
    if (!Files.exists(cache)) Files.createDirectory(cache);
  }

  public abstract ImmutableSet<Path> getTexts(ImmutableSet<String> searchTerms, int resultsLimit) throws IOException;
}
