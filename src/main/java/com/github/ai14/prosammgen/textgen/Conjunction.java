package com.github.ai14.prosammgen.textgen;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

public class Conjunction implements TextGenerator {

  private final ImmutableList<TextGenerator> generators;

  public Conjunction(ImmutableList<TextGenerator> generators) {
    this.generators = generators;
  }

  public Conjunction(TextGenerator... generators) {
    this(ImmutableList.copyOf(generators));
  }

  @Override
  public void generateText(Context context) {
    for (TextGenerator generator : generators) {
      generator.generateText(context);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Conjunction that = (Conjunction) o;

    if (!generators.equals(that.generators)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return generators.hashCode();
  }

  @Override
  public String toString() {
    return Joiner.on(' ').join(generators);
  }

}
