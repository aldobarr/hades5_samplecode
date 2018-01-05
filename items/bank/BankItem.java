package server.model.items.bank;

/**
 * 
 * @author hadesflames
 * 
 */
public class BankItem{
	public int id, amount, degrade;

	public BankItem(int id, int amount, int degrade){
		this.id = id;
		this.amount = amount;
		this.degrade = degrade;
	}

	/**
	 * Swaps this item with a specified item.
	 * 
	 * @param item
	 *            The item specified.
	 */
	public void swapItem(BankItem item){
		this.id = item.id;
		this.amount = item.amount;
		this.degrade = item.degrade;
	}

	/**
	 * Clones this object into a new object and returns it.
	 */
	public BankItem clone(){
		BankItem ret = new BankItem(id, amount, degrade);
		return ret;
	}
	
	/**
	 * @return
	 * 			Returns a string representation of the id, amount and degrade values of this item.
	 */
	public String toString(){
		return "(" + id + ", " + amount + " ," + degrade + ")";
	}
}
