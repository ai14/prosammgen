package com.github.ai14.prosammgen;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class App {

  public static void main(String[] args) {

    // Get input.
    String reflectionDocumentTitle = null, authorName = null;
    int wordLimit = -1;
    File previousReflectionDocument = null, readingMaterial = null, questions = null;
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
          previousReflectionDocument = new File(args[i + 1]);
          break;
        case "-r":
          readingMaterial = new File(args[i + 1]);
          break;
        case "-q":
          questions = new File(args[i + 1]);
          break;
      }
    }

    // Require input.
    if (reflectionDocumentTitle == null || authorName == null || wordLimit == -1
        || previousReflectionDocument == null | readingMaterial == null | questions == null) {
      System.err.println(
          "prosammgen [-t REFLECTION_DOCUMENT_TITLE | -a AUTHOR_NAME | -w WORD_LIMIT | -p PREVIOUS_REFLECTION_DOCUMENT | -r READING_MATERIAL | -q QUESTIONS]");
      System.err.println(
          "In order to generate a reflection document, start the program with the above arguments.");
      System.err.println("Make sure: ");
      System.err.println(
          "  REFLECTION_DOCUMENT_TITLE is the title of the current reflection seminar surrounded by quotes.");
      System.err.println("  AUTHOR_NAME is the author's name surrounded by quotes.");
      System.err.println("  WORD_LIMIT is a positive integer larger than zero.");
      System.err.println(
          "  PREVIOUS_REFLECTION_DOCUMENT is the path to a plaintext file with the author's previous reflection document.");
      System.err.println(
          "  READING_MATERIAL is the path to a plaintext file with all the reading material for the current reflection seminar.");
      System.err.println(
          "  QUESTIONS is the path to a plaintext file with the current seminar questions, with every question placed on its own line.");
      System.exit(-1);
    }

    //TODO Validate input and provide semantic errors.

    //TODO Sanitize input.

    // Create and train an AI with the input.
    // ReflectionDocumentGenerator rg = new ReflectionDocumentGenerator(previousReflectionDocument, readingMaterial, questions);

    // Generate a reflection document with the AI.
    String report = ""; // rg.generateReport(reflectionDocumentTitle, authorName, wordLimit);

    // Generate PDF report if pdftex exists on the current system. Otherwise output the LaTeX source on stdout.
    try {
      //TODO Replace characters in accordance with the prosamm instructions. å -> a, é -> e, etc.
      String filename = authorName.replaceAll("[^A-Za-z0-9]", "_");
      PrintWriter out = new PrintWriter(filename + ".tex");
      out.write(report);
      out.close();
      //TODO Prosammgen has to be run twice to output a PDF with cygwin pdftex.
      String[]
          cmd =
          {"pdftex", "&pdflatex", filename
                                  + ".tex"}; //TODO This command doesn't start properly on unix, even though the same command works directly in a terminal.
      Process p = Runtime.getRuntime().exec(cmd);
      if (p.getErrorStream().available() == 0) {
        System.out.println("Successfully generated a reflection document as PDF.");
      } else {
        Scanner s = new Scanner(p.getErrorStream());
        while (s.hasNext()) {
          System.err.println(s.next());
        }
      }
    } catch (IOException e) {
      System.err.println("Install pdflatex to generate reflection documents as PDF.");
      System.out.println(report);
    }
  }
}
