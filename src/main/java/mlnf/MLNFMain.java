package mlnf;

import java.util.Arrays;

import mlnf.eval.MeanRankCalc;

/**
 * Main controller for mlnf.
 * 
 * @author author <email>
 *
 */
public class MLNFMain {

	public static void main(String[] args) throws NumberFormatException, Exception {
		
		String[] argsw = Arrays.copyOfRange(args, 1, args.length);
		
		switch(args[0]) {
		case "plain":
			MLNF.main(argsw);
			break;
		case "eval":
			MeanRankCalc.main(argsw);
			break;
		default:
		}
		
	}

}
