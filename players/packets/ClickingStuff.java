package server.model.players.packets;

import server.model.minigames.Duel;
import server.model.players.Client;
import server.model.players.PacketType;

/**
 * Clicking stuff (interfaces)
 **/
public class ClickingStuff implements PacketType{

	@Override
	public void processPacket(Client c, int packetType, int packetSize){
		if(c.inTrade)
			c.getTradeAndDuel().declineTrade(true);
		if(c.isBanking)
			c.isBanking = false;
		if(c.isShopping)
			c.isShopping = false;
		if(c.duel != null && c.duel.status >= 1 && c.duel.status <= 2)
			Duel.declineDuel(c, true);
	}
}