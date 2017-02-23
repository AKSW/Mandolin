package mlnf.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author author <email>
 *
 */
public class DataIO {
	
	public static <T> void serialize(ArrayList<T> list, String filepath) throws FileNotFoundException, IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filepath));
        oos.writeObject(list);
        oos.close();
	}
	
	public static <T, U> void serialize(HashMap<T, U> map, String filepath) throws FileNotFoundException, IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filepath));
        oos.writeObject(map);
        oos.close();
	}

	@SuppressWarnings("unchecked")
	public static <T> ArrayList<T> readList(String filepath) throws FileNotFoundException, IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filepath));
        ArrayList<T> list = (ArrayList<T>) ois.readObject();
        ois.close();
		return list;
	}

	@SuppressWarnings("unchecked")
	public static <T, U> HashMap<T, U> readMap(String filepath) throws FileNotFoundException, IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filepath));
        HashMap<T, U> map = (HashMap<T, U>) ois.readObject();
        ois.close();
		return map;
	}

}

