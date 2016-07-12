package org.aksw.mandolin.rulemining;

import java.util.ArrayList;
import java.util.List;

import javatools.administrative.Announce;
import javatools.parsers.NumberFormatter;
import amie.mining.AMIE;
import amie.rules.Rule;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class AmieHandler {
	
	public static enum MiningStrategy {
		HEAD_COVERAGE, SUPPORT;
	}

	private String ontology;
	private List<Rule> rules = null;

	public AmieHandler(String ontology) {
		super();
		this.ontology = ontology;
	}

	public void run(MiningStrategy ms) throws Exception {

		AMIE miner;
		switch(ms) {
		case HEAD_COVERAGE:
			miner =	AMIE.getInstance(new String[] { ontology });
			break;
		case SUPPORT:
			miner =	AMIE.getInstance(new String[] { ontology, "-pm", "support", "-mins", "0" });
			break;
		default:
			throw new RuntimeException("MiningStrategy does not exist: " + ms.name());
		}
		
		Announce.doing("Starting the mining phase");

		long time = System.currentTimeMillis();

		rules = miner.mine();

		if (!miner.isRealTime()) {
			Rule.printRuleHeaders();
			for (Rule rule : rules) {
				System.out.println(rule.getFullRuleString());
			}
		}

		long miningTime = System.currentTimeMillis() - time;
		System.out.println("Mining done in "
				+ NumberFormatter.formatMS(miningTime));
		System.out.println(rules.size() + " rules mined.");

	}

	public List<Rule> getRules() {
		return rules;
	}

}
