package org.aksw.mandolin.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.TreeSet;

import org.aksw.mandolin.controller.NameMapper;
import org.aksw.mandolin.controller.NameMapper.Type;
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
		
		double max = Double.MIN_VALUE;
		double min = Double.MAX_VALUE;
		for (PredictionLiteral lit : this) {
			if(lit.getProb() > max)
				max = lit.getProb();
			if(lit.getProb() < min)
				min = lit.getProb();
		}
		System.out.println("max = "+max+", min = "+min);
		double delta = max - min;

//		int inf = 0;
		System.out.println("+++ INFERRED +++");
		for (PredictionLiteral lit : this) {
			
			// filter only aim relation from pset
			String p = map.getURI(lit.getP());
			if (!p.equals(aim))
				continue;
			
			// relative value for probability
			double relprob = (lit.getProb() - min) / delta;
				
			if (relprob >= theta) {
				String prob = String.valueOf(lit.getProb());
				System.out.println(lit + " (" + relprob + ")");
				String s = map.getURI(lit.getX());
				if(s == null) {
					int a = NameMapper.parse(lit.getX());
					String str = String.valueOf(-a);
					s = map.getURI(Type.CLASS.name() + str);
				}
				
				// filter out illegal triples...
				try {
					new URI(s);
				} catch (URISyntaxException e) {
					System.out.println("WARNING: A predicted triple has a subject "
							+ "(" + s + ") which is not an URI. Skipping triple...");
					continue;
				}
				
				String o = map.getURI(lit.getY());
				if(o == null) {
					int b = NameMapper.parse(lit.getY());
					String str = String.valueOf(-b);
					o = map.getURI(Type.CLASS.name() + str);
				}
				Triple t = new Triple(NodeFactory.createURI(s),
						NodeFactory.createURI(p), NodeFactory.createURI(o));
				System.out.println(prob + "\t" + t);
				writer.triple(t);
			}
		}
		
		writer.finish();

	}

}
