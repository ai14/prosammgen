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
    private double misspellingProbability;


    public Humanizer()throws IOException, ParseException {
        //Load words (both correct and misspelled ones)
        this.Words = Files.readAllLines(Paths.get("res/words"));
        WAnalyzerS Analyze = new WAnalyzerS();
        Analyze.analyze(Paths.get("example/previousreflectiondocument.in"));
        this.misspellingProbability = Analyze.getMisspellingWordsProbabilities();
    }

    /**
     * * This "humanize" the text by making some misspelled words given a text, a misspellingProb (as higher the probability
     * less similarity between words).
     * @param text Text to humanize
     * @return
     */
    public String TextHumanizer(String text)throws IOException, ParseException {
        //TODO: Change frequency of misspelling a word
        int numberOfParagraphs = 0;

        String[] paragraph = text.split("\"(?m)(?=^\\\\s{4})\"");

        numberOfParagraphs = paragraph.length;
        StringBuilder HumanizedText = new StringBuilder();
        //each paragraph
        for (int i = 0; i < numberOfParagraphs;++i){
            int numberOfWords = 0;
            String[] wordsPerParagraph = text.split("\\s+");
            numberOfWords = wordsPerParagraph.length;
            //each word
            for (int j =0; j < numberOfWords; ++j) {
                int misspellingRate = (int) (misspellingProbability*numberOfWords);
                boolean QuestionmarksEndOfSentence = false;
                boolean PointmarksEndOfSentence = false;
                boolean ExclamationmarksEndOfSentence = false;
                //keep the same word (just in case we don't switch it to a misspelled one)

                //Delete ".", "?" or "!" for the misspelling search
                if (wordsPerParagraph[j].endsWith(".") && wordsPerParagraph[j].length() > 0 && wordsPerParagraph[j] != null) {
                    wordsPerParagraph[j] = wordsPerParagraph[j].substring(0, wordsPerParagraph[j].length() - 1);
                    PointmarksEndOfSentence = true;
                }
                if (wordsPerParagraph[j].endsWith("?") && wordsPerParagraph[j].length() > 0 && wordsPerParagraph[j] != null) {
                    wordsPerParagraph[j] = wordsPerParagraph[j].substring(0, wordsPerParagraph[j].length() - 1);
                    QuestionmarksEndOfSentence = true;
                }
                if (wordsPerParagraph[j].endsWith("!") && wordsPerParagraph[j].length() > 0 && wordsPerParagraph[j] != null) {
                    wordsPerParagraph[j] = wordsPerParagraph[j].substring(0, wordsPerParagraph[j].length() - 1);
                    ExclamationmarksEndOfSentence = true;
                }
                String misspelledWord = wordsPerParagraph[j];

                String[] possibleMisspellingWords = null;
                //Find misspelling words according to a given probability
                //TODO: Check the case that doesn't found a misspelled word when it should (probability will change)
                if(j%misspellingRate == 0)possibleMisspellingWords = checkForPossibleMisspellingWords(wordsPerParagraph[j]);
                if (possibleMisspellingWords != null) {
                    misspelledWord = possibleMisspellingWords[0] + " ";
                    //if several options
                    if (possibleMisspellingWords.length > 1) {
                        double[] SimilarityRate = new double[possibleMisspellingWords.length];
                        SimilarityRate = getSimilaritudesBetweenWords(possibleMisspellingWords, wordsPerParagraph[j]);
                        misspelledWord = chooseWord(possibleMisspellingWords, SimilarityRate);
                    }
                }

                //If it had ".", "?" or "!" marks add them again
                if (QuestionmarksEndOfSentence) {
                    misspelledWord = (misspelledWord + "?");
                } else if (PointmarksEndOfSentence) {
                    misspelledWord = (misspelledWord + ".");
                } else if (ExclamationmarksEndOfSentence) {
                    misspelledWord = (misspelledWord + "!");
                }
                HumanizedText.append(misspelledWord);
                HumanizedText.append("\\s+");
            }
            HumanizedText.append("/n/n");
        }
        HumanizedText.append("\\end{document}");

        return HumanizedText.toString();
    }


    /**
     * Take the most suitable misspelling word
     * @param WordsToCompare
     * @param SimilarityRate
     * @return
     */
    private String chooseWord(String[] WordsToCompare, double[] SimilarityRate) {
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
