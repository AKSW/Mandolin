package org.aksw.simba.semsrl.controller;

import java.io.File;
import java.io.IOException;

import org.aksw.simba.semsrl.io.Bundle;
import org.aksw.simba.semsrl.model.ResourceGraph;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public abstract class GraphTranslator {
	
	protected String basepath;
	protected ResourceGraph graph;

	protected GraphTranslator(ResourceGraph graph, String name) {
		this.basepath = Bundle.getBundleName() + "-" + name;
		this.graph = graph;
		new File(basepath).mkdirs();
	}
	
	public abstract void translate() throws IOException;
	
	public abstract String getName();
	
	public ResourceGraph getGraph() {
		return graph;
	}
		
}
