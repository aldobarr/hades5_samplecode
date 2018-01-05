package server.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Scanner;

import server.Config;
import server.Connection;
import server.Server;
import server.model.items.MarketItem;
import server.model.items.bank.Bank;
import server.model.items.bank.BankItem;
import server.model.items.bank.Tab;
import server.model.minigames.CastleWars;
import server.model.minigames.ClanWars;
import server.model.minigames.HowlOfDeathManager;
import server.model.players.Client;
import server.model.players.Player;
import server.model.players.PlayerHandler;
import server.model.players.PlayerSave;
import server.model.region.RegionManager;
import server.net.ConnectionHandler;
import server.netty.RS2ProtocolHandler;
import server.util.Misc;
import server.util.MySQLManager;
import server.util.SimpleTimer;
import server.world.Market;

/**
 * 
 * @author hadesflames
 * 
 */
public class HadesThread implements Runnable{
	public Thread thread;
	public Client c = null;
	public boolean ip = false;
	private SimpleTimer debugTimer;
	private long sleepTime = 0, cycleRate = 575, cycleTime, value;
	public InetSocketAddress addr = null;
	public ConnectionHandler ch = null;
	public HighscoreAction hA = null;
	public int action = 0, type = -1, id = -1;
	public String reason = "", command = "", command2 = "";
	public ArrayList<String[]> data = null;
	public static int WINNING_NUM = 0;
	public static final int VOTE = 1, SAVE_HIGHSCORE = 2, SAVE_OFFLINE_HIGHSCORE = 3, CLAIM_LOTTERY = 4;
	public static final int ENTER_LOTTERY = 5, CHECK_LOTTERY = 6, AUTO_BAN = 7, LOTTERY = 8;
	public static final int EMAIL_MESSAGE = 9, SAVE_ALL_PLAYERS = 10, GIVE_DONOR = 11, MUTE = 12;
	public static final int CHECK_NAME = 13, IP_MUTE = 14, CHECK_IP = 15, BAN = 16, IP_BAN = 17;
	public static final int CHECK_BANK = 18, CHECK_ITEMS = 19, HS_BAN = 20, SERVER_LISTENER = 21;
	public static final int GAME_THREAD = 22, UPDATE_USERS_ONLINE = 23, UNBAN = 24, CHECK_NAME2 = 25;
	public static final int UN_MUTE = 26, UNIP_MUTE = 27, UNIPBAN = 28, HSUNBAN = 29, CHECK_EQUIP = 30;
	public static final int REPORT = 31, REGION_MANAGER = 32, UPDATE = 33, CHECK_DONOR_ITEM = 34, MAC_BAN = 35;
	public static final int UN_MAC_BAN = 36, CHECK_MAC = 37, RESET_DONOR_ITEM = 38, REMOVE_OLD_MACS = 39;
	public static final int ADD_NEW_MAC = 40, UPDATE_LAST_ONLINE = 41, DEMOTE = 42, POWERWARS = 43;
	public static final int UPDATE_MARKET_VALUE = 44, UPDATE_MARKET_CHANGE = 45, SAVE_CLAN_INFO = 46;
	
	@Override
	public void run(){
		switch(action){
			case 1:
				vote();
				break;
			case 2:
				saveHighScores();
				break;
			case 3:
				saveOfflineHighscores();
				break;
			case 4:
				claimLottery();
				break;
			case 5:
				enterLottery();
				break;
			case 6:
				checkLottery();
				break;
			case 7:
				autoBan();
				break;
			case 8:
				lottery();
				break;
			case 9:
				emailMessage();
				break;
			case 10:
				saveAll();
				break;
			case 11:
				new DonorReward(c);
				break;
			case 12:
				mute();
				break;
			case 13:
				getName();
				break;
			case 14:
				ipMute();
				break;
			case 15:
				checkIP();
				break;
			case 16:
				ban();
				break;
			case 17:
				ipBan();
				break;
			case 18:
				checkBank();
				break;
			case 19:
				checkItems();
				break;
			case 20:
				hsBan();
				break;
			case 21:
				// Old MINA stuff.
				break;
			case 22:
				gameThread();
				updateUsersOnline();
				break;
			case 23:
				updateUsersOnline();
				break;
			case 24:
				unban();
				break;
			case 25:
				checkName2();
				break;
			case 26:
				unmute();
				break;
			case 27:
				unipmute();
				break;
			case 28:
				unipban();
				break;
			case 29:
				hsunban();
				break;
			case 30:
				checkEquip();
				break;
			case 31:
				report();
				break;
			case 32:
				regionCleanup();
				break;
			case 33:
				update();
				break;
			case 34:
				checkDItem();
				break;
			case 35:
				macBan();
				break;
			case 36:
				unMacBan();
				break;
			case 37:
				checkMac();
				break;
			case 38:
				resetDItem();
				break;
			case 39:
				removeOldMacs();
				break;
			case 40:
				addNewMac();
				break;
			case 41:
				updateLastOnline();
				break;
			case 42:
				demote();
				break;
			case 43:
				powerWars();
				break;
			case 44:
				updateMarketValue();
				break;
			case 45:
				updateMarketChange();
				break;
			case 46:
				saveClanInfo();
				break;
		}
	}
	
	private void saveClanInfo(){
		Server.clanChat.saveClanInfo();
	}
	
	private void updateMarketChange(){
		int id = this.id;
		MarketItem item = Market.getMarketItem(id);
		if(item == null)
			return;
		long value = item.value;
		long change = this.value;
		try(MySQLManager umv = new MySQLManager(MySQLManager.SERVER)){
			umv.updateMarketChange(id, value, change);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void updateMarketValue(){
		int id = this.id;
		MarketItem item = Market.getMarketItem(id);
		if(item == null)
			return;
		try(MySQLManager umv = new MySQLManager(MySQLManager.SERVER)){
			umv.updateMarketValue(id, item);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void powerWars(){
		try(MySQLManager pw = new MySQLManager(MySQLManager.FORUM)){
			pw.powerWars(data, Misc.currentTimeSeconds());
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void demote(){
		String name = "";
		try(MySQLManager sqlName = new MySQLManager(MySQLManager.SERVER)){
			name = sqlName.getName(command);
		}catch(Exception e){
			e.printStackTrace();
		}
		if(name.isEmpty())
			c.sendMessage("That player was not found.");
		else{
			File f = new File("Data/characters/" + name.toLowerCase() + ".txt");
			boolean exists = false;
			String write = "";
			try(Scanner in = new Scanner(f)){
				String line = "";
				String line2[] = null;
				String new_line = System.getProperty("line.separator");
				while(in.hasNextLine()){
					line = in.nextLine();
					if(line.contains("character-rights")){
						line = "character-rights = {r}";
						exists = true;
						write += line + new_line;
					}else if(line.contains("senior-mod")){
						line = "senior-mod = false";
						write += line + new_line;
					}else if(line.contains("Donator")){
						line2 = line.split("=");
						write += line + new_line;
						write = write.replace("{r}", Integer.parseInt(line2[1].trim()) == 1 ? "5" : "0");
					}else
						write += line + new_line;
				}
			}catch(Exception e){
				e.printStackTrace();
				c.sendMessage("There was some kind of problem...");
				return;
			}
			if(exists){
				try(BufferedWriter out = new BufferedWriter(new FileWriter(f))){
					out.write(write);
				}catch(Exception e){
					e.printStackTrace();
				}
				c.sendMessage("You have made " + command + " a Player.");
			}else
				c.sendMessage("That player does not exist.");
		}
	}
	
	private void updateLastOnline(){
		try(MySQLManager ulo = new MySQLManager(MySQLManager.SERVER)){
			ulo.updateLastOnline(c.recoverId);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void addNewMac(){
		try(MySQLManager addMac = new MySQLManager(MySQLManager.SERVER)){
			addMac.addMac(id, command, command2);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void removeOldMacs(){
		try(MySQLManager removeMacs = new MySQLManager(MySQLManager.SERVER)){
			removeMacs.removeMacs(command);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void checkMac(){
		String name = "";
		try(MySQLManager sqlName = new MySQLManager(MySQLManager.SERVER)){
			name = sqlName.getName(command);
		}catch(Exception e){
			e.printStackTrace();
		}
		if(name.isEmpty()){
			c.sendMessage("That character was not found.");
			return;
		}
		boolean online = false;
		for(int i = 0; i<PlayerHandler.players.length; i++){
			if(PlayerHandler.players[i] == null)
				continue;
			if(!PlayerHandler.players[i].originalName.equalsIgnoreCase(name))
				continue;
			online = true;
			String mac = PlayerHandler.players[i].macAddress;
			c.sendMessage((mac.isEmpty() ? "Unable to find that player's mac address." : ("That player's mac is " + mac)));
			break;
		}
		if(!online){
			String mac = Connection.findMAC(name);
			c.sendMessage((mac.isEmpty() ? "That character was not found." : (mac.equalsIgnoreCase("-") ? "Unable to find that player's mac address." : ("That player's mac is " + mac))));
		}
	}
	
	private void unMacBan(){
		String name = "";
		try(MySQLManager sqlName = new MySQLManager(MySQLManager.SERVER)){
			name = sqlName.getName(command);
		}catch(Exception e){
			e.printStackTrace();
		}
		if(name.isEmpty()){
			c.sendMessage("That user does not exist.");
			return;
		}
		try{
			Connection.removeNameFromBanList(name);
			Connection.unIPBanUser(name);
			c.sendMessage("You have un-mac-banned the user " + name + " with the mac: " + Connection.unMacUser(name));
		}catch(Exception e){
			c.sendMessage("Looks like something went wrong...");
		}
	}
	
	private void macBan(){
		try{
			String name = command;
			String playerToBan = "";
			try(MySQLManager sqlName = new MySQLManager(MySQLManager.SERVER)){
				playerToBan = sqlName.getName(name);
			}catch(Exception e){
				e.printStackTrace();
			}
			if(playerToBan.equals(Config.OWNER)){
				c.sendMessage("Yeah...no...");
				return;
			}
			if(playerToBan.isEmpty()){
				c.sendMessage("That user does not exist.");
				return;
			}
			Connection.addNameToBanList(playerToBan);
			boolean iped = false, online = false;
			for(int i = 0; i < Config.MAX_PLAYERS; i++){
				if(PlayerHandler.players[i] != null){
					if(PlayerHandler.players[i].playerName.equalsIgnoreCase(name)){
						Client c2 = (Client)PlayerHandler.players[i];
						if((c.playerRights <= c2.playerRights && c2.playerRights != 5) && !c.inOwnersArray(c.playerName)){
							Connection.removeNameFromBanList(playerToBan);
							return;
						}
						Connection.addIpToBanList(PlayerHandler.players[i].connectedFrom);
						if(PlayerHandler.players[i].macAddress.isEmpty()){
							c.sendMessage("Unable to mac ban. Mac unavailable.");
							c.sendMessage("User has been ip banned with the host " + PlayerHandler.players[i].connectedFrom);
							iped = true;
							online = true;
							break;
						}
						Connection.addMacToBanList(PlayerHandler.players[i].macAddress);
						c.sendMessage("You have mac banned the user: " + PlayerHandler.players[i].playerName + " with the host: " + PlayerHandler.players[i].macAddress);
						iped = true;
						online = true;
						new HadesThread(HadesThread.SAVE_HIGHSCORE, c2);
						synchronized(c2){
							c2.setDisconnected(true);
							c2.highscores = 1;
						}
						break;
					}
				}
			}
			if(!online){
				File f = new File("Data/characters/" + playerToBan.toLowerCase() + ".txt");
				boolean exists = false;
				int xp[] = new int[25], rights = -1, highscores = 1, count = 0, id = -1;
				String write = "";
				try(Scanner in = new Scanner(f)){
					String line = "";
					String line2[] = null;
					String new_line = System.getProperty("line.separator");
					while(in.hasNextLine()){
						line = in.nextLine();
						if(line.contains("character-rights")){
							line2 = line.split("=");
							rights = Integer.parseInt(line2[1].trim());
							exists = true;
							write += line + new_line;
						}else if(line.contains("recover-id")){
							line2 = line.split("=");
							id = Integer.parseInt(line2[1].trim());
							write += line + new_line;
						}else if(line.contains("character-skill")){
							line2 = line.split("\t");
							xp[count++] = Integer.parseInt(line2[2]);
							write += line + new_line;
						}else if(line.contains("highscores")){
							write += "highscores = 1" + new_line;
						}else
							write += line + new_line;
					}
				}catch(Exception e){
				}
				if(exists && rights != -1){
					new HadesThread(HadesThread.SAVE_OFFLINE_HIGHSCORE, playerToBan.toLowerCase(), xp, rights, highscores, id);
					try(BufferedWriter out = new BufferedWriter(new FileWriter(f))){
						out.write(write);
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}
			if(!iped){
				String mac = Connection.addMacToBanList(playerToBan, 0);
				if(mac.isEmpty())
					c.sendMessage("The mac address is unabailable. Unable to mac ban.");
				else
					c.sendMessage("You have banned the user: " + playerToBan + " with the host: " + mac);
			}
		}catch(Exception e){
			c.sendMessage("Player Must Be Offline.");
		}
	}

	private void checkDItem(){
		int amount = 0, total = 0, itemId = Integer.parseInt(command);
		synchronized(Server.donorItems){
			Iterator<Entry<Integer, Integer>> iter = Server.donorItems.entrySet().iterator();
			while(iter.hasNext()){
				int id = iter.next().getKey();
				int amt = Server.donorItems.get(id);
				if(id == itemId)
					amount = amt;
				total += amt;
			}
		}
		c.sendMessage("That item has been purchased " + amount + " time" + (amount == 1 ? "" : "s") + " from the donor shop.");
		c.sendMessage("There " + (total == 1 ? "has" : "have") + " been " + total + (total == 1 ? " purchase" : " purchases") + " from the donor shop.");
	}
	
	private void resetDItem(){
		int itemId = Integer.parseInt(command);
		if(Server.donorItems.containsKey(itemId)){
			synchronized(Server.donorItems){
				Server.donorItems.put(itemId, 0);
			}
			c.sendMessage("The item has been reset to 0 purchases.");
			return;
		}
		c.sendMessage("The item is not featured in the donation shop's purchased list.");
	}

	private void regionCleanup(){
		while(!Server.shutdownServer){
			try{
				Thread.sleep(700);
				RegionManager.cleanup();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	private void hsunban(){
		try{
			String name = "";
			try(MySQLManager sqlName = new MySQLManager(MySQLManager.SERVER)){
				name = sqlName.getName(command);
			}catch(Exception e){
				e.printStackTrace();
			}
			if(name.isEmpty()){
				c.sendMessage("That user does not exist.");
				return;
			}
			boolean online = false;
			for(int i = 0; i < PlayerHandler.players.length; i++){
				if(PlayerHandler.players[i] != null){
					if(PlayerHandler.players[i].originalName.equalsIgnoreCase(name)){
						synchronized(PlayerHandler.players[i]){
							Client c2 = (Client)PlayerHandler.players[i];
							synchronized(c2){
								if((c.playerRights <= c2.playerRights && c2.playerRights != 5) && !c.inOwnersArray(c.playerName))
									return;
								c2.highscores = 0;
								online = true;
								new HadesThread(HadesThread.SAVE_HIGHSCORE, c2);
								c2.setDisconnected(true);
							}
						}
						break;
					}
				}
			}
			if(!online){
				File f = new File("Data/characters/" + name.toLowerCase() + ".txt");
				boolean exists = false;
				int xp[] = new int[25], rights = -1, highscores = 0, count = 0, id = -1;
				String write = "";
				try(Scanner in = new Scanner(f)){
					String line = "";
					String line2[] = null;
					String new_line = System.getProperty("line.separator");
					while(in.hasNextLine()){
						line = in.nextLine();
						if(line.contains("character-rights")){
							line2 = line.split("=");
							rights = Integer.parseInt(line2[1].trim());
							exists = true;
							write += line + new_line;
						}else if(line.contains("recover-id")){
							line2 = line.split("=");
							id = Integer.parseInt(line2[1].trim());
							write += line + new_line;
						}else if(line.contains("character-skill")){
							line2 = line.split("\t");
							xp[count++] = Integer.parseInt(line2[2]);
							write += line + new_line;
						}else if(line.contains("highscores")){
							write += "highscores = 0" + new_line;
						}else
							write += line + new_line;
					}
				}catch(Exception e){}
				if(!exists || rights == -1){
					c.sendMessage("That user does not exist.");
					return;
				}else{
					new HadesThread(HadesThread.SAVE_OFFLINE_HIGHSCORE, name.toLowerCase(), xp, rights, highscores, id);
					try(BufferedWriter out = new BufferedWriter(new FileWriter(f))){
						out.write(write);
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}
			c.sendMessage("You have re-added the player " + name + " to the highscores.");
		}catch(Exception e){
			c.sendMessage("You must use as ::hsunban username");
		}
	}

	private void unipban(){
		String name = "";
		try(MySQLManager sqlName = new MySQLManager(MySQLManager.SERVER)){
			name = sqlName.getName(command);
		}catch(Exception e){
			e.printStackTrace();
		}
		if(name.isEmpty()){
			c.sendMessage("That user does not exist.");
			return;
		}
		try{
			if(Connection.isMacBanned(Connection.findMAC(name)) && !Config.OWNER.equalsIgnoreCase(c.playerName) && !Config.OWNER_HIDDEN.equalsIgnoreCase(c.playerName))
				c.sendMessage("You must first unmacban this user.");
			else{
				Connection.removeNameFromBanList(name);
				c.sendMessage("You have unbanned the user " + name + " with the host: " + Connection.unIPBanUser(name));
			}
		}catch(Exception e){
			c.sendMessage("Looks like something went wrong...");
		}
	}

	private void unipmute(){
		try{
			String name = "";
			try(MySQLManager sqlName = new MySQLManager(MySQLManager.SERVER)){
				name = sqlName.getName(command);
			}catch(Exception e){
				e.printStackTrace();
			}
			if(name.isEmpty())
				throw new Exception();
			Connection.unMuteUser(name);
			boolean removed = false;
			for(int i = 0; i < Config.MAX_PLAYERS; i++){
				if(PlayerHandler.players[i] != null){
					if(PlayerHandler.players[i].originalName.equalsIgnoreCase(name)){
						Client c2 = (Client)PlayerHandler.players[i];
						Connection.unIPMuteUser(c2.connectedFrom);
						c.sendMessage("You have Un Ip-Muted the user: " + PlayerHandler.players[i].playerName + " with the host: " + PlayerHandler.players[i].connectedFrom);
						removed = true;
						break;
					}
				}
			}
			if(!removed)
				c.sendMessage("You have unmuted the user: " + name + " with the host: " + Connection.unIPMuteUser(name, 0));
		}catch(Exception e){
			c.sendMessage("That user does not exist.");
		}
	}

	private void unmute(){
		try{
			String name = "";
			try(MySQLManager sqlName = new MySQLManager(MySQLManager.SERVER)){
				name = sqlName.getName(command);
			}catch(Exception e){
				e.printStackTrace();
			}
			if(name.isEmpty())
				throw new Exception();
			Connection.unMuteUser(name);
			for(int i = 0; i < Config.MAX_PLAYERS; i++){
				if(PlayerHandler.players[i] != null){
					if(PlayerHandler.players[i].originalName.equalsIgnoreCase(name)){
						synchronized(PlayerHandler.players[i]){
							Client c2 = (Client)PlayerHandler.players[i];
							synchronized(c2){
								c2.timeMuted = false;
								c2.muteTime = 0;
								c2.sendMessage("You have been unmuted by the user " + (Config.OWNER_HIDDEN.equalsIgnoreCase(c.playerName) ? Config.OWNER : c.playerName));
							}
						}
						break;
					}
				}
			}
			c.sendMessage("You have Unmuted the user: " + name);
		}catch(Exception e){
			c.sendMessage("That user does not exist.");
		}
	}

	private void update(){
		boolean status = Integer.parseInt(command) >= 0 ? true : false;
		try(MySQLManager update = new MySQLManager(MySQLManager.SERVER)){
			update.updateStatus(status);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void checkName2(){
		String name = "";
		try(MySQLManager sqlName = new MySQLManager(MySQLManager.SERVER)){
			name = sqlName.getName2(command);
		}catch(Exception e){
			e.printStackTrace();
		}
		if(name.isEmpty())
			c.sendMessage("That user does not exist.");
		else
			c.sendMessage("The display name is: " + name);
	}

	private void unban(){
		String name = "";
		try(MySQLManager sqlName = new MySQLManager(MySQLManager.SERVER)){
			name = sqlName.getName(command);
		}catch(Exception e){
			e.printStackTrace();
		}
		if(name.isEmpty()){
			c.sendMessage("That user does not exist.");
			return;
		}
		try{
			if(Connection.isIpBanned(Connection.findIP(name)) && !Config.OWNER.equalsIgnoreCase(c.playerName) && !Config.OWNER_HIDDEN.equalsIgnoreCase(c.playerName))
				c.sendMessage("You must first unipban this user.");
			else if(Connection.isMacBanned(Connection.findMAC(name)) && !Config.OWNER.equalsIgnoreCase(c.playerName) && !Config.OWNER_HIDDEN.equalsIgnoreCase(c.playerName))
				c.sendMessage("You must first unmacban this user.");
			else{
				Connection.removeNameFromBanList(name);
				c.sendMessage("You have unbanned the user " + name + ".");
			}
		}catch(Exception e){
			c.sendMessage("Looks like something went wrong...");
		}
	}

	private void updateUsersOnline(){
		try(MySQLManager uuo = new MySQLManager(MySQLManager.SERVER)){
			uuo.updateUsersOnline(PlayerHandler.getPlayerCount());
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void gameThread(){
		SimpleTimer miniGameTimer = new SimpleTimer();
		debugTimer = new SimpleTimer();
		while(!Server.shutdownServer){
			try{
				Thread.sleep(sleepTime >= 0 ? sleepTime : 575);
				miniGameTimer.reset();
				Server.fightPits.process();
				CastleWars.process();
				ClanWars.process();
				HowlOfDeathManager.getInstance().process();
				RS2ProtocolHandler.process();
				//MySQLManager.cleanConnections();
				//Server.pestControl.process();
				for(Player p : PlayerHandler.players)
					if(p != null)
						CastleWars.deleteGameItems((Client)p, true);
				cycleTime = miniGameTimer.elapsed();
				sleepTime = cycleRate - cycleTime;
				debug();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	private void debug(){
		if(debugTimer.elapsed() > 360 * 1000){
			System.gc();
			System.runFinalization();
			debugTimer.reset();
		}
	}

	private void autoBan(){
		String name = "";
		try(MySQLManager autoBan = new MySQLManager(MySQLManager.SERVER)){
			name = autoBan.getName(c.playerName);
			autoBan.postBan(c, reason, type);
		}catch(Exception e){
			e.printStackTrace();
		}
		if(name.isEmpty())
			name = c.playerName;
		Connection.addNameToBanList(name);
		Connection.addNameToFile(name);
		if(ip){
			Connection.addIpToBanList(c.connectedFrom);
			Connection.addIpToFile(c.connectedFrom);
		}
		synchronized(c){
			c.setDisconnected(true);
		}
	}

	private void report(){
		try(MySQLManager postBan = new MySQLManager(MySQLManager.FORUM)){
			postBan.postBan(c, reason, type);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void hsBan(){
		try{
			boolean online = false;
			String name = command.substring(6);
			for(int i = 0; i < PlayerHandler.players.length; i++){
				if(PlayerHandler.players[i] != null){
					if(PlayerHandler.players[i].playerName.equalsIgnoreCase(name)){
						Client c2 = (Client)PlayerHandler.players[i];
						if((c.playerRights <= c2.playerRights && c2.playerRights != 5) && !c.inOwnersArray(c.playerName))
							return;
						c2.highscores = 1;
						online = true;
						new HadesThread(HadesThread.SAVE_HIGHSCORE, c2);
						c2.setDisconnected(true);
					}
				}
			}
			if(!online){
				try(MySQLManager sqlName = new MySQLManager(MySQLManager.SERVER)){
					name = sqlName.getName(name);
				}catch(Exception e){
					e.printStackTrace();
				}
				if(name.isEmpty()){
					c.sendMessage("That characer does not exist.");
					return;
				}
				File f = new File("Data/characters/" + name.toLowerCase() + ".txt");
				boolean exists = false;
				int xp[] = new int[25], rights = -1, highscores = 1, count = 0, id = -1;
				String write = "";
				try(Scanner in = new Scanner(f)){
					String line = "";
					String line2[] = null;
					String new_line = System.getProperty("line.separator");
					while(in.hasNextLine()){
						line = in.nextLine();
						if(line.contains("character-rights")){
							line2 = line.split("=");
							rights = Integer.parseInt(line2[1].trim());
							exists = true;
							write += line + new_line;
						}else if(line.contains("recover-id")){
							line2 = line.split("=");
							id = Integer.parseInt(line2[1].trim());
							write += line + new_line;
						}else if(line.contains("character-skill")){
							line2 = line.split("\t");
							xp[count++] = Integer.parseInt(line2[2]);
							write += line + new_line;
						}else if(line.contains("highscores")){
							write += "highscores = 1" + new_line;
						}else
							write += line + new_line;
					}
				}catch(Exception e){}
				if(!exists || rights == -1){
					c.sendMessage("That characters does not exist.");
					return;
				}else{
					new HadesThread(HadesThread.SAVE_OFFLINE_HIGHSCORE, name.toLowerCase(), xp, rights, highscores, id);
					try(BufferedWriter out = new BufferedWriter(new FileWriter(f))){
						out.write(write);
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}
			c.sendMessage("You have removed the player " + name + " from the highscores.");
		}catch(Exception e){
			c.sendMessage("You must use as ::hsban username");
		}
	}

	private void checkEquip(){
		String name = "";
		try{
			name = command.substring(11);
		}catch(Exception e){
			c.sendMessage("You didn't enter a name.");
			return;
		}
		if(name.isEmpty()){
			c.sendMessage("You didn't enter a name.");
			return;
		}
		boolean isOnline = false;
		boolean exists = true;
		Client c2 = null;
		for(Player a : PlayerHandler.players){
			c2 = (Client)a;
			if(c2 != null){
				if(c2.playerName.equalsIgnoreCase(name)){
					isOnline = true;
					break;
				}
			}
		}
		if(isOnline){
			synchronized(c){
				c.oBank = new Bank(c);
				for(int i = 0; i < c2.playerEquipment.length; i++)
					if(c2.playerEquipment[i] > 0 && c2.playerEquipmentN[i] > 0)
						c.oBank.tabs.get(0).add(new BankItem(c2.playerEquipment[i] + 1, c2.playerEquipmentN[i], c2.playerEquipmentD[i]));
			}
		}else{
			try(MySQLManager sqlName = new MySQLManager(MySQLManager.SERVER)){
				name = sqlName.getName(name);
			}catch(Exception e){
				e.printStackTrace();
			}
			if(name.isEmpty()){
				c.sendMessage("That characer does not exist.");
				return;
			}
			File acc = new File("Data/characters/" + name + ".txt");
			try(Scanner in = new Scanner(acc)){
				c.oBank = new Bank(c);
				boolean bank = false;
				while(in.hasNextLine()){
					String nextLine = in.nextLine();
					if(nextLine.equalsIgnoreCase("[EQUIPMENT]")){
						bank = true;
						continue;
					}else if(nextLine.equalsIgnoreCase("[LOOK]"))
						break;
					else if(bank && !nextLine.toLowerCase().contains("character-equip"))
						continue;
					else if(bank && !nextLine.isEmpty()){
						String line = nextLine.split("=")[1];
						line = line.trim();
						String stuff[] = line.split("	");
						int key = Integer.parseInt(stuff[0].trim());
						int id = Integer.parseInt(stuff[1].trim());
						int amount = Integer.parseInt(stuff[2].trim());
						if(id <= 0 || amount <= 0)
							continue;
						int tabId = 0;
						int degrade = -1;
						BankItem item = new BankItem(id + 1, amount, degrade);
						int size = c.oBank.tabs.size();
						int pos = tabId > size ? size : tabId;
						synchronized(c){
							if(size <= tabId)
								c.oBank.tabs.add(pos, new Tab());
							c.oBank.tabs.get(pos).tabItems.add(key > c.oBank.tabs.get(pos).tabItems.size() ? c.oBank.tabs.get(pos).tabItems.size() : key, item);
						}
					}
				}
			}catch(Exception e){
				exists = false;
				c.sendMessage("Character not found.");
			}
		}
		if(exists){
			synchronized(c){
				c.getPA().openUpOtherBank();
				c.isBanking = false;
				c.oBank = null;
			}
		}
	}

	private void checkItems(){
		String name = "";
		try{
			name = command.substring(11);
		}catch(Exception e){
			c.sendMessage("You didn't enter a name.");
			return;
		}
		if(name.isEmpty()){
			c.sendMessage("You didn't enter a name.");
			return;
		}
		boolean isOnline = false;
		boolean exists = true;
		Client c2 = null;
		for(Player a : PlayerHandler.players){
			c2 = (Client)a;
			if(c2 != null){
				if(c2.playerName.equalsIgnoreCase(name)){
					isOnline = true;
					break;
				}
			}
		}
		if(isOnline){
			synchronized(c){
				c.oBank = new Bank(c);
				for(int i = 0; i < c2.inventory.items.length; i++)
					if(c2.inventory.items[i] != null)
						c.oBank.tabs.get(0).add(new BankItem(c2.inventory.items[i].id, c2.inventory.items[i].amount, -1));
			}
		}else{
			try(MySQLManager sqlName = new MySQLManager(MySQLManager.SERVER)){
				name = sqlName.getName(name);
			}catch(Exception e){
				e.printStackTrace();
			}
			if(name.isEmpty()){
				c.sendMessage("That characer does not exist.");
				return;
			}
			File acc = new File("Data/characters/" + name + ".txt");
			try(Scanner in = new Scanner(acc)){
				c.oBank = new Bank(c);
				boolean bank = false;
				while(in.hasNextLine()){
					String nextLine = in.nextLine();
					if(nextLine.equalsIgnoreCase("[items]")){
						bank = true;
						continue;
					}else if(nextLine.equalsIgnoreCase("[bank]"))
						break;
					else if(bank && !nextLine.toLowerCase().contains("character-item"))
						continue;
					else if(bank && !nextLine.isEmpty()){
						String line = nextLine.split("=")[1];
						line = line.trim();
						String stuff[] = line.split("	");
						int key = Integer.parseInt(stuff[0].trim());
						int id = Integer.parseInt(stuff[1].trim());
						int amount = Integer.parseInt(stuff[2].trim());
						int tabId = 0;
						int degrade = -1;
						BankItem item = new BankItem(id, amount, degrade);
						int size = c.oBank.tabs.size();
						int pos = tabId > size ? size : tabId;
						synchronized(c){
							if(size <= tabId)
								c.oBank.tabs.add(pos, new Tab());
							c.oBank.tabs.get(pos).tabItems.add(key > c.oBank.tabs.get(pos).tabItems.size() ? c.oBank.tabs.get(pos).tabItems.size() : key, item);
						}
					}
				}
			}catch(Exception e){
				exists = false;
				c.sendMessage("Character not found.");
			}
		}
		if(exists){
			synchronized(c){
				c.getPA().openUpOtherBank();
				c.isBanking = false;
				c.oBank = null;
			}
		}
	}

	private void checkBank(){
		boolean isOnline = false;
		boolean exists = true;
		String name = "";
		try{
			name = command.substring(10);
		}catch(Exception e){
			c.sendMessage("You didn't enter a name.");
			return;
		}
		if(name.isEmpty()){
			c.sendMessage("You didn't enter a name.");
			return;
		}
		Client c2 = null;
		for(Player a : PlayerHandler.players){
			c2 = (Client)a;
			if(c2 != null){
				if(c2.playerName.equalsIgnoreCase(name)){
					isOnline = true;
					break;
				}
			}
		}
		if(isOnline){
			synchronized(c){
				c.oBank = c2.bank.clone(c);
			}
		}else{
			try(MySQLManager sqlName = new MySQLManager(MySQLManager.SERVER)){
				name = sqlName.getName(name);
			}catch(Exception e){
				e.printStackTrace();
			}
			if(name.isEmpty()){
				c.sendMessage("That characer does not exist.");
				return;
			}
			File acc = new File("Data/characters/" + name + ".txt");
			try(Scanner in = new Scanner(acc)){
				c.oBank = new Bank(c);
				boolean bank = false;
				while(in.hasNextLine()){
					String nextLine = in.nextLine();
					if(nextLine.equalsIgnoreCase("[bank]")){
						bank = true;
						continue;
					}else if(nextLine.trim().equalsIgnoreCase("[qp]") || nextLine.trim().equalsIgnoreCase("[FRIENDS]"))
						break;
					else if(bank && !nextLine.toLowerCase().contains("character-bank"))
						continue;
					else if(bank && !nextLine.isEmpty()){
						String line = nextLine.split("=")[1];
						line = line.trim();
						String stuff[] = line.split("	");
						int key = Integer.parseInt(stuff[0].trim());
						int id = Integer.parseInt(stuff[1].trim());
						int amount = Integer.parseInt(stuff[2].trim());
						int tabId = 0;
						int degrade = -1;
						try{
							try{
								degrade = Integer.parseInt(stuff[3]);
							}catch(Exception ex){
							}
							tabId = Integer.parseInt(stuff[4]);
						}catch(Exception e){
						}
						BankItem item = new BankItem(id, amount, degrade);
						int size = c.oBank.tabs.size();
						int pos = tabId > size ? size : tabId;
						synchronized(c){
							if(size <= tabId)
								c.oBank.tabs.add(pos, new Tab());
							c.oBank.tabs.get(pos).tabItems.add(key > c.oBank.tabs.get(pos).tabItems.size() ? c.oBank.tabs.get(pos).tabItems.size() : key, item);
						}
					}
				}
			}catch(Exception e){
				e.printStackTrace();
				exists = false;
				c.sendMessage("Character not found.");
			}
		}
		if(exists){
			synchronized(c){
				c.getPA().openUpOtherBank();
				c.isBanking = false;
				c.oBank = null;
			}
		}
	}

	private void ipBan(){
		try{
			String name = command.substring(6);
			String playerToBan = "";
			try(MySQLManager sqlName = new MySQLManager(MySQLManager.SERVER)){
				playerToBan = sqlName.getName(name);
			}catch(Exception e){
				e.printStackTrace();
			}
			if(playerToBan.equals(Config.OWNER)){
				c.sendMessage("Yeah...no...");
				return;
			}
			if(playerToBan.isEmpty()){
				c.sendMessage("That user does not exist.");
				return;
			}
			Connection.addNameToBanList(playerToBan);
			boolean iped = false, online = false;
			for(int i = 0; i < Config.MAX_PLAYERS; i++){
				if(PlayerHandler.players[i] != null){
					if(PlayerHandler.players[i].playerName.equalsIgnoreCase(name)){
						Client c2 = (Client)PlayerHandler.players[i];
						if((c.playerRights <= c2.playerRights && c2.playerRights != 5) && !c.inOwnersArray(c.playerName)){
							Connection.removeNameFromBanList(playerToBan);
							return;
						}
						Connection.addIpToBanList(PlayerHandler.players[i].connectedFrom);
						c.sendMessage("You have IP banned the user: " + PlayerHandler.players[i].playerName + " with the host: " + PlayerHandler.players[i].connectedFrom);
						iped = true;
						online = true;
						new HadesThread(HadesThread.SAVE_HIGHSCORE, c2);
						synchronized(c2){
							c2.setDisconnected(true);
							c2.highscores = 1;
						}
						break;
					}
				}
			}
			if(!online){
				File f = new File("Data/characters/" + playerToBan.toLowerCase() + ".txt");
				boolean exists = false;
				int xp[] = new int[25], rights = -1, highscores = 1, count = 0, id = -1;
				String write = "";
				try(Scanner in = new Scanner(f)){
					String line = "";
					String line2[] = null;
					String new_line = System.getProperty("line.separator");
					while(in.hasNextLine()){
						line = in.nextLine();
						if(line.contains("character-rights")){
							line2 = line.split("=");
							rights = Integer.parseInt(line2[1].trim());
							exists = true;
							write += line + new_line;
						}else if(line.contains("recover-id")){
							line2 = line.split("=");
							id = Integer.parseInt(line2[1].trim());
							write += line + new_line;
						}else if(line.contains("character-skill")){
							line2 = line.split("\t");
							xp[count++] = Integer.parseInt(line2[2]);
							write += line + new_line;
						}else if(line.contains("highscores")){
							write += "highscores = 1" + new_line;
						}else
							write += line + new_line;
					}
				}catch(Exception e){}
				if(exists && rights != -1){
					new HadesThread(HadesThread.SAVE_OFFLINE_HIGHSCORE, playerToBan.toLowerCase(), xp, rights, highscores, id);
					try(BufferedWriter out = new BufferedWriter(new FileWriter(f));){
						out.write(write);
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}
			if(!iped)
				c.sendMessage("You have banned the user: " + playerToBan + " with the host: " + Connection.addIpToBanList(playerToBan, 0));
		}catch(Exception e){
			c.sendMessage("Player Must Be Offline.");
		}
	}
	
	private void ban(){
		try{
			String name = command.substring(4);
			String playerToBan = "";
			try(MySQLManager sqlName = new MySQLManager(MySQLManager.SERVER)){
				playerToBan = sqlName.getName(name);
			}catch(Exception e){
				e.printStackTrace();
			}
			if(playerToBan.equals(Config.OWNER)){
				c.sendMessage("Yeah...no...");
				return;
			}
			if(playerToBan.isEmpty()){
				c.sendMessage("That character does not exist.");
				return;
			}
			Connection.addNameToBanList(playerToBan);
			boolean online = false;
			for(int i = 0; i < Config.MAX_PLAYERS; i++){
				if(PlayerHandler.players[i] != null){
					if(PlayerHandler.players[i].playerName.equalsIgnoreCase(name)){
						Client c2 = (Client)PlayerHandler.players[i];
						if((c.playerRights <= c2.playerRights && c2.playerRights != 5) && !c.inOwnersArray(c.playerName)){
							Connection.removeNameFromBanList(playerToBan);
							return;
						}
						online = true;
						synchronized(c2){
							c2.setDisconnected(true);
							c2.highscores = 1;
						}
						new HadesThread(HadesThread.SAVE_OFFLINE_HIGHSCORE, c2);
						break;
					}
				}
			}
			if(!online){
				File f = new File("Data/characters/" + playerToBan.toLowerCase() + ".txt");
				boolean exists = false;
				int xp[] = new int[25], rights = -1, highscores = 1, count = 0, id = -1;
				String write = "";
				try(Scanner in = new Scanner(f)){
					String line = "";
					String line2[] = null;
					String new_line = System.getProperty("line.separator");
					while(in.hasNextLine()){
						line = in.nextLine();
						if(line.contains("character-rights")){
							line2 = line.split("=");
							rights = Integer.parseInt(line2[1].trim());
							exists = true;
							write += line + new_line;
						}else if(line.contains("recover-id")){
							line2 = line.split("=");
							id = Integer.parseInt(line2[1].trim());
							write += line + new_line;
						}else if(line.contains("character-skill")){
							line2 = line.split("\t");
							xp[count++] = Integer.parseInt(line2[2]);
							write += line + new_line;
						}else if(line.contains("highscores")){
							write += "highscores = 1" + new_line;
						}else
							write += line + new_line;
					}
				}catch(Exception e){}
				if(!exists || rights == -1){
					c.sendMessage("That characters does not exist.");
					return;
				}else{
					new HadesThread(HadesThread.SAVE_OFFLINE_HIGHSCORE, playerToBan.toLowerCase(), xp, rights, highscores, id);
					try(BufferedWriter out = new BufferedWriter(new FileWriter(f))){
						out.write(write);
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}
			c.sendMessage("You have banned the user: " + playerToBan);
		}catch(Exception e){
			c.sendMessage("Player Must Be Offline.");
		}
	}

	private void checkIP(){
		String name = "";
		try(MySQLManager sqlName = new MySQLManager(MySQLManager.SERVER)){
			name = sqlName.getName(command);
		}catch(Exception e){
			e.printStackTrace();
		}
		if(name.isEmpty()){
			c.sendMessage("That character was not found.");
			return;
		}
		boolean online = false;
		for(int i = 0; i<PlayerHandler.players.length; i++){
			if(PlayerHandler.players[i] == null)
				continue;
			if(!PlayerHandler.players[i].originalName.equalsIgnoreCase(name))
				continue;
			online = true;
			String ip = PlayerHandler.players[i].connectedFrom;
			c.sendMessage("That player's ip is" + ip);
			break;
		}
		if(!online){
			String ip = Connection.findIP(name);
			c.sendMessage((ip.isEmpty() ? "That character was not found." : ("That player's ip is " + ip)));
		}
	}

	private void ipMute(){
		try{
			String name = command.substring(7);
			String playerToBan = "";
			try(MySQLManager sqlName = new MySQLManager(MySQLManager.SERVER)){
				playerToBan = sqlName.getName(name);
			}catch(Exception e){
				e.printStackTrace();
			}
			if(playerToBan.equals(Config.OWNER)){
				c.sendMessage("Yeah...no...");
				return;
			}
			if(playerToBan.isEmpty()){
				c.sendMessage("That user does not exist.");
				return;
			}
			Connection.addNameToMuteList(playerToBan);
			boolean iped = false;
			for(int i = 0; i < Config.MAX_PLAYERS; i++){
				if(PlayerHandler.players[i] != null){
					if(PlayerHandler.players[i].originalName.equalsIgnoreCase(name)){
						Client c2 = (Client)PlayerHandler.players[i];
						if((c.playerRights <= c2.playerRights && c2.playerRights != 5) && !c.inOwnersArray(c.playerName)){
							Connection.unMuteUser(playerToBan);
							return;
						}
						Connection.addIpToMuteList(c2.connectedFrom);
						c.sendMessage("You have IP Muted the user: " + PlayerHandler.players[i].playerName + " with the host: " + PlayerHandler.players[i].connectedFrom);
						c2.sendMessage("You have been muted by: " + (Config.OWNER_HIDDEN.equalsIgnoreCase(c.playerName) ? Config.OWNER : c.playerName));
						iped = true;
						break;
					}
				}
			}
			if(!iped)
				c.sendMessage("You have muted the user: " + playerToBan + " with the host: " + Connection.addIpToMuteList(playerToBan, 0));
		}catch(Exception e){
			c.sendMessage("Player Must Be Offline.");
		}
	}

	private void getName(){
		String name = "";
		try(MySQLManager sqlName = new MySQLManager(MySQLManager.SERVER)){
			name = sqlName.getName(command);
		}catch(Exception e){
			e.printStackTrace();
		}
		if(name.isEmpty())
			c.sendMessage("That player does not exist.");
		else
			c.sendMessage("The original name is: " + name);
	}

	private void mute(){
		try{
			String playerToBan = command.substring(5);
			String name = "";
			try(MySQLManager sqlName = new MySQLManager(MySQLManager.SERVER)){
				name = sqlName.getName(playerToBan);
			}catch(Exception e){
				e.printStackTrace();
			}
			if(name.equals(Config.OWNER)){
				c.sendMessage("Yeah...no...");
				return;
			}
			if(name.isEmpty()){
				c.sendMessage("That user does not exist.");
				return;
			}
			Connection.addNameToMuteList(name);
			c.sendMessage("You have Muted the user: " + playerToBan);
			for(int i = 0; i < Config.MAX_PLAYERS; i++){
				if(PlayerHandler.players[i] != null){
					if(PlayerHandler.players[i].originalName.equalsIgnoreCase(name)){
						Client c2 = (Client)PlayerHandler.players[i];
						if((c.playerRights <= c2.playerRights && c2.playerRights != 5) && !c.inOwnersArray(c.playerName)){
							Connection.unMuteUser(name);
							return;
						}
						c2.sendMessage("You have been muted by: " + (Config.OWNER_HIDDEN.equalsIgnoreCase(c.playerName) ? Config.OWNER : c.playerName));
						break;
					}
				}
			}
		}catch(Exception e){
			c.sendMessage("Player Must Be Offline.");
		}
	}

	private void saveAll(){
		System.out.println("Saving players...");
		synchronized(PlayerHandler.players){
			for(int i = 0; i < Config.MAX_PLAYERS; i++){
				if(PlayerHandler.players[i] != null){
					PlayerSave.saveGame((Client)PlayerHandler.players[i]);
				}
			}
		}
		System.out.println("All players saved...");
	}

	private void emailMessage(){
		try(MySQLManager email = new MySQLManager(MySQLManager.SERVER)){
			email.showEmail(c);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void lottery(){
		try(MySQLManager lottery = new MySQLManager(MySQLManager.FORUM)){
			lottery.lottery();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void checkLottery(){
		try(MySQLManager lottery = new MySQLManager(MySQLManager.FORUM)){
			lottery.checkLottery(c);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void enterLottery(){
		try(MySQLManager lottery = new MySQLManager(MySQLManager.FORUM)){
			lottery.enterLottery(c);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void claimLottery(){
		try(MySQLManager lottery = new MySQLManager(MySQLManager.FORUM)){
			lottery.claimLottery(c);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void saveOfflineHighscores(){
		if(hA == null)
			return;
		try(MySQLManager hs = new MySQLManager(MySQLManager.FORUM)){
			hs.saveHighScore(hA.offlineName.toLowerCase(), hA.offlineXP, hA.offlineRights, hA.offlineHighscores, hA.id);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void saveHighScores(){
		try(MySQLManager hs = new MySQLManager(MySQLManager.FORUM)){
			hs.saveHighScore(c);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void vote(){
		try(MySQLManager vote = new MySQLManager(MySQLManager.SERVER)){
			vote.vote(c);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public HadesThread(int action){
		this.action = action;
		thread = new Thread(this);
		thread.start();
	}
	
	public HadesThread(int action, int id){
		this.action = action;
		this.id = id;
		thread = new Thread(this);
		thread.start();
	}
	
	public HadesThread(int action, int id, long value){
		this.action = action;
		this.id = id;
		this.value = value;
		thread = new Thread(this);
		thread.start();
	}

	public HadesThread(int action, String offlineName, int offlineXP[], int offlineRights, int offlineHighscores, int id){
		this.action = action;
		hA = new HighscoreAction(offlineName, offlineXP, offlineRights, offlineHighscores, id);
		thread = new Thread(this);
		thread.start();
	}

	public HadesThread(int action, Client c){
		this.action = action;
		this.c = c;
		thread = new Thread(this);
		thread.start();
	}

	public HadesThread(int action, Client c, String reason, int type, boolean ip){
		this.action = action;
		this.c = c;
		this.reason = reason;
		this.type = type;
		this.ip = ip;
		thread = new Thread(this);
		thread.start();
	}
	
	public HadesThread(int action, String command){
		this.action = action;
		this.command = command;
		thread = new Thread(this);
		thread.start();
	}

	public HadesThread(int action, Client c, String command){
		this.action = action;
		this.c = c;
		this.command = command;
		thread = new Thread(this);
		thread.start();
	}
	
	public HadesThread(int action, int id, String command, String command2){
		this.action = action;
		this.id = id;
		this.command = command;
		this.command2 = command2;
		thread = new Thread(this);
		thread.start();
	}
	
	public HadesThread(int action, ArrayList<String[]> data){
		this.action = action;
		this.data = data;
		thread = new Thread(this);
		thread.start();
	}
}

class HighscoreAction{
	public int offlineXP[], offlineRights = -1, offlineHighscores = -1, id = -1;
	public String offlineName = "";

	public HighscoreAction(String offlineName, int offlineXP[], int offlineRights, int offlineHighscores, int id){
		this.offlineXP = offlineXP;
		this.id = id;
		this.offlineRights = offlineRights;
		this.offlineHighscores = offlineHighscores;
		this.offlineName = offlineName;
	}
}