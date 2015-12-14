package org.aksw.mandolin.eval;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.aksw.mandolin.Classes;
import org.aksw.mandolin.Evidence;
import org.aksw.mandolin.NameMapperProbKB;
import org.aksw.mandolin.NameMapperProbKB.Type;

import com.hp.hpl.jena.vocabulary.OWL;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class Evaluation {

	// input datasets
	public static final String SRC_PATH = "datasets/DBLPL3S-rdfs.nt"; // TODO
																		// revert
																		// to
																		// DBLPL3S.nt
	public static final String TGT_PATH = "datasets/LinkedACM.nt";
	public static final String LINKSET_PATH = "linksets/DBLPL3S-LinkedACM.nt";
	public static final String GOLD_STANDARD_PATH = "linksets/DBLPL3S-LinkedACM-GoldStandard.nt";

	public static final String BASE = "eval/10_publi-probkb";

	// thresholds for similarity joins among datatype values
	private static final int THR_MIN = 80;
	private static final int THR_MAX = 90;
	private static final int THR_STEP = 10;

	private static NameMapperProbKB map = new NameMapperProbKB(OWL.sameAs.getURI());

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		new File(BASE).mkdirs();

		Classes.build(map, SRC_PATH, TGT_PATH);
		Evidence.build(map, SRC_PATH, TGT_PATH, GOLD_STANDARD_PATH, THR_MIN,
				THR_MAX, THR_STEP);

		PostgreDB db = new PostgreDB();
		db.connect();

//		db.factors(100.0);

		ResultSet rs = null;

		int tp = 0, fp = 0;

//		while ((rs = db.next()) != null) {
//
//			// TODO
//			if (evaluate(rs))
//				tp++;
//			else
//				fp++;
//
//		}

		System.out.println("TP = " + tp);
		System.out.println("FP = " + fp);

	}

	private static boolean evaluate(ResultSet rs) {
		// TODO Auto-generated method stub

		try {
			Integer id1 = rs.getInt("id1");
			Integer id2 = rs.getInt("id2");
			Integer id3 = rs.getInt("id3");
			Double weight = rs.getDouble("weight");

			System.out.println("Evaluating:"
					+ "\n\tS: " + map.getURI(Type.ENTITY.toString() + id1) + ", "
					+ "\n\tP: " + map.getURI(Type.ENTITY.toString() + id2) + ", "
					+ "\n\tO: " + map.getURI(Type.ENTITY.toString() + id3) + ", "
					+ "\n\tw: " + weight);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

}
