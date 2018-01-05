package server.model.shops;

public class DonorShopItem{
	public int id, price, packAmount;
	public String name;

	public DonorShopItem(int id, int price, int packAmount, String name){
		this.id = id;
		this.price = price;
		this.packAmount = packAmount;
		this.name = name.equalsIgnoreCase("e") ? "" : name;
	}
}