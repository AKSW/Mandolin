package org.aksw.mandolin.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class Shell {

	private final static Logger LOGGER = LogManager.getLogger(Shell.class.getName());

	/**
	 * Execute a command which expects an output.
	 * 
	 * @param command
	 * @param show
	 * @return
	 */
	public static String execute(String command, boolean show) {
		StringBuffer sb = new StringBuffer();
		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					p.getInputStream()));

			String line = "";
			while ((line = reader.readLine()) != null) {
				if (show)
					LOGGER.info(line);
				sb.append(line + "\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	/**
	 * Execute a command which expects no output.
	 * 
	 * @param command
	 * @return
	 */
	public static String execute(String command) {
		StringBuffer sb = new StringBuffer();
		Process p;
		try {
			p = Runtime.getRuntime().exec(command);

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					p.getInputStream()));

			String line = "";
			while ((line = reader.readLine()) != null)
				sb.append(line);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LOGGER.info("Streamed:");
		String output = execute("ls -l", true);
		LOGGER.info("\nBuffered:\n" + output);
	}

}
