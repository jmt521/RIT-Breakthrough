import javax.sound.sampled.*;
import java.io.*;
import java.net.*;

/**
* Andrew DiStasi, DJ Crocker, Josh Vickerson, and Jonathan Theismann
* 4/7/13
* This is a class to create and run the 'breakthrough' game
*/
public class TigerRoar extends Thread{
	/**
	*Clip to play roar
	*/
	static Clip clip;
	
	/**
	*Boolean to record is user has sound effects enabled or disabled
	*Enabled by default
	*/
	 static boolean soundFx = true;
	/**
	*Method to run thread
	*/
	public void run(){
		//file containing sound
		URL musicFile = PlayMusic.class.getResource("roar.wav");
		
		//try playing the sound
		try {
			 clip = AudioSystem.getClip();
			 AudioInputStream inputStream = AudioSystem.getAudioInputStream(musicFile);
			 clip.open(inputStream);
			 clip.start(); 
		}
		//catch any exception
		catch (Exception e) {
		        System.err.println(e.getMessage());
		}
	}
}