package server.model.items;

public class GameItem{
	public int id, amount;
	public boolean stackable;

	public GameItem(int id, int amount){
		this.stackable = Item.itemStackable[id];
		this.id = id;
		this.amount = amount;
	}
}