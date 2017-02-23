package mlnf.semantifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import mlnf.util.DataIO;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

/**
 * Build the Commons.PUBS_WITH_AUTHORS_MAP containing all DBLPL3S publications
 * with their respective authors, if any. The process could have been carried
 * out by replacing namespaces (RKBExplorer to L3S), however this is a safer way
 * to do it.
 * 
 * @author author <email>
 *
 */
public class DatasetBuildStarter {

	public static void main(String[] args) throws IOException,
			ClassNotFoundException {
		new DatasetBuildStarter().run();
	}

	public void run() throws IOException, ClassNotFoundException {

		ArrayList<Elements> data = new ArrayList<>();

		for (String rkbURI : getRKBURIs()) {

			Elements e = getElements(rkbURI, Commons.DC_CREATOR.getURI(),
					Commons.DBLPL3S_ENDPOINT, Commons.DBLPL3S_GRAPH);

			System.out.println(e.getURI());

			if (e.getURI() != null) {
				// should always happen
				data.add(e);
			}

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

		Scanner in = new Scanner(new File(Commons.DBLP_ACM_CSV));
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
	 * @param rkbURI
	 * @param relation
	 * @param endpoint
	 * @return
	 */
	private Elements getElements(String rkbURI, String relation, String endpoint, String graph) {

		String query = "SELECT ?cr ?pub WHERE { ?pub <" + Commons.OWL_SAMEAS
				+ "> <" + rkbURI + "> ; <" + relation + "> ?cr }";
		System.out.println(query);

		ResultSet rs = Commons.sparql(query, endpoint, graph);

		ArrayList<String> list = new ArrayList<>();
		String l3sURI = null;

		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			l3sURI = qs.getResource("?pub").getURI();
			list.add(qs.getResource("?cr").getURI());
		}

		Elements elem;

		if (l3sURI == null) {
			elem = getElementsNoCreator(rkbURI, relation, endpoint, graph);
		} else {
			elem = new Elements(l3sURI);
			elem.setElements(list);
		}

		return elem;

	}

	private Elements getElementsNoCreator(String rkbURI, String relation,
			String endpoint, String graph) {
		String query = "SELECT ?pub WHERE { ?pub <" + Commons.OWL_SAMEAS
				+ "> <" + rkbURI + "> }";
		System.out.println(query);

		ResultSet rs = Commons.sparql(query, endpoint, graph);

		String l3sURI = null;

		while (rs.hasNext()) {
			l3sURI = rs.next().getResource("?pub").getURI();
		}

		Elements elem = new Elements(l3sURI);
		elem.setElements(new ArrayList<>());

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
