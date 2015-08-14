package org.aksw.mandolin;

import java.io.IOException;

import org.aksw.mandolin.semsrl.eval.AlchemyEvaluation;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class SemMLNController {

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		
		if(args[2].equals("start"))
			SemMLN.main(new String[]{args[0], args[1]});
	
		if(args[2].equals("eval") && args[1].equals("alchemy"))
			AlchemyEvaluation.run(new String[]{args[0]});
		
	}

}
