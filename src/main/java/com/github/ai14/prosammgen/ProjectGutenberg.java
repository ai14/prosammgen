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
  private Path gutenbergMirror = Paths.get("res/gutenberg");

  public ProjectGutenberg(NLPModel nlp) throws IOException, InterruptedException {
    super(
            nlp,
            Paths.get("cache/gutenberg"),
            Pattern.compile("(\\*\\*\\* START OF THIS PROJECT GUTENBERG EBOOK [\\w\\s]+ \\*\\*\\*)(.*?)(\\*\\*\\* END OF THIS PROJECT GUTENBERG EBOOK [\\w\\s]+ \\*\\*\\*)", Pattern.DOTALL)
    );
    if (!Files.exists(gutenbergMirror)) Files.createDirectories(gutenbergMirror);
    Process p = new ProcessBuilder()
            .redirectError(INHERIT)
            .redirectOutput(INHERIT)
            .command("rsync", "-av", "--del", "--include='*/'", "--include='*.txt'", "--exclude='*'", "ftp@ftp.ibiblio.org::gutenberg", gutenbergMirror.toString())
            .start();
    p.waitFor();
  }

  @Override
  public ImmutableSet<Path> getTexts(ImmutableSet<String> searchTerms, int resultsLimit) throws IOException {
    List<Path> results = new ArrayList<>();

    //TODO Lookup paths to relevant books with an index. For now, just go through all of the data (which will take a really really long time...).

    for (Path book : getBooks()) {
      Path p = cache.resolve(book.getFileName());

      // Skip parsing book if it's been done before.
      if (Files.exists(p)) continue;

      try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(p.toString())))) {
        try (Scanner s = new Scanner(book.toString())) {

          // Read file into a huge string. // TODO Beware of huge books.
          s.useDelimiter("\\Z");
          String response = s.next();

          // Skip non-english books.
          if (!response.contains("Language: English")) continue;

          // Look through the book for a search term, and consider the book relevant when a search term is found.
          boolean relevant = false;
          for (String searchTerm : searchTerms) {
            if (response.contains(searchTerm)) {
              relevant = true;
              break;
            }
          }
          if (!relevant) continue;

          // Extract content from file, extract running text and store the results.
          Matcher m = contentPattern.matcher(response);
          if (!m.find()) continue;
          out.println(extractRunningText(m.group(2)));
        }
      }

      results.add(p);
    }

    return ImmutableSet.copyOf(results);
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
