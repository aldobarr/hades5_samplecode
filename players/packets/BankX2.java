package server.model.players.packets;

import server.model.players.Client;
import server.model.players.PacketType;

/**
 * Entering an X amount of items to be banked, traded, or duelled.
 */

public class BankX2 implements PacketType{
	public boolean removeIDS(int id){
		int ids[] = {3900, 3415, 6669, 3322, 5382};
		for(int id1 : ids)
			if(id == id1)
				return true;
		return false;
	}

	@Override
	public void processPacket(Client c, int packetType, int packetSize){
		int amount = c.getInStream().readDWord();
		if(amount <= 0)
			return;
		if(c.xInterfaceId == 3322 && (c.duel != null && c.duel.status > 0) && c.inTrade)
			return;
		if(!removeIDS(c.xInterfaceId) && (c.inTrade || !c.inventory.hasItem(c.xRemoveId) || amount == 0))
			return;
		if(c.xInterfaceId == 5382 && (amount == 0 || !c.bank.bankHasItem(c.xRemoveId) || c.inTrade))
			return;
		switch(c.xInterfaceId){
			case 5064:
				if(!c.isBanking){
					c.getPA().closeAllWindows();
					return;
				}
				if(c.inventory.items[c.xRemoveSlot] != null)
					c.bank.deposit(c.inventory.items[c.xRemoveSlot].id - 1, c.xRemoveSlot, amount > c.inventory.getItemCount(c.xRemoveId) ? c.inventory.getItemCount(c.xRemoveId) : amount);
				break;

			case 3823:
				c.getShops().sellItem(c.xRemoveId, c.xRemoveSlot, amount);
				break;

			case 5382:
				if(!c.isBanking){
					c.getPA().closeAllWindows();
					return;
				}
				c.bank.withdraw(c.xRemoveId, c.xRemoveSlot, amount > c.bank.getBankAmount(c.xRemoveId) ? c.bank.getBankAmount(c.xRemoveId) : amount);
				break;

			case 3322:
				if(c.duel == null || (c.duel != null && c.duel.status == 0))
					c.getTradeAndDuel().tradeItem(c.xRemoveId, c.xRemoveSlot, amount > c.inventory.getItemCount(c.xRemoveId) ? c.inventory.getItemCount(c.xRemoveId) : amount);
				else if(c.duel != null && c.duel.status == 1)
					c.duel.stakeItem(c, c.xRemoveId, c.xRemoveSlot, amount > c.inventory.getItemCount(c.xRemoveId) ? c.inventory.getItemCount(c.xRemoveId) : amount);
				break;

			case 3415:
				if(c.duel == null || (c.duel != null && c.duel.status == 0))
					c.getTradeAndDuel().fromTrade(c.xRemoveId, c.xRemoveSlot, amount);
				break;

			case 6669:
				if(c.duel != null && c.duel.status == 1)
					c.duel.fromDuel(c, c.xRemoveId, c.xRemoveSlot, amount);
				break;
		}
	}
}