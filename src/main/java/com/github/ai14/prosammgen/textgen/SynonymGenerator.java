package com.github.ai14.prosammgen.textgen;

import com.github.ai14.prosammgen.WordNet;
import com.google.common.collect.ImmutableList;

public class SynonymGenerator implements TextGenerator {

  private final ImmutableList<String> words;

  public SynonymGenerator(ImmutableList<String> words) {
    this.words = words;
  }

  @Override
  public String generateText(Context context) {
    ImmutableList<String> candidates = WordNet.getSynonyms(words);
    return candidates.get(context.getRandom().nextInt(candidates.size()));
  }
}
