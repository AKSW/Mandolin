package mlnf.inference;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import mlnf.controller.NameMapper;
import mlnf.model.PredictionLiteral;
import mlnf.model.PredictionSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
 * @author author <email>
 *
 */
public abstract class RockitGibbsSampling {

	private final static Logger logger = LogManager.getLogger(RockitGibbsSampling.class);
	
	protected static SyntaxReader reader;
	
	protected NameMapper map;

	// Sampling only
	/**
	 * The maximum number of iterations for sampling.
	 */
	public static final int MAX_ITERATIONS = 10000000;
	
	protected GIBBSSampler gibbsSampler;

	protected RockitGibbsSampling(NameMapper map) {
		super();
		
		this.map = map;
		
		try {
			Parameters.readPropertyFile();
		} catch (ReadOrWriteToFileException e) {
			logger.error(e.getMessage());
		}
		Parameters.USE_CUTTING_PLANE_AGGREGATION = false;
		Parameters.USE_CUTTING_PLANE_INFERENCE = false;
		reader = new SyntaxReader();
	
	}

	public abstract PredictionSet infer(Integer samples);

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
			ArrayList<Clause> clauses, Collection<Literal> evidence, Integer sampling)
			throws SQLException, SolveException, ParseException {

		PredictionSet ps = new PredictionSet(map.getAim());

		gibbsSampler = new GIBBSSampler();
		int iter = iterations(clauses.size() + evidence.size(), sampling);
		ArrayList<GIBBSLiteral> gibbsOutput = gibbsSampler.sample(iter,
				clauses, evidence, consistentStartingPoints);

		for (GIBBSLiteral l : gibbsOutput)
			ps.add(new PredictionLiteral(l, iter));

		return ps;
	}

	/**
	 * Get number of iterations.
	 * @param sampling 
	 * 
	 * @param i
	 * @return
	 */
	private int iterations(int literals, Integer sampling) {
		
		
		int iterations;
		
		long iter = (long) literals * 1000;
		
		if(sampling != null) // pre-assigned 
			iterations = sampling;
		else if(iter >= Integer.MAX_VALUE) // overflow
			iterations = MAX_ITERATIONS;
		else if(iter >= MAX_ITERATIONS) // not overflow, but still too high
			iterations = MAX_ITERATIONS;
		else
			iterations = (int) iter; // acceptable value
		
		logger.info("literals={}, supposed_iter={}, actual_iter={}", literals, iter, iterations);
		return iterations;
		
	}

}
