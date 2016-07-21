package org.aksw.mandolin.rockit.pre;

import java.util.HashMap;
import java.util.TreeSet;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class NameMapperRockit {
	
	private HashMap<String, String> mlnToUri = new HashMap<>();
	private HashMap<String, String> uriToMln = new HashMap<>();
	
	private HashMap<RockitType, TreeSet<String>> listByType = new HashMap<>();	
	
	public enum RockitType {
		RESOURCE("Res"), PROPERTY("Prop");
		private String str;
		RockitType(String str) {
			this.str = str;
		}
		public String toString() {
			return str;
		}
	}
	
	private HashMap<RockitType, Integer> count = new HashMap<>();
	
	public NameMapperRockit() {
		super();
		count.put(RockitType.RESOURCE, 0);
		count.put(RockitType.PROPERTY, 0);
		listByType.put(RockitType.RESOURCE, new TreeSet<>());
		listByType.put(RockitType.PROPERTY, new TreeSet<>());
	}
	
	/**
	 * Add an URI to the map and return the MLN name.
	 * 
	 * @param uri
	 * @return
	 */
	public String add(String uri, RockitType type) {
		
		if(uriToMln.containsKey(uri))
			return uriToMln.get(uri);
		
		String name = type.toString() + count.get(type);
		mlnToUri.put(name, uri);
		uriToMln.put(uri, name);
		listByType.get(type).add(name);
		increase(type);
		return name;
	}
	
	private void increase(RockitType type) {
		count.put(type, count.get(type) + 1);
	}
	
	public String getURI(String name) {
		return mlnToUri.get(name);
	}

	public String getName(String uri) {
		return uriToMln.get(uri);
	}
	
	public TreeSet<String> getNamesByType(RockitType type) {
		return listByType.get(type);
	}
	
	public void pretty() {
		for(String key : mlnToUri.keySet())
//			if(listByType.get(Type.PROPERTY).contains(key)) // TODO remove me!
			System.out.println(key + "\t" + mlnToUri.get(key));
	}
	
}
