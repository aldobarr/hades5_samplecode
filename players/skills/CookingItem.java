package server.model.players.skills;

import java.util.HashMap;

public class CookingItem{
	private static HashMap<Integer, CookingItem> cookingItems = new HashMap<Integer, CookingItem>();
	private int rawId, cookedId, burntId, levelReq, xp, noBurn, fireNoBurn;

	public CookingItem(int rawId, int cookedId, int burntId, int levelReq, int xp, int noBurn, int fireNoBurn){
		this.rawId = rawId;
		this.cookedId = cookedId;
		this.burntId = burntId;
		this.levelReq = levelReq;
		this.noBurn = noBurn;
		this.fireNoBurn = fireNoBurn > 0 ? fireNoBurn : noBurn;
		this.xp = xp;
		cookingItems.put(rawId, this);
	}

	public static boolean contains(int id){
		return cookingItems.containsKey(id);
	}

	public static CookingItem get(int id){
		if(!contains(id))
			return null;
		return cookingItems.get(id);
	}

	public int getRaw(){
		return rawId;
	}

	public int getCooked(){
		return cookedId;
	}

	public int getBurnt(){
		return burntId;
	}

	public int getLevelReq(){
		return levelReq;
	}

	public int getNoBurn(){
		return noBurn;
	}

	public int getFireNoBurn(){
		return fireNoBurn > 0 ? fireNoBurn : noBurn;
	}

	public int getXP(){
		return xp;
	}
}