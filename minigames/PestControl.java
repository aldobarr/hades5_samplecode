package server.model.minigames;

import java.util.ArrayList;
import java.util.Collection;
import server.model.npcs.NPC;
import server.model.npcs.NPCHandler;
import server.model.players.Client;
import server.model.players.Player;
import server.model.players.PlayerHandler;
import server.model.region.RegionManager;
import server.util.Misc;
import server.Server;

/**
 * 
 * @author hadesflames
 * 
 */
public class PestControl{

	public PestControl(){

	}

	// npcType HP maxHit attack defense
	public final int pests[][] = {
			// Splatters
			// {3728, 23, 4, 25, 35}, - lvl 33, too easy.
	{3729, 34, 4, 27, 38}, {3730, 44, 5, 33, 42}, {3731, 55, 5, 37, 45},

			// Shifters
			// {3734, 38, 7, 45, 50}, - lvl 57, too easy.
			// {3736, 53, 8, 48, 52}, - lvl 76, too easy.
	{3738, 68, 9, 52, 54}, {3740, 83, 10, 55, 56},

			// Ravagers
			// {3743, 38, 10, 62, 45}, - lvl 53, too easy.
	{3744, 53, 12, 79, 63}, {3745, 68, 15, 98, 82}, {3746, 83, 17, 115, 99},

			// Spinners
			// {3748, 57, 7, 50, 60}, - lvl 55, too easy.
	{3749, 76, 8, 71, 79}, {3750, 90, 9, 85, 93}, {3751, 101, 10, 96, 104},

			// Torchers
			// {3757, 55, 10, 57, 49}, - lvl 67, too easy.
	{3759, 68, 11, 64, 57}, {3760, 78, 12, 79, 73}, {3761, 80, 12, 82, 75},

			// Defilers
	{3768, 62, 10, 65, 58}, {3769, 78, 12, 80, 73}, {3770, 97, 13, 92, 84},

			// Brawlers
	{3773, 83, 10, 88, 90}, {3775, 143, 16, 115, 120}, {3776, 162, 21, 150, 145},};
	public ArrayList<Integer> pestNPCS = new ArrayList<Integer>();
	public final int GAME_TIMER = 100; // 5 minutes
	public final int WAIT_TIMER = 7;
	public final int MIN_HITS[] = {50, 200};
	public final int MIN_PLAYERS = 3;
	public int eastGate = -1, westGate = -1, seGate = -1, swGate = -1;
	public int spawnEast = 0, spawnWest = 1, spawnSE = 2, spawnSW = 3;
	public int gameTimer = -1;
	public int waitTimer = 15;
	public int properTimer = 0;
	public ArrayList<Integer> playersInBoat = new ArrayList<Integer>();
	public ArrayList<Integer> playersInGame = new ArrayList<Integer>();
	public ArrayList<Integer> removeBoat = new ArrayList<Integer>();
	
	public void process(){
		setBoatInterface();
		setGameInterface();
		if(properTimer > 0){
			properTimer--;
			return;
		}else
			properTimer = 4;
		if(waitTimer > 0)
			waitTimer--;
		else if(waitTimer == 0)
			startGame();
		if(gameTimer > 0){
			gameTimer--;
			if(playersInGame.size() == 0)
				endGame(false);
			if(allPortalsDead()){
				endGame(true);
			}else{
				spawning();
			}
		}else if(gameTimer == 0)
			endGame(false);
	}

	public void startGame(){
		if(playersInBoat() >= MIN_PLAYERS){
			gameTimer = GAME_TIMER;
			waitTimer = -1;
			// spawn npcs
			spawnPortals();
			// move players into game
			for(int id : playersInBoat){
				if(PlayerHandler.players[id] != null){
					playersInGame.add(id);
					movePlayer(id);
					PlayerHandler.players[id].inPestGame = true;
				}
			}
			playersInBoat.clear();
		}else{
			waitTimer = WAIT_TIMER;
			for(int id : playersInBoat){
				if(PlayerHandler.players[id] != null){
					Client player = (Client)PlayerHandler.players[id];
					if(player.inPcBoat())
						player.sendMessage("There need to be at least 3 players to start a game of pest control.");
					else
						removeBoat.add(id);
				}
			}
			for(int id : removeBoat)
				playersInBoat.remove((Object)id);
		}
	}

	/**
	 * Set the interface information for players in the pest control boat.
	 */
	public void setBoatInterface(){
		for(int id : playersInBoat){
			if(PlayerHandler.players[id] != null){
				Client c = (Client)PlayerHandler.players[id];
				synchronized(c){
					int time = 3 * ((waitTimer > -1 ? waitTimer : 0) + (gameTimer > -1 ? gameTimer : 0));
					c.getPA().sendText("Next Departure: " + time + " seconds", 21120);
					c.getPA().sendText("Players Ready: " + playersInBoat(), 21121);
					c.getPA().sendText("(Need at least 3 players)", 21122);
					c.getPA().sendText("Points: " + c.pcPoints, 21123);
				}
			}
		}
	}

	/**
	 * Set the interface information for players in the pest control game.
	 */
	public void setGameInterface(){
		for(int id : playersInGame){
			if(PlayerHandler.players[id] != null){
				Client c = (Client)PlayerHandler.players[id];
				synchronized(c){
					c.getPA().sendText("" + (westGate > -1 ? NPCHandler.npcs[westGate].HP : "0"), 21111); // west
					c.getPA().sendText("" + (eastGate > -1 ? NPCHandler.npcs[eastGate].HP : "0"), 21112); // east
					c.getPA().sendText("" + (seGate > -1 ? NPCHandler.npcs[seGate].HP : "0"), 21113); // se
					c.getPA().sendText("" + (swGate > -1 ? NPCHandler.npcs[swGate].HP : "0"), 21114); // sw
					c.getPA().sendText("N/A", 21115);
					c.getPA().sendText("" + c.pcDamage, 21116);
					c.getPA().sendText("Time remaining: " + (gameTimer * 3) + " seconds", 21117);
				}
			}
		}
	}

	/**
	 * Checks to see how many players there are inside of the pest boat.
	 * 
	 * @return The number of players in the pest boat.
	 */
	public int playersInBoat(){
		int count = 0;
		for(int id : playersInBoat)
			if(PlayerHandler.players[id] != null)
				count++;
		return count;
	}

	public void endGame(boolean won){
		gameTimer = -1;
		waitTimer = WAIT_TIMER;
		spawnEast = Misc.random(3);
		spawnWest = Misc.random(3);
		spawnSW = Misc.random(3);
		spawnSE = Misc.random(3);
		for(int id : playersInGame){
			if(PlayerHandler.players[id] != null){
				Client c = (Client)PlayerHandler.players[id];
				synchronized(c){
					c.pestTemp = true;
					c.getPA().movePlayer(2657, 2639, 0);
					c.pestTemp = false;
					c.pestGameEnd = true;
				}
				if(won && c.pcDamage >= MIN_HITS[0]){
					int points = c.pcDamage > MIN_HITS[1] ? 4 : 3;
					synchronized(c){
						c.playerLevel[3] = c.getLevelForXP(c.playerXP[3]);
						c.playerLevel[5] = c.getLevelForXP(c.playerXP[5]);
						c.specAmount = 10;
						c.pcPoints += points;
						c.inventory.addItem(995, c.combatLevel * 10, -1);
						c.getPA().refreshSkill(3);
						c.getPA().refreshSkill(5);
						c.saveGame();
					}
					c.sendMessage("You have won the pest control game and have been awarded " + points + " pest control points.");
				}else if(won){
					c.sendMessage("The void knights notice your lack of zeal.");
				}else{
					c.sendMessage("You failed to kill all the portals in 5 minutes and have not been awarded any points.");
				}
				synchronized(c){
					c.pcDamage = 0;
					c.getItems().addSpecialBar(c.playerEquipment[c.playerWeapon]);
					c.getCombat().resetPrayers();
				}
			}
		}
		playersInGame.clear();
		for(int id : pestNPCS){
			synchronized(NPCHandler.npcs){
				if(NPCHandler.npcs[id] == null)
					continue;
				if(isPestControl(NPCHandler.npcs[id].npcType)){
					NPCHandler.npcs[id].absX = 20;
					NPCHandler.npcs[id].absY = 20;
					NPCHandler.npcs[id].updateRequired = true;
					NPCHandler.npcs[id].getRegion().removeNpc(NPCHandler.npcs[id]);
					NPCHandler.npcs[id] = null;
				}
			}
		}
		pestNPCS.clear();
	}

	public boolean allPortalsDead(){
		if(eastGate > -1)
			return false;
		if(westGate > -1)
			return false;
		if(swGate > -1)
			return false;
		if(seGate > -1)
			return false;
		return true;
	}

	public void movePlayer(int index){
		Client c = (Client)PlayerHandler.players[index];
		synchronized(c){
			c.pestTemp = true;
			c.getPA().movePlayer(2659 - Misc.random(3), 2612 - Misc.random(3), 0);
			c.pestTemp = false;
		}
	}

	public void spawning(){
		if(spawnEast-- == 0 && eastGate > -1){
			spawnEast = 3;
			spawnRandomPest(eastGate, 1);
		}
		if(spawnWest-- == 0 && westGate > -1){
			spawnWest = 3;
			spawnRandomPest(westGate, 2);
		}
		if(spawnSE-- == 0 && seGate > -1){
			spawnSE = 3;
			spawnRandomPest(seGate, 3);
		}
		if(spawnSW-- == 0 && swGate > -1){
			spawnSW = 3;
			spawnRandomPest(swGate, 4);
		}
	}

	public void spawnRandomPest(int id, int gate){
		synchronized(NPCHandler.npcs){
			if(NPCHandler.npcs[id] == null)
				return;
			int pest[] = pests[Misc.random(pests.length - 1)];
			int x_rand = getXRand(gate);
			int y_rand = getYRand(gate, x_rand);
			int npcId = Server.npcHandler.spawnNpc2(pest[0], NPCHandler.npcs[id].absX + x_rand, NPCHandler.npcs[id].absY + y_rand, 0, 0, pest[1], pest[2], pest[3], pest[4]);
			if(npcId < 0)
				return;
			NPCHandler.npcs[npcId].pestGate = getGateId(gate);
			NPCHandler.npcs[npcId].gfx0(1502);
			pestNPCS.add(npcId);
		}
	}

	/**
	 * Checks if the player is trying to walk through a Brawler.
	 * 
	 * @param dir
	 *            The walking dir.
	 * @param id
	 *            The NPC ID.
	 * @param player
	 *            The player that is attempting to move.
	 * @return True if the path is through a Brawler, false if not.
	 */
	public boolean checkBrawlerMovement(int dir, int id, Player player){
		if(!isBrawler(NPCHandler.npcs[id].npcType))
			return false;
		int x = NPCHandler.npcs[id].absX, y = NPCHandler.npcs[id].absY;
		int newX = player.absX + Misc.directionDeltaX[dir], newY = player.absY + Misc.directionDeltaY[dir];
		int dirs[] = getBrawlerDirs(NPCHandler.npcs[id]);
		int dirX = dirs[0], dirY = dirs[1];
		return ((newX == x || newX == x + dirX) && (newY == y || newY == y + dirY));
	}

	/**
	 * Find the direction that the brawler is facing in order to get the
	 * unwalkable blocks.
	 * 
	 * @param npc
	 *            The NPC we are looking at.
	 * @return The directions to block walking.
	 * 
	 */
	public int[] getBrawlerDirs(NPC npc){
		if(npc.face == 0 || npc.face == 32768)
			return new int[]{1, 1};
		int playerId = npc.face - 32768;
		if(PlayerHandler.players[playerId] == null){
			synchronized(npc){
				npc.facePlayer(0);
			}
			return new int[]{1, 1};
		}
		if(!PlayerHandler.players[playerId].inPcGame()){
			synchronized(npc){
				npc.facePlayer(0);
			}
			return new int[]{1, 1};
		}
		int x = PlayerHandler.players[playerId].absX, y = PlayerHandler.players[playerId].absY;
		if(x > npc.absX && y == npc.absY)
			return new int[]{-1, 1};
		else if(x > npc.absX && y > npc.absY)
			return new int[]{-1, -1};
		else if(x > npc.absX && y < npc.absY)
			return new int[]{-1, 1};
		else if(x == npc.absX && y > npc.absY)
			return new int[]{-1, -1};
		else if(x == npc.absX && y < npc.absY)
			return new int[]{1, 1};
		else if(x < npc.absX && y == npc.absY)
			return new int[]{1, -1};
		else if(x < npc.absX && y > npc.absY)
			return new int[]{1, -1};
		else
			return new int[]{1, 1};
	}

	// East = 1, West = 2, SE = 3, SW = 4
	public int getGateId(int gate){
		switch(gate){
			case 1:
				return eastGate;
			case 2:
				return westGate;
			case 3:
				return seGate;
			case 4:
				return swGate;
		}
		return -1;
	}

	/**
	 * Handles special actions and effects of different monsters within the pest
	 * control mini-game.
	 * 
	 * @param id
	 *            The npc ID of the monster we're looking at.
	 */
	public void handlePestMonster(int id){
		int type = NPCHandler.npcs[id].npcType;
		synchronized(NPCHandler.npcs[id]){
			if(NPCHandler.npcs[id].effectTick-- > 0)
				return;
			NPCHandler.npcs[id].effectTick = 4;
		}
		int red = 0;
		switch(type){
		// Spinners
			case 3748:
				red = 3;
			case 3749:
				red = 2;
			case 3750:
				red = 1;
			case 3751:
				int heal = 13 - red;
				if(NPCHandler.npcs[NPCHandler.npcs[id].pestGate] == null)
					return;
				if(!isPestControl(NPCHandler.npcs[NPCHandler.npcs[id].pestGate].npcType))
					return;
				synchronized(NPCHandler.npcs){
					NPCHandler.npcs[NPCHandler.npcs[id].pestGate].HP += heal;
					if(NPCHandler.npcs[NPCHandler.npcs[id].pestGate].HP > NPCHandler.npcs[NPCHandler.npcs[id].pestGate].MaxHP)
						NPCHandler.npcs[NPCHandler.npcs[id].pestGate].HP = NPCHandler.npcs[NPCHandler.npcs[id].pestGate].MaxHP;
					NPCHandler.npcs[NPCHandler.npcs[id].pestGate].updateRequired = true;
				}
				break;
		}
	}

	/**
	 * Handles the explosion of splatter npcs.
	 * 
	 * @param id
	 *            The npc ID for the exploding splatter.
	 */
	public void splatterExplosion(int id){
		int x = NPCHandler.npcs[id].absX, y = NPCHandler.npcs[id].absY;
		Collection<Player> players = RegionManager.getLocalPlayers(NPCHandler.npcs[id].getLocation());
		synchronized(NPCHandler.npcs){
			if(!NPCHandler.npcs[id].isDead)
				NPCHandler.npcs[id].animNumber = 3889;
			NPCHandler.npcs[id].animUpdateRequired = true;
			NPCHandler.npcs[id].exploded = true;
			NPCHandler.npcs[id].isDead = true;
		}
		for(Player player : players){
			synchronized(player){
				if(player == null)
					continue;
				if(player.absX >= x - 1 && player.absX <= x + 1 && player.absY >= y - 1 && player.absY <= y + 1 && player.heightLevel == NPCHandler.npcs[id].heightLevel){
					int mod = NPCHandler.npcs[id].npcType - 3728;
					int damage = Misc.random_range(3 + mod, 9 + mod);
					player.dealDamage(damage);
				}
			}
		}
		Collection<NPC> npcs = RegionManager.getLocalNpcs(NPCHandler.npcs[id].getLocation());
		for(NPC npc : npcs){
			synchronized(npc){
				if(npc == null)
					continue;
				if(npc.absX >= x - 1 && npc.absX <= x + 1 && npc.absY >= y - 1 && npc.absY <= y + 1 && npc.heightLevel == NPCHandler.npcs[id].heightLevel){
					if(npc.npcType >= 3728 && npc.npcType <= 3731 && !npc.exploded)
						splatterExplosion(npc.npcId);
					else{
						int mod = NPCHandler.npcs[id].npcType - 3728;
						int damage = Misc.random_range(3 + mod, 9 + mod);
						npc.hitDiff = damage;
						npc.HP -= damage;
						npc.hitUpdateRequired = true;
						npc.updateRequired = true;
					}
				}
			}
		}
	}

	/**
	 * Handles the death of a pest control NPC.
	 * 
	 * @param id
	 *            The id of the pest control NPC.
	 */
	public void handlePestDeath(int id){
		int type = NPCHandler.npcs[id].npcType;
		switch(type){
		// Splatters
			case 3728:
			case 3729:
			case 3730:
			case 3731:
				splatterExplosion(id);
				break;
			// Spinners
			case 3748:
			case 3749:
			case 3750:
			case 3751:
				int x = NPCHandler.npcs[id].absX,
				y = NPCHandler.npcs[id].absY;
				Collection<Player> players = RegionManager.getLocalPlayers(NPCHandler.npcs[id].getLocation());
				for(Player player : players){
					synchronized(player){
						if(player.absX >= x - 1 && player.absX <= x + 1 && player.absY >= y - 1 && player.absY <= y + 1 && player.heightLevel == NPCHandler.npcs[id].heightLevel){
							player.poisonDamage = 2;
							player.poisonMask = 1;
							player.handleHitMask(5);
							player.updateRequired = true;
							player.playerLevel[player.playerHitpoints] -= 5;
							if(player.playerLevel[player.playerHitpoints] < 0)
								player.playerLevel[player.playerHitpoints] = 0;
							((Client)player).getPA().refreshSkill(player.playerHitpoints);
							((Client)player).sendMessage("You've been poisoned!");
							((Client)player).sendMessage("The poison damages you!");
						}
					}
				}
				break;
		}
	}

	public int getXRand(int gate){
		int ret = 0;
		if(gate == 1){
			ret = Misc.random_range(1, 7) * -1;
			if(ret >= -4 && Misc.random(100) < 50)
				ret *= -1;
		}else if(gate == 2)
			ret = Misc.random_range(1, 10);
		else if(gate == 3)
			ret = Misc.random_range(1, 5);
		else if(gate == 4){
			ret = Misc.random_range(1, 8);
			if(ret <= 3 && Misc.random(100) < 50)
				ret *= -1;
		}
		return ret;
	}

	// 1 = east, 2 = west, 3 = south east, 4 = south west
	public int getYRand(int gate, int x){
		int ret = 0;
		if(gate == 1){
			ret = Misc.random_range(1, x >= 1 ? 4 : 5);
			if((ret == 1 || x < 1) && Misc.random(100) < 50)
				ret *= -1;
		}else if(gate == 2){
			ret = Misc.random_range(1, 7);
			if(ret <= 3 && Misc.random(100) < 50)
				ret *= -1;
		}else if(gate == 3){
			ret = Misc.random_range(1, 8);
			if(ret <= (2 + (x > 0 && x < 3 ? 2 : 0)) && Misc.random(100) < 50)
				ret *= -1;
		}else if(gate == 4){
			ret = Misc.random_range(1, 8);
			if(x >= -3 && ret <= 4 && Misc.random(100) < 50)
				ret *= -1;
		}
		return ret;
	}

	public boolean isBrawler(int npcType){
		return npcType >= 3773 && npcType <= 3776;
	}

	/**
	 * Check whether or not this type of NPC is a pest control monster.
	 * 
	 * @param npcType
	 *            The ID of the NPC Type.
	 * @return True if this NPC is a pest control monster. False if not.
	 */
	public boolean isPestControl(int npcType){
		if((npcType >= 6142 && npcType <= 6145) || (npcType >= 3727 && npcType <= 3776))
			return true;
		return false;
	}

	/**
	 * Check whether or not this type of NPC is an aggressive pest control
	 * monster.
	 * 
	 * @param npcType
	 *            The ID of the NPC Type.
	 * @return True if this NPC is an aggressive pest control monster. False if
	 *         not.
	 */
	public boolean isPestControlAggressive(int npcType){
		return ((npcType >= 3727 && npcType <= 3776) && (npcType < 3748 || npcType > 3751));
	}

	public void spawnPortals(){
		synchronized(NPCHandler.npcs){
			// npcType x y heightLevel WalkingType HP maxHit attack defence
			westGate = Server.npcHandler.spawnNpc2(6142, 2628, 2591, 0, 0, 200, 0, 0, 200);
			eastGate = Server.npcHandler.spawnNpc2(6143, 2680, 2588, 0, 0, 200, 0, 0, 200);
			seGate = Server.npcHandler.spawnNpc2(6144, 2669, 2570, 0, 0, 200, 0, 0, 200);
			swGate = Server.npcHandler.spawnNpc2(6145, 2645, 2569, 0, 0, 200, 0, 0, 200);
			NPCHandler.npcs[eastGate].npcObject = true;
			NPCHandler.npcs[westGate].npcObject = true;
			NPCHandler.npcs[swGate].npcObject = true;
			NPCHandler.npcs[seGate].npcObject = true;
		}
		pestNPCS.add(westGate);
		pestNPCS.add(eastGate);
		pestNPCS.add(seGate);
		pestNPCS.add(swGate);
	}
}