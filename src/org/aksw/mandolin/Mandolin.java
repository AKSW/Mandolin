package org.aksw.mandolin;

import java.io.File;

import org.aksw.mandolin.amie.RDFToTSV;
import org.aksw.mandolin.amie.RuleMiner;
import org.aksw.mandolin.grounding.Grounding;
import org.aksw.mandolin.inference.ProbKBToRockitGibbsSampling;
import org.aksw.mandolin.model.PredictionLiteral;
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
	private String[] INPUT_PATHS = new String[] { 
			"datasets/DBLPL3S-100.nt",
			"datasets/LinkedACM-100.nt", 
			"linksets/DBLPL3S-LinkedACM-100.nt",
	};
	private String BASE = "eval/0001";
	private String AIM_RELATION = OWL.sameAs.getURI();

	// thresholds for similarity joins among datatype values
	private int THR_MIN = 80;
	private int THR_MAX = 90;
	private int THR_STEP = 10;
	
	private boolean ENABLE_FWC = true;

	// -------------------------------------------------------------------------

	private NameMapper map;

	public Mandolin() {

		map = new NameMapper(AIM_RELATION);

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
		this.BASE = base;
		this.AIM_RELATION = aim;

		INPUT_PATHS = input.split(",");
	}

	/**
	 * @throws Exception
	 */
	private void run() throws Exception {

		System.out.println("Mandolin started!");

		// create working directory
		new File(BASE).mkdirs();
		
//		// inputs -> model.nt
		Validator.run(BASE, INPUT_PATHS, ENABLE_FWC);
		if(ENABLE_FWC) {
			// model.nt -> model-fwc.nt
			PelletReasoner.run(BASE);
		}
		
		// model-fwc.nt -> map (classes)
		Classes.build(map, BASE);
		// model-fwc.nt -> map (other)
		Evidence.build(map, BASE, THR_MIN, THR_MAX, THR_STEP);

		map.pretty();

		System.out.println("# entClasses: " + map.getEntClasses().size());
		System.out.println("# relClasses: " + map.getRelClasses().size());
		System.out.println("# relationships: " + map.getRelationships().size());

		// map -> KB description csv
		ProbKBData.buildCSV(map, BASE);

		// model-fwc.nt -> model.tsv
		RDFToTSV.run(BASE);
		// model.tsv -> MLN csv
		RuleMiner.run(map, BASE);

		// csv -> Postgre factors
		Grounding.ground(BASE);

		// Postgre factors -> predictions
		PredictionSet pset = new ProbKBToRockitGibbsSampling(map).infer();

		eval(pset);

		System.out.println("Mandolin done.");

	}

	/**
	 * XXX Temporary routine for tracing results...
	 * 
	 * @param pset
	 */
	private void eval(PredictionSet pset) {

		// TreeSet<String> rel = map.getRelationships();
		String aimName = map.getAimName();

		System.out.println("+++ INFERRED +++");
		for (PredictionLiteral lit : pset) {
			// filter only aim relation from pset
			if (!lit.getP().equals(aimName))
				continue;
			if (lit.getProb() > 0.9) {
				System.out.println(lit);
				System.out.println("=> " + map.getURI(lit.getX()));
				System.out.println("   " + map.getURI(lit.getP()));
				System.out.println("   " + map.getURI(lit.getY()));
				// if(!rel.contains(lit.getP()+"#"+lit.getX()+"#"+lit.getY()) &&
				// !lit.getP().equals("RELATION15")) {
				// System.out.println("   PREDICTION");
				// }
				System.out.println();
			}
		}
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
