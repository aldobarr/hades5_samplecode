package server.model.players.packets;

import server.Config;
import server.model.players.Client;
import server.model.players.PacketType;

/**
 * Trading
 */
public class Trade implements PacketType{

	// @SuppressWarnings("unused")
	@Override
	public void processPacket(Client c, int packetType, int packetSize){
		if(!Config.CAN_TRADE)
			return;
		int tradeId = c.getInStream().readSignedWordBigEndian();
		c.getPA().resetFollow();
		if(c.duel != null && c.duel.status > 0)
			c.sendMessage("You can not trade at this time.");
		else if(c.playerRights == 2 && !Config.ADMIN_CAN_TRADE)
			c.sendMessage("Administrators can't trade.");
		else if(tradeId != c.playerId)
			c.getTradeAndDuel().requestTrade(tradeId);
	}
}