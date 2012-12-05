import java.awt.*;
import javax.swing.*; 
import java.io.File;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
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
	public static LinkedList<String> instructions = new LinkedList<String>();
	public static HashMap<String,Point> stopMap = new HashMap<String,Point>(70);
	public static HashMap<Train,Point> trainMap = new HashMap<Train,Point>(70);
	public static final String IMAGE_PATH = "mbta.bmp";
	public static Image map;
	public static JLabel imageLabel;
	static JInternalFrame middleLeft;
	public static JCheckBox orderedList;

	public static final int MAX_TIME = 1000;
	public static final int MIN_TIME = 30;

	private static final int STOP_THRESHOLD = 15;
	private static final int TRAIN_THRESHOLD = 10;

	public static LinkedList<TrainLine> trainLines = new LinkedList<TrainLine>();
	private static LinkedList<Stop> stops;
	private static LinkedList<Integer> selectedStops = new LinkedList<Integer>();
	private static Stack<Integer> results = new Stack<Integer>();
	private static LinkedList<Integer> pathList = new LinkedList<Integer>();

	/**
	 * Table declarations
	 * **/
	public static JTable table;
	public static DefaultTableModel tableModel;
	public static JTable stopsTable;
	public static DefaultTableModel stopsTableModel;
	public static DefaultTableCellRenderer cellRenderer;
	// Table data
	static Object[][] data;
	// Row Colors for table
	static LinkedList<Color> rowColors = new LinkedList<Color>();
	static LinkedList<Color> stopRowColors = new LinkedList<Color>();
	// Stops table data
	static Object[][] stopsData;
	// Column names for the VIEW_TRAINS state
	private static String[] trainColumns = {"Next Stop", "Time To Next Stop", "Destination"};
	// Column names for the VIEW_STOPS state
	private static String[] stopColumns = {"Name", "Next Train Arrival Time"};
	// Column names for the VIEW_ROUTE state
	private static String[] routeColumns = {"Stops", "Instructions"};
	// Columns for Selected stops table
	private static String[] stopsTableColumns = {"Selected Stops"};
	private static JButton removeStop;


	private enum ViewState {
		VIEW_TRAINS,
		VIEW_STOPS,
		VIEW_ROUTE
	}
	private static ViewState currentViewState = ViewState.VIEW_TRAINS;

	private enum OptionsState {
		EARLY_ARR,
		EARLY_DEP,
		FEW_TRANS
	}
	private static OptionsState currentOptionsState = OptionsState.FEW_TRANS;

	JComboBox<String> selectStop;

	int draggedAtX, draggedAtY;

	static int timeOffset = 0;

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
	// Orange Line Color
	static Color ORANGE_LINE_COLOR = new Color(200,60,0);
	// Red Line Color
	static Color RED_LINE_COLOR = new Color(150,0,0);
	// Blue Line Color
	static Color BLUE_LINE_COLOR = new Color(6,6,80);

	// Values for drawing train circles
	private static final int SB_X_OFFSET = -18;
	private static final int SB_Y_OFFSET = -9;
	private static final int NB_X_OFFSET = 3;
	private static final int NB_Y_OFFSET = -9;
	private static final int CIRCLE_DIFF = -3;
	private static final Color CIRCLE_BACK_COLOR = Color.black;
	private static final Color SB_CIRCLE_COLOR = new Color(220,50,220);
	private static final Color NB_CIRCLE_COLOR = new Color(0,220,50);
	private static final int INNER_CIRCLE_SIZE = 11;
	private static final int OUTER_CIRCLE_SIZE = 15;

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
		// Update table and map if viewstate is trains
		if (currentViewState == ViewState.VIEW_TRAINS)
			update();
		else
			updateMap();
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
			private static final long serialVersionUID = -5687904822158992635L;

			// Custom tooltip handler
			// AG
			@Override
			public String getToolTipText(MouseEvent event) {
				int x = event.getX();
				int y = event.getY();
				String t = null;

				// Create tool tip for a train
				for(Train s : trainMap.keySet()){
					Point posn = trainMap.get(s);
					if(trainCollider(x, y, posn.x, posn.y)) {
						LinkedList<Integer> goals = new LinkedList<Integer>();
						for(Prediction p : s.getTrainPredictions()){
							goals.add(TripPlanner.getStopByName(p.getName()).stopID);
						}

						drawLineFromPoint(posn, 
								stopMap.get(TripPlanner.getStopNameByID(goals.get(0))),
								new Color(255, 255, 0, 200));
						drawTrainPath(goals, new Color(255, 255, 0, 200));

						t = "<html><font style='font-size:20;'>";
						t += "<br>"+s.getTrainID();

						boolean addMorePreds = true;
						int numPreds = 0;
						while (addMorePreds) {
							List<Prediction> preds = s.getTrainPredictions();
							if (numPreds <= 5 && preds.size()-1 >= numPreds) {
								t += "<br>Arriving at "+s.getTrainPredictions().get(numPreds).getName() + " in "
										+ s.getTrainPredictions().get(numPreds).getTime().toString() + " seconds";
								numPreds++;
							}
							else {
								addMorePreds = false;
							}
						}

						t += "</font></html>";
						return t;
					}
				}

				// Create tool tip for a stop
				for(String s : stopMap.keySet()){
					Point posn = stopMap.get(s);

					Train northTrain = TripPlanner.getTrainAtStop(s, TripPlanner.Direction.NORTHBOUND);
					Train southTrain = TripPlanner.getTrainAtStop(s, TripPlanner.Direction.SOUTHBOUND);

					if (stopCollider(x, y, posn.x, posn.y)){
						t = "<html><font style='font-size:20;'>"+getNextTrain(s, TripPlanner.Direction.NORTHBOUND)+"<br>"
								+ getNextTrain(s, TripPlanner.Direction.SOUTHBOUND);

						// Draw northbound train
						if (northTrain != null) {
							Point northPosn = trainMap.get(northTrain);
							Graphics2D g = (Graphics2D) map.getGraphics();
							g.setStroke(new BasicStroke(5f));
							g.setColor(NB_CIRCLE_COLOR);
							if(northPosn != null)
								g.drawOval(northPosn.x+CIRCLE_DIFF-2, northPosn.y+CIRCLE_DIFF-2, 20, 20);
						}
						// Draw southbound train
						if (southTrain != null) {
							Point trainPosn = trainMap.get(southTrain);
							Graphics2D g = (Graphics2D) map.getGraphics();
							g.setStroke(new BasicStroke(5f));
							g.setColor(SB_CIRCLE_COLOR);
							if(trainPosn != null)
								g.drawOval(trainPosn.x+CIRCLE_DIFF-2, trainPosn.y+CIRCLE_DIFF-2, 20, 20);
						}

						t += "</font></html>";
						return t;
					}
				}
				return t;
			}
		};
		imageLabel.addMouseListener(this);
		imageLabel.addMouseMotionListener(this);
		imageLabel.setToolTipText("");
		ToolTipManager.sharedInstance().registerComponent(imageLabel);
		ToolTipManager.sharedInstance().setDismissDelay(10000);
		ToolTipManager.sharedInstance().setInitialDelay(0);

		/**
		 * Spinners
		 * **/
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
				currentViewState = ViewState.VIEW_TRAINS;
				updateTable();
			}
		});

		// List Stops button
		JButton listStops = new JButton("List Stops");
		listStops.setBackground(FORE_COLOR);
		listStops.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				currentViewState = ViewState.VIEW_STOPS;
				updateTable();
			}
		});

		// List Route button
		JButton listRoute = new JButton("List Route");
		listRoute.setBackground(FORE_COLOR);
		listRoute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				currentViewState = ViewState.VIEW_ROUTE;
				updateTable();
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
				update();
			}
		});

		// Add Stop button
		JButton addStop = new JButton("Add Stop");
		addStop.setBackground(FORE_COLOR);
		addStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = selectStop.getSelectedIndex();
				Stop s = stops.get(index);

				selectedStops.add(s.stopID);
				System.out.println(s.stopID);
				System.out.println(selectedStops.toString());
				stopRowColors.add(lineToColor(s.Line));

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
					stopRowColors.remove(r);
				}
				updateStopsTable();
			}
		});

		// Calculate Route button
		JButton calcRoute = new JButton("Calculate Route");
		calcRoute.setBackground(FORE_COLOR);
		calcRoute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				timeOffset = 0;

				pathList = new LinkedList<Integer>();
				pathList = selectedStops;

				results = new Stack<Integer>();

				// Use unordered search
				if (!orderedList.isSelected())
					results = TripPlanner.graph.unorderedPermSearch(pathList);
				// Use ordered search
				else
					results = TripPlanner.graph.multiSearch(pathList);

				LinkedList<Integer> path = new LinkedList<Integer>();
				path.addAll(results);

				LinkedList<String> insts = new LinkedList<String>();
				createInstructions(path, path.size(), insts);
				instructions = new LinkedList<String>();
				instructions = insts;

				currentViewState = ViewState.VIEW_ROUTE;
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

		// Populate combo box
		selectStop = new JComboBox<String>() {
			private static final long serialVersionUID = 7447626094431663178L;


		};
		DefaultListCellRenderer comboRenderer = new DefaultListCellRenderer() {
			private static final long serialVersionUID = 273298630375212139L;

			@Override
			public Component getListCellRendererComponent(
					JList<?> list,
					Object value,
					int index,
					boolean isSelected,
					boolean cellHasFocus) {
				JLabel text = new JLabel();
				text.setOpaque(true);

				text.setForeground(Color.white);

				text.setText(value.toString());
				if (index > -1) {
					text.setBackground(lineToColor(stops.get(index).Line));
				}
				return text;
			}
		};
		selectStop.setRenderer(comboRenderer);
		for(int i = 0; i < stopList.length; i++){
			selectStop.addItem(stopList[i]);
		}

		/**
		 * Radio buttons
		 **/
		// Earliest departure radio button
		JRadioButton earliestDep = new JRadioButton("Earliest Departure");
		earliestDep.setBackground(FORE_COLOR);
		earliestDep.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				currentOptionsState = OptionsState.EARLY_DEP;
			}
		});

		// Fewest transfers radio button
		JRadioButton fewestTrans = new JRadioButton("Fewest Transfers");
		fewestTrans.setBackground(new Color(220,220,220));
		fewestTrans.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				currentOptionsState = OptionsState.FEW_TRANS;
			}
		});
		fewestTrans.setSelected(true);

		// Earliest arrival radio button
		JRadioButton earliestArr = new JRadioButton("Earliest Arrival");
		earliestArr.setBackground(new Color(210,210,210));
		earliestArr.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				currentOptionsState = OptionsState.EARLY_ARR;
			}
		});

		// Button group for advanced options
		ButtonGroup group = new ButtonGroup();
		group.add(fewestTrans);
		group.add(earliestDep);
		group.add(earliestArr);

		/**
		 * Checkboxes
		 **/
		//define all checkboxes
		orderedList = new JCheckBox("Ordered List");
		orderedList.setBackground(FORE_COLOR);
		orderedList.setSelected(true);

		JCheckBox depBox = new JCheckBox("Departure Time");
		depBox.setBackground(FORE_COLOR);
		depBox.setSelected(false);

		JCheckBox arrBox = new JCheckBox("Arrival Time");
		depBox.setBackground(FORE_COLOR);
		depBox.setSelected(false);

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

		JInternalFrame plannerControls = newFrame();
		plannerControls.setLayout(gridbag);
		gridbag.setConstraints(plannerControls, c);

		//////////////////////////////////////
		// Set up layout for RIGHT JFrame

		JInternalFrame right1 = newFrame(0,FORE_COLOR);
		right1.setMinimumSize(new Dimension(300,40));
		right1.setLayout(new FlowLayout());
		c.gridwidth = GridBagConstraints.REMAINDER; //end row
		c.weightx = 0.3;
		c.weighty = 0;
		gridbag.setConstraints(right1, c);
		//right.add(right1);

		JInternalFrame stopTableControls = newFrame(0, new Color(200,200,200));
		stopTableControls.setMinimumSize(new Dimension(300,40));
		stopTableControls.setLayout(new GridLayout(1,3));
		c.gridwidth = GridBagConstraints.REMAINDER; //end row
		c.weightx = 0.3;
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
		plannerControls.add(right4);

		JInternalFrame right5 = newFrame(0,new Color(200,200,200));
		//right5.setMinimumSize(new Dimension(300,40));
		right5.setLayout(new GridLayout(3,1,5,5));
		c.weighty = 1;
		c.gridwidth = GridBagConstraints.REMAINDER; //end row
		gridbag.setConstraints(right5, c);
		plannerControls.add(right5);

		JInternalFrame right6 = newFrame(0,new Color(200,200,200));
		//right6.setMinimumSize(new Dimension(300,60));
		right6.setLayout(new GridLayout(1,2,5,5));
		c.weighty = 1;
		c.gridwidth = GridBagConstraints.REMAINDER; //end row
		gridbag.setConstraints(right6, c);
		plannerControls.add(right6);

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

		plannerControls.add(right1);
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
		//left.add(topLeft);

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
		topMiddle.add(listRoute);
		topMiddle.add(testSystem);

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
		//topLeft.add(testSystem);
		// Add the map
		createMap();
		middleLeft.add(imageLabel);

		//add to right jframe
		createTable(bottomMiddle);
		right3.add(plannerControls);
		createStopsTable(right3);
		right1.add(orderedList);

		stopTableControls.add(addStop);
		stopTableControls.add(selectStop);
		stopTableControls.add(removeStop);
		right4.add(calcRoute);
		right5.add(earliestDep);
		right5.add(fewestTrans);
		right5.add(earliestArr);

		lastLeft.add(depBox);
		lastLeft.add(depFrame);
		depFrame.add(pickDep);
		lastRight.add(arrBox);
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
		table = new JTable(tableModel);
		cellRenderer = new DefaultTableCellRenderer() {
			private static final long serialVersionUID = 273298630375212139L;

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
			{
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				try {
					if (currentViewState != ViewState.VIEW_ROUTE 
							|| (currentViewState == ViewState.VIEW_ROUTE && column == 0)) {
						c.setBackground(table == Views.table ? rowColors.get(row) : stopRowColors.get(row));
						// Change font color
						c.setForeground(Color.white);
					}
					else {
						c.setBackground(Color.white);
						c.setForeground(Color.black);
					}
				}
				catch (IndexOutOfBoundsException e) {}

				return c;
			}
		};

		table.setDefaultRenderer(Object.class, cellRenderer);
		cellRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		table.setGridColor(Color.black);
		table.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
		table.setShowVerticalLines(true);
		table.setSize(700,700);
		JScrollPane scrollPane = new JScrollPane(table); 
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		//table.setFillsViewportHeight(true);

		container.add(table.getTableHeader(), BorderLayout.PAGE_START);
		container.add(scrollPane);
	}

	/**
	 * Updated the table based on the ViewState
	 * **/
	private static void updateTable() {
		// Clear list of row colors
		rowColors.clear();

		Object[][] data = new Object[0][];
		String[] cols = new String[0];

		switch(currentViewState) {

		// If the trains should be shown
		case VIEW_TRAINS:
			// Set columns
			cols = trainColumns;

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
					String nextString = train.getTrainPredictions().get(0).getName();



					int timeLeft = train.getTrainPredictions().get(0).getTime();

					if (currentViewState == ViewState.VIEW_TRAINS) {
						Object[] row = new Object[3];
						row[0] = nextString;
						row[1] = DateTime.minutesLeft(train, nextString) + ":" + DateTime.secondsLeft(train, nextString);
						row[2] = train.getTrainDestination();

						rowColors.add(lineToColor(lineName));

						// Add trains to data array for table
						data[counter] = row;
						counter++;
					}
				}
			}
			break;

			// If the stops should be shown
		case VIEW_STOPS:
			// Set columns
			cols = stopColumns;

			// Set data array to size of the stops list
			data = new Object[stops.size()][];

			// Iterate through stops
			for (Stop s : stops) {
				// Create row to hold stop info
				Object[] row = { s.stop_name, getNextClosestTrain(s.stop_name) };
				rowColors.add(lineToColor(s.Line));
				// Add row to data array
				data[stops.indexOf(s)] = row;
			}
			break;

			// If the route should be shown
		case VIEW_ROUTE:
			// Set columns
			cols = routeColumns;

			data = new Object[results.size()][];

			for (int s = 0; s < results.size(); s++) {
				int stopID = results.get(s);

				String stopName = TripPlanner.getStopNameByID(stopID);
				Object[] row = { stopName, (s < instructions.size() ? instructions.get(s) : "") };
				data[s] = row;
				rowColors.add(lineToColor(TripPlanner.getStopByID(stopID).Line));
			}
			break;

		}

		// Set columns to trains or stops
		if (tableModel != null) {
			tableModel.setDataVector(data, cols);
		}
	}

	/**
	 * Updates map based on lines and trains
	 * **/
	private static void updateMap() {
		Graphics2D g = (Graphics2D)map.getGraphics();
		createMap();
		drawTrainPath(pathList, PATH_COLOR);
		trainMap.clear();

		// Iterate through lines
		for (TrainLine line : trainLines) {
			// Get list of trains for each line
			LinkedList<Train> trains = line.getTrains();
			for (int t = 0; t < trains.size(); t++) {
				Train train = trains.get(t);
				String nextString = train.getTrainPredictions().get(0).getName();
				String destString = train.getTrainDestination();

				// Draw trains on map
				if(stopMap.containsKey(nextString) 
						&& stopMap.containsKey(destString)
						&& !nextString.equals(destString)) {
					createTrain(train);
				}
			}
		}

		// Dispose of graphics object
		g.dispose();
		// Repaint the imageLabel
		imageLabel.repaint();
	}

	/**
	 * Updates table and map based on lines and trains
	 * @author AG, CM and NF
	 **/
	public static void update() {
		updateTable();
		updateMap();
	}

	//returns the last stop
	//NF
	public static String getLastStop(Stop nextStop, Stop dest){
		String tempString = nextStop.stop_name;
		if (!nextStop.EndOfLine) {
			if(nextStop.stopID < dest.stopID){
				String temp2 =  TripPlanner.getStopNameByID(nextStop.stopID - 1);
				if(stops.contains(TripPlanner.getStopByName(tempString))){
					tempString = temp2;
				}
			}
			if(nextStop.stopID > dest.stopID){
				String temp2 = TripPlanner.getStopNameByID(nextStop.stopID + 1);
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
		return(y2*(1-mu)+y1*mu);
	}

	/**
	 * Makes the stops table
	 * @author NF and AG
	 * **/
	private void createStopsTable(JInternalFrame container) {
		stopsTableModel = new StopsTableModel() {
			private static final long serialVersionUID = 7594443031308851690L;

			@Override
			public void reorder(int from, int to) {
				for (int s = 0; s < selectedStops.size(); s++) {
					//if (s == )
				}
			}
		};
		stopsTableModel.addTableModelListener(this);
		stopsTable = new JTable(stopsTableModel);
		stopsTable.setDefaultRenderer(Object.class, cellRenderer);
		stopsTable.setGridColor(Color.black);
		stopsTable.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
		stopsTable.setShowVerticalLines(true);
		stopsTable.setDragEnabled(true);
		stopsTable.setDropMode(DropMode.INSERT_ROWS);
		stopsTable.setTransferHandler(new TableRowTransferHandler(stopsTable));

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
			Object[] row = { TripPlanner.getStopByID(selectedStops.get(s)).stop_name };

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
		Stop last = TripPlanner.getStopByName(lastStop);
		//System.out.println(a + " " + b + " " + lastStop);

		// Next stop position
		int nextX = stopMap.get(nextStop).x;
		int nextY = stopMap.get(nextStop).y;

		if(nextStop.equals(dest)){
			x = lastX;
			y = lastY;
		}
		// Previous stop position
		if(stopMap.containsKey(lastStop)) {
			lastX = stopMap.get(lastStop).x;	
			lastY = stopMap.get(lastStop).y;
		}
		//System.out.println(lastStop);

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
			/*System.out.println(t.getTrainID());
			System.out.println("Last X: "+lastX);
			System.out.println("Next X: "+nextX);
			System.out.println("Last Y: "+lastY);
			System.out.println("Next Y: "+nextY);*/
			//System.out.println("X: "+x);
			//System.out.println("Y: "+y);
			y = (int)(LinearInterpolate((float)lastY, (float)nextY, lerpTime)*100)/100; //(float)timeLeft/MAX_TIME			
			//System.out.println((int)(LinearInterpolate(0f, (float)nextX, lerpTime)*100)/100);		

			//	x = takeHalf(new Point(lastX,lastY), new Point(nextX,nextY)).x;
			//	y = takeHalf(new Point(lastX,lastY), new Point(nextX,nextY)).y;
		}
		TripPlanner.Direction trainDirection = TripPlanner.getDirection(lastStop, nextStop);
		if(trainDirection == TripPlanner.Direction.SOUTHBOUND){
			// SOUTH BOUND
			x += SB_X_OFFSET;
			y += SB_Y_OFFSET;
			g.setColor(CIRCLE_BACK_COLOR);
			g.fillOval(x+CIRCLE_DIFF, y+CIRCLE_DIFF, OUTER_CIRCLE_SIZE, OUTER_CIRCLE_SIZE);
			g.setColor(SB_CIRCLE_COLOR);
			g.fillOval(x, y, INNER_CIRCLE_SIZE, INNER_CIRCLE_SIZE);
		}
		else if(trainDirection == TripPlanner.Direction.NORTHBOUND){
			// NORTH BOUND
			x += NB_X_OFFSET;
			y += NB_Y_OFFSET;
			g.setColor(CIRCLE_BACK_COLOR);
			g.fillOval(x+CIRCLE_DIFF, y+CIRCLE_DIFF, OUTER_CIRCLE_SIZE, OUTER_CIRCLE_SIZE);
			g.setColor(NB_CIRCLE_COLOR);
			g.fillOval(x, y, INNER_CIRCLE_SIZE, INNER_CIRCLE_SIZE);
		}
		else{
			//STATIC
			x += NB_X_OFFSET;
			y += NB_Y_OFFSET;
			g.setColor(CIRCLE_BACK_COLOR);
			g.fillOval(x+CIRCLE_DIFF, y+CIRCLE_DIFF, OUTER_CIRCLE_SIZE, OUTER_CIRCLE_SIZE);
			g.setColor(new Color(255,255,0));
			g.fillOval(x, y, INNER_CIRCLE_SIZE, INNER_CIRCLE_SIZE);

		}

		trainMap.put(t,new Point(x,y));
		g.dispose();
		imageLabel.repaint();
	}

	// Creates instructions for a given path
	public static void createInstructions(LinkedList<Integer> pathList, int size, LinkedList<String> instructions) {
		LinkedList<Integer> path = new LinkedList<Integer>();
		path.addAll(pathList);

		if (!path.isEmpty()) {
			String firstStop = TripPlanner.getStopNameByID(path.get(0));
			String lastStop = TripPlanner.getStopNameByID(path.getLast());
			TripPlanner.Direction direction = TripPlanner.getDirection(firstStop, lastStop);
			Train nearestTrain = TripPlanner.getTrainAtStop(firstStop, direction, timeOffset);

			int sec = 0;
			int min = 0;

			if (nearestTrain != null) {
				sec = DateTime.secondsLeft(nearestTrain, firstStop);
				min = DateTime.minutesLeft(nearestTrain, firstStop);
			}

			if(nearestTrain == null && path.size() == 1) {
				String stopName = TripPlanner.getStopNameByID(path.get(0));
				instructions.add("Arrive at "
						+ lastStop + ".");
			}
			else if (nearestTrain == null) {
				instructions.add("There are no approaching trains.");
			}
			else if (path.size() == size) {
				instructions.add("Go to " + firstStop + 
						" and take the train that arrives in "+
						min+":"+sec+ " to " + lastStop + ".");
				path.pop();
				createInstructions(path, size, instructions);
			}
			else if (TrainGraph.isTransfer(nearestTrain, path)) {
				String stopName = TripPlanner.getStopNameByID(path.get(0));
				String line = TripPlanner.getStopByID(path.get(1)).Line;

				instructions.add("Get off at "+stopName+".");
				instructions.add("Transfer to the "+line+" line and take the train at "
						+DateTime.arrivesAt(nearestTrain, stopName)+".");
				path.pop();
				createInstructions(path, size, instructions);
			}
			else {
				instructions.add("");
				path.pop();
				createInstructions(path, size, instructions);
			}
		}
	}
	public static TripPlanner.Direction getClosestTrain(Train north, Train south, String stopName){
		if((north != null) && (south !=null)){
			int northTime = north.getPredictionByName(stopName).getTime();
			int southTime = south.getPredictionByName(stopName).getTime();
			if(northTime < southTime) return TripPlanner.Direction.NORTHBOUND;
			if(southTime < northTime) return TripPlanner.Direction.SOUTHBOUND;
			else{return TripPlanner.Direction.STATIC;}
		}
		else{
			return TripPlanner.Direction.STATIC;
		}
	}
	public static String getDirectionString(TripPlanner.Direction d){
		String direction = d.toString().toLowerCase();
		direction = direction.substring(0,1).toUpperCase() + direction.substring(1,direction.length());
		return direction;
	}
	public static String getNextClosestTrain(String stopName){
		Train north = TripPlanner.getTrainAtStop(stopName, TripPlanner.Direction.NORTHBOUND);
		Train south = TripPlanner.getTrainAtStop(stopName, TripPlanner.Direction.SOUTHBOUND);
		TripPlanner.Direction direction = getClosestTrain(north,south,stopName);
		return getNextTrain(stopName, direction);
	}
	public static String getNextTrain(String stopName, TripPlanner.Direction d){
		Train nearestTrain = TripPlanner.getTrainAtStop(stopName, d);	
		String direction = getDirectionString(d);
		if(nearestTrain != null) {
			int sec = nearestTrain.getPredictionByName(stopName).getTime();
			int min = sec/60;
			sec %= 60;
			String stopInfo = "";
			if (min > 0)
				stopInfo = "Train arrives in "+min+" minute(s) & "+sec+" second(s).";
			else if (min == 0 && sec > 0)
				stopInfo = "Train arrives in "+sec+" second(s).";
			else if (min < 0)
				stopInfo = "Train arrived "+-min+" minute(s) & "+-sec+" second(s) ago.";
			else if (sec <= 0)
				stopInfo = "Train arrived "+-sec+" second(s) ago.";
			return direction + " " + stopInfo;
		}
		else{
			return "There are no approaching trains.";
		}
		//return "Train will be at here in " + nearestTrain.getPredictionByName(stopName).getTime() + " seconds.";
	}
	/**
	 * Takes in a line name and returns the corresponding color
	 * @author AG
	 * **/
	private static Color lineToColor(String line) {
		if (line.equals("Orange")) {
			return ORANGE_LINE_COLOR;
		}
		else if (line.equals("Red")) {
			return RED_LINE_COLOR;
		}
		else if (line.equals("Blue")) {
			return BLUE_LINE_COLOR;
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

	// Draws a line from one point to another point
	// NF and AG
	public static void drawLineFromPoint(Point startPosn, Point endPosn, Color c){
		Graphics2D g = (Graphics2D)map.getGraphics();
		g.setStroke(new BasicStroke(10F));
		g.setColor(c);
		g.drawLine(startPosn.x, startPosn.y, endPosn.x, endPosn.y);
		imageLabel.repaint();
	}

	// Draws a train's path
	// NF and AG
	public static void drawTrainPath(LinkedList<Integer> goals, Color c){
		LinkedList<Stop> path = new LinkedList<Stop>();
		for (int r : results) {
			Stop s = TripPlanner.getStopByID(r);
			path.add(s);
		}
		Views.drawPath(path, c);
	}

	// Draws a line from each Stop to the next Stop in a list of stops
	// NF and AG
	public static void drawPath(LinkedList<Stop> path, Color color) {
		for (int s = 0; s < path.size()-1; s++) {
			Point startPos = stopMap.get(path.get(s).stop_name);
			Point endPos = stopMap.get(path.get(s+1).stop_name);
			drawLineFromPoint(startPos, endPos, color);
		}
		return ;
	}

	/*
	private Train getTrainAtPosn(Point posn) {
		return new Train();
	}

	private Stop getStopAtPosn(Point posn) {

	}*/

	/**
	 * Returns true if a train was clicked
	 * **/
	private boolean trainCollider(int mx, int my, int tx, int ty) {
		return collider(mx, my, tx, ty, TRAIN_THRESHOLD);
	}

	/**
	 * Returns true if a stop was clicked
	 * **/
	private boolean stopCollider(int mx, int my, int sx, int sy) {
		return collider(mx, my, sx, sy, STOP_THRESHOLD);
	}

	/**
	 * Determines if the point made by mx and my
	 * is within a certain threshold of the point
	 * made by ox and oy
	 * **/
	private boolean collider(int mx, int my, int ox, int oy, int threshold) {
		return (mx < ox + threshold) 
				&& (mx > ox - threshold)
				&& (my < oy + threshold) 
				&& (my > oy - threshold);
	}

	/**
	 * Adds a clicked stop to the selected stops list
	 * @author AG
	 * **/
	@Override
	public void mouseClicked(MouseEvent e) {
		//int x = e.getX();
		//int y = e.getY();
		int button = e.getButton();
		if (button == MouseEvent.BUTTON1) {
			/*
			// Can't get Orange Line Downtown Crossing
			// because Red is always on top of it
			for(String s : stopMap.keySet()){
				Point posn = stopMap.get(s);

				if (stopCollider(x, y, posn.x, posn.y)){
					selectedStops.add(s);
					stopRowColors.add(lineToColor(TripPlanner.getStopByName(s).Line));
					updateStopsTable();
				}
			}*/
		}
	}

	//sets the location of the map using the mouse coordinates
	//NF
	@Override
	public void mouseDragged(MouseEvent e) {
		imageLabel.setLocation(e.getX() - draggedAtX + imageLabel.getX(),
				e.getY() - draggedAtY + imageLabel.getY());		
	}

	//obtains the current mouse coordinates upon mousePress
	//NF
	@Override
	public void mousePressed(MouseEvent e) {

		if (e.getSource() == imageLabel) {
			draggedAtX = e.getX();
			draggedAtY = e.getY();
		}
	}

	// Update the map when the mouse is moved over it
	@Override
	public void mouseMoved(MouseEvent e) {
		updateMap();
	}

	/**
	 * 
	 * Stop Map
	 * 
	 * **/
	public static Point takeHalf(Point last, Point next){
		Point half = new Point();
		half.x = (next.x+last.x)/2;
		half.y = (next.y+last.y)/2;
		return half;

	}
	public static void pushHash(){
		stopMap.put("Oak Grove",new Point(799,77));
		stopMap.put("Malden Center",new Point(798,143));
		stopMap.put("Wellington",new Point(799,214));
		stopMap.put("Sullivan",new Point(799,298));
		stopMap.put("Community College",new Point(801,377));
		stopMap.put("North Station",new Point(813,471));
		stopMap.put("Chinatown",new Point(734,821));
		stopMap.put("Tufts Medical",new Point(680,876));
		stopMap.put("Back Bay",new Point(617,939));
		stopMap.put("Mass Ave",new Point(560,994));
		stopMap.put("Ruggles",new Point(504,1051));
		stopMap.put("Roxbury Crossing",new Point(449,1106));
		stopMap.put("Jackson Square",new Point(400,1157));
		stopMap.put("Stony Brook",new Point(345,1211));
		stopMap.put("Green Street",new Point(291,1264));
		stopMap.put("Forest Hills",new Point(239,1315));
		stopMap.put("Downtown Crossing",new Point(798,761));
		stopMap.put("State Street",new Point(870,681));
		stopMap.put("Haymarket",new Point(867,561));
		stopMap.put("Alewife",new Point(165,321));
		stopMap.put("Davis",new Point(253,321));
		stopMap.put("Porter Square",new Point(374,343));
		stopMap.put("Harvard Square",new Point(453,417));
		stopMap.put("Central Square",new Point(521,487));
		stopMap.put("Kendall/MIT",new Point(600,565));
		stopMap.put("Charles/MGH",new Point(672,638));
		stopMap.put("Park Street",new Point(728,691));
		stopMap.put("South Station",new Point(882,846));
		stopMap.put("Broadway",new Point(901,942));
		stopMap.put("Andrew",new Point(899,1021));
		stopMap.put("JFK/UMass",new Point(901,1104));
		stopMap.put("North Quincy",new Point(1075,1422));
		stopMap.put("Wollaston",new Point(1147,1496));
		stopMap.put("Quincy Center",new Point(1222,1569));
		stopMap.put("Quincy Adams",new Point(1292,1639));
		stopMap.put("Braintree",new Point(1314,1794));
		stopMap.put("Bowdoin",new Point(746,565));
		stopMap.put("Government Center",new Point(798,619));
		stopMap.put("Aquarium",new Point(936,621));
		stopMap.put("Maverick",new Point(1052,505));
		stopMap.put("Airport",new Point(1111,448));
		stopMap.put("Wood Island",new Point(1161,399));
		stopMap.put("Orient Heights",new Point(1214,345));
		stopMap.put("Suffolk Downs",new Point(1267,292));
		stopMap.put("Beachmont",new Point(1320,238));
		stopMap.put("Revere Beach",new Point(1371,186));
		stopMap.put("Wonderland",new Point(1435,124));
		stopMap.put("Savin Hill",new Point(847,1159));
		stopMap.put("Fields Corner",new Point(797,1230));
		stopMap.put("Shawmut",new Point(799,1282));
		stopMap.put("Ashmont",new Point(799,1342));
	}

	/**
	 * Unimplemented required methods
	 * **/
	@Override
	public void mouseExited(MouseEvent arg0) {
	}
	@Override
	public void mouseEntered(MouseEvent e) {
	}
	@Override
	public void mouseReleased(MouseEvent arg0) {
	}
	@Override
	public void tableChanged(TableModelEvent e) {
	}
}