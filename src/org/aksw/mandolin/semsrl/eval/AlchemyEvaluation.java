package org.aksw.mandolin.semsrl.eval;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Scanner;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aksw.mandolin.semsrl.controller.MappingFactory;
import org.aksw.mandolin.semsrl.model.ConnectedGroup;
import org.aksw.mandolin.semsrl.model.DataSource;
import org.aksw.mandolin.semsrl.model.Mapping;
import org.aksw.mandolin.semsrl.util.Bundle;
import org.apache.jena.atlas.lib.SetUtils;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class AlchemyEvaluation {

//	private static final double THRESHOLD = 0.08;
	
	// TODO hard coded: remove me!
	private static final String[] DATASRC_IDS = {"dblp", "acm"};
	
	private static final int EVAL_INTERVAL_FROM = 100;
	private static final int EVAL_INTERVAL_TO   = 1000;
	private static final int EVAL_INTERVAL_STEP = 100;
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static void run(String[] args) throws ClassNotFoundException, IOException {
		
		for(int i=EVAL_INTERVAL_FROM; i<=EVAL_INTERVAL_TO; i+=EVAL_INTERVAL_STEP)
			evaluate(args, i / 1000.0);
		
	}
	
	private static void evaluate(String[] args, double threshold) throws ClassNotFoundException, IOException {
		
		Bundle.setBundleName(args[0]);

		TreeSet<String> perfmap = new TreeSet<>();
		TreeSet<String> posclass = new TreeSet<>();
		
		// TODO remove me. (load training set)
		TreeSet<String> training = new TreeSet<>();
		Scanner in1 = new Scanner(new File(args[0] + "-alchemy/" + args[0] + ".tr"));
		while(in1.hasNextLine())
			training.add(in1.nextLine());
		in1.close();
		
		Mapping mapping = MappingFactory.createMapping(args[0]);
		for(ConnectedGroup cg : mapping.getGroups()) {
			String concat = "";
			for(DataSource ds : cg.getDataSources())
				concat += ds.getNamespace() + cg.getResourceURI(ds) + "#_ ";
			String instance = concat.trim();
			perfmap.add(instance);
//			TODO restore if(cg.isTraining())
			if(training.contains(instance))
				posclass.add(instance);
//			System.out.println(instance);
		}
		
		HashMap<String, String> map = readMap(args[0] + "-alchemy/" + args[0] + ".map");
		
		String file = args[0] + "-alchemy/" + args[0] + "-out.mln";
		Scanner in = new Scanner(new File(file));
		
		double precision, recall, fscore, tp = 0, tn = 0, fp = 0, fn = 0;
		
		while(in.hasNextLine()) {
			String[] line = in.nextLine().split(" ");
			String[] arg = extract(line[0]).split(",");
			double val = Double.parseDouble(line[1]);
			
			if(isSameDatasrc(arg) || arg[0].compareTo(arg[1]) > 0)
				continue;

//			System.out.println(arg[0] + "," + arg[1] + "\t"
//					+map.get(arg[0]) + " " + map.get(arg[1]) + " -> "+val);
			
			if(val >= threshold)
				posclass.add(map.get(arg[0]) + " " + map.get(arg[1]));
		}
		in.close();
		
		tp = SetUtils.intersection(perfmap, posclass).size();
		fp = SetUtils.difference(posclass, perfmap).size();
		fn = SetUtils.difference(perfmap, posclass).size();
		tn = Math.pow(mapping.getGroups().size(), 2) - tp - fp - fn;
		
		precision = (tp+fp == 0) ? 0.0 : tp / (tp+fp) * 100.0;
		recall = (tp+fp == 0) ? 0.0 : tp / (tp+fn) * 100.0;
		fscore = (precision+recall == 0) ? 0.0 : 2 * precision * recall / (precision + recall);
		
		String details = "";
		details += "tp = " + tp + "\tfp = " + fp + "\t";
		details += "tn = " + tn + "\tfn = " + fn + "\t";
		details += "pr% = " + precision + "\trc% = " + recall + "\t";
		details += "fscore% = " + fscore + "\n";
		details += "THR = " + threshold + "\t" + precision + "\t" + recall + "\t" + fscore;
		System.out.println(details);
	}
	
	private static String extract(String in) {
		Matcher m = Pattern.compile(".*\\((.*?)\\)").matcher(in);
		m.find();
		return m.group(1);
	}
	
	private static boolean isSameDatasrc(String[] arg) {
		int[] ind = new int[2];
		for(int j=0; j<2; j++) {
			String s = arg[j].split("_")[1];
			for(int i=0; i<DATASRC_IDS.length; i++)
				if(DATASRC_IDS[i].equals(s)) {
					ind[j] = i;
					break;
				}
		}
		return ind[0] == ind[1];
	}
	
	@SuppressWarnings({"unchecked"})
	public static HashMap<String, String> readMap(String file) throws FileNotFoundException, IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
        HashMap<String, String> anotherMap = (HashMap<String, String>) ois.readObject();
        ois.close();
        HashMap<String, String> otherWayRound = new HashMap<>();
        for(String key : anotherMap.keySet())
        	otherWayRound.put(anotherMap.get(key), key);
		return otherWayRound;
	}


}
