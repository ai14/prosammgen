package com.github.ai14.prosammgen;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Function;


public class WritingStyle {
  /**
   * The probability densities for word lengths. wordLengthProbabilities[i] is P(word of length i).
   */
  public final double[] wordLengthProbabilities;

  /**
   * The probability densities for sentence lengths. sentenceLengthProbabilities[i] is P(sentence of length i).
   */
  public final double[] sentenceLengthProbabilities;

  /**
   * The probability densities for paragraph lengths. paragraphLengthProbabilities[i] is P(paragraph of length i).
   */
  public final double[] paragraphLengthProbabilities;

  /**
   * Analyse the author's writing style of the input text.
   *
   * @param text
   */
  public WritingStyle(Path text) throws Exception {
    String content = new String(Files.readAllBytes(text));

    Function<String, double[]> probabilities = (String delimiter) -> {
      String[] subs = content.split(delimiter);
      int longestString = Arrays.stream(subs).mapToInt(s -> s.length()).max().getAsInt();
      int[] occurrences = new int[longestString];
      for (String s : subs) ++occurrences[s.length()];
      double sum = Arrays.stream(occurrences).sum();
      return Arrays.stream(occurrences).asDoubleStream().map(i -> i / sum).toArray();
    };

    wordLengthProbabilities = probabilities.apply("\\s+");
    sentenceLengthProbabilities = probabilities.apply("\\p{P}+");
    paragraphLengthProbabilities = probabilities.apply("[\\n\\r]+");
  }
}
