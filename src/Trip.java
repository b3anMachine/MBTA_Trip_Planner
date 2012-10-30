// Class to hold train information
//  Train objects to be generated from JSON data.

public class Trip {
 
	private String train_id;
 	private String train_line;
 	private String train_destination;
	private Position train_position;
	private Prediction[] train_predictions;
	
	public Trip(String ID, String line, String destination, Position position, Prediction[] predictions) {
		train_id = ID;
		train_line = line;
		train_destination = destination;
		train_position = position;
		train_predictions = predictions;
	}
	
	//  Accessor Functions
	public String getTrainID() {
		return train_id;
	}
	
	public String getTrainLine() {
		return train_line;
	}
	
	public String getTrainDestination () {
		return train_destination;
	}
	
	public Position getTrainPosition() {
		return train_position;
	}
	
	public Prediction[] getTrainPredictions() {
		return train_predictions;
	}
 
 	
 
 
 
}
