package server.model.minigames;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import server.Config;
import server.Server;
import server.model.HadesThread;
import server.model.items.GameItem;
import server.model.items.Item;
import server.model.players.Client;
import server.model.players.DuelRules;
import server.model.players.PlayerHandler;
import server.model.players.PlayerSave;
import server.task.Task;
import server.util.Misc;
import server.world.Market;

public class Duel{
	public Client player1;
	public Client player2;
	public int duelCount;
	public boolean isDead = false;
	public short status = 0;
	public int winner = -1;
	public boolean claimed = false;
	public boolean player1Accepted = false;
	public boolean player2Accepted = false;
	public DuelRules rules;
	public CopyOnWriteArrayList<GameItem> player1Items = new CopyOnWriteArrayList<GameItem>();
	public CopyOnWriteArrayList<GameItem> player2Items = new CopyOnWriteArrayList<GameItem>();
	private static final int ARENA_COORDS[][][] = {{{3345, 3251}, {3376, 3232}, {3345, 3213}}, 
		{{3376, 3251}, {3345, 3232}, {3376, 3213}}};
	
	public Duel(Client player1, Client player2){
		this.player1 = player1;
		this.player2 = player2;
	}
	
	public static void sendRequest(Client c, int id){
		try{
			if(id == c.playerId)
				return;
			Client o = (Client)PlayerHandler.players[id];
			if(o == null)
				return;
			if(o.inZombiesGame || c.inZombiesGame || c.isJailed || c.inHowlOfDeath || o.inHowlOfDeath || o.isJailed)
				return;
			if(o.overLoad > 0){
				c.sendMessage("That player is busy right now.");
				return;
			}
			if(c.overLoad > 0){
				c.sendMessage("You can't do that while you're overloading.");
				return;
			}
			if(!c.goodDistance(o.absX, o.absY, c.absX, c.absY, 15) || !o.inDuelArena() || !c.inDuelArena()){
				c.sendMessage("Unable to find " + o.playerName);
				return;
			}
			if((c.duel != null && c.duel.status > 0) || o == null || (o != null && (o.inTrade || o.isBanking))){
				c.sendMessage("The other player is currently busy.");
				if(c.duel != null && c.duel.status < 3)
					declineDuel(c, o != null && c.duel.duelingWith(c, o.playerId) ? true : false);
				return;
			}
			if(o.getPA().ignoreContains(Misc.playerNameToInt64(c.playerName))){
				c.sendMessage("Sending duel request...");
				return;
			}
			c.duel = ((o.duel != null && o.duel.duelingWith(o, c.playerId)) ? o.duel : new Duel(c, o));
			if(c.duel != null && o.duel != null && c.duel.duelingWith(c, o.playerId) && o.duel.duelingWith(o, c.playerId)){
				if(c.goodDistance(c.getX(), c.getY(), o.getX(), o.getY(), 1)){
					c.duel.openDuel();
					DuelRules rules = new DuelRules(c.playerId, o.playerId);
					c.duel.rules = rules;
				}else{
					c.sendMessage("You need to get closer to your opponent to start the duel.");
				}

			}else{
				c.sendMessage("Sending duel request...");
				o.sendMessage(c.playerName + ":duelreq:");
			}
		}catch(Exception e){
			System.out.println("Error requesting duel.");
		}
	}
	
	public static void declineDuel(Client c, boolean other){
		if(other){
			Client o = c.duel.getOtherPlayer(c.playerId);
			if(o != null && o.duel != null && o.duel.duelingWith(o, c.playerId))
				declineDuel(o, false);
		}
		c.getPA().removeAllWindows();
		c.duel.clearStake(c, true);
		c.duel2h = false;
		c.duel.rules = null;
		c.duel.resetDuel(c);
	}
	
	public void clearStake(Client c, boolean add){
		if(player1.playerId == c.playerId){
			if(add){
				for(GameItem item : player1Items){
					if(item.amount < 1)
						continue;
					c.inventory.addItem(item.id, item.amount, -1);
					item.amount = 0;
				}
			}
			player1Items.clear();
		}else{
			if(add){
				for(GameItem item : player2Items){
					if(item.amount < 1)
						continue;
					c.inventory.addItem(item.id, item.amount, -1);
					item.amount = 0;
				}
			}
			player2Items.clear();
		}
	}
	
	public CopyOnWriteArrayList<GameItem> getStake(int id){
		return (player1.playerId == id ? player1Items : (player2.playerId == id ? player2Items : null));
	}
	
	public CopyOnWriteArrayList<GameItem> getOtherStake(int id){
		return (player1.playerId == id ? player2Items : (player2.playerId == id ? player1Items : null));
	}
	
	public void addItem(Client c, GameItem item){
		if(c.playerId == player1.playerId)
			player1Items.add(item);
		else
			player2Items.add(item);
	}
	
	public boolean duelingWith(Client c, int otherId){
		if(player1.playerId == c.playerId)
			return player2.playerId == otherId;
		else if(player2.playerId == c.playerId)
			return player1.playerId == otherId;
		return false;
	}
	
	public void accept(int id){
		if(player1.playerId == id)
			player1Accepted = true;
		else if(player2.playerId == id)
			player2Accepted = true;
	}
	
	public Client getOtherPlayer(int id){
		return player1.playerId == id ? player2 : player1;
	}
	
	public void openDuel(){
		if(player1 == null || player2 == null)
			return;
		status = 1;
		refreshduelRules();
		refreshDuelScreen(player1);
		refreshDuelScreen(player2);
		for(int i = 0; i < player1.playerEquipment.length; i++){
			sendDuelEquipment(player1, player1.playerEquipment[i], player1.playerEquipmentN[i], i);
			sendDuelEquipment(player2, player2.playerEquipment[i], player2.playerEquipmentN[i], i);
		}
		player1.getPA().sendText("Dueling with: " + player2.playerName + " (level-" + player2.combatLevel + ")", 6671);
		player1.getPA().sendText("", 6684);
		player1.getPA().sendFrame248(6575, 3321);
		player1.inventory.resetItems(3322);
		player2.getPA().sendText("Dueling with: " + player1.playerName + " (level-" + player1.combatLevel + ")", 6671);
		player2.getPA().sendText("", 6684);
		player2.getPA().sendFrame248(6575, 3321);
		player2.inventory.resetItems(3322);
	}
	
	public void refreshduelRules(){
		player1.duelOption = player2.duelOption = 0;
		player1.getPA().sendFrame87(286, 0);
		player2.getPA().sendFrame87(286, 0);
	}

	public static void refreshDuelScreen(Client c){
		synchronized(c){
			Client o = c.duel.getOtherPlayer(c.playerId);
			if(o == null){
				return;
			}
			c.getOutStream().createFrameVarSizeWord(53);
			c.getOutStream().writeWord(6669);
			c.getOutStream().writeWord(c.duel.getStake(c.playerId).size());
			int current = 0;
			for(GameItem item : c.duel.getStake(c.playerId)){
				if(item.amount > 254){
					c.getOutStream().writeByte(255);
					c.getOutStream().writeDWord_v2(item.amount);
				}else{
					c.getOutStream().writeByte(item.amount);
				}
				if(item.id > Config.ITEM_LIMIT || item.id < 0){
					item.id = Config.ITEM_LIMIT;
				}
				c.getOutStream().writeWordBigEndianA(item.id + 1);

				current++;
			}

			if(current < 27){
				for(int i = current; i < 28; i++){
					c.getOutStream().writeByte(1);
					c.getOutStream().writeWordBigEndianA(-1);
				}
			}
			c.getOutStream().endFrameVarSizeWord();
			c.flushOutStream();

			c.getOutStream().createFrameVarSizeWord(53);
			c.getOutStream().writeWord(6670);
			c.getOutStream().writeWord(o.duel.getStake(o.playerId).size());
			current = 0;
			for(GameItem item : o.duel.getStake(o.playerId)){
				if(item.amount > 254){
					c.getOutStream().writeByte(255);
					c.getOutStream().writeDWord_v2(item.amount);
				}else{
					c.getOutStream().writeByte(item.amount);
				}
				if(item.id > Config.ITEM_LIMIT || item.id < 0){
					item.id = Config.ITEM_LIMIT;
				}
				c.getOutStream().writeWordBigEndianA(item.id + 1);
				current++;
			}

			if(current < 27){
				for(int i = current; i < 28; i++){
					c.getOutStream().writeByte(1);
					c.getOutStream().writeWordBigEndianA(-1);
				}
			}
			c.getOutStream().endFrameVarSizeWord();
			c.flushOutStream();
		}
	}
	
	public void sendDuelEquipment(Client c, int itemId, int amount, int slot){
		synchronized(c){
			if(itemId != 0){
				c.getOutStream().createFrameVarSizeWord(34);
				c.getOutStream().writeWord(13824);
				c.getOutStream().writeByte(slot);
				c.getOutStream().writeWord(itemId + 1);

				if(amount > 254){
					c.getOutStream().writeByte(255);
					c.getOutStream().writeDWord(amount);
				}else{
					c.getOutStream().writeByte(amount);
				}
				c.getOutStream().endFrameVarSizeWord();
				c.flushOutStream();
			}
		}
	}
	
	public boolean stakeItem(Client c, int itemID, int fromSlot, int amount){
		if(itemID == Config.EASTER_RING)
			return false;
		for(int i : Config.ITEM_TRADEABLE){
			if(i == itemID){
				c.sendMessage("You can't stake this item.");
				return false;
			}
			if(c.playerRights == 2 && !Config.ADMIN_CAN_TRADE){
				c.sendMessage("You can't stake as admin");
				return false;
			}
		}
		if(amount <= 0)
			return false;
		Client o = c.duel.getOtherPlayer(c.playerId);
		if(o == null || o.duel == null || !o.duel.duelingWith(o, c.playerId)){
			declineDuel(c, false);
			return false;
		}
		o.getPA().sendText("", 6684);
		c.getPA().sendText("", 6684);
		if(!c.inventory.hasItem(itemID, amount))
			return false;
		boolean found = false;
		for(GameItem item : c.duel.getStake(c.playerId)){
			if(item.id == itemID && Item.itemStackable[itemID]){
				found = true;
				item.amount += amount;
				c.inventory.deleteItem(itemID, amount);
			}
		}
		if(!found){
			c.inventory.deleteItem(itemID, amount);
			if(Item.itemStackable[itemID])
				c.duel.addItem(c, new GameItem(itemID, amount));
			else
				for(int i = 0; i < amount; i++)
					c.duel.addItem(c, new GameItem(itemID, 1));
		}
		found = false;
		c.inventory.resetItems(3214);
		c.inventory.resetItems(3322);
		o.inventory.resetItems(3214);
		o.inventory.resetItems(3322);
		refreshDuelScreen(c);
		refreshDuelScreen(o);
		c.getPA().sendText("", 6684);
		o.getPA().sendText("", 6684);
		return true;
	}
	public boolean fromDuel(Client c, int itemID, int fromSlot, int amount){
		Client o = c.duel.getOtherPlayer(c.playerId);
		if(o == null || o.duel == null || !o.duel.duelingWith(o, c.playerId)){
			declineDuel(c, false);
			return false;
		}
		if(amount <= 0)
			return false;
		if(Item.itemStackable[itemID]){
			if(c.inventory.freeSlots() - 1 < (c.duelSpaceReq)){
				c.sendMessage("You have too many rules set to remove that item.");
				return false;
			}
		}
		o.getPA().sendText("", 6684);
		c.getPA().sendText("", 6684);
		synchronized(c){
			ArrayList<GameItem> remove = new ArrayList<GameItem>();
			for(GameItem item : c.duel.getStake(c.playerId)){
				if(amount <= 0)
					break;
				if(item.id == itemID){
					if(item.amount <= 0)
						continue;
					int amount3 = Item.itemStackable[item.id] ? 1 : amount > item.amount ? item.amount : amount;
					if(!checkDuelSpace(c, amount3)){
						c.sendMessage("You do not have the required space to remove that staked item.");
						continue;
					}
					c.inventory.addItem(itemID, amount > item.amount ? item.amount : amount, -1);
					int temp = amount;
					amount -= item.amount > amount ? amount : item.amount;
					if(temp < item.amount)
						item.amount -= temp;
					else
						remove.add(item);
				}
			}
			for(GameItem item : remove)
				c.duel.getStake(c.playerId).remove(item);
		}
		c.inventory.resetItems(3214);
		c.inventory.resetItems(3322);
		o.inventory.resetItems(3214);
		o.inventory.resetItems(3322);
		refreshDuelScreen(c);
		refreshDuelScreen(o);
		o.getPA().sendText("", 6684);
		return true;
	}
	
	public boolean find(Client c, int i){
		if(i >= 11 && i <= 16)
			return c.playerEquipment[i - 11] > 0;
		int match[][] = {{17, 7}, {18, 9}, {19, 10}, {20, 12}, {21, 13}};
		for(int j = 0; j < 6; j++)
			if(match[j][0] == i)
				return c.playerEquipment[match[j][1]] > 0;
		return false;
	}
	
	public boolean checkDuelSpace(Client c, int amount){
		int spaces_required = 0;
		for(int i = 11; i < 22; i++)
			if(find(c, i) && rules.duelRule[i])
				spaces_required++;
		if(rules.duelRule[16] && c.playerEquipment[c.playerWeapon] > 0)
			if(c.getItems().is2handed(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]), c.playerEquipment[c.playerWeapon]))
				spaces_required++;
		if(c.inventory.freeSlots() - spaces_required < amount)
			return false;
		return true;
	}
	
	public void confirmDuel(Client c){
		Client o = c.duel.getOtherPlayer(c.playerId);
		if(o == null || o.duel == null || !o.duel.duelingWith(o, c.playerId) || o.duel.rules == null){
			declineDuel(c, false);
			return;
		}
		String itemId = "";
		for(GameItem item : c.duel.getStake(c.playerId)){
			if(Item.itemStackable[item.id] || Item.itemIsNote[item.id]){
				itemId += c.getItems().getItemName(item.id) + " x " + Misc.format(item.amount) + "\\n";
			}else{
				itemId += c.getItems().getItemName(item.id) + "\\n";
			}
		}
		c.getPA().sendText(itemId, 6516);
		itemId = "";
		for(GameItem item : o.duel.getStake(o.playerId)){
			if(Item.itemStackable[item.id] || Item.itemIsNote[item.id]){
				itemId += c.getItems().getItemName(item.id) + " x " + Misc.format(item.amount) + "\\n";
			}else{
				itemId += c.getItems().getItemName(item.id) + "\\n";
			}
		}
		c.getPA().sendText(itemId, 6517);
		c.getPA().sendText("", 8242);
		for(int i = 8238; i <= 8253; i++){
			c.getPA().sendText("", i);
		}
		c.getPA().sendText("Hitpoints will be restored.", 8250);
		c.getPA().sendText("Boosted stats will be restored.", 8238);
		if(rules.duelRule[8])
			c.getPA().sendText("There will be obstacles in the arena.", 8239);
		c.getPA().sendText("", 8240);
		c.getPA().sendText("", 8241);

		String rulesOption[] = {"Players cannot forfeit.", "Players cannot move.", "Players cannot use range.", "Players cannot use melee.", "Players cannot use magic.", "Players cannot drink pots.", "Players cannot eat food.", "Players cannot use prayer.", "Players can only use fun weapons.", "Players cannot use special attacks."};
		int rules[][] = {{0, 0}, {1, 1}, {2, 2}, {3, 3}, {4, 4}, {5, 5}, {6, 6}, {7, 7}, {9, 8}, {10, 9}};
		int lineNumber = 8242;
		for(int i = 0; i < rules.length; i++)
			if(c.duel.rules.duelRule[rules[i][0]])
				c.getPA().sendText("" + rulesOption[rules[i][1]], lineNumber++);
		c.getPA().sendText("", 6571);
		c.getPA().sendFrame248(6412, 197);
		// c.getPA().showInterface(6412);
	}
	
	public void startDuel(final Client player1, final Client player2, boolean count){
		synchronized(player1){
			if(player2 == null || rules == null){
				declineDuel(player1, false);
				return;
			}
			player1.headIconHints = 2;
			duelCount = 3;
			status = 3;
			if(rules.duelRule[7]){
				for(int p = 0; p < player1.PRAYER.length; p++){ // reset prayer glows
					player1.prayerActive[p] = false;
					player1.getPA().sendConfig(player1.PRAYER_GLOW[p], 0);
				}
				player1.headIcon = -1;
				player1.getPA().requestUpdates();
			}
			if(rules.duelRule[11])
				player1.getItems().removeItem(player1.playerEquipment[0], 0);
			if(rules.duelRule[12])
				player1.getItems().removeItem(player1.playerEquipment[1], 1);
			if(rules.duelRule[13])
				player1.getItems().removeItem(player1.playerEquipment[2], 2);
			if(rules.duelRule[14])
				player1.getItems().removeItem(player1.playerEquipment[3], 3);
			if(rules.duelRule[15])
				player1.getItems().removeItem(player1.playerEquipment[4], 4);
			if(rules.duelRule[16]){
				if(player1.getItems().is2handed(player1.getItems().getItemName(player1.playerEquipment[3]).toLowerCase(), player1.playerEquipment[3]))
					player1.getItems().removeItem(player1.playerEquipment[3], 3);
				player1.getItems().removeItem(player1.playerEquipment[5], 5);
			}
			if(rules.duelRule[17])
				player1.getItems().removeItem(player1.playerEquipment[7], 7);
			if(rules.duelRule[18])
				player1.getItems().removeItem(player1.playerEquipment[9], 9);
			if(rules.duelRule[19])
				player1.getItems().removeItem(player1.playerEquipment[10], 10);
			if(rules.duelRule[20])
				player1.getItems().removeItem(player1.playerEquipment[12], 12);
			if(rules.duelRule[21])
				player1.getItems().removeItem(player1.playerEquipment[13], 13);
			player1.getPA().removeAllWindows();
			player1.vengOn = false;
			player1.specAmount = 10;
			player1.getItems().addSpecialBar(player1.playerEquipment[player1.playerWeapon]);
			player1.solTime = -1;
			player1.solSpec = false;
			if(rules.duelRule[1])
				player1.getPA().movePlayer(player1.duelTeleX, player1.duelTeleY, 0);
			else{
				int x = 0, y = 0, pos = 0;
				if(player1.duelArena == -1){
					if(rules.duelRule[8]){
						do{
							pos = Misc.random(ARENA_COORDS[1].length - 1);
							x = ARENA_COORDS[1][pos][0] + (Misc.random(6) * (Misc.random(99) >= 49 ? 1 : -1));
							y = ARENA_COORDS[1][pos][1] + (Misc.random(3) * (Misc.random(99) >= 49 ? 1 : -1));
						}while(!server.clip.region.Region.canMove(x + 1, y + 1, x, y, player1.heightLevel, 1, 1));
					}else{
						pos = Misc.random(ARENA_COORDS[0].length - 1);
						x = ARENA_COORDS[0][pos][0] + (Misc.random(6) * (Misc.random(99) >= 49 ? 1 : -1));
						y = ARENA_COORDS[0][pos][1] + (Misc.random(3) * (Misc.random(99) >= 49 ? 1 : -1));
					}
					player1.duelArena = player2.duelArena = pos;
				}else{
					pos = player1.duelArena;
					player1.duelArena = player2.duelArena = -1;
					if(rules.duelRule[8]){
						do{
							x = ARENA_COORDS[1][pos][0] + (Misc.random(6) * (Misc.random(99) >= 49 ? 1 : -1));
							y = ARENA_COORDS[1][pos][1] + (Misc.random(3) * (Misc.random(99) >= 49 ? 1 : -1));
						}while(!server.clip.region.Region.canMove(x + 1, y + 1, x, y, player1.heightLevel, 1, 1));
					}else{
						x = ARENA_COORDS[0][pos][0] + (Misc.random(6) * (Misc.random(99) >= 49 ? 1 : -1));
						y = ARENA_COORDS[0][pos][1] + (Misc.random(3) * (Misc.random(99) >= 49 ? 1 : -1));
					}
				}
				player1.getPA().movePlayer(x, y, 0);
			}
			player1.getPA().createPlayerHints(10, player2.playerId);
			player1.getPA().showOption(3, 0, "Attack", 1);
			for(int i = 0; i < 20; i++){
				player1.playerLevel[i] = player1.getPA().getLevelForXP(player1.playerXP[i]);
				player1.getPA().refreshSkill(i);
			}
			player1.getPA().requestUpdates();
			player1.poisonDamage = -1;
			player1.poisonImmune = 0;
			player1.overloaded = new int[][]{{0, 0}, {1, 0}, {2, 0}, {4, 0}, {6, 0}};
			player1.overloadedBool = false;
			if(count){
				Server.scheduler.schedule(new Task(2){
					protected void execute(){
						synchronized(player1){
							synchronized(player2){
								if(duelCount == 0){
									player1.damageTaken = new int[Config.MAX_PLAYERS];
									player1.forcedChat("FIGHT!");
									player2.damageTaken = new int[Config.MAX_PLAYERS];
									player2.forcedChat("FIGHT!");
									duelCount--;
									stop();
								}else{
									player1.forcedChat("" + duelCount);
									player2.forcedChat("" + duelCount--);
								}
							}
						}
					}
				});
			}
		}
	}
	
	public void duelVictory(Client player){
		synchronized(player){
			Client o = player.duel.getOtherPlayer(player.playerId);
			if(o != null){
				player.getPA().sendText("" + o.combatLevel, 6839);
				player.getPA().sendText(o.playerName, 6840);
				checkStake(o);
			}else{
				player.getPA().sendText("", 6839);
				player.getPA().sendText("", 6840);
			}
			status = 4;
			player.duelInterface = true;
			finishDuel(player);
			player.getPA().showInterface(6733);
			duelRewardInterface(player);
			claimStakedItems(player);
		}
	}
	
	public void duelRewardInterface(Client player){
		if(winner != player.playerId)
			return;
		synchronized(player){
			player.freezeTimer = 3;
			player.getOutStream().createFrameVarSizeWord(53);
			player.getOutStream().writeWord(6822);
			player.getOutStream().writeWord(getOtherStake(player.playerId).size());
			for(GameItem item : getOtherStake(player.playerId)){
				if(item.amount > 254){
					player.getOutStream().writeByte(255);
					player.getOutStream().writeDWord_v2(item.amount);
				}else{
					player.getOutStream().writeByte(item.amount);
				}
				if(item.id > Config.ITEM_LIMIT || item.id < 0){
					item.id = Config.ITEM_LIMIT;
				}
				player.getOutStream().writeWordBigEndianA(item.id + 1);
			}
			player.getOutStream().endFrameVarSizeWord();
			player.flushOutStream();
		}
	}
	
	public void finishDuel(Client player){
		player.getCombat().resetPrayers();
		for(int i = 0; i < 20; i++){
			player.playerLevel[i] = player.getPA().getLevelForXP(player.playerXP[i]);
			player.getPA().refreshSkill(i);
		}
		player.playerIndex = 0;
		player.underAttackBy = 0;
		player.poisonDamage = -1;
		player.poisonImmune = 0;
		player.overloaded = new int[][]{{0, 0}, {1, 0}, {2, 0}, {4, 0}, {6, 0}};
		player.overloadedBool = false;
		player.duel2h = false;
		player.getPA().refreshSkill(3);
		player.specAmount = 10.0;
		player.doubleHit = false;
		player.specHit = false;
		player.usingSpecial = false;
		player.specEffect = 0;
		player.projectileStage = 0;
		player.getItems().updateSpecialBar();
		player.getPA().movePlayer(Config.DUELING_RESPAWN_X + (Misc.random(Config.RANDOM_DUELING_RESPAWN)), Config.DUELING_RESPAWN_Y + (Misc.random(Config.RANDOM_DUELING_RESPAWN)), 0);
		player.getPA().requestUpdates();
		player.getPA().showOption(3, 0, "Challenge", 3);
		player.getPA().createPlayerHints(10, -1);
		player.duelSpaceReq = 0;
		player.getCombat().resetPlayerAttack();
		player.duelRequested = false;
	}
	
	public void checkStake(Client o){
		long p1Val = 0, p2Val = 0;
		String p1Items = "{", p2Items = "{";
		try{
			for(GameItem item : o.duel.getStake(o.duel.player2.playerId)){
				if(item.id > 0){
					p2Items += "{" + item.id + "," + item.amount + "},";
					long value = Market.itemHasData(item.id) ? Market.getMarketItem(item.id).value : ((long)o.getShops().getItemShopValue(item.id));
					p2Val += (long)((value) * ((long)item.amount));
				}
			}
			for(GameItem item : o.duel.getStake(o.duel.player1.playerId)){
				if(item.id > 0){
					p1Items += "{" + item.id + "," + item.amount + "},";
					long value = Market.itemHasData(item.id) ? Market.getMarketItem(item.id).value : ((long)o.getShops().getItemShopValue(item.id));
					p1Val += (long)((value) * ((long)item.amount));
				}
			}
			p1Items = p1Items.substring(0, p1Items.length() - 1).replace(",", ", ");
			p2Items = p2Items.substring(0, p2Items.length() - 1).replace(",", ", ");
			if(p1Items.length() > 0)
				p1Items += "}";
			if(p2Items.length() > 0)
				p2Items += "}";
			long wealthTransfer = p1Val > p2Val ? p1Val - p2Val : p2Val - p1Val;
			if(wealthTransfer >= 1000000000 && p1Val >= 500000000)
				new HadesThread(HadesThread.REPORT, (p1Val > p2Val ? o.duel.player1 : o.duel.player2), "DUEL - " + (p1Val > p2Val ? o.duel.player1.playerName : o.duel.player2.playerName) + " to " + (p1Val > p2Val ? o.duel.player2.playerName : o.duel.player1.playerName) + " Wealth Transfer - " + Misc.numberFormat(wealthTransfer) + "<br /><br />" + o.duel.player1.playerName + " items - " + p1Items + "<br /><br />" + o.duel.player2.playerName + " items - " + p2Items, 9, true);
		}catch(Exception e){}
	}
	
	public void claimStakedItems(Client player){
		if(claimed)
			return;
		claimed = true;
		synchronized(player){
			for(GameItem item : player.duel.getOtherStake(player.playerId)){
				if(item.id > 0 && item.amount > 0){
					if(Item.itemStackable[item.id]){
						if(!player.inventory.addItem(item.id, item.amount, -1)){
							player.bank.addItem(item.id, item.amount);
						}
					}else{
						int amount = item.amount;
						for(int a = 1; a <= amount; a++){
							if(!player.inventory.addItem(item.id, 1, -1)){
								player.bank.addItem(item.id, 1);
							}
						}
					}
				}
			}
			player.duel.getOtherStake(player.playerId).clear();
			for(GameItem item : player.duel.getStake(player.playerId)){
				if(item.id > 0 && item.amount > 0){
					if(Item.itemStackable[item.id]){
						if(!player.inventory.addItem(item.id, item.amount, -1)){
							player.bank.addItem(item.id, item.amount);
						}
					}else{
						int amount = item.amount;
						for(int a = 1; a <= amount; a++){
							if(!player.inventory.addItem(item.id, 1, -1)){
								player.bank.addItem(item.id, 1);
							}
						}
					}
				}
			}
			player.duel.getStake(player.playerId).clear();
			player.freezeTimer = 3;
			resetDuel(player);
			PlayerSave.saveGame(player);
		}
	}
	
	public void selectRule(Client c, int i){ // rules
		if(player1 == null || player2 == null)
			return;
		if(rules == null)
			return;
		if(status != 1)
			return;
		player1.getPA().sendText("", 6684);
		player2.getPA().sendText("", 6684);
		Client o = getOtherPlayer(c.playerId);
		o.duelSlot = c.duelSlot;
		boolean c2h = c.getItems().is2handed(Item.getItemName(c.playerEquipment[c.playerWeapon]), c.playerEquipment[c.playerWeapon]);
		boolean o2h = o.getItems().is2handed(Item.getItemName(o.playerEquipment[o.playerWeapon]), o.playerEquipment[o.playerWeapon]);
		if(i >= 11 && c.duelSlot > -1){
			if(c.playerEquipment[c.duelSlot] > 0 || ((c.playerEquipment[c.playerWeapon] > 0 && c.duelSlot == c.playerShield && c2h)))
				c.duelSpaceReq += !rules.duelRule[i] ? 1 : -1;
			if(o.playerEquipment[o.duelSlot] > 0 || ((o.playerEquipment[o.playerWeapon] > 0 && o.duelSlot == o.playerShield && o2h)))
				o.duelSpaceReq += !rules.duelRule[i] ? 1 : -1;
			if(c.inventory.freeSlots() < (c.duelSpaceReq) || o.inventory.freeSlots() < (o.duelSpaceReq)){
				c.sendMessage("You or your opponent don't have the required space to set this rule.");
				if(c.playerEquipment[c.duelSlot] > 0 || (c.playerEquipment[c.playerWeapon] > 0 && c.duelSlot == c.playerShield && c.getItems().is2handed(Item.getItemName(c.playerEquipment[c.playerWeapon]), c.playerEquipment[c.playerWeapon])))
					c.duelSpaceReq--;
				if(o.playerEquipment[o.duelSlot] > 0 || (o.playerEquipment[o.playerWeapon] > 0 && o.duelSlot == o.playerShield && o.getItems().is2handed(Item.getItemName(o.playerEquipment[o.playerWeapon]), o.playerEquipment[o.playerWeapon])))
					o.duelSpaceReq--;
				return;
			}
		}
		if(!rules.duelRule[i]){
			rules.duelRule[i] = true;
			c.duelOption += c.DUEL_RULE_ID[i];
		}else{
			rules.duelRule[i] = false;
			c.duelOption -= c.DUEL_RULE_ID[i];
		}

		c.getPA().sendFrame87(286, c.duelOption);
		o.duelOption = c.duelOption;
		o.getPA().sendFrame87(286, o.duelOption);
		
		player1Accepted = false;
		player2Accepted = false;

		if((c.duelSlot == c.playerShield || c.duelSlot == c.playerWeapon) && c2h)
			c.duel2h = rules.duelRule[14] || rules.duelRule[16];
		if((o.duelSlot == o.playerShield || o.duelSlot == o.playerWeapon) && o2h)
			o.duel2h = rules.duelRule[14] || rules.duelRule[16];
		
		if(rules.duelRule[8] && rules.duelRule[1]){
			int x = 0, y = 0, pos = 0;
			do{
				pos = Misc.random(ARENA_COORDS[1].length - 1);
				x = ARENA_COORDS[1][pos][0] + (Misc.random(6) * (Misc.random(99) >= 49 ? 1 : -1));
				y = ARENA_COORDS[1][pos][1] + (Misc.random(3) * (Misc.random(99) >= 49 ? 1 : -1));
			}while(!server.clip.region.Region.canMove(x + 1, y + 1, x, y, c.heightLevel, 1, 1) || !!server.clip.region.Region.canMove(x, y, x - 1, y, c.heightLevel, 1, 1));
			c.duelTeleX = x;
			o.duelTeleX = x - 1;
			c.duelTeleY = y;
			o.duelTeleY = y;
		}else if(rules.duelRule[1]){
			int x = 0, y = 0, pos = 0;
			pos = Misc.random(ARENA_COORDS[0].length - 1);
			x = ARENA_COORDS[0][pos][0] + (Misc.random(6) * (Misc.random(99) >= 49 ? 1 : -1));
			y = ARENA_COORDS[0][pos][1] + (Misc.random(3) * (Misc.random(99) >= 49 ? 1 : -1));
			c.duelTeleX = x;
			o.duelTeleX = x - 1;
			c.duelTeleY = y;
			o.duelTeleY = y;
		}
	}
	
	public void resetDuel(Client player){
		if(status > 2)
			finishDuel(player);
		player.getPA().showOption(3, 0, "Challenge", 3);
		player.headIconHints = 0;
		refreshduelRules();
		rules = null;
		player.duelArena = -1;
		player.getPA().createPlayerHints(10, -1);
		player.duel2h = false;
		player.canOffer = true;
		player.duelSpaceReq = 0;
		player.vengOn = false;
		player.vecnaSkullTimer = -1;
		player.getPA().requestUpdates();
		player.getCombat().resetPlayerAttack();
		player.duelRequested = false;
		player.duel = null;
	}
}