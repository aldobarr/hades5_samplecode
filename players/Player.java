package server.model.players;

import java.util.ArrayList;

import server.Config;
import server.Server;
import server.model.items.InventoryItem;
import server.model.items.Item;
import server.model.minigames.Barricade;
import server.model.minigames.CastleWars;
import server.model.minigames.Duel;
import server.model.minigames.HowlOfDeath;
import server.model.minigames.HowlOfDeathManager;
import server.model.minigames.NexGames;
import server.model.npcs.NPC;
import server.model.npcs.NPCHandler;
import server.model.players.skills.Agility;
import server.model.players.skills.SlayerTask;
import server.model.quests.DemonSlayer;
import server.model.quests.QuestHandler;
import server.model.region.Location;
import server.model.region.Region;
import server.model.region.RegionManager;
import server.util.ISAACRandomGen;
import server.util.Misc;
import server.util.Stream;

public abstract class Player{
	public ArrayList<String> lastKilledPlayers = new ArrayList<String>();
	public long ignores[] = new long[100], lastOverload = 0, resetPin = 0;
	public double kills = 0, deaths = 0;
	public int playerDirection;
	public String lastCommand = "", ownedClanName = "", lastClan = "";
	public int slayerPoints = 0;
	public boolean bugKick = false, resting = false, arrowKept = false, rejoinClan = true, isOnline = true;
	public int selectedTab = 0, rest[] = {-1, -1};
	public long rod_delay = -1, glory_delay = -1;
	public long locateDelay = 0;
	public boolean ghostEvent = false;
	public int restoreRate = 60000, exitDelay = -1, clanWarsWalkInterface = -1, lastConversion = -1;
	public boolean bankStringSent = false, swap = true, isSearching = false, duel2h = false;
	public boolean hasCurses = false, inClanWars = false, canSummonNomad = false, canBank = false;
	public boolean pestTemp = false, inPestGame = false, pestGameEnd = false, inZombiesGame = false, zombieTemp = false;
	public int screenState = 0, pestEnd = 1;
	public boolean scoreBoardReset = false;
	public int zombieAttrition = 0, zombieAttritionDelay = 12, zombieAttritionTick = 0, zombieEffect = -1, zombieKills = 0, zombiePoints = 0;
	public ArrayList<Integer> newPlayers = new ArrayList<Integer>();
	public int overloaded[][] = {{0, 0}, {1, 0}, {2, 0}, {4, 0}, {6, 0}};
	public int doubleExpTime = 0;
	public boolean DCDamg = false, overloadedBool = false, capeMovement = false;
	public int dfsCharges = 0, overloadTime = 0, overloadTick = 0, maxCape = 0, maxCapeNpcId = -1;
	public boolean xpLock, usingQuincySpec = false, loadedPass = false, solSpec = false;
	public int solTime = -1, lastSkillEmote = -1;
	public int fmX = 0, fmY = 0, fmZ = 0;
	public int follow2 = 0, lightingSlot = -1, lightingTick = -1;
	public boolean voting = false, donating = false, lighting = false, deleteAnnihilation = false;
	public double getStr, getAtt, getDef, getRange, getMagic;
	public int strInc = 5, playNumber, pjAttackTimer = 0, deleteAnnihilationTick = -1;
	public int attInc = 5;
	public int defInc = 5;
	public int duelArena = -1;
	public int miasmicTime = 0;
	public boolean miasmicEffect = false;
	public int certGive = 0;
	public int rangeInc = 5, zarosModifier = 0;
	public int magicInc = 5, quincyStage = 0;
	public int DCdown = 0, overLoad = 0, teleTick = 0;
	public boolean sendRecovMessage = false, isJailed = false, inNexGame = false, inCWBase = false, removeCWNeeded = false;
	public int nexWaveTick = 0, nexTotal = 0;
	public int cwKills, cwDeaths, cwGames;
	public final int easterEggs[] = {3689, 3690, 3691, 3692, 3694};
	public final int capes[] = {9804, 9805, 9806, 9798, 9799, 9800, 9780, 9781, 9782, 9756, 9757, 9758, 9801, 9802, 9803, 9783, 9784, 9785, 9753, 9754, 9755, 9810, 9811, 9812, 9777, 9778, 9779, 9786, 9787, 9788, 9771, 9772, 9773, 9774, 9775, 9776, 9759, 9760, 9761, 9765, 9766, 9767, 9813, 9814, 9795, 9796, 9797, 9792, 9793, 9794, 9747, 9748, 9749, 9807, 9808, 9809, 9750, 9751, 9752, 9768, 9769, 9770, 9762, 9763, 9764, 9948, 9949};
	public int Donator = 0;
	public int Rating = 0;
	public int npcId = -1;
	public boolean isNpc = false, isWearingRing = false, setPin = false, settingPin = false, resetPinNow = false, inCWJail = false, inCutScene = false;
	public int summonedSlot = -1, attempts = 3, cwJailTime = -1;
	public int FishID;
	public int cookedFishID;
	public int highscores = 0, recoverId = -1;
	public int CookingEmote;
	public String CookFishName, clanId = "", bankPin = "";
	public int burnFishID;
	public int succeslvl;
	public int xamount, clawHitPos = 0;
	public int playerMac;
	public InventoryItem lostItems[] = new InventoryItem[28];
	public int lostEquip[][] = {new int[14], new int[14]};
	public boolean isCooking = false;
	public int completedSetsMar, usedOnObjectID, usedOnobjectX, usedOnobjectY, CWPlayerIndex, TrownSpellTimer;
	public int clawHits[] = {0, 0, 0, 0};
	public int sapTicks[] = {0, 0, 0, 0};
	public double sapAmount[] = {1.0, 1.0, 1.0, 1.0};
	public int sapGfx[][] = {{2214, 2216}, {2217, 2219}, {2220, 2222}, {2223, 2225}};
	public boolean sappedDef = false;
	public int leechTicks[] = {0, 0, 0, 0, 0, 0, 0};
	public double leechAmount[] = {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0};
	public int leechGfx[][] = {{2232, 2233}, {2238, 2237}, {2242, 2241}, {2246, 2245}, {2250, 2249}, {2254, 2253}, {2258, 2257}};
	public boolean leechedDef = false;

	public HowlOfDeath howlOfDeath = null;
	public boolean fixHowlOfDeathTele = false;
	public boolean inHowlOfDeath = false;
	public boolean inHowlOfDeathLobby = false;
	public int howlOfDeathLobby = 0;
	public boolean addedToPits = false;
	public boolean isDoingSkillcapeAnim = false;
	public boolean varrockTeleSelected = false;
	public boolean edgevileTeleSelected = false;
	public boolean faladorTeleSelected = false;
	public boolean catherbyTeleSelected = false;
	public boolean camelotTeleSelected = false;
	public boolean barbarianTeleSelected = false;
	public long lastCast = 0;
	public int combatLogout = -1;
	public boolean yanilleTeleSelected = false;
	public ArrayList<String> killedPlayers = new ArrayList<String>();
	public ArrayList<Integer> attackedPlayers = new ArrayList<Integer>();
	public int ignoreClipTick = 0;
	public boolean brandNew = false, loggingOut = false;
	public boolean ignoreClip = false, timeMuted = false, initialized = false, disconnected = false, ruleAgreeButton = false, RebuildNPCList = false, isActive = false, isKicked = false, isSkulled = false, friendUpdate = false, newPlayer = false, ignoreNulls = false, hasMultiSign = false, saveCharacter = false, mouseButton = false, splitChat = false, chatEffects = true, acceptAid = false, nextDialogue = false, autocasting = false, usedSpecial = false, mageFollow = false, dbowSpec = false, craftingLeather = false, properLogout = false, secDbow = false, maxNextHit = false, ssSpec = false, vengOn = false, addStarter = false, accountFlagged = false, msbSpec = false, newCmb = false, isBanking = false;
	public String nextMessage = "", nextMessage2 = "";
	public boolean nextMsg = false;
	public int muteTime = 0, headIconHints, pure, saveDelay, playerKilled, pkPoints, totalPlayerDamageDealt, killedBy, lastChatId = 1, privateChat = 0, publicChat = 0, tradeMode, friendSlot = 0, dialogueId, randomCoffin, newLocation, specEffect, specBarId, attackLevelReq, Donatorreq, defenceLevelReq, strengthLevelReq, rangeLevelReq, magicLevelReq, fireLevelReq, prayerLevelReq, hpLevelReq, followId, skullTimer, votingPoints, nextChat = 0, talkingNpc = -1, dialogueAction = 0, autocastId, followDistance, followId2, barrageCount = 0, delayedDamage = 0, delayedDamage2 = 0, voteTime = 0, isDonator, WildTimer = 0, magePoints = 0, desertTreasure = 0, lastArrowUsed = -1, autoRet = 0, pcDamage = 0, pcPoints = 0, donationPoints = 0, votePoints = 0, xInterfaceId = 0, xRemoveId = 0, xRemoveSlot = 0, tzhaarToKill = 0, tzhaarKilled = 0, waveId, poisonDamage = 0, teleAction = 0, bonusAttack = 0, lastNpcAttacked = 0, newCombat = 0;
	public String currentTime, date, lastKillIP = "";
	public String clanName, properName;
	public int lastIgnoreRemove = -1, lastIgnoreAdd = -1, firstMarionette = -1, nextFightCave = -1;
	public int voidStatus[] = new int[5];
	public int itemKeptId[] = new int[4];
	public int pouches[] = new int[4];
	public final int POUCH_SIZE[] = {3, 6, 9, 12};
	public long friends[] = new long[200];
	public long lastButton;
	public int clawDelay = 0;
	public int previousDamage = 0, nexTicketHP = -1;
	public boolean usingClaws = false, guthixBowSpec = false, duelInterface = false;
	public double specAmount = 0;
	public double specAccuracy = 1;
	public double specDamage = 1;
	public double prayerPoint = 1.0;
	public int teleGrabItem, teleGrabX, teleGrabY, underAttackBy, underAttackBy2, wildLevel, teleTimer, respawnTimer, saveTimer = 0, teleBlockLength, poisonDelay;
	public long lastPlayerMove, lastPoison, lastPoisonSip, poisonImmune, lastSpear, lastProtItem, dfsDelay, lastVeng, lastYell, teleGrabDelay, protMageDelay, protMeleeDelay, protRangeDelay, lastAction, lastThieve, lastAgil, lastLockPick, alchDelay, specDelay = System.currentTimeMillis(), teleBlockDelay, godSpellDelay, singleCombatDelay, singleCombatDelay2, reduceStat, restoreStatsDelay, restoreStatsDelay2, logoutDelay, buryDelay, foodDelay, potDelay;
	public boolean canChangeAppearance = false;
	public boolean mageAllowed;
	public byte poisonMask = 0;
	public int killCount = 0, bloodNecklaceTime = -1;
	
	public final int BOWS[] = {19146, 17295, 20097, 11235, 839, 845, 847, 851, 855, 859, 841, 843, 849, 853, 857, 861, 6724, 4734, 4934, 4935, 4936, 4937, 24456};
	public final int C_BOWS[] = {18357, 9185};
	public final int ARROWS[] = {882, 884, 886, 888, 890, 892, 4740, 11212, 19157};
	public final int BOLTS[] = {9140, 9141, 9142, 9143, 9144, 9240, 9241, 9242, 9243, 9244, 9245};
	public final int NO_ARROW_DROP[] = {15241, 13879, 13880, 13881, 13882, 13883, 4734, 4212, 4214, 4215, 4216, 4217, 4218, 4219, 4220, 4221, 4222, 4223, 4934, 4935, 4936, 4937, 20097, 20171, 20173};
	public final int OTHER_RANGE_WEAPONS[] = {13879, 13880, 13881, 13882, 13883, 863, 864, 865, 866, 867, 868, 869, 806, 807, 808, 809, 810, 811, 825, 826, 827, 828, 829, 830, 800, 801, 802, 803, 804, 805, 6522};

	public final int MAGIC_SPELLS[][] = {
			// example {magicId, level req, animation, startGFX, projectile Id,
			// endGFX, maxhit, exp gained, rune 1, rune 1 amount, rune 2, rune 2
			// amount, rune 3, rune 3 amount, rune 4, rune 4 amount}

			// Modern Spells
	{1152, 1, 711, 90, 91, 92, 2, 5, 556, 1, 558, 1, 0, 0, 0, 0}, // wind strike
	{1154, 5, 711, 93, 94, 95, 4, 7, 555, 1, 556, 1, 558, 1, 0, 0}, // water
																	// strike
	{1156, 9, 711, 96, 97, 98, 6, 9, 557, 2, 556, 1, 558, 1, 0, 0},// earth
																	// strike
	{1158, 13, 711, 99, 100, 101, 8, 11, 554, 3, 556, 2, 558, 1, 0, 0}, // fire
																		// strike
	{1160, 17, 711, 117, 118, 119, 9, 13, 556, 2, 562, 1, 0, 0, 0, 0}, // wind
																		// bolt
	{1163, 23, 711, 120, 121, 122, 10, 16, 556, 2, 555, 2, 562, 1, 0, 0}, // water
																			// bolt
	{1166, 29, 711, 123, 124, 125, 11, 20, 556, 2, 557, 3, 562, 1, 0, 0}, // earth
																			// bolt
	{1169, 35, 711, 126, 127, 128, 12, 22, 556, 3, 554, 4, 562, 1, 0, 0}, // fire
																			// bolt
	{1172, 41, 711, 132, 133, 134, 13, 25, 556, 3, 560, 1, 0, 0, 0, 0}, // wind
																		// blast
	{1175, 47, 711, 135, 136, 137, 14, 28, 556, 3, 555, 3, 560, 1, 0, 0}, // water
																			// blast
	{1177, 53, 711, 138, 139, 140, 15, 31, 556, 3, 557, 4, 560, 1, 0, 0}, // earth
																			// blast
	{1181, 59, 711, 129, 130, 131, 16, 35, 556, 4, 554, 5, 560, 1, 0, 0}, // fire
																			// blast
	{1183, 62, 711, 158, 159, 160, 17, 36, 556, 5, 565, 1, 0, 0, 0, 0}, // wind
																		// wave
	{1185, 65, 711, 161, 162, 163, 18, 37, 556, 5, 555, 7, 565, 1, 0, 0}, // water
																			// wave
	{1188, 70, 711, 164, 165, 166, 19, 40, 556, 5, 557, 7, 565, 1, 0, 0}, // earth
																			// wave
	{1189, 75, 711, 155, 156, 157, 20, 42, 556, 5, 554, 7, 565, 1, 0, 0}, // fire
																			// wave
	{1153, 3, 716, 102, 103, 104, 0, 13, 555, 3, 557, 2, 559, 1, 0, 0}, // confuse
	{1157, 11, 716, 105, 106, 107, 0, 20, 555, 3, 557, 2, 559, 1, 0, 0}, // weaken
	{1161, 19, 716, 108, 109, 110, 0, 29, 555, 2, 557, 3, 559, 1, 0, 0}, // curse
	{1542, 66, 729, 167, 168, 169, 0, 76, 557, 5, 555, 5, 566, 1, 0, 0}, // vulnerability
	{1543, 73, 729, 170, 171, 172, 0, 83, 557, 8, 555, 8, 566, 1, 0, 0}, // enfeeble
	{1562, 80, 729, 173, 174, 107, 0, 90, 557, 12, 555, 12, 556, 1, 0, 0}, // stun
	{1572, 20, 711, 177, 178, 181, 0, 30, 557, 3, 555, 3, 561, 2, 0, 0}, // bind
	{1582, 50, 711, 177, 178, 180, 2, 60, 557, 4, 555, 4, 561, 3, 0, 0}, // snare
	{1592, 79, 711, 177, 178, 179, 4, 90, 557, 5, 555, 5, 561, 4, 0, 0}, // entangle
	{1171, 39, 724, 145, 146, 147, 15, 25, 556, 2, 557, 2, 562, 1, 0, 0}, // crumble
																			// undead
	{1539, 50, 708, 87, 88, 89, 25, 42, 554, 5, 560, 1, 0, 0, 0, 0}, // iban
																		// blast
	{12037, 50, 1576, 327, 328, 329, 19, 30, 560, 1, 558, 4, 0, 0, 0, 0}, // magic
																			// dart
	{1190, 60, 811, 0, 0, 76, 20, 60, 554, 2, 565, 2, 556, 4, 0, 0}, // sara
																		// strike
	{1191, 60, 811, 0, 0, 77, 20, 60, 554, 1, 565, 2, 556, 4, 0, 0}, // cause of
																		// guthix
	{1192, 60, 811, 0, 0, 78, 20, 60, 554, 4, 565, 2, 556, 1, 0, 0}, // flames
																		// of
																		// zammy
	{12445, 85, 1819, 0, 344, 345, 0, 65, 563, 1, 562, 1, 560, 1, 0, 0}, // teleblock

			// Ancient Spells
	{12939, 50, 1978, 0, 384, 385, 13, 30, 560, 2, 562, 2, 554, 1, 556, 1}, // smoke
																			// rush
	{12987, 52, 1978, 0, 378, 379, 14, 31, 560, 2, 562, 2, 566, 1, 556, 1}, // shadow
																			// rush
	{12901, 56, 1978, 0, 0, 373, 15, 33, 560, 2, 562, 2, 565, 1, 0, 0}, // blood
																		// rush
	{12861, 58, 1978, 0, 360, 361, 16, 34, 560, 2, 562, 2, 555, 2, 0, 0}, // ice
																			// rush
	{12963, 62, 1979, 0, 0, 389, 19, 36, 560, 2, 562, 4, 556, 2, 554, 2}, // smoke
																			// burst
	{13011, 64, 1979, 0, 0, 382, 20, 37, 560, 2, 562, 4, 556, 2, 566, 2}, // shadow
																			// burst
	{12919, 68, 1979, 0, 0, 376, 21, 39, 560, 2, 562, 4, 565, 2, 0, 0}, // blood
																		// burst
	{12881, 70, 1979, 0, 0, 363, 22, 40, 560, 2, 562, 4, 555, 4, 0, 0}, // ice
																		// burst
	{12951, 74, 1978, 0, 386, 387, 23, 42, 560, 2, 554, 2, 565, 2, 556, 2}, // smoke
																			// blitz
	{12999, 76, 1978, 0, 380, 381, 24, 43, 560, 2, 565, 2, 556, 2, 566, 2}, // shadow
																			// blitz
	{12911, 80, 1978, 0, 374, 375, 25, 45, 560, 2, 565, 4, 0, 0, 0, 0}, // blood
																		// blitz
	{12871, 82, 1978, 366, 0, 367, 26, 46, 560, 2, 565, 2, 555, 3, 0, 0}, // ice
																			// blitz
	{12975, 86, 1979, 0, 0, 391, 27, 48, 560, 4, 565, 2, 556, 4, 554, 4}, // smoke
																			// barrage
	{13023, 88, 1979, 0, 0, 383, 28, 49, 560, 4, 565, 2, 556, 4, 566, 3}, // shadow
																			// barrage
	{12929, 92, 1979, 0, 0, 377, 29, 51, 560, 4, 565, 4, 566, 1, 0, 0}, // blood
																		// barrage
	{12891, 94, 1979, 0, 0, 369, 30, 52, 560, 4, 565, 2, 555, 6, 0, 0}, // ice
																		// barrage

	{-1, 80, 811, 301, 0, 0, 0, 0, 554, 3, 565, 3, 556, 3, 0, 0}, // charge
	{-1, 21, 712, 112, 0, 0, 0, 10, 554, 3, 561, 1, 0, 0, 0, 0}, // low alch
	{-1, 55, 713, 113, 0, 0, 0, 20, 554, 5, 561, 1, 0, 0, 0, 0}, // high alch
	{-1, 33, 728, 142, 143, 144, 0, 35, 556, 1, 563, 1, 0, 0, 0, 0}, // telegrab
	{12435, 82, 10546, 457, 0, 1333, -1, 70, 21773, 1, 0, 0, 0, 0, 0, 0}, // Storm of Armadyl (teleother fally)
	{-1, 43, 725, 148, 0, 0, 0, 15, 554, 4, 561, 1, 0, 0, 0, 0}, // Superheat item
	{-1, 97, 10518, 1853, 0, 1854, 35, 60, 557, 4, 566, 4, 565, 4, 0, 0} // Miasmic Barrage
	};
	// example {magicId, level req, animation, startGFX, projectile Id,
				// endGFX, maxhit, exp gained, rune 1, rune 1 amount, rune 2, rune 2
				// amount, rune 3, rune 3 amount, rune 4, rune 4 amount}
	public void setDisconnected(boolean disconnected){
		if(disconnected && (underAttackBy > 0 || underAttackBy2 > 0) && Config.COMBAT_DISCONNECTION){
			this.disconnected = false;
			combatLogout = Misc.currentTimeSeconds() + 15;
			return;
		}
		this.disconnected = disconnected;
	}

	public boolean withinInteractionDistance(int x, int y, int z){
		if(heightLevel != z)
			return false;
		Client c = (Client)this;
		int deltaX = x - c.getX(), deltaY = y - c.getY();
		return deltaX <= 4 && deltaX >= -4 && deltaY <= 4 && deltaY >= -4;
	}

	public void resetDegrade(){
		for(int i = 0; i < playerEquipmentDT.length; i++)
			playerEquipmentDT[i] = -1;
	}

	public boolean isAutoButton(int button){
		for(int i : autocastIds)
			if(i == button)
				return true;
		return false;
	}

	public int autocastIds[] = {51133, 32, 51185, 33, 51091, 34, 24018, 35, 51159, 36, 51211, 37, 51111, 38, 51069, 39, 51146, 40, 51198, 41, 51102, 42, 51058, 43, 51172, 44, 51224, 45, 51122, 46, 51080, 47, // Ancient
	7038, 52, 7039, 1, 7040, 2, 7041, 3, 7042, 4, 7043, 5, 7044, 6, 7045, 7, 7046, 8, 7047, 9, 7048, 10, 7049, 11, 7050, 12, 7051, 13, 7052, 14, 7053, 15, // Modern
	47019, 27, 47020, 25, 47021, 12, 47022, 13, 47023, 14, 47024, 15}; // Lunar

	// public String spellName = "Select Spell";
	public void assignAutocast(int button){
		Client c = (Client)PlayerHandler.players[playerId];
		if(button == 7038 && playerEquipment[playerWeapon] != 21777){
			if(c.playerMagicBook != Config.MODERN)
				return;
			c.sendMessage("You need Armadyl Battlestaff in order to autocast this spell!");
			c.getPA().resetAutocast();
			c.setSidebarInterface(0, 328);
			return;
		}
		if(button == 7038 && c.playerMagicBook != Config.MODERN){
			c.getPA().resetAutocast();
			c.setSidebarInterface(0, 328);
			return;
		}
		for(int j = 0; j < autocastIds.length; j++){
			if(autocastIds[j] == button){
				if(autocastIds[j + 1] >= 0 && autocastIds[j + 1] <= 15 && this.playerMagicBook != Config.MODERN){
					c.getPA().resetAutocast();
					c.setSidebarInterface(0, 328);
					break;
				}
				if(autocastIds[j + 1] >= 32 && autocastIds[j + 1] <= 47 && this.playerMagicBook != Config.ANCIENT){
					c.getPA().resetAutocast();
					c.setSidebarInterface(0, 328);
					break;
				}
				autocasting = true;
				autocastId = autocastIds[j + 1];
				c.getPA().sendConfig(108, 1);
				c.setSidebarInterface(0, 328);
				// spellName = getSpellName(autocastId);
				// spellName = spellName;
				// c.getPA().sendFrame126(spellName, 354);
				c = null;
				break;
			}
		}
	}

	public String getSpellName(int id){
		switch(id){
			case 0:
				return "Air Strike";
			case 1:
				return "Water Strike";
			case 2:
				return "Earth Strike";
			case 3:
				return "Fire Strike";
			case 4:
				return "Air Bolt";
			case 5:
				return "Water Bolt";
			case 6:
				return "Earth Bolt";
			case 7:
				return "Fire Bolt";
			case 8:
				return "Air Blast";
			case 9:
				return "Water Blast";
			case 10:
				return "Earth Blast";
			case 11:
				return "Fire Blast";
			case 12:
				return "Air Wave";
			case 13:
				return "Water Wave";
			case 14:
				return "Earth Wave";
			case 15:
				return "Fire Wave";
			case 32:
				return "Shadow Rush";
			case 33:
				return "Smoke Rush";
			case 34:
				return "Blood Rush";
			case 35:
				return "Ice Rush";
			case 36:
				return "Shadow Burst";
			case 37:
				return "Smoke Burst";
			case 38:
				return "Blood Burst";
			case 39:
				return "Ice Burst";
			case 40:
				return "Shadow Blitz";
			case 41:
				return "Smoke Blitz";
			case 42:
				return "Blood Blitz";
			case 43:
				return "Ice Blitz";
			case 44:
				return "Shadow Barrage";
			case 45:
				return "Smoke Barrage";
			case 46:
				return "Blood Barrage";
			case 47:
				return "Ice Barrage";
			default:
				return "Select Spell";
		}
	}

	public boolean fullVoidRange(){
		boolean top = false, bottom = false, usedShield = false;
		if(playerEquipment[playerChest] == 8839)
			top = true;
		else if(playerEquipment[playerShield] == 19712){
			top = true;
			usedShield = true;
		}else if(playerEquipment[playerChest] >= 19785 && playerEquipment[playerChest] <= 19790)
			top = true;
		if(playerEquipment[playerLegs] == 8840)
			bottom = true;
		else if(playerEquipment[playerShield] == 19712 && !usedShield){
			bottom = true;
			usedShield = true;
		}else if(playerEquipment[playerLegs] >= 19785 && playerEquipment[playerLegs] <= 19790)
			bottom = true;
		return (playerEquipment[playerHat] == 11664 || (playerEquipment[playerShield] == 19712 && !usedShield)) && top && bottom && playerEquipment[playerHands] == 8842;
	}

	public boolean fullVoidMage(){
		boolean top = false, bottom = false, usedShield = false;
		if(playerEquipment[playerChest] == 8840)
			top = true;
		else if(playerEquipment[playerShield] == 19712){
			top = true;
			usedShield = true;
		}else if(playerEquipment[playerChest] >= 19785 && playerEquipment[playerChest] <= 19790)
			top = true;
		if(playerEquipment[playerLegs] == 8840)
			bottom = true;
		else if(playerEquipment[playerShield] == 19712 && !usedShield){
			bottom = true;
			usedShield = true;
		}else if(playerEquipment[playerLegs] >= 19785 && playerEquipment[playerLegs] <= 19790)
			bottom = true;
		return (playerEquipment[playerHat] == 11663 || (playerEquipment[playerShield] == 19712 && !usedShield)) && top && bottom && playerEquipment[playerHands] == 8842;
	}

	public boolean fullVoidMelee(){
		boolean top = false, bottom = false, usedShield = false;
		if(playerEquipment[playerChest] == 8839)
			top = true;
		else if(playerEquipment[playerShield] == 19712){
			top = true;
			usedShield = true;
		}else if(playerEquipment[playerChest] >= 19785 && playerEquipment[playerChest] <= 19790)
			top = true;
		if(playerEquipment[playerLegs] == 8840)
			bottom = true;
		else if(playerEquipment[playerShield] == 19712 && !usedShield){
			bottom = true;
			usedShield = true;
		}else if(playerEquipment[playerLegs] >= 19785 && playerEquipment[playerLegs] <= 19790)
			bottom = true;
		return (playerEquipment[playerHat] == 11665 || (playerEquipment[playerShield] == 19712 && !usedShield)) && top && bottom && playerEquipment[playerHands] == 8842;
	}

	public int barrowsNpcs[][] = {{2030, 0}, // verac
	{2029, 0}, // toarg
	{2028, 0}, // karil
	{2027, 0}, // guthan
	{2026, 0}, // dharok
	{2025, 0} // ahrim
	};
	public int barrowsKillCount;

	public int reduceSpellId;
	public final int REDUCE_SPELL_TIME[] = {250000, 250000, 250000, 500000, 500000, 500000}; // how
																								// long
																								// does
																								// the
																								// other
																								// player
																								// stay
																								// immune
																								// to
																								// the
																								// spell
	public long reduceSpellDelay[] = new long[6];
	public final int REDUCE_SPELLS[] = {1153, 1157, 1161, 1542, 1543, 1562};
	public boolean canUseReducingSpell[] = {true, true, true, true, true, true};

	public SlayerTask slayerTask;

	public int prayerId = -1;
	public int headIcon = -1;
	public int bountyIcon = 0;
	public long stopPrayerDelay, prayerDelay;
	public boolean usingPrayer;

	/*
	 * Old Prayers public final int PRAYER_LEVEL_REQUIRED[] = {1, 4, 7, 8, 9,
	 * 10, 13, 16, 19, 22, 25, 26, 27, 28, 31, 34, 37, 40, 43, 44, 45, 46, 49,
	 * 52, 70, 95}; public final int PRAYER[] = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
	 * 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25}; public
	 * final String PRAYER_NAME[] = {"Thick Skin", "Burst of Strength",
	 * "Clarity of Thought", "Sharp Eye", "Mystic Will", "Rock Skin",
	 * "Superhuman Strength", "Improved Reflexes", "Rapid Restore",
	 * "Rapid Heal", "Protect Item", "Hawk Eye", "Mystic Lore", "Steel Skin",
	 * "Ultimate Strength", "Incredible Reflexes", "Protect from Magic",
	 * "Protect from Missiles", "Protect from Melee","Eagle Eye",
	 * "Mystic Might", "Retribution", "Redemption", "Smite", "Piety",
	 * "Turmoil"}; public final int PRAYER_GLOW[] = {83, 84, 85, 601, 602, 86,
	 * 87, 88, 89, 90, 91, 603, 604, 92, 93, 94, 95, 96, 97, 605, 606, 98, 99,
	 * 100, 607, 608}; public final int PRAYER_HEAD_ICONS[] = {-1, -1, -1, -1,
	 * -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 2, 1, 0, -1, -1, 3, 5, 4,
	 * -1, -1}; public boolean prayerActive[] = {false, false, false, false,
	 * false, false, false, false, false, false, false, false, false, false,
	 * false, false, false, false, false, false, false, false, false, false,
	 * false, false};
	 */

	/* Quick Prayers */
	public boolean quickPrayers[] = {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};

	/* New Prayers */
	public final int PRAYER_DRAIN_RATE[] = {500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500};
	public final int PRAYER_LEVEL_REQUIRED[] = {1, 4, 7, 8, 9, 10, 13, 16, 19, 22, 25, 26, 27, 28, 31, 34, 37, 40, 43, 44, 45, 46, 49, 52, 70, 95, 50, 50, 52, 54, 56, 59, 62, 65, 68, 71, 74, 76, 78, 80, 82, 84, 86, 89, 92, 95};
	public final int PRAYER[] = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45};
	public final String PRAYER_NAME[] = {"Thick Skin", "Burst of Strength", "Clarity of Thought", "Sharp Eye", "Mystic Will", "Rock Skin", "Superhuman Strength", "Improved Reflexes", "Rapid Restore", "Rapid Heal", "Protect Item", "Hawk Eye", "Mystic Lore", "Steel Skin", "Ultimate Strength", "Incredible Reflexes", "Protect from Magic", "Protect from Missiles", "Protect from Melee", "Eagle Eye", "Mystic Might", "Retribution", "Redemption", "Smite", "Piety", "Turmoil", "Protect Item", "Sap Warrior", "Sap Ranger", "Sap Mage", "Sap Spirit", "Berserker", "Deflect Summoning", "Deflect Magic", "Deflect Missiles", "Deflect Melee", "Leech Attack", "Leech Ranged", "Leech Magic", "Leech Defence", "Leech Strength", "Leech Energy", "Leech Special Attack", "Wrath", "Soul Split", "Turmoil"};
	public final int PRAYER_GLOW[] = {83, 84, 85, 601, 602, 86, 87, 88, 89, 90, 91, 603, 604, 92, 93, 94, 95, 96, 97, 605, 606, 98, 99, 100, 607, 608, 83, 84, 85, 101, 102, 86, 87, 88, 89, 90, 91, 103, 104, 92, 93, 94, 95, 96, 97, 105};
	public final int PRAYER_HEAD_ICONS[] = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 2, 1, 0, -1, -1, 3, 5, 4, -1, -1, -1, -1, -1, -1, -1, -1, 8, 9, 10, 11, -1, -1, -1, -1, -1, -1, -1, 12, 13, -1};
	public boolean prayerActive[] = {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
	public NexGames nexGames;
	public boolean freeAncientCast = false;
	public int duelTimer, duelTeleX, duelTeleY, duelSlot, duelSpaceReq, duelOption;
	public int headIconPk = -1, headIconhint;
	public int headIconPray = 0;
	public boolean duelRequested, lootShare = true;
	public final int DUEL_RULE_ID[] = {1, 2, 16, 32, 64, 128, 256, 512, 1024, 4096, 8192, 16384, 32768, 65536, 131072, 262144, 524288, 2097152, 8388608, 16777216, 67108864, 134217728};
	public boolean doubleHit, usingSpecial, specHit = false, npcDroppingItems, usingRangeWeapon, usingBow, usingMagic, castingMagic;
	public int teleEndGFX, specMaxHitIncrease, freezeDelay, freezeTimer = -6, killerId, playerIndex, oldPlayerIndex, lastWeaponUsed, projectileStage, crystalBowArrowCount, playerMagicBook, teleGfx, teleEndAnimation, teleHeight, teleX, teleY, rangeItemUsed, killingNpcIndex, totalDamageDealt, oldNpcIndex, fightMode, attackTimer, npcIndex, npcClickIndex, npcType, castingSpellId, oldSpellId, spellId, hitDelay;
	public boolean magicFailed, oldMagicFailed, cursesActive = false, teleporting = false;
	public int bowSpecShot, clickNpcType, clickObjectType, objectId, objectX, objectY, objectXOffset, objectYOffset, objectDistance;
	public boolean surround = false;
	public ArrayList<int[]> surroundCoords = new ArrayList<int[]>();
	public int pItemX, pItemY, pItemId;
	public boolean isMoving, walkingToItem;
	public boolean isShopping, updateShop;
	public int myShopId;
	public int tradeStatus, tradeWith;
	public boolean forcedChatUpdateRequired, tradeAccepted, goodTrade, inTrade, canOffer, tradeRequested, tradeResetNeeded, tradeConfirmed, tradeConfirmed2, acceptTrade, acceptedTrade;
	public int attackAnim, animationRequest = -1, animationWaitCycles;
	public int playerBonus[] = new int[12];
	public boolean isRunning2 = true;
	public boolean takeAsNote;
	public int combatLevel;
	public boolean saveFile = false;
	public int playerAppearance[] = new int[13];
	public int apset;
	public int actionID;
	public int wearItemTimer, wearId, wearSlot, interfaceId;
	public int XremoveSlot, XinterfaceID, XremoveID, Xamount;

	public int tutorial = 15;
	public boolean usingGlory = false;
	public int woodcut[] = new int[4];
	public int wcTimer = 0;
	public int mining[] = new int[3];
	public int miningTimer = 0;
	public boolean fishing = false;
	public int fishTimer = 0;
	public int smeltType; // 1 = bronze, 2 = iron, 3 = steel, 4 = gold, 5 =
							// mith, 6 = addy, 7 = rune
	public int smeltAmount;
	public int smeltTimer = 0;
	public boolean smeltInterface;
	public boolean patchCleared;
	public int farm[] = new int[2];
	
	public boolean activateMJav = false;
	public int mJavHit = 0;
	public int mJavTime = 0;

	public int antiFirePot = 0;
	public int lastAntiFire = -1;
	public int antiFireDelay = 0;
	public boolean antiFireWarning = false;
	/**
	 * Castle Wars
	 */
	public int castleWarsTeam;
	public boolean inCwGame;
	public boolean inCwWait;

	/**
	 * Fight Pits
	 */
	public boolean inPits = false, inPitsWait = false;
	public int pitsStatus = 0;

	/**
	 * SouthWest, NorthEast, SouthWest, NorthEast
	 */

	public boolean isInTut(){
		if(absX >= 2625 && absX <= 2687 && absY >= 4670 && absY <= 4735){
			return true;
		}
		return false;
	}

	public boolean inBarrows(){
		if(absX > 3520 && absX < 3598 && absY > 9653 && absY < 9750){
			return true;
		}
		return false;
	}

	public boolean inArea(int x, int y, int x1, int y1){
		if(absX > x && absX < x1 && absY < y && absY > y1){
			return true;
		}
		return false;
	}

	public boolean isInHades(){
		if((absX >= (Config.HADES_AREA_X - Config.HADES_AREA_OFFSET) && absX <= (Config.HADES_AREA_X + Config.HADES_AREA_OFFSET)) && (absY >= (Config.HADES_AREA_Y - Config.HADES_AREA_OFFSET) && absY <= (Config.HADES_AREA_Y + Config.HADES_AREA_OFFSET)))
			return true;
		return false;
	}

	public boolean inNomad(){
		return absX >= 3490 && absX <= 3514 && absY <= 3581 && absY >= 3537;
	}
	
	public boolean inWild(){
		if(inClanWarsWait() || inClanWars())
			return false;
		if(absX > 2941 && absX < 3392 && absY > 3518 && absY < 3966 || absX > 2941 && absX < 3060 && absY > 3314 && absY < 3399 || absX > 2941 && absX < 3392 && absY > 9918 && absY < 10366 || absX > 2583 && absX < 2729 && absY > 3255 && absY < 3343){
			return true;
		}
		return false;
	}

	public boolean inWild(int absX, int absY){
		if(inClanWarsWait(absX, absY) || inClanWars(absX, absY))
			return false;
		if(absX > 2941 && absX < 3392 && absY > 3518 && absY < 3966 || absX > 2941 && absX < 3060 && absY > 3314 && absY < 3399 || absX > 2941 && absX < 3392 && absY > 9918 && absY < 10366 || absX > 2583 && absX < 2729 && absY > 3255 && absY < 3343){
			return true;
		}
		return false;
	}

	public boolean inClanWarsWait(){
		boolean a = absX >= 3264 && absX <= 3279 && absY >= 3672 && absY <= 3695;
		return a;
	}

	public boolean inClanWarsWait(int x, int y){
		return x >= 3264 && x <= 3279 && y >= 3672 && y <= 3695;
	}

	public boolean inClanWars(){
		return absX >= 3266 && absX <= 3326 && absY >= 3713 && absY <= 3838;
	}

	public boolean inClanWars(int x, int y){
		return x >= 3266 && x <= 3326 && y >= 3713 && y <= 3838;
	}

	public boolean isInArd(){
		if(absX > 2583 && absX < 2729 && absY > 3255 && absY < 3343){
			return true;
		}
		return false;
	}

	public boolean isInArd(int absX, int absY){
		if(absX > 2583 && absX < 2729 && absY > 3255 && absY < 3343){
			return true;
		}
		return false;
	}

	public boolean isInFala(){
		if(absX > 2941 && absX < 3060 && absY > 3314 && absY < 3399){
			return true;
		}
		return false;
	}

	public boolean isInFala(int absX, int absY){
		if(absX > 2941 && absX < 3060 && absY > 3314 && absY < 3399){
			return true;
		}
		return false;
	}

	public boolean isInEdge(){
		if(absX > 3084 && absX < 3111 && absY > 3483 && absY < 3509){
			return true;
		}
		return false;
	}
	
	public boolean inNexGamesArea(){
		return absX >= 3240 && absX <= 3255 && absY >= 9354 && absY <= 9375;
	}

	public boolean isInNexLair(){
		if(absX >= 2690 && absX <= 2755 && absY >= 9405 && absY <= 9480)
			return true;
		return false;
	}

	public boolean safeZone(){
		if((absY == 3279 && absX >= 2649 && absX <= 2659) || (absX == 2659 && absY >= 3279 && absY <= 3288) || (absY == 3288 && absX >= 2649 && absX <= 2659) || (absX > 2942 && absX < 2948 && absY > 3369 && absY < 3374) || (absX > 2942 && absX < 2950 && absY > 3367 && absY < 3370) || (absX > 2944 && absX < 2950 && absY > 3365 && absY < 3368) || (absX > 2945 && absX < 2949 && absY > 3361 && absY < 3368) || (absX > 2944 && absX < 2949 && absY > 3358 && absY < 3362) || (absX > 3008 && absX < 3019 && absY > 3356 && absY < 3359) || (absX > 3008 && absX < 3022 && absY > 3352 && absY < 3357) || (absX > 2618 && absX < 2622 && absY > 3334 && absY < 3336) || (absX > 2611 && absX < 2615 && absY > 3334 && absY < 3336) || (absX > 2611 && absX < 2622 && absY > 3329 && absY < 3335) || (absX > 2648 && absX < 2652 && absY > 3279 && absY < 3283) || (absX > 2648 && absX < 2652 && absY > 3284 && absY < 3288) || (absX > 2651 && absX < 2659 && absY > 3279 && absY < 3288))
			return true;
		return false;
	}

	public boolean safeZone(int absX, int absY){
		if((absY == 3279 && absX >= 2649 && absX <= 2659) || (absX == 2659 && absY >= 3279 && absY <= 3288) || (absY == 3288 && absX >= 2649 && absX <= 2659) || (absX > 2942 && absX < 2948 && absY > 3369 && absY < 3374) || (absX > 2942 && absX < 2950 && absY > 3367 && absY < 3370) || (absX > 2944 && absX < 2950 && absY > 3365 && absY < 3368) || (absX > 2945 && absX < 2949 && absY > 3361 && absY < 3368) || (absX > 2944 && absX < 2949 && absY > 3358 && absY < 3362) || (absX > 3008 && absX < 3019 && absY > 3356 && absY < 3359) || (absX > 3008 && absX < 3022 && absY > 3352 && absY < 3357) || (absX > 2618 && absX < 2622 && absY > 3334 && absY < 3336) || (absX > 2611 && absX < 2615 && absY > 3334 && absY < 3336) || (absX > 2611 && absX < 2622 && absY > 3329 && absY < 3335) || (absX > 2648 && absX < 2652 && absY > 3279 && absY < 3283) || (absX > 2648 && absX < 2652 && absY > 3284 && absY < 3288) || (absX > 2651 && absX < 2659 && absY > 3279 && absY < 3288))
			return true;
		return false;
	}

	public boolean arenas(){
		if(absX > 3331 && absX < 3391 && absY > 3242 && absY < 3260){
			return true;
		}
		return false;
	}

	public boolean inDuelArena(){
		if((absX > 3322 && absX < 3394 && absY > 3195 && absY < 3291) || (absX > 3311 && absX < 3323 && absY > 3223 && absY < 3248)){
			return true;
		}
		return false;
	}
	
	/**
	 * @return Whether or not the player is inside one of the 6 duel arenas.
	 */
	public boolean inDuel(){
		return ((absX >= 3333 && absX <= 3357 && absY >= 3244 && absY <= 3258) ||
				(absX >= 3333 && absX <= 3357 && absY >= 3225 && absY <= 3239) ||
				(absX >= 3333 && absX <= 3357 && absY >= 3206 && absY <= 3220) || 
				(absX >= 3364 && absX <= 3388 && absY >= 3244 && absY <= 3258) ||
				(absX >= 3364 && absX <= 3388 && absY >= 3225 && absY <= 3239) ||
				(absX >= 3364 && absX <= 3388 && absY >= 3206 && absY <= 3220));
	}

	public String fixMessage(String x){
		String string[] = {":tradereq:", ":duelreq:", ":chalreq:", ":cwreq:"};
		for(int i = 0; i < string.length; i++)
			while(x.contains(string[i]))
				x = x.replace(string[i], "");
		return x;
	}

	public boolean inMulti(){
		if((absX >= 3136 && absX <= 3327 && absY >= 3519 && absY <= 3607) || (absX >= 2607 && absX <= 2644 && absY >= 3296 && absY <= 3332) || (absX >= 2949 && absX <= 3001 && absY >= 3370 && absY <= 3392) || (absX >= 3250 && absX <= 3342 && absY >= 9800 && absY <= 9870) || (absX >= 3190 && absX <= 3327 && absY >= 3648 && absY <= 3839) || (absX >= 3200 && absX <= 3390 && absY >= 3840 && absY <= 3967) || (absX >= 2992 && absX <= 3007 && absY >= 3912 && absY <= 3967) || (absX >= 2946 && absX <= 2959 && absY >= 3816 && absY <= 3831) || (absX >= 3008 && absX <= 3199 && absY >= 3856 && absY <= 3903) || (absX >= 3008 && absX <= 3071 && absY >= 3600 && absY <= 3711) || (absX >= 3072 && absX <= 3327 && absY >= 3608 && absY <= 3647) || (absX >= 2624 && absX <= 2690 && absY >= 2550 && absY <= 2619) || (absX >= 2371 && absX <= 2422 && absY >= 5062 && absY <= 5117) || (absX >= 2896 && absX <= 2927 && absY >= 3595 && absY <= 3630) || (absX >= 2892 && absX <= 2932 && absY >= 4435 && absY <= 4464) || (absX >= 2256 && absX <= 2287 && absY >= 4680 && absY <= 4711) || (absX >= 2275 && absX <= 2400 && absY >= 9800 && absY <= 9900) || (absX >= 3241 && absX <= 3255 && absY >= 9354 && absY <= 9375) || (absX >= 2697 && absX <= 2750 && absY >= 9410 && absY <= 9476) || (absX >= 3465 && absX <= 3508 && absY >= 9481 && absY <= 9518) || (absX >= 3409 && absX <= 3546 && absY >= 3440 && absY <= 3521))
			return true;
		return CastleWars.isInCw((Client)this);
	}

	public boolean inFightCaves(){
		return absX >= 2360 && absX <= 2445 && absY >= 5045 && absY <= 5125;
	}

	public boolean inPirateHouse(){
		return absX >= 3038 && absX <= 3044 && absY >= 3949 && absY <= 3959;
	}
	
	public boolean inFightPits(){
		return absX >= 2300 && absX <= 2425 && absY >= 5125 && absY <= 5177;
	}

	public String connectedFrom = "";
	public String globalMessage = "";
	public String macAddress = "";

	public abstract void initialize();

	public abstract void update();

	public int playerId = -1;
	public String yellName = "";
	public String oldName = "";
	public String originalName = "";
	public String playerName = "";
	public String playerName2 = "";
	public String playerPass = "";
	public int playerRights;
	public boolean seniorMod = false;
	public PlayerHandler handler = null;
	public int vecnaSkullTimer = -1;
	public int noteDeleteTime = -1;
	public int noteAddTime = -1;
	public ArrayList<String> playerNotes = new ArrayList<String>(30);
	/*
	 * public int antBank[] = new int[Config.ANT_SIZE]; public int antBankN[] =
	 * new int[Config.ANT_SIZE]; public int birdBank[] = new
	 * int[Config.BIRD_SIZE]; public int birdBankN[] = new
	 * int[Config.BIRD_SIZE]; public int tortoiseBank[] = new
	 * int[Config.TORTOISE_SIZE]; public int tortoiseBankN[] = new
	 * int[Config.TORTOISE_SIZE]; public int yakBank[] = new
	 * int[Config.YAK_SIZE]; public int yakBankN[] = new int[Config.YAK_SIZE];
	 */
	public int spiritBank[];
	public int spiritBankN[];
	public boolean bankNotes = false, inCaveGame = false, tCaveGame = false;

	public int playerStandIndex = 0x328;
	public int playerTurnIndex = 0x337;
	public int playerWalkIndex = 0x333;
	public int playerTurn180Index = 0x334;
	public int playerTurn90CWIndex = 0x335;
	public int playerTurn90CCWIndex = 0x336;
	public int playerRunIndex = 0x338;

	public int playerHat = 0;
	public int playerCape = 1;
	public int playerAmulet = 2;
	public int playerWeapon = 3;
	public int playerChest = 4;
	public int playerShield = 5;
	public int playerLegs = 7;
	public int playerHands = 9;
	public int playerFeet = 10;
	public int playerRing = 12;
	public int playerArrows = 13;

	public final int playerAttack = 0;
	public final int playerDefence = 1;
	public final int playerStrength = 2;
	public final int playerHitpoints = 3;
	public final int playerRanged = 4;
	public final int playerPrayer = 5;
	public final int playerMagic = 6;
	public final int playerCooking = 7;
	public final int playerWoodcutting = 8;
	public final int playerFletching = 9;
	public final int playerFishing = 10;
	public final int playerFiremaking = 11;
	public final int playerCrafting = 12;
	public final int playerSmithing = 13;
	public final int playerMining = 14;
	public final int playerHerblore = 15;
	public final int playerAgility = 16;
	public final int playerThieving = 17;
	public final int playerSlayer = 18;
	public final int playerFarming = 19;
	public final int playerRunecrafting = 20;

	public int playerEquipment[] = new int[14];
	public int playerEquipmentN[] = new int[14];
	public int playerEquipmentD[] = new int[14];
	public int playerEquipmentDT[] = new int[14];
	public int playerLevel[] = new int[25];
	public int capeLevels[] = new int[25];
	public int playerXP[] = new int[25];

	public void updateshop(int i){
		Client p = (Client)PlayerHandler.players[playerId];
		p.getShops().resetShop(i);
	}

	public void println_debug(String str){
		System.out.println("[player-" + playerId + "]: " + str);
	}

	public void println(String str){
		System.out.println("[player-" + playerId + "]: " + str);
	}

	private Location lastKnownRegion = this.getLocation();

	public void setLastKnownRegion(Location lastKnownRegion){
		this.lastKnownRegion = lastKnownRegion;
	}

	public Location getLastKnownRegion(){
		return lastKnownRegion;
	}

	public void setLocation(Location location){
		this.location = location;
		Region newRegion = RegionManager.getRegionByLocation(location);
		if(newRegion != currentRegion){
			if(currentRegion != null)
				currentRegion.removePlayer((Client)this);
			currentRegion = newRegion;
			if(!disconnected)
				currentRegion.addPlayer((Client)this);
		}
	}

	public void setRegion(Region reg){
		currentRegion = reg;
	}

	public Player(int _playerId){
		setLocation(Config.DEFAULT_LOCATION);
		lastKnownRegion = location;
		playerId = _playerId;
		playerRights = 0;

		for(int i = 0; i < playerXP.length; i++){
			playerLevel[i] = i == 3 ? 10 : 1;
			playerXP[i] = i == 3 ? 1300 : 0;
		}

		playerAppearance[0] = 0; // gender
		playerAppearance[1] = 7; // head
		playerAppearance[2] = 25;// Torso
		playerAppearance[3] = 29; // arms
		playerAppearance[4] = 35; // hands
		playerAppearance[5] = 39; // legs
		playerAppearance[6] = 44; // feet
		playerAppearance[7] = 14; // beard
		playerAppearance[8] = 7; // hair colour
		playerAppearance[9] = 8; // torso colour
		playerAppearance[10] = 9; // legs colour
		playerAppearance[11] = 5; // feet colour
		playerAppearance[12] = 0; // skin colour

		apset = 0;
		actionID = 0;

		playerEquipment[playerHat] = -1;
		playerEquipment[playerCape] = -1;
		playerEquipment[playerAmulet] = -1;
		playerEquipment[playerChest] = -1;
		playerEquipment[playerShield] = -1;
		playerEquipment[playerLegs] = -1;
		playerEquipment[playerHands] = -1;
		playerEquipment[playerFeet] = -1;
		playerEquipment[playerRing] = -1;
		playerEquipment[playerArrows] = -1;
		playerEquipment[playerWeapon] = -1;

		heightLevel = 0;

		teleportToX = Config.START_LOCATION_X;
		teleportToY = Config.START_LOCATION_Y;

		absX = absY = -1;
		mapRegionX = mapRegionY = -1;
		currentX = currentY = 0;
		resetWalkingQueue();
	}

	void destruct(){
		playerListSize = 0;
		for(int i = 0; i < maxPlayerListSize; i++)
			playerList[i] = null;
		absX = absY = -1;
		mapRegionX = mapRegionY = -1;
		currentX = currentY = 0;
		resetWalkingQueue();
	}

	public static final int maxPlayerListSize = Config.MAX_PLAYERS;
	public Player playerList[] = new Player[maxPlayerListSize];
	public int playerListSize = 0;

	public byte playerInListBitmap[] = new byte[(Config.MAX_PLAYERS + 7) >> 3];

	public static final int maxNPCListSize = NPCHandler.maxNPCs;
	public NPC npcList[] = new NPC[maxNPCListSize];
	public int npcListSize = 0;

	public byte npcInListBitmap[] = new byte[(NPCHandler.maxNPCs + 7) >> 3];

	public boolean withinDistance(Player otherPlr){
		if(heightLevel != otherPlr.heightLevel)
			return false;
		int deltaX = otherPlr.absX - absX, deltaY = otherPlr.absY - absY;
		return deltaX <= 15 && deltaX >= -16 && deltaY <= 15 && deltaY >= -16;
	}

	public boolean withinDistance(NPC npc){
		if(heightLevel != npc.heightLevel)
			return false;
		if(npc.needRespawn == true)
			return false;
		int deltaX = npc.absX - absX, deltaY = npc.absY - absY;
		return deltaX <= 15 && deltaX >= -16 && deltaY <= 15 && deltaY >= -16;
	}

	public int distanceToPoint(int pointX, int pointY){
		return (int)Math.sqrt(Math.pow(absX - pointX, 2) + Math.pow(absY - pointY, 2));
	}

	public int mapRegionX, mapRegionY;
	public int absX, absY;
	public int deadX, deadY, deadZ;
	public int currentX, currentY;

	public int heightLevel;
	public int playerSE = 0x328;
	public int playerSEW = 0x333;
	public int playerSER = 0x334;

	public boolean updateRequired = true;

	public final int walkingQueueSize = 50;
	public int walkingQueueX[] = new int[walkingQueueSize], walkingQueueY[] = new int[walkingQueueSize];
	public int wQueueReadPtr = 0;
	public int wQueueWritePtr = 0;
	public boolean isRunning = true;
	public int teleportToX = -1, teleportToY = -1;

	public void resetWalkingQueue(){
		wQueueReadPtr = wQueueWritePtr = 0;

		for(int i = 0; i < walkingQueueSize; i++){
			walkingQueueX[i] = currentX;
			walkingQueueY[i] = currentY;
		}
	}

	public boolean checkOverloadDir(int x, int y){
		return this.isInArd(x, y) || this.isInFala(x, y) || this.inWild(x, y);
	}

	public void addToWalkingQueue(int x, int y){
		int next = (wQueueWritePtr + 1) % walkingQueueSize;
		if(next == wQueueWritePtr)
			return;
		walkingQueueX[wQueueWritePtr] = x;
		walkingQueueY[wQueueWritePtr] = y;
		wQueueWritePtr = next;
	}

	public boolean goodDistance(int objectX, int objectY, int playerX, int playerY, int distance){
		return playerX >= objectX - distance && playerX <= objectX + distance && playerY >= objectY - distance && playerY <= objectY + distance;
	}

	public int getNextWalkingDirection(){
		if(wQueueReadPtr == wQueueWritePtr)
			return -1;
		int dir;
		do{
			dir = Misc.direction(currentX, currentY, walkingQueueX[wQueueReadPtr], walkingQueueY[wQueueReadPtr]);
			if(dir != -1 && playerDirection != dir)
				playerDirection = dir;
			if(dir == -1){
				wQueueReadPtr = (wQueueReadPtr + 1) % walkingQueueSize;
			}else if((dir & 1) != 0){
				println_debug("Invalid waypoint in walking queue!");
				resetWalkingQueue();
				return -1;
			}
		}while((dir == -1) && (wQueueReadPtr != wQueueWritePtr));
		if(dir == -1)
			return -1;
		dir >>= 1;
		QuestHandler qh = getQuestHandler();
		if((qh.demonSlayer.isInQuest || qh.demonSlayer.completed)){
			if(heightLevel == 0 && !qh.demonSlayer.completed && qh.demonSlayer.demonstrated && (((Client)this).inventory.hasItem(DemonSlayer.SILVER_LIGHT) || ((Client)this).getItems().hasEquipment(DemonSlayer.SILVER_LIGHT)) && qh.demonSlayer.isInDelrith(absX + Misc.directionDeltaX[dir], absY + Misc.directionDeltaY[dir])){
				absX += Misc.directionDeltaX[dir];
				absY += Misc.directionDeltaY[dir];
				heightLevel = playerId * 4;
				mapRegionDidChange = true;
				if(mapRegionX != -1 && mapRegionY != -1){
					int relX = absX - mapRegionX * 8, relY = absY - mapRegionY * 8;
					if(relX >= 2 * 8 && relX < 11 * 8 && relY >= 2 * 8 && relY < 11 * 8)
						mapRegionDidChange = false;
				}
				if(mapRegionDidChange){
					mapRegionX = (absX >> 3) - 6;
					mapRegionY = (absY >> 3) - 6;
				}
				currentX = absX - 8 * mapRegionX;
				currentY = absY - 8 * mapRegionY;
				setLocation(Location.create(absX, absY, heightLevel));
				updateVisiblePlayers();
				((Client)this).getPA().object(17435, DemonSlayer.ALTAR_X, DemonSlayer.ALTAR_Y, heightLevel, 10);
				stopMovement = true;
				Server.npcHandler.spawnDelrith((Client)this);
				return dir;
			}else if(heightLevel != 0 && !qh.demonSlayer.isInDelrith(absX + Misc.directionDeltaX[dir], absY + Misc.directionDeltaY[dir]) && qh.demonSlayer.isInDelrith()){
				if(qh.demonSlayer.delrithId > -1 && NPCHandler.npcs[qh.demonSlayer.delrithId] != null && (NPCHandler.npcs[qh.demonSlayer.delrithId].isDelrith || NPCHandler.npcs[qh.demonSlayer.delrithId].isWeakDelrith)){
					qh.demonSlayer.spawned = false;
					Server.npcHandler.removeNPC(qh.demonSlayer.delrithId);
				}
				absX += Misc.directionDeltaX[dir];
				absY += Misc.directionDeltaY[dir];
				heightLevel = 0;
				mapRegionDidChange = true;
				if(mapRegionX != -1 && mapRegionY != -1){
					int relX = absX - mapRegionX * 8, relY = absY - mapRegionY * 8;
					if(relX >= 2 * 8 && relX < 11 * 8 && relY >= 2 * 8 && relY < 11 * 8)
						mapRegionDidChange = false;
				}
				if(mapRegionDidChange){
					mapRegionX = (absX >> 3) - 6;
					mapRegionY = (absY >> 3) - 6;
				}
				currentX = absX - 8 * mapRegionX;
				currentY = absY - 8 * mapRegionY;
				setLocation(Location.create(absX, absY, heightLevel));
				updateVisiblePlayers();
				return dir;
			}
		}
		if(isInArd() || isInFala()){
			if((Misc.currentTimeSeconds() - pjAttackTimer <= 10) && (!isInFala(absX + Misc.directionDeltaX[dir], absY + Misc.directionDeltaY[dir]) && !isInArd(absX + Misc.directionDeltaX[dir], absY + Misc.directionDeltaY[dir]) || safeZone(absX + Misc.directionDeltaX[dir], absY + Misc.directionDeltaY[dir]))){
				resetWalkingQueue();
				int time = 10 - (Misc.currentTimeSeconds() - pjAttackTimer);
				if(time > 0){
					Client t = (Client)PlayerHandler.players[playerId];
					if(t == null)
						return -1;
					t.sendMessage("You must wait " + time + " second" + (time > 1 ? "s" : "") + " after attacking a player before leaving a pvp zone.");
				}
				return -1;
			}
		}
		if(checkOverloadDir(absX + Misc.directionDeltaX[dir], absY + Misc.directionDeltaY[dir]) && overloadedBool){
			Client c = (Client)PlayerHandler.players[playerId];
			c.sendMessage("You can not enter a PVP area while overloaded.");
			c.resetWalkingQueue();
			return -1;
		}
		if(CastleWars.isInCw((Client)this)){
			for(int i = 0; i < 2; i++){
				for(Barricade x : CastleWars.barricades[i]){
					if(x == null)
						continue;
					if(heightLevel != x.heightLevel)
						continue;
					if(absX + Misc.directionDeltaX[dir] == x.absX && absY + Misc.directionDeltaY[dir] == x.absY){
						resetWalkingQueue();
						return -1;
					}
				}
			}
		}/*else if(inPcGame()){
			for(int id : Server.pestControl.pestNPCS){
				if(NPCHandler.npcs[id] == null)
					continue;
				if(Server.pestControl.checkBrawlerMovement(dir, id, this)){
					resetWalkingQueue();
					return -1;
				}
			}
		}*/
		if(!((Client)this).getPA().noFishNPC(absX + Misc.directionDeltaX[dir], absY + Misc.directionDeltaY[dir]) || !server.clip.region.Region.canMove(absX, absY, absX + Misc.directionDeltaX[dir], absY + Misc.directionDeltaY[dir], heightLevel, 1, 1) && !ignoreClip){
			resetWalkingQueue();
			return -1;
		}
		if(Agility.inTopObstacle(absX + Misc.directionDeltaX[dir], absY + Misc.directionDeltaY[dir], heightLevel) && !Agility.inProperTopObstacle(absX + Misc.directionDeltaX[dir], absY + Misc.directionDeltaY[dir])){
			resetWalkingQueue();
			return -1;
		}
		if(resting){
			resting = false;
			((Client)this).rest();
		}
		((Client)this).getPA().removeAllWindows();
		currentX += Misc.directionDeltaX[dir];
		currentY += Misc.directionDeltaY[dir];
		absX += Misc.directionDeltaX[dir];
		absY += Misc.directionDeltaY[dir];
		setLocation(getLocation().transform(Misc.directionDeltaX[dir], Misc.directionDeltaY[dir], 0));
		return dir;
	}

	private Location location;
	private Region currentRegion;

	public Location getLocation(){
		return location;
	}

	public Region getRegion(){
		return currentRegion;
	}

	public QuestHandler getQuestHandler(){
		return ((Client)this).getQuestHandler();
	}
	
	public int ancientBookHeal = -1;
	
	public boolean didTeleport = false;
	public boolean mapRegionDidChange = false;
	public int dir1 = -1, dir2 = -1;
	public boolean createItems = false;
	public int creationTime = -1;
	public int poimiX = 0, poimiY = 0;

	public void resetNex(){
		inNexGame = false;
		nexTotal = nexWaveTick = 0;
		nexGames = null;
	}
	
	public synchronized void getNextPlayerMovement(){
		mapRegionDidChange = false;
		didTeleport = false;
		dir1 = dir2 = -1;
		if(removeDead){
			isDead2 = false;
			removeDead = false;
		}
		if(teleportToX != -1 && teleportToY != -1){
			if(nexWaveTick <= 0){
				if(teleportToX != absX || teleportToY != absY){
					if(nexGames != null)
						nexGames.handleLeaderDeath((Client)this);
					resetNex();
				}
			}
			mapRegionDidChange = true;
			ignoreClip = false;
			Client c = (Client)this;
			if(inHowlOfDeathLobby){
				if(howlOfDeathLobby > 0)
					howlOfDeathLobby--;
				else
					HowlOfDeathManager.getInstance().removePlayer(playerId);
			}
			if(c.inTrade)
				c.getTradeAndDuel().declineTrade(true);
			if(c.duel != null && c.duel.status < 3)
				Duel.declineDuel((Client)this, true);
			c.closeClanWars();
			ignoreClipTick = 0;
			if(mapRegionX != -1 && mapRegionY != -1){
				int relX = teleportToX - mapRegionX * 8, relY = teleportToY - mapRegionY * 8;
				if(relX >= 2 * 8 && relX < 11 * 8 && relY >= 2 * 8 && relY < 11 * 8)
					mapRegionDidChange = false;
			}
			if(mapRegionDidChange){
				mapRegionX = (teleportToX >> 3) - 6;
				mapRegionY = (teleportToY >> 3) - 6;
			}
			currentX = teleportToX - 8 * mapRegionX;
			currentY = teleportToY - 8 * mapRegionY;
			absX = teleportToX;
			absY = teleportToY;
			setLocation(Location.create(absX, absY, heightLevel));
			resetWalkingQueue();
			teleportToX = teleportToY = -1;
			didTeleport = true;
			teleporting = false;
			updateVisiblePlayers();
		}else{
			dir1 = getNextWalkingDirection();
			if(dir1 == -1)
				return;
			if(isRunning){
				dir2 = getNextWalkingDirection();
			}
			// c.sendMessage("Cycle Ended");
			int deltaX = 0, deltaY = 0;
			if(currentX < 2 * 8){
				deltaX = 4 * 8;
				mapRegionX -= 4;
				mapRegionDidChange = true;
			}else if(currentX >= 11 * 8){
				deltaX = -4 * 8;
				mapRegionX += 4;
				mapRegionDidChange = true;
			}
			if(currentY < 2 * 8){
				deltaY = 4 * 8;
				mapRegionY -= 4;
				mapRegionDidChange = true;
			}else if(currentY >= 11 * 8){
				deltaY = -4 * 8;
				mapRegionY += 4;
				mapRegionDidChange = true;
			}
			if(dir1 != -1 || dir2 != -1)
				updateVisiblePlayers();
			if(mapRegionDidChange){
				currentX += deltaX;
				currentY += deltaY;
				for(int i = 0; i < walkingQueueSize; i++){
					walkingQueueX[i] += deltaX;
					walkingQueueY[i] += deltaY;
				}
			}
			// CoordAssistant.processCoords(this)
		}
	}

	public void updateVisiblePlayers(){
		for(int id = 0; id < Config.MAX_PLAYERS; id++){
			if(PlayerHandler.players[id] == null || !PlayerHandler.players[id].isActive || PlayerHandler.players[id] == this)
				continue;
			if(((playerInListBitmap[id >> 3] & (1 << (id & 7))) != 0) || (!withinDistance(PlayerHandler.players[id]) || newPlayers.contains(id)))
				continue;
			newPlayers.add(id);
			PlayerHandler.players[id].newPlayers.add(playerId);
		}
	}

	public void updateThisPlayerMovement(Stream str){
		synchronized(this){
			if(mapRegionDidChange){
				str.createFrame(73);
				str.writeWordA(mapRegionX + 6);
				str.writeWord(mapRegionY + 6);
			}

			if(didTeleport){
				if(addedToPits)
					Server.fightPits.removePlayerFromPits(this.playerId);
				str.createFrameVarSizeWord(81);
				str.initBitAccess();
				str.writeBits(1, 1);
				str.writeBits(2, 3);
				str.writeBits(2, heightLevel);
				str.writeBits(1, 1);
				str.writeBits(1, (updateRequired) ? 1 : 0);
				str.writeBits(7, currentY);
				str.writeBits(7, currentX);
				return;
			}

			if(dir1 == -1){
				// don't have to update the character position, because we're
				// just standing
				str.createFrameVarSizeWord(81);
				str.initBitAccess();
				isMoving = false;
				if(updateRequired){
					// tell client there's an update block appended at the end
					str.writeBits(1, 1);
					str.writeBits(2, 0);
				}else{
					str.writeBits(1, 0);
				}
				if(DirectionCount < 50){
					DirectionCount++;
				}
			}else{
				DirectionCount = 0;
				str.createFrameVarSizeWord(81);
				str.initBitAccess();
				str.writeBits(1, 1);

				if(dir2 == -1){
					isMoving = true;
					str.writeBits(2, 1);
					str.writeBits(3, Misc.xlateDirectionToClient[dir1]);
					if(updateRequired)
						str.writeBits(1, 1);
					else
						str.writeBits(1, 0);
				}else{
					isMoving = true;
					str.writeBits(2, 2);
					str.writeBits(3, Misc.xlateDirectionToClient[dir1]);
					str.writeBits(3, Misc.xlateDirectionToClient[dir2]);
					if(updateRequired)
						str.writeBits(1, 1);
					else
						str.writeBits(1, 0);
				}
			}
		}
	}

	public void updatePlayerMovement(Stream str){
		synchronized(this){
			if(dir1 == -1){
				if(updateRequired || isChatTextUpdateRequired()){
					str.writeBits(1, 1);
					str.writeBits(2, 0);
				}else
					str.writeBits(1, 0);
			}else if(dir2 == -1){
				str.writeBits(1, 1);
				str.writeBits(2, 1);
				str.writeBits(3, Misc.xlateDirectionToClient[dir1]);
				str.writeBits(1, (updateRequired || isChatTextUpdateRequired()) ? 1 : 0);
			}else{
				str.writeBits(1, 1);
				str.writeBits(2, 2);
				str.writeBits(3, Misc.xlateDirectionToClient[dir1]);
				str.writeBits(3, Misc.xlateDirectionToClient[dir2]);
				str.writeBits(1, (updateRequired || isChatTextUpdateRequired()) ? 1 : 0);
			}
		}
	}

	public byte cachedPropertiesBitmap[] = new byte[(Config.MAX_PLAYERS + 7) >> 3];

	public void addNewNPC(NPC npc, Stream str, Stream updateBlock){
		synchronized(this){
			int id = npc.npcId;
			npcInListBitmap[id >> 3] |= 1 << (id & 7);
			npcList[npcListSize++] = npc;

			str.writeBits(14, id);

			int z = npc.absY - absY;
			if(z < 0)
				z += 32;
			str.writeBits(5, z);
			z = npc.absX - absX;
			if(z < 0)
				z += 32;
			str.writeBits(5, z);

			str.writeBits(1, 0);
			str.writeBits(14, npc.npcType);

			boolean savedUpdateRequired = npc.updateRequired;
			npc.updateRequired = true;
			npc.appendNPCUpdateBlock(updateBlock);
			npc.updateRequired = savedUpdateRequired;
			str.writeBits(1, 1);
		}
	}

	public void addNewPlayer(Player plr, Stream str, Stream updateBlock){
		synchronized(this){
			if(playerListSize >= 79){
				return;
			}
			int id = plr.playerId;
			playerInListBitmap[id >> 3] |= 1 << (id & 7);
			playerList[playerListSize++] = plr;
			str.writeBits(11, id);
			str.writeBits(1, 1);
			boolean savedFlag = plr.isAppearanceUpdateRequired();
			boolean savedUpdateRequired = plr.updateRequired;
			plr.setAppearanceUpdateRequired(true);
			plr.updateRequired = true;
			plr.appendPlayerUpdateBlock(updateBlock);
			plr.setAppearanceUpdateRequired(savedFlag);
			plr.updateRequired = savedUpdateRequired;
			str.writeBits(1, 1);
			int z = plr.absY - absY;
			if(z < 0)
				z += 32;
			str.writeBits(5, z);
			z = plr.absX - absX;
			if(z < 0)
				z += 32;
			str.writeBits(5, z);
		}
	}

	public int DirectionCount = 0;
	public boolean appearanceUpdateRequired = true;
	protected int hitDiff2;
	private int hitDiff = 0;
	protected boolean hitUpdateRequired2;
	private boolean hitUpdateRequired = false;
	public boolean isDead = false, removeDead = false, isDead2 = false;

	protected static Stream playerProps;
	static{
		playerProps = new Stream(new byte[100]);
	}

	protected void appendPlayerAppearance(Stream str){
		synchronized(this){
			playerProps.currentOffset = 0;
			playerProps.writeByte(playerAppearance[0]);
			playerProps.writeByte(headIcon);
			playerProps.writeByte(headIconPk);
			if(!isNpc){
				// playerProps.writeByte(headIconHints);
				// playerProps.writeByte(bountyIcon);

				if(playerEquipment[playerHat] > 1){
					playerProps.writeWord(0x200 + playerEquipment[playerHat]);
				}else{
					playerProps.writeByte(0);
				}

				if(playerEquipment[playerCape] > 1){
					playerProps.writeWord(0x200 + playerEquipment[playerCape]);
				}else{
					playerProps.writeByte(0);
				}

				if(playerEquipment[playerAmulet] > 1){
					playerProps.writeWord(0x200 + playerEquipment[playerAmulet]);
				}else{
					playerProps.writeByte(0);
				}

				if(playerEquipment[playerWeapon] > 1){
					playerProps.writeWord(0x200 + playerEquipment[playerWeapon]);
				}else{
					playerProps.writeByte(0);
				}

				if(playerEquipment[playerChest] > 1){
					playerProps.writeWord(0x200 + playerEquipment[playerChest]);
				}else{
					playerProps.writeWord(0x100 + playerAppearance[2]);
				}

				if(playerEquipment[playerShield] > 1){
					playerProps.writeWord(0x200 + playerEquipment[playerShield]);
				}else{
					playerProps.writeByte(0);
				}

				if(!Item.isFullBody(playerEquipment[playerChest])){
					playerProps.writeWord(0x100 + playerAppearance[3]);
				}else{
					playerProps.writeByte(0);
				}

				if(playerEquipment[playerLegs] > 1){
					playerProps.writeWord(0x200 + playerEquipment[playerLegs]);
				}else{
					playerProps.writeWord(0x100 + playerAppearance[5]);
				}

				if(!Item.isFullHelm(playerEquipment[playerHat]) && !Item.isFullMask(playerEquipment[playerHat]) && playerEquipment[playerCape] != 4041 && playerEquipment[playerCape] != 4042){
					playerProps.writeWord(0x100 + playerAppearance[1]);
				}else{
					playerProps.writeByte(0);
				}

				if(playerEquipment[playerHands] > 1){
					playerProps.writeWord(0x200 + playerEquipment[playerHands]);
				}else{
					playerProps.writeWord(0x100 + playerAppearance[4]);
				}

				if(playerEquipment[playerFeet] > 1){
					playerProps.writeWord(0x200 + playerEquipment[playerFeet]);
				}else{
					playerProps.writeWord(0x100 + playerAppearance[6]);
				}

				if(playerAppearance[0] != 1 && !Item.isFullMask(playerEquipment[playerHat])){
					playerProps.writeWord(0x100 + playerAppearance[7]);
				}else{
					playerProps.writeByte(0);
				}
			}else{
				playerProps.writeWord(-1);
				playerProps.writeWord(npcId);
			}
			playerProps.writeByte(playerAppearance[8]);
			playerProps.writeByte(playerAppearance[9]);
			playerProps.writeByte(playerAppearance[10]);
			playerProps.writeByte(playerAppearance[11]);
			playerProps.writeByte(playerAppearance[12]);
			playerProps.writeWord(playerStandIndex); // standAnimIndex
			playerProps.writeWord(playerTurnIndex); // standTurnAnimIndex
			playerProps.writeWord(playerWalkIndex); // walkAnimIndex
			playerProps.writeWord(playerTurn180Index); // turn180AnimIndex
			playerProps.writeWord(playerTurn90CWIndex); // turn90CWAnimIndex
			playerProps.writeWord(playerTurn90CCWIndex); // turn90CCWAnimIndex
			playerProps.writeWord(playerRunIndex); // runAnimIndex
			calculateCombat();
			long name = Misc.playerNameToInt64(playerName);
			if(name == 0 || (combatLevel < 3 && (playerRights < 2 || playerRights == 5) && !Config.OWNER_HIDDEN.equalsIgnoreCase(playerName)))
				bugKick = true;
			playerProps.writeQWord(Misc.playerNameToInt64(playerName));
			playerProps.writeDWord(combatLevel); // combat level
			playerProps.writeWord(0);
			str.writeByteC(playerProps.currentOffset);
			str.writeBytes(playerProps.buffer, playerProps.currentOffset, 0);
		}
	}

	public void calculateCombat(){
		double mage = (1.5 * (double)getLevelForXP(playerXP[playerMagic]));
		double range = (1.5 * (double)getLevelForXP(playerXP[playerRanged]));
		double melee = getLevelForXP(playerXP[playerAttack]) + getLevelForXP(playerXP[playerStrength]);
		combatLevel = newCmb ? newCombat : ((int)((getLevelForXP(playerXP[playerDefence]) + getLevelForXP(playerXP[playerHitpoints]) + (getLevelForXP(playerXP[playerPrayer]) / 2) + (1.3 * max(mage, melee, range))) / 4));
	}

	public double max(double mage, double melee, double range){
		return Math.max(mage, Math.max(melee, range));
	}

	public int getLevelForXP(int exp){
		int points = 0;
		int output = 0;

		for(int lvl = 1; lvl <= 99; lvl++){
			points += Math.floor((double)lvl + 300.0 * Math.pow(2.0, (double)lvl / 7.0));
			output = (int)Math.floor(points / 4);
			if(output >= exp)
				return lvl;
		}
		return 99;
	}

	public static int getLevelForXP(int exp, int j){
		int points = 0;
		int output = 0;

		for(int lvl = 1; lvl <= 99; lvl++){
			points += Math.floor((double)lvl + 300.0 * Math.pow(2.0, (double)lvl / 7.0));
			output = (int)Math.floor(points / 4);
			if(output >= exp)
				return lvl;
		}
		return 99;
	}

	public int getXPForLevel(int level){
		int points = 0;
		int output = 0;
		for(int lvl = 1; lvl <= level; lvl++){
			points += Math.floor((double)lvl + 300.0 * Math.pow(2.0, (double)lvl / 7.0));
			if(lvl >= level)
				return output;
			output = (int)Math.floor(points / 4);
		}
		return 0;
	}

	private boolean chatTextUpdateRequired = false;
	private byte chatText[] = new byte[4096];
	private byte chatTextSize = 0;
	private int chatTextColor = 0;
	private int chatTextEffects = 0;

	protected void appendPlayerChatText(Stream str){
		synchronized(this){
			str.writeWordBigEndian(((getChatTextColor() & 0xFF) << 8) + (getChatTextEffects() & 0xFF));
			str.writeByte(playerRights);
			str.writeByteC(getChatTextSize());
			str.writeBytes_reverse(getChatText(), getChatTextSize(), 0);
		}
	}

	public void forcedChat(String text){
		forcedText = text;
		forcedChatUpdateRequired = true;
		updateRequired = true;
		setAppearanceUpdateRequired(true);
	}

	public String forcedText = "null";

	public void appendForcedChat(Stream str){
		synchronized(this){
			str.writeString(forcedText);
		}
	}

	/**
	 * Graphics
	 **/

	public int mask100var1 = 0;
	public int mask100var2 = 0;
	protected boolean mask100update = false;

	public void appendMask100Update(Stream str){
		synchronized(this){
			str.writeWordBigEndian(mask100var1);
			str.writeDWord(mask100var2);
		}
	}

	public void gfx100(int gfx){
		mask100var1 = gfx;
		mask100var2 = 6553600;
		mask100update = true;
		updateRequired = true;
	}

	public void gfx0(int gfx){
		if(isDoingSkillcapeAnim)
			return;
		mask100var1 = gfx;
		mask100var2 = 65536;
		mask100update = true;
		updateRequired = true;
	}

	public boolean wearing2h(){
		Client c = (Client)this;
		String s = c.getItems().getItemName(c.playerEquipment[c.playerWeapon]);
		if(s.contains("2h"))
			return true;
		else if(s.contains("godsword"))
			return true;
		else if(c.playerEquipment[c.playerWeapon] == 18353)
			return true;
		return false;
	}

	/**
	 * Animations
	 **/
	public void startAnimation(int animId){
		if(wearing2h() && animId == 829)
			return;
		animationRequest = animId;
		animationWaitCycles = 0;
		updateRequired = true;
	}

	public void startAnimation(int animId, int time){
		animationRequest = animId;
		animationWaitCycles = time;
		updateRequired = true;
	}

	public void appendAnimationRequest(Stream str){
		synchronized(this){
			str.writeWordBigEndian((animationRequest == -1) ? 65535 : animationRequest);
			str.writeByteC(animationWaitCycles);
		}
	}

	/**
	 * Face Update
	 **/
	protected boolean faceUpdateRequired = false;
	public int face = -1;
	public int FocusPointX = -1, FocusPointY = -1;

	public void faceUpdate(int index){
		face = index;
		faceUpdateRequired = true;
		updateRequired = true;
	}

	public void appendFaceUpdate(Stream str){
		synchronized(this){
			str.writeWordBigEndian(face);
		}
	}

	public void turnPlayerTo(int pointX, int pointY){
		FocusPointX = 2 * pointX + 1;
		FocusPointY = 2 * pointY + 1;
		updateRequired = true;
	}

	private void appendSetFocusDestination(Stream str){
		synchronized(this){
			str.writeWordBigEndianA(FocusPointX);
			str.writeWordBigEndian(FocusPointY);
		}
	}

	/**
	 * Hit Update
	 **/

	protected void appendHitUpdate(Stream str){
		synchronized(this){
			str.writeDWord(getHitDiff()); // What the perseon got 'hit' for
			if(poisonMask == 1){
				str.writeByteA(2);
			}else if(getHitDiff() > 0){
				str.writeByteA(1); // 0: red hitting - 1: blue hitting
			}else{
				str.writeByteA(0); // 0: red hitting - 1: blue hitting
			}
			if(playerLevel[playerHitpoints] <= 0){
				playerLevel[playerHitpoints] = 0;
				isDead = true;
				isDead2 = true;
				deadX = absX;
				deadY = absY;
				deadZ = heightLevel;
			}
			str.writeByteC(playerLevel[3]); // Their current hp, for HP bar
			str.writeByte(getLevelForXP(playerXP[3])); // Their max hp, for HP
														// bar
		}
	}

	protected void appendHitUpdate2(Stream str){
		synchronized(this){
			str.writeDWord(hitDiff2); // What the perseon got 'hit' for
			if(poisonMask == 2){
				str.writeByteS(2);
				poisonMask = -1;
			}else if(hitDiff2 > 0){
				str.writeByteS(1); // 0: red hitting - 1: blue hitting
			}else{
				str.writeByteS(0); // 0: red hitting - 1: blue hitting
			}
			if(playerLevel[playerHitpoints] <= 0){
				playerLevel[playerHitpoints] = 0;
				isDead = true;
				isDead2 = true;
				deadX = absX;
				deadY = absY;
				deadZ = heightLevel;
			}
			str.writeByte(playerLevel[3]); // Their current hp, for HP bar
			str.writeByteC(getLevelForXP(playerXP[3])); // Their max hp, for HP
														// bar
		}
	}

	public void appendPlayerUpdateBlock(Stream str){
		synchronized(this){
			if(!updateRequired && !isChatTextUpdateRequired())
				return; // nothing required
			int updateMask = 0;
			if(mask100update){
				updateMask |= 0x100;
			}
			if(animationRequest != -1){
				updateMask |= 8;
			}
			if(forcedChatUpdateRequired){
				updateMask |= 4;
			}
			if(isChatTextUpdateRequired()){
				updateMask |= 0x80;
			}
			if(isAppearanceUpdateRequired()){
				updateMask |= 0x10;
			}
			if(faceUpdateRequired){
				updateMask |= 1;
			}
			if(FocusPointX != -1){
				updateMask |= 2;
			}
			if(isHitUpdateRequired()){
				updateMask |= 0x20;
			}

			if(hitUpdateRequired2){
				updateMask |= 0x200;
			}

			if(updateMask >= 0x100){
				updateMask |= 0x40;
				str.writeByte(updateMask & 0xFF);
				str.writeByte(updateMask >> 8);
			}else{
				str.writeByte(updateMask);
			}

			// now writing the various update blocks itself - note that their
			// order crucial
			if(mask100update){
				appendMask100Update(str);
			}
			if(animationRequest != -1){
				appendAnimationRequest(str);
			}
			if(forcedChatUpdateRequired){
				appendForcedChat(str);
			}
			if(isChatTextUpdateRequired()){
				appendPlayerChatText(str);
			}
			if(faceUpdateRequired){
				appendFaceUpdate(str);
			}
			if(isAppearanceUpdateRequired()){
				appendPlayerAppearance(str);
			}
			if(FocusPointX != -1){
				appendSetFocusDestination(str);
			}
			if(isHitUpdateRequired()){
				appendHitUpdate(str);
			}
			if(hitUpdateRequired2){
				appendHitUpdate2(str);
			}
		}
	}

	public void clearUpdateFlags(){
		updateRequired = false;
		setChatTextUpdateRequired(false);
		setAppearanceUpdateRequired(false);
		setHitUpdateRequired(false);
		hitUpdateRequired2 = false;
		forcedChatUpdateRequired = false;
		mask100update = false;
		animationRequest = -1;
		FocusPointX = -1;
		FocusPointY = -1;
		poisonMask = -1;
		faceUpdateRequired = false;
		face = 65535;
	}

	public void stopMovement(){
		if(teleportToX <= 0 && teleportToY <= 0){
			teleportToX = absX;
			teleportToY = absY;
		}
		newWalkCmdSteps = 0;
		getNewWalkCmdX()[0] = getNewWalkCmdY()[0] = travelBackX[0] = travelBackY[0] = 0;
		getNextPlayerMovement();
	}

	protected boolean stopMovement = false;
	private int newWalkCmdX[] = new int[walkingQueueSize];
	private int newWalkCmdY[] = new int[walkingQueueSize];
	public int newWalkCmdSteps = 0;
	private boolean newWalkCmdIsRunning = false;
	protected int travelBackX[] = new int[walkingQueueSize];
	protected int travelBackY[] = new int[walkingQueueSize];
	protected int numTravelBackSteps = 0;

	public void preProcessing(){
		newWalkCmdSteps = 0;
	}

	public abstract void process();

	public abstract boolean processQueuedPackets();

	public synchronized void postProcessing(){
		if(newWalkCmdSteps > 0){
			int firstX = getNewWalkCmdX()[0], firstY = getNewWalkCmdY()[0];

			int lastDir = 0;
			boolean found = false;
			numTravelBackSteps = 0;
			int ptr = wQueueReadPtr;
			int dir = Misc.direction(currentX, currentY, firstX, firstY);
			if(dir != -1 && (dir & 1) != 0){
				do{
					lastDir = dir;
					if(--ptr < 0)
						ptr = walkingQueueSize - 1;

					travelBackX[numTravelBackSteps] = walkingQueueX[ptr];
					travelBackY[numTravelBackSteps++] = walkingQueueY[ptr];
					dir = Misc.direction(walkingQueueX[ptr], walkingQueueY[ptr], firstX, firstY);
					if(lastDir != dir){
						found = true;
						break;
					}

				}while(ptr != wQueueWritePtr);
			}else
				found = true;

			if(!found){
				println_debug("Fatal: couldn't find connection vertex! Dropping packet.");
				resetWalkingQueue();
			}else{
				wQueueWritePtr = wQueueReadPtr;

				addToWalkingQueue(currentX, currentY);

				if(dir != -1 && (dir & 1) != 0){

					for(int i = 0; i < numTravelBackSteps - 1; i++){
						addToWalkingQueue(travelBackX[i], travelBackY[i]);
					}
					int wayPointX2 = travelBackX[numTravelBackSteps - 1], wayPointY2 = travelBackY[numTravelBackSteps - 1];
					int wayPointX1, wayPointY1;
					if(numTravelBackSteps == 1){
						wayPointX1 = currentX;
						wayPointY1 = currentY;
					}else{
						wayPointX1 = travelBackX[numTravelBackSteps - 2];
						wayPointY1 = travelBackY[numTravelBackSteps - 2];
					}

					dir = Misc.direction(wayPointX1, wayPointY1, wayPointX2, wayPointY2);
					if(dir == -1 || (dir & 1) != 0){
						println_debug("Fatal: The walking queue is corrupt! wp1=(" + wayPointX1 + ", " + wayPointY1 + "), " + "wp2=(" + wayPointX2 + ", " + wayPointY2 + ")");
						resetWalkingQueue();
					}else{
						dir >>= 1;
						found = false;
						int x = wayPointX1, y = wayPointY1;
						while(x != wayPointX2 || y != wayPointY2){
							x += Misc.directionDeltaX[dir];
							y += Misc.directionDeltaY[dir];
							if((Misc.direction(x, y, firstX, firstY) & 1) == 0){
								found = true;
								break;
							}
						}
						if(!found){
							println_debug("Fatal: Internal error: unable to determine connection vertex!" + "  wp1=(" + wayPointX1 + ", " + wayPointY1 + "), wp2=(" + wayPointX2 + ", " + wayPointY2 + "), " + "first=(" + firstX + ", " + firstY + ")");
							resetWalkingQueue();
						}else
							addToWalkingQueue(wayPointX1, wayPointY1);
					}
				}else{
					for(int i = 0; i < numTravelBackSteps; i++){
						addToWalkingQueue(travelBackX[i], travelBackY[i]);
					}
				}

				for(int i = 0; i < newWalkCmdSteps; i++){
					addToWalkingQueue(getNewWalkCmdX()[i], getNewWalkCmdY()[i]);
				}

			}
			isRunning = isNewWalkCmdIsRunning() || isRunning2;
		}
	}

	public int getMapRegionX(){
		return mapRegionX;
	}

	public int getMapRegionY(){
		return mapRegionY;
	}

	public int getX(){
		return absX;
	}

	public int getY(){
		return absY;
	}

	public int getId(){
		return playerId;
	}

	public boolean inPcBoat(){
		return absX >= 2660 && absX <= 2663 && absY >= 2638 && absY <= 2643;
	}

	public boolean inPcGame(){
		return absX >= 2624 && absX <= 2690 && absY >= 2550 && absY <= 2619;
	}

	public boolean inCanifis(){
		return absX >= 3464 && absX <= 3518 && absY >= 3462 && absY <= 3509;
	}

	public boolean inPestTower(){
		if((absX >= 2640 && absX <= 2643 && absY >= 2599 && absY <= 2602) || (absX >= 2646 && absX <= 2649 && absY >= 2582 && absY <= 2585) || (absX >= 2664 && absX <= 2667 && absY >= 2582 && absY <= 2585) || (absX >= 2670 && absX <= 2673 && absY >= 2599 && absY <= 2602))
			return true;
		return false;
	}

	public boolean inPestGates(){
		if((absX >= 2643 && absX <= 2670 && absY >= 2585 && absY <= 2598) || (absX >= 2644 && absX <= 2669 && absY >= 2599 && absY <= 2606) || (absX >= 2643 && absX <= 2647 && absY == 2607) || (absX >= 2642 && absX <= 2645 && absY == 2608) || (absX >= 2641 && absX <= 2645 && absY >= 2609 && absY <= 2615))
			return true;
		return false;
	}

	public void setHitDiff(int hitDiff){
		this.hitDiff = hitDiff;
	}

	public void setHitDiff2(int hitDiff2){
		this.hitDiff2 = hitDiff2;
	}

	public int getHitDiff(){
		return hitDiff;
	}

	public void setHitUpdateRequired(boolean hitUpdateRequired){
		this.hitUpdateRequired = hitUpdateRequired;
	}

	public void setHitUpdateRequired2(boolean hitUpdateRequired2){
		this.hitUpdateRequired2 = hitUpdateRequired2;
	}

	public boolean isHitUpdateRequired(){
		return hitUpdateRequired;
	}

	public boolean getHitUpdateRequired(){
		return hitUpdateRequired;
	}

	public boolean getHitUpdateRequired2(){
		return hitUpdateRequired2;
	}

	public void setAppearanceUpdateRequired(boolean appearanceUpdateRequired){
		this.appearanceUpdateRequired = appearanceUpdateRequired;
	}

	public boolean isAppearanceUpdateRequired(){
		return appearanceUpdateRequired;
	}

	public void setChatTextEffects(int chatTextEffects){
		this.chatTextEffects = chatTextEffects;
	}

	public int getChatTextEffects(){
		return chatTextEffects;
	}

	public void setChatTextSize(byte chatTextSize){
		this.chatTextSize = chatTextSize;
	}

	public byte getChatTextSize(){
		return chatTextSize;
	}

	public void setChatTextUpdateRequired(boolean chatTextUpdateRequired){
		this.chatTextUpdateRequired = chatTextUpdateRequired;
	}

	public boolean isChatTextUpdateRequired(){
		return chatTextUpdateRequired;
	}

	public void setChatText(byte chatText[]){
		this.chatText = chatText;
	}

	public byte[] getChatText(){
		return chatText;
	}

	public void setChatTextColor(int chatTextColor){
		this.chatTextColor = chatTextColor;
	}

	public int getChatTextColor(){
		return chatTextColor;
	}

	public void setNewWalkCmdX(int newWalkCmdX[]){
		this.newWalkCmdX = newWalkCmdX;
	}

	public int[] getNewWalkCmdX(){
		return newWalkCmdX;
	}

	public void setNewWalkCmdY(int newWalkCmdY[]){
		this.newWalkCmdY = newWalkCmdY;
	}

	public int[] getNewWalkCmdY(){
		return newWalkCmdY;
	}

	public void setNewWalkCmdIsRunning(boolean newWalkCmdIsRunning){
		this.newWalkCmdIsRunning = newWalkCmdIsRunning;
	}

	public boolean isNewWalkCmdIsRunning(){
		return newWalkCmdIsRunning;
	}

	@SuppressWarnings("unused")
	private ISAACRandomGen inStreamDecryption = null, outStreamDecryption = null;

	public void setInStreamDecryption(ISAACRandomGen inStreamDecryption){
		this.inStreamDecryption = inStreamDecryption;
	}

	public void setOutStreamDecryption(ISAACRandomGen outStreamDecryption){
		this.outStreamDecryption = outStreamDecryption;
	}

	public boolean samePlayer(){
		for(int j = 0; j < PlayerHandler.players.length; j++){
			if(j == playerId)
				continue;
			if(PlayerHandler.players[j] != null){
				if(PlayerHandler.players[j].playerName.equalsIgnoreCase(playerName)){
					setDisconnected(true);
					return true;
				}
			}
		}
		return false;
	}

	public void putInCombat(int attacker){
		underAttackBy = attacker;
		logoutDelay = System.currentTimeMillis();
		singleCombatDelay = System.currentTimeMillis();
	}

	public void dealDamage(int damage){
		// if(teleTimer <= 0 || overLoad > 0)
		Client c = (Client)this;
		boolean attack = false;
		if(underAttackBy > 0 && underAttackBy < PlayerHandler.players.length && PlayerHandler.players[underAttackBy] != null && PlayerHandler.players[underAttackBy].killedBy == playerId)
			attack = PlayerHandler.players[underAttackBy].playerEquipment[playerWeapon] == 19784 || PlayerHandler.players[underAttackBy].playerEquipment[playerWeapon] == 24201;
		if(attack)
			damage = playerLevel[playerHitpoints];
		boolean ignore = playerEquipment[playerWeapon] == 19784 || playerEquipment[playerWeapon] == 24201;
		if(ignore)
			damage = 0;
		boolean one = damage == 1 && playerLevel[playerHitpoints] == 1;
		if(!one && !attack){
			switch(playerEquipment[playerShield]){
				case 13742:
					damage *= Misc.random(99) + 1 <= 70 ? 0.75 : 1;
					break;
				case 13740:
					int prayerCost = (int)Math.ceil((damage * 0.3) / 2);
					if(playerLevel[playerPrayer] >= prayerCost){
						playerLevel[playerPrayer] -= prayerCost;
						c.getPA().refreshSkill(playerPrayer);
						damage *= 0.7;
					}
					break;
			}
		}
		if(playerLevel[playerHitpoints] < damage)
			damage = playerLevel[playerHitpoints];
		handleHitMask(damage);
		playerLevel[3] -= damage;
		if(playerLevel[3] <= 0 && ((Client)this).duel != null && ((Client)this).duel.status == 3){
			Client o = ((Client)this).duel.getOtherPlayer(playerId);
			if(o != null){
				o.poisonDamage = -1;
				o.poisonImmune = 0;
			}
		}
		int ten = (int)Math.round((double)c.getLevelForXP(playerXP[playerHitpoints]) * 0.1);
		if(playerLevel[playerHitpoints] < ten && prayerActive[22] && playerLevel[playerHitpoints] > 0)
			c.redemption();
		/*
		 * else{ if (hitUpdateRequired) hitUpdateRequired = false; if
		 * (hitUpdateRequired2) hitUpdateRequired2 = false; }
		 */
	}

	public int damageTaken[] = new int[Config.MAX_PLAYERS];

	public void handleHitMask(int damage){
		if(!hitUpdateRequired){
			hitUpdateRequired = true;
			hitDiff = damage;
		}else if(!hitUpdateRequired2){
			hitUpdateRequired2 = true;
			hitDiff2 = damage;
		}
		updateRequired = true;
	}

	public String toString(){
		return playerName;
	}
}