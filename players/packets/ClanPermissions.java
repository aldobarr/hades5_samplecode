package server.model.players.packets;

import server.Server;
import server.model.players.Client;
import server.model.players.PacketType;

public class ClanPermissions implements PacketType{
	public void processPacket(Client c, int packetType, int packetSize){
		int id = c.getInStream().readSignedByte();
		int permission = c.getInStream().readSignedByte();
		if(permission < 0 || id < 0 || id > 4 || permission > 7)
			return;
		Server.clanChat.setPermission(c, id, permission);
	}
}