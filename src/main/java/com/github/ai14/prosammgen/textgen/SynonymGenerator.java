package com.github.ai14.prosammgen.textgen;

import com.google.common.collect.ImmutableList;

import com.github.ai14.prosammgen.Synonyms;

public class SynonymGenerator implements TextGenerator {

  private final ImmutableList<String> words;
  private final Synonyms synonyms;

  public SynonymGenerator(ImmutableList<String> words, Synonyms synonyms) {
    this.words = words;
    this.synonyms = synonyms;
  }

  @Override
  public String generateText(Context context) {
    ImmutableList<String> candidates = synonyms.getSynonyms(words);
    return candidates.get(context.getRandom().nextInt(candidates.size()));
  }
}
