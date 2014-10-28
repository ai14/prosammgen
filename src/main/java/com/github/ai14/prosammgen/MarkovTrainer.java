package com.github.ai14.prosammgen;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MarkovTrainer {

  public static final int markovOrder = 2;
  private Map<String, Map<String, Integer>> nextWordCounter;
  private Map<String, List<Ngram>> wordToNgram;
  private List<Ngram> sentenceStarts;

  public MarkovTrainer() {
    nextWordCounter = Maps.newHashMap();
    wordToNgram = Maps.newHashMap();
    sentenceStarts = Lists.newArrayList();
  }

  public void train(ImmutableSet<File> files) throws IOException {
    train(1, files);
  }

  public void train(int weight, ImmutableSet<File> files) throws IOException {
    for (File file : files) {
      Ngram ngram = new Ngram(markovOrder);
      String wordBeforeNgram = "";
      for (String line : Files.readLines(file, Charsets.UTF_8)) {
        String[] words = line.split("(\\s+)");

        for (int i = 0; i < words.length; i++) {
          String ns = ngram.toString();
          String nw = words[i];

          // find starts of sentences
          if (wordBeforeNgram.equals("") || wordBeforeNgram.endsWith(".")) {
            Ngram ngramCopy = new Ngram(ngram);
            sentenceStarts.add(ngramCopy);
          }

          if (!nextWordCounter.containsKey(ns)) {
            nextWordCounter.put(ns, Maps.<String, Integer>newHashMap());
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

    // for every word, store the following ngram
    for (Map.Entry<String, Map<String, Integer>> me : nextWordCounter.entrySet()) {
      if (me.getKey().length() == 0) continue;

      String[] ngramKey = me.getKey().split(" ");
      String word = ngramKey[0];

      for (String followingWord : me.getValue().keySet()) {
        Ngram ngram = new Ngram(markovOrder);
        for (int i = 1; i < ngramKey.length; i++) {
          ngram.pushWord(ngramKey[i]);
        }

        ngram.pushWord(followingWord);

        if (!wordToNgram.containsKey(word)) {
          wordToNgram.put(word, Lists.<Ngram>newArrayList());
        }

        wordToNgram.get(word).add(ngram);
      }
    }
  }

  public Ngram getNewNgram() {
    return new Ngram(markovOrder);
  }

  public Map<String, ArrayList<WordProbability>> getMarkovChain() {
    Map<String, ArrayList<WordProbability>> markovChain = Maps.newHashMap();

    for (Map.Entry<String, Map<String, Integer>> me : nextWordCounter.entrySet()) {
      String word = me.getKey();
      Map<String, Integer> wordCounts = me.getValue();

      markovChain.put(word, Lists.<WordProbability>newArrayListWithCapacity(wordCounts.size()));
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

  public List<Ngram> getSentenceStarts(String word) {
    return wordToNgram.get(word);
  }
}