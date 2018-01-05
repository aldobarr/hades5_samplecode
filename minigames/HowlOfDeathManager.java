package server.model.minigames;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

import server.clip.region.Region;
import server.model.players.Client;
import server.model.players.PlayerHandler;
import server.util.Misc;

/**
 * 
 * @author hadesflames
 *
 */
public class HowlOfDeathManager{
	private static HowlOfDeathManager singleton;
	private static final int GAME_START_TIME = 60; // X * 3 = time in seconds.
	private static final int PROPER_TIME = 4;
	private static final int MIN_PLAYERS = 3;
	private static final int LOBBY_COORDS[] = {2195, 3251, 0};
	private PriorityQueue<PlayerVal> playersWaiting;
	private ArrayList<HowlOfDeath> games;
	private int gameStartTime;
	private int properTime;
	public static final int REWARD_TICKET = 10943;
	
	private HowlOfDeathManager(){
		gameStartTime = GAME_START_TIME;
		properTime = PROPER_TIME;
		playersWaiting = new PriorityQueue<PlayerVal>(15, new CompareVal());
		games = new ArrayList<HowlOfDeath>();
	}
	
	/**
	 * Instantiates the manager singleton if it has not been instantiated, and returns it.
	 * @return The Howl Of Death Manager singleton.
	 */
	public static HowlOfDeathManager getInstance(){
		if(singleton == null)
			singleton = new HowlOfDeathManager();
		return singleton;
	}
	
	/**
	 * Main game processor.
	 */
	public void process(){
		if(properTime > 0){
			properTime--;
			return;
		}else
			properTime = PROPER_TIME;
		if(gameStartTime > 0)
			gameStartTime--;
		else{
			gameStartTime = GAME_START_TIME;
			if(playersWaiting.size() >= MIN_PLAYERS)
				startGame();
		}
		if(playersWaiting.size() > 0)
			updateLobbyPlayers();
		if(games.size() > 0)
			for(HowlOfDeath game : games)
				game.process();
	}
	
	/**
	 * Displays game start information to all players in the waiting lobby.
	 */
	private void updateLobbyPlayers(){
		Iterator<PlayerVal> players = playersWaiting.iterator();
		ArrayList<Integer> remove = new ArrayList<Integer>();
		while(players.hasNext()){
			int id = players.next().id;
			Client player = (Client)PlayerHandler.players[id];
			if(player == null){
				remove.add(id);
				continue;
			}
			player.getPA().sendText("Next Game Begins In:@red@ " + (((gameStartTime * 3) + 3)) + " @whi@seconds.", 6570);
			player.getPA().sendText("", 6572);
			player.getPA().sendText("", 6664);
			player.getPA().walkableInterface(6673);
		}
		if(remove.size() > 0)
			for(int id : remove)
				playersWaiting.remove(id);
	}
	
	/**
	 * Begins a game.
	 */
	private void startGame(){
		HowlOfDeath newGame = new HowlOfDeath(playersWaiting);
		if(newGame.playerIds.size() == 0)
			playersWaiting.clear();
		else{
			playersWaiting.clear();
			games.add(newGame);
		}
	}
	
	/**
	 * Adds a player to the waiting lobby.
	 * @param player The player to be added.
	 */
	public void addPlayer(Client player){
		if(player == null || playersWaiting.contains(player.playerId))
			return;
		int x = 0;
		int y = 0;
		int count = 0;
		do{
			if(++count == 50){
				x = 0;
				y = 0;
				break;
			}
			x = Misc.random(3) * (Misc.random(99) >= 49 ? -1 : 1);
			y = Misc.random(3) * (Misc.random(99) >= 49 ? -1 : 1);
		}while(!Region.canMove(LOBBY_COORDS[0], LOBBY_COORDS[1], LOBBY_COORDS[0] + x, LOBBY_COORDS[1] + y, LOBBY_COORDS[2], 1, 1));
		if(!player.getPA().checkTele(LOBBY_COORDS[0] + x, LOBBY_COORDS[1] + y))
			return;
		playersWaiting.add(new PlayerVal(player.playerId, Misc.random(1000)));
		player.getPA().sendText("", 6570);
		player.getPA().sendText("", 6572);
		player.getPA().sendText("", 6664);
		player.getPA().walkableInterface(6673);
		player.getPA().dimLight();
		player.howlOfDeathLobby = 2;
		player.teleX = LOBBY_COORDS[0] + x;
		player.teleY = LOBBY_COORDS[1] + y;
		player.teleHeight = LOBBY_COORDS[2];
		player.teleTimer = 10;
		player.teleGfx = 0;
		player.teleEndAnimation = 0;
		player.teleEndGFX = 0;
		player.teleporting = true;
		player.inHowlOfDeathLobby = true;
	}
	
	/**
	 * Removes a player from the waiting lobby if they logout or teleport away.
	 */
	public void removePlayer(int id){
		removeFromQueue(id);
		PlayerHandler.players[id].howlOfDeathLobby = 0;
		if(PlayerHandler.players[id] != null && PlayerHandler.players[id].inHowlOfDeathLobby)
			PlayerHandler.players[id].inHowlOfDeathLobby = false;
	}
	
	/**
	 * Removes the specified player from the waiting queue.
	 * @param playerId The id of the player in question.
	 */
	private void removeFromQueue(int playerId){
		Iterator<PlayerVal> it = playersWaiting.iterator();
		PlayerVal player = null;
		while(it.hasNext()){
			PlayerVal pVal = it.next();
			if(pVal.id == playerId){
				player = pVal;
				break;
			}
		}
		if(player != null)
			playersWaiting.remove(player);
	}
	
	/**
	 * Removes a game from the game manager. This should only be used once the game is completed.
	 * @param game The game to be removed.
	 */
	protected void removeGame(HowlOfDeath game){
		games.remove(game);
	}
	
	public static int getMinPlayers(){
		return MIN_PLAYERS;
	}
	
	public static int[] getLobbyCoords(){
		return LOBBY_COORDS;
	}
}
class CompareVal implements Comparator<PlayerVal>{
	public int compare(PlayerVal o1, PlayerVal o2){
		return o1.rand_val < o2.rand_val ? -1 : o1.rand_val > o2.rand_val ? 1 : 0;
	}
}
class PlayerVal{
	int id, rand_val, hunter_val;
	
	public PlayerVal(int id, int rand_val){
		this.id = id;
		this.rand_val = rand_val;
	}
	
	public PlayerVal(int id, int rand_val, int hunter_val){
		this.id = id;
		this.rand_val = rand_val;
		this.hunter_val = hunter_val;
	}
}