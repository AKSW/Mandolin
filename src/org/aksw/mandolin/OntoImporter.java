package org.aksw.mandolin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.TreeSet;

import org.aksw.mandolin.util.PrettyRandom;
import org.apache.commons.io.FileUtils;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
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
	
	private static final Lang[] LANG_ATTEMPTS = {Lang.RDFXML, Lang.TTL, Lang.NT};

	/**
	 * @param BASE
	 * @param paths
	 */
	public static void run(final String BASE, final String[] paths) {

		final CollectionCache properties = new CollectionCache();
		final CollectionCache classes = new CollectionCache();

		final FileOutputStream output;
		try {
			output = new FileOutputStream(new File(BASE + "/model-tmp.nt"));
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
				writer.start();
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
		
		// ontology importer
		for(String uri : classes.set) {
			System.out.println("Crawling <"+uri+">...");
			Model model = ModelFactory.createDefaultModel();
			// visit URIs in classes and properties
			String path = BASE + "/temp-file-" + PrettyRandom.get(6) + "";
			File file = new File(path);
			try {
				FileUtils.copyURLToFile(new URL(uri), file);
			} catch (IOException e) {
				System.out.println("Cannot download <"+uri+">.");
				continue;
			}
			System.out.println("Saved to "+path+".");
			for(Lang lang : LANG_ATTEMPTS) {
				try {
					System.out.println("Trying with "+lang);
					RDFDataMgr.read(model, path, lang);
					break;
				} catch (RiotException e) {
					System.out.println("Cannot interpret <"+uri+"> using "+lang+".");
				}
			}
			System.out.println("\t# statements: "+model.size());
			StmtIterator list = model.listStatements();
			// append NT files to model...
			while(list.hasNext()) {
				// save wanted part of RDF files
				Statement stmt = list.next();
				
				System.out.println("\t"+stmt);
				
				boolean imprt = stmt.getPredicate().getURI().equals(uri);
				
				if(!imprt)
					if(stmt.getSubject().isURIResource())
						if(stmt.getSubject().getURI().equals(uri))
							imprt = true;
				if(!imprt)
					if(stmt.getObject().isURIResource())
						if(stmt.getObject().asResource().getURI().equals(uri))
							imprt = true;
				
				if(imprt) {
					Triple t = stmt.asTriple();
					System.out.println("\t"+t);
					writer.triple(t);
				}
			}
		}
		writer.finish();

	}

}

class CollectionCache {
	TreeSet<String> set = new TreeSet<>();
}