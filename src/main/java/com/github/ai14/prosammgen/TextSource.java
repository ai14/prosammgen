package com.github.ai14.prosammgen;

import java.nio.file.Path;

public interface TextSource {

  /**
   * Get texts that the Markov chain can train on.
   *
   * @return
   */
  public Path[] getTexts();
}
