package org.aksw.mandolin.model;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class ComparableLiteral implements Comparable<ComparableLiteral> {

	private String uri;
	private String val;

	public ComparableLiteral(String uri, String val) {
		this.uri = uri;
		this.val = val;
	}

	public String getUri() {
		return uri;
	}

	public String getVal() {
		return val;
	}

	@Override
	public int compareTo(ComparableLiteral o) {
		return this.getUri().compareTo(o.getUri());
	}

}