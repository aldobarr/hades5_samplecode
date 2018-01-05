package server.model.players.skills.fishing;

import server.Config;
import server.util.Misc;
import server.model.players.Client;
import server.model.players.skills.fishing.Fish;

public class Fishing{

	private Client c;
	private int possibleFish[], clickType;
	private static final int STAND = 65535;
	private Spot spot;
	static{
		initFish();
	}

	public Fishing(Client c){
		this.c = c;
	}

	public static void initFish(){
		// Fish Id, Level Req, Exp
		int fish_vals[][] = {{317, 1, 10}, {321, 1, 40}, {335, 20, 50}, {331, 30, 70}, {359, 35, 80}, {7944, 62, 120}, {383, 76, 150}, {389, 81, 300}, {349, 25, 60}, {371, 50, 100}, {377, 40, 90}, {15270, 90, 380}};
		for(int fish_val[] : fish_vals)
			if(fish_val.length >= 3)
				new Fish(fish_val[0], fish_val[1], fish_val[2]);
	}

	public void fish(Spot spot, int clickType){
		if(spot == null)
			return;
		possibleFish = spot.getPossibleFish(c, clickType);
		if(possibleFish.length <= 0)
			return;
		if(c.inventory.freeSlots() <= 0){
			c.sendMessage("You do not have enough space to do that!");
			resetFishing();
			return;
		}
		if(checkReqs(spot.getId(), clickType)){
			if(possibleFish[0] > 0){
				c.startAnimation(getEmote(spot.getId(), clickType));
				c.fishTimer = 3;
				c.fishing = true;
				this.spot = spot;
				this.clickType = clickType;
			}else{
				c.sendMessage("You need a fishing level of at least " + (possibleFish.length > 1 ? possibleFish[1] : 1) + " to fish here.");
				resetFishing();
			}
		}else{
			c.sendMessage("You do not have the correct equipment to use this fishing spot.");
			resetFishing();
		}
	}

	public void catchFish(){
		if(possibleFish.length <= 0)
			return;
		if(!checkReqs(spot.getId(), clickType)){
			c.sendMessage("You do not have the correct equipment to use this fishing spot.");
			resetFishing();
			return;
		}
		if(possibleFish[0] <= 0){
			c.sendMessage("You need a fishing level of at least " + (possibleFish.length > 1 ? possibleFish[1] : 1) + " to fish here.");
			resetFishing();
			return;
		}
		Fish fish = null;
		for(int i = 0; i < possibleFish.length; i++){
			fish = Fish.get(possibleFish[i]);
			if(Misc.random(100) < 15)
				break;
		}
		c.startAnimation(getEmote(spot.getId(), clickType));
		c.inventory.addItem(fish.getId(), 1, -1);
		c.getPA().addSkillXP(fish.getExp() * Config.FISHING_EXPERIENCE, c.playerFishing);
		c.sendMessage("You catch a fish.");
		c.fishTimer = 2 + Misc.random(4);
		if(c.inventory.freeSlots() <= 0){
			c.sendMessage("You have run out of space!");
			resetFishing();
			return;
		}
	}

	private boolean checkReqs(int spotId, int click){
		if(spotId == 314) // Lure | Bait
			return c.inventory.hasItem(click == 0 ? 309 : 307);
		else if(spotId == 324) // Cage | Harpoon
			return c.inventory.hasItem(click == 0 ? 301 : 311);
		else if(spotId == 326) // Net | Bait
			return c.inventory.hasItem(click == 0 ? 303 : 307);
		else if(spotId == 334) // Net | Harpoon
			return c.inventory.hasItem(click == 0 ? 303 : 311);
		return false;
	}

	public int getEmote(int spotId, int click){
		if(spotId == 314) // Lure | Bait
			return c.fishing ? 623 : 622;
		else if(spotId == 324) // Cage | Harpoon
			return click == 0 ? 619 : 618;
		else if(spotId == 326) // Net | Bait
			return click == 0 ? 621 : (c.fishing ? 623 : 622);
		else if(spotId == 334) // Net | Harpoon
			return click == 0 ? 621 : 618;
		return STAND;
	}

	public void resetFishing(){
		spot = null;
		possibleFish = null;
		clickType = -1;
		c.startAnimation(STAND);
		c.fishTimer = -1;
		c.fishing = false;
	}
}