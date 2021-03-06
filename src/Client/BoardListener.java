/**
* Andrew DiStasi, DJ Crocker, Josh Vickerson and Jonathan Theismann
* 4/7/13
* This is a class to add functionality to the gameboard
* This class implements ActionListener and Breakthrough Constants
*/

import java.io.*;
import java.net.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class BoardListener implements ActionListener, BreakthroughConstants
{
	//attributes for the class
	private Board gameBoard;
	private Tile board[][];
	private TileListener tileListener;
	
	
	/**
	* Parameterized constructor for the class
	* @param newGameBoard an instance of the 'Board' class
	* @param newBoard[][] A 2D-array of the 'Tile' class
	* @param _tileListener An instance of the 'TileListener' class
	*/
	public BoardListener(Board newGameBoard, Tile newBoard[][], TileListener _tileListener)
	{
		gameBoard = newGameBoard;
		board = newBoard;
		tileListener = _tileListener;
	}
	
	/**
	* The actionPerformed method to provide functionality to the reset and exit menu items
	* @param e the ActionEvent generated by the GUI
	*/
	public void actionPerformed(ActionEvent e)
	{
		if(e.getActionCommand().equals("Join Queue")) //If the user presses Join Que
		{
			gameBoard.getConnection().joinQueue();
		}
		else if(e.getActionCommand().equals("Connect")) //If the user selects the connect menu option
		{
			//Create a JOptionPane with 3 input forms and an OK/Cancel option
			JOptionPane connectPane = new JOptionPane();
			
			JPanel labelPanel = new JPanel(new GridLayout(3,1));
			JPanel inputPanel = new JPanel(new GridLayout(3,1));
			JPanel wholePanel = new JPanel(new BorderLayout());
			JLabel nameLabel = new JLabel("Username: ");
			JLabel addressLabel = new JLabel("Server Address: ");
			JLabel portLabel = new JLabel("Port: ");
			JTextField nameField = new JTextField(20);
			JTextField addressField = new JTextField(20);
			JTextField portField = new JTextField(("" + DEFAULT_PORT), 10);
			labelPanel.add(nameLabel);
			labelPanel.add(addressLabel);
			labelPanel.add(portLabel);
			inputPanel.add(nameField);
			inputPanel.add(addressField);
			inputPanel.add(portField);
			wholePanel.add(labelPanel, BorderLayout.WEST);
			wholePanel.add(inputPanel, BorderLayout.CENTER);
			
			// Options for our window
			String[] options = {"OK", "Cancel"};
			
			int result = JOptionPane.showOptionDialog(gameBoard, wholePanel, 
					"Connect to a Server", JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE, Board.TIGER_R,
					options, nameField);
			
			if(result == JOptionPane.OK_OPTION) //If the user presses OK
			{
				try
				{
					int port = Integer.parseInt(portField.getText()); //parse the Port Number in as an int
					
					if(port > 0 && port <65535) //If it is a valid port number
					{
						String serverAddress = addressField.getText();
						String username = nameField.getText();
						try
						{
							Socket s = new Socket(serverAddress,port);
							ServerConnection newConnection = new ServerConnection(s,gameBoard,username);
							newConnection.start();
							gameBoard.setConnection(newConnection);
						}
						catch(IOException ioe)
						{
							gameBoard.setConnection(null);
							gameBoard.setConnected(false);
							JOptionPane.showMessageDialog(gameBoard, "Connection failed", "ERROR", JOptionPane.ERROR_MESSAGE);
						}
					}
					else //If it is not a valid port number, display an error message
					{
						JOptionPane errorPane = new JOptionPane();
						errorPane.showMessageDialog(gameBoard, "Please input a valid port number", "ERROR", JOptionPane.ERROR_MESSAGE);
					}
				}
				catch(NumberFormatException nfe) //If it is not a number, display an error message
				{
					JOptionPane errorPane = new JOptionPane();
					errorPane.showMessageDialog(gameBoard, "Please input a valid port number", "ERROR", JOptionPane.ERROR_MESSAGE);
				}
			}
			else if(result == JOptionPane.CANCEL_OPTION) //If the user presses cancel, do nothing
			{
			
			}
		}
		else if(e.getActionCommand().equals("Disconnect")) //If the user selects the disconnect option
		{
			gameBoard.getConnection().disconnect();
		}
		else if(e.getActionCommand().equals("Exit")) //If the user presses exit, end the program
		{
			System.exit(0);
		}
		else if(e.getSource() == gameBoard.musicItem) //If the users selects the turn on/off music option
		{
			if(e.getActionCommand().equals("Turn Off Music")) //If the command reads "Turn Off Music" then stop the music and change the command to "Turn On Music"
			{
				PlayMusic.stopMusic();
				gameBoard.musicItem.setText("Turn On Music");
			}
			else if(e.getActionCommand().equals("Turn On Music")) //If the command reads "Turn on Music" then start the music and change the command to "Turn Off Music"
			{
				PlayMusic.startMusic();
				gameBoard.musicItem.setText("Turn Off Music");
			}
		}
		else if(e.getSource() == gameBoard.soundItem) //If the users selects the turn on/off sound effects option
		{
			if(e.getActionCommand().equals("Turn Off Sound Effects")) //If the command reads "Turn Off Sound Effects"...
			{
				TigerRoar.soundFx = false; //...turn the sound effects off and set the text to "Turn On Sound Effects"
				gameBoard.soundItem.setText("Turn On Sound Effects");
			}
			else if(e.getActionCommand().equals("Turn On Sound Effects")) //If the command reads "Turn On Sound Effects"...
			{
				TigerRoar.soundFx = true; //...turn the sound effects on and set the text to "Turn Off Sound Effects"
				gameBoard.soundItem.setText("Turn Off Sound Effects");
			}
		}
		else if(e.getSource() == gameBoard.helpItem) //If the user selects the help option, display a JOptionPane with help content
		{
			showHowToPlay();
		}
		else if(e.getSource() == gameBoard.aboutItem) //If the user selects the about option, display a JOptionPane with the about content
		{
			showAbout();
		}
	}
	
	/**
	* A method to create a JOptionPane with the information regarding how to play Breakthrough
	*/
	private void showHowToPlay()
	{
		String rules = String.format("%s%n%s%n%n%s%n%s%n%s%n%s",
				"This is a two player game similar to checkers.",
				"The goal is to have one of your pieces reach the other side of the board.",
				"Players take turns moving one piece at a time. Pieces may only move a",
				"single space forward or 'capture' an opponent's piece by moving one space",
				"diagonally. Pieces may not move directly forward if the adjacent space is",
				"occupied by an opponent's piece.");
		JOptionPane.showMessageDialog(gameBoard, rules, "Rules", JOptionPane.PLAIN_MESSAGE);
	}
	
	/**
	* A method to create a JOptionPane with the about information for this program
	*/
	private void showAbout()
	{
		String about = String.format("%s%n%s%n%s%n%s%n%s",
				"RIT Breakthrough\n\n",
				"Jon Theismann, DJ Crocker, & Andy DiStasi\n\n",
				"In loving memory of Josh Vickerson.\nA master coder called to the great API in the sky long before his time.\nHe was a decent programmer and a great friend.\n\n",
				"Programming for IT 3  219-02",
				"Version 2.0        May 2, 2013");
		JOptionPane.showMessageDialog(gameBoard, about, "About",
				JOptionPane.INFORMATION_MESSAGE, Board.TIGER_R);
	}

}