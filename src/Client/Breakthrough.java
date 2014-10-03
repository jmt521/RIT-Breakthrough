/**
* Andrew DiStasi, DJ Crocker, Josh Vickerson, and Jonathan Theismann
* 4/7/13
* This is a class to create and run the 'breakthrough' game
*/
public class Breakthrough
{
	/**
	* Main method to actually instantiate the game and start the threads containing the music
	*/
	public static void main(String[] args)
	{
		Board myBoard = new Board();
		PlayMusic music = new PlayMusic();
		music.start();
	}
}