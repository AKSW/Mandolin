package org.aksw.mandolin.semantifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.TreeSet;

import org.aksw.mandolin.util.DataIO;
import org.simmetrics.metrics.Levenshtein;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class DatasetBuilder {

	private static final String ENDPOINT = "http://139.18.8.97:8890/sparql";
	private static final String GRAPH = "http://acm.rkbexplorer.com";

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, IOException {
		new DatasetBuilder().run();
	}

	public void run() throws FileNotFoundException, ClassNotFoundException, IOException {
		
		// load DBLP l3s to ACM rkb
		HashMap<String, String> l3sMap = l3sToACMRkb();
		
		// build reverse map
		HashMap<String, TreeSet<String>> map = new HashMap<>();
		
		ArrayList<Elements> data = DataIO.readList("tmp/pubs-with-authors.dblp-l3s.map");
		for(Elements e : data) {
//			// TODO remove me!
//			if(!l3sMap.containsKey(e.getURI()))
//				continue;
			
			for(String el : e.getElements()) {
				TreeSet<String> pubSet;
				if(map.containsKey(el))
					pubSet = map.get(el);
				else {
					pubSet = new TreeSet<>();
					map.put(el, pubSet);
				}
				pubSet.add(e.getURI());
			}
		}
		
		HashMap<String, ArrayList<String>> sameAsMap = new HashMap<>();
		
		PrintWriter pw = new PrintWriter(new File("tmp/distances.csv"));
		
		// algorithm starts here
		for(String author : map.keySet()) {
			
			String authorName = getName(author);
			
			System.out.println("Listing " + authorName + " ("+author+"): "+map.get(author));
			
			TreeSet<String> sameAs = new TreeSet<>();
			
			for(String l3s : map.get(author)) {
				
				System.out.println("L3S: "+l3s);
				
				String acmRkb = l3sMap.get(l3s);
				
				float distMin = Float.MAX_VALUE;
				Entity entity = null;
				
				ArrayList<Entity> rkb = getCreators(acmRkb);
				for(Entity e : rkb) {
					Levenshtein lev = new Levenshtein();
					float d = lev.distance(authorName, e.getLabel());
					if(d <= distMin) {
						distMin = d;
						entity = e;
					}
					System.out.println("d("+authorName+", "+e.getLabel()+") = "+d);
				}
				
				if(entity == null) {
					System.out.println("URI "+acmRkb+" is deprecated or has issues.");
					continue;
				}
				
				if(distMin >= 5.0)
					pw.write(authorName+","+entity.getLabel()+","+author+","+entity.getUri()+"\n");
				
				System.out.println("sameAs = "+entity.getUri());
				sameAs.add(entity.getUri());
				
			}
			
			sameAsMap.put("http://mandolin.aksw.org/acm/" + author.substring(32), new ArrayList<>(sameAs));
			
			
//			System.out.println(sameAsMap);
//			break;
			
		}
		
		pw.close();
		
		DataIO.serialize(sameAsMap, "tmp/authors-sameas.map");

	}
	
	private String getName(String uri) {
		
		String query = "SELECT * WHERE { <"+uri+"> <http://www.w3.org/2000/01/rdf-schema#label> ?l }";
		System.out.println(query);
		
		ResultSet rs = DatasetBuilder.sparql(query, "http://dblp.l3s.de/d2r/sparql");

		if (rs.hasNext()) {
			QuerySolution qs = rs.next();
			return qs.getLiteral("?l").getString();
		}

		return "";
	}


	private ArrayList<Entity> getCreators(String acmRkb) {
		
		String query = "SELECT DISTINCT * WHERE { <"+acmRkb+"> "
				+ "<http://www.aktors.org/ontology/portal#has-author> ?s . "
				+ "?s <http://www.aktors.org/ontology/portal#full-name> ?l }";
		System.out.println(query);
		
		ResultSet rs = DatasetBuilder.sparql(query, ENDPOINT, GRAPH);

		ArrayList<Entity> ent = new ArrayList<>();
		
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			Entity e = new Entity(qs.getResource("?s").getURI(), qs.getLiteral("?l").getString());
			ent.add(e);
		}

		return ent;
	}

	private HashMap<String, String> l3sToACMRkb() throws FileNotFoundException {
		HashMap<String, String> map = new HashMap<>();
		
		Scanner in = new Scanner(new File("tmp/l3s-to-acmrkb.csv"));
		in.nextLine();
//		int i = 0; // TODO remove me!
		while(in.hasNextLine()) {
			String[] line = in.nextLine().split(",");
			map.put(line[0], "http://acm.rkbexplorer.com/id/" + line[1]);
//			if(++i == 100)
//				break;
		}
		in.close();
		
		return map;
	}

	public static ResultSet sparql(String query, String endpoint, String graph) {
	
		Query sparqlQuery = QueryFactory.create(query, Syntax.syntaxARQ);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint,
				sparqlQuery, graph);
		return qexec.execSelect();
	
	}

	public static ResultSet sparql(String query, String endpoint) {
		return sparql(query, endpoint, "");
	}

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

class Entity {
	
	String uri, label;
	
	Entity(String uri, String label) {
		this.uri = uri;
		this.label = label;
	}

	public String getUri() {
		return uri;
	}

	public String getLabel() {
		return label;
	}
	
}