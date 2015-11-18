package org.aksw.mandolin.amie;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.aksw.mandolin.MandolinProbKB;
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
public class RDFToTab {

	public static void main(String[] args) throws FileNotFoundException {

		run("tmp/DBLPACM.tsv", MandolinProbKB.SRC_PATH,
				MandolinProbKB.TGT_PATH, MandolinProbKB.GOLD_STANDARD_PATH);

	}

	public static void run(String outputFile, String... inputFile)
			throws FileNotFoundException {

		PrintWriter pw = new PrintWriter(new File(outputFile));

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

		for (String input : inputFile) {
			RDFDataMgr.parse(stream, input);
		}

		pw.close();

	}

}
