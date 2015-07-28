package org.aksw.simba.semsrl.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Property;

/**
 * All resources in this group are mutually connected within a transitive property linktype.
 * 
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class ConnectedGroup implements Comparable<ConnectedGroup> {
	
	private Property linktype;
	private HashMap<DataSource, String> content;
	
	private boolean training = false;

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
	
	public Set<DataSource> getDataSources() {
		return content.keySet();
	}
	
	public String getResourceURI(DataSource ds) {
		return content.get(ds);
	}
	
	public void addResourceURI(DataSource ds, String uri) {
//		content.put(ds, ds.getNamespace() + uri);
		content.put(ds, uri);
	}

	public Collection<String> getResourceURIs() {
		return content.values();
	}
	
	public void merge(ConnectedGroup cg) {
		content.putAll(cg.content);
	}

	@Override
	public int compareTo(ConnectedGroup o) {
		return this.hashCode() - o.hashCode();
	}

	public Map<DataSource, String> getMap() {
		return content;
	}

	public boolean isTraining() {
		return training;
	}

	public void setTraining(boolean training) {
		this.training = training;
	}

}
