package server.model.players;

import server.Config;
import server.Server;
import server.clip.region.Region;
import server.model.minigames.CastleWarObjects;
import server.model.minigames.CastleWars;
import server.model.minigames.FightPits;
import server.model.minigames.NexGames;
import server.model.npcs.NPCHandler;
import server.model.objects.Object;
import server.model.players.skills.fishing.Spot;
import server.model.quests.DemonSlayer;
import server.model.quests.QuestHandler;
import server.util.Misc;
import server.util.ScriptManager;
import server.world.Clan;

/**
 * 
 * @author hadesflames
 */
public class ActionHandler{

	private Client c;

	public ActionHandler(Client Client){
		this.c = Client;
	}
	
	private boolean canBank(int x, int y){
		for(int pos[] : Config.APPROVED_BANK_COORDS)
			if(pos[0] == x && pos[1] == y)
				return true;
		return false;
	}

	public void handleSpiralStairCase(int objectType, int obX, int obY){
		if(c.absX >= 2728 && c.absX <= 2730 && c.absY >= 3460 && c.absY <= 3462 && c.heightLevel == 0){
			c.teleportToX = 2729;
			c.teleportToY = 3462;
			c.heightLevel = 1;
		}else if(c.absX >= 2745 && c.absX <= 2747 && c.absY >= 3460 && c.absY <= 3462 && c.heightLevel == 0){
			c.teleportToX = 2745;
			c.teleportToY = 3461;
			c.heightLevel = 1;
		}else if(c.playerRights == 3)
			System.out.println("objectId: " + objectType + " objectX: " + obX + " objectY: " + obY);
	}
	
	public void handleSpiralDown(int objectType, int obX, int obY){
		if(c.absX == 2745 && c.absY >= 3460 && c.absY <= 3462 && c.heightLevel == 1){
			c.teleportToX = 2746;
			c.teleportToY = 3462;
			c.heightLevel = 0;
		}else if(c.absY == 3462 && c.absX >= 2728 && c.absX <= 2730 && c.heightLevel == 1){
			c.teleportToX = 2730;
			c.teleportToY = 3461;
			c.heightLevel = 0;
		}else if(c.playerRights == 3)
			System.out.println("objectId: " + objectType + " objectX: " + obX + " objectY: " + obY);
	}
	
	public static boolean ignoreObjectCheck(int id){
		int objects[] = {6, 7, 8, 9};
		for(int object : objects)
			if(object == id)
				return true;
		return false;
	}
	
	public static boolean multiZObject(int id){
		int objects[] = {75, 788, 789, 4483, 5159, 5160, 5168, 9357};
		for(int object : objects)
			if(object == id)
				return true;
		return false;
	}
	
	public void firstClickObject(int objectType, int obX, int obY){
		if(!ignoreObjectCheck(c.objectId)){
			if(!Region.objectExists(c.objectId, c.objectX, c.objectY, c.heightLevel) && !multiZObject(c.objectId))
				return;
			if(multiZObject(c.objectId) && !Region.objectExists(c.objectId, c.objectX, c.objectY, 0)){
				System.out.println("test");
				return;
			}
		}
		c.clickObjectType = 0;
		switch(objectType){
			case 2492:
				if(c.killCount >= 20){
					c.getDH().sendOption4("Armadyl", "Bandos", "Saradomin", "Zamorak");
					c.dialogueAction = 20;
				}else{
					c.sendMessage("You need 20 kill count before teleporting to a boss chamber.");
				}
				break;
			case 6:
				c.getCannon().fireCannon(obX, obY);
				break;
			case 7:
			case 8:
			case 9:
				c.getCannon().pickupCannon(objectType, obX, obY);
				break;
			case 788:
			case 789:
				if(c.nexGames != null)
					c.nexGames.beginFinalNex(c);
				break;
			case 6434:
				c.getPA().movePlayer(2004, 4426, 1);
				break;
			/*case 10686:
				if(c.absX >= 1990 && c.absX <= 2025 && c.absY >= 4425 && c.absY <= 4450 && c.heightLevel == 0 && c.inventory.hasItem(6875, 1)){
					c.inventory.deleteItem(6875, 1);
					c.inventory.addItem(6876, 1);
				}
				break;
			case 10687:
				if(c.absX >= 1990 && c.absX <= 2025 && c.absY >= 4425 && c.absY <= 4450 && c.heightLevel == 0 && c.inventory.freeSlots() > 0){
					if(!c.inventory.hasItem(6875) && !c.inventory.hasItem(6876) && !c.inventory.hasItem(6877) && !c.inventory.hasItem(6878))
						c.inventory.addItem(6875, 1);
					else
						c.sendMessage("You already have a blue puppet.");
				}
				break;
			case 10688:
				if(c.absX >= 1990 && c.absX <= 2025 && c.absY >= 4425 && c.absY <= 4450 && c.heightLevel == 0 && c.inventory.hasItem(6876, 1)){
					c.inventory.deleteItem(6876, 1);
					c.inventory.addItem(6877, 1);
				}
				break;
			case 10689:
				if(c.absX >= 1990 && c.absX <= 2025 && c.absY >= 4425 && c.absY <= 4450 && c.heightLevel == 0 && c.inventory.hasItem(6877, 1)){
					c.inventory.deleteItem(6877, 1);
					c.inventory.addItem(6878, 1);
				}
				break;
			case 10690:
				if(c.absX >= 1990 && c.absX <= 2025 && c.absY >= 4425 && c.absY <= 4450 && c.heightLevel == 0 && c.inventory.hasItem(6871, 1)){
					c.inventory.deleteItem(6871, 1);
					c.inventory.addItem(6872, 1);
				}
				break;
			case 10691:
				if(c.absX >= 1990 && c.absX <= 2025 && c.absY >= 4425 && c.absY <= 4450 && c.heightLevel == 0 && c.inventory.freeSlots() > 0){
					if(!c.inventory.hasItem(6871) && !c.inventory.hasItem(6872) && !c.inventory.hasItem(6873) && !c.inventory.hasItem(6874))
						c.inventory.addItem(6871, 1);
					else
						c.sendMessage("You already have a red puppet.");
				}
				break;
			case 10692:
				if(c.absX >= 1990 && c.absX <= 2025 && c.absY >= 4425 && c.absY <= 4450 && c.heightLevel == 0 && c.inventory.hasItem(6872, 1)){
					c.inventory.deleteItem(6872, 1);
					c.inventory.addItem(6873, 1);
				}
				break;
			case 10693:
				if(c.absX >= 1990 && c.absX <= 2025 && c.absY >= 4425 && c.absY <= 4450 && c.heightLevel == 0 && c.inventory.hasItem(6873, 1)){
					c.inventory.deleteItem(6873, 1);
					c.inventory.addItem(6874, 1);
				}
				break;
			case 10694:
				if(c.absX >= 1990 && c.absX <= 2025 && c.absY >= 4425 && c.absY <= 4450 && c.heightLevel == 0 && c.inventory.hasItem(6879, 1)){
					c.inventory.deleteItem(6879, 1);
					c.inventory.addItem(6880, 1);
				}
				break;
			case 10695:
				if(c.absX >= 1990 && c.absX <= 2025 && c.absY >= 4425 && c.absY <= 4450 && c.heightLevel == 0 && c.inventory.freeSlots() > 0){
					if(!c.inventory.hasItem(6879) && !c.inventory.hasItem(6880) && !c.inventory.hasItem(6881) && !c.inventory.hasItem(6882))
						c.inventory.addItem(6879, 1);
					else
						c.sendMessage("You already have a green puppet.");
				}
				break;
			case 10696:
				if(c.absX >= 1990 && c.absX <= 2025 && c.absY >= 4425 && c.absY <= 4450 && c.heightLevel == 0 && c.inventory.hasItem(6880, 1)){
					c.inventory.deleteItem(6880, 1);
					c.inventory.addItem(6881, 1);
				}
				break;
			case 10697:
				if(c.absX >= 1990 && c.absX <= 2025 && c.absY >= 4425 && c.absY <= 4450 && c.heightLevel == 0 && c.inventory.hasItem(6881, 1)){
					c.inventory.deleteItem(6881, 1);
					c.inventory.addItem(6882, 1);
				}
				break;*/
			case 10699:
				for(int i = 6868; i<=6882; i++)
					if(c.inventory.hasItem(i))
						c.inventory.deleteItem(i, c.inventory.getItemCount(i));
				c.getPA().movePlayer(3084, 3271, 0);
				break;
			case 10707:
				c.getPA().movePlayer(obX == 2006 ? 2005 : 2010, 4431, 1);
				break;
			case 10708:
				c.getPA().movePlayer(obX == 2006 ? 2007 : 2008, 4431, 0);
				break;
			case 3192:
				Server.pkScoreBoard.showBoard(c);
				break;
			case 75:
				if(c.nexGames != null)
					c.nexGames.handleNexVictory(c);
				break;
			case 1738:
				handleSpiralStairCase(objectType, obX, obY);
				break;
			case 1740:
				handleSpiralDown(objectType, obX, obY);
				break;
			case 28214:
			case 28140:
				if(!c.inClanWars || c.clanId.isEmpty() || !Server.clanChat.clans.containsKey(c.clanId) || c.exitDelay > Misc.currentTimeSeconds())
					break;
				c.getPA().movePlayer(3272, 3692, 0);
				c.getPA().walkableInterface(-1);
				c.getPA().showInterface(18725);
				c.clanWarsWalkInterface = -1;
				c.inClanWars = false;
				c.exitDelay = Misc.currentTimeSeconds() + 5;
				c.inCWJail = false;
				c.cwJailTime = -1;
				Clan clan = Server.clanChat.clans.get(c.clanId);
				if(objectType == 28214){
					clan.warMembersLeft--;
					clan.war.getOpposingTeam(clan).kills++;
				}
				break;
			case 28213:
				if(obY > 3690)
					System.out.println("North");
				else
					System.out.println("South");
				break;
			case 8987:
				c.getPA().startTeleport(Config.START_LOCATION_X, Config.START_LOCATION_Y, 0, true);
				break;
			case 4483:
				if(canBank(obX, obY))
					c.canBank = true;
				if((obX == 3248 && obY == 9354) && (c.nexGames == null || !c.nexGames.canBank))
					c.canBank = false;
				c.getPA().openUpBank();
				break;
			case 2469:
				c.getPA().startTeleport2(3507, 9494, 0);
				break;
			case 2465:
				c.resetNex();
				new NexGames(c);
				break;
			case 1765:
				c.getPA().movePlayer(2271, 4680, 0);
				break;
			case 1766:
				c.getPA().movePlayer(3016, 3849, 0);
				break;
			case 1817:
				c.getPA().startTeleport(3067, 10253, 0, true);
				break;
			case 2882:
			case 2883:
				if(c.objectX == 3268){
					if(c.absX < c.objectX){
						c.getPA().walkTo(1, 0);
					}else{
						c.getPA().walkTo(-1, 0);
					}
				}
				break;
			case 61:
				if(c.absX < 3084 || c.absX > 3085 || c.absY < 3488 || c.absY > 3491)
					break;
				if(c.cursesActive){
					c.sendMessage("You feel a drain on your memory.");
					c.setSidebarInterface(5, 5608);
					c.cursesActive = false;
					c.hasCurses = true;
					c.getQPH().resetQuickPrayers();
					c.getCombat().resetPrayers();
					if(c.pkPoints < 0)
						c.pkPoints = 0;
					PlayerSave.saveGame(c);
				}else if(c.hasCurses || c.playerRights > 1 && c.playerRights != 5){
					c.hasCurses = true;
					c.sendMessage("An ancient power fills your memory.");
					c.setSidebarInterface(5, 22500);
					c.cursesActive = true;
					c.getQPH().resetQuickPrayers();
					c.getCombat().resetPrayers();
					if(c.pkPoints < 0)
						c.pkPoints = 0;
					PlayerSave.saveGame(c);
				}else{
					if(c.pkPoints >= 250){
						c.pkPoints -= 250;
						c.hasCurses = true;
						c.sendMessage("An ancient power fills your memory.");
						c.setSidebarInterface(5, 22500);
						c.cursesActive = true;
						c.getQPH().resetQuickPrayers();
						c.getCombat().resetPrayers();
						PlayerSave.saveGame(c);
					}else{
						if(c.pkPoints < 0)
							c.pkPoints = 0;
						c.sendMessage("You need 250 PK Points to switch to Ancient Curses.");
					}
				}
				break;
			case 5159:
			case 5160: // Nomad bridge.
				if(!c.inZombiesGame && (c.playerRights < 2 || c.playerRights == 5))
					return;
				if(c.playerLevel[c.playerAgility] < 75){
					c.sendMessage("You need an agility level of at least 75 to jump over this bridge.");
					break;
				}
				c.zombieTemp = true;
				if(c.absY < 3560)
					c.getPA().movePlayer(c.absX, 3562, 4 * c.playerId);
				c.zombieTemp = false;
				break;
			case 5168: // Nomad.
				// hp, maxhit, attack, defense
				if(!c.canSummonNomad)
					return;
				c.canSummonNomad = false;
				if(c.inZombiesGame && c.zombies != null)
					c.zombies.handleNomadSummon(c);
				int nomad_stats[] = {1000, 35, 600, 350};
				Server.npcHandler.spawnNpc(c, 2001, 3504, 3575, c.heightLevel, 0, nomad_stats[0], nomad_stats[1], nomad_stats[2], nomad_stats[3], true, false);
				break;
			// Start Castle Wars.
			case 4411:
			case 4415:
			case 4417:
			case 4418:
			case 4419:
			case 4420:
			case 4469:
			case 4470:
			case 4911:
			case 4912:
			case 1747:
			case 1757:
			case 4437:
			case 6281:
			case 6280:
			case 4472:
			case 4471:
			case 4406:
			case 4407:
			case 4458:
			case 4902:
			case 4903:
			case 4900:
			case 4901:
			case 4461:
			case 4463:
			case 4464:
			case 4377:
			case 4378:
			case 1568:
				CastleWarObjects.handleObject(c, objectType, obX, obY);
				break;
			// End Castle Wars.
			
			case 272:
				c.getPA().movePlayer(c.absX, c.absY, 1);
				break;
			case 273:
				c.getPA().movePlayer(c.absX, c.absY, 0);
				break;
			case 245:
				c.getPA().movePlayer(c.absX, c.absY + 2, 2);
				break;
			case 246:
				c.getPA().movePlayer(c.absX, c.absY - 2, 1);
				break;
			case 410:
				c.getPA().resetAutocast();
				if(c.playerMagicBook == Config.MODERN || c.playerMagicBook == Config.ANCIENT){
					c.playerMagicBook = Config.LUNAR;
					c.setSidebarInterface(6, 29999);
					c.sendMessage("A Lunar wisdom fills your mind.");
					c.getPA().resetAutocast();
					c.setSidebarInterface(0, 328);
				}else{
					c.setSidebarInterface(6, 1151); // modern
					c.playerMagicBook = Config.MODERN;
					c.sendMessage("You feel a drain on your memory.");
					c.autocastId = -1;
					c.getPA().resetAutocast();
					c.setSidebarInterface(0, 328);
				}
				c.getItems().sendWeapon(c.playerEquipment[c.playerWeapon], c.getItems().getItemName(c.playerEquipment[c.playerWeapon]));
				break;
			case 6552:
				c.getPA().resetAutocast();
				if(c.playerMagicBook == Config.MODERN || c.playerMagicBook == Config.LUNAR){
					c.playerMagicBook = Config.ANCIENT;
					c.setSidebarInterface(6, 12855);
					c.sendMessage("An ancient wisdomin fills your mind.");
					c.getPA().resetAutocast();
					c.setSidebarInterface(0, 328);
				}else{
					c.setSidebarInterface(6, 1151); // modern
					c.playerMagicBook = Config.MODERN;
					c.sendMessage("You feel a drain on your memory.");
					c.autocastId = -1;
					c.getPA().resetAutocast();
					c.setSidebarInterface(0, 328);
				}
				c.getItems().sendWeapon(c.playerEquipment[c.playerWeapon], c.getItems().getItemName(c.playerEquipment[c.playerWeapon]));
				break;
			case 1816:
				c.getPA().startTeleport2(2271, 4680, 0);
				break;
			case 1814:
				// ardy lever
				c.getPA().startTeleport(3153, 3923, 0, true);
				break;
			case 1815:
				c.getPA().startTeleport2(2561, 3311, 0);
				break;
			case 1530:
				c.getPA().movePlayer(c.absX <= 2563 ? 2564 : 2563, 3310, 0);
				break;
			case 9356:
				c.getPA().enterCaves();
				break;
			case 9357:
				c.getPA().resetTzhaar();
				break;

			case 8959:
				if(c.getX() == 2490 && (c.getY() == 10146 || c.getY() == 10148)){
					if(c.getPA().checkForPlayer(2490, c.getY() == 10146 ? 10148 : 10146)){
						new Object(6951, c.objectX, c.objectY, c.heightLevel, 1, 10, 8959, 15);
					}
				}
				break;

			case 2213:
				if(c.inCanifis()){
					c.sendMessage("The bank seems to be closed...");
					break;
				}
			case 14367:
			case 11758:
			case 3193:
				if(canBank(obX, obY))
					c.canBank = true;
				c.getPA().openUpBank();
				break;

			case 10230:
				c.getPA().movePlayer(2900, 4449, 0);
				break;
			case 10229:
				c.getPA().movePlayer(1912, 4367, 0);
				break;
			case 2623:
				if(c.absX >= c.objectX)
					c.getPA().walkTo(-1, 0);
				else
					c.getPA().walkTo(1, 0);
				break;
			// pc boat
			case 14314:
				c.getPA().movePlayer(2657, 2639, 0);
				break;
			case 14315:
				/*if(c.teleporting)
					return;
				if(c.combatLevel < 40){
					c.sendMessage("You must be at least 40 to enter this boat.");
					return;
				}
				c.getPA().movePlayer(2661, 2639, 0);
				synchronized(Server.pestControl){
					Server.pestControl.playersInBoat.add(c.playerId);
				}*/
				break;
			case 14296:
				/*c.pestTemp = true;
				c.startAnimation(828);
				if(c.absY > 2597){
					if(c.absX > 2666)
						c.getPA().movePlayer(c.absX < 2669 ? 2670 : 2668, 2601, 0);
					else
						c.getPA().movePlayer(c.absX < 2644 ? 2645 : 2643, 2601, 0);
				}else{
					if(c.absX > 2660)
						c.getPA().movePlayer(2666, c.absY < 2586 ? 2587 : 2585, 0);
					else
						c.getPA().movePlayer(2647, c.absY < 2586 ? 2587 : 2585, 0);
				}
				c.pestTemp = false;*/
				break;

			case 1596:
			case 1597:
				if(c.absY == 3450 || c.absY == 3451){
					if(c.getX() >= 2936)
						c.getPA().walkTo(-1, 0);
					else
						c.getPA().walkTo(1, 0);
				}else if(c.absY == 3849 || c.absY == 3850){
					if(c.getX() <= 3007)
						c.getPA().walkTo(1, 0);
					else
						c.getPA().walkTo(-1,  0);
				}
				break;
			case 14235:
			case 14233:
				// Pest control shit.
				/*if(c.objectX == 2670)
					if(c.absX <= 2670)
						c.absX = 2671;
					else
						c.absX = 2670;
				if(c.objectX == 2643)
					if(c.absX >= 2643)
						c.absX = 2642;
					else
						c.absX = 2643;
				if(c.absX <= 2585)
					c.absY += 1;
				else
					c.absY -= 1;
				c.pestTemp = true;
				c.getPA().movePlayer(c.absX, c.absY, 0);
				c.pestTemp = false;*/
				break;

			case 14829:
			case 14830:
			case 14827:
			case 14828:
			case 14826:
			case 14831:
				// Server.objectHandler.startObelisk(objectType);
				Server.objectManager.startObelisk(objectType);
				break;
			
			// Start Castle Wars.
			case 4387:
				if(c.teleporting)
					return;
				CastleWars.addToWaitRoom(c, 1); // saradomin
				break;
			case 4388:
				if(c.teleporting)
					return;
				CastleWars.addToWaitRoom(c, 2); // zamorak
				break;
			case 4408:
				if(c.teleporting)
					return;
				CastleWars.addToWaitRoom(c, 3); // guthix
				break;
			case 4389: // Sara wating room portal
			case 4390: // Zammy waiting room portal
				if(c.teleporting)
					return;
				CastleWars.leaveWaitingRoom(c);
				break;
			// End Castle Wars.
			case 9369:
				FightPits.handleEntrance(c);
				break;

			case 9368:
				if(c.getY() < 5169){
					Server.fightPits.removePlayerFromPits(c.playerId);
					c.getPA().movePlayer(2399, 5177, 0);
				}
				break;

			case 2286:
			case 154:
			case 4058:
			case 2295:
			case 2285:
			case 2313:
			case 2312:
			case 2314:
				c.getAgility().handleGnomeCourse(objectType, obX, obY);
				break;

			// barrows
			// Chest
			case 10284:
				if(c.barrowsKillCount < 5){
					c.sendMessage("You haven't killed all the brothers.");
				}
				if(c.barrowsKillCount == 5 && c.barrowsNpcs[c.randomCoffin][1] == 1){
					c.sendMessage("I have already summoned this npc.");
				}
				if(c.barrowsNpcs[c.randomCoffin][1] == 0 && c.barrowsKillCount >= 5){
					Server.npcHandler.spawnNpc(c, c.barrowsNpcs[c.randomCoffin][0], 3551, 9694 - 1, 0, 0, 120, 30, 200, 200, true, true);
					c.barrowsNpcs[c.randomCoffin][1] = 1;
				}
				if((c.barrowsKillCount > 5 || c.barrowsNpcs[c.randomCoffin][1] == 2) && c.inventory.freeSlots() >= 2){
					c.getPA().resetBarrows();
					c.inventory.addItem(c.getPA().randomRunes(), Misc.random(150) + 100, -1);
					if(Misc.random(3) == 1)
						c.inventory.addItem(c.getPA().randomBarrows(), 1, -1);
					c.getPA().startTeleport(3564, 3288, 0, true);
				}else if(c.barrowsKillCount > 5 && c.inventory.freeSlots() <= 1){
					c.sendMessage("You need at least 2 inventory slot opened.");
				}
				break;
			// doors
			case 6749:
				if(obX == 3562 && obY == 9678){
					c.getPA().object(3562, 9678, 6749, -3, 0);
					c.getPA().object(3562, 9677, 6730, -1, 0);
				}else if(obX == 3558 && obY == 9677){
					c.getPA().object(3558, 9677, 6749, -1, 0);
					c.getPA().object(3558, 9678, 6730, -3, 0);
				}
				break;
			case 6730:
				if(obX == 3558 && obY == 9677){
					c.getPA().object(3562, 9678, 6749, -3, 0);
					c.getPA().object(3562, 9677, 6730, -1, 0);
				}else if(obX == 3558 && obY == 9678){
					c.getPA().object(3558, 9677, 6749, -1, 0);
					c.getPA().object(3558, 9678, 6730, -3, 0);
				}
				break;
			case 6727:
				if(obX == 3551 && obY == 9684){
					c.sendMessage("You cant open this door..");
				}
				break;
			case 6746:
				if(obX == 3552 && obY == 9684){
					c.sendMessage("You cant open this door..");
				}
				break;
			case 6748:
				if(obX == 3545 && obY == 9678){
					c.getPA().object(3545, 9678, 6748, -3, 0);
					c.getPA().object(3545, 9677, 6729, -1, 0);
				}else if(obX == 3541 && obY == 9677){
					c.getPA().object(3541, 9677, 6748, -1, 0);
					c.getPA().object(3541, 9678, 6729, -3, 0);
				}
				break;
			case 6729:
				if(obX == 3545 && obY == 9677){
					c.getPA().object(3545, 9678, 6748, -3, 0);
					c.getPA().object(3545, 9677, 6729, -1, 0);
				}else if(obX == 3541 && obY == 9678){
					c.getPA().object(3541, 9677, 6748, -1, 0);
					c.getPA().object(3541, 9678, 6729, -3, 0);
				}
				break;
			case 6726:
				if(obX == 3534 && obY == 9684){
					c.getPA().object(3534, 9684, 6726, -4, 0);
					c.getPA().object(3535, 9684, 6745, -2, 0);
				}else if(obX == 3535 && obY == 9688){
					c.getPA().object(3535, 9688, 6726, -2, 0);
					c.getPA().object(3534, 9688, 6745, -4, 0);
				}
				break;
			case 6745:
				if(obX == 3535 && obY == 9684){
					c.getPA().object(3534, 9684, 6726, -4, 0);
					c.getPA().object(3535, 9684, 6745, -2, 0);
				}else if(obX == 3534 && obY == 9688){
					c.getPA().object(3535, 9688, 6726, -2, 0);
					c.getPA().object(3534, 9688, 6745, -4, 0);
				}
				break;
			case 6743:
				if(obX == 3545 && obY == 9695){
					c.getPA().object(3545, 9694, 6724, -1, 0);
					c.getPA().object(3545, 9695, 6743, -3, 0);
				}else if(obX == 3541 && obY == 9694){
					c.getPA().object(3541, 9694, 6724, -1, 0);
					c.getPA().object(3541, 9695, 6743, -3, 0);
				}
				break;
			case 6724:
				if(obX == 3545 && obY == 9694){
					c.getPA().object(3545, 9694, 6724, -1, 0);
					c.getPA().object(3545, 9695, 6743, -3, 0);
				}else if(obX == 3541 && obY == 9695){
					c.getPA().object(3541, 9694, 6724, -1, 0);
					c.getPA().object(3541, 9695, 6743, -3, 0);
				}
				break;
			// end doors
			// coffins
			case 6707: // verac
				c.getPA().movePlayer(3556, 3298, 0);
				break;

			case 6823:
				if(server.model.minigames.Barrows.selectCoffin(c, objectType)){
					return;
				}
				if(c.barrowsNpcs[0][1] == 0){
					Server.npcHandler.spawnNpc(c, 2030, c.getX(), c.getY() - 1, 3, 0, 120, 25, 200, 200, true, true);
					c.barrowsNpcs[0][1] = 1;
				}else{
					c.sendMessage("You have already searched in this sarcophagus.");
				}
				break;

			case 6706: // torag
				c.getPA().movePlayer(3553, 3283, 0);
				break;

			case 6772:
				if(server.model.minigames.Barrows.selectCoffin(c, objectType)){
					return;
				}
				if(c.barrowsNpcs[1][1] == 0){
					Server.npcHandler.spawnNpc(c, 2029, c.getX() + 1, c.getY(), 3, 0, 120, 20, 200, 200, true, true);
					c.barrowsNpcs[1][1] = 1;
				}else{
					c.sendMessage("You have already searched in this sarcophagus.");
				}
				break;

			case 6705: // karil stairs
				c.getPA().movePlayer(3565, 3276, 0);
				break;
			case 6822:
				if(server.model.minigames.Barrows.selectCoffin(c, objectType)){
					return;
				}
				if(c.barrowsNpcs[2][1] == 0){
					Server.npcHandler.spawnNpc(c, 2028, c.getX(), c.getY() - 1, 3, 0, 90, 17, 200, 200, true, true);
					c.barrowsNpcs[2][1] = 1;
				}else{
					c.sendMessage("You have already searched in this sarcophagus.");
				}
				break;

			case 6704: // guthan stairs
				c.getPA().movePlayer(3578, 3284, 0);
				break;
			case 6773:
				if(server.model.minigames.Barrows.selectCoffin(c, objectType)){
					return;
				}
				if(c.barrowsNpcs[3][1] == 0){
					Server.npcHandler.spawnNpc(c, 2027, c.getX(), c.getY() - 1, 3, 0, 120, 23, 200, 200, true, true);
					c.barrowsNpcs[3][1] = 1;
				}else{
					c.sendMessage("You have already searched in this sarcophagus.");
				}
				break;

			case 6703: // dharok stairs
				c.getPA().movePlayer(3574, 3298, 0);
				break;
			case 6771:
				if(server.model.minigames.Barrows.selectCoffin(c, objectType)){
					return;
				}
				if(c.barrowsNpcs[4][1] == 0){
					Server.npcHandler.spawnNpc(c, 2026, c.getX(), c.getY() - 1, 3, 0, 120, 45, 250, 250, true, true);
					c.barrowsNpcs[4][1] = 1;
				}else{
					c.sendMessage("You have already searched in this sarcophagus.");
				}
				break;

			case 6702: // ahrim stairs
				c.getPA().movePlayer(3565, 3290, 0);
				break;
			case 6821:
				if(server.model.minigames.Barrows.selectCoffin(c, objectType)){
					return;
				}
				if(c.barrowsNpcs[5][1] == 0){
					Server.npcHandler.spawnNpc(c, 2025, c.getX(), c.getY() - 1, 3, 0, 90, 19, 200, 200, true, true);
					c.barrowsNpcs[5][1] = 1;
				}else{
					c.sendMessage("You have already searched in this sarcophagus.");
				}
				break;

			case 1276:
			case 1278: // trees
				c.woodcut[0] = 1511;
				c.woodcut[1] = 1;
				c.woodcut[2] = 25;
				c.woodcut[3] = 1344;
				c.getWoodcutting().startWoodcutting(c.woodcut[0], c.woodcut[1], c.woodcut[2], c.woodcut[3]);
				break;

			case 1281: // oak
				c.woodcut[0] = 1521;
				c.woodcut[1] = 15;
				c.woodcut[2] = 37;
				c.woodcut[3] = 1355;
				c.getWoodcutting().startWoodcutting(c.woodcut[0], c.woodcut[1], c.woodcut[2], c.woodcut[3]);
				break;

			case 1308: // willow
				c.woodcut[0] = 1519;
				c.woodcut[1] = 30;
				c.woodcut[2] = 68;
				c.woodcut[3] = 4329;
				c.getWoodcutting().startWoodcutting(c.woodcut[0], c.woodcut[1], c.woodcut[2], c.woodcut[3]);
				break;

			case 1307: // maple
				c.woodcut[0] = 1517;
				c.woodcut[1] = 45;
				c.woodcut[2] = 100;
				c.woodcut[3] = 1342;
				c.getWoodcutting().startWoodcutting(c.woodcut[0], c.woodcut[1], c.woodcut[2], c.woodcut[3]);
				break;

			case 1309: // yew
				c.woodcut[0] = 1515;
				c.woodcut[1] = 60;
				c.woodcut[2] = 175;
				c.woodcut[3] = 1355;
				c.getWoodcutting().startWoodcutting(c.woodcut[0], c.woodcut[1], c.woodcut[2], c.woodcut[3]);
				break;

			case 1306: // Magic
				c.woodcut[0] = 1513;
				c.woodcut[1] = 75;
				c.woodcut[2] = 250;
				c.woodcut[3] = 7401;
				c.getWoodcutting().startWoodcutting(c.woodcut[0], c.woodcut[1], c.woodcut[2], c.woodcut[3]);
				break;

			case 2090:// copper
			case 2091:
				c.mining[0] = 436;
				c.mining[1] = 1;
				c.mining[2] = 18;
				c.getMining().startMining(c.mining[0], c.mining[1], c.mining[2]);
				break;

			case 2094:// tin
				c.mining[0] = 438;
				c.mining[1] = 1;
				c.mining[2] = 18;
				c.getMining().startMining(c.mining[0], c.mining[1], c.mining[2]);
				break;

			case 145856:
			case 2092:
			case 2093: // iron
				c.mining[0] = 440;
				c.mining[1] = 15;
				c.mining[2] = 35;
				c.getMining().startMining(c.mining[0], c.mining[1], c.mining[2]);
				break;

			case 14850:
			case 14851:
			case 14852:
			case 2096:
			case 2097: // coal
				c.mining[0] = 453;
				c.mining[1] = 30;
				c.mining[2] = 50;
				c.getMining().startMining(c.mining[0], c.mining[1], c.mining[2]);
				break;

			case 2098:
			case 2099: // gold
				c.mining[0] = 444;
				c.mining[1] = 40;
				c.mining[2] = 65;
				c.getMining().startMining(c.mining[0], c.mining[1], c.mining[2]);
				break;

			case 2102:
			case 2103:
			case 14853:
			case 14854:
			case 14855: // mith ore
				c.mining[0] = 447;
				c.mining[1] = 55;
				c.mining[2] = 80;
				c.getMining().startMining(c.mining[0], c.mining[1], c.mining[2]);
				break;

			case 2105:
			case 14862: // addy ore
				c.mining[0] = 449;
				c.mining[1] = 70;
				c.mining[2] = 95;
				c.getMining().startMining(c.mining[0], c.mining[1], c.mining[2]);
				break;

			case 14859:
			case 14860: // rune ore
				c.mining[0] = 451;
				c.mining[1] = 85;
				c.mining[2] = 125;
				c.getMining().startMining(c.mining[0], c.mining[1], c.mining[2]);
			case 14861: // rune ore
				c.mining[0] = 451;
				c.mining[1] = 85;
				c.mining[2] = 125;
				c.getMining().startMining(c.mining[0], c.mining[1], c.mining[2]);
				break;

			case 8143:
				if(c.farm[0] > 0 && c.farm[1] > 0){
					c.getFarming().pickHerb(obX, obY);
				}
				break;

			// DOORS
			case 1516:
			case 1519:
				if(c.objectY == 9698){
					if(c.absY >= c.objectY)
						c.getPA().walkTo(0, -1);
					else
						c.getPA().walkTo(0, 1);
					break;
				}
			case 4465:
			case 4466:
			case 4423:
			case 4424:
			case 4425:
			case 4426:
			case 4427:
			case 4428:
			case 4429:
			case 4430:
			case 1531:
			case 1533:
			case 1534:
			case 11712:
			case 11711:
			case 11707:
			case 11708:
			case 6725:
			case 3198:
			case 3197:
			case 4467:
			case 4468:
				Server.objectHandler.doorHandling(c, objectType, c.objectX, c.objectY, 0);
				break;

			case 9319:
				if(c.heightLevel == 0)
					c.getPA().movePlayer(c.absX, c.absY, 1);
				else if(c.heightLevel == 1)
					c.getPA().movePlayer(c.absX, c.absY, 2);
				break;

			case 9320:
				if(c.heightLevel == 1)
					c.getPA().movePlayer(c.absX, c.absY, 0);
				else if(c.heightLevel == 2)
					c.getPA().movePlayer(c.absX, c.absY, 1);
				break;

			case 4496:
			case 4494:
				if(c.heightLevel == 2){
					c.getPA().movePlayer(3412, 3540, 1);
				}else if(c.heightLevel == 1){
					c.getPA().movePlayer(3438, 3537, 0);
				}
				break;

			case 4493:
				if(c.heightLevel == 0){
					c.getPA().movePlayer(3433, 3537, 1);
				}else if(c.heightLevel == 1 && c.absX < 3425){
					c.getPA().movePlayer(3417, 3540, 2);
				}
				break;

			case 4495:
				if(c.heightLevel == 1){
					c.getPA().movePlayer(3417, 3540, 2);
				}
				break;

			case 5126:
				if(c.absY == 3554)
					c.getPA().walkTo(0, 1);
				else
					c.getPA().walkTo(0, -1);
				break;

			case 1755:
				if(c.objectX == 2884 && c.objectY == 9797)
					c.getPA().movePlayer(c.absX, c.absY - 6400, 0);
				break;
			case 1759:
				if(c.objectX == 2884 && c.objectY == 3397)
					c.getPA().movePlayer(c.absX, c.absY + 6400, 0);
				break;
			case 3203: // dueling forfeit
				if(c.duel != null && c.duel.rules != null && c.duel.rules.duelRule[0] && c.duel.status == 3){
					c.sendMessage("Forfeiting the duel has been disabled!");
					break;
				}
				if(c.duel != null && c.duel.duelCount > 0){
					c.sendMessage("You can't forfeit yet.");
					break;
				}
				if(c.duel != null && c.duel.status == 3 && !c.duel.isDead){
					Client o = c.duel.getOtherPlayer(c.playerId);
					if(o != null){
						o.playerLevel[o.playerHitpoints] = o.getLevelForXP(o.playerXP[o.playerHitpoints]);
						o.getPA().refreshSkill(o.playerHitpoints);
						o.isDead = false;
						o.isDead2 = false;
						o.respawnTimer = -6;
					}
					c.getPA().giveLife();
				}
				break;

			case 409:
				if(c.playerLevel[5] < c.getPA().getLevelForXP(c.playerXP[5])){
					c.startAnimation(645);
					c.playerLevel[5] = c.getPA().getLevelForXP(c.playerXP[5]);
					c.sendMessage("You recharge your prayer points.");
					c.getPA().refreshSkill(5);
					c.resetWalkingQueue();
					c.stopMovement();
				}else{
					c.sendMessage("You already have full prayer points.");
				}
				break;
			case 2873:
				if(!c.getItems().ownsCape()){
					c.startAnimation(645);
					c.resetWalkingQueue();
					c.stopMovement();
					c.sendMessage("Saradomin blesses you with a cape.");
					c.inventory.addItem(2412, 1, -1);
				}
				break;
			case 2875:
				if(!c.getItems().ownsCape()){
					c.startAnimation(645);
					c.resetWalkingQueue();
					c.stopMovement();
					c.sendMessage("Guthix blesses you with a cape.");
					c.inventory.addItem(2413, 1, -1);
				}
				break;
			case 2874:
				if(!c.getItems().ownsCape()){
					c.startAnimation(645);
					c.resetWalkingQueue();
					c.stopMovement();
					c.sendMessage("Zamorak blesses you with a cape.");
					c.inventory.addItem(2414, 1, -1);
				}
				break;
			case 2879:
				c.getPA().movePlayer(2538, 4716, 0);
				break;
			case 2878:
				c.getPA().movePlayer(2509, 4689, 0);
				break;
			case 5960:
				if(c.overloadedBool){
					c.sendMessage("You can not enter a PVP area while overloaded.");
					break;
				}
				c.getPA().startTeleport2(3090, 3956, 0);
				break;
			case 9706:
				c.getPA().startTeleport2(3105, 3951, 0);
				break;
			case 9707:
				c.getPA().startTeleport2(3105, 3956, 0);
				break;

			case 5959:
				c.getPA().startTeleport2(2539, 4712, 0);
				break;

			case 2558:
				c.sendMessage("This door is locked.");
				break;

			case 9294:
				if(c.absX < c.objectX){
					c.getPA().movePlayer(2880, 9813, 0);
				}else if(c.absX > c.objectX){
					c.getPA().movePlayer(2878, 9813, 0);
				}
				break;

			case 9293:
				if(c.absX < c.objectX){
					c.getPA().movePlayer(2892, 9799, 0);
				}else{
					c.getPA().movePlayer(2886, 9799, 0);
				}
				break;
			case 10529:
			case 10527:
				if(c.absY <= c.objectY)
					c.getPA().walkTo(0, 1);
				else
					c.getPA().walkTo(0, -1);
				break;
			case 3044:
				c.getSmithing().sendSmelting();
				break;
			case 733:
				c.startAnimation(451);
				/*
				 * if (Misc.random(1) == 1) { c.getPA().removeObject(c.objectX,
				 * c.objectY); c.sendMessage("You slash the web."); } else {
				 * c.sendMessage("You fail to slash the webs."); }
				 */
				if(c.objectX == 3158 && c.objectY == 3951){
					Object o = new Object(734, c.objectX, c.objectY, c.heightLevel, 1, 10, 733, 50);
					o.setClip(Region.getClipping(o.objectX, o.objectY, o.height));
					Region.setClipping(c.objectX, c.objectY, c.heightLevel, 0);
				}else if((c.objectX == 3106 || c.objectX == 3105) && c.objectY == 3958){
					Object o = new Object(734, c.objectX, c.objectY, c.heightLevel, 3, 10, 733, 50);
					o.setClip(Region.getClipping(o.objectX, o.objectY, o.height));
					Region.setClipping(c.objectX, c.objectY, c.heightLevel, 0);
				}else if((c.objectX == 3093 || c.objectX == 3095) && c.objectY == 3957){
					Object o = new Object(734, c.objectX, c.objectY, c.heightLevel, 0, 10, 733, 50);
					o.setClip(Region.getClipping(o.objectX, o.objectY, o.height));
					Region.setClipping(c.objectX, c.objectY, c.heightLevel, 0);
				}else{
					Object o = new Object(734, c.objectX, c.objectY, c.heightLevel, 0, 10, 733, 50);
					o.setClip(Region.getClipping(o.objectX, o.objectY, o.height));
					Region.setClipping(c.objectX, c.objectY, c.heightLevel, 0);
				}
				break;

			default:
				ScriptManager.callFunc("objectClick1_" + objectType, c, objectType, obX, obY);
				break;

		}
	}

	public void secondClickObject(int objectType, int obX, int obY){
		if(!ignoreObjectCheck(c.objectId))
			if(!Region.objectExists(objectType, obX, obY, c.heightLevel))
				return;
		c.clickObjectType = 0;
		// c.sendMessage("Object type: " + objectType);
		switch(objectType){
			case 11666:
			case 3044:
				c.getSmithing().sendSmelting();
				break;
			case 2213:
				if(c.inCanifis()){
					c.sendMessage("The bank seems to be closed...");
					break;
				}
			case 14367:
			case 11758:
				if(canBank(obX, obY))
					c.canBank = true;
				c.getPA().openUpBank();
				break;
			case 6:
				c.getCannon().pickupCannon(objectType, obX, obY);
				break;
			case 4874:
				c.getThieving().stealFromStall(995, 3000 + Misc.random(5000), 100, 1);

				break;
			case 4875:
				c.getThieving().stealFromStall(995, 4000 + Misc.random(8000), 130, 25);

				break;
			case 4876:
				c.getThieving().stealFromStall(995, 5000 + Misc.random(8500), 160, 50);

				break;
			case 4877:
				c.getThieving().stealFromStall(995, 6000 + Misc.random(9000), 180, 75);

				break;
			case 4878:
				c.getThieving().stealFromStall(995, 7000 + Misc.random(10000), 250, 90);

				break;

			case 2558:
				if(System.currentTimeMillis() - c.lastLockPick < 3000 || c.freezeTimer > 0)
					break;
				if(c.inventory.hasItem(1523, 1)){
					c.lastLockPick = System.currentTimeMillis();
					if(Misc.random(10) <= 3){
						c.sendMessage("You fail to pick the lock.");
						break;
					}
					if(c.objectX == 3044 && c.objectY == 3956){
						if(c.absX == 3045){
							c.getPA().walkTo(-1, 0);
						}else if(c.absX == 3044){
							c.getPA().walkTo(1, 0);
						}

					}else if(c.objectX == 3038 && c.objectY == 3956){
						if(c.absX == 3037){
							c.getPA().walkTo(1, 0);
						}else if(c.absX == 3038){
							c.getPA().walkTo(-1, 0);
						}
					}else if(c.objectX == 3041 && c.objectY == 3959){
						if(c.absY == 3960){
							c.getPA().walkTo(0, -1);
						}else if(c.absY == 3959){
							c.getPA().walkTo(0, 1);
						}
					}
				}else{
					c.sendMessage("I need a lockpick to pick this lock.");
				}
				break;
			default:
				ScriptManager.callFunc("objectClick2_" + objectType, c, objectType, obX, obY);
				break;
		}
	}

	public void thirdClickObject(int objectType, int obX, int obY){
		if(!ignoreObjectCheck(c.objectId))
			if(!Region.objectExists(objectType, obX, obY, c.heightLevel))
				return;
		c.clickObjectType = 0;
		c.sendMessage("Object type: " + objectType);
		switch(objectType){
			default:
				ScriptManager.callFunc("objectClick3_" + objectType, c, objectType, obX, obY);
				break;
		}
	}

	public void firstClickNpc(int npcType){
		if(c.playerRights == 3)
			System.out.println("First Click Npc : " + npcType);
		switch(npcType){
			case 706:
				c.getDH().sendDialogues(9, npcType);
				break;
			case 1686: // Ghost disciple
				c.getDH().sendDialogues(c.ghostEvent ? 96 : 89, npcType);
				break;
			case 548: // Thessalia
				c.getDH().sendDialogues(22, 548);
				break;
			case 3082:
				if(c.inventory.hasItem(6874)){
					if(c.firstMarionette == -1)
						c.firstMarionette = 6867;
					c.inventory.deleteItem(6874, 1);
					c.inventory.addItem(6870, 1);
					c.getDH().sendDialogues(c.inventory.hasItem(6868) && c.inventory.hasItem(6869) ? 87 : 86, npcType);
				}else if(c.inventory.hasItem(6878)){
					if(c.firstMarionette == -1)
						c.firstMarionette = 6865;
					c.inventory.deleteItem(6878, 1);
					c.inventory.addItem(6868, 1);
					c.getDH().sendDialogues(c.inventory.hasItem(6869) && c.inventory.hasItem(6870) ? 87 : 86, npcType);
				}else if(c.inventory.hasItem(6882)){
					if(c.firstMarionette == -1)
						c.firstMarionette = 6866;
					c.inventory.deleteItem(6882, 1);
					c.inventory.addItem(6869, 1);
					c.getDH().sendDialogues(c.inventory.hasItem(6868) && c.inventory.hasItem(6870) ? 87 : 86, npcType);
				}else if(c.inventory.hasItem(6868) && c.inventory.hasItem(6869) && c.inventory.hasItem(6870)){
					c.inventory.deleteItem(6868, 1);
					c.inventory.deleteItem(6869, 1);
					c.inventory.deleteItem(6870, 1);
					int give = c.firstMarionette < 1 ? 6865 : c.firstMarionette;
					if(!c.inventory.hasItem(6865) && c.bank.getBankAmount(6865) == 0 && !c.inventory.hasItem(6866) && c.bank.getBankAmount(6866) == 0 && !c.inventory.hasItem(6867) && c.bank.getBankAmount(6867) == 0)
						c.inventory.addItem(give, 1);
					c.firstMarionette = -1;
					c.getDH().sendDialogues(88, npcType);
				}else
					c.getDH().sendDialogues(85, npcType);
				break;
			case 6524:
				c.getDH().sendDialogues(31, 6524);
				break;
			case 1778:
				c.getShops().openShop(45);
				break;
			case 1786:
				c.getShops().openShop(55);
				break;
			case 2290:
				c.getShops().openShop(13);
				break;
			case 273:
				c.getShops().openVoteShop();
				break;
			case 3508:
				c.getShops().openShop(35);
				break;
			case 1526:
				c.getShops().openShop(20);
				break;
			case 3081:
				if(c.inDuelArena())
					c.getDH().sendDialogues(35, npcType);
				else
					c.getDH().sendDialogues(41, npcType);
				break;
			case 2258:
				c.getDH().sendDialogues(17, npcType);
				break;
			case 536:
				c.getDH().sendDialogues(21, npcType);
				break;
			case 2244:
				c.getDH().sendDialogues(20, npcType);
				break;
			case 882:
				boolean start = c.getQuestHandler().demonSlayer.isInQuest;
				c.getQuestHandler().questId = QuestHandler.DEMON_SLAYER;
				if(!start && !c.getQuestHandler().demonSlayer.completed)
					c.getQuestHandler().beginQuest(QuestHandler.DEMON_SLAYER);
				if(c.getQuestHandler().demonSlayer.obtained)
					c.getQuestHandler().demonSlayer.dialogueId = (c.inventory.hasItem(DemonSlayer.SILVER_LIGHT) || c.bank.bankHasItem(DemonSlayer.SILVER_LIGHT) || c.playerEquipment[c.playerWeapon] == DemonSlayer.SILVER_LIGHT) ? 9 : (((c.inventory.hasItem(DemonSlayer.RUSTY_SWORD) || c.bank.bankHasItem(DemonSlayer.RUSTY_SWORD)) && (c.inventory.hasItem(DemonSlayer.SILVER_BAR) || c.bank.bankHasItem(DemonSlayer.SILVER_BAR))) ? 6 : 7);
				c.getQuestHandler().demonSlayer.handleDialogue(!start && c.getQuestHandler().demonSlayer.completed ? false : !start);
				break;
			case 8275:
				c.getDH().sendDialogues(c.slayerTask == null ? 11 : 13, npcType);
				break;
			case 1334:
				c.getShops().openShop(16);
				break;
			case 919:
				c.getShops().openShop(10);
				break;
			case 57:
				c.getShops().openShop(12);
				break;
			case 542:
				c.getShops().openShop(9);
				break;
			case 284:
				c.getShops().openShop(18);
				break;
			case 541:
				c.getShops().openShop(5);
				break;

			case 461:
				c.getShops().openShop(2);
				break;

			case 683:
				c.getShops().openShop(3);
				break;

			case 549:
				c.getShops().openShop(4);
				break;

			case 2538:
				c.getShops().openShop(6);
				break;

			case 519:
				c.getShops().openShop(8);
				break;
			case 1282:
				c.getShops().openShop(7);
				break;
			case 1152:
				c.getDH().sendDialogues(16, npcType);
				break;
			case 494:
			case 495:
				if(canBank(NPCHandler.npcs[c.npcClickIndex].absX, NPCHandler.npcs[c.npcClickIndex].absY))
					c.canBank = true;
				c.getPA().openUpBank();
				break;
			case 2566:
				c.getShops().openSkillCape();
				break;
			case 608:
				c.getShops().openDonorShop();
				break;
			case 3788:
				c.getShops().openShop(25);
				break;
			case 3789:
				c.getShops().openShop(11);
				break;
			case 905:
				c.getDH().sendDialogues(5, npcType);
				break;
			case 460:
				c.getDH().sendDialogues(3, npcType);
				break;
			case 462:
				c.getDH().sendDialogues(7, npcType);
				break;
			case 314:
			case 324:
			case 326:
			case 334:
				Spot.handleSpot(c, npcType, NPCHandler.npcs[c.npcClickIndex].absX, NPCHandler.npcs[c.npcClickIndex].absY, 0);
				break;
			case 522:
			case 523:
				c.getShops().openShop(1);
				break;
			case 599:
				c.getPA().showInterface(3559);
				c.canChangeAppearance = true;
				break;
			case 904:
				c.getShops().openShop(17);
				break;
			default:
				ScriptManager.callFunc("npcClick1_" + npcType, c, npcType);
				break;
		}
		c.clickNpcType = 0;
		c.npcClickIndex = 0;
	}

	public void secondClickNpc(int npcType){
		switch(npcType){
			case 1282:
				c.getShops().openShop(7);
				break;
			case 1686: // Ghost disciple
				if(c.ghostEvent)
					c.getShops().openShop(85);
				else
					c.sendMessage("I'd better see what he wants first.");
				break;
			case 3788:
				c.getShops().openVoid();
				break;
			case 57:
				c.getShops().openShop(12);
				break;
			case 494:
			case 495:
				if(canBank(NPCHandler.npcs[c.npcClickIndex].absX, NPCHandler.npcs[c.npcClickIndex].absY))
					c.canBank = true;
				c.getPA().openUpBank();
				break;
			case 6524:
				c.getDH().sendDialogues(32, 6524);
				break;
			case 1778:
				c.getShops().openShop(45);
				break;
			case 1786:
				c.getShops().openShop(55);
				break;
			case 3081:
				if(c.inDuelArena())
					c.getShops().openShop(65);
				else
					c.getDH().sendDialogues(48, npcType);
				break;
			case 314:
			case 324:
			case 334:
				Spot.handleSpot(c, npcType, NPCHandler.npcs[c.npcClickIndex].absX, NPCHandler.npcs[c.npcClickIndex].absY, 1);
				break;
			case 904:
				c.getShops().openShop(17);
				break;
			case 522:
			case 523:
				c.getShops().openShop(1);
				break;
			case 541:
				c.getShops().openShop(5);
				break;

			case 461:
				c.getShops().openShop(2);
				break;

			case 683:
				c.getShops().openShop(3);
				break;

			case 549:
				c.getShops().openShop(4);
				break;

			case 2538:
				c.getShops().openShop(6);
				break;

			case 519:
				c.getShops().openShop(8);
				break;
			case 3789:
				c.getShops().openShop(11);
				break;
			case 1:
			case 9:
			case 18:
			case 20:
			case 26:
			case 21:
			case 1911:
				c.getThieving().stealFromNPC(npcType);
				break;
			default:
				ScriptManager.callFunc("npcClick2_" + npcType, c, npcType);
				if(c.playerRights == 3)
					System.out.println("Second Click Npc : " + npcType);
				break;
		}
		c.clickNpcType = 0;
		c.npcClickIndex = 0;
	}

	public void thirdClickNpc(int npcType){
		c.clickNpcType = 0;
		c.npcClickIndex = 0;
		switch(npcType){
			case 1526:
				c.getShops().openShop(20);
				break;
			case 6524:
				c.inventory.decantAllPots(false);
				c.getDH().sendDialogues(34, 6524);
				break;
			case 8275:
				c.getShops().openShop(75);
				break;
			default:
				ScriptManager.callFunc("npcClick3_" + npcType, c, npcType);
				if(c.playerRights == 3)
					System.out.println("Third Click NPC : " + npcType);
				break;
		}
	}
	public static void main(String argsp[]){
		int amt = 927 * 4;
		int other = 0;
		do{
			other += 1;
			amt -= 6;
		}while(amt > 6);
		System.out.println(other);
	}
}
