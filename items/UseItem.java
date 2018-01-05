package server.model.items;

import java.text.DecimalFormat;
import java.util.Iterator;

import server.clip.region.Region;
import server.model.minigames.CastleWars;
import server.model.npcs.NPCHandler;
import server.model.players.ActionHandler;
import server.model.players.Client;
import server.model.players.skills.Firemaking;
import server.util.Misc;
import server.Config;

/**
 * 
 * @author Ryan / Lmctruck30
 * 
 */

public class UseItem{
	public static void ItemonObject(Client c, int objectID, int objectX, int objectY, int itemId){
		if(!ActionHandler.ignoreObjectCheck(objectID)){
			if(!Region.objectExists(objectID, objectX, objectY, c.heightLevel) && !ActionHandler.multiZObject(objectID))
				return;
			if(ActionHandler.multiZObject(objectID) && !Region.objectExists(objectID, objectX, objectY, 0))
				return;
		}
		if(!c.inventory.hasItem(itemId, 1))
			return;
		switch(objectID){
			case 2783:
				if(c.inventory.hasItem(2347))
					c.getSmithingInt().showSmithInterface(itemId);
				break;
			case 8151:
			case 8389:
				c.getFarming().checkItemOnObject(itemId, objectX, objectY);
				break;
			case 2728:
			case 2732:
			case 12269:
				c.getCooking().itemOnObject(itemId, objectID);
				break;
			case 409:
				if(c.getPrayer().isBone(itemId))
					c.getPrayer().bonesOnAltar(itemId);
				break;
			case 7:
			case 8:
			case 9:
				c.getCannon().itemOnObject(itemId, objectID, objectX, objectY);
				break;
			default:
				if(c.playerRights == 3)
					System.out.println("Player At Object id: " + objectID + " with Item id: " + itemId);
				break;
		}

	}

	public static void ItemonItem(Client c, int itemUsed, int useWith){
		// Firemaking
		if(itemUsed == 590 || useWith == 590){
			Firemaking fm = new Firemaking(c);
			if(itemUsed == 590 && fm.isLog(useWith)){
				int slot = fm.getSlot(useWith);
				if(slot > -1)
					fm.lightFire(slot);
			}else{
				int slot = fm.getSlot(itemUsed);
				if(slot > -1)
					fm.lightFire(slot);
			}
		}
		if((itemUsed == 4621 && useWith == 2454) || (itemUsed == 2454 && useWith == 4621))
			c.getHerblore().superAntiFire();
		if(itemUsed == 227 || useWith == 227)
			c.getHerblore().handlePotMaking(itemUsed, useWith);
		if(c.getItems().getItemName(itemUsed).contains("(") && c.getItems().getItemName(useWith).contains("(") && !Item.itemIsNote[itemUsed] && !Item.itemIsNote[itemUsed] && !c.getItems().getItemName(useWith).toLowerCase().contains("ring") && !c.getItems().getItemName(itemUsed).toLowerCase().contains("ring"))
			c.getPotMixing().mixPotion2(itemUsed, useWith);
		if(itemUsed == 1733 || useWith == 1733)
			c.getCrafting().handleLeather(itemUsed, useWith);
		if(itemUsed == 1755 || useWith == 1755)
			c.getCrafting().handleChisel(itemUsed, useWith);
		if(itemUsed == 946 || useWith == 946)
			c.getFletching().handleLog(itemUsed, useWith);
		if(itemUsed == 53 || useWith == 53 || itemUsed == 52 || useWith == 52)
			c.getFletching().makeArrows(itemUsed, useWith);
		if((itemUsed == 1540 && useWith == 11286) || (itemUsed == 11286 && useWith == 1540)){
			if(c.playerLevel[c.playerSmithing] >= 95){
				c.inventory.deleteItem(1540, c.inventory.findItemSlot(1540), 1);
				c.inventory.deleteItem(11286, c.inventory.findItemSlot(11286), 1);
				c.inventory.addItem(11284, 1, -1);
				c.sendMessage("You combine the two materials to create a dragonfire shield.");
				c.getPA().addSkillXP(500 * Config.SMITHING_EXPERIENCE, c.playerSmithing);
			}else{
				c.sendMessage("You need a smithing level of 95 to create a dragonfire shield.");
			}
		}
		if(itemUsed == 9142 && useWith == 9190 || itemUsed == 9190 && useWith == 9142){
			if(c.playerLevel[c.playerFletching] >= 58){
				int boltsMade = c.inventory.getItemCount(itemUsed) > c.inventory.getItemCount(useWith) ? c.inventory.getItemCount(useWith) : c.inventory.getItemCount(itemUsed);
				c.inventory.deleteItem(useWith, c.inventory.findItemSlot(useWith), boltsMade);
				c.inventory.deleteItem(itemUsed, c.inventory.findItemSlot(itemUsed), boltsMade);
				c.inventory.addItem(9241, boltsMade, -1);
				c.getPA().addSkillXP(boltsMade * 6 * Config.FLETCHING_EXPERIENCE, c.playerFletching);
			}else{
				c.sendMessage("You need a fletching level of 58 to fletch this item.");
			}
		}
		if(itemUsed == 9143 && useWith == 9191 || itemUsed == 9191 && useWith == 9143){
			if(c.playerLevel[c.playerFletching] >= 63){
				int boltsMade = c.inventory.getItemCount(itemUsed) > c.inventory.getItemCount(useWith) ? c.inventory.getItemCount(useWith) : c.inventory.getItemCount(itemUsed);
				c.inventory.deleteItem(useWith, c.inventory.findItemSlot(useWith), boltsMade);
				c.inventory.deleteItem(itemUsed, c.inventory.findItemSlot(itemUsed), boltsMade);
				c.inventory.addItem(9242, boltsMade, -1);
				c.getPA().addSkillXP(boltsMade * 7 * Config.FLETCHING_EXPERIENCE, c.playerFletching);
			}else{
				c.sendMessage("You need a fletching level of 63 to fletch this item.");
			}
		}
		if(itemUsed == 9143 && useWith == 9192 || itemUsed == 9192 && useWith == 9143){
			if(c.playerLevel[c.playerFletching] >= 65){
				int boltsMade = c.inventory.getItemCount(itemUsed) > c.inventory.getItemCount(useWith) ? c.inventory.getItemCount(useWith) : c.inventory.getItemCount(itemUsed);
				c.inventory.deleteItem(useWith, c.inventory.findItemSlot(useWith), boltsMade);
				c.inventory.deleteItem(itemUsed, c.inventory.findItemSlot(itemUsed), boltsMade);
				c.inventory.addItem(9243, boltsMade, -1);
				c.getPA().addSkillXP(boltsMade * 7 * Config.FLETCHING_EXPERIENCE, c.playerFletching);
			}else{
				c.sendMessage("You need a fletching level of 65 to fletch this item.");
			}
		}
		if(itemUsed == 9144 && useWith == 9193 || itemUsed == 9193 && useWith == 9144){
			if(c.playerLevel[c.playerFletching] >= 71){
				int boltsMade = c.inventory.getItemCount(itemUsed) > c.inventory.getItemCount(useWith) ? c.inventory.getItemCount(useWith) : c.inventory.getItemCount(itemUsed);
				c.inventory.deleteItem(useWith, c.inventory.findItemSlot(useWith), boltsMade);
				c.inventory.deleteItem(itemUsed, c.inventory.findItemSlot(itemUsed), boltsMade);
				c.inventory.addItem(9244, boltsMade, -1);
				c.getPA().addSkillXP(boltsMade * 10 * Config.FLETCHING_EXPERIENCE, c.playerFletching);
			}else{
				c.sendMessage("You need a fletching level of 71 to fletch this item.");
			}
		}
		if(itemUsed == 9144 && useWith == 9194 || itemUsed == 9194 && useWith == 9144){
			if(c.playerLevel[c.playerFletching] >= 58){
				int boltsMade = c.inventory.getItemCount(itemUsed) > c.inventory.getItemCount(useWith) ? c.inventory.getItemCount(useWith) : c.inventory.getItemCount(itemUsed);
				c.inventory.deleteItem(useWith, c.inventory.findItemSlot(useWith), boltsMade);
				c.inventory.deleteItem(itemUsed, c.inventory.findItemSlot(itemUsed), boltsMade);
				c.inventory.addItem(9245, boltsMade, -1);
				c.getPA().addSkillXP(boltsMade * 13 * Config.FLETCHING_EXPERIENCE, c.playerFletching);
			}else{
				c.sendMessage("You need a fletching level of 58 to fletch this item.");
			}
		}
		if(itemUsed == 1601 && useWith == 1755 || itemUsed == 1755 && useWith == 1601){
			if(c.playerLevel[c.playerFletching] >= 63){
				c.inventory.deleteItem(1601, c.inventory.findItemSlot(1601), 1);
				c.inventory.addItem(9192, 15, -1);
				c.getPA().addSkillXP(8 * Config.FLETCHING_EXPERIENCE, c.playerFletching);
			}else{
				c.sendMessage("You need a fletching level of 63 to fletch this item.");
			}
		}
		if(itemUsed == 1607 && useWith == 1755 || itemUsed == 1755 && useWith == 1607){
			if(c.playerLevel[c.playerFletching] >= 65){
				c.inventory.deleteItem(1607, c.inventory.findItemSlot(1607), 1);
				c.inventory.addItem(9189, 15, -1);
				c.getPA().addSkillXP(8 * Config.FLETCHING_EXPERIENCE, c.playerFletching);
			}else{
				c.sendMessage("You need a fletching level of 65 to fletch this item.");
			}
		}
		if(itemUsed == 1605 && useWith == 1755 || itemUsed == 1755 && useWith == 1605){
			if(c.playerLevel[c.playerFletching] >= 71){
				c.inventory.deleteItem(1605, c.inventory.findItemSlot(1605), 1);
				c.inventory.addItem(9190, 15, -1);
				c.getPA().addSkillXP(8 * Config.FLETCHING_EXPERIENCE, c.playerFletching);
			}else{
				c.sendMessage("You need a fletching level of 71 to fletch this item.");
			}
		}
		if(itemUsed == 1603 && useWith == 1755 || itemUsed == 1755 && useWith == 1603){
			if(c.playerLevel[c.playerFletching] >= 73){
				c.inventory.deleteItem(1603, c.inventory.findItemSlot(1603), 1);
				c.inventory.addItem(9191, 15, -1);
				c.getPA().addSkillXP(8 * Config.FLETCHING_EXPERIENCE, c.playerFletching);
			}else{
				c.sendMessage("You need a fletching level of 73 to fletch this item.");
			}
		}
		if(itemUsed == 1615 && useWith == 1755 || itemUsed == 1755 && useWith == 1615){
			if(c.playerLevel[c.playerFletching] >= 73){
				c.inventory.deleteItem(1615, c.inventory.findItemSlot(1615), 1);
				c.inventory.addItem(9193, 15, -1);
				c.getPA().addSkillXP(8 * Config.FLETCHING_EXPERIENCE, c.playerFletching);
			}else{
				c.sendMessage("You need a fletching level of 73 to fletch this item.");
			}
		}
		if(itemUsed >= 11710 && itemUsed <= 11714 && useWith >= 11710 && useWith <= 11714){
			if(c.getItems().hasAllShards()){
				c.getItems().makeBlade();
			}
		}
		if(itemUsed == 2368 && useWith == 2366 || itemUsed == 2366 && useWith == 2368){
			c.inventory.deleteItem(2368, c.inventory.findItemSlot(2368), 1);
			c.inventory.deleteItem(2366, c.inventory.findItemSlot(2366), 1);
			c.inventory.addItem(1187, 1, -1);
		}

		if(c.getItems().isHilt(itemUsed) || c.getItems().isHilt(useWith)){
			int hilt = c.getItems().isHilt(itemUsed) ? itemUsed : useWith;
			int blade = c.getItems().isHilt(itemUsed) ? useWith : itemUsed;
			if(blade == 11690){
				c.getItems().makeGodsword(hilt);
			}
		}
		if(c.playerRights == 3)
			System.out.println("Player used Item id: " + itemUsed + " with Item id: " + useWith);
	}

	public static void ItemonNpc(Client c, int itemId, int npcType, int slot, int npcId){
		DecimalFormat df = new DecimalFormat("#,###");
		if(c.playerRights == 3)
			System.out.println("Player used Item id: " + itemId + " with Npc id: " + npcType + " With Slot : " + slot);
		switch(itemId){
			case 4045:
				if(npcType != 1532)
					break;
				c.inventory.deleteItem(itemId, 1);
				NPCHandler.npcs[npcId].gfx0(547);
				NPCHandler.npcs[npcId].isDead = true;
				break;
			case 20137:
			case 20141:
			case 20145:
			case 20149:
			case 20153:
			case 20157:
			case 20161:
			case 20165:
			case 20169:
			case 20173:
			case 20174:
				if(c.inventory.items[slot] == null)
					return;
				if(c.inventory.items[slot].id - 1 != itemId || !Degrade.isDegradedItem(itemId))
					return;
				int amt = 0;
				int deg = c.inventory.items[slot].degrade <= 0 ? 1 : c.inventory.items[slot].degrade;
				int max = Degrade.getDegradeItem(itemId).getDegradeTime();
				double perc = (double)deg / (double)max;
				if(npcType == 536){
					switch(itemId){
						case 20137:
						case 20149:
						case 20161:
							amt = (int)Math.ceil(450000000 * perc);
							break;
						case 20141:
						case 20153:
						case 20165:
							amt = (int)Math.ceil(1000000000 * perc);
							break;
						case 20145:
						case 20157:
						case 20169:
							amt = (int)Math.ceil(700000000 * perc);
							break;
						case 20173:
							amt = (int)Math.ceil(2000000000 * perc);
							break;
						case 20174:
							amt = 2000000000;
							break;
					}
					if(c.inventory.hasItem(995, amt)){
						if(c.inventory.deleteItem(itemId, slot, 1)){
							c.inventory.addItem(itemId - 2, 1, -1);
							c.inventory.deleteItem(995, amt);
						}else
							break;
						c.sendMessage("Your item has been repaired");
						break;
					}
					c.sendMessage("You do not have enough coins to repair that item.");
					c.sendMessage("You need " + df.format(amt) + " coins.");
				}
				break;
			case 20138:
			case 20142:
			case 20146:
			case 20150:
			case 20154:
			case 20158:
			case 20162:
			case 20166:
			case 20170:
				if(c.inventory.items[slot] != null && c.inventory.items[slot].id - 1 != itemId)
					return;
				int amtf = 0;
				if(npcType == 536){
					switch(itemId){
						case 20138:
						case 20150:
						case 20162:
							amtf = 450000000;
							break;
						case 20142:
						case 20154:
						case 20166:
							amtf = 1000000000;
							break;
						case 20146:
						case 20158:
						case 20170:
							amtf = 700000000;
							break;
					}
					if(c.inventory.hasItem(995, amtf)){
						if(c.inventory.deleteItem(itemId, slot, 1)){
							c.inventory.addItem(itemId - 3, 1, -1);
							c.inventory.deleteItem(995, amtf);
						}else
							break;
						c.sendMessage("Your item has been repaired");
						break;
					}
					c.sendMessage("You do not have enough coins to repair that item.");
					c.sendMessage("You need " + df.format(amtf) + " coins.");
				}
				break;
			case 590:
				if(npcType != 1532)
					break;
				if(CastleWars.isInCw(c)){
					Iterator<Client> iterator = CastleWars.gameRoom.keySet().iterator();
					while(iterator.hasNext()){
						Client person = iterator.next();
						if(person != null){
							if(person.heightLevel == NPCHandler.npcs[npcId].heightLevel){
								if(person.distanceToPoint(NPCHandler.npcs[npcId].absX, NPCHandler.npcs[npcId].absY) <= 60){
									person.getPA().checkObjectSpawn(4422, NPCHandler.npcs[npcId].absX, NPCHandler.npcs[npcId].absY, 0, 10);
								}
							}
						}
					}
				}
				NPCHandler.npcs[npcId].absX = 20;
				NPCHandler.npcs[npcId].absY = 20;
				NPCHandler.npcs[npcId].updateRequired = true;
				CastleWars.burnCade(npcId);
				break;
			default:
				break;
		}
	}

	public static void ItemonPlayer(Client c, int itemId, Client c2, int slot){
		if(c == null || c2 == null)
			return;
		if(itemId == 4049 && CastleWars.isInCw(c) && CastleWars.isInCw(c2)){
			if(System.currentTimeMillis() - c.foodDelay >= 1500 && c2.playerLevel[3] > 0){
				c.foodDelay = System.currentTimeMillis();
				c.getCombat().resetPlayerAttack();
				c.attackTimer += 2;
				c.inventory.deleteItem(itemId, 1);
				int toHeal = Math.round(c2.getLevelForXP(c2.playerXP[3]) / 10);
				if(c2.playerLevel[3] < c2.getLevelForXP(c2.playerXP[3])){
					c2.playerLevel[3] += toHeal;
					if(c2.playerLevel[3] > c2.getLevelForXP(c2.playerXP[3]))
						c2.playerLevel[3] = c2.getLevelForXP(c2.playerXP[3]);
				}
				c2.getPA().refreshSkill(3);
			}
		}else if(c.getFood().isFood(itemId) && c.inNexGame && c2.inNexGame)
			c.getFood().eat(itemId, c2);
		else if(itemId == Config.CHRISTMAS_CRACKER && c.inventory.hasItem(Config.CHRISTMAS_CRACKER)){
			c.inventory.deleteItem(Config.CHRISTMAS_CRACKER, 1);
			c.saveGame();
			int user_items[][] = {{1037, 1}, {1053, 1}, {1055, 1}, {1057, 1}, {1038, 1}, {11700, 1}, {1040, 1}, {11696, 1}, {1042, 1}, {11694, 1}, {1044, 1}, {11698, 1}, {1046, 1}, {11724, 1}, {1048, 1}, {11726, 1}};
			int other_items[][] = {{995, 1000000}, {4151, 1}, {10551, 1}, {7462, 1}, {6737, 1}, {6585, 1}, {11728, 1}};
			int random = Misc.random(user_items.length - 1);
			int random2 = Misc.random(other_items.length - 1);
			c.inventory.addItem(user_items[random][0], user_items[random][1], -1);
			c.saveGame();
			c2.inventory.addItem(other_items[random2][0], other_items[random2][1], -1);
			c2.saveGame();
		}else
			c.sendMessage("Nothing interesting happens.");
	}
}
