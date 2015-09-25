package jp.ndca.similarity.join;

public interface Tokenizer {

	public String[] tokenize(String str, boolean allowDuplicate);

}
