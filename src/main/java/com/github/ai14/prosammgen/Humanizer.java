package com.github.ai14.prosammgen;

import com.github.ai14.prosammgen.textgen.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import org.apache.commons.lang3.StringUtils;

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

    private List<String> Words;


    public Humanizer()throws IOException, ParseException {
        //Load words (both correct and misspelled ones)
        Words = Files.readAllLines(Paths.get("res/words"));
    }

    /**
     * * This "humanize" the text by making some misspelled words given a text, a misspellingProb (as higher the probability
     * less similarity between words).
     * @param text Text to humanize
     * @param misspellingProb Probability of mistakes
     * @return
     */
    public Path TextHumanizer(Path text, double misspellingProb)throws IOException, ParseException {
        List<String> allText = Files.readAllLines(text);
        int numberOfWords = 0;
        for (String line : allText){
            String[] words = line.split("\\s+");
            boolean QuestionmarksEndOfSentence = false;
            boolean PointmarksEndOfSentence = false;
            boolean ExclamationmarksEndOfSentence = false;
            for(int j = 0; j < words.length; ++j) {


                //Delete ".", "?" or "!" for the misspelling search
                if (words[j].endsWith(".")  && words[j].length() > 0 && words[j] != null) {
                    words[j] = words[j].substring(0, words[j].length() - 1);
                    PointmarksEndOfSentence = true;
                }
                if (words[j].endsWith("?")  && words[j].length() > 0 && words[j] != null) {
                    words[j] = words[j].substring(0, words[j].length() - 1);
                    QuestionmarksEndOfSentence = true;
                }
                if (words[j].endsWith("!") && words[j].length() > 0 && words[j] != null) {
                    words[j] = words[j].substring(0, words[j].length() - 1);
                    ExclamationmarksEndOfSentence = true;
                }


                String[] possibleMisspellingWords = null;
                //Find misspelling words for that word
                possibleMisspellingWords = checkForPossibleMisspellingWords(words[j]);
                if (possibleMisspellingWords != null) {
                    String misspelledWord = possibleMisspellingWords[0];
                    //if several options
                    if (possibleMisspellingWords.length > 1) {
                        double[] SimilarityRate = new double[possibleMisspellingWords.length];
                        SimilarityRate = getSimilaritudesBetweenWords(possibleMisspellingWords, line);
                        misspelledWord = chooseWord(possibleMisspellingWords, SimilarityRate, misspellingProb);
                    }
                    //If it had ".", "?" or "!" marks add them again
                    if(QuestionmarksEndOfSentence){
                        misspelledWord = (misspelledWord + "?");
                    }
                    else if(PointmarksEndOfSentence){
                        misspelledWord = (misspelledWord + ".");
                    }
                    else if(ExclamationmarksEndOfSentence){
                        misspelledWord = (misspelledWord + "!");
                    }
                    //TODO: write into the text the misspelled word
                }
            }

        }
        return text;
    }


    /**
     * Take the most suitable misspelling word
     * @param WordsToCompare
     * @param SimilarityRate
     * @param misspellingProb
     * @return
     */
    private String chooseWord(String[] WordsToCompare, double[] SimilarityRate, double misspellingProb) {
        //TODO: implement algorithm to choose depending the probability
        double max = -1;
        String ChoosenOne = WordsToCompare[0];
        //So far take the most similar one.
        for(int i = 0; i < WordsToCompare.length; ++i){
            if(SimilarityRate[i] > max){
                max = SimilarityRate[i];
                ChoosenOne = WordsToCompare[i];
            }
        }
        return ChoosenOne;
    }


    /**
     * Calculates similarities between two words (the possible misspelling word and the original one)
     * @param WordsToCompare
     * @param OriginalWord
     * @return
     */
    private double[] getSimilaritudesBetweenWords(String[] WordsToCompare, String OriginalWord) {
        double []ratingMisspeled = new double [WordsToCompare.length];
        //Check
        for(int i = 0; i < WordsToCompare.length; ++i){
            ratingMisspeled[i] = StringUtils.getJaroWinklerDistance(WordsToCompare[i], OriginalWord);
        }
        return ratingMisspeled;
    }

    public  String[] checkForPossibleMisspellingWords(String correctWord){
        String[] possibleMisspellingWords = null;
        int numberPossibilities = 0;
        for(int i = 0; i < Words.size(); ++i){
            //found the word (correct words have $ at the beginning of it)
            if((Words.get(i)).equals("$"+correctWord)){
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
