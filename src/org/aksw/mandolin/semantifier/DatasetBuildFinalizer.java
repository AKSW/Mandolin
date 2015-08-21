package org.aksw.mandolin.semantifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.TreeSet;

import org.aksw.mandolin.util.DataIO;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;


/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class DatasetBuildFinalizer {

	public static final String ENDPOINT = "http://localhost:8890/sparql";
	
	private TreeSet<String> visited = new TreeSet<>();

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, IOException {
		
		new DatasetBuildFinalizer().run();
		
	}
	
	public void run() throws FileNotFoundException, ClassNotFoundException, IOException {
		
		Model m = ModelFactory.createDefaultModel();
		
		// build reverse map
		HashMap<String, ArrayList<String>> map = DataIO.readMap("authors-sameas.map");
		HashMap<String, String> old2new = new HashMap<>();
		for(String key : map.keySet())
			for(String val : map.get(key))
				old2new.put(val, key);
		System.out.println("# old author URIs = "+old2new.size());
		System.out.println("# new author URIs = "+map.size());
		
		// add the CBD of each publication and its direct neighbours to the model
		Scanner in = new Scanner(new File("l3s-to-acmrkb.csv"));
		in.nextLine(); // skip header
		TreeSet<String> neighbours = new TreeSet<>();
		while(in.hasNextLine()) {
			String uri = "http://acm.rkbexplorer.com/id/" + in.nextLine().split(",")[1];
			Model m1 = addToModel(uri, ENDPOINT, m);
			if(m1 == null)
				continue;
			TreeSet<String> neigh = getNeighbours(uri, m1);
			neighbours.addAll(neigh);
			System.out.println("URI        = "+uri);
			System.out.println("CBD size   = "+m1.size());
			System.out.println("Model size = "+m.size());
			System.out.println("URI Neighb = "+neigh.size());
			System.out.println("Tot Neighb = "+neighbours.size());
		}
		in.close();
		DatasetBuilder.save(m, "LinkedACM.nt");
		// remove all visited nodes from neighbours
		System.out.println("Neighbour size before = "+neighbours.size());
		neighbours.removeAll(visited);
		System.out.println("Neighbour size after  = "+neighbours.size());
		// add remaining neighbours and their CBD
		for(String uri : neighbours) {
			Model m1 = addToModel(uri, ENDPOINT, m);
			if(m1 == null)
				continue;
			System.out.println("Neighb URI = "+uri);
			System.out.println("N CBD size = "+m1.size());
			System.out.println("Model size = "+m.size());
		}
		DatasetBuilder.save(m, "LinkedACM.nt");
		// replace all occurrences of old ACM URIs with the new ones (old2new map)
		Iterator<Statement> it = m.listStatements();
		Model m2 = ModelFactory.createDefaultModel();
		while(it.hasNext()) {
			Statement st = it.next();
			String sub = st.getSubject().getURI();
			if(old2new.containsKey(sub)) {
				Resource newSub = ResourceFactory.createResource(old2new.get(sub));
				System.out.println(sub + " -> " + newSub.getURI());
				m2.add(m2.createStatement(newSub, st.getPredicate(), st.getObject()));
				it.remove();
			}
			if(st.getObject().isURIResource()) {
				String obj = st.getObject().asResource().getURI();
				if(old2new.containsKey(obj)) {
					Resource newObj = ResourceFactory.createResource(old2new.get(obj));
					System.out.println(obj + " -> " + newObj.getURI());
					m2.add(m2.createStatement(st.getSubject(), st.getPredicate(), newObj));
					it.remove();
				}
			}
		}
		m.add(m2);
		// save model
		System.out.println("Saving model...");
		DatasetBuilder.save(m, "LinkedACM.nt");
				
		
	}
	
	private TreeSet<String> getNeighbours(String uri, Model m1) {
		
		TreeSet<String> neighbours = new TreeSet<>();
		
		Iterator<Statement> it = m1.listStatements();
		while(it.hasNext()) {
			Statement st = it.next();
			if(st.getSubject().getURI().equals(uri)) {
				if(st.getObject().isURIResource())
					neighbours.add(st.getObject().asResource().getURI());
			}
			if(st.getObject().toString().equals(uri))
				neighbours.add(st.getSubject().getURI());
		}
		
		return neighbours;
	}

	/**
	 * Query for the specified resource on the specified SPARQL endpoint and add
	 * its CBD to the model.
	 * 
	 * @param uri
	 * @param endpoint
	 * @param m
	 */
	public Model addToModel(String uri, String endpoint, Model m) {
		
		if(visited.contains(uri))
			return null;

		String query = "DESCRIBE <"+uri+">";
		Query sparqlQuery = QueryFactory.create(query, Syntax.syntaxARQ);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint,
				sparqlQuery);
		Model m1 = qexec.execDescribe();
		m.add(m1);

		visited.add(uri);
		
		return m1;
	}


}
