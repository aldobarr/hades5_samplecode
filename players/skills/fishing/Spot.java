package server.model.players.skills.fishing;

import java.util.ArrayList;
import java.util.HashMap;
import server.model.players.Client;

/**
 * 
 * @author hadesflames
 * 
 */

public class Spot{
	private int id, x, y;
	private ArrayList<ArrayList<Integer>> possibleFish = new ArrayList<ArrayList<Integer>>();
	private static HashMap<Integer, Spot> spots = new HashMap<Integer, Spot>();

	public Spot(int id, int x, int y, int spotId){
		this.id = id;
		this.x = x;
		this.y = y;
		addFish();
		spots.put(spotId, this);
	}

	public int getId(){
		return id;
	}

	public int getX(){
		return x;
	}

	public int getY(){
		return y;
	}

	public void addFish(){
		ArrayList<Integer> zero = new ArrayList<Integer>(); // Top Spot Click.
		ArrayList<Integer> one = new ArrayList<Integer>(); // Bottom Spot Click.
		switch(id){
			case 314: // Trout, Salmon | Pike, Cavefish, Rocktail
				zero.add(335); // Trout
				zero.add(331); // Salmon
				one.add(349); // Pike
				one.add(15270); // Rocktail
				possibleFish.add(zero);
				possibleFish.add(one);
				break;
			case 324: // Lobsters | Tuna, Swordfish
				zero.add(377); // Lobster
				one.add(359); // Tuna
				one.add(371); // Swordfish
				possibleFish.add(zero);
				possibleFish.add(one);
				break;
			case 326: // Shrimp, Anchovies | Sardine, Herring
				zero.add(321); // Anchovies
				zero.add(317); // Shrimp
				possibleFish.add(zero);
				possibleFish.add(one);
				break;
			case 334: // Monkfish | Tuna, Swordfish, Sharks
				one.add(359); // Tuna
				one.add(371); // SwordFish
				one.add(383); // Sharks
				zero.add(7944); // Monkfish
				possibleFish.add(zero);
				possibleFish.add(one);
				break;
		}
	}

	public int[] getPossibleFish(Client c, int click){
		if(click < 0 || click >= possibleFish.size())
			return new int[]{};
		int min = 100;
		ArrayList<Integer> fish = new ArrayList<Integer>();
		for(int i = 0; i < possibleFish.get(click).size(); i++){
			int id = this.possibleFish.get(click).get(i);
			int level = Fish.get(id).getLevel();
			if(level < min)
				min = level;
			if(c.playerLevel[c.playerFishing] >= level)
				fish.add(id);
		}
		int possibleFish[] = new int[fish.size()];
		for(int i = 0; i < fish.size(); i++)
			possibleFish[i] = fish.get(i);
		return possibleFish.length == 0 ? new int[]{-1, min} : possibleFish;
	}

	public static Spot get(int id, int x, int y){
		int spotId = id * x * y;
		if(!spots.containsKey(spotId))
			return null;
		return spots.get(spotId);
	}

	public static void createSpot(int id, int x, int y){
		int spotId = id * x * y;
		if(!spots.containsKey(spotId))
			;
		new Spot(id, x, y, spotId);
	}

	public static void handleSpot(Client c, int id, int x, int y, int clickType){
		Spot spot = get(id, x, y);
		c.getFishing().fish(spot, clickType);
	}

	public static boolean isSpot(int id){
		int possibleIds[] = {314, 316, 324, 326, 334};
		for(int possibleId : possibleIds)
			if(id == possibleId)
				return true;
		return false;
	}
}