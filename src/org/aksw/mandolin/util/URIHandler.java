package org.aksw.mandolin.util;

import com.hp.hpl.jena.graph.Node;

/**
 * Add blank-node support.
 * 
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class URIHandler {
	
	public static String parse(Node r) {
		String s;
		try {
			s = r.getURI();
		} catch (UnsupportedOperationException e) {
			System.out.println(e.getMessage());
			s = r.getBlankNodeLabel();
			System.out.println("Changing to "+s);
		}
		return s;
	}

}
