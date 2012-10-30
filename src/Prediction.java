// Class to contain prediction information
// Says when a train is predicted to arrive at a stop in seconds.
public class Prediction {
	
	private int stopID;
	private String stop;
	private int seconds;
	
	public Prediction(int ID, String name, int seconds) {
		stopID = ID;
		stop = name;
		seconds = seconds;
	}
	
	//  Accessor Functions	
	public int getID() {
		return stopID;
	}
	
	public String getName() {
		return stop;
	}
	
	public int getTime() {
		return seconds;
	}

}
