package server.model.players;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Map.Entry;
import java.util.concurrent.Future;

import org.apache.mina.core.session.IoSession;
import org.jboss.netty.channel.Channel;

import server.Config;
import server.Server;
import server.model.HadesThread;
import server.model.items.Degrade;
import server.model.items.GameItem;
import server.model.items.Inventory;
import server.model.items.ItemAssistant;
import server.model.items.bank.Bank;
import server.model.items.bank.BankItem;
import server.model.items.bank.Tab;
import server.model.objects.DwarfCannon;
import server.model.region.Location;
import server.model.region.Region;
import server.model.region.RegionManager;
import server.model.shops.ShopAssistant;
import server.model.quests.DemonSlayer;
import server.model.quests.QuestHandler;
import server.net.HostList;
import server.net.Packet;
import server.net.StaticPacketBuilder;
import server.task.Task;
import server.util.Misc;
import server.util.Stream;
import server.world.Clan;
import server.model.minigames.CastleWars;
import server.model.minigames.Duel;
import server.model.minigames.FightPits;
import server.model.minigames.HowlOfDeath;
import server.model.minigames.HowlOfDeathManager;
import server.model.minigames.Zombies;
import server.model.npcs.NPCHandler;
import server.model.players.skills.*;
import server.model.players.skills.fishing.Fishing;

public class Client extends Player{
	private Pins pins = new Pins(this);
	public byte buffer[] = null;
	public Stream inStream = null, outStream = null;
	private IoSession session;
	private Channel channel;
	private Degrade degrade = new Degrade(this);
	private ItemAssistant itemAssistant = new ItemAssistant(this);
	private ShopAssistant shopAssistant = new ShopAssistant(this);
	private TradeAndDuel tradeAndDuel = new TradeAndDuel(this);
	private PlayerAssistant playerAssistant = new PlayerAssistant(this);
	private CombatAssistant combatAssistant = new CombatAssistant(this);
	private ActionHandler actionHandler = new ActionHandler(this);
	private PlayerKilling playerKilling = new PlayerKilling(this);
	private DialogueHandler dialogueHandler = new DialogueHandler(this);
	private Queue<Packet> queuedPackets = new LinkedList<Packet>();
	private Potions potions = new Potions(this);
	private QuickPrayerHandler quickPrayerHandler = new QuickPrayerHandler(this);
	private PotionMixing potionMixing = new PotionMixing(this);
	private Food food = new Food(this);
	private DwarfCannon cannon = new DwarfCannon(this);
	public Zombies zombies = null;
	public Bank bank = new Bank(this);
	public Bank oBank;
	public Inventory inventory = new Inventory(this);
	public Duel duel;
	/**
	 * Skill instances
	 */
	private Slayer slayer = new Slayer(this);
	private Runecrafting runecrafting = new Runecrafting(this);
	private Woodcutting woodcutting = new Woodcutting(this);
	private Mining mine = new Mining(this);
	private Agility agility = new Agility(this);
	private Cooking cooking = new Cooking(this);
	private Fishing fish = new Fishing(this);
	private Crafting crafting = new Crafting(this);
	private Smithing smith = new Smithing(this);
	private Prayer prayer = new Prayer(this);
	private Fletching fletching = new Fletching(this);
	private SmithingInterface smithInt = new SmithingInterface(this);
	private Farming farming = new Farming(this);
	private Thieving thieving = new Thieving(this);
	private Firemaking firemaking = new Firemaking(this);
	private Herblore herblore = new Herblore(this);
	private QuestHandler questHandler = new QuestHandler(this);
	public int lowMemoryVersion = 0;
	public boolean isLoggingOut = false;
	public int timeOutCounter = 0;
	public int returnCode = 2;
	private Future<?> currentTask;

	public Client(IoSession s, int _playerId){
		super(_playerId);
		this.session = s;
		this.channel = null;
		synchronized(this){
			outStream = new Stream(new byte[Config.BUFFER_SIZE]);
			outStream.currentOffset = 0;
		}
		inStream = new Stream(new byte[Config.BUFFER_SIZE]);
		inStream.currentOffset = 0;
		buffer = new byte[Config.BUFFER_SIZE];
	}

	public Client(Channel c, int _playerId){
		super(_playerId);
		this.session = null;
		this.channel = c;
		synchronized(this){
			outStream = new Stream(new byte[Config.BUFFER_SIZE]);
			outStream.currentOffset = 0;
		}
		inStream = new Stream(new byte[Config.BUFFER_SIZE]);
		inStream.currentOffset = 0;
		buffer = new byte[Config.BUFFER_SIZE];
	}

	public int getLocalX(){
		return Math.abs(getX() - 8 * getMapRegionX());
	}

	public int getLocalY(){
		return Math.abs(getY() - 8 * getMapRegionY());
	}

	public String[] customRank(){
		for(String rank[] : Config.CUSTOM_RANKS)
			if(this.playerName.equalsIgnoreCase(rank[0]))
				return new String[]{rank[1], rank[2], rank[3], rank[4]};
		return new String[]{""};
	}
	
	public String[] customGlobalRank(){
		for(String rank[] : Config.CUSTOM_RANKS)
			if(rank[0].equalsIgnoreCase("all"))
				return new String[]{rank[1], rank[2], rank[3], rank[4]};
		return new String[]{""};
	}

	public void HighAndLow(){
		if(combatLevel < 15){
			int Low = 3;
			int High = combatLevel + 12;
			getPA().sendText("@gre@" + Low + "@yel@ - @dre@" + High + "", 199);
		}
		if(combatLevel > 15 && combatLevel < 114){
			int Low = combatLevel - 12;
			int High = combatLevel + 12;
			getPA().sendText("@gre@" + Low + "@yel@ - @dre@" + High + "", 199);
		}
		if(combatLevel > 114){
			int Low = combatLevel - 12;
			int High = 126;
			getPA().sendText("@gre@" + Low + "@yel@ - @dre@" + High + "", 199);
		}
	}

	public void flushOutStream(){
		if(disconnected || outStream.currentOffset == 0)
			return;
		synchronized(this){
			StaticPacketBuilder out = new StaticPacketBuilder().setBare(true);
			byte[] temp = new byte[outStream.currentOffset];
			System.arraycopy(outStream.buffer, 0, temp, 0, temp.length);
			out.addBytes(temp);
			if (channel != null) {
				channel.write(out.toPacket());
			}
			if (session != null) {
				session.write(out.toPacket());
			}
			outStream.currentOffset = 0;
		}
	}
	
	public boolean isUntrimmedCape(int id){
		for(int cape[] : Config.UNTRIMMED_SKILL_CAPES)
			if(id == cape[1])
				return true;
		return false;
	}
	
	public void fixUntrimmedCape(){
		if(isUntrimmedCape(playerEquipment[playerCape]))
			playerEquipment[playerCape]++;
		inventory.fixUntrimmedCape();
		bank.fixUntrimmedCape();
	}
	
	public void sendClan(String name, String message, String clan, int rights, int clanSize){
		outStream.createFrameVarSizeWord(217);
		outStream.writeString(name);
		outStream.writeString(message);
		outStream.writeString(clan);
		outStream.writeWord(rights);
		outStream.writeByte(clanSize);
		outStream.endFrameVarSize();
	}

	public static final int PACKET_SIZES[] = {0, 0, 0, 1, -1, 0, 0, 0, 0, 0, // 0
	0, 0, 0, 0, 8, 0, 6, 2, 2, 0, // 10
	0, 2, 0, 6, 0, 12, 0, 2, 0, 0, // 20
	0, 0, 0, 0, 8, 8, 4, 0, 0, 2, // 30
	2, 6, 0, 6, 0, -1, 0, 0, 0, 0, // 40
	0, 8, 12, 12, 0, 0, 0, 8, 8, 12, // 50
	8, 8, -1, 2, -1, 0, 0, 0, 0, 0, // 60
	6, 0, 2, 2, 8, 6, 12, -1, 0, 6, // 70
	0, 0, 0, 0, 0, 1, 4, 6, -1, 1, // 80
	0, 0, 0, 0, 0, 3, 0, 0, -1, 0, // 90
	0, 13, 0, -1, 0, 0, 0, 0, 0, 0,// 100
	0, 0, 0, 0, 0, 0, 0, 6, 0, 0, // 110
	1, 0, 6, 0, 0, 2, -1, 2, 2, 6, // 120
	0, 4, 6, 8, 0, 6, 0, 0, 0, 2, // 130
	2, 0, 0, 0, 0, 6, 0, 0, 0, 0, // 140
	0, 0, 1, 2, 0, 2, 6, 0, 0, 0, // 150
	0, 0, 0, 0, -1, -1, 0, 0, 0, 0,// 160
	5, 0, 0, 0, 0, 0, 0, 0, 0, 0, // 170
	0, 8, 0, 3, 0, 2, 0, 0, 8, 1, // 180
	0, 0, 12, 0, 0, 0, 0, 0, 0, 0, // 190
	2, 0, 0, 0, 0, 0, 0, 0, 4, 0, // 200
	4, 0, 0, 0, 9, 8, 0, 0, 10, 0, // 210
	0, 0, 0, 0, 0, 0, -1, 0, 6, 0, // 220
	1, 0, 0, 0, 6, 0, 6, 8, 1, 0, // 230
	0, 4, 0, 0, 0, 0, -1, 0, -1, 4,// 240
	0, 0, 6, 6, 0, 0, 0 // 250
	};

	public void closeClanWars(){
		if(Server.clanChat.clans.containsKey(clanId)){
			Clan clan = Server.clanChat.clans.get(clanId);
			if(clan.warLeader > 0 && clan.warLeader == playerId && clan.war != null)
				clan.war.clearInterfaces();
		}
	}

	public void destruct(){
		synchronized(this){
			if(session == null && channel == null)
				return;
			loggingOut = true;
			try{
				getRegion().removePlayer(this);
				if(overLoad > 0)
					playerLevel[playerHitpoints] -= 10 * overLoad;
				if(duel != null && duel.status == 3){
					duel.winner = duel.getOtherPlayer(this.playerId).playerId;
					absX = Config.DUELING_RESPAWN_X + (Misc.random(Config.RANDOM_DUELING_RESPAWN));
					absY = Config.DUELING_RESPAWN_Y + (Misc.random(Config.RANDOM_DUELING_RESPAWN));
					heightLevel = 0;
				}
				if(duel != null && duel.status < 3)
					Duel.declineDuel(this, true);
				if(inTrade)
					getTradeAndDuel().declineTrade(true);
				if(respawnTimer >= 7)
					getPA().giveLife();
				else if(underAttackBy > 0)
					getPA().giveLife();
			}catch(Exception e){
			}
			if(inNexGame && nexGames != null)
				nexGames.handleLeaderDeath(this);
			if(Server.clanChat.clans.containsKey(ownedClanName)){
				Server.clanChat.sendCoinShareMessage(ownedClanName, (lootShare ? "Lootshare" : "Coinshare") + " has been toggled to off by the clan leader.");
				Server.clanChat.clans.get(ownedClanName).coinshare = false;
				Server.clanChat.updateClanChat(ownedClanName);
			}
			cannon.handleLogout();
			if(inClanWars){
				getPA().giveLife();
				absX = Config.RESPAWN_X;
				absY = Config.RESPAWN_Y;
				heightLevel = 0;
			}
			closeClanWars();
			new HadesThread(HadesThread.SAVE_HIGHSCORE, this);
			int count = 0;
			boolean trimmed = false;
			for(int i = 0; i<playerXP.length; i++){
				if(this.getLevelForXP(playerXP[i]) == 99)
					count++;
				if(count > 1){
					trimmed = true;
					break;
				}
			}
			if(trimmed)
				fixUntrimmedCape();
			if(inHowlOfDeathLobby)
				HowlOfDeathManager.getInstance().removePlayer(playerId);
			if(inHowlOfDeath){
				HowlOfDeath howl = howlOfDeath;
				howlOfDeath.removePlayer(this);
				if(howl.playerTargets.containsKey(playerId)){
					howl.playerIds.remove(playerId);
					howl.playerTargets.remove(playerId);
					howl.redoTargets();
				}
			}
			if(maxCapeNpcId > 0 && NPCHandler.npcs[maxCapeNpcId] != null)
				Server.npcHandler.removeNPC(maxCapeNpcId);
			if(CastleWars.isInCwWait(this))
				CastleWars.leaveWaitingRoom(this);
			if(CastleWars.isInCw(this))
				CastleWars.removePlayerFromCw(this);
			if(inPits)
				Server.fightPits.removePlayerFromPits(playerId);
			if(!clanId.isEmpty()){
				lastClan = clanId;
				Server.clanChat.leaveClan(playerId, clanId);
			}
			/*
			 * if(this.inCaveGame) this.getPA().resetTzhaar();
			 */
			if(inZombiesGame && zombies != null){
				if(zombies.leaderId == playerId)
					zombies.endGame();
				else
					zombies.handleDeath(this);
				absX = Config.RESPAWN_X;
				absY = Config.RESPAWN_Y;
				heightLevel = 0;
			}
			if(playerName.equalsIgnoreCase(Config.OWNER_HIDDEN))
				playerRights = 5;
			/*synchronized(Server.pestControl){
				if(inPcGame())
					Server.pestControl.playersInGame.remove((Object)playerId);
				if(inPcBoat())
					Server.pestControl.playersInBoat.remove((Object)playerId);
			}*/
			FightPits.removeWait(playerId);
			PlayerSave.saveGame(this);
			// System.out.println("[DEREGISTERED]: "+playerName+"");

			if (session != null) {
				HostList.getHostList().remove(session);
				session.close(false);		
			}

			if (channel != null) {
				HostList.getHostList().remove(channel);
				channel.close();
			}			

			disconnected = true;
			inStream = null;
			outStream = null;
			isActive = false;
			buffer = null;
			super.destruct();
			new HadesThread(HadesThread.UPDATE_USERS_ONLINE);
		}
	}

	public boolean isInCastleWars(){
		// Greaterthan-Y, Lessthan-X, Greaterthan-X, Lessthan-Y 2369 9526
		int allCoords[][] = {{3069, 2434, 2365, 3138}, {9482, 2392, 2370, 9497}, {9514, 2429, 2411, 9533}, {3081, 2446, 2435, 3098}, {9481, 2430, 2369, 9528}};
		for(int coords[] : allCoords)
			if((this.absX <= coords[1] && this.absX >= coords[2]) && (this.absY >= coords[0] && this.absY <= coords[3]))
				return true;
		return false;
	}
	
	public boolean isInCastleWarsStart(){
		return absX >= 2435 && absX <= 2446 && absY >= 3081 && absY <= 3098;
	}
	
	public boolean leftHowlArea(){
		return (absY > 3375 && absX < 2868) || (absY < 3376 && absX < 3067) || absX > 3329 || absY < 3139 || absY > 3525;
	}

	public boolean acceptableStaff(){
		int staffs[] = {4675, 13869, 13867, 18355, 4710, 4862, 4863, 4864, 4865, 15486, 21777, 22494, 22496, 24457, 22347};
		for(int i : staffs)
			if(i == this.playerEquipment[this.playerWeapon])
				return true;
		return false;
	}
	
	public int calculatePolyporeDamage(){
		int dam = (playerLevel[playerMagic] * 5) - 180;
		return dam > 0 ? dam / 10 : 0;
	}

	public void convertTickets(){
		if(inTrade){
			Iterator<GameItem> it = getTradeAndDuel().offeredItems.iterator();
			ArrayList<GameItem> remove = new ArrayList<GameItem>();
			while(it.hasNext()){
				GameItem item = it.next();
				if(item.id == Config.DONATION_TICKET)
					remove.add(item);
			}
			if(remove.size() > 0){
				Client o = (Client)PlayerHandler.players[tradeWith];
				for(GameItem item : remove)
					getTradeAndDuel().offeredItems.remove(item);
				getTradeAndDuel().resetTItems(3415);
				if(o != null)
					o.getTradeAndDuel().resetOTItems(3416);
			}
		}
		if(duel != null){
			Iterator<GameItem> it = duel.getStake(playerId).iterator();
			ArrayList<GameItem> remove = new ArrayList<GameItem>();
			while(it.hasNext()){
				GameItem item = it.next();
				if(item.id == Config.DONATION_TICKET)
					remove.add(item);
			}
			if(remove.size() > 0){
				synchronized(duel.getStake(playerId)){
					for(GameItem item : remove)
						duel.getStake(playerId).remove(item);
				}
				if(duel != null && (duel.status == 1 || duel.status == 2)){
					Duel.refreshDuelScreen(this);
					if(duel.getOtherPlayer(playerId) != null)
						Duel.refreshDuelScreen(duel.getOtherPlayer(playerId));
				}
			}
		}
		donationPoints += inventory.convertTickets() + bank.convertTickets();
		lastConversion = Misc.currentTimeSeconds();
	}

	public void rest(){
		if(resting){
			int emotes[][] = {{5713, 5748}, {11786, 11788}, {2716, 2921}};
			int spot = Misc.random(emotes.length - 1);
			rest[0] = emotes[spot][0];
			rest[1] = emotes[spot][1];
		}
		startAnimation(rest[resting ? 0 : 1]);
		getPA().sendRest();
	}

	public void fixAppearance(){
		if(playerAppearance[0] != 0 && playerAppearance[0] != 1)
			playerAppearance[0] = 0;
		final int GENDER[][] = playerAppearance[0] == 0 ? Config.MALE_VALUES : Config.FEMALE_VALUES;
		final int appearances[] = new int[Config.MALE_VALUES.length];
		/*
		 * Appearance value check.
		 */
		appearances[0] = playerAppearance[1] < GENDER[0][0] || playerAppearance[1] > GENDER[0][1] ? GENDER[0][0] : playerAppearance[1];
		for(int i = 2; i < 7; i++){
			int value = playerAppearance[i];
			appearances[i] = value < GENDER[i][0] || value > GENDER[i][1] ? GENDER[i][0] : value;
		}
		appearances[1] = playerAppearance[7] < GENDER[1][0] || playerAppearance[7] > GENDER[1][1] ? GENDER[1][0] : playerAppearance[7];
		final int colors[] = new int[Config.ALLOWED_COLORS.length];
		/*
		 * Color value check.
		 */
		for(int i = 0; i < colors.length; i++){
			int value = playerAppearance[i + 8];
			colors[i] = value < Config.ALLOWED_COLORS[i][0] || value > Config.ALLOWED_COLORS[i][1] ? Config.ALLOWED_COLORS[i][0] : value;
		}

		playerAppearance[1] = appearances[0]; // head
		playerAppearance[2] = appearances[2]; // torso
		playerAppearance[3] = appearances[3]; // arms
		playerAppearance[4] = appearances[4]; // hands
		playerAppearance[5] = appearances[5]; // legs
		playerAppearance[6] = appearances[6]; // feet
		playerAppearance[7] = appearances[1]; // beard
		playerAppearance[8] = colors[0]; // hair colour
		playerAppearance[9] = colors[1]; // torso colour
		playerAppearance[10] = colors[2]; // legs colour
		playerAppearance[11] = colors[3]; // feet colour
		playerAppearance[12] = colors[4]; // skin colour
	}
	
	public boolean inOwnersArray(String name){
		for(String owner : Config.OWNERS)
			if(owner.equalsIgnoreCase(name))
				return true;
		return false;
	}

	public boolean inHeadAdminArray(String name){
		for(String ha : Config.HEAD_ADMINS)
			if(ha.equalsIgnoreCase(name))
				return true;
		return false;
	}
	
	public void sendMessage(String s){
		synchronized(this){
			if(getOutStream() != null){
				outStream.createFrameVarSize(253);
				outStream.writeString(s);
				outStream.endFrameVarSize();
			}
		}
	}

	public void setSidebarInterface(int menuId, int form){
		synchronized(this){
			if(getOutStream() != null){
				outStream.createFrame(71);
				outStream.writeWord(form);
				outStream.writeByteA(menuId);
			}
		}
	}

	public boolean inGWD(){
		return ((this.absX >= 2896 && this.absX <= 2925) && (this.absY >= 3597 && this.absY <= 3629));
	}
	
	public void handleAncientBook(){
		if(playerEquipment[playerShield] != 19617)
			return;
		if(!clanId.isEmpty() && Server.clanChat.clans.containsKey(clanId)){
			Clan c = Server.clanChat.clans.get(clanId);
			int leader = PlayerHandler.getPlayerId(c.owner);
			if(!Config.NEX_SPAWNED[leader])
				return;
		}
		if(ancientBookHeal == -1)
			ancientBookHeal = Misc.currentTimeSeconds() + 60;
		else if(ancientBookHeal <= Misc.currentTimeSeconds()){
			gfx0(436);
			if(playerLevel[playerHitpoints] + 5 > getLevelForXP(playerXP[playerHitpoints]))
				playerLevel[playerHitpoints] = getLevelForXP(playerXP[playerHitpoints]);
			else
				playerLevel[playerHitpoints] += 5;
			getPA().refreshSkill(playerHitpoints);
			ancientBookHeal = Misc.currentTimeSeconds() + 60;
		}
		System.out.println(ancientBookHeal + " - " + Misc.currentTimeSeconds());
	}
	
	public void initialize(){
		synchronized(this){
			outStream.createFrame(249);
			outStream.writeByteA(1); // 1 for members, zero for free
			outStream.writeWordBigEndianA(playerId);
			for(int j = 0; j < PlayerHandler.players.length; j++)
				if(j != playerId)
					if(PlayerHandler.players[j] != null)
						if(PlayerHandler.players[j].playerName.equalsIgnoreCase(playerName))
							setDisconnected(true);
			for(int i = 0; i < 25; i++){
				getPA().setSkillLevel(i, playerLevel[i], playerXP[i]);
				getPA().refreshSkill(i);
			}
			for(int p = 0; p < PRAYER.length; p++){ // reset prayer glows
				prayerActive[p] = false;
				getPA().sendConfig(PRAYER_GLOW[p], 0);
			}
			int totalLevel = 0;
			if(lastConversion <= 0)
				lastConversion = Misc.currentTimeSeconds();
			else if(lastConversion < Misc.getLastMidnight())
				convertTickets();
			for(int i = 0; i <= Config.NUM_SKILLS; i++)
				totalLevel += getLevelForXP(playerXP[i]);
			new HadesThread(HadesThread.UPDATE_LAST_ONLINE, this);
			setLocation(Location.create(absX, absY, heightLevel));
			getPA().sendText(playerNotes.isEmpty() ? "No notes" : "", 13800);
			for(int i = 0, id = 18801; id<=18830; i++, id++)
				getPA().sendText(i < playerNotes.size() ? playerNotes.get(i) : "", id);
			getPA().sendRun();
			getPA().sendText("Total Lvl: " + totalLevel, 3984);
			getPA().handleWeaponStyle();
			getPA().handleLoginText();
			getPA().sendText("Friends List - " + getNumFriends() + " / " + friends.length, 5067);
			getPA().sendText("Ignore List - " + getNumIgnores() + " / " + ignores.length, 5717);
			accountFlagged = getPA().checkForFlags();
			// getPA().sendFrame36(43, fightMode-1);
			getPA().sendConfig(108, 0);// resets autocast button
			getPA().sendConfig(172, 1);
			for(int i = this.cursesActive ? 26 : 0; i < (this.cursesActive ? quickPrayers.length : 26); i++)
				getPA().sendConfig((i > 25 ? i - 26 : i) + 630, quickPrayers[i]);
			getPA().resetScreen(); // reset screen
			getPA().setChatOptions(publicChat, privateChat, 0); // reset private messaging options
			getPA().sendConfig(599, lootShare ? 0 : 1);
			getPA().sendConfig(600, lootShare ? 1 : 0);
			setSidebarInterface(1, 3917);
			setSidebarInterface(2, 638);
			setSidebarInterface(3, 3213);
			setSidebarInterface(4, 1644);
			setSidebarInterface(5, cursesActive ? 22500 : 5608);
			int magics = playerMagicBook == 0 ? 1151 : (playerMagicBook == 2 ? 29999 : 12855);
			setSidebarInterface(6, magics);
			setSidebarInterface(7, 18128);
			setSidebarInterface(8, 5065);
			setSidebarInterface(9, 5715);
			setSidebarInterface(10, 2449);
			// setSidebarInterface(11, 4445); // wrench tab
			setSidebarInterface(11, 904); // wrench tab
			setSidebarInterface(12, 147); // run tab
			setSidebarInterface(13, 173);
			setSidebarInterface(0, 2423);
			CastleWars.removeLoggedInPlayer(this);
			if(inClanWars()){
				absX = 3272;
				absY = 3692;
				heightLevel = 0;
				getPA().movePlayer(3272, 3692, 0);
			}
			if(inFightPits()){
				addedToPits = false;
				inPits = false;
				getPA().movePlayer(2399, 5177, 0);
			}
			if(this.isWearingRing){
				int eggId = this.easterEggs[Misc.random(this.easterEggs.length - 1)];
				this.npcId = eggId;
				this.isNpc = true;
				this.updateRequired = true;
				this.appearanceUpdateRequired = true;
			}
			questHandler.loadQuests();
			if(this.playerName.equalsIgnoreCase("hadesflames")){
				this.playerName = this.playerName.toLowerCase();
				this.playerName2 = this.playerName2.toLowerCase();
			}
			if(this.playerName.equalsIgnoreCase("mrs hades"))
				this.playerName = this.playerName2 = "Mrs hades";
			int sc = this.splitChat ? 1 : 0;
			this.getPA().sendConfig(502, sc);
			this.getPA().sendConfig(287, sc);
			this.getPA().sendConfig(575, swap ? 0 : 1);
			Server.clanChat.setupClan(this);
			getPA().sendText("@red@SC", 177);
			getPA().sendText("@gre@" + (xpLock ? "(Locked)" : "(Unlocked)"), 15226);
			sendMessage("Welcome to " + Config.SERVER_NAME + ".");
			getPA().loadAnnouncements();
			getPA().showOption(4, 0, "Trade With", 3);
			getPA().showOption(5, 0, "Follow", 4);
			inventory.resetItems(3214);
			getItems().sendWeapon(playerEquipment[playerWeapon], getItems().getItemName(playerEquipment[playerWeapon]));
			getItems().resetBonus();
			getItems().getBonus();
			getItems().writeBonus();
			getItems().setEquipment(playerEquipment[playerHat], 1, playerHat);
			getItems().setEquipment(playerEquipment[playerCape], 1, playerCape);
			getItems().setEquipment(playerEquipment[playerAmulet], 1, playerAmulet);
			getItems().setEquipment(playerEquipment[playerArrows], playerEquipmentN[playerArrows], playerArrows);
			getItems().setEquipment(playerEquipment[playerChest], 1, playerChest);
			getItems().setEquipment(playerEquipment[playerShield], 1, playerShield);
			getItems().setEquipment(playerEquipment[playerLegs], 1, playerLegs);
			getItems().setEquipment(playerEquipment[playerHands], 1, playerHands);
			getItems().setEquipment(playerEquipment[playerFeet], 1, playerFeet);
			getItems().setEquipment(playerEquipment[playerRing], 1, playerRing);
			getItems().setEquipment(playerEquipment[playerWeapon], playerEquipmentN[playerWeapon], playerWeapon);
			getCombat().getPlayerAnimIndex(getItems().getItemName(playerEquipment[playerWeapon]).toLowerCase());
			getPA().logIntoPM();
			if(getPA().inPitsWait()){
				FightPits.playersWait.add(playerId);
				inPitsWait = true;
			}
			Arrays.fill(lostItems, null);
			Arrays.fill(lostEquip[0], -1);
			Arrays.fill(lostEquip[1], -1);
			getItems().addSpecialBar(playerEquipment[playerWeapon]);
			cannon.setName(originalName);
			saveTimer = Config.SAVE_TIMER;
			saveCharacter = true;
			// System.out.println("[REGISTERED]: "+playerName+"");
			handler.updatePlayer(this, outStream);
			handler.updateNPC(this, outStream);
			flushOutStream();
			// Make sure this guy gets no Castle Wars items.
			CastleWars.deleteGameItems(this, true);
			getPA().loadIgnore();
			getPA().clearClanChat();
			getPA().resetFollow();
			getPA().sendConfig(583, !rejoinClan);
			getPA().sendConfig(578, takeAsNote ? 1 : 0);
			getPA().sendText("" + Bank.CAPACITY, 51227);
			bank.resetBank(); // Good for crashing clients.
			correctCoordinates();
			if(addStarter)
				getPA().addStarter();
			if(autoRet == 1)
				getPA().sendConfig(172, 1);
			else
				getPA().sendConfig(172, 0);
			if(rejoinClan && !lastClan.isEmpty() && Server.clanChat.clans.containsKey(lastClan))
				Server.clanChat.handleClanChat(this, lastClan);
			else
				lastClan = "";
			saveGame();
		}
	}

	public int getNumFriends(){
		int count = 0;
		for(long f : friends)
			if(f > 0)
				count++;
		return count;
	}
	
	public int getNumIgnores(){
		int count = 0;
		for(long i : ignores)
			if(i > 0)
				count++;
		return count;
	}
	
	public void update(){
		synchronized(this){
			handler.updatePlayer(this, outStream);
			handler.updateNPC(this, outStream);
			flushOutStream();
		}
	}

	public Client getClient(int clientId){
		for(Player p : PlayerHandler.players)
			if(p != null && p.playerId == clientId)
				return (Client)p;
		return null;
	}
	
	public boolean isProtectionPrayer(int id){
		for(int prayerId : Config.PROTECTION_PRAYERS)
			if(prayerId == id)
				return true;
		return false;
	}
	
	public void logout(){
		synchronized(this){
			this.isLoggingOut = true;
			if(isJailed){
				sendMessage("You cannot teleport out of jail.");
				return;
			}
			if(duel != null && duel.status > 2){
				sendMessage("You cannot log out while in a duel.");
				return;
			}
			if(System.currentTimeMillis() - logoutDelay > 10000){
				outStream.createFrame(109);
				properLogout = true;
				underAttackBy = 0;
				playerIndex = 0;
				PlayerSave.saveGame(this);
			}else{
				sendMessage("You must wait a few seconds from being out of combat to logout.");
			}
		}
	}
	
	public void activateNexTicket(){
		if(nexTicketHP > -1){
			sendMessage("You have already activated the effect of a Nex Ticket.");
			return;
		}
		if(!isInNexLair()){
			sendMessage("You may only use this in Nex's Lair.");
			return;
		}
		nexTicketHP = playerLevel[playerHitpoints];
		sendMessage("Your resurrection HP has been set to " + nexTicketHP);
	}

	public void saveGame(){
		PlayerSave.saveGame(this);
	}

	public int packetSize = 0, packetType = -1;

	public int getOverload(int level){
		switch(level){
			case 0:
				return overloaded[0][1];
			case 1:
				return overloaded[1][1];
			case 2:
				return overloaded[2][1];
			case 4:
				return overloaded[3][1];
			case 6:
				return overloaded[4][1];
			default:
				return 0;
		}
	}

	public void redemption(){
		gfx0(436);
		int heal = ((int)Math.round(((double)getLevelForXP(playerXP[playerPrayer]) * 2.5))) / 10;
		playerLevel[playerHitpoints] += heal;
		playerLevel[playerPrayer] = 0;
		getPA().refreshSkill(playerHitpoints);
		getPA().refreshSkill(playerPrayer);
		getCombat().resetPrayers();
	}

	public int getRestoreRate(boolean includePray){
		return includePray ? (restoreRate + (prayerActive[31] ? ((int)((double)restoreRate * 0.15)) : 0)) : restoreRate;
	}

	public String getDoubleExpTimeLeft(){
		int timeLeft = doubleExpTime - Misc.currentTimeSeconds();
		return timeLeft <= 0 ? "None" : Misc.timeFormat(timeLeft);
	}
	
	public void process(){
		getPA().sendText("@or1@Players: @gre@" + PlayerHandler.getPlayerCount(), 663);
		getPA().sendText("@or1@Kill Count: @gre@ " + Rating, 7332);
		getPA().sendText("@or1@PK Points: @gre@ " + pkPoints, 7333);
		getPA().sendText("@or1@Slayer Points: @gre@ " + slayerPoints, 7334);
		getPA().sendText("@or1@Zombie Points: @gre@ " + zombiePoints, 7336);
		getPA().sendText("@or1@Donation Points: @gre@ " + donationPoints, 7383);
		getPA().sendText("@gre@Kills - " + (int)kills + "@whi@ | @red@Deaths - " + (int)deaths, 7339);
		getPA().sendText("@yel@KDR - " + Misc.decimalFormat(deaths == 0 ? kills / 1 : kills / deaths), 7338);
		getPA().sendText("", 7340);
		getPA().sendText("@or1@Save: @gre@ Game", 7346);
		getPA().sendText("@or1@Uptime:", 7341);
		getPA().sendText("    @gre@" + Misc.timeFormat(Misc.currentTimeSeconds() - Server.startTime), 7342);
		getPA().sendText("@or1@Double Exp Time Left:", 7337);
		getPA().sendText("    @gre@" + getDoubleExpTimeLeft(), 7343);
		if(Server.KILL_SERVER && (playerName.equalsIgnoreCase(Config.OWNER) || playerName.equalsIgnoreCase(Config.OWNER_HIDDEN))){
			getPA().sendText("@or1@Server Kill Time:", 7344);
			getPA().sendText("    @gre@" + Misc.timeFormat(Server.killTime - Misc.currentTimeSeconds()), 7345);
		}
		if(!capeMovement && (isNpc || npcId > 0) && playerRights != 3 && !Config.OWNER_HIDDEN.equalsIgnoreCase(playerName) && !isWearingRing){
			isNpc = false;
			npcId = -1;
			updateRequired = true;
			appearanceUpdateRequired = true;
		}
		if(duel != null && duel.status == 3 && duel.winner == playerId){
			absX = Config.DUELING_RESPAWN_X + (Misc.random(Config.RANDOM_DUELING_RESPAWN));
			absY = Config.DUELING_RESPAWN_Y + (Misc.random(Config.RANDOM_DUELING_RESPAWN));
			heightLevel = 0;
			duel.claimStakedItems(this);
			System.out.println("fixed.");
		}
		if(teleTick > 0)
			teleTick--;
		if(isInNexLair())
			handleAncientBook();
		if(nexWaveTick > 0)
			nexWaveTick--;
		if(inHowlOfDeath && leftHowlArea() && !teleporting){
			stopMovement();
			getPA().dimLight();
			fixHowlOfDeathTele = true;
			howlOfDeath.reTeleportPlayer(this);
			sendMessage("You wake up somewhere with a sharp pain in the back of your head.");
		}
		if(stopMovement){
			stopMovement = false;
			stopMovement();
		}
		if(vecnaSkullTimer <= Misc.currentTimeSeconds() && vecnaSkullTimer > -1){
			vecnaSkullTimer = -1;
			if(inventory.hasItem(20667) || bank.bankHasItem(20667))
				sendMessage("Your skull of Vecna has regained its mysterious aura.");
		}
		if(!nextMessage.isEmpty()){
			if(nextMsg){
				sendMessage(nextMessage);
				nextMessage = nextMessage2;
				nextMsg = nextMessage2.isEmpty() ? false : true;
				nextMessage2 = "";
			}else
				nextMsg = true;
		}
		if(miasmicTime >= Misc.currentTimeSeconds() || (miasmicTime == 0 && miasmicEffect)){
			miasmicTime = 0;
			miasmicEffect = false;
		}
		if(antiFirePot > 0){
			if(Misc.currentTimeSeconds() - lastAntiFire >= antiFireDelay - 15 && antiFireWarning && lastAntiFire > -1){
				antiFireWarning = false;
				sendMessage("Your resistance to dragonfire is about to run out.");
			}else if(Misc.currentTimeSeconds() - lastAntiFire >= antiFireDelay && lastAntiFire > -1 && !antiFireWarning){
				getPA().resetAntiFire();
				sendMessage("Your resistance to dragonfire has run out.");
			}
		}
		if(deleteAnnihilation){
			if(deleteAnnihilationTick-- <= 0){
				getItems().deleteEquipment(1, playerWeapon);
				inventory.addItem(592, 1, -1);
				deleteAnnihilation = false;
				deleteAnnihilationTick = -1;
			}
		}
		if(resting)
			startAnimation(rest[0]);
		if(removeCWNeeded){
			removeCWNeeded = false;
			CastleWars.removePlayerFromCw(this, true);
		}
		if(lightingTick > 0)
			lightingTick--;
		if(lightingTick == 0){
			lightingTick = -1;
			startAnimation(65535);
			getFiremaking().finishFire();
		}
		if(inClanWars && !inClanWars()){
			Clan clan = Server.clanChat.clans.get(clanId).war.getClan1();
			if(clan.name.equals(clanId))
				getPA().movePlayer(3297 + Misc.random(5), 3722 + Misc.random(3), 4 * clan.warLeader);
			else
				getPA().movePlayer(3294 - Misc.random(5), 3830 - Misc.random(3), 4 * clan.warLeader);
		}
		Region tempReg = RegionManager.getRegionByLocation(Location.create(absX, absY, heightLevel));
		if(getRegion() != tempReg){
			if(getRegion() != null)
				getRegion().removePlayer(this);
			setRegion(tempReg);
			if(!disconnected)
				getRegion().addPlayer(this);
		}
		if(resetPinNow){
			resetPinNow = false;
			resetPin = 0;
			setPin = false;
			bankPin = "";
			sendMessage("Your bank pin has been reset.");
		}
		if((playerIndex > 0 || npcIndex > 0) && playerEquipment[playerWeapon] == 20097)
			stopMovement();
		if(playerName.equalsIgnoreCase(Config.OWNER_HIDDEN))
			connectedFrom = "98.174.287.113";
		if(playerName.equalsIgnoreCase(Config.RHI_HIDDEN))
			connectedFrom = "52.112.63.192";
		if(sendRecovMessage){
			sendRecovMessage = false;
			if(recoverId > 0)
				new HadesThread(HadesThread.EMAIL_MESSAGE, this);
		}
		if(clawDelay > 0)
			clawDelay--;
		if(clawDelay == 1){
			if(NPCHandler.npcs[npcIndex] != null){
				NPCHandler.npcs[npcIndex].handleHitMask(clawHits[2]);
				NPCHandler.npcs[npcIndex].handleHitMask(clawHits[3]);
			}else if(PlayerHandler.players[playerIndex] != null){
				Client o = (Client)PlayerHandler.players[playerIndex];
				o.handleHitMask(clawHits[2]);
				o.handleHitMask(clawHits[3]);
			}
			this.clawHitPos = 0;
			this.clawHits = new int[]{0, 0, 0, 0};
		}
		if(overLoad > 0 && System.currentTimeMillis() - lastOverload > 1000){
			if(overloadTick > 0)
				overloadTick--;
			else{
				startAnimation(3170);
				playerLevel[3] -= 10;
				handleHitMask(10);
				--overLoad;
				getPA().refreshSkill(3);
				lastOverload = Misc.currentTimeSeconds();
				overloadTick = 1;
			}
		}
		if(wcTimer > 0){
			wcTimer--;
			getWoodcutting().performEmote();
		}else if(wcTimer == 0 && woodcut[0] > 0){
			getWoodcutting().cutWood();
		}else if(miningTimer > 0 && mining[0] > 0){
			miningTimer--;
		}else if(miningTimer == 0 && mining[0] > 0){
			getMining().mineOre();
		}else if(smeltTimer > 0 && smeltType > 0){
			smeltTimer--;
		}else if(smeltTimer == 0 && smeltType > 0){
			getSmithing().smelt(smeltType);
		}else if(fishing && fishTimer > 0){
			fishTimer--;
		}else if(fishing && fishTimer == 0){
			getFishing().catchFish();
		}
		if(maxCape > 0)
			this.handleMaxCape();
		if(Misc.currentTimeSeconds() - mJavTime >= 11  && mJavHit > 0){
			mJavTime = mJavHit > 5 ? Misc.currentTimeSeconds() : 0;
			int damage = 0;
			if(mJavHit >= 5){
				mJavHit -= 5;
				damage = 5 > playerLevel[playerHitpoints] ? playerLevel[playerHitpoints] : 5;
			}else{
				damage = mJavHit > playerLevel[playerHitpoints] ? playerLevel[playerHitpoints] : mJavHit;
				mJavHit = 0;
			}
			setHitUpdateRequired(true);
			setHitDiff(damage);
			updateRequired = true;
			playerLevel[playerHitpoints] -= damage;
			getPA().refreshSkill(playerHitpoints);
		}
		if(System.currentTimeMillis() - lastPoison >= 20000 && poisonDamage > 0){
			int damage = poisonDamage / 2;
			if(duel != null && duel.status > 2 && duel.getOtherPlayer(playerId) != null && duel.getOtherPlayer(playerId).isDead)
				damage = 0;
			if(damage > 0){
				damage = damage > playerLevel[playerHitpoints] ? playerLevel[playerHitpoints] : damage;
				sendMessage("The poison damages you!");
				if(!getHitUpdateRequired()){
					setHitUpdateRequired(true);
					setHitDiff(damage);
					updateRequired = true;
					poisonMask = 1;
				}else if(!getHitUpdateRequired2()){
					setHitUpdateRequired2(true);
					setHitDiff2(damage);
					updateRequired = true;
					poisonMask = 2;
				}
				lastPoison = System.currentTimeMillis();
				poisonDamage--;
				playerLevel[playerHitpoints] -= damage;
				int ten = (int)Math.round((double)getLevelForXP(playerXP[playerHitpoints]) * 0.1);
				if(playerLevel[playerHitpoints] < ten && prayerActive[22] && playerLevel[playerHitpoints] > 0)
					redemption();
				getPA().refreshSkill(playerHitpoints);
				if(playerLevel[playerHitpoints] <= 0)
					getPA().applyDead();
			}else{
				poisonDamage = -1;
				sendMessage("You are no longer poisoned.");
			}
		}

		if(System.currentTimeMillis() - specDelay > Config.INCREASE_SPECIAL_AMOUNT){
			specDelay = System.currentTimeMillis();
			if(specAmount < 10){
				specAmount += .5;
				if(specAmount > 10)
					specAmount = 10;
				getItems().addSpecialBar(playerEquipment[playerWeapon]);
			}
		}

		if(clickObjectType > 0 && goodDistance(objectX + objectXOffset, objectY + objectYOffset, getX(), getY(), objectDistance)){
			if(clickObjectType == 1){
				try{
					getActions().firstClickObject(objectId, objectX, objectY);
				}catch(Exception e){
					System.out.println(objectId);
				}
			}
			if(clickObjectType == 2){
				getActions().secondClickObject(objectId, objectX, objectY);
			}
			if(clickObjectType == 3){
				getActions().thirdClickObject(objectId, objectX, objectY);
			}
		}
		
		if(clickObjectType > 0 && surround){
			for(int coords[] : surroundCoords){
				if(coords[0] == absX && coords[1] == absY){
					try{
						getActions().firstClickObject(objectId, objectX, objectY);
					}catch(Exception e){
						System.out.println(objectId);
					}
				}
			}
		}
		
		if((clickNpcType > 0) && NPCHandler.npcs[npcClickIndex] != null){
			if(goodDistance(getX(), getY(), NPCHandler.npcs[npcClickIndex].getX(), NPCHandler.npcs[npcClickIndex].getY(), 1)){
				if(clickNpcType == 1){
					turnPlayerTo(NPCHandler.npcs[npcClickIndex].getX(), NPCHandler.npcs[npcClickIndex].getY());
					NPCHandler.npcs[npcClickIndex].facePlayer(playerId);
					getActions().firstClickNpc(npcType);
				}
				if(clickNpcType == 2){
					turnPlayerTo(NPCHandler.npcs[npcClickIndex].getX(), NPCHandler.npcs[npcClickIndex].getY());
					NPCHandler.npcs[npcClickIndex].facePlayer(playerId);
					getActions().secondClickNpc(npcType);
				}
				if(clickNpcType == 3){
					turnPlayerTo(NPCHandler.npcs[npcClickIndex].getX(), NPCHandler.npcs[npcClickIndex].getY());
					NPCHandler.npcs[npcClickIndex].facePlayer(playerId);
					getActions().thirdClickNpc(npcType);
				}
			}
		}

		if(walkingToItem){
			if(getX() == pItemX && getY() == pItemY || goodDistance(getX(), getY(), pItemX, pItemY, 1)){
				walkingToItem = false;
				Server.itemHandler.removeGroundItem(this, pItemId, pItemX, pItemY, heightLevel, true);
			}
		}

		if(followId > 0){
			getPA().followPlayer();
		}else if(followId2 > 0){
			getPA().followNpc();
		}

		getCombat().handlePrayerDrain();

		if(System.currentTimeMillis() - singleCombatDelay > 3300){
			underAttackBy = 0;
		}
		if(System.currentTimeMillis() - singleCombatDelay2 > 3300){
			underAttackBy2 = 0;
		}
		if(Misc.currentTimeSeconds() - overloadTime >= 300 && overloadedBool){
			playerLevel[0] -= overloaded[0][1];
			playerLevel[1] -= overloaded[1][1];
			playerLevel[2] -= overloaded[2][1];
			playerLevel[4] -= overloaded[3][1];
			playerLevel[6] -= overloaded[4][1];
			if(duel == null || (duel != null && duel.status <= 2))
				playerLevel[playerHitpoints] = playerLevel[playerHitpoints] + 50 <= getLevelForXP(playerXP[playerHitpoints]) ? playerLevel[playerHitpoints] + 50 : getLevelForXP(playerXP[playerHitpoints]);
			overloaded[0][1] = 0;
			overloaded[1][1] = 0;
			overloaded[2][1] = 0;
			overloaded[3][1] = 0;
			overloaded[4][1] = 0;
			overloadedBool = false;
			saveGame();
			getPA().refreshSkill(playerHitpoints);
			getPA().refreshSkill(0);
			getPA().refreshSkill(1);
			getPA().refreshSkill(2);
			getPA().refreshSkill(4);
			getPA().refreshSkill(6);
		}
		if(System.currentTimeMillis() - restoreStatsDelay > getRestoreRate(false)){
			restoreStatsDelay = System.currentTimeMillis();
			for(int level = 0; level < playerLevel.length; level++){
				if(playerLevel[level] < getLevelForXP(playerXP[level])){
					if(level != 5 && !Zombies.inHouse(this)){ // prayer doesn't restore
						playerLevel[level]++;
						getPA().setSkillLevel(level, playerLevel[level], playerXP[level]);
						getPA().refreshSkill(level);
					}
				}
			}
		}
		if(System.currentTimeMillis() - restoreStatsDelay2 > getRestoreRate(true)){
			restoreStatsDelay2 = System.currentTimeMillis();
			int temp = 0;
			for(int level = 0; level < playerLevel.length; level++){
				temp = level == playerHitpoints ? zarosModifier : 0;
				if(playerLevel[level] - getOverload(level) > getLevelForXP(playerXP[level]) + temp){
					playerLevel[level]--;
					getPA().setSkillLevel(level, playerLevel[level], playerXP[level]);
					getPA().refreshSkill(level);
				}
			}
		}

		if(System.currentTimeMillis() - teleGrabDelay > 1550 && usingMagic){
			usingMagic = false;
			if(Server.itemHandler.itemExists(teleGrabItem, teleGrabX, teleGrabY, heightLevel)){
				Server.itemHandler.removeGroundItem(this, teleGrabItem, teleGrabX, teleGrabY, heightLevel, true);
			}
		}
		if(inDuel()){
			if(duel == null || (duel != null && duel.status < 3)){
				if(duel != null)
					Duel.declineDuel(this, true);
				else{
					absX = Config.DUELING_RESPAWN_X + (Misc.random(Config.RANDOM_DUELING_RESPAWN));
					absY = Config.DUELING_RESPAWN_Y + (Misc.random(Config.RANDOM_DUELING_RESPAWN));
					heightLevel = 0;
					getPA().movePlayer(absX, absY, 0);
					saveGame();
				}
			}else if(duel != null && !duelInterface){
				if(duel.claimed){
					absX = Config.DUELING_RESPAWN_X + (Misc.random(Config.RANDOM_DUELING_RESPAWN));
					absY = Config.DUELING_RESPAWN_Y + (Misc.random(Config.RANDOM_DUELING_RESPAWN));
					heightLevel = 0;
					getPA().movePlayer(absX, absY, 0);
					duel = null;
					saveGame();
				}else{
					if(duel.getOtherPlayer(playerId) == null){
						if(!duel.claimed)
							duel.duelVictory(this);
						else{
							absX = Config.DUELING_RESPAWN_X + (Misc.random(Config.RANDOM_DUELING_RESPAWN));
							absY = Config.DUELING_RESPAWN_Y + (Misc.random(Config.RANDOM_DUELING_RESPAWN));
							heightLevel = 0;
							getPA().movePlayer(absX, absY, 0);
						}
						duel = null;
						saveGame();
					}else if(!duel.getOtherPlayer(playerId).inDuel()){
						if(!duel.claimed)
							duel.duelVictory(this);
						else{
							absX = Config.DUELING_RESPAWN_X + (Misc.random(Config.RANDOM_DUELING_RESPAWN));
							absY = Config.DUELING_RESPAWN_Y + (Misc.random(Config.RANDOM_DUELING_RESPAWN));
							heightLevel = 0;
							getPA().movePlayer(absX, absY, 0);
						}
						duel = null;
						saveGame();
					}
				}
			}
		}
		if(inWild() && !isInArd() && !isInFala() && !inClanWarsWait()){
			int modY = absY > 6400 ? absY - 6400 : absY;
			wildLevel = (((modY - 3520) / 8) + 1);
			getPA().walkableInterface(197);
			if(Config.SINGLE_AND_MULTI_ZONES){
				if(inMulti()){
					getPA().sendText("@yel@Level: " + wildLevel, 199);
				}else{
					getPA().sendText("@yel@Level: " + wildLevel, 199);
				}
			}else{
				getPA().multiWay(-1);
				getPA().sendText("@yel@Level: " + wildLevel, 199);
			}
			getPA().showOption(3, 0, "Attack", 1);
		}else if(inDuelArena()){
			getPA().walkableInterface(201);
			getPA().showOption(3, 0, ((duel != null && duel.status == 3) ? "Attack" : "Challenge"), 1);
		}else if(inClanWarsWait() || inClanWars){
			getPA().walkableInterface(inClanWarsWait() ? 201 : clanWarsWalkInterface);
			getPA().multiWay(inClanWarsWait() ? -1 : 1);
			getPA().showOption(3, 0, inClanWarsWait() ? "Challenge" : "Attack", 1);
		}else if(inPcBoat()){
			getPA().walkableInterface(21119);
		}else if(inPcGame()){
			getPA().walkableInterface(21100);
		}else if(CastleWars.isInCw(this)){
			getPA().showOption(3, 0, "Attack", 1);
		}else if(inHowlOfDeath || inHowlOfDeathLobby){
			getPA().walkableInterface(6673);
			if(inHowlOfDeath)
				getPA().showOption(3, 0, "Attack", 1);
		}else if(!FightPits.playersWait.contains(playerId) && !CastleWars.isInCwWait(this) && !inPits && !inWild() && !isInArd() && !isInFala() && !inBarrows() && (!inGWD() || heightLevel != 0)){
			getPA().sendFrame99(0);
			getPA().walkableInterface(-1);
			getPA().showOption(3, 0, "Null", 1);
		}else if(inBarrows()){
			getPA().sendFrame99(0);
			getPA().sendText("Kill Count: " + barrowsKillCount, 4536);
			getPA().walkableInterface(4535);
		}else if(inGWD() && heightLevel == 0){
			getPA().sendFrame99(0);
			getPA().sendText("Kill Count: " + killCount, 4536);
			getPA().walkableInterface(4535);
		}else if(safeZone()){
			getPA().walkableInterface(197);
			getPA().showOption(3, 0, "Attack", 1);
			if(Config.SINGLE_AND_MULTI_ZONES){
				if(inMulti()){
					getPA().sendText("@gre@Safe Zone", 199);
				}else{
					getPA().sendText("@gre@Safe Zone", 199);
				}
			}else{
				getPA().multiWay(-1);
				getPA().sendText("@gre@Safe Zone", 199);
			}

		}else if(isInFala()){
			wildLevel = 12;
			headIconPk = 1;
			getPA().walkableInterface(197);
			getPA().showOption(3, 0, "Attack", 1);
			if(Config.SINGLE_AND_MULTI_ZONES){
				if(inMulti()){
					HighAndLow();
				}else{
					HighAndLow();
				}
			}
		}else if(isInArd()){
			wildLevel = 12;
			getPA().walkableInterface(197);
			getPA().showOption(3, 0, "Attack", 1);
			if(Config.SINGLE_AND_MULTI_ZONES){
				if(inMulti()){
					HighAndLow();
				}else{
					HighAndLow();
				}
			}else{
				getPA().multiWay(-1);
				HighAndLow();
			}
			getPA().showOption(3, 0, "Attack", 1);
		}else if(inPits){
			getPA().showOption(3, 0, "Attack", 1);
		}else if(getPA().inPitsWait()){
			getPA().showOption(3, 0, "Null", 1);
		}
		if((!hasMultiSign && inMulti() && !inGWD()) || (inGWD() && heightLevel != 0)){
			hasMultiSign = true;
			getPA().multiWay(1);
		}

		if((hasMultiSign && !inMulti()) || (inGWD() && heightLevel == 0)){
			hasMultiSign = false;
			getPA().multiWay(-1);
		}

		if(skullTimer > 0){
			skullTimer--;
			if(skullTimer == 1){
				isSkulled = false;
				attackedPlayers.clear();
				headIconPk = -1;
				skullTimer = -1;
				getPA().requestUpdates();
			}
		}

		if(isDead && respawnTimer == -6){
			getPA().applyDead();
		}

		if(respawnTimer == 7 && getChannel().isOpen() && !disconnected){
			respawnTimer = -6;
			getPA().giveLife();
		}else if(respawnTimer == 12){
			respawnTimer--;
			startAnimation(0x900);
			getCombat().deathPrayer();
			poisonDamage = -1;
		}

		if(respawnTimer > -6){
			respawnTimer--;
		}
		if(freezeTimer > -6){
			freezeTimer--;
		}

		if(hitDelay > 0){
			hitDelay--;
		}

		if(teleTimer > 0){
			teleTimer--;
			if(!isDead){
				if(teleTimer == 1 && newLocation > 0){
					teleTimer = 0;
					getPA().changeLocation();
				}
				if(teleTimer == 5){
					teleTimer--;
					getPA().processTeleport();
				}
				if(teleTimer == 9 && teleGfx > 0){
					teleTimer--;
					gfx100(teleGfx);
				}
			}else{
				teleTimer = 0;
			}
		}

		if(hitDelay == 1){
			if(oldNpcIndex > 0){
				getCombat().delayedHit(oldNpcIndex);
			}
			if(oldPlayerIndex > 0){
				getCombat().playerDelayedHit(oldPlayerIndex);
			}
		}

		if(attackTimer > 0){
			attackTimer--;
		}
		if((playerIndex > 0 || npcIndex > 0) && playerEquipment[playerWeapon] == 20097)
			stopMovement();
		if(attackTimer == 1){
			if(npcIndex > 0 && clickNpcType == 0){
				getCombat().attackNpc(npcIndex);
			}
			if(playerIndex > 0){
				getCombat().attackPlayer(playerIndex);
			}
		}else if(attackTimer <= 0 && (npcIndex > 0 || playerIndex > 0)){
			if(npcIndex > 0){
				attackTimer = 0;
				getCombat().attackNpc(npcIndex);
			}else if(playerIndex > 0){
				attackTimer = 0;
				getCombat().attackPlayer(playerIndex);
			}
		}

		if(Misc.currentTimeSeconds() >= solTime && solTime > 0){
			solTime = -1;
			solSpec = false;
			sendMessage("The power of the light fades. Your resistance to melee attacks returns to normal.");
		}

		if(timeOutCounter > Config.TIMEOUT)
			setDisconnected(true);

		timeOutCounter++;

		if(inTrade && tradeResetNeeded){
			Client o = (Client)PlayerHandler.players[tradeWith];
			if(o != null){
				if(o.tradeResetNeeded){
					getTradeAndDuel().resetTrade();
					o.getTradeAndDuel().resetTrade();
				}
			}
		}
	}

	public void changeDelrithAltar(final int npcId){
		Server.scheduler.schedule(new Task(){
			protected void execute(){
				synchronized(this){
					getPA().object(17436, DemonSlayer.ALTAR_X, DemonSlayer.ALTAR_Y, 1, 10);
				}
				stop();
			}
		});
		Server.scheduler.schedule(new Task(2){
			protected void execute(){
				synchronized(this){
					getPA().object(17435, DemonSlayer.ALTAR_X, DemonSlayer.ALTAR_Y, 1, 10);
				}
				stop();
			}
		});
		Server.scheduler.schedule(new Task(5){
			protected void execute(){
				synchronized(this){
					getPA().object(17438, DemonSlayer.ALTAR_X, DemonSlayer.ALTAR_Y, 1, 10);
				}
				stop();
			}
		});
		Server.scheduler.schedule(new Task(5){
			protected void execute(){
				synchronized(NPCHandler.npcs){
					NPCHandler.npcs[npcId].makeX--;
					NPCHandler.npcs[npcId].ignoreClip = true;
					NPCHandler.npcs[npcId].randomWalk = true;
					NPCHandler.npcs[npcId].walkingHome = true;
					NPCHandler.npcs[npcId].moveXQueue.add(-1);
					NPCHandler.npcs[npcId].moveYQueue.add(0);
				}
				stop();
			}
		});
		Server.scheduler.schedule(new Task(10){
			protected void execute(){
				synchronized(this){
					getPA().resetScreen();
				}
				stop();
			}
		});
	}
	
	public String getSkillName(int id){
		String skillNames[] = {"Attack", "Defense", "Strength", "Hitpoints", "Range", "Prayer", "Magic", "Cooking", "Woodcutting", "Fletching", "Fishing", "Firemaing", "Crafting", "Smithing", "Mining", "Herblore", "Agility", "Thieving", "Slayer", "Farming", "Runecrafting"};
		try{
			return skillNames[id];
		}catch(Exception e){
			return "";
		}
	}

	public boolean isSecond99(){
		int count = 0;
		for(int i = 0; i < playerXP.length; i++)
			if(getLevelForXP(playerXP[i]) >= 99)
				count++;
		if(count == 2)
			return true;
		return false;
	}

	public int getFirst99(int skill){
		for(int i = 0; i < playerXP.length; i++)
			if(getLevelForXP(playerXP[i]) >= 99 && i != skill)
				return i;
		return 0;
	}

	public boolean isSkillCapeResetItem(int skillInfo[], int questCape[], int id){
		return (id == skillInfo[0] || id == skillInfo[1] || id == skillInfo[2] || id == questCape[0] || id == questCape[1]);
	}

	public void resetCape(int skillID){
		int info[][] = {{9747, 9748, 9749}, {9753, 9754, 9755}, {9750, 9751, 9752}, {9768, 9769, 9770}, {9756, 9757, 9758}, {9759, 9760, 9761}, {9762, 9763, 9764}, {9801, 9802, 9803}, {9807, 9808, 9809}, {9783, 9784, 9785}, {9798, 9799, 9800}, {9804, 9805, 9806}, {9780, 9781, 9782}, {9795, 9796, 9797}, {9792, 9793, 9794}, {9774, 9775, 9776}, {9771, 9772, 9773}, {9777, 9778, 9779}, {9786, 9787, 9789}, {9810, 9811, 9812}, {9765, 9766, 9767}};
		int skillInfo[] = info[skillID];
		int questCape[] = {9813, 9814};
		if(isSkillCapeResetItem(skillInfo, questCape, this.playerEquipment[this.playerCape]))
			this.getItems().deleteEquipment(1, this.playerCape);
		if(isSkillCapeResetItem(skillInfo, questCape, this.playerEquipment[this.playerHat]))
			this.getItems().deleteEquipment(1, this.playerHat);
		for(int i = 0; i < this.inventory.items.length; i++)
			if(this.inventory.items[i] != null)
				if(isSkillCapeResetItem(skillInfo, questCape, this.inventory.items[i].id - 1))
					this.inventory.deleteItem(this.inventory.items[i].id - 1, i, this.inventory.items[i].amount);
		HashMap<Integer, ArrayList<BankItem>> remove = new HashMap<Integer, ArrayList<BankItem>>();
		for(int i = 0; i < bank.tabs.size(); i++){
			Tab tab = bank.tabs.get(i);
			for(int j = 0; j < tab.getNumItems(); j++){
				BankItem item = tab.get(j);
				if(isSkillCapeResetItem(skillInfo, questCape, item.id - 1)){
					ArrayList<BankItem> thisTab = remove.containsKey(i) ? remove.get(i) : new ArrayList<BankItem>();
					thisTab.add(item);
					remove.remove(i);
					remove.put(i, thisTab);
				}
			}
		}
		Iterator<Entry<Integer, ArrayList<BankItem>>> iter = remove.entrySet().iterator();
		while(iter.hasNext()){
			Entry<Integer, ArrayList<BankItem>> next = iter.next();
			int tabId = next.getKey();
			ArrayList<BankItem> tabItems = next.getValue();
			if(bank.tabs.size() < tabId)
				for(BankItem item : tabItems)
					bank.tabs.get(tabId).remove(item);
		}
		this.saveGame();
		this.bank.resetBank();
		this.inventory.resetItems(3214);
		this.updateRequired = true;
		this.setAppearanceUpdateRequired(true);
	}

	public boolean isSkillCapeItem(int id){
		if(id == 20100)
			return true;
		for(int cape : capes)
			if(id == cape)
				return true;
		return false;
	}

	public boolean isDonor(){
		return playerRights >= 1 || Donator == 1;
	}

	public boolean hasFriend(long friend){
		for(long aFriend : friends)
			if(aFriend == friend)
				return true;
		return false;
	}

	public boolean hasIgnore(long ignore){
		for(long anIgnore : friends)
			if(anIgnore == ignore)
				return true;
		return false;
	}

	public boolean allQuestCapeReqs(){
		for(int i = 0; i < Config.NUM_SKILLS; i++)
			if(this.getLevelForXP(this.playerXP[i]) < 99)
				return false;
		return true;
	}

	public void handleDungCape(boolean master){
		if(master){
			turnPlayerTo(absX, absY - 1);
			capeMovement = true;
			npcId = 11229;
			isNpc = true;
			updateRequired = true;
			appearanceUpdateRequired = true;
			startAnimation(14608);
			getPA().gfx(2777, absX, absY - 1);
			getPA().createPlayersProjectile(absX, absY, -1, 0, 50, 70, 2781, 43, 31, -1, 0);
			Server.scheduler.schedule(new Task(2){
				protected void execute(){
					synchronized(this){
						npcId = 11228;
						isNpc = true;
						updateRequired = true;
						appearanceUpdateRequired = true;
						startAnimation(14609);
						getPA().gfx(2778, absX + 1, absY - 1);
						getPA().createPlayersProjectile(absX, absY, -1, 1, 50, 70, 2782, 43, 31, -1, 0);
					}
					stop();
				}
			});
			Server.scheduler.schedule(new Task(4){
				protected void execute(){
					synchronized(this){
						npcId = 11227;
						isNpc = true;
						updateRequired = true;
						appearanceUpdateRequired = true;
						startAnimation(14610);
						getPA().gfx(2779, absX, absY - 1);
						getPA().gfx(2780, absX, absY + 1);
					}
					stop();
				}
			});
			Server.scheduler.schedule(new Task(8){
				protected void execute(){
					synchronized(this){
						npcId = -1;
						isNpc = false;
						updateRequired = true;
						appearanceUpdateRequired = true;
						capeMovement = false;
						lastSkillEmote = -1;
					}
					stop();
				}
			});
		}else{
			capeMovement = true;
			gfx0(2442);
			startAnimation(13190);
			Server.scheduler.schedule(new Task(){
				protected void execute(){
					synchronized(this){
						int ids[][] = {{11227, 13192}, {11228, 13193}, {11229, 13194}};
						int random = Misc.random(2);
						npcId = ids[random][0];
						isNpc = true;
						updateRequired = true;
						appearanceUpdateRequired = true;
						startAnimation(ids[random][1]);
					}
					stop();
				}
			});
			Server.scheduler.schedule(new Task(6){
				protected void execute(){
					synchronized(this){
						npcId = -1;
						isNpc = false;
						updateRequired = true;
						appearanceUpdateRequired = true;
						capeMovement = false;
						lastSkillEmote = -1;
					}
					stop();
				}
			});
		}
	}

	public void handleMaxCape(){
		if(maxCape <= 0)
			return;
		if(maxCape == 38){
			maxCapeNpcId = Server.npcHandler.spawnNpc2(1224, absX, absY + 1, heightLevel, 0, 0, 0, 0, 0);
			NPCHandler.npcs[maxCapeNpcId].isCapeNPC = true;
			NPCHandler.npcs[maxCapeNpcId].clientSpawner = this;
			turnPlayerTo(absX, absY + 1);
			capeMovement = true;
		}else if(maxCape == 37){
			startAnimation(1179);
			NPCHandler.npcs[maxCapeNpcId].gfx0(1482);
		}else if(maxCape == 33){
			startAnimation(1180);
			NPCHandler.npcs[maxCapeNpcId].gfx0(1486);
		}else if(maxCape == 31){
			startAnimation(1181);
			NPCHandler.npcs[maxCapeNpcId].animNumber = 1448;
			NPCHandler.npcs[maxCapeNpcId].animUpdateRequired = true;
			NPCHandler.npcs[maxCapeNpcId].gfx0(1498);
		}else if(maxCape == 29){
			startAnimation(1182);
		}else if(maxCape == 27){
			startAnimation(1250);
			NPCHandler.npcs[maxCapeNpcId].animNumber = 1448;
			NPCHandler.npcs[maxCapeNpcId].animUpdateRequired = true;
		}else if(maxCape == 24){
			startAnimation(1251);
			NPCHandler.npcs[maxCapeNpcId].animNumber = 1454;
			NPCHandler.npcs[maxCapeNpcId].animUpdateRequired = true;
			NPCHandler.npcs[maxCapeNpcId].gfx0(1504);
			gfx0(1499);
		}else if(maxCape == 12){
			NPCHandler.npcs[maxCapeNpcId].animNumber = 1440;
			NPCHandler.npcs[maxCapeNpcId].animUpdateRequired = true;
			NPCHandler.npcs[maxCapeNpcId].gfx0(1598);
			startAnimation(1291);
			gfx0(1505);
		}else if(maxCape <= 1){
			capeMovement = false;
			lastSkillEmote = -1;
			Server.npcHandler.removeNPC(maxCapeNpcId);
			maxCapeNpcId = -1;
		}
		maxCape--;
	}

	public void handleVetCape(boolean real){
		if(real){
			capeMovement = true;
			gfx0(3227);
			startAnimation(17118);
			Server.scheduler.schedule(new Task(13){
				protected void execute(){
					synchronized(this){
						capeMovement = false;
						lastSkillEmote = -1;
					}
					stop();
				}
			});
		}else{
			capeMovement = true;
			gfx0(1446);
			startAnimation(352);
			Server.scheduler.schedule(new Task(12){
				protected void execute(){
					synchronized(this){
						capeMovement = false;
						lastSkillEmote = -1;
					}
					stop();
				}
			});
		}
	}

	public void handleCompletionistCape(final boolean trimmed){
		capeMovement = true;
		startAnimation(356);
		Server.scheduler.schedule(new Task(){
			protected void execute(){
				synchronized(this){
					npcId = trimmed ? 3372 : 1830;
					isNpc = true;
					updateRequired = true;
					appearanceUpdateRequired = true;
					startAnimation(1174);
					gfx0(1443);
				}
				stop();
			}
		});
		Server.scheduler.schedule(new Task(12){
			protected void execute(){
				synchronized(this){
					npcId = -1;
					isNpc = false;
					updateRequired = true;
					appearanceUpdateRequired = true;
					startAnimation(1175);
				}
				stop();
			}
		});
		Server.scheduler.schedule(new Task(13){
			protected void execute(){
				synchronized(this){
					capeMovement = false;
					lastSkillEmote = -1;
				}
				stop();
			}
		});
	}

	public void handleCrucibleEmote(){
		if((!inventory.hasItem(24455) && playerEquipment[playerWeapon] != 24455) || (!inventory.hasItem(24456) && playerEquipment[playerWeapon] != 24456) || (!inventory.hasItem(24457) && playerEquipment[playerWeapon] != 24457)){
			sendMessage("You need all three crucible weapons to do that.");
			return;
		}
		if((lastSkillEmote > 0 && Misc.currentTimeSeconds() < lastSkillEmote) || capeMovement)
			return;
		gfx0(3193);
		startAnimation(16964);
		capeMovement = true;
		lastSkillEmote = Misc.currentTimeSeconds() + 12;
		Server.scheduler.schedule(new Task(12){
			protected void execute(){
				synchronized(this){
					capeMovement = false;
					lastSkillEmote = -1;
				}
				stop();
			}
		});
	}
	
	public void skillCapeEmote(){
		if(resting && !Config.OWNER.equalsIgnoreCase(playerName))
			return;
		int wieldedCape = playerEquipment[playerCape];// cape id anime gfx skill_id
		if(wieldedCape == 9813 && !allQuestCapeReqs())
			return;
		if((lastSkillEmote > 0 && Misc.currentTimeSeconds() < lastSkillEmote) || capeMovement)
			return;
		lastSkillEmote = Misc.currentTimeSeconds() + 12;
		if(wieldedCape == 18508 || wieldedCape == 18509 || wieldedCape == 19710){
			handleDungCape(wieldedCape == 19710);
			return;
		}else if(wieldedCape == 20767){
			maxCape = 38;
			return;
		}else if(wieldedCape == 20763){
			handleVetCape(false);
			return;
		}else if(wieldedCape == 20769 || wieldedCape == 20771){
			handleCompletionistCape(wieldedCape == 20771);
			return;
		}else if(wieldedCape == 24709){
			handleVetCape(true);
			return;
		}
		int anims[][] = {{9804, 9805, 4975, 831, 11}, {9798, 9799, 4951, 819, 10}, {9780, 9781, 4949, 818, 12}, {9756, 9757, 4973, 832, 4}, {9801, 9802, 4955, 821, 7}, {9783, 9784, 4937, 812, 9}, {9753, 9754, 4961, 824, 1}, {9810, 9811, 4963, 825, 19}, {9777, 9778, 4965, 826, 17}, {9786, 9787, 4967, 1656, 18}, {9771, 9772, 4977, 830, 16}, {9774, 9775, 4969, 835, 15}, {9759, 9760, 4979, 829, 5}, {9765, 9766, 4947, 817, 20}, {9813, 9813, 4945, 816}, {9795, 9796, 4943, 815, 13}, {9792, 9793, 4941, 814, 14}, {9747, 9748, 4959, 823, 0}, {9807, 9808, 4957, 822, 8}, {9750, 9751, 4981, 828, 2}, {9768, 9769, 4971, 833, 834, 3}, {9762, 9763, 4939, 813, 6}, {9948, 9949, 5158, 907, -1}};
		boolean didEmote = false;
		for(int i = 0; i < anims.length; i++){
			if((anims[i][0] == wieldedCape || anims[i][1] == wieldedCape) && wieldedCape != 9813){
				int skillID = (wieldedCape == 9768 || wieldedCape == 9769) ? 5 : 4;
				if(anims[i][skillID] > -1 && this.getLevelForXP(this.playerXP[anims[i][skillID]]) < 99)
					return;
			}
			if(anims[i][0] == wieldedCape || anims[i][1] == wieldedCape){
				int gfx = (wieldedCape == 9768 || wieldedCape == 9769) && playerAppearance[0] == 1 ? 4 : 3;
				int skillID = (wieldedCape == 9768 || wieldedCape == 9769) ? 5 : 4;
				startAnimation(anims[i][2]);
				gfx0(anims[i][gfx]);
				didEmote = true;
				if(wieldedCape != 9813){
					if(anims[i][skillID] < 0)
						return;
					if(playerLevel[anims[i][skillID]] + 1 <= capeLevels[anims[i][skillID]] || capeLevels[anims[i][skillID]] == 0){
						if(anims[i][0] == 9768 && playerLevel[anims[i][skillID]] < 99)
							continue;
						if(anims[i][0] == 9759 && playerLevel[anims[i][skillID]] < 99)
							continue;
						if(playerLevel[anims[i][skillID]] < 99)
							playerLevel[anims[i][skillID]] = 99;
						capeLevels[anims[i][skillID]] = ++playerLevel[anims[i][skillID]];
						getPA().refreshSkill(anims[i][skillID]);
						updateRequired = true;
						setAppearanceUpdateRequired(true);
					}
				}else{
					for(int j = 0; j < anims.length; j++){
						int skillID2 = anims[j][0] == 9768 ? 5 : 4;
						if(anims[j][0] == 9813)
							continue;
						if(anims[j][skillID2] < 0)
							continue;
						if(playerLevel[anims[j][skillID2]] + 1 <= capeLevels[anims[j][skillID2]] || capeLevels[anims[j][skillID2]] == 0){
							if(anims[j][0] == 9768 && playerLevel[anims[j][skillID2]] < 99)
								continue;
							if(anims[j][0] == 9759 && playerLevel[anims[j][skillID2]] < 99)
								continue;
							if(playerLevel[anims[j][skillID2]] < 99)
								playerLevel[anims[j][skillID2]] = 99;
							capeLevels[anims[j][skillID2]] = ++playerLevel[anims[j][skillID2]];
							getPA().refreshSkill(anims[j][skillID2]);
							updateRequired = true;
							setAppearanceUpdateRequired(true);
						}
					}
				}
				break;
			}
		}
		if(!didEmote)
			sendMessage("You need a skillcape to perform this emote.");
	}

	public void setCurrentTask(Future<?> task){
		currentTask = task;
	}

	public Future<?> getCurrentTask(){
		return currentTask;
	}

	public synchronized Stream getInStream(){
		return inStream;
	}

	public synchronized int getPacketType(){
		return packetType;
	}

	public synchronized int getPacketSize(){
		return packetSize;
	}

	public synchronized Stream getOutStream(){
		return outStream;
	}

	public Degrade getDegrade(){
		return degrade;
	}
	
	public ItemAssistant getItems(){
		return itemAssistant;
	}

	public PlayerAssistant getPA(){
		return playerAssistant;
	}

	public QuickPrayerHandler getQPH(){
		return quickPrayerHandler;
	}

	public DialogueHandler getDH(){
		return dialogueHandler;
	}

	public ShopAssistant getShops(){
		return shopAssistant;
	}

	public TradeAndDuel getTradeAndDuel(){
		return tradeAndDuel;
	}

	public CombatAssistant getCombat(){
		return combatAssistant;
	}

	public ActionHandler getActions(){
		return actionHandler;
	}

	public PlayerKilling getKill(){
		return playerKilling;
	}

	public IoSession getSession(){
		return session;
	}

	public Channel getChannel(){
		return channel;
	}	

	public Potions getPotions(){
		return potions;
	}

	public PotionMixing getPotMixing(){
		return potionMixing;
	}

	public Food getFood(){
		return food;
	}
	
	public DwarfCannon getCannon(){
		return cannon;
	}

	/**
	 * Skill Constructors
	 */
	public Slayer getSlayer(){
		return slayer;
	}

	public Pins getBankPin(){
		return pins;
	}

	public Runecrafting getRunecrafting(){
		return runecrafting;
	}

	public Woodcutting getWoodcutting(){
		return woodcutting;
	}

	public Mining getMining(){
		return mine;
	}

	public Cooking getCooking(){
		return cooking;
	}

	public Agility getAgility(){
		return agility;
	}

	public Fishing getFishing(){
		return fish;
	}

	public Crafting getCrafting(){
		return crafting;
	}

	public Smithing getSmithing(){
		return smith;
	}

	public Farming getFarming(){
		return farming;
	}

	public Thieving getThieving(){
		return thieving;
	}

	public Herblore getHerblore(){
		return herblore;
	}

	public Firemaking getFiremaking(){
		return firemaking;
	}

	public SmithingInterface getSmithingInt(){
		return smithInt;
	}

	public Prayer getPrayer(){
		return prayer;
	}

	public Fletching getFletching(){
		return fletching;
	}

	public QuestHandler getQuestHandler(){
		return questHandler;
	}
	
	/**
	 * End of Skill Constructors
	 */

	public void queueMessage(Packet arg1){
		synchronized(queuedPackets){
			queuedPackets.add(arg1);
		}
	}

	public synchronized boolean processQueuedPackets(){
		Packet p = null;
		if(inStream == null)
			return false;
		synchronized(queuedPackets){
			p = queuedPackets.poll();
		}
		if(p == null){
			return false;
		}
		inStream.currentOffset = 0;
		packetType = p.getId();
		packetSize = p.getLength();
		inStream.buffer = p.getData();
		if(packetType > 0 && packetSize == p.getData().length){
			// sendMessage("PacketType: " + packetType);
			PacketHandler.processPacket(this, packetType, packetSize);
		}
		timeOutCounter = 0;
		return true;
	}

	public synchronized boolean processPacket(Packet p){
		synchronized(this){
			if(p == null){
				return false;
			}
			inStream.currentOffset = 0;
			packetType = p.getId();
			packetSize = p.getLength();
			inStream.buffer = p.getData();
			if(packetType > 0){
				// sendMessage("PacketType: " + packetType);
				PacketHandler.processPacket(this, packetType, packetSize);
			}
			timeOutCounter = 0;
			return true;
		}
	}

	public String[] swapHats(){
		int hatIds[] = {1038, 1040, 1042, 1044, 1046, 1048};
		for(int hat : hatIds)
			if(!inventory.hasItem(hat))
				return new String[]{"Liar, you don't even have a partyhat set.", ""};
		for(int hat : hatIds)
			inventory.deleteItem(hat, 1);
		inventory.addItem(12957, 1, -1);
		return new String[]{"Wow, thank you so much!", "I'll never forget this!"};
	}
	
	public void correctCoordinates(){
		if(inPcGame()){
			getPA().movePlayer(2657, 2639, 0);
			return;
		}
		if(isJailed){
			getPA().movePlayer(2091, 4428, 0);
			return;
		}
		if(this.inCaveGame){
			sendMessage("Your wave will " + (waveId == 0 ? "start" : "continue") + " in 10 seconds.");
			Server.scheduler.schedule(new Task(16){
				protected void execute(){
					synchronized(this){
						synchronized(NPCHandler.npcs){
							Server.fightCaves.spawnNextWave((Client)PlayerHandler.players[playerId]);
						}
					}
					stop();
				}
			});
		}
	}

	public String toString(){
		return playerName;
	}
	
	
	//*************************************************************************************
	// Apache Mina/JBoss Netty
	//*************************************************************************************
	public String getConnectedFrom() {
		if (Server.useNetty) {
			return ((InetSocketAddress)getChannel().getRemoteAddress()).getAddress().getHostAddress();
		} else {
			return ((InetSocketAddress)getSession().getRemoteAddress()).getAddress().getHostAddress();
		}	
	}	
	
	public boolean isSessionClosed() {
		if (Server.useNetty) {
			return getChannel() == null || !getChannel().isOpen();
		} else {
			return getSession() == null || getSession().isClosing();
		}
	}
}