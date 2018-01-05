package server.model.players.packets;

import server.model.players.Client;
import server.model.players.PacketType;

/**
 * Bank 5 Items
 **/
public class Bank5 implements PacketType{
	public boolean removeIDS(int id){
		int ids[] = {3900, 3322, 5382, 3415, 6669, 1119, 1120, 1121, 1122, 1123};
		for(int id1 : ids)
			if(id == id1)
				return true;
		return false;
	}

	@Override
	public void processPacket(Client c, int packetType, int packetSize){
		int interfaceId = c.getInStream().readSignedWordBigEndianA();
		int removeId = c.getInStream().readSignedWordBigEndianA();
		int removeSlot = c.getInStream().readSignedWordBigEndian();
		if(interfaceId == 3322 && (c.duel != null && c.duel.status > 0) && (c.inTrade || !c.inventory.hasItem(removeId)))
			return;
		if(!removeIDS(interfaceId) && (c.inTrade || !c.inventory.hasItem(removeId)))
			return;
		if(interfaceId == 5382 && (c.inTrade || !c.bank.bankHasItem(removeId)))
			return;
		switch(interfaceId){
			case 3900:
				if(c.myShopId == 14)
					c.getShops().skillBuy(removeId, 1);
				else
					c.getShops().buyItem(removeId, removeSlot, 1);
				break;
			case 3823:
				c.getShops().sellItem(removeId, removeSlot, 1);
				break;

			case 5064:
				if(!c.isBanking){
					c.getPA().closeAllWindows();
					return;
				}
				c.bank.deposit(removeId, removeSlot, 5);
				break;

			case 5382:
				if(!c.isBanking){
					c.getPA().closeAllWindows();
					return;
				}
				c.bank.withdraw(removeId, removeSlot, 5);
				break;

			case 3322:
				if(c.duel == null || (c.duel != null && c.duel.status == 0)){
					c.getTradeAndDuel().tradeItem(removeId, removeSlot, 5);
				}else if(c.duel != null && c.duel.status == 1){
					c.duel.stakeItem(c, removeId, removeSlot, 5);
				}
				break;

			case 3415:
				if(c.duel == null || (c.duel != null && c.duel.status == 0)){
					c.getTradeAndDuel().fromTrade(removeId, removeSlot, 5);
				}
				break;

			case 6669:
				if(c.duel != null && c.duel.status == 1)
					c.duel.fromDuel(c, removeId, removeSlot, 5);
				break;

			case 1119:
			case 1120:
			case 1121:
			case 1122:
			case 1123:
				c.getSmithing().readInput(c.playerLevel[c.playerSmithing], Integer.toString(removeId), c, 5);
				break;

		}
	}

}
