package org.aksw.simba.semsrl;

import java.io.IOException;

import org.aksw.simba.semsrl.eval.AlchemyEvaluation;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class Main {

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		
		if(args[2].equals("start"))
			SemMLN.main(new String[]{args[0], args[1]});
	
		if(args[2].equals("eval") && args[1].equals("alchemy"))
			AlchemyEvaluation.main(new String[]{args[0]});
		
	}

}
