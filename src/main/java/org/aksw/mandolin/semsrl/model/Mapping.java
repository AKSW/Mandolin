package org.aksw.mandolin.semsrl.model;

import java.util.Set;
import java.util.TreeSet;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class Mapping {
	
	private String name;
	private String[] fileNames;
	private Set<ConnectedGroup> groups;
	private Set<DataSource> dataSources;
	
	public String getName() {
		return name;
	}

	public String[] getFileNames() {
		return fileNames;
	}

	public Set<ConnectedGroup> getGroups() {
		return groups;
	}
	
	public void addGroup(ConnectedGroup gnew) {
		for(String uri : gnew.getResourceURIs()) {
			Resource r = ResourceFactory.createResource(uri);
			for(ConnectedGroup g : groups)
				if(g.getResourceURIs().contains(r)) {
					g.merge(gnew);
					return;
				}
		}
		this.groups.add(gnew);
	}
	
	public void addDataSource(DataSource ds) {
		this.dataSources.add(ds);
	}

	public Mapping(String name, String[] fileNames) {
		this.name = name;
		this.fileNames = fileNames;
		this.groups = new TreeSet<>();
		this.dataSources = new TreeSet<>();
	}

}