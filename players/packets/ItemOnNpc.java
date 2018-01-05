package server.model.players.packets;

import server.model.items.UseItem;
import server.model.npcs.NPCHandler;
import server.model.players.Client;
import server.model.players.PacketType;

public class ItemOnNpc implements PacketType{

	@Override
	public void processPacket(Client c, int packetType, int packetSize){
		int itemId = c.getInStream().readSignedWordA();
		int i = c.getInStream().readSignedWordA();
		int slot = c.getInStream().readSignedWordBigEndian();
		if(NPCHandler.npcs[i] == null)
			return;
		int npcId = NPCHandler.npcs[i].npcType;

		UseItem.ItemonNpc(c, itemId, npcId, slot, i);
	}
}