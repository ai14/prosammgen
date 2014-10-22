package com.github.ai14.prosammgen;

import com.google.common.collect.ImmutableSet;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.ProcessBuilder.Redirect.INHERIT;

public class ProjectGutenberg extends TextSource {
  private static final Path localCache = CACHE.resolve("gutenberg");
  private final Pattern contentPattern;
  private Path gutenbergMirror;

  public ProjectGutenberg(NLPModel nlp) throws IOException, InterruptedException {
    super(nlp, localCache);
    gutenbergMirror = Paths.get("res/gutenberg");
    if (!Files.exists(gutenbergMirror)) Files.createDirectory(gutenbergMirror);
    contentPattern = Pattern.compile("(\\*\\*\\* START OF THIS PROJECT GUTENBERG EBOOK [\\w\\s]+ \\*\\*\\*)(.*?)(\\*\\*\\* END OF THIS PROJECT GUTENBERG EBOOK [\\w\\s]+ \\*\\*\\*)", Pattern.MULTILINE);
    Process p = new ProcessBuilder()
            .redirectError(INHERIT)
            .redirectOutput(INHERIT)
            .command("rsync", "-av", "--del", "--include=*/", "--include='*.txt'", "--exclude='*'", "ftp@ftp.ibiblio.org::gutenberg", gutenbergMirror.toString())
            .start();
    p.waitFor();
  }

  @Override
  public ImmutableSet<Path> getTexts(ImmutableSet<String> searchTerms, int resultsLimit) throws IOException {
    List<Path> bookTexts = new ArrayList<>();

    //TODO Lookup paths to relevant books. For now, just go through all of it (which will take a really really long time...).
    for (Path book : getBooks()) {
      Path p = localCache.resolve(book.getFileName());

      try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(p.toString())))) {
        try (Scanner s = new Scanner(book.toString())) {

          // Read file into a huge string. // TODO Beware of huge books.
          s.useDelimiter("\\Z");
          String response = s.next();

          // Skip non-english books. TODO Don't check non-english books at all!
          if (!response.contains("Language: English")) continue;

          // Look through the book for a search term, and consider the book relevant when a search term is found. //TODO Do this smarter. This will be super slow!
          boolean relevant = false;
          for (String searchTerm : searchTerms) {
            if (response.contains(searchTerm)) {
              relevant = true;
              break;
            }
          }
          if (!relevant) continue;

          // Extract book content from file.
          Matcher m = contentPattern.matcher(response);
          m.find();
          String content = m.group(2);
          System.err.println(content); //TODO Remove.

          // Find running text.
          Matcher m2 = runningTextPattern.matcher(content);
          while (m2.find()) {
            String paragraph = m2.group();

            // Only keep grammar correct sentences (using OpenNLP).
            for (String sentence : sentenceDetector.sentDetect(paragraph)) {
              out.print(sentence + " ");
            }
            out.println();
          }
        }
      }
      bookTexts.add(p);
    }

    return ImmutableSet.copyOf(bookTexts);
  }

  private List<Path> getBooks() throws IOException {
    return (getPathsToTextFiles(new ArrayList<>(), gutenbergMirror));
  }

  private List<Path> getPathsToTextFiles(List<Path> paths, Path directory) throws IOException {
    DirectoryStream ds = Files.newDirectoryStream(directory);
    for (Path p : Files.newDirectoryStream(directory)) {
      if (Files.isDirectory(p)) getPathsToTextFiles(paths, p);
      else if (p.endsWith(".txt")) paths.add(p);
    }
    ds.close();
    return paths;
  }
}
