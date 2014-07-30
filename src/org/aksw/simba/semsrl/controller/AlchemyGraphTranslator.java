package org.aksw.simba.semsrl.controller;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import org.aksw.simba.semsrl.io.Bundle;
import org.aksw.simba.semsrl.model.ConnectedGroup;
import org.aksw.simba.semsrl.model.DataSource;
import org.aksw.simba.semsrl.model.Mapping;
import org.aksw.simba.semsrl.model.ResourceGraph;
import org.aksw.simba.semsrl.util.StringClean;
import org.apache.commons.codec.digest.DigestUtils;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class AlchemyGraphTranslator extends GraphTranslator {

	private static final String NAME = "alchemy";

	public AlchemyGraphTranslator(ResourceGraph graph) {
		super(graph, NAME);
	}

	private String toStringOutput(String name, Set<String> set) {
		String out = name + " = { ";
		ArrayList<String> arr = new ArrayList<>(set);
		for(int i=0; i<arr.size(); i++) {
			out += arr.get(i);
			if(i < arr.size()-1)
				out += ", ";
		}
		return out + " }\n";
	}
	
	@Override
	public void translate() throws IOException {
		
		// restricting properties
		// TODO as main method parameter
		TreeSet<String> props = new TreeSet<>();
//		Scanner in = new Scanner(new File(basepath + "/" + graph.getName() + ".pro"));
//		while(in.hasNextLine())
//			props.add(in.nextLine());
//		in.close();
		
		// from URI to ProbCog alias
		HashMap<String, String> aliases = new HashMap<>();
		
		PrintWriter dbWriter = new PrintWriter(new File(basepath + "/" + graph.getName() + ".db"));
		PrintWriter mlnWriter = new PrintWriter(new File(basepath + "/" + graph.getName() + ".mln"));
		
		// indexing and writing entities (e.g., publications)
		Mapping mapping = graph.getMapping();
		int entIndex = 0, nodIndex = 0, resIndex = 0;

		TreeSet<String> entities = new TreeSet<>(); 
		TreeSet<String> nodes = new TreeSet<>(); 
		TreeSet<String> resources = new TreeSet<>();
		
		HashMap<String, String> refersTo = new HashMap<>();
		
		for(ConnectedGroup cg : mapping.getGroups())
			for(DataSource ds : cg.getDataSources()) {
				String localname = "P" + (++resIndex);
				aliases.put(ds.getNamespace() + cg.getResourceURI(ds), localname);
				resources.add(localname);
				String entLocalname = "E" + (++entIndex);
//				aliases.put(ds.getNamespace() + cg.getResourceURI(ds) + "#_", entLocalname);
				entities.add(entLocalname);
				dbWriter.write("RefersTo(" + localname + ", " + entLocalname + ")\n");
				refersTo.put(localname, entLocalname);
			}
		
		System.out.println(aliases);
		
		// indexing and writing nodes (e.g., titles) and other resources (e.g., authors)
		// index all non-present objects
		for(Statement link : graph.getLinks()) {
			RDFNode o = link.getObject();
			String id = toIdentifier(o);
			
			if(aliases.containsKey(id))
				continue;
			String localname;
			if(o.isResource()) {
				localname = "R" + (++resIndex);
				System.out.println("Resource: "+id);
				resources.add(localname);
			} else {
				localname = "N" + (++nodIndex);
				System.out.println("Value: "+id);
				nodes.add(localname);
			}
			aliases.put(id, localname);
		}
		mlnWriter.write(toStringOutput("entity", entities));
		mlnWriter.write(toStringOutput("node", nodes));
		mlnWriter.write(toStringOutput("resource", resources));
		mlnWriter.write("\n");
		
		for(String e : entities)
			dbWriter.write("EqualsEnt(" + e + ", " + e + ")\n");
		
		for(String e : nodes)
			dbWriter.write("NodeEquals(" + e + ", " + e + ")\n");
		
		for(String e : resources)
			dbWriter.write("Equals(" + e + ", " + e + ")\n");
		
		HashMap<String, String> propSpec = new HashMap<>();
		
		// add triples as first-order logic predicates
		for(Statement link : graph.getLinks()) {
			Resource s = link.getSubject();
			Property p = link.getPredicate();
			RDFNode o = link.getObject();
			
			String rel = toPredicate(p, s, o, aliases);
			aliases.put(p.getURI(), rel);
			
			String aliasS = aliases.get(toIdentifier(s));
			String aliasO = aliases.get(toIdentifier(o));
			
			propSpec.put(rel, aliasS.charAt(0) + "" + aliasO.charAt(0));
			
			if(!props.contains(rel)) {
				dbWriter.write(rel + "(" + aliasS + ", " + aliasO + ")\n");
				if(rel.equals("Equals") && aliasS.startsWith("P") && aliasO.startsWith("P"))
					dbWriter.write("EqualsEnt(" + refersTo.get(aliasS) + ", " + refersTo.get(aliasO) + ")\n");
			}

		}
				
		System.out.println(propSpec);
		
		dbWriter.close();
		
		String rules = "";
		for(String rel : propSpec.keySet()) {
			String spec = propSpec.get(rel);
			String[] domain = new String[2];
			for(int i=0; i<2; i++) {
				if(spec.charAt(i) == 'E')
					domain[i] = "entity";
				else if(spec.charAt(i) == 'N')
					domain[i] = "node";
				else // 'R' or 'P'
					domain[i] = "resource";
			}
			if(!props.contains(rel)) { 
				mlnWriter.write(rel + "(" + domain[0] + ", "+ domain[1] + ")\n");
				
				if(domain[1].equals("resource"))
					rules += "1 Equals(x, y) ^ "+rel+"(x, a) ^ "+rel+"(y, b) => Equals(a, b)\n";
				if(domain[1].equals("node"))
					rules += "1 Equals(x, y) ^ "+rel+"(x, a) ^ "+rel+"(y, b) => NodeEquals(a, b)\n";
			}
		}
		
		mlnWriter.write("RefersTo(resource, entity)\n");
		
		mlnWriter.write("EqualsEnt(entity, entity)\n");
		mlnWriter.write("NodeEquals(node, node)\n\n");
		
		mlnWriter.write("1 Equals(x, y) <=> Equals(y, x)\n");
		mlnWriter.write("1 Equals(x, y) ^ Equals(y, z) => Equals(x, z)\n");
		mlnWriter.write("1 EqualsEnt(x, y) <=> EqualsEnt(y, x)\n");
		mlnWriter.write("1 EqualsEnt(x, y) ^ EqualsEnt(y, z) => EqualsEnt(x, z)\n");
		mlnWriter.write("1 NodeEquals(x, y) <=> NodeEquals(y, x)\n");
		mlnWriter.write("1 NodeEquals(x, y) ^ NodeEquals(y, z) => NodeEquals(x, z)\n");
		mlnWriter.write("1 Equals(x, y) ^ RefersTo(x, p) ^ RefersTo(y, q) => EqualsEnt(p, q)\n");
		mlnWriter.write("1 Equals(x, x)\n");
		mlnWriter.write("1 EqualsEnt(x, x)\n");
		mlnWriter.write("1 NodeEquals(x, x)\n");
		

		mlnWriter.write(rules);
		
		mlnWriter.close();

	}

	private String toPredicate(Property p, Resource s, RDFNode o, HashMap<String, String> aliases) {
		if(p.getURI().equals(Bundle.getString("owl_same_as")))
			return "Equals";
		else
			return "R_" + StringClean.clean(p.getLocalName());
	}

	/**
	 * This ID may be either a URI or a literal.
	 * @param o
	 * @return
	 */
	private String toIdentifier(RDFNode o) {
		if(o.isResource())
			return o.isAnon() ? 
					Bundle.getString("local_namespace") + "blanknode/BN" + DigestUtils.sha1Hex(o.toString()) :
					((Resource) o).getURI();
		else
			return o.asLiteral().getString();
	}

	@Override
	public String getName() {
		return NAME;
	}

}
