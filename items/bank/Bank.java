package server.model.items.bank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import server.Config;
import server.model.items.Degrade;
import server.model.items.Item;
import server.model.players.Client;

/**
 * 
 * @author hadesflames
 * 
 */
public class Bank{
	public ArrayList<Tab> tabs = new ArrayList<Tab>();
	public Tab searchTab = new Tab();
	public Client player;
	public static final int CAPACITY = 600;
	public String searchTerm = "";
	public static final String SEARCH_RESULTS[] = {"No search term entered!", "No matches found!", ""};

	public Bank(Client c){
		player = c;
		tabs.add(new Tab());
	}

	/**
	 * Returns the total amount of this item in the bank.
	 * 
	 * @param id
	 *            The id of the item to search for.
	 * @return The total amount of this item in the bank.
	 */
	public int getBankAmount(int id){
		int count = 0;
		for(Tab tab : tabs)
			if(tab != null)
				for(BankItem item : tab.tabItems)
					if(item.id == id + 1)
						count += item.amount;
		return count;
	}

	/**
	 * Sends the client the current bank information so that it may be
	 * displayed.
	 */
	public void resetBank(){
		synchronized(player){
			if(tabs.size() == 0)
				tabs.add(new Tab());
			player.getOutStream().createFrameVarSizeWord(52);
			player.getOutStream().writeWord(numTabs());
			player.getOutStream().writeByte(player.selectedTab);
			for(int t = 1; t < numTabs(); t++)
				sendTabIdentifier(t);
			for(int i = 0; i < (player.selectedTab != 0 ? 1 : numTabs()); i++)
				sendTab(player.selectedTab != 0 ? player.selectedTab : i);
			player.getOutStream().endFrameVarSizeWord();
			player.flushOutStream();
			player.getPA().sendText("" + getNumItems(), 51226);
		}
	}

	private void sendTabIdentifier(int t){
		if(tabs.get(t) != null && tabs.get(t).getNumItems() > 0){
			BankItem item = tabs.get(t).get(0);
			player.getOutStream().writeDWord(item.amount);
			player.getOutStream().writeWord(item.id);
		}else{
			tabs.remove(t);
			player.getOutStream().writeDWord(0);
			player.getOutStream().writeWord(0);
		}
	}

	/**
	 * Sends a tab's info to the client.
	 * 
	 * @param i
	 *            The id of the tab to be sent.
	 */
	private void sendTab(int i){
		if(i < 0 || i >= tabs.size())
			return;
		synchronized(player){
			int numItems = tabs.get(i).getNumItems();
			if(numItems == 0 && tabs.size() > 1){
				tabs.remove(i);
				if(player.selectedTab == i)
					player.selectedTab = 0;
				return;
			}
			player.getOutStream().writeWord(numItems);
			for(int itemIndex = 0; itemIndex < numItems; itemIndex++){
				BankItem item = tabs.get(i).get(itemIndex);
				if(item == null){
					tabs.get(i).remove(item);
					continue;
				}
				player.getOutStream().writeDWord(item.amount);
				player.getOutStream().writeWord(item.id);
			}
		}
	}

	/**
	 * Sends a tab's info to the client.
	 * 
	 * @param tab
	 *            The tab to be sent.
	 */
	private void sendTab(Tab tab){
		synchronized(player){
			int numItems = tab.getNumItems();
			player.getOutStream().writeWord(numItems);
			for(int itemIndex = 0; itemIndex < numItems; itemIndex++){
				BankItem item = tab.get(itemIndex);
				if(item == null){
					tabs.get(itemIndex).remove(item);
					continue;
				}
				player.getOutStream().writeDWord(item.amount);
				player.getOutStream().writeWord(item.id);
			}
		}
	}
	
	/**
	 * Changes all untrimmed capes to trimmed capes.
	 */
	public void fixUntrimmedCape(){
		for(Tab tab : tabs)
			for(BankItem item : tab.tabItems)
				if(item != null && player.isUntrimmedCape(item.id - 1))
					item.swapItem(new BankItem(item.id + 1, item.amount, -1));
	}
	
	/**
	 * Sets the amount of an item in a player's bank.
	 * @param id
	 *            The id of the item in the bank.
	 * @param amount
	 *            The amount this item should have inside the bank.
	 */
	public void setItemAmount(int id, int amount){
		for(Tab tab : tabs){
			for(BankItem item : tab.tabItems){
				if(item.id == id + 1){
					item.amount = amount;
					return;
				}
			}
		}
	}

	/**
	 * Sends the search results to the client.
	 */
	public void createSearch(String searchTerm){
		searchTab.tabItems.clear();
		this.searchTerm = searchTerm;
		if(!searchTerm.isEmpty())
			populateSearchTab();
		player.getPA().sendText(SEARCH_RESULTS[searchTerm.isEmpty() ? 0 : (searchTab.tabItems.size() == 0 ? 1 : 2)], 51237);
		synchronized(player){
			player.getOutStream().createFrameVarSizeWord(52);
			player.getOutStream().writeWord(1);
			player.getOutStream().writeByte(player.selectedTab);
			sendTab(searchTab);
			player.getOutStream().endFrameVarSizeWord();
			player.flushOutStream();
		}
	}

	/**
	 * Populate the invisible search tab.
	 */
	public void populateSearchTab(){
		searchTab.tabItems.clear();
		for(Tab tab : tabs)
			for(BankItem item : tab.tabItems)
				if(Item.getItemName(item.id - 1).toLowerCase().contains(searchTerm))
					searchTab.add(item);
	}

	/**
	 * Deposits a player's entire inventory into the bank.
	 */
	public void depositInventory(){
		if(!player.isBanking)
			return;
		if(player.isSearching){
			player.isSearching = false;
			player.getPA().sendConfig(576, 0);
			player.getPA().closeInput();
			player.getPA().sendText("", 51237);
		}
		for(int i = 0; i < player.inventory.items.length; i++)
			if(player.inventory.items[i] != null)
				deposit(player.inventory.items[i].id - 1, i, player.inventory.items[i].amount);
	}

	/**
	 * Adds all of the player's equipment to his or her bank.
	 */
	public void depositEquip(){
		if(!player.isBanking)
			return;
		try{
			if(player.isSearching){
				player.isSearching = false;
				player.getPA().sendConfig(576, 0);
				player.getPA().closeInput();
				player.getPA().sendText("", 51237);
			}
			for(int i = 0; i < player.playerEquipment.length; i++){
				if(player.playerEquipment[i] > 0){
					if(Item.itemStackable[player.playerEquipment[i]] && !Degrade.isDegradedItem(player.playerEquipment[i])){
						int toBankSlot = 0;
						boolean alreadyInBank = false;
						Tab tab = null;
						for(int j = 0; j < tabs.size(); j++){
							tab = tabs.get(j);
							for(int k = 0; k < tab.getNumItems(); k++){
								if(tab.get(k).id == player.playerEquipment[i] + 1){
									alreadyInBank = true;
									toBankSlot = k;
									break;
								}
							}
							if(alreadyInBank)
								break;
						}
						if(!alreadyInBank){
							if(player.selectedTab >= tabs.size())
								player.selectedTab = 0;
							tab = tabs.get(player.selectedTab);
						}
						if(!alreadyInBank && freeSlots() > 0){
							tab.tabItems.add(new BankItem(player.playerEquipment[i] + 1, player.playerEquipmentN[i], player.playerEquipmentD[i]));
							player.getItems().deleteEquipment(player.playerEquipment[i], i);
						}else if(alreadyInBank){
							if((tab.get(toBankSlot).amount + player.playerEquipmentN[i]) <= Config.MAXITEM_AMOUNT && (tab.get(toBankSlot).amount + player.playerEquipmentN[i]) > -1)
								tab.get(toBankSlot).amount += player.playerEquipmentN[i];
							else{
								player.sendMessage("Bank full!");
								return;
							}
							player.getItems().deleteEquipment(player.playerEquipment[i], i);
						}else{
							player.sendMessage("Bank Full!");
							return;
						}
					}else{
						int toBankSlot = 0;
						Tab tab = null;
						boolean alreadyInBank = false;
						for(int j = 0; j < tabs.size(); j++){
							tab = tabs.get(j);
							for(int k = 0; k < tab.getNumItems(); k++){
								if(tab.get(k).id == player.playerEquipment[i] + 1){
									alreadyInBank = true;
									toBankSlot = k;
									break;
								}
							}
							if(alreadyInBank)
								break;
						}
						if(Degrade.isDegradedItem(player.playerEquipment[i]))
							alreadyInBank = false;
						if(!alreadyInBank){
							if(player.selectedTab >= tabs.size())
								player.selectedTab = 0;
							tab = tabs.get(player.selectedTab);
						}
						if(!alreadyInBank && freeSlots() > 0){
							tab.tabItems.add(new BankItem(player.playerEquipment[i] + 1, player.playerEquipmentN[i], player.playerEquipmentD[i]));
							player.getItems().deleteEquipment(player.playerEquipment[i], i);
						}else if(alreadyInBank){
							if((tab.get(toBankSlot).amount + player.playerEquipmentN[i]) <= Config.MAXITEM_AMOUNT && (tab.get(toBankSlot).amount + player.playerEquipmentN[i]) > -1)
								tab.get(toBankSlot).amount += player.playerEquipmentN[i];
							else{
								player.sendMessage("Bank full!");
								return;
							}
							player.getItems().deleteEquipment(player.playerEquipment[i], i);
							resetBank();
						}else{
							player.sendMessage("Bank full!");
							return;
						}
					}
				}
			}
		}finally{
			player.saveGame();
			resetBank();
			player.startAnimation(65535);
			player.getCombat().getPlayerAnimIndex(player.getItems().getItemName(player.playerEquipment[player.playerWeapon]).toLowerCase());
		}
	}

	/**
	 * Deposit an item from the player's inventory, to the player's bank in the
	 * tab that the player has selected if the bank does not have the item. If
	 * the does exist in the bank then it is deposited in the slot the item
	 * already exists in.
	 * 
	 * @param itemID
	 *            The id of the item being deposited.
	 * @param fromSlot
	 *            The slot the item is in the inventory.
	 * @param amount
	 *            The amount of the item being deposited.
	 * @return True if the deposit is successful.
	 */
	public boolean deposit(int itemID, int fromSlot, int amount){
		try{
			if(tabs.size() == 0)
				tabs.add(new Tab());
			if(player.isSearching){
				player.isSearching = false;
				player.getPA().sendConfig(576, 0);
				player.getPA().closeInput();
				player.getPA().sendText("", 51237);
			}
			itemID++;
			if(player.inventory.items[fromSlot] == null || player.inventory.items[fromSlot].id != itemID || !player.isBanking)
				return false;
			amount = player.inventory.getItemCount(itemID - 1) < amount ? player.inventory.getItemCount(itemID - 1) : amount;
			if(amount <= 0)
				return false;
			if(!Item.itemIsNote[player.inventory.items[fromSlot].id - 1]){
				if((Item.itemStackable[player.inventory.items[fromSlot].id - 1] || player.inventory.items[fromSlot].amount > 1) && !Degrade.isDegradedItem(player.inventory.items[fromSlot].id - 1)){
					int toBankSlot = 0;
					Tab tab = null;
					boolean alreadyInBank = false;
					for(int i = 0; i < tabs.size(); i++){
						tab = tabs.get(i);
						for(int j = 0; j < tab.getNumItems(); j++){
							if(tab.get(j).id == itemID){
								alreadyInBank = true;
								toBankSlot = j;
								break;
							}
						}
						if(alreadyInBank)
							break;
					}
					if(!alreadyInBank){
						if(player.selectedTab >= tabs.size() || player.selectedTab < 0)
							player.selectedTab = 0;
						tab = tabs.get(player.selectedTab);
					}
					if(!alreadyInBank && freeSlots() > 0){
						tab.add(new BankItem(itemID, amount, -1));
						player.inventory.deleteItem(itemID - 1, amount);
						player.inventory.resetTempItems();
						resetBank();
						return true;
					}else if(alreadyInBank){
						if((tab.get(toBankSlot).amount + amount) <= Config.MAXITEM_AMOUNT && (tab.get(toBankSlot).amount + amount) > -1)
							tab.get(toBankSlot).amount += amount;
						else{
							player.sendMessage("Bank full!");
							return false;
						}
						player.inventory.deleteItem(itemID - 1, amount);
						player.inventory.resetTempItems();
						resetBank();
						return true;
					}else{
						player.sendMessage("Bank full!");
						return false;
					}
				}else{
					int toBankSlot = 0;
					boolean alreadyInBank = false;
					Tab tab = null;
					for(int i = 0; i < tabs.size(); i++){
						tab = tabs.get(i);
						for(int j = 0; j < tab.getNumItems(); j++){
							if(tab.get(j).id == itemID){
								alreadyInBank = true;
								toBankSlot = j;
								break;
							}
						}
						if(alreadyInBank)
							break;
					}
					if(Degrade.isDegradedItem(itemID - 1))
						alreadyInBank = false;
					if(!alreadyInBank){
						if(player.selectedTab >= tabs.size())
							player.selectedTab = 0;
						tab = tabs.get(player.selectedTab);
					}
					if(!alreadyInBank && freeSlots() > 0){
						if(Degrade.isDegradedItem(itemID - 1)){
							tab.add(new BankItem(itemID, 1, player.inventory.items[fromSlot].degrade));
							player.inventory.deleteItem(itemID - 1, fromSlot, 1);
							player.inventory.resetTempItems();
							resetBank();
							return true;
						}
						tab.add(new BankItem(itemID, amount, -1));
						player.inventory.deleteItem(itemID - 1, amount);
						player.inventory.resetTempItems();
						resetBank();
						return true;
					}else if(alreadyInBank){
						if((tab.get(toBankSlot).amount + amount) <= Config.MAXITEM_AMOUNT && (tab.get(toBankSlot).amount + amount) > -1)
							tab.get(toBankSlot).amount += amount;
						else{
							player.sendMessage("Bank full!");
							return false;
						}
						player.inventory.deleteItem(itemID - 1, amount);
						player.inventory.resetTempItems();
						resetBank();
						return true;
					}else{
						player.sendMessage("Bank full!");
						return false;
					}
				}
			}else if(Item.itemIsNote[player.inventory.items[fromSlot].id - 1] && !Item.itemIsNote[player.inventory.items[fromSlot].id - 2]){
				if(Item.itemStackable[player.inventory.items[fromSlot].id - 1] || player.inventory.items[fromSlot].amount > 1){
					int toBankSlot = 0;
					boolean alreadyInBank = false;
					Tab tab = null;
					for(int i = 0; i < tabs.size(); i++){
						tab = tabs.get(i);
						for(int j = 0; j < tab.getNumItems(); j++){
							if(tab.get(j).id == itemID - 1){
								alreadyInBank = true;
								toBankSlot = j;
								break;
							}
						}
						if(alreadyInBank)
							break;
					}
					if(!alreadyInBank){
						if(player.selectedTab >= tabs.size())
							player.selectedTab = 0;
						tab = tabs.get(player.selectedTab);
					}
					if(!alreadyInBank && freeSlots() > 0){
						tab.add(new BankItem(itemID - 1, amount, -1));
						player.inventory.deleteItem(itemID - 1, amount);
						player.inventory.resetTempItems();
						resetBank();
						return true;
					}else if(alreadyInBank){
						if((tab.get(toBankSlot).amount + amount) <= Config.MAXITEM_AMOUNT && (tab.get(toBankSlot).amount + amount) > -1)
							tab.get(toBankSlot).amount += amount;
						else{
							player.sendMessage("Bank full!");
							return false;
						}
						player.inventory.deleteItem(itemID - 1, amount);
						player.inventory.resetTempItems();
						resetBank();
						return true;
					}else{
						player.sendMessage("Bank full!");
						return false;
					}
				}else{
					int toBankSlot = 0;
					boolean alreadyInBank = false;
					Tab tab = null;
					for(int i = 0; i < tabs.size(); i++){
						tab = tabs.get(i);
						for(int j = 0; j < tab.getNumItems(); j++){
							if(tab.get(j).id == itemID){
								alreadyInBank = true;
								toBankSlot = j;
								break;
							}
						}
						if(alreadyInBank)
							break;
					}
					if(!alreadyInBank){
						if(player.selectedTab >= tabs.size())
							player.selectedTab = 0;
						tab = tabs.get(player.selectedTab);
					}
					if(!alreadyInBank && freeSlots() > 0){
						tab.add(new BankItem(itemID, amount, -1));
						player.inventory.deleteItem(itemID - 1, amount);
						player.inventory.resetTempItems();
						resetBank();
						return true;
					}else if(alreadyInBank){
						if((tab.get(toBankSlot).amount + amount) <= Config.MAXITEM_AMOUNT && (tab.get(toBankSlot).amount + amount) > -1)
							tab.get(toBankSlot).amount += amount;
						else{
							player.sendMessage("Bank full!");
							return false;
						}
						player.inventory.deleteItem(itemID - 1, amount);
						player.inventory.resetTempItems();
						resetBank();
						return true;
					}else{
						player.sendMessage("Bank full!");
						return false;
					}
				}
			}else{
				player.sendMessage("Item not supported " + (player.inventory.items[fromSlot].id - 1));
				return false;
			}
		}finally{
			player.saveGame();
		}
	}

	/**
	 * Delete a specified number of a certain item, from a specified slot in a
	 * specified tab in the bank.
	 * 
	 * @param itemID
	 *            The id of the item.
	 * @param fromTab
	 *            The tab the item should be in.
	 * @param fromSlot
	 *            The slot in the tab the item should be in.
	 * @param amount
	 *            The amount of the item we are deleting.
	 */
	public void deleteFromBank(int itemID, int fromTab, int fromSlot, int amount){
		if(fromTab >= tabs.size())
			return;
		Tab tab = tabs.get(fromTab);
		BankItem item = tab.get(fromSlot);
		if(item == null)
			return;
		if(amount >= item.amount){
			tab.remove(item);
			if(tab.getNumItems() == 0 && tabs.size() > 1){
				tabs.remove(tab);
				if(player.selectedTab == tabs.indexOf(tab))
					player.selectedTab = 0;
			}
		}else
			item.amount -= amount;
		resetBank();
	}

	/**
	 * Withdraws an item from the bank into a player's inventory.
	 * 
	 * @param itemID
	 *            The id of the item to withdraw.
	 * @param fromSlot
	 *            The slot the item should be in the bank.
	 * @param amount
	 *            The amount of the item being removed.
	 */
	public void withdraw(int itemID, int fromSlot, int amount){
		try{
			if(tabs.size() == 0)
				tabs.add(new Tab());
			if(amount <= 0 || itemID <= 0 || fromSlot < 0)
				return;
			if(player.selectedTab >= tabs.size())
				player.selectedTab = 0;
			if(!selectedTabHasItem(itemID))
				return;
			Tab tab = player.isSearching ? getSearchTabItem(itemID) : player.selectedTab == 0 ? convertSlotToTab(fromSlot) : tabs.get(player.selectedTab);
			fromSlot = player.isSearching ? getSearchTabItemSlot(tab, itemID) : player.selectedTab == 0 ? convertSlotToTabSlot(fromSlot) : fromSlot;
			if(tab == null || fromSlot < 0 || fromSlot >= tab.tabItems.size() || tab.get(fromSlot) == null || tab.get(fromSlot).id != itemID + 1)
				return;
			if(!player.takeAsNote){
				if(Item.itemStackable[tab.get(fromSlot).id - 1]){
					if(tab.get(fromSlot).amount > amount){
						if(player.inventory.addItem(tab.get(fromSlot).id - 1, amount, tab.get(fromSlot).degrade)){
							tab.get(fromSlot).amount -= amount;
							if(player.isSearching)
								createSearch(searchTerm);
							else
								resetBank();
							player.inventory.resetItems(5064);
						}
					}else{
						if(player.inventory.addItem(tab.get(fromSlot).id - 1, tab.get(fromSlot).amount, tab.get(fromSlot).degrade)){
							tab.remove(fromSlot);
							if(tab.getNumItems() == 0 && tabs.size() > 1){
								tabs.remove(tab);
								player.selectedTab = 0;
							}
							if(player.isSearching)
								createSearch(searchTerm);
							else
								resetBank();
							player.inventory.resetItems(5064);
						}
					}
				}else{
					while(amount > 0){
						if(tab.get(fromSlot).amount > 0){
							if(player.inventory.addItem(tab.get(fromSlot).id - 1, 1, tab.get(fromSlot).degrade)){
								tab.get(fromSlot).amount--;
								amount--;
								if(tab.get(fromSlot).amount <= 0){
									tab.remove(fromSlot);
									if(tab.getNumItems() == 0 && tabs.size() > 1){
										tabs.remove(tab);
										player.selectedTab = 0;
									}
									break;
								}
							}else
								break;
						}else
							break;
					}
					if(player.isSearching)
						createSearch(searchTerm);
					else
						resetBank();
					player.inventory.resetItems(5064);
				}
			}else if(player.takeAsNote && Item.itemIsNote[tab.get(fromSlot).id]){
				if(tab.get(fromSlot).amount > amount){
					if(player.inventory.addItem(tab.get(fromSlot).id, amount, tab.get(fromSlot).degrade)){
						tab.get(fromSlot).amount -= amount;
						if(player.isSearching)
							createSearch(searchTerm);
						else
							resetBank();
						player.inventory.resetItems(5064);
					}
				}else{
					if(player.inventory.addItem(tab.get(fromSlot).id, tab.get(fromSlot).amount, tab.get(fromSlot).degrade)){
						tab.remove(fromSlot);
						if(tab.getNumItems() == 0 && tabs.size() > 1){
							tabs.remove(tab);
							player.selectedTab = 0;
						}
						if(player.isSearching)
							createSearch(searchTerm);
						else
							resetBank();
						player.inventory.resetItems(5064);
					}
				}
			}else{
				player.sendMessage("This item can't be withdrawn as a note.");
				if(Item.itemStackable[tab.get(fromSlot).id - 1]){
					if(tab.get(fromSlot).amount > amount){
						if(player.inventory.addItem(tab.get(fromSlot).id - 1, amount, tab.get(fromSlot).degrade)){
							tab.get(fromSlot).amount -= amount;
							if(player.isSearching)
								createSearch(searchTerm);
							else
								resetBank();
							player.inventory.resetItems(5064);
						}
					}else{
						if(player.inventory.addItem(tab.get(fromSlot).id - 1, tab.get(fromSlot).amount, tab.get(fromSlot).degrade)){
							tab.remove(fromSlot);
							if(tab.getNumItems() == 0 && tabs.size() > 1){
								tabs.remove(tab);
								player.selectedTab = 0;
							}
							if(player.isSearching)
								createSearch(searchTerm);
							else
								resetBank();
							player.inventory.resetItems(5064);
						}
					}
				}else{
					while(amount > 0){
						if(tab.get(fromSlot).amount > 0){
							if(player.inventory.addItem(tab.get(fromSlot).id - 1, 1, tab.get(fromSlot).degrade)){
								tab.get(fromSlot).amount--;
								amount--;
								if(tab.get(fromSlot).amount <= 0){
									tab.remove(fromSlot);
									if(tab.getNumItems() == 0 && tabs.size() > 1){
										tabs.remove(tab);
										player.selectedTab = 0;
									}
									break;
								}
							}else
								break;
						}else
							break;
					}
					if(player.isSearching)
						createSearch(searchTerm);
					else
						resetBank();
					player.inventory.resetItems(5064);
				}
			}
		}finally{
			player.saveGame();
		}
	}

	/**
	 * Swaps two items in the bank.
	 * 
	 * @param from
	 *            From.
	 * @param to
	 *            To.
	 * @param fromTab
	 *            The tab the item came from.
	 * @param toTab
	 *            The tab the item is going to.
	 */
	public void swapBankItem(int from, int to, int fromTab, int toTab){
		if(tabs.size() <= fromTab || tabs.size() <= toTab || !player.isBanking)
			return;
		Tab tab1 = player.selectedTab == 0 ? tabs.get(fromTab) : tabs.get(player.selectedTab);
		if(tab1.getNumItems() <= from)
			return;
		Tab tab2 = player.selectedTab == 0 ? tabs.get(toTab) : tabs.get(player.selectedTab);
		if(tab2.getNumItems() <= to)
			tab2.add(tab1.remove(from));
		else{
			BankItem temp = tab1.get(from).clone();
			tab1.get(from).swapItem(tab2.get(to));
			tab2.get(to).swapItem(temp);
		}
		if(tab1.getNumItems() == 0 && tabs.size() > 1){
			tabs.remove(tab1);
			player.selectedTab = 0;
		}
		if(tab2.getNumItems() == 0 && tabs.size() > 1){
			tabs.remove(tab2);
			player.selectedTab = 0;
		}
	}

	/**
	 * Swaps two items in the bank.
	 * 
	 * @param from
	 *            From.
	 * @param to
	 *            To.
	 * @param insertMode
	 *            Swap mode or insert mode.
	 * @param fromTab
	 *            The tab the item came from.
	 * @param toTab
	 *            The tab the item is going to.
	 */
	public void moveItems(int from, int to, boolean swap, int fromTab, int toTab){
		if(!swap){
			if(tabs.size() <= fromTab || tabs.size() <= toTab || !player.isBanking)
				return;
			Tab tab1 = player.selectedTab == 0 ? tabs.get(fromTab) : tabs.get(player.selectedTab);
			Tab tab2 = player.selectedTab == 0 ? tabs.get(toTab) : tabs.get(player.selectedTab);
			if(to >= tab2.getNumItems())
				to = tab2.getNumItems() - 1;
			tab2.tabItems.add(to, tab1.remove(from));
			if(tab1.getNumItems() == 0 && tabs.size() > 1){
				tabs.remove(tab1);
				player.selectedTab = 0;
			}
			if(tab2.getNumItems() == 0 && tabs.size() > 1){
				tabs.remove(tab2);
				player.selectedTab = 0;
			}
		}else
			swapBankItem(from, to, fromTab, toTab);
		resetBank();
	}

	/**
	 * Converts all the donor tickets in the player's bank to donation points.
	 * @return The number of donor tickets that were converted.
	 */
	public int convertTickets(){
		int points = 0;
		HashMap<BankItem, Tab> removes = new HashMap<BankItem, Tab>();
		for(Tab tab : tabs){
			for(BankItem item : tab.tabItems){
				if(item == null)
					continue;
				if(item.id == (Config.DONATION_TICKET + 1)){
					points += item.amount;
					removes.put(item, tab);
				}
			}
		}
		Set<BankItem> keys = removes.keySet();
		for(BankItem item : keys){
			Tab tab = removes.get(item);
			tab.remove(item);
		}
		return points;
	}
	
	/**
	 * Checks the bank to see the amount of free slots available in the bank.
	 * 
	 * @return The total number of free slots in the bank.
	 */
	public int freeSlots(){
		int totalItems = 0;
		for(Tab tab : tabs)
			totalItems += tab.getNumItems();
		return Bank.CAPACITY - totalItems;
	}

	/**
	 * Checks the bank to see how many tabs exist.
	 * 
	 * @return The total number of non-empty tabs there are available.
	 */
	public int numTabs(){
		int count = 0;
		for(Tab tab : tabs)
			if(tab != null)
				count++;
		return count;
	}

	/**
	 * Gets the tab a certain item is contained is. Primarily useful for search
	 * withdraw.
	 * 
	 * @param itemId
	 * @return
	 */
	public Tab getSearchTabItem(int itemId){
		for(Tab tab : tabs)
			for(BankItem item : tab.tabItems)
				if(item.id == itemId + 1)
					return tab;
		return tabs.get(0);
	}

	public int getSearchTabItemSlot(Tab tab, int itemId){
		for(int i = 0; i < tab.tabItems.size(); i++)
			if(tab.tabItems.get(i).id == itemId + 1)
				return i;
		return -1;
	}

	/**
	 * Converts a slot from the general bank slot style to the tab style.
	 * 
	 * @param fromSlot
	 *            The slot within the entire bank as a whole.
	 * @return The slot within the tab the item is found.
	 */
	public int convertSlotToTabSlot(int fromSlot){
		if(player.selectedTab != 0)
			return fromSlot;
		int nums = 0;
		for(Tab tab : tabs){
			nums += tab.getNumItems();
			if(fromSlot < nums){
				nums -= tab.getNumItems();
				break;
			}
		}
		return fromSlot - nums;
	}

	/**
	 * Converts a slot to a tab.
	 * 
	 * @param fromSlot
	 *            The slot.
	 * @return The converted tab.
	 */
	public Tab convertSlotToTab(int fromSlot){
		if(player.selectedTab != 0)
			return tabs.get(player.selectedTab);
		int nums = 0;
		Tab tempTab = null;
		for(Tab tab : tabs){
			tempTab = tab;
			nums += tab.getNumItems();
			if(fromSlot < nums)
				return tab;
		}
		return tempTab;
	}

	/**
	 * Finds a specified item, and returns the tab in which it is contained.
	 * 
	 * @param itemId
	 *            The id of the item being searched for.
	 * @return The tab that item is located in. Null if the item could not be
	 *         found.
	 */
	public Tab findItemTab(int itemId){
		for(Tab tab : tabs)
			for(BankItem item : tab.tabItems)
				if(item.id == itemId + 1)
					return tab;
		return null;
	}

	/**
	 * Checks to see if the selected tab has an item. If the selected tab is 0
	 * (general tab) then all tabs are checked.
	 * 
	 * @param itemId
	 *            The id of the item being searched.
	 * @return true if the item was found.
	 */
	public boolean selectedTabHasItem(int itemId){
		if(player.selectedTab >= tabs.size())
			player.selectedTab = 0;
		if(player.selectedTab == 0){
			for(int i = 0; i < tabs.size(); i++){
				Tab tab = tabs.get(i);
				for(int j = 0; j < tab.getNumItems(); j++)
					if(tab.get(j).id == itemId + 1)
						return true;
			}
			return false;
		}
		Tab tab = tabs.get(player.selectedTab);
		for(int i = 0; i < tab.getNumItems(); i++)
			if(tab.get(i).id == itemId + 1)
				return true;
		return false;
	}

	/**
	 * Checks the entire bank for a specified item.
	 * 
	 * @param itemId
	 *            The id of the specified item.
	 * @return True if the bank contains the item.
	 */
	public boolean bankHasItem(int itemId){
		int temp = player.selectedTab;
		player.selectedTab = 0;
		boolean bankHasItem = selectedTabHasItem(itemId);
		player.selectedTab = temp;
		return bankHasItem;
	}

	/**
	 * Moves an item to a new tab in the bank.
	 * 
	 * @param fromSlot
	 *            The slot where the item is located in the bank.
	 */
	public void moveItemToTab(int fromSlot, int toTab){
		Tab tab = convertSlotToTab(fromSlot);
		if(tab == null || !player.isBanking)
			return;
		int from = convertSlotToTabSlot(fromSlot);
		BankItem item = tab.remove(from);
		if(tab.getNumItems() == 0 && tabs.size() > 1){
			tabs.remove(tab);
			player.selectedTab = 0;
		}
		if(toTab < tabs.size())
			tabs.get(toTab).add(item);
		else{
			Tab newTab = new Tab(item);
			tabs.add(newTab);
		}
		resetBank();
	}

	/**
	 * Replace all untrimmed capes with trimmed capes after a second 99.
	 * 
	 * @param cape
	 *            The cape id of the untrimmed cape.
	 */
	public void fixLeveledCapes(int cape){
		for(Tab tab : tabs)
			for(BankItem item : tab.tabItems)
				if(item.id - 1 == cape)
					item.id = cape + 2;
		resetBank();
	}
	
	/**
	 * Adds an item to a players bank without inventory checks. This MUST NOT be used for degradable items.
	 * @param id The id of the item.
	 * @param amount The amount of the item to add.
	 */
	public void addItem(int itemId, int amount){
		for(int i = 0; i < tabs.size(); i++){
			Tab tab = tabs.get(i);
			for(int j = 0; j < tab.getNumItems(); j++){
				BankItem item = tab.get(j);
				if(item.id == itemId + 1){
					if(itemId == 995 && ((long)item.amount + (long)amount) > Integer.MAX_VALUE){
						int val = (int)(((long)item.amount + (long)amount) - 2000000000);
						addItem(Config.BANK_CERTIFICATE, 1);
						item.amount = val;
					}else
						tab.get(j).amount += amount;
					return;
				}
			}
		}
		tabs.get(0).tabItems.add(new BankItem(itemId + 1, amount, -1));
	}

	/**
	 * Calculates the total number of items in the bank.
	 * 
	 * @return The total number of items in the bank.
	 */
	public int getNumItems(){
		int count = 0;
		for(Tab tab : tabs)
			if(tab != null)
				for(BankItem item : tab.tabItems)
					if(item != null)
						count++;
		return count;
	}

	/**
	 * Creates an edit safe clone of this Bank.
	 * 
	 * @return The cloned Bank object.
	 */
	public Bank clone(){
		Bank bank = new Bank(player);
		bank.tabs.remove(0);
		for(int i = 0; i < tabs.size(); i++){
			bank.tabs.add(new Tab());
			for(BankItem item : tabs.get(i).tabItems)
				if(item != null)
					bank.tabs.get(i).add(new BankItem(item.id, item.amount, item.degrade));
		}
		// Check integrity
		ArrayList<Tab> remove = new ArrayList<Tab>();
		for(int i = 0; i < bank.tabs.size(); i++){
			if(bank.tabs.get(i).getNumItems() <= 0){
				remove.add(bank.tabs.get(i));
				continue;
			}
		}
		for(Tab r : remove)
			bank.tabs.remove(r);
		return bank;
	}

	/**
	 * Creates an edit safe clone of this Bank.
	 * 
	 * @param c
	 *            The Client object that should be used in the cloned bank.
	 * @return The cloned Bank object.
	 */
	public Bank clone(Client c){
		Bank bank = new Bank(c);
		bank.tabs.remove(0);
		for(int i = 0; i < tabs.size(); i++){
			bank.tabs.add(new Tab());
			for(BankItem item : tabs.get(i).tabItems)
				if(item != null)
					bank.tabs.get(i).add(new BankItem(item.id, item.amount, item.degrade));
		}
		// Check integrity
		ArrayList<Tab> remove = new ArrayList<Tab>();
		for(int i = 0; i < bank.tabs.size(); i++){
			if(bank.tabs.get(i).getNumItems() <= 0){
				remove.add(bank.tabs.get(i));
				continue;
			}
		}
		for(Tab r : remove)
			bank.tabs.remove(r);
		return bank;
	}
}
