package com.github.ai14.prosammgen;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Humanizer {
    private List<String> words;
    private List<String> closeCharacters;
    private double misspellingProbability;


    public Humanizer()throws IOException, ParseException {
        //Load words (both correct and misspelled ones)
        this.words = Files.readAllLines(Paths.get("res/words"));
        this.closeCharacters = Files.readAllLines(Paths.get("res/keyboardclosecharacters"));
        WAnalyzerS analyze = new WAnalyzerS();
        analyze.analyze(Paths.get("example/previousreflectiondocument.in"));
        this.misspellingProbability = analyze.getMisspellingWordsProbabilities();
    }

    /**
     * Humanize a text by misspelling some of the words
     * @param text Text to humanize
     * @return
     */
    public String textHumanizer(String text)throws IOException, ParseException {
        //TODO: Change frequency of misspelling a word
        //Get each paragraph of the text to different strings
        String[] allParagraph = text.split("[\\n\\n]+");
//int numberOfParagraphs = allParagraph.length;
        StringBuilder humanizedText = new StringBuilder();
        //for (int i = 0; i < numberOfParagraphs;++i){
        for (String paragraph : allParagraph){
            //Split one paragraph to words
            String[] wordsParagraph = paragraph.split("\\s+");
            int numberOfWords = wordsParagraph.length;
            for (int j =0; j < numberOfWords; ++j) {
                String newWord = wordsParagraph[j];
                //Check if is part of our answer
                if (!isAnswer(wordsParagraph[j])) {
                    //if it is not leave it as it is.
                    int brackets = HowManyBrackets(wordsParagraph[j]);
                    humanizedText.append(newWord + " ");
                    ++j;
                    while (brackets > 0 && j < numberOfWords) {
                        //check until you find an answer
                        brackets += HowManyBrackets(wordsParagraph[j]);
                        newWord = wordsParagraph[j];
                        humanizedText.append(newWord + " ");
                        ++j;
                    }
                }
                else {
                    //if it's an answer
                    newWord = wordsParagraph[j];
                    //calculate how often we want to misspell a word
                    double misspellingRate = (misspellingProbability * numberOfWords);
                    List<String> possibleMisspellingWords = new ArrayList<>();
                    //Find misspelling words according to a given probability and
                    //check if contains part for the latex generation(if it does, skip the word)
                    //TODO: if it contains parts for the latex generation check the next word
                    //TODO: don't make the mistakes to close
                   if ((((int) (misspellingRate % j)) == 0) &&
                      (!wordsParagraph[j].startsWith("\\")) &&
                      (!wordsParagraph[j].contains("{")) &&
                      (!wordsParagraph[j].contains("}"))) {
                        //Check for possible misspelled words.
                        possibleMisspellingWords = checkForPossibleMisspellingWords(wordsParagraph[j]);
                        //if there is no possible misspelled word (in our file "words")
                        if (possibleMisspellingWords.isEmpty()) {
                            //TODO: remove printer
                            System.err.println("SWITCH");
                            //Get a list of words with one of the characters
                            //(chosen randomly) switched by a close character of the (english QWERTY) keyboard.
                            possibleMisspellingWords = checkPossibleSwitcherCharacters(wordsParagraph[j]);
                        }
                        //TODO: remove printer
                        else System.err.println("WOOOORD");
                    }
                    //check if we could misspelled the word
                    if (!possibleMisspellingWords.isEmpty()) {
                        newWord = possibleMisspellingWords.get(0) + " ";
                        //if there are several misspelled options
                        if (possibleMisspellingWords.size() > 1) {
                            double[] similarityRate;
                            //get values (from 0 to 1) on how similar are to the original word
                            //using Jaro Winkler distance algorithm
                            similarityRate = getSimilaritiesBetweenWords(possibleMisspellingWords, wordsParagraph[j]);
                            //chose the most similar word
                            //TODO: change it depending on the probability of misspelling
                            newWord = chooseWord(possibleMisspellingWords, similarityRate);
                        }
                        //The file "words" contains strings with "_" as a "space" in between to parts of the word
                        //check if the new word contains that character
                        if (newWord.contains("_")){
                            //if it does add to the text as to separeted words
                            int index=0;
                            for(int h = 0; h < newWord.length(); ++h){
                                //get where the "_" is
                                if(newWord.charAt(h) == '_'){
                                    index = h;
                                    //TODO: Remove print
                                    System.err.println(newWord.substring(0,index)+" ");
                                    //add first part of the new word
                                    humanizedText.append(newWord.substring(0,index)+" ");
                                    break;
                                }
                            }
                            //get the second part
                            newWord = newWord.substring(index+1, newWord.length());
                        }
                        //TODO: take of the print
                        System.err.println(wordsParagraph[j]+"   "+newWord);
                    }
                    //add the new word (or the old one)
                    humanizedText.append(newWord+" ");
                }
            }
            humanizedText.append("\n\n");
        }
        return humanizedText.toString();
    }

    /**
     * Given a word (correctWord) it checks if there is any
     * misspelled words in our file "words".
     * @param correctWord
     * @return
     */
    public   List<String> checkForPossibleMisspellingWords(String correctWord){
        //take away the punctuation marks
        String[] punctuationMarks = checkForPunctuationMarks(correctWord);
        correctWord = punctuationMarks[0];
        List<String> possibleMisspellingWords = new ArrayList<>();
        for(int i = 0; i < words.size(); ++i){
            //Look for a possible misspelled word
            //correct words have a "$" on the beginning in our file "words"
            if((words.get(i)).equals("$"+correctWord)){
                ++i;
                String misspelledWord;
                while(i < words.size() && !(words.get(i)).contains("$") ){
                    //get the list of possible misspelling options for that word
                    misspelledWord = words.get(i);
                    //Adding punctuation symbols (if the original word had them)
                    if(!punctuationMarks[1].equals(" "))misspelledWord = punctuationMarks[1]+misspelledWord;
                    if(!punctuationMarks[2].equals(" "))misspelledWord = misspelledWord +punctuationMarks[2];
                    possibleMisspellingWords.add(misspelledWord);
                    ++i;
                }
                //TODO: the file "word" contains repeated words with different misspelled options try to gather them
                //Found the word no need to keep looking
                break;
            }
        }
        return possibleMisspellingWords;
    }


     /**
     * Given a correct word, switch a character to make it a misspelled one
     * @param correctWord word to misspell
     * @return
     */
    private List<String> checkPossibleSwitcherCharacters(String correctWord) {
        //take away the punctuation marks
        String[] punctuationMarks = checkForPunctuationMarks(correctWord);
        correctWord = punctuationMarks[0];
        Random random = new Random();
        //Choose the character to switch randomly
        int rand = random.nextInt(correctWord.length());
        char characterToSwitch = correctWord.charAt(rand);
        boolean isUpperCase = Character.isUpperCase(characterToSwitch);
        //writing character to lower case (if needed)
        if(isUpperCase)characterToSwitch = Character.toLowerCase(characterToSwitch);
        List<String> possibleSwitchingCharacter = new ArrayList<>();
        for(int i = 0; i < closeCharacters.size(); ++i){
            //Search for the possible characters
            if(closeCharacters.get(i).equals("$" + characterToSwitch)){
                String stringPossibility;
                ++i;
                while(i < closeCharacters.size() && !(closeCharacters.get(i)).contains("$")){
                    //get the list of possible close characters
                    char CloseCharacter = (closeCharacters.get(i)).charAt(0);
                    //if it was capital letter, make it again
                    if(isUpperCase) CloseCharacter = Character.toUpperCase(CloseCharacter);
                    //Switch the character
                    stringPossibility = switchCharacters(correctWord, rand,CloseCharacter);
                    //Adding punctuation symbols (if the original word had them)
                    if(!punctuationMarks[1].equals(" "))stringPossibility = punctuationMarks[1]+stringPossibility;
                    if(!punctuationMarks[2].equals(" "))stringPossibility = stringPossibility +punctuationMarks[2];
                    possibleSwitchingCharacter.add(stringPossibility);
                    ++i;
                }
                //Found the word no need to keep looking
                break;
            }
        }
        return possibleSwitchingCharacter;
    }

    /**
     * Checks if the word have punctuation symbols and returns them along with the word without it.
     * @param word
     * @return
     */
    private String[] checkForPunctuationMarks(String word) {
        String[] punctuationMarks = new String[3];
        //String[0] -> the word
        //String[1] -> characters from the beginning
        //String[2] -> characters from the end
        punctuationMarks[0] = word;
        punctuationMarks[1] = " ";
        punctuationMarks[2] = " ";
        //while there is punctuation marks at the beginning take them of the word
        while((punctuationMarks[0].startsWith(" ( "))||
                punctuationMarks[0].startsWith(" “ ")||
                punctuationMarks[0].startsWith(" \" ")
                && punctuationMarks[0].length() > 0 && punctuationMarks[0] != null) {
                    //if it's the first time, delete the space
                    if(punctuationMarks[1].equals(" "))punctuationMarks[1] = punctuationMarks[0].substring(0);
                    else punctuationMarks[1] = punctuationMarks[1] + punctuationMarks[0].substring(0);
                    punctuationMarks[0] = punctuationMarks[0].substring(1);
        }
        //while there is punctuation marks at the end take them of the word
        while ((punctuationMarks[0].endsWith(" . "))||
                punctuationMarks[0].endsWith("?") ||
                punctuationMarks[0].endsWith("!")||
                punctuationMarks[0].endsWith(",")||
                punctuationMarks[0].endsWith(":") ||
                punctuationMarks[0].endsWith(")")||
                punctuationMarks[0].endsWith("”")||
                punctuationMarks[0].endsWith(" \" ")
                &&punctuationMarks[0].length() > 0 && punctuationMarks[0] != null) {
                    //if it's the first time, delete the space
                    if(punctuationMarks[2].equals(" "))punctuationMarks[2] =punctuationMarks[0].substring(punctuationMarks[0].length() - 1);
                    else punctuationMarks[2] = punctuationMarks[2] + punctuationMarks[0].substring(punctuationMarks[0].length() - 1);
                    punctuationMarks[0] = punctuationMarks[0].substring(0, punctuationMarks[0].length() - 1);
        }
        //Different characters ’ and ' that are used for the same
        //just a precaution to not skip the word
        if(punctuationMarks[0].contains("’")) punctuationMarks[0] = punctuationMarks[0].replace("’", "'");
        return punctuationMarks;
    }

    /**
     * Get how many open brackets are in a word
     * @param word
     * @return
     */
    private int HowManyBrackets(String word) {
        int brackets = 0;
        for(int i = 0; i < word.length(); ++i){
            if(word.charAt(i) == '{') ++brackets;
            if(word.charAt(i) == '}') --brackets;
        }
        return brackets;
    }

    /**
     * Check is the word is part of one of our answers
     * @param word to check
     * @return
     */
    private boolean isAnswer(String word) {
        if(word.contains("\\documentclass"))return false;
        else if(word.contains("\\begin{"))return false;
        else if(word.contains("\\title"))return false;
        else if(word.contains("\\author"))return false;
        else if(word.contains("\\maketitle"))return false;
        else if(word.contains("\\section")) return false;
        else if(word.contains("\\end{document}")) return false;
        return true;
    }


    /**
     * Writes the given character into the word (at the index position)
     * @param word to switch
     * @param index of the switching character
     * @param characterToSwitch character to switch
     * @return
     */
    private String switchCharacters(String word, int index, char characterToSwitch) {
        return word.substring(0, index)+characterToSwitch+word.substring(index+1, word.length());
    }


    /**
     * Given a list of words and a % of similarity to an other word, choose one
     * @param wordsToCompare list of words to choose
     * @param similarityRate similarity %
     * @return
     */
    private String chooseWord(List<String> wordsToCompare, double[] similarityRate) {
        //TODO: implement algorithm to choose depending the probability
        double max = -1;
        String chosenOne = wordsToCompare.get(0);
        //So far take the most similar one.
        for(int i = 0; i < wordsToCompare.size(); ++i){
            if(similarityRate[i] > max){
                max = similarityRate[i];
                chosenOne = wordsToCompare.get(i);
            }
        }
        return chosenOne;
    }


    /**
     * Calculates similarities (from 0 to 1) between a word and a list of other words.
     * @param wordsToCompare
     * @param originalWord
     * @return
     */
    private double[] getSimilaritiesBetweenWords(List<String> wordsToCompare, String originalWord) {
        double []ratingMisspelled = new double [wordsToCompare.size()];
        //Get probabilities on how close are the WordToCompare to the originalWord
        for(int i = 0; i < wordsToCompare.size(); ++i){
            //Jaro Winkler Distance algorithm
            ratingMisspelled[i] = StringUtils.getJaroWinklerDistance(wordsToCompare.get(i), originalWord);
            //Levenshtein Distance algorithm
            //ratingMisspelled[i] = StringUtils.getLevenshteinDistance(wordsToCompare.get(i), originalWord);
        }
        return ratingMisspelled;
    }

}
