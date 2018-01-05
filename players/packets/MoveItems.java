package server.model.players.packets;

import server.model.items.bank.Bank;
import server.model.minigames.Duel;
import server.model.players.Client;
import server.model.players.PacketType;

/**
 * Move Items
 **/
public class MoveItems implements PacketType{

	@Override
	public void processPacket(Client c, int packetType, int packetSize){
		// Old & wrong packet reading from winterLove
		// int somejunk = c.getInStream().readUnsignedWordA(); //junk
		// int itemFrom = c.getInStream().readUnsignedWordA();// slot1
		// int itemTo = (c.getInStream().readUnsignedWordA() -128);// slot2
		/*
		 * 5382/34453 - bank 3214/3724 - inv 5064/18579 - inv while banking
		 */

		int interfaceId = c.getInStream().readSignedWordBigEndianA();// +2
		c.getInStream().readSignedByteC();// +1 boolean insertMode = method() ==
											// 1
		int from = c.getInStream().readSignedWordBigEndianA();// +2
		int to = c.getInStream().readSignedWordBigEndian();// +2
		int fromTab = c.getInStream().readSignedByte();
		int toTab = c.getInStream().readSignedByte();
		// c.sendMessage("interfaceId:"+interfaceId+",from:"+from+",to:"+to+",insert:");

		if(c.inTrade || c.tradeStatus > 0){
			c.getTradeAndDuel().declineTrade(true);
			return;
		}
		if(c.duel != null && c.duel.status < 3){
			Duel.declineDuel(c, true);
			return;
		}
		if(c.duel != null && c.duel.status >= 3)
			return;
		if(interfaceId == 5382 && from >= 0 && to >= 0 && from < Bank.CAPACITY && to < Bank.CAPACITY)
			c.bank.moveItems(from, to, c.swap, fromTab, toTab);
		else
			c.inventory.moveItems(from, to);
	}
}