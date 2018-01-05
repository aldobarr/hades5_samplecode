package server.model.npcs;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

import server.model.minigames.NexGames;
import server.model.minigames.Zombies;
import server.model.players.Client;
import server.model.region.Location;
import server.model.region.Region;
import server.model.region.RegionManager;
import server.util.Misc;
import server.util.Stream;

public class NPC{
	public static final int INVULNERABLE_TICK = 13;
	public int npcId;
	public Queue<Integer> moveXQueue = new LinkedList<Integer>();
	public Queue<Integer> moveYQueue = new LinkedList<Integer>();
	public Zombies zombies = null;
	public NexGames nexGames = null;
	public boolean isZombieNPC = false, isNexGamesNPC = false, isCapeNPC = false;
	public int nexStage = 0, nexSpawn = 0, nexSpawnTick = 0;
	public NPC siphons[] = {null, null, null};
	public Client clientSpawner;
	public int effectTick = 0;
	public boolean npcObject = false, exploded = false, healAttack = false, ignoreClip = false, isDelrith = false, isWeakDelrith = false;
	public int npcType, nexSpell = 0, invulnerableTick = -1, delrithRebirth = -1;
	public int absX, absY;
	public int heightLevel;
	public int x_length = 1, y_length = 1;
	public int makeX, makeY, maxHit, oldDefence, defence, attack, moveX, moveY, direction, walkingType;
	public int spawnX, spawnY;
	public int viewX, viewY;
	public int combat;
	public int pestGate = -1;
	public Location location;
	public Location lastKnownRegion = this.getLocation();
	private Region currentRegion;
	/**
	 * attackType: 0 = melee, 1 = range, 2 = mage
	 */
	public int attackType, projectileId, endGfx, spawnedBy, hitDelayTimer, HP, MaxHP, hitDiff, animNumber, actionTimer, enemyX, enemyY;
	public boolean applyDead, isDead, needRespawn, respawns, walkingHome, underAttack, randomWalk, summoned, hasBank, dirUpdateRequired, animUpdateRequired, hitUpdateRequired, updateRequired, forcedChatRequired, faceToUpdateRequired;
	public int freezeTimer, attackTimer, killerId, killedBy, oldIndex, underAttackBy;
	public long lastDamageTaken;
	public int firstAttacker;
	public String forcedText;
	public int mJavHit = 0;
	public int mJavTime = 0;
	
	public NPC(int _npcId, int _npcType){
		lastKnownRegion = location;
		npcId = _npcId;
		npcType = _npcType;
		direction = -1;
		isDead = false;
		applyDead = false;
		actionTimer = 0;
		randomWalk = true;
		x_length = getXLength(npcType);
		y_length = getYLength(npcType);
	}

	public int getXLength(int type){
		switch(type){
		// Brawlers
			case 3773:
			case 3775:
			case 3776:
				// Dags
			case 2881:
			case 2882:
			case 2883:
			case 1778:
			case 1786:
				return 2;
				// Corp
			case 8133:
				// Kalphite Queen Two
			case 1160:
				// KBD
			case 50:
				return 5;
				// Chaos Ele
			case 3200:
				// Tormented Demon
			case 8349:
				// Nex
			case 2000:
				return 3;
				// Kalphite Queen
			case 1158:
				return 6;
				// Delrith
			case 879:
			case 880:
			case 82: // Lesser Demon
				return 2;
			case 83: // Greater Demon
			case 84: // Black Demon
				return 3;
		}
		return 1;
	}

	public int getYLength(int type){
		switch(type){
			// Brawlers
			case 3773:
			case 3775:
			case 3776:
			case 1778:
			case 1786:
				return 2;
				// Dags
			case 2881:
			case 2882:
			case 2883:
				// Chaos Ele
			case 3200:
				// Tormented Demon
			case 8349:
				// Nex
			case 2000:
				return 3;
				// Corp
			case 8133:
				// Kalphite Queens
			case 1158:
			case 1160:
				// KBD
			case 50:
				return 5;
				// Delrith
			case 879:
			case 880:
			case 82: // Lesser Demon
				return 2;
			case 83: // Greater Demon
			case 84: // Black Demon
				return 3;
		}
		return 1;
	}
	
	public void handleNexStage(){
		if(nexStage == 5)
			return;
		if(nexStage == 1 && HP <= (MaxHP * 0.8))
			nexStage++;
		else if(nexStage == 2 && HP <= (MaxHP * 0.6))
			nexStage++;
		else if(nexStage == 3 && HP <= (MaxHP * 0.4))
			nexStage++;
		else if(nexStage == 4 && HP <= (MaxHP * 0.2)){
			nexStage++;
			HP = ((int)(MaxHP * 0.4));
			forceChat("NOW, THE POWER OF ZAROS!");
		}
	}
	
	public void handleWrath(){
		if(npcType != 2000)
			return;
		forceChat("Taste my wrath!");
		gfx0(2259);
		Collection<Client> clients = getRegion().getPlayers();
		int damage = 40;
		boolean first = true;
		for(Client c : clients){
			if(c == null)
				continue;
			if(first){
				c.getPA().gfx(2260, absX + 4, absY + 1, heightLevel);
				c.getPA().gfx(2260, absX + 1, absY + 4, heightLevel);
				c.getPA().gfx(2260, absX - 2, absY + 1, heightLevel);
				c.getPA().gfx(2260, absX + 1, absY - 2, heightLevel);
				c.getPA().gfx(2260, absX + 4, absY + 4, heightLevel);
				c.getPA().gfx(2260, absX + 4, absY - 2, heightLevel);
				c.getPA().gfx(2260, absX - 2, absY + 4, heightLevel);
				c.getPA().gfx(2260, absX - 2, absY - 2, heightLevel);
				c.getPA().gfx(2260, absX + 3, absY + 3, heightLevel);
				c.getPA().gfx(2260, absX + 3, absY - 1, heightLevel);
				c.getPA().gfx(2260, absX - 1, absY + 3, heightLevel);
				c.getPA().gfx(2260, absX - 1, absY - 1, heightLevel);
				first = false;
			}
			if(c.goodDistance(absX, absY, c.absX, c.absY, 3) && heightLevel == c.heightLevel){
				int dam = Misc.random(damage);
				if(c.playerLevel[c.playerHitpoints] < dam)
					dam = c.playerLevel[c.playerHitpoints];
				c.dealDamage(dam);
				c.getPA().refreshSkill(c.playerHitpoints);
			}
		}
	}

	public void setLocation(Location location){
		this.location = location;

		Region newRegion = RegionManager.getRegionByLocation(location);
		if(newRegion != currentRegion){
			if(currentRegion != null){
				currentRegion.removeNpc(this);
			}
			currentRegion = newRegion;
			currentRegion.addNpc(this);
		}
	}

	public Region getRegion(){
		return currentRegion;
	}

	public void setLastKnownRegion(Location lastKnownRegion){
		this.lastKnownRegion = lastKnownRegion;
	}

	public Location getLastKnownRegion(){
		return lastKnownRegion;
	}

	public void updateNPCMovement(Stream str){
		if(direction == -1){
			if(updateRequired){
				str.writeBits(1, 1);
				str.writeBits(2, 0);
			}else{
				str.writeBits(1, 0);
			}
		}else{
			str.writeBits(1, 1);
			str.writeBits(2, 1);
			str.writeBits(3, Misc.xlateDirectionToClient[direction]);
			if(updateRequired){
				str.writeBits(1, 1);
			}else{
				str.writeBits(1, 0);
			}
		}
	}

	/**
	 * Text update
	 **/

	public void forceChat(String text){
		forcedText = text;
		forcedChatRequired = true;
		updateRequired = true;
	}

	/**
	 * Graphics
	 **/
	public int mask80var1 = 0;
	public int mask80var2 = 0;
	protected boolean mask80update = false;

	public void appendMask80Update(Stream str){
		str.writeWord(mask80var1);
		str.writeDWord(mask80var2);
	}

	public void gfx100(int gfx){
		mask80var1 = gfx;
		mask80var2 = 6553600;
		mask80update = true;
		updateRequired = true;
	}

	public void gfx0(int gfx){
		mask80var1 = gfx;
		mask80var2 = 65536;
		mask80update = true;
		updateRequired = true;
	}

	public void appendAnimUpdate(Stream str){
		str.writeWordBigEndian(animNumber);
		str.writeByte(1);
	}

	/**
	 * 
	 Face
	 * 
	 **/

	public int FocusPointX = -1, FocusPointY = -1;
	public int face = 0;

	private void appendSetFocusDestination(Stream str){
		str.writeWordBigEndian(FocusPointX);
		str.writeWordBigEndian(FocusPointY);
	}

	public void turnNpc(int i, int j){
		if(this.npcType == 1532)
			return;
		FocusPointX = 2 * i + 1;
		FocusPointY = 2 * j + 1;
		updateRequired = true;
	}

	public void appendFaceEntity(Stream str){
		str.writeWord(face);
	}

	public void facePlayer(int player){
		if(npcType == 1532 || npcObject)
			return;
		face = player + 32768;
		dirUpdateRequired = true;
		updateRequired = true;
	}

	public void appendFaceToUpdate(Stream str){
		str.writeWordBigEndian(viewX);
		str.writeWordBigEndian(viewY);
	}

	public void appendNPCUpdateBlock(Stream str){
		if(!updateRequired)
			return;
		int updateMask = 0;
		if(animUpdateRequired)
			updateMask |= 0x10;
		if(hitUpdateRequired2)
			updateMask |= 8;
		if(mask80update)
			updateMask |= 0x80;
		if(dirUpdateRequired)
			updateMask |= 0x20;
		if(forcedChatRequired)
			updateMask |= 1;
		if(hitUpdateRequired)
			updateMask |= 0x40;
		if(FocusPointX != -1)
			updateMask |= 4;

		str.writeByte(updateMask);

		if(animUpdateRequired)
			appendAnimUpdate(str);
		if(hitUpdateRequired2)
			appendHitUpdate2(str);
		if(mask80update)
			appendMask80Update(str);
		if(dirUpdateRequired)
			appendFaceEntity(str);
		if(forcedChatRequired){
			str.writeString(forcedText);
		}
		if(hitUpdateRequired)
			appendHitUpdate(str);
		if(FocusPointX != -1)
			appendSetFocusDestination(str);

	}

	public void clearUpdateFlags(){
		updateRequired = false;
		forcedChatRequired = false;
		hitUpdateRequired = false;
		hitUpdateRequired2 = false;
		animUpdateRequired = false;
		dirUpdateRequired = false;
		mask80update = false;
		forcedText = null;
		moveX = 0;
		moveY = 0;
		direction = -1;
		FocusPointX = -1;
		FocusPointY = -1;
	}

	public int getNextWalkingDirection(){
		int dir;
		dir = Misc.direction(absX, absY, (absX + moveX), (absY + moveY));
		if(dir == -1)
			return -1;
		dir >>= 1;
		absX += moveX;
		absY += moveY;
		return dir;
	}

	public void getNextNPCMovement(int i){
		if(npcObject)
			return;
		direction = -1;
		if(NPCHandler.npcs[i].freezeTimer == 0){
			direction = getNextWalkingDirection();
		}
	}

	public void appendHitUpdate(Stream str){
		if(HP <= 0){
			isDead = true;
		}
		str.writeDWord(hitDiff);
		if(hitDiff > 0){
			str.writeByteS(1);
		}else{
			str.writeByteS(0);
		}
		str.writeDWord(HP);
		str.writeDWord(MaxHP);
	}

	public int hitDiff2 = 0;
	public boolean hitUpdateRequired2 = false;

	public void appendHitUpdate2(Stream str){
		if(HP <= 0){
			isDead = true;
		}
		str.writeDWord(hitDiff2);
		if(hitDiff2 > 0){
			str.writeByteC(1);
		}else{
			str.writeByteC(0);
		}
		str.writeDWord(HP);
		str.writeDWord(MaxHP);
	}

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

	public int getX(){
		return absX;
	}

	public int getY(){
		return absY;
	}

	public Location getLocation(){
		return location;
	}

	public boolean inCanifis(){
		return absX >= 3464 && absX <= 3518 && absY >= 3462 && absY <= 3509;
	}

	public boolean inMulti(){
		if((absX >= 3136 && absX <= 3327 && absY >= 3519 && absY <= 3607) || (absX >= 3190 && absX <= 3327 && absY >= 3648 && absY <= 3839) || (absX >= 3200 && absX <= 3390 && absY >= 3840 && absY <= 3967) || (absX >= 2992 && absX <= 3007 && absY >= 3912 && absY <= 3967) || (absX >= 2946 && absX <= 2959 && absY >= 3816 && absY <= 3831) || (absX >= 3008 && absX <= 3199 && absY >= 3856 && absY <= 3903) || (absX >= 3008 && absX <= 3071 && absY >= 3600 && absY <= 3711) || (absX >= 3072 && absX <= 3327 && absY >= 3608 && absY <= 3647) || (absX >= 2624 && absX <= 2690 && absY >= 2550 && absY <= 2619) || (absX >= 2371 && absX <= 2422 && absY >= 5062 && absY <= 5117) || (absX >= 2896 && absX <= 2927 && absY >= 3595 && absY <= 3630) || (absX >= 2892 && absX <= 2932 && absY >= 4435 && absY <= 4464) || (absX >= 2256 && absX <= 2287 && absY >= 4680 && absY <= 4711) || (absX >= 2275 && absX <= 2400 && absY >= 9800 && absY <= 9900) || (absX >= 3241 && absX <= 3255 && absY >= 9354 && absY <= 9375) || (absX >= 2697 && absX <= 2750 && absY >= 9410 && absY <= 9476) || (absX >= 3465 && absX <= 3508 && absY >= 9481 && absY <= 9518) || (absX >= 3409 && absX <= 3546 && absY >= 3440 && absY <= 3521)){
			return true;
		}
		return false;
	}

	public boolean inWild(){
		if(absX > 2941 && absX < 3392 && absY > 3518 && absY < 3966 || absX > 2941 && absX < 3392 && absY > 9918 && absY < 10366){
			return true;
		}
		return false;
	}
}