// Position class used to contain train position information
//
public class Position {

	private int pos_time;
	private float pos_lat;
	private float pos_long;
	private int pos_heading;
	
	public Position(int time, float current_lat, float current_long, int heading) {
		pos_time = time;
		pos_lat = current_lat;
		pos_long = current_long;
		pos_heading = heading;
	}
	
	//  Accessor Functions	
	public int getTime() {
		return pos_time;
	}
	
	public float getLat() {
		return pos_lat;
	}
	
	public float getLong() {
		return pos_long;
	}
	
	public int getHeading() {
		return pos_heading;
	}
		
}
