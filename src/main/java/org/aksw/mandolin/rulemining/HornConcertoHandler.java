package org.aksw.mandolin.rulemining;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.aksw.mandolin.controller.NameMapper;
import org.aksw.mandolin.util.Bundle;
import org.aksw.mandolin.util.PrettyRandom;
import org.aksw.mandolin.util.Shell;
import org.aksw.mandolin.util.Timer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;

/**
 * @author Tommaso Soru {@literal tsoru@informatik.uni-leipzig.de}
 *
 */
public class HornConcertoHandler implements IHandler {

	private final static Logger logger = LogManager
			.getLogger(HornConcertoHandler.class);

	/**
	 * Top N properties allowed to be in the head.
	 */
	private static final int TOP_PROP = 40;

	/**
	 * Max number of triangle types to check for xyz rules.
	 */
	private static final int MAX_TRIANG = 20;

	/**
	 * Min confidence score (i.e., support) for a rule.
	 */
	private Double MINING_DEFAULT = 0.1;

	private String ontology;
	
	// --------------------------

	public HornConcertoHandler(String ontology) {
		super();
		this.ontology = ontology;
	}

	public void run(NameMapper map, String base, Double mining) {
		
		if(mining == null)
			mining = MINING_DEFAULT;

		String home = System.getProperty("user.dir");
		String endpoint = Bundle.getString("sparql_endpoint");
		String graph = "http://localhost/" + PrettyRandom.get(6);

		logger.info("Installing graph...");
		String cmd1 = "bash " + home + "/horn-concerto/install-graph.sh "
				+ home + "/" + ontology + " " + graph;
		logger.info("> " + cmd1);
		String s1 = Shell.execute(cmd1, false);
		logger.info(s1);

		try {
			
			final String command = "/opt/virtuoso-opensource/bin/isql 1111 dba dba "
					+ "/opt/virtuoso-opensource/var/lib/virtuoso/db/script.sql";
			DockerClient dockerClient = DockerClientBuilder.getInstance()
					.build();

			ExecCreateCmdResponse execCreateCmdResponse = dockerClient
					.execCreateCmd("virtuoso").withAttachStdout(true)
					.withCmd(command.split(" ")).exec();
			dockerClient.execStartCmd(execCreateCmdResponse.getId())
					.exec(new ExecStartResultCallback(System.out, System.err))
					.awaitCompletion();

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error with Docker container 'virtuoso'. "+e.getMessage());
		}

		Timer t = new Timer();
		logger.info("Running Horn Concerto on " + ontology);
		String cmd2 = "python horn-concerto/horn_concerto.py " + endpoint + " "
				+ graph + " " + mining + " " + TOP_PROP + " " + MAX_TRIANG + " " + base;
		logger.info("> " + cmd2);
		Shell.execute(cmd2);
		t.lap();
		logger.info("Rule mining done in " + t.getLastLapSeconds() + " seconds.");
		
		String[] rules = {"rules-pxy-qxy.tsv", "rules-pxy-qyx.tsv", "rules-pxy-qxz-ryz.tsv", 
				"rules-pxy-qxz-rzy.tsv", "rules-pxy-qzx-ryz.tsv", "rules-pxy-qzx-rzy.tsv"};
		
		logger.info("Interpreting rules...");
		HornConcertoRuleDriver driver = new HornConcertoRuleDriver(map, base);
		for(int type=1; type<=6; type++) {
			String ruleF = rules[type-1];
			Scanner in = null;
			try {
				in = new Scanner(new File(base + "/" + ruleF));
				for(int i=0; in.hasNextLine(); i++) {
					String line = in.nextLine();
					logger.info(line);
					// skip header
					if(i > 0) {
						String[] data = line.split("\t");
						Double weight = Double.parseDouble(data[0]);
						if(type <= 2)
							// body size=1
							driver.process(type, weight, data[1], data[3]);
						else
							// body size=2
							driver.process(type, weight, data[1], data[3], data[5]);
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} finally {
				in.close();
			}
		}
		driver.buildCSV();
		logger.info("Interpretation done.");
	}

}
