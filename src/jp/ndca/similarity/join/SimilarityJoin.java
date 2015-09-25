package jp.ndca.similarity.join;

import java.util.List;
import java.util.Map.Entry;

public interface SimilarityJoin {

	public StringItem strConvert(String data, int id);

	public StringItem[] strConvert(List<String> dataset);

	public IntItem intConvert(int[] data, int id);

	public IntItem[] intConvert(List<int[]> dataset);

	public List<StringItem> search(StringItem query, StringItem[] dataSet,
			double threshold);

	public List<Entry<StringItem, StringItem>> extractPairs(
			StringItem[] dataSet, double threshold);

	public List<List<StringItem>> extractBulks(StringItem[] dataSet,
			double threshold);

	public List<IntItem> search(IntItem query, IntItem[] dataSet,
			double threshold);

	public List<Entry<IntItem, IntItem>> extractPairs(IntItem[] dataSet,
			double threshold);

	public List<List<IntItem>> extractBulks(IntItem[] dataSet, double threshold);

}
