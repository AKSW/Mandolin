package org.aksw.mandolin.rockit;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.googlecode.rockit.app.Parameters;
import com.googlecode.rockit.app.sampler.gibbs.GIBBSLiteral;
import com.googlecode.rockit.app.sampler.gibbs.GIBBSSampler;
import com.googlecode.rockit.app.solver.StandardSolver;
import com.googlecode.rockit.app.solver.exact.ExactSolver;
import com.googlecode.rockit.app.solver.exact.GroundingSet;
import com.googlecode.rockit.app.solver.pojo.Clause;
import com.googlecode.rockit.app.solver.pojo.Literal;
import com.googlecode.rockit.exception.ParseException;
import com.googlecode.rockit.exception.ReadOrWriteToFileException;
import com.googlecode.rockit.exception.SolveException;
import com.googlecode.rockit.javaAPI.HerbrandUniverse;
import com.googlecode.rockit.javaAPI.Model;
import com.googlecode.rockit.parser.SyntaxReader;

/**
 * @author Bernd Opitz <jopitz@mail.uni-mannheim.de>
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class LaunchInference {

	// Common
	private HerbrandUniverse u = HerbrandUniverse.getInstance();

	/**
	 * MLN file.
	 */
	private String input = "eval/11_publi-mln/prog.mln";

	/**
	 * DB file.
	 */
	private String groundings = "eval/11_publi-mln/evidence.db";

	private static SyntaxReader reader;
	private Model model;
	/**
	 * This parameter controls by how much the probabilities between exact
	 * inference and sampling may diverge before we consider them different.
	 */
	private static final double delta = 0.005;

	// Exact only
	private ExactSolver exactSolver;

	// Sampling only
	/**
	 * The number of iterations for sampling.
	 */
	private int iterations = 5000000;
	private GIBBSSampler gibbsSampler;

	public static void main(String[] args) throws ParseException, IOException,
			SolveException, SQLException, ReadOrWriteToFileException {

		new LaunchInference().run();

	}

	public LaunchInference() throws ReadOrWriteToFileException, ParseException,
			IOException {

		Parameters.readPropertyFile();
		Parameters.USE_CUTTING_PLANE_AGGREGATION = false;
		Parameters.USE_CUTTING_PLANE_INFERENCE = false;
		reader = new SyntaxReader();

		model = reader.getModel(input, groundings);

	}

	public void run() throws ParseException, IOException, SolveException,
			SQLException {

		exactSolver = new ExactSolver(model);
		Map<GroundingSet, Double> exactInferenceResult = exactSolver.solve();
		HashMap<String, Double> exact_aggregatedResult = exactSolver
				.aggregate(exactInferenceResult);
		System.out.println("Input: " + this.input);
		// System.out.println(exactSolver.getDistribution(exactInferenceResult,
		// true));

		// Sampling
		// Model model = reader.getModel(this.input.toString(),
		// this.groundings.toString());
		StandardSolver solver = new StandardSolver(model);
		gibbsSampler = new GIBBSSampler();
		// ground MLN and retrieve Clauses
		ArrayList<String> consistentStartingPoints = solver.solve();
		ArrayList<Clause> clauses = solver.getAllClauses();
		Collection<Literal> evidence = solver.getEvidenceAxioms();
		solver = null; // free memory
		ArrayList<GIBBSLiteral> gibbsOutput = gibbsSampler.sample(iterations,
				clauses, evidence, consistentStartingPoints);

		HashMap<String, Double> gibbs_result = new HashMap<String, Double>();
		for (GIBBSLiteral l : gibbsOutput) {
			gibbs_result.put(l.getName(), l.return_my_probability(iterations));
		}

		for (Entry<String, Double> e : exact_aggregatedResult.entrySet()) {
			Double exactValue = e.getValue();
			Double gibbsValue = gibbs_result.get(e.getKey());

			System.out.println("gibbsValue = " + gibbsValue);

			// This assumes that the Gibbs sampling is correct, but we do not
			// know whether the exact inference is. Assumption does not affect
			// the result.
			System.out.println(u.transformKeysToConstants(e.getKey())
					+ ": [Gibbs " + gibbsValue + " ] vs. [" + exactValue
					+ " Exact] | delta = " + delta);

		}

	}

}
