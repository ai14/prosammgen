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
  public String toString() {
    return name + "(" + Joiner.on(", ").join(args) + ")";
  }
}
