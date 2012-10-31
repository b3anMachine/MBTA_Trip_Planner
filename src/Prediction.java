/**
 * Class to contain prediction information
 * Says when a train is predicted to arrive at a stop in seconds.
 * */
public class Prediction {
	
	// Keys for accessing values in JSON files
	private static final String STOP_ID_KEY = "StopID";
	private static final String STOP_KEY = "Stop";
	private static final String SECONDS_KEY = "Seconds";
	
	// ID of the stop
	private int stopID;
	// Name of the stop
	private String stopName;
	// Time in seconds until stop is reached
	private int seconds;
	
	// Prediction constructor
	public Prediction(Object predObj) {
		// Get stop id
		Object idObj = TripPlanner.getFromMap(predObj, STOP_ID_KEY);
		this.stopID = TripPlanner.getIntFromObject(idObj);
		
		// Get destination
		Object nameObj = TripPlanner.getFromMap(predObj, STOP_KEY);
		this.stopName = TripPlanner.getStringFromObject(nameObj);
		
		// Get position
		Object secsObj = TripPlanner.getFromMap(predObj, SECONDS_KEY);
		this.seconds = TripPlanner.getIntFromObject(secsObj);
	}
	
	/** 
	 * Accessor Methods
	 * */	
	public int getID() {
		return stopID;
	}
	
	public String getName() {
		return stopName;
	}
	
	public int getTime() {
		return seconds;
	}
}
