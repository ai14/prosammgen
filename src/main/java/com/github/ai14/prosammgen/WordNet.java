package com.github.ai14.prosammgen;

import com.google.common.collect.ImmutableList;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.stream.Collectors;

public class WordNet {

  // Uses a local copy of Princeton's WordNet, a synonyms database.
  private static final String index = "res/wordnet.idx", data = "res/wordnet.dat";

  /**
   * Get synonyms for the input words, including the input words.
   *
   * @param words
   * @return
   */
  public static ImmutableList<String> getSynonyms(ImmutableList<String> words) {
    List<String> synonyms = new ArrayList<>();
    for (String w : words) synonyms.add(w); // The input words are always thought of as synonyms to themselves.

    boolean capitalize = Character.isUpperCase(words.get(0).charAt(0)); //TODO Improve capitalization when cross-referencing.

    try {
      // Lookup word in the index file to find the byte offsets for the word's synonyms in the data file.
      Queue<Long> offsets = new LinkedList<>();
      try (BufferedReader br = new BufferedReader(new FileReader(index))) {
        String line;
        while ((line = br.readLine()) != null) { // TODO Lookup in the index smarter than reading through the file. Hashing, for example.
          String[] ss = line.split("\\|");
          if (ss[0].equals(words.get(0))) {
            offsets.add(Long.parseLong(ss[1])); // TODO Is it correct that soon-to-be cross-referenced words can always be found in the results when looking up the first word?
          }
        }
      }

      // If word exists, lookup synonyms.
      try (RandomAccessFile raf = new RandomAccessFile(data, "r")) {
        while (!offsets.isEmpty()) {
          raf.seek(offsets.remove());

          // Data format:
          // word|no. of entries
          // (wordType)|synonym|synonym|synonym (related term)|synonym (generic term)|...
          List<String> candidates = new ArrayList<>();
          HashMap<String, Boolean> seen = new HashMap<>();
          for (String w : words) seen.put(w, false);
          int found = 0;
          int entries = Integer.parseInt(raf.readLine().split("\\|")[1]);
          for (int entry = 0; entry < entries; ++entry) {
            String[] fields = raf.readLine().split("\\|");
            for (int field = 1; field < fields.length; ++field) {
              if (fields[field].contains("(antonym)")) {
                continue;
              }
              if (!seen.get(fields[field])) {
                seen.put(fields[field], true);
                ++found;
                candidates.add(fields[field]);
              }
            }
          }

          // Only keep synonyms if all cross-reference words were seen.
          if (found == words.size()) {
            synonyms.addAll(candidates);
          }
        }
      }
    } catch (IOException e) {
      System.err.println("Synonym database unavailable. Returning the input word only.");
    } finally {
      return ImmutableList.copyOf((capitalize) ? synonyms
              .stream()
              .map(s -> Character.toUpperCase(s.charAt(0)) + s.substring(1))
              .collect(Collectors.toList()) : synonyms);
    }
  }
}
