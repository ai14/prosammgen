package com.github.ai14.prosammgen;

import java.nio.file.Path;

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
  public double[] getSentenceLengthProbabilities();

  /**
   * Get the probabilities for words of length [0..longest word in the text].
   *
   * @return
   */
  public double[] getWordLengthProbabilities();

  /**
   * Get the probabilities for number of sentences per paragraph from [0..most number of sentences in a paragraph in the text].
   *
   * @return
   */
  public double[] getSentencesPerParagraphProbabilities();

  /**
   * Get the probabilities for ratios (answer length / question length).
   *
   * @return
   */
  public double[] getQuestionLengthToAnswerLengthRatioProbabilities();
}
