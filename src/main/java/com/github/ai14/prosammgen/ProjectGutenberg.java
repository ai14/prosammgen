package com.github.ai14.prosammgen;

import com.google.common.collect.ImmutableSet;

import java.io.IOException;
import java.nio.file.Path;

import static java.lang.ProcessBuilder.Redirect.INHERIT;

public class ProjectGutenberg extends TextSource {
  private static final Path localCache = CACHE.resolve("gutenberg");

  public ProjectGutenberg() throws IOException, InterruptedException {
    super(localCache);
    Process p = new ProcessBuilder()
            .redirectError(INHERIT)
            .redirectOutput(INHERIT)
            .command("rsync", "-av", "--del", "--include=*/", "--include='*.txt'", "--exclude='*'", "ftp@ftp.ibiblio.org::gutenberg", localCache.toString())
            .start();
    p.waitFor();
  }

  @Override
  public ImmutableSet<Path> getTexts(ImmutableSet<String> searchTerms, int resultsLimit) throws IOException {
    // TODO Find relevant books.
    return null;
  }
}
