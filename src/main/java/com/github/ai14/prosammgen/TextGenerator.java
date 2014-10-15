package com.github.ai14.prosammgen;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

public class TextGenerator {

  private Trainer trainer;
  private Random rand;

  // IDEAS:

  // define a context free grammar instead, maybe with several possible rules to use and pick one at random?
  // let some placeholder words be generated from a markov chain in some way? (maybe by removing unnecessary word with NLP lib
  // from training data)
  // or maybe base it on words that appear close to each other in the training set?

  // use buzzwords!

  // try googling short questions?

  // the text following a question should be corresponding to length of question?
  // number of paragraph can be the number of subquestions?

  // rules should maybe be partially generated by the training texts?


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

    int lineLen = 0;
    for (int i = 0; i < 1000; i++) {
      sb.append(ngram.getLast() + " ");
      lineLen += ngram.getLast().length() + 1;
      if (lineLen > 100) {
        lineLen = 0;
        sb.append("\n");
      }
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
