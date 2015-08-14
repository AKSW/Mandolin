package org.aksw.mandolin.semsrl.io;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.aksw.mandolin.semsrl.model.ConnectedGroup;
import org.aksw.mandolin.semsrl.model.DataSource;
import org.aksw.mandolin.semsrl.model.Mapping;
import org.aksw.mandolin.semsrl.util.Bundle;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.opencsv.CSVReader;


/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class CSVLoader {

	public static void load(Mapping mapping, String fileName) throws IOException {
		
		CSVReader reader = new CSVReader(new FileReader(new File(fileName)));
		// link type
		Property linktype = ResourceFactory.createProperty(Bundle.getString("csv_link_type"));
		// column titles
		String[] nextLine = reader.readNext();
		// ordered as in the file
		ArrayList<DataSource> dataSources = new ArrayList<>();
		for(int i=0; i<nextLine.length; i++) {
			DataSource ds = new DataSource(nextLine[i]);
			ds.setNamespace(Bundle.getString(nextLine[i]+"_namespace"));
			ds.setStoreType(Bundle.getString(nextLine[i]+"_store_type"));
			ds.setStorePath(Bundle.getString(nextLine[i]+"_store_path"));
			mapping.addDataSource(ds);
			dataSources.add(ds);
		}
		
		int line = 0;
		while ((nextLine = reader.readNext()) != null) {
			ConnectedGroup cg = new ConnectedGroup(linktype);
			for(int i=0; i<nextLine.length; i++) {
				cg.addResourceURI(dataSources.get(i), nextLine[i]);
			}
			mapping.addGroup(cg);
			if(++line == 50)
				break;
		}
		reader.close();
		
	}
	
}
