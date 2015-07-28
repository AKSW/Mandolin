package org.aksw.simba.semsrl.model;

import java.util.List;

import org.aksw.simba.semsrl.io.Bundle;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class ResourceGraph {
	
	private Resource resource = null;
	private Model model = null;
	private Mapping mapping = null;
	private String name = null;
	
	public ResourceGraph(String name) {
		this.resource = ResourceFactory.createResource(Bundle.getString("local_namespace") + "alignment/" + name);
		this.model = ModelFactory.createDefaultModel();
		this.name = name;
	}

	public Resource getResource() {
		return resource;
	}
	
	public void addLink(Resource s, Property p, RDFNode o) {
		model.add(s, p, o);
	}
	
	public List<Statement> getLinks() {
		return model.listStatements().toList();
	}
	
	public void merge(ResourceGraph rg) {
		for(Statement s : rg.getLinks())
			model.add(s);
	}

	public Mapping getMapping() {
		return mapping;
	}

	public void setMapping(Mapping mapping) {
		this.mapping = mapping;
	}

	public String getName() {
		return name;
	}
	
}
