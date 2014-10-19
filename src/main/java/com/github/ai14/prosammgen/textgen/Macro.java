package com.github.ai14.prosammgen.textgen;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

public class Macro implements TextGenerator {

  private final String name;
  private final ImmutableList<String> args;

  public Macro(String name, ImmutableList<String> args) {
    this.name = name;
    this.args = args;
  }

  @Override
  public String generateText(Context context) {
    return context.getMacro(name).apply(args).generateText(context);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Macro macro = (Macro) o;

    if (!args.equals(macro.args)) {
      return false;
    }
    if (!name.equals(macro.name)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + args.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return name + "(" + Joiner.on(", ").join(args) + ")";
  }
}
