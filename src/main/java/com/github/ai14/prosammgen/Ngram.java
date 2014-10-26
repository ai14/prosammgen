package com.github.ai14.prosammgen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.LinkedList;

public class Ngram {
  private final int order;
  private LinkedList<String> ngram;

  public Ngram(int order) {
    this.order = order;
    ngram = Lists.newLinkedList();

    for (int i = 0; i < order; i++) {
      ngram.add("");
    }
  }

  public Ngram(Ngram ngramToCopy) {
    this.order = ngramToCopy.order;
    this.ngram = Lists.newLinkedList(ngramToCopy.ngram);
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
    StringBuilder sb = new StringBuilder();

    for (String s : ngram) {
      if (s.length() > 0) {
        sb.append(s);
        sb.append(" ");
      }
    }

    return sb.toString();
  }
}
