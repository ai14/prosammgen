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

class WAnalyzerS implements WritingStyleAnalyzer {

  /**
   * Given a text this calculates the probabilities for different metrics that describes the writing style of the text author.
   *
   * @param text
   */
  public void analyze(Path text){
    int i = 0;
  }

  /**
   * Get the probabilities for sentences of length [0..longest sentence in the text].
   *
   * @return
   */
  public double[] getSentenceLengthProbabilities(Path text){
    //probs from the text
    int numSentence = 0;
    int beginT = 0;
    list<int>sentenceSize = new ArrayList<>();
    //Read the text
    for (String line : Files.readAllLines(file)) {
        String[] words = line.split("\\s+");
        for (int i = 0; i < words.length; i++) {
          // find all sentences
          if (beginT == 0 || words[i].endsWith(".")) {
            beginT = 1;
            int count = 0;
            //Check distance of the sentence
            while(!words[i].endsWith(".")){
              cout ++;
              ++i;
            }
            sentenceSize[count]++;
            ++numSentence;
          }
        }
      }
      double[] prob = new double[sentenceSize.length];
      for(int j = 0; j < prob.length; j++){
        prob[j] = sentenceSize[j]/numSentence;
      }
      return prob;
  }

  /**
   * Get the probabilities for words of length [0..longest word in the text].
   *
   * @return
   */
  public double[] getWordLengthProbabilities(Path text){
    //probs from the text
    list<int>wordSize = new ArrayList<>();
    //Read the text
    for (String line : Files.readAllLines(file)) {
        String[] words = line.split("\\s+");
        for (int i = 0; i < words.length; i++) {
          //word size
          wordSize[words[i].length]++;
        }
      }
      //calculate probs
      double[] prob = new double[wordSize.length];
      for(int j = 0; j < prob.length ;j++){
        prob[j] = wordSize[j]/words.length;
      }
      return prob;
  }

  /**
   * Get the probabilities for number of sentences per paragraph from [0..most number of sentences in a paragraph in the text].
   *
   * @return
   */
  public double[] getSentencesPerParagraphProbabilities(Path text){
    return 1.0;
  }

  /**
   * Get the probabilities for ratios (answer length / question length).
   *
   * @return
   */
  public double[] getQuestionLengthToAnswerLengthRatioProbabilities(Path text){
    return 1.0;
  }
}
