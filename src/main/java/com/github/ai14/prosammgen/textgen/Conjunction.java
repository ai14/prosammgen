package com.github.ai14.prosammgen.textgen;

import com.google.common.base.MoreObjects;
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
  public String generateText(Context context) {
    final StringBuilder resultBuilder = new StringBuilder();

    for (TextGenerator generator : generators) {
      resultBuilder.append(generator.generateText(context));
    }

    return resultBuilder.toString();
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
    return MoreObjects.toStringHelper(this)
        .add("generators", generators)
        .toString();
  }

}
