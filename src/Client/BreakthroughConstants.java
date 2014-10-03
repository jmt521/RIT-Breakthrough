/**
* Andrew DiStasi, DJ Crocker, Josh Vickerson, and Jonathan Theismann
* 4/7/13
* This is an interface to store constants used in the program
*/

import java.awt.*;
import java.io.*;

public interface BreakthroughConstants
{
	//Server Constants
	int DEFAULT_PORT = 16238;
	int HEARTBEAT_INTERVAL = 200; // milliseconds. Ryan's group using 500
	int TIMEOUT = 50; // number of heartbeats. Ryan's group using 20
	
	// game constants
	int EMPTY = 0;
	int P1 = 1;
	int P2 = 2;
	int STARTING_BOARD[][] = { {P1,P1,EMPTY,EMPTY,EMPTY,EMPTY,P2,P2},
										{P1,P1,EMPTY,EMPTY,EMPTY,EMPTY,P2,P2},
										{P1,P1,EMPTY,EMPTY,EMPTY,EMPTY,P2,P2},
										{P1,P1,EMPTY,EMPTY,EMPTY,EMPTY,P2,P2},
										{P1,P1,EMPTY,EMPTY,EMPTY,EMPTY,P2,P2},
										{P1,P1,EMPTY,EMPTY,EMPTY,EMPTY,P2,P2},
										{P1,P1,EMPTY,EMPTY,EMPTY,EMPTY,P2,P2},
										{P1,P1,EMPTY,EMPTY,EMPTY,EMPTY,P2,P2} };
	
	// generic packet IDs (either way)
	int HEARTBEAT = 0;
	int LOBBY_CHAT = 1;
	int PRIVATE_CHAT = 2;
	
	// client packet IDs (sent to server from client)
	int JOIN_QUEUE = 3;
	int MOVE = 4;
	int LOGOUT = 5;
	
	// server packet IDs (sent to client from server)
	int GAME_STARTED = 3;
	int BOARD_UPDATE = 4;
	int ERROR_MESSAGE = 5;
	int GAME_MESSAGE = 6;
	int LIST_PLAYERS = 7;
	int GAME_OVER = 8;


	//Define --OFFICIAL-- RIT colors from the web standards (Gotta have our RIT swag!)
	Color ORANGE = new Color(243,111,33);
	Color BROWN  = new Color(81, 49, 39);
	Color LIGHT_CREAM = new Color(248,247,237);
	Color DARK_CREAM = new Color(223,222,203);
	
}