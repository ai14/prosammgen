package com.github.ai14.prosammgen;

public class WordProbability implements Comparable<WordProbability> {
  public String word;
  public double probability;

  public WordProbability(String word, double probability) {
    this.word = word;
    this.probability = probability;
  }


  @Override
  public int compareTo(WordProbability wordProbability) {
    return Double.compare(this.probability, wordProbability.probability);
  }
}
