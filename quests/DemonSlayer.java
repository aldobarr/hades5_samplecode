package server.model.quests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import server.Server;
import server.model.players.Client;
import server.model.players.DialogueHandler;

public class DemonSlayer{
	public static final int QUEST_ID = 1;
	public static final int DELRITH_ID[] = {879, 880};
	public static final int ARIS_ID = 882;
	public static final int SILVER_LIGHT = 2402;
	public static final int SILVER_LIGHT_EMPOWERED = 22404;
	public static final int RUSTY_SWORD = 686;
	public static final int SILVER_BAR = 2355;
	public static final int ALTAR_X = 3290;
	public static final int ALTAR_Y = 3886;
	public static final int HEAL_EMOTE = 4620;
	public static final String ARIS = "Aris";
	public int dialogueId = 0, delrithId = -1;
	public Client player;
	public boolean isInQuest = false, obtained = false, demonstrated = false, completed = false;
	public boolean spawned = false;
	public static QuestDialogue dialogue[];
	
	public DemonSlayer(Client c){
		player = c;
	}
	public boolean isInBuilding(){
		return (player.absX >= 3284 && player.absX <= 3293 && player.absY >= 3878 && player.absY <= 3892);
	}
	public boolean isInBuilding(int absX, int absY){
		return (absX >= 3284 && absX <= 3293 && absY >= 3878 && absY <= 3892);
	}
	public boolean isInDelrith(){
		return (player.absX >= 3279 && player.absX <= 3300 && player.absY >= 3875 && player.absY <= 3896);
	}
	public boolean isInDelrith(int absX, int absY){
		return (absX >= 3279 && absX <= 3300 && absY >= 3875 && absY <= 3896);
	}
	public static void initDialogue(){
		int nexts[][] = {{1}, {2, 18}, {3}, {4, 19}, {5}, {6}, {-1}, {8}, {-1}, {10}, {11}, {-1}, {13}, {-1}, {-1}, {-1}, {17}, {-1}, {-1}, {-1}};
		int emotes[] = {DialogueHandler.SCARED, -1, DialogueHandler.SAD, -1, DialogueHandler.LAUGH, -1, DialogueHandler.HAPPY_TALK, DialogueHandler.SHY, -1, DialogueHandler.LAUGH, DialogueHandler.HAPPY_TALK, DialogueHandler.STIFF, DialogueHandler.ANGRY, DialogueHandler.HAPPY_TALK, DialogueHandler.MAD, DialogueHandler.LAUGH, DialogueHandler.SCARED, -1, DialogueHandler.STIFF, DialogueHandler.STIFF};
		String messyDialogue = "WE'RE DOOMED!\n" + // 0
				"{po}What's the matter?\tSucks...\n" + // 1
				"Oh, young one. There's nothing you can do now.\tSome demented cult is going to summon\tthe evil demon lord Delrith.\tWe're all doomed, don't you see?\n" + // 2
				"{po}Actually, I'm an expert at slaying evil beings from other dimensions!\tYeah...I guess we're screwed.\n" + // 3
				"You really think you can? Wow, you might need this though.\n" + // 4
				"{pi}You are handed some odd items.\n" + // 5
				"Those items will help\tbut you'll need to magically fuse them somehow...\n" + // 6
				"Oh deer, it seems you've lost the items I gave you.\tGood thing I found them though.\tHere you are, try not to lose it again.\n" + // 7
				"{pi}You are handed some odd items.\n" + // 8
				"That's the magic sword Silverlight!\tIt can banish the evil lord Delrith back to his dimension!\tWe're saved!\n" + // 9
				"Now you need to go confront delrith.\tI believe he would be somewhere in the deep wilderness\tI'm sorry I can't be more specific than that\tBut I don't know where he'd be.\n" + // 10
				"But remember\tonly a hit from silverlight can finish off Delrith!\n" + // 11
				"You lost Silverlight!?\tHow clumsy can you be!?!?\n" + // 12
				"Wait a minute, isn't that Silverlight there\tunder your feet?\n" + // 13
				"There's no time to waste!\tYou better get going!\n" + // 14
				"You've saved us all!\tYou're a hero!\n" + // 15
				"Young adventurer, I have found your Silverlight.\tBut it is not empowered!\tThat means Delrith will return!\tHere take it, please save us!\n" + // 16
				"{pi}She hands you the Legendary Silverlight.\n" + // 17
				"Who do you think you are, hadesflames?\n" + // 18
				"Fine, I'll go ask some other idiot...\n"; // 19
		String splitDialogue[] = messyDialogue.split("\n");
		dialogue = new QuestDialogue[splitDialogue.length];
		for(int i = 0; i<dialogue.length; i++)
			dialogue[i] = new QuestDialogue(splitDialogue[i], nexts[i], emotes[i]);			
	}
	public void handleDelrithDefeat(){
		completed = true;
		demonstrated = false;
		spawned = false;
		delrithId = -1;
		player.getPA().movePlayer(player.absX, player.absY, 0);
		saveQuest();
	}
	public void handleSwordEmpowerment(){
		boolean found = false;
		if(player.playerEquipment[player.playerWeapon] == SILVER_LIGHT){
			player.getItems().deleteEquipment(0, player.playerWeapon);
			player.inventory.addItem(SILVER_LIGHT_EMPOWERED, 1, -1);
			player.getItems().wearItem(SILVER_LIGHT_EMPOWERED, player.inventory.findItemSlot(SILVER_LIGHT_EMPOWERED));
			found = true;
		}
		if(!found){
			player.inventory.deleteItem(SILVER_LIGHT, 1);
			player.inventory.addItem(SILVER_LIGHT_EMPOWERED, 1, -1);
		}
		player.sendMessage("As Delrith is banished to another dimension, you feel your blade being empowered.");
	}
	public void chatResponse(int response){
		if(dialogueId < 0 || dialogueId >= dialogue.length)
			return;
		dialogueId = dialogue[dialogueId].next[response];
		if(dialogueId < 0){
			dialogueId = 0;
			player.getPA().closeAllWindows();
			return;
		}
		handleDialogue(false);
	}
	public void handleDialogue(boolean start){
		if(start){
			if(player.inventory.freeSlots() < 2){
				player.sendMessage("You need at least 2 free inventory slots to start this quest.");
				return;
			}
			dialogueId = 0;
		}
		if(dialogueId < 0){
			player.getQuestHandler().questChat = false;
			dialogueId = 0;
			player.getPA().closeAllWindows();
			return;
		}else if(dialogueId == 7 && demonstrated && !Server.itemHandler.itemExists(SILVER_LIGHT, player.absX, player.absY, player.heightLevel, 10))
			dialogueId = 12;
		else if(dialogueId == 7 && demonstrated)
			dialogueId = 13;
		else if(dialogueId == 9 && demonstrated)
			dialogueId = 14;
		if(!isInQuest && completed && !player.bank.bankHasItem(SILVER_LIGHT_EMPOWERED) && !player.inventory.hasItem(SILVER_LIGHT_EMPOWERED) && player.playerEquipment[player.playerWeapon] != SILVER_LIGHT_EMPOWERED){
			isInQuest = true;
			completed = false;
			dialogueId = 16;
			saveQuest();
		}else if(completed){
			dialogueId = 15;
			isInQuest = false;
			saveQuest();
		}
		QuestDialogue qd = dialogue[dialogueId];
		player.getQuestHandler().questChat = true;
		player.getDH().emote = qd.emote < 0 ? DialogueHandler.CALM : qd.emote;
		if(qd.playerInfo){
			player.getDH().sendStatement(qd.dialogue[0]);
			if(dialogueId == 17){
				demonstrated = true;
				player.inventory.addItem(SILVER_LIGHT, 1, -1);
			}else{
				player.inventory.addItem(RUSTY_SWORD, 1, -1);
				player.inventory.addItem(SILVER_BAR, 1, -1);
				obtained = true;
			}
			dialogueId = qd.next[0];
		}else if(qd.playerOption){
			if(qd.options == 2)
				player.getDH().sendOption2(qd.dialogue[0], qd.dialogue[1]);
			else if(qd.options == 3)
				player.getDH().sendOption3(qd.dialogue[0], qd.dialogue[1], qd.dialogue[2]);
		}else{
			if(dialogueId == 11)
				demonstrated = true;
			else if(dialogueId == 12)
				Server.itemHandler.createGroundItem(player, SILVER_LIGHT, player.absX, player.absY, player.heightLevel, 1, player.playerId);
			dialogueId = qd.next[0];
			if(qd.options == 1)
				player.getDH().sendNpcChat1(qd.dialogue[0], ARIS_ID, ARIS);
			else if(qd.options == 2)
				player.getDH().sendNpcChat2(qd.dialogue[0], qd.dialogue[1], ARIS_ID, ARIS);
			else if(qd.options == 3)
				player.getDH().sendNpcChat3(qd.dialogue[0], qd.dialogue[1], qd.dialogue[2], ARIS_ID, ARIS);
			else if(qd.options == 4)
				player.getDH().sendNpcChat4(qd.dialogue[0], qd.dialogue[1], qd.dialogue[2], qd.dialogue[3], ARIS_ID, ARIS);
		}
		saveQuest();
	}
	public void saveQuest(){
		File quest = new File("./Data/quests/" + player.originalName);
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(quest.getAbsolutePath() + "/demonslayer.txt"))){
			bw.write("isInQuest = ", 0, 12);
			bw.write(Boolean.toString(isInQuest), 0, Boolean.toString(isInQuest).length());
			bw.newLine();
			bw.write("obtained = ", 0, 11);
			bw.write(Boolean.toString(obtained), 0, Boolean.toString(obtained).length());
			bw.newLine();
			bw.write("demonstrated = ", 0, 15);
			bw.write(Boolean.toString(demonstrated), 0, Boolean.toString(demonstrated).length());
			bw.newLine();
			bw.write("completed = ", 0, 12);
			bw.write(Boolean.toString(completed), 0, Boolean.toString(completed).length());
		}catch(Exception e){}
	}
}