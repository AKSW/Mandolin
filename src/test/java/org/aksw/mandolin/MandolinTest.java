package org.aksw.mandolin;

import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class MandolinTest {

	@Test
	public void mandolin() {

		String args = "eval/0001aksw datasets/AKSW-one-out.nt "
				+ "http://mandolin.aksw.org/example/topic 95 10 95 "
				+ "false false false";

		String[] argsw = args.split(" ");

		try {
			Mandolin.main(argsw);
		} catch (Exception e) {
			fail();
		}

	}

}
