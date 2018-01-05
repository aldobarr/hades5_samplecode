package server.model.players.packets;

import server.model.players.Client;
import server.model.players.PacketType;

/**
 * Bank X Items
 **/
public class BankX1 implements PacketType{

	public static final int PART1 = 135;
	public static final int PART2 = 208;
	public int XremoveSlot, XinterfaceID, XremoveID, Xamount;

	@Override
	public void processPacket(Client c, int packetType, int packetSize){
		if(packetType == 135){
			c.xRemoveSlot = c.getInStream().readSignedWordBigEndian();
			c.xInterfaceId = c.getInStream().readUnsignedWordA();
			c.xRemoveId = c.getInStream().readSignedWordBigEndianA();
			if(c.xInterfaceId != 3900){
				synchronized(c){
					c.getOutStream().createFrame(27);
				}
			}
		}
		if(c.xInterfaceId == 3900){
			if(c.inTrade)
				return;
			if(c.myShopId == 14)
				c.getShops().skillBuy(c.xRemoveId, 1);
			else
				c.getShops().buyItem(c.xRemoveId, c.xRemoveSlot, 50); // Buy 50
			c.xRemoveSlot = 0;
			c.xInterfaceId = 0;
			c.xRemoveId = 0;
			return;
		}
	}
}
