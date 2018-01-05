package server.model.players.skills;

import server.model.players.Client;
import server.util.Misc;
import server.Config;

public class Cooking{

	Client c;

	public Cooking(Client c){
		this.c = c;
	}

	static{
		initItems();
	}

	private static void initItems(){
		// Raw Id, cooked Id, Burnt Id, Cooking Level, Experience, No Burn
		// level, No Burn on fire level (optional).
		int cookingItems[][] = {{317, 315, 7954, 1, 30, 33}, {321, 319, 323, 1, 30, 34}, {335, 333, 343, 20, 70, 50}, {331, 329, 343, 30, 90, 58}, {359, 361, 367, 35, 100, 65, 66}, {377, 379, 381, 40, 120, 74}, {371, 373, 375, 50, 140, 86}, {7944, 7946, 7948, 62, 150, 90, 92}, {383, 385, 387, 80, 210, 100, 150}, {389, 391, 393, 91, 169, 150}, {15270, 15272, 15274, 93, 225, 100, 150}, {349, 351, 343, 20, 80, 64}, {395, 397, 399, 82, 211, 150}};
		for(int cookingItem[] : cookingItems)
			if(cookingItem.length >= 6)
				new CookingItem(cookingItem[0], cookingItem[1], cookingItem[2], cookingItem[3], cookingItem[4], cookingItem[5], (cookingItem.length == 7 ? cookingItem[6] : -1));
	}

	public void itemOnObject(int id, int objectId){
		int emote = 0, objectType = 0;
		switch(objectId){
			case 2732:
				objectType = 0;
				emote = 897;
				break;
			case 12269:
				objectType = 1;
				emote = 883;
				break;
			case 2728:
				objectType = 2;
				emote = 883;
				break;
		}
		if(CookingItem.contains(id))
			cookFish(id, emote, objectType);
	}

	public void cookFish(int id, int emoteId, int objectType){
		CookingItem item = CookingItem.get(id);
		for(int j = 0; j < 28; j++){
			if(c.inventory.hasItem(id, 1)){
				if(c.playerLevel[c.playerCooking] >= item.getLevelReq()){
					c.startAnimation(emoteId);
					c.turnPlayerTo(c.objectX, c.objectY);
					if(c.playerLevel[c.playerCooking] < (objectType == 0 ? item.getFireNoBurn() : item.getNoBurn()) && burnedFish(item, objectType)){
						c.sendMessage("You accidently burn the fish.");
						c.inventory.deleteItem(id, 1);
						c.inventory.addItem(item.getBurnt(), 1, -1);
					}else{
						c.inventory.deleteItem(id, 1);
						c.inventory.addItem(item.getCooked(), 1, -1);
						c.getPA().addSkillXP(item.getXP() * Config.COOKING_EXPERIENCE, c.playerCooking);
					}
				}else{
					c.sendMessage("You need a cooking level of " + item.getLevelReq() + " to cook this fish.");
					break;
				}
			}else
				break;
		}
	}

	public boolean burnedFish(CookingItem item, int objectType){
		int req = 3 - objectType;
		return Misc.random(c.playerLevel[c.playerCooking] + 5 - item.getLevelReq()) <= req;
	}
}