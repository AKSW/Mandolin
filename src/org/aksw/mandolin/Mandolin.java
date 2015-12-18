package org.aksw.mandolin;

import java.io.File;

import org.aksw.mandolin.amie.RDFToTSV;
import org.aksw.mandolin.amie.RuleMiner;
import org.aksw.mandolin.grounding.Grounding;
import org.aksw.mandolin.inference.ProbKBToRockitGibbsSampling;
import org.aksw.mandolin.model.PredictionLiteral;
import org.aksw.mandolin.model.PredictionSet;

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
	private String SRC_PATH = "datasets/DBLPL3S.nt";
	private String TGT_PATH = "datasets/LinkedACM.nt";
	private String LINKSET_PATH = "linksets/DBLPL3S-LinkedACM-closure.nt";

	private String BASE = "eval/0001";

	// thresholds for similarity joins among datatype values
	private int THR_MIN = 80;
	private int THR_MAX = 90;
	private int THR_STEP = 10;

	// TODO this is a temporary constant which should become variable like the
	// above...
	private String AIM_RELATION = OWL.sameAs.getURI();

	// -------------------------------------------------------------------------

	private String TEMP_OUTPUT = "tmp/temp_" + ((int) Math.random() * 100000)
			+ ".tsv";

	private NameMapper map;

	public Mandolin() {

		map = new NameMapper(AIM_RELATION);

	}

	public Mandolin(String src, String tgt, String lnk, String base, String aim) {
		this.SRC_PATH = src;
		this.TGT_PATH = tgt;
		this.LINKSET_PATH = lnk;
		this.BASE = base;
		this.AIM_RELATION = aim;
	}

	/**
	 * @throws Exception
	 */
	private void run() throws Exception {

		System.out.println("Mandolin started!");

		// create working directory
		new File(BASE).mkdirs();

		Classes.build(map, SRC_PATH, TGT_PATH);
		Evidence.build(map, SRC_PATH, TGT_PATH, LINKSET_PATH, THR_MIN, THR_MAX,
				THR_STEP);

		map.pretty();

		System.out.println("# entClasses: " + map.getEntClasses().size());
		System.out.println("# relClasses: " + map.getRelClasses().size());
		System.out.println("# relationships: " + map.getRelationships().size());

		ProbKBData.buildCSV(map, BASE);

		RDFToTSV.run(TEMP_OUTPUT, SRC_PATH, TGT_PATH, LINKSET_PATH);
		RuleMiner.run(map, BASE, TEMP_OUTPUT);

		Grounding.ground(BASE);

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
			new Mandolin(args[0], // source path
					args[1], // target path (could be an empty dataset)
					args[2], // linkset path (could be an empty dataset)
					args[3], // base workspace
					args[4] // aim relation URI
			).run();
		} else {
			// default values
			new Mandolin().run();
		}

	}
}
