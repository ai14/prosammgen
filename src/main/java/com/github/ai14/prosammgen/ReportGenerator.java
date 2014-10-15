package com.github.ai14.prosammgen;

import java.util.*;

public class ReportGenerator {
  private String heading = "prosamm report heading";

  private Random rand;

  private class StrDblPair {
    String str;
    double dbl;

    StrDblPair(String str, double dbl) {
      this.str = str;
      this.dbl = dbl;
    }
  }

  private Map<String, ArrayList<StrDblPair>> grammar = new HashMap<>();

  public ReportGenerator() {
    rand = new Random(System.currentTimeMillis());
    initGrammar();
  }

  public ProsammReport generateReport(List<String> questions) {
    ProsammReport report = new ProsammReport(heading);

    for (String question : questions) {

      // report.addQuestionAnswer(
    }

    return report;
  }

  private void initGrammar() {
    grammar.put("#PARAGRAPH", new ArrayList<>());
    grammar.get("#PARAGRAPH").add(new StrDblPair("#INTRO #MIDDLE #CONCLUSION", 1.0));

    grammar.put("#INTRO", new ArrayList<>());
    grammar.get("#INTRO").add(new StrDblPair("#INITSENTENCE", 1.0));

    grammar.put("#MIDDLE", new ArrayList<>());
    grammar.get("#MIDDLE").add(new StrDblPair("This is some markov generated crap. This will be trained on the input files.", 1.0));

    grammar.put("#CONCLUSION", new ArrayList<>());
    grammar.get("#CONCLUSION").add(new StrDblPair("#CONCLUSIONSTART This is the rest of the conclusion.", 1.0));

    grammar.put("#INITSENTENCE", new ArrayList<>());
    grammar.get("#INITSENTENCE").add(new StrDblPair("#INITOPINION #KEYWORD #OPINIONEND", 1.0));
    // add more here, change probs

    // add more production rules to this
    grammar.put("#INITOPINION", new ArrayList<>());
    grammar.get("#INITOPINION").add(new StrDblPair("I feel like", 0.33));
    grammar.get("#INITOPINION").add(new StrDblPair("I would argue that", 0.33));
    grammar.get("#INITOPINION").add(new StrDblPair("It's difficult to say that", 0.33));

    // TODO: this should be retrieved from the questions, change per paragraph?
    grammar.put("#KEYWORD", new ArrayList<>());
    grammar.get("#KEYWORD").add(new StrDblPair("intended learning outcomes", 1.0));

    grammar.put("#OPINIONEND", new ArrayList<>());
    grammar.get("#OPINIONEND").add(new StrDblPair("is very #POSORNEG for today's society.", 0.33));
    grammar.get("#OPINIONEND").add(new StrDblPair("is a very #INTERESTING topic.", 0.33));
    grammar.get("#OPINIONEND").add(new StrDblPair("is something that I'm passionate about.", 0.33));

    grammar.put("#POSORNEG", new ArrayList<>());
    grammar.get("#POSORNEG").add(new StrDblPair("positive", 0.5));
    grammar.get("#POSORNEG").add(new StrDblPair("negative", 0.5));

    grammar.put("#INTERESTING", new ArrayList<>());
    grammar.get("#INTERESTING").add(new StrDblPair("interesting", 0.33));
    grammar.get("#INTERESTING").add(new StrDblPair("fascinating", 0.33));
    grammar.get("#INTERESTING").add(new StrDblPair("intriguing", 0.33));

    grammar.put("#CONCLUSIONSTART", new ArrayList<>());
    grammar.get("#CONCLUSIONSTART").add(new StrDblPair("To summarize I would argue that #KEYWORD is something #POSORNEG", 1.0));
  }

  private void setKeywords(String[] keywords) {
    // TODO: update the grammar with these keywords, used for each paragraph
  }

  private void expand(StringBuilder sb, String rule) {
    String[] words = rule.split(" ");

    for (String word : words) {
      if (word.startsWith(("#"))) {

        ArrayList<StrDblPair> productions = grammar.get(word);
        String production = chooseProduction(productions);

        expand(sb, production);

      } else {
        sb.append(word + " ");
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

  public void testGenerateParagraph() {
    System.out.println(generateParagraph("asasdadsa"));
  }
}
