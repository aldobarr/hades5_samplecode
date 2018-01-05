package server.model.minigames;

import server.Config;
import server.Server;
import server.model.DonorReward;
import server.model.npcs.NPC;
import server.model.npcs.NPCHandler;
import server.model.players.Client;
import server.model.players.PlayerHandler;
import server.util.Misc;

public class NexGames{
	public Client leader;
	public int nexWave, nexWaveTick, nexLeft, nexRewards[][];
	private final int POSSIBLE_REWARDS[][][] = {{{16689, 1}, {-1, -1}, {-1, -1}, {-1, -1}, {16711, 1}, {-1, -1}, {-1, -1}, {16137, 1}, {995, 12400}, {-1, -1}, {-1, -1}, {-1, -1}, {17259, 1}, {-1, -1}, {-1, -1}, {17361, 1}, {16667, 1}}, {{9075, 200}, {560, 100}, {15594, 1}, {562, 150}, {555, 312}, {995, 1255}}, {{14484, 1}, {1333, 1}, {1231, 1}, {9243, 55}, {15594, 1}, {9185, 1}}, {{4225, 1}, {995, 560}, {563, 25}, {4587, 1}, {537, 15}, {1738, 5}, {13736, 1}}};
	public boolean canClaimReward = false;
	public boolean canBank = false;
	public boolean beginFinalNex = false;
	
	public NexGames(Client leader){
		if(leader.heightLevel != 0)
			return;
		if(leader.clanId.isEmpty()){
			leader.sendMessage("You must be in a clan chat to do that.");
			return;
		}else if(!Server.clanChat.clans.get(leader.clanId).owner.equalsIgnoreCase(leader.originalName)){
			leader.sendMessage("Only the clan chat owner can do that.");
			return;
		}
		this.leader = leader;
		nexWave = 0;
		nexWaveTick = 4;
		nexRewards = new int[4][2];
		nexLeft = Config.NEX_WAVES[0].length;
		for(int i = 0; i < POSSIBLE_REWARDS.length; i++){
			int item = Misc.random(POSSIBLE_REWARDS[i].length - 1);
			this.nexRewards[i][0] = POSSIBLE_REWARDS[i][item][0];
			this.nexRewards[i][1] = POSSIBLE_REWARDS[i][item][1];
		}
		int mems[] = Server.clanChat.clans.get(leader.clanId).activeMembers;
		for(int id : mems){
			if(id < 0)
				continue;
			Client player = (Client)PlayerHandler.players[id];
			if(player == null)
				continue;
			if(player.heightLevel != 0 || player.absX < 2305 || player.absX > 2331 || player.absY < 9793 || player.absY > 9816)
				continue;
			player.nexWaveTick = 10;
			player.getPA().movePlayer(3248 + Misc.random(3) + 1, 9364 + Misc.random(3) + 1, leader.playerId * 4);
			player.inNexGame = true;
			player.nexTotal = 0;
			player.nexGames = this;
			player.nexTicketHP = -1;
			player.sendMessage("The Nex Games have begun...");
		}
		spawnWave(nexWave);
	}
	
	public NexGames(Client leader, boolean owner){
		this.leader = leader;
	}
	
	public void spawnWave(int waveId){
		for(int i = 0; i < Config.NEX_WAVES[waveId].length; i++){
			int npcId = Config.NEX_WAVES[waveId][i];
			int x = i == 1 && npcId == 50 ? getNexX(npcId + 1) : getNexX(npcId);
			int y = i == 1 && npcId == 50 ? getNexY(npcId + 1) : getNexY(npcId);
			int h = leader.heightLevel;
			int hp = getNexHp(npcId);
			int max = getNexMax(npcId);
			int atk = getNexAtk(npcId);
			int def = getNexDef(npcId);
			NPC npc = NPCHandler.npcs[Server.npcHandler.spawnNpc(leader, npcId, x, y, h, 0, hp, max, atk, def, true, false)];
			npc.isNexGamesNPC = true;
			npc.nexGames = this;
		}
	}
	
	public void handleNexVictory(Client player){
		if(!canClaimReward)
			return;
		if(leader.clanId.isEmpty() || !Server.clanChat.clans.containsKey(leader.clanId))
			return;
		if(!Server.clanChat.clans.get(leader.clanId).owner.equalsIgnoreCase(player.originalName) || !player.inNexGame){
			player.sendMessage("Only the clan chat owner can do that.");
			return;
		}
		canClaimReward = false;
		int memberKills[][] = new int[4][2];
		int taken[] = new int[4];
		for(int playerId : Server.clanChat.clans.get(leader.clanId).activeMembers){
			if(playerId < 0)
				continue;
			Client c = (Client)PlayerHandler.players[playerId];
			if(c == null)
				continue;
			if(c.inNexGame){
				c.sendMessage("Congratulations, you have survived the Nex Games.");
				for(int i = 0; i < memberKills.length; i++){
					if(c.nexTotal > memberKills[i][0] && !nexTaken(taken, playerId)){
						memberKills[i][0] = c.nexTotal;
						memberKills[i][1] = playerId;
						taken[i] = playerId;
					}
				}
			}
		}
		int spot = 0;
		for(int i = 0; i < taken.length; i++){
			if(taken[i] == -1){
				memberKills[i][0] = memberKills[spot][0];
				memberKills[i][1] = memberKills[spot][1];
				taken[i] = memberKills[spot++][1];
			}
		}
		leader.sendMessage("The chest disappears as you claim your clan's reward.");
		leader.sendMessage("You may now move on to face Nex, if you dare.");
		for(int i = 0; i < memberKills.length; i++){
			Client c2 = (Client)PlayerHandler.players[memberKills[i][1]];
			if(c2 == null)
				continue;
			if(this.nexRewards[i][0] != -1)
				c2.sendMessage("Congratulations, you have received the " + (i + 1) + DonorReward.getSuffix(i + 1) + " reward of the Nex Games!");
			else{
				c2.sendMessage("Congratulations, you have received the " + (i + 1) + DonorReward.getSuffix(i + 1) + " reward.");
				c2.sendMessage("However, it was looted by the dying souls of the bosses you have slain.");
			}
			if(c2.inventory.freeSlots() > 0)
				c2.inventory.addItem(this.nexRewards[i][0], this.nexRewards[i][1], -1);
			else
				Server.itemHandler.createGroundItem(c2, nexRewards[i][0], c2.absX, c2.absY, c2.heightLevel, nexRewards[i][1], c2.playerId);
		}
		for(int playerId : Server.clanChat.clans.get(leader.clanId).activeMembers){
			if(playerId < 0)
				continue;
			Client c2 = (Client)PlayerHandler.players[playerId];
			if(c2 == null)
				continue;
			if(!c2.inNexGame)
				continue;
			c2.getPA().object(-1, 3248, 9364, 3, 10);
			c2.saveGame();
		}
		beginFinalNex = true;
	}
	
	public void beginFinalNex(Client player){
		if(!beginFinalNex || (!Config.NEX_SPAWNED[leader.playerId] && player.playerId != leader.playerId))
			return;
		player.resetNex();
		player.ancientBookHeal = -1;
		player.getPA().startTeleport(2718 + ((Misc.random(4)) * (Misc.random(51) % 2 == 0 ? -1 : 1)), 9439 + ((Misc.random(4)) * (Misc.random(51) % 2 == 0 ? -1 : 1)), leader.playerId * 4, true);
		if(!Config.NEX_SPAWNED[leader.playerId] && player.playerId == leader.playerId){
			int npc = 2000;
			int x = 2718 + ((Misc.random(4)) * (Misc.random(51) % 2 == 0 ? -1 : 1));
			int y = 9439 + ((Misc.random(4)) * (Misc.random(51) % 2 == 0 ? -1 : 1));
			int hp = this.getNexHp(npc);
			int max = this.getNexMax(npc);
			int atk = this.getNexAtk(npc);
			int def = this.getNexDef(npc);
			Server.npcHandler.newNPC(npc, x, y, leader.playerId * 4, 1, hp, max, atk, def, 1001);
			Config.NEX_SPAWNED[leader.playerId] = true;
		}
		if(hasZaros(player))
			player.sendMessage("You can feel the power of Zaros increase your might!");
	}
	
	public void handleLeaderDeath(Client player){
		if(player.playerId != leader.playerId)
			return;
		for(int playerId : Server.clanChat.clans.get(leader.clanId).activeMembers){
			if(playerId < 0)
				continue;
			Client temp = (Client)PlayerHandler.players[playerId];
			if(temp == null || !temp.inNexGame)
				continue;
			temp.resetNex();
			temp.saveGame();
			if(temp.playerId == player.playerId)
				continue;
			temp.getPA().movePlayer(Config.START_LOCATION_X, Config.START_LOCATION_Y, 0);
			temp.sendMessage("Your leader died, and so you have lost the Nex Games.");
		}
	}
	
	public int getNexX(int id){
		switch(id){
			case 941:
				return 3252;
			case 55:
				return 3244;
			case 53:
				return 3244;
			case 50:
				return 3251;
			case 51:
				return 3245;
			case 82:
				return 3247;
			case 83:
				return 3247;
			case 84:
				return 3247;
			case 8349:
				return 3247;
			case 8133:
				return 3248;
			case 3200:
				return 3245;
			default:
				return 1;
		}
	}
	
	public int getNexY(int id){
		switch(id){
			case 941:
				return 9370;
			case 55:
				return 9370;
			case 53:
				return 9360;
			case 50:
				return 9360;
			case 51:
				return 9370;
			case 82:
				return 9363;
			case 83:
				return 9366;
			case 84:
				return 9371;
			case 8349:
				return 9358;
			case 8133:
				return 9364;
			case 3200:
				return 9370;
			default:
				return 1;
		}
	}

	public int getNexHp(int id){
		switch(id){
			case 2000:
				return 1500;
			case 941:
				return 85;
			case 55:
				return 109;
			case 53:
				return 145;
			case 50:
				return 240;
			case 82:
				return 79;
			case 83:
				return 87;
			case 84:
				return 157;
			case 8349:
				return 385;
			case 8133:
				return 450;
			case 3200:
				return 250;
			case 3006:
				return 500;
			default:
				return 150;
		}
	}

	public int getNexMax(int id){
		switch(id){
			case 2000:
				return 70;
			case 941:
				return 14;
			case 55:
				return 16;
			case 53:
				return 18;
			case 50:
				return 25;
			case 82:
				return 15;
			case 83:
				return 16;
			case 84:
				return 18;
			case 8349:
				return 50;
			case 8133:
				return 65;
			case 3200:
				return 40;
			case 3006:
				return 75;
			default:
				return 15;
		}
	}

	public int getNexAtk(int id){
		switch(id){
			case 941:
				return 75;//
			case 55:
				return 60;// 40
			case 53:
				return 75;// 75
			case 50:
				return 500;
			case 82:
				return 40;// 40
			case 83:
				return 60;// 60
			case 84:
				return 70;// 70
			case 8349:
				return 400;
			case 8133:
				return 600;
			case 3200:
				return 450;
			case 3006:
				return 750;
			case 2000:
				return 1000;
			default:
				return 70;
		}
	}

	public int getNexDef(int id){
		switch(id){
			case 941:
				return 75;
			case 55:
				return 40;
			case 53:
				return 75;
			case 50:
				return 350;
			case 82:
				return 40;
			case 83:
				return 60;
			case 84:
				return 70;
			case 8349:
				return 250;
			case 8133:
				return 700;
			case 2000:
				return 1050;
			case 3200:
				return 550;
			case 3006:
				return 750;
			default:
				return 70;
		}
	}
	
	private boolean hasZaros(Client player){
		if(hasFullTorva(player))
			return true;
		if(hasFullPernix(player))
			return true;
		if(hasFullVirtus(player))
			return true;
		if(hasZaryteBow(player))
			return true;
		if(player.playerEquipment[player.playerShield] == 19617) // Ancient Book.
			return true;
		if(player.playerEquipment[player.playerWeapon] == 4675) // Ancient Staff.
			return true;
		return false;
	}
	
	public static boolean hasFullTorva(Client player){
		int helm = player.playerEquipment[player.playerHat];
		int body = player.playerEquipment[player.playerChest];
		int legs = player.playerEquipment[player.playerLegs];
		return ((helm == 20135 || helm == 20137) && (body == 20139 || body == 20141) && (legs == 20143 || legs == 20145));
	}
	
	public static boolean hasFullPernix(Client player){
		int helm = player.playerEquipment[player.playerHat];
		int body = player.playerEquipment[player.playerChest];
		int legs = player.playerEquipment[player.playerLegs];
		return ((helm == 20147 || helm == 20149) && (body == 20151 || body == 20153) && (legs == 20155 || legs == 20157));
	}
	
	public static boolean hasFullVirtus(Client player){
		int helm = player.playerEquipment[player.playerHat];
		int body = player.playerEquipment[player.playerChest];
		int legs = player.playerEquipment[player.playerLegs];
		return ((helm == 20159 || helm == 20161) && (body == 20163 || body == 20165) && (legs == 20167 || legs == 20169));
	}
	
	public static boolean hasZaryteBow(Client player){
		return player.playerEquipment[player.playerWeapon] == 20171 || player.playerEquipment[player.playerWeapon] == 20173;
	}
	
	private boolean nexTaken(int taken[], int id){
		for(int i : taken)
			if(i == id)
				return true;
		return false;
	}
}