package com.github.ai14.prosammgen.textgen;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import com.github.ai14.prosammgen.MarkovTrainer;
import com.github.ai14.prosammgen.Ngram;
import com.github.ai14.prosammgen.WordProbability;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringJoiner;

public class MarkovTextGenerator implements TextGenerator {

  private final MarkovTrainer trainer;
  private final int numSentences;

  public MarkovTextGenerator(MarkovTrainer trainer, int numSentences) {
    this.trainer = trainer;
    this.numSentences = numSentences;
  }

  @Override
  public void generateText(Context context) {
    List<String> previousWords = Splitter.on(CharMatcher.WHITESPACE).splitToList(context.getBuilder().toString());

    context.getBuilder().append(getText(context.getRandom(), numSentences, previousWords, trainer));
  }

  // TODO: remove averageSentenceLength from this method?
  // use calculated statistics instead
  private static String getText(Random rand, int numberSentences, List<String> previousWords, MarkovTrainer trainer) {
    Map<String, ArrayList<WordProbability>> markovChain = trainer.getMarkovChain();

    List<Ngram> startNgrams = trainer.getSentenceStarts();
    Ngram ngram = getStartNgram(rand, previousWords, markovChain, trainer);

    StringJoiner sentences = new StringJoiner(" ");
    for (int i = 0; i < numberSentences; i++) {
      sentences.add(getSentence(rand, markovChain, startNgrams, ngram));
    }

    return sentences.toString();
  }

  private static Ngram getStartNgram(Random rand,
                              List<String> previousWords,
                              Map<String, ArrayList<WordProbability>> markovChain,
                              MarkovTrainer trainer) {

    // First try to get an ngram from the n last words in the previous sentence
    Ngram ngram = new Ngram(MarkovTrainer.markovOrder);
    for (int i = previousWords.size() - MarkovTrainer.markovOrder; i < previousWords.size(); i++) {
      ngram.pushWord(previousWords.get(i));
    }

    boolean failed = false;
    for (int i = 0; i < MarkovTrainer.markovOrder && !failed; i++) {
      if (markovChain.containsKey(ngram.toString())) {
        String nextWord = chooseWord(rand, markovChain.get(ngram.toString()));
        ngram.pushWord(nextWord);
      } else {
        failed = true;
      }
    }

    if (!failed) {
      return ngram;
    }

    // Then try to get an ngram from the last word only
    String lastWord = previousWords.get(previousWords.size() - 1);
    List<Ngram> wordToNgrams = trainer.getSentenceStarts(lastWord);
    if (wordToNgrams != null) {
      return wordToNgrams.get(rand.nextInt(wordToNgrams.size()));
    }

    // If all else fails, simply take a random starting ngram
    List<Ngram> startNgrams = trainer.getSentenceStarts();
    return startNgrams.get(rand.nextInt(startNgrams.size()));
  }

  private static String getSentence(Random rand,
                                    Map<String, ArrayList<WordProbability>> markovChain,
                                    List<Ngram> startNgrams,
                                    Ngram ngram) {

    StringJoiner sentence = new StringJoiner(" ");

    int avgSentenceLength = 10; // TODO: later get this from statistics
    int currentLength = 0;
    boolean sentenceEnded = false;
    while (!sentenceEnded) {
      sentence.add(ngram.getFirst());
      currentLength++;

      // TODO: less arbitrary maybe and also not as strict, maybe write it as some kind of prob function
      boolean tryToEndSentence = currentLength >= avgSentenceLength - avgSentenceLength / 5;

      if (ngram.getFirst().endsWith(".")) {
        sentenceEnded = true;
        tryToEndSentence = false;
      }

      // TODO: find a better solution to this, or fix the input so that this will not be possible
      if (currentLength >= 80) {
        System.err.println("FINISHED SENTENCE WITHOUT FINDING PERIOD");
        sentenceEnded = true;
        tryToEndSentence = false;
      }

      chooseNextWord(rand, markovChain, startNgrams, ngram, tryToEndSentence);
    }

    return sentence.toString();
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