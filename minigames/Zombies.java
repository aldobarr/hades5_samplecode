package server.model.minigames;

import java.util.ArrayList;
import server.Config;
import server.Server;
import server.model.npcs.NPCHandler;
import server.model.players.Client;
import server.model.players.PlayerHandler;
import server.util.Misc;
import server.world.Clan;

/**
 * 
 * @author hadesflames
 * 
 */
public class Zombies{
	// npcType HP maxHit attack defense
	public static final int GRUNT_ZOMBIES[][] = {
		// {73, 22, 7, 30, 30}, // Combat - 13 Too weak
		{74, 29, 9, 35, 34}, // Combat - 18
		{75, 35, 10, 45, 40}, // Combat - 24
		{76, 35, 11, 47, 42}, // Combat - 25
		{422, 95, 15, 110, 100}, // Combat - 79
	};
	public static final int BOSS_ZOMBIES[][] = {
		{423, 155, 22, 145, 135}, // Combat - 120
		{424, 220, 28, 175, 160}, // Combat - 159
	};
	public static final int SPAWN_POINTS[][] = {{3524, 3510}, {3454, 3468}};
	public static final int MOVE_POINT[] = {3495, 3489};
	public static final int RANGE = 2, SPAWN_TICK = 2, BOSS_TICK = 5, MAX_ZOMBIES = 75;
	public String clanId;
	public int leaderId, bossTick = BOSS_TICK, spawnedZombieCount = 0;
	public int spawnTick = SPAWN_TICK;
	public boolean leaderIsDead = false;
	public ArrayList<Integer> players = new ArrayList<Integer>();
	public ArrayList<Integer> zombies = new ArrayList<Integer>();

	public Zombies(String clanId, int leaderId){
		this.clanId = clanId;
		this.leaderId = leaderId;
	}

	/**
	 * Checks if Client one is within a certain range of Client two
	 * 
	 * @param one
	 *            The Client we are checking against.
	 * @param two
	 *            The Client used for the check
	 * @return Returns true if Client one is within the range of Client two, and
	 *         false if they are not within range.
	 */
	public static boolean withinRange(Client one, Client two){
		return one.absX >= two.absX - RANGE && one.absX <= two.absX + RANGE && one.absY >= two.absY - RANGE && one.absY <= two.absY + RANGE;
	}

	/**
	 * Checks if a player is in one of the buildings in Canifis.
	 * 
	 * @param c
	 *            The Client object of the player.
	 * @return True if the player is inside a building, false if not.
	 */
	public static boolean inHouse(Client c){
		if((c.absX >= 3491 && c.absX <= 3504 && c.absY >= 3478 && c.absY <= 3479) || (c.absX >= 3488 && c.absX <= 3504 && c.absY >= 3471 && c.absY <= 3477) || (c.absX >= 3488 && c.absX <= 3501 && c.absY == 3470) || (c.absX >= 3488 && c.absX <= 3500 && c.absY == 3469) || (c.absX >= 3488 && c.absX <= 3499 && c.absY == 3468) || (c.absX >= 3509 && c.absX <= 3516 && c.absY >= 3483 && c.absY <= 3482) || (c.absX >= 3508 && c.absX <= 3516 && c.absY >= 3481 && c.absY <= 3479) || (c.absX >= 3509 && c.absX <= 3516 && c.absY == 3478) || (c.absX >= 3509 && c.absX <= 3515 && c.absY == 3477) || (c.absX >= 3509 && c.absX <= 3514 && c.absY == 3476) || (c.absX >= 3509 && c.absX <= 3512 && c.absY >= 3474 && c.absY <= 3475) || (c.absX >= 3504 && c.absX <= 3509 && c.absY == 3498) || (c.absX >= 3504 && c.absX <= 3510 && c.absY == 3497) || (c.absX >= 3504 && c.absX <= 3511 && c.absY == 3496) || (c.absX >= 3504 && c.absX <= 3511 && c.absY >= 3494 && c.absY <= 3495) || (c.absX >= 3505 && c.absX <= 3509 && c.absY == 3493) || (c.absX >= 3505 && c.absX <= 3508 && c.absY >= 3491 && c.absY <= 3492) || (c.absX >= 3497 && c.absX <= 3503 && c.absY == 3507) || (c.absX >= 3496 && c.absX <= 3504 && c.absY >= 3503 && c.absY <= 3506) || (c.absX >= 3489 && c.absX <= 3493 && c.absY >= 3503 && c.absY <= 3505) || (c.absX >= 3485 && c.absX <= 3493 && c.absY >= 3499 && c.absY <= 3502) || (c.absX >= 3485 && c.absX <= 3492 && c.absY == 3498) || (c.absX >= 3477 && c.absX <= 3480 && c.absY == 3500) || (c.absX >= 3474 && c.absX <= 3480 && c.absY >= 3491 && c.absY <= 3499) || (c.absX == 3488 && c.absY == 3497) || (c.absX == 3497 && c.absY == 3502) || (c.absX == 3504 && c.absY == 3492) || (c.absX == 3507 && c.absY == 3480))
			return true;
		return false;
	}

	/**
	 * Checks if a pair of coordinates are in one of the buildings in Canifis.
	 * 
	 * @param x
	 *            The X coordinate.
	 * @param y
	 *            The Y coordinate.
	 * @return True if the pair of coordinates are inside a building, false if
	 *         not.
	 */
	public static boolean inHouse(int x, int y){
		if((x >= 3491 && x <= 3504 && y >= 3478 && y <= 3479) || (x >= 3488 && x <= 3504 && y >= 3471 && y <= 3477) || (x >= 3488 && x <= 3501 && y == 3470) || (x >= 3488 && x <= 3500 && y == 3469) || (x >= 3488 && x <= 3499 && y == 3468) || (x >= 3509 && x <= 3516 && y >= 3483 && y <= 3482) || (x >= 3508 && x <= 3516 && y >= 3481 && y <= 3479) || (x >= 3509 && x <= 3516 && y == 3478) || (x >= 3509 && x <= 3515 && y == 3477) || (x >= 3509 && x <= 3514 && y == 3476) || (x >= 3509 && x <= 3512 && y >= 3474 && y <= 3475) || (x >= 3504 && x <= 3509 && y == 3498) || (x >= 3504 && x <= 3510 && y == 3497) || (x >= 3504 && x <= 3511 && y == 3496) || (x >= 3504 && x <= 3511 && y >= 3494 && y <= 3495) || (x >= 3505 && x <= 3509 && y == 3493) || (x >= 3505 && x <= 3508 && y >= 3491 && y <= 3492) || (x >= 3497 && x <= 3503 && y == 3507) || (x >= 3496 && x <= 3504 && y >= 3503 && y <= 3506) || (x >= 3489 && x <= 3493 && y >= 3503 && y <= 3505) || (x >= 3485 && x <= 3493 && y >= 3499 && y <= 3502) || (x >= 3485 && x <= 3492 && y == 3498) || (x >= 3477 && x <= 3480 && y == 3500) || (x >= 3474 && x <= 3480 && y >= 3491 && y <= 3499) || (x == 3488 && y == 3497) || (x == 3497 && y == 3502) || (x == 3504 && y == 3492) || (x == 3507 && y == 3480))
			return true;
		return false;
	}

	/**
	 * Handles the game.
	 * 
	 * @param player
	 *            The player in this process tick.
	 */
	public void process(Client player){
		if(!player.inZombiesGame)
			return;
		if(leaderIsDead)
			handleLeaderDeathEffects(player);
		if(player.playerId == players.get(0) && spawnTick-- == 0){
			handleNPCSpawn();
			spawnTick = SPAWN_TICK;
		}
	}

	/**
	 * Ends the mini-game.
	 */
	public void endGame(){
		for(int id : zombies){
			if(NPCHandler.npcs[id] == null)
				continue;
			NPCHandler.npcs[id].absX = 20;
			NPCHandler.npcs[id].absY = 20;
			NPCHandler.npcs[id].updateRequired = true;
			NPCHandler.npcs[id].getRegion().removeNpc(NPCHandler.npcs[id]);
			NPCHandler.npcs[id] = null;
		}
		zombies.clear();
		if(!players.isEmpty()){
			for(int id : players){
				if(PlayerHandler.players[id] == null)
					continue;
				Client player = (Client)PlayerHandler.players[id];
				player.sendMessage("You killed " + player.zombieKills + " zombies.");
				int points = player.zombieKills / 10;
				player.sendMessage("You are rewarded " + points + " Zombie Points.");
				player.zombiePoints += points;
				player.zombieTemp = true;
				player.getPA().movePlayer(Config.RESPAWN_X, Config.RESPAWN_Y, 0);
				resetVars(player, false);
			}
			players.clear();
		}
	}

	/**
	 * Handles the Zombies minigame teleportation of all players in the clan
	 * chat.
	 * 
	 * @param c
	 *            The Client that has initiated the teleport.
	 */
	public static void handleTeleport(Client c){
		if(c.teleporting)
			return;
		if(c.clanId.isEmpty()){
			c.sendMessage("You must be in a clan chat to do that.");
			c.getPA().closeAllWindows();
			return;
		}
		if(!Server.clanChat.clans.get(c.clanId).owner.equalsIgnoreCase(c.originalName)){
			c.sendMessage("Only the clan chat owner can do that.");
			c.getPA().closeAllWindows();
			return;
		}
		if(!c.getPA().checkTele(3495, 3489))
			return;
		Clan clan = Server.clanChat.clans.get(c.clanId);
		Zombies newGame = new Zombies(c.clanId, c.playerId);
		for(int member : clan.activeMembers){
			if(member < 0)
				continue;
			Client player = (Client)PlayerHandler.players[member];
			if(player == null)
				continue;
			if(!withinRange(player, c) || !player.getPA().checkTele(3495, 3489))
				continue;
			for(int i = 0; i < player.PRAYER.length; i++){
				player.prayerActive[i] = false;
				player.getPA().sendConfig(player.PRAYER_GLOW[i], 0);
			}
			newGame.players.add(member);
			player.headIcon = -1;
			player.zombies = newGame;
			player.inZombiesGame = true;
			player.zombieTemp = true;
			player.getPA().spellTeleport(3495, 3489, 4 * c.playerId);
			player.zombieTemp = false;
			player.canSummonNomad = true;
		}
	}

	/**
	 * Checks the player's position. If they are outside of Canifis, they begin
	 * to suffer attrition.
	 */
	public void handlePlayerPositioning(Client player){
		process(player);
		if(!player.inZombiesGame)
			return;
		if(player.inCanifis()){
			player.zombieAttrition = 0;
			player.zombieAttritionDelay = 12;
			player.zombieAttritionTick = 0;
			return;
		}
		if(player.zombieAttritionDelay-- == 0){
			player.sendMessage("A magical force damages you.");
			if(player.zombieAttrition == 0)
				player.zombieAttrition = 5;
			if(player.playerLevel[player.playerHitpoints] - player.zombieAttrition < 0)
				player.zombieAttrition = player.playerLevel[player.playerHitpoints];
			player.handleHitMask(player.zombieAttrition);
			player.playerLevel[player.playerHitpoints] -= player.zombieAttrition;
			player.zombieAttritionDelay = 8;
			player.getPA().refreshSkill(player.playerHitpoints);
			if(player.zombieAttritionTick-- == 0){
				player.zombieAttritionTick = 2;
				player.zombieAttrition++;
			}
		}
	}

	/**
	 * Clears the Zombie mini-game specific variables for the player that has
	 * died, and remove him from the players array.
	 * 
	 * @param c
	 *            The Client that died.
	 */
	public void resetVars(Client c, boolean ignoreStats){
		c.zombies = null;
		c.inZombiesGame = false;
		c.zombieAttrition = 0;
		c.zombieAttritionDelay = 12;
		c.zombieAttritionTick = 0;
		c.zombieEffect = -1;
		c.zombieTemp = false;
		c.zombieKills = 0;
		if(!ignoreStats){
			c.playerLevel[c.playerHitpoints] = c.getLevelForXP(c.playerXP[c.playerHitpoints]);
			c.playerLevel[c.playerDefence] = c.getLevelForXP(c.playerXP[c.playerDefence]);
			c.playerLevel[c.playerAttack] = c.getLevelForXP(c.playerXP[c.playerAttack]);
			c.playerLevel[c.playerStrength] = c.getLevelForXP(c.playerXP[c.playerStrength]);
			c.playerLevel[c.playerRanged] = c.getLevelForXP(c.playerXP[c.playerRanged]);
			c.playerLevel[c.playerMagic] = c.getLevelForXP(c.playerXP[c.playerMagic]);
			c.playerLevel[c.playerPrayer] = c.getLevelForXP(c.playerXP[c.playerPrayer]);
		}
		c.isRunning2 = true;
		c.getPA().sendConfig(173, 1);
	}

	/**
	 * Handles the player teleporting to fight Nomad.
	 * 
	 * @param c
	 *            The Client that is leaving the game.
	 */
	public void handleNomadSummon(Client c){
		players.remove((Object)c.playerId);
		if(players.isEmpty())
			endGame();
		if(c.playerId == leaderId)
			handleLeaderDeath();
		c.sendMessage("You killed " + c.zombieKills + " zombies.");
		int points = c.zombieKills / 10;
		c.sendMessage("You are rewarded " + points + " Zombie Points.");
		c.zombiePoints += points;
		resetVars(c, true);
	}
	
	/**
	 * Handles the death of a player.
	 * 
	 * @param c
	 *            The Client that has died.
	 */
	public void handleDeath(Client c){
		players.remove((Object)c.playerId);
		if(players.isEmpty())
			endGame();
		if(c.playerId == leaderId)
			handleLeaderDeath();
		c.sendMessage("You killed " + c.zombieKills + " zombies.");
		int points = c.zombieKills / 10;
		c.sendMessage("You are rewarded " + points + " Zombie Points.");
		c.zombiePoints += points;
		resetVars(c, false);
	}

	/**
	 * Handles the death of the leader, and the effects of this.
	 */
	public void handleLeaderDeath(){
		leaderIsDead = true;
		for(int id : players){
			Client player = (Client)PlayerHandler.players[id];
			if(player == null)
				continue;
			player.sendMessage("Your leader has died. His death has left you and your comrades in a devistated state.");
			player.zombieEffect = Misc.random(6);
		}
	}

	/**
	 * Handles the effects of the leader's death for the specified player.
	 * 
	 * @param c
	 *            The Client we're handling.
	 */
	public void handleLeaderDeathEffects(Client c){
		switch(c.zombieEffect){
			case 0: // Lowers defense by 30%.
				if(c.playerLevel[c.playerDefence] > c.getLevelForXP(c.playerXP[c.playerDefence]) * 0.7)
					c.playerLevel[c.playerDefence] = (int)(c.getLevelForXP(c.playerXP[c.playerDefence]) * 0.7);
				c.getPA().refreshSkill(c.playerDefence);
				break;
			case 1: // Lowers defense and magic by 15% each.
				if(c.playerLevel[c.playerDefence] > c.getLevelForXP(c.playerXP[c.playerDefence]) * 0.85)
					c.playerLevel[c.playerDefence] = (int)(c.getLevelForXP(c.playerXP[c.playerDefence]) * 0.85);
				if(c.playerLevel[c.playerMagic] > c.getLevelForXP(c.playerXP[c.playerMagic]) * 0.85)
					c.playerLevel[c.playerMagic] = (int)(c.getLevelForXP(c.playerXP[c.playerMagic]) * 0.85);
				c.getPA().refreshSkill(c.playerDefence);
				c.getPA().refreshSkill(c.playerMagic);
				break;
			case 2: // Lowers defense and strength by 15% each.
				if(c.playerLevel[c.playerDefence] > c.getLevelForXP(c.playerXP[c.playerDefence]) * 0.85)
					c.playerLevel[c.playerDefence] = (int)(c.getLevelForXP(c.playerXP[c.playerDefence]) * 0.85);
				if(c.playerLevel[c.playerStrength] > c.getLevelForXP(c.playerXP[c.playerStrength]) * 0.85)
					c.playerLevel[c.playerStrength] = (int)(c.getLevelForXP(c.playerXP[c.playerStrength]) * 0.85);
				c.getPA().refreshSkill(c.playerDefence);
				c.getPA().refreshSkill(c.playerStrength);
				break;
			case 3: // Lowers defense and attack by 15% each.
				if(c.playerLevel[c.playerDefence] > c.getLevelForXP(c.playerXP[c.playerDefence]) * 0.85)
					c.playerLevel[c.playerDefence] = (int)(c.getLevelForXP(c.playerXP[c.playerDefence]) * 0.85);
				if(c.playerLevel[c.playerAttack] > c.getLevelForXP(c.playerXP[c.playerAttack]) * 0.85)
					c.playerLevel[c.playerAttack] = (int)(c.getLevelForXP(c.playerXP[c.playerAttack]) * 0.85);
				c.getPA().refreshSkill(c.playerDefence);
				c.getPA().refreshSkill(c.playerAttack);
				break;
			case 4: // Lowers defense and range by 15% each.
				if(c.playerLevel[c.playerDefence] > c.getLevelForXP(c.playerXP[c.playerDefence]) * 0.85)
					c.playerLevel[c.playerDefence] = (int)(c.getLevelForXP(c.playerXP[c.playerDefence]) * 0.85);
				if(c.playerLevel[c.playerRanged] > c.getLevelForXP(c.playerXP[c.playerRanged]) * 0.85)
					c.playerLevel[c.playerRanged] = (int)(c.getLevelForXP(c.playerXP[c.playerRanged]) * 0.85);
				c.getPA().refreshSkill(c.playerDefence);
				c.getPA().refreshSkill(c.playerRanged);
				break;
			case 5: // Lowers defense by 50% and increases strength by 10%
				if(c.playerLevel[c.playerDefence] > c.getLevelForXP(c.playerXP[c.playerDefence]) * 0.50)
					c.playerLevel[c.playerDefence] = (int)(c.getLevelForXP(c.playerXP[c.playerDefence]) * 0.50);
				if(c.playerLevel[c.playerStrength] < c.getLevelForXP(c.playerXP[c.playerStrength]) * 1.1)
					c.playerLevel[c.playerStrength] = (int)(c.getLevelForXP(c.playerXP[c.playerStrength]) * 1.1);
				c.getPA().refreshSkill(c.playerDefence);
				c.getPA().refreshSkill(c.playerStrength);
				break;
			case 6: // Laziness. Player is unable to run.
				c.isRunning2 = false;
				c.getPA().sendConfig(173, 0);
				break;
		}
		if(c.zombieEffect < 6){
			c.overloadedBool = false;
			c.overloaded[0][1] = 0;
			c.overloaded[1][1] = 0;
			c.overloaded[2][1] = 0;
			c.overloaded[3][1] = 0;
			c.overloaded[4][1] = 0;
		}
	}

	/**
	 * Handles the spawning of zombies for the mini-game.
	 */
	public void handleNPCSpawn(){
		if(zombies.size() >= MAX_ZOMBIES)
			return;
		int zombie[] = GRUNT_ZOMBIES[Misc.random(GRUNT_ZOMBIES.length - 1)];
		if(spawnedZombieCount % 30 == 0 && spawnTick > 1)
			spawnTick--;
		if(bossTick-- == 0){
			bossTick = BOSS_TICK;
			zombie = BOSS_ZOMBIES[Misc.random(BOSS_ZOMBIES.length - 1)];
		}
		int spawn_point = Misc.random(SPAWN_POINTS.length - 1);
		int x = SPAWN_POINTS[spawn_point][0];
		int y = SPAWN_POINTS[spawn_point][1];
		int npcId = Server.npcHandler.spawnNpc2(zombie[0], x, y, leaderId * 4, 0, zombie[1], zombie[2], zombie[3], zombie[4]);
		if(npcId < 0)
			return;
		zombies.add(npcId);
		NPCHandler.npcs[npcId].makeX = MOVE_POINT[0] + (Misc.random(100) < 50 ? 0 : (Misc.random(5) * (spawn_point == 1 ? -1 : 1)));
		NPCHandler.npcs[npcId].makeY = MOVE_POINT[1] + (Misc.random(100) < 50 ? 0 : (Misc.random(5) * (spawn_point == 1 ? -1 : 1)));
		NPCHandler.npcs[npcId].isZombieNPC = true;
		NPCHandler.npcs[npcId].zombies = this;
		spawnedZombieCount++;
		handleZombieHomeMovement(npcId, spawn_point);
	}

	/**
	 * Handles the zombie's movement to Canifis.
	 * 
	 * @param npcId
	 *            The ID of the zombie.
	 * @param spawnId
	 *            The array index for the spawn coordinates that were used to
	 *            spawn this zombie.
	 */
	public void handleZombieHomeMovement(int npcId, int spawnId){
		int x[][] = {{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}};
		int y[][] = {{1, 1, 1, 1, 0, 0, 0, 0, -1, -1, -1, -1}, {1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0}};
		for(int x_coord : x[spawnId])
			NPCHandler.npcs[npcId].moveXQueue.add(x_coord);
		for(int y_coord : y[spawnId])
			NPCHandler.npcs[npcId].moveYQueue.add(y_coord);
	}
}