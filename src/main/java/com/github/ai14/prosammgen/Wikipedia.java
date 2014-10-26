package com.github.ai14.prosammgen;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.net.UrlEscapers;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Wikipedia extends TextSource {

  public Wikipedia(NLPModel nlp) throws IOException {
    super(
            nlp,
            new File("cache/wikipedia"),
            Pattern.compile("<extract xml:space=\"preserve\">(.*?)<\\/extract>", Pattern.DOTALL)
    );
  }

  @Override
  public ImmutableSet<File> getTexts(ImmutableSet<String> searchTerms, int resultsLimit) throws IOException {
    List<File> results = Lists.newArrayList();

    // Limit requests per search term to the maximum defined by the MediaWiki API Search extension.
    int requestsPerSearchterm = 1 + resultsLimit / (1 + searchTerms.size());
    if (requestsPerSearchterm > 50) {
      requestsPerSearchterm = 50;
    }

    for (String searchTerm : searchTerms) {

      // Use cached file for search term instead, if fresh enough.
      File p = new File(cache, searchTerm);
      if (p.exists()) {
        if (System.currentTimeMillis() - p.lastModified() < 2592000000l) {
          results.add(p);
          continue;
        } else {
          p.delete();
        }
      }

      // Fetch Wikipedia resultsLimit.
      PrintWriter out = null;
      try {
        out = new PrintWriter(new BufferedWriter(new FileWriter(p.toString())));
        // “Because excerpts generation can be slow the limit is capped at one whole-page extract.” Solution: do several requests. Sorry Wikipedia!
        for (int i = 0; i < requestsPerSearchterm; ++i) {
          String url = "http://en.wikipedia.org/w/api.php?format=xml&action=query&generator=search&gsrsearch=" + UrlEscapers.urlPathSegmentEscaper().escape(searchTerm) + "&gsrlimit=50&prop=extracts&exsectionformat=plain&explaintext&excontinue=" + i;
          Scanner s = null;
          try {
            s = new Scanner(new URL(url).openStream());
            // Read url into a huge string, extract content from file, extract running text and store the results.
            s.useDelimiter("\\Z");
            Matcher m1 = contentPattern.matcher(s.next());
            if (!m1.find()) continue;
            out.println(extractRunningText(StringEscapeUtils.unescapeHtml4(m1.group(1))));
          } finally {
            if (s != null) {
              s.close();
            }
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
}
