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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.stream.Collectors;


public class WAnalyzerS implements WritingStyleAnalyzer{ 

private final Path text;
 
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
  	try{
  		 //probs from the text
	    int numSentence = 0;
	    int beginT = 0;
		List<Integer>sentenceSize = new ArrayList<>();
	    //Read the text
	    for (String line : Files.readAllLines(text)) {
	        String[] words = line.split("\\s+");
	        for (int i = 0; i < words.length; i++) {
	          // find all sentences
	          if (beginT == 0 || words[i].endsWith(".")) {
	            beginT = 1;
	            int count = 1;
	            ++i;
	            //Check distance of the sentence
	            while(!words[i].endsWith(".")){
	              count ++;
	              ++i;
	            }
	            int x = sentenceSize.get(count);
	            x++;
	            sentenceSize.set(count, x);
	            ++numSentence;
	          }
	        }
	      }
	      double[] prob = new double [sentenceSize.size()];
	      for(int j = 0; j < prob.length; j++){
	        prob[j] = sentenceSize.get(j)/numSentence;
	      }
	      return prob;
  	}catch (IOException e) {
	      System.err.println("Couldn't read the text");
    	}
   
  }
  /**
   * Get the probabilities for words of length [0..longest word in the text].
   *
   * @return
   */
   @Override
  public double[] getWordLengthProbabilities(){
  	try{
  		//probs from the text
	    int textSize = 0;
	    List<Integer>wordSize = new ArrayList<>();
	    //Read the text
	    for (String line : Files.readAllLines(text)) {
	        String[] words = line.split("\\s+");
	        textSize = words.length;
	        for (int i = 0; i < textSize; i++) {
	          //word size        
			  int x = wordSize.get(words[i].length());
			  x++;
			  wordSize.set(words[i].length(), x);
	        }
	    }
	    //calculate probs
	    double[] prob = new double[wordSize.size()];
	    for(int j = 0; j < prob.length ;j++){
	        prob[j] = wordSize.get(j)/textSize;
	    }
	    return prob;
  		
  	}catch (IOException e) {
	      System.err.println("Couldn't read the text");
    	}
  }

  /**
   * Get the probabilities for number of sentences per paragraph from [0..most number of sentences in a paragraph in the text].
   *
   * @return
   */
   @Override
  public double[] getSentencesPerParagraphProbabilities(){
  	try{
	  			//probs from the text
		int numParagraph = 0;
		List<Integer>SentencesParagraph = new ArrayList<>();
		//Read the text
		for (String line : Files.readAllLines(text)) {
			//Split in paragraphs
			String[] paragraphs = line.split("\r");
			numParagraph = paragraphs.length;
			//each paragraph
			for (int i = 0; i < numParagraph; i++) {
				//Split into words every paragraph
				String[] worksParagraph = line.split("\\s+");
				int countSP = 0;			
				//Count number of sentence per paragraph
				for (int j = 0; j < worksParagraph.length; j++) {
					// find all sentences per paragraph
					if (worksParagraph[j].endsWith("."))++countSP;
				}
				int x = SentencesParagraph.get(countSP);
				x++;
				SentencesParagraph.set(countSP, x);
			}
		}	
		double[] prob = new double [SentencesParagraph.size()];
		for(int j = 0; j < prob.length; j++){
			prob[j] = SentencesParagraph.get(j)/numParagraph;
		}
		return prob;
  	}catch (IOException e) {
	      System.err.println("Couldn't read the text");
    	}
  	

    }

  /**
   * Get the probabilities for ratios (answer length / question length).
   *
   * @return
   */
   @Override
  public double[] getQuestionLengthToAnswerLengthRatioProbabilities(){
	  double [] i= new double[2];
    return i;
  }
  
   /**
   * Get the probabilities of mispelling words per sentence
   *
   * @return
   */
   @Override
  public double getMispellingWordsProbabilities(){ //TODO: take it off this class
  try{
  	//probs from the text
	    int numSentences = 0;
	    int misPellingWords = 0;
	    List<Integer>wordSize = new ArrayList<>();
	    //Read the text
	    for (String line : Files.readAllLines(text)) {
	    	//split text in sentences
	        String[] sentences = line.split("(?i)(?<=[.?!])\\S+(?=[a-z])");
	        numSentences = sentences.length;
	        //each sentence
	        for (int i = 0; i < numSentences; i++) {
	        	ImmutableList<String>SynWord = new ImmutableList<String>() ;
	        	List<String>Swords = new ArrayList<>();
		    	//split the sentence in words
				String[] words = sentences[i].split("\\s+");
				for(int j = 0;j < words.length; ++j){
					
					Swords.add(words[j]);
	
					//check for synonyms
					ImmutableList<String>Synonyms = synonyms.getSynonyms(SynWord.copyOf(Sword));
					//Check for misspelling words
					if(Synonyms.size() == 1 && (Synonyms.get(0)) == words[j]) ++misPellingWords;
					Swords.remove(0);
				}
	        }
	    }
	    //calculate probs
	    double prob;
	    prob = misPellingWords/numSentences;
	    return prob;
	  }catch (IOException e) {
	      System.err.println("Couldn't read the text");
    	}
  }
    
}
