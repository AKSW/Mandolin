package org.aksw.mandolin.semantifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.aksw.mandolin.util.DataIO;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class DatasetBuildStarter {

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		new DatasetBuildStarter().run();
	}

	public void run() throws IOException, ClassNotFoundException {
		
		ArrayList<Elements> data = new ArrayList<>();

		for (String rkbURI : getRKBURIs()) {

			Elements e = getElements(rkbURI, Commons.DC_CREATOR.getURI(), Commons.DBLPL3S_ENDPOINT);

			System.out.println(e.getURI());
			
			if(e.getURI() != null)
				data.add(e);

		}
		
		DataIO.serialize(data, Commons.PUBS_WITH_AUTHORS_MAP);		

	}

	/**
	 * Get publication URIs from the perfect mapping.
	 * 
	 * @return
	 * @throws FileNotFoundException
	 */
	private ArrayList<String> getRKBURIs() throws FileNotFoundException {

		ArrayList<String> list = new ArrayList<>();

		Scanner in = new Scanner(new File(Commons.DBLP_ACM_FIXED_CSV));
		in.nextLine(); // skip header
		while (in.hasNextLine()) {
			String[] line = in.nextLine().split(",");
			String rkb = line[0].replaceAll("\"", "");
			list.add(Commons.DBLP_NAMESPACE + rkb);
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

		String query = "SELECT ?cr ?pub WHERE { ?pub <"+Commons.OWL_SAMEAS+"> <"
				+ pubURI + "> ; <" + elementRel + "> ?cr }";
//		System.out.println(query);
		
		ResultSet rs = Commons.sparql(query, endpoint);

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
