package server.model.items;

import java.util.Arrays;
import server.Config;
import server.Server;
import server.model.items.bank.BankItem;
import server.model.items.bank.Tab;
import server.model.minigames.CastleWars;
import server.model.npcs.NPCHandler;
import server.model.players.Client;
import server.model.players.PlayerHandler;
import server.model.players.PlayerSave;
import server.util.Misc;

public class ItemAssistant{

	private Client c;

	public ItemAssistant(Client client){
		this.c = client;
	}

	/**
	 * Items
	 **/
	public int brokenBarrows[][] = {{4708, 4860}, {4710, 4866}, {4712, 4872}, {4714, 4878}, {4716, 4884}, {4720, 4896}, {4718, 4890}, {4720, 4896}, {4722, 4902}, {4732, 4932}, {4734, 4938}, {4736, 4944}, {4738, 4950}, {4724, 4908}, {4726, 4914}, {4728, 4920}, {4730, 4926}, {4745, 4956}, {4747, 4962}, {4749, 4968}, {4751, 4974}, {4753, 4980}, {4755, 4986}, {4757, 4992}, {4759, 4998}};

	public void writeBonus(){
		String send = "";
		for(int i = 0; i < c.playerBonus.length; i++){
			send = BONUS_NAMES[i] + ": " + (c.playerBonus[i] < 0 ? "" : "+") + c.playerBonus[i];
			c.getPA().sendText(send, (1675 + i + (i >= 10 ? 1 : 0)));
		}
	}

	public int getTotalCount(int itemID){
		return c.bank.getBankAmount(itemID) + c.inventory.getItemCount(itemID);
	}

	public boolean hasQuincyBow(){
		return hasEquipment(Config.QUINCY_BOW);
	}

	public boolean hasEquipment(){
		for(int i = 0; i < c.playerEquipment.length; i++)
			if(c.playerEquipment[i] > 0)
				return true;
		return false;
	}

	public boolean hasEquipment(int itemID){
		for(int i = 0; i < c.playerEquipment.length; i++)
			if(c.playerEquipment[i] == itemID)
				return true;
		return false;
	}

	/**
	 * Item kept on death
	 **/
	public boolean isPKPItem(int itemID){
		int pkpItem[] = {18346, 18349, 18351, 18353, 18355, 18357, 18359, 18361, 21462, 21463, 21464, 21465, 21466, 21467, 21468, 21469, 21470, 21471, 21472, 21473, 21474, 21475, 21476, 18363, 20667, 20668};
		for(int id : pkpItem)
			if(id == itemID)
				return true;
		return false;
	}

	public boolean isPCPItem(int itemID){
		int pcpItem[] = {19785, 19786, 19787, 19788, 19789, 19790};
		for(int id : pcpItem)
			if(id == itemID)
				return true;
		return false;
	}

	public boolean isCWItem(int itemID){
		int cwItem[] = {2577, 2581, 20098, 20099, 15259};
		for(int id : cwItem)
			if(id == itemID)
				return true;
		return false;
	}

	public boolean isZombiesItem(int itemID){
		int zombieItems[] = {1837, 1838, 9005, 3486, 3487, 3481, 3482, 3483, 3484, 3485, 3488, 3489, 13672};
		for(int id : zombieItems)
			if(id == itemID)
				return true;
		return false;
	}

	public void keepItem(int keepItem, boolean deleteItem){
		int value = 0;
		int item = 0;
		int slotId = 0;
		boolean itemInInventory = false;
		for(int i = 0; i < c.inventory.items.length; i++){
			if(c.inventory.items[i] != null){
				int mult = 1;
				if(isPKPItem(c.inventory.items[i].id - 1))
					mult = 1000000;
				else if(isCWItem(c.inventory.items[i].id - 1))
					mult = 750000;
				else if(isPCPItem(c.inventory.items[i].id - 1))
					mult = 500000;
				else if(isZombiesItem(c.inventory.items[i].id - 1))
					mult = 250000;
				int inventoryItemValue = c.getShops().getItemShopValue(c.inventory.items[i].id - 1);
				if((inventoryItemValue * mult) > value){
					value = (inventoryItemValue * mult);
					item = c.inventory.items[i].id - 1;
					slotId = i;
					itemInInventory = true;
				}
			}
		}
		for(int i1 = 0; i1 < c.playerEquipment.length; i1++){
			if(c.playerEquipment[i1] > 0){
				int mult = 1;
				if(isPKPItem(c.playerEquipment[i1]))
					mult = 1000000;
				else if(isCWItem(c.playerEquipment[i1]))
					mult = 750000;
				else if(isPCPItem(c.playerEquipment[i1]))
					mult = 500000;
				else if(isZombiesItem(c.playerEquipment[i1]))
					mult = 250000;
				int equipmentItemValue = c.getShops().getItemShopValue(c.playerEquipment[i1]);
				if((equipmentItemValue * mult) > value){
					value = (equipmentItemValue * mult);
					item = c.playerEquipment[i1];
					slotId = i1;
					itemInInventory = false;
				}
			}
		}
		if(itemInInventory){
			if(deleteItem)
				c.inventory.deleteItem(c.inventory.items[slotId].id - 1, slotId, 1);
		}else{
			if(deleteItem)
				deleteEquipment(item, slotId, 1);
		}
		c.itemKeptId[keepItem] = item;
	}

	/**
	 * Reset items kept on death
	 **/

	public void resetKeepItems(){
		for(int i = 0; i < c.itemKeptId.length; i++)
			c.itemKeptId[i] = -1;
	}

	/**
	 * delete all items
	 **/
	public void deleteAllItems(){
		for(int i1 = 0; i1 < c.playerEquipment.length; i1++)
			deleteEquipment(c.playerEquipment[i1], i1);
		c.inventory.deleteAllItems();
		PlayerSave.saveGame(c);
		c.inventory.resetItems(3214);
	}

	private boolean inKeptArray(int itemId){
		for(int item : c.itemKeptId)
			if(item == itemId)
				return true;
		return false;
	}

	/**
	 * Drop all items for your killer
	 **/

	public boolean isDroppable(int itemID){
		for(int i : Config.UNDROPPABLE_ITEMS)
			if(i == itemID || c.isSkillCapeItem(itemID))
				return false;
		return true;
	}

	public void loseItems(){
		c.lostItems = c.inventory.items.clone();
		c.lostEquip[0] = c.playerEquipment.clone();
		c.lostEquip[1] = c.playerEquipmentN.clone();
	}

	public void dropAllLostItems(){
		if((c.playerRights >= 2 && c.playerRights != 5 && Config.ADMIN_DROP_ITEMS) || c.playerRights == 5 || c.playerRights < 2){
			Client o = (Client)PlayerHandler.players[c.killerId];
			for(int i = 0; i < c.lostItems.length; i++){
				if(c.lostItems[i] == null)
					continue;
				if(inKeptArray(c.lostItems[i].id - 1) || !isDroppable(c.lostItems[i].id - 1))
					continue;
				if(o != null){
					if(c.lostItems[i].id - 1 == 4202 || c.lostItems[i].id - 1 == 6465 || inKeptArray(c.lostItems[i].id - 1))
						continue;
					if(c.lostItems[i].id - 1 == 11283 || isZarosDroppedHead(c.lostItems[i].id - 1) || isZarosDroppedBody(c.lostItems[i].id - 1) || isZarosDroppedLegs(c.lostItems[i].id - 1))
						c.lostItems[i].id++;
					if(tradeable(c.lostItems[i].id - 1))
						Server.itemHandler.createGroundItem(o, c.lostItems[i].id - 1, c.deadX, c.deadY, c.deadZ, c.lostItems[i].amount, c.killerId);
					else{
						if(specialCase(c.lostItems[i].id - 1))
							Server.itemHandler.createGroundItem(o, 995, c.deadX, c.deadY, c.deadZ, getUntradePrice(c.lostItems[i].id - 1), c.killerId);
						Server.itemHandler.createGroundItem(c, c.lostItems[i].id - 1, c.deadX, c.deadY, c.deadZ, c.lostItems[i].amount, c.playerId);
					}
				}else{
					if(c.lostItems[i].id - 1 == 11283 || isZarosDroppedHead(c.lostItems[i].id - 1) || isZarosDroppedBody(c.lostItems[i].id - 1) || isZarosDroppedLegs(c.lostItems[i].id - 1))
						c.lostItems[i].id++;
					Server.itemHandler.createGroundItem(c, c.lostItems[i].id - 1, c.deadX, c.deadY, c.deadZ, c.lostItems[i].amount, c.playerId);
				}
			}
			for(int e = 0; e < c.lostEquip[0].length; e++){
				if(inKeptArray(c.lostEquip[0][e]) || !isDroppable(c.lostEquip[0][e]))
					continue;
				if(o != null){
					if(c.lostEquip[0][e] == 4202 || c.lostEquip[0][e] == 6465)
						return;
					if(c.lostEquip[0][e] == 11283 || isZarosDroppedHead(c.lostEquip[0][e]) || isZarosDroppedBody(c.lostEquip[0][e]) || isZarosDroppedLegs(c.lostEquip[0][e]))
						c.lostEquip[0][e]++;
					if(tradeable(c.lostEquip[0][e])){
						Server.itemHandler.createGroundItem(o, c.lostEquip[0][e], c.deadX, c.deadY, c.deadZ, c.lostEquip[1][e], c.killerId);
					}else{
						if(specialCase(c.lostEquip[0][e]))
							Server.itemHandler.createGroundItem(o, 995, c.deadX, c.deadY, c.deadZ, getUntradePrice(c.lostEquip[0][e]), c.killerId);
						Server.itemHandler.createGroundItem(c, c.lostEquip[0][e], c.deadX, c.deadY, c.deadZ, c.lostEquip[1][e], c.playerId);
					}
				}else{
					if(c.lostEquip[0][e] == 11283 || isZarosDroppedHead(c.lostEquip[0][e]) || isZarosDroppedBody(c.lostEquip[0][e]) || isZarosDroppedLegs(c.lostEquip[0][e]))
						c.lostEquip[0][e]++;
					Server.itemHandler.createGroundItem(c, c.lostEquip[0][e], c.deadX, c.deadY, c.deadZ, c.lostEquip[1][e], c.playerId);
				}
			}
			if(o != null){
				addPVP();
				Server.itemHandler.createGroundItem(o, 526, c.deadX, c.deadY, c.deadZ, 1, c.killerId);
			}
		}
		Arrays.fill(c.lostItems, null);
		Arrays.fill(c.lostEquip[1], -1);
		Arrays.fill(c.lostEquip[1], -1);
	}

	public boolean inPvp(Client o){
		return o.isInArd() || o.inWild();
	}

	public void addArdPVP(Client o){
		if((Misc.random(99) + 1) < 45 || o.connectedFrom.equalsIgnoreCase(c.connectedFrom) || o.lastKillIP.equalsIgnoreCase(c.connectedFrom))
			return;
		int kats[] = {20085, 20086, 20087, 20088};
		int kat = kats[Misc.random(kats.length - 1)];
		Server.itemHandler.createGroundItem(o, kat, c.deadX, c.deadY, c.deadZ, 1, c.killerId);
	}

	/**
	 * Drop all items for your killer in PVP
	 **/
	public void addPVP(){
		if(c.killerId < 0)
			return;
		Client o = (Client)PlayerHandler.players[c.killerId];
		if(o == null)
			return;
		if(inPvp(o) && !o.isInFala()){
			addArdPVP(o);
			return;
		}
		if(!o.isInFala() || o.connectedFrom.equalsIgnoreCase(c.connectedFrom) || o.lastKillIP.equalsIgnoreCase(c.connectedFrom))
			return;
		int posKats[][] = {{20096, 20095, 20094, 20093, 20101}, {20089, 20090, 20091, 20092}, {20085, 20086, 20087, 20088}};
		int kats[];
		long riskedWealth = 0;
		for(int i = 0; i < o.playerEquipment.length; i++)
			if(o.playerEquipment[i] > 0)
				riskedWealth += (o.getShops().getItemShopValue(o.playerEquipment[i]) * o.playerEquipmentN[i]);
		for(int i = 0; i < o.inventory.items.length; i++)
			if(o.inventory.items[i] != null)
				riskedWealth += (o.getShops().getItemShopValue(o.inventory.items[i].id - 1) * o.inventory.items[i].amount);
		riskedWealth /= 4;
		int length = 0, len = 0;
		if(riskedWealth >= 200000000)
			length = 3;
		else if(riskedWealth >= 125000000)
			length = 2;
		else if(riskedWealth >= 25000000)
			length = 1;
		else
			return;
		for(int i = 0; i < length; i++)
			len += posKats[i].length;
		kats = new int[len];
		int k = 0;
		for(int i = 0; i < length; i++)
			for(int j : posKats[i])
				kats[k++] = j;
		int space = Misc.random(kats.length - 1);
		int item = kats[space];
		Server.itemHandler.createGroundItem(o, item, c.deadX, c.deadY, c.deadZ, 1, c.killerId);
	}

	public int getUntradePrice(int item){
		switch(item){
			case 2518:
			case 2524:
			case 2526:
				return 100000;
			case 2520:
			case 2522:
				return 150000;
		}
		return 0;
	}

	public boolean specialCase(int itemId){
		switch(itemId){
			case 2518:
			case 2520:
			case 2522:
			case 2524:
			case 2526:
				return true;
		}
		return false;
	}

	public void handleSpecialPickup(int itemId){
		// c.sendMessage("My " + getItemName(itemId) +
		// " has been recovered. I should talk to the void knights to get it back.");
		// c.getItems().addToVoidList(itemId);
	}

	public void addToVoidList(int itemId){
		switch(itemId){
			case 2518:
				c.voidStatus[0]++;
				break;
			case 2520:
				c.voidStatus[1]++;
				break;
			case 2522:
				c.voidStatus[2]++;
				break;
			case 2524:
				c.voidStatus[3]++;
				break;
			case 2526:
				c.voidStatus[4]++;
				break;
		}
	}

	public boolean tradeable(int itemId){
		for(int j = 0; j < Config.ITEM_TRADEABLE.length; j++)
			if(itemId == Config.ITEM_TRADEABLE[j])
				return false;
		return true;
	}

	/**
	 * Add Item
	 **/

	public int itemType(int item){
		for(int i = 0; i < Item.capes.length; i++)
			if(item == Item.capes[i])
				return 1;
		for(int i = 0; i < Item.hats.length; i++)
			if(item == Item.hats[i])
				return 0;
		for(int i = 0; i < Item.boots.length; i++)
			if(item == Item.boots[i])
				return 10;
		for(int i = 0; i < Item.gloves.length; i++)
			if(item == Item.gloves[i])
				return 9;
		for(int i = 0; i < Item.shields.length; i++)
			if(item == Item.shields[i])
				return 5;
		for(int i = 0; i < Item.amulets.length; i++)
			if(item == Item.amulets[i])
				return 2;
		for(int i = 0; i < Item.arrows.length; i++)
			if(item == Item.arrows[i])
				return 13;
		for(int i = 0; i < Item.rings.length; i++)
			if(item == Item.rings[i])
				return 12;
		for(int i = 0; i < Item.body.length; i++)
			if(item == Item.body[i])
				return 4;
		for(int i = 0; i < Item.legs.length; i++)
			if(item == Item.legs[i])
				return 7;
		return -1;
	}

	/**
	 * Bonuses
	 **/

	public final String[] BONUS_NAMES = {"Stab", "Slash", "Crush", "Magic", "Range", "Stab", "Slash", "Crush", "Magic", "Range", "Strength", "Prayer"};

	public void resetBonus(){
		for(int i = 0; i < c.playerBonus.length; i++){
			c.playerBonus[i] = 0;
		}
	}

	public void getBonus(){
		for(int i = 0; i < c.playerEquipment.length; i++){
			if(c.playerEquipment[i] > -1){
				for(int j = 0; j < Config.ITEM_LIMIT; j++){
					if(Server.itemHandler.ItemList[j] != null){
						if(Server.itemHandler.ItemList[j].itemId == c.playerEquipment[i]){
							for(int k = 0; k < c.playerBonus.length; k++){
								if(k >= 5 && c.playerEquipment[i] == 11283 && k <= 9)
									c.playerBonus[k] += Server.itemHandler.ItemList[j].Bonuses[k] + c.dfsCharges;
								else
									c.playerBonus[k] += Server.itemHandler.ItemList[j].Bonuses[k];
							}
							break;
						}
					}
				}
			}
		}
	}

	/**
	 * Wear Item
	 **/

	public void sendWeapon(int Weapon, String WeaponName){
		String WeaponName2 = WeaponName.replaceAll("Bronze", "");
		WeaponName2 = WeaponName2.replaceAll("Iron", "");
		WeaponName2 = WeaponName2.replaceAll("Steel", "");
		WeaponName2 = WeaponName2.replaceAll("Black", "");
		WeaponName2 = WeaponName2.replaceAll("Mithril", "");
		WeaponName2 = WeaponName2.replaceAll("Adamant", "");
		WeaponName2 = WeaponName2.replaceAll("Rune", "");
		WeaponName2 = WeaponName2.replaceAll("Granite", "");
		WeaponName2 = WeaponName2.replaceAll("Dragon", "");
		WeaponName2 = WeaponName2.replaceAll("Drag", "");
		WeaponName2 = WeaponName2.replaceAll("Crystal", "");
		WeaponName2 = WeaponName2.trim();
		if(WeaponName.equals("Unarmed")){
			c.setSidebarInterface(0, 5855); // punch, kick, block
			c.getPA().sendText(WeaponName, 5857);
		}else if(WeaponName.endsWith("whip")){
			c.setSidebarInterface(0, 12290); // flick, lash, deflect
			c.getPA().sendFrame246(12291, 200, Weapon);
			c.getPA().sendText(WeaponName, 12293);
		}else if(c.playerEquipment[c.playerWeapon] == 20097 || WeaponName.endsWith("bow") || WeaponName.endsWith("10") || WeaponName.endsWith("full") || WeaponName.startsWith("seercull")){
			c.setSidebarInterface(0, 1764); // accurate, rapid, longrange
			c.getPA().sendFrame246(1765, 200, Weapon);
			c.getPA().sendText(WeaponName, 1767);
		}else if(WeaponName.startsWith("Staff") || WeaponName.endsWith("staff") || WeaponName.endsWith("wand") || WeaponName.contains("staff")){
			c.setSidebarInterface(0, 328); // spike, impale, smash, block
			c.getPA().sendFrame246(329, 200, Weapon);
			c.getPA().sendText(WeaponName, 331);
		}else if(WeaponName2.startsWith("dart") || WeaponName2.startsWith("knife") || WeaponName2.startsWith("javelin") || WeaponName.equalsIgnoreCase("toktz-xil-ul")){
			c.setSidebarInterface(0, 4446); // accurate, rapid, longrange
			c.getPA().sendFrame246(4447, 200, Weapon);
			c.getPA().sendText(WeaponName, 4449);
		}else if(WeaponName2.startsWith("dagger") || WeaponName2.contains("sword")){
			c.setSidebarInterface(0, 2276); // stab, lunge, slash, block
			c.getPA().sendFrame246(2277, 200, Weapon);
			c.getPA().sendText(WeaponName, 2279);
		}else if(WeaponName2.startsWith("pickaxe")){
			c.setSidebarInterface(0, 5570); // spike, impale, smash, block
			c.getPA().sendFrame246(5571, 200, Weapon);
			c.getPA().sendText(WeaponName, 5573);
		}else if(WeaponName2.startsWith("axe") || WeaponName2.startsWith("battleaxe")){
			c.setSidebarInterface(0, 1698); // chop, hack, smash, block
			c.getPA().sendFrame246(1699, 200, Weapon);
			c.getPA().sendText(WeaponName, 1701);
		}else if(WeaponName2.startsWith("halberd")){
			c.setSidebarInterface(0, 8460); // jab, swipe, fend
			c.getPA().sendFrame246(8461, 200, Weapon);
			c.getPA().sendText(WeaponName, 8463);
		}else if(WeaponName2.toLowerCase().contains("scythe")){
			c.setSidebarInterface(0, 8460); // jab, swipe, fend
			c.getPA().sendFrame246(8461, 200, Weapon);
			c.getPA().sendText(WeaponName, 8463);
		}else if(WeaponName2.startsWith("spear")){
			c.setSidebarInterface(0, 4679); // lunge, swipe, pound, block
			c.getPA().sendFrame246(4680, 200, Weapon);
			c.getPA().sendText(WeaponName, 4682);
		}else if(WeaponName2.toLowerCase().contains("mace")){
			c.setSidebarInterface(0, 3796);
			c.getPA().sendFrame246(3797, 200, Weapon);
			c.getPA().sendText(WeaponName, 3799);

		}else{
			switch(c.playerEquipment[c.playerWeapon]){
				case 4153:
				case 15039:
				case 24455: // annihilation
				case 18353:
				case 13902:
				case 13904:
					c.setSidebarInterface(0, 425);
					c.getPA().sendFrame246(426, 200, Weapon);
					c.getPA().sendText(WeaponName, 428);
					break; // Pound, Pummel, Block mauls
				case 24456:
				case 15241:
					c.setSidebarInterface(0, 1764); // accurate, rapid, longrange
					c.getPA().sendFrame246(1765, 200, Weapon);
					c.getPA().sendText(WeaponName, 1767);
					break;
				case 24457:
					c.setSidebarInterface(0, 328); // spike, impale, smash, block
					c.getPA().sendFrame246(329, 200, Weapon);
					c.getPA().sendText(WeaponName, 331);
					break;
				case 13879:
				case 13880:
				case 13881:
				case 13882:
				case 13883:
					c.setSidebarInterface(0, 4446); // accurate, rapid, longrange
					c.getPA().sendFrame246(4447, 200, Weapon);
					c.getPA().sendText(WeaponName, 4449);
					break;
				default:
					c.setSidebarInterface(0, 2423); // chop, slash, lunge, block
					c.getPA().sendFrame246(2424, 200, Weapon);
					c.getPA().sendText(WeaponName, 2426);
					break;
			}
		}
	}

	/**
	 * Weapon Requirements
	 **/

	public void getRequirements(String itemName, int itemId){
		c.attackLevelReq = c.defenceLevelReq = c.strengthLevelReq = c.rangeLevelReq = c.magicLevelReq = c.fireLevelReq = c.hpLevelReq = c.prayerLevelReq = c.Donatorreq = 0;
		if(itemName.contains("mystic") || itemName.contains("nchanted")){
			if(itemName.contains("staff")){
				c.magicLevelReq = 20;
				c.attackLevelReq = 40;
			}else{
				c.magicLevelReq = 20;
				c.defenceLevelReq = 20;
			}
		}

		if(itemName.toLowerCase().contains("ava") && !itemName.toLowerCase().contains("attractor")){
			c.rangeLevelReq = 50;
			return;
		}

		if(itemName.contains("infinity")){
			c.magicLevelReq = 50;
			c.defenceLevelReq = 25;
		}
		if(itemName.contains("splitbark")){
			c.magicLevelReq = 40;
			c.defenceLevelReq = 40;
		}
		if(itemName.contains("rune c'bow")){
			c.rangeLevelReq = 61;
		}
		if(itemName.contains("black d'hide")){
			c.rangeLevelReq = 70;
		}
		if(itemName.contains("tzhaar-ket-om")){
			c.strengthLevelReq = 60;
		}
		if(itemName.contains("red d'hide")){
			c.rangeLevelReq = 60;
		}
		if(itemName.contains("blue d'hide")){
			c.rangeLevelReq = 50;
		}
		if(itemName.contains("green d'hide")){
			c.rangeLevelReq = 40;
		}
		if(itemName.contains("initiate")){
			c.defenceLevelReq = 20;
		}
		if(itemName.contains("defender")){
			String name = itemName.toLowerCase();
			if(name.contains("steel")){
				c.attackLevelReq = 5;
				c.defenceLevelReq = 5;
			}else if(name.contains("black")){
				c.attackLevelReq = 10;
				c.defenceLevelReq = 10;
			}else if(name.contains("mithril")){
				c.attackLevelReq = 20;
				c.defenceLevelReq = 20;
			}else if(name.contains("adamant")){
				c.attackLevelReq = 30;
				c.defenceLevelReq = 30;
			}else if(name.contains("rune")){
				c.attackLevelReq = 40;
				c.defenceLevelReq = 40;
			}else if(name.contains("dragon")){
				c.attackLevelReq = 60;
				c.defenceLevelReq = 60;
			}
			return;
		}
		if(itemName.contains("steel")){
			if(!itemName.contains("knife") && !itemName.contains("dart") && !itemName.contains("javelin") && !itemName.contains("thrownaxe")){
				if(itemName.contains("shield") || itemName.contains("leg") || itemName.contains("skirt") || itemName.contains("helm") || itemName.contains("body"))
					c.defenceLevelReq = 5;
				else
					c.attackLevelReq = 5;
			}
			return;
		}
		if(itemName.contains("black")){
			if(!itemName.contains("knife") && !itemName.contains("dart") && !itemName.contains("javelin") && !itemName.contains("thrownaxe") && !itemName.contains("vamb") && !itemName.contains("chap") && !itemName.contains("cavalier") && !itemName.contains("robe") && !itemName.contains("beret") && !itemName.contains("headband")){
				if(itemName.contains("shield") || itemName.contains("leg") || itemName.contains("skirt") || itemName.contains("helm") || itemName.contains("body"))
					c.defenceLevelReq = 10;
				else
					c.attackLevelReq = 10;
			}
			return;
		}
		if(itemName.contains("mithril")){
			if(!itemName.contains("knife") && !itemName.contains("dart") && !itemName.contains("javelin") && !itemName.contains("thrownaxe")){
				if(itemName.contains("shield") || itemName.contains("leg") || itemName.contains("skirt") || itemName.contains("helm") || itemName.contains("body"))
					c.defenceLevelReq = 20;
				else
					c.attackLevelReq = 20;
			}
			return;
		}
		if(itemName.contains("adamant")){
			if(!itemName.contains("knife") && !itemName.contains("dart") && !itemName.contains("javelin") && !itemName.contains("thrownaxe")){
				if(itemName.contains("shield") || itemName.contains("leg") || itemName.contains("skirt") || itemName.contains("helm") || itemName.contains("body"))
					c.defenceLevelReq = 30;
				else
					c.attackLevelReq = 30;
			}
			return;
		}
		if(itemName.contains("rune")){
			if(!itemName.contains("knife") && !itemName.contains("dart") && !itemName.contains("javelin") && !itemName.contains("thrownaxe") && !itemName.contains("'bow")){
				if(itemName.contains("shield") || itemName.contains("leg") || itemName.contains("skirt") || itemName.contains("helm") || itemName.contains("body"))
					c.defenceLevelReq = 40;
				else if(!itemName.toLowerCase().contains("cape") && !itemName.toLowerCase().contains("hood"))
					c.attackLevelReq = 40;
			}
			return;
		}
		if(itemName.contains("granite shield")){
			if(!itemName.contains("maul")){
				c.defenceLevelReq = 50;
			}
			return;
		}
		if(itemName.contains("granite maul")){
			if(!itemName.contains("shield")){
				c.attackLevelReq = 50;
			}
			return;
		}
		if(itemName.contains("warrior")){
			if(!itemName.contains("ring")){
				c.defenceLevelReq = 40;
			}
			return;
		}
		if(itemName.contains("dragonfire")){
			c.defenceLevelReq = 75;
		}
		if(itemName.contains("enchanted") || itemName.toLowerCase().contains("gilded")){
			c.defenceLevelReq = 40;
		}
		if(itemName.contains("d'hide")){
			if(!itemName.contains("chaps")){
				c.defenceLevelReq = c.rangeLevelReq = 40;
			}
			return;
		}
		if(itemName.contains("dragon dagger")){

			c.attackLevelReq = 60;
		}
		if(itemName.contains("drag dagger")){

			c.attackLevelReq = 60;
		}
		if(itemName.contains("ancient")){

			c.attackLevelReq = 50;
		}
		if(itemName.contains("staff of light")){

			c.attackLevelReq = 50;
		}
		if(itemName.contains("hardleather")){

			c.defenceLevelReq = 10;
		}
		if(itemName.contains("studded")){

			c.defenceLevelReq = 20;
		}
		if(itemName.contains("party")){

			c.Donatorreq = 0;

		}
		if(itemName.contains("h'ween")){

			c.Donatorreq = 0;

		}
		if(itemName.contains("santa")){

			c.Donatorreq = 0;

		}
		if(itemName.contains("bandos")){
			if(!itemName.contains("godsword")){
				c.strengthLevelReq = c.defenceLevelReq = 65;
				c.Donatorreq = 0;
				return;
			}
		}
		if(itemName.toLowerCase().contains("dragon")){
			if(!itemName.contains("nti-") && !itemName.contains("fire")){
				if(itemName.contains("shield") || itemName.contains("chain") || itemName.contains("helm") || itemName.contains("plate") || itemName.contains("boot")){
					c.defenceLevelReq = 60;
					return;
				}else{
					c.attackLevelReq = 60;
					return;
				}
			}
		}
		if(itemName.contains("crystal")){
			if(itemName.contains("shield")){
				c.defenceLevelReq = 70;
			}else{
				c.rangeLevelReq = 70;
			}
			return;
		}
		if(itemName.contains("ahrim")){
			if(itemName.contains("staff")){
				c.magicLevelReq = 70;
				c.attackLevelReq = 70;
			}else{
				c.magicLevelReq = 70;
				c.defenceLevelReq = 70;
			}
		}
		if(itemName.contains("karil")){
			if(itemName.contains("crossbow")){
				c.rangeLevelReq = 70;
			}else{
				c.rangeLevelReq = 70;
				c.defenceLevelReq = 70;
			}
		}
		if(itemName.contains("armadyl")){
			if(itemName.contains("godsword")){
				c.attackLevelReq = 75;
				c.Donatorreq = 0;
			}else{
				c.rangeLevelReq = c.defenceLevelReq = 65;
			}
		}
		if(itemName.contains("saradomin")){
			if(itemName.contains("sword")){
				c.attackLevelReq = 70;
			}
			if(itemName.contains("crozier")){
				c.attackLevelReq = 1;
				if(itemName.contains("robe")){
					c.attackLevelReq = 1;

				}else{
					c.defenceLevelReq = 40;

				}
			}
		}
		if(itemName.contains("godsword")){
			c.attackLevelReq = 75;
		}
		if(itemName.contains("3rd age") && !itemName.contains("amulet")){
			c.defenceLevelReq = 60;
		}
		if(itemName.contains("verac") || itemName.contains("guthan") || itemName.contains("dharok") || itemName.contains("torag")){

			if(itemName.contains("hammers")){
				c.attackLevelReq = 70;
				c.strengthLevelReq = 70;
			}else if(itemName.contains("axe")){
				c.attackLevelReq = 70;
				c.strengthLevelReq = 70;
			}else if(itemName.contains("warspear")){
				c.attackLevelReq = 70;
				c.strengthLevelReq = 70;
			}else if(itemName.contains("flail")){
				c.attackLevelReq = 70;
				c.strengthLevelReq = 70;
			}else{
				c.defenceLevelReq = 70;
			}
		}

		switch(itemId){
			case 18335:
				c.magicLevelReq = 85;
				return;
			case 17287:
				c.defenceLevelReq = 94;
				return;
			case 17295:
				c.rangeLevelReq = 98;
				return;
			case 14484:
				c.attackLevelReq = 60;
				return;
			case 20097:
				c.rangeLevelReq = 99;
				c.attackLevelReq = 99;
				return;
			case 21777:
				c.magicLevelReq = 82;
				c.attackLevelReq = 40;
				return;
			case 20929:
			case 23674:
			case 24433:
				c.attackLevelReq = 75;
				return;
			case 22346:
				c.attackLevelReq = 90;
				return;
			case 22358:
			case 22359:
			case 22360:
			case 22361:
				c.attackLevelReq = 80;
				return;
			case 22362:
			case 22363:
			case 22364:
			case 22365:
				c.rangeLevelReq = 80;
				return;
			case 22366:
			case 22367:
			case 22368:
			case 22369:
				c.magicLevelReq = 80;
				return;
			case 22482:
			case 22484:
			case 22486:
			case 22488:
			case 22490:
			case 22492:
				c.magicLevelReq = 85;
				c.defenceLevelReq = 85;
				return;
			case 19711:
				c.attackLevelReq = c.strengthLevelReq = c.defenceLevelReq = c.hpLevelReq = c.rangeLevelReq = c.magicLevelReq = 42;
				c.prayerLevelReq = 22;
				return;
			case 24455:
			case 16425:
				c.attackLevelReq = 99;
				c.strengthLevelReq = 99;
				return;
			case 24456:
				c.attackLevelReq = 99;
				c.rangeLevelReq = 99;
				return;
			case 24457:
				c.attackLevelReq = 99;
				c.magicLevelReq = 99;
				return;
			case 22347:
			case 22494:
			case 22496:
				c.magicLevelReq = 90;
				return;
			case 20137:
			case 20138:
			case 20141:
			case 20142:
			case 20145:
			case 20146:
			case 20135:
			case 20139:
			case 20143:
			case 20159:
			case 20161:
			case 20162:
			case 20163:
			case 20165:
			case 20166:
			case 20167:
			case 20169:
			case 20170:
			case 20147:
			case 20149:
			case 20150:
			case 20151:
			case 20153:
			case 20154:
			case 20155:
			case 20157:
			case 20158:
				c.defenceLevelReq = 99;
				c.strengthLevelReq = 99;
				c.attackLevelReq = 99;
				c.magicLevelReq = 99;
				c.rangeLevelReq = 99;
				return;
			case 15486:
				c.attackLevelReq = 75;
				c.magicLevelReq = 75;
				return;
			case 13734:
				c.defenceLevelReq = 40;
				return;
			case 13736:
				c.defenceLevelReq = 50;
				return;
			case 4087:
			case 4585:
			case 11732:
			case 13744:
				c.defenceLevelReq = 60;
				return;
			case 20171:
			case 20173:
			case 20174:
			case 19146:
				c.rangeLevelReq = 80;
				break;
			case 21787:
				c.attackLevelReq = c.defenceLevelReq = 85;
				break;
			case 21790:
				c.rangeLevelReq = c.defenceLevelReq = 85;
				break;
			case 21793:
				c.magicLevelReq = c.defenceLevelReq = 85;
				break;
			case 16689:
			case 16711:
			case 17259:
			case 17361:
			case 16667:
				c.defenceLevelReq = 99;
				return;
			case 20096:
			case 20095:
			case 20094:
			case 20093:
			case 16137:
				c.attackLevelReq = 99;
				return;
			case 13858:
			case 13860:
			case 13861:
			case 13863:
			case 13864:
			case 13866:
				c.defenceLevelReq = 70;
				c.magicLevelReq = 70;
				return;
			case 13870:
			case 13872:
			case 13873:
			case 13875:
			case 13876:
			case 13878:
				c.defenceLevelReq = 70;
				c.rangeLevelReq = 70;
				return;
			case 13893:
			case 13887:
			case 13889:
			case 13895:
			case 13742:
			case 13740:
			case 13886:
			case 13892:
			case 13898:
			case 13738:
			case 13890:
			case 13884:
			case 13896:
			case 18359:
			case 18363:
			case 20099:
			case 16293:
			case 16359:
				c.defenceLevelReq = 70;
				return;
			case 13899:
			case 13907:
			case 13905:
			case 13901:
			case 13902:
			case 13904:
			case 18349:
			case 18351:
			case 18353:
			case 20098:
			case 16955:
			case 15773:
			case 16403:
				c.attackLevelReq = 70;
				return;
			case 19784:
			case 19780:
				c.attackLevelReq = 80;
				return;
			case 13867:
			case 13869:
			case 18355:
				c.attackLevelReq = 70;
				c.magicLevelReq = 70;
				return;
			case 18357:
			case 13879:
			case 13883:
				c.attackLevelReq = 70;
				c.rangeLevelReq = 70;
				return;
			case 18361:
				c.defenceLevelReq = 70;
				c.rangeLevelReq = 70;
			case 8839:
			case 8840:
			case 8841:
			case 8842:
			case 11663:
			case 11664:
			case 11665:
				c.attackLevelReq = 42;
				c.rangeLevelReq = 42;
				c.strengthLevelReq = 42;
				c.magicLevelReq = 42;
				c.defenceLevelReq = 42;
				return;
			case 15241:
				c.rangeLevelReq = 75;
				c.fireLevelReq = 61;
				return;
			case 10551:
			case 2503:
			case 2501:
			case 2499:
			case 1135:
				c.defenceLevelReq = 40;
				return;
			case 11235:
			case 6522:
				c.rangeLevelReq = 60;
				return;
			case 6524:
				c.defenceLevelReq = 60;
				return;
			case 11284:
				c.defenceLevelReq = 75;
				return;
			case 6889:
			case 6914:
				c.magicLevelReq = 60;
				return;
			case 859:
			case 861:
				c.rangeLevelReq = 50;
				return;
			case 10828:
				c.defenceLevelReq = 55;
				return;
			case 11724:
			case 11726:
			case 11728:
				c.defenceLevelReq = 65;
				return;
			case 3751:
			case 3749:
			case 3755:
				c.defenceLevelReq = 40;
				return;
			case 7462:
			case 7461:
				c.defenceLevelReq = 40;
				return;
			case 8846:
				c.defenceLevelReq = 5;
				return;
			case 8847:
				c.defenceLevelReq = 10;
				return;
			case 8848:
				c.defenceLevelReq = 20;
				return;
			case 8849:
				c.defenceLevelReq = 30;
				return;
			case 8850:
				c.defenceLevelReq = 40;
			case 7460:
				c.defenceLevelReq = 20;
				return;
			case 15039:
				c.attackLevelReq = 85;
				return;
			case 837:
				c.rangeLevelReq = 61;
				return;
			case 4151: // if you don't want to use names
			case 15441:
			case 15442:
			case 15443:
			case 15444:
				c.attackLevelReq = 70;
				return;
			case 6724: // seercull
				c.rangeLevelReq = 60; // idk if that is correct
				return;
			case 4153:
				c.attackLevelReq = 50;
				c.strengthLevelReq = 50;
				return;
		}
	}

	/**
	 * two handed weapon check
	 **/
	public boolean is2handed(String itemName, int itemId){
		if(itemName.contains("ahrim") || itemName.contains("karil") || itemName.contains("verac") || itemName.contains("guthan") || itemName.contains("dharok") || itemName.contains("torag")){
			return true;
		}
		if(itemName.contains("longbow") || itemName.contains("shortbow") || itemName.contains("ark bow")){
			return true;
		}
		if(itemName.contains("crystal") || itemName.contains("katana") || itemName.contains("halberd")){
			return true;
		}
		if(itemName.contains("godsword") || itemName.contains("claw") || itemName.contains("aradomin sword") || itemName.contains("2h") || itemName.contains("spear")){
			return true;
		}
		switch(itemId){
			case 1419: // scythe
			case 22321: // golden scythe
			case 16425:
			case 4084:
			case 20097:
			case 6724: // seercull
			case 11730:
			case 4153:
			case 15039:
			case 6528:
			case 18353:
			case 24456:
			case 19146:
			case 20171:
			case 20173:
			case 20174:
			case 22346:
			case 15241:
				return true;
		}
		return false;
	}

	/**
	 * Weapons special bar, adds the spec bars to weapons that require them and
	 * removes the spec bars from weapons which don't require them
	 **/

	public void addSpecialBar(int weapon){
		switch(weapon){
			case 4151: // whip
			case 15441:
			case 15442:
			case 15443:
			case 15444:
				c.getPA().sendFrame171(0, 12323);
				specialAmount(weapon, c.specAmount, 12335);
				break;
			case 20097:
			case 859: // magic bows
			case 861:
			case 11235:
			case 19146:
			case 15241:
				c.getPA().sendFrame171(0, 7549);
				specialAmount(weapon, c.specAmount, 7561);
				break;
			case 14484: // dclwz
			case 4587: // dscimmy
				c.getPA().sendFrame171(0, 7599);
				specialAmount(weapon, c.specAmount, 7611);
				break;
			case 15486: // Staff of Light.
			case 3204: // d hally
				c.getPA().sendFrame171(0, 8493);
				specialAmount(weapon, c.specAmount, 8505);
				break;
			case 1377: // d battleaxe
				c.getPA().sendFrame171(0, 7499);
				specialAmount(weapon, c.specAmount, 7511);
				break;
			case 13902:
			case 13904:
			case 4153: // gmaul
			case 15039: // cmaul
			case 24455: // annihilation
				c.getPA().sendFrame171(0, 7474);
				specialAmount(weapon, c.specAmount, 7486);
				break;
			case 1249: // dspear
				c.getPA().sendFrame171(0, 7674);
				specialAmount(weapon, c.specAmount, 7686);
				break;
			case 13879: // Morrigan Javelins
			case 13880:
			case 13881:
			case 13882:
			case 13883: // Morrigan Axe
				c.getPA().sendFrame171(0, 7649);
				specialAmount(weapon, c.specAmount, 7661);
				break;
			case 20098:
			case 19780:
			case 19784:
			case 1215:// dragon dagger
			case 1231:
			case 5680:
			case 5698:
			case 1305: // dragon long
			case 11694:
			case 11698:
			case 11700:
			case 11730:
			case 13899:
			case 13901:
			case 11696:
			case 22346:
				c.getPA().sendFrame171(0, 7574);
				specialAmount(weapon, c.specAmount, 7586);
				break;
			case 1434: // dragon mace
				c.getPA().sendFrame171(0, 7624);
				specialAmount(weapon, c.specAmount, 7636);
				break;
			default:
				c.getPA().sendFrame171(1, 7624); // mace interface
				c.getPA().sendFrame171(1, 7474); // hammer, gmaul
				c.getPA().sendFrame171(1, 7499); // axe
				c.getPA().sendFrame171(1, 7549); // bow interface
				c.getPA().sendFrame171(1, 7574); // sword interface
				c.getPA().sendFrame171(1, 7599); // scimmy sword interface, for
													// most
													// swords
				c.getPA().sendFrame171(1, 8493);
				c.getPA().sendFrame171(1, 12323); // whip interface
				break;
		}
	}

	/**
	 * Specials bar filling amount
	 **/

	public void specialAmount(int weapon, double specAmount, int barId){
		c.specBarId = barId;
		for(int i = 10; i > 0; i--)
			c.getPA().sendFrame70(specAmount >= i ? 500 : 0, 0, (--barId));
		updateSpecialBar();
		sendWeapon(weapon, getItemName(weapon));
	}

	/**
	 * Special attack text and what to highlight or blackout
	 **/

	public void updateSpecialBar(){
		if(c.usingSpecial){
			c.getPA().sendText("" + (c.specAmount >= 2 ? "@yel@S P" : "@bla@S P") + "" + (c.specAmount >= 3 ? "@yel@ E" : "@bla@ E") + "" + (c.specAmount >= 4 ? "@yel@ C I" : "@bla@ C I") + "" + (c.specAmount >= 5 ? "@yel@ A L" : "@bla@ A L") + "" + (c.specAmount >= 6 ? "@yel@  A" : "@bla@  A") + "" + (c.specAmount >= 7 ? "@yel@ T T" : "@bla@ T T") + "" + (c.specAmount >= 8 ? "@yel@ A" : "@bla@ A") + "" + (c.specAmount >= 9 ? "@yel@ C" : "@bla@ C") + "" + (c.specAmount >= 10 ? "@yel@ K" : "@bla@ K"), c.specBarId);
		}else{
			c.getPA().sendText("@bla@S P E C I A L  A T T A C K", c.specBarId);
		}
	}

	/**
	 * Wear Item
	 **/
	public boolean isDonorItem(int itemID){
		int donorItems[] = {13887, 13896, 13884, 20100, 13864, 13858, 13860, 13863, 13866, 13869, 13861, 13867, 13876, 13872, 13875, 13878, 13870, 13873, 13879, 13883, 13893, 13890};
		for(int id : donorItems){
			if(itemID == id)
				return true;
			if(Degrade.isDegradedItem(id)){
				DegradeItem item = Degrade.getDegradeItem(id);
				if(item.isDegradedItem(itemID))
					return true;
			}
		}
		return false;
	}

	public boolean isZarosHead(int itemID){
		int possible_ids[] = {20135, 20137, 20147, 20149, 20159, 20161};
		for(int id : possible_ids)
			if(id == itemID)
				return true;
		return false;
	}

	public boolean isZarosDroppedHead(int itemID){
		int possible_ids[] = {20137, 20149, 20161};
		for(int id : possible_ids)
			if(id == itemID)
				return true;
		return false;
	}

	public boolean isZarosBody(int itemID){
		int possible_ids[] = {20139, 20141, 20151, 20153, 20163, 20165};
		for(int id : possible_ids)
			if(id == itemID)
				return true;
		return false;
	}

	public boolean isZarosDroppedBody(int itemID){
		int possible_ids[] = {20141, 20153, 20165};
		for(int id : possible_ids)
			if(id == itemID)
				return true;
		return false;
	}

	public boolean isZarosLegs(int itemID){
		int possible_ids[] = {20143, 20145, 20155, 20157, 20167, 20169};
		for(int id : possible_ids)
			if(id == itemID)
				return true;
		return false;
	}

	public boolean isZarosDroppedLegs(int itemID){
		int possible_ids[] = {20145, 20157, 20169};
		for(int id : possible_ids)
			if(id == itemID)
				return true;
		return false;
	}

	public boolean wearItem(int wearID, int slot){
		synchronized(c){
			if(slot >= c.inventory.items.length || slot < 0)
				return false;
			int targetSlot = 0;
			boolean canWearItem = true;
			if(c.inventory.items[slot] != null && c.inventory.items[slot].id == (wearID + 1)){
				getRequirements(getItemName(wearID).toLowerCase(), wearID);
				int fixedSlot = itemType(wearID);
				targetSlot = fixedSlot < 0 ? Item.targetSlots[wearID] : fixedSlot;
				if(c.deleteAnnihilation && targetSlot == c.playerWeapon)
					return false;
				if(CastleWars.isInCw(c) || CastleWars.isInCwWait(c)){
					if(targetSlot == 1 || targetSlot == 0){
						c.sendMessage("You can't wear your own capes or hats in a Castle Wars Game!");
						return false;
					}
				}
				if(isDonorItem(wearID) && !c.isDonor()){
					c.sendMessage("You must be a donor to wield this item.");
					return false;
				}
				if(c.duel != null && c.duel.rules != null && c.duel.rules.duelRule[11] && targetSlot == 0 && c.duel.status == 3){
					c.sendMessage("Wearing hats has been disabled in this duel!");
					return false;
				}
				if(c.duel != null && c.duel.rules != null && c.duel.rules.duelRule[12] && targetSlot == 1 && c.duel.status == 3){
					c.sendMessage("Wearing capes has been disabled in this duel!");
					return false;
				}
				if(c.duel != null && c.duel.rules != null && c.duel.rules.duelRule[13] && targetSlot == 2 && c.duel.status == 3){
					c.sendMessage("Wearing amulets has been disabled in this duel!");
					return false;
				}
				if(c.duel != null && c.duel.rules != null && c.duel.rules.duelRule[14] && targetSlot == 3 && c.duel.status == 3){
					c.sendMessage("Wielding weapons has been disabled in this duel!");
					return false;
				}
				if(c.duel != null && c.duel.rules != null && c.duel.rules.duelRule[15] && targetSlot == 4 && c.duel.status == 3){
					c.sendMessage("Wearing bodies has been disabled in this duel!");
					return false;
				}
				if((c.duel != null && c.duel.rules != null && c.duel.rules.duelRule[16] && targetSlot == 5) || (c.duel != null && c.duel.rules != null && c.duel.rules.duelRule[16] && is2handed(getItemName(wearID).toLowerCase(), wearID)) && c.duel.status == 3){
					c.sendMessage("Wearing shield has been disabled in this duel!");
					return false;
				}
				if(c.duel != null && c.duel.rules != null && c.duel.rules.duelRule[17] && targetSlot == 7 && c.duel.status == 3){
					c.sendMessage("Wearing legs has been disabled in this duel!");
					return false;
				}
				if(c.duel != null && c.duel.rules != null && c.duel.rules.duelRule[18] && targetSlot == 9 && c.duel.status == 3){
					c.sendMessage("Wearing gloves has been disabled in this duel!");
					return false;
				}
				if(c.duel != null && c.duel.rules != null && c.duel.rules.duelRule[19] && targetSlot == 10 && c.duel.status == 3){
					c.sendMessage("Wearing boots has been disabled in this duel!");
					return false;
				}
				if(c.duel != null && c.duel.rules != null && c.duel.rules.duelRule[20] && targetSlot == 12 && c.duel.status == 3){
					c.sendMessage("Wearing rings has been disabled in this duel!");
					return false;
				}
				if(c.duel != null && c.duel.rules != null && c.duel.rules.duelRule[21] && targetSlot == 13 && c.duel.status == 3){
					c.sendMessage("Wearing arrows has been disabled in this duel!");
					return false;
				}

				if(Config.itemRequirements){
					if(targetSlot == 3 || targetSlot == 10 || targetSlot == 7 || targetSlot == 5 || targetSlot == 4 || targetSlot == 0 || targetSlot == 9 || targetSlot == 10){
						if(c.defenceLevelReq > 0){
							if(c.getPA().getLevelForXP(c.playerXP[c.playerDefence]) < c.defenceLevelReq){
								c.sendMessage("You need a defence level of " + c.defenceLevelReq + " to wear this item.");
								canWearItem = false;
							}
						}
						if(c.rangeLevelReq > 0){
							if(c.getPA().getLevelForXP(c.playerXP[c.playerRanged]) < c.rangeLevelReq){
								c.sendMessage("You need a range level of " + c.rangeLevelReq + " to wear this item.");
								canWearItem = false;
							}
						}
						if(c.magicLevelReq > 0){
							if(c.getPA().getLevelForXP(c.playerXP[c.playerMagic]) < c.magicLevelReq){
								c.sendMessage("You need a magic level of " + c.magicLevelReq + " to wear this item.");
								canWearItem = false;
							}
						}
						if(c.attackLevelReq > 0){
							if(c.getPA().getLevelForXP(c.playerXP[c.playerAttack]) < c.attackLevelReq){
								c.sendMessage("You need an attack level of " + c.attackLevelReq + " to wield this weapon.");
								canWearItem = false;
							}
						}
						if(c.strengthLevelReq > 0){
							if(c.getPA().getLevelForXP(c.playerXP[c.playerStrength]) < c.strengthLevelReq){
								c.sendMessage("You need a strength level of " + c.strengthLevelReq + " to wield this weapon.");
								canWearItem = false;
							}
						}
						if(c.hpLevelReq > 0){
							if(c.getPA().getLevelForXP(c.playerXP[c.playerHitpoints]) < c.hpLevelReq){
								c.sendMessage("You need an hp level of " + c.hpLevelReq + " to wear this item.");
								canWearItem = false;
							}
						}
						if(c.prayerLevelReq > 0){
							if(c.getPA().getLevelForXP(c.playerXP[c.playerPrayer]) < c.prayerLevelReq){
								c.sendMessage("You need a prayer level of " + c.prayerLevelReq + " to wear this item.");
								canWearItem = false;
							}
						}
						if(c.fireLevelReq > 0){
							if(c.getPA().getLevelForXP(c.playerXP[c.playerFiremaking]) < c.fireLevelReq){
								c.sendMessage("You need a firemaking level of " + c.fireLevelReq + " to wear this item.");
								canWearItem = false;
							}
						}
						if(c.fireLevelReq > 0){
							if(c.getPA().getLevelForXP(c.playerXP[c.playerFiremaking]) < c.fireLevelReq){
								c.sendMessage("You need a firemaking level of " + c.fireLevelReq + " to wear this item.");
								canWearItem = false;
							}
						}
					}
				}
				if(!canWearItem)
					return false;

				int wearAmount = c.inventory.items[slot].amount;
				if(wearAmount < 1)
					return false;

				if(targetSlot == c.playerWeapon){
					c.autocasting = false;
					c.specAccuracy = 1;
					c.specDamage = 1;
					c.specHit = false;
					c.usingSpecial = false;
					c.doubleHit = false;
					c.usingClaws = false;
					c.ssSpec = false;
					c.autocastId = 0;
					if(c.playerEquipment[c.playerWeapon] == 15486 && c.solSpec){
						c.solSpec = false;
						c.solTime = -1;
						c.sendMessage("Removing the staff of light returns your melee defences to normal.");
					}
					c.getPA().sendConfig(108, 0);
				}

				if(slot >= 0 && wearID >= 0){
					int toEquip = c.inventory.items[slot].id;
					int toEquipN = c.inventory.items[slot].amount;
					int toRemove = c.playerEquipment[targetSlot];
					int toRemoveN = c.playerEquipmentN[targetSlot];
					if(CastleWars.SARA_BANNER == toRemove || CastleWars.ZAMMY_BANNER == toRemove){
						boolean is2h = is2handed(getItemName(wearID).toLowerCase(), wearID);
						boolean wearingShield = c.playerEquipment[c.playerShield] > 0;
						boolean wearingWeapon = c.playerEquipment[c.playerWeapon] > 0;
						if((wearingShield && wearingWeapon && is2h && c.inventory.freeSlots() > 0) || !is2h || !wearingShield || !wearingWeapon){
							CastleWars.dropFlag(c, toRemove);
							toRemove = -1;
							toRemoveN = 0;
						}
					}
					int degrade = c.inventory.items[slot].degrade;
					if(isZarosHead(toEquip - 1))
						c.zarosModifier += Config.ZAROS_ADD[0];
					if(isZarosBody(toEquip - 1))
						c.zarosModifier += Config.ZAROS_ADD[1];
					if(isZarosLegs(toEquip - 1))
						c.zarosModifier += Config.ZAROS_ADD[2];
					if(isZarosHead(toRemove))
						c.zarosModifier -= Config.ZAROS_ADD[0];
					if(isZarosBody(toRemove))
						c.zarosModifier -= Config.ZAROS_ADD[1];
					if(isZarosLegs(toRemove))
						c.zarosModifier -= Config.ZAROS_ADD[2];
					if(toEquip == toRemove + 1 && Item.itemStackable[toRemove]){
						c.inventory.deleteItem(toRemove, c.inventory.findItemSlot(toRemove), toEquipN);
						c.playerEquipmentN[targetSlot] += toEquipN;
					}else if(targetSlot != 5 && targetSlot != 3){
						if(toRemove > 0 && toRemoveN > 0){
							c.inventory.items[slot].id = toRemove + 1;
							c.inventory.items[slot].amount = toRemoveN;
						}else
							c.inventory.items[slot] = null;
						c.playerEquipment[targetSlot] = toEquip - 1;
						c.playerEquipmentN[targetSlot] = toEquipN;
					}else if(targetSlot == 5){
						boolean wearing2h = is2handed(getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase(), c.playerEquipment[c.playerWeapon]);
						if(wearing2h){
							toRemove = c.playerEquipment[c.playerWeapon];
							toRemoveN = c.playerEquipmentN[c.playerWeapon];
							c.playerEquipment[c.playerWeapon] = -1;
							c.playerEquipmentN[c.playerWeapon] = 0;
							updateSlot(c.playerWeapon);
						}
						c.inventory.items[slot].id = toRemove + 1;
						c.inventory.items[slot].amount = toRemoveN;
						c.playerEquipment[targetSlot] = toEquip - 1;
						c.playerEquipmentN[targetSlot] = toEquipN;
					}else if(targetSlot == 3){
						boolean is2h = is2handed(getItemName(wearID).toLowerCase(), wearID);
						boolean wearingShield = c.playerEquipment[c.playerShield] > 0;
						boolean wearingWeapon = c.playerEquipment[c.playerWeapon] > 0;
						if(is2h){
							if(wearingShield && wearingWeapon){
								if(c.inventory.freeSlots() > 0){
									c.inventory.items[slot].id = toRemove + 1;
									c.inventory.items[slot].amount = toRemoveN;
									c.playerEquipment[targetSlot] = toEquip - 1;
									c.playerEquipmentN[targetSlot] = toEquipN;
									removeItem(c.playerEquipment[c.playerShield], c.playerShield);
								}else{
									c.sendMessage("You do not have enough inventory space to do this.");
									return false;
								}
							}else if(wearingShield && !wearingWeapon){
								c.inventory.items[slot].id = c.playerEquipment[c.playerShield] + 1;
								c.inventory.items[slot].amount = c.playerEquipmentN[c.playerShield];
								c.playerEquipment[targetSlot] = toEquip - 1;
								c.playerEquipmentN[targetSlot] = toEquipN;
								c.playerEquipment[c.playerShield] = -1;
								c.playerEquipmentN[c.playerShield] = 0;
								updateSlot(c.playerShield);
							}else{
								c.inventory.items[slot].id = toRemove + 1;
								c.inventory.items[slot].amount = toRemoveN;
								c.playerEquipment[targetSlot] = toEquip - 1;
								c.playerEquipmentN[targetSlot] = toEquipN;
							}
						}else{
							c.inventory.items[slot].id = toRemove + 1;
							c.inventory.items[slot].amount = toRemoveN;
							c.playerEquipment[targetSlot] = toEquip - 1;
							c.playerEquipmentN[targetSlot] = toEquipN;
						}
					}
					int tD = c.playerEquipmentD[targetSlot];
					c.playerEquipmentD[targetSlot] = degrade;
					if(c.inventory.items[slot] != null)
						c.inventory.items[slot].degrade = tD;
					c.playerEquipmentDT[targetSlot] = -1;
					c.inventory.resetItems(3214);
				}
				if(targetSlot == 3){
					c.usingSpecial = false;
					addSpecialBar(wearID);
				}
				if(c.getOutStream() != null && c != null){
					c.getOutStream().createFrameVarSizeWord(34);
					c.getOutStream().writeWord(1688);
					c.getOutStream().writeByte(targetSlot);
					c.getOutStream().writeWord(wearID + 1);

					if(c.playerEquipmentN[targetSlot] > 254){
						c.getOutStream().writeByte(255);
						c.getOutStream().writeDWord(c.playerEquipmentN[targetSlot]);
					}else{
						c.getOutStream().writeByte(c.playerEquipmentN[targetSlot]);
					}

					c.getOutStream().endFrameVarSizeWord();
					c.flushOutStream();
				}
				sendWeapon(c.playerEquipment[c.playerWeapon], getItemName(c.playerEquipment[c.playerWeapon]));
				resetBonus();
				updateSlot(targetSlot);
				getBonus();
				writeBonus();
				c.getCombat().getPlayerAnimIndex(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
				c.getPA().requestUpdates();
				return true;
			}else{
				return false;
			}
		}
	}

	public void wearItem(int wearID, int wearAmount, int targetSlot){
		synchronized(c){
			if(c.getOutStream() != null && c != null){
				if(isDonorItem(wearID) && !c.isDonor()){
					c.sendMessage("You must be a donor to wield this item.");
					return;
				}
				c.getOutStream().createFrameVarSizeWord(34);
				c.getOutStream().writeWord(1688);
				c.getOutStream().writeByte(targetSlot);
				c.getOutStream().writeWord(wearID + 1);

				if(wearAmount > 254){
					c.getOutStream().writeByte(255);
					c.getOutStream().writeDWord(wearAmount);
				}else{
					c.getOutStream().writeByte(wearAmount);
				}
				c.getOutStream().endFrameVarSizeWord();
				c.flushOutStream();
				c.playerEquipment[targetSlot] = wearID;
				c.playerEquipmentN[targetSlot] = wearAmount;
				c.getItems().sendWeapon(c.playerEquipment[c.playerWeapon], c.getItems().getItemName(c.playerEquipment[c.playerWeapon]));
				c.getItems().resetBonus();
				c.getItems().getBonus();
				c.getItems().writeBonus();
				c.getCombat().getPlayerAnimIndex(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
				c.updateRequired = true;
				c.setAppearanceUpdateRequired(true);
			}
		}
	}

	public void updateSlot(int slot){
		synchronized(c){
			if(c.getOutStream() != null && c != null){
				c.getOutStream().createFrameVarSizeWord(34);
				c.getOutStream().writeWord(1688);
				c.getOutStream().writeByte(slot);
				c.getOutStream().writeWord(c.playerEquipment[slot] + 1);
				if(c.playerEquipmentN[slot] > 254){
					c.getOutStream().writeByte(255);
					c.getOutStream().writeDWord(c.playerEquipmentN[slot]);
				}else{
					c.getOutStream().writeByte(c.playerEquipmentN[slot]);
				}
				c.getOutStream().endFrameVarSizeWord();
				c.flushOutStream();
			}
		}

	}

	public boolean playerHasEquipped(int itemID){
		for(int i : c.playerEquipment)
			if(i == itemID)
				return true;
		return false;
	}

	/**
	 * Removes an item from the player's equipment to their inventory.
	 * 
	 * @param wearID
	 *            The id of the item being worn.
	 * @param slot
	 *            The slot the item is in the equipment.
	 */
	public void removeItem(int wearID, int slot){
		synchronized(c){
			if(c.getOutStream() != null && c != null){
				if(c.playerEquipment[slot] > -1){
					if(c.deleteAnnihilation && slot == c.playerWeapon)
						return;
					if((c.playerEquipment[slot] == CastleWars.SARA_CAPE || c.playerEquipment[slot] == CastleWars.ZAMMY_CAPE) && (CastleWars.isInCw(c) || CastleWars.isInCwWait(c))){
						c.sendMessage("You cannot unequip your castle wars cape during a game!");
						return;
					}
					if(slot == c.playerWeapon){
						c.specAccuracy = 1;
						c.specDamage = 1;
						c.specHit = false;
						c.usingSpecial = false;
						c.doubleHit = false;
						c.usingClaws = false;
						c.ssSpec = false;
					}
					if(c.inventory.addItem(c.playerEquipment[slot], c.playerEquipmentN[slot], c.playerEquipmentD[slot])){
						if(c.playerEquipment[slot] == CastleWars.SARA_BANNER || c.playerEquipment[slot] == CastleWars.ZAMMY_BANNER){
							CastleWars.dropFlag(c, c.playerEquipment[slot]);
							c.inventory.deleteItem(c.playerEquipment[slot], 1);
						}
						if(isZarosHead(c.playerEquipment[slot]))
							c.zarosModifier -= Config.ZAROS_ADD[0];
						if(isZarosBody(c.playerEquipment[slot]))
							c.zarosModifier -= Config.ZAROS_ADD[1];
						if(isZarosLegs(c.playerEquipment[slot]))
							c.zarosModifier -= Config.ZAROS_ADD[2];
						if(slot == 3 && c.playerEquipment[c.playerWeapon] == 15486 && c.solSpec){
							c.solSpec = false;
							c.solTime = -1;
							c.sendMessage("Removing the staff of light returns your melee defences to normal.");
						}
						c.playerEquipmentD[slot] = -1;
						c.playerEquipmentDT[slot] = -1;
						c.playerEquipment[slot] = -1;
						c.playerEquipmentN[slot] = 0;
						sendWeapon(c.playerEquipment[c.playerWeapon], getItemName(c.playerEquipment[c.playerWeapon]));
						resetBonus();
						getBonus();
						writeBonus();
						c.getCombat().getPlayerAnimIndex(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
						c.getOutStream().createFrame(34);
						c.getOutStream().writeWord(6);
						c.getOutStream().writeWord(1688);
						c.getOutStream().writeByte(slot);
						c.getOutStream().writeWord(0);
						c.getOutStream().writeByte(0);
						c.flushOutStream();
						c.updateRequired = true;
						c.setAppearanceUpdateRequired(true);
					}
				}
			}
		}
	}

	public void itemOnInterface(int id, int amount){
		synchronized(c){
			c.getOutStream().createFrameVarSizeWord(53);
			c.getOutStream().writeWord(2274);
			c.getOutStream().writeWord(1);
			if(amount > 254){
				c.getOutStream().writeByte(255);
				c.getOutStream().writeDWord_v2(amount);
			}else{
				c.getOutStream().writeByte(amount);
			}
			c.getOutStream().writeWordBigEndianA(id);
			c.getOutStream().endFrameVarSizeWord();
			c.flushOutStream();
		}
	}

	public void addLottery(long reward){
		if(reward <= 0)
			return;
		int certs = 0;
		int amount = c.inventory.getItemCount(995);
		reward += amount;
		c.inventory.deleteItem(995, amount);
		while(reward >= 2000000000){
			++certs;
			reward -= 2000000000;
		}
		boolean gave = certs > 0 ? false : true;
		boolean gave2 = reward > 0 ? false : true;
		if(c.inventory.freeSlots() > 0 && !gave){
			c.inventory.addItem(Config.BANK_CERTIFICATE, certs, -1);
			gave = true;
		}
		if(!gave)
			Server.itemHandler.createGroundItem(c, Config.BANK_CERTIFICATE, c.absX, c.absY, c.heightLevel, certs, c.playerId);
		if(c.inventory.freeSlots() > 0 && !gave2){
			c.inventory.addItem(995, (int)reward, -1);
			gave2 = true;
		}
		if(!gave2)
			Server.itemHandler.createGroundItem(c, 995, c.absX, c.absY, c.heightLevel, (int)reward, c.playerId);
	}

	public boolean isStackable(int itemID){
		return Item.itemStackable[itemID];
	}

	/**
	 * Update Equip tab
	 **/
	public void setEquipment(int wearID, int amount, int targetSlot){
		synchronized(c){
			c.getOutStream().createFrameVarSizeWord(34);
			c.getOutStream().writeWord(1688);
			c.getOutStream().writeByte(targetSlot);
			c.getOutStream().writeWord(wearID + 1);
			if(amount > 254){
				c.getOutStream().writeByte(255);
				c.getOutStream().writeDWord(amount);
			}else{
				c.getOutStream().writeByte(amount);
			}
			c.getOutStream().endFrameVarSizeWord();
			c.flushOutStream();
			if(isZarosHead(wearID))
				c.zarosModifier += Config.ZAROS_ADD[0];
			if(isZarosBody(wearID))
				c.zarosModifier += Config.ZAROS_ADD[1];
			if(isZarosLegs(wearID))
				c.zarosModifier += Config.ZAROS_ADD[2];
			c.playerEquipment[targetSlot] = wearID;
			c.playerEquipmentN[targetSlot] = amount;
			c.updateRequired = true;
			c.setAppearanceUpdateRequired(true);
		}
	}

	/**
	 * delete Item
	 **/

	public void deleteEquipment(int i, int j){
		synchronized(c){
			if(PlayerHandler.players[c.playerId] == null || i < 0)
				return;
			if(isZarosHead(c.playerEquipment[j]))
				c.zarosModifier -= Config.ZAROS_ADD[0];
			if(isZarosBody(c.playerEquipment[j]))
				c.zarosModifier -= Config.ZAROS_ADD[1];
			if(isZarosLegs(c.playerEquipment[j]))
				c.zarosModifier -= Config.ZAROS_ADD[2];
			c.playerEquipment[j] = -1;
			c.playerEquipmentN[j] = 0;
			c.getOutStream().createFrame(34);
			c.getOutStream().writeWord(6);
			c.getOutStream().writeWord(1688);
			c.getOutStream().writeByte(j);
			c.getOutStream().writeWord(0);
			c.getOutStream().writeByte(0);
			getBonus();
			if(j == c.playerWeapon)
				sendWeapon(-1, "Unarmed");
			resetBonus();
			getBonus();
			writeBonus();
			c.updateRequired = true;
			c.setAppearanceUpdateRequired(true);
		}
	}
	
	public void deleteEquipment(int i, int j, int z){
		synchronized(c){
			if(PlayerHandler.players[c.playerId] == null || i < 0)
				return;
			if(isZarosHead(c.playerEquipment[j]))
				c.zarosModifier -= Config.ZAROS_ADD[0];
			if(isZarosBody(c.playerEquipment[j]))
				c.zarosModifier -= Config.ZAROS_ADD[1];
			if(isZarosLegs(c.playerEquipment[j]))
				c.zarosModifier -= Config.ZAROS_ADD[2];
			c.playerEquipmentN[j] -= z > c.playerEquipmentN[j] ? c.playerEquipmentN[j] : z;
			if(c.playerEquipmentN[j] == 0)
				c.playerEquipment[j] = -1;
			c.getOutStream().createFrame(34);
			c.getOutStream().writeWord(6);
			c.getOutStream().writeWord(1688);
			c.getOutStream().writeByte(j);
			c.getOutStream().writeWord(0);
			c.getOutStream().writeByte(0);
			getBonus();
			if(j == c.playerWeapon){
				sendWeapon(-1, "Unarmed");
			}
			resetBonus();
			getBonus();
			writeBonus();
			c.updateRequired = true;
			c.setAppearanceUpdateRequired(true);
		}
	}

	public boolean deleteEquipment(){
		synchronized(c){
			if(c.playerEquipmentN[c.playerWeapon] == 1){
				c.getItems().deleteEquipment(c.playerEquipment[c.playerWeapon], c.playerWeapon);
			}else if(c.playerEquipmentN[c.playerWeapon] != 0){
				c.getOutStream().createFrameVarSizeWord(34);
				c.getOutStream().writeWord(1688);
				c.getOutStream().writeByte(c.playerWeapon);
				c.getOutStream().writeWord(c.playerEquipment[c.playerWeapon] + 1);
				if(c.playerEquipmentN[c.playerWeapon] - 1 > 254){
					c.getOutStream().writeByte(255);
					c.getOutStream().writeDWord(c.playerEquipmentN[c.playerWeapon] - 1);
				}else{
					c.getOutStream().writeByte(c.playerEquipmentN[c.playerWeapon] - 1);
				}
				c.getOutStream().endFrameVarSizeWord();
				c.flushOutStream();
				c.playerEquipmentN[c.playerWeapon]--;
			}else
				return false;
			c.updateRequired = true;
			c.setAppearanceUpdateRequired(true);
			return true;
		}
	}

	/**
	 * Determine if arrow is kept
	 */
	public boolean keepArrow(){
		if((c.playerEquipment[c.playerCape] == 20068 || c.playerEquipment[c.playerCape] == 20771) && c.playerEquipment[c.playerArrows] != 4740)
			return true;
		else if((c.playerEquipment[c.playerCape] == 20769 || c.playerEquipment[c.playerCape] == 10499) && Misc.random(5) != 1 && c.playerEquipment[c.playerArrows] != 4740)
			return true;
		else if(c.playerEquipment[c.playerCape] == 10498 && Misc.random(6) > 3 && c.playerEquipment[c.playerArrows] != 4740)
			return true;
		return false;
	}

	/**
	 * Delete Arrows
	 **/
	public boolean deleteArrow(){
		synchronized(c){
			if(keepArrow()){
				c.arrowKept = true;
				return true;
			}
			boolean ret = false;
			if(c.playerEquipmentN[c.playerArrows] == 1){
				c.getItems().deleteEquipment(c.playerEquipment[c.playerArrows], c.playerArrows);
				ret = true;
			}else if(c.playerEquipmentN[c.playerArrows] > 0){
				c.getOutStream().createFrameVarSizeWord(34);
				c.getOutStream().writeWord(1688);
				c.getOutStream().writeByte(c.playerArrows);
				c.getOutStream().writeWord(c.playerEquipment[c.playerArrows] + 1);
				if(c.playerEquipmentN[c.playerArrows] - 1 > 254){
					c.getOutStream().writeByte(255);
					c.getOutStream().writeDWord(c.playerEquipmentN[c.playerArrows] - 1);
				}else{
					c.getOutStream().writeByte(c.playerEquipmentN[c.playerArrows] - 1);
				}
				c.getOutStream().endFrameVarSizeWord();
				c.flushOutStream();
				c.playerEquipmentN[c.playerArrows]--;
				ret = true;
			}
			c.updateRequired = true;
			c.setAppearanceUpdateRequired(true);
			return ret;
		}
	}
	
	public void deleteLostArrow(){
		synchronized(c){
			if(c.playerEquipmentN[c.playerArrows] == 1)
				c.getItems().deleteEquipment(c.playerEquipment[c.playerArrows], c.playerArrows);
			else if(c.playerEquipmentN[c.playerArrows] > 0){
				c.getOutStream().createFrameVarSizeWord(34);
				c.getOutStream().writeWord(1688);
				c.getOutStream().writeByte(c.playerArrows);
				c.getOutStream().writeWord(c.playerEquipment[c.playerArrows] + 1);
				if(c.playerEquipmentN[c.playerArrows] - 1 > 254){
					c.getOutStream().writeByte(255);
					c.getOutStream().writeDWord(c.playerEquipmentN[c.playerArrows] - 1);
				}else{
					c.getOutStream().writeByte(c.playerEquipmentN[c.playerArrows] - 1);
				}
				c.getOutStream().endFrameVarSizeWord();
				c.flushOutStream();
				c.playerEquipmentN[c.playerArrows]--;
			}
			c.updateRequired = true;
			c.setAppearanceUpdateRequired(true);
		}
	}

	/**
	 * Dropping Arrows
	 **/
	public void dropArrowNpc(){
		if(c.arrowKept){
			c.arrowKept = false;
			return;
		}
		int enemyX = NPCHandler.npcs[c.oldNpcIndex].getX();
		int enemyY = NPCHandler.npcs[c.oldNpcIndex].getY();
		if(Misc.random(10) >= 4){
			int amount = Server.itemHandler.itemAmount(c.rangeItemUsed, enemyX, enemyY, c.heightLevel);
			if(amount == 0){
				Server.itemHandler.createGroundItem(c, c.rangeItemUsed, enemyX, enemyY, c.heightLevel, 1, c.getId());
			}else{
				if(Server.itemHandler.isCreator(c.playerName, c.rangeItemUsed, enemyX, enemyY, c.heightLevel))
					Server.itemHandler.addGroundItem(c.rangeItemUsed, enemyX, enemyY, c.heightLevel, 1);
				else
					Server.itemHandler.createGroundItem(c, c.rangeItemUsed, enemyX, enemyY, c.heightLevel, 1, c.getId());
			}
		}
	}

	public void dropArrowPlayer(){
		if(c.arrowKept){
			c.arrowKept = false;
			return;
		}
		int enemyX = PlayerHandler.players[c.oldPlayerIndex].getX();
		int enemyY = PlayerHandler.players[c.oldPlayerIndex].getY();
		if(Misc.random(10) >= 4){
			int amount = Server.itemHandler.itemAmount(c.rangeItemUsed, enemyX, enemyY, c.heightLevel);
			if(amount == 0){
				Server.itemHandler.createGroundItem(c, c.rangeItemUsed, enemyX, enemyY, c.heightLevel, 1, c.getId());
			}else{
				if(Server.itemHandler.isCreator(c.playerName, c.rangeItemUsed, enemyX, enemyY, c.heightLevel))
					Server.itemHandler.addGroundItem(c.rangeItemUsed, enemyX, enemyY, c.heightLevel, 1);
				else
					Server.itemHandler.createGroundItem(c, c.rangeItemUsed, enemyX, enemyY, c.heightLevel, 1, c.getId());
			}
		}
	}

	public String getItemName(int ItemID){
		for(int i = 0; i < Config.ITEM_LIMIT; i++){
			if(Server.itemHandler.ItemList[i] != null){
				if(Server.itemHandler.ItemList[i].itemId == ItemID){
					return Server.itemHandler.ItemList[i].itemName;
				}
			}
		}
		return "Unarmed";
	}

	public int getItemId(String itemName){
		for(int i = 0; i < Config.ITEM_LIMIT; i++){
			if(Server.itemHandler.ItemList[i] != null){
				if(Server.itemHandler.ItemList[i].itemName.equalsIgnoreCase(itemName)){
					return Server.itemHandler.ItemList[i].itemId;
				}
			}
		}
		return -1;
	}

	public int getUnnotedItem(int ItemID){
		int NewID = ItemID - 1;
		String NotedName = "";
		for(int i = 0; i < Config.ITEM_LIMIT; i++){
			if(Server.itemHandler.ItemList[i] != null){
				if(Server.itemHandler.ItemList[i].itemId == ItemID){
					NotedName = Server.itemHandler.ItemList[i].itemName;
				}
			}
		}
		for(int i = 0; i < Config.ITEM_LIMIT; i++){
			if(Server.itemHandler.ItemList[i] != null){
				if(Server.itemHandler.ItemList[i].itemName == NotedName){
					if(Server.itemHandler.ItemList[i].itemDescription.startsWith("Swap this note at any bank for a") == false){
						NewID = Server.itemHandler.ItemList[i].itemId;
						break;
					}
				}
			}
		}
		return NewID;
	}

	/**
	 * Drop Item
	 **/

	public void createGroundItem(int itemID, int itemX, int itemY, int itemAmount){
		synchronized(c){
			try{
				c.getOutStream().createFrame(85);
				c.getOutStream().writeByteC((itemY - 8 * c.mapRegionY));
				c.getOutStream().writeByteC((itemX - 8 * c.mapRegionX));
				c.getOutStream().createFrame(44);
				c.getOutStream().writeWordBigEndianA(itemID);
				c.getOutStream().writeWord(itemAmount);
				c.getOutStream().writeByte(0);
				c.flushOutStream();
			}catch(Exception e){
				System.out.println();
			}
		}
	}

	/**
	 * Pickup Item
	 **/
	public void removeGroundItem(int itemID, int itemX, int itemY, int Amount){
		synchronized(c){
			c.getOutStream().createFrame(85);
			c.getOutStream().writeByteC((itemY - 8 * c.mapRegionY));
			c.getOutStream().writeByteC((itemX - 8 * c.mapRegionX));
			c.getOutStream().createFrame(156);
			c.getOutStream().writeByteS(0);
			c.getOutStream().writeWord(itemID);
			c.flushOutStream();
		}
	}

	public boolean ownsCape(){
		if(c.inventory.hasItem(2412, 1) || c.inventory.hasItem(2413, 1) || c.inventory.hasItem(2414, 1))
			return true;
		for(Tab tab : c.bank.tabs)
			for(BankItem item : tab.tabItems)
				if(item.id == 2412 || item.id == 2413 || item.id == 2414)
					return true;
		if(c.playerEquipment[c.playerCape] == 2413 || c.playerEquipment[c.playerCape] == 2414 || c.playerEquipment[c.playerCape] == 2415)
			return true;
		return false;
	}

	public boolean hasAllShards(){
		return c.inventory.hasItem(11712, 1) && c.inventory.hasItem(11712, 1) && c.inventory.hasItem(11714, 1);
	}

	public void makeBlade(){
		c.inventory.deleteItem(11710, 1);
		c.inventory.deleteItem(11712, 1);
		c.inventory.deleteItem(11714, 1);
		c.inventory.addItem(11690, 1, -1);
		c.sendMessage("You combine the shards to make a blade.");
	}

	public void makeGodsword(int i){
		if(c.inventory.hasItem(11690) && c.inventory.hasItem(i)){
			c.inventory.deleteItem(11690, 1);
			c.inventory.deleteItem(i, 1);
			c.inventory.addItem(i - 8, 1, -1);
			c.sendMessage("You combine the hilt and the blade to make a godsword.");
		}
	}

	public boolean isHilt(int i){
		return i >= 11702 && i <= 11708 && i % 2 == 0;
	}
}