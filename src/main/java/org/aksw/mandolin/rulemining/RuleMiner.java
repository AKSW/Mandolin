package org.aksw.mandolin.rulemining;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.aksw.mandolin.controller.NameMapper;
import org.aksw.mandolin.rulemining.AmieHandler.MiningStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	
	private final static Logger logger = LogManager.getLogger(RuleMiner.class);

	/**
	 * @param map
	 * @param base
	 * @param mining
	 * @param maxRules
	 * @throws Exception
	 */
	public static void run(NameMapper map, String base, Double mining, Integer maxRules) throws Exception {
		
		boolean support = (mining == null);
		
		AmieHandler h = new AmieHandler(base + "/model.tsv");
		
		if(!support)  {
			h.setMiningThr(mining);
			h.run(MiningStrategy.HEAD_COVERAGE);
			if(h.getRules().isEmpty())
				support = true;
		}
		
		if(support) {
			h.run(MiningStrategy.SUPPORT);
			if(h.getRules().isEmpty()) {
				logger.fatal("Rules size = 0");
				throw new RuntimeException("Mandolin cannot continue without MLN rules!");
			}
		}
		
		List<Rule> rules = h.getRules();
		if(rules.isEmpty()) {
			logger.fatal("Rules size = 0");
			throw new RuntimeException("Mandolin cannot continue without MLN rules!");
		}
		
		TreeSet<String> topNRules = new TreeSet<>();
		if(maxRules != null) {
			HashMap<String, Double> rank = new HashMap<>();
			for(Rule rule : rules)
				rank.put(rule.toString(), rule.getPcaConfidence());
			ValueComparator bvc = new ValueComparator(rank);
	        TreeMap<String, Double> sortedRank = new TreeMap<String, Double>(bvc);
	        sortedRank.putAll(rank);
	        int i=0;
	        for(String key : sortedRank.keySet()) {
	        	topNRules.add(key);
	        	logger.trace(key + ", " + rank.get(key));
	        	if(++i == maxRules)
	        		break;
	        }
		}
		
		RuleDriver driver = new RuleDriver(map, base);
		
		for(Rule rule : rules) {
			
			if(maxRules != null)
				if(!topNRules.contains(rule.toString()))
					continue;
			
			// filter out RDF/RDFS/OWL-only rules
			if(isUpper(rule.getHeadRelation())) {
				boolean skip = true;
				for(ByteString[] bs : rule.getBody())
					if(!isUpper(bs[1].toString())) {
						skip = false;
						break;
					}
				if(skip) {
					logger.trace("Skipping upper-ontology rule...");
					continue;
				}
			}
			
			// send rule to driver
			driver.process(rule);
			// print rule information
			printInfo(rule);
		}
		
		// make CSVs
		driver.buildCSV();

	}

	/**
	 * @param rule
	 */
	private static void printInfo(Rule rule) {
		String str = "";
		for(ByteString[] bs : rule.getBody()) {
			String bstr = "";
			for(ByteString b : bs)
				bstr += b + ",";
			str += bstr + " | ";
		}
		logger.trace(rule.getHeadRelation() + "\t" + str + "\t" + rule.getPcaConfidence());		
	}

	/**
	 * @param headRelation
	 * @return
	 */
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

class ValueComparator implements Comparator<String> {
	
    Map<String, Double> base;

    public ValueComparator(Map<String, Double> base) {
        this.base = base;
    }

    // Note: this comparator imposes orderings that are inconsistent with
    // equals.
    public int compare(String a, String b) {
        if (base.get(a) >= base.get(b)) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys
    }
    
}
