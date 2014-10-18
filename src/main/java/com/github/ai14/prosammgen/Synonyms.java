package com.github.ai14.prosammgen;

import java.util.List;
import java.util.Random;

interface Synonyms {
  static final Random random = new Random();

  /**
   * Get a random synonym for the input words. If no synonyms can be found the input words are returned. Make sure the input words are actually synonyms to each other.
   *
   * @param words
   * @return
   */
  public default String getSynonym(String... words) {
    List<String> synonyms = getSynonyms(words);
    return synonyms.get(random.nextInt(synonyms.size()));
  }

  /**
   * Get all synonyms for the input words. If no synonyms can be found a list containing only the input words is returned. Make sure the input words are actually synonyms to each other.
   *
   * @param words
   * @return
   */
  public List<String> getSynonyms(String... words);
}
