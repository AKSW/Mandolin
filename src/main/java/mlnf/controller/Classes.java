package mlnf.controller;

import mlnf.controller.NameMapper.Type;
import mlnf.util.URIHandler;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * @author author <email>
 *
 */
public class Classes {
	
	private final static Logger logger = LogManager.getLogger(Classes.class);
	
	private final static Cache size = new Cache();
	
	/**
	 * @param map 
	 * @param SRC_PATH
	 * @param TGT_PATH
	 */
	public static void build(final NameMapper map, final String BASE) {
		
		final CollectionCache nodes = new CollectionCache();
//		final CollectionCache classes = new CollectionCache();
		
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

				// if property is rdf:type...
				if(p.equals(RDF.type.getURI())) {
					// then object is always a class
					String className = map.add(o, Type.CLASS);
					// if object is :Class...
					if(o.equals(OWL.Class.getURI()) ||
							o.equals(RDFS.Class.getURI())) {
						// then also subject is a class
						map.add(s, Type.CLASS);
					} else {
						// else subject is an entity
						// XXX subject could be even a property!
						String entName = map.add(s, Type.ENTITY);
						map.addEntClass(entName, className);
					}
//					// save class
//					// TODO this could be extended to all properties with domain or range = rdfs:Class
//					classes.set.add(o);
				}
				
				map.add(s, Type.ENTITY);
				map.add(o, Type.ENTITY);

				// save nodes
				nodes.set.add(s);
				nodes.set.add(o);
				
//				// save property
//				properties.set.add(p);
				// count triples
				size.value++;
			}

		};

		RDFDataMgr.parse(dataStream, BASE + "/model-fwc.nt");
		
		logger.info("Adding owl:Thing type to {} nodes.", nodes.set.size());
		for(String s : nodes.set)
			map.addEntClass(map.toName(s), map.getOwlThingName());
		
		map.setCollisionDelta(collisionDelta());
		
	}
	
	/**
	 * Compute the upper bound for the order of magnitude of entities and return the sum to add to avoid ID collision.
	 * 
	 * @return
	 */
	public static int collisionDelta() {
		int upper = (int) Math.log10(size.value * 2) + 1;
		return (int) Math.pow(10, upper);
	}


}

class Cache {
	int value = 0;
}
