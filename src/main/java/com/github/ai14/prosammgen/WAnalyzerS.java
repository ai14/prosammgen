package com.github.ai14.prosammgen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import com.google.common.collect.ImmutableList;
import com.github.ai14.prosammgen.*;



public class WAnalyzerS{

    private Path text;
    private String[] TotalWords;
    private String[] TotalSentences;
    private String[] TotalParagraphs;
 
  /**
   * Given a text this calculates the probabilities for different metrics that describes the writing style of the text author.
   *
   * @param text
   */
  public void analyze(Path text){
    try {
        this.text = text;
        for (String line : Files.readAllLines(text)) {
            this.TotalWords = line.split("\\s+"); //splitting into words
            this.TotalSentences = line.split("(?i)(?<=[.?!])\\S+(?=[a-z])");//splitting into sentences
            this.TotalParagraphs = line.split("(?m)(?=^\\s{4})");//splitting into Paragraphs
        }
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
	    int NumberOfSentences = TotalSentences.length;
		List<Integer>sentenceSize = new ArrayList<>();
        for (int i = 0; i < NumberOfSentences; i++) {
            //split sentences into words
            String[] words = TotalSentences[i].split("\\s+");
            int sentenceSameLength = sentenceSize.get(words.length);
            sentenceSameLength++;
            sentenceSize.set(words.length, sentenceSameLength);
        }
        double[] probabilities = new double [sentenceSize.size()];
        //Calculating probabilities
	    for(int j = 0; j < probabilities.length; j++){
            probabilities[j] = sentenceSize.get(j)/NumberOfSentences;
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
	    List<Integer>wordSize = new ArrayList<>();
        textSize = TotalWords.length;
        for (int i = 0; i < textSize; i++) {
          //increasing the number of characters with words[i].length
          int WordsSameLength = wordSize.get(TotalWords[i].length());
          WordsSameLength++;
          wordSize.set(TotalWords[i].length(), WordsSameLength);
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

        int NumberOfParagraph = TotalParagraphs.length;
        List<Integer>SentencesParagraph = new ArrayList<>();
        //each paragraph
        for (int i = 0; i < NumberOfParagraph; i++) {
            //Split into sentences every paragraph
            String[] SentencesOnParagraph = TotalParagraphs[i].split("(?i)(?<=[.?!])\\S+(?=[a-z])");
            //increasing the times that a paragraph have "countSentences" sentences
            int sentencesParagraph = SentencesParagraph.get(TotalParagraphs.length);
            sentencesParagraph++;
            SentencesParagraph.set(TotalParagraphs.length, sentencesParagraph);
        }

        double[] probabilities = new double [SentencesParagraph.size()];
        for(int j = 0; j < probabilities.length; j++){
            probabilities[j] = SentencesParagraph.get(j)/NumberOfParagraph;
        }
        return probabilities;

    }

  /**
   * Get the probabilities for ratios (answer length / question length).
   *
   * @return
   */

  public double[] getQuestionLengthToAnswerLengthRatioProbabilities(){
       //TODO: Implement function
	  double [] i= new double[2];
    return i;
  }
  
   /**
   * Get the probabilities of mispelling words per paragraph
   *
   * @return
   */

  public double getMisspellingWordsProbabilities(){
        int NumberOfParagraphs = TotalParagraphs.length;
        int NumberOfWords = TotalWords.length;
        WordNet CheckSynonym = new WordNet();
	    int misspellingWords = 0;
	    List<Integer>MisspellWords = new ArrayList<>();
        ImmutableList<String>WordToCheck = null;
        ImmutableList<String>Synonyms;
        //each word
        for (int j = 0; j < NumberOfWords; j++) {
            //If it's end of sentence, remove the last character to check the synonyms.
            if ((TotalWords[j].endsWith(".") || TotalWords[j].endsWith("?") || TotalWords[j].endsWith("!")) && TotalWords[j].length() > 0 && TotalWords[j] != null) {
                TotalWords[j] = TotalWords[j].substring(0, TotalWords[j].length() - 1);
            }
            WordToCheck.add(0, TotalWords[j]);
            //get synonyms
            Synonyms = CheckSynonym.getSynonyms(WordToCheck); 
            //if there is no synonym (only get the same word as output of the function means that the word is misspelled
            if (Synonyms.size() == 1 && (Synonyms.get(0)) == TotalWords[j]) ++misspellingWords;
            //check one word at a time, after checking we remove it from the list
            WordToCheck.remove(0);
        }
	    //calculate probability
      double probability = misspellingWords/NumberOfParagraphs;
      return probability;
  }
    
}
