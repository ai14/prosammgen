package com.github.ai14.prosammgen;

import java.io.File;
import java.util.*;

public class MarkovTextGenerator {

  private MarkovTrainer trainer;
  private Random rand;

  public MarkovTextGenerator(File... files) {
    trainer = new MarkovTrainer();
    trainer.train(files);
    rand = new Random(System.currentTimeMillis());
  }

  // TODO: remove averageSentenceLength from this method?
  // use calculated statistics instead
  public String getText(int numberSentences) {
    Map<String, ArrayList<WordProbability>> markovChain = trainer.getMarkovChain();
    StringBuilder sb = new StringBuilder();

    List<Ngram> startNgrams = trainer.getSentenceStarts();
    Ngram ngram = startNgrams.get(rand.nextInt(startNgrams.size()));

    for (int i = 0; i < numberSentences; i++) {
      sb.append(getSentence(markovChain, startNgrams, ngram) + " ");
    }

    return sb.toString();
  }

  private String getSentence(Map<String, ArrayList<WordProbability>> markovChain,
                             List<Ngram> startNgrams,
                             Ngram ngram) {

    StringBuilder sb = new StringBuilder();

    int avgSentenceLength = 10; // TODO: later get this from statistics
    int currentLength = 0;
    boolean sentenceEnded = ngram.toString().endsWith(".");
    while (!sentenceEnded) {
      sb.append(ngram.getFirst() + " ");
      currentLength++;

      // TODO: less arbitrary maybe
      boolean tryToEndSentence = currentLength >= avgSentenceLength - avgSentenceLength / 5;

      if (ngram.getFirst().endsWith(".")) {
        sentenceEnded = true;
        tryToEndSentence = false;
      }

      chooseNextWord(markovChain, startNgrams, ngram, tryToEndSentence);
    }

    return sb.toString();
  }

  private void chooseNextWord(Map<String, ArrayList<WordProbability>> markovChain,
                              List<Ngram> startNgrams,
                              Ngram ngram, boolean tryToEndSentence) {

    ArrayList<WordProbability> wordProbabilities = markovChain.get(ngram.toString());
    String nextWord;

    if (wordProbabilities == null) {
      // TODO: can we do this in any smarter way?
      Ngram newNgram = startNgrams.get(rand.nextInt(startNgrams.size()));
      for (String word : newNgram.getAll()) {
        ngram.pushWord(word);
      }

      return;
    }

    if (tryToEndSentence) {
      // choose only next words that end in "." if possible
      ArrayList<WordProbability> endWords = new ArrayList<>(wordProbabilities.size());

      double prob = 0.0;
      for (WordProbability wp : wordProbabilities) {
        if (wp.word.endsWith(".")) {
          endWords.add(new WordProbability(wp.word, wp.probability));
          prob = prob + wp.probability;
        }
      }

      // normalise
      for (WordProbability wp : endWords) {
        wp.probability = wp.probability / prob;
      }

      nextWord = !endWords.isEmpty() ? chooseWord(endWords) : chooseWord(wordProbabilities);

    } else {
      // choose from all possible next words
      nextWord = chooseWord(wordProbabilities);
    }

    ngram.pushWord(nextWord);
  }

  private String chooseWord(ArrayList<WordProbability> wordProbabilities) {
    double d = rand.nextDouble();
    double upperLimit = wordProbabilities.get(0).probability;

    for (int i = 0; i < wordProbabilities.size() - 1; i++) {
      if (Double.compare(d, upperLimit) < 0) {
        return wordProbabilities.get(i).word;
      }

      upperLimit = upperLimit + wordProbabilities.get(i + 1).probability;
    }

    return wordProbabilities.get(wordProbabilities.size() - 1).word;
  }
}