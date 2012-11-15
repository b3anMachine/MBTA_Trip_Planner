// Class to represent stops on the T. CM
import java.util.LinkedList;

public class Stop {
	
	private String line;
	private String stationName;
	private int stopID;
	private String stopName;
	private boolean startOfLine;
	private boolean endOfLine;
	private String direction;
	private int stopOrder;
	private LinkedList<Integer> connectsTo;
	
	public Stop(String lineName, String stationName, int stopID, String stopName, boolean startOfLine, boolean endOfLine, String direction, int stopOrder, LinkedList<Integer> connectsTo) {
		this.line = lineName;
		this.stationName = stationName;
		this.stopID = stopID;
		this.startOfLine = startOfLine;
		this.endOfLine = endOfLine;
		this.direction = direction;
		this.stopOrder = stopOrder;
		this.connectsTo = connectsTo;
	}

}
