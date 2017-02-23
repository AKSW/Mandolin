package mlnf.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author author <email>
 *
 */
public class Shell {

	private final static Logger logger = LogManager.getLogger(Shell.class);

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
					logger.debug(line);
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
		return execute(command, false);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		logger.debug("Streamed:");
		String output = execute("ls -l", true);
		logger.debug("\nBuffered:\n" + output);
	}

}
