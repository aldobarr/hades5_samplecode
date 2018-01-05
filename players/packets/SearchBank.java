package server.model.players.packets;

import server.model.players.Client;
import server.model.players.PacketType;

public class SearchBank implements PacketType{
	public void processPacket(Client c, int packetType, int packetSize){
		String itemName = c.getInStream().readString();
		if(c.isBanking && c.isSearching)
			c.bank.createSearch(itemName.toLowerCase());
	}
}