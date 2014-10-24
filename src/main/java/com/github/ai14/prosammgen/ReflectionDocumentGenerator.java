package com.github.ai14.prosammgen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import com.google.common.io.Resources;

import com.github.ai14.prosammgen.textgen.KeywordGenerator;
import com.github.ai14.prosammgen.textgen.MarkovTextGenerator;
import com.github.ai14.prosammgen.textgen.SynonymGenerator;
import com.github.ai14.prosammgen.textgen.TextGenerator;
import com.github.ai14.prosammgen.textgen.TextGenerators;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Random;
import java.util.function.Function;

public final class ReflectionDocumentGenerator {

  private final ImmutableSet<String> stopWords;
  private final NLPModel nlp;
  private final WordNet wordNet;
  private ImmutableMap<String, TextGenerator> generators;
  private ImmutableSet<TextSource> textSources;

  public ReflectionDocumentGenerator() throws IOException, InterruptedException, ParseException {

    // Load stop words.
    stopWords = ImmutableSet.copyOf(
        Resources.readLines(Resources.getResource(App.class, "stopwords"), StandardCharsets.UTF_8));

    // Load NLP.
    nlp =
        NLPModel.loadFromDBs(Resources.getResource(App.class, "en-sent.bin"),
                             Resources.getResource(App.class, "en-token.bin"),
                             Resources.getResource(App.class, "en-pos-maxent.bin"));

    // Load grammar.
    // Prepare text sources.
    textSources = ImmutableSet.of(
        new Wikipedia(nlp),
        new ProjectGutenberg(nlp)
    );
    generators = TextGenerators.parseGrammar(
        Resources.readLines(Resources.getResource(App.class, "grammar"), StandardCharsets.UTF_8));

    wordNet = WordNet.load(Resources.getResource(App.class, "wordnet.dat"));
  }

  public String generateReport(String title, String author, ImmutableList<String> questions,
                               Path readingMaterial, int wordCount, int maxWebRequests)
      throws IOException {
    StringBuilder report = new StringBuilder();
    report.append("\\documentclass{article}\\begin{document}\\title{")
        .append(title)
        .append("}\\author{")
        .append(author)
        .append("}\\maketitle");

    // Go through each question and try to answer it.
    for (String question : questions) {

      // Append the question to the report as a headline.
      report.append("\\section{").append(question).append("}");

      // Setup a new Markov chain for the current question.
      MarkovTrainer markovTrainer = new MarkovTrainer();

      // Create keyword generator for the current question and determine keywords, including the longest.
      KeywordGenerator keywordGenerator = KeywordGenerator.withPOSParsing(nlp, stopWords, question);
      ImmutableSet<String> searchTerms = keywordGenerator.getWords();

      // Get training data for the question from text sources.
      long s = 0;
      for (TextSource ts : textSources) {
        ImmutableSet<Path> texts = ts.getTexts(searchTerms, maxWebRequests);
        for (Path p : texts) {
          s += Files.size(p);
        }
        markovTrainer.train(texts);
      }

      // Calculate the ratio between given reading material and additional training texts.
      double ratio = Files.size(readingMaterial) / (double) s;
      int weight = (int) (0.5 / ratio);

      // aim for reading material to be 50% of wiki articles TODO Document properly.
      markovTrainer.train(weight, ImmutableSet.of(readingMaterial));

      // Define grammar macros for the current question.
      ImmutableMap.Builder<String, Function<ImmutableList<String>, TextGenerator>> macroBuilder = ImmutableMap.builder();
      macroBuilder.put("MARKOV", n -> new MarkovTextGenerator(markovTrainer, Integer.parseInt(n.get(0))));
      macroBuilder.put("SYNONYM", words -> new SynonymGenerator(wordNet, words));
      macroBuilder.put("KEYWORD", x -> keywordGenerator);

      // Expand grammar and generate some text.
      int remaining = wordCount / questions.size();
      while (remaining > 0) {
        StringBuilder paragraphBuilder = new StringBuilder();
        generators.get("PARAGRAPH").generateText(new SimpleContext(generators, macroBuilder.build(), paragraphBuilder));

        String paragraph = paragraphBuilder.toString();

        report.append(escapeLaTeXSpecialChars(paragraph));
        report.append("\n\n");

        remaining -= paragraph.split("\\s+").length;
      }
    }

    report.append("\\end{document}");

    return report.toString();
  }

  private String escapeLaTeXSpecialChars(String s) {
    StringBuilder sb = new StringBuilder(s.length());

    for (int i = 0; i < s.length(); ++i) {
      char c = s.charAt(i);
      switch (c) {
        case '{':
        case '}':
        case '#':
        case '$':
        case '%':
        case '&':
        case '_':
          sb.append("\\");
          sb.append(c);
          break;
        case '^':
          sb.append("\\textasciicircum{}");
          break;
        case '~':
          sb.append("\\textasciitilde{}");
          break;
        case '\\':
          sb.append("\\textbackslash{}");
          break;
        default:
          sb.append(c);
      }
    }

    return sb.toString();
  }

  private static class SimpleContext implements TextGenerator.Context {

    private final Random random = new Random();
    private final ImmutableMap<String, TextGenerator> generators;
    private final ImmutableMap<String, Function<ImmutableList<String>, TextGenerator>> macros;
    private final StringBuilder builder;

    private SimpleContext(ImmutableMap<String, TextGenerator> generators,
                          ImmutableMap<String, Function<ImmutableList<String>, TextGenerator>> macros,
                          StringBuilder builder) {
      this.generators = generators;
      this.macros = macros;
      this.builder = builder;
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

    @Override
    public StringBuilder getBuilder() {
      return builder;
    }

  }
}
