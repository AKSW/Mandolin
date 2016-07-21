package org.aksw.mandolin.semantifier;

import java.util.HashMap;
import java.util.TreeSet;

import org.aksw.mandolin.common.MandolinCommon;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDF;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.sparql.core.Quad;

/**
 * Given a discovery link (the target link to be discovered), build its
 * reflexive, symmetrical and transitive closure for the gold standard.
 * 
 * @deprecated Now the closure is implemented in Mandolin through forward-chain
 *             inference using a reasoner.
 * 
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
@Deprecated
public class DatasetBuildClosure {

	private static final Property DISCOVERY_LINK = Commons.OWL_SAMEAS;

	private final HashMap<String, TreeSet<String>> closure = new HashMap<>();

	public static void main(String[] args) {

		new DatasetBuildClosure().runReflSymmTransClosure();

	}

	public void runReflSymmTransClosure() {

		// add from evidence

		StreamRDF dataStream = new StreamRDF() {

			@Override
			public void base(String arg0) {
			}

			@Override
			public void finish() {
			}

			@Override
			public void prefix(String arg0, String arg1) {
			}

			@Override
			public void quad(Quad arg0) {
			}

			@Override
			public void start() {
			}

			@Override
			public void triple(Triple arg0) {

				// collect triple into index
				if (arg0.getPredicate().getURI()
						.equals(DISCOVERY_LINK.getURI())) {
					String s = arg0.getSubject().getURI();
					String o = arg0.getObject().toString();
					TreeSet<String> arrS;
					if (closure.containsKey(s))
						arrS = closure.get(s);
					else {
						arrS = new TreeSet<>();
						closure.put(s, arrS);
						arrS.add(s);
					}
					arrS.add(o);
					TreeSet<String> arrO;
					if (closure.containsKey(o))
						arrO = closure.get(o);
					else {
						arrO = new TreeSet<>();
						closure.put(o, arrO);
						arrO.add(o);
					}
					arrO.add(s);
				}

			}

		};

		RDFDataMgr.parse(dataStream, Commons.DBLPL3S_NT);
		RDFDataMgr.parse(dataStream, Commons.LINKEDACM_NT);
		RDFDataMgr.parse(dataStream, Commons.DBLPL3S_LINKEDACM_NT);

		// clone closure object...
		HashMap<String, TreeSet<String>> closureClone = new HashMap<>();
		for (String keyURI : closure.keySet())
			closureClone.put(keyURI, new TreeSet<>(closure.get(keyURI)));
		// ...to compute closure of the index
		for (String keyURI : closure.keySet()) {
			for (String valURI : closure.get(keyURI)) {
				for (String valOfVal : closure.get(valURI)) {
					closureClone.get(keyURI).add(valOfVal);
				}
			}
		}

		Model m = ModelFactory.createDefaultModel();

		for (String keyURI : closureClone.keySet()) {
			System.out.println(keyURI + "\t" + closureClone.get(keyURI));
			Resource key = ResourceFactory.createResource(keyURI);
			for (String valURI : closureClone.get(keyURI)) {
				Resource val = ResourceFactory.createResource(valURI);
				m.add(key, DISCOVERY_LINK, val);
			}
		}
		Commons.save(m, MandolinCommon.GOLD_STANDARD_PATH);

	}
}
