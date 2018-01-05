package server.model.players.skills;

import server.Config;
import server.clip.region.Region;
import server.model.players.Client;
import server.util.Misc;

public class Farming{

	private Client c;

	private final int[] VALID_SEEDS = {5291, 5292, 5293, 5294, 5295, 5296, 5297, 5298, 5299, 5300, 5301, 5302, 5303, 5304};
	private final int[] HERBS = {199, 201, 203, 205, 207, 3049, 209, 211, 213, 3051, 215, 2485, 217, 219}; /*
																											 * lant
																											 * ,
																											 * toad
																											 * ,
																											 * snap
																											 */
	private final int[] SEED_PLANT_EXP = {11, 14, 16, 22, 27, 34, 43, 55, 69, 88, 107, 135, 171, 200};
	private final int[] HERB_EXPS = {13, 15, 18, 24, 31, 39, 49, 62, 78, 99, 120, 152, 192, 225};
	private final int[] FARMING_REQS = {1, 14, 19, 26, 32, 38, 44, 50, 56, 62, 67, 73, 79, 85};
	private final int PATCH_HERBS = 8143;
	private final int PATCH_WEEDS = 8389;

	public Farming(Client c){
		this.c = c;
	}

	public void checkItemOnObject(int itemId, int x, int y){
		for(int j = 0; j < VALID_SEEDS.length; j++){
			if(itemId == VALID_SEEDS[j]){
				handleFarming(VALID_SEEDS[j], HERBS[j], HERB_EXPS[j], j, x, y);
				return;
			}
		}
	}

	private void handleFarming(int seedId, int herbId, int exp, int slot, int x, int y){
		if(c.playerLevel[c.playerFarming] < FARMING_REQS[slot]){
			c.sendMessage("You need a farming level of " + FARMING_REQS[slot] + " to farm this seed.");
			return;
		}
		if(c.inventory.hasItem(seedId, 1)){
			c.inventory.deleteItem(seedId, c.inventory.findItemSlot(seedId), 1);
			c.getPA().addSkillXP(SEED_PLANT_EXP[slot] * Config.FARMING_EXPERIENCE, c.playerFarming);
			c.getPA().refreshSkill(c.playerFarming);
			int herbAmount = Misc.random(5) + 3;
			c.farm[0] = herbId;
			c.farm[1] = herbAmount;
			updateHerbPatch(x, y);
		}
	}

	public int getExp(){
		for(int j = 0; j < HERBS.length; j++)
			if(HERBS[j] == c.farm[0])
				return HERB_EXPS[j];
		return 0;
	}

	public void updateHerbPatch(int x, int y){
		if(c.farm[0] > 0 && c.farm[1] > 0){
			// make object here
			// c.sendMessage("Make herbs...");
			c.getPA().object(PATCH_HERBS, x, y, -1, 10);
			Region.addObject(PATCH_HERBS, x, y, 0, 10, 0);
		}else{
			// make weed patch here
			// c.sendMessage("Make weeds...");
			c.getPA().object(PATCH_WEEDS, x, y, -1, 10);
			Region.addObject(PATCH_WEEDS, x, y, 0, 10, 0);
		}
	}

	public void pickHerb(int x, int y){
		if(c.farm[0] > 0 && c.farm[1] > 0){
			if(c.inventory.addItem(c.farm[0], 1, -1)){
				// c.startAnimation(2273);
				c.getPA().addSkillXP(getExp() * Config.FARMING_EXPERIENCE, c.playerFarming);
				c.farm[1]--;
				if(c.farm[1] == 0)
					c.farm[0] = -1;
				updateHerbPatch(x, y);
				c.sendMessage("You pick a herb.");
				c.getPA().resetAnimation();
			}
		}
	}

}