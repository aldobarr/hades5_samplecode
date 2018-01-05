package server.model.players.packets;

import server.model.players.Client;
import server.model.players.PacketType;

public class LightFloorLogs implements PacketType{
	public void processPacket(Client c, int packetType, int packetSize){
		c.getInStream().readSignedWord();
		c.getInStream().readUnsignedWordBigEndian();
		c.getInStream().readUnsignedWordBigEndianA();
	}
}