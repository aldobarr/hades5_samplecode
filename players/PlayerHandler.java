package server.model.players;

import java.util.ArrayList;

import server.Config;
import server.Connection;
import server.Server;
import server.model.HadesThread;
import server.model.npcs.NPC;
import server.model.region.RegionManager;
import server.util.Misc;
import server.util.Stream;

public class PlayerHandler{
	public static Player players[] = new Player[Config.MAX_PLAYERS];
	public static String messageToAll = "";
	public static boolean updateAnnounced;
	public static boolean updateRunning;
	public static int updateSeconds;
	public static long updateStartTime;
	private boolean kickAllPlayers = false;
	public static PlayerSave save;

	public boolean newPlayerClient(Client client1){
		int slot = -1;
		for(int i = 1; i < Config.MAX_PLAYERS; i++){
			if(players[i] == null){
				slot = i;
				break;
			}
		}
		if(slot == -1)
			return false;
		client1.handler = this;
		client1.playerId = slot;
		players[slot] = client1;
		players[slot].isActive = true;
		players[slot].connectedFrom = client1.getConnectedFrom();
		if(Config.SERVER_DEBUG)
			System.out.println("Player Slot " + slot + " slot 0 " + players[0] + " Player Hit " + players[slot]);
		return true;
	}

	public static int getPlayerId(String name){
		for(int i = 0; i < Config.MAX_PLAYERS; i++){
			if(players[i] == null)
				continue;
			if(players[i].playerName.equalsIgnoreCase(name))
				return i;
		}
		return -1;
	}
	
	public static int getONameId(String name){
		for(int i = 0; i < Config.MAX_PLAYERS; i++){
			if(players[i] == null)
				continue;
			if(players[i].originalName.equalsIgnoreCase(name))
				return i;
		}
		return -1;
	}

	public void destruct(){
		for(int i = 0; i < Config.MAX_PLAYERS; i++){
			if(players[i] == null)
				continue;
			players[i].destruct();
			players[i] = null;
		}
	}

	public static int getPlayerCount(){
		int playerCount = 0;
		synchronized(players){
			for(Player p : players)
				if(p != null)
					playerCount++;
		}
		return playerCount;
	}

	public static boolean isPlayerOn(String playerName){
		synchronized(PlayerHandler.players){
			for(int i = 0; i < Config.MAX_PLAYERS; i++){
				if(players[i] != null){
					if(players[i].originalName.equalsIgnoreCase(playerName)){
						return true;
					}
				}
			}
			return false;
		}
	}

	public void process(){
		synchronized(PlayerHandler.players){
			if(kickAllPlayers)
				for(int i = 1; i < Config.MAX_PLAYERS; i++)
					if(players[i] != null)
						players[i].setDisconnected(true);
			for(int i = 0; i < Config.MAX_PLAYERS; i++){
				if(players[i] == null || !players[i].isActive)
					continue;
				try{
					boolean close = ((Client)players[i]).isSessionClosed();
					if(close || (players[i].disconnected && (System.currentTimeMillis() - players[i].logoutDelay > 10000 || players[i].properLogout || kickAllPlayers))){
						if(players[i].inTrade){
							Client o = (Client)PlayerHandler.players[i];
							o.getTradeAndDuel().declineTrade(true);
						}
						players[i].getRegion().removePlayer((Client)players[i]);
						Client o = (Client)PlayerHandler.players[i];
						PlayerSave.saveGame(o);
						removePlayer(players[i]);
						continue;
					}
					if(players[i].playerIndex <= 0 && players[i].npcIndex <= 0)
						players[i].resetDegrade();
					int time = Misc.currentTimeSeconds();
					if((players[i].muteTime <= 0 || time >= players[i].muteTime) && players[i].timeMuted){
						players[i].timeMuted = false;
						players[i].muteTime = 0;
						Connection.unMuteUser(players[i].originalName);
					}
					if(players[i].pestGameEnd){
						if(players[i].pestEnd == 1)
							players[i].pestEnd--;
						else{
							players[i].pestGameEnd = false;
							players[i].pestEnd = 1;
							players[i].inPestGame = false;
						}
					}
					if(players[i].inPcGame() && !players[i].inPestGame){
						((Client)players[i]).getPA().movePlayer(2657, 2639, 0);
					}
					if(players[i].inZombiesGame){
						((Client)players[i]).zombies.handlePlayerPositioning((Client)players[i]);
					}
					if(players[i].combatLogout > 0 && Misc.currentTimeSeconds() >= players[i].combatLogout){
						players[i].combatLogout = -1;
						players[i].disconnected = true;
					}
					players[i].newWalkCmdSteps = 0;
					if(players[i] == null)
						continue;
					while(players[i].processQueuedPackets()){
						if(players[i] == null)
							break;
					}
					if(players[i] == null)
						continue;
					players[i].process();
					if(players[i] == null)
						continue;
					players[i].postProcessing();
					if(players[i] == null)
						continue;
					players[i].getNextPlayerMovement();
					if(players[i] == null)
						continue;
					if(players[i].bugKick){
						removePlayer(players[i]);
						continue;
					}
					if(players[i].ignoreClipTick > 0)
						players[i].ignoreClipTick--;
					if(players[i].ignoreClipTick <= 0){
						players[i].ignoreClip = false;
						players[i].ignoreClipTick = 0;
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}

			for(int i = 0; i < Config.MAX_PLAYERS; i++){
				if(players[i] == null || !players[i].isActive)
					continue;
				try{
					boolean close = ((Client)players[i]).isSessionClosed();					
					if(close || (players[i].disconnected && (System.currentTimeMillis() - players[i].logoutDelay > 10000 || players[i].properLogout || kickAllPlayers))){
						if(players[i].inTrade){
							Client o = (Client)PlayerHandler.players[i];
							o.getTradeAndDuel().declineTrade(true);
						}
						players[i].getRegion().removePlayer((Client)players[i]);
						Client o1 = (Client)PlayerHandler.players[i];
						PlayerSave.saveGame(o1);
						removePlayer(players[i]);
					}else{
						// if(o.g) {
						if(!players[i].initialized){
							players[i].initialize();
							players[i].initialized = true;
						}else
							players[i].update();
						// }
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}

			if(updateRunning && !updateAnnounced){
				updateAnnounced = true;
				Server.UpdateServer = true;
			}
			if(updateRunning && (updateSeconds - Misc.currentTimeSeconds() <= 0))
				Server.shutdownServer = true;

			for(int i = 0; i < Config.MAX_PLAYERS; i++){
				if(players[i] == null || !players[i].isActive)
					continue;
				try{
					players[i].clearUpdateFlags();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}

	public void updateNPC(Player plr, Stream str){
		synchronized(plr){
			updateBlock.currentOffset = 0;

			str.createFrameVarSizeWord(65);
			str.initBitAccess();

			str.writeBits(8, plr.npcListSize);
			int size = plr.npcListSize;
			plr.npcListSize = 0;
			for(int i = 0; i < size; i++){
				if(plr.RebuildNPCList == false && plr.withinDistance(plr.npcList[i]) == true){
					plr.npcList[i].updateNPCMovement(str);
					plr.npcList[i].appendNPCUpdateBlock(updateBlock);
					plr.npcList[plr.npcListSize++] = plr.npcList[i];
				}else{
					int id = plr.npcList[i].npcId;
					plr.npcInListBitmap[id >> 3] &= ~(1 << (id & 7));
					str.writeBits(1, 1);
					str.writeBits(2, 3);
				}
			}

			for(NPC npc : RegionManager.getLocalNpcs(plr)){
				if(npc == null)
					continue;
				int id = npc.npcId;
				if(id <= 0)
					continue;
				if(!plr.RebuildNPCList && (plr.npcInListBitmap[id >> 3] & (1 << (id & 7))) != 0)
					continue;
				plr.addNewNPC(npc, str, updateBlock);
			}
			/*
			 * for(int i = 0; i < NPCHandler.maxNPCs; i++) {
			 * if(NPCHandler.npcs[i] != null) { int id =
			 * NPCHandler.npcs[i].npcId; if (plr.RebuildNPCList == false &&
			 * (plr.npcInListBitmap[id>>3]&(1 << (id&7))) != 0) {
			 * 
			 * } else if (plr.withinDistance(NPCHandler.npcs[i]) == false) {
			 * 
			 * } else { plr.addNewNPC(NPCHandler.npcs[i], str, updateBlock); } }
			 * }
			 */

			plr.RebuildNPCList = false;

			if(updateBlock.currentOffset > 0){
				str.writeBits(14, 16383);
				str.finishBitAccess();
				str.writeBytes(updateBlock.buffer, updateBlock.currentOffset, 0);
			}else{
				str.finishBitAccess();
			}
			str.endFrameVarSizeWord();
		}
	}

	private Stream updateBlock = new Stream(new byte[Config.BUFFER_SIZE]);

	public void updatePlayer(Player plr, Stream str){
		synchronized(plr){
			updateBlock.currentOffset = 0;
			if(updateRunning && !updateAnnounced){
				str.createFrame(114);
				str.writeWordBigEndian((updateSeconds - Misc.currentTimeSeconds()) * 50 / 30);
			}
			plr.updateThisPlayerMovement(str);
			boolean saveChatTextUpdate = plr.isChatTextUpdateRequired();
			plr.setChatTextUpdateRequired(false);
			plr.appendPlayerUpdateBlock(updateBlock);
			plr.setChatTextUpdateRequired(saveChatTextUpdate);
			str.writeBits(8, plr.playerListSize);
			int size = plr.playerListSize;
			if(size > 79)
				size = 79;
			plr.playerListSize = 0;
			for(int i = 0; i < size; i++){
				if(!plr.didTeleport && !plr.playerList[i].didTeleport && plr.withinDistance(plr.playerList[i])){
					plr.playerList[i].updatePlayerMovement(str);
					plr.playerList[i].appendPlayerUpdateBlock(updateBlock);
					plr.playerList[plr.playerListSize++] = plr.playerList[i];
				}else{
					int id = plr.playerList[i].playerId;
					plr.playerInListBitmap[id >> 3] &= ~(1 << (id & 7));
					str.writeBits(1, 1);
					str.writeBits(2, 3);
				}
			}

			/*
			 * for(int i = 0; i < Config.MAX_PLAYERS; i++) { if(players[i] ==
			 * null || !players[i].isActive || players[i] == plr) continue; int
			 * id = players[i].playerId; if((plr.playerInListBitmap[id>>3]&(1 <<
			 * (id&7))) != 0) continue; if(!plr.withinDistance(players[i]))
			 * continue; plr.addNewPlayer(players[i], str, updateBlock); }
			 */
			for(Player player : RegionManager.getLocalPlayers(plr)){
				if(player == null)
					continue;
				if(!player.isActive || plr.playerId == player.playerId || player.playerId <= 0)
					continue;
				int id = player.playerId;
				if((plr.playerInListBitmap[id >> 3] & (1 << (id & 7))) != 0)
					continue;
				plr.addNewPlayer(player, str, updateBlock);
			}
			if(updateBlock.currentOffset > 0){
				str.writeBits(11, 2047);
				str.finishBitAccess();
				str.writeBytes(updateBlock.buffer, updateBlock.currentOffset, 0);
			}else
				str.finishBitAccess();
			str.endFrameVarSizeWord();
		}
	}

	public static int[] getArray(ArrayList<Integer> ints){
		int arr[] = new int[ints.size()];
		for(int i = 0; i < ints.size(); i++)
			arr[i] = ints.get(i);
		return arr;
	}

	public static void removePlayer(Player plr){
		if((plr == null) || (plr.playerId <= 0))
			return;
		int id = plr.playerId;
		if(plr.privateChat != 2){
			for(int i = 1; i < Config.MAX_PLAYERS; i++){
				if(players[i] == null || !players[i].isActive)
					continue;
				Client o = (Client)PlayerHandler.players[i];
				if(o != null)
					o.getPA().updatePM(plr.playerId, 0);
			}
		}
		new HadesThread(HadesThread.UPDATE_LAST_ONLINE, (Client)plr);
		plr.destruct();
		plr.isOnline = false;
		players[id] = null;
	}
}