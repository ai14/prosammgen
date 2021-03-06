package com.github.ai14.prosammgen;

import com.github.ai14.prosammgen.textgen.*;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import opennlp.tools.sentdetect.SentenceDetectorME;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Random;

public final class ReflectionDocumentGenerator {

  private final ImmutableSet<String> stopWords;
  private final NLPModel nlp;
  private final WordNet wordNet;
  private ImmutableMap<String, TextGenerator> generators;
  private ImmutableSet<TextSource> textSources;

  public ReflectionDocumentGenerator(WordNet wordNet, String outputDirectory) throws IOException, InterruptedException, ParseException {
    this.wordNet = wordNet;

    // Load stop words.
    stopWords = ImmutableSet.copyOf(Resources.readLines(Resources.getResource(App.class, "stopwords"), Charsets.UTF_8));

    // Load NLP.
    nlp = NLPModel.loadFromDBs(Resources.getResource(App.class, "en-sent.bin"), Resources.getResource(App.class, "en-token.bin"), Resources.getResource(App.class, "en-pos-maxent.bin"));

    // Prepare text sources.
    SentenceDetectorME sentenceDetector = new SentenceDetectorME(nlp.getSentenceModel());
    textSources = ImmutableSet.of(
            new Wikipedia(sentenceDetector, outputDirectory),
            new ProjectGutenberg(sentenceDetector, outputDirectory)
    );

    // Load grammar.
    generators = TextGenerators.parseGrammar(Resources.readLines(Resources.getResource(App.class, "grammar"), Charsets.UTF_8));
  }

  public String generateReport(String title, String author, ImmutableList<String> questions, File readingMaterial, int wordCount, int maxWebRequests) throws IOException {
    StringBuilder report = new StringBuilder();
    report
            .append("\\documentclass{article}\\begin{document}\\title{")
            .append(escapeLaTeXSpecialChars(title))
            .append("}\\author{")
            .append(escapeLaTeXSpecialChars(author))
            .append("}\\maketitle");

    // Go through each question and try to answer it.
    for (String question : questions) {

      // Append the question to the report as a headline.
      report.append("\\section{").append(escapeLaTeXSpecialChars(question)).append("}");

      // Setup a new Markov chain for the current question.
      final MarkovTrainer markovTrainer = new MarkovTrainer();

      // Create keyword generator for the current question and determine keywords, including the longest.
      KeywordGenerator keywordGenerator = KeywordGenerator.withPOSParsing(nlp, stopWords, question);
      ImmutableSet<String> searchTerms = keywordGenerator.getWords();

      // Get training data for the question from text sources.
      long s = 0;
      for (TextSource ts : textSources) {
        ImmutableSet<File> texts = ts.getTexts(searchTerms, maxWebRequests);
        for (File p : texts) {
          s += p.length();
        }
        markovTrainer.train(texts);
      }

      // Calculate the ratio between given reading material and additional training texts.
      double ratio = readingMaterial.length() / (double) s;
      int weight = (int) (0.5 / ratio);

      // aim for reading material to be 50% of wiki articles TODO Document properly.
      markovTrainer.train(weight, ImmutableSet.of(readingMaterial));

      // Define grammar macros for the current question.
      ImmutableMap.Builder<String, Function<? super ImmutableList<String>, ? extends TextGenerator>> macroBuilder = ImmutableMap.builder();
      macroBuilder.put("MARKOV", new Function<ImmutableList<String>, TextGenerator>() {
        public TextGenerator apply(ImmutableList<String> n) {
          return new MarkovTextGenerator(markovTrainer, Integer.parseInt(n.get(0)));
        }
      });
      macroBuilder.put("SYNONYM", new Function<ImmutableList<String>, TextGenerator>() {
        public TextGenerator apply(ImmutableList<String> words) {
          return new SynonymGenerator(wordNet, words);
        }
      });
      macroBuilder.put("KEYWORD", Functions.constant(keywordGenerator));

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
    private final ImmutableMap<String, Function<? super ImmutableList<String>, ? extends TextGenerator>> macros;
    private final StringBuilder builder;

    private SimpleContext(ImmutableMap<String, TextGenerator> generators,
                          ImmutableMap<String, Function<? super ImmutableList<String>, ? extends TextGenerator>> macros,
                          StringBuilder builder) {
      this.generators = generators;
      this.macros = macros;
      this.builder = builder;
    }

    public Random getRandom() {
      return random;
    }

    public TextGenerator getProduction(String name) {
      return generators.get(name);
    }

    public Function<? super ImmutableList<String>, ? extends TextGenerator> getMacro(String name) {
      return macros.get(name);
    }

    public StringBuilder getBuilder() {
      return builder;
    }

  }
}
