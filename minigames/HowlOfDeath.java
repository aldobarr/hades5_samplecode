package server.model.minigames;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import server.Config;
import server.Server;
import server.model.players.Client;
import server.model.players.PlayerHandler;
import server.util.Misc;

public class HowlOfDeath{
	public Map<Integer, Hunter> playerTargets;
	public List<Integer> playerIds;
	public static ArrayList<CityCoords> cities = new ArrayList<CityCoords>();
	
	public HowlOfDeath(PriorityQueue<PlayerVal> players){
		playerTargets = new HashMap<Integer, Hunter>();
		playerIds = new ArrayList<Integer>();
		int count = 0;
		Iterator<PlayerVal> tempPlayerIds = players.iterator();
		while(tempPlayerIds.hasNext()){
			int id = tempPlayerIds.next().id;
			boolean add = teleportPlayer(id, cities.get(Misc.random(cities.size() - 1)));
			if(add){
				count++;
				playerIds.add(id);
			}
		}
		if(count < HowlOfDeathManager.getMinPlayers()){
			for(int playerId : playerIds){
				Client player = (Client)PlayerHandler.players[playerId];
				if(player == null)
					continue;
				player.getPA().movePlayer(Config.EDGEVILLE_X, Config.EDGEVILLE_Y, 0);
				player.inHowlOfDeath = false;
				player.inHowlOfDeathLobby = false;
				player.howlOfDeath = null;
			}
			playerIds.clear();
			playerTargets.clear();
			return;
		}
		int prev = -1;
		int firstPlayer = players.poll().id;
		int playerOne = firstPlayer, playerTwo = players.poll().id;
		boolean br = false;
		do{
			playerTargets.put(playerOne, new Hunter(playerTwo, prev));
			if(br)
				break;
			prev = playerOne;
			playerOne = playerTwo;
			if(players.size() > 0){
				playerTwo = players.poll().id;
				playerIds.add(playerTwo);
			}else{
				playerTwo = firstPlayer;
				Hunter h = playerTargets.get(firstPlayer);
				h.hunter = playerOne;
				playerTargets.put(firstPlayer, h);
				br = true;
			}
		}while(true);
	}
	
	/**
	 * Loads city coordinate information. Hard coded because configs aren't necessary for this small
	 * bit of information.
	 */
	public static void loadCities(){
		CityCoords edgeville = new CityCoords();
		edgeville.addCoords(3086, 3494, 0);
		edgeville.addCoords(3094, 3503, 0);
		edgeville.addCoords(3110, 3497, 0);
		edgeville.addCoords(3098, 3479, 0);
		CityCoords lumbridge = new CityCoords();
		lumbridge.addCoords(3223, 3226, 0);
		lumbridge.addCoords(3241, 3201, 0);
		lumbridge.addCoords(3195, 3245, 0);
		CityCoords varrock = new CityCoords();
		varrock.addCoords(3221, 3430, 0);
		varrock.addCoords(3259, 3405, 0);
		varrock.addCoords(3239, 3383, 0);
		varrock.addCoords(3194, 3403, 0);
		varrock.addCoords(3183, 3455, 0);
		CityCoords draynor = new CityCoords();
		draynor.addCoords(3114, 3247, 0);
		draynor.addCoords(3093, 3260, 0);
		CityCoords barbarian = new CityCoords();
		barbarian.addCoords(3109, 3432, 0);
		barbarian.addCoords(3080, 3437, 0);
		CityCoords taverley = new CityCoords();
		taverley.addCoords(2891, 3404, 0);
		taverley.addCoords(2905, 3433, 0);
		taverley.addCoords(2924, 3456, 0);
		CityCoords al_kharid = new CityCoords();
		al_kharid.addCoords(3309, 3179, 0);
		al_kharid.addCoords(3290, 3202, 0);
		al_kharid.addCoords(3261, 3174, 0);
		cities.add(edgeville);
		cities.add(lumbridge);
		cities.add(varrock);
		cities.add(draynor);
		cities.add(barbarian);
		cities.add(taverley);
		cities.add(al_kharid);
	}
	
	/**
	 * Sends the player to their random area.
	 * @return True if and only if the player was teleported successfully.
	 */
	public boolean teleportPlayer(int playerId, CityCoords cityCoords){
		Client player = (Client)PlayerHandler.players[playerId];
		if(player == null)
			return false;
		City city = cityCoords.getRandomCoords();
		if(player.overLoad > 0){
			player.playerLevel[player.playerHitpoints] -= 10 * player.overLoad;
			player.overLoad = 0;
			player.overloadTick = 0;
			player.getPA().refreshSkill(player.playerHitpoints);
		}
		if(!player.getPA().checkTele(city.x, city.y))
			return false;
		player.getDH().sendStatement("You suddenly get blindfolded and carried away.");
		player.getPA().dimLight();
		player.inHowlOfDeath = true;
		player.inHowlOfDeathLobby = false;
		player.howlOfDeath = this;
		player.teleX = city.x;
		player.teleY = city.y;
		player.teleHeight = city.z;
		player.teleTimer = 10;
		player.teleGfx = 0;
		player.teleEndAnimation = 0;
		player.teleEndGFX = 0;
		player.teleporting = true;
		return true;
	}
	
	/**
	 * This is used to teleport a player back to an acceptable area when they walk away from the game area.
	 * @param player The player to be teleported.
	 */
	public void reTeleportPlayer(Client player){
		if(player == null)
			return;
		City city = cities.get(Misc.random(cities.size() - 1)).getRandomCoords();
		if(player.overLoad > 0){
			player.playerLevel[player.playerHitpoints] -= 10 * player.overLoad;
			player.overLoad = 0;
			player.overloadTick = 0;
			player.getPA().refreshSkill(player.playerHitpoints);
		}
		if(!player.getPA().checkTele(city.x, city.y))
			return;
		int hit = Misc.random_range(1, 7);
		hit = hit >= player.playerLevel[player.playerHitpoints] ? player.playerLevel[player.playerHitpoints] - 1 : hit;
		player.getDH().sendStatement("All of a sudden, you feel a sharp blow to the back of your head.");
		player.teleX = city.x;
		player.teleY = city.y;
		player.teleHeight = city.z;
		player.teleTimer = 10;
		player.teleGfx = 0;
		player.teleEndAnimation = 0;
		player.teleEndGFX = 0;
		player.teleporting = true;
		player.fixHowlOfDeathTele = false;
		player.playerLevel[player.playerHitpoints] -= hit;
		player.handleHitMask(hit);
		player.getPA().refreshSkill(player.playerHitpoints);
	}
	
	/**
	 * Re-assigns all the targets. This should only be used if there was a problem with how the targets
	 * are currently drawn, for example grabbing a target from the map causing a null pointer
	 * exception.
	 */
	public void redoTargets(){
		if(playerTargets.size() == 1){
			playerTargets.clear();
			playerIds.clear();
			Client winner = (Client)PlayerHandler.players[playerTargets.keySet().iterator().next()];
			winner.inHowlOfDeath = false;
			winner.howlOfDeath = null;
			winner.getDH().sendStatement("Congratulations on winning, you are truly a master Assassin!");
			HowlOfDeathManager.getInstance().removeGame(this);
			return;
		}
		Iterator<Integer> keys = playerTargets.keySet().iterator();
		PriorityQueue<PlayerVal> players = new PriorityQueue<PlayerVal>(15, new CompareVal());
		while(keys.hasNext()){
			int id = keys.next();
			if(PlayerHandler.players[id] == null || PlayerHandler.players[id].loggingOut)
				continue;
			Hunter hunter = playerTargets.get(id);
			players.add(new PlayerVal(id, Misc.random(1000), hunter.reward));
		}
		playerTargets.clear();
		playerIds.clear();
		if(players.size() == 1){
			PlayerVal pVal = players.poll();
			playerTargets.put(pVal.id, new Hunter(0, 0));
			redoTargets();
			return;
		}else if(players.size() == 0)
			HowlOfDeathManager.getInstance().removeGame(this);
		PlayerVal prev = new PlayerVal(-1, -1);
		PlayerVal firstPlayer = players.poll();
		PlayerVal playerOne = firstPlayer, playerTwo = players.poll();
		boolean br = false;
		if(players.size() > 0){
			do{
				playerTargets.put(playerOne.id, new Hunter(playerTwo.id, prev.id, playerOne.hunter_val));
				if(br)
					break;
				prev = playerOne;
				playerOne = playerTwo;
				if(players.size() > 0){
					playerTwo = players.poll();
					playerIds.add(playerTwo.id);
				}else{
					playerTwo = firstPlayer;
					Hunter h = playerTargets.get(firstPlayer);
					h.hunter = playerOne.id;
					playerTargets.put(firstPlayer.id, h);
					br = true;
				}
			}while(true);
		}
	}
	
	/**
	 * Removes a player from the game due to death or logout.
	 * @param player The player to be removed from the game.
	 */
	public void removePlayer(Client player){
		if(playerTargets.size() == 2){
			int id = playerTargets.get(player.playerId).target;
			Hunter hunter = playerTargets.get(id);
			playerTargets.clear();
			playerIds.clear();
			player.inHowlOfDeath = false;
			player.howlOfDeath = null;
			Client winner = (Client)PlayerHandler.players[id];
			if(winner != null){
				winner.inHowlOfDeath = false;
				winner.howlOfDeath = null;
				winner.nextChat = 0;
				winner.getDH().sendStatement("Congratulations on winning, you are truly a master Assassin!");
				hunter.reward++;
				Server.itemHandler.createGroundItem(winner, HowlOfDeathManager.REWARD_TICKET, player.absX, player.absY, player.heightLevel, hunter.reward * 2, winner.playerId);
			}
			HowlOfDeathManager.getInstance().removeGame(this);
			return;
		}
		// 0 = Person they are hunting, 1 = Person hunting them.
		Hunter hunted = null, hunter = null, target = null;
		try{
			hunted = playerTargets.get(player.playerId);
			hunter = playerTargets.get(hunted.hunter);
			target = playerTargets.get(hunted.target);
		}catch(Exception e){
			redoTargets();
			return;
		}
		hunter.reward++;
		playerTargets.put(hunted.hunter, new Hunter(hunted.target, hunter.hunter, hunter.reward));
		playerTargets.put(hunted.target, new Hunter(playerTargets.get(hunted.target).target, hunted.hunter, target.reward));
		playerTargets.remove(player.playerId);
		playerIds.remove((Object)player.playerId);
		Server.itemHandler.createGroundItem((Client)PlayerHandler.players[hunted.hunter], HowlOfDeathManager.REWARD_TICKET, player.absX, player.absY, player.heightLevel, hunter.reward, hunted.hunter);
		sendMessage();
		player.inHowlOfDeath = false;
		player.howlOfDeath = null;
	}
	
	/**
	 * Sends a message to all players stating how many players are left in the hunt.
	 */
	private void sendMessage(){
		Set<Integer> keys = playerTargets.keySet();
		int size = playerTargets.size();
		for(int key : keys){
			Client player = (Client)PlayerHandler.players[key];
			if(player == null)
				continue;
			player.sendMessage("There are " + size + " players left in the hunt.");
		}
	}
	
	/**
	 * Processes the mini-game. This is mainly used to display updated target information to the players.
	 */
	public void process(){
		Set<Integer> players = playerTargets.keySet();
		for(int id : players){
			Client player = (Client)PlayerHandler.players[id];
			if(player == null)
				continue;
			int targetId = playerTargets.get(id).target;
			String target = PlayerHandler.players[targetId] == null ? "null" : PlayerHandler.players[targetId].playerName2;
			player.getPA().sendText("Your target is " + target, 6570);
			player.getPA().sendText("", 6572);
			player.getPA().sendText("", 6664);
			player.getPA().walkableInterface(6673);
		}
	}
	
	/**
	 * Finds a person's target.
	 * @param id The id of the attacker.
	 * @return The id of the target.
	 */
	public int getTargetId(int id){
		if(!playerTargets.containsKey(id))
			return -1;
		return playerTargets.get(id).target;
	}
}
class CityCoords{
	private ArrayList<City> cities;
	
	public CityCoords(){
		cities = new ArrayList<City>();
	}
	
	public void addCoords(int x, int y, int z){
		cities.add(new City(x, y, z));
	}
	
	public City getRandomCoords(){
		return cities.get(Misc.random(cities.size() - 1));
	}
}
class City{
	public int x, y, z;
	
	public City(int x, int y, int z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
}
class Hunter{
	public int target, hunter, reward;
	
	public Hunter(int target, int hunter){
		this.target = target;
		this.hunter = hunter;
		this.reward = 0;
	}
	public Hunter(int target, int hunter, int reward){
		this.target = target;
		this.hunter = hunter;
		this.reward = reward;
	}
}