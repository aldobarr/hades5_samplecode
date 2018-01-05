package server.model.items;

/**
 * 
 * @author hadesflames
 * 
 */
public class InventoryItem{
	public int id, amount, degrade;

	public InventoryItem(int id, int amount, int degrade){
		this.id = id;
		this.amount = amount;
		this.degrade = degrade;
	}
}