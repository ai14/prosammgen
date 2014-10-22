package com.github.ai14.prosammgen;

import com.google.common.collect.ImmutableSet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class TextSource {
  public static final Path CACHE = Paths.get("cache");

  public TextSource() throws IOException {
    // Create cache directory.
    if (!Files.exists(CACHE)) {
      Files.createDirectory(CACHE);
    }
  }

  public abstract ImmutableSet<Path> getTexts(ImmutableSet<String> searchTerms, int resultsLimit) throws IOException;
}
