package org.aksw.mandolin.semsrl.translators;

import java.io.File;
import java.io.IOException;

import org.aksw.mandolin.semsrl.model.ResourceGraph;
import org.aksw.mandolin.util.Bundle;

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
