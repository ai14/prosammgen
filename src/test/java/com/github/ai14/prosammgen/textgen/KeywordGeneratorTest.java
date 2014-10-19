package com.github.ai14.prosammgen.textgen;

import com.google.common.collect.ImmutableSet;

import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

public class KeywordGeneratorTest {

  @Test
  public void testExample() throws Exception {
    ImmutableSet<String> stopwords =
        ImmutableSet.copyOf(Files.readAllLines(Paths.get("res/stopwords")));
    ImmutableSet<String> keywords = KeywordGenerator.extractKeywords(
        stopwords, "Why do these intended learning outcomes exist?  Who has an interest?");
    System.out.println(keywords);
  }
}
