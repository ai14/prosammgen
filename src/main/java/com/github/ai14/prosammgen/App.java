package com.github.ai14.prosammgen;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import com.google.common.io.Resources;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.Normalizer;
import java.text.ParseException;
import java.util.Scanner;

public class App {
  public static void main(String[] args) throws IOException, ParseException, InterruptedException {

    // Get input.
    int wordCount = 500, maxWebRequests = 10;
    String title = "", author = "", outputDirectory = "./";
    File previousReflectionDocument = null, readingMaterial = null, questions = null;
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-t")) {
        title = args[i + 1].replaceAll("^(\"|\')|(\"|\')$", "");
      } else if (args[i].equals("-a")) {
        author = args[i + 1].replaceAll("^(\"|\')|(\"|\')$", "");
      } else if (args[i].equals("-w")) {
        wordCount = Integer.parseInt(args[i + 1]);
      } else if (args[i].equals("-p")) {
        previousReflectionDocument = new File(args[i + 1]);
      } else if (args[i].equals("-r")) {
        readingMaterial = new File(args[i + 1]);
      } else if (args[i].equals("-q")) {
        questions = new File(args[i + 1]);
      } else if (args[i].equals("-o")) {
        outputDirectory = args[i + 1];
      } else if (args[i].equals("-m")) {
        maxWebRequests = Integer.parseInt(args[i + 1]);
      }
    }

    // Require input.
    if (title.equals("") || author.equals("") || previousReflectionDocument == null || readingMaterial == null || questions == null) {
      if (title.equals("")) System.err.println("Missing reflection document title!");
      if (author.equals("")) System.err.println("Missing author name!");
      if (previousReflectionDocument == null) System.err.println("Missing path to a previous reflection document!");
      if (readingMaterial == null) System.err.println("Missing path to the seminar reading material.");
      if (questions == null) System.err.println("Missing path to the seminar questions.");

      System.out.println("prosammgen [-t TITLE | -a AUTHOR | -p PREVIOUS_REFLECTION_DOCUMENT | -r READING_MATERIAL | -q QUESTIONS | -w WORD_COUNT | -o OUTPUT_DIRECTORY | -m MAX_WEB_REQUESTS]");
      System.out.println();
      System.out.println("TITLE - the upcoming reflection seminar");
      System.out.println("AUTHOR - reflection document author's name.");
      System.out.println("PREVIOUS_REFLECTION_DOCUMENT - path to a plaintext file with the author's previous reflection document.");
      System.out.println("READING_MATERIAL - path to a plaintext file with all of the reading material for the upcoming reflection seminar.");
      System.out.println("QUESTIONS - path to a plaintext file with the current seminar questions, with every question placed on its own line.");
      System.out.println("WORD_COUNT (optional) - integer with the sought number of words in the generated reflection document.");
      System.out.println("OUTPUT_DIRECTORY (optional) - path to the root directory where the output files will be saved. Default is the program root.");
      System.out.println("MAX_WEB_REQUESTS (optional) - integer limiting the amount of web requests allowed by the program. Default is the maximum.");

      System.exit(-1);
    }

    //TODO Sanitize input.

    // Parse questions.
    ImmutableList<String> questionList = ImmutableList.copyOf(Files.readLines(questions, Charsets.UTF_8));

    // Load synonyms database.
    WordNet synonyms = WordNet.load(Resources.getResource(App.class, "wordnet.dat"));

    // Analyze previous reflection document.
    WritingStyleAnalyzer analyzer = new WritingStyleAnalyzer(synonyms);
    analyzer.analyze(previousReflectionDocument);

    // Generate a reflection document.
    String report = new ReflectionDocumentGenerator(synonyms, outputDirectory).generateReport(title, author, questionList, readingMaterial, wordCount, maxWebRequests);

    // Transform writing style of the new reflection document to match the previous reflection document.
    Humanizer humanizer = new Humanizer(synonyms, previousReflectionDocument);
    report = humanizer.textHumanizer(report);

    // Replace characters in accordance with the prosamm instructions. å -> a, é -> e, etc.
    String filename = Normalizer.normalize(author, Normalizer.Form.NFD).replaceAll(" ", "_").replaceAll("[^A-Za-z_]", "") + ".tex";

    // Write LaTeX output to file.
    File f = new File(outputDirectory, filename);
    PrintWriter pw = new PrintWriter(f);
    pw.println(report);
    pw.close();
    System.out.println("Generated reflection document: " + f.getAbsolutePath());

    // Try to generate a PDF from the LaTeX file with xetex.
    Process p = new ProcessBuilder()
            .redirectErrorStream(true)
            .command("xetex", "-interaction=nonstopmode", "-output-directory=" + outputDirectory, "&xelatex", filename)
            .start();
    int statusCode = p.waitFor();
    if (statusCode == 0) {
      System.out.println("Successfully compiled PDF.");
    } else {
      Scanner s = new Scanner(p.getInputStream());
      while (s.hasNextLine()) System.out.println(s.nextLine());
      s.close();
    }
    System.exit(statusCode);
  }
}
