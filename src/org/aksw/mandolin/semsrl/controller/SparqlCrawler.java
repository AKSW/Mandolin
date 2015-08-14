package org.aksw.mandolin.semsrl.controller;

import org.aksw.mandolin.semsrl.model.DataSource;
import org.aksw.mandolin.semsrl.model.ResourceGraph;
import org.aksw.mandolin.semsrl.util.Bundle;

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
	
	private int query_depth = Integer.parseInt(Bundle.getString("query_depth"));
	
	public ResourceGraph crawl(DataSource ds, String id) {
		Resource r = ResourceFactory.createResource(ds.getNamespace() + id);
		ResourceGraph graph = new ResourceGraph(id);
		System.out.println("crawling: "+r);
		
		String query = query_depth == 1 ?
				"SELECT * WHERE { <"+ r.getURI() +"> ?p ?o }"
				: "SELECT * WHERE { <"+ r.getURI() +"> ?p ?o OPTIONAL { ?o ?p1 ?o1 } }";
		Query sparqlQuery = QueryFactory.create(query, Syntax.syntaxARQ);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				ds.getStorePath(), sparqlQuery);
		ResultSet results = qexec.execSelect();
		while (results.hasNext()) {
			QuerySolution qs = results.next();
			Property p = ResourceFactory.createProperty(qs.getResource("p").getURI());
			RDFNode o = qs.get("o");
			graph.addLink(r, p, o);
			if(query_depth == 2 && qs.getResource("p1") != null) {
				Property p1 = ResourceFactory.createProperty(qs.getResource("p1").getURI());
				RDFNode o1 = qs.get("o1");
				graph.addLink(o.asResource(), p1, o1);
			}
		}
		
		return graph;
	}

}
