package org.aksw.mandolin;

import java.io.File;

import org.aksw.mandolin.amie.RDFToTSV;
import org.aksw.mandolin.amie.RuleMiner;
import org.aksw.mandolin.inference.ProbKBToRockitGibbsSampling;
import org.aksw.mandolin.inference.RockitGroundingAndGibbsSampling;
import org.aksw.mandolin.model.PredictionLiteral;
import org.aksw.mandolin.model.PredictionSet;

import com.hp.hpl.jena.vocabulary.OWL;

/**
 * Mandolin for the ProbKB framework, i.e. CSV file build.
 * 
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class MandolinProbKB {

	// input datasets
	public static final String SRC_PATH = "datasets/DBLPL3S.nt"; 
	public static final String TGT_PATH = "datasets/LinkedACM.nt";
	public static final String LINKSET_PATH = "linksets/DBLPL3S-LinkedACM.nt";
	public static final String GOLD_STANDARD_PATH = "linksets/DBLPL3S-LinkedACM-GoldStandard.nt";

	public static final String BASE = "eval/0001";

	public static final int TRAINING_SIZE = Integer.MAX_VALUE; // TODO restore:
																// (int) (47 *
																// 0.9);
	
	// thresholds for similarity joins among datatype values
	private static final int THR_MIN = 80;
	private static final int THR_MAX = 90;
	private static final int THR_STEP = 10;
	
	// TODO this is a temporary constant which should become variable like the above...
	private static final String AIM_RELATION = OWL.sameAs.getURI();
	
	// -------------------------------------------------------------------------
	
	// ProbKB files
	public static final String CLASSES_CSV = BASE + "/classes.csv";
	public static final String ENTCLASSES_CSV = BASE + "/entClasses.csv";
	public static final String ENTITIES_CSV = BASE + "/entities.csv";
	public static final String FUNCTIONALS_CSV = BASE + "/functionals.csv";
	public static final String RELATIONS_CSV = BASE + "/relations.csv";
	public static final String RELATIONSHIPS_CSV = BASE + "/relationships.csv";
	public static final String RELCLASSES_CSV = BASE + "/relClasses.csv";

	private static final String TEMP_OUTPUT = "tmp/DBLPACM.tsv";

	private NameMapperProbKB map;

	public MandolinProbKB() {

		map = new NameMapperProbKB();

	}

	/**
	 * @throws Exception 
	 */
	private void run() throws Exception {
		
		System.out.println("Mandolin started!");

		// create working directory
		new File(BASE).mkdirs();
		
		Classes.build(map, SRC_PATH, TGT_PATH);
		Evidence.build(map, SRC_PATH, TGT_PATH, GOLD_STANDARD_PATH, THR_MIN, THR_MAX, THR_STEP);
		
		map.pretty();
		
		System.out.println("# entClasses: "+map.getEntClasses().size());
		System.out.println("# relClasses: "+map.getRelClasses().size());
		System.out.println("# relationships: "+map.getRelationships().size());
		
		ProbKBData.buildCSV(map, BASE);
		
		RDFToTSV.run(map, BASE, TEMP_OUTPUT);
		RuleMiner.run(map, BASE, TEMP_OUTPUT);
		
		// TODO update SQL file dynamically and launch grounding from PGSQL console
		
		// TODO launch inference
		PredictionSet pset = new ProbKBToRockitGibbsSampling(AIM_RELATION).infer();
//		PredictionSet pset = new RockitGroundingAndGibbsSampling(AIM_RELATION, "eval/11_publi-mln/prog.mln",
//				"eval/11_publi-mln/evidence.db").infer();
		System.out.println("+++ INFERRED +++");
		for(PredictionLiteral lit : pset)
			System.out.println(lit);
		
		System.out.println("Mandolin done.");
		
	}
	

	public NameMapperProbKB getMap() {
		return map;
	}

	public static void main(String[] args) throws Exception {

		new MandolinProbKB().run();

	}

}
