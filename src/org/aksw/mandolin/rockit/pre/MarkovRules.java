package org.aksw.mandolin.rockit.pre;

import java.util.ArrayList;
import java.util.TreeSet;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class MarkovRules {

	public static void main(String[] args) {

		TreeSet<String> pred = new TreeSet<>();
		for (int i = 0; i < 9; i++)
			pred.add("Prop" + i);

		for (String rule : getRules("Prop2", pred))
			System.out.println(rule);

	}

	public static ArrayList<String> getRules(String target, TreeSet<String> pred) {

		ArrayList<String> rules = new ArrayList<>();

		int N = 2;
		
		String[] prefixes = {"!", ""};

		for (String p : pred) {

			for (int j = 0; j < prefixes.length; j++) {
				
				for (int x = 0; x < Math.pow(2, N); x++) {

					String rule = prefixes[j] + target + "(x, y)";
					String base2 = Integer.toString(x, 2);
					// character of base2 indicates nature of predicate
					// (pos/neg)
					for (int i = 0; i < base2.length(); i++) {
						String prefix = prefixes[Integer.parseInt(""+base2.charAt(i))];
						rule += " v " + prefix + p + "(x, y)";
					}
					rules.add(rule);
				}
				
			}

		}

		return rules;
	}

}
