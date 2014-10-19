package com.github.ai14.prosammgen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

// TODO Rename class to MarkovChain.
public class MarkovTrainer {

  public static final int markovOrder = 3;
  private Map<String, Map<String, Integer>> nextWordCounter;
  private List<Ngram> sentenceStarts;
  private Set<Ngram> sentenceEnds;

  public MarkovTrainer() {
    nextWordCounter = new HashMap<>();
    sentenceStarts = new LinkedList<>();
    sentenceEnds = new HashSet<>();
  }

  public void train(Path... files) throws IOException {
    train(1, files);
  }

  public void train(int weight, Path... files) throws IOException {
    for (Path file : files) {
      Ngram ngram = new Ngram(markovOrder);
      String wordBeforeNgram = "";
      for (String line : Files.readAllLines(file)) {
        String[] words = line.split("(\\s+)");

        for (int i = 0; i < words.length; i++) {
          String ns = ngram.toString();
          String nw = words[i];

          // find starts of sentences
          if (wordBeforeNgram.equals("") || wordBeforeNgram.endsWith(".")) {
            Ngram ngramCopy = new Ngram(ngram);
            sentenceStarts.add(ngramCopy);
          }

          // find ends of sentences
          if (ngram.getLast().endsWith(".")) {
            Ngram ngramCopy = new Ngram(ngram);
            sentenceEnds.add(ngramCopy);
          }

          if (!nextWordCounter.containsKey(ns)) {
            nextWordCounter.put(ns, new HashMap<>());
          }

          if (!nextWordCounter.get(ns).containsKey(nw)) {
            nextWordCounter.get(ns).put(nw, 0);
          }

          int c = nextWordCounter.get(ns).get(nw);
          nextWordCounter.get(ns).put(nw, c + weight);

          wordBeforeNgram = ngram.getFirst();
          ngram.pushWord(nw);
        }
      }
    }
  }

  public Ngram getNewNgram() {
    return new Ngram(markovOrder);
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

  public List<Ngram> getSentenceStarts() {
    return sentenceStarts;
  }

  public Set<Ngram> getSentenceEnds() {
    return sentenceEnds;
  }
}