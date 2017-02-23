package mlnf.semantifier;

import java.io.IOException;

/**
 * 
 * @author author <email>
 *
 */
public class SemantifierPipeline {

	public static void main(String[] args) throws ClassNotFoundException,
			IOException {

		int n = Integer.parseInt(args[0]);

		if (args[1].equals("part1")) {

			new DatasetBuildStarter().run();
			new DatasetBuilderAlgorithm(n).run();

		} else if (args[1].equals("part2")) {

			System.out.println("SECTION START: FIXER");
			DatasetBuildFixer fixr = new DatasetBuildFixer();
			fixr.run();
			fixr.fix();
			System.out.println("SECTION START: SEMANTIFIER");
			DatasetBuildSemantifier semr = new DatasetBuildSemantifier(n);
			semr.linkedDBLP();
			semr.mapping();
//			semr.linkedACM();
			DatasetBuildSatellites.run();
//			System.out.println("SECTION START: CLOSURE");
//			DatasetBuildClosure clsr = new DatasetBuildClosure();
//			clsr.runReflSymmTransClosure();

		} else
			System.out.println("Second argument is {part1, part2}.");

	}

}
