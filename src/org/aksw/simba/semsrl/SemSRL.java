package org.aksw.simba.semsrl;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import netkit.classifiers.DataView;
import netkit.classifiers.relational.WeightedVoteRelationalNeighbor;
import netkit.graph.AttributeFixedCategorical;
import netkit.graph.FixedTokenSet;
import netkit.graph.Graph;

import org.aksw.simba.semsrl.controller.CSVCrawler;
import org.aksw.simba.semsrl.controller.Crawler;
import org.aksw.simba.semsrl.controller.GraphTranslator;
import org.aksw.simba.semsrl.controller.MappingFactory;
import org.aksw.simba.semsrl.controller.SparqlCrawler;
import org.aksw.simba.semsrl.controller.Translator;
import org.aksw.simba.semsrl.model.ConnectedGroup;
import org.aksw.simba.semsrl.model.DataSource;
import org.aksw.simba.semsrl.model.Mapping;
import org.aksw.simba.semsrl.model.ResourceGraph;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Statement;


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
		
		for(ConnectedGroup cg : mapping.getGroups()) {
			Map<DataSource, String> map = cg.getMap();
			System.out.println(map);
			for(DataSource ds : map.keySet()) {
				System.out.println("source: "+ds);
				Crawler crawler = null;
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
				// TODO
				Translator gtran = new GraphTranslator(rg);
				gtran.translate();
			}
//			TODO remove me!
//			if(map.keySet().contains(new DataSource("acm")))
				break;
		}
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
