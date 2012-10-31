/** 
 * Position class used to contain train position information 
 * */
public class Position {
	
	// Keys for accessing values in JSON files
	private static final String TIMESTAMP_KEY = "Timestamp";
	//private static final String TRAIN_KEY = "Train";
	private static final String LAT_KEY = "Lat";
	private static final String LONG_KEY = "Long";
	private static final String HEADING_KEY = "Heading";
	
	// Timestamp of last update
	private int timestamp;
	// Latitude of train
	private double latitude;
	// Longitude of train
	private double longitude;
	// Heading of train
	private int heading;
	
	// Position constructor
	public Position(Object posObj) {
		// Get timestamp
		Object timeObj = TripPlanner.getFromMap(posObj, TIMESTAMP_KEY);
		this.timestamp = TripPlanner.getIntFromObject(timeObj);
		
		// Get latitude
		Object latObj = TripPlanner.getFromMap(posObj, LAT_KEY);
		this.latitude = TripPlanner.getDoubleFromObject(latObj);
		
		// Get longitude
		Object longObj = TripPlanner.getFromMap(posObj, LONG_KEY);
		this.longitude = TripPlanner.getDoubleFromObject(longObj);
		
		// Get heading
		Object headObj = TripPlanner.getFromMap(posObj, HEADING_KEY);
		this.heading = TripPlanner.getIntFromObject(headObj);
	}
	
	/** 
	 * Accessor Methods
	 * */	
	public int getTime() {
		return timestamp;
	}
	
	public double getLat() {
		return latitude;
	}
	
	public double getLong() {
		return longitude;
	}
	
	public int getHeading() {
		return heading;
	}	
}
