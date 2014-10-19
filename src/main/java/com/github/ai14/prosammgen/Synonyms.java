package com.github.ai14.prosammgen;

import com.google.common.collect.ImmutableList;

public interface Synonyms {

  /**
   * Get all synonyms for the input words. If no synonyms can be found a list containing only the
   * input words is returned. Make sure the input words are actually synonyms to each other.
   */
  public ImmutableList<String> getSynonyms(ImmutableList<String> words);
}
