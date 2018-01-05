package server.model.players;

import server.model.minigames.HowlOfDeathManager;
import server.util.Misc;

public class DialogueHandler{

	private Client c;

	public DialogueHandler(Client client){
		this.c = client;
	}

	public int emote = 9847;
	public static final int SEND_NO_EMOTE = -1;
	public static final int CONFUSED = 9827;
	public static final int GASP = 9750;
	public static final int CALM = 9760;
	public static final int CRYING = 9765;
	public static final int SHY = 9770;
	public static final int SAD = 9775;
	public static final int SCARED = 9780;
	public static final int MAD = 9785;
	public static final int ANGRY = 9790;
	public static final int CRAZY = 9795;
	public static final int CRAZY_2 = 9800;
	public static final int SAYS_NOTHING = 9805;
	public static final int JUST_TALK_NO_ANIMATION = 9810;
	public static final int YEAH = 9815;
	public static final int DISGUSTED = 9820;
	public static final int NO_WAY = 9823;
	public static final int DRUNK = 9835;
	public static final int LAUGH = 9854;
	public static final int HEAD_SWAY_TALK = 9845;
	public static final int HAPPY_TALK = 9847;
	public static final int STIFF = 9855;
	public static final int STIFF_EYES_MOVE = 9860;
	public static final int PRIDE_FULL = 9865;
	public static final int DEMENTED = 9870;
	/**
	 * Handles all talking
	 * 
	 * @param dialogue
	 *            The dialogue you want to use
	 * @param npcId
	 *            The npc id that the chat will focus on during the chat
	 */
	public void sendDialogues(int dialogue, int npcId){
		c.talkingNpc = npcId;
		switch(dialogue){
			case 0:
				c.talkingNpc = -1;
				c.getPA().removeAllWindows();
				c.nextChat = 0;
				break;
			case 1:
				sendStatement("You found a hidden tunnel! Do you want to enter it?");
				c.dialogueAction = 1;
				c.nextChat = 2;
				break;
			case 2:
				sendOption2("Yea! I'm fearless!", "No way! That looks scary!");
				c.dialogueAction = 1;
				c.nextChat = 0;
				break;
			case 3:
				emote = HAPPY_TALK;
				sendNpcChat4("Hello!", "My name is Duradel and I am a master of the slayer skill.", "I can assign you a slayer task suitable to your combat level.", "Would you like a slayer task?", c.talkingNpc, "Duradel");
				c.nextChat = 4;
				break;
			case 5:
				emote = HAPPY_TALK;
				sendNpcChat4("Hello adventurer...", "My name is Kolodion, the master of this mage bank.", "Would you like to play a minigame in order ", "to earn points towards recieving magic related prizes?", c.talkingNpc, "Kolodion");
				c.nextChat = 6;
				break;
			case 6:
				emote = HAPPY_TALK;
				sendNpcChat4("The way the game works is as follows...", "You will be teleported to the wilderness,", "You must kill mages to recieve points,", "redeem points with the chamber guardian.", c.talkingNpc, "Kolodion");
				c.nextChat = 15;
				break;
			case 11:
				emote = HAPPY_TALK;
				sendNpcChat4("Hello!", "My name is Duradel and I am a master of the slayer skill.", "I can assign you a slayer task suitable to your combat level.", "Would you like a slayer task?", c.talkingNpc, "Duradel");
				c.nextChat = 12;
				break;
			case 12:
				sendOption2("Yes I would like a slayer task.", "No I would not like a slayer task.");
				c.dialogueAction = 5;
				break;
			case 13:
				emote = HAPPY_TALK;
				sendNpcChat4("Hello!", "My name is Duradel and I am a master of the slayer skill.", "I see I have already assigned you a task to complete.", "Would you like me to change your task for 20 slayer points?", c.talkingNpc, "Duradel");
				c.nextChat = 14;
				break;
			case 14:
				sendOption2("Yes I would like to change my task for 20 slayer points.", "No I would like to keep my task.");
				c.dialogueAction = 6;
				break;
			case 15:
				sendOption2("Yes I would like to play", "No, sounds too dangerous for me.");
				c.dialogueAction = 7;
				break;
			case 16:
				sendOption2("I would like to reset my barrows brothers.", "I would like to fix all my barrows");
				c.dialogueAction = 8;
				break;
			case 17:
				sendOption5("Air", "Fire", "Water", "Earth", "More");
				c.dialogueAction = 10;
				c.dialogueId = 17;
				c.teleAction = -1;
				break;
			case 18:
				sendOption5("Mind", "Body", "Cosmic", "Astral", "More");
				c.dialogueAction = 11;
				c.dialogueId = 18;
				c.teleAction = -1;
				break;
			case 19:
				sendOption5("Nature", "Law", "Death", "Blood", "More");
				c.dialogueAction = 12;
				c.dialogueId = 19;
				c.teleAction = -1;
				break;
			case 20:
				emote = HAPPY_TALK;
				sendNpcChat4("Hello adventurer! I was once a", "guide on RuneScape, until hades offered me better pay.", "I am here to inform you that if you need help", "You should join the \"help\" clan chat.", npcId, "hades5 Guide");
				c.nextChat = 0;
				break;
			case 21:
				emote = HAPPY_TALK;
				sendNpcChat3("Hello there", "I can repair your broken Ancient armour.", "Just use it on me if you have any.", npcId, "hades5 Guide");
				c.nextChat = 0;
				break;
			case 22:
				emote = HEAD_SWAY_TALK;
				sendNpcChat1("Hey, can you keep a secret?", npcId, "Thessalia");
				c.nextChat = 23;
				break;
			case 23:
				sendOption2("Yes", "No");
				c.dialogueAction = 9;
				c.dialogueId = 23;
				break;
			case 24:
				emote = HAPPY_TALK;
				sendNpcChat4("Great!", "You see, I found this really neat hat", "Called the Rainbow partyhat!", "But I really need a partyhat set...", npcId, "Thessalia");
				c.nextChat = 26;
				break;
			case 25:
				emote = SAD;
				sendNpcChat1("Oh, nevermind then...", npcId, "Thessalia");
				c.nextChat = 0;
				break;
			case 26:
				emote = SHY;
				sendNpcChat3("Would you be willing to trade", "A partyhat set for my Rainbow partyhat?", "It would really mean the world to me!", npcId, "Thessalia");
				c.nextChat = 27;
				break;
			case 27:
				sendOption3("Sure!", "I don't have a partyhat set.", "No, but I hear Nex has a set, talk to her!");
				c.dialogueAction = 1;
				c.dialogueId = 27;
				break;
			case 28:
				String response[] = c.swapHats();
				if(response[1].isEmpty()){
					emote = MAD;
					sendNpcChat1(response[0], npcId, "Thessalia");
				}else{
					emote = HAPPY_TALK;
					sendNpcChat2(response[0], response[1], npcId, "Thessalia");
				}
				c.nextChat = 0;
				break;
			case 29:
				emote = SAD;
				sendNpcChat1("Oh, well thanks anyway.", npcId, "Thessalia");
				c.nextChat = 0;
				break;
			case 30:
				emote = JUST_TALK_NO_ANIMATION;
				sendNpcChat1("Thanks for the tip!", npcId, "Thessalia");
				c.nextChat = 0;
				break;
			case 31:
				emote = HAPPY_TALK;
				sendNpcChat2("Hey, I can decant all your potions for you, noted or not!", "If you want, just right click on me and select decant.", npcId, "Bob Barter");
				c.nextChat = 0;
				break;
			case 32:
				emote = HAPPY_TALK;
				sendNpcChat1("So, you want to learn about herblore eh?", npcId, "Bob Barter");
				c.nextChat = 33;
				break;
			case 33:
				emote = HAPPY_TALK;
				sendNpcChat1("Well, piss off...", npcId, "Bob Barter");
				c.nextChat = 0;
				break;
			case 34:
				emote = HAPPY_TALK;
				sendNpcChat1("There you go mate!", npcId, "Bob Barter");
				c.nextChat = 0;
				break;
			case 35:
				emote = CALM;
				sendNpcChat3("Hello young adventurer", "I'm your friendly neighborhood Assassin.", "Would you like to test your assassination skills?", npcId, "Assassin");
				c.nextChat = 36;
				break;
			case 36:
				sendOption4("Sure, I'm up for it! (This is NOT a safe mini-game)", "No way, I'm a coward...", "Can I have a weapon like yours?", "What do you have for sale?");
				c.dialogueAction = 5;
				c.dialogueId = 36;
				break;
			case 37:
				emote = HAPPY_TALK;
				sendPlayerChat("Sure, I'm up for it!");
				c.nextChat = 44;
				break;
			case 38:
				emote = SCARED;
				sendPlayerChat("No way, I'm a coward...");
				c.nextChat = 45;
				break;
			case 39:
				sendStatement("All of a sudden, you feel a sharp blow to the back of your head.");
				c.nextChat = 40;
				break;
			case 40:
				c.getPA().removeAllWindows();
				int hit = Misc.random_range(1, 7);
				hit = hit >= c.playerLevel[c.playerHitpoints] ? (c.playerLevel[c.playerHitpoints] - 1) : hit;
				c.playerLevel[c.playerHitpoints] -= hit;
				c.handleHitMask(hit);
				c.getPA().refreshSkill(c.playerHitpoints);
				HowlOfDeathManager.getInstance().addPlayer(c);
				c.nextChat = 0;
				break;
			case 41:
				emote = CALM;
				sendNpcChat2("Sorry about that", "I couldn't let you see the way to our base.", npcId, "Assassin");
				c.nextChat = 42;
				break;
			case 42:
				emote = ANGRY;
				sendPlayerChat("Couldn't you have just given me a blindfold?!");
				c.nextChat = 43;
				break;
			case 43:
				emote = GASP;
				sendNpcChat1("A blindfold...?", npcId, "Assassin");
				c.nextChat = 0;
				break;
			case 44:
				emote = LAUGH;
				sendNpcChat1("Great! I knew you'd be up for it!", npcId, "Assassin");
				c.nextChat = 39;
				break;
			case 45:
				emote = MAD;
				sendNpcChat1("You disgust me, get out of my sight!", npcId, "Assassin");
				c.nextChat = 0;
				break;
			case 46:
				emote = LAUGH;
				sendNpcChat1("Nah, you don't look smart enough to know how to use it!", npcId, "Assassin");
				c.nextChat = 0;
				break;
			case 47:
				emote = HAPPY_TALK;
				sendPlayerChat("Can I have a weapon like yours?");
				c.nextChat = 46;
				break;
			case 48:
				emote = SAD;
				sendNpcChat1("Sorry, I left my inventory back at the duel arena.", npcId, "Assassin");
				c.nextChat = 0;
				break;
			case 49:
				sendOption5("How did you die?", "What is your name?", "Can you do any tricks?", "Want a new hat?", "Want to go scare some people?");
				c.dialogueId = 49;
				break;
			case 50:
				emote = SHY;
				sendPlayerChat("Hey, Head?");
				c.nextChat = 51;
				break;
			case 51:
				sendNpcChat1("What?", 2868, "Zombie Head");
				c.nextChat = npcId;
				break;
			case 52:
				emote = SHY;
				sendPlayerChat("How did you die?");
				c.nextChat = 53;
				break;
			case 53:
				emote = SAD;
				sendNpcChat1("I stuck my neck out for an old friend.", 2868, "Zombie Head");
				c.nextChat = 54;
				break;
			case 54:
				sendPlayerChat("You shouldn't get so cut up about it.");
				c.nextChat = 55;
				break;
			case 55:
				sendNpcChat2("Well if I keep it all bottled up I'll turn into a total head", "case.", 2868, "Zombie Head");
				c.nextChat = 0;
				break;
			case 56:
				emote = CALM;
				sendPlayerChat("What is your name?");
				c.nextChat = 57;
				break;
			case 57:
				emote = SHY;
				sendNpcChat1("Mumblemumblemumble...", 2868, "Zombie Head");
				c.nextChat = 58;
				break;
			case 58:
				emote = CONFUSED;
				sendPlayerChat("What was that?");
				c.nextChat = 59;
				break;
			case 59:
				emote = SHY;
				sendNpcChat1("My name is Edward Cranium.", 2868, "Zombie Head");
				c.nextChat = 60;
				break;
			case 60:
				emote = LAUGH;
				sendPlayerChat2("Edd Cranium?", "Hahahahahahahahahahaha!");
				c.nextChat = 61;
				break;
			case 61:
				emote = MAD;
				sendNpcChat1("Har Har...", 2868, "Zombie Head");
				c.nextChat = 0;
				break;
			case 62:
				emote = SHY;
				sendPlayerChat("Hey, Head?");
				c.nextChat = npcId;
				break;
			case 63:
				emote = MAD;
				sendNpcChat1("What now?", 2868, "Zombie Head");
				c.nextChat = 64;
				break;
			case 64:
				emote = CALM;
				sendPlayerChat("Can you do any tricks?");
				c.nextChat = 65;
				break;
			case 65:
				emote = SAD;
				sendNpcChat1("Not anymore...", 2868, "Zombie Head");
				c.nextChat = 66;
				break;
			case 66:
				emote = CONFUSED;
				sendPlayerChat("How come?");
				c.nextChat = 67;
				break;
			case 67:
				emote = SAD;
				sendNpcChat2("Because I used to be able to do a handstand for", "over an hour while juggling cannonballs on my feet...", 2868, "Zombie Head");
				c.nextChat = 68;
				break;
			case 68:
				emote = GASP;
				sendPlayerChat("Wow, you were quite the entertainer.");
				c.nextChat = 69;
				break;
			case 69:
				emote = SAD;
				sendNpcChat1("Yep. Now i can barely roll my eyes...", 2868, "Zombie Head");
				c.nextChat = 70;
				break;
			case 70:
				emote = CALM;
				sendPlayerChat("I know what you can do!");
				c.nextChat = 71;
				break;
			case 71:
				emote = CONFUSED;
				sendNpcChat1("What?", 2868, "Zombie Head");
				c.nextChat = 72;
				break;
			case 72:
				emote = CALM;
				sendPlayerChat("Vent...");
				c.nextChat = 73;
				break;
			case 73:
				emote = MAD;
				sendNpcChat1("Don't even suggest it!", 2868, "Zombie Head");
				c.nextChat = 74;
				break;
			case 74:
				emote = SHY;
				sendPlayerChat("Ok.");
				c.nextChat = 0;
				break;
			case 75:
				emote = SAD;
				sendNpcChat1("Can't I rest in peace?", 2868, "Zombie Head");
				c.nextChat = 76;
				break;
			case 76:
				emote = HAPPY_TALK;
				sendPlayerChat2("No!", "Would you like a new hat?");
				c.nextChat = 77;
				break;
			case 77:
				emote = CALM;
				sendNpcChat1("No, but could you screw a handle into the top of my head?", 2868, "Zombie Head");
				c.nextChat = 78;
				break;
			case 78:
				emote = CONFUSED;
				sendPlayerChat("A handle? Why?");
				c.nextChat = 79;
				break;
			case 79:
				emote = MAD;
				sendNpcChat2("Because currently you wave me about by my hair", "and it hurts.", 2868, "Zombie Head");
				c.nextChat = 0;
				break;
			case 80:
				emote = SAD;
				sendNpcChat1("Will you ever leave me alone?", 2868, "Zombie Head");
				c.nextChat = 81;
				break;
			case 81:
				emote = HAPPY_TALK;
				sendPlayerChat2("No!", "Want to go scare some people?");
				c.nextChat = 82;
				break;
			case 82:
				emote = SHY;
				sendNpcChat1("Let's leave it for now.", 2868, "Zombie Head");
				c.nextChat = 83;
				break;
			case 83:
				emote = SAD;
				sendPlayerChat2("All right...", "We'll quit while we're ahead!");
				c.nextChat = 84;
				break;
			case 84:
				emote = SHY;
				sendNpcChat2("If I try really hard I might be able to will myself", "deader...", 2868, "Zombie Head");
				c.nextChat = 0;
				break;
			case 85:
				emote = CALM;
				sendNpcChat2("Go to the boxes down stairs and build puppets", "Then bring them to me, so that I can string them.", c.talkingNpc, "Rosie");
				c.nextChat = 0;
				break;
			case 86:
				emote = HAPPY_TALK;
				sendNpcChat1("Great! Come back when you have another one of these.", c.talkingNpc, "Rosie");
				c.nextChat = 0;
				break;
			case 87:
				emote = CALM;
				sendNpcChat1("Okay, now give me all the strung marionettes.", c.talkingNpc, "Rosie");
				c.nextChat = 0;
				break;
			case 88:
				emote = LAUGH;
				sendNpcChat2("Well done! You've saved Christmas this year!", "Please take this marionette as a token of my appreciation!", c.talkingNpc, "Rosie");
				c.nextChat = 0;
				break;
			case 89:
				emote = SCARED;
				sendNpcChat1("The Revenants are back!!", c.talkingNpc, "Ghost disciples");
				c.nextChat = 90;
				break;
			case 90:
				emote = SAD;
				sendPlayerChat("Err, sorry?");
				c.nextChat = 91;
				break;
			case 91:
				emote = SCARED;
				sendNpcChat3("The Revenants!", "They're a group of dead warrior ghosts", "They're really strong and scary!", c.talkingNpc, "Ghost disciple");
				c.nextChat = 92;
				break;
			case 92:
				emote = SCARED;
				sendNpcChat2("You look like a strong adventurer", "Will you help get rid of them?", c.talkingNpc, "Ghost disciple");
				c.nextChat = 93;
				break;
			case 93:
				sendOption2("Sure I will!", "Fuck that...");
				c.dialogueAction = 10;
				break;
			case 94:
				emote = HAPPY_TALK;
				sendNpcChat3("Great!", "Go kill as many of them as you can", "Bring back what they drop as proof, and I'll reward you!", c.talkingNpc, "Ghost disciple");
				c.nextChat = 0;
				break;
			case 95:
				emote = SCARED;
				sendNpcChat1("They'll kill us all!", c.talkingNpc, "Ghost disciple");
				c.nextChat = 0;
				break;
			case 96:
				emote = HAPPY_TALK;
				sendNpcChat2("Hello adventurer", "How goes your revenant rampage?", c.talkingNpc, "Ghost disciple");
				c.nextChat = 97;
				break;
			case 97:
				sendOption3("It's going well.", "I keep dying...", "Let me have a look at your rewards.");
				c.dialogueAction = 2;
				break;
			case 98:
				emote = HAPPY_TALK;
				sendNpcChat1("Great to hear.", c.talkingNpc, "Ghost disciple");
				c.nextChat = 0;
				break;
			case 99:
				emote = DISGUSTED;
				sendNpcChat1("I guess those big steroid muscles must be for show then.", c.talkingNpc, "Ghost disciple");
				c.nextChat = 0;
				break;
		}
	}

	/*
	 * Information Box
	 */
	public void sendStartInfo(String text, String text1, String text2, String text3, String title){
		c.getPA().sendText(title, 6180);
		c.getPA().sendText(text, 6181);
		c.getPA().sendText(text1, 6182);
		c.getPA().sendText(text2, 6183);
		c.getPA().sendText(text3, 6184);
		c.getPA().sendFrame164(6179);
	}

	/*
	 * Options
	 */

	public void sendOption(String s, String s1){
		c.getPA().sendText("Select an Option", 2470);
		c.getPA().sendText(s, 2471);
		c.getPA().sendText(s1, 2472);
		c.getPA().sendText("Click here to continue", 2473);
		c.getPA().sendFrame164(2469);
	}

	public void sendOption2(String s, String s1){
		c.getPA().sendText("Select an Option", 2460);
		c.getPA().sendText(s, 2461);
		c.getPA().sendText(s1, 2462);
		c.getPA().sendFrame164(2459);
	}

	public void sendOption3(String s, String s1, String s2){
		c.getPA().sendText("Select an Option", 2470);
		c.getPA().sendText(s, 2471);
		c.getPA().sendText(s1, 2472);
		c.getPA().sendText(s2, 2473);
		c.getPA().sendFrame164(2469);
	}

	public void sendOption4(String s, String s1, String s2, String s3){
		c.getPA().sendText("Select an Option", 2481);
		c.getPA().sendText(s, 2482);
		c.getPA().sendText(s1, 2483);
		c.getPA().sendText(s2, 2484);
		c.getPA().sendText(s3, 2485);
		c.getPA().sendFrame164(2480);
	}

	public void sendOption5(String s, String s1, String s2, String s3, String s4){
		c.getPA().sendText("Select an Option", 2493);
		c.getPA().sendText(s, 2494);
		c.getPA().sendText(s1, 2495);
		c.getPA().sendText(s2, 2496);
		c.getPA().sendText(s3, 2497);
		c.getPA().sendText(s4, 2498);
		c.getPA().sendFrame164(2492);
	}

	/*
	 * Statements
	 */
	public void sendStatement(String s){ // 1 line click here to continue chat box interface
		c.getPA().sendText(s, 357);
		c.getPA().sendText("Click here to continue", 358);
		c.getPA().sendFrame164(356);
	}

	/*
	 * Npc Chatting
	 */
	public void sendNpcChat1(String s, int ChatNpc, String name){
		c.getPA().sendFrame200(4883, emote);
		c.getPA().sendText(name, 4884);
		c.getPA().sendText(s, 4885);
		c.getPA().sendFrame75(ChatNpc, 4883);
		c.getPA().sendFrame164(4882);
	}

	public void sendNpcChat2(String s, String s1, int ChatNpc, String name) {
		c.getPA().sendFrame200(4888, emote);
		c.getPA().sendText(name, 4889);
		c.getPA().sendText(s, 4890);
		c.getPA().sendText(s1, 4891);
		c.getPA().sendFrame75(ChatNpc, 4888);
		c.getPA().sendFrame164(4887);
	}
	
	public void sendNpcChat3(String s, String s1, String s2, int ChatNpc, String name) {
		c.getPA().sendFrame200(4894, emote);
		c.getPA().sendText(name, 4895);
		c.getPA().sendText(s, 4896);
		c.getPA().sendText(s1, 4897);
		c.getPA().sendText(s2, 4898);
		c.getPA().sendFrame75(ChatNpc, 4894);
		c.getPA().sendFrame164(4893);
	}
	
	public void sendNpcChat4(String s, String s1, String s2, String s3, int ChatNpc, String name){
		c.getPA().sendFrame200(4901, emote);
		c.getPA().sendText(name, 4902);
		c.getPA().sendText(s, 4903);
		c.getPA().sendText(s1, 4904);
		c.getPA().sendText(s2, 4905);
		c.getPA().sendText(s3, 4906);
		c.getPA().sendFrame75(ChatNpc, 4901);
		c.getPA().sendFrame164(4900);
	}

	/*
	 * Player Chating Back
	 */

	public void sendPlayerChat(String s){
		c.getPA().sendFrame200(969, emote);
		c.getPA().sendText(c.playerName2, 970);
		c.getPA().sendText(s, 971);
		c.getPA().sendFrame185(969);
		c.getPA().sendFrame164(968);
	}

	public void sendPlayerChat2(String s, String s1){
		c.getPA().sendFrame200(974, emote);
		c.getPA().sendText(c.playerName2, 975);
		c.getPA().sendText(s, 976);
		c.getPA().sendText(s1, 977);
		c.getPA().sendFrame185(974);
		c.getPA().sendFrame164(973);
	}

	public void sendPlayerChat3(String s, String s1, String s2){
		c.getPA().sendFrame200(980, emote);
		c.getPA().sendText(c.playerName2, 981);
		c.getPA().sendText(s, 982);
		c.getPA().sendText(s1, 983);
		c.getPA().sendText(s2, 984);
		c.getPA().sendFrame185(980);
		c.getPA().sendFrame164(979);
	}

	public void sendPlayerChat4(String s, String s1, String s2, String s3){
		c.getPA().sendFrame200(987, emote);
		c.getPA().sendText(c.playerName2, 988);
		c.getPA().sendText(s, 989);
		c.getPA().sendText(s1, 990);
		c.getPA().sendText(s2, 991);
		c.getPA().sendText(s3, 992);
		c.getPA().sendFrame185(987);
		c.getPA().sendFrame164(986);
	}
}
