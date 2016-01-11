package org.aksw.mandolin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class Validator {

	
	/**
	 * FIXME implement two different routines for enableOnt!
	 * 
	 * @param base
	 * @param input
	 * @param enableFwc
	 * @param enableOnt
	 */
	public static void run(String base, String[] input, boolean enableFwc, boolean enableOnt) {
		
		if(!enableOnt) {
			String file = enableFwc ? "model.nt" : "model-fwc.nt";
			
			final FileOutputStream output;
			try {
				output = new FileOutputStream(new File(base + "/" + file));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return;
			}
			final StreamRDF writer = StreamRDFWriter.getWriterStream(output, Lang.NT);
		}
		
		
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
				Node node = triple.getObject();
				if(node.isLiteral()) {
					if(!node.getLiteral().isWellFormed()) {
						// known issue: fix gYear literals
						if(node.getLiteralDatatypeURI() != null) {
							if(node.getLiteralDatatypeURI().equals(XSD.gYear.getURI()) || 
									node.getLiteralDatatypeURI().equals(XSD.gYear.getLocalName())) {
								Node newNode = NodeFactory.createLiteral(
										node.getLiteral().toString().substring(0, 4) + "^^" + XSD.gYear);
								triple = new Triple(triple.getSubject(), triple.getPredicate(), 
										newNode);
								System.out.println("Bad-formed literal: "+node+" - Using: "+newNode);
							}
						}
					}
				}
				if(!enableOnt)
					writer.triple(triple);
			}
			
		};
		
		if(!enableOnt) {
			for(String path : input)
				RDFDataMgr.parse(dataStream, path);
		} else {
			RDFDataMgr.parse(dataStream, base + "/model.nt");
		}
		
	}
	
	

}
