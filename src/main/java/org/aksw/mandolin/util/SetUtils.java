package org.aksw.mandolin.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.TreeSet;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class SetUtils {

	private final static Logger logger = LogManager.getLogger(SetUtils.class);
	
	/**
	 * Set difference.
	 * 
	 * @param setA
	 * @param setB
	 * @param output
	 */
	public static void minus(String setA, String setB, String output) {
		
		TreeSet<String> setBindex = new TreeSet<>();
		StreamRDF bStream = new StreamRDF() {
	
			@Override
			public void start() {
			}
	
			@Override
			public void triple(Triple triple) {
				setBindex.add(triple.toString());
			}
	
			@Override
			public void quad(Quad quad) {
			}
	
			@Override
			public void base(String base) {
			}
	
			@Override
			public void prefix(String prefix, String iri) {
			}
	
			@Override
			public void finish() {
			}
			
		};
		RDFDataMgr.parse(bStream, setB);
		
		final FileOutputStream out;
		final StreamRDF outStream;
		try {
			out = new FileOutputStream(new File(output));
			outStream = StreamRDFWriter.getWriterStream(out, Lang.NT);
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage());
			return;
		}
		
		outStream.start();
		
		StreamRDF aStream = new StreamRDF() {
	
			@Override
			public void start() {
			}
	
			@Override
			public void triple(Triple triple) {
				boolean trivial = triple.getPredicate().hasURI(RDF.type.getURI()) 
						&& triple.getObject().hasURI(OWL.Thing.getURI());
				boolean known = setBindex.contains(triple.toString());
				if(!known && !trivial) // save discovered triple
					outStream.triple(triple);
				logger.trace("\tknown=" + known + "\ttrivial=" + trivial + "\t" + triple.toString());
			}
			@Override
			public void quad(Quad quad) {
			}
	
			@Override
			public void base(String base) {
			}
	
			@Override
			public void prefix(String prefix, String iri) {
			}
	
			@Override
			public void finish() {
			}
			
		};
		RDFDataMgr.parse(aStream, setA);
		
		outStream.finish();
		
	}

	/**
	 * @param setA
	 * @param setB
	 * @param output
	 */
	public static void union(String setA, String setB, String output) {
		
		final FileOutputStream out;
		final StreamRDF outStream;
		try {
			out = new FileOutputStream(new File(output));
			outStream = StreamRDFWriter.getWriterStream(out, Lang.NT);
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage());
			return;
		}
		
		outStream.start();
		
		StreamRDF dataStream = new StreamRDF() {
	
			@Override
			public void start() {
			}
	
			@Override
			public void triple(Triple triple) {
				outStream.triple(triple);
			}
	
			@Override
			public void quad(Quad quad) {
			}
	
			@Override
			public void base(String base) {
			}
	
			@Override
			public void prefix(String prefix, String iri) {
			}
	
			@Override
			public void finish() {
			}
			
		};
		
		RDFDataMgr.parse(dataStream, setA);
		RDFDataMgr.parse(dataStream, setB);
		
		outStream.finish();
		
	}

	public static void keepOnly(String relation, String in,
			String out) {
	
		final FileOutputStream output;
		try {
			output = new FileOutputStream(new File(out));
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage());
			return;
		}
		
		final StreamRDF writer = StreamRDFWriter.getWriterStream(output, Lang.NT);		
		
		StreamRDF dataStream = new StreamRDF() {
	
			@Override
			public void start() {
				writer.start();
			}
	
			@Override
			public void quad(Quad quad) {
			}
	
			@Override
			public void base(String base) {
			}
	
			@Override
			public void prefix(String prefix, String iri) {
			}
	
			@Override
			public void finish() {
				writer.finish();
			}
			
			@Override
			public void triple(Triple triple) {
				if(triple.getPredicate().getURI().equals(relation))
					writer.triple(triple);
			}
			
		};
		
		RDFDataMgr.parse(dataStream, in);		
	}

}
