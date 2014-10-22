package com.github.ai14.prosammgen;

import com.google.common.collect.ImmutableSet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class TextSource {
  public static final Path CACHE = Paths.get("cache");

  public TextSource(Path cache) throws IOException {
    // Create cache directory.
    if (!Files.exists(CACHE)) Files.createDirectory(CACHE);
    if (!Files.exists(cache)) Files.createDirectory(cache);
  }

  public abstract ImmutableSet<Path> getTexts(ImmutableSet<String> searchTerms, int resultsLimit) throws IOException;
}
