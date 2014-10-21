package com.github.ai14.prosammgen;

import com.github.ai14.prosammgen.textgen.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.util.Random;
import java.util.function.Function;

public class Humanizer {

    private ImmutableSet<String> Words;
    Path TextResult;

    public Humanizer()throws IOException, ParseException {
        //Load words (both correct and misspelled ones)
        Words = ImmutableSet.copyOf(Files.readAllLines(Paths.get("res/words")));

        TextResult = null;
    }

    /**
     * * This "humanize" the text by making some misspelled words given a text, a misspellingRate (as higher the rate
     * less similarity between words).
     * @param text Text to humanize
     * @param misspellingRate Ratio of the mistakes we want
     * @return
     */
    public Path TextHumanizer(Path text, double misspellingRate)throws IOException, ParseException {
        for (String line : Files.readAllLines(text)){
            String [] possibleMisspellingWords = null;
            //Find misspelling words for that word
            possibleMisspellingWords = checkForPossibleMisspellingWords(line);
            //if there is several option, choose one
            if(possibleMisspellingWords != null) {
                String misspelledWord = possibleMisspellingWords[0];
                if (possibleMisspellingWords.length > 1) {
                    //TODO: check for the more suitable word
                }
                //TODO: write into the text the word
            }

        }
    }

    public  String[] checkForPossibleMisspellingWords(String correctWord){
        String[] possibleMisspellingWords = null;
        int numberPossibilities = 0;
        for(int i = 0; i < Words.size(); ++i){
            //found the word (correct words have $ at the beginning of it)
            if((Words.get(i)).equals("$"+line)){
                ++i;
                //get the list of possible misspelling options
                while(!(Words.get(i)).contains("$")){//while there is still possible misspelling words
                    possibleMisspellingWords[numberPossibilities] = (Words.get(i));
                    numberPossibilities++;
                    ++i;
                }
                //Found the word no need to keep looking
                break;
            }
        }
        return possibleMisspellingWords;
    }


}
