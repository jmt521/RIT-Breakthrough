import java.io.*;
import java.net.*;
import java.util.Vector;

/**
* Andrew DiStasi, DJ Crocker, Josh Vickerson, and Jonathan Theismann
* 4/7/13
* Facilitate connection to the server. Handles communications as they arrive and allows
* other classes to send data to the server
* This class extends Thread and implements BreakthroughConstants
*/
public class ServerConnection extends Thread implements BreakthroughConstants
{
	//attributes for the class
	private Socket s;
	private DataInputStream in;
	private DataOutputStream out;
	private boolean game = false;
	private boolean connected = false;
	private String username;
	private String[] names = {"",""};
	private Board gameBoard;
	private int timeout;
	int player;
	int turn;
	
	/**
		*Constructor. Creates the program's costant connection to the server.
		*@param newSocket the socket connection to the server
		*@param newBoard the Breakthrough game board
		*@param newName the player's username
	*/
	public ServerConnection(Socket newSocket, Board newBoard, String newName)
	{
		s = newSocket;
		gameBoard = newBoard;
		username = newName.trim();
		try
		{
			in = new DataInputStream(s.getInputStream());
			out = new DataOutputStream(s.getOutputStream());
			
			out.writeUTF(username);
			out.flush();
			if(in.readInt() == -1)
			{
				connected = false;
				gameBoard.displayError("A user with the name " + username + " is already connected to the server.");
			}
			else
			{
				connected = true;
			}
			gameBoard.setConnected(connected);
			if(!connected)
			{
				disconnect();
			}
			timeout = TIMEOUT;
		}
		catch(IOException ioe)
		{
			disconnect();
		}
	}
	
	/**
		*Loop every 0.2 seconds while connected to the server, handling any data that is
		*recieved.
	*/
	public void run()
	{
		while(connected) //Run the thread permanantly
		{
			try
			{
				if(in.available()>0)
				{
					// reset the timout countdown
					timeout = TIMEOUT;
					// handle all data currently available
					while(in.available()>0)
					{
						sendHeartbeat();
						int pktID = in.read();
						recieveData(pktID);
					}
				}
				else
				{
					timeout -= 1;
					// the client has gone too long without hearing from the server
					if(timeout <= 0)
					{
						disconnect();
						gameBoard.displayError("Timed out from server.");
					}
				}
			}
			catch(IOException ioe)
			{
				disconnect();
			}
			try
			{
				// wait before checking for data again
				sleep(HEARTBEAT_INTERVAL);
			}
			catch(InterruptedException ie)
			{
				disconnect();
			}
		}
		disconnect();
	}
	
	/**
		*Send a heartbeat packet to the server, letting it know that the client
		*is still connected
	*/
	public synchronized void sendHeartbeat()
	{
		try
		{
			out.write(HEARTBEAT);
			out.flush();
		}
		catch(IOException ioe)
		{
			disconnect();
		}
	}
	
	/**
		*Sends a message to the server to be relayed to all connected players
		*@param message the message to be sent to all players
	*/
	public synchronized void sendLobbyChat(String message)
	{
		try
		{
			out.write(LOBBY_CHAT);
			out.writeUTF(message);
			out.flush();
		}
		catch(IOException ioe)
		{
			disconnect();
		}
	}
	
	/**
		*Sends a message to the server to be relayed to game opponent only
		*@param message the message to be sent to opponent
	*/
	public synchronized void sendGameChat(String message)
	{
		try
		{
			out.write(PRIVATE_CHAT);
			out.writeUTF(message);
			out.flush();
		}
		catch(IOException ioe)
		{
			disconnect();
		}
	}
	
	/**
		*Tell the server that the client is ready to join a game
	*/
	public synchronized void joinQueue()
	{
		try
		{
			out.write(JOIN_QUEUE);
			out.flush();
			gameBoard.getTurnLabel().setText("Breakthrough - In Queue");
		}
		catch(IOException ioe)
		{
			disconnect();
		}
	}
	
	/**
		*Send a move to the server to be validated and executed
		*@param startR the initial row of the piece to be moved
		*@param startC the initial col of the piece to be moved
		*@param toR the target row of the piece to be moved
		*@param toC the target col of the piece to be moved
	*/
	public synchronized void sendMove(int startR, int startC, int toR, int toC)
	{
		try
		{
			out.write(MOVE);
			out.writeInt(startC);
			out.writeInt(startR);
			out.writeInt(toC);
			out.writeInt(toR);
			out.flush();
		}
		catch(IOException ioe)
		{
			disconnect();
		}
	}
	
	/**
		*Tell the server that the client is disconnecting
	*/
	public synchronized void sendLogout()
	{
		try
		{
			out.write(LOGOUT);
			out.flush();
		}
		catch(IOException ioe)
		{
			disconnect();
		}
	}
	
	/**
		*Read and handle data from the server based on the specified packet type
		*@param pktID the type of data to be read
	*/
	public synchronized void recieveData(int pktID)
	{
		switch(pktID)
		{
			// heartbeat recieved
			case HEARTBEAT:
				break;
			// game chat recieved
			case LOBBY_CHAT:
				try
				{
					String sender = in.readUTF();
					String message = in.readUTF();
					gameBoard.addLobbyChat(sender,message);
				}
				catch(IOException ioe)
				{
					disconnect();
				}
				break;
			// private chat recieved
			case PRIVATE_CHAT:
				try
				{
					String sender = in.readUTF();
					String message = in.readUTF();
					gameBoard.addGameChat(sender,message);
				}
				catch(IOException ioe)
				{
					disconnect();
				}
				break;
			// a game has started
			case GAME_STARTED:
				try
				{
					player = in.readInt();
					names[P1-1] = in.readUTF();
					names[P2-1] = in.readUTF();
					game = true;
					turn = P1;
					gameBoard.setGame(game);
					//gameBoard.setTurn(turn,names[turn-1]);
					gameBoard.setPlayer(player);
					gameBoard.setPlayerName(P1,names[P1-1]);
					gameBoard.setPlayerName(P2,names[P2-1]);
				}
				catch(IOException ioe)
				{
					disconnect();
				}
				break;
			// the board has changed
			case BOARD_UPDATE:
				try
				{
					turn = in.readInt();
					String[] tiles = in.readUTF().split(",");
					int[][] newBoard = new int[8][8];
					int i = 0;
					for(int r=0;r<8;r++)
					{
						for(int c=0;c<8;c++)
						{
							newBoard[r][c] = Integer.parseInt(tiles[i]);
							i++;
						}
					}
					gameBoard.setBoard(newBoard);
					gameBoard.setTurn(turn,names[turn-1]);
				}
				catch(IOException ioe)
				{
					disconnect();
				}
				break;
			// the server has sent an error message
			case ERROR_MESSAGE:
				try
				{
					String error = in.readUTF();
					gameBoard.displayError(error);
				}
				catch(IOException ioe)
				{
					disconnect();
				}
				break;
			// the server has sent a game related message
			case GAME_MESSAGE:
				try
				{
					String message = in.readUTF();
					gameBoard.displayMessage(message);
				}
				catch(IOException ioe)
				{
					disconnect();
				}
				break;
			// the server has sent a list of players
			case LIST_PLAYERS:
				try
				{
					Vector<String> users = new Vector<String>();
					int numPlayers = in.readInt();
					for(int i=0; i<numPlayers; i++)
					{
						users.add(in.readUTF());
					}
					gameBoard.updateUserList(users);
				}
				catch(IOException ioe)
				{
					disconnect();
				}
				break;
			// the current game has ended
			case GAME_OVER:
				TigerRoar roar = new TigerRoar();
				roar.start();
				
				game = false;
				player = 0;
				turn = 0;
				gameBoard.setGame(game);
				gameBoard.setPlayer(player);
				gameBoard.setTurn(turn,"Breakthrough");
				gameBoard.setBoard(STARTING_BOARD);
				gameBoard.setPlayerName(P1,"Player1");
				gameBoard.setPlayerName(P2,"Player2");
				try
				{
					String message = in.readUTF();
					gameBoard.displayMessage(message);
				}
				catch(IOException ioe)
				{
					disconnect();
				}
				break;
			default:
				
				break;
		}
	}
	
	/**
		*Close the connection to the server and notify the GUI.
	*/
	public synchronized void disconnect()
	{
		game = false;
		player = 0;
		connected = false;
		try
		{
			in.close();
			out.close();
			s.close();
		}
		catch(IOException ioe){}
		gameBoard.setGame(game);
		gameBoard.setPlayer(player);
		gameBoard.setConnected(connected);
		gameBoard.setConnection(null);
	}
}