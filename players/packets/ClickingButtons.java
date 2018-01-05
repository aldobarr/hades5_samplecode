package server.model.players.packets;

import server.Config;
import server.Server;
import server.model.items.GameItem;
import server.model.minigames.Duel;
import server.model.minigames.Zombies;
import server.model.players.Client;
import server.model.players.PacketType;
import server.model.players.PlayerHandler;
import server.model.players.PlayerSave;
import server.util.Misc;

public class ClickingButtons implements PacketType{
	private static int emotes[] = {161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 2154, 2155, 25103, 25106, 43092, 52050, 52051, 52052, 52053, 52054, 52055, 52056, 52057, 52058, 52071, 52072, 59062, 72254, 73076, 73077};
	public static boolean isEmote(int id){
		for(int emote : emotes)
			if(emote == id)
				return true;
		return false;
	}
	@Override
	public void processPacket(Client c, int packetType, int packetSize){
		int actionButtonId = Misc.hexToInt(c.getInStream().buffer, 0, packetSize);
		// int actionButtonId = c.getInStream().readShort();
		if(c.isDead)
			return;
		if(isEmote(actionButtonId) && c.inDuel())
			return;
		if(c.playerRights == 3)
			System.out.println(c.playerName + " - actionbutton: " + actionButtonId);
		switch(actionButtonId){
			case 150:
				c.autoRet = 1 - c.autoRet;
				break;
			case 48177:
				c.screenState = c.screenState != 1 ? 1 : 0;
				break;
			case 58074:
				c.getBankPin().close();
				break;
			case 58073:
				c.getBankPin().handleIDK();
			case 58025:
			case 58026:
			case 58027:
			case 58028:
			case 58029:
			case 58030:
			case 58031:
			case 58032:
			case 58033:
			case 58034:
				c.getBankPin().pinEnter(actionButtonId);
				break;
			// 1st tele option
			case 9190:
				if(c.teleAction == 1){
					// rock crabs
					c.getPA().spellTeleport(2676, 3715, 0);
				}else if(c.teleAction == 2){
					// barrows
					c.getPA().spellTeleport(3565, 3314, 0);
				}else if(c.teleAction == 3){
					// godwars
					c.getPA().spellTeleport(2916, 3612, 0);
				}else if(c.teleAction == 4){
					// varrock wildy
					c.getPA().spellTeleport(2539, 4716, 0);
				}else if(c.teleAction == 5){
					c.getPA().spellTeleport(3046, 9779, 0);
				}else if(c.teleAction == 20){
					// lum
					c.getPA().spellTeleport(3222, 3218, 0);// 3222 3218
				}

				if(c.dialogueAction == 10){
					c.getPA().spellTeleport(2845, 4832, 0);
					c.dialogueAction = -1;
				}else if(c.dialogueAction == 11){
					c.getPA().spellTeleport(2786, 4839, 0);
					c.dialogueAction = -1;
				}else if(c.dialogueAction == 12){
					c.getPA().spellTeleport(2398, 4841, 0);
					c.dialogueAction = -1;
				}else if(c.dialogueAction == 15){
					c.getDH().sendDialogues(50, 52);
					c.dialogueAction = -1;
				}
				break;
			// mining - 3046,9779,0
			// smithing - 3079,9502,0

			// 2nd tele option
			case 9191:
				if(c.teleAction == 1){
					// tav dungeon
					c.getPA().spellTeleport(2884, 9798, 0);
				}else if(c.teleAction == 2){
					// pest control
					c.getPA().spellTeleport(2662, 2650, 0);
				}else if(c.teleAction == 3){
					// kbd
					c.getPA().spellTeleport(3015, 3849, 0);
				}else if(c.teleAction == 4){
					// graveyard
					c.getPA().spellTeleport(2978, 3616, 0);
				}else if(c.teleAction == 5){
					c.getPA().spellTeleport(3079, 9502, 0);

				}else if(c.teleAction == 20){
					c.getPA().spellTeleport(3210, 3424, 0);// 3210 3424
				}
				if(c.dialogueAction == 10){
					c.getPA().spellTeleport(2584, 4836, 0);
					c.dialogueAction = -1;
				}else if(c.dialogueAction == 11){
					c.getPA().spellTeleport(2527, 4833, 0);
					c.dialogueAction = -1;
				}else if(c.dialogueAction == 12){
					c.getPA().spellTeleport(2464, 4834, 0);
					c.dialogueAction = -1;
				}else if(c.dialogueAction == 15){
					c.getDH().sendDialogues(50, 56);
					c.dialogueAction = -1;
				}
				break;
			// 3rd tele option

			case 9192:
				if(c.teleAction == 1){
					// slayer tower
					c.getPA().spellTeleport(3428, 3537, 0);
				}else if(c.teleAction == 2){
					// tzhaar
					c.getPA().spellTeleport(2438, 5168, 0);
					c.sendMessage("To fight Jad, enter the cave.");
				}else if(c.teleAction == 3){
					// dag kings
					c.getPA().spellTeleport(1910, 4367, 0);
					c.sendMessage("Climb down the ladder to get into the lair.");
				}else if(c.teleAction == 4){
					// Hillz
					c.getPA().spellTeleport(3351, 3659, 0);
				}else if(c.teleAction == 5){
					c.getPA().spellTeleport(2813, 3436, 0);
				}else if(c.teleAction == 20){
					c.getPA().spellTeleport(2757, 3477, 0);
				}

				if(c.dialogueAction == 10){
					c.getPA().spellTeleport(2713, 4836, 0);
					c.dialogueAction = -1;
				}else if(c.dialogueAction == 11){
					c.getPA().spellTeleport(2162, 4833, 0);
					c.dialogueAction = -1;
				}else if(c.dialogueAction == 12){
					c.getPA().spellTeleport(2207, 4836, 0);
					c.dialogueAction = -1;
				}else if(c.dialogueAction == 15){
					c.getDH().sendDialogues(62, 64);
					c.dialogueAction = -1;
				}
				break;
			// 4th tele option
			case 9193:
				if(c.teleAction == 1){
					// brimhaven dungeon
					c.getPA().spellTeleport(2710, 9466, 0);
				}else if(c.teleAction == 2){
					// duel arena
					c.getPA().spellTeleport(3366, 3266, 0);
				}else if(c.teleAction == 3){
					// chaos elemental
					c.getPA().spellTeleport(3295, 3921, 0);
				}else if(c.teleAction == 4){
					// Fala
					c.getPA().spellTeleport(2653, 3283, 0);

				}else if(c.teleAction == 5){
					c.getPA().spellTeleport(2724, 3484, 0);
					c.sendMessage("For magic logs, try north of the duel arena.");
				}
				if(c.dialogueAction == 10){
					c.getPA().spellTeleport(2660, 4839, 0);
					c.dialogueAction = -1;
				}else if(c.dialogueAction == 11){
					// c.getPA().spellTeleport(2527, 4833, 0); astrals here
					c.getRunecrafting().craftRunes(2489);
					c.dialogueAction = -1;
					c.getPA().removeAllWindows();
				}else if(c.dialogueAction == 12){
					// c.getPA().spellTeleport(2464, 4834, 0); bloods here
					c.getRunecrafting().craftRunes(2490);
					c.dialogueAction = -1;
					c.getPA().removeAllWindows();
				}else if(c.teleAction == 20){
					c.getPA().spellTeleport(2964, 3378, 0);
				}else if(c.dialogueAction == 15){
					c.getDH().sendDialogues(62, 75);
					c.dialogueAction = -1;
				}
				break;
			// 5th tele option
			case 9194:
				if(c.teleAction == 1){
					// island
					c.getPA().spellTeleport(3297, 9824, 0);
				}else if(c.teleAction == 2){
					// last minigame spot
					c.getPA().spellTeleport(3272, 3687, 0);
				}else if(c.teleAction == 3){
					// Corp Beast
					c.getPA().spellTeleport(2313, 9803, 0);
					c.sendMessage("Head North to face the Corporeal Beast.");
					c.sendMessage("Head East to face the Tormented Demon.");
				}else if(c.teleAction == 4){
					// ardy lever
					c.getPA().spellTeleport(2945, 3369, 0);
				}else if(c.teleAction == 5){
					c.getPA().spellTeleport(2812, 3463, 0);
				}
				if(c.dialogueAction == 10 || c.dialogueAction == 11){
					c.dialogueId++;
					c.getDH().sendDialogues(c.dialogueId, 0);
				}else if(c.dialogueAction == 12){
					c.dialogueId = 17;
					c.getDH().sendDialogues(c.dialogueId, 0);

				}else if(c.teleAction == 20){ // Canifis
					Zombies.handleTeleport(c);
				}else if(c.dialogueAction == 15){
					c.getDH().sendDialogues(62, 80);
					c.dialogueAction = -1;
				}
				break;
			case 67103:
				c.isRunning = !c.isRunning;
				c.setNewWalkCmdIsRunning(c.isRunning);
				c.isRunning2 = c.isRunning;
				c.getPA().sendRun();
				break;
			case 67108:
				if(c.isBanking || c.isShopping)
					break;
				c.resting = !c.resting;
				c.rest();
				break;
			case 72115:
				if(!c.clanId.isEmpty()){
					if(Server.clanChat.clans.get(c.clanId).owner.equalsIgnoreCase(c.originalName)){
						Server.clanChat.sendCoinShareMessage(c.clanId, (c.lootShare ? "Lootshare" : "Coinshare") + " has been toggled to " + (!Server.clanChat.clans.get(c.clanId).coinshare ? "on" : "off") + " by the clan leader.");
						Server.clanChat.clans.get(c.clanId).coinshare = !Server.clanChat.clans.get(c.clanId).coinshare;
						Server.clanChat.updateClanChat(c.clanId);
					}else
						c.sendMessage("Only the owner of the clan has the power to do that.");
				}else
					c.sendMessage("You must be in a clan to do that.");
				break;
			case 34185:
			case 34184:
			case 34183:
			case 34182:
			case 34189:
			case 34188:
			case 34187:
			case 34186:
			case 34193:
			case 34192:
			case 34191:
			case 34190:
				if(c.craftingLeather)
					c.getCrafting().handleCraftingClick(actionButtonId);
				if(c.getFletching().fletching)
					c.getFletching().handleFletchingClick(actionButtonId);
				break;

			case 15147:
				if(c.smeltInterface){
					c.smeltType = 2349;
					c.smeltAmount = 1;
					c.getSmithing().startSmelting(c.smeltType);
				}
				break;

			case 15151:
				if(c.smeltInterface){
					c.smeltType = 2351;
					c.smeltAmount = 1;
					c.getSmithing().startSmelting(c.smeltType);
				}
				break;

			case 15159:
				if(c.smeltInterface){
					c.smeltType = 2353;
					c.smeltAmount = 1;
					c.getSmithing().startSmelting(c.smeltType);
				}
				break;

			case 29017:
				if(c.smeltInterface){
					c.smeltType = 2359;
					c.smeltAmount = 1;
					c.getSmithing().startSmelting(c.smeltType);
				}
				break;

			case 29022:
				if(c.smeltInterface){
					c.smeltType = 2361;
					c.smeltAmount = 1;
					c.getSmithing().startSmelting(c.smeltType);
				}
				break;

			case 29026:
				if(c.smeltInterface){
					c.smeltType = 2363;
					c.smeltAmount = 1;
					c.getSmithing().startSmelting(c.smeltType);
				}
				break;
			case 70222:
				c.rejoinClan = !c.rejoinClan;
				c.getPA().sendConfig(583, !c.rejoinClan);
				break;
			case 59097:
				c.getPA().showInterface(15106);
				c.getItems().writeBonus();
				break;
			case 59103:
				c.xpLock = !c.xpLock;
				c.getPA().sendText("@gre@" + (c.xpLock ? "(Locked)" : "(Unlocked)"), 15226);
				break;
			case 59100:
				c.getPA().showItemsKeptOnDeath();
				break;
			case 59004:
				c.getPA().removeAllWindows();
				break;
			case 70212:
				Server.clanChat.leaveClan(c.playerId, c.clanId);
				break;
			case 62137:
				if(!c.clanId.isEmpty()){
					c.sendMessage("You are already in a clan.");
					break;
				}
				if(c.getOutStream() != null){
					c.getOutStream().createFrame(187);
					c.flushOutStream();
				}
				break;
			case 9178:
				if(c.usingGlory){
					int temp = c.playerMagicBook;
					c.playerMagicBook = 3;
					c.getPA().startTeleport(Config.EDGEVILLE_X, Config.EDGEVILLE_Y, 0, false);
					c.playerMagicBook = temp;
				}
				if(c.dialogueAction == 2)
					c.getPA().startTeleport(3428, 3538, 0, false);
				if(c.dialogueAction == 3)
					c.getPA().startTeleport(Config.EDGEVILLE_X, Config.EDGEVILLE_Y, 0, false);
				if(c.dialogueAction == 4)
					c.getPA().startTeleport(3565, 3314, 0, false);
				if(c.dialogueAction == 5)
					c.getDH().sendDialogues(37, 3081);
				if(c.dialogueAction == 20){
					// Armadyl
					c.getPA().startTeleport(2897, 3618, 4, true);
					c.killCount = 0;
				}

				break;
			case 9179:
				if(c.usingGlory){
					int temp = c.playerMagicBook;
					c.playerMagicBook = 3;
					c.getPA().startTeleport(Config.AL_KHARID_X, Config.AL_KHARID_Y, 0, false);
					c.playerMagicBook = temp;
				}
				if(c.dialogueAction == 2)
					c.getPA().startTeleport(2884, 3395, 0, false);
				if(c.dialogueAction == 3)
					c.getPA().startTeleport(3243, 3513, 0, false);
				if(c.dialogueAction == 4)
					c.getPA().startTeleport(2444, 5170, 0, false);
				if(c.dialogueAction == 5)
					c.getDH().sendDialogues(38, 3081);
				if(c.dialogueAction == 20){
					// Bandos
					c.getPA().startTeleport(2897, 3618, 12, true);
					c.killCount = 0;
				}
				break;
			case 67121:
				if(c.lootShare && Server.clanChat.clans.containsKey(c.ownedClanName) && Server.clanChat.clans.get(c.ownedClanName).coinshare)
					Server.clanChat.sendCoinShareMessage(c.ownedClanName, "Lootshare has been toggled to Coinshare by the clan leader.");
				c.lootShare = false;
				c.getPA().sendConfig(599, 1);
				c.getPA().sendConfig(600, 0);
				break;
			case 67122:
				if(!c.lootShare && Server.clanChat.clans.containsKey(c.ownedClanName) && Server.clanChat.clans.get(c.ownedClanName).coinshare)
					Server.clanChat.sendCoinShareMessage(c.ownedClanName, "Coinshare has been toggled to Lootshare by the clan leader.");
				c.lootShare = true;
				c.getPA().sendConfig(599, 0);
				c.getPA().sendConfig(600, 1);
				break;
			case 9180:
				if(c.usingGlory){
					int temp = c.playerMagicBook;
					c.playerMagicBook = 3;
					c.getPA().startTeleport(Config.KARAMJA_X, Config.KARAMJA_Y, 0, false);
					c.playerMagicBook = temp;
				}
				if(c.dialogueAction == 2)
					c.getPA().startTeleport(2471, 10137, 0, false);
				if(c.dialogueAction == 3)
					c.getPA().startTeleport(3363, 3676, 0, false);
				if(c.dialogueAction == 4)
					c.getPA().startTeleport(2659, 2676, 0, false);
				if(c.dialogueAction == 5)
					c.getDH().sendDialogues(47, 3081);
				if(c.dialogueAction == 20){
					// Saradomin
					c.getPA().startTeleport(2897, 3618, 8, true);
					c.killCount = 0;
				}
				break;

			case 9181:
				if(c.usingGlory){
					int temp = c.playerMagicBook;
					c.playerMagicBook = 3;
					c.getPA().startTeleport(Config.MAGEBANK_X, Config.MAGEBANK_Y, 0, false);
					c.playerMagicBook = temp;
				}
				if(c.dialogueAction == 2)
					c.getPA().startTeleport(2669, 3714, 0, false);
				if(c.dialogueAction == 3)
					c.getPA().startTeleport(2540, 4716, 0, false);
				if(c.dialogueAction == 4){
					c.getPA().startTeleport(3366, 3266, 0, false);
				}
				if(c.dialogueAction == 5){
					c.getShops().openShop(65);
				}
				if(c.dialogueAction == 20){
					// Zamorak
					c.getPA().startTeleport(2897, 3618, 16, true);
					c.killCount = 0;
				}
				break;

			case 1093:
			case 1094:
			case 1097:
				if(c.autocastId > 0){
					c.getPA().resetAutocast();
				}else{
					if(c.playerMagicBook == 1){
						if(c.acceptableStaff())
							c.setSidebarInterface(0, 1689);
						else
							c.sendMessage("You can't autocast ancients with that staff.");
					}else if(c.playerMagicBook == 0){
						if(c.playerEquipment[c.playerWeapon] == 4170)
							c.setSidebarInterface(0, 12050);
						else
							c.setSidebarInterface(0, 1829);
					}
				}
				break;

			case 9167:
				if(c.dialogueAction == 1){
					c.getDH().sendDialogues(28, 548);
				}else if(c.dialogueAction == 2){
					c.getDH().sendDialogues(98, 1686);
				}else{
					c.dialogueAction = 0;
					c.getPA().removeAllWindows();
				}
				break;
			case 9168:
				if(c.dialogueAction == 1){
					c.getDH().sendDialogues(29, 548);
				}else if(c.dialogueAction == 2){
					c.getDH().sendDialogues(99, 1686);
				}else{
					c.dialogueAction = 0;
					c.getPA().removeAllWindows();
				}
				break;
			case 9169:
				if(c.dialogueAction == 1){
					c.getDH().sendDialogues(30, 548);
				}else if(c.dialogueAction == 2){
					c.getShops().openShop(85);
				}else{
					c.dialogueAction = 0;
					c.getPA().removeAllWindows();
				}
				break;
			case 9157:// barrows tele to tunnels
				if(c.getQuestHandler().questChat){
					c.getQuestHandler().chatResponse(0);
					break;
				}
				if(c.dialogueAction == 1){
					// int r = Misc.random(3);
					int r = 4;
					switch(r){
						case 0:
							c.getPA().movePlayer(3534, 9677, 0);
							break;

						case 1:
							c.getPA().movePlayer(3534, 9712, 0);
							break;

						case 2:
							c.getPA().movePlayer(3568, 9712, 0);
							break;

						case 3:
							c.getPA().movePlayer(3568, 9677, 0);
							break;
						case 4:
							c.getPA().movePlayer(3551, 9694, 0);
							break;
					}
				}else if(c.dialogueAction == 2){
					c.getPA().movePlayer(2507, 4717, 0);
				}else if(c.dialogueAction == 5){
					c.getSlayer().requestTask();
				}else if(c.dialogueAction == 6){
					c.getSlayer().requestNewTask();
				}else if(c.dialogueAction == 7){
					c.getPA().startTeleport(3088, 3933, 0, false);
					c.sendMessage("NOTE: You are now in the wilderness...");
				}else if(c.dialogueAction == 8){
					c.getPA().resetBarrows();
					c.sendMessage("Your barrows have been reset.");
				}else if(c.dialogueAction == 9){
					c.getDH().sendDialogues(24, 548);
					break;
				}else if(c.dialogueAction == 10){
					c.ghostEvent = true;
					c.getDH().sendDialogues(94, 1686);
					break;
				}
				c.dialogueAction = 0;
				c.getPA().removeAllWindows();
				break;

			case 67200:
				if(c.playerNotes.size() == 30)
					c.getPA().closeInput();
				break;
			case 67203:
				c.playerNotes.clear();
				for(int i = 18801; i<18831; i++)
					c.getPA().sendText("", i);
				c.getPA().sendText("No notes", 13800);
				break;
				
			case 9158:
				if(c.getQuestHandler().questChat){
					c.getQuestHandler().chatResponse(1);
					break;
				}
				if(c.dialogueAction == 8)
					c.getPA().fixAllBarrows();
				else if(c.dialogueAction == 9){
					c.getDH().sendDialogues(25, 548);
				}else if(c.dialogueAction == 10){
					c.getDH().sendDialogues(95, 1686);
				}else{
					c.dialogueAction = 0;
					c.getPA().removeAllWindows();
				}
				break;

			/** Specials **/
			case 29213:
				c.specBarId = 7661;
				c.usingSpecial = !c.usingSpecial;
				c.getItems().updateSpecialBar();
				break;
			case 29188:
				c.specBarId = 7636; // the special attack text - sendframe126(S P E C I A L A T T A C K, c.specBarId);
				c.usingSpecial = !c.usingSpecial;
				c.getItems().updateSpecialBar();
				break;

			case 29163:
				c.specBarId = 7611;
				c.usingSpecial = !c.usingSpecial;
				c.getItems().updateSpecialBar();
				break;

			case 33033:
				c.specBarId = 8505;
				c.usingSpecial = !c.usingSpecial;
				c.getItems().updateSpecialBar();
				if(c.playerEquipment[c.playerWeapon] == 15486){ // Staff of Light
					if(c.specAmount < 10.0){
						c.usingSpecial = !c.usingSpecial;
						c.getItems().updateSpecialBar();
						c.sendMessage("You don't have the required special energy to use this attack.");
						break;
					}
					if(c.duel != null && c.duel.status == 3){
						c.usingSpecial = !c.usingSpecial;
						c.getItems().updateSpecialBar();
						c.sendMessage("This special has been disabled in the duel arena!");
						break;
					}
					c.specAmount -= 10.0;
					c.usingSpecial = !c.usingSpecial;
					c.getItems().updateSpecialBar();
					c.getItems().addSpecialBar(15486);
					c.solSpec = true;
					c.solTime = Misc.currentTimeSeconds() + 60;
					c.gfx0(2319);
					c.startAnimation(12804);
				}
				break;

			case 29038:
				c.specBarId = 7486;
				if(c.playerEquipment[c.playerWeapon] != 4153){
					c.usingSpecial = !c.usingSpecial;
					c.getItems().updateSpecialBar();
					break;
				}
				/*
				 * if (c.specAmount >= 5) { c.attackTimer = 0;
				 * c.getCombat().attackPlayer(c.playerIndex); c.usingSpecial =
				 * true; c.specAmount -= 5; }
				 */
				if(c.playerIndex > 0)
					c.getCombat().handleGmaulPlayer();
				else if(c.npcIndex > 0)
					c.getCombat().handleGmaulNPC();
				c.getItems().updateSpecialBar();
				break;

			case 29063:
				if(c.getCombat().checkSpecAmount(c.playerEquipment[c.playerWeapon])){
					c.gfx0(246);
					c.forcedChat("Raarrrrrgggggghhhhhhh!");
					c.startAnimation(1056);
					c.playerLevel[2] = c.getLevelForXP(c.playerXP[2]) + (c.getLevelForXP(c.playerXP[2]) * 15 / 100);
					c.getPA().refreshSkill(2);
					c.getItems().updateSpecialBar();
				}else{
					c.sendMessage("You don't have the required special energy to use this attack.");
				}
				break;

			case 48023:
				c.specBarId = 12335;
				c.usingSpecial = !c.usingSpecial;
				c.getItems().updateSpecialBar();
				break;

			case 29138:
				c.specBarId = 7586;
				c.usingSpecial = !c.usingSpecial;
				c.getItems().updateSpecialBar();
				break;

			case 29113:
				c.specBarId = 7561;
				c.usingSpecial = !c.usingSpecial;
				c.getItems().updateSpecialBar();
				break;

			case 29238:
				c.specBarId = 7686;
				c.usingSpecial = !c.usingSpecial;
				c.getItems().updateSpecialBar();
				break;

			/** Dueling **/
			case 26065: // no forfeit
			case 26040:
				c.duelSlot = -1;
				if(c.duel != null && c.duel.status == 1)
					c.duel.selectRule(c, 0);
				break;

			case 26066: // no movement
			case 26048:
				c.duelSlot = -1;
				if(c.duel != null && c.duel.status == 1)
					c.duel.selectRule(c, 1);
				break;

			case 26069: // no range
			case 26042:
				c.duelSlot = -1;
				if(c.duel != null && c.duel.status == 1)
					c.duel.selectRule(c, 2);
				break;

			case 26070: // no melee
			case 26043:
				c.duelSlot = -1;
				if(c.duel != null && c.duel.status == 1)
					c.duel.selectRule(c, 3);
				break;

			case 26071: // no mage
			case 26041:
				c.duelSlot = -1;
				if(c.duel != null && c.duel.status == 1)
					c.duel.selectRule(c, 4);
				break;

			case 26072: // no drinks
			case 26045:
				c.duelSlot = -1;
				if(c.duel != null && c.duel.status == 1)
					c.duel.selectRule(c, 5);
				break;

			case 26073: // no food
			case 26046:
				c.duelSlot = -1;
				if(c.duel != null && c.duel.status == 1)
					c.duel.selectRule(c, 6);
				break;

			case 26074: // no prayer
			case 26047:
				c.duelSlot = -1;
				if(c.duel != null && c.duel.status == 1)
					c.duel.selectRule(c, 7);
				break;

			case 26076: // obsticals
			case 26075:
				c.duelSlot = -1;
				if(c.duel != null && c.duel.status == 1)
					c.duel.selectRule(c, 8);
				break;

			case 2158: // fun weapons
			case 2157:
				c.duelSlot = -1;
				if(c.duel != null && c.duel.status == 1)
					c.duel.selectRule(c, 9);
				break;

			case 30136: // sp attack
			case 30137:
				c.duelSlot = -1;
				if(c.duel != null && c.duel.status == 1)
					c.duel.selectRule(c, 10);
				break;

			case 53245: // no helm
				c.duelSlot = 0;
				if(c.duel != null && c.duel.status == 1)
					c.duel.selectRule(c, 11);
				break;

			case 53246: // no cape
				c.duelSlot = 1;
				if(c.duel != null && c.duel.status == 1)
					c.duel.selectRule(c, 12);
				break;

			case 53247: // no ammy
				c.duelSlot = 2;
				if(c.duel != null && c.duel.status == 1)
					c.duel.selectRule(c, 13);
				break;

			case 53249: // no weapon.
				c.duelSlot = 3;
				if(c.duel != null && c.duel.status == 1)
					c.duel.selectRule(c, 14);
				break;

			case 53250: // no body
				c.duelSlot = 4;
				if(c.duel != null && c.duel.status == 1)
					c.duel.selectRule(c, 15);
				break;

			case 53251: // no shield
				c.duelSlot = 5;
				if(c.duel != null && c.duel.status == 1)
					c.duel.selectRule(c, 16);
				break;

			case 53252: // no legs
				c.duelSlot = 7;
				if(c.duel != null && c.duel.status == 1)
					c.duel.selectRule(c, 17);
				break;

			case 53255: // no gloves
				c.duelSlot = 9;
				if(c.duel != null && c.duel.status == 1)
					c.duel.selectRule(c, 18);
				break;

			case 53254: // no boots
				c.duelSlot = 10;
				if(c.duel != null && c.duel.status == 1)
					c.duel.selectRule(c, 19);
				break;

			case 53253: // no rings
				c.duelSlot = 12;
				if(c.duel != null && c.duel.status == 1)
					c.duel.selectRule(c, 20);
				break;

			case 53248: // no arrows
				c.duelSlot = 13;
				if(c.duel != null && c.duel.status == 1)
					c.duel.selectRule(c, 21);
				break;

			case 26018:
				if(c.duel == null || c.duel.status != 1)
					break;
				if(c.duel.player1 == null || c.duel.player2 == null){
					Duel.declineDuel(c, false);
					return;
				}

				if(c.duel.rules != null && c.duel.rules.duelRule[2] && c.duel.rules.duelRule[3] && c.duel.rules.duelRule[4]){
					c.sendMessage("You won't be able to attack the player with the rules you have set.");
					break;
				}
				c.duel.accept(c.playerId);
				if(c.duel.player1Accepted){
					c.duel.player1.getPA().sendText("Waiting for other player...", 6684);
					c.duel.player2.getPA().sendText("Other player has accepted.", 6684);
				}
				if(c.duel.player2Accepted){
					c.duel.player2.getPA().sendText("Waiting for other player...", 6684);
					c.duel.player1.getPA().sendText("Other player has accepted.", 6684);
				}

				if(c.duel.player1Accepted && c.duel.player2Accepted){
					c.duel.status = 2;
					c.duel.player1Accepted = false;
					c.duel.player2Accepted = false;
					c.duel.confirmDuel(c.duel.player1);
					c.duel.confirmDuel(c.duel.player2);
				}
				break;

			case 25120:
				if(c.duel.status != 2)
					break;
				Client o1 = c.duel.getOtherPlayer(c.playerId);
				if(c.duel.player1 == null || c.duel.player2 == null){
					Duel.declineDuel(c, false);
					return;
				}
				c.duel.accept(c.playerId);
				if(c.duel.player1Accepted && c.duel.player2Accepted){
					c.duel.startDuel(c.duel.player1, c.duel.player2, true);
					c.duel.startDuel(c.duel.player2, c.duel.player1, false);
				}else{
					c.getPA().sendText("Waiting for other player...", 6571);
					o1.getPA().sendText("Other player has accepted", 6571);
				}
				break;

			case 4169: // god spell charge
				c.usingMagic = true;
				if(!c.getCombat().checkMagicReqs(48)){
					break;
				}

				if(System.currentTimeMillis() - c.godSpellDelay < Config.GOD_SPELL_CHARGE){
					c.sendMessage("You still feel the charge in your body!");
					break;
				}
				c.godSpellDelay = System.currentTimeMillis();
				c.sendMessage("You feel charged with a magical power!");
				c.gfx100(c.MAGIC_SPELLS[48][3]);
				c.startAnimation(c.MAGIC_SPELLS[48][2]);
				c.usingMagic = false;
				break;
			case 28215:
				c.forcedText = "My Donation Points are " + c.donationPoints + ".";
				c.forcedChatUpdateRequired = true;
				c.updateRequired = true;
				break;
			case 28168: // zombie points
				c.forcedText = "My Zombie Points are " + c.zombiePoints + ".";
				c.forcedChatUpdateRequired = true;
				c.updateRequired = true;
				break;
			case 28166: // slayer points
				c.forcedText = "My Slayer Points are " + c.slayerPoints + ".";
				c.forcedChatUpdateRequired = true;
				c.updateRequired = true;
				break;
			case 28165: // pk points
				c.forcedText = "My PK Points are " + c.pkPoints + ".";
				c.forcedChatUpdateRequired = true;
				c.updateRequired = true;
				break;
			case 28164: // kill count
				c.forcedText = "My Kill Count is " + c.Rating + ".";
				c.forcedChatUpdateRequired = true;
				c.updateRequired = true;
				break;
			case 28178: // Save Game
				PlayerSave.saveGame(c);
				c.sendMessage("Your progress has been saved.");
				break;
			case 28171: // Kills and Deaths
				c.forcedText = "I have " + (int)c.kills + " kills and " + (int)c.deaths + " deaths.";
				c.forcedChatUpdateRequired = true;
				c.updateRequired = true;
				break;
			case 28170: // Kill to Death ratio.
				c.forcedText = "My Kill to Death ratio is " + Misc.decimalFormat(c.deaths == 0 ? c.kills / 1 : c.kills / c.deaths) + ".";
				c.forcedChatUpdateRequired = true;
				c.updateRequired = true;
				break;
			case 152:
				c.isRunning2 = !c.isRunning2;
				c.getPA().sendConfig(173, c.isRunning2 ? 1 : 0);
				break;
			/* QUICK PRAYERS */
			case 67050:
			case 67051:
			case 67052:
			case 67053:
			case 67054:
			case 67055:
			case 67056:
			case 67057:
			case 67058:
			case 67059:
			case 67060:
			case 67061:
			case 67062:
			case 67063:
			case 67064:
			case 67065:
			case 67066:
			case 67067:
			case 67068:
			case 67069:
			case 67070:
			case 67071:
			case 67072:
			case 67073:
			case 67074:
			case 67075:
			case 67076:
				c.getQPH().togglePrayer(actionButtonId - 66420);
				break;
			case 67080:
				c.getQPH().togglePrayer();
				break;
			case 67081:
				c.getQPH().openQuickPrayersMenu();
				break;
			case 67089:
				c.getQPH().saveQuickPrayers();
				break;
			case 9154:
				c.logout();
				break;

			case 21010:
				c.takeAsNote = true;
				break;

			case 21011:
				c.takeAsNote = false;
				break;

			case 20174:
				c.getBankPin().openSettings();
				break;
			case 58227:
				c.getPA().closeAllWindows();
				if(!c.setPin)
					c.getBankPin().open();
				else
					c.sendMessage("You already have a pin.");
				break;
			case 200003:
			case 200004:
			case 200005:
			case 200006:
			case 200007:
			case 200008:
			case 200009:
			case 200010:
			case 200011:
				if(!c.isBanking)
					return;
				int tabId = actionButtonId - 200003;
				if(tabId >= c.bank.tabs.size()){
					c.sendMessage("Please drag an item here to create a new tab.");
					break;
				}
				if(c.bank.tabs.get(tabId).getNumItems() == 0){
					c.bank.tabs.remove(tabId);
					break;
				}
				if(c.isSearching){
					c.isSearching = false;
					c.getPA().sendConfig(576, 0);
					c.getPA().closeInput();
					c.getPA().sendText("", 51237);
				}
				c.selectedTab = tabId;
				c.bank.resetBank();
				break;
			case 200019:
				c.swap = !c.swap;
				c.getPA().sendConfig(575, c.swap ? 0 : 1);
				break;
			case 200020:
				c.isSearching = !c.isSearching;
				c.getPA().sendConfig(576, c.isSearching ? 1 : 0);
				if(!c.isSearching){
					c.getPA().closeInput();
					c.bank.resetBank();
					c.getPA().sendText("", 51237);
				}
				break;
			case 200021:
				if((c.inDuelArena() && c.playerRights < 3) || (c.inWild() && c.playerRights < 3))
					return;
				if(!c.setPin){
					c.getPA().closeAllWindows();
					c.getBankPin().open();
				}else
					c.sendMessage("You already have a pin.");
				c.getPA().sendConfig(577, 0);
				break;
			case 200022:
				c.takeAsNote = !c.takeAsNote;
				c.getPA().sendConfig(578, c.takeAsNote ? 1 : 0);
				break;
			case 200032:
				c.bank.depositInventory();
				break;
			case 200033:
				c.bank.depositEquip();
				c.saveGame();
				break;
			case 72112:
				Server.clanChat.setup(c, true);
				break;
			case 117048:
				c.getPA().startTeleport(Config.START_LOCATION_X, Config.START_LOCATION_Y, 0, false);
				break;
			/**
			 * Begin Clan Wars.
			 */
			case 199085:
			case 199100:
			case 199101:
			case 199102:
			case 199103:
			case 199104:
			case 199105:
			case 199106:
			case 199107:
			case 199108:
			case 199109:
			case 199110:
			case 199111:
			case 199112:
			case 199127:
			case 199128:
			case 199129:
			case 199130:
			case 199131:
			case 199132:
			case 199133:
			case 199134:
			case 199135:
			case 199136:
			case 199150:
			case 199151:
			case 199152:
			case 199153:
			case 199154:
			case 199155:
			case 199156:
			case 199157:
			case 199158:
			case 199159:
			case 199161:
			case 199160:
				if(Server.clanChat.clans.containsKey(c.clanId))
					Server.clanChat.clans.get(c.clanId).war.handleSettingsButton(c, actionButtonId);
				break;
			/**
			 * End Clan Wars.
			 */
			// home teleports
			case 4171:
			case 50056:
				c.getPA().startTeleport(Config.START_LOCATION_X, Config.START_LOCATION_Y, 0, false);
				break;

			case 50235:
			case 4140:
			case 117112:
				// c.getPA().startTeleport(Config.LUMBY_X, Config.LUMBY_Y, 0,
				// "modern");
				c.getDH().sendOption5("Rock Crabs", "Taverly Dungeon", "Slayer Tower", "Brimhaven Dungeon", "Dragons Dungeon");

				c.teleAction = 1;
				break;

			case 4143:
			case 50245:
			case 117123:
				c.getDH().sendOption5("Barrows", "Pest Control", "TzHaar Cave", "Duel Arena", "Clan Wars");
				c.teleAction = 2;
				break;

			case 50253:
			case 117131:
			case 4146:
				c.getDH().sendOption5("Godwars", "King Black Dragon (Wild)", "Dagannoth Kings", "Chaos Elemental (Wild)", "Multi Bosses");
				c.teleAction = 3;
				break;

			case 51005:
			case 117154:
			case 4150:
				c.getDH().sendOption5("Mage Bank", "Green Dragons(13 Wild))", "East Dragons (18 Wild)", "Ardounge PVP", "Falador PVP (+0)");
				c.teleAction = 4;
				break;

			case 51013:
			case 6004:
			case 117162:
				c.getDH().sendOption5("Mining", "Smithing", "Fishing/Cooking", "Woodcutting", "Farming");
				c.teleAction = 5;
				break;

			case 117186:
			case 51023:
			case 6005:
				c.getDH().sendOption5("Lumbridge", "Varrock", "Camelot", "Falador", "Canifis");
				c.teleAction = 20;
				break;

			case 51031:
			case 29031:
				// c.getDH().sendOption5("Option 17", "Option 2", "Option 3",
				// "Option 4", "Option 5");
				// c.teleAction = 7;
				break;

			case 72038:
			case 51039:
				// c.getDH().sendOption5("Option 18", "Option 2", "Option 3",
				// "Option 4", "Option 5");
				// c.teleAction = 8;
				break;

			case 9125: // Accurate
			case 6221: // range accurate
			case 22230: // kick (unarmed)
			case 48010: // flick (whip)
			case 21200: // spike (pickaxe)
			case 1080: // bash (staff)
			case 6168: // chop (axe)
			case 6236: // accurate (long bow)
			case 17102: // accurate (darts)
			case 8234: // stab (dagger)
				c.fightMode = 0;
				if(c.autocasting)
					c.getPA().resetAutocast();
				break;

			case 9126: // Defensive
			case 48008: // deflect (whip)
			case 22228: // punch (unarmed)
			case 21201: // block (pickaxe)
			case 1078: // focus - block (staff)
			case 6169: // block (axe)
			case 33019: // fend (hally)
			case 18078: // block (spear)
			case 8235: // block (dagger)
				c.fightMode = 1;
				if(c.autocasting)
					c.getPA().resetAutocast();
				break;

			case 9127: // Controlled
			case 48009: // lash (whip)
			case 33018: // jab (hally)
			case 6234: // longrange (long bow)
			case 6219: // longrange
			case 18077: // lunge (spear)
			case 18080: // swipe (spear)
			case 18079: // pound (spear)
			case 17100: // longrange (darts)
				c.fightMode = 3;
				if(c.autocasting)
					c.getPA().resetAutocast();
				break;

			case 9128: // Aggressive
			case 6220: // range rapid
			case 22229: // block (unarmed)
			case 21203: // impale (pickaxe)
			case 21202: // smash (pickaxe)
			case 1079: // pound (staff)
			case 6171: // hack (axe)
			case 6170: // smash (axe)
			case 33020: // swipe (hally)
			case 6235: // rapid (long bow)
			case 17101: // repid (darts)
			case 8237: // lunge (dagger)
			case 8236: // slash (dagger)
				c.fightMode = 2;
				if(c.autocasting)
					c.getPA().resetAutocast();
				break;

			/** Prayers **/
			case 87231:
				if(!c.prayerActive[26] && (c.duel == null || (c.duel != null && c.duel.rules != null && (!c.duel.rules.duelRule[7] || c.duel.status != 3))) && !c.isInFala()){
					c.gfx0(2213);
					c.startAnimation(12567);
				}
				c.getCombat().activatePrayer(26);
				break;
			case 87233:
				c.getCombat().activatePrayer(27);
				break;
			case 87235:
				c.getCombat().activatePrayer(28);
				break;
			case 87237:
				c.getCombat().activatePrayer(29);
				break;
			case 87239:
				c.getCombat().activatePrayer(30);
				break;
			case 87241:
				if(!c.prayerActive[31] && (c.duel == null || (c.duel != null && c.duel.rules != null && (!c.duel.rules.duelRule[7] || c.duel.status != 3)))){
					c.gfx0(2266);
					c.startAnimation(12589);
				}
				c.getCombat().activatePrayer(31);
				break;
			case 87243:
				// c.getCombat().activatePrayer(32);
				c.sendMessage("This curse is disabled.");
				c.getPA().sendConfig(c.PRAYER_GLOW[32], 0);
				break;
			case 87245:
				c.getCombat().activatePrayer(33);
				break;
			case 87247:
				c.getCombat().activatePrayer(34);
				break;
			case 87249:
				c.getCombat().activatePrayer(35);
				break;
			case 87251:
				c.getCombat().activatePrayer(36);
				break;
			case 87253:
				c.getCombat().activatePrayer(37);
				break;
			case 87255:
				c.getCombat().activatePrayer(38);
				break;
			case 88001:
				c.getCombat().activatePrayer(39);
				break;
			case 88003:
				c.getCombat().activatePrayer(40);
				break;
			case 88005:
				// c.getCombat().activatePrayer(41);
				c.sendMessage("This curse is disabled.");
				c.getPA().sendConfig(c.PRAYER_GLOW[41], 0);
				break;
			case 88007:
				c.getCombat().activatePrayer(42);
				break;
			case 88009:
				c.getCombat().activatePrayer(43);
				break;
			case 88011:
				c.getCombat().activatePrayer(44);
				break;
			case 88013:
				if(!c.prayerActive[45] && (c.duel == null || (c.duel != null && c.duel.rules != null && (!c.duel.rules.duelRule[7] || c.duel.status != 3)))){
					c.gfx0(2226);
					c.startAnimation(12565);
				}
				c.getCombat().activatePrayer(45);
				break;
			case 21233: // thick skin
				c.getCombat().activatePrayer(0);
				break;
			case 21234: // burst of str
				c.getCombat().activatePrayer(1);
				break;
			case 21235: // charity of thought
				c.getCombat().activatePrayer(2);
				break;
			case 70080: // range
				c.getCombat().activatePrayer(3);
				break;
			case 70082: // mage
				c.getCombat().activatePrayer(4);
				break;
			case 21236: // rockskin
				c.getCombat().activatePrayer(5);
				break;
			case 21237: // super human
				c.getCombat().activatePrayer(6);
				break;
			case 21238: // improved reflexes
				c.getCombat().activatePrayer(7);
				break;
			case 21239: // hawk eye
				c.getCombat().activatePrayer(8);
				break;
			case 21240:
				c.getCombat().activatePrayer(9);
				break;
			case 21241: // protect Item
				c.getCombat().activatePrayer(10);
				break;
			case 70084: // 26 range
				c.getCombat().activatePrayer(11);
				break;
			case 70086: // 27 mage
				c.getCombat().activatePrayer(12);
				break;
			case 21242: // steel skin
				c.getCombat().activatePrayer(13);
				break;
			case 21243: // ultimate str
				c.getCombat().activatePrayer(14);
				break;
			case 21244: // incredible reflex
				c.getCombat().activatePrayer(15);
				break;
			case 21245: // protect from magic
				c.getCombat().activatePrayer(16);
				break;
			case 21246: // protect from range
				c.getCombat().activatePrayer(17);
				break;
			case 21247: // protect from melee
				c.getCombat().activatePrayer(18);
				break;
			case 70088: // 44 range
				c.getCombat().activatePrayer(19);
				break;
			case 70090: // 45 mystic
				c.getCombat().activatePrayer(20);
				break;
			case 2171: // retrui
				c.getCombat().activatePrayer(21);
				break;
			case 2172: // redem
				c.getCombat().activatePrayer(22);
				break;
			case 2173: // smite
				c.getCombat().activatePrayer(23);
				break;
			case 70092: // piety
				c.getCombat().activatePrayer(24);
				break;
			case 70094: // turmoil
				if(!c.prayerActive[25] && (c.duel == null || (c.duel != null && c.duel.rules != null && (!c.duel.rules.duelRule[7] || c.duel.status != 3)))){
					c.gfx0(2226);
					c.startAnimation(12565);
				}
				c.getCombat().activatePrayer(25);
				break;
			case 13092:
				if(System.currentTimeMillis() - c.lastButton < 400){
					c.lastButton = System.currentTimeMillis();
					break;
				}else{
					c.lastButton = System.currentTimeMillis();
				}
				Client ot = (Client)PlayerHandler.players[c.tradeWith];
				if(ot == null){
					c.getTradeAndDuel().declineTrade(true);
					c.sendMessage("Trade declined as the other player has disconnected.");
					break;
				}
				c.getPA().sendText("Waiting for other player...", 3431);
				ot.getPA().sendText("Other player has accepted", 3431);
				c.goodTrade = true;
				ot.goodTrade = true;

				for(GameItem item : c.getTradeAndDuel().offeredItems){
					if(item.id > 0){
						if(ot.inventory.freeSlots() < c.getTradeAndDuel().offeredItems.size()){
							c.sendMessage(ot.playerName + " only has " + ot.inventory.freeSlots() + " free slots, please remove " + (c.getTradeAndDuel().offeredItems.size() - ot.inventory.freeSlots()) + " items.");
							ot.sendMessage(c.playerName + " has to remove " + (c.getTradeAndDuel().offeredItems.size() - ot.inventory.freeSlots()) + " items or you could offer them " + (c.getTradeAndDuel().offeredItems.size() - ot.inventory.freeSlots()) + " items.");
							c.goodTrade = false;
							ot.goodTrade = false;
							c.getPA().sendText("Not enough inventory space...", 3431);
							ot.getPA().sendText("Not enough inventory space...", 3431);
							break;
						}else{
							c.getPA().sendText("Waiting for other player...", 3431);
							ot.getPA().sendText("Other player has accepted", 3431);
							c.goodTrade = true;
							ot.goodTrade = true;
						}
					}
				}
				if(c.inTrade && !c.tradeConfirmed && ot.goodTrade && c.goodTrade){
					c.tradeConfirmed = true;
					if(ot.tradeConfirmed){
						c.getTradeAndDuel().confirmScreen();
						ot.getTradeAndDuel().confirmScreen();
						break;
					}

				}
				break;

			case 13218:
				if(System.currentTimeMillis() - c.lastButton < 400){
					c.lastButton = System.currentTimeMillis();
					break;
				}else
					c.lastButton = System.currentTimeMillis();
				c.tradeAccepted = true;
				Client ot1 = (Client)PlayerHandler.players[c.tradeWith];
				if(ot1 == null){
					c.getTradeAndDuel().declineTrade(true);
					c.sendMessage("Trade declined as the other player has disconnected.");
					break;
				}

				if(c.inTrade && c.tradeConfirmed && ot1.tradeConfirmed && !c.tradeConfirmed2){
					c.tradeConfirmed2 = true;
					if(ot1.tradeConfirmed2){
						c.acceptedTrade = true;
						ot1.acceptedTrade = true;
						c.getTradeAndDuel().giveItems();
						break;
					}
					ot1.getPA().sendText("Other player has accepted.", 3535);
					c.getPA().sendText("Waiting for other player...", 3535);
				}
				break;
			/* Rules Interface Buttons */
			case 125011: // Click agree
				if(!c.ruleAgreeButton){
					c.ruleAgreeButton = true;
					c.getPA().sendConfig(701, 1);
				}else{
					c.ruleAgreeButton = false;
					c.getPA().sendConfig(701, 0);
				}
				break;
			case 125003:// Accept
				if(c.ruleAgreeButton){
					c.getPA().showInterface(3559);
					c.newPlayer = false;
				}else if(!c.ruleAgreeButton){
					c.sendMessage("You need to click on you agree before you can continue on.");
				}
				break;
			case 125006:// Decline
				c.sendMessage("You have chosen to decline, Client will be disconnected from the server.");
				break;
			/* End Rules Interface Buttons */
			/* Player Options */
			case 74176:
				if(!c.mouseButton){
					c.mouseButton = true;
					c.getPA().sendConfig(500, 1);
					c.getPA().sendConfig(170, 1);
				}else if(c.mouseButton){
					c.mouseButton = false;
					c.getPA().sendConfig(500, 0);
					c.getPA().sendConfig(170, 0);
				}
				break;
			case 3189:
				int sc = c.splitChat ? 0 : 1;
				c.splitChat = !c.splitChat;
				c.getPA().sendConfig(502, sc);
				c.getPA().sendConfig(287, sc);
				PlayerSave.saveGame(c);
				break;
			case 74180:
				if(!c.chatEffects){
					c.chatEffects = true;
					c.getPA().sendConfig(501, 1);
					c.getPA().sendConfig(171, 0);
				}else{
					c.chatEffects = false;
					c.getPA().sendConfig(501, 0);
					c.getPA().sendConfig(171, 1);
				}
				break;
			case 74188:
				if(!c.acceptAid){
					c.acceptAid = true;
					c.getPA().sendConfig(503, 1);
					c.getPA().sendConfig(427, 1);
				}else{
					c.acceptAid = false;
					c.getPA().sendConfig(503, 0);
					c.getPA().sendConfig(427, 0);
				}
				break;
			case 74192:
				if(!c.isRunning2){
					c.isRunning2 = true;
					c.getPA().sendConfig(504, 1);
					c.getPA().sendConfig(173, 1);
				}else{
					c.isRunning2 = false;
					c.getPA().sendConfig(504, 0);
					c.getPA().sendConfig(173, 0);
				}
				break;
			case 74201:// brightness1
				c.getPA().sendConfig(505, 1);
				c.getPA().sendConfig(506, 0);
				c.getPA().sendConfig(507, 0);
				c.getPA().sendConfig(508, 0);
				c.getPA().sendConfig(166, 1);
				break;
			case 74203:// brightness2
				c.getPA().sendConfig(505, 0);
				c.getPA().sendConfig(506, 1);
				c.getPA().sendConfig(507, 0);
				c.getPA().sendConfig(508, 0);
				c.getPA().sendConfig(166, 2);
				break;

			case 74204:// brightness3
				c.getPA().sendConfig(505, 0);
				c.getPA().sendConfig(506, 0);
				c.getPA().sendConfig(507, 1);
				c.getPA().sendConfig(508, 0);
				c.getPA().sendConfig(166, 3);
				break;

			case 74205:// brightness4
				c.getPA().sendConfig(505, 0);
				c.getPA().sendConfig(506, 0);
				c.getPA().sendConfig(507, 0);
				c.getPA().sendConfig(508, 1);
				c.getPA().sendConfig(166, 4);
				break;
			case 74206:// area1
				c.getPA().sendConfig(509, 1);
				c.getPA().sendConfig(510, 0);
				c.getPA().sendConfig(511, 0);
				c.getPA().sendConfig(512, 0);
				break;
			case 74207:// area2
				c.getPA().sendConfig(509, 0);
				c.getPA().sendConfig(510, 1);
				c.getPA().sendConfig(511, 0);
				c.getPA().sendConfig(512, 0);
				break;
			case 74208:// area3
				c.getPA().sendConfig(509, 0);
				c.getPA().sendConfig(510, 0);
				c.getPA().sendConfig(511, 1);
				c.getPA().sendConfig(512, 0);
				break;
			case 74209:// area4
				c.getPA().sendConfig(509, 0);
				c.getPA().sendConfig(510, 0);
				c.getPA().sendConfig(511, 0);
				c.getPA().sendConfig(512, 1);
				break;
			case 168:
				c.startAnimation(855);
				c.resetWalkingQueue();
				c.stopMovement();
				break;
			case 169:
				c.startAnimation(856);
				c.resetWalkingQueue();
				c.stopMovement();
				break;
			case 162:
				c.startAnimation(857);
				c.resetWalkingQueue();
				c.stopMovement();
				break;
			case 164:
				c.startAnimation(858);
				c.resetWalkingQueue();
				c.stopMovement();
				break;
			case 165:
				c.startAnimation(859);
				c.resetWalkingQueue();
				c.stopMovement();
				break;
			case 161:
				c.startAnimation(860);
				c.resetWalkingQueue();
				c.stopMovement();
				break;
			case 170:
				c.startAnimation(861);
				c.resetWalkingQueue();
				c.stopMovement();
				break;
			case 171:
				c.startAnimation(862);
				c.resetWalkingQueue();
				c.stopMovement();
				break;
			case 163:
				c.startAnimation(863);
				c.resetWalkingQueue();
				c.stopMovement();
				break;
			case 167:
				c.startAnimation(864);
				c.resetWalkingQueue();
				c.stopMovement();
				break;
			case 172:
				c.startAnimation(865);
				c.resetWalkingQueue();
				c.stopMovement();
				break;
			case 166:
				c.startAnimation(866);
				c.resetWalkingQueue();
				c.stopMovement();
				break;
			case 52050:
				c.startAnimation(2105);
				c.resetWalkingQueue();
				c.stopMovement();
				break;
			case 52051:
				c.startAnimation(2106);
				c.resetWalkingQueue();
				c.stopMovement();
				break;
			case 52052:
				c.startAnimation(2107);
				c.resetWalkingQueue();
				c.stopMovement();
				break;
			case 52053:
				c.startAnimation(2108);
				c.resetWalkingQueue();
				c.stopMovement();
				break;
			case 52054:
				c.startAnimation(2109);
				c.resetWalkingQueue();
				c.stopMovement();
				break;
			case 52055:
				c.startAnimation(2110);
				c.resetWalkingQueue();
				c.stopMovement();
				break;
			case 52056:
				c.startAnimation(2111);
				c.resetWalkingQueue();
				c.stopMovement();
				break;
			case 52057:
				c.startAnimation(2112);
				c.resetWalkingQueue();
				c.stopMovement();
				break;
			case 52058:
				c.startAnimation(2113);
				c.resetWalkingQueue();
				c.stopMovement();
				break;
			case 43092:
				c.startAnimation(0x558);
				c.resetWalkingQueue();
				c.stopMovement();
				break;
			case 2155:
				c.startAnimation(0x46B);
				c.resetWalkingQueue();
				c.stopMovement();
				break;
			case 25103:
				c.startAnimation(0x46A);
				c.resetWalkingQueue();
				c.stopMovement();
				break;
			case 25106:
				c.startAnimation(0x469);
				c.resetWalkingQueue();
				c.stopMovement();
				break;
			case 2154:
				c.startAnimation(0x468);
				c.resetWalkingQueue();
				c.stopMovement();
				break;
			case 52071:
				c.startAnimation(0x84F);
				c.resetWalkingQueue();
				c.stopMovement();
				break;
			case 52072:
				c.startAnimation(0x850);
				c.resetWalkingQueue();
				c.stopMovement();
				break;
			case 59062:
				c.startAnimation(2836);
				c.resetWalkingQueue();
				c.stopMovement();
				break;
			case 73076:
				c.startAnimation(3544);
				c.resetWalkingQueue();
				c.stopMovement();
				break;
			case 73077:
				c.startAnimation(3543);
				c.resetWalkingQueue();
				c.stopMovement();
				break;
			case 72254:
				c.resetWalkingQueue();
				c.stopMovement();
				c.skillCapeEmote();
				break;
			/* END OF EMOTES */
			case 118098:
				c.getPA().castVeng();
				break;
			case 33207:
				c.forcedText = "My Hitpoints level is  " + c.getPA().getLevelForXP(c.playerXP[3]) + ".";
				c.forcedChatUpdateRequired = true;
				c.updateRequired = true;
				break;
			case 33218:
				c.forcedText = "My Prayer level is  " + c.getPA().getLevelForXP(c.playerXP[5]) + ".";
				c.forcedChatUpdateRequired = true;
				c.updateRequired = true;
				break;
			case 33206:
				c.forcedText = "My Attack level is  " + c.getPA().getLevelForXP(c.playerXP[0]) + ".";
				c.forcedChatUpdateRequired = true;
				c.updateRequired = true;
				break;
			case 33212:
				c.forcedText = "My Defence level is  " + c.getPA().getLevelForXP(c.playerXP[1]) + ".";
				c.forcedChatUpdateRequired = true;
				c.updateRequired = true;
				break;
			case 33209:
				c.forcedText = "My Strength level is  " + c.getPA().getLevelForXP(c.playerXP[2]) + ".";
				c.forcedChatUpdateRequired = true;
				c.updateRequired = true;
				break;
			case 33215:
				c.forcedText = "My Ranged level is  " + c.getPA().getLevelForXP(c.playerXP[4]) + ".";
				c.forcedChatUpdateRequired = true;
				c.updateRequired = true;
				break;
			case 33221:
				c.forcedText = "My Magic level is  " + c.getPA().getLevelForXP(c.playerXP[6]) + ".";
				c.forcedChatUpdateRequired = true;
				c.updateRequired = true;
				break;
			case 47130:
				String slayer = (c.slayerTask == null ? "I should speak to Duradel for a new task." : "I must slay another " + c.slayerTask.amount + " " + Server.npcHandler.getNpcListName(c.slayerTask.monster).replace("_", " ") + (c.slayerTask.amount > 1 ? "s" : "") + ".");
				c.forcedText = slayer;
				c.forcedChatUpdateRequired = true;
				c.updateRequired = true;
				break;
			case 33210:
				c.forcedText = "My Agility level is  " + c.getPA().getLevelForXP(c.playerXP[16]) + ".";
				c.forcedChatUpdateRequired = true;
				c.updateRequired = true;
				break;
			case 33217:
				c.forcedText = "My Cooking level is  " + c.getPA().getLevelForXP(c.playerXP[7]) + ".";
				c.forcedChatUpdateRequired = true;
				c.updateRequired = true;
				break;
			case 33223:
				c.forcedText = "My Woodcutting level is  " + c.getPA().getLevelForXP(c.playerXP[8]) + ".";
				c.forcedChatUpdateRequired = true;
				c.updateRequired = true;
				break;
			case 33222:
				c.forcedText = "My Fletching level is  " + c.getPA().getLevelForXP(c.playerXP[9]) + ".";
				c.forcedChatUpdateRequired = true;
				c.updateRequired = true;
				break;
			case 33214:
				c.forcedText = "My Fishing level is  " + c.getPA().getLevelForXP(c.playerXP[10]) + ".";
				c.forcedChatUpdateRequired = true;
				c.updateRequired = true;
				break;
			case 33220:
				c.forcedText = "My Firemaking level is  " + c.getPA().getLevelForXP(c.playerXP[11]) + ".";
				c.forcedChatUpdateRequired = true;
				c.updateRequired = true;
				break;
			case 33219:
				c.forcedText = "My Crafting level is  " + c.getPA().getLevelForXP(c.playerXP[12]) + ".";
				c.forcedChatUpdateRequired = true;
				c.updateRequired = true;
				break;
			case 33211:
				c.forcedText = "My Smithing level is  " + c.getPA().getLevelForXP(c.playerXP[13]) + ".";
				c.forcedChatUpdateRequired = true;
				c.updateRequired = true;
				break;
			case 33208:
				c.forcedText = "My Mining level is  " + c.getPA().getLevelForXP(c.playerXP[14]) + ".";
				c.forcedChatUpdateRequired = true;
				c.updateRequired = true;
				break;
			case 33213:
				c.forcedText = "My Herblore level is  " + c.getPA().getLevelForXP(c.playerXP[15]) + ".";
				c.forcedChatUpdateRequired = true;
				c.updateRequired = true;
				break;
			case 33216:
				c.forcedText = "My Thieving level is  " + c.getPA().getLevelForXP(c.playerXP[17]) + ".";
				c.forcedChatUpdateRequired = true;
				c.updateRequired = true;
				break;
			case 54104:
				c.forcedText = "My Farming level is  " + c.getPA().getLevelForXP(c.playerXP[19]) + ".";
				c.forcedChatUpdateRequired = true;
				c.updateRequired = true;
				break;
			case 33224:
				c.forcedText = "My Runecrafting level is  " + c.getPA().getLevelForXP(c.playerXP[20]) + ".";
				c.forcedChatUpdateRequired = true;
				c.updateRequired = true;
				break;
			case 47069:
			case 7212:
				c.setSidebarInterface(0, 328);
			case 24017:
				c.getPA().resetAutocast();
				// c.sendFrame246(329, 200, c.playerEquipment[c.playerWeapon]);
				c.getItems().sendWeapon(c.playerEquipment[c.playerWeapon], c.getItems().getItemName(c.playerEquipment[c.playerWeapon]));
				// c.setSidebarInterface(0, 328);
				// c.setSidebarInterface(6, c.playerMagicBook == 0 ? 1151 :
				// c.playerMagicBook == 1 ? 12855 : 1151);
				break;
		}
		if(c.isAutoButton(actionButtonId))
			c.assignAutocast(actionButtonId);
	}
}