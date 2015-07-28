package org.aksw.simba.semsrl.util;

/**
 * @author Tommaso Soru <>
 *
 */
public class StringClean {
	
	public static String clean(String string) {
		return string.replaceAll("[^\\dA-Za-z]", "");
	}
	
	public static String oneRow(String string) {
		return string.replaceAll("\n", " ").replaceAll("\t", " ");
	}

}