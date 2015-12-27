package org.aksw.mandolin;

import java.util.TreeSet;

import org.aksw.mandolin.NameMapper.Type;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDF;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class OntoImporter {
	
	/**
	 * @param map 
	 * @param SRC_PATH
	 * @param TGT_PATH
	 */
	public static void run(final String BASE, final String[] paths) {
		
		final CollectionCache properties = new CollectionCache();
		final CollectionCache classes = new CollectionCache();
		
		// reader implementation
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
				String s = arg0.getSubject().getURI();
				String p = arg0.getPredicate().getURI();
				String o = arg0.getObject().toString();

				// if property is rdf:type...
				if(p.equals(RDF.type.getURI())) {
					// save class
					// TODO this could be extended to all properties with domain or range = rdfs:Class
					classes.set.add(o);
				}
				// save property
				properties.set.add(p);
				
			}

		};

		// FIXME here goes a foreach (perhaps swap with Validator)
		RDFDataMgr.parse(dataStream, BASE + "/model.nt");
		
		// TODO
		// visit URIs in classes and properties
		// save wanted part of RDF files
		// append NT files to model...
				
	}
	
}

class CollectionCache {
	TreeSet<String> set = new TreeSet<>();
}