package org.aksw.mandolin;

import java.io.File;
import java.util.Arrays;
import java.util.Scanner;

import org.aksw.mandolin.controller.Classes;
import org.aksw.mandolin.controller.Evidence;
import org.aksw.mandolin.controller.NameMapper;
import org.aksw.mandolin.controller.OntoImporter;
import org.aksw.mandolin.controller.ProbKBData;
import org.aksw.mandolin.controller.Validator;
import org.aksw.mandolin.grounding.Grounding;
import org.aksw.mandolin.inference.ProbKBToRockitGibbsSampling;
import org.aksw.mandolin.model.PredictionSet;
import org.aksw.mandolin.reasoner.PelletReasoner;
import org.aksw.mandolin.rulemining.RDFToTSV;
import org.aksw.mandolin.rulemining.RuleMiner;
import org.aksw.mandolin.util.PostgreNotStartedException;
import org.aksw.mandolin.util.SetUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The final pipeline for MANDOLIN, a scalable join of several
 * statistical-relational-learning algorithms to predict RDF links of any type
 * (i.e., triples) in one or more RDF datasets using rule mining of Horn
 * clauses, Markov Logic Networks, and Gibbs Sampling.
 * 
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class Mandolin {

	private final static Logger logger = LogManager.getLogger(Mandolin.class);
	
	private static final int THETA_MIN = 0;
	private static final int THETA_MAX = 10;
	// input datasets
	private String[] inputPaths;
	private String workspace;
	private String aimRelation;

	// thresholds for similarity joins among datatype values
	private int thrMin;
	private int thrMax;
	private int thrStep;
	
	/**
	 * Enable ontology import.
	 */
	private boolean enableOnt;
	
	/**
	 * Enable forward chain.
	 */
	private boolean enableFwc;
	
	/**
	 * Enable similarity graph enrichment.
	 */
	private boolean enableSim;

	/*
	 * Threshold for head-coverage method in rule mining. If null, support method is used.
	 */
	private Double mining;
	
	/**
	 * Maximum number of rules to mine.
	 */
	private Integer maxRules;

	// -------------------------------------------------------------------------

	private NameMapper map;

	/**
	 * Iteration for the Gibbs sampling.
	 */
	private Integer sampling;
	
	/**
	 * @param workspace workspace path
	 * @param csInputPaths comma-separated input paths
	 * @param aimRelation aim relation URI
	 * @param thrMin
	 * @param thrStep
	 * @param thrMax
	 * @param enableOnt
	 * @param enableFwc
	 * @param enableSim
	 */
	public Mandolin(String workspace, String csInputPaths, String aimRelation, int thrMin, int thrStep, int thrMax, boolean enableOnt, boolean enableFwc, boolean enableSim) {
		super();
		
		this.workspace = workspace;
		this.inputPaths = csInputPaths.split(",");
		this.aimRelation = aimRelation;
		this.thrMin = thrMin;
		this.thrStep = thrStep;
		this.thrMax = thrMax;
		this.enableOnt = enableOnt;
		this.enableFwc = enableFwc;
		this.enableSim = enableSim;

		map = new NameMapper(aimRelation);

	}

	/**
	 * @throws Exception
	 */
	public void run() throws Exception {

		logger.info("Mandolin started!");
		printInfo();

		// create working directory
		new File(workspace).mkdirs();
		
		if(enableOnt) {
			// inputs -> model-tmp.nt
			OntoImporter.run(workspace, inputPaths);
		}
		
		// inputs (or model-tmp.nt) -> model.nt (or model-fwc.nt)
		Validator.run(workspace, inputPaths, enableFwc, enableOnt);
		if(enableFwc) {
			// model.nt -> model-fwc.nt
			PelletReasoner.run(workspace);
		}
		
		// model-fwc.nt -> map (classes)
		Classes.build(map, workspace);
		// model-fwc.nt -> map (other)
		if(enableSim)
			Evidence.build(map, workspace, thrMin, thrMax, thrStep);
		else
			Evidence.build(map, workspace);

		if(logger.isTraceEnabled())
			map.pretty();

		logger.info("# entClasses: " + map.getEntClasses().size());
		logger.info("# relClasses: " + map.getRelClasses().size());
		logger.info("# relationships: " + map.getRelationships().size());

		// map -> KB description csv
		ProbKBData.buildCSV(map, workspace);

		// model-fwc.nt -> model.tsv
		RDFToTSV.run(workspace);
		// model.tsv -> MLN csv
		RuleMiner.run(map, workspace, mining, maxRules);

		// csv -> Postgre factors
		Grounding.ground(workspace);

		// Postgre factors -> predictions
		PredictionSet pset = new ProbKBToRockitGibbsSampling(map).infer(sampling);

		pset.saveTo(workspace + "/predictions.dat");
		
		for(int th=THETA_MIN; th<=THETA_MAX; th+=1) {
			double theta = th / 10.0;
			logger.info("theta = "+theta);
			
			// get set of predicted (just outputted) links
			String knowledge = workspace + "/model-fwc.nt";
			String predicted = workspace + "/output_" + theta + ".nt";
			pset.saveLinkset(map, theta, predicted);
			
			// compute set of discovered (emergent) links
			String discovered = workspace + "/discovered_" + theta + ".nt";
			SetUtils.minus(predicted, knowledge, discovered);
			logger.debug("+++ DISCOVERED +++");
			Scanner in = new Scanner(new File(discovered));
			int size = 0;
			while(in.hasNextLine()) {
				logger.debug(in.nextLine());
				size++;
			}
			in.close();
			logger.info("Discovered triples size: "+size);
		}
		

		logger.info("Mandolin done.");

	}

	/**
	 * 
	 */
	private void printInfo() {
		logger.info("BASE = "+workspace);
		logger.info("INPUT_PATHS:");
		for(String ip : inputPaths)
			logger.info("\t" + ip);
		logger.info("AIM_RELATION = "+aimRelation);
		logger.info("ONTO_IMPORT = "+enableOnt);
		logger.info("FORWARD_CHAIN = "+enableFwc);
		logger.info("SIMILARITIES = "+enableSim);
		logger.info("THR = [min="+thrMin+", step="+thrStep+", max="+thrMax+"]");
		logger.info("MINING_THR = "+mining);
		logger.info("MAX_RULES = "+maxRules);
		logger.info("SAMPLING_ITER = "+sampling);
	}


	public NameMapper getMap() {
		return map;
	}
	
	public static void main(String[] args) throws Exception {
		
		logger.info("Mandolin initialized with args = {}", Arrays.toString(args));
		
		String output = null, input = null, aim = "false", rules = null, 
				onto = "false", fwc = "false", mining = null, sampling = null;
		String[] simVal = {"-1", "-1", "-1"};
		
		for(int i=0; i<args.length; i+=2) {
			switch(args[i]) {
			case "--output":
				output = args[i+1];
				break;
			case "--input":
				input = args[i+1];
				break;
			case "--aim":
				aim = args[i+1];
				break;
			case "--sim":
				simVal = args[i+1].split(",");
				break;
			case "--onto":
				onto = args[i+1];
				break;
			case "--fwc":
				fwc = args[i+1];
				break;
			case "--mining":
				mining = args[i+1];
				break;
			case "--rules":
				rules = args[i+1];
				break;
			case "--sampling":
				sampling = args[i+1];
				break;
			}
		}

		try {
			
			Mandolin m = new Mandolin(output, input, aim, Integer.parseInt(simVal[0]), 
					Integer.parseInt(simVal[1]), Integer.parseInt(simVal[2]), 
					Boolean.parseBoolean(onto), Boolean.parseBoolean(fwc),
					!simVal[0].equals("-1"));
			if(mining != null)
				m.setMining(mining);
			if(rules != null)
				m.setMaxRules(rules);
			if(sampling != null)
				m.setSampling(sampling);
			m.run();
			
		// TODO handle all exceptions
		} catch (PostgreNotStartedException e) {
			logger.fatal("Mandolin exited with errors (-1).");
			throw new RuntimeException();
		}

	}

	public void setMaxRules(String rules) {
		this.maxRules = Integer.parseInt(rules);
	}

	public void setSampling(String sampling) {
		this.sampling = Integer.parseInt(sampling);
	}

	public void setMining(String mining) {
		switch(mining) {
		case "support":
			this.mining = null;
			break;
		default:
			this.mining = Double.parseDouble(mining);
			break;
		}
	}
}
