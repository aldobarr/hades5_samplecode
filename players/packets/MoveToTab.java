package server.model.players.packets;

import server.model.players.Client;
import server.model.players.PacketType;

public class MoveToTab implements PacketType{
	public void processPacket(Client c, int packetType, int packetSize){
		int interfaceId = c.getInStream().readSignedWordBigEndianA();
		int toTab = c.getInStream().readSignedByteC();
		int fromSlot = c.getInStream().readSignedWordBigEndianA();
		if(interfaceId != 5382 || toTab > c.bank.tabs.size())
			return;
		c.bank.moveItemToTab(fromSlot, toTab);
	}
}