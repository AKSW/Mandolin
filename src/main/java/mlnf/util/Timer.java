package mlnf.util;

import java.util.ArrayList;

import org.apache.log4j.Logger;

/**
 * @author author <email>
 *
 */
public class Timer {
	
	private final static Logger LOGGER = Logger.getLogger("ROCKER");
	
	private ArrayList<Long> stops;
	
	public Timer() {
		stops = new ArrayList<>();
		stops.add(System.currentTimeMillis());
	}
	
	public void lap() {
		stops.add(System.currentTimeMillis());
	}
	
	public double getLapSeconds(int lap) {
		if(stops.size() < 2)
			return Double.NaN;
		return (stops.get(lap + 1) - stops.get(lap)) / 1000.0;
	}

	public double getLapMillis(int lap) {
		if(stops.size() < 2)
			return Double.NaN;
		return (stops.get(lap + 1) - stops.get(lap));
	}
	
	public double getLastLapSeconds() {
		if(stops.size() < 2)
			return Double.NaN;
		return (stops.get(stops.size() - 1) - stops.get(stops.size() - 2)) / 1000.0;
	}

	public double getLastLapMillis() {
		if(stops.size() < 2)
			return Double.NaN;
		return stops.get(stops.size() - 1) - stops.get(stops.size() - 2);
	}
	
	public int getSize() {
		return stops.size() - 1;
	}
	
	public static void main(String[] args) throws InterruptedException {
		Timer t = new Timer();
		Thread.sleep(1000);
		t.lap();
		LOGGER.info(t.getLastLapMillis());
		LOGGER.info(t.getLapMillis(0));
		Thread.sleep(500);
		t.lap();
		LOGGER.info(t.getLastLapSeconds());
		LOGGER.info(t.getLapSeconds(1));
	}

}

