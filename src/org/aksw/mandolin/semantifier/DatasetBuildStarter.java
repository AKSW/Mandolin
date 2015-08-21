package org.aksw.mandolin.semantifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.TreeSet;

import org.aksw.mandolin.util.DataIO;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class DatasetBuildStarter {

	public static final String ORIGIN_ENDPOINT = "http://dblp.l3s.de/d2r/sparql";

	public static final String ELEMENT_REL = "http://purl.org/dc/elements/1.1/creator";

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		new DatasetBuildStarter().run();
	}

	public void run() throws IOException, ClassNotFoundException {
		
		ArrayList<Elements> data = new ArrayList<>();

		for (String rkbURI : getRKBURIs()) {

			Elements e = getElements(rkbURI, ELEMENT_REL, ORIGIN_ENDPOINT);

			System.out.println(e.getURI());
			
			if(e.getURI() != null)
				data.add(e);

		}

//		write(elem);
		
		DataIO.serialize(data, "data.map");		

	}

	/**
	 * Write element (e.g., author) list to text file.
	 * 
	 * @param elem
	 * @throws FileNotFoundException
	 */
	public void write(TreeSet<String> elem) throws FileNotFoundException {
		PrintWriter pw = new PrintWriter(new File("authors.txt"));
		for(String e : elem) {
			pw.write(e+"\n");
		}
		pw.close();
	}

	/**
	 * Get publication URIs from the perfect mapping.
	 * 
	 * @return
	 * @throws FileNotFoundException
	 */
	private ArrayList<String> getRKBURIs() throws FileNotFoundException {

		ArrayList<String> list = new ArrayList<>();

		Scanner in = new Scanner(new File("mappings/dblp-acm.csv"));
		in.nextLine(); // skip header
		while (in.hasNextLine()) {
			String[] line = in.nextLine().split(",");
			String rkb = line[0].replaceAll("\"", "");
			list.add("http://dblp.rkbexplorer.com/id/" + rkb);
		}
		in.close();

		return list;
	}

	/**
	 * Get the publication associated with a list of elements (e.g., authors).
	 * 
	 * @param pubURI
	 * @param elementRel
	 * @param endpoint
	 * @return
	 */
	private Elements getElements(String pubURI, String elementRel,
			String endpoint) {

		String query = "SELECT ?cr ?pub WHERE { ?pub <http://www.w3.org/2002/07/owl#sameAs> <"
				+ pubURI + "> ; <" + elementRel + "> ?cr }";
//		System.out.println(query);
		
		ResultSet rs = DatasetBuilder.sparql(query, endpoint);

		ArrayList<String> list = new ArrayList<>();
		String uri = null;

		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			uri = qs.getResource("?pub").getURI();
			list.add(qs.getResource("?cr").getURI());
		}
		Elements elem = new Elements(uri);
		elem.setElements(list);

		return elem;

	}

}

class Elements implements Serializable {

	private static final long serialVersionUID = -4523439946804741035L;
	
	private String uri;
	private List<String> elements;

	public void setElements(List<String> elements) {
		this.elements = elements;
	}

	Elements(String uri) {
		this.uri = uri;
		elements = new ArrayList<String>();
	}

	public String getURI() {
		return uri;
	}

	public List<String> getElements() {
		return elements;
	}
}
