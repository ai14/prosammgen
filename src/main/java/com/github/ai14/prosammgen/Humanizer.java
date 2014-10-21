package com.github.ai14.prosammgen;

import com.github.ai14.prosammgen.textgen.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import org.apache.commons.lang3.StringUtils;

import javax.print.DocFlavor;
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
        String[] paragraph = text.split("[\\n\\n]+");
        numberOfParagraphs = paragraph.length;
        System.err.println(numberOfParagraphs);
        StringBuilder HumanizedText = new StringBuilder();
        //each paragraph
        for (int i = 0; i < numberOfParagraphs;++i){
            int numberOfWords = 0;
            String[] WordsParagraph = paragraph[i].split("\\s+");
            numberOfWords = WordsParagraph.length;
            //each word
            for (int j =0; j < numberOfWords; ++j) {
                double misspellingRate = (misspellingProbability*numberOfWords);
                String newWord = WordsParagraph[j];
                List<String> possibleMisspellingWords = new ArrayList<>();
                //Find misspelling words according to a given probability
                //TODO: Improve when to look for a word
                if((int)(misspellingRate%j) == 0){
                    possibleMisspellingWords = checkForPossibleMisspellingWords(WordsParagraph[j]);
                }
                if (!possibleMisspellingWords.isEmpty()) {
                    newWord = possibleMisspellingWords.get(0) + " ";
                    //if several options
                    if (possibleMisspellingWords.size() > 1) {
                        double[] SimilarityRate = new double[possibleMisspellingWords.size()];
                        SimilarityRate = getSimilaritudesBetweenWords(possibleMisspellingWords, WordsParagraph[j]);
                        newWord = chooseWord(possibleMisspellingWords, SimilarityRate);
                    }
                    System.err.println(WordsParagraph[j]+"   "+newWord);
                }
                HumanizedText.append(newWord+" ");
            }
            HumanizedText.append("\n\n");
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
    private String chooseWord(List<String> WordsToCompare, double[] SimilarityRate) {
        //TODO: implement algorithm to choose depending the probability
        double max = -1;
        String ChoosenOne = WordsToCompare.get(0);
        //So far take the most similar one.
        for(int i = 0; i < WordsToCompare.size(); ++i){
            if(SimilarityRate[i] > max){
                max = SimilarityRate[i];
                ChoosenOne = WordsToCompare.get(i);
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
    private double[] getSimilaritudesBetweenWords(List<String> WordsToCompare, String OriginalWord) {
        double []ratingMisspeled = new double [WordsToCompare.size()];
        //Check
        for(int i = 0; i < WordsToCompare.size(); ++i){
            ratingMisspeled[i] = StringUtils.getJaroWinklerDistance(WordsToCompare.get(i), OriginalWord);
        }
        return ratingMisspeled;
    }



    public   List<String> checkForPossibleMisspellingWords(String correctWord){
        boolean ChapitalLetter = false;
        char cEnd = ' ';
        char cStart = ' ';
        if ((correctWord.endsWith(".") || correctWord.endsWith("?") || correctWord.endsWith("!")|| correctWord.endsWith(",")|| correctWord.endsWith(":") || correctWord.endsWith(")")|| correctWord.endsWith("”")||correctWord.endsWith(" \" ")) &&correctWord.length() > 0 && correctWord != null) {
            cEnd = correctWord.charAt(correctWord.length() - 1);
            correctWord = correctWord.substring(0, correctWord.length() - 1);
        }
        if ((correctWord.startsWith("(")|correctWord.startsWith("“")||correctWord.startsWith("\""))  && correctWord.length() > 0 && correctWord != null) {
            cStart = correctWord.charAt(0);
            correctWord = correctWord.substring(1);
        }
        //Diferent characters ’ and ' that are used for the same
        if(correctWord.contains("’")) correctWord = correctWord.replace("’","'");
        List<String> possibleMisspellingWords = new ArrayList<>();
        for(int i = 0; i < Words.size(); ++i){
            //found the word (correct words have $ at the beginning of it)
            if((Words.get(i)).equals("$"+correctWord)){
                ++i;
                //get the list of possible misspelling options
                String misspelledWord;
                while(!(Words.get(i)).contains("$") && i < Words.size()){//while there is still possible misspelling words
                    misspelledWord = Words.get(i);
                    if(cEnd != ' ')misspelledWord = misspelledWord +cEnd;
                    if(cStart != ' ')misspelledWord = cStart + misspelledWord;
                    possibleMisspellingWords.add(misspelledWord);
                    ++i;
                }
                //Found the word no need to keep looking
                break;
            }
        }
        return possibleMisspellingWords;
    }


}
