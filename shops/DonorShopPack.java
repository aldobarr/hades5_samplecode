package server.model.shops;

import java.util.ArrayList;

public class DonorShopPack{
	public int id;
	public ArrayList<Integer> items;

	public DonorShopPack(int id, ArrayList<Integer> items){
		this.id = id;
		this.items = items;
	}
}