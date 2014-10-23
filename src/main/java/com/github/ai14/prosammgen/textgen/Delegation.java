package com.github.ai14.prosammgen.textgen;

import com.google.common.base.MoreObjects;

public class Delegation implements TextGenerator {

  private final String production;

  public Delegation(String production) {
    this.production = production;
  }

  @Override
  public void generateText(Context context) {
    context.getProduction(production).generateText(context);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Delegation that = (Delegation) o;

    if (!production.equals(that.production)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return production.hashCode();
  }

  @Override
  public String toString() {
    return production;
  }

}
