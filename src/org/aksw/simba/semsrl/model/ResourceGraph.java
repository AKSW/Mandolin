package org.aksw.simba.semsrl.model;

import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class ResourceGraph {
	
	private Resource resource = null;
	
	public ResourceGraph(Resource resource) {
		this.resource = resource;
	}

	public Resource getResource() {
		return resource;
	}
	
}
