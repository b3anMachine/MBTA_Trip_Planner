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
	
	//Returns a LinkedList<Integer> that is a path from the first transfer found, to the destination
	//NF
	public static LinkedList<Integer> findTransfer(LinkedList<Integer> pathList){
		if(pathList.isEmpty()){
			return pathList;
		}
		
		String originLine = TripPlanner.getStopByID(pathList.get(0)).Line;
		for(int id = 0; id < pathList.size(); id++) {
			String curLine = TripPlanner.getStopByID(pathList.get(id)).Line;
			if(!curLine.equals(originLine)){
				LinkedList<Integer> newList = new LinkedList<Integer>();
				
				newList.addAll(pathList.subList(id, pathList.size()-1));
				
				return newList;
			}
		}
		pathList.remove(0);
		return pathList;
	}
	
	// Unordered Search using permutations
	//  CM
	public Stack<Integer> unorderedPermSearch(LinkedList<Integer> goals) {
		LinkedList<LinkedList<Integer>> permutations = new LinkedList<LinkedList<Integer>>();
		LinkedList<Stack<Integer>> routes = new LinkedList<Stack<Integer>>();
		
		// generate all possible permutations of goals and add them to permutations
		
		permutations = getPermutations(goals);
		
		// Uses multisearch to calculate a route for each permutation
		for (int p = 0; p < permutations.size(); p++) {
			Stack<Integer> route = multiSearch(permutations.get(p));
			routes.add(route);
		}
		
		// get the smallest route
		//System.out.println(getSmallest(routes));
		return getSmallest(routes);
	}
	
	//  get all the permutations
	//  DO NOT USE WITH MORE THAN 7 or 8 ENTRIES. WILL TAKE FOREVER.
	//  CM
	public LinkedList<LinkedList<Integer>> getPermutations (LinkedList<Integer> goals) {
		
		if(goals.size()==1){
            LinkedList<Integer> perm = new LinkedList<Integer>();
            perm.add(goals.get(0));
            LinkedList<LinkedList<Integer>> listOfList = new LinkedList<LinkedList<Integer>>();
            listOfList.add(perm);
            return listOfList;
        }
		
		LinkedList<LinkedList<Integer>> listOfLists = new LinkedList<LinkedList<Integer>>();
		LinkedList<Integer> remaining = new LinkedList<Integer>(goals);
		
		for (int g = 0; g < goals.size(); g++) {
			LinkedList<Integer> perm = new LinkedList<Integer>();
			perm.add(goals.get(g));
			
			LinkedList<Integer> coppied = new LinkedList<Integer>();
			coppied.addAll(remaining);
			coppied.remove(g);
			
			LinkedList<LinkedList<Integer>> permute = getPermutations(coppied);
			
			Iterator<LinkedList<Integer>> iterator = permute.iterator();
            while (iterator.hasNext()) {
                LinkedList<Integer> list = iterator.next();
                list.add(goals.get(g));
                listOfLists.add(list);
            }
		}
		//System.out.println("Size = "+ listOfLists.size() + ", ListOfLists: " + listOfLists);
		return listOfLists;
	}
	
	
	// Unordered list search
	// Search that sorts the IDs, then finds the route. Not using.
	//  CM
	/*
	public  Stack<Integer> unorderedSearch(LinkedList<Integer> goals) {
		//List to store possible routes
		LinkedList<Integer> remainingGoals = goals;
		Collections.sort(remainingGoals);
		LinkedList<Stack<Integer>> possibilities = new LinkedList<Stack<Integer>>();
		
		for (int g = 0; g < remainingGoals.size(); g++) {
		remainingGoals.addFirst(remainingGoals.get(g));
		remainingGoals.remove(g+1);
		possibilities.add(multiSearch(remainingGoals));
		}
		
		return getSmallest(possibilities);
				
		}*/
	
	// Get the smallest stack of stops for the route
	//  CM
	public Stack<Integer> getSmallest (LinkedList<Stack<Integer>> routes) {
		Stack<Integer> smallest = routes.getFirst();
		for (int r = 0; r < routes.size(); r++) {
			Stack<Integer> route = routes.get(r);
			if (route.size() < smallest.size()) {
				smallest = route;
			}
		}
		
		return smallest;
	}
}