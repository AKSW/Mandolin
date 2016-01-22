package org.aksw.mandolin.eval;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.aksw.mandolin.Mandolin;
import org.aksw.mandolin.model.Cache;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class CrossValidation {
	
	public static final int N_FOLDS = 10;
	
	/**
	 * Path where setP, setM, and setD lie.
	 */
	public static final String SET_PATH_SUFFIX = "/cv/sets";

	/**
	 * The union of all partitions (splits) form the original dataset.
	 */
	public static final String PART_PATH_SUFFIX = "/cv/partitions";
	
	/**
	 * Folds are pairs of training and test sets.
	 */
	public static final String FOLD_PATH_SUFFIX = "/cv/folds";
	
	/**
	 * A run path is the workspace for a given fold. The fold number will be appended to this.
	 */
	public static final String RUN_PATH_SUFFIX = "/cv/run";
	
	/**
	 * @param args
	 * @throws Exception 
	 * @throws NumberFormatException 
	 */
	public static void main(String[] args) throws NumberFormatException, Exception {
		
		run(args[0], args[1], args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]), Boolean.parseBoolean(args[6]), Boolean.parseBoolean(args[7]),
				Boolean.parseBoolean(args[8]));
		
	}

	/**
	 * @param workspace
	 * @param inputPaths
	 * @param aimRelation
	 * @param thrMin
	 * @param thrStep
	 * @param thrMax
	 * @param enableOnt
	 * @param enableFwc
	 * @param reverseCV
	 * @throws Exception 
	 */
	public static void run(String workspace, String inputPaths, String aimRelation, int thrMin, int thrStep, int thrMax, boolean enableOnt, boolean enableFwc, boolean reverseCV) throws Exception {
		
		// announce
		String cvType = reverseCV ? "reverse " : "";
		cvType += N_FOLDS + "-fold Cross-Validation";
		System.out.println("Starting " + cvType + "...");
		
		// create folders
		String setPath = workspace + SET_PATH_SUFFIX;
		String partitionPath = workspace + PART_PATH_SUFFIX;
		String foldPath = workspace + FOLD_PATH_SUFFIX;
		new File(setPath).mkdirs();
		new File(partitionPath).mkdirs();
		new File(foldPath).mkdirs();
		
		// divide aim relations (setP) and base dataset (setM) from original dataset (setD)
		divide(setPath, inputPaths, aimRelation);
		
		// partition aim relations into N_FOLDS parts
		partition(setPath, partitionPath);
		
		// create training/test sets, appending setM
		fold(setPath, partitionPath, foldPath);
		
		HashMap<Double, ArrayList<Double>> f1s = new HashMap<>();
		for(int th=0; th<=10; th+=1) {
			double theta = th / 10.0;
			f1s.put(theta, new ArrayList<>());
		}
		
		// for each fold, launch Mandolin
		for(int i=0; i<N_FOLDS; i++) {
			
			System.out.println("\n============= FOLD "+i+" =============\n");
			
			String runPath = workspace + RUN_PATH_SUFFIX + i;
			String trainingPath = foldPath + "/training" + i + ".nt";
			
			Mandolin m = new Mandolin(runPath, trainingPath, aimRelation, thrMin, thrStep, thrMax, enableOnt, enableFwc);
			m.run();
			
			// each theta has different results
			for(int th=0; th<=10; th+=1) {
				double theta = th / 10.0;
				System.out.println("\ntheta = "+theta);
				Evaluation eval = new Evaluation(runPath + "/output_" + theta + ".nt", partitionPath + "/" + i + ".nt");
				eval.run();
				f1s.get(theta).add(eval.getF1());
			}
		}
		
		for(Double theta : f1s.keySet())
			System.out.println("\ntheta = "+theta+"\tf1 = "+f1s.get(theta));
		
		
	}

	/**
	 * @param setPath
	 * @param partitionPath
	 * @param foldPath
	 */
	private static void fold(String setPath, String partitionPath, String foldPath) {
		
		final FileOutputStream[] output = new FileOutputStream[N_FOLDS];
		StreamRDF[] out = new StreamRDF[N_FOLDS];
		for(int i=0; i<N_FOLDS; i++) {
			try {
				output[i] = new FileOutputStream(new File(foldPath + "/training" + i + ".nt"));
				out[i] = StreamRDFWriter.getWriterStream(output[i], Lang.NT);
				out[i].start();
			} catch (FileNotFoundException e) {
				System.out.println(e.getMessage());
				return;
			}
			
		}
		
		for(int i=0; i<N_FOLDS; i++) {
			
			for(int j=0; j<N_FOLDS; j++) {
				
				if(i != j) {
					
					System.out.println("Adding partition "+j+" to fold "+i);
				
					final int J = j;
					
					StreamRDF dataStream = new StreamRDF() {
	
						@Override
						public void start() {
						}
	
						@Override
						public void triple(Triple triple) {
							out[J].triple(triple);
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
	
					RDFDataMgr.parse(dataStream, partitionPath + "/" + j + ".nt");
					
				}

			}
			
			// append setM
			final int I = i;
			
			StreamRDF dataStream = new StreamRDF() {
				
				@Override
				public void start() {
				}
				
				@Override
				public void triple(Triple triple) {
					out[I].triple(triple);
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
			
			RDFDataMgr.parse(dataStream, setPath + "/setM.nt");
			
		}
		
		for(int i=0; i<N_FOLDS; i++) {
			out[i].finish();
			System.out.println("File "+foldPath + "/training" + i + ".nt created.");
		}

	}

	/**
	 * @param setPath
	 * @param partitionPath
	 */
	private static void partition(String setPath, String partitionPath) {
		
		final FileOutputStream[] output = new FileOutputStream[N_FOLDS];
		StreamRDF[] out = new StreamRDF[N_FOLDS];
		for(int i=0; i<N_FOLDS; i++) {
			try {
				output[i] = new FileOutputStream(new File(partitionPath + "/" + i + ".nt"));
				out[i] = StreamRDFWriter.getWriterStream(output[i], Lang.NT);
				out[i].start();
			} catch (FileNotFoundException e) {
				System.out.println(e.getMessage());
				return;
			}
		}
		
		final Cache count = new Cache();
		
		StreamRDF dataStream = new StreamRDF() {

			@Override
			public void start() {
			}

			@Override
			public void triple(Triple triple) {
				out[count.count % N_FOLDS].triple(triple);
				count.count++;
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

		RDFDataMgr.parse(dataStream, setPath + "/setP.nt");
		
		for(int i=0; i<N_FOLDS; i++) {
			out[i].finish();
			System.out.println("File "+partitionPath + "/" + i + ".nt created.");
		}
		
	}

	/**
	 * @param workspace
	 * @param inputPaths
	 * @param aimRelation
	 */
	private static void divide(String workspace, String inputPaths,
			String aimRelation) {
		
		String[] paths = inputPaths.split(",");
		
		final FileOutputStream setP, setM;
		try {
			setP = new FileOutputStream(new File(workspace + "/setP.nt"));
			setM = new FileOutputStream(new File(workspace + "/setM.nt"));
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
			return;
		}
		
		StreamRDF setPw = StreamRDFWriter.getWriterStream(setP, Lang.NT);
		StreamRDF setMw = StreamRDFWriter.getWriterStream(setM, Lang.NT);

		setPw.start();
		setMw.start();
		
		final Cache count = new Cache();
		
		StreamRDF dataStream = new StreamRDF() {

			@Override
			public void start() {
			}

			@Override
			public void triple(Triple triple) {
				if(triple.getPredicate().getURI().equals(aimRelation)) {
					setPw.triple(triple);
					count.count++;
				} else
					setMw.triple(triple);
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

		for(String path : paths)
			RDFDataMgr.parse(dataStream, path);
		
		setPw.finish();
		System.out.println("File "+workspace + "/setP.nt created.\n"
				+"Property <"+aimRelation+"> was found in "+count.count+" statements.");
		setMw.finish();
		System.out.println("File "+workspace + "/setM.nt created.");
		
	}

}
