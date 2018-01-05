package server.model.players;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import server.Config;
import server.Server;
import server.model.HadesThread;
import server.model.items.InventoryItem;
import server.model.items.bank.BankItem;
import server.model.items.bank.Tab;
import server.model.players.skills.SlayerTask;
import server.util.Misc;
import server.util.MySQLManager;
import server.world.Clan;

public class PlayerSave{
	/**
	 * Loading
	 **/
	public static String convertOverloadSave(int overload[][]){
		String ovl = "";
		for(int oLoad[] : overload){
			ovl += "," + oLoad[0] + "," + oLoad[1];
		}
		return ovl.substring(1);
	}

	public static int[][] convertOverloadLoad(String overload){
		int ovl[][] = new int[5][2];
		String temp[] = overload.split(",");
		int i = 0, j = 0;
		for(String t : temp){
			if(j == 0)
				ovl[i][j++] = Integer.parseInt(t);
			else
				ovl[i++][j--] = Integer.parseInt(t);
		}
		return ovl;
	}
	
	public static String[] HAD(int id){
		String ret[] = null;
		try(MySQLManager getHad = new MySQLManager(MySQLManager.SERVER)){
			ret = getHad.getHad(id);
		}catch(Exception e){
			e.printStackTrace();
		}
		return ret;
	}
	
	public static boolean[] ignoreLogin(int id){
		boolean ret[] = null;
		try(MySQLManager ignores = new MySQLManager(MySQLManager.SERVER)){
			ret = ignores.getLoginIgnores(id);
		}catch(Exception e){
			e.printStackTrace();
		}
		return ret;
	}

	public static int loadGame(Client p, String playerName, String playerPass){
		String line = "";
		String token = "";
		String token2 = "";
		String token3[] = new String[3];
		ArrayList<Integer> tabIds = new ArrayList<Integer>();
		boolean EndOfFile = false, wrongPass = false;
		int ReadMode = 0;
		File f = new File("./Data/characters/" + playerName + ".txt");
		if(!f.exists()){
			int ret = 0;
			try(MySQLManager cna = new MySQLManager(MySQLManager.SERVER)){
				if(!cna.checkNewAccount(p))
					ret = 22;
			}catch(Exception e){
				e.printStackTrace();
				return 22;
			}
			if(ret != 0)
				return ret;
			p.newPlayer = false;
			p.originalName = playerName;
			return 0;
		}
		try(BufferedReader characterfile = new BufferedReader(new FileReader(f))){
			line = characterfile.readLine();
			p.originalName = playerName;
			while(EndOfFile == false && line != null){
				line = line.trim();
				int spot = line.indexOf("=");
				if(spot > -1){
					token = line.substring(0, spot);
					token = token.trim();
					token2 = line.substring(spot + 1);
					token2 = token2.trim();
					token3 = token2.split("\t");
					switch(ReadMode){
						case 1:
							if(token.equals("character-password"))
								if(!playerPass.equals(token2))
									wrongPass = true;
							break;
						case 2:
							if(token.equals("character-height")){
								p.heightLevel = Integer.parseInt(token2);
							}else if(token.equals("character-posx")){
								p.teleportToX = (Integer.parseInt(token2) <= 0 ? 3210 : Integer.parseInt(token2));
							}else if(token.equals("character-posy")){
								p.teleportToY = (Integer.parseInt(token2) <= 0 ? 3424 : Integer.parseInt(token2));
							}else if(token.equals("character-rights")){
								p.playerRights = Integer.parseInt(token2);
							}else if(token.equals("senior-mod")){
								p.seniorMod = Boolean.parseBoolean(token2);
							}else if(token.equals("in-castlewars")){
								p.inCwGame = Boolean.parseBoolean(token2);
								if(p.inCwGame)
									p.removeCWNeeded = true;
								p.inCwGame = false;
							}else if(token.equals("curses-active")){
								p.cursesActive = Boolean.parseBoolean(token2);
							}else if(token.equals("reset-pin-time")){
								p.resetPin = Long.parseLong(token2);
								if(p.resetPin <= Misc.currentTimeSeconds() && p.resetPin > 0)
									p.resetPinNow = true;
							}else if(token.equals("bankPin")){
								p.bankPin = token2;
							}else if(token.equals("last-conversion")){
								p.lastConversion = Integer.parseInt(token2);
							}else if(token.equalsIgnoreCase("public-chat")){
								p.publicChat = Integer.parseInt(token2);
							}else if(token.equalsIgnoreCase("private-chat")){
								p.privateChat = Integer.parseInt(token2);
							}else if(token.equals("donation-points")){
								p.donationPoints = Integer.parseInt(token2);
							}else if(token.equals("vote-points")){
								p.votePoints = Integer.parseInt(token2);
							}else if(token.equals("vecna-time")){
								p.vecnaSkullTimer = Integer.parseInt(token2);
							}else if(token.equals("kills")){
								p.kills = Double.parseDouble(token2);
							}else if(token.equalsIgnoreCase("rejoin-clan")){
								p.rejoinClan = Boolean.parseBoolean(token2);
							}else if(token.equalsIgnoreCase("last-clan")){
								p.lastClan = token2;
							}else if(token.equals("deaths")){
								p.deaths = Double.parseDouble(token2);
							}else if(token.equals("ghost-event")){
								p.ghostEvent = Boolean.parseBoolean(token2);
							}else if(token.equals("pc-points")){
								p.pcPoints = Integer.parseInt(token2);
							}else if(token.equalsIgnoreCase("swap")){
								p.swap = Boolean.parseBoolean(token2);
							}else if(token.equalsIgnoreCase("brandNew")){
								p.brandNew = Boolean.parseBoolean(token2);
							}else if(token.equalsIgnoreCase("has-curses")){
								p.hasCurses = Boolean.parseBoolean(token2);
							}else if(token.equalsIgnoreCase("zombie-points")){
								p.zombiePoints = Integer.parseInt(token2);
							}else if(token.equals("setPin")){
								p.setPin = Boolean.parseBoolean(token2);
							}else if(token.equals("tutorial-progress")){
								p.tutorial = Integer.parseInt(token2);
							}else if(token.equals("highscores")){
								p.highscores = Integer.parseInt(token2);
							}else if(token.equals("clan-name")){
								p.ownedClanName = token2;
								if(Server.clanChat.clans.containsKey(p.ownedClanName)){
									Clan clan = Server.clanChat.clans.get(p.ownedClanName);
									if(!clan.members.containsKey(p.playerName) && clan.owner.equalsIgnoreCase(p.originalName)){
										String oldName = "";
										try(MySQLManager clanLoad = new MySQLManager(MySQLManager.SERVER)){
											oldName = clanLoad.getOldName(p.playerName2);
										}catch(Exception e){
											e.printStackTrace();
										}
										if(clan.members.containsKey(oldName)){
											clan.display = p.playerName;
											clan.members.put(p.playerName, clan.members.remove(oldName));
											if(Server.clanChat.lastSave <= Misc.currentTimeSeconds()){
												new HadesThread(HadesThread.SAVE_CLAN_INFO);
												Server.clanChat.lastSave = Misc.currentTimeSeconds() + 60;
											}
										}
									}
								}
							}else if(token.equals("recover-id")){
								p.recoverId = Integer.parseInt(token2);
								String check[] = {"", ""};
								try(MySQLManager load = new MySQLManager(MySQLManager.SERVER)){
									check = load.checkLoadPass(p);
								}catch(Exception e){
									e.printStackTrace();
								}
								switch(Integer.parseInt(check[0])){
									case 1:
										if(wrongPass)
											return 3;
										break;
									case 2:
										if(!playerPass.equalsIgnoreCase(check[1]))
											return 3;
										p.loadedPass = true;
										p.playerPass = playerPass = token2 = check[1];
										try(MySQLManager load = new MySQLManager(MySQLManager.SERVER)){
											load.removeLoadPass(p);
										}catch(Exception e){
											e.printStackTrace();
										}
										break;
									default:
										break;
								}
								try(MySQLManager load = new MySQLManager(MySQLManager.SERVER)){
									load.checkName(p);
								}catch(Exception e){
									e.printStackTrace();
								}
							}else if(token.equals("run")){
								p.isRunning = p.isRunning2 = Boolean.parseBoolean(token2);
								p.setNewWalkCmdIsRunning(p.isRunning);
							}else if(token.equals("double-exp-time")){
								p.doubleExpTime = Integer.parseInt(token2);
							}else if(token.equals("crystal-bow-shots")){
								p.crystalBowArrowCount = Integer.parseInt(token2);
							}else if(token.equals("skull-timer")){
								p.skullTimer = Integer.parseInt(token2);
							}else if(token.equals("vote-time")){
								p.voteTime = Integer.parseInt(token2);
							}else if(token.equals("mute-time")){
								p.muteTime = Integer.parseInt(token2);
							}else if(token.equals("dfs-charge")){
								p.dfsCharges = Integer.parseInt(token2);
							}else if(token.equals("overload")){
								p.overloaded = convertOverloadLoad(token2);
							}else if(token.equals("overloaded")){
								p.overloadedBool = Boolean.parseBoolean(token2);
							}else if(token.equals("lootshare")){
								p.lootShare = Boolean.parseBoolean(token2);
							}else if(token.equals("overloadtime")){
								p.overloadTime = Integer.parseInt(token2);
							}else if(token.equals("time-mute")){
								p.timeMuted = Boolean.parseBoolean(token2);
							}else if(token.equals("wearingERing")){
								p.isWearingRing = Boolean.parseBoolean(token2);
							}else if(token.equals("magic-book")){
								p.playerMagicBook = Integer.parseInt(token2);
							}else if(token.equals("cavegame")){
								p.inCaveGame = Boolean.parseBoolean(token2);
							}else if(token.equals("brother-info")){
								p.barrowsNpcs[Integer.parseInt(token3[0])][1] = Integer.parseInt(token3[1]);
							}else if(token.equals("special-amount")){
								p.specAmount = Double.parseDouble(token2);
							}else if(token.equals("last-killip")){
								p.lastKillIP = token2;
							}else if(token.equals("cw-games")){
								p.cwGames = Integer.parseInt(token2);
							}else if(token.equals("selected-coffin")){
								p.randomCoffin = Integer.parseInt(token2);
							}else if(token.equals("Donator")){
								p.Donator = Integer.parseInt(token2);
							}else if(token.equals("isJailed")){
								p.isJailed = Boolean.parseBoolean(token2);
							}else if(token.equals("Rating")){
								p.Rating = Integer.parseInt(token2);
							}else if(token.equals("teleblock-length")){
								p.teleBlockDelay = System.currentTimeMillis();
								p.teleBlockLength = Integer.parseInt(token2);
							}else if(token.equals("pk-points")){
								p.pkPoints = Integer.parseInt(token2);
							}else if(token.equals("isDonator")){
								p.isDonator = Integer.parseInt(token2);
							}else if(token.equals("slayerTask")){
								if(token3.length == 4)
									p.slayerTask = new SlayerTask(Integer.parseInt(token3[0]), Integer.parseInt(token3[1]), Integer.parseInt(token3[2]), Integer.parseInt(token3[3]));
							}else if(token.equals("slayerPoints")){
								p.slayerPoints = Integer.parseInt(token2);
							}else if(token.equals("magePoints")){
								p.magePoints = Integer.parseInt(token2);
							}else if(token.equals("autoRet")){
								p.autoRet = Integer.parseInt(token2);
							}else if(token.equals("barrowskillcount")){
								p.barrowsKillCount = Integer.parseInt(token2);
							}else if(token.equals("flagged")){
								p.accountFlagged = Boolean.parseBoolean(token2);
							}else if(token.equals("wave")){
								p.waveId = Integer.parseInt(token2);
							}else if(token.equals("void")){
								for(int j = 0; j < token3.length; j++){
									p.voidStatus[j] = Integer.parseInt(token3[j]);
								}
							}else if(token.equals("gwkc")){
								p.killCount = Integer.parseInt(token2);
							}else if(token.equals("fightMode")){
								p.fightMode = Integer.parseInt(token2);
							}else if(token.equals("xpLock")){
								p.xpLock = Boolean.parseBoolean(token2);
							}else if(token.equals("splitChat")){
								p.splitChat = Boolean.parseBoolean(token2);
							}
							break;
						case 3:
							if(token.equals("character-equip")){
								p.playerEquipment[Integer.parseInt(token3[0])] = Integer.parseInt(token3[1]);
								p.playerEquipmentN[Integer.parseInt(token3[0])] = Integer.parseInt(token3[2]);
								if(token3.length == 4)
									p.playerEquipmentD[Integer.parseInt(token3[0])] = Integer.parseInt(token3[3]);
							}
							break;
						case 4:
							if(token.equals("character-look")){
								p.playerAppearance[Integer.parseInt(token3[0])] = Integer.parseInt(token3[1]);
							}
							break;
						case 5:
							if(token.equals("character-skill")){
								p.playerLevel[Integer.parseInt(token3[0])] = Integer.parseInt(token3[1]);
								p.playerXP[Integer.parseInt(token3[0])] = Integer.parseInt(token3[2]);
							}
							break;
						case 6:
							if(token.equals("character-item")){
								int id = Integer.parseInt(token3[1]);
								int amount = Integer.parseInt(token3[2]);
								int degrade = -1;
								if(token3.length == 4)
									degrade = Integer.parseInt(token3[3]);
								if(id > 0 && amount > 0)
									p.inventory.items[Integer.parseInt(token3[0])] = new InventoryItem(id, amount, degrade);
							}
							break;
						case 7:
							if(token.equals("character-bank")){
								int id = 0, amount = 0, degrade = -1, slot, tab = 0;
								slot = Integer.parseInt(token3[0]);
								try{
									id = Integer.parseInt(token3[1]);
									amount = Integer.parseInt(token3[2]);
								}catch(Exception e){}
								try{
									try{
										degrade = Integer.parseInt(token3[3]);
									}catch(Exception ex){
									}
									tab = Integer.parseInt(token3[4]);
								}catch(Exception e){
									System.out.println();
								}
								BankItem item = new BankItem(id, amount, degrade);
								if(p.bank.bankHasItem(item.id - 1) && item.id == 996){
									long itemAmount = p.bank.getBankAmount(item.id - 1);
									itemAmount += item.amount;
									if(itemAmount > Integer.MAX_VALUE){
										int certs = (int)(itemAmount / ((long)2000000000));
										itemAmount -= (((long)certs) * ((long)2000000000));
										p.bank.setItemAmount(item.id - 1, (int)itemAmount);
										if(p.bank.bankHasItem(Config.BANK_CERTIFICATE))
											p.bank.setItemAmount(Config.BANK_CERTIFICATE, p.bank.getBankAmount(Config.BANK_CERTIFICATE) + certs);
										else
											p.certGive += certs;
									}else
										p.bank.setItemAmount(item.id - 1, (int)itemAmount);
								}else if(p.bank.bankHasItem(item.id - 1)){
									int itemAmount = (((long)p.bank.getBankAmount(item.id - 1)) + ((long)item.amount)) > Integer.MAX_VALUE ? Integer.MAX_VALUE : ((p.bank.getBankAmount(item.id - 1)) + (item.amount));
									p.bank.setItemAmount(item.id - 1, itemAmount);
								}else if(item.id > 1 && item.amount > 0){
									int size = p.bank.tabs.size();
									int pos = tab > size ? size : tab;
									if(!tabIds.contains(tab)){
										tabIds.add(tab);
										p.bank.tabs.add(pos, new Tab());
									}
									int tabSize = p.bank.tabs.get(pos).tabItems.size();
									p.bank.tabs.get(pos).tabItems.add(slot > tabSize ? tabSize : slot, item);
									if(item.id == Config.BANK_CERTIFICATE + 1 && p.certGive > 0){
										p.bank.setItemAmount(Config.BANK_CERTIFICATE, p.certGive + p.bank.getBankAmount(Config.BANK_CERTIFICATE));
										p.certGive = 0;
									}
								}
							}
							break;
						case 8:
							if(token.equals("character-friend")){
								if(!p.hasFriend(Long.parseLong(token3[1])))
									p.friends[Integer.parseInt(token3[0])] = Long.parseLong(token3[1]);
							}
							break;
						case 9:
							if(token.equals("character-ignore")){
								if(!p.hasIgnore(Long.parseLong(token3[1])))
									p.ignores[Integer.parseInt(token3[0])] = Long.parseLong(token3[1]);
							}
							break;
						case 10:
							if(token.equals("character-skill"))
								p.capeLevels[Integer.parseInt(token3[0])] = Integer.parseInt(token3[1]);
							break;
						case 11:
							if(token.equals("quick-prayer"))
								p.quickPrayers[Integer.parseInt(token3[0])] = Boolean.parseBoolean(token3[1]);
							break;
						case 12:
							if(token.equals("note"))
								p.playerNotes.add(token3[1]);
							break;
					}
				}else{
					if(line.equals("[ACCOUNT]")){
						ReadMode = 1;
					}else if(line.equals("[CHARACTER]")){
						ReadMode = 2;
					}else if(line.equals("[EQUIPMENT]")){
						ReadMode = 3;
					}else if(line.equals("[LOOK]")){
						ReadMode = 4;
					}else if(line.equals("[SKILLS]")){
						ReadMode = 5;
					}else if(line.equals("[ITEMS]")){
						ReadMode = 6;
					}else if(line.equals("[BANK]")){
						ReadMode = 7;
					}else if(line.equals("[FRIENDS]")){
						ReadMode = 8;
					}else if(line.equals("[IGNORES]")){
						ReadMode = 9;
					}else if(line.equals("[CAPELEVELS]")){
						ReadMode = 10;
					}else if(line.equals("[QP]")){
						ReadMode = 11;
					}else if(line.equals("[NOTES]")){
						ReadMode = 12;
					}else if(line.equals("[EOF]")){
						p.sendRecovMessage = true;
						p.fixAppearance();
						return 1;
					}
				}
				line = characterfile.readLine();
			}
		}catch(Exception e){
			e.printStackTrace();
			return 3;
		}
		return 13;
	}

	/**
	 * Saving
	 **/
	public static boolean saveGame(Client p){
		if(p == null)
			return false;
		if(!p.saveFile || p.newPlayer || !p.saveCharacter)
			return false;
		if(!p.ignoreNulls)
			if(p.playerName == null || PlayerHandler.players[p.playerId] == null)
				return false;
		try(MySQLManager savePass = new MySQLManager(MySQLManager.SERVER)){
			savePass.checkPlayerSavePass(p);
		}catch(Exception e){
			e.printStackTrace();
		}
		int tbTime = (int)(p.teleBlockDelay - System.currentTimeMillis() + p.teleBlockLength);
		if(tbTime > 300000 || tbTime < 0){
			tbTime = 0;
		}

		try(BufferedWriter characterfile = new BufferedWriter(new FileWriter("./Data/characters/" + p.originalName + ".txt"))){
			/* ACCOUNT */
			characterfile.write("[ACCOUNT]", 0, 9);
			characterfile.newLine();
			characterfile.write("character-username = ", 0, 21);
			characterfile.write(p.originalName, 0, p.originalName.length());
			characterfile.newLine();
			characterfile.write("character-password = ", 0, 21);
			characterfile.write(p.playerPass, 0, p.playerPass.length());
			characterfile.newLine();
			characterfile.newLine();

			/* CHARACTER */
			characterfile.write("[CHARACTER]", 0, 11);
			characterfile.newLine();
			characterfile.write("character-height = ", 0, 19);
			characterfile.write(Integer.toString(p.heightLevel), 0, Integer.toString(p.heightLevel).length());
			characterfile.newLine();
			characterfile.write("character-posx = ", 0, 17);
			characterfile.write(Integer.toString(p.absX), 0, Integer.toString(p.absX).length());
			characterfile.newLine();
			characterfile.write("character-posy = ", 0, 17);
			characterfile.write(Integer.toString(p.absY), 0, Integer.toString(p.absY).length());
			characterfile.newLine();
			characterfile.write("character-rights = ", 0, 19);
			characterfile.write(Integer.toString(p.playerRights), 0, Integer.toString(p.playerRights).length());
			characterfile.newLine();
			characterfile.write("senior-mod = ", 0, 13);
			characterfile.write(Boolean.toString(p.seniorMod), 0, Boolean.toString(p.seniorMod).length());
			characterfile.newLine();
			characterfile.write("crystal-bow-shots = ", 0, 20);
			characterfile.write(Integer.toString(p.crystalBowArrowCount), 0, Integer.toString(p.crystalBowArrowCount).length());
			characterfile.newLine();
			characterfile.write("brandNew = ", 0, 11);
			characterfile.write(Boolean.toString(p.brandNew), 0, Boolean.toString(p.brandNew).length());
			characterfile.newLine();
			characterfile.write("vecna-time = ", 0, 13);
			characterfile.write(Integer.toString(p.vecnaSkullTimer), 0, Integer.toString(p.vecnaSkullTimer).length());
			characterfile.newLine();
			characterfile.write("reset-pin-time = ", 0, 17);
			characterfile.write(Long.toString(p.resetPin), 0, Long.toString(p.resetPin).length());
			characterfile.newLine();
			characterfile.write("bankPin = ", 0, 10);
			characterfile.write(p.bankPin, 0, p.bankPin.length());
			characterfile.newLine();
			characterfile.write("last-conversion = ", 0, 18);
			characterfile.write(Integer.toString(p.lastConversion), 0, Integer.toString(p.lastConversion).length());
			characterfile.newLine();
			characterfile.write("setPin = ", 0, 9);
			characterfile.write(Boolean.toString(p.setPin), 0, Boolean.toString(p.setPin).length());
			characterfile.newLine();
			characterfile.write("skull-timer = ", 0, 14);
			characterfile.write(Integer.toString(p.skullTimer), 0, Integer.toString(p.skullTimer).length());
			characterfile.newLine();
			characterfile.write("vote-time = ", 0, 12);
			characterfile.write(Integer.toString(p.voteTime), 0, Integer.toString(p.voteTime).length());
			characterfile.newLine();
			characterfile.write("ghost-event = ", 0, 14);
			characterfile.write(Boolean.toString(p.ghostEvent), 0, Boolean.toString(p.ghostEvent).length());
			characterfile.newLine();
			characterfile.write("magic-book = ", 0, 13);
			characterfile.write(Integer.toString(p.playerMagicBook), 0, Integer.toString(p.playerMagicBook).length());
			characterfile.newLine();
			characterfile.write("kills = ", 0, 8);
			characterfile.write(Integer.toString((int)p.kills), 0, Integer.toString((int)p.kills).length());
			characterfile.newLine();
			characterfile.write("deaths = ", 0, 9);
			characterfile.write(Integer.toString((int)p.deaths), 0, Integer.toString((int)p.deaths).length());
			characterfile.newLine();
			for(int b = 0; b < p.barrowsNpcs.length; b++){
				characterfile.write("brother-info = ", 0, 15);
				characterfile.write(Integer.toString(b), 0, Integer.toString(b).length());
				characterfile.write("	", 0, 1);
				characterfile.write(p.barrowsNpcs[b][1] <= 1 ? Integer.toString(0) : Integer.toString(p.barrowsNpcs[b][1]), 0, Integer.toString(p.barrowsNpcs[b][1]).length());
				characterfile.newLine();
			}
			// curses-active
			characterfile.write("curses-active = ", 0, 16);
			characterfile.write(Boolean.toString(p.cursesActive), 0, Boolean.toString(p.cursesActive).length());
			characterfile.newLine();
			characterfile.write("lootshare = ", 0, 12);
			characterfile.write(Boolean.toString(p.lootShare), 0, Boolean.toString(p.lootShare).length());
			characterfile.newLine();
			characterfile.write("special-amount = ", 0, 17);
			characterfile.write(Double.toString(p.specAmount), 0, Double.toString(p.specAmount).length());
			characterfile.newLine();
			characterfile.write("highscores = ", 0, 13);
			characterfile.write(Integer.toString(p.highscores), 0, Integer.toString(p.highscores).length());
			characterfile.newLine();
			characterfile.write("run = ", 0, 6);
			characterfile.write(Boolean.toString(p.isRunning), 0, Boolean.toString(p.isRunning).length());
			characterfile.newLine();
			characterfile.write("double-exp-time = ", 0, 18);
			characterfile.write(Integer.toString(p.doubleExpTime), 0, Integer.toString(p.doubleExpTime).length());
			characterfile.newLine();
			characterfile.write("cw-games = ", 0, 11);
			characterfile.write(Integer.toString(p.cwGames), 0, Integer.toString(p.cwGames).length());
			characterfile.newLine();
			characterfile.write("last-ip = ", 0, 10);
			characterfile.write(p.connectedFrom, 0, p.connectedFrom.length());
			characterfile.newLine();
			characterfile.write("last-mac = ", 0, 11);
			characterfile.write(p.macAddress, 0, p.macAddress.length());
			characterfile.newLine();
			characterfile.write("wearingERing = ", 0, 15);
			characterfile.write(Boolean.toString(p.isWearingRing), 0, Boolean.toString(p.isWearingRing).length());
			characterfile.newLine();
			characterfile.write("clan-name = ", 0, 12);
			characterfile.write(p.ownedClanName, 0, p.ownedClanName.length());
			characterfile.newLine();
			characterfile.write("mute-time = ", 0, 12);
			characterfile.write(Integer.toString(p.muteTime), 0, Integer.toString(p.muteTime).length());
			characterfile.newLine();
			characterfile.write("time-mute = ", 0, 12);
			characterfile.write(Boolean.toString(p.timeMuted), 0, Boolean.toString(p.timeMuted).length());
			characterfile.newLine();
			characterfile.write("swap = ", 0, 7);
			characterfile.write(Boolean.toString(p.swap), 0, Boolean.toString(p.swap).length());
			characterfile.newLine();
			characterfile.write("has-curses = ", 0, 13);
			characterfile.write(Boolean.toString(p.hasCurses), 0, Boolean.toString(p.hasCurses).length());
			characterfile.newLine();
			characterfile.write("last-killip = ", 0, 14);
			characterfile.write(p.lastKillIP, 0, p.lastKillIP.length());
			characterfile.newLine();
			characterfile.write("selected-coffin = ", 0, 18);
			characterfile.write(Integer.toString(p.randomCoffin), 0, Integer.toString(p.randomCoffin).length());
			characterfile.newLine();
			characterfile.write("Donator = ", 0, 10);
			characterfile.write(Integer.toString(p.Donator), 0, Integer.toString(p.Donator).length());
			characterfile.newLine();
			characterfile.write("in-castlewars = ", 0, 16);
			characterfile.write(Boolean.toString(p.inCwGame), 0, Boolean.toString(p.inCwGame).length());
			characterfile.newLine();
			characterfile.write("isJailed = ", 0, 11);
			characterfile.write(Boolean.toString(p.isJailed), 0, Boolean.toString(p.isJailed).length());
			characterfile.newLine();
			characterfile.write("Rating = ", 0, 9);
			characterfile.write(Integer.toString(p.Rating), 0, Integer.toString(p.Rating).length());
			characterfile.newLine();
			characterfile.write("barrows-killcount = ", 0, 20);
			characterfile.write(Integer.toString(p.barrowsKillCount), 0, Integer.toString(p.barrowsKillCount).length());
			characterfile.newLine();
			characterfile.write("teleblock-length = ", 0, 19);
			characterfile.write(Integer.toString(tbTime), 0, Integer.toString(tbTime).length());
			characterfile.newLine();
			characterfile.write("public-chat = ", 0, 14);
			characterfile.write(Integer.toString(p.publicChat), 0, Integer.toString(p.publicChat).length());
			characterfile.newLine();
			characterfile.write("private-chat = ", 0, 15);
			characterfile.write(Integer.toString(p.privateChat), 0, Integer.toString(p.privateChat).length());
			characterfile.newLine();
			characterfile.write("rejoin-clan = ", 0, 14);
			characterfile.write(Boolean.toString(p.rejoinClan), 0, Boolean.toString(p.rejoinClan).length());
			characterfile.newLine();
			characterfile.write("last-clan = ", 0, 12);
			characterfile.write(p.lastClan, 0, p.lastClan.length());
			characterfile.newLine();
			characterfile.write("donation-points = ", 0, 18);
			characterfile.write(Integer.toString(p.donationPoints), 0, Integer.toString(p.donationPoints).length());
			characterfile.newLine();
			characterfile.write("vote-points = ", 0, 14);
			characterfile.write(Integer.toString(p.votePoints), 0, Integer.toString(p.votePoints).length());
			characterfile.newLine();
			characterfile.write("pc-points = ", 0, 12);
			characterfile.write(Integer.toString(p.pcPoints), 0, Integer.toString(p.pcPoints).length());
			characterfile.newLine();
			characterfile.write("pk-points = ", 0, 12);
			characterfile.write(Integer.toString(p.pkPoints), 0, Integer.toString(p.pkPoints).length());
			characterfile.newLine();
			characterfile.write("zombie-points = ", 0, 16);
			characterfile.write(Integer.toString(p.zombiePoints), 0, Integer.toString(p.zombiePoints).length());
			characterfile.newLine();
			characterfile.write("isDonator = ", 0, 12);
			characterfile.write(Integer.toString(p.isDonator), 0, Integer.toString(p.isDonator).length());
			characterfile.newLine();
			/* Account recovery info */
			characterfile.write("recover-id = ", 0, 13);
			characterfile.write(Integer.toString(p.recoverId), 0, Integer.toString(p.recoverId).length());
			characterfile.newLine();
			/* Account recovery info */
			if(p.slayerTask != null){
				characterfile.write("slayerTask = ", 0, 13);
				characterfile.write(Integer.toString(p.slayerTask.monster), 0, Integer.toString(p.slayerTask.monster).length());
				characterfile.write("	", 0, 1);
				characterfile.write(Integer.toString(p.slayerTask.requirement), 0, Integer.toString(p.slayerTask.requirement).length());
				characterfile.write("	", 0, 1);
				characterfile.write(Integer.toString(p.slayerTask.level), 0, Integer.toString(p.slayerTask.level).length());
				characterfile.write("	", 0, 1);
				characterfile.write(Integer.toString(p.slayerTask.amount), 0, Integer.toString(p.slayerTask.amount).length());
				characterfile.newLine();
			}
			characterfile.write("slayerPoints = ", 0, 15);
			characterfile.write(Integer.toString(p.slayerPoints), 0, Integer.toString(p.slayerPoints).length());
			characterfile.newLine();
			characterfile.write("magePoints = ", 0, 13);
			characterfile.write(Integer.toString(p.magePoints), 0, Integer.toString(p.magePoints).length());
			characterfile.newLine();
			characterfile.write("autoRet = ", 0, 10);
			characterfile.write(Integer.toString(p.autoRet), 0, Integer.toString(p.autoRet).length());
			characterfile.newLine();
			characterfile.write("barrowskillcount = ", 0, 19);
			characterfile.write(Integer.toString(p.barrowsKillCount), 0, Integer.toString(p.barrowsKillCount).length());
			characterfile.newLine();
			characterfile.write("flagged = ", 0, 10);
			characterfile.write(Boolean.toString(p.accountFlagged), 0, Boolean.toString(p.accountFlagged).length());
			characterfile.newLine();
			characterfile.write("wave = ", 0, 7);
			characterfile.write(Integer.toString(p.waveId), 0, Integer.toString(p.waveId).length());
			characterfile.newLine();
			characterfile.write("cavegame = ", 0, 11);
			characterfile.write(Boolean.toString(p.inCaveGame), 0, Boolean.toString(p.inCaveGame).length());
			characterfile.newLine();
			characterfile.write("dfs-charge = ", 0, 13);
			characterfile.write(Integer.toString(p.dfsCharges), 0, Integer.toString(p.dfsCharges).length());
			characterfile.newLine();
			characterfile.write("overload = ", 0, 11);
			characterfile.write(convertOverloadSave(p.overloaded), 0, convertOverloadSave(p.overloaded).length());
			characterfile.newLine();
			characterfile.write("overloaded = ", 0, 13);
			characterfile.write(Boolean.toString(p.overloadedBool), 0, Boolean.toString(p.overloadedBool).length());
			characterfile.newLine();
			characterfile.write("overloadtime = ", 0, 15);
			characterfile.write(Integer.toString(p.overloadTime), 0, Integer.toString(p.overloadTime).length());
			characterfile.newLine();
			characterfile.write("gwkc = ", 0, 7);
			characterfile.write(Integer.toString(p.killCount), 0, Integer.toString(p.killCount).length());
			characterfile.newLine();
			characterfile.write("fightMode = ", 0, 12);
			characterfile.write(Integer.toString(p.fightMode), 0, Integer.toString(p.fightMode).length());
			characterfile.newLine();
			characterfile.write("xpLock = ", 0, 9);
			characterfile.write(Boolean.toString(p.xpLock), 0, Boolean.toString(p.xpLock).length());
			characterfile.newLine();
			characterfile.write("splitChat = ", 0, 12);
			characterfile.write(Boolean.toString(p.splitChat), 0, Boolean.toString(p.splitChat).length());
			characterfile.newLine();
			characterfile.write("void = ", 0, 7);
			String toWrite = p.voidStatus[0] + "\t" + p.voidStatus[1] + "\t" + p.voidStatus[2] + "\t" + p.voidStatus[3] + "\t" + p.voidStatus[4];
			characterfile.write(toWrite);
			characterfile.newLine();
			characterfile.newLine();

			/* EQUIPMENT */
			characterfile.write("[EQUIPMENT]", 0, 11);
			characterfile.newLine();
			for(int i = 0; i < p.playerEquipment.length; i++){
				characterfile.write("character-equip = ", 0, 18);
				characterfile.write(Integer.toString(i), 0, Integer.toString(i).length());
				characterfile.write("	", 0, 1);
				characterfile.write(Integer.toString(p.playerEquipment[i]), 0, Integer.toString(p.playerEquipment[i]).length());
				characterfile.write("	", 0, 1);
				characterfile.write(Integer.toString(p.playerEquipmentN[i]), 0, Integer.toString(p.playerEquipmentN[i]).length());
				characterfile.write("	", 0, 1);
				characterfile.write(Integer.toString(p.playerEquipmentD[i]), 0, Integer.toString(p.playerEquipmentD[i]).length());
				characterfile.write("	", 0, 1);
				characterfile.newLine();
			}
			characterfile.newLine();

			/* LOOK */
			characterfile.write("[LOOK]", 0, 6);
			characterfile.newLine();
			for(int i = 0; i < p.playerAppearance.length; i++){
				characterfile.write("character-look = ", 0, 17);
				characterfile.write(Integer.toString(i), 0, Integer.toString(i).length());
				characterfile.write("	", 0, 1);
				characterfile.write(Integer.toString(p.playerAppearance[i]), 0, Integer.toString(p.playerAppearance[i]).length());
				characterfile.newLine();
			}
			characterfile.newLine();

			/* SKILLS */
			characterfile.write("[SKILLS]", 0, 8);
			characterfile.newLine();
			for(int i = 0; i < p.playerLevel.length; i++){
				characterfile.write("character-skill = ", 0, 18);
				characterfile.write(Integer.toString(i), 0, Integer.toString(i).length());
				characterfile.write("	", 0, 1);
				characterfile.write(Integer.toString(p.playerLevel[i]), 0, Integer.toString(p.playerLevel[i]).length());
				characterfile.write("	", 0, 1);
				characterfile.write(Integer.toString(p.playerXP[i]), 0, Integer.toString(p.playerXP[i]).length());
				characterfile.newLine();
			}
			characterfile.newLine();

			/* CAPELEVELS */
			characterfile.write("[CAPELEVELS]", 0, 12);
			characterfile.newLine();
			for(int i = 0; i < p.capeLevels.length; i++){
				characterfile.write("cape-skill = ", 0, 13);
				characterfile.write(Integer.toString(i), 0, Integer.toString(i).length());
				characterfile.write("	", 0, 1);
				characterfile.write(Integer.toString(p.capeLevels[i]), 0, Integer.toString(p.capeLevels[i]).length());
				characterfile.newLine();
			}
			characterfile.newLine();

			/* ITEMS */
			characterfile.write("[ITEMS]", 0, 7);
			characterfile.newLine();
			for(int i = 0; i < p.inventory.items.length; i++){
				if(p.inventory.items[i] == null || p.inventory.items[i].id <= 0 || p.inventory.items[i].amount <= 0)
					continue;
				characterfile.write("character-item = ", 0, 17);
				characterfile.write(Integer.toString(i), 0, Integer.toString(i).length());
				characterfile.write("	", 0, 1);
				characterfile.write(Integer.toString(p.inventory.items[i].id), 0, Integer.toString(p.inventory.items[i].id).length());
				characterfile.write("	", 0, 1);
				characterfile.write(Integer.toString(p.inventory.items[i].amount), 0, Integer.toString(p.inventory.items[i].amount).length());
				characterfile.write("	", 0, 1);
				characterfile.write(Integer.toString(p.inventory.items[i].degrade), 0, Integer.toString(p.inventory.items[i].degrade).length());
				characterfile.newLine();
			}
			characterfile.newLine();

			/* BANK */
			characterfile.write("[BANK]", 0, 6);
			characterfile.newLine();
			// Slot in the tab, id, amount, degrade, tab id
			for(int i = 0; i < p.bank.tabs.size(); i++){
				Tab tab = p.bank.tabs.get(i);
				for(int j = 0; j < tab.getNumItems(); j++){
					BankItem item = tab.get(j);
					if(item == null)
						continue;
					characterfile.write("character-bank = ", 0, 17);
					characterfile.write(Integer.toString(j), 0, Integer.toString(j).length());
					characterfile.write("	", 0, 1);
					characterfile.write(Integer.toString(item.id), 0, Integer.toString(item.id).length());
					characterfile.write("	", 0, 1);
					characterfile.write(Integer.toString(item.amount), 0, Integer.toString(item.amount).length());
					characterfile.write("	", 0, 1);
					characterfile.write(Integer.toString(item.degrade), 0, Integer.toString(item.degrade).length());
					characterfile.write("	", 0, 1);
					characterfile.write(Integer.toString(i), 0, Integer.toString(i).length());
					characterfile.newLine();
				}
			}
			characterfile.newLine();

			/* Quick Prayers */
			characterfile.write("[QP]", 0, 4);
			characterfile.newLine();
			for(int i = 0; i < p.quickPrayers.length; i++){
				characterfile.write("quick-prayer = ", 0, 15);
				characterfile.write(Integer.toString(i), 0, Integer.toString(i).length());
				characterfile.write("	", 0, 1);
				characterfile.write(Boolean.toString(p.quickPrayers[i]), 0, Boolean.toString(p.quickPrayers[i]).length());
				characterfile.newLine();
			}
			characterfile.newLine();

			/* FRIENDS */
			characterfile.write("[FRIENDS]", 0, 9);
			characterfile.newLine();
			for(int i = 0; i < p.friends.length; i++){
				if(p.friends[i] > 0){
					String name = Misc.longToPlayerName2(p.friends[i]);
					if(name.equalsIgnoreCase(p.originalName) || name.equalsIgnoreCase(p.playerName))
						continue;
					characterfile.write("character-friend = ", 0, 19);
					characterfile.write(Integer.toString(i), 0, Integer.toString(i).length());
					characterfile.write("	", 0, 1);
					characterfile.write("" + p.friends[i]);
					characterfile.newLine();
				}
			}
			characterfile.newLine();
			characterfile.write("[IGNORES]", 0, 9);
			characterfile.newLine();
			for(int i = 0; i < p.ignores.length; i++){
				if(p.ignores[i] > 0){
					characterfile.write("character-ignore = ", 0, 19);
					characterfile.write(Integer.toString(i), 0, Integer.toString(i).length());
					characterfile.write("	", 0, 1);
					characterfile.write(Long.toString(p.ignores[i]), 0, Long.toString(p.ignores[i]).length());
					characterfile.newLine();
				}
			}
			characterfile.newLine();
			/* Notes */
			characterfile.write("[NOTES]", 0, 7);
			characterfile.newLine();
			for(int i = 0; i < p.playerNotes.size(); i++){
				characterfile.write("note = ", 0, 7);
				characterfile.write(Integer.toString(i), 0, Integer.toString(i).length());
				characterfile.write("	", 0, 1);
				characterfile.write(p.playerNotes.get(i), 0, p.playerNotes.get(i).length());
				characterfile.newLine();
			}
			characterfile.newLine();
			/* EOF */
			characterfile.write("[EOF]", 0, 5);
			characterfile.newLine();
			characterfile.newLine();
		}catch(IOException ioexception){
			System.out.println(p.playerName + ": error writing file.");
			ioexception.printStackTrace();
			return false;
		}
		return true;
	}
}