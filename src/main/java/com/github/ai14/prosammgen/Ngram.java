package com.github.ai14.prosammgen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.LinkedList;
import java.util.StringJoiner;

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

  public Ngram(Ngram ngramToCopy) {
    this.order = ngramToCopy.order;
    this.ngram = new LinkedList<>();
    for (String s : ngramToCopy.ngram) {
      this.ngram.add(s);
    }
  }

  public void pushWord(String word) {
    ngram.addLast(word);
    ngram.removeFirst();
  }

  public String getLast() {
    return ngram.getLast();
  }

  public String getFirst() {
    return ngram.getFirst();
  }

  public ImmutableList<String> getAll() {
    return ImmutableList.copyOf(ngram);
  }

  public int order() {
    return order;
  }

  @Override
  public String toString() {
    StringJoiner str = new StringJoiner(" ");
    for (String s : ngram) {
      if (s.length() > 0) {
        str.add(s);
      }
    }

    return str.toString();
  }
}
