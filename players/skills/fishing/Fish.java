package server.model.players.skills.fishing;

import java.util.HashMap;

/**
 * 
 * @author hadesflames
 * 
 */
public class Fish{
	private int id, level, exp;
	private static HashMap<Integer, Fish> fish = new HashMap<Integer, Fish>();

	public Fish(int id, int level, int exp){
		this.id = id;
		this.level = level;
		this.exp = exp;
		fish.put(id, this);
	}

	public int getId(){
		return id;
	}

	public int getLevel(){
		return level;
	}

	public int getExp(){
		return exp;
	}

	public static Fish get(int id){
		if(!fish.containsKey(id))
			return null;
		return fish.get(id);
	}
}