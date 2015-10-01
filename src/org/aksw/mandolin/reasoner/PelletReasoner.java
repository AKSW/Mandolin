package org.aksw.mandolin.reasoner;

import java.util.Iterator;

import org.aksw.mandolin.util.Timer;
import org.apache.jena.riot.RDFDataMgr;
import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ValidityReport;
import com.hp.hpl.jena.vocabulary.OWL;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class PelletReasoner {

	public static void main(String[] args) {
		usageWithDefaultModel();
	}

	public static void usageWithDefaultModel() {
		System.out.println("Results with OWL Model");
		System.out.println("----------------------------");
		System.out.println();
		
		Timer t = new Timer();
		// ontology that will be used
//		String ns = "http://www.aktors.org/ontology/portal#";

		// create Pellet reasoner
		Reasoner reasoner = PelletReasonerFactory.theInstance().create();

		// create an empty model
		OntModel emptyModel = ModelFactory.createOntologyModel( PelletReasonerFactory.THE_SPEC );

		// create an inferencing model using Pellet reasoner
		InfModel model = ModelFactory.createInfModel(reasoner, emptyModel);

		// read the file
//		model.read
		
		t.lap();
		System.out.println(t.getLastLapMillis());
		
		RDFDataMgr.read(model, "file:///Users/tom/PhD/srl/Mandolin/datasets/LinkedACM-100.nt");
		RDFDataMgr.read(model, "file:///Users/tom/PhD/srl/Mandolin/datasets/DBLPL3S-pellet.nt");
		RDFDataMgr.read(model, "file:///Users/tom/PhD/srl/Mandolin/linksets/DBLPL3S-LinkedACM-100.nt");
		
		t.lap();
		System.out.println(t.getLastLapMillis());

		// print validation report
		ValidityReport report = model.validate();
		printIterator(report.getReports(), "Validation Results");
		
		t.lap();
		System.out.println(t.getLastLapMillis());
		
		// print superclasses
		Resource c = model.getResource("http://dblp.l3s.de/d2r/resource/publications/conf/sigmod/BohmBKK01");
		printIterator(model.listObjectsOfProperty(c, OWL.sameAs),
				"All sameAs of " + c.getLocalName());

		System.out.println();
		
		t.lap();
		System.out.println(t.getLastLapMillis());
		
		System.out.println("Reasoner init: "+t.getLapMillis(0));
		System.out.println("Model load: "+t.getLapMillis(1));
		System.out.println("Model load (per triple): "+t.getLapMillis(1) / model.size());
		System.out.println("Validation: "+t.getLapMillis(2));
		System.out.println("Query for 1 resource: "+t.getLapMillis(3));


	}

	public static void printIterator(Iterator<?> i, String header) {
		System.out.println(header);
		for (int c = 0; c < header.length(); c++)
			System.out.print("=");
		System.out.println();

		if (i.hasNext()) {
			while (i.hasNext())
				System.out.println(i.next());
		} else
			System.out.println("<EMPTY>");

		System.out.println();
	}

}
