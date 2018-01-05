package server.model.players.skills;

import server.Config;
import server.model.players.Client;
import server.Server;
import server.model.objects.Objects;
import server.util.Misc;

/**
 * 
 * @author hadesflames
 * 
 */
public class Firemaking{

	private Client c;
	private int logs_data[][] = {{1511, 40, 1, 20}, {1521, 60, 15, 25}, {1519, 90, 30, 35}, {1517, 135, 45, 35}, {1515, 203, 60, 45}, {1513, 304, 75, 60}};
	public long lastLight;

	public Firemaking(Client c){
		this.c = c;
	}

	public boolean isLog(int itemID){
		for(int id[] : logs_data)
			if(id[0] == itemID)
				return true;
		return false;
	}

	public int getSlot(int log){
		for(int i = 0; i < logs_data.length; i++)
			if(logs_data[i][0] == log)
				return i;
		return -1;
	}

	private boolean onSomething(){
		boolean object = Server.objectHandler.objectExists(c.absX, c.absY, c.heightLevel) != null;
		boolean item = Server.itemHandler.itemExists(c.absX, c.absY, c.heightLevel);
		return object || item;
	}

	private void addFire(int slot){
		Objects obj = new Objects(2732, c.fmX, c.fmY, c.fmZ, 0, 10, logs_data[slot][3]);
		obj.addItem(592);
		Server.objectHandler.addObject(obj);
		Server.objectHandler.placeObject(obj);
	}

	/**
	 * Calculate the amount of time it takes for a log to be lit.
	 * 
	 * @param slot
	 *            The log slot that is being burned right now.
	 * @return The calculated light time.
	 */
	private int getLightingTick(int slot){
		int level = logs_data[slot][2];
		int range = 0, diff = c.playerLevel[c.playerFiremaking] - level;
		if(diff == 0)
			range = 0;
		else if(diff >= 10)
			range = 4;
		else if(diff >= 7)
			range = 3;
		else if(diff >= 5)
			range = 2;
		else if(diff >= 1)
			range = Misc.random(1);
		int time = Misc.random_range(3, 8 - range);
		if(time <= 2)
			time = 3;
		else if(time > 8)
			time = 8;
		return time;
	}

	public void lightFire(int slot){
		if(c.arenas()){
			c.sendMessage("Why am I trying to light a fire in the duel arena?");
			return;
		}
		if(c.lastAgil > 0 && System.currentTimeMillis() - c.lastAgil < 5000)
			return;
		if(c.playerLevel[c.playerFiremaking] >= logs_data[slot][2]){
			if(c.inventory.hasItem(590) && c.inventory.hasItem(logs_data[slot][0])){
				if(!c.lighting){
					if(!onSomething()){
						c.startAnimation(733);
						c.lighting = true;
						c.lightingSlot = slot;
						c.lightingTick = getLightingTick(slot);
						c.fmX = c.absX;
						c.fmY = c.absY;
						c.fmZ = c.heightLevel;
						c.getPA().playerWalk(c.absX - 1, c.absY);
						c.turnPlayerTo(c.getX() + 1, c.getY());
					}else
						c.sendMessage("You can not light a fire here.");
				}
			}
		}else{
			c.sendMessage("You need a firemaking level of " + logs_data[slot][2] + " to light this log.");
		}
	}

	public void finishFire(){
		if(!c.inventory.hasItem(logs_data[c.lightingSlot][0]))
			return;
		c.turnPlayerTo(c.getX() + 1, c.getY());
		c.inventory.deleteItem(logs_data[c.lightingSlot][0], c.inventory.findItemSlot(logs_data[c.lightingSlot][0]), 1);
		c.getPA().addSkillXP(logs_data[c.lightingSlot][1] * Config.FIREMAKING_EXPERIENCE, c.playerFiremaking);
		addFire(c.lightingSlot);
		c.lightingSlot = -1;
		c.lighting = false;
	}
}