package org.aksw.mandolin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.TreeSet;

import org.aksw.mandolin.NameMapper.Type;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Ontologies cannot be imported using Jena, because most datasets are not
 * actual OWL files, thus they do not provide meta-information about used and
 * imported ontologies. For instance, an N-Triples file could be using URIs
 * which are referred only in the file itself. Phisically visiting these URIs is
 * a method to retrieve their definition. In this version, we limit the URIs to
 * classes and properties.
 * 
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 * @version 0.0.1
 *
 */
public class OntoImporter {

	/**
	 * @param BASE
	 * @param paths
	 */
	public static void run(final String BASE, final String[] paths) {

		final CollectionCache properties = new CollectionCache();
		final CollectionCache classes = new CollectionCache();

		final FileOutputStream output;
		try {
			output = new FileOutputStream(new File(BASE + "/model.nt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		
		final StreamRDF writer = StreamRDFWriter.getWriterStream(output, Lang.NT);

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
			public void triple(Triple triple) {
				String s = triple.getSubject().getURI();
				String p = triple.getPredicate().getURI();
				String o = triple.getObject().toString();

				// if property is rdf:type...
				if (p.equals(RDF.type.getURI())) {
					// save class
					// TODO this could be extended to all properties with domain
					// or range = rdfs:Class
					classes.set.add(o);
					// as well as all super-classes of rdfs:Class
					if(o.equals(RDFS.Class.getURI()) || 
							o.equals(OWL.Class.getURI()))
						classes.set.add(s);
				}
				// save property
				properties.set.add(p);
				
				// write triple
				writer.triple(triple);

			}

		};
		
		for(String path : paths)
			RDFDataMgr.parse(dataStream, path);

		System.out.println("# classes collected = "+classes.set.size());
		System.out.println("# properties collected = "+properties.set.size());
		
		// TODO
		// visit URIs in classes and properties
		// save wanted part of RDF files
		// append NT files to model...

	}

}

class CollectionCache {
	TreeSet<String> set = new TreeSet<>();
}