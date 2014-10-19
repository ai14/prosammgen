package com.github.ai14.prosammgen;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class WikipediaArticles implements TextSource {
  private String[] searchTerms;
  private int requestsPerSearchterm;

  /**
   * Download plaintext article content from Wikipedia with the MediaWiki API.
   *
   * @param articles
   * @param searchTerms
   */
  public WikipediaArticles(int articles, String... searchTerms) {
    this.searchTerms = searchTerms;
    this.requestsPerSearchterm = articles / searchTerms.length;
  }

  @Override
  public Path[] getTexts() {
    List<Path> articles = new ArrayList<>();
    try {
      for (String searchTerm : searchTerms) {

        // Use cached file instead, if fresh enough.
        Path p = Paths.get(searchTerm + ".txt");
        if (Files.exists(p) && System.currentTimeMillis() - Files.getLastModifiedTime(p).toMillis() < 2592000000l) {
          articles.add(p);
          continue;
        } else {
          Files.delete(p);
        }

        // Fetch fresh Wikipedia articles.
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(searchTerm + ".txt")));
        int gaplimit = requestsPerSearchterm; // “Because excerpts generation can be slow the limit is capped at one whole-page extract.” Solution: do several requests. Sorry Wikipedia!
        for (int i = 0; i < gaplimit; i++) {
          String url = "http://en.wikipedia.org/w/api.php";
          String query = "?format=xml&action=query&generator=allpages&gaplimit=" + gaplimit + "&gapfrom=" + searchTerm + "&prop=extracts&exsectionformat=plain&explaintext&excontinue=" + i;

          // Read url into a huge string.
          Scanner s = new Scanner(new URL(url + query).openStream());
          s.useDelimiter("\\Z");
          String response = s.next();

          // Extract article content from the response.
          String content = response.replaceAll("\\<.*?\\>", ""); //TODO Don't strip actual text content surrounded by < and >.

          // Write article content to file.
          out.print(content);
        }
        articles.add(Paths.get(searchTerm + ".txt"));
      }
    } catch (IOException e) {
      System.err.println("Wikipedia could not be retrieved.");
    }

    return articles.toArray(new Path[0]);
  }
}
