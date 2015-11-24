package org.aksw.mandolin;

import java.util.HashMap;
import java.util.TreeSet;

import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class NameMapperProbKB {
	
	private HashMap<String, String> mlnToUri = new HashMap<>();
	private HashMap<String, String> uriToMln = new HashMap<>();
	
	private HashMap<Type, TreeSet<String>> listByType = new HashMap<>();
	
	private String RDF_TYPE_NAME;
	private String OWL_THING_NAME;
	
	public TreeSet<String> getEntClasses() {
		return entClasses;
	}

	public TreeSet<String> getRelClasses() {
		return relClasses;
	}

	public TreeSet<String> getRelationships() {
		return relationships;
	}

	// TODO change to HashMap<String, TreeSet<String>>
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
			count.put(t, 1);
			listByType.put(t, new TreeSet<>());
		}
		// for comodity, the first element is always rdf:type
		RDF_TYPE_NAME = this.add(RDF.type.getURI(), Type.RELATION);
		System.out.println("Alias for rdf:type is " + RDF_TYPE_NAME);
		// same for owl:Thing
		OWL_THING_NAME = this.add(OWL.Thing.getURI(), Type.CLASS);
		System.out.println("Alias for owl:Thing is " + OWL_THING_NAME);
	}
	
	public void addEntClass(String entName, String className) {
		entClasses.add(entName + "#" + className);
		// add an rdf:type relationship
		this.addRelationship(RDF_TYPE_NAME, entName, Type.ENTITY.toString() + "-" + className.substring(ProbKBData.CLS_LENGTH));
		// add rdf:type owl:Thing
		this.addRelationship(RDF_TYPE_NAME, entName, Type.ENTITY.toString() + "-" + OWL_THING_NAME.substring(ProbKBData.CLS_LENGTH));
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

	/**
	 * Return only the ID (number after the Type) of the class the given entity belongs to. If not found, return the ID of owl:Thing.
	 * 
	 * @param entityName
	 * @return
	 */
	public String classIdOf(String entityName) {
		for(String ec : entClasses)
			if(ec.startsWith(entityName+"#"))
				return ec.substring(ProbKBData.CLS_LENGTH);
		return OWL_THING_NAME.substring(ProbKBData.CLS_LENGTH);
	}
	
}
