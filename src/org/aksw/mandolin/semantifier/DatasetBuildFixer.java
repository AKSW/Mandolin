package org.aksw.mandolin.semantifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Scanner;
import java.util.TreeSet;

import org.simmetrics.metrics.Levenshtein;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

/**
 * Removes faulty mappings from the gold standard, e.g. when the authors cannot
 * be linked because one of them is missing in one dataset.
 * 
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class DatasetBuildFixer {

	public static void main(String[] args) throws IOException {
		
//		System.out.println(new Levenshtein().distance("Query Execution Techniques for Caching Expensive Methods.", "2Q"));
		
		new DatasetBuildFixer().run();
		new DatasetBuildFixer().fix();
	}

	public void fix() throws IOException {
		
		TreeSet<String> ids = new TreeSet<>();
		Scanner in = new Scanner(new File("tmp/to-be-deleted-id.txt"));
		while (in.hasNextLine())
			ids.add(in.nextLine());
		in.close();
		
		System.out.println("-----------\n"+ids);
		
		CSVReader reader = new CSVReader(new FileReader(new File("mappings/dblp-acm.csv")));
		CSVWriter writer = new CSVWriter(new FileWriter(new File("mappings/dblp-acm-fixed.csv")));
		CSVWriter removed = new CSVWriter(new FileWriter(new File("old/removed-publications.csv")));
		String[] nextLine = reader.readNext();
		writer.writeNext(nextLine);
		removed.writeNext(nextLine);
		while ((nextLine = reader.readNext()) != null) {
			if(ids.contains(nextLine[1])) {
				removed.writeNext(nextLine);
				System.out.println("Removed: "+nextLine[0]+" | "+nextLine[1]);
			} else
				writer.writeNext(nextLine);
		}
		removed.close();
		writer.close();
		reader.close();
		
	}

	public void run() throws FileNotFoundException {
		
		TreeSet<String> blacklist = new TreeSet<>();
		PrintWriter pw = new PrintWriter(new File("tmp/to-be-deleted-id.txt"));
		
		// get list of faulty authors
		TreeSet<String> pairs = new TreeSet<>();
		Scanner in = new Scanner(new File("tmp/to-be-deleted.txt"));
		while (in.hasNextLine())
			pairs.add(in.nextLine());
		in.close();

		for (String pair : pairs) {
			String dblp = pair.split(",")[0];
			String acm = pair.split(",")[1];
			
			System.out.println(dblp+" | "+acm);

			// query for DBLP-L3S publications
			HashMap<String, String> dblpLabelToURI = new HashMap<>();
			ResultSet rs1 = DatasetBuilder.sparql(
					"select ?p ?t where { ?p <http://purl.org/dc/elements/1.1/creator> <" + dblp
							+ "> . ?p <http://www.w3.org/2000/01/rdf-schema#label> ?t }",
					"http://dblp.l3s.de/d2r/sparql");
			while(rs1.hasNext()) {
				QuerySolution qs = rs1.next();
				dblpLabelToURI.put(qs.getLiteral("t").getString(), qs.getResource("p").getURI());
			}

			// query for ACM publications
			HashMap<String, String> acmLabelToURI = new HashMap<>();
			ResultSet rs2 = DatasetBuilder.sparql(
					"select ?p ?t where { ?p <http://www.aktors.org/ontology/portal#has-author> <" + acm
							+ "> . ?p <http://www.aktors.org/ontology/portal#has-title> ?t }",
					"http://localhost:8890/sparql");
			while(rs2.hasNext()) {
				QuerySolution qs = rs2.next();
				acmLabelToURI.put(qs.getLiteral("t").getString(), qs.getResource("p").getURI());
			}
			
			// Round-Robin among labels, checking also for substrings (e.g., to cut off undertitles)
			float dMin = Float.MAX_VALUE, dMinSub = Float.MAX_VALUE;
			String l1min = null, l2min = null, l1minSub = null, l2minSub = null;
			Levenshtein lev = new Levenshtein();
			for(String l1 : dblpLabelToURI.keySet()) {
				for(String l2 : acmLabelToURI.keySet()) {
					float d = lev.distance(l1.toLowerCase(), l2.toLowerCase());
					if(d < dMin) {
						dMin = d;
						l1min = l1;
						l2min = l2;
					}
					for(int i=0; i<l1.length(); i++) {
						float dd = lev.distance(l1.substring(0, i).toLowerCase(), l2.toLowerCase());
						if(dd < dMinSub) {
							dMinSub = dd;
							l1minSub = l1;
							l2minSub = l2;
						}
					}
					for(int i=0; i<l2.length(); i++) {
						float dd = lev.distance(l1.toLowerCase(), l2.substring(0, i).toLowerCase());
						if(dd < dMinSub) {
							dMinSub = dd;
							l1minSub = l1;
							l2minSub = l2;
						}
					}
				}
			}
			// give more importance to full string comparison
			if(dMin > 2.0) {
				System.out.println("Using substring comparison (dMin = "+dMin+")");
				dMin = dMinSub;
				l1min = l1minSub;
				l2min = l2minSub;
			}
				
			
			// add publications to the blacklist
			System.out.println("DISTANCE = " + dMin + "\n" + l1min + "\n" + l2min);
			String l2URI = acmLabelToURI.get(l2min);
			System.out.println("URI: "+l2URI + "\n");
			blacklist.add(l2URI.substring(l2URI.lastIndexOf("/") + 1));
			
//			break;
		}
		
		System.out.println(blacklist);
		for(String id : blacklist)
			pw.write(id+"\n");
		pw.close();
	}

}
