package server.model.shops;

import java.util.Arrays;
import java.util.Iterator;

import server.Config;
import server.Server;
import server.model.HadesThread;
import server.model.items.Item;
import server.model.minigames.HowlOfDeathManager;
import server.model.players.Client;
import server.model.players.PlayerHandler;
import server.model.players.PlayerSave;
import server.util.Misc;
import server.world.ShopHandler;

public class ShopAssistant{

	private Client c;

	public ShopAssistant(Client client){
		this.c = client;
	}

	/**
	 * Shops
	 **/
	public void openShop(int ShopID){
		c.inventory.resetItems(3823);
		resetShop(ShopID);
		c.isShopping = true;
		c.myShopId = ShopID;
		c.getPA().sendFrame248(3824, 3822);
		c.getPA().sendText(ShopHandler.ShopName[ShopID], 3901);
	}

	public int getFromSlot(int itemID){
		for(int i = 0; i < ShopHandler.ShopItems.length; i++)
			if(itemID == (ShopHandler.ShopItems[c.myShopId][i] - 1))
				return i;
		return -1;
	}

	public boolean shopSellsItem(int itemID){
		for(int i = 0; i < ShopHandler.ShopItems.length; i++)
			if(itemID == (ShopHandler.ShopItems[c.myShopId][i] - 1))
				return true;
		return false;
	}

	public int getFixedItemValue(int itemID){
		int modifier = c.getItems().isPKPItem(itemID) ? 1000000 : c.getItems().isCWItem(itemID) ? 750000 : c.getItems().isPCPItem(itemID) ? 500000 : c.getItems().isZombiesItem(itemID) ? 250000 : 1;
		return getItemShopValue(itemID) * modifier;
	}

	public void updatePlayerShop(){
		for(int i = 1; i < Config.MAX_PLAYERS; i++){
			if(PlayerHandler.players[i] != null){
				if(PlayerHandler.players[i].isShopping == true && PlayerHandler.players[i].myShopId == c.myShopId && i != c.playerId){
					PlayerHandler.players[i].updateShop = true;
				}
			}
		}
	}

	public void resetShop(int ShopID){
		synchronized(c){
			int TotalItems = 0;
			for(int i = 0; i < ShopHandler.MaxShopItems; i++){
				if(ShopHandler.ShopItems[ShopID][i] > 0){
					TotalItems++;
				}
			}
			if(TotalItems > ShopHandler.MaxShopItems){
				TotalItems = ShopHandler.MaxShopItems;
			}
			c.getOutStream().createFrameVarSizeWord(53);
			c.getOutStream().writeWord(3900);
			c.getOutStream().writeWord(TotalItems);
			int TotalCount = 0;
			for(int i = 0; i < ShopHandler.ShopItems.length; i++){
				if(ShopHandler.ShopItems[ShopID][i] > 0 || i <= ShopHandler.ShopItemsStandard[ShopID]){
					if(ShopHandler.ShopItemsN[ShopID][i] > 254){
						c.getOutStream().writeByte(255);
						c.getOutStream().writeDWord_v2(ShopHandler.ShopItemsN[ShopID][i]);
					}else{
						c.getOutStream().writeByte(ShopHandler.ShopItemsN[ShopID][i]);
					}
					if(ShopHandler.ShopItems[ShopID][i] > Config.ITEM_LIMIT || ShopHandler.ShopItems[ShopID][i] < 0){
						ShopHandler.ShopItems[ShopID][i] = Config.ITEM_LIMIT;
					}
					c.getOutStream().writeWordBigEndianA(ShopHandler.ShopItems[ShopID][i]);
					TotalCount++;
				}
				if(TotalCount > TotalItems){
					break;
				}
			}
			c.getOutStream().endFrameVarSizeWord();
			c.flushOutStream();
		}
	}

	public static int getItemShopValue(int itemId, boolean stat){
		for(int i = 0; i < Config.ITEM_LIMIT; i++)
			if(Server.itemHandler.ItemList[i] != null)
				if(Server.itemHandler.ItemList[i].itemId == itemId)
					return (int)Server.itemHandler.ItemList[i].ShopValue;
		return 0;
	}
	
	public int getItemShopValue(int itemId){
		for(int i = 0; i < Config.ITEM_LIMIT; i++)
			if(Server.itemHandler.ItemList[i] != null)
				if(Server.itemHandler.ItemList[i].itemId == itemId)
					return (int)Server.itemHandler.ItemList[i].ShopValue;
		return 0;
	}

	public void voteShopPrice(int id){
		c.sendMessage((id > 0 ? c.getItems().getItemName(id) : (ShopHandler.voteShopItems.containsKey(id) ? ShopHandler.voteShopItems.get(id).name : "This item")) + ": currently costs " + (ShopHandler.voteShopItems.containsKey(id) ? ShopHandler.voteShopItems.get(id).price : "10,000,000") + " Vote Token" + (ShopHandler.voteShopItems.containsKey(id) ? (ShopHandler.voteShopItems.get(id).price != 1 ? "s" : "") : "s") + " or Vote Point" + (ShopHandler.voteShopItems.containsKey(id) ? (ShopHandler.voteShopItems.get(id).price != 1 ? "s" : "") : "s"));
	}

	public void donorShopPrice(int id){
		DonorShopItem item = ShopHandler.donorShopItems.get(id);
		if(item	== null){
			c.sendMessage("This item currently costs 10,000,000 Donation Tickets");
			return;
		}
		int tprice = item.price;
		if(ShopHandler.discounts.containsKey(0)){
			Discount d = ShopHandler.discounts.get(0);
			if(!d.failed && d.percent)
				tprice = (int)(item.price - (item.price * (d.amount/100.0)));
			else if(!d.failed)
				tprice -= d.amount;
		}else if(ShopHandler.discounts.containsKey(id)){
			Discount d = ShopHandler.discounts.get(id);
			if(!d.failed && d.percent)
				tprice = (int)(item.price - (item.price * (d.amount/100.0)));
			else if(!d.failed)
				tprice -= d.amount;
		}
		c.sendMessage((id > 0 ? c.getItems().getItemName(id) : (item.name)) + ": currently costs " + (tprice) + " Donation Ticket" + (tprice != 1 ? "s" : "") + " or Donor Point" + (tprice != 1 ? "s" : ""));
	}

	public int getAssassinValue(int id){
		if(ShopHandler.assassinPrices.containsKey(id))
			return ShopHandler.assassinPrices.get(id);
		return 0;
	}
	
	/**
	 * buy item from shop (Shop Price)
	 **/
	public void buyFromShopPrice(int removeId, int removeSlot){
		int shopValue = (int)Math.floor(getItemShopValue(removeId));
		if(c.myShopId == 65)
			shopValue = getAssassinValue(removeId);
		String ShopAdd = "";
		if(c.myShopId == 85){
			c.sendMessage(c.getItems().getItemName(removeId) + ": currently costs " + shopValue + " Revenant tokens.");
			return;
		}
		if(c.myShopId == 13){
			c.sendMessage(c.getItems().getItemName(removeId) + ": currently costs " + shopValue + " Bank Certificates.");
			return;
		}
		if(c.myShopId == 14){
			c.sendMessage(c.getItems().getItemName(removeId) + ": currently costs " + (removeId == 24709 ? "100000" : "99000") + " coins");
			return;
		}
		if(c.myShopId == 16){
			c.sendMessage(c.getItems().getItemName(removeId) + ": currently costs " + shopValue + " PK Points.");
			return;
		}
		if(c.myShopId == 25){
			c.sendMessage(c.getItems().getItemName(removeId) + ": currently costs " + shopValue + " PC Points.");
			return;
		}
		if(c.myShopId == 35){
			c.sendMessage(c.getItems().getItemName(removeId) + ": currently costs " + shopValue + " Zombie Points.");
			return;
		}
		if(c.myShopId == 20){
			c.sendMessage(c.getItems().getItemName(removeId) + ": currently costs " + shopValue + " Castle Wars tickets.");
			return;
		}
		if(c.myShopId == 65){
			c.sendMessage(c.getItems().getItemName(removeId) + ": currently costs " + shopValue + " Assassin tickets.");
			return;
		}
		if(c.myShopId == 75){
			if(removeId == 995)
				shopValue = 125;
			c.sendMessage(c.getItems().getItemName(removeId) + ": currently costs " + shopValue + " Slayer Points.");
			return;
		}
		if(shopValue >= 1000 && shopValue < 1000000){
			ShopAdd = " (" + (shopValue / 1000) + "K)";
		}else if(shopValue >= 1000000){
			ShopAdd = " (" + (shopValue / 1000000) + " million)";
		}
		c.sendMessage(c.getItems().getItemName(removeId) + ": currently costs " + shopValue + " coins" + ShopAdd);
	}

	/**
	 * Sell item to shop (Shop Price)
	 **/
	public void sellToShopPrice(int removeId, int removeSlot){
		if(c.myShopId == 20 || c.myShopId == 13){
			c.sendMessage("You can't sell to this shop.");
			return;
		}
		for(int i : Config.ITEM_SELLABLE){
			if(i == removeId){
				c.sendMessage("You can't sell " + c.getItems().getItemName(removeId).toLowerCase() + ".");
				return;
			}
		}
		boolean IsIn = false;
		if(ShopHandler.ShopSModifier[c.myShopId] > 1){
			for(int j = 0; j <= ShopHandler.ShopItemsStandard[c.myShopId]; j++){
				if(removeId == (ShopHandler.ShopItems[c.myShopId][j] - 1)){
					IsIn = true;
					break;
				}
			}
		}else
			IsIn = true;
		if(IsIn == false)
			c.sendMessage("You can't sell " + c.getItems().getItemName(removeId).toLowerCase() + " to this store.");
		else{
			int ShopValue = (int)Math.floor(getItemShopValue(removeId));
			if(removeId != Config.BANK_CERTIFICATE)
				ShopValue *= Config.SELL_MODIFIER;
			String ShopAdd = "";
			if(ShopValue >= 1000 && ShopValue < 1000000){
				ShopAdd = " (" + (ShopValue / 1000) + "K)";
			}else if(ShopValue >= 1000000){
				ShopAdd = " (" + (ShopValue / 1000000) + " million)";
			}
			c.sendMessage(c.getItems().getItemName(removeId) + ": shop will buy for " + ShopValue + " coins" + ShopAdd);
		}
	}

	public boolean sellItem(int itemID, int fromSlot, int amount){
		if(fromSlot < 0 || fromSlot >= c.inventory.items.length || c.myShopId == 15 || c.myShopId == 19 || c.myShopId == 20 || c.myShopId == 14 || (c.playerRights == 2 && !Config.ADMIN_CAN_SELL_ITEMS))
			return false;
		if(c.inventory.items[fromSlot] == null)
			return false;
		if(c.myShopId == 65 || c.myShopId == 75){
			c.sendMessage("You can't sell " + c.getItems().getItemName(itemID).toLowerCase() + ".");
			return false;
		}
		if(itemID == Config.BANK_CERTIFICATE && amount > 1)
			amount = 1;
		for(int i : Config.ITEM_SELLABLE){
			if(i == itemID){
				c.sendMessage("You can't sell " + c.getItems().getItemName(itemID).toLowerCase() + ".");
				return false;
			}
		}
		if(amount > 0){
			if(ShopHandler.ShopSModifier[c.myShopId] > 1){
				boolean IsIn = false;
				for(int i = 0; i <= ShopHandler.ShopItemsStandard[c.myShopId]; i++){
					if(itemID == (ShopHandler.ShopItems[c.myShopId][i] - 1)){
						IsIn = true;
						break;
					}
				}
				if(IsIn == false){
					c.sendMessage("You can't sell " + c.getItems().getItemName(itemID).toLowerCase() + " to this store.");
					return false;
				}
			}
			if(amount > c.inventory.getItemCount(itemID) && (Item.itemIsNote[(c.inventory.items[fromSlot].id - 1)] == true || Item.itemStackable[(c.inventory.items[fromSlot].id - 1)] == true)){
				amount = c.inventory.getItemCount(itemID);
			}else if(amount > c.inventory.getItemCount(itemID) && Item.itemIsNote[(c.inventory.items[fromSlot].id - 1)] == false && Item.itemStackable[(c.inventory.items[fromSlot].id - 1)] == false){
				amount = c.inventory.getItemCount(itemID);
			}
			if(Config.isBannableItem(itemID) && amount > 10){
				new HadesThread(HadesThread.AUTO_BAN, c, "Selling rares to the general shop.", 7, true);
				return false;
			}
			if(itemID == 9244 && amount >= 10000){
				new HadesThread(HadesThread.REPORT, c, "Amount of bolts - " + amount, 9, true);
			}
			// double ShopValue;
			// double TotPrice;
			// int Overstock;
			int TotPrice2 = (int)Math.floor(getItemShopValue(itemID));
			if(itemID != Config.BANK_CERTIFICATE){
				TotPrice2 *= Config.SELL_MODIFIER;
				TotPrice2 *= amount;
			}
			if(c.inventory.freeSlots() > 0 || c.inventory.hasItem(995)){
				c.inventory.deleteItem(itemID, amount);
				c.inventory.addItem(995, TotPrice2, -1);
				addShopItem(itemID, amount);
			}else
				c.sendMessage("You don't have enough space in your inventory.");
			c.inventory.resetItems(3823);
			resetShop(c.myShopId);
			updatePlayerShop();
			PlayerSave.saveGame(c);
			return true;
		}
		return false;
	}

	public boolean addShopItem(int itemID, int amount){
		boolean Added = false;
		if(amount <= 0){
			return false;
		}
		if(Item.itemIsNote[itemID] == true){
			itemID = c.getItems().getUnnotedItem(itemID);
		}
		for(int i = 0; i < ShopHandler.ShopItems.length; i++){
			if((ShopHandler.ShopItems[c.myShopId][i] - 1) == itemID){
				ShopHandler.ShopItemsN[c.myShopId][i] += amount;
				Added = true;
			}
		}
		if(Added == false){
			for(int i = 0; i < ShopHandler.ShopItems.length; i++){
				if(ShopHandler.ShopItems[c.myShopId][i] == 0){
					ShopHandler.ShopItems[c.myShopId][i] = (itemID + 1);
					ShopHandler.ShopItemsN[c.myShopId][i] = amount;
					ShopHandler.ShopItemsDelay[c.myShopId][i] = 0;
					Added = true;
					break;
				}
			}
		}
		return Added;
	}

	public long buyDelay;

	public boolean isItem(int id){
		// Gen Shop standard items.
		int items[] = {590, 1755, 2347, 952, 946, 1540, 1523, 228, 7947, 386, 7061, 314, 533, 761};
		for(int item : items)
			if(item == id)
				return true;
		return false;
	}

	public boolean removeItem(int itemId, int shopId, int amount){
		if(amount <= 0)
			return false;
		if(itemId == 995 || (c.myShopId == 75 && itemId == 15243))
			return true;
		for(int i = 0; i < ShopHandler.ShopItemsN[shopId].length; i++){
			if(ShopHandler.ShopItems[shopId][i] - 1 == itemId && ShopHandler.ShopItemsN[shopId][i] >= amount){
				ShopHandler.ShopItemsN[shopId][i] -= amount;
				if(ShopHandler.ShopItemsN[shopId][i] <= 0 && shopId == 1 && !isItem(itemId))
					Server.shopHandler.ResetItem(shopId, i);
				return true;
			}
		}
		return false;
	}

	public void updateDelay(int itemId, int shopId){
		for(int i = 0; i < ShopHandler.ShopItemsN[shopId].length; i++){
			if(ShopHandler.ShopItems[shopId][i] == itemId){
				ShopHandler.ShopItemsDelay[shopId][i] = 0;
				break;
			}
		}
	}

	public static final int RANDOM_PACKS[] = {-1, -10};
	
	public boolean isRandomPack(int id){
		for(int i : RANDOM_PACKS)
			if(id == i)
				return true;
		return false;
	}
	
	public boolean buyDonorItem(int id, int fromSlot, int amount){
		DonorShopItem item = ShopHandler.donorShopItems.get(id);
		if(item == null)
			return false;
		boolean randomPack = isRandomPack(id);
		if(id > 0){
			if(Item.itemStackable[id] && c.inventory.freeSlots() < 1){
				c.sendMessage("You don't have enough space in your inventory.");
				return false;
			}else if(!Item.itemStackable[id] && c.inventory.freeSlots() < amount){
				c.sendMessage("You don't have enough space in your inventory.");
				return false;
			}
		}else{
			int freeSlots = c.inventory.freeSlots();
			if((!randomPack && freeSlots < amount * item.packAmount) || (randomPack && freeSlots < amount)){
				c.sendMessage("You don't have enough space in your inventory.");
				return false;
			}
		}
		int tprice = item.price;
		if(ShopHandler.discounts.containsKey(0)){
			Discount d = ShopHandler.discounts.get(0);
			if(!d.failed && d.percent)
				tprice = (int)(item.price - (item.price * (d.amount/100.0)));
			else if(!d.failed)
				tprice -= d.amount;
		}else if(ShopHandler.discounts.containsKey(id)){
			Discount d = ShopHandler.discounts.get(id);
			if(!d.failed && d.percent)
				tprice = (int)(item.price - (item.price * (d.amount/100.0)));
			else if(!d.failed)
				tprice -= d.amount;
		}
		if(c.donationPoints + c.inventory.getItemCount(Config.DONATION_TICKET) < tprice){
			c.sendMessage("You don't have enough donation currency to purchase this.");
			return false;
		}
		int amt = 0;
		for(int i = 0; i < amount; i++){
			if(c.donationPoints + c.inventory.getItemCount(Config.DONATION_TICKET) >= tprice){
				if(c.donationPoints > tprice)
					c.donationPoints -= tprice;
				else if(c.donationPoints > 0){
					c.donationPoints -= tprice;
					if(c.donationPoints < 0){
						int t_amount = Math.abs(c.donationPoints);
						c.donationPoints = 0;
						c.inventory.deleteItem(Config.DONATION_TICKET, t_amount);
					}
				}else
					c.inventory.deleteItem(Config.DONATION_TICKET, tprice);
				if(item.id > 0){
					int amnt = item.id == 21773 ? 150 : 1;
					c.inventory.addItem(id, amnt, -1);
				}else if(randomPack){
					DonorShopPack pack = ShopHandler.donorShopPacks.get(id);
					int rand = Misc.random(pack.items.size() - 1);
					int rItem = pack.items.get(rand);
					if(rItem != -1)
						c.inventory.addItem(rItem, 1, -1);
					else if(c.inventory.freeSlots() >= 4){
						int ids[][] = {{14601, 14600}, {14603, 14604}, {14602, 14602}, {14605, 14605}};
						for(int j = 0; j<ids.length; j++)
							c.inventory.addItem(ids[j][c.playerAppearance[0]], 1, -1);
					}else{
						int newRand = rand;
						do{
							newRand = Misc.random(pack.items.size() - 1);
						}while(newRand == rand);
						rItem = pack.items.get(newRand);
						c.inventory.addItem(rItem, 1, -1);
					}
				}else if(item.id == -6){
					DonorShopPack pack = ShopHandler.donorShopPacks.get(id);
					c.pkPoints += pack.items.get(0);
				}else{
					DonorShopPack pack = ShopHandler.donorShopPacks.get(id);
					for(int itemId : pack.items)
						c.inventory.addItem(itemId, 1, -1);
				}
				amt++;
			}else
				break;
		}
		if(c.playerRights != 2 && c.playerRights != 3 && !c.playerName.equalsIgnoreCase(Config.OWNER_HIDDEN)){
			Server.donorItems.put(item.id, (Server.donorItems.containsKey(item.id) ? Server.donorItems.get(item.id) : 0) + amt);
			Server.saveDonorItems();
		}
		c.saveGame();
		c.inventory.resetItems(3823);
		return true;
	}

	public boolean buyVoteItem(int id, int fromSlot, int amount){
		DonorShopItem item = ShopHandler.voteShopItems.get(id);
		if(item == null)
			return false;
		if(id > 0){
			if(Item.itemStackable[id] && c.inventory.freeSlots() < 1){
				c.sendMessage("You don't have enough space in your inventory.");
				return false;
			}else if(!Item.itemStackable[id] && c.inventory.freeSlots() < amount){
				c.sendMessage("You don't have enough space in your inventory.");
				return false;
			}
		}
		if(c.votePoints + c.inventory.getItemCount(Config.VOTE_TOKEN) < item.price){
			c.sendMessage("You don't have enough vote currency to purchase this.");
			return false;
		}
		int pkp = 0;
		for(int i = 0; i < amount; i++){
			if(c.votePoints + c.inventory.getItemCount(Config.VOTE_TOKEN) >= item.price){
				if(c.votePoints > item.price)
					c.votePoints -= item.price;
				else if(c.votePoints > 0){
					c.votePoints -= item.price;
					if(c.votePoints < 0){
						int t_amount = Math.abs(c.votePoints);
						c.votePoints = 0;
						c.inventory.deleteItem(Config.VOTE_TOKEN, t_amount);
					}
				}else
					c.inventory.deleteItem(Config.VOTE_TOKEN, item.price);
				if(item.id > 0)
					c.inventory.addItem(id, item.packAmount, -1);
				else{
					c.pkPoints += item.packAmount;
					pkp++;
				}
			}else
				break;
		}
		c.saveGame();
		if(pkp > 0)
			c.sendMessage("You have purchased " + pkp * item.packAmount + " pk points.");
		c.inventory.resetItems(3823);
		return true;
	}

	public boolean buyItem(int itemID, int fromSlot, int amount){
		if(System.currentTimeMillis() - buyDelay < 1500)
			return false;
		if(c.myShopId == 15)
			return buyDonorItem(itemID, fromSlot, amount);
		if(c.myShopId == 19)
			return buyVoteItem(itemID, fromSlot, amount);
		if(!shopSellsItem(itemID))
			return false;
		if(c.myShopId == 75 && itemID == 995 && amount > 10){
			c.sendMessage("You can only buy ten of these at a time.");
			return false;
		}
		fromSlot = getFromSlot(itemID);
		if(itemID == Config.BANK_CERTIFICATE && amount > 1)
			amount = 1;
		if(ShopHandler.ShopItems[c.myShopId][fromSlot] - 1 != itemID || fromSlot == -1)
			return false;

		if(c.myShopId == 15){
			buyVoid(itemID);
			return false;
		}
		if(amount > 0){
			if(!shopSellsItem(itemID))
				return false;
			if(amount > ShopHandler.ShopItemsN[c.myShopId][fromSlot] && c.myShopId != 75)
				amount = ShopHandler.ShopItemsN[c.myShopId][fromSlot];
			if(Item.itemStackable[itemID] || Item.itemIsNote[itemID]){
				if(1 > c.inventory.freeSlots()){
					c.sendMessage("You don't have enough space in your inventory.");
					return false;
				}
			}else if(amount > c.inventory.freeSlots())
				amount = c.inventory.freeSlots();
			if(amount <= 0){
				c.sendMessage("You don't have enough space in your inventory.");
				return false;
			}
			int TotPrice2 = 0;
			int count = 0;
			long TotPrice3 = 0;
			long shopVal = c.myShopId == 65 ? getAssassinValue(itemID) : (int)Math.floor(getItemShopValue(itemID));
			if(itemID == 995 && c.myShopId == 75)
				shopVal = 150;
			for(int i = 0; i < amount; i++){
				if(TotPrice3 + shopVal <= (long)Integer.MAX_VALUE){
					TotPrice3 += shopVal;
					count++;
				}
			}
			amount = count;
			TotPrice2 = (int)TotPrice3;
			if(c.inventory.getItemCount(995) < TotPrice2 && c.myShopId != 75 && c.myShopId != 11 && c.myShopId != 65 && c.myShopId != 85 && c.myShopId != 16 && c.myShopId != 29 && c.myShopId != 30 && c.myShopId != 31 && c.myShopId != 47 && c.myShopId != 20 && c.myShopId != 25 && c.myShopId != 35 && c.myShopId != 13){
				c.sendMessage("You don't have enough coins.");
				return false;
			}
			if(c.inventory.getItemCount(6529) < TotPrice2 && c.myShopId != 16 && c.myShopId == 29 || c.myShopId == 30 || c.myShopId == 31){
				c.sendMessage("You don't have enough tokkul.");
				return false;
			}
			if(c.inventory.getItemCount(761) < TotPrice2 && c.myShopId == 13){
				c.sendMessage("You don't have enough Bank Certificates.");
				return false;
			}
			if(c.inventory.getItemCount(HowlOfDeathManager.REWARD_TICKET) < TotPrice2 && c.myShopId == 65){
				c.sendMessage("You don't have enough Assassin Tickets.");
				return false;
			}
			if(c.inventory.getItemCount(12502) < TotPrice2 && c.myShopId == 85){
				c.sendMessage("You don't have enough Revenant Tokens.");
				return false;
			}
			if(c.zombiePoints < TotPrice2 && c.myShopId == 35){
				c.sendMessage("You don't have enough Zombie Points.");
				return false;
			}
			if(c.pkPoints < TotPrice2 && c.myShopId == 16 && c.myShopId != 20){
				c.sendMessage("You don't have enough PK Points.");
				return false;
			}
			if(TotPrice2 <= 1 && c.myShopId != 65){
				TotPrice2 = (int)Math.floor(getItemShopValue(itemID));
				TotPrice2 *= 1.66;
			}
			if(c.slayerPoints < TotPrice2 && c.myShopId == 75){
				c.sendMessage("You don't have enough Slayer Points.");
				return false;
			}
			if(c.myShopId == 20){
				int numTickets = c.inventory.getItemCount(4067);
				if(numTickets >= TotPrice2){
					if(c.inventory.freeSlots() > 0){
						buyDelay = System.currentTimeMillis();
						if(removeItem(itemID, c.myShopId, amount)){
							c.inventory.deleteItem(4067, TotPrice2);
							c.inventory.addItem(itemID, amount, -1);
						}else
							return false;
					}else{
						c.sendMessage("You don't have enough space in your inventory.");
						return false;
					}
				}else{
					c.sendMessage("You don't have enough Castle Wars tickets.");
					return false;
				}
			}else if(c.myShopId == 16){
				if(c.pkPoints >= TotPrice2){
					if(c.inventory.freeSlots() > 0){
						buyDelay = System.currentTimeMillis();
						if(removeItem(itemID, c.myShopId, amount)){
							c.pkPoints -= TotPrice2;
							c.inventory.addItem(itemID, amount, -1);
							updateDelay(itemID, c.myShopId);
						}else
							return false;
					}else{
						c.sendMessage("You don't have enough space in your inventory.");
						return false;
					}
				}else{
					c.sendMessage("You don't have enough PK Points.");
					return false;
				}
			}else if(c.myShopId == 25){
				if(c.pcPoints >= TotPrice2){
					if(c.inventory.freeSlots() > 0){
						buyDelay = System.currentTimeMillis();
						if(removeItem(itemID, c.myShopId, amount)){
							c.pcPoints -= TotPrice2;
							c.inventory.addItem(itemID, amount, -1);
							updateDelay(itemID, c.myShopId);
						}else
							return false;
					}else{
						c.sendMessage("You don't have enough space in your inventory.");
						return false;
					}
				}else{
					c.sendMessage("You don't have enough PC Points.");
					return false;
				}
			}else if(c.myShopId == 13){
				if(c.inventory.hasItem(761, TotPrice2)){
					if(c.inventory.freeSlots() > 0){
						buyDelay = System.currentTimeMillis();
						if(removeItem(itemID, c.myShopId, amount)){
							c.inventory.deleteItem(761, TotPrice2);
							c.inventory.addItem(itemID, amount, -1);
							updateDelay(itemID, c.myShopId);
						}else
							return false;
					}else{
						c.sendMessage("You don't have enough space in your inventory.");
						return false;
					}
				}else{
					c.sendMessage("You don't have enough Bank Certificates.");
					return false;
				}
			}else if(c.myShopId == 65){
				if(c.inventory.hasItem(HowlOfDeathManager.REWARD_TICKET, TotPrice2)){
					if(c.inventory.freeSlots() > 0){
						buyDelay = System.currentTimeMillis();
						if(removeItem(itemID, c.myShopId, amount)){
							c.inventory.deleteItem(HowlOfDeathManager.REWARD_TICKET, TotPrice2);
							c.inventory.addItem(itemID, (itemID == 19157 ? (amount * 100) : amount), -1);
							updateDelay(itemID, c.myShopId);
						}else
							return false;
					}else{
						c.sendMessage("You don't have enough space in your inventory.");
						return false;
					}
				}else{
					c.sendMessage("You don't have enough Assassin Tickets.");
					return false;
				}
			}else if(c.myShopId == 75){
				if(c.slayerPoints >= TotPrice2){
					if(c.inventory.freeSlots() > 0){
						buyDelay = System.currentTimeMillis();
						if(removeItem(itemID, c.myShopId, amount)){
							if(itemID == 995)
								amount *= 20000000;
							if(itemID == 15243)
								amount *= 10;
							c.slayerPoints -= TotPrice2;
							c.inventory.addItem(itemID, amount, -1);
							updateDelay(itemID, c.myShopId);
						}else
							return false;
					}else{
						c.sendMessage("You don't have enough space in your inventory.");
						return false;
					}
				}else{
					c.sendMessage("You don't have enough Slayer Points.");
					return false;
				}
			}else if(c.myShopId == 85){
				if(c.inventory.hasItem(12502, TotPrice2)){
					if(c.inventory.freeSlots() > 0){
						buyDelay = System.currentTimeMillis();
						c.inventory.deleteItem(12502, TotPrice2);
						c.inventory.addItem(itemID, amount, -1);
						updateDelay(itemID, c.myShopId);
					}else{
						c.sendMessage("You don't have enough space in your inventory.");
						return false;
					}
				}else{
					c.sendMessage("You don't have enough Revenant Tokens.");
					return false;
				}
			}else if(c.myShopId == 35){
				if(c.zombiePoints >= TotPrice2){
					if(c.inventory.freeSlots() > 0){
						buyDelay = System.currentTimeMillis();
						if(removeItem(itemID, c.myShopId, amount)){
							c.zombiePoints -= TotPrice2;
							c.inventory.addItem(itemID, amount, -1);
							updateDelay(itemID, c.myShopId);
						}else
							return false;
					}else{
						c.sendMessage("You don't have enough space in your inventory.");
						return false;
					}
				}else{
					c.sendMessage("You don't have enough Zombie Points.");
					return false;
				}
			}else if(c.myShopId == 29 || c.myShopId == 30 || c.myShopId == 31){
				if(c.inventory.hasItem(6529, TotPrice2)){
					if(c.inventory.freeSlots() > 0){
						buyDelay = System.currentTimeMillis();
						if(removeItem(itemID, c.myShopId, amount)){
							c.inventory.deleteItem(6529, TotPrice2);
							c.inventory.addItem(itemID, amount, -1);
							updateDelay(itemID, c.myShopId);
						}else
							return false;
					}else{
						c.sendMessage("You don't have enough space in your inventory.");
						return false;
					}
				}else{
					c.sendMessage("You don't have enough tokkul.");
					return false;
				}
			}else if(c.myShopId != 11 && c.myShopId != 65 && c.myShopId != 16 && c.myShopId != 29 || c.myShopId != 30 || c.myShopId != 31 || c.myShopId != 47){
				if(c.inventory.hasItem(995, TotPrice2)){
					if(c.inventory.freeSlots() > 0){
						buyDelay = System.currentTimeMillis();
						if(removeItem(itemID, c.myShopId, amount)){
							c.inventory.deleteItem(995, TotPrice2);
							c.inventory.addItem(itemID, amount, -1);
							updateDelay(itemID, c.myShopId);
						}else
							return false;
					}else{
						c.sendMessage("You don't have enough space in your inventory.");
						return false;
					}
				}else{
					c.sendMessage("You don't have enough coins.");
					return false;
				}
			}
			c.inventory.resetItems(3823);
			resetShop(c.myShopId);
			updatePlayerShop();
			PlayerSave.saveGame(c);
			return true;
		}
		return false;
	}

	public void openDonorShop(){
		c.myShopId = 15;
		setupDonationShop();
	}

	public void openVoteShop(){
		c.myShopId = 19;
		setupVoteShop();
	}

	public void setupVoteShop(){
		synchronized(c){
			c.inventory.resetItems(3823);
			c.isShopping = true;
			c.myShopId = 19;
			c.getPA().sendFrame248(3824, 3822);
			c.getPA().sendText("Vote Shop", 3901);
			int TotalItems = ShopHandler.voteShopItems.size() > ShopHandler.MaxShopItems ? ShopHandler.MaxShopItems : ShopHandler.voteShopItems.size();
			c.getOutStream().createFrameVarSizeWord(53);
			c.getOutStream().writeWord(3900);
			c.getOutStream().writeWord(TotalItems);
			Iterator<Integer> iterator = ShopHandler.voteShopItems.keySet().iterator();
			int temp[] = new int[ShopHandler.voteShopItems.size()];
			int i = 0;
			while(iterator.hasNext())
				temp[i++] = iterator.next();
			Arrays.sort(temp);
			for(int key : temp){
				DonorShopItem item = ShopHandler.voteShopItems.get(key);
				c.getOutStream().writeByte(255);
				c.getOutStream().writeDWord_v2(item.packAmount);
				if(item.id < 0){
					c.getOutStream().writeWordBigEndianA(1);
					c.getOutStream().writeDWord(item.id);
					c.getOutStream().writeString(item.name);
				}else
					c.getOutStream().writeWordBigEndianA(item.id + 1);
			}
			c.getOutStream().endFrameVarSizeWord();
			c.flushOutStream();
		}
	}

	public void openSkillCape(){
		int count99 = get99Count();
		int max = count99 == skillCapes.length ? (count99 + 1) : (count99 > skillCapes.length + 1 ? (skillCapes.length + 1) : count99);
		int capes = count99 > 1 ? 1 : 0;
		c.myShopId = 14;
		if(c.Donator == 1)
			max++;
		if(Misc.currentTimeSeconds() - c.creationTime >= Config.YEAR && c.creationTime > 0)
			max++;
		setupSkillCapes(capes, max);
	}

	/*
	 * public int[][] skillCapes =
	 * {{0,9747,4319,2679},{1,2683,4329,2685},{2,2680
	 * ,4359,2682},{3,2701,4341,2703
	 * },{4,2686,4351,2688},{5,2689,4347,2691},{6,2692,4343,2691},
	 * {7,2737,4325,2733
	 * },{8,2734,4353,2736},{9,2716,4337,2718},{10,2728,4335,2730
	 * },{11,2695,4321,2697},{12,2713,4327,2715},{13,2725,4357,2727},
	 * {14,2722,4345
	 * ,2724},{15,2707,4339,2709},{16,2704,4317,2706},{17,2710,4361,
	 * 2712},{18,2719,4355,2721},{19,2737,4331,2739},{20,2698,4333,2700}};
	 */
	public int[] skillCapes = {9747, 9753, 9750, 9768, 9756, 9759, 9762, 9801, 9807, 9783, 9798, 9804, 9780, 9795, 9792, 9774, 9771, 9777, 9786, 9810, 9765};
	public int questCape = 9813;
	public int donorCape = 20100;

	public int get99Count(){
		int count = 0;
		for(int j = 0; j < c.playerLevel.length; j++){
			if(c.getLevelForXP(c.playerXP[j]) >= 99){
				count++;
			}
		}
		return count;
	}

	public void setupDonationShop(){
		synchronized(c){
			c.inventory.resetItems(3823);
			c.isShopping = true;
			c.myShopId = 15;
			c.getPA().sendFrame248(3824, 3822);
			c.getPA().sendText("Donation Shop", 3901);
			int TotalItems = ShopHandler.donorShopItems.size() > ShopHandler.MaxShopItems ? ShopHandler.MaxShopItems : ShopHandler.donorShopItems.size();
			c.getOutStream().createFrameVarSizeWord(53);
			c.getOutStream().writeWord(3900);
			c.getOutStream().writeWord(TotalItems);
			Iterator<Integer> iterator = ShopHandler.donorShopItems.keySet().iterator();
			int temp[] = new int[ShopHandler.donorShopItems.size()];
			int i = 0;
			while(iterator.hasNext())
				temp[i++] = iterator.next();
			Arrays.sort(temp);
			for(int key : temp){
				DonorShopItem item = ShopHandler.donorShopItems.get(key);
				c.getOutStream().writeByte(255);
				c.getOutStream().writeDWord_v2(item.id == 21773 ? 150 : item.id == -6 ? 60 : 500);
				if(item.id < 0){
					c.getOutStream().writeWordBigEndianA(1);
					c.getOutStream().writeDWord(item.id);
					c.getOutStream().writeString(item.name);
				}else
					c.getOutStream().writeWordBigEndianA(item.id + 1);
			}
			c.getOutStream().endFrameVarSizeWord();
			c.flushOutStream();
		}
	}

	public void setupSkillCapes(int capes, int capes2){
		synchronized(c){
			c.inventory.resetItems(3823);
			c.isShopping = true;
			c.myShopId = 14;
			c.getPA().sendFrame248(3824, 3822);
			c.getPA().sendText("Skillcape Shop", 3901);
			int maxCapes = skillCapes.length;
			int TotalItems = 0;
			TotalItems = capes2;
			if(TotalItems > ShopHandler.MaxShopItems){
				TotalItems = ShopHandler.MaxShopItems;
			}
			c.getOutStream().createFrameVarSizeWord(53);
			c.getOutStream().writeWord(3900);
			c.getOutStream().writeWord(TotalItems);
			for(int i = 0; i < maxCapes; i++){
				if(c.getLevelForXP(c.playerXP[i]) < 99)
					continue;
				c.getOutStream().writeByte(1);
				c.getOutStream().writeWordBigEndianA(skillCapes[i] + 2);
			}
			if(get99Count() >= maxCapes){
				c.getOutStream().writeByte(1);
				c.getOutStream().writeWordBigEndianA(questCape + 1);
			}
			if(c.isDonor() && c.Donator == 1){
				c.getOutStream().writeByte(1);
				c.getOutStream().writeWordBigEndianA(donorCape + 1);
			}
			if(Misc.currentTimeSeconds() - c.creationTime >= Config.YEAR && c.creationTime > 0){
				c.getOutStream().writeByte(1);
				c.getOutStream().writeWordBigEndianA(24710);
			}
			c.getOutStream().endFrameVarSizeWord();
			c.flushOutStream();
		}
	}

	public void skillBuy(int item, int amount){
		int count = get99Count();
		int nn = count > 1 ? 1 : 0;
		if(item == donorCape){
			if(c.inventory.freeSlots() >= amount){
				if(c.inventory.hasItem(995, 99000 * amount)){
					if(c.Donator == 1){
						c.inventory.deleteItem(995, c.inventory.findItemSlot(995), 99000 * amount);
						c.inventory.addItem(donorCape, amount, -1);
					}else{
						c.sendMessage("You must be a donor to buy this cape.");
					}
				}else{
					c.sendMessage("You don't have enough money to buy that many.");
				}
			}else{
				c.sendMessage("You don't have enough space for that.");
			}
		}else if(item == 24709){
			if(c.inventory.freeSlots() >= amount * 2){
				if(c.inventory.hasItem(995, 100000 * amount)){
					if(Misc.currentTimeSeconds() - c.creationTime >= Config.YEAR && c.creationTime > 0){
						c.inventory.deleteItem(995, c.inventory.findItemSlot(995), 100000 * amount);
						c.inventory.addItem(24709, amount, -1);
						c.inventory.addItem(24710, amount, -1);
					}else{
						c.sendMessage("You must be a veteran to buy this cape.");
					}
				}else{
					c.sendMessage("You don't have enough money to buy that many.");
				}
			}else{
				c.sendMessage("You don't have enough space for that.");
			}
		}else if(item == questCape){
			if(c.inventory.freeSlots() >= amount * 2){
				if(c.inventory.hasItem(995, 99000 * amount)){
					if(count >= skillCapes.length){
						c.inventory.deleteItem(995, c.inventory.findItemSlot(995), 99000 * amount);
						c.inventory.addItem(questCape, amount, -1);
						c.inventory.addItem(questCape + 1, amount, -1);
					}else{
						c.sendMessage("You must have every 99 to buy this cape.");
					}
				}else{
					c.sendMessage("You don't have enough money to buy that many.");
				}
			}else{
				c.sendMessage("You don't have enough space for that.");
			}
		}else{
			for(int j = 0; j < skillCapes.length; j++){
				if(skillCapes[j] == item || skillCapes[j] + 1 == item){
					if(c.inventory.freeSlots() >= amount * 2){
						if(c.inventory.hasItem(995, 99000 * amount)){
							if(c.getLevelForXP(c.playerXP[j]) >= 99){
								c.inventory.deleteItem(995, c.inventory.findItemSlot(995), 99000 * amount);
								c.inventory.addItem(skillCapes[j] + nn, amount, -1);
								c.inventory.addItem(skillCapes[j] + 2, amount, -1);
							}else{
								c.sendMessage("You must have 99 in the skill of the cape you're trying to buy.");
							}
						}else{
							c.sendMessage("You need 99k to buy this item.");
						}
					}else{
						c.sendMessage("You don't have enough space for that.");
					}
				}
				/*
				 * if (skillCapes[j][1 + nn] == item) { if
				 * (c.inventory.freeSlots() >= 1) { if
				 * (c.inventory.hasItem(995,99000)) { if
				 * (c.getLevelForXP(c.playerXP[j]) >= 99) {
				 * c.inventory.deleteItem(995, c.getItems().getItemSlot(995),
				 * 99000); c.inventory.addItem(skillCapes[j] + nn,1);
				 * c.inventory.addItem(skillCapes[j] + 2,1); } else {
				 * c.sendMessage(
				 * "You must have 99 in the skill of the cape you're trying to buy."
				 * ); } } else {
				 * c.sendMessage("You need 99k to buy this item."); } } else {
				 * c.sendMessage(
				 * "You must have at least 1 inventory spaces to buy this item."
				 * ); } break; }
				 */
			}
		}
		c.saveGame();
		c.inventory.resetItems(3823);
	}

	public void openVoid(){
		synchronized(c){
			c.inventory.resetItems(3823);
			c.isShopping = true;
			c.myShopId = 15;
			c.getPA().sendFrame248(3824, 3822);
			c.getPA().sendText("Void Recovery", 3901);

			int TotalItems = 5;
			c.getOutStream().createFrameVarSizeWord(53);
			c.getOutStream().writeWord(3900);
			c.getOutStream().writeWord(TotalItems);
			for(int i = 0; i < c.voidStatus.length; i++){
				c.getOutStream().writeByte(c.voidStatus[i]);
				c.getOutStream().writeWordBigEndianA(2519 + i * 2);
			}
			c.getOutStream().endFrameVarSizeWord();
			c.flushOutStream();
		}
	}

	public void buyVoid(int item){}
}
