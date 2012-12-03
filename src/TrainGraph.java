import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;
import java.util.*;

/**
 * 
 * Graph class for route planning
 * @author CM and AG
 * 
 */
public class TrainGraph {

	// Adjacent nodes
	private HashMap<Integer, LinkedList<Integer>> adjacent;
	public HashMap<Integer, LinkedList<Integer>> getAdjacent() {
		return adjacent;
	}

	public TrainGraph(LinkedList<Stop> stops) {
		// Constructs the graph we will use for route planning. 
		//  Maps the stop ID to the IDs of those next to it.
		this.adjacent = new HashMap<Integer, LinkedList<Integer>>();
		for(int i = 0; i < stops.size(); i++) {
			Stop stop = stops.get(i);
			this.adjacent.put(stop.stopID, stop.NextTo);
		}
	}

	// Search from start to find goal and push visited nodes to results
	// AG
	public boolean depthFirstSearch(int start, int goal, Stack<Integer> results) {
		// Prevent infinite loops
		if (results.contains(start))
			return false;

		// Add start to results
		results.push(start);

		// The goal has been reached
		if (start == goal)
			return true;

		for (int v : adjacent.get(start)) {
			if (depthFirstSearch(v, goal, results))
				return true;
		}

		// No path was found
		results.pop();
		return false; 
	}

	// Depth First Search
	// AG
	public Stack<Integer> multiSearch(LinkedList<Integer> goals) {
		Stack<Integer> results = new Stack<Integer>();
		
		for (int g = 0; g < goals.size()-1; g++) {
			Stack<Integer> tempResults = new Stack<Integer>();
			
			depthFirstSearch(goals.get(g),goals.get(g+1),tempResults);
			
			results.addAll(tempResults);
		}

		// No path was found
		return results; 
	}
	
	// Unordered list search
	//  CM
	public  Stack<Integer> unorderedSearch(LinkedList<Integer> goals) {
		//List to store possible routes
		LinkedList<Integer> remainingGoals = goals;
		Collections.sort(remainingGoals);
		return multiSearch(remainingGoals);
		
		//LinkedList<Stack<Integer>> possibilities = new LinkedList<Stack<Integer>>();
		
		/*
		// Calculate the possible routes
		for (int p = 0; p < remainingGoals.size(); p++) {
			LinkedList<Integer> stopPair = new LinkedList<Integer>();
			stopPair.add(remainingGoals.get(p));
			stopPair.add(remainingGoals.get(p+1));
			Stack<Integer> tempResults = multiSearch(stopPair);
			possibilities.add(tempResults);
			remainingGoals.remove(p);
		}*/
			
		}
	
	
}