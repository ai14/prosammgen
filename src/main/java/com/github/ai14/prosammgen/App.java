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

    // Create and train an AI with the input.
    ReportGenerator rg = new ReportGenerator(previousReflectionDocument, readingMaterial, questions);

    // Generate a reflection document with the AI.
    String report = rg.generateReport(reflectionDocumentTitle, authorName, wordLimit);

    // Generate PDF report if pdftex exists on the current system. Otherwise output the LaTeX source on stdout.
    try {
      PrintWriter out = new PrintWriter("reflectiondocument.tex");
      out.write(report);
      out.close();
      //TODO Fix pdftex on unix.
      //TODO Fix that prosammgen has to be run twice to output a pdf.
      String[] cmd = {"pdftex", "'&pdflatex'", "reflectiondocument.tex"};
      Process p = Runtime.getRuntime().exec(cmd);
      if (p.getErrorStream().available() == 0)
        System.out.println("Successfully generated a reflection document as PDF.");
      else {
        Scanner s = new Scanner(p.getErrorStream());
        while (s.hasNext()) System.err.println(s.next());
      }
    } catch (IOException e) {
      System.err.println("Install pdflatex to generate reflection documents as PDF.");
      System.out.println(report);
    }
  }
}
