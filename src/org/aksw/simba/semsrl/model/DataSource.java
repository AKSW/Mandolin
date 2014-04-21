package org.aksw.simba.semsrl.model;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class DataSource implements Comparable<DataSource> {
	
	private String namespace;
	private String storeType;
	private String storePath;
	
	public DataSource(String namespace) {
		this.namespace = namespace;
	}

	public String getNamespace() {
		return namespace;
	}
	
	public boolean equals(Object o) {
		if(o instanceof DataSource) {
			DataSource ds = (DataSource) o;
			return namespace.equals(ds.getNamespace());
		}
		return false;
	}
	
	public String toString() {
		return namespace;
	}
	
	public int hashCode() {
		return namespace.hashCode();
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
