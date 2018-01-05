package server.model.quests;

import java.io.File;
import java.util.Scanner;

import server.model.players.Client;

public class QuestHandler{
	public static final int DEMON_SLAYER = 1;
	public int questId = -1;
	public Client player;
	public DemonSlayer demonSlayer = null;
	public boolean questChat = false;
	
	public QuestHandler(Client c){
		player = c;
		initQuests();
	}
	public void loadQuests(){
		File character = new File("./Data/quests/" + player.originalName);
		character.mkdir();
		File quests[] = character.listFiles();
		for(File quest : quests)
			loadQuest(quest);
	}
	public void loadQuest(File quest){
		int pos = quest.getName().indexOf('.');
		String name = quest.getName().substring(0, pos);
		if(name.equalsIgnoreCase("demonslayer")){
			try(Scanner in = new Scanner(quest)){
				while(in.hasNextLine()){
					String line[] = in.nextLine().split("=");
					if(line.length != 2)
						continue;
					String var = line[0].trim();
					String val = line[1].trim();
					if(var.equalsIgnoreCase("isInQuest"))
						demonSlayer.isInQuest = Boolean.parseBoolean(val);
					else if(var.equalsIgnoreCase("obtained"))
						demonSlayer.obtained = Boolean.parseBoolean(val);
					else if(var.equalsIgnoreCase("demonstrated"))
						demonSlayer.demonstrated = Boolean.parseBoolean(val);
					else if(var.equalsIgnoreCase("completed"))
						demonSlayer.completed = Boolean.parseBoolean(val);
				}
			}catch(Exception e){}
		}
	}
	public static void init(){
		DemonSlayer.initDialogue();
	}
	public void initQuests(){
		if(demonSlayer == null)
			demonSlayer = new DemonSlayer(player);
	}
	public void beginQuest(int questId){
		if(questId <= 0)
			return;
		this.questId = questId;
		switch(questId){
			case 1:
				demonSlayer.isInQuest = true;
				saveQuest(questId);
				break;
		}
	}
	public void handleChat(){
		questChat = false;
		if(questId == 1)
			demonSlayer.handleDialogue(false);
	}
	public void chatResponse(int response){
		questChat = false;
		if(questId == 1)
			demonSlayer.chatResponse(response);
	}
	public void saveQuest(int questId){
		switch(questId){
			case 1:
				demonSlayer.saveQuest();
				break;
		}
	}
}