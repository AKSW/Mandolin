
package org.aksw.mandolin.model;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.TreeSet;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class PredictionSet extends TreeSet<PredictionLiteral> implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 864082651004354757L;

	/**
	 * Internal name only.
	 */
	private String aim;
	
	public PredictionSet(String aim) {
		this.aim = aim;
		System.out.println("Created prediction set with aim: "+aim);
	}
	
	public String getAim() {
		return aim;
	}
	
	public void saveTo(String path) {
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(path));
			oos.writeObject(this);
			oos.close();
			System.out.println("Predictions saved to "+path);
		} catch (IOException e) {
			System.out.println("Cannot save "+this.toString()+": "+e.getMessage());
		}
	}

}
