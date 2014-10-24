package com.github.ai14.prosammgen.textgen;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class Disjunction implements TextGenerator {

  private final ImmutableSet<TextGenerator> generators;

  public Disjunction(ImmutableSet<TextGenerator> generators) {
    this.generators = generators;
  }

  @Override
  public void generateText(Context context) {
    Iterables.get(generators, context.getRandom().nextInt(generators.size()))
        .generateText(context);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Disjunction that = (Disjunction) o;

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
    return Joiner.on("\n  | ").join(generators);
  }

}
