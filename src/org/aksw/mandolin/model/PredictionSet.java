package org.aksw.mandolin.model;

import java.util.TreeSet;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class PredictionSet extends TreeSet<PredictionLiteral> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 864082651004354757L;

	/**
	 * Internal name only.
	 */
	private String aim;
	
	public PredictionSet(String aim) {
		this.aim = aim;
		System.out.println("Created prediction set with aim: "+aim);
	}
	
	public String getTarget() {
		return aim;
	}

}
