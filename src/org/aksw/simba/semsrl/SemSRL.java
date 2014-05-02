package org.aksw.simba.semsrl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.aksw.simba.semsrl.controller.CSVCrawler;
import org.aksw.simba.semsrl.controller.Crawler;
import org.aksw.simba.semsrl.controller.GraphTranslator;
import org.aksw.simba.semsrl.controller.MappingFactory;
import org.aksw.simba.semsrl.controller.SparqlCrawler;
import org.aksw.simba.semsrl.model.ConnectedGroup;
import org.aksw.simba.semsrl.model.DataSource;
import org.aksw.simba.semsrl.model.Mapping;
import org.aksw.simba.semsrl.model.ResourceGraph;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;


/**
 * Statistical Relational Learning of Semantic Links.
 * 
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class SemSRL {

	private String propFile;

//    private static Logger logger;
//
//    static {
//        System.setProperty("java.util.logging.config.file","logging.properties");
//        logger = Logger.getLogger("NetKit");
//    }
    
	public String getArgs() {
		return propFile;
	}

	public SemSRL(String propFile) {
		this.propFile = propFile;
	}
	
	/**
	 * Semantic SRL main algorithm.
	 * @throws IOException 
	 */
	public void learn() throws IOException {
		System.out.println("SemSRL started");
		Mapping mapping = MappingFactory.createMapping(propFile);
		
		ResourceGraph graph = new ResourceGraph(ResourceFactory.createResource("http://aksw.org/Groups/SIMBA/SemSRL/alignment/"+propFile));
		for(ConnectedGroup cg : mapping.getGroups()) {
			Map<DataSource, String> map = cg.getMap();
			System.out.println(map);
			for(DataSource ds : map.keySet()) {
				System.out.println("source: "+ds);
				Crawler crawler = null;
				// TODO use map instead of switch.
				switch(ds.getStoreType()) {
				case "sparql":
					crawler = new SparqlCrawler();
					break;
				case "csv":
					crawler = new CSVCrawler();
					break;
				default:
					System.err.println("Error: store type "+ds.getStoreType()+" not recognised.");
					continue;
				}
				ResourceGraph rg = crawler.crawl(ds, map.get(ds));
				graph.merge(rg);
			}
			// add sameAs links
			addSameAs(cg, graph);
//			TODO remove me!
//			if(map.keySet().contains(new DataSource("acm")))
				break;
		}
		
		GraphTranslator gtran = new GraphTranslator(graph);
		gtran.translate();

	}

	private void addSameAs(ConnectedGroup cg, ResourceGraph graph) { 
		Property sameAs = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#sameAs");
		ArrayList<Resource> res = new ArrayList<>();
		Map<DataSource, String> map = cg.getMap();
		for(DataSource ds : map.keySet())
			res.add(ResourceFactory.createResource(map.get(ds)));
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
		SemSRL srl = new SemSRL(args[0]);
		srl.learn();
	}

}
