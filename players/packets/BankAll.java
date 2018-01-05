package server.model.players.packets;

import server.model.items.GameItem;
import server.model.items.Item;
import server.model.items.bank.BankItem;
import server.model.players.Client;
import server.model.players.PacketType;

/**
 * Bank All Items
 **/
public class BankAll implements PacketType{
	public boolean removeIDS(int id){
		int ids[] = {3900, 3415, 6669, 3322, 5382};
		for(int id1 : ids)
			if(id == id1)
				return true;
		return false;
	}

	@Override
	public void processPacket(Client c, int packetType, int packetSize){
		int removeSlot = c.getInStream().readUnsignedWordA();
		int interfaceId = c.getInStream().readUnsignedWord();
		int removeId = c.getInStream().readSignedWordBigEndianA();
		if(interfaceId == 3322 && (c.duel != null && c.duel.status > 0) && (c.inTrade || !c.inventory.hasItem(removeId)))
			return;
		if(!removeIDS(interfaceId) && (c.inTrade || !c.inventory.hasItem(removeId)))
			return;
		if(interfaceId == 5382 && (c.inTrade || !c.bank.bankHasItem(removeId)))
			return;
		switch(interfaceId){
			case 3900:
				if(c.myShopId == 14)
					c.getShops().skillBuy(removeId, 10);
				else
					c.getShops().buyItem(removeId, removeSlot, 10);
				break;
			case 3823:
				c.getShops().sellItem(removeId, removeSlot, 10);
				break;

			case 5064:
				if(!c.isBanking){
					c.getPA().closeAllWindows();
					return;
				}
				if(c.inventory.items[removeSlot] != null)
					c.bank.deposit(c.inventory.items[removeSlot].id - 1, removeSlot, Item.itemStackable[c.inventory.items[removeSlot].id - 1] ? c.inventory.items[removeSlot].amount : c.inventory.getItemCount(c.inventory.items[removeSlot].id - 1));
				break;

			case 5382:
				if(!c.isBanking){
					c.getPA().closeAllWindows();
					return;
				}
				int amount = 0;
				if(c.isSearching && c.bank.searchTab.get(removeSlot) != null){
					BankItem item = c.bank.searchTab.get(removeSlot);
					if(item == null)
						break;
					amount = c.bank.getBankAmount(item.id - 1);
				}else{
					BankItem item = c.bank.convertSlotToTab(removeSlot).get(c.bank.convertSlotToTabSlot(removeSlot));
					if(item == null)
						break;
					amount = c.bank.getBankAmount(item.id - 1);
				}
				c.bank.withdraw(removeId, removeSlot, amount);
				break;

			case 3322:
				if(c.duel == null || (c.duel != null && c.duel.status == 0))
					c.getTradeAndDuel().tradeItem(removeId, removeSlot, c.inventory.getItemCount(removeId));
				else if(c.duel != null && c.duel.status == 1)
					c.duel.stakeItem(c, removeId, removeSlot, c.inventory.getItemCount(removeId));
				break;

			case 3415:
				if(c.duel == null || (c.duel != null && c.duel.status == 0)){
					if(Item.itemStackable[removeId]){
						for(GameItem item : c.getTradeAndDuel().offeredItems){
							if(item.id == removeId){
								c.getTradeAndDuel().fromTrade(removeId, removeSlot, c.getTradeAndDuel().offeredItems.get(removeSlot).amount);
							}
						}
					}else{
						for(GameItem item : c.getTradeAndDuel().offeredItems){
							if(item.id == removeId){
								c.getTradeAndDuel().fromTrade(removeId, removeSlot, 28);
							}
						}
					}
				}
				break;

			case 6669:
				if(c.duel != null && c.duel.status == 1){
					if(Item.itemStackable[removeId] || Item.itemIsNote[removeId]){
						for(GameItem item : c.duel.getStake(c.playerId)){
							if(item.id == removeId){
								c.duel.fromDuel(c, removeId, removeSlot, c.duel.getStake(c.playerId).get(removeSlot).amount);
							}
						}
					}else{
						c.duel.fromDuel(c, removeId, removeSlot, 28);
					}
				}
				break;
		}
	}
}