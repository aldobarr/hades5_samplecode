package server.model.minigames;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import server.Config;
import server.Server;
import server.clip.region.Region;
import server.model.items.bank.BankItem;
import server.model.items.bank.Tab;
import server.model.npcs.NPCHandler;
import server.model.players.Client;
import server.util.Misc;

/**
 * @author hadesflames
 */
public class CastleWars{

	/*
	 * Game timers.
	 */
	private static final int GAME_TIMER = 200; // X * 3 = time in seconds.
	private static final int GAME_START_TIMER = 40; // X * 3 = time in seconds.
	private static final int BURN_TIME = 5; // time in seconds.
	public static Barricade barricades[][] = {new Barricade[10], new Barricade[10]};
	public static int num_cades[] = {0, 0};
	public static boolean flags[] = {false, false};
	public static DroppedFlag saraDrop = null;
	public static DroppedFlag zammyDrop = null;
	public static boolean gameInProgress = false;
	/*
	 * Hashmap for the waitingroom players
	 */
	private static HashMap<Client, Integer> waitingRoom = new HashMap<Client, Integer>();
	/*
	 * hashmap for the gameRoom players
	 */
	public static HashMap<Client, Integer> gameRoom = new HashMap<Client, Integer>();
	/*
	 * The coordinates for the waitingRoom both sara/zammy
	 */
	private static final int WAIT_ROOM[][] = {{2377, 9485}, // sara
	{2421, 9524}// zammy
	};
	/*
	 * The coordinates for the gameRoom both sara/zammy
	 */
	private static final int GAME_ROOM[][] = {{2426, 3076}, // sara
	{2372, 3131}// zammy
	};
	public static final int FLAG_STANDS[][] = {{2429, 3074}, // sara{X-Coord,
																// Y-Coord)
	{2370, 3133}// zammy
	};
	/*
	 * Scores for saradomin and zamorak!
	 */
	private static int scores[] = {0, 0};
	/*
	 * Booleans to check if a team's flag is safe
	 */
	public static int zammyFlag = 0;
	public static int saraFlag = 0;
	/*
	 * Zamorak and saradomin banner/capes item ID's
	 */
	public static final int SARA_BANNER = 4037;
	public static final int ZAMMY_BANNER = 4039;
	public static final int SARA_CAPE = 4041;
	public static final int ZAMMY_CAPE = 4042;
	/*
     * 
     */
	private static int properTimer = 0;
	private static int timeRemaining = -1;
	private static int gameStartTimer = GAME_START_TIMER;

	/**
	 * Method we use to add someone to the waitinroom in a different method,
	 * this will filter out some error messages
	 * 
	 * @param player
	 *            the player that wants to join
	 * @param team
	 *            the team!
	 */
	public static void addToWaitRoom(Client player, int team){
		if(player == null || player.isWearingRing || player.isNpc){
			return;
		}else if(player.playerEquipment[player.playerHat] > 0 || player.playerEquipment[player.playerCape] > 0){
			player.sendMessage("You may not wear capes or helmets inside of castle wars.");
			return;
		}
		if(team == 3 && (!player.getPA().checkTele(WAIT_ROOM[0][0], WAIT_ROOM[0][1]) || !player.getPA().checkTele(WAIT_ROOM[1][0], WAIT_ROOM[1][1])))
			return;
		else if(team < 3 && !player.getPA().checkTele(WAIT_ROOM[team - 1][0], WAIT_ROOM[team - 1][1]))
			return;
		player.getPA().closeAllWindows();
		toWaitingRoom(player, team);
	}

	/**
	 * Method we use to transfer to player from the outside to the waitingroom
	 * (:
	 * 
	 * @param player
	 *            the player that wants to join
	 * @param team
	 *            team he wants to be in - team = 1 (saradomin), team = 2
	 *            (zamorak), team = 3 (random)
	 */
	public static void toWaitingRoom(Client player, int team){
		if(team == 1){
			if(getSaraPlayers() > getZammyPlayers() && getSaraPlayers() > 0){
				player.sendMessage("The saradomin team is full, try again later!");
				return;
			}
			if(getZammyPlayers() >= getSaraPlayers() || getSaraPlayers() == 0){
				player.sendMessage("You have been added to the @red@Saradomin@bla@ team.");
				player.sendMessage("Next Game Begins In:@red@ " + ((gameStartTimer * 3) + (timeRemaining * 3)) + " @bla@seconds.");
				synchronized(player){
					player.inCwWait = true;
					player.getPA().movePlayer(WAIT_ROOM[team - 1][0] + Misc.random(5), WAIT_ROOM[team - 1][1] + Misc.random(5), 0);
				}
				addCapes(player, SARA_CAPE);
				waitingRoom.put(player, team);
			}
		}else if(team == 2){
			if(getZammyPlayers() > getSaraPlayers() && getZammyPlayers() > 0){
				player.sendMessage("The zamorak team is full, try again later!");
				return;
			}
			if(getZammyPlayers() <= getSaraPlayers() || getZammyPlayers() == 0){
				player.sendMessage("You have been added to the @red@Zamorak@bla@ team.");
				player.sendMessage("Next Game Begins In:@red@ " + ((gameStartTimer * 3) + (timeRemaining * 3)) + " @bla@seconds.");
				synchronized(player){
					player.inCwWait = true;
					player.getPA().movePlayer(WAIT_ROOM[team - 1][0] + Misc.random(5), WAIT_ROOM[team - 1][1] + Misc.random(5), 0);
				}
				addCapes(player, ZAMMY_CAPE);
				waitingRoom.put(player, team);
			}
		}else if(team == 3){
			toWaitingRoom(player, getZammyPlayers() > getSaraPlayers() ? 1 : 2);
			return;
		}
	}

	/**
	 * Method to add score to scoring team
	 * 
	 * @param player
	 *            the player who scored
	 * @param banner
	 *            banner id!
	 */
	public static void returnFlag(Client player, int wearItem){
		if(player == null){
			return;
		}
		if(wearItem != SARA_BANNER && wearItem != ZAMMY_BANNER){
			return;
		}
		int team = gameRoom.get(player);
		int objectId = -1;
		int objectTeam = -1;
		switch(team){
			case 1:
				if(wearItem == SARA_BANNER){
					setSaraFlag(0);
					objectId = 4902;
					objectTeam = 0;
					player.sendMessage("returned the sara flag!");
				}else{
					objectId = 4903;
					objectTeam = 1;
					setZammyFlag(0);
					scores[0]++; // upping the score of a team; team 0 = sara,
									// team
									// 1 = zammy
					player.sendMessage("The team of Saradomin scores 1 point!");
				}
				break;
			case 2:
				if(wearItem == ZAMMY_BANNER){
					setZammyFlag(0);
					objectId = 4903;
					objectTeam = 1;
					player.sendMessage("returned the zammy flag!");
				}else{
					objectId = 4902;
					objectTeam = 0;
					setSaraFlag(0);
					scores[1]++; // upping the score of a team; team 0 = sara,
									// team
									// 1 = zammy
					player.sendMessage("The team of Zamorak scores 1 point!");
				}
				break;
		}
		changeFlagObject(objectId, objectTeam);
		player.getPA().createPlayerHints(10, -1);
		synchronized(player){
			player.playerEquipment[player.playerWeapon] = -1;
			player.playerEquipmentN[player.playerWeapon] = 0;
			player.getItems().updateSlot(3);
			player.appearanceUpdateRequired = true;
			player.updateRequired = true;
			player.inventory.resetItems(3214);
		}
	}

	/**
	 * Method that will capture a flag when being taken by the enemy team!
	 * 
	 * @param player
	 *            the player who returned the flag
	 * @param team
	 */
	public static void captureFlag(Client player){
		if(player.inventory.freeSlots() <= 0){
			player.sendMessage("You do not have enough inventory space to do that.");
			return;
		}
		int team = gameRoom.get(player);
		if(team == 2 && saraFlag == 0){ // sara flag
			setSaraFlag(1);
			addFlag(player, SARA_BANNER);
			createHintIcon(player, 1);
			changeFlagObject(4377, 0);
		}
		if(team == 1 && zammyFlag == 0){
			setZammyFlag(1);
			addFlag(player, ZAMMY_BANNER);
			createHintIcon(player, 2);
			changeFlagObject(4378, 1);
		}
	}

	/**
	 * Method that will add the flag to a player's weapon slot
	 * 
	 * @param player
	 *            the player who's getting the flag
	 * @param flagId
	 *            the banner id.
	 */
	public static void addFlag(Client player, int flagId){
		if(player.inventory.freeSlots() <= 0){
			player.sendMessage("You do not have enough inventory space to do that.");
			return;
		}
		synchronized(player){
			if(player.playerEquipment[player.playerWeapon] > 0)
				player.getItems().removeItem(player.playerEquipment[player.playerWeapon], player.playerWeapon);
			player.playerEquipment[player.playerWeapon] = flagId;
			player.playerEquipmentN[player.playerWeapon] = 1;
			player.getItems().updateSlot(player.playerWeapon);
			player.appearanceUpdateRequired = true;
			player.updateRequired = true;
		}
	}

	/**
	 * Method we use to handle the flag dropping
	 * 
	 * @param player
	 *            the player who dropped the flag/died
	 * @param flagId
	 *            the flag item ID
	 */
	public static void dropFlag(Client player, int flagId){
		int object = -1;
		if(flagId == SARA_BANNER && saraDrop != null)
			return;
		else if(flagId == ZAMMY_BANNER && zammyDrop != null)
			return;
		switch(flagId){
			case SARA_BANNER: // sara
				setSaraFlag(2);
				object = 4900;
				flags[1] = true;
				saraDrop = new DroppedFlag(Misc.currentTimeSeconds() + 3, player.absX, player.absY, player.heightLevel);
				createFlagHintIcon(player.getX(), player.getY());
				break;
			case ZAMMY_BANNER: // zammy
				setZammyFlag(2);
				object = 4901;
				flags[0] = true;
				zammyDrop = new DroppedFlag(Misc.currentTimeSeconds() + 3, player.absX, player.absY, player.heightLevel);
				createFlagHintIcon(player.getX(), player.getY());
				break;
		}
		Iterator<Client> iterator = gameRoom.keySet().iterator();
		while(iterator.hasNext()){
			Client teamPlayer = iterator.next();
			teamPlayer.getPA().object(object, player.getX(), player.getY(), 0, 10);
		}
	}

	/**
	 * Method we use to pickup the flag when it was dropped/lost
	 * 
	 * @param Player
	 *            the player who's picking it up
	 * @param objectId
	 *            the flag object id.
	 */
	public static void pickupFlag(Client player){
		int flag = 0;
		if(player.objectId == 4900 && saraDrop == null){
			player.getPA().object(-1, player.objectX, player.objectY, 0, 10);
			return;
		}
		if(player.objectId == 4901 && zammyDrop == null){
			player.getPA().object(-1, player.objectX, player.objectY, 0, 10);
			return;
		}
		if(player.objectId == 4900 && saraDrop.z != player.heightLevel)
			return;
		if(player.objectId == 4901 && zammyDrop.z != player.heightLevel)
			return;
		switch(player.objectId){
			case 4900: // sara
				if(!flags[1])
					return;
				flag = 1;
				setSaraFlag(1);
				saraDrop = null;
				addFlag(player, 4037);
				break;
			case 4901: // zammy
				if(!flags[0])
					return;
				setZammyFlag(1);
				zammyDrop = null;
				addFlag(player, 4039);
				break;
		}
		flags[flag] = false;
		createHintIcon(player, (gameRoom.get(player) == 1) ? 2 : 1);
		Iterator<Client> iterator = gameRoom.keySet().iterator();
		while(iterator.hasNext()){
			Client teamPlayer = iterator.next();
			teamPlayer.getPA().createObjectHints(player.objectX, player.objectY, 170, -1);
			teamPlayer.getPA().object(-1, player.objectX, player.objectY, 0, 10);
		}
	}

	/**
	 * Hint icons appear to your team when a enemy steals flag
	 * 
	 * @param player
	 *            the player who took the flag
	 * @param t
	 *            team of the opponent team. (:
	 */
	public static void createHintIcon(Client player, int t){
		Iterator<Client> iterator = gameRoom.keySet().iterator();
		while(iterator.hasNext()){
			Client teamPlayer = iterator.next();
			teamPlayer.getPA().createPlayerHints(10, -1);
			if(gameRoom.get(teamPlayer) == t){
				teamPlayer.getPA().createPlayerHints(10, player.playerId);
				teamPlayer.getPA().requestUpdates();
			}
		}
	}

	/**
	 * Hint icons appear to your team when a enemy steals flag
	 * 
	 * @param player
	 *            the player who took the flag
	 * @param t
	 *            team of the opponent team. (:
	 */
	public static void createFlagHintIcon(int x, int y){
		Iterator<Client> iterator = gameRoom.keySet().iterator();
		while(iterator.hasNext()){
			Client teamPlayer = iterator.next();
			teamPlayer.getPA().createObjectHints(x, y, 170, 2);
		}
	}

	/**
	 * This method is used to get the teamNumber of a certain player
	 * 
	 * @param player
	 * @return
	 */
	public static int getTeamNumber(Client player){
		if(player == null){
			return -1;
		}
		if(gameRoom.containsKey(player)){
			return gameRoom.get(player);
		}
		return -1;
	}

	/**
	 * The leaving method will be used on click object or log out
	 * 
	 * @param player
	 *            player who wants to leave
	 */
	public static void leaveWaitingRoom(Client player){
		if(player == null)
			return;
		player.getPA().closeAllWindows();
		deleteGameItems(player, false);
		if(waitingRoom.containsKey(player)){
			waitingRoom.remove(player);
			player.getPA().createPlayerHints(10, -1);
			player.sendMessage("You left your team!");
		}
		int x = 2439 + Misc.random(4), y = 3085 + Misc.random(5);
		synchronized(player){
			player.inCwWait = false;
			player.absX = x;
			player.absY = y;
			player.heightLevel = 0;
			player.getPA().movePlayer(x, y, 0);
		}
	}

	public static void process(){
		droppedFlags();
		if(properTimer > 0){
			properTimer--;
			return;
		}else
			properTimer = 4;
		if(gameStartTimer > 0 && timeRemaining <= 0){
			gameStartTimer--;
			updatePlayers();
		}else if(gameStartTimer == 0){
			startGame();
		}
		if(timeRemaining > 0){
			timeRemaining--;
			updateInGamePlayers();
			updatePlayers();
			burningCades();
		}else if(timeRemaining <= 0 || (getZammyPlayers2() <= 0 || getSaraPlayers2() <= 0))
			endGame();
	}

	/**
	 * Method we use to update the player's interface in the waiting room
	 */
	public static void updatePlayers(){
		Iterator<Client> iterator = waitingRoom.keySet().iterator();
		while(iterator.hasNext()){
			Client player = iterator.next();
			if(player != null){
				player.getPA().sendText("Next Game Begins In:@red@ " + (((gameStartTimer * 3) + 3) + (timeRemaining * 3)) + " @whi@seconds.", 6570);
				player.getPA().sendText((isInZammy(player) ? "@yel@" : "@whi@") + "Zamorak Players: @red@" + getZammyPlayers(), 6572);
				player.getPA().sendText((isInSara(player) ? "@yel@" : "") + "Saradomin Players: @red@" + getSaraPlayers(), 6664);
				player.getPA().walkableInterface(6673);
			}
		}
	}

	/**
	 * Method we use the update the player's interface in the game room
	 */
	public static void updateInGamePlayers(){
		if(getSaraPlayers2() > 0 && getZammyPlayers2() > 0){
			Iterator<Client> iterator = gameRoom.keySet().iterator();
			while(iterator.hasNext()){
				Client player = iterator.next();
				int config;
				if(player == null){
					continue;
				}
				player.getPA().walkableInterface(11146);
				player.getPA().sendText("Zamorak = " + scores[1], 11147);
				player.getPA().sendText(scores[0] + " = Saradomin", 11148);
				player.getPA().sendText(timeRemaining * 3 + " secs", 11155);
				config = (2097152 * saraFlag);
				player.getPA().sendFrame87(378, config);
				config = (2097152 * zammyFlag); // flags 0 = safe 1 = taken 2 =
												// dropped
				player.getPA().sendFrame87(377, config);
			}
		}
	}
	
	/**
	 * Take care of any dropped flags.
	 */
	public static void droppedFlags(){
		if(!gameInProgress)
			return;
		if(zammyDrop != null){
			if(Misc.currentTimeSeconds() >= zammyDrop.time){
				Iterator<Client> iterator = gameRoom.keySet().iterator();
				while(iterator.hasNext()){
					Client teamPlayer = iterator.next();
					teamPlayer.getPA().createObjectHints(zammyDrop.x, zammyDrop.y, 170, -1);
					teamPlayer.getPA().object(-1, zammyDrop.x, zammyDrop.y, 0, 10);
				}
				setZammyFlag(0);
				changeFlagObject(4903, 1);
				zammyDrop = null;
			}
		}
		if(saraDrop != null){
			if(Misc.currentTimeSeconds() >= saraDrop.time){
				Iterator<Client> iterator = gameRoom.keySet().iterator();
				while(iterator.hasNext()){
					Client teamPlayer = iterator.next();
					teamPlayer.getPA().createObjectHints(saraDrop.x, saraDrop.y, 170, -1);
					teamPlayer.getPA().object(-1, saraDrop.x, saraDrop.y, 0, 10);
				}
				setSaraFlag(0);
				changeFlagObject(4902, 0);
				saraDrop = null;
			}
		}
	}

	/**
	 * Sets door clipping for the castles.
	 */
	public static void setClip(){
		Region.setClipping(2384, 3134, 0, 21546);
		Region.setClipping(2414, 3073, 0, 4104);
		Region.setClipping(2372, 3119, 0, 66690);
		Region.setClipping(2372, 3120, 0, 16416);
		Region.setClipping(2373, 3119, 0, 5130);
		Region.setClipping(2373, 3120, 0, 20520);
		Region.setClipping(2384, 3134, 0, 21546);
		Region.setClipping(2385, 3134, 0, 65664);
		Region.setClipping(2426, 3088, 0, 82080);
		Region.setClipping(2426, 3087, 0, 66690);
		Region.setClipping(2427, 3088, 0, 20520);
		Region.setClipping(2427, 3087, 0, 5130);
		Region.setClipping(2415, 3073, 0, 83106);
		Region.setClipping(2414, 3073, 0, 4104);
	}

	/**
	 * Method that will start the game when there's enough players.
	 */
	public static void startGame(){
		if(getSaraPlayers() < 1 || getZammyPlayers() < 1){
			gameStartTimer = GAME_START_TIMER;
			return;
		}
		gameInProgress = true;
		setClip();
		gameStartTimer = GAME_START_TIMER;
		timeRemaining = GAME_TIMER;
		Iterator<Client> iterator = waitingRoom.keySet().iterator();
		while(iterator.hasNext()){
			Client player = iterator.next();
			int team = waitingRoom.get(player);
			if(player == null){
				continue;
			}
			synchronized(player){
				player.inCwWait = false;
				player.inCwGame = player.inCWBase = true;
				player.castleWarsTeam = team;
				player.getPA().movePlayer(GAME_ROOM[team - 1][0] + Misc.random(3), GAME_ROOM[team - 1][1] - Misc.random(3), 1);
			}
			player.getPA().walkableInterface(-1);
			gameRoom.put(player, team);
		}
		waitingRoom.clear();
	}

	/**
	 * Method we use to end an ongoing cw game.
	 */
	public static void endGame(){
		gameInProgress = false;
		Iterator<Client> iterator = gameRoom.keySet().iterator();
		int winningTeam = scores[0] == scores[1] ? 0 : scores[0] > scores[1] ? 1 : 2;
		while(iterator.hasNext()){
			Client player = iterator.next();
			if(player == null)
				continue;
			synchronized(player){
				player.getPA().resetOverLoad();
				player.stopMovement();
				player.getCombat().resetPlayerAttack();
				player.getCombat().resetPrayers();
				if(player.inTrade)
					player.getTradeAndDuel().declineTrade(true);
				player.getPA().removeAllWindows();
			}
			for(int i = 0; i <= Config.NUM_SKILLS; i++){
				synchronized(player){
					player.playerLevel[i] = player.getPA().getLevelForXP(player.playerXP[i]);
					player.getPA().refreshSkill(i);
				}
			}
			int team = gameRoom.get(player);
			deleteGameItems(player, false);
			synchronized(player){
				player.cwGames++;
				player.freezeTimer = 3;
				player.castleWarsTeam = -1;
				player.inCwGame = player.inCWBase = false;
				player.getPA().movePlayer(2440 + Misc.random(3), 3089 - Misc.random(3), 0);
			}
			player.sendMessage("[@red@CASTLE WARS@bla@] The Castle Wars Game has ended!");
			player.sendMessage("[@red@CASTLE WARS@bla@] Kills: @red@ " + player.cwKills + " @bla@Deaths:@red@ " + player.cwDeaths + "@bla@ Games Played: @red@" + player.cwGames + "@bla@.");
			player.getPA().createPlayerHints(10, -1);
			if(winningTeam == 0){
				synchronized(player){
					player.inventory.addItem(4067, 1, -1);
				}
				player.sendMessage("Tie game! You gain 1 CastleWars ticket!");
			}else if(winningTeam == team){
				synchronized(player){
					player.inventory.addItem(4067, 2, -1);
				}
				player.sendMessage("You won the CastleWars Game. You received 2 CastleWars Tickets!");
			}else
				player.sendMessage("You lost the CastleWars Game.");
		}
		resetGame();
	}

	/**
	 * reset the game variables
	 */
	public static void resetGame(){
		changeFlagObject(4902, 0);
		changeFlagObject(4903, 1);
		setSaraFlag(0);
		setZammyFlag(0);
		scores[0] = 0;
		scores[1] = 0;
		num_cades[0] = 0;
		num_cades[1] = 0;
		saraDrop = null;
		zammyDrop = null;
		for(int i = 0; i < 2; i++){
			for(int j = 0; j < 10; j++){
				if(barricades[i][j] != null){
					Server.npcHandler.removeNPC(barricades[i][j].id);
					barricades[i][j] = null;
				}
			}
		}
		timeRemaining = -1;
		gameRoom.clear();
	}

	/**
	 * When player logs in, make sure they are not considered to be in a cw
	 * game. Also makes sure they are outside of castle wars areas.
	 * 
	 * @param player
	 *            The Client object of the player we are checking.
	 */
	public static void removeLoggedInPlayer(Client player){
		player.inCwGame = player.inCWBase = false;
		if(isInCw(player)){
			synchronized(gameRoom){
				gameRoom.remove(player);
			}
		}
		if(player.isInCastleWars() && !player.isInCastleWarsStart()){
			player.absX = 2440;
			player.absY = 3089;
			player.heightLevel = 0;
			player.getPA().movePlayer(2440, 3089, 0);
		}
	}

	/**
	 * Method we use to remove a player from the game
	 * 
	 * @param player
	 *            the player we want to be removed
	 */
	public static void removePlayerFromCw(Client player){
		if(player == null)
			return;
		if(gameRoom.containsKey(player)){
			/*
			 * Logging/leaving with flag
			 */
			if(player.getItems().playerHasEquipped(SARA_BANNER)){
				synchronized(player){
					player.getItems().removeItem(player.playerEquipment[3], 3);
				}
				setSaraFlag(0); // safe flag
			}else if(player.getItems().playerHasEquipped(ZAMMY_BANNER)){
				synchronized(player){
					player.getItems().removeItem(player.playerEquipment[3], 3);
				}
				setZammyFlag(0); // safe flag
			}
			deleteGameItems(player, false);
			synchronized(player){
				player.overloaded = new int[][]{{0, 0}, {1, 0}, {2, 0}, {4, 0}, {6, 0}};
				player.overloadedBool = false;
				player.stopMovement();
				player.getCombat().resetPlayerAttack();
				player.getCombat().resetPrayers();
				if(player.inTrade)
					player.getTradeAndDuel().declineTrade(true);
				player.getPA().removeAllWindows();
			}
			for(int i = 0; i <= Config.NUM_SKILLS; i++){
				synchronized(player){
					player.playerLevel[i] = player.getPA().getLevelForXP(player.playerXP[i]);
					player.getPA().refreshSkill(i);
				}
			}
			synchronized(player){
				player.cwGames++;
				player.freezeTimer = 3;
				player.castleWarsTeam = -1;
				player.inCwGame = player.inCWBase = false;
				player.getPA().movePlayer(2440 + Misc.random(3), 3089 - Misc.random(3), 0);
			}
			player.sendMessage("[@red@CASTLE WARS@bla@] The Casle Wars Game has ended for you!");
			player.sendMessage("[@red@CASTLE WARS@bla@] Kills: @red@ " + player.cwKills + " @bla@Deaths:@red@ " + player.cwDeaths + "@bla@.");
			player.getPA().createPlayerHints(10, -1);
			gameRoom.remove(player);
		}
		if(getZammyPlayers2() <= 0 || getSaraPlayers2() <= 0){
			endGame();
		}
	}

	public static void removePlayerFromCw(Client player, boolean login){
		if(player == null)
			return;
		deleteGameItems(player, false);
		synchronized(player){
			player.inCwGame = player.inCWBase = false;
			player.absX = 2440;
			player.absY = 3089;
			player.heightLevel = 0;
			player.getPA().movePlayer(2440, 3089, 0);
		}
		player.getPA().createPlayerHints(10, -1);
	}

	/**
	 * Will add a cape to a player's equip
	 * 
	 * @param player
	 *            the player
	 * @param capeId
	 *            the capeId
	 */
	public static void addCapes(Client player, int capeId){
		synchronized(player){
			player.playerEquipment[player.playerCape] = capeId;
			player.playerEquipmentN[player.playerCape] = 1;
			player.getItems().updateSlot(player.playerCape);
			player.appearanceUpdateRequired = true;
			player.updateRequired = true;
		}
	}

	/**
	 * This method will delete all items received in game. Easy to add items to
	 * the array. (:
	 * 
	 * @param player
	 *            the player who want the game items deleted from.
	 */
	public static void deleteGameItems(Client player, boolean bank){
		if(isInCw(player) || isInCwWait(player))
			return;
		switch(player.playerEquipment[3]){
			case 4037:
			case 4039:
				while(player.playerEquipmentN[3] > 0){
					synchronized(player){
						player.getItems().deleteEquipment(player.playerEquipment[3], 3);
					}
				}
				break;
		}
		switch(player.playerEquipment[1]){
			case 4042:
			case 4041:
				while(player.playerEquipmentN[1] > 0){
					synchronized(player){
						player.getItems().deleteEquipment(player.playerEquipment[1], 1);
					}
				}
				break;
		}
		int items[] = {4049, 4050, 1265, 1266, 4045, 4046, 4053, 4054, 4042, 4041, 4037, 4038, 4039, 4040};
		for(int i = 0; i < items.length; i++){
			if(player.inventory.hasItem(items[i])){
				synchronized(player){
					player.inventory.deleteItem(items[i], player.inventory.getItemCount(items[i]));
				}
			}
		}
		if(bank){
			HashMap<Integer, ArrayList<BankItem>> remove = new HashMap<Integer, ArrayList<BankItem>>();
			for(int i = 0; i < player.bank.tabs.size(); i++){
				Tab tab = player.bank.tabs.get(i);
				for(int j = 0; j < tab.getNumItems(); j++){
					BankItem item = tab.get(j);
					for(int k = 0; k < items.length; k++){
						if(items[k] + 1 == item.id){
							ArrayList<BankItem> thisTab = remove.containsKey(i) ? remove.get(i) : new ArrayList<BankItem>();
							thisTab.add(item);
							remove.remove(i);
							remove.put(i, thisTab);
						}
					}
				}
			}
			Iterator<Entry<Integer, ArrayList<BankItem>>> iter = remove.entrySet().iterator();
			while(iter.hasNext()){
				Entry<Integer, ArrayList<BankItem>> next = iter.next();
				int tabId = next.getKey();
				ArrayList<BankItem> tabItems = next.getValue();
				if(player.bank.tabs.size() < tabId){
					for(BankItem item : tabItems){
						synchronized(player){
							player.bank.tabs.get(tabId).remove(item);
						}
					}
				}
			}
		}
	}

	/**
	 * Do not use this one.
	 * 
	 * @return don't use.
	 */
	public static int getZammyPlayers2(){
		int players = 0;
		Iterator<Integer> iterator = gameRoom.values().iterator();
		while(iterator.hasNext()){
			if(iterator.next() == 2){
				players++;
			}
		}
		return players;
	}

	/**
	 * Do not use this one.
	 * 
	 * @return don't use.
	 */
	public static int getSaraPlayers2(){
		int players = 0;
		Iterator<Integer> iterator = gameRoom.values().iterator();
		while(iterator.hasNext()){
			if(iterator.next() == 1){
				players++;
			}
		}
		return players;
	}

	/**
	 * Method we use to get the zamorak players
	 * 
	 * @return the amount of players in the zamorakian team!
	 */
	public static int getZammyPlayers(){
		int players = 0;
		Iterator<Integer> iterator = (!waitingRoom.isEmpty()) ? waitingRoom.values().iterator() : gameRoom.values().iterator();
		while(iterator.hasNext()){
			if(iterator.next() == 2){
				players++;
			}
		}
		return players;
	}

	/**
	 * Method used to check if a certain player is on the Zamorakian team.
	 * 
	 * @return true if the player is on the Zamorakian team, false if not.
	 */
	public static boolean isInZammy(Client player){
		if(!gameRoom.containsKey(player) && !waitingRoom.containsKey(player))
			return false;
		HashMap<Client, Integer> temp = gameRoom.containsKey(player) ? gameRoom : waitingRoom;
		return temp.get(player) == 2 ? true : false;
	}

	/**
	 * Method used to check if a certain player is on the Saradomin team.
	 * 
	 * @return true if the player is on the Saradomin team, false if not.
	 */
	public static boolean isInSara(Client player){
		if(!gameRoom.containsKey(player) && !waitingRoom.containsKey(player))
			return false;
		HashMap<Client, Integer> temp = gameRoom.containsKey(player) ? gameRoom : waitingRoom;
		return temp.get(player) == 1 ? true : false;
	}

	/**
	 * Method we use to get the saradomin players!
	 * 
	 * @return the amount of players in the saradomin team!
	 */
	public static int getSaraPlayers(){
		int players = 0;
		Iterator<Integer> iterator = (!waitingRoom.isEmpty()) ? waitingRoom.values().iterator() : gameRoom.values().iterator();
		while(iterator.hasNext()){
			if((Integer)iterator.next() == 1){
				players++;
			}
		}
		return players;
	}

	/**
	 * Method we use for checking if the player is in the gameRoom
	 * 
	 * @param player
	 *            who will be checking
	 * @return
	 */
	public static boolean isInCw(Client player){
		return gameRoom.containsKey(player);
	}

	/**
	 * Method we use for checking if the player is in the waitingRoom
	 * 
	 * @param player
	 *            who will be checking
	 * @return
	 */
	public static boolean isInCwWait(Client player){
		return waitingRoom.containsKey(player);
	}

	/**
	 * Method to make sara flag change status 0 = safe, 1 = taken, 2 = dropped
	 * 
	 * @param status
	 */
	public static void setSaraFlag(int status){
		saraFlag = status;
	}

	/**
	 * Method to make zammy flag change status 0 = safe, 1 = taken, 2 = dropped
	 * 
	 * @param status
	 */
	public static void setZammyFlag(int status){
		zammyFlag = status;
	}

	/**
	 * Method we use for the changing the object of the flag stands when
	 * capturing/returning flag
	 * 
	 * @param objectId
	 *            the object
	 * @param team
	 *            the team of the player
	 */
	public static void changeFlagObject(int objectId, int team){
		Iterator<Client> iterator = gameRoom.keySet().iterator();
		while(iterator.hasNext()){
			Client teamPlayer = iterator.next();
			if(teamPlayer.heightLevel == 3)
				teamPlayer.getPA().object(objectId, FLAG_STANDS[team][0], FLAG_STANDS[team][1], 0, 10);
		}
	}

	/**
	 * Method used to set barricades between the teams.
	 * 
	 * @param The
	 *            client that is putting down a barricade.
	 */
	public static void setCade(Client c){
		int team = isInZammy(c) ? 0 : 1;
		// if(team == 1 && !isInSara(c))
		// return;
		if(!isInCw(c)){
			deleteGameItems(c, true);
			return;
		}
		if(num_cades[team] >= 10){
			c.sendMessage("Your team already has 10 barricades set up!");
			return;
		}
		for(int i = 0; i < num_cades[team]; i++){
			if(barricades[team][i].absX == c.absX && barricades[team][i].absY == c.absY){
				c.sendMessage("There is already a barricade set up there!");
				return;
			}
		}
		int id = Server.npcHandler.spawnNpc3(1532, c.absX, c.absY, c.heightLevel, 0, 50, 0, 0, 0);
		if(id == -1){
			c.sendMessage("There was a problem setting up the barricade, please try again later.");
			return;
		}
		barricades[team][num_cades[team]++] = new Barricade(id, c.absX, c.absY, c.heightLevel, team);
		synchronized(c){
			c.inventory.deleteItem(4053, 1);
		}
		c.sendMessage("You set up a barricade.");
	}

	/**
	 * Method used to mark a barricade to burn.
	 * 
	 * @param id
	 *            the id of the barricade npc.
	 */
	public static void burnCade(int id){
		for(int i = 0; i < 2; i++){
			for(int j = 0; j < num_cades[i]; j++){
				if(id == barricades[i][j].id){
					barricades[i][j].burnTime = Misc.currentTimeSeconds() + BURN_TIME;
					barricades[i][j].burning = true;
					return;
				}
			}
		}
	}

	/**
	 * Method used to burn a barricade.
	 */
	private static void burningCades(){
		for(int i = 0; i < 2; i++){
			for(int j = 0; j < num_cades[i]; j++){
				if(barricades[i][j].burning && Misc.currentTimeSeconds() >= barricades[i][j].burnTime){
					NPCHandler.npcs[barricades[i][j].id].isDead = true;
					Iterator<Client> iterator = CastleWars.gameRoom.keySet().iterator();
					while(iterator.hasNext()){
						Client person = iterator.next();
						if(person != null)
							if(person.heightLevel == barricades[i][j].heightLevel)
								if(person.distanceToPoint(barricades[i][j].absX, barricades[i][j].absY) <= 60)
									person.getPA().checkObjectSpawn(-1, barricades[i][j].absX, barricades[i][j].absY, 0, 10);
					}
				}
			}
		}
	}

	/**
	 * Destroys the barricade.
	 * 
	 * @param id
	 *            the id of the barricade npc.
	 */
	public static void killCade(int id){
		boolean found = false;
		int team = -1;
		for(int i = 0; i < 2; i++){
			for(int j = 0; j < num_cades[i]; j++){
				if(barricades[i][j].id == id && !found){
					barricades[i][j] = null;
					found = true;
					team = i;
					continue;
				}else if(found){
					barricades[i][j - 1] = barricades[i][j];
					barricades[i][j] = null;
				}
			}
			if(found)
				break;
		}
		if(team == -1)
			return;
		--num_cades[team];
	}
}
class DroppedFlag{
	public int time, x, y, z;
	
	public DroppedFlag(int time, int x, int y, int z){
		this.time = time;
		this.x = x;
		this.y = y;
		this.z = z;
	}
}