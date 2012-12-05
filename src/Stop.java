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
	public LinkedList<Integer> NextTo;
	
	public Stop() {}
	
	 //General constructor for testing purposes
    //EN
    public Stop(String Line, String StationName, int stopID, String stop_name, boolean StartOfLine, boolean EndOfLine, LinkedList<Integer> NextTo) {
        this.Line = Line;
        this.StationName = StationName;
        this.stopID = stopID;
        this.stop_name = stop_name;
        this.StartOfLine = StartOfLine;
        this.EndOfLine = EndOfLine;
        this.NextTo = NextTo;
    }

	// Prints some fields to the console
	// AG
	public void printStop() {
		System.out.println("Line: "+Line+", Station: "+StationName+"," +
				" Stop ID: "+stopID+", NextTo: "+NextTo.toString());
	}
}
