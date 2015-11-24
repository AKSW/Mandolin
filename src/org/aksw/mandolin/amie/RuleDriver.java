package org.aksw.mandolin.amie;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javatools.datatypes.ByteString;

import org.aksw.mandolin.NameMapperProbKB;
import org.aksw.mandolin.ProbKBData;

import amie.rules.Rule;

import com.opencsv.CSVWriter;

/**
 * Driver of rules from Amie to ProbKB.
 * 
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class RuleDriver {
	
	private NameMapperProbKB map;
	private String base;

	private static final String HEAD_LEFT = "?a";
	private static final String HEAD_RIGHT = "?b";
	
	private HashMap<String, ArrayList<String[]>> csvContent = new HashMap<>();
	
	public RuleDriver(NameMapperProbKB map, String base) {
		super();
		this.map = map;
		this.base = base;
		for(int i=1; i<=6; i++)
			csvContent.put(base + "/mln"+i+".csv", new ArrayList<>());
	}

	public void process(Rule rule) throws IOException {
		
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
		return pcaConfidence * 100;
	}

	/**
	 * p(x,y) <- q(x,y)
	 *
	 * @param pHead
	 * @param pBody
	 * @param weight
	 * @throws IOException 
	 */
	private void addTypeOne(String pHead, String pBody, double weight) {
		System.out.println("Adding type one: "+pHead+", "+pBody+", "+weight);
		String str[] = {
				map.getName(pHead).substring(ProbKBData.REL_LENGTH),
				map.getName(pBody).substring(ProbKBData.REL_LENGTH),
				"0", // TODO class of pHead
				"0", // TODO class of pBody
				"" + weight
		};
		csvContent.get(base + "/mln1.csv").add(str);
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

	public void buildCSV() {
		// TODO Auto-generated method stub
		for(String key : csvContent.keySet()) {
			CSVWriter writer = null;
			try {
				writer = new CSVWriter(new FileWriter(new File(key)));
				for(String[] line : csvContent.get(key))
					writer.writeNext(line);
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
	}

}
