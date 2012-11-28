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
	
	private Map<Stop, LinkedList<Integer>> adjacent;
	
	public TrainGraph(LinkedList<Stop> stops) {
		int size = stops.size();
		int counter = 0;
		while(counter <= size) {
			Stop stop = stops.get(counter);
			counter++;
			adjacent.put(stop, stop.NextTo);
		}
	}
	
	

}

