package server.model;

import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import server.Config;
import server.model.players.Client;
import server.util.Misc;

public class ScoreBoard{
	// Interface Text Ids.
	private int interfaceIds[] = {6402, 6403, 6404, 6405, 6406, 6407, 6408, 6409, 6410, 6411, 8578, 8579, 8580, 8581, 8582, 8583, 8584, 8585, 8586, 8587, 8588, 8589, 8590, 8591, 8592, 8593, 8594, 8595, 8596, 8597, 8598, 8599, 8600, 8601, 8602, 8603, 8604, 8605, 8606, 8607, 8608, 8609, 8610, 8611, 8612, 8613, 8614, 8615, 8616, 8617};
	// Entry compare function.
	private Comparator<Entry> c = new CompareEntry();
	// Scoreboard List.
	private Queue<Entry> list = new PriorityQueue<Entry>(interfaceIds.length, c);
	// Has the scoreboard been reset?
	private boolean reset = false;
	private Map<String, Entry> map = new HashMap<String, Entry>();
	public ScoreBoard(){}
	public void add(String originalName, String playerName, boolean kill){
		Entry newEntry = new Entry(originalName, playerName, kill ? 1 : 0, kill ? 0 : 1);
		map.put(originalName, newEntry);
		list.add(newEntry);
	}
	public void update(String originalName, String playerName, boolean kill){
		if(!map.containsKey(originalName)){
			add(originalName, playerName, kill);
			return;
		}
		Entry playerEntry = map.get(originalName);
		list.remove(playerEntry);
		if(kill)
			playerEntry.kills++;
		else
			playerEntry.deaths++;
		if(!playerName.equalsIgnoreCase(playerEntry.playerName))
			playerEntry.playerName = playerName;
		list.add(playerEntry);
	}
	public void showBoard(Client player){
		if(!player.scoreBoardReset)
			resetBoard(player);
		player.getPA().showInterface(6308);
		player.getPA().sendText(Config.SERVER_NAME + " PK Score Board", 6400);
		player.getPA().sendText("Today's Top 50 PKers:", 6399);
		Queue<Entry> tempQueue = new PriorityQueue<Entry>(list);
		if(tempQueue.isEmpty()){
			resetBoard(player);
			return;
		}
		int i = 0;
		do{
			if(i == interfaceIds.length)
				break;
			Entry playerEntry = tempQueue.poll();
			playerEntry.calculateKdr();
			player.getPA().sendText((i + 1) + ". " + playerEntry.playerName + " - " + playerEntry.kills + " Kills - " + playerEntry.deaths + " Deaths - " + Misc.decimalFormat(playerEntry.kdr) + " KDR", interfaceIds[i++]);
		}while(!tempQueue.isEmpty());
	}
	public void resetBoard(Client player){
		for(int id : interfaceIds)
			player.getPA().sendText("", id);
		player.scoreBoardReset = true;
	}
	public void resetList(){
		int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		if(hour == 0 && !reset){
			reset = true;
			list.clear();
			map.clear();
		}else if(hour == 1 && reset)
			reset = false;
	}
}
class Entry{
	String playerName, originalName;
	int kills, deaths;
	double kdr = 0.0;
	public Entry(String originalName, String playerName, int kills, int deaths){
		this.originalName = originalName;
		this.playerName = playerName;
		this.kills = kills;
		this.deaths = deaths;
	}
	public void calculateKdr(){
		kdr = deaths > 0 ? (double)kills / (double)deaths : kills;
	}
}
class CompareEntry implements Comparator<Entry>{
	public int compare(Entry o1, Entry o2){
		o1.calculateKdr();
		o2.calculateKdr();
		return o1.kdr < o2.kdr ? 1 : (o1.kdr == o2.kdr ? (o1.kills < o2.kills ? 1 : (o1.kills == o2.kills ? (o1.deaths > o2.deaths ? 1 : (o1.deaths == o2.deaths ? 0 : -1)) : -1)) : -1);
	}
}