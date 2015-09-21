package org.aksw.mandolin.semantifier;

import java.io.IOException;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class SemantifierPipeline {

	public static void main(String[] args) throws ClassNotFoundException, IOException {
		
		int n = 10;
		
//		new DatasetBuildStarter().run();
//		new DatasetBuilderAlgorithm().run();
		System.err.println("SEMANTIFIER");
		DatasetBuildSemantifier semr = new DatasetBuildSemantifier(n);
		semr.linkedDBLP();
//		semr.mapping();
//		semr.linkedACM();
//		System.err.println("FIXER");
//		DatasetBuildFixer fixr = new DatasetBuildFixer();
//		fixr.run();
//		fixr.fix();
//		System.err.println("CLOSURE");
//		DatasetBuildClosure clsr = new DatasetBuildClosure();
//		clsr.runReflSymmTransClosure();
		
	}

}
