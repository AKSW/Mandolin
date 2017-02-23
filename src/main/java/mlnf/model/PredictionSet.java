package mlnf.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.TreeSet;

import mlnf.controller.NameMapper;
import mlnf.controller.NameMapper.Type;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.shared.JenaException;

/**
 * @author author <email>
 *
 */
public class PredictionSet extends TreeSet<PredictionLiteral> implements
		Serializable {

	private final static Logger logger = LogManager.getLogger(PredictionSet.class);
	
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
		logger.info("Created prediction set with aim: " + aim);
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
			logger.info("Predictions saved to " + path);
		} catch (IOException e) {
			logger.warn("Cannot save " + this.toString() + ": "
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
		double delta = max - min;
		logger.debug("Normalization extrema: max = "+max+", min = "+min+", delta = "+delta);

		logger.info("Inferred triples size: "+this.size());
		for (PredictionLiteral lit : this) {
			
			// filter only aim relation from pset
			String p = map.getURI(lit.getP());
			if (!p.equals(aim) && !aim.equals("*"))
				continue;
			
			// relative value for probability
			double relprob = (lit.getProb() - min) / delta;
				
			if (relprob >= theta) {
				logger.debug(lit + " (" + relprob + ")");
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
					logger.debug("A predicted triple has a subject "
							+ "(" + s + ") which is not a URI. Skipping triple...");
					continue;
				} catch (NullPointerException e) {
					logger.debug("Error on lit.X="+lit.getX()+ " lit.Y="+lit.getY());
					continue;
				}
				
				String o = map.getURI(lit.getY());
				if(o == null) {
					int b = NameMapper.parse(lit.getY());
					String str = String.valueOf(-b);
					o = map.getURI(Type.CLASS.name() + str);
				}
				Triple t;
				try {
					t = new Triple(NodeFactory.createURI(s),
							NodeFactory.createURI(p), NodeFactory.createURI(o));
				} catch (JenaException e) {
					logger.debug("Some of the following is not a URI: s="+s+", p="+p+", o="+o);
					continue;
				}
				
				logger.debug(lit.getProb() + "\t" + t);
				
				writer.triple(t);
			}
		}
		
		writer.finish();

	}

}
