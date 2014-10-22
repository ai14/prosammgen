package com.github.ai14.prosammgen;

import com.google.common.collect.ImmutableSet;
import com.google.common.net.UrlEscapers;
import opennlp.tools.sentdetect.SentenceDetectorME;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Wikipedia extends TextSource {

  private final Pattern searchResultsPattern, articleContentPattern, runningTextPattern;
  private final SentenceDetectorME sentenceDetector;
  private final Path localCache = CACHE.resolve("wikipedia");

  public Wikipedia(NLPModel nlp) throws IOException {
    super();
    searchResultsPattern = Pattern.compile("<searchinfo totalhits=\"([0-9]+)\" \\/>");
    articleContentPattern = Pattern.compile("<extract xml:space=\"preserve\">(.*?)<\\/extract>", Pattern.DOTALL);
    runningTextPattern = Pattern.compile("^([A-Z]\\w* ([\\w(),:'‘’\\-%/]* ){2,}([\\w(),:'‘’-]*[.,!?]) ){2,}", Pattern.MULTILINE);
    sentenceDetector = new SentenceDetectorME(nlp.getSentenceModel());
  }

  @Override
  public ImmutableSet<Path> getTexts(ImmutableSet<String> searchTerms, int resultsLimit) throws IOException {
    List<Path> searchResults = new ArrayList<Path>();

    // Limit requests per search term to the maximum defined by the MediaWiki API Search extension.
    int requestsPerSearchterm = 1 + resultsLimit / (1 + searchTerms.size());
    if (requestsPerSearchterm > 50) {
      requestsPerSearchterm = 50;
    }

    for (String searchTerm : searchTerms) {

      // Use cached file for search term instead, if fresh enough.
      Path p = localCache.resolve(searchTerm);
      if (Files.exists(p)) {
        if (System.currentTimeMillis() - Files.getLastModifiedTime(p).toMillis() < 2592000000l) {
          searchResults.add(p);
          continue;
        } else {
          Files.delete(p);
        }
      }

      // Fetch Wikipedia resultsLimit.
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

            // Find running text (get rid of meta data and so on).
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
      }

      // Store resulting resultsLimit in a file for the search term.
      searchResults.add(p);
    }

    return ImmutableSet.copyOf(searchResults);
  }
}
