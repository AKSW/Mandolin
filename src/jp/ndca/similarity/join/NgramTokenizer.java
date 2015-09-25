package jp.ndca.similarity.join;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NgramTokenizer implements Tokenizer {

	private static final int DEFAULT_N = 2;

	private int n = DEFAULT_N;

	public int getN() {
		return n;
	}

	public void setN(int n) {
		this.n = n;
	}

	public NgramTokenizer() {
	}

	public NgramTokenizer(int n) {
		this.n = n;
	}

	public String[] tokenize(String str, boolean allowDuplicate) {
		if (allowDuplicate)
			return tokenizeWithDuplication(str);
		else
			return tokenizeWithoutDuplication(str);
	}

	private String[] tokenizeWithoutDuplication(String str) {
		Set<String> ngramSet = new HashSet<String>();
		for (int i = 0; (i + (n - 1)) < str.length(); i++) {
			String ngram = str.substring(i, i + n);
			ngramSet.add(ngram);
		}
		String[] sets = new String[ngramSet.size()];
		int i = 0;
		for (String ngram : ngramSet)
			sets[i++] = ngram;
		Arrays.sort(sets);
		return sets;
	}

	private String[] tokenizeWithDuplication(String str) {
		List<String> result = new ArrayList<String>();
		for (int i = 0; (i + (n - 1)) < str.length(); i++) {
			String ngram = str.substring(i, i + n);
			result.add(ngram);
		}
		String[] strArray = result.toArray(new String[result.size()]);
		Arrays.sort(strArray);
		return strArray;
	}

}
