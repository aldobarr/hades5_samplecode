package server.model.items.bank;

import java.util.ArrayList;

/**
 * 
 * @author hadesflames
 * 
 */
public class Tab{
	public ArrayList<BankItem> tabItems = new ArrayList<BankItem>(Bank.CAPACITY);

	public Tab(){}

	public Tab(BankItem item){
		tabItems.add(item);
	}

	/**
	 * Checks the tab to see the amount of items in the tab.
	 * 
	 * @return The total number of items in this tab.
	 */
	public int getNumItems(){
		return tabItems.size();
	}

	/**
	 * Get an item in position pos from the tab. Return null if that item slot
	 * does not exist.
	 * 
	 * @param pos
	 *            The position in this tab.
	 * @return The item in that position.
	 */
	public BankItem get(int pos){
		return pos < tabItems.size() ? tabItems.get(pos) : null;
	}

	/**
	 * Adds an item to the end of the tab.
	 * 
	 * @param item
	 *            The item being added.
	 * @return true (as specified by Collections.add(E))
	 */
	public boolean add(BankItem item){
		if(item == null || item.id <= 0 || item.amount <= 0)
			return false;
		return tabItems.add(item);
	}

	/**
	 * Removes a specified item from the tab, if it exists.
	 * 
	 * @param item
	 *            The item being removed.
	 * @return true if this list contained the specified element.
	 */
	public boolean remove(BankItem item){
		return tabItems.remove(item);
	}

	/**
	 * Removes a specified item from the tab, if it exists.
	 * 
	 * @param index
	 *            The index of the item being removed in the array.
	 * @return The removed BankItem object.
	 */
	public BankItem remove(int index){
		if(index >= tabItems.size())
			return null;
		return tabItems.remove(index);
	}

	/**
	 * Checks the tab for the first slot that is empty.
	 * 
	 * @return The first available slot in the tab. If the tab is full, then
	 *         return -1.
	 */
	public int firstFreeSlot(){
		return tabItems.size() >= Bank.CAPACITY ? -1 : tabItems.size();
	}
}