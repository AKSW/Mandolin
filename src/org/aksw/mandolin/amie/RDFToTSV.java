package org.aksw.mandolin.amie;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDF;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;

/**
 * Generate input for AMIE.
 * 
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class RDFToTSV {

	public static void main(String[] args) throws Exception {
		
		run("eval/0001");
		
	}
	
	public static void run(String base)
			throws FileNotFoundException {

		PrintWriter pw = new PrintWriter(new File(base + "/model.tsv"));

		StreamRDF stream = new StreamRDF() {

			@Override
			public void triple(Triple triple) {
				pw.write(triple.getSubject().getURI() + "\t"
						+ triple.getPredicate().getURI() + "\t"
						+ triple.getObject().toString() + "\n");
			}

			@Override
			public void start() {
			}

			@Override
			public void quad(Quad quad) {
			}

			@Override
			public void prefix(String prefix, String iri) {
			}

			@Override
			public void finish() {
			}

			@Override
			public void base(String base) {
			}

		};

		RDFDataMgr.parse(stream, base + "/model-fwc.nt");

		pw.close();

	}

}
