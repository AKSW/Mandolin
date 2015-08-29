package org.aksw.mandolin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.aksw.mandolin.NameMapper.Type;
import org.aksw.mandolin.util.URLs;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDF;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class Mandolin {
	
	private static final String SRC_PATH = "datasets/DBLPL3S-100.nt";
	private static final String TGT_PATH = "datasets/LinkedACM-100.nt";
	private static final String LINKSET_PATH = "linksets/DBLPL3S-LinkedACM-100.nt";
	
	private static final String BASE = "publications-tuffy";
	
	private static final String EVIDENCE_DB = BASE + "/evidence.db";
	private static final String QUERY_DB = BASE + "/query.db";
	private static final String PROG_MLN = BASE + "/prog.mln";
	
	private NameMapper map = new NameMapper();

	private void run() throws FileNotFoundException {
		
		PrintWriter pwEvid = new PrintWriter(new File(EVIDENCE_DB));
		
		StreamRDF dataStream = new StreamRDF() {

			@Override
			public void base(String arg0) {}

			@Override
			public void finish() {}

			@Override
			public void prefix(String arg0, String arg1) {}

			@Override
			public void quad(Quad arg0) {}

			@Override
			public void start() {}

			@Override
			public void triple(Triple arg0) {
				String s = map.add(arg0.getSubject().getURI(), Type.RESOURCE);
				System.out.println("Added "+s+" - "+map.getURI(s));
				String p = map.add(arg0.getPredicate().getURI(), Type.PROPERTY);
				System.out.println("Added "+p+" - "+map.getURI(p));
				String o = map.add(arg0.getObject().toString(), Type.RESOURCE);
				System.out.println("Added "+o+" - "+map.getURI(o));
				
				pwEvid.write(p + "(" + s + ", " + o + ")\n");
			}
			
		};

		final Cache training = new Cache();
		
		StreamRDF mapStream = new StreamRDF() {

			@Override
			public void base(String arg0) {}

			@Override
			public void finish() {}

			@Override
			public void prefix(String arg0, String arg1) {}

			@Override
			public void quad(Quad arg0) {}

			@Override
			public void start() {}

			@Override
			public void triple(Triple arg0) {
				String s = map.add(arg0.getSubject().getURI(), Type.RESOURCE);
//				System.out.println("Added "+s+" - "+map.getURI(s));
				String p = map.add(arg0.getPredicate().getURI(), Type.PROPERTY);
//				System.out.println("Added "+p+" - "+map.getURI(p));
				String o = map.add(arg0.getObject().toString(), Type.RESOURCE);
//				System.out.println("Added "+o+" - "+map.getURI(o));
				
				if(++training.count <= 90) {
					System.out.println(training.count + "\t" + p + "(" + s + ", " + o + ")");
					pwEvid.write(p + "(" + s + ", " + o + ")\n");
				}
			}
			
		};

		RDFDataMgr.parse(dataStream, SRC_PATH);
		RDFDataMgr.parse(dataStream, TGT_PATH);
		RDFDataMgr.parse(mapStream, LINKSET_PATH);
		
		pwEvid.close();
		
		// ---------------
		
		PrintWriter pwQuery = new PrintWriter(new File(QUERY_DB));
		pwQuery.write(map.getName(URLs.OWL_SAMEAS));
		pwQuery.close();
		
		// ---------------
		
		PrintWriter pwProg = new PrintWriter(new File(PROG_MLN));
		for(String name : map.getNamesByType(Type.PROPERTY))
			pwProg.write(name+"(res, res)\n");
		pwProg.close();


	}

	public static void main(String[] args) throws FileNotFoundException {
		
		new Mandolin().run();
		
	}

}

class Cache {
	
	int count = 0;
	
}
