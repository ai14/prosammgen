package com.github.ai14.prosammgen;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class WordNetSynonyms implements Synonyms {
  private static final String index = "res/wordnet.idx", data = "res/wordnet.dat";

  @Override
  public List<String> getSynonyms(String... word) {
    //TODO Cross reference database with all input words.
    ArrayList<String> synonyms = new ArrayList<>();
    boolean capitalize = Character.isUpperCase(word[0].charAt(0));
    word[0] = word[0].toLowerCase();
    synonyms.add(word[0]); // The input words are always thought of as synonyms to themselves.

    try {
      // Lookup the word in the smaller index file to find the byte offset for the word in the data file.
      long offset = -1;
      BufferedReader br = new BufferedReader(new FileReader(index));
      String line;
      // TODO Lookup in the index smarter than reading through the file.
      while ((line = br.readLine()) != null) {
        String[] ss = line.split("\\|");
        if (ss[0].equals(word)) offset = Long.parseLong(ss[1]);
      }
      br.close();

      // If word exists, lookup synonyms.
      if (offset != -1) {
        RandomAccessFile raf = new RandomAccessFile(data, "r"); // Read-only
        raf.seek(offset);

        // Data format:
        // word|no. of entries
        // (wordType)|synonym|synonym|synonym (related term)|synonym (generic term)|...
        int entries = Integer.parseInt(raf.readLine().split("\\|")[1]);
        for (int entry = 0; entry < entries; ++entry) {
          String[] entryFields = raf.readLine().split("\\|");
          String wordType = entryFields[0]; // TODO Perhaps use NLP logic in the text generation? Verb, noun, adj info is available here.
          for (int field = 1; field < entryFields.length; ++field) {
            // TODO Currently skipping related terms, generic terms, antonyms, etc. Maybe use them in the text generation?
            if (entryFields[field].contains("(")) {
              continue;
            }

            synonyms.add(entryFields[field]);
          }
        }
      }
    } catch (IOException e) {
      System.err.println("Synonym database unavailable. Returning the input word only.");
    } finally {
      if (capitalize) {
        for (int i = 0; i < synonyms.size(); ++i) {
          synonyms.set(i, Character.toUpperCase(synonyms.get(i).charAt(0)) + synonyms.get(i).substring(1));
        }
      }
      return synonyms;
    }
  }
}
