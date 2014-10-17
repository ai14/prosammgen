package com.github.ai14.prosammgen;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class App {
  public static void main(String[] args) {

    // Read input
    //TODO Add word length limit and author name.
    if (args.length == 0) {
      System.err.println("prosammgen PREVIOUS_REFLECTION_DOCUMENT READING_MATERIAL QUESTIONS");
      System.err.println("In order to generate a reflection document, start the program with the above arguments (paths to plain text files).");
      System.exit(-1);
    }
    File previousReflectionDocument = new File(args[0]);
    File readingMaterial = new File(args[1]);
    File questions = new File(args[2]);

    // Create and train an AI with the input.
    ReportGenerator rg = new ReportGenerator(previousReflectionDocument, readingMaterial, questions);

    // Generate a reflection document with the AI.
    String report = rg.generateReport();

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
