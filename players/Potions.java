package server.model.players;

import java.io.File;
import java.util.HashMap;
import java.util.Scanner;

import server.model.items.Item;
import server.model.minigames.Duel;
import server.util.Misc;

/**
 * @author Sanity
 */

public class Potions{

	private Client c;
	public static HashMap<String, int[]> potIds = new HashMap<String, int[]>();
	
	public Potions(Client c){
		this.c = c;
	}
	
	public static void setupPots(){
		potIds.clear();
		File f = new File("Data/cfg/pots.cfg");
		try(Scanner in = new Scanner(f)){
			while(in.hasNextLine()){
				String line = in.nextLine();
				if(line.startsWith("//"))
					continue;
				String split[] = line.split("=");
				if(split.length != 2)
					continue;
				String name = split[0].trim();
				String temp_ids[] = split[1].split(",");
				if(temp_ids.length <= 1)
					continue;
				int ids[] = new int[temp_ids.length];
				for(int i = 0; i<ids.length; i++)
					ids[i] = Integer.parseInt(temp_ids[i].trim());
				potIds.put(name, ids);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void handleOverload(int itemId, int replaceItem, int slot){
		if(c.playerLevel[3] < 51){
			c.sendMessage("You do not have enough hp to drink this potion.");
			return;
		}
		if(c.checkOverloadDir(c.absX, c.absY)){
			c.sendMessage("You can not drink this potion in a pvp area.");
			return;
		}
		if(c.overloadedBool){
			c.sendMessage("You have already been overloaded!");
			return;
		}
		//if(c.inPcBoat() && Server.pestControl.gameTimer < 10)
			//return;
		if(c.teleporting || c.inventory.items[slot] == null)
			return;
		c.startAnimation(829);
		c.inventory.items[slot].id = replaceItem + 1;
		c.inventory.resetItems(3214);
		c.overLoad = 5;
		int add = getBrewStat(0, 0.27);
		c.overloaded[0][1] = c.getLevelForXP(c.playerXP[0]) + add >= c.playerLevel[0] + add ? add : add - (c.playerLevel[0] - c.getLevelForXP(c.playerXP[0]));
		c.playerLevel[0] += c.overloaded[0][1];
		add = getBrewStat(1, 0.27);
		c.overloaded[1][1] = c.getLevelForXP(c.playerXP[1]) + add >= c.playerLevel[1] + add ? add : add - (c.playerLevel[1] - c.getLevelForXP(c.playerXP[1]));
		c.playerLevel[1] += c.overloaded[1][1];
		add = getBrewStat(2, 0.27);
		c.overloaded[2][1] = c.getLevelForXP(c.playerXP[2]) + add >= c.playerLevel[2] + add ? add : add - (c.playerLevel[2] - c.getLevelForXP(c.playerXP[2]));
		c.playerLevel[2] += c.overloaded[2][1];
		add = getBrewStat(4, 0.1923) + 4;
		c.overloaded[3][1] = c.getLevelForXP(c.playerXP[4]) + add >= c.playerLevel[4] + add ? add : add - (c.playerLevel[4] - c.getLevelForXP(c.playerXP[4]));
		c.playerLevel[4] += c.overloaded[3][1];
		add = 7;
		c.overloaded[4][1] = c.getLevelForXP(c.playerXP[6]) + add >= c.playerLevel[6] + add ? add : add - (c.playerLevel[6] - c.getLevelForXP(c.playerXP[6]));
		c.playerLevel[6] += c.overloaded[4][1];
		c.overloadedBool = true;
		c.overloadTime = Misc.currentTimeSeconds();
		c.saveGame();
		c.getPA().refreshSkill(0);
		c.getPA().refreshSkill(1);
		c.getPA().refreshSkill(2);
		c.getPA().refreshSkill(4);
		c.getPA().refreshSkill(6);
	}

	public void handlePotion(int itemId, int slot){
		if(c.inTrade || (c.duel != null && c.duel.status > 0 && c.duel.status < 3))
			return;
		if(c.duel != null && c.duel.status == 0)
			Duel.declineDuel(c, false);
		if(c.duel != null && c.duel.rules != null && c.duel.rules.duelRule[5] && c.duel.status == 3){
			c.sendMessage("You may not drink potions in this duel.");
			return;
		}
		if(System.currentTimeMillis() - c.potDelay >= 1500){
			c.potDelay = System.currentTimeMillis();
			c.foodDelay = c.potDelay;
			c.getCombat().resetPlayerAttack();
			c.attackTimer++;
			switch(itemId){
				case 15332:
					handleOverload(itemId, 15333, slot);
					break;
				case 15333:
					handleOverload(itemId, 15334, slot);
					break;
				case 15334:
					handleOverload(itemId, 15335, slot);
					break;
				case 15335:
					handleOverload(itemId, 229, slot);
					break;
				case 6685: // brews
					doTheBrew(itemId, 6687, slot);
					break;
				case 6687:
					doTheBrew(itemId, 6689, slot);
					break;
				case 6689:
					doTheBrew(itemId, 6691, slot);
					break;
				case 6691:
					doTheBrew(itemId, 229, slot);
					break;
				case 3040:
					drinkStatPotion(itemId, 3042, slot, 6, false);
					break;
				case 3042:
					drinkStatPotion(itemId, 3044, slot, 6, false);
					break;
				case 3044:
					drinkStatPotion(itemId, 3046, slot, 6, false);
					break;
				case 3046:
					drinkStatPotion(itemId, 229, slot, 6, false);
					break;
				case 2436:
					drinkStatPotion(itemId, 145, slot, 0, true); // sup attack
					break;
				case 145:
					drinkStatPotion(itemId, 147, slot, 0, true);
					break;
				case 147:
					drinkStatPotion(itemId, 149, slot, 0, true);
					break;
				case 149:
					drinkStatPotion(itemId, 229, slot, 0, true);
					break;
				case 2440:
					drinkStatPotion(itemId, 157, slot, 2, true); // sup str
					break;
				case 157:
					drinkStatPotion(itemId, 159, slot, 2, true);
					break;
				case 159:
					drinkStatPotion(itemId, 161, slot, 2, true);
					break;
				case 161:
					drinkStatPotion(itemId, 229, slot, 2, true);
					break;
				case 2444:
					drinkStatPotion(itemId, 169, slot, 4, false); // range pot
					break;
				case 169:
					drinkStatPotion(itemId, 171, slot, 4, false);
					break;
				case 171:
					drinkStatPotion(itemId, 173, slot, 4, false);
					break;
				case 173:
					drinkStatPotion(itemId, 229, slot, 4, false);
					break;
				case 2432:
					drinkStatPotion(itemId, 133, slot, 1, false); // def pot
					break;
				case 133:
					drinkStatPotion(itemId, 135, slot, 1, false);
					break;
				case 135:
					drinkStatPotion(itemId, 137, slot, 1, false);
					break;
				case 137:
					drinkStatPotion(itemId, 229, slot, 1, false);
					break;
				case 113:
					drinkStatPotion(itemId, 115, slot, 2, false); // str pot
					break;
				case 115:
					drinkStatPotion(itemId, 117, slot, 2, false);
					break;
				case 117:
					drinkStatPotion(itemId, 119, slot, 2, false);
					break;
				case 119:
					drinkStatPotion(itemId, 229, slot, 2, false);
					break;
				case 2428:
					drinkStatPotion(itemId, 121, slot, 0, false); // attack pot
					break;
				case 121:
					drinkStatPotion(itemId, 123, slot, 0, false);
					break;
				case 123:
					drinkStatPotion(itemId, 125, slot, 0, false);
					break;
				case 125:
					drinkStatPotion(itemId, 229, slot, 0, false);
					break;
				case 2442:
					drinkStatPotion(itemId, 163, slot, 1, true); // super def pot
					break;
				case 163:
					drinkStatPotion(itemId, 165, slot, 1, true);
					break;
				case 165:
					drinkStatPotion(itemId, 167, slot, 1, true);
					break;
				case 167:
					drinkStatPotion(itemId, 229, slot, 1, true);
					break;
				case 3024:
					drinkPrayerPot(itemId, 3026, slot, true); // sup restore
					break;
				case 3026:
					drinkPrayerPot(itemId, 3028, slot, true);
					break;
				case 3028:
					drinkPrayerPot(itemId, 3030, slot, true);
					break;
				case 3030:
					drinkPrayerPot(itemId, 229, slot, true);
					break;
				case 10925:
					drinkPrayerPot(itemId, 10927, slot, true); // sanfew serums
					curePoison(300000);
					break;
				case 10927:
					drinkPrayerPot(itemId, 10929, slot, true);
					curePoison(300000);
					break;
				case 10929:
					drinkPrayerPot(itemId, 10931, slot, true);
					curePoison(300000);
					break;
				case 10931:
					drinkPrayerPot(itemId, 229, slot, true);
					curePoison(300000);
					break;
				case 2434:
					drinkPrayerPot(itemId, 139, slot, false); // pray pot
					break;
				case 139:
					drinkPrayerPot(itemId, 141, slot, false);
					break;
				case 141:
					drinkPrayerPot(itemId, 143, slot, false);
					break;
				case 143:
					drinkPrayerPot(itemId, 229, slot, false);
					break;
				case 2446:
					drinkAntiPoison(itemId, 175, slot, 30000); // anti poisons
					break;
				case 175:
					drinkAntiPoison(itemId, 177, slot, 30000);
					break;
				case 177:
					drinkAntiPoison(itemId, 179, slot, 30000);
					break;
				case 179:
					drinkAntiPoison(itemId, 229, slot, 30000);
					break;
				case 2448:
					drinkAntiPoison(itemId, 181, slot, 300000); // anti poisons
					break;
				case 181:
					drinkAntiPoison(itemId, 183, slot, 300000);
					break;
				case 183:
					drinkAntiPoison(itemId, 185, slot, 300000);
					break;
				case 185:
					drinkAntiPoison(itemId, 229, slot, 300000);
					break;
				case 2452:
				case 2454:
				case 2456:
				case 2458:
					handleAntiFire(itemId, itemId == 2458 ? 229 : itemId + 2, slot);
					break;
				case 15304:
				case 15305:
				case 15306:
				case 15307:
					handleAntiFire(itemId, itemId == 15307 ? 229 : itemId + 1, slot);
					break;
			}
		}
	}

	public void drinkAntiPoison(int itemId, int replaceItem, int slot, long delay){
		if(c.inventory.items[slot] == null || c.inventory.items[slot].id != itemId + 1)
			return;
		c.startAnimation(829);
		c.inventory.items[slot].id = replaceItem + 1;
		c.inventory.resetItems(3214);
		curePoison(delay);
	}

	public void curePoison(long delay){
		c.poisonDamage = 0;
		c.poisonImmune = delay;
		c.lastPoisonSip = System.currentTimeMillis();
	}

	public void drinkStatPotion(int itemId, int replaceItem, int slot, int stat, boolean sup){
		if(c.inventory.items[slot] == null || c.inventory.items[slot].id != itemId + 1)
			return;
		c.startAnimation(829);
		c.inventory.items[slot].id = replaceItem + 1;
		c.inventory.resetItems(3214);
		enchanceStat(stat, sup);
	}

	public void drinkPrayerPot(int itemId, int replaceItem, int slot, boolean rest){
		if(c.inventory.items[slot] == null || c.inventory.items[slot].id != itemId + 1)
			return;
		c.startAnimation(829);
		c.inventory.items[slot].id = replaceItem + 1;
		c.inventory.resetItems(3214);
		c.playerLevel[5] += (c.getLevelForXP(c.playerXP[5]) * .33);
		if(rest)
			c.playerLevel[5] += 1;
		if(c.playerLevel[5] > c.getLevelForXP(c.playerXP[5]))
			c.playerLevel[5] = c.getLevelForXP(c.playerXP[5]);
		c.getPA().refreshSkill(5);
		if(rest)
			restoreStats();
	}

	public void restoreStats(){
		for(int j = 0; j <= 6; j++){
			if(j == 5 || j == 3)
				continue;
			if(c.playerLevel[j] < c.getLevelForXP(c.playerXP[j])){
				c.playerLevel[j] += (c.getLevelForXP(c.playerXP[j]) * .33);
				if(c.playerLevel[j] > c.getLevelForXP(c.playerXP[j])){
					c.playerLevel[j] = c.getLevelForXP(c.playerXP[j]);
				}
				c.getPA().refreshSkill(j);
				c.getPA().setSkillLevel(j, c.playerLevel[j], c.playerXP[j]);
			}
		}
	}

	public void doTheBrew(int itemId, int replaceItem, int slot){
		if(c.inventory.items[slot] == null || c.inventory.items[slot].id != itemId + 1)
			return;
		if(c.duel != null && c.duel.rules != null && c.duel.rules.duelRule[6] && c.duel.status == 3){
			c.sendMessage("You may not eat in this duel.");
			return;
		}
		c.startAnimation(829);
		c.inventory.items[slot].id = replaceItem + 1;
		c.inventory.resetItems(3214);
		int toDecrease[] = {0, 2, 4, 6};
		if(!c.overloadedBool){
			for(int tD : toDecrease){
				c.playerLevel[tD] -= getBrewStat(tD, .10);
				if(c.playerLevel[tD] < 0)
					c.playerLevel[tD] = 1;
				c.getPA().refreshSkill(tD);
				c.getPA().setSkillLevel(tD, c.playerLevel[tD], c.playerXP[tD]);
			}
			c.playerLevel[1] += getBrewStat(1, .20);
			if(c.overloaded[1][1] <= 0){
				if(c.playerLevel[1] > (c.getLevelForXP(c.playerXP[1]) * 1.2 + 1)){
					c.playerLevel[1] = (int)(c.getLevelForXP(c.playerXP[1]) * 1.2);
				}
				c.getPA().refreshSkill(1);
			}
		}
		c.playerLevel[3] += getBrewStat(3, .15);
		if(c.playerLevel[3] > ((c.getLevelForXP(c.playerXP[3]) + c.zarosModifier) * 1.17 + 1)){
			c.playerLevel[3] = (int)((c.getLevelForXP(c.playerXP[3]) + c.zarosModifier) * 1.17);
		}
		c.getPA().refreshSkill(3);
	}
	
	public void handleAntiFire(int itemId, int replaceItem, int slot){
		if(c.inventory.items[slot] == null || c.inventory.items[slot].id != itemId + 1)
			return;
		if(c.duel != null && c.duel.rules != null && c.duel.rules.duelRule[6] && c.duel.status == 3){
			c.sendMessage("You may not eat in this duel.");
			return;
		}
		c.startAnimation(829);
		c.inventory.items[slot].id = replaceItem + 1;
		c.inventory.resetItems(3214);
		c.antiFirePot = ((itemId == 2452 || itemId == 2454 || itemId == 2456 || itemId == 2458) ? 1 : 2);
		c.lastAntiFire = Misc.currentTimeSeconds();
		c.antiFireDelay = 360;
		c.antiFireWarning = true;
	}

	public void enchanceStat(int skillID, boolean sup){
		c.playerLevel[skillID] += getBoostedStat(skillID, sup);
		c.getPA().refreshSkill(skillID);
	}

	public int getBrewStat(int skill, double amount){
		return (int)(c.getLevelForXP(c.playerXP[skill]) * amount);
	}

	public int getBoostedStat(int skill, boolean sup){
		int increaseBy = 0;
		boolean overload = ((skill == 0 || skill == 1 || skill == 2 || skill == 4 || skill == 6) ? true : false);
		if(sup)
			increaseBy = (int)(c.getLevelForXP(c.playerXP[skill]) * .20);
		else
			increaseBy = (int)(c.getLevelForXP(c.playerXP[skill]) * .13) + 1;
		if(overload){
			if(c.overloadedBool)
				return 0;
			if(c.playerLevel[skill] + increaseBy > c.getLevelForXP(c.playerXP[skill]) + increaseBy + 1)
				return c.getLevelForXP(c.playerXP[skill]) + increaseBy - (c.playerLevel[skill]);
		}else if(c.playerLevel[skill] + increaseBy > c.getLevelForXP(c.playerXP[skill]) + increaseBy + 1)
			return c.getLevelForXP(c.playerXP[skill]) + increaseBy - c.playerLevel[skill];
		return increaseBy;
	}

	public boolean isPotion(int itemId){
		String name = c.getItems().getItemName(itemId);
		return name.contains("(4)") || name.contains("(3)") || name.contains("(2)") || name.contains("(1)");
	}
	
	public boolean isPotion2(String name){
		return name.contains("(4)") || name.contains("(3)") || name.contains("(2)") || name.contains("(1)");
	}
	
	public static boolean isSamePot(String name1, String name2){
		int length = name1.length() < name2.length() ? name1.length() : name2.length();
		for(int i = 0; i<length; i++){
			if(name1.charAt(i) == '(' || name2.charAt(i) == '(')
				return true;
			if(name1.charAt(i) != name2.charAt(i))
				return false;
		}
		return false; // Must not be a potion at all.
	}
	
	public static int getAmount(String name){
		for(int i = 0; i<name.length(); i++)
			if(name.charAt(i) == '(')
				return (((int)name.charAt(i + 1)) - 48);
		return 0; // Must not be a potion at all.
	}
	
	public static String getPotName(String name){
		for(int i = 0; i<name.length(); i++)
			if(name.charAt(i) == '(')
				return name.substring(0, i);
		return name;
	}
	
	public int getFullId(String name, int id){
		if(!potIds.containsKey(name))
			return -1;
		return potIds.get(name)[Item.itemIsNote[id] ? 1 : 0];
	}
}