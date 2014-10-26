package com.github.ai14.prosammgen;

import com.google.common.collect.Lists;
import com.google.common.io.Resources;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.nio.charset.StandardCharsets;


public class WAnalyzerS{

    private Path text;
    private String[] totalWords;
    private String[] totalSentences;
    private String[] totalParagraphs;
    private List<String> words;
 
  /**
   * Given a text this calculates the probabilities for different metrics that describes the writing style of the text author.
   *
   * @param text
   */
  public void analyze(Path text){
    try {
        this.words = Resources.readLines(Resources.getResource(App.class, "words"), StandardCharsets.UTF_8);
        this.text = text;
        String allText = new String(Files.readAllBytes(text));
        this.totalWords = allText.split("\\s+"); //splitting into words
        this.totalSentences = allText.split("(?i)(?<=[.?!])\\S+(?=[a-z])");//splitting into sentences
        this.totalParagraphs = allText.split("\\n\\n");//splitting into Paragraphs

    }catch (IOException e) {
        System.err.println("Couldn't read the text");
      }
  }

  /**
   * Get the probabilities for sentences of length [0..longest sentence in the text].
   *
   * @return
   */

  public double[] getSentenceLengthProbabilities() {
	    int numberOfSentences = totalSentences.length;
		List<Integer>sentenceSize = Lists.newArrayList();
        for (int i = 0; i < numberOfSentences; i++) {
            //split sentences into words
            String[] words = totalSentences[i].split("\\s+");
            int sentenceSameLength = sentenceSize.get(words.length);
            sentenceSameLength++;
            sentenceSize.set(words.length, sentenceSameLength);
        }
        double[] probabilities = new double [sentenceSize.size()];
        //Calculating probabilities
	    for(int j = 0; j < probabilities.length; j++){
            probabilities[j] = sentenceSize.get(j)/numberOfSentences;
	    }
        return probabilities;
  }
  /**
   * Get the probabilities for words of length [0..longest word in the text].
   *
   * @return
   */

  public double[] getWordLengthProbabilities(){
	    int textSize = 0;
	    List<Integer>wordSize = Lists.newArrayList();
        textSize = totalWords.length;
        for (int i = 0; i < textSize; i++) {
          //increasing the number of characters with words[i].length
          int wordsSameLength = wordSize.get(totalWords[i].length());
          wordsSameLength++;
          wordSize.set(totalWords[i].length(), wordsSameLength);
        }
        //Calculating probabilities
        double[] probabilities = new double[wordSize.size()];
	    for(int j = 0; j < probabilities.length ;j++){
            probabilities[j] = wordSize.get(j)/textSize;
	    }
        return probabilities;
  }

  /**
   * Get the probabilities for number of sentences per paragraph from [0..most number of sentences in a paragraph in the text].
   *
   * @return
   */

  public double[] getSentencesPerParagraphProbabilities(){

        int numberOfParagraph = totalParagraphs.length;
        List<Integer>sentencesParagraph = Lists.newArrayList();
        //each paragraph
        for (int i = 0; i < numberOfParagraph; i++) {
            //Split into sentences every paragraph
            String[] sentencesOnParagraph = totalParagraphs[i].split("(?i)(?<=[.?!])\\S+(?=[a-z])");
            //increasing the times that a paragraph have "countSentences" sentences
            int numberSentencesPerParagraphs = sentencesParagraph.get(totalParagraphs.length);
            numberSentencesPerParagraphs++;
            sentencesParagraph.set(totalParagraphs.length, numberSentencesPerParagraphs);
        }

        double[] probabilities = new double [sentencesParagraph.size()];
        for(int j = 0; j < probabilities.length; j++){
            probabilities[j] = sentencesParagraph.get(j)/numberOfParagraph;
        }
        return probabilities;

    }


   /**
   * Get the probabilities of mispelling words per text
   *
   * @return
   */

  public double getMisspellingWordsProbabilities(){
        int numberOfWords = totalWords.length;
	int misspellingWords = 0;
        //for each word
        for (int j = 0; j < numberOfWords; j++) {
            //If it's end of sentence, remove the last character to check the synonyms.
            if ((totalWords[j].endsWith(".") || totalWords[j].endsWith("?") || totalWords[j].endsWith("!")|| totalWords[j].endsWith(",")|| totalWords[j].endsWith(":") || totalWords[j].endsWith(")")|| totalWords[j].endsWith("”")|| totalWords[j].endsWith(" \" ")) && totalWords[j].length() > 0 && totalWords[j] != null) {
                totalWords[j] = totalWords[j].substring(0, totalWords[j].length() - 1);
            }
            if ((totalWords[j].startsWith("(")|| totalWords[j].startsWith("“")|| totalWords[j].startsWith("\""))  && totalWords[j].length() > 0 && totalWords[j] != null) {
                totalWords[j] = totalWords[j].substring(1);
            }
            //Diferent characters ’ and ' that are used for the same
            if(totalWords[j].contains("’")) totalWords[j] = totalWords[j].replace("’","'");
            boolean correct = false;
            //Search for the correct word
            for(int i = 0; i < words.size(); ++i){
                //The checking part distinguish between capital letters, switching to LowerCase letters except "I"
                if(totalWords[j].length() > 1 || (totalWords[j].length() == 1 && !totalWords[j].equals("I"))) totalWords[j] = totalWords[j].toLowerCase();
                //found the word (correct words have $ at the beginning of it) or is a number
                if((words.get(i)).equals("$"+ totalWords[j]) || totalWords[j].matches("\\d+") ){
                    //Found the word no need to keep looking
                    correct = true;
                    break;
                }
            }
            //if we don't find the word, means is misspelled
            if (!correct){
                ++misspellingWords;

            }
        }
	    //calculate probability
      double probability = (double)misspellingWords/ totalWords.length;
      return probability;
  }
    
}
