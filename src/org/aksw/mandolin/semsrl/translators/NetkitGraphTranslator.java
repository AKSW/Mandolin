package org.aksw.mandolin.semsrl.translators;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aksw.mandolin.semsrl.model.ResourceGraph;
import org.aksw.mandolin.util.CustomQuoteMode;
import org.apache.commons.codec.digest.DigestUtils;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.opencsv.CSVWriter;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class NetkitGraphTranslator extends GraphTranslator {

	private static final String name = "netkit";
	
	public NetkitGraphTranslator(ResourceGraph graph) {
		super(graph, name);
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public void translate() throws IOException {
		
//		CSVWriter nodeWriter = new CSVWriter(new FileWriter(basepath+"/nodefile.csv"), ',');
		
		final CsvPreference CUSTOM_PREF = 
			    new CsvPreference.Builder(CsvPreference.STANDARD_PREFERENCE).useQuoteMode(new CustomQuoteMode()).build();
		CsvListWriter nodeWriter = new CsvListWriter(new FileWriter(basepath+"/nodefile.csv"), CUSTOM_PREF);
		
		
		BufferedWriter schema = new BufferedWriter(new FileWriter(basepath+"/schema.arff"));
		
		// map a subject to another hash map, which maps a property to its value
		HashMap<String, HashMap<String, String>> nodemap = new HashMap<>();
		
		// all the properties, sorted
		ArrayList<String> properties = new ArrayList<>();
		HashMap<String, CSVWriter> edgefiles = new HashMap<>();
		HashMap<String, String> edgefilenames = new HashMap<>();
		
		for(Statement link : graph.getLinks()) {
			
			RDFNode sub = link.getSubject();
			
			String s;
			if(sub.isAnon()) {
				s = "http://aksw.org/Groups/SIMBA/SemSRL/blanknode/BN" + DigestUtils.shaHex(sub.toString());
				if(!nodemap.containsKey(s)) {
					// object is new
					nodemap.put(s, new HashMap<String, String>());
					System.out.println("Reificating blank node: "+sub+" --> "+s);
				}
			} else {
				s = sub.asResource().getURI();
				// all subjects are nodes
				if(!nodemap.containsKey(s))
					// subject is new
					nodemap.put(s, new HashMap<String, String>());
			}
			HashMap<String, String> h = nodemap.get(s);
			
			String p = link.getPredicate().getURI();
			RDFNode o = link.getObject();
			
			System.out.println("TRIPLE: "+link);
			
			// save property
			if(!properties.contains(p)) {
				properties.add(p);
				String filename = "edge-" + DigestUtils.shaHex(p) + ".rn";
				edgefilenames.put(p, filename);
				edgefiles.put(p, new CSVWriter(new FileWriter(
						basepath+"/" + filename), ',', CSVWriter.NO_QUOTE_CHARACTER, '"'));
//						basepath+"/edge-" + clean(p) + ".rn"), ','));
			}
			
			// manage objects
			if(o instanceof Resource) {
				// object is a node
				if(o.isAnon()) {
					// object is a blank node
					String bUri = "http://aksw.org/Groups/SIMBA/SemSRL/blanknode/BN" + DigestUtils.shaHex(o.toString());
					if(!nodemap.containsKey(bUri)) {
						// object is new
						nodemap.put(bUri, new HashMap<String, String>());
						System.out.println("Reificating blank node: "+o+" --> "+bUri);
					}
					String save = s + "\t" + bUri + "\t" + 1;
					edgefiles.get(p).writeNext(save.split("\t"));
				} else {
					// object is a real entity
					String oUri = o.asResource().getURI();
					if(!nodemap.containsKey(oUri))
						// object is new
						nodemap.put(oUri, new HashMap<String, String>());
					// save to edge file
					String save = s + "\t" + oUri + "\t" + 1;
					edgefiles.get(p).writeNext(save.split("\t"));
				}
			} else if(o instanceof Literal) {
				// object is an attribute (literal)
				Literal oLit = o.asLiteral();
//				switch(oLit.getDatatype().getURI()) {
//				// TODO categorical / continuous
				// TODO also: let CSVCrawler identify data types
//				default:
					h.put(p, oLit.getString());
//				}
			} else {
				System.out.println("Object not added: "+o);
			}
			
		}
		
		System.out.println("MAP: "+nodemap);
		
//		System.out.println("saving map to .csv file");
//		for(String s : nodemap.keySet()) {
//			String[] entries = new String[properties.size() + 1];
//			entries[0] = s;
//			HashMap<String, String> h = nodemap.get(s);
//			for(int i=0; i<properties.size(); i++) {
//				String str = h.get(properties.get(i));
//				entries[i+1] = (str == null) ? "null" : str;
//			}
//			nodeWriter.writeNext(entries);
//		}

		List<CellProcessor> fields = new ArrayList<CellProcessor>();
		for(int i=0; i<properties.size() + 1; i++)
			fields.add(new NotNull());
		
		CellProcessor[] processors = fields.toArray(new CellProcessor[0]);
		
		System.out.println("saving map to .csv file");
		for(String s : nodemap.keySet()) {
			List<String> entries = new ArrayList<String>();
			entries.add(s);
			HashMap<String, String> h = nodemap.get(s);
			for(String p : properties) {
				String str = h.get(p);
				if(str == null)
					entries.add("null");
				else
					entries.add(str);
			}
			nodeWriter.write(entries, processors);
		}

		schema
			.append("@nodetype RDFNode\n")
			.append("@attribute Uri KEY\n");
		for(String p : properties)
			schema.append("@attribute Attr"+clean(p)+" CATEGORICAL\n");
		schema.append("@nodedata nodefile.csv\n\n");

		nodeWriter.close();
		
		for(String p : properties) {
			edgefiles.get(p).close();
			schema
				.append("@edgetype "+clean(p)+" RDFNode RDFNode\n")
				.append("@reversible\n")
				.append("@edgedata "+edgefilenames.get(p)+"\n\n");
		}
		schema.close();
	}

// ================================================================================================
	
//	public void translate() throws IOException {
//		
//		CSVWriter nodeWriter = new CSVWriter(new FileWriter(basepath+"/nodefile.csv"), ',');
//		
//		BufferedWriter schema = new BufferedWriter(new FileWriter(basepath+"/schema.arff"));
//		
//		// map a subject to another hash map, which maps a property to its value
//		HashMap<String, HashMap<String, String>> nodemap = new HashMap<>();
//		
//		// all the properties, sorted
//		ArrayList<String> properties = new ArrayList<>();
//		HashMap<String, CSVWriter> edgefiles = new HashMap<>();
//		HashMap<String, String> edgefilenames = new HashMap<>();
//		
//		for(Statement link : graph.getLinks()) {
//			
//			RDFNode sub = link.getSubject();
//			
//			String s;
//			if(sub.isAnon()) {
//				s = "http://aksw.org/Groups/SIMBA/SemSRL/blanknode/BN" + DigestUtils.sha1Hex(sub.toString());
//				if(!nodemap.containsKey(s)) {
//					// object is new
//					nodemap.put(s, new HashMap<String, String>());
//					System.out.println("Reificating blank node: "+sub+" --> "+s);
//				}
//			} else {
//				s = sub.asResource().getURI();
//				// all subjects are nodes
//				if(!nodemap.containsKey(s))
//					// subject is new
//					nodemap.put(s, new HashMap<String, String>());
//			}
//			HashMap<String, String> h = nodemap.get(s);
//			
//			String p = link.getPredicate().getURI();
//			RDFNode o = link.getObject();
//			
//			System.out.println("TRIPLE: "+link);
//			
//			// save property
//			if(!properties.contains(p)) {
//				properties.add(p);
//				String filename = "edge-" + DigestUtils.sha1Hex(p) + ".rn";
//				edgefilenames.put(p, filename);
//				edgefiles.put(p, new CSVWriter(new FileWriter(
//						basepath+"/" + filename), ','));
////						basepath+"/edge-" + clean(p) + ".rn"), ','));
//			}
//			
//			// manage objects
//			if(o instanceof Resource) {
//				// object is a node
//				if(o.isAnon()) {
//					// object is a blank node
//					String bUri = "http://aksw.org/Groups/SIMBA/SemSRL/blanknode/BN" + DigestUtils.sha1Hex(o.toString());
//					if(!nodemap.containsKey(bUri)) {
//						// object is new
//						nodemap.put(bUri, new HashMap<String, String>());
//						System.out.println("Reificating blank node: "+o+" --> "+bUri);
//					}
//					String save = s + "\t" + bUri + "\t" + 1;
//					edgefiles.get(p).writeNext(save.split("\t"));
//				} else {
//					// object is a real entity
//					String oUri = o.asResource().getURI();
//					if(!nodemap.containsKey(oUri))
//						// object is new
//						nodemap.put(oUri, new HashMap<String, String>());
//					// save to edge file
//					String save = s + "\t" + oUri + "\t" + 1;
//					edgefiles.get(p).writeNext(save.split("\t"));
//				}
//			} else if(o instanceof Literal) {
//				// object is an attribute (literal)
//				Literal oLit = o.asLiteral();
////				switch(oLit.getDatatype().getURI()) {
////				// TODO categorical / continuous
//				// TODO also: let CSVCrawler identify data types
////				default:
//					h.put(p, oLit.getString());
////				}
//			} else {
//				System.out.println("Object not added: "+o);
//			}
//			
//		}
//		
//		System.out.println("MAP: "+nodemap);
//		
//		System.out.println("saving map to .csv file");
//		for(String s : nodemap.keySet()) {
//			String[] entries = new String[properties.size() + 1];
//			entries[0] = s;
//			HashMap<String, String> h = nodemap.get(s);
//			for(int i=0; i<properties.size(); i++) {
//				String str = h.get(properties.get(i));
//				entries[i+1] = (str == null) ? "null" : str;
//			}
//			nodeWriter.writeNext(entries);
//		}
//
//		schema
//			.append("@nodetype RDFNode\n")
//			.append("@attribute Uri KEY\n");
//		for(String p : properties)
//			schema.append("@attribute Attr"+clean(p)+" CATEGORICAL\n");
//		schema.append("@nodedata nodefile.csv\n\n");
//
//		nodeWriter.close();
//		for(String p : properties) {
//			edgefiles.get(p).close();
//			schema
//				.append("@edgetype "+clean(p)+" RDFNode RDFNode\n")
//				.append("@reversible\n")
//				.append("@edgedata "+edgefilenames.get(p)+"\n\n");
//		}
//		schema.close();
//	}

// ================================================================================================
	
//	public static Graph translate(ResourceGraph rg) {
//		Graph g = new Graph();
//		List<Statement> links = rg.getLinks();
//		// index nodes
//		HashMap<String, Node> nodes = new HashMap<>();
//		HashMap<String, EdgeType> edges = new HashMap<>();
//		Attributes type = new Attributes("RDFNode");
//		g.addAttributes(type);
//		int index = 0;
//		for(Statement l : links) {
//			String s = l.getSubject().getURI();
//			Node n1, n2;
//			if(nodes.get(s) == null) {
//				n1 = new Node(s, type, index);
//				nodes.put(s, n1);
//				index++;
//			} else {
//				n1 = nodes.get(s);
//			}
//			String p = l.getPredicate().getURI();
//			EdgeType e;
//			if(edges.get(p) == null) {
//				e = new EdgeType(p, type.getName(), type.getName());
//				g.addEdgeType(e);
//				edges.put(p, e);
//			} else {
//				e = edges.get(p);
//			}
//			String o = l.getObject().getClass().getName();
//			if(nodes.get(o) == null) {
//				// TODO specify type here (resource | datatype)
//				n2 = new Node(o, type, index);
//				nodes.put(o, n2);
//				index++;
//			} else {
//				n2 = nodes.get(o);
//			}
//
//			g.addEdge(e, n1, n2, 1.0);
//			
//		}
//		return g;
//	}

	private String clean(String c) {
		Pattern pt = Pattern.compile("[^a-zA-Z0-9]");
		Matcher match = pt.matcher(c);
		while(match.find())
			c = c.replaceAll("\\"+match.group(), "");
        return c;
	}
	
}
