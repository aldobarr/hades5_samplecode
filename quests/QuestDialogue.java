package server.model.quests;

public class QuestDialogue{
	public int options, next[], emote;
	public boolean playerOption = false, playerInfo = false;
	public String dialogue[];
	
	public QuestDialogue(String dialogue, int next[], int emote){
		this.dialogue = dialogue.split("\t");
		this.emote = emote;
		this.next = next;
		options = this.dialogue.length;
		if(this.dialogue[0].contains("{po}")){
			playerOption = true;
			this.dialogue[0] = this.dialogue[0].replace("{po}", "");
		}else if(this.dialogue[0].contains("{pi}")){
			playerInfo = true;
			this.dialogue[0] = this.dialogue[0].replace("{pi}", "");
		}
	}
}