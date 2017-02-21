package org.aksw.mandolin;

import static org.junit.Assert.fail;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class MandolinTest {

	@Test
	public void aimRelationUsingAMIE() throws Exception {

		FileUtils.deleteDirectory(new File("eval/mandolin-test"));
		
		String theArgs = "--output eval/mandolin-test --input AKSW-one-out.nt "
				+ "--aim http://mandolin.aksw.org/example/topic --miner AMIE";
		
		run(theArgs);
		
	}

	@Test
	public void aimRelationUsingHC() throws Exception {

		FileUtils.deleteDirectory(new File("eval/mandolin-test"));
		
		String theArgs = "--output eval/mandolin-test --input AKSW-one-out.nt "
				+ "--aim http://mandolin.aksw.org/example/topic";
		
		run(theArgs);
		
	}

//	@Test
	public void fullLinkPrediction() throws Exception {

		FileUtils.deleteDirectory(new File("eval/mandolin-test"));
		
		String theArgs = "--output eval/mandolin-test --input AKSW-one-out.nt "
				+ "--aim *";

		run(theArgs);
				
	}

	private void run(String theArgs) {
		
		String[] theArgsArray = theArgs.split(" ");
		
		try {
			Mandolin.main(theArgsArray);
		} catch (Exception e) {
			fail();
		}
		
	}
	
}
