package org.aksw.mandolin.semsrl.util;

import org.supercsv.prefs.CsvPreference;
import org.supercsv.quote.NormalQuoteMode;
import org.supercsv.util.CsvContext;

/**
 * This SuperCSV preference mode adds quotes for strings containing a space.
 * 
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 * 
 */
public class CustomQuoteMode extends NormalQuoteMode {
	
	public boolean quotesRequired(String csvColumn, CsvContext context,
			CsvPreference preference) {
		if (csvColumn.contains(" "))
			return true;
		else
			return super.quotesRequired(csvColumn, context, preference);
	}
	
}
