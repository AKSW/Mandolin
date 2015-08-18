package org.aksw.mandolin;

import java.util.HashMap;
import java.util.TreeSet;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class NameMapper {
	
	private HashMap<String, String> mlnToUri = new HashMap<>();
	private HashMap<String, String> uriToMln = new HashMap<>();
	
	private HashMap<Type, TreeSet<String>> listByType = new HashMap<>();	
	
	public enum Type {
		RESOURCE("Res"), PROPERTY("Prop");
		private String str;
		Type(String str) {
			this.str = str;
		}
		public String toString() {
			return str;
		}
	}
	
	private HashMap<Type, Integer> count = new HashMap<>();
	
	public NameMapper() {
		super();
		count.put(Type.RESOURCE, 0);
		count.put(Type.PROPERTY, 0);
		listByType.put(Type.RESOURCE, new TreeSet<>());
		listByType.put(Type.PROPERTY, new TreeSet<>());
	}
	
	/**
	 * Add an URI to the map and return the MLN name.
	 * 
	 * @param uri
	 * @return
	 */
	public String add(String uri, Type type) {
		
		if(uriToMln.containsKey(uri))
			return uriToMln.get(uri);
		
		String name = type.toString() + count.get(type);
		mlnToUri.put(name, uri);
		uriToMln.put(uri, name);
		listByType.get(type).add(name);
		increase(type);
		return name;
	}
	
	private void increase(Type type) {
		count.put(type, count.get(type) + 1);
	}
	
	public String getURI(String name) {
		return mlnToUri.get(name);
	}

	public String getName(String uri) {
		return uriToMln.get(uri);
	}
	
	public TreeSet<String> getNamesByType(Type type) {
		return listByType.get(type);
	}
	
}
