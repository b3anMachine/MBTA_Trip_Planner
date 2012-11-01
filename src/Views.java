 import java.awt.*;
 import javax.swing.*; 
 import java.io.File;
 import java.awt.Image;

 import javax.imageio.ImageIO;
 
//Create a simple GUI window
 public class Views {
	 public static void createWindow() {
		 //Create and set up the window.
		 JFrame frame = new JFrame("MBTA Trip Planner"); 
		 frame.setBackground(Color.DARK_GRAY);
		 frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		 
		 //define all labels
		 JLabel imageLabel = new JLabel(); 
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
		 middle.setLayout(new FlowLayout());
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
		 createImage(imageLabel, "src/mbta_cropped.bmp");
		 left.add(imageLabel);
		 left.add(drawTrains);
		 
		 //add to middle jframe
		 middle.add(listTrains);
		 middle.add(listStops);		 
		 createTable(middle);
		 
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
		 
		 frame.setSize(1100,600);
	 }
	 
	 //loads an image from url to a jlabel
	 public static void createImage(JLabel label, String url){
		 try 
		 {
		   // Read from a file
		   File FileToRead = new File(url);
		   //Recognize file as image
		   Image Picture = ImageIO.read(FileToRead);
		   
		   Image p2 = Picture.getScaledInstance(380, 300,3);
		   
		   ImageIcon icon = new ImageIcon(p2);
		   
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
	 private static void createTable(JInternalFrame container){
		 String[] columnNames = {"ID",
                 "Destination",
                 "Line",
                 "Location",
                 "Predictions"};
		 Object[][] data = {
				{ "Joe", "Brown", "Pool", new Integer(10), new Boolean(false) },
				{ "Joe", "Brown", "Pool", new Integer(10), new Boolean(false) },
				{ "Joe", "Brown", "Pool", new Integer(10), new Boolean(false) },
				{ "Joe", "Brown", "Pool", new Integer(10), new Boolean(false) },
				{ "Joe", "Brown", "Pool", new Integer(10), new Boolean(false) },
				{ "Joe", "Brown", "Pool", new Integer(10), new Boolean(false) },
				{ "Joe", "Brown", "Pool", new Integer(10), new Boolean(false) },
				{ "Joe", "Brown", "Pool", new Integer(10), new Boolean(false) },
				{ "Joe", "Brown", "Pool", new Integer(10), new Boolean(false) },
				{ "Joe", "Brown", "Pool", new Integer(10), new Boolean(false) },
				{ "Joe", "Brown", "Pool", new Integer(10), new Boolean(false) },
				{ "Joe", "Brown", "Pool", new Integer(10), new Boolean(false) },
				{ "Joe", "Brown", "Pool", new Integer(10), new Boolean(false) },
				{ "Joe", "Brown", "Pool", new Integer(10), new Boolean(false) },
				{ "Joe", "Brown", "Pool", new Integer(10), new Boolean(false) },
				{ "Joe", "Brown", "Pool", new Integer(10), new Boolean(false) },
				{ "Joe", "Brown", "Pool", new Integer(10), new Boolean(false) },
				{ "Joe", "Brown", "Pool", new Integer(10), new Boolean(false) }
				};
		 JTable table = new JTable(data, columnNames);	
		 table.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
		 table.setShowVerticalLines(true);
		
		 //JScrollPane scrollPane = new JScrollPane(table); 
	     //scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		 //table.setFillsViewportHeight(true);
	
		 container.add(table.getTableHeader(), BorderLayout.PAGE_START);
		 container.add(table);		    
	 }	 
 } 