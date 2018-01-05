package server.model.players.packets;

import server.model.minigames.ClanWarsSettings;
import server.model.minigames.Duel;
import server.model.players.Client;
import server.model.players.PacketType;
import server.model.players.PlayerHandler;

/**
 * 
 * @author hadesflames
 * 
 */
public class ChallengePlayer implements PacketType{
	public void processPacket(Client c, int packetType, int packetSize){
		int otherPlayer = c.getInStream().readUnsignedWord();
		if(PlayerHandler.players[otherPlayer] == null && packetType != 125)
			return;
		if(packetType == 125){
			c.closeClanWars();
		}else if(packetType == 127){
			ClanWarsSettings.sendChallenge(c, (Client)PlayerHandler.players[otherPlayer]);
		}else if(packetType == 128){
			if(c.arenas() || (c.duel != null && c.duel.status >= 3)){
				c.sendMessage("You can't challenge inside the arena!");
				return;
			}
			Duel.sendRequest(c, otherPlayer);
		}
	}
}