import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.*;

public abstract class TripPlanner {
	// Keys for accessing values in JSON files
	private static final String TRIP_LIST_KEY = "TripList";

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

	// Constants for JSON URLs
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
	private static final File TEST_RED;
	static {
		File temp;
		temp = new File("MBTA_test_data/2012_10_19/TestRed_2012_10_19.json");
		TEST_RED = temp;
	}
	private static final File TEST_BLUE;
	static {
		File temp;
		temp = new File("MBTA_test_data/2012_10_19/TestBlue_2012_10_19.json");
		TEST_BLUE = temp;
	}
	private static final File TEST_ORANGE;
	static {
		File temp;
		temp = new File("MBTA_test_data/2012_10_19/TestOrange_2012_10_19.json");
		TEST_ORANGE = temp;
	}
	private static Views view;

	public static void main(String[] args) {
		// Initialize train lines
		blue = new TrainLine();
		orange = new TrainLine();
		red = new TrainLine();
		liveLines = new LinkedList<TrainLine>();
		testRed = new TrainLine();
		testBlue = new TrainLine();
		testOrange = new TrainLine();
		testLines = new LinkedList<TrainLine>();
		// Sets default to use live data instead of  test data
		liveData = true;
		
		if (!liveData) {
			testRed = updateLine(TEST_RED, testRed);
			testOrange = updateLine(TEST_ORANGE, testOrange);
			testBlue = updateLine(TEST_BLUE, testBlue);
			testLines.add(testRed);
			testLines.add(testOrange);
			testLines.add(testBlue);

			view = new Views(testLines);
		}
		else {
			// Update lines and view
			update();

			view = new Views(liveLines);
		}

		System.out.println(blue.toString());
		System.out.println(red.toString());
		System.out.println(orange.toString());
	}
	
	// Updates all train lines
	private static void update() {
		orange = updateLine(ORANGE_URL, orange);
		red = updateLine(RED_URL, red);
		blue = updateLine(BLUE_URL, blue);
		liveLines.add(orange);
		liveLines.add(red);
		liveLines.add(blue);
	}

	// Update and return given train line with the Jackson parser
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
			System.out.println(tripListObj.toString());

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
	 * */

	// Check if a value exists before getting it
	static Object getFromMap(Object map, String key) {
		if (map instanceof Map<?,?>) {
			Map<?,?> castMap = (Map<?,?>) map;
			if (castMap.containsKey(key))
				return castMap.get(key);
			else
				return null;
		}
		else
			return null;
	}

	// Return given object as an int
	static int getIntFromObject(Object o) {
		int temp = 0;
		if (o instanceof Integer)
			temp = mapper.convertValue(o, Integer.class);
		return temp;
	}

	// Return given object as a double
	static double getDoubleFromObject(Object o) {
		double temp = 0.0;
		if (o instanceof Double)
			temp = mapper.convertValue(o, Double.class);
		return temp;
	}

	// Return given object as a String
	static String getStringFromObject(Object o) {
		String temp = "";
		if (o instanceof String)
			temp = mapper.convertValue(o, String.class);
		return temp;
	}

	// Return given object as a list
	static List<?> getListFromObject(Object o) {
		List<?> temp = new LinkedList();
		if (o instanceof List<?>)
			temp = mapper.convertValue(o, List.class);
		return temp;
	}
}