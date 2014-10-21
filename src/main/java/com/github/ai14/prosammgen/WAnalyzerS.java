package com.github.ai14.prosammgen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import com.google.common.collect.ImmutableList;
import com.github.ai14.prosammgen.*;



public class WAnalyzerS{

    private Path text;
    private String[] TotalWords;
    private String[] TotalSentences;
    private String[] TotalParagraphs;
    private List<String> Words;
 
  /**
   * Given a text this calculates the probabilities for different metrics that describes the writing style of the text author.
   *
   * @param text
   */
  public void analyze(Path text){
    try {
        this.Words = Files.readAllLines(Paths.get("res/words"));
        this.text = text;
        String allText = new String(Files.readAllBytes(text));
        this.TotalWords = allText.split("\\s+"); //splitting into words
        this.TotalSentences = allText.split("(?i)(?<=[.?!])\\S+(?=[a-z])");//splitting into sentences
        this.TotalParagraphs = allText.split("\\n\\n");//splitting into Paragraphs

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
   * Get the probabilities of mispelling words per text
   *
   * @return
   */

  public double getMisspellingWordsProbabilities(){
        int NumberOfWords = TotalWords.length;
        WordNet CheckSynonym = new WordNet();
	    int misspellingWords = 0;
	    List<Integer>MisspellWords = new ArrayList<>();;
 //System.err.println(TotalParagraphs.length);
      //each word
        for (int j = 0; j < NumberOfWords; j++) {
            //If it's end of sentence, remove the last character to check the synonyms.
            if ((TotalWords[j].endsWith(".") || TotalWords[j].endsWith("?") || TotalWords[j].endsWith("!")|| TotalWords[j].endsWith(",")|| TotalWords[j].endsWith(":") || TotalWords[j].endsWith(")")|| TotalWords[j].endsWith("”")|| TotalWords[j].endsWith(" \" ")) && TotalWords[j].length() > 0 && TotalWords[j] != null) {
                TotalWords[j] = TotalWords[j].substring(0, TotalWords[j].length() - 1);
            }
            if ((TotalWords[j].startsWith("(")||TotalWords[j].startsWith("“")||TotalWords[j].startsWith("\""))  && TotalWords[j].length() > 0 && TotalWords[j] != null) {
                TotalWords[j] = TotalWords[j].substring(1);
            }
            //Diferent characters ’ and ' that are used for the same
            if(TotalWords[j].contains("’")) TotalWords[j] = TotalWords[j].replace("’","'");
            boolean correct = false;
            //Search for the correct word
//System.err.println(TotalWords[j]);
            for(int i = 0; i < Words.size(); ++i){
                //The checking part distinguish between capital letters, switching to LowerCase letters except "I"
                if(TotalWords[j].length() > 1 || (TotalWords[j].length() == 1 && !TotalWords[j].equals("I"))) TotalWords[j] =TotalWords[j].toLowerCase();
                //found the word (correct words have $ at the beginning of it) or is a number
                if((Words.get(i)).equals("$"+TotalWords[j]) ||TotalWords[j].matches("\\d+") ){
                    //Found the word no need to keep looking
                    correct = true;
                    break;
                }
            }
            //if we don't find the word, means is misspelled
            if (!correct){
                ++misspellingWords;

            }
            //check one word at a time, after checking we remove it from the list
            //WordToCheck.remove(0);
        }
	    //calculate probability

      double probability = (double)misspellingWords/TotalWords.length;
      return probability;
  }
    
}
