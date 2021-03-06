package com.github.ai14.prosammgen;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.common.io.Resources;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WritingStyleAnalyzer {

  private final WordNet wordNet;
  private File text;
  private String[] totalWords;
  private String[] totalSentences;
  private String[] totalParagraphs;
  private String allWords;
  private List<String> words;
  private Random rand = new Random();

  private boolean sentenceLengthProbabilityCalculated = false;
  private double[] sentenceLengthProbabilites;

  public WritingStyleAnalyzer(WordNet wordNet) {
    this.wordNet = wordNet;
  }

  /**
   * Given a text this calculates the probabilities for different metrics that describes the writing style of the text author.
   *
   * @param text
   */
  public void analyze(File text) {
    try {
      this.text = text;
      List<String> allText = Files.readLines(this.text, Charsets.UTF_8);
      allWords = "";
      for (String singleWord : allText) {
        this.allWords += singleWord + " ";
      }
      this.totalWords = this.allWords.split("\\s+"); //splitting into words
      this.totalSentences = this.allWords.split("(?<=[a-z])\\.\\s+");//splitting into sentences
      this.totalParagraphs = this.allWords.split("\\n\\n");//splitting into Paragraphs

      sentenceLengthProbabilityCalculated = false;

    } catch (IOException e) {
      System.err.println("Couldn't read the text");
    }
  }

  /**
   * Get the probabilities for sentences of length [0..longest sentence in the text].
   *
   * @return
   */

  public double[] getSentenceLengthProbabilities() {
    if (sentenceLengthProbabilityCalculated) {
      return sentenceLengthProbabilites;
    }

    int numberOfSentences = totalSentences.length;
    List<Integer> sentenceSize = new ArrayList<Integer>();
    int max = -1;
    for (int i = 0; i < numberOfSentences; i++) {
      //split sentences into words
      String[] words = totalSentences[i].split("\\s+");
      if (words.length > max) max = words.length;
      sentenceSize.add(words.length);
    }
    Integer[] total = new Integer[max + 1];
    for (int i = 0; i < sentenceSize.size(); i++) {
      //split sentences into words
      int size = sentenceSize.get(i);
      if (total[size] == null) total[size] = 1;
      else total[size]++;

    }
    double[] probabilities = new double[total.length];
    //Calculating probabilities
    for (int j = 0; j < probabilities.length; j++) {
      if (total[j] == null) probabilities[j] = 0;
      else probabilities[j] = (double) total[j] / numberOfSentences;
    }
    sentenceLengthProbabilityCalculated = true;
    sentenceLengthProbabilites = probabilities;

    return probabilities;
  }

  /**
   * @return a sentence length from the sentence length distribution
   */
  public int getSentenceLength() {
    // The indexes are the sentence lengths
    double[] prob = getSentenceLengthProbabilities();

    double d = rand.nextDouble();
    double upperLimit = prob[0];

    for (int i = 0; i < prob.length - 1; i++) {
      if (Double.compare(prob[i], 0.0) == 0) {
        continue;
      }

      if (Double.compare(d, upperLimit) < 0) {
        return i;
      }

      upperLimit += prob[i + 1];
    }

    return prob.length - 1;
  }

  /**
   * Get the probabilities for words of length [0..longest word in the text].
   *
   * @return
   */
  public double[] getWordLengthProbabilities() {
    int textSize = 0;
    List<Integer> wordSize = new ArrayList<Integer>();
    textSize = totalWords.length;
    int max = -1;
    for (int i = 0; i < textSize; i++) {
      //increasing the number of characters with words[i].length
      if (totalWords[i].length() > max) max = totalWords[i].length();
      wordSize.add(totalWords[i].length());
    }
    Integer[] total = new Integer[max + 1];
    for (int i = 0; i < wordSize.size(); i++) {
      //split sentences into words
      int size = wordSize.get(i);
      if (total[size] == null) total[size] = 1;
      else total[size]++;

    }
    //Calculating probabilities
    double[] probabilities = new double[total.length];
    for (int j = 0; j < probabilities.length; j++) {
      if (total[j] == null) probabilities[j] = 0;
      else probabilities[j] = (double) wordSize.get(j) / textSize;
    }
    return probabilities;
  }

  /**
   * Get the probabilities for number of sentences per paragraph from [0..most number of sentences in a paragraph in the text].
   *
   * @return
   */

  public double[] getSentencesPerParagraphProbabilities() {

    int numberOfParagraph = totalParagraphs.length;
    List<Integer> sentencesParagraph = Lists.newArrayList();
    //each paragraph
    int max = -1;
    for (int i = 0; i < numberOfParagraph; i++) {
      //Split into sentences every paragraph
      String[] sentencesOnParagraph = totalParagraphs[i].split("(?<=[a-z])\\.\\s+");
      //increasing the times that a paragraph have "countSentences" sentences
      if (sentencesOnParagraph.length > max) max = sentencesOnParagraph.length;
      sentencesParagraph.add(sentencesOnParagraph.length);
    }
    Integer[] total = new Integer[max + 1];
    for (int i = 0; i < sentencesParagraph.size(); i++) {
      //split sentences into words
      int size = sentencesParagraph.get(i);
      if (total[size] == null) total[size] = 1;
      else total[size]++;

    }
    double[] probabilities = new double[total.length];
    for (int j = 0; j < probabilities.length; j++) {
      if (total[j] == null) probabilities[j] = 0;
      probabilities[j] = (double) sentencesParagraph.get(j) / numberOfParagraph;
    }
    return probabilities;

  }


  /**
   * Get the probabilities of mispelling words per text
   *
   * @return
   */
  public double getMisspellingWordsProbabilities() throws IOException, ParseException {
    int numberOfWords = totalWords.length;
    int misspellingWords = 0;
    WordNet wordNet;
    wordNet = WordNet.load(Resources.getResource(App.class, "wordnet.dat"));
    //for each word
    for (int j = 0; j < numberOfWords; j++) {
      //If it's end of sentence, remove the last character to check the synonyms.
      if ((totalWords[j].endsWith(".") || totalWords[j].endsWith("?") || totalWords[j].endsWith("!") || totalWords[j].endsWith(",") || totalWords[j].endsWith(":") || totalWords[j].endsWith(")") || totalWords[j].endsWith("”") || totalWords[j].endsWith(" \" ")) && totalWords[j].length() > 0 && totalWords[j] != null) {
        totalWords[j] = totalWords[j].substring(0, totalWords[j].length() - 1);
      }
      if ((totalWords[j].startsWith("(") || totalWords[j].startsWith("“") || totalWords[j].startsWith("\"")) && totalWords[j].length() > 0 && totalWords[j] != null) {
        totalWords[j] = totalWords[j].substring(1);
      }
      //Diferent characters ’ and ' that are used for the same
      if (totalWords[j].contains("’")) totalWords[j] = totalWords[j].replace("’", "'");
      boolean correct = false;
      if (totalWords[j].length() > 1 || (totalWords[j].length() == 1 && !totalWords[j].equals("I")))
        totalWords[j] = totalWords[j].toLowerCase();
      //Search for the correct word
      ImmutableList<String> synWord = ImmutableList.of(totalWords[j]);

      ImmutableSet<String> synonym = this.wordNet.getSynonyms(synWord);
      if (synonym.size() > 1) correct = true;

      //if we don't find the word, means is misspelled
      if (!correct) {
        ++misspellingWords;

      }
    }
    //calculate probability
    double probability = ((double) misspellingWords) / totalWords.length;
    return probability;
  }

  /**
   * Given a list of words gets their the frequency on the text
   *
   * @param wordsToCheck
   * @return
   */
  public double[] getWordUseProbabilities(List<String> wordsToCheck) {
    double[] probabilies = new double[wordsToCheck.size()];
    for (int i = 0; i < wordsToCheck.size(); ++i) {
      Pattern pattern = Pattern.compile(wordsToCheck.get(i) + " ");
      Matcher matcher = pattern.matcher(this.allWords);
      int count = 0;
      while (matcher.find()) {
        count++;

      }

      probabilies[i] = ((double) count) / totalWords.length;
    }
    return probabilies;
  }

}
