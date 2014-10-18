package com.github.ai14.prosammgen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

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
   * @return
   */
  public String generateReport() {
    StringBuilder sb = new StringBuilder();
    sb.append(
            "\\documentclass{article}"
                    + "\\begin{document}"
                    + "\\title{My Prosamm Report}"
                    + "\\author{Author}"
                    + "\\maketitle"
    );

    // Parse questions and generate a paragraph each with the grammar and the trained markov chain.
    try {
      BufferedReader br = new BufferedReader(new FileReader(questions));
      String question;
      while ((question = br.readLine()) != null) {
        sb.append("\\section{" + question + "}");
        sb.append(generateText());
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
    String[] words = rule.split(" ");

    for (String word : words) {
      switch (word.charAt(0)) {

        case '#':
          ArrayList<StrDblPair> productions = grammar.get(word);
          String production = chooseProduction(productions);
          expand(sb, production);
          break;

        case '$':
          sb.append(synonyms.getSynonym(word.substring(1)) + " ");
          break;

        case '@':
          // TODO: use this for punctuation characters later

          break;

        case '%':
          String[] parts = word.substring(1).split("\\(");
          String command = parts[0];
          String argumentsStr = parts[1].substring(0, parts[1].length() - 1); // remove the closing paranthesis
          String[] arguments = argumentsStr.split(",");

          if (command.equals("MARKOV")) {
            int numSentences = Integer.parseInt(arguments[0]);
            int avgSentenceLen = Integer.parseInt(arguments[1]);
            sb.append(mtg.getText(numSentences, avgSentenceLen) + " ");
          }

          break;

        default:
          sb.append(word);
          if (word.length() > 0) {
            sb.append(" ");
          }
      }
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
      if (line.length() == 0 || line.startsWith("//")) {
        continue;
      }

      String[] words = line.split(" ");
      String productionLHS = words[0];

      if (!grammar.containsKey(productionLHS)) {
        grammar.put(productionLHS, new ArrayList<>());
      }

      StringBuilder sb = new StringBuilder();
      for (int i = 1; i < words.length; i++) {
        sb.append(words[i] + " ");
      }
      sb.deleteCharAt(sb.length() - 1); // remove last space
      String productionRHS = sb.toString();

      // Add probability 1.0 first, this changes later to make it stochastic
      grammar.get(productionLHS).add(new StrDblPair(productionRHS, 1.0));
    }

    // now make it stochastic
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
