package jp.ndca.similarity.join;

/**
 * String データの特徴を表すクラスです。
 * 
 * @author hattori_tsukasa
 *
 */
public class StringItem implements Comparable<StringItem> {

	private int id;
	private String[] tokens;

	public int getId() {
		return id;
	}

	public String[] getTokens() {
		return tokens;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setTokens(String[] tokens) {
		this.tokens = tokens;
	}

	/**
	 * constractor
	 * 
	 * argument "tokens" is already sorted. Please look at
	 * {@link SimilarityJoin#convert(java.util.List)}.
	 * 
	 * @param tokens
	 * @param id
	 */
	public StringItem(String[] tokens, int id) {
		this.tokens = tokens;
		this.id = id;
	}

	public int size() {
		return tokens.length;
	}

	public String get(int i) {
		return tokens[i];
	}

	@Override
	public int compareTo(StringItem o) {
		int num1 = this.size();
		int num2 = o.size();
		if (num1 < num2)
			return -1;
		else if (num2 < num1)
			return 1;
		else
			return 0;
	}

}