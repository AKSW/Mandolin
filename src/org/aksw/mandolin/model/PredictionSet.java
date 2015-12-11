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
	private String target;
	
	public PredictionSet(String target) {
		this.target = target;
	}
	
	public String getTarget() {
		return target;
	}

}
