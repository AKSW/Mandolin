package org.aksw.mandolin.semsrl.controller;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.aksw.mandolin.semsrl.model.DataSource;
import org.aksw.mandolin.semsrl.model.ResourceGraph;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.opencsv.CSVReader;

public class CSVCrawler implements Crawler {

	@Override
	public ResourceGraph crawl(DataSource ds, String id) throws IOException {
		Resource r = ResourceFactory.createResource(ds.getNamespace() + id);
		ResourceGraph graph = new ResourceGraph(id);
		System.out.println("crawling: "+r);
		
		CSVReader reader = new CSVReader(new FileReader(new File(ds.getStorePath())));
		// column titles
		String[] titles = reader.readNext();
		ArrayList<Property> properties = new ArrayList<>();
		for(String title : titles)
			properties.add(ResourceFactory.createProperty(title));
		// CSV file content
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			if(nextLine[0].equals(id)) {
				// starts from 1 because 1st column = resource ID
				for(int i=1; i<nextLine.length; i++) {
					graph.addLink(r, properties.get(i), ResourceFactory.createTypedLiteral(nextLine[i]));
				}
				break;
			}
		}
		reader.close();
		
		return graph;
	}

}
