package server.model.players;

import server.Config;
import server.Connection;
import server.Server;
import server.model.minigames.CastleWars;
import server.model.minigames.Duel;
import server.model.minigames.FightPits;
import server.model.minigames.HowlOfDeathManager;
import server.model.npcs.NPC;
import server.model.npcs.NPCHandler;
import server.model.players.skills.fishing.Spot;
import server.model.quests.DemonSlayer;
import server.model.region.Location;
import server.model.region.RegionManager;
import server.util.Misc;
import server.world.Clan;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Calendar;
import java.util.HashMap;

public class PlayerAssistant{
	private Client c;

	public PlayerAssistant(Client Client){
		this.c = Client;
	}

	public void writeCommandLog(String command){
		checkDateAndTime();
		String filePath = "./Data/CommandLog.txt";
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true));){
			bw.write("[" + c.date + "]" + "-" + "[" + c.currentTime + " " + checkTimeOfDay() + "]: " + "[" + c.playerName + "]: " + "[" + c.connectedFrom + "] " + "::" + command);
			bw.newLine();
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
	}

	public void loadIgnore(){
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				c.getOutStream().createFrame(214);
				c.getOutStream().writeWord(c.ignores.length * 8);
				for(int i = 0; i < c.ignores.length; i++){
					if(c.ignores[i] == 0)
						c.getOutStream().writeQWord(-1);
					else
						c.getOutStream().writeQWord(c.ignores[i]);
				}
				c.flushOutStream();
			}
		}
	}

	public String checkTimeOfDay(){
		Calendar cal = new GregorianCalendar();
		int TIME_OF_DAY = cal.get(Calendar.AM_PM);
		if(TIME_OF_DAY > 0)
			return "PM";
		else
			return "AM";
	}

	public void checkDateAndTime(){
		Calendar cal = new GregorianCalendar();

		int YEAR = cal.get(Calendar.YEAR);
		int MONTH = cal.get(Calendar.MONTH) + 1;
		int DAY = cal.get(Calendar.DAY_OF_MONTH);
		int HOUR = cal.get(Calendar.HOUR_OF_DAY);
		int MIN = cal.get(Calendar.MINUTE);
		int SECOND = cal.get(Calendar.SECOND);

		String day = "";
		String month = "";
		String hour = "";
		String minute = "";
		String second = "";

		if(DAY < 10)
			day = "0" + DAY;
		else
			day = "" + DAY;
		if(MONTH < 10)
			month = "0" + MONTH;
		else
			month = "" + MONTH;
		if(HOUR < 10)
			hour = "0" + HOUR;
		else
			hour = "" + HOUR;
		if(MIN < 10)
			minute = "0" + MIN;
		else
			minute = "" + MIN;
		if(SECOND < 10)
			second = "0" + SECOND;
		else
			second = "" + SECOND;

		c.date = day + "/" + month + "/" + YEAR;
		c.currentTime = hour + ":" + minute + ":" + second;
	}

	Properties p = new Properties();

	public void loadAnnouncements(){
		try(FileInputStream announce = new FileInputStream("./Announcements.ini");){
			p.load(announce);
			Enumeration<?> em = p.keys();
			ArrayList<String> announcements = new ArrayList<String>();
			while(em.hasMoreElements()){
				String key = (String)em.nextElement();
				key = p.getProperty(key);
				if(key.length() > 0)
					announcements.add(key);
			}
			for(int i = announcements.size() - 1; i >= 0; i--)
				c.sendMessage(announcements.get(i));
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public int CraftInt, Dcolor, FletchInt;

	/**
	 * MulitCombat icon
	 * 
	 * @param i1
	 *            0 = off 1 = on
	 */
	public void multiWay(int i1){
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				c.outStream.createFrame(61);
				c.outStream.writeByte(i1);
				c.updateRequired = true;
				c.setAppearanceUpdateRequired(true);
			}
		}
	}

	public void clearClanChat(){
		c.clanId = "";
		c.getPA().sendText("Talking in: ", 18139);
		c.getPA().sendText("Owner: ", 18140);
		for(int j = 18144; j < 18244; j++)
			c.getPA().sendText("", j);
	}

	public void resetAutocast(){
		c.autocastId = 0;
		c.autocasting = false;
		c.getPA().sendConfig(108, 0);
	}

	public void createPlayersObjectAnim(int x, int y, int animationID, int tileObjectType, int orientation){
		try{
			c.getOutStream().createFrame(85);
			c.getOutStream().writeByteC(y - (c.mapRegionY * 8));
			c.getOutStream().writeByteC(x - (c.mapRegionX * 8));
			c.getOutStream().createFrame(160);
			c.getOutStream().writeByteS(((0 & 7) << 4) + (0 & 7));      
			c.getOutStream().writeByteS((tileObjectType<<2) +(orientation&3));
			c.getOutStream().writeWordA(animationID);// animation id
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void sendText(String s, int id){
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				c.getOutStream().createFrameVarSizeWord(126);
				c.getOutStream().writeString(s);
				c.getOutStream().writeWordA(id);
				c.getOutStream().endFrameVarSizeWord();
				c.flushOutStream();
			}
		}
	}

	public void dimLight(){
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				c.getOutStream().createFrame(111);
				c.flushOutStream();
			}
		}
	}

	public void brightLight(){
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				c.getOutStream().createFrame(112);
				c.flushOutStream();
			}
		}
	}

	public void sendLink(String s){
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				c.getOutStream().createFrameVarSizeWord(187);
				c.getOutStream().writeString(s);
			}
		}
	}

	public void setSkillLevel(int skillNum, int currentLevel, int XP){
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				c.getOutStream().createFrame(134);
				c.getOutStream().writeByte(skillNum);
				c.getOutStream().writeDWord_v1(XP);
				c.getOutStream().writeByte(currentLevel);
				c.flushOutStream();
			}
		}
	}

	public void sendFrame106(int sideIcon){
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				c.getOutStream().createFrame(106);
				c.getOutStream().writeByteC(sideIcon);
				c.flushOutStream();
				requestUpdates();
			}
		}
	}

	public void createCutScene1(int x, int y, int z, int turnSpeed, int angle){
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				c.inCutScene = true;
				c.outStream.createFrame(177);
				c.outStream.writeByte(x);
			    c.outStream.writeByte(y);
			    c.outStream.writeWord(z);
			    c.outStream.writeByte(turnSpeed);
			    c.outStream.writeByte(angle);
			    c.flushOutStream();
			}
		}
	}
	
	public void createCutScene2(int x, int y, int z, int turnSpeed, int movement){
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				c.inCutScene = true;
				c.outStream.createFrame(166);
				c.outStream.writeByte(x);
			    c.outStream.writeByte(y);
			    c.outStream.writeWord(z);
			    c.outStream.writeByte(turnSpeed);
			    c.outStream.writeByte(movement);
			    c.flushOutStream();
			}
		}
	}
	
	public void resetScreen(){
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				c.inCutScene = false;
				c.getOutStream().createFrame(107);
				c.flushOutStream();
				c.updateRequired = true;
				c.appearanceUpdateRequired = true;
			}
		}
	}

	public void closeInput(){
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				c.getOutStream().createFrame(28);
				c.flushOutStream();
			}
		}
	}

	public void sendConfig(int id, int state){
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				c.getOutStream().createFrame(36);
				c.getOutStream().writeWordBigEndian(id);
				c.getOutStream().writeByte(state);
				c.flushOutStream();
			}
		}
	}

	public void sendConfig(int id, boolean state){
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				c.getOutStream().createFrame(36);
				c.getOutStream().writeWordBigEndian(id);
				c.getOutStream().writeByte(state ? 1 : 0);
				c.flushOutStream();
			}
		}
	}

	public void sendRun(){
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				c.getOutStream().createFrame(150);
				c.getOutStream().writeByte(c.isRunning ? 1 : 0);
				c.flushOutStream();
			}
		}
	}

	public void sendRest(){
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				c.getOutStream().createFrame(152);
				c.getOutStream().writeByte(c.resting ? 1 : 0);
				c.flushOutStream();
			}
		}
	}

	public void sendFrame185(int Frame){
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				c.getOutStream().createFrame(185);
				c.getOutStream().writeWordBigEndianA(Frame);
			}
		}
	}

	public void showInterface(int interfaceid){
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				c.getOutStream().createFrame(97);
				c.getOutStream().writeWord(interfaceid);
				c.flushOutStream();
			}
		}
	}

	public void sendFrame248(int MainFrame, int SubFrame){
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				c.getOutStream().createFrame(248);
				c.getOutStream().writeWordA(MainFrame);
				c.getOutStream().writeWord(SubFrame);
				c.flushOutStream();
			}
		}
	}
	
	public void sendFrame246(int MainFrame, int SubFrame, int SubFrame2){
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				c.getOutStream().createFrame(246);
				c.getOutStream().writeWordBigEndian(MainFrame);
				c.getOutStream().writeWord(SubFrame);
				c.getOutStream().writeWord(SubFrame2);
				c.flushOutStream();
			}
		}
	}

	public void sendFrame171(int MainFrame, int SubFrame){
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				c.getOutStream().createFrame(171);
				c.getOutStream().writeByte(MainFrame);
				c.getOutStream().writeWord(SubFrame);
				c.flushOutStream();
			}
		}
	}

	public void sendFrame200(int MainFrame, int SubFrame){
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				c.getOutStream().createFrame(200);
				c.getOutStream().writeWord(MainFrame);
				c.getOutStream().writeWord(SubFrame);
				c.flushOutStream();
			}
		}
	}

	public void sendFrame70(int i, int o, int id){
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				c.getOutStream().createFrame(70);
				c.getOutStream().writeWord(i);
				c.getOutStream().writeWordBigEndian(o);
				c.getOutStream().writeWordBigEndian(id);
				c.flushOutStream();
			}
		}
	}

	public void sendFrame75(int MainFrame, int SubFrame){
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				c.getOutStream().createFrame(75);
				c.getOutStream().writeWordBigEndianA(MainFrame);
				c.getOutStream().writeWordBigEndianA(SubFrame);
				c.flushOutStream();
			}
		}
	}

	public void sendFrame164(int Frame){
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				c.getOutStream().createFrame(164);
				c.getOutStream().writeWordBigEndian_dup(Frame);
				c.flushOutStream();
			}
		}
	}

	public void setPrivateMessaging(int i){ // friends and ignore list status
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				c.getOutStream().createFrame(221);
				c.getOutStream().writeByte(i);
				c.flushOutStream();
			}
		}
	}

	public void resetSap(int id){
		id -= 27;
		c.sapTicks[id] = 0;
		c.sapAmount[id] = 1.0;
	}

	public void resetLeech(int id){
		id -= 36;
		c.leechTicks[id] = 0;
		c.leechAmount[id] = 1.0;
	}

	public void setChatOptions(int publicChat, int privateChat, int tradeBlock){
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				c.getOutStream().createFrame(206);
				c.getOutStream().writeByte(publicChat);
				c.getOutStream().writeByte(privateChat);
				c.getOutStream().writeByte(tradeBlock);
				c.flushOutStream();
			}
		}
	}

	public void sendFrame87(int id, int state){
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				c.getOutStream().createFrame(87);
				c.getOutStream().writeWordBigEndian_dup(id);
				c.getOutStream().writeDWord_v1(state);
				c.flushOutStream();
			}
		}
	}

	public void sendPM(long name, int rights, byte[] chatmessage, int messagesize){
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				c.getOutStream().createFrameVarSize(196);
				c.getOutStream().writeQWord(name);
				c.getOutStream().writeDWord(c.lastChatId++);
				c.getOutStream().writeByte(rights);
				c.getOutStream().writeBytes(chatmessage, messagesize, 0);
				c.getOutStream().endFrameVarSize();
				c.flushOutStream();
			}
		}
	}

	public void createPlayerHints(int type, int id){
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				c.getOutStream().createFrame(254);
				c.getOutStream().writeByte(type);
				c.getOutStream().writeWord(id);
				c.getOutStream().write3Byte(0);
				c.flushOutStream();
			}
		}
	}

	public void createObjectHints(int x, int y, int height, int pos){
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				c.getOutStream().createFrame(254);
				c.getOutStream().writeByte(pos);
				c.getOutStream().writeWord(x);
				c.getOutStream().writeWord(y);
				c.getOutStream().writeByte(height);
				c.flushOutStream();
			}
		}
	}

	public void loadPM(long playerName, int world){
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				world += world != 0 ? 9 : !Config.WORLD_LIST_FIX ? 1 : 0;
				c.getOutStream().createFrame(50);
				c.getOutStream().writeQWord(playerName);
				c.getOutStream().writeByte(world);
				c.flushOutStream();
			}
		}
	}

	public void removeAllWindows(){
		if(c != null && c.getOutStream() != null){
			c.duelInterface = false;
			synchronized(c){
				c.getPA().resetVariables();
				c.getOutStream().createFrame(219);
				c.flushOutStream();
				c.isShopping = false;
				c.isBanking = false;
				c.getTradeAndDuel().declineTrade(true);
			}
		}
	}
	
	public void closeTrade(){
		if(c != null && c.getOutStream() != null){
			c.duelInterface = false;
			synchronized(c){
				c.getPA().resetVariables();
				c.getOutStream().createFrame(219);
				c.flushOutStream();
				c.isShopping = false;
				c.isBanking = false;
			}
		}
	}

	public void closeAllWindows(){
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				c.getOutStream().createFrame(219);
				c.flushOutStream();
				c.isBanking = false;
				c.isShopping = false;
				c.getTradeAndDuel().declineTrade(true);
			}
		}
	}
	
	public void closeAllWindows2(){
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				c.getOutStream().createFrame(219);
				c.flushOutStream();
				c.isBanking = false;
				c.isShopping = false;
			}
		}
	}

	public void showItemsKeptOnDeath(){
		int tempKept = 0;
		if(!c.isSkulled && !c.isInFala())
			tempKept = 3;
		if((c.prayerActive[10] || c.prayerActive[26]) && !c.isInFala())
			tempKept++;
		int numKept = tempKept > 3 ? 3 : tempKept;
		SortedItems<DeathItem> Items = new SortedItems<DeathItem>();
		for(int i = 0; i < c.inventory.items.length; i++)
			if(c.inventory.items[i] != null)
				Items.add(new DeathItem(c.inventory.items[i].id - 1, c.inventory.items[i].amount, c.getShops().getFixedItemValue(c.inventory.items[i].id - 1)));
		for(int i = 0; i < c.playerEquipment.length; i++)
			if(c.playerEquipment[i] > 0 && c.playerEquipmentN[i] > 0)
				Items.add(new DeathItem(c.playerEquipment[i], c.playerEquipmentN[i], c.getShops().getFixedItemValue(c.playerEquipment[i])));
		int count = 0;
		for(int i = 0; i < numKept; i++){
			for(int j = 0; j < Items.size(); j++){
				if(Items.get(j).amount > 0){
					Items.get(j).amount--;
					sendFrame35(6963, Items.get(j).id, i, 1);
					break;
				}
			}
		}
		for(int j = 0; j < Items.size(); j++){
			if(Items.get(j).amount > 0){
				sendFrame35(6822, Items.get(j).id, count++, Items.get(j).amount);
				Items.get(j).amount = 0;
			}
		}
		// 6963 = Items Kept.
		// 6822 = Items Lost.
		showInterface(17100);
		sendText("~ " + tempKept + " ~", 17108);
	}

	public void sendFrame35(int frame, int item, int slot, int amount){
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				c.outStream.createFrameVarSizeWord(34);
				c.outStream.writeWord(frame);
				c.outStream.writeByte(slot);
				c.outStream.writeWord(item + 1);
				c.outStream.writeByte(255);
				c.outStream.writeDWord(amount);
				c.outStream.endFrameVarSizeWord();
			}
		}
	}

	public void sendFrame34(int id, int slot, int column, int amount){
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				c.outStream.createFrameVarSizeWord(34); // init item to smith screen
				c.outStream.writeWord(column); // Column Across Smith Screen
				c.outStream.writeByte(4); // Total Rows?
				c.outStream.writeDWord(slot); // Row Down The Smith Screen
				c.outStream.writeWord(id + 1); // item
				c.outStream.writeByte(amount); // how many there are?
				c.outStream.endFrameVarSizeWord();
			}
		}
	}

	public void walkableInterface(int id){
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				c.getOutStream().createFrame(208);
				c.getOutStream().writeWordBigEndian_dup(id);
				c.flushOutStream();
			}
		}
	}

	public int mapStatus = 0;

	public void sendFrame99(int state){ // used for disabling map
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				if(mapStatus != state){
					mapStatus = state;
					c.getOutStream().createFrame(99);
					c.getOutStream().writeByte(state);
					c.flushOutStream();
				}
			}
		}
	}

	/**
	 * Reseting animations for everyone
	 **/

	public void frame1(){
		synchronized(c){
			for(Player p : RegionManager.getLocalPlayers(c)){
				if(p != null){
					Client person = (Client)p;
					if(person != null){
						if(person.getOutStream() != null && !person.disconnected){
							if(c.distanceToPoint(person.getX(), person.getY()) <= 25){
								person.getOutStream().createFrame(1);
								person.flushOutStream();
								person.getPA().requestUpdates();
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Creating projectile
	 **/
	public void createProjectile(int x, int y, int offX, int offY, int angle, int speed, int gfxMoving, int startHeight, int endHeight, int lockon, int time){
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				c.getOutStream().createFrame(85);
				c.getOutStream().writeByteC((y - (c.getMapRegionY() * 8)) - 2);
				c.getOutStream().writeByteC((x - (c.getMapRegionX() * 8)) - 3);
				c.getOutStream().createFrame(117);
				c.getOutStream().writeByte(angle);
				c.getOutStream().writeByte(offY);
				c.getOutStream().writeByte(offX);
				c.getOutStream().writeWord(lockon);
				c.getOutStream().writeWord(gfxMoving);
				c.getOutStream().writeByte(startHeight);
				c.getOutStream().writeByte(endHeight);
				c.getOutStream().writeWord(time);
				c.getOutStream().writeWord(speed);
				c.getOutStream().writeByte(16);
				c.getOutStream().writeByte(64);
				c.flushOutStream();
			}
		}
	}

	public void createProjectile2(int x, int y, int offX, int offY, int angle, int speed, int gfxMoving, int startHeight, int endHeight, int lockon, int time, int slope){
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				c.getOutStream().createFrame(85);
				c.getOutStream().writeByteC((y - (c.getMapRegionY() * 8)) - 2);
				c.getOutStream().writeByteC((x - (c.getMapRegionX() * 8)) - 3);
				c.getOutStream().createFrame(117);
				c.getOutStream().writeByte(angle);
				c.getOutStream().writeByte(offY);
				c.getOutStream().writeByte(offX);
				c.getOutStream().writeWord(lockon);
				c.getOutStream().writeWord(gfxMoving);
				c.getOutStream().writeByte(startHeight);
				c.getOutStream().writeByte(endHeight);
				c.getOutStream().writeWord(time);
				c.getOutStream().writeWord(speed);
				c.getOutStream().writeByte(slope);
				c.getOutStream().writeByte(64);
				c.flushOutStream();
			}
		}
	}

	// projectiles for everyone within 25 squares
	public void createPlayersProjectile(int x, int y, int offX, int offY, int angle, int speed, int gfxMoving, int startHeight, int endHeight, int lockon, int time){
		synchronized(c){
			for(Player p : RegionManager.getLocalPlayers(c.getLocation())){
				if(p != null){
					Client person = (Client)p;
					if(person != null){
						if(person.getOutStream() != null){
							if(person.distanceToPoint(x, y) <= 25){
								if(p.heightLevel == c.heightLevel)
									person.getPA().createProjectile(x, y, offX, offY, angle, speed, gfxMoving, startHeight, endHeight, lockon, time);
							}
						}
					}
				}
			}
		}
	}

	public void createPlayersProjectile2(int x, int y, int offX, int offY, int angle, int speed, int gfxMoving, int startHeight, int endHeight, int lockon, int time, int slope){
		synchronized(c){
			for(Player p : RegionManager.getLocalPlayers(c.getLocation())){
				if(p != null){
					Client person = (Client)p;
					if(person != null){
						if(person.getOutStream() != null){
							if(person.distanceToPoint(x, y) <= 25){
								person.getPA().createProjectile2(x, y, offX, offY, angle, speed, gfxMoving, startHeight, endHeight, lockon, time, slope);
							}
						}
					}
				}
			}
		}
	}

	/**
	 ** GFX
	 **/
	public void stillGfx(int id, int x, int y, int height, int time){
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				c.getOutStream().createFrame(85);
				c.getOutStream().writeByteC(y - (c.getMapRegionY() * 8));
				c.getOutStream().writeByteC(x - (c.getMapRegionX() * 8));
				c.getOutStream().createFrame(4);
				c.getOutStream().writeByte(0);
				c.getOutStream().writeWord(id);
				c.getOutStream().writeByte(height);
				c.getOutStream().writeWord(time);
				c.flushOutStream();
			}
		}
	}

	// creates gfx for everyone
	public void createPlayersStillGfx(int id, int x, int y, int height, int time){
		synchronized(c){
			for(Player p : RegionManager.getLocalPlayers(Location.create(x, y))){
				if(p != null){
					Client person = (Client)p;
					if(person != null){
						if(person.getOutStream() != null){
							if(person.distanceToPoint(x, y) <= 25){
								person.getPA().stillGfx(id, x, y, height, time);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Objects, add and remove
	 **/
	public void object(int objectId, int objectX, int objectY, int face, int objectType){
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				c.getOutStream().createFrame(85);
				c.getOutStream().writeByteC(objectY - (c.getMapRegionY() * 8));
				c.getOutStream().writeByteC(objectX - (c.getMapRegionX() * 8));
				c.getOutStream().createFrame(101);
				c.getOutStream().writeByteC((objectType << 2) + (face & 3));
				c.getOutStream().writeByte(0);

				if(objectId != -1){ // removing
					c.getOutStream().createFrame(151);
					c.getOutStream().writeByteS(0);
					c.getOutStream().writeWordBigEndian(objectId);
					c.getOutStream().writeByteS((objectType << 2) + (face & 3));
				}
				c.flushOutStream();
			}
		}
	}

	public void checkObjectSpawn(int objectId, int objectX, int objectY, int face, int objectType){
		if(c.distanceToPoint(objectX, objectY) > 60)
			return;
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				c.getOutStream().createFrame(85);
				c.getOutStream().writeByteC(objectY - (c.getMapRegionY() * 8));
				c.getOutStream().writeByteC(objectX - (c.getMapRegionX() * 8));
				c.getOutStream().createFrame(101);
				c.getOutStream().writeByteC((objectType << 2) + (face & 3));
				c.getOutStream().writeByte(0);

				if(objectId != -1){ // removing
					c.getOutStream().createFrame(151);
					c.getOutStream().writeByteS(0);
					c.getOutStream().writeWordBigEndian(objectId);
					c.getOutStream().writeByteS((objectType << 2) + (face & 3));
				}
				c.flushOutStream();
			}
		}
	}

	/**
	 * Show option, attack, trade, follow etc
	 **/
	public String optionType = "null";

	public void showOption(int i, int l, String s, int a){
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				if(!optionType.equalsIgnoreCase(s)){
					optionType = s;
					c.getOutStream().createFrameVarSize(104);
					c.getOutStream().writeByteC(i);
					c.getOutStream().writeByteA(l);
					c.getOutStream().writeString(s);
					c.getOutStream().endFrameVarSize();
					c.flushOutStream();
				}
			}
		}
	}

	/**
	 * Open bank
	 **/
	/*
	 * public void openUpBank(){ synchronized(c){ if(c.inWild() && !c.safeZone()
	 * && !c.inOwnersArray(c.playerName)){
	 * c.sendMessage("You can't bank right now!"); return; } if(c != null &&
	 * c.getOutStream() != null){ c.getPA().closeAllWindows(); c.isBanking =
	 * true; c.getItems().resetItems(5064); c.getItems().rearrangeBank();
	 * c.bank.resetBank(); c.getItems().resetTempItems();
	 * c.getOutStream().createFrame(248); c.getOutStream().writeWordA(5292);
	 * c.getOutStream().writeWord(5063); c.flushOutStream(); } } }
	 */
	public void openUpBank(){
		if(c.teleporting || c.inHowlOfDeath)
			return;
		if(!c.canBank)
			return;
		if(c.inTrade || c.tradeStatus == 1){
			Client o = (Client)PlayerHandler.players[c.tradeWith];
			if(o != null)
				o.getTradeAndDuel().declineTrade(true);
		}
		if(c.duel != null && c.duel.status <= 2){
			Client o = c.duel.getOtherPlayer(c.playerId);
			Duel.declineDuel(c, o != null);
		}
		if(c.playerRights != 3 && ((c.duel != null && c.duel.status >= 3) || (!c.safeZone() && (c.inWild() || c.isInArd() || c.isInFala())) || c.inClanWars || c.inFightPits() || c.inCwGame || c.inCwWait || (c.inNexGame && (c.nexGames == null || !c.nexGames.canBank)) || c.isInNexLair() || c.inNomad() || (c.inNexGamesArea() && (c.nexGames == null || !c.nexGames.canBank))))
			return;
		if(c.setPin){
			if(c.getBankPin().getFullPin().equalsIgnoreCase("")){
				c.getBankPin().open();
				return;
			}
		}
		c.canBank = false;
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				c.inventory.resetItems(5064);
				c.bank.resetBank();
				c.inventory.resetTempItems();
				c.getOutStream().createFrame(248);
				c.getOutStream().writeWordA(51200);
				c.getOutStream().writeWord(5063);
				c.flushOutStream();
				c.getPA().sendConfig(576, 0);
				c.isSearching = false;
				c.getPA().sendText("", 51237);
				if(!c.bankStringSent){
					sendText("The Bank of " + c.playerName2, 5383);
					c.bankStringSent = true;
				}
				c.isBanking = true;
			}
		}
	}

	public void openUpOtherBank(){
		if(c.setPin){
			if(c.getBankPin().getFullPin().equalsIgnoreCase("")){
				c.getBankPin().open();
				return;
			}
		}
		if(c.inTrade || c.tradeStatus == 1){
			Client o = (Client)PlayerHandler.players[c.tradeWith];
			if(o != null)
				o.getTradeAndDuel().declineTrade(true);
		}
		if(c.duel != null && c.duel.status <= 2){
			Client o = c.duel.getOtherPlayer(c.playerId);
			Duel.declineDuel(c, o != null);
		}
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				c.inventory.resetItems(5064);
				c.oBank.resetBank();
				c.inventory.resetTempItems();
				c.getOutStream().createFrame(248);
				c.getOutStream().writeWordA(51200);
				c.getOutStream().writeWord(5063);
				c.flushOutStream();
				if(!c.bankStringSent){
					sendText("The Bank of " + c.playerName2, 5383);
					c.bankStringSent = true;
				}
				c.isBanking = true;
			}
		}
	}

	public boolean ignoreContains(long id){
		for(long uid : c.ignores)
			if(id == uid)
				return true;
		return false;
	}

	/**
	 * Private Messaging
	 **/

	public void logIntoPM(){
		setPrivateMessaging(2);
		long cname = Misc.playerNameToInt64(c.playerName);
		for(Player p : PlayerHandler.players){
			Client o = (Client)p;
			if(o != null){
				if(!c.getPA().ignoreContains(Misc.playerNameToInt64(o.playerName)) || (o.playerRights > 0 && o.playerRights < 5)){
					o.getPA().updatePM(c.playerId, 1);
				}
			}
		}
		boolean pmLoaded = false;
		for(int i = 0; i < c.friends.length; i++){
			if(c.friends[i] != 0){
				for(int i2 = 1; i2 < Config.MAX_PLAYERS; i2++){
					Player p = PlayerHandler.players[i2];
					if(p == null)
						continue;
					Client o = (Client)p;
					if(o != null){
						if(p.isActive && Misc.playerNameToInt64(p.playerName) == c.friends[i]){
							if((o.getPA().ignoreContains(cname) || (!o.getPA().isInPM(cname) && o.privateChat == 1) || o.privateChat == 2) && (c.playerRights == 0 || c.playerRights == 5))
								loadPM(c.friends[i], 0);
							else if(c.playerRights >= 1 || p.privateChat == 0 || (p.privateChat == 1 && o.getPA().isInPM(cname))){
								loadPM(c.friends[i], 1);
								pmLoaded = true;
							}
							break;
						}
					}
				}
				if(!pmLoaded){
					loadPM(c.friends[i], 0);
				}
				pmLoaded = false;
			}
			for(Player p : PlayerHandler.players){
				Client o = (Client)p;
				if(o != null){
					long oname = Misc.playerNameToInt64(o.playerName);
					if((!c.getPA().ignoreContains(oname) && (c.privateChat == 0 || (c.privateChat == 1 && c.getPA().isInPM(oname)))) || (o.playerRights > 0 && o.playerRights < 5)){
						o.getPA().updatePM(c.playerId, 1);
					}else
						o.getPA().updatePM(c.playerId, 0);
				}
			}
		}
	}

	public void updatePM(int pID, int world){ // used for private chat updates
		Player p = PlayerHandler.players[pID];
		if(p == null || p.playerName == null || p.playerName.equals("null")){
			return;
		}
		Client o = (Client)p;
		if(o != null){
			long l = Misc.playerNameToInt64(PlayerHandler.players[pID].playerName);
			if(p.privateChat == 0){
				for(int i = 0; i < c.friends.length; i++){
					if(c.friends[i] != 0){
						if(l == c.friends[i]){
							loadPM(l, world);
							return;
						}
					}
				}
			}else if(p.privateChat == 1){
				for(int i = 0; i < c.friends.length; i++){
					if(c.friends[i] != 0){
						if(l == c.friends[i]){
							if(o.getPA().isInPM(Misc.playerNameToInt64(c.playerName))){
								loadPM(l, world);
								return;
							}else{
								loadPM(l, 0);
								return;
							}
						}
					}
				}
			}else if(p.privateChat == 2){
				for(int i = 0; i < c.friends.length; i++){
					if(c.friends[i] != 0){
						if(l == c.friends[i] && c.playerRights < 2){
							loadPM(l, 0);
							return;
						}
					}
				}
			}
		}
	}

	public boolean isInPM(long l){
		for(int i = 0; i < c.friends.length; i++){
			if(c.friends[i] != 0){
				if(l == c.friends[i]){
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Drink AntiPosion Potions
	 * 
	 * @param itemId
	 *            The itemId
	 * @param itemSlot
	 *            The itemSlot
	 * @param newItemId
	 *            The new item After Drinking
	 * @param healType
	 *            The type of poison it heals
	 */
	public void potionPoisonHeal(int itemId, int itemSlot, int newItemId, int healType){
		c.attackTimer = c.getCombat().getAttackDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
		if(c.duel != null && c.duel.rules != null && c.duel.rules.duelRule[5] && c.duel.status == 3){
			c.sendMessage("Potions has been disabled in this duel!");
			return;
		}
		if(!c.isDead && System.currentTimeMillis() - c.foodDelay > 2000){
			if(c.inventory.hasItem(itemId, 1, itemSlot, true)){
				c.sendMessage("You drink the " + c.getItems().getItemName(itemId).toLowerCase() + ".");
				c.foodDelay = System.currentTimeMillis();
				// Actions
				if(healType == 1){
					// Cures The Poison
				}else if(healType == 2){
					// Cures The Poison + protects from getting poison again
				}
				c.startAnimation(0x33D);
				c.inventory.deleteItem(itemId, itemSlot, 1);
				c.inventory.addItem(newItemId, 1, -1);
				requestUpdates();
			}
		}
	}

	/**
	 * Magic on items
	 **/

	public void magicOnItems(int slot, int itemId, int spellId){
		if(c.playerRights == 3)
			System.out.println("Spell ID: " + spellId);
		switch(spellId){
			case 1162: // low alch
				if(System.currentTimeMillis() - c.alchDelay > 2000){
					if(!c.getCombat().checkMagicReqs(49)){
						break;
					}
					if(itemId == 995){
						c.sendMessage("You can't alch coins");
						break;
					}
					if(!c.inventory.hasItem(itemId, 1, slot, true))
						break;
					c.inventory.deleteItem(itemId, slot, 1);
					c.inventory.addItem(995, c.getShops().getItemShopValue(itemId) / 3, -1);
					if(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase().contains("staff")){
						c.startAnimation(9625);
						c.gfx0(1692);
					}else{
						c.startAnimation(c.MAGIC_SPELLS[49][2]);
						c.gfx0(c.MAGIC_SPELLS[49][3]);
					}
					c.alchDelay = System.currentTimeMillis();
					sendFrame106(6);
					addSkillXP(c.MAGIC_SPELLS[49][7] * Config.MAGIC_EXP_RATE, 6);
					refreshSkill(6);
				}
				break;				
			case 1178: // high alch
				if(System.currentTimeMillis() - c.alchDelay > 2000){
					if(!c.getCombat().checkMagicReqs(50)){
						break;
					}
					if(itemId == 995){
						c.sendMessage("You can't alch coins");
						break;
					}
					if(!c.inventory.hasItem(itemId, 1, slot, true))
						break;
					c.inventory.deleteItem(itemId, slot, 1);
					c.inventory.addItem(995, (int)(c.getShops().getItemShopValue(itemId) * .75), -1);
					if(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase().contains("staff")){
						c.startAnimation(9633);
						c.gfx0(1693);
					}else{
						c.startAnimation(c.MAGIC_SPELLS[50][2]);
						c.gfx0(c.MAGIC_SPELLS[50][3]);
					}
					c.alchDelay = System.currentTimeMillis();
					sendFrame106(6);
					addSkillXP(c.MAGIC_SPELLS[50][7] * Config.MAGIC_EXP_RATE, 6);
					refreshSkill(6);
				}
				break;
			case 1173:
				if(System.currentTimeMillis() - c.alchDelay > 2000){
					if(!c.getCombat().checkMagicReqs(53)){
						break;
					}
					c.alchDelay = System.currentTimeMillis();
					if((itemId == DemonSlayer.RUSTY_SWORD && c.inventory.hasItem(DemonSlayer.SILVER_BAR)) ||
						itemId == DemonSlayer.SILVER_BAR && c.inventory.hasItem(DemonSlayer.RUSTY_SWORD)){
						c.startAnimation(c.MAGIC_SPELLS[53][2]);
						c.gfx100(c.MAGIC_SPELLS[53][3]);
						c.inventory.deleteItem(DemonSlayer.RUSTY_SWORD, 1);
						c.inventory.deleteItem(DemonSlayer.SILVER_BAR, 1);
						c.inventory.addItem(DemonSlayer.SILVER_LIGHT, 1, -1);
						addSkillXP(c.MAGIC_SPELLS[50][7] * Config.MAGIC_EXP_RATE, 6);
						break;
					}
				}
				break;
		}
	}

	public void wrathGfx(){
		for(int x = -2; x<=2; x += 2)
			for(int y = -2; y<=2; y += 2)
				if((x == 0 && y != 0) || (x != 0 && y == 0) || (x != 0 && y != 0))
					gfx(2260, c.absX + x, c.absY + y, c.heightLevel);
		gfx(2260, c.absX + 1, c.absY + 1, c.heightLevel);
		gfx(2260, c.absX + 1, c.absY - 1, c.heightLevel);
		gfx(2260, c.absX - 1, c.absY + 1, c.heightLevel);
		gfx(2260, c.absX - 1, c.absY - 1, c.heightLevel);
	}
	
	/**
	 * heighted gfx.
	 */
	public void gfx(int id, int x, int y, int height){
		for(Player p : RegionManager.getLocalPlayers(c.getLocation())){
			Client o = (Client)p;
			if(o != null && o.getOutStream() != null){
				synchronized(o){
					o.getOutStream().createFrame(85);
					o.getOutStream().writeByteC(y - (c.getMapRegionY() * 8));
					o.getOutStream().writeByteC(x - (c.getMapRegionX() * 8));
					o.getOutStream().createFrame(4);
					o.getOutStream().writeByte(0);
					o.getOutStream().writeWord(id);
					o.getOutStream().writeByte(height);
					o.getOutStream().writeWord(5);
				}
			}
		}
	}
	
	public void resetOverLoad(){
		c.overloaded = new int[][]{{0, 0}, {1, 0}, {2, 0}, {4, 0}, {6, 0}};
		c.overloadedBool = false;
		c.overLoad = 0;
		c.overloadTick = 0;
		c.overloadTime = 0;
		c.lastOverload = 0;
	}

	/**
	 * No height.
	 */
	public void gfx(int id, int x, int y){
		gfx(id, x, y, 0);
	}

	/**
	 * Dieing
	 **/

	public void applyDead(){
		if(c.nexTicketHP > -1 && c.isInNexLair()){
			if(c.inventory.hasItem(13663)){
				c.inventory.deleteItem(13663, 1);
				c.playerLevel[c.playerHitpoints] = c.nexTicketHP;
				refreshSkill(c.playerHitpoints);
				c.respawnTimer = -6;
				c.isDead = c.isDead2 = false;
				c.nexTicketHP = -1;
				return;
			}
		}
		int weapon = c.playerEquipment[c.playerWeapon];
		c.respawnTimer = 15;
		c.getCombat().resetPlayerAttack();
		removeAllWindows();
		c.tradeResetNeeded = true;
		c.killerId = findKiller();
		Client o = (Client)PlayerHandler.players[c.killerId];
		if(o != null){
			if(c.killerId != c.playerId)
				o.Rating++;
			o.playerKilled = c.playerId;
		}
		if(weapon == CastleWars.SARA_BANNER || weapon == CastleWars.ZAMMY_BANNER){
			c.getItems().deleteEquipment(weapon, c.playerWeapon);
			CastleWars.dropFlag(c, weapon);
		}
		c.faceUpdate(0);
		c.npcIndex = 0;
		c.playerIndex = 0;
		c.stopMovement();
		if((c.duel == null || (c.duel != null && c.duel.status < 3)) && !c.inClanWars && !c.inPits && !c.inHowlOfDeath){
			c.sendMessage("Oh dear you are dead!");
			int tpoints = ((!o.inClanWars && !o.inCwGame && !o.inPits && !o.connectedFrom.equalsIgnoreCase(c.connectedFrom) && !o.lastKillIP.equalsIgnoreCase(c.connectedFrom)) ? (o.isInFala() ? 2 : 1) : 0);
			o.pkPoints += tpoints;
			if(c != null && o != null && c.playerId != o.killerId){
				c.deaths++;
				o.kills++;
			}
			if(!o.inPits)
				o.lastKillIP = c.connectedFrom;
			if(o != null && !o.inClanWars && !o.inCwGame && !o.inPits && c.playerId != o.playerId){
				Server.pkScoreBoard.update(c.originalName, c.playerName2, false);
				Server.pkScoreBoard.update(o.originalName, o.playerName2, true);
			}
		}else if(c.duel != null && c.duel.status == 3){
			if(c.duel.getOtherPlayer(c.playerId) != null){
				c.duel.getOtherPlayer(c.playerId).poisonDamage = -1;
				c.duel.getOtherPlayer(c.playerId).poisonImmune = 0;
			}
			c.duel.isDead = true;
			c.sendMessage("You have lost the duel!");
		}
		if(CastleWars.isInCw(c)){
			c.cwDeaths += 1;
			o.cwKills += 1;
		}
		c.logoutDelay = System.currentTimeMillis();
		o.logoutDelay = System.currentTimeMillis();
		resetDamageDone();
		c.specAmount = 10;
		c.getItems().addSpecialBar(c.playerEquipment[c.playerWeapon]);
		c.lastVeng = 0;
		c.vengOn = false;
		resetFollowers();
		c.attackTimer = 10;
	}

	public void resetDamageDone(){
		for(int i = 0; i < PlayerHandler.players.length; i++){
			if(PlayerHandler.players[i] != null){
				PlayerHandler.players[i].damageTaken[c.playerId] = 0;
			}
		}
	}

	public void castVeng(){
		if(c.playerMagicBook != Config.LUNAR)
			return;
		if(c.playerLevel[6] < 94){
			c.sendMessage("You need a magic level of 94 to cast this spell.");
			return;
		}
		if(c.playerLevel[1] < 40){
			c.sendMessage("You need a defence level of 40 to cast this spell.");
			return;
		}
		if(c.duel != null && c.duel.rules != null && c.duel.rules.duelRule[4] && c.duel.status == 3){
			c.sendMessage("Magic has been disabled in this duel.");
			return;
		}
		if(!c.inventory.hasItem(9075, 4) || !c.inventory.hasItem(557, 10) || !c.inventory.hasItem(560, 2)){
			c.sendMessage("You don't have the required runes to cast this spell.");
			return;
		}
		if(System.currentTimeMillis() - c.lastCast < 30000){
			c.sendMessage("You can only cast vengeance every 30 seconds.");
			return;
		}
		if(c.vengOn){
			c.sendMessage("You have already cast vengeance.");
			return;
		}
		c.gfx100(726);
		c.startAnimation(4410);
		c.inventory.deleteItem(9075, 4);
		c.inventory.deleteItem(557, 10);// For these you need to change to
										// deleteItem(item, itemslot, amount);.
		c.inventory.deleteItem(560, 2);
		addSkillXP(10000, 6);
		refreshSkill(6);
		c.vengOn = true;
		c.lastCast = System.currentTimeMillis();
	}

	public void resetTb(){
		c.teleBlockLength = 0;
		c.teleBlockDelay = 0;
	}

	public void resetFollowers(){
		for(int j = 0; j < PlayerHandler.players.length; j++){
			if(PlayerHandler.players[j] != null){
				if(PlayerHandler.players[j].followId == c.playerId){
					Client c = (Client)PlayerHandler.players[j];
					c.getPA().resetFollow();
				}
			}
		}
	}

	public void giveLife(){
		removeAllWindows();
		c.getPA().resetAntiFire();
		c.canSummonNomad = false;
		c.respawnTimer = 6;
		c.tradeResetNeeded = true;
		c.mJavHit = 0;
		c.mJavTime = 0;
		c.faceUpdate(-1);
		c.playerIndex = 0;
		c.teleTick = 6;
		c.underAttackBy = 0;
		c.duel2h = false;
		c.ignoreClip = false;
		c.specAmount = 10.0;
		c.doubleHit = false;
		c.specHit = false;
		c.usingSpecial = false;
		c.specEffect = 0;
		c.projectileStage = 0;
		c.getItems().updateSpecialBar();
		c.ignoreClipTick = 0;
		c.freezeTimer = -6;
		c.poisonDamage = 0;
		c.canSummonNomad = false;
		if(c.inHowlOfDeath)
			c.howlOfDeath.removePlayer(c);
		if(c.inTrade)
			c.getTradeAndDuel().declineTrade(true);
		if(c.duel != null && c.duel.status < 3)
			Duel.declineDuel(c, true);
		if(c.inNexGame){
			if(c.nexGames != null)
				c.nexGames.handleLeaderDeath(c);
			c.resetNex();
		}
		boolean clanWarsDrop = c.inClanWars && c.inClanWars() && Server.clanChat.clans.containsKey(c.clanId) ? !Server.clanChat.clans.get(c.clanId).war.getKeepItems() : true;
		if(!CastleWars.isInCw(c) && !c.isInCastleWars() && !c.getPA().inPitsWait() && (c.duel == null || (c.duel != null && c.duel.status < 3)) && !c.arenas() && !c.inDuelArena() && !c.inPestGame){ // if we are not in a duel we must be in wildy so remove items
			if((c.inZombiesGame && !c.inCanifis()) || !c.inZombiesGame){
				if(!c.inPits && !c.inFightCaves() && clanWarsDrop){
					c.getItems().resetKeepItems();
					if((c.playerRights >= 2 && c.playerRights != 5 && Config.ADMIN_DROP_ITEMS) || c.playerRights < 2 || c.playerRights == 5){
						if(!c.isSkulled && !c.isInFala()){ // what items to keep
							c.getItems().keepItem(0, true);
							c.getItems().keepItem(1, true);
							c.getItems().keepItem(2, true);
						}
						if((c.prayerActive[10] || c.prayerActive[26]) && !c.isInFala()){
							c.getItems().keepItem(3, true);
						}
						if((c.duel == null || (c.duel != null && c.duel.status < 3)) && !c.inFightCaves() & !c.inPits){
							c.getItems().loseItems();
							c.getItems().deleteAllItems();
							c.getItems().dropAllLostItems(); // drop all items
						}
						if(!c.isSkulled && !c.isInFala()){ // add the kept items once we finish deleting and dropping them
							for(int i1 = 0; i1 < 3; i1++){
								if(c.itemKeptId[i1] > 0){
									c.inventory.addItem(c.itemKeptId[i1], 1, -1);
								}
							}
						}
						if((c.prayerActive[10] || c.prayerActive[26]) && !c.isInFala()){ // if we have protect items
							if(c.itemKeptId[3] > 0){
								c.inventory.addItem(c.itemKeptId[3], 1, -1);
							}
						}
					}
					c.getItems().resetKeepItems();
				}else if(c.inPits){
					Server.fightPits.removePlayerFromPits(c.playerId);
					c.pitsStatus = 1;
				}
			}
		}
		if(c.zombies != null)
			c.zombies.handleDeath(c);
		c.getCombat().resetPrayers();
		for(int i = 0; i < 20; i++){
			c.playerLevel[i] = getLevelForXP(c.playerXP[i]);
			c.getPA().refreshSkill(i);
		}
		resetOverLoad();
		if(c.pitsStatus == 1){
			c.getPA().movePlayer(2399, 5177, 0);
			c.inPits = false;
			c.addedToPits = false;
		}else if(c.inClanWars() && c.inClanWars){
			Clan clan = Server.clanChat.clans.containsKey(c.clanId) ? Server.clanChat.clans.get(c.clanId) : null;
			int x = clan != null ? clan.war.getJail(clan)[0] + Misc.random(2) : Config.RESPAWN_X;
			int y = clan != null ? clan.war.getJail(clan)[1] + Misc.random(8) : Config.RESPAWN_Y;
			int h = clan != null ? clan.war.getHeight() : 0;
			movePlayer(x, y, h);
			clan.warMembersLeft--;
			clan.war.getOpposingTeam(clan).kills++;
			c.inCWJail = true;
			c.cwJailTime = Misc.currentTimeSeconds() + 30;
		}/*else if(c.inPestGame){
			Server.pestControl.movePlayer(c.playerId);
		}*/else if(CastleWars.isInCw(c)){
			int x = CastleWars.getTeamNumber(c) == 1 ? 2426 + Misc.random(3) : 2373 + Misc.random(3), y = CastleWars.getTeamNumber(c) == 1 ? 3076 - Misc.random(3) : 3131 - Misc.random(3);
			c.absX = x;
			c.absY = y;
			c.getPA().movePlayer(x, y, 1);
			c.inCWBase = true;
		}else if((c.duel != null && c.duel.status == 3) || c.inDuelArena() || c.inDuel()){
			if(c.duel != null){
				Client o = c.duel.getOtherPlayer(c.playerId);
				c.duel.duelCount = 0;
				c.duel.status = 4;
				if(o != null && (c.duel.winner == -1 || c.duel.winner == o.playerId)){
					c.duel.winner = o.playerId;
					o.getPA().createPlayerHints(10, -1);
					o.duel.duelVictory(o);
					o.poisonDamage = -1;
				}else
					c.duel.claimStakedItems(c);
			}
			c.absX = Config.DUELING_RESPAWN_X + (Misc.random(Config.RANDOM_DUELING_RESPAWN));
			c.absY = Config.DUELING_RESPAWN_Y + (Misc.random(Config.RANDOM_DUELING_RESPAWN));
			c.heightLevel = 0;
			movePlayer(c.absX, c.absY, 0);
			c.duel.resetDuel(c);
			PlayerSave.saveGame(c);
		}else{
			if(c.inFightCaves())
				c.getPA().resetTzhaar();
			movePlayer(Config.RESPAWN_X, Config.RESPAWN_Y, 0);
			c.isSkulled = false;
			c.skullTimer = 0;
			c.attackedPlayers.clear();
			if(((Misc.random(99) + 1) > 60) && c.playerRights == 3 && c.getItems().hasQuincyBow()){
				Client o = (Client)PlayerHandler.players[c.killedBy];
				if(o != null)
					Server.itemHandler.createGroundItem(o, 20097, c.getX(), c.getY(), c.heightLevel, 1, o.playerId);
			}
		}
		for(int i = 27; i < 31; i++)
			resetSap(i);
		for(int i = 36; i < 43; i++)
			resetLeech(i);
		PlayerSave.saveGame(c);
		c.getCombat().resetPlayerAttack();
		resetAnimation();
		c.startAnimation(65535);
		frame1();
		resetTb();
		c.specHit = false;
		c.removeDead = true;
		c.isDead = false;
		c.isSkulled = false;
		c.pitsStatus = 0;
		c.attackedPlayers.clear();
		c.headIconPk = -1;
		c.skullTimer = -1;
		c.damageTaken = new int[Config.MAX_PLAYERS];
		c.getPA().requestUpdates();
	}

	/**
	 * Location change for digging, levers etc
	 **/

	public void changeLocation(){
		switch(c.newLocation){
			case 1:
				sendFrame99(2);
				movePlayer(3578, 9706, 3);
				break;
			case 2:
				sendFrame99(2);
				movePlayer(3568, 9683, 3);
				break;
			case 3:
				sendFrame99(2);
				movePlayer(3557, 9703, 3);
				break;
			case 4:
				sendFrame99(2);
				movePlayer(3556, 9718, 3);
				break;
			case 5:
				sendFrame99(2);
				movePlayer(3534, 9704, 3);
				break;
			case 6:
				sendFrame99(2);
				movePlayer(3546, 9684, 3);
				break;
		}
		c.newLocation = 0;
	}

	/**
	 * Teleporting
	 **/
	public void spellTeleport(int x, int y, int height){
		if(c.overLoad > 0)
			return;
		// c.teleProcess = true;
		if(c.inPits)
			Server.fightPits.removePlayerFromPits(c.playerId);
		if(c.inCaveGame)
			c.getPA().resetTzhaar();
		c.getPA().startTeleport(x, y, height, false);
	}

	public void startMovement(int x, int y, int height){
		if(c.overLoad > 0)
			return;
		if(c.inPits)
			Server.fightPits.removePlayerFromPits(c.playerId);
		if(c.duel != null && c.duel.status == 3){
			c.sendMessage("You can't teleport during a duel!");
			return;
		}
		if(CastleWars.isInCw(c) || CastleWars.isInCwWait(c)){
			c.sendMessage("You cannot teleport from Castle Wars!");
			return;
		}
		if(c.inCaveGame)
			c.getPA().resetTzhaar();
		if(c.inWild() && c.wildLevel > Config.NO_TELEPORT_WILD_LEVEL){
			c.sendMessage("You can't teleport above level " + Config.NO_TELEPORT_WILD_LEVEL + " in the wilderness.");
			return;
		}
		if(System.currentTimeMillis() - c.teleBlockDelay < c.teleBlockLength){
			c.sendMessage("You are teleblocked and can't teleport.");
			return;
		}
		if(!c.isDead && c.teleTimer == 0 && c.respawnTimer == -6){
			if(c.playerIndex > 0 || c.npcIndex > 0)
				c.getCombat().resetPlayerAttack();
			c.stopMovement();
			removeAllWindows();
			c.teleX = x;
			c.teleY = y;
			c.npcIndex = 0;
			c.playerIndex = 0;
			c.faceUpdate(0);
			c.teleHeight = height;
		}
	}

	public boolean checkTele(int x, int y){
		//closeAllWindows();
		if(c.teleTick > 0)
			return false;
		if(c.overLoad > 0)
			return false;
		if(c.inZombiesGame && !c.zombieTemp){
			c.sendMessage("A magical force binds you to this place.");
			return false;
		}
		if(c.inDuel())
			return false;
		if(c.inHowlOfDeath && !c.fixHowlOfDeathTele){
			c.sendMessage("A magical spell leaves you incapable of teleporting.");
			return false;
		}
		if(!c.zombieTemp)
			c.canSummonNomad = false;
		if(c.duel != null && c.duel.status < 3)
			Duel.declineDuel(c, true);
		if(c.inTrade)
			c.getTradeAndDuel().declineTrade(true);
		if(CastleWars.isInCw(c) || CastleWars.isInCwWait(c)){
			c.sendMessage("You cannot teleport from Castle Wars!");
			return false;
		}
		if(c.inClanWars){
			c.sendMessage("You cannot teleport away from Clan Wars!");
			return false;
		}
		if((c.isInArd(x, y) || c.isInFala(x, y) || c.inWild(x, y)) && c.overloadedBool){
			c.getPA().removeAllWindows();
			c.sendMessage("You can not enter a PVP area while overloaded.");
			return false;
		}
		if(!c.inOwnersArray(c.playerName)){
			if(c.duel != null && c.duel.status == 3){
				c.sendMessage("You can't teleport during a duel!");
				return false;
			}
			if(c.isJailed){
				c.sendMessage("You cannot teleport out of jail.");
				closeAllWindows();
				return false;
			}
			if(c.inWild() && c.wildLevel > Config.NO_TELEPORT_WILD_LEVEL){
				c.sendMessage("You can't teleport above level " + Config.NO_TELEPORT_WILD_LEVEL + " in the wilderness.");
				return false;
			}
		}
		if(System.currentTimeMillis() - c.teleBlockDelay < c.teleBlockLength){
			c.sendMessage("You are teleblocked and can't teleport.");
			return false;
		}
		c.getWoodcutting().resetWoodcut();
		c.getMining().resetMining();
		c.getFishing().resetFishing();
		return true;
	}

	public void teleBroom(){
		boolean inWild = c.inWild();
		int x = inWild ? 3209 : 3021;
		int y = inWild ? 3422 : 3631;
		int height = 0;
		if(c.overLoad > 0)
			return;
		if(!checkTele(x, y))
			return;
		c.teleTick = 2;
		if(c.inPits)
			Server.fightPits.removePlayerFromPits(c.playerId);
		if(c.inCaveGame)
			c.getPA().resetTzhaar();
		if(!c.isDead && c.teleTimer == 0 && c.respawnTimer == -6){
			if(c.playerIndex > 0 || c.npcIndex > 0)
				c.getCombat().resetPlayerAttack();
			c.stopMovement();
			c.resetWalkingQueue();
			c.woodcut[0] = c.mining[0] = c.smeltType = 0;
			c.fishing = false;
			removeAllWindows();
			c.teleporting = true;
			c.teleX = x;
			c.teleY = y;
			c.npcIndex = 0;
			c.playerIndex = 0;
			c.faceUpdate(0);
			c.teleHeight = height;
			if(c.absX >= 1990 && c.absX <= 2025 && c.absY >= 4425 && c.absY <= 4450)
				for(int i = 6868; i<=6882; i++)
					if(c.inventory.hasItem(i))
						c.inventory.deleteItem(i, c.inventory.getItemCount(i));
			if(c.inHowlOfDeathLobby)
				HowlOfDeathManager.getInstance().removePlayer(c.playerId);
			c.startAnimation(10538);
			c.teleTimer = 11;
			c.teleGfx = 0;
			c.teleEndAnimation = 10537;
			c.teleEndGFX = 1868;
			c.gfx0(1867);
		}
	}
	
	public void startTeleport(int x, int y, int height, boolean ignoreBook){
		if(c.overLoad > 0)
			return;
		//if(c.inPcBoat() && Server.pestControl.gameTimer < 10)
			//return;
		if(!checkTele(x, y))
			return;
		c.teleTick = 2;
		/*synchronized(Server.pestControl){
			if(c.inPcGame() && !c.pestTemp){
				Server.pestControl.playersInGame.remove((Object)c.playerId);
				c.inPestGame = false;
			}
			if(c.inPcBoat() && !c.pestTemp)
				Server.pestControl.playersInBoat.remove((Object)c.playerId);
		}*/
		if(c.inPits)
			Server.fightPits.removePlayerFromPits(c.playerId);
		if(c.inCaveGame)
			c.getPA().resetTzhaar();
		if(!c.isDead && c.teleTimer == 0 && c.respawnTimer == -6){
			if(c.playerIndex > 0 || c.npcIndex > 0)
				c.getCombat().resetPlayerAttack();
			c.stopMovement();
			c.resetWalkingQueue();
			c.woodcut[0] = c.mining[0] = c.smeltType = 0;
			c.fishing = false;
			removeAllWindows();
			c.teleporting = true;
			c.teleX = x;
			c.teleY = y;
			c.npcIndex = 0;
			c.playerIndex = 0;
			c.faceUpdate(0);
			c.teleHeight = height;
			if(c.absX >= 1990 && c.absX <= 2025 && c.absY >= 4425 && c.absY <= 4450)
				for(int i = 6868; i<=6882; i++)
					if(c.inventory.hasItem(i))
						c.inventory.deleteItem(i, c.inventory.getItemCount(i));
			if(c.inHowlOfDeathLobby)
				HowlOfDeathManager.getInstance().removePlayer(c.playerId);
			if(ignoreBook){
				c.startAnimation(8939);
				c.teleTimer = 11;
				c.teleGfx = 0;
				c.teleEndAnimation = 8941;
				c.teleEndGFX = 1577;
				c.gfx0(1576);
				return;
			}
			switch(c.playerMagicBook){
				case 0: // Modern
					c.startAnimation(8939);
					c.teleTimer = 11;
					c.teleGfx = 0;
					c.teleEndAnimation = 8941;
					c.teleEndGFX = 1577;
					c.gfx0(1576);
					break;
				case 1: // Ancients
					c.startAnimation(9599);
					c.teleTimer = 11;
					c.teleGfx = 0;
					c.teleEndAnimation = 9013;
					c.teleEndGFX = 0;
					c.gfx0(1681);
					break;
				case 2: // Lunar
					c.startAnimation(9606);
					c.teleGfx = 1685;
					c.teleTimer = 12;
					c.teleEndAnimation = 9013;
					c.teleEndGFX = 0;
					break;
				case 3: // Ring
					c.startAnimation(9603);
					c.teleTimer = 12;
					c.teleGfx = 0;
					c.teleEndAnimation = 9013;
					c.teleEndGFX = 0;
					c.gfx0(1684);
					break;
			}
		}
	}

	public void startTeleport2(int x, int y, int height){
		if(c.overLoad > 0)
			return;
		if(c.inPits)
			Server.fightPits.removePlayerFromPits(c.playerId);
		if(c.duel != null && c.duel.status == 3){
			c.sendMessage("You can't teleport during a duel!");
			return;
		}
		if(CastleWars.isInCw(c) || CastleWars.isInCwWait(c)){
			c.sendMessage("You cannot teleport from Castle Wars!");
			return;
		}
		if(c.inCaveGame)
			c.getPA().resetTzhaar();
		if(System.currentTimeMillis() - c.teleBlockDelay < c.teleBlockLength){
			c.sendMessage("You are teleblocked and can't teleport.");
			return;
		}
		if(!c.isDead && c.teleTimer == 0){
			c.stopMovement();
			removeAllWindows();
			HowlOfDeathManager.getInstance().removePlayer(c.playerId);
			c.teleX = x;
			c.teleY = y;
			c.npcIndex = 0;
			c.playerIndex = 0;
			c.faceUpdate(0);
			c.teleHeight = height;
			c.startAnimation(8939);
			c.teleTimer = 11;
			c.teleGfx = 0;
			c.teleEndAnimation = 8941;
			c.teleEndGFX = 1577;
			c.gfx0(1576);
		}
	}

	public void processTeleport(){
		if(c.overLoad > 0)
			return;
		if(c.inPits)
			Server.fightPits.removePlayerFromPits(c.playerId);
		if(c.inCaveGame)
			c.getPA().resetTzhaar();
		if((Misc.currentTimeSeconds() - c.pjAttackTimer <= 10) && (c.isInFala() || c.isInArd() && !c.safeZone())){
			int time = 10 - (Misc.currentTimeSeconds() - c.pjAttackTimer);
			if(time > 0)
				c.sendMessage("You must wait " + time + " second" + (time > 1 ? "s" : "") + " after attacking a player before leaving a pvp zone.");
			return;
		}
		c.stopMovement();
		c.teleportToX = c.teleX;
		c.teleportToY = c.teleY;
		c.heightLevel = c.teleHeight;
		if(c.teleEndAnimation > 0){
			c.startAnimation(c.teleEndAnimation);
		}
		if(c.teleEndGFX > 0){
			c.gfx0(c.teleEndGFX);
		}
		FightPits.removeWait(c.playerId);
		c.inPitsWait = false;
	}

	public void telehades(){
		if(c.playerName.equalsIgnoreCase(Config.OWNER) || c.playerName.equalsIgnoreCase(Config.OWNER_HIDDEN) || c.playerName.equalsIgnoreCase(Config.RHI))
			movePlayer(Config.HADES_AREA_X, Config.HADES_AREA_Y, 0);
	}
	
	public void movePlayer(int x, int y, int h){
		if(c.overLoad > 0){
			c.playerLevel[c.playerHitpoints] -= 10 * c.overLoad;
			c.overLoad = 0;
			c.overloadTick = 0;
			refreshSkill(c.playerHitpoints);
		}
		if(c.duel != null && c.duel.status < 3){
			Duel.declineDuel(c, c.duel.getOtherPlayer(c.playerId) != null);
			return;
		}
		/*synchronized(Server.pestControl){
			if(c.inPcGame() && !c.pestTemp)
				Server.pestControl.playersInGame.remove((Object)c.playerId);
			if(c.inPcBoat() && !c.pestTemp)
				Server.pestControl.playersInBoat.remove((Object)c.playerId);
		}*/
		if(c.inZombiesGame && !c.zombieTemp){
			c.sendMessage("A magical force binds you to this place.");
			return;
		}
		if(c.inPits && !c.addedToPits)
			Server.fightPits.removePlayerFromPits(c.playerId);
		if(c.inCaveGame && !c.tCaveGame)
			c.getPA().resetTzhaar();
		c.tCaveGame = false;
		c.resetWalkingQueue();
		c.teleportToX = x;
		c.teleportToY = y;
		c.heightLevel = h;
		requestUpdates();
		c.inPitsWait = false;
	}

	/**
	 * Following
	 **/

	public void playerWalk(int x, int y){
		PathFinder.getPathFinder().findRoute(c, x, y, true, 1, 1);
	}

	public boolean noFishNPC(int newX, int newY){
		Collection<NPC> npcs = c.getRegion().getNpcs();
		for(NPC npc : npcs)
			if(Spot.isSpot(npc.npcType) && npc.absX == newX && npc.absY == newY)
				return false;
		return true;
	}
	
	public void followPlayer(){
		if(PlayerHandler.players[c.followId] == null || PlayerHandler.players[c.followId].isDead){
			c.followId = 0;
			return;
		}
		if(c.freezeTimer > 0 || c.isDead || c.playerLevel[3] <= 0 || System.currentTimeMillis() - c.lastAgil < 2000 || (c.duel != null && c.duel.rules != null && c.duel.rules.duelRule[1]))
			return;

		int otherX = PlayerHandler.players[c.followId].getX();
		int otherY = PlayerHandler.players[c.followId].getY();
		//boolean withinDistance = c.goodDistance(otherX, otherY, c.getX(), c.getY(), 1);
		boolean hallyDistance = c.goodDistance(otherX, otherY, c.getX(), c.getY(), 2);
		boolean bowDistance = c.goodDistance(otherX, otherY, c.getX(), c.getY(), 8);
		boolean rangeWeaponDistance = c.goodDistance(otherX, otherY, c.getX(), c.getY(), 4);
		boolean sameSpot = c.absX == otherX && c.absY == otherY;
		if(!c.goodDistance(otherX, otherY, c.getX(), c.getY(), 25)){
			c.followId = 0;
			return;
		}
		/*if(c.goodDistance(otherX, otherY, c.getX(), c.getY(), 1)){
			if(otherX != c.getX() && otherY != c.getY()){
				stopDiagonal(otherX, otherY);
				return;
			}
		}*/

		if((c.underAttackBy == c.followId || c.killedBy == c.followId) && c.goodDistance(otherX, otherY, c.getX(), c.getY(), 1) && !sameSpot)
			return;
		
		if((c.usingBow || c.mageFollow || (c.playerIndex > 0 && c.autocastId > 0)) && bowDistance && !sameSpot)
			return;

		if(c.getCombat().usingHally() && hallyDistance && !sameSpot)
			return;

		if(c.usingRangeWeapon && rangeWeaponDistance && !sameSpot)
			return;

		c.faceUpdate(c.followId + 32768);
		if(otherX == c.absX && otherY == c.absY){
			int r = 0;
			boolean canWalk = false;
			for(int i = 0; i<4; i++){
				int newX = i == 0 || i == 1 ? c.absX : i == 2 ? c.absX + 1 : c.absX - 1;
				int newY = i == 2 || i == 3 ? c.absY : i == 0 ? c.absY - 1 : c.absY + 1;
				if(server.clip.region.Region.canMove(c.absX, c.absY, newX, newY, c.heightLevel, 1, 1) && noFishNPC(newX, newY)){
					canWalk = true;
					break;
				}
			}
			if(canWalk){
				do{
					r = Misc.random(3);
					int newX = r == 0 || r == 1 ? c.absX : r == 2 ? c.absX + 1 : c.absX - 1;
					int newY = r == 2 || r == 3 ? c.absY : r == 0 ? c.absY - 1 : c.absY + 1;
					if(server.clip.region.Region.canMove(c.absX, c.absY, newX, newY, c.heightLevel, 1, 1) && noFishNPC(newX, newY)){
						playerWalk(newX, newY);
						break;
					}
				}while(true);
			}
		}else{
			switch(PlayerHandler.players[c.followId].playerDirection){
				case 0:
					playerWalk(otherX, otherY - 1);
					break;
				case 1:
				case 2:
				case 3:
					playerWalk(otherX - 1, otherY - 1);
					break;
				case 4:
					playerWalk(otherX - 1, otherY);
					break;
				case 6:
				case 5:
				case 7:
					playerWalk(otherX - 1, otherY + 1);
					break;
				case 8:
					playerWalk(otherX, otherY + 1);
					break;
				case 9:
				case 10:
				case 11:
					playerWalk(otherX + 1, otherY + 1);
					break;
				case 12:
					playerWalk(otherX + 1, otherY);
					break;
				case 13:
				case 14:
				case 15:
					playerWalk(otherX + 1, otherY - 1);
					break;
			}
		}
		c.faceUpdate(c.followId + 32768);
	}

	public void followNpc(){
		if(NPCHandler.npcs[c.followId2] == null || NPCHandler.npcs[c.followId2].isDead){
			c.followId2 = 0;
			return;
		}
		if(c.freezeTimer > 0){
			return;
		}
		if(c.isDead || c.playerLevel[3] <= 0)
			return;

		int otherX = NPCHandler.npcs[c.followId2].getX();
		int otherY = NPCHandler.npcs[c.followId2].getY();

		boolean withinDistance = c.goodDistance(otherX, otherY, c.getX(), c.getY(), 2);
		// boolean goodDistance = c.goodDistance(otherX, otherY, c.getX(),
		// c.getY(), 1);
		boolean hallyDistance = c.goodDistance(otherX, otherY, c.getX(), c.getY(), 2);
		boolean bowDistance = c.goodDistance(otherX, otherY, c.getX(), c.getY(), 8);
		boolean rangeWeaponDistance = c.goodDistance(otherX, otherY, c.getX(), c.getY(), 4);
		boolean sameSpot = c.absX == otherX && c.absY == otherY;
		if(!c.goodDistance(otherX, otherY, c.getX(), c.getY(), 25)){
			c.followId2 = 0;
			return;
		}
		if(c.goodDistance(otherX, otherY, c.getX(), c.getY(), 1)){
			if(otherX != c.getX() && otherY != c.getY()){
				stopDiagonal(otherX, otherY);
				return;
			}
		}

		if((c.usingBow || c.mageFollow || (c.npcIndex > 0 && c.autocastId > 0)) && bowDistance && !sameSpot){
			return;
		}

		if(c.getCombat().usingHally() && hallyDistance && !sameSpot){
			return;
		}

		if(c.usingRangeWeapon && rangeWeaponDistance && !sameSpot){
			return;
		}

		c.faceUpdate(c.followId2);
		if(otherX == c.absX && otherY == c.absY){
			int r = Misc.random(3);
			switch(r){
				case 0:
					playerWalk(c.absX, c.absY - 1);
					break;
				case 1:
					playerWalk(c.absX, c.absY + 1);
					break;
				case 2:
					playerWalk(c.absX + 1, c.absY);
					break;
				case 3:
					playerWalk(c.absX - 1, c.absY);
					break;
			}
		}else if(c.isRunning2 && !withinDistance){
			if(otherY > c.getY() && otherX == c.getX()){
				playerWalk(otherX, otherY - 1);
			}else if(otherY < c.getY() && otherX == c.getX()){
				playerWalk(otherX, otherY + 1);
			}else if(otherX > c.getX() && otherY == c.getY()){
				playerWalk(otherX - 1, otherY);
			}else if(otherX < c.getX() && otherY == c.getY()){
				playerWalk(otherX + 1, otherY);
			}else if(otherX < c.getX() && otherY < c.getY()){
				playerWalk(otherX + 1, otherY + 1);
			}else if(otherX > c.getX() && otherY > c.getY()){
				playerWalk(otherX - 1, otherY - 1);
			}else if(otherX < c.getX() && otherY > c.getY()){
				playerWalk(otherX + 1, otherY - 1);
			}else if(otherX > c.getX() && otherY < c.getY()){
				playerWalk(otherX + 1, otherY - 1);
			}
		}else{
			if(otherY > c.getY() && otherX == c.getX()){
				playerWalk(otherX, otherY - 1);
			}else if(otherY < c.getY() && otherX == c.getX()){
				playerWalk(otherX, otherY + 1);
			}else if(otherX > c.getX() && otherY == c.getY()){
				playerWalk(otherX - 1, otherY);
			}else if(otherX < c.getX() && otherY == c.getY()){
				playerWalk(otherX + 1, otherY);
			}else if(otherX < c.getX() && otherY < c.getY()){
				playerWalk(otherX + 1, otherY + 1);
			}else if(otherX > c.getX() && otherY > c.getY()){
				playerWalk(otherX - 1, otherY - 1);
			}else if(otherX < c.getX() && otherY > c.getY()){
				playerWalk(otherX + 1, otherY - 1);
			}else if(otherX > c.getX() && otherY < c.getY()){
				playerWalk(otherX - 1, otherY + 1);
			}
		}
		c.faceUpdate(c.followId2);
	}

	/**
	 * This method allows walking without clipping. Primarily used for the
	 * agility skill.
	 * 
	 * @param absX
	 *            + i
	 * @param absY
	 *            + j
	 */
	public void walkTo(int i, int j){
		c.ignoreClip = true;
		c.ignoreClipTick = Math.abs(i) + Math.abs(j);
		c.newWalkCmdSteps = 0;
		if(++c.newWalkCmdSteps > 50)
			c.newWalkCmdSteps = 0;
		int k = c.getX() + i;
		k -= c.mapRegionX * 8;
		c.getNewWalkCmdX()[0] = c.getNewWalkCmdY()[0] = 0;
		int l = c.getY() + j;
		l -= c.mapRegionY * 8;
		for(int n = 0; n < c.newWalkCmdSteps; n++){
			c.getNewWalkCmdX()[n] += k;
			c.getNewWalkCmdY()[n] += l;
		}
	}

	public int getRunningMove(int i, int j){
		if(j - i > 2)
			return 2;
		else if(j - i < -2)
			return -2;
		else
			return j - i;
	}

	public void resetFollow(){
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				c.followId = 0;
				c.followId2 = 0;
				c.mageFollow = false;
				c.outStream.createFrame(174);
				c.outStream.writeWord(0);
				c.outStream.writeByte(0);
				c.outStream.writeWord(1);
			}
		}
	}

	public void stopDiagonal(int otherX, int otherY){
		if(c.freezeDelay > 0)
			return;
		c.newWalkCmdSteps = 1;
		int xMove = otherX - c.getX();
		int yMove = 0;
		if(xMove == 0)
			yMove = otherY - c.getY();
		/*
		 * if(!clipHor){ yMove = 0; }else if(!clipVer){ xMove = 0; }
		 */

		int k = c.getX() + xMove;
		k -= c.mapRegionX * 8;
		c.getNewWalkCmdX()[0] = c.getNewWalkCmdY()[0] = 0;
		int l = c.getY() + yMove;
		l -= c.mapRegionY * 8;

		for(int n = 0; n < c.newWalkCmdSteps; n++){
			c.getNewWalkCmdX()[n] += k;
			c.getNewWalkCmdY()[n] += l;
		}

	}

	public int getMove(int place1, int place2){
		if(System.currentTimeMillis() - c.lastSpear < 4000)
			return 0;
		if((place1 - place2) == 0){
			return 0;
		}else if((place1 - place2) < 0){
			return 1;
		}else if((place1 - place2) > 0){
			return -1;
		}
		return 0;
	}

	public boolean fullVeracs(){
		return c.playerEquipment[c.playerHat] == 4753 && c.playerEquipment[c.playerChest] == 4757 && c.playerEquipment[c.playerLegs] == 4759 && c.playerEquipment[c.playerWeapon] == 4755;
	}

	public boolean fullGuthans(){
		return c.playerEquipment[c.playerHat] == 4724 && c.playerEquipment[c.playerChest] == 4728 && c.playerEquipment[c.playerLegs] == 4730 && c.playerEquipment[c.playerWeapon] == 4726;
	}
	
	public boolean fullDharok(){
		return c.playerEquipment[c.playerHat] == 4716 && c.playerEquipment[c.playerChest] == 4720 && c.playerEquipment[c.playerLegs] == 4722 && c.playerEquipment[c.playerWeapon] == 4718;
	}

	/**
	 * reseting animation
	 **/
	public void resetAnimation(){
		c.getCombat().getPlayerAnimIndex(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
		c.startAnimation(c.playerStandIndex);
		requestUpdates();
	}

	public void requestUpdates(){
		synchronized(c){
			c.updateRequired = true;
			c.setAppearanceUpdateRequired(true);
		}
	}

	public void levelUp(int skill){
		int totalLevel = 0;
		for(int i = 0; i <= Config.NUM_SKILLS; i++)
			totalLevel += getLevelForXP(c.playerXP[i]);
		sendText("Total Lvl: " + totalLevel, 3984);
		int capes[][] = {{0, 9747}, {11, 9804}, {10, 9798}, {12, 9780}, {4, 9756}, {7, 9801}, {9, 9783}, {1, 9753}, {19, 9810}, {17, 9777}, {18, 9786}, {16, 9771}, {15, 9774}, {5, 9759}, {20, 9765}, {13, 9795}, {14, 9792}, {8, 9807}, {2, 9750}, {3, 9768}, {6, 9762}};
		HashMap<Integer, Integer> capeIds = new HashMap<Integer, Integer>();
		for(int c[] : capes)
			capeIds.put(c[0], c[1]);
		switch(skill){
			case 0:
				sendText("Congratulations, you just advanced an attack level!", 6248);
				sendText("Your attack level is now " + getLevelForXP(c.playerXP[skill]) + ".", 6249);
				c.sendMessage("Congratulations, you just advanced an attack level.");
				sendFrame164(6247);
				if(getLevelForXP(c.playerXP[skill]) >= 99 && c.isSecond99()){
					int first99 = c.getFirst99(skill);
					int cape = capeIds.get(first99);
					if(c.playerEquipment[c.playerCape] == cape){
						c.getItems().deleteEquipment(1, c.playerCape);
						c.getItems().setEquipment(cape + 1, 1, c.playerCape);
					}
					for(int i = 0; i < c.inventory.items.length; i++)
						if(c.inventory.items[i] != null && c.inventory.items[i].id - 1 == cape)
							c.inventory.items[i].id = cape + 2;
					c.bank.fixLeveledCapes(cape);
					c.inventory.resetItems(3214);
					c.updateRequired = true;
					c.setAppearanceUpdateRequired(true);
				}
				break;

			case 1:
				sendText("Congratulations, you just advanced a defence level!", 6254);
				sendText("Your defence level is now " + getLevelForXP(c.playerXP[skill]) + ".", 6255);
				c.sendMessage("Congratulations, you just advanced a defence level.");
				sendFrame164(6253);
				if(getLevelForXP(c.playerXP[skill]) >= 99 && c.isSecond99()){
					int first99 = c.getFirst99(skill);
					int cape = capeIds.get(first99);
					if(c.playerEquipment[c.playerCape] == cape){
						c.getItems().deleteEquipment(1, c.playerCape);
						c.getItems().setEquipment(cape + 1, 1, c.playerCape);
					}
					for(int i = 0; i < c.inventory.items.length; i++)
						if(c.inventory.items[i] != null && c.inventory.items[i].id - 1 == cape)
							c.inventory.items[i].id = cape + 2;
					c.bank.fixLeveledCapes(cape);
					c.inventory.resetItems(3214);
					c.updateRequired = true;
					c.setAppearanceUpdateRequired(true);
				}
				break;

			case 2:
				sendText("Congratulations, you just advanced a strength level!", 6207);
				sendText("Your strength level is now " + getLevelForXP(c.playerXP[skill]) + ".", 6208);
				c.sendMessage("Congratulations, you just advanced a strength level.");
				sendFrame164(6206);
				if(getLevelForXP(c.playerXP[skill]) >= 99 && c.isSecond99()){
					int first99 = c.getFirst99(skill);
					int cape = capeIds.get(first99);
					if(c.playerEquipment[c.playerCape] == cape){
						c.getItems().deleteEquipment(1, c.playerCape);
						c.getItems().setEquipment(cape + 1, 1, c.playerCape);
					}
					for(int i = 0; i < c.inventory.items.length; i++)
						if(c.inventory.items[i] != null && c.inventory.items[i].id - 1 == cape)
							c.inventory.items[i].id = cape + 2;
					c.bank.fixLeveledCapes(cape);
					c.inventory.resetItems(3214);
					c.updateRequired = true;
					c.setAppearanceUpdateRequired(true);
				}
				break;

			case 3:
				sendText("Congratulations, you just advanced a hitpoints level!", 6217);
				sendText("Your hitpoints level is now " + getLevelForXP(c.playerXP[skill]) + ".", 6218);
				c.sendMessage("Congratulations, you just advanced a hitpoints level.");
				sendFrame164(6216);
				if(getLevelForXP(c.playerXP[skill]) >= 99 && c.isSecond99()){
					int first99 = c.getFirst99(skill);
					int cape = capeIds.get(first99);
					if(c.playerEquipment[c.playerCape] == cape){
						c.getItems().deleteEquipment(1, c.playerCape);
						c.getItems().setEquipment(cape + 1, 1, c.playerCape);
					}
					for(int i = 0; i < c.inventory.items.length; i++)
						if(c.inventory.items[i] != null && c.inventory.items[i].id - 1 == cape)
							c.inventory.items[i].id = cape + 2;
					c.bank.fixLeveledCapes(cape);
					c.inventory.resetItems(3214);
					c.updateRequired = true;
					c.setAppearanceUpdateRequired(true);
				}
				break;

			case 4:
				sendText("Congratulations, you just advanced a ranged level!", 5453);
				sendText("Your ranged level is now " + getLevelForXP(c.playerXP[skill]) + ".", 6114);
				c.sendMessage("Congratulations, you just advanced a ranging level.");
				sendFrame164(4443);
				if(getLevelForXP(c.playerXP[skill]) >= 99 && c.isSecond99()){
					int first99 = c.getFirst99(skill);
					int cape = capeIds.get(first99);
					if(c.playerEquipment[c.playerCape] == cape){
						c.getItems().deleteEquipment(1, c.playerCape);
						c.getItems().setEquipment(cape + 1, 1, c.playerCape);
					}
					for(int i = 0; i < c.inventory.items.length; i++)
						if(c.inventory.items[i] != null && c.inventory.items[i].id - 1 == cape)
							c.inventory.items[i].id = cape + 2;
					c.bank.fixLeveledCapes(cape);
					c.inventory.resetItems(3214);
					c.updateRequired = true;
					c.setAppearanceUpdateRequired(true);
				}
				break;

			case 5:
				sendText("Congratulations, you just advanced a prayer level!", 6243);
				sendText("Your prayer level is now " + getLevelForXP(c.playerXP[skill]) + ".", 6244);
				c.sendMessage("Congratulations, you just advanced a prayer level.");
				sendFrame164(6242);
				if(getLevelForXP(c.playerXP[skill]) >= 99 && c.isSecond99()){
					int first99 = c.getFirst99(skill);
					int cape = capeIds.get(first99);
					if(c.playerEquipment[c.playerCape] == cape){
						c.getItems().deleteEquipment(1, c.playerCape);
						c.getItems().setEquipment(cape + 1, 1, c.playerCape);
					}
					for(int i = 0; i < c.inventory.items.length; i++)
						if(c.inventory.items[i] != null && c.inventory.items[i].id - 1 == cape)
							c.inventory.items[i].id = cape + 2;
					c.bank.fixLeveledCapes(cape);
					c.inventory.resetItems(3214);
					c.updateRequired = true;
					c.setAppearanceUpdateRequired(true);
				}
				break;

			case 6:
				sendText("Congratulations, you just advanced a magic level!", 6212);
				sendText("Your magic level is now " + getLevelForXP(c.playerXP[skill]) + ".", 6213);
				c.sendMessage("Congratulations, you just advanced a magic level.");
				sendFrame164(6211);
				if(getLevelForXP(c.playerXP[skill]) >= 99 && c.isSecond99()){
					int first99 = c.getFirst99(skill);
					int cape = capeIds.get(first99);
					if(c.playerEquipment[c.playerCape] == cape){
						c.getItems().deleteEquipment(1, c.playerCape);
						c.getItems().setEquipment(cape + 1, 1, c.playerCape);
					}
					for(int i = 0; i < c.inventory.items.length; i++)
						if(c.inventory.items[i] != null && c.inventory.items[i].id - 1 == cape)
							c.inventory.items[i].id = cape + 2;
					c.bank.fixLeveledCapes(cape);
					c.inventory.resetItems(3214);
					c.updateRequired = true;
					c.setAppearanceUpdateRequired(true);
				}
				break;

			case 7:
				sendText("Congratulations, you just advanced a cooking level!", 6227);
				sendText("Your cooking level is now " + getLevelForXP(c.playerXP[skill]) + ".", 6228);
				c.sendMessage("Congratulations, you just advanced a cooking level.");
				sendFrame164(6226);
				if(getLevelForXP(c.playerXP[skill]) >= 99 && c.isSecond99()){
					int first99 = c.getFirst99(skill);
					int cape = capeIds.get(first99);
					if(c.playerEquipment[c.playerCape] == cape){
						c.getItems().deleteEquipment(1, c.playerCape);
						c.getItems().setEquipment(cape + 1, 1, c.playerCape);
					}
					for(int i = 0; i < c.inventory.items.length; i++)
						if(c.inventory.items[i] != null && c.inventory.items[i].id - 1 == cape)
							c.inventory.items[i].id = cape + 2;
					c.bank.fixLeveledCapes(cape);
					c.inventory.resetItems(3214);
					c.updateRequired = true;
					c.setAppearanceUpdateRequired(true);
				}
				break;

			case 8:
				sendText("Congratulations, you just advanced a woodcutting level!", 4273);
				sendText("Your woodcutting level is now " + getLevelForXP(c.playerXP[skill]) + ".", 4274);
				c.sendMessage("Congratulations, you just advanced a woodcutting level.");
				sendFrame164(4272);
				if(getLevelForXP(c.playerXP[skill]) >= 99 && c.isSecond99()){
					int first99 = c.getFirst99(skill);
					int cape = capeIds.get(first99);
					if(c.playerEquipment[c.playerCape] == cape){
						c.getItems().deleteEquipment(1, c.playerCape);
						c.getItems().setEquipment(cape + 1, 1, c.playerCape);
					}
					for(int i = 0; i < c.inventory.items.length; i++)
						if(c.inventory.items[i] != null && c.inventory.items[i].id - 1 == cape)
							c.inventory.items[i].id = cape + 2;
					c.bank.fixLeveledCapes(cape);
					c.inventory.resetItems(3214);
					c.updateRequired = true;
					c.setAppearanceUpdateRequired(true);
				}
				break;

			case 9:
				sendText("Congratulations, you just advanced a fletching level!", 6232);
				sendText("Your fletching level is now " + getLevelForXP(c.playerXP[skill]) + ".", 6233);
				c.sendMessage("Congratulations, you just advanced a fletching level.");
				sendFrame164(6231);
				if(getLevelForXP(c.playerXP[skill]) >= 99 && c.isSecond99()){
					int first99 = c.getFirst99(skill);
					int cape = capeIds.get(first99);
					if(c.playerEquipment[c.playerCape] == cape){
						c.getItems().deleteEquipment(1, c.playerCape);
						c.getItems().setEquipment(cape + 1, 1, c.playerCape);
					}
					for(int i = 0; i < c.inventory.items.length; i++)
						if(c.inventory.items[i] != null && c.inventory.items[i].id - 1 == cape)
							c.inventory.items[i].id = cape + 2;
					c.bank.fixLeveledCapes(cape);
					c.inventory.resetItems(3214);
					c.updateRequired = true;
					c.setAppearanceUpdateRequired(true);
				}
				break;

			case 10:
				sendText("Congratulations, you just advanced a fishing level!", 6259);
				sendText("Your fishing level is now " + getLevelForXP(c.playerXP[skill]) + ".", 6260);
				c.sendMessage("Congratulations, you just advanced a fishing level.");
				sendFrame164(6258);
				if(getLevelForXP(c.playerXP[skill]) >= 99 && c.isSecond99()){
					int first99 = c.getFirst99(skill);
					int cape = capeIds.get(first99);
					if(c.playerEquipment[c.playerCape] == cape){
						c.getItems().deleteEquipment(1, c.playerCape);
						c.getItems().setEquipment(cape + 1, 1, c.playerCape);
					}
					for(int i = 0; i < c.inventory.items.length; i++)
						if(c.inventory.items[i] != null && c.inventory.items[i].id - 1 == cape)
							c.inventory.items[i].id = cape + 2;
					c.bank.fixLeveledCapes(cape);
					c.inventory.resetItems(3214);
					c.updateRequired = true;
					c.setAppearanceUpdateRequired(true);
				}
				break;

			case 11:
				sendText("Congratulations, you just advanced a fire making level!", 4283);
				sendText("Your firemaking level is now " + getLevelForXP(c.playerXP[skill]) + ".", 4284);
				c.sendMessage("Congratulations, you just advanced a fire making level.");
				sendFrame164(4282);
				if(getLevelForXP(c.playerXP[skill]) >= 99 && c.isSecond99()){
					int first99 = c.getFirst99(skill);
					int cape = capeIds.get(first99);
					if(c.playerEquipment[c.playerCape] == cape){
						c.getItems().deleteEquipment(1, c.playerCape);
						c.getItems().setEquipment(cape + 1, 1, c.playerCape);
					}
					for(int i = 0; i < c.inventory.items.length; i++)
						if(c.inventory.items[i] != null && c.inventory.items[i].id - 1 == cape)
							c.inventory.items[i].id = cape + 2;
					c.bank.fixLeveledCapes(cape);
					c.inventory.resetItems(3214);
					c.updateRequired = true;
					c.setAppearanceUpdateRequired(true);
				}
				break;

			case 12:
				sendText("Congratulations, you just advanced a crafting level!", 6264);
				sendText("Your crafting level is now " + getLevelForXP(c.playerXP[skill]) + ".", 6265);
				c.sendMessage("Congratulations, you just advanced a crafting level.");
				sendFrame164(6263);
				if(getLevelForXP(c.playerXP[skill]) >= 99 && c.isSecond99()){
					int first99 = c.getFirst99(skill);
					int cape = capeIds.get(first99);
					if(c.playerEquipment[c.playerCape] == cape){
						c.getItems().deleteEquipment(1, c.playerCape);
						c.getItems().setEquipment(cape + 1, 1, c.playerCape);
					}
					for(int i = 0; i < c.inventory.items.length; i++)
						if(c.inventory.items[i] != null && c.inventory.items[i].id - 1 == cape)
							c.inventory.items[i].id = cape + 2;
					c.bank.fixLeveledCapes(cape);
					c.inventory.resetItems(3214);
					c.updateRequired = true;
					c.setAppearanceUpdateRequired(true);
				}
				break;

			case 13:
				sendText("Congratulations, you just advanced a smithing level!", 6222);
				sendText("Your smithing level is now " + getLevelForXP(c.playerXP[skill]) + ".", 6223);
				c.sendMessage("Congratulations, you just advanced a smithing level.");
				sendFrame164(6221);
				if(getLevelForXP(c.playerXP[skill]) >= 99 && c.isSecond99()){
					int first99 = c.getFirst99(skill);
					int cape = capeIds.get(first99);
					if(c.playerEquipment[c.playerCape] == cape){
						c.getItems().deleteEquipment(1, c.playerCape);
						c.getItems().setEquipment(cape + 1, 1, c.playerCape);
					}
					for(int i = 0; i < c.inventory.items.length; i++)
						if(c.inventory.items[i] != null && c.inventory.items[i].id - 1 == cape)
							c.inventory.items[i].id = cape + 2;
					c.bank.fixLeveledCapes(cape);
					c.inventory.resetItems(3214);
					c.updateRequired = true;
					c.setAppearanceUpdateRequired(true);
				}
				break;

			case 14:
				sendText("Congratulations, you just advanced a mining level!", 4417);
				sendText("Your mining level is now " + getLevelForXP(c.playerXP[skill]) + ".", 4438);
				c.sendMessage("Congratulations, you just advanced a mining level.");
				sendFrame164(4416);
				if(getLevelForXP(c.playerXP[skill]) >= 99 && c.isSecond99()){
					int first99 = c.getFirst99(skill);
					int cape = capeIds.get(first99);
					if(c.playerEquipment[c.playerCape] == cape){
						c.getItems().deleteEquipment(1, c.playerCape);
						c.getItems().setEquipment(cape + 1, 1, c.playerCape);
					}
					for(int i = 0; i < c.inventory.items.length; i++)
						if(c.inventory.items[i] != null && c.inventory.items[i].id - 1 == cape)
							c.inventory.items[i].id = cape + 2;
					c.bank.fixLeveledCapes(cape);
					c.inventory.resetItems(3214);
					c.updateRequired = true;
					c.setAppearanceUpdateRequired(true);
				}
				break;

			case 15:
				sendText("Congratulations, you just advanced a herblore level!", 6238);
				sendText("Your herblore level is now " + getLevelForXP(c.playerXP[skill]) + ".", 6239);
				c.sendMessage("Congratulations, you just advanced a herblore level.");
				sendFrame164(6237);
				if(getLevelForXP(c.playerXP[skill]) >= 99 && c.isSecond99()){
					int first99 = c.getFirst99(skill);
					int cape = capeIds.get(first99);
					if(c.playerEquipment[c.playerCape] == cape){
						c.getItems().deleteEquipment(1, c.playerCape);
						c.getItems().setEquipment(cape + 1, 1, c.playerCape);
					}
					for(int i = 0; i < c.inventory.items.length; i++)
						if(c.inventory.items[i] != null && c.inventory.items[i].id - 1 == cape)
							c.inventory.items[i].id = cape + 2;
					c.bank.fixLeveledCapes(cape);
					c.inventory.resetItems(3214);
					c.updateRequired = true;
					c.setAppearanceUpdateRequired(true);
				}
				break;

			case 16:
				sendText("Congratulations, you just advanced a agility level!", 4278);
				sendText("Your agility level is now " + getLevelForXP(c.playerXP[skill]) + ".", 4279);
				c.sendMessage("Congratulations, you just advanced an agility level.");
				sendFrame164(4277);
				if(getLevelForXP(c.playerXP[skill]) >= 99 && c.isSecond99()){
					int first99 = c.getFirst99(skill);
					int cape = capeIds.get(first99);
					if(c.playerEquipment[c.playerCape] == cape){
						c.getItems().deleteEquipment(1, c.playerCape);
						c.getItems().setEquipment(cape + 1, 1, c.playerCape);
					}
					for(int i = 0; i < c.inventory.items.length; i++)
						if(c.inventory.items[i] != null && c.inventory.items[i].id - 1 == cape)
							c.inventory.items[i].id = cape + 2;
					c.bank.fixLeveledCapes(cape);
					c.inventory.resetItems(3214);
					c.updateRequired = true;
					c.setAppearanceUpdateRequired(true);
				}
				break;

			case 17:
				sendText("Congratulations, you just advanced a thieving level!", 4263);
				sendText("Your theiving level is now " + getLevelForXP(c.playerXP[skill]) + ".", 4264);
				c.sendMessage("Congratulations, you just advanced a thieving level.");
				sendFrame164(4261);
				if(getLevelForXP(c.playerXP[skill]) >= 99 && c.isSecond99()){
					int first99 = c.getFirst99(skill);
					int cape = capeIds.get(first99);
					if(c.playerEquipment[c.playerCape] == cape){
						c.getItems().deleteEquipment(1, c.playerCape);
						c.getItems().setEquipment(cape + 1, 1, c.playerCape);
					}
					for(int i = 0; i < c.inventory.items.length; i++)
						if(c.inventory.items[i] != null && c.inventory.items[i].id - 1 == cape)
							c.inventory.items[i].id = cape + 2;
					c.bank.fixLeveledCapes(cape);
					c.inventory.resetItems(3214);
					c.updateRequired = true;
					c.setAppearanceUpdateRequired(true);
				}
				break;

			case 18:
				sendText("Congratulations, you just advanced a slayer level!", 12123);
				sendText("Your slayer level is now " + getLevelForXP(c.playerXP[skill]) + ".", 12124);
				c.sendMessage("Congratulations, you just advanced a slayer level.");
				sendFrame164(12122);
				if(getLevelForXP(c.playerXP[skill]) >= 99 && c.isSecond99()){
					int first99 = c.getFirst99(skill);
					int cape = capeIds.get(first99);
					if(c.playerEquipment[c.playerCape] == cape){
						c.getItems().deleteEquipment(1, c.playerCape);
						c.getItems().setEquipment(cape + 1, 1, c.playerCape);
					}
					for(int i = 0; i < c.inventory.items.length; i++)
						if(c.inventory.items[i] != null && c.inventory.items[i].id - 1 == cape)
							c.inventory.items[i].id = cape + 2;
					c.bank.fixLeveledCapes(cape);
					c.inventory.resetItems(3214);
					c.updateRequired = true;
					c.setAppearanceUpdateRequired(true);
				}
				break;

			case 20:
				sendText("Congratulations, you just advanced a runecrafting level!", 4268);
				sendText("Your runecrafting level is now " + getLevelForXP(c.playerXP[skill]) + ".", 4269);
				c.sendMessage("Congratulations, you just advanced a runecrafting level.");
				sendFrame164(4267);
				if(getLevelForXP(c.playerXP[skill]) >= 99 && c.isSecond99()){
					int first99 = c.getFirst99(skill);
					int cape = capeIds.get(first99);
					if(c.playerEquipment[c.playerCape] == cape){
						c.getItems().deleteEquipment(1, c.playerCape);
						c.getItems().setEquipment(cape + 1, 1, c.playerCape);
					}
					for(int i = 0; i < c.inventory.items.length; i++)
						if(c.inventory.items[i] != null && c.inventory.items[i].id - 1 == cape)
							c.inventory.items[i].id = cape + 2;
					c.bank.fixLeveledCapes(cape);
					c.inventory.resetItems(3214);
					c.updateRequired = true;
					c.setAppearanceUpdateRequired(true);
				}
				break;
		}
		c.dialogueAction = 0;
		c.nextChat = 0;
	}

	public void refreshSkill(int i){
		switch(i){
			case 0:
				sendText("" + c.playerLevel[0] + "", 4004);
				sendText("" + getLevelForXP(c.playerXP[0]) + "", 4005);
				sendText("" + c.playerXP[0] + "", 4044);
				sendText("" + getXPForLevel(getLevelForXP(c.playerXP[0]) + 1) + "", 4045);
				break;

			case 1:
				sendText("" + c.playerLevel[1] + "", 4008);
				sendText("" + getLevelForXP(c.playerXP[1]) + "", 4009);
				sendText("" + c.playerXP[1] + "", 4056);
				sendText("" + getXPForLevel(getLevelForXP(c.playerXP[1]) + 1) + "", 4057);
				break;

			case 2:
				sendText("" + c.playerLevel[2] + "", 4006);
				sendText("" + getLevelForXP(c.playerXP[2]) + "", 4007);
				sendText("" + c.playerXP[2] + "", 4050);
				sendText("" + getXPForLevel(getLevelForXP(c.playerXP[2]) + 1) + "", 4051);
				break;

			case 3:
				sendText("" + c.playerLevel[3] + "", 4016);
				sendText("" + getLevelForXP(c.playerXP[3]) + "", 4017);
				sendText("" + c.playerXP[3] + "", 4080);
				sendText("" + getXPForLevel(getLevelForXP(c.playerXP[3]) + 1) + "", 4081);
				break;

			case 4:
				sendText("" + c.playerLevel[4] + "", 4010);
				sendText("" + getLevelForXP(c.playerXP[4]) + "", 4011);
				sendText("" + c.playerXP[4] + "", 4062);
				sendText("" + getXPForLevel(getLevelForXP(c.playerXP[4]) + 1) + "", 4063);
				break;

			case 5:
				sendText("" + c.playerLevel[5] + "", 4012);
				sendText("" + getLevelForXP(c.playerXP[5]) + "", 4013);
				sendText("" + c.playerXP[5] + "", 4068);
				sendText("" + getXPForLevel(getLevelForXP(c.playerXP[5]) + 1) + "", 4069);
				sendText("" + c.playerLevel[5] + "/" + getLevelForXP(c.playerXP[5]) + "", c.cursesActive ? 22501 : 687);// Prayer
																														// frame
				break;

			case 6:
				sendText("" + c.playerLevel[6] + "", 4014);
				sendText("" + getLevelForXP(c.playerXP[6]) + "", 4015);
				sendText("" + c.playerXP[6] + "", 4074);
				sendText("" + getXPForLevel(getLevelForXP(c.playerXP[6]) + 1) + "", 4075);
				break;

			case 7:
				sendText("" + c.playerLevel[7] + "", 4034);
				sendText("" + getLevelForXP(c.playerXP[7]) + "", 4035);
				sendText("" + c.playerXP[7] + "", 4134);
				sendText("" + getXPForLevel(getLevelForXP(c.playerXP[7]) + 1) + "", 4135);
				break;

			case 8:
				sendText("" + c.playerLevel[8] + "", 4038);
				sendText("" + getLevelForXP(c.playerXP[8]) + "", 4039);
				sendText("" + c.playerXP[8] + "", 4146);
				sendText("" + getXPForLevel(getLevelForXP(c.playerXP[8]) + 1) + "", 4147);
				break;

			case 9:
				sendText("" + c.playerLevel[9] + "", 4026);
				sendText("" + getLevelForXP(c.playerXP[9]) + "", 4027);
				sendText("" + c.playerXP[9] + "", 4110);
				sendText("" + getXPForLevel(getLevelForXP(c.playerXP[9]) + 1) + "", 4111);
				break;

			case 10:
				sendText("" + c.playerLevel[10] + "", 4032);
				sendText("" + getLevelForXP(c.playerXP[10]) + "", 4033);
				sendText("" + c.playerXP[10] + "", 4128);
				sendText("" + getXPForLevel(getLevelForXP(c.playerXP[10]) + 1) + "", 4129);
				break;

			case 11:
				sendText("" + c.playerLevel[11] + "", 4036);
				sendText("" + getLevelForXP(c.playerXP[11]) + "", 4037);
				sendText("" + c.playerXP[11] + "", 4140);
				sendText("" + getXPForLevel(getLevelForXP(c.playerXP[11]) + 1) + "", 4141);
				break;

			case 12:
				sendText("" + c.playerLevel[12] + "", 4024);
				sendText("" + getLevelForXP(c.playerXP[12]) + "", 4025);
				sendText("" + c.playerXP[12] + "", 4104);
				sendText("" + getXPForLevel(getLevelForXP(c.playerXP[12]) + 1) + "", 4105);
				break;

			case 13:
				sendText("" + c.playerLevel[13] + "", 4030);
				sendText("" + getLevelForXP(c.playerXP[13]) + "", 4031);
				sendText("" + c.playerXP[13] + "", 4122);
				sendText("" + getXPForLevel(getLevelForXP(c.playerXP[13]) + 1) + "", 4123);
				break;

			case 14:
				sendText("" + c.playerLevel[14] + "", 4028);
				sendText("" + getLevelForXP(c.playerXP[14]) + "", 4029);
				sendText("" + c.playerXP[14] + "", 4116);
				sendText("" + getXPForLevel(getLevelForXP(c.playerXP[14]) + 1) + "", 4117);
				break;

			case 15:
				sendText("" + c.playerLevel[15] + "", 4020);
				sendText("" + getLevelForXP(c.playerXP[15]) + "", 4021);
				sendText("" + c.playerXP[15] + "", 4092);
				sendText("" + getXPForLevel(getLevelForXP(c.playerXP[15]) + 1) + "", 4093);
				break;

			case 16:
				sendText("" + c.playerLevel[16] + "", 4018);
				sendText("" + getLevelForXP(c.playerXP[16]) + "", 4019);
				sendText("" + c.playerXP[16] + "", 4086);
				sendText("" + getXPForLevel(getLevelForXP(c.playerXP[16]) + 1) + "", 4087);
				break;

			case 17:
				sendText("" + c.playerLevel[17] + "", 4022);
				sendText("" + getLevelForXP(c.playerXP[17]) + "", 4023);
				sendText("" + c.playerXP[17] + "", 4098);
				sendText("" + getXPForLevel(getLevelForXP(c.playerXP[17]) + 1) + "", 4099);
				break;

			case 18:
				sendText("" + c.playerLevel[18] + "", 12166);
				sendText("" + getLevelForXP(c.playerXP[18]) + "", 12167);
				sendText("" + c.playerXP[18] + "", 12171);
				sendText("" + getXPForLevel(getLevelForXP(c.playerXP[18]) + 1) + "", 12172);
				break;

			case 19:
				sendText("" + c.playerLevel[19] + "", 13926);
				sendText("" + getLevelForXP(c.playerXP[19]) + "", 13927);
				sendText("" + c.playerXP[19] + "", 13921);
				sendText("" + getXPForLevel(getLevelForXP(c.playerXP[19]) + 1) + "", 13922);
				break;

			case 20:
				sendText("" + c.playerLevel[20] + "", 4152);
				sendText("" + getLevelForXP(c.playerXP[20]) + "", 4153);
				sendText("" + c.playerXP[20] + "", 4157);
				sendText("" + getXPForLevel(getLevelForXP(c.playerXP[20]) + 1) + "", 4158);
				break;
		}
	}

	public int getXPForLevel(int level){
		int points = 0;
		int output = 0;

		for(int lvl = 1; lvl <= level; lvl++){
			points += Math.floor((double)lvl + 300.0 * Math.pow(2.0, (double)lvl / 7.0));
			if(lvl >= level)
				return output;
			output = (int)Math.floor(points / 4);
		}
		return 0;
	}

	public int getLevelForXP(int exp){
		int points = 0;
		int output = 0;
		if(exp > 13034430)
			return 99;
		for(int lvl = 1; lvl <= 99; lvl++){
			points += Math.floor((double)lvl + 300.0 * Math.pow(2.0, (double)lvl / 7.0));
			output = (int)Math.floor(points / 4);
			if(output >= exp){
				return lvl;
			}
		}
		return 0;
	}

	public boolean addSkillXP(int amount, int skill){
		if(c.xpLock)
			return false;
		if(amount + c.playerXP[skill] < 0 || c.playerXP[skill] > 200000000){
			if(c.playerXP[skill] > 200000000)
				c.playerXP[skill] = 200000000;
			return false;
		}
		amount *= Config.SERVER_EXP_BONUS;
		if(c.doubleExpTime > Misc.currentTimeSeconds())
			amount *= 2;
		else if(c.doubleExpTime > 0)
			c.doubleExpTime = 0;
		int oldLevel = getLevelForXP(c.playerXP[skill]);
		c.playerXP[skill] += amount;
		if(oldLevel < getLevelForXP(c.playerXP[skill])){
			if(c.playerLevel[skill] < c.getLevelForXP(c.playerXP[skill]) && skill != 3 && skill != 5)
				c.playerLevel[skill] = c.getLevelForXP(c.playerXP[skill]);
			levelUp(skill);
			c.gfx100(199);
			requestUpdates();
		}
		setSkillLevel(skill, c.playerLevel[skill], c.playerXP[skill]);
		refreshSkill(skill);
		return true;
	}

	public void resetBarrows(){
		c.barrowsNpcs[0][1] = 0;
		c.barrowsNpcs[1][1] = 0;
		c.barrowsNpcs[2][1] = 0;
		c.barrowsNpcs[3][1] = 0;
		c.barrowsNpcs[4][1] = 0;
		c.barrowsNpcs[5][1] = 0;
		c.barrowsKillCount = 0;
		c.randomCoffin = Misc.random(3) + 1;
	}

	public static int Barrows[] = {4708, 4710, 4712, 4714, 4716, 4718, 4720, 4722, 4724, 4726, 4728, 4730, 4732, 4734, 4736, 4738, 4745, 4747, 4749, 4751, 4753, 4755, 4757, 4759};
	public static int Runes[] = {4740, 558, 560, 565};
	public static int Pots[] = {};

	public int randomBarrows(){
		return Barrows[(int)(Math.random() * Barrows.length)];
	}

	public int randomRunes(){
		return Runes[(int)(Math.random() * Runes.length)];
	}

	public int randomPots(){
		return Pots[(int)(Math.random() * Pots.length)];
	}

	/**
	 * Show an arrow icon on the selected player.
	 * 
	 * @Param i - Either 0 or 1; 1 is arrow, 0 is none.
	 * @Param j - The player/Npc that the arrow will be displayed above.
	 * @Param k - Keep this set as 0
	 * @Param l - Keep this set as 0
	 */
	public void drawHeadicon(int i, int j, int k, int l){
		synchronized(c){
			c.outStream.createFrame(254);
			c.outStream.writeByte(i);

			if(i == 1 || i == 10){
				c.outStream.writeWord(j);
				c.outStream.writeWord(k);
				c.outStream.writeByte(l);
			}else{
				c.outStream.writeWord(k);
				c.outStream.writeWord(l);
				c.outStream.writeByte(j);
			}
		}
	}

	/**
	 * 
	 * @param id
	 *            The id of the NPC you want to find
	 * @return If the NPC exists, then the return will be its ID. Otherwise the
	 *         return is -1.
	 */
	public int getNpcId(int id){
		return NPCHandler.npcs[id] == null ? -1 : id;
	}

	public void removeObject(int x, int y){
		object(-1, x, x, 10, 10);
	}

	private void objectToRemove(int X, int Y){
		object(-1, X, Y, 10, 10);
	}

	private void objectToRemove2(int X, int Y){
		object(-1, X, Y, -1, 0);
	}

	public void removeObjects(){
		objectToRemove(2638, 4688);
		objectToRemove2(2635, 4693);
		objectToRemove2(2634, 4693);
	}

	public void handleGlory(int gloryId, boolean equipped){
		if(c.glory_delay > -1 && System.currentTimeMillis() < c.glory_delay)
			return;
		c.dialogueAction = 0;
		c.dialogueId = 0;
		boolean trim = gloryId > 10000, teleport = false;
		if(equipped && c.playerEquipment[c.playerAmulet] == gloryId){
			c.getItems().deleteEquipment(gloryId, c.playerAmulet);
			gloryId += trim ? 2 : -2;
			c.inventory.addItem(gloryId, 1, -1);
			c.getItems().wearItem(gloryId, c.inventory.findItemSlot(gloryId));
			teleport = true;
		}else if(c.inventory.hasItem(gloryId)){
			c.inventory.deleteItem(gloryId, 1);
			gloryId += trim ? 2 : -2;
			c.inventory.addItem(gloryId, 1, -1);
			teleport = true;
		}
		if(teleport){
			c.glory_delay = System.currentTimeMillis() + 1500;
			c.getDH().sendOption4("Edgeville", "Al Kharid", "Karamja", "Mage Bank");
			c.usingGlory = true;
		}
	}
	
	public void handleVecnaSkull(){
		if(!c.inventory.hasItem(20667) || c.vecnaSkullTimer > Misc.currentTimeSeconds())
			return;
		c.gfx100(738);
		c.startAnimation(10530);
		c.vecnaSkullTimer = Misc.currentTimeSeconds() + 420;
		c.playerLevel[c.playerMagic] += 6;
		if(c.playerLevel[c.playerMagic] > 119)
			c.playerLevel[c.playerMagic] = 119;
		refreshSkill(c.playerMagic);
		c.sendMessage("The skull feeds off the life around you, boosting your magical ability.");
	}
	
	public void handleRODueling(int ringId, boolean equipped){
		if(c.rod_delay > -1 && System.currentTimeMillis() < c.rod_delay)
			return;
		//if(c.inPcBoat() && Server.pestControl.gameTimer < 10)
			//return;
		boolean teleport = false;
		if(equipped && c.playerEquipment[c.playerRing] == ringId){
			if(c.teleX != -1 && c.teleY != -1){
				c.getItems().deleteEquipment(ringId, c.playerRing);
				if(ringId != 2566){
					c.inventory.addItem(ringId + 2, 1, -1);
					c.getItems().wearItem(ringId + 2, c.inventory.findItemSlot(ringId + 2));
				}
				teleport = true;
			}
		}else if(c.inventory.hasItem(ringId)){
			if(c.teleX != -1 && c.teleY != -1){
				c.inventory.deleteItem(ringId, 1);
				if(ringId != 2566)
					c.inventory.addItem(ringId + 2, 1, -1);
				teleport = true;
			}
		}
		if(teleport){
			c.rod_delay = System.currentTimeMillis() + 1500;
			int temp = c.playerMagicBook;
			c.playerMagicBook = 3;
			c.getPA().startTeleport(2439 + Misc.random(4), 3085 + Misc.random(5), 0, false);
			c.playerMagicBook = temp;
		}
	}

	public void resetVariables(){
		c.getFishing().resetFishing();
		c.getCrafting().resetCrafting();
		c.getWoodcutting().resetWoodcut();
		c.usingGlory = false;
		c.smeltInterface = false;
		c.smeltType = 0;
		c.smeltAmount = 0;
		c.woodcut[0] = c.woodcut[1] = c.woodcut[2] = c.woodcut[3] = 0;
		c.mining[0] = c.mining[1] = c.mining[2] = 0;
	}

	public boolean inPitsWait(){
		return c.getX() <= 2404 && c.getX() >= 2394 && c.getY() <= 5175 && c.getY() >= 5169;
	}

	public void castleWarsObjects(){
		object(-1, 2373, 3119, -3, 10);
		object(-1, 2372, 3119, -3, 10);
	}

	public void resetAntiFire(){
		c.antiFirePot = 0;
		c.lastAntiFire = -1;
		c.antiFireDelay = 0;
	}
	
	public int antiFire(){
		int protect = ((c.playerEquipment[c.playerShield] == 1540 || c.playerEquipment[c.playerShield] == 11284 || c.playerEquipment[c.playerShield] == 11283) ? 1 : 0);
		protect += c.antiFirePot;
		return protect;
	}

	public boolean checkForFlags(){
		int[][] itemsToCheck = {{995, 100000000}, {35, 5}, {667, 5}, {2402, 5}, {746, 5}, {4151, 150}, {565, 100000}, {560, 100000}, {555, 300000}, {11235, 10}};
		for(int j = 0; j < itemsToCheck.length; j++){
			if(itemsToCheck[j][1] < c.getItems().getTotalCount(itemsToCheck[j][0]))
				return true;
		}
		return false;
	}

	public void addStarter(){
		int starters = Connection.getStarterPacks(c.macAddress);
		if(starters < Config.MAX_STARTERS){
			Connection.addIpToStarterList(c.macAddress);
			c.inventory.addItem(995, 25000000, -1);
			c.inventory.addItem(1712, 1, -1);
			c.inventory.addItem(554, 200, -1);
			c.inventory.addItem(557, 200, -1);
			c.inventory.addItem(555, 10000, -1);
			c.inventory.addItem(556, 200, -1);
			c.inventory.addItem(561, 200, -1);
			c.inventory.addItem(558, 50, -1);
			c.inventory.addItem(560, 1000, -1);
			c.inventory.addItem(565, 1000, -1);
			c.inventory.addItem(4587, 1, -1);
			c.inventory.addItem(868, 250, -1);
			c.inventory.addItem(15273, 100, -1);
		}else
			c.sendMessage("Please don't abuse the starter pack.");
		c.getPA().showInterface(3559);
		c.canChangeAppearance = true;
	}

	public int getWearingAmount(){
		int count = 0;
		for(int j = 0; j < c.playerEquipment.length; j++){
			if(c.playerEquipment[j] > 0)
				count++;
		}
		return count;
	}

	public void useOperate(int itemId){
		if(c.playerRights == 3)
			System.out.println(c.playerName + " - Operate item: " + itemId);
		switch(itemId){
			case 1712:
			case 1710:
			case 1708:
			case 1706:
			case 10354:
			case 10356:
			case 10358:
			case 10360:
				if(c.playerEquipment[c.playerAmulet] == itemId)
					handleGlory(itemId, true);
				break;
			case 11283:
				if(c.playerEquipment[c.playerShield] != itemId)
					break;
				if(c.dfsCharges <= 0)
					c.sendMessage("Your shield is completely drained.");
				else if(c.playerIndex > 0)
					c.getCombat().handleDfs();
				else if(c.npcIndex > 0)
					c.getCombat().handleDfsNPC();
				break;
			case 11284:
				if(c.playerEquipment[c.playerShield] != itemId)
					break;
				c.sendMessage("Your shield is completely drained.");
				break;
			case 6731:
				if(c.playerEquipment[c.playerRing] != itemId)
					break;
				c.getCombat().handleSeers();
				break;
			case 2552:
			case 2554:
			case 2556:
			case 2558:
			case 2560:
			case 2562:
			case 2564:
			case 2566:
				if(c.playerEquipment[c.playerRing] != itemId)
					break;
				c.getPA().handleRODueling(itemId, true);
				break;
			case 24455:
			case 24456:
			case 24457:
				c.handleCrucibleEmote();
				break;
			case 14057:
				teleBroom();
				break;
		}
	}

	public void getSpeared(int otherX, int otherY){
		int x = c.absX - otherX;
		int y = c.absY - otherY;
		if(x > 0)
			x = 1;
		else if(x < 0)
			x = -1;
		if(y > 0)
			y = 1;
		else if(y < 0)
			y = -1;
		playerWalk(c.absX + x, c.absY + y);
		c.lastSpear = System.currentTimeMillis();
	}

	public void moveCheck(int xMove, int yMove){
		movePlayer(c.absX + xMove, c.absY + yMove, c.heightLevel);
	}

	public int findKiller(){
		int killer = c.playerId;
		int damage = 0;
		for(Player p : RegionManager.getLocalPlayers(c)){
			if(p == null)
				continue;
			if(p.playerId == c.playerId || p.playerId < 1)
				continue;
			if(c.goodDistance(c.absX, c.absY, p.absX, p.absY, 40) || c.goodDistance(c.absX, c.absY + 9400, p.absX, p.absY, 40) || c.goodDistance(c.absX, c.absY, p.absX, p.absY + 9400, 40))
				if(c.damageTaken[p.playerId] > damage){
					damage = c.damageTaken[p.playerId];
					killer = p.playerId;
				}
		}
		return killer;
	}

	public void resetTzhaar(){
		c.nextFightCave = Misc.currentTimeSeconds() + 5;
		c.waveId = 0;
		c.tzhaarToKill = 0;
		c.tzhaarKilled = 0;
		c.inCaveGame = false;
		c.tCaveGame = true;
		c.getPA().movePlayer(2438, 5168, 0);
	}

	public void enterCaves(){
		if(c.nextFightCave > Misc.currentTimeSeconds())
			return;
		c.inCaveGame = true;
		c.tCaveGame = true;
		c.absX = 2413;
		c.absY = 5117;
		c.getPA().movePlayer(2413, 5117, c.playerId * 4);
		c.waveId = 0;
		c.tzhaarToKill = 0;
		c.tzhaarKilled = 0;
		c.correctCoordinates();
	}

	public void appendPoison(int damage){
		if(System.currentTimeMillis() - c.lastPoisonSip > c.poisonImmune){
			c.sendMessage("You have been poisoned.");
			c.poisonDamage = damage;
		}
	}

	public boolean checkForPlayer(int x, int y){
		for(Player p : RegionManager.getLocalPlayers(Location.create(x, y))){
			if(p != null){
				if(p.getX() == x && p.getY() == y)
					return true;
			}
		}
		return false;
	}

	public void checkPouch(int i){
		if(i < 0)
			return;
		c.sendMessage("This pouch has " + c.pouches[i] + " rune ess in it.");
	}

	public void fillPouch(int i){
		if(i < 0)
			return;
		int toAdd = c.POUCH_SIZE[i] - c.pouches[i];
		if(toAdd > c.inventory.getItemCount(1436)){
			toAdd = c.inventory.getItemCount(1436);
		}
		if(toAdd > c.POUCH_SIZE[i] - c.pouches[i])
			toAdd = c.POUCH_SIZE[i] - c.pouches[i];
		if(toAdd > 0){
			c.inventory.deleteItem(1436, toAdd);
			c.pouches[i] += toAdd;
		}
	}

	public void emptyPouch(int i){
		if(i < 0)
			return;
		int toAdd = c.pouches[i];
		if(toAdd > c.inventory.freeSlots()){
			toAdd = c.inventory.freeSlots();
		}
		if(toAdd > 0){
			c.inventory.addItem(1436, toAdd, -1);
			c.pouches[i] -= toAdd;
		}
	}

	public void fixAllBarrows(){
		int totalCost = 0;
		int cashAmount = c.inventory.getItemCount(995);
		for(int j = 0; j < c.inventory.items.length; j++){
			boolean breakOut = false;
			for(int i = 0; i < c.getItems().brokenBarrows.length; i++){
				if(c.inventory.items[j] == null)
					continue;
				if(c.inventory.items[j].id - 1 == c.getItems().brokenBarrows[i][1]){
					if(totalCost + 80000 > cashAmount){
						breakOut = true;
						c.sendMessage("You have run out of money.");
						break;
					}else{
						totalCost += 80000;
					}
					c.inventory.items[j].id = c.getItems().brokenBarrows[i][0] + 1;
				}
			}
			if(breakOut)
				break;
		}
		if(totalCost > 0)
			c.inventory.deleteItem(995, c.inventory.findItemSlot(995), totalCost);
	}

	public void handleLoginText(){
		c.getPA().sendText("Monster Teleport", 13037);
		c.getPA().sendText("Minigame Teleport", 13047);
		c.getPA().sendText("Boss Teleport", 13055);
		c.getPA().sendText("Pking Teleport", 13063);
		c.getPA().sendText("Skill Teleport", 13071);
		c.getPA().sendText("Monster Teleport", 1300);
		c.getPA().sendText("Minigame Teleport", 1325);
		c.getPA().sendText("Boss Teleport", 1350);
		c.getPA().sendText("Pking Teleport", 1382);
		c.getPA().sendText("Skill Teleport", 1415);
		c.getPA().sendText("City Teleport", 1454);
		c.getPA().sendText("Coming Soon (2)", 7457);
		c.getPA().sendText("Coming Soon (3)", 13097);
		c.getPA().sendText("Coming Soon (4)", 13089);
		c.getPA().sendText("City Teleport", 13081);
		c.getPA().sendText("hades5", 640);
		c.getPA().sendText("@or1@Players Online: @gre@" + PlayerHandler.getPlayerCount(), 663);
		c.getPA().sendText("@or1@Kill Count: @gre@ " + c.Rating, 7332);
		c.getPA().sendText("@or1@PK Points: @gre@ " + c.pkPoints, 7333);
		c.getPA().sendText("", 7334);
		c.getPA().sendText("@or1@Save: @gre@ Game", 7336);
		c.getPA().sendText("", 7383);
		c.getPA().sendText("", 7339);
		c.getPA().sendText("", 7338);
		c.getPA().sendText("", 7340);
		c.getPA().sendText("", 7346);
		c.getPA().sendText("", 7341);
		c.getPA().sendText("", 7342);
		c.getPA().sendText("", 7337);
		c.getPA().sendText("", 7343);
		c.getPA().sendText("", 7335);
		c.getPA().sendText("", 7344);
		c.getPA().sendText("", 7345);
		c.getPA().sendText("", 7347);
		c.getPA().sendText("", 7348);
	}

	public void handleWeaponStyle(){
		if(c.fightMode == 0){
			c.getPA().sendConfig(43, c.fightMode);
		}else if(c.fightMode == 1){
			c.getPA().sendConfig(43, 3);
		}else if(c.fightMode == 2){
			c.getPA().sendConfig(43, 1);
		}else if(c.fightMode == 3){
			c.getPA().sendConfig(43, 2);
		}
	}
}

class SortedItems<E> extends ArrayList<E>{
	private static final long serialVersionUID = 6769346943789104929L;

	public boolean add(E e){
		if(this.size() == 0)
			return super.add(e);
		else{
			for(int i = 0; i < this.size(); i++){
				if(((DeathItem)this.get(i)).id == ((DeathItem)e).id){
					((DeathItem)this.get(i)).amount += ((DeathItem)e).amount;
					return true;
				}
			}
			for(int i = 0; i < this.size(); i++){
				if(((DeathItem)this.get(i)).value < ((DeathItem)e).value){
					super.add(i, e);
					return true;
				}else if(((DeathItem)this.get(i)).value == ((DeathItem)e).value){
					if(i + 1 == this.size())
						return super.add(e);
					else{
						super.add(i + 1, e);
						return true;
					}
				}
			}
		}
		return super.add(e);
	}
}

class DeathItem{
	public int id = 0, amount = 0, value = 0;

	public DeathItem(int id, int amount, int value){
		this.id = id;
		this.amount = amount;
		this.value = value;
	}
}