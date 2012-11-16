import java.awt.*;
import javax.swing.*; 
import java.io.File;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Date;
import java.util.LinkedList;
import java.math.*;
import javax.imageio.ImageIO;
import javax.swing.event.*;
import javax.swing.table.*;

//Create a simple GUI window
public class Views implements MouseListener, TableModelListener, MouseMotionListener {
	public static final String IMAGE_PATH = "mbta.bmp";
	public static JLabel imageLabel;
	public static int scaleX = 800;
	public static int scaleY = 1000;
	public static final int SCALE_TYPE = 3;
	public static LinkedList<TrainLine> trainLines = new LinkedList<TrainLine>();
	private static LinkedList<Stop> stops;
	public static JTable table;
	public static DefaultTableModel tableModel;
	public static String[] trainColumns = {"ID", "Line", "Location", "Destination", "Realtime"};
	public static String[] stopColumns = {"Line","Name","Direction","Platform Order"};
	public static boolean showTrains = false;
	public enum viewState {
		VIEWING_TRAINS,
		VIEWING_STOPS,
		VIEWING_ROUTE
	}

	int draggedAtX, draggedAtY;

	// Views constructor
	public Views(LinkedList<TrainLine> lines, LinkedList<Stop> stops) {
		this.stops = stops;
		setLines(lines);
		createWindow(trainLines);
	}

	public void setLines(LinkedList<TrainLine> lines) {
		// Set the trainLines to the given list of lines
		trainLines = lines;
		// Update the table data
		updateTableData();
	}

	//creates the GUI
	//NF
	public void createWindow(LinkedList<TrainLine> lines) {
		//Create and set up the window.
		JFrame frame = new JFrame("MBTA Trip Planner"); 
		frame.setBackground(new Color(100,100,100));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 

		//define all labels
		imageLabel = new JLabel();
		imageLabel.addMouseListener(this);
		imageLabel.addMouseMotionListener(this);
		JLabel depTime = new JLabel("Departure Time");
		JLabel arrTime = new JLabel("Arrival Time");

		SpinnerListModel hours1 = new SpinnerListModel(getTime(1,13));
		SpinnerListModel mins1 = new SpinnerListModel(getTime(1,60));
		SpinnerListModel hours2 = new SpinnerListModel(getTime(1,13));
		SpinnerListModel mins2 = new SpinnerListModel(getTime(1,60));
		JSpinner pickArrHour = new JSpinner(hours1);
		JSpinner pickDepHour = new JSpinner(hours2);
		JSpinner pickArrMin = new JSpinner(mins1);
		JSpinner pickDepMin = new JSpinner(mins2);
		

		/**
		 * The Buttons
		 * NF and AG
		 * **/
		// List Trains button
		JButton listTrains = new JButton("List Trains");
		listTrains.setBackground(new Color(230,230,230));
		listTrains.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showTrains = true;
				updateTableData();
			}
		});
		
		// List Stops button
		JButton listStops = new JButton("List Stops");
		listStops.setBackground(new Color(230,230,230));
		listStops.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showTrains = false;
				updateTableData();
			}
		});
		
		// Test System button
		JButton testSystem = new JButton("Test System");
		testSystem.setBackground(new Color(230,230,230));
		
		// Add Stop button
		JButton addStop = new JButton("Add Stop");
		addStop.setBackground(new Color(230,230,230));
		
		// Calculate Route button
		JButton calcRoute = new JButton("Calculate Route");
		calcRoute.setBackground(new Color(230,230,230));

		

		String[] stops = {"stop a", "stop b", "stop c", "stop d", "stop e", "stop f"};

		JComboBox stopInfo = new JComboBox();
		JComboBox selectStop = new JComboBox();
		for(int i=0;i<stops.length;i++){
			stopInfo.addItem(stops[i]);
			selectStop.addItem(stops[i]);
		}

		JRadioButton earliestDep = new JRadioButton("Earliest Departures");
		earliestDep.setBackground(new Color(230,230,230));
		//birdButton.setSelected(true);

		JRadioButton fewestTrans = new JRadioButton("Fewest Transfers");
		fewestTrans.setBackground(new Color(220,220,220));
		//catButton.setActionCommand(catString);

		JRadioButton earliestArr = new JRadioButton("Earliest Arrival");
		earliestArr.setBackground(new Color(210,210,210));
		//dogButton.setActionCommand(dogString);

		ButtonGroup group = new ButtonGroup();
		group.add(earliestDep);
		group.add(fewestTrans);
		group.add(earliestArr);

		JRadioButton am1 = new JRadioButton("PM");
		fewestTrans.setBackground(new Color(220,220,220));

		JRadioButton pm1 = new JRadioButton("AM");
		earliestArr.setBackground(new Color(210,210,210));

		JRadioButton am2 = new JRadioButton("PM");
		fewestTrans.setBackground(new Color(220,220,220));

		JRadioButton pm2 = new JRadioButton("AM");
		earliestArr.setBackground(new Color(210,210,210));

		ButtonGroup ampm1 = new ButtonGroup();
		ampm1.add(am1);
		ampm1.add(pm1);

		ButtonGroup ampm2 = new ButtonGroup();
		ampm2.add(am2);
		ampm2.add(pm2);


		//define all checkboxes
		JCheckBox orderedList = new JCheckBox("Ordered List");
		orderedList.setBackground(new Color(230,230,230));
		JCheckBox drawTrains = new JCheckBox("Show Trains on Map");
		drawTrains.setBackground(new Color(230,230,230));

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		//Display the window
		//divide the window into 3 columns
		frame.setLayout(new GridLayout(1,3,5,0));
		//set window as visible
		frame.setVisible(true); 

		//create left, middle, right internal jframes
		JInternalFrame left = newFrame();
		JInternalFrame right = newFrame();
		JInternalFrame middle = newFrame();

		//set internal jframe layouts
		left.setLayout(gridbag);
		middle.setLayout(gridbag);
		//right.setLayout(new GridLayout(3,1,5,5));
		right.setLayout(gridbag);

		//////////////////////////////////////
		//set up layout for rightmost jframe
		JInternalFrame right1 = newFrame(2,new Color(230,230,230));
		right1.setMinimumSize(new Dimension(300,40));
		right1.setLayout(new FlowLayout());
		c.gridwidth = GridBagConstraints.REMAINDER; //end row
		c.weightx = 1.0;
		c.weighty = 0.0;
		gridbag.setConstraints(right1, c);
		right.add(right1);

		JInternalFrame right2 = newFrame(0, new Color(200,200,200));
		right2.setMinimumSize(new Dimension(300,40));
		right2.setLayout(new FlowLayout());
		c.gridwidth = GridBagConstraints.REMAINDER; //end row
		c.weightx = 0;
		c.weighty = 0;
		gridbag.setConstraints(right2, c);
		right.add(right2);

		JInternalFrame right3 = newFrame();
		right3.setMinimumSize(new Dimension(300,40));
		//right3.setLayout(new FlowLayout());
		c.weighty = 1;
		c.gridwidth = GridBagConstraints.REMAINDER; //end row
		gridbag.setConstraints(right3, c);
		right.add(right3);

		JInternalFrame right4 = newFrame(0,new Color(200,200,200));
		right4.setMinimumSize(new Dimension(300,40));
		right4.setLayout(new FlowLayout());
		c.weighty = 0;
		c.gridwidth = GridBagConstraints.REMAINDER; //end row
		gridbag.setConstraints(right4, c);
		right.add(right4);

		JInternalFrame right5 = newFrame(0,new Color(200,200,200));
		right5.setMinimumSize(new Dimension(300,40));
		right5.setLayout(new GridLayout(3,1,5,5));
		c.weighty = 1;
		c.gridwidth = GridBagConstraints.REMAINDER; //end row
		gridbag.setConstraints(right5, c);
		right.add(right5);

		JInternalFrame right6 = newFrame();
		//right6.setMinimumSize(new Dimension(300,40));
		right6.setLayout(new GridLayout(1,2,5,5));
		c.weighty = 1;
		c.gridwidth = GridBagConstraints.REMAINDER; //end row
		gridbag.setConstraints(right6, c);
		right.add(right6);

		JInternalFrame lastLeft = newFrame(0,new Color(220,220,220));
		lastLeft.setLayout(new GridLayout(3,1,5,5));
		JInternalFrame lastRight = newFrame(0,new Color(220,220,220));
		lastRight.setLayout(new GridLayout(3,1,5,5));
		right6.add(lastLeft);
		right6.add(lastRight);
		JInternalFrame depFrame = newFrame(0,new Color(220,220,220));
		depFrame.setLayout(new GridLayout(1,2,5,5));
		JInternalFrame arrFrame = newFrame(0,new Color(220,220,220));
		arrFrame.setLayout(new GridLayout(1,2,5,5));
		JInternalFrame ampmFrame1 = newFrame(0,new Color(220,220,220));
		ampmFrame1.setLayout(new GridLayout(1,2,5,5));
		JInternalFrame ampmFrame2 = newFrame(0,new Color(220,220,220));
		ampmFrame2.setLayout(new GridLayout(1,2,5,5));
		///////////////////////////////////////

		//////////////////////////////////////
		//set up layout for LEFT jframe
		JInternalFrame topLeft = newFrame(2,new Color(230,230,230));
		topLeft.setLayout(new FlowLayout());
		topLeft.setMinimumSize(new Dimension(300,40));
		c.gridwidth = GridBagConstraints.REMAINDER; //end row
		//c2.gridheight = GridBagConstraints.RELATIVE;
		c.weightx = 1;
		c.weighty = 0;
		gridbag.setConstraints(topLeft, c);
		left.add(topLeft);

		JInternalFrame middleLeft = newFrame();
		//middleLeft.setBackground(new Color(255,255,255));
		middleLeft.setLayout(new FlowLayout());
		//middleLeft.setMinimumSize(new Dimension(100,100));
		c.gridwidth = GridBagConstraints.REMAINDER; //end row
		//c2.gridheight = GridBagConstraints.RELATIVE;
		c.weighty = 1;
		gridbag.setConstraints(middleLeft, c);
		left.add(middleLeft);

		JInternalFrame bottomLeft = newFrame(2,new Color(230,230,230));
		bottomLeft.setLayout(new FlowLayout());
		bottomLeft.setMinimumSize(new Dimension(300,40));
		c.gridwidth = GridBagConstraints.REMAINDER; //end row
		//c2.gridheight = GridBagConstraints.RELATIVE;
		c.weighty = 0;
		gridbag.setConstraints(bottomLeft, c);
		left.add(bottomLeft);
		///////////////////////////////////////

		//////////////////////////////////////
		//set up layout for LEFT jframe
		JInternalFrame topMiddle = newFrame(2,new Color(230,230,230));
		//topMiddle.setLayout(new GridLayout(1,3,5,5));
		topMiddle.setLayout(new FlowLayout());
		topMiddle.setMinimumSize(new Dimension(300,40));
		c.gridwidth = GridBagConstraints.REMAINDER; //end row
		//c2.gridheight = GridBagConstraints.RELATIVE;
		c.weightx = 1;
		c.weighty = 0;
		gridbag.setConstraints(topMiddle, c);
		middle.add(topMiddle);
		topMiddle.add(listTrains);
		topMiddle.add(listStops);
		topMiddle.add(stopInfo);

		JInternalFrame bottomMiddle = newFrame();
		//middleLeft.setBackground(new Color(255,255,255));
		//bottomMiddle.setLayout(new FlowLayout());
		bottomMiddle.setMinimumSize(new Dimension(300,40));
		//middleLeft.setMinimumSize(new Dimension(100,100));
		c.gridwidth = GridBagConstraints.REMAINDER; //end row
		//c2.gridheight = GridBagConstraints.RELATIVE;
		c.weighty = 1;
		gridbag.setConstraints(bottomMiddle, c);
		middle.add(bottomMiddle);
		///////////////////////////////////////

		//add to left jframe
		topLeft.add(testSystem);
		createMap(scaleX, scaleY, SCALE_TYPE);
		middleLeft.add(imageLabel);
		bottomLeft.add(drawTrains);

		//add to middle jframe
		//middle.add(listTrains);
		//middle.add(listStops);		 
		createTable(bottomMiddle, lines);
		stopsTable(right3);
		//topRight.add(textLabel, BorderLayout.NORTH); 		 
		right1.add(orderedList);

		right2.add(addStop);
		right2.add(selectStop);
		right4.add(calcRoute);
		right5.add(earliestDep);
		right5.add(fewestTrans);
		right5.add(earliestArr);

		lastLeft.add(depTime);
		lastLeft.add(depFrame);
		depFrame.add(pickDepHour);
		depFrame.add(pickDepMin);
		lastLeft.add(ampmFrame1);
		ampmFrame1.add(am1);
		ampmFrame1.add(pm1);
		lastRight.add(arrTime);
		lastRight.add(arrFrame);
		arrFrame.add(pickArrHour);
		arrFrame.add(pickArrMin);
		lastRight.add(ampmFrame2);
		ampmFrame2.add(am2);
		ampmFrame2.add(pm2);

		//add internal jframes in order to fill the grid layout
		frame.add(left);
		frame.add(middle);
		frame.add(right);

		//pack the frames neatly		
		frame.pack(); 
		//topRight.setSize(100,100);
		//middle.pack();
		frame.setSize(1200,600);
	}
	//loads map
	public static void createMap(int width, int height, int type) {
		createImage(imageLabel, IMAGE_PATH, width, height, type);
	}

	//loads an image from url to a jlabel with certain dimensions and a scaling type
	//NF
	public static void createImage(JLabel label, String url, int width, int height, int type){
		try 
		{
			// Read from a file
			File FileToRead = new File(url);
			//Recognize file as image
			Image Picture = ImageIO.read(FileToRead);
			Image pic = Picture.getScaledInstance(width, height, type);
			ImageIcon icon = new ImageIcon(pic);
			//Show the image inside the label
			label.setIcon(icon);
		} 
		catch (Exception e) 
		{
			//Display a message if something goes wrong
			JOptionPane.showMessageDialog( null, e.toString() );
		}
	}

	//returns a new internal jframe without a toolbar
	// NF
	private static JInternalFrame newFrame(int borderWidth, Color c){
		JInternalFrame frame = new JInternalFrame("",false,false,false,false);
		frame.setBorder(BorderFactory.createLineBorder(new Color(150,150,150), borderWidth));
		frame.setBackground(c);
		javax.swing.plaf.InternalFrameUI ifu= frame.getUI(); 
		((javax.swing.plaf.basic.BasicInternalFrameUI)ifu).setNorthPane(null);
		frame.setVisible(true);
		frame.moveToFront();
		return frame;
	}
	private static JInternalFrame newFrame(){
		return newFrame(2,new Color(230,230,230));
	}
	//takes an internal jframe and create a table in it
	private void createTable(JInternalFrame container, LinkedList<TrainLine> lines){
		tableModel = new DefaultTableModel();
		tableModel.addTableModelListener(this);
		table = new JTable(tableModel);
		table.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
		table.setShowVerticalLines(true);
		table.setSize(700,700);
		updateTableData();
		JScrollPane scrollPane = new JScrollPane(table); 
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		//table.setFillsViewportHeight(true);

		container.add(table.getTableHeader(), BorderLayout.PAGE_START);
		container.add(scrollPane);
		//container.add(table);
	}


	// Returns updated data for the table based on lines
	//  CM and AG
	public static void updateTableData() {
		int counter = 0;
		Object[][] data;

		// If the trains data should be shown
		if (showTrains) {
			// Get number of all trains
			for (TrainLine l : trainLines) {
				for (Train t : l.getTrains()) {
					counter++;
				}
			}
			// initialize length of data array
			data = new Object[counter][];
			// reset counter
			counter = 0;
			// iterate through lines
			for (TrainLine line : trainLines) {
				String lineName = line.getLine();
				// Get list of trains for each line
				LinkedList<Train> trains = line.getTrains();
				for (int t = 0; t < trains.size(); t++) {
					Position pos = trains.get(t).getTrainPosition();
					Double lat = (double)((int)(pos.getLat()*100))/100;
					Double lon = (double)((int)(pos.getLong()*100))/100;
					String posString;
					if (lat.equals(0.0) && lon.equals(0.0))
						posString = "none";
					else
						posString = "("+lat+","+lon+")";
					Object[] row = { trains.get(t).getTrainID(), lineName, 
							posString, trains.get(t).getTrainDestination(), new Date().getTime() };
					data[counter] = row;
					counter++;
				}
			}
		}
		// If the stops should be shown
		else {
			data = new Object[stops.size()][];
			counter = 0;
			for (Stop s : stops) {
				Object[] row = { s.Line, s.StationName, s.Direction, s.PlatformOrder };
				data[counter] = row;
				counter++;
			}
		}
		if (tableModel != null) {
			tableModel.setDataVector(data, (showTrains ? trainColumns : stopColumns));
		}
	}

	//makes the stops table (to be refactored)
	//NF
	private static void stopsTable(JInternalFrame container){
		String[] columnNames = {"Stops"};
		String[][] data = {{"State Street"}, {"Gov't Center"}, {"Park Street"}, {"Harvard Ave"},
				{"State Street"}, {"Gov't Center"}, {"Park Street"}, {"Harvard Ave"},
				{"State Street"}, {"Gov't Center"}, {"Park Street"}, {"Harvard Ave"},
				{"State Street"}, {"Gov't Center"}, {"Park Street"}, {"Harvard Ave"}};
		JTable table = new JTable(data, columnNames);	
		table.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
		table.setShowVerticalLines(true);
		table.setSize(700,700);
		JScrollPane scrollPane = new JScrollPane(table); 
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		//table.setFillsViewportHeight(true);

		container.add(table.getTableHeader(), BorderLayout.PAGE_START);
		container.add(scrollPane);
		//container.add(table);
	}

	//scales the map on mouse click; scales in for left click, scales out for right click
	//NF + AG
	public void mouseClicked(MouseEvent e) {
		int button = e.getButton();		
		if (button == MouseEvent.BUTTON1) {
			System.out.println("button 1");
			scaleX *= 1.5;
			scaleY *= 1.5;
			createMap(scaleX, scaleY, SCALE_TYPE);
		}
		else if (button == MouseEvent.BUTTON3) {
			System.out.println("button 3");
			scaleX /= 1.5;
			scaleY /= 1.5;
			createMap(scaleX, scaleY, SCALE_TYPE);
		}

	}
	//sets the location of the map using the mouse coordinates
	//NF
	public void mouseDragged(MouseEvent e) {		
		imageLabel.setLocation(e.getX() - draggedAtX + imageLabel.getX(),
				e.getY() - draggedAtY + imageLabel.getY());		
	}

	public void mouseMoved(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	//obtains the current mouse coordinates upon mousePress
	//NF
	public void mousePressed(MouseEvent e) {

		if (e.getSource() == imageLabel) {
			draggedAtX = e.getX();
			draggedAtY = e.getY();
		} 

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {

		// TODO Auto-generated method stub

	}

	@Override
	public void tableChanged(TableModelEvent e) {
	}

	//returns an array of strings from min to max
	public String[] getTime(int min, int max){
		String[] time = new String[max];
		for(int i =0; i<time.length;i++){
			time[i] = String.valueOf(i);
		}
		return time;
	}
} 