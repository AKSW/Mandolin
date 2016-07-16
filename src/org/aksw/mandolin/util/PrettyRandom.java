package org.aksw.mandolin.util;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class PrettyRandom {

	public static String get(int digits) {
		String r = "";
		while (r.equals("") || r.length() < digits)
			r = String.valueOf(Math.random()).substring(2);
		return r.substring(0, digits);
	}
	
}
