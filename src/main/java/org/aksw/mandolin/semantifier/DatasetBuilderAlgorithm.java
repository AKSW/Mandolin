package org.aksw.mandolin.semantifier;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.TreeSet;

import org.aksw.mandolin.util.DataIO;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotNotFoundException;
import org.simmetrics.metrics.Levenshtein;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class DatasetBuilderAlgorithm {
	
	private int N_EXAMPLES;
	
	public DatasetBuilderAlgorithm(int n) {
		this.N_EXAMPLES = n;
	}

	public static void main(String[] args) throws FileNotFoundException,
			ClassNotFoundException, IOException {
		new DatasetBuilderAlgorithm(100).run();
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
			System.out.println(e.getURI());
			// TODO remove me!
			if (!l3sMap.containsKey(e.getURI()))
				continue;

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

			nextPub: for (String l3s : map.get(author)) {

				System.out.println("L3S: " + l3s);

				String acmRkb = l3sMap.get(l3s);

				float distMin = Float.MAX_VALUE;
				Entity entity = null;

				ArrayList<Entity> rkb;
				final int MAX_TRIES = 3;
				int tries = 0;
				do {

					rkb = getCreators(acmRkb);
					tries++;
					for (Entity e : rkb) {
						Levenshtein lev = new Levenshtein();
						float d = lev.distance(authorName, e.getLabel());
						if (d <= distMin) {
							distMin = d;
							entity = e;
						}
						System.out.println("d(" + authorName + ", "
								+ e.getLabel() + ") = " + d);
					}

					if (entity == null) {
						System.out.println("URI " + acmRkb
								+ " is deprecated or has issues.");

						acmRkb = getRedirect(acmRkb.substring(acmRkb
								.lastIndexOf('/') + 1));
						
						if(acmRkb == null) {
							System.out.println("*** No redirects available. Skipping "+l3s);
							continue nextPub;
						}
						System.out.println("*** Redirected to: " + acmRkb);

					}

				} while (rkb.isEmpty() && tries < MAX_TRIES);
				
				if (distMin >= 5.0)
					pw.write(authorName + "," + entity.getLabel() + ","
							+ author + "," + entity.getUri() + "\n");

				System.out.println("sameAs = " + entity.getUri());
				sameAs.add(entity.getUri());

			}

			if (!sameAs.isEmpty())
				sameAsMap.put(
						Commons.LINKEDACM_NAMESPACE + author.substring(32),
						new ArrayList<>(sameAs));
			else
				System.out.println("*** " + Commons.LINKEDACM_NAMESPACE
						+ author.substring(32) + " had an empty sameAs set.");

			// System.out.println(sameAsMap);
			// break;

		}

		pw.close();

		DataIO.serialize(sameAsMap, Commons.AUTHORS_SAMEAS_MAP);

	}

	private String getRedirect(String acmID) {

		// get remote file
		String uri = Commons.ACMRKB_NAMESPACE + acmID;
		String fileIn = "http://acm.rkbexplorer.com/data/" + acmID;
		String fileOut = "tmp/" + acmID + ".rdf";
		try {
			download(fileIn, fileOut);
		} catch (IOException e) {
		}

		Model model = null;
		try {
			model = RDFDataMgr.loadModel(fileOut);
		} catch (RiotNotFoundException e) {
			// There is no information about the requested URI in this repository.
			return null;
		}
		NodeIterator it = model.listObjectsOfProperty(
				ResourceFactory.createResource(uri), Commons.OWL_SAMEAS);

		if (it.hasNext())
			return it.nextNode().asResource().getURI();

		return null;
	}

	private void download(String url, String file) throws IOException {
		URL link = new URL(url);
		InputStream in = new BufferedInputStream(link.openStream());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int n = 0;
		while (-1 != (n = in.read(buf))) {
			out.write(buf, 0, n);
		}
		out.close();
		in.close();
		byte[] response = out.toByteArray();

		FileOutputStream fos = new FileOutputStream(file);
		fos.write(response);
		fos.close();
	}

	private String getName(String uri) {

		String query = "SELECT * WHERE { <" + uri + "> <" + Commons.RDFS_LABEL
				+ "> ?l }";
		System.out.println(query);

		ResultSet rs = Commons.sparql(query, Commons.DBLPL3S_ENDPOINT, Commons.DBLPL3S_GRAPH);

		if (rs.hasNext()) {
			QuerySolution qs = rs.next();
			return qs.getLiteral("?l").getString();
		}

		return "";
	}

	private ArrayList<Entity> getCreators(String acmRkb) {

		String query = "SELECT DISTINCT * WHERE { <" + acmRkb + "> " + "<"
				+ Commons.HAS_AUTHOR + "> ?s . " + "?s <" + Commons.FULL_NAME
				+ "> ?l }";
		System.out.println(query);

		ResultSet rs = Commons.sparql(query, Commons.ACMRKB_ENDPOINT,
				Commons.ACMRKB_GRAPH);

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

		Scanner in = new Scanner(new File(Commons.DBLP_ACM_CSV));
		in.nextLine();
		int i = 0;
		while (in.hasNextLine()) {
			String[] line = in.nextLine().split(",");
			map.put(Commons.DBLPL3S_NAMESPACE + line[0].replaceAll("\"", ""),
					Commons.ACMRKB_NAMESPACE + line[1].replaceAll("\"", ""));
			if (++i == N_EXAMPLES)
				break;
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