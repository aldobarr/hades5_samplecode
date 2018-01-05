package server.model.players.packets;

import server.model.items.UseItem;
import server.model.players.Client;
import server.model.players.PacketType;
import server.model.players.PlayerHandler;

public class ItemOnPlayer implements PacketType{

	@Override
	public void processPacket(Client c, int packetType, int packetSize){
		int slot = c.getInStream().readSignedWord();
		int i = c.getInStream().readSignedWord();
		int itemId = c.getInStream().readSignedWord();
		Client c2 = (Client)PlayerHandler.players[i];
		UseItem.ItemonPlayer(c, itemId, c2, slot);
	}
}