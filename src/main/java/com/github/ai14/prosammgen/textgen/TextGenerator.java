package com.github.ai14.prosammgen.textgen;

import java.util.Random;

public interface TextGenerator {

  String generateText(Context context);

  public interface Context {

    Random getRandom();

    TextGenerator getProduction(String name);
  }
}
