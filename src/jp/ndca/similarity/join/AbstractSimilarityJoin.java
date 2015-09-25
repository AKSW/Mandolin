package jp.ndca.similarity.join;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;

import jp.ndca.similarity.distance.Jaccard;

public abstract class AbstractSimilarityJoin implements SimilarityJoin {

	protected final static Jaccard jaccard = new Jaccard();

	protected Tokenizer tokenizer = new NgramTokenizer(2);

	public Tokenizer getTokenizer() {
		return tokenizer;
	}

	public boolean isUseConvertingTypeData() {
		return useConvertingTypeData;
	}

	protected boolean useConvertingTypeData = false;

	public void setTokenizer(Tokenizer tokenizer) {
		this.tokenizer = tokenizer;
	}

	public void setUseConvertingTypeData(boolean useConvertingTypeData) {
		this.useConvertingTypeData = useConvertingTypeData;
	}

	/**
	 * convert a data type from String to StringItem.
	 *
	 * @param dataSet
	 * @returns
	 */
	@Override
	public StringItem strConvert(String data, int id) {
		return new StringItem(tokenizer.tokenize(data, useConvertingTypeData),
				id);
	}

	/**
	 * convert a dataset type from String to StringItem.
	 *
	 * @param dataSet
	 * @return
	 */
	@Override
	public StringItem[] strConvert(List<String> dataSet) {
		int len = dataSet.size();
		StringItem[] _dataSet = new StringItem[len];
		for (int i = 0; i < len; i++)
			_dataSet[i] = new StringItem(tokenizer.tokenize(dataSet.get(i),
					useConvertingTypeData), i);
		Arrays.sort(_dataSet);
		return _dataSet;
	}

	@Override
	public IntItem intConvert(int[] data, int id) {
		Arrays.sort(data);
		return new IntItem(data, id);
	}

	@Override
	public IntItem[] intConvert(List<int[]> dataset) {
		IntItem[] items = new IntItem[dataset.size()];
		int i = 0;
		for (int[] data : dataset) {
			Arrays.sort(data);
			items[i++] = new IntItem(data, i++);
		}
		return items;
	}

	protected <K extends Comparable<K>> void validation(K[] dataSet,
			double threshold, boolean useSort) {
		if (threshold <= 0 || 1 <= threshold)
			throw new IllegalArgumentException(
					"argumenrt \"threshold\" is between 0 and 1.0");
		if (useSort)
			Arrays.sort(dataSet);
	}

	protected boolean strUnion(List<StringItem> S,
			List<List<StringItem>> result, double threshold, IntSet buffer) {

		boolean isUnioned = false;
		String[] query = S.get(0).getTokens();
		int querySize = query.length;

		for (List<StringItem> set : result) {
			String[] candidate = set.get(0).getTokens();
			int candidateSize = candidate.length;

			// Jaccard Constraint
			if (querySize < threshold * candidateSize
					|| candidateSize < threshold * querySize)
				continue;

			double score = jaccard.calcByMerge(query, candidate);
			if (threshold <= score) {
				set.addAll(S);
				isUnioned = true;
				break;
			}
		}
		return isUnioned;

	}

	protected boolean intUnion(List<IntItem> S, List<List<IntItem>> result,
			double threshold, IntSet buffer) {

		boolean isUnioned = false;
		int[] query = S.get(0).getTokens();
		int querySize = query.length;

		for (List<IntItem> set : result) {
			int[] candidate = set.get(0).getTokens();
			int candidateSize = candidate.length;

			// Jaccard Constraint
			if (querySize < threshold * candidateSize
					|| candidateSize < threshold * querySize)
				continue;

			double score = jaccard.calcByMerge(query, candidate);
			if (threshold <= score) {
				set.addAll(S);
				isUnioned = true;
				break;
			}
		}
		return isUnioned;

	}

}

class LinkedPositions {

	private Node root;

	private Node last;

	int size;

	public LinkedPositions() {
		Node node = new Node();
		root = node;
		last = node;
	}

	class Node {
		private int position;
		private int id;
		private Node next;
		private Node pre;

		public int getPosition() {
			return position;
		}

		public void setPosition(int position) {
			this.position = position;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public Node getNext() {
			return next;
		}

		public void setNext(Node next) {
			this.next = next;
		}

		public Node getPre() {
			return pre;
		}

		public void setPre(Node pre) {
			this.pre = pre;
		}

		public void remove() {
			if (pre != null)
				pre.next = next;
			if (next != null)
				next.pre = pre;
			else
				// if( next == null ) → this node is last node.
				last = pre;
			size--;
		}
	}

	public void put(int id, int pointer) {
		Node node = new Node();
		node.setId(id);
		node.setPosition(pointer);
		node.setPre(last);
		last.next = node;
		last = node;
		size++;
	}

	public Node getRootNode() {
		return root;
	}

}

/**
 * Int InvertedIndex For SimilarityJoin algorithm
 * 
 * @author hattori_tsukasa
 *
 */
class IntLinkedInvertedIndex {

	// word, Position
	Int2ObjectMap<LinkedPositions> positionsMap = new Int2ObjectOpenHashMap<LinkedPositions>();

	/**
	 * get str's positions
	 * 
	 * @param str
	 * @return
	 */
	public LinkedPositions get(int str) {
		return positionsMap.get(str);
	}

	/**
	 * put id and position into int's Inverted-Index.
	 * 
	 * @param dataID
	 * @param id
	 * @param pointer
	 */
	public void put(int dataID, int id, int pointer) {
		LinkedPositions positions = positionsMap.get(dataID);
		if (positions == null) {
			positions = new LinkedPositions();
			positionsMap.put(dataID, positions);
		}
		positions.put(id, pointer);
	}

	/**
	 * get number of kinds of word.
	 * 
	 * @return
	 */
	public int size() {
		return positionsMap.size();
	}

	/**
	 * get word's set
	 * 
	 * @return
	 */
	public IntSet keySet() {
		return positionsMap.keySet();
	}

}

/**
 * String InvertedIndex For SimilarityJoin algorithm
 * 
 * @author hattori_tsukasa
 *
 */
class StringLinkedInvertedIndex {

	// word, Position
	Map<String, LinkedPositions> positionsMap = new HashMap<String, LinkedPositions>();

	/**
	 * get str's positions
	 * 
	 * @param str
	 * @return
	 */
	public LinkedPositions get(String str) {
		return positionsMap.get(str);
	}

	/**
	 * put id and position into str's Inverted-Index.
	 * 
	 * @param str
	 * @param id
	 * @param pointer
	 */
	public void put(String str, int id, int pointer) {
		LinkedPositions positions = positionsMap.get(str);
		if (positions == null) {
			positions = new LinkedPositions();
			positionsMap.put(str, positions);
		}
		positions.put(id, pointer);
	}

	/**
	 * get number of kinds of word.
	 * 
	 * @return
	 */
	public int size() {
		return positionsMap.size();
	}

	/**
	 * get word's set
	 * 
	 * @return
	 */
	public Set<String> keySet() {
		return positionsMap.keySet();
	}

}