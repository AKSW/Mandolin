package org.aksw.mandolin.amie;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import javatools.datatypes.ByteString;

import org.aksw.mandolin.MandolinProbKB;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDF;

import amie.mining.AMIE;
import amie.rules.Rule;

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

		String output = "tmp/DBLPACM.tsv";

		run(output, MandolinProbKB.SRC_PATH, MandolinProbKB.TGT_PATH,
				MandolinProbKB.GOLD_STANDARD_PATH);

		AmieHandler h = new AmieHandler(output);
		h.run();
		
		RuleDriver driver = new RuleDriver();
		for(Rule rule : h.getRules()) {
			// send rule to driver
			driver.process(rule);
			// print rule information
			String str = "";
			for(ByteString[] bs : rule.getBody()) {
				String bstr = "";
				for(ByteString b : bs)
					bstr += b + ",";
				str += bstr + " | ";
			}
			System.out.println(rule.getHeadRelation() + "\t" + str + "\t" + rule.getPcaConfidence());
		}

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
