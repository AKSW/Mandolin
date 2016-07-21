package org.aksw.mandolin.semsrl;

import java.io.IOException;

import org.aksw.mandolin.semsrl.eval.AlchemyEvaluation;

/**
 * Statistical Relational Learning of Semantic Links using Markov Logic Networks.
 * <br>
 * Warning: experimental code.
 * 
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class SemMLNController {

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		
		if(args[2].equals("start"))
			SemMLN.main(new String[]{args[0], args[1]});
	
		if(args[2].equals("eval")) {
			switch(args[1]) {
			case "alchemy":
				AlchemyEvaluation.run(new String[]{args[0]});
				break;
			case "netkit":
			case "probcog":
			default:
				System.err.println("Not yet implemented.");
			}
		}
		
	}

}
