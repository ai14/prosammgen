package com.github.ai14.prosammgen;

interface KeywordIdentifier {

  /**
   * Identify the keyword in a text.
   *
   * @param text
   * @return keyword
   */
  public String getKeyword(String text);
}
