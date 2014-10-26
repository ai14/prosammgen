package com.github.ai14.prosammgen;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.io.CharSource;
import com.google.common.io.Resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class WordNet {

  private static final Splitter SPLITTER = Splitter.on('|');
  private final ImmutableMultimap<String, String> synonymsForWords;

  private WordNet(ImmutableMultimap<String, String> synonymsForWords) {
    this.synonymsForWords = synonymsForWords;
  }

  public static WordNet load(URL dbUrl) throws IOException {
    CharSource dataSource = Resources.asCharSource(dbUrl, StandardCharsets.ISO_8859_1);
    ImmutableMultimap.Builder<String, String> synonymsBuilder = ImmutableMultimap.builder();

    BufferedReader br = null;
    try {
      br = dataSource.openBufferedStream();

      if (!"ISO8859-1".equals(br.readLine())) {
        throw new RuntimeException("Unexpected synonym DB encoding");
      }

      String line;
      while ((line = br.readLine()) != null) {
        // Data format:
        // word|no. of entries
        // (wordType)|synonym|synonym|synonym (related term)|synonym (generic term)|...

        if (line.startsWith("(")) {
          // The .dat file is a bit weird, and this is some faulty data... Just skip
          continue;
        }

        List<String> headerFields = SPLITTER.splitToList(line);
        if (headerFields.size() != 2) {
          // Again, just skip
          continue;
        }

        String word = headerFields.get(0);
        int entries = Integer.parseInt(headerFields.get(1));

        while (entries-- > 0 && (line = br.readLine()) != null) {
          List<String> fields = SPLITTER.splitToList(line);
          fields = fields.subList(1, fields.size()); // Skip the first element, the word type

          for (String synonym : fields) {
            int paren = synonym.indexOf('(');
            if (paren >= 0) {
              if (synonym.substring(paren + 1).startsWith("antonym")) {
                // Not a synonym
                continue;
              }
              synonym = synonym.substring(0, paren).trim();
            }
            synonymsBuilder.put(word, synonym);
          }
        }
      }
    } finally {
      if (br != null) {
        br.close();
      }
    }

    return new WordNet(synonymsBuilder.build());
  }

  /**
   * Get synonyms for the input words, including the input words.
   */
  public ImmutableSet<String> getSynonyms(ImmutableList<String> words) {
    // Capitalize output if all input words are capitalized
    boolean capitalize = Iterables.all(words, IsCapitalized.INSTANCE);

    Set<String> result = null;
    for (String word : words) {
      Collection<String> synonyms = synonymsForWords.get(word);

      if (synonyms != null) {
        // Found synonyms; add the word itself and make into a set for easier handling
        ImmutableSet<String> synonymSet =
            ImmutableSet.<String>builder().add(word).addAll(synonyms).build();

        if (result == null) {
          // This is the first word, add all synonyms
          result = synonymSet;
        } else {
          // This is not the first word; only limit the synonym set to words that are also synonym
          // with the current word.
          result = Sets.intersection(result, synonymSet);
        }
      }
    }

    if (result == null) {
      // words was empty... which is fine I guess, return no synonyms
      return ImmutableSet.of();
    } else {
      // Return the synonyms properly capitalized
      return ImmutableSet.copyOf(
          capitalize ? Iterables.transform(result, Capitalizer.INSTANCE) : result);
    }
  }

  private enum IsCapitalized implements Predicate<String> {
    INSTANCE;

    @Override
    public boolean apply(String input) {
      return Character.isUpperCase(input.charAt(0));
    }
  }

  private enum Capitalizer implements Function<String, String> {
    INSTANCE;

    @Override
    public String apply(String input) {
      return Character.toUpperCase(input.charAt(0)) + input.substring(1);
    }
  }
}
