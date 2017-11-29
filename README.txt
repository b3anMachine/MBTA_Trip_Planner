Team: The Boys

Chris McConnell     mcconnell.c@husky.neu.edu
Adam Gressen        gressen.a@husky.neu.edu
Nikita Filatov      filatov.n@husky.neu.edu
Edward Nunez        nunez.ed@husky.neu.edu


Compiling/running program:
You can compile and run our program by cd'ing into the MBTA-Trip-Planner directory and
running the following command:
javac -classpath .:* src/*.java
java -cp ./bin:./*:./MBTA_test_data/2012_10_19/* TripPlanner


Using the Application:

1. The user wants to know where she can go using the T. (Essential)
When the application starts, it will display a map on the left side of the window.
This map can be navigated by clicking and dragging on it.


2. The user wants to know the current location of all trains. (Essential)
The map that loads initially will automatically populate with live data, and give current positions
for all trains. The user can also click the "List Trains" button, which will load the table on the 
right with all current trains, and when they will arrive at the next stop on their route.


3. The user wants to know when the next trains get to stop A. (Essential)
The user can click on "List Stops" and scroll to the stop they want to view in the table.
The table will display the nest train that is projected to arrive at the stop.
Alrernatively, the user can hover the mouse over the desired stop. While hovering,
a popup will be displayed showing the next trains that will arrive at the stop, and
the closest train in each direction will be hilighted on the map.


4. The user wants to know her options for getting from stop A to stop B. (Essential)
To get a route between two stops, select stop A in the dropdown menu and click "Add Stop".
Repeat with stop B to add it to the list.
Then click "Compute Route", and the path between the two routes will be presented in the
"List Routes" table, and hilighted on the map.

Stops can be removed from the list by clicking on the stop and then clicking "Remove Stop".


5. The user wants to know her options for getting to an ordered list of stops. (Desirable)
To get the route for an ordered list of stops, add as many stops as desired using the dropdown
menu and the "Add Stop" button. When all stops are added, make sure that the "Ordered List"
checkbox is CHECKED, and click "Calculate Route". The path will be displayed in the "List Routes"
table and hilighted on the graph.


6. The user wants to know her options for getting to an unordered list of stops. (Optional)
To get the route for an ordered list of stops, add as many stops as desired using the dropdown
menu and the "Add Stop" button. When all stops are added, make sure that the "Ordered List"
checkbox is NOT CHECKED, and click "Calculate Route". The path will be displayed in the "List Routes"
table and hilighted on the graph.

**Note: The unordered algorithm works by computing all possible orders and then computing a path.
This takes factorial time, so it will take longer for longer lists of stops. Try to limit it to
7 or 8 at the most when possible**


7. For any trip on the T, the user wants the option to specify departure and/or arrival times. (Desirable)
When selecting a list of stops to visit, the user can enter desired departure and arrival times. To 
have these times used in the calculation of routes, make sure that the appropriate box is CHECKED.

**Note: These will not significantly impact the route that will be calculated, but will be used to filter the
available trains that are listed for the user to take.**


8. For any trip on the T, the user wants to know the fastest route, the earliest departure, the earliest arrival, and fewest transfers. (Desirable)
To filter a route by any of these options, make sure that the appropriate radio button is selected before calculating a route.
The system will default to the finding the smallest possible route if none are selected.


9. The client wants to test the system with old data. (Essential)
To test the system with old data, make sure the data is loaded into the MTBA_test_data folder. The program will automatically start
using live data, but may be switched to test data at any time but clicking the "Use Test Data" button in the upper right-hand
corner of the screen. When using test data, it may be switched back to live data by clicking the "Use Live Data" button that
replaces it.


Additional notes:
Trains on the map may be hovered over with the mouse to display a list of predicted arrival times for the next stops
Hovering over a train will also highlight its route on the map.





