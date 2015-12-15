package org.aksw.mandolin.inference;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.aksw.mandolin.NameMapper;
import org.aksw.mandolin.model.PredictionLiteral;
import org.aksw.mandolin.model.PredictionSet;

import com.googlecode.rockit.app.Parameters;
import com.googlecode.rockit.app.sampler.gibbs.GIBBSLiteral;
import com.googlecode.rockit.app.sampler.gibbs.GIBBSSampler;
import com.googlecode.rockit.app.solver.pojo.Clause;
import com.googlecode.rockit.app.solver.pojo.Literal;
import com.googlecode.rockit.exception.ParseException;
import com.googlecode.rockit.exception.ReadOrWriteToFileException;
import com.googlecode.rockit.exception.SolveException;
import com.googlecode.rockit.parser.SyntaxReader;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public abstract class RockitGibbsSampling {

	protected static SyntaxReader reader;
	
	protected NameMapper map;

	// Sampling only
	/**
	 * The number of iterations for sampling.
	 */
	public static final int ITERATIONS = 5000000;
	protected GIBBSSampler gibbsSampler;

	protected RockitGibbsSampling(NameMapper map) {
		super();
		
		this.map = map;
		
		try {
			Parameters.readPropertyFile();
		} catch (ReadOrWriteToFileException e) {
			e.printStackTrace();
		}
		Parameters.USE_CUTTING_PLANE_AGGREGATION = false;
		Parameters.USE_CUTTING_PLANE_INFERENCE = false;
		reader = new SyntaxReader();
	
	}

	public abstract PredictionSet infer();

	/**
	 * Gibbs Sampling by RockIt.
	 * 
	 * @param consistentStartingPoints
	 * @param clauses
	 * @param evidence
	 * @throws SQLException
	 * @throws SolveException
	 * @throws ParseException
	 */
	public PredictionSet gibbsSampling(
			ArrayList<String> consistentStartingPoints,
			ArrayList<Clause> clauses, Collection<Literal> evidence)
			throws SQLException, SolveException, ParseException {

		PredictionSet ps = new PredictionSet(map.getAim());

		gibbsSampler = new GIBBSSampler();
		ArrayList<GIBBSLiteral> gibbsOutput = gibbsSampler.sample(ITERATIONS,
				clauses, evidence, consistentStartingPoints);

		for (GIBBSLiteral l : gibbsOutput)
			ps.add(new PredictionLiteral(l));

		return ps;
	}

}
