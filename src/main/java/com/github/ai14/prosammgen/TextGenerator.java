package com.github.ai14.prosammgen;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

public class TextGenerator {

  private Trainer trainer;
  private Random rand;


  public TextGenerator(File[] files) {
    trainer = new Trainer();
    trainer.train(files);
    rand = new Random(System.currentTimeMillis());
  }

  public String generateText() {
    Map<String, ArrayList<WordProbability>> markovChain = trainer.getMarkovChain();

    // TODO: find actual start words in the training data
    StringBuilder sb = new StringBuilder();
    Ngram ngram = new Ngram(Trainer.markovOrder);

    for (int i = 0; i < 15; i++) {
      sb.append(ngram.getLast() + " ");
      getNextWord(markovChain, ngram);
    }

    return sb.toString();
  }

  private void getNextWord(Map<String, ArrayList<WordProbability>> markovChain, Ngram ngram) {
    ArrayList<WordProbability> wordProbabilities = markovChain.get(ngram.toString());

    double d = rand.nextDouble();
    double upperLimit = wordProbabilities.get(0).probability;

    for (int i = 0; i < wordProbabilities.size() - 1; i++) {
      if (Double.compare(d, upperLimit) < 0) {
        ngram.pushWord(wordProbabilities.get(i).word);
        return;

      } else {
        upperLimit += wordProbabilities.get(i + 1).probability;
      }
    }

    ngram.pushWord(wordProbabilities.get(wordProbabilities.size() - 1).word);
  }
}

