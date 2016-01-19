package org.aksw.mandolin;

import java.io.File;

import org.aksw.mandolin.amie.RDFToTSV;
import org.aksw.mandolin.amie.RuleMiner;
import org.aksw.mandolin.grounding.Grounding;
import org.aksw.mandolin.inference.ProbKBToRockitGibbsSampling;
import org.aksw.mandolin.model.PredictionSet;
import org.aksw.mandolin.reasoner.PelletReasoner;

import com.hp.hpl.jena.vocabulary.OWL;

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

	// input datasets
	private String[] inputPaths = new String[] { 
			"datasets/DBLPL3S-100.nt",
			"datasets/LinkedACM-100.nt", 
			"linksets/DBLPL3S-LinkedACM-100.nt",
	};
	private String workspace = "eval/0001";
	private String aimRelation = OWL.sameAs.getURI();

	// thresholds for similarity joins among datatype values
	private int thrMin = 80;
	private int thrMax = 90;
	private int thrStep = 10;
	
	/**
	 * Enable ontology import.
	 */
	private boolean enableOnt = true;
	
	/**
	 * Enable forward chain.
	 */
	private boolean enableFwc = true;

	// -------------------------------------------------------------------------

	private NameMapper map;
	
	/**
	 * Use demo values.
	 */
	public Mandolin() {
		super();
		
		map = new NameMapper(aimRelation);
		
	}

	/**
	 * @param workspace
	 * @param inputPaths
	 * @param aimRelation
	 * @param thrMin
	 * @param thrStep
	 * @param thrMax
	 * @param enableOnt
	 * @param enableFwc
	 */
	public Mandolin(String workspace, String[] inputPaths, String aimRelation, int thrMin, int thrStep, int thrMax, boolean enableOnt, boolean enableFwc) {
		super();
		
		this.workspace = workspace;
		this.inputPaths = inputPaths;
		this.aimRelation = aimRelation;
		this.thrMin = thrMin;
		this.thrStep = thrStep;
		this.thrMax = thrMax;
		this.enableOnt = enableOnt;
		this.enableFwc = enableFwc;

		map = new NameMapper(aimRelation);

	}

	/**
	 * @param input
	 *            comma-separated paths
	 * @param base
	 *            workspace path
	 * @param aim
	 *            aim relation URI
	 */
	public Mandolin(String input, String base, String aim) {
		this.workspace = base;
		this.aimRelation = aim;

		inputPaths = input.split(",");
	}

	/**
	 * @throws Exception
	 */
	private void run() throws Exception {

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
		Evidence.build(map, workspace, thrMin, thrMax, thrStep);

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

//		eval(pset);
		pset.saveTo(workspace + "/predictions.dat");
		
		for(int th=0; th<=10; th+=1) {
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

	public static void main(String[] args) throws Exception {

		if (args.length > 0) {
			// set values
			new Mandolin(args[0], // paths, comma-separated
					args[1], // base workspace
					args[2] // aim relation URI
			).run();
		} else {
			// default values
			new Mandolin().run();
		}

	}
}
