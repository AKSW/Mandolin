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
	static Dataset DATASET;
	
	/**
	 * Experiment code = Second parameter.
	 */
	static String EXP_CODE;
	
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		
		if(args.length > 0) {
			DATASET = Dataset.valueOf(args[0]);
			EXP_CODE = args[1];
		} else {
			// demo values
			DATASET = Dataset.FB15K;
			EXP_CODE = "09_?m_v";
		}
		
		final String REF = DATASET.ref;
		final String BASE = "eval/" + DATASET.prefix + EXP_CODE;
		
		ArrayList<Double> meanranks = new ArrayList<>();
		
		for(int i=1; i<=5; i++) {
			
			String testSet = REF;
			String output = BASE.replace("?", String.valueOf(i));
			
			MeanRankCalc mr = new MeanRankCalc(testSet, output);
			mr.setMinThr(0);
			mr.partitionData();
			meanranks.add(mr.start());
			
		}
		
		System.out.println("\nmeanranks = " + meanranks);

	}

}
