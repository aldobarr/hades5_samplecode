package server.model.players.packets;

import server.util.Misc;
import server.model.players.Client;
import server.model.players.PacketType;
import server.Server;

/**
 * Chat
 **/
public class ClanChat implements PacketType{

	@Override
	public void processPacket(Client c, int packetType, int packetSize){
		long l = c.getInStream().readQWord();
		boolean join = l > 0;
		l = Math.abs(l);
		String textSent = Misc.longToPlayerName2(l);
		textSent = textSent.replaceAll("_", " ");
		// c.sendMessage(textSent);
		if(join)
			Server.clanChat.handleClanChat(c, textSent);
		else
			Server.clanChat.setName(c, textSent);
	}
}
