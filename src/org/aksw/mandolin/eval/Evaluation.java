package org.aksw.mandolin.eval;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.aksw.mandolin.Classes;
import org.aksw.mandolin.Evidence;
import org.aksw.mandolin.NameMapper;
import org.aksw.mandolin.NameMapper.Type;
import org.aksw.mandolin.inference.PostgreDB;

import com.hp.hpl.jena.vocabulary.OWL;

/**
 * XXX The gold standard path (links to be predicted) is here, not in Mandolin.
 * 
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class Evaluation {

	// input datasets
	public static final String SRC_PATH = "datasets/DBLPL3S.nt";
	public static final String TGT_PATH = "datasets/LinkedACM.nt";
	public static final String LINKSET_PATH = "linksets/DBLPL3S-LinkedACM.nt";
	public static final String GOLD_STANDARD_PATH = "linksets/DBLPL3S-LinkedACM-GoldStandard.nt";

	public static final String BASE = "eval/10_publi-probkb";

	// thresholds for similarity joins among datatype values
	private static final int THR_MIN = 80;
	private static final int THR_MAX = 90;
	private static final int THR_STEP = 10;
	
	// TODO
	private int TRAINING_SIZE = Integer.MAX_VALUE;

	private static NameMapper map = new NameMapper(OWL.sameAs.getURI());

	/**
	 * @param args
	 */
	public static void main(String[] args) {


		int tp = 0, fp = 0;

//			TODO
//			if (evaluate(rs))
//				tp++;
//			else
//				fp++;

		System.out.println("TP = " + tp);
		System.out.println("FP = " + fp);

	}

}
