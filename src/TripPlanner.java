import java.awt.List;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public abstract class TripPlanner {
	private boolean liveData;
	private static TripList blue;
	private static TripList red;
	private static TripList orange;
	
	// Constants for JSON URLs
	static final String ORANGE_URL = "http://developer.mbta.com/lib/rthr/orange.json";
	static final String BLUE_URL = "http://developer.mbta.com/lib/rthr/blue.json";
	static final String RED_URL = "http://developer.mbta.com/lib/rthr/red.json";
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		blue = new TripList();
		orange = new TripList();
		red = new TripList();
		update();
		System.out.println(blue.toString());
		System.out.println(red.toString());
		System.out.println(orange.toString());
		

	}
	
	// Updates all train lines
	private static void update() {
		orange = updateLine(ORANGE_URL, orange);
		red = updateLine(RED_URL, red);
		blue = updateLine(BLUE_URL, blue);
	}
	
	// Update one train line with the Jackson parser
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static TripList updateLine(String address, TripList list) {
		ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally
		try {
			URL url = new URL(address);
			System.out.println(url.toURI());
			TripWrapper trip = mapper.readValue(new URL(address), TripWrapper.class);
			list = trip.TripList;
			
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return list;
	}

	
	public static class TripWrapper {
		  public TripList TripList; // or if you prefer, setters+getters
		  
		  
		  
		  public TripWrapper(){
		  }
	}
}
