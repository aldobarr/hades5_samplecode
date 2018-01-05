package server.model.items;

import java.util.ArrayList;

import server.Config;
import server.model.players.Client;
import server.model.players.PlayerSave;
import server.model.players.Potions;

/**
 * 
 * @author hadesflames
 * 
 */
public class Inventory{
	public Client player;
	public InventoryItem items[] = new InventoryItem[28];

	public Inventory(Client player){
		this.player = player;
	}

	/**
	 * Counts the total number of empty inventory spaces a player has.
	 * 
	 * @return The total number of empty inventory spaces.
	 */
	public int freeSlots(){
		int count = 0;
		for(int i = 0; i < items.length; i++)
			if(items[i] == null)
				count++;
		return count;
	}

	/**
	 * Counts the total amount of an item a player has in their inventory.
	 * 
	 * @param itemId
	 *            The id of the item we are looking for.
	 * @return The total amount of that item the player has in their inventory.
	 */
	public int getItemCount(int itemId){
		int count = 0;
		for(int i = 0; i < items.length; i++)
			if(items[i] != null && items[i].id == itemId + 1)
				count += items[i].amount;
		return count;
	}

	/**
	 * Deletes all items in a player's inventory.
	 */
	public void deleteAllItems(){
		for(int i = 0; i < items.length; i++)
			items[i] = null;
		PlayerSave.saveGame(player);
		resetItems(3214);
	}
	
	/**
	 * Replaces some item in the player's inventory with a different item.
	 * Stack amounts remain unchanged, and all matching items in all slots of the inventory get replaced.
	 * @param oldId The id of the item we're looking to replace.
	 * @param newId The id of the new item we want to replace the old with.
	 */
	public void replaceItem(int oldId, int newId){
		for(int i = 0; i<items.length; i++)
			if(items[i] != null && items[i].id - 1 == oldId)
				items[i].id = newId + 1;
		player.saveGame();
		resetItems(3214);
	}
	
	/**
	 * Converts all the donor tickets in the player's inventory to donation points.
	 * @return The number of donor tickets that were converted.
	 */
	public int convertTickets(){
		int points = 0;
		for(int i = 0; i<items.length; i++){
			if(items[i] == null)
				continue;
			if(items[i].id == (Config.DONATION_TICKET + 1)){
				points += items[i].amount;
				deleteItem(Config.DONATION_TICKET, i, items[i].amount);
			}
		}
		return points;
	}

	/**
	 * Add an item to a player's inventory with no degrade.
	 * 
	 * @param item
	 *            The id of the item to be added.
	 * @param amount
	 *            The amount of the item to add.
	 * @return True if the item was successfully added.
	 */
	public boolean addItem(int item, int amount){
		return addItem(item, amount, -1);
	}
	
	/**
	 * Add an item to a player's inventory.
	 * 
	 * @param item
	 *            The id of the item to be added.
	 * @param amount
	 *            The amount of the item to add.
	 * @param degrade
	 *            Degrade time of an item. (if -1, item is not degradable)
	 * @return True if the item was successfully added.
	 */
	public boolean addItem(int item, int amount, int degrade){
		synchronized(player){
			amount = amount < 1 ? 1 : amount;
			if(item <= 0)
				return false;
			if(freeSlots() > 0 || (hasItem(item) && Item.itemStackable[item])){
				if(Item.itemStackable[item]){
					for(int i = 0; i < items.length; i++){
						if(items[i] != null){
							if(items[i].id == (item + 1)){
								items[i].amount += (((items[i].amount + amount) < Config.MAXITEM_AMOUNT) && ((items[i].amount + amount) > -1)) ? amount : (Config.MAXITEM_AMOUNT - items[i].amount);
								if(player != null && player.getOutStream() != null){
									player.getOutStream().createFrameVarSizeWord(34);
									player.getOutStream().writeWord(3214);
									player.getOutStream().writeByte(i);
									player.getOutStream().writeWord(items[i].id);
									if(items[i].amount > 254){
										player.getOutStream().writeByte(255);
										player.getOutStream().writeDWord(items[i].amount);
									}else
										player.getOutStream().writeByte(items[i].amount);
									player.getOutStream().endFrameVarSizeWord();
									player.flushOutStream();
								}
								return true;
							}
						}
					}
				}
				amount = amount > -1 ? amount : 0;
				boolean added = false;
				while(amount > 0 && freeSlots() > 0){
					for(int i = 0; i < items.length; i++){
						if(items[i] == null){
							items[i] = new InventoryItem(item + 1, Item.itemStackable[item] ? amount : 1, degrade);
							amount -= Item.itemStackable[item] ? amount : 1;
							added = true;
							break;
						}
					}
				}
				resetItems(3214);
				return added;
			}else{
				resetItems(3214);
				player.sendMessage("Not enough space in your inventory.");
				return false;
			}
		}
	}
	
	/**
	 * Changes all untrimmed capes to trimmed capes.
	 */
	public void fixUntrimmedCape(){
		for(InventoryItem item : items)
			if(item != null && player.isUntrimmedCape(item.id - 1))
				item.id++;
	}

	/**
	 * Resets the inventory interface to show any new changes to the inventory.
	 * 
	 * @param WriteFrame
	 *            The frame id of the inventory.
	 */
	public void resetItems(int WriteFrame){
		synchronized(player){
			if(player != null && player.getOutStream() != null){
				player.getOutStream().createFrameVarSizeWord(53);
				player.getOutStream().writeWord(WriteFrame);
				player.getOutStream().writeWord(items.length);
				for(int i = 0; i < items.length; i++){
					if(items[i] == null){
						player.getOutStream().writeByte(0);
						player.getOutStream().writeWordBigEndianA(0);
						continue;
					}
					if(items[i].id <= 0 || items[i].amount <= 0){
						items[i] = null;
						player.getOutStream().writeByte(0);
						player.getOutStream().writeWordBigEndianA(0);
						continue;
					}
					player.getOutStream().writeByte(items[i].amount > 254 ? 255 : items[i].amount);
					if(items[i].amount > 254)
						player.getOutStream().writeDWord_v2(items[i].amount);
					player.getOutStream().writeWordBigEndianA(items[i].id);
				}
				player.getOutStream().endFrameVarSizeWord();
				player.flushOutStream();
			}
		}
	}

	/**
	 * Resets the temporary inventory interface to show any new changes to the
	 * temporary inventory.
	 */
	public void resetTempItems(){
		synchronized(player){
			if(player != null && player.getOutStream() != null){
				player.getOutStream().createFrameVarSizeWord(53);
				player.getOutStream().writeWord(5064);
				player.getOutStream().writeWord(items.length);
				for(int i = 0; i < items.length; i++){
					if(items[i] == null){
						player.getOutStream().writeByte(0);
						player.getOutStream().writeWordBigEndianA(0);
						continue;
					}
					if(items[i].id <= 0 || items[i].amount <= 0){
						items[i] = null;
						player.getOutStream().writeByte(0);
						player.getOutStream().writeWordBigEndianA(0);
						continue;
					}
					player.getOutStream().writeByte(items[i].amount > 254 ? 255 : items[i].amount);
					if(items[i].amount > 254)
						player.getOutStream().writeDWord_v2(items[i].amount);
					player.getOutStream().writeWordBigEndianA(items[i].id);
				}
				player.getOutStream().endFrameVarSizeWord();
				player.flushOutStream();
			}
		}
	}

	/**
	 * Checks if the player has a certain item in his inventory
	 * 
	 * @param itemId
	 *            The id of the item being searched for.
	 * @return True if the player has that item.
	 */
	public boolean hasItem(int itemId){
		itemId++;
		for(InventoryItem item : items)
			if(item != null && item.id == itemId)
				return true;
		return false;
	}

	/**
	 * Checks the player's inventory for a certain amount of a certain item.
	 * 
	 * @param itemId
	 *            The id of the item being searched for.
	 * @param amount
	 *            The amount of the item being searched for.
	 * @return True if the player has the item and at least the amount
	 *         specified.
	 */
	public boolean hasItem(int itemId, int amount){
		itemId++;
		int num = 0;
		for(InventoryItem item : items){
			if(item == null)
				continue;
			if(item.id == itemId){
				num += item.amount;
				if(num >= amount)
					return true;
			}
		}
		return num >= amount;
	}

	/**
	 * Checks if the player has a certain item.
	 * 
	 * @param itemId
	 *            The id of the item being searched for.
	 * @param amount
	 *            The amount of the item.
	 * @param slot
	 *            The slot the item should be in.
	 * @param strict
	 * 			  If true, the slot must have at least the same amount as the amount specified.
	 * 			  If false, then the amount can be distributed out in the inventory.
	 * @return True if the player has the amount of the item being searched for,
	 *         or more AND the slot looked at has that item.
	 */
	public boolean hasItem(int itemId, int amount, int slot, boolean strict){
		itemId++;
		int found = 0;
		if(strict && items[slot] != null)
			return items[slot].id == itemId && items[slot].amount >= amount;
		if(items[slot] != null && items[slot].id == itemId){
			for(int i = 0; i < items.length; i++){
				if(items[i] != null && items[i].id == itemId){
					found += amount;
					if(items[i].amount >= amount || found >= amount)
						return true;
				}
			}
			return found >= amount;
		}
		return false;
	}

	/**
	 * Moves items around in the invetory.
	 * 
	 * @param from
	 *            From.
	 * @param to
	 *            To.
	 */
	public void moveItems(int from, int to){
		if(items[from] == null)
			return;
		InventoryItem temp = new InventoryItem(items[from].id, items[from].amount, items[from].degrade);
		items[from] = items[to];
		items[to] = temp;
		resetTempItems();
		resetItems(3214);
	}

	/**
	 * Deletes a certain amount of an item in the player's inventory.
	 * 
	 * @param id
	 *            The id of the item being deleted.
	 * @param amount
	 *            The amount of the item being deleted.
	 */
	public void deleteItem(int id, int amount){
		if(id <= 0 || amount <= 0)
			return;
		for(int i = 0; i < items.length; i++){
			if(items[i] != null && items[i].id == id + 1){
				if(items[i].amount > amount){
					items[i].amount -= amount;
					if(items[i].amount <= 0)
						items[i] = null;
					break;
				}else{
					amount -= items[i].amount;
					items[i] = null;
					if(amount <= 0)
						break;
				}
			}
		}
		resetItems(3214);
	}

	/**
	 * Deletes a certain amount of an item from a specified slot in the player's
	 * inventory.
	 * 
	 * @param id
	 *            The id of the item to be deleted.
	 * @param slot
	 *            The slot the item is located.
	 * @param amount
	 *            The amount of the item to be removed.
	 * @return True if the item was successfully deleted.
	 */
	public boolean deleteItem(int id, int slot, int amount){
		if(id <= 0 || slot < 0 || slot >= items.length || items[slot] == null || items[slot].id != (id + 1) || amount <= 0)
			return false;
		if(items[slot].amount > amount){
			items[slot].amount -= amount;
			if(items[slot].amount <= 0)
				items[slot] = null;
		}else
			items[slot] = null;
		resetItems(3214);
		return true;
	}
	
	/**
	 * Swaps an item in the inventory for another item.
	 * This will do nothing if the new id is the same as the old id.
	 * 
	 * @param id
	 *            The id of the item to be swapped.
	 * @param slot
	 *            The slot the item is located.
	 * @param newId
	 *            The id of the new item.
	 * @param degrade
	 * 			  The degrade time of the new item.
	 * @return True if the item was successfully swapped.
	 */
	public boolean swapItem(int id, int slot, int newId, int degrade){
		if(id <= 0 || slot < 0 || slot >= items.length || items[slot] == null || items[slot].id != (id + 1) || newId == id)
			return false;
		int amount = items[slot].amount;
		items[slot] = new InventoryItem(newId, amount, degrade);
		resetItems(3214);
		return true;
	}
	
	/**
	 * Swaps an item in the inventory for another item. The new item will have no degrade.
	 * This will do nothing if the new id is the same as the old id.
	 * 
	 * @param id
	 *            The id of the item to be swapped.
	 * @param slot
	 *            The slot the item is located.
	 * @param newId
	 *            The id of the new item.
	 * @return True if the item was successfully swapped.
	 */
	public boolean swapItem(int id, int slot, int newId){
		if(id <= 0 || slot < 0 || slot >= items.length || items[slot] == null || items[slot].id != (id + 1) || newId == id)
			return false;
		int amount = items[slot].amount;
		items[slot] = new InventoryItem(newId, amount, -1);
		resetItems(3214);
		return true;
	}
	
	/**
	 * Decants one potion type in the player's inventory.
	 * @param name The name of the potion to be decanted.
	 */
	private MyArrayList decantPot(String name, int id){
		MyArrayList stuff = new MyArrayList();
		int amounts = 0;
		for(int i = 0; i<items.length; i++){
			InventoryItem item = items[i];
			if(item == null)
				continue;
			if(Item.itemIsNote[id] != Item.itemIsNote[item.id - 1])
				continue;
			String temp_name = player.getItems().getItemName(item.id - 1);
			if(!player.getPotions().isPotion2(temp_name) || !Potions.isSamePot(name + "(", temp_name))
				continue;
			amounts += Potions.getAmount(temp_name) * item.amount;
			stuff.removes.add(i);
		}
		stuff.pot.amount = amounts;
		stuff.pot.fullId = player.getPotions().getFullId(name, id);
		stuff.pot.isNote = Item.itemIsNote[id];
		return stuff;
	}

	/**
	 * Decants all of the potions in a player's inventory.
	 */
	public void decantAllPots(boolean notes){
		ArrayList<Integer> removes = new ArrayList<Integer>();
		ArrayList<Potion> adds = new ArrayList<Potion>();
		ArrayList<String> names = new ArrayList<String>();
		boolean found_notes = false;
		for(InventoryItem item : items){
			if(item == null || !player.getPotions().isPotion(item.id - 1))
				continue;
			if(Item.itemIsNote[item.id - 1] && !notes){
				found_notes = true;
				continue;
			}
			if(!Item.itemIsNote[item.id - 1] && notes)
				continue;
			String name = Potions.getPotName(player.getItems().getItemName(item.id - 1));
			if(names.contains(name))
				continue;
			MyArrayList temp = decantPot(name, item.id - 1);
			removes.addAll(temp.removes);
			adds.add(temp.pot);
			names.add(name);
		}
		for(int i : removes)
			items[i] = null;
		for(Potion pot : adds){
			int temp = pot.amount / 4;
			pot.amount -= temp * 4;
			if(notes){
				int slot = findItemSlot(pot.fullId);
				if(slot > -1)
					items[slot].amount += temp;
				else{
					for(int i = 0; i<items.length; i++){
						if(items[i] == null && temp > 0){
							items[i] = new InventoryItem(pot.fullId + 1, temp, -1);
							temp--;
							break;
						}
					}
				}
			}else{
				for(int i = 0; i<items.length; i++){
					if(items[i] == null && temp > 0){
						items[i] = new InventoryItem(pot.fullId + 1, 1, -1);
						temp--;
						continue;
					}
				}
			}
			String name = Potions.getPotName(player.getItems().getItemName(pot.fullId));
			if(pot.amount > 0 && Potions.potIds.containsKey(name)){
				int ids[] = Potions.potIds.get(name);
				int id = 0;
				for(int i = 0; i<ids.length; i++){
					if(ids[i] < 1 || Item.itemIsNote[pot.fullId] != Item.itemIsNote[ids[i]])
						continue;
					if(Potions.getAmount(player.getItems().getItemName(ids[i])) == pot.amount){
						id = ids[i];
						break;
					}
				}
				for(int i = 0; i<items.length; i++){
					if(items[i] == null){
						items[i] = new InventoryItem(id + 1, 1, -1);
						break;
					}
				}
			}
		}
		if((!notes && !found_notes) || notes)
			resetItems(3214);
		if(!notes && found_notes)
			decantAllPots(true);
	}
	
	/**
	 * Finds an item in the player's invetory.
	 * 
	 * @param itemId
	 *            The id of the item being searched.
	 * @return The slot the item is located in inventory, or -1 if it was not
	 *         found.
	 */
	public int findItemSlot(int itemId){
		for(int i = 0; i < items.length; i++)
			if(items[i] != null && items[i].id == itemId + 1)
				return i;
		return -1;
	}
}
class MyArrayList{
	public ArrayList<Integer> removes = new ArrayList<Integer>();
	public Potion pot = new Potion();
}
class Potion{
	public int fullId = 0;
	public int amount = 0;
	public boolean isNote = false;
}