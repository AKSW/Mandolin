package org.aksw.mandolin.semantifier;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import org.aksw.mandolin.util.URLs;

import com.opencsv.CSVReader;

/**
 * Called by BenchmarkSemantifier.
 * 
 * @deprecated This was used before learning ACM authors were not linked. Use
 *             DatasetBuild* instead.
 * 
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
@Deprecated
public class MappingSemantifier {

	public static void run(String srcNs, String tgtNs, String csvFile,
			String ntFile) throws IOException {
		MappingSemantifier
				.run(srcNs, tgtNs, csvFile, ntFile, Integer.MAX_VALUE);
	}

	public static void run(String srcNs, String tgtNs, String csvFile,
			String ntFile, int count) throws IOException {

		CSVReader reader = new CSVReader(new FileReader(new File(csvFile)));
		PrintWriter pw = new PrintWriter(new File(ntFile));

		String[] nextLine = reader.readNext(); // skip header

		for (int i = 0; (nextLine = reader.readNext()) != null && i < count; i++) {
			pw.write("<" + srcNs + nextLine[0] + "> <" + URLs.OWL_SAMEAS
					+ "> <" + tgtNs + nextLine[1] + "> .\n");
		}

		pw.close();
		reader.close();

	}

	public static void main(String[] args) throws IOException {
		MappingSemantifier.run("http://dblp.rkbexplorer.com/id/",
				"http://acm.rkbexplorer.com/id/", "mappings/dblp-acm.csv",
				"datasets/DBLP-ACM-100.nt", 100);
	}

}
