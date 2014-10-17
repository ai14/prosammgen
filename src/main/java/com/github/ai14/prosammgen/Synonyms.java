package com.github.ai14.prosammgen;

import java.util.List;

interface Synonyms {
  /**
   * Get a random synonym for the input words. If no synonyms can be found the input words are returned.
   *
   * @param word
   * @return
   */
  public String getSynonym(String... word);

  /**
   * Get all synonyms for the input words. If no synonyms can be found a list containing only the input words is returned.
   *
   * @param word
   * @return
   */
  public List<String> getSynonyms(String... word);
}
