package server.model.players.skills;

public class SlayerTask{
	public int monster;
	public int requirement;
	public int level;
	public int amount;
	
	public SlayerTask(int monster, int req, int level){
		this(monster, req, level, 0);
	}
	
	public SlayerTask(int monster, int req, int level, int amount){
		this.monster = monster;
		this.requirement = req;
		this.level = level;
		this.amount = amount;
	}
	
	public SlayerTask clone(){
		return new SlayerTask(monster, requirement, level, amount);
	}
}