/**
	* Andrew DiStasi, DJ Crocker, Josh Vickerson, and Jonathan Theismann
	* 4/7/13
	*Class reculating gameplay between two clients
*/
public class GameInstance implements BreakthroughConstants
{
	// instance variables 
	private ClientThread player1;
	private ClientThread player2;
	private int[][] board = BreakthroughServer.generateBoard();
	private int turn; // the player whose turn it is
	private boolean gameOver;
	
	/**
		*Create a game between two clients
		*@param p1 the player 1 client
		*@param p2 the player 2 client
	*/
	public GameInstance(ClientThread playerOne, ClientThread playerTwo)
	{
		player1 = playerOne;
		player2 = playerTwo;
		gameOver = false;
		turn = P1;
		player1.setGame(this,P1);
		player2.setGame(this,P2);
		player1.sendGameJoin(P1,playerOne.getUsername(),player2.getUsername());
		player2.sendGameJoin(P2,playerOne.getUsername(),player2.getUsername());
		player1.sendBoardUpdate(turn,board);
		player2.sendBoardUpdate(turn,board);
	}
	
	/**
		*Validate and execute a move
		*@param client the connection for the moving player
		*@param player the player moving
		*@param startR the initial row for the moving piece
		*@param startC the initial col for the moving piece
		*@param toR the target row for the moving piece
		*@param toC the target col for the moving piece
	*/
	public void move(ClientThread client, int player, int startR, int startC, int toR, int toC)
	{
		if(!gameOver)
		{
			boolean validMove = true;
			
			if((turn == P2 || player == P2) && board[startR][startC] == P1) //If a red piece is selected on blue's turn, display an error message
			{
				validMove = false;
				client.sendGameError("Please select one of your pieces");
			}
			else if((turn == P1 || player == P1) && board[startR][startC] == P2) //If a blue piece is selected on red's turn, display an error message
			{
				validMove = false;
				client.sendGameError("Please select one of your pieces");
			}
			
			if(board[toR][toC] == P1) //If the user selects a red piece
			{
				if(toR == startR &&
					toC == startC) //place the tile
				{
					validMove = false;
					client.sendGameError("You must move");
				}
				else if(board[startR][startC] == P2 &&
					toC != (startC - 1) ||
					!(toR == (startR + 1) || toR == (startR - 1))) //If the user captures a blue piece, remove that piece and place the red piece there
				{
					validMove = false;
					client.sendGameError("You can only capture diagonally");
				}
				else if(board[startR][startC] == P1) //Don't allow the red player to select a tile already occupied by a red piece
				{
					validMove = false;
					client.sendGameError("You cannot capture your own piece");
				}
			}
			else if(board[toR][toC] == P2) //If the piece is blue
			{
				if(toR == startR &&
					toC == startC) //place the tile
				{
					validMove = false;
					client.sendGameError("You must move");
				}
				else if(board[startR][startC] == P1 &&
					toC != (startC + 1) ||
					!(toR == (startR + 1) || toR == (startR - 1)))
				{
					validMove = false;
					client.sendGameError("You can only capture diagonally");
				}
				else if(board[startR][startC] == P2) //don't allow the user to place a blue piece on a tile that already has a blue piece
				{
					validMove = false;
					client.sendGameError("You cannot capture your own piece");
				}
			}
			else
			{
				if(board[startR][startC] == P1 &&
					(toC != (startC + 1) ||
					(toR != startR && toR != (startR + 1) && toR != (startR - 1)))) //...and the piece is an invalid move with red, reset the cursor and selected 
				{
					validMove = false;
					client.sendGameError("Invalid move");
				}
				else if(board[startR][startC] == P2 &&
					(toC != (startC - 1) ||
					(toR != startR && toR != (startR + 1) && toR != (startR - 1)))) //...and the piece is an invalid move with blue, reset the cursor and selected
				{
					validMove = false;
					client.sendGameError("Invalid move");
				}
			}
			if(validMove)
			{
				// update the board array to reflect move
				board[toR][toC] = board[startR][startC];
				board[startR][startC] = EMPTY;
				// change the turn
				if(turn == P1)
				{
					turn = P2;
				}
				else
				{
					turn = P1;
				}
				player1.sendBoardUpdate(turn,board);
				player2.sendBoardUpdate(turn,board);
				
				// announce victory
				if(player == P1 && toC == 7)
				{
					gameOver(player1.getUsername() + " wins!");
				}
				else if(player == P2 && toC == 0)
				{
					gameOver(player2.getUsername() + " wins!");
					
				}
			}
		}
	}
	
	/**
		*Send message to each player notifying them of the outcome of the game
		*@param message the game over message
	*/
	public void gameOver(String message)
	{
		gameOver = true;
		player1.sendGameOver(message);
		player2.sendGameOver(message);
		player1.setGame(null,0);
		player2.setGame(null,0);
		BreakthroughServer.removeGame(this);
	}
	
	/**
		*Get the players in the game
		*@return the game's players
	*/
	public ClientThread[] getPlayers()
	{
		ClientThread[] players = {player1,player2};
		return players;
	}
	
	/**
		*Return a string with players' names
		*@the players' names
	*/
	public String toString()
	{
		return player1.getUsername()+" and "+player2.getUsername();
	}
}