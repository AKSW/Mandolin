package org.aksw.mandolin.eval;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
enum Dataset {
	
	FB15K("benchmark/fb15k/freebase_mtr100_mte100-test.nt", "fb15k_"),
	WN18("benchmark/wn18/wordnet-mlj12-test.nt", "wn18_");
	
	String ref, prefix;
	
	Dataset(String ref, String prefix) {
		this.ref = ref;
		this.prefix = prefix;
	}
	
}