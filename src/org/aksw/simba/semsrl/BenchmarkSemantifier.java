package org.aksw.simba.semsrl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.TreeSet;

import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

/**
 * As some datasets were created for an Entity Resolution benchmark (see <a
 * href="http://goo.gl/9ALII4">this link</a>), important semantic information
 * such as multiple objects and object properties were not kept within the CSV
 * format. This class builds two models starting from the perfect mapping.
 * SPARQL endpoints have to be specified.
 * 
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 */
public class BenchmarkSemantifier {

	String perfMapping, srcNamespace, tgtNamespace, srcEndpoint, tgtEndpoint,
			srcOut, tgtOut;

	private boolean hasHeader = true;

	private static final Logger LOGGER = Logger
			.getLogger(BenchmarkSemantifier.class);

	/**
	 * @param perfMapping
	 * @param srcNamespace
	 * @param tgtNamespace
	 * @param srcEndpoint
	 * @param tgtEndpoint
	 * @param srcOut
	 * @param tgtOut
	 */
	public BenchmarkSemantifier(String perfMapping, String srcNamespace,
			String tgtNamespace, String srcEndpoint, String tgtEndpoint,
			String srcOut, String tgtOut) {
		this.perfMapping = perfMapping;
		this.srcNamespace = srcNamespace;
		this.tgtNamespace = tgtNamespace;
		this.srcEndpoint = srcEndpoint;
		this.tgtEndpoint = tgtEndpoint;
		this.srcOut = srcOut;
		this.tgtOut = tgtOut;
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		final int ARG_SIZE = 7;

		if (args.length != ARG_SIZE) {
			LOGGER.warn("args[] size should be " + ARG_SIZE
					+ ". Using demo values...");
			String[] asd = {
				"mappings/dblp-acm.csv",
				"http://dblp.rkbexplorer.com/id/",
				"http://acm.rkbexplorer.com/id/",
				"http://dblp.rkbexplorer.com/sparql",
				"http://acm.rkbexplorer.com/sparql",
				"datasets/dblp.nt",
				"datasets/acm.nt",
			};
			args = asd;
		}

		BenchmarkSemantifier sem = new BenchmarkSemantifier(args[0], args[1], args[2],
				args[3], args[4], args[5], args[6]);
		sem.run();
	}

	/**
	 * @throws IOException
	 */
	public void run() throws IOException {

		Model m1 = ModelFactory.createDefaultModel();
		Model m2 = ModelFactory.createDefaultModel();
		CSVReader reader = new CSVReader(new FileReader(perfMapping), ',', '"',
				CSVWriter.NO_ESCAPE_CHARACTER);
		String[] nextLine;
		if (hasHeader) // skip header
			reader.readNext();
//		int i = 0;
		while ((nextLine = reader.readNext()) != null) {

			String srcURI = srcNamespace + nextLine[0];
			LOGGER.info("Crawling " + srcURI);
			addToModel(srcURI, srcEndpoint, m1);
			LOGGER.info("Source model size = " + m1.size());

			String tgtURI = tgtNamespace + nextLine[1];
			LOGGER.info("Crawling " + tgtURI);
			addToModel(tgtURI, tgtEndpoint, m2);
			LOGGER.info("Target model size = " + m2.size());

//			if (++i == 1)
//				break;
		}

		reader.close();

		addSatellites(m1, srcEndpoint);
		addSatellites(m2, tgtEndpoint);

		LOGGER.info("Saving source model...");
		RDFDataMgr.write(new FileOutputStream(new File(srcOut)), m1,
				RDFFormat.NT);
		LOGGER.info("Saving target model...");
		RDFDataMgr.write(new FileOutputStream(new File(tgtOut)), m2,
				RDFFormat.NT);
		LOGGER.info("Done.");

	}

	/**
	 * @param model
	 * @param endpoint
	 */
	private void addSatellites(Model model, String endpoint) {

		// collect objects which are URIs (no repetition)
		TreeSet<String> objURIs = new TreeSet<String>();
		NodeIterator it = model.listObjects();
		while (it.hasNext()) {
			RDFNode node = it.next();
			if (node.isURIResource())
				objURIs.add(node.asResource().getURI());
		}

		for (String uri : objURIs) {
			LOGGER.info("Crawling satellite " + uri);
			addToModel(uri, endpoint, model);
			LOGGER.info("Model size = " + model.size());
		}
	}

	/**
	 * @param uri
	 * @param endpoint
	 * @param m
	 */
	private void addToModel(String uri, String endpoint, Model m) {

		Resource s = m.createResource(uri);

		String query = "SELECT ?p ?o WHERE { <" + uri + "> ?p ?o }";
		Query sparqlQuery = QueryFactory.create(query, Syntax.syntaxARQ);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint,
				sparqlQuery);
		ResultSet rs = qexec.execSelect();

		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			Property p = ResourceFactory.createProperty(qs.getResource("p")
					.getURI());
			m.add(s, p, qs.get("o"));
		}

	}

	public boolean hasHeader() {
		return hasHeader;
	}

	public void setHeader(boolean hasHeader) {
		this.hasHeader = hasHeader;
	}

}
