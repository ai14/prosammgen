package com.github.ai14.prosammgen.textgen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import com.github.ai14.prosammgen.WordNet;

public class SynonymGenerator implements TextGenerator {

  private final WordNet wordNet;
  private final ImmutableList<String> words;

  public SynonymGenerator(WordNet wordNet, ImmutableList<String> words) {
    this.wordNet = wordNet;
    this.words = words;
  }

  @Override
  public String generateText(Context context) {
    ImmutableSet<String> candidates = wordNet.getSynonyms(words);
    return Iterables.get(candidates, context.getRandom().nextInt(candidates.size()));
  }
}
