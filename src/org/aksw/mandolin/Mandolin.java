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
	
	public static final String SRC_PATH = "datasets/DBLPL3S-100.nt";
	public static final String TGT_PATH = "datasets/LinkedACM-100.nt";
	public static final String LINKSET_PATH = "linksets/DBLPL3S-LinkedACM-100.nt";
	
	public static final String BASE = "publications-tuffy";
	
	public static final String EVIDENCE_DB = BASE + "/evidence.db";
	public static final String QUERY_DB = BASE + "/query.db";
	public static final String PROG_MLN = BASE + "/prog.mln";
	
	private NameMapper map;
	
	public NameMapper getMap() {
		return map;
	}

	public Mandolin() {
		
		map = new NameMapper();
		
	}

	private void run() throws FileNotFoundException {
		
		PrintWriter pwEvid = new PrintWriter(new File(EVIDENCE_DB));
		graphEvidence(pwEvid);
		mappingEvidence(pwEvid, 0, (int)(417 * 0.9));
		
		buildQueryDB(new PrintWriter(new File(QUERY_DB)));
		
		buildProgMLN(new PrintWriter(new File(PROG_MLN)));

	}

	public void buildProgMLN(PrintWriter pwProg) {

		String sameAs = map.getName(URLs.OWL_SAMEAS);
		for(String name : map.getNamesByType(Type.PROPERTY)) {
			// closed world assumption is false for owl:sameAs
			String cw = name.equals(sameAs) ? "" : "*";
			pwProg.write(cw + name + "(res, res)\n");
		}
		pwProg.write("\n");
		for(String name : map.getNamesByType(Type.PROPERTY)) {
			// symmetric property
			pwProg.write("1 !"+name+"(x, y) v "+name+"(y, x)\n");
			pwProg.write("1 !"+name+"(y, x) v "+name+"(x, y)\n");
			// transitive property
			pwProg.write("1 !"+name+"(x, y) v !"+name+"(y, z) v "+name+"(x, z)\n");
		}
		pwProg.close();

	}

	public void buildQueryDB(PrintWriter pwQuery) {
		
		String sameAs = map.getName(URLs.OWL_SAMEAS);
		pwQuery.write(sameAs);
		pwQuery.close();
		
	}

	public void graphEvidence(PrintWriter pwEvid) {
		
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
				
				if(pwEvid != null)
					pwEvid.write(p + "(" + s + ", " + o + ")\n");
			}
			
		};
		
		RDFDataMgr.parse(dataStream, SRC_PATH);
		RDFDataMgr.parse(dataStream, TGT_PATH);
		
	}
	
	public void mappingEvidence(PrintWriter pwEvid, final int START, final int END) {

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
				
				if(pwEvid != null) {
					int c = ++training.count;
					if(START <= c && c <= END) {
						System.out.println(training.count + "\t" + p + "(" + s + ", " + o + ")");
						pwEvid.write(p + "(" + s + ", " + o + ")\n");
					}
				}
			}
			
		};

		RDFDataMgr.parse(mapStream, LINKSET_PATH);
		
		pwEvid.close();
		
	}

	public static void main(String[] args) throws FileNotFoundException {
		
		new Mandolin().run();
		
	}

}

class Cache {
	
	int count = 0;
	
}
