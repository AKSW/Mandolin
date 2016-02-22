package org.aksw.mandolin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.TreeSet;

import org.aksw.mandolin.NameMapper.Type;
import org.aksw.mandolin.model.Cache;
import org.aksw.mandolin.model.ComparableLiteral;
import org.aksw.mandolin.util.URIHandler;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class Evidence {

	/**
	 * @param map
	 * @param SRC_PATH
	 * @param TGT_PATH
	 * @param LNK_PATH
	 * @param THR_MIN
	 * @param THR_MAX
	 * @param THR_STEP
	 */
	public static void build(final NameMapper map, final String BASE,
			final int THR_MIN, final int THR_MAX, final int THR_STEP) {

		// for similarity join
		final TreeSet<ComparableLiteral> setOfStrings = new TreeSet<>();
		final Cache cache = new Cache();

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
				String s = URIHandler.parse(arg0.getSubject());
				String p = arg0.getPredicate().getURI();
				String o = arg0.getObject().toString();

				// now check for non-instantiations...
				if (!p.equals(RDF.type.getURI())) {
					// it is supposed that the map contains only classes
					// and instances of these classes (see Classes.build)
					String relName = map.add(p, Type.RELATION);
					String subjName = map.getName(s);
					String objName = map.getName(o);

					// domain/range specification
					if (p.equals(RDFS.domain.getURI())) {
						subjName = map.add(s, Type.RELATION);
						// property name, target class, is domain
						map.addRelClass(subjName, objName, true);
					}
					if (p.equals(RDFS.range.getURI())) {
						subjName = map.add(s, Type.RELATION);
						// property name, target class, is range
						map.addRelClass(subjName, objName, false);
					}

					// if subject or object are not found, it means that they
					// have not been instantiated earlier (see Classes.build)
					if (subjName == null)
						// not found => instance subject, create entity
						subjName = map.add(s, Type.ENTITY);
					else {
						// create entity form for class
						if (subjName.startsWith(Type.CLASS.toString()))
							subjName = map.classToEntityForm(subjName);
						// create stable entity form for properties
						if (subjName.startsWith(Type.RELATION.toString()))
							subjName = map.relationToEntityForm(subjName);

					}
					if (objName == null)
						// not found => instance/datatype object, create entity
						objName = map.add(o, Type.ENTITY);
					else {
						// create entity form for class
						if (objName.startsWith(Type.CLASS.toString()))
							objName = map.classToEntityForm(objName);
						// create stable entity form for properties
						if (objName.startsWith(Type.RELATION.toString()))
							objName = map.relationToEntityForm(objName);
					}

					// property, subject (entity), object (entity) names
					map.addRelationship(relName, subjName, objName);

				}

				if (arg0.getObject().isLiteral()) {
					String dtURI = arg0.getObject().getLiteralDatatypeURI();

					boolean considerString;
					if (dtURI == null)
						considerString = true;
					else
						considerString = dtURI.equals(XSD.xstring.getURI());

					if (considerString) {
						ComparableLiteral lit = new ComparableLiteral(arg0
								.getObject().getLiteral().toString(true), arg0
								.getObject().getLiteral().getValue().toString());
						setOfStrings.add(lit);
					}
				}

			}

		};

		RDFDataMgr.parse(dataStream, BASE + "/model-fwc.nt");

		// call similarity join
		SimilarityJoin.build(map, setOfStrings, cache, BASE, THR_MIN, THR_MAX,
				THR_STEP);
		
		// append model-sim to model-fwc
		final FileOutputStream output;
		try {
			output = new FileOutputStream(new File(BASE + "/model-fwc-sim.nt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		
		final StreamRDF writer = StreamRDFWriter.getWriterStream(output, Lang.NT);
		// TODO
		

	}

}
