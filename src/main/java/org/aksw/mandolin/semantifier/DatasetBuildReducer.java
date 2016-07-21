package org.aksw.mandolin.semantifier;

import java.util.TreeSet;

import org.apache.jena.riot.RDFDataMgr;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * Reduce the dataset by deleting all authors and all publications that have
 * been added as neighbours which do not belong to the perfect mapping. This
 * step is necessary, because otherwise the MLN algorithm could predict some
 * owl:sameAs links that would become false positives even when classified
 * correctly.
 * 
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
@Deprecated
public class DatasetBuildReducer {

	private static final String ENDPOINT = "http://localhost:8890/sparql";
	private static final String GRAPH = "http://mandolin.aksw.org/acm";

	public static void main(String[] args) {
		new DatasetBuildReducer().run();
	}

	public void run() {

		System.out.println("Loading model...");
		Model m = RDFDataMgr.loadModel("LinkedACM.nt");

		for (String personURI : getPersonURIs()) {
			System.out.println("Person URI: " + personURI);
			m.remove(getCBD(personURI));
			for (String pubURI : getUnconsideredPublications(personURI)) {
				System.out.println("Publication URI: " + personURI);
				m.remove(getCBD(pubURI));
			}
		}
		TreeSet<String> authorless = getAuthorlessPublications();
		int size = authorless.size(), i = 0;
		for (String pubURI : authorless) {
			System.out.println("Publication URI:\t" + pubURI + "\t"+(++i)+" / "+size);
			m.remove(getCBD(pubURI));
		}

		Commons.save(m, "LinkedACM-final.nt");

	}

	private Model getCBD(String uri) {
		String query = "DESCRIBE <" + uri + ">";
		Query sparqlQuery = QueryFactory.create(query, Syntax.syntaxARQ);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(ENDPOINT,
				sparqlQuery, GRAPH);
		return qexec.execDescribe();
	}

	private TreeSet<String> getPersonURIs() {
		String query = "SELECT DISTINCT ?s WHERE { "
				+ "{ ?o ?p ?s } UNION { ?s ?p ?o } "
				+ "FILTER regex(?s,'^http://acm.rkbexplorer.com/id/person-','i') }";
		TreeSet<String> uris = new TreeSet<>();

		ResultSet rs = Commons.sparql(query, ENDPOINT, GRAPH);
		while (rs.hasNext())
			uris.add(rs.next().getResource("s").getURI());

		return uris;
	}

	private TreeSet<String> getUnconsideredPublications(String personURI) {
		String query = "SELECT ?s WHERE { "
				+ "?s <http://www.aktors.org/ontology/portal#has-author> <"
				+ personURI + "> }";
		TreeSet<String> uris = new TreeSet<>();

		ResultSet rs = Commons.sparql(query, ENDPOINT, GRAPH);
		while (rs.hasNext())
			uris.add(rs.next().getResource("s").getURI());

		return uris;
	}
	
	private TreeSet<String> getAuthorlessPublications() {
		String query = "SELECT DISTINCT ?s WHERE { "
				+ "{ ?o ?p ?s } UNION { ?s ?p ?o } . "
				+ "FILTER regex(?s,'^http://acm.rkbexplorer.com/id/','i') "
				+ "MINUS { ?s <http://www.aktors.org/ontology/portal#has-author> ?au } }";
		TreeSet<String> uris = new TreeSet<>();

		ResultSet rs = Commons.sparql(query, ENDPOINT, GRAPH);
		while (rs.hasNext())
			uris.add(rs.next().getResource("s").getURI());

		return uris;
	}
	

}
