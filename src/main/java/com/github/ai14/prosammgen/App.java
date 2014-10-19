package com.github.ai14.prosammgen;

import com.github.ai14.prosammgen.textgen.*;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.text.ParseException;
import java.util.Map;
import java.util.function.Function;

import static java.lang.ProcessBuilder.Redirect.INHERIT;

public class App {

  public static void main(String[] args) throws IOException, ParseException, InterruptedException {

    // Get input.
    String reflectionDocumentTitle = null, authorName = null;
    int wordLimit = -1;
    Path previousReflectionDocument = null, readingMaterial = null, questions = null;
    for (int i = 0; i < args.length; i++) {
      switch (args[i]) {
        case "-t":
          reflectionDocumentTitle = args[i + 1].replaceAll("^(\"|\')|(\"|\')$", "");
          break;
        case "-a":
          authorName = args[i + 1].replaceAll("^(\"|\')|(\"|\')$", "");
          break;
        case "-w":
          wordLimit = Integer.parseInt(args[i + 1]);
          break;
        case "-p":
          previousReflectionDocument = Paths.get(args[i + 1]);
          break;
        case "-r":
          readingMaterial = Paths.get(args[i + 1]);
          break;
        case "-q":
          questions = Paths.get(args[i + 1]);
          break;
      }
    }

    // Require input.
    if (reflectionDocumentTitle == null || authorName == null || wordLimit == -1 || previousReflectionDocument == null | readingMaterial == null | questions == null) {
      System.err.println("prosammgen [-t REFLECTION_DOCUMENT_TITLE | -a AUTHOR_NAME | -w WORD_LIMIT | -p PREVIOUS_REFLECTION_DOCUMENT | -r READING_MATERIAL | -q QUESTIONS]");
      System.err.println("In order to generate a reflection document, start the program with the above arguments.");
      System.err.println("Make sure: ");
      System.err.println("  REFLECTION_DOCUMENT_TITLE is the title of the current reflection seminar surrounded by quotes.");
      System.err.println("  AUTHOR_NAME is the author's name surrounded by quotes.");
      System.err.println("  WORD_LIMIT is a positive integer larger than zero.");
      System.err.println("  PREVIOUS_REFLECTION_DOCUMENT is the path to a plaintext file with the author's previous reflection document.");
      System.err.println("  READING_MATERIAL is the path to a plaintext file with all the reading material for the current reflection seminar.");
      System.err.println("  QUESTIONS is the path to a plaintext file with the current seminar questions, with every question placed on its own line.");
      System.exit(-1);
    }

    //TODO Validate input and provide semantic errors.

    //TODO Sanitize input.

    ImmutableList<String> questionList = ImmutableList.copyOf(Files.readAllLines(questions));

    // Create a keyword identifier.
    ImmutableSet<String> stopWords =
            ImmutableSet.copyOf(Files.readAllLines(Paths.get("res/stopwords")));
    NLPModel nlpModel =
            NLPModel.loadFromDBs(Paths.get("res/en-sent.bin"), Paths.get("res/en-token.bin"),
                    Paths.get("res/en-pos-maxent.bin"));
    KeywordGenerator keywordGenerator =
            KeywordGenerator.withPOSParsing(nlpModel, stopWords, Joiner.on('\n').join(questionList));

    // Create and train a markov chain for the grammar.
    MarkovTrainer trainer = new MarkovTrainer();
    //TODO Add keywords from the keyword identifier as well.
    ImmutableSet<String> searchTerms = keywordGenerator.getWords();
    TextSource wa = new WikipediaArticles(10, searchTerms.toArray(new String[searchTerms.size()])); //TODO Change signature to ImmutableSet.

    // temp
    long rmSize = Files.size(readingMaterial);
    long waSize = 0;
    for (Path p : wa.getTexts()) {
      waSize += Files.size(p);
    }
    // aim for reading material to be 50% of wiki articles
    int weight = (int) (0.5 / (rmSize / ((double) waSize)));
    // TODO: use a weight when training on reading material
    trainer.train(weight, readingMaterial);
    trainer.train(wa.getTexts());

    // Create a synonyms database for the grammar.
    Synonyms synonyms = new WordNetSynonyms();

    ImmutableMap<String, Function<ImmutableList<String>, TextGenerator>> macros = ImmutableMap.of(
            "MARKOV", n -> new MarkovTextGenerator(trainer, Integer.parseInt(n.get(0))),
            "SYNONYM", words -> new SynonymGenerator(words, synonyms)
    );

    ImmutableMap<String, TextGenerator> generators =
            TextGenerators.parseGrammar(Files.readAllLines(Paths.get("res/grammar")));

    System.err.println("Grammar is:");
    for (Map.Entry<String, TextGenerator> generator : generators.entrySet()) {
      System.err.println(generator.getKey() + " :-\n    " + generator.getValue());
    }
    System.err.println();

    // Create and train an AI with the input.
    ReflectionDocumentGenerator rg = new ReflectionDocumentGenerator(generators, questionList, macros, nlpModel, stopWords);

    // Generate a reflection document with the AI.
    String report = rg.generateReport(reflectionDocumentTitle, authorName, wordLimit);

    // Generate PDF report if pdftex exists on the current system. Otherwise output the LaTeX source on stdout.

    // Replace characters in accordance with the prosamm instructions. å -> a, é -> e, etc.
    String filename = Normalizer.normalize(authorName, Normalizer.Form.NFD).replaceAll(" ", "_").replaceAll("[^A-Za-z_]", "");
    PrintWriter out = new PrintWriter(filename + ".tex");
    out.write(report);
    out.close();

    //TODO Prosammgen has to be run twice to output a PDF with cygwin pdftex.
    Process p = new ProcessBuilder()
            .redirectError(INHERIT)
            .redirectOutput(INHERIT)
            .command("pdftex", "-file-line-error", "-halt-on-error", "&pdflatex", filename + ".tex")
            .start();
    if (p.waitFor() == 0) System.out.println("Successfully generated a reflection document as PDF.");
  }

}
