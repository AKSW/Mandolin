package org.aksw.mandolin.rulemining;

import org.aksw.mandolin.controller.NameMapper;

/**
 * @author Tommaso Soru {@literal tsoru@informatik.uni-leipzig.de}
 *
 */
public class HornConcertoRuleDriver extends RuleDriver {

	public HornConcertoRuleDriver(NameMapper map, String base) {
		super(map, base);
	}
	
	public void process(int type, Double weight, String... terms) {
		
		drive(type, weight, terms);
		
	}

}
