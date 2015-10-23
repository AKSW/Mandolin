package jp.ndca.similarity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;

import jp.ndca.similarity.join.PPJoin;
import jp.ndca.similarity.join.StringItem;
import jp.ndca.similarity.join.Tokenizer;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class PPJoinTest {

	private static final String TEST_FILE = "tmp/ppjoin-test.txt";

	static double threshold = 0.7;

	public static void main(String[] args) throws IOException{
		
		new PPJoinTest().extractPairsTest();
	
	}

	public void extractPairsTest() throws IOException {
		
		PPJoin ppjoin = new PPJoin();
		
		Tokenizer tok = ppjoin.getTokenizer();
		
		List<StringItem> stringItems = new ArrayList<StringItem>();

		HashMap<Integer, String> dataset = new HashMap<>();
		
		Scanner br = new Scanner(new File(TEST_FILE));
		for (int id=0; br.hasNextLine(); id++) {

			String line = br.nextLine();
			
			dataset.put(id, line);
			
			String[] tokens = tok.tokenize(line, false);
			
			Arrays.sort(tokens);
			
			stringItems.add(new StringItem(tokens, id));
			
		}
		br.close();

		StringItem[] strDatum = stringItems.toArray( new StringItem[stringItems.size()] );
		Arrays.sort(strDatum);
		
		ppjoin.setUseSortAtExtractPairs(false);
		
		List<Entry<StringItem,StringItem>> result = ppjoin.extractPairs( strDatum, threshold );
		for(Entry<StringItem,StringItem> entry : result) {
			System.out.println(dataset.get(entry.getKey().getId())
					+" <=> "
					+dataset.get(entry.getValue().getId()));
		}
	}
}
