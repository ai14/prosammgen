package com.github.ai14.prosammgen;

import java.io.File;
import java.util.*;

// TODO Rename class to ReflectionDocumentGenerator.
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

  public ReportGenerator(File[] files) {
    MarkovTextGenerator mtg = new MarkovTextGenerator(files);
    rand = new Random(System.currentTimeMillis());
    initGrammar();
  }

  private void initGrammar() {
    // TODO: add support for "productions" starting with $
    // these should be looked up for possible synonyms with the Synonyms class
    // This means we should change some productions that are simply synonyms to use this instead

    grammar.put("#PARAGRAPH", new ArrayList<>());
    grammar.get("#PARAGRAPH").add(new StrDblPair("#INTRO #MIDDLE #CONCLUSION", 1.0));

    grammar.put("#INTRO", new ArrayList<>());
    grammar.get("#INTRO").add(new StrDblPair("#INITSENTENCE #INITFOLLOWUP", 1.0));

    grammar.put("#INITSENTENCE", new ArrayList<>());
    grammar.get("#INITSENTENCE").add(new StrDblPair("#INITOPINION #KEYWORD #OPINIONEND", 1.0));
    // add more here, change probs

    // add more production rules to this
    grammar.put("#INITOPINION", new ArrayList<>());
    grammar.get("#INITOPINION").add(new StrDblPair("#METAPHORICALLY I feel like", 0.25));
    grammar.get("#INITOPINION").add(new StrDblPair("#METAPHORICALLY I would argue that", 0.25));
    grammar.get("#INITOPINION").add(new StrDblPair("#METAPHORICALLY It's difficult to say that", 0.25));
    grammar.get("#INITOPINION").add(new StrDblPair("#METAPHORICALLY my experiences point to the fact that", 0.25));

    grammar.put("#INITFOLLOWUP", new ArrayList<>());
    grammar.get("#INITFOLLOWUP").add(new StrDblPair("This means that #FAMOUSPERSON was wrong.", 0.33));
    grammar.get("#INITFOLLOWUP").add(new StrDblPair("For this reason I #THINK that #FAMOUSPERSON has been proven correct.", 0.33));
    grammar.get("#INITFOLLOWUP").add(new StrDblPair("What #FAMOUSPERSON had to say about this is obviously also #RELATED.", 0.33));

    grammar.put("#MIDDLE", new ArrayList<>());
    grammar.get("#MIDDLE").add(new StrDblPair("This is some markov generated crap. This will be trained on the input files.", 1.0));

    grammar.put("#CONCLUSION", new ArrayList<>());
    grammar.get("#CONCLUSION").add(new StrDblPair("#CONCLUSIONSTART This is the rest of the conclusion.", 1.0));

    // TODO: this should be retrieved from the questions, change per paragraph?
    grammar.put("#KEYWORD", new ArrayList<>());
    grammar.get("#KEYWORD").add(new StrDblPair("intended learning outcomes", 1.0));

    grammar.put("#OPINIONEND", new ArrayList<>());
    grammar.get("#OPINIONEND").add(new StrDblPair("is very #POSORNEG for today's society.", 0.33));
    grammar.get("#OPINIONEND").add(new StrDblPair("is a very #INTERESTING topic.", 0.33));
    grammar.get("#OPINIONEND").add(new StrDblPair("is something that I'm passionate about.", 0.33));

    grammar.put("#METAPHORICALLY", new ArrayList<>());
    grammar.get("#METAPHORICALLY").add(new StrDblPair("Metaphorically", 0.25));
    grammar.get("#METAPHORICALLY").add(new StrDblPair("Figuratively", 0.25));
    grammar.get("#METAPHORICALLY").add(new StrDblPair("Illustratively", 0.25));
    grammar.get("#METAPHORICALLY").add(new StrDblPair("", 0.25));

    // TODO: use synonyms
    grammar.put("#THINK", new ArrayList<>());
    grammar.get("#THINK").add(new StrDblPair("think", 0.25));
    grammar.get("#THINK").add(new StrDblPair("conceive", 0.25));
    grammar.get("#THINK").add(new StrDblPair("consider", 0.25));
    grammar.get("#THINK").add(new StrDblPair("believe", 0.25));

    grammar.put("#FAMOUSPERSON", new ArrayList<>());
    grammar.get("#FAMOUSPERSON").add(new StrDblPair("Albert Einstein", 0.2));
    grammar.get("#FAMOUSPERSON").add(new StrDblPair("Thomas Kuhn", 0.2));
    grammar.get("#FAMOUSPERSON").add(new StrDblPair("Immanuel Kant", 0.2));
    grammar.get("#FAMOUSPERSON").add(new StrDblPair("Karl Popper", 0.2));
    grammar.get("#FAMOUSPERSON").add(new StrDblPair("Viggo Kann", 0.2));

    grammar.put("#CSBUZZWORD", new ArrayList<>());
    grammar.get("#CSBUZZWORD").add(new StrDblPair("technology", 0.1));
    grammar.get("#CSBUZZWORD").add(new StrDblPair("cloud based solution", 0.1));
    grammar.get("#CSBUZZWORD").add(new StrDblPair("big data", 0.1));
    grammar.get("#CSBUZZWORD").add(new StrDblPair("data mining", 0.1));
    grammar.get("#CSBUZZWORD").add(new StrDblPair("JVM based language", 0.1));
    grammar.get("#CSBUZZWORD").add(new StrDblPair("decision problem", 0.1));
    grammar.get("#CSBUZZWORD").add(new StrDblPair("privacy concern", 0.1));
    grammar.get("#CSBUZZWORD").add(new StrDblPair("cloud computing", 0.1));
    grammar.get("#CSBUZZWORD").add(new StrDblPair("distributed computing", 0.1));
    grammar.get("#CSBUZZWORD").add(new StrDblPair("SCRUM methodology", 0.1));
    grammar.get("#CSBUZZWORD").add(new StrDblPair("the internet of things", 0.1));

    // TODO: use synonyms
    grammar.put("#RELATED", new ArrayList<>());
    grammar.get("#RELATED").add(new StrDblPair("related", 0.25));
    grammar.get("#RELATED").add(new StrDblPair("correlated", 0.25));
    grammar.get("#RELATED").add(new StrDblPair("relevant", 0.25));
    grammar.get("#RELATED").add(new StrDblPair("connected", 0.25));

    // TODO: use synonyms
    grammar.put("#POSORNEG", new ArrayList<>());
    grammar.get("#POSORNEG").add(new StrDblPair("positive", 0.5));
    grammar.get("#POSORNEG").add(new StrDblPair("negative", 0.5));

    grammar.put("#INTERESTING", new ArrayList<>());
    grammar.get("#INTERESTING").add(new StrDblPair("interesting", 0.2));
    grammar.get("#INTERESTING").add(new StrDblPair("fascinating", 0.2));
    grammar.get("#INTERESTING").add(new StrDblPair("intriguing", 0.2));
    grammar.get("#INTERESTING").add(new StrDblPair("enthralling", 0.2));
    grammar.get("#INTERESTING").add(new StrDblPair("stimulating", 0.2));

    grammar.put("#MAYBENOT", new ArrayList<>());
    grammar.get("#MAYBENOT").add(new StrDblPair("", 0.5));
    grammar.get("#MAYBENOT").add(new StrDblPair("not", 0.5));

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

  public void testGenerateParagraph() {
    System.out.println(generateParagraph("asasdadsa"));
  }
}
