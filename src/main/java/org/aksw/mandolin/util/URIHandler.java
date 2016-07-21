package org.aksw.mandolin.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hp.hpl.jena.graph.Node;

/**
 * Add blank-node support.
 * 
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class URIHandler {
	
	private final static Logger logger = LogManager.getLogger(URIHandler.class);
	
	public static String parse(Node r) {
		String s;
		try {
			s = r.getURI();
		} catch (UnsupportedOperationException e) {
			logger.debug(e.getMessage());
			s = r.getBlankNodeLabel();
			logger.debug("Changing to "+s);
		}
		return s;
	}

}
