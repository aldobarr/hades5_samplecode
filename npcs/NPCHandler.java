package server.model.npcs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import server.Config;
import server.Server;
import server.model.minigames.CastleWars;
import server.model.minigames.NexGames;
import server.model.players.Client;
import server.model.players.Player;
import server.model.players.PlayerHandler;
import server.model.players.skills.fishing.Spot;
import server.model.quests.DemonSlayer;
import server.model.region.Location;
import server.model.region.RegionManager;
import server.task.Task;
import server.util.Misc;
import server.clip.region.Region;

public class NPCHandler{
	public static int guideChatTimer = 13;
	public static int maxNPCs = 10000;
	public static int maxListedNPCs = 10000;
	public static int maxNPCDrops = 10000;
	public static int QueenStuff[] = null;
	public static boolean QueenTwo = false;
	public static boolean queenOneRespawn = false;
	public static int queenOneTimer = 0;
	public static int queenOneMaxTime = 0;
	public static NPC npcs[] = new NPC[maxNPCs];
	public static NPCList NpcList[] = new NPCList[maxListedNPCs];

	public NPCHandler(){
		for(int i = 0; i < maxNPCs; i++){
			npcs[i] = null;
		}
		for(int i = 0; i < maxListedNPCs; i++){
			NpcList[i] = null;
		}
		loadNPCList("./Data/cfg/npc.cfg");
		loadAutoSpawn("./Data/cfg/spawn-config.cfg");
	}
	
	public void removeNPC(int id){
		NPCHandler.npcs[id].absX = 20;
		NPCHandler.npcs[id].absY = 20;
		NPCHandler.npcs[id].updateRequired = true;
		NPCHandler.npcs[id].getRegion().removeNpc(NPCHandler.npcs[id]);
		NPCHandler.npcs[id] = null;
	}

	public void spawnNpcSummoning(Client c, int npcType, int x, int y, int heightLevel, int WalkingType, int HP, int maxHit, int attack, int defence, boolean attackPlayer, boolean headIcon, boolean summonFollow, boolean gfx){
		int slot = -1;
		for(int i = 1; i < maxNPCs; i++){
			if(npcs[i] == null){
				slot = i;
				break;
			}
		}
		if(slot == -1)
			return;
		NPC newNPC = new NPC(slot, npcType);
		c.summonedSlot = slot;
		newNPC.setLocation(Location.create(x, y, heightLevel));
		newNPC.absX = x;
		newNPC.absY = y;
		newNPC.makeX = x;
		newNPC.makeY = y;
		newNPC.heightLevel = heightLevel;
		newNPC.walkingType = WalkingType;
		newNPC.HP = HP;
		newNPC.MaxHP = HP;
		newNPC.maxHit = maxHit;
		newNPC.attack = attack;
		newNPC.defence = defence;
		newNPC.spawnedBy = c.getId();
		newNPC.underAttackBy = c.getId();
		newNPC.underAttack = true;
		newNPC.killerId = c.getId();
		if(summonFollow){
			newNPC.summoned = true;
			newNPC.facePlayer(newNPC.killerId);
		}
		if(gfx)
			newNPC.gfx0(1308);// summon gfx
		if(attackPlayer)
			newNPC.underAttack = true;
		npcs[slot] = newNPC;
	}

	public void multiAttackGfx(int i, int gfx){
		if(npcs[i].projectileId < 0)
			return;
		for(Player p : RegionManager.getLocalPlayers(npcs[i].getLocation())){
			if(p != null){
				Client c = (Client)p;
				if(c.heightLevel != npcs[i].heightLevel)
					continue;
				if(p.goodDistance(c.absX, c.absY, npcs[i].absX, npcs[i].absY, 15)){
					int nX = NPCHandler.npcs[i].getX() + offset(i);
					int nY = NPCHandler.npcs[i].getY() + offset(i);
					int pX = c.getX();
					int pY = c.getY();
					int offX = (nY - pY) * -1;
					int offY = (nX - pX) * -1;
					c.getPA().createPlayersProjectile(nX, nY, offX, offY, 50, getProjectileSpeed(i), npcs[i].projectileId, 43, 31, -c.getId() - 1, 65);
				}
			}
		}
	}

	public boolean switchesAttackers(int i){
		switch(npcs[i].npcType){
			case 6261:
			case 6263:
			case 6265:
			case 6223:
			case 6225:
			case 6227:
			case 6248:
			case 6250:
			case 6252:
			case 2892:
			case 2894:
				return true;

		}

		return false;
	}

	public void multiAttackDamage(int i){
		int max = getMaxHit(i);
		int tempdam = 0;
		ArrayList<Player> players = new ArrayList<Player>(RegionManager.getLocalPlayers(npcs[i].getLocation()));
		int doTele = Misc.random(players.size() - 1);
		for(int j = 0; j<players.size(); j++){
			Player p = players.get(j);
			if(p != null){
				Client c = (Client)p;
				if(c.isDead || c.heightLevel != npcs[i].heightLevel)
					continue;
				if(c.autoRet == 1)
					c.npcIndex = i;
				if(p.goodDistance(c.absX, c.absY, npcs[i].absX, npcs[i].absY, 15)){
					if(npcs[i].attackType == 2){ // Mage
						if(Misc.random(500) + 200 > Misc.random(c.getCombat().mageDef())){
							int dam = Misc.random(max);
							tempdam = dam;
							int dam2 = 0;
							if(npcs[i].npcType == 2000){
								if(npcs[i].nexStage == 2 && (c.prayerActive[17] || c.prayerActive[34])){
									dam *= 0.6;
									if(c.prayerActive[33])
										dam2 = (((Misc.random(99) + 1) >= 65) ? tempdam / 10 : 0);
								}
								if(npcs[i].nexStage != 2 && (c.prayerActive[16] || c.prayerActive[33])){
									dam *= 0.6;
									if(c.prayerActive[33])
										dam2 = (((Misc.random(99) + 1) >= 65) ? tempdam / 10 : 0);
								}
							}else{
								if(c.prayerActive[16] || c.prayerActive[33]){
									dam = 0;
									if(c.prayerActive[33])
										dam2 = (((Misc.random(99) + 1) >= 65) ? tempdam / 10 : 0);
								}
							}
							if(dam2 > 0){
								if(npcs[i].HP - dam2 < 0)
									dam2 = npcs[i].HP;
								npcs[i].HP -= dam2;
								npcs[i].hitDiff = dam2;
								npcs[i].updateRequired = true;
								npcs[i].hitUpdateRequired = true;
								c.totalDamageDealt += dam2;
							}
							if(c.playerEquipment[c.playerHands] == 22362 || c.playerEquipment[c.playerHands] == 22363 ||
									c.playerEquipment[c.playerHands] == 22364 || c.playerEquipment[c.playerHands] == 22365)
								dam = ((int)Math.floor(dam * 0.95));
							if(dam > c.playerLevel[c.playerHitpoints])
								dam = c.playerLevel[c.playerHitpoints];
							if(c.playerEquipment[c.playerShield] == 18361 && dam >= 20){
								c.sendMessage("Your shield soaks up 18% damage from the original " + dam);
								dam = (int)Math.round(dam * 0.82);
							}
							if(c.playerEquipment[c.playerShield] == 18363 && dam >= 20){
								c.sendMessage("Your shield soaks up 10% damage from the original " + dam);
								dam = (int)Math.round(dam * 0.90);
							}
							if(c.playerLevel[c.playerHitpoints] < dam)
								dam = c.playerLevel[c.playerHitpoints];
							c.dealDamage(dam);
						}else
							c.dealDamage(0);
						switch(npcs[i].nexSpell){
							case 1: // Smoke
								int random = Misc.random(99) > 69 ? 1 : 0;
								if(Misc.random(3) == 3 && c.poisonDamage <= 0)
									c.getPA().appendPoison(6);
								if(random == 0){
									if(c.playerLevel[c.playerPrayer] > 0)
										c.playerLevel[c.playerPrayer] -= 4 > c.playerLevel[c.playerPrayer] ? c.playerLevel[c.playerPrayer] : 4;
									c.getPA().refreshSkill(c.playerPrayer);
									if(Misc.random(99) >= 69)
										npcs[i].forceChat("Let the virus flow through you!");
								}else{
									if(c.playerEquipment[c.playerShield] != 19617)
										c.getCombat().resetPrayers();
									npcs[i].forceChat("There is... NO ESCAPE!");
									int x = Misc.random(99) > 49 ? 1 : -1;
									int y = Misc.random(99) > 49 ? 1 : -1;
									if(doTele == j)
										c.getPA().movePlayer(npcs[i].absX + x, npcs[i].absY + y, c.heightLevel);
								}
								break;
							case 2: // Shadow
								c.playerLevel[c.playerAttack] = (int)(c.getLevelForXP(c.playerXP[c.playerAttack]) * 0.85);
								c.getPA().refreshSkill(c.playerAttack);
								break;
							case 3: // Blood
								if(Misc.random(99) > 59)
									npcs[i].forceChat("I demand a blood sacrifice!");
								npcs[i].HP += tempdam * 0.25;
								npcs[i].hitUpdateRequired = true;
								npcs[i].updateRequired = true;
								break;
							case 4: // Ice
								if(c.freezeTimer <= -3){
									c.freezeTimer = 33;
									c.resetWalkingQueue();
									c.sendMessage("You have been frozen.");
									npcs[i].forceChat("Die now, in a prison of ice!");
								}
								break;
							case 5: // Miasmic
								if(!c.miasmicEffect){
									c.miasmicEffect = true;
									c.miasmicTime = Misc.currentTimeSeconds() + 48;
								}
								break;
							default:
								break;
						}
					}else if(npcs[i].attackType == 1){ // Range
						if(!c.prayerActive[17]){
							int dam = Misc.random(max);
							int dam2 = 0;
							if(c.prayerActive[17] || c.prayerActive[34]){
								dam2 = (((Misc.random(99) + 1) >= 65) ? dam / 10 : 0);
								dam = 0;
							}
							if(dam > 0){
								if(c.playerEquipment[c.playerHands] == 22358 || c.playerEquipment[c.playerHands] == 22359 ||
										c.playerEquipment[c.playerHands] == 22360 || c.playerEquipment[c.playerHands] == 22361)
									dam = ((int)Math.floor(dam * 0.95));
								if(dam > c.playerLevel[c.playerHitpoints])
									dam = c.playerLevel[c.playerHitpoints];
								if(c.playerEquipment[c.playerShield] == 18361 && dam >= 20){
									c.sendMessage("Your shield soaks up 12% damage from the original " + dam);
									dam = (int)Math.round(dam * 0.88);
								}
								if(Misc.random(500) + 200 > Misc.random(c.getCombat().calculateRangeDefence())){
									if(c.playerLevel[c.playerHitpoints] < dam)
										dam = c.playerLevel[c.playerHitpoints];
									c.dealDamage(dam);
								}else{
									c.dealDamage(0);
								}
							}else{
								c.dealDamage(0);
							}
							if(dam2 > 0){
								if(npcs[i].HP - dam2 < 0)
									dam2 = npcs[i].HP;
								npcs[i].HP -= dam2;
								npcs[i].hitDiff = dam2;
								npcs[i].updateRequired = true;
								npcs[i].hitUpdateRequired = true;
								c.totalDamageDealt += dam2;
							}
						}else{
							c.dealDamage(0);
						}
					}
					if(npcs[i].endGfx > 0){
						c.gfx0(npcs[i].endGfx);
					}
				}
				c.getPA().refreshSkill(3);
			}
		}
	}

	public int getClosePlayer(int i){
		for(Player p : RegionManager.getLocalPlayers(npcs[i].getLocation())){
			if(p != null){
				if(p.playerId == npcs[i].spawnedBy)
					return p.playerId;
				if(goodDistance(p.absX, p.absY, npcs[i].absX, npcs[i].absY, 2 + distanceRequired(i) + followDistance(i)) || isFightCaveNpc(i))
					if((p.underAttackBy <= 0 && p.underAttackBy2 <= 0) || p.inMulti())
						if(p.heightLevel == npcs[i].heightLevel)
							return p.playerId;
			}
		}
		return 0;
	}

	public int getCloseRandomPlayer(int i){
		ArrayList<Integer> players = new ArrayList<Integer>();
		Collection<Player> playersC = RegionManager.getLocalPlayers(npcs[i].getLocation());
		for(Player p : playersC){
			if(p != null){
				if(goodDistance(p.absX, p.absY, npcs[i].absX, npcs[i].absY, 2 + distanceRequired(i) + followDistance(i)) || isFightCaveNpc(i)){
					if((p.underAttackBy <= 0 && p.underAttackBy2 <= 0) || p.inMulti())
						if(p.heightLevel == npcs[i].heightLevel)
							players.add(p.playerId);
				}
			}
		}
		if(players.size() > 0)
			return players.get(Misc.random(players.size() - 1));
		else
			return 0;
	}

	public int npcSize(int i){
		switch(npcs[i].npcType){
			case 2883:
			case 2882:
			case 2881:
				return 3;
		}
		return 0;
	}

	public boolean isAggressive(int i){
		switch(npcs[i].npcType){
			case 50:
			case 2000:
			case 6260:
			case 1158:
			case 1159:
			case 1160:
			case 6261:
			case 6263:
			case 6265:
			case 6222:
			case 6223:
			case 6225:
			case 6227:
			case 6247:
			case 6248:
			case 6250:
			case 6252:
			case 2892:
			case 2894:
			case 2881:
			case 2882:
			case 8133:
			case 2883:
			case 3006:
			case 6203:
			case 6204:
			case 6206:
			case 6208:
			case 8349:
			case 879:
				return true;
		}
		if(npcs[i].inWild() && npcs[i].MaxHP > 0)
			return true;
		if(isFightCaveNpc(i) || /*Server.pestControl.isPestControlAggressive(npcs[i].npcType) ||*/ (npcs[i].isZombieNPC && npcs[i].inCanifis()))
			return true;
		return false;
	}

	public boolean isFightCaveNpc(int i){
		switch(npcs[i].npcType){
			case 2627:
			case 2630:
			case 2631:
			case 2741:
			case 2743:
			case 2745:
				return true;
		}
		return false;
	}
	
	public NPC replaceNpc(Client c, int npcType, int x, int y, int heightLevel, int WalkingType, int HP, int maxHit, int attack, int defence, boolean attackPlayer, boolean headIcon, int slot){
		NPC newNPC = new NPC(slot, npcType);
		newNPC.setLocation(Location.create(x, y, heightLevel));
		newNPC.absX = x;
		newNPC.absY = y;
		newNPC.makeX = x;
		newNPC.makeY = y;
		newNPC.heightLevel = heightLevel;
		newNPC.walkingType = WalkingType;
		newNPC.HP = HP;
		newNPC.MaxHP = HP;
		newNPC.maxHit = maxHit;
		newNPC.attack = attack;
		newNPC.defence = newNPC.oldDefence = defence;
		newNPC.spawnedBy = c.playerId;
		if(headIcon)
			c.getPA().drawHeadicon(1, slot, 0, 0);
		if(attackPlayer){
			newNPC.underAttack = true;
			if(c != null){
				if(server.model.minigames.Barrows.COFFIN_AND_BROTHERS[c.randomCoffin][1] != newNPC.npcType){
					if(newNPC.npcType == 2025 || newNPC.npcType == 2026 || newNPC.npcType == 2027 || newNPC.npcType == 2028 || newNPC.npcType == 2029 || newNPC.npcType == 2030){
						newNPC.forceChat("You dare disturb my rest!");
					}
				}
				if(server.model.minigames.Barrows.COFFIN_AND_BROTHERS[c.randomCoffin][1] == newNPC.npcType){
					newNPC.forceChat("You dare steal from us!");
				}
				if(newNPC.npcType == 2001){
					newNPC.forceChat("Fool! How dare you invade my island!");
				}
				newNPC.killerId = c.playerId;
			}
		}
		return newNPC;
	}

	/**
	 * Summon npc, barrows, etc
	 **/
	public int spawnNpc(Client c, int npcType, int x, int y, int heightLevel, int WalkingType, int HP, int maxHit, int attack, int defence, boolean attackPlayer, boolean headIcon){
		// first, search for a free slot
		int slot = -1;
		for(int i = 1; i < maxNPCs; i++){
			if(npcs[i] == null){
				slot = i;
				break;
			}
		}
		if(slot == -1){
			// Misc.println("No Free Slot");
			return -1; // no free slot found
		}
		NPC newNPC = new NPC(slot, npcType);
		newNPC.setLocation(Location.create(x, y, heightLevel));
		newNPC.absX = x;
		newNPC.absY = y;
		newNPC.makeX = x;
		newNPC.makeY = y;
		newNPC.heightLevel = heightLevel;
		newNPC.walkingType = WalkingType;
		newNPC.HP = HP;
		newNPC.MaxHP = HP;
		newNPC.maxHit = maxHit;
		newNPC.attack = attack;
		newNPC.defence = newNPC.oldDefence = defence;
		newNPC.spawnedBy = c.playerId;
		if(headIcon)
			c.getPA().drawHeadicon(1, slot, 0, 0);
		if(attackPlayer){
			newNPC.underAttack = true;
			if(c != null){
				if(server.model.minigames.Barrows.COFFIN_AND_BROTHERS[c.randomCoffin][1] != newNPC.npcType){
					if(newNPC.npcType == 2025 || newNPC.npcType == 2026 || newNPC.npcType == 2027 || newNPC.npcType == 2028 || newNPC.npcType == 2029 || newNPC.npcType == 2030){
						newNPC.forceChat("You dare disturb my rest!");
					}
				}
				if(server.model.minigames.Barrows.COFFIN_AND_BROTHERS[c.randomCoffin][1] == newNPC.npcType){
					newNPC.forceChat("You dare steal from us!");
				}
				if(newNPC.npcType == 2001){
					newNPC.forceChat("Fool! How dare you invade my island!");
				}
				newNPC.killerId = c.playerId;
			}
		}
		npcs[slot] = newNPC;
		return slot;
	}

	public int spawnNpc2(int npcType, int x, int y, int heightLevel, int WalkingType, int HP, int maxHit, int attack, int defence){
		// first, search for a free slot
		int slot = -1;
		for(int i = 1; i < maxNPCs; i++){
			if(npcs[i] == null){
				slot = i;
				break;
			}
		}
		if(slot == -1){
			// Misc.println("No Free Slot");
			return -1; // no free slot found
		}
		NPC newNPC = new NPC(slot, npcType);
		newNPC.setLocation(Location.create(x, y, heightLevel));
		newNPC.absX = x;
		newNPC.absY = y;
		newNPC.makeX = x;
		newNPC.makeY = y;
		newNPC.heightLevel = heightLevel;
		newNPC.walkingType = WalkingType;
		newNPC.HP = HP;
		newNPC.MaxHP = HP;
		newNPC.maxHit = maxHit;
		newNPC.attack = attack;
		newNPC.defence = newNPC.oldDefence = defence;
		npcs[slot] = newNPC;
		return slot;
	}

	public int spawnNpc3(int npcType, int x, int y, int heightLevel, int WalkingType, int HP, int maxHit, int attack, int defence){
		// first, search for a free slot
		int slot = -1;
		for(int i = 1; i < maxNPCs; i++){
			if(npcs[i] == null){
				slot = i;
				break;
			}
		}
		if(slot == -1){
			// Misc.println("No Free Slot");
			return -1; // no free slot found
		}
		NPC newNPC = new NPC(slot, npcType);
		newNPC.setLocation(Location.create(x, y, heightLevel));
		newNPC.absX = x;
		newNPC.absY = y;
		newNPC.makeX = x;
		newNPC.makeY = y;
		newNPC.heightLevel = heightLevel;
		newNPC.walkingType = WalkingType;
		newNPC.HP = HP;
		newNPC.MaxHP = HP;
		newNPC.maxHit = maxHit;
		newNPC.attack = attack;
		newNPC.defence = newNPC.oldDefence = defence;
		npcs[slot] = newNPC;
		return slot;
	}

	public static int getNPCDeleteTime(int i){
		switch(npcs[i].npcType){
			case 6142:
			case 6143:
			case 6144:
			case 6145:
				return 3;
			case 880:
				return 15;
			default:
				return 4;
		}
	}

	/**
	 * Emotes
	 **/
	public static int getBlockEmote(int i){
		switch(NPCHandler.npcs[i].npcType){
			case 2036:
			case 2037:
				return 5489;
			case 2627:
				return 9231;
			case 2630:
				return 9235;
			case 2631:
				return 9242;
			case 2741:
				return 9253;
			case 2743:
				return 9268;
			case 2745:
				return 9278;
			case 8133:
				return 10386;
			case 2000:
				return 6948;
			case 13481:
				return 14264;
			case 13480:
				return 7443;
			case 13478:
				return 7413;
			case 13477:
				return 7476;
			case 13476:
				return 7469;
			case 13474:
				return 7399;
			case 2685:
			case 2686:
			case 2687:
				return 2961;
			case 6210:
				return 6574;
			case 1158:
				return 6232;
			case 1160:
				return 6237;
			case 96:
				return 6563;
			case 879:
				return 65;
				// Zombies
			case 103:
				return 5541;
			case 104:
				return 5533;
			case 73:
			case 74:
			case 75:
			case 76:
			case 77:
			case 422:
			case 423:
			case 424:
				return 5567;
			case 1612:
				return 9451; // banshee
			case 1618:
			case 1619:
				return 9132; // bloodveld
			case 1624:
				return 1555; // dust devils
			case 1610:
				return 9455; // gargoyles
			case 1613:
				return 9489; // nechryael
			case 2783:
				return 2732; // dark beast
			case 1648: // hands
			case 1649:
			case 1650:
			case 1651:
			case 1652:
			case 1654:
			case 1655:
			case 1656:
			case 1657:
				return 9127;
				/* Begin Pest Control NPCs */
				// Splatters
			case 3728:
			case 3729:
			case 3730:
			case 3731:
				return 3890;
				// Shifters
			case 3734:
			case 3736:
			case 3738:
			case 3740:
				return 3902;
				// Ravagers
			case 3743:
			case 3744:
			case 3745:
			case 3746:
				return 3916;
				// Spinners
			case 3748:
			case 3749:
			case 3750:
			case 3751:
				return 3909;
				// Torchers
			case 3757:
			case 3759:
			case 3760:
			case 3761:
				return 3880;
				// Defilers
			case 3768:
			case 3769:
			case 3770:
				return 3921;
				// Brawlers
			case 3773:
			case 3775:
			case 3776:
				return 3895;
				/* End Pest Control NPCs */
			case 50:
			case 51:
			case 52:
			case 53:
			case 54:
			case 55:
			case 941:
			case 1589:
			case 1590:
			case 1591:
			case 1592:
				return 89;
			case 2001:
				return 12693;
			case 82:
			case 83:
			case 84:
				return 65;
			case 110:
			case 111:
			case 112:
				return 4651;
			case 1338:
			case 1339:
			case 1340:
			case 1341:
			case 1342:
			case 1343:
			case 1344:
			case 1345:
			case 1346:
			case 1347:
				return 1340;
			case 2881:
			case 2882:
			case 2883:
				return 2852;
				/* Begin Godwars */
				// Zammy
			case 6203:
				return 6944;
			case 6204:
			case 6206:
			case 6208:
				return 6944;
				// Arma
			case 6222:
				return 6974;
			case 6223:
			case 6225:
			case 6227:
				return 6955;
				// Sara
			case 6247:
				return 6966;
			case 6248:
				return 6375;
			case 6250:
				return 7017;
			case 6252:
				return 7010;
				// Bandos
			case 6260:
				return 7061;
			case 6261:
			case 6263:
			case 6265:
				return 6155;
				/* End Godwars */
			default:
				return -1;
		}
	}

	public static int getAttackEmote(int i){
		switch(NPCHandler.npcs[i].npcType){
			case 8133:
				if(npcs[i].attackType == 0)
					return 10057;
				else if(npcs[i].attackType == 1)
					return 10065;
				else
					return 10053;
			case 8349:
				if(npcs[i].attackType == 0){
					npcs[i].endGfx = 1886;
					return 10922;
				}else if(npcs[i].attackType == 1)
					return 10918;
				else
					return 10917;
			case 6260:
				return npcs[i].attackType == 0 ? 7060 : 7063;
			case 2001: // Nomad
				return npcs[i].attackType == 0 ? 12696 : (npcs[i].invulnerableTick > -1 ? 12698 : 12697);
			case 2000:
				if(npcs[i].attackType == 0){ // Melee
					int temp = Misc.random(1);
					return temp == 1 ? 6354 : 6355;
				}else
					return 6326; // Mage
			case 1158:
				return 6223;
			case 879:
				return 64;
			case 1160:
				return 6235;
			case 2685:
			case 2686:
			case 2687:
				return 2959;
			case 13481:
				return 14261;
			case 13480:
				return 7441;
			case 13478:
				return 7411;
			case 13477:
				return 7474;
			case 13476:
				return 7467;
			case 13474:
				return 7397;
			case 6210:
				return 6581;
			case 2892:
			case 2894:
				return 2868;
			case 2627:
				return 9232;
			case 2630:
				return 9233;
			case 2631:
				return 9243;
			case 2741:
				return 9252;
			case 2746:
				return 2637;
				
			case 2607:
				return 2611;
			case 96:
				return 6559;
			case 2743:// 360
				return 9266;
			case 912:
			case 913:
				return 811;
			case 914:
				return 197;
				// Zombies
			case 73:
			case 74:
			case 75:
			case 76:
			case 77:
			case 422:
			case 423:
			case 424:
				return Misc.random(1) == 1 ? 5568 : 5571;
			case 1612:
				return 9449; // banshee
			case 1618:
			case 1619:
				return 9130; // bloodveld
				// bandos gwd
			case 6261:
			case 6263:
			case 6265:
				return 6154;
				// end of gwd
				// arma gwd
			case 6222:
				return 6977;
			case 6225:
				return 6953;
			case 6223:
				return 6952;
			case 6227:
				return 6954;
				// end of arma gwd
				// zammy gwd
			case 6203:
				if(npcs[i].attackType == 0)
					return 6945;
				else
					return 6947;
			case 6204:
			case 6206:
			case 6208:
				return 6945;
				// end zammy gwd
				// sara gwd
			case 6247:
				return 6964;
			case 6248:
				return 6376;
			case 6250:
				return 7018;
			case 6252:
				return 7009;
				// end of sara gwd
			case 13: // wizards
				return 711;
				/* Begin Pest Control NPCs */
				// Splatters
			case 3728:
			case 3729:
			case 3730:
			case 3731:
				return 3891;
				// Shifters
			case 3734:
			case 3736:
			case 3738:
			case 3740:
				return 3901;
				// Ravagers
			case 3743:
			case 3744:
			case 3745:
			case 3746:
				return 3915;
				// Spinners
			case 3748:
			case 3749:
			case 3750:
			case 3751:
				return 3908;
				// Torchers
			case 3757:
			case 3759:
			case 3760:
			case 3761:
				return 3880;
				// Defilers
			case 3768:
			case 3769:
			case 3770:
				return 3923;
				// Brawlers
			case 3773:
			case 3775:
			case 3776:
				return Misc.random(1) == 1 ? 3896 : 3897;
				/* End Pest Control NPCs */
			case 103:
				return 5540;
			case 104:
				return 5532;
			case 1624:
				return 1557;

			case 1648:
			case 1649:
			case 1650:
			case 1651:
			case 1652:
			case 1654:
			case 1655:
			case 1656:
			case 1657:
				return 9125;

			case 2783: // dark beast
				return 2731;

			case 1615: // abby demon
				return 1537;

			case 1613: // nech
				return 9491;

			case 1610: // garg
				return 9454;

			case 1616: // basilisk
				return 1546;
			case 2036:
			case 2037: // skele
				return 5485;

			case 50:// drags
			case 53:
			case 54:
			case 55:
			case 941:
			case 1590:
			case 1591:
			case 1592:
				return 80;

			case 124: // earth warrior
				return 390;

			case 803: // monk
				return 422;

			case 52: // baby drag
				return 25;

			case 58: // Shadow Spider
			case 59: // Giant Spider
			case 60: // Giant Spider
			case 61: // Spider
			case 62: // Jungle Spider
			case 63: // Deadly Red Spider
			case 64: // Ice Spider
			case 134:
				return 143;

			case 105: // Bear
			case 106: // Bear
				return 41;

			case 412:
			case 78:
				return 30;

			case 2033: // rat
				return 138;

			case 2031: // bloodworm
				return 2070;

			case 101: // goblin
				return 309;

			case 81: // cow
				return 0x03B;

			case 21: // hero
				return 451;

			case 41: // chicken
				return 55;

			case 9: // guard
			case 32: // guard
			case 20: // paladin
				return 451;

			case 1338: // dagannoth
			case 1340:
			case 1342:
				return 1341;

			case 19: // white knight
				return 406;

			case 110:
			case 111: // ice giant
			case 112:
			case 117:
				return 4652;

			case 2452:
				return 1312;

			case 2889:
				return 2859;

			case 118:
			case 119:
				return 99;

			case 82:// Lesser Demon
			case 83:// Greater Demon
			case 84:// Black Demon
			case 1472:// jungle demon
				return 64;

			case 1267:
			case 1265:
				return 1312;

			case 125: // ice warrior
			case 178:
				return 451;

			case 1153: // Kalphite Worker
			case 1154: // Kalphite Soldier
			case 1155: // Kalphite guardian
			case 1156: // Kalphite worker
			case 1157: // Kalphite guardian
				return 1184;

			case 123:
			case 122:
				return 164;

			case 2028: // karil
				return 2075;

			case 2025: // ahrim
				return 729;

			case 2026: // dharok
				return 2067;

			case 2027: // guthan
				return 2080;

			case 2029: // torag
				return 0x814;

			case 2030: // verac
				return 2062;

			case 2881: // supreme
				return 2855;

			case 2882: // prime
				return 2854;

			case 2883: // rex
				return 2851;

			case 3200:
				return 3146;

			case 2745:
				if(npcs[i].attackType == 2)
					return 9300;
				else if(npcs[i].attackType == 1)
					return 9276;
				else if(npcs[i].attackType == 0)
					return 9277;

			default:
				return 0x326;
		}
	}

	public int getDeadEmote(int i){
		switch(npcs[i].npcType){
			case 2000: // nex
				return 6951;
				// sara gwd
			case 6247:
				return 6965;
			case 6248:
				return 6377;
			case 6250:
				return 7016;
			case 6252:
				return 7011;
			case 880: // Weak Delrith
				return 4624;
				/* Begin Pest Control */
				// Portals
			case 6142:
				return 3935;
			case 6143:
				return 3938;
			case 6144:
			case 6145:
				return 3930;
				// Splatters
			case 3728:
			case 3729:
			case 3730:
			case 3731:
				return 3888;
				// Shifters
			case 3734:
			case 3736:
			case 3738:
			case 3740:
				return 3903;
				// Ravagers
			case 3743:
			case 3744:
			case 3745:
			case 3746:
				return 3917;
				// Spinners
			case 3748:
			case 3749:
			case 3750:
			case 3751:
				return 3910;
				// Torchers
			case 3757:
			case 3759:
			case 3760:
			case 3761:
				return 3881;
				// Defilers
			case 3768:
			case 3769:
			case 3770:
				return 3922;
				// Brawlers
			case 3773:
			case 3775:
			case 3776:
				return 3894;
				/* End Pest Control */
			case 914:
				return 196;
			case 110:
			case 111:
			case 112:
			case 117:
				return 4653;
			case 13481:
				return 14265;
			case 13480:
				return 7442;
			case 13478:
				return 7412;
			case 13477:
				return 7475;
			case 13476:
				return 7468;
			case 13474:
				return 7398;
				// Zombies
			case 73:
			case 74:
			case 75:
			case 76:
			case 77:
			case 422:
			case 423:
			case 424:
				return 5569;
				// zammy gwd
			case 6203:
			case 6204:
			case 6206:
				return 6946;
			case 6208:
				return 6947;
			case 8349:
				return 10924;
				// bandos gwd
			case 6261:
			case 6263:
			case 6265:
				return 6156;
			case 6260:
				return 7062;
			case 2892:
			case 2894:
				return 2865;
			case 1612: // banshee
				return 9450;
			case 1618:
			case 1619:
				return 9131; // bloodveld
			case 2001: // Nomad
				return 12694;
			case 1158:
				return 6230;
			case 1160:
				return 6234;
			case 6222:
				return 6975;
			case 6223:
			case 6225:
			case 6227:
				return 6956;
			case 2607:
				return 2607;
			case 2627:
				return 9230;
			case 2630:
				return 9234;
			case 2631:
				return 9239;
			case 2738:
				return 2627;
			case 2741:
				return 9257;
			case 2746:
				return 2638;
			case 2743:
				return 9269;
			case 2745:
				return 9279;
			case 8133:
				return 10059;

			case 3200:
				return 3147;

			case 2035: // spider
				return 146;

			case 2033: // rat
				return 141;

			case 2031: // bloodvel
				return 2073;

			case 101: // goblin
				return 313;

			case 81: // cow
				return 0x03E;

			case 41: // chicken
				return 57;

			case 1338: // dagannoth
			case 1340:
			case 1342:
				return 1342;

			case 2881:
			case 2882:
			case 2883:
				return 2856;

			case 125: // ice warrior
				return 843;

			case 751:// Zombies!!
				return 302;

			case 1626:
			case 1627:
			case 1628:
			case 1629:
			case 1630:
			case 1631:
			case 1632: // turoth!
				return 1597;

			case 1616: // basilisk
				return 1548;

			case 1653: // hand
				return 1590;

			case 82:// demons
			case 83:
			case 84:
				return 67;

			case 1605:// abby spec
				return 1508;

			case 51:// baby drags
			case 52:
			case 1589:
			case 3376:
				return 28;

			case 1610:
				return 9460;

			case 1620:
			case 1621:
				return 1563;

			case 2783:
				return 2733;

			case 1615:
				return 1538;

			case 1624:
				return 1553;

			case 1613:
				return 9488;

			case 1633:
			case 1634:
			case 1635:
			case 1636:
				return 1580;

			case 1648:
			case 1649:
			case 1650:
			case 1651:
			case 1652:
			case 1654:
			case 1655:
			case 1656:
			case 1657:
				return 9126;

			case 100:
			case 102:
				return 313;

			case 105:
			case 106:
				return 44;
			case 6210:
				return 6576;
			case 412:
			case 78:
				return 36;
			case 2685:
			case 2686:
			case 2687:
				return 2304;
			case 122:
			case 123:
				return 167;
			case 96:
				return 6558;
			case 58:
			case 59:
			case 60:
			case 61:
			case 62:
			case 63:
			case 64:
			case 134:
				return 146;

			case 1153:
			case 1154:
			case 1155:
			case 1156:
			case 1157:
				return 1190;

			case 103:
				return 5542;
			case 104:
				return 5534;
			case 118:
			case 119:
				return 102;
			case 2036:
			case 2037:
				return 5491;
			case 50:// drags
			case 53:
			case 54:
			case 55:
			case 941:
			case 1590:
			case 1591:
			case 1592:
				return 92;
			default:
				return 2304;
		}
	}

	/**
	 * Attack delays
	 **/
	public int getNpcDelay(int i){
		switch(npcs[i].npcType){
			case 2025:
			case 2028:
			case 8133:
				return 7;
			case 8349:
				if(npcs[i].attackType == 2)
					return 7;
				return 5;
			case 2745:
				return 8;
			case 6222:
			case 6223:
			case 6225:
			case 6227:
			case 6260:
			case 6203:
				return 6;
				// saradomin gw boss
			case 2001:
				return npcs[i].attackType == 0 ? 5 : (npcs[i].invulnerableTick > -1 ? NPC.INVULNERABLE_TICK + 2 : 5);
			case 6247:
				return 2;
			default:
				return 5;
		}
	}

	/**
	 * Hit delays
	 **/
	public int getHitDelay(int i){
		switch(npcs[i].npcType){
			case 2881:
			case 2882:
			case 3200:
			case 2892:
			case 2894:
				return 3;
			case 8349:
				if(npcs[i].attackType == 2)
					return 5;
				return 2;
			case 2743:
			case 2631:
			case 6222:
			case 6223:
			case 6225:
				return 3;
			case 2001:
				return npcs[i].attackType == 0 ? 2 : (npcs[i].invulnerableTick > -1 ? NPC.INVULNERABLE_TICK : 3);
			case 2745:
				if(npcs[i].attackType == 1 || npcs[i].attackType == 2)
					return 5;
				else
					return 2;
			case 2025:
				return 4;
			case 2028:
				return 3;
			default:
				return 2;
		}
	}

	/**
	 * Npc respawn time
	 **/
	public int getRespawnTime(int i){
		switch(npcs[i].npcType){
			case 2881:
			case 6252:
			case 2882:
			case 2883:
			case 6222:
			case 6223:
			case 6225:
			case 6227:
			case 6247:
			case 6248:
			case 6250:
			case 3006:
			case 6260:
			case 6261:
			case 6263:
			case 6265:
			case 6203:
			case 6204:
			case 6206:
			case 6208:
			case 8349:
			case 8133:
				return 100;
			default:
				return 25;
		}
	}

	public void newNPC(int npcType, int x, int y, int heightLevel, int WalkingType, int HP, int maxHit, int attack, int defence, int combat){
		// first, search for a free slot
		int slot = -1;
		for(int i = 1; i < maxNPCs; i++){
			if(npcs[i] == null){
				slot = i;
				break;
			}
		}

		if(slot == -1)
			return; // no free slot found

		NPC newNPC = new NPC(slot, npcType);
		newNPC.setLocation(Location.create(x, y, heightLevel));
		newNPC.absX = x;
		newNPC.absY = y;
		newNPC.makeX = x;
		newNPC.makeY = y;
		newNPC.heightLevel = heightLevel;
		newNPC.walkingType = WalkingType;
		newNPC.HP = HP;
		newNPC.MaxHP = HP;
		newNPC.maxHit = maxHit;
		newNPC.attack = attack;
		newNPC.defence = newNPC.oldDefence = defence;
		newNPC.combat = combat;
		newNPC.nexSpawn = npcType == 2000 ? heightLevel / 4 : 0;
		newNPC.nexSpawnTick = 10;
		newNPC.nexStage = npcType == 2000 ? 1 : 0;
		if(Spot.isSpot(npcType)){
			newNPC.npcObject = true;
			Spot.createSpot(npcType, x, y);
		}
		npcs[slot] = newNPC;
	}

	public void newNPCList(int npcType, String npcName, int combat, int HP){
		// first, search for a free slot
		int slot = -1;
		for(int i = 0; i < maxListedNPCs; i++){
			if(NpcList[i] == null){
				slot = i;
				break;
			}
		}

		if(slot == -1)
			return; // no free slot found

		NPCList newNPCList = new NPCList(npcType);
		newNPCList.npcName = npcName;
		newNPCList.npcCombat = combat;
		newNPCList.npcHealth = HP;
		NpcList[slot] = newNPCList;
	}

	public void process(){
		for(int i = 0; i < maxNPCs; i++){
			if(npcs[i] == null)
				continue;
			npcs[i].clearUpdateFlags();
		}
		for(int i = 0; i < maxNPCs; i++){
			if(npcs[i] != null){
				if(npcs[i].npcType == 1532){
					npcs[i].freezeTimer = npcs[i].attackTimer = 100;
				}
				if(npcs[i].actionTimer > 0){
					npcs[i].actionTimer--;
				}
				if(npcs[i].npcType == 2244 || npcs[i].npcType == 8540){
					if(guideChatTimer == 0){
						guideChatTimer = 15;
						npcs[i].updateRequired = true;
						if(npcs[i].npcType == 2244)
							npcs[i].forceChat("Join the cc help if you need help!");
						else
							npcs[i].forceChat("Merry Christmas!");
					}else
						guideChatTimer--;
				}
				if(queenOneTimer > 0 && QueenTwo)
					queenOneTimer--;
				if(npcs[i].npcType == 2001 && npcs[i].invulnerableTick > -1){
					npcs[i].invulnerableTick--;
				}
				if(npcs[i].npcType != 1532){
					if(npcs[i].walkingType < 0){
						switch(npcs[i].walkingType){
							case -1: // East
								npcs[i].turnNpc(npcs[i].absX + 1, npcs[i].absY);
								break;
							case -2: // West
								npcs[i].turnNpc(npcs[i].absX - 1, npcs[i].absY);
								break;
							case -3: // North
								npcs[i].turnNpc(npcs[i].absX, npcs[i].absY + 1);
								break;
							case -4: // South
								npcs[i].turnNpc(npcs[i].absX, npcs[i].absY - 1);
								break;
							default: // Nothing to do.
								break;
						}
					}
				}

				if(npcs[i].freezeTimer > 0){
					npcs[i].freezeTimer--;
				}

				if(npcs[i].hitDelayTimer > 0){
					npcs[i].hitDelayTimer--;
				}

				if(npcs[i].hitDelayTimer == 1){
					npcs[i].hitDelayTimer = 0;
					applyDamage(i);
				}

				if(npcs[i].attackTimer > 0){
					npcs[i].attackTimer--;
				}

				if(npcs[i].spawnedBy > 0){ // delete summons npc
					if(PlayerHandler.players[npcs[i].spawnedBy] == null || PlayerHandler.players[npcs[i].spawnedBy].heightLevel != npcs[i].heightLevel || PlayerHandler.players[npcs[i].spawnedBy].respawnTimer > 0 || (!PlayerHandler.players[npcs[i].spawnedBy].goodDistance(npcs[i].getX(), npcs[i].getY(), PlayerHandler.players[npcs[i].spawnedBy].getX(), PlayerHandler.players[npcs[i].spawnedBy].getY(), 20) && !PlayerHandler.players[npcs[i].spawnedBy].inCaveGame)){

						if(PlayerHandler.players[npcs[i].spawnedBy] != null){
							for(int o = 0; o < PlayerHandler.players[npcs[i].spawnedBy].barrowsNpcs.length; o++){
								if(npcs[i].npcType == PlayerHandler.players[npcs[i].spawnedBy].barrowsNpcs[o][0]){
									if(PlayerHandler.players[npcs[i].spawnedBy].barrowsNpcs[o][1] == 1)
										PlayerHandler.players[npcs[i].spawnedBy].barrowsNpcs[o][1] = 0;
								}
							}
						}
						npcs[i].getRegion().removeNpc(npcs[i]);
						npcs[i] = null;
					}
				}
				if(npcs[i] == null)
					continue;

				/*if(Server.pestControl.isPestControl(npcs[i].npcType)){
					synchronized(Server.pestControl){
						Server.pestControl.handlePestMonster(i);
					}
				}*/
				if(npcs[i].isCapeNPC){
					if(npcs[i].clientSpawner == null){
						removeNPC(i);
						continue;
					}else if(npcs[i].clientSpawner.maxCape <= 1){
						removeNPC(i);
						continue;
					}
				}
				/**
				 * Attacking player
				 **/
				if(isAggressive(i) && !npcs[i].underAttack && !npcs[i].isDead && !switchesAttackers(i)){
					npcs[i].killerId = getCloseRandomPlayer(i);
				}else if(isAggressive(i) && !npcs[i].underAttack && !npcs[i].isDead && switchesAttackers(i)){
					npcs[i].killerId = getCloseRandomPlayer(i);
				}
				if(npcs[i].killerId == 0 && npcs[i].npcType == 2000 && npcs[i].nexSpawnTick <= 0){
					Config.NEX_SPAWNED[npcs[i].nexSpawn] = false;
					removeNPC(i);
					continue;
				}else if(npcs[i].npcType == 2000)
					npcs[i].nexSpawnTick--;

				if(System.currentTimeMillis() - npcs[i].lastDamageTaken > 5000)
					npcs[i].underAttackBy = 0;

				if((npcs[i].killerId > 0 || npcs[i].underAttack) && !npcs[i].walkingHome && retaliates(npcs[i].npcType)){
					if(!npcs[i].isDead){
						int p = npcs[i].killerId;
						if(PlayerHandler.players[p] != null){
							if(!npcs[i].summoned && !npcs[i].isWeakDelrith){
								Client c = (Client)PlayerHandler.players[p];
								followPlayer(i, c.playerId);
								if(npcs[i] == null)
									continue;
								if(npcs[i].attackTimer == 0){
									if(!c.isDead){
										attackPlayer(c, i);
									}else{
										npcs[i].killerId = 0;
										npcs[i].underAttack = false;
										npcs[i].facePlayer(0);
									}
								}
							}else{
								Client c = (Client)PlayerHandler.players[p];
								followPlayer(i, c.playerId);
							}
						}else{
							npcs[i].killerId = 0;
							npcs[i].underAttack = false;
							npcs[i].facePlayer(0);
						}
					}
				}

				if(Misc.currentTimeSeconds() - npcs[i].mJavTime >= 11  && npcs[i].mJavHit > 0){
					npcs[i].mJavTime = npcs[i].mJavHit > 5 ? Misc.currentTimeSeconds() : 0;
					int damage = 0;
					if(npcs[i].mJavHit >= 5){
						npcs[i].mJavHit -= 5;
						damage = 5 > npcs[i].HP ? npcs[i].HP : 5;
					}else{
						damage = npcs[i].mJavHit > npcs[i].HP ? npcs[i].HP : npcs[i].mJavHit;
						npcs[i].mJavHit = 0;
					}
					npcs[i].underAttack = true;
					npcs[i].hitDiff = damage;
					npcs[i].HP -= damage;
					npcs[i].hitUpdateRequired = true;
				}
				
				/**
				 * Random walking and walking home
				 **/
				if(npcs[i] == null)
					continue;
				if(npcs[i].freezeTimer == 0){
					if((!npcs[i].underAttack || npcs[i].walkingHome) && npcs[i].randomWalk && !npcs[i].isDead){
						npcs[i].facePlayer(0);
						npcs[i].killerId = 0;
						if(npcs[i].spawnedBy == 0){
							if((npcs[i].absX > npcs[i].makeX + Config.NPC_RANDOM_WALK_DISTANCE) || (npcs[i].absX < npcs[i].makeX - Config.NPC_RANDOM_WALK_DISTANCE) || (npcs[i].absY > npcs[i].makeY + Config.NPC_RANDOM_WALK_DISTANCE) || (npcs[i].absY < npcs[i].makeY - Config.NPC_RANDOM_WALK_DISTANCE)){
								npcs[i].walkingHome = true;
							}
						}

						if(npcs[i].walkingHome && npcs[i].absX == npcs[i].makeX && npcs[i].absY == npcs[i].makeY){
							npcs[i].walkingHome = false;
						}else if(npcs[i].walkingHome){
							if(!npcs[i].moveXQueue.isEmpty()){
								if(npcs[i].moveXQueue.size() != npcs[i].moveYQueue.size()){
									npcs[i].moveXQueue.clear();
									npcs[i].moveYQueue.clear();
								}else{
									npcs[i].moveX = npcs[i].moveXQueue.poll();
									npcs[i].moveY = npcs[i].moveYQueue.poll();
									handleClipping(i);
									npcs[i].getNextNPCMovement(i);
									npcs[i].setLocation(npcs[i].getLocation().transform(npcs[i].moveX, npcs[i].moveY));
									npcs[i].updateRequired = true;
								}
							}else{
								npcs[i].moveX = GetMove(npcs[i].absX, npcs[i].makeX);
								npcs[i].moveY = GetMove(npcs[i].absY, npcs[i].makeY);
								handleClipping(i);
								npcs[i].getNextNPCMovement(i);
								npcs[i].setLocation(npcs[i].getLocation().transform(npcs[i].moveX, npcs[i].moveY));
								npcs[i].updateRequired = true;
							}
						}
						if(npcs[i].walkingType == 1){
							if(Misc.random(3) == 1 && !npcs[i].walkingHome){
								int MoveX = 0;
								int MoveY = 0;
								int Rnd = Misc.random(9);
								if(Rnd == 1){
									MoveX = 1;
									MoveY = 1;
								}else if(Rnd == 2){
									MoveX = -1;
								}else if(Rnd == 3){
									MoveY = -1;
								}else if(Rnd == 4){
									MoveX = 1;
								}else if(Rnd == 5){
									MoveY = 1;
								}else if(Rnd == 6){
									MoveX = -1;
									MoveY = -1;
								}else if(Rnd == 7){
									MoveX = -1;
									MoveY = 1;
								}else if(Rnd == 8){
									MoveX = 1;
									MoveY = -1;
								}

								if(MoveX == 1){
									if(npcs[i].absX + MoveX < npcs[i].makeX + 1){
										npcs[i].moveX = MoveX;
									}else{
										npcs[i].moveX = 0;
									}
								}

								if(MoveX == -1){
									if(npcs[i].absX - MoveX > npcs[i].makeX - 1){
										npcs[i].moveX = MoveX;
									}else{
										npcs[i].moveX = 0;
									}
								}

								if(MoveY == 1){
									if(npcs[i].absY + MoveY < npcs[i].makeY + 1){
										npcs[i].moveY = MoveY;
									}else{
										npcs[i].moveY = 0;
									}
								}

								if(MoveY == -1){
									if(npcs[i].absY - MoveY > npcs[i].makeY - 1){
										npcs[i].moveY = MoveY;
									}else{
										npcs[i].moveY = 0;
									}
								}
								handleClipping(i);
								npcs[i].getNextNPCMovement(i);
								npcs[i].setLocation(npcs[i].getLocation().transform(npcs[i].moveX, npcs[i].moveY));
								npcs[i].updateRequired = true;
							}
						}
					}
				}

				if(npcs[i].isDead){
					if(npcs[i].actionTimer == 0 && !npcs[i].applyDead && !npcs[i].needRespawn){
						if(npcs[i].isDelrith){
							final int killerId = npcs[i].killerId, x = npcs[i].absX, y = npcs[i].absY, z = npcs[i].heightLevel;
							removeNPC(i);
							npcs[i] = replaceNpc((Client)PlayerHandler.players[killerId], DemonSlayer.DELRITH_ID[1], x, y, z, 0, 75, 0, 0, 35, false, false, i);
							npcs[i].killerId = killerId;
							npcs[i].isWeakDelrith = true;
							final int id = i;
							Server.scheduler.schedule(new Task(34){
								protected void execute(){
									synchronized(npcs[id]){
										if(npcs[id] == null){
											stop();
											return;
										}
										if(!npcs[id].isWeakDelrith || npcs[id].spawnedBy != killerId){
											stop();
											return;
										}
										npcs[id].animNumber = DemonSlayer.HEAL_EMOTE;
										npcs[id].animUpdateRequired = true;
										npcs[id].defence = 100000;
										npcs[id].HP = 750;
										npcs[id].MaxHP = 750;
									}
									stop();
								}
							});
							Server.scheduler.schedule(new Task(35){
								protected void execute(){
									synchronized(npcs[id]){
										if(npcs[id] == null){
											stop();
											return;
										}
										if(!npcs[id].isWeakDelrith || npcs[id].spawnedBy != killerId){
											stop();
											return;
										}
										npcs[id].absX = 20;
										npcs[id].absY = 20;
										npcs[id].updateRequired = true;
										npcs[id].getRegion().removeNpc(npcs[id]);
										npcs[id] = replaceNpc((Client)PlayerHandler.players[killerId], DemonSlayer.DELRITH_ID[0], x, y, z, 0, 750, 43, 450, 220, true, false, id);
										npcs[id].isDelrith = true;
									}
									stop();
								}
							});
							continue;
						}
						if(npcs[i].isWeakDelrith){
							npcs[i].spawnedBy = -1;
							((Client)PlayerHandler.players[npcs[i].killerId]).getQuestHandler().demonSlayer.handleSwordEmpowerment();
						}
						npcs[i].updateRequired = true;
						npcs[i].facePlayer(0);
						npcs[i].killedBy = getNpcKillerId(i);
						npcs[i].animNumber = getDeadEmote(i); // dead emote
						npcs[i].animUpdateRequired = true;
						npcs[i].freezeTimer = 0;
						npcs[i].applyDead = true;
						npcs[i].handleWrath();
						Client t = (Client)PlayerHandler.players[npcs[i].killedBy];
						if(npcs[i].npcType == 2000 && npcs[i].nexSpawn > -1 && npcs[i].nexSpawn < Config.NEX_SPAWNED.length)
							Config.NEX_SPAWNED[npcs[i].nexSpawn] = false;
						if(npcs[i].npcType == 1532){
							CastleWars.killCade(i);
							removeNPC(i);
							continue;
						}
						killedBarrow(i);
						killedNex(i, t);
						if(isFightCaveNpc(i))
							killedTzhaar(i);
						npcs[i].actionTimer = getNPCDeleteTime(i); // delete time
						resetPlayersInCombat(i);
					}else if(npcs[i].actionTimer == 0 && npcs[i].applyDead && !npcs[i].needRespawn){
						npcs[i].needRespawn = true;
						npcs[i].actionTimer = getRespawnTime(i); // respawn time
						dropItems(i); // npc drops items!
						appendSlayerExperience(i);
						appendKillCount(i);
						npcs[i].absX = npcs[i].makeX;
						npcs[i].absY = npcs[i].makeY;
						npcs[i].HP = npcs[i].MaxHP;
						npcs[i].animNumber = 0x328;
						npcs[i].updateRequired = true;
						npcs[i].animUpdateRequired = true;
						if(npcs[i].npcType >= 2440 && npcs[i].npcType <= 2446){
							Server.objectManager.removeObject(npcs[i].absX, npcs[i].absY);
						}
						npcs[i].getRegion().removeNpc(npcs[i]);
						if(npcs[i].npcType == 1158){
							QueenTwo = true;
							queenOneMaxTime = 20000;
							QueenStuff = new int[9];
							QueenStuff[0] = npcs[i].npcType;
							QueenStuff[1] = npcs[i].makeX;
							QueenStuff[2] = npcs[i].makeY;
							QueenStuff[3] = npcs[i].heightLevel;
							QueenStuff[4] = npcs[i].walkingType;
							QueenStuff[5] = npcs[i].MaxHP;
							QueenStuff[6] = npcs[i].maxHit;
							QueenStuff[7] = npcs[i].attack;
							QueenStuff[8] = npcs[i].defence;
							spawnNpc2(1160, npcs[i].absX, npcs[i].absY, npcs[i].heightLevel, npcs[i].walkingType, npcs[i].MaxHP, npcs[i].maxHit, npcs[i].attack, npcs[i].defence);
							npcs[i] = null;
							return;
						}
						if(npcs[i].npcType == 2000){
							npcs[i] = null;
							return;
						}
						if(npcs[i].isWeakDelrith){
							((Client)PlayerHandler.players[npcs[i].killerId]).getQuestHandler().demonSlayer.handleDelrithDefeat();
							removeNPC(i);
							return;
						}
						if(npcs[i].isZombieNPC){
							if(PlayerHandler.players[npcs[i].killedBy] != null && PlayerHandler.players[npcs[i].killedBy].inZombiesGame)
								PlayerHandler.players[npcs[i].killedBy].zombieKills++;
							npcs[i].zombies.zombies.remove((Object)i);
							removeNPC(i);
							return;
						}
						/*if(Server.pestControl.isPestControl(npcs[i].npcType)){
							synchronized(Server.pestControl){
								if(i == Server.pestControl.eastGate)
									Server.pestControl.eastGate = -1;
								else if(i == Server.pestControl.westGate)
									Server.pestControl.westGate = -1;
								else if(i == Server.pestControl.swGate)
									Server.pestControl.swGate = -1;
								else if(i == Server.pestControl.seGate)
									Server.pestControl.seGate = -1;
								Server.pestControl.handlePestDeath(i);
								npcs[i].absX = 20;
								npcs[i].absY = 20;
								npcs[i].updateRequired = true;
								npcs[i].getRegion().removeNpc(npcs[i]);
								Server.pestControl.pestNPCS.remove((Object)i);
								npcs[i] = null;
							}
							return;
						}*/
						if(npcs[i].npcType == 1160){
							queenOneTimer = queenOneMaxTime;
							queenOneRespawn = true;
							npcs[i] = null;
							return;
						}
						if(npcs[i].npcType == 2745){
							handleJadDeath(i);
						}
					}else if(npcs[i].actionTimer == 0 && npcs[i].needRespawn){
						if(npcs[i].spawnedBy > 0 || npcs[i].npcObject)
							npcs[i] = null;
						else{
							int old1 = npcs[i].npcType;
							int old2 = npcs[i].makeX;
							int old3 = npcs[i].makeY;
							int old4 = npcs[i].heightLevel;
							int old5 = npcs[i].walkingType;
							int old6 = npcs[i].MaxHP;
							int old7 = npcs[i].maxHit;
							int old8 = npcs[i].attack;
							int old9 = npcs[i].defence;
							int old10 = npcs[i].combat;
							npcs[i] = null;
							newNPC(old1, old2, old3, old4, old5, old6, old7, old8, old9, old10);
						}
					}
				}
				if(QueenTwo && queenOneTimer == 0 && queenOneRespawn){
					spawnNpc2(QueenStuff[0], QueenStuff[1], QueenStuff[2], QueenStuff[3], QueenStuff[4], QueenStuff[5], QueenStuff[6], QueenStuff[7], QueenStuff[8]);
					QueenStuff = null;
					queenOneRespawn = false;
					QueenTwo = false;
				}
				if(npcs[i] != null)
					npcs[i].setLocation(Location.create(npcs[i].absX, npcs[i].absY, npcs[i].heightLevel));
			}
		}
	}

	public boolean getsPulled(int i){
		switch(npcs[i].npcType){
			case 6260:
				if(npcs[i].firstAttacker > 0)
					return false;
				break;
		}
		return true;
	}

	public boolean multiAttacks(int i){
		switch(npcs[i].npcType){
			case 6222:
				return true;
			case 6247:
			case 6260:
			case 6203:
			case 3006:
			case 8349:
			case 8133:
			case 2000:
				if(npcs[i].attackType > 0)
					return true;
				return false;
		}
		return false;
	}

	/**
	 * Npc killer id?
	 **/

	public int getNpcKillerId(int npcId){
		int oldDamage = 0;
		int killerId = 0;
		for(int p = 1; p < Config.MAX_PLAYERS; p++){
			if(PlayerHandler.players[p] != null){
				if(PlayerHandler.players[p].lastNpcAttacked == npcId){
					if(PlayerHandler.players[p].totalDamageDealt > oldDamage){
						oldDamage = PlayerHandler.players[p].totalDamageDealt;
						killerId = p;
					}
					PlayerHandler.players[p].totalDamageDealt = 0;
				}
			}
		}
		return killerId;
	}

	/**
	 * 
	 */
	private void killedBarrow(int i){
		Client c = (Client)PlayerHandler.players[npcs[i].killedBy];
		if(c != null){
			for(int o = 0; o < c.barrowsNpcs.length; o++){
				if(npcs[i].npcType == c.barrowsNpcs[o][0]){
					c.barrowsNpcs[o][1] = 2; // 2 for dead
					c.barrowsKillCount++;
				}
			}
		}
	}

	private void killedTzhaar(int i){
		Client c = (Client)PlayerHandler.players[npcs[i].spawnedBy];
		if(c == null)
			return;
		c.tzhaarKilled++;
		if(c.tzhaarKilled == c.tzhaarToKill){
			c.waveId++;
			final Client tempClient = c;
			Server.scheduler.schedule(new Task(13){
				protected void execute(){
					synchronized(tempClient){
						synchronized(NPCHandler.npcs){
							if(tempClient != null)
								Server.fightCaves.spawnNextWave(tempClient);
						}
					}
					stop();
				}
			});
		}
	}

	public void handleJadDeath(int i){
		Client c = (Client)PlayerHandler.players[npcs[i].spawnedBy];
		if(c == null)
			return;
		c.inventory.addItem(6570, 1, -1);
		c.sendMessage("Congratulations on completing the fight caves minigame!");
		c.getPA().resetTzhaar();
		c.waveId = 300;
	}

	public void killedNex(int i, Client c){
		if(c == null)
			return;
		if(!c.inNexGame || !Server.clanChat.clans.containsKey(c.clanId) || !npcs[i].isNexGamesNPC)
			return;
		c.nexTotal++;
		final NexGames nexGames = npcs[i].nexGames;
		nexGames.nexLeft--;
		boolean nextWave = nexGames.nexLeft == 0 ? true : false;
		if(nextWave){
			nexGames.nexWave++;
			nexGames.nexLeft = nexGames.nexWave >= Config.NEX_WAVES.length ? -1 : Config.NEX_WAVES[nexGames.nexWave].length;
		}
		if(nexGames.nexLeft == -1){
			nexGames.canClaimReward = true;
			nexGames.canBank = true;
			for(int player : Server.clanChat.clans.get(c.clanId).activeMembers){
				if(player <= 0)
					continue;
				Client c2 = (Client)PlayerHandler.players[player];
				if(c2 == null)
					continue;
				c2.getPA().object(75, 3248, 9364, 3, 10);
				c2.getPA().object(4483, 3248, 9354, 2, 10);
				c2.sendMessage("A bank chest appears through the energy of your fallen foes.");
			}
		}
		if(nextWave && nexGames.nexLeft != -1){
			Server.scheduler.schedule(new Task(8){
				protected void execute(){
					synchronized(nexGames){
						nexGames.spawnWave(nexGames.nexWave);
					}
					stop();
				}
			});
		}
	}

	/**
	 * Dropping Items!
	 **/

	public boolean rareDrops(int i, boolean row){
		int rarity = NPCDrops.dropRarity.get(npcs[i].npcType);
		if(row)
			rarity *= 0.90;
		return Misc.random(rarity) == 0;
	}

	public void dropItems(int i){
		// long start = System.currentTimeMillis();
		Client c = (Client)PlayerHandler.players[npcs[i].killedBy];
		if(c != null){
			if(npcs[i].npcType == 912 || npcs[i].npcType == 913 || npcs[i].npcType == 914)
				c.magePoints += 1;
			HashMap<Integer, Integer> drop = new HashMap<Integer, Integer>();
			if(NPCDrops.constantDrops.get(npcs[i].npcType) != null){
				for(int item : NPCDrops.constantDrops.get(npcs[i].npcType)){
					if(Server.clanChat.clans.containsKey(c.clanId) && Server.clanChat.clans.get(c.clanId).coinshare && PlayerHandler.getONameId(Server.clanChat.clans.get(c.clanId).owner) > 0 && PlayerHandler.players[PlayerHandler.getONameId(Server.clanChat.clans.get(c.clanId).owner)].lootShare)
						drop.put(item, 1);
					else
						Server.itemHandler.createGroundItem(c, item, npcs[i].absX, npcs[i].absY, npcs[i].heightLevel, 1, c.playerId);
					// if (c.clanId >= 0)
					// Server.clanChat.handleLootShare(c, item, 1);
				}
			}
			int cmb = npcs[i].combat, slay = c.playerLevel[c.playerSlayer];
			double isDonor = (c.isDonator == 1 || c.playerRights == 5 ? 1.5 : 1.0);
			boolean row = c.playerEquipment[c.playerRing] == 2572;
			int base = row ? 990000000 : 1000000000;
			int pl = base / ((int)(slay * isDonor));
			boolean giveCracker = false;
			int rand1 = Misc.random(pl);
			if(rand1 <= cmb)
				giveCracker = true;
			if(NPCDrops.dropRarity.get(npcs[i].npcType) != null){
				if(giveCracker){
					if(Server.clanChat.clans.containsKey(c.clanId) && Server.clanChat.clans.get(c.clanId).coinshare){
						if(PlayerHandler.players[PlayerHandler.getONameId(Server.clanChat.clans.get(c.clanId).owner)].lootShare){
							drop.put(Config.CHRISTMAS_CRACKER, 1);
							Server.clanChat.handleLootShare(c, drop, npcs[i].absX, npcs[i].absY, npcs[i].heightLevel);
						}else
							Server.clanChat.handleCoinShare(c, Config.CHRISTMAS_CRACKER, 1);
					}else
						Server.itemHandler.createGroundItem(c, Config.CHRISTMAS_CRACKER, npcs[i].absX, npcs[i].absY, npcs[i].heightLevel, 1, c.playerId);
				}else if(rareDrops(i, row)){
					int random = Misc.random(NPCDrops.rareDrops.get(npcs[i].npcType).length - 1);
					if(Server.clanChat.clans.containsKey(c.clanId) && Server.clanChat.clans.get(c.clanId).coinshare){
						if(PlayerHandler.players[PlayerHandler.getONameId(Server.clanChat.clans.get(c.clanId).owner)].lootShare){
							drop.put(NPCDrops.rareDrops.get(npcs[i].npcType)[random][0], NPCDrops.rareDrops.get(npcs[i].npcType)[random][1]);
							Server.clanChat.handleLootShare(c, drop, npcs[i].absX, npcs[i].absY, npcs[i].heightLevel);
						}else
							Server.clanChat.handleCoinShare(c, NPCDrops.rareDrops.get(npcs[i].npcType)[random][0], NPCDrops.rareDrops.get(npcs[i].npcType)[random][1]);
					}else
						Server.itemHandler.createGroundItem(c, NPCDrops.rareDrops.get(npcs[i].npcType)[random][0], npcs[i].absX, npcs[i].absY, npcs[i].heightLevel, NPCDrops.rareDrops.get(npcs[i].npcType)[random][1], c.playerId);
				}else{
					int random = Misc.random(NPCDrops.normalDrops.get(npcs[i].npcType).length - 1);
					if(Server.clanChat.clans.containsKey(c.clanId) && Server.clanChat.clans.get(c.clanId).coinshare && PlayerHandler.players[PlayerHandler.getONameId(Server.clanChat.clans.get(c.clanId).owner)].lootShare){
						drop.put(NPCDrops.normalDrops.get(npcs[i].npcType)[random][0], NPCDrops.normalDrops.get(npcs[i].npcType)[random][1]);
						Server.clanChat.handleLootShare(c, drop, npcs[i].absX, npcs[i].absY, npcs[i].heightLevel);
					}else if(Server.clanChat.clans.containsKey(c.clanId) && Server.clanChat.clans.get(c.clanId).coinshare)
						Server.clanChat.handleCoinShare(c, NPCDrops.normalDrops.get(npcs[i].npcType)[random][0], NPCDrops.normalDrops.get(npcs[i].npcType)[random][1]);
					else if(NPCDrops.normalDrops.get(npcs[i].npcType).length > 0)
						Server.itemHandler.createGroundItem(c, NPCDrops.normalDrops.get(npcs[i].npcType)[random][0], npcs[i].absX, npcs[i].absY, npcs[i].heightLevel, NPCDrops.normalDrops.get(npcs[i].npcType)[random][1], c.playerId);
				}
			}
		}
		// System.out.println("Took: " + (System.currentTimeMillis() - start));
	}

	public void appendKillCount(int i){
		Client c = (Client)PlayerHandler.players[npcs[i].killedBy];
		if(c != null){
			int kcMonsters[] = {6210, 96};
			for(int j : kcMonsters){
				if(npcs[i].npcType == j){
					if(c.killCount < 20)
						c.killCount++;
					break;
				}
			}
		}
	}

	// id of bones dropped by npcs
	public int boneDrop(int type){
		switch(type){
			case 1:// normal bones
			case 9:
			case 100:
			case 12:
			case 3246:
			case 3247:
			case 3248:
			case 3249:
			case 3250:
			case 3251:
			case 3252:
			case 3261:
			case 3262:
			case 3263:
			case 803:
			case 18:
			case 81:
			case 101:
			case 41:
			case 19:
			case 2036:
			case 2037:
			case 75:
			case 86:
			case 78:
			case 912:
			case 913:
			case 914:
			case 1648:
			case 1643:
			case 1618:
			case 1624:
			case 181:
			case 119:
			case 49:
			case 26:
			case 1341:
				return 526;
			case 117:
				return 532;// big bones
			case 50:// drags
			case 53:
			case 54:
			case 55:
			case 941:
			case 1590:
			case 1591:
			case 1592:
				return 536;
			case 84:
			case 1615:
			case 1613:
			case 82:
			case 3200:
				return 592;
			case 2881:
			case 2882:
			case 2883:
				return 6729;
			default:
				return -1;
		}
	}

	public int getStackedDropAmount(int itemId, int npcId){
		switch(itemId){
			case 995:
				switch(npcId){
					case 1:
						return 50 + Misc.random(50);
					case 9:
						return 133 + Misc.random(100);
					case 1624:
						return 1000 + Misc.random(300);
					case 1618:
						return 1000 + Misc.random(300);
					case 1643:
						return 1000 + Misc.random(300);
					case 1610:
						return 1000 + Misc.random(1000);
					case 1613:
						return 1500 + Misc.random(1250);
					case 1615:
						return 3000;
					case 18:
						return 500;
					case 101:
						return 60;
					case 913:
					case 912:
					case 914:
						return 750 + Misc.random(500);
					case 1612:
						return 250 + Misc.random(500);
					case 1648:
						return 250 + Misc.random(250);
					case 2036:
					case 2037:
						return 200;
					case 82:
						return 1000 + Misc.random(455);
					case 52:
						return 400 + Misc.random(200);
					case 49:
						return 1500 + Misc.random(2000);
					case 1341:
						return 1500 + Misc.random(500);
					case 26:
						return 500 + Misc.random(100);
					case 20:
						return 750 + Misc.random(100);
					case 21:
						return 890 + Misc.random(125);
					case 117:
						return 500 + Misc.random(250);
					case 2607:
						return 500 + Misc.random(350);
				}
				break;
			case 11212:
				return 10 + Misc.random(4);
			case 565:
			case 561:
				return 10;
			case 560:
			case 563:
			case 562:
				return 15;
			case 555:
			case 554:
			case 556:
			case 557:
				return 20;
			case 892:
				return 40;
			case 886:
				return 100;
			case 6522:
				return 6 + Misc.random(5);

		}

		return 1;
	}

	/**
	 * Slayer Experience
	 **/

	public void appendSlayerExperience(int i){
		Client c = (Client)PlayerHandler.players[npcs[i].killedBy];
		if(c == null)
			return;
		if(c.slayerTask != null && c.slayerTask.monster == npcs[i].npcType){
			c.slayerTask.amount--;
			c.getPA().addSkillXP(npcs[i].MaxHP * Config.SLAYER_EXPERIENCE, 18);
			if(c.slayerTask.amount <= 0){
				int points = c.getSlayer().getPoints();
				c.slayerPoints += points;
				c.sendMessage("You completed your " + c.getSlayer().getDifficulty() + " slayer task. Please see a slayer master to get a new one.");
				c.sendMessage("You have been awarded " + points + " slayer points.");
				c.slayerTask = null;
			}
		}
	}

	/**
	 * Resets players in combat
	 */

	public void resetPlayersInCombat(int i){
		for(Player p : RegionManager.getLocalPlayers(npcs[i].getLocation()))
			if(p != null)
				if(p.underAttackBy2 == i)
					p.underAttackBy2 = 0;
	}

	/**
	 * Npc Follow Player
	 **/

	public int GetMove(int Place1, int Place2){
		if((Place1 - Place2) == 0)
			return 0;
		else if((Place1 - Place2) < 0)
			return 1;
		else if((Place1 - Place2) > 0)
			return -1;
		return 0;
	}

	public boolean followPlayer(int i){
		switch(npcs[i].npcType){
			case 2892:
			case 2894:
				return false;
		}
		return true;
	}

	public void followPlayer(int i, int playerId){
		if(PlayerHandler.players[playerId] == null || npcs[i].npcObject || npcs[i].isWeakDelrith){
			return;
		}
		if(npcs[i].isZombieNPC && !npcs[i].inCanifis()){
			npcs[i].underAttack = false;
			npcs[i].underAttackBy = -1;
			npcs[i].walkingHome = true;
			return;
		}
		if(PlayerHandler.players[playerId].respawnTimer > 0){
			npcs[i].facePlayer(0);
			npcs[i].randomWalk = true;
			npcs[i].underAttack = false;
			return;
		}

		if(!followPlayer(i)){
			npcs[i].facePlayer(playerId);
			return;
		}

		int playerX = PlayerHandler.players[playerId].absX;
		int playerY = PlayerHandler.players[playerId].absY;
		npcs[i].randomWalk = false;
		if(goodDistance(npcs[i].getX(), npcs[i].getY(), playerX, playerY, distanceRequired(i)))
			return;
		if((npcs[i].spawnedBy > 0) || ((npcs[i].absX < npcs[i].makeX + Config.NPC_FOLLOW_DISTANCE) && (npcs[i].absX > npcs[i].makeX - Config.NPC_FOLLOW_DISTANCE) && (npcs[i].absY < npcs[i].makeY + Config.NPC_FOLLOW_DISTANCE) && (npcs[i].absY > npcs[i].makeY - Config.NPC_FOLLOW_DISTANCE))){
			if(npcs[i].heightLevel == PlayerHandler.players[playerId].heightLevel){
				if(PlayerHandler.players[playerId] != null && npcs[i] != null){
					if(playerY < npcs[i].absY){
						npcs[i].moveX = GetMove(npcs[i].absX, playerX);
						npcs[i].moveY = GetMove(npcs[i].absY, playerY);
					}else if(playerY > npcs[i].absY){
						npcs[i].moveX = GetMove(npcs[i].absX, playerX);
						npcs[i].moveY = GetMove(npcs[i].absY, playerY);
					}else if(playerX < npcs[i].absX){
						npcs[i].moveX = GetMove(npcs[i].absX, playerX);
						npcs[i].moveY = GetMove(npcs[i].absY, playerY);
					}else if(playerX > npcs[i].absX){
						npcs[i].moveX = GetMove(npcs[i].absX, playerX);
						npcs[i].moveY = GetMove(npcs[i].absY, playerY);
					}else if(playerX == npcs[i].absX || playerY == npcs[i].absY){
						int o = Misc.random(3);
						switch(o){
							case 0:
								npcs[i].moveX = GetMove(npcs[i].absX, playerX);
								npcs[i].moveY = GetMove(npcs[i].absY, playerY + 1);
								break;
							case 1:
								npcs[i].moveX = GetMove(npcs[i].absX, playerX);
								npcs[i].moveY = GetMove(npcs[i].absY, playerY - 1);
								break;
							case 2:
								npcs[i].moveX = GetMove(npcs[i].absX, playerX + 1);
								npcs[i].moveY = GetMove(npcs[i].absY, playerY);
								break;
							case 3:
								npcs[i].moveX = GetMove(npcs[i].absX, playerX - 1);
								npcs[i].moveY = GetMove(npcs[i].absY, playerY);
								break;
						}
					}
					// int x = (npcs[i].absX + npcs[i].moveX);
					// int y = (npcs[i].absY + npcs[i].moveY);
					npcs[i].facePlayer(playerId);
					handleClipping(i);
					npcs[i].getNextNPCMovement(i);
					npcs[i].setLocation(npcs[i].getLocation().transform(npcs[i].moveX, npcs[i].moveY));
					npcs[i].facePlayer(playerId);
					npcs[i].updateRequired = true;
				}
			}
		}else{
			npcs[i].facePlayer(0);
			npcs[i].randomWalk = true;
			npcs[i].underAttack = false;
		}
	}

	public void handleClipping(int i){
		NPC npc = npcs[i];
		if(npc.ignoreClip)
			return;
		if(npc.moveX == 1 && npc.moveY == 1){
			if(!Region.canMove(npc.absX, npc.absY, npc.absX + 1, npc.absY + 1, npc.heightLevel, npc.x_length, npc.y_length)){
				npc.moveX = 0;
				npc.moveY = 0;
				if(Region.canMove(npc.absX, npc.absY, npc.absX, npc.absY + 1, npc.heightLevel, npc.x_length, npc.y_length))
					npc.moveY = 1;
				else
					npc.moveX = 1;
			}
		}else if(npc.moveX == -1 && npc.moveY == -1){
			if(!Region.canMove(npc.absX, npc.absY, npc.absX - 1, npc.absY - 1, npc.heightLevel, npc.x_length, npc.y_length)){
				npc.moveX = 0;
				npc.moveY = 0;
				if(Region.canMove(npc.absX, npc.absY, npc.absX, npc.absY - 1, npc.heightLevel, npc.x_length, npc.y_length))
					npc.moveY = -1;
				else
					npc.moveX = -1;
			}
		}else if(npc.moveX == 1 && npc.moveY == -1){
			if(!Region.canMove(npc.absX, npc.absY, npc.absX + 1, npc.absY - 1, npc.heightLevel, npc.x_length, npc.y_length)){
				npc.moveX = 0;
				npc.moveY = 0;
				if(Region.canMove(npc.absX, npc.absY, npc.absX, npc.absY - 1, npc.heightLevel, npc.x_length, npc.y_length))
					npc.moveY = -1;
				else
					npc.moveX = 1;
			}
		}else if(npc.moveX == -1 && npc.moveY == 1){
			if(!Region.canMove(npc.absX, npc.absY, npc.absX - 1, npc.absY + 1, npc.heightLevel, npc.x_length, npc.y_length)){
				npc.moveX = 0;
				npc.moveY = 0;
				if(Region.canMove(npc.absX, npc.absY, npc.absX, npc.absY + 1, npc.heightLevel, npc.x_length, npc.y_length))
					npc.moveY = 1;
				else
					npc.moveX = -1;
			}
		} // Checking Diagonal movement.
		if(npc.moveY == -1){
			if(!Region.canMove(npc.absX, npc.absY, npc.absX, npc.absY - 1, npc.heightLevel, npc.x_length, npc.y_length))
				npc.moveY = 0;
		}else if(npc.moveY == 1){
			if(!Region.canMove(npc.absX, npc.absY, npc.absX, npc.absY + 1, npc.heightLevel, npc.x_length, npc.y_length))
				npc.moveY = 0;
		} // Checking Y movement.
		if(npc.moveX == 1){
			if(!Region.canMove(npc.absX, npc.absY, npc.absX + 1, npc.absY, npc.heightLevel, npc.x_length, npc.y_length))
				npc.moveX = 0;
		}else if(npc.moveX == -1){
			if(!Region.canMove(npc.absX, npc.absY, npc.absX - 1, npc.absY, npc.heightLevel, npc.x_length, npc.y_length))
				npc.moveX = 0;
		} // Checking X movement.
	}

	/*
	 * public void handleClipping(int i){ NPC npc = npcs[i]; if(npc.moveX == 1
	 * && npc.moveY == 1){ if((Region.getClipping(npc.absX + 1, npc.absY + 1,
	 * npc.heightLevel) & 0x12801e0) != 0){ npc.moveX = 0; npc.moveY = 0;
	 * if((Region.getClipping(npc.absX, npc.absY + 1, npc.heightLevel) &
	 * 0x1280120) == 0) npc.moveY = 1; else npc.moveX = 1; } }else if(npc.moveX
	 * == -1 && npc.moveY == -1){ if((Region.getClipping(npc.absX - 1, npc.absY
	 * - 1, npc.heightLevel) & 0x128010e) != 0){ npc.moveX = 0; npc.moveY = 0;
	 * if((Region.getClipping(npc.absX, npc.absY - 1, npc.heightLevel) &
	 * 0x1280102) == 0) npc.moveY = -1; else npc.moveX = -1; } }else
	 * if(npc.moveX == 1 && npc.moveY == -1){ if((Region.getClipping(npc.absX +
	 * 1, npc.absY - 1, npc.heightLevel) & 0x1280183) != 0){ npc.moveX = 0;
	 * npc.moveY = 0; if((Region.getClipping(npc.absX, npc.absY - 1,
	 * npc.heightLevel) & 0x1280102) == 0) npc.moveY = -1; else npc.moveX = 1; }
	 * }else if(npc.moveX == -1 && npc.moveY == 1){
	 * if((Region.getClipping(npc.absX - 1, npc.absY + 1, npc.heightLevel) &
	 * 0x128013) != 0){ npc.moveX = 0; npc.moveY = 0;
	 * if((Region.getClipping(npc.absX, npc.absY + 1, npc.heightLevel) &
	 * 0x1280120) == 0) npc.moveY = 1; else npc.moveX = -1; } } //Checking
	 * Diagonal movement. if (npc.moveY == -1){ if
	 * ((Region.getClipping(npc.absX, npc.absY - 1, npc.heightLevel) &
	 * 0x1280102) != 0) npc.moveY = 0; }else if(npc.moveY == 1){
	 * if((Region.getClipping(npc.absX, npc.absY + 1, npc.heightLevel) &
	 * 0x1280120) != 0) npc.moveY = 0; } //Checking Y movement. if(npc.moveX ==
	 * 1){ if((Region.getClipping(npc.absX + 1, npc.absY, npc.heightLevel) &
	 * 0x1280180) != 0) npc.moveX = 0; }else if(npc.moveX == -1){
	 * if((Region.getClipping(npc.absX - 1, npc.absY, npc.heightLevel) &
	 * 0x1280108) != 0) npc.moveX = 0; } //Checking X movement. }
	 */
	
	public int getNexMelee(int npc){
		Collection<Player> players = RegionManager.getLocalPlayers(npcs[npc].getLocation());
		for(Player player : players){
			if(player == null)
				continue;
			if(player.goodDistance(npcs[npc].absX, npcs[npc].absY, player.absX, player.absY, 1) && npcs[npc].heightLevel == player.heightLevel)
				return player.playerId;
		}
		return -1;
	}

	/**
	 * load spell
	 **/
	public void loadSpell2(int i){
		npcs[i].attackType = 3;
		int random = Misc.random(3);
		if(random == 0){
			npcs[i].projectileId = 393; // red
			npcs[i].endGfx = 430;
		}else if(random == 1){
			npcs[i].projectileId = 394; // green
			npcs[i].endGfx = 429;
		}else if(random == 2){
			npcs[i].projectileId = 395; // white
			npcs[i].endGfx = 431;
		}else if(random == 3){
			npcs[i].projectileId = 396; // blue
			npcs[i].endGfx = 428;
		}
	}

	public void loadSpell(int i){
		int shield = PlayerHandler.players[npcs[i].killerId].playerEquipment[PlayerHandler.players[npcs[i].killerId].playerShield];
		switch(npcs[i].npcType){
			case 2892:
				npcs[i].projectileId = 94;
				npcs[i].attackType = 2;
				npcs[i].endGfx = 95;
				break;
			case 2894:
				npcs[i].projectileId = 298;
				npcs[i].attackType = 1;
				break;
			// Torchers
			case 3757:
			case 3759:
			case 3760:
			case 3761:
				npcs[i].projectileId = 280;
				npcs[i].attackType = 2;
				npcs[i].endGfx = 659;
				break;
			// Defilers
			case 3768:
			case 3769:
			case 3770:
				npcs[i].projectileId = 280;
				npcs[i].attackType = 1;
				break;
			case 912: // Zammy
				npcs[i].projectileId = 0;
				npcs[i].attackType = 2;
				npcs[i].endGfx = 78;
				break;
			case 913: // Sara
				npcs[i].projectileId = 0;
				npcs[i].attackType = 2;
				npcs[i].endGfx = 76;
				break;
			case 914: // Guthix
				npcs[i].projectileId = 0;
				npcs[i].attackType = 2;
				npcs[i].endGfx = 77;
				break;
			case 53:
			case 54:
			case 55:
			case 941:
				int random = Misc.random(1);
				npcs[i].projectileId = -1; // red
				npcs[i].endGfx = -1;
				npcs[i].attackType = 0;
				if(random == 1){
					npcs[i].projectileId = 393; // red
					npcs[i].endGfx = ((shield == 11283 || shield == 11284) ? 1164 : 430);
					npcs[i].attackType = 3;
				}
				break;
			case 50:
				random = Misc.random(4);
				if(random == 0){
					npcs[i].projectileId = 393; // red
					npcs[i].endGfx = 430;
					npcs[i].attackType = 3;
				}else if(random == 1){
					npcs[i].projectileId = 394; // green
					npcs[i].endGfx = 429;
					npcs[i].attackType = 3;
				}else if(random == 2){
					npcs[i].projectileId = 395; // white
					npcs[i].endGfx = 431;
					npcs[i].attackType = 3;
				}else if(random == 3){
					npcs[i].projectileId = 396; // blue
					npcs[i].endGfx = 428;
					npcs[i].attackType = 3;
				}else if(random == 4){
					npcs[i].projectileId = -1; // melee
					npcs[i].endGfx = -1;
					npcs[i].attackType = 0;
				}
				if((shield == 11283 || shield == 11284) && random != 4)
					npcs[i].endGfx = 1164;
				break;
			case 6203:
				random = Misc.random(2);
				if(random == 0 || random == 1)
					npcs[i].attackType = 0;
				else{
					npcs[i].attackType = 2;
					npcs[i].endGfx = 616;
					npcs[i].projectileId = 632;
				}
				break;
			case 8349:
				random = Misc.random(2);
				npcs[i].attackType = random;
				npcs[i].projectileId = -1;
				npcs[i].endGfx = -1;
				if(random == 1){ // Range
					npcs[i].projectileId = 1884;
					npcs[i].endGfx = 429;
				}else if(random == 2){ // Magic
					npcs[i].projectileId = 1885;
					npcs[i].endGfx = 431;
				}
				break;
			case 2000: // Nex
				int meleeId = getNexMelee(i);
				boolean melee = meleeId > 0 && Misc.random(99) > 60;
				npcs[i].projectileId = -1;
				npcs[i].endGfx = -1;
				npcs[i].attackType = melee ? 0 : 2;
				if(melee)
					npcs[i].killerId = npcs[i].killedBy = npcs[i].oldIndex = meleeId;
				else{
					npcs[i].nexSpell = npcs[i].nexStage;
					npcs[i].endGfx = npcs[i].nexSpell == 1 ? 391 : npcs[i].nexSpell == 2 ? 381 : npcs[i].nexSpell == 3 ? 377 : npcs[i].nexSpell == 4 ? 369 : 1854;
				}
				break;
			case 2001: // Nomad
				random = !goodDistance(npcs[i].absX, npcs[i].absY, PlayerHandler.players[npcs[i].killerId].absX, PlayerHandler.players[npcs[i].killerId].absY, 1) ? 2 : Misc.random(2);
				if(random == 0){
					npcs[i].attackType = 0;
					npcs[i].projectileId = -1;
					npcs[i].endGfx = -1;
					npcs[i].maxHit = 32;
				}else{
					random = Misc.random(99) + 1;
					npcs[i].attackType = 2;
					if(random >= 90){
						npcs[i].projectileId = -1;
						npcs[i].endGfx = 1364;
						npcs[i].invulnerableTick = NPC.INVULNERABLE_TICK;
						npcs[i].maxHit = 75;
					}else{
						random = (npcs[i].HP < (npcs[i].MaxHP / 3)) && ((Misc.random(99) + 1) > 40) ? 0 : 1;
						npcs[i].projectileId = random == 0 ? -1 : 605;
						npcs[i].endGfx = random == 0 ? 1449 : 2282;
						npcs[i].maxHit = 45 + random == 0 ? 10 : 0;
						npcs[i].healAttack = random == 0;
					}
				}
				break;
			case 8133:
				random = Misc.random(2);
				npcs[i].attackType = random;
				npcs[i].projectileId = -1;
				npcs[i].endGfx = -1;
				if(random == 1){
					npcs[i].projectileId = 1823;
					npcs[i].endGfx = 1624;
				}else if(random == 2){
					npcs[i].projectileId = 1824;
					npcs[i].endGfx = 1625;
				}
				break;
			case 6204:
				npcs[i].attackType = 0;
				break;
			case 6206:
				npcs[i].attackType = 1; // Range
				npcs[i].projectileId = 629;
				break;
			case 6208:
				npcs[i].attackType = 2; // Magic
				npcs[i].projectileId = 607;
				break;
			case 13474:
				random = Misc.random(2);
				if(random == 2)
					random = 0;
				npcs[i].attackType = random;
				if(random != 0)
					npcs[i].projectileId = 1278;
				break;
			case 13476:
				random = Misc.random(2);
				if(random == 2)
					random = 0;
				npcs[i].attackType = random;
				if(random != 0)
					npcs[i].projectileId = 1278;
				break;
			case 13477:
				random = Misc.random(2);
				if(random == 2)
					random = 0;
				npcs[i].attackType = random;
				if(random != 0)
					npcs[i].projectileId = 1278;
				break;
			case 13478:
				random = Misc.random(2);
				npcs[i].attackType = random;
				if(random != 0)
					npcs[i].projectileId = random == 2 ? 1276 : 1278;
				break;
			case 13480:
				random = Misc.random(2);
				npcs[i].attackType = random;
				if(random != 0)
					npcs[i].projectileId = random == 2 ? 1276 : 1278;
				break;
			case 13481:
				random = Misc.random(2);
				npcs[i].attackType = random;
				if(random != 0)
					npcs[i].projectileId = random == 2 ? 1276 : 1278;
				break;
			// arma npcs
			case 6227:
				npcs[i].attackType = 0;
				break;
			case 6225:
				npcs[i].attackType = 1;
				npcs[i].projectileId = 1190;
				break;
			case 6223:
				npcs[i].attackType = 2;
				npcs[i].projectileId = 1203;
				break;
			case 6222:
				random = Misc.random(1);
				npcs[i].attackType = 1 + random;
				if(npcs[i].attackType == 1){
					npcs[i].projectileId = 1197;
				}else{
					npcs[i].attackType = 2;
					npcs[i].projectileId = 1198;
				}
				break;
			// sara npcs
			case 6247: // sara
				random = Misc.random(1);
				if(random == 0){
					npcs[i].attackType = 2;
					npcs[i].endGfx = 1224;
					npcs[i].projectileId = -1;
				}else if(random == 1)
					npcs[i].attackType = 0;
				break;
			case 6248: // star
				npcs[i].attackType = 0;
				break;
			case 6250: // growler
				npcs[i].attackType = 2;
				npcs[i].projectileId = 1203;
				break;
			case 6252: // bree
				npcs[i].attackType = 1;
				npcs[i].projectileId = 9;
				break;
			// bandos npcs
			case 6260:
				random = Misc.random(2);
				if(random == 0 || random == 1)
					npcs[i].attackType = 0;
				else{
					npcs[i].attackType = 1;
					npcs[i].endGfx = 1211;
					npcs[i].projectileId = 288;
				}
				break;
			case 6261:
				npcs[i].attackType = 0;
				break;
			case 6263:
				npcs[i].attackType = 2;
				npcs[i].projectileId = 1203;
				break;
			case 6265:
				npcs[i].attackType = 1;
				npcs[i].projectileId = 1206;
				break;
			case 2025:
				npcs[i].attackType = 2;
				int r = Misc.random(3);
				if(r == 0){
					npcs[i].gfx100(158);
					npcs[i].projectileId = 159;
					npcs[i].endGfx = 160;
				}
				if(r == 1){
					npcs[i].gfx100(161);
					npcs[i].projectileId = 162;
					npcs[i].endGfx = 163;
				}
				if(r == 2){
					npcs[i].gfx100(164);
					npcs[i].projectileId = 165;
					npcs[i].endGfx = 166;
				}
				if(r == 3){
					npcs[i].gfx100(155);
					npcs[i].projectileId = 156;
				}
				break;
			case 2881:// supreme
				npcs[i].attackType = 1;
				npcs[i].projectileId = 298;
				break;

			case 2882:// prime
				npcs[i].attackType = 2;
				npcs[i].projectileId = 162;
				npcs[i].endGfx = 477;
				break;

			case 2028:
				npcs[i].attackType = 1;
				npcs[i].projectileId = 27;
				break;

			case 3200:
				int r2 = Misc.random(1);
				if(r2 == 0){
					npcs[i].attackType = 1;
					npcs[i].gfx100(550);
					npcs[i].projectileId = 551;
					npcs[i].endGfx = 552;
				}else{
					npcs[i].attackType = 2;
					npcs[i].gfx100(553);
					npcs[i].projectileId = 554;
					npcs[i].endGfx = 555;
				}
				break;
			case 2745:
				int r3 = goodDistance(npcs[i].absX, npcs[i].absY, PlayerHandler.players[npcs[i].spawnedBy].absX, PlayerHandler.players[npcs[i].spawnedBy].absY, 1) ? Misc.random(2) : Misc.random(1);
				npcs[i].attackType = r3;
				npcs[i].endGfx = (r3 == 0 ? -1 : (r3 == 1 ? 451 : 157));
				npcs[i].projectileId = (r3 == 0 ? -1 : (r3 == 1 ? 1627 : 448));
				break;
			case 2743:
				npcs[i].attackType = 2;
				npcs[i].projectileId = 445;
				npcs[i].endGfx = 446;
				break;

			case 2631:
				npcs[i].attackType = 1;
				npcs[i].projectileId = 443;
				break;
		}
	}

	/**
	 * Distanced required to attack
	 **/
	public int distanceRequired(int i){
		switch(npcs[i].npcType){
			case 2025:
			case 2028:
				return 6;
			case 2001:
				return 10;
			case 50:
			case 6247:
				return 2;
			case 2881:// dag kings
			case 2882:
			case 3200:// chaos ele
			case 2743:
			case 2631:
			case 2745:
				return 8;
			case 2883:// rex
				return 1;
			case 6263:
			case 6265:
			case 2556:
			case 2557:
			case 6222:
			case 6223:
			case 6225:
			case 6250:
			case 6252:
				return 9;
				// things around dags
			case 2892:
			case 2894:
				return 10;
			case 2000:
				return 10;
			default:
				return 1;
		}
	}

	public int followDistance(int i){
		switch(npcs[i].npcType){
			case 6203:
			case 6204:
			case 6206:
			case 6208:
			case 6260:
			case 6261:
			case 6263:
			case 6265:
			case 6247:
			case 6248:
			case 6250:
			case 6252:
			case 6222:
			case 6223:
			case 6225:
			case 6227:
			case 2883:
				return 4;
				// Zombies
			case 73:
			case 74:
			case 75:
			case 76:
			case 422:
			case 423:
			case 424:
				return 25;
			case 2000:
				return 30;
			case 8349:
			case 8133:
				return 10;
			case 2881:
			case 2882:
				return 1;
			case 3005:
				return 2;
			default:
				return 4;
		}
	}

	public int getProjectileSpeed(int i){
		switch(npcs[i].npcType){
			case 2881:
			case 2882:
			case 3200:
				return 85;

			case 2745:
				return 130;

			case 50:
				return 90;

			case 2025:
				return 85;

			case 2028:
				return 80;

			default:
				return 85;
		}
	}

	/**
	 * NPC Attacking Player
	 **/

	public void attackPlayer(Client c, int i){
		if(npcs[i] != null && !npcs[i].npcObject){
			if(npcs[i].isDead)
				return;
			if(!npcs[i].inMulti() && npcs[i].underAttackBy > 0 && npcs[i].underAttackBy != c.playerId){
				npcs[i].killerId = 0;
				return;
			}
			if(!npcs[i].inMulti() && (c.underAttackBy > 0 || (c.underAttackBy2 > 0 && c.underAttackBy2 != i))){
				npcs[i].killerId = 0;
				return;
			}
			if(npcs[i].heightLevel != c.heightLevel){
				npcs[i].killerId = 0;
				return;
			}
			npcs[i].facePlayer(c.playerId);
			boolean special = false;// specialCase(c,i);
			if(goodDistance(npcs[i].getX(), npcs[i].getY(), c.getX(), c.getY(), distanceRequired(i)) || special){
				if(c.respawnTimer <= 0){
					npcs[i].attackType = 0;
					if(special)
						loadSpell2(i);
					else
						loadSpell(i);
					npcs[i].facePlayer(c.playerId);
					npcs[i].attackTimer = getNpcDelay(i);
					npcs[i].hitDelayTimer = getHitDelay(i);
					//if(Server.pestControl.isPestControl(npcs[i].npcType) && (c.inPestTower() || c.inPestGates()) && npcs[i].attackType == 0)
						//return;
					if(npcs[i].attackType == 3)
						npcs[i].hitDelayTimer += 2;
					if(multiAttacks(i)){
						multiAttackGfx(i, npcs[i].projectileId);
						startAnimation(getAttackEmote(i), i);
						npcs[i].oldIndex = c.playerId;
						return;
					}
					if(npcs[i].projectileId > 0){
						int nX = NPCHandler.npcs[i].getX() + offset(i);
						int nY = NPCHandler.npcs[i].getY() + offset(i);
						int pX = c.getX();
						int pY = c.getY();
						int offX = (nY - pY) * -1;
						int offY = (nX - pX) * -1;
						c.getPA().createPlayersProjectile(nX, nY, offX, offY, 50, getProjectileSpeed(i), npcs[i].projectileId, 43, 31, -c.getId() - 1, 65);
					}
					c.underAttackBy2 = i;
					c.singleCombatDelay2 = System.currentTimeMillis();
					npcs[i].oldIndex = c.playerId;
					startAnimation(getAttackEmote(i), i);
					if(npcs[i].attackType == 0 && npcs[i].endGfx > 0)
						c.gfx100(npcs[i].endGfx);
					c.getPA().removeAllWindows();
					if(c.isWearingRing){
						c.getItems().removeItem(Config.EASTER_RING, c.playerRing);
						c.isWearingRing = false;
						c.npcId = -1;
						c.isNpc = false;
						c.updateRequired = true;
						c.appearanceUpdateRequired = true;
					}
				}
			}
		}
	}

	public int offset(int i){
		switch(npcs[i].npcType){
			case 50:
				return 2;
			case 2881:
			case 2882:
				return 1;
			case 2745:
			case 2743:
				return 1;
		}
		return 0;
	}

	public boolean specialCase(Client c, int i){ // responsible for npcs that
													// much
		if(goodDistance(npcs[i].getX(), npcs[i].getY(), c.getX(), c.getY(), 8) && !goodDistance(npcs[i].getX(), npcs[i].getY(), c.getX(), c.getY(), distanceRequired(i)))
			return true;
		return false;
	}

	public boolean retaliates(int npcType){
		return npcType < 3777 || npcType > 3780 && !(npcType >= 2440 && npcType <= 2446);
	}

	public void applyDamage(int i){
		if(npcs[i] != null){
			if(PlayerHandler.players[npcs[i].oldIndex] == null){
				return;
			}
			if(npcs[i].isDead)
				return;
			Client c = (Client)PlayerHandler.players[npcs[i].oldIndex];
			if(multiAttacks(i)){
				multiAttackDamage(i);
				return;
			}
			if(((npcs[i].npcType >= 50 && npcs[i].npcType <= 55) || npcs[i].npcType == 941) && (c.playerEquipment[c.playerShield] == 11284 || (c.playerEquipment[c.playerShield] == 11283 && c.dfsCharges < 50)) && npcs[i].attackType == 3){
				if(c.playerEquipment[c.playerShield] == 11284){
					c.getItems().deleteEquipment(1, c.playerShield);
					c.getItems().setEquipment(11283, 1, c.playerShield);
				}
				c.updateRequired = true;
				c.dfsCharges = 50;
				c.getItems().resetBonus();
				c.getItems().getBonus();
				c.getItems().writeBonus();
				c.saveGame();
				c.sendMessage("Your shield has been fully charged.");
			}
			if(c.playerIndex <= 0 && c.npcIndex <= 0)
				if(c.autoRet == 1)
					c.npcIndex = i;
			if(c.attackTimer <= 3 || c.attackTimer == 0 && c.npcIndex == 0 && c.oldNpcIndex == 0){
				c.startAnimation(c.getCombat().getBlockEmote());
			}
			if(c.respawnTimer <= 0){
				int damage = 0;
				if(npcs[i].attackType == 0){
					damage = Misc.random(getMaxHit(i));
					int damage2 = 0;
					int tempdam = damage;
					if(npcs[i].npcType == 2000 || npcs[i].npcType == 2001 || npcs[i].npcType == 1158 || npcs[i].npcType == 1160 || npcs[i].npcType == 879){
						if(c.prayerActive[18] || c.prayerActive[35]){
							damage *= ((npcs[i].npcType == 2000 || npcs[i].npcType == 2001) ? 0.75 : 0.6);
							if(c.prayerActive[35])
								damage2 = (((Misc.random(99) + 1) >= 65) ? Math.round(tempdam / 10) : 0);
						}
					}else{
						if(c.prayerActive[18] || c.prayerActive[35]){
							damage = 0;
							if(c.prayerActive[35])
								damage2 = (((Misc.random(99) + 1) >= 65) ? Math.round(tempdam / 10) : 0);
						}
					}
					if(npcs[i].npcType == 2000 && NexGames.hasFullTorva(c))
						damage = (int)Math.ceil(damage * 0.80);
					if(c.playerEquipment[c.playerShield] == 18363 && damage >= 20){
						c.sendMessage("Your shield soaks up 20% damage from the original " + damage);
						damage = (int)Math.round(damage * 0.80);
					}
					if(damage2 > 0){
						if(npcs[i].HP - damage2 < 0)
							damage2 = npcs[i].HP;
						npcs[i].HP -= damage2;
						npcs[i].hitDiff = damage2;
						npcs[i].updateRequired = true;
						npcs[i].hitUpdateRequired = true;
						c.totalDamageDealt += damage2;
					}
					if(10 + Misc.random(c.getCombat().calculateMeleeDefence()) > Misc.random(NPCHandler.npcs[i].attack)){
						damage = 0;
					}
					if(c.playerLevel[3] - damage < 0){
						damage = c.playerLevel[3];
					}
				}
				if(npcs[i].attackType == 1){ // range
					damage = Misc.random(npcs[i].maxHit);
					int damage2 = 0;
					int tempdam = damage;
					if(npcs[i].npcType == 2000 || npcs[i].npcType == 2001 || npcs[i].npcType == 1158 || npcs[i].npcType == 1160 || npcs[i].npcType == 879){
						if(c.prayerActive[17] || c.prayerActive[34]){
							damage *= ((npcs[i].npcType == 2000 || npcs[i].npcType == 2001) ? 0.75 : 0.6);
							if(c.prayerActive[34])
								damage2 = (((Misc.random(99) + 1) >= 65) ? Math.round(tempdam / 10) : 0);
						}
					}else{
						if(c.prayerActive[17] || c.prayerActive[34]){
							damage = 0;
							if(c.prayerActive[34])
								damage2 = (((Misc.random(99) + 1) >= 65) ? Math.round(tempdam / 10) : 0);
						}
					}
					if(damage2 > 0){
						if(npcs[i].HP - damage2 < 0)
							damage2 = npcs[i].HP;
						npcs[i].HP -= damage2;
						npcs[i].hitDiff = damage2;
						npcs[i].updateRequired = true;
						npcs[i].hitUpdateRequired = true;
						c.totalDamageDealt += damage2;
					}
					if(c.playerEquipment[c.playerShield] == 18361 && damage >= 20){
						c.sendMessage("Your shield soaks up 12% damage from the original " + damage);
						damage = (int)Math.round(damage * 0.88);
					}
					if(c.playerEquipment[c.playerHands] == 22358 || c.playerEquipment[c.playerHands] == 22359 ||
							c.playerEquipment[c.playerHands] == 22360 || c.playerEquipment[c.playerHands] == 22361)
						damage = ((int)Math.floor(damage * 0.95));
					if(c.playerLevel[3] - damage < 0){
						damage = c.playerLevel[3];
					}
					if(10 + Misc.random(c.getCombat().calculateRangeDefence()) > Misc.random(NPCHandler.npcs[i].attack)){
						damage = 0;
					}
					if(damage > 0 && npcs[i].npcType >= 13474 && npcs[i].npcType <= 13481 && c.poisonDamage <= 0)
						c.getPA().appendPoison(8);
				}

				if(npcs[i].attackType == 2){ // magic
					damage = Misc.random_range(npcs[i].npcType == 2001 ? 10 : 0, npcs[i].maxHit);
					boolean magicFailed = false;
					if(10 + Misc.random(c.getCombat().mageDef()) > Misc.random(NPCHandler.npcs[i].attack) && npcs[i].npcType != 2001){
						damage = 0;
						magicFailed = true;
					}
					int damage2 = 0;
					int tempdam = damage;
					if(npcs[i].npcType == 2000 || npcs[i].npcType == 2001 || npcs[i].npcType == 1158 || npcs[i].npcType == 1160 || npcs[i].npcType == 879){
						if(c.prayerActive[16] || c.prayerActive[33]){
							damage *= ((npcs[i].npcType == 2000 || npcs[i].npcType == 2001) ? 0.75 : 0.6);
							if(c.prayerActive[33])
								damage2 = (((Misc.random(99) + 1) >= 65) ? Math.round(tempdam / 10) : 0);
						}
					}else{
						if(c.prayerActive[16] || c.prayerActive[33]){
							damage = 0;
							if(c.prayerActive[33])
								damage2 = (((Misc.random(99) + 1) >= 65) ? Math.round(tempdam / 10) : 0);
						}
					}
					if(damage2 > 0){
						if(npcs[i].HP - damage2 < 0)
							damage2 = npcs[i].HP;
						npcs[i].HP -= damage2;
						npcs[i].hitDiff = damage2;
						npcs[i].updateRequired = true;
						npcs[i].hitUpdateRequired = true;
						c.totalDamageDealt += damage2;
					}
					if(npcs[i].healAttack && npcs[i].npcType == 2001){
						npcs[i].HP += tempdam * 0.75;
						npcs[i].hitUpdateRequired = true;
						npcs[i].updateRequired = true;
						npcs[i].gfx0(1308);
						npcs[i].hitDiff = 0;
					}
					if(npcs[i].nexSpell > 0){
						switch(npcs[i].nexSpell){
							case 1: // Smoke
								int random = Misc.random(99) > 69 ? 1 : 0;
								if(Misc.random(3) == 3 && c.poisonDamage <= 0)
									c.getPA().appendPoison(6);
								if(random == 0){
									if(c.playerLevel[c.playerPrayer] > 0)
										c.playerLevel[c.playerPrayer] -= 4 > c.playerLevel[c.playerPrayer] ? c.playerLevel[c.playerPrayer] : 4;
									c.getPA().refreshSkill(c.playerPrayer);
									if(Misc.random(99) >= 69)
										npcs[i].forceChat("Let the virus flow through you!");
								}else{
									if(c.playerEquipment[c.playerShield] != 19617)
										c.getCombat().resetPrayers();
									npcs[i].forceChat("There is... NO ESCAPE!");
									int x = Misc.random(99) > 49 ? 1 : -1;
									int y = Misc.random(99) > 49 ? 1 : -1;
									c.getPA().movePlayer(npcs[i].absX + x, npcs[i].absY + y, c.heightLevel);
								}
								break;
							case 2: // Shadow
								c.playerLevel[c.playerAttack] = (int)(c.getLevelForXP(c.playerXP[c.playerAttack]) * 0.85);
								c.getPA().refreshSkill(c.playerAttack);
								break;
							case 3: // Blood
								if(Misc.random(99) > 59)
									npcs[i].forceChat("I demand a blood sacrifice!");
								npcs[i].HP += tempdam * 0.25;
								npcs[i].hitUpdateRequired = true;
								npcs[i].updateRequired = true;
								break;
							case 4: // Ice
								if(c.freezeTimer <= -3){
									c.freezeTimer = 33;
									c.resetWalkingQueue();
									c.sendMessage("You have been frozen.");
									npcs[i].forceChat("Die now, in a prison of ice!");
								}
								break;
							case 5: // Miasmic
								if(!c.miasmicEffect){
									c.miasmicEffect = true;
									c.miasmicTime = Misc.currentTimeSeconds() + 48;
								}
								break;
							default:
								break;
						}
					}
					if(c.playerEquipment[c.playerHands] == 22362 || c.playerEquipment[c.playerHands] == 22363 ||
							c.playerEquipment[c.playerHands] == 22364 || c.playerEquipment[c.playerHands] == 22365)
						damage = ((int)Math.floor(damage * 0.95));
					if(c.playerEquipment[c.playerShield] == 18361 && damage >= 20){
						c.sendMessage("Your shield soaks up 18% damage from the original " + damage);
						damage = (int)Math.round(damage * 0.82);
					}
					if(c.playerEquipment[c.playerShield] == 18363 && damage >= 20){
						c.sendMessage("Your shield soaks up 10% damage from the original " + damage);
						damage = (int)Math.round(damage * 0.90);
					}
					if(c.playerLevel[3] - damage < 0){
						damage = c.playerLevel[3];
					}
					if(npcs[i].endGfx > 0 && (!magicFailed || isFightCaveNpc(i))){
						c.gfx0(npcs[i].endGfx);
					}else{
						c.gfx100(85);
					}
				}

				if(npcs[i].attackType == 3){ // fire breath
					int anti = c.getPA().antiFire();
					if(anti == 0){
						damage = Misc.random(30) + 10;
						c.sendMessage("You are badly burnt by the dragon fire!");
					}else if(anti == 1)
						damage = Misc.random(15);
					else if(anti >= 2)
						damage = 0;
					if(c.playerLevel[3] - damage < 0)
						damage = c.playerLevel[3];
					c.gfx100(npcs[i].endGfx);
				}
				handleSpecialEffects(c, i, damage);
				c.logoutDelay = System.currentTimeMillis(); // logout delay
				// c.setHitDiff(damage);
				c.dealDamage(damage);
				c.getPA().refreshSkill(3);
				c.updateRequired = true;
				// c.setHitUpdateRequired(true);
			}
		}
	}

	public void handleSpecialEffects(Client c, int i, int damage){
		if(npcs[i].npcType == 2892 || npcs[i].npcType == 2894){
			if(damage > 0){
				if(c != null){
					if(c.playerLevel[5] > 0){
						c.playerLevel[5]--;
						c.getPA().refreshSkill(5);
						c.getPA().appendPoison(12);
					}
				}
			}
		}

	}

	public void startAnimation(int animId, int i){
		npcs[i].animNumber = animId;
		npcs[i].animUpdateRequired = true;
		npcs[i].updateRequired = true;
	}

	public boolean goodDistance(int objectX, int objectY, int playerX, int playerY, int distance){
		return playerX >= objectX - distance && playerX <= objectX + distance && playerY >= objectY - distance && playerY <= objectY + distance;
	}

	public int getMaxHit(int i){
		switch(npcs[i].npcType){
			case 2000:
				switch(npcs[i].attackType){
					case 0:
						return 70;
					case 1:
						return 0;
					case 2:
						if(npcs[i].nexSpell == 1)
							return 40;
						else if(npcs[i].nexSpell == 2)
							return 45;
						else if(npcs[i].nexSpell == 3)
							return 52;
						else if(npcs[i].nexSpell == 4)
							return 58;
						else
							return 65;
				}
			case 8133:
				switch(npcs[i].attackType){
					case 0:
						return 60;
					case 1:
						return 40;
					case 2:
						return 45;
				}
			case 8349:
				switch(npcs[i].attackType){
					case 0:
						return 35;
					case 1:
						return 42;
					case 2:
						return 47;
				}
			case 6222:
				if(npcs[i].attackType == 2)
					return 28;
				else
					return 68;
			case 6247:
				return 31;
			case 6260:
				return 36;
		}
		return npcs[i].maxHit;
	}

	public boolean loadAutoSpawn(String fileName){
		String line = "";
		String token = "";
		String token2 = "";
		String token2_2 = "";
		String[] token3 = new String[10];
		boolean EndOfFile = false;
		File f = new File("./" + fileName);
		if(!f.exists()){
			System.out.println(fileName + ": file not found.");
			return false;
		}
		try(BufferedReader characterfile = new BufferedReader(new FileReader(f))){
			line = characterfile.readLine();
			while(EndOfFile == false && line != null){
				line = line.trim();
				int spot = line.indexOf("=");
				if(spot > -1){
					token = line.substring(0, spot);
					token = token.trim();
					token2 = line.substring(spot + 1);
					token2 = token2.trim();
					token2_2 = token2.replaceAll("\t\t", "\t");
					token2_2 = token2_2.replaceAll("\t\t", "\t");
					token2_2 = token2_2.replaceAll("\t\t", "\t");
					token2_2 = token2_2.replaceAll("\t\t", "\t");
					token2_2 = token2_2.replaceAll("\t\t", "\t");
					token3 = token2_2.split("\t");
					int combat = 0;
					int token4 = Integer.parseInt(token3[0]);
					for(int i = 0; i < NpcList.length; i++){
						if(NpcList[i] == null)
							continue;
						if(NpcList[i].npcId == token4){
							combat = NpcList[i].npcCombat;
							break;
						}
					}
					if(token.equals("spawn")){
						if(Integer.parseInt(token3[3]) == -1)
							for(int i = 0; i<200; i++)
								newNPC(token4, Integer.parseInt(token3[1]), Integer.parseInt(token3[2]), i * 4, Integer.parseInt(token3[4]), getNpcListHP(Integer.parseInt(token3[0])), Integer.parseInt(token3[5]), Integer.parseInt(token3[6]), Integer.parseInt(token3[7]), combat);
						else
							newNPC(token4, Integer.parseInt(token3[1]), Integer.parseInt(token3[2]), Integer.parseInt(token3[3]), Integer.parseInt(token3[4]), getNpcListHP(Integer.parseInt(token3[0])), Integer.parseInt(token3[5]), Integer.parseInt(token3[6]), Integer.parseInt(token3[7]), combat);
					}
				}else if(line.equals("[ENDOFSPAWNLIST]"))
					return true;
				line = characterfile.readLine();
			}
		}catch(IOException ioexception){
			ioexception.printStackTrace();
			return false;
		}
		return false;
	}

	public int getNpcListHP(int npcId){
		for(int i = 0; i < maxListedNPCs; i++){
			if(NpcList[i] != null){
				if(NpcList[i].npcId == npcId){
					return NpcList[i].npcHealth;
				}
			}
		}
		return 0;
	}

	public String getNpcListName(int npcId){
		for(int i = 0; i < maxListedNPCs; i++){
			if(NpcList[i] != null){
				if(NpcList[i].npcId == npcId){
					return NpcList[i].npcName;
				}
			}
		}
		return "nothing";
	}

	public boolean loadNPCList(String fileName){
		String line = "";
		String token = "";
		String token2 = "";
		String token2_2 = "";
		String[] token3 = new String[10];
		boolean EndOfFile = false;
		File f = new File("./" + fileName);
		if(!f.exists()){
			System.out.println(fileName + ": file not found.");
			return false;
		}
		try(BufferedReader characterfile = new BufferedReader(new FileReader(f))){
			line = characterfile.readLine();
			while(EndOfFile == false && line != null){
				line = line.trim();
				int spot = line.indexOf("=");
				if(spot > -1){
					token = line.substring(0, spot);
					token = token.trim();
					token2 = line.substring(spot + 1);
					token2 = token2.trim();
					token2_2 = token2.replaceAll("\t\t", "\t");
					token2_2 = token2_2.replaceAll("\t\t", "\t");
					token2_2 = token2_2.replaceAll("\t\t", "\t");
					token2_2 = token2_2.replaceAll("\t\t", "\t");
					token2_2 = token2_2.replaceAll("\t\t", "\t");
					token3 = token2_2.split("\t");
					if(token.equals("npc")){
						newNPCList(Integer.parseInt(token3[0]), token3[1], Integer.parseInt(token3[2]), Integer.parseInt(token3[3]));
					}
				}else if(line.equals("[ENDOFNPCLIST]"))
					return true;
				line = characterfile.readLine();
			}
		}catch(IOException ioexception){
			ioexception.printStackTrace();
			return false;
		}
		return false;
	}

	public void spawnDelrith(Client player){
		player.getPA().createCutScene1(player.mapRegionX, player.mapRegionY, player.heightLevel, 1, 45);
		player.getPA().object(17435, DemonSlayer.ALTAR_X, DemonSlayer.ALTAR_Y, 1, 10);
		final int id = player.getQuestHandler().demonSlayer.delrithId = spawnNpc(player, DemonSlayer.DELRITH_ID[0], DemonSlayer.ALTAR_X, DemonSlayer.ALTAR_Y, player.heightLevel, 0, 750, 43, 450, 220, true, false);
		player.getQuestHandler().demonSlayer.spawned = true;
		NPCHandler.npcs[id].animNumber = 12608;
		NPCHandler.npcs[id].animUpdateRequired = true;
		NPCHandler.npcs[id].isDelrith = true;
		player.changeDelrithAltar(id);
	}
}