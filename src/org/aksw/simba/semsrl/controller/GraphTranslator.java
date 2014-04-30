package org.aksw.simba.semsrl.controller;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.aksw.simba.semsrl.io.Bundle;
import org.aksw.simba.semsrl.model.ResourceGraph;

import au.com.bytecode.opencsv.CSVWriter;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class GraphTranslator {

	private String basepath;
	private ResourceGraph graph;
	
	public GraphTranslator(ResourceGraph graph) {
		this.basepath = Bundle.getBundleName();
		this.graph = graph;
		new File(basepath).mkdirs();
	}
	
	public ResourceGraph getGraph() {
		return graph;
	}

	public void translate() throws IOException {
		
		CSVWriter writer = new CSVWriter(new FileWriter(basepath+"/nodefile.csv"), ',');
		
		// map a subject to another hash map, which maps a property to its value
		HashMap<String, HashMap<String, String>> map = new HashMap<>();
		
		// all the properties, sorted
		ArrayList<String> properties = new ArrayList<>();
		
		for(Statement link : graph.getLinks()) {
			
			RDFNode sub = link.getSubject();
			String s = sub.isAnon() ? sub.asNode().getBlankNodeLabel() : sub.asResource().getURI();
			String p = link.getPredicate().getURI();
			RDFNode o = link.getObject();
			
			System.out.println("TRIPLE: "+link);
			
			// all subjects are nodes
			HashMap<String, String> h;
			if(map.get(s) == null) {
				// subject is new
				h = new HashMap<>();
				if(s == null)
					System.out.println();
				map.put(s, h);
			} else {
				// subject is present
				h = map.get(s);
			}
			
			// save property
			if(!properties.contains(p))
				properties.add(p);
			
			// manage objects
			if(o instanceof Resource) {
				// object is a node
				String oUri = o.asResource().getURI();
				if(oUri != null && map.get(oUri) == null)
					// object is new
					map.put(oUri, new HashMap<String, String>());
				// TODO save to edges
			} else if(o instanceof Literal) {
				// object is an attribute (literal)
				Literal oLit = o.asLiteral();
//				switch(oLit.getDatatype().getURI()) {
//				// TODO categorical / continuous
				// TODO also: let CSVCrawler identify data types
//				default:
					h.put(p, oLit.getString());
//				}
			} else {
				System.out.println("Object not added: "+o.asNode().getName());
			}
			
		}
		
		System.out.println("MAP: "+map);
		
		System.out.println("saving map to .csv file");
		for(String s : map.keySet()) {
			String[] entries = new String[properties.size() + 1];
			entries[0] = s;
			HashMap<String, String> h = map.get(s);
			for(int i=0; i<properties.size(); i++)
				entries[i+1] = h.get(properties.get(i));
			writer.writeNext(entries);
		}

		writer.close();
	}

	
//	public static Graph translate(ResourceGraph rg) {
//		Graph g = new Graph();
//		List<Statement> links = rg.getLinks();
//		// index nodes
//		HashMap<String, Node> nodes = new HashMap<>();
//		HashMap<String, EdgeType> edges = new HashMap<>();
//		Attributes type = new Attributes("RDFNode");
//		g.addAttributes(type);
//		int index = 0;
//		for(Statement l : links) {
//			String s = l.getSubject().getURI();
//			Node n1, n2;
//			if(nodes.get(s) == null) {
//				n1 = new Node(s, type, index);
//				nodes.put(s, n1);
//				index++;
//			} else {
//				n1 = nodes.get(s);
//			}
//			String p = l.getPredicate().getURI();
//			EdgeType e;
//			if(edges.get(p) == null) {
//				e = new EdgeType(p, type.getName(), type.getName());
//				g.addEdgeType(e);
//				edges.put(p, e);
//			} else {
//				e = edges.get(p);
//			}
//			String o = l.getObject().getClass().getName();
//			if(nodes.get(o) == null) {
//				// TODO specify type here (resource | datatype)
//				n2 = new Node(o, type, index);
//				nodes.put(o, n2);
//				index++;
//			} else {
//				n2 = nodes.get(o);
//			}
//
//			g.addEdge(e, n1, n2, 1.0);
//			
//		}
//		return g;
//	}

}
