package com.github.ai14.prosammgen;

import com.github.ai14.prosammgen.textgen.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Random;
import java.util.function.Function;

public final class ReflectionDocumentGenerator {
  private final ImmutableSet<String> stopWords;
  private final NLPModel nlp;
  private ImmutableMap<String, TextGenerator> generators;
  private Synonyms synonyms;

  public ReflectionDocumentGenerator() throws IOException, ParseException {

    // Load stop words.
    stopWords = ImmutableSet.copyOf(Files.readAllLines(Paths.get("res/stopwords")));

    // Load NLP.
    nlp = NLPModel.loadFromDBs(Paths.get("res/en-sent.bin"), Paths.get("res/en-token.bin"), Paths.get("res/en-pos-maxent.bin"));

    // Load synonyms database.
    synonyms = new WordNetSynonyms();

    // Load grammar.
    generators = TextGenerators.parseGrammar(Files.readAllLines(Paths.get("res/grammar")));
  }

  public String generateReport(String title, String author, ImmutableList<String> questions, Path readingMaterial, int wordCount) throws IOException {
    StringBuilder report = new StringBuilder();
    report.append("\\documentclass{article}\\usepackage[utf8]{inputenc}\\begin{document}\\title{")
            .append(title)
            .append("}\\author{")
            .append(author)
            .append("}\\maketitle");

    // Go through each question and try to answer it.
    for (String question : questions) {

      // Append the question to the report as a headline.
      report.append("\\section{").append(question).append("}");

      // Determine keywords for the question.
      KeywordGenerator keywordGenerator = KeywordGenerator.withPOSParsing(nlp, stopWords, question);

      // Generate a Markov chain for the current question.
      MarkovChain markovChain = generateMarkovChain(question, readingMaterial);

      // Define grammar macros for the current question.
      ImmutableMap.Builder<String, Function<ImmutableList<String>, TextGenerator>> macroBuilder = ImmutableMap.builder();
      macroBuilder.put("MARKOV", n -> new MarkovTextGenerator(markovChain, Integer.parseInt(n.get(0))));
      macroBuilder.put("SYNONYM", words -> new SynonymGenerator(words, synonyms));
      macroBuilder.put("KEYWORD", x -> keywordGenerator);

      int remaining = wordCount / questions.size();
      while (remaining > 0) {
        String paragraph = generators.get("PARAGRAPH").generateText(new SimpleContext(generators, macroBuilder.build()));
        report.append(paragraph);
        report.append("\n\n");
        remaining -= paragraph.split("\\s+").length;
      }
    }

    report.append("\\end{document}");

    return report.toString();
  }

  private MarkovChain generateMarkovChain(String question, Path readingMaterial) {
    MarkovChain markovChain = new MarkovChain();

    try {
      // Get additional training data for the question from text sources (Wikipedia articles).
      //TODO Don't call with all keywords, use only the longest for example (or whatever is emph{} in the report.
      KeywordGenerator keywordGenerator = KeywordGenerator.withPOSParsing(nlp, stopWords, question);
      ImmutableSet<String> searchTerms = keywordGenerator.getWords();
      TextSource wa = new WikipediaArticles(100, searchTerms.toArray(new String[searchTerms.size()]));
      Path[] trainingTexts = wa.getTexts();

      // Calculate the ratio of amount of reading material per additional training text.
      long waSize = 0, rmSize = Files.size(readingMaterial);
      for (Path p : trainingTexts) waSize += Files.size(p);
      int weight = (int) (0.5 / (rmSize / ((double) waSize)));

      // Train the Markov chain.
      markovChain.train(weight, readingMaterial); // aim for reading material to be 50% of wiki articles TODO Document properly.
      markovChain.train(trainingTexts);
    } catch (IOException e) {
      System.err.println("Could not train a Markov chain.");
      System.exit(-1);
    }

    return markovChain;
  }

  private static class SimpleContext implements TextGenerator.Context {
    private static final Random random = new Random();
    private final ImmutableMap<String, TextGenerator> generators;
    private final ImmutableMap<String, Function<ImmutableList<String>, TextGenerator>> macros;

    private SimpleContext(ImmutableMap<String, TextGenerator> generators, ImmutableMap<String, Function<ImmutableList<String>, TextGenerator>> macros) {
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
