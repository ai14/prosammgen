package com.github.ai14.prosammgen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import com.google.common.collect.ImmutableList;
import com.github.ai14.prosammgen.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.stream.Collectors;


public class WAnalyzerS implements WritingStyleAnalyzer{ 

private Path text;
 
  /**
   * Given a text this calculates the probabilities for different metrics that describes the writing style of the text author.
   *
   * @param text
   */
  public void analyze(Path text){

      this.text = text;
  }

  /**
   * Get the probabilities for sentences of length [0..longest sentence in the text].
   *
   * @return
   */
@Override  
  public double[] getSentenceLengthProbabilities() {
  	double[] probabilities = new double[0];
  	try{
	    int SentenceNumber = 0;
        //Using this to also count the first sentece of the text
	    int FirstSentence = 0;
		List<Integer>sentenceSize = new ArrayList<>();
	    //Read the text
	    for (String line : Files.readAllLines(text)) {
            //Spliting text in words.
	        String[] words = line.split("\\s+");
	        for (int i = 0; i < words.length; i++) {
	          // find all sentences
	          if (FirstSentence == 0 || words[i].endsWith(".")) {
                  FirstSentence = 1;
	              int count = 1;//To count the first word too.
	              ++i;
	            //Check words of the sentence
	            while(!words[i].endsWith(".")){
	              count ++;
	              ++i;
	            }
                //increasing quantity of sentence with "count" words.
	            int sentenceSameLength = sentenceSize.get(count);
                  sentenceSameLength++;
	            sentenceSize.set(count, sentenceSameLength);
	            ++SentenceNumber;
	          }
	        }
	    }
        probabilities = new double [sentenceSize.size()];
        //Calculating probabilities
	    for(int j = 0; j < probabilities.length; j++){
            probabilities[j] = sentenceSize.get(j)/SentenceNumber;
	    }
  	}catch (IOException e) {
	      System.err.println("Couldn't read the text");
    	} finally {
      		return probabilities;
    	}
   
  }
  /**
   * Get the probabilities for words of length [0..longest word in the text].
   *
   * @return
   */
   @Override
  public double[] getWordLengthProbabilities(){
  	double[] probabilities = new double[0];
  	try{
	    int textSize = 0;
	    List<Integer>wordSize = new ArrayList<>();
	    //Read the text
	    for (String line : Files.readAllLines(text)) {
            //getting all the words
	        String[] words = line.split("\\s+");
	        textSize = words.length;
	        for (int i = 0; i < textSize; i++) {
	          //increasing the number of characters with words[i].length
			  int WordsSameLength = wordSize.get(words[i].length());
              WordsSameLength++;
			  wordSize.set(words[i].length(), WordsSameLength);
	        }
	    }
        //Calculating probabilities
        probabilities = new double[wordSize.size()];
	    for(int j = 0; j < probabilities.length ;j++){
            probabilities[j] = wordSize.get(j)/textSize;
	    }
  	}catch (IOException e) {
	      System.err.println("Couldn't read the text");
    	} finally {
      		return probabilities;
    	}
  }

  /**
   * Get the probabilities for number of sentences per paragraph from [0..most number of sentences in a paragraph in the text].
   *
   * @return
   */
   @Override
  public double[] getSentencesPerParagraphProbabilities(){
  	double[] probabilities = new double[0];
  	try{
		int numParagraph = 0;
		List<Integer>SentencesParagraph = new ArrayList<>();
		//Read the text
		for (String line : Files.readAllLines(text)) {
			//getting text per paragraph
			String[] paragraphs = line.split("\r");
			numParagraph = paragraphs.length;
			//each paragraph
			for (int i = 0; i < numParagraph; i++) {
				//Split into words every paragraph
				String[] worksParagraph = line.split("\\s+");
				int countSentences = 0;
				//Count number of sentence per paragraph
				for (int j = 0; j < worksParagraph.length; j++) {
					// find all sentences per paragraph
					if (worksParagraph[j].endsWith("."))++countSentences;
				}
                //increasing the times that a paragraph have "countSentences" sentences
				int sentencesParagraph = SentencesParagraph.get(countSentences);
                sentencesParagraph++;
				SentencesParagraph.set(countSentences, sentencesParagraph);
			}
		}
        probabilities = new double [SentencesParagraph.size()];
		for(int j = 0; j < probabilities.length; j++){
            probabilities[j] = SentencesParagraph.get(j)/numParagraph;
		}
  	}catch (IOException e) {
	      System.err.println("Couldn't read the text");
    	} finally {
      		return probabilities;
    	}
  	

    }

  /**
   * Get the probabilities for ratios (answer length / question length).
   *
   * @return
   */
   @Override
  public double[] getQuestionLengthToAnswerLengthRatioProbabilities(){
       //TODO: Implement function
	  double [] i= new double[2];
    return i;
  }
  
   /**
   * Get the probabilities of mispelling words per sentence
   *
   * @return
   */
   @Override
  public double getMisspellingWordsProbabilities(){ //TODO: take it off this class
  double probability = 0;
  try{
  	    WordNetSynonyms WordSynonym = new WordNetSynonyms();
	    int TotalSentences = 0;
	    int misspellingWords = 0;
	    List<Integer>wordSize = new ArrayList<>();
	    //Read the text
	    for (String line : Files.readAllLines(text)) {
	    	//split text in sentences
	        String[] sentences = line.split("(?i)(?<=[.?!])\\S+(?=[a-z])");
	        TotalSentences = sentences.length;
	        //each sentence
	        for (int i = 0; i < TotalSentences; i++) {
		    	//split the sentence in words
				String[] words = sentences[i].split("\\s+");
				ImmutableList<String>WordToCheck = null;
				ImmutableList<String>PossibleSynonym;
                //per each word
				for(int j = 0;j < words.length; ++j){
                    WordToCheck.add(0, words[j]);
					//get synonyms
                    PossibleSynonym = WordSynonym.getSynonyms(WordToCheck); //TODO: not this name in master
					//if there is no synonym (only get the same word as output of the function means that the word is misspelled
					if(PossibleSynonym.size() == 1 && (PossibleSynonym.get(0)) == words[j]) ++misspellingWords;
                    //check one word at a time, after checking we remove it from the list
                    WordToCheck.remove(0);
				}
	        }
	    }
	    //calculate probability
      probability = misspellingWords/TotalSentences;
	  }catch (IOException e) {
		      System.err.println("Couldn't read the text");
    	} finally {
      		return probability;
    	}
  }
    
}
