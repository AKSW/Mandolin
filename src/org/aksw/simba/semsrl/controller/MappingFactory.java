package org.aksw.simba.semsrl.controller;

import java.io.IOException;

import org.aksw.simba.semsrl.io.Bundle;
import org.aksw.simba.semsrl.io.CSVLoader;
import org.aksw.simba.semsrl.io.NTLoader;
import org.aksw.simba.semsrl.model.Mapping;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class MappingFactory {
	
	public static Mapping createMapping(String propFile) throws IOException {
		
		Bundle.setBundleName(propFile);
		String[] mappingFiles = Bundle.getString("mapping_files").split(",");
		Mapping mapping = new Mapping(propFile, mappingFiles);
		
		for(String fileName : mapping.getFileNames()) {
			System.out.println("processing file: "+fileName);
			String lwc = fileName.toLowerCase();
			if(lwc.endsWith(".csv"))
				CSVLoader.load(mapping, fileName);
			else if(lwc.endsWith(".nt") || lwc.endsWith(".ttl"))
				NTLoader.load(mapping, fileName);
			else
				System.err.println("Error: file "+fileName+" extension not supported. Skipping.");
		}

		return mapping;
	}

}
