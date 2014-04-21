package org.aksw.simba.semsrl.model;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class DataSource implements Comparable<DataSource> {
	
	private String namespace;
	private String id;
	private String storeType;
	private String storePath;
	
	public DataSource(String id) {
		this.id = id;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getId() {
		return id;
	}

	public boolean equals(Object o) {
		if(o instanceof DataSource) {
			DataSource ds = (DataSource) o;
			return id.equals(ds.getId());
		}
		return false;
	}
	
	public String toString() {
		return id;
	}
	
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public int compareTo(DataSource o) {
		return this.hashCode() - o.hashCode();
	}

	public String getStorePath() {
		return storePath;
	}

	public void setStorePath(String storePath) {
		this.storePath = storePath;
	}

	public String getStoreType() {
		return storeType;
	}

	public void setStoreType(String storeType) {
		this.storeType = storeType;
	}
	
}
