package com.github.ai14.prosammgen;

import java.util.LinkedList;

public class Ngram {
  private final int order;
  private LinkedList<String> ngram;

  public Ngram(int order) {
    this.order = order;
    ngram = new LinkedList<>();

    for (int i = 0; i < order; i++) {
      ngram.add("");
    }
  }

  public void pushWord(String word) {
    ngram.addLast(word);
    ngram.removeFirst();
  }

  public String getLast() {
    return ngram.getLast();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (String s : ngram) {
      if (s.length() > 0) {
        sb.append(s + " ");
      }
    }

    if (sb.length() > 0) {
      sb.deleteCharAt(sb.length() - 1);
    }

    return sb.toString();
  }
}
