package org.aksw.simba.semsrl.controller;

import org.aksw.simba.semsrl.model.DataSource;
import org.aksw.simba.semsrl.model.ResourceGraph;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class SparqlCrawler implements Crawler {
	
	public ResourceGraph crawl(DataSource ds, String id) {
		Resource r = ResourceFactory.createResource(ds.getNamespace() + id);
		ResourceGraph graph = new ResourceGraph(r);
		System.out.println("crawling: "+r);
		
		String query = "SELECT * WHERE { <"+ r.getURI() +"> ?p ?o }";
		Query sparqlQuery = QueryFactory.create(query, Syntax.syntaxARQ);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				ds.getStorePath(), sparqlQuery);
		ResultSet results = qexec.execSelect();
		while (results.hasNext()) {
			QuerySolution qs = results.next();
			Property p = ResourceFactory.createProperty(qs.getResource("p").getURI());
			RDFNode o = qs.get("o");
			graph.addLink(r, p, o);
		}
		
		return graph;
	}

}
