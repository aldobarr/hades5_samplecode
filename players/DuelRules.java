package server.model.players;

public class DuelRules{
	public boolean duelRule[] = new boolean[22];
	public int player_one, player_two;
	public boolean canShout;
	
	public DuelRules(int player_one, int player_two){
		this.player_one = player_one;
		this.player_two = player_two;
		this.canShout = true;
	}
}