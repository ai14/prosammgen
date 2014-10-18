package com.github.ai14.prosammgen;

import java.io.File;

public interface TextSource {

  /**
   * Get texts that the Markov chain can train on.
   *
   * @return
   */
  public File[] getTexts();
}
