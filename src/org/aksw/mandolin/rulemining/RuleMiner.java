package org.aksw.mandolin.rulemining;

import org.aksw.mandolin.controller.NameMapper;
import org.aksw.mandolin.rulemining.AmieHandler.MiningStrategy;

import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import javatools.datatypes.ByteString;
import amie.rules.Rule;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class RuleMiner {

	public static void run(NameMapper map, String base) throws Exception {
		
		AmieHandler h = new AmieHandler(base + "/model.tsv");
		
		h.run(MiningStrategy.HEAD_COVERAGE);
		if(h.getRules().isEmpty()) {
			h.run(MiningStrategy.SUPPORT);
			if(h.getRules().isEmpty())
				throw new RuntimeException("Mandolin halts: 0 rules mined.");
		}
		
		RuleDriver driver = new RuleDriver(map, base);
		for(Rule rule : h.getRules()) {
			
			// filter out RDF/RDFS/OWL-only rules
			if(isUpper(rule.getHeadRelation())) {
				boolean skip = true;
				for(ByteString[] bs : rule.getBody())
					if(!isUpper(bs[1].toString())) {
						skip = false;
						break;
					}
				if(skip) {
					System.out.println("Skipping upper-ontology rule...");
					continue;
				}
			}
			
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

	private static boolean isUpper(String headRelation) {
		if(headRelation.startsWith(OWL.NS))
			return true;
		if(headRelation.startsWith(RDF.getURI()))
			return true;
		if(headRelation.startsWith(RDFS.getURI()))
			return true;
		return false;
	}
	
}
