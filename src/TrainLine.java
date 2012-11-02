import java.util.LinkedList;
import java.util.List;

/**  
 * This class contains information about a 
 * train line line of the MBTA based on
 * the JSON data from the MBTA website 
 * */

public class TrainLine {
	
	// Keys for accessing values in JSON files
	private static final String CURRENT_TIME_KEY = "CurrentTime";
	private static final String LINE_KEY = "Line";
	private static final String TRIPS_KEY = "Trips";
	
	// Current time
	private int currentTime;
	// Line name
	private String line;
	// List of trains
	private LinkedList<Train> trains;
	
	// TrainLine constructors
	public TrainLine() { }
	
	public TrainLine(Object tripListObj) {
		// Get current time
		Object timeObj = TripPlanner.getFromMap(tripListObj, CURRENT_TIME_KEY);
		this.currentTime = TripPlanner.getIntFromObject(timeObj);
		
		// Get line name
		Object lineObj = TripPlanner.getFromMap(tripListObj, LINE_KEY);
		this.line = TripPlanner.getStringFromObject(lineObj);
		
		// Get trains list
		LinkedList<Train> ts = new LinkedList<Train>();
		Object trainsObj = TripPlanner.getFromMap(tripListObj, TRIPS_KEY);
		List<?> temp = TripPlanner.getListFromObject(trainsObj);
		// Create and add each train to list
		for (Object t : temp) {
			ts.add(new Train(t));
		}
		this.trains = ts;
	}
	
	/** 
	 * Accessor Methods
	 * */
	public int getTime() {
		return currentTime;
	}
	
	public String getLine() {
		return line;
	}
	
	public LinkedList<Train> getTrains() {
		return trains;
	}
}