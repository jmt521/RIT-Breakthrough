/**
* Andrew DiStasi, DJ Crocker, Josh Vickerson, and Jonathan Theismann
* 4/7/13
* This is a class to create a GUI to display the information relevant to the server
* This class extends JFrame
* This class implements ActionListener
*/
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.Vector;

public class ServerGUI extends JFrame implements ActionListener
{
	//attributes for the class
	private JTextArea loggedOn = new JTextArea(30,13); //left
	private JTextArea gamesPlayed = new JTextArea(30,13); //right
	private JTextArea serverMessages = new JTextArea(30,30); //middle
	private JLabel serverStatus = new JLabel("The server is: ");
	JMenuItem exitItem;
	
	/**
		*Default Constructor to create the GUI and attach listeners.
	*/
	public ServerGUI()
	{
		super("Server GUI");
		setLocation(100,150);
		setSize(650,600);
		
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		exitItem = new JMenuItem("Exit");
		fileMenu.add(exitItem);
		menuBar.add(fileMenu);
		setJMenuBar(menuBar);
		
		JPanel centerNorthPanel = new JPanel();
		JPanel northPanel = new JPanel(new BorderLayout());
		JPanel southPanel = new JPanel();
		
		JLabel serverMessagesLabel = new JLabel("Server Messages/Chat");
		JLabel gamesPlayedLabel = new JLabel("Games Being Played     ");
		JLabel loggedOnLabel = new JLabel("     Users Logged On");
		
		JScrollPane eastPane = new JScrollPane(gamesPlayed, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		JScrollPane centerPane = new JScrollPane(serverMessages, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		JScrollPane westPane = new JScrollPane(loggedOn, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		loggedOn.setLineWrap(true);
		loggedOn.setWrapStyleWord(true);
		loggedOn.setEditable(false);
		gamesPlayed.setLineWrap(true);
		gamesPlayed.setWrapStyleWord(true);
		gamesPlayed.setEditable(false);
		serverMessages.setLineWrap(true);
		serverMessages.setWrapStyleWord(true);
		serverMessages.setEditable(false);
		
		centerNorthPanel.add(serverMessagesLabel);
		
		northPanel.add(loggedOnLabel, BorderLayout.WEST);
		northPanel.add(centerNorthPanel, BorderLayout.CENTER);
		northPanel.add(gamesPlayedLabel, BorderLayout.EAST);
		
		serverMessagesLabel.setHorizontalTextPosition(JLabel.CENTER);
		
		southPanel.add(serverStatus);
				
		add(eastPane, BorderLayout.EAST);
		add(centerPane, BorderLayout.CENTER);
		add(westPane, BorderLayout.WEST);
		add(northPanel, BorderLayout.NORTH);
		add(southPanel, BorderLayout.SOUTH);
		
		exitItem.addActionListener(this);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	/**
	* ActionListener for the GUI that allows for functionality.
	*/
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == exitItem)
		{
			System.exit(0);
		}
	}
	
	/**
	* A method to update the usernames being displayed
	* @param clients A vector containing the clients playing games
	*/
	public void updateUsers(Vector<ClientThread> clients)
	{
		loggedOn.setText("");
		
		for(ClientThread user : clients)
		{
			loggedOn.append(user.getUsername() + "\n");
		}
	}
	
	/**
	* A method to update the server messages being displayed
	* @param message A String containing the server message
	*/
	public void updateMessages(String message)
	{
		serverMessages.append(message + "\n");
		serverMessages.setCaretPosition(serverMessages.getText().length());
	}
	
	/**
	* A method to update the display of what games are being played
	* @param games A vector containing the games being played
	*/
	public void updateGames(Vector<GameInstance> games)
	{
		gamesPlayed.setText("");
		
		for(GameInstance game : games)
		{
			gamesPlayed.append(game/**.toString()*/ + "\n");  //DON'T FORGET TO COME IN HERE AND REMOVE THE COMMENTS SO THE TO STRING METHOD CAN BE CALLED
		}
	}
	
	/**
	* A method to update the server status
	* @param status The current status of the server
	*/
	public void updateStatus(String status)
	{
		serverStatus.setText("The server is " + status);
	}
}