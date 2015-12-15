package org.aksw.mandolin.util;

import java.io.FileInputStream;
import java.io.IOException;
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
		} catch (MissingResourceException | NullPointerException e) {
			String str = getConfig(key);
			if(str != null)
				return str;
			else
				return '!' + key + '!';
		}
	}
	
	private static String getConfig(String key) {
		// to load application's properties, we use this class
		java.util.Properties mainProperties = new java.util.Properties();

		FileInputStream file;

		// the base folder is ./, the root of the main.properties file
		String path = "./mandolin.properties";

		try {
			// load the file handle for main.properties
			file = new FileInputStream(path);

			// load all the properties from this file
			mainProperties.load(file);

			// we have loaded the properties, so close the file handle
			file.close();
			// retrieve the property we are intrested
			return mainProperties.getProperty(key);
		} catch (IOException e) {
			return null;
		}

	}

}
