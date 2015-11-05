package org.aksw.mandolin;

import java.util.HashMap;
import java.util.TreeSet;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class NameMapperProbKB {
	
	private HashMap<String, String> mlnToUri = new HashMap<>();
	private HashMap<String, String> uriToMln = new HashMap<>();
	
	private HashMap<Type, TreeSet<String>> listByType = new HashMap<>();
	
	public TreeSet<String> getEntClasses() {
		return entClasses;
	}

	public TreeSet<String> getRelClasses() {
		return relClasses;
	}

	public TreeSet<String> getRelationships() {
		return relationships;
	}

	private TreeSet<String> entClasses = new TreeSet<>();
	private TreeSet<String> relClasses = new TreeSet<>();
	private TreeSet<String> relationships = new TreeSet<>();
	
	public enum Type {
		CLASS, ENTITY, RELATION;
		public String toString() {
			return this.name();
		}
	}
	
	private HashMap<Type, Integer> count = new HashMap<>();
	
	public NameMapperProbKB() {
		super();
		for(Type t : Type.values()) {
			count.put(t, 0);
			listByType.put(t, new TreeSet<>());
		}
	}
	
	public void addEntClass(String entName, String className) {
		entClasses.add(entName + "#" + className);
	}

	public void addRelClass(String relName, String className) {
		relClasses.add(relName + "#" + className);
	}

	public void addRelationship(String relName, String name1, String name2) {
		relationships.add(relName + "#" + name1 + "#" + name2);
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
	
	public HashMap<String, String> getNamesToURIs() {
		return mlnToUri;
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
	
	public void pretty() {
		for(String key : mlnToUri.keySet())
//			if(listByType.get(Type.PROPERTY).contains(key)) // TODO remove me!
			System.out.println(key + "\t" + mlnToUri.get(key));
	}
	
}
