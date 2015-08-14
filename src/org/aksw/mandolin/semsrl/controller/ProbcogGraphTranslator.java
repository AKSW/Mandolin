package org.aksw.mandolin.semsrl.controller;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Scanner;
import java.util.TreeSet;

import org.aksw.mandolin.semsrl.model.ConnectedGroup;
import org.aksw.mandolin.semsrl.model.DataSource;
import org.aksw.mandolin.semsrl.model.Mapping;
import org.aksw.mandolin.semsrl.model.ResourceGraph;
import org.aksw.mandolin.semsrl.util.Bundle;
import org.aksw.mandolin.semsrl.util.StringClean;
import org.apache.commons.codec.digest.DigestUtils;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class ProbcogGraphTranslator extends GraphTranslator {

	private static final String NAME = "probcog";
//	private static final String[] SETS = {"entity", "node", "resource"};

	public ProbcogGraphTranslator(ResourceGraph graph) {
		super(graph, NAME);
	}

	@Override
	public void translate() throws IOException {
		
		// restricting properties
		TreeSet<String> props = new TreeSet<>();
		Scanner in = new Scanner(new File(basepath + "/" + graph.getName() + ".pro"));
		while(in.hasNextLine())
			props.add(in.nextLine());
		in.close();
		
		// from URI to ProbCog alias
		HashMap<String, String> aliases = new HashMap<>();
		
		PrintWriter dbWriter = new PrintWriter(new File(basepath + "/" + graph.getName() + ".db"));
		
		// indexing and writing entities (e.g., publications)
		Mapping mapping = graph.getMapping();
		int entIndex = 0;
//		String entities = "", resources = "";
		String nodes = "";
		for(ConnectedGroup cg : mapping.getGroups())
			for(DataSource ds : cg.getDataSources()) {
				String localname = "p" + (++entIndex);
				aliases.put(ds.getNamespace() + cg.getResourceURI(ds), localname);
//				entities += localname + ", ";
				nodes += localname + ", ";
			}
		
		System.out.println(aliases);
		
		// indexing and writing nodes (e.g., titles) and other resources (e.g., authors)
		int nodIndex = 0, resIndex = 0;
		// index all non-present objects
		for(Statement link : graph.getLinks()) {
			RDFNode o = link.getObject();
			String id = toIdentifier(o);
			
			if(aliases.containsKey(id))
				continue;
			String localname;
			if(o.isResource()) {
				localname = "r" + (++resIndex);
				System.out.println("Resource: "+id);
//				resources += localname + ", ";
				nodes += localname + ", ";
			} else {
				localname = "n" + (++nodIndex);
				System.out.println("Value: "+id);
				nodes += localname + ", ";
			}
			aliases.put(id, localname);
		}
//		dbWriter.write("entity = {" + entities + "}\n");
		dbWriter.write("node = {" + nodes + "}\n");
//		dbWriter.write("resource = {" + resources + "}\n");
		
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
			
			if(props.contains(rel)) { 
				dbWriter.write(rel + "(" + aliasS + ", " + aliasO + ")\n");
				if(rel.equals("Equals"))
					dbWriter.write(rel + "(" + aliasO + ", " + aliasS + ")\n");
			}

		}
				
		System.out.println(propSpec);
			
		dbWriter.close();
		
		PrintWriter mlnWriter = new PrintWriter(new File(basepath + "/" + graph.getName() + ".mln"));

		String rules = "";
		for(String rel : propSpec.keySet()) {
//			String spec = propSpec.get(rel);
			String[] domain = new String[2];
			for(int i=0; i<2; i++) {
//				if(spec.charAt(i) == 'p')
//					domain[i] = "entity";
//				if(spec.charAt(i) == 'n')
//					domain[i] = "node";
//				if(spec.charAt(i) == 'r')
//					domain[i] = "resource";
				domain[i] = "node";
			}
			if(props.contains(rel)) { 
				mlnWriter.write(rel + "(" + domain[0] + ", "+ domain[1] + ")\n");
				
//				if(domain[1].equals("entity"))
//					rules += "1 Equals(x, y) ^ "+rel+"(x, a) ^ "+rel+"(y, b) => Equals(a, b)\n";
				if(domain[1].equals("node"))
//					rules += "1 Equals(x, y) ^ "+rel+"(x, a) ^ "+rel+"(y, b) => NodeEquals(a, b)\n";
					rules += "1 Equals(x, y) ^ "+rel+"(x, a) ^ "+rel+"(y, b) => Equals(a, b)\n";
			}
		}
		
//		mlnWriter.write("NodeEquals(node, node)\n\n");
		
		mlnWriter.write("1 Equals(x, y) <=> Equals(y, x)\n");
		mlnWriter.write("1 Equals(x, y) ^ Equals(y, z) => Equals(x, z)\n");
//		mlnWriter.write("1 NodeEquals(x, y) <=> NodeEquals(y, x)\n");
//		mlnWriter.write("1 NodeEquals(x, y) ^ NodeEquals(y, z) => NodeEquals(x, z)\n");

		mlnWriter.write(rules);
		
		mlnWriter.close();

	}

	private String toPredicate(Property p, Resource s, RDFNode o, HashMap<String, String> aliases) {
		if(p.getURI().equals(Bundle.getString("owl_same_as"))) // && aliases.get(toIdentifier(s)).startsWith("p") && aliases.get(toIdentifier(o)).startsWith("p")
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
					Bundle.getString("local_namespace") + "blanknode/BN" + DigestUtils.shaHex(o.toString()) :
					((Resource) o).getURI();
		else
			return o.asLiteral().getString();
	}

	@Override
	public String getName() {
		return NAME;
	}

}
