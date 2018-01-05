package server.model.players.packets;

import server.Config;
import server.Server;
import server.model.players.Client;
import server.model.players.PacketType;
import server.model.quests.DemonSlayer;

/**
 * Pickup Item
 **/
public class PickupItem implements PacketType{
	public void processPacket(Client c, int packetType, int packetSize){
		c.pItemY = c.getInStream().readSignedWordBigEndian();
		c.pItemId = c.getInStream().readUnsignedWord();
		c.pItemX = c.getInStream().readSignedWordBigEndian();
		if(Math.abs(c.getX() - c.pItemX) > 25 || Math.abs(c.getY() - c.pItemY) > 25){
			c.resetWalkingQueue();
			return;
		}
		boolean pickup = c.getCannon().canPickupCannon(c.pItemId);
		if(c.pItemId >= 6865 && c.pItemId <= 6882){
			c.sendMessage("You can't pick this up, this is for the children!");
			return;
		}
		if(c.pItemId == Config.ZOMBIE_HEAD)
			pickup = !c.inventory.hasItem(Config.ZOMBIE_HEAD) && c.bank.getBankAmount(Config.ZOMBIE_HEAD) == 0;
		if((c.inventory.hasItem(c.pItemId) || c.bank.getBankAmount(c.pItemId) > 0) && (c.pItemId == 11019 || c.pItemId == 11020 || c.pItemId == 11021 || c.pItemId == 11022))
			pickup = false;
		if((c.pItemId == 11019 || c.pItemId == 11020 || c.pItemId == 11021 || c.pItemId == 11022) && (c.playerEquipment[c.playerHat] == c.pItemId || c.playerEquipment[c.playerChest] == c.pItemId || c.playerEquipment[c.playerLegs] == c.pItemId || c.playerEquipment[c.playerFeet] == c.pItemId))
			pickup = false;
		c.getCombat().resetPlayerAttack();
		if(!Server.itemHandler.isCreator(c.playerName, c.pItemId, c.pItemX, c.pItemY, c.heightLevel) && c.pItemId == DemonSlayer.SILVER_LIGHT)
			return;
		if(!pickup)
			c.sendMessage("You already have one of these.");
		else if(c.getX() == c.pItemX && c.getY() == c.pItemY)
			Server.itemHandler.removeGroundItem(c, c.pItemId, c.pItemX, c.pItemY, c.heightLevel, true);
		else
			c.walkingToItem = true;
	}
}
