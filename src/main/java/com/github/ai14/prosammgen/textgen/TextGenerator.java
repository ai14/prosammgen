package com.github.ai14.prosammgen.textgen;

import com.google.common.collect.ImmutableList;

import java.util.Random;
import java.util.function.Function;

public interface TextGenerator {

  String generateText(Context context);

  public interface Context {

    Random getRandom();

    TextGenerator getProduction(String name);

    Function<ImmutableList<String>, TextGenerator> getMacro(String name);
  }
}
