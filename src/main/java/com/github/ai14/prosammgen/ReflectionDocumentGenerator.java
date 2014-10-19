package com.github.ai14.prosammgen;

import com.github.ai14.prosammgen.textgen.KeywordGenerator;
import com.github.ai14.prosammgen.textgen.TextGenerator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.io.IOException;
import java.util.Random;
import java.util.function.Function;

public class ReflectionDocumentGenerator {

  private final ImmutableMap<String, TextGenerator> generators;
  private final ImmutableList<String> questions;
  private final ImmutableMap<String, Function<ImmutableList<String>, TextGenerator>> baseMacros;
  private final NLPModel nlpModel;
  private final ImmutableSet<String> stopWords;

  public ReflectionDocumentGenerator(ImmutableMap<String, TextGenerator> generators,
                                     ImmutableList<String> questions,
                                     ImmutableMap<String, Function<ImmutableList<String>, TextGenerator>> baseMacros,
                                     NLPModel nlpModel, ImmutableSet<String> stopWords) {
    this.generators = generators;
    this.questions = questions;
    this.baseMacros = baseMacros;
    this.nlpModel = nlpModel;
    this.stopWords = stopWords;
  }

  public String generateReport(String title, String author, int wordLimit) throws IOException {
    StringBuilder sb = new StringBuilder();
    Random random = new Random();
    sb.append("\\documentclass{article}\\usepackage[utf8]{inputenc}\\begin{document}\\title{")
            .append(title)
            .append("}").append("\\author{").append(author).append("}").append("\\maketitle");

    int answerWordCount = wordLimit / questions.size();
    for (String question : questions) {
      sb.append("\\section{").append(question).append("}");

      KeywordGenerator keywordGenerator = KeywordGenerator.withPOSParsing(nlpModel, stopWords, question);

      ImmutableMap.Builder<String, Function<ImmutableList<String>, TextGenerator>> macroBuilder =
              ImmutableMap.builder();

      macroBuilder.putAll(baseMacros);
      macroBuilder.put("KEYWORD", x -> keywordGenerator);
      int remaining = answerWordCount;

      while (remaining > 0) {
        String paragraph = generators.get("PARAGRAPH").generateText(new SimpleContext(random, generators, macroBuilder.build()));
        sb.append(paragraph);
        sb.append("\n\n");
        remaining -= paragraph.split("\\s+").length;
      }
    }

    sb.append("\\end{document}");

    return sb.toString();
  }

  private static class SimpleContext implements TextGenerator.Context {

    private final Random random;
    private final ImmutableMap<String, TextGenerator> generators;
    private final ImmutableMap<String, Function<ImmutableList<String>, TextGenerator>> macros;

    private SimpleContext(Random random, ImmutableMap<String, TextGenerator> generators,
                          ImmutableMap<String, Function<ImmutableList<String>, TextGenerator>> macros) {
      this.random = random;
      this.generators = generators;
      this.macros = macros;
    }

    @Override
    public Random getRandom() {
      return random;
    }

    @Override
    public TextGenerator getProduction(String name) {
      return generators.get(name);
    }

    @Override
    public Function<ImmutableList<String>, TextGenerator> getMacro(String name) {
      return macros.get(name);
    }
  }
}
