package org.aksw.mandolin.semantifier;

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
 * @deprecated This was used before learning ACM authors were not linked. Use
 *             DatasetBuild* instead.
 * 
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 * @version 0.2
 */
@Deprecated
public class BenchmarkSemantifier {

	String perfMapping, srcNamespace, tgtNamespace, srcEndpoint, tgtEndpoint,
			srcOut, tgtOut, mapOut;

	/**
	 * Whether the CSV file has a header or not.
	 */
	private boolean hasHeader = true;

	/**
	 * Output format for RDF datasets.
	 */
	private RDFFormat format = RDFFormat.NT;

	private static final Logger LOGGER = Logger
			.getLogger(BenchmarkSemantifier.class);

	/**
	 * Semantifier constructor.
	 * 
	 * @param perfMapping
	 *            path of the perfect mapping in CSV format
	 * @param srcNamespace
	 *            source dataset namespace, if needed
	 * @param tgtNamespace
	 *            target dataset namespace, if needed
	 * @param srcEndpoint
	 *            source SPARQL endpoint to fetch data
	 * @param tgtEndpoint
	 *            target SPARQL endpoint to fetch data
	 * @param srcOut
	 *            source RDF output file
	 * @param tgtOut
	 *            target RDF output file
	 * @param mapOut
	 *            mapping RDF output file
	 */
	public BenchmarkSemantifier(String perfMapping, String srcNamespace,
			String tgtNamespace, String srcEndpoint, String tgtEndpoint,
			String srcOut, String tgtOut, String mapOut) {
		this.perfMapping = perfMapping;
		this.srcNamespace = srcNamespace;
		this.tgtNamespace = tgtNamespace;
		this.srcEndpoint = srcEndpoint;
		this.tgtEndpoint = tgtEndpoint;
		this.srcOut = srcOut;
		this.tgtOut = tgtOut;
		this.mapOut = mapOut;
	}

	/**
	 * Command-line execution: specify the constructor arguments, or no
	 * arguments for a demo.
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		final int ARG_SIZE = 8;

		if (args.length != ARG_SIZE) {
			LOGGER.warn("args[] size should be " + ARG_SIZE
					+ ". Using demo values...");
			args = new String[] { "mappings/dblp-acm.csv",
					"http://dblp.rkbexplorer.com/id/",
					"http://acm.rkbexplorer.com/id/",
					"http://dblp.rkbexplorer.com/sparql",
					"http://acm.rkbexplorer.com/sparql",
					"datasets/DBLP-new.nt", "datasets/ACM-new.nt",
					"datasets/DBLP-ACM-new.nt" };
		}

		BenchmarkSemantifier sem = new BenchmarkSemantifier(args[0], args[1],
				args[2], args[3], args[4], args[5], args[6], args[7]);
		sem.run();
	}

	/**
	 * Run the semantifier.
	 * 
	 * @throws IOException
	 */
	public void run() throws IOException {

		LOGGER.info(getClass().getSimpleName() + " started.");

		Model m1 = ModelFactory.createDefaultModel();
		Model m2 = ModelFactory.createDefaultModel();
		CSVReader reader = new CSVReader(new FileReader(perfMapping), ',', '"',
				CSVWriter.NO_ESCAPE_CHARACTER);
		String[] nextLine;
		if (hasHeader) // skip header
			reader.readNext();
		int c = 0;
		while ((nextLine = reader.readNext()) != null) {

			String srcURI = srcNamespace + nextLine[0];
			LOGGER.info("Crawling " + srcURI);
			addToModel(srcURI, srcEndpoint, m1);
			LOGGER.info("Done. Source model size = " + m1.size());

			String tgtURI = tgtNamespace + nextLine[1];
			LOGGER.info("Crawling " + tgtURI);
			addToModel(tgtURI, tgtEndpoint, m2);
			LOGGER.info("Done. Target model size = " + m2.size());

			if (++c == 100)
				break;
		}

		reader.close();

		LOGGER.info("Adding satellites for source dataset...");
		addSatellites(m1, srcEndpoint);
		LOGGER.info("Adding satellites for target dataset...");
		addSatellites(m2, tgtEndpoint);

		LOGGER.info("Saving source model...");
		RDFDataMgr.write(new FileOutputStream(new File(srcOut)), m1, format);
		LOGGER.info("Saving target model...");
		RDFDataMgr.write(new FileOutputStream(new File(tgtOut)), m2, format);
		LOGGER.info("Saving mapping model...");
		MappingSemantifier.run(srcNamespace, tgtNamespace, perfMapping, mapOut);
		LOGGER.info("Done.");

	}

	/**
	 * Add satellites (i.e., URI resources directly linked with the main
	 * resources in the model) and add their CBDs to the model.
	 * 
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

		// add CBDs to model
		for (String uri : objURIs) {
			LOGGER.info("Crawling satellite " + uri);
			addToModel(uri, endpoint, model);
			LOGGER.info("Model size = " + model.size());
		}
	}

	/**
	 * Query for the specified resource on the specified SPARQL endpoint and add
	 * its CBD to the model.
	 * 
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

	public RDFFormat getFormat() {
		return format;
	}

	public void setFormat(RDFFormat format) {
		this.format = format;
	}

}
