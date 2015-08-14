package org.aksw.mandolin.util;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 * 
 */
public class Bundle {

	private static String bundleName;
	private static ResourceBundle resBundle;

	public static String getBundleName() {
		return bundleName;
	}

	public static void setBundleName(String bName) {
		bundleName = bName;
		resBundle = ResourceBundle.getBundle(bundleName);
	}
	
	public static int getArrayValue(String key, int pos) {
		return Integer.parseInt(getString(key).split(",")[pos]);
	}

	public static String getString(String key) {
		try {
			return resBundle.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
	
}
