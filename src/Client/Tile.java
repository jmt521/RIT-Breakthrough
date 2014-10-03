import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
* Andrew DiStasi, DJ Crocker, Josh Vickerson, and Jonathan Theismann
* 4/7/13
* This is a class to create the Tiles that make up the gameboard
* This class extends JPanel and implements BreakthroughConstants
*/
public class Tile extends JPanel implements BreakthroughConstants
{
	//attributes for the class
	private int row;
	private int col;
	private int piece;
	
	private static final Color ORANGE = new Color(243,111,33);
   private static final Color BROWN  = new Color(81, 49, 39);
	
	/**
	* Parameterized constructor for the Tile Class
	* @param newRow The row the tile is located in
	* @param newCol The column the tile is located in
	*/
	public Tile(int newRow, int newCol)
	{
		row = newRow;
		col = newCol;
		setLayout(new BorderLayout());
		// add(new JLabel("("+row+","+col+")"));
		setPreferredSize(new Dimension(75,75));
		if((row+col) % 2 != 0) //coloring the tile based on position
		{
			setBackground(ORANGE);
		}
		else
		{
			setBackground(BROWN);
		}
		
		if(col == 0 || col == 1) //placing pieces based on position
		{
			piece = P1;
			add(new JLabel(Board.TIGER_R));
		}
		else if(col == 6 || col == 7) //placing pieces based on position
		{
			piece =  P2;
			add(new JLabel(Board.TIGER_L));
		}
		else
		{
			piece = EMPTY;
		}
	}
	
	/**
	* Accessor method to return the piece
	* @return  piece The piece (red, blue, or empty) located in the tile
	*/
	public int getPiece()
	{
		return piece;
	}
	
	/**
	* Accessor method to return the row
	* @return  row The row the tile is located in
	*/
	public int getRow()
	{
		return row;
	}
	
	/**
	* Accessor method to return the column
	* @return  col The column the tile is located in
	*/
	public int getCol()
	{
		return col;
	}
	
	/**
	* A method to add a piece to a Tile
	* @param newPiece The piece to be added to the tile
	*/
	public void setPiece(int newPiece)
	{
		removeAll();
		piece = newPiece;
		if(piece == P1) //If the piece is red...
		{
			add(new JLabel(Board.TIGER_R));
		}
		else if(piece == P2) //If the piece is blue
		{
			add(new JLabel(Board.TIGER_L));
		}
		validate();
  		repaint();
	}
	
	/**
	* A method to reset the gameBoard to it's original state
	*/
	public void reset()
	{
		if(col==0 || col==1) //Place the red pieces in the first two columns
		{
			setPiece(P1);
		}
		else if(col==6 || col==7) //Place the blue pieices in the last two columns
		{
			setPiece(P2);
		}
		else //All other columns are empty
		{
			setPiece(EMPTY);
		}
	}
}