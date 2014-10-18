package com.github.ai14.prosammgen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

// TODO Rename class to ReflectionDocumentGenerator.
public class ReportGenerator {
  static private Random rand = new Random();
  private File previousReflectionDocument, readingMaterial, questions;
  private MarkovTextGenerator mtg;
  private Map<String, ArrayList<StrDblPair>> grammar;
  private Synonyms synonyms;

  public ReportGenerator(File previousReflectionDocument, File readingMaterial, File questions) {
    this.previousReflectionDocument = previousReflectionDocument;
    this.readingMaterial = readingMaterial;
    this.questions = questions;
    this.synonyms = new WordNetSynonyms();
    this.grammar = new HashMap<>();

    this.mtg = new MarkovTextGenerator(new File[]{readingMaterial}); //TODO Train on questions as well?

    //TODO Determine keywords in the questions by cross-referencing with the reading material.

    //TODO Determine personal writing style by statistically analysing the previous reflection document.

    File grammarFile = new File("res/grammar");
    try {
      loadGrammar(grammarFile);
    } catch (IOException e) {
      System.err.println("failed to load grammar");
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
    //TODO Latexify title and author (i.e. escape characters etc.).
    sb.append(
            "\\documentclass{article}"
                    + "\\begin{document}"
                    + "\\title{" + title + "}"
                    + "\\author{" + author + "}"
                    + "\\maketitle"
    );

    // Parse questions and generate a paragraph each with the grammar and the trained markov chain.
    try {
      BufferedReader br = new BufferedReader(new FileReader(questions));
      String question;
      while ((question = br.readLine()) != null) {
        sb.append("\\section{" + question + "}");
        sb.append(generateText()); //TODO generateText should try to match the suggested word count.
      }
      br.close();
    } catch (IOException e) {
      System.err.println("Could not generate report. Questions could not be parsed. Make sure that every question is written on a single line each.");
    }

    sb.append("\\end{document}");

    return sb.toString();
  }

  private void setKeywords(String[] keywords) {
    // TODO: update the grammar with these keywords, used for each paragraph
  }

  private void expand(StringBuilder sb, String rule) {
    String[] symbols = rule.split("\\t");
    for (int i = 0; i < symbols.length; i++) {
      String symbol = symbols[i];
      switch (symbol.charAt(0)) { //TODO Carl fixar.
        case '#': // Production rule
          ArrayList<StrDblPair> productions = grammar.get(symbol);
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
              int goalWordcount = Integer.parseInt(arguments[0]); //TODO Fix mismatch between grammar readme and variable naming. What is goalWordcount?
              int avgSentenceLen = Integer.parseInt(arguments[1]); //TODO Fix mismatch between grammar readme and variable naming. What is avgSentenceLen?
              sb.append(mtg.getText(goalWordcount, avgSentenceLen));
              break;
            case "%SYNONYM":
              sb.append(synonyms.getSynonym(arguments));
              break;
            //TODO case "%KEYWORD(sentence)"
          }
          break;
        default:
          sb.append(symbol);
      }

      // Add space if not followed by punctuation.
      if (i < symbols.length-1 && !symbols[i+1].matches("\\.|,|;|:|!|\\?")) sb.append(' ');
    }
  }

  private String chooseProduction(ArrayList<StrDblPair> productions) {
    double d = rand.nextDouble();
    double upperLimit = 0.0;
    for (int i = 0; i < productions.size(); i++) {
      upperLimit = upperLimit + productions.get(i).dbl;

      if (Double.compare(d, upperLimit) < 0) {
        return productions.get(i).str;
      }
    }

    return productions.get(productions.size() - 1).str;
  }

  private String generateText() {
    StringBuilder sb = new StringBuilder();
    expand(sb, "#PARAGRAPH");
    return sb.toString();
  }

  private void loadGrammar(File grammarFile) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(grammarFile));

    String line;
    while ((line = br.readLine()) != null) {
      if (line.length() == 0 || line.startsWith("//")) continue;
      String[] words = line.split("\\t");
      String lhs = words[0];
      String rhs = line.replaceFirst(lhs, "");
      if (!grammar.containsKey(lhs)) grammar.put(lhs, new ArrayList<>());
      grammar.get(lhs).add(new StrDblPair(rhs, 1.0)); // Initial probability is always 1.0.
    }

    // Make stochastic.
    for (Map.Entry<String, ArrayList<StrDblPair>> grammarEntry : grammar.entrySet()) {
      int n = grammarEntry.getValue().size();

      for (StrDblPair sdp : grammarEntry.getValue()) {
        sdp.dbl = 1.0 / n;
      }
    }

    br.close();
  }

  private class StrDblPair {
    String str;
    double dbl;

    StrDblPair(String str, double dbl) {
      this.str = str;
      this.dbl = dbl;
    }
  }
}
