import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 
 */

/**
 * @author CM
 * 
 * Graph class for route planning
 */
public class TrainGraph {
	
	private Map<Integer, LinkedList<Integer>> adjacent;
	
	public TrainGraph(LinkedList<Stop> stops) {
		// Constructs the graph we will use for route planning. 
		//  Maps the stop ID to the IDs of those next to it.
		this.adjacent = new HashMap<Integer, LinkedList<Integer>>();
		int size = stops.size();
		for(int i = 0; i< size; i++) {
			Stop stop = stops.get(i);
			this.adjacent.put(stop.stopID, stop.NextTo);
		}
	}
	
	

}

