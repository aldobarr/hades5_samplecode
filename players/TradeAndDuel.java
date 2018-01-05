package server.model.players;

import java.util.concurrent.CopyOnWriteArrayList;

import server.Config;
import server.model.HadesThread;
import server.model.items.GameItem;
import server.model.items.Item;
import server.model.items.MarketItem;
import server.util.Misc;
import server.world.Market;

public class TradeAndDuel{
	private Client c;

	public TradeAndDuel(Client Client){
		this.c = Client;
	}

	/**
	 * Trading
	 **/

	public CopyOnWriteArrayList<GameItem> offeredItems = new CopyOnWriteArrayList<GameItem>();

	public void requestTrade(int id){
		try{
			Client o = (Client)PlayerHandler.players[id];
			if(o == null)
				return;
			if(o.inZombiesGame || c.inZombiesGame || c.inHowlOfDeath || o.inHowlOfDeath)
				return;
			if(o.playerRights == 2){
				c.sendMessage("Admins can't trade.");
				return;
			}
			if(c.inTrade){
				Client temp = (Client)PlayerHandler.players[c.tradeWith];
				if(temp != null)
					temp.getTradeAndDuel().declineTrade(true);
			}
			if(c.isLoggingOut || o.isLoggingOut)
				return;
			if(!c.goodDistance(o.absX, o.absY, c.absX, c.absY, 15)){
				c.sendMessage("Unable to find " + o.playerName);
				return;
			}
			if(o.isBanking || o.isShopping || o.inTrade){
				c.sendMessage("This player is currently busy.");
				if(o.inTrade && o.tradeWith == c.playerId)
					o.getTradeAndDuel().declineTrade(true);
				return;
			}
			if((c.inCwGame && !o.inCwGame) || (!c.inCwGame && o.inCwGame))
				return;
			if(o.getPA().ignoreContains(Misc.playerNameToInt64(c.playerName))){
				c.sendMessage("Sending trade request...");
				return;
			}
			if(id == c.playerId)
				return;
			c.tradeWith = id;
			if(!c.inTrade && o.tradeRequested && o.tradeWith == c.playerId){
				c.getTradeAndDuel().openTrade();
				o.getTradeAndDuel().openTrade();
			}else if(!c.inTrade){
				c.tradeRequested = true;
				c.sendMessage("Sending trade request...");
				o.sendMessage(c.playerName + ":tradereq:");
			}
		}catch(Exception e){
			System.out.println("Error requesting trade.");
		}
	}

	public void openTrade(){
		Client o = (Client)PlayerHandler.players[c.tradeWith];
		if(o == null)
			return;
		if(c.isBanking || o.isBanking){
			c.getPA().closeTrade();
			o.getPA().closeTrade();
			return;
		}
		c.inTrade = true;
		c.canOffer = true;
		c.tradeStatus = 1;
		c.tradeRequested = false;
		c.inventory.resetItems(3322);
		resetTItems(3415);
		resetOTItems(3416);
		c.getPA().sendText("Trading with: " + o.playerName + " who has @gre@" + o.inventory.freeSlots() + " free slots", 3417);
		c.getPA().sendText("", 3431);
		c.getPA().sendText("Are you sure you want to make this trade?", 3535);
		c.getPA().sendFrame248(3323, 3321);
	}

	public void resetTItems(int WriteFrame){
		synchronized(c){
			c.getOutStream().createFrameVarSizeWord(53);
			c.getOutStream().writeWord(WriteFrame);
			int len = offeredItems.toArray().length;
			int current = 0;
			c.getOutStream().writeWord(len);
			for(GameItem item : offeredItems){
				if(item.amount > 254){
					c.getOutStream().writeByte(255);
					c.getOutStream().writeDWord_v2(item.amount);
				}else{
					c.getOutStream().writeByte(item.amount);
				}
				c.getOutStream().writeWordBigEndianA(item.id + 1);
				current++;
			}
			if(current < 27){
				for(int i = current; i < 28; i++){
					c.getOutStream().writeByte(1);
					c.getOutStream().writeWordBigEndianA(-1);
				}
			}
			c.getOutStream().endFrameVarSizeWord();
			c.flushOutStream();
		}
	}

	public boolean fromTrade(int itemID, int fromSlot, int amount){
		Client o = (Client)PlayerHandler.players[c.tradeWith];
		if(o == null || amount <= 0)
			return false;
		try{
			if(!c.inTrade || !c.canOffer){
				declineTrade(true);
				return false;
			}
			c.tradeConfirmed = false;
			o.tradeConfirmed = false;
			for(GameItem item : offeredItems){
				if(item.id == itemID){
					if(item.amount <= 0)
						continue;
					c.inventory.addItem(itemID, amount > item.amount ? item.amount : amount, -1);
					int temp = amount;
					amount -= item.amount > amount ? amount : item.amount;
					if(temp < item.amount)
						item.amount -= temp;
					else
						offeredItems.remove(item);
					o.getPA().sendText("Trading with: " + c.playerName + " who has @gre@" + c.inventory.freeSlots() + " free slots", 3417);
				}
				if(amount <= 0)
					break;
			}
			o.getPA().sendText("Trading with: " + c.playerName + " who has @gre@" + c.inventory.freeSlots() + " free slots", 3417);
			c.tradeConfirmed = false;
			o.tradeConfirmed = false;
			c.inventory.resetItems(3322);
			resetTItems(3415);
			o.getTradeAndDuel().resetOTItems(3416);
			c.getPA().sendText("", 3431);
			o.getPA().sendText("", 3431);
		}catch(Exception e){
		}
		return true;
	}

	public boolean tradeItem(int itemID, int fromSlot, int amount){
		Client o = (Client)PlayerHandler.players[c.tradeWith];
		if(o == null || itemID == Config.EASTER_RING)
			return false;
		for(int i : Config.ITEM_TRADEABLE){
			if(i == itemID || c.isSkillCapeItem(itemID)){
				c.sendMessage("You can't trade this item.");
				return false;
			}
		}
		c.tradeConfirmed = false;
		o.tradeConfirmed = false;
		if(amount <= 0)
			return false;
		if(c.inventory.getItemCount(itemID) < amount){
			amount = c.inventory.getItemCount(itemID);
			if(amount <= 0)
				return false;
		}
		if(!c.inTrade || !c.canOffer){
			declineTrade(true);
			return false;
		}
		boolean inTrade = false;
		for(GameItem item : offeredItems){
			if(item.id == itemID && Item.itemStackable[itemID]){
				inTrade = true;
				item.amount += amount;
				c.inventory.deleteItem(itemID, amount);
				o.getPA().sendText("Trading with: " + c.playerName + " who has @gre@" + c.inventory.freeSlots() + " free slots", 3417);
				break;
			}
		}
		if(!inTrade){
			c.inventory.deleteItem(itemID, amount);
			if(Item.itemStackable[itemID])
				offeredItems.add(new GameItem(itemID, amount));
			else
				for(int i = 0; i < amount; i++)
					offeredItems.add(new GameItem(itemID, 1));
			o.getPA().sendText("Trading with: " + c.playerName + " who has @gre@" + c.inventory.freeSlots() + " free slots", 3417);
		}
		o.getPA().sendText("Trading with: " + c.playerName + " who has @gre@" + c.inventory.freeSlots() + " free slots", 3417);
		c.inventory.resetItems(3322);
		resetTItems(3415);
		o.getTradeAndDuel().resetOTItems(3416);
		c.getPA().sendText("", 3431);
		o.getPA().sendText("", 3431);
		return true;
	}

	public void resetTrade(){
		offeredItems.clear();
		c.inTrade = false;
		c.tradeWith = 0;
		c.canOffer = true;
		c.tradeConfirmed = false;
		c.tradeConfirmed2 = false;
		c.acceptedTrade = false;
		c.getPA().closeTrade();
		c.tradeResetNeeded = false;
		c.getPA().sendText("Are you sure you want to make this trade?", 3535);
	}

	public void declineTrade(boolean tellOther){
		c.tradeStatus = 0;
		c.getPA().closeTrade();
		Client o = (Client)PlayerHandler.players[c.tradeWith];
		if(o == null){
			return;
		}

		if(tellOther){
			o.getTradeAndDuel().declineTrade(false);
			o.getPA().closeTrade();
		}

		for(GameItem item : offeredItems){
			if(item.amount < 1){
				continue;
			}
			if(item.stackable){
				c.inventory.addItem(item.id, item.amount, -1);
			}else{
				for(int i = 0; i < item.amount; i++){
					c.inventory.addItem(item.id, 1, -1);
				}
			}
		}
		c.canOffer = true;
		c.tradeConfirmed = false;
		c.tradeConfirmed2 = false;
		offeredItems.clear();
		c.inTrade = false;
		c.tradeWith = 0;
	}

	public void resetOTItems(int WriteFrame){
		synchronized(c){
			Client o = (Client)PlayerHandler.players[c.tradeWith];
			if(o == null){
				return;
			}
			c.getOutStream().createFrameVarSizeWord(53);
			c.getOutStream().writeWord(WriteFrame);
			int len = o.getTradeAndDuel().offeredItems.toArray().length;
			int current = 0;
			c.getOutStream().writeWord(len);
			for(GameItem item : o.getTradeAndDuel().offeredItems){
				if(item.amount > 254){
					c.getOutStream().writeByte(255); // item's stack count. if
														// over 254, write byte
														// 255
					c.getOutStream().writeDWord_v2(item.amount);
				}else{
					c.getOutStream().writeByte(item.amount);
				}
				c.getOutStream().writeWordBigEndianA(item.id + 1); // item id
				current++;
			}
			if(current < 27){
				for(int i = current; i < 28; i++){
					c.getOutStream().writeByte(1);
					c.getOutStream().writeWordBigEndianA(-1);
				}
			}
			c.getOutStream().endFrameVarSizeWord();
			c.flushOutStream();
		}
	}

	public void confirmScreen(){
		Client o = (Client)PlayerHandler.players[c.tradeWith];
		if(o == null){
			return;
		}
		c.canOffer = false;
		c.inventory.resetItems(3214);
		String SendTrade = "Absolutely nothing!";
		String SendAmount = "";
		int Count = 0;
		for(GameItem item : offeredItems){
			if(item.id > 0){
				if(item.amount >= 1000 && item.amount < 1000000){
					SendAmount = "@cya@" + (item.amount / 1000) + "K @whi@(" + Misc.format(item.amount) + ")";
				}else if(item.amount >= 1000000){
					SendAmount = "@gre@" + (item.amount / 1000000) + " million @whi@(" + Misc.format(item.amount) + ")";
				}else{
					SendAmount = "" + Misc.format(item.amount);
				}
				if(Count == 0){
					SendTrade = c.getItems().getItemName(item.id);
				}else{
					SendTrade = SendTrade + "\\n" + c.getItems().getItemName(item.id);
				}
				if(item.stackable){
					SendTrade = SendTrade + " x " + SendAmount;
				}
				Count++;
			}
		}

		c.getPA().sendText(SendTrade, 3557);
		SendTrade = "Absolutely nothing!";
		SendAmount = "";
		Count = 0;

		for(GameItem item : o.getTradeAndDuel().offeredItems){
			if(item.id > 0){
				if(item.amount >= 1000 && item.amount < 1000000){
					SendAmount = "@cya@" + (item.amount / 1000) + "K @whi@(" + Misc.format(item.amount) + ")";
				}else if(item.amount >= 1000000){
					SendAmount = "@gre@" + (item.amount / 1000000) + " million @whi@(" + Misc.format(item.amount) + ")";
				}else{
					SendAmount = "" + Misc.format(item.amount);
				}
				if(Count == 0){
					SendTrade = c.getItems().getItemName(item.id);
				}else{
					SendTrade = SendTrade + "\\n" + c.getItems().getItemName(item.id);
				}
				if(item.stackable){
					SendTrade = SendTrade + " x " + SendAmount;
				}
				Count++;
			}
		}
		c.getPA().sendText(SendTrade, 3558);
		c.getPA().sendFrame248(3443, 197);
	}
	
	private void updateMarket(Client o, boolean tryAgain){
		if(offeredItems.size() == 0 || o.getTradeAndDuel().offeredItems.size() == 0)
			return;
		if(!Config.USE_MARKET_DATA)
			return;
		GameItem item = offeredItems.get(0);
		if(item.id == 995 || item.id == Config.BANK_CERTIFICATE){
			long value = ((long)item.amount * ((long)(item.id == Config.BANK_CERTIFICATE ? 2000000000 : 1)));
			int id = 0, add = 0;
			long amount = 0;
			for(int i = 1; i<offeredItems.size(); i++){
				item = offeredItems.get(i);
				if(item.id != 995 && item.id != Config.BANK_CERTIFICATE)
					return;
				value += ((long)item.amount * ((long)(item.id == Config.BANK_CERTIFICATE ? 2000000000 : 1)));
			}
			item = o.getTradeAndDuel().offeredItems.get(0);
			if(item.id == 995 || item.id == Config.BANK_CERTIFICATE)
				return;
			id = item.id;
			if(Item.itemStackable[id]){
				if(Item.itemIsNote[id])
					add = -1;
				else
					add = 0;
			}else{
				if(Item.itemIsNote[id + 1])
					add = 1;
				else
					add = 0;
			}
			amount = item.amount;
			for(int i = 1; i<o.getTradeAndDuel().offeredItems.size(); i++){
				item = o.getTradeAndDuel().offeredItems.get(i);
				if(item.id == 995 || item.id == Config.BANK_CERTIFICATE)
					return;
				if(item.id != id && item.id + add == id)
					return;
				amount += item.amount;
			}
			id = Item.itemIsNote[id] ? id - 1 : id;
			MarketItem marketItem = Market.getMarketItem(id);
			boolean newItem = true;
			long oldVal = 0;
			if(marketItem != null){
				newItem = false;
				oldVal = marketItem.value;
			}
			value /= amount;
			Market.updateItem(id, value);
			long change = newItem ? value : Market.getMarketItem(id).value - oldVal;
			new HadesThread(HadesThread.UPDATE_MARKET_VALUE, id);
			new HadesThread(HadesThread.UPDATE_MARKET_CHANGE, id, change);
		}else if(tryAgain)
			o.getTradeAndDuel().updateMarket(c, false);
	}

	public void giveItems(){
		long p1Val = 0, p2Val = 0;
		String p1Items = "{", p2Items = "{";
		Client o = (Client)PlayerHandler.players[c.tradeWith];
		if(o == null)
			return;
		try{
			updateMarket(o, true);
			for(GameItem item : o.getTradeAndDuel().offeredItems){
				if(item.id > 0){
					c.inventory.addItem(item.id, item.amount, -1);
					p1Items += "{" + item.id + "," + item.amount + "},";
					long value = Market.itemHasData(item.id) ? Market.getMarketItem(item.id).value : ((long)c.getShops().getItemShopValue(item.id));
					p1Val += (long)((value) * ((long)item.amount));
				}
			}
			o.getTradeAndDuel().offeredItems.clear();
			for(GameItem item : c.getTradeAndDuel().offeredItems){
				if(item.id > 0){
					o.inventory.addItem(item.id, item.amount, -1);
					p2Items += "{" + item.id + "," + item.amount + "},";
					long value = Market.itemHasData(item.id) ? Market.getMarketItem(item.id).value : ((long)c.getShops().getItemShopValue(item.id));
					p2Val += (long)((value) * ((long)item.amount));
				}
			}
			p1Items = p1Items.substring(0, p1Items.length() - 1).replace(",", ", ");
			p2Items = p2Items.substring(0, p2Items.length() - 1).replace(",", ", ");
			if(p1Items.length() > 0)
				p1Items += "}";
			if(p2Items.length() > 0)
				p2Items += "}";
			c.getTradeAndDuel().offeredItems.clear();
			c.getPA().closeTrade();
			c.tradeResetNeeded = true;
			o.getPA().closeTrade();
			o.tradeResetNeeded = true;
			long wealthTransfer = p1Val > p2Val ? p1Val - p2Val : p2Val - p1Val;
			if(wealthTransfer >= 1000000000)
				new HadesThread(HadesThread.REPORT, (p1Val > p2Val ? o : c), (p1Val > p2Val ? o.playerName : c.playerName) + " to " + (p1Val > p2Val ? c.playerName : o.playerName) + " Wealth Transfer - " + Misc.numberFormat(wealthTransfer) + "<br /><br />" + o.playerName + " items - " + p1Items + "<br /><br />" + c.playerName + " items - " + p2Items, 9, true);
		}catch(Exception e){
			e.printStackTrace();
		}
		c.saveGame();
		o.saveGame();
	}

	/**
	 * Dueling
	 **/

	/*public void requestDuel(int id){
		try{
			if(id == c.playerId)
				return;
			Client o = (Client)PlayerHandler.players[id];
			if(o == null)
				return;
			if(o.inZombiesGame || c.inZombiesGame || c.isJailed || c.duelStatus != 0 || c.inHowlOfDeath || o.inHowlOfDeath)
				return;
			if(o.overLoad > 0){
				c.sendMessage("That player is busy right now.");
				return;
			}
			if(c.overLoad > 0){
				c.sendMessage("You can't do that while you're overloading.");
				return;
			}
			if(!c.goodDistance(o.absX, o.absY, c.absX, c.absY, 15) || !o.inDuelArena() || !c.inDuelArena()){
				c.sendMessage("Unable to find " + o.playerName);
				return;
			}
			if(c.duelStatus > 0 || o.duelStatus > 0 || o == null){
				c.sendMessage("The other player is currently busy.");
				if(c.duelStatus > 0 && c.duelStatus < 5)
					declineDuel(o != null && o.duelingWith == c.playerId ? true : false);
				return;
			}
			declineDuel(false);
			resetDuelItems();
			resetDuel();
			if(o.getPA().ignoreContains(Misc.playerNameToInt64(c.playerName))){
				c.sendMessage("Sending duel request...");
				return;
			}
			c.duelingWith = id;
			c.duelRequested = true;
			if(c.duelStatus == 0 && o.duelStatus == 0 && c.duelRequested && o.duelRequested && c.duelingWith == o.getId() && o.duelingWith == c.getId()){
				if(c.goodDistance(c.getX(), c.getY(), o.getX(), o.getY(), 1)){
					c.getTradeAndDuel().openDuel();
					o.getTradeAndDuel().openDuel();
					DuelRules rules = new DuelRules(c.playerId, o.playerId);
					c.rules = rules;
					o.rules = rules;
				}else{
					c.sendMessage("You need to get closer to your opponent to start the duel.");
				}

			}else{
				c.sendMessage("Sending duel request...");
				o.sendMessage(c.playerName + ":duelreq:");
			}
		}catch(Exception e){
			System.out.println("Error requesting duel.");
		}
	}

	public void openDuel(){
		Client o = (Client)PlayerHandler.players[c.duelingWith];
		if(o == null)
			return;
		resetDuelItems();
		c.duelStatus = 1;
		refreshduelRules();
		refreshDuelScreen();
		c.canOffer = true;
		for(int i = 0; i < c.playerEquipment.length; i++){
			sendDuelEquipment(c.playerEquipment[i], c.playerEquipmentN[i], i);
		}
		c.getPA().sendText("Dueling with: " + o.playerName + " (level-" + o.combatLevel + ")", 6671);
		c.getPA().sendText("", 6684);
		c.getPA().sendFrame248(6575, 3321);
		c.inventory.resetItems(3322);
	}

	public void sendDuelEquipment(int itemId, int amount, int slot){
		synchronized(c){
			if(itemId != 0){
				c.getOutStream().createFrameVarSizeWord(34);
				c.getOutStream().writeWord(13824);
				c.getOutStream().writeByte(slot);
				c.getOutStream().writeWord(itemId + 1);

				if(amount > 254){
					c.getOutStream().writeByte(255);
					c.getOutStream().writeDWord(amount);
				}else{
					c.getOutStream().writeByte(amount);
				}
				c.getOutStream().endFrameVarSizeWord();
				c.flushOutStream();
			}
		}
	}

	public void refreshduelRules(){
		c.rules = null;
		c.getPA().sendFrame87(286, 0);
		c.duelOption = 0;
	}

	public void refreshDuelScreen(){
		synchronized(c){
			Client o = (Client)PlayerHandler.players[c.duelingWith];
			if(o == null){
				return;
			}
			c.getOutStream().createFrameVarSizeWord(53);
			c.getOutStream().writeWord(6669);
			c.getOutStream().writeWord(c.stakedItems.toArray().length);
			int current = 0;
			for(GameItem item : c.stakedItems){
				if(item.amount > 254){
					c.getOutStream().writeByte(255);
					c.getOutStream().writeDWord_v2(item.amount);
				}else{
					c.getOutStream().writeByte(item.amount);
				}
				if(item.id > Config.ITEM_LIMIT || item.id < 0){
					item.id = Config.ITEM_LIMIT;
				}
				c.getOutStream().writeWordBigEndianA(item.id + 1);

				current++;
			}

			if(current < 27){
				for(int i = current; i < 28; i++){
					c.getOutStream().writeByte(1);
					c.getOutStream().writeWordBigEndianA(-1);
				}
			}
			c.getOutStream().endFrameVarSizeWord();
			c.flushOutStream();

			c.getOutStream().createFrameVarSizeWord(53);
			c.getOutStream().writeWord(6670);
			c.getOutStream().writeWord(o.stakedItems.toArray().length);
			current = 0;
			for(GameItem item : o.stakedItems){
				if(item.amount > 254){
					c.getOutStream().writeByte(255);
					c.getOutStream().writeDWord_v2(item.amount);
				}else{
					c.getOutStream().writeByte(item.amount);
				}
				if(item.id > Config.ITEM_LIMIT || item.id < 0){
					item.id = Config.ITEM_LIMIT;
				}
				c.getOutStream().writeWordBigEndianA(item.id + 1);
				current++;
			}

			if(current < 27){
				for(int i = current; i < 28; i++){
					c.getOutStream().writeByte(1);
					c.getOutStream().writeWordBigEndianA(-1);
				}
			}
			c.getOutStream().endFrameVarSizeWord();
			c.flushOutStream();
		}
	}

	public boolean stakeItem(int itemID, int fromSlot, int amount){
		if(itemID == Config.EASTER_RING)
			return false;
		for(int i : Config.ITEM_TRADEABLE){
			if(i == itemID){
				c.sendMessage("You can't stake this item.");
				return false;
			}
			if(c.playerRights == 2 && !Config.ADMIN_CAN_TRADE){
				c.sendMessage("You can't stake as admin");
				return false;
			}
		}
		if(amount <= 0)
			return false;
		Client o = (Client)PlayerHandler.players[c.duelingWith];
		if(o == null){
			declineDuel(false);
			return false;
		}
		if(o.duelStatus <= 0 || c.duelStatus <= 0){
			declineDuel(true);
			return false;
		}
		if(!c.canOffer){
			return false;
		}
		changeDuelStuff();
		if(!c.inventory.hasItem(itemID, amount))
			return false;
		boolean found = false;
		for(GameItem item : c.stakedItems){
			if(item.id == itemID && Item.itemStackable[itemID]){
				found = true;
				item.amount += amount;
				c.inventory.deleteItem(itemID, amount);
			}
		}
		if(!found){
			c.inventory.deleteItem(itemID, amount);
			if(Item.itemStackable[itemID])
				c.stakedItems.add(new GameItem(itemID, amount));
			else
				for(int i = 0; i < amount; i++)
					c.stakedItems.add(new GameItem(itemID, 1));
		}
		found = false;
		synchronized(o){
			o.otherStakedItems.clear();
			for(GameItem item : c.stakedItems)
				o.otherStakedItems.add(item);
		}
		c.inventory.resetItems(3214);
		c.inventory.resetItems(3322);
		o.inventory.resetItems(3214);
		o.inventory.resetItems(3322);
		refreshDuelScreen();
		o.getTradeAndDuel().refreshDuelScreen();
		c.getPA().sendText("", 6684);
		o.getPA().sendText("", 6684);
		return true;
	}

	public boolean fromDuel(int itemID, int fromSlot, int amount){
		Client o = (Client)PlayerHandler.players[c.duelingWith];
		if(o == null){
			declineDuel(false);
			return false;
		}
		if(o.duelStatus <= 0 || c.duelStatus <= 0){
			declineDuel(true);
			return false;
		}
		if(Item.itemStackable[itemID]){
			if(c.inventory.freeSlots() - 1 < (c.duelSpaceReq)){
				c.sendMessage("You have too many rules set to remove that item.");
				return false;
			}
		}
		if(!c.canOffer){
			return false;
		}
		changeDuelStuff();
		synchronized(c){
			for(GameItem item : c.stakedItems){
				if(item.id == itemID){
					if(item.amount <= 0)
						continue;
					int amount3 = Item.itemStackable[item.id] ? 1 : amount > item.amount ? item.amount : amount;
					if(!checkDuelSpace(amount3)){
						c.sendMessage("You do not have the required space to remove that staked item.");
						continue;
					}
					c.inventory.addItem(itemID, amount > item.amount ? item.amount : amount, -1);
					int temp = amount;
					amount -= item.amount > amount ? amount : item.amount;
					if(temp < item.amount)
						item.amount -= temp;
					else
						c.stakedItems.remove(item);
				}
				if(amount <= 0)
					break;
			}
		}
		synchronized(o){
			o.otherStakedItems.clear();
			for(GameItem item : c.stakedItems)
				o.otherStakedItems.add(item);
		}
		o.duelStatus = 1;
		c.duelStatus = 1;
		c.inventory.resetItems(3214);
		c.inventory.resetItems(3322);
		o.inventory.resetItems(3214);
		o.inventory.resetItems(3322);
		c.getTradeAndDuel().refreshDuelScreen();
		o.getTradeAndDuel().refreshDuelScreen();
		o.getPA().sendText("", 6684);
		return true;
	}

	public void confirmDuel(){
		Client o = (Client)PlayerHandler.players[c.duelingWith];
		if(o == null || o.rules == null || c.rules == null){
			declineDuel(false);
			return;
		}
		String itemId = "";
		for(GameItem item : c.stakedItems){
			if(Item.itemStackable[item.id] || Item.itemIsNote[item.id]){
				itemId += c.getItems().getItemName(item.id) + " x " + Misc.format(item.amount) + "\\n";
			}else{
				itemId += c.getItems().getItemName(item.id) + "\\n";
			}
		}
		c.getPA().sendText(itemId, 6516);
		itemId = "";
		for(GameItem item : o.stakedItems){
			if(Item.itemStackable[item.id] || Item.itemIsNote[item.id]){
				itemId += c.getItems().getItemName(item.id) + " x " + Misc.format(item.amount) + "\\n";
			}else{
				itemId += c.getItems().getItemName(item.id) + "\\n";
			}
		}
		c.getPA().sendText(itemId, 6517);
		c.getPA().sendText("", 8242);
		for(int i = 8238; i <= 8253; i++){
			c.getPA().sendText("", i);
		}
		c.getPA().sendText("Hitpoints will be restored.", 8250);
		c.getPA().sendText("Boosted stats will be restored.", 8238);
		if(c.rules.duelRule[8])
			c.getPA().sendText("There will be obstacles in the arena.", 8239);
		c.getPA().sendText("", 8240);
		c.getPA().sendText("", 8241);

		String rulesOption[] = {"Players cannot forfeit.", "Players cannot move.", "Players cannot use range.", "Players cannot use melee.", "Players cannot use magic.", "Players cannot drink pots.", "Players cannot eat food.", "Players cannot use prayer.", "Players can only use fun weapons.", "Players cannot use special attacks."};
		int rules[][] = {{0, 0}, {1, 1}, {2, 2}, {3, 3}, {4, 4}, {5, 5}, {6, 6}, {7, 7}, {9, 8}, {10, 9}};
		int lineNumber = 8242;
		for(int i = 0; i < rules.length; i++)
			if(c.rules.duelRule[rules[i][0]])
				c.getPA().sendText("" + rulesOption[rules[i][1]], lineNumber++);
		c.getPA().sendText("", 6571);
		c.getPA().sendFrame248(6412, 197);
		// c.getPA().showInterface(6412);
	}

	public void startDuel(){
		synchronized(c){
			Client o = (Client)PlayerHandler.players[c.duelingWith];
			if(o == null || o.rules == null || c.rules == null){
				declineDuel(false);
				return;
			}
			c.headIconHints = 2;
			c.beginDueling = true;
			c.duelCount = 3;
			if(c.rules.duelRule[7]){
				for(int p = 0; p < c.PRAYER.length; p++){ // reset prayer glows
					c.prayerActive[p] = false;
					c.getPA().sendConfig(c.PRAYER_GLOW[p], 0);
				}
				c.headIcon = -1;
				c.getPA().requestUpdates();
			}
			if(c.rules.duelRule[11])
				c.getItems().removeItem(c.playerEquipment[0], 0);
			if(c.rules.duelRule[12])
				c.getItems().removeItem(c.playerEquipment[1], 1);
			if(c.rules.duelRule[13])
				c.getItems().removeItem(c.playerEquipment[2], 2);
			if(c.rules.duelRule[14])
				c.getItems().removeItem(c.playerEquipment[3], 3);
			if(c.rules.duelRule[15])
				c.getItems().removeItem(c.playerEquipment[4], 4);
			if(c.rules.duelRule[16]){
				if(c.getItems().is2handed(c.getItems().getItemName(c.playerEquipment[3]).toLowerCase(), c.playerEquipment[3]))
					c.getItems().removeItem(c.playerEquipment[3], 3);
				c.getItems().removeItem(c.playerEquipment[5], 5);
			}
			if(c.rules.duelRule[17])
				c.getItems().removeItem(c.playerEquipment[7], 7);
			if(c.rules.duelRule[18])
				c.getItems().removeItem(c.playerEquipment[9], 9);
			if(c.rules.duelRule[19])
				c.getItems().removeItem(c.playerEquipment[10], 10);
			if(c.rules.duelRule[20])
				c.getItems().removeItem(c.playerEquipment[12], 12);
			if(c.rules.duelRule[21])
				c.getItems().removeItem(c.playerEquipment[13], 13);
			c.inDuel = true;
			c.getPA().closeTrade();
			c.vengOn = false;
			c.specAmount = 10;
			c.getItems().addSpecialBar(c.playerEquipment[c.playerWeapon]);
			c.solTime = -1;
			c.solSpec = false;
			if(c.rules.duelRule[1])
				c.getPA().movePlayer(c.duelTeleX, c.duelTeleY, 0);
			else{
				int x = 0, y = 0, pos = 0;
				if(c.duelArena == -1){
					if(c.rules.duelRule[8]){
						do{
							pos = Misc.random(ARENA_COORDS[1].length - 1);
							x = ARENA_COORDS[1][pos][0] + (Misc.random(6) * (Misc.random(99) >= 49 ? 1 : -1));
							y = ARENA_COORDS[1][pos][1] + (Misc.random(3) * (Misc.random(99) >= 49 ? 1 : -1));
						}while(!server.clip.region.Region.canMove(x + 1, y + 1, x, y, c.heightLevel, 1, 1));
					}else{
						pos = Misc.random(ARENA_COORDS[0].length - 1);
						x = ARENA_COORDS[0][pos][0] + (Misc.random(6) * (Misc.random(99) >= 49 ? 1 : -1));
						y = ARENA_COORDS[0][pos][1] + (Misc.random(3) * (Misc.random(99) >= 49 ? 1 : -1));
					}
					c.duelArena = o.duelArena = pos;
				}else{
					pos = c.duelArena;
					c.duelArena = o.duelArena = -1;
					if(c.rules.duelRule[8]){
						do{
							x = ARENA_COORDS[1][pos][0] + (Misc.random(6) * (Misc.random(99) >= 49 ? 1 : -1));
							y = ARENA_COORDS[1][pos][1] + (Misc.random(3) * (Misc.random(99) >= 49 ? 1 : -1));
						}while(!server.clip.region.Region.canMove(x + 1, y + 1, x, y, c.heightLevel, 1, 1));
					}else{
						x = ARENA_COORDS[0][pos][0] + (Misc.random(6) * (Misc.random(99) >= 49 ? 1 : -1));
						y = ARENA_COORDS[0][pos][1] + (Misc.random(3) * (Misc.random(99) >= 49 ? 1 : -1));
					}
				}
				c.getPA().movePlayer(x, y, 0);
			}
			c.getPA().createPlayerHints(10, o.playerId);
			c.getPA().showOption(3, 0, "Attack", 1);
			for(int i = 0; i < 20; i++){
				c.playerLevel[i] = c.getPA().getLevelForXP(c.playerXP[i]);
				c.getPA().refreshSkill(i);
			}
			c.getPA().requestUpdates();
			c.poisonDamage = -1;
			c.poisonImmune = 0;
			c.overloaded = new int[][]{{0, 0}, {1, 0}, {2, 0}, {4, 0}, {6, 0}};
			c.overloadedBool = false;
			c.duelStatus = 5;
			final int other = c.duelingWith;
			Server.scheduler.schedule(new Task(2){
				protected void execute(){
					if(c == null){
						Client p2 = (Client)PlayerHandler.players[other];
						if(p2 != null){
							synchronized(p2){
								p2.getTradeAndDuel().duelVictory();
								p2.Rating++;
							}
						}
						stop();
						return;
					}
					synchronized(c){
						if(c.duelCount == 0){
							c.damageTaken = new int[Config.MAX_PLAYERS];
							c.forcedChat("FIGHT!");
							c.duelCount--;
							stop();
						}else
							c.forcedChat("" + c.duelCount--);
					}
				}
			});
		}
	}
	
	public void resetBrokenDuel(Client o){
		for(int i = 0; i<c.rules.duelRule.length; i++)
			c.rules.duelRule[i] = o.rules.duelRule[i];
		c.duelingWith = o.playerId;
		c.headIconHints = 2;
		if(c.rules.duelRule[7]){
			for(int p = 0; p < c.PRAYER.length; p++){ // reset prayer glows
				c.prayerActive[p] = false;
				c.getPA().sendConfig(c.PRAYER_GLOW[p], 0);
			}
			c.headIcon = -1;
			c.getPA().requestUpdates();
		}
		if(c.rules.duelRule[11]){
			c.getItems().removeItem(c.playerEquipment[0], 0);
		}
		if(c.rules.duelRule[12]){
			c.getItems().removeItem(c.playerEquipment[1], 1);
		}
		if(c.rules.duelRule[13]){
			c.getItems().removeItem(c.playerEquipment[2], 2);
		}
		if(c.rules.duelRule[14]){
			c.getItems().removeItem(c.playerEquipment[3], 3);
		}
		if(c.rules.duelRule[15]){
			c.getItems().removeItem(c.playerEquipment[4], 4);
		}
		if(c.rules.duelRule[16]){
			if(c.getItems().is2handed(c.getItems().getItemName(c.playerEquipment[3]).toLowerCase(), c.playerEquipment[3]))
				c.getItems().removeItem(c.playerEquipment[3], 3);
			c.getItems().removeItem(c.playerEquipment[5], 5);
		}
		if(c.rules.duelRule[17]){
			c.getItems().removeItem(c.playerEquipment[7], 7);
		}
		if(c.rules.duelRule[18]){
			c.getItems().removeItem(c.playerEquipment[9], 9);
		}
		if(c.rules.duelRule[19]){
			c.getItems().removeItem(c.playerEquipment[10], 10);
		}
		if(c.rules.duelRule[20]){
			c.getItems().removeItem(c.playerEquipment[12], 12);
		}
		if(c.rules.duelRule[21]){
			c.getItems().removeItem(c.playerEquipment[13], 13);
		}
		c.inDuel = true;
		c.getPA().closeTrade();
		c.specAmount = 10;
		c.getItems().addSpecialBar(c.playerEquipment[c.playerWeapon]);
		c.solTime = -1;
		c.solSpec = false;
		c.getPA().createPlayerHints(10, o.playerId);
		c.getPA().showOption(3, 0, "Attack", 1);
		for(int i = 0; i < 20; i++){
			c.playerLevel[i] = c.getPA().getLevelForXP(c.playerXP[i]);
			c.getPA().refreshSkill(i);
		}
		c.getPA().requestUpdates();
		c.poisonDamage = -1;
		c.poisonImmune = 0;
		c.overloaded = new int[][]{{0, 0}, {1, 0}, {2, 0}, {4, 0}, {6, 0}};
		c.overloadedBool = false;
		c.duelStatus = 5;
	}
	
	public void brokenDuel(){
		Client o = (Client)PlayerHandler.players[c.duelingWith];
		if(o != null){
			o.duelStatus = 0;
			o.getTradeAndDuel().finishDuel();
			for(GameItem item : c.otherStakedItems){
				if(item.id > 0 && item.amount > 0){
					if(Item.itemStackable[item.id])
						if(!o.inventory.addItem(item.id, item.amount, -1))
							Server.itemHandler.createGroundItem(o, item.id, o.getX(), o.getY(), o.heightLevel, item.amount, o.getId());
					else{
						int amount = item.amount;
						for(int a = 1; a <= amount; a++)
							if(!o.inventory.addItem(item.id, 1, -1))
								Server.itemHandler.createGroundItem(o, item.id, o.getX(), o.getY(), o.heightLevel, 1, o.getId());
					}
				}
			}
			o.saveGame();
			c.otherStakedItems.clear();
			o.getTradeAndDuel().resetDuelItems();
			o.getTradeAndDuel().resetDuel();
			c.getTradeAndDuel().finishDuel();
			c.getTradeAndDuel().claimStakedItems();
			c.freezeTimer = 0;
		}else{
			c.getTradeAndDuel().duelVictory();
			c.getTradeAndDuel().claimStakedItems();
		}
	}

	public void finishDuel(){
		c.getCombat().resetPrayers();
		for(int i = 0; i < 20; i++){
			c.playerLevel[i] = c.getPA().getLevelForXP(c.playerXP[i]);
			c.getPA().refreshSkill(i);
		}
		c.playerIndex = 0;
		c.underAttackBy = 0;
		c.poisonDamage = -1;
		c.poisonImmune = 0;
		c.overloaded = new int[][]{{0, 0}, {1, 0}, {2, 0}, {4, 0}, {6, 0}};
		c.overloadedBool = false;
		c.beginDueling = false;
		c.duel2h = false;
		c.getPA().refreshSkill(3);
		c.specAmount = 10.0;
		c.doubleHit = false;
		c.specHit = false;
		c.usingSpecial = false;
		c.specEffect = 0;
		c.projectileStage = 0;
		c.getItems().updateSpecialBar();
		c.getPA().showInterface(6733);
		c.getPA().movePlayer(Config.DUELING_RESPAWN_X + (Misc.random(Config.RANDOM_DUELING_RESPAWN)), Config.DUELING_RESPAWN_Y + (Misc.random(Config.RANDOM_DUELING_RESPAWN)), 0);
		c.getPA().requestUpdates();
		c.getPA().showOption(3, 0, "Challenge", 3);
		c.getPA().createPlayerHints(10, -1);
		c.canOffer = true;
		c.duelSpaceReq = 0;
		c.duelingWith = 0;
		c.inDuel = false;
		c.getCombat().resetPlayerAttack();
		c.duelRequested = false;
	}
	
	public void duelVictory(){
		synchronized(c){
			Client o = (Client)PlayerHandler.players[c.duelingWith];
			if(o != null){
				c.getPA().sendText("" + o.combatLevel, 6839);
				c.getPA().sendText(o.playerName, 6840);
				o.duelStatus = 0;
				checkStake(o);
			}else{
				c.getPA().sendText("", 6839);
				c.getPA().sendText("", 6840);
			}
			c.duelStatus = 6;
			finishDuel();
			if(o != null)
				o.getTradeAndDuel().resetDuelItems();
			duelRewardInterface();
		}
	}

	public void duelRewardInterface(){
		synchronized(c){
			c.freezeTimer = 3;
			c.getOutStream().createFrameVarSizeWord(53);
			c.getOutStream().writeWord(6822);
			c.getOutStream().writeWord(c.otherStakedItems.size());
			for(GameItem item : c.otherStakedItems){
				if(item.amount > 254){
					c.getOutStream().writeByte(255);
					c.getOutStream().writeDWord_v2(item.amount);
				}else{
					c.getOutStream().writeByte(item.amount);
				}
				if(item.id > Config.ITEM_LIMIT || item.id < 0){
					item.id = Config.ITEM_LIMIT;
				}
				c.getOutStream().writeWordBigEndianA(item.id + 1);
			}
			c.getOutStream().endFrameVarSizeWord();
			c.flushOutStream();
		}
	}

	public void checkStake(Client o){
		long p1Val = 0, p2Val = 0;
		String p1Items = "{", p2Items = "{";
		try{
			for(GameItem item : c.stakedItems){
				if(item.id > 0){
					p2Items += "{" + item.id + "," + item.amount + "},";
					long value = Market.itemHasData(item.id) ? Market.getMarketItem(item.id).value : ((long)c.getShops().getItemShopValue(item.id));
					p2Val += (long)((value) * ((long)item.amount));
				}
			}
			for(GameItem item : c.otherStakedItems){
				if(item.id > 0){
					p1Items += "{" + item.id + "," + item.amount + "},";
					long value = Market.itemHasData(item.id) ? Market.getMarketItem(item.id).value : ((long)c.getShops().getItemShopValue(item.id));
					p1Val += (long)((value) * ((long)item.amount));
				}
			}
			p1Items = p1Items.substring(0, p1Items.length() - 1).replace(",", ", ");
			p2Items = p2Items.substring(0, p2Items.length() - 1).replace(",", ", ");
			if(p1Items.length() > 0)
				p1Items += "}";
			if(p2Items.length() > 0)
				p2Items += "}";
			long wealthTransfer = p1Val > p2Val ? p1Val - p2Val : p2Val - p1Val;
			if(wealthTransfer >= 1000000000 && p1Val >= 500000000)
				new HadesThread(HadesThread.REPORT, (p1Val > p2Val ? o : c), "DUEL - " + (p1Val > p2Val ? o.playerName : c.playerName) + " to " + (p1Val > p2Val ? c.playerName : o.playerName) + " Wealth Transfer - " + Misc.numberFormat(wealthTransfer) + "<br /><br />" + o.playerName + " items - " + p1Items + "<br /><br />" + c.playerName + " items - " + p2Items, 9, true);
		}catch(Exception e){}
	}
	public void claimStakedItems(){
		synchronized(c){
			for(GameItem item : c.otherStakedItems){
				if(item.id > 0 && item.amount > 0){
					if(Item.itemStackable[item.id]){
						if(!c.inventory.addItem(item.id, item.amount, -1)){
							Server.itemHandler.createGroundItem(c, item.id, c.getX(), c.getY(), c.heightLevel, item.amount, c.getId());
						}
					}else{
						int amount = item.amount;
						for(int a = 1; a <= amount; a++){
							if(!c.inventory.addItem(item.id, 1, -1)){
								Server.itemHandler.createGroundItem(c, item.id, c.getX(), c.getY(), c.heightLevel, 1, c.getId());
							}
						}
					}
				}
			}
			for(GameItem item : c.stakedItems){
				if(item.id > 0 && item.amount > 0){
					if(Item.itemStackable[item.id]){
						if(!c.inventory.addItem(item.id, item.amount, -1)){
							Server.itemHandler.createGroundItem(c, item.id, c.getX(), c.getY(), c.heightLevel, item.amount, c.getId());
						}
					}else{
						int amount = item.amount;
						for(int a = 1; a <= amount; a++){
							if(!c.inventory.addItem(item.id, 1, -1)){
								Server.itemHandler.createGroundItem(c, item.id, c.getX(), c.getY(), c.heightLevel, 1, c.getId());
							}
						}
					}
				}
			}
			c.freezeTimer = 3;
			c.duelStatus = 0;
			resetDuelItems();
			resetDuel();
			PlayerSave.saveGame(c);
		}
	}

	public void declineDuel(boolean other){
		if(other){
			Client o = (Client)PlayerHandler.players[c.duelingWith];
			if(o != null)
				o.getTradeAndDuel().declineDuel(false);
		}
		c.getPA().closeTrade();
		c.canOffer = true;
		c.duelStatus = 0;
		c.duelingWith = 0;
		c.duel2h = false;
		c.duelSpaceReq = 0;
		c.duelRequested = false;
		for(GameItem item : c.stakedItems){
			if(item.amount < 1)
				continue;
			c.inventory.addItem(item.id, item.amount, -1);
			item.amount = 0;
		}
		resetDuelItems();
		c.rules = null;
	}

	public void resetDuel(){
		c.getPA().showOption(3, 0, "Challenge", 3);
		c.headIconHints = 0;
		c.rules = null;
		c.duelArena = -1;
		c.getPA().createPlayerHints(10, -1);
		c.duelStatus = 0;
		c.inDuel = false;
		c.duel2h = false;
		c.canOffer = true;
		c.duelSpaceReq = 0;
		c.duelingWith = 0;
		c.vengOn = false;
		c.vecnaSkullTimer = -1;
		c.getPA().requestUpdates();
		c.getCombat().resetPlayerAttack();
		c.duelRequested = false;
	}

	public void resetDuelItems(){
		synchronized(c){
			c.stakedItems.clear();
			c.otherStakedItems.clear();
		}
	}

	public void changeDuelStuff(){
		Client o = (Client)PlayerHandler.players[c.duelingWith];
		if(o == null)
			return;
		o.duelStatus = 1;
		c.duelStatus = 1;
		o.getPA().sendText("", 6684);
		c.getPA().sendText("", 6684);
	}

	public boolean find(int i){
		if(i >= 11 && i <= 16)
			return c.playerEquipment[i - 11] > 0;
		int match[][] = {{17, 7}, {18, 9}, {19, 10}, {20, 12}, {21, 13}};
		for(int j = 0; j < 6; j++)
			if(match[j][0] == i)
				return c.playerEquipment[match[j][1]] > 0;
		return false;
	}

	public boolean checkDuelSpace(int amount){
		int spaces_required = 0;
		for(int i = 11; i < 22; i++)
			if(find(i) && c.rules.duelRule[i])
				spaces_required++;
		if(c.rules.duelRule[16] && c.playerEquipment[c.playerWeapon] > 0)
			if(c.getItems().is2handed(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]), c.playerEquipment[c.playerWeapon]))
				spaces_required++;
		if(c.inventory.freeSlots() - spaces_required < amount)
			return false;
		return true;
	}

	public void selectRule(int i){ // rules
		Client o = (Client)PlayerHandler.players[c.duelingWith];
		if(o == null)
			return;
		if(!c.canOffer || o.duelingWith != c.playerId || o.rules == null || c.rules == null || (c.playerId != o.rules.player_one && c.playerId != o.rules.player_two) || (c.playerId != c.rules.player_one && c.playerId != c.rules.player_two))
			return;
		changeDuelStuff();
		o.duelSlot = c.duelSlot;
		boolean c2h = c.getItems().is2handed(Item.getItemName(c.playerEquipment[c.playerWeapon]), c.playerEquipment[c.playerWeapon]);
		boolean o2h = o.getItems().is2handed(Item.getItemName(o.playerEquipment[o.playerWeapon]), o.playerEquipment[o.playerWeapon]);
		if(i >= 11 && c.duelSlot > -1){
			if(c.playerEquipment[c.duelSlot] > 0 || ((c.playerEquipment[c.playerWeapon] > 0 && c.duelSlot == c.playerShield && c2h) && !c.duel2h))
				c.duelSpaceReq += !c.rules.duelRule[i] ? 1 : -1;
			if(o.playerEquipment[o.duelSlot] > 0 || ((o.playerEquipment[o.playerWeapon] > 0 && o.duelSlot == o.playerShield && o2h) && !o.duel2h))
				o.duelSpaceReq += !o.rules.duelRule[i] ? 1 : -1;
			if(c.inventory.freeSlots() < (c.duelSpaceReq) || o.inventory.freeSlots() < (o.duelSpaceReq)){
				c.sendMessage("You or your opponent don't have the required space to set this rule.");
				if(c.playerEquipment[c.duelSlot] > 0 || (c.playerEquipment[c.playerWeapon] > 0 && c.duelSlot == c.playerShield && c.getItems().is2handed(Item.getItemName(c.playerEquipment[c.playerWeapon]), c.playerEquipment[c.playerWeapon])))
					c.duelSpaceReq--;
				if(o.playerEquipment[o.duelSlot] > 0 || (o.playerEquipment[o.playerWeapon] > 0 && o.duelSlot == o.playerShield && o.getItems().is2handed(Item.getItemName(o.playerEquipment[o.playerWeapon]), o.playerEquipment[o.playerWeapon])))
					o.duelSpaceReq--;
				return;
			}
		}
		if(!c.rules.duelRule[i]){
			c.rules.duelRule[i] = true;
			c.duelOption += c.DUEL_RULE_ID[i];
		}else{
			c.rules.duelRule[i] = false;
			c.duelOption -= c.DUEL_RULE_ID[i];
		}

		c.getPA().sendFrame87(286, c.duelOption);
		o.duelOption = c.duelOption;
		o.rules.duelRule[i] = c.rules.duelRule[i];
		o.getPA().sendFrame87(286, o.duelOption);

		if((c.duelSlot == c.playerShield || c.duelSlot == c.playerWeapon) && c2h)
			c.duel2h = c.rules.duelRule[14] || c.rules.duelRule[16];
		if((o.duelSlot == o.playerShield || o.duelSlot == o.playerWeapon) && o2h)
			o.duel2h = o.rules.duelRule[14] || o.rules.duelRule[16];
		
		if(c.rules.duelRule[8] && c.rules.duelRule[1]){
			int x = 0, y = 0, pos = 0;
			do{
				pos = Misc.random(ARENA_COORDS[1].length - 1);
				x = ARENA_COORDS[1][pos][0] + (Misc.random(6) * (Misc.random(99) >= 49 ? 1 : -1));
				y = ARENA_COORDS[1][pos][1] + (Misc.random(3) * (Misc.random(99) >= 49 ? 1 : -1));
			}while(!server.clip.region.Region.canMove(x + 1, y + 1, x, y, c.heightLevel, 1, 1) || !!server.clip.region.Region.canMove(x, y, x - 1, y, c.heightLevel, 1, 1));
			c.duelTeleX = x;
			o.duelTeleX = x - 1;
			c.duelTeleY = y;
			o.duelTeleY = y;
		}else if(c.rules.duelRule[1]){
			int x = 0, y = 0, pos = 0;
			pos = Misc.random(ARENA_COORDS[0].length - 1);
			x = ARENA_COORDS[0][pos][0] + (Misc.random(6) * (Misc.random(99) >= 49 ? 1 : -1));
			y = ARENA_COORDS[0][pos][1] + (Misc.random(3) * (Misc.random(99) >= 49 ? 1 : -1));
			c.duelTeleX = x;
			o.duelTeleX = x - 1;
			c.duelTeleY = y;
			o.duelTeleY = y;
		}
	}*/
}