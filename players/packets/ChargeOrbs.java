package server.model.players.packets;

import server.model.players.Client;
import server.model.players.PacketType;

/**
 * Magic on items
 **/
public class ChargeOrbs implements PacketType{

	@Override
	public void processPacket(Client c, int packetType, int packetSize){
		c.getInStream().readUnsignedWordBigEndian();
		c.getInStream().readUnsignedWordA();
		c.getInStream().readUnsignedWordBigEndian();
		c.getInStream().readSignedWordA();
	}
}