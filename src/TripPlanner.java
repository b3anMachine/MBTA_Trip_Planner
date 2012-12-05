import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;

public abstract class TripPlanner {
	// Keys for accessing values in JSON files
	private static final String TRIP_LIST_KEY = "TripList";
	private static final int REPEAT_TIME = 10000; // Milliseconds (10)
	private static final int TIMER_DELAY = 10000; // Milliseconds (10)

	public enum Direction{
		NORTHBOUND,
		SOUTHBOUND,
		STATIC
	}
	// Train Graph
	static TrainGraph graph;

	// Whether we're using live data or not
	private static boolean liveData;
	// Live Lines
	private static LinkedList<TrainLine> liveLines;
	// Blue Line
	private static TrainLine blue;
	// Red Line
	private static TrainLine red;
	// Orange Line
	private static TrainLine orange;
	// Test Lines
	private static LinkedList<TrainLine> testLines;
	// Test Red Line
	private static TrainLine testRed;
	// Test Blue Line
	private static TrainLine testBlue;
	// Test Orange Line
	private static TrainLine testOrange;
	// Jackson processor ObjectMapper
	static ObjectMapper mapper = new ObjectMapper();
	// List of Stops
	static LinkedList<Stop> stops = new LinkedList<Stop>();

	/**
	 * Constants for JSON URLs
	 * **/
	// Orange line live data URL
	private static final URL ORANGE_URL;
	static {
		URL temp;
		try {
			temp = new URL("http://developer.mbta.com/lib/rthr/orange.json");
		} catch (MalformedURLException e) {
			temp = null;
		}
		ORANGE_URL = temp;
	}
	// Blue line live data URL
	private static final URL BLUE_URL;
	static {
		URL temp;
		try {
			temp = new URL("http://developer.mbta.com/lib/rthr/blue.json");
		} catch (MalformedURLException e) {
			temp = null;
		}
		BLUE_URL = temp;
	}
	// Red line live data URL
	private static final URL RED_URL;
	static {
		URL temp;
		try {
			temp = new URL("http://developer.mbta.com/lib/rthr/red.json");
		} catch (MalformedURLException e) {
			temp = null;
		}
		RED_URL = temp;
	}
	// Red line test data URL
	private static final File TEST_RED;
	static {
		File temp;
		temp = new File("MBTA_test_data/2012_10_19/TestRed_2012_10_19.json");
		TEST_RED = temp;
	}
	// Blue line test data URL
	private static final File TEST_BLUE;
	static {
		File temp;
		temp = new File("MBTA_test_data/2012_10_19/TestBlue_2012_10_19.json");
		TEST_BLUE = temp;
	}
	// Orange line test data URL
	private static final File TEST_ORANGE;
	static {
		File temp;
		temp = new File("MBTA_test_data/2012_10_19/TestOrange_2012_10_19.json");
		TEST_ORANGE = temp;
	}
	// An instance of the Views class
	private static Views view;

	public static void main(String[] args) {
		generateStops();

		// Initialize train lines
		blue = new TrainLine();
		orange = new TrainLine();
		red = new TrainLine();
		liveLines = new LinkedList<TrainLine>();
		testRed = new TrainLine();
		testBlue = new TrainLine();
		testOrange = new TrainLine();
		testLines = new LinkedList<TrainLine>();
		testOrange = updateLine(TEST_ORANGE, orange);
		testRed = updateLine(TEST_RED, red);
		testBlue = updateLine(TEST_BLUE, blue);
		testLines.add(testRed);
		testLines.add(testBlue);
		testLines.add(testOrange);

		// Sets default to use live data instead of  test data
		liveData = true;

		// Update lines and view
		update();
		view = new Views(liveLines, stops);

		createUpdateTask();

		/*
		LinkedList<Integer> goals = new LinkedList<Integer>();
		goals.add(1);
		goals.add(2);
		goals.add(32);
		goals.add(23);
		graph.multiSearch(goals);
		graph.unorderedPermSearch(goals);
		*/
	}
	
	// Sets up timer to update trains every 10 seconds
	// CM and AG and NF
	private static void createUpdateTask() {
		Timer updateTimer = new Timer();

		class Updater extends TimerTask{
			public void run() {
				if (liveData) {
					TripPlanner.update();
					view.setLines(liveLines);
				}
			}
		}
		TimerTask updateTask = new Updater();
		updateTimer.schedule(updateTask, TIMER_DELAY, REPEAT_TIME);
	}

	// Updates all train lines
	private static void update() {
		orange = updateLine(ORANGE_URL, orange);
		red = updateLine(RED_URL, red);
		blue = updateLine(BLUE_URL, blue);
		liveLines.clear();
		liveLines.add(red);
		liveLines.add(blue);
		liveLines.add(orange);
	}

	// Switches between live and test data
	public static void toggleLiveData() {
		if (liveData) {
			liveData = false;
			view.setLines(testLines);
		}
		else {
			liveData = true;
			update();
			view.setLines(liveLines);
			//createUpdateTask();
		}
	}

	// Update and return given train line with the Jackson parser
	// AG
	public static <T> TrainLine updateLine(T address, TrainLine line) {
		try {
			Object trainData = new Object();
			if (address instanceof URL) {
				// Get train data from web
				trainData = mapper.readValue((URL) address, Object.class);
			}
			else if (address instanceof File) {
				// Get train data locally
				trainData = mapper.readValue((File) address, Object.class);
			}
			//System.out.println(trainData.toString());
			// Go inside the wrapper
			Object tripListObj = getFromMap(trainData, TRIP_LIST_KEY);

			line = new TrainLine(tripListObj);
		}
		catch (JsonParseException e) {
			e.printStackTrace();
		} 
		catch (JsonMappingException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}

		return line;
	}

	/**
	 * Deal with Objects received from JSON
	 * AG
	 * */

	// Check if a value exists before getting it
	// AG
	static Object getFromMap(Object map, String key) {
		if (map instanceof Map<?,?>) {
			Map<?,?> castMap = (Map<?,?>) map;
			if (castMap.containsKey(key)) {
				return castMap.get(key);
			}
			else {
				return null;
			}
		}
		else {
			return null;
		}
	}

	// Return given object as an int
	// AG
	static int getIntFromObject(Object o) {
		int temp = 0;
		if (o instanceof Integer)
			temp = mapper.convertValue(o, Integer.class);
		return temp;
	}

	// Return given object as a double
	// AG
	static double getDoubleFromObject(Object o) {
		double temp = 0.0;
		if (o instanceof Double)
			temp = mapper.convertValue(o, Double.class);
		return temp;
	}

	// Return given object as a String
	// AG
	static String getStringFromObject(Object o) {
		String temp = "";
		if (o instanceof String)
			temp = mapper.convertValue(o, String.class);
		return temp;
	}

	// Return given object as a list
	// AG
	static List<?> getListFromObject(Object o) {
		@SuppressWarnings("rawtypes")
		List<?> temp = new LinkedList();
		if (o instanceof List<?>)
			temp = mapper.convertValue(o, List.class);
		return temp;
	}

	// Generates the list of stops from the included stops file
	// AG
	public static void generateStops() {
		try {
			JsonFactory f = new JsonFactory();
			JsonParser jp = f.createJsonParser(new File("stopsAdjacent.json"));
			// advance stream to START_ARRAY first:
			jp.nextToken();
			// and then each time, advance to opening START_OBJECT
			while (jp.nextToken() == JsonToken.START_OBJECT) {
				Stop stop = mapper.readValue(jp, Stop.class);
				stops.add(stop);
				// after binding, stream points to closing END_OBJECT
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		graph = new TrainGraph(stops);
	}

	/**
	 * Returns the name of a stop
	 * @author NF and AG
	 * **/
	public static String getStopNameByID(int stopID) {
		String stopName = "";
		for (Stop s : stops) {
			if (s.stopID == stopID)
				stopName = s.stop_name;
		}
		return stopName;
	}
	
	/**
	 * Returns Stop given a Stop's name
	 * @author NF
	 * **/
	public static Stop getStopByName(String name){
		Stop tempStop = new Stop();
		for(Stop s : stops){
			if(s.stop_name.equals(name))
				tempStop = s;
		}
		return tempStop;		
	}
	
	//gets Stop by ID
	public static Stop getStopByID(int stopID){		
		return getStopByName(getStopNameByID(stopID));
	}
	//returns a public enum Direction based on last and next stop id's
	//NF
	public static Direction getDirection(String last, String next){
		if(getStopByName(last).stopID > getStopByName(next).stopID){
			return Direction.SOUTHBOUND;

		}
		else if(getStopByName(last).stopID < getStopByName(next).stopID){
			return Direction.NORTHBOUND;

		}
		else{ return Direction.STATIC;}
	}
	public static Train getTrainAtStop(String stopName, Direction d){
		return getTrainAtStop(stopName, d ,0);
	}
	//Returns the train with the smallest time until arrival for the given stopname
	//NF
	public static Train getTrainAtStop(String stopName, Direction d, int offset){
		LinkedList<Train> curTrains = new LinkedList<Train>();
		for (TrainLine line : (liveData ? liveLines : testLines)) {			
			LinkedList<Train> trains = line.getTrains();			
			for (int t = 0; t < trains.size(); t++) {
				Train curTrain = trains.get(t);
				if(curTrain.containsStop(stopName, offset) && curTrain.getTrainDirection() == d){
					curTrains.add(curTrain);
				}
			}
		}
		return getEarliestTrain(curTrains, stopName);
	}
	
	//Compares 2 trains and returns true if o1 has a greater time than o2
	//for the first prediction
	//NF
    public static boolean compare(Train o1, Train o2, String stopName) {
    	return o1.getPredictionByName(stopName).getTime() > o2.getPredictionByName(stopName).getTime();
    }
    
    //Consumes a LinkedList of Trains and returns the one with the smallest time
    //for the first element in the list of predictions
    //NF
	public static Train getEarliestTrain(LinkedList<Train> trains, String stopName){
		//for(int t=0;t<trains.size();t++){
		if(trains.isEmpty()){
			return null;
		}
		else if(trains.size() == 1){
			return trains.get(0);
		}
		else if(compare(trains.get(0),trains.get(1),stopName)){
			trains.remove(0);
			return getEarliestTrain(trains,stopName);
		}
		else{
			trains.remove(1);
			return getEarliestTrain(trains,stopName);
		}

	}
}