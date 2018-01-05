package server.model.objects;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import server.Config;
import server.Server;
import server.model.items.GroundItem;
import server.model.npcs.NPC;
import server.model.npcs.NPCHandler;
import server.model.players.Client;
import server.model.players.Player;
import server.model.region.RegionManager;
import server.task.Task;
import server.util.Misc;
import server.world.ItemHandler;

public class DwarfCannon{
	private static Map<String, Task> removeTasks = new HashMap<String, Task>();
	private Client player;
	private String playerName;
	private int face;
	private int x = 0, y = 0, z = 0;
	private Task rotation;
	private Task removeCannon;
	private int cannonBalls = 0;
	private Objects cannon;
	private boolean firing = false;
	private boolean cannonSet = false;
	private int npcId = 0;
	private int npcType = 0;
	private boolean nameSet = false;
	private boolean cannonDropping = false;
	private int cannonStatus = 0;
	private static final int REMOVE_TIME = 200; // Increments of 100 = 1 minute.
	private static final int MAX_HIT = 40;
	private static final int MAX_AMOUNT = 60;
	private static final int SET_CANNON_EMOTE = 827;
	public static final int CANNON_BALL = 2;
	public static final int CANNON_BASE_OBJECT = 7;
	public static final int CANNON_BASE_ID = 6;
	public static final int CANNON_STAND_OBJECT = 8;
	public static final int CANNON_STAND_ID = 8;
	public static final int CANNON_BARREL_OBJECT = 9;
	public static final int CANNON_BARREL_ID = 10;
	public static final int CANNON_OBJECT = 6;
	public static final int CANNON_FURNACE_ID = 12;
	private static final int PROJECTILE_SPEED = 60;
	private static final int PROJECTILE_GFX = 53;
	
	public DwarfCannon(Client player){
		this.player = player;
		face = 1;
		cannonBalls = 0;
	}
	
	public void setName(String playerName){
		this.playerName = playerName;
		nameSet = true;
	}
	
	public void setBase(){
		if(!nameSet)
			return;
		if(!canSetCannon()){
			player.sendMessage("I don't think the cannon fits here...");
			return;
		}
		if(player.inventory.deleteItem(CANNON_BASE_ID, player.inventory.findItemSlot(CANNON_BASE_ID), 1)){
			player.turnPlayerTo(player.absX, player.absY + 1);
			player.startAnimation(SET_CANNON_EMOTE);
			x = player.absX - 1;
			y = player.absY;
			z = player.heightLevel;
			cannonSet = true;
			cannonStatus = 1;
			cannon = new Objects(CANNON_BASE_OBJECT, x, y, z, 0, 10, 0);
			cannon.addCannon(this);
			Server.objectHandler.addObject(cannon);
			Server.objectHandler.placeObject(cannon);
		}
	}
	
	public void itemOnObject(int item, int object, int x, int y){
		if(!nameSet)
			return;
		Objects obj = Server.objectHandler.objectExists(x, y, player.heightLevel);
		if(obj == null || obj.cannon == null || obj.cannon.player == null)
			return;
		if(!obj.cannon.playerName.equalsIgnoreCase(player.originalName)){
			player.sendMessage("This isn't yours.");
			return;
		}
		if(cannon == null || cannon != obj)
			cannon = obj;
		if(object == CANNON_BASE_OBJECT && item == CANNON_STAND_ID){
			if(!player.inventory.deleteItem(CANNON_STAND_ID, player.inventory.findItemSlot(CANNON_STAND_ID), 1))
				return;
			cannon.objectId = CANNON_STAND_OBJECT;
			cannonStatus = 2;
			player.turnPlayerTo(x + 1, y + 1);
			player.startAnimation(SET_CANNON_EMOTE);
			Server.objectHandler.placeObject(cannon);
		}else if(object == CANNON_STAND_OBJECT && item == CANNON_BARREL_ID){
			if(!player.inventory.deleteItem(CANNON_BARREL_ID, player.inventory.findItemSlot(CANNON_BARREL_ID), 1))
				return;
			cannon.objectId = CANNON_BARREL_OBJECT;
			cannonStatus = 3;
			player.turnPlayerTo(x + 1, y + 1);
			player.startAnimation(SET_CANNON_EMOTE);
			Server.objectHandler.placeObject(cannon);
		}else if(object == CANNON_BARREL_OBJECT && item == CANNON_FURNACE_ID){
			if(!player.inventory.deleteItem(CANNON_FURNACE_ID, player.inventory.findItemSlot(CANNON_FURNACE_ID), 1))
				return;
			cannon.objectId = CANNON_OBJECT;
			cannonStatus = 4;
			player.turnPlayerTo(x + 1, y + 1);
			player.startAnimation(SET_CANNON_EMOTE);
			Server.objectHandler.placeObject(cannon);
		}else
			player.sendMessage("I don't think this item goes there just yet...");
	}
	
	public void pickupCannon(int object, int x, int y){
		if(!nameSet)
			return;
		Objects obj = Server.objectHandler.objectExists(x, y, player.heightLevel);
		if(obj == null || obj.cannon == null || obj.cannon.player == null || cannonDropping)
			return;
		if(!obj.cannon.playerName.equalsIgnoreCase(player.originalName)){
			player.sendMessage("This isn't yours.");
			return;
		}
		if(cannon == null || cannon != obj)
			cannon = obj;
		if(object == CANNON_BASE_OBJECT){
			int free = cannonBalls > 0 ? 1 : 0;
			if(player.inventory.freeSlots() <= free){
				player.sendMessage("You don't have enough space to do that.");
				return;
			}
			Server.objectHandler.removeObject(obj);
			Server.objectHandler.globalObjects.remove(obj);
			cannon.cannon = null;
			player.inventory.addItem(CANNON_BASE_ID, 1);
			if(cannonBalls > 0)
				player.inventory.addItem(CANNON_BALL, cannonBalls);
			reset();
		}else if(object == CANNON_STAND_OBJECT){
			int free = cannonBalls > 0 ? 2 : 1;
			if(player.inventory.freeSlots() <= free){
				player.sendMessage("You don't have enough space to do that.");
				return;
			}
			Server.objectHandler.removeObject(obj);
			Server.objectHandler.globalObjects.remove(obj);
			cannon.cannon = null;
			player.inventory.addItem(CANNON_BASE_ID, 1);
			player.inventory.addItem(CANNON_STAND_ID, 1);
			if(cannonBalls > 0)
				player.inventory.addItem(CANNON_BALL, cannonBalls);
			reset();
		}else if(object == CANNON_BARREL_OBJECT){
			int free = cannonBalls > 0 ? 3 : 2;
			if(player.inventory.freeSlots() <= free){
				player.sendMessage("You don't have enough space to do that.");
				return;
			}
			Server.objectHandler.removeObject(obj);
			Server.objectHandler.globalObjects.remove(obj);
			cannon.cannon = null;
			player.inventory.addItem(CANNON_BASE_ID, 1);
			player.inventory.addItem(CANNON_STAND_ID, 1);
			player.inventory.addItem(CANNON_BARREL_ID, 1);
			if(cannonBalls > 0)
				player.inventory.addItem(CANNON_BALL, cannonBalls);
			reset();
		}else if(object == CANNON_OBJECT){
			int free = cannonBalls > 0 ? 4 : 3;
			if(player.inventory.freeSlots() <= free){
				player.sendMessage("You don't have enough space to do that.");
				return;
			}
			Server.objectHandler.removeObject(obj);
			Server.objectHandler.globalObjects.remove(obj);
			cannon.cannon = null;
			player.inventory.addItem(CANNON_BASE_ID, 1);
			player.inventory.addItem(CANNON_STAND_ID, 1);
			player.inventory.addItem(CANNON_BARREL_ID, 1);
			player.inventory.addItem(CANNON_FURNACE_ID, 1);
			if(cannonBalls > 0)
				player.inventory.addItem(CANNON_BALL, cannonBalls);
			reset();
		}
	}
	
	public void fireCannon(int x, int y){
		if(!nameSet)
			return;
		Objects obj = Server.objectHandler.objectExists(x, y, player.heightLevel);
		if(obj == null || obj.cannon == null || obj.cannon.player == null)
			return;
		if(!obj.cannon.playerName.equalsIgnoreCase(player.originalName)){
			player.sendMessage("This isn't yours.");
			return;
		}
		if(cannon == null || cannon != obj)
			cannon = obj;
		if(cannonBalls == 0){
			if(player.inventory.hasItem(CANNON_BALL)){
				int amount = player.inventory.hasItem(CANNON_BALL, MAX_AMOUNT) ? MAX_AMOUNT : player.inventory.getItemCount(CANNON_BALL);
				if(player.inventory.deleteItem(CANNON_BALL, player.inventory.findItemSlot(CANNON_BALL), amount))
					cannonBalls += amount;
			}else
				player.sendMessage("You don't have any cannon balls!");
		}else if(!firing)
			fireCannon();
	}
	
	public void handleLogout(){
		if(!nameSet)
			return;
		if(!cannonSet)
			return;
		terminateProcess();
		final int x = this.x, y = this.y, z = this.z;
		removeCannon = new Task(REMOVE_TIME){
			protected void execute(){
				if(cannonSet){
					synchronized(this){
						cannonDropping = true;
					}
					if(cannonSet){
						Objects obj = Server.objectHandler.objectExists(x, y, z);
						synchronized(Server.objectHandler){
							Server.objectHandler.removeObject(obj);
							Server.objectHandler.globalObjects.remove(obj);
						}
						synchronized(Server.itemHandler){
							GroundItem base = new GroundItem(CANNON_BASE_ID, x, y, z, 1, 0, 0, playerName);
							GroundItem stand = new GroundItem(CANNON_STAND_ID, x, y, z, 1, 0, 0, playerName);
							GroundItem barrel = new GroundItem(CANNON_BARREL_ID, x, y, z, 1, 0, 0, playerName);
							GroundItem furnace = new GroundItem(CANNON_FURNACE_ID, x, y, z, 1, 0, 0, playerName);
							base.removeTicks = stand.removeTicks = barrel.removeTicks = furnace.removeTicks = ItemHandler.HIDE_TICKS;
							GroundItem cannon[] = {base, stand, barrel, furnace};
							for(int i = 1; i<=cannonStatus; i++){
								Server.itemHandler.addItem(cannon[i - 1]);
								Server.itemHandler.createGlobalItem(cannon[i - 1]);
							}
						}
						synchronized(this){
							reset();
						}
					}
				}
				if(isRunning())
					stop();
			}
		};
		if(removeTasks.containsKey(playerName))
			removeTasks.get(playerName).stop();
		removeTasks.put(playerName, removeCannon);
		Server.scheduler.schedule(removeCannon);
	}
	
	public boolean canPickupCannon(int item){
		if(item == CANNON_BASE_ID || item - 1 == CANNON_BASE_ID || item == CANNON_STAND_ID || item - 1 == CANNON_STAND_ID)
			return !player.inventory.hasItem(item) && player.bank.getBankAmount(item) == 0 && !player.inventory.hasItem(item - 1) && player.bank.getBankAmount(item - 1) == 0;
		if(item == CANNON_BARREL_ID || item - 1 == CANNON_BARREL_ID || item == CANNON_FURNACE_ID || item - 1 == CANNON_FURNACE_ID)
			return !player.inventory.hasItem(item) && player.bank.getBankAmount(item) == 0 && !player.inventory.hasItem(item - 1) && player.bank.getBankAmount(item - 1) == 0;
		return true;
	}
	
	private void fireCannon(){
		if(x == 0 && y == 0 && z == 0)
			return;
		firing = true;
		rotation = new Task(true){
			protected void execute(){
				processCannon();
			}
		};
		Server.scheduler.schedule(rotation);
	}
	
	private void terminateProcess(){
		if(rotation != null && rotation.isRunning())
			rotation.stop();
	}
	
	private void processCannon(){
		sendRotationAnim(x, y, 515 + face, 10, -1);
		synchronized(this){
			face += face == 6 ? -7 : 1;
			if(cannonBalls == 0){
				firing = false;
				npcId = npcType = 0;
				terminateProcess();
				return;
			}
		}
		Collection<NPC> npcs = RegionManager.getLocalNpcs(player);
		if(!player.inMulti()){
			if(player.underAttackBy2 > 0 && NPCHandler.npcs[player.underAttackBy2] != null && NPCHandler.npcs[player.underAttackBy2].oldIndex == player.playerId)
				attackNPC(NPCHandler.npcs[player.underAttackBy2]);
			else{
				if(npcId == 0){
					NPC npc = findTarget(npcs, false);
					if(npc != null)
						attackNPC(npc);
				}else if(NPCHandler.npcs[npcId] == null || NPCHandler.npcs[npcId].npcType != npcType || NPCHandler.npcs[npcId].isDead){
					NPC npc = findTarget(npcs, false);
					if(npc != null)
						attackNPC(npc);
				}else if(inLineOfSight(NPCHandler.npcs[npcId]))
					attackNPC(NPCHandler.npcs[npcId]);
			}
		}else{
			NPC npc = findTarget(npcs, true);
			if(npc != null)
				attackNPC(npc);
		}
	}
	
	private boolean inLineOfSight(NPC npc){
		if(npc.heightLevel != z)
			return false;
		if(npc.absX == x && npc.absY == y) // NPC is on top of cannon.
			return true;
		if(face == 1 && npc.absY > y){ // North
			int diff = npc.absY - y;
			int range = diff + (diff - 1);
			int div = ((int)(range / 2));
			return ((npc.absX > x) ? (npc.absX - x <= div) : (x - npc.absX <= div));
		}else if(face == 2) // North-East
			return npc.absX > x && npc.absY > y;
		else if(face == 3 && npc.absX > x){ // East
			int diff = npc.absX - x;
			int range = diff + (diff - 1);
			int div = ((int)(range / 2));
			return ((npc.absY > y) ? (npc.absY - y <= div) : (y - npc.absY <= div));
		}else if(face == 4) // South-East
			return npc.absX > x && npc.absY < y;
		else if(face == 5 && npc.absY < y){ // South
			int diff = y - npc.absY;
			int range = diff + (diff - 1);
			int div = ((int)(range / 2));
			return ((npc.absX > x) ? (npc.absX - x <= div) : (x - npc.absX <= div));
		}else if(face == 6) // South-West
			return npc.absX < x && npc.absY < y;
		else if(face == -1 && npc.absX < x){ // West
			int diff = x - npc.absX;
			int range = diff + (diff - 1);
			int div = ((int)(range / 2));
			return ((npc.absY > y) ? (npc.absY - y <= div) : (y - npc.absY <= div));
		}else if(face == 0) // North-West
			return npc.absX < x && npc.absY > y;
		return false;
	}
	
	private void attackNPC(NPC npc){
		synchronized(npc){
			int damage = Misc.random_range(1, MAX_HIT);
			if(Misc.random(npc.defence) > Misc.random(10 + player.getCombat().calculateRangeAttack()))
				damage = 0;
			if(npc.HP < damage)
				damage = npc.HP;
			player.getPA().addSkillXP(((damage / 2) * Config.RANGE_EXP_RATE), 4);
			npc.underAttack = true;
			npc.hitDiff = damage;
			npc.HP -= damage;
			npc.underAttackBy = player.playerId;
			npc.lastDamageTaken = System.currentTimeMillis();
			npc.hitUpdateRequired = true;
			player.killingNpcIndex = npc.npcId;
			player.totalDamageDealt += damage;
			int pX = x + 1;
			int pY = y + 1;
			int nX = npc.getX();
			int nY = npc.getY();
			int offX = (pY - nY) * -1;
			int offY = (pX - nX) * -1;
			player.getPA().createPlayersProjectile(pX, pY, offX, offY, 50, PROJECTILE_SPEED, PROJECTILE_GFX, 43, 31, npc.npcId + 1, 30);
			synchronized(this){
				cannonBalls--;
			}
		}
	}
	
	private NPC findTarget(Collection<NPC> npcs, boolean multi){
		NPC attack = null;
		for(NPC npc : npcs){
			if(npc != null && npc.HP > 0 && inLineOfSight(npc)){
				attack = npc;
				if(!multi){
					npcId = npc.npcId;
					npcType = npc.npcType;
				}
				break;
			}
		}
		return attack;
	}
	
	private void sendRotationAnim(int x, int y, int animationID, int tileObjectType, int orientation){
		Collection<Player> players = RegionManager.getLocalPlayers(player);
		for(Player p : players)
			if(p != null)
				((Client)p).getPA().createPlayersObjectAnim(x, y, animationID, tileObjectType, orientation);
	}
	
	private void reset(){
		x = y = z = 0;
		cannonBalls = 0;
		firing = false;
		face = 1;
		npcId = npcType = 0;
		cannonSet = false;
		cannonStatus = 0;
		cannon = null;
		if(removeCannon != null && removeCannon.isRunning())
			removeCannon.stop();
		if(removeTasks.containsKey(playerName)){
			Task remove = removeTasks.remove(playerName);
			if(remove != null && remove.isRunning())
				remove.stop();
		}
	}
	
	private boolean canSetCannon(){
		int x = player.absX - 1;
		int y = player.absY;
		int z = player.heightLevel;
		boolean up = server.clip.region.Region.canMove(x, y, x, y + 3, z, 1, 1);
		boolean right = server.clip.region.Region.canMove(x, y, x + 3, y, z, 1, 1);
		boolean diag = server.clip.region.Region.canMove(x, y, x + 3, y + 3, z, 1, 1);
		return up && right && diag && !nearbyCannon(x, y, z);
	}
	
	private boolean nearbyCannon(int x, int y, int z){
		for(int i = -3; i<=3; i++){
			for(int j = -3; j<=3; j++){
				Objects obj = Server.objectHandler.objectExists(x + i, y + j, z);
				if(obj == null)
					continue;
				if(obj.objectX == x && obj.objectY == y)
					return true;
				if(obj.objectX < x){
					if(obj.objectY < y && obj.objectX + 2 >= x && obj.objectY + 2 >= y)
						return true;
					else if(obj.objectX + 2 >= x && y + 2 >= obj.objectY)
						return true;
				}else{
					if(obj.objectY < y && x + 2 >= obj.objectX && obj.objectY + 2 >= y)
						return true;
					else if(x + 2 >= obj.objectX && y + 2 >= obj.objectY)
						return true;
				}
			}
		}
		return false;
	}
}