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
  }

  public Map<String, ArrayList<WordProbability>> getMarkovChain() {
    Map<String, ArrayList<WordProbability>> markovChain = new HashMap<>();

    for (String word : nextWordCounter.keySet()) {
      int n = nextWordCounter.get(word).size();
      markovChain.put(word, new ArrayList<>(n));

      ArrayList<WordProbability> probabilities = markovChain.get(word);

      for (Map.Entry<String, Integer> wordFreq : nextWordCounter.get(word).entrySet()) {
        String w = wordFreq.getKey();
        int freq = wordFreq.getValue();
        probabilities.add(new WordProbability(w, ((double) freq) / n));
      }

      Collections.sort(probabilities);
    }

    return markovChain;
  }
}