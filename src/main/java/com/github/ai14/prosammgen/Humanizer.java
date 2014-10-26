package com.github.ai14.prosammgen;

import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.List;
import java.util.Random;

public class Humanizer {
  private List<String> words;
  private List<String> closeCharacters;
  private double misspellingProbability;


  public Humanizer(WordNet wordNet, File example) throws IOException, ParseException {
    //Load words (both correct and misspelled ones)
    this.words = Resources.readLines(Resources.getResource(App.class, "words"), Charset.forName("UTF-8"));
    this.closeCharacters = Resources.readLines(Resources.getResource(App.class, "keyboardclosecharacters"), Charset.forName("UTF-8"));
    WAnalyzerS analyze = new WAnalyzerS(wordNet);
    analyze.analyze(example);
    this.misspellingProbability = analyze.getMisspellingWordsProbabilities(); //get misspelling probability from "previousreflectiondocument"
  }


  /**
   * Humanize a text by misspelling some of the words
   *
   * @param text Text to humanize
   * @return
   */
  public String textHumanizer(String text) throws IOException, ParseException {
    String[] allParagraph = text.split("[\\n\\n]+");//Get each paragraph of the text to different strings
    StringBuilder humanizedText = new StringBuilder();
    int jAnswer;
    for (String paragraph : allParagraph) {
      String[] wordsParagraph = paragraph.split("\\s+");//Split one paragraph to words
      int numberOfWords = wordsParagraph.length;
      for (int j = 0; j < numberOfWords; ++j) {
        jAnswer = j;
        //Check if contains some Latex command (Questions are inside of a Latex command)
        if (isLatexCommand(wordsParagraph[j])) {
          jAnswer = goToAnswerPart(paragraph, j);
        }
        //Add non question part
        while (j < jAnswer) {
          humanizedText.append(wordsParagraph[j] + " ");
          j++;
        }
        if (j >= numberOfWords) break;
        String newWord = wordsParagraph[j];
        int misspellingRate = (int) (misspellingProbability * numberOfWords); //calculate how often we want to misspell a word
        List<String> possibleMisspellingWords;
        if ((((j % misspellingRate)) == 0)) { //Check if it should misspell a word according to the calculated probability
          possibleMisspellingWords = checkForPossibleMisspellingWords(wordsParagraph[j]);//Check for possible misspelled words.
          if (possibleMisspellingWords.isEmpty()) { //if there is no possible misspelled word (in our file "words")
            possibleMisspellingWords = checkPossibleSwitcherCharacters(wordsParagraph[j]);//Get a list of words with one of the characters switched
          }
          if (!possibleMisspellingWords.isEmpty()) {//check if we could misspell the word
            newWord = possibleMisspellingWords.get(0) + " ";
            if (possibleMisspellingWords.size() > 1) { //if there are several options (of misspelled words)
              double[] similarityRate;
              similarityRate = getSimilaritiesBetweenWords(possibleMisspellingWords, wordsParagraph[j]); //get how similar are to the original word
              newWord = chooseWord(possibleMisspellingWords, similarityRate);//choose one of them

            }
            //The file "words" contains strings with "_" as a "space" in between parts of the word
            if (newWord.contains("_")) {//check if the new word contains that character
              int index = 0;
              for (int h = 0; h < newWord.length(); ++h) {//if it does, add to the text as different words
                //get where the "_" is
                if (newWord.charAt(h) == '_') {
                  index = h;
                  humanizedText.append(newWord.substring(0, index) + " ");//add first part of the new word
                  break;
                }
              }
              newWord = newWord.substring(index + 1, newWord.length()); //get the second part
            }
          }
        }
        humanizedText.append(newWord + " ");//add the new word (or the old one)
      }
      humanizedText.append("\n\n");
    }
    return humanizedText.toString();
  }

  /**
   * Given a word (correctWord) checks if there is any
   * misspelled words in our file "words".
   *
   * @param correctWord
   * @return
   */
  public List<String> checkForPossibleMisspellingWords(String correctWord) {
    String[] punctuationMarks = checkForPunctuationMarks(correctWord);//take away the punctuation marks
    correctWord = punctuationMarks[0];
    List<String> possibleMisspellingWords = Lists.newArrayList();
    for (int i = 0; i < words.size(); ++i) { //Look for a possible misspelled word
      if ((words.get(i)).equals("$" + correctWord)) {//correct words have a "$" on the beginning in our file "words"
        ++i;
        String misspelledWord;
        while (i < words.size() && !(words.get(i)).contains("$")) {//get the list of possible misspelling options for that word
          misspelledWord = words.get(i);
          //Adding punctuation symbols (if the original word had them)
          if (!punctuationMarks[1].equals(" ")) misspelledWord = punctuationMarks[1] + misspelledWord;
          if (!punctuationMarks[2].equals(" ")) misspelledWord = misspelledWord + punctuationMarks[2];
          possibleMisspellingWords.add(misspelledWord);
          ++i;
        }
      }
    }
    return possibleMisspellingWords;
  }


  /**
   * Given a correct word, switch a character to make it a misspelled one
   *
   * @param correctWord word to misspell
   * @return
   */
  private List<String> checkPossibleSwitcherCharacters(String correctWord) {
    String[] punctuationMarks = checkForPunctuationMarks(correctWord);//take away the punctuation marks
    correctWord = punctuationMarks[0];
    Random random = new Random();
    int rand = random.nextInt(correctWord.length());
    while (!Character.isLetter(correctWord.charAt(rand))) rand = random.nextInt(correctWord.length());
    char characterToSwitch = correctWord.charAt(rand);//Choose the character to switch randomly
    boolean isUpperCase = Character.isUpperCase(characterToSwitch);
    if (isUpperCase)
      characterToSwitch = Character.toLowerCase(characterToSwitch);//writing character to lower case (if needed)
    List<String> possibleSwitchingCharacter = Lists.newArrayList();
    for (int i = 0; i < closeCharacters.size(); ++i) {
      if (closeCharacters.get(i).equals("$" + characterToSwitch)) {//Search for the possible characters
        String stringPossibility;
        ++i;
        while (i < closeCharacters.size() && !(closeCharacters.get(i)).contains("$")) {//get the list of possible close characters
          char CloseCharacter = (closeCharacters.get(i)).charAt(0);
          if (isUpperCase)
            CloseCharacter = Character.toUpperCase(CloseCharacter); //if it was capital letter, make it again
          stringPossibility = switchCharacters(correctWord, rand, CloseCharacter);//Switch the character
          //Adding punctuation symbols (if the original word had them)
          if (!punctuationMarks[1].equals(" ")) stringPossibility = punctuationMarks[1] + stringPossibility;
          if (!punctuationMarks[2].equals(" ")) stringPossibility = stringPossibility + punctuationMarks[2];
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
   *
   * @param word
   * @return
   */
  private String[] checkForPunctuationMarks(String word) {
    String[] punctuationMarks = new String[3];
    punctuationMarks[0] = word;
    punctuationMarks[1] = " "; //String[1] -> characters from the beginning
    punctuationMarks[2] = " "; //String[2] -> characters from the end
    while ((punctuationMarks[0].startsWith(" ( ")) ||//while there is punctuation marks at the beginning take them of the word
            punctuationMarks[0].startsWith(" “ ") ||
            punctuationMarks[0].startsWith(" \" ")
                    && punctuationMarks[0].length() > 0 && punctuationMarks[0] != null) {

      if (punctuationMarks[1].equals(" "))
        punctuationMarks[1] = punctuationMarks[0].substring(0);//if it's the first found, delete the space
      else punctuationMarks[1] = punctuationMarks[1] + punctuationMarks[0].substring(0);
      punctuationMarks[0] = punctuationMarks[0].substring(1);
    }
    while ((punctuationMarks[0].endsWith(" . ")) || //while there is punctuation marks at the end take them of the word
            punctuationMarks[0].endsWith("?") ||
            punctuationMarks[0].endsWith("!") ||
            punctuationMarks[0].endsWith(",") ||
            punctuationMarks[0].endsWith(":") ||
            punctuationMarks[0].endsWith(")") ||
            punctuationMarks[0].endsWith("”") ||
            punctuationMarks[0].endsWith(" \" ")
                    && punctuationMarks[0].length() > 0 && punctuationMarks[0] != null) {

      if (punctuationMarks[2].equals(" "))
        punctuationMarks[2] = punctuationMarks[0].substring(punctuationMarks[0].length() - 1);//if it's the first found, delete the space
      else punctuationMarks[2] = punctuationMarks[2] + punctuationMarks[0].substring(punctuationMarks[0].length() - 1);
      punctuationMarks[0] = punctuationMarks[0].substring(0, punctuationMarks[0].length() - 1);
    }
    //" ’ " and " ' " are different characters but used for the same
    //just a precaution to not skip the word
    if (punctuationMarks[0].contains("’")) punctuationMarks[0] = punctuationMarks[0].replace("’", "'");
    return punctuationMarks;
  }

  /**
   * Get how many open brackets are in a word
   *
   * @param word
   * @return
   */
  private int HowManyBrackets(String word) {
    int brackets = 0;
    for (int i = 0; i < word.length(); ++i) {
      if (word.charAt(i) == '{') ++brackets;
      if (word.charAt(i) == '}') --brackets;
    }
    return brackets;
  }

  /**
   * Checks if the word is for the Latex generation of the text
   *
   * @param word
   * @return
   */
  private boolean isLatexCommand(String word) {
    if (word.contains("\\documentclass")) return true;
    else if (word.contains("\\begin{document}")) return true;
    else if (word.contains("\\title")) return true;
    else if (word.contains("\\author")) return true;
    else if (word.contains("\\maketitle")) return true;
    else if (word.contains("\\section")) return true;
    else if (word.contains("\\end{document}")) return true;
    else if (word.contains("\\emph")) return true;
    return false;
  }

  /**
   * Writes the given character into the word (at the index position)
   *
   * @param word              to switch
   * @param index             of the switching character
   * @param characterToSwitch character to switch
   * @return
   */
  private String switchCharacters(String word, int index, char characterToSwitch) {
    return word.substring(0, index) + characterToSwitch + word.substring(index + 1, word.length());
  }


  /**
   * Given a list of words and a % of similarity to an other word, choose one
   *
   * @param wordsToCompare list of words to choose
   * @param similarityRate similarity %
   * @return
   */
  private String chooseWord(List<String> wordsToCompare, double[] similarityRate) {
    double max = -1;
    String chosenOne = wordsToCompare.get(0);
    for (int i = 0; i < wordsToCompare.size(); ++i) {
      if (similarityRate[i] > max) {//take the most similar one.
        max = similarityRate[i];
        chosenOne = wordsToCompare.get(i);
      }
    }
    return chosenOne;
  }


  /**
   * Calculates similarities (from 0 to 1) between a word and a list of other words.
   *
   * @param wordsToCompare
   * @param originalWord
   * @return
   */
  private double[] getSimilaritiesBetweenWords(List<String> wordsToCompare, String originalWord) {
    double[] ratingMisspelled = new double[wordsToCompare.size()];
    for (int i = 0; i < wordsToCompare.size(); ++i) { //Get probabilities on how close are the WordsToCompare to the originalWord (from 0 to 1)
      //Jaro Winkler Distance algorithm
      ratingMisspelled[i] = 0;//TODO StringUtils.getJaroWinklerDistance(wordsToCompare.get(i), originalWord);
    }
    return ratingMisspelled;
  }

  /**
   * Search in a paragraph when is part of an answer and returns the index
   *
   * @param paragraph
   * @param index
   * @return
   */
  private int goToAnswerPart(String paragraph, int index) {
    String[] wordsParagraph = paragraph.split("\\s+");//Split one paragraph to words
    //Brackets are part of Latex Headers and questions
    int brackets = HowManyBrackets(wordsParagraph[index]);
    ++index;
    while (brackets > 0 && index < wordsParagraph.length) {
      //check until you find an answer
      brackets += HowManyBrackets(wordsParagraph[index]);
      ++index;
    }
    return index;
  }
}
