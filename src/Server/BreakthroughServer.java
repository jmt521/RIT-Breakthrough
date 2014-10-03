import java.io.*;
import java.net.*;
import java.util.Vector;

/**
	* Andrew DiStasi, DJ Crocker, Josh Vickerson, and Jonathan Theismann
	* 4/7/13
	*Main class for the breakthrough server.
	*Makes and manages connections and games
	*@version 0.1
*/
public class BreakthroughServer implements BreakthroughConstants
{
	// instance variables
	private static Vector<ClientThread> clients = new Vector<ClientThread>(); // vectors are already synchronized. Yay!
	private static Vector<ClientThread> queue = new Vector<ClientThread>();
	private static Vector<GameInstance> games = new Vector<GameInstance>();
	private static ServerGUI gui;
	
	/**
		*Main method. Instantiate the GUI and creates sockets for clients
		*as they connect.
		*@param args not used
	*/
	public static void main(String[] args)
	{
		gui = new ServerGUI();
		try
		{
			ServerSocket ss = new ServerSocket(DEFAULT_PORT);
			
			// continuously wait for clients to connect
			while(true)
			{
				Socket s = ss.accept();
				// create the client thread, add it to the vector, and start it
				ClientThread client = new ClientThread(s);
				if(client.isConnected())
				{
					clients.add(client);
					client.start();
				}
				// send out updated player list to all clients
				for(ClientThread c : clients)
				{
					c.sendPlayerList();
				}
				gui.updateUsers(clients);
			}
		}
		catch(IOException ioe){}
	}
	
	/**
		*Accessor for the clients connected to the server
		*@return the clients connected to the server
	*/
	public static Vector<ClientThread> getClients()
	{
		return clients;
	}
	
	/**
		*Accessor for the queue
		*@return the queue of clients waiting for games
	*/
	public static Vector<ClientThread> getQueue()
	{
		return queue;
	}
	
	/**
		*Accessor for the games in progress on the server
		*@return the games currently taking place
	*/
	public static Vector<GameInstance> getGames()
	{
		return games;
	}
	
	/**
		*Remove a client connection from the server
		*@param c the client to be removed
	*/
	public static void removeClient(ClientThread c)
	{
		if(clients.contains(c))
		{
			clients.remove(c);
			gui.updateUsers(clients);
			for(ClientThread client : clients)
			{
				client.sendPlayerList();
			}
		}
	}
	
	/**
		*Add a client to the queue to wait for available games
		*@parm client the client joining the queue
	*/
	public static synchronized void addToQueue(ClientThread client)
	{
		// make sure client isn't already in queue
		if(queue.contains(client))
		{
			client.sendGameError("You are already in the queue");
		}
		else
		{
			queue.add(client);
			// make games if there are enough clients in queue
			while(queue.size() >= 2)
			{
				games.add(new GameInstance(queue.get(0),queue.get(1)));
				queue.remove(1);
				queue.remove(0);
				gui.updateGames(games);
			}
		}
	}
	
	/**
		*Remove a game from the server
		*@param g the game to be removed from the server
	*/
	public static synchronized void removeGame(GameInstance g)
	{
		games.remove(g);
		gui.updateGames(games);
	}
	
	/**
		*Generates a new standard board to be played on
		*@return the new board
	*/
	public static int[][] generateBoard()
	{
		// copy the STARTING_BOARD constant
		int[][] newBoard = new int[8][8];
		for(int r=0; r<8; r++)
		{
			for(int c=0; c<8; c++)
			{
				newBoard[r][c] = STARTING_BOARD[r][c];
			}
		}
		return newBoard;
	}
	
	/**
		*Update the GUI with the server's activity
		*@param sender the entty sending the message
		*@param recipient the entity recieving the message
		*@param message the message sent
	*/
	public static void serverMessage(String sender, String recipient, String message)
	{
		gui.updateMessages(sender+"->"+recipient+": "+message);
	}
}