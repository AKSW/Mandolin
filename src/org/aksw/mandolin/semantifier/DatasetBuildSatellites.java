package org.aksw.mandolin.semantifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.TreeSet;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Add datatype properties and satellites (URIs belonging to the CBD) for each
 * author. The only tolerated predicates for satellites are defined in the
 * 'predicates' set.
 * 
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class DatasetBuildSatellites {

	private static final String ENDPOINT = "http://localhost:8890/sparql";
	private static final String GRAPH = "http://mandolin.aksw.org/acm";

	private static final String FILE = "LinkedACM-10.nt";
	private static final String ARTICLE = "http://www.aktors.org/ontology/portal#Article-Reference";

	private static TreeSet<String> predicates = new TreeSet<>();

	static {
		// tolerate only these two types of object properties of satellites
		predicates.add(RDF.type.getURI());
		predicates.add(OWL.sameAs.getURI());
	}

	public static void main(String[] args) {
		
		run();
		
	}
	
	public static void run() {

		FileOutputStream output;
		try {
			output = new FileOutputStream(new File("datasets2/" + FILE));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}

		final StreamRDF writer = StreamRDFWriter.getWriterStream(output,
				Lang.NT);

		TreeSet<String> articleIDs = new TreeSet<>();
		TreeSet<String> satelliteIDs = new TreeSet<>();

		// stream LinkedACM dataset
		// search for ?s a
		// <http://www.aktors.org/ontology/portal#Article-Reference>
		// collect article IDs
		collectWrite(articleIDs, writer);

		System.out.println("file = " + FILE);
		System.out.println("articles = " + articleIDs.size());

		// for each article ID:
		// add its CBD and
		// collect satellite IDs
		for (String a : articleIDs) {
			System.out.print(a + "...");
			cbd(a, writer, articleIDs, satelliteIDs, true);
			System.out.println(" OK");
		}

		System.out.println();

		System.out.println("satellites = " + satelliteIDs.size());

		// for each satellite ID:
		// launch describe query
		// write out triples
		for (String aut : satelliteIDs) {
			System.out.print(aut + "...");
			cbd(aut, writer, articleIDs, satelliteIDs, false);
			System.out.println(" OK");
		}

		writer.finish();
		System.out.println("\nDone.");

	}

	private static void cbd(String uri, StreamRDF writer,
			TreeSet<String> articleIDs, TreeSet<String> satelliteIDs,
			boolean addAll) {
		String query = "DESCRIBE <" + uri + ">";
		Query sparqlQuery = QueryFactory.create(query, Syntax.syntaxARQ);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(ENDPOINT,
				sparqlQuery, GRAPH);
		Model m = qexec.execDescribe();
		StmtIterator it = m.listStatements();
		while (it.hasNext()) {
			Triple t = it.next().asTriple();

			if (addAll) {
				writer.triple(t);
				String s = t.getSubject().getURI();
				boolean isUri = t.getObject().isURI();
				if (isUri) {
					String o = t.getObject().getURI();
					if (s.equals(uri))
						satelliteIDs.add(o);
					if (o.equals(uri))
						satelliteIDs.add(s);
				}

			} else {
				String s = t.getSubject().getURI();
				String p = t.getPredicate().getURI();
				boolean isUri = t.getObject().isURI();

				if (!isUri) {
					writer.triple(t);
				} else {
					String o = t.getObject().getURI();
					if (articleIDs.contains(o))
						writer.triple(t);
					else if (articleIDs.contains(s))
						writer.triple(t);
					else if (predicates.contains(p))
						writer.triple(t);
				}
			}

		}
	}

	private static void collectWrite(TreeSet<String> articleIDs,
			StreamRDF writer) {

		StreamRDF dataStream = new StreamRDF() {

			@Override
			public void start() {
				writer.start();
			}

			@Override
			public void triple(Triple triple) {

				if (triple.getPredicate().getURI().equals(RDF.type.getURI()))
					if (triple.getObject().getURI().equals(ARTICLE))
						articleIDs.add(triple.getSubject().getURI());

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
				// finishes later
			}

		};

		RDFDataMgr.parse(dataStream, "datasets/" + FILE);

	}

}
