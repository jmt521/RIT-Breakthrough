import javax.sound.sampled.*;
import java.io.*;
import java.net.*;

/**
* Andrew DiStasi, DJ Crocker, Josh Vickerson, and Jonathan Theismann
* 4/7/13
* This is a class to play the music
*/
public class PlayMusic extends Thread{
	/**
	*Clip to load music 
	*/
	static Clip clip;
	
	/**
	 *Boolean to record is user has music enabled or disabled
	 *Enabled by default
	 */
	 static boolean musicOn = true;
	/**
	*Method to run thread
	*/
	public void run(){
		
		URL musicFile = PlayMusic.class.getResource("eye.wav");
		
		//try to play music
		try {
			 clip = AudioSystem.getClip();
			 AudioInputStream inputStream = AudioSystem.getAudioInputStream(musicFile);
			 clip.open(inputStream);
			 clip.loop(Clip.LOOP_CONTINUOUSLY);
			 clip.start(); 

		}
		//catch any exception
		catch (Exception e) {
		        System.err.println(e.getMessage());
		}
	}
	
	/**
	*Method to stop music during the game
	*Resets clip to begining of song
	*/
	public static void stopMusic()
	{
		clip.stop();
		clip.setFramePosition(0);
		musicOn = false;
	}
	
	/**
	*Method to restart music during the game
	*/
	public static void startMusic()
	{
		clip.start();
		musicOn = true;
	}
}