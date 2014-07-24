package org.aksw.simba.semsrl.controller;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

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

public class ProbcogGraphTranslator extends GraphTranslator {

	private static final String name = "probcog";

	public ProbcogGraphTranslator(ResourceGraph graph) {
		super(graph, name);
	}

	@Override
	public void translate() throws IOException {
		
		// from URI to ProbCog alias
		HashMap<String, String> aliases = new HashMap<>();
		
		PrintWriter dbWriter = new PrintWriter(new File(basepath + "/" + graph.getName() + ".db"));
		
		// indexing and writing entities (e.g., publications)
		Mapping mapping = graph.getMapping();
		int entIndex = 0;
		String entities = "";
		for(ConnectedGroup cg : mapping.getGroups())
			for(DataSource ds : cg.getDataSources()) {
				String localname = "p" + (++entIndex);
				aliases.put(ds.getNamespace() + cg.getResourceURI(ds), localname);
				entities += localname + ", ";
			}
		dbWriter.write("entities = {" + entities + "}\n");
		
		System.out.println(aliases);
		
		// indexing and writing nodes (e.g., titles) and other resources (e.g., authors)
		String nodes = "", resources = "";
		int nodIndex = 0, resIndex = 0;
		// index all non-present objects
		for(Statement link : graph.getLinks()) {
			RDFNode o = link.getObject();
			String id = toIdentifier(o);
			// FIXME find the bug, if any
			if(aliases.containsKey(id))
				continue;
			String localname;
			if(o.isResource()) {
				localname = "r" + (++resIndex);
				System.out.println("Resource: "+id);
				resources += localname + ", ";
			} else {
				localname = "n" + (++nodIndex);
				System.out.println("Value: "+id);
				nodes += localname + ", ";
			}
			aliases.put(id, localname);
		}
		dbWriter.write("nodes = {" + nodes + "}\n");
		dbWriter.write("resources = {" + resources + "}\n");
		
		// add triples as first-order logic predicates
		for(Statement link : graph.getLinks()) {
			Resource s = link.getSubject();
			Property p = link.getPredicate();
			RDFNode o = link.getObject();
			
			String rel = toPredicate(p);
			aliases.put(p.getURI(), rel);
			
			dbWriter.write(rel + "(" + toIdentifier(s) + ", " + toIdentifier(o) + ")\n");
			dbWriter.write("\t" + "(" + aliases.get(toIdentifier(s)) + ", " + aliases.get(toIdentifier(o)) + ")\n");
		}
				
		System.out.println(aliases);
			
		dbWriter.close();
	}

	private String toPredicate(Property p) {
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
		return name;
	}

}
