package org.aksw.mandolin.rulemining;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javatools.parsers.NumberFormatter;
import amie.mining.AMIE;
import amie.rules.Rule;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class AmieHandler {
	
	private final static Logger logger = LogManager.getLogger(AmieHandler.class);
	
	public static enum MiningStrategy {
		HEAD_COVERAGE, SUPPORT;
	}

	private String ontology;
	private List<Rule> rules = null;
	private Double miningThr = 0.01;

	public AmieHandler(String ontology) {
		super();
		this.ontology = ontology;
	}

	public void run(MiningStrategy ms) throws Exception {

		AMIE miner;
		switch(ms) {
		case HEAD_COVERAGE:
			miner =	AMIE.getInstance(new String[] { ontology, "-minhc", String.valueOf(miningThr) });
			break;
		case SUPPORT:
			miner =	AMIE.getInstance(new String[] { ontology, "-pm", "support", "-mins", "0" });
			break;
		default:
			throw new RuntimeException("MiningStrategy does not exist: " + ms.name());
		}
		
		logger.info("Starting the mining phase");

		long time = System.currentTimeMillis();

		rules = miner.mine();

		if (!miner.isRealTime()) {
			Rule.printRuleHeaders();
			for (Rule rule : rules) {
				logger.info(rule.getFullRuleString());
			}
		}

		long miningTime = System.currentTimeMillis() - time;
		logger.info("Mining done in "
				+ NumberFormatter.formatMS(miningTime));
		logger.info(rules.size() + " rules mined.");

	}

	public List<Rule> getRules() {
		return rules;
	}

	public void setMiningThr(Double mining) {
		this.miningThr = mining;
	}

}
