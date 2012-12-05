//Class for testing main search algorithm
//EN

import java.util.LinkedList;
import java.util.Stack;
import java.util.HashMap;

public class Test {
    
 //Create and fill adjacency lists for Stop instances
 LinkedList<Integer> list1 = new LinkedList<Integer>();
 LinkedList<Integer> list2 = new LinkedList<Integer>();
 LinkedList<Integer> list3 = new LinkedList<Integer>();
 
 Boolean add1 = list1.add(1);
 Boolean add2 = list1.add(2);
 Boolean add3 = list1.add(3);
 
 Boolean add4 = list2.add(4);
 Boolean add5 = list2.add(2);
 Boolean add6 = list2.add(6);
 
 Boolean add7 = list3.add(7);
 Boolean add8 = list3.add(8);
 Boolean add9 = list3.add(9);
 
//Instantiate Stop examples
Stop stop1 = new Stop("Foo", "Bah", 1, "Bar", false, false, list1);     
Stop stop2 = new Stop("Foo", "Bah", 2, "Bar", false, false, list1);    
Stop stop3 = new Stop("Foo", "Bah", 3, "Bar", true, true, list1);    
Stop stop4 = new Stop("Foo", "Bah", 4, "Bar", false, true, list2);    
Stop stop5 = new Stop("Foo", "Bah", 5, "Bar", true, false, list2);    
Stop stop6 = new Stop("Foo", "Bah", 6, "Bar", false, false, list2);    
Stop stop7 = new Stop("Foo", "Bah", 7, "Bar", false, false, list3);    
Stop stop8 = new Stop("Foo", "Bah", 8, "Bar", false, false, list3);    
Stop stop9 = new Stop("Foo", "Bah", 9, "Bar", false, false, list3);    

//Tie stops together in a list for trainGraph
LinkedList<Stop> stopList = new LinkedList<Stop>();

Boolean add10 = stopList.add(stop1);
Boolean add11 = stopList.add(stop2);
Boolean add12 = stopList.add(stop3);
Boolean add13 = stopList.add(stop4);
Boolean add14 = stopList.add(stop5);
Boolean add15 = stopList.add(stop6);
Boolean add16 = stopList.add(stop7);
Boolean add17 = stopList.add(stop8);
Boolean add18 = stopList.add(stop9);

//Instantiate test stacks for trainGraph
Stack<Integer> stack1 = new Stack<Integer>();
Object push1 = stack1.push(1);

Stack<Integer> stack2 = new Stack<Integer>();
Object push2 = stack2.push(1);
Object push3 = stack2.push(2);
Object push4 = stack2.push(3);

Stack<Integer> stack3 = new Stack<Integer>();
Object push5 = stack3.push(11);
Object push6 = stack3.push(12);
Object push7 = stack3.push(13);

//Instantiate the test trianGraph
TrainGraph graph = new TrainGraph(stopList);

//Test all paths of the trainGraph search algorithm 
Boolean check1 = graph.depthFirstSearch(1, 2, stack1);
Boolean test1 = check1 == false;

Boolean check2 = graph.depthFirstSearch(2, 2, stack1);
Boolean test2 = check2 == true;

Boolean check3 = graph.depthFirstSearch(7, 8, stack2);
Boolean test3 = check3 == true;

Boolean check4 = graph.depthFirstSearch(1, 13, stack3);
Boolean test4 = check4 == false;
}