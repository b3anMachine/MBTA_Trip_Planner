import java.util.*;

public class Dijkstra {
	private final HashMap<Integer, LinkedList<Integer>> nodes;
	  private Set<Integer> settledNodes;
	  private Set<Integer> unSettledNodes;
	  private Map<Integer, Integer> predecessors;
	  private Map<Integer, Integer> distance;

	  public Dijkstra(TrainGraph graph) {
	    // Create a copy of the array so that we can operate on this array
	    this.nodes = new HashMap<Integer, LinkedList<Integer>>(graph.getAdjacent());
	  }

	  public void execute(Integer source) {
	    settledNodes = new HashSet<Integer>();
	    unSettledNodes = new HashSet<Integer>();
	    distance = new HashMap<Integer, Integer>();
	    predecessors = new HashMap<Integer, Integer>();
	    distance.put(source, 0);
	    unSettledNodes.add(source);
	    while (unSettledNodes.size() > 0) {
	      Integer node = getMinimum(unSettledNodes);
	      settledNodes.add(node);
	      unSettledNodes.remove(node);
	      findMinimalDistances(node);
	    }
	  }

	  private void findMinimalDistances(Integer node) {
	    List<Integer> adjacentNodes = getNeighbors(node);
	    for (Integer target : adjacentNodes) {
	      if (getShortestDistance(target) > getShortestDistance(node) + 1) {
	        distance.put(target, getShortestDistance(node) + 1);
	        predecessors.put(target, node);
	        unSettledNodes.add(target);
	      }
	    }

	  }

	  private List<Integer> getNeighbors(Integer node) {
	    return nodes.get(node);
	  }

	  private Integer getMinimum(Set<Integer> Integeres) {
	    Integer minimum = null;
	    for (Integer i : Integeres) {
	      if (minimum == null) {
	        minimum = i;
	      } else {
	        if (getShortestDistance(i) < getShortestDistance(minimum)) {
	          minimum = i;
	        }
	      }
	    }
	    return minimum;
	  }


	  private int getShortestDistance(Integer destination) {
	    Integer d = distance.get(destination);
	    if (d == null) {
	      return Integer.MAX_VALUE;
	    } else {
	      return d;
	    }
	  }

	  /*
	   * This method returns the path from the source to the selected target and
	   * NULL if no path exists
	   */
	  public Stack<Integer> getPath(Integer target) {
	    Stack<Integer> path = new Stack<Integer>();
	    int step = target;
	    // Check if a path exists
	    if (predecessors.get(step) == null) {
	      return null;
	    }
	    path.push(step);
	    while (predecessors.get(step) != null) {
	      step = predecessors.get(step);
	      path.push(step);
	    }
	    // Put it into the correct order
	    Collections.reverse(path);
	    return path;
	  }

}

