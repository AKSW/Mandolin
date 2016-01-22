package org.aksw.mandolin.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.TreeSet;

import org.aksw.mandolin.NameMapper;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class PredictionSet extends TreeSet<PredictionLiteral> implements
		Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 864082651004354757L;

	/**
	 * Internal name only.
	 */
	private String aim;

	public PredictionSet(String aim) {
		this.aim = aim;
		System.out.println("Created prediction set with aim: " + aim);
	}

	public String getAim() {
		return aim;
	}

	public void saveTo(String path) {
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(path));
			oos.writeObject(this);
			oos.close();
			System.out.println("Predictions saved to " + path);
		} catch (IOException e) {
			System.out.println("Cannot save " + this.toString() + ": "
					+ e.getMessage());
		}
	}

	public void saveLinkset(NameMapper map, double theta, String path) {

		FileOutputStream output;
		try {
			output = new FileOutputStream(new File(path));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}

		StreamRDF writer = StreamRDFWriter.getWriterStream(output, Lang.NT);
		writer.start();

		int inf = 0;
		System.out.println("+++ INFERRED +++");
		for (PredictionLiteral lit : this) {
			// filter only aim relation from pset
			String p = map.getURI(lit.getP());
			if (!p.equals(aim))
				continue;
			// when probabilities are above 1.0 (resp. below 0), they should be
			// considered as values towards +Infinity (resp. -Infinity),
			// meaning the associated triples belong to the training set as
			// positive (resp. negative) examples.
			if (lit.getProb() > 1.0 || lit.getProb() < 0.0) {
				inf++;
				continue;
			}
			if (lit.getProb() >= theta) {
				System.out.println(lit);
				String s = map.getURI(lit.getX());
				String o = map.getURI(lit.getY());
				Triple t = new Triple(NodeFactory.createURI(s),
						NodeFactory.createURI(p), NodeFactory.createURI(o));
				System.out.println(lit.getProb() + "\t" + t);
				writer.triple(t);
			}
		}
		
		System.out.println("Infinite probabilities found = "+inf);

		writer.finish();

	}

}
