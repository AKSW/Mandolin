package jp.ndca.similarity.join;

/**
 * Vector データの特徴を表すクラスです。
 * 
 * @author hattori_tsukasa
 *
 */
public class IntItem implements Comparable<IntItem> {

	private int id;
	private int[] tokens;

	public int getId() {
		return id;
	}

	public int[] getTokens() {
		return tokens;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setTokens(int[] tokens) {
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
	IntItem(int[] tokens, int id) {
		this.tokens = tokens;
		this.id = id;
	}

	public int size() {
		return tokens.length;
	}

	public int get(int i) {
		return tokens[i];
	}

	@Override
	public int compareTo(IntItem o) {
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