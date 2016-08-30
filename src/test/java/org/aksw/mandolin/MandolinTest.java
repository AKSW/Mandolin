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

		String theArgs = "--output eval/mandolin-test --input AKSW-one-out.nt "
				+ "--aim http://mandolin.aksw.org/example/topic";
		
		run(theArgs);
		
	}

	@Test
	public void aimAnything() throws Exception {

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
