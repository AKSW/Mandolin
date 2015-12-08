package org.aksw.mandolin.semantifier;

import java.io.FileOutputStream;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * Database builder common constants and methods.
 * 
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class Commons {

	// SPARQL
	// used to be "http://dblp.l3s.de/d2r/sparql"
	public static final String DBLPL3S_ENDPOINT = "http://139.18.8.97:8890/sparql";
	// used to be ""
	public static final String DBLPL3S_GRAPH = "http://dblp.l3s.de";
	public static final String ACMRKB_ENDPOINT = "http://139.18.8.97:8890/sparql";
	public static final String ACMRKB_GRAPH = "http://acm.rkbexplorer.com";

	public static final String DBLP_NAMESPACE = "http://dblp.rkbexplorer.com/id/";
	public static final String DBLPL3S_NAMESPACE = "http://dblp.l3s.de/d2r/resource/publications/";
	public static final String ACMRKB_NAMESPACE = "http://acm.rkbexplorer.com/id/";
	public static final String LINKEDACM_NAMESPACE = "http://mandolin.aksw.org/acm/";
	
	public static final String OLD_AUTHOR_PREFIX = "http://acm.rkbexplorer.com/id/person-";

	// URIs
	public static final Resource DBLPL3S_PUBLICATION_CLASS = ResourceFactory
			.createResource("http://xmlns.com/foaf/0.1/Document");
	public static final Resource ACMRKB_PUBLICATION_CLASS = ResourceFactory
			.createResource("http://www.aktors.org/ontology/portal#Article-Reference");
	public static final Resource DBLPL3S_AUTHOR_CLASS = ResourceFactory
			.createResource("http://xmlns.com/foaf/0.1/Agent");
	public static final Resource ACMRKB_AUTHOR_CLASS = ResourceFactory
			.createResource("http://www.aktors.org/ontology/portal#Person");
	public static final Property RDF_TYPE = ResourceFactory
			.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
	public static final Property OWL_SAMEAS = ResourceFactory
			.createProperty("http://www.w3.org/2002/07/owl#sameAs");
	public static final Property RDFS_LABEL = ResourceFactory
			.createProperty("http://www.w3.org/2000/01/rdf-schema#label");
	public static final Property HAS_AUTHOR = ResourceFactory
			.createProperty("http://www.aktors.org/ontology/portal#has-author");
	public static final Property HAS_TITLE = ResourceFactory
			.createProperty("http://www.aktors.org/ontology/portal#has-title");
	public static final Property FULL_NAME = ResourceFactory
			.createProperty("http://www.aktors.org/ontology/portal#full-name");
	public static final Property DC_CREATOR = ResourceFactory
			.createProperty("http://purl.org/dc/elements/1.1/creator");
	
	public static final String SIMILAR_TO_PREFIX = "http://mandolin.aksw.org/ontology#similarTo";

	// I/O
	public static final String DBLP_ACM_CSV = "mappings/dblp-acm.csv";
	public static final String DBLP_ACM_FIXED_CSV = "mappings/dblp-acm-fixed.csv";
	public static final String DBLP_ACM_REMOVED_CSV = "tmp/removed-publications.csv";
	
	public static final String PUBS_WITH_AUTHORS_MAP = "tmp/pubs-with-authors.dblp-l3s.map";
	public static final String AUTHORS_SAMEAS_MAP = "tmp/authors-sameas.map";
	
	public static final String TO_BE_DELETED_ID = "tmp/to-be-deleted-id.txt";
	public static final String TO_BE_DELETED = "tmp/to-be-deleted.txt";
	public static final String DISTANCES_CSV = "tmp/distances.csv";
	
	public static final String LINKEDACM_NT = "datasets/LinkedACM.nt";
	public static final String DBLPL3S_LINKEDACM_NT = "linksets/DBLPL3S-LinkedACM.nt";
	public static final String DBLPL3S_NT = "datasets/DBLPL3S.nt";

	
	public static final String getSimilarTo(int thr) {
		// no such property
		if(thr <= 0 || thr >= 100)
			return null;
		return SIMILAR_TO_PREFIX + thr;
	}
	
	/**
	 * Perform SPARQL query against an endpoint on a given graph.
	 * 
	 * @param query
	 * @param endpoint
	 * @param graph
	 * @return
	 */
	public static ResultSet sparql(String query, String endpoint, String graph) {

		Query sparqlQuery = QueryFactory.create(query, Syntax.syntaxARQ);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint,
				sparqlQuery, graph);
		return qexec.execSelect();

	}

	/**
	 * Save the model to N-Triple file.
	 * 
	 * @param m
	 * @param name
	 */
	public static void save(Model m, String name) {

		// save to TURTLE/N3
		try {
			FileOutputStream fout = new FileOutputStream(name);
			m.write(fout, "N-TRIPLES");
			fout.close();
		} catch (Exception e) {
			System.out.println("Exception caught" + e.getMessage());
			e.printStackTrace();
		}

	}

}
