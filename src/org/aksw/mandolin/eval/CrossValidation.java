package org.aksw.mandolin.eval;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

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
	
	public static void main(String[] args) {
		
		run(args[0], args[1], args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]), Boolean.parseBoolean(args[6]), Boolean.parseBoolean(args[7]),
				Boolean.parseBoolean(args[8]));
		
	}

	public static void run(String workspace, String inputPaths, String aimRelation, int thrMin, int thrStep, int thrMax, boolean enableOnt, boolean enableFwc, boolean reverseCV) {
		
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
		
		// TODO for each fold, launch Mandolin
		
		
		
//		Mandolin m = new Mandolin(workspace, inputPaths, aimRelation, thrMin, thrStep, thrMax, enableOnt, enableFwc);
		
	}

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
