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

class WAnalyzerS implements WritingStyleAnalyzer{

  /**
   * Given a text this calculates the probabilities for different metrics that describes the writing style of the text author.
   *
   * @param text
   */
  public void analyze(Path text){
    int i = 0;
  }

  /**
   * Get the probabilities for sentences of length [0..longest sentence in the text].
   *
   * @return
   */
  public double[] getSentenceLengthProbabilities(Path text) {
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
  }
  /**
   * Get the probabilities for words of length [0..longest word in the text].
   *
   * @return
   */
  public double[] getWordLengthProbabilities(Path text) {
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
  }

  /**
   * Get the probabilities for number of sentences per paragraph from [0..most number of sentences in a paragraph in the text].
   *
   * @return
   */
  public double[] getSentencesPerParagraphProbabilities(Path text){
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
    }

  /**
   * Get the probabilities for ratios (answer length / question length).
   *
   * @return
   */
  public double[] getQuestionLengthToAnswerLengthRatioProbabilities(Path text){
	  double [] i= new double[2];
    return i;
  }
}
