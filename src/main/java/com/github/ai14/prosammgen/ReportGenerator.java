package com.github.ai14.prosammgen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

// TODO Rename class to ReflectionDocumentGenerator.
public class ReportGenerator {
  private MarkovTextGenerator mtg;
  private Random rand;
  private Map<String, ArrayList<StrDblPair>> grammar = new HashMap<>();

  public ReportGenerator(File[] files) {
    this.mtg = new MarkovTextGenerator(files);
    this.rand = new Random(System.currentTimeMillis());

    File grammarFile = new File("res/grammar");
    try {
      loadGrammar(grammarFile);
    } catch (IOException e) {
      System.err.println("failed to load grammar");
      System.exit(1);
    }
  }

  public void generateReport(String filepath, List<String> questions) {

  }

  public void testGenerateParagraph() {
    System.out.println(generateParagraph("asasdadsa"));
  }

  private void setKeywords(String[] keywords) {
    // TODO: update the grammar with these keywords, used for each paragraph
  }

  private void expand(StringBuilder sb, String rule) {
    String[] words = rule.split(" ");

    for (String word : words) {
      if (word.startsWith(("#"))) {

        // TODO: create a paragraph class that we append to instead that fixes periods and stuff
        String dot = "";
        if (word.endsWith(".")) {
          word = word.substring(0, word.length() - 1);
          dot = ".";
        }

        ArrayList<StrDblPair> productions = grammar.get(word);
        String production = chooseProduction(productions);

        expand(sb, production);
        sb.append(dot);

      } else {
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

  private String generateParagraph(String question) {
    StringBuilder sb = new StringBuilder();

    // update grammar with question?

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
