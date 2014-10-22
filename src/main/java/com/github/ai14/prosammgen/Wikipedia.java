package com.github.ai14.prosammgen;

import com.google.common.collect.ImmutableSet;
import com.google.common.net.UrlEscapers;
import org.apache.commons.lang3.StringEscapeUtils;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Wikipedia {

  private static final Pattern searchResultsPattern = Pattern.compile("<searchinfo totalhits=\"([0-9]+)\" \\/>");
  private static final Pattern articleContentPattern = Pattern.compile("<extract xml:space=\"preserve\">(.*?)<\\/extract>", Pattern.DOTALL);
  private static final Pattern scrollingTextPattern = Pattern.compile("(.*)[!.?](.*)[!.?]");

  /**
   * Download Wikipedia articles with the MediaWiki API and parse out article content as plaintext.
   *
   * @param articles
   * @param searchTerms
   * @return
   */
  public static ImmutableSet<Path> getArticles(int articles, ImmutableSet<String> searchTerms) throws IOException {
    List<Path> searchResults = new ArrayList<>();

    // Create cache directory.
    Path cache = Paths.get("cache");
    if (!Files.exists(cache)) {
      Files.createDirectory(cache);
    }

    // Limit requests per search term to the maximum defined by the MediaWiki API Search extension.
    int requestsPerSearchterm = 1 + articles / (1 + searchTerms.size());
    if (requestsPerSearchterm > 50) {
      requestsPerSearchterm = 50;
    }

    for (String searchTerm : searchTerms) {

      // Use cached file for search term instead, if fresh enough.
      Path p = Paths.get("cache/" + searchTerm);
      if (Files.exists(p)) {
        if (System.currentTimeMillis() - Files.getLastModifiedTime(p).toMillis() < 2592000000l) {
          searchResults.add(p);
          continue;
        } else {
          Files.delete(p);
        }
      }

      // Fetch Wikipedia articles.
      try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(p.toString())))) {

        // “Because excerpts generation can be slow the limit is capped at one whole-page extract.” Solution: do several requests. Sorry Wikipedia!
        for (int i = 0; i < requestsPerSearchterm; ++i) {
          String url = "http://en.wikipedia.org/w/api.php?format=xml&action=query&generator=search&gsrsearch=" + UrlEscapers.urlPathSegmentEscaper().escape(searchTerm) + "&gsrlimit=50&prop=extracts&exsectionformat=plain&explaintext&excontinue=" + i;
          try (Scanner s = new Scanner(new URL(url).openStream())) {

            // Read url into a huge string.
            s.useDelimiter("\\Z");
            String response = s.next();

            // Extract article content from the response.
            Matcher m1 = articleContentPattern.matcher(response);
            if (!m1.find()) continue;
            String content = m1.group(1);

            // Convert HTML entities to unicode.
            content = StringEscapeUtils.unescapeHtml4(content);

            // Only keep paragraphs of at least two sentences (in order to filter out meta data sections such as "External links").
            Matcher m2 = scrollingTextPattern.matcher(content);
            while (m2.find()) {
              String paragraph = m2.group();
              out.println(paragraph);
            }
          }
        }
      }

      // Store resulting articles in a file for the search term.
      searchResults.add(p);
    }

    return ImmutableSet.copyOf(searchResults);
  }
}
