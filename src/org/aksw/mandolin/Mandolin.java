package org.aksw.mandolin;

import java.io.File;

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
	
	private static final int THETA_MIN = 1;
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

	// -------------------------------------------------------------------------

	private NameMapper map;
	
	private boolean isVerbose = true;
	
	/**
	 * Use demo values.
	 */
	public Mandolin() {
		super();

		this.workspace = "eval/0001";
		this.inputPaths = new String[] { 
				"datasets/AKSW-one-out.nt",
//				"datasets/DBLPL3S-10-one-out.nt",
//				"datasets/LinkedACM-10.nt", 
//				"linksets/DBLPL3S-LinkedACM-10.nt",
		};
//		this.aimRelation = OWL.sameAs.getURI();
		this.aimRelation = "http://mandolin.aksw.org/example/topic";
		this.thrMin = 95;
		this.thrStep = 10;
		this.thrMax = 95;
		this.enableOnt = false;
		this.enableFwc = false;
		this.enableSim = true;

		map = new NameMapper(aimRelation);
		
	}

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

		System.out.println("Mandolin started!");
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

		if(isVerbose)
			map.pretty();

		System.out.println("# entClasses: " + map.getEntClasses().size());
		System.out.println("# relClasses: " + map.getRelClasses().size());
		System.out.println("# relationships: " + map.getRelationships().size());

		// map -> KB description csv
		ProbKBData.buildCSV(map, workspace);

		// model-fwc.nt -> model.tsv
		RDFToTSV.run(workspace);
		// model.tsv -> MLN csv
		RuleMiner.run(map, workspace);

		// csv -> Postgre factors
		Grounding.ground(workspace);

		// Postgre factors -> predictions
		PredictionSet pset = new ProbKBToRockitGibbsSampling(map).infer();

		pset.saveTo(workspace + "/predictions.dat");
		
		for(int th=THETA_MIN; th<=THETA_MAX; th+=1) {
			double theta = th / 10.0;
			System.out.println("\ntheta = "+theta);
			pset.saveLinkset(map, theta, workspace + "/output_" + theta + ".nt");
		}

		System.out.println("Mandolin done.");

	}

	/**
	 * 
	 */
	private void printInfo() {
		System.out.println("BASE = "+workspace);
		System.out.println("INPUT_PATHS:");
		for(String ip : inputPaths)
			System.out.println("\t" + ip);
		System.out.println("ONTO_IMPORT = "+enableOnt);
		System.out.println("FORWARD_CHAIN = "+enableFwc);
		System.out.println("THR = [min="+thrMin+", step="+thrStep+", max="+thrMax+"]");
		System.out.println();
	}


	public NameMapper getMap() {
		return map;
	}
	
	public void setVerbose(boolean v) {
		this.isVerbose = v;
	}
	
	public boolean isVerbose() {
		return this.isVerbose;
	}

	public static void main(String[] args) throws Exception {

		try {
			
			new Mandolin().run();
			
		// TODO handle all exceptions
		} catch (PostgreNotStartedException e) {
			logger.fatal("Mandolin exited with errors (-1).");
		}

	}
}
