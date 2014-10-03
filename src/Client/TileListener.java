import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
* Andrew DiStasi, DJ Crocker, Josh Vickerson, and Jonathan Theismann
* 4/7/13
* This is a class add functionality to the tiles
* This class extends MouseAdapter and implements BreakthroughConstants
*/
public class TileListener extends MouseAdapter implements BreakthroughConstants
{
	//Attributes for the class
	Tile selected;
	JLabel playerTurn;
	int turnCounter;
	Tile[][] board;
	Board gameBoard;
	
	
	/**
	* Parameterized constructor for the class
	* @param newTurnField A JTextField corresponding with the JTextField used in the turn counter in the GUI
	* @param newFrame A JFrame that corresponds with the board
	* @param _board[][] A 2D Array from the 'Tile' Class
	*/
	public TileListener(JLabel newPlayerTurn, Board _gameBoard, Tile _board[][])
	{
		selected = null;
		playerTurn = newPlayerTurn;
		turnCounter = 1;
		board = _board;
		gameBoard = _gameBoard;
	}
	
	/**
	* A method to reset the turn counter and selected piece to their original values
	*/
	public void reset()
	{
		turnCounter = 1;
		selected = null;
	}
	
	/**
	* A method to provide functionality and allow the tiles to be moved when clicked
	*/
	public void mouseClicked(MouseEvent me)
	{
		try
		{
			gameBoard.getNotifyPlayer().stopNotifying();
		}
		catch(NullPointerException npe){}
	
		if(gameBoard.isGame() && gameBoard.getTurn() == gameBoard.getPlayer())
		{
			Tile clicked = (Tile)(me.getSource());
			
			//Prints out the row and column of the clicked tile: for debugging purposes
			//System.out.println("Row: "+clicked.getRow()+ " Col: "+clicked.getCol());
			
			if(gameBoard.getPlayer() != clicked.getPiece() && selected == null) //If a red piece is selected on blue's turn, display an error message
			{
				return;		
			}
			if(clicked.getPiece() == P1) //If the user selects a red piece
			{
				if(selected == null) //If there was no previously selected piece, select that piece and set the cursor to that image
				{
					selected = clicked;
					setGameCursor(P1);
					
				}
				else if(clicked.getRow() == selected.getRow() &&
					clicked.getCol() == selected.getCol()) //If the user is setting a piece down, reset the cursor and 'selected'
				{
					setGameCursor(EMPTY);
					selected = null;
				}
				else if(selected.getPiece() == P2 &&
					clicked.getCol() != (selected.getCol() - 1) ||
					!(clicked.getRow() == (selected.getRow() + 1) || clicked.getRow() == (selected.getRow() - 1))) //If the user captures a blue piece, remove that piece and place the red piece there
				{
					setGameCursor(EMPTY);
					selected = null;
				}
				else if(selected.getPiece() == P1) //Don't allow the red player to select a tile already occupied by a red piece
				{
					setGameCursor(EMPTY);
					selected = null;
				}
				else //If it is a valid move on the right turn, move the piece
				{
					move(selected,clicked);
				}
			}
			else if(clicked.getPiece() == P2) //If the piece is blue
			{
				if(selected == null) //pick up the tile
				{
					selected = clicked;
					setGameCursor(P2);
				}
				else if(clicked.getRow() == selected.getRow() &&
					clicked.getCol() == selected.getCol()) //place the tile
				{
					setGameCursor(EMPTY);
					selected = null;
				}
				else if(selected.getPiece() == P1 &&
					clicked.getCol() != (selected.getCol() + 1) ||
					!(clicked.getRow() == (selected.getRow() + 1) || clicked.getRow() == (selected.getRow() - 1))) //allow for capturing of red pieces
				{
					setGameCursor(EMPTY);
					selected = null;
				}
				else if(selected.getPiece() == P2) //don't allow the user to place a blue piece on a tile that already has a blue piece
				{
					setGameCursor(EMPTY);
					selected = null;
				}
				else //If it is a valid move, allow the user to move the piece
				{
					move(selected,clicked);
				}
			}
			else
			{
				if(selected != null) //If a piece was selected...
				{
					if(selected.getPiece() == P1 &&
						(clicked.getCol() != (selected.getCol() + 1) ||
						(clicked.getRow() != selected.getRow() && clicked.getRow() != (selected.getRow() + 1)&& clicked.getRow() != (selected.getRow() - 1)))) //...and the piece is an invalid move with red, reset the cursor and selected 
					{
						setGameCursor(EMPTY);
						selected = null;
					}
					else if(selected.getPiece() == P2 &&
						(clicked.getCol() != (selected.getCol() - 1) ||
						(clicked.getRow() != selected.getRow() && clicked.getRow() != (selected.getRow() + 1)&& clicked.getRow() != (selected.getRow() - 1)))) //...and the piece is an invalid move with blue, reset the cursor and selected
					{
						setGameCursor(EMPTY);
						selected = null;
					}
					else //If it's a valid move, move the piece
					{
						move(selected,clicked);
					}
				}
			}
		}
	}
	
	/**
	* A method to move the pieces
	* @param start The starting position of a piece
	* @param end The ending position of a piece
	*/
	public void move(Tile start, Tile end)
	{
		gameBoard.getConnection().sendMove(start.getRow(),start.getCol(),end.getRow(),end.getCol());
		setGameCursor(EMPTY);
		selected = null;
	}
	
	/**
	* A method to set the cursor to the corresponding piece
	* @param piece An integer value corresponding with a piece
	*/
	public void setGameCursor(int piece)
	{
		Toolkit toolkit = Toolkit.getDefaultToolkit();
	 	if(piece == P1) //If it's a red piece, make the cursor a red piece
		{
			Image cursorImage = toolkit.getImage(TileListener.class.getResource("tiger_r.png"));
			Point hotSpot = new Point(16,16);
   		Cursor pieceCursor = toolkit.createCustomCursor(cursorImage, hotSpot, "Player 1");
   		gameBoard.setCursor(pieceCursor);
		}
		else if(piece == P2) //If it's a blue piece, make the cursor a blue piece
		{
			Image cursorImage = toolkit.getImage(TileListener.class.getResource("tiger_l.png"));
			Point hotSpot = new Point(16,16);
   		Cursor pieceCursor = toolkit.createCustomCursor(cursorImage,hotSpot, "Player 2");
   		gameBoard.setCursor(pieceCursor);
		}
		else if(piece == EMPTY) //If it's an empty piece, set the cursor to the default
		{
			gameBoard.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}
}