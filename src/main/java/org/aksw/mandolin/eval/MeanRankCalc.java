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


	public static void main(String[] args) throws IOException {
		
		String testSet = args[0];
		String output = args[1];
		
		partitionData(output);
		meanRank(testSet, output);
		
	}
	
	public static void meanRank(String testSet, String output) throws FileNotFoundException {
		
		Scanner in = new Scanner(new File(output + "/entities.csv"));
		int entities = 0;
		while(in.hasNextLine()) {
			in.nextLine();
			entities++;
		}
		in.close();
		
		final Integer MEDIAN_RANK = entities / 2;
		System.out.println("Median rank = "+MEDIAN_RANK);
		
		DecimalFormat df = new DecimalFormat("0.0");
		
		// load 10 models in descending order
		final Model[] m = new Model[10];
		for(int i=m.length; i>=1; i--) {
			String thr = String.valueOf(df.format((double) i / 10.0));
			String discovered = output + "/ranked_" + thr + ".nt";
			System.out.println("Loading model "+i+"...");
			m[m.length-i] = RDFDataMgr.loadModel(discovered);
		}
		
		final ArrayList<Integer> ranks = new ArrayList<>();
		
		final MRCache cache = new MRCache();
		
		StreamRDF dataStream = new StreamRDF() {

			@Override
			public void start() {				
			}

			private boolean check(Model mdl, MRCache cache, Triple triple, boolean forward) {
				
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
						System.out.println(triple+"  >>>  "+(cache.x+1));
						ranks.add(cache.x + 1);
						// next triple
						return true;
					}
					y++;
				}
				// add up to rank value
				cache.x += y;

				return false;
			}

			@Override
			public void triple(Triple triple) {
				// initialize rank value
				cache.x = 0;
				
				// for each model
				for(int i=0; i<m.length; i++) {
					
					Model model = m[i];
					
//					check(model, triple, false);
					if(check(model, cache, triple, true)) {
						System.out.println("\ttriple found in model #"+(m.length-i));
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
		
		// compute mean rank
		int sum = 0;
		for(Integer i : ranks)
			sum += i;
		System.out.println("\nMeanRank = "+(double) sum / (double) ranks.size());
		
	}

	public static void partitionData(String output) throws IOException {
		
		System.out.println("Partitioning data...");
		
		DecimalFormat df = new DecimalFormat("0.0");
		for (int i = 1; i <= 9; i++) {
			String thrA = String.valueOf(df.format((double) i / 10.0));
			String thrB = String.valueOf(df.format((double) (i+1) / 10.0));
			System.out.println(thrA+","+thrB);
			String outA = output + "/discovered_" + thrA + ".nt";
			String outB = output + "/discovered_" + thrB + ".nt";
			String ranked = output + "/ranked_" + thrA + ".nt";

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
		FileUtils.copyFile(new File(output + "/discovered_1.0.nt"), new File(output + "/ranked_1.0.nt"));


	}

}

class MRCache {
	int x;
}
