package jp.ndca.similarity.distance;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Accumulate overlap value
 *
 * @author hattori_tsukasa
 *
 */
public class Overlap {

	public Overlap() {
		super();
	}

	/**
	 *
	 * @param a
	 * @param b
	 * @return
	 */
	public double calc(Object[] a, Object[] b) {
		int alen = a.length;
		int blen = b.length;
		Set<Object> set = new HashSet<Object>(alen + blen);
		set.addAll(Arrays.asList(a));
		set.addAll(Arrays.asList(b));

		return innerCalc(alen, blen, set.size());
	}

	/**
	 *
	 * @param a
	 * @param b
	 * @return
	 */
	public double calc(List<? extends Object> a, List<? extends Object> b) {
		int alen = a.size();
		int blen = b.size();
		Set<Object> set = new HashSet<Object>(alen + blen);
		set.addAll(a);
		set.addAll(b);
		return innerCalc(alen, blen, set.size());
	}

	public double calc(List<? extends Object> a, int offsetA,
			List<? extends Object> b, int offsetB) {
		int alen = a.size() - offsetA;
		int blen = b.size() - offsetB;
		Set<Object> set = new HashSet<Object>();
		for (int i = offsetA; i < a.size(); i++) {
			set.add(a.get(i));
		}
		for (int i = offsetB; i < b.size(); i++) {
			set.add(b.get(i));
		}
		return innerCalc(alen, blen, set.size());
	}

	public <K> double calc(K[] a, int offsetA, K[] b, int offsetB) {
		int alen = a.length - offsetA;
		int blen = b.length - offsetB;
		Set<Object> set = new HashSet<Object>();
		for (int i = offsetA; i < a.length; i++)
			set.add(a[i]);
		for (int i = offsetB; i < b.length; i++)
			set.add(b[i]);
		return innerCalc(alen, blen, set.size());
	}

	public <K extends Comparable<K>> double calcByMerge(K[] a, int offsetA,
			K[] b, int offsetB) {

		int overlap = 0;
		int i = offsetA;
		int j = offsetB;

		while (i < a.length && j < b.length) {
			if (a[i].equals(b[j])) {
				overlap++;
				i++;
				j++;
			} else if (a[i].compareTo(b[j]) < 0)
				i++;
			else
				j++;
		}
		return overlap;

	}

	public double calcByMerge(int[] a, int offsetA, int[] b, int offsetB) {

		int overlap = 0;
		int i = offsetA;
		int j = offsetB;

		while (i < a.length && j < b.length) {
			if (a[i] == b[j]) {
				overlap++;
				i++;
				j++;
			} else if (a[i] < b[j])
				i++;
			else
				j++;
		}
		return overlap;

	}

	private double innerCalc(int alen, int blen, int union) {
		double intersection = alen + blen - union;
		return intersection;
	}

}
