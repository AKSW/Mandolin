package org.aksw.mandolin.amie;

import javatools.datatypes.ByteString;
import amie.rules.Rule;

/**
 * Driver of rules from Amie to ProbKB.
 * 
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class RuleDriver {

	private static final String HEAD_LEFT = "?a";
	private static final String HEAD_RIGHT = "?b";

	public RuleDriver() {
		super();
	}

	public void process(Rule rule) {
		
		int size = rule.getBody().size();
		if(size == 0) {
			System.out.println("size = 0");
			return;
		}

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
			String pBodyQ = b1[1].toString();
			String pBodyR = b2[1].toString();

			if (b1[0].toString().equals(HEAD_LEFT) && b2[0].toString().equals(HEAD_RIGHT))
				addTypeThree(pHead, pBodyQ, pBodyR,
						toWeight(rule.getPcaConfidence()));
			if (b1[0].toString().equals(HEAD_RIGHT) && b2[0].toString().equals(HEAD_LEFT))
				addTypeThree(pHead, pBodyR, pBodyQ,
						toWeight(rule.getPcaConfidence()));
			
			if (b1[0].toString().equals(HEAD_LEFT) && b2[2].toString().equals(HEAD_RIGHT))
				addTypeFour(pHead, pBodyQ, pBodyR,
						toWeight(rule.getPcaConfidence()));
			if (b1[2].toString().equals(HEAD_RIGHT) && b2[0].toString().equals(HEAD_LEFT))
				addTypeFour(pHead, pBodyR, pBodyQ,
						toWeight(rule.getPcaConfidence()));
			
			if (b1[2].toString().equals(HEAD_LEFT) && b2[0].toString().equals(HEAD_RIGHT))
				addTypeFive(pHead, pBodyQ, pBodyR,
						toWeight(rule.getPcaConfidence()));
			if (b1[0].toString().equals(HEAD_RIGHT) && b2[2].toString().equals(HEAD_LEFT))
				addTypeFive(pHead, pBodyR, pBodyQ,
						toWeight(rule.getPcaConfidence()));
			
			if (b1[2].toString().equals(HEAD_LEFT) && b2[2].toString().equals(HEAD_RIGHT))
				addTypeSix(pHead, pBodyQ, pBodyR,
						toWeight(rule.getPcaConfidence()));
			if (b1[2].toString().equals(HEAD_RIGHT) && b2[2].toString().equals(HEAD_LEFT))
				addTypeSix(pHead, pBodyR, pBodyQ,
						toWeight(rule.getPcaConfidence()));
			
		}
	}

	/**
	 * @param pcaConfidence
	 * @return
	 */
	private double toWeight(double pcaConfidence) {
		return pcaConfidence * 100;
	}

	/**
	 * p(x,y) <- q(x,y)
	 *
	 * @param pHead
	 * @param pBody
	 * @param weight
	 */
	private void addTypeOne(String pHead, String pBody, double weight) {
		// TODO mysql insert into table mln1...
		System.out.println("Adding type one: "+pHead+", "+pBody+", "+weight);
	}

	/**
	 * p(x,y) <- q(y,x)
	 *
	 * @param pHead
	 * @param pBody
	 * @param weight
	 */
	private void addTypeTwo(String pHead, String pBody, double weight) {
		// TODO
		System.out.println("Adding type two: "+pHead+", "+pBody+", "+weight);
	}

	/**
	 * p(x,y) <- q(x,z), r(y,z)
	 * 
	 * @param pHead
	 * @param pBodyQ
	 * @param pBodyR
	 * @param weight
	 */
	private void addTypeThree(String pHead, String pBodyQ, String pBodyR,
			double weight) {
		// TODO
		System.out.println("Adding type three: "+pHead+", "+pBodyQ+", "+pBodyR+", "+weight);
	}

	/**
	 * p(x,y) <- q(x,z), r(z,y)
	 * @param pHead
	 * @param pBodyQ
	 * @param pBodyR
	 * @param weight
	 */
	private void addTypeFour(String pHead, String pBodyQ, String pBodyR,
			double weight) {
		// TODO
		System.out.println("Adding type four: "+pHead+", "+pBodyQ+", "+pBodyR+", "+weight);
	}

	/**
	 * p(x,y) <- q(z,x), r(y,z)
	 * @param pHead
	 * @param pBodyQ
	 * @param pBodyR
	 * @param weight
	 */
	private void addTypeFive(String pHead, String pBodyQ, String pBodyR,
			double weight) {
		// TODO
		System.out.println("Adding type five: "+pHead+", "+pBodyQ+", "+pBodyR+", "+weight);
	}

	/**
	 * p(x,y) <- q(z,x), r(z,y)
	 * @param pHead
	 * @param pBodyQ
	 * @param pBodyR
	 * @param weight
	 */
	private void addTypeSix(String pHead, String pBodyQ, String pBodyR,
			double weight) {
		// TODO
		System.out.println("Adding type six: "+pHead+", "+pBodyQ+", "+pBodyR+", "+weight);
	}

}
