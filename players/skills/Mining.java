package server.model.players.skills;

import server.model.players.*;
import server.Config;
import server.util.Misc;

public class Mining{

	Client c;

	private final int VALID_PICK[] = {1265, 1267, 1269, 1273, 1271, 1275, 15259};
	private final int[] PICK_REQS = {1, 1, 6, 21, 31, 41, 61};
	private final int[] RANDOM_GEMS = {1623, 1621, 1619, 1617, 1631};
	private int oreType;
	private int exp;
	public int levelReq;
	public int pickType;
	private final int EMOTE = 625;

	public Mining(Client c){
		this.c = c;
	}

	public void startMining(int oreType, int levelReq, int exp){
		c.turnPlayerTo(c.objectX, c.objectY);
		if(goodPick() > 0){
			if(c.playerLevel[c.playerMining] >= levelReq){
				this.oreType = oreType;
				this.exp = exp;
				this.levelReq = levelReq;
				this.pickType = goodPick();
				c.sendMessage("You swing your pick at the rock.");
				c.miningTimer = getMiningTimer(oreType);
				c.startAnimation(EMOTE);
			}else{
				resetMining();
				c.sendMessage("You need a mining level of " + levelReq + " to mine this rock.");
				c.startAnimation(65535);
			}
		}else{
			resetMining();
			c.sendMessage("You need a pickaxe to mine this rock.");
			c.startAnimation(65535);
			c.getPA().resetVariables();
		}
	}

	public void mineOre(){
		if(goodPick() > 0){
			if(c.inventory.addItem(oreType, 1, -1)){
				c.startAnimation(EMOTE);
				c.sendMessage("You manage to mine some ore.");
				int finalExp = getExp(exp * Config.MINING_EXPERIENCE);
				c.getPA().addSkillXP(finalExp, c.playerMining);
				c.getPA().refreshSkill(c.playerMining);
				c.miningTimer = getMiningTimer(oreType);
				if(Misc.random(25) == 10){
					c.inventory.addItem(RANDOM_GEMS[(int)(RANDOM_GEMS.length * Math.random())], 1, -1);
					c.sendMessage("You find a gem!");
				}
			}else{
				c.getPA().resetVariables();
				c.startAnimation(65535);
			}
		}else{
			resetMining();
			c.sendMessage("You need a pickaxe to mine this rock.");
			c.startAnimation(65535);
			c.getPA().resetVariables();
		}
	}
	
	private int getExp(int exp){
		double percent = 0.0;
		if(c.playerEquipment[c.playerHat] == 20789)
			percent += 0.01;
		if(c.playerEquipment[c.playerChest] == 20791)
			percent += 0.01;
		if(c.playerEquipment[c.playerLegs] == 20790)
			percent += 0.01;
		if(c.playerEquipment[c.playerHands] == 20787)
			percent += 0.01;
		if(c.playerEquipment[c.playerFeet] == 20788)
			percent += 0.01;
		return ((int)Math.ceil(exp * (1 + percent)));
	}

	public void resetMining(){
		this.oreType = -1;
		this.exp = -1;
		this.levelReq = -1;
		this.pickType = -1;
	}

	public int goodPick(){
		for(int j = VALID_PICK.length - 1; j >= 0; j--){
			if(c.playerEquipment[c.playerWeapon] == VALID_PICK[j]){
				if(c.playerLevel[c.playerMining] >= PICK_REQS[j])
					return VALID_PICK[j];
			}
		}
		for(int i = 0; i < c.inventory.items.length; i++){
			for(int j = VALID_PICK.length - 1; j >= 0; j--){
				if(c.inventory.items[i] == null)
					continue;
				if(c.inventory.items[i].id == VALID_PICK[j] + 1){
					if(c.playerLevel[c.playerMining] >= PICK_REQS[j])
						return VALID_PICK[j];
				}
			}
		}
		return -1;
	}

	public int getMiningTimer(int pick){
		int ore_time = getOreTime();
		if(ore_time < 0)
			return -1;
		int add = 0;
		for(int i = VALID_PICK.length - 1; i >= 0; i--){
			if(pick == VALID_PICK[i]){
				ore_time += add;
				break;
			}
			if(i == VALID_PICK.length - 1)
				++add;
			++add;
		}
		return ore_time;
	}

	public int getOreTime(){
		switch(oreType){
			case 436:
			case 438:
			case 440:
				return 1;
			case 444:
				return 2;
			case 447:
				return 3;
			case 449:
				return 4;
			case 451:
				return 5;
			case 453:
				return 7;
			default:
				return -1;
		}
	}
}