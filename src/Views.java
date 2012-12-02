import java.awt.*;
import javax.swing.*; 
import java.io.File;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.LinkedList;
import javax.imageio.ImageIO;
import javax.swing.event.*;
import javax.swing.table.*;

/**
 * Create the main GUI of our application
 * **/
public class Views implements MouseListener, TableModelListener, MouseMotionListener{

	/**
	 * Map declarations
	 * **/
	public static HashMap<String,MapStop> stopMap = new HashMap<String,MapStop>(70);
	public static final String IMAGE_PATH = "mbta.bmp";
	public static Image map;
	public static JLabel imageLabel;
	static JInternalFrame middleLeft;
	static double scaleX = 1.0;
	static double scaleY = 1.0;
	static AffineTransform trans;

	public static final int MAX_TIME = 1000;
	public static final int MIN_TIME = 60;

	public static LinkedList<TrainLine> trainLines = new LinkedList<TrainLine>();
	private static LinkedList<Stop> stops;
	private static LinkedList<String> selectedStops = new LinkedList<String>();

	/**
	 * Table declarations
	 * **/
	public static JTable table;
	public static DefaultTableModel tableModel;
	public static JTable stopsTable;
	public static DefaultTableModel stopsTableModel;
	// Table data
	static Object[][] data;
	// Stops table data
	static Object[][] stopsData;
	// Column names for the list of trains
	private static String[] trainColumns = {"ID", "Line", "Location", "Destination"};
	// Column names for the list of stops
	private static String[] stopColumns = {"Line", "Name", "Stop ID"};
	//
	private static String[] stopsTableColumns = {"Selected Stops"};
	private static boolean showTrains = true;
	private enum viewState {
		VIEWING_TRAINS,
		VIEWING_STOPS,
		VIEWING_ROUTE
	}
	
	JComboBox<String> stopInfo;
	JComboBox<String> selectStop;

	int draggedAtX, draggedAtY;

	/**
	 * Colors
	 * **/
	// Foreground color
	static Color FORE_COLOR = new Color(230,230,230);
	// Background color
	static Color BACK_COLOR = new Color(100,100,100);
	// Train colors
	static Color TRAIN_COLOR = Color.green;

	/**
	 * Sizes
	 * **/
	// Max window size
	Dimension MAX_SIZE = new Dimension(1920, 1080);
	// Min window size
	Dimension MIN_SIZE = new Dimension(1200, 800);

	/**
	 * Constructor for the Views class
	 * @param lines
	 * @param s
	 * **/
	public Views(LinkedList<TrainLine> lines, LinkedList<Stop> s) {
		stops = s;
		trainLines = lines;
		pushHash();
		createWindow();
		update();
	}

	/**
	 * Sets the lines
	 * @param lines
	 */
	public void setLines(LinkedList<TrainLine> lines) {
		//trainLines.clear();
		// Set the trainLines to the given list of lines
		trainLines = lines;//.addAll(lines);
		// Update table and map
		update();
	}

	/**
	 * Creates and sets up the window
	 * @author NF
	 */
	public void createWindow() {
		JFrame frame = new JFrame("MBTA Trip Planner");
		frame.setBackground(BACK_COLOR);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setMinimumSize(MIN_SIZE);
		frame.setMaximumSize(MAX_SIZE);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

		/**
		 * The Labels
		 * **/
		imageLabel = new JLabel();
		imageLabel.addMouseListener(this);
		imageLabel.addMouseMotionListener(this);
		JLabel depTime = new JLabel("Departure Time");
		JLabel arrTime = new JLabel("Arrival Time");

		JSpinner pickArr = new JSpinner(new SpinnerDateModel());
		JSpinner.DateEditor arrEditor = new JSpinner.DateEditor(pickArr, "HH:mm");
		pickArr.setEditor(arrEditor);
		JSpinner pickDep = new JSpinner(new SpinnerDateModel());
		JSpinner.DateEditor depEditor = new JSpinner.DateEditor(pickDep, "HH:mm");
		pickDep.setEditor(depEditor);

		/**
		 * The Buttons
		 * **/
		// List Trains button
		JButton listTrains = new JButton("List Trains");
		listTrains.setBackground(FORE_COLOR);
		listTrains.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showTrains = true;
				update();
			}
		});

		// List Stops button
		JButton listStops = new JButton("List Stops");
		listStops.setBackground(FORE_COLOR);
		listStops.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showTrains = false;
				update();
			}
		});

		// Test System button
		final JButton testSystem = new JButton("Use Test Data");
		testSystem.setBackground(FORE_COLOR);
		testSystem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TripPlanner.toggleLiveData();
				if (testSystem.getText().equals("Use Live Data"))
					testSystem.setText("Use Test Data");
				else
					testSystem.setText("Use Live Data");
			}
		});

		// Add Stop button
		JButton addStop = new JButton("Add Stop");
		addStop.setBackground(FORE_COLOR);
		addStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedStops.add((String) selectStop.getSelectedItem());
				updateStopsTable();
			}
		});

		// Calculate Route button
		JButton calcRoute = new JButton("Calculate Route");
		calcRoute.setBackground(FORE_COLOR);

		/**
		 * Combo boxes
		 * **/
		// Populate list of stops for GUI
		String[] stopList = new String[stops.size()];
		for (int s = 0; s < stops.size(); s++) {
			stopList[s] = stops.get(s).stop_name;
		}

		// Populate combo boxes
		stopInfo = new JComboBox<String>();
		selectStop = new JComboBox<String>();
		for(int i = 0; i < stopList.length; i++){
			stopInfo.addItem(stopList[i]);
			selectStop.addItem(stopList[i]);
		}

		/**
		 * Radio buttons
		 **/
		// Earliest departure radio button
		JRadioButton earliestDep = new JRadioButton("Earliest Departure");
		earliestDep.setBackground(FORE_COLOR);
		//birdButton.setSelected(true);

		// Fewest transfers radio button
		JRadioButton fewestTrans = new JRadioButton("Fewest Transfers");
		fewestTrans.setBackground(new Color(220,220,220));
		//catButton.setActionCommand(catString);

		// Earliest arrival radio button
		JRadioButton earliestArr = new JRadioButton("Earliest Arrival");
		earliestArr.setBackground(new Color(210,210,210));
		//dogButton.setActionCommand(dogString);

		// Button group for advanced options
		ButtonGroup group = new ButtonGroup();
		group.add(earliestDep);
		group.add(fewestTrans);
		group.add(earliestArr);

		/**
		 * Checkboxes
		 **/
		//define all checkboxes
		JCheckBox orderedList = new JCheckBox("Ordered List");
		orderedList.setBackground(FORE_COLOR);

		// Create GridBagLayout
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
		JInternalFrame right1 = newFrame(2,FORE_COLOR);
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
		///////////////////////////////////////

		//////////////////////////////////////
		//set up layout for LEFT jframe
		JInternalFrame topLeft = newFrame(2,FORE_COLOR);
		topLeft.setLayout(new FlowLayout());
		topLeft.setMinimumSize(new Dimension(300,40));
		c.gridwidth = GridBagConstraints.REMAINDER; //end row
		//c2.gridheight = GridBagConstraints.RELATIVE;
		c.weightx = 1;
		c.weighty = 0;
		gridbag.setConstraints(topLeft, c);
		left.add(topLeft);

		middleLeft = newFrame();
		//middleLeft.setBackground(new Color(255,255,255));
		middleLeft.setLayout(new FlowLayout());
		//middleLeft.setMinimumSize(new Dimension(100,100));
		c.gridwidth = GridBagConstraints.REMAINDER; //end row
		//c2.gridheight = GridBagConstraints.RELATIVE;
		c.weighty = 1;
		gridbag.setConstraints(middleLeft, c);
		left.add(middleLeft);

		JInternalFrame bottomLeft = newFrame(2,FORE_COLOR);
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
		JInternalFrame topMiddle = newFrame(2,FORE_COLOR);
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
		createMap();
		middleLeft.add(imageLabel);
		//bottomLeft.add(drawTrains);

		//add to middle jframe
		createTable(bottomMiddle);
		createStopsTable(right3);
		right1.add(orderedList);

		right2.add(addStop);
		right2.add(selectStop);
		right4.add(calcRoute);
		right5.add(earliestDep);
		right5.add(fewestTrans);
		right5.add(earliestArr);

		lastLeft.add(depTime);
		lastLeft.add(depFrame);
		depFrame.add(pickDep);
		lastRight.add(arrTime);
		lastRight.add(arrFrame);
		arrFrame.add(pickArr);

		//add internal jframes in order to fill the grid layout
		frame.add(left);
		frame.add(middle);
		frame.add(right);

		//pack the frames neatly		
		frame.pack();
		//frame.setSize(1920,1080);
	}

	/**
	 * Loads map
	 * @author NF
	 * **/
	public static void createMap() {
		try 
		{
			// Read from a file
			File FileToRead = new File(IMAGE_PATH);
			// Recognize file as image
			map = ImageIO.read(FileToRead);
			ImageIcon icon = new ImageIcon(map);
			// Show the image inside the label
			imageLabel.setIcon(icon);
		} 
		catch (Exception e) 
		{
			// Display a message if something goes wrong
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
		return newFrame(2,FORE_COLOR);
	}

	//takes an internal jframe and create a table in it
	private void createTable(JInternalFrame container){
		tableModel = new DefaultTableModel();
		tableModel.addTableModelListener(this);
		table = new JTable(tableModel);
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

	/**
	 * Updates table and map based on lines and trains
	 * @author AG, CM and NF
	 **/
	public static void update() {
		Graphics2D g = (Graphics2D)map.getGraphics();
		g.clearRect(0, 0, imageLabel.getWidth(), imageLabel.getHeight());
		createMap();
		/*
		trans = new AffineTransform(g.getTransform());
		trans.scale(scaleX, scaleY);
		g.setTransform(trans);*/

		// If the trains should be shown
		if (showTrains) {
			// Number of trains
			int numTrains = 0;
			// Get the total number of trains
			for (TrainLine l : trainLines) {
				numTrains += l.getTrains().size();
			}

			// initialize length of data array
			data = new Object[numTrains][];
		}

		// If the stops should be shown
		else {
			// Set data array to size of the stops list
			data = new Object[stops.size()][];
			// Iterate through stops
			for (Stop s : stops) {
				// Create row to hold stop info
				Object[] row = { s.Line, s.stop_name, s.stopID };
				// Add row to data array
				data[stops.indexOf(s)] = row;
			}
		}

		// Counter for rows in data array
		int counter = 0;
		// iterate through lines
		for (TrainLine line : trainLines) {
			String lineName = line.getLine();
			// Get list of trains for each line
			LinkedList<Train> trains = line.getTrains();
			for (int t = 0; t < trains.size(); t++) {
				Train train = trains.get(t);

				Object[] row = new Object[4];

				if (train.getTrainPredictions() != null) {
					String nextString = train.getTrainPredictions().get(0).getName();
					int timeLeft = train.getTrainPredictions().get(0).getTime();
					String destString = train.getTrainDestination();
					row[0] = train.getTrainID();
					row[1] = lineName;
					row[2] = nextString;
					row[3] = train.getTrainDestination();

					// Draw trains on map
					if(stopMap.containsKey(nextString.toUpperCase()) 
							&& stopMap.containsKey(destString.toUpperCase())
							&& !nextString.equals(destString)) {
						drawTrain(nextString, destString, timeLeft);
					}
				}
				else {
					row[0] = train.getTrainID();
					row[1] = lineName;
					row[2] = "";
					row[3] = train.getTrainDestination();
				}

				// Add trains to data array for table
				if (showTrains) {
					data[counter] = row;
					counter++;
				}
			}
		}

		// Set columns to trains or stops
		if (tableModel != null) {
			tableModel.setDataVector(data, (showTrains ? trainColumns : stopColumns));
		}

		// Dispose of graphics object
		g.dispose();
		// Repaint the imageLabel
		imageLabel.repaint();
	}

	//returns the last stop
	//NF
	public static String getLastStop(Stop curStop, Stop dest){
		String tempString = dest.stop_name;
		if(!curStop.StartOfLine || !curStop.EndOfLine){
			if(curStop.stopID < dest.stopID){
				String temp2 =  TripPlanner.getStopNameByID(curStop.stopID - 2);
				if(stops.contains(TripPlanner.getStopByName(tempString))){
					tempString = temp2;
				}
			}
			if(curStop.stopID > dest.stopID){
				String temp2 = TripPlanner.getStopNameByID(curStop.stopID + 2);
				if(stops.contains(TripPlanner.getStopByName(tempString))){
					tempString = temp2;
				}
			}
		}

		return tempString;

	}

	//linear interpolation between 2 floats and time
	public static Float LinearInterpolate(Float y1,Float y2,Float mu)
	{
		return(y1*(1-mu)+y2*mu);
	}

	//makes the stops table (to be refactored)
	//NF
	private void createStopsTable(JInternalFrame container) {
		stopsTableModel = new DefaultTableModel();
		stopsTableModel.addTableModelListener(this);
		stopsTable = new JTable(stopsTableModel);
		stopsTable.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
		stopsTable.setShowVerticalLines(true);
		JScrollPane scrollPane = new JScrollPane(stopsTable); 
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		//table.setFillsViewportHeight(true);
		
		updateStopsTable();

		container.add(stopsTable.getTableHeader(), BorderLayout.PAGE_START);
		container.add(scrollPane);
	}
	
	// Update stops table data
	private static void updateStopsTable() {
		stopsData = new Object[selectedStops.size()][];
		
		for (int s = 0; s < selectedStops.size(); s++) {
			// Create row to hold selected stop info
			Object[] row = { selectedStops.get(s) };
			
			stopsData[s] = row;
		}
		
		if (stopsTableModel != null)
			stopsTableModel.setDataVector(stopsData, stopsTableColumns);
	}

	/**
	 * Draws a train
	 * @author NF and refactored by AG
	 * **/
	public static void drawTrain(String nextStop, String dest, int timeLeft) {
		Graphics2D g = (Graphics2D)map.getGraphics();

		int x = 0;
		int y = 0;	
		int lastX = 0;
		int lastY = 0;
		//int z = stopMap.get(getLastStop(getStopByName(position),getStopByName(destination))).x;
		Stop next = TripPlanner.getStopByName(nextStop);
		Stop destination = TripPlanner.getStopByName(dest);
		String lastStop = getLastStop(next,destination);
		//System.out.println(a + " " + b + " " + lastStop);

		// Next stop position
		int nextX = stopMap.get(nextStop.toUpperCase()).x;
		int nextY = stopMap.get(nextStop.toUpperCase()).y;

		// Previous stop position
		if(stopMap.containsKey(lastStop.toUpperCase())) {
			lastX = stopMap.get(lastStop.toUpperCase()).x;		
			lastY = stopMap.get(lastStop.toUpperCase()).y;
		}
		//System.out.println(lastStop.toUpperCase());

		// If the train will soon reach the next predicted stop
		// or the previous stop is can't be determined
		if(timeLeft < MIN_TIME || lastX == 0){
			x = nextX;
			y = nextY;
		}
		// If the train won't soon reach the next predicted stop
		else if(timeLeft > MAX_TIME || nextX == 0){
			x = lastX;
			y = lastY;
		}
		// Draw the train in between stops
		else {
			x = (int)(LinearInterpolate((float)lastX, (float)nextX, (float)timeLeft/MAX_TIME)*100)/100;
			y = (int)(LinearInterpolate((float)lastY, (float)nextY, (float)timeLeft/MAX_TIME)*100)/100;
			//x = (nextX+lastX)/2;
			//y = (nextY+lastY)/2;
		}

		g.setColor(TRAIN_COLOR);
		g.fillOval(x-9, y-9, 18, 18);
		g.dispose();
		imageLabel.repaint();
	}

	// scales the map on mouse click; scales in for left click, scales out for right click
	// NF + AG
	public void mouseClicked(MouseEvent e) {
		int button = e.getButton();		
		if (button == MouseEvent.BUTTON1) {
			//Graphics2D g = (Graphics2D)map.getGraphics();

			/*
				String name = JOptionPane.showInputDialog(null,
						"What is your name?",
						"Enter your name",
						JOptionPane.QUESTION_MESSAGE);
				//MapStop stop1 = new MapStop(e.getX(),e.getY());
				//stopMap.put(name, stop1);
				//drawNode(stopMap.get("Downtown Crossing").x,stopMap.get("Downtown Crossing").y,g);
				System.out.println("stopMap.put('"+name+"',new MapStop("+e.getX()+","+e.getY()+"));");
			 */


			System.out.println("button 1");
			scaleX *= 1.5;
			scaleY *= 1.5;			
		}

		/*
			else if (button == MouseEvent.BUTTON3) {
				System.out.println("button 3");
				scaleX /= 1.5;
				scaleY /= 1.5;
				createMap(scaleX, scaleY, SCALE_TYPE);
			}
		 */
	}

	//sets the location of the map using the mouse coordinates
	//NF
	public void mouseDragged(MouseEvent e) {		
		imageLabel.setLocation(e.getX() - draggedAtX + imageLabel.getX(),
				e.getY() - draggedAtY + imageLabel.getY());		
	}

	//obtains the current mouse coordinates upon mousePress
	//NF
	public void mousePressed(MouseEvent e) {

		if (e.getSource() == imageLabel) {
			draggedAtX = e.getX();
			draggedAtY = e.getY();
		}
	}

	public static void pushHash(){
		stopMap.put("OAK GROVE",new MapStop(799,77));
		stopMap.put("MALDEN",new MapStop(798,143));
		stopMap.put("WELLINGTON",new MapStop(799,214));
		stopMap.put("SULLIVAN",new MapStop(799,298));
		stopMap.put("COMMUNITY COLLEGE",new MapStop(801,377));
		stopMap.put("NORTH STATION",new MapStop(813,471));
		stopMap.put("CHINATOWN",new MapStop(724,837));
		stopMap.put("TUFTS MEDICAL CENTER",new MapStop(684,872));
		stopMap.put("BACK BAY",new MapStop(652,907));
		stopMap.put("MASS AVE",new MapStop(616,944));
		stopMap.put("RUGGLES",new MapStop(579,979));
		stopMap.put("ROXBURY CROSSING",new MapStop(544,1011));
		stopMap.put("JACKSON SQUARE",new MapStop(506,1049));
		stopMap.put("STONY BROOK",new MapStop(472,1085));
		stopMap.put("GREEN STREET",new MapStop(433,1123));
		stopMap.put("FOREST HILLS",new MapStop(400,1158));
		stopMap.put("DOWNTOWN CROSSING",new MapStop(798,761));
		stopMap.put("STATE",new MapStop(870,681));
		stopMap.put("HAYMARKET",new MapStop(867,561));
		stopMap.put("ALEWIFE",new MapStop(165,321));
		stopMap.put("DAVIS",new MapStop(253,321));
		stopMap.put("PORTER",new MapStop(374,343));
		stopMap.put("HARVARD",new MapStop(453,417));
		stopMap.put("CENTRAL SQUARE",new MapStop(521,487));
		stopMap.put("KENDALL/MIT",new MapStop(600,565));
		stopMap.put("CHARLES/MGH",new MapStop(672,638));
		stopMap.put("PARK ST",new MapStop(728,691));
		stopMap.put("SOUTH STATION",new MapStop(882,846));
		stopMap.put("BROADWAY",new MapStop(901,942));
		stopMap.put("ANDREW",new MapStop(899,1021));
		stopMap.put("JFK/UMASS",new MapStop(901,1104));
		stopMap.put("NORTH QUINCY",new MapStop(1075,1422));
		stopMap.put("WOLLASTON",new MapStop(1147,1496));
		stopMap.put("QUINCY CENTER",new MapStop(1222,1569));
		stopMap.put("QUINCY ADAMS",new MapStop(1292,1639));
		stopMap.put("BRAINTREE",new MapStop(1314,1794));
		stopMap.put("BOWDOIN",new MapStop(746,565));
		stopMap.put("GOVERNMENT CENTER",new MapStop(798,619));
		stopMap.put("AQUARIUM",new MapStop(936,621));
		stopMap.put("MAVERICK",new MapStop(1052,505));
		stopMap.put("AIRPORT",new MapStop(1111,448));
		stopMap.put("WOOD ISLAND",new MapStop(1161,399));
		stopMap.put("ORIENT HEIGHTS",new MapStop(1214,345));
		stopMap.put("SUFFOLK DOWNS",new MapStop(1267,292));
		stopMap.put("BEACHMONT",new MapStop(1320,238));
		stopMap.put("REVERE BEACH",new MapStop(1371,186));
		stopMap.put("WONDERLAND",new MapStop(1435,124));
		stopMap.put("SAVIN HILL",new MapStop(847,1159));
		stopMap.put("FIELDS CORNER",new MapStop(797,1230));
		stopMap.put("SHAWMUT",new MapStop(799,1282));
		stopMap.put("ASHMONT",new MapStop(799,1342));
	}

	//returns an array of strings from min to max
	//NF
	public String[] getTime(int min, int max){
		String[] time = new String[max];
		for(int i =0; i<time.length;i++){
			time[i] = String.valueOf(i);
		}
		return time;
	}

	public static void drawPath(LinkedList<Stop> path, Color color) {
		Graphics2D g = (Graphics2D)map.getGraphics();
		for (int s = 0; s < path.size()-1; s++) {
			MapStop startPos = stopMap.get(path.get(s).stop_name.toUpperCase());
			int startX = startPos.x;
			int startY = startPos.y;

			MapStop endPos = stopMap.get(path.get(s+1).stop_name.toUpperCase());
			int endX = endPos.x;
			int endY = endPos.y;

			g.setStroke(new BasicStroke(10F));
			g.setColor(color);
			g.drawLine(startX, startY, endX, endY);
			imageLabel.repaint();
		}
	}

	/**
	 * Unimplemented required methods
	 * **/
	@Override
	public void mouseMoved(MouseEvent e) {
	}
	@Override
	public void mouseEntered(MouseEvent arg0) {
	}
	@Override
	public void mouseExited(MouseEvent arg0) {
	}
	@Override
	public void mouseReleased(MouseEvent arg0) {
	}
	@Override
	public void tableChanged(TableModelEvent e) {
	}
} 