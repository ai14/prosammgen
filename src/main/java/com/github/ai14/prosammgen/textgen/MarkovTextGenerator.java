package com.github.ai14.prosammgen.textgen;

import com.github.ai14.prosammgen.MarkovTrainer;
import com.github.ai14.prosammgen.Ngram;
import com.github.ai14.prosammgen.WordProbability;
import com.github.ai14.prosammgen.textgen.TextGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MarkovTextGenerator implements TextGenerator {

  private final MarkovTrainer trainer;
  private final int numSentences;

  public MarkovTextGenerator(MarkovTrainer trainer, int numSentences) {
    this.trainer = trainer;
    this.numSentences = numSentences;
  }

  @Override
  public String generateText(Context context) {
    return getText(context.getRandom(), numSentences, trainer);
  }

  // TODO: remove averageSentenceLength from this method?
  // use calculated statistics instead
  private static String getText(Random rand, int numberSentences, MarkovTrainer trainer) {
    Map<String, ArrayList<WordProbability>> markovChain = trainer.getMarkovChain();
    StringBuilder sb = new StringBuilder();

    List<Ngram> startNgrams = trainer.getSentenceStarts();
    Ngram ngram = startNgrams.get(rand.nextInt(startNgrams.size()));

    for (int i = 0; i < numberSentences; i++) {
      sb.append(getSentence(rand, markovChain, startNgrams, ngram));
    }

    return sb.toString();
  }

  private static String getSentence(Random rand,
                                    Map<String, ArrayList<WordProbability>> markovChain,
                                    List<Ngram> startNgrams,
                                    Ngram ngram) {

    StringBuilder sb = new StringBuilder();

    int avgSentenceLength = 10; // TODO: later get this from statistics
    int currentLength = 0;
    boolean sentenceEnded = ngram.toString().endsWith(".");
    while (!sentenceEnded) {
      sb.append(ngram.getFirst() + " ");
      currentLength++;

      // TODO: less arbitrary maybe and also not as strict, maybe write it as some kind of prob function
      boolean tryToEndSentence = currentLength >= avgSentenceLength - avgSentenceLength / 5;

      if (ngram.getFirst().endsWith(".")) {
        sentenceEnded = true;
        tryToEndSentence = false;
      }

      chooseNextWord(rand, markovChain, startNgrams, ngram, tryToEndSentence);
    }

    // remove the last space
    sb.deleteCharAt(sb.length() - 1);

    return sb.toString();
  }

  private static void chooseNextWord(
      Random rand,
      Map<String, ArrayList<WordProbability>> markovChain,
      List<Ngram> startNgrams,
      Ngram ngram, boolean tryToEndSentence) {

    ArrayList<WordProbability> wordProbabilities = markovChain.get(ngram.toString());
    String nextWord;

    if (wordProbabilities == null) {
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

      nextWord =
          !endWords.isEmpty() ? chooseWord(rand, endWords) : chooseWord(rand, wordProbabilities);

    } else {
      // choose from all possible next words
      nextWord = chooseWord(rand, wordProbabilities);
    }

    ngram.pushWord(nextWord);
  }

  private static String chooseWord(Random rand, ArrayList<WordProbability> wordProbabilities) {
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