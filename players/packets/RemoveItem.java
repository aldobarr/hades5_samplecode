package server.model.players.packets;

import server.model.players.Client;
import server.model.players.PacketType;

/**
 * Remove Item
 **/
public class RemoveItem implements PacketType{

	@Override
	public void processPacket(Client c, int packetType, int packetSize){
		int interfaceId = c.getInStream().readUnsignedWordA();
		int removeSlot = c.getInStream().readUnsignedWordA();
		int removeId = c.getInStream().readSignedWordBigEndianA();
		switch(interfaceId){
			case 1688:
				if(removeSlot == c.playerRing){
					c.isWearingRing = false;
					c.npcId = -1;
					c.isNpc = false;
					c.updateRequired = true;
					c.appearanceUpdateRequired = true;
				}else if(removeSlot == c.playerWeapon)
					c.getPA().resetAutocast();
				c.getItems().removeItem(removeId, removeSlot);
				break;

			case 5064:
				if(!c.isBanking){
					c.getPA().closeAllWindows();
					break;
				}
				c.bank.deposit(removeId, removeSlot, 1);
				break;

			case 5382:
				if(!c.isBanking){
					c.getPA().closeAllWindows();
					break;
				}
				c.bank.withdraw(removeId, removeSlot, 1);
				break;

			case 3900:
				if(c.myShopId == 15)
					c.getShops().donorShopPrice(removeId);
				else if(c.myShopId == 19)
					c.getShops().voteShopPrice(removeId);
				else
					c.getShops().buyFromShopPrice(removeId, removeSlot);
				break;

			case 3823:
				if(c.myShopId != 15 && c.myShopId != 19)
					c.getShops().sellToShopPrice(removeId, removeSlot);
				break;

			case 3322:
				if(c.duel == null || (c.duel != null && c.duel.status == 0)){
					c.getTradeAndDuel().tradeItem(removeId, removeSlot, 1);
				}else if(c.duel != null && c.duel.status == 1){
					c.duel.stakeItem(c, removeId, removeSlot, 1);
				}
				break;

			case 3415:
				if(c.duel == null || (c.duel != null && c.duel.status == 0)){
					c.getTradeAndDuel().fromTrade(removeId, removeSlot, 1);
				}
				break;

			case 6669:
				if(c.duel != null && c.duel.status == 1)
					c.duel.fromDuel(c, removeId, removeSlot, 1);
				break;

			case 1119:
			case 1120:
			case 1121:
			case 1122:
			case 1123:
				c.getSmithing().readInput(c.playerLevel[c.playerSmithing], Integer.toString(removeId), c, 1);
				break;
		}
	}
}