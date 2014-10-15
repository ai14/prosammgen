package com.github.ai14.prosammgen;

import java.io.*;
import java.util.*;

public class Trainer {

  public static final int markovOrder = 2;
  private Map<String, Map<String, Integer>> nextWordCounter;

  public Trainer() {
    nextWordCounter = new HashMap<>();
  }

  public void train(File[] files) {
    System.out.println("Starting training");
    long trainStart = System.currentTimeMillis();

    for (File file : files) {
      try {
        BufferedReader br = new BufferedReader(new FileReader(file));

        Ngram ngram = new Ngram(markovOrder);

        String line;
        while ((line = br.readLine()) != null) {
          String[] words = line.split("(\\s+)");

          for (int i = 0; i < words.length; i++) {
            String ns = ngram.toString();
            String nw = words[i];

            if (!nextWordCounter.containsKey(ns)) {
              nextWordCounter.put(ns, new HashMap<>());
            }

            if (!nextWordCounter.get(ns).containsKey(nw)) {
              nextWordCounter.get(ns).put(nw, 0);
            }

            int c = nextWordCounter.get(ns).get(nw);
            nextWordCounter.get(ns).put(nw, c+1);

            ngram.pushWord(nw);
          }
        }

      } catch (FileNotFoundException e) {
      } catch (IOException e) {
      }
    }

    long trainEnd = System.currentTimeMillis();
    System.out.println("Finished training in " + (trainEnd - trainStart) + " ms.");
    System.out.println(nextWordCounter.size() + " ngrams created.");
    System.out.println();
  }

  public Map<String, ArrayList<WordProbability>> getMarkovChain() {
    Map<String, ArrayList<WordProbability>> markovChain = new HashMap<>();

    for (Map.Entry<String, Map<String, Integer>> me : nextWordCounter.entrySet()) {
      String word = me.getKey();
      Map<String, Integer> wordCounts = me.getValue();

      markovChain.put(word, new ArrayList<>(wordCounts.size()));
      ArrayList<WordProbability> probabilities = markovChain.get(word);

      int n = 0;
      for (Map.Entry<String, Integer> wordFreq : wordCounts.entrySet()) {
        n += wordFreq.getValue();
      }

      for (Map.Entry<String, Integer> wordFreq : wordCounts.entrySet()) {
        String w = wordFreq.getKey();
        int freq = wordFreq.getValue();
        probabilities.add(new WordProbability(w, ((double) freq / n)));
      }

      Collections.sort(probabilities);
    }

    return markovChain;
  }
}