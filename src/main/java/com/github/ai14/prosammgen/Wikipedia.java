package com.github.ai14.prosammgen;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.net.UrlEscapers;
import opennlp.tools.sentdetect.SentenceDetectorME;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Wikipedia extends TextSource {
  public Wikipedia(SentenceDetectorME sentenceDetector, String outputDirectory) throws IOException {
    super(
            outputDirectory,
            "wikipedia",
            Pattern.compile("<extract xml:space=\"preserve\">(.*?)<\\/extract>", Pattern.DOTALL),
            sentenceDetector
    );
  }

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

      // Fetch Wikipedia articles.
      PrintWriter out = null;
      try {
        out = new PrintWriter(new BufferedWriter(new FileWriter(p.toString())));
        // “Because excerpts generation can be slow the limit is capped at one whole-page extract.” Solution: do several requests. Sorry Wikipedia!
        for (int i = 0; i < requestsPerSearchterm; ++i) {
          // Read url into a huge string, extract content from file, extract running text and store the results.
          Scanner s = null;
          try {
            s = new Scanner(new URL("http://en.wikipedia.org/w/api.php?format=xml&action=query&generator=search&gsrsearch=" + UrlEscapers.urlPathSegmentEscaper().escape(searchTerm) + "&gsrlimit=50&prop=extracts&exsectionformat=plain&explaintext&excontinue=" + i).openStream());
            s.useDelimiter("\\Z");
            String response = StringEscapeUtils.unescapeHtml4(s.next());
            Matcher m = contentPattern.matcher(response);
            if (!m.find()) continue;
            String content = m.group(1);
            String runningText = extractRunningText(content);
            out.println(runningText);
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
