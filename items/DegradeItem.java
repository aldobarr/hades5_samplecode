package server.model.items;

public class DegradeItem{
	private int itemId, degradeTime, degradeItems[];
	private boolean time;
	public DegradeItem(int itemId, int degradeTime, int degradeItems[], boolean time){
		this.itemId = itemId;
		this.degradeTime = degradeTime;
		this.degradeItems = degradeItems;
		this.time = time;
	}
	public int getItemId(){
		return itemId;
	}
	public int getDegradeTime(){
		return degradeTime;
	}
	public boolean getTime(){
		return time;
	}
	public boolean isDegradeItem(int itemId){
		if(this.itemId == itemId)
			return true;
		for(int i = 0; i<degradeItems.length; i++)
			if(degradeItems[i] == itemId)
				return true;
		return false;
	}
	public boolean isDegradedItem(int itemId){
		for(int i = 0; i<degradeItems.length; i++)
			if(degradeItems[i] == itemId)
				return true;
		return false;
	}
	public int[] getDegradeItems(){
		return degradeItems;
	}
	public String toString(){
		String str = "[" + itemId + ", " + (time ? "" : "-") + degradeTime + ", [";
		for(int i = 0; i<degradeItems.length; i++)
			str += degradeItems[i] + (i + 1 == degradeItems.length ? "" : ", ");
		return str + "]]";
	}
}