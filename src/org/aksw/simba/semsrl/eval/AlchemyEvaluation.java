package org.aksw.simba.semsrl.eval;

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

import org.aksw.simba.semsrl.controller.MappingFactory;
import org.aksw.simba.semsrl.io.Bundle;
import org.aksw.simba.semsrl.model.ConnectedGroup;
import org.aksw.simba.semsrl.model.DataSource;
import org.aksw.simba.semsrl.model.Mapping;
import org.apache.jena.atlas.lib.SetUtils;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class AlchemyEvaluation {

	private static final double THRESHOLD = 0.4;

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws ClassNotFoundException, IOException {
		
		Bundle.setBundleName(args[0]);

		TreeSet<String> perfmap = new TreeSet<>();
		TreeSet<String> posclass = new TreeSet<>();
		
		Mapping mapping = MappingFactory.createMapping(args[0]);
		for(ConnectedGroup cg : mapping.getGroups()) {
			String concat = "";
			for(DataSource ds : cg.getDataSources())
				concat += ds.getNamespace() + cg.getResourceURI(ds) + "#_ ";
			perfmap.add(concat.trim());
			System.out.println(concat.trim());
		}
		
		HashMap<String, String> map = readMap(args[0] + "-alchemy/" + args[0] + ".map");
		
		String file = args[0] + "-alchemy/" + args[0] + "-out.mln";
		Scanner in = new Scanner(new File(file));
		
		double precision, recall, fscore, tp = 0, tn = 0, fp = 0, fn = 0;
		
		while(in.hasNextLine()) {
			String[] line = in.nextLine().split(" ");
			String[] arg = extract(line[0]).split(",");
			double val = Double.parseDouble(line[1]);
			
			System.out.println(map.get(arg[0]) + " " + map.get(arg[1]) + " -> "+val);
			
			if(val >= THRESHOLD)
				posclass.add(map.get(arg[0]) + " " + map.get(arg[1]));
			else tn++;
		}
		in.close();
		
		// FIXME this is not correct because it considers also other resources!
		tp = SetUtils.intersection(perfmap, posclass).size();
		fp = SetUtils.difference(posclass, perfmap).size();
		fn = SetUtils.difference(perfmap, posclass).size();
		
		precision = (tp+fp == 0) ? 0.0 : tp / (tp+fp) * 100.0;
		recall = (tp+fp == 0) ? 0.0 : tp / (tp+fn) * 100.0;
		fscore = (precision+recall == 0) ? 0.0 : 2 * precision * recall / (precision + recall);
		
		String details = "";
		details += "tp = " + tp + "\tfp = " + fp + "\n";
		details += "tn = " + tn + "\tfn = " + fn + "\n";
		details += "pr% = " + precision + "\nrc% = " + recall + "\n";
		details += "fscore% = " + fscore;
		System.out.println(details);
	}
	
	private static String extract(String in) {
		Matcher m = Pattern.compile(".*\\((.*?)\\)").matcher(in);
		m.find();
		return m.group(1);
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
