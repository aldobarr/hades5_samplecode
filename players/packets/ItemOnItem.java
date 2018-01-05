package server.model.players.packets;

/**
 * @author Ryan / Lmctruck30
 */

import server.model.items.UseItem;
import server.model.players.Client;
import server.model.players.PacketType;

public class ItemOnItem implements PacketType{

	@Override
	public void processPacket(Client c, int packetType, int packetSize){
		int usedWithSlot = c.getInStream().readUnsignedWord();
		int itemUsedSlot = c.getInStream().readUnsignedWordA();
		if(usedWithSlot >= c.inventory.items.length || usedWithSlot < 0 || itemUsedSlot >= c.inventory.items.length || itemUsedSlot < 0)
			return;
		if(c.inventory.items[usedWithSlot] == null || c.inventory.items[itemUsedSlot] == null)
			return;
		int useWith = c.inventory.items[usedWithSlot].id - 1;
		int itemUsed = c.inventory.items[itemUsedSlot].id - 1;
		UseItem.ItemonItem(c, itemUsed, useWith);
	}

}
