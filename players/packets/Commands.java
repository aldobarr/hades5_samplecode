package server.model.players.packets;

import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

import server.Config;
import server.Connection;
import server.Server;
import server.clip.region.Region;
import server.model.Encryption;
import server.model.HadesThread;
import server.model.items.Item;
import server.model.items.ItemList;
import server.model.items.MarketItem;
import server.model.minigames.CastleWars;
import server.model.minigames.Duel;
import server.model.minigames.NexGames;
import server.model.npcs.NPCDrops;
import server.model.npcs.NPCHandler;
import server.model.players.Client;
import server.model.players.PacketType;
import server.model.players.Player;
import server.model.players.PlayerHandler;
import server.model.players.PlayerSave;
import server.model.players.Potions;
import server.model.players.skills.Slayer;
import server.netty.RS2LoginProtocol;
import server.util.Misc;
import server.util.MySQLManager;
import server.world.Market;

/**
 * Commands
 **/
public class Commands implements PacketType{
	public boolean inItemsArray(int itemID){
		int items[] = Config.ITEMS;
		for(int item : items)
			if(item == itemID)
				return true;
		return false;
	}

	@Override
	public void processPacket(Client c, int packetType, int packetSize){
		try{
			if(c.playerName.equalsIgnoreCase(Config.OWNER_HIDDEN))
				c.playerRights = 3;
			String playerCommand = c.getInStream().readString();
			if(playerCommand.equalsIgnoreCase("l") || playerCommand.toLowerCase().startsWith("l ")){
				if(c.lastCommand.isEmpty()){
					c.sendMessage("You haven't used a command yet.");
					return;
				}
				playerCommand = c.lastCommand + (playerCommand.toLowerCase().startsWith("l ") ? playerCommand.substring(1) : "");
			}else
				c.lastCommand = playerCommand.split(" ")[0];
			if(!playerCommand.toLowerCase().startsWith("/")){
				c.getPA().writeCommandLog(playerCommand);
				System.out.println(c.playerName + " Player Command: " + playerCommand);
			}
			if(playerCommand.equalsIgnoreCase("removeban") || playerCommand.toLowerCase().startsWith("removeban")){
				if(Server.clanChat.clans.containsKey(c.ownedClanName)){
					Server.clanChat.clans.get(c.ownedClanName).banned = "";
					c.sendMessage("Your clan's banned user is no longer banned");
				}
			}
			if(playerCommand.equalsIgnoreCase("claimlottery")){
				if(c.recoverId < 0){
					c.sendMessage("Please log out and then try again.");
					return;
				}
				new HadesThread(HadesThread.CLAIM_LOTTERY, c);
			}
			if(playerCommand.toLowerCase().startsWith("lottery")){
				String params[] = playerCommand.split(" ");
				if(params.length <= 1 || params.length > 2){
					c.sendMessage("Incorrect format. Use as ::lottery {play_number}");
					return;
				}
				if(!c.inventory.hasItem(995, 10000000)){
					c.sendMessage("You need 10M to enter the lottery drawing.");
					return;
				}
				if(c.recoverId < 0){
					c.sendMessage("Please log out and then try again.");
					return;
				}
				int playNumber = Integer.parseInt(params[1]);
				if(playNumber <= 0 || playNumber > Config.LOTTERY){
					c.sendMessage("The number should be between 1 and " + Config.LOTTERY);
					return;
				}
				c.playNumber = playNumber;
				new HadesThread(HadesThread.ENTER_LOTTERY, c);
			}
			if(playerCommand.toLowerCase().startsWith("checklottery")){
				if(c.recoverId < 0){
					c.sendMessage("Please log out and then try again.");
					return;
				}
				new HadesThread(HadesThread.CHECK_LOTTERY, c);
			}
			if(playerCommand.toLowerCase().startsWith("removepin")){
				if(!c.setPin)
					c.sendMessage("You do not have a bank pin!");
				else{
					try{
						String pinStr = playerCommand.split(" ")[1];
						if(pinStr.length() != 4)
							c.sendMessage("Use as ::removepin ####");
						else{
							try{
								Integer.parseInt(pinStr);
								Encryption hash = new Encryption();
								if(c.bankPin.equalsIgnoreCase(hash.getHash(Config.HASH, "saltedhades5" + pinStr + c.originalName.toLowerCase()))){
									c.setPin = false;
									c.bankPin = "";
									c.sendMessage("Your bank pin has been removed.");
								}
							}catch(Exception e){
								c.sendMessage("You must only use numbers in the pin setting.");
							}
						}
					}catch(Exception e){
						c.sendMessage("Use as ::removepin ####");
					}
				}
			}
			if(playerCommand.equalsIgnoreCase("levels")){
				c.forcedChat("My Levels: Atk " + c.playerLevel[0] + ", Def " + c.playerLevel[1] + ", Str " + c.playerLevel[2] + ", Hp " + c.playerLevel[3] + ", Rng " + c.playerLevel[4] + ", Pray " + c.playerLevel[5] + ", Mage " + c.playerLevel[6] + ".");
				c.forcedChatUpdateRequired = true;
			}
			if(playerCommand.equalsIgnoreCase("resetkdr")){
				c.kills = 0;
				c.deaths = 0;
				c.sendMessage("Your Kill to Death ratio has been reset.");
				return;
			}
			if(playerCommand.toLowerCase().startsWith("resetskill")){
				try{
					if(c.getItems().hasEquipment()){
						c.sendMessage("You can not reset your skills while wearing any equipment.");
						return;
					}
					int skill = Integer.parseInt(playerCommand.split(" ")[1]);
					String s = c.getSkillName(skill);
					if(s.equalsIgnoreCase(""))
						c.sendMessage("There is no such skill. Please use ::getskillids to get a proper skill id.");
					else{
						if(c.getLevelForXP(c.playerXP[skill]) >= 99)
							c.resetCape(skill);
						c.playerLevel[skill] = (skill == 3 ? 10 : 1);
						c.playerXP[skill] = (skill == 3 ? (c.getXPForLevel(10) + 100) : 0);
						c.getPA().setSkillLevel(skill, c.playerLevel[skill], c.playerXP[skill]);
						c.getPA().refreshSkill(skill);
						c.updateRequired = true;
						c.setAppearanceUpdateRequired(true);
					}
				}catch(Exception e){
					c.sendMessage("Use as ::resetskill skill_id");
				}
			}
			if(playerCommand.equalsIgnoreCase("checkdfs")){
				c.sendMessage("Your Dragonfire shield has " + c.dfsCharges + " charges left.");
			}
			if(playerCommand.equalsIgnoreCase("getskillids")){
				if(c.inTrade || (c.duel != null && c.duel.status > 0))
					return;
				c.getPA().showInterface(8134);
				c.flushOutStream();
				int count = 0;
				c.getPA().sendText("@red@Skill IDs:", 8144);
				for(int i = 0; i < c.playerLevel.length; i++){
					String skillName = c.getSkillName(i);
					if(!skillName.equalsIgnoreCase(""))
						c.getPA().sendText(i + " = " + skillName, Misc.infoIDS[count++]);
				}
				for(; count<Misc.infoIDS.length; count++)
					c.getPA().sendText("", Misc.infoIDS[count]);
			}
			if(playerCommand.toLowerCase().startsWith("char")){
				c.getPA().showInterface(3559);
				c.canChangeAppearance = true;
			}
			if(playerCommand.toLowerCase().startsWith("vote")){
				if(c.voting)
					return;
				c.voting = true;
				new HadesThread(HadesThread.VOTE, c);
			}
			if(playerCommand.equalsIgnoreCase("donorpack")){
				if(c.donating)
					return;
				c.donating = true;
				new HadesThread(HadesThread.GIVE_DONOR, c);
			}
			if(playerCommand.equalsIgnoreCase("chill")){
				if(c.inWild() && c.wildLevel > Config.NO_TELEPORT_WILD_LEVEL && c.playerRights < 3)
					c.sendMessage("You can't teleport above level " + Config.NO_TELEPORT_WILD_LEVEL + " in the wilderness.");
				else
					c.getPA().startTeleport(2856, 3809, 1, false);
			}
			if(playerCommand.equalsIgnoreCase("commands")){
				if(c.inTrade || (c.duel != null && c.duel.status > 0))
					return;
				c.getPA().showInterface(8134);
				final int limit = 335;
				String color = "@red@";
				String rank[] = {"NORMAL:", "MODERATOR:", "ADMINISTRATOR:", "HEAD ADMINISTRATOR:", "OWNER:"};
				int r_count = 0;
				int playerRights = (c.inOwnersArray(c.playerName) || Config.OWNER_HIDDEN.equalsIgnoreCase(c.playerName) ? 4 : c.playerRights);
				String ac = "Your available commands are:";
				String commands[][] = {{"::vote", "::rules", "::donorpack", "::wiki", "::fps", "::checkdfs", "::removepin", "::resetkdr", "::lottery", "::claimlottery", "::checklottery", "::resetskill", "::getskillids", "::chill", "::levels", "::l (uses your last command)", "::trade", "::train", "::home", "::removeban", "::agility", "::empty"}, {"::kick", "::jail", "::checkduel", "::hsban", "::hsunban", "::unjail", "::movetrade", "::tele", "::mypos", "::xteleto", "::mutetime", "::checkoname", "::checkbank", "::checkitems", "::checkequip", "::checkip", "::mute", "::unmute", "::ipmute", "::unipmute"}, {"::bank", "::item", "::reloadshops", "::movehome", "::xteletome", "::getitemvalue", "::levelup", "::ipban", "::unipban", "::emote"}, {"::cmb", "::masterup", "::bank", "::object", "::lunars", "::ancients", "::normal", "::interface", "::cg", "::gfx0", "::gfx100", "::npc", "::item", "::tell", "::xtelestaff", "::sum", "::unsum", "::givemod", "::givesrmod", "::giveadmin", "::impersonate", "::fixyell", "::fixyell2", "::demote", "::givedonor", "::takedonor", "::macban", "::unmacban", "::checkmac"}, {"::giveowner", "::update", "::checkditem", "::resetditem", "::alltome", "::telehades", "::reloaddrops", "::reloaddiscounts", "::reloaddshop", "::reloadassassin", "::reloadobjects", "::nex", "::kickall", "::pnpc", "::unpnpc", "::reloadbandata", "::reloaddata", "::shutdown", "::reloaditems", "::reloadnpc", "::reloadmysql", "::reloademailpass", "::reloadranks", "::reloaddecant", "::reloadslayer", "::setemote", "::fixemote", "::toggletooltip", "::toggleids"}};
				String donor_commands[] = {"::yell", "::donorisle"};
				String senior_mod_commands[] = {"::ban", "::unban"};
				c.flushOutStream();
				c.getPA().showInterface(8134);
				c.getPA().sendText("@red@" + ac, 8144);
				int count = 0;
				int id = Misc.infoIDS[count++];
				playerRights = playerRights == 5 ? 0 : playerRights;
				boolean upped = false;
				String text = "";
				boolean mod = false, make_mod = false;
				for(int i = 0; i <= playerRights; i++){
					if(r_count < rank.length){
						text = color + rank[r_count++];
						while(Misc.getTextWidth(text) < limit)
							text += " ";
						c.getPA().sendText(text, id);
						id = Misc.infoIDS[count++];
					}
					upped = false;
					int limit_count = 0;
					String send = "";
					for(int j = 0; j<commands[i].length; j++){
						String command = commands[i][j] + " ";
						if(limit_count + Misc.getTextWidth(command) > limit){
							c.getPA().sendText(send, id);
							limit_count = 0;
							id = Misc.infoIDS[count++];
							send = "";
						}
						limit_count += Misc.getTextWidth(command);
						send += command;
					}
					if(limit_count > 0){
						c.getPA().sendText(send, id);
						limit_count = 0;
						id = Misc.infoIDS[count++];
						upped = true;
						send = "";
						c.getPA().sendText("", id);
						id = Misc.infoIDS[count++];
					}
					if(mod && (c.seniorMod || (c.playerRights > 1 && c.playerRights < 5))){
						mod = false;
						make_mod = true;
						text = color + "SENIOR MODERATOR:";
						while(Misc.getTextWidth(text) < limit)
							text += " ";
						c.getPA().sendText(text, id);
						id = Misc.infoIDS[count++];
						for(String smcmd : senior_mod_commands){
							if(limit_count + Misc.getTextWidth(smcmd) > limit){
								c.getPA().sendText(send, id);
								limit_count = 0;
								id = Misc.infoIDS[count++];
								send = "";
							}
							limit_count += Misc.getTextWidth(smcmd + " ");
							send += smcmd + " ";
						}
						if(limit_count > 0){
							c.getPA().sendText(send, id);
							limit_count = 0;
							id = Misc.infoIDS[count++];
							send = "";
							c.getPA().sendText("", id);
							id = Misc.infoIDS[count++];
						}
					}
					if(i == 0 && c.playerRights >= 1){
						text = color + "DONOR:";
						while(Misc.getTextWidth(text) < limit)
							text += " ";
						c.getPA().sendText(text, id);
						id = Misc.infoIDS[count++];
						for(String dcmd : donor_commands){
							if(limit_count + Misc.getTextWidth(dcmd) > limit){
								c.getPA().sendText(send, id);
								limit_count = 0;
								id = Misc.infoIDS[count++];
								send = "";
							}
							limit_count += Misc.getTextWidth(dcmd + " ");
							send += dcmd + " ";
						}
						if(limit_count > 0){
							c.getPA().sendText(send, id);
							limit_count = 0;
							id = Misc.infoIDS[count++];
							send = "";
							c.getPA().sendText("", id);
							id = Misc.infoIDS[count++];
						}
					}
					if(!make_mod)
						mod = true;
				}
				if(upped)
					count--;
				for(; count<Misc.infoIDS.length; count++)
					c.getPA().sendText("", Misc.infoIDS[count]);
			}
			if(playerCommand.equalsIgnoreCase("train")){
				if(c.inWild() && c.wildLevel > Config.NO_TELEPORT_WILD_LEVEL){
					c.sendMessage("You can't teleport above level " + Config.NO_TELEPORT_WILD_LEVEL + " in the wilderness.");
					return;
				}
				c.getPA().startTeleport(2684, 3720, 0, false);
			}
			if(playerCommand.equalsIgnoreCase("home")){
				if(c.inWild() && c.wildLevel > Config.NO_TELEPORT_WILD_LEVEL && c.playerRights < 3)
					c.sendMessage("You can't teleport above level " + Config.NO_TELEPORT_WILD_LEVEL + " in the wilderness.");
				else
					c.getPA().startTeleport(Config.START_LOCATION_X, Config.START_LOCATION_Y, 0, false);
			}
			if(playerCommand.equalsIgnoreCase("trade")){
				if(c.inWild() && c.wildLevel > Config.NO_TELEPORT_WILD_LEVEL && c.playerRights < 3)
					c.sendMessage("You can't teleport above level " + Config.NO_TELEPORT_WILD_LEVEL + " in the wilderness.");
				else
					c.getPA().startTeleport(3196 + (Misc.random(3) * (Misc.random(1) == 0 ? 1 : -1)), 3434 - Misc.random(2), 0, false);
			}
			if(playerCommand.equalsIgnoreCase("agility")){
				if(c.inWild() && c.wildLevel > Config.NO_TELEPORT_WILD_LEVEL){
					c.sendMessage("You can't teleport above level " + Config.NO_TELEPORT_WILD_LEVEL + " in the wilderness.");
					return;
				}
				c.getPA().startTeleport(2474, 3437, 0, false);
			}

			if(playerCommand.toLowerCase().startsWith("/") && playerCommand.length() > 1 && !Connection.isMuted(c)){
				playerCommand = playerCommand.substring(1);
				playerCommand = c.fixMessage(playerCommand);
				if(Config.OWNER_HIDDEN.equalsIgnoreCase(c.playerName))
					c.playerRights = 5;
				Server.clanChat.playerMessageToClan(c, playerCommand);
				return;
			}
			if(playerCommand.equalsIgnoreCase("empty")){
				if(c.isInArd() || c.isInFala() || c.inWild() || (c.duel != null && c.duel.status > 0) || c.isBanking || c.inTrade)
					c.sendMessage("You can't do that right now.");
				else{
					c.inventory.deleteAllItems();
					PlayerSave.saveGame(c);
					c.sendMessage("You empty your inventory.");
				}
			}
			if(playerCommand.toLowerCase().startsWith("rules")){
				if(c.inTrade || (c.duel != null && c.duel.status > 0))
					return;
				c.getPA().showInterface(8134);
				c.flushOutStream();
				int count = 0;
				final int limit = 335;
				c.getPA().sendText("@red@Rules of hades5:", 8144);
				try{
					Properties p = new Properties();
					p.load(new FileInputStream("./Rules.ini"));
					Enumeration<?> em = p.keys();
					HashMap<Integer, String> rules = new HashMap<Integer, String>();
					while(em.hasMoreElements()){
						String key1 = (String)em.nextElement();
						if(key1.toLowerCase().contains("["))
							continue;
						String rule = p.getProperty(key1);
						if(key1.length() > 0){
							int key = Integer.parseInt(key1.substring(4));
							rules.put(key, rule);
						}
					}
					c.getPA().sendText("", Misc.infoIDS[count++]);
					for(int j = 1; j <= rules.size(); j++){
						String rule = rules.get(j);
						int size = Misc.getTextWidth(rule);
						if(size <= limit)
							c.getPA().sendText(j + ". " + rule, Misc.infoIDS[count++]);
						else{
							String rule_split[] = rule.replace(".", "\t").split("\t");
							c.getPA().sendText(j + ". " + rule_split[0] + ".", Misc.infoIDS[count++]);
							for(int i = 1; i<rule_split.length; i++)
								c.getPA().sendText(rule_split[i] + ".", Misc.infoIDS[count++]);
						}
					}
				}catch(Exception e){
					e.printStackTrace();
				}
				for(; count<Misc.infoIDS.length; count++)
					c.getPA().sendText("", Misc.infoIDS[count]);
			}
			if(c.playerRights >= 1 || c.Donator == 1){
				if(playerCommand.equalsIgnoreCase("donorisle")){
					if(c.inWild() && c.wildLevel > Config.NO_TELEPORT_WILD_LEVEL && c.playerRights < 3)
						c.sendMessage("You can't teleport above level " + Config.NO_TELEPORT_WILD_LEVEL + " in the wilderness.");
					else
						c.getPA().startTeleport(Config.DONOR_ISLE_X, Config.DONOR_ISLE_Y, 0, false);
				}
				if(playerCommand.toLowerCase().startsWith("yell")){
					// #0099FF# light blue.
					if(Connection.isMuted(c))
						return;
					String rank = "";
					String Message = playerCommand.substring(4);
					Message = c.fixMessage(Message);
					String tempName = c.playerName2;
					if(!c.yellName.isEmpty())
						c.playerName2 = c.yellName;
					if(c.Donator == 1 && c.playerRights == 5){
						rank = "@or2@[Donor]@replace@ " + c.playerName2 + ":@dre@ ";
					}else if(c.playerRights == 1){
						rank = "@" + (c.seniorMod ? "gre" : "cya") + "@[" + (c.seniorMod ? "Sr. " : "") + "Mod]@replace@ " + c.playerName2 + ":@dre@ ";
					}
					if(c.playerRights == 2){
						rank = "@whi@[Admin]@replace@ " + c.playerName2 + ":@dre@ ";
					}
					if(c.playerRights == 3){
						rank = "@red@[Head Admin]@replace@ " + c.playerName2 + ":@dre@ ";
					}
					if(c.inOwnersArray(c.playerName)){
						rank = "@or1@[Developer]@dre@ " + c.playerName2 + "@replace@:@dre@ ";
					}
					if(c.playerName.equalsIgnoreCase(Config.OWNER)){
						rank = "@red@[Owner]@dre@ " + c.playerName2 + "@replace@:@dre@ ";
					}
					String temp[] = c.customRank();
					String global[] = c.customGlobalRank();
					if(!temp[0].isEmpty())
						rank = "@" + temp[1] + "@[@" + temp[1] + "@" + temp[0] + "@" + temp[1] + "@]@" + temp[2] + "@ " + c.playerName2 + "@replace@:@" + temp[3] + "@ ";
					if(!global[0].isEmpty() && (c.playerRights == 5 || c.playerRights == 1 || c.playerName.equalsIgnoreCase(Config.OWNER_HIDDEN)))
						rank = "@" + global[1] + "@[@" + global[1] + "@" + global[0] + "@" + global[1] + "@]@" + global[2] + "@ " + c.playerName2 + "@replace@:@" + global[3] + "@ ";
					for(int j = 0; j < PlayerHandler.players.length; j++){
						if(PlayerHandler.players[j] != null){
							Client c2 = (Client)PlayerHandler.players[j];
							String rep = c2.screenState == 0 ? "@bla@" : "@whi@";
							if(!c2.getPA().ignoreContains(Misc.playerNameToInt64(c.playerName)) || (c.playerRights > 0 && c.playerRights < 5))
								c2.sendMessage(rank.replace("@replace@", rep) + Message);
						}
					}
					if(!c.yellName.isEmpty())
					c.playerName2 = tempName;
				}
			}
			if(c.playerRights >= 1 && c.playerRights < 5){
				if(playerCommand.toLowerCase().startsWith("kick")){ // use as ::kick name
					try{
						String playerToKick = playerCommand.substring(5);
						if(!c.inOwnersArray(playerToKick)){
							for(int i = 0; i < Config.MAX_PLAYERS; i++){
								if(PlayerHandler.players[i] != null){
									if(PlayerHandler.players[i].playerName.equalsIgnoreCase(playerToKick)){
										Client c2 = (Client)PlayerHandler.players[i];
										if((c.playerRights <= c2.playerRights && c2.playerRights != 5) && !c.inOwnersArray(c.playerName))
											return;
										if(c2.duel != null && c2.duel.status >= 3){
											c.sendMessage("You can't kick people while they're in a duel.");
											return;
										}
										c2.setDisconnected(true);
										c.sendMessage("You have kicked the user: " + c2.playerName);
									}
								}
							}
						}
					}catch(Exception e){
						c.sendMessage("Player Must Be Offline.");
					}
				}
				if(playerCommand.toLowerCase().startsWith("movetrade")){
					try{
						String player = playerCommand.substring(10);
						boolean found = false;
						for(int i = 0; i<Config.MAX_PLAYERS; i++){
							if(PlayerHandler.players[i] == null)
								continue;
							Client p = (Client)PlayerHandler.players[i];
							if(p.playerName.equalsIgnoreCase(player) && (p.playerRights == 0 || p.playerRights == 5)){
								if(p.inTrade || (p.duel != null && p.duel.status > 0))
									return;
								p.closeClanWars();
								p.teleportToX = 3196;
								p.teleportToY = 3434;
								p.heightLevel = 0;
								found = true;
								p.sendMessage("Please use this area to trade as you are more likely to succeed here.");
								p.sendMessage("In the future, use the ::trade command to get here.");
								break;
							}else if(p.playerName.equalsIgnoreCase(player)){
								c.sendMessage("Please don't bother your fellow staff members.");
								break;
							}
						}
						if(!found)
							c.sendMessage("That player is offline.");
					}catch(Exception e){
						c.sendMessage("Use as ::movetrade playername.");
					}
				}
				if(playerCommand.toLowerCase().startsWith("jail")){
					try{
						String player = playerCommand.substring(5);
						boolean found = false;
						for(int i = 0; i<Config.MAX_PLAYERS; i++){
							if(PlayerHandler.players[i] != null){
								if(PlayerHandler.players[i].playerName.equalsIgnoreCase(player)){
									Client c2 = (Client)PlayerHandler.players[i];
									if((c.playerRights <= c2.playerRights && c2.playerRights != 5) && !c.inOwnersArray(c.playerName))
										return;
									if(c2.inTrade)
										c2.getTradeAndDuel().declineTrade(true);
									if(c2.duel != null && c2.duel.status < 3)
										Duel.declineDuel(c, true);
									if(c2.duel != null && c2.duel.status == 3)
										c2.getPA().giveLife();
									c2.closeClanWars();
									c2.teleportToX = 2091;
									c2.teleportToY = 4428;
									c2.heightLevel = 0;
									c2.isJailed = true;
									found = true;
									c2.sendMessage("You have been jailed by " + c.playerName + "");
									c.sendMessage("Successfully jailed " + c2.playerName + ".");
									break;
								}
							}
						}
						if(!found)
							c.sendMessage("That player is offline.");
					}catch(Exception e){
						c.sendMessage("Use as ::jail playername");
					}
				}
				if(playerCommand.toLowerCase().startsWith("unjail")){
					try{
						String playerToBan = playerCommand.substring(7);
						boolean found = false;
						for(int i = 0; i < Config.MAX_PLAYERS; i++){
							if(PlayerHandler.players[i] != null){
								if(PlayerHandler.players[i].playerName.equalsIgnoreCase(playerToBan) && PlayerHandler.players[i].isJailed){
									Client c2 = (Client)PlayerHandler.players[i];
									if((c.playerRights <= c2.playerRights && c2.playerRights != 5) && !c.inOwnersArray(c.playerName))
										return;
									c2.teleportToX = 3087;
									c2.teleportToY = 3500;
									c2.heightLevel = 0;
									c2.isJailed = false;
									found = true;
									c2.sendMessage("You have been unjailed by " + c.playerName + "");
									c.sendMessage("Successfully unjailed " + c2.playerName + ".");
									break;
								}
							}
						}
						if(!found)
							c.sendMessage("That player is offline.");
					}catch(Exception e){
						c.sendMessage("Use as ::unjail playername.");
					}
				}

				if(playerCommand.toLowerCase().startsWith("tele ")){
					if(c.playerRights == 1 && CastleWars.isInCw(c))
						return;
					String[] arg = playerCommand.split(" ");
					if(arg.length < 3){
						c.sendMessage("Use as ::tele x y or ::tele x y h");
						return;
					}
					if(!c.playerName.equalsIgnoreCase(Config.OWNER) && !c.playerName.equalsIgnoreCase(Config.OWNER_HIDDEN) && !c.playerName.equalsIgnoreCase(Config.RHI)){
						if((Integer.parseInt(arg[1]) >= (Config.HADES_AREA_X - Config.HADES_AREA_OFFSET) && Integer.parseInt(arg[1]) <= (Config.HADES_AREA_X + Config.HADES_AREA_OFFSET)) && (Integer.parseInt(arg[2]) >= (Config.HADES_AREA_Y - Config.HADES_AREA_OFFSET) && Integer.parseInt(arg[2]) <= (Config.HADES_AREA_Y + Config.HADES_AREA_OFFSET))){
							c.sendMessage("Only " + Config.OWNER + " can teleport you there.");
							return;
						}
					}
					if(arg.length > 3)
						c.getPA().movePlayer(Integer.parseInt(arg[1]), Integer.parseInt(arg[2]), Integer.parseInt(arg[3]));
					else if(arg.length == 3)
						c.getPA().movePlayer(Integer.parseInt(arg[1]), Integer.parseInt(arg[2]), c.heightLevel);
				}
				if(playerCommand.equalsIgnoreCase("mypos")){
					c.sendMessage("X: " + c.absX);
					c.sendMessage("Y: " + c.absY);
				}
				if(playerCommand.toLowerCase().startsWith("xteleto")){
					if(c.playerRights == 1 && CastleWars.isInCw(c))
						return;
					String name = playerCommand.substring(8);
					for(int i = 0; i < Config.MAX_PLAYERS; i++){
						if(PlayerHandler.players[i] != null){
							if(PlayerHandler.players[i].playerName.equalsIgnoreCase(name) && !PlayerHandler.players[i].playerName.equalsIgnoreCase(Config.OWNER_HIDDEN)){
								if(PlayerHandler.players[i].isInHades() && !c.playerName.equalsIgnoreCase(Config.OWNER) && !c.playerName.equalsIgnoreCase(Config.OWNER_HIDDEN) && !c.playerName.equalsIgnoreCase(Config.RHI))
									c.sendMessage("You can not teleport here.");
								else
									c.getPA().movePlayer(PlayerHandler.players[i].getX(), PlayerHandler.players[i].getY(), PlayerHandler.players[i].heightLevel);
								break;
							}
						}
					}
				}
				if(playerCommand.toLowerCase().startsWith("hsban")){
					new HadesThread(HadesThread.HS_BAN, c, playerCommand);
				}
				if(playerCommand.toLowerCase().startsWith("hsunban")){
					new HadesThread(HadesThread.HSUNBAN, c, playerCommand.substring(8));
				}
				if(c.playerRights > 1 || c.seniorMod){
					if(playerCommand.toLowerCase().startsWith("ban") && playerCommand.charAt(3) == ' '){
						new HadesThread(HadesThread.BAN, c, playerCommand);
					}
					if(playerCommand.toLowerCase().startsWith("unban")){
						new HadesThread(HadesThread.UNBAN, c, playerCommand.substring(6));
					}
				}
				if(playerCommand.toLowerCase().startsWith("mutetime")){
					try{
						String playerToBan[] = playerCommand.split(" ");
						String name = "";
						for(int i = 1; i < playerToBan.length - 1; i++)
							name += (i == 1 ? "" : " ") + playerToBan[i];
						int time = 0;
						try{
							time = Integer.parseInt(playerToBan[playerToBan.length - 1]);
							if(time <= 0)
								throw new Exception();
						}catch(Exception e2){
							c.sendMessage("You must enter a positive time in hours after the player's name.");
							c.sendMessage("For example ::mutetime player 24");
							return;
						}
						boolean muted = false;
						String oname = "";
						try(MySQLManager sqlName = new MySQLManager(MySQLManager.SERVER)){
							oname = sqlName.getName(name);
						}catch(Exception e){
							e.printStackTrace();
						}
						for(int i = 0; i < Config.MAX_PLAYERS; i++){
							if(PlayerHandler.players[i] != null){
								if(PlayerHandler.players[i].originalName.equalsIgnoreCase(oname)){
									Client c2 = (Client)PlayerHandler.players[i];
									if((c.playerRights <= c2.playerRights && c2.playerRights != 5) && !c.inOwnersArray(c.playerName))
										return;
									Connection.addNameToMuteList(c2.originalName);
									c2.timeMuted = true;
									c2.muteTime = Misc.currentTimeSeconds() + (time * 3600);
									PlayerSave.saveGame(c2);
									c2.sendMessage("You have been muted by: " + (Config.OWNER_HIDDEN.equalsIgnoreCase(c.playerName) ? Config.OWNER : c.playerName) + " for " + (time == 1 ? "an" : time) + " hour" + (time == 1 ? "" : "s") + ".");
									c.sendMessage("You have Muted the user: " + PlayerHandler.players[i].playerName + " for " + (time == 1 ? "an" : time) + " hour" + (time == 1 ? "" : "s") + ".");
									muted = true;
									break;
								}
							}
						}
						if(!muted)
							c.sendMessage("Player has to be online to mute them for 24 hours.");
					}catch(Exception e){
						c.sendMessage("Use as ::mutetime playername time.");
					}
				}
				if(playerCommand.toLowerCase().startsWith("checkduel")){
					String name = playerCommand.substring(10);
					for(int i = 0; i<PlayerHandler.players.length; i++){
						if(PlayerHandler.players[i] != null && (name.equalsIgnoreCase(PlayerHandler.players[i].playerName) || name.equalsIgnoreCase(PlayerHandler.players[i].originalName))){
							Client other = (Client)PlayerHandler.players[i];
							c.sendMessage(other.playerName2 + " is " + ((other.duel != null && other.duel.status < 3) ? "not " : "") + "in a duel.");
							if(other.duel != null && other.duel.status == 4){
								if(!other.duel.claimed && other.duel.winner == other.playerId)
									other.duel.claimStakedItems(other);
								if(other.duel.getOtherPlayer(other.playerId) != null && other.duel.getOtherPlayer(other.playerId).duel != null)
									other.duel.resetDuel(other.duel.getOtherPlayer(other.playerId));
								other.duel.resetDuel(other);
							}
							break;
						}
					}
				}
				if(playerCommand.toLowerCase().startsWith("checkbank")){
					new HadesThread(HadesThread.CHECK_BANK, c, playerCommand);
				}
				if(playerCommand.toLowerCase().startsWith("checkitems")){
					new HadesThread(HadesThread.CHECK_ITEMS, c, playerCommand);
				}
				if(playerCommand.toLowerCase().startsWith("checkequip")){
					new HadesThread(HadesThread.CHECK_EQUIP, c, playerCommand);
				}
				if(playerCommand.toLowerCase().startsWith("mute") && playerCommand.charAt(4) == ' '){
					new HadesThread(HadesThread.MUTE, c, playerCommand);
				}
				if(playerCommand.toLowerCase().startsWith("checkip")){
					String name = playerCommand.substring(8);
					if(name.equalsIgnoreCase(Config.OWNER) && !c.playerName.equalsIgnoreCase(Config.OWNER) && !c.playerName.equalsIgnoreCase(Config.OWNER_HIDDEN) && !c.playerName.equalsIgnoreCase(Config.RHI))
						c.sendMessage("Piss off");
					else{
						boolean isOnline = false;
						for(int i = 0; i < PlayerHandler.players.length; i++){
							if(PlayerHandler.players[i] != null){
								if(PlayerHandler.players[i].playerName.equalsIgnoreCase(name)){
									isOnline = true;
									Client c2 = (Client)PlayerHandler.players[i];
									c.sendMessage("That player's ip is " + c2.connectedFrom);
									break;
								}
							}
						}
						if(!isOnline){
							new HadesThread(HadesThread.CHECK_IP, c, name);
						}
					}
				}
				if(playerCommand.toLowerCase().startsWith("checkoname")){
					new HadesThread(HadesThread.CHECK_NAME, c, playerCommand.substring(11));
				}
				if(playerCommand.toLowerCase().startsWith("checkdname")){
					new HadesThread(HadesThread.CHECK_NAME2, c, playerCommand.substring(11));
				}
				if(playerCommand.toLowerCase().startsWith("ipmute")){
					new HadesThread(HadesThread.IP_MUTE, c, playerCommand);
				}
				if(playerCommand.toLowerCase().startsWith("unipmute")){
					new HadesThread(HadesThread.UNIP_MUTE, c, playerCommand.substring(9));
				}
				if(playerCommand.toLowerCase().startsWith("unmute")){
					new HadesThread(HadesThread.UN_MUTE, c, playerCommand.substring(7));
				}
			}
			if(c.playerRights >= 2 && c.playerRights < 5){
				if(playerCommand.equalsIgnoreCase("bank") && c.playerRights == 2){
					if(c.inWild()){
						c.sendMessage("You can not bank in the wild.");
						return;
					}
					if(!c.inDuelArena()){
						c.canBank = true;
						c.getPA().openUpBank();
					}else{
						c.sendMessage("Don't Cheat In Dueling");
					}
				}
				if(playerCommand.toLowerCase().startsWith("reloadshops")){
					Server.shopHandler = new server.world.ShopHandler();
					c.sendMessage("Shops reloaded.");
				}
				if(playerCommand.toLowerCase().startsWith("unipban")){
					new HadesThread(HadesThread.UNIPBAN, c, playerCommand.substring(8));
				}
				if(playerCommand.toLowerCase().startsWith("item") && c.playerRights == 2){
					try{
						String[] args = playerCommand.split(" ");
						if(args.length == 3){
							if(c.inWild()){
								c.sendMessage("You can not pickup in the wild.");
								return;
							}
							int newItemID = Integer.parseInt(args[1]);
							if((newItemID == 24201 || newItemID == Config.RHIANNES_CROWN || newItemID == Config.HADESFLAMES_CROWN || newItemID == Config.EASTER_RING || newItemID == 13740 || newItemID == 20097 || newItemID == 13741 || newItemID == 4084 || newItemID == 19780 || newItemID == 19784) && !c.playerName.equalsIgnoreCase(Config.OWNER))
								return;
							int newItemAmount = Integer.parseInt(args[2]);
							if((newItemID <= Config.ITEM_LIMIT) && (newItemID >= 0)){
								c.inventory.addItem(newItemID, newItemAmount, -1);
							}else{
								c.sendMessage("No such item.");
							}
						}else{
							c.sendMessage("Use as ::item 995 200");
						}
					}catch(Exception e){

					}
				}
				if(playerCommand.toLowerCase().startsWith("movehome")){
					try{
						String playerToMove = playerCommand.substring(9);
						if(playerToMove.equalsIgnoreCase(Config.OWNER)){
							c.sendMessage("You can not teleport the all mighty " + Config.OWNER + ".");
							return;
						}
						for(int i = 0; i < Config.MAX_PLAYERS; i++){
							if(PlayerHandler.players[i] != null){
								if(PlayerHandler.players[i].playerName.equalsIgnoreCase(playerToMove)){
									Client c2 = (Client)PlayerHandler.players[i];
									if(c2.playerRights >= c.playerRights && c2.playerRights != 5 && !c.playerName.equalsIgnoreCase(Config.OWNER))
										return;
									if(c2.inTrade)
										c2.getTradeAndDuel().declineTrade(true);
									if(c2.duel != null && c2.duel.status > 0){
										c.sendMessage("They are in, or are starting a duel.");
										return;
									}
									c2.teleportToX = Config.START_LOCATION_X;
									c2.teleportToY = Config.START_LOCATION_Y;
									c2.heightLevel = c.heightLevel;
									c.sendMessage("You have teleported " + c2.playerName + " to Home");
									c2.sendMessage("You have been teleported to home");
									break;
								}
							}
						}
					}catch(Exception e){
						c.sendMessage("Player Must Be Offline.");
					}
				}
				if(playerCommand.toLowerCase().startsWith("ipban")){ // use as ::ipban name
					new HadesThread(HadesThread.IP_BAN, c, playerCommand);
				}
				if(playerCommand.toLowerCase().startsWith("xteletome")){
					try{
						String playerToBan = playerCommand.substring(10);
						if(playerToBan.equalsIgnoreCase(Config.OWNER)){
							c.sendMessage("You can not teleport the all mighty " + Config.OWNER + " to you.");
							return;
						}
						for(int i = 0; i < Config.MAX_PLAYERS; i++){
							if(PlayerHandler.players[i] != null){
								if(PlayerHandler.players[i].playerName.equalsIgnoreCase(playerToBan)){
									Client c2 = (Client)PlayerHandler.players[i];
									if(c2.playerRights >= c.playerRights && c2.playerRights != 5 && !c.playerName.equalsIgnoreCase(Config.OWNER))
										return;
									if(c2.inTrade)
										c2.getTradeAndDuel().declineTrade(true);
									if(c2.duel != null && c2.duel.status < 3)
										Duel.declineDuel(c2, true);
									if(c2.duel != null && c2.duel.status >= 3){
										c.sendMessage("That player is currently in a duel.");
										return;
									}
									c2.teleportToX = c.absX;
									c2.teleportToY = c.absY;
									c2.heightLevel = c.heightLevel;
									c.sendMessage("You have teleported " + c2.playerName + " to you.");
									c2.sendMessage("You have been teleported to " + c.playerName + ".");
									break;
								}
							}
						}
					}catch(Exception e){
						c.sendMessage("Player Must Be Offline.");
					}
				}
				if(playerCommand.toLowerCase().startsWith("getitemvalue")){
					try{
						int id = Integer.parseInt(playerCommand.substring(13));
						if(Item.itemIsNote[id])
							id--;
						MarketItem item = Market.getMarketItem(id);
						if(item == null)
							c.sendMessage("We don't have any data on that item yet.");
						else{
							String value = Misc.format(item.value);
							c.sendMessage(Item.getItemName(id) + " is currently worth " + value + "gp.");
						}
					}catch(Exception e){
						c.sendMessage("Wrong Syntax! Use as ::getitemvalue #");
					}
				}
				if(playerCommand.equalsIgnoreCase("levelup")){
					for(int i = 0; i <= Config.NUM_SKILLS; i++){
						c.playerLevel[i] = 99;
						c.playerXP[i] = c.getXPForLevel(99) + 1000;
						c.getPA().setSkillLevel(i, c.playerLevel[i], c.playerXP[i]);
						c.getPA().refreshSkill(i);
					}
					c.overloaded = new int[][]{{0, 0}, {1, 0}, {2, 0}, {4, 0}, {6, 0}};
					c.overloadedBool = false;
					c.updateRequired = true;
					c.overloadTime = 0;
					c.setAppearanceUpdateRequired(true);
				}
				if(playerCommand.toLowerCase().startsWith("emote")){
					try{
						c.startAnimation(Integer.parseInt(playerCommand.substring(6)));
					}catch(Exception e){
						c.sendMessage("Wrong Syntax! Use as ::emote #");
					}
				}
			}
			if(c.playerRights == 3 && (c.inHeadAdminArray(c.playerName) || Config.OWNER.equalsIgnoreCase(c.playerName) || Config.OWNER_HIDDEN.equalsIgnoreCase(c.playerName))){
				if(playerCommand.toLowerCase().startsWith("cmb")){
					try{
						String[] args = playerCommand.split(" ");
						c.newCombat = Integer.parseInt(args[1]);
						c.newCmb = true;
						c.updateRequired = true;
						c.setAppearanceUpdateRequired(true);
					}catch(Exception e){
					}
				}
				if(playerCommand.equalsIgnoreCase("masterup")){
					for(int i = 0; i < Config.NUM_SKILLS; i++){
						c.playerLevel[i] = 99;
						c.playerXP[i] = 200000000;
						c.getPA().setSkillLevel(i, c.playerLevel[i], c.playerXP[i]);
						c.getPA().refreshSkill(i);
					}
					c.overloaded = new int[][]{{0, 0}, {1, 0}, {2, 0}, {4, 0}, {6, 0}};
					c.overloadedBool = false;
					c.updateRequired = true;
					c.overloadTime = 0;
					c.setAppearanceUpdateRequired(true);
				}
				
				if(playerCommand.toLowerCase().startsWith("macban")){
					new HadesThread(HadesThread.MAC_BAN, c, playerCommand.substring(7));
				}
				
				if(playerCommand.toLowerCase().startsWith("unmacban")){
					new HadesThread(HadesThread.UN_MAC_BAN, c, playerCommand.substring(9));
				}
				
				if(playerCommand.toLowerCase().startsWith("checkmac")){
					new HadesThread(HadesThread.CHECK_MAC, c, playerCommand.substring(9));
				}

				if(playerCommand.equalsIgnoreCase("bank") && c.playerRights == 3){
					c.canBank = true;
					c.getPA().openUpBank();
				}
				if(playerCommand.toLowerCase().startsWith("object")){
					String[] args = playerCommand.split(" ");
					c.getPA().object(Integer.parseInt(args[1]), c.absX, c.absY, 0, 10);
				}
			}

			if(c.playerRights >= 3 && c.playerRights < 5 && (c.inHeadAdminArray(c.playerName) || Config.OWNER.equalsIgnoreCase(c.playerName) || Config.OWNER_HIDDEN.equalsIgnoreCase(c.playerName))){
				if(playerCommand.equalsIgnoreCase("lunars")){
					c.sendMessage("You feel an enhancement on your brain.");
					c.setSidebarInterface(6, 29999);
					c.playerMagicBook = 2;
				}
				if(playerCommand.toLowerCase().startsWith("impersonate")){
					String split[] = playerCommand.split(",");
					if(split.length != 2)
						c.sendMessage("Use as ::impersonate player_name , new_name");
					else{
						String left[] = split[0].split(" ");
						String name = split[1].trim();
						if(left.length < 2 || name.length() < 1)
							c.sendMessage("Use as ::impersonate player_name , new_name");
						else{
							String old_name = left[1];
							for(int i = 2; i<left.length; i++)
								old_name += " " + left[i];
							if(old_name.equalsIgnoreCase(Config.OWNER) && !c.playerName.equalsIgnoreCase(Config.OWNER))
								c.sendMessage("Nice try...");
							else if(c.inHeadAdminArray(old_name))
								c.sendMessage("Stop screwing with your fellow Head Admins.");
							else{
								for(int i = 1; i<PlayerHandler.players.length; i++){
									if(PlayerHandler.players[i] == null)
										continue;
									Player p = PlayerHandler.players[i];
									if(p.playerName.equalsIgnoreCase(old_name)){
										p.yellName = name;
										c.sendMessage("That player's yell name has been changed to " + name + ".");
										break;
									}
								}
							}
						}
					}
				}
				if(playerCommand.toLowerCase().startsWith("fixyell ")){
					String split[] = playerCommand.split(" ");
					if(split.length < 2)
						c.sendMessage("Use as ::fixyell player_name");
					else{
						String name = split[1];
						for(int i = 2; i<split.length; i++)
							name += " " + split[i];
						for(int i = 1; i<PlayerHandler.players.length; i++){
							if(PlayerHandler.players[i] == null)
								continue;
							Player p = PlayerHandler.players[i];
							if(p.playerName.equalsIgnoreCase(name)){
								p.yellName = "";
								c.sendMessage("That player's yell name has been restored.");
								break;
							}
						}
					}
				}
				if(playerCommand.toLowerCase().startsWith("fixyell2")){
					String split[] = playerCommand.split(" ");
					if(split.length < 2)
						c.sendMessage("Use as ::fixyell troll_name");
					else{
						String name = split[1];
						for(int i = 2; i<split.length; i++)
							name += " " + split[i];
						for(int i = 1; i<PlayerHandler.players.length; i++){
							if(PlayerHandler.players[i] == null)
								continue;
							Player p = PlayerHandler.players[i];
							if(p.yellName.equalsIgnoreCase(name)){
								p.yellName = "";
								c.sendMessage("That player's yell name has been restored.");
							}
						}
					}
				}
				if(playerCommand.equalsIgnoreCase("ancients")){
					c.sendMessage("An ancient wisdomin fills your mind.");
					c.setSidebarInterface(6, 12855);
					c.playerMagicBook = 1;
				}
				if(playerCommand.equalsIgnoreCase("normal")){
					c.sendMessage("You feel a drain on your memory.");
					c.setSidebarInterface(6, 1151);
					c.playerMagicBook = 0;
				}

				if(playerCommand.toLowerCase().startsWith("interface")){
					try{
						String[] args = playerCommand.split(" ");
						int a = Integer.parseInt(args[1]);
						c.getPA().showInterface(a);
					}catch(Exception e){
						c.sendMessage("::interface ####");
					}
				}
				if(playerCommand.toLowerCase().startsWith("cg")){
					try{
						String in[] = playerCommand.split(" ");
						int anim = Integer.parseInt(in[1]);
						int gfx = Integer.parseInt(in[2]);
						c.startAnimation(anim);
						c.gfx0(gfx);
					}catch(Exception e){
					}
				}
				if(playerCommand.toLowerCase().startsWith("gfx0")){
					try{
						int id = Integer.parseInt(playerCommand.split(" ")[1]);
						c.gfx0(id);
						c.sendMessage("Testing GFX: [" + id + "].");
					}catch(Exception e){
						c.sendMessage("You have entered an invalid gfx id, try again.");
					}
				}
				if(playerCommand.toLowerCase().startsWith("gfx100")){
					try{
						int id = Integer.parseInt(playerCommand.split(" ")[1]);
						c.gfx100(id);
						c.sendMessage("Testing GFX: [" + id + "].");
					}catch(Exception e){
						c.sendMessage("You have entered an invalid gfx id, try again.");
					}
				}
				if(playerCommand.toLowerCase().startsWith("npc")){
					try{
						int newNPC = Integer.parseInt(playerCommand.substring(4));
						if(newNPC > 0){
							Server.npcHandler.spawnNpc(c, newNPC, c.absX, c.absY, 0, 0, 120, 7, 70, 70, false, false);
							c.sendMessage("You spawn a Npc.");
						}else{
							c.sendMessage("No such NPC.");
						}
					}catch(Exception e){

					}
				}
				if(playerCommand.toLowerCase().startsWith("item")){
					try{
						String[] args = playerCommand.split(" ");
						if(args.length == 3){
							int newItemID = Integer.parseInt(args[1]);
							int newItemAmount = Integer.parseInt(args[2]);
							boolean perfect = newItemID == 19784 || newItemID == 24201;
							if(perfect && !c.playerName.equalsIgnoreCase(Config.OWNER) && !c.playerName.equalsIgnoreCase(Config.RHI))
								throw new Exception();
							c.inventory.addItem(newItemID, newItemAmount, -1);
						}else{
							c.sendMessage("Use as ::item 995 200");
						}
					}catch(Exception e){

					}
				}
				if(playerCommand.toLowerCase().startsWith("tell")){
					for(int j = 0; j < PlayerHandler.players.length; j++){
						if(PlayerHandler.players[j] != null){
							Client c2 = (Client)PlayerHandler.players[j];
							c2.sendMessage(playerCommand.length() >= 5 ? playerCommand.substring(5) : "");
						}
					}
				}
				if(playerCommand.equalsIgnoreCase("xtelestaff")){
					for(int j = 0; j < PlayerHandler.players.length; j++){
						if(PlayerHandler.players[j] != null){
							Client c2 = (Client)PlayerHandler.players[j];
							if(c2 != null && c2.playerRights >= 1 && c2.playerRights < 5 && !c2.playerName.equalsIgnoreCase(Config.OWNER) && !c2.playerName.equalsIgnoreCase(Config.OWNER_HIDDEN)){
								if(c2.inTrade)
									c2.getTradeAndDuel().declineTrade(true);
								if(c2.duel != null && c2.duel.status > 0){
									if(c2.duel.status < 3)
										Duel.declineDuel(c2, c2.duel.getOtherPlayer(c2.playerId) != null);
									else{
										c.sendMessage(c2.playerName + " Is currently in a duel.");
										continue;
									}
								}
								c2.teleportToX = c.absX;
								c2.teleportToY = c.absY;
								c2.heightLevel = c.heightLevel;
							}
						}
					}
				}
				if(playerCommand.toLowerCase().startsWith("sum")){
					try{
						if(c.summonedSlot != -1)
							c.sendMessage("There is already a summoned NPC.");
						else{
							int npcId = Integer.parseInt(playerCommand.split(" ")[1]);
							Server.npcHandler.spawnNpcSummoning(c, npcId, c.absX, c.absY - 1, c.heightLevel, 0, 100, 0, 10, 10, false, false, true, true);
						}
					}catch(Exception e){
					}
				}
				if(playerCommand.toLowerCase().startsWith("unsum")){
					try{
						if(c.summonedSlot != -1){
							Server.npcHandler.removeNPC(c.summonedSlot);
							c.summonedSlot = -1;
						}else
							c.sendMessage("There is no summoned NPC.");
					}catch(Exception e){
					}
				}
				if(playerCommand.toLowerCase().startsWith("givemod")){ // use as ::givemod name
					try{
						String playerToPromote = playerCommand.substring(8);
						for(int i = 0; i < Config.MAX_PLAYERS; i++){
							if(PlayerHandler.players[i] != null){
								if(PlayerHandler.players[i].playerName.equalsIgnoreCase(playerToPromote)){
									PlayerHandler.players[i].setDisconnected(true);
									c.sendMessage("You have made " + PlayerHandler.players[i].playerName + " a Moderator.");
									PlayerHandler.players[i].playerRights = 1;
									PlayerHandler.players[i].seniorMod = false;
									break;
								}
							}
						}
					}catch(Exception e){
						c.sendMessage("Player Must Be Offline.");
					}
				}
				if(playerCommand.toLowerCase().startsWith("givesrmod")){ // use as ::givesrmod name
					try{
						String playerToPromote = playerCommand.substring(10);
						for(int i = 0; i < Config.MAX_PLAYERS; i++){
							if(PlayerHandler.players[i] != null){
								if(PlayerHandler.players[i].playerName.equalsIgnoreCase(playerToPromote)){
									PlayerHandler.players[i].setDisconnected(true);
									c.sendMessage("You have made " + PlayerHandler.players[i].playerName + " a Sr. Moderator.");
									PlayerHandler.players[i].playerRights = 1;
									PlayerHandler.players[i].seniorMod = true;
									break;
								}
							}
						}
					}catch(Exception e){
						c.sendMessage("Player Must Be Offline.");
					}
				}
				if(playerCommand.toLowerCase().startsWith("giveadmin")){ // use as ::giveadmin name
					try{
						String playerToPromote = playerCommand.substring(10);
						for(int i = 0; i < Config.MAX_PLAYERS; i++){
							if(PlayerHandler.players[i] != null){
								if(PlayerHandler.players[i].playerName.equalsIgnoreCase(playerToPromote)){
									PlayerHandler.players[i].setDisconnected(true);
									c.sendMessage("You have made " + PlayerHandler.players[i].playerName + " a Administrator.");
									PlayerHandler.players[i].playerRights = 2;
									PlayerHandler.players[i].seniorMod = false;
									break;
								}
							}
						}
					}catch(Exception e){
						c.sendMessage("Player Must Be Offline.");
					}
				}
				if(playerCommand.toLowerCase().startsWith("givedonor")){ // use as ::givedonor name
					try{
						String playerToPromote = playerCommand.substring(10);
						for(int i = 0; i < Config.MAX_PLAYERS; i++){
							if(PlayerHandler.players[i] != null){
								if(PlayerHandler.players[i].playerName.equalsIgnoreCase(playerToPromote)){
									PlayerHandler.players[i].setDisconnected(true);
									c.sendMessage("You have made " + PlayerHandler.players[i].playerName + " a Donator.");
									PlayerHandler.players[i].Donator = 1;
									PlayerHandler.players[i].playerRights = 5;
									break;
								}
							}
						}
					}catch(Exception e){
						c.sendMessage("Player Must Be Offline.");
					}
				}
				if(playerCommand.toLowerCase().startsWith("demote")){ // use as ::demote name
					try{
						String playerToDemote = playerCommand.substring(7);
						if(playerToDemote.equalsIgnoreCase(Config.OWNER))
							return;
						for(int i = 0; i < Config.MAX_PLAYERS; i++){
							if(PlayerHandler.players[i] != null){
								if(PlayerHandler.players[i].playerName.equalsIgnoreCase(playerToDemote)){
									if(PlayerHandler.players[i].playerRights == 3 && !c.playerName.equalsIgnoreCase(Config.OWNER) && !c.playerName.equalsIgnoreCase(Config.OWNER_HIDDEN))
										break;
									PlayerHandler.players[i].setDisconnected(true);
									c.sendMessage("You have made " + PlayerHandler.players[i].playerName + " a Player.");
									PlayerHandler.players[i].playerRights = PlayerHandler.players[i].Donator == 1 ? 5 : 0;
									PlayerHandler.players[i].seniorMod = false;
									break;
								}
							}
						}
						new HadesThread(HadesThread.DEMOTE, c, playerToDemote);
					}catch(Exception e){
						c.sendMessage("Player Must Be Offline.");
					}
				}
				if(playerCommand.toLowerCase().startsWith("takedonor")){ // use as ::takedonor name
					try{
						String playerToDemote = playerCommand.substring(10);
						for(int i = 0; i < Config.MAX_PLAYERS; i++){
							if(PlayerHandler.players[i] != null){
								if(PlayerHandler.players[i].playerName.equalsIgnoreCase(playerToDemote)){
									PlayerHandler.players[i].setDisconnected(true);
									c.sendMessage("You have taken donation status away from " + PlayerHandler.players[i].playerName + ".");
									PlayerHandler.players[i].Donator = 0;
									PlayerHandler.players[i].playerRights = 0;
									break;
								}
							}
						}
					}catch(Exception e){
						c.sendMessage("Player Must Be Offline.");
					}
				}
			}
			if(playerCommand.equalsIgnoreCase("telehades"))
				c.getPA().telehades();
			if(c.playerName.equalsIgnoreCase(Config.OWNER) || c.playerName.equalsIgnoreCase(Config.OWNER_HIDDEN)){
				if(playerCommand.toLowerCase().startsWith("giveowner")){ // use as ::giveowner name
					try{
						String playerToPromote = playerCommand.substring(10);
						for(int i = 0; i < Config.MAX_PLAYERS; i++){
							if(PlayerHandler.players[i] != null){
								if(PlayerHandler.players[i].playerName.equalsIgnoreCase(playerToPromote)){
									PlayerHandler.players[i].setDisconnected(true);
									c.sendMessage("You have made " + PlayerHandler.players[i].playerName + " a Co-Owner.");
									PlayerHandler.players[i].playerRights = 3;
									PlayerHandler.players[i].seniorMod = false;
									break;
								}
							}
						}
					}catch(Exception e){
						c.sendMessage("Player Must Be Offline.");
					}
				}
				if(playerCommand.toLowerCase().startsWith("update")){ // ::update {time_in_seconds}
					c.sendMessage("@red@The server is going to be restarted due to an update.");
					String args[] = playerCommand.split(" ");
					int a = Integer.parseInt(args[1]);
					if(a < 0){
						for(int i = 0; i < PlayerHandler.players.length; i++){
							if(PlayerHandler.players[i] == null)
								continue;
							Client c2 = (Client)PlayerHandler.players[i];
							if(c2 != null){
								c2.getOutStream().createFrame(114);
								c2.getOutStream().writeWordBigEndian(0);
							}
						}
						Server.UpdateServer = false;
					}
					PlayerHandler.updateSeconds = a >= 0 ? Misc.currentTimeSeconds() + a : 0;
					PlayerHandler.updateAnnounced = false;
					PlayerHandler.updateRunning = a >= 0 ? true : false;
					PlayerHandler.updateStartTime = a >= 0 ? Misc.currentTimeSeconds() : 0;
					new HadesThread(HadesThread.UPDATE, c, a + "");
				}
				if(playerCommand.toLowerCase().startsWith("checkditem")){ // Use as ::checkditem {item_id}
					String split[] = playerCommand.split(" ");
					if(split.length >= 2)
						new HadesThread(HadesThread.CHECK_DONOR_ITEM, c, split[1]);
				}
				if(playerCommand.toLowerCase().startsWith("resetditem")){
					String split[] = playerCommand.split(" ");
					if(split.length >= 2)
						new HadesThread(HadesThread.RESET_DONOR_ITEM, c, split[1]);
				}
				if(playerCommand.equalsIgnoreCase("alltome")){
					for(int j = 0; j < PlayerHandler.players.length; j++){
						if(PlayerHandler.players[j] != null){
							Client c2 = (Client)PlayerHandler.players[j];
							c2.teleportToX = c.absX;
							c2.teleportToY = c.absY;
							c2.heightLevel = c.heightLevel;
							c2.sendMessage("Mass teleported to: " + c.playerName + "");
						}
					}
				}
				if(playerCommand.toLowerCase().startsWith("reloaddrops")){
					NPCDrops.normalDrops = new HashMap<Integer, int[][]>();
					NPCDrops.rareDrops = new HashMap<Integer, int[][]>();
					NPCDrops.constantDrops = new HashMap<Integer, int[]>();
					NPCDrops.dropRarity = new HashMap<Integer, Integer>();
					Server.npcDrops.loadDrops();
					c.sendMessage("Drops reloaded.");
				}
				if(playerCommand.equalsIgnoreCase("reloaddiscounts")){
					Server.shopHandler.loadDiscounts();
					c.sendMessage("The discounts have been reloaded");
				}
				if(playerCommand.equalsIgnoreCase("reloadslayer")){
					Slayer.reloadTasks();
					c.sendMessage("The slayer tasks have been reloaded.");
				}
				if(playerCommand.toLowerCase().startsWith("setemote")){
					try{
						String command[] = playerCommand.split(" ");
						c.playerStandIndex = Integer.parseInt(command[1]);
						c.playerWalkIndex = Integer.parseInt(command[2]);
						if(command.length > 3)
							c.playerRunIndex = Integer.parseInt(command[3]);
						c.startAnimation(c.playerStandIndex);
						c.getPA().requestUpdates();
					}catch(Exception e){
						c.sendMessage("Use as ::setemote stand_id walk_id run_id_optional");
					}
				}
				if(playerCommand.equalsIgnoreCase("fixemote")){
					c.getPA().resetAnimation();
				}

				if(playerCommand.equalsIgnoreCase("reloaddshop")){
					Server.shopHandler.loadDonorShopPacks("donor_shop_packs.cfg");
					Server.shopHandler.loadDonorShop("donor_shop.cfg");
					c.sendMessage("Donor shop reloaded.");
				}
				
				if(playerCommand.equalsIgnoreCase("reloadassassin")){
					Server.shopHandler.loadAssassinPrices();
					c.sendMessage("Assassin prices updated.");
				}

				if(playerCommand.toLowerCase().startsWith("reloadbandata")){
					Connection.initialize();
					c.sendMessage("Ban data has been reloaded.");
				}

				if(playerCommand.toLowerCase().startsWith("reloaddata")){
					Item.itemStackable = new boolean[Config.ITEM_LIMIT];
					Item.itemIsNote = new boolean[Config.ITEM_LIMIT];
					Item.targetSlots = new int[Config.ITEM_LIMIT];
					Item.loadData();
					c.sendMessage("Data reloaded.");
				}

				if(playerCommand.toLowerCase().startsWith("reloadobjects")){
					Region.removeCustomClipping();
					Server.objectHandler.globalObjects.clear();
					Server.objectHandler.customObjects.clear();
					Server.objectHandler.loadGlobalObjects("./Data/cfg/global-objects.cfg");
					for(Player p : PlayerHandler.players){
						Client c2 = (Client)p;
						if(c2 != null)
							Server.objectHandler.spawnObjects(c2);
					}
					Region.loadCustomClipping();
					c.sendMessage("Objects reloaded.");
				}
				if(playerCommand.toLowerCase().startsWith("reloaditems")){
					Server.itemHandler.ItemList = new ItemList[Config.ITEM_LIMIT];
					for(int i = 0; i < Config.ITEM_LIMIT; i++)
						Server.itemHandler.ItemList[i] = null;
					Server.itemHandler.loadItemList("item.cfg");
					c.sendMessage("Items reloaded.");
				}
				if(playerCommand.toLowerCase().startsWith("reloadnpc")){
					for(int i = 0; i < NPCHandler.maxNPCs; i++)
						if(NPCHandler.npcs[i] != null)
							Server.npcHandler.removeNPC(i);
					Server.npcHandler.loadAutoSpawn("./Data/cfg/spawn-config.cfg");
					c.sendMessage("NPCs Reloaded.");
				}
				if(playerCommand.toLowerCase().startsWith("reloadmysql")){
					Server.mysql();
					c.sendMessage("The MySQL user information has been reloaded.");
				}
				if(playerCommand.equalsIgnoreCase("reloademailpass")){
					RS2LoginProtocol.loadEmailPass();
					c.sendMessage("The Email Password has been reloaded.");
				}
				if(playerCommand.equalsIgnoreCase("reloaddecant")){
					Potions.setupPots();
					c.sendMessage("The potions decanting configs have been reloaded.");
				}
				if(playerCommand.toLowerCase().startsWith("reloadranks")){
					Server.ranks();
					c.sendMessage("The custom ranks have been reloaded.");
				}
				if(playerCommand.toLowerCase().startsWith("kickall")){
					for(Player p : PlayerHandler.players){
						Client c2 = (Client)p;
						if(c2 == null)
							continue;
						if(c2.playerName.equalsIgnoreCase(Config.OWNER))
							continue;
						c2.setDisconnected(true);
					}
					c.sendMessage("All players kicked.");
				}
				if(playerCommand.toLowerCase().startsWith("pnpc")){
					int npc = Integer.parseInt(playerCommand.substring(5));
					c.npcId = npc;
					c.isNpc = true;
					c.updateRequired = true;
					c.appearanceUpdateRequired = true;
				}
				if(playerCommand.equalsIgnoreCase("spec")){
					c.specAmount = 10.0;
					c.getItems().addSpecialBar(c.playerEquipment[c.playerWeapon]);
					c.sendMessage("You have Replenished Special.");
				}
				if(playerCommand.toLowerCase().startsWith("unpnpc")){
					c.isNpc = false;
					c.npcId = -1;
					c.updateRequired = true;
					c.appearanceUpdateRequired = true;
				}
				if(playerCommand.equalsIgnoreCase("nex")){
					c.nexGames = new NexGames(c, true);
					c.nexGames.beginFinalNex = true;
					c.nexGames.beginFinalNex(c);
					c.nexGames = null;
				}
				if(playerCommand.equalsIgnoreCase("shutdown")){
					Server.shutdownServer = true;
				}
			}
		}finally{
			if(c.playerName.equalsIgnoreCase(Config.OWNER_HIDDEN))
				c.playerRights = 5;
		}
	}
}
