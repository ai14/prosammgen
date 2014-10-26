package com.github.ai14.prosammgen.textgen;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

import java.util.Random;

public interface TextGenerator {

  void generateText(Context context);

  public interface Context {

    Random getRandom();

    TextGenerator getProduction(String name);

    Function<? super ImmutableList<String>, ? extends TextGenerator> getMacro(String name);

    StringBuilder getBuilder();
  }
}
