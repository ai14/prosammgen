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
        int numberOfParagraphs = 0;
        String[] paragraph = text.split("[\\n\\n]+");
        numberOfParagraphs = paragraph.length;
        StringBuilder humanizedText = new StringBuilder();
        for (int i = 0; i < numberOfParagraphs;++i){
            String[] wordsParagraph = paragraph[i].split("\\s+");
            int numberOfWords = wordsParagraph.length;
            for (int j =0; j < numberOfWords; ++j) {
                String newWord = wordsParagraph[j];
                //Check if is one of our answer or not
                if (!isAnswer(wordsParagraph[j])) {
                    int brackets = HowManyBrackets(wordsParagraph[j]);
                    humanizedText.append(newWord + " ");
                    ++j;
                    while (brackets > 0 && j < numberOfWords) {

                        brackets += HowManyBrackets(wordsParagraph[j]);
                        newWord = wordsParagraph[j];
                        humanizedText.append(newWord + " ");
                        ++j;
                    }
                }
                else {
                    newWord = wordsParagraph[j];
                    double misspellingRate = (misspellingProbability * numberOfWords);
                    List<String> possibleMisspellingWords = new ArrayList<>();
                    //Find misspelling words according to a given probability and
                    //check if contains characters for latex
                    if ((((int) (misspellingRate % j)) == 0) && (!wordsParagraph[j].startsWith("\\")) && (!wordsParagraph[j].contains("{")) && (!wordsParagraph[j].contains("}"))) {
                        possibleMisspellingWords = checkForPossibleMisspellingWords(wordsParagraph[j]);
                        if (possibleMisspellingWords.isEmpty()) {
                            //TODO: remove printer
                            System.err.println("SWITCH");
                            possibleMisspellingWords = checkPossibleSwitcherCharacters(wordsParagraph[j]);
                        }
                        //TODO: remove printer
                        else System.err.println("WOOOORD");
                    }
                    if (!possibleMisspellingWords.isEmpty()) {
                        newWord = possibleMisspellingWords.get(0) + " ";
                        //if several options
                        if (possibleMisspellingWords.size() > 1) {
                            double[] similarityRate;
                            similarityRate = getSimilaritiesBetweenWords(possibleMisspellingWords, wordsParagraph[j]);
                            newWord = chooseWord(possibleMisspellingWords, similarityRate);
                        }
                        //The file "words" contains strings with "_" as a space in between to parts of the word
                        if (newWord.contains("_")){
                            int index=0;
                            for(int h = 0; h < newWord.length(); ++h){
                                if(newWord.charAt(h) == '_'){
                                    index = h;
                                    //TODO: Remove print
                                    System.err.println(newWord.substring(0,index)+" ");
                                    humanizedText.append(newWord.substring(0,index)+" ");
                                    break;
                                }
                            }
                            newWord = newWord.substring(index+1, newWord.length());
                        }
                        //TODO: take of the print
                        System.err.println(wordsParagraph[j]+"   "+newWord);
                    }
                    humanizedText.append(newWord+" ");
                }
            }
            humanizedText.append("\n\n");
        }
        return humanizedText.toString();
    }

    private int HowManyBrackets(String word) {
        int brackets = 0;
        for(int i = 0; i < word.length(); ++i){
            if(word.charAt(i) == '{') ++brackets;
            if(word.charAt(i) == '}') --brackets;
        }
        return brackets;
    }

    /**
     * Check is the question is part of one of our answers
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
     * Given a correct word switches the character to make it a misspelled one
     * @param correctWord word to misspell
     * @return
     */
    private List<String> checkPossibleSwitcherCharacters(String correctWord) {
        //Delete punctuation symbols
        String[] punctuationMarks;
        punctuationMarks = checkForPunctuationMarks(correctWord);
        correctWord = punctuationMarks[0];
        Random random = new Random();
        //Choose character randomly
        int rand = random.nextInt(correctWord.length());
        char characterToSwitch = correctWord.charAt(rand);
        boolean isUpperCase = Character.isUpperCase(characterToSwitch);
        //writing character to lower case
        if(isUpperCase)characterToSwitch = Character.toLowerCase(characterToSwitch);

        List<String> possibleSwitchingCharacter = new ArrayList<>();
        for(int i = 0; i < closeCharacters.size(); ++i){
            //Search for the character
            if(closeCharacters.get(i).equals("$" + characterToSwitch)){
                String stringPossibility;
                ++i;
                while(i < closeCharacters.size() && !(closeCharacters.get(i)).contains("$")){//while there is still possible misspelling words
                    char CloseCharacter = (closeCharacters.get(i)).charAt(0);
                    if(isUpperCase){
                        CloseCharacter = Character.toUpperCase(CloseCharacter);
                    }
                    //Switch the character
                    stringPossibility = switchCharacters(correctWord, rand,CloseCharacter);
                    //Reading punctuation symbols
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
     * Writes the given character into the word on the index
     * @param word to switch
     * @param index of the switching character
     * @param characterToSwitch character to switch
     * @return
     */
    private String switchCharacters(String word, int index, char characterToSwitch) {
        return word.substring(0, index)+characterToSwitch+word.substring(index+1, word.length());
    }


    /**
     * Take the most suitable misspelling word
     * @param wordsToCompare
     * @param similarityRate
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
     * Calculates similarities between two words (the possible misspelling word and the original one)
     * @param wordsToCompare
     * @param originalWord
     * @return
     */
    private double[] getSimilaritiesBetweenWords(List<String> wordsToCompare, String originalWord) {
        double []ratingMisspelled = new double [wordsToCompare.size()];
        //Get probabilities on how close are the WordToCompare to the original word
        for(int i = 0; i < wordsToCompare.size(); ++i){
            ratingMisspelled[i] = StringUtils.getJaroWinklerDistance(wordsToCompare.get(i), originalWord);
            //ratingMisspelled[i] = StringUtils.getLevenshteinDistance(wordsToCompare.get(i), originalWord);
        }
        return ratingMisspelled;
    }



    public   List<String> checkForPossibleMisspellingWords(String correctWord){
        //TODO: check capitals characters
        String[] punctuationMarks = new String[3];
        punctuationMarks = checkForPunctuationMarks(correctWord);
        correctWord = punctuationMarks[0];
        List<String> possibleMisspellingWords = new ArrayList<>();
        for(int i = 0; i < words.size(); ++i){
            //found the word (correct words have $ at the beginning of it)
            if((words.get(i)).equals("$"+correctWord)){
                ++i;
                //get the list of possible misspelling options
                String misspelledWord;
                while(i < words.size() && !(words.get(i)).contains("$") ){//while there is still possible misspelling words

                    misspelledWord = words.get(i);;
                    //Reading punctuation symbols
                    if(!punctuationMarks[1].equals(" "))misspelledWord = punctuationMarks[1]+misspelledWord;
                    if(!punctuationMarks[2].equals(" "))misspelledWord = misspelledWord +punctuationMarks[2];
                    possibleMisspellingWords.add(misspelledWord);
                    ++i;
                }
                //Found the word no need to keep looking
                break;
            }
        }
        return possibleMisspellingWords;
    }

    /**
     * Checks if the word have punctuation symbols and returns them along with the word without it.
     * @param word
     * @return
     */
    private String[] checkForPunctuationMarks(String word) {
        String[] punctuationMarks = new String[3];
        //String[0] -> the word
        //String[1] -> character in the beginning
        //String[2] -> character in the end
        punctuationMarks[0] = word;
        punctuationMarks[1] = " ";
        punctuationMarks[2] = " ";
        while ((punctuationMarks[0].endsWith(".") || punctuationMarks[0].endsWith("?") || punctuationMarks[0].endsWith("!")|| punctuationMarks[0].endsWith(",")|| punctuationMarks[0].endsWith(":") || punctuationMarks[0].endsWith(")")|| punctuationMarks[0].endsWith("”")||punctuationMarks[0].endsWith(" \" ")) &&punctuationMarks[0].length() > 0 && punctuationMarks[0] != null) {
            if(punctuationMarks[2].equals(" "))punctuationMarks[2] =punctuationMarks[0].substring(punctuationMarks[0].length() - 1);
            else punctuationMarks[2] = punctuationMarks[2] + punctuationMarks[0].substring(punctuationMarks[0].length() - 1);
            punctuationMarks[0] = punctuationMarks[0].substring(0, punctuationMarks[0].length() - 1);
        }
        while((punctuationMarks[0].startsWith("(")||punctuationMarks[0].startsWith("“")||punctuationMarks[0].startsWith("\""))  && punctuationMarks[0].length() > 0 && punctuationMarks[0] != null) {
            if(punctuationMarks[1].equals(" "))punctuationMarks[1] = punctuationMarks[0].substring(0);
            else punctuationMarks[1] = punctuationMarks[1] + punctuationMarks[0].substring(0);
            punctuationMarks[0] = punctuationMarks[0].substring(1);
        }
        //Different characters ’ and ' that are used for the same
        if(punctuationMarks[0].contains("’")) punctuationMarks[0] = punctuationMarks[0].replace("’", "'");
        return punctuationMarks;
    }


}
