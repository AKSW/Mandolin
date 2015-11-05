package org.aksw.mandolin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeSet;

import jp.ndca.similarity.join.PPJoin;
import jp.ndca.similarity.join.StringItem;
import jp.ndca.similarity.join.Tokenizer;

import org.aksw.mandolin.NameMapperProbKB.Type;
import org.aksw.mandolin.model.Cache;
import org.aksw.mandolin.model.ComparableLiteral;
import org.aksw.mandolin.semantifier.Commons;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class SimilarityJoin {

	public static void build(NameMapperProbKB map, TreeSet<ComparableLiteral> setOfStrings,
			Cache cache, final int THR_MIN, final int THR_MAX, final int THR_STEP) {
		
		PPJoin ppjoin = new PPJoin();
		Tokenizer tok = ppjoin.getTokenizer();
		HashMap<Integer, ComparableLiteral> dataset = new HashMap<>();

		Iterator<ComparableLiteral> it = setOfStrings.iterator();
		for (int i = 0; it.hasNext(); i++) {
			ComparableLiteral lit = it.next();
			String val = lit.getVal();
			cache.stringItems.add(new StringItem(tok.tokenize(val, false), i));
			dataset.put(i, lit);
		}

//		System.out.println(cache.stringItems.size());
		List<StringItem> stringItems = cache.stringItems;

		StringItem[] strDatum = stringItems.toArray(new StringItem[stringItems
				.size()]);
		Arrays.sort(strDatum);

		ppjoin.setUseSortAtExtractPairs(false);

		for (int thr = THR_MIN; thr <= THR_MAX; thr += THR_STEP) {
//			System.out.println("thr = " + (thr / 100.0));
			List<Entry<StringItem, StringItem>> result = ppjoin.extractPairs(
					strDatum, thr / 100.0);
			for (Entry<StringItem, StringItem> entry : result) {
				ComparableLiteral lit1 = dataset.get(entry.getKey().getId());
				ComparableLiteral lit2 = dataset.get(entry.getValue().getId());
//				TODO make it dynamic...
				String rel = (thr == 90) ? Commons.SIMILAR_TO_90.getURI() :
					Commons.SIMILAR_TO_80.getURI();
				map.addRelationship(map.add(rel, Type.RELATION), map.getName(lit1.getUri()), map.getName(lit2.getUri()));
//				System.out.println(lit1.getUri() + " <=> " + lit2.getUri());
//				System.out.println(lit1.getVal() + " <=> " + lit2.getVal());
			}
		}
		
	}

}
