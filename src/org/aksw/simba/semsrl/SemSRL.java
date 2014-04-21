package org.aksw.simba.semsrl;

import java.io.IOException;

import org.aksw.simba.semsrl.controller.MappingFactory;
import org.aksw.simba.semsrl.model.ConnectedGroup;
import org.aksw.simba.semsrl.model.Mapping;


/**
 * Statistical Relational Learning of Semantic Links.
 * 
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class SemSRL {

	private String propFile;
	
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
		Mapping mapping = MappingFactory.createMapping(propFile);
		
		for(ConnectedGroup cg : mapping.getGroups())
			System.out.println(cg.getMap());
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
