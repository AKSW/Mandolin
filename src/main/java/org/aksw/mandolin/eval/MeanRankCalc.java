package org.aksw.mandolin.eval;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDF;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class MeanRankCalc {
	
	private PrintWriter pw; 
	
	private String testSet, mandolinOut; 
	
	private int minThr = 1;

	public int getMinThr() {
		return minThr;
	}

	public void setMinThr(int minThr) {
		this.minThr = minThr;
	}

	public static void main(String[] args) throws IOException {
		
		String testSet = args[0];
		String mandolinOut = args[1];
		
		MeanRankCalc mr = new MeanRankCalc(testSet, mandolinOut);
	
		mr.partitionData();
		mr.start();
		
	}
	
	public MeanRankCalc(String testSet, String mandolinOut) {
		super();
		this.testSet = testSet;
		this.mandolinOut = mandolinOut;
	}
	
	public double start() throws FileNotFoundException {
		
		pw = new PrintWriter(new File(mandolinOut + "/evaluation.csv"));
		
		Scanner in = new Scanner(new File(mandolinOut + "/entities.csv"));
		int entities = 0;
		while(in.hasNextLine()) {
			in.nextLine();
			entities++;
		}
		in.close();
		
		final Integer MEDIAN_RANK = entities / 2;
		System.out.println("Median rank = "+MEDIAN_RANK);
		
		DecimalFormat df = new DecimalFormat("0.0");
		
		// load N=max-min+1 models in descending order
		final Model[] m = new Model[10 - minThr + 1];
		for(int i=m.length; i>=1; i--) {
			String thr = String.valueOf(df.format((double) (i+minThr-1) / 10.0));
			String discovered = mandolinOut + "/ranked_" + thr + ".nt";
			System.out.println("Loading model "+i+"...");
			m[m.length-i] = RDFDataMgr.loadModel(discovered);
		}
		
		final ArrayList<Integer> ranks = new ArrayList<>();
		
		final MRCache cache = new MRCache();
		
		StreamRDF dataStream = new StreamRDF() {

			@Override
			public void start() {				
			}

			private Integer check(Model mdl, MRCache cache, Triple triple, boolean forward) {
				
				Resource s = ResourceFactory.createResource(triple.getSubject()
						.getURI());
				Property p = ResourceFactory.createProperty(triple
						.getPredicate().getURI());
				Resource o = ResourceFactory.createResource(triple.getObject()
						.getURI());

				ExtendedIterator<? extends RDFNode> it = forward ? mdl
						.listObjectsOfProperty(s, p) : mdl
						.listSubjectsWithProperty(p, o);
				// initialize count
				int y = 0;
				while (it.hasNext()) {
					
					Resource res = it.next().asResource();
					String uri = forward ? triple.getObject().getURI() : triple
							.getSubject().getURI();
					// if triple is found
					if (res.getURI().equals(uri)) {
						// rank[triple] = x + 1
						int rank = cache.x+1;
						System.out.println(triple+"  >>>  "+rank);
						ranks.add(cache.x + 1);
						// next triple
						return rank;
					}
					y++;
				}
				// add up to rank value
				cache.x += y;

				return null;
			}

			@Override
			public void triple(Triple triple) {
				// initialize rank value
				cache.x = 0;
				
				// for each model
				for(int i=0; i<m.length; i++) {
					
					Model model = m[i];
					
					Integer rank = check(model, cache, triple, true);
					if(rank != null) {
						System.out.println("\ttriple found in model #"+(m.length-i));
						if(rank <= 1)
							cache.hitsAt1++;
						if(rank <= 3)
							cache.hitsAt3++;
						if(rank <= 10)
							cache.hitsAt10++;
						return;
					}
			
				}
				
				// median rank
				ranks.add(MEDIAN_RANK);
				System.out.println(triple+"  >>>  (median)");
			}

			@Override
			public void quad(Quad quad) {
			}

			@Override
			public void base(String base) {
			}

			@Override
			public void prefix(String prefix, String iri) {
			}

			@Override
			public void finish() {
			}
			
		};
		
		// stream test set
		RDFDataMgr.parse(dataStream, testSet);
		
		System.out.println("\n=== FILTERED SETTING === "+mandolinOut.substring(mandolinOut.lastIndexOf('/')+1));
		// compute mean rank
		int sum = 0, sumR = 0;
		System.out.println(ranks);
		for(Integer i : ranks) {
			sum += i;
			sumR += 1.0 / i;
		}
		
		double mr = (double) sum / (double) ranks.size();
		double mrr = (double) sumR / (double) ranks.size();
		System.out.println("\nMeanRank = "+mr);
		System.out.println("\nMRR = "+mrr);
		
		double h1 = (double) cache.hitsAt1 * 100 / (double) ranks.size();
		double h3 = (double) cache.hitsAt3 * 100 / (double) ranks.size();
		double h10 = (double) cache.hitsAt10 * 100 / (double) ranks.size();
		System.out.println("\nHits@1  = "+h1);
		System.out.println("Hits@3  = "+h3);
		System.out.println("Hits@10 = "+h10);
		
		pw.println(mandolinOut + "," + mrr + "," + h1 + "," + h3 + "," + h10);
		pw.close();
		
		return mr;
	}

	public void partitionData() throws IOException {
		
		System.out.println("Partitioning data...");
		
		DecimalFormat df = new DecimalFormat("0.0");
		for (int i = minThr; i <= 9; i++) {
			String thrA = String.valueOf(df.format((double) i / 10.0));
			String thrB = String.valueOf(df.format((double) (i+1) / 10.0));
			System.out.println(thrA+","+thrB);
			String outA = mandolinOut + "/discovered_" + thrA + ".nt";
			String outB = mandolinOut + "/discovered_" + thrB + ".nt";
			String ranked = mandolinOut + "/ranked_" + thrA + ".nt";
			
			if(new File(ranked).exists()) {
				System.out.println("Partitions exist. Skipping...");
				return;
			}

			Scanner inB = new Scanner(new File(outB));
			TreeSet<String> indexB = new TreeSet<>();
			while(inB.hasNextLine())
				indexB.add(inB.nextLine());
			inB.close();

			Scanner inA = new Scanner(new File(outA));
			PrintWriter pw = new PrintWriter(new File(ranked));
			while(inA.hasNextLine()) {
				String line = inA.nextLine();
				if(!indexB.contains(line))
					pw.println(line);
			}
			pw.close();
			inA.close();
		}
		FileUtils.copyFile(new File(mandolinOut + "/discovered_1.0.nt"), new File(mandolinOut + "/ranked_1.0.nt"));


	}

}

class MRCache {
	int x;
	int hitsAt1 = 0;
	int hitsAt3 = 0;
	int hitsAt10 = 0;
}
