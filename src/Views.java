import java.awt.*;
import javax.swing.*; 
import java.io.File;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

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
	public static HashMap<String,Point> stopMap = new HashMap<String,Point>(70);
	public static HashMap<Train,Point> trainMap = new HashMap<Train,Point>(70);
	public static final String IMAGE_PATH = "mbta.bmp";
	public static Image map;
	public static JLabel imageLabel;
	static JInternalFrame middleLeft;
	static double scaleX = 1.0;
	static double scaleY = 1.0;
	static AffineTransform trans;

	public static final int MAX_TIME = 1300;
	public static final int MIN_TIME = 15;

	private static final int STOP_THRESHOLD = 15;
	private static final int TRAIN_THRESHOLD = 10;

	public static LinkedList<TrainLine> trainLines = new LinkedList<TrainLine>();
	private static LinkedList<Stop> stops;
	private static LinkedList<String> selectedStops = new LinkedList<String>();
	private static LinkedList<Integer> pathList = new LinkedList<Integer>();

	/**
	 * Table declarations
	 * **/
	public static JTable table;
	public static DefaultTableModel tableModel;
	public static JTable stopsTable;
	public static DefaultTableModel stopsTableModel;
	// Table data
	static Object[][] data;
	// Row Colors for table
	static LinkedList<Color> rowColors = new LinkedList<Color>();
	// Stops table data
	static Object[][] stopsData;
	// Column names for the list of trains
	private static String[] trainColumns = {"Next Stop", "Time To Next Stop", "Destination"};
	// Column names for the list of stops
	private static String[] stopColumns = {"Name"};
	//
	private static String[] stopsTableColumns = {"Selected Stops"};
	private static JButton removeStop;
	private static boolean showTrains = true;
	/*
	private enum viewState {
		VIEWING_TRAINS,
		VIEWING_STOPS,
		VIEWING_ROUTE
	}*/

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
	// Train color
	static Color TRAIN_COLOR = Color.green;
	// Path color
	static Color PATH_COLOR = new Color(255, 255, 255, 200);

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
	 * @author NF and AG
	 */
	public void createWindow() {
		JFrame frame = new JFrame("MBTA Trip Planner");
		frame.setBackground(BACK_COLOR);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setMinimumSize(MIN_SIZE);
		frame.setMaximumSize(MAX_SIZE);
		frame.setPreferredSize(MIN_SIZE);
		//frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

		/**
		 * The Labels
		 * **/
		// The label that holds the map
		imageLabel = new JLabel() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -5687904822158992635L;

			// Custom tool tip handler
			// AG
			@Override
			public String getToolTipText(MouseEvent event) {
				int x = event.getX();
				int y = event.getY();
				String t = null;

				// Create tool tip for a train
				for(Train s : trainMap.keySet()){
					Point posn = trainMap.get(s);
					if((x < posn.x + TRAIN_THRESHOLD) && (x > posn.x - TRAIN_THRESHOLD)
							&& (y < posn.y + TRAIN_THRESHOLD) && (y > posn.y - TRAIN_THRESHOLD)) {
						LinkedList<Integer> goals = new LinkedList<Integer>();
						for(Prediction p : s.getTrainPredictions()){
							goals.add(TripPlanner.getStopByName(p.getName()).stopID);
						}

						//drawLineFromPoint(posn, stopMap.get(s.getTrainPredictions().get(0).getName()), Color.pink);
						//drawTrainPath(goals);

						t = "<html>Schedule:";
						t += "<br>"+s.getTrainID();

						boolean addMorePreds = true;
						int numPreds = 0;
						while (addMorePreds) {
							List<Prediction> preds = s.getTrainPredictions();
							if (numPreds <= 2 && preds.size()-1 >= numPreds) {
								t += "<br>"+s.getTrainPredictions().get(numPreds).getName() + ": "
										+ s.getTrainPredictions().get(numPreds).getTime().toString() + " Seconds Left";
								numPreds++;
							}
							else {
								addMorePreds = false;
							}
						}

						t += "</html>";
						return t;
					}
				}

				// Create tool tip for a map
				for(String s : stopMap.keySet()){
					Point posn = stopMap.get(s);

					if ((x < posn.x + STOP_THRESHOLD) && (x > posn.x - STOP_THRESHOLD)
							&& (y < posn.y + STOP_THRESHOLD) && (y > posn.y - STOP_THRESHOLD)){
						t = "<html>"+s;

						t += "</html>";
						return t;
					}
				}
				return t;
			}
		};
		imageLabel.addMouseListener(this);
		imageLabel.addMouseMotionListener(this);
		imageLabel.setToolTipText("Nothing");
		ToolTipManager.sharedInstance().registerComponent(imageLabel);
		ToolTipManager.sharedInstance().setInitialDelay(0);

		JLabel depLabel = new JLabel("Departure Time");
		JLabel arrLabel = new JLabel("Arrival Time");

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

		// Remove Stop button
		removeStop = new JButton("Remove Stop");
		removeStop.setBackground(FORE_COLOR);
		removeStop.setEnabled(false);
		removeStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LinkedList<Integer> removeList = new LinkedList<Integer>();
				for (int index : stopsTable.getSelectedRows()) {
					removeList.add(index);
				}
				Collections.reverse(removeList);
				for (int r : removeList) {
					selectedStops.remove(r);
				}
				updateStopsTable();
			}
		});

		// Calculate Route button
		JButton calcRoute = new JButton("Calculate Route");
		calcRoute.setBackground(FORE_COLOR);
		calcRoute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pathList.clear();
				for (String s : selectedStops) {
					pathList.add(TripPlanner.getStopByName(s).stopID);
				}
				update();
			}
		});

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
		orderedList.setSelected(true);

		/**
		 * Create Layout
		 * **/
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		frame.setLayout(new GridLayout(1,2));
		// Make the window visible
		frame.setVisible(true);

		JInternalFrame plannerFrame = newFrame();
		plannerFrame.setLayout(gridbag);
		//create left, middle, right internal jframes
		JInternalFrame left = newFrame();
		JInternalFrame right = newFrame();
		JInternalFrame middle = newFrame();

		frame.add(left);
		frame.add(plannerFrame);

		c.weightx = 1;
		c.weighty = 1;
		middle.setMinimumSize(new Dimension(800,0));	
		gridbag.setConstraints(middle, c);
		plannerFrame.add(middle);

		//c.gridheight = GridBagConstraints.REMAINDER;
		c.weightx = 0;
		//c.gridx = 2;
		//c.gridy = 0;
		right.setMinimumSize(new Dimension(250,500));	
		gridbag.setConstraints(right, c);
		//plannerFrame.add(right);

		//set internal jframe layouts
		left.setLayout(gridbag);
		middle.setLayout(gridbag);
		//right.setLayout(new GridLayout(3,1,5,5));
		right.setLayout(gridbag);

		JInternalFrame plannerButtons = newFrame();
		plannerButtons.setLayout(gridbag);
		gridbag.setConstraints(plannerButtons, c);

		//////////////////////////////////////
		//set up layout for rightmost jframe
		JInternalFrame right1 = newFrame(0,FORE_COLOR);
		right1.setMinimumSize(new Dimension(300,40));
		right1.setLayout(new FlowLayout());
		c.gridwidth = GridBagConstraints.REMAINDER; //end row
		c.weightx = 0.3;
		c.weighty = 0.0;
		gridbag.setConstraints(right1, c);
		right.add(right1);

		JInternalFrame stopTableControls = newFrame(0, new Color(200,200,200));
		stopTableControls.setMinimumSize(new Dimension(300,40));
		stopTableControls.setLayout(new GridLayout(1,3));
		c.gridwidth = GridBagConstraints.REMAINDER; //end row
		c.weightx = 0;
		c.weighty = 0;
		gridbag.setConstraints(stopTableControls, c);
		right.add(stopTableControls);

		JInternalFrame right3 = newFrame(0,new Color(200,200,200));
		//right3.setMinimumSize(new Dimension(300,40));
		right3.setLayout(new GridLayout(1,2));
		c.weighty = 1;
		c.gridwidth = GridBagConstraints.REMAINDER; //end row
		gridbag.setConstraints(right3, c);
		right.add(right3);

		JInternalFrame right4 = newFrame(0,new Color(200,200,200));
		//right4.setMinimumSize(new Dimension(300,40));
		right4.setLayout(new FlowLayout());
		c.weightx = 1;
		//c.weighty = 0;
		c.gridwidth = GridBagConstraints.REMAINDER; //end row
		gridbag.setConstraints(right4, c);
		plannerButtons.add(right4);

		JInternalFrame right5 = newFrame(0,new Color(200,200,200));
		//right5.setMinimumSize(new Dimension(300,40));
		right5.setLayout(new GridLayout(3,1,5,5));
		c.weighty = 1;
		c.gridwidth = GridBagConstraints.REMAINDER; //end row
		gridbag.setConstraints(right5, c);
		plannerButtons.add(right5);

		JInternalFrame right6 = newFrame(0,new Color(200,200,200));
		//right6.setMinimumSize(new Dimension(300,60));
		right6.setLayout(new GridLayout(1,2,5,5));
		c.weighty = 1;
		c.gridwidth = GridBagConstraints.REMAINDER; //end row
		gridbag.setConstraints(right6, c);
		plannerButtons.add(right6);

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
		// Set up layout for LEFT JFrame
		JInternalFrame topLeft = newFrame(2,FORE_COLOR);
		topLeft.setLayout(new FlowLayout());
		topLeft.setMinimumSize(new Dimension(300,40));
		c.gridwidth = GridBagConstraints.REMAINDER; //end row
		//c2.gridheight = GridBagConstraints.RELATIVE;
		c.weightx = 1;
		c.weighty = 0;
		gridbag.setConstraints(topLeft, c);
		left.add(topLeft);

		middleLeft = newFrame(0,Color.black);
		middleLeft.setLayout(new FlowLayout());
		//middleLeft.setMinimumSize(new Dimension(100,100));
		c.gridwidth = GridBagConstraints.REMAINDER; //end row
		//c2.gridheight = GridBagConstraints.RELATIVE;
		c.weighty = 1;
		gridbag.setConstraints(middleLeft, c);
		left.add(middleLeft);

		/*
		JInternalFrame bottomLeft = newFrame(2,FORE_COLOR);
		bottomLeft.setLayout(new FlowLayout());
		bottomLeft.setMinimumSize(new Dimension(300,40));
		c.gridwidth = GridBagConstraints.REMAINDER; //end row
		//c2.gridheight = GridBagConstraints.RELATIVE;
		c.weighty = 0;
		gridbag.setConstraints(bottomLeft, c);
		left.add(bottomLeft);
		 */
		///////////////////////////////////////

		/////////////////////////////////////
		JInternalFrame topMiddle = newFrame(2,FORE_COLOR);
		topMiddle.setLayout(new GridLayout(1,3));
		//topMiddle.setLayout(new FlowLayout());
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
		bottomMiddle.setLayout(new GridLayout(2,1));
		bottomMiddle.setMinimumSize(new Dimension(300,400));
		//middleLeft.setMinimumSize(new Dimension(100,100));
		c.gridwidth = GridBagConstraints.REMAINDER; //end row
		//c2.gridheight = GridBagConstraints.RELATIVE;
		c.weighty = 0.5;
		gridbag.setConstraints(bottomMiddle, c);
		middle.add(bottomMiddle);
		///////////////////////////////////////

		/**
		 * Left JFrame
		 * **/
		// Add test system button
		topLeft.add(testSystem);
		// Add the map
		createMap();
		middleLeft.add(imageLabel);

		//add to right jframe
		createTable(bottomMiddle);
		createStopsTable(right3);
		right1.add(orderedList);

		stopTableControls.add(addStop);
		stopTableControls.add(selectStop);
		stopTableControls.add(removeStop);
		right3.add(plannerButtons);
		right4.add(calcRoute);
		right5.add(earliestDep);
		right5.add(fewestTrans);
		right5.add(earliestArr);

		lastLeft.add(depLabel);
		lastLeft.add(depFrame);
		depFrame.add(pickDep);
		lastRight.add(arrLabel);
		lastRight.add(arrFrame);
		arrFrame.add(pickArr);

		bottomMiddle.add(right);

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
		tableModel = new DefaultTableModel() {
			private static final long serialVersionUID = -2290452371399748402L;

			@Override
			public boolean isCellEditable(int row, int column) {
				//all cells false
				return false;
			}
		};
		tableModel.addTableModelListener(this);
		table = new JTable(tableModel)/* {
			private static final long serialVersionUID = -1917817069879534003L;

			// Draw Line color behind row
			@Override
			public Component prepareRenderer (TableCellRenderer renderer, int index_row, int index_col){  
				Component comp = super.prepareRenderer(renderer, index_row, index_col);

				try {
					comp.setBackground(rowColors.get(index_row));
					// Change font color
					comp.setForeground(Color.white);
				}
				catch (IndexOutOfBoundsException e) {}

				if(isCellSelected(index_row, index_col)){
					comp.setBackground(new Color(0, 0, 112));  
				}
				return comp;
			}
		}*/;
		DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
			private static final long serialVersionUID = 273298630375212139L;

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
			{
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				try {
					//if (column == 0) {
						c.setBackground(rowColors.get(row));
						// Change font color
						c.setForeground(Color.white);
					/*}
					else {
						c.setBackground(Color.white);
						c.setForeground(Color.black);
					}*/
				}
				catch (IndexOutOfBoundsException e) {}
				
				return c;
			}
		};
		
		table.setDefaultRenderer(Object.class, cellRenderer);
		cellRenderer.setHorizontalAlignment(SwingConstants.CENTER);
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
		createMap();
		drawTrainPath(pathList);
		trainMap.clear();
		rowColors.clear();
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

			// Counter for rows in data array
			int counter = 0;
			// iterate through lines
			for (TrainLine line : trainLines) {
				String lineName = line.getLine();

				// Get list of trains for each line
				LinkedList<Train> trains = line.getTrains();
				for (int t = 0; t < trains.size(); t++) {
					Train train = trains.get(t);
					Object[] row = new Object[3];
					String nextString = train.getTrainPredictions().get(0).getName();
					int timeLeft = train.getTrainPredictions().get(0).getTime();
					String destString = train.getTrainDestination();
					row[0] = nextString;
					row[1] = timeLeft;
					row[2] = train.getTrainDestination();
					rowColors.add(lineToColor(lineName));

					// Draw trains on map
					if(stopMap.containsKey(nextString.toUpperCase()) 
							&& stopMap.containsKey(destString.toUpperCase())
							&& !nextString.equals(destString)) {
						createTrain(train);
					}

					// Add trains to data array for table
					if (showTrains) {
						data[counter] = row;
						counter++;
					}
				}
			}
		}

		// If the stops should be shown
		else {
			// Set data array to size of the stops list
			data = new Object[stops.size()][];
			// Iterate through stops
			for (Stop s : stops) {
				// Create row to hold stop info
				Object[] row = { s.stop_name };
				rowColors.add(lineToColor(s.Line));
				// Add row to data array
				data[stops.indexOf(s)] = row;
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
	public static String getLastStop(Stop nextStop, Stop dest){
		String tempString = nextStop.stop_name;
		if (!nextStop.EndOfLine) {
			if(nextStop.stopID < dest.stopID){
				String temp2 =  TripPlanner.getStopNameByID(nextStop.stopID - 2);
				if(stops.contains(TripPlanner.getStopByName(tempString))){
					tempString = temp2;
				}
			}
			if(nextStop.stopID > dest.stopID){
				String temp2 = TripPlanner.getStopNameByID(nextStop.stopID + 2);
				if(stops.contains(TripPlanner.getStopByName(tempString))){
					tempString = temp2;
				}
			}
		}
		else {
			if(nextStop.stopID < dest.stopID){
				String temp2 =  TripPlanner.getStopNameByID(nextStop.stopID + 2);
				if(stops.contains(TripPlanner.getStopByName(tempString))){
					tempString = temp2;
				}
			}
			if(nextStop.stopID > dest.stopID){
				String temp2 = TripPlanner.getStopNameByID(nextStop.stopID - 2);
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

	/**
	 * Makes the stops table
	 * @author NF and AG
	 * **/
	private void createStopsTable(JInternalFrame container) {
		stopsTableModel = new DefaultTableModel() {
			private static final long serialVersionUID = 36544289767178149L;

			@Override
			public boolean isCellEditable(int row, int column) {
				//all cells false
				return false;
			}
		};
		stopsTableModel.addTableModelListener(this);
		stopsTable = new JTable(stopsTableModel);
		stopsTable.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
		stopsTable.setShowVerticalLines(true);

		/*
		stopsTable.setDragEnabled(true);
		stopsTable.setDropMode(DropMode.INSERT_ROWS);
		//stopsTable.setTransferHandler(new TableRowTransferHandler(table)); 
		 */

		// Selection handler
		ListSelectionModel selectModel = stopsTable.getSelectionModel();
		selectModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		selectModel.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				int numSelected = stopsTable.getSelectedRowCount();

				if (numSelected != 0)
					removeStop.setEnabled(true);
				else
					removeStop.setEnabled(false);
			}

		});

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
	public static void createTrain(Train t) {
		Graphics2D g = (Graphics2D)map.getGraphics();

		String nextStop = t.getTrainPredictions().get(0).getName();
		int timeLeft = t.getTrainPredictions().get(0).getTime();
		String dest = t.getTrainDestination();
		int x = 0;
		int y = 0;	
		int lastX = 0;
		int lastY = 0;
		//int z = stopMap.get(getLastStop(getStopByName(position),getStopByName(destination))).x;

		Stop next = TripPlanner.getStopByName(nextStop);
		Stop destination = TripPlanner.getStopByName(dest);
		String lastStop = getLastStop(next,destination);
		Stop last =  TripPlanner.getStopByName(lastStop);
		//System.out.println(a + " " + b + " " + lastStop);

		// Next stop position
		int nextX = stopMap.get(nextStop.toUpperCase()).x;
		int nextY = stopMap.get(nextStop.toUpperCase()).y;

		if(nextStop.equals(dest)){
			x = lastX;
			y = lastY;
		}
		// Previous stop position
		if(stopMap.containsKey(lastStop.toUpperCase())) {
			lastX = stopMap.get(lastStop.toUpperCase()).x;	
			lastY = stopMap.get(lastStop.toUpperCase()).y;
		}
		//System.out.println(lastStop.toUpperCase());

		// If the train will soon reach the next predicted stop
		// or the previous stop is can't be determined
		if(timeLeft < MIN_TIME || (lastX == 0 && lastY == 0)){
			x = nextX;
			y = nextY;
		}
		// If the train won't soon reach the next predicted stop
		else if(timeLeft > MAX_TIME || (nextX == 0 && nextY == 0)){
			x = lastX;
			y = lastY;
		}
		// Draw the train in between stops
		else {
			float lerpTime = (float)timeLeft/MAX_TIME;
			//System.out.println(lerpTime);
			if(lerpTime >= 1.0f){ lerpTime = 1.0f;}
			else if(lerpTime <= 0.0f){ lerpTime = 0.0f;}
			x = (int)(LinearInterpolate((float)lastX, (float)nextX, lerpTime)*100)/100; //(float)timeLeft/MAX_TIME
			System.out.println(t.getTrainID());
			System.out.println("Last X: "+lastX);
			System.out.println("Next X: "+nextX);
			System.out.println("Last Y: "+lastY);
			System.out.println("Next Y: "+nextY);
			//System.out.println("X: "+x);
			//System.out.println("Y: "+y);
			y = (int)(LinearInterpolate((float)lastY, (float)nextY, lerpTime)*100)/100; //(float)timeLeft/MAX_TIME			
			//System.out.println((int)(LinearInterpolate(0f, (float)nextX, lerpTime)*100)/100);			
			//x = (nextX+lastX)/2;
			//y = (nextY+lastY)/2;
		}

		if(last.stopID < next.stopID){
			g.setColor(Color.black);
			// SOUTH --purple
			g.fillOval(x-18, y-9, 15, 15);
			g.setColor(new Color(220,50,220));
			g.fillOval(x-15, y-6, 11, 11);

		}
		if(last.stopID > next.stopID){
			g.setColor(Color.black);
			// NORTH --green
			g.fillOval(x, y-9, 15, 15);
			g.setColor(new Color(0,220,50));
			g.fillOval(x+3, y-6, 11, 11);
		}



		trainMap.put(t,new Point(x,y));
		g.dispose();
		imageLabel.repaint();
	}
	public static void pushHash(){
		stopMap.put("OAK GROVE",new Point(799,77));
		stopMap.put("MALDEN",new Point(798,143));
		stopMap.put("WELLINGTON",new Point(799,214));
		stopMap.put("SULLIVAN",new Point(799,298));
		stopMap.put("COMMUNITY COLLEGE",new Point(801,377));
		stopMap.put("NORTH STATION",new Point(813,471));


		stopMap.put("CHINATOWN",new Point(734,821));
		stopMap.put("TUFTS MEDICAL CENTER",new Point(680,876));
		stopMap.put("BACK BAY",new Point(617,939));
		stopMap.put("MASS AVE",new Point(560,994));
		stopMap.put("RUGGLES",new Point(504,1051));
		stopMap.put("ROXBURY CROSSING",new Point(449,1106));
		stopMap.put("JACKSON SQUARE",new Point(400,1157));
		stopMap.put("STONY BROOK",new Point(345,1211));
		stopMap.put("GREEN STREET",new Point(291,1264));
		stopMap.put("FOREST HILLS",new Point(239,1315));
		stopMap.put("DOWNTOWN CROSSING",new Point(798,761));
		stopMap.put("STATE",new Point(870,681));
		stopMap.put("HAYMARKET",new Point(867,561));
		stopMap.put("ALEWIFE",new Point(165,321));
		stopMap.put("DAVIS",new Point(253,321));
		stopMap.put("PORTER",new Point(374,343));
		stopMap.put("HARVARD",new Point(453,417));
		stopMap.put("CENTRAL SQUARE",new Point(521,487));
		stopMap.put("KENDALL/MIT",new Point(600,565));
		stopMap.put("CHARLES/MGH",new Point(672,638));
		stopMap.put("PARK ST",new Point(728,691));
		stopMap.put("SOUTH STATION",new Point(882,846));
		stopMap.put("BROADWAY",new Point(901,942));
		stopMap.put("ANDREW",new Point(899,1021));
		stopMap.put("JFK/UMASS",new Point(901,1104));
		stopMap.put("NORTH QUINCY",new Point(1075,1422));
		stopMap.put("WOLLASTON",new Point(1147,1496));
		stopMap.put("QUINCY CENTER",new Point(1222,1569));
		stopMap.put("QUINCY ADAMS",new Point(1292,1639));
		stopMap.put("BRAINTREE",new Point(1314,1794));
		stopMap.put("BOWDOIN",new Point(746,565));
		stopMap.put("GOVERNMENT CENTER",new Point(798,619));
		stopMap.put("AQUARIUM",new Point(936,621));
		stopMap.put("MAVERICK",new Point(1052,505));
		stopMap.put("AIRPORT",new Point(1111,448));
		stopMap.put("WOOD ISLAND",new Point(1161,399));
		stopMap.put("ORIENT HEIGHTS",new Point(1214,345));
		stopMap.put("SUFFOLK DOWNS",new Point(1267,292));
		stopMap.put("BEACHMONT",new Point(1320,238));
		stopMap.put("REVERE BEACH",new Point(1371,186));
		stopMap.put("WONDERLAND",new Point(1435,124));
		stopMap.put("SAVIN HILL",new Point(847,1159));
		stopMap.put("FIELDS CORNER",new Point(797,1230));
		stopMap.put("SHAWMUT",new Point(799,1282));
		stopMap.put("ASHMONT",new Point(799,1342));
	}

	/**
	 * Takes in a line name and returns the corresponding color
	 * @author AG
	 * **/
	private static Color lineToColor(String line) {
		if (line.equals("Orange")) {
			return new Color(255,100,0);
		}
		else if (line.equals("Red")) {
			return Color.red;

		}
		else if (line.equals("Blue")) {
			return Color.blue;
		}
		return Color.white;
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

	public static void drawLineFromPoint(Point startPosn, Point endPosn, Color c){
		Graphics2D g = (Graphics2D)map.getGraphics();
		g.setStroke(new BasicStroke(10F));
		g.setColor(c);
		g.drawLine(startPosn.x, startPosn.y, endPosn.x, endPosn.y);
		imageLabel.repaint();
	}

	public static void drawTrainPath(LinkedList<Integer> goals){
		Stack<Integer> results = new Stack<Integer>();
		results = TripPlanner.graph.multiSearch(goals);
		LinkedList<Stop> path = new LinkedList<Stop>();
		for (int r : results) {
			String stopName = TripPlanner.getStopNameByID(r);
			Stop s = TripPlanner.getStopByName(stopName);
			System.out.println(stopName);
			path.add(s);
		}
		Views.drawPath(path, PATH_COLOR);
	}

	public static void drawPath(LinkedList<Stop> path, Color color) {
		for (int s = 0; s < path.size()-1; s++) {
			Point startPos = stopMap.get(path.get(s).stop_name.toUpperCase());
			Point endPos = stopMap.get(path.get(s+1).stop_name.toUpperCase());
			drawLineFromPoint(startPos, endPos, color);
		}
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
				//Point stop1 = new Point(e.getX(),e.getY());
				//stopMap.put(name, stop1);
				//drawNode(stopMap.get("Downtown Crossing").x,stopMap.get("Downtown Crossing").y,g);
				System.out.println("stopMap.put('"+name+"',new Point("+e.getX()+","+e.getY()+"));");
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

	@Override
	public void mouseMoved(MouseEvent e) {
		/*
		update();
		Graphics2D g = (Graphics2D)map.getGraphics();
		int x = e.getX();
		int y = e.getY();

		for(Train s : trainMap.keySet()){
			Point posn = trainMap.get(s);
			System.out.println(s.getTrainID());
			if((x<posn.x+15) && (x>posn.x-15) && (y<posn.y+15) && (y>posn.y-15)){
				//update();
				//System.out.println("asf");
				g.setColor(Color.darkGray);
				g.fillRect(posn.x+15, posn.y-15, 230, 70);
				g.setColor(Color.LIGHT_GRAY);
				g.fillRect(posn.x+20, posn.y-20, 230, 70);
				g.setColor(Color.black);
				LinkedList<Integer> goals = new LinkedList<Integer>();

				for(Prediction p : s.getTrainPredictions()){
					//System.out.println(p.getID());
					goals.add(TripPlanner.getStopByName(p.getName()).stopID);
				}

				//drawLineFromPoint(posn, stopMap.get(s.getTrainPredictions().get(0).getName()), Color.pink);
				String predictions = "Schedule:";
				//TripPlanner.drawTrainPath(goals);
				g.drawBytes(predictions.getBytes(),0,predictions.length(),posn.x+30,posn.y-5);
				g.drawBytes(s.getTrainPredictions().get(0).getName().getBytes(),0,s.getTrainPredictions().get(0).getName().length(),posn.x+30,posn.y+10);
				g.drawBytes(s.getTrainPredictions().get(1).getName().getBytes(),0,s.getTrainPredictions().get(1).getName().length(),posn.x+30,posn.y+25);
				g.drawBytes(s.getTrainPredictions().get(2).getName().getBytes(),0,s.getTrainPredictions().get(2).getName().length(),posn.x+30,posn.y+40);

				String secondsLeft1 = s.getTrainPredictions().get(0).getTime().toString() + " Seconds Left";
				String secondsLeft2 = s.getTrainPredictions().get(1).getTime().toString() + " Seconds Left";
				String secondsLeft3 = s.getTrainPredictions().get(2).getTime().toString() + " Seconds Left";

				g.drawBytes(secondsLeft1.getBytes(),0,secondsLeft1.length(),posn.x+150,posn.y+10);
				g.drawBytes(secondsLeft2.getBytes(),0,secondsLeft2.length(),posn.x+150,posn.y+25);
				g.drawBytes(secondsLeft3.getBytes(),0,secondsLeft3.length(),posn.x+150,posn.y+40);

				g.dispose();	    	
				imageLabel.repaint();

			}

		}

		for(String s : stopMap.keySet()){
			Point posn = stopMap.get(s);

			if((x<posn.x+15) && (x>posn.x-15) && (y<posn.y+15) && (y>posn.y-15)){
				//update();
				//System.out.println("asf");
				g.setColor(Color.LIGHT_GRAY);
				g.fillRect(posn.x+20, posn.y-20, 100, 20);
				g.setColor(Color.black);
				g.drawBytes(s.getBytes(),0,s.getBytes().length,posn.x+30,posn.y-5);
				g.dispose();	    	
				imageLabel.repaint();
			}
		}*/

	}

	/**
	 * Unimplemented required methods
	 * **/
	@Override
	public void mouseEntered(MouseEvent e) {

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