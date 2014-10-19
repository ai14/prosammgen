package com.github.ai14.prosammgen;

import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.text.ParseException;

import static java.lang.ProcessBuilder.Redirect.INHERIT;

public class App {

  public static void main(String[] args) throws IOException, ParseException, InterruptedException {

    // Get input.
    String reflectionDocumentTitle = null, authorName = null;
    int wordCount = -1;
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
          wordCount = Integer.parseInt(args[i + 1]);
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
    if (reflectionDocumentTitle == null || authorName == null || wordCount == -1 || previousReflectionDocument == null | readingMaterial == null | questions == null) {
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

    // Parse questions.
    ImmutableList<String> questionList = ImmutableList.copyOf(Files.readAllLines(questions));

    // Generate a reflection document.
    String report = new ReflectionDocumentGenerator().generateReport(reflectionDocumentTitle, authorName, questionList, readingMaterial, wordCount);

    // Replace characters in accordance with the prosamm instructions. å -> a, é -> e, etc.
    String filename = Normalizer.normalize(authorName, Normalizer.Form.NFD).replaceAll(" ", "_").replaceAll("[^A-Za-z_]", "");

    // Write LaTeX output to file.
    try (PrintWriter out = new PrintWriter(filename + ".tex")) {
      out.write(report);
    }

    // Generate PDF from LaTeX file.
    Process p = new ProcessBuilder()
            .redirectError(INHERIT)
            .redirectOutput(INHERIT)
            .command("pdftex", "-file-line-error", "-halt-on-error", "&pdflatex", filename + ".tex")
            .start();
    if (p.waitFor() == 0) System.out.println("Successfully generated a reflection document as PDF.");
  }

}
