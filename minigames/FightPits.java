package server.model.minigames;

import java.util.ArrayList;

import server.Config;
import server.model.players.Client;
import server.model.players.PlayerHandler;
import server.util.Misc;

/**
 * @author hadesflames
 */

public class FightPits{
	private static int FEE = 100000000;
	public int[] playerInPits = new int[200];

	public static ArrayList<Integer> playersWait = new ArrayList<Integer>();
	private int GAME_TIMER = 140;
	private int GAME_START_TIMER = 40;

	private int gameTime = -1;
	private int gameStartTimer = 30;
	private int properTimer = 0;
	public int playersRemaining = 0;
	private static long pot = 0, game_pot = 0;
	private static final double PERCENT = 0.6;
	
	public String pitsChampion = "Nobody";

	public void process(){
		if(properTimer > 0){
			properTimer--;
			return;
		}else{
			properTimer = 4;
		}
		if(gameStartTimer > 0){
			gameStartTimer--;
			updateWaitRoom();
		}
		if(gameStartTimer == 0){
			if(playersRemaining > 0)
				gameStartTimer = 150;
			else
				startGame();
		}
		if(gameTime > 0){
			gameTime--;
			if(playersRemaining == 1)
				endPitsGame(getLastPlayerName());
		}else if(gameTime == 0){
			if(playersRemaining <= 0)
				endPitsGame("Nobody");
			else
				gameTime = 150;
		}
	}

	public String getLastPlayerName(){
		for(int j = 0; j < playerInPits.length; j++){
			if(playerInPits[j] > 0)
				return PlayerHandler.players[playerInPits[j]].playerName;
		}
		return "Nobody";
	}

	public static void removeWait(int id){
		for(int i = 0; i < playersWait.size(); i++){
			if(playersWait.get(i) == id){
				playersWait.remove(i);
				break;
			}
		}
	}

	public static void handleEntrance(Client c){
		if(!c.inPitsWait){
			synchronized(c){
				if(!c.inventory.hasItem(995, FEE)){
					c.sendMessage("In order to enter this tournament, you need to have the entrance fee.");
					c.sendMessage("The entrance fee is " + (FEE / 1000000) + "M.");
					return;
				}
				c.inventory.deleteItem(995, FEE);
				pot += FEE;
				c.sendMessage("Warning, if you leave you will automatically lose your entrance fee.");
				c.getPA().movePlayer(2399, 5175, 0);
				c.inPitsWait = true;
			}
			playersWait.add(c.playerId);
		}else{
			removeWait(c.playerId);
			synchronized(c){
				c.getPA().movePlayer(2399, 5177, 0);
				c.inPitsWait = false;
			}
		}
	}

	public void updateWaitRoom(){
		for(int i = 0; i < playersWait.size(); i++){
			int id = playersWait.get(i);
			if(PlayerHandler.players[id] != null){
				Client c = (Client)PlayerHandler.players[id];
				c.getPA().sendText("Next Game Begins In : " + (((gameStartTimer * 3) + (gameTime * 3)) + 3) + " seconds.", 6570);
				c.getPA().sendText("Champion: " + pitsChampion, 6572);
				c.getPA().sendText("", 6664);
				c.getPA().walkableInterface(6673);
			}else
				playersWait.remove(i);
		}
	}

	public void startGame(){
		if(playersWait.size() < 2){
			gameStartTimer = GAME_START_TIMER / 2;
			// System.out.println("Unable to start fight pits game due to lack of players.");
			return;
		}
		playersRemaining = 0;
		for(int id : playersWait)
			if(PlayerHandler.players[id] != null)
				addToPitsGame(id);
		game_pot = pot;
		pot = 0;
		playersWait.clear();
		System.out.println("Fight Pits game started.");
		gameStartTimer = GAME_START_TIMER + GAME_TIMER;
		gameTime = GAME_TIMER;
	}

	public void removePlayerFromPits(int playerId){
		for(int j = 0; j < playerInPits.length; j++){
			if(playerInPits[j] == playerId){
				Client c = (Client)PlayerHandler.players[playerInPits[j]];
				synchronized(c){
					c.addedToPits = true;
					c.getPA().movePlayer(2399, 5177, 0);
					c.inPits = false;
					c.addedToPits = false;
				}
				playerInPits[j] = -1;
				playersRemaining--;
				break;
			}
		}
	}

	public void endPitsGame(String champion){
		for(int j = 0; j < playerInPits.length; j++){
			if(playerInPits[j] < 0)
				continue;
			if(PlayerHandler.players[playerInPits[j]] == null)
				continue;
			Client c = (Client)PlayerHandler.players[playerInPits[j]];
			synchronized(c){
				long fixed_val = (long)(game_pot * PERCENT);
				if(fixed_val > Integer.MAX_VALUE){
					int certs = (int)Math.floor(((double)fixed_val / (double)2000000000));
					int cash = (int)(fixed_val - (certs * 2000000000));
					c.bank.addItem(Config.BANK_CERTIFICATE, certs);
					c.bank.addItem(995, cash);
				}else
					c.bank.addItem(995, (int)fixed_val);
				c.sendMessage("Congratulations, you won the " + (fixed_val / 1000000) + "M pot!");
				c.getPA().movePlayer(2399, 5177, 0);
				c.inPits = false;
			}
			break;
		}
		playerInPits = new int[200];
		pitsChampion = champion;
		playersRemaining = 0;
		game_pot = 0;
		pitsSlot = 0;
		gameStartTimer = GAME_START_TIMER;
		gameTime = -1;
	}

	private int pitsSlot = 0;

	public void addToPitsGame(int playerId){
		if(PlayerHandler.players[playerId] == null)
			return;
		Client c = (Client)PlayerHandler.players[playerId];
		playersRemaining++;
		c.getPA().walkableInterface(-1);
		playerInPits[pitsSlot++] = playerId;
		synchronized(c){
			c.inPits = true;
			c.addedToPits = true;
			c.getPA().movePlayer(2392 + Misc.random(12), 5139 + Misc.random(25), 0);
			c.addedToPits = false;
		}
	}
}