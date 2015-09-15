package org.aksw.mandolin.semsrl.eval;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import org.aksw.mandolin.Mandolin;
import org.aksw.mandolin.NameMapper;


/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class MandolinEvaluation {
	
	private static ArrayList<Example> perfectMapping = new ArrayList<>();

	public static void main(String[] args) throws FileNotFoundException {

		Mandolin m = new Mandolin();
		// fill out NameMapper without saving graph evidence on file
		m.graphEvidence(null);
		// save a hidden evidence on file (perfect mapping for the remaining fold)
		File perfMapping = new File(Mandolin.BASE + "/hidden_evidence.db");
		m.mappingEvidence(new PrintWriter(perfMapping),
				(int)(417 * 0.9) + 1, 417);
		NameMapper map = m.getMap();
		
		// load perfect mapping
		Scanner in = new Scanner(perfMapping);
		while(in.hasNextLine()) {
			String line = in.nextLine();
			String[] middle = line.substring(line.indexOf('(') + 1, line.indexOf(')')).split(",");
			String s = middle[0].trim(), t = middle[1].trim();
			System.out.println(s + "\t" + map.getURI(s));
			System.out.println(t + "\t" + map.getURI(t));
			System.out.println();
			perfectMapping.add(new Example(s, t, null));
			
		}
		in.close();
		
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
			// XXX reflexive statements are discarded
			if(!s.equals(t))
				tuffy.add(new Example(s, t, score));
		}
		tIn.close();

		// TODO for each threshold, compare mappings and compute F1 (knowing all
		// positive examples should be faster than usual)
		
		int tp = 0;
		ArrayList<Example> tpArray = new ArrayList<>();
		ArrayList<Example> fpArray = new ArrayList<>();
		for(Example ex : tuffy) {
			if(isInMapping(ex)) {
				tp++;
				tpArray.add(ex);
			} else {
				fpArray.add(ex);
			}
		}
		System.out.println(tp + " / " + tuffy.size());
		
		System.out.println("=== TRUE POSITIVES ===");
		for(Example ex : tpArray)
			System.out.println(map.getURI(ex.getSrc())+" | "+map.getURI(ex.getTgt()));
		System.out.println("=== FALSE POSITIVES ==="); // FIXME actually, these are not all FP...
		for(Example ex : fpArray)
			System.out.println(map.getURI(ex.getSrc())+" | "+map.getURI(ex.getTgt()));

	}

	private static boolean isInMapping(Example e1) {
		
		for(Example e2 : perfectMapping) {
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
