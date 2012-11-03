Team: The Boys

Chris McConnell     mcconnell.c@husky.neu.edu
Adam Gressen        gressen.a@husky.neu.edu
Nikita Filatov      filatov.n@husky.neu.edu
Edward Nunez        nunez.ed@husky.neu.edu


Compiling/running program:
You can compile and run our program by cd'ing into the MBTA-Trip-Planner directory and
running the following command:
java -cp ./bin:./*:./MBTA_test_data/2012_10_19/* TripPlanner

Getting current location of all trains:
The program will automatically generate a list of trains in the main screen of the application.
This list is simply a representation of our data objects right now, and will be prettied up 
for the final version. The predictions are being stored, but are being left out of the table
because there are too many and it looks nicer without that much information in the table.