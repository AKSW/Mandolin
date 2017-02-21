package org.aksw.mandolin.rulemining;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.aksw.mandolin.controller.NameMapper;
import org.aksw.mandolin.controller.ProbKBData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.opencsv.CSVWriter;

/**
 * @author Tommaso Soru {@literal tsoru@informatik.uni-leipzig.de}
 *
 */
public abstract class RuleDriver {

	protected final static Logger logger = LogManager.getLogger(RuleDriver.class);
	
	protected NameMapper map;
	protected String base;

	protected HashMap<String, ArrayList<String[]>> csvContent = new HashMap<>();
	
	public RuleDriver(NameMapper map, String base) {
		super();
		this.map = map;
		this.base = base;
		for(int i=1; i<=6; i++)
			csvContent.put(base + "/mln"+i+".csv", new ArrayList<>());
	}

	/**
	 * @param type
	 * @param weight
	 * @param parts
	 */
	protected void drive(int type, double weight, String... parts) {
		switch(type) {
		case 1:
			addTypeOne(parts[0], parts[1], weight);
			return;
		case 2:
			addTypeTwo(parts[0], parts[1], weight);
			return;
		case 3:
			addTypeThree(parts[0], parts[1], parts[2], weight);
			return;
		case 4:
			addTypeFour(parts[0], parts[1], parts[2], weight);
			return;
		case 5:
			addTypeFive(parts[0], parts[1], parts[2], weight);
			return;
		case 6:
			addTypeSix(parts[0], parts[1], parts[2], weight);
			return;
		}
	}
	
	/**
	 * p(x,y) <- q(x,y)
	 *
	 * @param pHead
	 * @param pBody
	 * @param weight
	 * @throws IOException 
	 */
	protected void addTypeOne(String pHead, String pBody, double weight) {
		logger.trace("Adding type one: "+pHead+", "+pBody+", "+weight);
		String headName = map.getName(pHead).substring(ProbKBData.REL_LENGTH);
		String bodyName = map.getName(pBody).substring(ProbKBData.REL_LENGTH); 
		String str[] = {
				headName,
				bodyName,
				"1", // TODO class of x
				"1", // TODO class of y
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
	protected void addTypeTwo(String pHead, String pBody, double weight) {
		logger.trace("Adding type two: "+pHead+", "+pBody+", "+weight);
		String str[] = {
				map.getName(pHead).substring(ProbKBData.REL_LENGTH),
				map.getName(pBody).substring(ProbKBData.REL_LENGTH),
				"1", // TODO class of x
				"1", // TODO class of y
				"" + weight
		};
		csvContent.get(base + "/mln2.csv").add(str);
	}

	/**
	 * p(x,y) <- q(x,z), r(y,z)
	 * 
	 * @param pHead
	 * @param pBodyQ
	 * @param pBodyR
	 * @param weight
	 */
	protected void addTypeThree(String pHead, String pBodyQ, String pBodyR,
			double weight) {
		logger.trace("Adding type three: "+pHead+", "+pBodyQ+", "+pBodyR+", "+weight);
		String str[] = {
				map.getName(pHead).substring(ProbKBData.REL_LENGTH),
				map.getName(pBodyQ).substring(ProbKBData.REL_LENGTH),
				map.getName(pBodyR).substring(ProbKBData.REL_LENGTH),
				"1", // TODO class of x
				"1", // TODO class of y
				"1", // TODO class of z
				"" + weight
		};
		csvContent.get(base + "/mln3.csv").add(str);
	}

	/**
	 * p(x,y) <- q(x,z), r(z,y)
	 * @param pHead
	 * @param pBodyQ
	 * @param pBodyR
	 * @param weight
	 */
	protected void addTypeFour(String pHead, String pBodyQ, String pBodyR,
			double weight) {
		logger.trace("Adding type four: "+pHead+", "+pBodyQ+", "+pBodyR+", "+weight);
		String str[] = {
				map.getName(pHead).substring(ProbKBData.REL_LENGTH),
				map.getName(pBodyQ).substring(ProbKBData.REL_LENGTH),
				map.getName(pBodyR).substring(ProbKBData.REL_LENGTH),
				"1", // TODO class of x
				"1", // TODO class of y
				"1", // TODO class of z
				"" + weight
		};
		csvContent.get(base + "/mln4.csv").add(str);
	}

	/**
	 * p(x,y) <- q(z,x), r(y,z)
	 * @param pHead
	 * @param pBodyQ
	 * @param pBodyR
	 * @param weight
	 */
	protected void addTypeFive(String pHead, String pBodyQ, String pBodyR,
			double weight) {
		logger.trace("Adding type five: "+pHead+", "+pBodyQ+", "+pBodyR+", "+weight);
		String str[] = {
				map.getName(pHead).substring(ProbKBData.REL_LENGTH),
				map.getName(pBodyQ).substring(ProbKBData.REL_LENGTH),
				map.getName(pBodyR).substring(ProbKBData.REL_LENGTH),
				"1", // TODO class of x
				"1", // TODO class of y
				"1", // TODO class of z
				"" + weight
		};
		csvContent.get(base + "/mln5.csv").add(str);
	}

	/**
	 * p(x,y) <- q(z,x), r(z,y)
	 * @param pHead
	 * @param pBodyQ
	 * @param pBodyR
	 * @param weight
	 */
	protected void addTypeSix(String pHead, String pBodyQ, String pBodyR,
			double weight) {
		logger.trace("Adding type six: "+pHead+", "+pBodyQ+", "+pBodyR+", "+weight);
		String str[] = {
				map.getName(pHead).substring(ProbKBData.REL_LENGTH),
				map.getName(pBodyQ).substring(ProbKBData.REL_LENGTH),
				map.getName(pBodyR).substring(ProbKBData.REL_LENGTH),
				"1", // TODO class of x
				"1", // TODO class of y
				"1", // TODO class of z
				"" + weight
		};
		csvContent.get(base + "/mln6.csv").add(str);
	}

	public void buildCSV() {
		
		for(String key : csvContent.keySet()) {
			CSVWriter writer = null;
			try {
				writer = new CSVWriter(new FileWriter(new File(key)));
				for(String[] line : csvContent.get(key))
					writer.writeNext(line);
				writer.close();
			} catch (IOException e) {
				logger.error(e.getMessage());
				// XXX RuntimeException?
			}			
		}
	}

}
