package server.model.players.packets;

import server.Config;
import server.Connection;
import server.Server;
import server.model.players.Client;
import server.model.players.PacketType;
import server.model.players.PlayerHandler;
import server.model.players.PlayerSave;
import server.util.Misc;

/**
 * Private messaging, friends etc
 **/
public class PrivateMessaging implements PacketType{

	public final int ADD_FRIEND = 188, SEND_PM = 126, REMOVE_FRIEND = 215, CHANGE_PM_STATUS = 95, REMOVE_IGNORE = 74, ADD_IGNORE = 133;

	@Override
	public void processPacket(Client c, int packetType, int packetSize){
		switch(packetType){
			case ADD_FRIEND:
				c.friendUpdate = true;
				long friendToAdd = c.getInStream().readQWord();
				String name = Misc.longToPlayerName2(friendToAdd);
				if(name.equalsIgnoreCase(c.playerName) || name.equalsIgnoreCase(c.originalName))
					break;
				long cname = Misc.playerNameToInt64(c.playerName);
				boolean canAdd = true;
				int slot = -1;
				for(int i = 0; i < c.friends.length; i++){
					if(c.friends[i] == 0 && slot < 0)
						slot = i;
					if(c.friends[i] == friendToAdd){
						canAdd = false;
						c.sendMessage(friendToAdd + " is already on your friends list.");
						break;
					}
				}
				if(canAdd && slot > -1){
					if(c.friends[slot] == 0){
						c.friends[slot] = friendToAdd;
						PlayerSave.saveGame(c);
						c.getPA().sendText("Friends List - " + c.getNumFriends() + " / " + c.friends.length, 5067);
						for(int i = 1; i < Config.MAX_PLAYERS; i++){
							if(PlayerHandler.players[i] != null && PlayerHandler.players[i].isActive && Misc.playerNameToInt64(PlayerHandler.players[i].playerName) == friendToAdd){
								Client o = (Client)PlayerHandler.players[i];
								if(o != null){
									if(c.privateChat == 1 && o.getPA().isInPM(cname))
										o.getPA().loadPM(cname, 1);
									if((!o.getPA().ignoreContains(Misc.playerNameToInt64(c.playerName)) && (o.privateChat == 0 || (o.privateChat == 1 && o.getPA().isInPM(cname)))) || (c.playerRights > 0 && c.playerRights < 5)){
										c.getPA().loadPM(friendToAdd, 1);
										break;
									}
								}
							}
						}
					}
				}
				break;
			case SEND_PM:
				long sendMessageToFriendId = c.getInStream().readQWord();
				byte pmchatText[] = new byte[100];
				int pmchatTextSize = (byte)(packetSize - 8);
				c.getInStream().readBytes(pmchatText, pmchatTextSize, 0);
				if(Connection.isMuted(c)){
					return;
				}
				long myName = Misc.playerNameToInt64(c.playerName);
				for(long element : c.friends){
					if(element == sendMessageToFriendId){
						boolean pmSent = false;
						for(int i2 = 1; i2 < Config.MAX_PLAYERS; i2++){
							if(PlayerHandler.players[i2] != null && PlayerHandler.players[i2].isActive && (Misc.playerNameToInt64(PlayerHandler.players[i2].playerName) == sendMessageToFriendId)){
								Client o = (Client)PlayerHandler.players[i2];
								if(o != null){
									if(o.getPA().ignoreContains(Misc.playerNameToInt64(c.playerName)) && (c.playerRights == 0 || c.playerRights == 5))
										break;
									if((c.playerRights >= 1 && c.playerRights < 5) || PlayerHandler.players[i2].privateChat == 0 || (PlayerHandler.players[i2].privateChat == 1 && o.getPA().isInPM(myName))){
										o.getPA().sendPM(myName, c.playerRights, pmchatText, pmchatTextSize);
										pmSent = true;
									}
								}
								break;
							}
						}
						if(!pmSent){
							c.sendMessage("That player is currently offline.");
							break;
						}
					}
				}
				break;
			case REMOVE_FRIEND:
				c.friendUpdate = true;
				long friendToRemove = c.getInStream().readQWord();
				for(int i1 = 0; i1 < c.friends.length; i1++){
					if(c.friends[i1] == friendToRemove){
						c.friends[i1] = 0;
						PlayerSave.saveGame(c);
						c.getPA().sendText("Friends List - " + c.getNumFriends() + " / " + c.friends.length, 5067);
						break;
					}
				}
				if(c.privateChat == 1){
					for(int i = 1; i<PlayerHandler.players.length; i++){
						if(PlayerHandler.players[i] == null || PlayerHandler.players[i].playerId == c.playerId)
							continue;
						Client player = (Client)PlayerHandler.players[i];
						if(player.playerRights >= 1 && player.playerRights <= 3)
							continue;
						long lname = Misc.playerNameToInt64(player.playerName);
						if(lname == friendToRemove && player.getPA().isInPM(lname))
							player.getPA().loadPM(Misc.playerNameToInt64(c.playerName), 0);
					}
				}
				String remove = Misc.longToPlayerName2(friendToRemove);
				if(Server.clanChat.clans.containsKey(c.ownedClanName))
					if(Server.clanChat.clans.get(c.ownedClanName).members.containsKey(remove))
						Server.clanChat.clans.get(c.ownedClanName).members.remove(remove);
				break;
			case REMOVE_IGNORE:
				if(c.lastIgnoreRemove > Misc.currentTimeSeconds())
					break;
				c.lastIgnoreRemove = Misc.currentTimeSeconds() + 5;
				c.friendUpdate = true;
				long ignoreRemove = c.getInStream().readQWord();
				for(int i = 0; i < c.ignores.length; i++){
					if(c.ignores[i] == ignoreRemove){
						c.ignores[i] = 0;
						PlayerSave.saveGame(c);
						c.getPA().sendText("Ignore List - " + c.getNumIgnores() + " / " + c.ignores.length, 5717);
						c.getPA().logIntoPM();
						break;
					}
				}
				break;
			case CHANGE_PM_STATUS:
				c.getInStream().readUnsignedByte();
				c.privateChat = c.getInStream().readUnsignedByte();
				c.getInStream().readUnsignedByte();
				for(int i1 = 1; i1 < Config.MAX_PLAYERS; i1++){
					if(PlayerHandler.players[i1] != null && PlayerHandler.players[i1].isActive == true){
						Client o = (Client)PlayerHandler.players[i1];
						if(o != null){
							o.getPA().updatePM(c.playerId, 1);
						}
					}
				}
				break;
			case ADD_IGNORE:
				if(c.lastIgnoreAdd > Misc.currentTimeSeconds())
					break;
				c.lastIgnoreAdd = Misc.currentTimeSeconds() + 5;
				c.friendUpdate = true;
				long ignoreAdd = c.getInStream().readQWord();
				for(int i = 0; i < c.ignores.length; i++){
					if(c.ignores[i] == 0){
						c.ignores[i] = ignoreAdd;
						PlayerSave.saveGame(c);
						c.getPA().sendText("Ignore List - " + c.getNumIgnores() + " / " + c.ignores.length, 5717);
						c.getPA().logIntoPM();
						break;
					}
				}
				break;
		}
	}
}
