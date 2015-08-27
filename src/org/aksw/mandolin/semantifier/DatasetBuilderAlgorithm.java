package org.aksw.mandolin.semantifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.TreeSet;

import org.aksw.mandolin.util.DataIO;
import org.simmetrics.metrics.Levenshtein;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class DatasetBuilderAlgorithm {

	public static void main(String[] args) throws FileNotFoundException,
			ClassNotFoundException, IOException {
		new DatasetBuilderAlgorithm().run();
	}

	public void run() throws FileNotFoundException, ClassNotFoundException,
			IOException {

		// load DBLP l3s to ACM rkb
		HashMap<String, String> l3sMap = l3sToACMRkb();

		// build reverse map
		HashMap<String, TreeSet<String>> map = new HashMap<>();

		ArrayList<Elements> data = DataIO
				.readList(Commons.PUBS_WITH_AUTHORS_MAP);
		for (Elements e : data) {
			// // TODO remove me!
			// if(!l3sMap.containsKey(e.getURI()))
			// continue;

			for (String el : e.getElements()) {
				TreeSet<String> pubSet;
				if (map.containsKey(el))
					pubSet = map.get(el);
				else {
					pubSet = new TreeSet<>();
					map.put(el, pubSet);
				}
				pubSet.add(e.getURI());
			}
		}

		HashMap<String, ArrayList<String>> sameAsMap = new HashMap<>();

		PrintWriter pw = new PrintWriter(new File(Commons.DISTANCES_CSV));

		// algorithm starts here
		for (String author : map.keySet()) {

			String authorName = getName(author);

			System.out.println("Listing " + authorName + " (" + author + "): "
					+ map.get(author));

			TreeSet<String> sameAs = new TreeSet<>();

			for (String l3s : map.get(author)) {

				System.out.println("L3S: " + l3s);

				String acmRkb = l3sMap.get(l3s);

				float distMin = Float.MAX_VALUE;
				Entity entity = null;

				ArrayList<Entity> rkb = getCreators(acmRkb);
				for (Entity e : rkb) {
					Levenshtein lev = new Levenshtein();
					float d = lev.distance(authorName, e.getLabel());
					if (d <= distMin) {
						distMin = d;
						entity = e;
					}
					System.out.println("d(" + authorName + ", " + e.getLabel()
							+ ") = " + d);
				}

				if (entity == null) {
					System.out.println("URI " + acmRkb
							+ " is deprecated or has issues.");
					continue;
				}

				if (distMin >= 5.0)
					pw.write(authorName + "," + entity.getLabel() + ","
							+ author + "," + entity.getUri() + "\n");

				System.out.println("sameAs = " + entity.getUri());
				sameAs.add(entity.getUri());

			}

			sameAsMap.put(
					Commons.LINKEDACM_NAMESPACE + author.substring(32),
					new ArrayList<>(sameAs));

			// System.out.println(sameAsMap);
			// break;

		}

		pw.close();

		DataIO.serialize(sameAsMap, Commons.AUTHORS_SAMEAS_MAP);

	}

	private String getName(String uri) {

		String query = "SELECT * WHERE { <" + uri
				+ "> <"+Commons.RDFS_LABEL+"> ?l }";
		System.out.println(query);

		ResultSet rs = Commons.sparql(query,
				Commons.DBLPL3S_ENDPOINT);

		if (rs.hasNext()) {
			QuerySolution qs = rs.next();
			return qs.getLiteral("?l").getString();
		}

		return "";
	}

	private ArrayList<Entity> getCreators(String acmRkb) {

		String query = "SELECT DISTINCT * WHERE { <" + acmRkb + "> "
				+ "<"+Commons.HAS_AUTHOR+"> ?s . "
				+ "?s <"+Commons.FULL_NAME+"> ?l }";
		System.out.println(query);

		ResultSet rs = Commons.sparql(query, Commons.ACMRKB_ENDPOINT, Commons.ACMRKB_GRAPH);

		ArrayList<Entity> ent = new ArrayList<>();

		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			Entity e = new Entity(qs.getResource("?s").getURI(), qs.getLiteral(
					"?l").getString());
			ent.add(e);
		}

		return ent;
	}

	private HashMap<String, String> l3sToACMRkb() throws FileNotFoundException {
		HashMap<String, String> map = new HashMap<>();

		Scanner in = new Scanner(new File(Commons.DBLP_ACM_FIXED_CSV));
		in.nextLine();
		// int i = 0; // TODO remove me!
		while (in.hasNextLine()) {
			String[] line = in.nextLine().split(",");
			map.put(Commons.DBLPL3S_NAMESPACE + line[0],
					Commons.ACMRKB_NAMESPACE + line[1]);
			// if(++i == 100)
			// break;
		}
		in.close();

		return map;
	}

}

class Entity {

	String uri, label;

	Entity(String uri, String label) {
		this.uri = uri;
		this.label = label;
	}

	public String getUri() {
		return uri;
	}

	public String getLabel() {
		return label;
	}

}