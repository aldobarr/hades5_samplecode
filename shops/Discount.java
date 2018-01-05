package server.model.shops;

public class Discount{
	public int itemId;
	public int amount;
	public boolean percent;
	public boolean failed;

	public Discount(String contents[]){
		failed = false;
		try{
			itemId = Integer.parseInt(contents[0]);
			amount = Integer.parseInt(contents[1]);
			percent = contents[2].equalsIgnoreCase("p");
			if(!percent && !contents[2].equalsIgnoreCase("a"))
				failed = true;
		}catch(Exception e){
			failed = true;
		}
	}
}
