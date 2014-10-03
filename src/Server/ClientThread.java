import java.io.*;
import java.net.*;
import java.util.Vector;

/**
	* Andrew DiStasi, DJ Crocker, Josh Vickerson, and Jonathan Theismann
	* 4/7/13
	*Class representing the server's connection to a client. Handles all incoming data and
	*allows data to be sent in a synchronized, standardized protocol
*/
public class ClientThread extends Thread implements BreakthroughConstants
{
	// instance variables
	private Socket s;
	private DataInputStream in;
	private DataOutputStream out;
	boolean connected = false;
	private String username;
	private int timeout;
	private GameInstance game = null;
	private int playerID = 0;
	
	/**
		*Constructor. Sets up the connection using a socket, getting a username
		*and checking its validity
		*@param newSocket the socket connection to the client
	*/
	public ClientThread(Socket newSocket)
	{
		s = newSocket;
		try
		{
			in = new DataInputStream(s.getInputStream());
			out = new DataOutputStream(s.getOutputStream());
			
			// read in the client's proposed username and make sure a user with that name is not already connected
			username = in.readUTF().trim();
			int validName = 0;
			for(ClientThread t : BreakthroughServer.getClients())
			{
				if(username.equals(t.getUsername()))
				{
					validName = -1;
					break;
				}
			}
			// let client know of name's vlidity
			out.writeInt(validName);
			out.flush();
			timeout = TIMEOUT;
			if(validName == 0)
			{
				connected = true;
			}
			else
			{
				connected = false;
			}
		}
		catch(IOException ioe)
		{
			disconnect();
		}
	}
	
	public void run()
	{
		// run continuously while connected
		while(connected)
		{
			try
			{
				if(in.available()>0)
				{
					// reset the timeout countdown
					timeout = TIMEOUT;
					// handle all available data
					while(in.available()>0)
					{
						int pktID = in.read();
						recieveData(pktID);
					}
				}
				else
				{
					timeout -= 1;
					
					if(timeout <= 0)
					{
						disconnect();
					}
					// send a heartbeat every fifth cycle
					else if(timeout%5 == 0)
					{
						sendHeartbeat();
					}
				}
			}
			catch(IOException ioe)
			{
				//ioe.printStackTrace();
				disconnect();
			}
			try
			{
				// wait before checking for more data
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
		*Get the client's username
		*@return the client's username
	*/
	public String getUsername()
	{
		return username;
	}
	
	/**
		*Check whether the client is connected
		*@return whether the client is connected
	*/
	public synchronized boolean isConnected()
	{
		return connected;
	}
	
	/**
		*Send a heartbeat packet to the client to notify it of the
		*server's presence
	*/
	public synchronized void sendHeartbeat()
	{
		try
		{
			out.write(HEARTBEAT);
			out.flush();
		}
		catch(IOException e)
		{
			disconnect();
		}
	}
	
	/**
		*Sends a lobby chat message to the client, letting it know who sent it
		*@param sender the message's sender
		*@param message the message
	*/
	public synchronized void sendLobbyChat(String sender, String message)
	{
		try
		{
			out.write(LOBBY_CHAT);
			out.writeUTF(sender);
			out.writeUTF(message.trim());
			out.flush();
			BreakthroughServer.serverMessage("Server",getUsername(),"LOBBY CHAT: "+message.trim());
		}
		catch(IOException ioe)
		{
			disconnect();
		}
	}
	
	/**
		*Sends a game chat message to the client, letting it know who sent it
		*@param sender the message's sender
		*@param message the message
	*/
	public synchronized void sendGameChat(String sender, String message)
	{
		try
		{
			out.write(PRIVATE_CHAT);
			out.writeUTF(sender);
			out.writeUTF(message.trim());
			out.flush();
			BreakthroughServer.serverMessage("Server",getUsername(),"GAME CHAT: "+message.trim());
		}
		catch(IOException ioe)
		{
			disconnect();
		}
	}
	
	/**
		*Let the client know that it has joined a game
		*@param playerID the client's playerID
		*@param p1Name player 1's name
		*@param p2Name player 2's name
	*/
	public synchronized void sendGameJoin(int playerID, String p1Name, String p2Name)
	{
		try
		{
			out.write(GAME_STARTED);
			out.writeInt(playerID);
			out.writeUTF(p1Name);
			out.writeUTF(p2Name);
			out.flush();
			BreakthroughServer.serverMessage("Server",getUsername(),"GAME JOINED: "+playerID+" "+p1Name+" "+p2Name);
		}
		catch(IOException ioe)
		{
			disconnect();
		}
		sendBoardUpdate(P1,BreakthroughServer.generateBoard());
	}
	
	/**
		*Send board and current turn to player
		*@param player the current turn
		*@param board the current board state
	*/
	public synchronized void sendBoardUpdate(int player, int[][] board)
	{
		// convert the board state to a comma separated string before sending
		String boardState = "";
		for(int row=0; row<8; row++)
		{
			for(int col=0; col<8; col++)
			{
				boardState += board[row][col] + ",";
			}
		}
		// trim off the trailing comma
		boardState = boardState.substring(0,boardState.length()-1);
		System.out.println(boardState);
		try
		{
			out.write(BOARD_UPDATE);
			out.writeInt(player);
			out.writeUTF(boardState);
			out.flush();
			BreakthroughServer.serverMessage("Server",getUsername(),"BOARD UPDATE: "+player+" "+boardState);
		}
		catch(IOException ioe)
		{
			disconnect();
		}
	}
	
	/**
		*Send an error message to the client
		*@param message the error message
	*/
	public synchronized void sendGameError(String message)
	{
		try
		{
			out.write(ERROR_MESSAGE);
			out.writeUTF(message);
			out.flush();
			BreakthroughServer.serverMessage("Server",getUsername(),"ERROR MESSAGE: "+message);
		}
		catch(IOException ioe)
		{
			disconnect();
		}
	}
	
	/**
		*Send an game message to the client
		*@param message the game message
	*/
	public synchronized void sendGameMessage(String message)
	{
		try
		{
			out.write(GAME_MESSAGE);
			out.writeUTF(message);
			out.flush();
			BreakthroughServer.serverMessage("Server",getUsername(),"GAME MESSAGE: "+message);
		}
		catch(IOException ioe)
		{
			disconnect();
		}
	}
	
	/**
		*Send a list of currently connected players to the client
	*/
	public synchronized void sendPlayerList()
	{
		try
		{
			out.write(LIST_PLAYERS);
			Vector<ClientThread> clients = BreakthroughServer.getClients();
			out.writeInt(clients.size());
			for(ClientThread c : clients)
			{
				out.writeUTF(c.getUsername());
			}
			out.flush();
			BreakthroughServer.serverMessage("Server",getUsername(),"LIST PLAYERS");
		}
		catch(IOException ioe)
		{
			disconnect();
		}
	}
	
	/**
		*Sends a game over message to the client
		*@param message the game over message
	*/
	public synchronized void sendGameOver(String message)
	{
		try
		{
			out.write(GAME_OVER);
			out.writeUTF(message);
			out.flush();
			BreakthroughServer.serverMessage("Server",getUsername(),"GAME OVER: "+message);
		}
		catch(IOException ioe){}
	}
	
	/**
		*Read and handle data from the client based on the specified packet type
		*@param pktID the type of data to be read
	*/
	public synchronized void recieveData(int pktID)
	{
		switch(pktID)
		{
			// heartbeat recieved
			case HEARTBEAT:
				break;
			// lobby chat recieved
			case LOBBY_CHAT:
				try
				{
					String message = in.readUTF();
					BreakthroughServer.serverMessage(getUsername(),"Server","LOBBY CHAT: "+message.trim());
					// send the chat message to each connected client
					for(ClientThread c : BreakthroughServer.getClients())
					{
						c.sendLobbyChat(username,message);
					}
				}
				catch(IOException ioe) {}
				break;
			// game chat recieved
			case PRIVATE_CHAT:
				try
				{
					String message = in.readUTF();
					BreakthroughServer.serverMessage(getUsername(),"Server","GAME CHAT: "+message.trim());
					if(game != null)
						for(ClientThread c : game.getPlayers())
						{
							c.sendGameChat(username,message);
						}
					else
						{
							sendGameError("***Not currently in game");
						}
				}
				catch(IOException ioe) {}
				break;
			// the client has requested to join the queue
			case JOIN_QUEUE:
				BreakthroughServer.addToQueue(this);
				BreakthroughServer.serverMessage(getUsername(),"Server","JOIN QUEUE");
				break;
			// the client has submitted a move
			case MOVE:
				try
				{
					int startC = in.readInt();
					int startR = in.readInt();
					int toC = in.readInt();
					int toR = in.readInt();
					BreakthroughServer.serverMessage(getUsername(),"Server","MOVE: "+getUsername()+" ("+startC+","+startR+")->("+toC+","+toR+")");
					if(game != null && playerID != 0)
					{
						game.move(this,playerID,startR,startC,toR,toC);
					}
				}
				catch(IOException ioe) {}
				break;
			// the client is logging out
			case LOGOUT:
				BreakthroughServer.serverMessage(getUsername(),"Server","LOGOUT");
				disconnect();
				break;
			default:
				
		}
	}
	
	/**
		*Set the game instance the client is playing in
		*@param g the game instance being played in
		*@param id the client's player number
	*/
	public synchronized void setGame(GameInstance g, int id)
	{
		game = g;
		playerID = id;
	}
	
	/**
		*Close the connection to the client
	*/
	public synchronized void disconnect()
	{
		connected = false;
		if(game != null)
		{
			// notify opponent of disconnect
			game.gameOver(getUsername() + " disconnected");
		}
		try
		{
			in.close();
			out.close();
			s.close();
		}
		catch(IOException ioe) {}
		
		// remove all traces of the client from the server
		if(BreakthroughServer.getQueue().contains(this))
		{
			BreakthroughServer.getQueue().remove(this);
		}
		
		BreakthroughServer.removeClient(this);
	}
}