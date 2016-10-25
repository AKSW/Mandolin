package org.aksw.mandolin.eval;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class LinkPredictionEvaluation {
	
	/**
	 * Dataset = First parameter. 
	 */
	final static Dataset DATASET = Dataset.FB15K;
	
	/**
	 * Experiment code = Second parameter.
	 */
	final static String EXP_CODE = "08_???m_va";
	
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		
		final String REF = DATASET.ref;
		final String BASE = "eval/" + DATASET.prefix + EXP_CODE;
		
		ArrayList<Double> meanranks = new ArrayList<>();
		
		for(int i=1; i<=6; i++) {
			
			// dirty hack
			if(i==6) i = 10;
			
			String testSet = REF;
			String output = BASE.replace("???", String.valueOf(i));
			
			MeanRankCalc mr = new MeanRankCalc(testSet, output);
			mr.setMinThr(0);
			mr.partitionData();
			meanranks.add(mr.start());
			
		}
		
		System.out.println("\nmeanranks = " + meanranks);

	}

}
