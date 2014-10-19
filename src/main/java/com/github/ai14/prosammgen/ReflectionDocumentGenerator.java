package com.github.ai14.prosammgen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import com.github.ai14.prosammgen.textgen.TextGenerator;

import java.util.Random;

public class ReflectionDocumentGenerator {

  private final ImmutableMap<String, TextGenerator> generators;
  private final ImmutableList<String> questions;

  public ReflectionDocumentGenerator(ImmutableMap<String, TextGenerator> generators,
                                     ImmutableList<String> questions) {
    this.generators = generators;
    this.questions = questions;
  }

  public String generateReport(String title, String author, int wordLimit) {
    StringBuilder sb = new StringBuilder();
    Random random = new Random();
    sb.append("\\documentclass{article}\\begin{document}\\title{").append(title)
        .append("}").append("\\author{").append(author).append("}").append("\\maketitle");

    for (String question : questions) {
      sb.append("\\section{").append(question).append("}");
      sb.append(generators.get("PARAGRAPH").generateText(new SimpleContext(random, generators)));
    }

    sb.append("\\end{document}");

    //TODO Replace weird characters with LaTeX formatting. Use a Java library.

    return sb.toString();
  }

  private static class SimpleContext implements TextGenerator.Context {

    private final Random random;
    private final ImmutableMap<String, TextGenerator> generators;

    private SimpleContext(Random random, ImmutableMap<String, TextGenerator> generators) {
      this.random = random;
      this.generators = generators;
    }

    @Override
    public Random getRandom() {
      return random;
    }

    @Override
    public TextGenerator getProduction(String name) {
      return generators.get(name);
    }
  }
}
