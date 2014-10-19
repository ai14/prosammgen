package com.github.ai14.prosammgen;

import java.nio.file.Path;
import java.nio.file.Files;

interface WritingStyleAnalyzer {

  /**
   * Given a text this calculates the probabilities for different metrics that describes the writing style of the text author.
   *
   * @param text
   */
  public void analyze(Path text);

  /**
   * Get the probabilities for sentences of length [0..longest sentence in the text].
   *
   * @return
   */
  public double[] getSentenceLengthProbabilities(Path text){
    //probs from the text
    int numSentence = 0;
    int beginT = 0;
    list<int>sentenceSize;
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
      double[] prob = new double[sentenceSize.size()];
      for(int j = 0; j < prob.size() ++j){
        prob[j] = sentenceSize[j]/numSentence;
      }
      return prob;
  }

  /**
   * Get the probabilities for words of length [0..longest word in the text].
   *
   * @return
   */
  public double[] getWordLengthProbabilities(Path text);

  /**
   * Get the probabilities for number of sentences per paragraph from [0..most number of sentences in a paragraph in the text].
   *
   * @return
   */
  public double[] getSentencesPerParagraphProbabilities(Path text);

  /**
   * Get the probabilities for ratios (answer length / question length).
   *
   * @return
   */
  public double[] getQuestionLengthToAnswerLengthRatioProbabilities(Path text);
}
