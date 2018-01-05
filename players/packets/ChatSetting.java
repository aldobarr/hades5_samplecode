package server.model.players.packets;

import server.model.players.Client;
import server.model.players.PacketType;
import server.model.players.PlayerHandler;
import server.util.Misc;

public class ChatSetting implements PacketType{
	public void processPacket(Client c, int packetType, int packetSize){
		// 0 = on, 1 = friend, 2 = off
		int setting = c.getInStream().readSignedByte();
		int pvt = c.getInStream().readSignedByte();
		if(pvt == 0){
			if(c.playerRights == 0 || c.playerRights == 5){
				c.publicChat = setting;
				return;
			}
			c.publicChat = 0;
			c.getPA().setChatOptions(c.publicChat, c.privateChat, 0);
		}else{
			if(c.playerRights >= 1 && c.playerRights < 5)
				c.getPA().setChatOptions(c.publicChat, c.privateChat, 0);
			else if(setting == 1)
				turnChatFriend(c);
			else if(setting == 2)
				turnChatOff(c);
			else
				turnChatOn(c);
		}
	}
	public void turnChatOn(Client c){
		if(c.privateChat == 0)
			return;
		c.privateChat = 0;
		for(int i = 1; i<PlayerHandler.players.length; i++){
			if(PlayerHandler.players[i] == null || PlayerHandler.players[i].playerId == c.playerId)
				continue;
			Client player = (Client)PlayerHandler.players[i];
			if(player.playerRights >= 1 && player.playerRights <= 3)
				continue;
			long name = Misc.playerNameToInt64(c.playerName);
			if(player.getPA().isInPM(name))
				player.getPA().loadPM(name, 1);
		}
	}
	public void turnChatFriend(Client c){
		if(c.privateChat == 1)
			return;
		c.privateChat = 1;
		for(int i = 1; i<PlayerHandler.players.length; i++){
			if(PlayerHandler.players[i] == null || PlayerHandler.players[i].playerId == c.playerId)
				continue;
			Client player = (Client)PlayerHandler.players[i];
			if(player.playerRights >= 1 && player.playerRights <= 3)
				continue;
			long name = Misc.playerNameToInt64(c.playerName);
			if(player.getPA().isInPM(name)){
				boolean inPM = c.getPA().isInPM(Misc.playerNameToInt64(player.playerName));
				player.getPA().loadPM(name, inPM ? 1 : 0);
			}
		}
	}
	public void turnChatOff(Client c){
		if(c.privateChat == 2)
			return;
		c.privateChat = 2;
		for(int i = 1; i<PlayerHandler.players.length; i++){
			if(PlayerHandler.players[i] == null || PlayerHandler.players[i].playerId == c.playerId)
				continue;
			Client player = (Client)PlayerHandler.players[i];
			if(player.playerRights >= 1 && player.playerRights <= 3)
				continue;
			long name = Misc.playerNameToInt64(c.playerName);
			if(player.getPA().isInPM(name))
				player.getPA().loadPM(name, 0);
		}
	}
}