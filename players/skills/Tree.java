package server.model.players.skills;

import java.util.HashMap;
import server.Server;
import server.model.objects.Objects;
import server.util.Misc;

public class Tree{
	private static HashMap<Integer, Tree> trees = new HashMap<Integer, Tree>();
	private static final int SALT = 8273;
	public int id, x, y, h, treeId, exp, nextExp, stumpId = -1, constantTime;
	private int logsLeft;

	private Tree(int id, int x, int y, int h){
		this.id = id;
		this.x = x;
		this.y = y;
		this.h = h;
		int temp[] = getExp(id);
		exp = temp[0];
		nextExp = temp[1];
		constantTime = getConstantTime(id);
		treeId = (id + x + y + h) * SALT;
		logsLeft = getLogsLeft(id);
		trees.put(treeId, this);
	}

	public static Tree getTree(int id, int x, int y, int h){
		int treeId = (id + x + y + h) * SALT;
		if(trees.containsKey(treeId))
			return trees.get(treeId);
		Tree newTree = new Tree(id, x, y, h);
		return newTree;
	}

	private static int getLogsLeft(int id){
		switch(id){
			case 1276:
			case 1278: // trees
				return 1;
			case 1281: // oak
				return (Misc.random(3) + 1);
			case 1308: // willow
				return (Misc.random(6) + 1);
			case 1307: // maple
				return (Misc.random(7) + 1);
			case 1309: // yew
				return (Misc.random(8) + 1);
			case 1306: // Magic
				return (Misc.random(10) + 1);
		}
		return 1;
	}

	private static int getConstantTime(int id){
		switch(id){
			case 1276:
			case 1278: // trees
				return 0;
			case 1281: // oak
				return 1;
			case 1308: // willow
				return 3;
			case 1307: // maple
				return 3;
			case 1309: // yew
				return 6;
			case 1306: // Magic
				return 10;
		}
		return 0;
	}

	private static int[] getExp(int id){
		int trees[][] = {{1276, 1278}, {1281}, {1308}, {1307}, {1309}, {1306}};
		int exp[] = {1, 15, 30, 45, 60, 75, 105};
		int ret[] = {-1, -1};
		for(int i = 0; i < trees.length; i++){
			for(int treeId : trees[i]){
				if(treeId == id){
					ret[0] = exp[i];
					ret[1] = exp[i + 1];
					return ret;
				}
			}
		}
		return ret;
	}

	public void setStump(){
		Objects stump = new Objects(stumpId, x, y, h, 0, 10, 0);
		stump.treeId = id;
		stump.treeSpawn = Misc.random(10) + 20;
		Server.objectHandler.placeObject(stump);
		Server.objectHandler.addObject(stump);
	}

	public int subtractLogs(){
		return --logsLeft;
	}

	public int getLogs(){
		return logsLeft;
	}

	public void newLogs(){
		logsLeft = getLogsLeft(id);
	}
}