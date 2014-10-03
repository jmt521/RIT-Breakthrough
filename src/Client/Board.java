/**
* Andrew DiStasi, DJ Crocker, Josh Vickerson, and Jonathan Theismann
* 4/7/13
* This is a class to create the gameboard used in the game
* This class extends JFrame
* This class implements BreakthroughConstants
*/

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.util.Vector;
import javax.swing.event.*;
import javax.imageio.ImageIO;
import java.io.*;
import java.net.*;

public class Board extends JFrame implements BreakthroughConstants
{
	//attributes for the class
	private Tile[][] gameBoard = new Tile[8][8];
	private JLabel playerTurn, player1, player2;
	private JTextArea lobbyChat = new JTextArea(15,25);
	private JTextArea gameChat = new JTextArea(15,25);
	private JTextArea connectedUsers = new JTextArea(15,25);
	private JTextArea submission = new JTextArea("Enter your message here",5,25);
	JMenuItem musicItem, soundItem, helpItem, aboutItem, connectItem, disconnectItem, joinItem;
	private JTabbedPane chatWindows;
	private boolean isConnected = false;
	private boolean isGame = false;
	private int playerId = 0;
	private ServerConnection connection = null;
	private int turn = 0;	
	private NotifyPlayer notify = null;

	//Image constants that will be used to build the game board
	static ImageIcon TIGER_R, TIGER_L;

	
	/**
	* Default Constructor to create the board and its components
	*/	
	public Board()
	{
		//Create the Frame
		super("Breakthrough");

		//Load Images from JAR file
		try
		{
			URL tiger1In = BreakthroughConstants.class.getResource("tiger_r.png");
			Image tiger_r = ImageIO.read(tiger1In);
			TIGER_R = new ImageIcon(tiger_r);

			URL tiger2In = BreakthroughConstants.class.getResource("tiger_l.png");
			Image tiger_l = ImageIO.read(tiger2In);
			TIGER_L = new ImageIcon(tiger_l);

			//set icon of the frame
			setIconImage(tiger_r);
		}
		catch(Exception e)
		{

		}
		
		//Create the menuBar
		JMenuBar menubar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenu optionsMenu = new JMenu("Options");
		JMenu helpMenu = new JMenu("Help");
		connectItem = new JMenuItem("Connect");
		disconnectItem = new JMenuItem("Disconnect");
		disconnectItem.setEnabled(false);
		joinItem = new JMenuItem("Join Queue");
		JMenuItem exitItem = new JMenuItem("Exit");
		helpItem = new JMenuItem("Help");
		aboutItem = new JMenuItem("About");
		
		//checks to see if sound effects are enabled when creating a new board
		if(TigerRoar.soundFx == true){
			soundItem   = new JMenuItem("Turn Off Sound Effects");
		}
		else{
			soundItem   = new JMenuItem("Turn On Sound Effects");
		}
		
		//checks to see if music is enabled when creating a new board
		if(PlayMusic.musicOn == true){
			musicItem   = new JMenuItem("Turn Off Music");
		}
		else{
			musicItem   = new JMenuItem("Turn On Music");
		}
		
		joinItem.setEnabled(false);
		
		//add all menus and menuitems
		fileMenu.add(connectItem);
		fileMenu.add(disconnectItem);
		fileMenu.addSeparator();
		fileMenu.add(joinItem);
		fileMenu.add(exitItem);
		optionsMenu.add(soundItem);
		optionsMenu.add(musicItem);
		helpMenu.add(helpItem);
		helpMenu.add(aboutItem);
		menubar.add(fileMenu);
		menubar.add(optionsMenu);
		menubar.add(helpMenu);
		setJMenuBar(menubar);
		
		//Create the JPanels
		JPanel boardPanel = new JPanel(new GridLayout(8,8));
		JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		northPanel = buildGameHeader();
		JPanel eastPanel = new JPanel(new BorderLayout(5,5));
		
		//format the textAreas used in the JTabbedPane
		lobbyChat.setLineWrap(true);
		lobbyChat.setWrapStyleWord(true);
		lobbyChat.setEditable(false);
		gameChat.setLineWrap(true);
		gameChat.setWrapStyleWord(true);
		gameChat.setEditable(false);
		connectedUsers.setLineWrap(true);
		connectedUsers.setWrapStyleWord(true);
		connectedUsers.setEditable(false);
		submission.setLineWrap(true);
		submission.setWrapStyleWord(true);
		
		//Create JScrollPanes containing the JTextAreas
		JScrollPane lobbyPane = new JScrollPane(lobbyChat, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		JScrollPane gamePane = new JScrollPane(gameChat, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		JScrollPane connectedPane = new JScrollPane(connectedUsers, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		JScrollPane submissionPane = new JScrollPane(submission, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		//Create and Format the JTabbedPane
		UIManager.put("TabbedPane.selected", ORANGE);
		chatWindows = new JTabbedPane();
		lobbyChat.setBackground(LIGHT_CREAM);
		gameChat.setBackground(LIGHT_CREAM);
		connectedUsers.setBackground(LIGHT_CREAM);
		submission.setBackground(LIGHT_CREAM);
		chatWindows.setBackground(DARK_CREAM);
		eastPanel.setBackground(BROWN);
		chatWindows.addTab("Lobby Chat", lobbyPane);
		chatWindows.addTab("Game Chat", gamePane);
		chatWindows.addTab("Players", connectedPane);
		chatWindows.addChangeListener(new ChangeListener(){ //An anonymous inner class to set the submission field to false
			public void stateChanged(ChangeEvent ce)
			{
				if(chatWindows.getSelectedIndex() == 2) //If players tab is currently open, set the submission field to uneditable and unfocusable
				{
					submission.setEditable(false);
					submission.setFocusable(false);
				}
				else if(chatWindows.getSelectedIndex() == 0) //If players tab is not currently open, set the submission field to editable and focusable
				{
					submission.setEditable(true);
					submission.setFocusable(true);
				}
				else if(chatWindows.getSelectedIndex() == 1)
				{
					submission.setEditable(true);
					submission.setFocusable(true);
				}
			}
		});
		
		submission.addKeyListener(new KeyAdapter() {
			/**
				*Send message to appropriate chat when enter is pressed
				*@param ke the key pressed
			*/
			public void keyPressed(KeyEvent ke)
			{
				if(ke.getKeyCode() == KeyEvent.VK_ENTER)
				{
					if(submission.getText().trim().length() != 0)
					{
						if(chatWindows.getSelectedIndex() == 0 && isConnected)
						{
							connection.sendLobbyChat(submission.getText().trim());
							submission.setText("");
							//System.out.println("Sending lobby chat");
						}
						else if(chatWindows.getSelectedIndex() == 1 && isGame)
						{
							connection.sendGameChat(submission.getText().trim());
							submission.setText("");
							//System.out.println("Sending game chat");
						}
					}
				}
			}
			/**
				*Clear the text area when enter is released
				*@param ke the key that was released
			*/
			public void keyReleased(KeyEvent ke) {
		        if(ke.getKeyCode() == KeyEvent.VK_ENTER)
				 	{
		            submission.setText("");
					}
			}
		});
		
		submission.addFocusListener(new FocusAdapter() {
			/**
				*Clear the default text from the textarea
				*@param fe the event generated when the textfield gains focus
			*/
			public void focusGained(FocusEvent fe)
			{
				if(submission.getText().equals("Enter your message here"))
				{
					submission.setText("");
				}
			}
			
			/**
			*Replace the default text to the textarea
			*@param fe the event generated when the textfield loses focus
			*/
			public void focusLost(FocusEvent fe)
			{
				if(submission.getText().equals(""))
				{
					submission.setText("Enter your message here");
				}
			}
		});
		
		//Add the JTabbedPane and submission JScrollPane to a panel
		eastPanel.add(chatWindows, BorderLayout.CENTER);
		eastPanel.add(submissionPane, BorderLayout.SOUTH);
		
		//Create an actionListener for the tiles
		TileListener tileListener = new TileListener(playerTurn, this, gameBoard);
		
		//Add the tiles to the 2D array representing the board and add ActionListeners to each tile
		for(int row=0; row<8; row++) //Adds tiles to each row position
		{
			for(int col=0; col<8; col++) //adds tiles to each column position
			{
				Tile tile = new Tile(row,col);
				gameBoard[row][col] = tile;
				tile.addMouseListener(tileListener);
				boardPanel.add(tile);
			}
		}
		
		//Add the panels to the board
		add(boardPanel, BorderLayout.CENTER);	
		add(northPanel, BorderLayout.NORTH);
		add(eastPanel, BorderLayout.EAST);
		
		//Create and add ActionListeners for the board
		BoardListener boardListener = new BoardListener(this, gameBoard, tileListener);
		connectItem.addActionListener(boardListener);
		disconnectItem.addActionListener(boardListener);
		exitItem.addActionListener(boardListener);
		joinItem.addActionListener(boardListener);
		musicItem.addActionListener(boardListener);
		soundItem.addActionListener(boardListener);
		helpItem.addActionListener(boardListener);
		aboutItem.addActionListener(boardListener);

		//Format the board
		pack();
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setVisible(true);
	}
	
	/**
	* A method to create the "Header" for the game board
	* @return The 'header' panel being created
	*/
	private JPanel buildGameHeader() {
    	//Make text labels
    	player1 = new JLabel("Player 1");
    	player2 = new JLabel("Player 2");
		JLabel chatLabel = new JLabel("Chat");
    	playerTurn     = new JLabel("Breakthrough");
    	player1.setForeground(BROWN);
    	player2.setForeground(BROWN);
    	playerTurn.setForeground(Color.WHITE);
		chatLabel.setForeground(BROWN);
    	player1.setFont(new Font("Arial", Font.BOLD, 14));
    	player2.setFont(new Font("Arial", Font.BOLD, 14));
    	playerTurn.setFont(new Font("Arial", Font.BOLD, 18));
		chatLabel.setFont(new Font("Arial", Font.BOLD, 18));
    	JPanel display = new JPanel(new FlowLayout(FlowLayout.LEFT));
    	
    	//Create a panel and add everything to the panel
    	display.setBackground(ORANGE);
    	display.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		display.add(buildSpacerPanel(40,20));
    	display.add(player1);
    	display.add(buildSpacerPanel(125,20));
    	display.add(playerTurn);
    	display.add(buildSpacerPanel(120,20));
    	display.add(player2);
		display.add(buildSpacerPanel(175, 20));
		display.add(chatLabel);
    	return display;
    }
	 
	 /**
	 * A method to create a JPanel to allow for formatting
	 * @param width An integer for the width of the JPanel
	 * @param length An integer for the length of the JPanel
	 * @return spacerPanel The JPanel that is being used
	 */
	 private JPanel buildSpacerPanel(int width, int height) {
		JPanel spacerPanel = new JPanel();
		spacerPanel.setPreferredSize(new Dimension(width, height));
		spacerPanel.setBackground(ORANGE);
		return spacerPanel;
	}
	
	/**
	* A method to set the pieces on the game board
	* @param newBoard A 2D array of integers used to create the board
	*/
	public void setBoard(int[][] newBoard)
	{
		for(int row=0;row<gameBoard.length;row++)
		{
			for(int col=0;col<gameBoard[row].length;col++)
			{
				gameBoard[row][col].setPiece(newBoard[row][col]);
			}
		}
	}
	
	/**
	* A method to set the user names for player 1 and player 2
	* @param newID An integer representing the user ID
	* @param newName A String containing the Username
	*/
	public void setPlayerName(int newID, String newName)
	{
		if(newID == P1) //If the user is player 1, set the name for player 1
		{
			player1.setText(String.format("%10s",newName));
		}
		else if(newID == P2) //If the user is player 2, set the name for player 2
		{
			player2.setText(String.format("%10s",newName));
		}
	}
	
	/**
	* A method to allow the server to show messages to the user
	* @param newMessage The message being displayed
	*/
	public void displayMessage(String newMessage)
	{
		JOptionPane.showMessageDialog(this,newMessage);
	}
	
	/**
	* A method to allow the server to show errors to the user
	* @param newError The error message being displayed
	*/
	public void displayError(String newError)
	{
		JOptionPane.showMessageDialog(this,newError,"Error",JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	* A method to update the list of users currently logged on
	* @param newUserList A vector of the players currently logged on
	*/
	public void updateUserList(Vector<String> newUserList)
	{
		connectedUsers.setText("");
		for(String s : newUserList) //For each user in the user list, append the name to the appropriate textArea
		{
			connectedUsers.append(s + "\n");
		}
	}
	
	/**
	* A method to allow the user to send chat within the game
	* @param userName A string containing the player's username
	* @param newMessage A string containing the message the player is sending
	*/
	public void addGameChat(String userName, String newMessage)
	{
		gameChat.append(userName + ": " + newMessage + "\n");
		gameChat.setCaretPosition(gameChat.getText().length());
	}
	
	/**
	* A method to allow the user to send chat in the "lobby"
	* @param userName A string containing the player's username
	* @param newMessage A string containing the message the player is sending
	*/
	public void addLobbyChat(String userName, String newMessage)
	{
		lobbyChat.append(userName + ": " + newMessage + "\n");
		lobbyChat.setCaretPosition(lobbyChat.getText().length());
	}
	
	/**
	* A method to set the connected status of the board
	* @param _connected The current status of the board
	*/
	public void setConnected(boolean _connected)
	{
		isConnected = _connected;
		
		//set menu items based on whether connecting or disconnecting
		if(_connected == true)
		{
			connectItem.setEnabled(false);
			disconnectItem.setEnabled(true);
			joinItem.setEnabled(true);
		}
		else if(_connected == false)
		{
			connectItem.setEnabled(true);
			disconnectItem.setEnabled(false);
			connectedUsers.setText("");
			setBoard(STARTING_BOARD);
			joinItem.setEnabled(false);
		}

		try
		{
			notify.stopNotifying();
		}
		catch(NullPointerException npe){}
	}
	
	/**
	* A method to return the connected status of the board
	* @return isConnected The connected status of the board
	*/
	public boolean isConnected()
	{
		return isConnected;
	}
	
	/**
		*Accessor for the connection to the server
		*@return the connection to the server
	*/
	public ServerConnection getConnection()
	{
		return connection;
	}
	
	/**
		*Accessor for whether the client is in a game
		*@return whether the client is in a game
	*/
	public boolean isGame()
	{
		return isGame;
	}
	
	/**
		*Set whether the client is in a game
		*@param _game whether the client is in a game
	*/
	public void setGame(boolean _game)
	{
		isGame = _game;
		setBoard(STARTING_BOARD);

		//enable items and labels based on player numbers and names
		if(_game == true)
		{
			joinItem.setEnabled(false);
			JOptionPane.showMessageDialog(null,"You're now in a game!");
		}
		else if(_game == false)
		{
			joinItem.setEnabled(true);
			player1.setText("Player 1");
			player2.setText("Player 2");
			playerTurn.setText("Breakthrough");
			gameChat.setText("");

			try
			{
				notify.stopNotifying();
			}
			catch(NullPointerException npe){}
		}
	}
	
	/**
		*Accessor for player id (which player the client is)
		*@return the client's player id
	*/
	public int getPlayer()
	{
		return playerId;
	}
	
	/**
		*Accessor for opponent's player id (which player the opponent is)
		*@return the opponent's player id
	*/
	public int getOpponent()
	{
		if(playerId == P1)
		{
			return P2;
		}
		else if(playerId == P2)
		{
			return P1;
		}
		else
		{
			return 0;
		}
	}
	
	/**
		*Sets the client's player id
		*@param _playerId the client's player id
	*/
	public void setPlayer(int _playerId)
	{
		playerId = _playerId;
	}
	
	/**
		*Sets the client's connection to the server
		*@param newConnecion the new connection to the server
	*/
	public void setConnection(ServerConnection newConnection)
	{
		connection = newConnection;
	}
	
	/**
		*Set the game's turn
		*@param _newTurn the current player's turn
		*@param _playerName the player whose turn it is
	*/
	public void setTurn(int _newTurn, String _playerName)
	{
		turn = _newTurn;

		if(_newTurn == 0)
		{
			playerTurn.setText(_playerName);
		}
		else
		{
			playerTurn.setText(String.format("%8s%7s",_playerName,"'s Turn"));
		}

		
		if(_newTurn == playerId && playerId != 0)
		{
			try
			{
				//we are never ever EVER running this twice
				if(notify == null || !notify.isRunning())
				{
					System.out.println("STARTING NOTIFY: Player: " + _newTurn + "PlayerID: " + playerId);
					notify = new NotifyPlayer(this, playerTurn);
					notify.start();
				}
			}
			catch(NullPointerException npe){}
		}
		else
		{
			try
			{
				System.out.println("STOPPING NOTIFY");
				notify.stopNotifying();
			}
			catch(NullPointerException npe){}
		}
	}
	
	/**
		*Gets the current turn
		*@return the current turn
	*/
	public int getTurn()
	{
		return turn;
	}

	/**
		*Gets the player notifier
		*@return the player notifier
	*/
	public NotifyPlayer getNotifyPlayer()
	{
		return notify;
	}
	
	/**
		*Gets the label that contains the current player's name
		*@return the current player's label
	*/
	public JLabel getTurnLabel()
	{
		return playerTurn;
	}
}
