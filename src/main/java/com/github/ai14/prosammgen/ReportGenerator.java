package com.github.ai14.prosammgen;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

// TODO Rename class to ReflectionDocumentGenerator.
public class ReportGenerator {
  static private Random random = new Random();
  String keyword;
  private File questions;
  private MarkovTextGenerator markovTextGenerator;
  private Map<String, ArrayList<StringDoublePair>> grammar = new HashMap<>();
  private Synonyms synonyms = new WordNetSynonyms();

  public ReportGenerator(File previousReflectionDocument, File readingMaterial, File questions) {
    // Remember questions for the parsing, as they are used as headlines.
    this.questions = questions;

    // Train markov chain on the reading material.
    this.markovTextGenerator = new MarkovTextGenerator(readingMaterial);

    //TODO Determine personal writing style analysing the previous reflection document.
    // writingStyleAnalyzer.analyzeWritingStyle(previousReflectionDocument);

    // Load grammar.
    try {
      Scanner s = new Scanner(new FileReader(new File("res/grammar")));
      while (s.hasNextLine()) {
        String line = s.nextLine();
        if (line.length() == 0 || line.startsWith("//")) continue;
        String[] words = line.split("\t");
        String lhs = words[0];
        String rhs = line.replaceFirst(lhs + "\t", "");
        if (!grammar.containsKey(lhs)) grammar.put(lhs, new ArrayList<>());
        grammar.get(lhs).add(new StringDoublePair(rhs, 1.0));
      }
      s.close();

      // Make grammar probabilities stochastic.
      for (ArrayList<StringDoublePair> g : grammar.values()) for (StringDoublePair sdp : g) sdp.d /= g.size();
    } catch (IOException e) {
      System.err.println("Could not load grammar file.");
      System.exit(1);
    }
  }

  /**
   * Generate a LaTeX formatted PROSAMM report.
   *
   * @param title  Title of the report.
   * @param author Author of the report.
   * @param words  Suggested word count.
   * @return
   */
  public String generateReport(String title, String author, int words) {
    StringBuilder sb = new StringBuilder();
    sb.append(
            "\\documentclass{article}"
                    + "\\begin{document}"
                    + "\\title{" + title + "}"
                    + "\\author{" + author + "}"
                    + "\\maketitle"
    );

    // Parse questions and generate a paragraph each with the grammar and the trained markov chain.
    try {
      Scanner s = new Scanner(new FileReader(questions));
      while (s.hasNextLine()) {
        String question = s.nextLine();
        sb.append("\\section{" + question + "}");
        //TODO Identify keywords in the question.
        // this.keyword = keywordIdentifier.identifyKeywords(question);
        expand(sb, "#PARAGRAPH");
      }
      s.close();
    } catch (IOException e) {
      System.err.println("Could not generate report. Questions could not be parsed. Make sure that every question is written on a single line each.");
    }
    sb.append("\\end{document}");

    //TODO Replace weird characters with LaTeX formatting. Use a Java library.

    System.err.println(sb.toString());

    return sb.toString();
  }

  private void expand(StringBuilder sb, String rule) {
    String[] symbols = rule.split("\t");
    for (int i = 0; i < symbols.length; i++) {
      String symbol = symbols[i];
      switch (symbol.charAt(0)) {
        case '#': // Production rule
          ArrayList<StringDoublePair> productions = grammar.get(symbol);
          String production = chooseProduction(productions);
          expand(sb, production);
          break;
        case '%': // Predicate
          // Parse out arguments.
          String[] parts = symbol.split("\\(|\\)");
          String command = parts[0];
          String[] arguments = parts[1].split(",");

          // Call predicate function.
          switch (command) {
            case "%MARKOV":
              int sentences = Integer.parseInt(arguments[0]);
              sb.append(markovTextGenerator.getText(sentences, 10)); //TODO Remove second argument?
              break;
            case "%SYNONYM":
              sb.append(synonyms.getSynonym(arguments));
              break;
            case "%KEYWORD":
              sb.append(this.keyword);
              break;
          }
          break;
        default:
          sb.append(symbol);
      }

      // Add space if not followed by punctuation.
      if (i < symbols.length - 1 && !symbols[i + 1].matches("\\.|,|;|:|!|\\?")) sb.append(' ');
    }
  }

  private String chooseProduction(ArrayList<StringDoublePair> productions) {
    double d = random.nextDouble();
    double limit = 0.0;
    for (int i = 0; i < productions.size(); i++) {
      limit += productions.get(i).d;
      if (Double.compare(d, limit) < 0) {
        return productions.get(i).s;
      }
    }
    return productions.get(productions.size() - 1).s;
  }

  private class StringDoublePair {
    String s;
    double d;

    StringDoublePair(String s, double d) {
      this.s = s;
      this.d = d;
    }
  }
}
