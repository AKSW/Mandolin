package org.aksw.simba.semsrl.model;

import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class ResourceGraph {
	
	private Resource resource = null;
	private Model model = null;
	
	public ResourceGraph(Resource resource) {
		this.resource = resource;
		this.model = ModelFactory.createDefaultModel();
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
	
}
