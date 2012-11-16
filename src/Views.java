import java.awt.*;
import javax.swing.*; 
import java.io.File;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import java.math.*;
import javax.imageio.ImageIO;
import javax.swing.event.*;
import javax.swing.table.*;

//Create a simple GUI window
public class Views implements MouseListener, TableModelListener {
	public static final String IMAGE_PATH = "mbta.bmp";
	public static JLabel imageLabel;
	public static int scaleX = 400;
	public static int scaleY = 500;
	public static final int SCALE_TYPE = 3;
	public static LinkedList<TrainLine> trainLines;
	public static JTable table;
	public static DefaultTableModel tableModel;
	public enum viewState {
		VIEWING_TRAINS,
		VIEWING_STOPS,
		VIEWING_ROUTE
	}

	public Views(LinkedList<TrainLine> lines) {
		setLines(lines);
		createWindow(trainLines);
	}

	public void setLines(LinkedList<TrainLine> lines) {
		trainLines = lines;
		updateTableData();
	}
	public void createWindow(LinkedList<TrainLine> lines) {
		//Create and set up the window.
		JFrame frame = new JFrame("MBTA Trip Planner"); 
		frame.setBackground(Color.DARK_GRAY);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 

		//define all labels
		imageLabel = new JLabel();
		imageLabel.addMouseListener(this);
		JLabel textLabel = new JLabel("This Area will have stuff in it.");

		//define all buttons
		JButton listTrains = new JButton("List Trains");
		JButton listStops = new JButton("List Stops");
		JButton testSystem = new JButton("Test System");
		JButton planRoute = new JButton("Plan Route");
		JButton addStop = new JButton("Add Stop");
		JButton calcRoute = new JButton("Calculate Route");

		//define all checkboxes
		JCheckBox orderedList = new JCheckBox("Ordered List");
		JCheckBox drawTrains = new JCheckBox("Show Trains on Map");

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
		left.setLayout(new FlowLayout());
		//middle.setLayout(new FlowLayout());
		right.setLayout(new GridLayout(3,1,5,5));

		//////////////////////////////////////
		//set up layout for rightmost jframe
		JInternalFrame topRight = newFrame();
		topRight.setLayout(new FlowLayout());
		right.add(topRight);

		JInternalFrame topMiddle = newFrame();
		topMiddle.setLayout(new FlowLayout());
		right.add(topMiddle);

		JInternalFrame bottomRight = newFrame();
		bottomRight.setLayout(new FlowLayout());
		right.add(bottomRight);
		///////////////////////////////////////

		//add to left jframe
		left.add(testSystem);
		createMap(scaleX, scaleY, SCALE_TYPE);
		left.add(imageLabel);
		left.add(drawTrains);

		//add to middle jframe
		//middle.add(listTrains);
		//middle.add(listStops);		 
		createTable(middle, lines);

		//add to right jframe
		topRight.add(planRoute);
		topRight.add(textLabel, BorderLayout.NORTH); 		 
		topRight.add(orderedList);

		//add internal jframes in order to fill the grid layout
		frame.add(left);
		frame.add(middle);
		frame.add(right);

		//pack the frames neatly		
		//frame.pack(); 
		//middle.pack();
		frame.setSize(1200,600);
	}

	public static void createMap(int width, int height, int type) {
		createImage(imageLabel, IMAGE_PATH, width, height, type);
	}

	//loads an image from url to a jlabel
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
	private static JInternalFrame newFrame(){
		JInternalFrame frame = new JInternalFrame("",false,false,false,false);
		frame.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
		javax.swing.plaf.InternalFrameUI ifu= frame.getUI(); 
		((javax.swing.plaf.basic.BasicInternalFrameUI)ifu).setNorthPane(null);
		frame.setVisible(true);
		frame.moveToFront();
		frame.setBackground(Color.LIGHT_GRAY);
		return frame;
	}

	//takes an internal jframe and create a table in it
	private void createTable(JInternalFrame container, LinkedList<TrainLine> lines){
		String[] columnNames = {"ID", "Line", "Location", "Destination"};
		Object[][] data = updateTableData();
		tableModel = new DefaultTableModel(data, columnNames);
		tableModel.addTableModelListener(this);
		table = new JTable(/*data, columnNames*/tableModel);
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
	
	
	// Returns updated data for the table based on lines
	//  CM
	public static Object[][] updateTableData() {
		int counter = 0;
		for (TrainLine l : trainLines) {
			for (Train t : l.getTrains()) {
				counter++;
			}
		}
		Object[][] data = new Object[counter][];
		counter = 0;
		for (TrainLine line : trainLines) {
			String lineName = line.getLine();
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
						posString, trains.get(t).getTrainDestination() };
				data[counter] = row;
				counter++;
			}
		}
		return data;
	}

	
	@Override
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

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void tableChanged(TableModelEvent e) {
		tableModel.fireTableDataChanged();
	}
}