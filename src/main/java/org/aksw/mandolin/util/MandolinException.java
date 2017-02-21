package org.aksw.mandolin.util;

import org.apache.logging.log4j.Logger;

/**
 * @author Tommaso Soru {@literal tsoru@informatik.uni-leipzig.de}
 *
 */
public class MandolinException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3950659004299788805L;
	
	public MandolinException(String message, Logger logger) {
		super(message);
		logger.fatal(MandolinException.class.getName() + ": " + message);
	}

}
