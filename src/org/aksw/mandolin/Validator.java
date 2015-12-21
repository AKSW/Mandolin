package org.aksw.mandolin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class Validator {

	public static void run(String base, String[] input) {
		
		final FileOutputStream output;
		try {
			output = new FileOutputStream(new File(base + "/model-fwc.nt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
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
				if(triple.getObject().isLiteral()) {
					Node node = triple.getObject();
					if(!node.getLiteral().isWellFormed()) {
						System.out.println("Discarding non-well-formed literal: "+node);
						return;
					}
				}
				writer.triple(triple);
			}
			
		};
		
		for(String path : input)
			RDFDataMgr.parse(dataStream, path);
		
	}
	
	

}
