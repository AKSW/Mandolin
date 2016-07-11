package org.aksw.mandolin.controller;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import org.aksw.mandolin.controller.NameMapper.Type;

import com.opencsv.CSVWriter;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class ProbKBData {
	
	public final static int ENT_LENGTH = Type.ENTITY.name().length();
	public final static int CLS_LENGTH = Type.CLASS.name().length();
	public final static int REL_LENGTH = Type.RELATION.name().length();
	
	private static String base;
	private static NameMapper map;
	
	public static void buildCSV(NameMapper theMap, String theBase) throws IOException {
		
		base = theBase;
		map = theMap;
		
		allNodes();
		
		entClasses();
		relClasses();
		relationships();
		functionals();
		
	}
	
	
	private static void functionals() throws IOException {
		
		CSVWriter writer = new CSVWriter(new FileWriter(new File(base + "/functionals.csv"))); 
		
		// TODO
		
		writer.close();

	}


	private static void allNodes() throws IOException {
		
		CSVWriter entWriter = new CSVWriter(new FileWriter(new File(base + "/entities.csv"))); 
		CSVWriter clsWriter = new CSVWriter(new FileWriter(new File(base + "/classes.csv"))); 
		CSVWriter relWriter = new CSVWriter(new FileWriter(new File(base + "/relations.csv"))); 
		
		HashMap<String, String> hmap = map.getNamesToURIs();
		
		for(String key : hmap.keySet()) {
			String id = "";
			if(key.startsWith(Type.ENTITY.name())) {
				id = key.substring(ENT_LENGTH);
				entWriter.writeNext(new String[] {id, hmap.get(key)});
			}
			if(key.startsWith(Type.CLASS.name())) {
				id = key.substring(CLS_LENGTH);
				clsWriter.writeNext(new String[] {id, hmap.get(key)});
				entWriter.writeNext(new String[] {"-" + id, hmap.get(key)});
			}
			if(key.startsWith(Type.RELATION.name())) {
				id = key.substring(REL_LENGTH);
				relWriter.writeNext(new String[] {id, hmap.get(key)});
			}
		}
		
		relWriter.close();
		clsWriter.close();
		entWriter.close();

	}


	private static void entClasses() throws IOException {
		
		CSVWriter writer = new CSVWriter(new FileWriter(new File(base + "/entClasses.csv"))); 
		
		for(String line : map.getEntClasses()) {
			String[] arr = line.split("#");
			// entity_id+"|"+class_id
			String id1 = arr[0].substring(ENT_LENGTH);
			String id2 = arr[1].substring(CLS_LENGTH);
			writer.writeNext(new String[] {id1, id2});
		}
		
		writer.close();
		
	}
	
	
	/**
	 * Domain and range information, as required by ProbKB.
	 * 
	 * @throws IOException
	 */
	private static void relClasses() throws IOException {
		
		HashMap<String, String[]> entries = new HashMap<>();
		
		String owlThing = map.getOwlThingId();
		
		// set defaults
		for(String prop : map.getNamesByType(Type.RELATION)) {
			String rel = prop.substring(REL_LENGTH);
			entries.put(rel, new String[] {rel, owlThing, owlThing});
		}
		
		for(String line : map.getRelClasses()) {
			String[] arr = line.split("#");
			// rel_id+"#"+class_id+"#"+is_domain
			String rel = arr[0].substring(REL_LENGTH);
			String cl = arr[1].substring(CLS_LENGTH);
			Boolean isDomain = Boolean.parseBoolean(arr[2]);
			
			String[] obj;
			if(entries.containsKey(rel))
				obj = entries.get(rel);
			else {
				obj = new String[] {rel, owlThing, owlThing};
				entries.put(rel, obj);
			}
			obj[isDomain ? 1 : 2] = cl;
			System.out.println((isDomain ? "domain" : "range") + " => " + Arrays.toString(obj));
			
		}
		
		CSVWriter writer = new CSVWriter(new FileWriter(new File(base + "/relClasses.csv"))); 
		
		for(String entry : entries.keySet())
			writer.writeNext(entries.get(entry));
		
		writer.close();
		
	}

	
	private static void relationships() throws IOException {
		
		CSVWriter writer = new CSVWriter(new FileWriter(new File(base + "/relationships.csv"))); 
		
		Iterator<String> it = map.getRelationships().iterator();
		while(it.hasNext()) {
			String line = it.next();
			String[] arr = line.split("#");
			// relation_id+"|"+entity_id+"|"+entity_id
			String id1 = arr[0].substring(REL_LENGTH);
			String id2 = arr[1].substring(ENT_LENGTH);
			String id3 = arr[2].substring(ENT_LENGTH);
			
//			System.out.println(line);
//			if(arr[0].startsWith(Type.ENTITY.toString())) {
//				id1 = String.valueOf(Integer.parseInt(arr[0].substring(ENT_LENGTH)) + 10000);
//				System.out.println("rel = "+id1);
//			}
			
			writer.writeNext(new String[] {id1, id2, id3, "1.0", "http://"});
		}
		
		writer.close();
		
	}


}
