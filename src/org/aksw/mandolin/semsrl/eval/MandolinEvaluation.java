package org.aksw.mandolin.semsrl.eval;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.aksw.mandolin.Mandolin;
import org.aksw.mandolin.NameMapper;
import org.aksw.mandolin.semantifier.Commons;


/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class MandolinEvaluation {
	
	// additional occurences of the target link within the evidence
	private static ArrayList<Example> graphSet = new ArrayList<>();

	private static ArrayList<Example> closureLinkSet = new ArrayList<>();

	private static ArrayList<Example> trainingSet = new ArrayList<>();
	private static ArrayList<Example> testSet = new ArrayList<>();
	
	public static void main(String[] args) throws FileNotFoundException {
		
		Mandolin m = new Mandolin();
		NameMapper map = m.getMap();

		// fill out NameMapper without saving graph evidence on file
		File graphEvidence = new File(Mandolin.BASE + "/graph_evidence.db");
		PrintWriter pwGrEvid = new PrintWriter(graphEvidence);
		m.graphEvidence(pwGrEvid);
		pwGrEvid.close();
		// load graph evidence
		Scanner gIn = new Scanner(graphEvidence);
		String linkURI = map.getName(Commons.OWL_SAMEAS.getURI());
		while(gIn.hasNextLine()) {
			String line = gIn.nextLine();
			String[] middle = line.substring(line.indexOf('(') + 1, line.indexOf(')')).split(",");
			String s = middle[0].trim(), t = middle[1].trim();
			System.out.println(s + "\t" + map.getURI(s));
			System.out.println(t + "\t" + map.getURI(t));
			System.out.println();
			if(line.substring(0, line.indexOf('(')).equals(linkURI))
				graphSet.add(new Example(s, t, null));
		}
		gIn.close();

		// save a link evidence on file (linkset statements only)
		File perfMapping = new File(Mandolin.BASE + "/link_evidence.db");
		PrintWriter pwPerfMap = new PrintWriter(perfMapping);
		m.mappingEvidence(pwPerfMap, 0, 417);
		pwPerfMap.close();
		// load perfect mapping
		Scanner in = new Scanner(perfMapping);
		for(int i=0; in.hasNextLine(); i++) {
			String line = in.nextLine();
			String[] middle = line.substring(line.indexOf('(') + 1, line.indexOf(')')).split(",");
			String s = middle[0].trim(), t = middle[1].trim();
			System.out.println(s + "\t" + map.getURI(s));
			System.out.println(t + "\t" + map.getURI(t));
			System.out.println();
			if(i <= Mandolin.TRAINING_SIZE)
				trainingSet.add(new Example(s, t, null));
			else
				testSet.add(new Example(s, t, null));
		}
		in.close();
		
		// closure evidence
		File closureMapping = new File(Mandolin.BASE + "/closure_evidence.db");
		PrintWriter pwClosure = new PrintWriter(closureMapping);
		m.closureEvidence(pwClosure);
		pwClosure.close();
		// load closure evidence
		Scanner cIn = new Scanner(closureMapping);
		while(cIn.hasNextLine()) {
			String line = cIn.nextLine();
			String[] middle = line.substring(line.indexOf('(') + 1, line.indexOf(')')).split(",");
			String s = middle[0].trim(), t = middle[1].trim();
			System.out.println(s + "\t" + map.getURI(s));
			System.out.println(t + "\t" + map.getURI(t));
			System.out.println();
			closureLinkSet.add(new Example(s, t, null));
		}
		cIn.close();
		
		// load Tuffy output and scores
		Scanner tIn = new Scanner(new File("eval/02_publi-tuffy/out.txt"));
		ArrayList<Example> tuffy = new ArrayList<>();
		while(tIn.hasNextLine()) {
			String[] line = tIn.nextLine().replaceAll("\"", "").split("\t");
			Double score = Double.parseDouble(line[0]);
			String[] middle = line[1].substring(line[1].indexOf('(') + 1, line[1].indexOf(')')).split(",");
			String s = middle[0].trim(), t = middle[1].trim();
			System.out.println(s + "\t" + map.getURI(s));
			System.out.println(t + "\t" + map.getURI(t));
			System.out.println(score);
			tuffy.add(new Example(s, t, score));
		}
		tIn.close();
		
		// TODO for each threshold, compare mappings and compute F1 (knowing all positive examples should be faster than usual)
		
		ArrayList<Example> tpArray = new ArrayList<>();
		ArrayList<Example> fpArray = new ArrayList<>();
		
		// for each predicted link...
		for(Example ex : tuffy) {
			
			System.out.println("[" + ex.getSrc() + ", " + ex.getTgt() + "] => [" + map.getURI(ex.getSrc()) + ", " + map.getURI(ex.getTgt()) + "]");
			
			// if it belongs to the training set, it is no real prediction
			if(isInList(ex, trainingSet)) {
				System.out.println("\tbelongs to TRAINING SET -> SKIP");
				continue;
			}
			
			// if it belongs to the evidence?
			if(isInList(ex, graphSet)) {
				System.out.println("\tbelongs to GRAPH SET -> SKIP");
				continue;
			}

			// if it belongs to the test set?
			if(isInList(ex, testSet)) {
				System.out.println("\tbelongs to TEST SET -> TRUE POSITIVE");
				tpArray.add(ex);
			} else {
				// if it belongs to the closure?
				if(isInList(ex, closureLinkSet)) {
					System.out.println("\tbelongs to CLOSURE\\TEST SET -> SKIP");
					continue;
				} else {
					System.out.println("\tdoes not belong to any set -> FALSE POSITIVE");
					fpArray.add(ex);
				}
			}
		}
		
		if(!tpArray.isEmpty()) {
			System.out.println("= TRUE POSITIVES ====");
			for(Example ex : tpArray)
				System.out.println(map.getURI(ex.getSrc())+" | "+map.getURI(ex.getTgt()));
			System.out.println("=====================");
		}
		if(!fpArray.isEmpty()) {
			System.out.println("= FALSE POSITIVES ===");
			for(Example ex : fpArray)
				System.out.println(map.getURI(ex.getSrc())+" | "+map.getURI(ex.getTgt()));
			System.out.println("=====================");
		}

		System.out.println("Training: "+trainingSet.size());
		System.out.println("Test: "+testSet.size());
		
		int tp = tpArray.size();
		int fp = fpArray.size();
		int fn = testSet.size() - tp;
		
		System.out.println("=====================");
		System.out.println("TP = "+tp);
		System.out.println("FP = "+fp);
		System.out.println("FN = "+fn);
		
		double pre = (double) tp / (tp + fp);
		double rec = (double) tp / (tp + fn);
		double f1 = 2 * pre * rec / (pre + rec);
		
		System.out.println("=====================");
		System.out.println("Pre = " + pre);
		System.out.println("Rec = " + rec);
		System.out.println("F1  = " + f1);
		
	}
		

	private static boolean isInList(Example e1, List<Example> list) {
		
		for(Example e2 : list) {
			if(e1.getSrc().equals(e2.getSrc()) && e1.getTgt().equals(e2.getTgt()))
				return true;
			// XXX this check is valid only with symmetric properties!
			if(e1.getSrc().equals(e2.getTgt()) && e1.getTgt().equals(e2.getSrc()))
				return true;
		}
		
		return false;
	}

}

class Example {
	
	String src, tgt;
	Double score;
	
	Example(String src, String tgt, Double score) {
		this.src = src;
		this.tgt = tgt;
		this.score = score;
	}

	public String getSrc() {
		return src;
	}

	public String getTgt() {
		return tgt;
	}

	public Double getScore() {
		return score;
	}
}
