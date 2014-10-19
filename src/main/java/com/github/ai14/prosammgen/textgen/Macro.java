package com.github.ai14.prosammgen.textgen;

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
}
