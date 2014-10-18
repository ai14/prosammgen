package com.github.ai14.prosammgen;

public interface TrainingText {

  /**
   * Get a text that the Markov chain can train on.
   *
   * @return
   */
  public String getText();
}
