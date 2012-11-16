// Class to represent stops on the T.
// CM and AG
import java.util.LinkedList;

public class Stop {
	public String Line;
	public String StationName;
	public int stopID;
	public String stop_name;
	public boolean StartOfLine;
	public boolean EndOfLine;
	public String Direction;
	public int PlatformOrder;
	public LinkedList<Integer> connectsTo;
	/*
	public Stop(String lineName, String stationName, String stopName, boolean startOfLine, boolean endOfLine, String direction, int stopOrder) {
		this.Line = lineName;
		this.StationName = stationName;
		this.StartOfLine = startOfLine;
		this.EndOfLine = endOfLine;
		this.Direction = direction;
		this.PlatformOrder = stopOrder;
	}*/
	
	public Stop() {}

	public void printStop() {
		System.out.println("Line: "+Line+", Station: "+StationName+", Direction: "+Direction);
	}
}
