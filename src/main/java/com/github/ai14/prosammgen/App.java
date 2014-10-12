package com.github.ai14.prosammgen;

import java.io.File;

public class App {
    public static void main(String[] args) {
      File[] files = new File[args.length];
      for (int i = 0; i < files.length; i++) {
        files[i] = new File(args[i]);
      }

      TextGenerator textGen = new TextGenerator(files);
      String text = textGen.generateText();

      System.out.println(text);
    }
}
