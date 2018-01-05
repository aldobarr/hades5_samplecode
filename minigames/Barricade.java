package server.model.minigames;

/**
 * 
 * @author hadesflames
 * 
 */
public class Barricade{
	public int id, absX, absY, heightLevel, team;
	public long burnTime;
	public boolean burning = false;

	public Barricade(int id, int x, int y, int height, int team){
		this.id = id;
		this.absX = x;
		this.absY = y;
		this.heightLevel = height;
		this.team = team;
	}
}