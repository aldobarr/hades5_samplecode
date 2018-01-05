package server.model.players.skills;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import server.Server;
import server.model.players.Client;
import server.util.Misc;

public class Slayer{
	private Client c;
	public static int EASY_TASK = 1;
	public static int MEDIUM_TASK = 2;
	public static int HARD_TASK = 3;
	private static Map<Integer, ArrayList<SlayerTask>> tasks = new HashMap<Integer, ArrayList<SlayerTask>>();
	
	public Slayer(Client c){
		this.c = c;
	}
	
	public void requestTask(){
		if(c.slayerTask != null){
			c.sendMessage("You already have a slayer task.");
			return;
		}
		giveTask();
	}
	
	public void requestNewTask(){
		if(c.slayerPoints < 20)
			c.sendMessage("You don't have enough slayer points for that.");
		else{
			c.slayerPoints -= 20;
			giveTask();
		}
	}
	
	public void giveTask(){
		if(c.playerLevel[c.playerSlayer] < 30)
			giveTask(EASY_TASK);
		else if(c.playerLevel[c.playerSlayer] >= 30 && c.playerLevel[c.playerSlayer] <= 70)
			giveTask(MEDIUM_TASK);
		else
			giveTask(HARD_TASK);
	}
	
	public void giveTask(int difficulty){
		@SuppressWarnings("unchecked")
		List<SlayerTask> chosenTasks = (List<SlayerTask>)tasks.get(difficulty).clone();
		SlayerTask task = null;
		do{
			int rand = Misc.random(chosenTasks.size() - 1);
			task = chosenTasks.remove(rand).clone();
			if(task.requirement > c.playerLevel[c.playerSlayer] || (c.slayerTask != null && c.slayerTask.monster == task.monster))
				task = null;
		}while(task == null && chosenTasks.size() > 0);
		int amount = Misc.random(100) + 15;
		SlayerTask st = new SlayerTask(task.monster, task.requirement, task.level, amount);
		c.slayerTask = st;
		c.saveGame();
		c.sendMessage("You have been assigned to kill " + st.amount + " " + Server.npcHandler.getNpcListName(st.monster).replace("_", " ") + "s as a slayer task.");
	}
	public int getPoints(){
		return ((int)(getPoints(c.slayerTask.level) + ((int)(c.playerLevel[c.playerSlayer] / 10)) + ((int)(c.slayerTask.requirement / 10))));
	}
	private int getPoints(int level){
		return (level == HARD_TASK ? 15 : (level == MEDIUM_TASK ? 10 : 5));
	}
	public String getDifficulty(){
		return (c.slayerTask != null ? (c.slayerTask.level == EASY_TASK ? "EASY" : (c.slayerTask.level == MEDIUM_TASK ? "MEDIUM" : "HARD")) : "");
	}
	public static void loadTasks(){
		try(Scanner in = new Scanner(new File("./Data/cfg/slayer.cfg"))){
			while(in.hasNextLine()){
				String line = in.nextLine();
				if(line.startsWith("//"))
					continue;
				String pieces[] = line.split("//")[0].trim().split(", ");
				if(pieces.length < 3)
					continue;
				SlayerTask st = new SlayerTask(Integer.parseInt(pieces[0]), Integer.parseInt(pieces[1]), Integer.parseInt(pieces[2]));
				addTask(st);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public static int levelReq(int monster){
		int level = -1;
		Collection<ArrayList<SlayerTask>> all = tasks.values();
		for(ArrayList<SlayerTask> level_tasks : all)
			for(SlayerTask task : level_tasks)
				if(task.monster == monster && (level < 0 || task.requirement < level))
					level = task.requirement;
		return level;
	}
	public static void addTask(SlayerTask st){
		List<SlayerTask> task = null;
		if(tasks.containsKey(st.level))
			task = tasks.get(st.level);
		else
			task = new ArrayList<SlayerTask>();
		task.add(st);
		tasks.put(st.level, (ArrayList<SlayerTask>)task);
	}
	public static void reloadTasks(){
		tasks.clear();
		loadTasks();
	}
}