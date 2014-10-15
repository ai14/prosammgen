package com.github.ai14.prosammgen;

import java.util.List;

interface Synonyms {
    /**
     * Get a random synonym for the input word. If no synonyms can be found the input word is returned.
     * @param word
     * @return
     */
    public String getSynonym(String word);

    /**
     * Get all synonyms for the input word. If no synonyms can be found a list containing only the input word is returned.
     * @param word
     * @return
     */
    public List<String> getSynonyms(String word);
}
