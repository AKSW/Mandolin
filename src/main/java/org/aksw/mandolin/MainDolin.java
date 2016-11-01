package org.aksw.mandolin;

import java.util.Arrays;

import org.aksw.mandolin.eval.CrossValidation;
import org.aksw.mandolin.eval.LinkPredictionEvaluation;

/**
 * Main controller for Mandolin.
 * 
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class MainDolin {

	public static void main(String[] args) throws NumberFormatException, Exception {
		
		String[] argsw = Arrays.copyOfRange(args, 1, args.length);
		
		switch(args[0]) {
		case "plain":
			Mandolin.main(argsw);
			break;
		case "eval":
			LinkPredictionEvaluation.main(argsw);
			break;
		case "cv":
			CrossValidation.main(argsw);
			break;
		default:
		}
		
	}

}
