package com.github.ai14.prosammgen;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class App {
  public static void main(String[] args) {

    // Read input
    //TODO Add word length limit and author name.
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
      //TODO Find pdftex.
      String[] cmd = {"pdftex", "reflectiondocument.tex"};
      Runtime.getRuntime().exec(cmd);
      System.out.println("Successfully generated a reflection document as PDF.");
    } catch (IOException e) {
      System.out.println("Tip: Install pdftex to generate reflection documents as PDF.");
      System.out.println();
      System.out.println(report);
    }
  }
}
