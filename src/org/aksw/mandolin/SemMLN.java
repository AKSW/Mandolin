package org.aksw.mandolin;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.aksw.mandolin.semsrl.controller.AlchemyGraphTranslator;
import org.aksw.mandolin.semsrl.controller.CSVCrawler;
import org.aksw.mandolin.semsrl.controller.Crawler;
import org.aksw.mandolin.semsrl.controller.GraphTranslator;
import org.aksw.mandolin.semsrl.controller.MappingFactory;
import org.aksw.mandolin.semsrl.controller.NetkitGraphTranslator;
import org.aksw.mandolin.semsrl.controller.ProbcogGraphTranslator;
import org.aksw.mandolin.semsrl.controller.SparqlCrawler;
import org.aksw.mandolin.semsrl.model.ConnectedGroup;
import org.aksw.mandolin.semsrl.model.DataSource;
import org.aksw.mandolin.semsrl.model.Mapping;
import org.aksw.mandolin.semsrl.model.ResourceGraph;
import org.aksw.mandolin.semsrl.util.Bundle;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;


/**
 * Statistical Relational Learning of Semantic Links using Markov Logic Networks.
 * 
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class SemMLN {

	private static final double TRAINING_FRACTION = 0.1;
	
	/*
	 * Logger.
	 */
	private static Logger logger = Logger.getLogger(SemMLN.class);
	private static HashMap<String, Crawler> crawlers;
	
	private String propFile;
	private String approach;

	private ResourceGraph graph;
	private ArrayList<ConnectedGroup> groups;
	
	public String getArgs() {
		return propFile;
	}

	public SemMLN(String propFile, String approach) {
		this.propFile = propFile;
		this.approach = approach;
		
		logger.setResourceBundle(ResourceBundle.getBundle("log4j"));
		Bundle.setBundleName(propFile);

		crawlers = new HashMap<>();
		crawlers.put("sparql", new SparqlCrawler());
		crawlers.put("csv", new CSVCrawler());
	}

	/**
	 * Prepare datasets.
	 * 
	 * @throws IOException 
	 */
	public void prepare() throws IOException {
		System.out.println("Preparing datasets...");
		Mapping mapping = MappingFactory.createMapping(propFile);
		
		graph = new ResourceGraph(propFile);
		graph.setMapping(mapping);
		groups = new ArrayList<>(mapping.getGroups());
		
		for(ConnectedGroup cg : groups) {
			Map<DataSource, String> map = cg.getMap();
			System.out.println(map);
			for(DataSource ds : map.keySet()) {
				System.out.println("source: "+ds);
				Crawler crawler;
				try {
					crawler = crawlers.get(ds.getStoreType());
				} catch (Exception e) {
					System.err.println("Error: store type "+ds.getStoreType()+" not recognised.");
					continue;
				}
				ResourceGraph rg = crawler.crawl(ds, map.get(ds));
				graph.merge(rg);
			}
		}
		
	}
	
	/**
	 * SemMLN main algorithm.
	 * TODO Part of this code is shared with SemSRL: arrange it better.
	 * 
	 * @throws IOException 
	 */
	public void learn() throws IOException {
		System.out.println("SemMLN started...");
		
		// training data
		PrintWriter writer = new PrintWriter(new File(propFile + "-" + approach + "/" + propFile + ".tr"));
		for(int i=0; i<groups.size() * TRAINING_FRACTION; i++) {
			ConnectedGroup cg = groups.get(i);
			String concat = "";
			for(DataSource ds : cg.getDataSources())
				concat += ds.getNamespace() + cg.getResourceURI(ds) + "#_ ";
			writer.write(concat.trim() + "\n");
			// add sameAs links
			addSameAs(cg, graph);
		}
		writer.close();
		
		GraphTranslator gtran = null;
		if(approach.equals("netkit"))
			gtran = new NetkitGraphTranslator(graph);
		if(approach.equals("probcog"))
			gtran = new ProbcogGraphTranslator(graph);
		if(approach.equals("alchemy"))
			gtran = new AlchemyGraphTranslator(graph);
		gtran.translate();
	}

	private void addSameAs(ConnectedGroup cg, ResourceGraph graph) { 
		cg.setTraining(true);
		Property sameAs = ResourceFactory.createProperty(Bundle.getString("owl_same_as"));
		ArrayList<Resource> res = new ArrayList<>();
		Map<DataSource, String> map = cg.getMap();
		for(DataSource ds : map.keySet())
			res.add(ResourceFactory.createResource(ds.getNamespace() + map.get(ds)));
		// now mutually create sameAs links
		for(Resource s : res)
			for(Resource o : res)
				if(s != o)
					graph.addLink(s, sameAs, o);
	}

	/**
	 * @param args The property file path.
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		SemMLN srl = new SemMLN(args[0], args[1]);
		srl.prepare();
		srl.learn();
	}

	public String getApproach() {
		return approach;
	}

	public void setApproach(String approach) {
		this.approach = approach;
	}

}
