package org.aksw.mandolin;

import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class MandolinTest {

	@Test
	public void aimRelation() throws Exception {

		String theArgs = "eval/mandolin-test AKSW-one-out.nt "
				+ "http://mandolin.aksw.org/example/topic 95 10 95 "
				+ "false false false";
		
		run(theArgs);
		
	}

	@Test
	public void aimAnything() throws Exception {

		String theArgs = "eval/mandolin-test AKSW-one-out.nt "
				+ "* 95 10 95 false false false";

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
