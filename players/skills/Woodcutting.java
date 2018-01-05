package server.model.players.skills;

import server.Config;
import server.Server;
import server.model.players.Client;
import server.model.objects.Objects;
import server.util.Misc;

public class Woodcutting{
	Client c;
	private Axe axes[] = new Axe[8];
	private Tree currentTree;
	private int logType;
	private int exp;
	public static final int WALK = 65535;
	public int levelReq;
	public Axe currentAxe;

	public Woodcutting(Client c){
		this.c = c;
		setupAxes();
	}

	public void setupAxes(){
		axes[0] = new Axe(1351, 1, 879, 0);
		axes[1] = new Axe(1349, 1, 877, 1);
		axes[2] = new Axe(1353, 6, 875, 2);
		axes[3] = new Axe(1361, 6, 873, 3);
		axes[4] = new Axe(1355, 21, 871, 4);
		axes[5] = new Axe(1357, 31, 869, 5);
		axes[6] = new Axe(1359, 41, 867, 6);
		axes[7] = new Axe(6739, 61, 2846, 7);
	}

	public void startWoodcutting(int logType, int levelReq, int exp, int stumpId){
		currentAxe = goodAxe();
		if(c.inventory.freeSlots() < 1){
			c.startAnimation(WALK);
			c.sendMessage("Not enough space in your inventory.");
			c.getPA().resetVariables();
			return;
		}
		Objects temp = Server.objectHandler.objectExists(c.objectX, c.objectY, c.heightLevel);
		if(temp != null && temp.objectId == stumpId){
			c.startAnimation(WALK);
			c.getPA().resetVariables();
			return;
		}
		if(currentAxe != null){
			c.turnPlayerTo(c.objectX, c.objectY);
			if(c.playerLevel[c.playerWoodcutting] >= levelReq){
				this.currentTree = Tree.getTree(c.objectId, c.objectX, c.objectY, c.heightLevel);
				if(currentTree.stumpId < 0)
					currentTree.stumpId = stumpId;
				this.logType = logType;
				this.exp = exp;
				this.levelReq = levelReq;
				c.wcTimer = getWcTimer();
				c.startAnimation(currentAxe.emote);
			}else{
				c.getPA().resetVariables();
				c.startAnimation(WALK);
				c.sendMessage("You need a woodcutting level of " + levelReq + " to cut this tree.");
			}
		}else{
			c.startAnimation(WALK);
			c.sendMessage("You need an axe to cut this tree.");
			c.getPA().resetVariables();
		}
	}

	public void resetWoodcut(){
		this.logType = -1;
		this.exp = -1;
		this.levelReq = -1;
		this.currentTree = null;
		this.currentAxe = null;
		c.wcTimer = -1;
	}

	public void cutWood(){
		if(currentAxe != null && currentTree.getLogs() > 0 && c.inventory.addItem(logType, 1, -1)){
			int logsLeft = currentTree.subtractLogs();
			c.startAnimation(logsLeft <= 0 || c.inventory.freeSlots() < 1 ? WALK : currentAxe.emote);
			c.sendMessage("You get some logs.");
			c.getPA().addSkillXP(exp * Config.WOODCUTTING_EXPERIENCE, c.playerWoodcutting);
			c.getPA().refreshSkill(c.playerWoodcutting);
			c.wcTimer = getWcTimer();
			if(c.inventory.freeSlots() < 1 || logsLeft <= 0){
				if(logsLeft <= 0)
					currentTree.setStump();
				c.getPA().resetVariables();
			}
		}else
			c.getPA().resetVariables();
	}

	public void performEmote(){
		c.startAnimation(currentAxe.emote);
	}

	public Axe goodAxe(){
		for(Axe axe : axes)
			if(c.playerEquipment[c.playerWeapon] == axe.id && c.playerLevel[c.playerWoodcutting] >= axe.exp)
				return axe;
		for(int i = axes.length - 1; i >= 0; i--)
			for(int j = 0; j < c.inventory.items.length; j++)
				if(c.inventory.items[j] != null)
					if(c.inventory.items[j].id == axes[i].id + 1 && c.playerLevel[c.playerWoodcutting] >= axes[i].exp)
						return axes[i];
		return null;
	}

	public int getWcTimer(){
		int xp = currentTree.nextExp - c.playerLevel[c.playerWoodcutting] >= 0 ? ((currentTree.nextExp - c.playerLevel[c.playerWoodcutting]) / 2) : 0;
		int time = (xp + Misc.random(1) + 2 + currentTree.constantTime) - getAxeMod();
		return time >= 1 ? time : 1;
	}

	public int getAxeMod(){
		switch(currentAxe.id){
			case 1351:
				return -2;
			case 1349:
				return -1;
			case 1353:
				return 0;
			case 1361:
				return 0;
			case 1355:
				return 1;
			case 1357:
				return 2;
			case 1359:
				return 3;
			case 6739:
				return 4;

		}
		return -2;
	}
}

class Axe{
	public final int id, exp, emote, axeMod;

	public Axe(int id, int exp, int emote, int axeMod){
		this.id = id;
		this.exp = exp;
		this.emote = emote;
		this.axeMod = axeMod;
	}
}