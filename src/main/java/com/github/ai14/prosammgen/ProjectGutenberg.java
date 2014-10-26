package com.github.ai14.prosammgen;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import java.io.*;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//TODO wget relevant books directly instead of relying on a mirrored resource.
public class ProjectGutenberg extends TextSource {
  public ProjectGutenberg(NLPModel nlp) throws IOException, InterruptedException {
    super(
            nlp,
            new File("cache/gutenberg"),
            Pattern.compile("(\\*\\*\\* START OF THIS PROJECT GUTENBERG EBOOK [\\w\\s]+ \\*\\*\\*)(.*?)(\\*\\*\\* END OF THIS PROJECT GUTENBERG EBOOK [\\w\\s]+ \\*\\*\\*)", Pattern.DOTALL)
    );
  }

  @Override
  public ImmutableSet<File> getTexts(ImmutableSet<String> searchTerms, int resultsLimit) throws IOException {
    List<File> results = Lists.newArrayList();

    //TODO Lookup paths to relevant books with an index. For now, just go through all of the data (which will take a really really long time...).

    for (File book : getBooks()) {
      File p = new File(cache, book.getName());

      // Skip parsing book if it's been done before.
      if (p.exists()) continue;
      PrintWriter out = null;
      try {
        out = new PrintWriter(new BufferedWriter(new FileWriter(p.toString())));
        Scanner s = null;
        try {
          s = new Scanner(book.toString());

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
        } finally {
          if (s != null) {
            s.close();
          }
        }
      } finally {
        if (out != null) {
          out.close();
        }
      }

      results.add(p);
    }

    return ImmutableSet.copyOf(results);
  }

  private List<File> getBooks() throws IOException {
    return (getPathsToTextFiles(Lists.<File>newArrayList(), cache));
  }

  private List<File> getPathsToTextFiles(List<File> paths, File directory) throws IOException {
    for (File p : directory.listFiles()) {
      if (p.isDirectory()) getPathsToTextFiles(paths, p);
      else if (p.getName().endsWith(".txt")) paths.add(p);
    }
    return paths;
  }
}
