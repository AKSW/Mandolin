package mlnf.util;

/**
 * @author author <email>
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
