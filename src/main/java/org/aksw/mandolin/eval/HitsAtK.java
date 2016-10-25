package org.aksw.mandolin.eval;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDF;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * Benchmark evaluation (FB15K, WN18) using training + validation + test
 * datasets.
 * 
 * @deprecated Use LinkPredictionEvaluation.
 * 
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
@Deprecated()
public class HitsAtK {

	/**
	 * The 'K' value in Hits@K.
	 */
	protected static final int K = 10;
	
	private int minThr = 1;

	// protected static final boolean TEST_OUTPUTS = false;

	private String testSet, output;

	public HitsAtK(String testSet, String output) {
		super();
		this.testSet = testSet;
		this.output = output;
		System.out.println("Benchmark bound.");
	}

	public static void main(String[] args) throws FileNotFoundException {

		String testSet = args[0];
		String output = args[1];

		HitsAtK b = new HitsAtK(testSet, output);
		
		b.start();
		
	}
	
	public void start() throws FileNotFoundException {

		DecimalFormat df = new DecimalFormat("0.0");

		PrintWriter pw = new PrintWriter(new File(output.substring(output
				.lastIndexOf('/') + 1) + "_hits_at_10.csv"));
		pw.println("threshold\thitsMax\thitsMin");
		for (int i = minThr; i <= 10; i++) {
			String thr = String.valueOf(df.format((double) i / 10.0));
			Cache c = run("/discovered_" + thr
					+ ".nt");
			pw.println(thr+"\t"+c.hitsMax+"\t"+c.hitsMin);
		}
		pw.close();

		// if (TEST_OUTPUTS)
		// for (int i = 1; i <= 10; i++)
		// b.run("/output_" + String.valueOf(df.format((double) i / 10.0))
		// + ".nt");

	}

	public int getMinThr() {
		return minThr;
	}

	public void setMinThr(int minThr) {
		this.minThr = minThr;
	}

	private Cache run(String format) {

		String outfile = output + format;

		System.out.print("\nLoading " + outfile + "...");
		final Model out = RDFDataMgr.loadModel(outfile, Lang.NTRIPLES);
		System.out.println(" Done.");

		final Cache c = new Cache();

		StreamRDF dataStream = new StreamRDF() {

			@Override
			public void start() {
			}

			@Override
			public void triple(Triple triple) {

				check(triple, c, true);
				check(triple, c, false);

			}

			private void check(Triple triple, Cache c, boolean forward) {

				Resource s = ResourceFactory.createResource(triple.getSubject()
						.getURI());
				Property p = ResourceFactory.createProperty(triple
						.getPredicate().getURI());
				Resource o = ResourceFactory.createResource(triple.getObject()
						.getURI());

				boolean morethanK = false, hit = false;
				ExtendedIterator<? extends RDFNode> it = forward ? out
						.listObjectsOfProperty(s, p) : out
						.listSubjectsWithProperty(p, o);
				for (int i = 0; it.hasNext(); i++) {
					Resource res = it.next().asResource();
					String uri = forward ? triple.getObject().getURI() : triple
							.getSubject().getURI();
					if (res.getURI().equals(uri)) {
						c.hits++;
						hit = true;
					}
					if (i > K)
						morethanK = true;
				}

				if (morethanK && hit)
					c.hitsMoreThanK++;

				c.triples++;

			}

			@Override
			public void quad(Quad quad) {
			}

			@Override
			public void base(String base) {
			}

			@Override
			public void prefix(String prefix, String iri) {
			}

			@Override
			public void finish() {
			}

		};

		System.out.print("Streaming " + testSet + "...");
		RDFDataMgr.parse(dataStream, testSet);
		System.out.println(" Done.");

		double hitsMax = (double) c.hits * 100d / (double) c.triples;
		double hitsMin = (double) (c.hits - c.hitsMoreThanK) * 100d
				/ (double) c.triples;

		System.out.println("====== EVAL ======");
		System.out.println("test set size: " + c.triples);
		System.out.printf("Hits@%d_max = %.2f%n", K, hitsMax);
		System.out.printf("Hits@%d_min = %.2f%n", K, hitsMin);

		c.hitsMax = hitsMax;
		c.hitsMin = hitsMin;

		return c;

	}

}

class Cache {
	int hits = 0;
	int hitsMoreThanK = 0;
	int triples = 0;

	double hitsMax;
	double hitsMin;
}