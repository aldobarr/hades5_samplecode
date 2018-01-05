package server.model.players.packets;

import server.Server;
import server.model.players.Client;
import server.model.players.PacketType;
import server.model.players.PlayerHandler;

public class ClanSettings implements PacketType{
	public void processPacket(Client c, int packetType, int packetSize){
		int id = c.getInStream().readSignedByte();
		String msg = c.getInStream().readString();
		if(id == 0){
			String info[] = msg.split(",");
			String rank = info[0].toLowerCase();
			String name = info[1].replace("_", " ");
			int rankId = Server.clanChat.getRankId(rank);
			int playerId = PlayerHandler.getPlayerId(name);
			Server.clanChat.setMemberPermission(c, playerId, rankId, name);
		}else{
			int playerId = PlayerHandler.getPlayerId(msg);
			if(playerId > 0)
				Server.clanChat.kick(c, playerId, msg);
		}
	}
}