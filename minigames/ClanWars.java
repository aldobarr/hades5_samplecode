package server.model.minigames;

import java.util.ArrayList;
import server.model.players.Client;
import server.model.players.PlayerHandler;
import server.util.Misc;

/**
 * 
 * @author hadesflames
 * 
 */
public class ClanWars{
	private ClanWarsSettings settings;
	private int endTime;
	private static ArrayList<ClanWars> clanWars = new ArrayList<ClanWars>();
	private static ArrayList<ClanWars> remove = new ArrayList<ClanWars>();
	
	public ClanWars(ClanWarsSettings cws){
		settings = cws;
		endTime = settings.getTimeLimit() > 0 ? Misc.currentTimeSeconds() + 5 + (settings.getTimeLimit() * 60) : -1;
		synchronized(clanWars){
			clanWars.add(this);
		}
	}

	/**
	 * Processes all the games.
	 */
	public static void process(){
		for(ClanWars cw : clanWars){
			if(cw.endTime > 0 && cw.endTime <= Misc.currentTimeSeconds()){
				remove.add(cw);
				cw.endGame();
			}else if(cw.settings.getVictory() == -1 && (cw.settings.getClan1().warMembersLeft <= 0 || cw.settings.getClan2().warMembersLeft <= 0)){
				remove.add(cw);
				cw.endGame();
			}else if(cw.settings.getVictory() > 0 && (cw.settings.getVictory() <= cw.settings.getClan1().kills || cw.settings.getVictory() <= cw.settings.getClan2().kills)){
				remove.add(cw);
				cw.endGame();
			}else{
				if(cw.settings.getVictory() > -1){ // Restore players.
					for(int i = 0; i < cw.settings.getClan1().activeMembers.length; i++){
						Client c = cw.settings.getClan1().activeMembers[i] > 0 ? (Client)PlayerHandler.players[cw.settings.getClan1().activeMembers[i]] : null;
						Client o = cw.settings.getClan2().activeMembers[i] > 0 ? (Client)PlayerHandler.players[cw.settings.getClan2().activeMembers[i]] : null;
						if(c != null && c.inClanWars && c.inCWJail && Misc.currentTimeSeconds() >= c.cwJailTime && c.cwJailTime > 0){
							synchronized(c){
								c.cwJailTime = -1;
								c.inCWJail = false;
								c.getPA().movePlayer(3297 + Misc.random(5), 3722 + Misc.random(3), cw.settings.getHeight());
								cw.settings.getClan1().warMembersLeft++;
							}
						}
						if(o != null && o.inClanWars && o.inCWJail && Misc.currentTimeSeconds() >= o.cwJailTime && o.cwJailTime > 0){
							synchronized(o){
								o.cwJailTime = -1;
								o.inCWJail = false;
								o.getPA().movePlayer(3294 - Misc.random(5), 3830 - Misc.random(3), cw.settings.getHeight());
								cw.settings.getClan2().warMembersLeft++;
							}
						}
					}
				}
				cw.sendInfo();
				cw.checkDefaultVictory();
			}
		}
		if(remove.size() > 0)
			for(ClanWars cw : remove)
				if(clanWars.contains(cw))
					clanWars.remove(cw);
		remove.clear();
	}

	/**
	 * If there are ever no clan members left in the war, another clan will have won.
	 */
	private void checkDefaultVictory(){
		int clan1 = 0, clan2 = 0;
		for(int i = 0; i < settings.getClan1().activeMembers.length; i++){
			Client c = settings.getClan1().activeMembers[i] > 0 ? (Client)PlayerHandler.players[settings.getClan1().activeMembers[i]] : null;
			Client o = settings.getClan2().activeMembers[i] > 0 ? (Client)PlayerHandler.players[settings.getClan2().activeMembers[i]] : null;
			if(c != null && c.inClanWars)
				clan1++;
			if(o != null && o.inClanWars)
				clan2++;
		}
		if(clan1 == 0 || clan2 == 0){
			settings.setVictory(-1);
			ClanWars.remove.add(this);
			endGame();
		}
	}
	
	/**
	 * Sends the clan wars information, such as Time left, kills and amount of
	 * members left to each client.
	 */
	private void sendInfo(){
		for(int i = 0; i < settings.getClan1().activeMembers.length; i++){
			Client c = settings.getClan1().activeMembers[i] > 0 ? (Client)PlayerHandler.players[settings.getClan1().activeMembers[i]] : null;
			Client o = settings.getClan2().activeMembers[i] > 0 ? (Client)PlayerHandler.players[settings.getClan2().activeMembers[i]] : null;
			if(c != null && c.inClanWars && c.inClanWars()){
				synchronized(c){
					c.getPA().sendText("" + settings.getClan1().warMembersLeft, 18709);
					c.getPA().sendText("" + settings.getClan2().warMembersLeft, 18711);
					if(settings.getVictory() > -1){
						c.getPA().sendText("" + settings.getClan1().kills, 18710);
						c.getPA().sendText("" + settings.getClan2().kills, 18712);
					}
				}
			}
			if(o != null && o.inClanWars && o.inClanWars()){
				synchronized(o){
					o.getPA().sendText("" + settings.getClan2().warMembersLeft, 18709);
					o.getPA().sendText("" + settings.getClan1().warMembersLeft, 18711);
					if(settings.getVictory() > -1){
						o.getPA().sendText("" + settings.getClan2().kills, 18710);
						o.getPA().sendText("" + settings.getClan1().kills, 18712);
					}
				}
			}
		}
	}

	/**
	 * Ends the clan wars game for this war.
	 */
	private void endGame(){
		boolean draw = settings.getClan1().kills == settings.getClan2().kills;
		boolean clanOneWon = settings.getVictory() > -1 ? settings.getClan1().kills > settings.getClan2().kills : settings.getClan1().warMembersLeft > 0;
		for(int i = 0; i < settings.getClan1().activeMembers.length; i++){
			Client c = settings.getClan1().activeMembers[i] > 0 ? (Client)PlayerHandler.players[settings.getClan1().activeMembers[i]] : null;
			Client o = settings.getClan2().activeMembers[i] > 0 ? (Client)PlayerHandler.players[settings.getClan2().activeMembers[i]] : null;
			if(c != null && c.inClanWars){
				synchronized(c){
					c.getPA().resetOverLoad();
					int id = clanOneWon ? 51000 : 51010;
					c.getPA().movePlayer(3272, 3692, 0);
					if(!draw)
						c.getPA().showInterface(id);
					else
						c.sendMessage("The battle was a draw.");
					c.getPA().showInterface(18725);
					c.getPA().walkableInterface(-1);
					c.inClanWars = false;
					c.inCWJail = false;
					c.clanWarsWalkInterface = -1;
					c.cwJailTime = -1;
				}
			}
			if(o != null && o.inClanWars){
				synchronized(o){
					o.getPA().resetOverLoad();
					int id = !clanOneWon ? 51000 : 51010;
					o.getPA().movePlayer(3272, 3692, 0);
					if(!draw)
						o.getPA().showInterface(id);
					else
						o.sendMessage("The battle was a draw.");
					o.getPA().showInterface(18725);
					o.getPA().walkableInterface(-1);
					o.inClanWars = false;
					o.inCWJail = false;
					o.clanWarsWalkInterface = -1;
					o.cwJailTime = -1;
				}
			}
		}
		settings.resetVals();
	}
}