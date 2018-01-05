package server.model.players;

import java.util.Collection;
import java.util.Random;

import org.uncommons.maths.random.GaussianGenerator;

import server.Config;
import server.Server;
import server.util.Misc;
import server.model.minigames.CastleWars;
import server.model.minigames.ClanWarsSettings;
import server.model.minigames.Duel;
import server.model.minigames.NexGames;
import server.model.minigames.Zombies;
import server.model.npcs.NPC;
import server.model.npcs.NPCHandler;
import server.model.players.Client;
import server.model.players.skills.Slayer;
import server.model.quests.DemonSlayer;
import server.model.region.RegionManager;

public class CombatAssistant{

	private Client c;
	private Random r = new Random();

	public CombatAssistant(Client Client){
		this.c = Client;
	}

	public int slayerReqs[][] = {{1648, 5}, {1612, 15}, {1643, 45}, {1618, 50}, {1624, 65}, {1610, 75}, {1613, 80}, {1615, 85}, {2783, 90}};

	public boolean goodSlayer(int i){
		int lev = Slayer.levelReq(NPCHandler.npcs[i].npcType);
		if(lev > c.playerLevel[c.playerSlayer]){
			c.sendMessage("You need a slayer level of " + lev + " to harm this NPC.");
			return false;
		}
		return true;
	}

	/**
	 * Attack Npcs
	 */
	public void attackNpc(int i){
		if(i < 0 || i >= NPCHandler.npcs.length)
			return;
		NPC npc = NPCHandler.npcs[i];
		if(npc != null){
			if(c.specHit)
				return;
			if(npc.isDead || npc.MaxHP <= 0){
				c.usingMagic = false;
				c.faceUpdate(0);
				c.npcIndex = 0;
				return;
			}
			if(c.respawnTimer > 0 || c.teleTimer > 0){
				c.npcIndex = 0;
				return;
			}
			if(npc.isWeakDelrith && c.playerEquipment[c.playerWeapon] != DemonSlayer.SILVER_LIGHT){
				c.sendMessage("His evil magic is making him immune to your weapon!");
				resetPlayerAttack();
				return;
			}
			if(npc.underAttackBy > 0 && npc.underAttackBy != c.playerId && !npc.inMulti()){
				c.npcIndex = 0;
				c.sendMessage("This monster is already in combat.");
				return;
			}
			if((c.underAttackBy > 0 || c.underAttackBy2 > 0) && c.underAttackBy2 != i && !c.inMulti()){
				resetPlayerAttack();
				c.sendMessage("I am already under attack.");
				return;
			}
			if(!goodSlayer(i)){
				resetPlayerAttack();
				return;
			}
			if(Zombies.inHouse(c)){
				c.sendMessage("You feel weakened.");
				resetPlayerAttack();
				return;
			}
			c.followId2 = i;
			c.followId = 0;
			if(c.attackTimer <= 0){
				c.getDegrade().handleDegrade();
				boolean usingBow = false;
				boolean usingArrows = false;
				boolean usingCBow = false;
				boolean usingBolts = false;
				boolean usingOtherRangeWeapons = false;
				c.bonusAttack = 0;
				c.rangeItemUsed = 0;
				c.projectileStage = 0;
				c.saveGame();
				if(c.autocasting){
					c.spellId = c.autocastId;
					c.usingMagic = true;
				}
				if(c.spellId > 0){
					c.usingMagic = true;
				}
				c.attackTimer = getAttackDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
				c.specAccuracy = 1.0;
				c.specDamage = 1.0;
				if(!c.usingMagic){
					for(int bowId : c.BOWS){
						if(c.playerEquipment[c.playerWeapon] == bowId){
							usingBow = true;
							for(int arrowId : c.ARROWS){
								if(c.playerEquipment[c.playerArrows] == arrowId){
									usingArrows = true;
									break;
								}
							}
							break;
						}
					}
					if(!usingBow){
						for(int bowId : c.C_BOWS){
							if(c.playerEquipment[c.playerWeapon] == bowId){
								usingCBow = true;
								for(int arrowId : c.BOLTS){
									if(c.playerEquipment[c.playerArrows] == arrowId){
										usingBolts = true;
										break;
									}
								}
								break;
							}
						}
					}
					if(!usingBow && !usingCBow){
						for(int otherRangeId : c.OTHER_RANGE_WEAPONS){
							if(c.playerEquipment[c.playerWeapon] == otherRangeId){
								usingOtherRangeWeapons = true;
								break;
							}
						}
					}
					if(usingCrystalBow() || usingZaryteBow() || c.playerEquipment[c.playerWeapon] == 15241)
						usingBow = true;
				}
				if((armaNpc(i) || npc.npcType == 1160) && !usingCBow && !usingBow && !c.usingMagic && !usingCrystalBow() && !usingZaryteBow() && !usingOtherRangeWeapons){
					resetPlayerAttack();
					c.sendMessage("You can not use melee combat on that NPC.");
					return;
				}
				if((!c.goodDistance(c.getX(), c.getY(), npc.getX(), npc.getY(), 2) && (usingHally() && !usingOtherRangeWeapons && !usingBow && !usingCBow && !c.usingMagic)) || (!c.goodDistance(c.getX(), c.getY(), npc.getX(), npc.getY(), 4) && (usingOtherRangeWeapons && !usingBow && !usingCBow && !c.usingMagic)) || (!c.goodDistance(c.getX(), c.getY(), npc.getX(), npc.getY(), 1) && (!usingOtherRangeWeapons && !usingHally() && !usingBow && !usingCBow && !c.usingMagic)) || ((!c.goodDistance(c.getX(), c.getY(), npc.getX(), npc.getY(), 8) && (usingBow || usingCBow || c.usingMagic)))){
					c.attackTimer = 2;
					return;
				}

				if(!usingCBow && !usingArrows && usingBow && !usingCrystalBow() && !usingZaryteBow() && c.playerEquipment[c.playerWeapon] != 15241 && !c.usingMagic){
					c.sendMessage("You have run out of arrows!");
					c.stopMovement();
					c.npcIndex = 0;
					return;
				}
				if(correctBowAndArrows() < c.playerEquipment[c.playerArrows] && Config.CORRECT_ARROWS && usingBow && !usingCrystalBow() && !usingZaryteBow() && c.playerEquipment[c.playerWeapon] != 15241 && (c.playerEquipment[c.playerWeapon] != 9185 && c.playerEquipment[c.playerWeapon] != 18357)){
					c.sendMessage("You can't use " + c.getItems().getItemName(c.playerEquipment[c.playerArrows]).toLowerCase() + "s with a " + c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase() + ".");
					c.stopMovement();
					c.npcIndex = 0;
					return;
				}
				if(usingCBow && !properBolts() && c.playerEquipment[c.playerWeapon] != 15241){
					c.sendMessage("You must use bolts with a crossbow.");
					c.stopMovement();
					resetPlayerAttack();
					return;
				}
				if(c.playerEquipment[c.playerWeapon] == 15241 && c.playerEquipment[c.playerArrows] != 15243){
					c.sendMessage("You must use hand cannon shots with a hand cannon.");
					c.stopMovement();
					resetPlayerAttack();
					return;
				}else if(c.playerEquipment[c.playerArrows] == 15243 && c.playerEquipment[c.playerWeapon] == 15241){
					usingBolts = true;
					usingCBow = true;
				}

				if(usingBow || usingCBow || c.usingMagic || usingOtherRangeWeapons || (c.goodDistance(c.getX(), c.getY(), NPCHandler.npcs[i].getX(), NPCHandler.npcs[i].getY(), 2) && usingHally())){
					c.stopMovement();
					c.followId = c.followId2 = 0;
				}

				if(!checkMagicReqs(c.spellId)){
					c.stopMovement();
					c.npcIndex = 0;
					return;
				}

				c.faceUpdate(i);
				// c.specAccuracy = 1.0;
				// c.specDamage = 1.0;
				npc.underAttackBy = c.playerId;
				npc.lastDamageTaken = System.currentTimeMillis();
				if(c.usingSpecial && !c.usingMagic){
					if(checkSpecAmount(c.playerEquipment[c.playerWeapon])){
						c.lastWeaponUsed = c.playerEquipment[c.playerWeapon];
						c.lastArrowUsed = c.playerEquipment[c.playerArrows];
						activateSpecial(c.playerEquipment[c.playerWeapon], i);
						return;
					}else{
						c.sendMessage("You don't have the required special energy to use this attack.");
						c.usingSpecial = false;
						c.getItems().updateSpecialBar();
						c.npcIndex = 0;
						return;
					}
				}
				c.specMaxHitIncrease = 0;
				if(!c.usingMagic){
					c.startAnimation(getWepAnim(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase()));
				}else{
					c.startAnimation(getMageAnim());
				}
				c.lastWeaponUsed = c.playerEquipment[c.playerWeapon];
				c.lastArrowUsed = c.playerEquipment[c.playerArrows];
				if(!usingBow && !c.usingMagic && !usingOtherRangeWeapons && !usingCBow){ // melee hit delay
					c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
					c.projectileStage = 0;
					c.oldNpcIndex = i;
					if(NPCHandler.npcs[i].invulnerableTick > -1 || NPCHandler.npcs[i].npcType == 2001)
						NPCHandler.npcs[i].gfx0(2319);
				}

				if((usingBow && (usingArrows || usingCrystalBow() || usingZaryteBow()) && !usingOtherRangeWeapons && !c.usingMagic) || (usingCBow && usingBolts)){ // range hit delay
					if(usingCBow)
						c.usingBow = true;
					if(c.fightMode == 2)
						c.attackTimer--;
					c.lastArrowUsed = ((usingCrystalBow() || usingZaryteBow()) ? -1 : c.playerEquipment[c.playerArrows]);
					c.lastWeaponUsed = c.playerEquipment[c.playerWeapon];
					if(usingCrystalBow()){
						c.rangeItemUsed = c.playerEquipment[c.playerWeapon];
						if(c.rangeItemUsed != 20097)
							c.crystalBowArrowCount++;
						c.lastArrowUsed = 0;
					}else if(!usingZaryteBow()){
						c.rangeItemUsed = c.playerEquipment[c.playerArrows];
						//c.getItems().deleteArrow();
					}else
						c.rangeItemUsed = c.playerEquipment[c.playerWeapon];
					if(c.playerEquipment[c.playerWeapon] != 15241)
						c.gfx100(getRangeStartGFX());
					else
						c.gfx0(getRangeStartGFX());
					c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
					c.projectileStage = 1;
					c.oldNpcIndex = i;
					fireProjectileNpc();
					if(NPCHandler.npcs[i].invulnerableTick > -1 || NPCHandler.npcs[i].npcType == 2001)
						NPCHandler.npcs[i].gfx0(2319);
				}

				if(usingOtherRangeWeapons && !c.usingMagic && !usingBow && !usingCBow){ // knives, darts, etc hit delay
					c.rangeItemUsed = c.playerEquipment[c.playerWeapon];
					c.gfx100(getRangeStartGFX());
					c.lastArrowUsed = 0;
					c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
					c.projectileStage = 1;
					c.lastWeaponUsed = c.playerEquipment[c.playerWeapon];
					c.oldNpcIndex = i;
					if(c.fightMode == 2)
						c.attackTimer--;
					fireProjectileNpc();
					if(NPCHandler.npcs[i].invulnerableTick > -1 || NPCHandler.npcs[i].npcType == 2001)
						NPCHandler.npcs[i].gfx0(2319);
				}

				if(c.usingMagic){ // magic hit delay
					if(npc.isWeakDelrith)
						return;
					int pX = c.getX();
					int pY = c.getY();
					int nX = npc.getX();
					int nY = npc.getY();
					int offX = (pY - nY) * -1;
					int offY = (pX - nX) * -1;
					c.castingMagic = true;
					c.projectileStage = 2;
					if(c.MAGIC_SPELLS[c.spellId][3] > 0){
						if(getStartGfxHeight() == 100){
							c.gfx100(c.MAGIC_SPELLS[c.spellId][3]);
						}else{
							c.gfx0(c.MAGIC_SPELLS[c.spellId][3]);
						}
					}
					if(c.MAGIC_SPELLS[c.spellId][4] > 0){
						c.getPA().createPlayersProjectile(pX, pY, offX, offY, 50, 78, c.MAGIC_SPELLS[c.spellId][4], getStartHeight(), getEndHeight(), i + 1, 50);
					}
					c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
					c.oldNpcIndex = i;
					c.oldSpellId = c.spellId;
					c.spellId = 0;
					if(!c.autocasting)
						c.npcIndex = 0;
					if(NPCHandler.npcs[i].invulnerableTick > -1 && NPCHandler.npcs[i].npcType == 2001)
						NPCHandler.npcs[i].gfx0(2319);
				}

				if(usingBow && Config.CRYSTAL_BOW_DEGRADES){ // crystal bow degrading
					if(c.playerEquipment[c.playerWeapon] == 4212){ // new crystal bow becomes full bow on the first shot
						c.getItems().wearItem(4214, 1, 3);
					}

					if(c.crystalBowArrowCount >= 250){
						c.crystalBowArrowCount = 0;
						if(c.playerEquipment[c.playerWeapon] == 4223){ // 1/10 bow
							c.getItems().wearItem(-1, 1, 3);
							c.sendMessage("Your crystal bow has fully degraded.");
							if(!c.inventory.addItem(4207, 1, -1))
								Server.itemHandler.createGroundItem(c, 4207, c.getX(), c.getY(), c.heightLevel, 1, c.getId());
						}else{
							c.getItems().wearItem(++c.playerEquipment[c.playerWeapon], 1, 3);
							c.sendMessage("Your crystal bow degrades.");
						}
					}
					if(NPCHandler.npcs[i].invulnerableTick > -1 || NPCHandler.npcs[i].npcType == 2001)
						NPCHandler.npcs[i].gfx0(2319);
				}
			}
		}
	}

	public void delayedHit(int i){ // npc hit delay
		if(NPCHandler.npcs[i] != null){
			if(NPCHandler.npcs[i].isDead){
				c.npcIndex = 0;
				return;
			}
			if(!NPCHandler.npcs[i].npcObject)
				NPCHandler.npcs[i].facePlayer(c.playerId);

			if(NPCHandler.npcs[i].underAttackBy > 0 && Server.npcHandler.getsPulled(i)){
				NPCHandler.npcs[i].killerId = c.playerId;
			}else if(NPCHandler.npcs[i].underAttackBy < 0 && !Server.npcHandler.getsPulled(i)){
				NPCHandler.npcs[i].killerId = c.playerId;
			}

			if(NPCHandler.npcs[i].attackTimer <= 3 || NPCHandler.npcs[i].attackTimer == 0 && NPCHandler.npcs[i].underAttackBy == 0 && !c.castingMagic){ // block
																																						// animation
				int emote = NPCHandler.getBlockEmote(i);
				if(emote > 0)
					Server.npcHandler.startAnimation(emote, i);
			}
			if(c.playerEquipment[c.playerWeapon] == 15241)
				cannonExplosion();
			if(c.specHit)
				c.specHit = false;
			c.lastNpcAttacked = i;
			
			if(c.projectileStage == 0){ // melee hit damage
				int dam = 0;
				if(c.playerEquipment[c.playerAmulet] == 17291 && c.bloodNecklaceTime <= Misc.currentTimeSeconds()){
					c.bloodNecklaceTime = Misc.currentTimeSeconds() + 15;
					dam = Misc.random(3);
					if(NPCHandler.npcs[i].HP - dam <= 0)
						dam = (NPCHandler.npcs[i].HP - dam);
					c.playerLevel[c.playerHitpoints] += dam;
					c.getPA().refreshSkill(c.playerHitpoints);
					if(c.playerLevel[c.playerHitpoints] > c.getLevelForXP(c.playerXP[c.playerHitpoints]))
						c.playerLevel[c.playerHitpoints] = c.getLevelForXP(c.playerXP[c.playerHitpoints]);
					NPCHandler.npcs[i].HP -= dam;
					c.totalDamageDealt += dam;
				}
				applyNpcMeleeDamage(i, 1);
				/*if(NPCHandler.npcs[i].npcType >= 3748 && NPCHandler.npcs[i].npcType <= 3751 && Misc.random(100) <= 25){
					Server.pestControl.splatterExplosion(i);
					return;
				}*/
				if(c.doubleHit || c.usingClaws){
					applyNpcMeleeDamage(i, 2);
				}else
					NPCHandler.npcs[i].hitDiff2 = dam;
				if(NPCHandler.npcs[i].npcType == 2000)
					NPCHandler.npcs[i].handleNexStage();
			}

			if(!c.castingMagic && c.projectileStage > 0){ // range hit damage
				if(c.quincyStage == 1){
					if(c.playerEquipment[c.playerWeapon] == 20097)
						quincySpec();
					c.quincyStage = 0;
				}
				if(usingCrystalBow() || usingZaryteBow())
					c.lastArrowUsed = -1;
				int damage = NPCHandler.npcs[i].npcType == 2001 ? 0 : Misc.random(rangeMaxHit());
				int damage2 = -1;
				if(c.lastWeaponUsed == 11235 || c.bowSpecShot == 1)
					damage2 = Misc.random(rangeMaxHit());
				boolean ignoreDef = false;
				if(Misc.random(5) == 1 && c.lastArrowUsed == 9243){
					ignoreDef = true;
					NPCHandler.npcs[i].gfx0(758);
				}
				if(NPCHandler.npcs[i].npcType != 2000 || !NexGames.hasFullPernix(c)){
					if(Misc.random(NPCHandler.npcs[i].defence) > Misc.random(10 + calculateRangeAttack()) && !ignoreDef){
						damage = 0;
					}else if(NPCHandler.npcs[i].npcType == 2881 || NPCHandler.npcs[i].npcType == 2883 && !ignoreDef){
						damage = 0;
					}else if(NPCHandler.npcs[i].npcType == 1158)
						damage = 0;
				}
				if(c.prayerActive[44]){
					int heal = (int)Math.floor(damage * 0.2);
					int temp = c.getLevelForXP(c.playerXP[c.playerHitpoints]) + c.zarosModifier;
					if(c.playerLevel[c.playerHitpoints] + heal > temp)
						heal = temp >= c.playerLevel[c.playerHitpoints] ? temp - c.playerLevel[c.playerHitpoints] : 0;
					c.playerLevel[c.playerHitpoints] += heal;
					c.getPA().refreshSkill(c.playerHitpoints);
					NPCHandler.npcs[i].gfx0(2264);
				}
				if(c.usingQuincySpec){
					NPCHandler.npcs[i].gfx0(728);
					c.usingQuincySpec = false;
				}
				if(damage > 0 && NPCHandler.npcs[i].npcType == 2000 && usingZaryteBow()){
					NPCHandler.npcs[i].gfx0(377);
					int heal = (int)(damage * 0.25);
					if(c.playerLevel[3] + heal >= c.getPA().getLevelForXP(c.playerXP[3]))
						c.playerLevel[3] = c.getPA().getLevelForXP(c.playerXP[3]);
					else
						c.playerLevel[3] += heal;
					c.getPA().refreshSkill(3);
				}
				if(Misc.random(4) == 1 && c.lastArrowUsed == 9242 && damage > 0){
					if(NPCHandler.npcs[i].npcType != 2001){
						NPCHandler.npcs[i].gfx0(754);
						damage = NPCHandler.npcs[i].HP / 5;
					}else
						c.sendMessage("That fiend is immune to the effects of your bolts!");
					c.dealDamage(c.playerLevel[3] / 10);
					c.gfx0(754);
				}

				if(c.lastWeaponUsed == 11235 || c.bowSpecShot == 1){
					if(Misc.random(NPCHandler.npcs[i].defence) > Misc.random(10 + calculateRangeAttack()))
						damage2 = 0;
				}
				if(c.dbowSpec){
					NPCHandler.npcs[i].gfx100(1100);
					if(damage < 8)
						damage = 8;
					if(damage2 < 8)
						damage2 = 8;
					c.dbowSpec = false;
				}
				if(NPCHandler.npcs[i].npcType == 1158)
					damage2 = 0;
				if(damage > 0 && Misc.random(5) == 1 && c.lastArrowUsed == 9244){
					damage *= 1.45;
					NPCHandler.npcs[i].gfx0(756);
				}

				if(NPCHandler.npcs[i].HP - damage < 0){
					damage = NPCHandler.npcs[i].HP;
				}
				if(NPCHandler.npcs[i].HP - damage <= 0 && damage2 > 0){
					damage2 = 0;
				}
				if(c.playerEquipment[c.playerAmulet] == 17291 && c.bloodNecklaceTime <= Misc.currentTimeSeconds()){
					c.bloodNecklaceTime = Misc.currentTimeSeconds() + 15;
					int dam = Misc.random(3);
					if((NPCHandler.npcs[i].HP - damage) - damage2 <= 0)
						dam = 0;
					if(((NPCHandler.npcs[i].HP - damage) - damage2) - dam <= 0)
						dam = ((NPCHandler.npcs[i].HP - damage) - damage2);
					c.playerLevel[c.playerHitpoints] += dam;
					c.getPA().refreshSkill(c.playerHitpoints);
					if(c.playerLevel[c.playerHitpoints] > c.getLevelForXP(c.playerXP[c.playerHitpoints]))
						c.playerLevel[c.playerHitpoints] = c.getLevelForXP(c.playerXP[c.playerHitpoints]);
					if(damage2 > 0){
						NPCHandler.npcs[i].HP -= damage2;
						c.totalDamageDealt += damage2;
					}else
						damage2 = dam;
				}
				if(c.fightMode == 3){
					c.getPA().addSkillXP((damage * Config.RANGE_EXP_RATE / 3), 4);
					c.getPA().addSkillXP((damage * Config.RANGE_EXP_RATE / 3), 1);
					c.getPA().addSkillXP((damage * Config.RANGE_EXP_RATE / 3), 3);
					c.getPA().refreshSkill(1);
					c.getPA().refreshSkill(3);
					c.getPA().refreshSkill(4);
				}else{
					c.getPA().addSkillXP((damage * Config.RANGE_EXP_RATE), 4);
					c.getPA().addSkillXP((damage * Config.RANGE_EXP_RATE / 3), 3);
					c.getPA().refreshSkill(3);
					c.getPA().refreshSkill(4);
				}
				/*if(damage > 0){
					if(Server.pestControl.isPestControl(NPCHandler.npcs[i].npcType)){
						c.pcDamage += damage;
					}
				}*/
				boolean dropArrows = true;
				boolean usingOtherRangeWeapons = false;
				for(int otherRangeId : c.OTHER_RANGE_WEAPONS){
					if(c.playerEquipment[c.playerWeapon] == otherRangeId){
						usingOtherRangeWeapons = true;
						break;
					}
				}
				for(int noArrowId : c.NO_ARROW_DROP){
					if(c.lastWeaponUsed == noArrowId){
						dropArrows = false;
						break;
					}
				}
				if(c.lastArrowUsed == 19157)
					dropArrows = false;
				if(dropArrows && !usingOtherRangeWeapons){
					c.rangeItemUsed = c.playerEquipment[c.playerArrows];
					if(usingDbow() && c.getItems().deleteArrow())
						c.getItems().dropArrowNpc();
					if(c.getItems().deleteArrow())
						c.getItems().dropArrowNpc();
					else{
						c.sendMessage("You must use bolts with a crossbow.");
						resetPlayerAttack();
						return;
					}
				}else if(dropArrows && usingOtherRangeWeapons){
					c.rangeItemUsed = c.playerEquipment[c.playerWeapon];
					if(c.getItems().deleteEquipment())
						c.getItems().dropArrowNpc();
					else{
						resetPlayerAttack();
						return;
					}
				}else if(usingOtherRangeWeapons && !dropArrows){
					if(!c.getItems().deleteEquipment()){
						resetPlayerAttack();
						return;
					}
				}else if(c.lastArrowUsed == 19157 || c.lastWeaponUsed == 15241)
					c.getItems().deleteLostArrow();
				if(NPCHandler.npcs[i].invulnerableTick > -1 && NPCHandler.npcs[i].npcType == 2001){
					damage = 0;
					c.sendMessage("This guy seems to be immune to your attack!");
					NPCHandler.npcs[i].hitDiff = 0;
					NPCHandler.npcs[i].hitUpdateRequired = true;
					return;
				}
				if(c.slayerTask != null && c.slayerTask.monster == NPCHandler.npcs[i].npcType && c.playerEquipment[c.playerHat] == 15492){ // Full slayer helm
					damage *= 1.1;
				}
				if(damage > 0 && c.activateMJav){
					c.activateMJav = false;
					NPCHandler.npcs[i].mJavHit += damage;
					NPCHandler.npcs[i].mJavTime = Misc.currentTimeSeconds();
				}
				/*if(NPCHandler.npcs[i].npcType >= 3748 && NPCHandler.npcs[i].npcType <= 3751 && Misc.random(100) <= 25){
					Server.pestControl.splatterExplosion(i);
					return;
				}*/
				if(c.guthixBowSpec){
					damage2 = ((int)(damage * 0.5));
					if(NPCHandler.npcs[i].HP - damage <= 0 && damage2 > 0)
						damage2 = 0;
					else{
						c.playerLevel[c.playerHitpoints] += damage;
						if(c.playerLevel[c.playerHitpoints] > c.getLevelForXP(c.playerXP[c.playerHitpoints]) + c.zarosModifier)
							c.playerLevel[c.playerHitpoints] = c.getLevelForXP(c.playerXP[c.playerHitpoints]) + c.zarosModifier;
						c.getPA().refreshSkill(c.playerHitpoints);
					}
					NPCHandler.npcs[i].gfx0(127);
					c.guthixBowSpec = false;
				}
				NPCHandler.npcs[i].underAttack = true;
				NPCHandler.npcs[i].hitDiff = damage;
				NPCHandler.npcs[i].HP -= damage;
				if(NPCHandler.npcs[i].npcType == 2000)
					NPCHandler.npcs[i].handleNexStage();
				if(damage2 > -1){
					NPCHandler.npcs[i].hitDiff2 = damage2;
					NPCHandler.npcs[i].HP -= damage2;
					c.totalDamageDealt += damage2;
				}
				if(c.killingNpcIndex != c.oldNpcIndex){
					c.totalDamageDealt = 0;
				}
				c.killingNpcIndex = c.oldNpcIndex;
				c.totalDamageDealt += damage;
				NPCHandler.npcs[i].hitUpdateRequired = true;
				if(damage2 > -1)
					NPCHandler.npcs[i].hitUpdateRequired2 = true;
				NPCHandler.npcs[i].updateRequired = true;
			}else if(c.projectileStage > 0){ // magic hit damage
				int damage = 0;
				if(c.MAGIC_SPELLS[c.oldSpellId][6] == -1){
					int minHit = (int)((((c.getLevelForXP(c.playerXP[c.playerMagic]) - 82) * 0.5) + 2.5) + (c.playerEquipment[c.playerWeapon] == 21777 ? 5 : 0));
					int maxHit = (int)((((c.getLevelForXP(c.playerXP[c.playerMagic]) - 82) * 0.5) + 18.5) + (c.playerEquipment[c.playerWeapon] == 21777 ? 5 : 0));
					damage = Misc.random_range(minHit, maxHit);
				}else
					damage = Misc.random(c.MAGIC_SPELLS[c.oldSpellId][6]);
				if(godSpells()){
					if(System.currentTimeMillis() - c.godSpellDelay < Config.GOD_SPELL_CHARGE){
						damage += Misc.random(10);
					}
				}
				boolean magicFailed = false;
				// c.npcIndex = 0;
				int bonusAttack = getBonusAttack(i);
				if(Misc.random(NPCHandler.npcs[i].defence) > 10 + Misc.random(mageAtk()) + bonusAttack){
					damage = 0;
					magicFailed = true;
				}else if(NPCHandler.npcs[i].npcType == 2881 || NPCHandler.npcs[i].npcType == 2882 || NPCHandler.npcs[i].npcType == 1158){
					damage = 0;
					magicFailed = true;
				}
				if(c.prayerActive[44]){
					int heal = (int)Math.floor(damage * 0.2);
					int temp = c.getLevelForXP(c.playerXP[c.playerHitpoints]) + c.zarosModifier;
					if(c.playerLevel[c.playerHitpoints] + heal > temp)
						heal = temp >= c.playerLevel[c.playerHitpoints] ? temp - c.playerLevel[c.playerHitpoints] : 0;
					c.playerLevel[c.playerHitpoints] += heal;
					c.getPA().refreshSkill(c.playerHitpoints);
					NPCHandler.npcs[i].gfx0(2264);
				}
				if(c.slayerTask != null && c.slayerTask.monster == NPCHandler.npcs[i].npcType && c.playerEquipment[c.playerHat] == 15492) // Full slayer helm
					damage *= 1.1;
				if(c.playerEquipment[c.playerWeapon] == 24201)
					magicFailed = false;
				if(NPCHandler.npcs[i].HP - damage < 0){
					damage = NPCHandler.npcs[i].HP;
				}

				c.getPA().addSkillXP((c.MAGIC_SPELLS[c.oldSpellId][7] + damage * Config.MAGIC_EXP_RATE), 6);
				c.getPA().addSkillXP((c.MAGIC_SPELLS[c.oldSpellId][7] + damage * Config.MAGIC_EXP_RATE / 3), 3);
				c.getPA().refreshSkill(3);
				c.getPA().refreshSkill(6);
				/*if(damage > 0){
					if(Server.pestControl.isPestControl(NPCHandler.npcs[i].npcType)){
						c.pcDamage += damage;
					}
				}*/
				if(NPCHandler.npcs[i].invulnerableTick > -1 && NPCHandler.npcs[i].npcType == 2001){
					damage = 0;
					c.sendMessage("This guy seems to be immune to your attack!");
					NPCHandler.npcs[i].hitDiff = 0;
					NPCHandler.npcs[i].hitUpdateRequired = true;
					return;
				}
				if(getEndGfxHeight() == 100 && !magicFailed){ // end GFX
					NPCHandler.npcs[i].gfx100(c.MAGIC_SPELLS[c.oldSpellId][5]);
				}else if(!magicFailed){
					NPCHandler.npcs[i].gfx0(c.MAGIC_SPELLS[c.oldSpellId][5]);
				}

				if(magicFailed){
					NPCHandler.npcs[i].gfx100(85);
				}
				if(!magicFailed){
					if(NPCHandler.npcs[i].npcType == 2000 && c.playerEquipment[c.playerWeapon] == 4675 && c.playerLevel[c.playerPrayer] < c.getLevelForXP(c.playerXP[c.playerPrayer])){
						c.playerLevel[c.playerPrayer]++;
						c.getPA().refreshSkill(c.playerPrayer);
					}
					if(NPCHandler.npcs[i].npcType == 2000 && NexGames.hasFullVirtus(c))
						damage *= 1.25;
					int freezeDelay = getFreezeTime();// freeze
					if(freezeDelay > 0 && NPCHandler.npcs[i].freezeTimer == 0){
						NPCHandler.npcs[i].freezeTimer = freezeDelay;
					}
					switch(c.MAGIC_SPELLS[c.oldSpellId][0]){
						case 12435:
							if(magicFailed)
								break;
							NPCHandler.npcs[i].defence -= NPCHandler.npcs[i].defence == 0 ? 0 : 1;
							break;
						case 12901:
						case 12919: // blood spells
						case 12911:
						case 12929:
							int heal = (int)(damage * 0.25);
							if(c.playerLevel[3] + heal >= c.getPA().getLevelForXP(c.playerXP[3])){
								c.playerLevel[3] = c.getPA().getLevelForXP(c.playerXP[3]);
							}else{
								c.playerLevel[3] += heal;
							}
							c.getPA().refreshSkill(3);
							break;
					}

				}
				NPCHandler.npcs[i].underAttack = true;
				/*if(NPCHandler.npcs[i].npcType >= 3748 && NPCHandler.npcs[i].npcType <= 3751 && Misc.random(100) <= 25){
					Server.pestControl.splatterExplosion(i);
					return;
				}*/
				if(c.playerEquipment[c.playerWeapon] == 24201)
					damage = NPCHandler.npcs[i].HP;
				int dam = 0;
				if(c.playerEquipment[c.playerAmulet] == 17291 && c.bloodNecklaceTime <= Misc.currentTimeSeconds()){
					c.bloodNecklaceTime = Misc.currentTimeSeconds() + 15;
					dam = Misc.random(3);
					if(NPCHandler.npcs[i].HP - damage <= 0)
						dam = 0;
					if((NPCHandler.npcs[i].HP - damage) - dam <= 0)
						dam = (NPCHandler.npcs[i].HP - damage);
					c.playerLevel[c.playerHitpoints] += dam;
					c.getPA().refreshSkill(c.playerHitpoints);
					if(c.playerLevel[c.playerHitpoints] > c.getLevelForXP(c.playerXP[c.playerHitpoints]))
						c.playerLevel[c.playerHitpoints] = c.getLevelForXP(c.playerXP[c.playerHitpoints]);
					NPCHandler.npcs[i].HP -= dam;
					NPCHandler.npcs[i].hitDiff2 = dam;
					c.totalDamageDealt += dam;
				}
				if(c.MAGIC_SPELLS[c.oldSpellId][6] != 0){
					NPCHandler.npcs[i].hitDiff = damage;
					NPCHandler.npcs[i].HP -= damage;
					NPCHandler.npcs[i].hitUpdateRequired = true;
					c.totalDamageDealt += damage;
				}
				if(NPCHandler.npcs[i].npcType == 2000)
					NPCHandler.npcs[i].handleNexStage();
				c.killingNpcIndex = c.oldNpcIndex;
				NPCHandler.npcs[i].updateRequired = true;
				c.usingMagic = false;
				c.castingMagic = false;
				c.oldSpellId = 0;
			}
		}

		if(c.bowSpecShot <= 0){
			c.oldNpcIndex = 0;
			c.projectileStage = 0;
			c.doubleHit = false;
			c.lastWeaponUsed = 0;
			c.bowSpecShot = 0;
		}
		if(c.bowSpecShot >= 2){
			c.bowSpecShot = 0;
			// c.attackTimer =
			// getAttackDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
		}
		if(c.bowSpecShot == 1){
			fireProjectileNpc();
			c.hitDelay = 2;
			c.bowSpecShot = 0;
		}
	}

	public void applyNpcMeleeDamage(int i, int damageMask){
		int damage = Misc.random(calculateMeleeMaxHit(i, true));
		if(c.ssSpec){
			c.ssSpec = false;
			damageMask = 2;
			int specDamage = damage > 0 ? Misc.random_range(5, 20) : 0;
			boolean magicFailed = specDamage == 0;
			if(c.prayerActive[44]){
				int heal = (int)Math.floor(specDamage * 0.2);
				int temp = c.getLevelForXP(c.playerXP[c.playerHitpoints]) + c.zarosModifier;
				if(c.playerLevel[c.playerHitpoints] + heal > temp)
					heal = temp >= c.playerLevel[c.playerHitpoints] ? temp - c.playerLevel[c.playerHitpoints] : 0;
				c.playerLevel[c.playerHitpoints] += heal;
				c.getPA().refreshSkill(c.playerHitpoints);
				NPCHandler.npcs[i].gfx0(2264);
			}
			if(NPCHandler.npcs[i].HP - specDamage < 0)
				specDamage = NPCHandler.npcs[i].HP;

			c.getPA().addSkillXP((specDamage * Config.MAGIC_EXP_RATE), 6);
			c.getPA().addSkillXP((specDamage * Config.MAGIC_EXP_RATE / 3), 3);
			c.getPA().refreshSkill(3);
			c.getPA().refreshSkill(6);
			/*if(damage > 0){
				if(Server.pestControl.isPestControl(NPCHandler.npcs[i].npcType)){
					c.pcDamage += damage;
				}
			}*/
			if(NPCHandler.npcs[i].invulnerableTick > -1 && NPCHandler.npcs[i].npcType == 2001){
				specDamage = 0;
				c.sendMessage("This guy seems to be immune to your attack!");
				NPCHandler.npcs[i].hitDiff = 0;
				NPCHandler.npcs[i].hitUpdateRequired = true;
				return;
			}
			if(magicFailed)
				NPCHandler.npcs[i].gfx100(85);
			NPCHandler.npcs[i].underAttack = true;
			NPCHandler.npcs[i].hitDiff = specDamage;
			NPCHandler.npcs[i].HP -= specDamage;
			NPCHandler.npcs[i].hitUpdateRequired = true;
			c.totalDamageDealt += specDamage;
			c.killingNpcIndex = c.oldNpcIndex;
			NPCHandler.npcs[i].updateRequired = true;
			c.usingMagic = false;
			c.castingMagic = false;
			c.oldSpellId = 0;
		}
		if(c.prayerActive[44]){
			int temp = c.getLevelForXP(c.playerXP[c.playerHitpoints]) + c.zarosModifier;
			int heal = (int)Math.floor(damage * 0.2);
			if(c.playerLevel[c.playerHitpoints] + heal > temp)
				heal = temp >= c.playerLevel[c.playerHitpoints] ? temp - c.playerLevel[c.playerHitpoints] : 0;
			c.playerLevel[c.playerHitpoints] += heal;
			c.getPA().refreshSkill(c.playerHitpoints);
			NPCHandler.npcs[i].gfx0(2264);
		}
		if(c.slayerTask != null && c.slayerTask.monster == NPCHandler.npcs[i].npcType && c.playerEquipment[c.playerHat] == 15492) // Full slayer helm
			damage *= 1.1;
		boolean guthansEffect = c.getPA().fullGuthans() && Misc.random(4) == 1;
		if(c.fightMode == 3){
			c.getPA().addSkillXP((damage * Config.MELEE_EXP_RATE / 3), 0);
			c.getPA().addSkillXP((damage * Config.MELEE_EXP_RATE / 3), 1);
			c.getPA().addSkillXP((damage * Config.MELEE_EXP_RATE / 3), 2);
			c.getPA().addSkillXP((damage * Config.MELEE_EXP_RATE / 3), 3);
			c.getPA().refreshSkill(0);
			c.getPA().refreshSkill(1);
			c.getPA().refreshSkill(2);
			c.getPA().refreshSkill(3);
		}else{
			c.getPA().addSkillXP((damage * Config.MELEE_EXP_RATE), c.fightMode);
			c.getPA().addSkillXP((damage * Config.MELEE_EXP_RATE / 3), 3);
			c.getPA().refreshSkill(c.fightMode);
			c.getPA().refreshSkill(3);
		}
		damage = NPCHandler.npcs[i].HP < damage ? NPCHandler.npcs[i].HP : damage;
		if(NPCHandler.npcs[i].invulnerableTick > -1 && NPCHandler.npcs[i].npcType == 2001){
			damage = 0;
			c.sendMessage("This guy seems to be immune to your attack!");
			NPCHandler.npcs[i].hitDiff = 0;
			NPCHandler.npcs[i].hitUpdateRequired = true;
			return;
		}
		if(NPCHandler.npcs[i].npcType == 2001 && c.playerEquipment[c.playerWeapon] != 19784)
			damage = 0;
		if(damage > 0 && guthansEffect){
			c.playerLevel[3] += damage;
			if(c.playerLevel[3] > c.getLevelForXP(c.playerXP[3]))
				c.playerLevel[3] = c.getLevelForXP(c.playerXP[3]);
			c.getPA().refreshSkill(3);
			NPCHandler.npcs[i].gfx0(398);
		}
		NPCHandler.npcs[i].underAttack = true;
		// NPCHandler.npcs[i].killerId = c.playerId;
		c.killingNpcIndex = c.npcIndex;
		c.lastNpcAttacked = i;
		switch(c.specEffect){
			case 2:
				if(damage > 0){
					if(NPCHandler.npcs[i].freezeTimer <= 0)
						NPCHandler.npcs[i].freezeTimer = 30;
					NPCHandler.npcs[i].gfx0(369);
					c.sendMessage("You freeze your enemy.");
				}
				break;
			case 3:
				if(damage > 0){
					NPCHandler.npcs[i].defence -= damage;
					if(NPCHandler.npcs[i].defence < 1)
						NPCHandler.npcs[i].defence = 1;
				}
				break;
			case 4:
				if(damage > 0){
					if(c.playerLevel[3] + damage > c.getLevelForXP(c.playerXP[3]))
						if(c.playerLevel[3] > c.getLevelForXP(c.playerXP[3]))
							;
						else
							c.playerLevel[3] = c.getLevelForXP(c.playerXP[3]);
					else
						c.playerLevel[3] += damage;
					c.getPA().refreshSkill(3);
				}
				break;
			case 5:
				c.clawDelay = 2;
				break;
			case 6:
				if(damage > 0)
					NPCHandler.npcs[i].defence -= (int)Math.round(1 + (0.3 * NPCHandler.npcs[i].defence));
				break;
		}
		c.specEffect = 0;
		/*if(damage > 0){
			if(Server.pestControl.isPestControl(NPCHandler.npcs[i].npcType)){
				c.pcDamage += damage;
			}
		}*/
		switch(damageMask){
			case 1:
				NPCHandler.npcs[i].hitDiff = damage;
				NPCHandler.npcs[i].HP -= damage;
				c.totalDamageDealt += damage;
				NPCHandler.npcs[i].hitUpdateRequired = true;
				NPCHandler.npcs[i].updateRequired = true;
				break;
			case 2:
				NPCHandler.npcs[i].hitDiff2 = damage;
				NPCHandler.npcs[i].HP -= damage;
				c.totalDamageDealt += damage;
				NPCHandler.npcs[i].hitUpdateRequired2 = true;
				NPCHandler.npcs[i].updateRequired = true;
				c.usingClaws = c.doubleHit = false;
				break;
		}
	}

	public void fireProjectileNpc(){
		if(c.oldNpcIndex > 0){
			if(NPCHandler.npcs[c.oldNpcIndex] != null){
				c.projectileStage = 2;
				int pX = c.getX();
				int pY = c.getY();
				int nX = NPCHandler.npcs[c.oldNpcIndex].getX();
				int nY = NPCHandler.npcs[c.oldNpcIndex].getY();
				int offX = (pY - nY) * -1;
				int offY = (pX - nX) * -1;
				c.getPA().createPlayersProjectile(pX, pY, offX, offY, 50, getProjectileSpeed(), getRangeProjectileGFX(), 43, 31, c.oldNpcIndex + 1, getStartDelay());
				if(usingDbow())
					c.getPA().createPlayersProjectile2(pX, pY, offX, offY, 50, getProjectileSpeed(), getRangeProjectileGFX(), 60, 31, c.oldNpcIndex + 1, getStartDelay(), 35);
			}
		}
	}

	/**
	 * Attack Players, same as npc tbh xD
	 **/

	public void attackPlayer(int i){
		if(PlayerHandler.players[i] != null){
			if(PlayerHandler.players[i].isDead){
				resetPlayerAttack();
				return;
			}
			if(c.respawnTimer > 0 || PlayerHandler.players[i].respawnTimer > 0 || c.teleTimer > 0 || c.hitDelay > 1){
				resetPlayerAttack();
				return;
			}
			/*
			 * if (c.teleTimer > 0 || PlayerHandler.players[i].teleTimer > 0) {
			 * resetPlayerAttack(); return; }
			 */
			try{
				if(!c.getCombat().checkReqs()){
					c.playerIndex = 0;
					return;
				}
			}catch(Exception e){
				e.printStackTrace();
				return;
			}
			if(c.specHit)
				return;
			c.getDegrade().handleDegrade();
			Client o = (Client)PlayerHandler.players[i];
			if(c.prayerActive[30]){
				if(c.sapTicks[3] > 0){
					c.sapTicks[3]--;
					if(c.sapTicks[3] == 5){
						c.startAnimation(12569);
						c.gfx0(c.sapGfx[3][0]);
						o.gfx0(c.sapGfx[3][1]);
						c.sapAmount[3] = 0.1;
						o.specAmount -= 10 * c.sapAmount[3];
						if(o.specAmount < 0)
							o.specAmount = 0;
						o.getItems().updateSpecialBar();
					}
				}else{
					c.sapTicks[3] = 5;
					c.startAnimation(12569);
					c.gfx0(c.sapGfx[3][0]);
					o.gfx0(c.sapGfx[3][1]);
					o.specAmount -= 10 * c.sapAmount[3];
					if(o.specAmount < 0)
						o.specAmount = 0;
					o.getItems().updateSpecialBar();
				}
			}
			if(c.prayerActive[42]){
				if(c.leechTicks[6] > 0){
					c.leechTicks[6]--;
					if(c.leechTicks[6] == 5){
						c.startAnimation(12569);
						o.gfx0(c.leechGfx[6][0]);
						c.leechAmount[6] = 0.1;
						c.specAmount += 10 * c.leechAmount[6];
						o.specAmount -= 10 * c.leechAmount[6];
						if(o.specAmount < 0)
							o.specAmount = 0;
						if(c.specAmount > 10)
							c.specAmount = 10;
						c.getItems().updateSpecialBar();
						o.getItems().updateSpecialBar();
					}
				}else{
					c.leechTicks[6] = 5;
					c.startAnimation(12575);
					o.gfx0(c.leechGfx[6][0]);
					c.specAmount += 10 * c.leechAmount[6];
					o.specAmount -= 10 * c.leechAmount[6];
					if(o.specAmount < 0)
						o.specAmount = 0;
					if(c.specAmount > 10)
						c.specAmount = 10;
					c.getItems().updateSpecialBar();
					o.getItems().updateSpecialBar();
				}
			}
			if(CastleWars.isInCw((Client)PlayerHandler.players[i]) && CastleWars.isInCw(c)){
				if(CastleWars.getTeamNumber(c) == CastleWars.getTeamNumber((Client)PlayerHandler.players[i])){
					c.sendMessage("You cannot attack your own teammate.");
					resetPlayerAttack();
					return;
				}
			}
			if(o.inClanWars && o.clanId.equalsIgnoreCase(c.clanId)){
				c.sendMessage("You cannot attack your own teammate.");
				resetPlayerAttack();
				return;
			}
			if(o.isWearingRing){
				o.getItems().removeItem(Config.EASTER_RING, o.playerRing);
				o.isWearingRing = false;
				o.npcId = -1;
				o.isNpc = false;
				o.updateRequired = true;
				o.appearanceUpdateRequired = true;
			}
			boolean sameSpot = c.absX == PlayerHandler.players[i].getX() && c.absY == PlayerHandler.players[i].getY();
			if(!c.goodDistance(PlayerHandler.players[i].getX(), PlayerHandler.players[i].getY(), c.getX(), c.getY(), 25) && !sameSpot){
				resetPlayerAttack();
				return;
			}

			c.pjAttackTimer = Misc.currentTimeSeconds();

			if(PlayerHandler.players[i].respawnTimer > 0){
				PlayerHandler.players[i].playerIndex = 0;
				resetPlayerAttack();
				return;
			}

			if(PlayerHandler.players[i].heightLevel != c.heightLevel){
				resetPlayerAttack();
				return;
			}
			c.followId = i;
			c.followId2 = 0;
			if(c.attackTimer <= 0){
				c.usingBow = false;
				c.specEffect = 0;
				c.usingRangeWeapon = false;
				c.rangeItemUsed = 0;
				boolean usingBow = false;
				boolean usingCBow = false;
				boolean usingArrows = false;
				boolean usingBolts = false;
				boolean usingOtherRangeWeapons = false;
				c.projectileStage = 0;

				if(c.absX == PlayerHandler.players[i].absX && c.absY == PlayerHandler.players[i].absY){
					if(c.freezeTimer > 0){
						resetPlayerAttack();
						return;
					}
					c.followId = i;
					c.attackTimer = 0;
					return;
				}
				if(!c.usingMagic){
					for(int bowId : c.BOWS){
						if(c.playerEquipment[c.playerWeapon] == bowId){
							usingBow = true;
							for(int arrowId : c.ARROWS){
								if(c.playerEquipment[c.playerArrows] == arrowId){
									usingArrows = true;
									break;
								}
							}
							break;
						}
					}
					if(!usingBow){
						for(int bowId : c.C_BOWS){
							if(c.playerEquipment[c.playerWeapon] == bowId){
								usingCBow = true;
								for(int arrowId : c.BOLTS){
									if(c.playerEquipment[c.playerArrows] == arrowId){
										usingBolts = true;
										break;
									}
								}
								break;
							}
						}
					}
					if(!usingBow && !usingCBow){
						for(int otherRangeId : c.OTHER_RANGE_WEAPONS){
							if(c.playerEquipment[c.playerWeapon] == otherRangeId){
								usingOtherRangeWeapons = true;
								break;
							}
						}
					}
					if(usingCrystalBow() || usingZaryteBow() || c.playerEquipment[c.playerWeapon] == 15241)
						usingBow = true;
				}
				if(c.autocasting){
					c.spellId = c.autocastId;
					c.usingMagic = true;
				}
				if(c.spellId > 0){
					c.usingMagic = true;
				}
				c.attackTimer = getAttackDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());

				/*if(c.duelRule[9] && c.duel.duelStatus == 3){
					boolean canUseWeapon = false;
					for(int funWeapon : Config.FUN_WEAPONS){
						if(c.playerEquipment[c.playerWeapon] == funWeapon){
							canUseWeapon = true;
						}
					}
					if(!canUseWeapon){
						c.sendMessage("You can only use fun weapons in this duel!");
						resetPlayerAttack();
						return;
					}
				}*/
				if(c.duel != null && c.duel.rules != null && c.duel.rules.duelRule[2] && (usingBow || usingOtherRangeWeapons || usingCBow || usingCrystalBow() || usingZaryteBow() || c.playerEquipment[c.playerWeapon] == 20097) && c.duel.status == 3){
					c.sendMessage("Range has been disabled in this duel!");
					resetPlayerAttack();
					return;
				}
				if(c.duel != null && c.duel.rules != null && c.duel.rules.duelRule[3] && (!usingBow && !usingOtherRangeWeapons && !c.usingMagic && !usingCBow) && c.duel.status == 3){
					c.sendMessage("Melee has been disabled in this duel!");
					resetPlayerAttack();
					return;
				}

				if(c.duel != null && c.duel.rules != null && c.duel.rules.duelRule[4] && c.usingMagic && c.duel.status == 3){
					c.sendMessage("Magic has been disabled in this duel!");
					resetPlayerAttack();
					return;
				}
				boolean noFollow = false;
				if((!c.goodDistance(c.getX(), c.getY(), PlayerHandler.players[i].getX(), PlayerHandler.players[i].getY(), 4) && (usingOtherRangeWeapons && !usingBow && !usingCBow && !c.usingMagic)) || (!c.goodDistance(c.getX(), c.getY(), PlayerHandler.players[i].getX(), PlayerHandler.players[i].getY(), 2) && (!usingOtherRangeWeapons && usingHally() && !usingBow && !usingCBow && !c.usingMagic)) || (!c.goodDistance(c.getX(), c.getY(), PlayerHandler.players[i].getX(), PlayerHandler.players[i].getY(), getRequiredDistance()) && (!usingOtherRangeWeapons && !usingHally() && !usingBow && !usingCBow && !c.usingMagic)) || (!c.goodDistance(c.getX(), c.getY(), PlayerHandler.players[i].getX(), PlayerHandler.players[i].getY(), 10) && (usingBow || usingCBow || c.usingMagic))){
					c.attackTimer = 1;
					if(!usingBow && !usingCBow && !c.usingMagic && !usingOtherRangeWeapons && c.freezeTimer > 0)
						resetPlayerAttack();
					return;
				}

				if(!usingCBow && !usingArrows && usingBow && !usingCrystalBow() && !usingZaryteBow() && c.playerEquipment[c.playerWeapon] != 15241 && !c.usingMagic){
					c.sendMessage("You have run out of arrows!");
					c.stopMovement();
					resetPlayerAttack();
					return;
				}
				if(correctBowAndArrows() < c.playerEquipment[c.playerArrows] && Config.CORRECT_ARROWS && usingBow && !usingCBow && !usingCrystalBow() && c.playerEquipment[c.playerWeapon] != 15241 && !usingZaryteBow() && c.playerEquipment[c.playerWeapon] != 9185 && c.playerEquipment[c.playerWeapon] != 18359 && !c.usingMagic){
					c.sendMessage("You can't use " + c.getItems().getItemName(c.playerEquipment[c.playerArrows]).toLowerCase() + "s with a " + c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase() + ".");
					c.stopMovement();
					resetPlayerAttack();
					return;
				}
				if(usingCBow && !properBolts() && !c.usingMagic && c.playerEquipment[c.playerWeapon] != 15241){
					c.sendMessage("You must use bolts with a crossbow.");
					c.stopMovement();
					resetPlayerAttack();
					return;
				}
				if(c.playerEquipment[c.playerWeapon] == 15241 && c.playerEquipment[c.playerArrows] != 15243){
					c.sendMessage("You must use hand cannon shots with a hand cannon.");
					c.stopMovement();
					resetPlayerAttack();
					return;
				}else if(c.playerEquipment[c.playerArrows] == 15243 && c.playerEquipment[c.playerWeapon] == 15241){
					usingBolts = true;
					usingCBow = true;
				}

				if(usingBow || usingCBow || c.usingMagic || usingOtherRangeWeapons || (c.goodDistance(c.getX(), c.getY(), PlayerHandler.players[i].getX(), PlayerHandler.players[i].getY(), 2) && usingHally())){
					c.stopMovement();
					noFollow = true;
					c.followId = c.followId2 = 0;
				}

				if(!checkMagicReqs(c.spellId)){
					c.stopMovement();
					resetPlayerAttack();
					return;
				}

				c.faceUpdate(i + 32768);

				if((c.duel == null || (c.duel != null && c.duel.status != 3)) && !c.inPits && !c.inCwGame && !c.inClanWars){
					if(!c.attackedPlayers.contains(c.playerIndex) && !PlayerHandler.players[c.playerIndex].attackedPlayers.contains(c.playerId)){
						c.attackedPlayers.add(c.playerIndex);
						c.isSkulled = true;
						c.skullTimer = Config.SKULL_TIMER;
						c.headIconPk = 0;
						c.getPA().requestUpdates();
					}
				}
				c.specAccuracy = 1.0;
				c.specDamage = 1.0;
				c.delayedDamage = c.delayedDamage2 = 0;
				if(c.usingSpecial && !c.usingMagic){
					if(c.duel != null && c.duel.rules != null && c.duel.rules.duelRule[10] && c.duel.status == 3){
						c.sendMessage("Special attacks have been disabled during this duel!");
						c.usingSpecial = false;
						c.getItems().updateSpecialBar();
						resetPlayerAttack();
						return;
					}
					if(checkSpecAmount(c.playerEquipment[c.playerWeapon])){
						c.lastArrowUsed = c.playerEquipment[c.playerArrows];
						activateSpecial(c.playerEquipment[c.playerWeapon], i);
						if(!noFollow)
							c.followId = c.playerIndex;
						return;
					}else{
						c.sendMessage("You don't have the required special energy to use this attack.");
						c.usingSpecial = false;
						c.getItems().updateSpecialBar();
						/*
						 * c.playerIndex = 0; return;
						 */
					}
				}

				if(!c.usingMagic){
					c.startAnimation(getWepAnim(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase()));
					if(!usingArrows && !usingBolts && !usingBow && !usingCBow && !usingOtherRangeWeapons && !usingCrystalBow() && !usingZaryteBow() && !usingDbow()){
						boolean veracsEffect = Misc.random(4) == 1 && c.getPA().fullVeracs();
						if(!veracsEffect && o.solSpec)
							o.gfx0(2320);
					}
					c.mageFollow = false;
				}else{
					c.startAnimation(getMageAnim());
					c.mageFollow = true;
					c.followId = c.playerIndex;
				}
				PlayerHandler.players[i].underAttackBy = c.playerId;
				PlayerHandler.players[i].logoutDelay = System.currentTimeMillis();
				PlayerHandler.players[i].singleCombatDelay = System.currentTimeMillis();
				PlayerHandler.players[i].killerId = c.playerId;
				c.lastArrowUsed = 0;
				c.rangeItemUsed = 0;
				if(!usingBow && !usingCBow && !c.usingMagic && !usingOtherRangeWeapons){ // melee hit delay
					c.followId = PlayerHandler.players[c.playerIndex].playerId;
					c.getPA().followPlayer();
					c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
					// c.delayedDamage = Misc.random(calculateMeleeMaxHit(i,
					// false));
					c.projectileStage = 0;
					c.oldPlayerIndex = i;
				}

				if((usingBow && !usingOtherRangeWeapons && !c.usingMagic && (usingArrows || usingCrystalBow() || usingZaryteBow())) || (usingCBow && usingBolts)){ // range hit delay
					if(usingCrystalBow()){
						c.rangeItemUsed = c.playerEquipment[c.playerWeapon];
						if(c.rangeItemUsed != 20097)
							c.crystalBowArrowCount++;
					}else if(!usingZaryteBow()){
						c.rangeItemUsed = c.playerEquipment[c.playerArrows];
						//c.getItems().deleteArrow();
					}else
						c.rangeItemUsed = c.playerEquipment[c.playerWeapon];
					if(c.fightMode == 2)
						c.attackTimer--;
					if(usingCBow)
						c.usingBow = true;
					c.usingBow = true;
					c.followId = PlayerHandler.players[c.playerIndex].playerId;
					c.getPA().followPlayer();
					c.lastWeaponUsed = c.playerEquipment[c.playerWeapon];
					c.lastArrowUsed = ((usingCrystalBow() || usingZaryteBow()) ? -1 : c.playerEquipment[c.playerArrows]);
					if(c.playerEquipment[c.playerWeapon] != 15241)
						c.gfx100(getRangeStartGFX());
					else
						c.gfx0(getRangeStartGFX());
					c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
					if(c.fightMode == 2 && c.lastWeaponUsed == 4734)
						c.hitDelay--;
					c.projectileStage = 1;
					c.oldPlayerIndex = i;
					fireProjectilePlayer();
				}

				if(usingOtherRangeWeapons){ // knives, darts, etc hit delay
					c.rangeItemUsed = c.playerEquipment[c.playerWeapon];
					c.usingRangeWeapon = true;
					c.followId = PlayerHandler.players[c.playerIndex].playerId;
					c.getPA().followPlayer();
					c.lastWeaponUsed = c.playerEquipment[c.playerWeapon];
					c.gfx100(getRangeStartGFX());
					if(c.fightMode == 2)
						c.attackTimer--;
					c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
					c.projectileStage = 1;
					c.oldPlayerIndex = i;
					fireProjectilePlayer();
				}

				if(c.usingMagic){ // magic hit delay
					int pX = c.getX();
					int pY = c.getY();
					int nX = PlayerHandler.players[i].getX();
					int nY = PlayerHandler.players[i].getY();
					int offX = (pY - nY) * -1;
					int offY = (pX - nX) * -1;
					c.castingMagic = true;
					c.projectileStage = 2;
					if(c.MAGIC_SPELLS[c.spellId][3] > 0){
						if(getStartGfxHeight() == 100){
							c.gfx100(c.MAGIC_SPELLS[c.spellId][3]);
						}else{
							c.gfx0(c.MAGIC_SPELLS[c.spellId][3]);
						}
					}
					if(c.MAGIC_SPELLS[c.spellId][4] > 0){
						c.getPA().createPlayersProjectile(pX, pY, offX, offY, 50, 78, c.MAGIC_SPELLS[c.spellId][4], getStartHeight(), getEndHeight(), -i - 1, getStartDelay());
					}
					if(c.autocastId > 0){
						c.followId = c.playerIndex;
						c.followDistance = 5;
					}
					c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
					c.oldPlayerIndex = i;
					c.oldSpellId = c.spellId;
					c.spellId = 0;
					if(c.MAGIC_SPELLS[c.oldSpellId][0] == 12891 && o.isMoving){
						// c.sendMessage("Barrage projectile..");
						c.getPA().createPlayersProjectile(pX, pY, offX, offY, 50, 85, 368, 25, 25, -i - 1, getStartDelay());
					}
					if(Misc.random(o.getCombat().mageDef()) > Misc.random(mageAtk())){
						c.magicFailed = true;
					}else{
						c.magicFailed = false;
					}
					int freezeDelay = getFreezeTime();// freeze time
					if(freezeDelay > 0 && PlayerHandler.players[i].freezeTimer <= -3 && !c.magicFailed){
						PlayerHandler.players[i].freezeTimer = freezeDelay;
						o.resetWalkingQueue();
						o.sendMessage("You have been frozen.");
					}
					if(!c.autocasting && c.spellId <= 0)
						c.playerIndex = 0;
				}

				if(usingBow && Config.CRYSTAL_BOW_DEGRADES){ // crystal bow degrading
					if(c.playerEquipment[c.playerWeapon] == 4212){ // new crystal bow becomes full bow on the first shot
						c.getItems().wearItem(4214, 1, 3);
					}
					if(c.crystalBowArrowCount >= 250){
						c.crystalBowArrowCount = 0;
						if(c.playerEquipment[c.playerWeapon] == 4223){ // 1/10
																		// bow
							c.getItems().wearItem(-1, 1, 3);
							c.sendMessage("Your crystal bow has fully degraded.");
							if(!c.inventory.addItem(4207, 1, -1))
								Server.itemHandler.createGroundItem(c, 4207, c.getX(), c.getY(), c.heightLevel, 1, c.getId());
						}else{
							c.getItems().wearItem(++c.playerEquipment[c.playerWeapon], 1, 3);
							c.sendMessage("Your crystal bow degrades.");
						}
					}
				}
			}
		}
	}

	public boolean usingCrystalBow(){
		return ((c.playerEquipment[c.playerWeapon] >= 4212 && c.playerEquipment[c.playerWeapon] <= 4223) || c.playerEquipment[c.playerWeapon] == 20097);
	}
	
	public boolean usingZaryteBow(){
		return (c.playerEquipment[c.playerWeapon] >= 20171 && c.playerEquipment[c.playerWeapon] < 20174);
	}

	public void appendVengeance(int otherPlayer, int damage){
		if(damage <= 0 || (c.duel != null && c.duel.rules != null && c.duel.rules.duelRule[4] && c.duel.status == 3))
			return;
		int vengDam = (int)(damage * 0.75);
		if(c.duel != null && c.duel.status == 3 && vengDam >= c.playerLevel[3])
			return;
		Player o = PlayerHandler.players[otherPlayer];
		if(o.isDead2)
			return;
		o.forcedText = "Taste Vengeance!";
		o.forcedChatUpdateRequired = true;
		o.updateRequired = true;
		o.vengOn = false;
		vengDam = vengDam > c.playerLevel[3] ? c.playerLevel[3] : vengDam;
		if(c.playerEquipment[c.playerWeapon] == 24201 || c.playerEquipment[c.playerWeapon] == 19784)
			vengDam = 0;
		c.setHitDiff2(vengDam);
		c.setHitUpdateRequired2(true);
		c.playerLevel[3] -= vengDam;
		c.getPA().refreshSkill(3);
		c.updateRequired = true;
	}

	public void playerDelayedHit(int i){
		if(PlayerHandler.players[i] != null){
			if(PlayerHandler.players[i].isDead || c.isDead || PlayerHandler.players[i].playerLevel[3] <= 0 || c.playerLevel[3] <= 0){
				c.playerIndex = 0;
				return;
			}
			if(PlayerHandler.players[i].respawnTimer > 0){
				c.faceUpdate(0);
				c.playerIndex = 0;
				return;
			}
			if(c.specHit)
				c.specHit = false;
			Client o = (Client)PlayerHandler.players[i];
			o.getPA().removeAllWindows();
			if(o.playerIndex <= 0 && o.npcIndex <= 0){
				if(o.autoRet == 1){
					o.playerIndex = c.playerId;
				}
			}
			if(o.attackTimer <= 3 || o.attackTimer == 0 && o.playerIndex == 0 && !c.castingMagic){ // block animation
				o.startAnimation(o.getCombat().getBlockEmote());
			}
			if(o.inTrade){
				o.getTradeAndDuel().declineTrade(true);
			}
			if(c.projectileStage == 0 && !c.usingMagic && !c.castingMagic){ // melee hit damage
				int hits[] = new int[4];
				if(c.usingClaws){
					c.usingClaws = false;
					int bit = 0;
					int tempDamage = getMeleeDamage((calculateMeleeMaxHit(i, false)), i, false);
					int damage = (int)(tempDamage * 0.7);
					if(o.playerEquipment[c.playerShield] == 18363 && damage >= 20){
						o.sendMessage("Your shield soaks up 20% damage from the original " + damage);
						tempDamage = (int)Math.floor(tempDamage * 0.80);
					}
					hits[0] = tempDamage;
					hits[1] = hits[0] == 0 ? getMeleeDamage(calculateMeleeMaxHit(i, false), i, false) : hits[0] / 2;
					hits[2] = (hits[0] == 0 && hits[1] == 0) ? getMeleeDamage((int)(calculateMeleeMaxHit(i, false) * 0.75), i, false) : hits[1] / 2;
					if(hits[1] > 0 && hits[2] < Math.round(hits[1] / 2))
						bit++;
					hits[3] = (hits[0] == 0 && hits[1] == 0) ? (hits[2] == 0 ? (int)(getMeleeDamage((int)(calculateMeleeMaxHit(i, false) * 0.75), i, false) * 1.5) : getMeleeDamage((int)(calculateMeleeMaxHit(i, false) * 0.75), i, false)) : hits[2] + bit;
					if(hits[3] == 0)
						hits[3] = Misc.random(7);
					int temp = -1;
					int add = 0;
					for(int j = 0; j < hits.length; j++){
						if(temp > -1 && temp <= j){
							hits[j] = 0;
							continue;
						}
						add += hits[j];
						if(add > o.playerLevel[o.playerHitpoints]){
							temp = j + 1;
							hits[j] = o.playerLevel[o.playerHitpoints] - (add - hits[j]);
						}
					}
					c.clawHitPos = 0;
					c.clawHits = new int[]{0, 0, 0, 0};
					for(int j : hits){
						applyPlayerClawDamage(i, 3, (int)(j * 0.70));
					}
				}else if(c.doubleHit){
					c.doubleHit = false;
					int hit1 = getMeleeDamage(calculateMeleeMaxHit(i, false) + Misc.random(4), i, false);
					if(o.playerEquipment[c.playerShield] == 18363 && hit1 >= 20){
						o.sendMessage("Your shield soaks up 20% damage from the original " + hit1);
						hit1 = (int)Math.floor(hit1 * 0.80);
					}
					applyPlayerClawDamage(i, 2, hit1);
					applyPlayerClawDamage(i, 2, getMeleeDamage(calculateMeleeMaxHit(i, false) + Misc.random(4), i, false));
				}else{
					int hit = getMeleeDamage(calculateMeleeMaxHit(i, false), i, false);
					if(o.playerEquipment[c.playerShield] == 18363 && hit >= 20){
						o.sendMessage("Your shield soaks up 20% damage from the original " + hit);
						hit = (int)Math.floor(hit * 0.80);
					}
					applyPlayerClawDamage(i, 1, hit);
					if(c.ssSpec){
						c.ssSpec = false;
						int damage = hit > 0 ? Misc.random_range(5, 20) : 0;
						if(c.prayerActive[44]){
							int heal = (int)Math.floor(damage * 0.2);
							int temp = c.getLevelForXP(c.playerXP[c.playerHitpoints]) + c.zarosModifier;
							if(c.playerLevel[c.playerHitpoints] + heal > temp)
								heal = temp >= c.playerLevel[c.playerHitpoints] ? temp - c.playerLevel[c.playerHitpoints] : 0;
							c.playerLevel[c.playerHitpoints] += heal;
							c.getPA().refreshSkill(c.playerHitpoints);
							int drain = (int)Math.floor(damage * 0.2);
							if(o.playerLevel[o.playerPrayer] < 0)
								drain = o.playerLevel[o.playerPrayer];
							o.playerLevel[o.playerPrayer] -= drain;
							o.getPA().refreshSkill(o.playerPrayer);
							o.gfx0(2264);
						}
						if(o.playerEquipment[c.playerShield] == 18361 && damage >= 20){
							o.sendMessage("Your shield soaks up 18% damage from the original " + damage);
							damage = (int)Math.floor(damage * 0.82);
						}
						if(o.playerEquipment[c.playerShield] == 18363 && damage >= 20){
							o.sendMessage("Your shield soaks up 10% damage from the original " + damage);
							damage = (int)Math.floor(damage * 0.90);
						}
						if((o.prayerActive[16] || o.prayerActive[33]) && System.currentTimeMillis() - o.protMageDelay > 1500)
							damage = (int)damage * 60 / 100;
						if(o.vengOn)
							appendVengeance(i, damage);
						if(damage > 0)
							applyRecoil(damage, i);
						c.getPA().addSkillXP((damage * Config.MAGIC_EXP_RATE), 6);
						c.getPA().addSkillXP((damage * Config.MAGIC_EXP_RATE / 3), 3);
						c.getPA().refreshSkill(3);
						c.getPA().refreshSkill(6);
						if(PlayerHandler.players[i].playerLevel[3] - damage < 0)
							damage = PlayerHandler.players[i].playerLevel[3];
						PlayerHandler.players[i].underAttackBy = c.playerId;
						c.killedBy = PlayerHandler.players[i].playerId;
						PlayerHandler.players[i].dealDamage(damage);
						PlayerHandler.players[i].damageTaken[c.playerId] += damage;
						c.totalPlayerDamageDealt += damage;
						applySmite(i, damage);
						o.getPA().refreshSkill(3);
						PlayerHandler.players[i].updateRequired = true;
						c.usingMagic = false;
						c.castingMagic = false;
					}
				}
			}

			if(!c.castingMagic && c.projectileStage > 0){ // range hit damage
				if(c.quincyStage == 1){
					if(c.playerEquipment[c.playerWeapon] == 20097)
						quincySpec();
					c.quincyStage = 0;
				}
				if(c.playerEquipment[c.playerWeapon] == 15241)
					cannonExplosion();
				if(usingCrystalBow() || usingZaryteBow())
					c.lastArrowUsed = -1;
				int rngMaxHit = rangeMaxHit();
				int damage = Misc.random(rngMaxHit);
				int damage2 = -1;
				if(c.lastWeaponUsed == 11235 || c.bowSpecShot == 1)
					damage2 = Misc.random(rngMaxHit);
				boolean ignoreDef = false;
				if(Misc.random(4) == 1 && c.lastArrowUsed == 9243){
					ignoreDef = true;
					o.gfx0(758);
				}
				if(c.usingQuincySpec){
					o.gfx0(728);
					c.usingQuincySpec = false;
				}
				if(Misc.random(10 + o.getCombat().calculateRangeDefence()) > Misc.random(10 + calculateRangeAttack()) && !ignoreDef){
					damage = 0;
				}
				if(c.prayerActive[44]){
					int heal = (int)Math.floor(damage * 0.2);
					int temp = c.getLevelForXP(c.playerXP[c.playerHitpoints]) + c.zarosModifier;
					if(c.playerLevel[c.playerHitpoints] + heal > temp)
						heal = temp >= c.playerLevel[c.playerHitpoints] ? temp - c.playerLevel[c.playerHitpoints] : 0;
					c.playerLevel[c.playerHitpoints] += heal;
					c.getPA().refreshSkill(c.playerHitpoints);
					int drain = (int)Math.floor(damage * 0.2);
					if(o.playerLevel[o.playerPrayer] < 0)
						drain = o.playerLevel[o.playerPrayer];
					o.playerLevel[o.playerPrayer] -= drain;
					if(o.playerLevel[o.playerPrayer] < 0)
						o.playerLevel[o.playerPrayer] = 0;
					o.getPA().refreshSkill(o.playerPrayer);
					o.gfx0(2264);
				}
				if(Misc.random(4) == 1 && c.lastArrowUsed == 9242 && damage > 0){
					o.gfx0(754);
					damage = o.playerLevel[o.playerHitpoints] / 5;
					c.dealDamage(c.playerLevel[3] / 10);
					c.gfx0(754);
				}

				if(c.lastWeaponUsed == 11235 || c.bowSpecShot == 1){
					if(Misc.random(10 + o.getCombat().calculateRangeDefence()) > Misc.random(10 + calculateRangeAttack()))
						damage2 = 0;
				}

				if(c.dbowSpec){
					o.gfx100(1100);
					if(damage < 8)
						damage = 8;
					if(damage2 < 8)
						damage2 = 8;
					c.dbowSpec = false;
				}
				if(damage > 0 && Misc.random(5) == 1 && c.lastArrowUsed == 9244){
					damage *= 1.45;
					o.gfx0(756);
				}
				int damage3 = 0;
				if((o.prayerActive[17] || o.prayerActive[34]) && System.currentTimeMillis() - o.protRangeDelay > 1500){ // if prayer active reduce damage by half
					if(o.prayerActive[34])
						damage3 = (((Misc.random(99) + 1) >= 65) ? damage / 10 : 0);
					damage = (int)damage * 60 / 100;
					if(c.lastWeaponUsed == 11235 || c.bowSpecShot == 1)
						damage2 = (int)damage2 * 60 / 100;
				}
				if(o.playerEquipment[c.playerShield] == 18361 && damage >= 20){
					o.sendMessage("Your shield soaks up 12% damage from the original " + damage);
					damage = (int)Math.floor(damage * 0.88);
					if(damage2 >= 20)
						damage2 = (int)Math.floor(damage * 0.93);
				}
				if(damage < 0)
					damage = 0;
				if(damage2 < 0 && damage2 != -1)
					damage2 = 0;
				if(o.vengOn){
					appendVengeance(i, damage);
					appendVengeance(i, damage2);
				}
				if(damage > 0)
					applyRecoil(damage, i);
				if(damage2 > 0)
					applyRecoil(damage2, i);
				if(c.fightMode == 3){
					c.getPA().addSkillXP((damage * Config.RANGE_EXP_RATE / 3), 4);
					c.getPA().addSkillXP((damage * Config.RANGE_EXP_RATE / 3), 1);
					c.getPA().addSkillXP((damage * Config.RANGE_EXP_RATE / 3), 3);
					c.getPA().refreshSkill(1);
					c.getPA().refreshSkill(3);
					c.getPA().refreshSkill(4);
				}else{
					c.getPA().addSkillXP((damage * Config.RANGE_EXP_RATE), 4);
					c.getPA().addSkillXP((damage * Config.RANGE_EXP_RATE / 3), 3);
					c.getPA().refreshSkill(3);
					c.getPA().refreshSkill(4);
				}
				boolean dropArrows = true;
				boolean usingOtherRangeWeapons = false;
				for(int otherRangeId : c.OTHER_RANGE_WEAPONS){
					if(c.playerEquipment[c.playerWeapon] == otherRangeId){
						usingOtherRangeWeapons = true;
						break;
					}
				}
				for(int noArrowId : c.NO_ARROW_DROP){
					if(c.lastWeaponUsed == noArrowId){
						dropArrows = false;
						break;
					}
				}
				if(c.lastArrowUsed == 19157)
					dropArrows = false;
				if(dropArrows && !usingOtherRangeWeapons){
					c.rangeItemUsed = c.playerEquipment[c.playerArrows];
					if(usingDbow() && c.getItems().deleteArrow())
						c.getItems().dropArrowPlayer();
					if(c.getItems().deleteArrow())
						c.getItems().dropArrowPlayer();
					else{
						c.sendMessage("You must use bolts with a crossbow.");
						resetPlayerAttack();
						return;
					}
				}else if(dropArrows && usingOtherRangeWeapons){
					c.rangeItemUsed = c.playerEquipment[c.playerWeapon];
					if(c.getItems().deleteEquipment())
						c.getItems().dropArrowPlayer();
					else{
						resetPlayerAttack();
						return;
					}
				}else if(usingOtherRangeWeapons && !dropArrows){
					if(!c.getItems().deleteEquipment()){
						resetPlayerAttack();
						return;
					}
				}else if(c.lastArrowUsed == 19157 || c.lastWeaponUsed == 15241)
					c.getItems().deleteLostArrow();
				if(c.lastWeaponUsed == 4734){ // Karil's bow
					if(!c.getItems().deleteArrow()){
						c.sendMessage("You must use bolt racks with a Karil's crossbow.");
						resetPlayerAttack();
						return;
					}
				}
				if(o.playerEquipment[o.playerWeapon] == 19784){
					damage = 0;
					damage2 = 0;
				}
				if(c.guthixBowSpec){
					damage2 = ((int)(damage * 0.5));
					if(o.playerLevel[o.playerHitpoints] - damage <= 0 && damage2 > 0)
						damage2 = 0;
					else{
						c.playerLevel[c.playerHitpoints] += damage;
						if(c.playerLevel[c.playerHitpoints] > c.getLevelForXP(c.playerXP[c.playerHitpoints]) + c.zarosModifier)
							c.playerLevel[c.playerHitpoints] = c.getLevelForXP(c.playerXP[c.playerHitpoints]) + c.zarosModifier;
						c.getPA().refreshSkill(c.playerHitpoints);
					}
					o.gfx0(127);
					c.guthixBowSpec = false;
				}
				if(o.playerEquipment[o.playerHands] == 22358 || o.playerEquipment[o.playerHands] == 22359 ||
						o.playerEquipment[o.playerHands] == 22360 || o.playerEquipment[o.playerHands] == 22361){
					damage = ((int)Math.floor(damage * 0.95));
					damage2 = ((int)Math.floor(damage2 * 0.95));
				}
				if(damage > 0 && c.activateMJav){
					c.activateMJav = false;
					PlayerHandler.players[i].mJavHit += damage;
					PlayerHandler.players[i].mJavTime = Misc.currentTimeSeconds();
				}
				PlayerHandler.players[i].underAttackBy = c.playerId;
				PlayerHandler.players[i].logoutDelay = System.currentTimeMillis();
				PlayerHandler.players[i].singleCombatDelay = System.currentTimeMillis();
				PlayerHandler.players[i].killerId = c.playerId;
				// PlayerHandler.players[i].setHitDiff(damage);
				// PlayerHandler.players[i].playerLevel[3] -= damage;
				if(PlayerHandler.players[i].playerLevel[3] - damage < 0)
					damage = PlayerHandler.players[i].playerLevel[3];
				if(PlayerHandler.players[i].playerLevel[3] - damage - damage2 < 0)
					damage2 = PlayerHandler.players[i].playerLevel[3] - damage;
				c.killedBy = PlayerHandler.players[i].playerId;
				PlayerHandler.players[i].dealDamage(damage);
				PlayerHandler.players[i].damageTaken[c.playerId] += damage;
				if(damage2 != -1){
					// PlayerHandler.players[i].playerLevel[3] -= damage2;
					PlayerHandler.players[i].dealDamage(damage2);
					PlayerHandler.players[i].damageTaken[c.playerId] += damage2;
				}
				o.getPA().refreshSkill(3);
				if(damage3 > 0 && damage > 0){
					c.dealDamage(damage3);
					c.damageTaken[i] += damage3;
					c.getPA().refreshSkill(3);
					o.gfx0(2227);
					o.startAnimation(12573);
				}
				// PlayerHandler.players[i].setHitUpdateRequired(true);
				PlayerHandler.players[i].updateRequired = true;
				applySmite(i, damage);
				if(damage2 != -1)
					applySmite(i, damage2);
			}else if(c.projectileStage > 0){ // magic hit damage
				int damage = 0;
				boolean storm = false;
				if(c.MAGIC_SPELLS[c.oldSpellId][6] == -1){
					storm = true;
					int minHit = (int)((((c.getLevelForXP(c.playerXP[c.playerMagic]) - 82) * 0.5) + 2.5) + (c.playerEquipment[c.playerWeapon] == 21777 ? 5 : 0));
					int maxHit = (int)((((c.getLevelForXP(c.playerXP[c.playerMagic]) - 82) * 0.5) + 18.5) + (c.playerEquipment[c.playerWeapon] == 21777 ? 5 : 0));
					damage = Misc.random_range(minHit, maxHit);
				}else
					damage = Misc.random(c.MAGIC_SPELLS[c.oldSpellId][6]);
				if(godSpells()){
					if(System.currentTimeMillis() - c.godSpellDelay < Config.GOD_SPELL_CHARGE){
						damage += 10;
					}
				}
				if(c.playerEquipment[c.playerWeapon] == 24201)
					c.magicFailed = false;
				// c.playerIndex = 0;
				if(c.magicFailed && !storm)
					damage = 0;
				if(c.prayerActive[44]){
					int heal = (int)Math.floor(damage * 0.2);
					int temp = c.getLevelForXP(c.playerXP[c.playerHitpoints]) + c.zarosModifier;
					if(c.playerLevel[c.playerHitpoints] + heal > temp)
						heal = temp >= c.playerLevel[c.playerHitpoints] ? temp - c.playerLevel[c.playerHitpoints] : 0;
					c.playerLevel[c.playerHitpoints] += heal;
					c.getPA().refreshSkill(c.playerHitpoints);
					int drain = (int)Math.floor(damage * 0.2);
					if(o.playerLevel[o.playerPrayer] < 0)
						drain = o.playerLevel[o.playerPrayer];
					o.playerLevel[o.playerPrayer] -= drain;
					o.getPA().refreshSkill(o.playerPrayer);
					o.gfx0(2264);
				}
				if(o.playerEquipment[c.playerShield] == 18361 && damage >= 20){
					o.sendMessage("Your shield soaks up 18% damage from the original " + damage);
					damage = (int)Math.floor(damage * 0.82);
				}
				if(o.playerEquipment[c.playerShield] == 18363 && damage >= 20){
					o.sendMessage("Your shield soaks up 10% damage from the original " + damage);
					damage = (int)Math.floor(damage * 0.90);
				}
				int damage2 = 0;
				if((o.prayerActive[16] || o.prayerActive[33]) && System.currentTimeMillis() - o.protMageDelay > 1500){ // if prayer active reduce damage by half
					if(o.prayerActive[33])
						damage2 = (((Misc.random(99) + 1) >= 65) ? damage / 10 : 0);
					damage = (int)damage * 60 / 100;
				}
				if(o.vengOn)
					appendVengeance(i, damage);
				if(damage > 0)
					applyRecoil(damage, i);
				c.getPA().addSkillXP((c.MAGIC_SPELLS[c.oldSpellId][7] + damage * Config.MAGIC_EXP_RATE), 6);
				c.getPA().addSkillXP((c.MAGIC_SPELLS[c.oldSpellId][7] + damage * Config.MAGIC_EXP_RATE / 3), 3);
				c.getPA().refreshSkill(3);
				c.getPA().refreshSkill(6);

				if(getEndGfxHeight() == 100 && !c.magicFailed){ // end GFX
					PlayerHandler.players[i].gfx100(c.MAGIC_SPELLS[c.oldSpellId][5]);
				}else if(!c.magicFailed){
					PlayerHandler.players[i].gfx0(c.MAGIC_SPELLS[c.oldSpellId][5]);
				}else if(c.magicFailed){
					PlayerHandler.players[i].gfx100(85);
				}

				if(!c.magicFailed){
					if(System.currentTimeMillis() - PlayerHandler.players[i].reduceStat > 35000){
						PlayerHandler.players[i].reduceStat = System.currentTimeMillis();
						switch(c.MAGIC_SPELLS[c.oldSpellId][0]){
							case 12987:
							case 13011:
							case 12999:
							case 13023:
								PlayerHandler.players[i].playerLevel[0] -= ((o.getPA().getLevelForXP(PlayerHandler.players[i].playerXP[0]) * 10) / 100);
								break;
						}
					}

					switch(c.MAGIC_SPELLS[c.oldSpellId][0]){
						case 12445: // teleblock
							if(c.playerMagicBook != 0)
								break;
							if(System.currentTimeMillis() - o.teleBlockDelay > o.teleBlockLength){
								o.teleBlockDelay = System.currentTimeMillis();
								o.sendMessage("You have been teleblocked.");
								if((o.prayerActive[16] || o.prayerActive[33]) && System.currentTimeMillis() - o.protMageDelay > 1500)
									o.teleBlockLength = 150000;
								else
									o.teleBlockLength = 300000;
							}
							break;
						case 12435:
							o.playerLevel[o.playerDefence] -= (o.playerLevel[o.playerDefence] == 0 ? 0 : 1);
							o.getPA().refreshSkill(o.playerDefence);
							break;
						case 12901:
						case 12919: // blood spells
						case 12911:
						case 12929:
							int heal = (int)(damage * 0.25);
							if(c.playerLevel[3] + heal > c.getPA().getLevelForXP(c.playerXP[3])){
								c.playerLevel[3] = c.getPA().getLevelForXP(c.playerXP[3]);
							}else{
								c.playerLevel[3] += heal;
							}
							c.getPA().refreshSkill(3);
							break;

						case 1153:
							PlayerHandler.players[i].playerLevel[0] -= ((o.getPA().getLevelForXP(PlayerHandler.players[i].playerXP[0]) * 5) / 100);
							o.sendMessage("Your attack level has been reduced!");
							PlayerHandler.players[i].reduceSpellDelay[c.reduceSpellId] = System.currentTimeMillis();
							o.getPA().refreshSkill(0);
							break;

						case 1157:
							PlayerHandler.players[i].playerLevel[2] -= ((o.getPA().getLevelForXP(PlayerHandler.players[i].playerXP[2]) * 5) / 100);
							o.sendMessage("Your strength level has been reduced!");
							PlayerHandler.players[i].reduceSpellDelay[c.reduceSpellId] = System.currentTimeMillis();
							o.getPA().refreshSkill(2);
							break;

						case 1161:
							PlayerHandler.players[i].playerLevel[1] -= ((o.getPA().getLevelForXP(PlayerHandler.players[i].playerXP[1]) * 5) / 100);
							o.sendMessage("Your defence level has been reduced!");
							PlayerHandler.players[i].reduceSpellDelay[c.reduceSpellId] = System.currentTimeMillis();
							o.getPA().refreshSkill(1);
							break;

						case 1542:
							PlayerHandler.players[i].playerLevel[1] -= ((o.getPA().getLevelForXP(PlayerHandler.players[i].playerXP[1]) * 10) / 100);
							o.sendMessage("Your defence level has been reduced!");
							PlayerHandler.players[i].reduceSpellDelay[c.reduceSpellId] = System.currentTimeMillis();
							o.getPA().refreshSkill(1);
							break;

						case 1543:
							PlayerHandler.players[i].playerLevel[2] -= ((o.getPA().getLevelForXP(PlayerHandler.players[i].playerXP[2]) * 10) / 100);
							o.sendMessage("Your strength level has been reduced!");
							PlayerHandler.players[i].reduceSpellDelay[c.reduceSpellId] = System.currentTimeMillis();
							o.getPA().refreshSkill(2);
							break;

						case 12861:
						case 12871:
						case 12891:
						case 12881:
							if(o.freezeTimer < -4){
								o.freezeTimer = getFreezeTime();
								o.stopMovement();
							}
							break;

						case 1562:
							PlayerHandler.players[i].playerLevel[0] -= ((o.getPA().getLevelForXP(PlayerHandler.players[i].playerXP[0]) * 10) / 100);
							o.sendMessage("Your attack level has been reduced!");
							PlayerHandler.players[i].reduceSpellDelay[c.reduceSpellId] = System.currentTimeMillis();
							o.getPA().refreshSkill(0);
							break;
					}
				}
				if(o.playerEquipment[o.playerWeapon] == 19784)
					damage = 0;
				if(o.playerEquipment[o.playerHands] == 22362 || o.playerEquipment[o.playerHands] == 22363 ||
						o.playerEquipment[o.playerHands] == 22364 || o.playerEquipment[o.playerHands] == 22365)
					damage = ((int)Math.floor(damage * 0.95));
				if(Misc.random(99) >= 39 && (c.playerEquipment[c.playerHands] == 22366 || c.playerEquipment[c.playerHands] == 22367 ||
						c.playerEquipment[c.playerHands] == 22368 || c.playerEquipment[c.playerHands] == 22369)){
					int attack = ((int)Math.floor(o.getLevelForXP(o.playerXP[o.playerAttack]) * 0.9));
					int strength = ((int)Math.floor(o.getLevelForXP(o.playerXP[o.playerStrength]) * 0.9));
					int def = ((int)Math.floor(o.getLevelForXP(o.playerXP[o.playerDefence]) * 0.9));
					if(attack < o.playerLevel[o.playerAttack]){
						o.playerLevel[o.playerAttack] = attack;
						o.getPA().refreshSkill(o.playerAttack);
					}
					if(strength < o.playerLevel[o.playerStrength]){
						o.playerLevel[o.playerStrength] = strength;
						o.getPA().refreshSkill(o.playerStrength);
					}
					if(def < o.playerLevel[o.playerDefence]){
						o.playerLevel[o.playerDefence] = def;
						o.getPA().refreshSkill(o.playerDefence);
					}
				}
				PlayerHandler.players[i].logoutDelay = System.currentTimeMillis();
				PlayerHandler.players[i].underAttackBy = c.playerId;
				PlayerHandler.players[i].killerId = c.playerId;
				PlayerHandler.players[i].singleCombatDelay = System.currentTimeMillis();
				c.killedBy = PlayerHandler.players[i].playerId;
				if(c.MAGIC_SPELLS[c.oldSpellId][6] != 0){
					// PlayerHandler.players[i].playerLevel[3] -= damage;
					if(PlayerHandler.players[i].playerLevel[3] - damage < 0)
						damage = PlayerHandler.players[i].playerLevel[3];
					PlayerHandler.players[i].dealDamage(damage);
					PlayerHandler.players[i].damageTaken[c.playerId] += damage;
					c.totalPlayerDamageDealt += damage;
					if(!c.magicFailed){
						// PlayerHandler.players[i].setHitDiff(damage);
						// PlayerHandler.players[i].setHitUpdateRequired(true);
						if(damage2 > 0){
							c.dealDamage(damage2);
							c.damageTaken[i] += damage2;
							c.getPA().refreshSkill(3);
							o.gfx0(2228);
							o.startAnimation(12573);
						}
					}
				}
				applySmite(i, damage);
				o.getPA().refreshSkill(3);
				PlayerHandler.players[i].updateRequired = true;
				c.usingMagic = false;
				c.castingMagic = false;
				if(o.inMulti() && multis()){
					c.barrageCount = 0;
					for(Player p : RegionManager.getLocalPlayers(c)){
						if(p != null){
							if(p.playerId == o.playerId)
								continue;
							if(c.barrageCount >= 9)
								break;
							if((c.inClanWars && p.clanId.equalsIgnoreCase(c.clanId)) || (c.inCwGame && c.castleWarsTeam == p.castleWarsTeam))
								continue;
							if(o.goodDistance(o.getX(), o.getY(), p.getX(), p.getY(), 1))
								appendMultiBarrage(p.playerId, c.magicFailed);
						}
					}
				}
				c.getPA().refreshSkill(3);
				c.getPA().refreshSkill(6);
				c.oldSpellId = 0;
			}
		}
		c.getPA().requestUpdates();
		if(c.bowSpecShot <= 0){
			c.oldPlayerIndex = 0;
			c.projectileStage = 0;
			c.lastWeaponUsed = 0;
			c.doubleHit = false;
			c.bowSpecShot = 0;
		}
		if(c.bowSpecShot != 0){
			c.bowSpecShot = 0;
		}
	}

	public boolean multis(){
		switch(c.MAGIC_SPELLS[c.oldSpellId][0]){
			case 12891:
			case 12881:
			case 13011:
			case 13023:
			case 12919: // blood spells
			case 12929:
			case 12963:
			case 12975:
				return true;
		}
		return false;

	}

	public void appendMultiBarrage(int playerId, boolean splashed){
		if(PlayerHandler.players[playerId] != null){
			Client c2 = (Client)PlayerHandler.players[playerId];
			if(c2.isDead || c2.respawnTimer > 0)
				return;
			if(checkMultiBarrageReqs(playerId)){
				c.barrageCount++;
				if(Misc.random(mageAtk()) > Misc.random(mageDef()) && !c.magicFailed){
					if(getEndGfxHeight() == 100){ // end GFX
						c2.gfx100(c.MAGIC_SPELLS[c.oldSpellId][5]);
					}else{
						c2.gfx0(c.MAGIC_SPELLS[c.oldSpellId][5]);
					}
					int damage = Misc.random(c.MAGIC_SPELLS[c.oldSpellId][6]);
					if(c2.prayerActive[12]){
						damage *= (int)(.60);
					}
					if(c2.playerLevel[3] - damage < 0){
						damage = c2.playerLevel[3];
					}
					c.getPA().addSkillXP((c.MAGIC_SPELLS[c.oldSpellId][7] + damage * Config.MAGIC_EXP_RATE), 6);
					c.getPA().addSkillXP((c.MAGIC_SPELLS[c.oldSpellId][7] + damage * Config.MAGIC_EXP_RATE / 3), 3);
					// PlayerHandler.players[playerId].setHitDiff(damage);
					// PlayerHandler.players[playerId].setHitUpdateRequired(true);
					// PlayerHandler.players[playerId].playerLevel[3] -= damage;
					PlayerHandler.players[playerId].dealDamage(damage);
					PlayerHandler.players[playerId].damageTaken[c.playerId] += damage;
					c2.getPA().refreshSkill(3);
					c.totalPlayerDamageDealt += damage;
					multiSpellEffect(playerId, damage);
				}else{
					c2.gfx100(85);
				}
			}
		}
	}

	public void multiSpellEffect(int playerId, int damage){
		switch(c.MAGIC_SPELLS[c.oldSpellId][0]){
			case 13011:
			case 13023:
				if(System.currentTimeMillis() - PlayerHandler.players[playerId].reduceStat > 35000){
					PlayerHandler.players[playerId].reduceStat = System.currentTimeMillis();
					PlayerHandler.players[playerId].playerLevel[0] -= ((PlayerHandler.players[playerId].getLevelForXP(PlayerHandler.players[playerId].playerXP[0]) * 10) / 100);
				}
				break;
			case 12919: // blood spells
			case 12929:
				int heal = (int)(damage * 0.25);
				if(c.playerLevel[3] + heal >= c.getPA().getLevelForXP(c.playerXP[3])){
					c.playerLevel[3] = c.getPA().getLevelForXP(c.playerXP[3]);
				}else{
					c.playerLevel[3] += heal;
				}
				c.getPA().refreshSkill(3);
				break;
			case 12891:
			case 12881:
				if(PlayerHandler.players[playerId].freezeTimer < -4){
					PlayerHandler.players[playerId].freezeTimer = getFreezeTime();
					PlayerHandler.players[playerId].stopMovement();
				}
				break;
		}
	}

	public void applyPlayerClawDamage(int i, int damageMask, int damage){
		Client o = (Client)PlayerHandler.players[i];
		if(o == null){
			return;
		}
		if(c.prayerActive[44]){
			int heal = (int)Math.floor(damage * 0.2);
			int temp = c.getLevelForXP(c.playerXP[c.playerHitpoints]) + c.zarosModifier;
			if(c.playerLevel[c.playerHitpoints] + heal > temp)
				heal = temp >= c.playerLevel[c.playerHitpoints] ? temp - c.playerLevel[c.playerHitpoints] : 0;
			c.playerLevel[c.playerHitpoints] += heal;
			c.getPA().refreshSkill(c.playerHitpoints);
			int drain = (int)Math.floor(damage * 0.2);
			if(o.playerLevel[o.playerPrayer] < 0)
				drain = o.playerLevel[o.playerPrayer];
			o.playerLevel[o.playerPrayer] -= drain;
			o.getPA().refreshSkill(o.playerPrayer);
			o.gfx0(2264);
		}
		c.previousDamage = damage;
		int deflectDamage = 0;
		if(o.prayerActive[35])
			deflectDamage = Misc.random((int)Math.round(damage * 0.25));
		boolean guthansEffect = c.getPA().fullGuthans() && Misc.random(4) == 1;
		boolean veracsEffect = c.getPA().fullVeracs() && Misc.random(4) == 1;
		if(damage > 0 && guthansEffect){
			c.playerLevel[3] += damage;
			if(c.playerLevel[3] > c.getLevelForXP(c.playerXP[3]))
				c.playerLevel[3] = c.getLevelForXP(c.playerXP[3]);
			c.getPA().refreshSkill(3);
			o.gfx0(398);
		}
		if(o.vengOn && damage > 0)
			appendVengeance(i, damage);
		if(damage > 0)
			applyRecoil(damage, i);
		switch(c.specEffect){
			case 1: // dragon scimmy special
				if(damage > 0){
					for(int j = 0; j<46; j++){
						if(o.isProtectionPrayer(j) && o.prayerActive[j]){
							o.prayerActive[j] = false;
							o.getPA().sendConfig(c.PRAYER_GLOW[j], 0);
						}
					}
					o.headIcon = -1;
					o.sendMessage("You have been injured!");
					o.stopPrayerDelay = System.currentTimeMillis();
					o.getPA().requestUpdates();
				}
				break;
			case 2:
				if(damage > 0){
					if(o.freezeTimer <= 0)
						o.freezeTimer = 30;
					o.gfx0(369);
					o.sendMessage("You have been frozen.");
					o.stopMovement();
					c.sendMessage("You freeze your enemy.");
				}
				break;
			case 3:
				if(damage > 0){
					o.playerLevel[1] -= damage;
					o.sendMessage("You feel weak.");
					if(o.playerLevel[1] < 1)
						o.playerLevel[1] = 1;
					o.getPA().refreshSkill(1);
				}
				break;
			case 4:
				if(damage > 0){
					if(c.playerLevel[3] + damage > c.getLevelForXP(c.playerXP[3]))
						if(c.playerLevel[3] > c.getLevelForXP(c.playerXP[3]))
							;
						else
							c.playerLevel[3] = c.getLevelForXP(c.playerXP[3]);
					else
						c.playerLevel[3] += damage;
					c.getPA().refreshSkill(3);
				}
				break;
			case 5:
				c.clawDelay = 2;
				break;
			case 6:
				if(damage > 0){
					o.playerLevel[o.playerDefence] -= (int)Math.round(1 + (0.3 * o.playerLevel[o.playerDefence]));
					o.getPA().refreshSkill(o.playerDefence);
				}
				break;
		}
		if((o.prayerActive[18] || o.prayerActive[35]) && System.currentTimeMillis() - o.protMeleeDelay > 1500 && !veracsEffect && c.playerEquipment[c.playerWeapon] != 19784) // if prayer active reduce maxHit by 40%
			damage = (int)damage * 60 / 100;
		if(c.fightMode == 3){
			c.getPA().addSkillXP((damage * Config.MELEE_EXP_RATE / 3), 0);
			c.getPA().addSkillXP((damage * Config.MELEE_EXP_RATE / 3), 1);
			c.getPA().addSkillXP((damage * Config.MELEE_EXP_RATE / 3), 2);
			c.getPA().addSkillXP((damage * Config.MELEE_EXP_RATE / 3), 3);
			c.getPA().refreshSkill(0);
			c.getPA().refreshSkill(1);
			c.getPA().refreshSkill(2);
			c.getPA().refreshSkill(3);
		}else{
			c.getPA().addSkillXP((damage * Config.MELEE_EXP_RATE), c.fightMode);
			c.getPA().addSkillXP((damage * Config.MELEE_EXP_RATE / 3), 3);
			c.getPA().refreshSkill(c.fightMode);
			c.getPA().refreshSkill(3);
		}
		if(!veracsEffect && o.solSpec)
			damage /= 2;
		if(o.playerEquipment[o.playerWeapon] == 19784)
			damage = 0;
		if(o.playerLevel[o.playerHitpoints] - damage < 0)
			damage = o.playerLevel[o.playerHitpoints];
		if(c.specEffect == 7){
			int prayerDrain = ((int)(damage * 0.45));
			prayerDrain = o.playerLevel[o.playerPrayer] < prayerDrain ? o.playerLevel[o.playerPrayer] : prayerDrain;
			o.playerLevel[o.playerPrayer] -= prayerDrain;
			c.playerLevel[c.playerPrayer] += prayerDrain;
			o.getPA().refreshSkill(o.playerPrayer);
			c.getPA().refreshSkill(c.playerPrayer);
		}
		c.specEffect = 0;
		PlayerHandler.players[i].logoutDelay = System.currentTimeMillis();
		PlayerHandler.players[i].underAttackBy = c.playerId;
		PlayerHandler.players[i].killerId = c.playerId;
		PlayerHandler.players[i].singleCombatDelay = System.currentTimeMillis();
		if(c.killedBy != PlayerHandler.players[i].playerId)
			c.totalPlayerDamageDealt = 0;
		c.killedBy = PlayerHandler.players[i].playerId;
		if(deflectDamage > 0 && c.playerEquipment[c.playerWeapon] != 19784){
			c.dealDamage(deflectDamage);
			c.damageTaken[i] += deflectDamage;
			c.getPA().refreshSkill(3);
			o.gfx0(2230);
			o.startAnimation(12573);
		}
		applySmite(i, damage);
		switch(damageMask){
			case 1:
				/*
				 * if (!PlayerHandler.players[i].getHitUpdateRequired()){
				 * PlayerHandler.players[i].setHitDiff(damage);
				 * PlayerHandler.players[i].setHitUpdateRequired(true); } else {
				 * PlayerHandler.players[i].setHitDiff2(damage);
				 * PlayerHandler.players[i].setHitUpdateRequired2(true); }
				 */
				// PlayerHandler.players[i].playerLevel[3] -= damage;
				PlayerHandler.players[i].dealDamage(damage);
				PlayerHandler.players[i].damageTaken[c.playerId] += damage;
				c.totalPlayerDamageDealt += damage;
				PlayerHandler.players[i].updateRequired = true;
				o.getPA().refreshSkill(3);
				break;
			case 2:
				/*
				 * if (!PlayerHandler.players[i].getHitUpdateRequired2()){
				 * PlayerHandler.players[i].setHitDiff2(damage);
				 * PlayerHandler.players[i].setHitUpdateRequired2(true); } else
				 * { PlayerHandler.players[i].setHitDiff(damage);
				 * PlayerHandler.players[i].setHitUpdateRequired(true); }
				 */
				// PlayerHandler.players[i].playerLevel[3] -= damage;
				PlayerHandler.players[i].dealDamage(damage);
				PlayerHandler.players[i].damageTaken[c.playerId] += damage;
				c.totalPlayerDamageDealt += damage;
				PlayerHandler.players[i].updateRequired = true;
				c.doubleHit = false;
				o.getPA().refreshSkill(3);
				break;
			case 3:
				PlayerHandler.players[i].dealDamage(damage);
				PlayerHandler.players[i].damageTaken[c.playerId] += damage;
				c.totalPlayerDamageDealt += damage;
				PlayerHandler.players[i].updateRequired = true;
				o.getPA().refreshSkill(3);
				c.clawDelay = c.clawDelay == 0 ? 2 : c.clawDelay;
				c.clawHits[c.clawHitPos++] = damage;
				break;
		}
	}

	public void applyPlayerMeleeDamage(int i, int damageMask){
		Client o = (Client)PlayerHandler.players[i];
		if(o == null){
			return;
		}
		int damage = 0;
		int deflectDamage = 0;
		boolean guthansEffect = false;
		if(c.getPA().fullGuthans()){
			if(Misc.random(4) == 1){
				guthansEffect = true;
			}
		}
		if(damageMask == 1){
			damage = c.delayedDamage;
			c.delayedDamage = 0;
		}else{
			damage = c.delayedDamage2;
			c.delayedDamage2 = 0;
		}
		if(o.prayerActive[35])
			deflectDamage = (((Misc.random(99) + 1) >= 65) ? damage / 10 : 0);
		if(damage > 0 && guthansEffect){
			c.playerLevel[3] += damage;
			if(c.playerLevel[3] > c.getLevelForXP(c.playerXP[3]))
				c.playerLevel[3] = c.getLevelForXP(c.playerXP[3]);
			c.getPA().refreshSkill(3);
			o.gfx0(398);
		}
		if(o.vengOn && damage > 0)
			appendVengeance(i, damage);
		if(damage > 0)
			applyRecoil(damage, i);
		switch(c.specEffect){
			case 1: // dragon scimmy special
				if(damage > 0){
					for(int j = 0; j<46; j++){
						if(o.isProtectionPrayer(j) && o.prayerActive[j]){
							o.prayerActive[j] = false;
							o.getPA().sendConfig(c.PRAYER_GLOW[j], 0);
						}
					}
					o.headIcon = -1;
					o.sendMessage("You have been injured!");
					o.stopPrayerDelay = System.currentTimeMillis();
					o.getPA().requestUpdates();
				}
				break;
			case 2:
				if(damage > 0){
					if(o.freezeTimer <= 0)
						o.freezeTimer = 30;
					o.gfx0(369);
					o.sendMessage("You have been frozen.");
					o.stopMovement();
					c.sendMessage("You freeze your enemy.");
				}
				break;
			case 3:
				if(damage > 0){
					o.playerLevel[1] -= damage;
					o.sendMessage("You feel weak.");
					if(o.playerLevel[1] < 1)
						o.playerLevel[1] = 1;
					o.getPA().refreshSkill(1);
				}
				break;
			case 4:
				if(damage > 0){
					if(c.playerLevel[3] + damage > c.getLevelForXP(c.playerXP[3]))
						if(c.playerLevel[3] > c.getLevelForXP(c.playerXP[3]))
							;
						else
							c.playerLevel[3] = c.getLevelForXP(c.playerXP[3]);
					else
						c.playerLevel[3] += damage;
					c.getPA().refreshSkill(3);
				}
				break;
			case 5:
				c.clawDelay = 2;
				break;
		}
		c.specEffect = 0;
		if(c.fightMode == 3){
			c.getPA().addSkillXP((damage * Config.MELEE_EXP_RATE / 3), 0);
			c.getPA().addSkillXP((damage * Config.MELEE_EXP_RATE / 3), 1);
			c.getPA().addSkillXP((damage * Config.MELEE_EXP_RATE / 3), 2);
			c.getPA().addSkillXP((damage * Config.MELEE_EXP_RATE / 3), 3);
			c.getPA().refreshSkill(0);
			c.getPA().refreshSkill(1);
			c.getPA().refreshSkill(2);
			c.getPA().refreshSkill(3);
		}else{
			c.getPA().addSkillXP((damage * Config.MELEE_EXP_RATE), c.fightMode);
			c.getPA().addSkillXP((damage * Config.MELEE_EXP_RATE / 3), 3);
			c.getPA().refreshSkill(c.fightMode);
			c.getPA().refreshSkill(3);
		}
		PlayerHandler.players[i].logoutDelay = System.currentTimeMillis();
		PlayerHandler.players[i].underAttackBy = c.playerId;
		PlayerHandler.players[i].killerId = c.playerId;
		PlayerHandler.players[i].singleCombatDelay = System.currentTimeMillis();
		if(c.killedBy != PlayerHandler.players[i].playerId)
			c.totalPlayerDamageDealt = 0;
		c.killedBy = PlayerHandler.players[i].playerId;
		if(deflectDamage > 0){
			c.dealDamage(deflectDamage);
			c.damageTaken[i] += deflectDamage;
			c.getPA().refreshSkill(3);
		}
		applySmite(i, damage);
		switch(damageMask){
			case 1:
				/*
				 * if (!PlayerHandler.players[i].getHitUpdateRequired()){
				 * PlayerHandler.players[i].setHitDiff(damage);
				 * PlayerHandler.players[i].setHitUpdateRequired(true); } else {
				 * PlayerHandler.players[i].setHitDiff2(damage);
				 * PlayerHandler.players[i].setHitUpdateRequired2(true); }
				 */
				// PlayerHandler.players[i].playerLevel[3] -= damage;
				PlayerHandler.players[i].dealDamage(damage);
				PlayerHandler.players[i].damageTaken[c.playerId] += damage;
				c.totalPlayerDamageDealt += damage;
				PlayerHandler.players[i].updateRequired = true;
				o.getPA().refreshSkill(3);
				break;

			case 2:
				/*
				 * if (!PlayerHandler.players[i].getHitUpdateRequired2()){
				 * PlayerHandler.players[i].setHitDiff2(damage);
				 * PlayerHandler.players[i].setHitUpdateRequired2(true); } else
				 * { PlayerHandler.players[i].setHitDiff(damage);
				 * PlayerHandler.players[i].setHitUpdateRequired(true); }
				 */
				// PlayerHandler.players[i].playerLevel[3] -= damage;
				PlayerHandler.players[i].dealDamage(damage);
				PlayerHandler.players[i].damageTaken[c.playerId] += damage;
				c.totalPlayerDamageDealt += damage;
				PlayerHandler.players[i].updateRequired = true;
				c.doubleHit = false;
				o.getPA().refreshSkill(3);
				break;
		}
	}

	public void applySmite(int index, int damage){
		if(!c.prayerActive[23])
			return;
		if(damage <= 0)
			return;
		if(PlayerHandler.players[index] != null){
			Client c2 = (Client)PlayerHandler.players[index];
			c2.playerLevel[5] -= (int)(damage / 4);
			if(c2.playerLevel[5] <= 0){
				c2.playerLevel[5] = 0;
				c2.getCombat().resetPrayers();
			}
			c2.getPA().refreshSkill(5);
		}

	}

	public void fireProjectilePlayer(){
		if(c.oldPlayerIndex > 0){
			if(PlayerHandler.players[c.oldPlayerIndex] != null){
				c.projectileStage = 2;
				int pX = c.getX();
				int pY = c.getY();
				int oX = PlayerHandler.players[c.oldPlayerIndex].getX();
				int oY = PlayerHandler.players[c.oldPlayerIndex].getY();
				int offX = (pY - oY) * -1;
				int offY = (pX - oX) * -1;
				if(!c.msbSpec)
					c.getPA().createPlayersProjectile(pX, pY, offX, offY, 50, getProjectileSpeed(), getRangeProjectileGFX(), 43, 31, -c.oldPlayerIndex - 1, getStartDelay());
				else if(c.msbSpec){
					c.getPA().createPlayersProjectile2(pX, pY, offX, offY, 50, getProjectileSpeed(), getRangeProjectileGFX(), 43, 31, -c.oldPlayerIndex - 1, getStartDelay(), 10);
					c.msbSpec = false;
				}
				if(usingDbow())
					c.getPA().createPlayersProjectile2(pX, pY, offX, offY, 50, getProjectileSpeed(), getRangeProjectileGFX(), 60, 31, -c.oldPlayerIndex - 1, getStartDelay(), 35);
			}
		}
	}

	public boolean usingDbow(){
		return c.playerEquipment[c.playerWeapon] == 11235;
	}

	/** Prayer **/
	public void activatePrayer(int id){
		if((id < 26 && c.cursesActive) || (id > 25 && !c.cursesActive))
			return;
		for(int i = id > 25 ? 0 : 26; i<(id > 25 ? 26 : 46); i++)
			c.prayerActive[i] = false;
		if(System.currentTimeMillis() - c.stopPrayerDelay < 5000 && c.isProtectionPrayer(id)){
			c.sendMessage("You have been injured and can't use this prayer!");
			c.prayerActive[id] = false;
			c.getPA().sendConfig(c.PRAYER_GLOW[id], 0);
			return;
		}
		if(c.inZombiesGame){
			c.sendMessage("You are unable to hear the voice of your God.");
			c.prayerActive[id] = false;
			c.getPA().sendConfig(c.PRAYER_GLOW[id], 0);
			return;
		}
		if(c.duel != null && c.duel.rules != null && c.duel.rules.duelRule[7] && c.duel.status == 3){
			for(int p = 0; p < c.PRAYER.length; p++){ // reset prayer glows
				c.prayerActive[p] = false;
				c.getPA().sendConfig(c.PRAYER_GLOW[p], 0);
			}
			c.sendMessage("Prayer has been disabled in this duel!");
			return;
		}
		if((id == 26 || id == 10) && c.isInFala()){
			c.getPA().sendConfig(c.PRAYER_GLOW[id], 0);
			c.sendMessage("You can't Protect item in Falador PVP.");
			c.sendMessage("You lose all items in Falador PVP.");
			return;
		}
		if(id == 24 && c.getPA().getLevelForXP(c.playerXP[1]) < 70){
			c.getPA().sendConfig(c.PRAYER_GLOW[id], 0);
			c.sendMessage("You need 70 Defence to use Piety");
			return;
		}
		if((id == 25 || id == 45) && c.getPA().getLevelForXP(c.playerXP[1]) < 20){
			c.getPA().sendConfig(c.PRAYER_GLOW[id], 0);
			c.sendMessage("You need 20 defence to use turmoil");
			return;
		}

		int defPray[] = {0, 5, 13, 24, 25};
		int strPray[] = {1, 6, 14, 24, 25};
		int atkPray[] = {2, 7, 15, 24, 25};
		int rangePray[] = {3, 11, 19};
		int magePray[] = {4, 12, 20};
		int turmoil = 45;
		int curseSap[] = {27, 28, 29, 30, turmoil};
		int curseLeech[] = {36, 37, 38, 39, 40, 41, 42, turmoil};
		int deflectPray[] = {32, 33, 34, 35, 43, 44};

		if(c.playerLevel[5] > 0 || !Config.PRAYER_POINTS_REQUIRED){
			if(c.getPA().getLevelForXP(c.playerXP[5]) >= c.PRAYER_LEVEL_REQUIRED[id] || !Config.PRAYER_LEVEL_REQUIRED){
				boolean headIcon = false;
				switch(id){
					case 0:
					case 5:
					case 13:
						if(c.prayerActive[id] == false){
							for(int j = 0; j < defPray.length; j++){
								if(defPray[j] != id){
									c.prayerActive[defPray[j]] = false;
									c.getPA().sendConfig(c.PRAYER_GLOW[defPray[j]], 0);
								}
							}
						}
						break;
					case 27:
					case 28:
					case 29:
					case 30:
						if(c.prayerActive[id] == false){
							for(int j : curseLeech){
								c.prayerActive[j] = false;
								c.getPA().sendConfig(c.PRAYER_GLOW[j], 0);
								if(j != turmoil)
									c.getPA().resetLeech(j);
							}
						}
						if(c.prayerActive[id] == false)
							c.sapTicks[id - 27] = Config.CURSE_TICKS;
						else
							c.getPA().resetSap(id);
						break;
					case 32:
					case 33:
					case 34:
					case 35:
					case 43:
					case 44:
						headIcon = true;
						if(c.prayerActive[id] == false){
							for(int j : deflectPray){
								if(j != id){
									c.prayerActive[j] = false;
									c.getPA().sendConfig(c.PRAYER_GLOW[j], 0);
								}
							}
						}
						break;
					case 36:
					case 37:
					case 38:
					case 39:
					case 40:
					case 41:
					case 42:
						if(c.prayerActive[id] == false){
							for(int j : curseSap){
								c.prayerActive[j] = false;
								c.getPA().sendConfig(c.PRAYER_GLOW[j], 0);
								if(j != turmoil)
									c.getPA().resetSap(j);
							}
						}
						if(c.prayerActive[id] == false)
							c.leechTicks[id - 36] = Config.CURSE_TICKS;
						else
							c.getPA().resetLeech(id);
						break;
					case 45:
						if(c.prayerActive[id] == false){
							for(int j : curseSap){
								if(j == turmoil)
									continue;
								c.prayerActive[j] = false;
								c.getPA().sendConfig(c.PRAYER_GLOW[j], 0);
								c.getPA().resetSap(j);
							}
							for(int j : curseLeech){
								if(j == turmoil)
									continue;
								c.prayerActive[j] = false;
								c.getPA().sendConfig(c.PRAYER_GLOW[j], 0);
								c.getPA().resetLeech(j);
							}
						}
						break;
					case 1:
					case 6:
					case 14:
						if(c.prayerActive[id] == false){
							for(int j = 0; j < strPray.length; j++){
								if(strPray[j] != id){
									c.prayerActive[strPray[j]] = false;
									c.getPA().sendConfig(c.PRAYER_GLOW[strPray[j]], 0);
								}
							}
							for(int j = 0; j < rangePray.length; j++){
								if(rangePray[j] != id){
									c.prayerActive[rangePray[j]] = false;
									c.getPA().sendConfig(c.PRAYER_GLOW[rangePray[j]], 0);
								}
							}
							for(int j = 0; j < magePray.length; j++){
								if(magePray[j] != id){
									c.prayerActive[magePray[j]] = false;
									c.getPA().sendConfig(c.PRAYER_GLOW[magePray[j]], 0);
								}
							}
						}
						break;

					case 2:
					case 7:
					case 15:
						if(c.prayerActive[id] == false){
							for(int j = 0; j < atkPray.length; j++){
								if(atkPray[j] != id){
									c.prayerActive[atkPray[j]] = false;
									c.getPA().sendConfig(c.PRAYER_GLOW[atkPray[j]], 0);
								}
							}
							for(int j = 0; j < rangePray.length; j++){
								if(rangePray[j] != id){
									c.prayerActive[rangePray[j]] = false;
									c.getPA().sendConfig(c.PRAYER_GLOW[rangePray[j]], 0);
								}
							}
							for(int j = 0; j < magePray.length; j++){
								if(magePray[j] != id){
									c.prayerActive[magePray[j]] = false;
									c.getPA().sendConfig(c.PRAYER_GLOW[magePray[j]], 0);
								}
							}
						}
						break;

					case 3:// range prays
					case 11:
					case 19:
						if(c.prayerActive[id] == false){
							for(int j = 0; j < atkPray.length; j++){
								if(atkPray[j] != id){
									c.prayerActive[atkPray[j]] = false;
									c.getPA().sendConfig(c.PRAYER_GLOW[atkPray[j]], 0);
								}
							}
							for(int j = 0; j < strPray.length; j++){
								if(strPray[j] != id){
									c.prayerActive[strPray[j]] = false;
									c.getPA().sendConfig(c.PRAYER_GLOW[strPray[j]], 0);
								}
							}
							for(int j = 0; j < rangePray.length; j++){
								if(rangePray[j] != id){
									c.prayerActive[rangePray[j]] = false;
									c.getPA().sendConfig(c.PRAYER_GLOW[rangePray[j]], 0);
								}
							}
							for(int j = 0; j < magePray.length; j++){
								if(magePray[j] != id){
									c.prayerActive[magePray[j]] = false;
									c.getPA().sendConfig(c.PRAYER_GLOW[magePray[j]], 0);
								}
							}
						}
						break;
					case 4:
					case 12:
					case 20:
						if(c.prayerActive[id] == false){
							for(int j = 0; j < atkPray.length; j++){
								if(atkPray[j] != id){
									c.prayerActive[atkPray[j]] = false;
									c.getPA().sendConfig(c.PRAYER_GLOW[atkPray[j]], 0);
								}
							}
							for(int j = 0; j < strPray.length; j++){
								if(strPray[j] != id){
									c.prayerActive[strPray[j]] = false;
									c.getPA().sendConfig(c.PRAYER_GLOW[strPray[j]], 0);
								}
							}
							for(int j = 0; j < rangePray.length; j++){
								if(rangePray[j] != id){
									c.prayerActive[rangePray[j]] = false;
									c.getPA().sendConfig(c.PRAYER_GLOW[rangePray[j]], 0);
								}
							}
							for(int j = 0; j < magePray.length; j++){
								if(magePray[j] != id){
									c.prayerActive[magePray[j]] = false;
									c.getPA().sendConfig(c.PRAYER_GLOW[magePray[j]], 0);
								}
							}
						}
						break;
					case 10:
						c.lastProtItem = System.currentTimeMillis();
						break;

					case 16:
					case 17:
					case 18:
						if(id == 16)
							c.protMageDelay = System.currentTimeMillis();
						else if(id == 17)
							c.protRangeDelay = System.currentTimeMillis();
						else if(id == 18)
							c.protMeleeDelay = System.currentTimeMillis();
					case 21:
					case 22:
					case 23:
						headIcon = true;
						for(int p = 16; p < 24; p++){
							if(id != p && p != 19 && p != 20){
								c.prayerActive[p] = false;
								c.getPA().sendConfig(c.PRAYER_GLOW[p], 0);
							}
						}
						break;
					case 24:
						if(c.prayerActive[id] == false){

							for(int j = 0; j < atkPray.length; j++){
								if(atkPray[j] != id){
									c.prayerActive[atkPray[j]] = false;
									c.getPA().sendConfig(c.PRAYER_GLOW[atkPray[j]], 0);
								}
							}
							for(int j = 0; j < strPray.length; j++){
								if(strPray[j] != id){
									c.prayerActive[strPray[j]] = false;
									c.getPA().sendConfig(c.PRAYER_GLOW[strPray[j]], 0);
								}
							}
							for(int j = 0; j < rangePray.length; j++){
								if(rangePray[j] != id){
									c.prayerActive[rangePray[j]] = false;
									c.getPA().sendConfig(c.PRAYER_GLOW[rangePray[j]], 0);
								}
							}
							for(int j = 0; j < magePray.length; j++){
								if(magePray[j] != id){
									c.prayerActive[magePray[j]] = false;
									c.getPA().sendConfig(c.PRAYER_GLOW[magePray[j]], 0);
								}
							}
							for(int j = 0; j < defPray.length; j++){
								if(defPray[j] != id){
									c.prayerActive[defPray[j]] = false;
									c.getPA().sendConfig(c.PRAYER_GLOW[defPray[j]], 0);
								}
							}
						}
						break;
					case 25:

						if(c.prayerActive[id] == false){ // turmoil
							for(int j = 0; j < atkPray.length; j++){
								if(atkPray[j] != id){
									c.prayerActive[atkPray[j]] = false;
									c.getPA().sendConfig(c.PRAYER_GLOW[atkPray[j]], 0);
								}
							}
							for(int j = 0; j < strPray.length; j++){
								if(strPray[j] != id){
									c.prayerActive[strPray[j]] = false;
									c.getPA().sendConfig(c.PRAYER_GLOW[strPray[j]], 0);
								}
							}
							for(int j = 0; j < rangePray.length; j++){
								if(rangePray[j] != id){
									c.prayerActive[rangePray[j]] = false;
									c.getPA().sendConfig(c.PRAYER_GLOW[rangePray[j]], 0);
								}
							}
							for(int j = 0; j < magePray.length; j++){
								if(magePray[j] != id){
									c.prayerActive[magePray[j]] = false;
									c.getPA().sendConfig(c.PRAYER_GLOW[magePray[j]], 0);
								}
							}
							for(int j = 0; j < defPray.length; j++){
								if(defPray[j] != id){
									c.prayerActive[defPray[j]] = false;
									c.getPA().sendConfig(c.PRAYER_GLOW[defPray[j]], 0);
								}
							}
						}
						break;
				}

				if(!headIcon){
					if(!c.prayerActive[id]){
						c.prayerActive[id] = true;
						c.getPA().sendConfig(c.PRAYER_GLOW[id], 1);
					}else{
						c.prayerActive[id] = false;
						c.getPA().sendConfig(c.PRAYER_GLOW[id], 0);
					}
				}else{
					if(!c.prayerActive[id]){
						c.prayerActive[id] = true;
						c.getPA().sendConfig(c.PRAYER_GLOW[id], 1);
						c.headIcon = c.PRAYER_HEAD_ICONS[id];
						c.getPA().requestUpdates();
					}else{
						c.prayerActive[id] = false;
						c.getPA().sendConfig(c.PRAYER_GLOW[id], 0);
						c.headIcon = -1;
						c.getPA().requestUpdates();
					}
				}
			}else{
				c.getPA().sendConfig(c.PRAYER_GLOW[id], 0);
				c.getPA().sendText("You need a @blu@Prayer level of " + c.PRAYER_LEVEL_REQUIRED[id] + " to use " + c.PRAYER_NAME[id] + ".", 357);
				c.getPA().sendText("Click here to continue", 358);
				c.getPA().sendFrame164(356);
				c.nextChat = 0;
			}
		}else{
			c.getPA().sendConfig(c.PRAYER_GLOW[id], 0);
			c.sendMessage("You have run out of prayer points!");
		}
	}

	/**
	 * Specials
	 **/

	public void activateSpecial(int weapon, int i){
		if(NPCHandler.npcs[i] == null && c.npcIndex > 0){
			return;
		}
		if(PlayerHandler.players[i] == null && c.playerIndex > 0){
			return;
		}
		Client o = c.playerIndex > 0 ? (Client)PlayerHandler.players[i] : null;
		if(c.playerIndex > 0 && o == null)
			return;
		c.doubleHit = false;
		c.specEffect = 0;
		c.projectileStage = 0;
		c.specMaxHitIncrease = 2;
		if(c.npcIndex > 0){
			c.oldNpcIndex = i;
		}else if(c.playerIndex > 0){
			c.oldPlayerIndex = i;
			PlayerHandler.players[i].underAttackBy = c.playerId;
			PlayerHandler.players[i].logoutDelay = System.currentTimeMillis();
			PlayerHandler.players[i].singleCombatDelay = System.currentTimeMillis();
			PlayerHandler.players[i].killerId = c.playerId;
		}
		switch(weapon){
			case 1305: // dragon long
				c.gfx100(248);
				c.startAnimation(1058);
				c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
				c.specAccuracy = 1.68;
				c.specDamage = 1.35;
				c.specHit = true;
				break;
			case 15241:
				c.specAccuracy = 2;
				c.specDamage = 1.25;
				c.specHit = true;
				c.startAnimation(12175);
				c.gfx0(2141);
				c.lastWeaponUsed = weapon;
				c.projectileStage = 1;
				if(c.playerIndex > 0)
					fireProjectilePlayer();
				else if(c.npcIndex > 0)
					fireProjectileNpc();
				c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
				break;
			case 19146:
				c.guthixBowSpec = true;
				c.usingBow = true;
				c.rangeItemUsed = c.playerEquipment[c.playerArrows];
				//c.getItems().deleteArrow();
				//c.getItems().deleteArrow();
				c.lastWeaponUsed = weapon;
				c.startAnimation(426);
				c.projectileStage = 1;
				c.specHit = true;
				c.gfx100(124);
				c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
				if(c.fightMode == 2)
					c.attackTimer--;
				if(c.playerIndex > 0)
					fireProjectilePlayer();
				else if(c.npcIndex > 0)
					fireProjectileNpc();
				c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
				break;
			case 24455:
				c.specAccuracy = 0.75;
				c.specDamage = 2.4;
				c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
				c.specHit = true;
				c.startAnimation(16961);
				c.gfx0(44);
				c.deleteAnnihilation = true;
				c.deleteAnnihilationTick = 5;
				break;
			case 22346: // Dominion sword.
				c.specAccuracy = 0.8;
				c.specDamage = 1.45;
				c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
				c.specHit = true;
				c.specEffect = 7;
				c.gfx0(479);
				c.startAnimation(2876);
				break;
			case 13879: // Morrigan's Javelin.
			case 13880:
			case 13881:
			case 13882:
				c.activateMJav = true;
				c.projectileStage = 1;
				c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
				break;
			case 13883: // Morrigan's Javelin.
				c.specHit = true;
				c.specDamage = 1.2;
				c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
				break;
			case 20097:
				c.quincyStage++;
				c.attackTimer = 10;
				break;
			case 19784:
			case 19780: // Korasi
				c.startAnimation(1872);
				if(o != null)
					o.gfx100(1248);
				else
					NPCHandler.npcs[i].gfx100(1248);
				c.specAccuracy = 3.5;
				c.specDamage = 1.85;
				c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
				c.specHit = true;
				break;
			case 1215: // dragon daggers
			case 1231:
			case 5680:
			case 5698:
				c.gfx100(252);
				c.startAnimation(1062);
				c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
				c.doubleHit = true;
				c.usingClaws = false;
				c.specAccuracy = 1.30;
				c.specDamage = 1.05;
				c.specHit = true;
				break;
			case 11730:
				c.gfx100(1224);
				c.startAnimation(811);
				c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
				c.ssSpec = true;
				c.specDamage = 1.10;
				c.specHit = true;
				break;
			case 14484: // Dragon claws
				c.startAnimation(10961);
				c.gfx0(1950);
				c.doubleHit = false;
				c.usingClaws = true;
				c.specAccuracy = 2.40;
				c.specDamage = 1.40;
				c.specHit = true;
				c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
				break;
			case 4151: // whip
			case 15441:
			case 15442:
			case 15443:
			case 15444:
				c.startAnimation(11956);
				if(o != null)
					o.gfx100(2108);
				else if(NPCHandler.npcs[i] != null)
					NPCHandler.npcs[i].gfx100(2108);
				c.specAccuracy = 1.10;
				c.specHit = true;
				c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
				break;
			case 11694: // ags
				c.startAnimation(7074);
				c.specDamage = 1.7;
				c.specAccuracy = 2.20;
				c.gfx0(1222);
				c.specHit = true;
				c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
				break;
			case 20098:
				c.startAnimation(2890);
				if(c.playerIndex > 0){
					if(o.playerLevel[o.playerDefence] == o.getLevelForXP(o.playerXP[o.playerDefence])){
						o.playerLevel[o.playerDefence] *= 0.80;
						o.getPA().refreshSkill(o.playerDefence);
					}
				}else{
					if(NPCHandler.npcs[i].oldDefence == NPCHandler.npcs[i].defence)
						NPCHandler.npcs[i].defence *= 0.80;
				}
				c.specHit = true;
				c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
				break;
			case 11700:
				c.startAnimation(7070);
				c.gfx0(1221);
				c.specAccuracy = 1.25;
				c.specHit = true;
				c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
				c.specEffect = 2;
				break;
			case 11696:
				c.startAnimation(7073);
				c.gfx0(1223);
				c.specDamage = 1.30;
				c.specAccuracy = 1.85;
				c.specHit = true;
				c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
				c.specEffect = 3;
				break;
			case 11698:
				c.startAnimation(7071);
				c.gfx0(1220);
				c.specHit = true;
				c.specAccuracy = 1.25;
				c.specEffect = 4;
				c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
				break;
			case 1249:
				if(c.duel != null && c.duel.rules != null && c.duel.rules.duelRule[1] && c.duel.status == 3){
					c.sendMessage("Movement has been disabled for this duel!");
					break;
				}
				c.startAnimation(1064);
				c.gfx100(253);
				if(c.playerIndex > 0)
					o.getPA().getSpeared(c.absX, c.absY);
				break;
			case 3204: // d hally
				c.gfx100(282);
				c.specHit = true;
				c.startAnimation(1203);
				c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
				if(NPCHandler.npcs[i] != null && c.npcIndex > 0){
					if(!c.goodDistance(c.getX(), c.getY(), NPCHandler.npcs[i].getX(), NPCHandler.npcs[i].getY(), 1)){
						c.doubleHit = true;
					}
				}
				if(PlayerHandler.players[i] != null && c.playerIndex > 0){
					if(!c.goodDistance(c.getX(), c.getY(), PlayerHandler.players[i].getX(), PlayerHandler.players[i].getY(), 1)){
						c.doubleHit = true;
						c.delayedDamage2 = Misc.random(calculateMeleeMaxHit(i, false));
					}
				}
				break;

			case 4153: // maul
				c.startAnimation(1667);
				c.specHit = true;
				c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
				/*
				 * if (c.playerIndex > 0) gmaulPlayer(i); else gmaulNpc(i);
				 */
				c.gfx100(337);
				break;
			case 4587: // dscimmy
				c.gfx100(347);
				c.specHit = true;
				c.specEffect = 1;
				c.startAnimation(1872);
				c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
				break;
			case 1434: // mace
				c.startAnimation(1060);
				c.specHit = true;
				c.gfx100(251);
				c.specMaxHitIncrease = 3;
				c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase()) + 1;
				c.specDamage = 1.35;
				c.specAccuracy = 1.15;
				break;
			case 859: // magic long
				c.usingBow = true;
				c.bowSpecShot = 3;
				c.rangeItemUsed = c.playerEquipment[c.playerArrows];
				//c.getItems().deleteArrow();
				c.lastWeaponUsed = weapon;
				c.startAnimation(426);
				c.specHit = true;
				c.gfx100(250);
				c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
				c.projectileStage = 1;
				if(c.fightMode == 2)
					c.attackTimer--;
				break;
			case 861: // magic short
				c.usingBow = true;
				c.bowSpecShot = 1;
				c.specHit = true;
				c.rangeItemUsed = c.playerEquipment[c.playerArrows];
				//c.getItems().deleteArrow();
				c.lastWeaponUsed = weapon;
				c.startAnimation(1074);
				c.hitDelay = 3;
				c.projectileStage = 1;
				c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
				if(c.fightMode == 2)
					c.attackTimer--;
				if(c.playerIndex > 0)
					fireProjectilePlayer();
				else if(c.npcIndex > 0)
					fireProjectileNpc();
				break;
			case 11235: // dark bow
				c.usingBow = true;
				c.dbowSpec = true;
				c.rangeItemUsed = c.playerEquipment[c.playerArrows];
				//c.getItems().deleteArrow();
				//c.getItems().deleteArrow();
				c.lastWeaponUsed = weapon;
				c.hitDelay = 3;
				c.startAnimation(426);
				c.projectileStage = 1;
				c.specHit = true;
				c.gfx100(getRangeStartGFX());
				c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
				if(c.fightMode == 2)
					c.attackTimer--;
				if(c.playerIndex > 0)
					fireProjectilePlayer();
				else if(c.npcIndex > 0)
					fireProjectileNpc();
				c.specAccuracy = 1.75;
				c.specDamage = 1.50;
				break;
			case 13902: // Statius Warhammer.
			case 13904:
				c.startAnimation(10505);
				c.specHit = true;
				c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
				c.specDamage = 1.25;
				c.specEffect = 6;
				break;
			case 13899: // Vesta's Longsword.
			case 13901:
				c.specHit = true;
				c.startAnimation(10502);
				c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
				c.specAccuracy = 1.4;
				c.specDamage = 1.2;
				break;
		}
		c.delayedDamage = Misc.random(calculateMeleeMaxHit(i, c.npcIndex > 0));
		c.delayedDamage2 = Misc.random(calculateMeleeMaxHit(i, c.npcIndex > 0));
		c.usingSpecial = false;
		c.getItems().updateSpecialBar();
	}
	
	public void cannonExplosion(){
		int explode = (c.getLevelForXP(c.playerXP[c.playerFiremaking]) * 5) + 25;
		if(c.specHit)
			explode *= 0.5;
		if(Misc.random(explode) == 0){
			c.gfx0(2140);
			int damage = Misc.random(15) + 1;
			c.sendMessage("Your hand cannon has exploded!");
			if(c.playerEquipment[c.playerWeapon] == 15241)
				c.getItems().deleteEquipment(15241, c.playerWeapon);
			else
				c.inventory.deleteItem(15241, 1);
			if(c.playerLevel[c.playerHitpoints] < damage)
				damage = c.playerLevel[c.playerHitpoints];
			c.handleHitMask(damage);
			c.playerLevel[c.playerHitpoints] -= damage;
			int ten = (int)Math.round((double)c.getLevelForXP(c.playerXP[c.playerHitpoints]) * 0.1);
			if(c.playerLevel[c.playerHitpoints] < ten && c.prayerActive[22] && c.playerLevel[c.playerHitpoints] > 0)
				c.redemption();
		}
	}

	public void quincySpec(){
		c.specAmount = 0.0;
		c.getItems().addSpecialBar(20097);
		c.quincyStage = 0;
		c.specAccuracy = 6.0;
		c.usingQuincySpec = true;
		c.specDamage = 6.5;
		c.projectileStage = 1;
		c.playerLevel[c.playerDefence] -= c.playerLevel[c.playerDefence] * 0.25;
		c.playerLevel[c.playerRanged] -= c.playerLevel[c.playerRanged] * 0.25;
		int temp[] = {(int)(c.getLevelForXP(c.playerXP[c.playerDefence]) * 0.75), (int)(c.getLevelForXP(c.playerXP[c.playerRanged]) * 0.75)};
		if(c.playerLevel[c.playerDefence] < temp[0])
			c.playerLevel[c.playerDefence] = temp[0];
		if(c.playerLevel[c.playerRanged] < temp[1])
			c.playerLevel[c.playerRanged] = temp[1];
		c.getPA().refreshSkill(c.playerDefence);
		c.getPA().refreshSkill(c.playerRanged);
		c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
	}

	public boolean checkSpecAmount(int weapon){
		switch(weapon){
			case 1249:
				if(c.specAmount >= 2.5){
					if(c.duel != null && c.duel.rules != null && c.duel.rules.duelRule[1] && c.duel.status == 3)
						return true;
					c.specAmount -= 2.5;
					c.getItems().addSpecialBar(weapon);
					return true;
				}
				return false;
			case 20097:
				return c.specAmount >= 10.0;
			case 13899:
			case 13901:
			case 1215:
			case 1231:
			case 5680:
			case 5698:
			case 1305:
			case 1434:
				if(c.specAmount >= 2.5){
					c.specAmount -= 2.5;
					c.getItems().addSpecialBar(weapon);
					return true;
				}
				return false;
			case 13879:
			case 13880:
			case 13881:
			case 13882:
			case 13883:
			case 15241:
				if(c.specAmount >= 5.0){
					c.specAmount -= 5.0;
					c.getItems().addSpecialBar(weapon);
					return true;
				}
				return false;
			case 20098:
				if(c.specAmount >= 6){
					c.specAmount -= 7;
					c.getItems().addSpecialBar(weapon);
					return true;
				}
				return false;
			case 13902:
			case 13904:
				if(c.specAmount >= 3.5){
					c.specAmount -= 3.5;
					c.getItems().addSpecialBar(weapon);
					return true;
				}
				return false;
			case 19784:
			case 19780: // Korasi
				if(c.specAmount >= 7){
					c.specAmount -= 7;
					c.getItems().addSpecialBar(weapon);
					return true;
				}
				return false;
			case 24455:
				if(c.specAmount < 10)
					return false;
				c.specAmount = 0;
				c.getItems().addSpecialBar(weapon);
				return true;
			case 19146:
				if(c.specAmount >= 5.5){
					c.specAmount -= 5.5;
					c.getItems().addSpecialBar(weapon);
					return true;
				}
				return false;
			case 22346:
				if(c.specAmount >= 5){
					c.specAmount -= 5;
					c.getItems().addSpecialBar(weapon);
					return true;
				}
				return false;
			case 4151:
			case 15441:
			case 15442:
			case 15443:
			case 15444:
			case 11694:
			case 14484:
			case 11698:
			case 4153:
				if(c.specAmount >= 5){
					c.specAmount -= 5;
					c.getItems().addSpecialBar(weapon);
					return true;
				}
				return false;
			case 3204:
				if(c.specAmount >= 3){
					c.specAmount -= 3;
					c.getItems().addSpecialBar(weapon);
					return true;
				}
				return false;
			case 1377:
			case 11696:
			case 11730:
				if(c.specAmount >= 10){
					c.specAmount -= 10;
					c.getItems().addSpecialBar(weapon);
					return true;
				}
				return false;
			case 4587:
			case 859:
			case 861:
			case 11235:
			case 11700:
				if(c.specAmount >= 5.5){
					c.specAmount -= 5.5;
					c.getItems().addSpecialBar(weapon);
					return true;
				}
				return false;
			default:
				return true; // incase u want to test a weapon
		}
	}

	public void resetPlayerAttack(){
		c.usingMagic = false;
		c.npcIndex = 0;
		c.faceUpdate(0);
		c.playerIndex = 0;
		c.getPA().resetFollow();
		// c.sendMessage("Reset attack.");
	}

	public int getCombatDifference(int combat1, int combat2){
		if(combat1 > combat2){
			return (combat1 - combat2);
		}
		if(combat2 > combat1){
			return (combat2 - combat1);
		}
		return 0;
	}

	/**
	 * Get killer id
	 **/
	public int getKillerId(int playerId){
		int oldDamage = 0;
		int killerId = 0;
		for(Player p : RegionManager.getLocalPlayers(PlayerHandler.players[playerId])){
			if(p != null){
				if(p.killedBy == playerId){
					if(p.withinDistance(PlayerHandler.players[playerId])){
						if(p.totalPlayerDamageDealt > oldDamage){
							oldDamage = p.totalPlayerDamageDealt;
							killerId = p.playerId;
						}
					}
					p.totalPlayerDamageDealt = 0;
					p.killedBy = 0;
				}
			}
		}
		return killerId;
	}

	double[] prayerData = {1, // Thick Skin.
	1, // Burst of Strength.
	1, // Clarity of Thought.
	1, // Sharp Eye.
	1, // Mystic Will.
	2, // Rock Skin.
	2, // SuperHuman Strength.
	2, // Improved Reflexes.
	0.4, // Rapid restore.
	0.6, // Rapid Heal.
	0.6, // Protect Items.
	1.5, // Hawk eye.
	2, // Mystic Lore.
	4, // Steel Skin.
	4, // Ultimate Strength.
	4, // Incredible Reflexes.
	4, // Protect from Magic.
	4, // Protect from Missiles.
	4, // Protect from Melee.
	4, // Eagle Eye.
	4, // Mystic Might.
	1, // Retribution.
	2, // Redemption.
	6, // Smite.
	8, // Chivalry.
	8, // Piety.
	0.6, // Protect Item
	3, // Sap Warrior
	3, // Sap Ranger
	3, // Sap Mage
	3, // Sap Spirit
	0.6, // Berserker
	4, // Deflect Summoning
	4, // Deflect Magic
	4, // Deflect Missiles
	4, // Deflect Melee
	3.5, // Leech Attack
	3.5, // Leech Ranged
	3.5, // Leech Magic
	3.5, // Leech Defence
	3.5, // Leech Strength
	3.5, // Leech Energy
	3.5, // Leech Special
	0.9, // Wrath
	8, // SS
	8, // Turmoil
	};

	public void handlePrayerDrain(){
		c.usingPrayer = false;
		double toRemove = 0.0;
		for(int j = 0; j < prayerData.length; j++){
			if(c.prayerActive[j]){
				toRemove += prayerData[j] / 20;
				c.usingPrayer = true;
			}
		}
		if(toRemove > 0)
			toRemove /= (1 + (0.035 * c.playerBonus[11]));
		
		c.prayerPoint -= toRemove;
		if(c.prayerPoint <= 0){
			c.prayerPoint = 1.0 + c.prayerPoint;
			reducePrayerLevel();
		}
	}

	/**
	 * Wrath and Redemption.
	 */
	public void deathPrayer(){
		if(!c.prayerActive[21] && !c.prayerActive[43])
			return;
		c.gfx0(c.prayerActive[21] ? 437 : 2259);
		int length = c.prayerActive[21] ? 1 : 2;
		int damage = ((int)Math.round(((double)c.getLevelForXP(c.playerXP[c.playerPrayer]) * 2.5))) / 10;
		if(c.prayerActive[43])
			c.getPA().wrathGfx();
		if(c.duel != null && c.duel.status >= 3)
			return;
		if(CastleWars.isInCw(c) || c.inPits || (c.duel != null && c.duel.status == 3) || c.isInArd() || c.isInFala() || c.inWild()){
			Collection<Client> clients = c.getRegion().getPlayers();
			if(c.duel != null && c.duel.status > 1){
				Client o = c.duel.getOtherPlayer(c.playerId);
				if(o == null)
					return;
				if((o.absX <= c.absX + length && o.absX >= c.absX - length) && (o.absY <= c.absY + length && o.absY >= c.absY - length)){
					int dam = Misc.random(damage);
					o.dealDamage(dam);
				}
				return;
			}
			if(!c.inMulti()){
				Client o = (Client)PlayerHandler.players[c.killerId];
				if(o != null && (o.absX <= c.absX + length && o.absX >= c.absX - length) && (o.absY <= c.absY + length && o.absY >= c.absY - length)){
					int dam = Misc.random(damage);
					if(o.playerLevel[o.playerHitpoints] < dam)
						dam = o.playerLevel[o.playerHitpoints];
					o.dealDamage(dam);
					o.getPA().refreshSkill(o.playerHitpoints);
				}
				return;
			}
			for(Client o : clients){
				if(o == null || o.playerId == c.playerId)
					continue;
				if((o.absX <= c.absX + length && o.absX >= c.absX - length) && (o.absY <= c.absY + length && o.absY >= c.absY - length)){
					if((c.isInArd() || c.isInFala() || c.inWild()) && o.safeZone())
						continue;
					int dam = Misc.random(damage);
					if(o.playerLevel[o.playerHitpoints] < dam)
						dam = o.playerLevel[o.playerHitpoints];
					o.dealDamage(dam);
					o.getPA().refreshSkill(o.playerHitpoints);
				}
			}
		}
		Collection<NPC> npcs = c.getRegion().getNpcs();
		for(NPC npc : npcs){
			if(npc == null)
				continue;
			if((npc.absX <= c.absX + length && npc.absX >= c.absX - length) && (npc.absY <= c.absY + length && npc.absY >= c.absY - length)){
				int dam = Misc.random(damage);
				if(npc.HP < dam)
					dam = npc.HP;
				npc.hitDiff = dam;
				npc.HP -= dam;
				npc.hitUpdateRequired = true;
				if(!c.inMulti())
					return;
			}
		}
	}

	public void reducePrayerLevel(){
		boolean infinitePrayer = c.playerEquipment[c.playerArrows] == Config.ECTOPLASMATOR;
		if(infinitePrayer)
			return;
		if(c.playerLevel[5] - 1 > 0){
			c.playerLevel[5] -= 1;
		}else{
			c.sendMessage("Oh no ,you have run out of prayer points!");
			c.playerLevel[5] = 0;
			resetPrayers();
			c.prayerId = -1;
		}
		c.getPA().refreshSkill(5);
	}

	public void resetPrayers(){
		for(int i = 0; i < c.prayerActive.length; i++){
			if(i >= 27 && i <= 30)
				c.getPA().resetSap(i);
			if(i >= 36 && i <= 42)
				c.getPA().resetLeech(i);
			c.prayerActive[i] = false;
			c.getPA().sendConfig(c.PRAYER_GLOW[i], 0);
		}
		c.headIcon = -1;
		c.getQPH().togglePrayerButton();
		c.getPA().requestUpdates();
	}

	/**
	 * Wildy and duel info
	 **/

	public boolean checkReqs(){
		if(PlayerHandler.players[c.playerIndex] == null)
			return false;
		if(c.playerIndex == c.playerId)
			return false;
		Client o = (Client)PlayerHandler.players[c.playerIndex];
		if(o == null)
			return false;
		if(c.inCWBase || o.inCWBase || c.inCWJail || o.inCWJail)
			return false;
		if((c.inCwGame && !o.inCwGame) || (o.inCwGame && !c.inCwGame))
			return false;
		if(c.inPits && o.inPits)
			return true;
		if(c.playerEquipment[c.playerWeapon] == 20174){
			c.sendMessage("I probably shouldn't use this bow to fight...");
			return false;
		}
		if(c.inHowlOfDeath){
			if(!o.inHowlOfDeath){
				c.sendMessage("This is not your target.");
				return false;
			}
			if(c.howlOfDeath.getTargetId(c.playerId) != o.playerId){
				c.sendMessage("This is not your target.");
				return false;
			}
			return true;
		}
		if(o.inDuelArena() && (c.duel == null || (c.duel != null && c.duel.status < 3)) && !c.usingMagic){
			if(c.arenas() || (c.duel != null && c.duel.status >= 3)){
				c.sendMessage("You can't challenge inside the arena!");
				return false;
			}
			Duel.sendRequest(c, c.playerIndex);
			return false;
		}
		if(c.inClanWarsWait()){
			ClanWarsSettings.sendChallenge(c, (Client)PlayerHandler.players[c.playerIndex]);
			return false;
		}
		if(c.inClanWars() && c.inClanWars)
			return true;
		if(CastleWars.isInCw(c))
			return true;
		if(c.duel != null && c.duel.status == 3){
			if(c.duel.getOtherPlayer(c.playerId).playerId != c.playerIndex){
				c.sendMessage("This isn't your opponent!");
				return false;
			}else
				return true;
		}
		if(c.duel == null || (c.duel != null && c.duel.status < 3)){
			if(!o.inWild()){
				c.sendMessage("That player is not in the wilderness.");
				c.stopMovement();
				c.getCombat().resetPlayerAttack();
				return false;
			}
			if(o.safeZone()){
				c.sendMessage("This player is currently in a safe zone.");
				c.stopMovement();
				c.getCombat().resetPlayerAttack();
				return false;
			}
			if(!c.inWild()){
				c.sendMessage("You are not in the wilderness.");
				c.stopMovement();
				c.getCombat().resetPlayerAttack();
				return false;
			}
			if(c.safeZone()){
				c.sendMessage("You are standing in a safe zone.");
				c.stopMovement();
				c.getCombat().resetPlayerAttack();
				return false;
			}
			if(Config.COMBAT_LEVEL_DIFFERENCE && !c.inClanWars && !c.inCwGame){
				int combatDif1 = c.getCombat().getCombatDifference(c.combatLevel, o.combatLevel);
				if(combatDif1 > c.wildLevel || combatDif1 > o.wildLevel){
					c.sendMessage("Your combat level difference is too great to attack that player here.");
					c.stopMovement();
					c.getCombat().resetPlayerAttack();
					return false;
				}
			}

			if(Config.SINGLE_AND_MULTI_ZONES){
				if(!o.inMulti()){ // single combat zones
					if(o.underAttackBy != c.playerId && o.underAttackBy != 0){
						c.sendMessage("That player is already in combat.");
						c.stopMovement();
						c.getCombat().resetPlayerAttack();
						return false;
					}
					if(o.playerId != c.underAttackBy && c.underAttackBy != 0 || c.underAttackBy2 > 0){
						c.sendMessage("You are already in combat.");
						c.stopMovement();
						c.getCombat().resetPlayerAttack();
						return false;
					}
				}
			}
		}
		return true;
	}

	public boolean checkMultiBarrageReqs(int i){
		if(PlayerHandler.players[i] == null){
			return false;
		}
		if(i == c.playerId)
			return false;
		if(c.inPits && PlayerHandler.players[i].inPits)
			return true;
		if(!PlayerHandler.players[i].inMulti()){
			return false;
		}
		if(Config.COMBAT_LEVEL_DIFFERENCE && !c.inClanWars && !c.inCwGame){
			int combatDif1 = c.getCombat().getCombatDifference(c.combatLevel, PlayerHandler.players[i].combatLevel);
			if(combatDif1 > c.wildLevel || combatDif1 > PlayerHandler.players[i].wildLevel){
				c.sendMessage("Your combat level difference is too great to attack that player here.");
				return false;
			}
		}

		if(Config.SINGLE_AND_MULTI_ZONES){
			if(!PlayerHandler.players[i].inMulti()){ // single combat zones
				if(PlayerHandler.players[i].underAttackBy != c.playerId && PlayerHandler.players[i].underAttackBy != 0){
					return false;
				}
				if(PlayerHandler.players[i].playerId != c.underAttackBy && c.underAttackBy != 0){
					c.sendMessage("You are already in combat.");
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Weapon stand, walk, run, etc emotes
	 **/

	public void getPlayerAnimIndex(String weaponName){
		c.playerStandIndex = 0x328;
		c.playerTurnIndex = 0x337;
		c.playerWalkIndex = 0x333;
		c.playerTurn180Index = 0x334;
		c.playerTurn90CWIndex = 0x335;
		c.playerTurn90CCWIndex = 0x336;
		c.playerRunIndex = 0x338;
		if(c.playerEquipment[c.playerWeapon] == 24201)
			return;
		if(weaponName.contains("halberd") || weaponName.contains("guthan")){
			c.playerStandIndex = 809;
			c.playerWalkIndex = 1146;
			c.playerRunIndex = 1210;
			return;
		}
		if(weaponName.contains("sled")){
			c.playerStandIndex = 1461;
			c.playerWalkIndex = 1468;
			c.playerRunIndex = 1467;
			return;
		}
		if(weaponName.contains("chaotic maul")){
			c.playerStandIndex = 1662;
			c.playerWalkIndex = 1663;
			c.playerRunIndex = 1664;
			return;
		}
		if(weaponName.contains("dharok")){
			c.playerStandIndex = 0x811;
			c.playerWalkIndex = 0x67F;
			c.playerRunIndex = 0x680;
			return;
		}
		if(weaponName.contains("ahrim")){
			c.playerStandIndex = 809;
			c.playerWalkIndex = 1146;
			c.playerRunIndex = 1210;
			return;
		}
		if(weaponName.contains("verac")){
			c.playerStandIndex = 1832;
			c.playerWalkIndex = 1830;
			c.playerRunIndex = 1831;
			return;
		}
		if(weaponName.contains("wand") || weaponName.contains("staff")){
			c.playerStandIndex = 809;
			c.playerRunIndex = 1210;
			c.playerWalkIndex = 1146;
			return;
		}
		if(weaponName.contains("karil")){
			c.playerStandIndex = 2074;
			c.playerWalkIndex = 2076;
			c.playerRunIndex = 2077;
			return;
		}
		if(weaponName.contains("2h sword") || weaponName.contains("godsword") || weaponName.contains("saradomin sw")){
			c.playerStandIndex = 7047;
			c.playerWalkIndex = 7046;
			c.playerRunIndex = 7039;
			return;
		}
		if(weaponName.contains("bow")){
			c.playerStandIndex = 808;
			c.playerWalkIndex = 819;
			c.playerRunIndex = 824;
			return;
		}
		if(weaponName.toLowerCase().contains("katana")){
			c.playerStandIndex = 11973;
			c.playerWalkIndex = 11975;
			c.playerRunIndex = 11976;
		}
		switch(c.playerEquipment[c.playerWeapon]){
			case 4565:
				c.playerStandIndex = c.playerWalkIndex = c.playerRunIndex = 1836;
				break;
			case 1419: //Scythes
			case 22321:
				c.playerStandIndex = 847;
				break;
			case 4151:
			case 15441:
			case 15442:
			case 15443:
			case 15444:
				c.playerStandIndex = 11973;
				c.playerWalkIndex = 11975;
				c.playerRunIndex = 11976;
				break;
			case 15241:
				c.playerStandIndex = 12155;
				c.playerWalkIndex = 12154;
				c.playerRunIndex = 12154;
				break;
			case 24457:
				c.playerStandIndex = 809;
				c.playerRunIndex = 1210;
				c.playerWalkIndex = 1146;
				break;
			case 6528:
				c.playerStandIndex = 0x811;
				c.playerWalkIndex = 2064;
				c.playerRunIndex = 1664;
				break;
			case 18353:
			case 16425:
			case 4153:
				c.playerStandIndex = 1662;
				c.playerWalkIndex = 1663;
				c.playerRunIndex = 1664;
				break;
			case 10095:
				c.playerStandIndex = 7047;
				c.playerWalkIndex = 7046;
				c.playerRunIndex = 7039;
				break;
			case 11694:
			case 11696:
			case 11730:
			case 11698:
			case 11700:
				c.playerStandIndex = 4300;
				c.playerWalkIndex = 4306;
				c.playerRunIndex = 4305;
				break;
			case 1305:
				c.playerStandIndex = 809;
				break;
		}
	}

	public int getMageAnim(){
		switch(c.playerEquipment[c.playerWeapon]){
			case 24457: // Obliteration
				return 16963;
		}
		return c.MAGIC_SPELLS[c.spellId][2];
	}
	
	/**
	 * Weapon emotes
	 **/

	public int getWepAnim(String weaponName){
		if(c.playerEquipment[c.playerWeapon] <= 0){
			switch(c.fightMode){
				case 0:
					return 422;
				case 2:
					return 423;
				case 1:
					return 451;
			}
		}
		if(weaponName.contains("knife") || weaponName.contains("dart") || weaponName.contains("javelin") || weaponName.contains("thrownaxe")){
			return 806;
		}
		if(weaponName.contains("halberd")){
			return 440;
		}
		if(weaponName.startsWith("dragon dagger")){
			return 402;
		}
		if(weaponName.endsWith("dagger")){
			return 412;
		}
		if(weaponName.contains("2h sword") || weaponName.contains("godsword") || weaponName.contains("aradomin sword")){
			return 7041;
		}
		// 406 for slash down (katana).
		if(weaponName.contains("katana"))
			return 407;
		if(weaponName.contains("sword")){
			return 451;
		}
		if(weaponName.contains("karil")){
			return 2075;
		}
		if(weaponName.contains("bow") && !weaponName.contains("'bow") && !weaponName.toLowerCase().contains("chaotic")){
			return 426;
		}
		if(weaponName.contains("'bow"))
			return 4230;
		switch(c.playerEquipment[c.playerWeapon]){ // if you don't want to use strings
			case 20097:
				return 427;
			case 18349: // chaotic rapier
			case 16955: // primal rapier
				return 386;
			case 18357:
				return 4230;
			case 6522:
				return 2614;
			case 18353:
			case 16425:
			case 4153: // granite maul
				return 1665;
			case 4726: // guthan
				return 2080;
			case 4747: // torag
				return 0x814;
			case 4718: // dharok
				return 2067;
			case 4710: // ahrim
				return 406;
			case 4755: // verac
				return 2062;
			case 4734: // karil
				return 2075;
			case 1419: // scythes
			case 22321:
				return 407;
			case 24455:
				return 401;
			case 15241:
				return 12153;
			case 4151:
			case 15441:
			case 15442:
			case 15443:
			case 15444:
				return 11969;
			case 6528:
				return 2661;
			case 24456: // Decimation
				return 16962;
			case 19780:
			case 19784:
				return 12309;
			default:
				return 451;
		}
	}

	/**
	 * Block emotes
	 */
	public int getBlockEmote(){
		if(c.playerEquipment[c.playerShield] >= 8844 && c.playerEquipment[c.playerShield] <= 8850)
			return 4177;
		if(c.playerEquipment[c.playerWeapon] == 15241)
			return 1666;
		if(c.playerEquipment[c.playerShield] <= 0)
			return 404;
		return 1156;
	}

	/**
	 * Weapon and magic attack speed!
	 **/

	public int getAttackDelay(String s){
		int ret = 0;
		try{
			if(c.usingMagic){
				switch(c.MAGIC_SPELLS[c.spellId][0]){
					case 12871: // ice blitz
					case 13023: // shadow barrage
					case 12891: // ice barrage
						return (ret = 5);
					case 12435:
						return (ret = (c.playerEquipment[c.playerWeapon] == 21777 ? 4 : 5));
					default:
						return (ret = 5);
				}
			}
			if(c.playerEquipment[c.playerWeapon] == -1)
				return (ret = 4);// unarmed
	
			switch(c.playerEquipment[c.playerWeapon]){
				case 19784:
				case 19780:
				case 22404: // Silverlight empowered.
					return (ret = 4);
				case 24455:
				case 15773:
					return (ret = 6);
				case 20097:
					return (ret = 1);
				case 11235:
					return (ret = 9);
				case 11730:
					return (ret = 4);
				case 6528:
					return (ret = 7);
				case 18353:
				case 16425:
					return (ret = 7);
				case 16137:
					return (ret = 8);
				case 16955:
				case 18349:
					return (ret = 3);
				case 16403:
					return (ret = 5);
			}
	
			if(s.endsWith("greataxe"))
				return (ret = 7);
			else if(s.equals("torags hammers"))
				return (ret = 5);
			else if(s.equals("guthans warspear"))
				return (ret = 5);
			else if(s.equals("veracs flail"))
				return (ret = 5);
			else if(s.equals("ahrims staff"))
				return (ret = 6);
			else if(s.contains("staff")){
				if(s.contains("zamarok") || s.contains("guthix") || s.contains("saradomian") || s.contains("slayer") || s.contains("ancient"))
					return (ret = 4);
				else
					return (ret = 5);
			}else if(s.contains("bow")){
				if(s.contains("composite") || s.equals("seercull"))
					return (ret = 5);
				else if(s.contains("aril"))
					return (ret = 4);
				else if(s.contains("Ogre"))
					return (ret = 8);
				else if(s.contains("short") || s.contains("hunt") || s.contains("sword"))
					return (ret = 5);
				else if(s.contains("long") || s.contains("crystal"))
					return (ret = 6);
				else if(s.contains("'bow"))
					return (ret = 7);
				else if(s.contains("cannon"))
					return (ret = 9);
				return (ret = 5);
			}else if(s.contains("dagger"))
				return (ret = 4);
			else if(s.contains("godsword") || s.contains("2h"))
				return (ret = 6);
			else if(s.contains("longsword") || s.contains("katana") || (s.toLowerCase().contains("dominion") && s.toLowerCase().contains("sword")))
				return (ret = 5);
			else if(s.contains("sword"))
				return (ret = 4);
			else if(s.contains("scimitar"))
				return (ret = 4);
			else if(s.contains("mace"))
				return (ret = 5);
			else if(s.contains("battleaxe"))
				return (ret = 6);
			else if(s.contains("pickaxe"))
				return (ret = 5);
			else if(s.contains("thrownaxe"))
				return (ret = 5);
			else if(s.contains("axe"))
				return (ret = 5);
			else if(s.contains("warhammer"))
				return (ret = 6);
			else if(s.contains("2h"))
				return (ret = 7);
			else if(s.contains("spear"))
				return (ret = 5);
			else if(s.contains("claw"))
				return (ret = 4);
			else if(s.contains("halberd"))
				return (ret = 7);
	
			// sara sword, 2400ms
			else if(s.equals("granite maul"))
				return (ret = 7);
			else if(s.equals("toktz-xil-ak"))// sword
				return (ret = 4);
			else if(s.equals("tzhaar-ket-em"))// mace
				return (ret = 5);
			else if(s.equals("tzhaar-ket-om"))// maul
				return (ret = 7);
			else if(s.equals("toktz-xil-ek"))// knife
				return (ret = 4);
			else if(s.equals("toktz-xil-ul"))// rings
				return (ret = 4);
			else if(s.equals("toktz-mej-tal"))// staff
				return (ret = 6);
			else if(s.contains("whip"))
				return (ret = 4);
			else if(s.contains("dart"))
				return (ret = 3);
			else if(s.contains("knife"))
				return (ret = 3);
			else if(s.contains("javelin"))
				return (ret = 6);
			return (ret = 5);
		}finally{
			if(c.miasmicEffect && !c.usingMagic)
				return ((int)(ret * 1.5));
		}
	}

	/**
	 * How long it takes to hit your enemy
	 **/
	public int getHitDelay(String weaponName){
		if(c.usingMagic){
			switch(c.MAGIC_SPELLS[c.spellId][0]){
				case 12891:
					return 4;
				case 12871:
					return 6;
				default:
					return 4;
			}
		}else{
			if(weaponName.contains("knife") || weaponName.contains("dart") || weaponName.contains("javelin") || weaponName.contains("thrownaxe")){
				return 3;
			}
			if(weaponName.contains("cross") || weaponName.contains("c'bow")){
				return 4;
			}
			if(weaponName.contains("cannon") && !c.specHit){
				return c.fightMode == 2 ? 3 : 4;
			}else if(weaponName.contains("cannon")){
				return 6;
			}
			if(weaponName.contains("bow") && !c.dbowSpec){
				return 4;
			}else if(c.dbowSpec){
				return 4;
			}
			switch(c.playerEquipment[c.playerWeapon]){
				case 6522: // Toktz-xil-ul
					return 3;
				default:
					return 2;
			}
		}
	}

	public int getRequiredDistance(){
		if(c.followId > 0 && c.freezeTimer <= 0 && !c.isMoving)
			return 2;
		else if(c.followId > 0 && c.freezeTimer <= 0 && c.isMoving){
			return 3;
		}else{
			return 1;
		}
	}

	public boolean usingHally(){
		switch(c.playerEquipment[c.playerWeapon]){
			case 3190:
			case 3192:
			case 3194:
			case 3196:
			case 3198:
			case 3200:
			case 3202:
			case 3204:
				return true;

			default:
				return false;
		}
	}

	/**
	 * Melee
	 **/

	public int calculateMeleeAttack(){
		int attackLevel = c.playerLevel[0];
		int index = c.playerIndex > 0 ? c.playerIndex : c.npcIndex > 0 ? c.npcIndex : 0;
		boolean player = c.playerIndex > 0;
		if(player){
			if(PlayerHandler.players[index] == null)
				return 0;
		}else if(NPCHandler.npcs[index] == null)
			return 0;
		// 2, 5, 11, 18, 19
		if(c.prayerActive[2]){
			attackLevel += c.getLevelForXP(c.playerXP[c.playerAttack]) * 0.05;
		}else if(c.prayerActive[7]){
			attackLevel += c.getLevelForXP(c.playerXP[c.playerAttack]) * 0.1;
		}else if(c.prayerActive[15]){
			attackLevel += c.getLevelForXP(c.playerXP[c.playerAttack]) * 0.15;
		}else if(c.prayerActive[24]){
			attackLevel += c.getLevelForXP(c.playerXP[c.playerAttack]) * 0.20;
		}else if(c.prayerActive[25] || c.prayerActive[45]){
			attackLevel += ((c.getLevelForXP(c.playerXP[c.playerAttack]) * 0.15) + ((player ? PlayerHandler.players[index].combatLevel : NPCHandler.npcs[index].combat) * (index > 0 ? 0.15 : 0)));
		}
		if(c.fullVoidMelee())
			attackLevel += c.getLevelForXP(c.playerXP[c.playerAttack]) * 0.1;
		attackLevel *= c.specAccuracy;
		// c.sendMessage("Attack: " + (attackLevel +
		// (c.playerBonus[bestMeleeAtk()] * 2)));
		int i = c.playerBonus[bestMeleeAtk()];
		i += c.bonusAttack;
		if(c.playerEquipment[c.playerAmulet] == 11128 && c.playerEquipment[c.playerWeapon] == 6528){
			i *= 1.30;
		}
		return (int)(attackLevel + (attackLevel * 0.15) + (i + i * 0.05));
	}

	public int bestMeleeAtk(){
		int bonuses[] = {1, 2};
		int ret_bonus = 0;
		for(int bonus : bonuses)
			if(c.playerBonus[bonus] > c.playerBonus[ret_bonus])
				ret_bonus = bonus;
		return ret_bonus;
	}
	
	public double getPrayerBonus(){
		double prayerBonus = 1.0;
		int index = c.playerIndex > 0 ? c.playerIndex : c.npcIndex > 0 ? c.npcIndex : 0;
		if(c.prayerActive[1])
			prayerBonus = 1.05;
		else if(c.prayerActive[6])
			prayerBonus = 1.1;
		else if(c.prayerActive[14])
			prayerBonus = 1.15;
		else if(c.prayerActive[24])
			prayerBonus = 1.23;
		else if(c.prayerActive[25] || c.prayerActive[45]){
			int cmb = (c.playerIndex > 0 ? PlayerHandler.players[index].combatLevel : NPCHandler.npcs[index].combat);
			prayerBonus = 1 + ((23 + Math.floor((100 * Math.floor(0.15 * cmb)) / c.combatLevel)) / 100);
		}
		return prayerBonus;
	}
	
	public int getProbability(int i, boolean npc){
		Client o = (Client)PlayerHandler.players[i];
		if(o == null && !npc)
			return 1;
		int index = c.playerIndex > 0 ? c.playerIndex : c.npcIndex > 0 ? c.npcIndex : 0;
		boolean player = c.playerIndex > 0;
		if(player){
			if(PlayerHandler.players[index] == null)
				return 1;
		}else if(NPCHandler.npcs[index] == null)
			return 1;
		double attack = c.playerLevel[c.playerAttack] + Math.max(c.playerBonus[0], Math.max(c.playerBonus[1], c.playerBonus[2]));
		double defence = o.playerLevel[o.playerDefence] + Math.max(o.playerBonus[5], Math.max(o.playerBonus[6], o.playerBonus[7]));
		double A_Constant = 0.515;
		double hit_scalar = attack / defence;
		int probability = (int)(1 / (A_Constant * hit_scalar));
		return Misc.random(probability);
	}
	
	public int getMeleeDamage(int maxHit, int i, boolean npc){
		if(!Config.NEW_DAMAGE_CALCULATOR)
			return Misc.random(maxHit);
		Client o = (Client)PlayerHandler.players[i];
		if(o == null && !npc)
			return 0;
		int index = c.playerIndex > 0 ? c.playerIndex : c.npcIndex > 0 ? c.npcIndex : 0;
		boolean player = c.playerIndex > 0;
		if(player){
			if(PlayerHandler.players[index] == null)
				return 0;
		}else if(NPCHandler.npcs[index] == null)
			return 0;
		if(getProbability(i, npc) > 0)
			return 0;
		double atkLevel = (double)c.playerLevel[c.playerAttack];
        double defLevel = (double)(player ? o.playerLevel[o.playerDefence] : NPCHandler.npcs[index].defence);
        double atkLevelBonus = (atkLevel / defLevel) / 4D;
        double mean = maxHit * atkLevelBonus;
        double stan = mean;
        GaussianGenerator g = new GaussianGenerator(mean, stan, r);
        double randVal = g.nextValue().doubleValue();
        double finalHit = (mean + randVal) > maxHit ? maxHit : (mean + randVal) <= 0 ? 0 : (mean + randVal);
        return (int)Math.floor(finalHit + 0.5F);
	}
	
	public int calculateMeleeMaxHit(int i, boolean npc){
		Client o = (Client)PlayerHandler.players[i];
		if(o == null && !npc)
			return 0;
		int index = c.playerIndex > 0 ? c.playerIndex : c.npcIndex > 0 ? c.npcIndex : 0;
		boolean player = c.playerIndex > 0;
		if(player){
			if(PlayerHandler.players[index] == null)
				return 0;
		}else if(NPCHandler.npcs[index] == null)
			return 0;
		boolean veracsEffect = c.getPA().fullVeracs() && Misc.random(4) == 1;
		double maxHit = 0.0, effectiveStr = 0.0, baseDamage = 0.0;
		int strength = c.playerLevel[c.playerStrength];
		double strBonus = c.playerBonus[10];
		double prayerBonus = getPrayerBonus();
		effectiveStr = 8 + ((int)(strength * prayerBonus)) + 3;
		if(o != null){
			if(c.prayerActive[27]){
				if(c.sapTicks[0] > 0){
					c.sapTicks[0]--;
					if(c.sapTicks[0] == 5){
						c.startAnimation(12569);
						c.gfx0(c.sapGfx[0][0]);
						o.gfx0(c.sapGfx[0][1]);
						c.sapAmount[0] = 1.1;
					}
				}else{
					c.sapTicks[0] = 5;
					if(c.sapAmount[0] < 1.2)
						c.sapAmount[0] += 0.01;
					c.startAnimation(12569);
					c.gfx0(c.sapGfx[0][0]);
					o.gfx0(c.sapGfx[0][1]);
				}
				effectiveStr *= c.sapAmount[0];
			}else if(c.prayerActive[40]){
				if(c.leechTicks[4] > 0){
					c.leechTicks[4]--;
					if(c.leechTicks[4] == 5){
						c.startAnimation(12575);
						o.gfx0(c.leechGfx[4][0]);
						c.leechAmount[4] = 1.15;
					}
				}else{
					c.leechTicks[4] = 5;
					if(c.leechAmount[4] < 1.3)
						c.leechAmount[4] += 0.01;
					c.startAnimation(12569);
					o.gfx0(c.leechGfx[4][0]);
				}
				effectiveStr *= c.leechAmount[4];
			}
		}
		if(c.fullVoidMelee())
			effectiveStr *= 1.1;
		baseDamage = 5 + effectiveStr * (1 + (strBonus / 114));
		maxHit = baseDamage * (c.specDamage > 1 ? c.specDamage : 1);
		if(c.getPA().fullDharok())
			maxHit *= (double)(2.0 - (c.playerLevel[c.playerHitpoints] / c.getLevelForXP(c.playerXP[c.playerHitpoints])));
		if(npc){
			if(!veracsEffect){
				if(Misc.random(NPCHandler.npcs[i].defence) > 10 + Misc.random(calculateMeleeAttack())){
					maxHit = 0;
				}else if(NPCHandler.npcs[i].npcType == 2882 || NPCHandler.npcs[i].npcType == 2883){
					maxHit = 0;
				}
			}
		}else{
			if(Misc.random(o.getCombat().calculateMeleeDefence()) > Misc.random(calculateMeleeAttack()) && !veracsEffect){
				maxHit = 0;
				c.bonusAttack = 0;
			}else if(c.playerEquipment[c.playerWeapon] == 5698 && o.poisonDamage <= 0 && Misc.random(3) == 1){
				o.getPA().appendPoison(13);
				c.bonusAttack += maxHit / 3;
			}else
				c.bonusAttack += maxHit / 3;
		}
		return (int)Math.floor(maxHit / 10);
	}

	public int calculateMeleeMaxHit(int i, boolean npc, boolean old){
		Client o = (Client)PlayerHandler.players[i];
		if(o == null && !npc){
			return 0;
		}
		double maxHit = 0;
		boolean veracsEffect = c.getPA().fullVeracs() && Misc.random(4) == 1;
		int strBonus = c.playerBonus[10];
		int strength = c.playerLevel[2];
		int lvlForXP = c.getLevelForXP(c.playerXP[2]);
		int index = c.playerIndex > 0 ? c.playerIndex : c.npcIndex > 0 ? c.npcIndex : 0;
		boolean player = c.playerIndex > 0;
		if(player){
			if(PlayerHandler.players[index] == null)
				return 0;
		}else if(NPCHandler.npcs[index] == null)
			return 0;
		if(c.prayerActive[1]){
			strength += (int)(lvlForXP * .05);
		}else if(c.prayerActive[6]){
			strength += (int)(lvlForXP * .10);
		}else if(c.prayerActive[14]){
			strength += (int)(lvlForXP * .15);
		}else if(c.prayerActive[24]){
			strength += (int)(lvlForXP * .23);
		}else if(c.prayerActive[25] || c.prayerActive[45]){
			strength += (int)(lvlForXP * .23) + ((player ? PlayerHandler.players[index].combatLevel : NPCHandler.npcs[index].combat) * (index > 0 ? 0.10 : 0));
		}
		if(c.playerEquipment[c.playerHat] == 2526 && c.playerEquipment[c.playerChest] == 2520 && c.playerEquipment[c.playerLegs] == 2522){
			maxHit += (maxHit * 10 / 100);
		}
		maxHit += 1.05D + (double)(strBonus * strength) * 0.00175D;
		maxHit += (double)strength * 0.11D;
		if(c.playerEquipment[c.playerWeapon] == 4718 && c.playerEquipment[c.playerHat] == 4716 && c.playerEquipment[c.playerChest] == 4720 && c.playerEquipment[c.playerLegs] == 4722){
			maxHit += (c.getPA().getLevelForXP(c.playerXP[3]) - c.playerLevel[3]) / 2;
		}
		if(c.specDamage > 1)
			maxHit = (int)(maxHit * c.specDamage);
		if(maxHit < 0)
			maxHit = 1;
		if(c.fullVoidMelee())
			maxHit = (int)(maxHit * 1.10);
		if(c.playerEquipment[c.playerAmulet] == 11128 && c.playerEquipment[c.playerWeapon] == 6528){
			maxHit *= 1.20;
		}
		if(npc){
			if(!veracsEffect){
				if(Misc.random(NPCHandler.npcs[i].defence) > 10 + Misc.random(calculateMeleeAttack())){
					maxHit = 0;
				}else if(NPCHandler.npcs[i].npcType == 2882 || NPCHandler.npcs[i].npcType == 2883){
					maxHit = 0;
				}
			}
		}else{
			if(Misc.random(o.getCombat().calculateMeleeDefence()) > Misc.random(calculateMeleeAttack()) && !veracsEffect){
				maxHit = 0;
				c.bonusAttack = 0;
			}else if(c.playerEquipment[c.playerWeapon] == 5698 && o.poisonDamage <= 0 && Misc.random(3) == 1){
				o.getPA().appendPoison(13);
				c.bonusAttack += maxHit / 3;
			}else{
				c.bonusAttack += maxHit / 3;
			}
		}
		return (int)Math.floor(maxHit);
	}

	public int calculateMeleeDefence(){
		int defenceLevel = c.playerLevel[1];
		int i = c.playerBonus[bestMeleeDef()];
		int index = c.playerIndex > 0 ? c.playerIndex : c.npcIndex > 0 ? c.npcIndex : 0;
		boolean player = c.playerIndex > 0;
		if(player){
			if(PlayerHandler.players[index] == null)
				return 0;
		}else if(NPCHandler.npcs[index] == null)
			return 0;
		Client o = null;
		if(player)
			o = (Client)PlayerHandler.players[index];
		boolean sapDef = false;
		if(player){
			if(c.prayerActive[27]){
				defenceLevel *= c.sapAmount[0];
				sapDef = true;
			}
			if(c.prayerActive[28] && !sapDef){
				defenceLevel *= c.sapAmount[1];
				sapDef = true;
			}
			if(c.prayerActive[39]){
				if(c.leechTicks[3] > 0){
					c.leechTicks[3]--;
					if(c.leechTicks[3] == 5){
						c.startAnimation(12575);
						o.gfx0(c.leechGfx[3][0]);
						c.leechAmount[3] = 1.15;
					}
				}else{
					c.leechTicks[3] = 5;
					if(c.leechAmount[3] < 1.3)
						c.leechAmount[3] += 0.01;
					c.startAnimation(12569);
					o.gfx0(c.leechGfx[3][0]);
				}
				defenceLevel *= c.leechAmount[3];
			}
			if(c.prayerActive[29] && !sapDef){
				if(c.sapTicks[2] > 0){
					c.sapTicks[2]--;
					if(c.sapTicks[2] == 5){
						c.startAnimation(12569);
						c.gfx0(c.sapGfx[2][0]);
						o.gfx0(c.sapGfx[2][1]);
						c.sapAmount[2] = 0.9;
					}
				}else{
					c.sapTicks[2] = 5;
					if(c.sapAmount[2] > 0.8)
						c.sapAmount[2] -= 0.01;
					c.startAnimation(12569);
					c.gfx0(c.sapGfx[2][0]);
					o.gfx0(c.sapGfx[2][1]);
				}
				defenceLevel *= c.sapAmount[2];
			}
		}
		if(c.prayerActive[0]){
			defenceLevel += c.getLevelForXP(c.playerXP[c.playerDefence]) * 0.05;
		}else if(c.prayerActive[5]){
			defenceLevel += c.getLevelForXP(c.playerXP[c.playerDefence]) * 0.1;
		}else if(c.prayerActive[13]){
			defenceLevel += c.getLevelForXP(c.playerXP[c.playerDefence]) * 0.15;
		}else if(c.prayerActive[24]){
			defenceLevel += c.getLevelForXP(c.playerXP[c.playerDefence]) * 0.23;
		}else if(c.prayerActive[25] || c.prayerActive[45]){
			defenceLevel += ((c.getLevelForXP(c.playerXP[c.playerDefence]) * 0.27) + ((player ? PlayerHandler.players[index].combatLevel : NPCHandler.npcs[index].combat) * (index > 0 ? 0.10 : 0)));
		}
		return (int)(defenceLevel + (defenceLevel * 0.15) + (i + i * 0.05));
	}

	public int bestMeleeDef(){
		int bonuses[] = {6, 7};
		int ret_bonus = 5;
		for(int bonus : bonuses)
			if(c.playerBonus[bonus] > c.playerBonus[ret_bonus])
				ret_bonus = bonus;
		return ret_bonus;
	}

	/**
	 * Range
	 **/

	public int calculateRangeAttack(){
		int attackLevel = c.playerLevel[4];
		if(c.prayerActive[28] && c.playerIndex > 0)
			attackLevel *= c.sapAmount[1];
		attackLevel *= c.specAccuracy;
		if(c.fullVoidRange())
			attackLevel += c.getLevelForXP(c.playerXP[c.playerRanged]) * 0.1;
		if(c.prayerActive[3])
			attackLevel *= 1.05;
		else if(c.prayerActive[11])
			attackLevel *= 1.10;
		else if(c.prayerActive[19])
			attackLevel *= 1.15;
		// dbow spec
		if(c.fullVoidRange() && c.specAccuracy > 1.15){
			attackLevel *= 1.75;
		}
		return (int)(attackLevel + (c.playerBonus[4] * 1.95));
	}

	public int calculateRangeDefence(){
		int defenceLevel = c.playerLevel[1];
		if(c.prayerActive[0]){
			defenceLevel += c.getLevelForXP(c.playerXP[c.playerDefence]) * 0.05;
		}else if(c.prayerActive[5]){
			defenceLevel += c.getLevelForXP(c.playerXP[c.playerDefence]) * 0.1;
		}else if(c.prayerActive[13]){
			defenceLevel += c.getLevelForXP(c.playerXP[c.playerDefence]) * 0.15;
		}else if(c.prayerActive[24]){
			defenceLevel += c.getLevelForXP(c.playerXP[c.playerDefence]) * 0.2;
		}else if(c.prayerActive[25]){
			defenceLevel += c.getLevelForXP(c.playerXP[c.playerDefence]) * 0.25;
		}
		return (int)(defenceLevel + c.playerBonus[9] + (c.playerBonus[9] / 2));
	}

	public boolean usingBolts(){
		return c.playerEquipment[c.playerArrows] >= 9130 && c.playerEquipment[c.playerArrows] <= 9145 || c.playerEquipment[c.playerArrows] >= 9230 && c.playerEquipment[c.playerArrows] <= 9245;
	}

	public int rangeMaxHit(){
		if(c.usingQuincySpec)
			return 65;
		int rangeLevel = c.playerLevel[4];
		double modifier = 1.0;
		double wtf = c.specDamage;
		Client o = null;
		if(c.playerIndex > 0)
			o = (Client)PlayerHandler.players[c.playerIndex];
		int itemUsed = c.usingBow ? c.lastArrowUsed : c.lastWeaponUsed;
		if(o != null){
			if(c.prayerActive[28]){
				if(c.sapTicks[1] > 0){
					c.sapTicks[1]--;
					if(c.sapTicks[1] == 5){
						c.startAnimation(12569);
						c.gfx0(c.sapGfx[1][0]);
						o.gfx0(c.sapGfx[1][1]);
						c.sapAmount[1] = 1.1;
					}
				}else{
					c.sapTicks[1] = 5;
					if(c.sapAmount[1] < 1.2)
						c.sapAmount[1] += 0.01;
					c.startAnimation(12569);
					c.gfx0(c.sapGfx[1][0]);
					o.gfx0(c.sapGfx[1][1]);
				}
				rangeLevel *= c.sapAmount[1];
			}
		}
		if(o != null){
			if(c.prayerActive[37]){
				if(c.leechTicks[1] > 0){
					c.leechTicks[1]--;
					if(c.leechTicks[1] == 5){
						c.startAnimation(12569);
						c.gfx0(c.leechGfx[1][0]);
						o.gfx0(c.leechGfx[1][1]);
						c.leechAmount[1] = 1.15;
					}
				}else{
					c.leechTicks[1] = 5;
					if(c.leechAmount[1] > 1.3)
						c.leechAmount[1] += 0.01;
					c.startAnimation(12569);
					c.gfx0(c.leechGfx[1][0]);
					o.gfx0(c.leechGfx[1][1]);
				}
				rangeLevel *= c.leechAmount[1];
			}
		}
		if(c.prayerActive[3])
			modifier += 0.05;
		else if(c.prayerActive[11])
			modifier += 0.10;
		else if(c.prayerActive[19])
			modifier += 0.15;
		if(c.fullVoidRange())
			modifier += 0.20;

		double c = modifier * rangeLevel;
		int rangeStr = getRangeStr(itemUsed);
		double max = (c + 8) * (rangeStr + 64) / 640;
		if(wtf != 1)
			max *= wtf;
		if(max < 1)
			max = 1;
		return (int)max;
	}

	public int getRangeStr(int i){
		if(i == 4214 || i == 4215 || i == 4216 || i == 4217 || i == 4218 || i == 4219 || i == 4220 || i == 4221 || i == 4222 || i == 4223)
			return 70;
		if(c.playerEquipment[c.playerWeapon] == 17295){
			if(c.npcIndex > 0 || PlayerHandler.players[c.playerIndex] == null)
				return 70;
			return (int) (45 + (1.5 * PlayerHandler.players[c.playerIndex].playerLevel[c.playerMagic]));
		}
		if(c.playerEquipment[c.playerWeapon] == 15241)
			return 180;
		if(c.playerEquipment[c.playerWeapon] == 19146)
			return i == 19157 ? 125 : 105;
		if(c.playerEquipment[c.playerWeapon] == 20171 || c.playerEquipment[c.playerWeapon] == 20173)
			return 135;
		switch(i){
		// bronze to rune bolts
			case 877:
				return 10;
			case 9140:
				return 46;
			case 9141:
				return 64;
			case 9142:
			case 9241:
			case 9240:
				return 82;
			case 9143:
			case 9243:
			case 9242:
				return 100;
			case 13879:
			case 13880:
			case 13881:
			case 13882:
				return 65;
			case 13883:
				return 70;
			case 9144:
			case 9244:
			case 9245:
				return 115;
				// bronze to dragon arrows
			case 882:
				return 7;
			case 884:
				return 10;
			case 886:
				return 16;
			case 888:
				return 22;
			case 890:
				return 31;
			case 892:
			case 4740:
				return 49;
			case 11212:
				return 60;
				// knifes
			case 864:
				return 3;
			case 863:
				return 4;
			case 865:
				return 7;
			case 866:
				return 10;
			case 867:
				return 14;
			case 868:
				return 24;
		}
		return 0;
	}

	/*
	 * public int rangeMaxHit() { int rangehit = 0; rangehit += c.playerLevel[4]
	 * / 7.5; int weapon = c.lastWeaponUsed; int Arrows = c.lastArrowUsed; if
	 * (weapon == 4223) {//Cbow 1/10 rangehit = 2; rangehit += c.playerLevel[4]
	 * / 7; } else if (weapon == 4222) {//Cbow 2/10 rangehit = 3; rangehit +=
	 * c.playerLevel[4] / 7; } else if (weapon == 4221) {//Cbow 3/10 rangehit =
	 * 3; rangehit += c.playerLevel[4] / 6.5; } else if (weapon == 4220) {//Cbow
	 * 4/10 rangehit = 4; rangehit += c.playerLevel[4] / 6.5; } else if (weapon
	 * == 4219) {//Cbow 5/10 rangehit = 4; rangehit += c.playerLevel[4] / 6; }
	 * else if (weapon == 4218) {//Cbow 6/10 rangehit = 5; rangehit +=
	 * c.playerLevel[4] / 6; } else if (weapon == 4217) {//Cbow 7/10 rangehit =
	 * 5; rangehit += c.playerLevel[4] / 5.5; } else if (weapon == 4216) {//Cbow
	 * 8/10 rangehit = 6; rangehit += c.playerLevel[4] / 5.5; } else if (weapon
	 * == 4215) {//Cbow 9/10 rangehit = 6; rangehit += c.playerLevel[4] / 5; }
	 * else if (weapon == 4214) {//Cbow Full rangehit = 7; rangehit +=
	 * c.playerLevel[4] / 5; } else if (weapon == 6522) { rangehit = 5; rangehit
	 * += c.playerLevel[4] / 6; } else if (weapon == 9029) {//dragon darts
	 * rangehit = 8; rangehit += c.playerLevel[4] / 10; } else if (weapon == 811
	 * || weapon == 868) {//rune darts rangehit = 2; rangehit +=
	 * c.playerLevel[4] / 8.5; } else if (weapon == 810 || weapon == 867)
	 * {//adamant darts rangehit = 2; rangehit += c.playerLevel[4] / 9; } else
	 * if (weapon == 809 || weapon == 866) {//mithril darts rangehit = 2;
	 * rangehit += c.playerLevel[4] / 9.5; } else if (weapon == 808 || weapon ==
	 * 865) {//Steel darts rangehit = 2; rangehit += c.playerLevel[4] / 10; }
	 * else if (weapon == 807 || weapon == 863) {//Iron darts rangehit = 2;
	 * rangehit += c.playerLevel[4] / 10.5; } else if (weapon == 806 || weapon
	 * == 864) {//Bronze darts rangehit = 1; rangehit += c.playerLevel[4] / 11;
	 * } else if (Arrows == 4740 && weapon == 4734) {//BoltRacks rangehit = 3;
	 * rangehit += c.playerLevel[4] / 6; } else if (Arrows == 11212) {//dragon
	 * arrows rangehit = 4; rangehit += c.playerLevel[4] / 5.5; } else if
	 * (Arrows == 892) {//rune arrows rangehit = 3; rangehit += c.playerLevel[4]
	 * / 6; } else if (Arrows == 890) {//adamant arrows rangehit = 2; rangehit
	 * += c.playerLevel[4] / 7; } else if (Arrows == 888) {//mithril arrows
	 * rangehit = 2; rangehit += c.playerLevel[4] / 7.5; } else if (Arrows ==
	 * 886) {//steel arrows rangehit = 2; rangehit += c.playerLevel[4] / 8; }
	 * else if (Arrows == 884) {//Iron arrows rangehit = 2; rangehit +=
	 * c.playerLevel[4] / 9; } else if (Arrows == 882) {//Bronze arrows rangehit
	 * = 1; rangehit += c.playerLevel[4] / 9.5; } else if (Arrows == 9244) {
	 * rangehit = 8; rangehit += c.playerLevel[4] / 3; } else if (Arrows ==
	 * 9139) { rangehit = 12; rangehit += c.playerLevel[4] / 4; } else if
	 * (Arrows == 9140) { rangehit = 2; rangehit += c.playerLevel[4] / 7; } else
	 * if (Arrows == 9141) { rangehit = 3; rangehit += c.playerLevel[4] / 6; }
	 * else if (Arrows == 9142) { rangehit = 4; rangehit += c.playerLevel[4] /
	 * 6; } else if (Arrows == 9143) { rangehit = 7; rangehit +=
	 * c.playerLevel[4] / 5; } else if (Arrows == 9144) { rangehit = 7; rangehit
	 * += c.playerLevel[4] / 4.5; } int bonus = 0; bonus -= rangehit / 10;
	 * rangehit += bonus; if (c.specDamage != 1) rangehit *= c.specDamage; if
	 * (rangehit == 0) rangehit++; if (c.fullVoidRange()) { rangehit *= 1.10; }
	 * if (c.prayerActive[3]) rangehit *= 1.05; else if (c.prayerActive[11])
	 * rangehit *= 1.10; else if (c.prayerActive[19]) rangehit *= 1.15; return
	 * rangehit; }
	 */

	public boolean properBolts(){
		return c.playerEquipment[c.playerArrows] >= 9140 && c.playerEquipment[c.playerArrows] <= 9144 || c.playerEquipment[c.playerArrows] >= 9240 && c.playerEquipment[c.playerArrows] <= 9244;
	}

	public int correctBowAndArrows(){
		if(usingBolts())
			return -1;
		switch(c.playerEquipment[c.playerWeapon]){

			case 839:
			case 841:
				return 882;

			case 843:
			case 845:
				return 884;

			case 847:
			case 849:
				return 886;

			case 851:
			case 853:
				return 888;

			case 855:
			case 857:
				return 890;

			case 859:
			case 861:
				return 892;

			case 4734:
			case 4935:
			case 4936:
			case 4937:
				return 4740;
			case 24456:
			case 11235:
			case 17295:
				return 11212;
			case 19146:
				return 19157;
		}
		return -1;
	}

	public int getRangeStartGFX(){
		switch(c.rangeItemUsed){
			case 19157:
				return 95;
			case 863:
				return 220;
			case 864:
				return 219;
			case 865:
				return 221;
			case 866: // knives
				return 223;
			case 867:
				return 224;
			case 868:
				return 225;
			case 869:
				return 222;
			case 15243:
				return 2138;
			case 806:
				return 232;
			case 807:
				return 233;
			case 808:
				return 234;
			case 809: // darts
				return 235;
			case 810:
				return 236;
			case 811:
				return 237;

			case 825:
				return 206;
			case 826:
				return 207;
			case 827: // javelin
				return 208;
			case 828:
				return 209;
			case 829:
				return 210;
			case 830:
				return 211;

			case 800:
				return 42;
			case 801:
				return 43;
			case 802:
				return 44; // axes
			case 803:
				return 45;
			case 804:
				return 46;
			case 805:
				return 48;

			case 882:
				return 19;

			case 884:
				return 18;

			case 886:
				return 20;

			case 888:
				return 21;

			case 890:
				return 22;

			case 892:
				return 24;

			case 11212:
				return 26;

			case 20097:
			case 4212:
			case 4214:
			case 4215:
			case 4216:
			case 4217:
			case 4218:
			case 4219:
			case 4220:
			case 4221:
			case 4222:
			case 4223:
				return 250;
			case 20171:
			case 20173:
				c.usingBow = false;
				return 1065;
		}
		return -1;
	}

	public int getRangeProjectileGFX(){
		if(c.dbowSpec){
			return 672;
		}
		if(c.bowSpecShot > 0){
			switch(c.rangeItemUsed){
				default:
					return 249;
			}
		}
		if(c.playerEquipment[c.playerWeapon] == 9185)
			return 27;
		switch(c.rangeItemUsed){
			case 19157:
				return 98;
			case 863:
				return 213;
			case 864:
				return 212;
			case 865:
				return 214;
			case 866: // knives
				return 216;
			case 867:
				return 217;
			case 868:
				return 218;
			case 869:
				return 215;

			case 806:
				return 226;
			case 807:
				return 227;
			case 808:
				return 228;
			case 809: // darts
				return 229;
			case 810:
				return 230;
			case 811:
				return 231;

			case 825:
				return 200;
			case 826:
				return 201;
			case 827: // javelin
				return 202;
			case 828:
				return 203;
			case 829:
				return 204;
			case 830:
				return 205;

			case 6522: // Toktz-xil-ul
				return 442;

			case 800:
				return 36;
			case 801:
				return 35;
			case 802:
				return 37; // axes
			case 803:
				return 38;
			case 804:
				return 39;
			case 805:
				return 40;

			case 882:
				return 10;

			case 884:
				return 9;

			case 886:
				return 11;

			case 888:
				return 12;

			case 890:
				return 13;

			case 892:
				return 15;

			case 11212:
				return 17;

			case 4740: // bolt rack
				return 27;

			case 20097:
			case 4212:
			case 4214:
			case 4215:
			case 4216:
			case 4217:
			case 4218:
			case 4219:
			case 4220:
			case 4221:
			case 4222:
			case 4223:
				return 249;
			case 20171:
			case 20173:
				return 1066;
		}
		return -1;
	}

	public int getProjectileSpeed(){
		if(c.dbowSpec)
			return 100;
		return 70;
	}

	public int getProjectileShowDelay(){
		switch(c.playerEquipment[c.playerWeapon]){
			case 863:
			case 864:
			case 865:
			case 866: // knives
			case 867:
			case 868:
			case 869:

			case 806:
			case 807:
			case 808:
			case 809: // darts
			case 810:
			case 811:

			case 825:
			case 826:
			case 827: // javelin
			case 828:
			case 829:
			case 830:

			case 800:
			case 801:
			case 802:
			case 803: // axes
			case 804:
			case 805:

			case 4734:
			case 9185:
			case 4935:
			case 4936:
			case 4937:
				return 15;

			default:
				return 5;
		}
	}

	/**
	 * MAGIC
	 **/

	public int mageAtk(){
		int attackLevel = c.playerLevel[6];
		if(c.fullVoidMage())
			attackLevel += c.getLevelForXP(c.playerXP[6]) * 0.2;
		if(c.playerEquipment[c.playerWeapon] == 21777)
			attackLevel *= 1.15;
		if(c.prayerActive[4])
			attackLevel *= 1.05;
		else if(c.prayerActive[12])
			attackLevel *= 1.10;
		else if(c.prayerActive[20])
			attackLevel *= 1.15;
		return (int)(attackLevel + (c.playerBonus[3] * 2));
	}

	public int mageDef(){
		int defenceLevel = c.playerLevel[1] / 2 + c.playerLevel[6] / 2;
		if(c.prayerActive[0]){
			defenceLevel += c.getLevelForXP(c.playerXP[c.playerDefence]) * 0.05;
		}else if(c.prayerActive[3]){
			defenceLevel += c.getLevelForXP(c.playerXP[c.playerDefence]) * 0.1;
		}else if(c.prayerActive[9]){
			defenceLevel += c.getLevelForXP(c.playerXP[c.playerDefence]) * 0.15;
		}else if(c.prayerActive[18]){
			defenceLevel += c.getLevelForXP(c.playerXP[c.playerDefence]) * 0.2;
		}else if(c.prayerActive[19]){
			defenceLevel += c.getLevelForXP(c.playerXP[c.playerDefence]) * 0.25;
		}
		return (int)(defenceLevel + c.playerBonus[8] + (c.playerBonus[8] / 3));
	}

	public boolean wearingStaff(int runeId){
		int wep = c.playerEquipment[c.playerWeapon];
		int shield = c.playerEquipment[c.playerShield];
		switch(runeId){
			case 554:
				if(wep == 1387)
					return true;
				break;
			case 555:
				if(wep == 1383 || shield == 18346)
					return true;
				break;
			case 556:
				if(wep == 1381 || wep == 21777)
					return true;
				break;
			case 557:
				if(wep == 1385)
					return true;
				break;
		}
		return false;
	}

	public boolean checkMagicReqs(int spell){
		boolean checkRunes = true;
		if(c.usingMagic && Config.RUNES_REQUIRED){ // check for runes
			if(c.playerEquipment[c.playerWeapon] == 4675 && c.playerMagicBook == Config.ANCIENT)
				checkRunes = !c.freeAncientCast;
			if(checkRunes){
				if((!c.inventory.hasItem(c.MAGIC_SPELLS[spell][8], c.MAGIC_SPELLS[spell][9]) && !wearingStaff(c.MAGIC_SPELLS[spell][8])) || (!c.inventory.hasItem(c.MAGIC_SPELLS[spell][10], c.MAGIC_SPELLS[spell][11]) && !wearingStaff(c.MAGIC_SPELLS[spell][10])) || (!c.inventory.hasItem(c.MAGIC_SPELLS[spell][12], c.MAGIC_SPELLS[spell][13]) && !wearingStaff(c.MAGIC_SPELLS[spell][12])) || (!c.inventory.hasItem(c.MAGIC_SPELLS[spell][14], c.MAGIC_SPELLS[spell][15]) && !wearingStaff(c.MAGIC_SPELLS[spell][14]))){
					c.sendMessage("You don't have the required runes to cast this spell.");
					return false;
				}
			}
		}

		if(c.usingMagic && c.playerIndex > 0){
			if(PlayerHandler.players[c.playerIndex] != null){
				for(int r = 0; r < c.REDUCE_SPELLS.length; r++){ // reducing spells, confuse etc
					if(PlayerHandler.players[c.playerIndex].REDUCE_SPELLS[r] == c.MAGIC_SPELLS[spell][0]){
						c.reduceSpellId = r;
						if((System.currentTimeMillis() - PlayerHandler.players[c.playerIndex].reduceSpellDelay[c.reduceSpellId]) > PlayerHandler.players[c.playerIndex].REDUCE_SPELL_TIME[c.reduceSpellId]){
							PlayerHandler.players[c.playerIndex].canUseReducingSpell[c.reduceSpellId] = true;
						}else{
							PlayerHandler.players[c.playerIndex].canUseReducingSpell[c.reduceSpellId] = false;
						}
						break;
					}
				}
				if(!PlayerHandler.players[c.playerIndex].canUseReducingSpell[c.reduceSpellId]){
					c.sendMessage("That player is currently immune to this spell.");
					c.usingMagic = false;
					c.stopMovement();
					resetPlayerAttack();
					return false;
				}
			}
		}

		int staffRequired = getStaffNeeded();
		if(c.usingMagic && staffRequired > 0 && Config.RUNES_REQUIRED){ // staff required
			if(c.playerEquipment[c.playerWeapon] != staffRequired){
				c.sendMessage("You need a " + c.getItems().getItemName(staffRequired).toLowerCase() + " to cast this spell.");
				return false;
			}
		}

		if(c.usingMagic && Config.MAGIC_LEVEL_REQUIRED){ // check magic level
			if(c.playerLevel[6] < c.MAGIC_SPELLS[spell][1]){
				c.sendMessage("You need to have a magic level of " + c.MAGIC_SPELLS[spell][1] + " to cast this spell.");
				return false;
			}
		}
		if(c.usingMagic && Config.RUNES_REQUIRED && checkRunes){
			if(c.MAGIC_SPELLS[spell][8] > 0){ // deleting runes
				if(!wearingStaff(c.MAGIC_SPELLS[spell][8]))
					c.inventory.deleteItem(c.MAGIC_SPELLS[spell][8], c.inventory.findItemSlot(c.MAGIC_SPELLS[spell][8]), c.MAGIC_SPELLS[spell][9]);
			}
			if(c.MAGIC_SPELLS[spell][10] > 0){
				if(!wearingStaff(c.MAGIC_SPELLS[spell][10]))
					c.inventory.deleteItem(c.MAGIC_SPELLS[spell][10], c.inventory.findItemSlot(c.MAGIC_SPELLS[spell][10]), c.MAGIC_SPELLS[spell][11]);
			}
			if(c.MAGIC_SPELLS[spell][12] > 0){
				if(!wearingStaff(c.MAGIC_SPELLS[spell][12]))
					c.inventory.deleteItem(c.MAGIC_SPELLS[spell][12], c.inventory.findItemSlot(c.MAGIC_SPELLS[spell][12]), c.MAGIC_SPELLS[spell][13]);
			}
			if(c.MAGIC_SPELLS[spell][14] > 0){
				if(!wearingStaff(c.MAGIC_SPELLS[spell][14]))
					c.inventory.deleteItem(c.MAGIC_SPELLS[spell][14], c.inventory.findItemSlot(c.MAGIC_SPELLS[spell][14]), c.MAGIC_SPELLS[spell][15]);
			}
		}
		if(c.playerEquipment[c.playerWeapon] == 4675 && c.playerMagicBook == Config.ANCIENT)
			c.freeAncientCast = !c.freeAncientCast;
		return true;
	}

	public int getFreezeTime(){
		switch(c.MAGIC_SPELLS[c.oldSpellId][0]){
			case 1572:
			case 12861: // ice rush
				return 10;

			case 1582:
			case 12881: // ice burst
				return 17;

			case 1592:
			case 12871: // ice blitz
				return 25;

			case 12891: // ice barrage
				return 33;

			default:
				return 0;
		}
	}

	public void freezePlayer(int i){

	}

	public int getStartHeight(){
		switch(c.MAGIC_SPELLS[c.spellId][0]){
			case 1562: // stun
				return 25;

			case 12939:// smoke rush
				return 35;

			case 12987: // shadow rush
				return 38;

			case 12861: // ice rush
				return 15;

			case 12951: // smoke blitz
				return 38;

			case 12999: // shadow blitz
				return 25;

			case 12911: // blood blitz
				return 25;

			default:
				return 43;
		}
	}

	public int getEndHeight(){
		switch(c.MAGIC_SPELLS[c.spellId][0]){
			case 1562: // stun
				return 10;

			case 12939: // smoke rush
				return 20;

			case 12987: // shadow rush
				return 28;

			case 12861: // ice rush
				return 10;

			case 12951: // smoke blitz
				return 28;

			case 12999: // shadow blitz
				return 15;

			case 12911: // blood blitz
				return 10;

			default:
				return 31;
		}
	}

	public int getStartDelay(){
		if(c.playerEquipment[c.playerWeapon] == 15241)
			return 30;
		switch(c.MAGIC_SPELLS[c.spellId][0]){
			case 1539:
				return 60;

			default:
				return 53;
		}
	}

	public int getStaffNeeded(){
		switch(c.MAGIC_SPELLS[c.spellId][0]){
			case 1539:
				return 1409;

			case 12037:
				return 4170;

			case 1190:
				return 2415;

			case 1191:
				return 2416;

			case 1192:
				return 2417;

			default:
				return 0;
		}
	}

	public boolean godSpells(){
		switch(c.MAGIC_SPELLS[c.spellId][0]){
			case 1190:
				return true;

			case 1191:
				return true;

			case 1192:
				return true;

			default:
				return false;
		}
	}

	public int getEndGfxHeight(){
		switch(c.MAGIC_SPELLS[c.oldSpellId][0]){
			case 12987:
			case 12901:
			case 12861:
			case 12445:
			case 1192:
			case 13011:
			case 12919:
			case 12881:
			case 12999:
			case 12911:
			case 12871:
			case 13023:
			case 12929:
			case 12891:
				return 0;

			default:
				return 100;
		}
	}

	public int getStartGfxHeight(){
		switch(c.MAGIC_SPELLS[c.spellId][0]){
			case 12871:
			case 12891:
			case 12435:
				return 0;

			default:
				return 100;
		}
	}

	public void handleDfs(){
		if(!c.isDead2 && System.currentTimeMillis() - c.dfsDelay > 60000){
			if(c.playerIndex > 0 && PlayerHandler.players[c.playerIndex] != null){
				Client o = (Client)PlayerHandler.players[c.playerIndex];
				if(c.inDuelArena() || o.inDuelArena()){
					if((c.duel != null && c.duel.status < 3) || (o.duel != null && o.duel.status < 3))
						return;
					if(c.duel != null && !c.duel.duelingWith(c, o.playerId)){
						c.sendMessage("This is not your opponent!");
						return;
					}
				}
				int damage = Misc.random(15) + 5;
				damage = damage > o.playerLevel[3] ? o.playerLevel[3] : damage;
				c.startAnimation(6696);
				c.gfx0(1165);
				// Projectile 1166
				o.gfx100(1167);
				o.dealDamage(damage);
				o.damageTaken[c.playerId] += damage;
				o.getPA().refreshSkill(3);
				c.dfsDelay = System.currentTimeMillis();
				c.dfsCharges--;
				if(c.dfsCharges == 0){
					c.getItems().deleteEquipment(1, c.playerShield);
					c.getItems().setEquipment(11284, 1, c.playerShield);
				}
				c.getItems().resetBonus();
				c.getItems().getBonus();
				c.getItems().writeBonus();
			}else{
				c.sendMessage("I should be in combat before using this.");
			}
		}else{
			c.sendMessage("My shield hasn't finished recharging yet.");
		}
	}

	public void handleZerker(){

		if(c.isDonator == 1){
			if(System.currentTimeMillis() - c.dfsDelay > 60000){
				if(c.playerIndex > 0 && PlayerHandler.players[c.playerIndex] != null){
					int damage = Misc.random(10) + 7;
					c.startAnimation(369);
					c.gfx0(369);
					PlayerHandler.players[c.playerIndex].playerLevel[3] -= damage;
					PlayerHandler.players[c.playerIndex].hitDiff2 = damage;
					c.forcedText = "Feel the power of the Berserker Ring!";
					PlayerHandler.players[c.playerIndex].hitUpdateRequired2 = true;
					PlayerHandler.players[c.playerIndex].updateRequired = true;
					c.dfsDelay = System.currentTimeMillis();
				}else{
					c.sendMessage("I should be in combat before using this.");
				}
			}else{
				c.sendMessage("My ring hasn't finished recharging yet (60 Seconds)");
			}
			if(c.isDonator == 0)
				c.sendMessage("Only Donators can use the ring's Special attack");
		}
	}

	public void handleWarrior(){
		if(c.isDonator == 1){
			if(System.currentTimeMillis() - c.dfsDelay > 60000){
				if(c.playerIndex > 0 && PlayerHandler.players[c.playerIndex] != null){
					int damage = Misc.random(10) + 7;
					c.startAnimation(369);
					c.gfx0(369);
					PlayerHandler.players[c.playerIndex].playerLevel[3] -= damage;
					c.forcedText = "Feel the power of the Warrior Ring!";
					PlayerHandler.players[c.playerIndex].hitDiff2 = damage;
					PlayerHandler.players[c.playerIndex].hitUpdateRequired2 = true;
					PlayerHandler.players[c.playerIndex].updateRequired = true;
					c.dfsDelay = System.currentTimeMillis();
				}else{
					c.sendMessage("I should be in combat before using this.");
				}
			}else{
				c.sendMessage("My ring hasn't finished recharging yet (60 Seconds)");
			}
			if(c.isDonator == 0)
				c.sendMessage("Only Donators can use the ring's Special attack");
		}
	}

	public void handleSeers(){
		/*
		 * 
		 * c.castingMagic = true; if(c.isDonator == 1){ if
		 * (System.currentTimeMillis() - c.dfsDelay > 60000) { if (c.playerIndex
		 * > 0 && PlayerHandler.players[c.playerIndex] != null) { int damage =
		 * Misc.random(10) + 7; c.startAnimation(1979);
		 * PlayerHandler.players[c.playerIndex].gfx0(369); c.gfx0(368);
		 * PlayerHandler.players[c.playerIndex].freezeTimer = 15;
		 * PlayerHandler.players[c.playerIndex].resetWalkingQueue();
		 * PlayerHandler.players[c.playerIndex].frozenBy = c.playerId;
		 * PlayerHandler.players[c.playerIndex].playerLevel[3] -= damage;
		 * c.forcedText = ("Feel the power of the Seers Ring!");
		 * PlayerHandler.players[c.playerIndex].hitDiff2 = damage;
		 * 
		 * PlayerHandler.players[c.playerIndex].hitUpdateRequired2 = true;
		 * PlayerHandler.players[c.playerIndex].updateRequired = true;
		 * c.dfsDelay = System.currentTimeMillis(); } else {
		 * c.sendMessage("I should be in combat before using this."); } } else {
		 * c.sendMessage("My ring hasn't finished recharging yet (45 Seconds)");
		 * }if (c.isDonator == 0)
		 */
		c.sendMessage("Spec comes back soon");

	}

	public void handleArcher(){
		if(c.isDonator == 1){
			if(System.currentTimeMillis() - c.dfsDelay > 60000){
				if(c.playerIndex > 0 && PlayerHandler.players[c.playerIndex] != null){
					int damage = Misc.random(10) + 7;
					c.startAnimation(369);
					c.gfx0(369);
					PlayerHandler.players[c.playerIndex].playerLevel[3] -= damage;
					PlayerHandler.players[c.playerIndex].hitDiff2 = damage;
					c.forcedText = "Feel the power of the Archer Ring!";
					PlayerHandler.players[c.playerIndex].hitUpdateRequired2 = true;
					PlayerHandler.players[c.playerIndex].updateRequired = true;
					c.dfsDelay = System.currentTimeMillis();
				}else{
					c.sendMessage("I should be in combat before using this.");
				}
			}else{
				c.sendMessage("My ring hasn't finished recharging yet (60 Seconds)");
			}
			if(c.isDonator == 0)
				c.sendMessage("Only Donators can use the ring's Special attack");
		}
	}

	public void handleDfsNPC(){
		if(!c.isDead2 && System.currentTimeMillis() - c.dfsDelay > 30000){
			if(c.npcIndex > 0 && NPCHandler.npcs[c.npcIndex] != null){
				int damage = Misc.random(15) + 5;
				c.startAnimation(6696);
				c.gfx0(1165);
				// Projectile 11666
				NPCHandler.npcs[c.npcIndex].gfx100(1167);
				NPCHandler.npcs[c.npcIndex].HP -= damage;
				NPCHandler.npcs[c.npcIndex].hitDiff2 = damage;
				NPCHandler.npcs[c.npcIndex].hitUpdateRequired2 = true;
				NPCHandler.npcs[c.npcIndex].updateRequired = true;
				c.dfsDelay = System.currentTimeMillis();
				c.dfsCharges--;
				if(c.dfsCharges == 0){
					c.getItems().deleteEquipment(1, c.playerShield);
					c.getItems().setEquipment(11284, 1, c.playerShield);
				}
				c.getItems().resetBonus();
				c.getItems().getBonus();
				c.getItems().writeBonus();
			}else{
				c.sendMessage("I should be in combat before using this.");
			}
		}else{
			c.sendMessage("My shield hasn't finished recharging yet.");
		}
	}

	public void applyRecoil(int damage, int i){
		if(damage > 0 && PlayerHandler.players[i].playerEquipment[c.playerRing] == 2550){
			int recDamage = damage / 10 + 1;
			if(c.playerEquipment[c.playerWeapon] == 24201 || c.playerEquipment[c.playerWeapon] == 19784)
				recDamage = 0;
			if(!c.getHitUpdateRequired()){
				c.setHitDiff(recDamage);
				c.setHitUpdateRequired(true);
			}else if(!c.getHitUpdateRequired2()){
				c.setHitDiff2(recDamage);
				c.setHitUpdateRequired2(true);
			}
			c.dealDamage(recDamage);
			c.updateRequired = true;
		}
	}

	public int getBonusAttack(int i){
		switch(NPCHandler.npcs[i].npcType){
			case 2883:
				return Misc.random(50) + 30;
			case 2026:
			case 2027:
			case 2029:
			case 2030:
				return Misc.random(50) + 30;
		}
		return 0;
	}

	public void handleGmaulPlayer(){
		if(c.playerIndex > 0){
			Client o = (Client)PlayerHandler.players[c.playerIndex];
			if(c.goodDistance(c.getX(), c.getY(), o.getX(), o.getY(), getRequiredDistance())){
				boolean reqs = false;
				try{
					reqs = checkReqs();
				}catch(Exception e){
				}
				if(reqs){
					if(checkSpecAmount(4153) && c.isDead == false){
						boolean hit = Misc.random(calculateMeleeAttack()) > Misc.random(o.getCombat().calculateMeleeDefence());
						int damage = 0;
						if(hit)
							damage = Misc.random(calculateMeleeMaxHit(c.playerIndex, false));
						if((o.prayerActive[18] || o.prayerActive[35]) && System.currentTimeMillis() - o.protMeleeDelay > 1500)
							damage *= .6;
						c.startAnimation(1667);
						c.gfx100(340);
						o.dealDamage(damage);
						o.damageTaken[c.playerId] += damage;
						o.getPA().refreshSkill(3);
					}
				}else
					c.playerIndex = 0;
			}
		}
	}
	
	public void handleGmaulNPC(){
		if(c.npcIndex > 0){
			NPC o = NPCHandler.npcs[c.npcIndex];
			if(c.goodDistance(c.getX(), c.getY(), o.getX(), o.getY(), getRequiredDistance())){
				if(o.underAttackBy > 0 && o.underAttackBy != c.playerId && !o.inMulti()){
					c.npcIndex = 0;
					c.sendMessage("This monster is already in combat.");
					return;
				}
				if((c.underAttackBy > 0 || c.underAttackBy2 > 0) && c.underAttackBy2 != c.npcIndex && !c.inMulti()){
					resetPlayerAttack();
					c.sendMessage("I am already under attack.");
					return;
				}
				if(checkSpecAmount(4153) && c.isDead == false){
					int damage = 0;
					damage = Misc.random(calculateMeleeMaxHit(c.npcIndex, true));
					c.startAnimation(1667);
					c.gfx100(340);
					o.underAttack = true;
					o.hitDiff = damage;
					o.HP -= damage;
				}
			}
		}
	}

	public boolean armaNpc(int i){
		switch(NPCHandler.npcs[i].npcType){
			case 6222:
			case 6223:
			case 6225:
			case 6227:
				return true;
		}
		return false;
	}
}