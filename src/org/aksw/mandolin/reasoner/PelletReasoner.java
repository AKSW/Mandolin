package org.aksw.mandolin.reasoner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Iterator;

import org.aksw.mandolin.util.Timer;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ValidityReport;
import com.hp.hpl.jena.shared.Lock;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class PelletReasoner {

	public static void main(String[] args) {
		// usageWithDefaultModel();
		run("eval/0001");
	}

	/**
	 * Add OWL rules and compute the forward chain.
	 * 
	 * @param base
	 * @param datasetPaths
	 */
	public static void run(String base) {

		Reasoner reasoner = PelletReasonerFactory.theInstance().create();
		OntModel emptyModel = ModelFactory
				.createOntologyModel(PelletReasonerFactory.THE_SPEC);
		InfModel model = ModelFactory.createInfModel(reasoner, emptyModel);

		String path = System.getProperty("user.dir");
		RDFDataMgr.read(model, "file://" + path + "/" + base + "/model.nt");

		ValidityReport report = model.validate();
		printIterator(report.getReports(), "Validation Results");

		model.enterCriticalSection(Lock.READ);

		try {
			RDFDataMgr.write(
					new FileOutputStream(new File(base + "/model-fwc.nt")), model,
					Lang.NT);
			System.out.println("Model generated.");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
		    model.leaveCriticalSection() ;
		}


	}

	public static void usageWithDefaultModel() {
		System.out.println("Results with OWL Model");
		System.out.println("----------------------------");
		System.out.println();

		Timer t = new Timer();
		// ontology that will be used
		// String ns = "http://www.aktors.org/ontology/portal#";

		// create Pellet reasoner
		Reasoner reasoner = PelletReasonerFactory.theInstance().create();

		// create an empty model
		OntModel emptyModel = ModelFactory
				.createOntologyModel(PelletReasonerFactory.THE_SPEC);

		// create an inferencing model using Pellet reasoner
		InfModel model = ModelFactory.createInfModel(reasoner, emptyModel);

		// read the file
		// model.read

		t.lap();

		String path = System.getProperty("user.dir");

		RDFDataMgr.read(model, "file://" + path + "/datasets/DBLPL3S.nt");
		RDFDataMgr.read(model, "file://" + path + "/datasets/LinkedACM.nt");
		RDFDataMgr.read(model, "file://" + path
				+ "/linksets/DBLPL3S-LinkedACM.nt");

		t.lap();

		// print validation report
		ValidityReport report = model.validate();

		t.lap();

		// print superclasses
		// Resource c = model
		// .getResource("http://dblp.l3s.de/d2r/resource/publications/conf/sigmod/BohmBKK01");
		// printIterator(model.listObjectsOfProperty(c, OWL.sameAs),
		// "All sameAs of " + c.getLocalName());

		System.out.println();

		t.lap();

		System.out.println("Reasoner init: " + t.getLapMillis(0));
		System.out.println("Model load: " + t.getLapMillis(1));
		System.out.println("Model load (per triple): " + t.getLapMillis(1)
				/ model.size());
		System.out.println("Validation: " + t.getLapMillis(2));
		System.out.println("Query for 1 resource: " + t.getLapMillis(3));
		printIterator(report.getReports(), "Validation Results");

	}

	private static void printIterator(Iterator<?> i, String header) {
		System.out.println(header);

		if (i.hasNext()) {
			while (i.hasNext())
				System.out.println(i.next());
		} else
			System.out.println("<EMPTY>");

		System.out.println();
	}

}
