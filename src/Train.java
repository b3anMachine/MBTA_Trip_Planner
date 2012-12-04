import java.util.LinkedList;
import java.util.List;
/** 
 * Class to hold train information
 * Train objects to be generated from JSON data.
 * */

public class Train {
	
	// Keys for accessing values in JSON files
	private static final String TRIP_ID_KEY = "TripID";
	private static final String DESTINATION_KEY = "Destination";
	private static final String POSITION_KEY = "Position";
	private static final String PREDICTIONS_KEY = "Predictions";
	
	// Train ID
	private String trainID;
 	// Train's destination
 	private String destination;
 	// Train's position
	private Position position;
	// Train's predictions
	private List<Prediction> predictions;
	
	// Train constructor	
	public Train(Object trainObj) {
		// Get train id
		Object idObj = TripPlanner.getFromMap(trainObj, TRIP_ID_KEY);
		this.trainID = TripPlanner.getStringFromObject(idObj);
		
		// Get destination
		Object destObj = TripPlanner.getFromMap(trainObj, DESTINATION_KEY);
		this.destination = TripPlanner.getStringFromObject(destObj);
		
		// Get position
		Object posObj = TripPlanner.getFromMap(trainObj, POSITION_KEY);
		this.position = new Position(posObj);
		
		// Get predictions
		List<Prediction> preds = new LinkedList<Prediction>();
		Object predsObj = TripPlanner.getFromMap(trainObj, PREDICTIONS_KEY);
		List<?> temp = TripPlanner.getListFromObject(predsObj);
		// Create and add each train to list
		for (Object p : temp) {
			preds.add(new Prediction(p));
		}
		this.predictions = preds;
	}
	
	/** 
	 * Accessor Methods
	 * */
	public String getTrainID() {
		return trainID;
	}
	
	public String getTrainDestination () {
		return destination;
	}
	
	public Position getTrainPosition() {
		return position;
	}
	
	public List<Prediction> getTrainPredictions() {
		return predictions;
	}
	public boolean checkName(String predName, String stopName){
		if(stopName.equals(stopName.toUpperCase())){
			return predName.toUpperCase().equals(stopName);
		}
		else{
			return predName.equals(stopName);
		}
	}
	public Prediction getPredictionByName(String stopName){
		Prediction prediction = null;
		for(Prediction p : predictions){
			if(checkName(p.getName(),stopName)){
				prediction =  p;
			}
		}
		return prediction;
	}
	public boolean containsStop(String stopName){
		boolean contains = false;
		for(Prediction p : predictions){
			contains = contains || checkName(p.getName(),stopName);
		}
		return contains;
	}
}