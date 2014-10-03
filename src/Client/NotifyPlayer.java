/**
* Andrew DiStasi, DJ Crocker, Josh Vickerson, and Jonathan Theismann
* 4/7/13
* This is a class to notify a player of when it is their turn by blinking text color
* This class extends Thread
* This class implements BreakthroughConstants
*/

import java.awt.*;
import javax.swing.*;

public class NotifyPlayer extends Thread implements BreakthroughConstants
{
	//attributes for the class
	Board gameBoard;
	JLabel playerTurn;
	boolean keepNotifying = true;
	boolean isRunning;

	/**
	 *Method to notify a player
	 *@param _gameBoard instance of the game board
	 *@param _playerTurn label to flash
	 */
	public NotifyPlayer(Board _gameBoard, JLabel _playerTurn)
	{
		gameBoard = _gameBoard;
		playerTurn = _playerTurn;
		isRunning = false;
	}

	/**
	*Method to run notify
	*/
	public void run()
	{
		isRunning = true;

		//while it is supposed to keep notifying
		while(getNotify())
		{	
			try
			{
				//flash colors
				gameBoard.getTurnLabel().setForeground(BROWN);
				sleep(500);
				gameBoard.getTurnLabel().setForeground(Color.WHITE);
				sleep(500);
			}
			catch(InterruptedException ie){
				System.out.println("INTERRUPTED");
				keepNotifying = false;
			}

		}
	}

	/**
	 *Method to stop notifying
	 */
	public synchronized void stopNotifying()
	{
		keepNotifying = false;
		isRunning = false;
	}

	/**
	 *Accessor to determine if notify should continue running
	 *@return if notify should keep running
	 */
	public synchronized boolean getNotify()
	{
		return keepNotifying;
	}

	/**
	 *Method to determine if notify is running
	 *@return if it is running
	 */
	public synchronized boolean isRunning()
	{
		return isRunning;
	}
}