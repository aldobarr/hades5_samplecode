package server.model.minigames;

import java.util.ArrayList;

import server.Server;
import server.model.HadesThread;
import server.model.players.Client;
import server.model.players.PlayerHandler;
import server.util.Misc;
import server.world.Clan;

/**
 * 
 * @author hadesflames
 * 
 */
public class ClanWarsSettings{
	private Clan c1, c2;
	private boolean c1Accepted = false, c2Accepted = false;
	private int victory = -1, time_limit = 0;
	private boolean keep_items = true;
	private boolean melee = true;
	private boolean magic = true;
	private boolean range = true;
	private boolean prayer = true;
	private boolean food = true;
	private boolean potions = true;
	private int jailOne[] = {3319, 3764}; // + 2, + 8
	private int jailTwo[] = {3319, 3775}; // + 2, + 8

	public ClanWarsSettings(Clan c1, Clan c2){
		this.c1 = c1;
		this.c2 = c2;
	}

	/**
	 * Begins the clan war with the settings the leaders applied.
	 */
	public void start(){
		c1.inWar = c2.inWar = true;
		int clanData = victory > 0 ? 18700 : 18720;
		int keepItems = keep_items ? 0 : 1;
		ArrayList<String[]> data = new ArrayList<String[]>();
		boolean powerwars = ((c1.name.equalsIgnoreCase("powerwars t1") && c2.name.equalsIgnoreCase("powerwars t2")) || (c1.name.equalsIgnoreCase("powerwars t2") && c2.name.equalsIgnoreCase("powerwars t1")));
		for(int i = 0; i < c1.activeMembers.length; i++){
			Client c = c1.activeMembers[i] > 0 ? (Client)PlayerHandler.players[c1.activeMembers[i]] : null;
			Client o = c2.activeMembers[i] > 0 ? (Client)PlayerHandler.players[c2.activeMembers[i]] : null;
			if(c != null && c.inClanWarsWait() && !c.teleporting){
				c.inClanWars = true;
				c1.warMembersLeft++;
				c.clanWarsWalkInterface = clanData;
				c.getPA().movePlayer(3297 + Misc.random(5), 3722 + Misc.random(3), 4 * c1.warLeader);
				c.getPA().walkableInterface(clanData);
				c.getPA().showInterface(18725);
				if(victory > 0)
					c.getPA().sendText("/ " + victory, 18707);
				c.getPA().sendText(c1.name + ":", 18704);
				c.getPA().sendConfig(420, keepItems);
				c.getPA().sendText("0", 18709);
				c.getPA().sendText("0", 18710);
				c.getPA().sendText("0", 18711);
				c.getPA().sendText("0", 18712);
				if(powerwars)
					data.add(new String[]{c.originalName, c1.name});
			}
			if(o != null && o.inClanWarsWait() && !o.teleporting){
				o.inClanWars = true;
				c2.warMembersLeft++;
				o.clanWarsWalkInterface = clanData;
				o.getPA().movePlayer(3294 - Misc.random(5), 3830 - Misc.random(3), 4 * c1.warLeader);
				o.getPA().walkableInterface(clanData);
				o.getPA().showInterface(18725);
				if(victory > 0)
					o.getPA().sendText("/ " + victory, 18707);
				o.getPA().sendText(c2.name + ":", 18704);
				o.getPA().sendConfig(420, keepItems);
				o.getPA().sendText("0", 18709);
				o.getPA().sendText("0", 18710);
				o.getPA().sendText("0", 18711);
				o.getPA().sendText("0", 18712);
				if(powerwars)
					data.add(new String[]{o.originalName, c2.name});
			}
		}
		if(powerwars)
			new HadesThread(HadesThread.POWERWARS, data);
		new ClanWars(this);
	}

	/**
	 * Handles button clicking for the clan wars settings interface.
	 * 
	 * @param c
	 *            The client sending the button click.
	 * @param buttonId
	 *            The id of the button being clicked.
	 */
	public void handleSettingsButton(Client c, int buttonId){
		if(c.clanId.isEmpty() || !Server.clanChat.clans.containsKey(c.clanId) || c.playerId != Server.clanChat.clans.get(c.clanId).warLeader)
			return;
		Client o = (Client)PlayerHandler.players[c1.warLeader == c.playerId ? c2.warLeader : c1.warLeader];
		if(o == null){
			return;
		}
		boolean changed = false;
		switch(buttonId){
			case 199085:
				keep_items = !keep_items;
				c.getPA().sendConfig(566, keep_items ? 0 : 1);
				o.getPA().sendConfig(566, keep_items ? 0 : 1);
				String dropText = keep_items ? "keep" : "DROP ALL";
				c.getPA().sendText("...you " + dropText + "\\nyour items.", 51030);
				o.getPA().sendText("...you " + dropText + "\\nyour items.", 51030);
				if(c1Accepted || c2Accepted)
					resetButton(c, o);
				break;
			case 199100:
				c.getPA().sendConfig(567, 1);
				c.getPA().sendConfig(571, 0);
				o.getPA().sendConfig(567, 1);
				o.getPA().sendConfig(571, 0);
				changed = victory != -1;
				victory = -1; // Knock-out
				if(changed && (c1Accepted || c2Accepted))
					resetButton(c, o);
				break;
			case 199101:
				c.getPA().sendConfig(567, 2);
				c.getPA().sendConfig(571, 0);
				o.getPA().sendConfig(567, 2);
				o.getPA().sendConfig(571, 0);
				changed = victory != 0 || time_limit == 0;
				victory = 0; // Most kills at end
				if(time_limit == 0){
					time_limit = 5;
					c.getPA().sendConfig(569, 0);
					c.getPA().sendConfig(572, 1);
					o.getPA().sendConfig(569, 0);
					o.getPA().sendConfig(572, 1);
				}
				if(changed && (c1Accepted || c2Accepted))
					resetButton(c, o);
				break;
			case 199102: // Stragglers (Kill 'em all)
			case 199103: // Stragglers (ignore 5)
				break;
			case 199104:
				c.getPA().sendConfig(569, 1);
				c.getPA().sendConfig(572, 0);
				o.getPA().sendConfig(569, 1);
				o.getPA().sendConfig(572, 0);
				changed = time_limit != 0 || victory == 0;
				time_limit = 0;
				if(victory == 0){
					victory = -1;
					c.getPA().sendConfig(567, 1);
					o.getPA().sendConfig(567, 1);
					c.getPA().sendConfig(571, 0);
					o.getPA().sendConfig(571, 0);
				}
				if(changed && (c1Accepted || c2Accepted))
					resetButton(c, o);
				break;
			case 199105:
				if(!magic && !range){
					c.sendMessage("You can not turn off all forms of combat.");
					c.getPA().sendConfig(570, melee ? 0 : 1);
					break;
				}
				melee = !melee;
				c.getPA().sendConfig(570, melee ? 0 : 1);
				o.getPA().sendConfig(570, melee ? 0 : 1);
				if(c1Accepted || c2Accepted)
					resetButton(c, o);
				break;
			case 199106:
				if(!melee && !range){
					c.sendMessage("You can not turn off all forms of combat.");
					c.getPA().sendConfig(574, magic ? 0 : 1);
					break;
				}
				magic = !magic;
				c.getPA().sendConfig(574, magic ? 0 : 1);
				o.getPA().sendConfig(574, magic ? 0 : 1);
				if(c1Accepted || c2Accepted)
					resetButton(c, o);
				break;
			case 199107:
				if(!magic && !melee){
					c.sendMessage("You can not turn off all forms of combat.");
					c.getPA().sendConfig(575, range ? 0 : 1);
					break;
				}
				range = !range;
				c.getPA().sendConfig(575, range ? 0 : 1);
				o.getPA().sendConfig(575, range ? 0 : 1);
				if(c1Accepted || c2Accepted)
					resetButton(c, o);
				break;
			case 199108:
				prayer = !prayer;
				c.getPA().sendConfig(576, prayer ? 0 : 1);
				o.getPA().sendConfig(576, prayer ? 0 : 1);
				if(c1Accepted || c2Accepted)
					resetButton(c, o);
				break;
			case 199110:
				food = !food;
				c.getPA().sendConfig(578, food ? 0 : 1);
				o.getPA().sendConfig(578, food ? 0 : 1);
				if(c1Accepted || c2Accepted)
					resetButton(c, o);
				break;
			case 199111:
				potions = !potions;
				c.getPA().sendConfig(579, potions ? 0 : 1);
				o.getPA().sendConfig(579, potions ? 0 : 1);
				if(c1Accepted || c2Accepted)
					resetButton(c, o);
				break;
			case 199112:
				boolean other;
				if(c1.warLeader == c.playerId){
					c1Accepted = true;
					other = c2Accepted;
				}else{
					c2Accepted = true;
					other = c1Accepted;
				}
				if(!other){
					c.getPA().sendConfig(581, 1);
					c.getPA().sendText("", 51125);
					c.getPA().sendText("Waiting for\\nopponent...", 51126);
					c.getPA().sendText("", 51127);
					o.getPA().sendText("", 51125);
					o.getPA().sendText("", 51126);
					o.getPA().sendText("Accept -\\nOpponent has\\naccepted.", 51127);
				}else{
					c.getPA().closeAllWindows();
					o.getPA().closeAllWindows();
					resetButton(c, o);
					start();
				}
				break;
			case 199127:
				c.getPA().sendConfig(567, 0);
				c.getPA().sendConfig(571, 1);
				o.getPA().sendConfig(567, 0);
				o.getPA().sendConfig(571, 1);
				changed = victory != 25;
				victory = 25;
				if(changed && (c1Accepted || c2Accepted))
					resetButton(c, o);
				break;
			case 199128:
				c.getPA().sendConfig(567, 0);
				c.getPA().sendConfig(571, 2);
				o.getPA().sendConfig(567, 0);
				o.getPA().sendConfig(571, 2);
				changed = victory != 50;
				victory = 50;
				if(changed && (c1Accepted || c2Accepted))
					resetButton(c, o);
				break;
			case 199129:
				c.getPA().sendConfig(567, 0);
				c.getPA().sendConfig(571, 3);
				o.getPA().sendConfig(567, 0);
				o.getPA().sendConfig(571, 3);
				changed = victory != 100;
				victory = 100;
				if(changed && (c1Accepted || c2Accepted))
					resetButton(c, o);
				break;
			case 199130:
				c.getPA().sendConfig(567, 0);
				c.getPA().sendConfig(571, 4);
				o.getPA().sendConfig(567, 0);
				o.getPA().sendConfig(571, 4);
				changed = victory != 200;
				victory = 200;
				if(changed && (c1Accepted || c2Accepted))
					resetButton(c, o);
				break;
			case 199131:
				c.getPA().sendConfig(567, 0);
				c.getPA().sendConfig(571, 5);
				o.getPA().sendConfig(567, 0);
				o.getPA().sendConfig(571, 5);
				changed = victory != 400;
				victory = 400;
				if(changed && (c1Accepted || c2Accepted))
					resetButton(c, o);
				break;
			case 199132:
				c.getPA().sendConfig(567, 0);
				c.getPA().sendConfig(571, 6);
				o.getPA().sendConfig(567, 0);
				o.getPA().sendConfig(571, 6);
				changed = victory != 750;
				victory = 750;
				if(changed && (c1Accepted || c2Accepted))
					resetButton(c, o);
				break;
			case 199133:
				c.getPA().sendConfig(567, 0);
				c.getPA().sendConfig(571, 7);
				o.getPA().sendConfig(567, 0);
				o.getPA().sendConfig(571, 7);
				changed = victory != 1000;
				victory = 1000;
				if(changed && (c1Accepted || c2Accepted))
					resetButton(c, o);
				break;
			case 199134:
				c.getPA().sendConfig(567, 0);
				c.getPA().sendConfig(571, 8);
				o.getPA().sendConfig(567, 0);
				o.getPA().sendConfig(571, 8);
				changed = victory != 2500;
				victory = 2500;
				if(changed && (c1Accepted || c2Accepted))
					resetButton(c, o);
				break;
			case 199135:
				c.getPA().sendConfig(567, 0);
				c.getPA().sendConfig(571, 9);
				o.getPA().sendConfig(567, 0);
				o.getPA().sendConfig(571, 9);
				changed = victory != 5000;
				victory = 5000;
				if(changed && (c1Accepted || c2Accepted))
					resetButton(c, o);
				break;
			case 199136:
				c.getPA().sendConfig(567, 0);
				c.getPA().sendConfig(571, 10);
				o.getPA().sendConfig(567, 0);
				o.getPA().sendConfig(571, 10);
				changed = victory != 10000;
				victory = 10000;
				if(changed && (c1Accepted || c2Accepted))
					resetButton(c, o);
				break;
			case 199150:
				c.getPA().sendConfig(569, 0);
				c.getPA().sendConfig(572, 1);
				o.getPA().sendConfig(569, 0);
				o.getPA().sendConfig(572, 1);
				changed = time_limit != 5;
				time_limit = 5;
				if(changed && (c1Accepted || c2Accepted))
					resetButton(c, o);
				break;
			case 199151:
				c.getPA().sendConfig(569, 0);
				c.getPA().sendConfig(572, 2);
				o.getPA().sendConfig(569, 0);
				o.getPA().sendConfig(572, 2);
				changed = time_limit != 10;
				time_limit = 10;
				if(changed && (c1Accepted || c2Accepted))
					resetButton(c, o);
				break;
			case 199152:
				c.getPA().sendConfig(569, 0);
				c.getPA().sendConfig(572, 3);
				o.getPA().sendConfig(569, 0);
				o.getPA().sendConfig(572, 3);
				changed = time_limit != 30;
				time_limit = 30;
				if(changed && (c1Accepted || c2Accepted))
					resetButton(c, o);
				break;
			case 199153:
				c.getPA().sendConfig(569, 0);
				c.getPA().sendConfig(572, 4);
				o.getPA().sendConfig(569, 0);
				o.getPA().sendConfig(572, 4);
				changed = time_limit != 60;
				time_limit = 60;
				if(changed && (c1Accepted || c2Accepted))
					resetButton(c, o);
				break;
			case 199154:
				c.getPA().sendConfig(569, 0);
				c.getPA().sendConfig(572, 5);
				o.getPA().sendConfig(569, 0);
				o.getPA().sendConfig(572, 5);
				changed = time_limit != 90;
				time_limit = 90;
				if(changed && (c1Accepted || c2Accepted))
					resetButton(c, o);
				break;
			case 199155:
				c.getPA().sendConfig(569, 0);
				c.getPA().sendConfig(572, 6);
				o.getPA().sendConfig(569, 0);
				o.getPA().sendConfig(572, 6);
				changed = time_limit != 120;
				time_limit = 120;
				if(changed && (c1Accepted || c2Accepted))
					resetButton(c, o);
				break;
			case 199156:
				c.getPA().sendConfig(569, 0);
				c.getPA().sendConfig(572, 7);
				o.getPA().sendConfig(569, 0);
				o.getPA().sendConfig(572, 7);
				changed = time_limit != 150;
				time_limit = 150;
				if(changed && (c1Accepted || c2Accepted))
					resetButton(c, o);
				break;
			case 199157:
				c.getPA().sendConfig(569, 0);
				c.getPA().sendConfig(572, 8);
				o.getPA().sendConfig(569, 0);
				o.getPA().sendConfig(572, 8);
				changed = time_limit != 180;
				time_limit = 180;
				if(changed && (c1Accepted || c2Accepted))
					resetButton(c, o);
				break;
			case 199158:
				c.getPA().sendConfig(569, 0);
				c.getPA().sendConfig(572, 9);
				o.getPA().sendConfig(569, 0);
				o.getPA().sendConfig(572, 9);
				changed = time_limit != 240;
				time_limit = 240;
				if(changed && (c1Accepted || c2Accepted))
					resetButton(c, o);
				break;
			case 199159:
				c.getPA().sendConfig(569, 0);
				c.getPA().sendConfig(572, 10);
				o.getPA().sendConfig(569, 0);
				o.getPA().sendConfig(572, 10);
				changed = time_limit != 300;
				time_limit = 300;
				if(changed && (c1Accepted || c2Accepted))
					resetButton(c, o);
				break;
			case 199160:
				c.getPA().sendConfig(569, 0);
				c.getPA().sendConfig(572, 11);
				o.getPA().sendConfig(569, 0);
				o.getPA().sendConfig(572, 11);
				changed = time_limit != 360;
				time_limit = 360;
				if(changed && (c1Accepted || c2Accepted))
					resetButton(c, o);
				break;
			case 199161:
				c.getPA().sendConfig(569, 0);
				c.getPA().sendConfig(572, 12);
				o.getPA().sendConfig(569, 0);
				o.getPA().sendConfig(572, 12);
				changed = time_limit != 480;
				time_limit = 480;
				if(changed && (c1Accepted || c2Accepted))
					resetButton(c, o);
				break;
		}
	}

	/**
	 * Clear the clan wars settings interfaces for the players.
	 */
	public void clearInterfaces(){
		Client one = c1.warLeader < 1 || c1.warLeader >= PlayerHandler.players.length ? null : (Client)PlayerHandler.players[c1.warLeader];
		Client two = c2.warLeader < 1 || c2.warLeader >= PlayerHandler.players.length ? null : (Client)PlayerHandler.players[c2.warLeader];
		if(one != null)
			one.getPA().closeAllWindows();
		if(two != null)
			two.getPA().closeAllWindows();
	}

	/**
	 * Resets all clan wars variables, and closes the interfaces if
	 * clearInterfaces variable is true.
	 * 
	 * @param clearInterfaces
	 *            Variable that tells whether or not to try to close interfaces
	 *            for the war leaders.
	 */
	public void resetVals(){
		c1.inWar = c2.inWar = false;
		c1.otherClan = c2.otherClan = "";
		c1.warLeader = c2.warLeader = -1;
		c1.kills = c2.kills = 0;
		c1.warMembersLeft = c2.warMembersLeft = 0;
		c1.otherLeader = c2.otherLeader = -1;
		c1.sentRequest = c2.sentRequest = false;
		c1.war = c2.war = null;
	}

	/**
	 * Resets the accept button, usually when a setting is changed in the setup
	 * interface.
	 * 
	 * @param c
	 *            The first player.
	 * @param o
	 *            The second player.
	 */
	public void resetButton(Client c, Client o){
		if(c == null || o == null)
			return;
		c1Accepted = c2Accepted = false;
		c.getPA().sendConfig(581, 0);
		o.getPA().sendConfig(581, 0);
		c.getPA().sendText("Accept", 51125);
		c.getPA().sendText("", 51126);
		c.getPA().sendText("", 51127);
		o.getPA().sendText("Accept", 51125);
		o.getPA().sendText("", 51126);
		o.getPA().sendText("", 51127);
	}

	/**
	 * @return Clan 1
	 */
	public Clan getClan1(){
		return c1;
	}

	/**
	 * @return Clan 2
	 */
	public Clan getClan2(){
		return c2;
	}

	/**
	 * @return Victory conditions
	 */
	public int getVictory(){
		return victory;
	}
	
	/**
	 * Sets the victory variable to the specified value.
	 * @param victory The new value.
	 */
	public void setVictory(int victory){
		this.victory = victory;
	}

	/**
	 * @return Time limit setting
	 */
	public int getTimeLimit(){
		return time_limit;
	}

	/**
	 * @return Whether or not you keep items
	 */
	public boolean getKeepItems(){
		return keep_items;
	}

	/**
	 * @return Melee permission
	 */
	public boolean getMelee(){
		return melee;
	}

	/**
	 * @return Magic permission
	 */
	public boolean getMagic(){
		return magic;
	}

	/**
	 * @return Range permission
	 */
	public boolean getRange(){
		return range;
	}

	/**
	 * @return Prayer permission
	 */
	public boolean getPrayer(){
		return prayer;
	}

	/**
	 * @return Food permission
	 */
	public boolean getFood(){
		return food;
	}

	/**
	 * @return Potions permission
	 */
	public boolean getPotions(){
		return potions;
	}

	/**
	 * @return South Jail
	 */
	public int[] getJailOne(){
		return jailOne;
	}

	/**
	 * @return North Jail
	 */
	public int[] getJailTwo(){
		return jailTwo;
	}

	/**
	 * @param clan
	 *            The clan the member is in.
	 * @return The corresponding jail for the person.
	 */
	public int[] getJail(Clan clan){
		return clan.name.equals(c1.name) ? jailOne : jailTwo;
	}

	/**
	 * @return The height of the war.
	 */
	public int getHeight(){
		return c1.warLeader * 4;
	}

	/**
	 * @param clan
	 *            The member's current clan.
	 * @return The member's opposing clan.
	 */
	public Clan getOpposingTeam(Clan clan){
		return clan.name.equals(c1.name) ? c2 : c1;
	}

	/**
	 * Sends and creates challenges for clan wars.
	 * 
	 * @param c1
	 *            The challenger.
	 * @param c2
	 *            The challengee.
	 */
	public static void sendChallenge(Client c1, Client c2){
		if(!c1.inClanWarsWait() || !c2.inClanWarsWait())
			return;
		if(c1.clanId.isEmpty()){
			c1.sendMessage("You must be in a clan chat to do that.");
			return;
		}
		if(c2.clanId.isEmpty()){
			c1.sendMessage("The other player must be in a clan chat to do that.");
			return;
		}
		if(c1.clanId.equalsIgnoreCase(c2.clanId)){
			c1.sendMessage("You can't challenge one of your own clan mates!");
			return;
		}
		if(!Server.clanChat.clans.containsKey(c1.clanId) || !Server.clanChat.clans.containsKey(c2.clanId))
			return;
		Clan clan1 = Server.clanChat.clans.get(c1.clanId);
		Clan clan2 = Server.clanChat.clans.get(c2.clanId);
		if((clan1.warLeader != c1.playerId && clan1.sentRequest) || clan1.inWar){
			c1.sendMessage("Someone has already started a war for your clan.");
			return;
		}
		if(clan1.permissions[Clan.WAR] > 0 && (!clan1.members.containsKey(c1.playerName) || clan1.permissions[Clan.WAR] > clan1.members.get(c1.playerName))){
			c1.sendMessage("You do not have permission to start a clan war!");
			return;
		}
		if(clan2.permissions[Clan.WAR] > 0 && (!clan2.members.containsKey(c2.playerName) || clan2.permissions[Clan.WAR] > clan2.members.get(c2.playerName))){
			c1.sendMessage("The other player does not have permission to start a clan war!");
			return;
		}
		if(!clan2.sentRequest || !clan2.otherClan.equalsIgnoreCase(clan1.name) || clan2.warLeader != c2.playerId){
			c1.sendMessage("Sending challenge request...");
			c2.sendMessage(c1.playerName + ":cwreq:");
			clan1.warLeader = clan2.otherLeader = c1.playerId;
			clan2.warLeader = clan1.otherLeader = c2.playerId;
			clan1.otherClan = clan2.name;
			clan1.sentRequest = clan2.sentRequest = true;
		}else if(clan2.sentRequest && clan2.otherClan.equalsIgnoreCase(clan1.name)){
			clan1.otherClan = clan2.name;
			ClanWarsSettings war = new ClanWarsSettings(clan1, clan2);
			clan1.war = war;
			clan2.war = war;
			c1.getPA().sendConfig(566, 0);
			c2.getPA().sendConfig(566, 0);
			c1.getPA().sendConfig(569, 1);
			c1.getPA().sendConfig(572, 0);
			c2.getPA().sendConfig(569, 1);
			c2.getPA().sendConfig(572, 0);
			c1.getPA().sendConfig(567, 1);
			c2.getPA().sendConfig(567, 1);
			c1.getPA().sendConfig(571, 0);
			c2.getPA().sendConfig(571, 0);
			for(int i = 573; i < 580; i++){
				if(i != 577){
					c1.getPA().sendConfig(i, 0);
					c2.getPA().sendConfig(i, 0);
				}
			}
			c1.getPA().sendConfig(566, 0);
			c2.getPA().sendConfig(566, 0);
			c1.getPA().sendText("...you keep\\nyour items.", 51030);
			c2.getPA().sendText("...you keep\\nyour items.", 51030);
			c1.getPA().showInterface(51020);
			c2.getPA().showInterface(51020);
			clan1.otherClan = clan2.otherClan = "";
		}
	}
}