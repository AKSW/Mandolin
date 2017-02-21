package org.aksw.mandolin.rulemining;

import java.io.IOException;

import javatools.datatypes.ByteString;

import org.aksw.mandolin.controller.NameMapper;

import amie.rules.Rule;

/**
 * Driver of rules from Amie to ProbKB.
 * 
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class AmieRuleDriver extends RuleDriver {
	
	private static final String HEAD_LEFT = "?a";
	private static final String HEAD_RIGHT = "?b";
	
	public AmieRuleDriver(NameMapper map, String base) {
		super(map, base);
	}

	/**
	 * @param rule
	 * @throws IOException
	 */
	public void process(Rule rule) throws IOException {
		
		int size = rule.getBody().size();

		if (size == 1) { // call one or two

			ByteString[] b = rule.getBody().get(0);
			// subject, predicate, object
			String pHead = rule.getHeadRelation();
			String pBody = b[1].toString(); // TODO check me!
			if (b[0].toString().equals(HEAD_LEFT))
				addTypeOne(pHead, pBody, toWeight(rule.getPcaConfidence()));
			else
				addTypeTwo(pHead, pBody, toWeight(rule.getPcaConfidence()));
		} else { // call three to six

			ByteString[] b1 = rule.getBody().get(0);
			ByteString[] b2 = rule.getBody().get(1);

			String pHead = rule.getHeadRelation();
			String pBody1 = b1[1].toString();
			String pBody2 = b2[1].toString();

			if (b1[0].toString().equals(HEAD_LEFT) && b2[0].toString().equals(HEAD_RIGHT))
				addTypeThree(pHead, pBody1, pBody2,
						toWeight(rule.getPcaConfidence()));
			if (b1[0].toString().equals(HEAD_RIGHT) && b2[0].toString().equals(HEAD_LEFT))
				addTypeThree(pHead, pBody2, pBody1,
						toWeight(rule.getPcaConfidence()));
			
			if (b1[0].toString().equals(HEAD_LEFT) && b2[2].toString().equals(HEAD_RIGHT))
				addTypeFour(pHead, pBody1, pBody2,
						toWeight(rule.getPcaConfidence()));
			if (b1[2].toString().equals(HEAD_RIGHT) && b2[0].toString().equals(HEAD_LEFT))
				addTypeFour(pHead, pBody2, pBody1,
						toWeight(rule.getPcaConfidence()));
			
			if (b1[2].toString().equals(HEAD_LEFT) && b2[0].toString().equals(HEAD_RIGHT))
				addTypeFive(pHead, pBody1, pBody2,
						toWeight(rule.getPcaConfidence()));
			if (b1[0].toString().equals(HEAD_RIGHT) && b2[2].toString().equals(HEAD_LEFT))
				addTypeFive(pHead, pBody2, pBody1,
						toWeight(rule.getPcaConfidence()));
			
			if (b1[2].toString().equals(HEAD_LEFT) && b2[2].toString().equals(HEAD_RIGHT))
				addTypeSix(pHead, pBody1, pBody2,
						toWeight(rule.getPcaConfidence()));
			if (b1[2].toString().equals(HEAD_RIGHT) && b2[2].toString().equals(HEAD_LEFT))
				addTypeSix(pHead, pBody2, pBody1,
						toWeight(rule.getPcaConfidence()));
			
		}
	}

	/**
	 * @param pcaConfidence
	 * @return
	 */
	private double toWeight(double pcaConfidence) {
		return pcaConfidence;
	}


}
