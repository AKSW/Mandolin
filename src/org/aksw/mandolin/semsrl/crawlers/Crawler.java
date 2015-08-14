package org.aksw.mandolin.semsrl.crawlers;

import java.io.IOException;

import org.aksw.mandolin.semsrl.model.DataSource;
import org.aksw.mandolin.semsrl.model.ResourceGraph;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public interface Crawler {

	public ResourceGraph crawl(DataSource ds, String id) throws IOException;
	
}
