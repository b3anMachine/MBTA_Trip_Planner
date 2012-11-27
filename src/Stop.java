import java.util.LinkedList;

// Class to represent stops on the T.
// CM and AG
public class Stop {
	public String Line;
	public String StationName;
	public int stopID;
	public String stop_name;
	public boolean StartOfLine;
	public boolean EndOfLine;
	public String Direction;
	public LinkedList<Integer> NextTo;
	
	public Stop() {}

	// Prints some fields to the console
	// AG
	public void printStop() {
		System.out.println("Line: "+Line+", Station: "+StationName+"," +
				" Stop ID: "+stopID+", Direction: "+Direction);
	}
}
