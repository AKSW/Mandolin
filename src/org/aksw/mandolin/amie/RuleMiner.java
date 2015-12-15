package org.aksw.mandolin.amie;

import org.aksw.mandolin.NameMapper;

import javatools.datatypes.ByteString;
import amie.rules.Rule;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class RuleMiner {

	public static void run(NameMapper map, String base, String output) throws Exception {
		
		AmieHandler h = new AmieHandler(output);
		h.run();
		
		RuleDriver driver = new RuleDriver(map, base);
		for(Rule rule : h.getRules()) {
			// send rule to driver
			driver.process(rule);
			// print rule information
			String str = "";
			for(ByteString[] bs : rule.getBody()) {
				String bstr = "";
				for(ByteString b : bs)
					bstr += b + ",";
				str += bstr + " | ";
			}
			System.out.println(rule.getHeadRelation() + "\t" + str + "\t" + rule.getPcaConfidence());
		}
		
		// make CSVs
		driver.buildCSV();

	}
	
}
