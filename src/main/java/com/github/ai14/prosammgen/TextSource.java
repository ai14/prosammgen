package com.github.ai14.prosammgen;

import java.io.File;
import java.util.List;

public interface TextSource {

  /**
   * Get texts that the Markov chain can train on.
   *
   * @return
   */
  public List<File> getTexts();
}
