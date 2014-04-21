package org.aksw.simba.semsrl.model;

import java.util.Collection;
import java.util.HashMap;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * All resources in this group are mutually connected within a transitive property linktype.
 * 
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class ConnectedGroup implements Comparable<ConnectedGroup> {
	
	private Property linktype;
	private HashMap<DataSource, Resource> content;

	public ConnectedGroup(Property linktype) {
		this.setLinktype(linktype);
		content = new HashMap<>();
	}

	public Property getLinktype() {
		return linktype;
	}

	public void setLinktype(Property linktype) {
		this.linktype = linktype;
	}
	
	public Resource getResource(DataSource ds) {
		return content.get(ds);
	}
	
	public void addResource(DataSource ds, Resource r) {
		content.put(ds, r);
	}

	public void addResource(DataSource ds, String uri) {
		content.put(ds, ResourceFactory.createResource(uri));
	}

	public Collection<Resource> getResources() {
		return content.values();
	}
	
	public void merge(ConnectedGroup cg) {
		content.putAll(cg.content);
	}

	@Override
	public int compareTo(ConnectedGroup o) {
		return this.hashCode() - o.hashCode();
	}

	public HashMap<DataSource, Resource> getMap() {
		return content;
	}

}
